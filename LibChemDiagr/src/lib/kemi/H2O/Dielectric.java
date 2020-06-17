package lib.kemi.H2O;

/** A set of routines used to calculate the dielectric constant of water, etc<br>
 * Fernández, D.P., Goodwin, A.R.H., Lemmon, E.W., Levelt Sengers, J.M.H.,
 * Williams, R.C., 1997. A formulation for the static permittivity of water and
 * steam at temperatures from 238 K to 873 K at pressures up to 1200 MPa,
 * including derivatives and Debye–Hückel coefficients.
 * Journal of Physical and Chemical Reference Data 26, 1125–1166.
 * doi:10.1063/1.555997<br>
 * 
 * Copyright (C) 2019-2020 I.Puigdomenech.
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
public class Dielectric {
public Dielectric(){};
/** Zero Celsius = 273.15 K */
public static final double T0 = 273.15; // K
/** triple point temperature = 273.16 K (= 0.01 C)*/
public static final double TRIPLE_POINT_T = 273.16; // K
/** triple point temperature = 0.01 C (= 273.16 K) */
public static final double TRIPLE_POINT_TC = 0.01; // C
/** Critical temperature = 647.096 (+/- 0.01) K  (= 373.946 C) */
public static final double CRITICAL_T = 647.096; // K
/** Critical temperature = 373.946 C (=647.096 K) */
public static final double CRITICAL_TC = 373.946; // C
/** temperature of melting ice Ih at highest pressure = 251.165 K (at p = 2085.66 bar) */
private static final double MELTING_T_ICE_Ih_AT_HIGH_P = 251.165;

//<editor-fold defaultstate="collapsed" desc="epsSat(tC)">
/** Returns the static permitivity of water (dielectric constant)
 * at the liquid-vapor saturated pressure, calculated using the
 * equations reported in:<br>
 * Fernández, D.P., Goodwin, A.R.H., Lemmon, E.W., Levelt Sengers, J.M.H.,
 * Williams, R.C., 1997. A formulation for the static permittivity of water and
 * steam at temperatures from 238 K to 873 K at pressures up to 1200 MPa,
 * including derivatives and Debye–Hückel coefficients.
 * Journal of Physical and Chemical Reference Data 26, 1125–1166.
 * doi: 10.1063/1.555997<br>
 *
 * If tC = 0 it returns 87.956, the value at the triple point (0.01 C and 0.00611657 bar).
 * Range of conditions: 0.01°C (triple point) to 373.9°C (critical point).
 * It throws an exception outside this range.
 * 
 * @param tC the temperature in degrees Celsius
 * @return the dielectric constant of water (unitless) at the vapor saturated
 * pressure
 * @throws IllegalArgumentException
 * @see lib.kemi.H2O.Dielectric#epsJN(double, double) epsJN
 * @see lib.kemi.H2O.Dielectric#eps(double, double) eps
 */
