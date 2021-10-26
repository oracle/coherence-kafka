/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka.pof;

import com.oracle.coherence.common.schema.Schema;
import com.oracle.coherence.common.schema.SchemaBuilder;

import com.tangosol.io.pof.ConfigurablePofContext;
import java.util.Collection;
import java.util.Set;

/**
 * @author Aleks Seovic  2021.01.21
 */
public class KafkaPofContext
        extends ConfigurablePofContext
    {
    private Schema schema;

    public KafkaPofContext()
        {
        }

    public KafkaPofContext(String sLocator)
        {
        super(sLocator);
        }

    protected synchronized void initialize()
        {
        super.initialize();

        schema = new SchemaBuilder()
                .addSchemaSource(new KafkaPofContextSchemaSource(this))
                .build();
        }

    public Schema getSchema()
        {
        ensureInitialized();
        return schema;
        }

    @SuppressWarnings("unchecked")
    Set<Class<?>> getUserTypes()
        {
        ensureInitialized();
        return getPofConfig().m_mapTypeIdByClass.keySet();
        }
    }
