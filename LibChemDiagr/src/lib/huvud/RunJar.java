package lib.huvud;

import lib.common.MsgExceptn;
import lib.common.Util;

/** Has two methods: "runJarLoadingFile" and "jarDone"
 * <br>
 * Copyright (C) 2014-2016 I.Puigdomenech.
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
 * @see RunJar#runJarLoadingFile(java.awt.Component, java.lang.String, java.lang.String[], boolean, java.lang.String) runJarLoadingFile
 * @see RunJar#jarDone() jarDone
 * @see RunProgr#runProgramInProcess(java.awt.Component, java.lang.String, java.lang.String[], boolean, boolean, java.lang.String) runProgramInProcess
 * @author Ignasi Puigdomenech */
public class RunJar {
  /** A SwingWorker to perform tasks in the background. It invokes the "main"-method
   * of a jar-file, with an array of arguments. It is like typing<<pre>
   * java -jar file.jar arg1 arg2 arg3</pre>
   * but as a background task within this java virtual machine. */
  private WorkTask tsk;
  private static final String SLASH = java.io.File.separator;
  private static final String nl = System.getProperty("line.separator");
  private JarClassLoader jcl;
  private String jarFileName;
  private String[] argsCopy;
  private String mainClassName;
  private boolean dbg;
  private boolean finished = false;

  //<editor-fold defaultstate="collapsed" desc="runJarLoadingFile">
 /** Loads a jar file, which may be local or remote, and executes its "main" method.
  * This method waits while the daughter application runs in the background
  * using a SwingWorker.
  * @param parent The calling frame, used to link error-message boxes.
  * The cursor (wait or default) is left un-modified.
  * @param jar the name of the jar-file whose "main" method is to be executed
  * @param args arguments to the "main" method of the jar-file
  * @param debug true to output debug information
  * @param path the path of the application calling this procedure.
  * If no path is given in <code>prgm</code> (the name of the program or jar file to run)
  * then the application's path is used.
  * @see #jarDone() jarDone
  * @see RunProgr#runProgramInProcess(java.awt.Component, java.lang.String, java.lang.String[], boolean, boolean, java.lang.String) runProgramInProcess
  */
  public void runJarLoadingFile(final java.awt.Component parent,
          final String jar,
          final String[] args,
          final boolean debug,
          String path) {
    //--- check the jar file
    if(jar == null || jar.trim().length() <=0) {
            MsgExceptn.exception("Programming error detected in \"runJarLoadingFile(jar)\""+nl+
                    "   jar-file name is either \"null\" or empty.");
            return;}
    if(!jar.toLowerCase().endsWith(".jar")) {
            MsgExceptn.exception("Programming error detected in"+nl+
                    "   \"runJarLoadingFile("+jar+")"+nl+
                    "   The name does not end with \".jar\"");
            return;}
    dbg = debug;
    if(args == null) {
        argsCopy = new String[0];
    } else {
        java.util.List<String> commandArgs = new java.util.ArrayList<String>();
        //System.arraycopy( args, 0, argsCopy, 0, args.length );
        //for(int i=0; i<args.length; i++) {
        for (String a1 : args) {
            if(a1 != null && a1.length()>0) {
                // -- remove enclosing quotes
                if(a1.length() >2 && a1.startsWith("\"") && a1.endsWith("\"")) {
                    a1 = a1.substring(1, a1.length()-1);
                }
                commandArgs.add(a1);
            } //if a1
        } //for i
        argsCopy = new String[commandArgs.size()];
        argsCopy = commandArgs.toArray(argsCopy);
    }
    if(dbg) {System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - -"+nl+
        "Loading jar file: \""+jar+"\" with arguments:");
        for (String argsCopy1 : argsCopy) {System.out.println("   " + argsCopy1);}
        System.out.flush();
    }
    if(jar.contains(SLASH) || path == null || path.length() <=0) {
      jarFileName = jar;
    } else {
      if(path.endsWith(SLASH)) {path = path.substring(0,path.length()-1);}
      jarFileName = path + SLASH + jar;
    }
    final java.io.File jarFile = new java.io.File(jarFileName);
    if(!jarFile.exists()) {
        String msg = "Error: file Not found:"+nl+
                    "   \""+jarFile.getAbsolutePath()+"\""+nl+
                    "   Can not \"run\" jar-file.";
        MsgExceptn.showErrMsg(parent,msg,1);
        return;
    }
    if(dbg) {System.out.println("jar file: "+jarFile.getAbsolutePath());}
    // --- jar ok, invoke the "main" method
    //if needed for debugging: sleep some milliseconds (wait)
    //try {Thread.sleep(3000);} catch(Exception ex) {}
    java.net.URL url;
    try {url = jarFile.toURI().toURL();}
    catch (java.net.MalformedURLException ex) {
        String msg = "Error: \""+ex.toString()+"\""+nl+
                    "   while attempting to access file:"+nl+
                    "   \""+jarFileName+"\"";
        MsgExceptn.showErrMsg(parent,msg,1);
        return;
    }
    // Create the class loader for the jar file
    jcl = new JarClassLoader(url);
    // Get the application's main class name
    try {mainClassName = jcl.getMainClassName();}
    catch (java.io.IOException ex) {
        String msg = "Error: \""+ex.toString()+"\""+nl+
               "   while attempting to open file:"+nl+
               "   \""+jarFileName+"\"";
        MsgExceptn.showErrMsg(parent,msg,1);
        return;
    }
    if(mainClassName == null) {
    String msg = "Error: the 'Main-Class' manifest attribute"+nl+
                "   is Not found in jar file:"+nl+
                "   \""+jarFileName+"\"";
        MsgExceptn.showErrMsg(parent,msg,1);
        return;
    }

