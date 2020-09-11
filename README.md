# Arachne Community Edition build and run manual


## Upgrade
##### Upgrade to 1.16.x 
The upgrade to version 16 is feasible only from the 15th version. If you want to upgrade from a version lesser than 15, then go first to 15, and only then to 16

## Instalation
### Prerequisites
For building and run the Arachne please install following applications:
- [Apache Maven 3](https://maven.apache.org/download.cgi)
- [JDK up to 8u241](https://www.oracle.com/java/technologies/javase/javase8u211-later-archive-downloads.html)
- [LibreOffice 6](https://www.libreoffice.org/download/download/)  - for running the ArachneCentralAPI only
- [Apache Solr 7](http://lucene.apache.org/solr/downloads.html)
- [Postgres DBMS 9.6+](https://www.postgresql.org/download/windows/)

 
#### Prepare databases: 
Please create ohdsi user and 2 databases: arachne_portal and datanode. That can achieved by running following command in psql console:
```   
create role ohdsi with LOGIN password 'ohdsi';
create database arachne_portal owner ohdsi;
create database datanode owner ohdsi;
```

## Getting sources
Arachne network consists of two applications – Datanode and CentralApi. Sources are located in the github repositories. Please checkout: 
[ArachneCentralAPI](https://github.com/OHDSI/ArachneCentralAPI)
[ArachneNodeAPI](https://github.com/OHDSI/ArachneNodeAPI) 
The latest released version can be found in the master branch.

## Solr Configuration
Download solr 7 binaries.
Solr configuration is stored in ArachneCentralAPI/solr-config. Please run command to create and configure cores:  
```
solr start -c && \
    solr create_collection -c users -n arachne-config && \
    solr create_collection -c data-sources -n arachne-config && \
    solr create_collection -c studies -n arachne-config && \
    solr create_collection -c analyses -n arachne-config && \
    solr create_collection -c analysis-files -n arachne-config && \
    solr create_collection -c papers -n arachne-config && \
    solr create_collection -c paper-protocols -n arachne-config && \
    solr create_collection -c paper-files -n arachne-config && \
    solr create_collection -c submissions -n arachne-config && \
    solr create_collection -c insights -n arachne-config && \
    solr create_collection -c result-files -n arachne-config && \
    solr create_collection -c study-files -n arachne-config && \
    solr zk upconfig -n arachne-config -d /$PATHTO/ArachneCentralAPI/solr_config -z localhost:9983 && \
    solr stop -all
```
Start solr application by running following command in the terminal:
```
solr start -c && \
    solr zk upconfig -n arachne-config -d /$PATHTO/ArachneCentralAPI/solr_config -z localhost:9983 
```
Solr console should be available at: http://localhost:8983/solr



#### Build ArachneCentralAPI and ArachneNodeApi
Arachne application property files contains few configuration profiles. For this manual we use DEV. Please review available options in the: 
- ArachneCentralAPI/src/main/resources
- ArachneNodeAPI/src/main/resources

1. Open command prompt terminal in the ArachneCentralAPI/ folder and run:
mvn clean package -DskipTests -DskipDocker -P dev

2. Open command prompt terminal in the ArachneNodeAPI/ folder and run:
mvn clean package -DskipTests -DskipDocker -P dev

Two artifacts should be created: ArachneCentralAPI/target/portal-exec.jar and ArachneNodeAPI/target/datanode-exec.jar which are spring-boot fat jars and contains all the required dependencies.

#### Start ArachneCentralAPI
Create folder and grant RW access: mkdir -p /var/arachne/files/jcr/workspaces. 
Arachne applications expect jasypt encryption for passwords. Please generate values using e.g.:
[jasypt online encoder](https://www.devglan.com/online-tools/jasypt-online-encryption-decryption)


Start application using following command: 
```
java -jar ArachneCentralAPI/target/portal-exec.jar --office.home=/usr/lib/libreoffice/ --jasypt.encryptor.password=dummy "--spring.datasource.password=ENC(3b0hKjcVNZjGGLwd85Q+tw==)" "--spring.mail.password=ENC(O8Of4J1ejz9r7tZo05CS/Q==)" --portal.urlWhiteList=https://localhost:8080
```
spring.mail.password and spring.mail.username contains dummy values in this example, please replace them with your settings, otherwise send mail functionality will not work. Please encrypt email and database passwords with the same jasypt password. You can do it via [online](https://www.devglan.com/online-tools/jasypt-online-encryption-decryption)  or via [jasypt cli](http://www.jasypt.org/cli.html)  

ArachneCentralAPI should be available at: https://localhost:8080

#### Start ArachneNodeAPI

Arachne DataNodeAPI application at start registers in ArachneCentralAPI. In current scenario it should be running on https://localhost:8080

```
java -jar target/datanode-exec.jar --datanode.arachneCentral.host=https://localhost --datanode.arachneCentral.port=8080 --jasypt.encryptor.password=dummy "--spring.datasource.password=ENC(3b0hKjcVNZjGGLwd85Q+tw==)" "--spring.mail.password=ENC(O8Of4J1ejz9r7tZo05CS/Q==)" --spring.datasource.url=jdbc:postgresql://localhost:5432/datanode
```
ArachneNodeAPI should be available at: https://localhost:8880


Supply your mail sender configuration parameters otherwise emails will not work.

You may override any configuration parameter using  “--name=value” spring boot notation.
