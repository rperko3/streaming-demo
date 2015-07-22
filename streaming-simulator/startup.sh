#!/usr/bin/env bash

printUsage() {
 echo "Usage: startup.sh [services home dir or directory of property file]"
 echo "example: ./startup.sh /opt/some-dir"
 echo "example 2: ./startup.sh \`pwd\`"
}

if [ $# = 0 ]; then
  printUsage
  exit
fi

java -jar -Dapp.home=$1 stream-sim-0.1.0.jar
