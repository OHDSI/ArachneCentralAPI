#! /bin/bash
service postgresql start
COUNTER=1
STATUS=""
echo "Checking Postgres service status"
while [ "$STATUS" != "OK" ]; do
  if  PGPASSWORD=ohdsi bash -c 'psql -h 127.0.0.1 -p 5434 -d arachne_portal -U ohdsi -w -q -n -c "\q"'
    then
      STATUS="OK"
      echo "Postgresql service check: OK"
    else
      echo "Postgresql service check: FAILED"
      sleep 5s
      let COUNTER=COUNTER+1
      echo
      echo Attempt is $COUNTER
  fi
  if [ $COUNTER -eq 60 ]
    then
      echo "####################################"
      echo "# Postgresql service check: FAILED #"
      echo "# Reason: Too many attempts        #"
      echo "####################################"
      exit 1
  fi
done
service solr start
echo "PORTAL WILL START"
java -Djava.security.egd=file:/dev/./urandom -jar portal.jar
exit 0