/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.server;

import java.util.Properties;

import org.openejb.OpenEJB;
import org.openejb.util.Logger;
import org.openejb.util.Messages;
import org.openejb.util.SafeToolkit;

/**
 * This is the base class for orcistrating the other daemons 
 * which actually accept and react to calls coming in from 
 * different protocols or channels.
 * 
 * To perform this task, this class will
 *    newInstance()
 *    init( port, properties)
 *    start()
 *    stop()
 * 
 * 
 */
public class Server implements org.openejb.spi.Service {

    private SafeToolkit toolkit = SafeToolkit.getToolkit("OpenEJB EJB Server");
    private Messages _messages = new Messages( "org.openejb.server" );
    private Logger logger = Logger.getInstance( "OpenEJB.server.remote", "org.openejb.server" );

    Properties props;

    static Server server;

    public static Server getServer() {
        if ( server == null ) {
            server = new Server();
        }

        return server;
    }

    public void init(java.util.Properties props) throws Exception {
        this.props = props;

        System.out.println( _messages.message( "ejbdaemon.startup" ) );

        props.put("openejb.nobanner", "true");

        OpenEJB.init(props, new ServerFederation());

        System.out.println("[init] OpenEJB Remote Server");
        ServiceManager manager = ServiceManager.getManager();
        manager.init();
        manager.start();

    }


//
//  Vector           clientSockets  = new Vector();
//  ServerSocket     serverSocket   = null;
//
//  // The EJB Server Port
//  int    port = 4201;
//  String ip   = "127.0.0.1";
//  Properties props;
//
//  static InetAddress[] admins;
//  
//  boolean stop = false;
//
//
//  public static Server getServer(){
//      if ( thiss == null ) {
//          thiss = new EjbDaemon();
//      }
//
//      return thiss;
//  }
//
//  public void init(Properties props) throws Exception{
//
//      this.props = props;
//      printVersion();
//
//      System.out.println( _messages.message( "ejbdaemon.startup" ) );
//
//      props.putAll(System.getProperties());
//      props.put("openejb.nobanner", "true");
//      OpenEJB.init(props, this);
//
//      System.out.println("[init] OpenEJB Remote Server");
//
//
//      clientJndi = (javax.naming.Context)OpenEJB.getJNDIContext().lookup("openejb/ejb");
//
//      DeploymentInfo[] ds = OpenEJB.deployments();
//
//      // This intentionally has the 0 index as null. The 0 index is the
//      // default value of an unset deploymentCode.
//      deployments = new DeploymentInfo[ ds.length +1 ];
//
//      System.arraycopy( ds, 0, deployments, 1, ds.length);
//
//      deploymentsMap = new HashMap( deployments.length );
//      for (int i=1; i < deployments.length; i++){
//          deploymentsMap.put( deployments[i].getDeploymentID(), new Integer(i));
//      }
//
//      parseAdminIPs();
//  }
//
//
//
//  public synchronized void start(){
//      try{
//          System.out.println("  ** Starting Services **");
//          System.out.println("  NAME             IP              PORT");
//
//          SafeProperties safeProps = toolkit.getSafeProperties(props);
//
//          /* Start the EJB Server threads *************/
//          /*   ejb server       127.0.0.1       4201  */
//          port = safeProps.getPropertyAsInt("openejb.server.port");
//          ip   = safeProps.getProperty("openejb.server.ip");
//
//          sMetaData = new ServerMetaData(ip, port);
//
//          System.out.print("  ejb server       ");
//          try{
//              serverSocket = new ServerSocket(port, 20, InetAddress.getByName(ip));
//          } catch (Exception e){
//              System.out.println("");
//              System.out.println("");
//              System.out.println("");
//              System.out.println("Cannot bind to the ip: "+ip+" and port: "+port+".");
//              System.out.println("Received exception: "+ e.getClass().getName()+":"+ e.getMessage());
//              System.out.println("");
//              System.out.println("This is most likely because you have another version of OpenEJB running");
//              System.out.println("somewhere on your machine.  To shut that version down, type:");
//              System.out.println("");
//              System.out.println("telnet "+ip+" "+(port-1));
//              System.out.println("");
//              System.out.println("and issue the command 'stop'.  If you do not get an OpenEJB prompt when");
//              System.out.println("you telnet, then another program has that address and port bound. "); 
//      	System.out.println("You can select a new port by using the -p option of the start command: ");
//              System.out.println("");
//              System.out.println("\topenejb start -p <port>");
//              System.out.println("");
//      	System.out.println("You can select a new ip address by using the -h option of the start command: ");
//              System.out.println("");
//              System.out.println("\topenejb start -h <address>");
//              System.out.println("");
//
//              System.exit(1);
//          }
//          int threads = Integer.parseInt( (String)props.get("openejb.server.threads") );
//          for (int i=0; i < threads; i++){
//              Thread d = new Thread(this);
//              d.setName("EJB Daemon ["+i+"]");
//              d.setDaemon(true);
//              d.start();
//          }
//
//          String serverIP = serverSocket.getInetAddress().getHostAddress();
//          serverIP += "         ";
//          serverIP = serverIP.substring(0,15);
//
//          System.out.println(serverIP +" "+port);
//
//          /* Start the Text Admin Console *************/
//          /*   admin console    127.0.0.1       4202  */
//          System.out.print("  telnet console   ");
//
//          TextConsole textConsole = new TextConsole(this);
//          textConsole.init(props);
//          textConsole.start();
//
//          System.out.println(serverIP +" "+(port+1));
//          
//          /* Start the Text Admin Console *************/
//          /*   admin console    127.0.0.1       4202  */
//          System.out.print("  web console      ");
//          
//          // Start the WebAdmin thread
//          // TODO:1: Make this configurable
//          // using vm properties
//          HttpDaemon httpd = new HttpDaemon(this);
//          httpd.init(props);
//          Thread admin = new Thread(httpd);
//          admin.start();
//
//          System.out.println(serverIP +" "+(port+2));
//
//          serverIP = serverIP.trim();
//
//          System.out.println("-----------------INFO------------------");
//          System.out.println("To administer the server via telnet,   ");
//          System.out.println("start a telnet client and telnet to:"); 
//          System.out.print(" telnet ");
//          System.out.println(serverIP+" "+(port+1));
//          System.out.println("");
//          System.out.println("To administer the server via http, open");
//          System.out.println("a web browser to the following URL: "); 
//          System.out.print(" http://");
//          System.out.println(serverIP+":"+(port+2));
//          System.out.println("---------------------------------------");
//          System.out.println("Ready!");
//          /*
//           * This will cause the user thread (the thread that keeps the
//           *  vm alive) to go into a state of constant waiting.
//           *  Each time the thread is woken up, it checks to see if
//           *  it should continue waiting.
//           *
//           *  To stop the thread (and the VM), just call the stop method
//           *  which will set 'stop' to true and notify the user thread.
//           */
//          try{
//              while ( !stop ) {
//                  //System.out.println("[] waiting to stop \t["+Thread.currentThread().getName()+"]");
//                  this.wait(Long.MAX_VALUE);
//              }
//          } catch (Throwable t){
//              logger.fatal("Unable to keep the server thread alive. Received exception: "+t.getClass().getName()+" : "+t.getMessage());
//          }
//          System.out.println("[] exiting vm");
//          logger.info("Stopping Remote Server");
//      }catch (Throwable t){
//          t.printStackTrace();
//
//      }
//  }
//
//  public static void checkHostsAdminAuthorization(InetAddress client, InetAddress server) throws SecurityException {
//      // Authorization flag.  This starts out as unauthorized
//      // and will stay that way unless a matching admin ip is
//      // found.
//      boolean authorized = false;
//
//      // Check the client ip agains the server ip. Hosts are
//      // allowed to administer themselves, so if these ips
//      // match, the following for loop will be skipped.
//      authorized = client.equals( server );
//
//      for (int i=0; i < admins.length && !authorized; i++){
//          authorized = admins[i].equals( client );
//      }
//
//      if ( !authorized ) {
//          throw new SecurityException("Host "+client.getHostAddress()+" is not authorized to administer this server.");
//      }
//  }
//
//  public void stop(InetAddress client, InetAddress server) throws SecurityException {
//      checkHostsAdminAuthorization( client, server );
//      System.out.println("[] stop request from "+client.getHostAddress() );
//      stop();
//  }
//
//  public void stop( InetAddress client ) throws SecurityException {
//      System.out.println("[] stop request from "+client.getHostAddress() );
//      stop();
//  }
//  public synchronized void stop() {
//      System.out.println("[] sending stop signal");
//      stop = true;
//      try{
//          this.notifyAll();
//      } catch (Throwable t){
//          logger.error("Unable to notify the server thread to stop. Received exception: "+t.getClass().getName()+" : "+t.getMessage());
//      }
//  }
}

    


