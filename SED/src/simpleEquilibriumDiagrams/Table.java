package simpleEquilibriumDiagrams;

import lib.kemi.chem.Chem;

/**  Methods to save numerical results into output text file(s).
 * <br>
 * Copyright (C) 2014-2020 I.Puigdomenech.
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
private java.io.Writer tW = null;
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
/** field separator (for example a semicolon, ";", a comma ",", etc). */
private String fs;

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
/** Prepare a new output text file to receive the table data.
 * @param ch where data on the chemical system are stored
 * @param tblExtension
 * @return false if an error occurs */
boolean tableHeader(Chem ch, java.io.File tableFile) {
    if(sed.dbg) {out.println("--- tableHeader");}
    this.tf = tableFile;
    String msg;
    try {
        tW = new java.io.BufferedWriter(
                new java.io.OutputStreamWriter(
                        new java.io.FileOutputStream(tf),"UTF8"));
    } catch (java.io.IOException ex) {
        msg = "Error: \""+ex.getMessage()+"\""+nl+
                            "while opening file \""+tf.getAbsolutePath()+"\"";
        sed.showErrMsgBx(msg, 1);
        tableClose();
        return false;
    }

    msg = "Writing table file:"+nl+"    \""+tf.getAbsolutePath()+"\"";
    out.println(msg);
    if(sed.consoleOutput) {System.out.println(msg);}

    try{
    tW.write(commentLineStart+"TABLE output from program SED  (I.Puigdomenech http://github.com/ignasi-p/eq-diagr/releases)"+commentLineEnd+nl);
    java.text.DateFormat dateFormatter =
            java.text.DateFormat.getDateTimeInstance
                (java.text.DateFormat.DEFAULT, java.text.DateFormat.DEFAULT, java.util.Locale.getDefault());
    java.util.Date today = new java.util.Date();
    String dateOut = dateFormatter.format(today);
    tW.write(commentLineStart+"file written: "+dateOut+commentLineEnd+nl);
    tW.write(nl);

    Chem.ChemSystem cs = ch.chemSystem;
    Chem.ChemSystem.NamesEtc namn = cs.namn;
    Chem.Diagr diag = ch.diag;
    Chem.DiagrConcs dgrC = ch.diagrConcs;

    tW.write(String.format(engl, "%sNbr chemical components = %d%s%n%s Concentrations:%s%n", commentLineStart, cs.Na, commentLineEnd, commentLineStart, commentLineEnd));
    // Concentration types:
    // hur =1 for "T" (fixed Total conc.)
    // hur =2 for "TV" (Tot. conc. Varied)
    // hur =3 for "LTV" (Log(Tot.conc.) Varied)
    // hur =4 for "LA" (fixed Log(Activity) value)
    // hur =5 for "LAV" (Log(Activity) Varied)
    for(int i=0; i < cs.Na; i++) {
        if(dgrC.hur[i] == 1) {
            tW.write(String.format(engl,"%s  Tot. Conc. constant = %10.3g for %s%s%n", commentLineStart, dgrC.cLow[i],namn.identC[i],commentLineEnd));}
        if(dgrC.hur[i] == 2) {
            tW.write(String.format(engl,"%s  Tot. Conc. varied between %10.3g and %10.3g for %s%s%n", commentLineStart, dgrC.cLow[i],dgrC.cHigh[i],namn.identC[i],commentLineEnd));}
        if(dgrC.hur[i] == 3) {
            tW.write(String.format(engl,"%s  log10(Tot.Conc.) varied between %10.3g and %10.3g for %s%s%n", commentLineStart, dgrC.cLow[i],dgrC.cHigh[i],namn.identC[i],commentLineEnd));}
        if(dgrC.hur[i] == 4) {
            tW.write(String.format(engl,"%s  log10(Activity) constant = %10.3g for %s%s%n", commentLineStart, dgrC.cLow[i],namn.identC[i],commentLineEnd));}
        if(dgrC.hur[i] == 5) {
            tW.write(String.format(engl,"%s  log10(Activity) varied between %10.3g and %10.3g for %s%s%n", commentLineStart, dgrC.cLow[i],dgrC.cHigh[i],namn.identC[i],commentLineEnd));}
    }//for i
    String ionStr;
    if(diag.ionicStrength < 0)  {ionStr = "calculated at each point";}
    else {ionStr = String.format(engl,"%6.2f molal",diag.ionicStrength).trim();}
    String t = String.format(engl,"t=%3d C",Math.round((float)diag.temperature));
    if(!Double.isNaN(diag.pressure) && diag.pressure > 1.02) {
        if(diag.pressure < 220.64) {
            t = t + String.format(java.util.Locale.ENGLISH,", p=%.2f bar",diag.pressure);
        } else {
            if(diag.pressure <= 500) {t = t + String.format(", p=%.0f bar",diag.pressure);}
            else {t = t + String.format(java.util.Locale.ENGLISH,", p=%.1f kbar",(diag.pressure/1000.));}
        }
    }
    if(sed.calcActCoeffs && !Double.isNaN(diag.ionicStrength) && Math.abs(diag.ionicStrength) > 1.e-10) {
        tW.write(nl);
        tW.write(commentLineStart+"Activity coefficients calculated at "+t+" and I = "+ionStr+commentLineEnd+nl);
        String At = At = String.format(engl, "%9.5f", sed.factor.Agamma).trim();
        String Bt = String.format(engl, "%6.3f", sed.factor.rB).trim();        
        if(diag.activityCoeffsModel == 0) { //Davies
            tW.write(commentLineStart+"using Davies eqn.:"+commentLineEnd+nl);
            tW.write(commentLineStart+"  log f(i) = -"+At+" Zi^2 ( I^0.5 / (1 + I^0.5) - 0.3 I)"+commentLineEnd+nl);
        }
        else if(diag.activityCoeffsModel == 1) { //SIT
            tW.write(commentLineStart+"using the SIT model:"+commentLineEnd+nl);
            tW.write(commentLineStart+"  log f(i) = -("+At+" Zi^2 I^0.5 / (1 + "+Bt+" I^0.5))"+
                " + Sum[ eps(i,j) m(j) ]"+commentLineEnd+nl);
            tW.write(commentLineStart+"for all 'j' with Zj*Zi<=0;  where eps(i,j) is a specific ion interaction parameter (in general independent of I)"+commentLineEnd+nl);
        }
        else if(diag.activityCoeffsModel == 2) { //HKF
            tW.write(commentLineStart+"using simplified Helgeson, Kirkham & Flowers model:"+commentLineEnd+nl);
            String bgit = String.format(engl, "%7.4f", sed.factor.bgi).trim();
            tW.write(commentLineStart+"  log f(i) = -("+At+" Zi^2 I^0.5) / (1 + "+Bt+" I^0.5) + Gamma + ("+bgit+" I)"+commentLineEnd+nl);
            tW.write(commentLineStart+"where: Gamma = -log(1+0.0180153 I)"+commentLineEnd+nl);
        }
    }
    tW.write(nl);
    t = "(program error)";
    if(diag.plotType ==1) {t="fractions";}
    else if(diag.plotType ==2) {t="solubilities";}
    else if(diag.plotType ==3) {t="log10(concs.)";}
    else if(diag.plotType ==4) {t="log10(ai/ar)";}
    else if(diag.plotType ==5) {t="pe (calc.)";}
    else if(diag.plotType ==6) {t="pH (calc.)";}
    else if(diag.plotType ==7) {t="log10(activities)";}
    else if(diag.plotType ==8) {t="d(H-bound)/d(pH)";}
    tW.write(String.format("%sComponent in X-axis is: %s;  the Y-axis data are %s.%s%n", commentLineStart, namn.identC[diag.compX],t,commentLineEnd));
    tW.flush();
    tW.write(String.format("%sOutput data (there are %d lines and (1+%d) columns)%s%n", commentLineStart, (sed.nSteps+1),Plot.nbrSpeciesInPlot,commentLineEnd));
    tW.write(commentLineStart+"(the first column contains the X-axis values)"+commentLineEnd+nl);
    //--- column captions:
    if(sed.tblFieldSeparator != null && sed.tblFieldSeparator.length() >0) {
        fs = sed.tblFieldSeparator;
    } else {fs = " ";}
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
    if(diag.plotType !=2) { //not solubilities
        if(diag.plotType ==8) {
            tW.write("\""+namn.identC[diag.compX]+"\""+fs+"\"d(H-h)/d(pH)\""+fs);
        } else {
            tW.write("\""+namn.identC[diag.compX]+"\""+fs);
            for(int i =0; i < Plot.nbrSpeciesInPlot; i++) {
                tW.write("\""+namn.ident[Plot.speciesInPlot[i]]+"\""+fs);
            }
        }
    } else { // solubilities
        tW.write("\""+namn.identC[diag.compX]+"\""+fs);
        for(int i =0; i < Plot.nbrSpeciesInPlot; i++) {
            tW.write("\""+namn.identC[Plot.speciesInPlot[i]]+"\""+fs);
        }
    }

    // print ionic strength, concentrations and activity coefficients
    if(!Double.isNaN(diag.ionicStrength) &&
       Math.abs(diag.ionicStrength) > 1.e-10 && (diag.plotType !=4 && diag.plotType !=8)) {
        tW.write(fs);
        tW.write(String.format(engl,"I (molal)%sSum of m%s",fs,fs));
        int nIon = cs.Na + cs.nx;
        for(int j = 0; j < nIon; j++) {
            if(j != cs.jWater) {
                tW.write(String.format(engl,"C(%s)%slogf()%s",namn.ident[j],fs,fs));
            } else {
                tW.write(String.format(engl,"a(H2O)%sphi%s",fs,fs));
            }
        }
    }

    tW.write(nl);

    } catch (Exception ex) {
        msg = "Error: \""+ex.getMessage()+"\""+nl+
                            "while writing file \""+tf.getAbsolutePath()+"\"";
        sed.showErrMsgBx(msg, 1);
        tableClose();
        return false;
    }

    return true;
} //tableHeader()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="tableBody()">
/** Print diagram data for this point (the equilibrium composition).
 * @param nP the point number (along the x-axis)
 * @param ch where the data for the chemical system are stored */
