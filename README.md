
Axel
============

## Introduktion

Axel 2.0.0

* innehåller en samling kompontenter för att möjliggöra integration med SHS och RIV
* implementerar en SHS-server med hjälp av dessa komponenter
* implementerar en enkel brygga mellan RIV och denna SHS server
* samt exempel på hur SSEK kan bryggas mot RIV/SHS

Således kan Axel användas både som en självständig SHS-nod, "verktygslåda" för att integrera
proprietära ESB-lösningar med SHS-nätverket, samt en kommunikations-axel mellan RIV-, SSEK- och SHS-nätverken.
(D.v.s mellan myndigheter, föräkringsbolag och vården)

## Förberedelser

Dessa programvaror behöver finnas förinstallerade för att man ska kunna bygga och köra Axel:

* Java JDK 1.8
* Maven 3.2
* MongoDB 2.4  
* LDAP Server med SHS-katalogen, central eller lokal. Se nedan för instruktioner.


Bygga
---------------

Bygg Axel från toppnivån med

    mvn clean install

Förutom att alla komponenter nu finns i det lokala maven-repot så är en default Axel-distribution
förpaketerad i, t.ex:

    axel/platforms/karaf/distribution/target/axel-2.0.0.tar.gz

En WAR-fil med enbart SHS-servern finns paketerad i 
    
    axel/platforms/war/shs-broker-war/target/axel-shs-broker-2.0.0.war

Installera
---------------
Det enklaste sättet att installera en Axel-server är att få tag i, och packa upp distributionsfilen
`axel-2.0.0.tar.gz` på lämpligt ställe:

    $ tar xvfz axel-2.0.0.tar.gz -C /opt

När detta är gjort kan du läsa om konfiguration m.m. i filen `/opt/axel-2.0.0/README.md`


*Observera att denna förpaketerade version enbart är testad under Linux.*



Konfigurera OpenLDAP för SHS
-------------------------------

Ldap-servern måste prepareras med

* En rot för SHS, L=SHS
* SHS-schemat måste läggas in.
* Samt importera en liten struktur från en LDIF-fil.

Schema och exempel ldif-fil ligger i `axel/platforms/karaf/distribution/src/main/distribution/etc/ldap`.







