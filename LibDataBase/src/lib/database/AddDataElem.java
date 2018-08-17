package lib.database;

import lib.common.MsgExceptn;
import lib.huvud.Div;

/** Procedures dealing with reactants and chemical elements.
 * Used for example by "FrameAddData"
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
public class AddDataElem {
  /** New-line character(s) to substitute "\n" */
  private static final String nl = System.getProperty("line.separator");

 //<editor-fold defaultstate="collapsed" desc="elemFileCheck">
 /** For a given <b>text</b> database "dbName", check if the corresponding element-reactant
  * file exists. If it does not exist, create it, including any reactants
  * found in "elemComp". If it exists, check that all reactants in "dbName"
  * are found in the element file.
  * @param dbg if true, extra output messages will be printed
  * @param parent used to display dialogs, if needed.
  * @param addFile the name, with complete path, of the file with data.
  * It may not exist, or it may be null.
  * @param elemComp Array list of String[3] objects with the reactants (components):<br>
  *  [0] contains the element name (e.g. "C"),<br>
  *  [1] the component formula ("CN-" or "cit-3"),<br>
  *  [2] the component name ("cyanide" or null); not really needed, but used to help the user
  * @return the name of the element-reactant file if everything is ok; <code>null</code> otherwise  */
  public static String elemFileCheck(final boolean dbg,
          java.awt.Component parent,
          String addFile,
          java.util.ArrayList<String[]> elemComp) {
    if(addFile == null || addFile.trim().length() <=0) {
        if(dbg){System.out.println("--- elemFileCheck(); addFile is \"null\" or empty");}
        return null;
    }
    if(dbg) {System.out.println("--- elemFileCheck("+addFile+")");}
    java.io.File af = new java.io.File(addFile);
    String addFileEle = getElemFileName(dbg, parent, addFile);
    if(addFileEle == null) {
        if(dbg){System.out.println("--- elemFileCheck(); could not get an element file name for file: "+addFile);}
        return null;
    }
    java.io.File afE = new java.io.File(addFileEle);
    if(!afE.exists()) {
        System.out.println("--- The \"element\"-file for database: \""+af.getName()+"\" does not exist.");
        String msg = "Could not find the \"element\"-file"+nl+
                "for database: \""+af.getName()+"\"."+nl+nl+
                "The file \""+afE.getName()+"\" will be created.";
        Object[] opt = {"OK", "Cancel"};
        int m = javax.swing.JOptionPane.showOptionDialog(parent,msg,
                        "Missing file", javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[0]);
        if(m != javax.swing.JOptionPane.YES_OPTION) {
                if(dbg) {System.out.println("Cancelled by the user");}
                return null;
        }
    }
    //--- List of reactants and their chemical elements
    java.util.ArrayList<String[]> elemCompNewFile = new java.util.ArrayList<String[]>();
    //--- Are reactants in "addFile" not found in "addFileEle"?
    //    if so, are they in "elemComp"?
    //    if new reactants are found the file "addFileEle" is automatically saved
    try{elemCompAdd_Update(dbg, parent, addFile, addFileEle, elemComp, elemCompNewFile);}
        catch (AddDataException ex) {
            if(parent != null) {if(!parent.isVisible()) {parent.setVisible(true);}}
            MsgExceptn.showErrMsg(parent,ex.getMessage(),1);
            return null;
        }
    return addFileEle;
  }
 //</editor-fold>

 //<editor-fold defaultstate="collapsed" desc="getElemFileName">
