#!/bin/bash

PW=admin

#sed -i "s/olcSaslSecProps.*/olcSaslSecProps: none/" /etc/openldap/slapd.d/cn=config.ldif

cp /usr/share/openldap-servers/DB_CONFIG.example /var/lib/ldap/DB_CONFIG

echo "olcRootPW: `slappasswd -n -s $PW`" >> /etc/openldap/slapd.d/cn=config/olcDatabase={2}bdb.ldif
#sed "s/olcRootPW:.*/olcRootPW: $PW" /etc/openldap/slapd.d/cn=config.ldif

SUFFIX="L=SHS"
sed -i "s/olcRootDN:.*/olcRootDN: cn=Manager, $SUFFIX/" /etc/openldap/slapd.d/cn=config/olcDatabase={2}bdb.ldif
sed -i "s/olcSuffix:.*/olcSuffix: $SUFFIX/" /etc/openldap/slapd.d/cn=config/olcDatabase={2}bdb.ldif

slapadd -b "cn=config" -l /tmp/shs-schema.ldif
slapadd -b "L=SHS" -l /tmp/axel-initial.ldif

#ldapmodify -Q -Y EXTERNAL -H ldapi:/// -f /etc/axel-update.ldif
#ldapadd -Q -Y EXTERNAL -H ldapi:/// -f /etc/shs-schema.ldif

#ldapadd -D "cn=Manager,l=SHS" -w $PW -f /etc/axel-initial.ldif
