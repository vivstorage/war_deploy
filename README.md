Application can deploy/undeploy java war file to tomcat webapp dir  
 
Support Linux, Windows, MacOS  
required tomcat 8, jdk 1.8 and groovy 2.4, deploy work only on local dir,  
Windows should have no spaces in tomcat webapp dir path and script should 'run as administrator' or 
user should have admin rights in system.  
For Linux and Windows CATALINA_BASE environment variable should be defined in system.  
Mac OS version tested only on Darwin mbp13 16.6.0 with tomcat 8.5.16 installed from brew  
and only 1 tomcat should be installed  
  
deploy.gvy --help  
usage: deploy.gvy --host <host> --action <deploy/undeploy> --app <war filepath> --config <script config path>  
 -a,--app <app>             application war file path  
 -ac,--action <action>      Action: deploy or undeploy application  
 -c,--config <config>       config file  
 -h,--help                  Show usage information  
 -hn,--host <host>          hostname for application server  
 -p,--password <password>   tomcat password  
 -u,--username <username>   tomcat username  
  
   
App for test https://github.com/myCustomDemo/servlet
