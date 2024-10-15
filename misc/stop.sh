#!/bin/bash
PID=`ps -ef | grep echo-sshd-server | grep -v grep | awk '{print $2}'`
if [ -z "$PID" ]
then
  echo "echo-sshd-server is already dead! $PID"
else
  sudo kill -9 $PID
  echo "killed echo-sshd-server pid: $PID"
fi
