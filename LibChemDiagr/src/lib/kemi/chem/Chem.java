package lib.kemi.chem;

import lib.common.Util;

/** Chem: a class with nested classes to store data.
 * Mainly intended to be used by HaltaFall:<ul>
 *  <li> a class "ChemSystem" defining the thermodynamic data and
 *       the stoichiometry of a chemical system
 *  <li> an inner class "ChemConcs" with the concentrations and
 *      instructions to do the calculations
 *  <li> a class "NamesEtc" with the names of chemical species,
 *      their electric charges, etc. These data is also used to calculate
 *      activity coefficients in HaltaFall.</ul>
 * Other programs (SED, Predom, etc) also use the following classes:<ul>
 *  <li> two classes "Diagr" and "DiagrConcs" with data on how the
 *      diagram is to be drawn, etc
 * </ul>
 * Each instance of this class contains one instance of the inner classes. 
 * <br>
 * Copyright (C) 2014-2018 I.Puigdomenech.
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
public class Chem {
/** Default value for debug output in HaltaFall: <code>DBGHALTA_DEF</code> =1 (output errors only).
 * @see ChemSystem.ChemConcs#dbg Chem.ChemSystem.ChemConcs.dbg */
public static final int DBGHALTA_DEF = 1;
/** the default tolerance in programs SED and Predom */
public static final double TOL_HALTA_DEF = 1E-4;
/** A class that contains thermodynamic and stoichiometric information for a chemical system. */
public ChemSystem chemSystem = null;
/** A class that contains diverse information (except arrays) associated with a chemical
 * diagram: what components in the axes, diagram type, etc. */
public Diagr diag = null;
/** A class that contains diverse information (arrays) associated with a chemical
 * diagram: concentration ranges and how the concentrations are varied for
 * each component in the diagram. */
public DiagrConcs diagrConcs = null;
/** New-line character(s) to substitute "\n" */
private static final String nl = System.getProperty("line.separator");

/** A container class: its inner classes are ChemSystem, NamesEtc,
 * Diagr and DiagrConcs. These inner classes store information on
 * chemical system and the diagram to draw.<br>
 * When an object of this class is created, an object of each inner class
 * is also created.<br>
 * However, more objects of each of the inner classes may be created later
 * if needed, and these new objects may describe different chemical systems
 * (different array sizes, etc).
 * @param Na int number of chemical components
 * @param Ms int the total number of species: components (soluble + solid)
 *     + soluble complexes + solids.
 * @param mSol int the number of solids (reaction products + components)
 * @param solidC int how many of the components are solid
 * @throws lib.kemi.chem.Chem.ChemicalParameterException */
public Chem(int Na, int Ms, int mSol, int solidC)
        throws ChemicalParameterException {
    if(Na<0 || Ms<0 || mSol<0 || solidC<0) {
        throw new ChemicalParameterException(
            "Error in \"Chem\"-constructor: Na="+Na+", Ms="+Ms+", mSol="+mSol+", solidC="+solidC+nl+"   All must be >=0.");
    }
    if(Ms < (Na+mSol)) {
        throw new ChemicalParameterException(
            "Error in \"Chem\"-constructor: Ms="+Ms+", and Na="+Na+", mSol="+mSol+nl+"   Ms must be >=(Na+mSol).");
    }
    if(solidC > Na) {
        throw new ChemicalParameterException(
            "Error in \"Chem\"-constructor: solidC="+solidC+", and Na="+Na+nl+"   solidC must be < Na.");
    }
    //create objects of the inner classes
    chemSystem = new ChemSystem(Na,Ms,mSol,solidC);
    diag = new Diagr();
    diagrConcs = new DiagrConcs(Na);
    // eps = new SITepsilon(Ms - mSol);
} // Chem constructor

/** The parameters supplied to a constructor are invalid: either outside the
 * allowed range or incompatible with each other.
 * The constractor can not be executed. */
public static class ChemicalParameterException extends Exception {
    /** The parameters supplied to a constructor are invalid: either outside the
     * allowed range or incompatible with each other.
     * The constractor can not be executed. */
    public ChemicalParameterException() {}
    /** The parameters supplied to a constructor are invalid: either outside the
     * allowed range or incompatible with each other.
     * The constractor can not be executed.
     * @param txt text description */
    public ChemicalParameterException(String txt) {super(txt);}
} //ChemicalParameterException

//<editor-fold defaultstate="collapsed" desc="class ChemSystem + inner classes: ChemConcs + NamesEtc">
/** A class to store the minimum information needed to define a chemical
 * system. It contains two inner classes: "<code>ChemConcs</code>" with the
 * concentrations and instructions to do the calculations with <code>HaltaFall</code>.
 * And "<code>NamesEtc</code>" with the names, electrical charge, etc of all
 * species in the chemical system.
 * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs ChemConcs
 * @see lib.kemi.chem.Chem.ChemSystem.NamesEtc NamesEtc
 * @see lib.kemi.haltaFall.HaltaFall HaltaFall */
