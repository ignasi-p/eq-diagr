package lib.kemi.haltaFall;

import lib.kemi.chem.Chem;

/** <code>HaltaFall</code> ("Concentrations and precipitates" in Swedish)
 * calculates the equilibrium composition of a chemical system with
 * fluid and solid phases. It is essentially the Java translation of
 * the <code>HALTAFALL</code> program, published in:<ul>
 * <li>  N.Ingri, W.Kakolowicz, L.G.Sillen and B.Warnqvist, "High-Speed Computers
 *     as a Supplement to Graphical Methods - V. HALTAFALL, a General Program
 *     for Calculating the Composition of Equilibrium Mixtures".
 *     <i>Talanta</i>, <b>14</b> (1967) 1261-1286
 * <li>  N.Ingri, W.Kakolowicz, L.G.Sillen and B.Warnqvist, "Errata".
 *     <i>Talanta</i>, <b>15</b> (1968) xi-xii
 * <li>  R.Ekelund, L.G.Sillen and O.Wahlberg, "Fortran editions of Haltafall and
 *     Letagrop".  <i>Acta Chemica Scandinavica</i>, <b>24</b> (1970) 3073
 * <li>  B.Warnqvist and N.Ingri, "The HALTAFALL program - some corrections, and
 *     comments on recent experience".
 *     <i>Talanta</i>, <b>18</b> (1971) 457-458
 * </ul>
 * However, in this version the supersaturated solid phases are picked-up
 * one at a time, instead of all at once.
 * <p>
 * The input data is supplied through an instance of <code>Chem.ChemSystem</code>
 * and its nested class <code>Chem.ChemSystem.ChemConcs</code>. The equilibrium composition
 * is calculated by method <code>haltaCalc</code> and stored in the arrays of
 * the <code>ChemConcs</code> instance.
 * <b>Note</b> that if the contents of <code>ChemSystem</code> is
 * changed <i>after</i> the creation of the <code>HaltaFall</code> object,
 * unpredictable results and errors may occur. The contents of <code>ChemConcs</code>
 * should however be changed <i>before</i> every call to method <code>haltaCalc</code>.
 * <p>
 * A <code>HaltaFall</code> object is also associated with an instance of
 * a class <code>Factor</code> having a method <code>factor</code> that
 * calculates the activity coefficients. In its simplest form:
 * <pre>
 *  public class Factor {
 *      public Factor() {}
 *      public void factor(double[] C, double[] lnf) {
 *          for(int i=0; i&lt;lnf.length; i++) {lnf[i]=0;}
 *      }
 *  }
 * </pre>
 *
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
 * @see lib.kemi.chem.Chem.ChemSystem ChemSystem
 * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs ChemConcs
 * @author Ignasi Puigdomenech */
public class HaltaFall {
 //<editor-fold defaultstate="collapsed" desc="private fields">
 /** an instance of a class to store data defining a chemical system */
  private Chem.ChemSystem cs;
 /** an instance of a class to store concentration data about a chemical system */
  private Chem.ChemSystem.ChemConcs c;
 /** where messages will be printed. It may be "System.out" */
  private java.io.PrintStream out;
 /** an instance of class <code>Factor</code> used to calculate activity coefficients */
  private Factor factor = null;

 /** If <code>true</code> only one solid is allowed to precipitate at
  * each call of <code>fasta()</code>; if <code>false</code> all supersaturated
  * solids are allowed to precipitate. In many cases 10-20 solids are found to
  * be supersaturated in a system with less than 5 chemical components.
  * This leads to long iterations trying to select the few solids that
  * precipitate and to reject the 15 or more solids that do not precipitate. */
  private final boolean ONLY_ONE_SOLID_AT_A_TIME = true;
 /** Used in procedure <code>kille()</code>: When the stepwise changes of x
  * are smaller than <code>STEGBYT</code> the procedure switches to
  * the chord method. Should be small enough to avout rounding errors
  * @see lib.kemi.haltaFall.HaltaFall#x x
  * @see lib.kemi.haltaFall.HaltaFall#y y
  * @see lib.kemi.haltaFall.HaltaFall#y0 y0
  * @see lib.kemi.haltaFall.HaltaFall#STEG0 STEG0
  * @see lib.kemi.haltaFall.HaltaFall#steg steg
  * @see lib.kemi.haltaFall.HaltaFall#catchRoundingErrors catchRoundingErrors
  * @see lib.kemi.haltaFall.HaltaFall#kille() kille() */
  private final double STEGBYT = 0.05;
 /** starting value of steg[] in the iterations of procedure kille
  * when dealing with a new chemical system, that is, when
  * <code>c.cont = false</code>.
  * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs#cont cont
  * @see lib.kemi.haltaFall.HaltaFall#kille() kille()
  * @see lib.kemi.haltaFall.HaltaFall#x x
  * @see lib.kemi.haltaFall.HaltaFall#y y
  * @see lib.kemi.haltaFall.HaltaFall#y0 y0
  * @see lib.kemi.haltaFall.HaltaFall#STEG0_CONT STEG0_CONT
  * @see lib.kemi.haltaFall.HaltaFall#STEGBYT STEGBYT
  * @see lib.kemi.haltaFall.HaltaFall#steg steg */
  private final double STEG0 = 0.1;
 /** starting value of steg[] in the iterations of procedure kille
  * when dealing with the same chemical system as last calculation, that is,
  * when <code>c.cont = true</code>.
  * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs#cont cont
  * @see lib.kemi.haltaFall.HaltaFall#kille() kille()
  * @see lib.kemi.haltaFall.HaltaFall#x x
  * @see lib.kemi.haltaFall.HaltaFall#y y
  * @see lib.kemi.haltaFall.HaltaFall#y0 y0
  * @see lib.kemi.haltaFall.HaltaFall#STEG0 STEG0
  * @see lib.kemi.haltaFall.HaltaFall#STEGBYT STEGBYT
  * @see lib.kemi.haltaFall.HaltaFall#steg steg */
  private final double STEG0_CONT = 0.001;
 /** maximum number of iterations in procedure kille */
  private static final int ITER_MAX =60; // it is perhaps ok with 40
 /** maximum number of iterations when calculating activity coefficients */
  private static final int ITERAC_MAX = 75;
 /** maximum number of "InFall" iterations in procedure fasta() */
  private static final int ITER_FASTA_MAX = 100;
 /** The default value for the tolerance (in natural log scale) when iterating
  * activity coefficients (lnG[]).<br>
  * <code>TOL_LNG = 0.0023026</code> (=0.001 in log10 scale).
  * @see #lnG lnG
  * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs#tolLogF tolLogF
  * @see #actCoeffs() actCoeffs() */
  private final double TOL_LNG =  0.0023026; // =0.001 in log10 scale

  private final int MXA;  // nax nbr components
  private final int MXX;  // max nbr species
  private final int MXS;  // max nbr of solids
  private final int MXC;  //= MXX-MXA-MXS; // max nbr aqueous complexes
  private final int MXAQ; //= MXX-MXS; // max nbr aqueous species (comps.+complexes)

//-- booleans in alphabetical order:
 /** if <b><code>true</code></b> the ongoing calculations will be stopped at
  * the earliest oportunity.
  * @see lib.kemi.haltaFall.HaltaFall#haltaCancel() haltaCancel()  */
  private boolean panic = false;
 /** <b><code>true</code></b> if matrix "ruta" in procedure fasta() has come out singular
  * @see lib.kemi.haltaFall.HaltaFall#ruta ruta */
  private boolean singFall;

//-- integers in alphabetical order:
 /** component for which tot[ivar] is being tested and lnA[ivar] adjusted
  * @see lib.kemi.haltaFall.HaltaFall#iva iva
  * @see lib.kemi.haltaFall.HaltaFall#ivaBra ivaBra
  * @see lib.kemi.haltaFall.HaltaFall#ivaNov ivaNov */
  private int ivar;
 /** iteration counter when performing activity coefficient calculations */
  private int iterAc;
 /** iterations counter when calling procedure fasta(), that is,
  * how many different sets of mass balance equations have been solved */
  private int iterFasta;
 /** flag to indicate degrees of success or failure from some procedures */
  private int indik;
  /** The routine Fasta cycles through chunks of code in the original code,
   * using the value of nextFall  as a guide in each case. The meanings
   * of nextFall on return to this routine are as follows:
   *   nextFall   Next Routine       Line No. in original HaltaFall
   *     0        Exit to PROV,          9000, 10000, 25000
   *              indik set to ready
   *     1        fallProv               14000
   *     2        beFall                 16000
   *     3        anFall                 24000
   *     4        utFall                 17000
   *     5        sing                   18000
   *     6        fUtt                   20000  */
  private int nextFall;
 /** number of solids present at equilibrium
  * @see lib.kemi.haltaFall.HaltaFall#nvaf nvaf  */
  private int nfall;
 /** nbr of solids indicated at INFALL in procedure fasta() */
  private int nfSpar = 0;
 /** number of "ions", that is, species for which the activity coefficients
  * need to be calculated */
  private int nIon;
 /** number of solids systematically eliminated at some stage while singFall
  * is true in procedure fasta()
  * @see lib.kemi.haltaFall.HaltaFall#fut fut
  * @see lib.kemi.haltaFall.HaltaFall#singFall singFall */
  private int nUt;
 /** nr of mass balance equations for tot[ia] to be tested (lnA[ivar] to be varied)
  * in absence of solids at equilibrium */
  private int nva;
 /** nbr of mass balance equations for tot[ia] to be tested (lnA[ivar] to be varied)
  * in the presence of solids at equilibrium (nvaf = nva - nfall)
  * @see lib.kemi.haltaFall.HaltaFall#nfall nfall
  * @see lib.kemi.haltaFall.HaltaFall#nva nva  */
  private int nvaf;

//-- doubles in alphabetical order:
 /** independent variable in y(x)=y0 (in procedure kille)
  * @see lib.kemi.haltaFall.HaltaFall#kille() kille()
  * @see lib.kemi.haltaFall.HaltaFall#y y
  * @see lib.kemi.haltaFall.HaltaFall#y0 y0 */
  private double x;
 /** dependent variable in y(x)=y0 (in procedure kille)
  * @see lib.kemi.haltaFall.HaltaFall#kille() kille()
  * @see lib.kemi.haltaFall.HaltaFall#x x
  * @see lib.kemi.haltaFall.HaltaFall#y0 y0 */
  private double y;
 /** value for y aimed at in y(x)=y0 (in procedure kille)
  * @see lib.kemi.haltaFall.HaltaFall#kille() kille()
  * @see lib.kemi.haltaFall.HaltaFall#x x
  * @see lib.kemi.haltaFall.HaltaFall#y y */
  private double y0;

//-- boolean arrays in alphabetical order:
 /** true if lnA[] is calculated from a solubility product (eqn.(14))
  * @see lib.kemi.haltaFall.HaltaFall#ibe ibe */
  private boolean[] ber;
 /** true if the solid is assumed to be present at equilibrium */
  private boolean[] fall;
 /** true if the component is assumed to occur in one or more solids at equilibrium */
  private boolean[] falla;
 /** mono[] = true for components that only take part in mononuclear soluble
  * complexes (a[ix][ia]=0 or +1 for all ix)
  * @see lib.kemi.chem.Chem.ChemSystem#a Chem.ChemSystem.a */
  private boolean[] mono;
 /** true if any of the stoichiometric coeffients involving this component are positive (&gt;0) */
  private boolean[] pos;
 /** true if any of the stoichiometric coeffients involving this component are negative (&lt;0) */
  private boolean[] neg;
 /** true if it has been requested to solve the mass balance equation for this component
  * (that is, kh=1) but the calculation is not possible as seen from the
  * stoichimotric coefficients
  * @see lib.kemi.chem.Chem.ChemSystem.ChemConcs#kh Chem.ChemSystem.ChemConcs.kh */
  private boolean[] noCalc;
  /** the value of logA when noCalc is true */
  private final double NOCALC_LOGA = -9999;
 /** true if the two components are independent as seen from the stoichimotric coefficients
  * (no complex or solid contains both components)
  * @see lib.kemi.haltaFall.HaltaFall#nober nober */
  private boolean[][] ober;

//-- integer arrays in alphabetical order:
 /** control number used in procedures <code>kille</code> and <code>totBer</code>
  * to catch numerical rounding errors:  If two <i>practivally equal</i> lnA[ivar]
  * are found (x1 and x2), that correspond to calculated values (y1 and y2) higher
  * and lower, respectively, than the given tot[ivar] (y0), then x (=lnA[iva])
  * can not be further adjusted, because x1 and x2 are equal. This occurs often
  * if STEGBYT is too large.
  * @see lib.kemi.haltaFall.HaltaFall#kille() kille()
  * @see lib.kemi.haltaFall.HaltaFall#STEGBYT STEGBYT */
  private int[] catchRoundingErrors;
 /** fut[i] = ifSpar number of the i'th solid to be eliminated at some stage in
  * systematic variation during <code>singFall</code> in procedure <code>fasta()</code>
  * @see lib.kemi.haltaFall.HaltaFall#nUt nUt
  * @see lib.kemi.haltaFall.HaltaFall#ifSpar ifSpar
  * @see lib.kemi.haltaFall.HaltaFall#singFall singFall */
  private int[] fut;
 /** component numbers for which tot[ia] is to be calculated
  * by means of the solutbility product (0 to nfall-1)
  * @see lib.kemi.haltaFall.HaltaFall#nva nva  */
  private int[] ibe;
 /** <code>iber[j]</code> = j'th of <code>iva</code> numbers to be an <code>ibe</code>
  * <code>(ibe[j] = iva[iber[j]], j= 0 to nfall-1)</code>
  * @see lib.kemi.haltaFall.HaltaFall#nfall nfall
  * @see lib.kemi.haltaFall.HaltaFall#ibe ibe
  * @see lib.kemi.haltaFall.HaltaFall#iva iva  */
  private int[] iber;
 /** numbers of the solids present at equilibrium (0 to nfall-1)
  * @see lib.kemi.haltaFall.HaltaFall#nfall nfall  */
  private int[] ifall;
 /** In some calculations where the tolerance is high, a solid
  * might be found bot supersaturated and with zero or slightly negative
  * concentration, and this leads to a loop. To avoid this situation, the
  * sets of solids found in the last two iterations at routine fasta() are
  * stored, so that the algorithm can decide that the solid is at the limit
  * of saturation, given the tolerance chosen.
  * numbers of the solids present at equilibrium (0 to nfall-1)
  * @see lib.kemi.haltaFall.HaltaFall#ifall ifall
  * @see lib.kemi.haltaFall.HaltaFall#nfall nfall  */
  private int[][] ifallSpar;  
 /** nbr of the solids indicated as possible after INFALL in proceure <code>fasta()</code>,
  * (needed when <code>singFall</code>)
  * @see lib.kemi.haltaFall.HaltaFall#nUt nUt
  * @see lib.kemi.haltaFall.HaltaFall#fut fut
  * @see lib.kemi.haltaFall.HaltaFall#singFall singFall */
  private int[] ifSpar;
 /** control number used in procedures <code>kille</code> and <code>totBer</code>
  * to catch too many iterations */
  private int[] iter;
 /** components numbers for which the mass balance equation has to be solved
  * (lnA to be varied) when some solids are present at equilibrium (0 to nvaf-1)
  * @see lib.kemi.haltaFall.HaltaFall#nfall nfall
  * @see lib.kemi.haltaFall.HaltaFall#nvaf nvaf
  * @see lib.kemi.haltaFall.HaltaFall#ibe ibe */
  private int[] ivaf;
 /** components numbers for which the mass balance equation has to be solved
  * (lnA to be varied) in absence of solids (0 to nva-1)
  * @see lib.kemi.haltaFall.HaltaFall#ivaf ivaf
  * @see lib.kemi.haltaFall.HaltaFall#nva nva */
  private int[] iva;
 /** ivaBra[ivar]= the component number to be tested after ivar,
  * if the mass balance for tot[ivar] is satisfied
  * @see lib.kemi.haltaFall.HaltaFall#ivar ivar
  * @see lib.kemi.haltaFall.HaltaFall#iva iva
  * @see lib.kemi.haltaFall.HaltaFall#ivaNov ivaNov  */
  private int[] ivaBra;
 /** ivaNov[ivar]= the component number to be tested after ivar,
  * if the mass balance for tot[ivar] is not satisfied
  * @see lib.kemi.haltaFall.HaltaFall#ivar ivar
  * @see lib.kemi.haltaFall.HaltaFall#iva iva
  * @see lib.kemi.haltaFall.HaltaFall#ivaBra ivaBra  */
  private int[] ivaNov;
 /** control number for solving the equations in procedure kille
  * @see lib.kemi.haltaFall.HaltaFall#kille() kille()  */
  private int[] karl;
 /** number of other components which are independent of this one
  * @see lib.kemi.haltaFall.HaltaFall#ober ober */
  private int[] nober;

//-- double arrays in alphabetical order:
 /** scale factor for solid phases, used when evaluating supersaturation */
  private double[] fscal;
 /** natural logarithm of the activity of the components */
  private double[] lnA;
 /** part of ln(c[]) independent of lnA[ivar] (eqn. 4, procedure lnaBas) */
  private double[] lnBA;
 /** natural logarithm of the formation equilibrium constant of a complex */
  private double[] lnBeta;
 /** ln(activity coeff.): natural logarithm of the single-ion activity coefficients */
  private double[] lnG;
 /** natural logarithm of the equilibrium constant for the dissolution of a solid */
  private double[] lnKf;
 /** term in lnKf', reduced lnKf (eqn 14a, procedure lnaBer) */
  private double[] lnKmi;
 /** the ln(activity coeff.) from the previous iteration of activity coefficient
  * calculations using procedure <code>factor</code> */
  private double[] oldLnG;
 /** matrix combining stoichiometric coefficients and "rut1" (eqn.16b)
  * at ANFALL in procedure fasta()
  * @see lib.kemi.haltaFall.HaltaFall#rut1 rut1 */
  private double[][] pva;
  /** matrix combining stoichiometric coefficients and "ruta" (eqns 16a, 16b)
   * at ANFALL in procedure fasta()
   * @see lib.kemi.haltaFall.HaltaFall#ruta ruta */
  private double[][] rut1;
 /** matrix with stoichiometric coefficients for solid phases for the components
  * that are "be", that is, those components for which the lnA[] are calculated
  * from the solubility products (eqns 11m', 14, 15) at UTFALL in procedure fasta().
  * Note that ruta must be inverted.
  * @see lib.kemi.haltaFall.HaltaFall#singFall singFall */
  private double[][] ruta;
 /** step for adjusting lnA in procedure kille
  * @see lib.kemi.haltaFall.HaltaFall#kille() kille()
  * @see lib.kemi.haltaFall.HaltaFall#STEG0 STEG0
  * @see lib.kemi.haltaFall.HaltaFall#STEGBYT STEGBYT */
  private double[] steg;
 /** tolerance when solving the mass balance equations for tot[] */
  private double[] tolY;
 /** <code>totBe[i]</code> = term for component <code>ibe[i]</code> (eqn. 15) used for calculating
  * <i>cf</i> after FallProv in procedure fasta() */
  private double[] totBe;
 /** term of reduced total concentration in the
  * presence of solids at equilibrium (eqn. 16a, procedure lnaBer) */
  private double[] totVA;
 /** value for x below the right value (in procedure kille)
  * @see lib.kemi.haltaFall.HaltaFall#kille() kille()
  * @see lib.kemi.haltaFall.HaltaFall#x x
  * @see lib.kemi.haltaFall.HaltaFall#y1 y1 */
  private double[] x1;
 /** value for x above the right value (in procedure kille)
  * @see lib.kemi.haltaFall.HaltaFall#kille() kille()
  * @see lib.kemi.haltaFall.HaltaFall#x x
  * @see lib.kemi.haltaFall.HaltaFall#y2 y2 */
  private double[] x2;
 /** the value of x obtained during the last iteration (in procedure kille)
  * @see lib.kemi.haltaFall.HaltaFall#kille() kille()
  * @see lib.kemi.haltaFall.HaltaFall#x x */
  private double[] xOld;
 /** value for y corresponding to x1 (in procedure kille)
  * @see lib.kemi.haltaFall.HaltaFall#kille() kille()
  * @see lib.kemi.haltaFall.HaltaFall#x1 x1 */
  private double[] y1;
 /** value for y corresponding to x2 (in procedure kille)
  * @see lib.kemi.haltaFall.HaltaFall#kille() kille()
  * @see lib.kemi.haltaFall.HaltaFall#x2 x2 */
  private double[] y2;