/** returns the name, with complete path, for the
 * "chemical element and reactant" file that corresponds to the
 * database "addFile".  Note that the file might not exist.
 * @param dbg if true, extra output messages will be printed
 * @param parent
 * @param addFile the name, with complete path, of the database file
 * @return a file name with complete path. It is equal to "addFile" but
 * with extension either "elb", "elt" or "elm". For a text database:<br>
 *  -- If both "elt" and "elm" files exist, or none exists, the returned extension is "elt".<br>
 *  -- If "elm" exists, and "elt" does not exist, "elm" is returned<br>
 *  -- Note: if both files exist, a warning dialog is displayed. If the user selects
 * "cancel", <code>null</code> is returned, if the user selects "ok" the returned extension is "elt"<br>
 *  -- If "addFile" is null or empty, or if its
 * extension is either "elb", "elt" or "elm", then <code>null</code> is returned  */
  public static String getElemFileName(final boolean dbg,
          java.awt.Component parent,
          final String addFile) {
    if(addFile == null || addFile.trim().length() <= 0) {return null;}
    if(Div.getFileNameExtension(addFile).equalsIgnoreCase("elt")
            || Div.getFileNameExtension(addFile).equalsIgnoreCase("elm")
            || Div.getFileNameExtension(addFile).equalsIgnoreCase("elb")) {
        String msg = "File \""+addFile+"\":"+nl+"can not have extension \"elb\", \"elt\", nor \"elm\".";
        MsgExceptn.showErrMsg(parent,msg,1);
        return null;
    }
    String addFileEle;
    boolean binary = Div.getFileNameExtension(addFile).equalsIgnoreCase("db");
    if(binary) {
        addFileEle = Div.getFileNameWithoutExtension(addFile)+".elb";
    } else {
        addFileEle = Div.getFileNameWithoutExtension(addFile)+".elt";
        java.io.File elemFile = new java.io.File(addFileEle);
        String elN = elemFile.getName();
        if(!elemFile.exists()) {
            addFileEle = Div.getFileNameWithoutExtension(addFile)+".elm";
            elemFile = new java.io.File(addFileEle);
            if(!elemFile.exists()) {
                addFileEle = Div.getFileNameWithoutExtension(addFile)+".elt";
            }
        } else { // "*.elt" exists
            //check if both "*.elb" and "*.elt" exist
            String elN2 = Div.getFileNameWithoutExtension(addFile)+".elm";
            java.io.File elemFile2 = new java.io.File(elN2);
            if(elemFile2.exists()) {
                Object[] opt = {"OK", "Cancel"};
                elN2 = elemFile2.getName();
                if(dbg) {System.out.println("   Warning: both \""+elN+"\""+nl+"  and \""+elN2+"\""+nl+"  exist.");}
                int m = javax.swing.JOptionPane.showOptionDialog(parent,"Note:  both  \""+elN+"\""+nl+
                    "and  \""+elN2+"\"  exist."+nl+nl+"Only file \""+elN+"\" will be used."+nl+" ",
                    "Warning", javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[0]);
                if(m != javax.swing.JOptionPane.YES_OPTION) {
                    if(dbg) {System.out.println("Cancelled by the user");}
                    return null;
                }
            } //both files exist
        } // "*.elt" exists
    } // text or binary?
    return addFileEle;
  }
 //</editor-fold>

 //<editor-fold defaultstate="collapsed" desc="addFileEle_Read()">
/** Reads the text element-file "addFileEle", and stores the data in "elemCompAdd"
 * @param dbg if true, extra output messages will be printed
 * @param addFileEle the name, with complete path, of the chemical element and
 * reactant file (that corresponds to the database "addFile").
 * Note: if the file "addFileEle" does not exist, "elemCompAdd" will only contain H2O, H+ and e-.
 * @param elemCompAdd This array list will contain the components (reactants) and
 * the corresponding chemical elements in the "add" file. Contains String[3] objects:<br>
 *  [0] contains the element name (e.g. "C"),<br>
 *  [1] the component formula ("CN-" or "cit-3"),<br>
 *  [2] the component description ("cyanide" or null); not really needed, but used to help the user
 * @see ProgramDataDB#elemComp elemComp
 * @see LibDB#readElemFileText(java.io.File, java.util.ArrayList) readElemFileText
 * @see AddDataElem#elemCompAdd_Update(boolean, java.awt.Component, java.lang.String, java.lang.String, java.util.ArrayList, java.util.ArrayList)  elemCompAdd_Update
 * @throws lib.database.AddDataElem.AddDataException  */
  public static void addFileEle_Read(final boolean dbg,
          final String addFileEle,
          java.util.ArrayList<String[]> elemCompAdd)
          throws AddDataException {
    if(dbg) {System.out.println("-- addFileEle_Read(..)");}
    if(addFileEle == null || addFileEle.length() <=0) {
        throw new AddDataException("Programming error: \"addFileEle\"=null or empty in \"addFileEle_Read\"");
    }
    if(elemCompAdd == null) {
        throw new AddDataException("Programming error: \"elemCompAdd\"=null in \"addFileEle_Read\"");
    }
    elemCompAdd.clear();
    elemCompAdd.add(new String[]{"XX","H2O","water"});
    elemCompAdd.add(new String[]{"H","H+","hydrogen ion"});
    elemCompAdd.add(new String[]{"e-","e-","electron (= redox potential)"});

    java.io.File ef = new java.io.File(addFileEle);
    if(ef.exists()) {
        if(dbg) {System.out.println("Reading file \""+ef.getName()+"\" in addFileEle_Read");}
        try {LibDB.readElemFileText(ef, elemCompAdd);}
        catch (LibDB.ReadElemException ex) {throw new AddDataException(ex.getMessage());}
    } //if element file exists
    //return;
  } //addFileEle_Read
 //</editor-fold>

 //<editor-fold defaultstate="collapsed" desc="addFileEle_Write()">
