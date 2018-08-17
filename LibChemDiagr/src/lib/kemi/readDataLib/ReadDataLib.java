package lib.kemi.readDataLib;

import lib.common.Util;

/** Read data from an input file where data is sepparated by commas,
 * white space, or end-of-line. The procedures used are <code>readA</code>,
 * <code>readI</code>, <code>readR</code> and <code>readLine</code>.
 * 
 * Comments may be added to any line after a "/" if it is the 1st
 * non-blank character in the line, or if "/" follows either a comma or a blank.
 * 
 * Text strings begin at the 1st non-blank character and must end either
 * with a comma, an End-Of-Line or an End-Of-File.
 * Therefore text strings may contain blank space, but not commas.
 * Also text strings may not contain the sequence " /" (which indicates the
 * beginning of a comment)
 *
 * A comma following another comma or following an end-of-line is taken
 * to be either a zero or an empty string.
 *
 * Comments may be retrieved with the variable <code>dataLineComment</code>,
 * which contains the comment in the input line for the last data item retrieved
 * (using either <code>readA</code>, <code>readI</code> or <code>readR</code>).
 * This means that a comment written in a line by itself can only be retrieved
 * using the procedure <code>readLine</code>.
 *
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
public class ReadDataLib {
//--- public ---
/** Provided by the user before calling the "readI()", "readR()", etc,
 * so that info if an error occurs may be provided.  */
public String nowReading;
/** Set by this class to the name of the intput data file. Not to be changed */
public String dataFileName;
/** It will contain the comment (if any) corresponding to the line read from
 * the input file when reading the last data value. Not to be changed. */
public StringBuffer dataLineComment;
/** Set by this class to:
 * <pre> =1 if the comment at the end of the first line contains
 *    either "HYDRA" or "DATABASE";
 * =2 if the comment contains either "MEDUSA" or "SPANA";
 * =0 otherwise.</pre>
 * Not to be changed
 * @see lib.kemi.chem.Chem.Diagr#databaseSpanaFile databaseSpanaFile
 */
public int fileIsDatabaseOrSpana;
//--- private ---
private String temperatureString = null;
/** what units are given after the temperature in the comment
 * in the first line:<br>
 * false = given as "t=25" (no units) or "t=25 C" (Celsius = default)<br>
 * true = given as "t=298 K"<br>
 * Note: the units for temperature are not case-sensitive
 * (it is OK to write: T=298k). */
private boolean temperatureUnitsK = false;
private String temperatureDataLineNextOriginal = null;
private String pressureString = null;
/** what units are given after the pressure in the comment
 * in the first line:<br>
 * 0= given as "p=1" (no units) or "p=1 bar" (bar = default)<br>
 * 1= given as "p=1 atm"<br>
 * 2= given as "p=1 MPa"<br>
 * Note: the units for pressure are not case-sensitive. */
private int pressureUnits = 0;
private String pressureDataLineNextOriginal = null;
private java.io.BufferedReader inputBuffReader;
/** this is a temporary working string where data separated by commas are read.
 * Comments have been removed.  When a value is read, the corresponding part
 * (at the left) of <code>thisDataLine</code> is removed.  For example,
 * if <code>thisDataLine</code> contains "3,5,1," and a value is read then
 * <code>thisDataLine</code> will contain "5,1," and the value read is 3. */
private StringBuffer thisDataLine;
/** the original line in the data file that was read. Comments included.
 * Used for example in error reporting. */
private StringBuffer thisDataLineOriginal;
private StringBuffer nextDataLine;
private StringBuffer nextDataLineOriginal;
private String nextDataLineComment;
private boolean reading1stLine;
/** New-line character(s) to substitute "\n" */
private static final String nl = System.getProperty("line.separator");

//<editor-fold defaultstate="collapsed" desc="ReadDataLib constructor">
/** Create an instance of this class associated with an input data file.
 * @param inpF Input text file.
 * @throws lib.kemi.readDataLib.ReadDataLib.DataFileException
 */