 /** natural logarithm of 10 */
  private static final double ln10 = Math.log(10);
 /** English locale for debug print-out */
  private java.util.Locale e = java.util.Locale.ENGLISH;
 /** New-line character(s) to substitute "\n" */
  private static final String nl = System.getProperty("line.separator");

  // dbg-values, i.e. for debug print-out:
  private static final int ERR_ONLY_1 = 1;
  private static final int ERR_RESULTS_2 = 2;
  private static final int ERR_RESL_INPUT_3 = 3;
  private static final int ERR_DEBUG_FASTA_4 = 4;
  private static final int ERR_DEBUG_ACT_COEF_5 = 5;
  private static final int ERR_XTRA_DEBUG_6 = 6;
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="HaltaFall - constructor">
/** Constructs an instance of <code>HaltaFall</code>.
 * @param cs an instance of chem.Chem.ChemSystem
 * @param factor an instance of haltaFall.Factor
 * @param ut where messages will be printed. It may be "System.out" or null
 * @throws lib.kemi.chem.Chem.ChemicalParameterException
 * @see lib.kemi.chem.Chem.ChemSystem ChemSystem
 * @see lib.kemi.haltaFall.Factor Factor
 * @see lib.kemi.haltaFall.HaltaFall HaltaFall
 */
public HaltaFall (Chem.ChemSystem cs, Factor factor,
                java.io.PrintStream ut)
        throws Chem.ChemicalParameterException {
    if(ut != null) {this.out = ut;} else {this.out = System.out;}
    this.cs = cs;  // get the chemical system and concentrations
    this.c = cs.chemConcs;
    this.factor = factor;
    if(c.dbg >=ERR_RESL_INPUT_3) {out.println(nl+"HaltaFall - object Constructor"+nl+
                                                 "            debug level = "+c.dbg);}
    c.errFlags = 0;
// NYKO
    //check the chemical system
    if(cs.Na <=0 || cs.mSol <0 || cs.nx < 0 || cs.solidC < 0) {
        throw new Chem.ChemicalParameterException(
                    "Error in \"HaltaFall\": Na, nx, mSol, solidC ="+cs.Na+", "+cs.nx+", "+cs.mSol+", "+cs.solidC+nl+
                    "   Na must be >0; nx, mSol and solidC must be >=0.");
    }
    if(cs.nx != (cs.Ms - cs.Na - cs.mSol)) {
        throw new Chem.ChemicalParameterException(
                    "Error in \"HaltaFall\": nx ="+cs.nx+", must be = "+(cs.Ms - cs.Na - cs.mSol)+" (= Ms-Na-mSol)");
    }
    //check length of arrays
    //note that species with names starting with "*" are removed,
    //     so length of arrays may be larger than needed
    if(cs.lBeta.length < (cs.Ms-cs.Na)) {
        throw new Chem.ChemicalParameterException(
                    "Error in \"HaltaFall\": lBeta.length ="+cs.lBeta.length+
                    "   must be >= "+(cs.Ms-cs.Na)+" (= Ms-Na)");
    }
    if(cs.noll.length < cs.Ms) {
        throw new Chem.ChemicalParameterException(
                    "Error in \"HaltaFall\": noll.length ="+cs.noll.length+
                    "   must be >= "+cs.Ms+" (= Ms)");
    }
    if(c.kh.length != cs.Na) {
        throw new Chem.ChemicalParameterException(
                    "Error in \"HaltaFall\": kh.length ="+c.kh.length+
                    "   must be = "+cs.Na+" (= Na)");
    }
    if(c.logA.length < cs.Ms) {
        throw new Chem.ChemicalParameterException(
                    "Error in \"HaltaFall\": logA.length ="+c.logA.length+
                    "   must be >= "+cs.Ms+" (= Ms)");
    }
    //check values of kh[]
    for(int i =0; i < cs.Na; i++) {
        if(c.kh[i] <1 || c.kh[i] >2) {
            throw new Chem.ChemicalParameterException(
                    "Error in \"HaltaFall\": kh["+i+"]="+c.kh[i]+
                    " (must be = 1 or 2). Note: component numbers start at zero.");
        } } //for i
    if(c.dbg >= ERR_RESL_INPUT_3) {
        out.println("Debug output requested from HaltaFall at level "+c.dbg+nl+
                    "   Note that arrays start at \"zero\":  numbers for"+nl+
                    "   components, complexes and solids start with 0.");
    }
    MXA = cs.Na;   // nbr components
    MXS = cs.mSol; // nbr of solids
    MXX = cs.Ms;   // nbr species
    MXC = cs.Ms -cs.Na -cs.mSol; // nbr aqueous complexes
    MXAQ = cs.Ms -cs.mSol; // nbr aqueous species (comps.+complexes)
    // create working arrays
    karl = new int[MXA];
    iter = new int[MXA];
    catchRoundingErrors = new int[MXA];
    x1 = new double[MXA]; x2 = new double[MXA]; xOld = new double[MXA];
    y1 = new double[MXA]; y2 = new double[MXA];
    steg = new double[MXA];
    ibe = new int[MXA];
    ifall = new int[MXS];
    ifallSpar = new int[MXS][2];
    iva = new int[MXA+1];
    ivaf = new int[MXA];
    ivaBra = new int[MXA+1];  ivaNov = new int[MXA+1];

    lnBeta = new double[MXC];
    lnA = new double[MXX];
    lnBA = new double[MXC];
    lnG = new double[MXAQ];
    tolY = new double[MXA];
    totVA = new double[MXA];
    mono = new boolean[MXA];
    ber = new boolean[MXA];
    falla = new boolean[MXA];
    if(MXS >0) {
        lnKf = new double[MXS];
        lnKmi = new double[MXS];
        fall = new boolean[MXS];
        ruta = new double[MXS][MXS];
        rut1 = new double[MXA][MXA];
        fscal = new double[MXS];
    }

    pos = new boolean[MXA];
    neg = new boolean[MXA];
    ober = new boolean[MXA][MXA];
    nober = new int[MXA];
    noCalc = new boolean[MXA];
    pva = new double[MXA][MXC];

    oldLnG = new double[MXAQ];
    for(int lix =0; lix <MXAQ; lix++) {oldLnG[lix] = 0;}
    fut = new int[MXS];
    iber = new int[MXA+1];
    ifSpar = new int[MXS];
    totBe = new double[MXA];

 // The first time HaltaFall is called, a plan is made to solve the mass
 // balance equations, some variables are initialized, etc.

    nIon = cs.Na + cs.nx;
    int liax, liaf;
    for(int lix =0; lix <cs.nx; lix++) {lnBeta[lix] = ln10*cs.lBeta[lix];}
    if(cs.mSol != 0) {
        for(int lif=0; lif <cs.mSol; lif++) {
            liaf = cs.nx +lif;
            lnKf[lif] = -cs.lBeta[liaf]*ln10;
            fscal[lif] = 1;
            liax = cs.Na +cs.nx +lif;
            if(!cs.noll[liax]) {
                for(int lia =0; lia < cs.Na; lia++) {
                    fscal[lif] = fscal[lif] +Math.abs(cs.a[liaf][lia]);
                }
            }
        } // for lif
    } // mSol !=0
    for(int lia =0; lia < cs.Na; lia++) {
        mono[lia] = true;
        pos[lia] = false;
        if(!cs.noll[lia]) {pos[lia] = true;}
        neg[lia] = false;
        for(int lix=cs.Na; lix < cs.Ms; lix++) {
            liax = lix -cs.Na;
            if(cs.noll[lix]) {continue;}
            if(Math.abs(cs.a[liax][lia]) > 0.00001
               && Math.abs(cs.a[liax][lia]-1) > 0.00001)  {mono[lia] = false;}
            if(cs.a[liax][lia] > 0) pos[lia] = true;
            if(cs.a[liax][lia] < 0) neg[lia] = true;
            } // for lix
        } // for i
    double xm;
    for(int li=0; li <cs.Na; li++) {
        for(int lj=0; lj <cs.Na; lj++) {
            ober[li][lj] = li != lj;
            for(int lix=0; lix <cs.nx; lix++) {
                if(cs.noll[cs.Na+lix]) {continue;}
                xm = cs.a[lix][li]*cs.a[lix][lj];
                if(Math.abs(xm) > 0.00001) {ober[li][lj] = false;}
                } // for lix
            if(cs.mSol == 0) {continue;}
            for(int lif=0; lif <cs.mSol; lif++) {
                if(cs.noll[nIon+lif]) {continue;}
                liaf = cs.nx +lif;
                xm = cs.a[liaf][li]*cs.a[liaf][lj];
                if(Math.abs(xm) > 0.00001) {ober[li][lj] = false;}
                } // for lif
            } // for lj
        } // for li
    for(int li=0; li <cs.Na; li++) {
        nober[li]=0;
        for(int lj=0; lj <cs.Na; lj++) {
            if(ober[li][lj]) nober[li] = nober[li] +1;
            } // for lj
        } // for li
    for(int ia=0; ia <cs.Na; ia++) {
        if(c.kh[ia] == 2) {continue;} //calculation not requested
        noCalc[ia] = false; // that is, calc = true, calculation is possible
        if(!(pos[ia] && neg[ia]) &&
           !(pos[ia] && c.tot[ia] > 0) &&
           !(neg[ia] && c.tot[ia] < 0)) {
                noCalc[ia] = true; //calculation is not possible
                c.logA[ia] = NOCALC_LOGA;
        }
    } // for ia

    haltaGetIva();

    if(c.dbg >=ERR_RESL_INPUT_3){
        printInput();
        out.println("HaltaFall object constructor ends");
    }

} // HaltaFall - constructor
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="haltaCalc">
/** Calculates the equilibrium composition of a Chemical System.<br>
 * Error reporting:  Exceptions are thrown if needed, and the variable
 * "<code>Chem.ChemSystem.ChemConncs.errFlags</code>" is set.
 * Output is written to a user-specified PrintStream provided to the constructor.
 * @see lib.kemi.haltaFall.HaltaFall HaltaFall
 * @see lib.kemi.haltaFall.HaltaFall#HaltaFall(lib.kemi.chem.Chem.ChemSystem, lib.kemi.haltaFall.Factor, java.io.PrintStream) HaltaFall(chemSystem, factor, printStream)
 * @see lib.kemi.haltaFall.HaltaFall#haltaCancel() haltaCancel()
 * @throws lib.kemi.chem.Chem.ChemicalParameterException */
public void haltaCalc()
        throws Chem.ChemicalParameterException {
    int ia; int rva; boolean tjat; double w;
    if(c.dbg >=ERR_RESL_INPUT_3) {out.println("haltaCalc(concs): Starting calculation with new concentrations."+nl+
                                              "                  debug level = "+c.dbg);}
    panic = false;

    //For some reason the calculations go faster if they are performed in two steps,
    //  first with a small tolerance (1E-3) and then with the user-requested tolerance.
    //  This is accomplished with "loopTol". To remove this loop, set tol0 = 1E-10.
    //
    //Smaller tolerances are set to components solved in "inner" iteration loops.
    //  The maximum decrease in the tolerance is "MAXtolSteg" (equals 2 or 3 in log-units).
    //  For example, if tol=1e-5, the tolerance for the component in the inner
    //  loop might be 1e-8, depending on the number of equations to solve (nva).
    final int MAXtolSPAN;
    if(nva <= 3) {MAXtolSPAN = 2;} else {MAXtolSPAN = 3;}  //log10-units
    double tol, tol0 = 5.01187E-3; // log10(tol0) = -2.3

    int tolLoop = 0;
    loopTol:
    do {
        tol = Math.min(Math.max(Math.abs(c.tol),1e-9),1e-2); // something between 1E-9 and 1E-2
        if(tolLoop == 0) {
            if(tol < tol0) {tol = tol0;} else {tolLoop++;}
        }
        tolLoop++;

    c.errFlags = 0;
    haltaInit();
    boolean firstLoop=true;
    singFall = false;
    iterFasta = 1;
    iterAc = 0;

// NYA:  get lnA[] and/or tolY[]
    double logTol = Math.log10(tol);
    if(c.dbg >=ERR_RESL_INPUT_3) {out.println("Max tolerance to solve mass-balance eqns. (log) = "+(float)logTol+", max tolerance span (log) = "+MAXtolSPAN);}
    // Get minTot and tolStep:
    //    minTot will contain the lowest non-zero total concentration
    double minTot = Double.MAX_VALUE;
    //    tolStep is the step size in log-units for the change in tol between components
    double tolStep = 0;
    if(nva > 1) {
        // get a value between 1 and MAXtolSPAN
        // if nva = 2  then tolSpan = 1
        // if nva = 3  then tolSpan = 2
        // if nva >= 4  then tolSpan = 3
        int tolSpan = Math.min(MAXtolSPAN, nva-1);
        // if nva = 2  then tolSteg = 1
        // if nva = 3  then tolSteg = 1
        // if nva = 4  then tolSteg = 1
        // if nva = 5  then tolSteg = 0.75
        // if nva = 6  then tolSteg = 0.6  etc
        tolStep = (double)tolSpan/(double)(nva-1);
       // get minTot: the lowest non-zero total concentration
       for(ia=0; ia <cs.Na; ia++) {
         if(c.kh[ia]==1 && c.tot[ia]!=0) {minTot = Math.min(Math.abs(c.tot[ia]),minTot);}
       }
       minTot = minTot * 1e-4; // this value is used when a tot.conc.=0
    }
    // if the lowest non-zero total concentration is too high:
    // use 1e-8 as "zero" concentration.
    minTot = Math.min(minTot, 1e-8);
    if(c.dbg >=ERR_RESL_INPUT_3){out.println("Mass-balance tolerances (relative):");}
    int ivaIndex;
    for(ia=0; ia <cs.Na; ia++) {
      if(c.kh[ia] == 1 && !noCalc[ia]) {
          w = logTol;
          if(nva > 1) {
            ivaIndex = 0;
            for(ivar=0; ivar < nva; ivar++) {
                if(iva[ivar] == ia) {ivaIndex = ivar; break;}
            }
            ivaIndex = (nva-1) - ivaIndex;
            w = logTol - tolStep*(double)ivaIndex;
          }
          w = Math.pow(10,w);
          if(c.dbg >=ERR_RESL_INPUT_3){out.print(" "+(float)w);}
          // if the total concentration is zero, use a minimum value
          tolY[ia] = w * Math.max(Math.abs(c.tot[ia]),minTot);
      } //if kh =1
      else {
          if(c.dbg >=ERR_RESL_INPUT_3) {out.print(" NaN");}
          tolY[ia]=Double.NaN;
      }
      lnA[ia] = ln10 * c.logA[ia];
    } //for ia
    if(c.dbg >=ERR_RESL_INPUT_3){ // ---- debug ----
        out.println(nl+"Mass-balance tolerances (absolute):");
        for(ia=0; ia <cs.Na; ia++) {out.print(" "+(float)tolY[ia]);}
        out.println();
        out.println("Continuation run: "+c.cont+", STEG0= "+STEG0+", STEG0_CONT= "+STEG0_CONT+", STEGBYT= "+STEGBYT);
    }

    if(nva <= 0) { // NVA=0  No mass balance eqn. needs to be solved
        if(c.dbg >=ERR_DEBUG_FASTA_4) {out.println("  nva = 0");}
        //java: changed ivar=1 to =0
        ivar = 0;
        iterFasta = 0;
        x = lnaBas(ivar);
        while(true) {
            cBer(ivar);
            if(!c.actCoefCalc || actCoeffs()) {break;} // ok!
            if(c.isErrFlagsSet(6)) {break;} // activity factors did not converge
        }
        fasta();
        nog();
        if(c.dbg >=ERR_DEBUG_FASTA_4) {out.println("haltaCalc returns;"+nl);}
        return;
    } //if nva =0

//SLINGOR:  ("loops" in Swedish)
    // Either:
    // 1- calculation starts (a first guess is made for the unknown lnA[])
    // 2- after fasta()-ANFALL when a different set of solids is found,
    //    this is the starting point to find the new lnA[] with the new set of solids
    // 3- after activity coefficient corrections, find the new lnA[]
    Slingor:
    while(true) {
    if(panic) {break;} //Slingor

    if(c.dbg >=ERR_DEBUG_FASTA_4) {
        out.println("--- haltaCalc at Slingor; nva="+nva+", nfall="+nfall+"; nvaf="+nvaf);
        printArrays(true,false); //print iva[], ivaBra[], ivaNov[]
        if(nvaf>0) {printArraysFasta(true, false,false,false,false, true, false,false);} //print ifall and ivaf[]
    }

    for(rva =0; rva < nva; rva++) {
        ia = iva[rva];
        karl[ia] = 1;
        if(!c.cont) {steg[ia] = STEG0;} else {steg[ia] = STEG0_CONT;}
        iter[ia]=0;
        catchRoundingErrors[ia]=0;
    } //for rva

    ivar =iva[0]; //java: changed iva[1] to iva[0]
    if(c.dbg >= ERR_DEBUG_FASTA_4) {
        out.println("    haltaCalc at Slingor; new ivar="+ivar);
    }
    if(nfall >0) {lnaBer1();}
    x = lnaBas(ivar);
    if(ber[ivar]) { //calculate lnA from a solubility product
        if(c.dbg >= ERR_DEBUG_FASTA_4) {
            out.println("    ber["+ivar+"]=true;  x="+x+";  tjat=false;");
            printArrays(false,true); //print lnA
        }
        cBer(ivar);
        if(nvaf >0) {lnaBer2();}
        tjat = false;
    } //if ber[ivar]
    else {
        tjat = true;
        if(cs.nx == 0 && cs.noll[ivar]) {lnA[ivar]=1; tjat = false;} //added 2011-sept.
        if(c.dbg >= ERR_DEBUG_FASTA_4) {out.println("    ber["+ivar+"]=false;  tjat="+tjat);}
    }

//TJAT  ( = "repeat" or "nagg" in Swedish)
//      Solve the mass balance equations through iterations
    Tjat:
    while(true) {
    if(panic) {break Slingor;}
    if(c.dbg >= ERR_DEBUG_FASTA_4) {out.println("--- haltaCalc at Tjat; ivar="+ivar+", tjat="+tjat);}

    goToProv: {
    if(tjat) {
        lnA[ivar] =x;
        if(falla[ivar]) {
            lnaBer1();
            x = lnaBas(ivar);
        }
        if(ivar != ivaNov[ivar]) {

        // Clear "too Many Iterations"
        // for "inner" loops if they are "dependent" with this ivar
            for(rva=0; rva < nva; rva++) {
                if(iva[rva] != ivar) {continue;}
                if(rva == 0) {break;} // do not clear anything for the 1st iva
                for(int n=0; n < rva; n++) {
                    ia = iva[n];
                    if(ia != ivar && !ober[ivar][ia]) {
                        if(c.dbg >= ERR_XTRA_DEBUG_6) {out.println("    setting  iter["+ia+"]=0  and  catchRoundingErrors["+ia+"]=0");}
                        iter[ia] = 0; catchRoundingErrors[ia]=0;
                    }
                }
                break;
            }

            ivar = ivaNov[ivar];
            if(c.dbg >=ERR_XTRA_DEBUG_6) {out.println("--- haltaCalc at Tjat; new ivar="+ivar);}
            x = lnaBas(ivar);
            if(ber[ivar]) {
                cBer(ivar);
                if(nvaf >0) lnaBer2();
                break goToProv;
            } //if ber[ivar]
        }
        cBer(ivar);
        if(nvaf >0) {lnaBer2();}
        totBer(); /* Returns
                   * indik=1 not ok but it is a component only involved in
                   *    mononuclear reactions and there are no solids present
                   * indik=2 ok (the Y is equal to Y0 within the tolerance)
                   *    or if too many iterations (iter[ivar] is then = -1)
                   * indik=3 not ok and it is either not a mono component or
                   *    there are solids present */
        if(indik ==1) {continue;} //Tjat
        else if(indik ==2) {
            break goToProv;
        }
        else if(indik ==3) {
            kille();
            continue; //Tjat
        }
    } //if tjat
    else {tjat = true;}
    } //goToProv:

//PROV    ( = "test" in Swedish)
    Prov: //The mass balance for component "ivar" was satisfied
    while(true) {
        if(panic) {break Slingor;}
        if(c.dbg >= ERR_DEBUG_FASTA_4) {
            out.println("--- haltaCalc at Prov;  old ivar="+ivar+", new ivar="+ivaBra[ivar]);
            printArrays(false,true); //print lnA
        }
        ivar = ivaBra[ivar];
        //java: changed ivar==0 to ivar==-1
        if(ivar == -1) {
            // This was the last ivar
            //     check first activity coefficients
            //     and then the solid phases
            //
            // Calculate activity coefficients
            if(firstLoop && c.actCoefCalc && !actCoeffs()) {
                // act.coeffs. changed
                singFall = false;  // solid phases might change as well...
                continue Slingor;
            }
            // ok, act.coeffs. not changed
            firstLoop = false;
            //if(!c.isErrFlagsSet(6)) {iterc = 0;}

            // check for "too many iterations"
            for(rva=0; rva < nva; rva++) {
                if(catchRoundingErrors[iva[rva]]==3) {c.errFlagsSet(1);} // uncertain solution
                if(iter[iva[rva]]<0) {iter[iva[rva]] = ITER_MAX+1; c.errFlagsSet(2); break Slingor;}
            }

            // Solid phases
            fasta(); /* Returns
                      * indik=2 if a new set of solids is chosen
                      * indik=3 if ok: either no solids precipitate in the system,
                      *     or no solubility products are exceeded and no solid
                      *     assumed present has zero or negative concentration,
                      *     or if too may solid phase combinations have been tested
                      * indik=1 if "tji" (all combinations of solids give unsoluble equations,
                      *     i.e. negative determinants). */

            if(panic) {break Slingor;}

            if(indik ==1 || indik ==3) { // ok in fasta()
                if(c.dbg >= ERR_DEBUG_FASTA_4) {
                    out.println("haltaCalc at Prov after fasta()");
                    printArrays(false,true); //print lnA
                }
                // Calculate activity coefficients
                if(!c.actCoefCalc || actCoeffs()) { // ok: act.coeffs. not changed
                    break Slingor;
                } else { // not ok: act.coeffs. changed
                    singFall = false;  // solid phases might also change
                    continue Slingor;
                }
            } else if(indik ==2) { // not ok in fasta()
                // new set of solids: clear the "too many iterations" flag
                c.errFlagsClear(1);
                c.errFlagsClear(2);
                c.errFlagsClear(5);
                continue Slingor;
            }
        } //if ivar=-1

        if(ber[ivar]) { // ber[]=true if lnA is calculated from a solid
            continue; //Prov
        }
        x = lnaBas(ivar);
        totBer(); /* Returns
                   * indik=1 not ok but it is a component only involved in
                   *    mononuclear reactions and there are no solids present
                   * indik=2 ok (the Y is equal to Y0 within the tolerance)
                   *    or if too many iterations (iter[ivar] is then = -1)
                   * indik=3 not ok and it is either not a mono component or
                   *    there are solids present */
        if(indik ==1) {continue Tjat;}
        else if(indik ==2) {continue;} //Prov
        else if(indik ==3) {
            kille();
            continue Tjat;
        }

        break Slingor; // this statement should not be executed

    } //while Prov:

    } //while Tjat:

    } //while Slingor:

//NOG   ( = "enough" in Swedish)
    if(!panic) {nog();} else {
        c.errFlagsSet(7);
        if(c.dbg >=ERR_ONLY_1) {out.println(" *****  Interrupted by the user!  *****");}
        break loopTol;
    }

    } while (tolLoop <= 1); // loopTol:

    c.cont = !c.isErrFlagsSet(2) && !c.isErrFlagsSet(3) && !c.isErrFlagsSet(4) &&
             !c.isErrFlagsSet(6) && !c.isErrFlagsSet(7);
    if(c.dbg >=ERR_RESL_INPUT_3) {
        out.println("---- haltaCalc(concs) returns;  cont = "+c.cont+nl+
                "   iterations: "+iter[iva[nva-1]]+", solid combinations = "+iterFasta+nl+
                "   activity coefficient iterations:"+iterAc+", "+c.errFlagsToString()+nl);
    }

} // haltaCalc
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="haltaCancel()">
/** Cancels (stops) any ongoing calculation of the equilibrium composition
 * The variable <code>Chem.ChemSystem.ChemConncs.errFlags</code>" is set.
 * @see lib.kemi.haltaFall.HaltaFall HaltaFall
 * @see lib.kemi.haltaFall.HaltaFall#HaltaFall(lib.kemi.chem.Chem.ChemSystem, lib.kemi.haltaFall.Factor, java.io.PrintStream) HaltaFall(chemSystem, factor, printStream)
 * @see lib.kemi.haltaFall.HaltaFall#haltaCalc() haltaCalc()
 */
public void haltaCancel() {panic = true;}
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="printConcs">
/** Prints the data stored the instance of <code>Chem.ChemSystem.ChemConcs</code>
 * that is associated with this instance of <code>HaltaFall</code>. If called
 * <i>after</i> method <code>haltaCalc</code> it will print the calculated
 * chemical equilibrium composition.
 * Output is written to the user-specified <code>PrintStream</code> provided to the constructor.
 * @see lib.kemi.haltaFall.HaltaFall#haltaCalc()  haltaCalc()
 * @see lib.kemi.haltaFall.HaltaFall#HaltaFall(lib.kemi.chem.Chem.ChemSystem, lib.kemi.haltaFall.Factor, java.io.PrintStream) HaltaFall()
 * @see lib.kemi.haltaFall.HaltaFall HaltaFall */
public void printConcs() {
  int n0, nM, iPl, nP;
  out.flush();
  boolean failed = c.errFlags >0 && (c.isErrFlagsSet(2) || c.isErrFlagsSet(3) ||
          c.isErrFlagsSet(4) || c.isErrFlagsSet(6));
  if(failed) {out.println("--- HaltaFall - Output composition (failed calculation):");}
  else {out.println("--- HaltaFall - Calculated equilibrium composition:");}
  out.println("Components:");
    n0 = 0;     //start index to print
    nM = cs.Na-1;  //end index to print
    iPl = 4; nP= nM-n0; if(nP >=0) { //items_Per_Line and itemsto print
  out.print(" tot conc =");
    print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
    out.format(e," %13.5g",c.tot[kjj]);
    if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
    out.println(); out.print("           ");} //for ijj
    }
    n0 = 0;
    nM = cs.Na-1;
    iPl = 4; nP= nM-n0; if(nP >=0) {
  out.print("solubility=");
    print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
    out.format(e," %13.5g",c.solub[kjj]);
    if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
    out.println(); out.print("           ");} //for ijj
    }
    n0 = 0;
    nM = cs.Na-1;
    iPl = 4; nP= nM-n0; if(nP >=0) {
  out.print("   log Act=");
    print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
    out.format(e," %13.5g",c.logA[kjj]);
    if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
    out.println(); out.print("           ");} //for ijj
    }
  out.println("Aqu.Species  (all components + aqu.complexes):");
    n0 = 0;
    nM = nIon-1;
    iPl = 4; nP= nM-n0; if(nP >=0) {
  out.print("     Conc =");
    print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
    out.format(e," %13.5g",c.C[kjj]);
    if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
    out.println(); out.print("           ");} //for ijj
    }
    n0 = 0;
    nM = nIon-1;
    iPl = 4; nP= nM-n0; if(nP >=0) {
  out.print("   log f=");
    print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
    out.format(e," %13.5g",c.logf[kjj]);
    if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
    out.println(); out.print("         ");} //for ijj
    }
  out.println("Solid Phases:");
    n0 = nIon;
    nM = cs.Ms-1;
    iPl = 4; nP= nM-n0; if(nP >=0) {
  out.print("     Conc =");
    print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
    out.format(e," %13.5g",c.C[kjj]);
    if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
    out.println(); out.print("           ");} //for ijj
    }
    n0 = nIon;
    nM = cs.Ms-1;
    iPl = 4; nP= nM-n0; if(nP >=0) {
  out.print("  log Act =");
    print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
    out.format(e," %13.5g",c.logA[kjj]);
    if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
    out.println(); out.print("           ");} //for ijj
    }
  out.println("Tolerances used (absolute):");
    for(int ia=0; ia <cs.Na; ia++) {out.print((float)tolY[ia]+" ");}
    out.println();
  if(failed) {out.println("--- HaltaFall - End of output composition (failed calculation).");}
  else {out.println("--- HaltaFall - End of equilibrium composition.");}
  out.flush();
} //printConcs()
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="private procedures">

