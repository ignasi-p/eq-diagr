package lib.kemi.interpolate;

/** Interpolation methods.
 *
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
public class Interpolate {
  private static final String nl = System.getProperty("line.separator");

  //<editor-fold defaultstate="collapsed" desc="logKinterpolateTP">
/** Returns the value of logK at the requested tC and pBar, interpolated
 * from a provided array grid, at temperatures (0 to 600 C)
 * and pressures (1 to 5000 bar).
 * @param tC the input temperature in degrees Celsius
 * @param pBar the input pressure in bar
 * @param logKarray an array of logK values (logKarray[5][14])
 * with pressures as the first index {pSat, 500, 1000, 3000 and 5000} and
 * temperatures as the second index
 * {0,25,50,100,150,200,250,300,350,400,450,500,550,600}, where pSat is the
 * liquid-vapour saturated pressure (for temperatures below the critical point).
 * <b>Note:</b> provide NaN (Not-a-Number) where no data is available,
 * for example at a pressure = 500 bar and temperatures above 450C, etc.
 * @return the value of logK interpolated at the provided values of tC and pBar
 */
public static float logKinterpolateTP(final float tC, final float pBar,
        final float[][] logKarray) throws IllegalArgumentException {
    //-------------------
    boolean dbg = false;
    //-------------------
    //                     index:            0   1   2    3    4    5    6    7    8    9   10   11   12   13
    final float[] temperatures = new float[]{0f,25f,50f,100f,150f,200f,250f,300f,350f,400f,450f,500f,550f,600f};
    //                     index:         0   1     2     3     4
    final float[] pressures = new float[]{1f,500f,1000f,3000f,5000f};
    //                     index:    0    1   2   3       4       5       6       7        8       9   10   11   12   13
    //                 temperature:  0   25  50  100     150     200     250     300     350 C   400C 450C 500C 550C 600C
    final float[] pSat = new float[]{1f, 1f, 1f,1.0141f,4.7615f,15.549f,39.762f,85.878f,165.29f, 300f,500f,650f,800f,950f};

    if(logKarray == null) {
        throw new IllegalArgumentException("\"interpolate3D\": logKarray = \"null\"");}
    if(Float.isNaN(tC) || Float.isNaN(pBar)) {
        throw new IllegalArgumentException("\"interpolate3D\": tC = "+tC+", pBar = "+pBar+"; both must be numbers");}
    if(tC < 0 || tC > temperatures[temperatures.length-1]) {
        throw new IllegalArgumentException("\"interpolate3D\": tC = "+tC+", must be >"+temperatures[0]+" and <"+temperatures[temperatures.length-1]);}
    if(pBar < 1 || pBar > pressures[pressures.length-1]) {
        throw new IllegalArgumentException("\"interpolate3D\": pBar = "+pBar+", must be >"+pressures[0]+" and <"+ pressures[pressures.length-1]);}
    if(logKarray.length != pressures.length) {
        throw new IllegalArgumentException("\"interpolate3D\": length of logKarray must be "+pressures.length);}
    for(int i = 0; i < pressures.length; i++) {
        if(logKarray[i] == null) {throw new IllegalArgumentException("\"interpolate3D\": logKarray["+i+"] = \"null\"");}
    }

    // --- Locate the place in the tables of temperatures and pressures,
    //     given tC and pBar (at which the logK evaluation is desired).
    java.awt.Point indx = indexer3D(tC,pBar,temperatures,pressures, dbg);
    int iT = indx.x, iP = indx.y;
    // --- The values of "iT,iP" define a 3x3 matrix of temperature and pressure
    //     (iT-2, iT-1, iT)x(iP,iP+1,iP+2). The values of tC and pBar are inside
    //     this sub-grid of logKarray.  If a NaN value for logK is found at some
    //     of the edges of this 3x3: grid move the grid (if possible).
    int iTnew = iT, iPnew = iP;
    double[] press = new double[pressures.length];
    for(int i = 0; i < pressures.length; i++) {press[i] = pressures[i];}
    for(int i = iT-2; i <= iT; i++) {
        if(i == iT-1) {continue;}
        for(int j = iP; j <= iP+2; j++) {
            if(j == iP+1) {continue;}
            if(temperatures[i] <= 400 && press[j] < 1.1) {press[j] = pSat[i];}
            else if(temperatures[i] > 450 && press[j] > 500 && press[j] <= 1000) {press[j] = pSat[i];}
            if(Double.isNaN(logKarray[j][i])) {
                if(j == iP) {
                    if(pBar >= press[iP] && iP+3 < pressures.length) {iPnew++; break;}
                } else if(j == iP+2) {
                    if(pBar <= press[iP+2] && iP > 0) {iPnew--; break;}
                }
                if(i == iT-2) {
                    if(tC >= temperatures[iT-2] && iT+1 < temperatures.length) {iTnew++; break;}
                } else if(i == iT) {
                    if(tC <= temperatures[iT] && iT > 2) {iTnew--; break;}
                }
            }
        }
        if(iT != iTnew || iP != iPnew) {break;}
    }
    iT = iTnew; iP = iPnew;
    if(dbg){if(temperatures[iT] <= 400  && press[iP] < 1.1) {press[iP] = pSat[iT];}
            else if(temperatures[iT] > 450 && press[iP] > 500 && press[iP] <= 1000) {press[iP] = pSat[iT];}
            System.out.println("\"interpolate3D\": tC="+tC+", pBar="+pBar+
            ", new x="+iT+", y="+iP+";"+
            " t["+iT+"]="+temperatures[iT]+","+
            "  p["+iP+"]="+press[iP]);}

    // Get three (3) interpolated logK's at the selected pressure,
    // at the three temperatures bracketing the selected temperature.
    // Note that lowest iT value passed by indexer3D is 2 and the
    // lowest iP value passed is 0.
    // The highest values are (temperatures.length-1) and (pressures.length-3).
    float sum, logK,pK,pJ;
    float[] lgK = new float[3];
    for(int i = 0; i < lgK.length; i++) {lgK[i] = 0f;}
    logK = 0f;
    int i,j,k,l = 0;
    for(i = iT-2; i <= iT; i++) {
        for(j = iP; j <= iP+2; j++) {
            sum = logKarray[j][i];
            for(k = iP; k <= iP+2; k++) {
                if(k != j) {
                    pK = pressures[k]; pJ = pressures[j];
                    if(temperatures[i] <= 400) {
                        // 1st pressure (="1"): at tC < 400C use pSat instead of "1"
                        if(k == 0) {pK = pSat[i];} else if(j == 0) {pJ = pSat[i];}
                    }
                    if(temperatures[i] > 450) {
                        // 2nd pressure (="500"): at tC > 450C use the pSat array
                        if(k == 1) {pK = pSat[i];} else if(j == 1) {pJ = pSat[i];}
                    }
                    sum = sum *(pBar-pK)/(pJ-pK);
                }
            }
            lgK[l] = lgK[l] + sum;
        }
        l++;
    }
    if(dbg) {
        System.out.println("tC="+tC+", pBar="+pBar+", iT="+iT+", iP="+iP+" lgK 0 to 2 = "+java.util.Arrays.toString(lgK));
        System.out.println("temperatures 0 to "+(temperatures.length-1)+", pressures 0 to "+(pressures.length-1));
    }
    // Interpolate logK at the selected temperature using
    // the three temperature values in lgK[]
    k = 0;
    for(i = iT-2; i <= iT; i++) {
        sum = lgK[k];
        for(j = iT-2; j <= iT; j++) {
            if(j != i) {sum = sum *(tC-temperatures[j])/(temperatures[i]-temperatures[j]);}
        }
        logK = logK + sum;
        k++;
    }
    if(dbg) {System.out.println("tC="+tC+", pBar="+pBar+", iT="+iT+", iP="+iP+" logK = "+logK);}
    return logK;
}

  //<editor-fold defaultstate="collapsed" desc="indexer3D">