public ReadDataLib(java.io.File inpF) throws DataFileException {
    if(inpF == null) {
        throw new DataFileException("Error in \"ReadDataLib\": input file is \"null\"!");
        }
    dataFileName = inpF.getPath();
    if(!inpF.exists()) {
        throw new DataFileException("Error in \"ReadDataLib\": input file does not exist"+nl+"  \""+dataFileName+"\"");
        }
    if(!inpF.canRead()) {
        throw new DataFileException("Error in \"ReadDataLib\": can not read input file"+nl+"  \""+dataFileName+"\"");
        }
    inputBuffReader = null;
    try {inputBuffReader = new java.io.BufferedReader(new java.io.FileReader(inpF));}
    catch(java.io.FileNotFoundException ex) {
        throw new DataFileException("Error in \"ReadDataLib\": "+ex.getMessage()+nl+
                            "  with input file:\""+dataFileName+"\"");}
    temperatureDataLineNextOriginal = null;
    temperatureString = null;
    temperatureUnitsK = false;
    pressureDataLineNextOriginal = null;
    pressureString = null;
    pressureUnits = 0;
    fileIsDatabaseOrSpana = 0;
    reading1stLine = true;
    nowReading = null;
    thisDataLine = null;
    thisDataLineOriginal = null;
    nextDataLine = new StringBuffer();
    nextDataLineOriginal = new StringBuffer();
    dataLineComment = new StringBuffer();
} //ReadDataLib constructor
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="close()">
/** Close the input file
 * @throws lib.kemi.readDataLib.ReadDataLib.ReadDataLibException
 */
public void close() throws ReadDataLibException {
    try {if (inputBuffReader != null) {inputBuffReader.close();}}
    catch (java.io.IOException ex) {
        throw new ReadDataLibException("Error in \"ReadDataLib.close\": "+ex.getMessage());
    }
    finally {
        dataFileName = null;
        nowReading = null;
        thisDataLine = null;
        thisDataLineOriginal = null;
        dataLineComment = null;
        nextDataLine = null;
        nextDataLineOriginal = null;
        nextDataLineComment = null;
        pressureDataLineNextOriginal = null;
        pressureString = null;
        temperatureDataLineNextOriginal = null;
        temperatureString = null;
    }
} // close()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="String readA()">
/** Read a text string from the input file.
 * Note that a text begins with the first non-blank character and it
 * ends with either a comma or an end-of-line: it may contain embedded spaces.
 * @return the next text string from the input file.
 * @throws lib.kemi.readDataLib.ReadDataLib.DataReadException
 * @throws lib.kemi.readDataLib.ReadDataLib.DataEofException
 */
public String readA() throws DataReadException, DataEofException {
    if(inputBuffReader == null) {return null;}  // do we have a BufferedReader?
    if(nowReading == null) {nowReading = "a text string";}
    getThisDataLine();
    if (thisDataLine.length() <=0) {nowReading = null; return null;} // an error occurred

    // at this point: thisDataLine.length()>0
    // where does next number or text beguin?
    int j = thisDataLine.indexOf(","); // j is between 0 and (thisDataLine.length()-1)
    int k = thisDataLine.length();
    if(j>-1 && j<k) {k=j;}
    // is there a next number or text in this line?
    if(k >-1 && k < thisDataLine.length()) { //there is a comma in thisDataLine (there should be!)
        String txt = thisDataLine.substring(0,k).trim();
        thisDataLine.delete(0,k);
        if(thisDataLine.length()>0) { // remove the comma marking the end of the text
            if(thisDataLine.charAt(0) == ',') {thisDataLine.deleteCharAt(0);}
        }
        // remove whitespace around the remains of the input line
        thisDataLine = new StringBuffer(thisDataLine.toString().trim());
        nowReading = null;
        return txt;
    } else { //there is no comma in thisDataLine (this should not happen because
                //  checkNextDataLine() adds a comma at the end of the line)
        String txt = thisDataLine.toString().trim();
        thisDataLine.delete(0, thisDataLine.length());
        nowReading = null;
        return txt;
    }
} //readA()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="int readI()">
/** Reads an integer from the input file. Data may be separated by commas,
 * blanks or end-of-line.
 * @return the next integer in the input file.
 * @throws lib.kemi.readDataLib.ReadDataLib.DataReadException
 * @throws lib.kemi.readDataLib.ReadDataLib.DataEofException
 */
