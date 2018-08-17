package predominanceAreaDiagrams;

import lib.common.Util;
import lib.kemi.chem.Chem;
import lib.kemi.graph_lib.GraphLib;
import lib.kemi.interpolate.Interpolate;

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
/** iOther[j] = how many neighbour points which are borderline between two
 * areas, in one of which, species j predominates. */
  private int[] iOther;
/** neighb[j][n] = number of the "n" neighbour point which is borderline
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
    if(nLines == 1) {} //continue;
    else if(nLines == 2) {
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
      dist = pd.yCentre[i] - pd.yCentre[i2nd];
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
void drawPlot(java.io.File plotFile, Chem ch, PredomData diagP) {
Chem.ChemSystem cs = ch.chemSystem;
Chem.ChemSystem.ChemConcs csC = cs.chemConcs;
Chem.ChemSystem.NamesEtc namn = cs.namn;
Chem.Diagr diag = ch.diag;
Chem.DiagrConcs dgrC = ch.diagrConcs;

out.println("--- Drawing the plot...");

boolean xMolar, yMolar;

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
    // Molar scale in Y-axis
    yMolar = true;
    if(dgrC.hur[diag.compY] <=2) { // T or TV
        if(Math.abs(yLow) <0.9d && Math.abs(yHigh) <0.9d) {
            //milimolar units in Y-axis
            yMolar = false;
            yLow = yLow * 1000.d; yHigh = yHigh * 1000.d;
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
    if(!xMolar) {xL = xL * 1000;}  // T or TV and "mM"
    if(!yMolar) {yL = yL * 1000;}  // T or TV and "mM"
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
    catch (GraphLib.OpenPlotFileException ex) {pred.showErrMsgBx(ex.getMessage(),1); g.end(); return;}
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
        if(yMolar) {t = t + "    M";}
        else  {t = t + "    mM";}
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
        if(xMolar) {t = t + "    M";}
        else  {t = t + "    mM";}
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
        try {pHn = n_pH(diag.temperature);}
        catch (Chem.ChemicalParameterException ex) {
            pred.showMsg(ex);  pHn = -1;
        }
        catch (Interpolate.RationalInterpolationException ex) {
            pred.showMsg(ex);  pHn = -1;
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
        pred.showMsg("Error: temperature NOT available in a pH/(pe or Eh) diagram.",0);
      } else {
        double O2lgK;
        try {O2lgK = O2_logK(diag.temperature);}
        catch (Chem.ChemicalParameterException ex) {
            pred.showMsg(ex);  O2lgK = -1;
        }
        catch (Interpolate.RationalInterpolationException ex) {
            pred.showMsg(ex);  O2lgK = -1;
        }
        if(O2lgK > 0) {
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
              line_O2[0]= ((0.25*O2lgK) - line_pH[0]) * w1;
              line_O2[1]= ((0.25*O2lgK) - line_pH[1]) * w1;
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
            String units = "M";
            if(wa < 1 && wa >= 0.9999E-4) {w = w*1.E+3; units = "mM";}
            else if(wa < 0.9999E-4 && wa >= 0.9999E-7) {w = w*1.E+6; units = "$M";}
            else if(wa < 0.9999E-7 && wa >= 0.9999E-10) {w = w*1.E+9; units = "nM";}
            else if(wa < 0.9999E-10) {units = " ";}
            if((wa <= 9999.9 && wa >= 0.9999E-10) || wa < 1.E-99) {
                value = String.format(engl,"=%8.2f ",(float)w) + units;
            } else {
                value = String.format(engl,"=%10.2e M",(float)w);
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
            t = String.format(engl,"I=%6.3f M",diag.ionicStrength);
        } else {t = "I= varied";}
        g.sym(headColumnX, yP, heightAx, t, 0, -1, false);
    } // if ionicStrength != NaN & !=0

    // ---- Temperature
    if(!Double.isNaN(diag.temperature) && diag.temperature > -1.e-6) {
        g.setLabel("-- Temperature --");
        g.setPen(-1);
        t = String.format(engl,"t=%3d~C",Math.round((float)diag.temperature));
        w = 7; // 2.7 for program SED
        g.sym((float)(xMx+w*heightAx), (0.1f*heightAx), heightAx, t, 0, -1, false);
    } //if temperature_InCommandLine >0

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
/** returns the neutral pH at the temperature "t" (in Celsius)
 * @param t temperature in Celsius
 * @return neutral pH at the temperature t
 * @throws chem.Chem.ChemicalParameterException
 * @throws diverse.Div.RationalInterpolationException */
private double n_pH(double t)
        throws Chem.ChemicalParameterException,
        Interpolate.RationalInterpolationException {
    return (double)n_pH((float)t);
}
/** returns the neutral pH at the temperature "t" (in Celsius)
 * @param t temperature in Celsius
 * @return neutral pH at the temperature t
 * @throws chem.Chem.ChemicalParameterException
 * @throws diverse.Div.RationalInterpolationException  */
private float n_pH(float t)
        throws Chem.ChemicalParameterException,
        Interpolate.RationalInterpolationException {
  float[] temp = { 0f,   25f,    60f,    100f,   150f,   200f,   250f,   300f,  350f};
  float[] logK ={14.949f,14.000f,13.038f,12.271f,11.658f,11.325f,11.216f,11.39f,11.93f};
  if(t < 0 || t > 350) {
      throw new Chem.ChemicalParameterException (nl+
              "Error in procedure \"n_pH\":"+
              "  temperature = "+t+"°C (min=0, max=350).");
  }
  float npH = Interpolate.rationalInterpolation(temp, logK, t) / 2f;
  if(pred.dbg) {out.println("--- n_pH("+t+") = "+npH);}
  return npH;
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="O2_logK">
/** returns the logK for reaction [O2(g) + 4H+ + 4e- = 2H2O(l)]
 * at the temperature "t" (in Celsius). Note: logK=4pH+4pe, and
 * for reaction [H2(g) = 2H+ + 2e-] logK=0  at every temperature.
 * @param t temperature in Celsius
 * @return logK value at the temperature t
 * @throws chem.Chem.ChemicalParameterException
 * @throws diverse.Div.RationalInterpolationException */
private double O2_logK(double t)
        throws Chem.ChemicalParameterException,
        Interpolate.RationalInterpolationException {
    return (double)O2_logK((float)t);
}
/** returns the neutral pH at the temperature "t" (in Celsius)
 * @param t temperature in Celsius
 * @return neutral pH at the temperature t
 * @throws chem.Chem.ChemicalParameterException
 * @throws diverse.Div.RationalInterpolationException  */
private float O2_logK(float t)
        throws Chem.ChemicalParameterException,
        Interpolate.RationalInterpolationException {
  float[] temp = { 0f,   25f,    60f,    100f,   150f,   200f,   250f,   300f,  350f};
  float[] logK ={92.269f,83.091f,72.588f,63.038f,53.685f,46.348f,40.445f,35.60f,31.58f};
  if(t < 0 || t > 350) {
      throw new Chem.ChemicalParameterException (nl+
              "Error in procedure \"O2_line\":"+
              "  temperature = "+t+"°C (min=0, max=350).");
  }
  float O2lgK = Interpolate.rationalInterpolation(temp, logK, t);
  if(pred.dbg) {out.println("--- O2_logK("+t+") = "+O2lgK);}
  return O2lgK;
}
//</editor-fold>


}// class Plot_Predom
