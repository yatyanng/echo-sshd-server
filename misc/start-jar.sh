#!/bin/bash
PID=`ps -ef | grep echo-sshd-server.jar | grep -v grep | awk '{print $2}'`
if [ -z "$PID" ]
then
  sudo nohup /opt/jdk-17/bin/java -jar echo-sshd-server.jar conf > logs/exec.log 2>&1 &
  sleep 1 
  PID=`ps -ef | grep echo-sshd-server.jar | grep -v grep | awk '{print $2}'`
  echo "started echo-sshd-server.jar pid: $PID"
else
  echo "already exist echo-sshd-server.jar pid: $PID"
fi