public int readI() throws DataReadException, DataEofException {
    if(inputBuffReader == null) {return Integer.MIN_VALUE;} // do we have a BufferedReader?
    if(nowReading == null) {nowReading = "an integer";}
    getThisDataLine();
    if (thisDataLine.length() <=0)
        {nowReading = null; return Integer.MIN_VALUE;} // an error occurred

    // at this point: thisDataLine.length()>0
    int i = thisDataLine.indexOf(" "); // i is between 0 and (thisDataLine.length()-1)
    int j = thisDataLine.indexOf(",");
    int k = thisDataLine.length();
    if(i>-1 && i<k) {k=i;}
    if(j>-1 && j<k) {k=j;}
    String txt;
    if(k >-1 && k < thisDataLine.length()) { //there is a comma or space in thisDataLine (there should be!)
        txt = thisDataLine.substring(0,k).trim();
        thisDataLine.delete(0,(k+1));
        //remove whitespace at the end and beginning of the line
        thisDataLine = new StringBuffer(thisDataLine.toString().trim());
        if(thisDataLine.length()>0) {
            // if there was a space followed by comma
            //    after the number remove the comma
            if(thisDataLine.charAt(0) == ',') {thisDataLine.deleteCharAt(0);}
        }
        //remove whitespace at the end and beginning of the line
        //  (after removing leading comma)
        thisDataLine = new StringBuffer(thisDataLine.toString().trim());
    } else { //there is no comma or space in thisDataLine (this should not happen because
             //   checkNextDataLine() adds a comma at the end of the line)
        txt = thisDataLine.toString().trim();
        thisDataLine.delete(0, thisDataLine.length());
    }
    int value = Integer.MIN_VALUE;
    if(txt.length() == 0) {value = 0;}
    else {
    try{value = Integer.valueOf(txt);}
    catch(NumberFormatException ex) {throw new DataReadException(
                    "Error:"+nl+
                    "NumberFormatException reading an integer from input string: \""+txt+"\""+nl+
                    "when reading: "+nowReading+nl+
                    "in line: \""+thisDataLineOriginal.toString()+"\""+nl+
                    "in file: \""+dataFileName+"\".");}
    catch(NullPointerException ex) {throw new DataReadException(
                    "Error:"+nl+
                    "NullPointerException reading an integer from input string: \""+txt+"\""+nl+
                    "when reading: "+nowReading+nl+
                    "in line: \""+thisDataLineOriginal.toString()+"\""+nl+
                    "in file: \""+dataFileName+"\".");}
    } // txt = ""?
    nowReading = null;
    return value;
} //readI
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="float readR()">
/** Read a float from the input file. Data may be separated by commas,
 * blanks or end-of-line.
 * @return the next float from the input file.
 * @throws lib.kemi.readDataLib.ReadDataLib.DataReadException
 * @throws lib.kemi.readDataLib.ReadDataLib.DataEofException
 */
public float readR() throws DataReadException, DataEofException {
    float f = (float) readD();
    if(Math.abs(f) < Float.MIN_VALUE) {f = 0f;}
    return f;
} //readR
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="double readD()">
/** Read a double from the input file. Data may be separated by commas,
 * blanks or end-of-line.
 * @return the next double from the input file.
 * @throws lib.kemi.readDataLib.ReadDataLib.DataReadException
 * @throws lib.kemi.readDataLib.ReadDataLib.DataEofException
 */
