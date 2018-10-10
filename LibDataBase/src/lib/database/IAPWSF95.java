package lib.database;

/** A set of routines used to calculate the density of fluid water,
 * using the "H2O" model in:<br>
 * Wagner, W, Pruß, A (2002) The IAPWS Formulation 1995 for the thermodynamic
 * properties of ordinary water substance for general and scientific use;
 * Journal of Physical and Chemical Reference Data 31, 387–535. DOI: 10.1063/1.1461829.
 * 
 * Copyright (C) 2015-2018 I.Puigdomenech.
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
public class IAPWSF95 {
public void iapwsf95() {}
/** Zero Celsius = 273.15 K */
public static final double T0 = 273.15; // K
/** triple point temperature = 273.16 K (= 0.01 C)*/
public static final double TRIPLE_POINT_T = 273.16; // K
/** triple point temperature = 0.01 C (= 273.16 K)*/
public static final double TRIPLE_POINT_TC = 0.01; // K
/** triple point pressure = 611.657 Pa (= 0.0061 bar) */
public static final double TRIPLE_POINT_P = 611.657; // Pa
/** Critical temperature = 647.096 (+/- 0.01) K  (= 373.946 C) */
public static final double CRITICAL_T = 647.096; // K
/** Critical temperature = 373.946 C  (= 647.096 K) */
public static final double CRITICAL_TC = CRITICAL_T-T0; // C
/** Critical pressure = 22.064 (+/- 0.27) MPa (= 220.64 bar) */
public static final double CRITICAL_p = 22.064; // MPa
/** Critical pressure = 220.64 bar (= 22.064 MPa) */
public static final double CRITICAL_pBar = 220.64; // bar
/** Critical densisty = 322 (+/- 3) kg/m3 (= 0.322 g/cm3) */
public static final double CRITICAL_rho = 322; // kg/m3
/** temperature of melting ice Ih at highest pressure = 251.165 K (at p = 2085.66 bar) */
private static final double MELTING_T_ICE_Ih_AT_HIGH_P = 251.165;
/** temperature of melting ice III at highest pressure = 256.164 K (at p = 3501 bar) */
private static final double MELTING_T_ICE_III_AT_HIGH_P = 256.164;
/** temperature of melting ice V at highest pressure = 273.31 K (at p = 6324 bar) */
private static final double MELTING_T_ICE_V_AT_HIGH_P = 273.31;
/** temperature of melting ice VI at highest pressure = 355 K (at p = 22 160 bar) */
private static final double MELTING_T_ICE_VI_AT_HIGH_P = 355;
/** pressure of melting ice Ih at highest pressure = 208.566 MPa (at T = 251.165 K) */
private static final double MELTING_P_ICE_Ih_AT_HIGH_P = 208.566;
/** pressure of melting ice III at highest pressure = 350.1 MPa (at T = 256.164 K) */
private static final double MELTING_P_ICE_III_AT_HIGH_P = 350.1;
/** pressure of melting ice V at highest pressure = 632.4 MPa (at T = 273.31 K) */
private static final double MELTING_P_ICE_V_AT_HIGH_P = 632.4;
/** pressure of melting ice VI at highest pressure = 2216 MPa (at T = 355 K) */
private static final double MELTING_P_ICE_VI_AT_HIGH_P = 2216;
/** the gas constant (kJ/(kg K)) for a molar mass for water = 18.015268 g/mol  */
private static final double R = 0.46151805;

//<editor-fold defaultstate="collapsed" desc="pSat(tC)">
/** Returns the saturation pressure (bar) of ordinary water substance, that is,
 * the pressure at the vapor–liquid phase boundary, for a given temperature (in
 * degrees Celsius). Uses eqn. (2.5) in Wagner, W., Pruß, A., 2002. "The IAPWS
 * formulation 1995 for the thermodynamic properties of ordinary water substance
 * for general and scientific use. Journal of Physical and Chemical Reference
 * Data 31, 387–535. DOI: 10.1063/1.1461829.
 * 
 * @param tC intput temperature in degrees Celsius
 * @return the pressure in units of bar
 * @throws IllegalArgumentException 
 */
