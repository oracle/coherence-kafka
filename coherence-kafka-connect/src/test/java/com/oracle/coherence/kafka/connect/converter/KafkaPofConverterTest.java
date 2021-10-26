/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka.connect.converter;

import com.oracle.coherence.kafka.pof.Address;
import com.oracle.coherence.kafka.pof.Person;

import java.util.Map;

import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;

import org.apache.kafka.connect.data.SchemaAndValue;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Aleks Seovic  2021.01.21
 */
public class KafkaPofConverterTest
    {
    @Test
    void testConversionRoundTrip()
        {
        Person person = new Person()
                .setName("Homer Simpson")
                .setAge(50)
                .setAddress(new Address().setCity("Springfield").setState("USA").setZip("12345"));

        KafkaPofConverter c = new KafkaPofConverter();
        c.configure(Map.of(), false);

        byte[] abData = c.fromConnectData("people", null, person);
        SchemaAndValue schemaAndValue = c.toConnectData("people", abData);

        assertThat(schemaAndValue.value(), is(person));
        }
    
    @Test
    void testSchemaGeneration()
        {
        KafkaPofConverter c = new KafkaPofConverter();
        c.configure(Map.of(), false);
        
        Schema person = c.computeSchema(Person.class);
        Schema address = c.computeSchema(Address.class);

        printSchema(person);
        printSchema(address);

        assertThat(person.name(), is(Person.class.getName()));
        assertThat(person.version(), is(0));
        assertThat(person.field("Name").schema().type(), is(Schema.Type.STRING));
        assertThat(person.field("Age").schema().type(), is(Schema.Type.INT32));
        assertThat(person.field("Address").schema().type(), is(Schema.Type.STRUCT));
        assertThat(person.field("Address").schema().name(), is(Address.class.getName()));

        assertThat(address.name(), is(Address.class.getName()));
        assertThat(address.version(), is(0));
        assertThat(address.field("Street").schema().type(), is(Schema.Type.STRING));
        assertThat(address.field("City").schema().type(), is(Schema.Type.STRING));
        assertThat(address.field("State").schema().type(), is(Schema.Type.STRING));
        assertThat(address.field("Zip").schema().type(), is(Schema.Type.STRING));
        }

    private void printSchema(Schema schema)
        {
        System.out.println(schemaType(schema) + ", version=" + schema.version());
        for (Field f : schema.fields())
            {
            System.out.println("  " + schemaType(f.schema()) + " " + f.name());
            }
        System.out.println();
        }

    private String schemaType(Schema schema)
        {
        Schema.Type type = schema.type();
        return type + (type == Schema.Type.STRUCT ? ":" + schema.name() : "");
        }
    }