public class ChemSystem{
/** number of chemical components */
public int Na;
/** total number of species (components (soluble + solid)
 *                      + soluble complexes + solid products) */
public int Ms;
/** number of solids (components + reaction products)
 * <pre> Note: If some component is a solid phase:
 * for each solid component a new solid complex is added.
 * If the number of solid components is "solidC",
 * Increase Ms (nbr of species) and mSol (nbr solids) by solidC:
 *      mSol = (solid products)+solidC
 *      Ms = (Tot. nbr. species)+solidC
 * Then the solid reaction product "i" are added as follows:
 * for all i = 0 ... (solidC-1):
 *      j = (Ms-Na-solidC)+i;
 *      k = (Na-solidC)+i;
 *      lBeta[j] = 0;
 * and for all n (0...(Na-1)):
 *      a[j][n]=0.; except
 *      a[j][n]=1.; if n = k
 * and set noll[k] = true;
 * (the solid component is not a soluble species)</pre> */
public int mSol;
/** Nbr of soluble (non-solid) complexes   (nx = Ms - Na - mSol) */
public int nx;
/** How many of the components are solids.
 * <pre> Note: If some of the components is a solid phase,
 * for each solid component a new solid complex is added.
 * Increase Ms (nbr of species) and mSol (nbr solids) by solidC:
 *      mSol = (solid reaction products)+solidC
 *      Ms = (Tot. nbr. species)+solidC
 * Then the solid reaction products "i" are added as follows:
 * for all i = 0 ... (solidC-1):
 *      j = (Ms-Na-solidC)+i;
 *      k = (Na-solidC)+i;
 *      lBeta[j] = 0;
 * and for all n (0...(Na-1)):
 *      a[j][n]=0.; except
 *      a[j][n]=1.; if n = k
 * and set noll[k] = true;
 * (the solid component is not a soluble species)</pre> */
public int solidC = 0;
// ------------------------------------------------------------
/**  a[i][j] = formula units for species i and component j
 *              (i=0...(Ms-Na-1), j=0...(Na-1)) */
public double[][] a;
/**  lBeta[i] = log10(Beta) for complex i (Beta = global equilibrium
 *             constant of formation, i=0...Ms-Na-1);
 *             solid phases are those with: i = Ms-mSol ... (Ms-1) */
public double[] lBeta;
/**  noll[i] = True if the concentration of this species is zero ("e-", etc);
 *  False if this is a normal species (i=0...(Ms-1)) */
public boolean[] noll;
/** Which of the components in the chemical system is "water";
 * -1 if water is not present. Water is identified in <code>readChemSystAndPlotInfo</code>
 * as the component with a name in <code>identC[]</code> of either "H2O" or
 * "H2O(l)".
 * @see lib.kemi.haltaFall.Factor#osmoticCoeff haltaFall.Factor.osmoticCoeff
 * @see lib.kemi.haltaFall.Factor#log10aH2O haltaFall.Factor.log10aH2O */
public int jWater = -1;
/** A class that contains the chemical concentrations associated
 *  with a chemical system. */
public ChemConcs chemConcs;
/** A class that contains diverse information associated with a chemical system,
 * such as names of species and electric charge. */
public NamesEtc namn = null;

/** A class to store the minimum information needed to define a chemical
 * system. When an object of this class is created, an object of the
 * inner class "ChemConcs" is also created. ALthough more "ChemConcs"  objects
 * could be created later if needed, these new objects exist within
 * the same chemical systems (the same array sizes, etc).
 * @param Na int number of chemical components
 * @param Ms int the total number of species: components (soluble + solid)
 *     + soluble complexes + solids.
 * @param mSol int the number of solids (reaction products + components)
 * @param solidC int how many of the components are solid
 * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs ChemConcs
 * @see lib.kemi.haltaFall.HaltaFall HaltaFall
 * @throws lib.kemi.chem.Chem.ChemicalParameterException */
public ChemSystem(int Na, int Ms, int mSol, int solidC)
    throws ChemicalParameterException {
    if(Na<=0 || Ms<=0) {
        throw new ChemicalParameterException(
            "Error in \"ChemSystem\"-constructor: Na="+Na+", Ms="+Ms+nl+"   All must be >0.");
    }
    if(mSol<0) {
        throw new ChemicalParameterException(
            "Error in \"ChemSystem\"-constructor:  mSol="+mSol+nl+"   must be >=0.");
    }
    if(Ms < (Na+mSol)) {
        throw new ChemicalParameterException(
            "Error in \"ChemSystem\"-constructor: Na="+Na+", Ms="+Ms+", mSol="+mSol+nl+"   Ms must be >=(Na+mSol).");
    }
    this.Na = Na; this.Ms = Ms; this.mSol = mSol; this.solidC = solidC;
    nx = Ms - Na - mSol;
    a = new double[Ms-Na][Na];
    lBeta = new double[Ms-Na];
    noll = new boolean[Ms];
    for(int i = 0; i < noll.length; i++)
        {noll[i] = false;} // for i
    chemConcs = new ChemConcs();
    namn = new NamesEtc(Na, Ms, mSol);
} // ChemSystem(Na, Ms, mSol)

//<editor-fold defaultstate="collapsed" desc="printChemSystem">
/** Print the data defining a chemical system
 * @param out a PrintStrem, such as <code>System.out</code>.
 * If null, <code>System.out</code> is used. */
public void printChemSystem(java.io.PrintStream out) {
  if(out == null) {out = System.out;}
  out.flush();
  java.util.Locale e = java.util.Locale.ENGLISH;
  final String LINE = "-------------------------------------";
  int j=0,js=0;
  String t, t2=" ", tjs;
  int n0, nM, iPl, nP;
  out.println(LINE);
    out.println("Chemical System: Na, Nx, Msol, solidC = "+Na+", "+nx+", "+mSol+", "+solidC);
    if(solidC>0) {
        out.println("Components (the last "+solidC+" are solids), name and noll:");
    } else {out.println("Components, name and noll:");}
    n0 = 0;     //start index to print
    nM = Na-1;  //end index to print
    iPl = 1; nP= nM-n0; //items_Per_Line and number of items to print
  print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        if(namn != null && namn.ident[kjj] != null) {
            if(namn.ident[kjj].isEmpty()) {t="\"\"";} else {t=namn.ident[kjj];}
        } else {t="\"null\"";}
        out.format(e,"%3d %20s, %5b",j,t,noll[kjj]); j++;
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); // out.print("    ");
    } //for ijj

    out.println("reaction products: name, logBeta, noll, a[]=");
    for(int i = 0; i <nx; i++) {
    if(namn != null && namn.ident[i+Na] != null) {
        if(namn.ident[i+Na].isEmpty()) {t="\"\"";} else {t=namn.ident[i+Na];}
    } else {t="\"null\"";}
    out.format(e,"%3d%4s %20s, %10.5f %5b ",j,t2,t,lBeta[i],noll[i+Na]); j++;
    n0 = 0;     //start index to print
    nM = Na-1;  //end index to print
    iPl = 8; nP= nM-n0; //items_Per_Line and number of items to print
  print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        out.format(e," %8.3f",a[i][kjj]);
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); out.print("              ");} //for ijj
    } //for i

    for(int i = nx; i <(nx+mSol); i++) {
    if(namn != null && namn.ident[i+Na] != null) {
        if(namn.ident[i+Na].isEmpty()) {t="\"\"";} else {t=namn.ident[i+Na];}
    } else {t="\"null\"";}
    tjs=Integer.toString(js);
    t2="(";
    t2= t2.concat(tjs.trim()).concat(")");
    out.format(e,"%3d%4s %20s, %10.5f %5b ",j,t2,t,lBeta[i],noll[i+Na]); j++; js++;
    n0 = 0;     //start index to print
    nM = Na-1;  //end index to print
    iPl = 8; nP= nM-n0; //items_Per_Line and number of items to print
  print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        out.format(e," %8.3f",a[i][kjj]);
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); out.print("              ");} //for ijj
    } //for i

    out.println(LINE);
    out.flush();
} //printChemSystem
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="inner class ChemConcs">
/** A class to store the chemical concentrations associated
 * with a chemical system. Used by <code>HaltaFall</code>.
 * @see lib.kemi.chem.Chem.ChemSystem ChemSystem
 * @see lib.kemi.haltaFall.HaltaFall HaltaFall */
