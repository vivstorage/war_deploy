#!/usr/bin/env groovy

@Grab( 'net.java.dev.jna:jna:3.5.2' )
@Grab(group='net.java.dev.jna', module='platform', version='3.5.2')
import java.text.*
import java.nio.file.Files
import java.nio.file.Paths
import com.sun.jna.*
import com.sun.jna.win32.*
import com.sun.jna.platform.win32.*


void Restart_tomcat() {
    def Ee = new StringBuffer()
    if (System.properties['os.name'].toLowerCase().contains('windows')) { 
      try
        {
            W32ServiceManager serviceManager = new W32ServiceManager();
            serviceManager.open(Winsvc.SC_MANAGER_ALL_ACCESS);
            W32Service service = serviceManager.openService("tomcat8", Winsvc.SC_MANAGER_ALL_ACCESS);
            println "Stopping tomcat..." 
            service.stopService();
            println "Starting tomcat..."
            service.startService();
            service.close();
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
       }
    if (System.properties['os.name'].toLowerCase().contains('linux')) {
        println "Stopping tomcat..."
        stop = 'service tomcat8 stop'.execute()
        stop.consumeProcessErrorStream(Ee)
        stop.waitForOrKill(10)
        println Ee.toString()        
        
        println "Starting tomcat..."
        start = 'service tomcat8 start'.execute()
        start.consumeProcessErrorStream(Ee)
        start.waitForOrKill(10)
        println Ee.toString()        
        }
        
    if (System.properties['os.name'].toLowerCase().contains('mac os x')) {
        println "Stopping tomcat..."
        stop = 'sudo catalina stop'.execute()
        stop.consumeProcessErrorStream(Ee)
        stop.waitForOrKill(10)
        println Ee.toString()
        
        println "Starting tomcat..."
        start = 'sudo catalina start'.execute()
        start.consumeProcessErrorStream(Ee)
        start.waitForOrKill(10)
        println Ee.toString()
    }

}

void Application_check() {
    println "Checking application..."
    def url = new URL("http://localhost:8080/test-1/hello")
    HttpURLConnection connection = (HttpURLConnection) url.openConnection()
    connection.setRequestMethod("GET")
    sleep(3000)
    connection.connect()
    if (connection.responseCode == 200) {
        String response = connection.inputStream.withReader { Reader reader -> reader.text }
        println "Application check ok, http code " + connection.responseCode
        println response
    } else {
      exitWithMessage("ERROR problem with application, check data")
    }
}

void Deploy (String file, String host) {
    println "Deploying application..."
    //looking for tomcat webapp dir
    if (System.properties['os.name'].toLowerCase().contains('mac os x')) {
        new File("/usr/local/Cellar/tomcat/").eachFile() { file ->
        tomcat = file.getName() }
        catalina_base = '/usr/local/Cellar/tomcat/' + tomcat + '/libexec' }
    else { catalina_base = System.getenv('CATALINA_BASE')}
    if (!catalina_base)
        exitWithMessage("CATALINA_BASE variable not set")
    war_file = catalina_base+'/webapps/' + new File(file).name
    !new File (war_file).exists() || exitWithMessage("Found app file in tomcat webapp dir, run undeploy before deploy")
    Files.copy(Paths.get(file), Paths.get(war_file))
    Restart_tomcat()
    Application_check()
    println "Application deploy finished successfully!"
}

void unDeploy(String file, String host) {
    println "Undeploying application..."
    //looking for tomcat webapp dir
    if (System.properties['os.name'].toLowerCase().contains('mac os x')) {
        new File("/usr/local/Cellar/tomcat/").eachFile() { file ->
        tomcat = file.getName() }
        catalina_base = '/usr/local/Cellar/tomcat/' + tomcat + '/libexec' }
    else { catalina_base = System.getenv('CATALINA_BASE')}
    if (!catalina_base)
        exitWithMessage("CATALINA_BASE variable not set")
    war_file = catalina_base+'/webapps/' + file
    boolean fileSuccessfullyDeleted =  new File(war_file).delete()
    if (!fileSuccessfullyDeleted)
        exitWithMessage("ERROR: Can't remove war file, check if file exist")     
    Restart_tomcat()
    println "Application removed successfully!"
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
