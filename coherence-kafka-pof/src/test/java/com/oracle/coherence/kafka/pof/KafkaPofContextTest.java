/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka.pof;

import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;

import org.hamcrest.Matchers;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link KafkaPofContext}.
 *
 * @author Aleks Seovic  2021.01.21
 */
public class KafkaPofContextTest
    {
    @Test
    void shouldPopulateSchema()
        {
        KafkaPofContext ctx = new KafkaPofContext();
        assertThat(ctx.getSchema(), is(notNullValue()));
        assertThat(ctx.getSchema().findTypeByJavaName(Person.class.getName()), is(notNullValue()));
        assertThat(ctx.getSchema().findTypeByJavaName(Address.class.getName()), is(notNullValue()));
        }

    @Test
    void testRoundTrip()
        {
        KafkaPofContext ctx = new KafkaPofContext();

        Person p1 = new Person()
                .setName("Homer Simpson")
                .setAge(50)
                .setAddress(new Address().setCity("Springfield").setState("USA").setZip("12345"));

        Binary binData = ExternalizableHelper.toBinary(p1, ctx);
        Person p2 = ExternalizableHelper.fromBinary(binData, ctx);

        assertThat(p2, Matchers.is(p1));
        }
    }