static public double epsSat(final double tC) throws IllegalArgumentException {
 if(Double.isNaN(tC)) throw new IllegalArgumentException("\"epsSat(t)\": tC = NaN");
 if(tC == 0) {return 87.95631;} // the value at the triple point: 0.01 C and pBar = 0.00611657 bar
 if(tC < 0.01 || tC >= CRITICAL_TC) throw new IllegalArgumentException("\"epsSat(t)\": tC = "+tC+" (must be zero or >=0.01 and < 373.946 C)");
 final double T = tC + T0;
 // Table 8 in Fernandez et al (1997)
 final double[] L = new double[]{
     2.725384249466,
     1.090337041668,
     21.45259836736,
     -47.12759581194,
     4.346002813555,
     237.5561886971,
     -417.7353077397,
     249.3834003133};
  // Eqns. (35) and (36) in Fernandez et al (1997)
  final double theta = Math.pow((1.-(T/CRITICAL_T)),(1./3.));
  double epsLiq = 1.;
  for (int i=0; i < L.length; i++) {
      epsLiq = epsLiq + L[i] * Math.pow(theta,(i+1.));
  }
  return epsLiq * 5.36058;
}
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="eps(tC,pbar)">
/**  Returns the static permitivity of water (dielectric constant)
 * calculated using the equations reported in:<br>
 * Fernández, D.P., Goodwin, A.R.H., Lemmon, E.W., Levelt Sengers, J.M.H.,
 * Williams, R.C., 1997. A formulation for the static permittivity of water and
 * steam at temperatures from 238 K to 873 K at pressures up to 1200 MPa,
 * including derivatives and Debye–Hückel coefficients.
 * J. Phys. Chem. Ref. Data 26, 1125–1166. doi:10.1063/1.555997<br>
 *
 * <p>Range of conditions: 0 to 12,000 bar, -35 to 600°C.
 * It throws an exception outside this range, or if
 * water is frozen at the requested conditions.
 * If tC = 0 and pBar = 1, it returns the value at 0.01 Celsius and 1 bar.
 * <p>If the temperature is below the critical point and the pressure
 * corresponds to the vapor saturation (within 0.5%),
 * then epsSat is called.
 * @param tC the temperature in degrees Celsius
 * @param pBar the pressure in bar
 * @return the dielectric constant of liquid (or supercritical fluid) water (unitless)
 * @throws IllegalArgumentException
 * @see lib.kemi.H2O.Dielectric#epsSat(double) epsSat
 * @see lib.kemi.H2O.Dielectric#eps(double, double) eps
 */
