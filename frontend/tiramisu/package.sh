#!/bin/bash
mvn package
echo now copying .war file to webapps folder in tomcat
cp target/tiramisu.war /usr/local/Cellar/tomcat/8.5.6/libexec/webapps/tiramisu.war  
