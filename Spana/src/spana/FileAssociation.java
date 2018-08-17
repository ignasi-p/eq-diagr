package spana;

/**  Windows registry: Associates a program with file extensions.
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
public class FileAssociation {
private static final String PROGNAME = "Eq-Diagr";
private static final String BACKUP = "Backup_by_"+PROGNAME;
/** New-line character(s) to substitute "\n" */
private static final String nl = System.getProperty("line.separator");

//<editor-fold defaultstate="collapsed" desc="associateFileExtension">
/**
 * Associates a file extension with an exe-file in the Windows registry
 * @param ext extension is a three letter string without a ".": must be either "plt" or "dat"
 * @param pathToExecute is full path to exe file
 * @throws IllegalAccessException
 * @throws java.lang.reflect.InvocationTargetException
 * @throws IllegalArgumentException
 * @throws java.io.IOException
 * @throws java.net.URISyntaxException
 */
public static void associateFileExtension(
        String ext,
        String pathToExecute)
        throws IllegalAccessException,
            java.lang.reflect.InvocationTargetException,
            IllegalArgumentException,
            java.io.IOException,
            java.net.URISyntaxException {
  String msg = "Error in \"associateFileExtension\""+nl;
  if(ext == null || ext.length() <=0) {throw new IllegalArgumentException(msg+
          "\"ext\" is null or empty.");}
  if(pathToExecute == null || pathToExecute.length() <=0) {throw new IllegalArgumentException(msg+
          "\"pathToExecute\" is null or empty.");}
  if(ext.contains(".")) {throw new IllegalArgumentException(msg+
          "\"ext\" = "+ext+" (must not contain a \".\")");}
  java.io.File fileToExecute = new java.io.File(pathToExecute);
  if(!fileToExecute.exists()) {throw new IllegalArgumentException(msg+
          "   \"pathToExecute\" = \""+pathToExecute+"\""+nl+"   file does not exist.");}
  String appName = PROGNAME+"_"+ext.toUpperCase()+"_File";
  String value;
  //--- Create a root entry called 'appName'
  // appName = either "PROGNAME_DAT_File" or "PROGNAME_PLT_File"
  WinRegistry.createKey("Software\\Classes\\"+appName);
  WinRegistry.writeStringValue("Software\\Classes\\"+appName, "",appName);
  WinRegistry.createKey("Software\\Classes\\"+appName+"\\shell\\open\\command");
  //Set the command line for 'appName'.
  WinRegistry.writeStringValue("Software\\Classes\\"+appName+"\\shell\\open\\command", "",
          pathToExecute+" \"%1\"");
  //Set the default icon
  String path;
  path = fileToExecute.getParent();
  java.io.File pathF = new java.io.File(path);
  try{path = pathF.getCanonicalPath();}
  catch (java.io.IOException ex) {}
  String iconFileName = null;
  if(ext.equalsIgnoreCase("dat") || ext.equalsIgnoreCase("plt")) {
      iconFileName = path + java.io.File.separator +
          "Icon_" + ext.toLowerCase().trim() + ".ico";}
  if(iconFileName != null) {
    java.io.File iconFile = new java.io.File(iconFileName);
    if(iconFile.exists()) {
      WinRegistry.createKey("Software\\Classes\\"+appName+"\\DefaultIcon");
      WinRegistry.writeStringValue("Software\\Classes\\"+appName+"\\DefaultIcon", "",
          iconFileName);
    }
  }
  //--- Create a root entry for the extension to be associated with "appName".
  final String dotExt = "."+ext;
  //Make a backup of existing association, if any
  value = WinRegistry.readString("Software\\Classes\\"+dotExt, "");
  if(value != null && value.length() > 0 && !value.equals(appName)) {
          WinRegistry.createKey("Software\\Classes\\"+dotExt+"\\"+BACKUP);
          WinRegistry.writeStringValue("Software\\Classes\\"+dotExt+"\\"+BACKUP, "", value);
  } //end of backup
  else {
      WinRegistry.createKey("Software\\Classes\\"+dotExt);
  }
  //Now create the association
  WinRegistry.writeStringValue("Software\\Classes\\"+dotExt, "", appName);

  //return;
} //associateFileExtension
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="unAssociateFileExtension">
/**  Un-associates a file extension with an exe-file in the Windows registry
 * @param ext extension is a three letter string without the "."
 * @throws IllegalAccessException
 * @throws java.lang.reflect.InvocationTargetException
 * @throws IllegalArgumentException
 * @throws java.io.IOException
 * @throws java.net.URISyntaxException
 */
