package lib.database;

import lib.common.Util;

/**  Contains data for a "complex": name, stoichiometry, formation equilibrium
 * constant, reference, etc.  May be sorted according to name.
 * <br>
 * Copyright (C) 2014-2019 I.Puigdomenech.
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
public class Complex implements Comparable<Complex>, Cloneable {
  /** the name of the reaction product */
  public String name;
  /** the value of logK at 25 C */
  public double constant;
  /** if <b>true</b>, parameter values (a[]) for an equation describing the
   * logK temperature dependency have been given for this reaction;
   * if <b>false</b> then either a look-up table is used, or
   * the values of detla-H (enthalpy) and delta-Cp (heat capacity)
   * for the reaction have been given, or are empty.
   * @see lib.database.Complex#lookUp lookUp
   * @see lib.database.Complex#a a[]
   * @see lib.database.Complex#tMax tMax */
  public boolean analytic;
  /** if <b>true</b>, a look-up table of logK values (logKarray[][])
   * is used to interpolate values of logK at the desired temperature
   * and pressure; if <b>false</b> then either an analytic power series
   * expression is used, or the values of detla-H (enthalpy) and delta-Cp
   * (heat capacity) for the reaction have been given, or are empty.
   * @see lib.database.Complex#analytic analytic
   * @see lib.database.Complex#logKarray logKarray
   * @see lib.database.Complex#tMax tMax */
  public boolean lookUp;
  /** the parameters for the analytic logK(t) equation<br>
   * (log K(t)= a[0] + a[1] T + a[2]/T +  a[3] log10(T)  + a[4] / T^2 + a[5] T^2)<br>
   * where T is the temperature in Kelvin. If <b>analytic</b> = false, then
   * the enthalpy + heat-capacity values have been given, and
   * values of a[0], a[2] and a[3] are calculated from the enthalpy
   * and heat capacity.
   * @see lib.database.Complex#analytic analytic
   * @see lib.database.Complex#tMax tMax */
  public double[] a;
  /** A grid of logK values, from which values are interpolated at the
   * desired temperature and pressure; pressure is the first index (0 to 4)
   * and temperatures is the second index (0 to 13).  The temperature
   * (degrees Celsius) and pressure (bar) grid is in the following table:
   * <pre>
   * t=  0, 25, 50,  100,  150,  200,  250,  300,  350,400,450,500,550,600C
   * p=  1,  1,  1, 1.01, 4.76, 15.6, 39.8, 85.9,  165,300, - , - , - , -
   *   500,500,500,  500,  500,  500,  500,  500,  500,500,500,650,900,950
   *    1k, 1k, 1k,   1k,   1k,   1k,   1k,   1k,   1k, 1k, 1k, 1k, 1k, 1k
   *    3k, 3k, 3k,   3k,   3k,   3k,   3k,   3k,   3k, 3k, 3k, 3k, 3k, 3k
   *    5k, 5k, 5k,   5k,   5k,   5k,   5k,   5k,   5k, 5k, 5k, 5k, 5k, 5k</pre>
   * <b>Note:</b> use Double.NaN (Not-a-Number) where no data is available,
   * for example at temperatures above 400C and pressures below 300bar.
   * @see lib.database.Complex#lookUp lookUp
   * @see lib.database.Complex#tMax tMax */
  public float[][] logKarray;
  /** The higherst temperature limit (Celsius) for the temperature extrapolations.
   * If no <b>analytic</b> equation has been given (that is,
   * if <b>analytic</b> = false) then: tMax = 25 if values for
   * enthalpy and heat capacity have not been given, tMax = 100
   * if enthalpy has been given but not the heat capacity, and
   * tMax = 200 if both enthalpy and heat capacity have been given
   * for this reaction.
   * @see lib.database.Complex#a a[]
   * @see lib.database.Complex#analytic analytic
   */
  public double tMax;
  /** the names of the reactants */
  public java.util.ArrayList<String> reactionComp;
  /** the stoichiometric coefficients for the reactants */
  public java.util.ArrayList<Double> reactionCoef;
  /** a citation to a reference containing the logK etc */
  public String reference;
  /** a comment, such a chemical formula, etc */
  public String comment;
  /** EMPTY = -999999.9, a value used to indicate missing data **/
  public static final double EMPTY = -999999.9;
  /** ANALYTIC = -888888.8, a value used when reading the enthalpy of reaction
   * from binary databases, to indicate that instead of enthalpy and heat
   * capacity, an anlytic power series expression is used to represent the
   * temperature and pressure  variation of loK
   * @see lib.database.Complex#anaytic analytic
   * @see lib.database.Complex#a a[] */
  public static final double ANALYTIC = -888888.8;
  /** LOOKUP = -888888.8, a value used when reading the enthalpy of reaction
   * from binary databases, to indicate that instead of enthalpy and heat
   * capacity, an anlytic power series expression is used to represent the
   * temperature and pressure  variation of loK
   * @see lib.database.Complex#anaytic analytic
   * @see lib.database.Complex#a a[] */
  public static final double LOOKUP = -777777.7;
  public static final String FILE_FIRST_LINE = "COMPLEX;LogK;DH_kJmol;DCp_Jmol;R1;N1;R2;N2;R3;N3;R4;N4;R5;N5;R6;N6;H;Reference / Comment";
  /** the number of reactants for the "old" complex format */
  public static final int NDIM = 6;
  /** New-line character(s) to substitute "\n" */
  private static final String nl = System.getProperty("line.separator");
  /** natural logarithm of 10 */
  private static final double ln10 = Math.log(10);
  /** the reference temperature = 298.15 K (=25C) */
  private static final double T0 = 298.15;
  /** the gas constant R = 8.3144598 J/(mol K) */
  private static final double R = 8.3144598; // J/(mol K)
  /** R * ln(10) */
  private static final double R_LN10 = R*ln10; // J/(mol K)
  /** (1 + ln(T0) */
  private static final double lnT0plus1 = 1.+Math.log(T0); //

  public Complex() {
    name = "";
    // flags that no value has been given 
    constant = EMPTY;
    analytic = false;
    lookUp = false;
    tMax = 25.;
    a = new double[6];
    for(int i=0; i < a.length; i++) {a[i] = EMPTY;}
    logKarray = new float[5][];
    reactionComp = new java.util.ArrayList<>();
    reactionCoef = new java.util.ArrayList<>();
    reference = "";
    comment = "";
  }

@Override public Complex clone() throws CloneNotSupportedException {
    super.clone();
    Complex c = new Complex();
    c.name = this.name;
    c.constant = this.constant;
    c.analytic = this.analytic;
    c.lookUp = this.lookUp;
    c.tMax = this.tMax;
    if(this.a != null && this.a.length > 0) {System.arraycopy(this.a, 0, c.a, 0, this.a.length);}
    for (int i = 0; i < this.logKarray.length; i++) {
          if (this.logKarray[i] != null) {
              c.logKarray[i] = new float[this.logKarray[i].length];
              System.arraycopy(this.logKarray[i], 0, c.logKarray[i], 0, this.logKarray[i].length);
          }
      }
    for (String t : this.reactionComp) {c.reactionComp.add(t);}
    for (Double w : this.reactionCoef) {c.reactionCoef.add(w);}
    c.reference = this.reference;
    c.comment = this.comment;
    return c;
}

  //<editor-fold defaultstate="collapsed" desc="compareTo(Complex)">
  /** For sorting reaction products. With different names (ignoring case) reaction products are sorted
   * alphabetically.
   * <p>
   * If the name is the same (ignoring case): then if they have the same stoichiometry,
   * they are considered equal. Note: If you store Complexes in a sorted java.util.TreeSet, then
   * you will not be able to have two Complexes that are equal.
   * <p>
   * If the name is the same but the stoichiometry differs: they will be sorted according
   * to their components (and "proton"): the complex with more components goes after,
   * or if equal number of components, the complex with larger sum of (stoichiometric coefficients)
   * is sorted after.
   * <p>
   * <b>Note:</b> this is not fool proof. For example, it does not check if a
   * component is given twice for any of the Complexes, or if a stoichiometric
   * coefficient is given but the component has no name, etc.
   * @param other a Complex
   * @return The compareTo method returns zero if the Complex passed is equal to this instance.
   * It returns a negative value if this Complex is less than (preceedes) the Complex argument;
   * and a value >0 if this Complex is greater than the Complex argument. */
  @Override public int compareTo(Complex other) {
    // --- check for empty names
    if(this.name == null) {
          if(other.name != null) {return -1;} }
    else if(this.name.length() <=0) {
        if(other.name == null) {return 1;}
        if(other.name.length() >0) {return -1;}
    } else { //this name is not empty
        if(other.name == null || other.name.length() <=0) {return 1;} 
    }
    // --- names are not empty
    int k =0;
    if(this.name != null && other.name != null) {k = this.name.compareToIgnoreCase(other.name);}
    // --- names are not equal (ignoring case):
    if(k != 0) {return k;}
    // --- names are equal (ignoring case): check stoichiometry
    k = this.sortReactants().toString().compareToIgnoreCase(other.sortReactants().toString());
    return k;    
  } //compareTo(Complex)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="isEqualTo(Complex)">
