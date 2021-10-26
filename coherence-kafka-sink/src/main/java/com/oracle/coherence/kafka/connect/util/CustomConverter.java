/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka.connect.util;

import com.tangosol.util.Base;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.Deserializer;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.errors.DataException;
import org.apache.kafka.connect.errors.NotFoundException;
import org.apache.kafka.connect.storage.Converter;
import org.apache.kafka.connect.storage.ConverterType;
import org.apache.kafka.connect.storage.HeaderConverter;

/**
 * Custom Converter class allowing the use of configured serializer/deserializer.
 */
public class CustomConverter implements Converter, HeaderConverter
    {
    // ----- Converter interface --------------------------------------------

    @Override
    public ConfigDef config()
        {
        return CustomConverterConfig.getConfigDef();
        }

    @Override
    public void configure(Map<String, ?> configs)
        {
        CustomConverterConfig conf = new CustomConverterConfig(configs);
        ClassLoader loader = Base.getContextClassLoader(this);

        boolean isKey = conf.type() == ConverterType.KEY;

        try
            {
            Map<String, Object> serderConf = new HashMap<>(configs);

            Class<? extends Serializer> clzSerializer = (Class<? extends Serializer>)
                    Class.forName(conf.getSerializer(), true, loader);
            m_serializer = clzSerializer.getDeclaredConstructor().newInstance();
            m_serializer.configure(serderConf, isKey);

            Class<? extends Deserializer> clzDeserializer = (Class<? extends Deserializer>)
                    Class.forName(conf.getDeserializer(), true, loader);
            m_deserializer = clzDeserializer.getDeclaredConstructor().newInstance();
            m_deserializer.configure(serderConf, isKey);
            }
        catch (Exception e)
            {
            throw new NotFoundException(e);
            }
        }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey)
        {
        Map<String, Object> conf = new HashMap<>(configs);

        conf.put(CustomConverterConfig.TYPE_CONFIG, isKey ? ConverterType.KEY.getName() : ConverterType.VALUE.getName());
        configure(conf);
        }

    @Override
    public byte[] fromConnectData(String topic, Schema schema, Object value)
        {
        try
            {
            return m_serializer.serialize(topic, value);
            }
        catch (SerializationException e)
            {
            throw new DataException("Failed to serialize object: ", e);
            }
        }

    @Override
    public SchemaAndValue toConnectData(String topic, byte[] value)
        {
        try
            {
            return new SchemaAndValue(SchemaBuilder.type(Schema.Type.STRUCT).optional().build(), m_deserializer.deserialize(topic, value));
            }
        catch (SerializationException e)
            {
            throw new DataException("Failed to deserialize object: ", e);
            }
        }

    @Override
    public byte[] fromConnectHeader(String topic, String headerKey, Schema schema, Object value)
        {
        return fromConnectData(topic, schema, value);
        }

    @Override
    public SchemaAndValue toConnectHeader(String topic, String headerKey, byte[] value)
        {
        return toConnectData(topic, value);
        }

    @Override
    public void close()
        {
        // do nothing
        }

    // ----- data fields ----------------------------------------------------

    private Serializer   m_serializer;
    private Deserializer m_deserializer;
    }
