/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka.pof;

import com.tangosol.util.ExternalizableHelper;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

/**
 * @author Aleks Seovic  2021.01.20
 */
public class KafkaPofSerializer<T>
        extends KafkaPofBase
        implements Serializer<T>
    {
    public byte[] serialize(String topic, T value)
        {
        try
            {
            return ExternalizableHelper
                    .toBinary(value, getPofContext())
                    .getBufferInput()
                    .getBuffer()
                    .toByteArray();
            }
        catch (Throwable e)
            {
            throw new SerializationException(e);
            }
        }
    }
