/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka.connect;

import com.oracle.bedrock.testsupport.deferred.Eventually;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Base;
import com.tangosol.util.filter.AlwaysFilter;
import com.tangosol.util.processor.ConditionalPut;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SourceTestIT
    extends AbstractKafkaIT
    {
    @BeforeAll
    private static void beforeAll()
        {
        // deploy pass-through connect plugin
        startDeploy(8083, SINK_PASTHROUGH_CONNECTOR, "KafkaPassthrough", "", true);

        KafkaConsumer<String, String> consumer;

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-groupid");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        consumer = new KafkaConsumer<>(props);

        consumer.subscribe(Arrays.asList("foo"));

        m_consumer = consumer;
        }

    @Test
    @Order(1)
    public void rwbmTestBasic()
        {
        String theKey = "key1";
        String theValue = "value1";

        NamedCache<String, String> foo = CacheFactory.getCache("foo");
        foo.put(theKey, theValue);

        assertThat(foo.get(theKey), is(theValue));

        // check record made it into kafka
        Map<String, String> ref = Stream.of(new String[][] {
                { theKey, theValue },
                }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
        checkKafka(ref);
        }

    @Test
    @Order(2)
    public void rwbmTestInvoke()
        {
        String theKey = "key2";
        String theValue = "value2";

        NamedCache<String, String> foo = CacheFactory.getCache("foo");
        foo.invoke(theKey, new ConditionalPut<>(AlwaysFilter.INSTANCE, theValue));

        assertThat(foo.get(theKey), is(theValue));

        // check record made it into kafka
        Map<String, String> ref = Stream.of(new String[][] {
                { theKey, theValue },
                }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
        checkKafka(ref);
        }

    @Test
    @Order(3)
    public void rwbmTestPutAll()
        {
        String theKey = "key";
        String theValue = "value";
        HashMap<String, String> referenceValues = new HashMap<>();

        NamedCache<String, String> foo = CacheFactory.getCache("foo");
        for (int i = 0; i < PUT_COUNT; i++)
            {
            String key, value;

            foo.put(key = (theKey + i), value = (theValue + i));
            referenceValues.put(key, value);
            }

        assertThat(foo.get(theKey + "10"), is(theValue + "10"));

        // check record made it into kafka
        for (int i = 0; i < PUT_COUNT; i++)
            {
            checkKafka(referenceValues);
            }
        }

    @Test
    @Order(4)
    public void rwbmTestDelete()
        {
        String theKey = "key10";

        NamedCache<String, String> foo = CacheFactory.getCache("foo");
        foo.remove(theKey);

        assertThat(foo.get(theKey), is(nullValue()));

        checkKafkaNull(theKey);
        }

    @Test
    @Order(5)
    public void passthroughTest()
        {
        User user = new User("Tommy", 30);

        NamedCache<Integer, User> testPassthrough = CacheFactory.getCache("TestPassthrough");
        testPassthrough.put(1, user);

        NamedCache<Integer, User> kafkaSide = CacheFactory.getCache("KafkaPassthrough");

        Eventually.assertDeferred(() -> kafkaSide.get(1), is(user));
        }

    @Test
    @Order(6)
    public void rwbmTestException()
        {
        String theKey = "keyException";
        String theValue = "valueException";

        NamedCache<String, String> cache = CacheFactory.getCache("TestException");

        cache.put(theKey, theValue);

        Eventually.assertDeferred(() -> cache.size(), is(1));
        }

    private void checkKafka(Map<String, String> m)
        {
        boolean timedout = false;
        long start = Base.getSafeTimeMillis();
        while(m.size() > 0 && !timedout)
            {
            ConsumerRecords<String, String> records = m_consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records)
                {
                System.out.println("Record: " + record);
                String value = m.get(record.key());
                if (value.equals(record.value()))
                    {
                    m.remove(record.key());
                    }
                }
            if ((Base.getSafeTimeMillis() - start) > 30000)
                {
                timedout = true;
                }
            }

        assertThat("Error for " + m, m.isEmpty(), is(true));
        }

    private void checkKafkaNull(String key)
        {
        boolean timedout = false;
        boolean found    = false;
        long start = Base.getSafeTimeMillis();
        while(!found && !timedout)
            {
            ConsumerRecords<String, String> records = m_consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records)
                {
                System.out.println("Record: " + record);
                if (key.equals(record.key()) && record.value() == null)
                    {
                    found = true;
                    break;
                    }
                }
            if ((Base.getSafeTimeMillis() - start) > 30000)
                {
                timedout = true;
                }
            }

        assertThat(found, is(true));
        }

    private static KafkaConsumer<String, String> m_consumer;
        private static final int PUT_COUNT = 100;
    }