static public double pSat(final double tC) throws IllegalArgumentException {
 if(tC < (TRIPLE_POINT_T-T0-1e-4) || tC >= (CRITICAL_T-T0)) {
     throw new IllegalArgumentException("\"pSat\": input temperature tC="+tC+
             ", must be >="+(float)(TRIPLE_POINT_T-T0)+" and <"+(float)(CRITICAL_T-T0));
 }
 final double a1 = -7.85951783, a2 = 1.84408259, a3 = -11.7866497, a4 = 22.6807411, a5 = -15.9618719,
              a6 = 1.80122502;
 final double tK = tC + T0;
 double ϑ = 1.-(tK/CRITICAL_T);
 double ln_pSat = Math.log(CRITICAL_p) +
         (CRITICAL_T/tK)*
            (a1*ϑ + a2*Math.pow(ϑ,1.5) + a3*Math.pow(ϑ,3)
             + a4*Math.pow(ϑ,3.5) + a5*Math.pow(ϑ,4) + a6*Math.pow(ϑ,7.5));
 return Math.exp(ln_pSat)*10.; // convert MPa to bar
}
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="rhoSat(tC)">
/** Returns the density (g/cm3) of ordinary water substance at the
 * vapor–liquid phase boundary, for a given temperature in
 * degrees Celsius. Uses eqn. (2.6) in Wagner, W., Pruß, A., 2002. "The IAPWS
 * formulation 1995 for the thermodynamic properties of ordinary water substance
 * for general and scientific use. Journal of Physical and Chemical Reference
 * Data 31, 387–535. DOI: 10.1063/1.1461829.
 * 
 * @param tC intput temperature in degrees Celsius
 * @return the density in units of g/cm3
 * @throws IllegalArgumentException 
 */