void tableBody(int nP, Chem ch) {
    if(sed.dbg) {out.println("--- tableBody("+nP+", ch)");}
    Chem.ChemSystem cs = ch.chemSystem;
    Chem.ChemSystem.ChemConcs csC = cs.chemConcs;
    Chem.ChemSystem.NamesEtc namn = cs.namn;
    Chem.Diagr diag = ch.diag;
    Chem.DiagrConcs dgrC = ch.diagrConcs;

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
    int i;
    xValue = sed.bt[diag.compX][nP];
    if(dgrC.hur[diag.compX] ==3) {  //"LTV"
        xValue = Math.log10(xValue);
    } else if(diag.pInX !=0) {
        xValue = -xValue;
    }
    if(diag.plotType ==1) { //fractions
        for(int k=0; k < Plot.nbrSpeciesInPlot; k++) {
            i = Plot.speciesInPlot[k];
            if(i >= cs.Ms) {continue;}
            if(Math.abs(Plot.tot0[diag.compY][nP])>1.e-30) {
                double o;
                if(i < cs.Na) {
                    if(i==diag.compY) {o=1;} else {o=0;}
                } else {o =cs.a[i-cs.Na][diag.compY];}
                y[i]=o*Plot.c0[i][nP]/Plot.tot0[diag.compY][nP];
            } else {
                y[i] = 0;
            }
            if(y[i] >1d && y[i] < 1.01d) {y[i] = 1;}
        } //for i
    } else if(diag.plotType ==2) { //solubilities
        for(int k=0; k < Plot.nbrSpeciesInPlot; k++) {
            i = Plot.speciesInPlot[k];
            if(i >= cs.Ms) {continue;}
            if(Plot.tot0[i][nP]>1.e-50) {y[i] = Math.log10(Plot.tot0[i][nP]);}
            else {y[i] = -50;}
        }
    } else if(diag.plotType ==3) { //log(concs.)
        for(int k=0; k < Plot.nbrSpeciesInPlot; k++) {
            i = Plot.speciesInPlot[k];
            if(i >= cs.Ms) {continue;}
            if(Plot.c0[i][nP]>1.e-35) {y[i] = Math.log10(Plot.c0[i][nP]);}
            else {y[i] = -99;}
        }
    } else if(diag.plotType ==4) { //log(ai/ar)
        for(int k=0; k < Plot.nbrSpeciesInPlot; k++) {
            i = Plot.speciesInPlot[k];
            if(i >= cs.Ms) {continue;}
            y[i] = Plot.c0[i][nP] - Plot.c0[diag.compY][nP];
        }
    } else if(diag.plotType ==5 || diag.plotType ==6) { //pe or pH
        for(int k=0; k < Plot.nbrSpeciesInPlot; k++) {
            i = Plot.speciesInPlot[k];
            if(i >= cs.Ms) {continue;}
            y[i] = -Plot.c0[i][nP];
        }
    } else if(diag.plotType ==7) { //log(act.)
        for(int k=0; k < Plot.nbrSpeciesInPlot; k++) {
            i = Plot.speciesInPlot[k];
            if(i >= cs.Ms) {continue;}
            y[i] = Plot.c0[i][nP];
        }
    } else if(diag.plotType ==8) { //Proton affinity
        //Note:  c0[0][nSp] =c[Hplus]   c0[1][nSp] =logA[Hplus]   c0[2][nSp] =c[OHmin]
        if(nP ==0) { //first point
            y[0] =0;
        } else if(nP <= sed.nSteps) {
            // w1 = delta (-pH)
            // w2 = delta (H_bound) = delta ([H]_tot - [H+] + [OH-]
            w1 = Plot.c0[1][nP]-Plot.c0[1][nP-1];
            w2 = (Plot.tot0[diag.Hplus][nP]-Plot.c0[0][nP]+Plot.c0[2][nP])
                        - (Plot.tot0[diag.Hplus][nP-1]-Plot.c0[0][nP-1]+Plot.c0[2][nP-1]);
            if(Math.abs(w1) >= 1.E-35) y[0] = ( w2 / w1);
        }
    } else { //error
        for(int k=0; k < y.length; k++) {y[k] = -9999.9999;}
    }

    try{
    tW.write(String.format(engl,"%13.5g",xValue));
        for(int k=0; k < Plot.nbrSpeciesInPlot; k++) {
            i = Plot.speciesInPlot[k];
            if(i >= cs.Ms) {continue;}
            tW.write(String.format(engl,fs+"%13.5g",y[i]));
        }

    tW.write(fs);

    // print ionic strength, concentrations and activity coefficients
    if(!Double.isNaN(diag.ionicStrength) &&
       Math.abs(diag.ionicStrength) > 1.e-10 && (diag.plotType !=4 && diag.plotType !=8)) {
        tW.write(String.format(engl,"%s% 9.4f%s %9.4f", fs,diag.ionicStrCalc,fs,diag.sumM));
        int nIon = cs.Na + cs.nx;
        for(int j = 0; j < nIon; j++) {
            if(j != cs.jWater) {
                tW.write(String.format(engl,"%s% 12.4g%s %9.4f", fs,cs.chemConcs.C[j],fs,cs.chemConcs.logf[j]));
            } else {
                tW.write(String.format(engl,"%s% 9.4f%s %9.4f", fs,Math.pow(10,cs.chemConcs.logA[j]),fs,diag.phi));
            }
        }
    }

    tW.write(nl);

    } catch (Exception ex) {
        sed.showErrMsgBx("Error: \""+ex.getMessage()+"\""+nl+
                            "while writing file \""+tf.getAbsolutePath()+"\"", 1);
        tableClose();
    }

} //tableBody()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="tableClose()">
void tableClose() {
    if(sed.dbg) {out.println("--- tableClose");}
    out.println("Table output written to file:"+nl+"   \""+tf.getName()+"\"");
    try{if(tW != null) {tW.flush(); tW.close();}}
    catch (Exception ex) {
        sed.showErrMsgBx("Error: \""+ex.getMessage()+"\""+nl+
                            "while closing file \""+tf.getAbsolutePath()+"\"", 1);
    }
}
//</editor-fold>

} // class Table
