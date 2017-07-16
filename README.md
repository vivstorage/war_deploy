Application can deploy/undeploy java war file to tomcat webapp dir

required linux debian or redhat family, jdk 1.8 and groovy 2.4, , deploy work only on local dir

deploy.gvy --help
usage: deploy.gvy --host <host> --action <deploy/undeploy> --app <war file
                  path> --config <script config path>
 -a,--app <app>             application war file path
 -ac,--action <action>      Action: deploy or undeploy application
 -c,--config <config>       config file
 -h,--help                  Show usage information
 -hn,--host <host>          hostname for application server
 -p,--password <password>   tomcat password
 -u,--username <username>   tomcat username

 
tested with "https://github.com/myCustomDemo/servlet/"