static public double rhoSat(final double tC) throws IllegalArgumentException {
 if(tC < (TRIPLE_POINT_T-T0-1e-4) || tC > (CRITICAL_T-T0+1e-4)) {
    throw new IllegalArgumentException("\"rhoSat\": input temperature tC="+tC+
             ", must be >="+(float)(TRIPLE_POINT_T-T0)+" and <="+(float)(CRITICAL_T-T0));
 }
 final double b1 = 1.99274064, b2 = 1.09965342, b3 = -0.510839303, b4 = -1.75493479,
              b5 = -45.5170352, b6 = -6.74694450e+5;
 final double tK = tC + T0;
 final double ϑ = 1.-(tK/CRITICAL_T);
 double rho = CRITICAL_rho * 
            (1.0 + b1*Math.pow(ϑ,(1./3.)) + b2*Math.pow(ϑ,(2./3.)) + b3*Math.pow(ϑ,(5./3.))
             + b4*Math.pow(ϑ,(16./3.)) + b5*Math.pow(ϑ,(43./3.)) + b6*Math.pow(ϑ,(110./3.)));
 return rho/1000.;
}
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="isWaterLiquid(tC,pbar)">
/** Finds out if water is liquid at the given input temperature and pressure
 * values.
 * Uses the eqns.(6), (7), (8), (9) and (10) in:
 * Wagner, W., Riethmann, T., Feistel, R., Harvey, A.H., 2011. New equations for
 * the sublimation pressure and melting pressure of H2O ice Ih. Journal of
 * Physical and Chemical Reference Data vol.40, 043103. DOI:10.1063/1.3657937.
 * 
 * @param tC the temperature in degrees Celsius
 * @param pbar the pressure in bar
 * @return an empty text string if water is liquid at the given temperature
 * and pressure, or a text sting specifying which phace (steam or ice) is
 * stable for the given temperature and pressure
 * @throws IllegalArgumentException 
 */
 static public String isWaterLiquid(final double tC, final double pbar) 
                throws IllegalArgumentException {
  if(Double.isNaN(tC) || tC < -T0) {
    throw new IllegalArgumentException("\"isWaterLiquid\": input temperature t="+tC+
             " C, must be >"+(-T0)+" C.");
  }
  if(Double.isNaN(pbar) || pbar > 206000. || pbar <= 0) {
    throw new IllegalArgumentException("\"isWaterLiquid\": input pressure p="+pbar+
             " bar, must be >0 and <206000 bar.");
  }
  final double pMPa = pbar * 0.1;
  final double tK = tC + T0;
  if(pMPa < TRIPLE_POINT_P*1e-6) {
      return "Water exists as either steam or ice-Ih at p = "+pbar+" bar (below the tripple point).";
  }
  if(pMPa <= CRITICAL_p && tK >= TRIPLE_POINT_T && tK <= CRITICAL_T) {
            // above pSat = liquid, below pSat = steam
            if(pbar < pSat(tC)) {return "Water exists as steam at t = "+tC+" C and p = "+pbar+" bar.";}
  }
  // --- divide the t-p diagram area into pressure intervals for the stability
  // of the differend ice phases (Ih, III, V, VI and VII).
  // Note that ice Ih melts when pressure is increased, while the other
  // ice phases become more stable when pressure is increased.
  if(pMPa < MELTING_P_ICE_Ih_AT_HIGH_P) { // ---- Ice Ih ----
    if(tK >= TRIPLE_POINT_T) { // temperature above the tripple point
        // whe have already excluded above the steam t-p area of stability...
        // so above the tripple point it must be liquid
        return "";
    } else // temperature below the tripple point but in the ice-Ih pressure range
    if(pbar >= p_melt_ice_Ih(tC)) {
        // ice-Ih melts when the pressure is increased;
        // at higher pressures water is liquid
        return "";
    } else {
        // the temperature is below the tripple point
        // and the pressure is less than the melting point
        return "Ice-Ih is the stable phase at t = "+tC+" C and p = "+pbar+" bar.";
    }
  } else if(pMPa < MELTING_P_ICE_III_AT_HIGH_P) { // ---- Ice III ----
    // pressure above Ice Ih and in the Ice III pressure range
    // (this is always above the critical point)
    // For Ice III, V, VI and VII, the stability is increased with pressure
    if(tK > MELTING_T_ICE_III_AT_HIGH_P || pbar < p_melt_ice_III(tC)) {return "";} else {
        return "Ice-III is the stable phase at t = "+tC+" C and p = "+pbar+" bar.";
    }
  } else if(pMPa < MELTING_P_ICE_V_AT_HIGH_P) { // ---- Ice V ----
    // pressure above Ice III and in the Ice V pressure range
    // (this is always above the critical point)
    // For Ice III, V, VI and VII, the stability is increased with pressure
    if(tK > MELTING_T_ICE_V_AT_HIGH_P || pbar < p_melt_ice_V(tC)) {return "";} else {
        return "Ice-V is the stable phase at t = "+tC+" C and p = "+pbar+" bar.";
    }
  } else if(pMPa < MELTING_P_ICE_VI_AT_HIGH_P) { // ---- Ice VI ----
    // pressure above Ice V and in the Ice VI pressure range
    // (this is always above the critical point)
    // For Ice III, V, VI and VII, the stability is increased with pressure
    if(tK > MELTING_T_ICE_VI_AT_HIGH_P || pbar < p_melt_ice_VI(tC)) {return "";} else {
        return "Ice-VI is the stable phase at t = "+tC+" C and p = "+pbar+" bar.";
    }
  } else { // ---- Ice VII ----
    // pressure above Ice VI and therefore in the Ice VII pressure range
    // (this is always above the critical point)
    // For Ice III, V, VI and VII, the stability is increased with pressure
    if(tK > 715 || pbar < p_melt_ice_VII(tC)) {return "";} else {
        return "Ice-VII is the stable phase at t = "+tC+" C and p = "+pbar+" bar.";
    }
  }
}
/** Returns the melting pressure of ice Ih, from the tripple point (273.16 K) to the
 * boundary with ice III (251.165 K). Uses eqn.(6) in:
 * Wagner, W., Riethmann, T., Feistel, R., Harvey, A.H., 2011. New equations for
 * the sublimation pressure and melting pressure of H2O ice Ih. Journal of
 * Physical and Chemical Reference Data vol.40, 043103. DOI:10.1063/1.3657937.
 * 
 * @param tC the input temperature in degrees Celsius
 * @return the melting pressure of ice Ih in bar
 * @throws IllegalArgumentException 
 */