/** Returns array indices using the specified temperature and pressure.
 * 
 * For <b>temperature</b>, the minimum value returned is 2 so that the first
 * three temperature values may be used for the subsequent extrapolation.
 * For example, if the temperature array is [0, 25, 50, 100, 150], with indices
 * 0 to 4, then if tC=5 or tC=45, the returned index value is 2, but if tC=85,
 * the returned value is 3 (so that temperatures 25, 50 and 100 may be used
 * for the extrapolation).
 * 
 * For <b>pressure</b>, the returned value is always less
 * than (length-3), so that the last three pressures may be used for the
 * subsequent extrapolation. For example if the pressure array is
 * [1, 500, 1000, 3000, 5000], with indices 0 to 4, then if pBar = 4500
 * the returned "y" value is 2 (so that the pressures 1000, 3000 and 5000 may
 * be used for the subsequent extrapolation), but if pBar = 50, the returned
 * value is 0 (pressures 1, 500 and 1000 may be used for the extrapolation).
 * 
 * @param tC temperature in degrees C
 * @param pBar pressure in bar
 * @param temperatures array of temperature values
 * @param pressures array of pressure values
 * @return the index for the temperature as "x" and the index for the pressure
 * as the "y" fields  */
private static java.awt.Point indexer3D(final float tC, final float pBar,
        final float[] temperatures, final float[] pressures, final boolean dbg)
        throws IllegalArgumentException {
    
    int iT,iP;
    if(temperatures.length <=3 || pressures.length <= 3) {
        throw new IllegalArgumentException("\"indexer3D\": "+
                "the length of the temperature and pressure arrays must be >3.");}
    

    int jLow = 0, jUpp = temperatures.length-1, jMid;
    if(tC < temperatures[jLow] || tC > temperatures[jUpp]) {
        throw new IllegalArgumentException("\"indexer3D\": t = "+tC+
                " must be >="+temperatures[jLow]+" and <="+temperatures[jUpp]);}

    if(tC == temperatures[jLow]) {iT = jLow;}
    else if(tC == temperatures[jUpp]) {iT = jUpp;}
    else{
        while((jUpp - jLow) > 1) {
            jMid = (jLow + jUpp) /2;
            if(tC > temperatures[jMid]) {jLow = jMid;} else {jUpp = jMid;}
        }
        iT = jUpp;
    }
    iT = Math.min(temperatures.length-1,Math.max(2, iT));

    jLow = 0; jUpp = pressures.length-1;
    if(pBar < pressures[jLow] || pBar > pressures[jUpp]) {
        throw new IllegalArgumentException("\"indexer3D\": p = "+pBar+
                " must be >="+pressures[jLow]+" and <="+pressures[jUpp]);}

    if(pBar <= pressures[jLow]) {iP = jLow;}
    else if(pBar > pressures[jUpp]) {iP = jUpp;}
    else{
        while((jUpp - jLow) > 1) {
            jMid = (jLow + jUpp) /2;
            if(pBar >= pressures[jMid]) {jLow = jMid;} else {jUpp = jMid;}
        }
        iP = jLow;
    }
    iP = Math.min(pressures.length-3,Math.max(0, iP));
    if(dbg) {System.out.println("\"indexer3D\": tC="+tC+", pBar="+pBar+
            ", returns x="+iT+", y="+iP+";"+
            " t["+iT+"]="+temperatures[iT]+","+
            "  p["+iP+"]="+pressures[iP]);}
    java.awt.Point indexes = new java.awt.Point();
    indexes.x = iT;
    indexes.y = iP;
    return indexes;
}
  //</editor-fold>
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="logKinterpolatePsat">
/** Returns the value of logK at the requested tC, interpolated
 * from the provided array, at temperatures 0 to 350 C 
 * @param tC the input temperature in degrees Celsius
 * @param logKarray an array of logK values (logKarray[9]) at
 * the temperatures {0,25,50,100,150,200,250,300,350} corresponding
 * to saturated vapor pressure. Set absent logK values to not-a-number (NaN).
 * @return the value of logK interpolated at the provided tC value */
