# streaming-message-archiver

reads from stdin and writes zip files after 'n' messages are collected

intended usage for the demo is to use in combination with the kafka-console-consumer.sh app and read from a topic like the ten-percent.  

configured on the command line.  

Build:  
`mvn package`  

Usage:  
`java -jar target/streaming-message-archiver-1.0-SNAPSHOT.jar <message count per zip> <thread count>`  

Example:    
`$KAFKA_HOME/bin/kafka-console-consumer.sh --from-beginning --zookeeper localhost:2181 --topic ten-percent java -jar target/streaming-message-archiver-1.0-SNAPSHOT.jar 5000 4`  

