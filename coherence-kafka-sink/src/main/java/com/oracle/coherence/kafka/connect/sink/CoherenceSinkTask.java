/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka.connect.sink;

import com.oracle.coherence.kafka.connect.util.Version;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultCacheServer;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Binary;
import com.tangosol.util.NullImplementation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sink task class.
 *
 * The actual work of populating a cache with records consumed from a Kafka topic
 * is performed here.
 * By default, the name of the cache matches the Kafka topic. This can be overriden
 * for arbitrary mappings via configuration, see {@link CoherenceSinkConfig}
 *
 * @author Maurice Gamanho
 */
public class CoherenceSinkTask extends SinkTask
    {
    // ----- SinkTask implementation  ---------------------------------------

    @Override
    public void start(final Map<String, String> props)
        {
        LOG.info("Starting Coherence Sink task");
        LOG.info("Configuration: " + props);
        m_config = new CoherenceSinkConfig(props);
        m_fPassThrough = m_config.isPassThrough();
        m_topicsCaches = m_config.getTopicsCaches();

        if (m_config.isStartServer())
            {
            LOG.info("Starting Coherence DefaultCacheServer");
            DefaultCacheServer.start();
            }
        }

    /**
     * Put records in the Coherence cache.
     *
     * @param records  the set of records to put in the cache
     */
    @Override
    public void put(Collection<SinkRecord> records)
        {
        Map        mapPut    = new HashMap();
        Set        setRemove = new HashSet();
        NamedCache cache     = null;

        for (SinkRecord record : records)
            {
            // skip null keys, not supported in Coherence
            if (record.key() == null)
                {
                LOG.error("The key is null for record: " + record);
                continue;
                }

            String sCacheName = m_topicsCaches.get(record.topic());
            if (sCacheName == null)
                {
                sCacheName = record.topic();
                }

            try
                {
                cache = m_fPassThrough
                        ? CacheFactory.getCache(sCacheName, NullImplementation.getClassLoader())
                        : CacheFactory.getCache(sCacheName);
                }
            catch (Exception e)
                {
                LOG.error("There was an error accessing cache " + sCacheName + ": " + e.getMessage());

                // skip this record
                continue;
                }

            if (record.value() == null)
                {
                setRemove.add(record.key());
                }
            else
                {
                if (m_fPassThrough)
                    {
                    Binary key   = new Binary((byte[]) record.key());
                    Binary value = new Binary((byte[]) record.value());

                    cache.put(key, value);
                    }
                else
                    {
                    // rely on converter's deserializer
                    mapPut.put(record.key(), record.value());
                    }
                }
            }

        if (cache != null)
            {
            cache.keySet().removeAll(setRemove);
            cache.putAll(mapPut);
            }
        }

    @Override
    public void stop()
        {
        LOG.info("Stopping Coherence Sink task");
        }

    @Override
    public String version()
        {
        return Version.getVersion();
        }

    // ----- constants ------------------------------------------------------

    private static final Logger LOG = LoggerFactory.getLogger(CoherenceSinkTask.class);

    // ----- data fields ----------------------------------------------------

    private Map<String, String> m_topicsCaches;
    private CoherenceSinkConfig m_config;
    private boolean             m_fPassThrough;
    }
