#!/usr/bin/env groovy

import java.text.*

void Restart_tomcat() {
    def Ee = new StringBuffer()
    println "Stopping tomcat..."
    stop = 'service tomcat stop'.execute()
    stop.consumeProcessErrorStream(Ee)
    stop.waitForOrKill(10)
    println Ee.toString()

    println "Starting tomcat..."
    start = 'service tomcat start'.execute()
    start.consumeProcessErrorStream(Ee)
    start.waitForOrKill(10)
    println Ee.toString()
}

void Application_check() {
    println "Checking application..."
    http_status = '/usr/bin/curl -s -w "%{http_code}" -o /dev/null http://localhost:8080/test-1/hello'.execute()
    http_result = http_status.text
    if (http_result.toLowerCase() == '200'){
      println "http code: OK " +http_result
      get_html_body = 'curl -sSf http://localhost:8080/test-1/hello'.execute()
      println get_html_body.text
    } else {
      exitWithMessage("ERROR problem with application, check data")
    }
}

void Deploy (String file, String host) {
    println "Deploying application..."
    //looking for tomcat webapp dir
    find_proc = 'find / -type d -name webapps'.execute() | 'grep -w tomcat'.execute()
    find_proc.in.eachLine { line -> tomcat_webapp_dir=line }
    copy_file = ['cp' ,file, tomcat_webapp_dir].execute()
    print copy_file.err.text
    Restart_tomcat()
    Application_check()
}


void unDeploy(String file, String host) {
    println "Undeploying application..."
    //looking for tomcat webapp dir
    find_proc = 'find / -type d -name webapps'.execute() | 'grep -w tomcat'.execute()
    find_proc.in.eachLine { line -> tomcat_webapp_dir=line }
    filename = tomcat_webapp_dir +"/" + file
    boolean fileSuccessfullyDeleted =  new File(filename).delete()
    Restart_tomcat()
    println "Undeploying done"
}

def update_config(args) {
   def cli = new CliBuilder(usage: 'deploy.gvy --host <host> --action <deploy/undeploy> --app <war file path> --config <script config path>')
    // Create the list of options.
    cli.with {
        h longOpt: 'help', 'Show usage information'
        hn longOpt: 'host', args: 1, argName: 'host','hostname for application server'
        u longOpt: 'username', args: 1, argName: 'username', 'tomcat username'
        p longOpt: 'password', args: 1, argName: 'password', 'tomcat password'
        ac longOpt: 'action', args: 1, argName: 'action', 'Action: deploy or undeploy application'
        a longOpt: 'app', args: 1, argName: 'app', 'application war file path'
        c longOpt: 'config', args: 1, argName: 'config',  'config file', required: true
    }

    def options = cli.parse(args)
    if (!options) {
        return
    }
    configFile = new File(options.c)
    configFile.exists() || exitWithMessage("File not found ${configFile} ")

    //read config file
    def properties = new Properties()
        this.getClass().getResource( options.c ).withInputStream {
            properties.load(it)
    }
    def config = new ConfigSlurper().parse(properties)

    //will show help dialog
    if (options.h){
        cli.usage()
        return
    }

    //update options
    if (options.hn){
        config.host = options.hn    }
    else if (!config.host){
        exitWithMessage("No host specified") }
    if (options.a){
        config.app = options.a }
    else if (!config.app){
        exitWithMessage("No application file specified") }
    if (options.u){
        config.user = options.u    }   
    if (options.p){
        config.password = options.p   }
    if (options.ac){
        config.action = options.ac  }
    else if (!config.action){
        exitWithMessage("No action specified") }

    //check if war file exist and extension is .war
    warFile = new File(config.app)
    warFile.name.endsWith('war') || exitWithMessage('Unknown file type ' + warFile.name + ', .war only accepted')
    warFile.exists() || exitWithMessage("File not found ${warFile} ")

    if (config.action.toLowerCase() == 'deploy') {
        Deploy(config.app, config.host)
    } else if (config.action.toLowerCase() == 'undeploy') {
        unDeploy(warFile.name, config.host)
    } else { exitWithMessage('Unknown action, please use deploy or undeploy')}
     }

static void exitWithMessage(String message) {
    System.err.println(message)
    System.exit(1)
}

println update_config(args)
