/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka;

import com.oracle.coherence.common.base.Logger;

import com.tangosol.coherence.config.Config;
import com.tangosol.config.ConfigurationException;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.cache.NonBlockingEntryStore;
import com.tangosol.net.cache.StoreObserver;
import com.tangosol.util.BinaryEntry;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Set;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * Kafka entry store implementation.
 * <p>
 * Implements a mechanism to automatically send entries added or updated in Coherence,
 * to a Kafka cluster using a configured topic name.
 * <p>
 * Key and value serializers can be configured, and optionally a pass-through mode
 * can be used to avoid serialization. In this mode, the serialized version of an entry
 * is sent to Kafka, with the assumption that the system pulling this entry can process
 * raw Coherence entries.
 * <p>
 * The configuration is used as follows in the Coherence cache config file, in a
 * cachestore-scheme/class-scheme element, and handled by the {@link KafkaNamespaceHandler}
 * <p>
 * It passes the properties to be used by the Kafka producer according to the version
 * of kafka used. The properties are translated from configuration as such:
 * {@code bootstrap-servers} -&gt; {@code bootstrap.servers}, etc.
 * <p>
 * For example:
 * <pre>{@code
 *           &lt;cachestore-scheme&gt;
 *             &lt;class-scheme&gt;
 *               &lt;kafka:producer&gt;
 *                 &lt;kafka:entry-store-class-name&gt;com.oracle.coherence.kafka.KafkaNonBlockingEntryStore&lt;/kafka:entry-store-class-name&gt;
 *                 &lt;kafka:topic-name&gt;foo&lt;/kafka:topic-name&gt;
 *                 &lt;kafka:passthrough&gt;false&lt;/kafka:passthrough&gt;
 *                 &lt;kafka:bootstrap-servers&gt;localhost:9092&lt;/kafka:bootstrap-servers&gt;
 *                 &lt;kafka:key-serializer&gt;org.apache.kafka.common.serialization.StringSerializer&lt;/kafka:key-serializer&gt;
 *                 &lt;kafka:value-serializer&gt;org.apache.kafka.common.serialization.StringSerializer&lt;/kafka:value-serializer&gt;
 *                 &lt;kafka:max-block-ms&gt;5000&lt;/kafka:max-block-ms&gt;
 *               &lt;/kafka:producer>
 *             &lt;/class-scheme>
 *           &lt;/cachestore-scheme>
 * }</pre>
 *
 * @author Maurice Gamanho
 */
