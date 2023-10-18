# Coherence integration with Apache Kafka

This project provides component libraries that support moving data from a Coherence map to a Kafka topic and vice versa.

Supported on Coherence CE 21.06 and later.

## Getting Started

The Coherence Kafka integration project publishes a set of jar dependencies that you use in your application.
There is nothing to install, you use the Maven modules just as you would any other Java dependencies.

## The following components are provided

- **Kafka Entry Store** - a CacheStore type of extension that copies data from a Coherence map to a Kafka topic upon modification. This is contained in the `coherence-kafka-core` project.
- **Kafka Sink Connector** - an Apache Kafka Connect plugin of type "sink", which copies data posted to a Kafka topic to a Coherence map. This is contained in the `coherence-kafka-sink` project

Serializers and deserializers can be configured to handle data to and from Kafka, or an optimized pass-through mode will handle serialized entries which are already in a Coherence binary form.

## Kafka Entry Store

When configuring a cachestore-scheme as follows, Coherence will send entries to the specified Kafka broker:

<details>
<summary>Click to see config</summary>
<p>

```xml
<?xml version="1.0"?>

<cache-config xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xmlns="http://xmlns.oracle.com/coherence/coherence-cache-config"
              xmlns:kafka="class://com.oracle.coherence.kafka.KafkaNamespaceHandler"
              xsi:schemaLocation="http://xmlns.oracle.com/coherence/coherence-cache-config coherence-cache-config.xsd">
  <caching-scheme-mapping>
    <cache-mapping>
      <cache-name>foo</cache-name>
      <scheme-name>partitioned-rwbm-kafka</scheme-name>
    </cache-mapping>
  </caching-scheme-mapping>

  <caching-schemes>
    <distributed-scheme>
      <scheme-name>partitioned-rwbm-kafka</scheme-name>

      <backing-map-scheme>
        <partitioned>true</partitioned>
        <read-write-backing-map-scheme>
          <internal-cache-scheme>
            <local-scheme>
            </local-scheme>
          </internal-cache-scheme>
          <cachestore-scheme>
            <class-scheme>
              <kafka:producer>
                <!-- use default class -->
                <kafka:topic-name>{cache-name}</kafka:topic-name>
                <!-- default for passthrough is false -->
                <kafka:bootstrap-servers>localhost:9092</kafka:bootstrap-servers>
                <kafka:key-serializer>org.apache.kafka.common.serialization.StringSerializer</kafka:key-serializer>
                <kafka:value-serializer>org.apache.kafka.common.serialization.StringSerializer</kafka:value-serializer>
                <kafka:max-block-ms>5000</kafka:max-block-ms>
              </kafka:producer>
            </class-scheme>
          </cachestore-scheme>
        </read-write-backing-map-scheme>
      </backing-map-scheme>

      <autostart>true</autostart>
    </distributed-scheme>

  </caching-schemes>
</cache-config>
```

</p>
</details>

The `kafka:producer` portion takes properties that are intended for a Kafka producer and uses them (the dash is turned into a dot, for example `bootstrap-servers` to `bootstrap.servers`)

### Supported operations are:
- `load()` and `loadAll()`: not supported; see Kafka Sink Connector for this
- `store()`: Corresponds to a `put()` operation on a `NamedMap`, and performs a `producer.send()` on the specified Kafka topic
- `storeAll()`: Corresponds to a `putAll()` operation on a `NamedMap`, where each element in the map will be sent to the specified Kafka topic
- `erase()`: Corresponds to a `remove()` operation on a `NamedMap`; when a topic is configured with a `compact` policy, all records with the same key will be deleted (aka `tombstone`)
- `eraseAll()`: Corresponds to a `removeAll()` operation on a `NamedMap`, and is equivalent to calling `erase()` on each element of the passed map

For more details see the `coherence-kafka-core` project.

## Kafka Sink Connector

The sink connector is built as a zip file intended for being deployed in a Connect process' environment.

### Supported operations are:
- Coherence Entry creation/update: records published on a topic are forwarded to a Coherence NamedMap and corresponding entries are created
- Coherence Entry deletion: when Kafka records have a null value, the corresponding entry in Coherence is removed if it existed

**Note:** Kafka records with a null key cannot be represented in Coherence and are discarded with a logged message such as:

```text
connect> [2021-10-20 19:21:35,680] ERROR The key is null for record: SinkRecord{kafkaOffset=1, timestampType=CreateTime} ConnectRecord{topic='MyTopic', kafkaPartition=0, key=null, keySchema=Schema{STRING}, value=User(NoKey, 20), valueSchema=Schema{STRUCT}, timestamp=1634757687699, headers=ConnectHeaders(headers=)} (com.oracle.coherence.kafka.connect.sink.CoherenceSinkTask)
```

Configuration is done using a curl command such as the following (Confluent Kafka)

<details><summary>Click to see command</summary>
<p>

```
curl -X POST -H "Content-Type: application/json" \
    http://localhost:8083/connectors \
    -d '{"name":"coh-sink",
             "config":
                 {
                     "connector.class":"com.oracle.coherence.kafka.connect.CoherenceSinkConnector",
                     "topics":"MyTopic",
                     "coherence.cache.mappings":"MyTopic->MyCache"
                 }
         }'
```

</p>
</details>

or a properties file for Kafka OSS

<details><summary>Click to expand</summary>
<p>

```properties
name=coh-sink
topics=MyTopic
tasks.max=1
connector.class=com.oracle.coherence.kafka.connect.CoherenceSinkConnector
coherence.cache.mappings=MyTopic->MyCache
key.converter=com.oracle.coherence.kafka.connect.util.CustomConverter
value.converter=com.oracle.coherence.kafka.connect.util.CustomConverter
value.converter.serializer=org.apache.kafka.common.serialization.StringSerializer
value.converter.deserializer=org.apache.kafka.common.serialization.StringDeserializer
key.converter.serializer=org.apache.kafka.common.serialization.StringSerializer
key.converter.deserializer=org.apache.kafka.common.serialization.StringDeserializer
```

</p>
</details>

## Contributing

This project welcomes contributions from the community. Before submitting a pull request, please [review our contribution guide](./CONTRIBUTING.md)

## Security

Please consult the [security guide](./SECURITY.md) for our responsible security vulnerability disclosure process

## License

Copyright (c) 2021, 2023 Oracle and/or its affiliates.

Released under the Universal Permissive License v1.0 as shown at
<https://oss.oracle.com/licenses/upl/>.