//<editor-fold defaultstate="collapsed" desc="nog()">
/** Enough:
 *  Calculate logA[], logf[], tot[] and solub[]
 */
private void nog() {
    if(c.dbg >=ERR_XTRA_DEBUG_6) {out.println("nog() in;");}
    int lia, lix, liax, liaf;
    for(lia =0; lia < cs.Na; lia++) {
        c.logf[lia] = lnG[lia]/ln10; //activity coeff.
        c.solub[lia] = 0;
        if(!cs.noll[lia]) {c.solub[lia] = c.C[lia];}
        for(lix =0; lix <cs.nx; lix++) {
            liax = cs.Na +lix;
            if(!cs.noll[liax]) {
                c.solub[lia] = c.solub[lia] +cs.a[lix][lia]*c.C[liax];
            }
        } //for lix
        if(c.kh[lia] ==2) { //logA given as input
            //for water (H2O) the activity might have been changed
            //when calculating activity coefficients
            if(lia == cs.jWater) {c.logA[lia] = lnA[lia]/ln10;}
            c.tot[lia] = c.solub[lia];
            if(cs.mSol > 0) {
            for(lix =0; lix < cs.mSol; lix++) {
                liax = nIon +lix;
                liaf = cs.nx +lix;
                if(!cs.noll[liax]) {
                        c.tot[lia] = c.tot[lia] +cs.a[liaf][lia]*c.C[liax];
                }
            } //for lix
            } //if mSol >0
        } //if kh =2
        else { // Total Concentration given as input
            c.logA[lia] = lnA[lia]/ln10;
        } //if kh =1
    } //for i

    for(lix =0; lix <cs.nx; lix++) {
        liax = cs.Na + lix;
        c.logf[liax] = lnG[liax]/ln10;
        c.logA[liax] = lnA[liax]/ln10;
    } //for lix

    if(cs.mSol >0) {
        for(lix =0; lix <cs.mSol; lix++) {
            liax = nIon + lix;
            c.logA[liax] = lnA[liax]/ln10;
        } //for lix
    } //if mSol >0

    if(c.dbg >=ERR_RESULTS_2) {printConcs();} // ---- debug ----

    if(c.dbg >=ERR_XTRA_DEBUG_6) {out.println("nog() returns;");}
} // nog
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="haltaInit">
/**
 * Checks if for some component the total concentration has changed in a way
 * that makes the mass-balance equations to have no solution.
 */