public class KafkaEntryStore<K, V>
        implements NonBlockingEntryStore<K, V>
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Constructor, must be configured using a &lt;kafka:producer&gt; element in the
     * cache-config file, in a /cache-config/../cachestore-scheme section. See
     * example below.
     *
     * @param props  Properties object constructed by the configuration
     *               builder in the KafkaProducer namespace
     *
     */
    public KafkaEntryStore(Properties props)
        {
        m_props = props;

        props.put("client.id", CacheFactory.getCluster().getLocalMember().getUid().toString());

        String sPassThrough = props.getProperty("passthrough");

        if (sPassThrough == null)
            {
            sPassThrough = Config.getProperty("coherence.kafka.passthrough");
            }
        f_fPassThrough = Boolean.parseBoolean(sPassThrough == null ? "false" : sPassThrough);

        if (!props.containsKey("key.serializer"))
            {
            props.put("key.serializer", org.apache.kafka.common.serialization.ByteArraySerializer.class.getName());
            }

        if (!props.containsKey("value.serializer"))
            {
            props.put("value.serializer", org.apache.kafka.common.serialization.ByteArraySerializer.class.getName());
            }

        m_sTopicName = props.getProperty("topic.name");
        if (m_sTopicName == null)
            {
            throw new ConfigurationException("topic.name not specified", "Specify a topic name using the topic.name property.");
            }
        }

    // ----- NonBlockingEntryStore implementation  --------------------------

    @Override
    public void load(BinaryEntry<K, V> binEntry, StoreObserver<K, V> observer)
        {
        // get is blocking, this unblocks it
        observer.onError(binEntry, new UnsupportedOperationException());
        }

    @Override
    public void loadAll(Set<? extends BinaryEntry<K, V>> setBinEntries, StoreObserver<K, V> observer)
        {
        // getAll is blocking, this unblocks it
        setBinEntries.forEach(entry -> load(entry, observer));
        }

    @Override
    public void store(BinaryEntry<K, V> binEntry, StoreObserver<K, V> observer)
        {
        byte[] abEventName = (binEntry.getOriginalBinaryValue() == null ? "INSERT" : "UPDATE").getBytes(StandardCharsets.UTF_8);

        Producer producer;
        try
            {
            producer = ensureProducer();

            ProducerRecord record = new ProducerRecord(
                    m_sTopicName,
                    f_fPassThrough ? binEntry.getBinaryKey().toByteArray() : binEntry.getKey(),
                    f_fPassThrough ? binEntry.getBinaryValue().toByteArray() : binEntry.getValue());

            record.headers().add("event", abEventName);

            producer.send(record, ((metadata, exception) ->
                                   {
                                   if (exception != null)
                                       {
                                       Logger.err(() -> "Failed to publish record for key: " + binEntry.getKey() + " to topic: " + m_sTopicName);
                                       observer.onError(binEntry, exception);
                                       }
                                   else
                                       {
                                       Logger.finest(() -> "Published record for key: " + binEntry.getKey()
                                                           + " to topic: " + metadata.topic()
                                                           + " (partition=" + metadata.partition()
                                                           + ", offset=" + metadata.offset()
                                                           + ", timestamp=" + metadata.timestamp()
                                                           + ")");
                                       observer.onNext(binEntry);
                                       }
                                   }));
            }
        catch (Exception e)
            {
            observer.onError(binEntry, e);
            return;
            }
        }

    @Override
    public void storeAll(Set<? extends BinaryEntry<K, V>> setBinEntries, StoreObserver<K, V> observer)
        {
        setBinEntries.forEach(entry -> store(entry, observer));
        }

    @Override
    public void erase(BinaryEntry<K, V> binEntry)
        {
        // produce a "tombstone"
        Producer producer;
        try
            {
            producer = ensureProducer();

            ProducerRecord record = new ProducerRecord(
                    m_sTopicName,
                    f_fPassThrough ? binEntry.getBinaryKey().toByteArray() : binEntry.getKey(),
                    null);

            record.headers().add("event", ("DELETE").getBytes(StandardCharsets.UTF_8));

            producer.send(record, ((metadata, exception) ->
                                   {
                                   if (exception != null)
                                       {
                                       Logger.err(() -> "Failed to publish record for key: " + binEntry.getKey() + " to topic: " + m_sTopicName);
                                       }
                                   else
                                       {
                                       Logger.finest(() -> "Published tombstone record for key: " + binEntry.getKey()
                                                           + " to topic: " + metadata.topic()
                                                           + " (partition=" + metadata.partition()
                                                           + ", offset=" + metadata.offset()
                                                           + ", timestamp=" + metadata.timestamp()
                                                           + ")");
                                       }
                                   }));
            }
        catch (Exception e)
            {
            Logger.err(() -> "Failed to erase record, " + e.getMessage());
            return;
            }
        }

    @Override
    public void eraseAll(Set<? extends BinaryEntry<K, V>> setBinEntries)
        {
        setBinEntries.forEach(entry -> erase(entry));
        }

    // ----- helpers ------------------------------------------------------

    /**
     * Allocates a producer, returns the currently allocated one if it was already done.
     *
     * @return the kafka producer
     */
    private KafkaProducer ensureProducer()
        {
        if (m_producer == null)
            {
            try
                {
                m_producer = new KafkaProducer<>(m_props);
                }
            catch (Exception e)
                {
                Logger.err("Error initializing producer", e);
                throw new RuntimeException(e);
                }
            }

        return m_producer;
        }

    // ----- data fields ----------------------------------------------------

    private final boolean f_fPassThrough;

    private Properties    m_props;
    private String        m_sTopicName;
    private KafkaProducer m_producer;
    }
