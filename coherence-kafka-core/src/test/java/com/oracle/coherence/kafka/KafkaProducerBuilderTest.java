/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka;

import com.tangosol.config.ConfigurationException;
import com.tangosol.config.expression.LiteralExpression;
import com.tangosol.config.expression.SystemPropertyParameterResolver;
import com.tangosol.net.cache.BinaryEntryStore;
import com.tangosol.net.cache.CacheStore;
import com.tangosol.net.cache.NonBlockingEntryStore;
import com.tangosol.net.cache.StoreObserver;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;

import com.tangosol.util.BinaryEntry;
import com.tangosol.util.WrapperException;
import java.util.Properties;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class KafkaProducerBuilderTest
    {
    @Test
    public void testRealizeValidNBES()
        {
        String sXml =
                "              <kafka:producer>"
                + "                <kafka:topic-name>atopic</kafka:topic-name>"
                + "                <kafka:bootstrap-servers>localhost:19092</kafka:bootstrap-servers>"
                + "                <kafka:some-dummy-property>blah</kafka:some-dummy-property>"
                + "              </kafka:producer>";
        XmlElement elt = XmlHelper.loadXml(sXml);

        KafkaProducerBuilder builder = new KafkaProducerBuilder(elt);
        builder.setTopicName(new LiteralExpression<>("mytopic"));
        builder.setClassName(new LiteralExpression<>("com.oracle.coherence.kafka.KafkaProducerBuilderTest$TestStore1"));
        Object result = builder.realize(new SystemPropertyParameterResolver(), null, null);

        assertThat(result, notNullValue());
        }

    @Test
    public void testRealizeInvalidNBES()
        {
        String sXml =
                "              <kafka:producer>"
                + "                <kafka:topic-name>atopic</kafka:topic-name>"
                + "                <kafka:bootstrap-servers>localhost:19092</kafka:bootstrap-servers>"
                + "                <kafka:some-dummy-property>blah</kafka:some-dummy-property>"
                + "              </kafka:producer>";
        XmlElement elt = XmlHelper.loadXml(sXml);

        assertThrows(WrapperException.class, () ->
                 {
                 KafkaProducerBuilder builder = new KafkaProducerBuilder(elt);
                 builder.setTopicName(new LiteralExpression<>("mytopic"));
                 builder.setClassName(new LiteralExpression<>("com.oracle.coherence.kafka.KafkaProducerBuilderTest$TestStoreInvalid"));
                 Object result = builder.realize(new SystemPropertyParameterResolver(), null, null);
                 });
        }


    @Test
    public void testRealizeValidBES()
        {
        String sXml =
                "              <kafka:producer>"
                + "                <kafka:topic-name>atopic</kafka:topic-name>"
                + "                <kafka:bootstrap-servers>{test-value somehost:19092}</kafka:bootstrap-servers>"
                + "                <kafka:some-dummy-property>dummy</kafka:some-dummy-property>"
                + "              </kafka:producer>";
        XmlElement elt = XmlHelper.loadXml(sXml);

        KafkaProducerBuilder builder = new KafkaProducerBuilder(elt);
        builder.setTopicName(new LiteralExpression<>("mytopic"));
        builder.setClassName(new LiteralExpression<>("com.oracle.coherence.kafka.KafkaProducerBuilderTest$TestStore2"));
        Object result = builder.realize(new SystemPropertyParameterResolver(), null, null);

        assertThat(result, notNullValue());
        }

    @Test
    public void testRealizeValidCS()
        {
        String sXml =
                "              <kafka:producer>"
                + "                <kafka:topic-name>atopic</kafka:topic-name>"
                + "                <kafka:bootstrap-servers>localhost:19092</kafka:bootstrap-servers>"
                + "                <kafka:some-dummy-property>blah</kafka:some-dummy-property>"
                + "              </kafka:producer>";
        XmlElement elt = XmlHelper.loadXml(sXml);

        KafkaProducerBuilder builder = new KafkaProducerBuilder(elt);
        builder.setTopicName(new LiteralExpression<>("mytopic"));
        builder.setClassName(new LiteralExpression<>("com.oracle.coherence.kafka.KafkaProducerBuilderTest$TestStore3"));
        Object result = builder.realize(new SystemPropertyParameterResolver(), null, null);

        assertThat(result, notNullValue());
        }

    @Test
    public void testRealizeNotValid()
        {
        String sXml =
                "              <kafka:producer>"
                + "                <kafka:topic-name>atopic</kafka:topic-name>"
                + "                <kafka:entry-store-class-name>com.oracle.coherence.kafka.KafkaNamespaceHandler</kafka:entry-store-class-name>"
                + "                <kafka:bootstrap-servers>localhost:19092</kafka:bootstrap-servers>"
                + "                <kafka:some-dummy-property>blah</kafka:some-dummy-property>"
                + "              </kafka:producer>";
        XmlElement elt = XmlHelper.loadXml(sXml);

        assertThrows(ConfigurationException.class, () ->
            {
            KafkaProducerBuilder builder = new KafkaProducerBuilder(elt);
            builder.setTopicName(new LiteralExpression<>("mytopic"));
            builder.setClassName(new LiteralExpression<>("com.oracle.coherence.kafka.KafkaNamespaceHandler"));
            Object result = builder.realize(new SystemPropertyParameterResolver(), null, null);
            }
        );
        }

    @Test
    public void testRealizeNotaClass()
        {
        String sXml =
                "              <kafka:producer>"
                + "                <kafka:topic-name>atopic</kafka:topic-name>"
                + "                <kafka:entry-store-class-name>com.oracle.coherence.kafka.KafkaNamespaceHandler</kafka:entry-store-class-name>"
                + "                <kafka:bootstrap-servers>localhost:19092</kafka:bootstrap-servers>"
                + "                <kafka:some-dummy-property>blah</kafka:some-dummy-property>"
                + "              </kafka:producer>";
        XmlElement elt = XmlHelper.loadXml(sXml);

        assertThrows(WrapperException.class, () ->
                     {
                     KafkaProducerBuilder builder = new KafkaProducerBuilder(elt);
                     builder.setTopicName(new LiteralExpression<>("mytopic"));
                     builder.setClassName(new LiteralExpression<>("dummy"));
                     Object result = builder.realize(new SystemPropertyParameterResolver(), null, null);
                     }
        );
        }

    static class TestStore1<K, V>
            implements NonBlockingEntryStore<K, V>
        {
        public TestStore1(Properties props)
            { }

        public void load(BinaryEntry<K, V> binEntry, StoreObserver<K, V> observer)
            { }

        public void loadAll(Set<? extends BinaryEntry<K, V>> setBinEntries, StoreObserver<K, V> observer)
            { }

        public void store(BinaryEntry<K, V> var1, StoreObserver<K, V> var2)
            { }

        public void storeAll(Set<? extends BinaryEntry<K, V>> var1, StoreObserver<K, V> var2)
            { }

        public void erase(BinaryEntry<K, V> var1)
            { }

        public void eraseAll(Set<? extends BinaryEntry<K, V>> var1)
            { }
        }

    static class TestStoreInvalid<K, V>
            implements NonBlockingEntryStore<K, V>
        {
        public void load(BinaryEntry<K, V> binEntry, StoreObserver<K, V> observer)
            { }

        public void loadAll(Set<? extends BinaryEntry<K, V>> setBinEntries, StoreObserver<K, V> observer)
            { }

        public void store(BinaryEntry<K, V> var1, StoreObserver<K, V> var2)
            { }

        public void storeAll(Set<? extends BinaryEntry<K, V>> var1, StoreObserver<K, V> var2)
            { }

        public void erase(BinaryEntry<K, V> var1)
            { }

        public void eraseAll(Set<? extends BinaryEntry<K, V>> var1)
            { }
        }

    static class TestStore2<K, V>
            implements BinaryEntryStore<K, V>
        {
        public TestStore2(Properties props)
            { }

        public void load(BinaryEntry<K, V> var1)
            { }

        public void loadAll(Set<? extends BinaryEntry<K, V>> var1)
            { }

        public void store(BinaryEntry<K, V> var1)
            { }

        public void storeAll(Set<? extends BinaryEntry<K, V>> var1)
            { }

        public void erase(BinaryEntry<K, V> var1)
            { }

        public void eraseAll(Set<? extends BinaryEntry<K, V>> var1)
            { }
        }

    static class TestStore3<K, V>
            implements CacheStore<K, V>
        {
        public TestStore3(Properties props)
            { }

        public V load(K var1)
            {
            return null;
            }

        public void store(K var1, V var2)
            { }

        public void erase(K var1)
            { }
        }
    }
