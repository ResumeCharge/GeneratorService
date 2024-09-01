mvn package spring-boot:repackage

sudo docker build -t generator-service .

sudo docker image tag generator-service adalaws/generator-service:latest

sudo docker image push adalaws/generator-service:latest
