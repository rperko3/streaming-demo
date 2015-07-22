# streaming-simulator

Simulates streaming data using twitter samples

intended usage for demo is to call and stream to a kafka topic using the kafka-console-producer.sh command  

Example:  
`curl --retry 999 --retry-max-time 0 -X GET http://localhost:8080/stream | $KAFKA_HOME/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic all`  

build:  
`mvn package`  

configure:  
`src/main/resources/streaming-simulator.properties`  

run:  
`java -Dapp.home=src/main/resources -jar target/stream-sim-0.1.0.jar`  

Usage:  

* streaming randomized tweets  
GET  
`http://localhost:8080/stream`   

* throttle  
GET  
`localhost:8080/throttle?mps=<messages per second>`  