private void haltaInit() {
    if(c.dbg >= ERR_RESL_INPUT_3) {out.println("haltaInit() in");}
    boolean ok = c.cont;
    c.tolLogF = TOL_LNG / ln10;
    for(int ia =0; ia <cs.Na; ia++) {
        iter[ia] = 0;
        catchRoundingErrors[ia] = 0;
        xOld[ia] = Double.MAX_VALUE;
        if(c.kh[ia] == 2 || (pos[ia] && neg[ia])) {continue;}
        if((pos[ia] && c.tot[ia]>0) || (neg[ia] && c.tot[ia]<0)) {
            if(!noCalc[ia]) {continue;} //calculation was possible
            ok = false;
            noCalc[ia] = false; //calculation is possible
            c.logA[ia] = -10;
            if(c.tot[ia] >0) {c.logA[ia] = Math.log10(c.tot[ia]) -2;}
            } //if pos | neg
        else {
            if(!noCalc[ia]) {ok = false;} //calculation was possible
            noCalc[ia] = true; //calculation is not possible
            c.logA[ia] = NOCALC_LOGA;
        }
    } //for ia

    if(c.dbg >= ERR_RESL_INPUT_3) {
        int n0 = 0;
        int nM = cs.Na-1;
        int iPl = 20; int nP= nM-n0; if(nP >=0) {
    out.print(" noCalc[]=");
        print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        out.format(e," %6b",noCalc[kjj]);
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); out.print("          ");} //for ijj
        }
    }

    if(!ok) {haltaGetIva();}

    if(c.dbg >=ERR_RESL_INPUT_3) {
        int n0, nM, iPl, nP;
        n0 = 0;
        nM = cs.Na-1;
        iPl = 12; nP= nM-n0; if(nP >=0) {
    out.print("mono[]= ");
        print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        out.format(e," %6b",mono[kjj]);
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); out.print("       ");} //for ijj
        }
        n0 = 0;
        nM = cs.Na-1;
        iPl = 20; nP= nM-n0; if(nP >=0) {
    out.print("nober[]= ");
        print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        out.format(e," %3d",nober[kjj]);
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); out.print("       ");} //for ijj
        }
    for(int ia=0; ia < cs.Na; ia++) {
        n0 = 0;
        nM = cs.Na-1;
        iPl = 12; nP= nM-n0; if(nP >=0) {
    out.print("ober["+ia+"][]= ");
        print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        out.format(e," %6b",ober[ia][kjj]);
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); out.print("       ");} //for ijj
        }
    }
    printArrays(true, false);
        n0 = 0;
        nM = cs.Na-1;
        iPl = 12; nP= nM-n0; if(nP >=0) {
    out.print("noCalc[]= ");
        print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        out.format(e," %6b",noCalc[kjj]);
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); out.print("       ");} //for ijj
        }
    }

    // use provided act.coeffs.
    for(int lix =0; lix <cs.nx; lix++) {lnG[lix] = ln10*c.logf[lix];}

    if(c.dbg >=ERR_RESL_INPUT_3) {out.println("haltaInit() returns");}
} // haltaInit
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="haltaGetIva()">
/** Makes a plan made to solve the mass balance equations,
 * initializes some variables, etc. */
private void haltaGetIva() {
    if(c.dbg >=ERR_RESL_INPUT_3) {out.println("haltaGetIva() in at NYA");}
// NYA
    nfall = 0;
    for(int ia=0; ia <cs.Na; ia++) {
        ber[ia] = false;
        falla[ia] = false;
        } // for ia
    if(cs.mSol>0) {
        for(int lif=0; lif <cs.mSol; lif++) {fall[lif] = false;}
    }
    nva = 0;
    for(int ia = 0; ia <cs.Na; ia++) {
        if(c.kh[ia] != 2 && !noCalc[ia]) { // calculation requested and possible
            nva = nva +1;
            iva[nva-1] = ia;  //java: changed iva[nva] to iva[nva-1]
        }
    } // for ia

// PLAN
    if(c.dbg >=ERR_RESL_INPUT_3) {out.println("haltaGetIva() at PLAN;");}
    if(nva >1) {
    int m;
    do {
        m = 0;
        for(int li = 0; li <(nva-1); li++) {
            if(nober[iva[li+1]] > nober[iva[li]]) {
                m = iva[li];
                iva[li] = iva[li+1];
                iva[li+1] = m;
            } else {
               if(mono[iva[li+1]] && !mono[iva[li]] &&
                    nober[iva[li+1]] == nober[iva[li]]) {
                        m = iva[li];
                        iva[li] = iva[li+1];
                        iva[li+1] = m;
               }
            }
        } // for li
    } while(m!=0);
    } // if nva >1

    iva[nva]=-1; ivaBra[nva]=-1; //java: changed from iva and ivaBra [nva+1]=0 to [nva]=-1
    if(nva >0) {
      for(int li=0; li<nva; li++) {
        ivar = iva[li];
        ivaBra[ivar] = iva[li+1];
        int i = -1;
// HOPP ("jump" in Swedish)
        while (true) {
            i = i+1;
            if(!ober[ivar][iva[i]]) {ivaNov[ivar] = iva[i]; break;}
        }//while(true)
      } // for li
    } //if nva >0

    if(c.dbg >=ERR_RESL_INPUT_3) {out.println("haltaGetIva() returns");}
} // haltaGetIva()
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="kille">
/** Kille: Solving the mass balance equation of component <code>ivar</code>,
 * which forms polynuclear complexes.  Locating the solution by giving
 * steg[ivar]=0.5,1,2,4,8,16,32,64,...  until a pair of x1 and x2 is found
 * (when karl[ivar]=2 or 3). Then decreasing steg[ivar] (=steg[ivar]/2,
 * when karl[ivar]=4) until  x1 and x2 are separated less than STEGBYT; at
 * that point a chord method is used.
 * @see lib.kemi.haltaFall.HaltaFall#x x
 * @see lib.kemi.haltaFall.HaltaFall#y y
 * @see lib.kemi.haltaFall.HaltaFall#y0 y0
 * @see lib.kemi.haltaFall.HaltaFall#x1 x1
 * @see lib.kemi.haltaFall.HaltaFall#x2 x2
 * @see lib.kemi.haltaFall.HaltaFall#y1 y1
 * @see lib.kemi.haltaFall.HaltaFall#y2 y2
 * @see lib.kemi.haltaFall.HaltaFall#karl karl
 * @see lib.kemi.haltaFall.HaltaFall#STEG0 STEG0
 * @see lib.kemi.haltaFall.HaltaFall#steg steg
 * @see lib.kemi.haltaFall.HaltaFall#STEGBYT STEGBYT
 * @see lib.kemi.haltaFall.HaltaFall#catchRoundingErrors catchRoundingErrors */
private void kille() {
// VARIABLES----------------------------------------------------------------
//  x= independent variable in the equation y0=y(x)
//  y= dependent variable
//  y0= value aimed at in y0=y(x)
//  x1(ia) and x2(ia)= values for x below and above the aimed value
//  y1(ia) and y2(ia)= values for y corresponding to x1 and x2
//--------------------------------------------------------------------------
    boolean yL;
    double w, w1;
    if(c.dbg >=ERR_XTRA_DEBUG_6) {
        out.println("kille() in; ivar="+ivar+", karl["+ivar+"]="+karl[ivar]+", x="+x+", steg="+steg[ivar]+" (STEGBYT="+STEGBYT+")"+nl+
                    "    x1["+ivar+"]="+x1[ivar]+", x2["+ivar+"]="+x2[ivar]);}
// Locating the solution by giving steg(Ivar)=0.5,1,2,4,8,16,32,64,...
//   until a pair of x1 and x2 is found (in karl(ivar)=2 or 3). Then
//   decreasing steg(ivar) (=steg(ivar)*0.5, in karl(ivar)=4) until
//   x1 and x2 are separated less than STEGBYT
    yL = (y > y0);
    if(yL) {
        x2[ivar] =x;
        y2[ivar] =y;
    } //if yL
    else {
        x1[ivar] =x;
        y1[ivar] =y;
    } //if !yL
    switch (karl[ivar]) {
        case 1: { //karl =1 the beginning
            if(yL) {karl[ivar]=3; x = x -steg[ivar];}
            else {karl[ivar]=2; x = x +steg[ivar];}
            break;
        } //karl = 1
        case 2: { //karl =2 it was y<y0
            if(yL) {karl[ivar] =4;}
            else{
                steg[ivar] = steg[ivar] + steg[ivar];
                x = x +steg[ivar];}
            break;
        } //karl = 2
        case 3: { //karl =3 it was y>y0
            if(!yL) {karl[ivar] =4;}
            else {
                steg[ivar] = steg[ivar] + steg[ivar];
                x = x -steg[ivar];}
            break;
        } //karl = 3
        case 4: { //karl =4 we have x1 and x2 corresponding to y<y0 and y>y0
            if(steg[ivar] >= STEGBYT) {
                steg[ivar] = 0.5 * steg[ivar];
                if(!yL) {x = x + steg[ivar];} else {x = x - steg[ivar];}
                break;
            } //if steg > STEGBYT
        //KORDA:  aproximating the solution by chord shooting ('secant method')
            if(c.dbg >= ERR_XTRA_DEBUG_6) {out.println("Korda: y0,y1,y2 = "+y0+", "+y1[ivar]+", "+y2[ivar]+", x="+x);}
            w = y0 - y1[ivar];
            w1 = x2[ivar] - x1[ivar];
            x = x1[ivar] + w * w1 / (y2[ivar]-y1[ivar]);
            if(c.dbg >= ERR_XTRA_DEBUG_6) {out.println("   x1,x2="+x1[ivar]+", "+x2[ivar]+", new x="+x);}
            
            // --- Avoid rounding errors
            if(Math.abs(1-(xOld[ivar]/x)) < 1e-14) {
                if(c.dbg >= ERR_XTRA_DEBUG_6) {out.println("   ---- catchRoundingErrors["+ivar+"]="+(catchRoundingErrors[ivar]+1)+
                                                                    ", x="+x+"  old x="+xOld[ivar]);}
                if(catchRoundingErrors[ivar]==0) {
                    x = x + 1e-10*Math.abs(x);
                } else if(catchRoundingErrors[ivar]==1) {
                    x = x - 2e-10*Math.abs(x);
                }
                if(catchRoundingErrors[ivar] < 3) { // catchRoundingErrors[] may be 0,1,2, or 3.
                    catchRoundingErrors[ivar]++;
                    if(c.dbg >= ERR_XTRA_DEBUG_6) {out.println("        new x="+x);}
                }
            }
            break;
        } //karl = 4
        default: { //this should not happen
            out.println("!? Programming error in \"HaltaFall\"; karl[ivar]="+karl[ivar]);
            karl[ivar]=1;
            break;}
    } //switch (lq)

    xOld[ivar] = x;
    if(c.dbg >=ERR_XTRA_DEBUG_6) {out.println("kille() returns, karl["+ivar+"]="+karl[ivar]+"; indik="+indik+"; x="+x);}
} // kille()
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="fasta">

