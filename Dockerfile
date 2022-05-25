FROM dordoka/tomcat
 
ADD ./WebRoot/ /opt/tomcat/webapps/ROOT

EXPOSE 8080

CMD ["catalina.sh", "run"]