public double readD() throws DataReadException, DataEofException {
    if(inputBuffReader == null) {return Double.NaN;}  // do we have a BufferedReader?
    if(nowReading == null) {nowReading = "a double (floating point value)";}
    getThisDataLine();
    if (thisDataLine.length() <=0)
        {nowReading = null; return Double.NaN;} // an error occurred

    // at this point: thisDataLine.length()>0
    int i = thisDataLine.indexOf(" "); // i is between 0 and (thisDataLine.length()-1)
    int j = thisDataLine.indexOf(",");
    int k = thisDataLine.length();
    if(i>-1 && i<k) {k=i;}
    if(j>-1 && j<k) {k=j;}
    String txt;
    if(k >-1 && k < thisDataLine.length()) { //there is a comma or space in thisDataLine (there should be!)
        txt = thisDataLine.substring(0,k).trim();
        thisDataLine.delete(0,(k+1));
        //remove whitespace at the end and beginning of the line
        thisDataLine = new StringBuffer(thisDataLine.toString().trim());
        if(i == k && thisDataLine.length()>0) {
            // if there was a space followed by comma
            //    after the number remove the comma
            if(thisDataLine.charAt(0) == ',') {thisDataLine.deleteCharAt(0);}
        }
        //remove whitespace at the end and beginning of the line
        //  (after removing leading comma)
        thisDataLine = new StringBuffer(thisDataLine.toString().trim());
    } else { //there is no comma or space in thisDataLine (this should not happen because
             //   checkNextDataLine() adds a comma at the end of the line)
        txt = thisDataLine.toString().trim();
        thisDataLine.delete(0, thisDataLine.length());
    }
    double value = Double.NaN;
    if(txt.length() == 0) {value = 0;}
    else {
    try{value = Double.valueOf(txt);}
    catch(NumberFormatException ex) {throw new DataReadException(
                    "Error:"+nl+
                    "NumberFormatException reading a float value from input string: \""+txt+"\""+nl+
                    "when reading: "+nowReading+nl+
                    "in line: \""+thisDataLineOriginal.toString()+"\""+nl+
                    "in file: \""+dataFileName+"\".");}
    catch(NullPointerException ex) {throw new DataReadException(
                    "Error:"+nl+
                    "NullPointerException reading a float value from input string: \""+txt+"\""+nl+
                    "when reading: "+nowReading+nl+
                    "in line: \""+thisDataLineOriginal.toString()+"\""+nl+
                    "in file: \""+dataFileName+"\".");}
    } // txt = ""?
    nowReading = null;
    if(Math.abs(value) < Double.MIN_VALUE) {value = 0;}
    return value;
} //readD
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="String readLine()">
/** Read a text line from the input file. Note:<br>
 * - any values not yet read in the current input line are discarded!<br>
 * - it reads a complete line, including comments.
 * @return the next text line from the input file.
 * @throws lib.kemi.readDataLib.ReadDataLib.DataReadException
 * @throws lib.kemi.readDataLib.ReadDataLib.DataEofException
 */
public String readLine() throws DataReadException, DataEofException {
    if(inputBuffReader == null) {return null;}  // do we have a BufferedReader?
    // clean up
    nextDataLineComment = null;
    if(thisDataLineOriginal != null && thisDataLineOriginal.length() >0) {thisDataLineOriginal.delete(0, thisDataLineOriginal.length());}
    if(thisDataLine != null && thisDataLine.length() >0) {thisDataLine.delete(0, thisDataLine.length());}
    if(dataLineComment == null) {dataLineComment = new StringBuffer();}
    else {if(dataLineComment.length() >0) {dataLineComment.delete(0, dataLineComment.length());}}
    //
    if(nowReading == null) {nowReading = "a text line";}
    getNextDataLine();
    nowReading = null;
    if(nextDataLineOriginal == null) {
        throw new DataEofException("Error: reached the End-Of-File,"+nl+
                  "when reading: "+nowReading+nl+
                  "in file: \""+dataFileName+"\".");
    }
    return nextDataLineOriginal.toString();
} //readLine()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="double getTemperature">
/** Returns the temperature (in degrees Celcius) if it is found in a
 * comment in the first line of the input data file.
 * @return the temperature in degrees Celsius. Returns NaN if:
 * a) the call is made before the first line has been read;
 * b) there is no comment with the temperature;
 * @throws lib.kemi.readDataLib.ReadDataLib.DataReadException
 */
public double getTemperature() throws DataReadException {
  if(temperatureString == null ||
          temperatureString.length() <=0) {return Double.NaN;}
  int i;
  for(i =0; i < temperatureString.length(); i++) {
    if(!Character.isDigit(temperatureString.charAt(i)) &&
            temperatureString.charAt(i) != 'E' && //the string is uppercase
            temperatureString.charAt(i) != '.' &&
            temperatureString.charAt(i) != '-' &&
            temperatureString.charAt(i) != '+') {break;}
  } //for i
  if(i <=0) {return Double.NaN;}
  if(i < temperatureString.length()-1) {
      temperatureString = temperatureString.substring(0, i);
  }
  Double w;
  try{w = Double.parseDouble(temperatureString);}
  catch (NumberFormatException ex) {
      throw new DataReadException("Error:"+nl+
                            "NumberFormatException while reading the temperature in line:"+nl+
                            "\""+temperatureDataLineNextOriginal+"\""+nl+
                            "in file \""+dataFileName+"\"");
  }
  if(temperatureUnitsK) {w = w - 273.15;}
  return w;
} //getTemperature()

