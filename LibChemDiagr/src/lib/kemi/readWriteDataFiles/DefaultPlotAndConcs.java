package lib.kemi.readWriteDataFiles;

import lib.common.Util;
import lib.kemi.chem.Chem;

/** If a data file does not contain a plot definition, nor the concentrations
 * for each chemical component, the static methods in this class will assign
 * default values for a plot type and for the concentrations.
 *
 * Copyright (C) 2014-2019 I.Puigdomenech.
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
public class DefaultPlotAndConcs {
  public DefaultPlotAndConcs() {} //a constructor

  //<editor-fold defaultstate="collapsed" desc="setDefaultPlot">
  /** Sets a default plot type (log(conc) in the Y-axis) and chooses a default
   * component for the X-axis.
   * @param cs a reference to an instance of Chem.ChemSystem
   * @param diag a reference to an instance of Chem.Diagr
   * @param dgrC a reference to an instance of Chem.DiagrConcs
   */
  public static void setDefaultPlot (
          Chem.ChemSystem cs,
          Chem.Diagr diag,
          Chem.DiagrConcs dgrC) {
    //--- default plot type
    diag.plotType = 3; // log(conc) in the Y-axis
    diag.yLow = -9; diag.yHigh = 1;
    diag.compY = -1;
    diag.compMain = -1;
    //--- take a default component for the X-axis
    diag.compX = -1;
    //is there H+ among the components in the chemical system?
    for(int i =0; i < cs.Na; i++) {
      if(Util.isProton(cs.namn.identC[i])) {diag.compX = i; break;}
    } //for i
    if(diag.compX <0) {
        //is there e- among the components in the chemical system?
        for(int i =0; i < cs.Na; i++) {
            if(Util.isElectron(cs.namn.identC[i])) {diag.compX = i; break;}
        } //for i
    }
    if(diag.compX <0 && dgrC != null) { //no H+ or e- among the components
        for(int i =0; i < cs.Na; i++) {
          //is the concentration varied? (assuming concs. are given)
          if(dgrC.hur[i] ==2 || dgrC.hur[i] ==3 || dgrC.hur[i] ==5) {
            diag.compX = i; break;
          } //conc. varied
        } //for i
    }
    if(diag.compX < 0) {
          for(int i =0; i < cs.Na; i++) {
            // take the first non-cation
            if(!Util.isCation(cs.namn.identC[i])) {
              diag.compX = i; break;
            } //conc. varied
          } //for i
    }
    //nothing works: take the first component
    if(diag.compX < 0) {diag.compX = 0;}

  } //setDefaultPlot
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="checkConcsInAxesAndMain">
  /** Checks that components in the axes have varied concentrations (if needed)
   * and that the "main" component (in a Predom diagram) has a fixed concentration.
   * It also sets non-varied concentrations for any component that is not in the axes.
   *
   * @param namn a reference to an instance of Chem.NamesEtc
   * @param diag a reference to an instance of Chem.Diagr
   * @param dgrC a reference to an instance of Chem.DiagrConcs
   * @param dbg if true some "debug" output is printed
   * @param kth if <code>true</code> deffault total concetration for cationic
   * components is 0.01 instead of 1e-5. Special settings for students at
   * the school of chemistry at the Royal Institute of Technology (KTH)
   * in Stockholm, Sweden.
   * @see DefaultPlotAndConcs#setDefaultConcs setDefaultConcs
   * @see DefaultPlotAndConcs#setDefaultConc(int, java.lang.String, lib.kemi.chem.Chem.DiagrConcs) setDefaultConc */
  public static void checkConcsInAxesAndMain(
          Chem.ChemSystem.NamesEtc namn,
          Chem.Diagr diag,
          Chem.DiagrConcs dgrC,
          boolean dbg, boolean kth) {
    //
    if(dbg) {System.out.println("checkConcsInAxesAndMain - plotType ="+diag.plotType
                    +", compMain ="+diag.compMain+", compX ="+diag.compX+", compY ="+diag.compY);}
    for(int i =0; i < dgrC.hur.length; i++) {
        if(i == diag.compX
                || (diag.plotType ==0 && (i == diag.compMain || i == diag.compY))) {
            //if(dbg) {System.out.println("  i="+i+" ("+namn.identC[i]+"), ok");}
            continue;
        }
        if(dgrC.hur[i] <= 0 || dgrC.hur[i] >5 || Double.isNaN(dgrC.cLow[i])
                || (dgrC.hur[i] !=1 && dgrC.hur[i] !=4 // concentration is varied
                    && Double.isNaN(dgrC.cHigh[i]))) {
            //if(dbg) {System.out.println("  i="+i+" ("+namn.identC[i]+"), setting defaults.");}
            setDefaultConc(i, namn.identC[i], dgrC, kth);
        }
    } //for i

    //--- is the concentration for the X-axis component not varied?
    if(diag.compX >=0 &&
       (dgrC.hur[diag.compX] !=2 && dgrC.hur[diag.compX] !=3 && dgrC.hur[diag.compX] !=5)) {
          //make it varied
          dgrC.hur[diag.compX] = 5; //"LAV" log(actiity) varied
          dgrC.cLow[diag.compX] = -10; dgrC.cHigh[diag.compX] = -1;
          if(Util.isProton(namn.identC[diag.compX])) {dgrC.cLow[diag.compX] = -12;}
          if(Util.isElectron(namn.identC[diag.compX])) {dgrC.cLow[diag.compX] = -10; dgrC.cHigh[diag.compX] = 10;}
    } //if conc. not varied

    //--- is the concentration for the Y-axis (Predom) component not varied?
    if(diag.compY >=0 && diag.plotType == 0 &&
       (dgrC.hur[diag.compY] !=2 && dgrC.hur[diag.compY] !=3 && dgrC.hur[diag.compY] !=5)) {
          //make it varied
          dgrC.hur[diag.compY] = 5; //"LAV" log(actiity) varied
          dgrC.cLow[diag.compY] = -10; dgrC.cHigh[diag.compY] = -1;
          if(Util.isProton(namn.identC[diag.compY])) {dgrC.cLow[diag.compY] = -12;}
          if(Util.isElectron(namn.identC[diag.compY])) {dgrC.cLow[diag.compY] = -10; dgrC.cHigh[diag.compY] = 10;}
    } //if conc. not varied

    //--- is the concentration for the Main component (Predom) not in an axis and not fixed?
    if(diag.compMain >=0 && diag.compMain != diag.compY && diag.compMain != diag.compX &&
       (dgrC.hur[diag.compMain] ==2 || dgrC.hur[diag.compMain] ==3 || dgrC.hur[diag.compMain] ==5)) {
          //make it fixed
          dgrC.hur[diag.compMain] = 1; //"T" total conc. fixed
          dgrC.cLow[diag.compMain] = 1e-6; dgrC.cHigh[diag.compMain] = 0;
    } //if conc. not varied

  } //checkConcsInAxesAndMain
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="setDefaultConc(ChemSystem, DiagrConcs)">
  /** Sets default concentrations (fixed, not varied) for all components in a
   * chemcial system. After this procedure you should run
   * <code>checkConcsInAxesAndMain</code> to make sure
   * that components in the axes have varied concentrations.
   * @param cs a reference to an instance of Chem.ChemSystem
   * @param dgrC a reference to an instance of Chem.DiagrConcs
   * @param kth if <code>true</code> deffault total concetration for cationic
   * components is 0.01 instead of 1e-5. Special settings for students at
   * the school of chemistry at the Royal Institute of Technology (KTH)
   * in Stockholm, Sweden.
   * @see DefaultPlotAndConcs#checkConcsInAxesAndMain checkConcsInAxesAndMain   */
  public static void setDefaultConcs (
            Chem.ChemSystem cs,
            Chem.DiagrConcs dgrC,
            boolean kth) {
    for(int i =0; i < cs.Na; i++) {
      setDefaultConc(i, cs.namn.identC[i], dgrC, kth);
    } //for i
  } //setDefaultConcs (ChemSystem, DiagrConcs)
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="setDefaultConc(i, compName, DiagrConcs)">
  /** Sets a default concentration (fixed, not varied) for a given
   * component "i" in a chemcial system.
   * @param i the component (0 to (cs.Na-1)) for which default values of
   * <code>hur</code>, <code>cLow</code> and <code>cHigh</code> are needed
   * @param compName the name of the component, for example "CO3 2-"
   * @param dgrC a reference to an instance of Chem.DiagrConcs
   * @param kth if <code>true</code> deffault total concetration for cationic
   * components is 0.01 instead of 1e-5. Special settings for students at
   * the school of chemistry at the Royal Institute of Technology (KTH)
   * in Stockholm, Sweden.
   * @see DefaultPlotAndConcs#checkConcsInAxesAndMain checkConcsInAxesAndMain
   * @see DefaultPlotAndConcs#setDefaultConcs setDefaultConcs
   * @see Chem.DiagrConcs#hur hur
   * @see Chem.DiagrConcs#cLow cLow
   * @see Chem.DiagrConcs#cHigh cHigh
   */
  public static void setDefaultConc (int i, String compName, Chem.DiagrConcs dgrC, boolean kth) {
      // defaults
      dgrC.hur[i] = 1;   //"T" fixed total concentration
      dgrC.cHigh[i] = 0; //not used
      if(kth) {dgrC.cLow[i] = 0.01;} else {dgrC.cLow[i] = 1e-5;}

      //is this e-
      if(Util.isElectron(compName)) {
        dgrC.cLow[i] = -8.5;
        dgrC.hur[i] = 4; //"LA" fixed log(activity)
      } // e-
      else if(Util.isProton(compName)) {
        dgrC.cLow[i] = -7; //pH=7
        dgrC.hur[i] = 4;   //"LA" fixed log(activity)
      } // H+
      else {
        if(!Util.isCation(compName)) {dgrC.cLow[i] = 0.01;}
        if(Util.isGas(compName)) {
          dgrC.cLow[i] = -3.5; dgrC.hur[i] = 4; //"LA" fixed log(activity)
        } //if gas
        else if(Util.isWater(compName)) {
          dgrC.cLow[i] = 0; dgrC.hur[i] = 4; //"LA" fixed log(activity)
        } //if H2O
      } //not H_present and not e_present
  } //setDefaultConcs(i, compName, DiagrConcs)
  // </editor-fold>

} 