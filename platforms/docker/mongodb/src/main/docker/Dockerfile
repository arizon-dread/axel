FROM centos:6

ENV LC_ALL=en_US.UTF-8

COPY mongodb.repo /etc/yum.repos.d/

RUN yum update -y && yum install -y \
    mongodb-org-server \
    && yum clean all -y

MAINTAINER Predictly AB <support@predictly.se>

EXPOSE 27017

VOLUME /data/db

ENTRYPOINT mongod --dbpath /data/db

