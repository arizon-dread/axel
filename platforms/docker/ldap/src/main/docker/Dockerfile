FROM centos:6

ENV LC_ALL=en_US.UTF-8

RUN yum update -y && yum install -y \
    openldap-servers \
    openldap-clients \
    && yum clean all -y

MAINTAINER Predictly AB <support@predictly.se>

EXPOSE 389

ADD *.ldif /tmp/
ADD ldap-setup.sh /tmp/
RUN bash /tmp/ldap-setup.sh

VOLUME /var/lib/ldap

CMD /usr/sbin/slapd -d 72
