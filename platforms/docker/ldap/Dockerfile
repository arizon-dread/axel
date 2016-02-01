FROM axelbase:latest

RUN yum install -y openldap-servers openldap-clients && yum clean all -y

EXPOSE 389

ADD *.ldif /tmp/
ADD ldap-setup.sh /tmp/
RUN bash /tmp/ldap-setup.sh

VOLUME /var/lib/ldap

CMD /usr/sbin/slapd -d 72