public class ChemConcs{
/**  <code>kh[i] (i=0...(Na-1))</code>
 * <ul><li><code>kh =1</code> if the total concentration is given as
 * input to HaltaFall in array tot[]. The mass-balance equation has then
 * to be solved in HaltaFall; the tolerance is given in tol (e.g. 1e-4)</li>
 * <li><code>kh =2</code> if log10(activity) is given as input to HaltaFall in array logA[].
 * The total concentration will be calculated by HaltaFall</li></ul>
 * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs#tot tot
 * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs#tol tol
 * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs#cont cont */
    public int[] kh;
/**  logA[i] = log10(activity) for component i (i=0...(Na-1));
 * needed only if kh[i]=2)
 * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs#kh kh */
    public double[] logA;
/**  tot[i]  = total concentration for component i (i=0...(Na-1));
 * needed only if kh[i]=1)
 * @see lib.kemi.chem.Chem$ChemSystem.ChemConcs#kh kh */
    public double[] tot;
/** relative maximum tolerance to solve the mass balance equations; used only for
 * components with kh[i]=1. For example: 1e-4. Must be between 1e-9 and 1e-2.
 * If the total concentration of a component is zero, then a small absolute
 * tolerance is used, which depends on the total concentrations for the other
 * components, for example = 1e-8 times tol.
 * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs#kh kh */
    public double tol;
/**  solub[i]= calculated solubility for component i (i=0...(Na-1)) */
    public double[] solub;
/**  c(i) = calculated concentration for species i (i=0...(Ms-1)) */
    public double[] C;
/**  logF(i) = log10(activity coefficient) for species i (i=0...(Ms-1)) */
    public double[] logf;
/** when calling HaltaFall set <code>dbg</code> equal to zero or one on a normal run,
 * and to a larger value for debug print-out:<br> 
 * <code>dbg</code> =0 do not output anything<br>
 * =1 output errors, but no debug information<br>
 * =2 errors and results<br>
 * =3 errors, results and input<br>
 * =4 errors, results, input and debug for procedure <code>fasta()</code><br>
 * =5 errors, results, input and debug for activity coefficients<br>
 * >=6 errors, results, input and full debug print-out<br>
 * Default = Chem.DBGHALTA_DEF = 1 (report errors only)
 * @see Chem#DBGHALTA_DEF Chem.DBGHALTA_DEF */
    public int dbg;
/** set <code>cont = true</code> before calling procedure <code>haltaCalc</code>
 * if the same set of equilibrium solids obtained from the last call to
 * <code>haltaCalc</code> are to be tested first;<br>
 * set <code>cont = false</code> to discard the set of solids at equilibrium found
 * in the last calculation.<br>
 * <b>Note:</b> For the first call to <code>haltaCalc</code> there is no previous set of solids,
 * so <code>cont = true</code> has no effect.<br>
 * If the array <code>kh[]</code> is changed since the last calculation, then
 * the last set of solids is discarded, and setting <code>cont = true</code> has no effect.<br>
 * If there are no errors, <code>haltaCalc</code> sets <code>cont=true</code> else,
 * if the calculation fails it is set to <code>false</code>. So in general the
 * user does not need to change this parameter.
 * @see lib.kemi.haltaFall.HaltaFall#haltaCalc() haltaCalc()
 * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs#errFlags errFlags
 * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs#kh kh */
    public boolean cont;
/** If the HaltaFall calculation succeeds, this variable is zero.<br>
 * If the calculation fails, up to 6 error flags are set bitwise
 * in this variable.<br>
 * The meaning of the different flags:<br>
 * 1: the numerical solution is uncertain.<br>
 * 2: too many iterations when solving the mass balance equations.<br>
 * 3: failed to find a satisfactory combination of solids.<br>
 * 4: too many iterations trying to find the solids at equilibrium.<br>
 * 5: some aqueous concentration(s) are too large (>20): uncertain activity coefficients<br>
 * 6: activity factors did not converge.<br>
 * 7: calculation interrupted by the user.<br>
 * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs#errFlagsSet(int) errFlagsSet
 * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs#errFlagsClear(int) errFlagsClear
 * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs#isErrFlagsSet(int) isErrFlagsSet
 * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs#errFlagsToString() errFlagsToString
 * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs#errFlagsGetMessages() errFlagsGetMessages
 * @see lib.kemi.haltaFall.Factor#MAX_CONC MAX_CONC */
    public int errFlags;
/** If it is <b><code>true</code></b> activity coefficents (ionic strength effects) will
 * be calculated by <code>HaltaFall</code> using using the provided instance of
 * <code>Factor</code>. If it is <b><code>false</code></b> then the calculations in 
 * <code>HaltaFall</code> are made for ideal solutions (all activity coefficients = 1)
 * and <code>Factor</code> is never called during the iterations.
 * @see lib.kemi.chem.Chem.Diagr#activityCoeffsModel Chem.Diagr.activityCoeffsModel
 * @see lib.kemi.chem.Chem.Diagr#ionicStrength Chem.Diagr.ionicStrength
 * @see lib.kemi.haltaFall.Factor#ionicStrengthCalc haltaFall.Factor.ionicStrengthCalc */
    public boolean actCoefCalc = false;
 /** The tolerance being used to iterate activity coefficient calculations.
  * For systems where the highest concentration for a ionic species is less
  * than 1 (mol/L), the tolerance is 0.001 in log-10 scale. If one or more concentration
  * for a ionic species <nobr>(C[i])</nobr> is larger than 1, then
  * <nobr><code>tolLogF = 0.001*C[i]</code>.</nobr>  For example, if the highest
  * ionic concentration is 12 mol/L, then <code>tolLogF = 0.012</code>.
  * @see #actCoefCalc actCoefCalc
  * @see #logf logf
  * @see lib.kemi.haltaFall.HaltaFall#TOL_LNG TOL_LNG */
   public double tolLogF;

/** An inner class to store the chemical concentrations associated
* with a chemical system. Used by <code>HaltaFall</code>.
* @see lib.kemi.chem.Chem.ChemSystem ChemSystem
* @see lib.kemi.haltaFall.HaltaFall HaltaFall
* @throws lib.kemi.chem.Chem.ChemicalParameterException */
    private ChemConcs() throws ChemicalParameterException {
        /** number of chemical components */
        int na = ChemSystem.this.Na;
        /** the total number of species: components (soluble + solid)
         * + soluble complexes + solid products */
        int ms = ChemSystem.this.Ms;
        if(na<0 || ms<0) { //this should not occur if the enclosing class is ok
            throw new ChemicalParameterException(
            "Error in \"ChemConcs\"-constructor: na="+na+", ms="+ms+nl+"   All must be >=0.");
        }
        if(na>ms) { //this should not occur if the enclosing class is ok
            throw new ChemicalParameterException(
            "Error in \"ChemConcs\"-constructor: na="+na+", ms="+ms+nl+"   ms must be >= na.");
        }
        this.kh = new int[na];
        this.tot = new double[na];
        this.tol = 1e-4;
        this.solub = new double[na];
        this.C = new double[ms];
        this.logA = new double[ms];
        this.logf = new double[ms];
        this.dbg = DBGHALTA_DEF;
        this.cont = false;
        this.errFlags = 0;
        this.actCoefCalc = false;
        }

