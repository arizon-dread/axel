#!/bin/bash

cd /usr/local/
tar xvfz /tmp/tomcat.tar.gz
mv *tomcat* tomcat
rm -rf /usr/local/tomcat/webapps/*