//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="double getPressure">
/** Returns the pressure (in "bar") if it is found in a comment in
 * the first line of the input data file.
 * @return the tpressure in bar. Returns NaN if:
 * a) the call is made before the first line has been read;
 * b) there is no comment with the pressure;
 * @throws lib.kemi.readDataLib.ReadDataLib.DataReadException
 */
public double getPressure() throws DataReadException {
  if(pressureString == null ||
          pressureString.length() <=0) {return Double.NaN;}
  int i;
  for(i =0; i < pressureString.length(); i++) {
    if(!Character.isDigit(pressureString.charAt(i)) &&
            pressureString.charAt(i) != 'E' && //the string is uppercase
            pressureString.charAt(i) != '.' &&
            pressureString.charAt(i) != '+') {break;}
  } //for i
  if(i <=0) {return Double.NaN;}
  if(i < pressureString.length()-1) {
      pressureString = pressureString.substring(0, i);
  }
  Double w;
  try{w = Double.parseDouble(pressureString);}
  catch (NumberFormatException ex) {
      throw new DataReadException("Error:"+nl+
                            "NumberFormatException while reading the pressure in line:"+nl+
                            "\""+pressureDataLineNextOriginal+"\""+nl+
                            "in file \""+dataFileName+"\"");
  }
  if(pressureUnits == 1) {w = w*1.013;} //atm
  else if(pressureUnits == 2) {w = w/10;} //MPa
  return w;
} //getPressure()

//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="private methods">

//<editor-fold defaultstate="collapsed" desc="getThisDataLine()">
/** If <code>thisDataLine</code> is empty read the next (not empty) line
 * from the input file. Otherwise do nothing.<br>
 * This procedure is called at the beginning of <code>readA</code>, <code>readI</code> and
 * <code>readR</code>; but NOT from <code>readLine</code>.
 * @throws readDataLib.ReadDataLib.DataReadException
 * @throws readDataLib.ReadDataLib.DataEofException
 */
private void getThisDataLine() throws DataReadException, DataEofException {
  if(thisDataLine == null) {thisDataLine = new StringBuffer();}
  while(thisDataLine.length() <=0) { //get a non-empty line
    // clean contents:
    if(thisDataLineOriginal == null) {thisDataLineOriginal = new StringBuffer();}
    else {if(thisDataLineOriginal.length() >0) {thisDataLineOriginal.delete(0, thisDataLineOriginal.length());}}
    if(dataLineComment == null) {dataLineComment = new StringBuffer();}
    else {if(dataLineComment.length() >0) {dataLineComment.delete(0, dataLineComment.length());}}
    // move next line it into "thisDataLine" and get a new "next" data line
    //read "next" non-empty line from the input file or until end-of-file
    while(true) {
        getNextDataLine();  //note that nextDataLine might be an empty or "null"
        if(nextDataLine == null) {
            String t = "reached the End-Of-File,"+nl;
            if(nowReading != null && nowReading.length()>0) {t = t+"when reading: "+nowReading+nl;}
            t = t+"in file: \""+dataFileName+"\".";
            throw new DataEofException(t);
        }
        if(nextDataLine.length() >0) {break;}
    }
    thisDataLine.append(nextDataLine);
    thisDataLineOriginal.append(nextDataLineOriginal);
    if(nextDataLineComment != null) {dataLineComment.append(nextDataLineComment);}
    nextDataLineComment = null;
  } //while thisDataLine empty
} // getThisDataLine()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="getNextDataLine()">
/** Read next line. Store the information in variables
 * <code>nextDataLine</code> and <code>nextDataLineOriginal</code>.<br>
 * Note that the next line in the data file may be empty or only contain comments,
 * if so <code>nextDataLine</code> and/or <code>nextDataLineOriginal</code>
 * will be empty too.<br>
 * @throws readDataLib.ReadDataLib.DataReadException
 */
