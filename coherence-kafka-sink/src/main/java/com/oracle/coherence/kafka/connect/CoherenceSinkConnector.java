/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka.connect;

import com.oracle.coherence.kafka.connect.sink.CoherenceSinkTask;
import com.oracle.coherence.kafka.connect.sink.CoherenceSinkConfig;
import com.oracle.coherence.kafka.connect.util.Version;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Kafka Connect Sink Connector class main entry point.
 *
 * Sets up the worker tasks where the actual work is performed.
 *
 * @author Maurice Gamanho
 */
public class CoherenceSinkConnector extends SinkConnector
  {
  // ----- SinkConnector implementation ---------------------------------------

  public Class<? extends Task> taskClass()
    {
    return CoherenceSinkTask.class;
    }

  @Override
  public List<Map<String, String>> taskConfigs(int maxTasks)
    {
    f_logger.info("Setting task configurations for {} workers.", maxTasks);
    final List<Map<String, String>> configs = new ArrayList<>(maxTasks);
    for (int i = 0; i < maxTasks; ++i)
      {
      configs.add(m_mapConfig);
      }
    return configs;
    }

  @Override
  public void start(Map<String, String> props)
    {
    f_logger.info("Start Coherence Sink Connector");

    m_mapConfig = props;
    }

  @Override
  public void stop()
    {
    f_logger.info("Stop Coherence Sink Connector");
    }

  @Override
  public ConfigDef config()
    {
    return CoherenceSinkConfig.CONFIG_DEF;
    }

  @Override
  public String version()
    {
    return Version.getVersion();
    }

  // ----- data fields ----------------------------------------------------

  private static final Logger f_logger = LoggerFactory.getLogger(CoherenceSinkConnector.class);

  private Map<String, String> m_mapConfig;
  }
