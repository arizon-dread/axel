FROM centos:6

RUN yum update -y; yum install -y hostname git which wget; yum clean all;
RUN yum install -y java-1.8.0-openjdk

ENV JAVA_HOME /usr/lib/jvm/jre-1.8.0-openjdk.x86_64

RUN cp /etc/localtime /root/old.timezone && \
    rm -f /etc/localtime && \
    ln -s /usr/share/zoneinfo/Europe/Stockholm /etc/localtime

ENTRYPOINT java -version


