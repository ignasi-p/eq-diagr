package lib.kemi.readWriteDataFiles;

import lib.common.Util;
import lib.kemi.chem.Chem;

/** Write a data file.
 * The data to write must be stored in an instance of class Chem.
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
public class WriteChemSyst {
private static java.util.Locale engl = java.util.Locale.ENGLISH;
/** New-line character(s) to substitute "\n" */
private static String nl = System.getProperty("line.separator");

/** Write a data file.
 * @param ch an instance of of the class Chem,
 *    containing the data to be written
 * @param dataFile the file that will be written
 * @throws java.io.IOException
 * @throws lib.kemi.readWriteDataFiles.WriteChemSyst.DataLimitsException
 * @throws lib.kemi.readWriteDataFiles.WriteChemSyst.WriteChemSystArgsException */
public static void writeChemSyst (Chem ch,
                                     java.io.File dataFile)
        throws DataLimitsException, WriteChemSystArgsException, java.io.IOException {
  if(ch == null) {
      throw new WriteChemSystArgsException(
              nl+"Error in \"writeChemSyst\":"+nl+
                         "   empty instance of class \"Chem\"");
  }
  if(dataFile == null || dataFile.getName().length()<=0) {
      throw new WriteChemSystArgsException(
              nl+"Error in \"writeChemSyst\":"+nl+
                         "   no output data file given");
  }
  String dataFileName;
  try{dataFileName = dataFile.getCanonicalPath();}
  catch (java.io.IOException ex) {dataFileName = dataFile.getAbsolutePath();}
  if(!dataFileName.toLowerCase().endsWith(".dat")) {
        throw new WriteChemSystArgsException(
              "File: \""+dataFileName+"\""+nl+
              "Error: data file name must end with \".dat\"");
  } // does not end with ".dat"
  if(dataFile.exists() && (!dataFile.canWrite() || !dataFile.setWritable(true))) {
      throw new WriteChemSystArgsException("Error: can not modify file:"+nl+
              "   \""+dataFile.getPath()+"\""+nl+
              "Is this file write-protected?");
  }
  // Make a temporary file to write the information. If no error occurs,
  // then the data file is overwritten with the temporary file
  String tmpFileName;
  tmpFileName = dataFileName.substring(0,dataFileName.length()-4).concat(".tmp");
  java.io.File tmpFile = new java.io.File(tmpFileName);
  java.io.FileOutputStream fos = null;
  java.io.Writer w = null;
  fos = new java.io.FileOutputStream(tmpFile);
  w = new java.io.BufferedWriter(new java.io.OutputStreamWriter(fos,"UTF8"));
  Chem.ChemSystem cs = ch.chemSystem;
  Chem.ChemSystem.NamesEtc namn = cs.namn;
  Chem.Diagr diag = ch.diag;
  Chem.DiagrConcs dgrC = ch.diagrConcs;

  // make some simple checks
  if(cs.Na<1 || cs.Na>1000) {
      throw new DataLimitsException(
            nl+"Error: Number of components is: "+cs.Na+nl+
            "Must be >0 and <1000.");
      }
  if(cs.nx < 0 || cs.nx > 1000000) {
        throw new DataLimitsException(
            nl+"Error: Number of soluble complexes is: "+cs.nx+nl+
            "Must be >=0 and < 1 000 000.");}
  int nrSol = cs.mSol - cs.solidC;
  if(nrSol < 0 || nrSol > 100000) {
        throw new DataLimitsException(
            nl+"Error: Number of solid reaction products is: "+nrSol+nl+
            "Must be >=0 and < 100 000.");}
  if(cs.solidC < 0 || cs.solidC > cs.Na) {
        throw new DataLimitsException(
            nl+"Error: Number of solid components is: "+cs.solidC+nl+
            "Must be >=0 and <= "+cs.Na+" (the nbr of components).");}

  String m = ",   /SPANA (MEDUSA)";
  if(!Double.isNaN(diag.temperature)) {m = m + ", t="+Util.formatDbl3(diag.temperature);}
  if(!Double.isNaN(diag.pressure)) {m = m + ", p="+Util.formatDbl3(diag.pressure);}
  w.write(" "+String.valueOf(cs.Na)+", "+
          String.valueOf(cs.nx)+", "+String.valueOf(nrSol)+", "+
          String.valueOf(cs.solidC) + m.trim() +nl);
  for(int i=0; i < cs.Na; i++) {
    w.write(String.format("%s",namn.identC[i]));
    if(namn.comment[i] != null && namn.comment[i].length() >0) {w.write(String.format(" /%s",namn.comment[i]));}
    w.write(nl);
  } //for i
  w.flush();
  int ix; StringBuilder logB = new StringBuilder(); int j;
  for(int i=cs.Na; i < cs.Na+cs.nx+nrSol; i++) {
    if(namn.ident[i].length()<=20) {
      w.write(String.format(engl, "%-20s, ",namn.ident[i]));
    } else {w.write(String.format(engl, "%s, ",namn.ident[i]));}
    ix = i - cs.Na;
    if(logB.length()>0) {logB.delete(0, logB.length());}
    if(Double.isNaN(cs.lBeta[ix])) {
        throw new WriteChemSystArgsException(nl+
                    "Error: species \""+namn.ident[i]+"\" has logK = Not-a-Number."+nl+
                    "   in \"writeChemSyst\","+nl+
                    "   while writing the data file"+nl+
                    "   \""+tmpFileName+"\"");
    }
    logB.append(Util.formatDbl3(cs.lBeta[ix]));
    //make logB occupy at least 10 chars: padding with space
    j = 10 - logB.length();
    if(j>0) {for(int k=0;k<j;k++) {logB.append(' ');}}
    else {logB.append(' ');} //add at least one space
    w.write(logB.toString());
    for(j=0; j < cs.Na-1; j++) {
      w.write(Util.formatDbl4(cs.a[ix][j])+" ");
    }//for j
    w.write(Util.formatDbl4(cs.a[ix][cs.Na-1]));
    if(namn.comment[i] != null && namn.comment[i].length() >0) {w.write(String.format(" /%s",namn.comment[i]));}
    w.write(nl);

  }//for i
  w.flush();

  // ------------------------
  //  Write Plot information
  // ------------------------
  boolean ePresent =false;
  for(int i=0; i < cs.Na; i++) {
    if(Util.isElectron(namn.identC[i])) {ePresent = true; break;}
  }
  if(!ePresent) {
      for(int i=cs.Na; i < cs.Na+cs.nx+nrSol; i++) {
        if(Util.isElectron(namn.ident[i])) {ePresent = true; break;}
      }
  }
  if(diag.plotType == 0) { // a Predom diagram:
    if(ePresent && diag.Eh) {w.write("EH, ");}
    w.write(namn.identC[diag.compY]+", "+
                     namn.identC[diag.compX]+", "+
                     namn.identC[diag.compMain]+","+nl);
  } else { // a SED diagram
    if(diag.plotType == 1) { // "Fraction"
      w.write(namn.identC[diag.compY]+", ");
      if(ePresent && diag.Eh) {w.write("EH, ");}
      w.write(namn.identC[diag.compX]+","+nl);
    } else
    if(diag.plotType == 2) { // "log Solubilities"
      w.write("LS,"+Util.formatDbl4(diag.yLow)+","+Util.formatDbl4(diag.yHigh)+", ");
      if(ePresent && diag.Eh) {w.write("EH, ");}
      w.write(namn.identC[diag.compX]+","+nl);
    } else
    if(diag.plotType == 3) { // "Logarithmic"
      w.write("LC,"+Util.formatDbl4(diag.yLow)+","+Util.formatDbl4(diag.yHigh)+", ");
      if(ePresent && diag.Eh) {w.write("EH, ");}
      w.write(namn.identC[diag.compX]+","+nl);
    } else
    if(diag.plotType == 4) { // "Relative activities"
      w.write("R, "+namn.identC[diag.compY]+", "
              +Util.formatDbl4(diag.yLow)+","+Util.formatDbl4(diag.yHigh)+", ");
      if(ePresent && diag.Eh) {w.write("EH, ");}
      w.write(namn.identC[diag.compX]+","+nl);
    } else
    if(diag.plotType == 5) { // "calculated pe" "calculated Eh"
      w.write("pe,"+Util.formatDbl4(diag.yLow)+","+Util.formatDbl4(diag.yHigh)+", ");
      if(ePresent && diag.Eh) {w.write("EH, ");}
      w.write(namn.identC[diag.compX]+","+nl);
    } else
    if(diag.plotType == 6) { // "calculated pH"
      w.write("pH,"+Util.formatDbl4(diag.yLow)+","+Util.formatDbl4(diag.yHigh)+", ");
      if(ePresent && diag.Eh) {w.write("EH, ");}
      w.write(namn.identC[diag.compX]+","+nl);
    } else
    if(diag.plotType == 7) { // "log Activities"
      w.write("LAC,"+Util.formatDbl4(diag.yLow)+","+Util.formatDbl4(diag.yHigh)+", ");
      if(ePresent && diag.Eh) {w.write("EH, ");}
      w.write(namn.identC[diag.compX]+","+nl);
    } else
    if(diag.plotType == 8) { // "H+ affinity spectrum"
      w.write("PS, ");
      if(ePresent && diag.Eh) {w.write("EH, ");}
      w.write(namn.identC[diag.compX]+","+nl);
    }
  } // SED or Predom?
  w.flush();

  // ------------------------
  //  Write concentrations
  // ------------------------
  String[] ct = {" ","T","TV","LTV","LA","LAV"};
  int h;
  for(int i=0; i<cs.Na; i++) {
    h = dgrC.hur[i];
    if(h ==2 || h ==3 || h ==5) { //conc varied
      w.write(ct[h]+", "+Util.formatDbl6(dgrC.cLow[i])+" "+
                                    Util.formatDbl6(dgrC.cHigh[i])+nl);
    } else
    if(h ==1 || h ==4) { //conc constant
      w.write(ct[h]+", "+Util.formatDbl6(dgrC.cLow[i])+nl);
    }
  } //for i
  // title and any comment lines
  if(diag.title != null && diag.title.length() >0) {
    w.write(diag.title+nl);
  }
  if(diag.endLines != null && diag.endLines.length() >0) {
    if(diag.title == null || diag.title.length() <=0) {w.write(nl);}
    w.write(diag.endLines+nl);
  }
  w.flush(); w.close(); fos.close();

  //the temporary file has been created without a problem
  //  delete the data file and rename the temporary file
  dataFile.delete();
  tmpFile.renameTo(dataFile);

  //return;
} //writeChemSyst
public static class WriteChemSystArgsException extends Exception {
    public WriteChemSystArgsException() {}
    public WriteChemSystArgsException(String txt) {super(txt);}
    } //WriteChemSystArgsException
public static class DataLimitsException extends Exception {
    public DataLimitsException() {}
    public DataLimitsException(String txt) {super(txt);}
} //DataLimitsException

} 