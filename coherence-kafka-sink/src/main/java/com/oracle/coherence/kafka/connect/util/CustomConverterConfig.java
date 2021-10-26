/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka.connect.util;

import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.storage.ConverterConfig;

/**
 * Config for CustomConverter.
 */
public class CustomConverterConfig
    extends ConverterConfig
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Constructor
     */
    public CustomConverterConfig(Map<String, ?> props)
        {
        super(CONFIG_DEF, props);
        }

    // ----- accessors ------------------------------------------------------

    /**
     * Returns the config definition
     *
     * @return config definition
     */
    public static ConfigDef getConfigDef()
        {
        return CONFIG_DEF;
        }

    /**
     * Get the configured serializer
     *
     * @return the serializer class name
     */
    public String getSerializer()
        {
        return getString(SERIALIZER_CONFIG);
        }

    /**
     * Get the configured deserializer
     *
     * @return the deserializer class name
     */
    public String getDeserializer()
        {
        return getString(DESERIALIZER_CONFIG);
        }

    // ----- constants ------------------------------------------------------

    private static final String SERIALIZER_CONFIG    = "serializer";
    private static final String SERIALIZER_DEFAULT   = org.apache.kafka.common.serialization.ByteArraySerializer.class.getName();
    private static final String SERIALIZER_DOC       = "Serializer class name for this converter";
    private static final String SERIALIZER_DISPLAY   = "Serializer class name";

    private static final String DESERIALIZER_CONFIG  = "deserializer";
    private static final String DESERIALIZER_DEFAULT = org.apache.kafka.common.serialization.ByteArrayDeserializer.class.getName();
    private static final String DESERIALIZER_DOC     = "Deserializer class name for this converter";
    private static final String DESERIALIZER_DISPLAY = "Deserializer class name";

    private static final ConfigDef CONFIG_DEF = ConverterConfig.newConfigDef()
            .define(
                    SERIALIZER_CONFIG,
                    ConfigDef.Type.STRING,
                    SERIALIZER_DEFAULT,
                    ConfigDef.Importance.HIGH,
                    SERIALIZER_DOC,
                    null,
                    -1,
                    ConfigDef.Width.MEDIUM,
                    SERIALIZER_DISPLAY
            )
            .define(
                    DESERIALIZER_CONFIG,
                    ConfigDef.Type.STRING,
                    DESERIALIZER_DEFAULT,
                    ConfigDef.Importance.HIGH,
                    DESERIALIZER_DOC,
                    null,
                    -1,
                    ConfigDef.Width.MEDIUM,
                    DESERIALIZER_DISPLAY
            );
    }
