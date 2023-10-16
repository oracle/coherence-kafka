/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka.connect;

import com.oracle.bedrock.testsupport.deferred.Eventually;
import com.oracle.coherence.kafka.pof.Person;
import com.oracle.coherence.kafka.pof.Address;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.messaging.ConnectionException;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.Producer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static com.oracle.bedrock.testsupport.deferred.Eventually.within;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * Test coherence sink connector
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SinkTestIT
    extends AbstractKafkaIT
    {
    @BeforeAll
    private static void beforeAll()
        {
        // deploy connect plugins
        // basic non-passthrough tests
        startDeploy(8083, SINK_BASIC_CONNECTOR, "MyTopic,NoCacheMapping", "MyTopic->MyCache,NoCacheMapping->NoCache", false);
        // POF tests
        startDeploy(8083, SINK_POF_CONNECTOR, "POFTopic", "", false);
        }

    @Test
    @Order(1)
    public void testInsert() throws InterruptedException, ExecutionException
        {
        Producer<String, User> producer = new KafkaProducer<>(setupProps());

        User user1 = new User("Jerry", 35);

        producer.send(new ProducerRecord<>(TEST_TOPIC, user1.getFirstName(), user1)).get();

        System.out.println("Message " + user1.getFirstName() + ", " + user1.toString() + " sent");

        User user2 = new User("NoKey", 20);

        producer.send(new ProducerRecord<>(TEST_TOPIC, user2)).get();
        System.out.println("Message " + user2.getFirstName() + ", " + user2.toString() + " sent, should not result in cache entry");

        // get to cache
        getCache();

        final NamedCache<String, User> cache = CacheFactory.getCache(TEST_CACHE);

        System.out.println("basicTest Result: " + cache.values());
        Eventually.assertDeferred(cache::size, is(1), within(30, TimeUnit.SECONDS));
        Eventually.assertDeferred(() -> cache.get("Jerry").getAge(), is(35), within(30, TimeUnit.SECONDS));
        Eventually.assertDeferred(() -> cache.containsValue(user2), is(false), within(30, TimeUnit.SECONDS));
        }

    @Test
    @Order(2)
    public void testUpdate() throws Exception
        {
        Producer<String, User> producer = new KafkaProducer<>(setupProps());

        User user1 = new User("Jerry", 55);

        producer.send(new ProducerRecord<>(TEST_TOPIC, user1.getFirstName(), user1)).get();

        // get to cache
        getCache();

        final NamedCache<String, User> cache = CacheFactory.getCache(TEST_CACHE);

        Eventually.assertDeferred(() -> cache.get("Jerry").getAge(), is(55), within(30, TimeUnit.SECONDS));
        }

    @Test
    @Order(3)
    public void testDelete() throws Exception
        {
        NamedCache<String, User> cache = CacheFactory.getCache(TEST_CACHE);

        Eventually.assertDeferred(cache::size, is(1));
        Eventually.assertDeferred(() -> cache.get("Jerry").getAge(), is(55));

        Producer<String, User> producer = new KafkaProducer<>(setupProps());

        producer.send(new ProducerRecord<>(TEST_TOPIC, "Jerry", null)).get();

        Eventually.assertDeferred(cache::size, is(0), within(30, TimeUnit.SECONDS));
        }


    @Test
    @Order(4)
    public void testMultiple() throws Exception
        {
        final int count = 100;

        Producer<String, User> producer = new KafkaProducer<>(setupProps());

        for (int i = 0; i < count; i++)
            {
            User user = new User("User" + i, 20 + i);

            producer.send(new ProducerRecord<>(TEST_TOPIC, user.getFirstName(), user)).get();
            }

        // get to cache
        getCache();

        NamedCache<String, User> cache = CacheFactory.getCache(TEST_CACHE);

        System.out.println("Result: " + cache.values());
        Eventually.assertDeferred(cache::size, is(count));
        Eventually.assertDeferred(() -> cache.get("User15").getAge(), is(35));
        }

    @Test
    @Order(5)
    public void testNoCacheMapping() throws Exception
        {
        Producer<String, User> producer = new KafkaProducer<>(setupProps());

        User user = new User("NoCache", 20);

        producer.send(new ProducerRecord<>("NoCacheMapping", user.getFirstName(), user)).get();

        // sink task will shut down if an unhandled exception happens
        getCache();
        }

    //@Test
    //@Order(6)
    public void testPOF() throws Exception
        {
        Producer<String, Person> producer = new KafkaProducer<>(setupProps(true));

        Person p1 = new Person()
                .setName("Homer Simpson")
                .setAge(50)
                .setAddress(new Address().setCity("Springfield").setState("USA").setZip("12345"));

        producer.send(new ProducerRecord<>(POF_TOPIC_CACHE, p1.getName(), p1)).get();

        // get to cache
        getCache(POF_TOPIC_CACHE);

        NamedCache<String, Person> cache = CacheFactory.getCache(POF_TOPIC_CACHE);

        Eventually.assertDeferred(cache::size, is(1));
        System.out.println("Result: " + cache.values());
        Eventually.assertDeferred(() -> cache.get("Homer Simpson").getAge(), is(50));
        }

    private void getCache()
        {
        getCache(TEST_CACHE);
        }

    private void getCache(String sCacheName)
        {
        Eventually.assertDeferred(() ->
                                  {
                                  try
                                      {
                                      return CacheFactory.getCache(sCacheName);
                                      }
                                  catch (ConnectionException ce)
                                      {
                                      return null;
                                      }
                                  }, notNullValue(),
                                  within(30, TimeUnit.SECONDS));
        }

    private Properties setupProps()
        {
        return setupProps(false);
        }

    /**
     * Common properties for producer
     */
    private Properties setupProps(boolean fPof)
        {
        Properties props = new Properties();

        props.put("bootstrap.servers", "localhost:19092");
        props.put("client.id", "test-clientid");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        if (fPof)
            {
            props.put("value.serializer", "com.oracle.coherence.kafka.pof.KafkaPofSerializer");
            }
        else
            {
            props.put("value.serializer", "com.oracle.coherence.kafka.connect.UserSerializer");
            }

        return props;
        }

    private final static String TEST_TOPIC      = "MyTopic";
    private final static String TEST_CACHE      = "MyCache";
    private final static String POF_TOPIC_CACHE = "POFTopic";
    }