static public double eps(final double tC, final double pBar) throws IllegalArgumentException {
  if(Double.isNaN(tC)) throw new IllegalArgumentException("\"eps(t,p)\": tC = NaN");
  if(tC < -35. || tC > 600.001) throw new IllegalArgumentException("\"eps(t,p)\": tC = "+tC+" (must be >-35 and <= 600 C)");
  if(Double.isNaN(pBar)) throw new IllegalArgumentException("\"eps(t,p)\": pBar = NaN");
  if(pBar > 10000.01 || pBar <= 0.) throw new IllegalArgumentException("\"eps(t,p)\": pBar = "+pBar+" (must be >0 and <= 10 kbar)");
  final double t_C;
  // if pressure = 1 bar and temperature = 0, set temperature to 0.01 C (tripple point of water)
  if(pBar >0.99999 && pBar < 1.00001 && Math.abs(tC) < 0.001) {t_C = 0.01;} else {t_C = tC;}
  String str = lib.kemi.H2O.IAPWSF95.isWaterLiquid(t_C, pBar);
  if(str.length() >0) throw new IllegalArgumentException("\"eps(t,p)\": "+str);
  if(t_C >= 0.01 && t_C < CRITICAL_TC) {
    final double pSat;
    try {pSat= lib.kemi.H2O.IAPWSF95.pSat(t_C);}
    catch (Exception ex) {throw new IllegalArgumentException("\"epsJN(t,p)\": "+ex.getMessage());}
    if(pBar < (pSat*0.995)) {throw new IllegalArgumentException("\"epsJN(t,p)\": tC = "+tC+", pBar = "+pBar+" (pBar must be >= "+pSat+")");}
    // if temperature >= 100 C and pressure = pSat (+/-0.5%) use epsSat-function
    // (so, if tC = 25 (or 0.01) and pBar = 1, the full calculation is made)
    if(pBar < (pSat*1.005) && t_C > 99.61) {
        try {return epsSat(t_C);}
        catch (Exception ex) {throw new IllegalArgumentException("\"epsJN(t,p)\": "+ex.getMessage());}
    }
  }
  // Permittivity of free space (C^2 J^-1 m^-1)
  final double eps_0 = 1./(4e-7 * Math.PI * Math.pow(299792458,2));
  // Mean molecular polarizability (C^2 J^-1 m^2)
  final double alpha = 1.636e-40;
  // Molecular dipole moment (C m)
  final double mu = 6.138e-30;
  // Boltzmann's constant (J K^-1)
  final double k = 1.380658e-23;
  // Avogadro's number (mol^-1)
  final double N_A = 6.0221367e23;
  // Molar mass of water (kg/mol)
  final double M_w = 0.018015268;
  // Table 5 in Fernandez et al (1997)
  final double[] N = new double[]{
      0.978224486826,
      -0.957771379375,
      0.237511794148,
      0.714692244396,
      -0.298217036956,
      -0.108853472196,
      0.949327488264e-1,
      -0.980469816509e-2,
      0.165167634970e-4,
      0.937359795772e-4,
      -0.123179218720e-9};
  final double N12 = 0.196096504426e-2;
  // Table 5 in Fernandez et al (1997)
  final double[] i = new double[]{   1,1,  1, 2,  3,  3, 4,5,6, 7, 10};
  final double[] j = new double[]{0.25,1,2.5,1.5,1.5,2.5,2,2,5,0.5,10};
  final double Tc = CRITICAL_T;
  final double T = t_C + T0;
  final double rho_c = 322 / M_w; // mol/m3
  final double rho;
  try {rho = lib.kemi.H2O.IAPWSF95.rho(t_C, pBar) * 1000. // // change to kg/m3
          / M_w; // change to mol/m3
  } catch (Exception ex) {throw new IllegalArgumentException("\"eps(t,p)\": "+ex.getMessage());}
  if(Double.isNaN(rho)) {throw new IllegalArgumentException("\"eps(t,p)\": could not calculate density at tC = "+tC+", pBar = "+pBar);}
  // Eqn. (34) in Fernandez et al (1997)
  double g = 1.;
  double rho_rho_c = rho/rho_c, Tc_T = Tc/T;
  for(int h=0; h < N.length; h++) {
      g = g + N[h] * Math.pow(rho_rho_c,i[h]) * Math.pow(Tc_T,j[h]);
  }
  int h = 0;
  g = g + N12 * (rho_rho_c) * Math.pow(((T/228.)-1.),-1.2);
  // Eqns. (23)+(24) in Fernandez et al (1997)
  final double A = N_A * mu*mu * rho * g / (eps_0 * k * T);
  final double B = N_A * alpha * rho / (3 * eps_0);
  // Eqn. (26) in Fernandez et al (1997)
  double w = 9. + 2.*A + 18.*B + A*A + 10.*A*B + 9.*B*B;
  double eps = (1.+ A + 5.*B + Math.sqrt(w)) / (4. - 4.*B);
  return eps;
}
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="epsJN(tC,pbar)">
/** *   Returns the static permitivity of water (dielectric constant)
 * calculated using the equations reported in:<br>
 * Johnson J W, Norton D (1991) Critical phenomena in hydrothermal systems:
 * state, thermodynamic, electrostatic, and transport properties of H2O in the
 * critical region. American Journal of Science, 291, 541–648.
 * doi: 10.2475/ajs.291.6.541<br>
 * 
 * see also eqns. (36) and (40)-(44) and Table 1 in:
 * Johnson J W, Oelkers E H, Helgeson H C (1992) SUPCRT92: A software package
 * for calculating the standard molal thermodynamic properties of minerals,
 * gases, aqueous species, and reactions from 1 to 5000 bar and 0 to 1000°C.
 * Computers & Geosciences, 18, 899–947. doi:10.1016/0098-3004(92)90029-Q<br>
 *
 * Range of conditions: 1 to 5000 bar, 0 to 1000°C, density >0.05 g/cm3
 * and pressures above the vaporization boundary (if temperature is below
 * the critical point).
 * It throws an exception outside this range.
 * If tC = 0 and pBar = 1, it returns the value at 0.01 Celsius and 1 bar.
 * <p>If the temperature is below the critical point and the pressure
 * corresponds to the vapor saturation (within 0.5%),
 * then epsSat is called.
 * 
 * @param tC the temperature in degrees Celsius
 * @param pBar the pressure in bar
 * @return the dielectric constant of water (unitless)
 * @throws IllegalArgumentException
 * @see lib.kemi.H2O.Dielectric#epsSat(double) epsSat
 * @see lib.kemi.H2O.Dielectric#eps(double, double) eps
 */