//<editor-fold defaultstate="collapsed" desc="fasta()">
/**
 * Finds out what solids are present in the system at equilibrium.
 * Returns <b><code>indik</code><b><br>
 * = 2 if a new set of solids is chosen<br>
 * = 3 if ok (either no solids precipitate in the system, or no solubility
 *   products are exceeded and no solid assumed present has zero or negative
 *   concentration), or if too may solid phase combinations tested<br>
 * = 1 if "tji" (all combinations of solids give unsoluble equations,
 *   i.e. negative determinants).
 */
private void fasta() {
//  VARIABLES -----------------------------------------------
//  nfall= nr of solids present at equilibrium
//  nfspar= nr of solids indicated at FallProv
//  nvaf= nr of mass balance equations to be solved in presence of solids
//  singFall= true if the matrix "ruta" has come out singular
//  nUt= nr of solids systematically eliminated at some stage while
//       singFall is true.
//  ber[ia]=true if lnA[ia] is calculated by means of the solubility product
//          in procedure lnaBer
//  fall[if]=true if the solid 'if' is assumed present at equilibrium
//  falla[ia]=true if the component 'ia' is assumed to occur in one or more
//            solids at equilibrium
//  fut[i]= ifspar number of i'th solid to be eliminated at some stage in
//          systematic variation during singFall= true
//  ibe[m]= 'ia' number for m'th lnA[ia] to be calculated with the solubility
//         product in procedure lnaBer (m=0 to nfall-1)
//  iber[j]= j'th of 'iva' numbers to be an 'ibe' (ibe[j]=iva[iber[j]], (j=0 to
//           nfall-1)
//  ifall[m]= number of the m'th solid present at equilibrium (m=0 to nfall-1)
//  ifspar[m]= number of the m'th solid indicated as possible a FallProv
//  iva[m]= 'ia' number for m'th component to be tested in absence of solids
//         (m=0 to nva)
//  ivaf[m]='ia' number for m'th component to be tested in presence of solids
//          (m=0 to nvaf)
// ---------------------------------------------------------
// Master routine for solid phase calculations. The routine cycles through
// chunks of code in the original FASTA routine, using the value of nextFall
// as switch. The meanings of nextFall on return to this routine
// are as follows:
//   nextFall     next routine       line No. in original programme
//       0        Exit to PROV,          9000, 10000, 25000
//                "indik" set ready
//       1        fallProv_InFall        14000
//       2        beFall                 16000
//       3        anFall                 24000
//       4        utFall                 17000
//       5        sing                   18000
//       6        fUtt                   20000
//       7        hoppFut                21000
//       8        inFut                  22000
//       9        uppNut                 23000

    nextFall = 1;
    do {
        if(panic) {break;}
        switch (nextFall) {
            case 1: {fallProv_InFall(); break;}
            case 2: {beFall(); break;}
            case 3: {anFall(); break;}
            case 4: {utFall(); break;}
            case 5: {sing_Hopsi(); break;}
            case 6: {fUtt(); break;}
            case 7: {hoppFut(); break;}
            case 8: {inFut(); break;}
            case 9: {uppNut(); break;}
        } //switch nextFall
    } while (nextFall > 0);
} // fasta()
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="fallProv_InFall()">
/**
 * Tests for changes in the number of solids
 * calculated with the given lnA[] values.
 */
private void fallProv_InFall() {
  boolean bra, foundOne;
  int nyfall, lj, lq, lf, lqa, ia, li, lia, lix, lif, liax, liaf;
  double w;
  double zMax; int kMax;
// ---------------------------------------------------------
// FALLPROV: Check that the solubility product is not exeeded
//           for any solid phase assumed to be absent.
//           Find ifall[] and fall[] for new solids appearing.
// ---------------------------------------------------------
  if(c.dbg >= ERR_DEBUG_FASTA_4) {
      out.println("--- Fasta() in at FallProv; nfall="+nfall);
      //print ifall
      printArraysFasta(true,false,false,false,false,false,false,false);
      out.println("Testing that no additional solid is supersaturated and that the formal"+nl+
                  "concentration is not negative for any solid assumed to be present.");
  }
  bra = true; // "bra" means "good" or "ok" in Swedish
  nyfall = 0;
  // zMax and kMax are used with ONLY_ONE_SOLID_AT_A_TIME
  // to find which of the supersaturated solids is to be included
  // in the equilibrium system
  zMax = -1.e-30; kMax = -1;
  for(lif =0; lif < cs.mSol; lif++) {
      liax = nIon + lif;
      if(!fall[lif]) {
          liaf = cs.nx + lif;
          c.C[liax] =0;
          w =0;
          for(lia =0; lia < cs.Na; lia++) {w = w + cs.a[liaf][lia]*lnA[lia];}
          lnA[liax] = w -lnKf[lif];
          if(w <= lnKf[lif] || cs.noll[liax] || nva == 0) {continue;}
          // ---- Block added 2013-Jan.
          //      Solids may be oversaturated that can not be allowed to
          //      precipitate. For example:
          //      let us say C(s) is oversaturated at low redox potential,
          //      if HCO3- is the component and its total conc. is given,
          //      then C(s) may precipitate;  but if CO2(g) is the component
          //      and the partial pressure of CO2(g) is given, then C(s)
          //      may NOT be present at equilibrium.
          foundOne = false;
          // loop through components for which the total conc. is given
          for(lia =0; lia < nva; lia++) {
              // is the stoichiometric coefficient non-zero?
              if(Math.abs(cs.a[liaf][iva[lia]]) >0.00001) {foundOne = true; break;}
          }
          if(!foundOne) {continue;} // all coefficients zero?
          // ---- Block end
          if(!ONLY_ONE_SOLID_AT_A_TIME) {
              bra = false;
              fall[lif]= true;
              nyfall++;
              //java: changed ifall[nfall+nyfall] to ifall[nfall+nyfall-1]
              ifall[nfall + nyfall -1] = lif;
          } else {
              w = lnA[liax]/fscal[lif];
              if(w > zMax) {zMax = w; kMax = lif;}
          }
      } //if !fall[lif]
      else {lnA[liax] = 0;}
  } //for lif
  if(ONLY_ONE_SOLID_AT_A_TIME && kMax > -1) {
        bra = false;
        fall[kMax]= true;
        nyfall = 1;
        ifall[nfall] = kMax;
  }
  if(!bra && c.dbg >= ERR_DEBUG_FASTA_4) {
      out.print("FallProv nyfall="+nyfall+", ");
      if(ONLY_ONE_SOLID_AT_A_TIME) {out.println("new solid nbr. is: "+ifall[nfall]);}
      else {printArraysFasta(true, false, false, false, false, false, false, false);} //print ifall[]
  }
  // ---- Block added 2018-March.
  // When singFall is true, solids are removed one at a time until
  // a set is found that gives a non-singular matrix ruta. When
  // testing a reduced set of solids, if a new solid becomes
  // supersaturated which was not in the list "ifspar", then the
  // new solid is tested by setting singFall = false
  if(ONLY_ONE_SOLID_AT_A_TIME && kMax > -1) {
    singFallInterrupt: {
    if(singFall) {
        for(lif =0; lif < nfall; lif++) {if(ifSpar[lif] == kMax) {break singFallInterrupt;}}
        if(c.dbg >= ERR_DEBUG_FASTA_4) {out.println("The new solid is not among the \"ifSpar\" list.  Setting singFall=false.");}
        singFall = false;
    }
    }
  }
  // ---- Block end

  // for java:
  if(nfall+nyfall >0 && nfall+nyfall < ifall.length) {
      for(lif = nfall+nyfall; lif < ifall.length; lif++) {ifall[lif]=-1;}
  }

  if(nva == 0) {
      if(c.dbg >=ERR_DEBUG_FASTA_4) {out.println("Fasta() returns (nva=0)");}
      nextFall = 0;
      return;
  }

  //  Check that the quantity of solid is not negative for any solid
  //  assumed to be present
  if(nfall > 0) {
    for(li =0; li < nfall; li++) {
        ia = ibe[li];
        w = c.tot[ia] - c.C[ia];
        for(lix =0; lix < cs.nx; lix++) {
            liax = cs.Na + lix;
            w = w - cs.a[lix][ia]*c.C[liax];
        }
        totBe[li] = w;
    }
    for(li =0; li < nfall; li++) {
        w = 0;
        for(lj =0; lj < nfall; lj++) {w = w + ruta[li][lj] * totBe[lj];}
        lq = ifall[li];
        lqa = nIon + lq;
        c.C[lqa] = w;
        if(c.C[lqa] < 0) {
            fall[lq] = false;
            c.C[lqa] = 0;
            bra = false; // not "ok"
            if(c.dbg >= ERR_DEBUG_FASTA_4) {
                out.println("Fasta(): for solid "+lq+" the concentration is <0");
            }
        }
    }
  } //if nfall >0

  if(bra) { // if "ok"
    indik =3;
    if(c.dbg >= ERR_DEBUG_FASTA_4) {
        out.println("HaltaFall.fasta() returns OK (to nog()); indik =3; nfall="+nfall+"; iterFasta="+iterFasta);
        //print most arrays
        printArraysFasta(true,true,true,true,true, false,false,false);
    }
    nextFall = 0;
    return;
  }
  if(iterFasta > ITER_FASTA_MAX) {
    indik =3;
    c.errFlagsSet(4);
    if(c.dbg >=ERR_DEBUG_FASTA_4) {
        out.println("Error in HaltaFall.fasta(): "+ITER_FASTA_MAX+
                    " different solid phase combinations"+nl+
                    "were tested and found NOT satisfactory.");
        out.println("Fasta() returns (to nog()); indik =3; "+c.errFlagsToString());
    }
    nextFall = 0;
    return;
  }
  iterFasta++;

 // ---------------------------------------------------------
 // INFALL:  find new: nfall, ifall[nfall]
 // ---------------------------------------------------------
 // The lnA[] were not consistent with the solid phases.
 // Either nyfall new solid phases appeared, or some solids assumed
 // present had negative C[].  At first it is assumed at BEFALL that
 // the first nfall of the iva[] are the ibe[] to be calculated.
 // If the determinant of "ruta" is found to be zero at UTFALL,
 // the ibe are changed systematically at SING-HOPPSI by means of
 // array iber[], until a non-zero determinant is found.
 // Then at ANFALL, the arrays rut1 and pva are calculated, and
 // the new mass balance equations are solved at SLINGOR.
  nfall = nfall + nyfall;
  li = -1;
  do {
    li++;
    if(fall[ifall[li]]) {continue;}
    nfall--;
    if(nfall != 0) {for(lj = li; lj < nfall; lj++) {ifall[lj] = ifall[lj+1];}}
    li--;
  } while (li < (nfall-1));

  if(c.dbg >= ERR_DEBUG_FASTA_4) {
      out.println("Fasta() at InFall; nfall="+nfall+", nyfall="+nyfall+
                ", nva="+nva+", singFall="+singFall);
      //print ifall
      printArraysFasta(true, false,false,false,false,false,false,false);
      if(singFall) { //print ifSpar
          printArraysFasta(false,false,false,false,false,false,true,false);
      }
  }

  if(nfall <= 0) {
      nextFall = 3; // no solid phases, goto ANFALL;
      return;
  }
  //nfall >0
  if(!singFall) {
      for(li =0; li < nfall; li++) {ifSpar[li] = ifall[li];}
      nfSpar = nfall;
      nUt =0;
      if(nfall > nva) {
          nUt = nfall - nva -1;
          if(c.dbg >= ERR_DEBUG_FASTA_4) {
              out.println("   nfall (="+nfall+") is > nva (="+nva+")"+
                          " & !singFall"+nl+"   nfSpar = "+nfSpar);
              printArraysFasta(false,false,false,false,false,false,true,false);
              out.println("   setting singFall=true");
          }
          singFall = true;
          nextFall = 9; // goto UPPNUT;
          return;
      } //if nfall > nva
  } else { //if singFall
    if(nfall >= nva) {
        if(c.dbg >= ERR_DEBUG_FASTA_4) {
            out.println("   nfall (="+nfall+") is >= nva (="+nva+")"+
                        " & singFall = "+singFall+nl+
                        "   nfSpar = "+nfSpar+" nUt = "+nUt);
        }
        nfall = nfSpar -nUt;
        for(lf =0; lf < cs.mSol; lf++) { // ---- for(lf) added April-2013
            fall[lf] = false;
            for(li =0; li < nfSpar; li++) {
                if(ifSpar[li] == lf) {fall[lf] =true; break;}
            }
        } // ---- for(lf) added April-2013
        nextFall = 7; // goto HOPPFUT:
        return;
    }
  }

  nextFall = 2; // solids present: goto BEFALL then UTFALL

} // fallProv()
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="beFall()">
/**
 * Initialises iber[].
 */
private void beFall() {
  int li;
  if(c.dbg >= ERR_DEBUG_FASTA_4) {
      out.println("Fasta() at BeFall, nfall="+nfall);
      //print array "ifall"
      //printArraysFasta(true,false,false,false,false,false,false,false);
  }
  for(li =0; li < nfall; li++) {iber[li]=li;}
  iber[nfall] = -1; //java: changed from iber[nfall+1]=0

  nextFall = 4; // goto UTFALL

} // beFall()
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="anFall()">
/**
 * Calculates ibe[], ivaf[], rut1[][] and pva[][] arrays.
 */