/** Check if two Complexes are "equal".
 * The names of species are compared taking into account different ways to write
 * charges so that "Fe+3" is equal to "Fe 3+". Also "CO2" is equivalent to "CO2(aq)".
 * @param other
 * @return true if the two Complexes have the same name and data (equilibrium constant)
 * and if the reaction is the same, even if the order of the reactants differs.
 * For example, <code>A + B = C</code> is equal to <code>B + A = C</code>
 * @see Complex#sameNameAndStoichiometry sameNameAndStoichiometry
 */
  public boolean isEqualTo(Complex other) {
    final boolean dbg = false;
    if(this == null && other == null) {
        if(dbg) {System.out.println("isEqualTo = true (both null)");}
        return true;
    }
    else if(this == null || other == null) {
        if(dbg) {System.out.println("isEqualTo = false (one is null)");}
        return false;
    }
    // -- check names
    if(!Util.nameCompare(this.name, other.name)) {
        if(dbg) {System.out.println("isEqualTo = false (names different)");}
        return false;
    }
    // -- check data
    if(!Util.areEqualDoubles(this.constant, other.constant)) {
        if(dbg) {System.out.println("isEqualTo = false (logK different)");}
        return false;
    }
    if(this.analytic != other.analytic) {
        if(dbg) {System.out.println("isEqualTo = false (analytic flag different)");}
        return false;
    }
    if(this.lookUp != other.lookUp) {
        if(dbg) {System.out.println("isEqualTo = false (look-up flag different)");}
        return false;
    }
    if(!Util.areEqualDoubles(this.tMax, other.tMax)) {
        if(dbg) {System.out.println("isEqualTo = false (tMax different)");}
        return false;
    }
    for(int i = 0; i < this.a.length; i++) {
        if(!Util.areEqualDoubles(this.a[i], other.a[i])) {
            if(dbg) {System.out.println("isEqualTo = false (a["+i+"] different: "+this.a[i]+", "+other.a[i]+")");}
            return false;
        }
    }
    for(int i = 0; i < this.logKarray.length; i++) {
        if(this.logKarray[i] == null && other.logKarray[i] == null) {continue;}
        if((this.logKarray[i] != null && other.logKarray[i] == null)
            || (this.logKarray[i] == null && other.logKarray[i] != null)) {
            if(dbg) {System.out.println("isEqualTo = false (one of logKarray["+i+"] is null.");}
            return false;
        }
        for(int j = 0; j < this.logKarray[i].length; j++) {
            if(!Util.areEqualDoubles(this.logKarray[i][j], other.logKarray[i][j])) {
                if(dbg) {System.out.println("isEqualTo = false (logKarray["+i+"]["+j+"] different: "+
                                            this.logKarray[i][j]+", "+other.logKarray[i][j]+")");}
                return false;
            }
        }
    }
    this.reactionComp.trimToSize();
    other.reactionComp.trimToSize();
    int nTotThis = Math.min(this.reactionComp.size(),this.reactionCoef.size());
    int nTotOther = Math.min(other.reactionComp.size(),other.reactionCoef.size());
    if(nTotThis != nTotOther) {
        if(dbg) {System.out.println("isEqualTo = false (reaction sizes: this="+nTotThis+", other="+nTotOther+" are different)");}
        return false;
    }
    int thisNComps = 0, otherNComps = 0;
    int k;
    double coef1, coef2;
    // -- first check that all components in "this" are found in "other"
    int nTot = Math.min(this.reactionComp.size(),this.reactionCoef.size());
    for(int i=0; i < nTot; i++) {
      coef1 = this.reactionCoef.get(i);
      if(Math.abs(coef1) >= 0.001) {thisNComps++;}
      if(this.reactionComp.get(i) == null || this.reactionComp.get(i).length()<=0) {continue;}
      k =-1;
      for(int i2=0; i2 < nTot; i2++) {
          if(other.reactionComp.get(i2) == null || other.reactionComp.get(i2).length()<=0) {continue;}
          if(Util.nameCompare(this.reactionComp.get(i),other.reactionComp.get(i2))) {k = i2; break;}
      }//for i2
      if(k < 0) { //the component "i" is not present in the other Complex
          if(dbg) {System.out.println("isEqualTo = false (reactions different; "+this.reactionComp.get(i)+")");}
          return false;
      } else { //k >= 0: both Complexes have the same component
          coef2 = other.reactionCoef.get(k);
          if(Math.abs(coef1-coef2) >= 0.001) {
              if(dbg) {System.out.println("isEqualTo = false (reactions different; "+this.reactionComp.get(i)+")");}
              return false;
          }
      } //k
    } //for i
    // -- now check that all components in "other" are also found in "this"
    // so that "other" does not have more components than "this"
    for(int i=0; i < nTot; i++) {
      coef2 = other.reactionCoef.get(i);
      if(Math.abs(coef2) >= 0.001) {otherNComps++;}
      if(other.reactionComp.get(i) == null || other.reactionComp.get(i).length()<=0) {continue;}
      k =-1;
      for(int i2=0; i2 < nTot; i2++) {
          if(this.reactionComp.get(i2) == null || this.reactionComp.get(i2).length()<=0) {continue;}
          if(Util.stringsEqual(other.reactionComp.get(i),this.reactionComp.get(i2))) {k = i2; break;}
      }//for i2
      if(k < 0) { //the component "i" is not present in the other Complex
          if(dbg) {System.out.println("isEqualTo = false (reactions different; "+other.reactionComp.get(i)+")");}
          return false;
      } else { //k >= 0: both Complexes have the same component
          coef1 = this.reactionCoef.get(k);
          if(Math.abs(coef2-coef1) >= 0.001) {
              if(dbg) {System.out.println("isEqualTo = false (reactions different; "+other.reactionComp.get(i)+")");}
              return false;
          }
      } //k
    } //for i
    if(thisNComps != otherNComps) {
        if(dbg) {System.out.println("isEqualTo = false (different nbr reactants)");}
        return false;
    }
    if(!Util.stringsEqual(this.reference,other.reference)) {
        if(dbg) {System.out.println("isEqualTo = false (reference different)");}
        return false;
    }
    if(!Util.stringsEqual(this.comment,other.comment)) {
        if(dbg) {System.out.println("isEqualTo = false (comment different)");}
        return false;
    }
    if(dbg) {System.out.println("isEqualTo = true ("+this.name+")");}
    return true;
  } //isEqualTo(Complex)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="isRedox()">
