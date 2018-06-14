# camel-mqtt-demo
Apache Camel ESP8226 to Elasticsearch demo presented at Apache EU Roadshow 2018

This is a example Apache Camel application running under Spring Boot.

The example consumes messages from MQTT broker whose hostname and port trasforms the data and adds it to a Elasticsearch index.

Hostnames and ports are configured via application.properties, and the demo uses publicly available MQTT broker at broker.hivemq.com.

The sample is expecting to receive data as text in the format of two numbers separated by comma, e.g.: "25,75". The first number is considered to be the temperature (C), and the second number is considered to be the humidity (%).

To send data to Elasticsearch it needs to be transformed to JSON that looks like:

```json
{
  "@timestamp": "2018-06-14T11:25:00.000-2:00",
  "sensor": "sensor-2d43",
  "temperature": 25,
  "humidity": 60
}
```

To give it a spin run:

```shell
$ ./mvnw spring-boot:run
```

You'll need Elasticsearch instance running locally and to visualise the incoming data a Kibana instance. So download those from http://elastic.co/ and run:

```shell
$ cd elasticsearch-*/bin
$ ./elasticsearch
# in another terminal
$ cd kibana-*/bin
$ ./kibana
```

It makes sense to declare field types in Elastic search, to do so it's easiest to run this in Dev Tools within Kibana:

```
DELETE sensors

PUT sensors
{
  "mappings": {
    "data": {
      "properties": {
        "sensor": {
          "type": "text"
        },
        "temperature": {
          "type": "integer"
        },
        "humidity": {
          "type": "integer"
        },
        "@timestamp": {
          "type": "date"
        }
      }
    }
  }
}
```

This will delete the `sensors` index and re-create it by specifying the field data types.

You can import `visualisations.json` and `dashboard.json` into Kibana to get the demo dashboards.

You can use `mosquitto_pub` from Eclipse Mosquitto (https://mosquitto.org/) to generate some data.

```shell
$ mosquitto_pub -h 192.168.19.81 -t sensors/sensor-xyz -m '25,75'
```