private void getNextDataLine() throws DataReadException {
  if(nextDataLine == null) {return;} // this occurs after End-Of-File
  //clean variables
  nextDataLine.delete(0, nextDataLine.length());
  if(nextDataLineOriginal == null) {nextDataLineOriginal = new StringBuffer();}
  else {if(nextDataLineOriginal.length() >0) {nextDataLineOriginal.delete(0, nextDataLineOriginal.length());}}
  //read next line from the input file
  String nextLine = null;
  try {nextLine = inputBuffReader.readLine();}
  catch (java.io.IOException ex) {
        String t = ex.getMessage()+ " in \"getNextDataLine()\""+nl;
        if(nowReading != null && nowReading.length()>0) {t = t+"when reading: "+nowReading+nl;}
        t = t + "in file: \""+dataFileName+"\".";
        throw new DataReadException(t);
  }
  if(nextLine == null) { //end-of-file encountered
        nextDataLineOriginal = null;
        nextDataLine = null;  //end of file
  } else { //nextLine != null
        nextLine = Util.rTrim(nextLine);
        nextDataLine.append(nextLine);
        nextDataLineOriginal.append(nextLine);
        try {checkNextDataLine();} //this could set nextDataLine = ""
        catch(Exception ex) {
            String t = ex.getMessage()+ " in \"getNextDataLine()\""+nl;
            if(nowReading != null && nowReading.length()>0) {t = t+"when reading: "+nowReading+nl;}
            t = t + "in line: \""+nextLine+"\""+nl+"in file: \""+dataFileName+"\".";
            throw new DataReadException(t);
        }
  } // nextLine != null?
} //getNextDataLine()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="checkNextDataLine()">
/** Modify <code>nextDataLine</code>:<br>
 * - Remove any comment at the end of <code>nextDataLine</code> and 
 * store the comment in variable <code>nextDataLineComment</code>.<br>
 * - Read temperature/pressure from <code>nextDataLineComment</code>.<br>
 * - Remove whitespace from <code>nextDataLine</code> and make sure it ends with "," (comma). */
