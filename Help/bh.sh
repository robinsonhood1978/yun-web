#!/bin/sh
cp -f /Users/mark/tomcat/cms/Help/config.txt /Users/mark/tomcat/cms/WebRoot/WEB-INF/config.txt
docker run -d --name tomcat -p 8080:8080 -v /Users/mark/tomcat/cms/WebRoot:/opt/tomcat/webapps/ROOT --rm dordoka/tomcat