private void anFall() {
  int lia, li, liaf, lix, lj, lm, lq, lz;
  double w;
 // ---------------------------------------------------------
 // ANFALL:
 // A set of nfall solids has been found with a non-zero determinant
 // for the array "ruta". Some variables are changed, and the arrays
 // rut1 and pva are calculated.
  if(c.dbg >= ERR_DEBUG_FASTA_4) {
      out.println("Fasta() at Anfall; nfall="+nfall);
  }
  for(lia =0; lia < cs.Na; lia++) {falla[lia]=false; ber[lia]=false;}
  if(nfall > 0) {
      //Get falla[], ber[] and ibe[] (from ifall and fall)
      for(li = 0; li < nfall; li++) {
          liaf = cs.nx + ifall[li];
          if(fall[ifall[li]]) {
              for(lia =0; lia < cs.Na; lia++) {
                  if(cs.a[liaf][lia] != 0) {falla[lia]=true;}}
          }
          ibe[li] = iva[iber[li]];
          ber[ibe[li]] = true;
      } //for li

      // set up ivaf[]
      nvaf = 0;
      if(nva > 0) {
          // Find nvaf and ivaf[]
          for(li =0; li < nva; li++) {
              lia = iva[li];
              if(ber[lia]) {continue;}
              nvaf++;
              ivaf[nvaf-1] = lia; //java
          }

          if(c.dbg >= ERR_DEBUG_FASTA_4) {
              out.println("  nvaf="+nvaf);
              //print arrays "ber" and "ivaf"
              printArraysFasta(false,false,false,false,true,true,false,false);
          }

          // Calculate rut1[][]
          if(nvaf > 0) {
              for(li =0; li < nvaf; li++) {
                  for(lj =0; lj < nfall; lj++) {
                      w = 0;
                      for(lm =0; lm < nfall; lm++) {
                          lq = ivaf[li];
                          lz = cs.nx + ifall[lm];
                          w = w + cs.a[lz][lq]*ruta[lm][lj];
                      } //for lm
                      rut1[li][lj] = w;
                  } //for lj
              } //for li
              // Calculate pva[ia][ix]
              for(li =0; li < nvaf; li++) {
                  for(lix =0; lix < cs.nx; lix++) {
                      lia = ivaf[li];
                      w = cs.a[lix][lia];
                      if(nfall > 0) {
                          for(lm =0; lm < nfall; lm++) {
                              lq = ibe[lm];
                              w = w - cs.a[lix][lq]*rut1[li][lm];
                          } //for lm
                      } //if nfall >0
                      pva[lia][lix] = w;
                  } //for lix
              } //for li

          } //if nvaf >0
      } //if nva >0
  } //if nfall >0

  //SLINGOR(IN)
  indik = 2;
  if(c.dbg >= ERR_DEBUG_FASTA_4) {
    out.println("HaltaFall.fasta() returns to Slingor(In); nfall="+nfall+"; iterFasta="+iterFasta);
    //print most arrays
    printArraysFasta(true,true,true,true,true, false,false,false);
  }

  nextFall = 0; // exit to Slingor

} // anFall()
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="utFall()">
/**
 * Calculates ruta[][] and inverts the array.
 */
private void utFall() {
  //Calculate "ruta[][]" and check that it is not singular
  int ia, li, lj, lf;
  if(c.dbg >= ERR_DEBUG_FASTA_4) {
      out.println("Fasta() at UtFall, nfall="+nfall);
      // print array "ifall" and "iber"
      printArraysFasta(true, true, false,false,false,false,false,false);
  }
  for(li =0; li < nfall; li++) {
      ia = iva[iber[li]];
      for(lj =0; lj < nfall; lj++) {
          lf = cs.nx + ifall[lj];
          ruta[li][lj] = cs.a[lf][ia];
      } //for lj
  } //for li
  invert(ruta, nfall); // sets indik =0 (ok) or =1 (matrix singular)
  if(indik == 1) {
      nextFall = 5;  //matrix singular, goto SING:
  } else {
      nextFall = 3;  //matrix inverted, goto ANFALL:
  }
} // utFall()
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="sing()">
/** Deals with the case of singular ruta[]. */
private void sing_Hopsi() {
  // the matrix ruta[][] was singular
  // --------
  // SING:
  // --------
  if(c.dbg >= ERR_DEBUG_FASTA_4) {out.println("Fasta() at Sing-HoppSi");}
  // --------
  // HOPPSI:
  // --------
  // Bump up iber[] indices  one at a time to give all combinations of nfall
  // "be" components from the set of nva
  if(hopp(iber,nva)) {
      // all iber[] sets used up: move to FUTT to remove a solid phase
      if(c.dbg >= ERR_DEBUG_FASTA_4) {
          out.println("     ruta[][] was singular for all possible iber[] "+
                  "combinations; setting singFall = true");
      }
      singFall = true;
      // note that nfspar and ifspar[] are already set at INFALL
      nextFall = 6; // FUTT
  } else {
      // move to UTFALL to try the new iber[] set
      nextFall = 4; // UTFALL
  }
  if(c.dbg >= ERR_DEBUG_FASTA_4) {out.println("     Sing-HoppSi returns");}
} // sing()
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="fUtt()">
/** Routine to handle systematic removal of solid phases in cases where no
 * consistent set can be found.  */
private void fUtt() {
  // ---------------------------------------------------------
  // FUTT:
  // ---------------------------------------------------------
  // iber[] sets are exausted. Either:
  //   - It has been proved inpossible to pick out a group of nfall
  //     components iber[] such that the array "ruta" has a non zero
  //     determinant,
  //   - Or more solids are indicated than there are logA[] values
  //     to vary (nfall > nva).
  // One must try systematically combinations of a smaller number
  // (nfall-nUt) of solid phases, until a non-zero determinant for
  // "ruta" is found. This is done at labels FUTT, INFUT and UPPNUT
  // using the array fut[]. To avoid going into a loop, the program
  // sets singFall=true and remembers the first set of solids
  // (ifspar[nfspar]) which gave only zero determinants.
  // ---------------------------------------------------------
  // Changes by I.Puigdomenech in 2011-Jan.:
  // Curiously, the FUTT-HOPPFUT-INFUT-UPPNUT system published in 1967
  // does not work when nfSpar=1, because setting nUt=1 means that all
  // solids (i.e. the only one) are "striked out".
  // The changes are at UPPNUT and at INFUT.
  // ---------------------------------------------------------
  if(c.dbg >= ERR_DEBUG_FASTA_4) {
      out.println("Fasta() at Futt;  nUt="+nUt+
              ", nfall="+nfall+", nfspar="+nfSpar);
      //print arrays "ifspar" and "fut"
      printArraysFasta(false,false,false,false,false,false, true, true);
  }
  if(nUt == 0) {
      nextFall = 9; // goto UPPNUT:
  } else {
      nextFall = 7; // goto HOPPFUT:
  }
} // fUtt()
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="hoppFut()">
/**
 * Selects a new fut[].
 */
private void hoppFut() {
  boolean hoppKlart;
  // ---------
  // HOPPFUT:
  // ---------
  // if nUt >0 call HOPP to pick a new fut[] array,
  //  if all used, then increment nUt at UPPNUT;
  //  else goto INFUT, BEFALL, UTFALL, ANFALL, and back to Slingor
  if(c.dbg >= ERR_DEBUG_FASTA_4) {out.println("Fasta() at HoppFut");}
  hoppKlart = hopp(fut,nfSpar);
  if(c.dbg >= ERR_DEBUG_FASTA_4) {//print array "fut"
      printArraysFasta(false,false,false,false,false,false,false, true);
  }
  if(hoppKlart) {
      nextFall = 9; // goto UPPNUT
  } else {
      nextFall = 8; // goto INFUT:
  }
} // hoppFut()
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="inFut()">
/**
 * Select new fall[] and ifall[] for the reduced group of solids.
 */
private void inFut() {
  int j, li;
  // ---------
  // INFUT:
  // ---------
  // find new fall[] and ifall[] for the reduced group of solids
  if(c.dbg >= ERR_DEBUG_FASTA_4) {
      out.println("Fasta() at InFut; nfall="+nfall+
              ", nfSpar="+nfSpar+", nUt="+nUt);
      //print array "fut"
      printArraysFasta(false,false,false,false,false,false,false, true);
  }
  for(li =0; li < nfSpar; li++) {fall[ifSpar[li]] = true;}
  if(nUt > 0) {
      for(li =0; li < nUt; li++) {fall[ifSpar[fut[li]]] = false;}
  }
  if(nfSpar > 1) { // ---- line added 2011-Jan.
      j = -1; //java
      for(li =0; li < nfSpar; li++) {
          if(fall[ifSpar[li]]) {
              j++;
              ifall[j] = ifSpar[li];
          }
      } //for li
      nfall = j+1; // ----  line added 2011-Jan.
  } // if nfSpar > 1
  // ---- next line added 2011-Jan.
  else if(nfall==1 && nfSpar==1) {ifall[0] = ifall[1];}

  if(c.dbg >= ERR_DEBUG_FASTA_4) {
      out.println("        after InFut: nfall="+nfall);
      //print arrays "ifall"
      printArraysFasta(true, false, false, false,false,false,false,false);
  } //if debug
  if(nfall == 0) {
      nextFall = 3; // goto ANFALL and then Slingor
  } else {
      nextFall = 2; // goto BEFALL, UTFALL, ANFALL, and back to Slingor
  }
} // inFut()
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="uppNut()">
/** 
 * Increments nUt and sets initial fut[] values.
 */
private void uppNut() {
  int li, lf, ia;
  // ---------
  // UPPNUT:
  // ---------
  // Increment nUt and set initial fut[] values for this nUt.
  // Exit in confusion if nUt > nfSpar
  nUt++;
  nfall = nfSpar -nUt;
  if(c.dbg >= ERR_DEBUG_FASTA_4) {
      out.println("Fasta() at UppNut;  nUt="+nUt+", nfSpar="+nfSpar+
                  ",  nfall="+nfall+"(=nfSpar-nUt)");
  }
  if(nfall > 0) {
      for(li =0; li < nUt; li++) {fut[li] = li;}
      fut[nUt] = -1; //java changed fut[nUt+1]=0 to fut[nUt] = -1
      if(c.dbg >= ERR_DEBUG_FASTA_4) { //print array "fut"
             printArraysFasta(false,false,false,false,false,false,false, true);
      }
  }
  // ---- Block added 2011-Jan.
  else if(nfall == 0 && nfSpar == 1) {
      nfall = 1; fut[0] = 0; fut[1] = -1;
      if(c.dbg >= ERR_DEBUG_FASTA_4) { //print array "fut"
          printArraysFasta(false,false,false,false,false,false,false, true);
      }
  }
  // ---- Block end
  else {
      // --------
      // "TJI" (Tji is Romani (Gypsy) and means "not, nothing")
      // --------
      nfall = 0; nvaf = 0;  // ---- line added 2013-Jan.
      for(lf =0; lf < cs.mSol; lf++) {fall[lf] =false;}
      for(ia =0; ia < cs.Na; ia++) {ber[ia] =false; falla[ia] =false;}
      if(c.dbg >=ERR_ONLY_1) { // ---- error ----
          out.println("Error in HaltaFall.fasta(): "+
                      "The "+nfSpar+
                      " solids found give only zero determinants (\"Tji\")."+
                      nl+"List of solids (array ifSpar):");
          // print array "ifSpar"
          printArraysFasta(false,false,false,false,false,false, true, false);
      } // ---- error ----
      indik = 1;
      c.errFlagsSet(3);
      if(c.dbg >= ERR_DEBUG_FASTA_4) {
            out.println("HaltaFall.fasta() returns at \"Tji\", "+
                    c.errFlagsToString()+", nvaf = "+nvaf+", iterFasta="+iterFasta);}
      nextFall = 0;
      return;  // NYP (IN)
  }

  nextFall = 8; // goto INFUT;

} // uppNut()
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="hopp(lista,max)">
/**
 * Moves to the next permutation of variables in the array list.
 * @param lista array to move
 * @param max
 * @return false if a new permutation has been found;
 * true if the last permutation has already been used
 */
private boolean hopp(int lista[], int max) {
  int lj, i;
  i = -1;
  while(true) {
      i++;
      if(c.dbg >= ERR_XTRA_DEBUG_6) {out.println("Fasta() at Hopp, max="+max+", i="+i);}
      if(lista[i] == max-1) {
          return true;
      } else {
          if(lista[i+1] == lista[i]+1) {continue;}
          lista[i] = lista[i] +1;
          if(i > 0) {
              for(lj=0; lj <= (i-1); lj++) {lista[lj] = lj;}
          }
          return false;
      }
  } //while
} // hopp(lista,max)
// </editor-fold>

// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="invert">
/** Invert: Matrix inversion.<br>
 * Adapted from "www.csee.umbc.edu/~squire/".<br>
 * Sets variable indik = 1 (matrix is singular)
 * otherwise indik = 0 (ok).  */