    //<editor-fold defaultstate="collapsed" desc="errFlagsSet(i)">
    /** Sets an error flag
     * @param i the error flag: a value between 1 and 7
     * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs#errFlags errFlags */
    public void errFlagsSet(int i) {
        if(i ==1) {errFlags |=1;}
        else if(i ==2) {errFlags |=2;}
        else if(i ==3) {errFlags |=4;}
        else if(i ==4) {errFlags |=8;}
        else if(i ==5) {errFlags |=16;}
        else if(i ==6) {errFlags |=32;}
        else if(i ==7) {errFlags |=64;}
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="isErrFlagsSet(i)">
    /** Returns true if the error flag is set, false otherwise
     * @param i the error flag: a value between 1 and 7
     * @return true if the error flag is set, false otherwise
     * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs#errFlags errFlags  */
    public boolean isErrFlagsSet(int i) {
        boolean b = false;
        if(i ==1) {if((errFlags & 1) == 1) {b = true;}}
        else if(i ==2) {if((errFlags & 2) == 2) {b = true;}}
        else if(i ==3) {if((errFlags & 4) == 4) {b = true;}}
        else if(i ==4) {if((errFlags & 8) == 8) {b = true;}}
        else if(i ==5) {if((errFlags & 16) == 16) {b = true;}}
        else if(i ==6) {if((errFlags & 32) == 32) {b = true;}}
        else if(i ==7) {if((errFlags & 64) == 64) {b = true;}}
        return b;
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="errFlagsClear(i)">
    /** Clears an error flag
     * @param i the error flag: a value between 1 and 7
     * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs#errFlags errFlags  */
    public void errFlagsClear(int i) {
        if(i ==1) {errFlags &= ~1;}
        else if(i ==2) {errFlags &= ~2;}
        else if(i ==3) {errFlags &= ~4;}
        else if(i ==4) {errFlags &= ~8;}
        else if(i ==5) {errFlags &= ~16;}
        else if(i ==6) {errFlags &= ~32;}
        else if(i ==7) {errFlags &= ~64;}
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="errFlagsToString()">
    /** Returns a text line with the error flags, bitwise as 0 or 1
     * @return the message "errFlags (1 to 6): 0000000" with zeros replaced by "1"
     * for each flag that is set.
     * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs#errFlags errFlags */
    public String errFlagsToString() {
        String t = "errFlags (1 to 6): ";
        if((errFlags & 1) == 1) {t = t+ "1";} else {t = t + "0";}
        if((errFlags & 2) == 2) {t = t+ "1";} else {t = t + "0";}
        if((errFlags & 4) == 4) {t = t+ "1";} else {t = t + "0";}
        if((errFlags & 8) == 8) {t = t+ "1";} else {t = t + "0";}
        if((errFlags &16) ==16) {t = t+ "1";} else {t = t + "0";}
        if((errFlags &32) ==32) {t = t+ "1";} else {t = t + "0";}
        if((errFlags &64) ==64) {t = t+ "1";} else {t = t + "0";}
        return t;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="errFlagsGetMessages()">
    /** Returns a message with one text line for each error flag that is set,
     * separated with new-lines
     * @return the error messages or <code>null</code> ir no errFlags are set
     * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs#errFlags errFlags */
    public String errFlagsGetMessages() {
        if(errFlags <= 0) {return null;}
        String t = "";
        if((errFlags & 1) == 1) {
            t = t+ "The numerical solution is uncertain.";
        }
        if((errFlags & 2) == 2) {
            if(t.length()>0) {t=t+nl;}
            t = t+ "Too many iterations when solving the mass balance equations.";
        }
        if((errFlags & 4) == 4) {
            if(t.length()>0) {t=t+nl;}
            t = t+ "Failed to find a satisfactory combination of solids.";
        }
        if((errFlags & 8) == 8) {
            if(t.length()>0) {t=t+nl;}
            t = t+ "Too many iterations trying to find the solids at equilibrium.";
        }
        if((errFlags &16) ==16) {
            if(t.length()>0) {t=t+nl;}
            t = t+ "Some aqueous concentration(s) too large (>50): uncertain activity coefficients.";
        }
        if((errFlags &32) ==32) {
            if(t.length()>0) {t=t+nl;}
            t = t+ "Activity factors did not converge.";
        }
        if((errFlags &64) ==64) {
            if(t.length()>0) {t=t+nl;}
            t = t+ "Calculation interrupted by the user.";
        }
        if(t.trim().length()<=0) {return null;} else {return t;}
    }
    //</editor-fold>

    } // class ChemConcs
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="inner class NamesEtc">

/** An inner class to "Chem" that contains diverse information associated
 *  with a chemical system: names of species, electric charge, etc */
public class NamesEtc {
/** Names of the chemical components */
    public String[] identC;
/** Names of all species ident[ms]
 * @see lib.kemi.chem.Chem.ChemSystem#Ms Ms
 */
    public String[] ident;
/** The length of the name of a species excluding spaces between name
 * and charge. For example for "Fe 3+" nameLength = 4. */
    public int[] nameLength;
/** Which species are the components: this is needed for GASOL (SolgasWater);
 * but in HaltaFall the components are the 1st Na species. */
    public int[] iel;
/** The electric charges of all aqueous species.  Plus the electric charge of
 * two fictive species (Na+ and Cl-) that are used to ensure electrically neutral
 * aqueous solutions (electroneutrality) when calculating the ionic strength and
 * activity coefficients */
    public int[] z;
/** The comment in the input file (if any) corresponding to this species */
    public String[] comment;

/** An inner class that contains diverse information associated
 * with a chemical system: the names of the species and the components, the
 * electric charge of each species, informatio on the diagram to be drawn, etc.
 * @param na int number of chemical components
 * @param ms int the total number of species: components (soluble + solid)
 *     + soluble complexes + solid products
 * @param mSol int the number of solids (components + reaction products)
 * @throws lib.kemi.chem.Chem.ChemicalParameterException */
public NamesEtc (int na, int ms, int mSol) throws ChemicalParameterException {
    if(na<0 || ms<0 || mSol<0) {
        throw new ChemicalParameterException(
            "Error in \"NamesEtc\"-constructor: na="+na+", ms="+ms+", mSol="+mSol+nl+"   All must be >=0.");
    }
    if(ms<na || ms<mSol) {
        throw new ChemicalParameterException(
            "Error in \"NamesEtc\"-constructor: na="+na+", ms="+ms+", mSol="+mSol+nl+"   ms must be >na and >mSol.");
    }
    identC = new String[na];
    ident = new String[ms];
    comment = new String[ms];
    nameLength = new int[ms];
    // ---- Which species are the components
    //      (this is needed for GASOL; in HALTA the components
    //      are the first "na" species of the list)
    iel = new int[na];
    for(int i =0; i < na; i++) {iel[i] =i;}

    int nx = ms - na - mSol; // soluble complexes
    int nIons = na + nx; // = (ms-mSol)
    //Note: The number of soluble species is (nIons+2).  Must add the
    // electric charge of two fictive species (Na+ and Cl-)
    // that are used to ensure electrically neutral aqueous solutions
    // when calculating the ionic strength and activity coefficients
    z = new int[nIons+2]; // add two electroneutrality species (Na+ and Cl-)
} // constructor

//<editor-fold defaultstate="collapsed" desc="printNamesEtc">
/** Print data defining a chemical system
 * @param out a PrintStrem, such as <code>System.out</code>.
 * If null, <code>System.out</code> is used. */
public void printNamesEtc(java.io.PrintStream out) {
  if(out == null) {out = System.out;}
  out.flush();
  java.util.Locale e = java.util.Locale.ENGLISH;
   //access the enclosing class from the inner class
  int Na = Chem.this.chemSystem.Na;
  int Ms = Chem.this.chemSystem.Ms;
  int mSol = Chem.this.chemSystem.mSol;
  int n0, nM, iPl, nP;
  out.println("components: names="); out.print("    ");
      n0 = 0;     //start index to print
      nM = Na-1;  //end index to print
      iPl = 5; nP= nM-n0; if(nP >=0) { //items_Per_Line and itemsto print
      print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
          out.format(e," %15s",identC[kjj]);
          if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
          out.println(); out.print("    ");} //for ijj
      }
  int nx = Ms - Na - mSol; // soluble complexes
  out.println("complexes: names="); out.print("    ");
      n0 = Na;     //start index to print
      nM = Na+nx+mSol-1;  //end index to print
      iPl = 5; nP= nM-n0; if(nP >=0) {
      print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
          out.format(e," %15s",ident[kjj]);
          if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
          out.println(); out.print("    ");} //for ijj
      }
  out.println("soluble species: z="); out.print("    ");
      n0 = 0;     //start index to print
      nM = Na+nx+2-1;  //end index to print
      iPl = 20; nP= nM-n0; if(nP >=0) {
      print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
          out.format(e," %3d",z[kjj]);
          if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
          out.println(); out.print("    ");} //for ijj
      }
  out.flush();
} //printNamesEtc()
//</editor-fold>

} // class NamesEtc
//</editor-fold>
} // class ChemSystem
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="inner class Diagr">

