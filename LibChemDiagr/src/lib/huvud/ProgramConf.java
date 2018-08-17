package lib.huvud;

import lib.common.MsgExceptn;

/** Class to store "configuration" information about the DataBase and Spana programs.
 * These data are stored in the configuration file.
 * The class is used to retrieve data in diverse methods
 *
 * Copyright (C) 2015-2016 I.Puigdomenech.
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
public class ProgramConf {
  /** The name of the program, either "Spana" or "DataBase" */
  public String progName = null;
  /** The path where the application (jar-file) is located */
  public String pathAPP;
  /** true if debug printout is to be made. Messages and errors are directed
   * to a message frame */
  public boolean dbg = false;
  /**  <code>true</code> if the ini-file is to be saved (or read) in the
   * applications path only, for example if running from a USB-memory
   * (or a portable drive).  Note: if the application directory is
   * write-protected, then if this parameter is <true>true</true> an ini-file
   * will NOT be written, while if <code>false</code> the ini-file will be written
   * elsewhere (for example in the user's home directory).*/
  public boolean saveIniFileToApplicationPathOnly = false;
  /** default directory for opening and writing input and ouput files.
   * Note that it may end with the file separator character, e.g. "D:\" */
  public StringBuffer pathDef = new StringBuffer();
  public static final String HELP_JAR = "Chem_Diagr_Help.jar";
  private static final String SLASH = java.io.File.separator;
  private static final String nl = System.getProperty("line.separator");

  public ProgramConf() {dbg = false; saveIniFileToApplicationPathOnly = false;}
  public ProgramConf(String pgName) {
      progName = pgName;
      dbg = false;
      saveIniFileToApplicationPathOnly = false;
  }

  //<editor-fold defaultstate="collapsed" desc="setPathDef">

  /** Set the variable "pathDef" to the user directory ("user.home", system dependent) */
  public void setPathDef() {
    String t = System.getProperty("user.home");
    setPathDef(t);
  }

  /** Set the variable "pathDef" to the path of a file name.
   * Note that "pathDef" may end with the file separator character, e.g. "D:\"
   * @param fName String with the file name */
  public void setPathDef(String fName) {
    java.io.File f = new java.io.File(fName);
    setPathDef(f);
  }

  /** Sets the variable "pathDef" to the path of a file.
   * Note that "pathDef" may end with the file separator character, e.g. "D:\"
   * @param f File */
  public void setPathDef(java.io.File f) {
    if(pathDef == null) {pathDef = new StringBuffer();}
    java.net.URI uri;
    if(f != null) {
        if(!f.getAbsolutePath().contains(SLASH)) {
            // it is a bare file name, without a path
            if(pathDef.length()>0) {return;}
        }
        try{uri = f.toURI();}
        catch (Exception ex) {uri = null;}
    } else {uri = null;}
    if(pathDef.length()>0) {pathDef.delete(0, pathDef.length());}
    if(uri != null) {
        if(f != null && f.isDirectory()) {
          pathDef.append((new java.io.File(uri.getPath())).toString());
        } else {
          pathDef.append((new java.io.File(uri.getPath())).getParent().toString());
        } //directory?
    } else { //uri = null:  set Default Path = Start Directory
        java.io.File currDir = new java.io.File("");
        try {pathDef.append(currDir.getCanonicalPath());}
        catch (java.io.IOException e) {
          try{pathDef.append(System.getProperty("user.dir"));}
          catch (Exception e1) {pathDef.append(".");}
        }
    } //uri = null
  } // setPathDef(File)

