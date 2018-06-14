package com.github.zregvart.camel.mqtt;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

/**
 * This is a example Apache Camel application running under Spring Boot.
 *
 * The example consumes messages from MQTT broker whose hostname and
 * port trasforms the data and adds it to a Elasticsearch index.
 *
 * Hostnames and ports are configured via application.properties,
 * and the demo uses publicly available MQTT broker at
 * broker.hivemq.com.
 *
 * The sample is expecting to receive data as text in the format of two
 * numbers separated by comma, e.g.: "25,75". The first number is
 * considered to be the temperature (C), and the second number is
 * considered to be the humidity (%).
 *
 * To send data to Elasticsearch it needs to be transformed to JSON that
 * looks like:
 *
 * <pre>
 * {
 *   "@timestamp": "2018-06-14T11:25:00.000-2:00",
 *   "sensor": "sensor-2d43",
 *   "temperature": 25,
 *   "humidity": 60
 * }
 * </pre>
 */
@SpringBootApplication
public class MqttSampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(MqttSampleApplication.class, args);
	}

	@Component
	public static class MqttSampleRoute extends RouteBuilder {

		@Override
		public void configure() throws Exception {
			// Camel Simple expression to generate the JSON as Java String
			String expression =
				"{"
				+   "\"@timestamp\":  \"${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}\","
				+   "\"sensor\":      \"${headers.CamelMQTTSubscribeTopic}\","
				+   "\"temperature\":   ${body.nextInt()},"
				+   "\"humidity\":      ${body.nextInt()}"
				+ "}";

			// from MQTT to Elasticsearch route
			from("mqtt:in?subscribeTopicName=sensors/+")

				// give the route a id, so it shows up nicely
				.id("demo-route")

				// data transformation: tokenize gives us java.util.Scanner
				// in the message body, so "25,75" is converted to 
				// 
				// Scanner scanner = new Scanner("25,75").useDelimiter(",")
				// 
				// this is not something you would typically do in Camel but
				// it serves the demo purposes well
				.transform(body().tokenize(","))
				.setBody(simple(expression))

				.to("elasticsearch-rest://elasticsearch?operation=Index&indexName=sensors&indexType=data");
		}

	}
}
