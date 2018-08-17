package lib.database;

import lib.common.MsgExceptn;

/** Search reactions in the databases.
 * <br>
 * Copyright (C) 2014-2017 I.Puigdomenech.
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
public class LibSearch {
  //<editor-fold defaultstate="collapsed" desc="private fields">
  private java.util.ArrayList<String> localDBlist;
  /** counter: the database being read */
  private int db;
  /** name of the database being read */
  private String complxFileName;
  /** a counter indicating how many reactions have been read so far */
  private long cmplxNbr;
  /** the binary database being read */
  private java.io.DataInputStream dis;
  /** the text database being read */
  private java.io.BufferedReader br;
  /** if <code>binaryOrText</code> = 2 reading a binary database<br>
   * if <code>binaryOrText</code> = 1 reading text database<br>
   * if <code>binaryOrText</code> = 0 then all files are closed (e.g. when all have been read) */
  private int binaryOrText;
  /** true if the current database has been searched to the end and therefore the next
   * database must be opened (if there are any databases left to be searched) */
  private boolean openNextFile;
  /** true if no databases could be openend and searched */
  private boolean noFilesFound;
  
  /** New-line character(s) to substitute "\n" */
  private static final String nl = System.getProperty("line.separator");
  //</editor-fold>

 /** Constructor of a LibSearch instance
  * @param dataBaseslist an ArrayList with the names of the database files to search.
  * It will not be modified by this code
  * @throws LibSearch.LibSearchException   */
  public LibSearch(java.util.ArrayList<String> dataBaseslist) throws LibSearchException{
    db = 0;
    cmplxNbr = 0;
    binaryOrText = 0;
    noFilesFound = true;
    openNextFile = true;
    dis = null;
    br = null;
    if(dataBaseslist == null) {throw new LibSearchException("Error: dataBaseslist = null in \"DBSearch\" constructor");}
    if(dataBaseslist.size() <=0) {throw new LibSearchException("Error: dataBaseslist is empty in \"DBSearch\" constructor");}
    this.localDBlist = dataBaseslist;
  }

  //<editor-fold defaultstate="collapsed" desc="getComplex(first)">
  /** Get the next "complex" (without checking if the complex fits with selected components).
   * Both binary and text files will be read.
   * This routine must NOT be called to convert database files (Text <=> Binary), because
   * it reads both binary and text files.
   * <p>On output:<br>
   * if <code>binaryOrText</code> = 2 reading binary database<br>
   * if <code>binaryOrText</code> = 1 reading text database<br>
   * if <code>binaryOrText</code> = 0 then all files are closed
   *   (because they have been read)
   * 
   * @param firstComplex if true the first file is opened and the first complex
   * is searched for; if false then find the next complex from the list of
   * database files
   * @return either a "Complex" object, or null if nomore reactionsare found.
   * @throws LibSearch.LibSearchException */
  public Complex getComplex(boolean firstComplex) throws LibSearchException {
    if(firstComplex) {  // open the first file
        openNextFile = true;
        db = 0;
    }//firstComplex
    while (db < localDBlist.size()) {
      if(openNextFile) {
        try{
            if(dis != null) {dis.close();} else if(br != null) {br.close();}
        } catch (java.io.IOException ioe) {MsgExceptn.msg(ioe.getMessage());}
        complxFileName = localDBlist.get(db);
        if(complxFileName == null || complxFileName.length() <=0) {continue;}
        java.io.File dbf = new java.io.File(complxFileName);
        if(!dbf.exists() || !dbf.canRead()) {
            String msg = "Can not open file"+nl+
                         "    \""+complxFileName+"\".";
            if(!dbf.exists()) {msg = msg +nl+ "(the file does not exist)."+nl+
                                              "Search terminated";}
            throw new LibSearchException(msg);
        }

        cmplxNbr = 0;
        //--- text or binary?
        try{
            if(complxFileName.toLowerCase().endsWith("db")) { //--- binary file
                binaryOrText = 2;
                dis = new java.io.DataInputStream(new java.io.FileInputStream(dbf));
            } else { //--- text file
                binaryOrText = 1;
                br = new java.io.BufferedReader(new java.io.FileReader(dbf));
            } //--- text or binary?
        }
        catch (java.io.FileNotFoundException ex) {
            try{
                if(dis != null) {dis.close();} else if(br != null) {br.close();}
            } catch (java.io.IOException ioe) {MsgExceptn.msg(ioe.getMessage());}
            String msg = "Error: "+ex.getMessage()+nl+
                    "while trying to open file: \""+complxFileName+"\"."+nl+"Search terminated";
            throw new LibSearchException(msg);
        }
        noFilesFound = false;
        openNextFile = false;
      } //if openNextFile

      Complex complex = null;
      loopComplex:
      while (true) {
        cmplxNbr++;
        
        if(binaryOrText ==2) { //Binary complex database
            try {complex = LibDB.getBinComplex(dis);}
            catch (LibDB.ReadBinCmplxException ex) {
                String msg = ex.getMessage()+nl+
                    "reading reaction nbr. = "+cmplxNbr+" in \"getComplex\""+nl+
                    "from file: \""+complxFileName+"\"";
                throw new LibSearchException(msg);
            }
        } else if(binaryOrText ==1) { // Text  complex database
            try {
                try {complex = LibDB.getTxtComplex(br);}
                catch (LibDB.EndOfFileException ex) {complex = null;}
            }
            catch (LibDB.ReadTxtCmplxException ex) {
                String msg = ex.getMessage()+nl+
                    "reading reaction nbr. = "+cmplxNbr+" in \"getComplex\""+nl+
                    "from file: \""+complxFileName+"\"";
                throw new LibSearchException(msg);
            }
        } //binaryOrText =1 (Text file)

//try{Thread.sleep(1);} catch (InterruptedException ex) {}

        if(complex == null) {break;} // loopComplex // end-of-file, open next database
        return complex;
      } //while (true)  --- loopComplex:
      // ----- no complex found: end-of-file, or error. Get next file
      db++;
      openNextFile = true;
      binaryOrText = 0;
    } //while sd.db < pd.dataBasesList.size()

    if(noFilesFound) { // this should not happen...
        throw new LibSearchException("None of the databases could be found.");
    }
    libSearchClose();
    return null; //return null if no more reactions
  } //getComplex(firstComplex)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="libSearchClose">
  public void libSearchClose() {
    try{
        if(dis != null) {dis.close();} else if(br != null) {br.close();}
    } catch (java.io.IOException ioe) {MsgExceptn.exception(ioe.getMessage());}
    binaryOrText = 0;
    openNextFile = true;
  }
  //</editor-fold>

public class LibSearchException extends Exception {
    public LibSearchException() {super();}
    public LibSearchException(String txt) {super(txt);}
}

}
