/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka.pof;

import com.tangosol.io.pof.ConfigurablePofContext;

import com.tangosol.io.pof.PofContext;
import java.util.Map;

/**
 * @author Aleks Seovic  2021.01.21
 */
public abstract class KafkaPofBase
    {
    private KafkaPofContext ctx;

    /**
     * Configure this class.
     *
     * @param configs configs in key/value pairs
     * @param isKey   whether is for key or value
     */
    public void configure(Map<String, ?> configs, boolean isKey)
        {
        Object pofConfig = configs.get("kafka.pof.config");
        if (pofConfig == null)
            {
            pofConfig = configs.get(ConfigurablePofContext.PROPERTY_CONFIG);
            }

        if (pofConfig == null)
            {
            ctx = new KafkaPofContext();
            }
        else if (pofConfig instanceof String)
            {
            ctx = new KafkaPofContext((String) pofConfig);
            }
        else
            {
            throw new IllegalArgumentException("The name of the POF configuration file must be string value");
            }
        }

    /**
     * Return the {@link PofContext} to use for serialization.
     *
     * @return the {@link PofContext} to use
     */
    public KafkaPofContext getPofContext()
        {
        return ctx;
        }
    }
