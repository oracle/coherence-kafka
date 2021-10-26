/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka.connect;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.apache.kafka.common.serialization.Deserializer;

public class UserDeserializer implements Deserializer<User>
    {
    @Override
    public void close()
        {
        }

    @Override
    public void configure(Map<String, ?> arg0, boolean arg1)
        {
        }

    @Override
    public User deserialize(String arg0, byte[] arg1)
        {
        User user = null;

        try
            {
            Jsonb jsonb = JsonbBuilder.create();
            user = jsonb.fromJson(new ByteArrayInputStream(arg1), User.class);
            }
        catch (Exception e)
            {
            System.out.println("Error with JSON: " + new String(arg1, StandardCharsets.UTF_8));
            e.printStackTrace();

            throw e;
            }

        return user;
        }
    }