    // Invoke application's main class
    finished = false;
    tsk = new WorkTask();
    tsk.execute(); // this returns inmediately
    // but the SwingWorker proceeds...

    waitForTask(); // wait for the SwingWorker to end
    /* An alternative method to to execute code <i>after</i> the daughter
     * application has finished, do as follows:<pre>
     *   class MyClass
     *     private javax.swing.Timer tmr;
     *     ...
     *     void myMethod() {
     *         final RunJar rj = new RunJar();
     *         rj.runJarLoadingFile(this, "test.jar", null, true,"D:\\myPath\\dist");
     *         tmr = new javax.swing.Timer(500, new java.awt.event.ActionListener() {
     *              public void actionPerformed(java.awt.event.ActionEvent e) {
     *                  if(rj.jarDone()) {
     *                      if(tmr != null) {tmr.stop();}
     *                      // put here tasks to run after the daughter application has finished
     *                     javax.swing.JOptionPane.showMessageDialog(this, "Finished!");
     *                  }}});
     *         tmr.start();
     *     }</pre> 
     */

    if(dbg) {System.out.println("--- RunJar returns..."); System.out.flush();}
  } // runJarLoadingFile
  // </editor-fold>

  private synchronized void waitForTask() {
    while(!finished) { try {wait();} catch (InterruptedException ex) {} }
  }
  private synchronized void notify_All() {notifyAll();}

 /** Returns true if the SwingWorker running the jar-file is done, or if it is cancelled
  * @return true if the SwingWorker running the jar-file is done, or if it is cancelled;
  * false otherwise
  * @see #runJarLoadingFile(java.awt.Component, java.lang.String, java.lang.String[], boolean, java.lang.String)  runJarLoadingFile
  * @see RunProgr#runProgramInProcess(java.awt.Component, java.lang.String, java.lang.String[], boolean, boolean, java.lang.String) runProgramInProcess
  */
  public boolean jarDone() {
    if(tsk == null) {return true;}
    return (tsk.isDone() || tsk.isCancelled());
  }

  //<editor-fold defaultstate="collapsed" desc="class WorkTask">
/** A SwingWorker to perform tasks in the background.
 * @see WorkTask#doInBackground() doInBackground() */
private class WorkTask extends javax.swing.SwingWorker<Boolean, String> {
  /** The instructions to be executed are defined here: it invokes the "main"-method
   * of a jar-file, with an array of arguments. It is like
   * typing<<pre>
   * java -jar file.jar arg1 arg2 arg3
   * </pre>but in this java virtual machine as a background task.
   * @return true if no error occurs, false otherwise
   * @throws Exception */
  @Override protected Boolean doInBackground() throws Exception {
    boolean ok;
    String msg = null;
    try {
        jcl.invokeClass(mainClassName, argsCopy);
        ok = true;
    }
    catch (ClassNotFoundException e) {
        msg = "Error: Class not found: \"" + mainClassName +"\""+nl+
                "   in jar file:"+nl+
                "   \""+jarFileName+"\"";
        ok = false;
    }
    catch (NoSuchMethodException e) {
        msg = "Error: Class \"" + mainClassName +"\""+nl+
                    "   in jar file:"+nl+
                    "   \""+jarFileName+"\""+nl+
                    "   does not define a 'main' method";
        ok = false;
    }
    catch (java.lang.reflect.InvocationTargetException e) {
        msg = "Error: \"" + e.getTargetException().toString() +"\""+nl+
                "   in class \""+mainClassName+"\""+nl+
                "   in jar file:"+nl+
                "   \""+jarFileName+"\"";
        msg = msg + nl + Util.stack2string(e);
        ok = false;
    }
    if(msg != null) {System.out.println(msg); System.out.flush();}
    return ok;
  } //doInBackground()

  /** It wakes up the waitng thread and it prints a message if in debug "mode". */
  @Override protected void done() {
    if(dbg) {
        if(isCancelled()) {
            System.out.println("SwingWorker cancelled.");
        } else {
            System.out.println("SwingWorker done.");
        }
        System.out.flush();
    }
    finished = true;
    notify_All();
  }
/* There is no "publish()" in doInBackground(), so this is not needed.
  @Override protected void process(java.util.List<String> chunks) {
      // Here we receive the values that we publish().
      // They may come grouped in chunks.
      //jLabelCount.setText(chunks.get(chunks.size()-1));
      //System.out.println(chunks.get(chunks.size()-1));
  } */
} //class WorkTask
  // </editor-fold>

}