public static float logKinterpolatePsat(final float tC, final float[] logKarray) {
    //                     index:            0   1   2   3    4    5    6    7    8
    final float[] temperatures = new float[]{0f,25f,50f,100f,150f,200f,250f,300f,350f};
    if(logKarray.length != temperatures.length) {
        throw new IllegalArgumentException("\"interpolate2D\": the length of logKarray must be "+temperatures.length+".");
    }
    // Locate the place in the table of temperatures
    // of tC (at which the logK evaluation is desired).
    int min = 0, max = temperatures.length-1;
    for (int i = 0; i < temperatures.length; i++) {
        if(!Float.isNaN(logKarray[i])) {min = i; break;}
    }
    for (int i = temperatures.length-1; i > -1; i--) {
        if(!Float.isNaN(logKarray[i])) {max = i; break;}
    }
    for (int i = min; i <= max; i++) {
        if(Float.isNaN(logKarray[i])) {
            throw new IllegalArgumentException("\"interpolate2D\": found NaN value in logKarray["+i+"], in between two numeric values.");
        }
    }
    // -----------------
    //   Special cases
    // -----------------
    // --- out of range?
    if(tC < 0 || tC > temperatures[max]+50
            || tC > 370) { // the critical temperature is 374 C
        return Float.NaN;
    }
    // --- only logK data at one temperature is given (25C), and tC "close to" this temperature
    if(min == max) {
        if(tC >= (temperatures[min]-15) && tC <= (temperatures[min]+15)) {
            //System.out.println("\"interpolate2D\": tC="+tC+", only logK data at 25C provided, and tC \"close to\" 25C");
            return logKarray[min];
        }
        else {return Float.NaN;}
    }
    // --- only two logK values are given
    //     if tC is between them use linear interpolation
    if((max-min) == 1) {
        // data at 25 and 50C given, and tC between 0 and 75 C
        if((min == 1 && max ==2 && tC >= 0 && tC <= 75)
                // or tC within the two temperature values +/- 15C
                || (tC >= (temperatures[min]-15) && tC <= (temperatures[max]+15))) {
                //System.out.println("\"interpolate2D\": tC="+tC+", only logK data at 25 and 50C provided, and tC between 0 and 75 C");
                return logKarray[min]
                    +(tC-temperatures[min])*(logKarray[max]-logKarray[min])
                                         /(temperatures[max]-temperatures[min]);
        } else {return Float.NaN;}
    }
    // -----------------------------------
    //   General case:
    //   three or more logK values given
    // -----------------------------------
    int iT = min;
    for(int i = min; i < max; i++) {
        if(tC > temperatures[i] && tC <=temperatures[i+1]) {iT = i; break;}
    }
    if(tC > temperatures[max]) {iT = max-1;}
    //System.out.println("\"interpolate2D\": tC="+tC+", iT="+iT+",  logKarray["+iT+"]="+logKarray[iT]+", min="+min+" max="+max);
    float sum, lgK = 0;
    int forMin, forMax;
    // --- if tC <= the second temperature value, use the 3 smallest temperature values
    if(iT <= min) {forMin = min; forMax = min+2;}
    // --- if tC >= next to last temperature value, use the 3 largest temperature values
    else if(iT >= max-1) {forMin = max-2; forMax = max;}
    // --- else use two temperature values below and one above tC
    else {forMin = iT-1; forMax = iT+1;}
    // ---
    for(int i = forMin; i <= forMax; i++) {
        sum = logKarray[i];
        for(int j=forMin; j <= forMax; j++) {
            if(j != i) {
                sum = sum*(tC-temperatures[j])/(temperatures[i]-temperatures[j]);
            }
        }
        lgK = lgK + sum;
    }
    //System.out.println("\"interpolate2D\": returns "+lgK);
    return lgK;
}
  //</editor-fold>

 //<editor-fold defaultstate="collapsed" desc="not used">


  //<editor-fold defaultstate="collapsed" desc="interpolate">