static private double p_melt_ice_Ih(double tC) throws IllegalArgumentException {
 if(tC < (MELTING_T_ICE_Ih_AT_HIGH_P-T0-1e-5) || tC > (TRIPLE_POINT_T-T0+1e-5)) {
    throw new IllegalArgumentException("\"p_melt_ice_Ih\": input temperature tC="+tC+
             ", must be >="+(float)(MELTING_T_ICE_Ih_AT_HIGH_P-T0)+" and <="+(float)(TRIPLE_POINT_T-T0));
 }
 final double a1 = 0.119539337e7, a2 = 0.808183159e5, a3 = 0.333826860e4,
              b1 = 0.300000e1,    b2 = 0.257500e2,    b3 = 0.103750e3;
 final double tK = tC + T0;
 final double θ = (tK/TRIPLE_POINT_T);
 double pi = 1.0 + a1*(1.-Math.pow(θ,b1)) + a2*(1.-Math.pow(θ,b2)) + a3*(1.-Math.pow(θ,b3));
 return pi*TRIPLE_POINT_P*1e-5; // convert to bar
}
/** Returns the melting pressure of ice III, from the boundary with ice Ih
 * (251.165 K) to the boundary with ice V (256.164 K). Uses eqn.(7) in:
 * Wagner, W., Riethmann, T., Feistel, R., Harvey, A.H., 2011. New equations for
 * the sublimation pressure and melting pressure of H2O ice Ih. Journal of
 * Physical and Chemical Reference Data vol.40, 043103. DOI:10.1063/1.3657937.
 * 
 * @param tC the input temperature in degrees Celsius
 * @return the melting pressure of ice III in bar
 * @throws IllegalArgumentException 
 */
static private double p_melt_ice_III(double tC) throws IllegalArgumentException {
 if(tC < (MELTING_T_ICE_Ih_AT_HIGH_P-T0-1e-5) || tC > (MELTING_T_ICE_III_AT_HIGH_P-T0+1e-5)) {
    throw new IllegalArgumentException("\"p_melt_ice_III\": input temperature tC="+tC+
             ", must be >="+(float)(MELTING_T_ICE_Ih_AT_HIGH_P-T0)+" and <="+(float)(MELTING_T_ICE_III_AT_HIGH_P-T0));
 }
 final double tK = tC + T0;
 final double θ = (tK/MELTING_T_ICE_Ih_AT_HIGH_P);
 double pi = 1.0 - 0.299948 * (1.-Math.pow(θ,60.));
 return pi*MELTING_P_ICE_Ih_AT_HIGH_P*10.; // convert to bar
}
/** Returns the melting pressure of ice V, from the boundary with ice III
 * (256.164 K) to the boundary with ice VI (273.31 K). Uses eqn.(8) in:
 * Wagner, W., Riethmann, T., Feistel, R., Harvey, A.H., 2011. New equations for
 * the sublimation pressure and melting pressure of H2O ice Ih. Journal of
 * Physical and Chemical Reference Data vol.40, 043103. DOI:10.1063/1.3657937.
 * 
 * @param tC the input temperature in degrees Celsius
 * @return the melting pressure of ice V in bar
 * @throws IllegalArgumentException 
 */
static private double p_melt_ice_V(double tC) throws IllegalArgumentException {
 if(tC < (MELTING_T_ICE_III_AT_HIGH_P-T0-1e-5) || tC > (MELTING_T_ICE_V_AT_HIGH_P-T0+1e-5)) {
    throw new IllegalArgumentException("\"p_melt_ice_V\": input temperature tC="+tC+
             ", must be >="+(float)(MELTING_T_ICE_III_AT_HIGH_P-T0)+" and <="+(float)(MELTING_T_ICE_V_AT_HIGH_P-T0));
 }
 final double tK = tC + T0;
 final double θ = (tK/MELTING_T_ICE_III_AT_HIGH_P);
 double pi = 1.0 - 1.18721 * (1.-Math.pow(θ,8.));
 return pi*MELTING_P_ICE_III_AT_HIGH_P*10.; // convert to bar
}
/** Returns the melting pressure of ice VI, from the boundary with ice V
 * (273.31 K) to the boundary with ice VII (355 K). Uses eqn.(9) in:
 * Wagner, W., Riethmann, T., Feistel, R., Harvey, A.H., 2011. New equations for
 * the sublimation pressure and melting pressure of H2O ice Ih. Journal of
 * Physical and Chemical Reference Data vol.40, 043103. DOI:10.1063/1.3657937.
 * 
 * @param tC the input temperature in degrees Celsius
 * @return the melting pressure of ice VI in bar
 * @throws IllegalArgumentException 
 */
