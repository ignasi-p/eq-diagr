package lib.database;

import lib.common.MsgExceptn;
import lib.common.Util;

/** Check for errors in the reactions in a database file.
 * <br>
 * Copyright (C) 2017-2018 I.Puigdomenech.
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
public class CheckDatabases {
  /** New-line character(s) to substitute "\n" */
  private static final String nl = System.getProperty("line.separator");

  //<editor-fold defaultstate="collapsed" desc="static nested class CheckDataBasesLists">
  /** static nested class to transfer results from checkADatabase
   * @see CheckDatabases#checkADatabase(boolean, lib.database.ProgramDataDB, java.awt.Component, java.lang.String, lib.database.CheckDatabases.CheckDataBasesLists) checkADatabase
   */
  public static class CheckDataBasesLists {
    public int nbrCompsInElementFiles;
    /** a list of all product-reaction combinations: String[]{product,reaction} */
    public java.util.HashSet<String []> productsReactionsSet;
    /** a map of reactants found in the database(s), mapping each reactant
     * to how many reactions it participates */
    public java.util.HashMap<String, Integer> reactantsSet;
    /** reactants (components) present in the reactions database(s) not found in the
     * corresponding element-reactants file(s) */
    public java.util.HashSet<String> reactantsUnknown;
    /** Components in the element-reactant file(s) not used in the reactions database(s): */
    public java.util.HashSet<String> reactantsNotUsed;
    /** Reactant names in the reaction database(s) that are equivalent but will be treated as different */
    public java.util.ArrayList<String> reactantsCompare;
    /** Reactant names in the element-reactant files(s) that are equivalent but will be treated as different */
    public java.util.ArrayList<String> elementReactantsCompare;
    /** reactions (product name) where a reactant is given without a coefficient */
    public java.util.HashSet<String> reactantWithoutCoef;
    /** reactions (product name) where a coefficient is given with an empty reactant */
    public java.util.HashSet<String> coefWithoutReactant;
    /** reactions  (product name) with charge imbalance */
    public java.util.HashSet<String> chargeImbalance;
    /** reactions (product name) with H+ conflict */
    public java.util.HashSet<String> protonConflict;
    /** reaction products present in two or more different reactions */
    public java.util.TreeSet<String> duplProductsSet;
    /** duplicate reactions (with the same reaction product) */
    public java.util.TreeSet<String> duplReactionsSameProdctSet;
    /** duplicate reactions (with different reaction product) */
    public java.util.TreeSet<String> duplReactionsDifProductSet;
    /** duplicate solids (with different ending: "(s)" and "(cr)") */
    public java.util.TreeSet<String> duplSolidsSet;
    /** reaction product names not containing one or more reactant names, for example Fe+2 + Cl- = ZnCl+
    * (in this case "ZnCl+" does not contain "Fe") */
    public java.util.ArrayList<String> itemsNames;
    /** list of reference citations (key) not matching any known reference */
    public java.util.ArrayList<String> refsNotFnd;
    /** list of reference citations (key) that do match a known reference */
    public java.util.ArrayList<String> refsFnd;

    public CheckDataBasesLists() {}
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="checkDataBases">
/** checks databses, writing statistics and errors in a CheckDataBasesLists object
 * 
 * @param dbg write debug information?
 * @param parent as the owner of error messages
 * @param dataBaseList the list of file names to check
 * @param refs the references available for the calling program. It may be "null".
 * @param lists an object where the errors and statistics will be written
 * @return <code>true</code> if no errors occurr, <code>false</code> otherwise
 */
  public static boolean checkDatabases(final boolean dbg,
          final java.awt.Component parent,
          final java.util.ArrayList<String> dataBaseList,
          final References refs,
          final CheckDataBasesLists lists) {
    if(dbg) {System.out.println("---- checkDatabases, debug = true");}
    if(lists == null) {
        MsgExceptn.exception("Error in \"checkADatabase\":"+nl+
                "parameter \"lists\" (where output arrays would be stored) is null.");
        return false;
    }
    lists.nbrCompsInElementFiles = 0;
    lists.productsReactionsSet = new java.util.HashSet<String []>();
    lists.reactantsSet = new java.util.HashMap<String, Integer>();
    lists.reactantsUnknown = new java.util.HashSet<String>();
    lists.reactantsNotUsed = new java.util.HashSet<String>();
    lists.reactantsCompare = new java.util.ArrayList<String>();
    lists.elementReactantsCompare = new java.util.ArrayList<String>();
    lists.reactantWithoutCoef = new java.util.HashSet<String>();
    lists.coefWithoutReactant = new java.util.HashSet<String>();
    lists.chargeImbalance = new java.util.HashSet<String>();
    lists.protonConflict = new java.util.HashSet<String>();    
    lists.duplProductsSet = new java.util.TreeSet<String>();
    lists.duplReactionsSameProdctSet = new java.util.TreeSet<String>();
    lists.duplReactionsDifProductSet = new java.util.TreeSet<String>();
    lists.duplSolidsSet = new java.util.TreeSet<String>();
    lists.itemsNames = new java.util.ArrayList<String>();
    lists.refsNotFnd = null;
    lists.refsFnd = null;
    if(refs != null)  {
        lists.refsNotFnd = new java.util.ArrayList<String>();
        lists.refsFnd = new java.util.ArrayList<String>();
    }
    if(dataBaseList == null) {
        MsgExceptn.exception("Error in \"checkDatabases\": dataBaseList is null."); return false;
    }
    if(dataBaseList.size() <=0) {
        MsgExceptn.exception("Error in \"checkDatabases\": dataBaseList is empty."); return false;
    }
    java.io.File f;
    for(String dbName : dataBaseList) {
        if(dbg) {System.out.println("     database name = "+dbName);}
        f = new java.io.File(dbName);
        if(!f.exists() || !f.isFile())  {
            MsgExceptn.msg("Warning in \"checkADatabase\":"+nl+
                "file \""+dbName+"\" either does not exist or is not a normal file...");
            return false;
        }
        if(!f.canRead())  {
            MsgExceptn.exception("Error in \"checkADatabase\":"+nl+
                "can NOT read file \""+dbName+"\".");
            return false;
        }
    }
    int nH, i, j;
    boolean fnd, ok, isRedoxReaction;
    Complex cmplx;
    String product, reaction;
    /** the reference text for each reaction is split into keys and each key
     * is stored in array "rfs" */
    java.util.ArrayList<String> rfs;

    // -- get and prepare the search engine "LibSearch"    
    final LibSearch libS;
    try{libS = new LibSearch(dataBaseList);}
    catch (LibSearch.LibSearchException ex) {
        MsgExceptn.exception(ex.getMessage());
        return false;
    }
    // -- read the elements/components found in the database's element file
    /** array list of String[3] objects<br>
     * [0] contains the element name (e.g. "C"),<br>
     * [1] the component formula ("CN-" or "Fe+2"),<br>
     * [2] the component name ("cyanide" or null), which is not really needed,
     * but used to help the user */
    java.util.ArrayList<String[]> elemsComps = new java.util.ArrayList<String[]>();


    LibDB.getElements(parent, dbg, dataBaseList, elemsComps);
    //the number of unique reactants in elemsComps
    lists.nbrCompsInElementFiles = elemsComps.size()+1;
    for(i=0; i < (elemsComps.size()-1); i++) {
        for(j=(i+1); j < elemsComps.size(); j++) {
            if(elemsComps.get(i)[1].equals(elemsComps.get(j)[1])) {lists.nbrCompsInElementFiles--;}
        }
    }

    // -- loop through all reactions
    boolean fistComplex = true;
    try{
    while(true) {
        try {cmplx = libS.getComplex(fistComplex);}
        catch (LibSearch.LibSearchException ex) {
            libS.libSearchClose();
            String msg = ex.getMessage();
            MsgExceptn.showErrMsg(parent, msg, 1);
            break;
        }
        fistComplex = false;
        if(cmplx == null) {break;} // last complex
        if(cmplx.name.startsWith("@")) {product = cmplx.name.substring(1);} else {product = cmplx.name;}
        reaction = reactString(cmplx); // if it starts with "@" the reaction is "" (empty)
        isRedoxReaction = Complex.isRedox(cmplx);

        for(String[] r : lists.productsReactionsSet) {
            // -- find out duplicate reaction products having different reactions
            if(Util.nameCompare(product, r[0])) {
                if(!isRedoxReaction
                        && !(r[1].length() > 0 && Util.stringsEqual(r[1], reaction))) {
                    lists.duplProductsSet.add(product);
                }
            }
            // (empty reaction products are not compared)
            if(r[1].length() > 0 && Util.stringsEqual(r[1], reaction)) {
                // -- find out duplicate reactions (with the same reaction product)
                if(Util.nameCompare(product, r[0])) {lists.duplReactionsSameProdctSet.add(product);}
                // -- find out duplicate reactions (with different reaction product)
                //    Names that will not give a warning, for example:
                //       - Fe(OH)2(s) and Fe(OH)2(cr) and Fe(OH)2
                //       - CO2 and CO2(g)
                //    but Fe(c) and Fe(cr) will give a warning
                if(// both are solid but not equal, e.g. AmCO3OH(s) and AmOHCO3(cr)
                   (Util.isSolid(product) && Util.isSolid(r[0]) && 
                        !Util.bareNameOf(product).equals(Util.bareNameOf(r[0])))
                   // both are solid and equal except for "(c)" and "(cr)"
                   || (Util.is_cr_or_c_solid(product) && Util.is_cr_or_c_solid(r[0])
                        && Util.bareNameOf(product).equals(Util.bareNameOf(r[0])))
                   // both gas but different, e.g. H2S(g) and SH2(g)
                   || (Util.isGas(product) && Util.isGas(r[0]) && 
                        !Util.bareNameOf(product).equals(Util.bareNameOf(r[0])))
                   // none is solid or gas, but different, such as VO2(OH)2- and VO3-
                   || (!Util.isSolid(product) && !Util.isSolid(r[0]) &&
                       !Util.isGas(product) && !Util.isGas(r[0]) &&
                              !Util.nameCompare(product, r[0]))) {
                    lists.duplReactionsDifProductSet.add(product+"   and:  "+r[0]);
                }
                // -- find out duplicate solids (with different phase designation)
                if(!product.equals(r[0]) && Util.isSolid(product) && Util.isSolid(r[0])
                        && Util.bareNameOf(product).equals(Util.bareNameOf(r[0]))) {
                    if(product.endsWith("(am)")) {
                          lists.duplSolidsSet.add(r[0]+"   and:  "+product);
                    } else {
                          lists.duplSolidsSet.add(product+"   and:  "+r[0]);
                    }
                }
            }
        }
        // -- keep a list of all products/reactions
        lists.productsReactionsSet.add(new String[]{product,reaction});

        // --
        if(cmplx.name.startsWith("@")) {continue;}
        // -- list all reactions with a reactant with no coefficients
        for(i=0; i<Complex.NDIM; i++) {
              if(cmplx.component[i] != null && cmplx.component[i].trim().length()>0
                      && Math.abs(cmplx.numcomp[i]) < 0.001) {
                  lists.reactantWithoutCoef.add(cmplx.name);
                  break;
              }
        }
        // -- find reactions with a coefficnet with no reactant name
        for(i=0; i<Complex.NDIM; i++) {
            if(Math.abs(cmplx.numcomp[i]) >= 0.001
                      && (cmplx.component[i] == null || cmplx.component[i].trim().length() <=0)) {
                  lists.coefWithoutReactant.add(cmplx.name);
                  break;
            }
        }
        // -- find reactions not charge balanced
        if(!Complex.isChargeBalanced(cmplx)) {lists.chargeImbalance.add(cmplx.name);}

        // -- list reactants; keep track of H+
        nH = -1;
        for(i =0; i < Complex.NDIM; i++) {
            if(cmplx.component[i] == null || cmplx.component[i].trim().length() <=0) {continue;}
            if(Util.isProton(cmplx.component[i])) {nH = i;}
            if(lists.reactantsSet.containsKey(cmplx.component[i])) {
                j = lists.reactantsSet.get(cmplx.component[i]);
                lists.reactantsSet.put(cmplx.component[i],j+1);
            } else {
                lists.reactantsSet.put(cmplx.component[i],1);
            }
        }
        // -- find reactions with error in H+
        if(nH >=0 && !Util.areEqualDoubles(cmplx.proton, cmplx.numcomp[nH])) {
            lists.protonConflict.add(cmplx.name);
        }

        // -- find out if the reactants are in the name of the product
        //<editor-fold defaultstate="collapsed" desc="is reactant in product name?">
        for(i =0; i < Complex.NDIM; i++) {
            String t = cmplx.component[i];
            if(t == null || t.length() <=0) {continue;}
            if(Util.isElectron(t) || Util.isProton(t) || Util.isWater(t))  {continue;}
            t = Util.bareNameOf(t);
            ok = cmplx.name.contains(t);
            if(!ok && t.equals("Hg2")) {ok = cmplx.name.contains("Hg");}
            if(!ok && t.equals("CH3Hg")) {ok = (cmplx.name.contains("Hg") && cmplx.name.contains("CH"));}
            if(!ok && t.equals("NH3")) {ok = cmplx.name.contains("NH");}
            if(!ok && t.equals("NH4")) {ok = cmplx.name.contains("NH");}
            if(!ok && t.equals("NH3") && isRedoxReaction) {ok = cmplx.name.contains("N");}
            if(!ok && t.equals("NH4") && isRedoxReaction) {ok = cmplx.name.contains("N");}
            if(!ok && (t.equals("NO2") || t.equals("NO3"))) {ok = cmplx.name.contains("N");}
            if(!ok && (t.equals("CO3") || t.equals("HCO3") || t.equals("HCOO"))) {ok = cmplx.name.contains("C");}
            if(!ok && (t.equals("MoO4") || t.equals("Mo2O2")
                    || t.equals("Mo2O4") || t.equals("Mo2(OH)2"))) {ok = cmplx.name.contains("Mo");}
            if(!ok && t.equals("WO4")) {ok = cmplx.name.contains("W");}
            if(!ok && t.equals("CrO4")) {ok = cmplx.name.contains("Cr");}
            if(!ok && t.equals("VO2")) {ok = cmplx.name.contains("V");}
            if(!ok && t.equals("UO2")) {ok = cmplx.name.contains("U");}
            if(!ok && t.equals("AmO2")) {ok = cmplx.name.contains("Am");}
            if(!ok && t.equals("NpO2")) {ok = cmplx.name.contains("Np");}
            if(!ok && t.equals("PuO2")) {ok = cmplx.name.contains("Pu");}
            if(!ok && t.equals("TcO(OH)2")) {ok = cmplx.name.contains("Tc");}
            if(!ok && (t.equals("As(OH)3") || t.equals("AsO4")
                    || t.equals("H3AsO3") || t.equals ("H2AsO3"))) {ok = cmplx.name.contains("As");}
            if(!ok && (t.equals("Sb(OH)3") || t.equals("Sb(OH)6"))) {ok = cmplx.name.contains("Sb");}
            if(!ok && (t.equals("Ge(OH)4") || t.equals("Ge(OH)2"))) {ok = cmplx.name.contains("Ge");}
            if(!ok && (t.equals("Te(OH)4") || t.equals("Te(OH)6") || t.equals("HTe"))) {ok = cmplx.name.contains("Te");}
            if(!ok && (t.contains("TeO3") || t.contains("TeO4") || t.contains("TeO6"))) {ok = cmplx.name.contains("Te");}
            if(!ok && t.equals("Ta(OH)5")) {ok = cmplx.name.contains("Ta");}
            if(!ok && t.equals("Nb(OH)5")) {ok = cmplx.name.contains("Nb");}
            if(!ok && t.equals("PoO") || t.equals("HPo")) {ok = cmplx.name.contains("Po");}
            if(!ok && t.equals("Si(OH)4") || t.equals("H4SiO4") || t.equals("SiO2")) {ok = cmplx.name.contains("Si");}
            if(!ok && t.equals("OsO4")) {ok = cmplx.name.contains("Os");}
            if(!ok && t.equals("TcO4")) {ok = cmplx.name.contains("Tc");}
            if(!ok && (t.equals("ReO4") || t.equals("Re(OH)4"))) {ok = cmplx.name.contains("Re");}
            if(!ok && (t.equals("RuO4") || t.equals("Ru(OH)2"))) {ok = cmplx.name.contains("Ru");}
            if(!ok && (t.equals("TiO") || t.equals("Ti(OH)4"))) {ok = cmplx.name.contains("Ti");}
            if(!ok && t.equals("VO")) {ok = cmplx.name.contains("V");}
            if(!ok && (t.equals("B(OH)3") || t.equals("H3BO3"))) {ok = cmplx.name.contains("B");}
            if(!ok && (t.equals("PO4") || t.equals("HPO4") || t.equals("H2PO2")
                    || t.equals("HPO3") || t.equals("P2O6"))) {ok = cmplx.name.contains("P");}
            if(!ok && t.equals("CNO")) {ok = cmplx.name.contains("CN");}
            if(!ok && (t.equals("BrO") || t.equals("BrO3"))) {ok = cmplx.name.contains("Br");}
            if(!ok && t.equals("IO3")) {ok = cmplx.name.contains("I");}
            if(!ok && t.equals("H2O2")) {ok = cmplx.name.contains("O2");}
            if(!ok && (t.equals("H2Se") || t.equals("HSe") || t.equals("SeO4") || t.equals("SeO3")
                    || t.equals("SeCN"))) {ok = cmplx.name.contains("Se");}
            if(!ok && t.equals("HS")) {ok = cmplx.name.contains("S");}
            if(!ok && (t.equals("SO4") || t.equals("SO3") || t.equals("S2O3"))) {ok = cmplx.name.contains("S");}
            if(!ok) {
                lists.itemsNames.add(cmplx.name+"  does not contain  "+cmplx.component[i]);
            }
        }
        //</editor-fold>

        if(cmplx.reference !=null && cmplx.reference.trim().length()>0
                  && refs !=null && lists.refsNotFnd != null) {
            rfs = refs.splitRefs(cmplx.reference);
            for(String r : rfs) {
                if(refs.isRefThere(r) == null) {
                    if(!lists.refsNotFnd.contains(r)) {
                        lists.refsNotFnd.add(r);
                    }
                } else {
                    if(lists.refsFnd != null && !lists.refsFnd.contains(r)) {
                        lists.refsFnd.add(r);
                    }
                }
            }
        }

    } // -- while // (loop through all reactions)
    } catch (Exception ex) {MsgExceptn.exception(ex.getMessage());}
    // --
    for(String component : lists.reactantsSet.keySet()) {
        fnd = false;
        for(i=0; i < elemsComps.size(); i++) {                
            if(elemsComps.get(i)[1].equals(component)) {fnd = true; break;}
        }
        if(!fnd) {lists.reactantsUnknown.add(component);}
    }
    // -- Components in element-reactant files not used in the databases for reactions
    for(i=0; i < elemsComps.size(); i++) {
        if(!lists.reactantsSet.containsKey(elemsComps.get(i)[1])) {
            lists.reactantsNotUsed.add(elemsComps.get(i)[1]);
        }
    }
    // -- Reactant names in the reaction database that are equivalent but will be treated as different
    java.util.ArrayList<String> arrayList = new java.util.ArrayList<String>(lists.reactantsSet.keySet());
    java.util.Collections.sort(arrayList, String.CASE_INSENSITIVE_ORDER);
    for(j=0; j < (arrayList.size()-1); j++) {
        for(i=(j+1); i < arrayList.size(); i++) {
            if(Util.nameCompare(arrayList.get(j),arrayList.get(i))) {
                lists.reactantsCompare.add(arrayList.get(j)+"   and:  "+arrayList.get(i));
            }
        }
    }
    // -- Reactant names in the elements-reactants file(s) that are equivalent but will be treated as different
    arrayList = new java.util.ArrayList<String>();
    for(i=0; i<elemsComps.size(); i++) {arrayList.add(elemsComps.get(i)[1]);}
    java.util.Collections.sort(arrayList, String.CASE_INSENSITIVE_ORDER);
    for(j=0; j < (arrayList.size()-1); j++) {
        for(i=(j+1); i < arrayList.size(); i++) {
            if(!arrayList.get(j).equals(arrayList.get(i))
                    && Util.nameCompare(arrayList.get(j),arrayList.get(i))) {
                lists.elementReactantsCompare.add(arrayList.get(j)+"   and:  "+arrayList.get(i));
            }
        }
    }

    return true;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="reactString(Complex)">
  /** Returns a String representation of the reaction such as:<pre>
   * "<code>Fe 2+;1;;;e-;-1;;;;;;;0;</code>"</pre>
   * Equivalent to <code>Complex.toString</code>, except that (1) the product name,
   * the logK and the reference are excluded, and (2) the reactants are sorted.
   * That is, it returns only the reaction (sorted).
   * If the product name starts with "@" this method returns an empty String ("").
   * @param cmplx the complex
   * @return a text representing the reaction
   * @see Complex#toString() Complex.toString  */
    private static String reactString(Complex cmplx) {
    if(cmplx == null) {return "";}
    //if(cmplx.name.startsWith("@")) {return text.toString();}
    if(cmplx.name.startsWith("@")) {return "";}
    Complex c;
    try {c = (Complex)cmplx.clone();}
    catch(CloneNotSupportedException cex) {return "";}
    // -- exclude water?
    //for(int ic =0; ic < Complex.NDIM; ic++) {
    //  if(Util.isWater(c.component[ic])) {c.component[ic] = ""; c.numcomp[ic] = 0; break;}
    //}
    StringBuilder text = new StringBuilder();  
    //text.append(Complex.encloseInQuotes(c.name)); text.append(";");
    Complex.sortReactants(c);
    for(int ic =0; ic < Complex.NDIM; ic++) {
      if(c.component[ic] == null || c.component[ic].length()<=0
              // || Util.isWater(c.component[ic])
              || Math.abs(c.numcomp[ic]) < 0.001) {text.append(";;"); continue;}
      text.append(Complex.encloseInQuotes(c.component[ic])); text.append(";");
      text.append(Util.formatDbl4(c.numcomp[ic]).trim()); text.append(";");
    }
    if(Math.abs(c.proton) >=0.001) {text.append(Util.formatDbl4(c.proton).trim());}
    text.append(";");
    return text.toString();
  } //reactString(cmplx)
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="displayDatabaseErrors">
 /** display errors in dialogs conatining two buttons (ok and cancel).
  * The errors are provided in an object of CheckDataBasesLists.
  * The text on the first button is provided by the user, for example
  * "Exit anyway", the second button is always "Cancel".
  * 
  * @param dbg write debug information?
  * @param parent as the owner of error messages
  * @param title for error messages
  * @param database the name of the database, written in the error message (html).
  * Several file names may be given separated by lne brakes "&lt;br&gt;".
  * @param lists an object where the errors have been reported
  * @return <code>true</code> if no errors are found or if the user selects "ok" anyway;
  * <code>false</code> if there are errors and the user selects "cancel"
  */
  public static boolean displayDatabaseErrors(final boolean dbg,
          final java.awt.Component parent,
          final String title,
          final String database,
          final CheckDataBasesLists lists) {
    if(dbg) {System.out.println("---- displayDatabaseErrors, debug = true");}
    if(lists == null) {
        MsgExceptn.exception("Error in \"displayDatabaseErrors\":"+nl+
                "   parameter \"lists\" (where output arrays would be stored) is null.");
        return false;
    }
    if(lists.reactantsUnknown == null) {
        MsgExceptn.exception("Error in \"displayDatabaseErrors\":"+nl+
                "   \"lists.reactantsUnknown\" is null.");
        return false;
    }
    if(lists.reactantWithoutCoef == null) {
        MsgExceptn.exception("Error in \"displayDatabaseErrors\":"+nl+
                "   \"lists.reactantWithoutCoef\" is null.");
        return false;
    }
    if(lists.coefWithoutReactant == null) {
        MsgExceptn.exception("Error in \"displayDatabaseErrors\":"+nl+
                "   \"lists.coefWithoutReactant\" is null.");
        return false;
    }
    java.util.TreeSet<String> treeSet;
    javax.swing.DefaultListModel<String> aModel; // javax.swing.DefaultListModel aModel; // java 1.6
    String msg;
    boolean answer = true;
    if(lists.reactantsUnknown.size() >0) {
        treeSet = new java.util.TreeSet<String>(lists.reactantsUnknown);
        aModel = new javax.swing.DefaultListModel<>(); // aModel = new javax.swing.DefaultListModel();  // java 1.6
        if(dbg) {System.out.println(nl+"Error: reactants (components) in the reactions database(s) NOT found"+nl+
                            "   in the element-reactant file(s)."+nl+
                            "   Note that any reaction involving these components"+nl+
                            "   will NOT be found in a database search!");}
        for (String r : treeSet) {
            aModel.addElement(r);
            if(dbg){System.out.println(" "+r);}
        }

        msg = "<html>File: <b>"+database+"</b><br>&nbsp;<br>Could NOT find the following reactant";
        if(aModel.size()>1) {msg = msg+"s";}
        
        msg = msg + ".<br>&nbsp;<br>You should add ";
        if(aModel.size()>1) {msg = msg+"them";} else {msg = msg+"it";}
        msg = msg+" to<br>the element-reactant file<br>before proceeding.<br>&nbsp;</html>";
        answer = showListDialog(parent, title, msg, "Proceed anyway", "Cancel", aModel);
    }
    if(!answer) {return false;}
    if(lists.reactantWithoutCoef.size()>0 || lists.coefWithoutReactant.size()>0) {
        treeSet = new java.util.TreeSet<String>();
        if(lists.reactantWithoutCoef.size()>0) {
            for(String t : lists.reactantWithoutCoef) {treeSet.add(t);}
        }
        if(lists.coefWithoutReactant.size()>0) {
            for(String t : lists.coefWithoutReactant) {treeSet.add(t);}
        }
        aModel = new javax.swing.DefaultListModel<>(); // aModel = new javax.swing.DefaultListModel();  // java 1.6
        if(dbg) {System.out.println(nl+"The following reaction(s) had either"+nl+
                "  - reactants with zero coefficients, or"+nl+
                "  - non-zero coefficient without reactants.");}
        for (String r : treeSet) {
            aModel.addElement(r);
            if(dbg){System.out.println(" "+r);}
        }

        msg = "<html>File: <b>"+database+"</b><br>&nbsp;<br>The following reaction";
        if(aModel.size()>1) {msg = msg+"s";}
        msg = msg + " have either<br>"+
                " - reactants with zero coefficients, or<br>"+
                " - non-zero coefficient without reactants.<br><br>"+
                "Please remove the unused reactants/coefficients<br>"+
                "in the following reactions before proceeding.<br>&nbsp;</html>";
        answer = showListDialog(parent, title, msg, "Proceed anyway", "Cancel", aModel);
    }
    if(!answer) {return false;}
    if(lists.chargeImbalance.size()>0) {
        treeSet = new java.util.TreeSet<String>(lists.chargeImbalance);
        aModel = new javax.swing.DefaultListModel<>(); // aModel = new javax.swing.DefaultListModel(); // java 1.6
        if(dbg) {System.out.println(nl+"The following reactions are NOT charge balanced:");}
        for (String r : treeSet) {
            aModel.addElement(r);
            if(dbg){System.out.println(" "+r);}
        }

        msg = "<html>File: <b>"+database+"</b><br>&nbsp;<br>The following reaction";
        if(aModel.size()>1) {msg = msg+"s";}
        msg = msg + "<br>";
        if(aModel.size()>1) {msg = msg+"are";} else {msg = msg+"is";}
        msg = msg + " NOT charge balanced.<br>";
        msg = msg + "Please correct ";
        if(aModel.size()>1) {msg = msg + "them";} else {msg = msg + "it";}
        msg = msg + " before proceeding.<br>&nbsp;</html>";
        answer = showListDialog(parent, title, msg, "Proceed anyway", "Cancel", aModel);
    }
    return answer;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="-- show List Dialog --">
  /** Show a dialog with a label, a list, and two buttons (for example OK/Cancel)
   * @param parent
   * @param title
   * @param labelText a label to be displayed on top of the JList
   * @param opt1 for example "OK"
   * @param opt2 for example "Cancel"
   * @param aModel a list of objects to display in a JList
   * @return true if the user selects "opt1" (OK); false otherwise */
  public static boolean showListDialog(java.awt.Component parent, String title,
          String labelText,
          String opt1, String opt2,
          javax.swing.DefaultListModel<String> aModel) { // javax.swing.DefaultListModel aModel) { // java 1.6
    if(opt1 == null || opt1.length() <=0) {opt1 = "OK";}
    if(opt2 == null || opt2.length() <=0) {opt1 = "Cancel";}
    //-- create the object to display in the JOptionPane
    javax.swing.JLabel label = new javax.swing.JLabel(labelText);
    Object[] array;
    if(aModel != null && !aModel.isEmpty()) {
        javax.swing.JList<String> aList = new javax.swing.JList<>(); // javax.swing.JList aList = new javax.swing.JList(); // java 1.6
        aList.setModel(aModel);
        aList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);    
        aList.setVisibleRowCount(5);
        javax.swing.JScrollPane aScrollPane = new javax.swing.JScrollPane();
        aScrollPane.setViewportView(aList);
        aList.setFocusable(false);
        array = new Object[2];
        array[0] = label;
        array[1] = aScrollPane;
    } else {
        array = new Object[1];
        array[0] = label;
    }
    //-- the option pane
    Object[] options = {opt1,opt2};
    javax.swing.JOptionPane pane = new javax.swing.JOptionPane(array,
            javax.swing.JOptionPane.ERROR_MESSAGE, javax.swing.JOptionPane.OK_CANCEL_OPTION,
            null, options, options[0]);
    //-- bind to the arrow keys
    java.util.Set<java.awt.AWTKeyStroke> keys = new java.util.HashSet<java.awt.AWTKeyStroke>(
          pane.getFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
    keys.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0));
    pane.setFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, keys);
    keys = new java.util.HashSet<java.awt.AWTKeyStroke>(
          pane.getFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
    keys.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0));
    pane.setFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, keys);
    //-- show the dialog
    if(!parent.isVisible()) {parent.setVisible(true);}
    javax.swing.JDialog dialog = pane.createDialog(parent, title);
    dialog.setVisible(true);
    //-- Get the return value
    int res = javax.swing.JOptionPane.CLOSED_OPTION; //default return value, signals nothing selected
    // Get the selected Value
    Object selectedValue = pane.getValue();
    // If none, then nothing selected
    if(selectedValue != null) {
        options = pane.getOptions();
        if (options == null) {// default buttons, no array specified
            if (selectedValue instanceof Integer) {
              res = ((Integer) selectedValue);
            }
        } else {// Array of option buttons specified
            for (int i = 0, n = options.length; i < n; i++) {
              if (options[i].equals(selectedValue)) {res = i; break;}
            }
        }
    }
    return res == javax.swing.JOptionPane.OK_OPTION;
  }
  //</editor-fold>

}