public static double epsJN(final double tC, final double pBar) throws IllegalArgumentException {
  if(Double.isNaN(tC)) throw new IllegalArgumentException("\"epsJN(t,p)\": tC = NaN");
  if(tC < 0 || tC > 1000.001) throw new IllegalArgumentException("\"epsJN(t,p)\": tC = "+tC+" (must be >=0 and <= 1000 C)");
  if(Double.isNaN(pBar)) throw new IllegalArgumentException("\"epsJN(t,p)\": pBar = NaN");
  if(pBar > 5000.01 || pBar <= 0.) throw new IllegalArgumentException("\"epsJN(t,p)\": pBar = "+pBar+" (must be >0 and <= 5 kbar)");

  final double t_C;
  // if pressure = 1 bar and temperature = 0, set temperature to 0.01 C (tripple point of water)
  if(pBar >0.99999 && pBar < 1.00001 && Math.abs(tC) < 0.001) {t_C = 0.01;} else {t_C = tC;}
  if(t_C >= 0.01 && t_C < CRITICAL_TC) {
    final double pSat;
    try {pSat= lib.kemi.H2O.IAPWSF95.pSat(t_C);}
    catch (Exception ex) {throw new IllegalArgumentException("\"epsJN(t,p)\": "+ex.getMessage());}
    if(pBar < (pSat*0.995)) {throw new IllegalArgumentException("\"epsJN(t,p)\": tC = "+tC+", pBar = "+pBar+" (pBar must be >= "+pSat+")");}
    // if temperature >= 100 C and pressure = pSat (+/-0.5%) use epsSat-function
    // (so, if tC = 25 (or 0.01) and pBar = 1, the full calculation is made)
    if(pBar < (pSat*1.005) && t_C > 99.61) {
        try {return epsSat(t_C);}
        catch (Exception ex) {throw new IllegalArgumentException("\"epsJN(t,p)\": "+ex.getMessage());}
    }
  }
  final double rho;
  try {rho = lib.kemi.H2O.IAPWSF95.rho(t_C, pBar);}
  catch (Exception ex) {throw new IllegalArgumentException("\"epsJN(t,p)\": "+ex.getMessage());}
  if(Double.isNaN(rho)) {throw new IllegalArgumentException("\"epsJN(t,p)\": could not calculate density at tC = "+tC+", pBar = "+pBar);}

  if( // rho > 1.1 || // see Fig.2 in SUPCRT92 (Jonhson et al 1992) 
         rho < 0.05) {throw new IllegalArgumentException("\"epsJN(t,p)\": calculated density = "+rho+" at tC = "+tC+", pBar = "+pBar+" (must be >0.05 g/cm3)");}
  final double[] a = new double[]{
      0.1470333593E+2,
      0.2128462733E+3,
     -0.1154445173E+3,
      0.1955210915E+2,
     -0.8330347980E+2,
      0.3213240048E+2,
     -0.6694098645E+1,
     -0.3786202045E+2,
      0.6887359646E+2,
     -0.2729401652E+2};
  final double Tr = 298.15;
  final double T = t_C + 273.15;
  final double T_=T/Tr, T_2 = T_*T_;
  final double rho2 = rho*rho;
  final double k1 = a[0]/T_;
  final double k2 = a[1]/T_ + a[2] + a[3]*T_;
  final double k3 = a[4]/T_ + a[5]*T_ + a[6]*T_2;
  final double k4 = a[7]/T_2 + a[8]/T_ + a[9];
  double eps = 1. + k1*rho + k2*rho2 + k3*rho2*rho + k4*rho2*rho2;
  return eps;
}
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="gHKF(tC,pbar)">
/** HKF (Helgeson-Krikham-Flowers) model:
 * g designates a P/T-dependent solvent function that provides for dielectric
 * saturation and the compressibility of the solvent at high temperatures and
 * pressures. With this function the temperature and pressure dependence of the
 * effective electrostatic radii of aqueous ions can be calculated.
 * See eqns.(49) to (52) and Tables 2 and 3 in:<br>
 * 
 * Johnson J W, Oelkers E H, Helgeson H C, (1992) SUPCRT92: A software package
 * for calculating the standard molal thermodynamic properties of minerals,
 * gases, aqueous species, and reactions from 1 to 5000 bar and 0 to 1000°C.
 * Computers & Geosciences, 18, 899–947. doi: 10.1016/0098-3004(92)90029-Q<br>
 * 
 * as well as eqns. (25), (26), (32), (33) and Table 5 in:<br>
 * Shock E L, Oelkers E H, Johnson J W, Sverjensky D A, Helgeson H C, (1992)
 * Calculation of the thermodynamic properties of aqueous species at high
 * pressures and temperatures. Effective electrostatic radii, dissociation
 * constants and standard partial molal properties to 1000°C and 5 kbar.
 * J. Chem. Soc., Faraday Transactions, 88, 803–826. doi:10.1039/FT9928800803<br>
 * Range of conditions: 0 to 5000 bar, 0 to 1000°C, density 0.35 to 1 g/cm3
 * and pressures above the vaporization boundary
 * (if temperature is below the critical point).
 * 
 * @param tC the temperature in degrees Celsius
 * @param pBar the pressure in bar
 * @return the g-function of the HKF model in metres (m)
 * @throws IllegalArgumentException
 */