// </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Cfg-file">
 /** Read program options (configuration file)<br>
  * Exceptions are reported only to the console:
  * The program's functionality is not affected if this method fails
  * @param fileNameCfg
  * @param pc  */
  public static void read_cfgFile(java.io.File fileNameCfg, ProgramConf pc) {
    if(fileNameCfg == null) {System.err.println("Error: fileNameCfg =null in routine \"read_cfgFile\""); return;}
    if(pc == null) {System.err.println("Error: pc =null in routine \"read_cfgFile\""); return;}
    java.util.Properties cfg = new java.util.Properties();
    java.io.FileInputStream cfgFile = null;
    boolean loadedOK;
    try {
      cfgFile = new java.io.FileInputStream(fileNameCfg);
      cfg.load(cfgFile);
      loadedOK = true;
    } //try
    catch (java.io.FileNotFoundException e) {
      System.out.println("Warning: file Not found: \""+fileNameCfg.getPath()+"\""+nl+
                         "using default program options.");
      write_cfgFile(fileNameCfg, pc);
      return;
    }
    catch (java.io.IOException e) {
      MsgExceptn.msg("Error: \""+e.getMessage()+"\""+nl+
            "   while loading config.-file:"+nl+
            "   \""+fileNameCfg.getPath()+"\"");
      loadedOK = false;
    }
    try {if(cfgFile != null) {cfgFile.close();}}
    catch (java.io.IOException e) {
            MsgExceptn.msg("Error: \""+e.getMessage()+"\""+nl+
                  "   while closing config.-file:"+nl+
                  "   \""+fileNameCfg.getPath()+"\"");
    }
    if(loadedOK == true) {
        try {
            System.out.println("Reading file: \""+fileNameCfg.getPath()+"\"");
            if(cfg.getProperty("Debug") != null &&
               cfg.getProperty("Debug").equalsIgnoreCase("true")) {
                pc.dbg = true;
            }
            pc.saveIniFileToApplicationPathOnly = cfg.getProperty("SaveIniFileToApplicationPathOnly") != null &&
                    cfg.getProperty("SaveIniFileToApplicationPathOnly").equalsIgnoreCase("true");
        }
        catch (Exception ex) {
          MsgExceptn.msg("Error: \""+ex.getMessage()+"\""+nl+
                  "   while reading file: \""+fileNameCfg.getPath()+"\"");
          write_cfgFile(fileNameCfg, pc);
        }
    } // if (loadedOK)
  } // read_cfgFile()

 /** Write program options (configuration file).<br>
  * Exceptions are reported to the console. */
  private static void write_cfgFile(java.io.File fileNameCfg, ProgramConf pc) {
        java.io.PrintWriter cfgFile = null;
        try {
            cfgFile = new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter(fileNameCfg)));
        }
        catch (java.io.IOException e) {
            MsgExceptn.msg("Error: \""+e.getMessage()+"\""+nl+
                  "   trying to write config.-file: \""+fileNameCfg.toString()+"\"");
            try{if(cfgFile != null) {cfgFile.flush(); cfgFile.close();}}
            catch (Exception e1) {
                MsgExceptn.msg("Error: \""+e1.getMessage()+"\""+nl+
                  "   trying to close config.-file: \""+fileNameCfg.toString()+"\"");
            }
            return;
        } //catch
        cfgFile.println(
                "# Next parameter should be \"true\" if running from a USB-memory"+nl+
                "#  (or a portable drive): then the ini-file will ONLY be saved in"+nl+
                "#  the application directory.  If the application directory"+nl+
                "#  is write-protected, then no ini-file will be written."+nl+
                "#  If not \"true\" then the ini-file is saved in one of the"+nl+
                "#  following paths (depending on which environment variables"+nl+
                "#  are defined and if the paths are not write-protected):"+nl+
                "#     The installation directory"+nl+
                "#     %HOMEDRIVE%%HOMEPATH%"+nl+
                "#     %HOME%"+nl+
                "#     the user's home directory (system dependent)."+nl+
                "#  Except for the installation directory, the ini-file will"+nl+
                "#  be writen in a sub-folder named \".config\\eq-diagr\".");
        if(pc.saveIniFileToApplicationPathOnly) {
            cfgFile.println("SaveIniFileToApplicationPathOnly=true");
        } else {
            cfgFile.println("SaveIniFileToApplicationPathOnly=false");
        }
        cfgFile.println(
                "# Change next to \"true\" to output debugging information"+nl+
                "#   to the messages window.");
        if(pc.dbg) {cfgFile.println("Debug=true");}
        else {cfgFile.println("Debug=false");}
        cfgFile.flush(); cfgFile.close();
        if (pc.dbg) {System.out.println("Written: \""+fileNameCfg.toString()+"\"");}
  } // write_cfgFile()
  //</editor-fold>

} 