/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka;

import com.tangosol.config.xml.AbstractNamespaceHandler;

/**
 * Custom namespace handler for the Kafka cachestore configuration.
 *
 * For example:
 * <pre>{@code
 *           &lt;cachestore-scheme&gt;
 *             &lt;class-scheme&gt;
 *               &lt;kafka:producer&gt;
 *                 &lt;kafka:topic-name&gt;foo&lt;/kafka:topic-name&gt;
 *                 &lt;kafka:bootstrap-servers&gt;localhost:9092&lt;/kafka:bootstrap-servers&gt;
 *               &lt;/kafka:producer>
 *             &lt;/class-scheme>
 *           &lt;/cachestore-scheme>
 * }</pre>
 */
public class KafkaNamespaceHandler
        extends AbstractNamespaceHandler
    {
    /**
     * Construct a {@code KafkaNamespaceHandler} instance
     */
    public KafkaNamespaceHandler()
        {
        registerProcessor(KafkaProcessor.class);
        }
    }
