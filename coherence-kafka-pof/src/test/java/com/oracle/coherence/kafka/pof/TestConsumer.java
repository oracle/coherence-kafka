/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.kafka.pof;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

/**
 * @author Aleks Seovic  2021.01.27
 */
public class TestConsumer
    {
    public static void main(String[] args)
        {
        Map<String, Object> config = Map.of(
                "bootstrap.servers", "localhost:9092",
                "group.id", "test-consumer",
                "key.deserializer", KafkaPofDeserializer.class.getName(),
                "value.deserializer", KafkaPofDeserializer.class.getName()
        );

        KafkaConsumer<String, Person> consumer = new KafkaConsumer<>(config);
        consumer.subscribe(Set.of("test"));

        while (true)
            {
            ConsumerRecords<String, Person> records = consumer.poll(Duration.ofSeconds(1));

            for (ConsumerRecord<String, Person> record : records)
                {
                String event = new String(record.headers().lastHeader("event").value(), StandardCharsets.UTF_8);
                System.out.println(String.format("%s: %s=%s (partition=%d, offset=%d)", event, record.key(), record.value(), record.partition(), record.offset()));
                }

            consumer.commitSync();
            }
        }
    }
