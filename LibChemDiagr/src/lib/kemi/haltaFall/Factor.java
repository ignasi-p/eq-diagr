package lib.kemi.haltaFall;

import lib.common.Util;
import lib.kemi.chem.Chem;
import lib.kemi.readDataLib.ReadDataLib;

/** A class to calculate single ion activity coefficients in aqueous solutions.
 * The method "factor(double[] C, double[] lnf)" is to be called repeatedly by
 * "HaltaFall": it should be as fast as possible. The calculation model is
 * defined in <code>c.diag.activityCoeffsModel</code> when this class is
 * instantiated.
 * Three (3) models are implemented: Davies eqn., SIT, and simplified HKF.<br>
 * The methods of this class may throw exceptions.<br>
 * A value for the ionic strength, "I", is needed as input (stored in
 * <code>chem.Chem.Diagr.ionicStrength</code>):
 * <ul>
 * <li>if I&gt;0 then the activity coefficients are calculated and stored.
 * For models that only depend on ionic strength (Davies and HKF) on subsequent
 * calls to "factor" the activity coefficients are not calculated if the
 * ionic strength has not changed.</li>
 * <li>if I=0 (or if I = NaN, that is, not a number = undefined)
 * then all activity coefficients = 1.</li>
 * <li>if I&lt;0 then the ionic strength is calculated at each time for
 * an electrically neutral aqueous solution.</li>
 * </ul>
 * If an instance of this class, <code>Factor</code>, is constructed without
 * providing a pointer to an instance of <code>chem.Chem.Diagr</code>, then
 * an ideal aqueous solution (I=0) is used.<br>
 * 
 * Copyright (C) 2014-2020  I.Puigdomenech.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 * 
 * @author Ignasi Puigdomenech */