static private double p_melt_ice_VI(double tC) throws IllegalArgumentException {
 if(tC < (MELTING_T_ICE_V_AT_HIGH_P-T0-1e-5) || tC > (MELTING_T_ICE_VI_AT_HIGH_P-T0+1e-5)) {
    throw new IllegalArgumentException("\"p_melt_ice_V\": input temperature tC="+tC+
             ", must be >="+(float)(MELTING_T_ICE_V_AT_HIGH_P-T0)+" and <="+(float)(MELTING_T_ICE_VI_AT_HIGH_P-T0));
 }
 final double tK = tC + T0;
 final double θ = (tK/MELTING_T_ICE_V_AT_HIGH_P);
 double pi = 1.0 - 1.07476 * (1.-Math.pow(θ,4.6));
 return pi*MELTING_P_ICE_V_AT_HIGH_P*10.; // convert to bar
}
/** Returns the melting pressure of ice VII, from the boundary with ice V
 * (273.31 K) to the boundary with ice VII (355 K). Uses eqn.(10) in:
 * Wagner, W., Riethmann, T., Feistel, R., Harvey, A.H., 2011. New equations for
 * the sublimation pressure and melting pressure of H2O ice Ih. Journal of
 * Physical and Chemical Reference Data vol.40, 043103. DOI:10.1063/1.3657937.
 * 
 * @param tC the input temperature in degrees Celsius
 * @return the melting pressure of ice VII in bar
 * @throws IllegalArgumentException 
 */
static private double p_melt_ice_VII(double tC) throws IllegalArgumentException {
 if(tC < (MELTING_T_ICE_VI_AT_HIGH_P-T0-1e-5) || tC > (715-T0+1e-5)) {
    throw new IllegalArgumentException("\"p_melt_ice_V\": input temperature tC="+tC+
             ", must be >="+(float)(MELTING_T_ICE_VI_AT_HIGH_P-T0)+" and <="+(float)(715-T0));
 }
 final double tK = tC + T0;
 final double θ = (tK/MELTING_T_ICE_VI_AT_HIGH_P);
 double lnPi = 1.73683 * (1.-(1/θ)) - 0.0544606 * (1.-Math.pow(θ,5))
         + 0.806106e-7 * (1.-Math.pow(θ,22));
 return Math.exp(lnPi)*2216.*10.; // convert to bar
}
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="rho(tC,pbar)">
/** Calculates the density (kg/m3) of liquid (or supercritical fluid) water
 * at a given pressure (bar) and temperature (Celsius) using an iterative
 * procedure and the "H2O" model in:
 * Wagner, W., Pruß, A. (2002) The IAPWS Formulation 1995 for the thermodynamic
 * properties of ordinary water substance for general and scientific use.
 * Journal of Physical and Chemical Reference Data 31, 387–535. DOI: 10.1063/1.1461829.
 * <p>
 * If water is not liquid (or supercritical fluid), that is, if at the given
 * t-p conditions the stable phase is steam or ice, then an IllegalArgumentException
 * is thrown.
 * 
 * @param tC the input temperature in degrees Celsius
 * @param pbar the input temperature in bar
 * @return the density (kg/m3) of liquid (or supercritical fluid) water calculated
 * with the equations of Wagner and Pruß, (2002).
 * @throws IllegalArgumentException 
 */
