/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka.connect.converter;

import com.oracle.coherence.common.schema.ExtensibleProperty;
import com.oracle.coherence.common.schema.ExtensibleType;
import com.oracle.coherence.common.schema.lang.java.JavaProperty;
import com.oracle.coherence.common.schema.lang.java.JavaType;
import com.oracle.coherence.common.schema.lang.java.JavaTypeDescriptor;

import com.oracle.coherence.kafka.pof.KafkaPofContext;
import com.oracle.coherence.kafka.pof.KafkaPofDeserializer;
import com.oracle.coherence.kafka.pof.KafkaPofSerializer;

import com.tangosol.io.pof.schema.PofType;

import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.storage.Converter;

/**
 * @author Aleks Seovic  2021.01.20
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class KafkaPofConverter implements Converter
    {
    private final static ConcurrentMap<Class<?>, Schema> SCHEMAS;

    static
        {
        ConcurrentMap<Class<?>, Schema> schemas = new ConcurrentHashMap<>();
        schemas.put(Boolean.class, Schema.OPTIONAL_BOOLEAN_SCHEMA);
        schemas.put(Boolean.TYPE,  Schema.BOOLEAN_SCHEMA);
        schemas.put(Byte.class,    Schema.OPTIONAL_INT8_SCHEMA);
        schemas.put(Byte.TYPE,     Schema.INT8_SCHEMA);
        schemas.put(Short.class,   Schema.OPTIONAL_INT16_SCHEMA);
        schemas.put(Short.TYPE,    Schema.INT16_SCHEMA);
        schemas.put(Integer.class, Schema.OPTIONAL_INT32_SCHEMA);
        schemas.put(Integer.TYPE,  Schema.INT32_SCHEMA);
        schemas.put(Long.class,    Schema.OPTIONAL_INT64_SCHEMA);
        schemas.put(Long.TYPE,     Schema.INT64_SCHEMA);
        schemas.put(Float.class,   Schema.OPTIONAL_FLOAT32_SCHEMA);
        schemas.put(Float.TYPE,    Schema.FLOAT32_SCHEMA);
        schemas.put(Double.class,  Schema.OPTIONAL_FLOAT64_SCHEMA);
        schemas.put(Double.TYPE,   Schema.FLOAT64_SCHEMA);
        schemas.put(String.class,  Schema.OPTIONAL_STRING_SCHEMA);
        schemas.put(byte[].class,  Schema.OPTIONAL_BYTES_SCHEMA);
        SCHEMAS = schemas;
        }

    private final KafkaPofSerializer   serializer   = new KafkaPofSerializer();
    private final KafkaPofDeserializer deserializer = new KafkaPofDeserializer();

    public void configure(Map<String, ?> map, boolean isKey)
        {
        serializer.configure(map, isKey);
        deserializer.configure(map, isKey);
        }

    public byte[] fromConnectData(String topic, Schema schema, Object value)
        {
        return serializer.serialize(topic, value);
        }

    public SchemaAndValue toConnectData(String topic, byte[] bytes)
        {
        Object value = deserializer.deserialize(topic, bytes);
        return new SchemaAndValue(SCHEMAS.computeIfAbsent(value.getClass(), this::computeSchema), value);
        }

    KafkaPofSerializer getSerializer()
        {
        return serializer;
        }

    KafkaPofDeserializer getDeserializer()
        {
        return deserializer;
        }

    Schema computeSchema(Class<?> aClass)
        {
        KafkaPofContext ctx = serializer.getPofContext();
        ExtensibleType t = ctx.getSchema().findTypeByJavaName(aClass.getName());
        if (t == null)
            {
            return null;
            }

        JavaType jt = t.getExtension(JavaType.class);
        PofType  pt = t.getExtension(PofType.class);
        SchemaBuilder sb = SchemaBuilder.struct().name(jt.getFullName()).version(pt.getVersion());

        for (ExtensibleProperty p : t.getProperties())
            {
            JavaProperty       jp        = p.getExtension(JavaProperty.class);
            JavaTypeDescriptor jtd       = jp.resolveType(ctx.getSchema());
            Class<?>           propClass = getPropertyClass(ctx, jtd);
            sb.field(jp.getName(), SCHEMAS.computeIfAbsent(propClass, this::computeSchema));
            }

        return sb.build();
        }

    private Class<?> getPropertyClass(KafkaPofContext ctx, JavaTypeDescriptor jtd)
        {
        switch (jtd.getFullName())
            {
            case "boolean":             return Boolean.TYPE;
            case "byte":                return jtd.isArray() ? byte[].class : Byte.TYPE;
            case "short":               return Short.TYPE;
            case "int":                 return Integer.TYPE;
            case "long":                return Long.TYPE;
            case "float":               return Float.TYPE;
            case "double":              return Double.TYPE;
            case "java.lang.Boolean":   return Boolean.class;
            case "java.lang.Byte":      return Byte.class;
            case "java.lang.Short":     return Short.class;
            case "java.lang.Integer":   return Integer.class;
            case "java.lang.Long":      return Long.class;
            case "java.lang.Float":     return Float.class;
            case "java.lang.Double":    return Double.class;
            case "java.lang.String":    return String.class;
            default:
                int typeId = ctx.getUserTypeIdentifier(jtd.getFullName());
                return ctx.getClass(typeId);
            }
        }
    }