public static void unAssociateFileExtension(
        String ext)
        throws IllegalAccessException, java.lang.reflect.InvocationTargetException,
        IllegalArgumentException, java.io.IOException, java.net.URISyntaxException {
  String msg = "Error in \"unAssociateFileExtension\""+nl;
  if(ext == null || ext.length() <=0) {throw new IllegalArgumentException(msg+
          "\"ext\" is null or empty.");}
  if(ext.contains(".")) {throw new IllegalArgumentException(msg+
          "\"ext\" = "+ext+" (must not contain a \".\")");}
  String appName = PROGNAME+"_"+ext.toUpperCase()+"_File";
  String value;
  java.util.List<String> lst;

  //--- Delete the "appName" key
  //   appName = either "PROGNAME_Data_File" or "PROGNAME_Plot_File"
  //- Delete the default iconFileName
  value = WinRegistry.readString("Software\\Classes\\"+appName+"\\DefaultIcon", "");
  if(value != null) { //not empty: possible to delete
    WinRegistry.deleteValue("Software\\Classes\\"+appName+"\\DefaultIcon", "");
    lst = WinRegistry.readStringSubKeys("Software\\Classes\\"+appName+"\\DefaultIcon");
    if(lst == null || lst.isEmpty()) { //empty: delete key
        WinRegistry.deleteKey("Software\\Classes\\"+appName+"\\DefaultIcon");
    }
  }
  //- Delete the command
  value = WinRegistry.readString("Software\\Classes\\"+appName+"\\shell\\open\\command", "");
  if(value != null) { //not empty: possible to delete
    WinRegistry.deleteValue("Software\\Classes\\"+appName+"\\shell\\open\\command", "");
    lst = WinRegistry.readStringSubKeys("Software\\Classes\\"+appName+"\\shell\\open\\command");
    if(lst == null || lst.isEmpty()) { //empty: delete key
      WinRegistry.deleteKey("Software\\Classes\\"+appName+"\\shell\\open\\command");
      lst = WinRegistry.readStringSubKeys("Software\\Classes\\"+appName+"\\shell\\open");
      if(lst == null || lst.isEmpty()) { //empty: delete key
        WinRegistry.deleteKey("Software\\Classes\\"+appName+"\\shell\\open");
        lst = WinRegistry.readStringSubKeys("Software\\Classes\\"+appName+"\\shell");
        if(lst == null || lst.isEmpty()) { //empty: delete key
          WinRegistry.deleteKey("Software\\Classes\\"+appName+"\\shell");
          // finally remove the key itself
          lst = WinRegistry.readStringSubKeys("Software\\Classes\\"+appName);
          if(lst == null || lst.isEmpty()) { //empty: delete key
            // an error will occur if the key does not exist
            WinRegistry. deleteKey("Software\\Classes\\"+appName);
          }
        } //deleteKey appName + \shell
      } //deleteKey appName + \shell\open
    } //deleteKey appName + \shell\open\command
  }
  //--- Delete ".ext": the entry for the extension associated with "appName".
  //First check if ".ext" it is associated to "appName"; if not do nothing
  final String dotExt = "."+ext;
  value = WinRegistry.readString("Software\\Classes\\"+dotExt, "");
  if(value != null && value.length() >0 && value.equals(appName)) {
      //".ext" is associated to the software
      WinRegistry.deleteValue("Software\\Classes\\"+dotExt, "");
      // because "readStringValues" not always works: do not delete key
      //try { // an error will occur if the key does not exist
      //    java.util.Map<String,String> res = WinRegistry.readStringValues(HKC,dotExt);
      //    lst = WinRegistry.readStringSubKeys(HKC,dotExt);
      //    if((lst == null || lst.isEmpty()) && (res == null || res.isEmpty())) { //empty: delete key
      //        WinRegistry.deleteKey(HKC, dotExt);
      //    }
      //} catch (Exception ex) {}
  } //".ext" is associated to the software
  //check if there is an association backup
  value = WinRegistry.readString("Software\\Classes\\"+dotExt+"\\"+BACKUP, "");
  if(value != null && value.length() >0) {
        //there is backup data: put the backup data into ".ext"
        WinRegistry.writeStringValue("Software\\Classes\\"+dotExt, "", value);
        //get rid of the backup
        WinRegistry.deleteValue("Software\\Classes\\"+dotExt+"\\"+BACKUP, "");
        lst = WinRegistry.readStringSubKeys("Software\\Classes\\"+dotExt+"\\"+BACKUP);
        if(lst == null || lst.isEmpty()) { //empty: delete key
            WinRegistry.deleteKey("Software\\Classes\\"+dotExt+"\\"+BACKUP);
        }
  } //backup?
  //Delete HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion
  //                             \Explorer\FileExts\.ext\OpenWithProgids
  WinRegistry.deleteValue(
          "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\" + dotExt +
          "\\OpenWithProgids", appName);
  //return;
} //unAssociateFileExtension
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="isAssociated">
/**
 * @param ext extension is a three letter string without a "."
 * @param pathToExecute is full path to exe file
 * @return true if files with extension "ext" are associated with application "pathToExecute"
 * @throws IllegalArgumentException
 * @throws java.lang.reflect.InvocationTargetException
 * @throws IllegalAccessException
 */
public static boolean isAssociated(
        String ext,
        String pathToExecute)
        throws IllegalArgumentException, java.lang.reflect.InvocationTargetException,
        IllegalAccessException {
  String msg = "Error in \"isAssociated\""+nl;
  if(ext == null || ext.length() <=0) {throw new IllegalArgumentException(msg+
          "\"ext\" is null or empty.");}
  if(pathToExecute == null || pathToExecute.length() <=0) {throw new IllegalArgumentException(msg+
          "\"pathToExecute\" is null or empty.");}
  if(ext.contains(".")) {throw new IllegalArgumentException(msg+
          "\"ext\" = "+ext+" (must not contain a \".\")");}
  String appName = PROGNAME+"_"+ext.toUpperCase()+"_File";
  String value;
  value = WinRegistry.readString("Software\\Classes\\"+appName+"\\shell\\open\\command", "");
  if(value == null || value.length() <=0) {return false;}
  if(!value.toLowerCase().startsWith(pathToExecute.toLowerCase())) {return false;}
  final String dotExt = "."+ext;
  value = WinRegistry.readString("Software\\Classes\\"+dotExt, "");
  return value != null && value.length() > 0 && value.equalsIgnoreCase(appName);
}
//</editor-fold>

}