/** Writes the contents of array list "elemCompAdd" to text file "addFileEle".
 * First a temporary file is created, and if no error occurs, the original file
 * is deleted, and the temporary file is renamed.
 * Note: "elemCompAdd" is not changed.
 * @param dbg if true, extra output messages will be printed
 * @param addFileEle the name, with complete path, of the chemical element -
 * reactant file (that corresponds to the database "addFile")
 * @param elemCompAdd Array list of String[3] objects with the reactants (components)
 * used in file "addFile"<br>
 *  [0] contains the element name (e.g. "C"),<br>
 *  [1] the component formula ("CN-" or "cit-3"),<br>
 *  [2] the component name ("cyanide" or null); not really needed, but used to help the user
 * @see ProgramDataDB#elemComp elemComp
 * @see AddDataElem#elemCompAdd_Update(boolean, java.awt.Component, java.lang.String, java.lang.String, java.util.ArrayList, java.util.ArrayList)  elemCompAdd_Update
 * @throws lib.database.AddDataElem.AddDataException  */
  public static void addFileEle_Write(final boolean dbg,
          final String addFileEle,
          java.util.ArrayList<String[]> elemCompAdd)
          throws AddDataException {
    if(dbg) {System.out.println("-- addFileEle_Write(..)");}
    if(addFileEle == null || addFileEle.length() <=0) {
        throw new AddDataException("Error: \"addFileEle\"=null or empty in \"addFileEle_Write\"");
    }
    if(elemCompAdd == null || elemCompAdd.size() <=0) {
        throw new AddDataException("Error: \"elemCompAdd\"=null or empty in \"addFileEle_Write\"");
    }
    java.io.File wf = new java.io.File(addFileEle);
    if(wf.exists() && (!wf.canWrite() || !wf.setWritable(true))) {
        throw new AddDataException ("Error: can not write to file"+nl+"    \""+addFileEle+"\"");
    }
    // --- open output files
    boolean ok;
    boolean replace = wf.exists();
    java.io.File tmpF;
    String addFileEleTmp = Div.getFileNameWithoutExtension(addFileEle)+"-"+
            Div.getFileNameExtension(addFileEle)+".tmp";
    if(replace) {
        tmpF = new java.io.File(addFileEleTmp);
        if(tmpF.exists()) {
            try {ok = tmpF.delete();}
            catch (Exception ex) {throw new AddDataException(ex.getMessage());}
            if(!ok) {throw new AddDataException("Could not delete file:"+nl+"\""+addFileEleTmp+"\"");}
        }
    } else {tmpF = wf;}
    java.io.PrintWriter pw;
    try {pw =  new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter(tmpF)));}
    catch (java.io.IOException ex) {throw new AddDataException(ex.getMessage());}
    int n;
    for(int i =0; i < LibDB.elementName.length; i++) {
        n=0;
        for(int j=0; j < elemCompAdd.size(); j++) {
            if(elemCompAdd.get(j)[0].equals(LibDB.elementSymb[i])) {n++;}
        } //for j
        if(n == 0) {continue;}
        pw.format("%-2s,%2d ,", LibDB.elementSymb[i],n);
        for(int j=0; j < elemCompAdd.size(); j++) {
            if(elemCompAdd.get(j)[0].equals(LibDB.elementSymb[i])) {
                pw.print(Complex.encloseInQuotes(elemCompAdd.get(j)[1])+",");
                pw.print(Complex.encloseInQuotes(elemCompAdd.get(j)[2])+",");
            }
        } //for j
        pw.println();
    } //for i
    // For components in the database not belonging to any element
    // set all of them into "XX"
    n = 0;
    for(int j=0; j < elemCompAdd.size(); j++) {
        ok = false;
        for(int i =0; i < LibDB.elementName.length; i++) {
            if(elemCompAdd.get(j)[0].equals(LibDB.elementSymb[i])) {ok = true; break;}
        } //for i
        if(!ok) {n++;}
    } //for j
    if(n > 0) {
        pw.format("%-2s,%2d ,", "XX",n);
        for(int j=0; j < elemCompAdd.size(); j++) {
            ok = false;
            for(int i =0; i < LibDB.elementName.length; i++) {
                if(elemCompAdd.get(j)[0].equals(LibDB.elementSymb[i])) {ok = true; break;}
            } //for i
            if(!ok) {
                pw.print(Complex.encloseInQuotes(elemCompAdd.get(j)[1])+",");
                pw.print(Complex.encloseInQuotes(elemCompAdd.get(j)[2])+",");
            }
        } //for j
        pw.println();
    } // n>0
    // finished
    pw.close();
    if(replace) {
        // copy temporary file to final destination
        String line = null;
        try {ok = wf.delete();}
        catch (Exception ex) {
            line = ex.getMessage()+nl;
            ok = false;
        }
        if(!ok || line != null) {
            if(line == null) {line = "";}
            throw new AddDataException(line+"Could not delete file:"+nl+"\""+addFileEle+"\"");
        }
        line = null;
        try {ok = tmpF.renameTo(wf);}
        catch (Exception ex) {
            line = ex.getMessage()+nl;
            ok = false;
        }
        if(!ok || line != null) {
            if(line == null) {line = "";}
            throw new AddDataException(line+"Could not rename file:"+nl+
                    "\""+addFileEleTmp+"\""+nl+
                    "into: \""+addFileEle+"\"");
        }
    } //if replace
  } //addFileEle_Write
 //</editor-fold>

 //<editor-fold defaultstate="collapsed" desc="elemCompAdd_Update()">
