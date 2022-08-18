#!/bin/bash
# launch script
# mvn compile
# mvn install
mvn exec:java -Dexec.mainClass=net.server.Server -Dexec.args=-Xmx2048m