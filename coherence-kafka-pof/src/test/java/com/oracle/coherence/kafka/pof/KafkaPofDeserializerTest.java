/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka.pof;

import java.io.IOException;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link KafkaPofDeserializer}.
 *
 * @author Aleks Seovic  2021.01.21
 */
public class KafkaPofDeserializerTest
    {
    @Test
    void shouldCreateDefaultPofContext()
        {
        KafkaPofDeserializer<Person> deserializer = new KafkaPofDeserializer<>();
        deserializer.configure(Map.of(), false);

        assertThat(deserializer.getPofContext().getUserTypes(), containsInAnyOrder(Person.class, Address.class));
        }

    @Test
    void shouldPreferKafkaPofConfig()
        {
        KafkaPofDeserializer<Person> deserializer = new KafkaPofDeserializer<>();
        deserializer.configure(Map.of("kafka.pof.config", "kafka-pof-config.xml",
                                      "coherence.pof.config", "empty-pof-config.xml"), false);

        assertThat(deserializer.getPofContext().getUserTypes(), containsInAnyOrder(Address.class));
        }

    @Test
    void shouldUseCoherencePofConfig()
        {
        KafkaPofDeserializer<Person> deserializer = new KafkaPofDeserializer<>();
        deserializer.configure(Map.of("coherence.pof.config", "empty-pof-config.xml"), false);

        assertThat(deserializer.getPofContext().getUserTypes(), not(contains(Person.class, Address.class)));
        }

    @Test
    void shouldFailIfPofConfigIsNotString()
        {
        KafkaPofDeserializer<Person> deserializer = new KafkaPofDeserializer<>();
        assertThrows(IllegalArgumentException.class,
                     () -> deserializer.configure(Map.of("kafka.pof.config", 123), false));
        }

    @Test
    void testDeserialization() throws IOException
        {
        KafkaPofDeserializer<Person> deserializer = new KafkaPofDeserializer<>();
        deserializer.configure(Map.of(), false);

        KafkaPofSerializer<Person> serializer = new KafkaPofSerializer<>();
        serializer.configure(Map.of(), false);

        Person p1 = new Person()
                .setName("Homer Simpson")
                .setAge(50)
                .setAddress(new Address().setCity("Springfield").setState("USA").setZip("12345"));


        byte[] abData = serializer.serialize("people", p1);

        Person p2 = deserializer.deserialize("people", abData);
        assertThat(p2, Matchers.is(p1));
        }
    }