public static double gHKF(double tC, double pBar) throws IllegalArgumentException {
  if(Double.isNaN(tC)) throw new IllegalArgumentException("\"gHKF(t,p)\": tC = NaN");
  if(Double.isNaN(pBar)) throw new IllegalArgumentException("\"gHKF(t,p)\": pBar = NaN");
  if(tC > 1000 || pBar <= 0 || pBar > 5000) throw new IllegalArgumentException("\"gHKF(t,p)\": tC = "+tC+", pBar = "+pBar+" (must be < 1000 C and zero to 5 kbar)");
//--------
  if(tC <= 100.01) {return 0;}
//--------
  double pSat = Double.NaN;
  if(tC >= 0.01 && tC < CRITICAL_TC) {
    try {pSat= lib.kemi.H2O.IAPWSF95.pSat(tC);}
    catch (Exception ex) {throw new IllegalArgumentException("\"gHKF(t,p)\": "+ex.getMessage());}
    if(pBar < (pSat*0.995)) {throw new IllegalArgumentException("\"gHKF(t,p)\": tC = "+tC+", pBar = "+pBar+" (pBar must be >= "+pSat+")");}
  }
  final double rho;
  try {rho = lib.kemi.H2O.IAPWSF95.rho(tC, pBar);}
  catch (Exception ex) {throw new IllegalArgumentException("\"gHKF(t,p)\": "+ex.getMessage());}
  if(Double.isNaN(rho)) throw new IllegalArgumentException("\"gHKF(t,p)\": could not calculate density at tC = "+tC+", pBar = "+pBar);
  if( //rho < 0.35) { // limit given in Johnson et al (1992)
        rho < 0.15) {
        throw new IllegalArgumentException("\"gHKF(t,p)\": calculated density = "+rho+" at tC = "+tC+", pBar = "+pBar+" (must be >0.15 g/cm3)");
  }
  if(rho > 1.) {return 0;}
// Expressions in Johnson et al (1992)
//   g = a (1 − ρ*)^b − f(T,P)     (eq.49)
//   a = a1 + a2 T + a3 T^2         (eq.50)
//   b = b1 + b2 T + b3 T^2         (eq.51)
//   ρ* = ρ(T,P) / (1 g/cm3)
//   f(T,P) = [ ((T-155)/300)^(4.8)
//           + c1 ((T-155)/300)^16 ]
//         ×[c2 (1000−P)^3 + c3 (1000−P)^4]     (eq.52)
//Table 2 in Johnson et al (1992)
  double a1 = -2.037662,        b1 = 6.107361;
  double a2 = 5.747000E-3,      b2 = -1.074377E-2;
  double a3 = -6.557892E-6,     b3 = 1.268348E-5;
//Table 3 in Johnson et al (1992)
  double c1 = 36.6666,  c2 = -1.504956E-10,  c3 = 5.01799E-14;
  double T = tC, P = pBar;
  double f, w;
  if(T < 155 || T > 355 || P < pSat || P > 1000) {f = 0;}
  else {w = (T-155.)/300.;
      f = (Math.pow(w,4.8) + c1*Math.pow(w,16.))
          * ((c2*Math.pow((1000.-P),3.))+(c3*(Math.pow((1000.-P),4.))));}
  double ag = (a1+a2*T+a3*T*T), bg = (b1+b2*T+b3*T*T);
  return 1.e-10*((ag*(Math.pow((1.-rho),bg)))-f); // Convert Angstrom to metres
}
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="A_gamma(tC,pbar)">
/** Calculates the Debye-Hückel slope as defined in eqn(30) of:<br>
 * Staples, B.R., Nuttall, R.L., 1977.The activity and osmotic coefficients of
 * aqueous calcium chloride at 298.15 K.J. Phys. Chem. Ref. Data vol.6, p.385–407.<br>
 * See also eqn(2-2) in:<br>
 * Hamer, W.J. and Wu, Y.-C., 1972. Osmotic coefficients and mean activity
 * coefficients of uni-univalent electrolytes in water at 25 °C.
 * J. Phys. Chem. Ref. Data, vol.1, p.1047–1099.
 * 
 * @param tC the temperature in degrees Celsius
 * @param rho the density of water in g/cm3
 * @param eps the dielectric constant of water at the given temperature and density (unitless)
 * @return the Debye-Hückel slope in units of (kg/mol)^0.5
 */