public class Factor {
 /** The osmotic coefficient of water, calculated in this class.
  * @see lib.kemi.chem.Chem.ChemSystem#jWater Chem.ChemSystem.jWater
  * @see lib.kemi.haltaFall.Factor#log10aH2O haltaFall.Factor.log10aH2O */
  public double osmoticCoeff = Double.NaN;
 /** The log10 of the activity of water, calculated in this class.
  * @see lib.kemi.chem.Chem.ChemSystem#jWater Chem.ChemSystem.jWater
  * @see lib.kemi.haltaFall.Factor#osmoticCoeff haltaFall.Factor.osmoticCoeff */
  public double log10aH2O = Double.NaN;
 /** The electrical balance of the aqueous solutions:
  * the sum of cationic charges plus sum of anionic charges. Calculated in
  * <code>Factor</code> from the concns. and the charge of each species. */
  public double electricBalance = Double.NaN;
  /** Debye-Hückel parameter */
  public double Agamma = Double.NaN;
  /** the extended Debye-Hückel parameter in the HKF model */
  public double bgi = 0;
  /** Debye-Hückel parameter including the size parameter (=Bgamma x å) */
  public double rB = 0;

//<editor-fold defaultstate="collapsed" desc="private fields">
  // ------------------------
  // ---- private data:  ----
  /** the "first time" some checks are made, and it is decided which species
   * require activity coefficients, for example, gases are excluded */
  private boolean begin = true;
  /** If any of the SIT files contains as a first line "NoDefaults" then
   * this variable will be <code>false</code> and default values for
   * SIT coefficients will be zero. Otherwise it will be <code>true</code>
   * and non-zero default values will be used for the SIT coefficients */
  private boolean setDefaultValues = true;
  /** true if a reference to a chem.Chem instance is not available */
  private boolean dataNotSupplied = true;
  /** where messages will be printed. It may be "System.out" */
  private java.io.PrintStream out;
  private Chem c;
  private Chem.ChemSystem cs;
  /** Object of a class with input temperature and ionic strength */
  private Chem.Diagr diag;
  /** Object of a class containing the array "z" with electric charges */
  private Chem.ChemSystem.NamesEtc namn;
  private SITeps eps; // class with ion interaction coefficients
  /** directories to search for the SIT-file */
  private String[] pathToSITdataFile = new String[3];
  private final String SIT_FILE = "SIT-coefficients.dta";
  /** nIon = Na + Nx (nbr of "ions" = number of components
   *            + number of soluble products) */
  private int nIon = -1;
  /** For each species: true if this species is either gas, liquid or solid, or
   * if noll[] = true. Then the activity coefficient does not need to be
   * calculated. False otherwise. */
  private boolean[] gas;
  /** the temperature given by the user the last time this procedure was executed
   * @see lib.kemi.chem.Chem.Diagr#temperature Chem.Diagr.temperature */
  private double lastTemperature = Float.MAX_VALUE;
  /** the pressure given by the user the last time this procedure was executed
   * @see lib.kemi.chem.Chem.Diagr#pressure Chem.Diagr.pressure */
  private double lastPressure = Float.MAX_VALUE;
  /** the ionic strength given by the user the last time this procedure was executed
   * @see lib.kemi.haltaFall.Factor#ionicStr haltaFall.Factor.ionicStr
   * @see lib.kemi.chem.Chem.Diagr#ionicStrength Chem.Diagr.ionicStrength */
  private double lastIonicStr = 0;
  /** The ionic strength, either provided by the user
   * in <code>diag.ionicStrength</code>, or calculated
   * (if <code>diag.ionicStrength</code> is negative).
   * @see lib.kemi.chem.Chem.Diagr#ionicStrength Chem.Diagr.ionicStrength */
  public double ionicStr = Double.NaN;
  /** the square root of the ionic strength */
  private double rootI = 0;
  /** The dielectric constant of water */
  private double eps_H2O = Double.NaN;
  /** The density of water in units of g/cm3 */
  private double rho = Double.NaN;
  /** Debye-Hückel parameter */
  private double Bgamma = Double.NaN;
  /** the g-function in the HKF model, in units of Å */
  private double g_function = 0;
  /** the coefficient in Davies eqn.  */
  private final double DAVIES = 0.3;
  /** sum of molalities of all species in the aqueous solution */
  private double sumM;
  private double sumPrd;
  private double phiDH;
  /** = 55.508 = 1000/(15.9994+2*1.00794) */
  private static final double molH2Oin1kg = 1000/(15.9994+2*1.00794);
  /** ln(10)= 2.302585092994046 */
  private static final double ln10 = 2.302585092994046;
  /** The cut-off abs value for a log10 of the activity coefficients: MAX_LOG_G = 3 */
  private static final double MAX_LOG_G = 3; // = 6.90776 in ln-units
  /** The cut-off abs value for log10aH2O:  MAX_LOGAH2O = 3.999 */
  private static final double MAX_LOGAH2O = 3.999; // = 9.21 in ln-units
  /** The cut-off concentration when calculating the ionic strength etc
   * to avoid "unreasonable" results: MAX_CONC = 20 */
  public static final double MAX_CONC = 20;
  private static final String dashLine="- - - - - - - - - - - - - - - - - - -";
  private static final String nl = System.getProperty("line.separator");
  private static final java.util.Locale engl = java.util.Locale.ENGLISH;
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="constructors">
/** A class to calculate single ion activity coefficients in aqueous solutions.
 * Without arguments all activity coefficients are = 1.
 * That is, the calculations will be done for an ideal aqueous solution */
public Factor() {out = System.out; dataNotSupplied = true;}

/** A class to calculate single ion activity coefficients in aqueous solutions.
 * Activity coefficients will be calculated using the information stored in
 * the <code>chem.Chem</code> instance.
 * The calculation model is defined in: c.diag.activityCoeffsModel.
 * @param c pointer to an object of storing the chemical data
 * @throws IllegalArgumentException
 * @see lib.kemi.chem.Chem.Diagr#activityCoeffsModel
 *                                          Chem.Diagr.activityCoeffsModel */
public Factor(Chem c) throws IllegalArgumentException {
    out = System.out;
    try{beginFactor(c, null, null, null);}
    catch (Exception ex) {throw new IllegalArgumentException(ex.getMessage());}
}

/** A class to calculate single ion activity coefficients in aqueous solutions.
 * Activity coefficients will be calculated using the information stored in
 * the <code>chem.Chem</code> instance.
 * The calculation model is defined in: c.diag.activityCoeffsModel.
 * <br>
 * <code>Path1</code>, <code>path2</code> and <code>path3</code> are
 * directories where the SIT-file will be searched. The intention
 * is that <code>path1</code> should be set to the path where the
 * application is located, <code>path2</code> set to the user directory
 * and <code>path3</code> set to the "current" directory. If the SIT-file
 * is found in two or more of these paths, then the data in
 * <code>path3</code> supersedes that of <code>path2</code>, etc.
 * @param c pointer to an object storing the chemical data
 * @param path1 a directory where a copy of the SIT-file is found.
 * It may be null.
 * @param path2 a directory where a copy of the SIT-file is found.
 * It may be null.
 * @param path3 a directory where a copy of the SIT-file is found.
 * It may be null.
 * @param out0 where messages will be printed. It may be
 * <code>System.out</code>. If null, <code>System.out</code> is used
 * @throws IllegalArgumentException
 * @see lib.kemi.chem.Chem.Diagr#activityCoeffsModel
 *                                      Chem.Diagr.activityCoeffsModel */
public Factor(Chem c,
              String path1, String path2, String path3,
              java.io.PrintStream out0) throws IllegalArgumentException {
    if(out0 != null) {this.out = out0;} else {this.out = System.out;}
    try{beginFactor(c, path1, path2, path3);}
    catch (Exception ex) {throw new IllegalArgumentException(ex.getMessage());}
}

//<editor-fold defaultstate="collapsed" desc="private beginFactor">
private void beginFactor(Chem c, String path1, String path2, String path3)
        throws IllegalArgumentException {
  if(c != null) {
    dataNotSupplied = false;
    // get the chemical system and concentrations
    this.c = c;
    //-----------------------------------------------------------
    // factor uses the variables:
    //     cs.Na, cs.Nx, cs.jWater, cs.noll
    // and when printing activity coefficients:
    //     cs.chemConcs.tolLogF
    //     cs.chemConcs.logA[]
    //     cs.chemConcs.logf[]
    //     cs.chemConcs.C[]
    this.cs = c.chemSystem;
    nIon = cs.Na + cs.nx;
    gas = new boolean[nIon];
    //-----------------------------------------------------------
    // factor uses the variables:  namn.z, namn.ident
    this.namn = cs.namn;
    //-----------------------------------------------------------
    // factor uses the variables:
    //   diag.activityCoeffsModel, diag.ionicStrength,
    //   diag.tempererature, diag.pressure
    this.diag = c.diag;
    lastIonicStr = 0;
    try{checks_and_start();}
    catch (Exception ex) {
      diag.activityCoeffsModel = -1;
      diag.ionicStrength = Double.NaN; diag.ionicStrCalc = Double.NaN;
      diag.phi = Double.NaN;           diag.sumM = Double.NaN;
      throw new IllegalArgumentException(ex.getMessage());
    }
  }
  pathToSITdataFile[0] = path1;
  pathToSITdataFile[1] = path2;
  pathToSITdataFile[2] = path3;
}
/** Makes a few checks and calculates rho, eps_H2O, Agamma and g_function.
 * @throws IllegalArgumentException
 * @throws ArithmeticException  */
private void checks_and_start() throws IllegalArgumentException, ArithmeticException {
    if(dataNotSupplied || diag.activityCoeffsModel < 0) {
        diag.activityCoeffsModel = -1;
        diag.ionicStrength = Double.NaN; diag.ionicStrCalc = Double.NaN;
        diag.phi = Double.NaN;           diag.sumM = Double.NaN;
        return;
    }
    //--- make some checks
    if(nIon <=0) {
        throw new IllegalArgumentException("\"haltaFall.Factor\": nIon="+nIon+" (must be >0)");
    }
    if(namn.z.length < nIon || namn.ident.length < nIon || cs.noll.length < nIon) {
        throw new IllegalArgumentException(
                "\"haltaFall.Factor\": z.length="+namn.z.length+" noll.length="+cs.noll.length+", both must be >="+nIon+" (nIon)");
    }
    // --- Find out which species need activity coeffs.
    //     those with gas[] = false
    //     gas[] = true if noll[]=true
    //     or if the name ends with either "(g)" or "(l)"
    for(int i = 0; i < nIon; i++) {
        gas[i] = false;
        if(cs.noll[i]) {gas[i] = true; continue;}
        if(namn.z[i] != 0) {continue;}
        if(isGasOrLiquid(namn.ident[i])) {gas[i]=true;}
    }
    lastTemperature = diag.temperature;
    lastPressure = diag.pressure;
    try{rho = lib.kemi.H2O.IAPWSF95.rho(diag.temperature, diag.pressure);}
    catch (Exception ex) {
        throw new ArithmeticException("\"haltaFall.Factor\": "+ex.getMessage());
    }
    try{eps_H2O = lib.kemi.H2O.Dielectric.epsJN(diag.temperature, diag.pressure);}
    catch (Exception ex) {eps_H2O = Double.NaN;}
    if(Double.isNaN(eps_H2O)) {
        try{eps_H2O = lib.kemi.H2O.Dielectric.eps(diag.temperature, diag.pressure);}
        catch (Exception ex) {
            throw new IllegalArgumentException("\"haltaFall.Factor\": "+ex.getMessage());
        }
    }
    Agamma = lib.kemi.H2O.Dielectric.A_gamma(diag.temperature, rho, eps_H2O);
    if(diag.activityCoeffsModel == 2) { // HKF model
        try{g_function = lib.kemi.H2O.Dielectric.gHKF(diag.temperature,diag.pressure)*1.e+10;} // convert to Å
        catch (Exception ex) {
            throw new IllegalArgumentException("\"haltaFall.Factor\": "+ex.getMessage());
        }
    }
  }
// </editor-fold>

// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="factor">
/** Calculates the natural log of the activity coefficients for a given
 * chemical system. This method is called repeatedly by class "HaltaFall":
 * it should be as fast as possible. The calculation model is defined in
 * <code>c.diag.activityCoeffsModel</code> when this class is instantiated.
 * If c.diag.ionicStrength is &lt; 0, then the ionic strength
 * is calculated. <b>It is expected that the length of the arrays will not
 * be changed between iterations.</b>
 * @param C array with the concentration of each species (ions)
 * @param lnf array where calculated values of the natural logs of the
 * individual ion activity coefficients will be stored
 * @see lib.kemi.chem.Chem.Diagr#activityCoeffsModel
 *                                              Chem.Diagr.activityCoeffsModel
 * @throws IllegalArgumentException
 * @throws lib.kemi.haltaFall.Factor.SITdataException */
public void factor(double[] C, double[] lnf)
        throws IllegalArgumentException, ArithmeticException, SITdataException {
  boolean tChanged, iChanged;
  //-----------------------------------------------------------
  //  Define upper and lower bounds on the concentrations if concentrations far
  //  from the equilibrium values can cause exponents out of the range allowed
  //-----------------------------------------------------------

  //-----------------------------------------------------------
  // --- make some tests about the parameters (arguments)
  if(begin) { // the first time this procedure is run...
      begin = false;
      int nlnf = lnf.length;
      if(nlnf <=0) {
          diag.activityCoeffsModel = -1;
          diag.ionicStrength = Double.NaN; diag.ionicStrCalc = Double.NaN;
          diag.phi = Double.NaN;           diag.sumM = Double.NaN;
          throw new IllegalArgumentException("\"haltaFall.Factor\": lnf.length must be > 0");
      }
      if(C.length <=0) {
          diag.activityCoeffsModel = -1;
          diag.ionicStrength = Double.NaN; diag.ionicStrCalc = Double.NaN;
          diag.phi = Double.NaN;           diag.sumM = Double.NaN;
          throw new IllegalArgumentException("\"haltaFall.Factor\": C.length="+C.length+" (must be >0)");
      }
      if(nlnf < nIon || C.length < nIon) {
          diag.activityCoeffsModel = -1;
          diag.ionicStrength = Double.NaN; diag.ionicStrCalc = Double.NaN;
          diag.phi = Double.NaN;           diag.sumM = Double.NaN;
          throw new IllegalArgumentException( "\"haltaFall.Factor\": "+
               "lnf.length="+nlnf+", C.length="+C.length+", must be >= "+nIon+" (= Na + Nx = "+cs.Na+" + "+cs.nx+")");
      }
  } //if begin
  //-----------------------------------------------------------

  // --- ideal solution
  if(dataNotSupplied || diag.activityCoeffsModel < 0) {
        diag.ionicStrength = Double.NaN; diag.ionicStrCalc = Double.NaN;
        diag.phi = Double.NaN;           diag.sumM = Double.NaN;
        for(int i = 0; i < lnf.length; i++) {lnf[i] = 0;}
        lastIonicStr = 0;
        return;
  }

  //-----------------------------------------------------------
  // To speed-up things, if the ionic strength and temperature-pressure do
  // not change, and the model to calculate activity coefficients depends
  // only on the ionic strength and (T,P) (i.e. Davies and HKF), then the
  // activity coeffs are not recalculated.
  //-----------------------------------------------------------
  // Has the user changed the ionic strength for the calculation?
  // (or is this the first time?)
  if(Double.isNaN(diag.ionicStrength)) {iChanged = true; ionicStr = 0; sumM =0; electricBalance =0;}
  else if(diag.ionicStrength ==0) {iChanged = (lastIonicStr ==0); ionicStr = 0;  sumM =0; electricBalance =0;}
  else if(diag.ionicStrength >0) {iChanged = (Math.abs(1-(lastIonicStr/diag.ionicStrength)) > 1e-3); ionicStr = Math.min(diag.ionicStrength,200);}
  else {iChanged = true; ionicStr = -1;} // diag.ionicStrength < 0

  tChanged = (Math.abs(lastTemperature - diag.temperature) > 0.1 ||
          (Math.abs((lastPressure - diag.pressure)/diag.pressure)) > 0.001);
  // find the temperature-dependent values of A-gamma
  if(tChanged) {
    lastTemperature = diag.temperature;
    lastPressure = diag.pressure;
    try{rho = lib.kemi.H2O.IAPWSF95.rho(diag.temperature, diag.pressure);}
    catch (Exception ex) {
        diag.activityCoeffsModel = -1;
        diag.ionicStrength = Double.NaN; diag.ionicStrCalc = Double.NaN;
        diag.phi = Double.NaN;           diag.sumM = Double.NaN;
        throw new ArithmeticException("\"haltaFall.Factor\": "+ex.getMessage());
    }
    try{eps_H2O = lib.kemi.H2O.Dielectric.epsJN(diag.temperature, diag.pressure);}
    catch (Exception ex) {eps_H2O = Double.NaN;}
    if(Double.isNaN(eps_H2O)) {
        try{eps_H2O = lib.kemi.H2O.Dielectric.eps(diag.temperature, diag.pressure);}
        catch (Exception ex) {
            diag.activityCoeffsModel = -1;
            diag.ionicStrength = Double.NaN; diag.ionicStrCalc = Double.NaN;
            diag.phi = Double.NaN;           diag.sumM = Double.NaN;
            throw new IllegalArgumentException("\"haltaFall.Factor\": "+ex.getMessage());
        }
    }
    Agamma = lib.kemi.H2O.Dielectric.A_gamma(diag.temperature, rho, eps_H2O);
  }

  // calculate: ionicStrength, electricBalance and sumM.
  if(ionicStr < 0) {ionicStr = calcIonicStr(C,namn.z);}

  // ionicStr should now be >=0
  rootI = 0;
  if(ionicStr > 0) {rootI = Math.sqrt(ionicStr);}

  // ---- Calculate activity coefficients
  if(diag.activityCoeffsModel == 0) { //Davies
      if(tChanged || iChanged) {
            try{calcDavies(lnf, namn.z);}
            catch (Exception ex) {
                diag.activityCoeffsModel = -1;
                diag.ionicStrength = Double.NaN; diag.ionicStrCalc = Double.NaN;
                diag.phi = Double.NaN;           diag.sumM = Double.NaN;
                throw new IllegalArgumentException("\"haltaFall.Factor\": "+ex.getMessage());
            }
      }
  } //Davies

  if(diag.activityCoeffsModel == 1) { //SIT
      if(tChanged) {
          Bgamma = lib.kemi.H2O.Dielectric.B_gamma(diag.temperature, rho, eps_H2O);
          // this gives 1.5 at 25°C
          rB = 4.56899 * Bgamma;
      }
      // With the SIT the activity coefficients may change even if the
      // ionic strength does not change: they depend on the composition.
      // Hence, the values of lnf[] will NOT be calculated correctly
      // in "calcSIT" unless the values of C[] are correct!
      try{calcSIT(C, lnf, namn.z);}
      catch (Exception ex) {
        diag.activityCoeffsModel = -1;
        diag.ionicStrength = Double.NaN; diag.ionicStrCalc = Double.NaN;
        diag.phi = Double.NaN;           diag.sumM = Double.NaN;
        throw new IllegalArgumentException("\"haltaFall.Factor\": "+ex.getMessage());
      }
  } //SIT

  if(diag.activityCoeffsModel == 2) { //HKF
      if(tChanged || iChanged) {
          if(tChanged) {
            g_function = 0;
            try{
              // use distance of closest approach for NaCl (HKF-4, Table 3)
              // Shock et al., 1992 J. Chem. Soc., Faraday Trans., 88, 803–826. doi: 10.1039/FT9928800803
              // Table 9(b) r_e(Cl-)=1.81, Table 9(a) r_e(Na+)=1.91 (=0.97+0.94)
              // Eqs.(3)-(5) where kz = 0.94 for cations and zero for anions.
              g_function = lib.kemi.H2O.Dielectric.gHKF(diag.temperature, diag.pressure)*1.e+10; // convert to Å
              bgi = b_gamma_NaCl(diag.temperature, diag.pressure, eps_H2O, g_function);
            } catch (Exception ex) {
                diag.activityCoeffsModel = -1;
                diag.ionicStrength = Double.NaN; diag.ionicStrCalc = Double.NaN;
                diag.phi = Double.NaN;           diag.sumM = Double.NaN;
                throw new IllegalArgumentException("\"haltaFall.Factor\": "+ex.getMessage());
            }
            rB = lib.kemi.H2O.Dielectric.B_gamma(diag.temperature, rho, eps_H2O)
                                                * ( (1.81+g_function) + (0.97 + (0.94+g_function)) );
          }
          calcHKF(lnf, namn.z);
      }
  } //HKF

  lastIonicStr = ionicStr;
  diag.ionicStrCalc = ionicStr;

  //return;

} // factor
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="calcDavies">
private void calcDavies(double[] lnf, int[] z) {
  if(ionicStr <=0) {return;}
  // the number "1" assumed to be temperature independent
  double w = - Agamma *((rootI/(1. + rootI)) -(DAVIES * ionicStr));
  double logf; int zz;
  for(int i = 0; i < lnf.length; i++) {
    if(!gas[i]) {
        if(z[i] != 0) {
            zz = z[i]*z[i];
            logf =  zz * w;
            logf = Math.max(-MAX_LOG_G*zz,Math.min(logf,MAX_LOG_G*zz));
            lnf[i] = ln10 * logf;
        } else {lnf[i] = 0;}
        //lg_ACF(I)=lg_ACF(I) + LNW
    } else {lnf[i] = 0;}
  }

  if(cs.jWater < 0) {return;}
  // Debye-Huckel term for phi
  phiDH = ((2d*ln10)/3d) * Agamma * ionicStr * rootI * sigma(rootI);
  osmoticCoeff = 1;
  if(sumM > 1e-15) {
      osmoticCoeff =
              1 - (phiDH/sumM)
                + (ln10 * Agamma * DAVIES * Math.pow(ionicStr,2))/sumM;
  }
  // Calculate Water Activity
  log10aH2O =
        Math.max(-MAX_LOGAH2O,
            Math.min(MAX_LOGAH2O,
                    (-osmoticCoeff * sumM / (ln10* molH2Oin1kg)) ) );

  lnf[cs.jWater] = ln10 * log10aH2O;
  diag.phi = osmoticCoeff;
  diag.sumM = sumM;
} //calcDavies
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="calcHKF">
/** <pre>Calculation of activity coefficients with:
 *   log f(i) = - (Aγ(T) Z(i)² √I)/(1 + r B(T) √I) + Γ + (bγ(T) I)
 * where: Γ = -log(1+0.0180153 Σm).</pre>
 * These eqns. are an approximation to the HKF model
 * (Helgeson, Kirkham and Flowers),
 * see Eqs.121,165-167,297,298 in: H.C.Helgeson, D.H.Kirkham and G.C.Flowers,
 * Amer.Jour.Sci., 281 (1981) 1249-1516,  Eqs.22 & 23 in: E.H.Oelkers and
 * H.C.Helgeson, Geochim.Cosmochim.Acta, 54 (1990) 727-738, etc.
 * @param lnf
 * @param z
 */
private void calcHKF(double[] lnf, int[] z) {
  if(ionicStr <= 0) {return;}
  // note that "sumM" is not used in this verions of HKF
  double w = -Agamma * (rootI/(1+(rB*rootI)));
  double gamma = Math.log10(1+(0.0180153*ionicStr)); // sumM));
  double logf; int zz;
  for(int i = 0; i < lnf.length; i++) {
    if(!gas[i]) {
      logf = 0;
      if(z[i] != 0) {
        zz = z[i]*z[i];
        logf = zz * w - gamma + bgi*ionicStr;
        logf = Math.max(-MAX_LOG_G*zz,Math.min(logf,MAX_LOG_G*zz));
      } //if z != 0
      lnf[i] = ln10 * logf;
    } else { lnf[i] = 0; }
  }

  if(cs.jWater < 0) {return;}
  // Debye-Huckel term for phi
  phiDH = ((2d*ln10)/3d) * Agamma * ionicStr * rootI * sigma(rB*rootI);
  osmoticCoeff = 1;
  if(ionicStr > 1e-15) { // sumM
      osmoticCoeff =
                (ln10 * gamma / (0.0180153*ionicStr)) // sumM))
                - (phiDH/ionicStr) // sumM)
                + (ln10 * bgi * 0.5 * ionicStr);
  }
  // Calculate Water Activity
  log10aH2O =
        Math.max(-MAX_LOGAH2O,
            Math.min(MAX_LOGAH2O, (-osmoticCoeff * ionicStr // sumM
                    / (ln10* molH2Oin1kg)) ) );

  lnf[cs.jWater] = ln10 * log10aH2O;
  diag.phi = osmoticCoeff;
  diag.sumM = sumM;
} //calcHKF
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="SIT">

//<editor-fold defaultstate="collapsed" desc="calcSIT">
/** Calculate single-ion activity coefficients using the SIT (Specific Ion
 * Interaction model)
 * @param C concentration of each species
 * @param lnf the calculated natural logarithm of the
 * single ion activity coefficient
 * @param z the charge of each aqueous species
 * @throws lib.kemi.haltaFall.Factor.SITdataException */
private void calcSIT(double[] C, double[] lnf, int[] z) throws SITdataException {
  // --- Check if there is a need to get "epsilon" values
  try {readSITdataFiles();}
  catch (Exception ex) {
    throw new SITdataException("\"haltaFall.Factor.calcSIT\": "+nl+ex.getMessage());
  }

  if(ionicStr <= 0) {return;}
  double elBal = Math.abs(electricBalance);
  float ε; //epsilon
  double DH = -Agamma * rootI/(1. + (rB * rootI));
  double sumEpsM, Ci, logf;
  int zz;
  // --- Calculate the individual ionic activity coefficients
  // For neutral species this program uses ε(i,MX)*[M] + ε(i,MX)*[X]
  // As a consequence:
  //   for a MX electrolyte: ε(i,M)*[MX] + ε(i,X)*[MX]
  //   for a M2X (or MX2) electrolyte: ε(i,M)*2*[M2X] + ε(i,X)*[M2X]
  //   (or ε(i,M)*[MX2] + ε(i,X)*2*[MX2])
  // In the SIT-file you must enter ε = ε(i,MX)/2 (or ε = ε(i,M2X)/3)
  for(int i = 0; i < nIon; i++) {
    if(gas[i]) {lnf[i] = 0; continue;}
    sumEpsM = 0;
    if(z[i] ==0) {
        ε = getEpsilon(i,i);
        Ci = Math.max(0, Math.min(MAX_CONC, C[i]));
        sumEpsM = ε * Ci;
    } else if(elBal > 1e-10) {
        ε = 0;
        if(electricBalance < -1e-10) {ε = getEpsilon(nIon,i);}
        else if(electricBalance > -1e-10) {ε = getEpsilon(nIon+1,i);}
        sumEpsM = sumEpsM + ε * Math.max(0, Math.min(MAX_CONC, elBal));
    }
    for(int j=0; j<nIon; j++) {
        if(j==i || z[i]*z[j] >0) {continue;}
        ε = getEpsilon(i,j);
        Ci = Math.max(0, Math.min(MAX_CONC, C[j]));
        sumEpsM = sumEpsM + ε * Ci;
    } //for j
    zz = z[i]*z[i];
    logf = zz*DH + sumEpsM;
    // lg_ACF(I) = lg_ACF(I) + LNW
    logf = Math.max(-MAX_LOG_G*zz, Math.min(logf,MAX_LOG_G*zz));
    lnf[i] = ln10 * logf;
  } //for i =0 to (nIon-1)

  // --- Calculate Osmotic Coeff.
  if(cs.jWater < 0) {return;}

  osmoticCoeff = 1;
  // Debye-Huckel term for phi
  phiDH = ((2d*ln10)/3d) * Agamma * ionicStr*rootI * sigma(rB*rootI);
  //Calculte the sum of ions and the sum of products of conc. times epsilon
  double Cj;
  sumPrd = 0;
  double sumPrd_i;
  // loop through cations and neutral species
  for(int i = 0; i < nIon; i++) {
    if(gas[i]) {continue;}
    Ci = Math.max(0, Math.min(MAX_CONC, C[i]));
    sumPrd_i = 0;
    if(z[i] < 0) {continue;} // skip anions
    // --- neutral species ---
    if(z[i] == 0) {
        for(int j = 0; j < nIon; j++) {
          Cj = Math.max(0, Math.min(MAX_CONC, C[j]));
          if(z[j]==0) {
            ε = getEpsilon(i,i);
            sumPrd_i = sumPrd_i + (ε * Ci * Cj)/2;
            continue;
          }
          ε = getEpsilon(i,j);
          sumPrd_i = sumPrd_i + ε * Ci * Cj;
        } //for j=0 to (nIon-1)
        if(elBal > 1e-10) {
          ε = getEpsilon(i,nIon); //interaction with Na+ (elec.balance)
          sumPrd_i = sumPrd_i + ε * Ci * elBal;
        }
    } else {
    // --- cations ---
        for(int j = 0; j < nIon; j++) {
          if(i==j) {continue;}
          if(z[j] < 0) {
            Cj = Math.max(0, Math.min(MAX_CONC, C[j]));
            ε = getEpsilon(i,j);
            sumPrd_i = sumPrd_i + ε * Ci * Cj;
          }
        } //for j=0 to (nIon-1)
        if(electricBalance > 1e-10) {
          ε = getEpsilon(i,nIon+1); //interaction with Cl- (elec.balance)
          sumPrd_i = sumPrd_i + ε * Ci * electricBalance;
        }
      } // --- cations
    sumPrd = sumPrd + sumPrd_i;
  } //for i=0 to (nIon-1)

  // the remaining cation is Na+ "added" for electic balance
  if(electricBalance < -1e-10) {
    for(int i = 0; i < nIon; i++) {
      if(z[i] >= 0 || gas[i]) {continue;}
      ε = getEpsilon(i,nIon);
      Ci = Math.max(0, Math.min(MAX_CONC, C[i]));
      sumPrd = sumPrd - ε * electricBalance * Ci;
    } //for i=0 to (nIon-1)
  }

  if(sumM > 1e-15) {
      osmoticCoeff = 1 - (phiDH/sumM) + (ln10 * sumPrd)/sumM;
  }
  // Calculate Water Activity
  log10aH2O =
        Math.max(-MAX_LOGAH2O,
            Math.min(MAX_LOGAH2O,
                    (-osmoticCoeff * sumM / (ln10* molH2Oin1kg)) ) );

  lnf[cs.jWater] = ln10 * log10aH2O;
  diag.phi = osmoticCoeff;
  diag.sumM = sumM;
}
private double sigma(double x) {
  if(x<0) {return Double.NaN;}
  else if(x<1e-10) {return 1;}
  else {return (3/Math.pow(x, 3)) * ((1+x) - 2*Math.log(1+x) - (1/(1+x)));}
} //sigma
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="readSITdata">

//<editor-fold defaultstate="collapsed" desc="readSITdataFiles">
/** The SIT-file(s) are read for user supplied epsilon values
 * (SIT coefficients). Then default values for SIT ion interaction
 * coefficients are set for empty (non-user supplied) values.
 * @throws lib.kemi.haltaFall.Factor.SITdataException */
private void readSITdataFiles() throws SITdataException {
  // create the arrays
  if(eps == null) {
      try{eps = new SITeps(nIon);}
      catch (Exception ex) {
          throw new SITdataException("Error in \"haltaFall.Factor.readSITdataFiles\" "+nl+ex.getMessage());
      }
  }
  // --- If the arrays in c.eps are already initialised, skip the rest
  if(!Float.isNaN(eps.eps0[0][0])) {return;}

  String line = "- - - - - - - - -";
  // --- Read the input files and
  //     get user-supplied specific ion interaction coefficients
  // set all coefficients to "empty" values
  for(int i = 0; i < nIon+2; i++) {
      for(int j=0; j<(i+1); j++) {setEpsilon(i,j, Float.NaN, 0f, 0f);}
  }
  boolean someFile = false;
  for(String pathToSITdataFile1 : pathToSITdataFile) {
      if (pathToSITdataFile1 == null) {continue;}
      // --- prepare the file for reading:
      java.io.File pathToSITfile = new java.io.File(pathToSITdataFile1);
      if(!pathToSITfile.exists() || !pathToSITfile.isDirectory()) {
          out.println(nl+line+nl+"Error in \"haltaFall.Factor.readSITdataFile\":"+nl+
                      "   path = \""+pathToSITfile.getPath()+"\""+nl+
                      "   either does not exist or it is not a directory."+nl+line);
          continue; // try to read next path
      }
      java.io.File SITdata = new java.io.File(pathToSITdataFile1
                                        + java.io.File.separator + SIT_FILE);
      // create a ReadDataLib instance
      ReadDataLib rd;
      try {rd = new ReadDataLib(SITdata);}
      catch (ReadDataLib.DataFileException ex) {
          out.println("Note - SIT file NOT found: \""+SITdata+"\"");
          continue; // try to read next path: read next SIT-file if any
      }
      someFile = true;
      out.println("Reading SIT data from \""+SITdata+"\"");
      // --- Search the SIT-file
      //    (compare names such as "Fe+2" and "Fe 2+")
      try {readSITfileSection(1,rd);}
      catch (ReadDataLib.DataEofException | ReadDataLib.DataReadException ex) {throw new SITdataException(ex.getMessage());}
      catch (SITdataException ex) {
          throw new SITdataException(ex.getMessage()+nl+"reading file \""+SITdata+"\".");
      }
      // --- Search the SIT-file for epsilon-values for neutral species with MX
      //    (compare names with and without "(aq)", etc)
      try {readSITfileSection(2,rd);}
      catch (ReadDataLib.DataEofException | ReadDataLib.DataReadException ex) {throw new SITdataException(ex.getMessage());}
      catch (SITdataException ex) {
          throw new SITdataException(ex.getMessage()+nl+"reading file \""+SITdata+"\".");
      }
      // --- Search the SIT-file for epsilon-values for neutral species with
      //     themselves or other neutral species
      //    (compare names with and without "(aq)", etc)
      try {readSITfileSection(3,rd);}
      catch (ReadDataLib.DataEofException | ReadDataLib.DataReadException ex) {throw new SITdataException(ex.getMessage());}
      catch (SITdataException ex) {
          throw new SITdataException(ex.getMessage()+nl+"reading file \""+SITdata+"\".");
      }
      // --- The End
      try{rd.close();}
      catch (Exception ex) {}
  } //for i=0 to <pathToSITdataFile.length
  if(!someFile) {
      throw new SITdataException("SIT model for activity coefficients requested,"+nl+
              " but no SIT data file found.");
  } else {out.println("Finished reading SIT data.");}

  // --- Set Default values for the specific ion interaction coefficients:
  //     Because epsilon values are symmetric (eps(M,X) = eps(X,M)) only
  //     half of the values need to be assigned
  if(!setDefaultValues) {
    out.println("Setting \"empty\" SIT-coefficients to zero"
               +" (default SIT-coefficients NOT used).");
    // Set all "empty" coefficients to zero
    for(int i = 0; i < nIon+2; i++) {
      for(int j=0; j<=i; j++) {
          if(Float.isNaN(getEpsilon(i,j))) {setEpsilon(i,j, 0f, 0f, 0f);}
      }
    }
    return;
  }
  out.println("Setting \"empty\" SIT-coefficients to default values.");
  float w; int k;
  //Set the default values according to Hummel (2009) for interactions with
  //    ClO4, Cl- and Na+
  //For eps(Na+,Cl-) and eps(Na+,ClO4-) the default values for Cl- and ClO4-
  // are used (instead of the default for Na+)
  final String identClO4 = "ClO4";
  final String identCl = "Cl";
  final String identNa = "Na";
  String species1, species2;
  boolean ClO41, Cl1, Na1, ClO42, Cl2, Na2;
  for(int i = 0; i < nIon+2; i++) {
      if(i < nIon && gas[i]) {continue;}
      if(i<nIon) {
        species1 = Util.nameOf(namn.ident[i]);
        if(species1.equals(identClO4) && namn.z[i] == -1) {
            ClO41 = true; Cl1 = false; Na1 = false;
        }
        else if(species1.equals(identCl) && namn.z[i] == -1) {
            ClO41 = false; Cl1 = true; Na1 = false;
        }
        else if(species1.equals(identNa) && namn.z[i] == +1) {
            ClO41 = false; Cl1 = false; Na1 = true;
        }
        else {ClO41= false; Cl1 = false; Na1 = false;}
      }
      else if(i==nIon) {
          //species1 = "Na";
          ClO41 = false; Cl1 = false; Na1 = true;
      }
      else if(i==nIon+1) {
          //species1 = "Cl";
          ClO41 = false; Cl1 = true; Na1 = false;
      }
      else {
          //species1 = "(gas)";
          ClO41 = false; Cl1 = false; Na1 = false;
      }
      for(int j=0; j<(i+1); j++) {
          if(namn.z[i]*namn.z[j] >=0) {continue;}
          if(j<nIon) {
              species2 = Util.nameOf(namn.ident[j]);
              if(species2.equals(identClO4) && namn.z[j] == -1) {
                  ClO42 = true; Cl2 = false; Na2 = false;
              }
              else if(species2.equals(identCl) && namn.z[j] == -1) {
                  ClO42 = false; Cl2 = true; Na2 = false;
              }
              else if(species2.equals(identNa) && namn.z[j] == +1) {
                  ClO42 = false; Cl2 = false; Na2 = true;
              }
              else {ClO42= false; Cl2 = false; Na2 = false;}
          }
          else if(j==nIon) {
              //species2 = "Na";
              ClO42 = false; Cl2 = false; Na2 = true;
          }
          else if(j==nIon+1) {
              //species2 = "Cl";
              ClO42 = false; Cl2 = true; Na2 = false;}
          else {
              // species2 = "?";
              ClO42 = false; Cl2 = false; Na2 = false;
          }
          // note that if ClO41 is true, then ClO42 can not be true, etc
          if(ClO41) {
              if(Float.isNaN(getEpsilon(i,j))) {
                  setEpsilon(i,j, (0.2f * namn.z[j]),0f,0f);
              }
          } // ClO4-
          else if(ClO42) {
              if(Float.isNaN(getEpsilon(i,j))) {
                  setEpsilon(i,j, (0.2f * namn.z[i]),0f,0f);
              }
          } // ClO4-
          else if(Cl1) {
              if(Float.isNaN(getEpsilon(i,j))) {
                  setEpsilon(i,j, (0.1f * namn.z[j])-0.05f,0f,0f);
              }
          } // Cl-
          else if(Cl2) {
              if(Float.isNaN(getEpsilon(i,j))) {
                  setEpsilon(i,j, (0.1f * namn.z[i])-0.05f,0f,0f);
              }
          } // Cl-
          else if(Na1) {
              if(Float.isNaN(getEpsilon(i,j))) {
                  setEpsilon(i,j, (0.05f * namn.z[j]),0f,0f);
              }
          } // Na+
          else if(Na2) {
              if(Float.isNaN(getEpsilon(i,j))) {
                  setEpsilon(i,j, (0.05f * namn.z[i]), 0f, 0f);
              }
          } // Na+
          // species2 = "?";
      } //for j
  } //for i
  // other epsilon defaults
  for(int i = 0; i < nIon+2; i++) {
      // default values:
      if((i < nIon && !gas[i]) || i >= nIon) {
          for(int j=0; j<(i+1); j++) {
              if(namn.z[i]*namn.z[j] <0) {
                // default values: eps(M^+z, X^-y) = 0.15 + 0.15 (z + y)
                if(namn.z[i]*namn.z[j] < -1 
                        && (namn.z[i] == +1 || namn.z[j] == +1)) {
                    // Z = +1 and Z < -1
                    if(namn.z[j]<0) {k = j;} else {k = i;}
                    w = 0.0500001f*namn.z[k];
                }
                else { // either Z=+1 and Z=-1 or Z > +1 and Z <= -1
                    if(namn.z[i]>0) {k = i;} else {k = j;}
                    w = -0.05000001f + 0.1f*namn.z[k];
                }
                if(Float.isNaN(getEpsilon(i,j))) {setEpsilon(i,j,w,0f,0f);}
              }
          } //for j
      } //!gas
  } //for i
  // Set all remaining "empty" coefficients to zero
  for(int i = 0; i < nIon+2; i++) {
      for(int j=0; j<=i; j++) {
          if(Float.isNaN(getEpsilon(i,j))) {setEpsilon(i,j, 0f, 0f, 0f);}
      }
  }
  out.println("SIT-coefficients assigned.");
  // return;
} //readSITdataFiles
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="readSITfileSection">
/** Read a section of the SIT file. The SIT file has three sections:
 * 1) "normal" epsilon values; 2) epsilon values for neutral species with
 * electrolytes; and 3) epsilon values for neutral species with neutral species
 *
 * @param section the section to read, 1, 2 or 3
 * @param rd a reference to the class used for reading the SIT-file
 * @throws lib.kemi.haltaFall.Factor.SITdataException
 * @throws lib.kemi.readDataLib.ReadDataLib.ReadDataLib.DataReadException
 * @throws lib.kemi.readDataLib.ReadDataLib.ReadDataLib.ReadDataLib.DataEofException  */
private void readSITfileSection(int section, ReadDataLib rd)
        throws SITdataException, ReadDataLib.DataReadException, ReadDataLib.DataEofException {
  if(section < 1 || section > 3) {
      throw new SITdataException("Error in \"haltaFall.Factor.readSITfileSection\": section = "
                    +section+" (must be 1,2 or 3).");
  }
  if(rd == null) {
      throw new SITdataException(
            "Error in \"haltaFall.Factor.readSITfileSection\":"
                    +" instance of ReadDataLib is \"null\"");
  }
  // --- Search the SIT-file
  //    (compare names such as "Fe+2" and "Fe 2+")
  int zCat;
  int zAn = 0;
  float w0, w1, w2;
  String identNa = "Na"; String identCl = "Cl";
  String cation, identCation;
  String anion ="", identAnion ="";
  String identI;
  boolean firstLine = true;
  loopWhile:
  while (true) {
    //-- get the cation (or neutral species)
    rd.nowReading = "Cation name";
    if(section > 1) {rd.nowReading = "Neutral species name";}
    cation = rd.readA();
    if(cation.equalsIgnoreCase("NoDefaults") && firstLine) {
        setDefaultValues = false;
        continue;  // loopWhile
    }
    firstLine = false;
    if(cation.equalsIgnoreCase("END")) {break;} // loopWhile
    zCat = Util.chargeOf(cation);
    if(section < 2 && zCat<=0) {
        throw new SITdataException(
            "Error reading file \""+SIT_FILE+"\":"+nl+
            "   Trying to read a cation name, found: \""+cation+"\""+nl+
            "   but the charge is: "+zCat+" (must be >0)");
    } else if(section >= 2 && zCat!=0) {
        throw new SITdataException(
            "Error reading file \""+SIT_FILE+"\":"+nl+
            "   Trying to read a neutral species name, found: \""+cation+"\""+nl+
            "   but the charge is: "+zCat+" (must be zero)");
    }
    //get name without charge, and without "(aq)", etc
    identCation = Util.nameOf(cation);
    if(section == 1) {
        //-- get the anion
        rd.nowReading = "Anion name";
        anion = rd.readA();
        zAn = Util.chargeOf(anion);
        if(zAn>=0) {
            throw new SITdataException(
                "Error reading file \""+SIT_FILE+"\":"+nl+
                "   Trying to read an anion name, found: \""+anion+"\""+nl+
                "   but the charge is: "+zAn+" (must be <0)");
        }
        identAnion = Util.nameOf(anion);
    }
    //-- get three parameters for the interaction coefficient
    //   epsilon = eps0 + eps1 * T + eps2 * T*T  (T = temperature in Kelvins)
    if(section == 1) {
        rd.nowReading = "SIT interaction coefficient eps0["+cation+","+anion+"]";
        w0 = (float)rd.readR();
        if(Math.abs(w0)>=10f) {
            throw new SITdataException(
                "Error reading file \""+SIT_FILE+"\":"+nl+
                "   Trying to read the SIT coefficient eps0["+cation+","+anion+"]"+nl+
                "   but the value is: "+w0+" (must be between <10 and >-10)");
        }
        rd.nowReading = "SIT interaction coefficient eps1["+cation+","+anion+"]";
        w1 = (float)rd.readR();
        rd.nowReading = "SIT interaction coefficient eps2["+cation+","+anion+"]";
        w2 = (float)rd.readR();
        //-- search the chemical system for this cation-anion couple
        loopForI:
        for(int i=0; i<nIon; i++) {
            //    if Z>0 and the cation is the same as that in the SIT-file
            //           Find out if the anion is also in the chemical system.
            //           If yes get epsilon(cation,anion)
            //           Find also:  epsilon(cation,Cl-)
            //    if Z<0 and the cation is the same as that in the SIT-file
            //           find only:  epsilon(Na+,anion)
            if(gas[i]) {continue;}
            identI = Util.nameOf(namn.ident[i]);
            if(namn.z[i]<0) { // is this the anion in the SIT-file?
              if(!identI.equalsIgnoreCase(identAnion)
                      || namn.z[i] != zAn) {continue;}
              if(!identCation.equalsIgnoreCase(identNa)
                      || zCat != 1) {continue;}
              //found anion and cation is Na+
              setEpsilon(nIon, i, w0, w1, w2);
            } else
            { //z[i]>0 is this the cation in the SIT-file?
              if(!identI.equalsIgnoreCase(identCation)
                      || namn.z[i] != zCat) {continue;}
              // found the cation, check for Cl-
              if(identAnion.equalsIgnoreCase(identCl) && zAn == -1) {
                //found cation and anion is Cl-
                setEpsilon(nIon+1, i, w0, w1, w2);
              }
              //found the cation, look in chemical system for all anions
              String identI2;
              for(int i2=0; i2<nIon; i2++) {
                if(gas[i]) {continue;}
                identI2 = Util.nameOf(namn.ident[i2]);
                if(!identI2.equalsIgnoreCase(identAnion)
                        || namn.z[i2] != zAn) {continue;}
                // found both cation and anion, get value and quit search
                setEpsilon(i, i2, w0, w1, w2);
                break loopForI;
              } //for i2
            } //if z[i]>0
        } //for i
        // look for epsilon(Na+,Cl-)
        if(identCation.equalsIgnoreCase(identNa) && zCat == 1 &&
           identAnion.equalsIgnoreCase(identCl) &&  zAn == -1) {
                setEpsilon(nIon, nIon+1, w0, w1, w2);
        }
    } //if(section == 1)
    else
    if(section == 2) {
        rd.nowReading = "SIT interaction coefficient eps0["+cation+",MX]";
        w0 = (float)rd.readR();
        if(Math.abs(w0)>=10f) {
            throw new SITdataException(
                    "Error reading file \""+SIT_FILE+"\":"+nl+
                    "   When trying to read the SIT coefficient eps["+cation+",MX]"+nl+
                    "   but the value is: "+w0+" (must be <10 and >-10)");
        }
        rd.nowReading = "SIT interaction coefficient eps1["+cation+",MX]";
        w1 = (float)rd.readR();
        rd.nowReading = "SIT interaction coefficient eps2["+cation+",MX]";
        w2 = (float)rd.readR();
        for(int i=0; i<nIon; i++) {
            if(cs.noll[i] || namn.z[i] != 0) {continue;}
            identI = Util.nameOf(namn.ident[i]);
            if(!identI.equalsIgnoreCase(identCation)) {continue;}
            // found the neutral species
            for(int i2=0; i2<nIon; i2++) {
                if(cs.noll[i2] || namn.z[i2] == 0) {continue;}
                setEpsilon(i, i2, w0, w1, w2);
            } //for i2
            setEpsilon(i, nIon, w0, w1, w2);
            setEpsilon(i, nIon+1, w0, w1, w2);
        } //for i
    } //if(section == 3)
    else
    if(section == 3) {
        rd.nowReading = "SIT interaction coefficient eps["+cation+",neutral]";
        w0 = (float)rd.readR();
        if(Math.abs(w0)>=10f) {
            throw new SITdataException(
                    "Error reading file \""+SIT_FILE+"\":"+nl+
                    "   When trying to read the SIT coefficient eps["+cation+",neutral]"+nl+
                    "   but the value is: "+w0+" (must be <10 and >-10)");
        }
        rd.nowReading = "SIT interaction coefficient eps1["+cation+",MX]";
        w1 = (float)rd.readR();
        rd.nowReading = "SIT interaction coefficient eps2["+cation+",MX]";
        w2 = (float)rd.readR();
        for(int i=0; i<nIon; i++) {
            if(cs.noll[i] || namn.z[i] != 0) {continue;}
            identI = Util.nameOf(namn.ident[i]);
            if(!identI.equalsIgnoreCase(identCation)) {continue;}
            setEpsilon(i, i, w0, w1, w2);
        } //for i
    } //if(section == 4)
  } //while
} //readSITfileSection
// </editor-fold>
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="printEpsilons">

/** Prints a table with the epsilon values
 * @param out0 where the table will be printed. It may be
 *   <code>System.out</code>. If null, <code>System.out</code> is used.
 */
private void printEpsilons(java.io.PrintStream out0) {
  java.io.PrintStream o;
  if(out0 != null) {o = out0;} else {o = System.out;}
  o.println("List of \"epsilon\" values:");
  // --- print a title line with names
  o.print("            ");
  for(int i = 0; i < nIon; i++) {
    if(namn.ident[i].length() <=10) {o.format("%-10s",namn.ident[i]);}
    else {o.format("%-10s",namn.ident[i].substring(0,10));}
  } //for i
  o.format("%-10s%-10s%s","Na+","Cl-",nl);
  // --- table body:
  for(int i = 0; i < nIon+2; i++) {
    if(i<nIon) {
        if(namn.ident[i].length() <=10) {o.format("%-10s",namn.ident[i]);}
        else {o.format("%-10s",namn.ident[i].substring(0,10));}
    }
    else if(i==nIon) {o.format("%-10s","Na+");}
    else if(i==(nIon+1)) {o.format("%-10s","Cl-");}
        for(int j=0; j <= i ; j++) {
            o.format(engl, " %6.3f   ", getEpsilon(i,j));
        } //for j
        o.println();
  } //for i
  o.println(
          "Note that eps[i,j]=eps[j,i], and esp[i,j]=0 if z[i]*z[j]>0"+nl+
          "(equal charge sign), and  eps[i,i] is used only if z[i]=0."+nl+
          "The last two rows/columns, Na+ and Cl-, are used only"+nl+
          "if the aqueous solution is not electrically neutral.");
} //printEpsilons()
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="getEpsilon">
/** Retrieves a value corresponding to an element in a
 * square diagonal-symmetric matrix array using an equivalent triangular array.
 * If either <code>row</code> or <code>col</code> are negative,
 * <code>NaN</code> is returned.
 * @param row in the square matrix. If <code>row</code> is negative,
 *      <code>NaN</code> is returned.
 * @param col the column in the square matrix. If <code>col</code> is negative,
 *      <code>NaN</code> is returned.
 * @return the value retrieved
 * @see lib.kemi.haltaFall.Factor#setEpsilon(int, int, float, float, float)
 *                                                              setEpsilon
 * @see lib.kemi.haltaFall.Factor#printEpsilons(java.io.PrintStream)
 *                                                              printEpsilons
 */
private float getEpsilon(int row, int col) {
  if(row <0 || col <0 || eps == null) {return Float.NaN;}
  int i,j;
  float tKelvin;
  if(row > col) {
      i = row; j = col;
  } else {
      i = col; j = row;
  }
  float epsilon = eps.eps0[i][j];
  if(!Float.isNaN(epsilon)) {
    tKelvin = (float)(diag.temperature + 273.15);
    if(eps.eps1[i][j] != 0f) {
        epsilon = epsilon + eps.eps1[i][j] * tKelvin;
    }
    if(eps.eps2[i][j] != 0f) {
        epsilon = epsilon + eps.eps2[i][j] * tKelvin * tKelvin;
    }
  }
  return epsilon;
} // getEpsilon(row,col)

// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="setEpsilon">
/** Stores SIT values corresponding to an element in a square,
 * diagonal-symmetric matrix array, using an equivalent triangular array.
 * If either <code>row</code> or <code>col</code> are negative,
 * nothing is performed.
 * @param row in the square matrix. If <code>row</code> is negative
 *      nothing is performed.
 * @param col the column in the square matrix. If <code>col</code> is negative
 *      nothing is performed.
 * @param val0 a value to store
 * @param val1 a value to store
 * @param val2 a value to store
 * @see lib.kemi.haltaFall.Factor#getEpsilon(int, int) getEpsilon
 * @see lib.kemi.haltaFall.Factor#printEpsilons(java.io.PrintStream)
 *                                                          printEpsilons
 */
private void setEpsilon(int row, int col, float val0, float val1, float val2) {
  if(row <0 || col <0) {return;}
  if(row > col) {
      eps.eps0[row][col] = val0;
      eps.eps1[row][col] = val1;
      eps.eps2[row][col] = val2;
  } else {
      eps.eps0[col][row] = val0;
      eps.eps1[col][row] = val1;
      eps.eps2[col][row] = val2;
  }
} // setEpsilon(row,col, val)

// </editor-fold>

// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="factorPrint">

/** Prints the equations for the model used to calculate the log of the
 * activity coefficients.
 * @param verbose if false a single line is printed
 * @throws IllegalArgumentException
 * @throws lib.kemi.haltaFall.Factor.SITdataException */
public void factorPrint(boolean verbose)
        throws IllegalArgumentException, SITdataException {
  if(verbose) {out.println(dashLine);}
  if(dataNotSupplied || diag.activityCoeffsModel < 0) { // ideal solution
    if(verbose) {
        out.println("\"haltaFall.factor\" (activity coefficient calculations):"+nl+
              "    dataNotSupplied = "+dataNotSupplied+", activityCoeffsModel = "+diag.activityCoeffsModel);
    }
    out.println("Activity coefficient calulations will not be performed.");
    if(verbose) {out.println(dashLine);}
    return;
  } // ideal solution
  // rho and eps_H2O and Agamma are calculated by the constructor
  if(Double.isNaN(rho) || Double.isNaN(eps_H2O) || Double.isNaN(Agamma)) {
        try{checks_and_start();}
        catch(Exception ex) {
            diag.activityCoeffsModel = -1;
            diag.ionicStrength = Double.NaN; diag.ionicStrCalc = Double.NaN;
            diag.phi = Double.NaN;           diag.sumM = Double.NaN;
            throw new IllegalArgumentException("\"haltaFall.factor\"  (activity coefficient calculations):"+nl+
              "    "+ex.toString()+nl+
              "Activity coefficient calulations will not be performed.");
        }
  }
  String I;
  double iStr = 0;
  if(!Double.isNaN(diag.ionicStrength)) {iStr = diag.ionicStrength;}

  final String sigma ="where sigma(x) = (3/x^3) {(1+x) - 1/(1+x) - 2 ln(1+x)}";

  if(diag.activityCoeffsModel == 0) { //Davies
    if(verbose) {
      if(iStr < 0) {I="varied";}
      else {I=Double.toString(iStr);}
      String At = String.format(engl, "%8.4f", Agamma).trim();
      String Dt = String.format(engl, "%8.2f", DAVIES).trim();
      out.println(
        "Calculation of activity coefficients with I = "+I+
        ";  t = "+diag.temperature+"°C"+nl+
        "using Davies eqn.:"+nl+
        "  log f(i) = -"+At+" Zi^2 ( I^0.5 / (1 + I^0.5) - " + Dt +" I)");
      if(cs.jWater >= 0) {
      out.println(
        "The activity of water is calculated according to"+nl+
        "  log(a(H2O)) = - phi Sum[m] /(ln(10) 55.508)"+nl+
        "the osmotic coefficient \"phi\" is calculated from:"+nl+
        "  phi = 1 - (2/3) (ln(10)/Sum[m]) "+At+" I^(3/2) sigma(I^0.5)" +nl+
        "              + (ln(10)/Sum[m]) (" +At + " x " + Dt + ") I^2"
                +nl+ sigma + ".");
      }
    } else {out.println("Davies eqn. for activity coefficient calculations.");}
  } //Davies
  else if(diag.activityCoeffsModel == 1) { //SIT
    String S;
    if(iStr < 0) {I="varied"; S="Sum[m]";}
    else {I=Double.toString(iStr); S="I";}
    Bgamma = lib.kemi.H2O.Dielectric.B_gamma(diag.temperature, rho, eps_H2O);
    rB = 4.56899 * Bgamma;  // this gives 1.5 at 25°C
    if(verbose) {
      String At = String.format(engl, "%8.4f", Agamma).trim();
      String Bt = String.format(engl, "%5.2f", rB).trim();
      out.println(
        "Calculation of activity coefficients with I = "+I+",  and t = "
                +diag.temperature+"°C."+nl+
        "using the SIT model:"+nl+
        "  log f(i) = -("+At+" Zi^2 I^0.5 / (1 + "+Bt+" I^0.5))"+
                " + Sum[ eps(i,j) m(j) ]"+nl+
        "for all \"j\" with Zj*Zi<=0;  where eps(i,j) is a"
                +" specific ion interaction"+nl+
        "parameter (in general independent of I).");
      if(cs.jWater >= 0) {
      out.println(
        "The activity of water is calculated according to"+nl+
        "  log(a(H2O)) = - phi Sum[m] /(ln(10) 55.508)"+nl+
        "the osmotic coefficient \"phi\" is calculated from:"+nl+
        "  phi = 1 - (2/3) (ln(10)/Sum[m]) "+At+" I^(3/2)"+
                " sigma("+Bt+" I^0.5)" + nl +
        "          + (ln(10)/Sum[m]) Sum_i[ Sum_j[ eps(i,j) m(i) m(j) ]]"+nl+
        sigma + ";" +nl+
        "and where \"i\" are cations or neutral species and \"j\" are anions.");
      }

    } else {out.println("SIT method for activity coefficient calculations.");}
    // --- Check if there is a need to get "epsilon" values
    if(nIon > 0) {
        if(eps == null || Float.isNaN(eps.eps0[0][0])) {
            try{readSITdataFiles();}
            catch (Exception ex) {
                diag.activityCoeffsModel = -1;
                diag.ionicStrength = Double.NaN; diag.ionicStrCalc = Double.NaN;
                diag.phi = Double.NaN;           diag.sumM = Double.NaN;
                throw new SITdataException("\"haltaFall.factor\"  (activity coefficient calculations):"+nl+
                      ex.getMessage()+nl+"Activity coefficient calulations will not be performed.");
            }
        }
        if(verbose) {printEpsilons(out);}
    }
  } //SIT
  else if(diag.activityCoeffsModel == 2) { //HKF
    String S;
    if(iStr < 0) {I="varied"; S="I";}
    else {
        I=Double.toString(iStr);
        S="I) = "+(-(float)Math.log10(1+0.0180153*iStr));
    }
    try{bgi = b_gamma_NaCl(diag.temperature, diag.pressure, eps_H2O, g_function);}
    catch (Exception ex) {
        diag.activityCoeffsModel = -1;
        diag.ionicStrength = Double.NaN; diag.ionicStrCalc = Double.NaN;
        diag.phi = Double.NaN;           diag.sumM = Double.NaN;
        throw new IllegalArgumentException("\"haltaFall.factor\"  (activity coefficient calculations):"+nl+
              "    "+ex.toString()+nl+
              "Activity coefficient calulations will not be performed.");
    }
    // use distance of closest approach for NaCl (HKF-4, Table 3)
    rB = lib.kemi.H2O.Dielectric.B_gamma(diag.temperature, rho, eps_H2O) *
                ( (1.81+g_function) + (0.97 + (0.94+g_function)) );
    if(verbose) {
      String At = String.format(engl, "%9.5f", Agamma).trim();
      String Bt = String.format(engl, "%6.3f", rB).trim();
      String bgit = String.format(engl, "%7.4f", bgi).trim();
      out.println(
        "Calculation of activity coefficients with I = "+I+
        ";  t = "+diag.temperature+"°C"+nl+
        "using simplified Helgeson, Kirkham & Flowers model:"+nl+
        "  log f(i) = -("+At+" Zi^2 I^0.5) / (1 + "+Bt+" I^0.5)"+
                " + Gamma + ("+bgit+" I)"+nl+
        "where: Gamma = -log(1+0.0180153 "+S+")");
      if(cs.jWater >= 0) {
      out.println(
        "The activity of water is calculated according to"+nl+
        "  log(a(H2O)) = - phi Sum[m] /(ln(10) 55.508)"+nl+
        "the osmotic coefficient \"phi\" is calculated from:"+nl+
        "  phi = (ln(10) Gamma / (0.0180153 "+S+"))" + nl +
        "              - (2/3) (ln(10)/Sum[m]) "+At+" I^(3/2)"+
                " sigma("+Bt+" I^0.5)" + nl +
        "              + (ln(10) (1/2) " + bgit + " I)" + nl +
        sigma + ".");
      }
    } else {
        out.println("\"HKF\" model for activity coefficient calculations.");
    }
  } //HKF
  if(verbose) {out.println(dashLine);}
} //factorPrint

// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="printActivityCoeffs">
/** Prints the calculated activity coefficients, the ionic strength,
 * and the sum of solute concentrations
 * @param out0 where messages will be printed. It may be
 *   <code>System.out</code>. If null, <code>System.out</code> is used.
 */
public void printActivityCoeffs(java.io.PrintStream out0) {
  java.io.PrintStream o;
  if(out0 != null) {o = out0;} else {o = System.out;}
  if(diag.activityCoeffsModel <0 || diag.activityCoeffsModel >2) {
      o.println("No activity coefficient calculations performed (ideal solutions).");
      return;
  }
  else {
    String t = "Activity coefficients calculated using ";
    if(diag.activityCoeffsModel == 0) {o.println(t+"Davies eqn.");}
    else if(diag.activityCoeffsModel == 1) {o.println(t+"SIT model.");}
    else if(diag.activityCoeffsModel == 2) {
        o.println(t+"the simplified HKF model.");
    }
  }
  if(Double.isNaN(diag.ionicStrength) || diag.ionicStrength < 0) {
      o.print("Ionic Strength (calculated) = "+(float)ionicStr);
  } else {out.print("Ionic Strength (provided by the user) = "+(float)diag.ionicStrength);}
  o.println();
  o.format(engl,"Electrical balance: %-+14.6g%s",electricBalance,nl);
  o.println("Tolerance = "+(float)cs.chemConcs.tolLogF+" (when calculating log10(f)).");
  o.println("List of calculated activity coefficients:"+nl
           +" nbr  species                  z    log10(f)   Conc.");
  for(int j = 0; j < nIon; j++) {
    if(j == cs.jWater) {
      o.format(engl,"%4d   %-20s  a(H2O) =%7.4f  phi =%7.4f%s",j,namn.ident[j],
              Math.pow(10,cs.chemConcs.logA[j]),osmoticCoeff,nl);
    } else {
      o.format(engl,"%4d   %-20s  %3d  %9.4f  %9.2g%s",j,namn.ident[j],
              namn.z[j],cs.chemConcs.logf[j],cs.chemConcs.C[j],nl);
    }
  } //for j
  o.format(engl,"Sum of concentrations, Sum[m] = %-14.6g%s",sumM,nl);
} //printActivityCoeffs
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="calcIonicStr">
/** Calculates <code>ionicStr</code>, <code>electricBalance</code> and
 * <code>sumM</code>.<br>
 * First the Electrical Balance is calculated.  To maintain an electrically
 * neutral solution: If <code>electricBalance</code> &gt; 0, then an inert
 * anion (X-) is added. If <code>electricBalance</code> &lt; 0,  then  an
 * inert cation (M+) is added.
 * @param C concentrations
 * @param z electric charges
 * @return the calculated value for the ionic strength
 */
private double calcIonicStr(double[] C, int[] z) {
    double ionicStrengthCalc = 0;
    sumM = 0;
    electricBalance = 0;
    double Ci;
    for(int i =0; i < nIon; i++) {
        if(gas[i]) {continue;}
        //Ci = Math.max(0, Math.min(MAX_CONC, C[i]));
        Ci = Math.max(0, Math.min(1.e+35, C[i]));
        sumM = sumM + Ci;
        if(z[i] == 0) {continue;}
        // Ci = Math.max(0, Math.min(1e35, C[i])); // max. concentration
        electricBalance = electricBalance + z[i]*Ci;
        ionicStrengthCalc = ionicStrengthCalc + z[i]*z[i]*Ci;
    }
    ionicStrengthCalc = 0.5 * (Math.abs(electricBalance) + ionicStrengthCalc);
    sumM = sumM + Math.abs(electricBalance);
    //sumM = Math.min(50, sumM);
    //ionicStrengthCalc = Math.min(200, ionicStrengthCalc); // max = 20 molal ThCl4 -> I=200
    return ionicStrengthCalc;
} //calcIonicStr
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="isGasOrLiquid(String)">
private static boolean isGasOrLiquid(String t0) {
    if(t0 == null) {return false;}
    if(t0.length() > 3) {
        return (t0.toUpperCase().endsWith("(G)")
             || t0.toUpperCase().endsWith("(L)"));
    }
    return false;
} //isGasOrLiquid(String)
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="b_gamma_NaCl">
/** Returns b_gamma_NaCl in the HKF model of activity coefficients.
 * Range of conditions: < 5,000 bar, -35 to 1000°C.
 * It throws an exception outside this range.
 * If pBar < 1 a value of pBar = 1 is used.
 * If tC = 0 it returns the value at 0.01 Celsius.
 * NOTE: values returned at tC <0 are extrapolations outside the valid range.
 * It requires a vlues of <code>eps_H2O</code> and of <code>g_function</code>
 * @param tC temperature in Celsius
 * @param pBar pressure in bar
 * @param epsH2O the dielectric constant
 * @param gf the g-function (in units of Å) in the HKF model
 * @return b_gamma_NaCl in the HKF model of activity coefficients
 * @throws IllegalArgumentException
 */
public static double b_gamma_NaCl(final double tC, final double pBar,
        final double epsH2O, final double gf)
        throws IllegalArgumentException {
/** b_gamma_NaCl is “defined” in Eqn. (173) of Helgeson, Kirkham and Flowers (1981)
 * values at 25°C and 1 bar are listed in Tables 5 and 6, while Table 26 gives
 * values at temps up to 325°C and Psat (pressures corresponding to the
 * liquid-vapor equilibrium) and Table 27 lists values at temperatures up to
 * 500°C and pressures up to 5000 bar.
 * <p>
 * See also eqn.(22) in  Oelkers and Helgeson (1990) where "bγ,NaCl" is given in
 * Table A-2, and the eqn. (B-12) and parameters in Table A-1.  Note that the
 * values at 3 kbar and temps. of 800-1000°C in the Table A-2 are wrong:
 * they must be multiplied by 10.  Note also that eqn.(B-13) in that paper
 * is wrong.  The same expression for bγ may be found as eqns.(2) and (31)-(32)
 * in Pokrovskii and Helgeson (1997).
 * <p>
 * To calculate values in Table A-2 of Oelkers and Helgeson (1990) using eqn (B-12)
 * or the equivalent eqns. (31)-(32) in Pokrovskii and Helgeson (1997) one needs
 * values of the dielectric constant (ε) and of ω, which requires values of the
 * g-function (see e.g. eqn.(15) in Pokrovskii and Helgeson 1997).
 * <p>
 * Values of the relative permittivity of water (dielectric constant ε) are
 * calculated using the equations of Johnson and Norton (1991).  See also
 * Johnson et al (1992) and Shock et al (1992) which lists values of ε in Table C2.
 * <p>
 * The g-function is described in Johnson et al (1992), eqns. (49)-(51) and
 * parameters in Tables 2 and 3.  See also Shock et al. (1992), where values of
 * g are given in Table 5.
 * <p>
 * References
 * Helgeson, Kirkham and Flowers, Amer. J. Sci. 281 (1981) 1249-1516, doi: 10.2475/ajs.281.10.1249
 * Oelkers and Helgeson, Geochim. Cosmochim. Acta 54 (1990) 727-738 doi: 10.1016/0016-7037(90)90368-U
 * Johnson, Norton. Amer. J. Sci., 291 (1991) 541–648. doi: 10.2475/ajs.291.6.541
 * Johnson, Oelkers, Helgeson. Computers & Geosciences, 18 (1992) 899–947. doi: 10.1016/0098-3004(92)90029-Q
 * Shock, Oelkers, Johnson, Sverjensky, Helgeson. J. Chem. Soc., Faraday Trans., 88 (1992) 803–826. doi: 10.1039/FT9928800803
 * Pokrovskii and Helgeson, Geochim. Cosmochim. Acta 61 (1997) 2175-2183 doi: 10.1016/S0016-7037(97)00070-7
 */
    if(Double.isNaN(tC) || Double.isNaN(pBar)) {
        throw new IllegalArgumentException("\"haltaFall.Factor.b_gamma_NaCl\": tC="+tC+", pBar="+pBar+" (must be a number)");
    }
    if(tC < -35 || tC > 1000.01 || pBar < 0 || pBar > 5000.01) {
        throw new IllegalArgumentException("\"haltaFall.Factor.b_gamma_NaCl\": tC="+tC+", pBar="+pBar+" (must be -35 to 1000 C and 0 to 5 kbar)");
    }
    if(Double.isNaN(epsH2O) || Double.isNaN(gf)) {
        throw new IllegalArgumentException("\"haltaFall.Factor.b_gamma_NaCl\": eps(H2O)="+epsH2O+", g-function="+gf+" (must be a number)");
    }
    final double p_Bar = Math.max(1., pBar);
    final double t_C;
    // if temperature = 0, set temperature to 0.01 C (tripple point of water)
    if(Math.abs(tC) < 0.001) {t_C = 0.01;} else {t_C = tC;}
    final double tK = t_C + 273.15;
    final double tr = 298.15;
    final double eta = 1.66027e5;
    final double r_c = 0.97, r_a = 1.81;
    final double a1 = 0.030056, a2 = -202.55, a3 = -2.9092, a4 = 20302;
    final double a5 = -0.206,   c1 = -1.50,   c2 = 53300., omg = 178650.;
    final double bg = -174.623, bs = 2.164;
    final double r_eff_c = r_c + (0.94+gf);
    final double r_eff_a = r_a + gf;
    final double omgpt = eta*( (1./r_eff_c) + (1./r_eff_a) );
    final double f1T = tK*Math.log(tK/tr) - tK + tr;
    final double f2T = ((1./(tK-228.))-(1./(tr-228.))) * ((228.-tK)/228.) -(tK/(228.*228.))
				* Math.log((tr*(tK-228.))/(tK*(tr-228.)));
    final double f1P = p_Bar-1.;
    final double f2P = Math.log((2600.+p_Bar)/(2600.+1.));
    final double f1PT = (p_Bar-1.)/(tK-228.);
    final double f2PT = (1./(tK-228.))*Math.log((2600.+p_Bar)/(2600.+1.));
    /**
    System.out.println("r_eff_c = "+(float)r_eff_c+", r_eff_a = "+(float)r_eff_a);
    System.out.println("eps = "+(float)epsH2O+", g = "+(float)(gf*10000.)+", omgpt = "+(float)omgpt);
    System.out.println("- bg + bs*(tK-298.15) = "+(float)(- bg + bs*(tK-tr)));
    System.out.println("c1*( f1T ) = "+(float)(c1*( f1T )));
    System.out.println("c2*( f2T ) = "+(float)(c2*( f2T )));
    System.out.println("a3*( f1PT ) + a4*( f2PT )= "+(float)(a3*( f1PT )+ a4*( f2PT )));
    // */
    final double nbg = - bg + bs*(tK-tr)
            - c1*( f1T ) - c2*( f2T )
            + a1*( f1P ) + a2*( f2P )
            + a3*( f1PT ) + a4*( f2PT )
            + a5*(omgpt*((1./epsH2O)-1.) - omg*((1./78.244)-1.) + (-5.799e-5)*omg*(tK-tr));
    final double bgam = nbg/(2.*ln10*(1.98720426)*tK); // gas constant in cal/(K mol)
    return bgam;
}
  //</editor-fold>

//<editor-fold defaultstate="collapsed" desc="inner class SITeps">
/** <p>A class to store SIT specific ion interaction coefficients
 * (usually called "epsilon") for a given chemical system.
 * <p>The number of epsilon coefficients needed is (nIons+2) to achieve
 * electroneutrality.  Aqueous solutions are made electrically neutral
 * by adding two fictive species (Na+ or Cl-) in method "factor",
 * and therefore epsilon values for the two species Na+ and Cl- must be
 * added to any chemical system. */
public class SITeps {
 /** <ul><li>
  * <code>eps0[i][j]</code> = temperature-independent term for the
  * specific ion interaction coefficient between a cation <code>i</code> and
  * an anion <code>j</code>.<li>
  * <code>eps0[n][n]</code> = T-independent term for the specific ion
  * interaction coefficient between a neutral species <code>n</code> and
  * other neutral species</ul>
  * The specific ion interaction coefficient are temperature-dependent:
  * epsilon = <code>eps0 + eps1 * T + eps2 * T*T</code>;
  * where <code>T</code> is the temperature in Kelvins */
  public float[][] eps0;
 /** specific ion interaction coefficients: <code>eps1[][]</code> = temperature
  * dependence term: epsilon = <code>eps0 + eps1 * T + eps2 * T*T</code>;
  * where <code>T</code> is the temperature in Kelvins
  * @see lib.kemi.chem.Chem.SITepsilon#eps0 eps0  */
  public float[][] eps1;
 /** specific ion interaction coefficients: <code>eps2[][]</code> = temperature
  * dependence term: epsilon = <code>eps0 + eps1 * T + eps2 * T*T</code>;
  * where <code>T</code> is the temperature in Kelvins
  * @see lib.kemi.chem.Chem.SITepsilon#eps0 eps0  */
  public float[][] eps2;

/** Create an instance of a class to store SIT specific ion interaction
 * coefficients, usually called "epsilon", for a given chemical system.
 * @param nIons
 * @throws lib.kemi.haltaFall.Factor.SITdataException */
  public SITeps (int nIons) throws SITdataException  {
    if(nIons<=0) {
        throw new SITdataException(
        "Error in \"SITepsilon\"-constructor: nIons="+nIons+". Must be >0.");
    }
    // The number of epsilon coefficients needed is (nIons+2). This is because
    // aqueous solutions are made electrically neutral by adding (Na+ or Cl-)
    // in method "factor". For electroneutrality the 2 species Na+ and Cl-
    // must be added to any chemical system. This means that the array "z" also
    // has the dimension (nIons+2).
    //
    // The SIT coefficients are symmetric: eps[i][j] = eps[j][i].
    // To save memory a triangular array is used. The diagonal is kept
    // to store interaction coefficients of neutral species with themselves
    //
    // Make the triangular arrays
    eps0 = new float[nIons+2][];
    eps1 = new float[nIons+2][];
    eps2 = new float[nIons+2][];
    for(int i = 0; i< (nIons+2); i++) {
      eps0[i] = new float[i+2];
      eps1[i] = new float[i+2];
      eps2[i] = new float[i+2];
    }
    eps0[0][0] = Float.NaN; // flag that data is not initialised yet
  } //constructor
  } //class SITepsilon
//</editor-fold>

/** Exception when dealing with SIT (Specific Ion interaction Theory) data */
public static class SITdataException extends Exception {
    /** Exception when dealing with SIT
     * (Specific Ion interaction Theory) data. */
    public SITdataException() {}
    /** Exception when dealing with SIT
     * (Specific Ion interaction Theory) data
     * @param txt text description  */
    public SITdataException(String txt) {super(txt);}
  }

} 