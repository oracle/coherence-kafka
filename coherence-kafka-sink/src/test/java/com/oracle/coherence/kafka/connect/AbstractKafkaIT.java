/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka.connect;

import java.net.URI;
import java.net.http.HttpClient;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Common actions for sink and source
 */
public abstract class AbstractKafkaIT
    {
    /**
     * Deploy a connector instance
     */
    @BeforeAll
    public static void before()
        {
        // pass-through
//        startDeploy(8083, SINK_PASTHROUGH_CONNECTOR, "KafkaPassthrough", "", true);
        }

    protected static void startDeploy(int nPort, String sConnectorName, String sTopics, String sCacheMappings, boolean fPassThrough)
        {
        // deploy and start connect plugin
        HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

        try
            {
            HttpRequest checkExists = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + nPort + "/connectors/" + sConnectorName))
                    .timeout(Duration.ofMinutes(2))
                    .GET()
                    .build();
            HttpResponse<String> resp = httpClient.send(checkExists, HttpResponse.BodyHandlers.ofString());

            System.out.println("REST response(1): " + resp);

            if (resp.statusCode() != 200)
                {
                String sSerDer = sConnectorName.equals(SINK_POF_CONNECTOR) ?
                                 ("\"value.converter.serializer\":\"com.oracle.coherence.kafka.pof.KafkaPofSerializer\", " +
                                  "\"value.converter.deserializer\":\"com.oracle.coherence.kafka.pof.KafkaPofDeserializer\", ") :
                                 ("\"value.converter.serializer\":\"com.oracle.coherence.kafka.connect.UserSerializer\", " +
                                  "\"value.converter.deserializer\":\"com.oracle.coherence.kafka.connect.UserDeserializer\", ");


                String sinkDeploy = "{\"name\":\"" + sConnectorName + "\"," +
                                    "\"config\": " +
                                    "{\"connector.class\":\"com.oracle.coherence.kafka.connect.CoherenceSinkConnector\"," +
                                    "\"coherence.session\":\"" + sConnectorName + "\"," +
                                    "\"coherence.cache.mappings\":\"" + sCacheMappings + "\"," +
                                    "\"coherence.passthrough\":\"" + (fPassThrough == true ? "true" : "false") + "\"," +
                                    (fPassThrough ?
                                         ("\"key.converter\":\"org.apache.kafka.connect.converters.ByteArrayConverter\", " +
                                          "\"value.converter\":\"org.apache.kafka.connect.converters.ByteArrayConverter\", ")
                                                  :
                                         ("\"key.converter\":\"org.apache.kafka.connect.storage.StringConverter\", " +
                                          "\"value.converter\":\"com.oracle.coherence.kafka.connect.util.CustomConverter\", " +
                                          sSerDer)
                                    ) +
                                    "\"topics\":\"" + sTopics + "\"" +
                                    "}}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + nPort + "/connectors"))
                        .header("content-type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(sinkDeploy))
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("REST response(2): " + response);

                assertThat(response.statusCode(), is(201));
                }
            }
        catch (Exception e)
            {
            Assertions.fail("Error configuring kafka sink connector: " + e.getMessage());
            }
        }

    protected static final String SINK_BASIC_CONNECTOR = "coh-sink";
    protected static final String SINK_POF_CONNECTOR = "coh-sink-pof";
    protected static final String SINK_PASTHROUGH_CONNECTOR = "coh-sink-PT";
    }
