package database;

import lib.common.MsgExceptn;
import lib.common.Util;
import lib.database.Complex;
import lib.kemi.H2O.IAPWSF95;
import lib.database.LibDB;
import lib.database.ProgramDataDB;
import lib.huvud.ProgramConf;

/** Search reactions in the databases.
 * <br>
 * Copyright (C) 2016-2020 I.Puigdomenech.
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
public class DBSearch {
  /** the search results: complexes and solids found in the database search */
  java.util.ArrayList<Complex> dat = new java.util.ArrayList<Complex>();
  /** temperature in degrees Celsius */
  double temperature_C = 25;
  /** pressure in bar */
  double pressure_bar = 25;
  /** number of components (soluble and solid) */
  int na;
  /** number of soluble complexes */
  int nx;
  /** number of solid reaction products */
  int nf;
  /** number of solid components */
  int solidC;

  //<editor-fold defaultstate="collapsed" desc="private fields">
  private ProgramConf pc;
  private ProgramDataDB pd;
  private FrameDBmain dbF;
  /** counter: the database being read */
  private int db;
  /** name of the database being read */
  private String complxFileName;
  /** the size in bytes of the file being read */
  private long complxFileNameSize;
  /** a counter indicating how many reactions have been read so far */
  private long cmplxNbr = 0;
  private final double SIZE_FACTOR_TXT = 54.675;
  private final double SIZE_FACTOR_BIN = 124.929;
  /** the binary database being read */
  private java.io.DataInputStream dis;
  /** the text database being read */
  private java.io.BufferedReader br;
  /** is "e-" among the components selected by the user? */
  private boolean redox;
  /** the data bases are searched again when new redoc components are found */
  private int nLoops;
  /** if <code>binaryOrText</code> = 2 reading a binary database<br>
   * if <code>binaryOrText</code> = 1 reading text database<br>
   * if <code>binaryOrText</code> = 0 then all files are closed (because they have been read) */
  private int binaryOrText;
  /** Contains the selected components, both the original,
   * selected by the user, and new redox components
   * (if the user selects Fe+2 and e-, then selectedComps[] contains Fe+2 and Fe+3) */
  private java.util.ArrayList<String> selectedComps = new java.util.ArrayList<String>();
  /** In advanced mode the user may select to exclude some redox couples,
   * for example HS-/SO4-2, or NH3/NO3-. In such a case the complex SO4-2
   * has to be excluded if HS- and e- are selected.
   * The list with these components/complexes is kept in comps_X[] */
  private java.util.ArrayList<String> comps_X = new java.util.ArrayList<String>();
  /** List of all other possible components for elements of selected-components.
   * For example: if CN- is selected, and it is listed under the elements C and N,
   * then <code>comps[]</code> will contain CO3-2, EDTA-4, NO3-, NH3, etc. */
  private java.util.ArrayList<String> comps = new java.util.ArrayList<String>();
  /** The new redox components. For example, if the user selects H+, e- and Fe+2,
   * then <code>selectedComps[]</code> contains H+, e-, Fe+2 and Fe+3 and
   * <code>rRedox[]</code> will contain the new redox components (Fe+3).
   * @see SearchData#selectedComps selectedComps */
  private java.util.ArrayList<Complex> rRedox = new java.util.ArrayList<Complex>();
  /** true if the current database has been searched to the end and therefore the next
   * database must be opened (if there are any databases left to be searched) */
  private boolean openNextFile;
  /** true if no databases could be openend and searched */
  private boolean noFilesFound;
  
  /** New-line character(s) to substitute "\n" */
  private static final String nl = System.getProperty("line.separator");
  //</editor-fold>

 /** Constructor of a DBSearch instance
  * @param programConf configuration data about the "calling" program
  * @param programData data about the "calling" program, including the list of databases
  * @throws DBSearch.SearchException   */
  public DBSearch(ProgramConf programConf, ProgramDataDB programData)
          throws DBSearch.SearchException {
    binaryOrText = 0;
    noFilesFound = true;
    openNextFile = true;
    if(programConf == null) {throw new SearchException("Error: programConf = null in \"DBSearch\" constructor");}
    this.pc = programConf;
    if(programData == null) {throw new SearchException("Error: programDataDB = null in \"DBSearch\" constructor");}
    this.pd = programData;
    this.temperature_C = pd.temperature_C;
    this.pressure_bar = pd.pressure_bar;
  }

  //<editor-fold defaultstate="collapsed" desc="searchComplexes">

 /** Searches the databases for reactions fitting the components selected by the user
  * and specified in the selectedComps[] list. If the electron "e-" is selected,
  * the databases may have to be scanned repeated times if new redox components
  * are found. For example, if {Fe+2, e-} are selected, after the first database
  * scan Fe+3 is found, and the databases must be scanned again for {Fe+2, Fe+3, e-}.
  * 
  * The reactions found in the search are stored in ArrayList "dat".
  * The progress bars in the lower half of the FrameDBmain show the search progress.
  * @param mainFrame
  * @throws DBSearch.SearchException */
  void searchComplexes (FrameDBmain mainFrame) throws DBSearch.SearchException {
    if(mainFrame == null) {MsgExceptn.exception("Error: mainFrame = null in \"searchComplexes\""); throw new SearchException();}
    this.dbF = mainFrame;
    if(dbF.modelSelectedComps.size() <=0) {
        MsgExceptn.exception("Error: modelSelectedComps.size() <=0 in \"searchComplexes\"");
        throw new SearchException();
    }
    if(pd.dataBasesList.size() <=0) {
        MsgExceptn.exception("Error: dataBasesList.size() <=0 in \"searchComplexes\"");
        throw new SearchException();
    }
    boolean found;

    try{
    if(pc.dbg) {System.out.println(FrameDBmain.LINE+nl+"--- Searching reactions");}


    nx =0;
    nf =0;
    binaryOrText = 0;
    nLoops = 1;

    // --- What components has the user selected?
    //  For redox systems (the user selected "e-" as a component)
    //  selectedComps[] contains the selected components, both the original,
    //  selected by the user, and new redox components. For example, if the user
    //  selects e- and Fe+2, then selectedComps[] contains Fe+2 and Fe+3)
    for(int i = 0; i < dbF.modelSelectedComps.size(); i++) {
        selectedComps.add(dbF.modelSelectedComps.get(i).toString());
    }

    redox = isComponentSelected("e-");
    na = selectedComps.size();
    solidC = 0;
    for(int i =0; i < na; i++) {
        if(Util.isSolid(selectedComps.get(i))) {solidC++;}
    }
    if(dbF.solidSelectedComps != solidC) {
        MsgExceptn.exception("Error in \"searchComplexes\":"+nl+
                "the number of solid components does not match!");
    }
    } catch (Exception ex) {throw new SearchException(Util.stack2string(ex));}
// todo ?
/* advanced option: exclude some redox reactions?
If redox And RedoxAsk Then  */

    if(!redoxChecks()) {
        System.out.println("--- Search cancelled.");
        return;
    }

    // -------- For redox systems (the user selected "e-" as a component) ----------
    // Make a list of all other possible components for elements of selected-components
    // for example: if CN- is selected, and it is listed under the elements C and N,
    //              the list will contain CO3-2, EDTA-4, NO3-, NH3, etc
    // The list is stored in:   comps[]

    // selectedComps[nSelectedComps] contains the selected components, both the original,
    //          selected by the user, and new redox components
    //          (if the user selects Fe+2, then selectedComps[] contains Fe+2 and Fe+3)

    // rRedox[] will contain the new redox components (Fe+3)

    // The user may select to exclude some redox couples,
    //    for example HS-/SO4-2, or NH3/NO3-
    // In such a case the complex SO4-2 might have to be excluded
    //    if HS- and e- are selected
    // The list with these components/complexes is kept in comps_X[]
    // --------------- Redox loop: new components ----------------------------------
    try{
    if(redox) {
      boolean excluded;
      String[] elemComp; String selCompName; String el;
      for(String selComp : selectedComps) {
        selCompName = selComp.toString();
        for(int k0 =0; k0< pd.elemComp.size(); k0++) { //loop through all components in the database (CO3-2,SO4-2,HS-,etc)
          elemComp = pd.elemComp.get(k0);
          if(Util.nameCompare(elemComp[1],selCompName)) { //got the component selected by the user
            //Note: array elemComp[0] contains: the name-of-the-element (e.g. "C"),
            //  the formula-of-the-component ("CN-"), and
            //  the name-of-the-component ("cyanide")
              el = elemComp[0]; //get the element corresponding to the component: e.g. "S" for SO4-2
              for(int k1 =0; k1< pd.elemComp.size(); k1++) { //loop through all components in the database (CO3-2,SO4-2,HS-,etc)
                elemComp = pd.elemComp.get(k1);
                if(elemComp[0].equals(el)) { //got the right element
                  if(k1 != k0) {
                    excluded = false;
                    if((el.equals("N") && !pd.redoxN) || (el.equals("S") && !pd.redoxS)
                            || (el.equals("P") && !pd.redoxP)) {
                            excluded = true;
                    }
                    if(!excluded) {
                        // check if this component is already in the list,
                        // if it is not, it must be considered as a possible redox component
                        found = false;
                        if(comps.size()>0) {
                            for(String t : comps) {
                              if(Util.nameCompare(elemComp[1],t)) {found = true; break;}
                            } //for j
                        }
                        if(!found) {comps.add(elemComp[1]);}
                    } else { //excluded:
                        //check if this component is already in the list,
                        //if it is not, it must be considered as a possible redox component
                        found = false;
                        if(comps_X.size() >0) {
                            for(String t : comps_X) {
                              if(Util.nameCompare(elemComp[1],t)) {found = true; break;}
                            } //for j
                        }
                        if(!found) {comps_X.add(elemComp[1]);}
                    } //excluded?
                  } //if k1 != k0
                } //elemComp[0] = el
              } //for k1;  list of all available components
          } //if elemComp[1] = selCompName
        } //for k0;  list of all available components
      } // for all selected components
      if(pc.dbg) {
        int n = comps.size();
        System.out.println("--- Possible new redox components:"+nl+"  comps[] size:"+n);
        for(int j=0; j<n; j++) {System.out.println("    "+comps.get(j));}
        n = comps_X.size();
        System.out.println("  comps_X[] size:"+n);
        for(int j=0; j<n; j++) {System.out.println("    "+comps_X.get(j));}
        if(comps.size() > 0 || n > 0) {System.out.println("---");}
      }
    } //if redox
    } catch (Exception ex) {throw new SearchException(Util.stack2string(ex));}
    //-------- end of make lists for redox systems ----------

    // --------------------------------------------------
    // -------- Redox loop: new species -----------------
    //   loop searching database for redox systems
    //   (for non-redox systems the loop is run only once)
    // --------------------------------------------------
    try{
    while(true) {

        // ---------------------------------------------
        //  Search databases for all reactions
        // ---------------------------------------------
        try {scanDataBases();}
        catch (SearchInternalException ex) {
            if(!(ex instanceof SearchInternalException)) {
                String msg = "Error in \"searchComplexes\":"+nl+Util.stack2string(ex);
                throw new SearchException(msg);
            } else {throw new SearchException(ex.getMessage());}
        }
        // ---------------------------------------------

        if(!redox) {
            if(pc.dbg) {System.out.println("--- Search reactions ended.");}
            return;
        }

        //--------- For redox systems:
        //  If components appear as reaction products it will be needed to search again.
        //  For example, if the components selected by the user are Fe+2 and e-,
        //  the database search will find Fe+3 as a complex,
        //  which must be regarded as a new component
        //
        //  Update: rRedox[] - contains a list with the new components.
        //   selectedComps[] - contains a list with all components:
        //                     those selected originally by the user and
        //                     the new ones found in the database search.
        int n_selectedComps_0 = selectedComps.size();
        for(Complex cplx : dat) {
            if(cplx.isRedox()) {
                found = false;
                String t1 = cplx.name;
                for(String t2 : comps) {
                    if(Util.nameCompare(t1,t2)) {
                        //found a complex (e g Fe+3) which is a component
                        //   check that it is not already selected
                        found = true;
                        for(String t3 : selectedComps) {
                            if(Util.nameCompare(t1,t3)) {found = false; break;}
                        } //for k
                        break;
                    }
                } //for j
                if(found) {
                    selectedComps.add(t1);
                    rRedox.add(cplx);
                } //if found
            } //if ePresent
        } //for cplx

        // --------------------------------
        // If new redox components have been found,
        // one must search the database again.
        // For example, if the components selected
        // are Fe+2 and e-,  the database search will
        // find Fe+3 as a new component and another
        // database search is needed including Fe+3
        // --------------------------------
        if(n_selectedComps_0 != selectedComps.size()) {
            nLoops++;
            continue; //while
        }
        break;
    } //while
    } catch (Exception ex) {throw new SearchException(Util.stack2string(ex));}
    // --------------------------------
    //   end loops searching database
    //   for redox systems
    // --------------------------------

    // --------------------------------------------------
    // -------- Redox loop: correct reactions -----------
    // If rRedox() is not empty:
    // Perform corrections for the new redox components.
    //
    // Example: if H+, e- and Fe+2 are choosen,
    // a new redox component Fe+3 is found, and
    // to the reaction: Fe+3 - H+ = FeOH+2
    // one must add:    Fe+2 - e- = Fe+3    etc
    try{
    if(rRedox.size() > 0) {
        double n1, n2, np;
        int rdxC, fnd, nTot;
        String lComp, lComp2;
        boolean needsMore;
        Complex rcomp;
        for(Complex cplx: dat) { // loop through all Complexes
            // because cplx is a reference to an object of type Complex in the ArrayList,
            // any change to cplx changes the object in the ArrayList (dat)
            while (true) {
                needsMore = false;
                np = 0;
                nTot = Math.min(cplx.reactionComp.size(),cplx.reactionCoef.size());
                for(int ic=0; ic < nTot; ic++) {
                    lComp = cplx.reactionComp.get(ic);
                    if(lComp != null && lComp.length() >0) {
                      n1 = cplx.reactionCoef.get(ic);
                      if(Util.isProton(lComp)) {np = n1;}
                      rdxC = -1; //is this component a new redox component ?
                      for(int ir=0; ir < rRedox.size(); ir++) {
                          if(Util.nameCompare(rRedox.get(ir).name, lComp)) {rdxC = ir; break;}
                      }//for ir
                      if(rdxC > -1) { //it has a redox component: Make corrections
                          needsMore = true;
                          rcomp = rRedox.get(rdxC);
                          //add the equilibrium constant of rRedox(rdxC)
                          cplx.constant = cplx.constant + n1 * rcomp.constant;
                          for(int i = 0; i < cplx.a.length; i++) {
                                if(cplx.a[i] != Complex.EMPTY && rcomp.a[i] != Complex.EMPTY) {cplx.a[i] = cplx.a[i] + n1 * rcomp.a[i];}
                          }
                          if(cplx.lookUp || rcomp.lookUp) {
                              if(!cplx.lookUp) {cplx.toLookUp();}
                              if(!rcomp.lookUp) {rcomp.toLookUp();}
                              for(int i = 0; i < cplx.logKarray.length; i++) {
                                for(int j = 0; j < cplx.logKarray[i].length; j++) {
                                    if(!Float.isNaN(cplx.logKarray[i][j]) && !Float.isNaN(rcomp.logKarray[i][j])) {
                                        cplx.logKarray[i][j] = cplx.logKarray[i][j] + (float)n1 * rcomp.logKarray[i][j];
                                    } else {cplx.logKarray[i][j] = Float.NaN;}
                                }
                              }
                          }
                          cplx.tMax = Math.min(cplx.tMax,rcomp.tMax);
                          cplx.pMax = Math.min(cplx.pMax,rcomp.pMax);
                          //add all stoichiometric coefficients of rRedox(rDxC)
                          cplx.reactionComp.set(ic,"");
                          cplx.reactionCoef.set(ic,0.);
                          for(int irr=0;
                                  irr < Math.min(rcomp.reactionComp.size(),rcomp.reactionCoef.size());
                                  irr++) {
                              lComp2 = rcomp.reactionComp.get(irr);
                              if(lComp2 == null || lComp2.length() <= 0) {continue;}
                              n2 = rcomp.reactionCoef.get(irr);                                  
                              fnd = -1;
                              for(int ic2=0;
                                          ic2<Math.min(cplx.reactionComp.size(), cplx.reactionCoef.size());
                                          ic2++) {
                                  if(Util.nameCompare(cplx.reactionComp.get(ic2), lComp2)) {
                                    fnd = ic2;
                                    cplx.reactionCoef.set(ic2, cplx.reactionCoef.get(ic2) + n1 * n2);
                                    break;
                                  }
                              }//for ic2
                              if(fnd < 0) {
                                cplx.reactionComp.add(lComp2);
                                if(Util.isProton(lComp2)) {
                                    cplx.reactionCoef.add(np + n1 * n2);
                                } else {
                                    cplx.reactionCoef.add(n1 * n2);
                                }//if "H+"
                              }//if fnd <0
                          }//for irr
                      }//if rDxC>-1
                    }//if lComp
                } //for ic
                if(!needsMore) {break;}
            } //while true
            System.out.println(cplx.toString());
            System.out.println(cplx.sortReactants().toString());
        }//for i (loop through all Complexes)
    } //iv sd.rRedox.size() > 0
    } catch (Exception ex) {throw new SearchException(Util.stack2string(ex));}

    // ------- End of: Perform corretions for
    //         new redox components.
    // ------------------------------------------


    // ---------------------------------------------
    //   end of database search
    // ---------------------------------------------

    if(pc.dbg) {System.out.println("--- Search reactions ended.");}
  } //searchComplexes
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="checkTemperature">
  /** Checks that for a given search (srch), where the temperature and
   * pressure are specified, each reaction has data that allows the calculation
   * of logK at the given temperature and pressure. If any of the reactions does
   * not have data, a message is displayed to the user, "false" is returned (not OK).
   * If all the reactions have adequate temperature-pressure data, "true" is returned (OK).
   * 
   * @param srch a search engine object, where the temperature and pressure are specified
   * @param parent a window to anchor messages
   * @param warning if true a "note" message will be displayed telling the user
   * that temperature extrapolations will be made
   * @return true if there are no problems with the temperature-pressure extrpolations,
   * false if some of the reactions do not have data at the required temperature
   */
  public static boolean checkTemperature(DBSearch srch, java.awt.Container parent, boolean warning) {
    boolean fnd = false, temperatureCorrectionsPossible = true;
    if(srch.temperature_C > 24.9 && srch.temperature_C < 25.1) {return temperatureCorrectionsPossible;}
    if(srch.nx+srch.nf < 1) {
        MsgExceptn.exception("Programming error: checking temperature before \"searchComplexes\".");
        return temperatureCorrectionsPossible;
    }
    java.util.ArrayList<String> items = new java.util.ArrayList<String>();
    // Is there T-P data for all reactions?
    double maxT = Double.MAX_VALUE, maxP = Double.MAX_VALUE;
    String txt;
    long cnt = 0;
    for(int ix=0; ix < srch.nx+srch.nf; ix++) {
        if(srch.temperature_C > srch.dat.get(ix).tMax || srch.pressure_bar > srch.dat.get(ix).pMax
                || Double.isNaN(srch.dat.get(ix).logKatTandP(srch.temperature_C, srch.pressure_bar))) {
            if(!fnd) {
                System.out.println("--------- Temperature-pressure extrapolations to "
                    +String.format("%.0f",srch.temperature_C)+" C, pressure = "+
                    String.format(java.util.Locale.ENGLISH,"%.2f",srch.pressure_bar)+" bar");
                fnd = true;
            }
            if(srch.dat.get(ix).pMax < 221) { // below critical point
                txt = String.format("%.3f",srch.dat.get(ix).pMax);
            } else {
                txt = String.format("%.0f",srch.dat.get(ix).pMax);
            }
            System.out.println("species \""+srch.dat.get(ix).name+
                    "\": missing T-P data, max temperature = "+
                    String.format("%.0f",srch.dat.get(ix).tMax)+", max pressure = "+txt);
            items.add(srch.dat.get(ix).name);
            maxT = Math.min(maxT, srch.dat.get(ix).tMax);
            maxP = Math.min(maxP, srch.dat.get(ix).pMax);
            cnt++;
        }
    }
    if(cnt >0) {
        System.out.println("---------");
        //javax.swing.DefaultListModel aModel = new javax.swing.DefaultListModel(); // java 1.6
        java.util.Iterator<String> iter = items.iterator();
        txt = "";
        while(iter.hasNext()) {txt = txt+iter.next()+"\n";}
        String msg = "<html><font size=\"+1\"><b>Error:</b></font><br>"+
                    "Temperature and pressure extrapolations are<br>"+
                    "requested to "+
                    String.format("%.0f",srch.temperature_C)+"°C and pressure = ";
        if(srch.pressure_bar > IAPWSF95.CRITICAL_pBar) {msg = msg + Util.formatNumAsInt(srch.pressure_bar);}
        else {msg = msg + String.format(java.util.Locale.ENGLISH,"%.2f",srch.pressure_bar);}
        msg = msg + " bar,<br>but the necessary temperature-pressure<br>"+
                    "data are missing for the following species:</html>";
        javax.swing.JLabel aLabel = new javax.swing.JLabel(msg);
        // javax.swing.JList aList = new javax.swing.JList(aModel); // java 1.6
        javax.swing.JTextArea aTextArea = new javax.swing.JTextArea(6,20);
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(aTextArea);
        scrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        aTextArea.append(txt);
        aTextArea.setEditable(true);
        aTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {evt.consume();}
            @Override
            public void keyTyped(java.awt.event.KeyEvent evt) {evt.consume();}
        });
        aTextArea.setFocusable(true);
        String p = "\"pSat\" (vapor-liquid equilibrium)";
        if(maxP > IAPWSF95.CRITICAL_pBar) {p = Util.formatNumAsInt(maxP)+" bar";}
        javax.swing.JLabel endLabel = new javax.swing.JLabel(
                "<html>For these reactions the maximum extrapolation<br>"+
                      "temperature is "+Util.formatNumAsInt(maxT)+"°C, and maximum<br>"+
                      "pressure is "+p+".<br>&nbsp;<br>"+
                "Please change the temperature in the menu \"Options\".</html>");
        Object[] o = {aLabel, scrollPane, endLabel};
        javax.swing.JOptionPane.showMessageDialog(parent, o, "Temperature extrapolations",
                                        javax.swing.JOptionPane.ERROR_MESSAGE);
        temperatureCorrectionsPossible = false;
    } else { // cnt <= 0
        temperatureCorrectionsPossible = true;
        if(warning) {
            String msg = "Note:"+nl+"Equilibrium constants will be"+nl+
                    "extrapolated from 25 to "+Util.formatNumAsInt(srch.temperature_C)+"°C"+nl+"(pressure ";
            if(srch.pressure_bar > IAPWSF95.CRITICAL_pBar) {msg = msg + Util.formatNumAsInt(srch.pressure_bar);}
            else {msg = msg + String.format(java.util.Locale.ENGLISH,"%.2f",srch.pressure_bar);}
            msg = msg + " bar)"+nl+"when you save the data file.";
            javax.swing.JOptionPane.showMessageDialog(parent,msg,
                    "Selected Temperature = "+Util.formatNumAsInt(srch.temperature_C),
                    javax.swing.JOptionPane.WARNING_MESSAGE);
        } // if warning
    } // cnt >0?
    return temperatureCorrectionsPossible;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="private methods">

  //<editor-fold defaultstate="collapsed" desc="scanDataBases">
 /** Reads all databases looking for all reaction products formed by the components
  * in the selectedComps[] list. The reactions found are stored in ArrayList "dat".
  * @throws DBSearch.SearchInternalException */
  private void scanDataBases() throws DBSearch.SearchInternalException {
    if(pc.dbg) {
        System.out.println("--- \"scanDataBases\", nLoops = "+nLoops+",  Selected components:");
        for (String selectedComp : selectedComps) {System.out.println("    " + selectedComp);}
        System.out.println("---");
    }
    int answer; String msg, t; boolean found; int i,j;
    boolean firstComplex = true;
        Complex rr;
    complxFileName = "";
    // the number of "components" will vary for redox systems,
    // for example, if Fe+2 is selected by the user, we have to search Fe+3 as well
    int nSelectedComps = selectedComps.size();

    // ---------------------------------------------
    //  Search all databases for reactions
    // ---------------------------------------------
    while(true) {
        rr = getOneComplex(firstComplex); //throws SearchInternalException()
        if(rr == null) {break;} //no more reactions
        if(rr.name.length() >0) {

            //---- make some primitive consistency checks, in case of a text file
            if(binaryOrText == 1 && nLoops ==1) {
                msg = rr.check();
                for(String s : rr.reactionComp) {
                    //check if the component is in the list of possible components
                    if(s != null && s.length() >0) {
                        found = false;
                        String[] elemComp;
                        for(j=0; j < pd.elemComp.size(); j++) {
                            elemComp = pd.elemComp.get(j);
                            if(Util.nameCompare(s,elemComp[1])) {found = true; break;}
                        } //for j
                        if(!found) {
                            t = "Component \""+s+"\" in complex \""+rr.name+"\""+nl+"not found in the element-files.";
                            if(msg.length() > 0) {msg = msg +nl+ t;} else {msg = t;}
                        }//not found
                    }
                }//for i
                if(msg != null) {
                    int nTot = Math.min(rr.reactionComp.size(),rr.reactionCoef.size());
                    System.out.println("---- Error \""+msg+"\""+nl+"      for complex \""+rr.name+"\", logK="+rr.constant+", ref.=\""+rr.reference+"\"");
                    for(i=0; i< nTot; i++) {System.out.print(" "+rr.reactionComp.get(i)+" "+rr.reactionCoef.get(i)+";");}
                    System.out.println();
                        Object[] opt = {"OK", "Cancel"};
                        answer = javax.swing.JOptionPane.showOptionDialog(dbF,
                                "Error in file \""+complxFileName+"\""+nl+"\""+msg+"\"",
                                pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
                                javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
                        if(answer != javax.swing.JOptionPane.YES_OPTION) {
                            try{
                                if(binaryOrText ==2 && dis != null) {dis.close();}
                                else if(binaryOrText ==1 && br != null) {br.close();}
                            } catch (java.io.IOException e) {}
                            throw new SearchInternalException();
                        }
                } //if msg !=null
            } //if text file and nLoops =1
            //---- end of consistency checks

            if(!rr.name.startsWith("@")) {
            // ----- Name does not begin with "@" (the normal case)
            //       Add the complex
                //check if we have already this complex. If found: replace it
                boolean include = true;
                if(redox) { //does this complex involve "e-" ?
                    boolean ePresent = false;
                    for(String s : rr.reactionComp) {
                        if (Util.isElectron(s)) {ePresent = true; break;}
                    }
                    if(ePresent) {
                      //does the user want to exclude this complex/component from redox equilibria?
                      for(j=0; j < comps_X.size(); j++) {
                          if(Util.nameCompare(rr.name,comps_X.get(j))) { //the complex (e g Fe+3) is a component to be excluded
                            include = false;  break;
                          }
                      } //for j
                      if(include) {
                        //It could be a new redox component (like Fe+2 - e- = Fe+3)
                        //if so: replace RRedox if already found, otherwise add it
                        found = false; //check if it is already selected
                        for(int k=0; k < selectedComps.size(); k++) {
                            if(Util.nameCompare(rr.name,selectedComps.get(k))) {found = true; break;}
                        } //for k
                        if(found) { //already selected: replace?
                            if(pc.dbg) {System.out.println("Complex already there (it was one of the components): "+rr.name);}
                            include = false; //the complex was already there
                            //-- Replace rRedox if same name + same stoichiometry
                            for(i=0; i < rRedox.size(); i++) {
                                if(Complex.sameNameAndStoichiometry(rr, rRedox.get(i))) {
                                    if(pc.dbg) {System.out.println("Complex already there and same stoichiometry: "+rr.name);}
                                    rRedox.set(i, rr); break;
                                }
                            }//for i
                        } //if found

                        ////The complex was not in the RRedox-list:
                        ////Should it be excluded because the user does not want
                        ////    redox equilibria for this element?
                        ////This is done with the function  "isRedoxComp" (= true if the component
                        ////    only contains one element and H/O.
                        ////    For example: SO4-2 contains only "S" isRedoxComp=true;
                        ////    but for Fe(SO4)2- isRedoxComp=false)
                        //boolean exclude = false;
                        //if(isRedoxComp("C", rr.name) && !pd.redoxC) {exclude = true;}
                        //if(isRedoxComp("N", rr.name) && !pd.redoxN) {exclude = true;}
                        //if(isRedoxComp("S", rr.name) && !pd.redoxS) {exclude = true;}
                        //if(isRedoxComp("P", rr.name) && !pd.redoxP) {exclude = true;}
                        //if(exclude) {include = false;}

                      } //if include
                    } //if ePresent
                } //if redox

                //If  "include":  add the complex (replace otherwise)
                if(include) {
                    found = false;
                    for(i = 0; i < dat.size(); i++) {
                        if(Util.nameCompare(rr.name,dat.get(i).name)) { //replace!
                            dat.set(i, rr); found = true; break;
                        }
                    }//for i
                    if(!found) { //add!
                        //allSolids:  0=include all solids; 1=exclude (cr); 2=exclude (c); 3=exclude (cr)&(c)
                        boolean excludeSolid = true;
                        if(!Util.is_cr_or_c_solid(rr.name) ||
                           pd.allSolids == 0 ||
                           (Util.is_cr_solid(rr.name) && pd.allSolids !=1 && pd.allSolids !=3) ||
                           (Util.is_c_solid(rr.name) && pd.allSolids <2)) {excludeSolid = false;}
                        if(excludeSolid) {
                            final String s; if(pd.allSolids == 1) {s="(cr) solids excluded!";}
                            else if(pd.allSolids == 2) {s="(c) solids excluded!";}
                            else if(pd.allSolids == 3) {s="(cr) & (c) solids excluded!";} else {s = " ";}
                            javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
                              dbF.jLabel_cr_solids.setText(s);
                            }});
                        } else { //include solid or not a solid
                            if(Util.isSolid(rr.name)) {
                                dat.add(rr);
                                nf++;
                            } //if solid
                            else { //soluble
                                dat.add(nx, rr);
                                nx++;
                            } //solid/soluble?
                        } //excludeSolid?
                    }//if !found
                }//if include
            // ----- end of: name does Not begin with a "@"
            } else {
            // ----- Complex name begins with "@"
            //       Withdraw the complex if it already has been read from the database
                rr.name = rr.name.substring(1);
                // first see if it is a "normal" complex
                if(nx+nf >0) {
                    i = 0;
                    do{ //while (i < (nx+nf))    --- loop through all reactions
                        if(Util.nameCompare(dat.get(i).name,rr.name)) {
                            dat.remove(i);
                            if(i < nx) {nx--;} else {nf--;} 
                        }
                        else {i++;}
                    } while (i < (nx+nf));
                } //if nx+nf >0
                // if redox: remove any components that are equivalent to the @-complex
                if(redox) {
                    int fnd = -1;
                    for(j = dbF.modelSelectedComps.size(); //search only new redox comps.
                            j < nSelectedComps; j++) {
                        if(Util.nameCompare(rr.name,selectedComps.get(j))) {fnd = j; break;}
                    }
                    if(fnd > -1) {
                        //First remove from components list
                        selectedComps.remove(fnd);
                        nSelectedComps--;
                        //2nd remove from "rRedox"
                        for(j =0; j<rRedox.size(); j++) {
                            if(Util.nameCompare(rr.name,rRedox.get(j).toString())) {rRedox.remove(j); break;}
                        }//for j
                        //remove from "dat" all reaction products formed by this redox component:
                        if(nx+nf >0) {
                            i = 0;
                            do{ //while (i < (nx+nf))    --- loop through all reactions
                                found = false;
                                for(String s : dat.get(i).reactionComp) {
                                    if(Util.nameCompare(s,rr.name)) {found = true; break;}
                                }
                                if(found) {
                                    dat.remove(i);
                                    if(i < nx) {nx--;} else {nf--;} 
                                } //if found
                                else {i++;}
                            } while (i < (nx+nf));
                        } //if nx+nf >0
                    } //if fnd >-1
                } //if redox
            } //----- end of: the name begins with a "@"                
        } //if rr.name.length() >0
        if(binaryOrText == 0) {break;}
        firstComplex = false;
    } //while
    // -----------------------------------------------
    //  end of database search
    // -----------------------------------------------
    try{
        if(binaryOrText ==2 && dis != null) {dis.close();}
        else if(binaryOrText ==1 && br != null) {br.close();}
    } catch (java.io.IOException e) {}
    dbF.updateProgressBarLabel(" ", 0);
    dbF.updateProgressBar(0);

  } //scanDataBases()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="getOneComplex">
  /** This routine will get the next "complex" with the condition that
   * all its components must be in the "selected-components" list.
   * Both binary and text files will be read.
   * This routine is NOT called to convert database files (Text <=> Binary).
   * <p> On output:<br>
   * if <code>binaryOrText</code> = 2 reading binary database<br>
   * if <code>binaryOrText</code> = 1 reading text database<br>
   * if <code>binaryOrText</code> = 0 then all files are closed
   *   (because they have been read)
   * 
   * @param firstComplex if true the first file is opened and the first complex
   * is searched for; if false then find the next complex from the list of
   * database files
   * @return
   * @see lib.database.LibSearch#getComplex(boolean) getComplex
   * @throws DBSearch.SearchInternalException */
  private Complex getOneComplex(boolean firstComplex) throws DBSearch.SearchInternalException {
    boolean protonPresent;
    String msg;
    if(firstComplex) {
        //open the first file
        openNextFile = true;
        db = 0;
    }

    while (db < pd.dataBasesList.size()) {
      if(openNextFile) {
        try{
            if(dis != null) {dis.close();} else if(br != null) {br.close();}
        } catch (java.io.IOException ioe) {MsgExceptn.msg(ioe.getMessage());}
        complxFileName = pd.dataBasesList.get(db);
        if(complxFileName == null || complxFileName.length() <=0) {continue;}
        java.io.File dbf = new java.io.File(complxFileName);
        if(!dbf.exists() || !dbf.canRead()) {
            msg = "Error: can not open file"+nl+
                         "    \""+complxFileName+"\".";
            if(!dbf.exists()) {msg = msg +nl+ "(the file does not exist)."+nl+
                                              "Search terminated";}
            throw new SearchInternalException(msg);
        }
        complxFileNameSize = dbf.length();
        cmplxNbr = 0;
        this.dbF.updateProgressBarLabel("Searching \""+complxFileName+"\"", nLoops);
        this.dbF.updateProgressBar(0);
        //--- text or binary?
        try{
            if(binaryOrText ==2 && dis != null) {dis.close();}
            else if(binaryOrText ==1 && br != null) {br.close();}
        } catch (java.io.IOException e) {}
        try{
            if(complxFileName.toLowerCase().endsWith("db")) { //--- binary file
                binaryOrText = 2;
                dis = new java.io.DataInputStream(new java.io.FileInputStream(dbf));
            } else { //--- text file
                binaryOrText = 1;
                br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(
                                new java.io.FileInputStream(dbf),"UTF8"));
                // -- comments at the top of the file
                // topComments = rd.dataLineComment.toString();
            } //--- text or binary?
        }
        catch (Exception ex) {
            try{
                if(dis != null) {dis.close();} else if(br != null) {br.close();}
            } catch (Exception ioe) {MsgExceptn.msg(ioe.getMessage());}
            msg = "Error: "+ex.toString()+nl+
                    "while trying to open file: \""+complxFileName+"\"."+nl+"search terminated";
            throw new SearchInternalException(msg);
        }
        noFilesFound = false;
        openNextFile = false;
        if(pc.dbg) {System.out.println("Scanning database \""+complxFileName+"\"");}
      } //if sd.openNextFile

      Complex complex = null;
      loopComplex:
      while (true) {
        cmplxNbr++;
        if(binaryOrText ==2) { //Binary complex database
            try {
                complex = LibDB.getBinComplex(dis);
                dbF.updateProgressBar((int)(100*(double)cmplxNbr*SIZE_FACTOR_BIN
                        /(double)complxFileNameSize));
            }
            catch (LibDB.ReadBinCmplxException ex) {
                msg = "Error: in \"getOneComplex\", cmplxNbr = "+cmplxNbr+nl+
                    "ReadBinCmplxException: "+ex.getMessage()+nl+
                    "in file: \""+complxFileName+"\"";
                throw new SearchInternalException(msg);
            }
        } //binaryOrText =2 (Binary file)
        else if(binaryOrText ==1) { // Text  complex database
            try {
                try{
                    complex = LibDB.getTxtComplex(br);
                    dbF.updateProgressBar((int)(100*(double)cmplxNbr*SIZE_FACTOR_TXT
                            /(double)complxFileNameSize));
                }
                catch (LibDB.EndOfFileException ex) {complex = null;}                    
            }
            catch (LibDB.ReadTxtCmplxException ex) {
                msg = "Error: in \"getOneComplex\", cmplxNbr = "+cmplxNbr+nl+
                    ex.getMessage()+nl+
                    "in file: \""+complxFileName+"\"";
                throw new SearchInternalException(msg);
            }
        } //binaryOrText =1 (Text file)

        if(complex == null || complex.name == null) {break;} //  loopComplex // end-of-file, open next database
        if(complex.name.startsWith("@")) {return complex;}
        // --- is this a species formed from the selected components?
        protonPresent = false;
        int nTot = Math.min(complex.reactionComp.size(),complex.reactionCoef.size());
        for(int i=0; i < nTot; i++) {
            if(complex.reactionComp.get(i) == null || complex.reactionComp.get(i).length() <=0 ||
               Util.isWater(complex.reactionComp.get(i))) {continue;} //H2O
            if(!isComponentSelected(complex.reactionComp.get(i)) &&
               Math.abs(complex.reactionCoef.get(i)) >0.0001) {continue loopComplex;}
            if(Util.isProton(complex.reactionComp.get(i))) {protonPresent = true;}
        } //for i
        // all components are selected: select complex
        return complex;
      } //while (true)  --- loopComplex:

      // ----- no complex found: end-of-file, or error. Get next file
      db++;
      openNextFile = true;
    } //while sd.db < pd.dataBasesList.size()

    if(noFilesFound) { // this should not happen...
        MsgExceptn.exception("Error: none of the databases could be found.");
        throw new SearchInternalException();
    }
    try{
        if(dis != null) {dis.close();} else if(br != null) {br.close();}
    } catch (java.io.IOException ioe) {MsgExceptn.msg(ioe.getMessage());}
    binaryOrText = 0;
    return null; //return null if no more reactions
  } //getOneComplex
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="isComponentSelected">
  /** find out if "component" is in the list of selected components
   * (in ArrayList selectedComps) */
  private boolean isComponentSelected(String component) {
      for(int i=0; i< selectedComps.size(); i++) {
          if(Util.nameCompare(component,selectedComps.get(i))) {return true;}
      }
      return false;
  } //isComponentSelected
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="isRedoxComp(element, component)">
  /** For a given component, and one of its elements:
   * is this a component which might be involved in redox equilibria?
   * This is used to warn the user of a possible problem in the choice of components.
   * For example, if the user selects both V+2 and VO2+, it might be better to choose
   * instead V+2 and e-.<br>
   * Rule: if the component is formed by only the element, oxygen and hydrogen,
   * then return <code>true</code>. Examples: Fe+2, CrO4-2, H2PO4-
   * (return <code>true</code> for the elements Fe, Cr and P),
   * while for CN- return <code>false</code>. */
  private static boolean isRedoxComp(String element, String component) {
      StringBuilder comp = new StringBuilder(component);
      //---If it does not contain the element in the name, it is not a redox couple
      int i = component.indexOf(element);
      if(i<0) {return false;}
      //---Take away *, @, at the beginning of the name
      if(comp.length() >=2) {
          if(comp.charAt(0) == '*' || comp.charAt(0) == '@') {
              comp.delete(0, 1);
          }
      }
      //--- Take away diverse endings:
      // --- Take away (c), (s), (l), (g)
      if(comp.length() >=4) {
          String end = comp.substring(comp.length()-3, comp.length());
          if(end.equalsIgnoreCase("(c)") || end.equalsIgnoreCase("(s)") ||
             end.equalsIgnoreCase("(l)") || end.equalsIgnoreCase("(g)")) {
              comp.delete(comp.length()-3, comp.length());
          }
      }
      // --- Take away (cr), (am), (aq)
      if(comp.length() >=5) {
          String end = comp.substring(comp.length()-4, comp.length());
          if(end.equalsIgnoreCase("(cr)") || end.equalsIgnoreCase("(am)") ||
             end.equalsIgnoreCase("(aq)")) {comp.delete(comp.length()-3, comp.length());}
      }
      // --- Take away (vit)
      if(comp.length() >=6) {
          String end = comp.substring(comp.length()-5, comp.length());
          if(end.equalsIgnoreCase("(vit)")) {comp.delete(comp.length()-3, comp.length());}
      }
      //--- Take away numbers charge (+/-) and space
      i = 0;
      while(i<comp.length()) {
          if((comp.charAt(i)>='0' && comp.charAt(i)<='9') || comp.charAt(i) == '+' ||
             comp.charAt(i) == '-' ||
             comp.charAt(i) == '\u2013' || comp.charAt(i) == '\u2212' || // unicode en dash or minus
             comp.charAt(i) == '.' || comp.charAt(i) == ';' || comp.charAt(i) == ',' ||
             comp.charAt(i) == '(' || comp.charAt(i) == ')' ||
             comp.charAt(i) == '[' || comp.charAt(i) == ']' ||
             comp.charAt(i) == '{' || comp.charAt(i) == '}') {
                comp.delete(i, i+1);
          } else {i++;}
      } //while
      //--- Take away the element name
      while(true) {
          i = comp.indexOf(element);
          if(i<0) {break;}
          comp.delete(i, i+element.length());
      } //while
      //--- Take away oxygen
      while(true) {
          i = comp.indexOf("O");
          if(i<0) {break;}
          comp.delete(i, i+1);
      } //while
      //--- Take away hydrogen
      while(true) {
          i = comp.indexOf("H");
          if(i<0) {break;}
          comp.delete(i, i+1);
      } //while
      //--- Take away white space
      i = 0;
      while(i<comp.length()) {
          if(Character.isWhitespace(comp.charAt(i))) {comp.delete(i, i+1);}
          else {i++;}
      } //while
      return comp.length() <= 0;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="redoxChecks">
  /** Checks that the user does not select both Fe+2 and Fe+3 as components...
   * @return  true (go on with the search) if there was no problem (two similar components were not selected)
   * or if the user clicks to go ahead anyway. Returns false (cancel the search) if there was a
   * problem and the user selected to cancel the search
   */
  private boolean redoxChecks() {
    if(pc.dbg) {System.out.println("--- \"redoxChecks\".  redox = "+redox);}
    //----------
    // Check that the user does not select both Fe+2 and Fe+3 as components...
    //----------
    // This is done with the function  "is_redox" that =True if the component
    //    only contains one element and H/O. For example: SO4-2 contains only "S" is_redox%=True; but for CN- is_redox%=False
    String[] elemComp; String selCompName, el; boolean problem = false;
    for(int i =0; i< selectedComps.size(); i++) { //loop through all components selected by the user (e.g.: H+,CO3-,Fe+3)
      selCompName = selectedComps.get(i).toString();
      for(int k0 =0; k0< pd.elemComp.size(); k0++) { //loop through all components in the database (CO3-2,SO4-2,HS-,etc)
        elemComp = pd.elemComp.get(k0);
        if(Util.nameCompare(elemComp[1],selCompName)) { //this component has been selected by the user
            //Note: array ElemComp[0] contains: the name-of-the-element (e.g. "C"),
            //  the formula-of-the-component ("CN-"), and
            //  the name-of-the-component ("cyanide")
            el = elemComp[0]; //get the element corresponding to the component: e.g. "S" for SO4-2
            if(!isRedoxComp(el, selCompName)) {break;} // k0
            if(!pd.redoxAsk || redox) {
                if(el.equals("N") && !pd.redoxN) {break;} // k0
                else if(el.equals("P") && !pd.redoxP) {break;} // k0
                else if(el.equals("S") && !pd.redoxS) {break;} // k0
            }
            //the component selected by the user "is redox": it only contains one element in addition to H/O
            //  such as H+, SO4-2 or Fe+3.
            //Look and see if there are other components for the same element
            for(int k1 =0; k1< pd.elemComp.size(); k1++) { //loop through all components in the database (CO3-2,SO4-2,HS-,etc)
              elemComp = pd.elemComp.get(k1);
              if(elemComp[0].equals(el)) { //got the right element
                  if(!Util.nameCompare(elemComp[1],selCompName)) { //look at all other components with the same element
                      if(isRedoxComp(el, elemComp[1])) {
                        // this component also "is redox".
                        // For example, the user selected SO4-2 and this component is HS-
                        // check if this redox component has also been selected by the user
                        for(int k2 =i; k2< selectedComps.size(); k2++) { //search the rest of the selected components
                          if(Util.nameCompare(elemComp[1],selectedComps.get(k2).toString())) {
                            problem = true;
                            String msg = "Warning!"+nl+"You selected two components for the same element:"+
                                  "  \""+selCompName+"\" and \""+elemComp[1]+"\""+nl+
                                  "They are probably related by redox reactions."+nl;
                            if(redox) {msg = msg + "You should remove one of these components.";}
                            else {
                                msg = msg + "You should instead select \"e-\" as a component, and remove either "+
                                        "\""+selCompName+"\" or \""+elemComp[1]+"\".";
                            } //if redox
                            msg = msg + nl + "The calculations will then determine their respective concentrations."+nl+nl+
                                    "Are you sure that you want to continue ?";
                            System.out.println("- - - - - -"+nl+msg+nl+"- - - - - -");
                            Object[] opt = {"Continue", "Cancel"};
                            int answer = javax.swing.JOptionPane.showOptionDialog(
                                    dbF, msg, pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
                                    javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
                            if(answer != javax.swing.JOptionPane.YES_OPTION) {
                              System.out.println("Answer: Cancel");
                              return false;
                            }
                            System.out.println("Answer: Continue anyway");
                          } //if elemComp[1] = dbF.modelSelectedComps[k2]
                        } //for k2
                      }//isRedoxComp(el, elemComp[1])
                  }//elemComp[1] != selCompName
              }//elemComp[0] = el
            } //for k1
        } //if elemComp[1] = selCompName
      } //for k0
    } //for i
    //---------- check ends
    if(pc.dbg && !problem) {System.out.println("Checks ok.");}
    return true;
  } //redoxChecks
  //</editor-fold>

  //</editor-fold>

class SearchException extends Exception {
    public SearchException() {super();}
    public SearchException(String txt) {super(txt);}
} // SearchException
private class SearchInternalException extends Exception {
    public SearchInternalException() {super();}
    public SearchInternalException(String txt) {super(txt);}
} //SearchInternalException

}