/**  Creates "elemCompAdd": the matrix with all existing reactants (components)
 * and the corresponding chemical elements.
 * <br>
 *  - Reads the components found in text file "addFileEle" (if any). If the file
 * does not exist it is created.
 * <br>
 *  - Then adds any components in the reactions in text file "addFile" (if any)
 * that were missing in "addFileEle" but that are found in "elemComp" (the
 * list of reactants from the main database)
 * @param dbg if true, extra output messages will be printed
 * @param parent used to display dialogs, if needed.
 * @param addFile the name, with complete path, of the text file with new data.
 * It may not exist, or it may be null.
 * @param addFileEle the name, with complete path, of the chemical element -
 * reactant text file corresponding to the database "addFile".
 * If it does not exist it will be created.
 * @param elemComp
 * @param elemCompAdd Array list of String[3] objects with the reactants (components)
 * used in file "addFile"<br>
 *  [0] contains the element name (e.g. "C"),<br>
 *  [1] the component formula ("CN-" or "cit-3"),<br>
 *  [2] the component name ("cyanide" or null); not really needed, but used to help the user<br>
 * In this method this array list is "updated" to contain all components needed
 * for the "add" file. That is, all components initially in file "addFileEle" (if any)
 * are read, <b>and</b> any component in the reactions in file "addFile"
 * that were missing in "addFileEle" (if any), are added if found in the
 * list of components from the main database(s), that is, if found in "elemComp".
 * @see ProgramDataDB#elemComp elemComp
 * @see AddDataElem#addFileEle_Read(boolean, java.lang.String, java.util.ArrayList) addFileEle_Read
 * @throws lib.database.AddDataElem.AddDataException  */
  public static synchronized void elemCompAdd_Update(final boolean dbg,
          java.awt.Component parent,
          String addFile,
          String addFileEle,
          java.util.ArrayList<String[]> elemComp,
          java.util.ArrayList<String[]> elemCompAdd)
          throws AddDataException {
    if(dbg) {System.out.println("-- elemCompAdd_Update(..)");}
    if(addFileEle == null || addFileEle.length() <=0) {
        throw new AddDataException("Programming error: \"addFileEle\"=null or empty in \"elemCompAdd_Update\"");
    }
    if(elemComp == null) {
        throw new AddDataException("Programming error: \"elemComp\"=null in \"elemCompAdd_Update\"");
    }
    if(elemCompAdd == null) {
        elemCompAdd = new java.util.ArrayList<String[]>();
    } else {elemCompAdd.clear();}

    // ---- read file "addFileEle" (if it exists) into "elemCompAdd"
    addFileEle_Read(dbg, addFileEle, elemCompAdd);
    java.io.File afE = new java.io.File(addFileEle);
    if(!afE.exists()) {
        try{addFileEle_Write(dbg, addFileEle, elemCompAdd);}
        catch (AddDataException ex) {throw new AddDataException(ex.getMessage());}
    }

    // ---- add to "elemCompAdd" any components in reactions
    //      in file "addFile" that were missing in "addFileEle"
    //      but that are found in the component-list from the main database
    if(addFile == null || addFile.length() <=0) {return;}
    java.io.File afF = new java.io.File(addFile);
    if(!afF.exists()) {return;}
    if(!afF.canRead()) {
        throw new AddDataException("Error: can not open file"+nl+"    \""+addFile+"\".");
    }
    if(dbg) {
        System.out.println("  reading file \""+afF.getName()+"\" to search for missing components");
    }
    java.util.ArrayList<String> items = new java.util.ArrayList<String>();
    int cmplxNbr = 0;
    java.io.BufferedReader br = null;
    try{
        br = new java.io.BufferedReader(new java.io.FileReader(afF));
        String line;
        Complex c;
        boolean fnd, there;
        // read the reaction-file "addFile"
        while ((line = br.readLine()) != null){
            cmplxNbr++;
            if(line.length()<=0 || line.toUpperCase().startsWith("COMPLEX") || line.startsWith("@")) {continue;}
            try {c = Complex.fromString(line);}
            catch (Complex.ReadComplexException ex) {MsgExceptn.msg(ex.getMessage()); break;}
            if(c == null) {continue;}
            for(int i=0; i < Complex.NDIM; i++) { // take each reactant
                if(c.component[i] != null && c.component[i].length()>0 && Math.abs(c.numcomp[i]) >=0.001) {
                    fnd = false;
                    for (int j=0; j < elemCompAdd.size(); j++) { // search in data from "addFileEle"
                        if(elemCompAdd.get(j)[1].equals(c.component[i])) {fnd = true; break;}
                    } //for j
                    if(!fnd) { // if not found in "addFileEle"
                        // search in other open databases
                        for(int j=0; j < elemComp.size(); j++) {
                            if(elemComp.get(j)[1].equals(c.component[i])) {
                                there = false;
                                for(String t : items) {if(t.equals(elemComp.get(j)[1])) {there = true; break;}}
                                if(!there) {items.add(elemComp.get(j)[1]);}
                                String[] s = {elemComp.get(j)[0],elemComp.get(j)[1],elemComp.get(j)[2]};
                                elemCompAdd.add(s);
                            }
                        } //for j
                    } //if !fnd
                } //if reactant not empty
            } //for i
        } //while
    } //try
    catch (java.io.IOException ex) {
        String msg = ex.getMessage()+nl+"reading line "+cmplxNbr+nl+"in file: \""+addFile+"\"";
        if(dbg) {System.out.println("Error "+msg);}
        items.clear();
        throw new AddDataException(msg);
    }
    finally {
        if(dbg) {System.out.println("  elemCompAdd_Update() finished reading \""+afF.getName()+"\"");}
        try{if(br != null) {br.close();}}
        catch(java.io.IOException ex) {
            items.clear();
            String msg = ex.getMessage()+nl+"with file: \""+addFile+"\"";
            throw new AddDataException(msg);
        }
    }

    if(items.size() <= 0) {return;}

    // javax.swing.DefaultListModel aModel = new javax.swing.DefaultListModel();  // java 1.6
    javax.swing.DefaultListModel<String> aModel = new javax.swing.DefaultListModel<>();
    java.util.Collections.sort(items,String.CASE_INSENSITIVE_ORDER);
    java.util.Iterator<String> iter = items.iterator();
    while(iter.hasNext()) {aModel.addElement(iter.next());}

    String msg = "<html>The following reactant";
    if(aModel.size()>1) {msg = msg+"s";}
    msg = msg + "<br>will be written to file<br>\""+afE.getName()+"\".<br>&nbsp;</html>";
    javax.swing.JLabel aLabel = new javax.swing.JLabel(msg);
    // javax.swing.JList aList = new javax.swing.JList(aModel); // java 1.6
    javax.swing.JList<String> aList = new javax.swing.JList<>(aModel);
    aList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    aList.setVisibleRowCount(5);
    javax.swing.JScrollPane aScrollPane = new javax.swing.JScrollPane();
    aScrollPane.setViewportView(aList);
    aList.setFocusable(false);
    Object[] o = {aLabel, aScrollPane};
    javax.swing.JOptionPane.showMessageDialog(parent, o,
            "Writing: Elements - Components",
            javax.swing.JOptionPane.INFORMATION_MESSAGE);

    addFileEle_Write(dbg, addFileEle, elemCompAdd);

  } //elemCompAdd_Update()
 //</editor-fold>

 //<editor-fold defaultstate="collapsed" desc="addFileEle_ComponentDelete()">