/** An inner class that contains diverse information associated
 * with a diagram: components in the axes, diagram type, etc.
 * A sister class, DiagrConcs, contains array data. */
public class Diagr implements Cloneable {
/** <pre>plotType=0 Predom diagram;      compMain= main component
plotType=1 fraction diagram;    compY= main component
plotType=2 log solubility diagram
plotType=3 log (conc) diagram
plotType=4 log (ai/ar) diagram; compY= reference species
plotType=5 pe in Y-axis
plotType=6 pH in Y-axis
plotType=7 log (activity) diagram
plotType=8 H-affinity diagram</pre> */
public int plotType;
/** the component in the X-axis in the diagram */
public int compX;
/** the component in the Y-axis in a Predom diagram.
 * For a SED diagram it is either the component for which the fractions
 * are calculated, or it is the reference species in a relative activity diagram. */
public int compY;
/** the "main" component in a Predom diagram: the component for which the
 * dominating species or existing solids are determined and plotted */
public int compMain;
/** For a Predominance area diagram: if <code>oneArea</code> &gt;=0 this value indicates
 * which species the user has requested (in the input file) to plot as a single
 * predominance area. <code>oneArea</code> &lt;= -1 if the user made no such request. */
public int oneArea;
/** the lowest value in the Y-axis */
public double yLow;
/** the highest value in the Y-axis */
public double yHigh;
/** Eh: true if Eh is displayed either in the axes or in the caption;
 * false if "pe" is used instead. */
public boolean Eh;
/** a line of text used as title (caption) above the diagram.
 * It is the first line of text (if any) found after the plot information in the input data file
* @see lib.kemi.chem.Chem.Diagr#endLines endLines */
public String title;
/** any lines of text that may be found after the title line in the input data file
 * @see lib.kemi.chem.Chem.Diagr#title title */
public String endLines;
/** <pre> =1 if the comment at the end of the first line contains
 *    either "HYDRA" or "DATABASE";
 * =2 if the comment contains either "MEDUSA" or "SPANA";
 * =0 otherwise.</pre>
 * Not to be changed
 * @see lib.kemi.readDataLib.ReadDataLib#fileIsDatabaseOrSpana fileIsDatabaseOrSpana */
public int databaseSpanaFile;
/** True if the range of values in the Y-axis is given in the input data file */
public boolean inputYMinMax;
/** pInX: 0 = "normal" axis; 1 = pH in axis; 2 = pe in axis;
 * 3 = Eh in axis. */
public int pInX;
/** pInY: 0 = "normal" axis; 1 = pH in axis; 2 = pe in axis;
 * 3 = Eh in axis. */
public int pInY;
/** which species is H+ */
public int Hplus;
/** which species is OH- */
public int OHmin;
/** True if the species names indicate that this system is an aqueous system,
 * (and not a gaseous phase, etc) */
public boolean aquSystem;
/** temperature in Celsius */
public double temperature;
/** pressure in bar */
public double pressure;
/** The ionic strength given by the user to make the calculations.
 * If the ionic strength is negative (e.g. =-1) then it is calculated.
 * @see lib.kemi.chem.Chem.Diagr#activityCoeffsModel Chem.Diagr.activityCoeffsModel
 * @see lib.kemi.haltaFall.Factor#ionicStrengthCalc haltaFall.Factor.ionicStrengthCalc */
public double ionicStrength;
/** Model to calculate activity coefficents (ionic strength effects):<ul>
 * <li> &lt;0 for ideal solutions (all activity coefficients = 1)
 * <li> =0 Davies eqn.
 * <li> =1 SIT (Specific Ion interaction "Theory")
 * <li> =2 Simplified HKF (Helson, Kirkham and Flowers)
 * </ul>
 * @see lib.kemi.chem.Chem.Diagr#ionicStrength Chem.Diagr.ionicStrength
 * @see lib.kemi.haltaFall.Factor#ionicStrengthCalc haltaFall.Factor.ionicStrengthCalc */
public int activityCoeffsModel = -1;
  /** the minimum fraction value that a species must reach
   * to be displayed in a fraction diagram. A value of 0.03 means that
   * a species must have a fraction above 3% in order to be displayed
   * in a fraction diagram. */
public float fractionThreshold = 0.03f;
/** An inner class that contains diverse information associated
 *  with a diagram: components in the axes, diagram type, etc */
public Diagr(){
//    predom = false;
    plotType = -1;
    compX = -1; compY = -1; compMain = -1;
    oneArea = -1;
    yLow = Double.NaN; yHigh = Double.NaN;
    Eh = true;
    title = null;
    endLines = null;
    databaseSpanaFile = 0;
    inputYMinMax = false;
    pInX =0; pInY =0;
    Hplus = -1; OHmin = -1;
    aquSystem = false;
    ionicStrength = 0;
    temperature = 25;
    pressure = Double.NaN;
    activityCoeffsModel = -1;
    fractionThreshold = 0.03f;
} // constructor
@Override public Object clone() throws CloneNotSupportedException {
    super.clone();
    Diagr d = new Diagr();
    d.plotType = this.plotType;
    d.compX = this.compX;
    d.compY = this.compY;
    d.compMain = this.compMain;
    d.oneArea = this.oneArea;
    d.yLow = this.yLow;
    d.yHigh = this.yHigh;
    d.Eh = this.Eh;
    d.title = this.title;
    d.endLines = this.endLines;
    d.databaseSpanaFile = this.databaseSpanaFile;
    d.inputYMinMax = this.inputYMinMax;
    d.pInX = this.pInX;
    d.pInY = this.pInY;
    d.Hplus = this.Hplus;
    d.OHmin = this.OHmin;
    d.aquSystem = this.aquSystem;
    d.ionicStrength = this.ionicStrength;
    d.temperature = this.temperature;
    d.pressure = this.pressure;
    d.fractionThreshold = this.fractionThreshold;
    return d;
} // clone()

public boolean isEqualTo(Diagr another) {
    return another != null &&
            this.plotType == another.plotType &&
            this.compX == another.compX &&
            this.compY == another.compY &&
            this.compMain == another.compMain &&
            this.oneArea == another.oneArea &&
            this.yLow == another.yLow &&
            this.yHigh == another.yHigh &&
            this.Eh == another.Eh &&
            Util.stringsEqual(this.title, another.title) &&
            Util.stringsEqual(this.endLines, another.endLines) &&
            this.databaseSpanaFile == another.databaseSpanaFile &&
            this.inputYMinMax == another.inputYMinMax &&
            this.pInX == another.pInX &&
            this.pInY == another.pInY &&
            this.Hplus == another.Hplus &&
            this.OHmin == another.OHmin &&
            this.aquSystem == another.aquSystem &&
            this.ionicStrength == another.ionicStrength &&
            this.temperature == another.temperature &&
            this.pressure == another.pressure &&
            this.fractionThreshold == another.fractionThreshold;
}

/** @param out a PrintStrem, such as <code>System.out</code>.
 * If null, <code>System.out</code> is used. */
public void printPlotType(java.io.PrintStream out) {
  if(out == null) {out = System.out;}
  out.flush();
  //access the enclosing class from the inner class
  int Na = Chem.this.chemSystem.Na;
  out.println("Plot data:");
  String[] plotTypes = {"Predom","Fraction","log solub.","log conc.","log (ai/ar)","calc.pe","calc.pH","log act.","H-aff."};
  String t; if(plotType >=0 && plotType <=8) {t = plotTypes[plotType];} else {t = "undefined";}
  out.print("  plot type = "+plotType+" ("+t+");");
  if(!Double.isNaN(yLow)) {out.print("   yLow = "+yLow);} else {out.print("   yLow = NaN");}
  if(!Double.isNaN(yHigh)) {out.println("   yHigh = "+yHigh);} else {out.println("   yHigh = NaN");}
  out.println("  compX = "+compX+"  compY = "+compY+"  compMain = "+compMain);
  out.println("  oneArea = "+oneArea+"  fractionThreshold = "+fractionThreshold);
  out.flush();
} //printPlotType()

} // class Diagr
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="inner class DiagrConcs">

