package lib.huvud;

import lib.common.MsgExceptn;
import lib.common.Util;

/** Has one method: "runProgramInProcess". It uses a ProcessBuilder to run
 * the process.
 * <br>
 * Copyright (C) 2014-2020 I.Puigdomenech.
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
 * @see RunProgr#runProgramInProcess(java.awt.Component, java.lang.String, java.lang.String[], boolean, boolean, java.lang.String) runProgramInProcess
 * @see RunJar#runJarLoadingFile(java.awt.Component, java.lang.String, java.lang.String[], boolean, java.lang.String) runJarLoadingFile
 * @author Ignasi Puigdomenech */
public class RunProgr {
  private static final String nl = System.getProperty("line.separator");
  private static final String SLASH = java.io.File.separator;

  //<editor-fold defaultstate="collapsed" desc="runProgramInProcess">
  /** Starts a system process to execute either a program or
   * the "main" method in a jar-file. Error messages are printed
   * to "System.err".
   * @param parent The calling frame, used to link error-message boxes.
   *               The cursor (wait or default) is left unmodified.
   *               May be "null".
   * @param prgm the name of the program or jar file to run. It can include
   *             a full path. If no path is specified, the directory given in the
   *             <code>path</code> variable is assumed.
   * @param a array with the command-line parameters. If needed the user must
   *          enclose individual parameters in quotes.
   * @param waitForCompletion if true the method will wait for the
   *                          execution of the program to finish
   * @param dbg if true debug information will be written to "System.out"
   * @param path the working directory.
   *        If no path is given in <code>prgm</code> (the name of the program
   *        or jar file to run) then this path is used.
   * @return false if an error occurs
   * @see RunJar#runJarLoadingFile(java.awt.Component, java.lang.String, java.lang.String[], boolean, java.lang.String) runJarLoadingFile
   */
    public static boolean runProgramInProcess(final java.awt.Component parent,
            String prgm,
            String[] a,
            final boolean waitForCompletion,
            final boolean dbg,
            String path) {
        if(prgm == null || prgm.trim().length() <=0) {
            String msg = "Programming error detected in \"runProgramInProcess\""+nl;
            if(prgm == null) {
                msg = msg + "   program or jar file name is \"null\".";
            } else {msg = msg + "   program or jar file name is empty.";}
            MsgExceptn.showErrMsg(parent,msg,1); 
            return false;
        }
        if(dbg) {
            System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - -"+nl+
                    "runProgramInProcess:"+nl+"  "+prgm);
            if(a.length>0) {
                for(int i=0; i < a.length; i++) {System.out.print("  "+a[i]);}
                System.out.println();
            }
            System.out.println("  waitForCompletion = "+waitForCompletion+",  debug = "+dbg);
            if(path != null && path.trim().length() >0) {
                System.out.println("  path = "+path);
            } else {System.out.println("  path = (not given)");}
            System.out.flush();            
        }
        //remove enclosing quotes
        if(prgm.length() >2 && prgm.startsWith("\"") && prgm.endsWith("\"")) {
            prgm = prgm.substring(1, prgm.length()-1);
        }
        final String prgmFileName;
        if(!prgm.contains(SLASH) && path != null && path.trim().length() >0) {
            String dir;
            if(path.endsWith(SLASH)) {dir = path.substring(0,path.length()-1);} else {dir = path;}
            prgmFileName = dir + SLASH + prgm;
        } else {prgmFileName = prgm;}

        if(!(new java.io.File(prgmFileName).exists())) {
            MsgExceptn.showErrMsg(parent,"Error:  Program or jar file:"+nl+
                "       "+prgmFileName+nl+
                "   does NOT exist.",1); 
            return false;
        }

        // create the operating-system command to run a program or jar-file
        java.util.List<String> command = new java.util.ArrayList<String>();
        if(prgmFileName.toLowerCase().endsWith(".jar")) {
                command.add("java");
                command.add("-jar");
        } else if(prgmFileName.toLowerCase().endsWith(".app")
                && System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
                command.add("/usr/bin/open");
                command.add("-n");
        }
        command.add(prgmFileName);
        if(a == null) {a = new String[0];}
        if(a.length>0) {
                for (String a1 : a) {
                    if (a1 != null && a1.length() > 0) {
                        //remove enclosing quotes: spaces will be taken care by ProcessBuilder
                        if(a1.length() >2 && a1.startsWith("\"") && a1.endsWith("\"")) {
                            a1 = a1.substring(1, a1.length()-1);
                        }
                        command.add(a1);
                    }
                }
        }
        if(dbg) {
            System.out.println("starting process:");
            if(command.size()>0) { // it should be >0...
                for(int i=0; i < command.size(); i++) {System.out.println("   "+command.get(i));}
            }
            System.out.flush();
        }
        ProcessBuilder builder = new ProcessBuilder(command);
        //java.util.Map<String, String> environ = builder.environment();

        if(path != null && path.trim().length()>0) {
                if(dbg) {
                    System.out.println("   --- setting working directory to: "+path);
                    System.out.flush();
                }
                builder = builder.directory(new java.io.File(path));
        }
        System.out.flush();
        if(!waitForCompletion) {builder = builder.redirectErrorStream(true);}
        try { // Start the program.
            if(dbg) {System.out.println("   --- starting the process..."); System.out.flush();}
            final Process p = builder.start();
            if(waitForCompletion) {
                if(dbg) {System.out.println("   --- waiting for process to terminate...");}
                // Get the output
                java.io.BufferedReader stdInput = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getInputStream()));
                java.io.BufferedReader stdError = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getErrorStream()));
                String s;
                while ((s = stdInput.readLine()) != null) {System.out.println(s);}
                while ((s = stdError.readLine()) != null) {System.err.println(s);}
                //if needed for debugging: sleep some milliseconds (wait)
                //try {Thread.sleep(3000);} catch(Exception ex) {}
                p.waitFor();
            } else {
                // Get the output
                new Thread(new StreamEater(p.getInputStream())).start();
            }
            if(dbg) {System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - -");}
        }
        catch (InterruptedException e) {
            MsgExceptn.showErrMsg(parent,Util.stack2string(e),1);
            return false;}
        catch (java.io.IOException e) {
            MsgExceptn.showErrMsg(parent,Util.stack2string(e),1);
            return false;}
        return true;
    } // runProgramInProcess

  //<editor-fold defaultstate="collapsed" desc="private StreamEater">
  private static class StreamEater implements Runnable {
    private final java.io.InputStream stream;
    StreamEater(java.io.InputStream stream) {this.stream = stream;}
    @Override public void run() {
        byte[] buf = new byte[32];
        try{while (stream.read(buf) != -1) {}} catch (java.io.IOException e) {e.printStackTrace();}
    }
  }
  // </editor-fold>

  // </editor-fold>

}
