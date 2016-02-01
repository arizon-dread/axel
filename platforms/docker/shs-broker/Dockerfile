FROM axelbase:latest

RUN yum install -y java-1.8.0-openjdk-headless tar apr && yum clean all -y

ADD http://apache.mirrors.spacedump.net/tomcat/tomcat-8/v8.0.30/bin/apache-tomcat-8.0.30.tar.gz /tmp/tomcat.tar.gz
ADD tomcat-setup.sh /tmp/
RUN chmod +x /tmp/tomcat-setup.sh && /tmp/tomcat-setup.sh

ADD axel-shs-broker-*.war /usr/local/tomcat/webapps/ROOT.war

ENV CATALINA_OPTS "-Daxel.home=file:/home/axel"
#VOLUME /home/axel
EXPOSE 8080
CMD exec /usr/local/tomcat/bin/catalina.sh run

COPY axel-home /home/axel/
ENV SHS_ORG_ID=${SHS_ORG_ID:-0000000000}
