/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka.pof;

import com.oracle.coherence.common.schema.ClassFileSchemaSource;
import com.oracle.coherence.common.schema.Schema;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Aleks Seovic  2021.01.21
 */
public class KafkaPofContextSchemaSource
        extends ClassFileSchemaSource
    {
    private final KafkaPofContext m_ctx;
    private int m_nPass;

    public KafkaPofContextSchemaSource(KafkaPofContext ctx)
        {
        m_ctx = ctx;
        withMissingPropertiesAsObject();
        }

    public void populateSchema(Schema schema)
        {
        try
            {
            // do two passes in order to ensure that all types are defined
            // before attempting to resolve base types, property types and interfaces
            for (int i = 1; i <= 2; i++)
                {
                m_nPass = i;

                for (Class<?> aClass : m_ctx.getUserTypes())
                    {
                    String name = '/' + aClass.getName().replace('.', '/') + ".class";
                    try (InputStream in = aClass.getResourceAsStream(name))
                        {
                        populateSchema(schema, in);
                        }
                    catch (Exception e)
                        {
                        throw e;
                        }
                    }
                }
            }
        catch (IOException e)
            {
            throw new RuntimeException(e);
            }
        }

    protected boolean isPass(int nPass)
        {
        return m_nPass == nPass;
        }
    }
