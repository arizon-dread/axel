FROM tomcat:8-jre8

ADD axel-shs-broker-*.war /usr/local/tomcat/webapps/shs.war
ADD axel-home /home/axel/

ENV CATALINA_OPTS "-Daxel.home=file:/home/axel"

#VOLUME /home/axel

EXPOSE 8080

