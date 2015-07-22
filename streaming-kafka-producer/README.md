# streaming-kafka-producer

reads from stdin and writes (or routes) to two kafka topics

intended usage for the demo is to use in combination with the kafka-console-consumer.sh command
configuration is locate in streaming-kafka-producer.properties  

Build:  
`mvn package`  

Example:  
`$KAFKA_HOME/bin/kafka-console-consumer.sh --from-beginning --zookeeper localhost:2181 --topic all |  java -Xmx2g -jar target/streaming-kafka-producer-1.0.jar streaming-kafka-producer.properties`   

##known issue: runs out of memory##
