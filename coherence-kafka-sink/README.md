# Coherence Sink Connect

## Overview

This connect plugin listens on a Kafka topic and populates a Coherence cache. If specified, a topic to cache mapping associates the Kafka topic to a Coherence named cache, e.g.:

```
MyTopic->MyCache
```

If no mapping exists, the topic name is used as the cache name.

Data can be serialized and de-serialized to/from Kafka using a custom converter.

## Quickstart

### Build project and run tests

The tests portion of this project contains a complete example creating a Kafka instance and putting data into Coherence from Kafka.
It can be used as a starting point for a development project. Remove the `-Ddocker.showLogs` if the output is too verbose.

```
mvn -Ddocker.showLogs verify
```

The following sections contain details on the specific actions needed to install and run the plugin.

### Installing and deploying the plugin

The following instructions apply to the Confluent distribution of Kafka. The OSS distribution is similar and will be addressed in a different section.

#### Installation

From the location where connect is to be started, run the following command:

```
confluent-hub install --no-prompt /<plugin location>/oracle-coherence-kafka-connect-1.0.0-SNAPSHOT.zip
```

#### Deployment

Using the Confluent distribution, the plugin must be deployed and started using a connect REST call.

The following curl command starts the plugin with the configured parameters:

```
curl -X POST -H "Content-Type: application/json" \
    http://localhost:8083/connectors \
    -d '{"name":"sink",
             "config":
                 {
                     "connector.class":"com.oracle.coherence.kafka.connect.CoherenceSinkConnector",
                     "topics":"MyTopic",
                     "coherence.cache.mappings":"MyTopic->MyCache"
                 }
         }'
```

### Data conversion

Kafka natively stores keys and values as series of bytes. These are then converted to/from their original format using converters, which wrap serializers/deserializers.

In instances where serialization is not desired, it is possible to bypass this process. This is referred to as "pass-through".

In both cases it is necessary to configure a converter via the `key.converter` and `value.converter` properties.

#### Converting data formats

Kafka Converters wrap the serializer/deserializer used. Users may provide their own converters or existing ones, as long as the same underlying serializer/deserializer is also used on the Coherence side.

For an example of how this works, see the `com.oracle.coherence.kafka.connect.util.CustomConverter`, which allows you to simply configure serializer/deserializer, along with the example integration test serializing a User class into/from json.

#### Pass-through mode

To use pass-through, for example when the data is stored in the Coherence raw format, the `key.` and `value.converter` properties must be set as:

```
key.converter=org.apache.kafka.connect.converters.ByteArrayConverter
value.converter=org.apache.kafka.connect.converters.ByteArrayConverter
```