/** Given arrays x[] and y[], each of the same length, with the
 * x[] values sorted, and given a value xx,
 * this procedure returns an interpolated value yy.
 * @param x array of x-values
 * @param y the corresponding y-values
 * @param xx the target
 * @return the interpolated y-value corresponding to the target x-value.
 * Returns NaN (not-a-number) if xx is outside the range of x[]
 //
public static float interpolate(final float[] x, final float[] y, final float xx)
        throws IllegalArgumentException {
    if(y.length != x.length) {
        throw new IllegalArgumentException("\"interpolation\": the length of y-array must be "+x.length+".");
    }
    // Locate the place in the table of x-values
    // of xx (at which the evaluation of y is desired).
    int min = 0, max = x.length-1;
    for (int i = 0; i < x.length; i++) {
        if(!Float.isNaN(y[i])) {min = i; break;}
    }
    for (int i = x.length-1; i > -1; i--) {
        if(!Float.isNaN(y[i])) {max = i; break;}
    }
    for (int i = min; i <= max; i++) {
        if(Float.isNaN(y[i])) {
            throw new IllegalArgumentException("\"interpolation\": found NaN value in y["+i+"], between two numeric values.");
        }
    }
    // -----------------
    //   Special cases
    // -----------------
    // --- out of range?
    if(xx < x[min] || xx > x[max]) {return Float.NaN;}
    // --- only y value at one x value is given, and xx "close to" this x value
    if(min == max) {
        if(xx >= (x[min]*0.95) && xx <= (x[min]+1.05)) {
            //System.out.println("\"interpolation\": xx="+xx+" and only \"y\" data at x="+x[min]+" provided.");
            return y[min];
        }
        else {return Float.NaN;}
    }
    // --- only two y values are given
    //     if xx is between them use linear interpolation
    if((max-min) == 1) {
        if(xx >= (x[min]-0.95) && xx <= (x[max]+1.05)) {
                //System.out.println("\"interpolation\": xx="+xx+", only \"y\" data x = "+x[min]+" and "+x[max]+" provided.");
                return y[min]
                    +(xx-x[min])*(y[max]-y[min])
                                         /(x[max]-x[min]);
        } else {return Float.NaN;}
    }
    // -----------------------------------
    //   General case:
    //   three or more y values given
    // -----------------------------------
    int ix = min;
    for(int i = min; i < max; i++) {
        if(xx > x[i] && xx <=x[i+1]) {ix = i; break;}
    }
    if(xx > x[max]) {ix = max-1;}
    //System.out.println("\"interpolation\": xx="+xx+", ix="+ix+",  y["+ix+"]="+y[ix]+", min="+min+" max="+max);
    float sum, yy = 0;
    int forMin, forMax;
    // --- if xx <= the second y value, use the 3 smallest y values
    if(ix <= min) {forMin = min; forMax = min+2;}
    // --- if xx >= next to last y value, use the 3 largest y values
    else if(ix >= max-1) {forMin = max-2; forMax = max;}
    // --- else use two y values below and one above xx
    else {forMin = ix-1; forMax = ix+1;}
    // ---
    for(int i = forMin; i <= forMax; i++) {
        sum = y[i];
        for(int j=forMin; j <= forMax; j++) {
            if(j != i) {
                sum = sum*(xx-x[j])/(x[i]-x[j]);
            }
        }
        yy = yy + sum;
    }
    //System.out.println("\"interpolation\": returns "+yy);
    return yy;
}
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="rationalInterpolation">
/** Rational interpolation (1-dimensional interpolation):
 * Given arrays xTable and yTable, each of the same length, with the
 * xTable values sorted, and given a value x,
 * this procedure returns an interpolated value y.
 * @param xTable array of x-values
 * @param yTable the corresponding y-values
 * @param x the target
 * @return the interpolated y-value corresponding to the target x-value

  public static double rationalInterpolation(double[] xTable, double[] yTable, double x)
        throws IllegalArgumentException, ArithmeticException {
    //adapted from: NUMERICAL RECIPES. THE ART OF SCIENTIFIC COMPUTING.
    //   by   W.H.Press, B.P.Flannery, S.A.Teukolsky and W.T.Vetterling
    //        Cambridge University Press, Cambridge (1987), p. 85
    if(xTable == null) {
        throw new IllegalArgumentException(
                "Error in \"rationalInterpolation\"; xTable = \"null\"");
    }
    int nTable = xTable.length;
    if(nTable < 2) { // at least two points needed for an interpolation
        throw new IllegalArgumentException(
                "Error in \"rationalInterpolation\"; length of array < 2.");
    }
    for(int i=0; i < nTable; i++) {
      if(Double.isNaN(xTable[i])) {
        throw new IllegalArgumentException(
            "Error in \"rationalInterpolation\"; xTable["+i+"] = NaN.");
      }
    }
    for(int i=1; i < nTable; i++) {
      if(xTable[i] < xTable[i-1]) {
        throw new IllegalArgumentException(
            "Error in \"rationalInterpolation\"; x-values not in ascending order.");
      }
    }
    if(x > xTable[nTable-1] || x < xTable[0]) {
        throw new IllegalArgumentException(
            "Error in \"rationalInterpolation\"; x = "+x+nl+
            "   is outside interpolation range.");
    }
    if(yTable == null) {
        throw new IllegalArgumentException(
                "Error in \"rationalInterpolation\"; yTable = \"null\"");
    }
    if(yTable.length != nTable) {
        throw new IllegalArgumentException(
            "Error in \"rationalInterpolation\"; length of arrays are not equal.");
    }
    for(int i=0; i < nTable; i++) {
      if(Double.isNaN(yTable[i])) {
        throw new IllegalArgumentException(
            "Error in \"rationalInterpolation\"; yTable["+i+"] = NaN.");
      }
    }

    double y;
    double tiny = 1e-25; // a small number
    double dist, t;
    //-- yErr = error estimate
    double yErr;
    double[] c = new double[nTable];
    double[] d = new double[nTable];
    //-- get closest table entry
    double delta =Math.abs(x-xTable[0]);
    int nClose = 0;
    for(int i=0; i < nTable; i++) {
        dist = Math.abs(x-xTable[i]);
        if(dist <= 0) {
            y = yTable[i];
            return y;
        }
        if(dist < delta) {nClose = i; delta = dist;}
        c[i] = yTable[i];
        d[i] = yTable[i] + tiny; // "tiny" is needed to prevent a rare zero-over-zero condition
    } //for i
    y = yTable[nClose];
    nClose--;
    for(int m =1; m < nTable; m++) {
        for(int i =0; i < nTable-m; i++) {
            dist = xTable[i+m] -x; // can't be zero, we checked above
            t = (xTable[i]-x)*d[i]/dist;
            delta = t - c[i+1];
            if(delta == 0) {
                throw new ArithmeticException(
                    "Error in \"rationalInterpolation\": Pole at the requested value of x="+x);
            }
            delta = (c[i+1]-d[i]) / delta;
            d[i] = c[i+1] * delta;
            c[i] = t * delta;
        } //for i
        if(2*nClose < nTable - m) {
            yErr = c[nClose+1];
        } else {
            yErr = d[nClose]; nClose--;
        }
        y = y + yErr;
    } //for m
    return y;
  } //rationalInterpolation()
  //</editor-fold>
  
  */
  //</editor-fold>
}
