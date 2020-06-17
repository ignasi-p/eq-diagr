package predominanceAreaDiagrams;

import lib.common.Util;
import lib.kemi.chem.Chem;
import lib.kemi.graph_lib.GraphLib;

/** Methods to create a chemical equilibrium diagram.
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
public class Plot_Predom {
  private Predom pred = null;
  private static final java.util.Locale engl = java.util.Locale.ENGLISH;
/** Where errors will be printed. It may be <code>System.err</code>.
 * If null, <code>System.err</code> is used. */
  private final java.io.PrintStream err;
/** Where messages will be printed. It may be <code>System.out</code>.
 * If null, <code>System.out</code> is used. */
  private final java.io.PrintStream out;
/** iFL[k] > 0  if the line pair[k][1/2] has been drawn to the point k, and
 * iFL[k] = 0  if the line pair[k][1/2] starts at point k */
  private int[] iFL;
/** true if the line pair[i][1/2] continues with another point */
  private boolean lineContinued;
/** iOther[j] = how many neighbor points which are borderline between two
 * areas, in one of which, species j predominates. */
  private int[] iOther;
/** neighb[j][n] = number of the "n" neighbor point which is borderline
 * between two areas, in one of which,
 * species "j" predominates. */
  private int[][] neighb;

  private double[] ax;
  private double[] ay;
  private double[] ix;
  private double[] iy;

  private static final String nl = System.getProperty("line.separator");

/**
 * Constructor.
 * @param pred0 The PREDOM "frame".
 * @param err0 Where errors will be printed. It may be <code>System.err</code>.
 * If null, <code>System.err</code> is used.
 * @param out0 Where messages will be printed. It may be <code>System.out</code>.
 * If null, <code>System.out</code> is used. */
public Plot_Predom(Predom pred0, java.io.PrintStream err0, java.io.PrintStream out0) {
    this.pred = pred0;
    if(err0 != null) {this.err = err0;} else {this.err = System.err;}
    if(out0 != null) {this.out = out0;} else {this.out = System.out;}
} //constructor

//<editor-fold defaultstate="collapsed" desc="minMax(ch)">
/** Gets the position of the center of each predominance area, even
 * when the area is not rectangular or divided in two parts.
 * The values are stored in pd.xCentre and pd.yCentre
 * @param ch
 * @param pd
 */
