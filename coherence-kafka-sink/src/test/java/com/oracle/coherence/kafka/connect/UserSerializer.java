/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka.connect;

import java.io.ByteArrayOutputStream;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class UserSerializer implements Serializer<User>
    {
    @Override
    public void configure(Map<String, ?> map, boolean b)
        {
        }

    @Override
    public byte[] serialize(String arg0, User arg1)
        {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Jsonb jsonb = JsonbBuilder.create();
        jsonb.toJson(arg1, stream);
        return stream.toByteArray();
        }

    @Override
    public void close()
        {
        }
    }
