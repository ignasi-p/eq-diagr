package chemicalDiagramsHelp;

/** Class to store "configuration" information about the DataBase and Spana programs.
 * These data are stored in the configuration file.
 * The class is used to retrieve data in diverse methods
 *
 * Copyright (C) 2015-2020 I.Puigdomenech.
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
  /** true if debug printout is to be made. Messages and errors are directed
   * to a message frame */
  public boolean dbg = false;
  /**  <code>true</code> if the ini-file is to be saved (or read) in the
   * applications path only, for example if running from a USB-memory
   * (or a portable drive).  Note: if the application directory is
   * write-protected, then if this parameter is <code>true</code> an ini-file
   * will NOT be written, while if <code>false</code> the ini-file will be written
   * elsewhere (for example in the user's home directory).*/
  public boolean saveIniFileToApplicationPathOnly = false;
  private static final String nl = System.getProperty("line.separator");

  public ProgramConf() {dbg = false; saveIniFileToApplicationPathOnly = false;}
  public ProgramConf(String pgName) {
      progName = pgName;
      dbg = false;
      saveIniFileToApplicationPathOnly = false;
  }

  //<editor-fold defaultstate="collapsed" desc="Cfg-file">
 /** Read program options (configuration file)<br>
  * Exceptions are reported only to the console:
  * The program's functionality is not affected if this method fails
  * @param fileNameCfg
  * @param pc  */
  public static void read_cfgFile(java.io.File fileNameCfg, ProgramConf pc) {
    if(fileNameCfg == null) {HelpWindow.ErrMsg("Error: fileNameCfg = null in routine \"read_cfgFile\""); return;}
    if(pc == null) {HelpWindow.ErrMsg("Error: pc = null in routine \"read_cfgFile\""); return;}
    java.util.Properties cfg = new java.util.Properties();
    java.io.FileInputStream fis = null;
    java.io.BufferedReader r = null;
    boolean loadedOK = false;
    try {
      fis = new java.io.FileInputStream(fileNameCfg);
      r = new java.io.BufferedReader(new java.io.InputStreamReader(fis,"UTF8"));
      cfg.load(r);
      loadedOK = true;
    } //try
    catch (java.io.FileNotFoundException e) {
      HelpWindow.OutMsg("Warning: file Not found: \""+fileNameCfg.getPath()+"\""+nl+
                        "   using default program options.");
      write_cfgFile(fileNameCfg, pc);
      return;
    } //catch FileNotFoundException
    catch (java.io.IOException e) {
      HelpWindow.ErrMsg(e.getMessage()+nl+
            "   while loading config.-file:"+nl+
            "   \""+fileNameCfg.getPath()+"\"");
      loadedOK = false;
    } // catch Exception
    try {if(r != null) {r.close();} if(fis != null) {fis.close();}}
    catch (java.io.IOException e) {
            HelpWindow.ErrMsg(e.getMessage()+nl+
                  "   while closing config.-file:"+nl+
                  "   \""+fileNameCfg.getPath()+"\"");
    } // catch
    if(loadedOK) {
        try {
            HelpWindow.OutMsg("Reading file: \""+fileNameCfg.getPath()+"\"");
            if(cfg.getProperty("Debug") != null &&
               cfg.getProperty("Debug").equalsIgnoreCase("true")) {
                pc.dbg = true;
            }
            pc.saveIniFileToApplicationPathOnly = cfg.getProperty("SaveIniFileToApplicationPathOnly") != null &&
                    cfg.getProperty("SaveIniFileToApplicationPathOnly").equalsIgnoreCase("true");
        }
        catch (Exception ex) {
          HelpWindow.ErrMsg(ex.getMessage()+nl+
                  "   while reading file: \""+fileNameCfg.getPath()+"\"");
          write_cfgFile(fileNameCfg, pc);
        }
    } // if (loadedOK)
  } // read_cfgFile()

 /** Write program options (configuration file).<br>
  * Exceptions are reported to the console. */
  private static void write_cfgFile(java.io.File fileNameCfg, ProgramConf pc) {
        java.io.FileOutputStream fos = null;
        java.io.Writer w = null;
        try {
            fos = new java.io.FileOutputStream(fileNameCfg);
            w = new java.io.BufferedWriter(new java.io.OutputStreamWriter(fos,"UTF8"));
        }
        catch (java.io.IOException e) {
            HelpWindow.ErrMsg(e.getMessage()+nl+
                  "   trying to write config.-file: \""+fileNameCfg.toString()+"\"");
            try{if(w != null) {w.close();} if(fos != null) {fos.close();}}
            catch (Exception e1) {
                HelpWindow.ErrMsg(e1.getMessage()+nl+
                  "   trying to close config.-file: \""+fileNameCfg.toString()+"\"");
            }
            return;
        } //catch
        try{
        w.write(
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
                "#  be writen in a sub-folder named \".config\\eq-diagr\"."+nl);
        if(pc.saveIniFileToApplicationPathOnly) {
            w.write("SaveIniFileToApplicationPathOnly=true"+nl);
        } else {
            w.write("SaveIniFileToApplicationPathOnly=false"+nl);
        }
        w.write(
                "# Change next to \"true\" to output debugging information"+nl+
                "#   to the messages window."+nl);
        if(pc.dbg) {w.write("Debug=true"+nl);}
        else {w.write("Debug=false"+nl);}
        w.close(); fos.close();
        if(pc.dbg) {HelpWindow.OutMsg("Written: \""+fileNameCfg.toString()+"\"");}
        }
        catch (Exception ex) {
                HelpWindow.ErrMsg(ex.getMessage()+nl+
                  "   trying to write config.-file: \""+fileNameCfg.toString()+"\"");
        }
  } // write_cfgFile()
  //</editor-fold>

} 