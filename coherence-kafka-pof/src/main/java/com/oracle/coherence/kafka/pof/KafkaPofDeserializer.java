/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka.pof;

import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * @author Aleks Seovic  2021.01.20
 */
public class KafkaPofDeserializer<T>
        extends KafkaPofBase
        implements Deserializer<T>
    {
    @SuppressWarnings("unchecked")
    public T deserialize(String s, byte[] bytes)
        {
        try
            {
            return (T) ExternalizableHelper.fromBinary(new Binary(bytes), getPofContext());
            }
        catch (Throwable e)
            {
            throw new SerializationException(e);
            }
        }
    }
