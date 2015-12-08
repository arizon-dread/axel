FROM centos:6

RUN yum update -y && yum install -y openldap-servers openldap-clients

EXPOSE 389

ADD *.ldif /tmp/
ADD ldap-setup.sh /tmp/
RUN bash /tmp/ldap-setup.sh

VOLUME /var/lib/ldap

CMD /usr/sbin/slapd -d 20