/** An inner class containing arrays with information on the concentrations
 * for each component in a diagram and how ("hur" in Swedish) they are varied.
 * A sister class, Diagr, contains non-array data: which components are
 * in the axes, diagram type, etc */
public class DiagrConcs implements Cloneable {
/** Concentration types for each component:
<pre>hur =1 for "T" (fixed Total conc.)
hur =2 for "TV" (Tot. conc. Varied)
hur =3 for "LTV" (Log(Tot.conc.) Varied)
hur =4 for "LA" (fixed Log(Activity) value)
hur =5 for "LAV" (Log(Activity) Varied)</pre> */
    public int[] hur;
/** For each component either:<ul>
 * <li>the fixed total concentration, if hur[i]=1
 * <li>the fixed log(activity), if hur[i]=4
 * <li>the lowest value for either the total concentration, the log(Tot.Conc.)
 * or the log(activity) when these are varied, if hur[i]=2, 3 or 5, respectively
 * </ul>  */
    public double[] cLow;
/** For each component either:<ul>
 * <li>undefined, if hur[i]=1 or 4
 * <li>the highest value for either the total concentration, the log(Tot.Conc.)
 * or the log(activity) when these are varied, if hur[i]=2, 3 or 5, respectively
 * </ul>  */
    public double[] cHigh;

/** An inner class to "Chem" containing arrays with information on the
 * concentrations for each component in a diagram and how (hur) they are varied.
 * A sister class, Diagr, contains non-array data: which components are
 * in the axes, diagram type, etc
 * @param Na int number of chemical components
 * @throws lib.kemi.chem.Chem.ChemicalParameterException */
public DiagrConcs(int Na) throws ChemicalParameterException {
    if(Na<0) {
        throw new ChemicalParameterException(
            "Error in \"DiagrConcs\"-constructor: Na="+Na+". Must be >=0.");
    }
    hur = new int[Na];
    cLow = new double[Na];
    cHigh = new double[Na];
    for(int i = 0; i < Na; i++) {hur[i]=-1; cLow[i]=Double.NaN; cHigh[i]=Double.NaN;}
} // constructor
@Override public Object clone() throws CloneNotSupportedException {
    super.clone();
    int Na = this.hur.length;
    DiagrConcs dc;
    try {dc = new DiagrConcs(Na);}
    catch(ChemicalParameterException ex) {
        //this should not happen
        return null;
    }
    System.arraycopy(this.hur, 0, dc.hur, 0, Na);
    System.arraycopy(this.cLow, 0, dc.cLow, 0, Na);
    System.arraycopy(this.cHigh, 0, dc.cHigh, 0, Na);
    return dc;
} // clone()
public boolean isEqualTo(DiagrConcs another) {
    if(another != null) {
      boolean ok = true;
      if(!java.util.Arrays.equals(this.hur, another.hur)) {
          //System.out.println(" hur not equal");
          ok = false;
      }
      if(ok && cLow.length != another.cLow.length) {
          //System.out.println("cLow length not equal");
          ok = false;
      }
      if(ok && cHigh.length != another.cHigh.length) {
          //System.out.println("cHigh length not equal");
          ok = false;
      }
      if(ok && cLow.length >0) {
        for(int i=0; i < cLow.length; i++) {
          if(!Util.areEqualDoubles(cLow[i], another.cLow[i])) {
              //System.out.println("cLow not equal i="+i+", cLow[i]="+cLow[i]+", another.cLow[i]="+another.cLow[i]);
              ok = false; break;}
        } //for i
        //if(!ok) {System.out.println("cLow not equal");}
      }
      if(ok && cHigh.length >0) {
        for(int i=0; i < cHigh.length; i++) {
          if(!Util.areEqualDoubles(cHigh[i], another.cHigh[i])) {ok = false; break;}
        } //for i
        //if(!ok) {System.out.println("cHigh not equal");}
      }
      return ok;
    }
    return false;
} // isEqualTo(DiagrConcs)

@Override
public String toString() {
  //access the enclosing class from the inner class
  int Na = Chem.this.chemSystem.Na;
  String txt = "concentrations for components:";
  String[] concTypes = {" - ","T","TV","LTV","LA","LAV"};
  String t;
  for(int i=0; i < Na; i++) {
    if(hur[i] >=1 && hur[i] <=5) {t = concTypes[hur[i]];} else {t = concTypes[0];}
    txt = txt + nl +
          " i="+i+" hur = "+hur[i]+" ("+t+"), cLow="+cLow[i]+", cHigh="+cHigh[i];
  }//for i
  return txt;
}

} //class DiagrConcs
//</editor-fold>

} 