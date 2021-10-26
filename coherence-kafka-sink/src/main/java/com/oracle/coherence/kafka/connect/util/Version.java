/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka.connect.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * Kafka Connect version class.
 */
public class Version
    {
    /**
     * Returns the version of this connector.
     *
     * @return the version
     */
    public static String getVersion()
        {
        return version;
        }

    // ----- constants ------------------------------------------------------

    private static final Logger LOG = LoggerFactory.getLogger(Version.class);
    private static final String PATH = "/oracle-kafka-connect-coherence-version.properties";

    // ----- data fields ----------------------------------------------------

    private static String version = "unknown";

    // ----- static initializations -----------------------------------------

    static
        {
        try (InputStream stream = Version.class.getResourceAsStream(PATH))
            {
            Properties props = new Properties();
            props.load(stream);
            version = props.getProperty("version", version).trim();
            }
        catch (Exception e)
            {
            LOG.warn("Error while loading version:", e);
            }
        }

    }