static public double A_gamma(final double tC, final double rho, final double eps) {
  if(Double.isNaN(tC) || Double.isNaN(rho) || Double.isNaN(eps)) {return Double.NaN;}
  final double T = tC + T0;
  // The Debye-Hückel slope is:
  // ((1/ln(10))*(2*π*Na*ρ))^0.5
  //    * (e^2 / (4*π*ε0*ε*k*T))^(3/2)
  // where:
  // Na = 6.02214076e23 1/mol (Avogadro's number)
  // ε0 = 8.8541878128e-12 F/m (permittivity of vacuum)
  // e = 1.602176634e-19 C (elementary charge)
  // k = 1.380649e-23 J/K (Boltzmann's constant)
  // ρ = the density of water in g/cm^3
  // ε = the dielectric constant of water
  // T = the temperature in Kelvin
  final double A = 1.8248117e6;
  return A * Math.sqrt(rho) / Math.pow((eps * T),(3./2.));  
}
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="B_gamma(tC,pbar)">
/** Calculates the Debye-Hückel slope as defined by eqn(35) in:<br>
 * Staples, B.R., Nuttall, R.L., 1977. The activity and osmotic coefficients of
 * aqueous calcium chloride at 298.15 K. J. Phys. Chem. Ref. Data, vol.6, p.385–407.<br>
 * See also eqn(2-5) in:<br>
 * Hamer, W.J. and Wu, Y.-C., 1972. Osmotic coefficients and mean activity
 * coefficients of uni-univalent electrolytes in water at 25 °C.
 * J. Phys. Chem. Ref. Data, vol.1, p.1047–1099.
 * 
 * @param tC the temperature in degrees Celsius
 * @param rho the density of water in g/cm3
 * @param eps the dielectric constant of water at the given temperature and density (unitless)
 * @return the Debye-Hückel parameter "B" in units of ((kg/mol)^0.5 * Å^-1)
 */
static public double B_gamma(final double tC, final double rho, final double eps) {
  if(Double.isNaN(tC) || Double.isNaN(rho) || Double.isNaN(eps)) {return Double.NaN;}
  final double T = tC + T0;
  // The Debye-Hückel [B*å] constant is:
  // ((8*π * Na * 1000)^(0.5) * e
  //    / (4*π*ε0*ε*k*T))^(0.5) * (ρ^0.5) * å
  // where:
  // Na = 6.02214076e23 1/mol (Avogadro's number)
  // e = 1.602176634e-19 C (elementary charge)
  // ε0 = 8.8541878128e-12 F/m (permittivity of vacuum)
  // k = 1.380649e-23 J/K (Boltzmann's constant)
  // ρ = the density of water in g/cm^3
  // ε = the dielectric constant of water
  // T = the temperature in Kelvin
  // å = distance parameter in units of metres
  final double B = 5.0290371e1;
  return B * Math.sqrt(rho/(eps * T));  
}
// </editor-fold>

}
