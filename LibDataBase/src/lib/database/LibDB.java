package lib.database;

import java.io.IOException;
import lib.common.MsgExceptn;
import lib.common.Util;
import lib.huvud.Div;

/** Some procedures used witin this package.
 * <br>
 * Copyright (C) 2015-2018 I.Puigdomenech.
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
public class LibDB {
  public static final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();;
  public static final int ELEMENTS = 104;
  /** electron, Hydrogen, Helium, Lithium... */
  public static String[] elementName = new String[113];
  /** e-, H, He, Li ... */
  public static String[] elementSymb = new String[113];
  /** New-line character(s) to substitute "\n" */
  private static final String nl = System.getProperty("line.separator");
  private static final String SLASH = java.io.File.separator;

static { // static initializer
    setElementNames();
} // static initializer

  //<editor-fold defaultstate="collapsed" desc="setElementNames()">
  private static void setElementNames(){
      elementSymb[0]="e-"; elementSymb[1]="H"; elementSymb[2]="He";
      elementSymb[3]="Li"; elementSymb[4]="Be"; elementSymb[5]="B";
      elementSymb[6]="C"; elementSymb[7]="N"; elementSymb[8]="O";
      elementSymb[9]="F"; elementSymb[10]="Ne"; elementSymb[11]="Na";
      elementSymb[12]="Mg"; elementSymb[13]="Al"; elementSymb[14]="Si";
      elementSymb[15]="P"; elementSymb[16]="S"; elementSymb[17]="Cl";
      elementSymb[18]="Ar"; elementSymb[19]="K"; elementSymb[20]="Ca";
      elementSymb[21]="Sc"; elementSymb[22]="Ti"; elementSymb[23]="V";
      elementSymb[24]="Cr"; elementSymb[25]="Mn"; elementSymb[26]="Fe";
      elementSymb[27]="Co"; elementSymb[28]="Ni"; elementSymb[29]="Cu";
      elementSymb[30]="Zn"; elementSymb[31]="Ga"; elementSymb[32]="Ge";
      elementSymb[33]="As"; elementSymb[34]="Se"; elementSymb[35]="Br";
      elementSymb[36]="Kr"; elementSymb[37]="Rb"; elementSymb[38]="Sr";
      elementSymb[39]="Y"; elementSymb[40]="Zr"; elementSymb[41]="Nb";
      elementSymb[42]="Mo"; elementSymb[43]="Tc"; elementSymb[44]="Ru";
      elementSymb[45]="Rh"; elementSymb[46]="Pd"; elementSymb[47]="Ag";
      elementSymb[48]="Cd"; elementSymb[49]="In"; elementSymb[50]="Sn";
      elementSymb[51]="Sb"; elementSymb[52]="Te"; elementSymb[53]="I";
      elementSymb[54]="Xe"; elementSymb[55]="Cs"; elementSymb[56]="Ba";
      elementSymb[57]="La"; elementSymb[58]="Ce"; elementSymb[59]="Pr";
      elementSymb[60]="Nd"; elementSymb[61]="Pm"; elementSymb[62]="Sm";
      elementSymb[63]="Eu"; elementSymb[64]="Gd"; elementSymb[65]="Tb";
      elementSymb[66]="Dy"; elementSymb[67]="Ho"; elementSymb[68]="Er";
      elementSymb[69]="Tm"; elementSymb[70]="Yb"; elementSymb[71]="Lu";
      elementSymb[72]="Hf"; elementSymb[73]="Ta"; elementSymb[74]="W";
      elementSymb[75]="Re"; elementSymb[76]="Os"; elementSymb[77]="Ir";
      elementSymb[78]="Pt"; elementSymb[79]="Au"; elementSymb[80]="Hg";
      elementSymb[81]="Tl"; elementSymb[82]="Pb"; elementSymb[83]="Bi";
      elementSymb[84]="Po"; elementSymb[85]="At"; elementSymb[86]="Rn";
      elementSymb[87]="Fr"; elementSymb[88]="Ra"; elementSymb[89]="Ac";
      elementSymb[90]="Th"; elementSymb[91]="Pa"; elementSymb[92]="U";
      elementSymb[93]="Np"; elementSymb[94]="Pu"; elementSymb[95]="Am";
      elementSymb[96]="Cm"; elementSymb[97]="Bk"; elementSymb[98]="Cf";
      elementSymb[99]="Es"; elementSymb[100]="Fm"; elementSymb[101]="Md";
      elementSymb[102]="No"; elementSymb[103]="Lr"; elementSymb[104]="Rf";
      elementSymb[105]="Db"; elementSymb[106]="Sg"; elementSymb[107]="Bh";
      elementSymb[108]="Hs"; elementSymb[109]="Mt"; elementSymb[110]="Ds";
      elementSymb[111]="Rg"; elementSymb[112]="Cn";
      elementName[0]="Electron"; elementName[1]="Hydrogen"; elementName[2]="Helium";
      elementName[3]="Lithium"; elementName[4]="Beryllium"; elementName[5]="Boron";
      elementName[6]="Carbon"; elementName[7]="Nitrogen"; elementName[8]="Oxygen";
      elementName[9]="Fluorine"; elementName[10]="Neon"; elementName[11]="Sodium";
      elementName[12]="Magnesium"; elementName[13]="Aluminium"; elementName[14]="Silicon";
      elementName[15]="Phosphorus"; elementName[16]="Sulfur"; elementName[17]="Chlorine";
      elementName[18]="Argon"; elementName[19]="Potassium"; elementName[20]="Calcium";
      elementName[21]="Scandium"; elementName[22]="Titanium"; elementName[23]="Vanadium";
      elementName[24]="Chromium"; elementName[25]="Manganese"; elementName[26]="Iron";
      elementName[27]="Cobalt"; elementName[28]="Nickel"; elementName[29]="Copper";
      elementName[30]="Zinc"; elementName[31]="Gallium"; elementName[32]="Germanium";
      elementName[33]="Arsenic"; elementName[34]="Selenium"; elementName[35]="Bromine";
      elementName[36]="Krypton"; elementName[37]="Rubidium"; elementName[38]="Strontium";
      elementName[39]="Yttrium"; elementName[40]="Zirconium"; elementName[41]="Niobium";
      elementName[42]="Molybdenum"; elementName[43]="Technetium"; elementName[44]="Ruthenium";
      elementName[45]="Rhodium"; elementName[46]="Palladium"; elementName[47]="Silver";
      elementName[48]="Cadmium"; elementName[49]="Indium"; elementName[50]="Tin";
      elementName[51]="Antimony"; elementName[52]="Tellurium"; elementName[53]="Iodine";
      elementName[54]="Xenon"; elementName[55]="Caesium"; elementName[56]="Barium";
      elementName[57]="Lanthanum"; elementName[58]="Cerium"; elementName[59]="Praseodymium";
      elementName[60]="Neodymium"; elementName[61]="Promethium"; elementName[62]="Samarium";
      elementName[63]="Europium"; elementName[64]="Gadolinium"; elementName[65]="Terbium";
      elementName[66]="Dysprosium"; elementName[67]="Holmium"; elementName[68]="Erbium";
      elementName[69]="Thulium"; elementName[70]="Ytterbium"; elementName[71]="Lutetium";
      elementName[72]="Hafnium"; elementName[73]="Tantalum"; elementName[74]="Tungsten";
      elementName[75]="Rhenium"; elementName[76]="Osmium"; elementName[77]="Iridium";
      elementName[78]="Platinum"; elementName[79]="Gold"; elementName[80]="Mercury";
      elementName[81]="Thallium"; elementName[82]="Lead"; elementName[83]="Bismuth";
      elementName[84]="Polonium"; elementName[85]="Astatine"; elementName[86]="Radon";
      elementName[87]="Francium"; elementName[88]="Radium"; elementName[89]="Actinium";
      elementName[90]="Thorium"; elementName[91]="Protactinium"; elementName[92]="Uranium";
      elementName[93]="Neptunium"; elementName[94]="Plutonium"; elementName[95]="Americium";
      elementName[96]="Curium"; elementName[97]="Berkelium"; elementName[98]="Californium";
      elementName[99]="Einsteinium"; elementName[100]="Fermium"; elementName[101]="Mendelevium";
      elementName[102]="Nobelium"; elementName[103]="Lawrencium"; elementName[104]="Rutherfordium";
      elementName[105]="Dubnium"; elementName[106]="Seaborgium"; elementName[107]="Bohrium";
      elementName[108]="Hassium"; elementName[109]="Meitnerium"; elementName[110]="Darmstadtium";
      elementName[111]="Roentgenium"; elementName[112]="Copernicium";
  } //setElementNames()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="getElements">
 /** Reads (from the element-component file) the components (reactants) and the elements and
  * stores them in elemCompStringArray.
  * <p>This routine should be called at the program start.
  * <p>elemCompStringArray[0..2] contains the name-of-the-element (e.g. "C"),
  * the formula of the component ("CN-"), and the name of the component ("cyanide").
  * Note that for CN- there can be two entries, one associated to "C" and another to "N";
  * while for EDTA there is perhaps only one entry: with "C".
  * @param parent the owner of error messages
  * @param dbg print debugging info
  * @param dataBasesList list of database names to read
  * @param elemCompStringArray  data is returned here (chemical element,
  * component formula, component name)
  * @return true if all is ok, false if some file is missing, etc  */
  public static boolean getElements(java.awt.Component parent,
          boolean dbg,
          java.util.ArrayList<String> dataBasesList,
          java.util.ArrayList<String[]> elemCompStringArray) {
    if(dbg) {System.out.println("-- getElements(..)");}
    if(dataBasesList == null) {
        MsgExceptn.exception("Programming error: dataBasesList = null in \"getElements\"");
        return false;
    }
    if(elemCompStringArray == null) {
        MsgExceptn.exception("Programming error: elemCompStringArray = null in \"getElements\"");
        return false;
    }
    elemCompStringArray.clear();
    elemCompStringArray.add(new String[]{"H","H+","hydrogen ion"});
    if(dataBasesList.size() <= 0) {
        System.out.println("---- Warning: dataBasesList.size() = 0 in \"getElements\"");
        return true;
    }
    if(dbg) {System.out.println("Reading metals and ligands from "+dataBasesList.size()+" data bases.");}
    int i, db = 0;
    boolean binaryDB;
    String elN;

    // --- loop through the databases
    while (db < dataBasesList.size()) {
      String dbName = dataBasesList.get(db);
      java.io.File dbFile = new java.io.File(dbName);
      java.io.File elemFile;
      binaryDB = dbName.toLowerCase().endsWith(".db");
      if(binaryDB) {
        elemFile = new java.io.File(Div.getFileNameWithoutExtension(dbName)+".elb");
        elN = elemFile.getName();
        if(dbFile.exists() && !elemFile.exists()) {
            String msg = "Can not find the \"element\"-file"+nl+
                    "for database: \""+dbFile.getName()+"\"."+nl+nl+
                    "Expected to find file \""+elN+"\".";
            if(parent != null && !parent.isVisible()) {parent.setVisible(true);}
            MsgExceptn.showErrMsg(parent,msg,1);
            return false;
        } else
        if(dbFile.exists() && !elemFile.canRead()) {
            String msg = "Error: can not read the \"element\"-file"+nl+
                    "for database: \""+dbFile.getName()+"\"."+nl+nl+
                    "You might not have permissions to read file \""+elN+"\".";
            if(parent != null && !parent.isVisible()) {parent.setVisible(true);}
            MsgExceptn.showErrMsg(parent, msg, 1);
            return false;
        } else
        if(!dbFile.exists() && !elemFile.canRead()) {
            if(dbg) {System.out.println("-- Warning: files \""+dbFile.getName()+"\" and \""+elN+"\" do not exist.");}
            db++;
            continue;
        }
      } else { // not binaryDB
        String elemFileN = AddDataElem.getElemFileName(dbg, parent, dbName);
        if(elemFileN == null || elemFileN.trim().length() <=0) {return false;}
        elemFile = new java.io.File(elemFileN);
        elN = elemFile.getName();
        if(dbFile.exists()) { //if the data file exists, error if the element file is not there
            if(!elemFile.exists()) {
                String msg = "Could not find the \"element\"-file"+nl+
                    "for database: \""+dbFile.getName()+"\"."+nl+nl+
                    "The file \""+elN+"\" will be created.";
                if(parent != null && !parent.isVisible()) {parent.setVisible(true);}
                MsgExceptn.showErrMsg(parent, msg, 1);
                try {AddDataElem.elemCompAdd_Update(dbg, parent, dbName, elemFileN,
                        elemCompStringArray,  new java.util.ArrayList<String[]>());}
                catch (AddDataElem.AddDataException ex) {
                    if(parent != null && !parent.isVisible()) {parent.setVisible(true);}
                    MsgExceptn.showErrMsg(parent, ex.getMessage(),1);
                    return false;
                }
            } else
            if(!elemFile.canRead()) {
                String msg ="Error: can not rean the \"element\"-file"+nl+
                    "for database: \""+dbFile.getName()+"\"."+nl+nl+
                    "You might not have permissions to read file \""+elN+"\".";
                if(parent != null && !parent.isVisible()) {parent.setVisible(true);}
                MsgExceptn.showErrMsg(parent,msg,1);
                return false;
            }
        } else { //!dbFile.exists()
            if(!elemFile.canRead()) {
                if(dbg) {System.out.println("-- Warning: files \""+dbFile.getName()+"\" and \""+elN+"\" do not exist.");}
                db++;
                continue;
            }
        } // dbFile.exists() ?
      } // binaryDB ?
      if(dbg) {System.out.println("  File \""+elemFile+"\"");}
      try {
        if(binaryDB) {
            readElemFileBinary(dbg, elemFile, elemCompStringArray);
        } else { //!binaryDB
            readElemFileText(elemFile, elemCompStringArray);
        }
      }
      catch (ReadElemException ex) {
        if(parent != null && !parent.isVisible()) {parent.setVisible(true);}
        MsgExceptn.showErrMsg(parent,ex.getMessage(),1);
        System.err.println("Removing file \""+dbName+"\""+nl+"from the database list");
        for(i=0; i < dataBasesList.size(); i++) {
            if(dataBasesList.get(i).equals(dbName)) {dataBasesList.remove(i); break;}
        }
      }
      db++;
    } //loop through databases
    return true;
  } //getElements()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="readElemFileText">
 /** Reads (from a single element database file) the components and the elements and
  * stores them either in elemCompStringArray or in elemCompString.
  * <p>elemCompStringArray[0..2] contains the name-of-the-element (e.g. "C"),
  * the formula of the component ("CN-"), and the name of the component ("cyanide").
  * For CN- there can be two entries associated to "C" and "N";
  * while for EDTA there is perhaps only one entry with "C".
  * @param elemFile
  * @param elemCompStringArray  data is returned here (chemical element,
  * component formula, component name)
  * @throws ReadElemException  */
  public static void readElemFileText(
          java.io.File elemFile,
          java.util.ArrayList<String[]> elemCompStringArray) throws ReadElemException {
    if(elemFile == null) {throw new ReadElemException("Input file is \"null\"!");}
    String elN = elemFile.getName();
    if(elN == null) {throw new ReadElemException("Input file is \"null\"!");}
    elN = elemFile.getAbsolutePath();
    if(!elemFile.exists()) {throw new ReadElemException("Input file does not exist"+nl+"\""+elN+"\"");}
    if(!elemFile.canRead()) {throw new ReadElemException("Can not read input file"+nl+"\""+elN+"\"");}
    java.io.BufferedReader br;
    try {br = new java.io.BufferedReader(new java.io.FileReader(elemFile));}
    catch (java.io.FileNotFoundException ex) {throw new ReadElemException(ex.getMessage());}
    String line = null;
    int lineNbr = 0;
    int i,j,k,n;
    boolean found;
    String t1, t2;
    String[] elemCompLocal = null;
    String element;
    java.util.ArrayList<String> aList;
    try {
      while((line = br.readLine()) != null) { //--- read all elements (lines) in the input file
        lineNbr++;
        if(line.length()<=0 || line.trim().startsWith("/")) {continue;}
        try {aList = CSVparser.splitLine(line);} // this will remove enclosing quotes
        catch (CSVparser.CSVdataException ex) {
            throw new ReadTxtElemEx("Error (CSVdataException) "+ex.getMessage()+nl+
                    "while reading line nbr "+lineNbr+":"+nl+
                    "    "+line+nl+"from file:"+nl+"   \""+elN+"\"");
        }
        element = aList.get(0);
        if(element == null || element.length() <=0) {
            throw new ReadTxtElemEx("Error: empty element in line"+nl+
                    "   "+line+nl+"in file"+nl+"   \""+elN+"\"");
        }
        try {n = Integer.parseInt(aList.get(1));}
        catch(NumberFormatException ex) {
            throw new ReadTxtElemEx("Error reading an integer from \""+aList.get(1)+"\" in line"+nl+
                    "   "+line+nl+"in file"+nl+"   \""+elN+"\"");
        }
        if(n>0) {
            if(aList.size()<3 || n*2 > (aList.size()-1)) {
                String msg = "Error: too litle data"+nl+"while reading line"+nl+
                    "    "+line+nl+"from file:"+nl+"   \""+elN+"\"";
                throw new ReadTxtElemEx(msg);
            }
            found = false;
            for(i=0; i< elementSymb.length; i++) {
                if(element.equals(elementSymb[i])) {found = true; break;}
            }
            if(!found) {element = "XX";}
            for(i=0; i<n; i++) {
                t1 = aList.get((i*2)+2);
                if(t1.length() <=0) {
                    String msg = "Error: empty reactant"+nl+"while reading line"+nl+
                        "    "+line+nl+"from file:"+nl+"   \""+elN+"\"";
                    throw new ReadTxtElemEx(msg);
                }
                j = (i*2)+3;
                if(j < aList.size()) {t2 = aList.get(j);} else {t2 = "";}
                if(t1.startsWith("@") && elemCompStringArray.size() >0) {
                  //--- remove an existing ligand/metal
                  t1 = t1.substring(1); //remove @
                  j = elemCompStringArray.size();
                  while(j >0) {
                        j--;
                        if(elemCompStringArray.get(j)[1].equals(t1)) {elemCompStringArray.remove(j); break;}
                  }
                } //starts with "@"
                else
                { //does not start with "@"
                  int jFound = -1;
                  k = elemCompStringArray.size();
                  for(j=0; j < k; j++) {
                    if(elemCompStringArray.get(j)[0].equals(element) && elemCompStringArray.get(j)[1].equals(t1)) {
                        jFound = j; 
                        elemCompLocal = elemCompStringArray.get(jFound);
                        break;
                    }
                  } //for j
                  if(jFound > -1) {
                    if(t2.length()>0) {
                        elemCompLocal[2] = t2;
                        elemCompStringArray.set(jFound,elemCompLocal);
                    }
                  } else { //jFound =-1
                    elemCompLocal = new String[3];
                    elemCompLocal[0] = element;
                    elemCompLocal[1] = t1;
                    elemCompLocal[2] = t2;
                    elemCompStringArray.add(elemCompLocal);
                  } //jFound?
                } //starts with "@"?
            } //for i
        } //if n>0
        else {
            throw new ReadTxtElemEx("Error reading \""+aList.get(1)+"\" (must be a number >0)"+nl+
                    "in line"+nl+"   "+line+nl+"in file"+nl+"   \""+elN+"\"");
        } //if n<=0
      } //while loop through elements
    } //try
    catch (ReadTxtElemEx ex) {
        throw new ReadElemException(ex.getMessage());
    }
    catch (java.io.IOException ex) {
        MsgExceptn.exception(Util.stack2string(ex));
        throw new ReadElemException("Error "+ex.getMessage()+nl+"while reading line"+nl+
                    "    "+line+nl+"from file:"+nl+"   \""+elN+"\"");
    }
    finally {
        try {br.close();}
        catch (java.io.IOException ex) {
            throw new ReadElemException("Error "+ex.getMessage()+nl+
                "closing file:"+nl+"   \""+elN+"\"");
        }
    }
    //return;
  } //readElemFileText
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="readElemFileBinary">
 /** Read (from a single element database file) the components and the elements and
  * store them either in elemCompStringArray or in elemCompString.
  * <p>elemCompStringArray[0..2] contains the name-of-the-element (e.g. "C"),
  * the formula of the component ("CN-"), and the name of the component ("cyanide").
  * For CN- there can be two entries associated to "C" and "N";
  * while for EDTA there is perhaps only one entry with "C".
  * @param dbg if true then some output is written to System.out
  * @param elemFile
  * @param elemCompStringArray  data is returned here (chemical element,
  * component formula, component name)
  * @throws ReadElemException  */
  private static void readElemFileBinary(boolean dbg,
          java.io.File elemFile,
          java.util.ArrayList<String[]> elemCompStringArray) throws ReadElemException {
    if(elemFile == null) {throw new ReadElemException("Input file is \"null\"!");}
    String elN = elemFile.getName();
    if(elN == null) {throw new ReadElemException("Input file is \"null\"!");}
    if(!elemFile.exists()) {throw new ReadElemException("Input file does not exist"+nl+"\""+elN+"\"");}
    if(!elemFile.canRead()) {throw new ReadElemException("Can not read input file"+nl+"\""+elN+"\"");}
    java.io.DataInputStream dis = null;
    try {dis = new java.io.DataInputStream(new java.io.FileInputStream(elemFile));}
    catch (java.io.FileNotFoundException ex) {
        if(dis != null) {try {dis.close();} catch (java.io.IOException e) {}}
        throw new ReadElemException("Error "+ex.getMessage()+nl+"   in \"readElemFileBinary\"");
    }
    int i,j,k,n;
    boolean found;
    String t1, t2;
    if(dbg) {System.out.println("  readElemFileBinary("+elemFile+")");}
    String[] elemCompLocal = null;
    String element;
    boolean first = true;
    try {
      while(true) { //--- loop reading all elements in the input file
        element = dis.readUTF();
        if(element == null || element.length() <=0) {
            if(first){
                throw new ReadElemException("Error in \"readElemFileBinary\""+nl+
                            "could not read the first element from file:"+nl+"   \""+elN+"\".");
            }
            break;
        }
        first = false;
        n = dis.readInt();
        if(n>0) {
            found = false;
            for(i=0; i< elementSymb.length; i++) {
                if(element.equals(elementSymb[i])) {found = true; break;}
            }
            if(!found) {element = "XX";}
            for(i=0; i<n; i++) {
                t1 = dis.readUTF(); t2 = dis.readUTF();
                if(t1.startsWith("@") && elemCompStringArray.size() >0) {
                  //--- remove an existing ligand/metal
                  t1 = t1.substring(1); //remove @
                  j = elemCompStringArray.size();
                  while(j >0) {
                    j--;
                    if(elemCompStringArray.get(j)[1].equals(t1)) {elemCompStringArray.remove(j); break;}
                  }
                } //starts with "@"
                else
                { //does not start with "@"
                  int jFound = -1;
                  k = elemCompStringArray.size();
                  for(j=0; j < k; j++) {
                    if(elemCompStringArray.get(j)[0].equals(element) && elemCompStringArray.get(j)[1].equals(t1)) {
                        jFound = j; 
                        elemCompLocal = elemCompStringArray.get(jFound);
                        break;
                    }
                  } //for j
                  if(jFound > -1) {
                    if(t2.length()>0) {
                        elemCompLocal[2] = t2;
                        elemCompStringArray.set(jFound,elemCompLocal);
                    }
                  } else { //jFound =-1
                    elemCompLocal = new String[3];
                    elemCompLocal[0] = element;
                    elemCompLocal[1] = t1;
                    elemCompLocal[2] = t2;
                    elemCompStringArray.add(elemCompLocal);
                  } //jFound?
                } //starts with "@"?
            } //for i
        } //if n>0
      } //while loop through elements
    } //try
    catch (java.io.EOFException eof) {
        if(first) {
            throw new ReadElemException("Error in \"readElemFileBinary\""+nl+
                "could not read the first element from file:"+nl+"   \""+elN+"\".");
        }
    }
    catch (IOException ex) {
        throw new ReadElemException(Util.stack2string(ex));
    } catch (ReadElemException ex) {
        throw new ReadElemException(Util.stack2string(ex));
    }
    finally {try{dis.close();} catch (java.io.IOException ex) {}}
    //return;
  } //readElemFileBinary
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="checkDataBasesList">
  /** Check that the database names are not empty, the files exist
   * and can be read, and that there are no duplicates
   * @param parent
   * @param dataBasesList the databases
   * @param dbg ture if warnings and error messages are to be printed */
  public static synchronized void checkDataBasesList(
          java.awt.Component parent,
          java.util.ArrayList<String> dataBasesList, boolean dbg) {
    if(dataBasesList == null || dataBasesList.size() <=0) {return;}
    //--- check for non-existing files
    int nbr = dataBasesList.size();
    String dbName;
    int i=0;
    while(i < nbr) {
        dbName = dataBasesList.get(i);
        if(dbName != null && dbName.length() >0) {
          if(isDBnameOK(parent, dbName, dbg)) {i++; continue;}
        } else {
          if(dbg) {System.out.println("Error: found an empty database name in position "+i);}
        }
        dataBasesList.remove(i);
        nbr--;
    } //while i < nbr
    //--- check for duplicates
    i=0; int j;
    if(nbr >1) {
        boolean found;
        while(i < nbr) {
            dbName = dataBasesList.get(i);
            found = false;
            for(j=i+1; j < nbr; j++) {
                if(dbName.equalsIgnoreCase(dataBasesList.get(j))) {found = true; break;}
            }
            if(!found) {i++; continue;}
            if(dbg) {System.out.println("Warning: found duplicated database: \""+dbName+"\"");}
            dataBasesList.remove(j);
            nbr--;
    } //while i < nbr
    } // nbr >1
  } //checkDataBasesList
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="isDBnameOK">
  /**  Check if a database exists and can be read
   * @param parent
   * @param dBname a database
   * @param dbg set to <code>true</code> to print error messages to <code>System.err</code>
   * @return true if the database exists and can be read, false otherwise */
  public static boolean isDBnameOK(
          java.awt.Component parent,
          String dBname, boolean dbg) {
    boolean ok = true;
    if(dBname != null && dBname.length() >0) {
        java.io.File dbf = new java.io.File(dBname);
        if(!dbf.exists()) {
          MsgExceptn.msg("Note: database file does not exist:"+nl+dBname);
          ok = false;
        } else if(!dbf.canRead()) {
          MsgExceptn.showErrMsg(parent, "Error: no read permission for database:"+nl+dBname, 1);
          ok = false;
        }
    } else {ok = false;}
    return ok;
  } //isDBnameOK
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="getTxtComplex">
  /** Read a <code>Complex</code> from the text file "connected" to a BufferedReader
   * @param br
   * @return
   * @throws ReadTxtCmplxException
   * @throws EndOfFileException */
  public static Complex getTxtComplex(java.io.BufferedReader br)
          throws ReadTxtCmplxException, EndOfFileException  {
    if(br == null) {return null;}
    Complex cmplx;
    String line, line2;
    try{
        while ((line = br.readLine()) != null){
            if(line.trim().length()<=0 || line.trim().toUpperCase().startsWith("COMPLEX")
                    || line.trim().startsWith("/")) {continue;}
            if(line.toLowerCase().contains("lookup")) { // lookUpTable
                for(int i = 0; i < 5; i++) {
                    line2 = br.readLine();
                    if(line2 != null) {line = line + nl + line2;}
                }
            }
            try{cmplx = Complex.fromString(line);}
            catch (Complex.ReadComplexException ex) {throw new ReadTxtCmplxException(ex.getMessage());}
            if(cmplx != null) {return cmplx;}
        } // while
    }
    catch (java.io.IOException ex) {throw new ReadTxtCmplxException(ex.toString());}
    //if(line == null) 
    throw new EndOfFileException();
  } //getTxtComplex(rd)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="writeTxtComplex">
  public static void writeTxtComplex(java.io.PrintWriter pw, Complex cmplx)
            throws WriteTxtCmplxException {
    if(pw == null) {throw new WriteTxtCmplxException(nl+"Error: PrintWriter = null in \"writeTxtComplex\"");}
    if(cmplx == null) {
        throw new WriteTxtCmplxException(nl+"Error: cmplx = null in \"writeTxtComplex\"");
    }
    if(cmplx.name == null || cmplx.name.length() <=0) {
        throw new WriteTxtCmplxException(nl+"Error: empty cmplx name in \"writeTxtComplex\"");
    }
    try {
        pw.print(cmplx.toString());
        pw.println();
    } catch (Exception ex) {
        throw new WriteTxtCmplxException(nl+"Error: "+ex.getMessage()+nl+" in \"writeTxtComplex\"");
    }
  } //writeTxtComplex
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="getBinComplex">
  /** Read a <code>Complex</code> from the binary file "connected" to a DataInputStream instance.
   * Returns null after a java.io.EOFException.
   * @param dis
   * @return
   * @throws ReadBinCmplxException    */
  public static Complex getBinComplex(java.io.DataInputStream dis)
            throws ReadBinCmplxException {
    if(dis == null) {return null;}
    Complex cmplx = new Complex();
    String txt;
    int nTot, nr;
    boolean thereisHplus = false;
    double n_H, deltaH, deltaCp;
    StringBuilder nowReading = new StringBuilder();
    nowReading.replace(0, nowReading.length(), "Name of complex");
    try{
        cmplx.name = dis.readUTF();
        if(cmplx.name == null) {throw new ReadBinCmplxException(nl+"Error: complex name = \"null\".");}
        nowReading.replace(0, nowReading.length(), "logK for `"+cmplx.name+"´");
        cmplx.constant = dis.readDouble();
        nowReading.replace(0, nowReading.length(), "delta-H for `"+cmplx.name+"´ (or \"analytic - lookUp\" flag)");
        deltaH = dis.readDouble();
        if(deltaH == Complex.ANALYTIC) {
            // analytic equation
            cmplx.analytic = true;
            cmplx.lookUp = false;
            nowReading.replace(0, nowReading.length(), "max temperature for `"+cmplx.name+"´");
            cmplx.tMax = dis.readDouble();
            for(int i = 0; i < cmplx.a.length; i++) {
                nowReading.replace(0, nowReading.length(), "parameter a[+"+i+"] for `"+cmplx.name+"´");
                cmplx.a[i] = dis.readDouble();
            }
        } else if(deltaH == Complex.LOOKUP) {
            // look-up table
            cmplx.lookUp = true;
            cmplx.analytic = false;
            nowReading.replace(0, nowReading.length(), "max temperature for `"+cmplx.name+"´");
            cmplx.tMax = dis.readDouble();
        } else { // delta-H and delta-Cp
            cmplx.analytic = false;
            cmplx.lookUp = false;
            nowReading.replace(0, nowReading.length(), "delta-Cp for `"+cmplx.name+"´");
            deltaCp = dis.readDouble();
            cmplx.a = Complex.deltaToA(cmplx.constant, deltaH, deltaCp);
            cmplx.tMax = 25.;
            if(deltaH != Complex.EMPTY) {
                cmplx.tMax = 75.;
                if(deltaCp != Complex.EMPTY) {cmplx.tMax = 150.;}
            }
        }
        nowReading.replace(0, nowReading.length(), "reactant nbr.1 or nbr of reactants for `"+cmplx.name+"´");
        txt = dis.readUTF();
        try {nTot = Integer.parseInt(txt);} catch (Exception ex) {nTot = -1;}
        if(nTot <= 0) { // nTot < 0  ---- old format ----
            nr = 0;
            for(int i=0; i < Complex.NDIM; i++) {
                if(i>0) { 
                    nowReading.replace(0, nowReading.length(), "reactant nbr."+(i+1)+" for `"+cmplx.name+"´");
                    txt = dis.readUTF();
                }
                if(txt.trim().isEmpty()) {dis.readDouble(); continue;}
                cmplx.reactionComp.add(txt);
                nr++;
                nowReading.replace(0, nowReading.length(), "stoichiometric coeff. nbr."+(i+1)+" for `"+cmplx.name+"´");
                cmplx.reactionCoef.add(dis.readDouble());
                if(Util.isProton(cmplx.reactionComp.get(nr-1))) {
                    thereisHplus = true;
                    n_H = cmplx.reactionCoef.get(nr-1);
                }
            } // for i
            nowReading.replace(0, nowReading.length(), "nbr H+ for `"+cmplx.name+"´");
            n_H = dis.readDouble();
            if(!thereisHplus && Math.abs(n_H) > 0.00001) {cmplx.reactionComp.add("H+"); cmplx.reactionCoef.add(n_H);}
        } else { // nTot >0  ---- new format ----
            for(int i=0; i < nTot; i++) {
                nowReading.replace(0, nowReading.length(), "reactant nbr."+(i+1)+" for `"+cmplx.name+"´");
                cmplx.reactionComp.add(dis.readUTF());
                nowReading.replace(0, nowReading.length(), "stoichiometric coeff. nbr."+(i+1)+" for `"+cmplx.name+"´");
                cmplx.reactionCoef.add(dis.readDouble());
            }
        }
        nowReading.replace(0, nowReading.length(), "reference for `"+cmplx.name+"´");
        cmplx.reference = dis.readUTF();
        cmplx.comment = dis.readUTF();
        if(cmplx.lookUp) { // read look-up table
            for(int i=0; i < cmplx.logKarray.length; i++) {
                cmplx.logKarray[i] = new float[14];
                for(int j=0; j < cmplx.logKarray[i].length; j++) {cmplx.logKarray[i][j]=Float.NaN;}
                if(i == 0) {nr = 9;} else if(i == 1) {nr = 11;} else {nr = 14;}
                for(int j=0; j < nr; j++) {
                    nowReading.replace(0, nowReading.length(), "logKarray["+i+"]["+j+"] for `"+cmplx.name+"´");
                    cmplx.logKarray[i][j] = dis.readFloat();
                }
            }
        }
    }
    catch (java.io.EOFException eof) {return null;}
    catch (IOException ex) {
        throw new ReadBinCmplxException(nl+"Error: "+ex.getMessage()+nl+"while reading \""+nowReading.toString()+"\"");
    } catch (ReadBinCmplxException ex) {
        throw new ReadBinCmplxException(nl+"Error: "+ex.getMessage()+nl+"while reading \""+nowReading.toString()+"\"");
    }
    if(cmplx.name == null) {return null;}
    return cmplx;
  } //getBinComplex(dis)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="writeBinCmplx">
  public static void writeBinCmplx(java.io.DataOutputStream ds, Complex cmplx)
          throws WriteBinCmplxException {
    if(ds == null) {throw new WriteBinCmplxException(nl+"Error: DataOutputStream = null in \"writeBinCmplx\"");}
    if(cmplx == null) {throw new WriteBinCmplxException(nl+"Error: cmplx = null in \"writeBinCmplx\"");}
    if(cmplx.name == null || cmplx.name.length() <=0) {
        throw new WriteBinCmplxException(nl+"Error: empty cmplx in \"writeBinCmplx\"");
    }
    try{
      ds.writeUTF(cmplx.name);
      ds.writeDouble(cmplx.constant);
      if(cmplx.analytic) {
          // analytic equation
          ds.writeDouble(Complex.ANALYTIC);
          ds.writeDouble(cmplx.tMax);
          for(int i = 0; i < cmplx.a.length; i++) {ds.writeDouble(cmplx.a[i]);}
      } else if(cmplx.lookUp) {
          // look-up Table
          ds.writeDouble(Complex.LOOKUP);
          ds.writeDouble(cmplx.tMax);
      } else {
          // delta-H and delta-Cp
          ds.writeDouble(cmplx.getDeltaH());
          ds.writeDouble(cmplx.getDeltaCp());
      }
      int nTot = Math.min(cmplx.reactionComp.size(),cmplx.reactionCoef.size());
      ds.writeUTF(String.valueOf(nTot));
      for(int i = 0; i < nTot; i++) {
        ds.writeUTF(cmplx.reactionComp.get(i));
        ds.writeDouble(cmplx.reactionCoef.get(i));
      }
      ds.writeUTF(cmplx.reference);
      if(cmplx.comment != null && cmplx.comment.length() >=0) {ds.writeUTF(cmplx.comment);}
      else {ds.writeUTF("");}
      if(cmplx.lookUp) { // write look-up Table
          int nr;
          for(int i=0; i < cmplx.logKarray.length; i++) {
            if(i == 0) {nr = 9;} else if(i == 1) {nr = 11;} else {nr = 14;}
            for(int j=0; j < nr; j++) {ds.writeFloat(cmplx.logKarray[i][j]);}
          }
      }
    } catch (java.io.IOException ex) {throw new WriteBinCmplxException(nl+"Error: "+ex.getMessage()+nl+"in \"writeBinCmplx\"");}
  } //writeBinCmplx(DataOutputStream, complex)
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="getDiagramProgr">
  /** Check if the program "diagramProgr" exists. If "prog" is the name 
   * without extension, then if both "prog.jar" and "prog.exe" exist,
   * then the jar-file name is returned. If none exists, <code>null</code> is returned.
   * This means that if the user selects an "exe" file, the corresponding
   * "jar" file, if it exists, will be anyway executed.
   * @param diagramProgr a program name with existing directory path.
   * With or without file extension.
   * @return the program name with extension ".jar" if a jar-file exists, otherwise
   * with the extension ".exe" if the exe-file exists, otherwise <code>null</code>.
   * Note that the directory is not changed. 
   */
  public static String getDiagramProgr(String diagramProgr) {
    if(diagramProgr == null || diagramProgr.trim().length() <= 0) {return null;}
    java.io.File f = new java.io.File(diagramProgr);
    String os = System.getProperty("os.name").toLowerCase();
    if(f.isDirectory() && !os.startsWith("mac os")) {return null;}
    String dir = f.getParent();
    if(dir == null || dir.trim().length() <=0)  {return null;}
    String prog = f.getName();
    if(prog == null || prog.trim().length() <=0) {return null;}
    f = new java.io.File(dir);
    if(!f.exists() || !f.isDirectory()) {return null;}
    dir = f.getAbsolutePath();
    if(dir != null && dir.endsWith(SLASH)) {dir = dir.substring(0,dir.length()-1);}
    // get "prog" without extension
    String progName;
    if(prog.length() == 1) {
        if(prog.equals(".")) {return null;} else {progName = prog;}
    } else {
        int dot = prog.lastIndexOf(".");
        if(dot <= 0) {
            progName = prog; //no extension in name
        } else {
            progName = prog.substring(0,dot);
        }
    }
    //
    String name;
    name = dir + SLASH + progName + ".jar";
    f = new java.io.File(name);
    if(f.exists() && f.isFile()) {
        return name;
    } else if(os.startsWith("windows")) {
        name = dir + SLASH + progName + ".exe";
        f = new java.io.File(name);
        if(f.exists() && f.isFile()) {return name;}
    } else if(os.startsWith("mac os")) {
        name = dir + SLASH + progName + ".app";
        f = new java.io.File(name);
        if(f.exists()) {return name;}
    }
    return null;
  }
  //</editor-fold>

  public static class ReadElemException extends Exception {
    public ReadElemException() {super();}
    public ReadElemException(String txt) {super(txt);}
  }
  private static class ReadTxtElemEx extends Exception {
    public ReadTxtElemEx() {super();}
    public ReadTxtElemEx(String txt) {super(txt);}
  }
  public static class ReadTxtCmplxException extends Exception {
    public ReadTxtCmplxException() {super();}
    public ReadTxtCmplxException(String txt) {super(txt);}
  }
  public static class EndOfFileException extends Exception {
    public EndOfFileException() {super();}
    public EndOfFileException(String txt) {super(txt);}
  }
  public static class ReadBinCmplxException extends Exception {
    public ReadBinCmplxException() {super();}
    public ReadBinCmplxException(String txt) {super(txt);}
  }
  public static class WriteTxtCmplxException extends Exception {
    public WriteTxtCmplxException() {super();}
    public WriteTxtCmplxException(String txt) {super(txt);}
  }
  public static class WriteBinCmplxException extends Exception {
    public WriteBinCmplxException() {super();}
    public WriteBinCmplxException(String txt) {super(txt);}
  }

}
