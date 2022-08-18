@echo off
@title Boswell-v83
mvn exec:java -Dexec.mainClass=net.server.Server -Dexec.args=-Xmx2048m
pause