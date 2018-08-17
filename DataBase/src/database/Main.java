package database;

/** Checks that all the jar-libraries needed exist.
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
 * @author Ignasi Puigdomenech */
public class Main {
  private static final String progName = "Program \"DataBase\""; //Leta? Lagra? Samla?
  /** New-line character(s) to substitute "\n" */
  private static final String nl = System.getProperty("line.separator");
  private static boolean started = false;
  private static final String SLASH = java.io.File.separator;

  /** Check that all the jar-libraries needed do exist.
   * @param args the command line arguments */
  public static void main(String[] args) {
    // ---- are all jar files needed there?
    if(!doJarFilesExist()) {return;}

    // ---- ok!
    FrameDBmain.main(args);

  } //main

//<editor-fold defaultstate="collapsed" desc="doJarFilesExist">
/** Look in the running jar file's classPath Manifest for any other "library"
 * jar-files listed under "Class-path".
 * If any of these jar files does not exist display an error message
 * (and an error Frame) and continue.
 * @return true if all needed jar files exist; false otherwise.
 * @version 2016-Aug-03 */
  private static boolean doJarFilesExist() {
    java.io.File libJarFile, libPathJarFile;
    java.util.jar.JarFile runningJar = getRunningJarFile();
    // runningJar.getName() = C:\Eq-Calc_Java\dist\Prog.jar
    if(runningJar != null) { // if running within Netbeans there will be no jar-file
        java.util.jar.Manifest manifest;
        try {manifest = runningJar.getManifest();}
        catch (java.io.IOException ex) {
            manifest = null;
            String msg = "Warning: no manifest found in the application's jar file:"+nl+
                    "\""+runningJar.getName()+"\"";
            ErrMsgBox emb = new ErrMsgBox(msg, progName);
            //this will return true;
        }
        if(manifest != null) {
            String classPath = manifest.getMainAttributes().getValue("Class-Path");
            if(classPath != null && classPath.length() > 0) {
                // this will be a list of space-separated names
                String[] jars = classPath.split("\\s+"); //regular expression to match one or more spaces
                if(jars.length >0) {
                    java.io.File[] rootNames = java.io.File.listRoots();
                    boolean isPathAbsolute;
                    String pathJar;
                    String p = getPathApp();  // get the application's path
                    for(String jar : jars) {  // loop through all jars needed
                        libJarFile = new java.io.File(jar);
                        if(libJarFile.exists()) {continue;}
                        isPathAbsolute = false;
                        for(java.io.File f : rootNames) {
                            if(jar.toLowerCase().startsWith(f.getAbsolutePath().toLowerCase())) {
                                isPathAbsolute = true;
                                break;}
                        }
                        if(!isPathAbsolute) { // add the application's path
                            if(!p.endsWith(SLASH) && !jar.startsWith(SLASH)) {p = p+SLASH;}
                            pathJar = p + jar;
                        } else {pathJar = jar;}
                        libPathJarFile = new java.io.File(pathJar);
                        if(libPathJarFile.exists()) {continue;}
                        libPathJarFile = new java.io.File(libPathJarFile.getAbsolutePath());
                        ErrMsgBox emb = new ErrMsgBox(progName+" - Error:"+nl+
                                "   File: \""+jar+"\"  NOT found."+nl+
                                "   And file: \""+libPathJarFile.getName()+"\" is NOT in folder:"+nl+
                                "         \""+libPathJarFile.getParent()+"\""+nl+
                                "   either!"+nl+nl+
                                "   This file is needed by the program."+nl, progName);
                        return false;
                    }
                }
            }//if classPath != null
        } //if Manifest != null
    } //if runningJar != null
    return true;
  } //doJarFilesExist()

//<editor-fold defaultstate="collapsed" desc="getRunningJarFile()">
/** Find out the jar file that contains this class
 * @return a File object of the jar file containing the enclosing class "Main",
 * or null if the main class is not inside a jar file.
 * @version 2014-Jan-17 */
public static java.util.jar.JarFile getRunningJarFile() {
  //from http://www.rgagnon.com/javadetails/
  //and the JarClassLoader class
  C c = new C();
  String className = c.getClass().getName().replace('.', '/');
  // class = "progPackage.Main";   className = "progPackage/Main"
  java.net.URL url = c.getClass().getResource("/" + className + ".class");
  // url = "jar:file:/C:/Eq-Calc_Java/dist/Prog.jar!/progPackage/Main.class"
  if(url.toString().startsWith("jar:")) {
      java.net.JarURLConnection jUrlC;
      try{
        jUrlC = (java.net.JarURLConnection)url.openConnection();
        return jUrlC.getJarFile();
      } catch(java.io.IOException ex) {
        ErrMsgBox emb = new ErrMsgBox("Error "+ex.toString(), progName);
        return null;
      }
  } else {
        // it might not be a jar file if running within NetBeans
      return null;
  }
} //getRunningJarFile()
//</editor-fold>

//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="getPathApp">
/** Get the path where an application is located.
 * @return the directory where the application is located,
 * or "user.dir" if an error occurs
 * @version 2014-Jan-17 */
  private static class C {private static void C(){}}
  public static String getPathApp() {
    C c = new C();
    String path;
    java.net.URI dir;
    try{
        dir = c.getClass().
                getProtectionDomain().
                    getCodeSource().
                        getLocation().
                            toURI();
        if(dir != null) {
            String d = dir.toString();
            if(d.startsWith("jar:") && d.endsWith("!/")) {
                d = d.substring(4, d.length()-2);
                dir = new java.net.URI(d);
            }
            path = (new java.io.File(dir.getPath())).getParent();
        } else {path = System.getProperty("user.dir");}
    }
    catch (java.net.URISyntaxException e) {
      if(!started) {
        ErrMsgBox emb = new ErrMsgBox("Error: "+e.toString()+nl+
                    "   trying to get the application's directory.", progName);
      }
      path = System.getProperty("user.dir");
    } // catch
    started = true;
    return path;
  } //getPathApp()
//</editor-fold>

}
