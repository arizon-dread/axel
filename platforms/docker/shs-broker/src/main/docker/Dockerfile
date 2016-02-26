FROM centos:6

ENV LC_ALL=en_US.UTF-8

RUN yum update -y && yum install -y \
    java-1.8.0-openjdk-headless \
    tar \
    apr \
    && yum clean all -y

MAINTAINER Predictly AB <support@predictly.se>

ENV TOMCAT_VERSION=8.0.32
RUN curl http://apache.mirrors.spacedump.net/tomcat/tomcat-8/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz \
       | tar -xzC /opt \
    && rm -rf /opt/apache-tomcat-${TOMCAT_VERSION}/webapps/*


ENV CATALINA_OPTS "-Daxel.home=file:/home/axel"
#VOLUME /home/axel
EXPOSE 8080
CMD exec /opt/apache-tomcat-${TOMCAT_VERSION}/bin/catalina.sh run
ENV SHS_ORG_ID=${SHS_ORG_ID:-0000000000}

COPY axel-home /home/axel/
ADD axel-shs-broker.war /opt/apache-tomcat-${TOMCAT_VERSION}/webapps/ROOT.war
