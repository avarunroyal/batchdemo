# batchdemo
Simple refreshtokens-batchdemo microservice which does tokens status update operations connecting to MYSQL/Kafka docker containers

# RUN mysql as docker container - here just use docker-compose for both mysql&kafka
https://hub.docker.com/_/mysql
<br />$ docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=D@tabase123 -e MYSQL_DATABASE=refreshtokens -e MYSQL_USER=varun -e MYSQL_PASSWORD=D@tabase123 --name=mysql mysql:8.0.28

# RUN kafka as docker container
https://www.baeldung.com/ops/kafka-docker-setup
create a folder name kafka and add the docker-compose.yml and then open GIT BASH - execute below command
<br />$ cd kafka
<br />$ docker-compose up -d

# Create docker image of batchdemo microservice and push to docker repository
$ mvn clean install
<br />$ docker push devopsvarun/batchdemo:0.0.1-SNAPSHOT

# EXECUTE dbscript - INSERT query
open sqldeveloper - new connection - mysql db - localhost:3306 - user: root/varun as we used while creation - login
open dbscript file, execute script.

# RUN batchdemo microservice as docker container - singlenode
$ docker run --network=kafka_default -p 8080:8080 -e "SPRING_PROFILES_ACTIVE=dev" -e MYSQL_HOSTNAME=mysql -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 --link=kafka --name=batchdemo devopsvarun/batchdemo:0.0.1-SNAPSHOT




