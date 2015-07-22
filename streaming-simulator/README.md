# streaming-simulator

Simulates streaming data using twitter samples

build:
mvn package

configure:
src/main/resources/streaming-simulator.properties

run:
java -Dapp.home=src/main/resources -jar target/stream-sim-0.1.0.jar

using:

streaming randomized tweets
GET
http://localhost:8080/stream 

throttle
GET
localhost:8080/throttle?mps=<messages per second>