void minMax(Chem ch, PredomData pd){
  if(pred.dbg) {
      out.println("--- minMax"+nl+"Calculating the position of the centre of each predominance area");
  }
  Chem.ChemSystem cs = ch.chemSystem;
  // ax and ix are the maximum and minimum x-coordinates, similarly for ay and iy
  ax = new double[cs.Ms];
  ay = new double[cs.Ms];
  ix = new double[cs.Ms];
  iy = new double[cs.Ms];
  for(int i=0; i<cs.Ms; i++) {ax[i]=-50000; ay[i]=-50000; ix[i]=10000; iy[i]=10000;}
  // ------------------------------------------------
  // Maximum and Minimum X and Y values for each area
  int j;
  for (int i=0; i< pd.nPoint; i++) {
    for(int k=0; k<=2; k++) {
        j = pd.pair[i][k];
        if(j>=0) {
            if(pd.xPl[i] > ax[j]) {ax[j]=pd.xPl[i];}
            if(pd.xPl[i] < ix[j]) {ix[j]=pd.xPl[i];}
            if(pd.yPl[i] > ay[j]) {ay[j]=pd.yPl[i];}
            if(pd.yPl[i] < iy[j]) {iy[j]=pd.yPl[i];}
        } //pair[i][k] >=0
    }
  } //for nPoint
  // ------------------------------------------------
  //    Get the position of the center of the area
  //        when the area is not rectangular
  //              or divided in two
  double z2 = 0.001 * Math.abs(pd.stepY);
  double zq = Math.abs(pd.stepX) + 0.001 * Math.abs(pd.stepX);
  double zx5 = Math.abs(pd.xRight - pd.xLeft) / 10;
  double zy5 = Math.abs(pd.yTop - pd.yBottom) / 10;
  double distS = Math.sqrt(zx5*zx5 + zy5*zy5);
  // number of lines belonging to area "i" that cross line X = xCentre[i]
  int[] nCentr = new int[4];
  // the Y-coordinates for each line belonging to area "i" that cross line X = xCentre[i]
  double[] yCentr = new double[4];
  int nLines; double last;
  double w, w2, z1, z3, q1, q2, q3, q4, ay1, ay2, iy1, iy2, yCent1, yCent2, dist;
  int ij, i2nd;
  for(int i =0; i < cs.Ms; i++) { //loop through all possible areas
    pd.xCentre[i] = (ax[i]+ix[i])/2;
    pd.yCentre[i] = (ay[i]+iy[i])/2;
    if(pd.xCentre[i] < -1000 || pd.yCentre[i] < -1000) {continue;}
    if(pred.dbg) {
        out.println(" Species "+i+", \""+cs.namn.ident[i]+"\", Centre = "+(float)pd.xCentre[i]+", "+(float)pd.yCentre[i]);
        out.println("   X from = "+(float)ix[i]+" to "+(float)ax[i]+", Y from = "+(float)iy[i]+" to "+(float)ay[i]);
    }
    // if the area is small, finished
    if((ay[i]-iy[i]) <= zy5 || (ax[i]-ix[i]) <= zx5) {continue;}
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // Find nLines = number of lines belonging to area "i"
    // that cross line X = xCentre[i]
    nLines = 0;
    last = -10000;
    w = pd.xCentre[i] + Math.abs(pd.stepX);
    for(int ip=0; ip < pd.nPoint; ip++) {
      if(pd.xPl[ip] < pd.xCentre[i] || pd.xPl[ip] >= w) {continue;}
      // Check that the point belongs to the area "i"
      if(pd.pair[ip][1] !=i && pd.pair[ip][0] !=i && pd.pair[ip][2] !=i) {continue;}
      // Check that the point is not inmediately following the previous one
      z1 = last + pd.stepY;
      z3 = last;
      last = pd.yPl[ip];
      if(last == z3 || Math.abs(last-z1) < z2) {continue;}
      nLines++;
      nCentr[nLines-1] = ip;
      yCentr[nLines-1] = pd.yPl[ip];
      if(nLines >= 4) {break;}
    } //for ip
    i2nd = i + cs.Ms;

    if(pred.dbg) { // debug printout
      out.println("   nLines = "+nLines);
      for(j=0; j<nLines; j++) {
        ij = nCentr[j];
        out.println("   point: "+ij+" species: "+pd.pair[ij][0]+","+
                pd.pair[ij][1]+"/"+pd.pair[ij][2]+",  x,y="+(float)pd.xPl[ij]+", "+(float)pd.yPl[ij]);
      }
    } //if dbg

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // how many lines go through X=xCentre ?
    //if(nLines == 1) {} //continue;
    //else
    if(nLines == 2) {
      //normal area
      pd.yCentre[i]=(yCentr[0]+yCentr[1])/2.;
      //continue;
    }
    else if(nLines == 0 || nLines == 3) {
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      // Species that has two predominance areas (i.e., area divided in two)
      //   one area to the left, one to the right
      // Find max and min Y values for ax[i] and ix[i]
      q1= ax[i] - zq;  q2= ax[i] + zq;  q3= ix[i] - zq;  q4= ix[i] + zq;
      ay1= -50000;  iy1= 10000;
      ay2= -50000;  iy2= 10000;
      for(int ip=0; ip <= pd.nPoint; ip++) {
        if(pd.pair[ip][0] != i && pd.pair[ip][1] != i && pd.pair[ip][2] != i) {continue;}
        if(pd.xPl[ip] >= q1 && pd.xPl[ip] <= q2) {
            if(pd.yPl[ip] > ay1) {ay1 = pd.yPl[ip];}
            if(pd.yPl[ip] < iy1) {iy1 = pd.yPl[ip];}
        }
        if(pd.xPl[ip] >= q3 && pd.xPl[ip] <= q4) {
            if(pd.yPl[ip] > ay2) {ay2 = pd.yPl[ip];}
            if(pd.yPl[ip] < iy2) {iy2 = pd.yPl[ip];}
        }
      } //for ip
      yCent1 = (ay1+iy1)/2.;
      yCent2 = (ay2+iy2)/2.;
      pd.yCentre[i2nd] = pd.yCentre[i];
      if(yCent1 > -1000) {pd.yCentre[i] = yCent1;}
      if(yCent2 > -1000) {pd.yCentre[i2nd] = yCent2;}
      pd.xCentre[i] = ax[i] - zx5/2.;
      pd.xCentre[i2nd] = ix[i] + zx5/2.;
      if(pred.dbg) {
        out.println("   q1,q2,q3,q4 = "+(float)q1+", "+(float)q2+", "+(float)q3+", "+(float)q4);
        out.println("   ay1,iy1,ay2,iy2 = "+(float)ay1+", "+(float)iy1+", "+(float)ay2+", "+(float)iy2);
        out.println("   (x/y)Centr[i] = "+(float)pd.xCentre[i]+", "+(float)pd.yCentre[i]+
                    ",  (x/y)Centr[i2nd] = "+(float)pd.xCentre[i2nd]+", "+(float)pd.yCentre[i2nd]);
      } //if dbg
      // Check if the distance is too small
      w = Math.pow(pd.xCentre[i] - pd.xCentre[i2nd], 2);
      w2 = Math.pow(pd.yCentre[i] - pd.yCentre[i2nd], 2);
      dist = Math.sqrt(w+w2);
      if(dist <= distS) {
          pd.xCentre[i] = (ax[i]+ix[i])/2.;
          pd.yCentre[i] = (ay[i]+iy[i])/2.;
          pd.xCentre[i2nd] = -30000;
          pd.yCentre[i2nd] = -30000;
      }
      if(pred.dbg) {
        out.println("   -- centres: "+(float)pd.xCentre[i]+", "+(float)pd.yCentre[i]+
                "  and "+(float)pd.xCentre[i2nd]+", "+(float)pd.yCentre[i2nd]);
      }
      // continue;
    }
    else if(nLines == 4) {
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      // Species that has two predominance areas (i.e., area divided in two)
      // one area up, one area down
      pd.yCentre[i] = (yCentr[0]+yCentr[1])/2.;
      pd.yCentre[i2nd] = (yCentr[2]+yCentr[3])/2.;
      pd.xCentre[i2nd] = pd.xCentre[i];
      // Check if the distance in Y is too small
      dist = Math.abs(pd.yCentre[i] - pd.yCentre[i2nd]);
      if(dist <= distS) {
          pd.yCentre[i] = (ay[i]+iy[i])/2.;
          pd.xCentre[i2nd] = -30000;
          pd.yCentre[i2nd] = -30000;
      }
      if(pred.dbg) {
        out.println("   -- centres: "+(float)pd.xCentre[i]+", "+(float)pd.yCentre[i]+
                "  and "+(float)pd.xCentre[i2nd]+", "+(float)pd.yCentre[i2nd]);
      }
      // continue;
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  } //for i
}//minMax(ch)
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="drawPlot (plotFile, ch)">
/** Create a diagram. Saving the diagram information in a PltData() object
 * and simultaneously store the data in a plot file.
 * @param plotFile where the diagram will be saved
 * @param ch where the data for the chemical system are stored
 * @param diagP contains information on the diagram
 */
void drawPlot(java.io.File plotFile, Chem ch, PredomData diagP)
        throws GraphLib.WritePlotFileException {
Chem.ChemSystem cs = ch.chemSystem;
Chem.ChemSystem.ChemConcs csC = cs.chemConcs;
Chem.ChemSystem.NamesEtc namn = cs.namn;
Chem.Diagr diag = ch.diag;
Chem.DiagrConcs dgrC = ch.diagrConcs;

out.println("--- Drawing the plot...");

//-- If true, the concentration is displayed as is.
//   If false, the concentration is displayed as milli molal, micro molal, or nano molal.
boolean xMolal, yMolal;

//-- display of concentrations: units and notation
//   default is that:
//   1- if the temperature is between 0 and 45 Celsius and the pressure
//      is below 50 bars, then units = "M" (molar) and the notation is
//      engineering (millimolar, micromolar, etc)
//   2- otherwise units = "molal" and the notation is engineering
//      (10'-3` molal, 10'-6` molal, etc)
pred.conc_units = Math.min(2, Math.max(pred.conc_units, -1));
pred.conc_nottn = Math.min(2, Math.max(pred.conc_nottn, 0));
if(pred.conc_nottn == 0) {pred.conc_nottn = 2;} // engineering    
if( (Double.isNaN(diag.temperature) || (diag.temperature >= 0 && diag.temperature <= 45))
        && (Double.isNaN(diag.pressure) || diag.pressure <=50)) {
    // temperatures around 25 and low pressures
    if(pred.conc_units == 0) {pred.conc_units = 2;} // units = "M"
}
String cUnit = pred.cUnits[(pred.conc_units+1)];
String mUnit = ("×10'-3` "+cUnit).trim();
String uUnit = ("×10'-6` "+cUnit).trim();
String nUnit = ("×10'-9` "+cUnit).trim();
if(pred.conc_units == 2) {mUnit = " mM"; uUnit = " $M"; nUnit = " nM";}

//---- Max and Min values in the axes: xLow,xHigh, yLow,yHigh
    double xLow = dgrC.cLow[diag.compX];
    double xHigh = dgrC.cHigh[diag.compX];
    // Concentration types:
    // hur =1 for "T" (fixed Total conc.)
    // hur =2 for "TV" (Tot. conc. Varied)
    // hur =3 for "LTV" (Log(Tot.conc.) Varied)
    // hur =4 for "LA" (fixed Log(Activity) value)
    // hur =5 for "LAV" (Log(Activity) Varied)
    //if(dgrC.hur[diag.compX] ==3) { // LTV
    //    xLow = Math.log10(xLow);
    //    xHigh = Math.log10(xHigh);
    //}
    if(Util.isProton(namn.identC[diag.compX]) ||
       Util.isElectron(namn.identC[diag.compX])) {
        if(dgrC.hur[diag.compX] !=5) {diag.pInX = 0;}  // not LAV: standard axis
        else { // LAV
            xLow = -xLow;  xHigh = -xHigh;
            if(diag.pInX == 3) {
                xLow  = pred.peEh * xLow;
                xHigh = pred.peEh * xHigh;
            }
        } // if LAV
    } // is H+ or engl-
    // standard scale in X-axis
    xMolal = true;
    if(dgrC.hur[diag.compX] <=2) { // T or TV
        if((pred.conc_nottn == 2 || (pred.conc_nottn == 0 && pred.conc_units == 2)) &&
                Math.abs(xLow) <0.9 && Math.abs(xHigh) <0.9) {
            //milli units in X-axis
            xMolal = false;
            xLow = xLow * 1000.; xHigh = xHigh * 1000.;
        }
    } //T or TV
    //Values for the Y-axis
    double yLow = dgrC.cLow[diag.compY];
    double yHigh = dgrC.cHigh[diag.compY];
    //if(dgrC.hur[diag.compY] ==3) { // LTV
    //    yLow = Math.log10(yLow);
    //    yHigh = Math.log10(yHigh);
    //}
    if(Util.isProton(namn.identC[diag.compY]) ||
       Util.isElectron(namn.identC[diag.compY])) {
        if(dgrC.hur[diag.compY] !=5) {diag.pInY = 0;}  // not LAV
        else { // LAV
            yLow = -yLow;  yHigh = -yHigh;
            if(diag.pInY == 3) {
                yLow  = pred.peEh * yLow;
                yHigh = pred.peEh * yHigh;
            }
        } // if LAV
    } // is H+ or engl-
    // standard scale in Y-axis
    yMolal = true;
    if(dgrC.hur[diag.compY] <=2) { // T or TV
        if(pred.conc_nottn == 2 ||
           (pred.conc_nottn == 0 && pred.conc_units == 2 && 
                Math.abs(yLow) <0.9 && Math.abs(yHigh) <0.9)) {
            //milli molal units in Y-axis
            yMolal = false;
            yLow = yLow * 1000.; yHigh = yHigh * 1000.;
        }
    } //T or TV

//---- Get the length of the axes labels
    int nTextX = 6 + namn.nameLength[namn.iel[diag.compX]];
    if(diag.pInX ==1 || diag.pInX ==2) {nTextX =2;} // pH or pe
    else if(diag.pInX ==3) {nTextX =8;} // "E`SHE' / V"
    int nTextY = 6 + namn.nameLength[namn.iel[diag.compY]];
    if(diag.pInY ==1 || diag.pInY ==2) {nTextY =2;} // pH or pe
    else if(diag.pInY ==3) {nTextY =8;} // "E`SHE' / V"

//---- Dimensions of the diagramData,  Size of text: height.  Origo: xOr,yOr
    float xAxl =10; float yAxl = 10;
    float heightAx = 0.035f * yAxl;
    if(pred.tHeight > 0.0001) {heightAx = (float)pred.tHeight*heightAx;}
    float xOr; float yOr;
    //xOr = 7.5f * heightAx;
    //yOr = 4.0f * heightAx;
    //for Predom:
    xOr = 4.5f;
    yOr = 1.6f;
    float xMx = xOr + xAxl;  float yMx = yOr + yAxl;
    //  xL and yL are scale factors, according to
    //  the axis-variables (pH, pe, Eh, log{}, log[]tot, etc)
    float xL = xAxl / (float)(xHigh - xLow);  float xI = xL * (float)xLow - xOr;
    float yL = yAxl / (float)(yHigh - yLow);  float yI = yL * (float)yHigh - yMx;
    if(!xMolal) {xL = xL * 1000;}  // T or TV and "milli units"
    if(!yMolal) {yL = yL * 1000;}  // T or TV and "milli units"
    //  pInX=0 "normal" X-axis
    //  pInX=1 pH in X-axis
    //  pInX=2 pe in X-axis
    //  pInX=3 Eh in X-axis
    if(diag.pInX ==1 || diag.pInX == 2) {xL = -xL;}
    else if(diag.pInX ==3) {xL = -xL * (float)pred.peEh;}
    if(diag.pInY ==1 || diag.pInY == 2) {yL = -yL;}
    else if(diag.pInY ==3) {yL = -yL * (float)pred.peEh;}

    // -------------------------------------------------------------------
    //          Create a PltData instance
    pred.dd = new GraphLib.PltData();
    //          Create a GraphLib instance
    GraphLib g = new GraphLib();
    boolean textWithFonts = true;
    try {g.start(pred.dd, plotFile, textWithFonts);}
    catch (GraphLib.WritePlotFileException ex) {pred.showErrMsgBx(ex.getMessage(),1); g.end(); return;}
    pred.dd.axisInfo = false;
    g.setLabel("-- PREDOM DIAGRAM --");
    // -------------------------------------------------------------------
    //                  Draw Axes
    g.setIsFormula(true);
    g.setPen(1);
    //g.setLabel("-- AXIS --");
    g.setPen(-1);
    // draw axes
    try {g.axes((float)xLow, (float)xHigh, (float)yLow, (float)yHigh,
            xOr,yOr, xAxl,yAxl, heightAx,
            false, false, true);}
    catch (GraphLib.AxesDataException ex) {pred.showMsg(ex); g.end(); return;}
    //---- Write text under axes
    // Concentration types:
    // hur =1 for "T" (fixed Total conc.)
    // hur =2 for "TV" (Tot. conc. Varied)
    // hur =3 for "LTV" (Log(Tot.conc.) Varied)
    // hur =4 for "LA" (fixed Log(Activity) value)
    // hur =5 for "LAV" (Log(Activity) Varied)
    // ---- Y-axis
    float xP; float yP;
    yP =((yAxl/2f)+yOr)-((((float)nTextY)/2f)*(1.3f*heightAx));
    xP = xOr - 6.6f*heightAx;
    String t;
    if(dgrC.hur[diag.compY] ==5) { //"LAV"
        if(diag.pInY ==0) {
            if(Util.isGas(namn.identC[diag.compY])) {t = "Log P`"+namn.identC[diag.compY]+"'";}
            else {t = "Log {"+namn.identC[diag.compY]+"}";} // not Gas
        } // pInY=0
        else if(diag.pInY ==1) {t="pH";}
        else if(diag.pInY ==2) {t="pe";}
        else //if(pInY ==3)
         {t="E`SHE' / V";}
    } //"LAV"
    else if(dgrC.hur[diag.compY] ==2) { //"TV"
        t = "["+namn.identC[diag.compY]+"]`TOT'";
        if(yMolal) {t = t + "   "+cUnit;}
        else  {t = t + "   "+mUnit;}
    } else  { // if(dgrC.hur[diag.compY] ==3)  "LTV"
        t = "Log ["+namn.identC[diag.compY]+"]`TOT'";
    }
    if(dgrC.hur[diag.compY] ==2 || dgrC.hur[diag.compY] ==3 || dgrC.hur[diag.compY] ==5) {
        g.setLabel("Y-AXIS TEXT");
        g.moveToDrawTo(0, 0, 0);
        g.sym((float)xP, (float)yP, (float)heightAx, t, 90, 0, false);
    }
    // ---- X-axis label (title)
    yP = yOr - 3.6f*heightAx;
    xP =((xAxl/2f)+xOr)-((((float)nTextX)/2f)*(1.1429f*heightAx));
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
        if(xMolal) {t = t + "   "+cUnit;}
        else  {t = t + "   "+mUnit;}
    } else { // if(dgrC.hur[diag.compX] ==3) "LTV"
        t = "Log ["+namn.identC[diag.compX]+"]`TOT'";
    } 
    if(dgrC.hur[diag.compX] ==2 || dgrC.hur[diag.compX] ==3 || dgrC.hur[diag.compX] ==5) {
        g.setLabel("X-AXIS TEXT");
        g.moveToDrawTo(0, 0, 0);
        g.sym((float)xP, (float)yP, (float)heightAx, t, 0, 0, false);
    }

    // -------------------------------------------------------------------
    //         Draw the lines separating predominance areas
    g.setLabel("-- PREDOMINANCE AREAS --"); g.moveToDrawTo(0, 0, 0);
    g.setLabel("-- LINES --"); g.moveToDrawTo(0, 0, 0);
    g.setPen(1);
    //-- get the distance used to extimate if two points are neighbours
    diagP.stepX = diagP.stepX * xL;
    diagP.stepY = diagP.stepY * yL;
    double xDMin = Math.sqrt((diagP.stepX*diagP.stepX)+(diagP.stepY*diagP.stepY));
    xDMin = 1.3*xDMin + xDMin*0.05;

    // iOther[j] = how many points that are borderline between two areas,
    //   in one of which species j predominates, are neighbours (within the xDMin distance)
    iOther = new int[cs.Ms];
    // neighb[j][n] = list of neighbour points which are borderline
    //    between two areas, in one of which, species "j" predominates.
    //    We assume that at most there will be 8 neighbours (for xDmin = 1x)
    //      at most there will be 24 neighbours (for xDmin = 2x)
    //      at most there will be 48 neighbours (for xDmin = 3x)
    neighb = new int[cs.Ms][24];
    // iFL[k] > 0  if the line pair[k][1/2] has been drawn to the point k, and
    // iFL[k] = 0  if the line pair[k][1/2] starts at point k
    iFL = new int[diagP.nPoint+1];
    for(int i =0; i < iFL.length; i++) {iFL[i]=0;}
    int j, now, ia, pairI0,pairI1, pairJ0,pairJ1, k;
    int ipoint = -1;
    double w1, w2, dist, dMin;
    boolean b;
    // ------------------------------------
    //   loop through all points to plot
    //
    // ---- Take a point ----
    for(int i = 0; i <= diagP.nPoint; i++) {
        pairI0 = diagP.pair[i][0];
        pairI1 = diagP.pair[i][1];
        if(pairI0 <= -1 || pairI1 <= -1) {continue;}
        if(diag.oneArea >=0 &&
                pairI0 != diag.oneArea && pairI1 != diag.oneArea) {continue;}
        lineContinued = false;
        iOther[pairI0] = 0; // no neighbouring points
        iOther[pairI1] = 0;
        now = -1;
        // ---- Take another point ----
        for(j = i+1; j <= diagP.nPoint; j++) {
            pairJ0 = diagP.pair[j][0];
            pairJ1 = diagP.pair[j][1];
            //-- See that both points are within the minimum distance
            w1 = Math.abs(diagP.xPl[i]-diagP.xPl[j])*xL;
            w2 = Math.abs(diagP.yPl[i]-diagP.yPl[j])*yL;
            dist = Math.sqrt((w1*w1)+(w2*w2));
            if(dist > xDMin) {continue;}
            if(!lineContinued || iFL[i] <= 1) {
                // -- See that they belong to the same line
                //    (both species in pair[i][] are the same)
                if(diag.oneArea >=0 &&
                      pairJ0 != diag.oneArea && pairJ1 != diag.oneArea) {continue;}
                if(pairJ0 == pairI0 && pairJ1 == pairI1) {
                    //-- draw the line between the two points
                    if(now != i) {g.moveToDrawTo((diagP.xPl[i]*xL-xI), (diagP.yPl[i]*yL-yI), 0);}
                    g.moveToDrawTo((diagP.xPl[j]*xL-xI), (diagP.yPl[j]*yL-yI), 1);

                    now = j;
                    iFL[j]++;
                    lineContinued = true; //flag that the next "j" is a continuation
                    continue; //next j
                } //else: not the same line
            } // if !lineContinued || iFL[i] <=1
            //-- Continuing the line,
            //   or points i and j do not belong to the same line:
            //   see if they have at least one species in common
            k = -1;
            if(pairJ0 == pairI0 || pairJ1 == pairI0) {k=pairI0;}
            if(pairJ0 == pairI1 || pairJ1 == pairI1) {k=pairI1;}
            if(k>=0) {
                neighb[k][iOther[k]] = j;
                iOther[k]++;
                // continue;
            }
        } //for j
        if(lineContinued && iFL[i] != 0) {continue;} //not continuing
        // --- The line pair[i][1/2] did not continue further.
        //     Check if there is some neighbour points of any other lines
        //     with one species in common.
        if(iOther[pairI0] > 0) {ia = pairI0;}
        else if(iOther[pairI1] > 0) {ia = pairI1;}
        else {continue;}
        b = false;
        do{
            //this loop is performed once or twice,
            //  with "ia" either ia = pairI0  and/or  ia = pairI1
            if(b) {ia = pairI1;}
            // Take the neighbour more far away from the center of the area
            dMin = 0;
            for(int ip = 0; ip < iOther[ia]; ip++) {
              j = neighb[ia][ip];
              w1 = Math.abs(diagP.xCentre[ia]-diagP.xPl[j]);
              w2 = Math.abs(diagP.yCentre[ia]-diagP.yPl[j]);
              dist = Math.sqrt((w1*w1)+(w2*w2));
              if(dist < dMin) {continue;}
              dMin = dist;
              ipoint = j;
            } //for ip
            //-- draw the line between "i" and "ipoint"
            j = ipoint;
            if(now != i) {g.moveToDrawTo((diagP.xPl[i]*xL-xI), (diagP.yPl[i]*yL-yI), 0);}
            g.moveToDrawTo((diagP.xPl[j]*xL-xI), (diagP.yPl[j]*yL-yI), 1);
            now = j;
            b = true;
        } while (ia == pairI0 && iOther[pairI1] >0);
    } //for i

    // -------------------------------------------------------------------
    //               Labels for Predominating Species
    g.setLabel("-- AREA LABELS --"); g.moveToDrawTo(0, 0, 0);
    g.setPen(4);
    g.setPen(-4);
    double size = heightAx * 0.75;
    double incrY = heightAx / 6;
    boolean overlp;
    int sign, incr;
    double yCentre0;
    int ii, ji;
    for(int i = 0; i < cs.Ms+cs.Ms; i++) {
      if(diagP.xCentre[i] < -1000) {continue;}
      ii = i;
      if(ii > cs.Ms) {ii = ii - cs.Ms;}
      if(diag.oneArea >=0 && ii != diag.oneArea) {continue;}
      diagP.xCentre[i] = (diagP.xCentre[i]*xL - xI) - (0.5*size*namn.nameLength[ii]);
      diagP.yCentre[i] = (diagP.yCentre[i]*yL - yI) - (0.5*size);
      // Check that labels do not overlap each other
      if(i>0) {
        yCentre0 = diagP.yCentre[i];
        sign = -1;
        incr = 1;
        do {
          overlp = false;
          for(j = 0; j < i; j++) {
            if(diagP.xCentre[j] < -1000) {continue;}
            ji = j;
            if(ji > cs.Ms) {ji = ji - cs.Ms;}
            if(diagP.yCentre[i] > (diagP.yCentre[j]+2*size)) {continue;}
            if(diagP.yCentre[i] < (diagP.yCentre[j]-2*size)) {continue;}
            if(diagP.xCentre[i] > (diagP.xCentre[j]+size*namn.nameLength[ji])) {continue;}
            if(diagP.xCentre[i] < (diagP.xCentre[j]-size*namn.nameLength[ji])) {continue;}
            overlp = true; break;
          } //for j
          if(overlp) {
            diagP.yCentre[i] = yCentre0 +(sign * incr * incrY);
            sign = -sign;
            if(sign == -1) {incr++;}
          } //if overlp
        } while (overlp);
      } //if i>0
      // plot the label
      g.sym(diagP.xCentre[i], diagP.yCentre[i], size, namn.ident[ii], 0, 0, false);
    } //for i


    // -------------------------------------------------------------------
    //                  Neutral pH - Dotted line
    // for diagrams with pH in one axis
    if(pred.neutral_pH && (diag.pInX ==1 || diag.pInY ==1)) {
      if(Double.isNaN(diag.temperature)) {
        pred.showMsg("Error: Neutral pH line requested but temperature NOT available.",0);
      } else {
        double pHn;
        try {pHn = n_pH(diag.temperature, diag.pressure);}
        catch (Exception ex) {
            pred.showMsg(ex);  pHn = -10;
        }
        if(pHn > 0) {
          g.setLabel("-- DOT LINE: neutral pH --"); g.moveToDrawTo(0, 0, 0);
          g.setPen(3);
          g.setPen(-3);
          g.lineType(5);
          double[] lineX = new double[2];
          double[] lineY = new double[2];
          if(diag.pInX == 1) {
              lineX[0]=pHn; lineX[1]=pHn;
              lineY[0]=diagP.yTop; lineY[1]=diagP.yBottom;
          } else {
              lineX[0]=diagP.xLeft; lineY[1]=diagP.xRight;
              lineY[0]=pHn; lineY[1]=pHn;
          }
          g.line(lineX, lineY);
          g.lineType(0);
        }
      } // temperature OK
    } // neutral_pH
    // -------------------------------------------------------------------
    //                  O2/H2O - H2/H2O lines
    if(diag.pInX >0 && diag.pInY >0) {
      // get equilibrium constants at the temperature (in Celsius) for:
      //   O2(g)  +  4 H+  +  4 e-  =  2 H2O(l)   logK(1)=4pH+4pe
      //   H2(g)  =  2 H+  +  2 e-              (logK(2)=0  at every temp)
      if(Double.isNaN(diag.temperature)) {
        pred.showMsg("Temperature NOT available in a pH/(pe or Eh) diagram.",2);
      } else {
        double lgKO2;
        final double CRITICAL_TC = 373.946;
        if(Double.isNaN(diag.pressure)) {
            if(diag.temperature >= 0 && diag.temperature < CRITICAL_TC) {
                diag.pressure = Math.max(1.,lib.kemi.H2O.IAPWSF95.pSat(diag.temperature));
            }
        }
        if(Double.isNaN(diag.pressure)) {diag.pressure = 1000;}
        try {lgKO2 = logK_O2(diag.temperature, diag.pressure);}
        catch (Exception ex) {pred.showMsg(ex);  lgKO2 = -1;}
        if(pred.dbg) {out.println("logK_O2("+diag.temperature+","+diag.pressure+") = "+lgKO2);}
        if(!Double.isNaN(lgKO2) && lgKO2 > 0) {
          g.setLabel("-- DASH LINES: O2(g) and H2(g) = 1 atm --"); g.moveToDrawTo(0, 0, 0);
          g.setPen(4);
          g.setPen(-5);
          g.lineType(1);
          double[] line_pH = new double[2];
          double[] line_O2 = new double[2];
          double[] line_H2 = new double[2];
              //the pH range:
              line_pH[0]=-10; line_pH[1]=+20;
              if(diag.pInX == 3 || diag.pInY == 3) {w1 = pred.peEh;} else {w1 = 1;}
              line_O2[0]= ((0.25*lgKO2) - line_pH[0]) * w1;
              line_O2[1]= ((0.25*lgKO2) - line_pH[1]) * w1;
              line_H2[0] = -line_pH[0] * w1;
              line_H2[1] = -line_pH[1] * w1;
          if(Util.isProton(namn.identC[diag.compX])) {
              g.line(line_pH, line_O2);
              g.line(line_pH, line_H2);
          } else {
              g.line(line_O2, line_pH);
              g.line(line_H2, line_pH);
          }
          g.lineType(0);
        }
      } // temperature OK
    } //both pInX and pInY

    // -------------------------------------------------------------------
    //                  Text with concentrations as a Heading
    g.setLabel("-- HEADING --"); g.setPen(1); g.setPen(-1);
    if(pred.dbg) {
        out.print("Heading; concentration units: \""+pred.cUnits[pred.conc_units+1]+"\"");
        if(pred.conc_nottn == 2 || (pred.conc_nottn == 0 && pred.conc_units == 2)) {out.print(",  notation: engineering");}
        if(pred.conc_nottn == 1 || (pred.conc_nottn == 0 && pred.conc_units != 2)) {out.print(",  notation: scientific");}
        out.println();
    }
    float headColumnX = 0.5f*heightAx;
    int headRow = 0;
    int headRowMax = Math.max(2,(1+cs.Na)/2);
    yP = yMx + 0.5f*heightAx; // = yMx + heightAx in SED
    float yPMx = yMx;
    double w, wa;
    // Concentration types:
    // hur =1 for "T" (fixed Total conc.)
    // hur =2 for "TV" (Tot. conc. Varied)
    // hur =3 for "LTV" (Log(Tot.conc.) Varied)
    // hur =4 for "LA" (fixed Log(Activity) value)
    // hur =5 for "LAV" (Log(Activity) Varied)
    int i;
    for(j =0; j < cs.Na; j++) {
        if(dgrC.hur[j] !=1 && dgrC.hur[j] !=4) {continue;} //"T" or "LA" only
        if(dgrC.hur[j] ==4 && Util.isWater(namn.ident[j])) {continue;}
        i = namn.iel[j];
        yP = yP + 2f*heightAx;
        headRow++;
        if(headRow == headRowMax) {
            headColumnX = headColumnX + (33f*heightAx);
            yP = yMx + 2.5f*heightAx; // = yMx + 3f*heightAx in SED
        }
        yPMx = Math.max(yPMx,yP);
        String value;
        if(dgrC.hur[j] == 1) { //"T"
            w = csC.tot[j]; wa = Math.abs(w);
            // use engineering notation?
            if(pred.conc_nottn == 2 || (pred.conc_nottn == 0 && pred.conc_units == 2)) {
                if(wa < 1.E-99) {value = String.format(engl,"=%8.2f",(float)w);}
                else if(wa < 1. && wa >= 0.9999E-4) {
                    w = w*1.E+3;
                    value = String.format(engl,"=%8.2f"+mUnit,(float)w);
                } else if(wa < 0.9999E-4 && wa >= 0.9999E-7) {
                    w = w*1.E+6;
                    value = String.format(engl,"=%8.2f"+uUnit,(float)w);
                } else if(wa < 0.9999E-7 && wa >= 0.9999E-10) {
                    w = w*1.E+9;
                    value = String.format(engl,"=%8.2f"+nUnit,(float)w);
                } else if(wa <= 9999.99 && wa >= 0.99) {
                    value = String.format(engl,"=%8.2f "+cUnit,(float)w);
                } else {
                    value = "= "+double2String(w)+" "+cUnit;
                }
            } else {
                if(wa < 1.E-99) {value = String.format(engl,"=%8.2f",(float)w);}
                else if(wa <= 9999.99 && wa >= 0.99) {
                    value = String.format(engl,"=%8.2f "+cUnit,(float)w);
                } else {
                    value = "= "+double2String(w)+" "+cUnit;
                }
            }
            t = "["+namn.ident[i]+"]`TOT' "+value;
        } // hur=1: "T"
        else //if(pred.hur[j] == 4) { //"LA"
        {   String c;
            boolean volt = false;
            if(Util.isElectron(namn.ident[j])) {
                w = -dgrC.cLow[j];
                if(diag.Eh){c = "E`H' = "; w = w*pred.peEh; volt = true;}
                else {c = "pe =";}
            } //isElectron
            else if(Util.isProton(namn.ident[i])) {
                w = -dgrC.cLow[j]; c = "pH=";}
            else if(Util.isGas(namn.ident[i])) {
                  w = dgrC.cLow[j];  c = "Log P`"+namn.ident[i]+"' =";}
            else {w = dgrC.cLow[j];  c = "Log {"+namn.ident[i]+"} =";}
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
            yP = yMx + 2.5f*heightAx; // = yMx + 3f*heightAx in SED
        }
        if(yP > (yPMx + 0.1f*heightAx)) {headColumnX = (0.5f*heightAx); yPMx = yP;}
        if(diag.ionicStrength > 0) {
            t = String.format(engl,"I=%6.3f "+cUnit,diag.ionicStrength);
        } else {t = "I= varied";}
        g.sym(headColumnX, yP, heightAx, t, 0, -1, false);
    } // if ionicStrength != NaN & !=0

    // ---- Temperature and pressure
    //if(!Double.isNaN(diag.temperature) && diag.temperature > -1.e-6) {
    //    g.setLabel("-- Temperature --");
    //    g.setPen(-1);
    //    t = String.format(engl,"t=%3d~C",Math.round((float)diag.temperature));
    //    w = 7; // 2.7 for program SED
    //    g.sym((float)(xMx+w*heightAx), (0.1f*heightAx), heightAx, t, 0, -1, false);
    //} //if temperature_InCommandLine >0
    // ---- Temperature + Pressure (in the heading)
    if(!Double.isNaN(diag.temperature)) {
        if(Double.isNaN(diag.pressure) || diag.pressure < 1.02) {
            g.setLabel("-- Temperature --");
        } else {g.setLabel("-- Temperature and Pressure --");}
        g.setPen(-1);
        t = String.format(engl,"t=%3d~C",Math.round((float)diag.temperature));
        if(!Double.isNaN(diag.pressure) && diag.pressure > 1.02) {
            if(diag.pressure < 220.64) {
                t = t + String.format(java.util.Locale.ENGLISH,", p=%.2f bar",diag.pressure);
            } else {
                if(diag.pressure <= 500) {t = t + String.format(", p=%.0f bar",diag.pressure);}
                else {t = t + String.format(java.util.Locale.ENGLISH,", p=%.1f kbar",(diag.pressure/1000.));}
            }
        }
        yP = yP + 2f*heightAx;
        headRow++;
        if(headRow == headRowMax) {
            headColumnX = headColumnX + (33f*heightAx);
            yP = yMx + 2.5f*heightAx;  // = yMx + 3f*heightAx in SED
        }
        if(yP > (yPMx + 0.1f*heightAx)) {headColumnX = (0.5f*heightAx); yPMx = yP;}
        g.sym(headColumnX, yP, heightAx, t, 0, -1, false);
    } //if temperature >0

    yP = yPMx;
    headColumnX = (float)(0.5d*heightAx);
    if(pred.aqu) {
        yP = yP + 2f*heightAx;
        g.sym(headColumnX, yP, heightAx, "(aqueous species only)", 0, -1, false);
    }
    if(diag.oneArea >= 0) {
        yP = yP + 2f*heightAx;
        g.sym(headColumnX, yP, heightAx, "(one area only)", 0, -1, false);
    }

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

//<editor-fold defaultstate="collapsed" desc="n_pH(temperature)">
/** Returns the neutral pH at the given temperature and pressure.
 * It uses the equation reported in
 * Marshall W L, Franck E U (1981) Ion product of water substance, 0-1000 °C,
 * 1-10,000 bars. New international formulation and its background.
 * J. Phys. Chem. Ref. Data, vol.10, pp.295–304. doi:10.1063/1.555643
 * @param tC temperature in Celsius (0 to 1000)
 * @param pBar pressure in bar (1 to 10 000)
 * @return neutral pH at the temperature t
 * @throws chem.Chem.ChemicalParameterException  */
private static double n_pH(double tC, double pBar) throws IllegalArgumentException,
        ArithmeticException {
  if(tC < 0 || tC > 1000) {
      throw new IllegalArgumentException (nl+
              "Error in procedure \"n_pH\":  temperature = "+tC+"°C (min=0, max=1000).");
  }
  if(pBar < 1 || pBar > 10000) {
      throw new IllegalArgumentException (nl+
              "Error in procedure \"n_pH\":  pressure = "+pBar+" bar (min=1, max=10000).");
  }
  if(pBar < 220){ // Critical pressure = 220.64 bar
      if(tC >= 373) { // Critical temperature = 373.946 C
        throw new IllegalArgumentException (nl+
              "Error in procedure \"n_pH\":"+
              "  t = "+tC+", p = "+pBar+" bar (at (p < 220 bar) t must be < 373C).");
      }
  }
  double rho = Double.NaN;
  try {rho = lib.kemi.H2O.IAPWSF95.rho(tC, pBar);}
  catch (Exception ex) {throw new ArithmeticException(ex.toString());}
  double a = -4.098, b = -3245.2, c = 2.2362e5, d = -3.984e7;
  double e = +13.957, f = -1262.3, g = +8.5641e5;
  double tK = tC + 273.15, tK2 = tK*tK;
  double npH = a + b/tK + c/tK2 + d/(tK2*tK) + (e + f/tK + g/tK2)*Math.log10(rho);
  npH = npH / 2;
  boolean dbg = false;
  if(dbg) {System.out.println("--- n_pH("+tC+","+pBar+") = "+npH);}
  return npH;
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="logK_O2(tC,pbar)">
/** Calculates the equilibrium constant for reaction 2H2O = O2(g)+2H2(g)
 * as a function of temperature and pressure. This reaction is equivalent to
 * 2H2O = O2(g) + 4H+ + 4e-, because the equilibrium constant for H2(g) = 2H+ + 2e-
 * is by definition zero at all temperatures and pressures. An equation is
 * used based in the values calculated with the SUPCRT92 software.<b>
 * Range of conditions: 1 to 5,000 bar, -35 to 600°C.
 * It returns NaN (Not a Number) outside this range, or if
 * water is frozen at the requested conditions.
 * If pBar < 1 a value of pBar = 1 is used.
 * If tC = 0 and pBar = 1, it returns the value at 0.01 Celsius and 1 bar.
 * NOTE: values returned at tC <0 are extrapolations outside the valid range.
 * @param tC temperature in Celsius
 * @param pBar pressure in bar
 * @return the equilibrium constant for the reaction 2H2O = O2(g)+2H2(g)
 * @throws IllegalArgumentException 
 */
  private static double logK_O2(final double tC, final double pBar) throws IllegalArgumentException {
  if(Double.isNaN(tC)) {throw new IllegalArgumentException("\"logK_O2\" tC = NaN.");}
  if(Double.isNaN(pBar)) {throw new IllegalArgumentException("\"logK_O2\" pBar = NaN.");}
  if(tC < -35 || tC > 1000.01 || pBar < 0 || pBar > 5000.01) {
      throw new IllegalArgumentException("\"logK_O2\" tC = "+tC+", pBar = "+pBar+" (must be -35 to 1000 C and 0 to 5000 bar)");
  }
  // set minimum pressure to 1 bar
  final double p_Bar = Math.max(pBar, 1.);
  final double t_C;
  // if pressure = 1 bar and temperature = 0, set temperature to 0.01 C (tripple point of water)
  if(p_Bar >0.99999 && p_Bar < 1.00001 && Math.abs(tC) < 0.001) {t_C = 0.01;} else {t_C = tC;}  
  // Make sure that water is not frozen or steam.
  String str = lib.kemi.H2O.IAPWSF95.isWaterLiquid(t_C, p_Bar); // ma
  if(str.length() >0) {throw new IllegalArgumentException(str);}
  // check if pressure is below the saturated vapor line
  if(t_C > 0.01 && t_C < 373.946) {
    final double pSat = lib.kemi.H2O.IAPWSF95.pSat(t_C);
    if(p_Bar < (pSat*0.99)) {
      throw new IllegalArgumentException("\"logK_O2\": pBar = "+p_Bar+ "(at tC = "+t_C+" pBar must be above "+(float)pSat+" bar)");
    }
  }

  final double logK0 = 83.105; // from the SUPCRT92 software
  final double tK = t_C + 273.15, tK0 = 298.15;
  final double tK_tK0 = tK-tK0;
  final double pBar_p0 = p_Bar - 1.;
  if(Math.abs(tK_tK0) < 1 && p_Bar < 2) {return logK0;}
  // final double R = 8.31446262; // gas constant
  // final double ln10 = 2.302585093;
  // final double Rln10 = 19.14475768082;
  // final double deltaS0 = -327.77;  // from the SUPCRT92 software
  final double deltaS0_Rln10 = -17.06840094; // = deltaS0 / (R ln(10))
  // the equation used is:
  // logK = (T0/T)logK0 + (deltaS0/(R T ln10)) (T-T0) + a (- (T-T0)/T + ln(T/T0))
  //       + b (-1/(2T) (T^2-T0^2) + (T-T0)) + e ((1/T) (1/T - 1/T0) - (1/2)(1/T^2 - 1/T0^2))
  //       - (q1 + q2 T + q3 T^2) (1/T) (P-P0) + (u1 + u2 T + u3 T^2) (1/2T) (P-P0)^2
  final double a = 5.585, b = -0.000404, e = -257000.;
  final double q1 = 0.2443,  q2 = -0.0004178, q3 = 7.357E-7;
  final double u1 = 2.17E-5, u2 = -1.044E-7,  u3 = 1.564E-10;
  final double tK2 = tK*tK;
  final double tK02 = 88893.4225; // = 298.15 x 298.15
  double logK = (tK0/tK)*logK0 + (deltaS0_Rln10/tK)*(tK_tK0);
  logK = logK + a * (-tK_tK0/tK + Math.log(tK/tK0));
  logK = logK + b * (-(1./(2.*tK))*(tK2-tK02) + tK_tK0);
  logK = logK + e * ((1./tK)*((1./tK)-(1./tK0)) - 0.5*((1./tK2)-(1./tK02)));
  if(p_Bar < 2) {return logK;}
  final double dq = q1 + q2*tK + q3*tK2;
  final double du = u1 + u2*tK + u3*tK2;
  logK = logK - dq * (1./tK)*pBar_p0 + du * (1./(2.*tK))*pBar_p0*pBar_p0;
  return logK;
  }
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="double2String">
/** Returns a text representing a double in the format
 * 1.23×10'-1`.
 * @param d
 * @return a string such as 1.23×10'-1`
 */
private static String double2String(double d) {
    if(Double.isNaN(d) || Double.isInfinite(d)) {return String.valueOf(d);}
    if(d == 0) {return "0.00";}
    boolean ok = true;
    // txt = number_in_scientific_notation
    final String txt = String.format(engl,"%10.2e",d).trim();
    int e = txt.indexOf('e');
    String exp = "", sign="";
    if(e >= 0) {
        exp = txt.substring(e+1);
        final int length =exp.length();
        if(length > 1) {
            int i, j;
            sign = exp.substring(0,1);
            if(sign.equals("+") || sign.equals("-")) {j=1;} else {j=0; sign="";}
            if(length > j+1) {
                for (i = j; i < length-1; i++) {
                    if (exp.charAt(i) != '0') {break;}
                }
                exp = exp.substring(i);
            } else {ok = false;}
        } else {ok = false;}
    } else {
        ok = false;
    }
    int k;
    try{k = Integer.parseInt(exp);} catch (Exception ex) {
        System.err.println(ex.getMessage());
        k=0;
        ok = false;
    }
    if(ok && k == 0) {return txt.substring(0, e);}
    if(ok && exp.length() >0) {
        return txt.substring(0, e)+"×10'"+sign+exp+"`";
    } else {return txt;}
}
//</editor-fold>

}// class Plot_Predom
