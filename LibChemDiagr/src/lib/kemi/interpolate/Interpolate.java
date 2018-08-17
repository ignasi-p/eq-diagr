package lib.kemi.interpolate;

/** Rational interpolation.
 * Adapted from: NUMERICAL RECIPES. THE ART OF SCIENTIFIC COMPUTING.
 * by W.H.Press, B.P.Flannery, S.A.Teukolsky and W.T.Vetterling,
 * Cambridge University Press, Cambridge (1987), p. 85
 *
 * Copyright (C) 2014 I.Puigdomenech.
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
public class Interpolate {
  private static final String nl = System.getProperty("line.separator");

//<editor-fold defaultstate="collapsed" desc="rationalInterpolation">
/** Rational interpolation:
 * Given arrays xTable and yTable, each of the same length, with the
 * xTable values sorted, and given a value x,
 * this procedure returns an interpolated value y.
 * @param xTable array of x-values
 * @param yTable the corresponding y-values
 * @param x the target
 * @return the interpolated y-value corresponding to the target x-value
 * @throws lib.kemi.interpolate.Interpolate.RationalInterpolationException
 */
  public static float rationalInterpolation(float[] xTable, float[] yTable, float x)
        throws RationalInterpolationException {
    float y;
    int nTable = xTable.length;
    if(nTable < 2) { // at least two points needed for an interpolation
        throw new RationalInterpolationException(
                "Error in \"rationalInterpolation\"; length of array < 2.");
    }
    //if(nTable < 5) { // this algorithm does not work well with so litle points
    //  System.out.println("Warnng: \"interpolate\" does not work well with so litle points.");
    //}
    if(yTable.length != nTable) {
        throw new RationalInterpolationException(
            "Error in \"rationalInterpolation\"; length of arrays are not equal.");
    }
    boolean unsorted = false;
    for(int i=1; i < nTable; i++) {
      if(xTable[i] < xTable[i-1]) {unsorted = true; break;}
    }
    if(unsorted) {
        throw new RationalInterpolationException(
            "Error in \"rationalInterpolation\"; x-values not in ascending order.");
    }
    if(x > xTable[nTable-1] || x < xTable[0]) {
        throw new RationalInterpolationException(
            "Error in \"rationalInterpolation\"; x = "+x+nl+
            "   is outside interpolation range.");
    }
    //get closest table entry
    float dist;
    int nClose = 0;
    float delta =Math.abs(x-xTable[0]);
    for(int i=0; i<nTable; i++) {
      dist = Math.abs(x-xTable[i]);
      if(dist <= 0) {y = yTable[i]; return y;}
      if(dist < delta) {nClose = i; delta = dist;}
    } //for i
    // perform a rational interpolation using at most
    // the nearest 7 table entries. This is why the values in xTable
    // must be sorted.
    int n = Math.min(7,nTable);
    int nStart = 0;
    int nEnd;
    if(nTable > 7) { //then n=7
      nStart = nClose - 3;
      if(nStart < 0) {nStart = 0;}
      nEnd = nStart + 6;
      if(nEnd >= nTable) {
          nStart = nTable - 7;}
    }
    float[] xRatInt = new float[n];
    float[] yRatInt = new float[n];
    System.arraycopy(xTable, nStart, xRatInt, 0, n);
    System.arraycopy(yTable, nStart, yRatInt, 0, n);
    y = ratInt(xRatInt, yRatInt, x);
    return y;
  } //rationalInterpolation()

/** 1-dimensional interpolation. Given arrays xTable and yTable,
 * each of the same length (the values of xTable do not need to be sorted),
 * and given a value x, this procedure returns an interpolated value y
 *
 * @param xTable table of x-values
 * @param yTable table of corresponding y-values
 * @param x the value for which an y-value is needed
 * @return the interpolated value of y.
 */
private static float ratInt(float[] xTable, float[] yTable, float x)
            throws RationalInterpolationException {
  //adapted from: NUMERICAL RECIPES. THE ART OF SCIENTIFIC COMPUTING.
  //   by   W.H.Press, B.P.Flannery, S.A.Teukolsky and W.T.Vetterling
  //        Cambridge University Press, Cambridge (1987), p. 85
  float y;
  int nTable = xTable.length;
  if(nTable <= 0) {
      throw new RationalInterpolationException(
              "Error in \"Interpolate.ratInt\": length of x-array <=0.");
  }
  if(yTable.length != nTable) {
      throw new RationalInterpolationException(
              "Error in \"Interpolate.ratInt\": length of arrays unequal.");
  }
  //get closest table entry
  float tiny = 1e-25f; // a small number
  float dist, t;
  /** yErr = error estimate */
  float yErr;
  float[] c = new float[nTable];
  float[] d = new float[nTable];
  float delta =Math.abs(x-xTable[0]);
  int nClose = 0;
  for(int i=0; i < nTable; i++) {
    dist = Math.abs(x-xTable[i]);
    if(dist <= 0) {
        y = yTable[i];
        return y;
    }
    if(dist < delta) {nClose = i; delta = dist;}
    c[i] = yTable[i];
    d[i] = yTable[i] + tiny;
  } //for i
  y = yTable[nClose];
  nClose--;
  for(int m =1; m < nTable; m++) {
    for(int i =0; i < nTable-m; i++) {
      dist = xTable[i+m] -x; // can't be zero, we checked above
      t = (xTable[i]-x)*d[i]/dist;
      delta = t - c[i+1];
      if(delta == 0) {
          throw new RationalInterpolationException(
                  "Error in \"Interpolate.ratInt\":"+nl+
                  "   Pole at the requested value of x="+x);
      }
      delta = (c[i+1]-d[i]) / delta;
      d[i] = c[i+1] * delta;
      c[i] = t * delta;
    } //for i
    if(2*nClose < nTable - m) {
        yErr = c[nClose+1];
    } else {yErr = d[nClose]; nClose--;}
    y = y + yErr;
  } //for m
  return y;
} //ratInt

public static class RationalInterpolationException extends Exception {
    public RationalInterpolationException() {}
    public RationalInterpolationException(String txt) {super(txt);}
}

// </editor-fold>

}
