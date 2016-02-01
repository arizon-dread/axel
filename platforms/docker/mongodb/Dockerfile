FROM axelbase:latest

COPY mongodb.repo /etc/yum.repos.d/
RUN yum install -y mongodb-org-server && yum clean all -y

EXPOSE 27017

VOLUME /data/db

ENTRYPOINT mongod --dbpath /data/db

