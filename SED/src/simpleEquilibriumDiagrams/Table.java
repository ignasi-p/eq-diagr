package simpleEquilibriumDiagrams;

import lib.kemi.chem.Chem;

/**  Methods to save numerical results into output text file(s).
 * <br>
 * Copyright (C) 2014-2015 I.Puigdomenech.
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
public class Table {
private java.io.File tf = null;
private java.io.PrintWriter tPW = null;
private final static java.util.Locale engl = java.util.Locale.ENGLISH;
private SED sed = null;
/** Where messages will be printed. It may be <code>System.out</code>.
 * If null, <code>System.out</code> is used. */
private final java.io.PrintStream out;
/** New-line character(s) to substitute "\n". */
private static final String nl = System.getProperty("line.separator");
/** Text at the start of a text line to indicate that the line is a comment */
private String commentLineStart = "\"";
/** Text appended to the end of comment lines*/
private String commentLineEnd = "\"";
private static final String SLASH = java.io.File.separator;

/** Constructs an object of this class
 * @param sed0 the program SED frame
 * @param err0 Where errors will be printed. It may be <code>System.err</code>.
 * If null, <code>System.err</code> is used.
 * @param out0 Where messages will be printed. It may be <code>System.out</code>.
 * If null, <code>System.out</code> is used. */
public Table(SED sed0, java.io.PrintStream err0, java.io.PrintStream out0) {
    this.sed = sed0;
    if(out0 != null) {this.out = out0;} else {this.out = System.out;}
    commentLineStart = sed.tblCommentStart;
    commentLineEnd = sed.tblCommentEnd;
} //constructor

//<editor-fold defaultstate="collapsed" desc="tableHeader()">
/** Prepare a new output text file ro receive the table data. The files
 * created by this method have names in series: SED000.ext, SED001.ext,
 * SED002.ext ... SED999.ext. If you delete a file in the middle of the
 * series, for example if you remove file SED002 when you have files
 * SED000 to SED008, the next file create will be SED009.
 * If file SED999.ext already exists, an error occurs.
 * @param ch where data on the chemical system are stored
 * @param tblExtension
 * @return false if an error occurs */