/** Removes the component "compName" from the "elemCompAdd" list and saves the list
 * to the text file "addFileEle"
 * @param dbg if true, extra output messages will be printed
 * @param compName the reactant (component) to remove.
 * @param addFileEle
 * @param elemCompAdd
 * @throws lib.database.AddDataElem.AddDataException
 * @see FrameAddData#elemCompAdd elemCompAdd  */
  public static void addFileEle_ComponentDelete(final boolean dbg,
          final String compName,
          final String addFileEle,
          final java.util.ArrayList<String[]> elemCompAdd)
          throws AddDataException {
    if(dbg) {System.out.println("-- addFileEle_ComponentDelete("+compName+" ..)");}
    if(compName == null || compName.trim().length() <=0) {return;}
    String compDel = compName.trim();

    // make a copy in case something goes wrong
    java.util.ArrayList<String[]> elemCompAdd0 = new java.util.ArrayList<String[]>(elemCompAdd);
    //remove "compDel"
    int i = 0;
    while(i < elemCompAdd.size()) {
        if(compDel.equals(elemCompAdd.get(i)[1])) {
                elemCompAdd.remove(i);
                continue;
        }
        i++;
    } //while

    //save changes made in "elemCompAdd" in file "addFileEle"
    try{addFileEle_Write(dbg, addFileEle, elemCompAdd);}
    catch (AddDataException ex) {
        // undo the deletion
        elemCompAdd.clear();
        for(i =0; i < elemCompAdd0.size(); i++) {elemCompAdd.add(elemCompAdd0.get(i));}
        throw new AddDataException(ex.getMessage());
    }
    //return;
  } //addFileEle_ComponentDelete()
 //</editor-fold>

 //<editor-fold defaultstate="collapsed" desc="addFileEle_ComponentSave()">

 /** Adds a component to text file "addFileEle" (re-writes the file)
  * @param dbg
  * @param parent
  * @param newComp the new component, for example "SCN-"
  * @param linkedTo text describing what elements are "linked to" this component,
  * for example: "C,N" for cyanide, "C" for oxalate.
  * @param descr text describing the component, for example "cyanide"
  * @param addFile
  * @param addFileEle
  * @param elemComp
  * @param elemCompAdd
  * @return true if "newComp" has been saved.
  * False otherwise: if an error occurs or if the user cancels the operation
  * @throws lib.database.AddDataElem.AddDataException
  */
  public static boolean addFileEle_ComponentSave(final boolean dbg,
          java.awt.Component parent,
          final String newComp,
          final String linkedTo,
          final String descr,
          final String addFile,
          final String addFileEle,
          java.util.ArrayList<String[]> elemComp,
          final java.util.ArrayList<String[]> elemCompAdd)
          throws AddDataException {
    if(dbg) {System.out.println("-- addFileEle_ComponentSave()");}
    // ---- Find out if new component is already in the database
    boolean fnd = false;
    String linkedToOld = "";
    for(int i=0; i < elemCompAdd.size(); i++) {
        if(newComp.equals(elemCompAdd.get(i)[1])) {
            fnd = true;
            if(linkedToOld.indexOf(elemCompAdd.get(i)[0]) < 0) {
                if(linkedToOld.length()>0) {linkedToOld = linkedToOld+", ";}
                linkedToOld = linkedToOld + elemCompAdd.get(i)[0];
            }
        }
    } //for i
    // ---- ask the user for confirmation
    String msg;
    if(fnd) {
        msg = "Replace:"+nl+"  \""+newComp+"\" linked to: "+linkedToOld+nl+nl+
              "with:"+nl+"   \""+newComp+"\" linked to: "+linkedTo;
    } else {
        msg = "Add component \""+newComp+"\""+nl+"linked to: "+linkedTo+" ?";
    }
    Object[] opt = {"Yes", "Cancel"};
    int m = javax.swing.JOptionPane.showOptionDialog(parent, msg,
                "Writing: Elements - Components", javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
    if(m != javax.swing.JOptionPane.YES_OPTION) {return false;}

    // ---- To add a component, the matrix with all existing components is created: "elemCompAdd".
    //      If a file with data for reactions exists (addFile), any missing component is added,
    //      then the new component is added at the end of the "elemCompAdd".
    //      Finally "elemCompAdd" is saved in the element-file "addFileEle"
    elemCompAdd_Update(dbg,parent,addFile,addFileEle,elemComp,elemCompAdd);

    // ---- make a copy in case something goes wrong
    java.util.ArrayList<String[]> elemCompAdd0 = new java.util.ArrayList<String[]>(elemCompAdd);

    // ---- remove any old occurences of this component
    if(fnd) {
        int i = 0;
        while(i < elemCompAdd.size()) {
            if(newComp.equals(elemCompAdd.get(i)[1])) {
                elemCompAdd.remove(i);
                continue;
            }
            i++;
        } //while
    } //if fnd

    // ---- add the new component
    java.util.ArrayList<String> aL;
    try{aL = CSVparser.splitLine(linkedTo);}
    catch (CSVparser.CSVdataException ex) {
        elemCompAdd.clear();
        for(int i =0; i < elemCompAdd0.size(); i++) {elemCompAdd.add(elemCompAdd0.get(i));}
        throw new AddDataException(ex.getMessage());
    }
    for(int i=0; i<aL.size(); i++) {
        String[] s = {aL.get(i), newComp, descr};
        elemCompAdd.add(s);
    }

    // ---- save changes made in "elemCompAdd" in file "addFileEle"
    try{addFileEle_Write(dbg, addFileEle, elemCompAdd);}
    catch (AddDataException ex) {
        elemCompAdd.clear();
        for(int i =0; i < elemCompAdd0.size(); i++) {elemCompAdd.add(elemCompAdd0.get(i));}
        throw new AddDataException(ex.getMessage());
    }

    return true;
  } //addFileEle_ComponentSave()
 //</editor-fold>

public static class AddDataException extends Exception {
    public AddDataException() {super();}
    public AddDataException(String txt) {super(txt);}
    } //AddDataInternalException

}
