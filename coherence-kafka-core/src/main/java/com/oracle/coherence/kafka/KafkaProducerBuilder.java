/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka;

import com.tangosol.coherence.config.ParameterList;
import com.tangosol.coherence.config.ParameterMacroExpression;
import com.tangosol.coherence.config.builder.ParameterizedBuilder;

import com.tangosol.config.ConfigurationException;
import com.tangosol.config.annotation.Injectable;
import com.tangosol.config.expression.Expression;
import com.tangosol.config.expression.ParameterResolver;
import com.tangosol.net.cache.BinaryEntryStore;
import com.tangosol.net.cache.CacheStore;
import com.tangosol.net.cache.NonBlockingEntryStore;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.util.Base;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;


public class KafkaProducerBuilder
        implements ParameterizedBuilder<Object>
    {
    /**
     * Construct {@code KafkaProducerBuilder} instance.
     *
     * @param config  the content of the kafka:producer element
     */
    public KafkaProducerBuilder(XmlElement config)
        {
        m_props = new Properties();

        ((List<XmlElement>) config.getElementList()).forEach(element ->
                         m_props
                             .put(element
                                    .getQualifiedName()
                                    .getLocalName()
                                    .replace("-", "."),
                                  element.getValue()));
        }

    /**
     * Sets the name of the class to load, optionally
     *
     * @param exprClassName  class realizing a {@code NonBlockingEntryStore}
     */
    @Injectable("entry-store-class-name")
    public void setClassName(Expression<String> exprClassName)
        {
        m_exprClassName = exprClassName;
        }

    public Expression<String> getClassName()
        {
        return m_exprClassName;
        }

    /**
     * Sets the name of the topic to send messages to
     *
     * @param exprTopicName  name of the topic
     */
    @Injectable("topic-name")
    public void setTopicName(Expression<String> exprTopicName)
        {
        m_exprTopicName = exprTopicName;
        }

    public Expression<String> getTopicName()
        {
        return m_exprTopicName;
        }

    // ---- ParameterizedBuilder interface ----------------------------------

    @Override
    public Object realize(ParameterResolver resolver, ClassLoader loader, ParameterList listParameters)
        {
        Object store = null;
        try
            {
            // evaluate expressions in properties
            m_props.forEach((key, value) ->
                            {
                            Expression<String> expr = new ParameterMacroExpression<>((String) value, java.lang.String.class);
                            m_props.replace(key, expr.evaluate(resolver));
                            });

            // ensure we have a classloader
            loader = Base.ensureClassLoader(loader);

            Class<?> clazz = null;
            if (getClassName() != null)
                {
                String sClassName = getClassName().evaluate(resolver);

                // attempt to load the class
                clazz = loader.loadClass(sClassName);
                }
            else
                {
                // default, only if class not configured
                clazz = loader.loadClass(KafkaEntryStore.class.getName());
                }

            m_props.put(TOPIC_NAME, getTopicName().evaluate(resolver));

            if (Arrays.stream(clazz.getInterfaces())
                    .anyMatch(
                            Predicate.isEqual(NonBlockingEntryStore.class)
                                      .or(Predicate.isEqual(BinaryEntryStore.class))
                                      .or(Predicate.isEqual(CacheStore.class))
                    ))
                {
                store = clazz.getDeclaredConstructor(Properties.class).newInstance(m_props);
                }
            else
                {
                throw new ConfigurationException("Cannot load entry-store-class-name class: " + clazz.getName(),
                         "Please make sure the class implements " + NonBlockingEntryStore.class.getName() +
                         ", " + BinaryEntryStore.class.getName() + " or " + CacheStore.class.getName() + ".");
                }
            }
        catch (Exception e)
            {
            throw Base.ensureRuntimeException(e);
            }

        return store;
        }

    // ----- constants ------------------------------------------------------

    private static final String TOPIC_NAME = "topic.name";

    // ----- data members ---------------------------------------------------

    private Expression<String> m_exprClassName;
    private Expression<String> m_exprTopicName;
    private Properties         m_props;
    }