boolean tableHeader(Chem ch, java.io.File tableFile) {
    if(sed.dbg) {
        out.println("--- tableHeader"+nl+
                    "    table output file: "+tableFile.getAbsolutePath());
    }
    this.tf = tableFile;
    Chem.ChemSystem cs = ch.chemSystem;
    Chem.ChemSystem.NamesEtc namn = cs.namn;
    Chem.Diagr diag = ch.diag;
    Chem.DiagrConcs dgrC = ch.diagrConcs;
    String msg;
    try {
        tPW = new java.io.PrintWriter(
                new java.io.BufferedWriter(
                new java.io.FileWriter(tf)));
    }
    catch (java.io.IOException ex) {
        msg = "Error: \""+ex.getMessage()+"\""+nl+
                            "while opening input file \""+tf.getAbsolutePath()+"\"";
        sed.showErrMsgBx(msg, 1);
        tableClose();
        return false;
    }

    msg = "Writing table file:"+nl+"    \""+tf.getAbsolutePath()+"\"";
    out.println(msg);
    if(sed.consoleOutput) {System.out.println(msg);}
    tPW.println(commentLineStart+"TABLE output from program SED         (I.Puigdomenech (1983) TRITA-OOK-3010)"+commentLineEnd);
    java.text.DateFormat dateFormatter =
            java.text.DateFormat.getDateTimeInstance
                (java.text.DateFormat.DEFAULT, java.text.DateFormat.DEFAULT, java.util.Locale.getDefault());
    java.util.Date today = new java.util.Date();
    String dateOut = dateFormatter.format(today);
    tPW.println(commentLineStart+"file written: "+dateOut+commentLineEnd);
    tPW.println();
    tPW.format(engl, "%sNbr chemical components = %d%s%n%s Concentrations:%s%n", commentLineStart, cs.Na, commentLineEnd, commentLineStart, commentLineEnd);
    // Concentration types:
    // hur =1 for "T" (fixed Total conc.)
    // hur =2 for "TV" (Tot. conc. Varied)
    // hur =3 for "LTV" (Log(Tot.conc.) Varied)
    // hur =4 for "LA" (fixed Log(Activity) value)
    // hur =5 for "LAV" (Log(Activity) Varied)
    for(int i=0; i < cs.Na; i++) {
        if(dgrC.hur[i] == 1) {
            tPW.format(engl,"%s  Tot. Conc. constant = %10.3g for %s%s%n", commentLineStart, dgrC.cLow[i],namn.identC[i],commentLineEnd);}
        if(dgrC.hur[i] == 2) {
            tPW.format(engl,"%s  Tot. Conc. varied between %10.3g and %10.3g for %s%s%n", commentLineStart, dgrC.cLow[i],dgrC.cHigh[i],namn.identC[i],commentLineEnd);}
        if(dgrC.hur[i] == 3) {
            tPW.format(engl,"%s  log10(Tot.Conc.) varied between %10.3g and %10.3g for %s%s%n", commentLineStart, dgrC.cLow[i],dgrC.cHigh[i],namn.identC[i],commentLineEnd);}
        if(dgrC.hur[i] == 4) {
            tPW.format(engl,"%s  log10(Activity) constant = %10.3g for %s%s%n", commentLineStart, dgrC.cLow[i],namn.identC[i],commentLineEnd);}
        if(dgrC.hur[i] == 5) {
            tPW.format(engl,"%s  log10(Activity) varied between %10.3g and %10.3g for %s%s%n", commentLineStart, dgrC.cLow[i],dgrC.cHigh[i],namn.identC[i],commentLineEnd);}
    }//for i
    String t;
    if(diag.ionicStrength < 0)  {t = "calculated at each point";}
    else {t = String.format(engl,"%6.2f",diag.ionicStrength);}
    if(sed.calcActCoeffs) {tPW.format(engl,"%sActivity coeffs. calculated at t =%5.1f degrees and I = %s M%s%n", commentLineStart, sed.temperature_InCommandLine,t,commentLineEnd);}
    t = "(program error)";
    if(diag.plotType ==1) {t="fractions";}
    else if(diag.plotType ==2) {t="solubilities";}
    else if(diag.plotType ==3) {t="log10(concs.)";}
    else if(diag.plotType ==4) {t="log10(ai/ar)";}
    else if(diag.plotType ==5) {t="pe (calc.)";}
    else if(diag.plotType ==6) {t="pH (calc.)";}
    else if(diag.plotType ==7) {t="log10(activities)";}
    else if(diag.plotType ==8) {t="d(H-bound)/d(pH)";}
    tPW.format("%sComponent in X-axis is: %s;  the Y-axis data are %s.%s%n", commentLineStart, namn.identC[diag.compX],t,commentLineEnd);
    tPW.flush();
    return true;
} //tableHeader()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="tableBody()">
void tableBody(Chem ch) {
    if(sed.dbg) {out.println("--- tableBody");}
    Chem.ChemSystem cs = ch.chemSystem;
    Chem.ChemSystem.NamesEtc namn = cs.namn;
    Chem.Diagr diag = ch.diag;
    Chem.DiagrConcs dgrC = ch.diagrConcs;

    /** field separator */
    String fs;
    if(sed.tblFieldSeparator != null && sed.tblFieldSeparator.length() >0)
        {fs = sed.tblFieldSeparator;}
    else {fs = " ";}
    tPW.format("%sOutput data (there are %d lines and (1+%d) columns)%s%n", commentLineStart, (sed.nSteps+1),Plot.nbrSpeciesInPlot,commentLineEnd);
    tPW.println(commentLineStart+"(the first column contains the X-axis values)"+commentLineEnd);
    //--- column captions:
    if(diag.plotType !=2) { //not solubilities
        if(diag.plotType ==8) {
            tPW.println("\""+namn.identC[diag.compX]+"\""+fs+"\"d(H-h)/d(pH)\""+fs);
        } else {
            tPW.print("\""+namn.identC[diag.compX]+"\""+fs);
            for(int i =0; i < Plot.nbrSpeciesInPlot; i++) {
                tPW.print("\""+namn.ident[Plot.speciesInPlot[i]]+"\""+fs);
            }
            tPW.println();
        }
    } else { // solubilities
        tPW.print("\""+namn.identC[diag.compX]+"\""+fs);
        for(int i =0; i < Plot.nbrSpeciesInPlot; i++) {
            tPW.print("\""+namn.identC[Plot.speciesInPlot[i]]+"\""+fs);
        }
        tPW.println();
    }
    //---
// Values for the Y-axis
//  plotType=1 fraction diagram      compY= main component
//  plotType=2 log solubility diagram
//  plotType=3 log (conc) diagram
//  plotType=4 log (ai/ar) diagram   compY= reference species
//  plotType=5 pe in Y-axis
//  plotType=6 pH in Y-axis
//  plotType=7 log (activity) diagram
//  plotType=8 H-affinity diagram
    double xValue;
    double w1; double w2; double y1; double y2;
    double y[] = new double[cs.Ms];
    for(int np =0; np < sed.nSteps+1; np++) {
        xValue = sed.bt[diag.compX][np];
        if(dgrC.hur[diag.compX] ==3) {xValue = Math.log10(xValue);} //"LTV"
        else if(diag.pInX !=0) {xValue = -xValue;}
        if(diag.plotType ==1) { //fractions
            for(int k=0; k < Plot.nbrSpeciesInPlot; k++) {
                int i = Plot.speciesInPlot[k];
                if(i >= cs.Ms) {continue;}
                if(Math.abs(Plot.tot0[diag.compY][np])>1.e-30) {
                    double o;
                    if(i < cs.Na) {if(i==diag.compY) {o=1;} else {o=0;}}
                    else {o =cs.a[i-cs.Na][diag.compY];}
                    y[i]=o*Plot.c0[i][np]/Plot.tot0[diag.compY][np];}
                else {y[i] = 0;}
                if(y[i] >1d && y[i] < 1.01d) {y[i] = 1;}

            } //for i
        } //plotType =1 (fractions)
        else if(diag.plotType ==2) { //solubilities
            for(int k=0; k < Plot.nbrSpeciesInPlot; k++) {
                int i = Plot.speciesInPlot[k];
                if(i >= cs.Ms) {continue;}
                if(Plot.tot0[i][np]>1.e-35) {y[i] = Math.log10(Plot.tot0[i][np]);}
                else {y[i] = -50;}
            } //for i
        } //plotType =2 (solubilities)
        else if(diag.plotType ==3) { //log(concs.)
            for(int k=0; k < Plot.nbrSpeciesInPlot; k++) {
                int i = Plot.speciesInPlot[k];
                if(i >= cs.Ms) {continue;}
                if(Plot.c0[i][np]>1.e-35) {y[i] = Math.log10(Plot.c0[i][np]);}
                else {y[i] = -99;}
            } //for i
        } //plotType =3 (log(concs.))
        else if(diag.plotType ==4) { //log(ai/ar)
            for(int k=0; k < Plot.nbrSpeciesInPlot; k++) {
                int i = Plot.speciesInPlot[k];
                if(i >= cs.Ms) {continue;}
                y[i] = Plot.c0[i][np] - Plot.c0[diag.compY][np];
            } //for i
        } //plotType =4 (log(ai/ar))
        else if(diag.plotType ==5 || diag.plotType ==6) { //pe or pH
            for(int k=0; k < Plot.nbrSpeciesInPlot; k++) {
                int i = Plot.speciesInPlot[k];
                if(i >= cs.Ms) {continue;}
                y[i] = -Plot.c0[i][np];
            } //for i
        } //plotType =5|6 (pe or pH)
        else if(diag.plotType ==7) { //log(act.)
            for(int k=0; k < Plot.nbrSpeciesInPlot; k++) {
                int i = Plot.speciesInPlot[k];
                if(i >= cs.Ms) {continue;}
                y[i] = Plot.c0[i][np];
            } //for i
        } //plotType =7 (log(act.))
        else if(diag.plotType ==8) { //Proton affinity
            //Note:  c0[0][nSp] =c[Hplus]   c0[1][nSp] =logA[Hplus]   c0[2][nSp] =c[OHmin]
            if(np ==0) {
                w1 = Plot.c0[1][np+1]-Plot.c0[1][np];
                w2 = (Plot.tot0[diag.Hplus][np+1]-Plot.c0[0][np+1]+Plot.c0[2][np+1])
                     - (Plot.tot0[diag.Hplus][np]-Plot.c0[0][np]+Plot.c0[2][np]);
                y[0] =0;
                if(Math.abs(w1) >= 1.E-35) y[0]=( w2 / w1);
            } //first point
            else if(np < sed.nSteps) {
                w1 = Plot.c0[1][np]-Plot.c0[1][np-1];
                w2 = (Plot.tot0[diag.Hplus][np]-Plot.c0[0][np]+Plot.c0[2][np])
                        - (Plot.tot0[diag.Hplus][np-1]-Plot.c0[0][np-1]+Plot.c0[2][np-1]);
                y1 = 0;
                if(Math.abs(w1) >= 1.E-35) y1= ( w2 / w1);
                w1 = Plot.c0[1][np]-Plot.c0[1][np+1];
                w2 = (Plot.tot0[diag.Hplus][np]-Plot.c0[0][np]+Plot.c0[2][np])
                        - (Plot.tot0[diag.Hplus][np+1]-Plot.c0[0][np+1]+Plot.c0[2][np+1]);
                y2 = 0;
                if(Math.abs(w1) >= 1.E-35) y2= ( w2 / w1);
                y[0] = ((y1+y2)/0.5d);
            } else {
                w1 = Plot.c0[1][np]-Plot.c0[1][np-1];
                w2 = (Plot.tot0[diag.Hplus][np]-Plot.c0[0][np]+Plot.c0[2][np])
                        - (Plot.tot0[diag.Hplus][np-1]-Plot.c0[0][np-1]+Plot.c0[2][np-1]);
                if(Math.abs(w1) >= 1.E-35) y[0] = ( w2 / w1);
            } //last point
        } //plotType =8 (Proton affinity)
        else { //error
            for(int k=0; k < y.length; k++) {
                y[k] = -9999.9999;}}
        tPW.format(engl,"%13.5g",xValue);
        for(int k=0; k < Plot.nbrSpeciesInPlot; k++) {
                int i = Plot.speciesInPlot[k];
                if(i >= cs.Ms) {continue;}
                tPW.format(engl,fs+"%13.5g",y[i]);
            } //for i
        tPW.println();
    } //for np
    tableClose();
    out.println("Table output written to file:"+nl+"   \""+tf.getName()+"\"");
} //tableBody()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="tableClose()">
private void tableClose() {
    if(sed.dbg) {out.println("--- tableClose");}
    if(tPW != null) {tPW.flush(); tPW.close();}
} //tableClose()
//</editor-fold>

} // class Table
