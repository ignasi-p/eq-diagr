package simpleEquilibriumDiagrams;

import lib.common.Util;
import lib.kemi.chem.Chem;
import lib.kemi.graph_lib.GraphLib;


/** Methods to create a chemical equilibrium diagram.
 * <br>
 * Copyright (C) 2014-2016 I.Puigdomenech.
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
public class Plot {
    private SED sed = null;
    // this and other fields are also used in the Table class
    static int nbrSpeciesInPlot;
    static int[] speciesInPlot;
    /** max. number of species that need two labels in the diagram */
    private static final int L2 = 20;
    /** the max y-value for each curve in the diagram */
    private static double[] yMax;
    /** values of conc. or log(activity) for each point c0[Ms][nP] */
    static double c0[][];
    /** values of solubility or tot. conc. for each point tot0[Na][nP] */
    static double tot0[][];
    private static boolean xMolar = true;
    private static final java.util.Locale engl = java.util.Locale.ENGLISH;
/** Where errors will be printed. It may be <code>System.err</code>.
 * If null, <code>System.err</code> is used. */
    private final java.io.PrintStream err;
/** Where messages will be printed. It may be <code>System.out</code>.
 * If null, <code>System.out</code> is used. */
    private final java.io.PrintStream out;
    private static final String nl = System.getProperty("line.separator");

/** Constructor.
 * @param sed0
 * @param err0 Where errors will be printed. It may be <code>System.err</code>.
 * If null, <code>System.err</code> is used.
 * @param out0 Where messages will be printed. It may be <code>System.out</code>.
 * If null, <code>System.out</code> is used.
 */
public Plot(SED sed0, java.io.PrintStream err0, java.io.PrintStream out0) {
    this.sed = sed0;
    if(err0 != null) {this.err = err0;} else {this.err = System.err;}
    if(out0 != null) {this.out = out0;} else {this.out = System.out;}
} //constructor

//<editor-fold defaultstate="collapsed" desc="preparePlot(ch)">
/** Create arrays to store the diagram data
 * @param ch where the data for the chemical system are stored */
void preparePlot(Chem ch) {
    if(sed.dbg) {out.println("--- preparePlot(ch)");}
    Chem.ChemSystem cs = ch.chemSystem;
    Chem.ChemSystem.ChemConcs csC = cs.chemConcs;
    Chem.ChemSystem.NamesEtc namn = cs.namn;
    Chem.Diagr diag = ch.diag;
    //chem.Chem.DiagrConcs dgrC = ch.diagrConcs;

    yMax = new double[cs.Ms+L2];
    for(int i=0; i < yMax.length; i++) {yMax[i] =0;}
    c0 = new double[cs.Ms][sed.nSteps+1];
    tot0 = new double[cs.Na][sed.nSteps+1];

} //preparePlot
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="storePlotData(nP, ch)">
/** Store diagram data for this point (an equilibrium composition)
 * in the arrays provided in this class.
 * @param nP the point number (along the x-axis)
 * @param ch where the data for the chemical system are stored */
