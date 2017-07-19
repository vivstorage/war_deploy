Application can deploy/undeploy java war file to tomcat webapp dir  
  
required tomcat 8, jdk 1.8 and groovy 2.4, , deploy work only on local dir
Windows should not have any spaces in tomcat installation dir
  
deploy.gvy --help  
usage: deploy.gvy --host <host> --action <deploy/undeploy> --app <war filepath> --config <script config path>  
 -a,--app <app>             application war file path  
 -ac,--action <action>      Action: deploy or undeploy application  
 -c,--config <config>       config file  
 -h,--help                  Show usage information  
 -hn,--host <host>          hostname for application server  
 -p,--password <password>   tomcat password  
 -u,--username <username>   tomcat username  
  
   
tested on https://github.com/myCustomDemo/servlet