private void invert(double a[][], int n) {
    int row[] = new int[n];
    int col[] = new int[n];
    double temp[] = new double[n];
    int hold , iPivot , jPivot;
    double pivot;
    if(c.dbg >= ERR_DEBUG_FASTA_4) {out.println("invert(ruta[][],"+n+") in");}

    // set up row and column interchange vectors
    for(int k=0; k<n; k++) {row[k] = k ; col[k] = k ;}
    // begin main reduction loop
    for(int k=0; k<n; k++) {
      // find largest element for pivot
      pivot = a[row[k]][col[k]] ;
      iPivot = k;
      jPivot = k;
      for(int i=k; i<n; i++) {
        for(int j=k; j<n; j++) {
          if(Math.abs(a[row[i]][col[j]]) > Math.abs(pivot)) {
            iPivot = i;
            jPivot = j;
            pivot = a[row[i]][col[j]];
          }
        }
      }
      if(Math.abs(pivot) < 1.0E-10) {
        indik = 1;
        if(c.dbg >= ERR_DEBUG_FASTA_4) {
            out.println("invert() returns; indik=1 (matrix singular)");
        }
        return;
      }
      hold = row[k];
      row[k]= row[iPivot];
      row[iPivot] = hold ;
      hold = col[k];
      col[k]= col[jPivot];
      col[jPivot] = hold ;
      // reduce about pivot
      a[row[k]][col[k]] = 1.0 / pivot ;
      for(int j=0; j<n; j++) {
        if(j != k) {a[row[k]][col[j]] = a[row[k]][col[j]] * a[row[k]][col[k]];}
      }
      // inner reduction loop
      for(int i=0; i<n; i++) {
        if(k != i) {
          for(int j=0; j<n; j++) {
            if(k != j) {
                a[row[i]][col[j]] = a[row[i]][col[j]]
                                    - a[row[i]][col[k]] * a[row[k]][col[j]];
            }
          }
          a[row[i]][col [k]] = - a[row[i]][col[k]] * a[row[k]][col[k]];
        }
      }
    }
    // end main reduction loop

    // unscramble rows
    for(int j=0; j<n; j++) {
      for(int i=0; i<n; i++) {temp[col[i]] = a[row[i]][j];}
      for(int i=0; i<n; i++) {a[i][j] = temp[i];}
    }
    // unscramble columns
    for(int i=0; i<n; i++) {
      for(int j=0; j<n; j++) {temp[row[j]] = a[i][col[j]];}
      System.arraycopy(temp, 0, a[i], 0, n);
    }
    indik = 0;
    if(c.dbg >= ERR_DEBUG_FASTA_4) {
        out.println("invert() returns; indik="+indik+" (ok)");
    }
  } // end invert
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="invert0">
/** Invert0: Matrix inversion  */
/**
private void invert0(double a[][], int n) {
    if(c.dbg >= ERR_DEBUG_FASTA_4) {out.println("invert0(a[][],"+n+") in");}
    int[] iPivot = new int[n];
    double[] pivot = new double[n];
    int[][] indx = new int[n][2];
    double swap, zmax, t;
    int j, i, k, l, l1;
    int row, column;
    // Initialization
    for(i = 0; i < n; i++) {iPivot[i] = 0;}
    // Search for pivot element
    column =-1;
    row =-1;
    for(i = 0; i < n; i++) {
        zmax = 0;
        for(j = 0; j < n; j++) {
            if(iPivot[j] == 1) {continue;}
            for(k = 0; k < n; k++) {
                if(iPivot[k] == 1) {continue;}
                if(iPivot[k] > 1) {
                    indik = 1;
                    if(c.dbg >= ERR_DEBUG_FASTA_4) {out.println("invert0() returns; indik=1 (matrix singular)!");}
                    return;}
                if(Math.abs(a[j][k]) > Math.abs(zmax)) {
                    row =j;
                    column =k;
                    zmax = a[j][k];
                } //if
            } //for k
        } // for j
        iPivot[column] = iPivot[column] +1;
        // Interchange rows to put pivot element on diagonal
        if(row != column) {
            for(l = 0; l < n; l++) {
                swap = a[row][l];
                a[row][l] = a[column][l];
                a[column][l] = swap;
            } //for l
        } //if row != column
        indx[i][0] = row;  indx[i][1] = column;
        pivot[i] = a[column][column];
        // Divide pivot row by pivot element
        if(Math.abs(zmax) < 1.0E-10) {
            indik =1;
            if(c.dbg >= ERR_DEBUG_FASTA_4) {out.println("invert0() returns; indik=1 (matrix singular)");}
            return;} //abs(zmax) =0
        a[column][column] = 1;
        for(l = 0; l < n; l++) {a[column][l] = a[column][l] / pivot[i];}
        // Reduce non-pivot rows
        for(l1 = 0; l1 < n; l1++) {
            if(l1 == column) {continue;}
            t = a[l1][column];
            a[l1][column] = 0;
            for(l = 0; l < n; l++) {a[l1][l] = a[l1][l] - a[column][l] * t;}
        } //for l1
    } //for i

    // Interchange columns
    for(i =0; i < n; i++) {
        //java: changed L=N+1-I to L=(N-1)-I
        l = (n-1) - i;
        //java: changed indx[][1] and [][2] to [][0] and [][1]
        if(indx[l][0] == indx[l][1]) {continue;}
        row = indx[l][0];
        column = indx[l][1];
        for(k =0; k < n; k++) {
            swap = a[k][row];
            a[k][row] = a[k][column];
            a[k][column] = swap;
        } //for k
    } //for i
    indik = 0;
    if(c.dbg >= ERR_DEBUG_FASTA_4) {out.println("invert0() returns; indik="+indik+" (ok)");}
} // invert0
*/
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="cBer">
/** CBER:
 *  1.-  Calculation of the activity of soluble complexes.
 *  2.-  Calculation of the concentrations of all soluble species
 *       with old values for activity coefficients.
 * @param ivar
 */
private void cBer(int ivar) {
    if(c.dbg >=ERR_XTRA_DEBUG_6) {out.println("cBer("+ivar+") in");}
    // Calculate activities of soluble complexes
    int lix, liax, lia;
    double q;
    for(lix =0; lix <cs.nx; lix++) {
        liax = cs.Na +lix;
        if(noCalc[ivar]) {
            q = Math.abs(cs.a[lix][ivar]);
        } else {
            q = cs.a[lix][ivar];
        }
        lnA[liax] = lnBA[lix] + q * lnA[ivar];
    } //for lix
    // Calculate Concentrations:
    //   components and soluble complexes
    double lnC;
    for(lia =0; lia <nIon; lia++) {
        c.C[lia] = 0;
        if(!cs.noll[lia]) {
            lnC = lnA[lia] - lnG[lia];
            if(lnC >  81) {lnC = 81;} //max concentration 1.5E+35
            if(lnC > -103) { // min concentration 1.8E-45
                c.C[lia] = Math.exp(lnC);
            }
        } //if !noll
    } //for i
    if(c.dbg >=ERR_XTRA_DEBUG_6) {out.println("cBer() returns");}
} // cBer()
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="lnaBer1">
/**
 * lnaBer - Part1
 * Calculates lnKmi and lnA[ibe[]] when some solid phase is assumed to be present.
 */
private void lnaBer1() {
    int li, iF, liaf, lia, lj, ia;
    double w;
    if(c.dbg >=ERR_XTRA_DEBUG_6) {out.println("lnaBer1() in,  nfall="+nfall);}
    // Calculate lnKmi
    for(li = 0; li < nfall; li++) {
        iF = ifall[li];
        liaf = cs.nx + iF;
        w = lnKf[iF];
        for (lia = 0; lia <cs.Na; lia++) {
            if(!ber[lia]) {w = w - cs.a[liaf][lia]*lnA[lia];}
        } //for i
        lnKmi[li] = w;
    } //for li
    // Calculate lnA[ibe[]]
    for(li = 0; li < nfall; li++) {
        w = 0;
            for(lj = 0; lj < nfall; lj++) {
                w = w + lnKmi[lj] * ruta[lj][li];
            } //for lj
        ia = ibe[li];
        lnA[ia] = w;
    } //for li
    if(c.dbg >=ERR_XTRA_DEBUG_6) {out.println("lnaBer1() returns");}
} // lnaBer1()
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="lnaBas">
/** LNABAS: calculates those parts of the activity of the soluble complexes
 * that are independent of Lna(Ivar), (the Lna varied) and stores them in
 * lnBA(ix). This method is called once every time the value of ivar is changed.
 *
 * @param ivar
 * @return x
 */
private double lnaBas(int ivar) {
    int lix; int li;
    if(c.dbg >=ERR_XTRA_DEBUG_6) {out.println("lnaBas("+ivar+") in, lnA["+ivar+"] = "+lnA[ivar]);}
    double w = lnA[ivar];
    double q;
    for(lix = 0; lix < cs.nx; lix++) {
        lnBA[lix] = lnBeta[lix];
        for(li = 0; li < cs.Na; li++) {
            if(li != ivar) {
                if(noCalc[li]) {q = Math.abs(cs.a[lix][li]);} else {q = cs.a[lix][li];}
                lnBA[lix] = lnBA[lix] + q*lnA[li];
            }
        } //for li
    } //for lix
    if(c.dbg >=ERR_XTRA_DEBUG_6) {out.println("lnaBas() returns,  x = "+w);}
    return w;
} // lnaBas(ivar)
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="totBer">
/** TotBer (TotCalc): Calculates Y and compares it with Y0.
 * Calculates the total concentration (Y) of a component given the free
 * concentrations of all components (logA[]). The procedure is different
 * if there are solid phases present or not.<br>
 * Returns <b><code>indik</code>:</b><br>
 * =1 not ok but it is a component only involved in
 * mononuclear reactions and there are no solids present<br>
 * =2 if ok (the Y is equal to Y0 within the tolerance)<br>
 * =3 not ok and it is either not a mono component or there are solids present */
private void totBer() {
    int lix, liax;
    double w = 0;
    if(c.dbg >=ERR_XTRA_DEBUG_6) {out.println("totBer() in, ivar="+ivar+", indik="+indik);}
    try { //catch too many iterations
        y = c.C[ivar];
        if(nfall > 0) { // Some solid is assumed to be present
            for (lix =0; lix < cs.nx; lix++) {
                liax = cs.Na + lix;
                y = y + pva[ivar][lix] * c.C[liax];
            } //for lix
            y = y - totVA[ivar];
            y0 = 0;
            w = Math.abs(y-y0);
            if(tolY[ivar] >0 &&
                    Math.abs(totVA[ivar]) > 1 &&
                    w < Math.abs(tolY[ivar]*totVA[ivar])) {w = 0;}
        } //if nfall !=0
        else { // No solid phase assumed to be present
            for (lix =0; lix < cs.nx; lix++) {
                liax = cs.Na + lix;
                y = y + cs.a[lix][ivar] * c.C[liax];
            } //for lix
            y0 = c.tot[ivar];
            w = Math.abs(y-y0);
        } //if nfall >0

        // Compare Y with Y0
        if(tolY[ivar] < w) { // It was not OK
            if(catchRoundingErrors[ivar] == 3) { //the solution can not be made better: it is uncertain
                indik = 2; // ok
                karl[ivar] = 1;
                steg[ivar] = STEG0_CONT;
                //iter[ivar] = 0;
                if(c.dbg >=ERR_XTRA_DEBUG_6) {prnt(); out.println("totBer() returns; indik = 2 (ok), but \"rounding errors\"; iter["+ivar+"]="+iter[ivar]);}
                return;
            }
            if(mono[ivar] && nfall == 0) { // mononuclear component
                if(y0 <= 0 || y <= 0) {
                    indik = 3;
                    iter[ivar]++;
                    if(iter[ivar] >= ITER_MAX) {throw new TooManyIterationsException();}
                    if(c.dbg >=ERR_XTRA_DEBUG_6) {prnt(); out.println("totBer() returns; indik =3 (not ok & not mono or solids); iter["+ivar+"]="+iter[ivar]);}
                    return;
                }
                lnA[ivar] = lnA[ivar] + Math.log(y0) - Math.log(y);
                x = lnA[ivar];
                indik = 1;
                iter[ivar]++;
                if(iter[ivar] >= ITER_MAX) {throw new TooManyIterationsException();}
                if(c.dbg >=ERR_XTRA_DEBUG_6) {prnt(); out.println("totBer() returns; indik =1 (not ok & mono & no solids), mono["+ivar+"]=true, iter["+ivar+"]="+iter[ivar]);}
                //return;
            } else { // !mono or nfall !=0
                indik = 3;
                iter[ivar]++;
                if(iter[ivar] >= ITER_MAX) {throw new TooManyIterationsException();}
                if(c.dbg >=ERR_XTRA_DEBUG_6) {prnt(); out.println("totBer() returns; indik =3  (not ok & not mono or solids); iter["+ivar+"]="+iter[ivar]);}
                //return;
            } //if !mono | nfall !=0
        } else { // OK
            indik = 2;
            karl[ivar] = 1;
            steg[ivar] = STEG0_CONT;
            iter[ivar]++;
            if(c.dbg >=ERR_XTRA_DEBUG_6) {prnt(); out.println("totBer() returns; indik =2 (ok); iter["+ivar+"]="+iter[ivar]);}
            //return;
        } //if OK
    } //try //try

    catch (TooManyIterationsException ex) {
        if(c.dbg >=ERR_XTRA_DEBUG_6) {out.println("--- Too many iterations for ivar="+ivar);}
        if(ivaBra[ivar] == -1) { // only print error message for the "outer" loop component
            if(c.dbg >= ERR_DEBUG_FASTA_4) {printTooManyIterations(w);} //debug print-out
        }
        if(iter[ivar] > ITER_MAX+1) {
            indik = 2;
            karl[ivar] = 1;
            if(!c.cont) {steg[ivar] = STEG0;} else {steg[ivar] = STEG0_CONT;}
            iter[ivar] = -1; // flag for too many iterations
        }
        if(c.dbg >=ERR_XTRA_DEBUG_6) {prnt(); out.println("totBer() returns; indik ="+indik+"; iter["+ivar+"]="+iter[ivar]+", too many iterations.");}
    } //catch //catch

} // totBer()
private class TooManyIterationsException extends Exception {
    public TooManyIterationsException() {}
    public TooManyIterationsException(String txt) {super(txt);}
} //TooManyIterationsException
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="lnaBer2">
/** lnaBer - Part 2
 * Calculate totVA when a solid phase is assumed to be present. */
private void lnaBer2() {
    if(c.dbg >=ERR_XTRA_DEBUG_6) {out.println("lnaBer2() in, nfall="+nfall);}
    int li, ia, lj;
    double w;
    double[] totMi = new double[MXA];
    for (li=0; li < nfall; li++) {
        ia = ibe[li];
        totMi[li] = c.tot[ia] - c.C[ia];
    } //for li
    for (li=0; li < nvaf; li++) {
        ia = ivaf[li];
        w = c.tot[ia];
        for (lj=0; lj < nfall; lj++) {
            w = w - rut1[li][lj] * totMi[lj];
        } //for lj
        totVA[ia] = w;
    } //for li
    if(c.dbg >=ERR_XTRA_DEBUG_6) {out.println("lnaBer2() return");}
} // lnaBer2()
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="actCoeffs">
/** Calculates activity coefficients (array lnG[]).
 * If the activity coeficients (which depend on the composition of the
 * fluid) have changed since they were last calculated, then
 * the equilibrium composition must be recalculated.
 * @return false if the activity coefficients have changed since last
 * call (not ok); true if the calculations have converged (ok).
 */
