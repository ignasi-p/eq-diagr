package lib.huvud;

/**  A collection of static methods used by the libraries and
 * by the software "Chemical Equilibrium Diagrams".
 *
 * Copyright (C) 2014 I.Puigdomenech.
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
public class Div {
  private static final String SLASH = java.io.File.separator;
  private static final String nl = System.getProperty("line.separator");

  //<editor-fold defaultstate="collapsed" desc="getFileNameWithoutExtension">
  /**
   * @param fileName a file name with or without the directory path
   * @return the argument (<code>fileName</code>) without the extension.
   * If the file name corresponds to an existing directory,
   * or if there is no extension, <code>fileName</code> is returned.
   * It returns <code>null</code> if the file name is <code>null</code>.
   * It returns "" if the file name is exactly ".".
   */
  public static String getFileNameWithoutExtension(String fileName) {
    if(fileName == null || fileName.length() <= 0) {return fileName;}
    if(fileName.length() == 1) {
        if(fileName.equals(".")) {return "";} else {return fileName;}
    }
    java.io.File f = new java.io.File(fileName);
    if(f.isDirectory()) {return fileName;}
    int dot = fileName.lastIndexOf(".");
    if(dot <= 0) {return fileName;} //no extension in name
    // check that the last dot is in the name part, and not in the path (as in C:/dir.1/fileName)
    String ext;
    if(dot <= fileName.length()) {ext = fileName.substring(dot+1);} else {ext = "";}
    if(ext.contains(SLASH)) {return fileName;} else {return fileName.substring(0,dot);}
  } // getFileNameWithoutExtension(fileName)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="getFileNameExtension">
  /** Returns the extension (without a ".").
   * It returns <code>null</code> if the file name is <code>null</code>.
   * It returns "" if:<br>
   * .- the file name corresponds to an existing directory<br>
   * .- there is no "." in the file name<br>
   * .- the file name ends with "."<br>
   * .- there is no "." after the last slash (java.io.File.separator)<br>
   * @param fileName
   * @return   */
  public static String getFileNameExtension(String fileName) {
    java.io.File f = new java.io.File(fileName);
    if(f.isDirectory()) {return "";}
    if(fileName == null) {return fileName;}
    if(fileName.length() <= 1 || fileName.endsWith(".")) {return "";}
    int dot = fileName.lastIndexOf(".");
    if(dot <= 0) {return "";} //no extension
    // check that the last dot is in the name part, and not in the path (as in C:/dir.1/fileName)
    String ext;
    if(dot <= fileName.length()) {ext = fileName.substring(dot+1);} else {ext = "";}
    if(ext.contains(SLASH)) {return "";} else {return ext;}
  } // getFileNameExtension(fileName)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="myLogger(pattern, progName)">
  public static java.util.logging.Logger myLogger(final String pattern, final String progName) {
    if(pattern == null || pattern.length() <=0 || progName == null || progName.length() <=0) {
        return null;
    }
    java.util.logging.Logger lggr;
    try {
        int limit = 1000000; // 1 Mb
        int numLogFiles = 2;
        // %h = user.home;  %g = generation number for rotated logs
        // %u = unique number to resolve conflicts
        java.util.logging.FileHandler fh =
            new java.util.logging.FileHandler(pattern,limit,numLogFiles);
        fh.setFormatter(
            new java.util.logging.Formatter() {@Override
                public String format(java.util.logging.LogRecord rec) {
                    StringBuilder buf = new StringBuilder(1000);
                    //buf.append(new java.util.Date()+" ");
                    //buf.append(rec.getLevel()+" ");
                    buf.append(formatMessage(rec));
                    //buf.append(nl);
                    return buf.toString();
                } //format(rec)
        }
        ); // setFormatter
        lggr = java.util.logging.Logger.getLogger(progName);
        lggr.addHandler(fh);
        //To suppress the logging output to the console :
        java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
        java.util.logging.Handler[] handlers = rootLogger.getHandlers();
        if(handlers[0] instanceof java.util.logging.ConsoleHandler) {
            rootLogger.removeHandler(handlers[0]);
        }
    } // try
    catch (java.io.IOException e) {
        e.printStackTrace();
        lggr = null;
    }
    return lggr;
  } //myLogger
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="progExists, progSEDexists, progPredomExists">
  /**
   * @param dir a directory or a full-path file name
   * @return true if either of the files "SED.jar" or "SED.exe" (under Windows)
   * exist in "dir", or if "dir" ends with either of these two file names. False otherwise. */
  public static boolean progSEDexists(java.io.File dir) {
    if(dir == null || !dir.exists()) {return false;}
    if(progExists(dir, "SED","jar")) {return true;}
    if(System.getProperty("os.name").toLowerCase().startsWith("windows")) {
         if(progExists(dir, "SED","exe")) {return true;}
    }
    return false;
  }
  /**
   * @param dir a directory or a full-path file name
   * @return true if either of the files "Predom.jar", "Predom.exe" (on Windows), or
   * "Predom2.exe" (on Windows) exist in "dir",
   * or if "dir" ends with either of these three file names. False otherwise. */
  public static boolean progPredomExists(java.io.File dir) {
    if(dir == null || !dir.exists()) {return false;}
    if(progExists(dir, "Predom","jar")) {return true;}
    if(System.getProperty("os.name").toLowerCase().startsWith("windows")) {
         if(progExists(dir, "Predom","exe") || progExists(dir, "Predom2","exe")) {return true;}
    }
    return false;
  }
  /**
   * @param dir a directory or a full-path file name
   * @param prog a file name without extension
   * @param ext an extension, with or without initial ".". For example "jar".
   * @return true if "dir/prog.ext" exists, or if "dir" ends with "prog.ext" and it exists  */
  public static boolean progExists(java.io.File dir, String prog, String ext) {
    if(dir == null || prog == null || ext == null) {return false;}
    if(!dir.exists()) {return false;}
    if(!ext.startsWith(".")) {ext = "."+ext;}
    String name;
    if(dir.isDirectory()) {
      String path = dir.getAbsolutePath();
      if(path.endsWith(SLASH)) {path = path.substring(0,path.length()-1);}
      name = path + SLASH + prog + ext;
      java.io.File f = new java.io.File(name);
      if(f.exists() && f.isFile()) {return true;}
    } else { //"dir" is a file
      name = dir.getName();
      if(name.equalsIgnoreCase(prog + ext)) {return true;}
    }
    return false;
  } //progExists
  //</editor-fold>

  //</editor-fold>

}