static public double rho(final double tC, final double pbar)
        throws IllegalArgumentException, ArithmeticException {
  if(Double.isNaN(tC) || tC < (MELTING_T_ICE_Ih_AT_HIGH_P-T0) || tC > (1273.-T0)) {
    throw new IllegalArgumentException("\"rho\": input temperature t="+tC+
             " C, must be >"+(float)(MELTING_T_ICE_Ih_AT_HIGH_P-T0)+" and <"+(1273.-T0));
  }
  if(Double.isNaN(pbar) || pbar > 10000. || pbar <= 0.) {
    throw new IllegalArgumentException("\"rho\": input pressure p="+pbar+
             " bar, must be >0 and <10000 bar.");
  }
  String msg;
  msg=isWaterLiquid(tC, pbar);
  if(!msg.isEmpty()) {throw new IllegalArgumentException("\"rho\": "+msg);}

  double step = 10, r = 1000, rMax = 2000, rMin = 0, pMax = 1e25, pMin = -1e25, pCalc;
  double rTop = 2000, rBottom = 0, tolP = pbar*1e-6;
  if(tC >= (TRIPLE_POINT_T-T0) && tC <= (CRITICAL_T-T0)) {
      rMin = rhoSat(tC) * 1000; // convert to kg/m3.
      pCalc = pSat(tC);
      if(Math.abs(pbar-pCalc)<=tolP) {return rMin;}
      rBottom = rMin*0.9;
  }

  // ---- debug print-out
          boolean dbg = false;
  // ---- 
  if(dbg) System.out.println("rho: input tC="+(float)tC+", pbar="+(float)pbar);
  // ---- Find two values of rho (rMin and rMax) separated less than 5 units (kg/m3), such
  //      that they give calculated pressures (pMin and pMax) above and below pbar
  int sign, iter = 0, iterMax = 200;
  while (Math.abs(rMax-rMin) > 5 || pMin < 0) {
      r = Math.min(Math.max(r, rBottom),rTop);
      if(r>0) {pCalc = p4rhoT(r, tC, false)  * 10.;} // convert MPa to bar
              else {pCalc = 0;}
      iter++;
      if(Math.abs(pCalc-pbar) <= tolP || iter > iterMax) break;
      if(pCalc < pbar) {
          sign = +1;
          if((pbar-pCalc) <= (pbar-pMin)) {pMin = pCalc; rMin = r;}
      } else {
          sign = -1;
          if((pCalc-pbar) <= (pMax-pbar)) {pMax = pCalc; rMax = r;}
      }
      if(dbg) {System.out.println("r="+(float)r+" step="+(float)step+
              ", pCalc="+(float)pCalc+", pMin="+(float)pMin+
              ", pMax="+(float)pMax+", rMin="+(float)rMin+", rMax="+(float)rMax);}
      if(pMin != -1e25 && pMax != 1e25) {step = 0.5*step;}
      r = r + sign*step;
  } // while
  if(iter > iterMax) {
      throw new ArithmeticException("\"rho("+(float)tC+","+(float)pbar+")\": too many iterations."+
              " rhoMin = "+(float)rMin+" at pMin="+(float)pMin+
              ", rhoMax = "+(float)rMax+" at pMax="+(float)pMax);
  }
  if(dbg) System.out.println("iter="+iter+", r="+(float)r);
  // ---- Now use "cord shooting"
  double rOld = r, tolRho = Math.abs(r*1e-5);
  iter = 0;
  while(Math.abs(pMax-pMin)>tolP && Math.abs(rMax-rMin)>tolRho) {
    iter++;
    if(iter > iterMax) break;
    r = rMin + (pbar - pMin) * ((rMax - rMin) / (pMax-pMin));
    pCalc = p4rhoT(r, tC, false)  * 10.; // convert MPa to bar
    if(pCalc < pbar) {
        pMin = pCalc; rMin = r;
    } else {
        pMax = pCalc; rMax = r;
    }
    tolRho = Math.abs(r*1e-5);
    if(Math.abs(r-rOld)<tolRho && Math.abs(pbar-pCalc)<tolP) {break;}
    rOld = r;
  }
  if(iter > iterMax) {
      throw new ArithmeticException("\"rho\": too many iterations."+
              " rhoMin = "+(float)rMin+" at pMin="+(float)pMin+
              ", rhoMax = "+(float)rMax+" at pMax="+(float)pMax+"; target p="+(float)pbar);
  }
  if(dbg) System.out.println("iter="+iter+", r="+(float)r);
  return r;
}
/** Calculates the pressure (MPa) at a given density (kg/m3) and temperature (Celsius)
 * using the "H2O" model in:
 * Wagner, W., Pruß, A. (2002) The IAPWS Formulation 1995 for the thermodynamic
 * properties of ordinary water substance for general and scientific use.
 * Journal of Physical and Chemical Reference Data 31, 387–535. DOI: 10.1063/1.1461829
 * 
 * @param rho the input density in kg/m3
 * @param tC tje input temperature in degrees Celsius
 * @param dbg if true then results of intermediate calculations are printed
 * @return the pressure in MPa
 * @throws IllegalArgumentException 
 */