void storePlotData(int nP, Chem ch) {
    if(sed.dbg) {out.println("--- storePlotData("+nP+", ch)");}
    Chem.ChemSystem cs = ch.chemSystem;
    Chem.ChemSystem.ChemConcs csC = cs.chemConcs;
    Chem.ChemSystem.NamesEtc namn = cs.namn;
    Chem.Diagr diag = ch.diag;
    //chem.Chem.DiagrConcs dgrC = ch.diagrConcs;
// Values for the Y-axis
//  plotType=1 fraction diagram      compY= main component
//  plotType=2 log solubility diagram
//  plotType=3 log (conc) diagram
//  plotType=4 log (ai/ar) diagram   compY= reference species
//  plotType=5 pe in Y-axis
//  plotType=6 pH in Y-axis
//  plotType=7 log (activity) diagram
//  plotType=8 H-affinity diagram
    double w, y, z;
    // save conc. or log(activity)
    for(int i =0; i < cs.Ms; i++) {
        c0[i][nP] = 0;
        if(diag.plotType ==4 || diag.plotType ==7) {c0[i][nP] = -9999;} // log(a) or log(ai/ar)
        if(diag.plotType ==5 || diag.plotType ==6) {c0[i][nP] = +9999;} // pe/Eh or pH
        if(csC.isErrFlagsSet(2) || csC.isErrFlagsSet(3) || csC.isErrFlagsSet(4)
                            || csC.isErrFlagsSet(6)) {continue;}
        if(diag.plotType < 4) { // fraction, log(c), or log(solub)
            if(csC.C[i] > 1.e+35) {c0[i][nP] = 1.e+35;}
            else if(Math.abs(csC.C[i]) <= 1.e+35)
                                    {c0[i][nP] = csC.C[i];}
        } else {
            if(diag.plotType ==8) {continue;} // H-affinity
            //for log(ai/ar), log(activity) or pe or pH diagrams: set highest limit
            if(csC.logA[i] > 1.e+35) {c0[i][nP] = 1.e+35;}
            w = Math.abs(csC.logA[i]);
            //set lowest limit, and if loga =0 check if concentration =0
            if(w <= 1e+35 && (w > 0 || Math.abs(csC.C[i]) > 0))
                                    {c0[i][nP] = csC.logA[i];}
            if(cs.noll[i] && (w > 0 && w <= 1e+35))
                                    {c0[i][nP] = csC.logA[i];}
        }
    } //for i
    // save tot.conc. or solubility
    for(int j =0; j < cs.Na; j++) {
        tot0[j][nP] = 0.d;
        if(csC.errFlags < -1) {continue;}
        if(diag.plotType ==2) { //log(solub)
            if(Math.abs(csC.solub[j]) < 1.e+35d)
                                    {tot0[j][nP] = csC.solub[j];}
        } else {
            if(Math.abs(csC.tot[j]) < 1.e+35d)
                                    {tot0[j][nP] = csC.tot[j];}
        }
    } //for j
    // for H-affinity diagrams:
    if(diag.plotType ==8 && (diag.Hplus >=0 && diag.Hplus < cs.Ms)) {
         //c0[0][nP]=C(H+)    c0[1][nP]=LOGA(H+)    c0[2][nP]=C(OH-)
        if(Math.abs(csC.C[diag.Hplus]) <= 1e+35)
                                    {c0[0][nP] = csC.C[diag.Hplus];}
        w = Math.abs(csC.logA[diag.Hplus]);
        if(w <= 1e+35 &&
                (w >= 1e-35 || Math.abs(csC.C[diag.Hplus]) >= 1e-35))
                                    {c0[1][nP] = csC.logA[diag.Hplus];}
        if(diag.OHmin > -1) {if(Math.abs(csC.C[diag.OHmin]) <= 1e+35)
                                    {c0[2][nP] = csC.C[diag.OHmin];}}
        //get Max and Min values for Y-axis
        if(nP > 0) {
            w = c0[1][nP] - c0[1][nP-1];
            y = (tot0[diag.Hplus][nP]-c0[0][nP]+c0[2][nP])
                    - (tot0[diag.Hplus][nP-1]-c0[0][nP-1]+c0[2][nP-1]);
            z = 0;
            if(Math.abs(w) >= 1e-30) {z = y/w;}
            if(z < diag.yLow) {diag.yLow = z;}
            if(z > diag.yHigh) {diag.yHigh = z;}
        } // if nP >0
    } // H-affinity
} //storePlotData()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="drawPlot(plotFile, ch)">
/** Create a diagram. Saving the diagram information in a PltData() object
 * and simultaneously store the data in a plot file.
 * @param plotFile where the diagram will be saved
 * @param ch where the data for the chemical system are stored
 */
