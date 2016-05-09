
### Docker

Här ligger ett exempel på en Docker-konfiguration för Axel SHS Broker.

Projeket innehåller tre docker images och en docker-compose som visar ett sätt att använda dessa.

* ldap
    en enkel OpenLDAP som initialiseras med 2 st testaktörer
    
* mongodb
    en fristående MongoDB med de index behövs för Axel SHS Broker

* shs-broker
    Axel SHS Server deployad i en Apache Tomcat instans.

Konfigurationen för shs-broker ligger i [axel-home/etc/shs-broker.properties](shs-broker/src/main/docker/axel-home/etc/).
