/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka.connect.sink;

import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import static java.util.stream.Collectors.toMap;

/**
 * Configuration class for sink task.
 *
 * These are passed to the Connect REST management server for runtime deployment
 * on the Confluent platform or via a properties file for Kafka OSS.
 *
 * Example of REST payload using curl:
 *
 * <pre>
 * {@code
 * curl -X POST -H "Content-Type: application/json" \
 *     http://localhost:8083/connectors \
 *     -d '{"name":"sink",
 *              "config":
 *                  {
 *                      "connector.class":"com.oracle.coherence.kafka.connect.CoherenceSinkConnector",
 *                      "topics":"MyTopic",
 *                      "coherence.cache.mappings":"MyTopic->MyCache"
 *                  }
 *          }'
 * }
 * </pre>
 *
 * @author Maurice Gamanho
 */
public class CoherenceSinkConfig extends AbstractConfig
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Main constructor.
     *
     * @param props
     */
    public CoherenceSinkConfig(Map<String, String> props)
        {
        super(CONFIG_DEF, props);
        f_fStartServer = getBoolean(START_SERVER);
        f_fPassThrough = getBoolean(PASS_THROUGH);
        f_topicsCaches = getList(CACHE_MAPPINGS)
                .stream()
                .map(s -> s.split("->"))
                .collect(toMap(s -> s[0], s -> s[1]));
        }

    // ----- accessors ------------------------------------------------------

    public boolean isStartServer()
        {
        return f_fStartServer;
        }

    public boolean isPassThrough()
        {
        return f_fPassThrough;
        }

    public Map<String, String> getTopicsCaches()
        {
        return f_topicsCaches;
        }

    // ----- constants ------------------------------------------------------

    private static final String START_SERVER         = "coherence.server";
    private static final String START_SERVER_DEFAULT = "true";
    private static final String START_SERVER_DOC =
            "Whether this connect JVM instance will start a coherence DefaultCacheServer";
    private static final String START_SERVER_DISPLAY = "Start DefaultCacheServer";

    private static final String PASS_THROUGH         = "coherence.passthrough";
    private static final String PASS_THROUGH_DEFAULT = "false";
    private static final String PASS_THROUGH_DOC =
            "Enable pass-through data handling to bypass serialization";
    private static final String PASS_THROUGH_DISPLAY = "Pass-through (bypass serialization)";

    private static final String GLOBAL_GROUP = "Global";

    private static final String CACHE_MAPPINGS         = "coherence.cache.mappings";
    private static final String CACHE_MAPPINGS_DEFAULT = "";
    private static final String CACHE_MAPPINGS_DOC =
            "Comma-separated list of topic->cache pairs";
    private static final String CACHE_MAPPINGS_DISPLAY = "topic->cache mappings";

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(
                    START_SERVER,
                    ConfigDef.Type.BOOLEAN,
                    START_SERVER_DEFAULT,
                    ConfigDef.Importance.HIGH,
                    START_SERVER_DOC,
                    GLOBAL_GROUP,
                    1,
                    ConfigDef.Width.SHORT,
                    START_SERVER_DISPLAY
            )
            .define(
                    PASS_THROUGH,
                    ConfigDef.Type.BOOLEAN,
                    PASS_THROUGH_DEFAULT,
                    ConfigDef.Importance.HIGH,
                    PASS_THROUGH_DOC,
                    GLOBAL_GROUP,
                    2,
                    ConfigDef.Width.SHORT,
                    PASS_THROUGH_DISPLAY
            )
            .define(
                    CACHE_MAPPINGS,
                    ConfigDef.Type.LIST,
                    CACHE_MAPPINGS_DEFAULT,
                    ConfigDef.Importance.MEDIUM,
                    CACHE_MAPPINGS_DOC,
                    GLOBAL_GROUP,
                    3,
                    ConfigDef.Width.MEDIUM,
                    CACHE_MAPPINGS_DISPLAY
            );

    // ----- data fields ----------------------------------------------------

    private final boolean             f_fStartServer;
    private final boolean             f_fPassThrough;
    private final Map<String, String> f_topicsCaches;
    }
