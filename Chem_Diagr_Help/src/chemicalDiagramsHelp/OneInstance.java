package chemicalDiagramsHelp;

/**  The method <b>findOtherInstance</b> returns true if another instance 
 * of the application is already running in another JVM.
 * <p>
 * A server socket is used. The application tries to connect to the socket.
 * If a server is not found, the socket is free and the server socket is
 * constructed.  If a server is found and a connection is made, it means that
 * the application is already running.  In that case the command line arguments
 * are sent to other instance.
 * <p>
 * Modify the code after lines containing <code>"==="</code>.
 * <p>
 * Use "getInstance()" instead of creating a new OneInstance()!
 * <p>
 * Copyright (C) 2014-2018 I.Puigdomenech.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 * 
 * @author Ignasi Puigdomenech  */
public class OneInstance {
  private static OneInstance one = null;
  private static final String knockKnock = "Ett skepp kommer lastad";
  private boolean dbg;
  private int port;
  private java.net.ServerSocket serverSocket = null;
  private java.net.Socket socket;
  private java.io.BufferedReader socketIn;
  private java.io.PrintWriter socketOut;
  private String whosThere;
  /** New-line character(s) to substitute "\n" */
  private static final String nl = System.getProperty("line.separator");

/**  Find out if another instance of the application is already running in another JVM.
 * <p>
 * A server socket is used. The application tries to connect to the socket.
 * If a server is not found, the socket is free and the server socket is
 * constructed.  If a server is found and a connection is made, it means that
 * the application is already running.  In that case the command line arguments
 * are sent to other instance.
 * <p>
 * Modify the code after lines containing <code>"==="</code>.
 * <p>
 * Use "getInstance()" instead of creating a new OneInstance()!
 * @param args the command line arguments to the main method of the application.
 * The arguments are sent to the other instance through the server socket.
 * Not used if no other instance of the application is found.
 * @param portDef a port to search for a server socket.
 * If no other application is found, at least five additional sockets are tried.
 * @param prgName the application's name, used for message and error reporting.
 * @param debg true if performance messages are wished.
 * @return true if another instance is found, false otherwise. */
  public boolean findOtherInstance(
           final String[] args,
           final int portDef,
           final String prgName,
           final boolean debg) {
    if(one != null) {
        String msg;
        if(prgName != null && prgName.trim().length() >0) {msg = prgName;} else {msg = "OneInstance";}
        if(debg) {HelpWindow.OutMsg("findOtherInstance already running.");}
        return false;
    } else {
        one = this;
    }
    dbg = debg;
    String progName;
    if(prgName != null && prgName.trim().length() >0) {progName = prgName;} else {progName = "OneInstance";}
    whosThere = progName;

    // loop though sockets numbers, if a socket is busy check if it is
    // another instance of this application
    //<editor-fold defaultstate="collapsed" desc="first loop">
    port = portDef;
    int portNbrMax = portDef + 5;
    java.net.InetAddress address = null;
    do{
        // note: testing a client socket (creating a stream socket and connecting it)
        //       would be much more time consuming
        try{
            serverSocket = new java.net.ServerSocket(port);
            if(dbg){HelpWindow.OutMsg("socket "+port+" is not used.");}
            serverSocket.close();
            serverSocket = null;
            port++;
        } catch (java.net.BindException ex) {
            if(dbg){HelpWindow.OutMsg("socket "+port+" busy. Another instance already running?");}
            if(address == null) {
                try {
                    address = java.net.InetAddress.getLocalHost();
                    if(dbg){HelpWindow.OutMsg("Local machine address: "+address.toString());}
                }
                catch (java.net.UnknownHostException e) {
                    HelpWindow.ErrMsg(e.getMessage()+"  using getLocalHost().");
                    address = null;
                } //catch
            }
            if (!isOtherInstance(address, port)) {
                //Not another instance. It means some software is using
                //  this socket port number;  continue looking...
                if(socketOut != null) {socketOut.close(); socketOut = null;}
                if(socketIn != null) {
                    try{socketIn.close();} catch (java.io.IOException ioe) {}
                    socketIn = null;
                }
                socket = null;
                port++;
            } else {
                //Another instance of this software is already running:
                //  send args[] and end the program (exit from "main")
                sendArgsToOtherInstance(args);
                return true;
            }
        } catch (java.io.IOException ex) { // there should be no error...
            HelpWindow.ErrMsg(ex.getMessage()+nl+"   while creating a stream Socket.");
            break;
        } // catch
    } while (port < portNbrMax); // do-while

    if(socketOut != null) {socketOut.close(); socketOut = null;}
    if(socketIn != null) {
        try{socketIn.close();} catch (java.io.IOException ioe) {}
        socketIn = null;
    }
    socket = null;
    //address = null;
    //</editor-fold>

    // Another instance not found: create a server socket on the first free port
    //<editor-fold defaultstate="collapsed" desc="create server socket">
    port = portDef;
    do{
        try{
            serverSocket = new java.net.ServerSocket(port);
            if(dbg){HelpWindow.OutMsg("Created server socket on port "+port);}
            break;
        } catch (java.net.BindException ex) {
            if(dbg){HelpWindow.OutMsg("socket "+port+" is busy.");}
            port++;
        }  //Socket busy
        catch (java.io.IOException ex) { // there should be no error...
            HelpWindow.ErrMsg(ex.getMessage()+nl+"   while creating a Server Socket.");
            break;
        }
    } while (port < portNbrMax); // do-while
    //</editor-fold>

    if(port == portNbrMax) {
        HelpWindow.ErrMsg("Error: could not create a server socket.");
        return false;
    }

    // A Server Socket has been created.
    // Wait for new connections on the serverSocket on a separate thread.
    // The program will continue execution simultaenously...
    //<editor-fold defaultstate="collapsed" desc="accept clients thread">
    Thread t = new Thread(){@Override public void run(){
        while(true){
            final java.net.Socket client;
            try{  // Wait until we get a client in serverSocket
                if(dbg){HelpWindow.OutMsg("waiting for a connection to serverSocket("+port+").");}
                // this will block until a connection is made:
                client = serverSocket.accept();
            }
            catch (java.net.SocketException ex) {break;}
            catch (java.io.IOException ex) {
                HelpWindow.ErrMsg(ex.getMessage()+nl+"   Accept failed on server port: "+port);
                break; //while
            }
            //Got a connection, wait for lines of text from the new connection (client)
            //<editor-fold defaultstate="collapsed" desc="deal with a client thread">
            if(dbg){HelpWindow.OutMsg("connection made to serverSocket in port "+port);}
            Thread t = new Thread(){@Override public void run(){
                String line;
                boolean clientNewInstance = false;
                boolean connected = true;
                java.io.BufferedReader in = null;
                java.io.PrintWriter out = null;
                try{
                    client.setSoTimeout(3000); // avoid socket.readLine() lock
                    in = new java.io.BufferedReader(new java.io.InputStreamReader(client.getInputStream()));
                    out = new java.io.PrintWriter(client.getOutputStream(), true);
                    if(dbg){HelpWindow.OutMsg("while (connected)");}
                    while(connected){
                        if(dbg){HelpWindow.OutMsg("(waiting for text line from client)");}
                        // wait for a line of text from the client
                        // note: client.setSoTimeout(3000) means
                        //       SocketTimeoutException after 3 sec
                        line = in.readLine();
                        if(line == null) {break;}
                        if(dbg){HelpWindow.OutMsg("got text line from client (in port "+port+"): "+line);}
                        // is this another instance asking who am I?
                        if(line.toLowerCase().equals(knockKnock.toLowerCase())) {
                            // yes: another instance calling!
                            // === add code here to bring this instance to front ===
                            // --- for HelpWindow.java:
                            if(HelpWindow.getInstance() != null) {
                                HelpWindow.getInstance().bringToFront();
                                if(dbg) {HelpWindow.OutMsg("bringToFront()");}
                            }
                            // --- for Spana:
                            //if(SpanaFrame.getInstance() != null) {SpanaFrame.getInstance().bringToFront();}
                            // --- for Database:
                            //if(FrameDBmain.getInstance() != null) {FrameDBmain.getInstance().bringToFront();}
                            // ---
                            //answer to client with program identity
                            if(dbg){HelpWindow.OutMsg("sending text line to client (at port "+port+"): "+whosThere);}
                            out.println(whosThere);
                            clientNewInstance=true;
                        } else {// line != knockKnock
                            if(clientNewInstance) {
                                // === add code here to deal with the command-line arguments ===
                                //     from the new instance sendt to this instance
                                // --- for HelpWindow.java:
                                if(HelpWindow.getInstance() != null) {
                                    HelpWindow.getInstance().bringToFront();
                                    HelpWindow.getInstance().setHelpID(line);
                                }
                                // --- for Spana:
                                //if(SpanaFrame.getInstance() != null) {SpanaFrame.getInstance().dispatchArg(line);}
                                // --- for Database:
                                //if(FrameDBmain.getInstance() != null) {FrameDBmain.getInstance().dispatchArg(line);}
                                // ---
                            } else {  //not clientNewInstance
                                if(dbg){HelpWindow.ErrMsg("Warning: got garbage in port "+port+" from another application; text line = \""+line+"\"");}
                                connected = false; // close the connection
                            }
                        } //line = knockKnock ?
                    } // while(connected)
                    out.close(); out = null;
                    in.close();  in = null;
                    client.close();
                } catch (java.io.IOException ioe) {
                    HelpWindow.ErrMsg(ioe.getMessage()+nl+"   Closing socket connection in port "+port);
                } finally {
                    if(dbg){HelpWindow.OutMsg("Connection to serverSocket("+port+") closed ");}
                    if(out != null) {out.close();}
                    try{ if(in != null) {in.close();} client.close(); }
                    catch (java.io.IOException ioe) {}
                }
            }};
            t.start(); // "start" returns inmediately without waiting.
            //</editor-fold>

            // wait for next connection....
        } // while(true)
    }};
    t.start(); // "start" returns inmediately without waiting.
    //</editor-fold>

    //-------------------------------------------------------------
    // Finished checking for another instance
    //-------------------------------------------------------------
    return false; // found no other instance
  }

    
//<editor-fold defaultstate="collapsed" desc="isOtherInstanceInSocket">
// -------------------------------
 /** Checking for another instance:<br>
  * The first instance of this program opens a server socket connection and
  * listens for other instances of this program to send messages. The
  * first instance also creates a "lock" file containing the socket number.
  * This method tries to send a message to a socket, and checks if the
  * response corresponds to that expected from the first instance of this
  * application. If the response is correct, it returns true.
  * @param port the socket number to try
  * @return <code>true</code> if another instance of this program is
  * listening at this socket number; <code>false</code> if either
  * an error occurs, no response is obtained from this socket within one sec,
  * or if the answer received is not the one expected from the first instance.
  */
  private boolean isOtherInstance(final java.net.InetAddress address, final int port) {
      if (port <= 0) {return false;}
      if(dbg){HelpWindow.OutMsg("isOtherInstance("+port+") ?");}
      // Create socket connection
      String line;
      boolean ok;
      try{
          socket = new java.net.Socket(address, port);
          socket.setSoTimeout(1000); // avoid socket.readLine() lock
          socketOut = new java.io.PrintWriter(socket.getOutputStream(), true);
          socketIn = new java.io.BufferedReader(
                  new java.io.InputStreamReader(socket.getInputStream()));
        //Send data over socket
        if(dbg){HelpWindow.OutMsg("Sending text:\""+knockKnock+"\" to port "+port);}
        socketOut.println(knockKnock);
        //Receive text from server.
            if(dbg){HelpWindow.OutMsg("Reading answer from socket "+port);}
            // note: socket.setSoTimeout(1000) means
            //       SocketTimeoutException after 1 sec
            try{
                line = socketIn.readLine();
                if(dbg){HelpWindow.OutMsg("Text received: \"" + line + "\" from port "+port);}
            } catch (java.io.IOException ex){
                line = null;
                HelpWindow.ErrMsg(ex.getMessage()+nl+"    in socket("+port+").readLine()");
            } //catch
        
        // did we get the correct answer?
        if(line != null && line.toLowerCase().startsWith(whosThere.toLowerCase())) {
            if(dbg){HelpWindow.OutMsg("isOtherInstance("+port+") = true");}
            ok = true;
        } else {
            if(dbg){HelpWindow.OutMsg("isOtherInstance("+port+") = false");}
            ok = false;
        }
      } catch (java.io.IOException ex) {
          HelpWindow.OutMsg(ex.getMessage()+", isOtherInstance("+port+") = false");
          ok = false;
      }
      return ok;
  // -------------------------------------
  } // isOtherInstance(port)
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="sendArgsToOtherInstance">
// -------------------------------
    /** Checking for another instance:<br>
     * The first instance of this program opens a server socket connection and
     * listens for other instances of this program to send messages. The
     * first instance also creates a "lock" file containing the socket number.
     * This method assumes that this is a "2nd" instance of this program
     * and it sends the command-line arguments from this instance to the
     * first instance, through the socket connetion.
     *
     * <p>The connection is closed and the program ends after sending the
     * arguments.</p>
     *
     * @param args <code>String[]</code> contains the command-line
     * arguments given to this instance of the program.
     */
    private void sendArgsToOtherInstance(String args[]) {
        if(socketOut == null) {return;}
        if(args != null && args.length >0) {
            for(String arg : args) {
                if(dbg){HelpWindow.OutMsg("sending command-line arg to other instance: \"" + arg + "\"");}
                socketOut.println(arg);
            } // for arg : args
        } // if args.length >0
        try {
            if(socketIn != null) {socketIn.close(); socketIn = null;}
            if(socketOut != null) {socketOut.close(); socketOut = null;}
            socket = null;
        } catch (java.io.IOException ex) {
            HelpWindow.OutMsg(ex.getMessage()+", while closing streams.");
        }
    // --------------------------------
    } //sendArgsToOtherInstance(args[])
// </editor-fold>

 /** Use this method to get the instance of this class to start
  * a the server socket thread, instead of constructing a new object.
  * @return an instance of this class
  * @see OneInstance#endCheckOtherInstances() endCheckOtherInstances */
  public static OneInstance getInstance() {return one;}

 /** Stops the server socket thread. */
  public static void endCheckOtherInstances() {
    if(one != null) {
        try{one.serverSocket.close(); one.serverSocket = null; one = null;} catch (java.io.IOException ex) {}
    }
  }
}
