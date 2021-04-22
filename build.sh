#!/bin/bash

#docker-compose down

docker-compose build
docker-compose up -d --remove-orphans

until docker exec mysql_master sh -c 'export MYSQL_PWD=123456; mysql -u root -e ";"'
do
    echo "Waiting for mysql_master database connection..."
    sleep 4
done

# create an account solely for the purposes of replication, that account needs only the REPLICATION SLAVE privilege
priv_stmt='GRANT REPLICATION SLAVE ON *.* TO "mydb_replica_user"@"%" IDENTIFIED BY "mydb_replica_pwd"; FLUSH PRIVILEGES;'
docker exec mysql_master sh -c "export MYSQL_PWD=123456; mysql -u root -e '$priv_stmt'"

until docker-compose exec mysql_replica sh -c 'export MYSQL_PWD=123456; mysql -u root -e ";"'
do
    echo "Waiting for mysql_replica database connection..."
    sleep 4
done

docker-ip() {
    docker inspect --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' "$@"
}

MS_STATUS=`docker exec mysql_master sh -c 'export MYSQL_PWD=123456; mysql -u root -e "SHOW MASTER STATUS"'`
CURRENT_LOG=`echo $MS_STATUS | awk '{print $6}'`
CURRENT_POS=`echo $MS_STATUS | awk '{print $7}'`

#CHANGE MASTER TO changes the parameters that the replica server uses for connecting to the source and for reading data from the source.
start_replica_stmt="CHANGE MASTER TO MASTER_HOST='$(docker-ip mysql_master)',MASTER_USER='mydb_replica_user',MASTER_PASSWORD='mydb_replica_pwd',MASTER_LOG_FILE='$CURRENT_LOG',MASTER_LOG_POS=$CURRENT_POS; START SLAVE;"
start_replica_cmd='export MYSQL_PWD=123456; mysql -u root -e "'
start_replica_cmd+="$start_replica_stmt"
start_replica_cmd+='"'
docker exec mysql_replica sh -c "$start_replica_cmd"

docker exec mysql_replica sh -c "export MYSQL_PWD=123456; mysql -u root -e 'SHOW SLAVE STATUS \G'"