void drawPlot(java.io.File plotFile, Chem ch) {
Chem.ChemSystem cs = ch.chemSystem;
Chem.ChemSystem.ChemConcs csC = cs.chemConcs;
Chem.ChemSystem.NamesEtc namn = cs.namn;
Chem.Diagr diag = ch.diag;
Chem.DiagrConcs dgrC = ch.diagrConcs;

if(sed.dbg) {
    out.println("--- drawPlot("+plotFile.toString()+", ch)"+System.getProperty("line.separator")+
            "Drawing the plot...");
}

// Values for the Y-axis
//  plotType=1 fraction diagram      compY= main component
//  plotType=2 log solubility diagram
//  plotType=3 log (conc) diagram
//  plotType=4 log (ai/ar) diagram   compY= reference species
//  plotType=5 pe in Y-axis
//  plotType=6 pH in Y-axis
//  plotType=7 log (activity) diagram
//  plotType=8 H-affinity diagram
//---- what species to diagramData ----
    nbrSpeciesInPlot = cs.Ms;
    speciesInPlot = new int[cs.Ms+L2];
    if(diag.plotType == 2) { // solubility diagram
        nbrSpeciesInPlot = 0;
        for(int n =0; n < cs.Na; n++) {
            String t = namn.identC[n];
            if(Util.isElectron(t) ||
               Util.isProton(t) ||
               Util.isWater(t)) {continue;}
            nbrSpeciesInPlot++;
            speciesInPlot[nbrSpeciesInPlot-1] = n;
        } //for n
    } else if(diag.plotType ==5 || diag.plotType ==6) { //pe or pH in the Y-AXIS
        for(int n =0; n < cs.Ms; n++) {
            String t = namn.ident[n];
            if(diag.plotType ==5) {//pe
                if(Util.isElectron(t)) {speciesInPlot[0] = n; break;}
            } else { //pH
                if(Util.isProton(t)) {speciesInPlot[0] = n; break;}
            }
        } //for n
        nbrSpeciesInPlot = 1;
    } else if(diag.plotType ==8) { //H-affinity diagram
        nbrSpeciesInPlot = 1;
        speciesInPlot[0] = diag.Hplus;
    } else { //Log(c), Log(a), Fraction and log(ai/ar)
        nbrSpeciesInPlot = 0;
        for(int n =0; n < cs.Ms; n++) {
            String t = namn.ident[n].toUpperCase();
            //if not log(a): exclude electrons
            if(diag.plotType !=7 && Util.isElectron(t)) {continue;}
            if(diag.plotType ==4) { //log (ai/ar)
                if(n == diag.compY) {
                    nbrSpeciesInPlot++;
                    speciesInPlot[nbrSpeciesInPlot-1] = n;
                    continue;
                }
                if(diag.compY >= cs.Na) {
                    //relative diagram where the reference species is not a component
                    if(n < cs.Na && Math.abs(cs.a[diag.compY][n]) <= 1e-10) {continue;}
                    nbrSpeciesInPlot++;
                    speciesInPlot[nbrSpeciesInPlot-1] = n;
                    continue;
                }
                if(n < cs.Na) {continue;}
                //include only species related to the reference species
                if(Math.abs(cs.a[n-cs.Na][diag.compY]) > 1e-10) {
                    nbrSpeciesInPlot++;
                    speciesInPlot[nbrSpeciesInPlot-1] = n;
                }
            } else {
                nbrSpeciesInPlot++;
                speciesInPlot[nbrSpeciesInPlot-1] = n;
            }
        } //for n
    } //Log(c), Log(a), Fraction and log(ai/ar)
//---- Max and Min values in the axes: xLow,xHigh, yLow,yHigh
    double xLow = sed.bt[diag.compX][0];
    double xHigh = sed.bt[diag.compX][(sed.bt[0].length-1)];
    // Concentration types:
    // hur =1 for "T" (fixed Total conc.)
    // hur =2 for "TV" (Tot. conc. Varied)
    // hur =3 for "LTV" (Log(Tot.conc.) Varied)
    // hur =4 for "LA" (fixed Log(Activity) value)
    // hur =5 for "LAV" (Log(Activity) Varied)
    if(dgrC.hur[diag.compX] ==3) { // LTV
        xLow = Math.log10(xLow);
        xHigh = Math.log10(xHigh);
    }
    if(Util.isProton(namn.identC[diag.compX]) ||
       Util.isElectron(namn.identC[diag.compX])) {
        if(dgrC.hur[diag.compX] !=5) {diag.pInX = 0;}  // not LAV
        else { // LAV
            xLow = -xLow;  xHigh = -xHigh;
            if(diag.pInX == 3) {
                xLow  = sed.peEh * xLow;
                xHigh = sed.peEh * xHigh;
            }
        } // if LAV
    } // is H+ or engl-
    // Molar scale in X-axis
    xMolar = true;
    if(dgrC.hur[diag.compX] <=2) { // T or TV
        if(Math.abs(xLow) <0.9d && Math.abs(xHigh) <0.9d) {
            //milimolar units in X-axis
            xMolar = false;
            xLow = xLow * 1000.d; xHigh = xHigh * 1000.d;
        }
    } //T or TV
    //Values for the Y-axis
    /** the low limit for the y-axis */
    double yLow = diag.yLow;
    /** the high limit for the y-axis */
    double yHigh = diag.yHigh;
    //  plotType=1 fraction diagram      compY= main component
    //  plotType=2 log solubility diagram
    //  plotType=3 log (conc) diagram
    //  plotType=4 log (ai/ar) diagram   compY= reference species
    //  plotType=5 pe in Y-axis
    //  plotType=6 pH in Y-axis
    //  plotType=7 log (activity) diagram
    //  plotType=8 H-affinity diagram
    if(diag.plotType <= 3 && !diag.inputYMinMax) { // fraction, log(c) or log(solub)
        if(diag.plotType ==2) {yLow =-16;} //log(solub)
        else if(diag.plotType ==3) {yLow =-9;} // log(c)
        else {yLow =0;} //fraction

        if(diag.plotType ==2) {yHigh =+2;} //log(solub)
        else {yHigh =1;} //fraction or log(conc.)
    }
    if(diag.plotType == 8) {yLow = Math.min(yLow, 0);} // for H-affinity diagram

    /** For a curve to show in the graph: at least 0.5% of the Y-axis span
     *  (3% in fraction diagrams (0.03 fraction)) */
    double yMin = yLow + 0.005 * (yHigh - yLow);
    if(diag.plotType == 1) {yMin = yLow + (double)diag.fractionThreshold * (yHigh - yLow);}  //fraction namn

//---- Get the length of the axes labels
    int nTextX = 6 + namn.nameLength[namn.iel[diag.compX]];
    if(diag.pInX ==1 || diag.pInX ==2) {nTextX =2;} // pH or pe
    else if(diag.pInX ==3) {nTextX =8;} // "E`SHE' / V"
    int nTextY = 8;
    if(diag.plotType ==3) {nTextY =9;} //log(conc.)
    else if(diag.plotType ==2) {nTextY =10;} //log(solub)
    else if(diag.plotType ==4) {nTextY =12;} //log(ai/aref)
    else if(diag.plotType ==5 || diag.plotType ==6) {nTextY = 2;} //pH or pe

//---- Dimensions of the diagramData,  Size of text: height.  Origo: xOr,yOr
    float xAxl =15; float yAxl = 10;
    float heightAx = 0.035f * yAxl;
    if(sed.tHeight > 0.0001) {heightAx = (float)sed.tHeight*heightAx;}
    float xOr; float yOr;
    xOr = 7.5f * heightAx;
    yOr = 4.0f * heightAx;
    float xMx = xOr + xAxl;  float yMx = yOr + yAxl;
    //  xL and yL are scale factors, according to
    //  the axis-variables (pH, pe, Eh, log{}, log[]tot, etc)
    float xL = xAxl / (float)(xHigh - xLow);  float xI = xL * (float)xLow - xOr;
    float yL = yAxl / (float)(yHigh - yLow);  float yI = yL * (float)yHigh - yMx;
    if(!xMolar) {xL = xL * 1000;}  // T or TV and "mM"
    //  pInX=0 "normal" X-axis
    //  pInX=1 pH in X-axis
    //  pInX=2 pe in X-axis
    //  pInX=3 Eh in X-axis
    if(diag.pInX ==1 || diag.pInX == 2) {xL = -xL;}
    else if(diag.pInX ==3) {xL = -xL * (float)sed.peEh;}

    // -------------------------------------------------------------------
    //          Create a PltData instance
    sed.dd = new GraphLib.PltData();
    //          Create a GraphLib instance
    GraphLib g = new GraphLib();
    boolean textWithFonts = true;
    try {g.start(sed.dd, plotFile, textWithFonts);}
    catch (GraphLib.OpenPlotFileException ex) {sed.showErrMsgBx(ex.getMessage(),1); g.end(); return;}
    sed.dd.axisInfo = false;
    g.setLabel("-- SED DIAGRAM --");
    // -------------------------------------------------------------------
    //                  Draw Axes
    g.setIsFormula(true);
    g.setPen(1);
    //g.setLabel("-- AXIS --");
    g.setPen(-1);
    // draw axes
    try {g.axes((float)xLow, (float)xHigh, (float)yLow, (float)yHigh,
            xOr,yOr, xAxl,yAxl, heightAx,
            false, false, false);}
    catch (GraphLib.AxesDataException ex) {sed.showMsg(ex); g.end(); return;}
    //---- Write text under axes
    // Y-axis
    float xP; float yP;
    yP =((yAxl/2f)+yOr)-((((float)nTextY)/2f)*(1.3f*heightAx));
    //xP = 1.1f*heightAx;
    xP = xOr - 6.6f*heightAx;
    // Values for the Y-axis
    //  plotType=1 fraction diagram      compY= main component
    //  plotType=2 log solubility diagram
    //  plotType=3 log (conc) diagram
    //  plotType=4 log (ai/ar) diagram   compY= reference species
    //  plotType=5 pe in Y-axis
    //  plotType=6 pH in Y-axis
    //  plotType=7 log (activity) diagram
    //  plotType=8 H-affinity diagram
    String t = "Fraction";
    if(diag.plotType ==2) {t="Log Solubl.";}
    else if(diag.plotType ==3) {t="Log Conc.";}
    else if(diag.plotType ==4) {t="Log (a`i'/a`ref')";}
    else if(diag.plotType ==5) {
        t="pe";
        if(diag.Eh) {t="E`H'";}
    }
    else if(diag.plotType ==6) {t="pH";}
    else if(diag.plotType ==7) {t="Log Activity";}
    else if(diag.plotType ==8) {t="d([H+]`bound')/d(-pH)";} //H-affinity (proton-affinity)
    g.setLabel("Y-AXIS TEXT");  g.moveToDrawTo(0, 0, 0);
    g.sym(xP, yP, heightAx, t, 90, 0, false);
    // ---- X-axis label (title)
    yP = yOr - 3.6f*heightAx;
    xP =((xAxl/2f)+xOr)-((((float)nTextX)/2f)*(1.1429f*heightAx));
    // Concentration types:
    // hur =1 for "T" (fixed Total conc.)
    // hur =2 for "TV" (Tot. conc. Varied)
    // hur =3 for "LTV" (Log(Tot.conc.) Varied)
    // hur =4 for "LA" (fixed Log(Activity) value)
    // hur =5 for "LAV" (Log(Activity) Varied)
    if(dgrC.hur[diag.compX] ==5) { //"LAV"
        if(diag.pInX ==0) {
            if(Util.isGas(namn.identC[diag.compX])) {t = "Log P`"+namn.identC[diag.compX]+"'";}
            else {t = "Log {"+namn.identC[diag.compX]+"}";} // not Gas
        } // pInX=0
        else if(diag.pInX ==1) {t="pH";}
        else if(diag.pInX ==2) {t="pe";}
        else //if(pInX ==3)
         {t="E`SHE' / V";}
    } //"LAV"
    else if(dgrC.hur[diag.compX] ==2) { //"TV"
        t = "["+namn.identC[diag.compX]+"]`TOT'";
        if(xMolar) {t = t + "    M";}
        else  {t = t + "    mM";}
    }  else { // if(dgrC.hur[diag.compX] ==3)  "LTV"
        t = "Log ["+namn.identC[diag.compX]+"]`TOT'";
    }
    if(dgrC.hur[diag.compX] ==2 || dgrC.hur[diag.compX] ==3 || dgrC.hur[diag.compX] ==5) {
    g.setLabel("X-AXIS TEXT");  g.moveToDrawTo(0, 0, 0);
    g.sym((float)xP, (float)yP, (float)heightAx, t, 0, 0, false);}

    // -------------------------------------------------------------------
    //                  Draw the Curves
    g.setLabel("-- CURVES --"); g.setPen(1);
    //--- values for X-axis
    float[] xax = new float[sed.nSteps+1];
    for(int i =0; i < sed.nSteps+1; i++) {
        if(dgrC.hur[diag.compX] ==3) { //"LTV"
            xax[i] = (float)(xL*Math.log10(sed.bt[diag.compX][i])-xI);}
        else{xax[i] = (float)(xL*sed.bt[diag.compX][i]-xI);}
    } //for i
    float[] yax = new float[sed.nSteps+1];
    for(int i =0; i < yax.length; i++) {yax[i] =0f;}
    //--- curve counter: k
    int i, j, curvePoint;
    int xtraLabel = 0;
    boolean aPointIsShown, aPointIsShownNextNot, secondLabel, curveStarted;
    double yLow1 = yLow;
    if(diag.plotType ==1) {yLow1 = 0.005;} //fraction
    int[] maxPoint = new int[cs.Ms+L2];
    int k = 0;  // k is a curve counter
    do { // while (k < nbrSpeciesInPlot);
        kLoop:
        while(true) {
            i = speciesInPlot[k];
            if(diag.plotType ==1) {//fraction
                if(i>cs.Ms) {break;} //kLoop
                for(j=0; j < (sed.nSteps+1); j++) {
                    if(Math.abs(tot0[diag.compY][j]) > 1.e-30) {
                        double o;
                        if(i < cs.Na) {if(i==diag.compY) {o=1;} else {o=0;}}
                        else {o =cs.a[i-cs.Na][diag.compY];}
                        yax[j]=(float)(o*c0[i][j]/tot0[diag.compY][j]);
                        if(yax[j] >= 1.f && yax[j] <= 1.05f) {yax[j]=1.0f-1.E-5f;}
                    } else {yax[j] = 0f;}
                } //for j
            } else if(diag.plotType ==2) {//log solubility
                for(j=0; j < (sed.nSteps+1); j++) {
                    yax[j] = -50f;
                    if(tot0[i][j] > 1.E-35f) {yax[j]=(float)Math.log10(tot0[i][j]);}
                } //for j
            } else if(diag.plotType ==3) {//log conc.
                for(j=0; j < (sed.nSteps+1); j++) {
                    yax[j] = -99.f;
                    if(c0[i][j] > 1.E-35f) {yax[j]=(float)Math.log10(c0[i][j]);}
                } //for j
            } else if(diag.plotType ==4) {//log(ai/ar) diagram
                for(j=0; j < (sed.nSteps+1); j++) {yax[j] = (float)(c0[i][j] - c0[diag.compY][j]);}
            } else if(diag.plotType ==5) {//calc. pe
                for(j=0; j < (sed.nSteps+1); j++) {
                    if(!diag.Eh) {yax[j] = (float)(-c0[i][j]);}
                    else {yax[j] = (float)(-c0[i][j]*sed.peEh);}
                } //for j
            } else if(diag.plotType ==6) {//calc. pH
                for(j=0; j < (sed.nSteps+1); j++) {yax[j] = (float)(-c0[i][j]);}
            } else if(diag.plotType ==7) {//log act.
                for(j=0; j < (sed.nSteps+1); j++) {yax[j] = (float)(c0[i][j]);}
            } else if(diag.plotType ==8) {//H-affinity "d(H-bound)/d(-pH)"
                if(diag.Hplus >= 0 || diag.Hplus <= cs.Ms) {
                    //C0[0][n]=C(H+)    C0[1][n]=LOGA(H+)    C0[2][n]=C(OH-)
                    double w1, w2, y1, y2 = 0;
                    w1 = c0[1][1]-c0[1][0];
                    w2 = (tot0[diag.Hplus][1]-c0[0][1]+c0[2][1])
                         - (tot0[diag.Hplus][0]-c0[0][0]+c0[2][0]);
                    yax[0] =0f;
                    if(Math.abs(w1) >= 1e-35) yax[0]= (float)(w2 / w1);
                    for(j=1; j < sed.nSteps; j++) {
                        w1 = c0[1][j]-c0[1][j-1];
                        w2 = (tot0[diag.Hplus][j]-c0[0][j]+c0[2][j])
                                - (tot0[diag.Hplus][j-1]-c0[0][j-1]+c0[2][j-1]);
                        y1 = 0;
                        if(Math.abs(w1) >= 1e-35) y1= (float)(w2 / w1);
                        w1 = c0[1][j+1]-c0[1][j];
                        w2 = (tot0[diag.Hplus][j+1]-c0[0][j+1]+c0[2][j+1])
                            - (tot0[diag.Hplus][j]-c0[0][j]+c0[2][j]);
                        y2 = 0;
                        if(Math.abs(w1) >= 1e-35) y2= (float)(w2 / w1);
                        yax[j] = (float)((y1+y2)/2);
                    } //for j
                    yax[sed.nSteps] = (float)y2;
                }
            } else {
              err.println("Programming error in \"drawPlot\";  plotType = "+diag.plotType);
              g.end();
              return;
            }

            //--- Determine the Maximum values for the curve
            //    where the label(s) to the curves will be plotted
            double w;
            maxPoint[k] =-1;
            yMax[k] = yMin;
            yMax[nbrSpeciesInPlot+xtraLabel]=yMin;
            aPointIsShown =false;
            aPointIsShownNextNot =false;
            secondLabel =false;
            for(j=0; j < (sed.nSteps+1); j++) {
                if(aPointIsShown && (yax[j] < yMin)) {aPointIsShownNextNot =true;}
                if(aPointIsShownNextNot && (yax[j] > yMin)) {
                    if(xtraLabel < L2) {
                        if(yax[j] >= yMax[nbrSpeciesInPlot+xtraLabel] && yax[j] <= yHigh) {
                            secondLabel = true;
                            maxPoint[nbrSpeciesInPlot+xtraLabel] = j;
                            yMax[nbrSpeciesInPlot+xtraLabel] = yax[j];
                        }
                    }
                } else {
                    if(yax[j] < yMax[k] || yax[j] > yHigh) {continue;}
                    aPointIsShown =true;
                    maxPoint[k] = j;
                    yMax[k] = yax[j];
                }
            } //for j
            if(secondLabel) {
                speciesInPlot[nbrSpeciesInPlot+xtraLabel] = speciesInPlot[k];
                xtraLabel++;
            }

            //--- Do NOT plot if curve does not appear between YMIN and YHIGH
            if(maxPoint[k] <0) {break;} // kLoop

            if(diag.plotType ==2) {g.setLabel(namn.identC[i]);} // solubility namn
            else {g.setLabel(namn.ident[i]);}
            g.setPen(-(speciesInPlot[k]+1));

            //--- Draw the curve

            curveStarted = false;
            curvePoint = 0;
            if(yax[curvePoint] >yLow1 && yax[curvePoint] < yHigh) {
                g.moveToDrawTo(xax[curvePoint],yL*yax[curvePoint]-yI,0);
                curveStarted = true;
            }
            for(curvePoint = 1; curvePoint < (sed.nSteps+1); curvePoint++) {
                if(yax[curvePoint] >yLow1 && yax[curvePoint] < yHigh) {
                    if(!curveStarted) {
                        j= curvePoint-1;
                        if(yax[j] > yLow1) {
                            //xax2[k]= xax[j]+(xax[curvePoint]-xax[j])*((float)yHigh-yax[j]) /(yax[curvePoint]-yax[j]);
                            w = xax[j]+(xax[curvePoint]-xax[j])*((float)yHigh-yax[j])
                                    /(yax[curvePoint]-yax[j]);
                            yMax[k]= yHigh;
                            g.moveToDrawTo(w,yMx,0);
                        } else if(yax[j] == yLow1) {
                            g.moveToDrawTo(xax[j],yOr,0);
                        } else { // if (yax[j] < yLow1) 
                            xP= xax[j]+(xax[curvePoint]-xax[j])*((float)yLow-yax[j])
                                    /(yax[curvePoint]-yax[j]);
                            xP=Math.min(xMx,Math.max(xOr,xP));
                            g.moveToDrawTo(xP, yOr, 0);
                        }
                        curveStarted = true; // do not remove this line
                    }
                    g.moveToDrawTo(xax[curvePoint],yL*yax[curvePoint]-yI,1);
                } else {  //point outside yLow1 - yHigh range
                    if(curveStarted) {
                        j= curvePoint-1;
                        if(yax[curvePoint] > yLow1) {
                            //xax2[k]= xax[j]+(xax[curvePoint]-xax[j])*((float)yHigh-yax[j])/(yax[curvePoint]-yax[j]);
                            w = xax[j]+(xax[curvePoint]-xax[j])*((float)yHigh-yax[j])
                                    /(yax[curvePoint]-yax[j]);
                            yMax[k]= yHigh;
                            g.moveToDrawTo(w,yMx,1);
                        } else if(yax[curvePoint] == yLow1) {
                            g.moveToDrawTo(xax[curvePoint],yOr,1);
                        } else {//if (yax[n] < yLow1)
                            xP= xax[j]+(xax[curvePoint]-xax[j])*((float)yLow-yax[j])
                                    /(yax[curvePoint]-yax[j]);
                            xP=Math.min(xMx,Math.max(xOr,xP));
                            g.moveToDrawTo(xP, yOr, 1);
                        }
                        curveStarted = false; // do not remove this line
                    }
                }
            } //for curvePoint

            break; // kLoop
        } //kLoop: while(true) - breaks will go here...

        k++;
    } while (k < nbrSpeciesInPlot);

    // -------------------------------------------------------------------
    //                  Draw Labels on the Curves
    float[] xPl = new float[cs.Ms+L2];
    float[] yPl = new float[cs.Ms+L2];
    double w;
    if(diag.plotType !=5 && diag.plotType !=6 &&
                   diag.plotType !=8) { // no labels for pe pH or H-affinity
        g.setLabel("-- LABELS ON CURVES --");
        g.setPen(1);

        for(k =0; k < nbrSpeciesInPlot+xtraLabel; k++) {
            if(maxPoint[k] <0) {continue;}
            i = speciesInPlot[k];
            float xP1 = 0.5f*namn.nameLength[i]*heightAx;
            xP = xax[maxPoint[k]] -xP1;
            xP = Math.min(xMx-xP1, Math.max(xP,xOr+0.4f*heightAx));
            xPl[k] = (float)xP;
            yPl[k] = (float)(yL*yMax[k]+0.5*heightAx-yI);
        } //for k
        // Check that labels do not overlap
        boolean overlap; float yPl0; int iSign; int inkr;
        for(k =0; k < nbrSpeciesInPlot+xtraLabel; k++) {
            if(maxPoint[k] <0) {continue;}
            if(k >0) {//not first label
                yPl0 = yPl[k]; iSign = -1; inkr =1;
                do {//loop until a non-overlapping Y-position is found
                    overlap = false; 
                    for(int ij = 0; ij <k; ij++) {
                        if(maxPoint[ij] <0) {continue;}
                        if((yPl[k] > yPl[ij]+1.5*heightAx) ||
                           (yPl[k] < yPl[ij]-1.5*heightAx)) {continue;}
                        if((xPl[k] > xPl[ij]+heightAx*namn.nameLength[speciesInPlot[ij]]) ||
                           (xPl[ij] > xPl[k]+heightAx*namn.nameLength[speciesInPlot[k]])) {continue;}
                        overlap = true; break;
                    } //for ij
                    if(overlap) {
                        yPl[k] = yPl0 + (iSign * inkr * (float)heightAx/6.f);
                        iSign = -iSign; if(iSign ==-1) {inkr++;}
                    }
                } while (overlap);
                //at this point the label does not overlap with any other
            } //nor first label
            // diagramData the label:
            if(diag.plotType ==2) {t = namn.identC[speciesInPlot[k]];} //log (solubl.)
            else {t = namn.ident[speciesInPlot[k]];}
            g.setPen(-(speciesInPlot[k]+1));
            g.sym(xPl[k], yPl[k], (float)heightAx, t, 0f, -1, false);
        } //for k
    } // labels only for pe pH or H-affinity

    // -------------------------------------------------------------------
    //                  Text with concentrations as a Heading
    g.setLabel("-- HEADING --"); g.setPen(1); g.setPen(-1);
    float headColumnX = 0.5f*heightAx;
    int headRow = 0;
    int headRowMax = Math.max(2,(2+cs.Na)/2);
    yP = yMx + heightAx; // = yMx + 0.5f*heightAx in PREDOM
    float yPMx = yMx;
    double wa;
    // Concentration types:
    // hur =1 for "T" (fixed Total conc.)
    // hur =2 for "TV" (Tot. conc. Varied)
    // hur =3 for "LTV" (Log(Tot.conc.) Varied)
    // hur =4 for "LA" (fixed Log(Activity) value)
    // hur =5 for "LAV" (Log(Activity) Varied)
    for(j =0; j < cs.Na; j++) {
        if(dgrC.hur[j] !=1 && dgrC.hur[j] !=4) {continue;} //"T" or "LA" only
        if(dgrC.hur[j] ==4 && Util.isWater(namn.ident[j])) {continue;}
        i = namn.iel[j];
        yP = yP + 2f*heightAx;
        headRow++;
        if(headRow == headRowMax) {
            headColumnX = headColumnX + (33f*heightAx);
            yP = yMx + 3f*heightAx; // = yMx + 2.5f*heightAx in PREDOM
        }
        yPMx = Math.max(yPMx,yP);
        String value;
        if(dgrC.hur[j] == 1) { //"T"
            w = csC.tot[j]; wa = Math.abs(w);
            String units = "M";
            if(wa < 1 && wa >= 0.9999E-4) {w = w*1.E+3; units = "mM";}
            else if(wa < 0.9999E-4 && wa >= 0.9999E-7) {w = w*1.E+6; units = "$M";}
            else if(wa < 0.9999E-7 && wa >= 0.9999E-10) {w = w*1.E+9; units = "nM";}
            else if(wa < 0.9999E-10) {units = " ";}
            if((wa <= 9999.9 && wa >= 0.9999E-10) || wa < 1E-99 ) {
                value = String.format(engl,"=%8.2f ",(float)w) + units;
            } else {
                value = String.format(engl,"=%10.2e M",(float)w);
            }
            t = "["+namn.ident[i]+"]`TOT' "+value;
        } // hur=1: "T"
        else //if(sed.hur[j] == 4) { //"LA"
        {   String c;
            boolean volt = false;
            if(Util.isElectron(namn.ident[j])) {
                w = -dgrC.cLow[j];
                if(diag.Eh){c = "E`H' = "; w = w*sed.peEh; volt = true;}
                else {c = "pe =";}
            } //isElectron
            else if(Util.isProton(namn.ident[i])) {
                w = -dgrC.cLow[j]; c = "pH=";}
            else if(Util.isGas(namn.ident[i])) {
                  w = dgrC.cLow[j];  c = "Log P`"+namn.ident[i]+"' =";}
            else {w = dgrC.cLow[j];  c = "Log {"+namn.ident[i]+"} =";}
            if(Math.abs(w) < Double.MIN_VALUE) {w = 0;} // no negative zero!
            value = String.format(engl,"%7.2f",(float)w);
            t = c + value;
            if(volt) {t = t+" V";}
        } //hur=4: "LA"
        g.sym(headColumnX, yP, heightAx, t, 0f, -1, false);
    } //for j

    // ---- Ionic Strength
    if(!Double.isNaN(diag.ionicStrength) &&
       Math.abs(diag.ionicStrength) > 1.e-10) {
        g.setLabel("-- Ionic Strength --");
        g.setPen(-1);
        yP = yP + 2f*heightAx;
        headRow++;
        if(headRow == headRowMax) {
            headColumnX = headColumnX + (33f*heightAx);
            yP = yMx + 3f*heightAx;  // = yMx + 2.5f*heightAx in PREDOM
        }
        if(yP > (yPMx + 0.1f*heightAx)) {headColumnX = (0.5f*heightAx); yPMx = yP;}
        if(diag.ionicStrength > 0) {
            t = String.format(engl,"I=%6.3f M",diag.ionicStrength);
        } else {t = "I= varied";}
        g.sym(headColumnX, yP, heightAx, t, 0, -1, false);
    } // if ionicStrength != NaN & !=0

    // ---- Temperature
    if(!Double.isNaN(diag.temperature) && diag.temperature > -1.e-6) {
        g.setLabel("-- Temperature --");
        g.setPen(-1);
        t = String.format(engl,"t=%3d~C",Math.round((float)diag.temperature));
        w = 2.7; // 7 for program PREDOM
        g.sym((float)(xMx+w*heightAx), (0.1f*heightAx), heightAx, t, 0, -1, false);
    } //if temperature_InCommandLine >0

    // ---- Title
    if(diag.title != null && diag.title.trim().length() > 0) {
        g.setLabel("-- TITLE --");
        g.setPen(2); g.setPen(-2);
        g.sym(0, yPMx+(3.5f*heightAx), heightAx, diag.title, 0, -1, false);
    } // plotTitle != null

    // -------------------------------------------------------------------
    //                  Finished
    g.end();
} //drawPlot()
//</editor-fold>

}// class Plot