private boolean actCoeffs() {
   /* Especially adapted to avoid some oscillatoty behaviour found in some
    * concentrated aqueous solutions, like AlCl3, etc, where the activity
    * coefficients and the aqueous composition are affecting each other in a
    * strong way. The procedure does give slightly slower convergence for
    * solutions of NaCl for example, but it seems more reliable to converge.
    * However, ITERC_MAX must be sufficiently large (perhaps 100?).
    */
   /* If one of the components is water (H2O), it complicates things:
    * if H2O is consumed, for example through the precipitation of a
    * hydrous solid, the mass of the fluid phase decreases and the
    * concentrations must be increased accordingly.
    */
    double diff, newDiff, absDiff, maxAbsDiff, f;
    int i, iMaxDiff;
    if(c.dbg >= ERR_DEBUG_ACT_COEF_5) {out.println("actCoeffs() in");}
    boolean ok, factorOK = true;

    // --- get lnG[]= natural log of activity coefficient ---
    try {factor.factor(c.C, lnG);}
    catch (Exception ex) {
        if(c.dbg >=ERR_ONLY_1) {out.println(ex.getMessage());}
        factorOK = false;
    }
    if(!factorOK) {
        for(i =0; i <nIon; i++) {lnG[i]=0;}
        if(cs.jWater >= 0) {lnA[cs.jWater] = 0;}
        c.errFlagsSet(6);
        if(c.dbg >= ERR_DEBUG_ACT_COEF_5) {
            out.println("actCoeffs() returns.  Error in Factor.");
        }
        return true;
    }
    if(cs.jWater >= 0) { //for H2O
      lnA[cs.jWater] = lnG[cs.jWater];
      //lnG[cs.jWater] = 0;
    }
    
    // --- decide what tolerances in the lnG[] to use
    double tolLnG = TOL_LNG;
    for(i =0; i <nIon; i++) {
        if(c.C[i] < 1) {continue;}
        if(cs.namn.z[i] !=0 && !cs.noll[i]) {
            tolLnG = Math.max(tolLnG, TOL_LNG*c.C[i]);
        }
    }
    tolLnG = Math.min(tolLnG,0.1);
    c.tolLogF = (tolLnG / ln10);

    //print C[], lnG and diff =lnG-oldLnG
    if(c.dbg >= ERR_DEBUG_ACT_COEF_5) {printLnG(0);}
    ok = true;
    iMaxDiff = -1;
    maxAbsDiff = -1;
    for(i =0; i <nIon; i++) {
        if(c.C[i] < 1e-20) {oldLnG[i] = lnG[i]; continue;}
        diff = lnG[i] - oldLnG[i];
        absDiff = Math.abs(diff);
        if(absDiff > tolLnG) {
            ok = false;
            if(maxAbsDiff < absDiff) {iMaxDiff = i; maxAbsDiff = absDiff;}
        }
        // ---- instead of going ahead and use the new activity coefficients in
        //      a new iteration step, for species with "large" conc. we do not
        //      apply the full change: it is scaled down to avoid oscillations
        final double cLim=0.25, fL=0.50;
        // After a few iterations, and for larger concs:
        //     f = fraction giving how much change in lnG should be applied
        if(iterAc >1 && cs.chemConcs.C[i] > cLim) {
            f = fL/Math.sqrt(cs.chemConcs.C[i]);
            // --- f has a value between 1 and 0.1
            newDiff = diff * f;
            if(c.dbg >= ERR_DEBUG_ACT_COEF_5) {
                out.println("note, for \""+cs.namn.ident[i]+"\" C["+i+"] = "
                        +(float)cs.chemConcs.C[i]+" diff="+(float)diff
                        +" f="+(float)f+" applying lnG-change = "
                        +(float)newDiff);
            }
            lnG[i] = oldLnG[i] + newDiff;
        }
        oldLnG[i] = lnG[i];
    } //for i
    if(c.dbg >= ERR_DEBUG_ACT_COEF_5) {
        if(iterAc >0 && !ok) {out.println("New values:"); printLnG(1);}
        if(maxAbsDiff > 0) {
            out.println("Max abs(diff) for \""+cs.namn.ident[iMaxDiff]
                    +"\", lnG["+iMaxDiff+"]="+(float)maxAbsDiff);
        }
    }

    if(ok) { // some aqueous concentration(s) too large?
        for(i =0; i <nIon; i++) {
            if(c.C[i] >20) {
                if(c.dbg >= ERR_DEBUG_ACT_COEF_5) {
                    out.println("note: C["+i+"]="+(float)c.C[i]+" >"+(int)Factor.MAX_CONC+" (too large)");}
                c.errFlagsSet(5);
                break;
            }
        }
    } else {
        iterAc++;
        if(iterAc > ITERAC_MAX) {ok = true; c.errFlagsSet(6);}
    }
    if(c.dbg >= ERR_DEBUG_FASTA_4) {
      if(ok) {
        if(c.isErrFlagsSet(6)) {
            out.print("--- actCoeffs() returns Ok after too many iterations");
        } else {
            out.print("--- actCoeffs() returns OK after "+iterAc+" iterations");
        }
      } else {
          out.print("--- actCoeffs() returns NOT OK. Iter nbr."+iterAc);}
          out.println(", tol="+(float)c.tolLogF
                  +", I="+(float)factor.ionicStr
                  +", el.bal.="+(float)factor.electricBalance);
    }
    return ok;
} // actCoeffs()
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="print arrays, etc">

private void prnt() {
    printArrays(false,true); //print lnA
    out.println("  x="+x+", y="+y+", y0="+y0+", tolY["+ivar+"]="+(float)tolY[ivar]);
}

//<editor-fold defaultstate="collapsed" desc="printTooManyIterations">
private void printTooManyIterations(double w) {
  int n0, nM, iPl, nP;
  out.flush();

  if(iter[ivar] == ITER_MAX) {
    out.println("Error: too many iterations with ivar = "+ivar);
    out.println("Component nbrs. in the order they are iterated (starting with zero):");
    printArrays(true, false); // print iva[]
      n0 = 0;
      nM = cs.Na-1;
      iPl = 5; nP= nM-n0; if(nP >=0) {
    out.print("tot[]= ");
      print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
      out.format(e," %15.7g",c.tot[kjj]);
      if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
      out.println(); out.print("       ");} //for ijj
      }
      n0 = 0;     //start index to print
      nM = nIon + cs.mSol -1;  //end index to print
      iPl = 5; nP= nM-n0; if(nP >=0) { //items_Per_Line and itemsto print
    out.print("C[]= ");
      print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
      out.format(e," %15.7g",c.C[kjj]);
      if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
      out.println(); out.print("     ");} //for ijj
      }
      out.flush();
  } //if iter[ivar] =ITER_MAX

  out.println("Component: "+ivar+", iteration: "+iter[ivar]);
    printArrays(false, true); // print lnA[]
    n0 = 0;     //start index to print
    nM = cs.Na -1;  //end index to print
    iPl = 5; nP= nM-n0; if(nP >=0) { //items_Per_Line and itemsto print
  out.print("  ln f[]=");
    print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
    out.format(e," %15.7g",lnG[kjj]);
    if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
    out.println(); out.print("         ");} //for ijj
    }

/**
  if(iter[ivar] > ITER_MAX) {
    n0 = 0;     //start index to print
    nM = nIon + cs.mSol -1;  //end index to print
    iPl = 5; nP= nM-n0; if(nP >=0) { //items_Per_Line and itemsto print
  out.print("  C[]= ");
    print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
    out.format(e," %15.7g",c.C[kjj]);
    if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
    out.println(); out.print("       ");} //for ijj
    }
  } //if iter >ITER_MAX
*/

  if(nfall == 0) {
      out.format("  Tot(calc) = %17.9g, Tot(input) = %17.9g, tolerance = %10.2g%n",y,y0,tolY[ivar]);
      if(!mono[ivar]) {
        out.format(e,"  low LnA= %23.16g, high LnA= %23.16g%n  low Tot(calc)= %17.9g, high Tot(calc)= %17.9g%n",
        x1[ivar],x2[ivar],y1[ivar],y2[ivar]);
      } //if !mono
    } //if nfall=0
    else {
      out.println("Nbr. solids: "+nfall+",  error in tot.conc. = "+w+", tolerance ="+tolY[ivar]);
      out.format(e,"  low LnA= %23.16g, high LnA= %23.16g%n"+
                          "  low Tot(calc)= %17.9g, high Tot(calc)= %17.9g%n",
                             x1[ivar], x2[ivar], y1[ivar], y2[ivar] );
    } //if nfall>0
  out.flush();
} //printTooManyIterations()
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="printLnG">
/**  print lnG[] and diff =lnG[]-oldLnG[] 
 * @param printDiffs controls printout:<br>
 * =0 print C[] + lnG[] + diffs[] (=lnG[]-oldLnG[])<br>
 * =1 print lnG[]<br>
 * else print lnG[] + diffs[] (=lnG[]-oldLnG[])
 */
private void printLnG(final int printDiffs) {
  int n0, nM, iPl, nP;
  out.flush();
  if(cs.jWater >= 0) {out.format(e,"lnA[H2O]= %10.6f%n",lnA[cs.jWater]);}
  n0 = 0;
  nM = nIon-1;
  iPl = 7; nP= nM-n0; if(nP >=0) {
    if(printDiffs == 0) {
      out.print("    C[]=");
      print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        out.format(e," %10.2g",c.C[kjj]);
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); out.print("        ");} //for ijj
    }
    out.print("  lnG[]=");
    print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        out.format(e," %10.6f",lnG[kjj]);
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); out.print("        ");} //for ijj
    if(printDiffs == 1) {return;}
    out.print("diffs[]=");
    print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        out.format(e," %10.6f",Math.abs(oldLnG[kjj]-lnG[kjj]));
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); out.print("        ");} //for ijj
    out.println("tolerance (log10)= "+(float)(c.tolLogF*ln10)+", I = "+(float)factor.ionicStr+
            ", electric balance = "+(float)factor.electricBalance);
  }
} //printLnG()
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="printArrays">
private void printArrays(boolean print_iva, boolean print_lnA) {
  int n0, nM, iPl, nP;
  out.flush();
  if(print_iva) {
    n0 = 0;     //start index to print
    nM = nva-1;  //end index to print
    iPl = 20; nP= nM-n0; if(nP >=0) { //items_Per_Line and itemsto print
    out.print("   iva[]=");
        print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        out.format(e," %3d",iva[kjj]);
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); out.print("        ");} //for ijj
        }
    n0 = 0;     //start index to print
    nM = nva-1;  //end index to print
    iPl = 20; nP= nM-n0; if(nP >=0) { //items_Per_Line and itemsto print
    out.print("ivaBra[]=");
        print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        out.format(e," %3d",ivaBra[kjj]);
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); out.print("         ");} //for ijj
        }
    n0 = 0;     //start index to print
    nM = nva-1;  //end index to print
    iPl = 20; nP= nM-n0; if(nP >=0) { //items_Per_Line and itemsto print
    out.print("ivaNov[]=");
        print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        out.format(e," %3d",ivaNov[kjj]);
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); out.print("         ");} //for ijj
        }
  }
  if(print_lnA) {
    n0 = 0;
    nM = cs.Na-1;
    iPl = 7; nP= nM-n0; if(nP >=0) {
    out.print("  lnA[]=");
  print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        out.format(e," %10.6f",lnA[kjj]);
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); out.print("        ");} //for ijj
    }
  }
  out.flush();
} //printArrays()
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="printInput">
private void printInput() {
  int n0, nM, iPl, nP;
  out.flush();
  out.println("--- HaltaFall - input data:");
  cs.printChemSystem(out);
  out.println("components: kh=");
      n0 = 0;     //start index to print
      nM = cs.Na-1;  //end index to print
      iPl = 12; nP= nM-n0; if(nP >=0) { //items_Per_Line and itemsto print
      out.print("    ");
      print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
          out.format(e," %5d",c.kh[kjj]);
          if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
          out.println(); out.print("    ");} //for ijj
      }
  out.println("components: tot=");
      n0 = 0;     //start index to print
      nM = cs.Na-1;  //end index to print
      iPl = 7; nP= nM-n0; if(nP >=0) { //items_Per_Line and itemsto print
      out.print("    ");
      print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
          out.format(e," %10.2g",c.tot[kjj]);
          if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
          out.println(); out.print("    ");} //for ijj
      }
  out.println("components: loga=");
      n0 = 0;     //start index to print
      nM = cs.Na-1;  //end index to print
      iPl = 7; nP= nM-n0; if(nP >=0) { //items_Per_Line and itemsto print
      out.print("    ");
      print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
          out.format(e," %10.2f",c.logA[kjj]);
          if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
          out.println(); out.print("    ");} //for ijj
      }
  out.println("components: tol="+(float)c.tol);
  out.println("--- HaltaFall - end of input data.");
  out.flush();
} //printInput()
// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="printArraysFasta()">
/** print arrays ifall, iber, fall, fallA, ber, ivaf, ifSpar, fut */
private void printArraysFasta(
        boolean _ifall, boolean _iber, boolean _fall,
        boolean _fallA, boolean _ber, boolean _ivaf,
        boolean _ifSpar, boolean _fut) {
  int n0, nM, iPl, nP;
  out.flush();
  if(_ifall) {
        n0 = 0;     //start index to print
        nM = nfall-1;  //end index to print
        iPl = 20; nP= nM-n0; if(nP >=0) { //items_Per_Line and itemsto print
      out.print("  ifall[]=");
        print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        out.format(e," %3d",ifall[kjj]);
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); out.print("          ");} //for ijj
        }
  }
  if(_iber) {
        n0 = 0;
        nM = nfall-1;
        iPl = 20; nP= nM-n0; if(nP >=0) {
      out.print("   iber[]=");
        print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        out.format(e," %3d",iber[kjj]);
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); out.print("          ");} //for ijj
        }
  }
  if(_fall) {
        n0 = 0;
        nM = cs.mSol-1;
        iPl = 15; nP= nM-n0; if(nP >=0) {
      out.print("   fall[]=");
        print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        out.format(e," %1b",fall[kjj]);
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); out.print("          ");} //for ijj
        }
  }
  if(_fallA) {
        n0 = 0;
        nM = cs.Na-1;
        iPl = 20; nP= nM-n0; if(nP >=0) {
      out.print("  fallA[]=");
        print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        out.format(e,"  %1b",falla[kjj]);
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); out.print("          ");} //for ijj
        }
  }
  if(_ber) {
        n0 = 0;
        nM = cs.Na-1;
        iPl = 20; nP= nM-n0; if(nP >=0) {
      out.print("    ber[]=");
        print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        out.format(e,"  %1b",ber[kjj]);
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); out.print("          ");} //for ijj
        }
  }
  if(_ivaf) {
        n0 = 0;
        nM = nvaf-1;
        iPl = 20; nP= nM-n0; if(nP >=0) {
      out.print("   ivaf[]=");
        print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        out.format(e," %3d",ivaf[kjj]);
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); out.print("          ");} //for ijj
        }
  }
  if(_ifSpar) {
        n0 = 0;
        nM = nfSpar-1;
        iPl = 15; nP= nM-n0; if(nP >=0) {
      out.print(" ifSpar[]=");
        print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        out.format(" %4d",ifSpar[kjj]);
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); out.print("          ");} //for ijj
        }
  }
  if(_fut) {
        n0 = 0;     //start index to print
        nM = nUt-1;   //end index to print
        iPl = 15; nP= nM-n0; if(nP >=0) {//items_Per_Line and itemsto print
      out.print("    fut[]=");
        print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl +jjj);
        out.format(" %4d",fut[kjj]);
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); out.print("          ");} //for ijj
        }
  }
  out.flush();
} //printArraysFasta()
// </editor-fold>
//<editor-fold defaultstate="collapsed" desc="printMatrix(matrix,length)">
/**
private void printMatrix(double matrix[][], int length) {
  int n0, nM, iPl, nP;
  out.flush();
  for(int lf = 0; lf < length; lf++) {
        n0 = 0;     //start index to print
        nM = length-1;  //end index to print
        iPl = 7; nP= nM-n0; if(nP >=0) { //items_Per_Line and itemsto print
        out.print("  matrix["+lf+"][]=");
        print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        out.format(e," %10.3f",ruta[lf][kjj]);
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for j
        out.println(); out.print("          ");} //for ijj
        }
  }
  out.flush();
} //printMatrix(matrix,length)
*/
// </editor-fold>
// </editor-fold>

//  /**  Throw a NullPointerException: <code>simulateErr(null);</code>
//   * @param n must be "<code>null</code>" to throw an Exception */
//  private static void throwErr(Integer n) {int a = n*2;}

// </editor-fold>

} 