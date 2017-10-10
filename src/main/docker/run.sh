#! /bin/bash
service postgresql start
service solr start
echo "PORTAL WILL START"
java -Djava.security.egd=file:/dev/./urandom -jar portal.jar
exit 0