/** Check if "electron" is one of the reactants.
 * @return true if "electron" is one of the reactants and its stoichiometric coefficient is not zero  */
  public boolean isRedox() {
    int nTot = Math.min(this.reactionComp.size(),this.reactionCoef.size());
    for(int i=0; i < nTot; i++) {
      if(Double.isNaN(this.reactionCoef.get(i)) || Math.abs(this.reactionCoef.get(i)) < 0.0001) {continue;}
      if(this.reactionComp.get(i) == null || this.reactionComp.get(i).trim().length() <=0) {continue;}
      if(Util.isElectron(this.reactionComp.get(i))) {return true;}
    }
    return false;
  } //isRedox(Complex)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="sortReactants()">
  /** Sorts the reactants of a complex in alphabetical order,
   * except that:<ul>
   * <li>If the reaction contains H+, e-, H2O, these reactants are placed first. </li>
   * <li>If two reactants are equal the result is not predictable.</li>
   * <li>Empty reactants (or reactants with zero coefficient) are removed.</li>
   * </ul>
   * @return a Complex with the reactants sorted */
  public Complex sortReactants() {
      int nTot = Math.min(this.reactionComp.size(),this.reactionCoef.size());
      String[] r = new String[nTot];
      double[] d = new double[nTot];
      for(int i = 0; i < nTot; i++) {
          r[i] = this.reactionComp.get(i);
      }
      java.util.Arrays.sort(r);
      for(int i=0; i < r.length; i++) {
          for(int j=0; j < nTot; j++) {
              if(Util.nameCompare(r[i],this.reactionComp.get(j))) {d[i] = this.reactionCoef.get(j);}
          }
      }
      double w;
      String s;
      // move H2O to the top
      for(int i=1; i < r.length; i++) { // if "H2O" is in positio zero, do nothing
          s = r[i]; w = d[i];
          if(Util.isWater(s)) {
              for(int j=i-1; j >= 0; j--) {r[j+1] =r[j];  d[j+1] =d[j];}
              r[0] = s; d[0] = w;
              break;
          }
      }      
      // move e- to the top
      for(int i=1; i < r.length; i++) { // if "e-" is in positio zero, do nothing
          s = r[i]; w = d[i];
          if(Util.isElectron(s)) {
              for(int j=i-1; j >= 0; j--) {r[j+1] =r[j];  d[j+1] =d[j];}
              r[0] = s; d[0] = w;
              break;
          }
      }      
      // move H+ to the top
      for(int i=1; i < r.length; i++) { // if "H+" is in positio zero, do nothing
          s = r[i]; w = d[i];
          if(Util.isProton(s)) {
              for(int j=i-1; j >= 0; j--) {r[j+1] =r[j];  d[j+1] =d[j];}
              r[0] = s; d[0] = w;
              break;
          }
      }      
      this.reactionComp.clear();
      this.reactionCoef.clear();
      for(int i = 0; i < r.length; i++) {
        if(r[i] == null || r[i].trim().length() <= 0 || Math.abs(d[i]) < 0.00001) {continue;}
        this.reactionComp.add(r[i]);
        this.reactionCoef.add(d[i]);
      }
      return this;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="sameNameAndStoichiometry(cmplx1, cmplx2)">
 /** Checks if the name of the two products is equivalent, and if the two Complexes
  * have the same reaction, even if the order of the reactants differs.
  * The names of the reaction products are compared taking into account different ways to write
  * charges so that <nobr>"Fe+3"</nobr> is equal to <nobr>"Fe 3+".</nobr>
  * Also <nobr>"CO2"</nobr> is equivalent to <nobr>"CO2(aq)".</nobr>
  * But <nobr>H4SiO4</nobr> is not equal to <nobr>Si(OH)4,</nobr> and not equal to <nobr>SiO2.</nobr>
  * @param cmplx1
  * @param cmplx2
  * @return <ul><li>if the two Complexes have the same reaction, even if the order of the
  * reactants differs, then returns <code>true</code> if the product names are
  * equvalent and <code>false</code>  if they differ.</li>
  * <li>if the two Complexes have a different reaction it returns <code>false</code></li>
  * <li>For example,<br>
  * (1) <code>A + B = C</code><br>
  * (2) <code>B + A = C</code><br>
  * (3) <code>A + B + H2O = C</code><br>
  * (4) <code>A + B = F</code><br>
  * then (1)+(3) returns <code>false</code> (different reaction),
  * <nobr>(1)+(2)</nobr>  returns <code>true</code> (same reaction, same name),
  * <nobr>(1)+(4)</nobr>  returns <code>false</code> (same reaction, different name).</li></ul>
  * @see lib.database.Complex#checkNameSameStoichiometry(lib.database.Complex, lib.database.Complex) checkNameSameStoichiometry
  * @see lib.database.Complex#isEqualTo isEqualTo
  */
  public static boolean sameNameAndStoichiometry(Complex cmplx1, Complex cmplx2) {
      if(cmplx1.name == null) {
          if(cmplx2.name != null) {return false;} }
      else if(cmplx1.name.length() <=0) {
        if(cmplx2.name == null || cmplx2.name.length() >0) {return false;}
      } else { //cmplx1.name.length() >0
        if(cmplx2.name == null || cmplx2.name.length() <=0) {return false;} 
        if(!Util.nameCompare(cmplx1.name,cmplx2.name)) {return false;}
      }
      if(cmplx1.reactionComp.size() != cmplx2.reactionComp.size() ||
              cmplx1.reactionCoef.size() != cmplx2.reactionCoef.size()) {return false;}
      double coef1, coef2;
      int found;
      // -- first check that all components in "1" are found in "2"
      int nTot = Math.min(cmplx1.reactionComp.size(), cmplx1.reactionCoef.size());
      for(int i=0; i< nTot; i++) {
          if(cmplx1.reactionComp.get(i) == null || cmplx1.reactionComp.get(i).length() <=0) {continue;}
          coef1 = cmplx1.reactionCoef.get(i);
          found =-1;
          for(int i2=0; i2< nTot; i2++) {
              if(cmplx2.reactionComp.get(i2) == null || cmplx2.reactionComp.get(i2).length() <=0) {continue;}
              if(Util.nameCompare(cmplx1.reactionComp.get(i),cmplx2.reactionComp.get(i2))) {found = i2; break;}
          }//for i2
          if(found > -1) {
              coef2 = cmplx2.reactionCoef.get(found);
              if(Math.abs(coef1-coef2) > 0.001) {return false;}
          } //if found
          else { //not found
              return false;
          } //found?
      } //for i
      // -- now check that all components in "2" are also found in "1"
      // so that "2" does not have more components than "1"
      for(int i=0; i< nTot; i++) {
          if(cmplx2.reactionComp.get(i) == null || cmplx2.reactionComp.get(i).length() <=0) {continue;}
          coef2 = cmplx2.reactionCoef.get(i);
          found =-1;
          for(int i2=0; i2< nTot; i2++) {
              if(cmplx1.reactionComp.get(i) == null || cmplx1.reactionComp.get(i).length() <=0) {continue;}
              if(Util.nameCompare(cmplx2.reactionComp.get(i),cmplx1.reactionComp.get(i2))) {found = i2; break;}
          }//for i2
          if(found > -1) {
              coef1 = cmplx1.reactionCoef.get(found);
              if(Math.abs(coef2-coef1) > 0.001) {return false;}
          } //if found
          else { //not found
              return false;
          } //found?
      } //for i
      return true;
  } //sameNameAndStoichiometry
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="toLookUp()">
  /** Sets "lookUp"=true and sets values to logKarray at pSat (the saturated
   * vapor-liquid pressure) for temperatures between 0 and 350 C.
   * The values of logKarray are calculated using the <b>a[]</b>-parameters
   * for the logK(t) equation<br>
   * (log K(t)= a[0] + a[1] T + a[2]/T +  a[3] log10(T)  + a[4] / T^2 + a[5] T^2)<br>
   * where T is the temperature in Kelvin. The value of "tMax" is set to a
   * maximum value of 350.
   * 
   * @see lib.database.Complex#analytic analytic
   * @see lib.database.Complex#a a
   * @see lib.database.Complex#lookUp lookUp
   * @see lib.database.Complex#deltaToA(double, double, double) deltaToA
   * @see lib.database.Complex#logKatTandP(double, double) logKatTandP
   * @see lib.database.Complex#logKarray logKarray
   */
  public void toLookUp() {
      if(lookUp) {return;}
      if(logKarray == null) {logKarray = new float[5][];}
      for(int i=0; i < logKarray.length; i++) {
          if(logKarray[i] == null) {
            logKarray[i] = new float[14];
            for(int j=0; j < logKarray[i].length; j++) {logKarray[i][j] = Float.NaN;}
          }
      }
      double[] t = new double[]{0, 25, 50,  100, 150, 200, 250, 300, 350};
      for(int j=0; j < t.length; j++) {
          logKarray[0][j] = (float)logKatTpSat(t[j]);
          if(!Float.isNaN(logKarray[0][j])) {tMax = Math.max(t[j], tMax);}
      }
      tMax = Math.max(25,Math.min(350, tMax));
      lookUp = true;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="deltaToA(logK0,deltaH,deltaCp)">
  /** returns the <b>a[]</b>-parameters for the logK(t) equation<br>
   * (log K(t)= a[0] + a[1] T + a[2]/T +  a[3] log10(T)  + a[4] / T^2 + a[5] T^2)<br>
   * where T is the temperature in Kelvin, equivalent to the given values of
   * delta-H (entropy) and deltaCp (heat capacity) for the reaction.
   * @param logK0 the log10 of the equilibrium constant at 25C
   * @param deltaH the enthalpy change for the reaction in kJ/mol
   * @param deltaCp the heat capacity change for the reaction in J/(K mol)
   * @return the values of the <b>a[]</b> parameters: all EMPTY except<br>
   *  a[0] = logK + deltaH*1000/(R T0 ln(10)) − deltaCp /(R ln(10)) (1 + ln(T0))<br>
   *  a[2] = -deltaH*1000/(R ln(10)) + deltaCp T0 /(R ln(10)) 
   *  a[3] = deltaCp /R<br>
   * where R is the gas constant, and T0 is the reference temperature (298.15 K).
   * These values are obtained from<pre>
   * logK(T) = logK(T0) - (deltaH/(R ln(10)))((1/T)-(1/T0))-(deltaCp/(R ln(10)))(1-(T0/T)+ln(10)*log(T0/T))
   * logK(T) = logK(T0) + deltaH/(R T0 ln(10)) - deltaCp/(R ln(10))(1+ln(T0))
   *       +(- deltaH/(R ln(10)) + deltaCp T0 /(R ln(10))) / T
   *       +(deltaCp/R) log10(T)</pre>
   * 
   * @see lib.database.Complex#anaytic analytic
   * @see lib.database.Complex#a a[]
   * @see lib.database.Complex#aToDeltaH(double[]) aToDeltaH
   * @see lib.database.Complex#tMax tMax */
  public static double[] deltaToA(final double logK0, final double deltaH, final double deltaCp) {
      double[] a = new double[6];
      for(int i=0; i < a.length; i++) {a[i] = EMPTY;}
      a[0] = logK0;
      if(Double.isNaN(deltaH) || deltaH == EMPTY) {return a;}
      a[0] = a[0] + deltaH*1000/(R_LN10*T0);
      a[2] = - deltaH*1000/R_LN10;
      if(Double.isNaN(deltaCp) || deltaCp == EMPTY) {return a;}
      a[0] = a[0] - (deltaCp * lnT0plus1) / R_LN10;
      a[2] = a[2] + (deltaCp*T0) /R_LN10;
      a[3] = deltaCp / R;
      return a;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="getDeltaH()">
  /** returns the reaction enthalpy calculated from the <b>a[]</b>-parameters
   * for the logK(t) equation<br>
   * (log K(t)= a[0] + a[1] T + a[2]/T +  a[3] log10(T)  + a[4] / T^2 + a[5] T^2<br>
   *  + (a[6] + a[7] T + a[8] log10(T)+ a[9] T^2) log(rho-H2O))<br>
   * (T is the temperature in Kelvin).
   * @return the enthalpy of reaction in kJ/mol<br>
   * deltaH = R ln(10) (a[1] T0^2 − a[2] + a[3] T0 /ln(10) − 2 a[4] / T0 + 2 a[5] T0^3)<br>
   * where R is the gas constant, and T0 is the reference temperature (298.15 K).
   * @see lib.database.Complex#anaytic analytic
   * @see lib.database.Complex#deltaToA(double, double, double) deltaToA
   * @see lib.database.Complex#aToDeltaCp(double[]) aToDeltaCp
   * @see lib.database.Complex#a a[]
   * @see lib.database.Complex#tMax tMax */
  public double getDeltaH() {
      double deltaH;
      if(a[0] == EMPTY || a[2] == EMPTY) {return EMPTY;}
      deltaH = 0.;
      if(a[1] != EMPTY && a[1] != 0.) {deltaH = a[1]*T0*T0;}
      if(a[2] != EMPTY && a[2] != 0.) {deltaH = deltaH - a[2];}
      if(a[3] != EMPTY && a[3] != 0.) {deltaH = deltaH + a[3]*T0/ln10;}
      if(a[4] != EMPTY && a[4] != 0.) {deltaH = deltaH - 2.*a[4]/T0;}
      if(a[5] != EMPTY && a[5] != 0.) {deltaH = deltaH - 2.*a[5]*T0*T0*T0;}
      return R_LN10*deltaH/1000.;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="getDeltaCp()">
  /** returns the reaction heat capacity calculated from the <b>a[]</b>-parameters
   * for the logK(t) equation<br>
   * (log K(t)= a[0] + a[1] T + a[2]/T +  a[3] log10(T)  + a[4] / T^2 + a[5] T^2<br>
   *  + (a[6] + a[7] T + a[8] log10(T)+ a[9] T^2) log(rho-H2O))<br>
   * (T is the temperature in Kelvin).
   * @return the heat capacity of reaction in J/(K mol)<br>
   * deltaCp = deltaCp = R ln(10) (2 a[1] T0 + a[3]/ln(10) + 2 a[4] / T0^2 + 6 a[5] T0^2)<br>
   * where R is the gas constant, and T0 is the reference temperature (298.15 K).
   * @see lib.database.Complex#anaytic analytic
   * @see lib.database.Complex#deltaToA(double, double, double) deltaToA
   * @see lib.database.Complex#aToDeltaH(double[]) aToDeltaH
   * @see lib.database.Complex#a a[]
   * @see lib.database.Complex#tMax tMax */
  public double getDeltaCp() {
      double deltaCp;
      if(a[0] == EMPTY || a[2] == EMPTY || a[3] == EMPTY) {return EMPTY;}
      deltaCp = 0.;
      if(a[1] != EMPTY && a[1] != 0.) {deltaCp = 2.*a[1]*T0;}
      if(a[3] != EMPTY && a[3] != 0.) {deltaCp = deltaCp + a[3]/ln10;}
      if(a[4] != EMPTY && a[4] != 0.) {deltaCp = deltaCp + 2.*a[4]/(T0*T0);}
      if(a[5] != EMPTY && a[5] != 0.) {deltaCp = deltaCp + 6.*a[5]*T0*T0;}
      return R_LN10*deltaCp;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="fromString(text)">
/** Get a Complex with data read within a sting with ";" separated values.
 * The comment (if any) is read after a slash "/" following either a semicolon, a space, or a comma.
 * @param textLines for example: "<code>Fe 3+;-13.02;;;Fe 2+;1;;;e-;-1;;;;;;;0;Wateq4F /comment</code>"
 * @return an instance of Complex or <code>null</code> if the reaction product is empty or "COMPLEX"
 * @see lib.database.Complex#toString() toString
 * @throws lib.database.Complex.ReadComplexException */
  public static Complex fromString(String textLines) throws ReadComplexException {
    if(textLines == null || textLines.length() <= 0
            || textLines.trim().startsWith("/")) {return null;}
    // Check if the input text string contains several lines
    java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.StringReader(textLines))) {
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        } catch (java.io.IOException exc) {
            String msg;
            if(textLines.length() <=40) {msg = textLines;} else {msg = textLines.substring(0, 38)+"...";}
            throw new ReadComplexException("\"Complex.fromString("+msg+"): "+nl+
                lib.common.Util.stack2string(exc));
        }
    StringBuilder nowReading = new StringBuilder();
    java.util.ArrayList<String> aList;
    int i,j,nr, n, line = -1;
    String t;
    double deltaH, deltaCp;
    Complex c = new Complex();
    // -- loop through the lines
    //    (in most cases there is only one line)
    for(String text: lines) {
        line++;
        if(text.indexOf(';')<0 && text.indexOf(',')<0) {
            if(line == 0 && text.startsWith("@")) {text = text.trim()+";";}
            else {throw new ReadComplexException("Line \""+text+"\""+nl+
                    "contains neither semicolons nor commas"+nl+
                    "in \"Complex.fromString()\"");}
        }
        try{aList = CSVparser.splitLine(text);}
        catch (CSVparser.CSVdataException ex) {throw new ReadComplexException("CSVdataException: "+
                ex.getMessage()+nl+"in \"Complex.fromString()\"");}
        n = 0;
        //System.out.println("line = "+text);
        if(c.lookUp) { // this means that this is not the first line
            //System.out.println("line nr="+line);
            c.logKarray[line-1] = new float[14];
            try{
                for(i = 0; i < c.logKarray[line-1].length; i++) {
                    nowReading.replace(0, nowReading.length(), "logKarray["+(line-1)+"]["+i+"]");
                    if(n < aList.size()) {
                        if(aList.get(n).length() >0) {c.logKarray[line-1][i] = Float.parseFloat(aList.get(n));}
                        else {c.logKarray[line-1][i] = Float.NaN;}
                    } else {
                        throw new ReadComplexException("Error in \"Complex.fromString()\":"+nl+
                            "missing data in line: "+text+nl+
                            "while reading "+nowReading.toString()+nl+"for \""+c.name+"\"");
                    }
                    n++;
                } // for a[i]
            } catch (NumberFormatException ex) {
                throw new ReadComplexException("Error in \"Complex.fromString()\":"+nl+
                    ex.toString()+nl+"in line: "+text+nl+
                    "while reading "+nowReading.toString()+"");
            }
            continue;
        } // if c.lookUp
        try{
            nowReading.replace(0, nowReading.length(), "name");
            if(n < aList.size()) {c.name = aList.get(n);} else {
                throw new ReadComplexException("Error in \"Complex.fromString()\":"+nl+
                            "missing reaction product name in line: \""+text+"\"");
            }
            if(c.name.equalsIgnoreCase("COMPLEX")) {return null;}
            if(c.name.length() <=0) {
                for (i=1; i<aList.size(); i++) { // check that the reaction is empty. Comments are OK
                    if(aList.get(i) != null && aList.get(i).trim().length() >0) {
                        throw new ReadComplexException(
                            "Empty reaction product in line:"+nl+"   \""+text+"\""+nl+
                            "found in \"Complex.fromString()\"");
                    }
                }
                return null;
            }
            n++;
            nowReading.replace(0, nowReading.length(), "logK");
            if(n < aList.size()) {
                if(aList.get(n).length() >0) {c.constant = Double.parseDouble(aList.get(n));
                } else {c.constant = EMPTY;}
            } else {
                throw new ReadComplexException("Error in \"Complex.fromString()\":"+nl+
                    "missing data while reading "+nowReading.toString()+nl+"for \""+c.name+"\"");
            }
            n++;
            nowReading.replace(0, nowReading.length(), "delta-H or \"analytic\"");
            if(n < aList.size()) {
                t = aList.get(n).trim();
                if(t != null && t.length() >0) {
                    if(t.equalsIgnoreCase("analytic") || t.equalsIgnoreCase("-analytic")) {c.analytic = true;}
                    if(t.toLowerCase().startsWith("lookup") || t.toLowerCase().startsWith("-lookup")) {c.lookUp = true;}
                }
            } else {
                throw new ReadComplexException("Error in \"Complex.fromString()\":"+nl+
                    "missing data while reading "+nowReading.toString()+nl+"for \""+c.name+"\"");
            }
            n++;
            if(!c.analytic) {
                if(!c.lookUp) { // not analytic and not look-up table
                    if(t != null && t.length() >0) {deltaH = Double.parseDouble(t);} else {deltaH = EMPTY;}
                    nowReading.replace(0, nowReading.length(), "delta-Cp");
                    if(n < aList.size()) {
                        if(aList.get(n).length() >0) {deltaCp = Double.parseDouble(aList.get(n));} else {deltaCp = EMPTY;}
                    } else {
                        throw new ReadComplexException("Error in \"Complex.fromString()\":"+nl+
                            "missing data while reading "+nowReading.toString()+nl+"for \""+c.name+"\"");
                    }
                    n++;
                    c.tMax = 25.;
                    if(deltaH != EMPTY) {c.tMax = 100.; if(deltaCp != EMPTY) {c.tMax = 200.;}}
                    c.a = deltaToA(c.constant, deltaH, deltaCp);
                } else { // look-up Table
                    nowReading.replace(0, nowReading.length(), "Max-temperature");
                    c.tMax = 25.;
                    if(n < aList.size()) {
                        if(aList.get(n).length() >0) {c.tMax = Double.parseDouble(aList.get(n));}
                    } else {
                        throw new ReadComplexException("Error in \"Complex.fromString()\":"+nl+
                            "missing data while reading "+nowReading.toString()+nl+"for \""+c.name+"\"");
                    }
                    n++;            
                } // lookUp?
            } else { // analytic
                nowReading.replace(0, nowReading.length(), "Max-temperature");
                c.tMax = 25.;
                if(n < aList.size()) {
                    if(aList.get(n).length() >0) {c.tMax = Double.parseDouble(aList.get(n));}
                } else {
                    throw new ReadComplexException("Error in \"Complex.fromString()\":"+nl+
                        "missing data while reading "+nowReading.toString()+nl+"for \""+c.name+"\"");
                }
                n++;
                for(i = 0; i < c.a.length; i++) {
                    nowReading.replace(0, nowReading.length(), "parameter a["+i+"]");
                    if(n < aList.size()) {
                        if(aList.get(n).length() >0) {c.a[i] = Double.parseDouble(aList.get(n));
                        } else {c.a[i] = EMPTY;}
                    } else {
                        throw new ReadComplexException("Error in \"Complex.fromString()\":"+nl+
                        "missing data while reading "+nowReading.toString()+nl+"for \""+c.name+"\"");
                    }
                    n++;
                } // for a[i]
            } // analytic
            double n_H = 0;
            boolean thereisHplus = false, foundReactant;
            c.reactionComp.clear();
            c.reactionCoef.clear();
            int nTot;
            nowReading.replace(0, nowReading.length(), "name of 1st reactant or nbr of reactants in reaction");
            if(n < aList.size()) {t = aList.get(n);} else {
                throw new ReadComplexException("Error in \"Complex.fromString()\":"+nl+
                    "missing data while reading "+nowReading.toString()+nl+"for \""+c.name+"\"");
            }
            n++;
            if(t.trim().length() > 0) {
                try{nTot = Integer.parseInt(t);} catch (NumberFormatException ex) {nTot = -1;}
            } else {nTot = -1;}
            if(nTot <= 0) { //  ---- old format ----
                nr = 0;
                for(i =0; i < NDIM; i++) {
                    if(i >0) {
                        nowReading.replace(0, nowReading.length(), "name of reactant["+(i+1)+"]");
                        if(n < aList.size()) {t = aList.get(n);} else {
                            throw new ReadComplexException("Error in \"Complex.fromString()\":"+nl+
                                "missing data while reading "+nowReading.toString()+nl+"for \""+c.name+"\"");
                        }
                        n++;
                    }
                    foundReactant = false;
                    if(!t.isEmpty()) {
                        nr++;
                        c.reactionComp.add(t);
                        foundReactant = true;
                    }
                    nowReading.replace(0, nowReading.length(), "coefficient for reactant["+(i+1)+"]");
                    if(n < aList.size()) {
                        if(foundReactant) {
                            if(aList.get(n).length() >0) {c.reactionCoef.add(Double.parseDouble(aList.get(n)));} else {c.reactionCoef.add(0.);}
                        }
                    } else {
                            throw new ReadComplexException("Error in \"Complex.fromString()\":"+nl+
                                "missing data while reading "+nowReading.toString()+nl+"for \""+c.name+"\"");
                    }
                    n++;
                    if(nr > 0 && Util.isProton(c.reactionComp.get(nr-1))) {thereisHplus = true; n_H = c.reactionCoef.get(nr-1);}
                } //for i
                nowReading.replace(0, nowReading.length(), "number of H+");
                if(n < aList.size()) {
                    if(aList.get(n).length() >0) {
                        n_H = Double.parseDouble(aList.get(n));
                    }
                } else {
                    throw new ReadComplexException("Error in \"Complex.fromString()\":"+nl+
                        "missing data while reading "+nowReading.toString()+nl+"for \""+c.name+"\"");
                }
                n++;
                if(!thereisHplus && Math.abs(n_H) > 0.00001) {c.reactionComp.add("H+"); c.reactionCoef.add(n_H);}
            } else { // nTot >0  ---- new format ----
                for(i =0; i < nTot; i++) {
                    nowReading.replace(0, nowReading.length(), "name of reactant["+(i+1)+"]");
                    if(n < aList.size()) {c.reactionComp.add(aList.get(n));} else {
                        throw new ReadComplexException("Error in \"Complex.fromString()\":"+nl+
                            "missing data while reading "+nowReading.toString()+nl+"for \""+c.name+"\"");
                    }
                    n++;
                    nowReading.replace(0, nowReading.length(), "coefficient for reactant["+(i+1)+"]");
                    if(n < aList.size()) {
                        if(aList.get(n).length() >0) {
                            c.reactionCoef.add(Double.parseDouble(aList.get(n)));
                        } else {c.reactionCoef.add(0.);}
                    } else {
                        throw new ReadComplexException("Error in \"Complex.fromString()\":"+nl+
                            "missing data while reading "+nowReading.toString()+nl+"for \""+c.name+"\"");
                    }
                    n++;
                } //for i
            } // new format?
        } catch (NumberFormatException ex) {
            throw new ReadComplexException("Error in \"Complex.fromString()\":"+nl+
                    ex.toString()+nl+"in line: "+text+nl+
                    "while reading "+nowReading.toString()+nl+"for \""+c.name+"\"");
        }
        nowReading.replace(0, nowReading.length(), "reference");
        if(n < aList.size()) {c.reference = aList.get(n);} else {
                throw new ReadComplexException("Error in \"Complex.fromString()\":"+nl+
                    "missing data while reading "+nowReading.toString()+nl+"for \""+c.name+"\"");
        }
        n++;
        while (n  < aList.size()) {
            c.reference = c.reference +","+ aList.get(n);
            n++;
        }
        //System.out.println("\""+c.name+"\", ref="+c.reference);
        // remove ";" or "," at the beginning
        while (true) {
            if(c.reference.startsWith(";") || c.reference.startsWith(",")) {
                c.reference = c.reference.substring(1).trim();
            } else {break;}
        }
        c.reference = c.reference.trim();
        if(c.reference.startsWith("/")) {
                c.comment = c.reference.substring(1).trim();
                c.reference = "";
        } else {
                int commentStart = c.reference.length();
                j = c.reference.indexOf(";/");
                if(j > -1 && j < commentStart) {commentStart = j;}
                j = c.reference.indexOf(",/");
                if(j > -1 && j < commentStart) {commentStart = j;}
                j = c.reference.indexOf(" /");
                if(j > -1 && j < commentStart) {commentStart = j;}
                if(commentStart >=0 && commentStart < c.reference.length()) {
                    c.comment = c.reference.substring((commentStart+2), c.reference.length()).trim();
                    c.reference = c.reference.substring(0, commentStart).trim();
                }
        }
    } // for(String text: lines)
    if(c.lookUp) {
        for(i = 0; i < c.logKarray.length; i++) {
            if(c.logKarray[i] == null) {
                throw new ReadComplexException("Error in \"Complex.fromString()\":"+nl+
                    "missing line "+(i+1)+" of logK look-up table for \""+c.name+"\"");
            }
        }
    }
    return c;
  } // fromString
  public static class ReadComplexException extends Exception {
    public ReadComplexException() {super();}
    public ReadComplexException(String txt) {super(txt);}
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="toString()">
/** converts a Complex into a String, such as:
 * "<code>Fe 3+;-13.02;;;Fe 2+;1;;;e-;-1;;;;;;;0;Wateq4F /comment</code>"
 * @return the string describing the Complex object
 * @see lib.database.Complex#fromString(java.lang.String) fromString
 */
  @Override
  public String toString() {
    StringBuilder text = new StringBuilder();
    double w;
    text.append(encloseInQuotes(this.name)); text.append(";");

    if(this.name.startsWith("@")) {return text.toString();}
    if(this.constant != EMPTY) {text.append(Util.formatDbl3(this.constant).trim());}
    text.append(";");
    // If there is no second row of logKarray, it means that the look-up-table
    // (if it is not null) has been constructed from array a[].
    boolean thereIsLookUpTable = false;
    if(this.logKarray[1] != null) {
        for(int j = 0; j < this.logKarray[1].length; j++) {
            if(!Float.isNaN(this.logKarray[1][j])) {thereIsLookUpTable = true; break;}
        }
    }
    // are there protons involved in the reaction?
    boolean proton = false;
    double n_H = 0;
    int nTot = Math.min(this.reactionComp.size(),this.reactionCoef.size());
    for(int ic =0; ic < nTot; ic++) {
      if(this.reactionComp.get(ic) == null || this.reactionComp.get(ic).length()<=0) {continue;}
      if(Math.abs(this.reactionCoef.get(ic)) < 0.0001) {continue;}
      if(Util.isProton(this.reactionComp.get(ic))) {n_H = this.reactionCoef.get(ic); proton = true;}
    }
    if(nTot > 7 || (nTot == 7 && !proton)  || this.analytic || this.lookUp && thereIsLookUpTable) {
        System.out.println("toStringShort()...");
        return this.toStringShort();
    }
    // delta-H and delta-Cp
    w = this.getDeltaH();
    if(w != EMPTY) {text.append(Util.formatDbl3(w).trim());}
    text.append(";");
    w = this.getDeltaCp();
    if(w != EMPTY) {text.append(Util.formatDbl3(w).trim());}
    text.append(";");
    for(int ic =0; ic < 6; ic++) {
      if(ic >= nTot || this.reactionComp.get(ic) == null || this.reactionComp.get(ic).length()<=0) {
          text.append(";;");
          continue;
      } else {
          text.append(encloseInQuotes(this.reactionComp.get(ic))); text.append(";");
      }
      if(Math.abs(this.reactionCoef.get(ic)) > 0.0001) {
          text.append(Util.formatDbl4(this.reactionCoef.get(ic)).trim());
      }
      text.append(";");
    }
    if(Math.abs(n_H) >=0.001) {text.append(Util.formatDbl4(n_H).trim());} text.append(";");
    String t;
    if(this.comment != null && this.comment.length() >0) {
        t = this.reference + " /" + this.comment;
    } else {
        t = this.reference;
    }
    text.append(encloseInQuotes(t));
    return text.toString();
  } // toString()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="toStringShort()">
/** converts a Complex into a String, such as:
 * "<code>Fe 3+;-13.02;;;2;Fe 2+;1;e-;-1;0;Wateq4F / comment</code>"
 * @return the string describing the Complex object
 * @see lib.database.Complex#fromString(java.lang.String) fromString
 */
  // @Override
  public String toStringShort() {
    StringBuilder text = new StringBuilder();
    double w;
    text.append(encloseInQuotes(this.name)); text.append(";");

    if(this.name.startsWith("@")) {return text.toString();}
    if(this.constant != EMPTY) {text.append(Util.formatDbl3(this.constant).trim());}
    text.append(";");
    // If there is no second row of logKarray, it means that the look-up-table
    // (if it is not null) has been constructed from array a[].
    boolean thereIsLookUpTable = false;
    if(this.logKarray[1] != null) {
        for(int j = 0; j < this.logKarray[1].length; j++) {
            if(!Float.isNaN(this.logKarray[1][j])) {thereIsLookUpTable = true; break;}
        }
    }
    if(this.analytic) {
        text.append("analytic;");
        if(this.tMax == EMPTY || this.tMax < 25) {this.tMax = 25.;}
        text.append(Util.formatNumAsInt(this.tMax).trim());
        text.append(";");
        for (int i = 0; i < this.a.length; i++) {
            if(this.a[i] != EMPTY && this.a[i] !=0) {
                text.append(Util.formatDbl6(this.a[i]).trim());
            }
            text.append(";");
        }
    } else if(this.lookUp && thereIsLookUpTable) {
        text.append("lookUpTable;");
        if(this.tMax == EMPTY || this.tMax < 25) {this.tMax = 25.;}
        text.append(Util.formatNumAsInt(this.tMax).trim());
        text.append(";");
    } else { // delta-H and delta-Cp
        w = this.getDeltaH();
        if(w != EMPTY) {text.append(Util.formatDbl3(w).trim());}
        text.append(";");
        w = this.getDeltaCp();
        if(w != EMPTY) {text.append(Util.formatDbl3(w).trim());}
        text.append(";");
    }
    int nTot = Math.min(this.reactionComp.size(),this.reactionCoef.size());
    text.append(Integer.toString(nTot));
    text.append(";");
    for(int ic =0; ic < nTot; ic++) {
      if(this.reactionComp.get(ic) == null || this.reactionComp.get(ic).length()<=0) {
          text.append(";;");
          continue;
      } else {
          text.append(encloseInQuotes(this.reactionComp.get(ic))); text.append(";");
      }
      if(Math.abs(this.reactionCoef.get(ic)) > 0.0001) {
          text.append(Util.formatDbl4(this.reactionCoef.get(ic)).trim());
      }
      text.append(";");
    }
    String t;
    if(this.comment != null && this.comment.length() >0) {
        t = this.reference + " /" + this.comment;
    } else {
        t = this.reference;
    }
    text.append(encloseInQuotes(t));
    if(this.lookUp && thereIsLookUpTable) {
        for(int i = 0; i < this.logKarray.length; i++) {
            text.append(nl);
            if(this.logKarray[i] == null) {
                this.logKarray[i] = new float[14];
                for(int j = 0; j < this.logKarray[i].length; j++) {this.logKarray[i][j] = Float.NaN;}
            }
            for(int j = 0; j < this.logKarray[i].length; j++) {
                if(!Float.isNaN(this.logKarray[i][j])) {
                    text.append(Util.formatDbl3(this.logKarray[i][j]));
                } else {text.append("NaN");}
                text.append(";");
            }
        }
    }
    return text.toString();
  } // toStringShort()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="sortedReactionString()">
  /** Returns a String representation of the reaction such as:<pre>
   * "<code>Fe 2+;1;;;e-;-1;;;;;;;0;</code>"</pre>
   * Equivalent to <code>Complex.toString</code>, except that (1) the product name,
   * the logK and the reference are excluded, and (2) the reactants are sorted.
   * That is, it returns only the reaction (sorted).
   * If the product name starts with "@" this method returns an empty String ("").
   * @return a text representing the reaction
   * @see lib.database.Complex#toString() Complex.toString  */
    public String sortedReactionString() {
    if(this.name.startsWith("@")) {return "";}
    Complex c;
    try {c = (Complex)this.clone();}
    catch(CloneNotSupportedException cex) {return "";}
    // -- exclude water?
    //for(int ic =0; ic < Complex.NDIM; ic++) {
    //  if(Util.isWater(c.component[ic])) {c.component[ic] = ""; c.numcomp[ic] = 0; break;}
    //}
    StringBuilder text = new StringBuilder();  
    //text.append(Complex.encloseInQuotes(c.name)); text.append(";");
    c.sortReactants();
    int nTot = Math.min(c.reactionComp.size(),c.reactionCoef.size());
    for(int ic =0; ic < nTot; ic++) {
      if(c.reactionComp.get(ic) == null || c.reactionComp.get(ic).length()<=0
              // || Util.isWater(c.component[ic])
              || Math.abs(c.reactionCoef.get(ic)) < 0.0001) {text.append(";;"); continue;}
      text.append(Complex.encloseInQuotes(c.reactionComp.get(ic))); text.append(";");
      text.append(Util.formatDbl4(c.reactionCoef.get(ic)).trim()); text.append(";");
    }
    return text.toString();
  } //sortedReactionString()
//</editor-fold>  

  //<editor-fold defaultstate="collapsed" desc="reactionText()">
/** returns a simple, easy to read description of the reaction defining
 * this complex. For example: "Fe 2+ + H2O = FeOH- + H+".
 * @return a text describing the reaction
 */
  public String reactionText() {
    StringBuilder text = new StringBuilder();
    StringBuffer stoich = new StringBuffer();
    boolean first = true;
    double aX;
    int nTot = Math.min(this.reactionComp.size(),this.reactionCoef.size());
    for(int ic =0; ic < nTot; ic++) {
        aX = this.reactionCoef.get(ic);
        if(aX > 0 && this.reactionComp.get(ic) != null && this.reactionComp.get(ic).length() >0) {
            if(first) {
                stoich.delete(0, stoich.length());
                first = false;
            } else {
                stoich.delete(0, stoich.length());
                stoich.append(" +");
            }//first?
            if(Math.abs(aX-1)>0.001) { // aX != +1
                stoich.append(Util.formatDbl3(aX));
            }
            text.append(stoich.toString());  text.append(" ");  text.append(this.reactionComp.get(ic));
        }//if aX > 0
    }//for ic
    text.append(" = ");
    first = true;
    for(int ic =0; ic < nTot; ic++) {
        aX = -this.reactionCoef.get(ic);
        if(aX > 0 && this.reactionComp.get(ic) != null && this.reactionComp.get(ic).length() >0) {
            if(first) {
                stoich.delete(0, stoich.length());
                first = false;
            } else {
                stoich.delete(0, stoich.length());
                stoich.append(" +");
            }//first?
            if(Math.abs(aX-1)>=0.001) { // aX != +1
                stoich.append(Util.formatDbl3(aX));
            }
            text.append(stoich.toString());  text.append(" ");  text.append(this.reactionComp.get(ic));
        }//if aX > 0
    }//for ic
    stoich.delete(0, stoich.length());
    if(first) {stoich.append(" ");} else {stoich.append(" + ");}
    text.append(stoich); text.append(this.name);
    String t = text.toString().trim();
    text.delete(0, text.length());
    text.append(t);
    if(t.startsWith("=") && t.contains("@")) {text.deleteCharAt(0);}
    return text.toString();
  } //reactionText
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="reactionTextWithLogK(temperature)">
/** returns a simple, easy to read description of the reaction defining
 * this complex, including the equilibrium constant (logK) and reference code.
 * For example: "Fe 2+ + H2O = FeOH- + H+;  logK=X.XX [99ref]".
 * @param tC temperature in degrees Celsius, to calculate logK
 * @param pBar pressure in bar, to calculate logK
 * @return a text describing the reaction, the equilibrium constant and the reference code
 */
  public String reactionTextWithLogK(double tC, double pBar) {
    StringBuilder text = new StringBuilder();
    double lgK;
    text.append(reactionText());
    if(this.constant != EMPTY && !this.name.startsWith("@")) {
        lgK = this.logKatTandP(tC, pBar);
        if(!Double.isNaN(lgK) && lgK != EMPTY) {text.append(";  logK="); text.append(Util.formatDbl3(lgK));}
        else {text.append(";  logK= ??");}
    } else {
        if(!this.name.startsWith("@")) {text.append(";  logK= ??");}
    }
    text.append(" ");
    if(this.comment != null && this.comment.length() >0) {text.append(" ("); text.append(this.comment); text.append(")");}
    if(this.reference != null && this.reference.length() >0) {text.append(" ["); text.append(this.reference); text.append("]");}
    String t = text.toString().trim();
    text.delete(0, text.length());
    text.append(t);
    if(t.startsWith("=") && t.contains("@")) {text.deleteCharAt(0);}
    return text.toString();
  } //reactionText
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="logKatTpSat(tC)">
 /** Returns the logK value at the requested temperature (below the
  * critical point) and at the saturated liquid-vapor pressure (pSat).
  * @param tC0 the temperature in degrees Celsius
  * @return the logK value at the requested temperature and pSat.
  * It returns NaN (not-a-number) if the logK value can not be calculated,
  * or if the temperature is either NaN or above the critical point
  * or higher than tMax
  * @see lib.database.Complex#tMax tMax
  * @see lib.database.Complex#analytic analytic
  * @see lib.database.Complex#a a
  * @see lib.database.Complex#lookUp lookUp
  * @see lib.database.Complex#logKarray logKarray  */
  public double logKatTpSat(final double tC0) {
    if(Double.isNaN(tC0)) {return Double.NaN;}
    this.tMax = Math.min(600,Math.max(25, this.tMax));
    if(tC0 > this.tMax+0.0001) {return Double.NaN;}
    double tC = Math.max(tC0,0.01); // triple point of water
    if(tC >= 373.946) {return Double.NaN;} // crtitical point of water
    if(this.lookUp)  {
        if(logKarray[0] == null) {return Double.NaN;}
        //   t=  0, 25, 50, 100, 150, 200, 250, 300, 350 C
        float[] logK = new float[9];
        System.arraycopy(logKarray[0], 0, logK, 0, logK.length);
        return lib.kemi.interpolate.Interpolate.interpolate2D((float)tC, logK);
    } else {
        return analyticExpression(this, tC);
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="logKatTandP(tC,pBar)">
 /** Returns the logK value at the requested temperature and pressure
  * if it can be calculated. 
  * @param tC0 the temperature in degrees Celsius
  * @param pBar the pressure in bar
  * @return the logK value at the requested temperature and pressure.
  * It returns NaN (not-a-number) if (a) the logK value can not be calculated
  * (t-P in the vapor region or in the low-density region) or (b) if either
  * the temperature or the pressure are NaNs, or (c) if tC0 is higher than tMax,
  * or (d) if lookUp is false and pBar is larger than 221 bar.
  * @see lib.database.Complex#tMax tMax
  * @see lib.database.Complex#analytic analytic
  * @see lib.database.Complex#a a
  * @see lib.database.Complex#lookUp lookUp
  * @see lib.database.Complex#logKarray logKarray  */
  public double logKatTandP(final double tC0, final double pBar) {
    if(Double.isNaN(tC0) || Double.isNaN(pBar)) {return Double.NaN;}
    this.tMax = Math.min(600,Math.max(25, this.tMax));
    if(tC0 > this.tMax+0.0001) {return Double.NaN;}
    double tC = Math.max(tC0,0.01); // triple point of water
    boolean thereIsLookUpTable = false;
    if(this.logKarray[1] != null) {
        for(int j = 0; j < this.logKarray[1].length; j++) {
            if(!Float.isNaN(this.logKarray[1][j])) {thereIsLookUpTable = true; break;}
        }
    }
    if(tC < 373.946) { // crtitical point of water
        double pSat = Math.max(1,IAPWSF95.pSat(tC));
        if(pBar < (pSat*0.998)) {return Double.NaN;} // below saturated liquid-vapor pressure
        else if(Math.abs(pBar-pSat) < pSat*0.002) { // pBar = pSat
            return logKatTpSat(tC);
        }
    } else if(tC == 373.946) {return Double.NaN;}
    double[] tLimit = new double[]{373.946,400,410,430,440,460,470,490,510,520,540,550,570,580,600};
    double[] pLimit = new double[]{        300,350,400,450,500,550,600,650,700,750,800,850,900,950};
    for(int i = 0; i < (tLimit.length-1); i++) {
        if(tC > tLimit[i] && tC <= tLimit[i+1] && pBar < pLimit[i]) {return Double.NaN;}
    }
    // If there is no second row of logKarray, it means that the look-up-table
    // (if it is not null) has been constructed from array a[].
    if(thereIsLookUpTable)  {
        return lib.kemi.interpolate.Interpolate.interpolate3D((float)tC, (float)pBar, this.logKarray);
    } else {
        if(pBar > 221) {return Double.NaN;}
        return analyticExpression(this, tC);
    }
  }

  //<editor-fold defaultstate="collapsed" desc="Not used: constCp(complex,tC)">
  /** Extrapolates the log10 of an equilibrium constant "logK0" using the
   * the constant heat capacity approximation, or if deltaCp is not provided,
   * using the constant enthalpy (van't Hoff) equation. The temperature is
   * forced to be in the range 0 to 100 degrees Celsius. 
   * @param cmplx
   * @param tC
   * @return logK(tC) calculated using the reaction enthalpy and heat capacity.
   * It returns NaN if either of the input parameters is NaN or if
   * cmplx.constant = EMPTY.
   * @see Complex#EMPTY EMPTY
   */
  /**
  private static double constantCp(Complex cmplx, final double tC) {
    if(Double.isNaN(cmplx.constant) || cmplx.constant == EMPTY || Double.isNaN(tC)) {
        MsgExceptn.exception("Constant heat-capacity model for \""+cmplx.name+":"+nl+
                "    logK="+cmplx.constant+", temperature = "+tC);
        return Double.NaN;
    }
    if(tC>24.9 && tC<25.1) {return cmplx.constant;}
    if(tC > 100.1) {
        MsgExceptn.exception("Constant heat-capacity model for \""+cmplx.name+":"+nl+
            "    temperature = "+tC+" (must be < 100).");
        return Double.NaN;
    }
    if(Double.isNaN(cmplx.deltH) || cmplx.deltH == EMPTY) {
        MsgExceptn.exception("Constant heat-capacity model for \""+cmplx.name+": no enthalpy data.");
        return Double.NaN;
    }
    final double tK =  273.15 + Math.min(350,Math.max(0,tC));
    final double T0 = 298.15;
    final double R_LN10 = 19.1448668; // J/(mol K)
    double logK = cmplx.constant + (cmplx.deltH * 1000 / R_LN10) * ((1/T0) - (1/tK));
    if(Double.isNaN(cmplx.deltCp) || cmplx.deltCp == EMPTY) { // no heat capacity
        if(tC > 50.1) {
            MsgExceptn.exception("Constant heat-capacity model for \""+cmplx.name+":"+nl+
                    "    temperature = "+tC+" and no delta-Cp data (needed for t > 50 C).");
            return Double.NaN;
        }
        else {return logK;}
    }
    // there a value for the heat capacity:
    logK = logK + (cmplx.deltCp / R_LN10) * ((T0/tK)-1+Math.log(tK/T0));
    return logK;
  }
  // */
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="analyticExpression(complex,tC)">
  /** Extrapolates the log10 of an equilibrium constant "logK0" using the
   * an analytic expression of logK(t)
   * @param cmplx
   * @param tC the temperature (degrees Celsius, between 0 and cmplx.tMax degrees)
   * @return the logK(t,p)
   * It returns NaN if either of the input parameters is NaN or if the array
   * "a" is not defined. Returns EMPTY if logK0 = EMPTY.
   * @see lib.database.Complex#deltaToA(double, double, double) deltaToA
   * @see lib.database.Complex#analytic analytic
   * @see lib.database.Complex#a a[]
   * @see lib.database.Complex#tMax tMax
   * @see lib.database.Complex#EMPTY EMPTY
   */
  private static double analyticExpression(Complex cmplx, final double tC) {
    if(Double.isNaN(cmplx.constant) || cmplx.constant == EMPTY
            || Double.isNaN(tC)) {return Double.NaN;}
    if(tC>24.999 && tC<25.001) {return cmplx.constant;}
    if(cmplx.a[0] == EMPTY) {return Double.NaN;}
    if(cmplx.tMax == EMPTY || cmplx.tMax < 25) {cmplx.tMax = 25;}
    cmplx.tMax = Math.min(600,cmplx.tMax);
    if(tC > cmplx.tMax+0.0001) {return Double.NaN;}
    final double tK = Math.max(0.01,tC)+273.15; // triple point of water
    double logK = cmplx.a[0];
    // log K = A0 + A1 T + A2/T +  A3 log(T)  + A4 / T^2 + A5 T^2
    if(cmplx.a[1] != EMPTY && cmplx.a[1] != 0.) {logK = logK + cmplx.a[1]*tK;}
    if(cmplx.a[2] != EMPTY && cmplx.a[2] != 0.) {logK = logK + cmplx.a[2]/tK;}
    if(cmplx.a[3] != EMPTY && cmplx.a[3] != 0.) {logK = logK + cmplx.a[3]*Math.log10(tK);}
    if(cmplx.a[4] != EMPTY && cmplx.a[4] != 0.) {logK = logK + cmplx.a[4]/(tK*tK);}
    if(cmplx.a[5] != EMPTY && cmplx.a[5] != 0.) {logK = logK + cmplx.a[5]*(tK*tK);}
    return logK;
  }
  //</editor-fold>

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="isChargeBalanced()">
  /** is the reaction specified in "complex" charge balanced?
   * @return true if the reaction specified in "complex" is charge balanced
   */
  public boolean isChargeBalanced() {
    if(this.name.startsWith("@")) {
        //prefix "@" means remove such a component/complex if already included
        return true;
    }
    double totCharge = Util.chargeOf(this.name);
    int nTot = Math.min(this.reactionComp.size(),this.reactionCoef.size());
    for(int i=0; i < nTot; i++) {
      if(this.reactionComp.get(i) == null || this.reactionComp.get(i).length() <=0 || 
            Math.abs(this.reactionCoef.get(i)) < 0.0001) {continue;}
      totCharge = totCharge -
              this.reactionCoef.get(i) * Util.chargeOf(this.reactionComp.get(i));
    } //for i
    return Math.abs(totCharge) < 0.001;
  } //isChargeBalanced(complex)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="check()">
/** Check that<ul>
 * <li>if a component is given, it has non-zero stoichiometric coefficient, and
 * <i>vice-versa</i>, that if a reaction coefficient is given, the component name is
 * not empty
 * <li>the reaction is charge balanced
 * <li>that if "H+" is given as one of the component names, its reaction
 * coeffitient and "proton" agree</ul>
 * 
 * @return either an error message or "null"
 */
  public String check() {
    StringBuilder sb = new StringBuilder();
    boolean start = true;
    if(this.reactionComp.size() != this.reactionCoef.size()) {
        if(start) {sb.append("Complex: \"");
        sb.append(this.name);
        sb.append("\""); sb.append(nl); start = false;}
        sb.append("Length of reaction arrays (components and coefficients) do not match.");
    }
    int nTot = Math.min(this.reactionComp.size(), this.reactionCoef.size());
    for(int i=0; i < nTot; i++) {
        if((this.reactionComp.get(i) == null || this.reactionComp.get(i).length() <=0)
                && Math.abs(this.reactionCoef.get(i)) >= 0.001) {
            if(start) {sb.append("Complex: \"");
            sb.append(this.name);
            sb.append("\""); sb.append(nl); start = false;}
            sb.append("Reaction coefficient given with no component.");
        } else if(this.reactionComp.get(i) != null && this.reactionComp.get(i).length() >0) {
            if(Math.abs(this.reactionCoef.get(i)) < 0.0001) {
            if(start) {sb.append("Complex: \"");
            sb.append(this.name);
            sb.append("\""); sb.append(nl); start = false;}
            sb.append("No reaction coefficient for component \"");
            sb.append(this.reactionComp.get(i)); sb.append("\"");
            }
        }
    }//for i
    //--- check for charge balance
    if(!this.isChargeBalanced()) {
        if(sb.length() >0) {sb.append(nl);}
        if(start) {sb.append("Complex: \"");
        sb.append(this.name);
        sb.append("\""); sb.append(nl);}
        sb.append("Reaction is not charge balanced.");
    }
    if(sb.length() > 0) {return sb.toString();} else {return null;}
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="encloseInQuotes(text)">
/** Enclose a text in quotes if:<br>
 * - it starts with either a single quote or a double quote<br>
 * - it contains either a comma or a semicolon<br>
 * If the text is enclosed in quotes, an enclosed quote is duplicated. For example
 * if text is "a'b" then the procedure returns '"a''b"'. 
 * @param text
 * @return <code>text</code> enclose in quotes if needed
 */
  public static String encloseInQuotes(String text) {
    if(text == null) {return null;}
    if(text.length()<=0) {return text;}
    StringBuilder sb = new StringBuilder(text);
    if(sb.charAt(0) == '"') {
        duplicateQuote(sb,'\'');
        return ("'"+sb.toString()+"'");
    }
    else if(sb.charAt(0) == '\'') {
        duplicateQuote(sb,'"');
        return ("\""+sb.toString()+"\"");
    }
    else if(sb.indexOf(",") >-1 || sb.indexOf(";") >-1) { // contains separator
        if(sb.indexOf("\"") >-1) {
            duplicateQuote(sb,'\'');
            return ("'"+sb.toString()+"'");
        } else {
            duplicateQuote(sb,'"');
            return ("\""+sb.toString()+"\"");
        }
    } //if separator
    return text;
  } //encloseInQuotes(text)
  private static void duplicateQuote(StringBuilder sb, char quote) {
    int pos = 0;
    int l = sb.length();
    while(pos < l) {
        if(sb.charAt(pos) == quote) {sb.insert(pos, quote); l++; pos++;}
        pos++;
    } //while
  } //duplicateQuote
  //</editor-fold>

}