static public double p4rhoT(final double rho, final double tC, boolean dbg) throws IllegalArgumentException {
//<editor-fold defaultstate="collapsed" desc="(parameters)">
    // the parameters in Table 6.2 of Wagner and Pruß, (2002)
    final double[] n = new double[]{
        0.12533547935523e-1,
        0.78957634722828e1,
        -0.87803203303561e1,
        0.31802509345418,
        -0.26145533859358,
        -0.78199751687981e-2,
        0.88089493102134e-2,
        -0.66856572307965,
        0.20433810950965,
        -0.66212605039687e-4,
        -0.19232721156002,
        -0.25709043003438,
        0.16074868486251,
        -0.40092828925807e-1,
        0.39343422603254e-6,
        -0.75941377088144e-5,
        0.56250979351888e-3,
        -0.15608652257135e-4,
        0.11537996422951e-8,
        0.36582165144204e-6,
        -0.13251180074668e-11,
        -0.62639586912454e-9,
        -0.10793600908932,
        0.17611491008752e-1,
        0.22132295167546,
        -0.40247669763528,
        0.58083399985759,
        0.49969146990806e-2,
        -0.31358700712549e-1,
        -0.74315929710341,
        0.47807329915480,
        0.20527940895948e-1,
        -0.13636435110343,
        0.14180634400617e-1,
        0.83326504880713e-2,
        -0.29052336009585e-1,
        0.38615085574206e-1,
        -0.20393486513704e-1,
        -0.16554050063734e-2,
        0.19955571979541e-2,
        0.15870308324157e-3,
        -0.16388568342530e-4,
        0.43613615723811e-1,
        0.34994005463765e-1,
        -0.76788197844621e-1,
        0.22446277332006e-1,
        -0.62689710414685e-4,
        -0.55711118565645e-9,
        -0.19905718354408,
        0.31777497330738,
        -0.11841182425981,
        -0.31306260323435e2,
        0.31546140237781e2,
        -0.25213154341695e4,
        -0.14874640856724,
        0.31806110878444  };
    final double[] a = new double[56];
    final double[] b = new double[56];
    final double[] B = new double[56];
    final double[] C = new double[56];
    final double[] D = new double[56];
    final double[] A = new double[56];
    final double[] α = new double[54];
    final double[] β = new double[56];
    final double[] γ = new double[54];
    final double[] ε = new double[54];
    for(int i=0; i<56; i++){
        a[i]=Double.NaN;
        b[i]=Double.NaN;
        B[i]=Double.NaN;
        C[i]=Double.NaN;
        D[i]=Double.NaN;
        A[i]=Double.NaN;
        β[i]=Double.NaN;
        if(i >= 54) continue;
        α[i]=Double.NaN;
        γ[i]=Double.NaN;
        ε[i]=Double.NaN;
    }
    α[51]=20; β[51]=150; γ[51]=1.21; ε[51]=1;
    α[52]=20; β[52]=150; γ[52]=1.21; ε[52]=1;
    α[53]=20; β[53]=250; γ[53]=1.25; ε[53]=1;
        a[54]=3.5;  b[54]=0.85;  B[54]=0.2;  C[54]=28;  D[54]=700;  A[54]=0.32; β[54]=0.3;
        a[55]=3.5;  b[55]=0.95;  B[55]=0.2;  C[55]=32;  D[55]=800;  A[55]=0.32; β[55]=0.3;
    final double[] c = new double[]{
        Double.NaN,
        Double.NaN,
        Double.NaN,
        Double.NaN,
        Double.NaN,
        Double.NaN,
        Double.NaN,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        3,
        3,
        3,
        3,
        4,
        6,
        6,
        6,
        6,
        Double.NaN,
        Double.NaN,
        Double.NaN };
    final double[] d = new double[]{
        1,
        1,
        1,
        2,
        2,
        3,
        4,
        1,
        1,
        1,
        2,
        2,
        3,
        4,
        4,
        5,
        7,
        9,
        10,
        11,
        13,
        15,
        1,
        2,
        2,
        2,
        3,
        4,
        4,
        4,
        5,
        6,
        6,
        7,
        9,
        9,
        9,
        9,
        9,
        10,
        10,
        12,
        3,
        4,
        4,
        5,
        14,
        3,
        6,
        6,
        6,
        3,
        3,
        3 };
    final double[] t = new double[]{
        -0.5,
        0.875,
        1,
        0.5,
        0.75,
        0.375,
        1,
        4,
        6,
        12,
        1,
        5,
        4,
        2,
        13,
        9,
        3,
        4,
        11,
        4,
        13,
        1,
        7,
        1,
        9,
        10,
        10,
        3,
        7,
        10,
        10,
        6,
        10,
        10,
        1,
        2,
        3,
        4,
        8,
        6,
        9,
        8,
        16,
        22,
        23,
        23,
        10,
        50,
        44,
        46,
        50,
        0,
        1,
        4 };
// </editor-fold>
    double Δ, θ, ψ, δ_1_2, dψ_dδ,dΔbi_dδ,dΔ_dδ;
    double sum7 = 0, sum51 = 0, sum54 = 0, sum56 = 0;
    double tK = tC + T0;  // absolute temperature
    double τ = CRITICAL_T/tK;  // inverse reduced temperature
    double δ = rho/CRITICAL_rho;  // reduced density
    // The first equation of Table 6.3 of Wagner and Pruß, (2002):
    //    p = (rho * R * tK) * ( 1 + δ * φr_δ );
    // where φr_δ is the partial derivative of the residual part (φr) of
    // the dimensionless Helmholtz free energy with respect to δ at constant τ
    // This derivative is given as the second expresion in Table 6.5
    // of Wagner and Pruß, (2002). See also the derivatives of the
    // distance function Δ^b[i] and of the exponential function ψ at the
    // end of that table.
    if(dbg) System.out.println("tK="+tK+", τ="+(float)τ+", δ="+(float)δ);
    for(int i=54; i<56; i++) {
        δ_1_2 = Math.pow(δ-1.,2.);
        ψ = Math.exp(-C[i]*δ_1_2 -D[i]*Math.pow(τ-1.,2.));
        θ = (1.-τ)+ A[i]*Math.pow(δ_1_2,(1./(2.*β[i])));
        Δ = Math.pow(θ,2.) + B[i]*Math.pow(δ_1_2,a[i]);
        dψ_dδ = -2.*C[i]*(δ-1.)*ψ;
        dΔ_dδ = (δ-1.)*( A[i]*θ*(2./β[i])*Math.pow(δ_1_2,(1./(2.*β[i]))-1.)
                        + 2.*B[i]*a[i]*Math.pow(δ_1_2,(a[i]-1.)) );
        dΔbi_dδ = b[i]*Math.pow(Δ,b[i]-1.)*dΔ_dδ;
        if(dbg) System.out.println("i="+i+", θ="+(float)θ+", ψ="+(float)ψ+", Δ="+(float)Δ
                +", dψ_dδ="+(float)dψ_dδ+", dΔ_dδ="+(float)dΔ_dδ+", dΔbi_dδ="+(float)dΔbi_dδ);
        sum56 = sum56 +n[i]
                * ( Math.pow(Δ,b[i])*(ψ+δ*(dψ_dδ)) + dΔbi_dδ *δ*ψ );
    }
    for(int i=0; i<7; i++) {
        sum7 = sum7 +n[i]*d[i]*Math.pow(δ,(d[i]-1.))*Math.pow(τ,t[i]);
    }
    for(int i=7; i<51; i++) {
        sum51 = sum51 +n[i]*Math.exp(-Math.pow(δ,c[i]))
                *( Math.pow(δ,(d[i]-1.))*Math.pow(τ,t[i])
                    *(d[i]-c[i]*Math.pow(δ,c[i])) );
    }
    for(int i=51; i<54; i++) {
        sum54 = sum54 +n[i]*Math.pow(δ,d[i])*Math.pow(τ,t[i])
                * Math.exp(-α[i]*Math.pow(δ-ε[i],2.)-β[i]*Math.pow(τ-γ[i],2.))
                * ( (d[i]/δ)-2.*α[i]*(δ-ε[i]) );
    }
    // Add up "φr_δ": the partial derivative of the residual part (φr)
    // with respect to δ at constant τ
    double φr_δ = sum7 + sum51 + sum54 + sum56;
    if(dbg) System.out.println("φr_δ="+(float)φr_δ);
    // Calcuate the pressure using the first equation of Table 6.3
    // of Wagner and Pruß, (2002)
    double p = (rho * R * tK) * ( 1 + δ * φr_δ );
    // System.out.println("( 1 + δ * φr_δ )="+( 1 + δ * φr_δ ));
    return p/1000.; // convert to MPa units (the constant R is given in kJ instead of J)
}
// </editor-fold>

}