private void checkNextDataLine() {
    nextDataLineComment = null;
    if(nextDataLine.length() <=0) {return;}
    // Comments: after "/" if it is 1st character in the line
    //      or if it follows either space, tab or comma
    int i =0; int imax =nextDataLine.length();
    int i1,i2,i3,i4,i5;
    do {
        i = nextDataLine.indexOf("/", i);
        if(i >0) {
            char c = nextDataLine.charAt(i-1);
            if(c == ',' || c == ' ' || c == '\t') {
                nextDataLineComment = nextDataLine.substring(i+1); // skip the slash
                nextDataLine.delete(i, imax);
                break;
            }
            if(i>imax) {break;}
            i = i+1;
        } //if "/" found
        else if(i == 0) {
            nextDataLineComment = nextDataLine.toString();
            nextDataLine.delete(0, nextDataLine.length());
            break;
        } //if i=0
        else {break;}
    } while(true);

    //--- read temperature and pressure. Is it a DataBase/Spana or Hydra/Medusa file?
    if(reading1stLine && nextDataLineComment != null) {
        // is this a DataBase/Hydra or Spana/Medusa file?
        if(nextDataLineComment.indexOf("HYDRA") > -1
                || nextDataLineComment.indexOf("DATABASE") > -1) {
            fileIsDatabaseOrSpana = 1;
        } else if(nextDataLineComment.indexOf("MEDUSA") > -1
                || nextDataLineComment.indexOf("SPANA") > -1) {
            fileIsDatabaseOrSpana = 2;
        }

        // store information on temperature if found
        String commentUC = nextDataLineComment.toUpperCase();
        i1 = commentUC.indexOf("T=");
        i2 = commentUC.indexOf("T =");
        i = i1;
        if( i2 >-1 && ((i1 >-1 && i2 <i1) || i1 <0)) {i = i2;}
        if (i >-1) {
            commentUC = commentUC.substring(i);
            i = commentUC.indexOf("="); //get the "=" in "T="
            if(i <(commentUC.length()-1)) {
                commentUC = commentUC.substring(i+1).trim();
                i1 = commentUC.indexOf("C");
                if(i1>0) {
                    // remove degree sign, as in: t=25°C; quote, as in: t=25"C; etc
                    String s = commentUC.substring(i1-1,i1);
                    if(s.equals("°") || s.equals("\"") || s.equals("'")) {i1--;}
                }
                i2 = commentUC.indexOf("K");
                i4 = commentUC.indexOf(" ");
                i5 = commentUC.indexOf(",");
                i = i1;
                if (i2 >-1 && ((i >-1 && i2 <i) || i <0)) {i = i2;}
                if (i4 >-1 && ((i >-1 && i4 <i) || i <0)) {i = i4;}
                if (i5 >-1 && ((i >-1 && i5 <i) || i <0)) {i = i5;}
                if(i>-1) {commentUC = commentUC.substring(0,i).trim();}
                temperatureString = commentUC;
                if(i2 >-1 && (i1<=-1 || (i1 > i2))) {temperatureUnitsK = true;}
                temperatureDataLineNextOriginal = nextDataLineOriginal.toString();
            } //if i <(commentUC.length()-1)
        } // if found "T="

        // store information on pressure if found
        commentUC = nextDataLineComment.toUpperCase();
        i1 = commentUC.indexOf("P=");
        i2 = commentUC.indexOf("P =");
        i = i1;
        if( i2 >-1 && ((i1 >-1 && i2 <i1) || i1 <0)) {i = i2;}
        if (i >-1) {
            commentUC = commentUC.substring(i);
            i = commentUC.indexOf("=");
            if(i <(commentUC.length()-1)) {
                commentUC = commentUC.substring(i+1).trim();
                i1 = commentUC.indexOf("BAR");
                i2 = commentUC.indexOf("ATM");
                i3 = commentUC.indexOf("MPA");
                i4 = commentUC.indexOf(" ");
                i5 = commentUC.indexOf(",");
                i = i1;
                if (i2 >-1 && ((i >-1 && i2 <i) || i <0)) {i = i2;}
                if (i3 >-1 && ((i >-1 && i3 <i) || i <0)) {i = i3;}
                if (i4 >-1 && ((i >-1 && i4 <i) || i <0)) {i = i4;}
                if (i5 >-1 && ((i >-1 && i5 <i) || i <0)) {i = i5;}
                if(i>-1) {commentUC = commentUC.substring(0,i).trim();}
                pressureString = commentUC;
                pressureUnits = 0;
                if(i2 >-1 && (i1<=-1 || (i1 > i2)) && (i3<=-1 || (i3 > i2))) {
                    pressureUnits = 1;
                }
                if(i3 >-1 && (i1<=-1 || (i1 > i3)) && (i2<=-1 || (i2 > i3))) {
                    pressureUnits = 2;
                }
                pressureDataLineNextOriginal = nextDataLineOriginal.toString();
            } //if i <(commentUC.length()-1)
        } // if found "P="

    } // read temperature and pressure if reading 1st line and nextDataLineComment !=null
    reading1stLine = false;

    //remove whitespace at the end and beginning of the line
    nextDataLine = new StringBuffer(nextDataLine.toString().trim());
    //if the line is empty:
    if(nextDataLine.length() <= 0) {return;}

    // change any character lower than space to space
    for(i=0; i <nextDataLine.length(); i++) {
        if((int)nextDataLine.charAt(i) < 32) {nextDataLine.setCharAt(i,' ');}
    }
    //remove whitespace at the end
    String t = Util.rTrim(nextDataLine.toString());
    if(t.length() <= 0) {return;}
    nextDataLine.delete(0, nextDataLine.length());
    nextDataLine.append(t);
    // add comma at the end if necessary
    if(nextDataLine.charAt(nextDataLine.length()-1) != ',') {
        nextDataLine.append(",");
    }
} // checkNextDataLine()
//</editor-fold>

//</editor-fold>

/** an exception occurred within the "ReadDataLib" procedures */
public class ReadDataLibException extends Exception {
    public ReadDataLibException() {}
    public ReadDataLibException(String txt) {super(txt);}
} //ReadDataLibException
/** an exception occurred while thrying either to open or to close the input data file */
public class DataFileException extends ReadDataLibException {
    public DataFileException() {}
    public DataFileException(String txt) {super(txt);}
} //DataFileException
/** wrong numeric format while reading either some integer or some double */
public class DataReadException extends ReadDataLibException {
    public DataReadException() {}
    public DataReadException(String txt) {super(txt);}
} //DataReadException
/** End-Of-File encountered while trying to read some value */
public class DataEofException extends ReadDataLibException {
    private DataEofException() {}
    private DataEofException(String txt) {super(txt);}
} //DataEofException
} 