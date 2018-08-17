package lib.database;

import lib.common.Util;

/**  Contains data for a "complex": name, stoichiometry, formation equilibrium
 * constant, reference, etc.  May be sorted according to name.
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
public class Complex implements Comparable<Complex>, Cloneable {
  public String name;
  public double constant;
  public double deltH;
  public double deltCp;
  public String[] component;
  public double[] numcomp;
  public double proton;
  public String reference;
  public String comment;
  /** EMPTY = -999999.9, a value used to indicate missing data. **/
  public static final double EMPTY = -999999.9;
  public static final String FILE_FIRST_LINE = "COMPLEX;LogK;DH_kJmol;DCp_Jmol;R1;N1;R2;N2;R3;N3;R4;N4;R5;N5;R6;N6;H;Reference / Comment";
  public static final int NDIM = 6;
  /** the number of values needed in a text line to read or to store a <code>Complex</code> */
  public static final int NDATA = 18;
  private java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
  /** New-line character(s) to substitute "\n" */
  private static final String nl = System.getProperty("line.separator");

  public Complex() {
    name = "";
    component = new String[NDIM];
    numcomp = new double[NDIM];
    for(int i =0; i<NDIM; i++) {component[i] = ""; numcomp[i] =0;}
    // flags that no value has been given 
    constant = EMPTY;
    deltH = EMPTY;
    deltCp = EMPTY;
    proton = 0;
    reference = "";
    comment = "";
  }
@Override public Object clone() throws CloneNotSupportedException {
    super.clone();
    Complex c = new Complex();
    c.name = this.name;
    c.constant = this.constant;
    c.deltH = this.deltH;
    c.deltCp = this.deltCp;
    for(int i =0; i<NDIM; i++) {c.component[i] = this.component[i]; c.numcomp[i] =this.numcomp[i];}
    c.proton = this.proton;
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
    boolean reactionEqual = true;
    int nComps = 0;
    double sumStoich = 0;
    boolean protonGiven = false;
    int iOther;
    for(int i=0; i < NDIM; i++) {
      if(this.component[i] == null || this.component[i].length()<=0) {continue;}
      if(Math.abs(this.numcomp[i]) >= 0.001) {
          nComps++;
          sumStoich = sumStoich + this.numcomp[i];
          if(Util.isProton(this.component[i])) {protonGiven = true;}
      }
      iOther =-1;
      for(int i2=0; i2 < NDIM; i2++) {
          if(other.component[i2] == null || other.component[i2].length()<=0) {continue;}
          if(Util.nameCompare(this.component[i],other.component[i2])) {iOther = i2; break;}
      }//for i2
      if(iOther < 0) { //the component "i" is not present in the other Complex
        if(Util.isProton(this.component[i])) {
            if(Math.abs(this.numcomp[i]-other.proton) >= 0.001) {
              reactionEqual = false;
            }
        } //H+
        else { //not H+: this Complex has a component which is absent in the other
          reactionEqual = false;            
        }
      } else { //iOther >= 0: both Complexes have the same component
        if(Math.abs(this.numcomp[i]-other.numcomp[iOther]) >= 0.001) {
          reactionEqual = false;
        }
      } //if found
    } //for i
    if(!protonGiven && Math.abs(this.proton) >= 0.001) {
          nComps++;
          sumStoich = sumStoich + this.proton;
    }
    if(Math.abs(this.proton-other.proton) >= 0.001) {reactionEqual = false;}
    // --- if same name and same stoichiometry: they are equivalent
    if(reactionEqual) {return 0;}
    // --- same name but different reactions:
    int nCompsOther = 0;
    double sumStoichOther = 0;
    protonGiven = false;
    for(int i=0; i < NDIM; i++) {
      if(other.component[i] == null || other.component[i].length()<=0) {continue;}
      if(Math.abs(other.numcomp[i]) >= 0.001) {
          nCompsOther++;
          sumStoichOther = sumStoichOther + other.numcomp[i];
          if(Util.isProton(other.component[i])) {protonGiven = true;}
      }
    } //for i
    if(!protonGiven && Math.abs(other.proton) >= 0.001) {
          nCompsOther++;
          sumStoichOther = sumStoichOther + other.proton;
    }
    // --- with equal names a Complex is sorted after if the reaction has more components
    if((nComps - nCompsOther) != 0) {return (nComps - nCompsOther);}
    // --- if both Complexes have the same name and both have no reaction: they are equal
    if((nComps + nCompsOther) == 0) {return 0;}
    // --- nComps != nCompsOther != 0
    // --- with equal names a Complex is sorted after if its reaction has largest sum(stoichiometric coeffs.)
    double diff = sumStoich - sumStoichOther;
    if(Math.abs(diff) >=0.001) {if(diff > 0) {return 1;} else {return -1;}}
    // --- if both Complexes have no reaction: they are equal
    if(Math.abs(sumStoich + sumStoichOther) < 0.001) {return 0;}
    // --- still equal:
    //   names equal
    //   nComps = nCompsOther != 0
    //   sumAbsStoich = sumAbsStoichOther != 0
    // --- there must a difference somewhere!
    for(int i=0; i < NDIM; i++) {
      if(this.component[i] == null || this.component[i].length()<=0) {continue;} //take a non-empty component name
      //if the other is empty, then this is sorted after
      if(other.component[i] == null || other.component[i].length()<=0) {return 1;}
      //both components have non-empty names:
      k = this.component[i].compareToIgnoreCase(other.component[i]);
      diff = this.numcomp[i] - other.numcomp[i];
      if(k!=0 || Math.abs(diff) >= 0.001) {break;}
    } //for i
    if(k!=0) {return k;} else {
        if(Math.abs(diff) < 0.001) {return 0;}
        if(diff > 0) {return 1;} else {return -1;}
    }
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
    // -- check for empty names
    if(!Util.nameCompare(this.name, other.name)) {
        if(dbg) {System.out.println("isEqualTo = false (names different)");}
        return false;
    }
    if(!Util.areEqualDoubles(this.constant, other.constant)) {
        if(dbg) {System.out.println("isEqualTo = false (logK different)");}
        return false;
    }
    if(!Util.areEqualDoubles(this.deltH, other.deltH)) {
        if(dbg) {System.out.println("isEqualTo = false (deltH different)");}
        return false;
    }
    if(!Util.areEqualDoubles(this.deltCp, other.deltCp)) {
        if(dbg) {System.out.println("isEqualTo = false (deltCp different)");}
        return false;
    }
    boolean ok = true;
    int thisNComps = 0, otherNComps = 0;
    int k;
    // -- first check that all components in "this" are found in "other"
    for(int i=0; i < NDIM; i++) {
      if(Math.abs(this.numcomp[i]) >= 0.001) {thisNComps++;}
      if(this.component[i] == null || this.component[i].length()<=0) {continue;}
      k =-1;
      for(int i2=0; i2 < NDIM; i2++) {
          if(other.component[i2] == null || other.component[i2].length()<=0) {continue;}
          if(Util.nameCompare(this.component[i],other.component[i2])) {k = i2; break;}
      }//for i2
      if(k < 0) { //the component "i" is not present in the other Complex
          if(dbg) {System.out.println("isEqualTo = false (reactions different; "+this.component[i]+")");}
          return false;
      } else { //iOther >= 0: both Complexes have the same component
          if(Math.abs(this.numcomp[i]-other.numcomp[k]) >= 0.001) {
              if(dbg) {System.out.println("isEqualTo = false (reactions different; "+this.numcomp[i]+")");}
              return false;
          }
      } //k
    } //for i
    // -- now check that all components in "other" are also found in "this"
    // so that "other" does not have more components than "this"
    for(int i=0; i < NDIM; i++) {
      if(Math.abs(other.numcomp[i]) >= 0.001) {otherNComps++;}
      if(other.component[i] == null || other.component[i].length()<=0) {continue;}
      k =-1;
      for(int i2=0; i2 < NDIM; i2++) {
          if(this.component[i2] == null || this.component[i2].length()<=0) {continue;}
          if(Util.stringsEqual(other.component[i],this.component[i2])) {k = i2; break;}
      }//for i2
      if(k < 0) { //the component "i" is not present in the other Complex
          if(dbg) {System.out.println("isEqualTo = false (reactions different; "+other.component[i]+")");}
          return false;
      } else { //iOther >= 0: both Complexes have the same component
          if(Math.abs(this.numcomp[i]-other.numcomp[k]) >= 0.001) {
              if(dbg) {System.out.println("isEqualTo = false (reactions different; "+other.numcomp[k]+")");}
              return false;
          }
      } //k
    } //for i
    if(thisNComps != otherNComps) {
        if(dbg) {System.out.println("isEqualTo = false (reactions different)");}
        return false;
    }
    if(!Util.areEqualDoubles(this.proton, other.proton)) {
        if(dbg) {System.out.println("isEqualTo = false (proton different)");}
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

  //<editor-fold defaultstate="collapsed" desc="isRedox(Complex)">
/** Check if "electron" is one of the reactants.
 * @param c
 * @return true if "electron" is one of the reactants and its stoichiometric coefficient is not zero  */
  public static boolean isRedox(Complex c) {
    if(c == null) {return false;}
    for(int i=0; i < NDIM; i++) {
      if(Double.isNaN(c.numcomp[i]) || Math.abs(c.numcomp[i]) < 0.001) {continue;}
      if(c.component[i] == null || c.component[i].trim().length() <=0) {continue;}
      if(Util.isElectron(c.component[i])) {return true;}
    } //for i
    return false;
  } //isRedox(Complex)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="sortReactants(cmplx)">
  /** sorts the reactants of a complex in alphabetical order,
   * except that empty reactants are placed at the end.
   * <p>If two reactants are equal the result is not predictable
   * @param cmplx */
  public static void sortReactants(Complex cmplx) {
      String[] r = new String[NDIM];
      System.arraycopy(cmplx.component, 0, r, 0, NDIM);
      for(int i=0; i < NDIM-1; i++) { // the last one is not checked
          if(r[i] != null) {r[i] = r[i].trim();}
      }
      java.util.Arrays.sort(r);
      double[] d = new double[NDIM];
      for(int i=0; i < NDIM; i++) {
          if(r[i] == null || r[i].length() <=0) {d[i] = 0; continue;}
          for(int j=0; j < NDIM; j++) {
              if(Util.nameCompare(r[i],cmplx.component[j])) {d[i] = cmplx.numcomp[j];}
          }
      }
      // move empty reactants to the end
      double w;
      if(r[0] == null || r[0].length() <=0) { //there is an empty reactant
        String s;
        for(int i=0; i < NDIM-1; i++) { // the last one is not checked
          s = r[0]; w = d[0];
          for(int j=0; j < NDIM-1; j++) {r[j] =r[j+1];  d[j] =d[j+1];}
          r[NDIM-1] = s; // = "";
          d[NDIM-1] = w; // = 0;
          if(r[0] != null && r[0].length() >0) {break;} //no more empty reactants
        } //for i
      }
      System.arraycopy(r, 0, cmplx.component, 0, NDIM);
      System.arraycopy(d, 0, cmplx.numcomp, 0, NDIM);
  } //sortReactants
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
  * @see Complex#checkNameSameStoichiometry(lib.database.Complex, lib.database.Complex) checkNameSameStoichiometry
  * @see Complex#isEqualTo isEqualTo
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
      if(Math.abs(cmplx1.proton-cmplx2.proton) > 0.001) {return false;}
      int found;
      // -- first check that all components in "1" are found in "2"
      for(int i=0; i< NDIM; i++) {
          if(cmplx1.component[i] == null || cmplx1.component[i].length() <=0) {continue;}
          found =-1;
          for(int i2=0; i2< NDIM; i2++) {
              if(cmplx2.component[i] == null || cmplx2.component[i].length() <=0) {continue;}
              if(Util.nameCompare(cmplx1.component[i],cmplx2.component[i2])) {found = i2; break;}
          }//for i2
          if(found > -1) {
              if(Math.abs(cmplx1.numcomp[i]-cmplx2.numcomp[found]) > 0.001) {return false;}
          } //if found
          else { //not found
              if(Util.isProton(cmplx1.component[i])) {
                  if(Math.abs(cmplx1.numcomp[i]-cmplx2.proton) > 0.001) {return false;}
              } //H+
              else {return false;}
          } //found?
      } //for i
      // -- now check that all components in "2" are also found in "1"
      // so that "2" does not have more components than "1"
      for(int i=0; i< NDIM; i++) {
          if(cmplx2.component[i] == null || cmplx2.component[i].length() <=0) {continue;}
          found =-1;
          for(int i2=0; i2< NDIM; i2++) {
              if(cmplx1.component[i] == null || cmplx1.component[i].length() <=0) {continue;}
              if(Util.nameCompare(cmplx2.component[i],cmplx1.component[i2])) {found = i2; break;}
          }//for i2
          if(found > -1) {
              if(Math.abs(cmplx2.numcomp[i]-cmplx1.numcomp[found]) > 0.001) {return false;}
          } //if found
          else { //not found
              if(Util.isProton(cmplx2.component[i])) {
                  if(Math.abs(cmplx2.numcomp[i]-cmplx1.proton) > 0.001) {return false;}
              } //H+
              else {return false;}
          } //found?
      } //for i
      return true;
  } //sameNameAndStoichiometry
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="checkNameSameStoichiometry(cmplx1, cmplx2)">
 /** If the two Complexes have the same reaction, even if the order of the reactants
  * differs, checks if the name of the product is equivalent.
  * The names of the reaction products are compared taking into account different ways to write
  * charges so that <nobr>"Fe+3"</nobr> is equal to <nobr>"Fe 3+".</nobr>
  * Also <nobr>"CO2"</nobr> is equivalent to <nobr>"CO2(aq)".</nobr>
  * But <nobr>H4SiO4</nobr> is not equal to <nobr>Si(OH)4,</nobr> and not equal to <nobr>SiO2.</nobr>
  * Note also that names of reactants must match exactly: <nobr>"Fe+3"</nobr> is NOT equal
  * to <nobr>"Fe 3+"</nobr> and <nobr>"H+"</nobr> is not equal to <nobr>"H +".</nobr>
  * @param cmplx1
  * @param cmplx2
  * @return <ul><li>if the two Complexes have the same reaction, even if the order of the
  * reactants differs, then returns <code>true</code> if the product names are
  * equvalent and <code>false</code> if they differ.</li>
  * <li>if the two Complexes have a different reaction it returns <code>true</code></li>
  * <li>For example,<br>
  * (1) <code>A + B = C</code><br>
  * (2) <code>B + A = C</code><br>
  * (3) <code>A + B + H2O = C</code><br>
  * (4) <code>A + B = F</code><br>
  * then (1)+(3) returns <code>true</code> (different reaction),
  * <nobr>(1)+(2)</nobr>  returns <code>true</code> (same reaction, same name),
  * <nobr>(1)+(4)</nobr>  returns <code>false</code> (same reaction, different name).</li></ul>
  * @see Complex#sameNameAndStoichiometry(lib.database.Complex, lib.database.Complex) sameNameAndStoichiometry
  * @see Complex#isEqualTo(lib.database.Complex) isEqualTo
  */
  public static boolean checkNameSameStoichiometry(Complex cmplx1, Complex cmplx2) {
      if(Math.abs(cmplx1.proton-cmplx2.proton) > 0.001) {return true;}
      int found;
      boolean ok = true;
      // -- first check that all components in "1" are found in "2"
      for(int i=0; i< NDIM; i++) {
          if(cmplx1.component[i] == null || cmplx1.component[i].length() <=0) {continue;}
          found =-1;
          for(int i2=0; i2< NDIM; i2++) {
              if(cmplx2.component[i] == null || cmplx2.component[i].length() <=0) {continue;}
              if(Util.nameCompare(cmplx1.component[i],cmplx2.component[i2])) {found = i2; break;}
          }//for i2
          if(found > -1) {
              if(Math.abs(cmplx1.numcomp[i]-cmplx2.numcomp[found]) > 0.001) {return true;}
          } //if found
          else { //not found
              if(Util.isProton(cmplx1.component[i])) {
                  if(Math.abs(cmplx1.numcomp[i]-cmplx2.proton) > 0.001) {return true;}
              } //H+
              else {return true;}
          } //found?
      } //for i
      // -- now check that all components in "2" are also found in "1"
      // so that "2" does not have more components than "1"
      for(int i=0; i< NDIM; i++) {
          if(cmplx2.component[i] == null || cmplx2.component[i].length() <=0) {continue;}
          found =-1;
          for(int i2=0; i2< NDIM; i2++) {
              if(cmplx1.component[i] == null || cmplx1.component[i].length() <=0) {continue;}
              if(Util.nameCompare(cmplx2.component[i],cmplx1.component[i2])) {found = i2; break;}
          }//for i2
          if(found > -1) {
              if(Math.abs(cmplx2.numcomp[i]-cmplx1.numcomp[found]) > 0.001) {return true;}
          } //if found
          else { //not found
              if(Util.isProton(cmplx2.component[i])) {
                  if(Math.abs(cmplx2.numcomp[i]-cmplx1.proton) > 0.001) {return true;}
              } //H+
              else {return true;}
          } //found?
      } //for i
      // so far so good, bot reactions have the same reaction
      // is the product the same?
      if(cmplx1.name == null) {
          if(cmplx2.name != null) {return false;}
      } else if(cmplx1.name.length() <=0) {
        if(cmplx2.name == null || cmplx2.name.length() >0) {return false;}
      } else { //cmplx1.name.length() >0
        if(cmplx2.name == null || cmplx2.name.length() <=0) {return false;} 
        if(!Util.nameCompare(cmplx1.name,cmplx2.name)) {return false;}
      }
      return true;
  } //checkNameSameStoichiometry
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="fromString()">
/** Get a Complex with data read within a sting with ";" separated values.
 * The comment (if any) is read after a slash "/" following either a semicolon, a space, or a comma.
 * @param text for example: "<code>Fe 3+;-13.02;;;Fe 2+;1;;;e-;-1;;;;;;;0;Wateq4F /comment</code>"
 * @return an instance of Complex or <code>null</code> if the reaction product is empty or "COMPLEX"
 * @see Complex#toString() toString
 * @throws Complex.ReadComplexException */
  public static Complex fromString(String text) throws ReadComplexException {
    if(text == null || text.length() <= 0
            || text.trim().startsWith("/")) {return null;}
    if(text.indexOf(';')<0 && text.indexOf(',')<0) {
        if(text.startsWith("@")) {text = text.trim()+";";}
        else {throw new ReadComplexException("Line \""+text+"\""+nl+
                "contains neither semicolons nor commas"+nl+
                "in \"Complex.fromString()\"");}
    }
    //System.out.println("line = "+text);
    java.util.ArrayList<String> aList;
    try{aList = CSVparser.splitLine_N(text, NDATA);}
    catch (CSVparser.CSVdataException ex) {
        throw new ReadComplexException("CSVdataException: "+ex.getMessage()+nl+
                "in \"Complex.fromString()\"");
    }
    StringBuilder nowReading = new StringBuilder();
    Complex c = new Complex();
    int n = 0;
    double n_H = 0;
    try{
        nowReading.replace(0, nowReading.length(), "name");
        if(n < aList.size()) {c.name = aList.get(n);} else {return c;}
        if(c.name.equalsIgnoreCase("COMPLEX")) {return null;}
        if(c.name.length() <=0) {
            for (int i=1; i<(NDATA-1); i++) { // check that the reaction is empty. Comments are OK
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
        } else {return c;}
        n++;
        nowReading.replace(0, nowReading.length(), "delta-H");
        if(n < aList.size()) {
            if(aList.get(n).length() >0) {c.deltH = Double.parseDouble(aList.get(n));
            } else {c.deltH = EMPTY;}
        } else {return c;}
        n++;
        nowReading.replace(0, nowReading.length(), "delta-Cp");
        if(n < aList.size()) {
            if(aList.get(n).length() >0) {c.deltCp = Double.parseDouble(aList.get(n));
            } else {c.deltCp = EMPTY;}
        } else {return c;}
        n++;
        for(int i =0; i < NDIM; i++) {
            nowReading.replace(0, nowReading.length(), "name of reactant["+(i+1)+"]");
            if(n < aList.size()) {c.component[i] = aList.get(n);} else {return c;}
            n++;
            nowReading.replace(0, nowReading.length(), "number for reactant["+(i+1)+"]");
            if(n < aList.size()) {
                if(aList.get(n).length() >0) {c.numcomp[i] = Double.parseDouble(aList.get(n));
                } else {c.numcomp[i] = 0;}
            } else {return c;}
            if(Util.isProton(c.component[i])) {n_H = c.numcomp[i];}
            n++;
        } //for i
        nowReading.replace(0, nowReading.length(), "number of H+");
        if(n < aList.size()) {
            if(aList.get(n).length() >0) {
                c.proton = Double.parseDouble(aList.get(n));
            } else {
                c.proton = n_H;
            }
        } else {return c;}
        n++;
        nowReading.replace(0, nowReading.length(), "reference");
        if(n < aList.size()) {c.reference = aList.get(n);} else {return c;}
        //System.out.println("ref=´"+c.reference+"´");
        n++;
        while (n  < aList.size()) {
            c.reference = c.reference +","+ aList.get(n);
            n++;
        }
        //System.out.println("ref = '"+c.reference+"'");
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
            int j = c.reference.indexOf(";/");
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
    } catch (NumberFormatException ex) {
        throw new ReadComplexException("Error in \"Complex.fromString()\":"+nl+
                ex.toString()+nl+
                "in line: "+text+nl+
                "while reading "+nowReading.toString()+"");
    }
    return c;
  } //fromString
  public static class ReadComplexException extends Exception {
    public ReadComplexException() {super();}
    public ReadComplexException(String txt) {super(txt);}
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="toString()">
/** converts a Complex into a String, such as:
 * "<code>Fe 3+;-13.02;;;Fe 2+;1;;;e-;-1;;;;;;;0;Wateq4F /comment</code>"
 * @return
 * @see Complex#fromString(java.lang.String) fromString
 */
  @Override
  public String toString() {
    StringBuilder text = new StringBuilder();  
    
    text.append(encloseInQuotes(this.name)); text.append(";");

    if(this.name.startsWith("@")) {return text.toString();}
    if(this.constant != EMPTY) {text.append(Util.formatDbl3(this.constant).trim());}
    text.append(";");
    if(this.deltH != EMPTY) {text.append(Util.formatDbl3(this.deltH).trim());}
    text.append(";");
    if(this.deltCp != EMPTY) {text.append(Util.formatDbl3(this.deltCp).trim());}
    text.append(";");
    for(int ic =0; ic < NDIM; ic++) {
      if(this.component[ic] == null || this.component[ic].length()<=0) {text.append(";;"); continue;}
      text.append(encloseInQuotes(this.component[ic])); text.append(";");
      text.append(Util.formatDbl4(this.numcomp[ic]).trim()); text.append(";");
    }
    if(Math.abs(this.proton) >=0.001) {text.append(Util.formatDbl4(this.proton).trim());} text.append(";");
    String t;
    if(this.comment != null && this.comment.length() >0) {
        t = this.reference + " /" + this.comment;
    } else {
        t = this.reference;
    }
    text.append(encloseInQuotes(t));
    return text.toString();
  } //toString()
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

  //<editor-fold defaultstate="collapsed" desc="reactionTextWithLogK(complex, temperature)">
/** returns a simple, easy to read description of the reaction defining
 * the complex "c", including the equilibrium constant (logK) and reference code.
 * @param c a Complex object
 * @param temperature in degrees Celsius, to calculate logK
 * @return a text describing the reaction, the equilibrium constant and the reference code
 */
  public static String reactionTextWithLogK(Complex c, double temperature) {
    if(c == null) {return "";}
    StringBuilder text = new StringBuilder();
    text.append(reactionText(c));
    if(c.constant != EMPTY && !c.name.startsWith("@")) {
        double lgK = constCp(c.constant, c.deltH, c.deltCp, temperature);
        text.append(";  logK="); text.append(Util.formatDbl3(lgK));
    } else {
        if(!c.name.startsWith("@")) {text.append(";  logK= ??");}
    }
    text.append(" ");
    if(c.comment != null && c.comment.length() >0) {text.append(" ("); text.append(c.comment); text.append(")");}
    if(c.reference != null && c.reference.length() >0) {text.append(" ["); text.append(c.reference); text.append("]");}
    String t = text.toString().trim();
    text.delete(0, text.length());
    text.append(t);
    if(t.startsWith("=") && t.contains("@")) {text.deleteCharAt(0);}
    return text.toString();
  } //reactionText
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="reactionText(complex)">
/** returns a simple, easy to read description of the reaction defining
 * the complex "c".
 * @param c a Complex object
 * @return a text describing the reaction
 */
  public static String reactionText(Complex c) {
    if(c == null) {return "";}
    StringBuilder text = new StringBuilder();
    StringBuffer stoich = new StringBuffer();
    boolean first = true;
    boolean protonsPresent = false;
    double aX;
    for(int ic =0; ic < Complex.NDIM; ic++) {
        if(Util.isProton(c.component[ic])) {protonsPresent = true;}
        aX = c.numcomp[ic];
        if(aX > 0 && c.component[ic] != null && c.component[ic].length() >0) {
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
            text.append(stoich.toString());  text.append(" ");  text.append(c.component[ic]);
        }//if aX > 0
    }//for ic
    if(!protonsPresent && c.proton >= 0.001) {
        if(first) {
            stoich.delete(0, stoich.length());
        } else {
            stoich.delete(0, stoich.length());
            stoich.append(" +");
        }//first?
        if(Math.abs(c.proton-1)>=0.001) { // aX != +1
            stoich.append(Util.formatDbl3(c.proton));
        }
        text.append(stoich.toString());  text.append(" H+");
    }//proton
    text.append(" = ");
    first = true;
    for(int ic =0; ic < Complex.NDIM; ic++) {
        aX = -c.numcomp[ic];
        if(aX > 0 && c.component[ic] != null && c.component[ic].length() >0) {
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
            text.append(stoich.toString());  text.append(" ");  text.append(c.component[ic]);
        }//if aX > 0
    }//for ic
    if(!protonsPresent && c.proton < -0.001) {
        if(first) {
            stoich.delete(0, stoich.length());
            first = false;
        } else {
            stoich.delete(0, stoich.length());
            stoich.append(" +");
        }//first?
        if(Math.abs(c.proton-1)>=0.001) { // aX != +1
            stoich.append(Util.formatDbl3(c.proton));
        }
        text.append(stoich.toString());  text.append(" H+");
    }//proton
    stoich.delete(0, stoich.length());
    if(first) {stoich.append(" ");} else {stoich.append(" + ");}
    text.append(stoich); text.append(c.name);
    String t = text.toString().trim();
    text.delete(0, text.length());
    text.append(t);
    if(t.startsWith("=") && t.contains("@")) {text.deleteCharAt(0);}
    return text.toString();
  } //reactionText
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="constCp(logK,DH,DCp,t)">
  /** Extrapolates the log10 of an equilibrium constant "logK0" using the
   * the constant heat capacity approximation, or if deltaCp is not provided,
   * using the constant enthalpy (van't Hoff) equation. The temperature is
   * forced to be in the range 0 to 100 degrees Celsius. 
   * @param logK0 the log10(K) at 25 degrees Celsius.
   * @param deltaH_kJmol the enthalpy change of the reaction at 25 degrees Celsius, in kJ/mol.
   * @param deltaCp_JmolK the heat capacity change of the reaction at 25 degrees Celsius, in J/(mol K).
   * @param tC the temperature in degrees Celsius (between 0 and 350 degrees)
   * @return logK(tC) calculated using the reaction enthalpy and heat capacity.
   * It returns NaN if either of the input parameters is NaN. Returns EMPTY if
   * logK0 = EMPTY.
   * @see Complex#EMPTY EMPTY
   */
  public static double constCp(double logK0, double deltaH_kJmol, double deltaCp_JmolK, double tC) {
    if(Double.isNaN(logK0) || Double.isNaN(deltaH_kJmol)
            || Double.isNaN(deltaCp_JmolK) || Double.isNaN(tC)) {return Double.NaN;}
    if(logK0 == EMPTY || (tC>24.999 && tC<25.001)) {return logK0;}
    if(deltaH_kJmol == EMPTY) {return logK0;}
    double tK =  273.15 + Math.min(350,Math.max(0,tC));
    final double T0 = 298.15;
    final double R_LN10 = 19.1448668; // J/(mol K)
    double logK = logK0 + (deltaH_kJmol * 1000 / R_LN10) * ((1/T0) - (1/tK));
    if(deltaCp_JmolK == EMPTY) {return logK;}
    logK = logK + (deltaCp_JmolK / R_LN10) * ((T0/tK)-1+Math.log(tK/T0));
    return logK;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="isChargeBalanced(complex)">
  /** is the reaction specified in "complex" charge balanced?
   * @param complex
   * @return true if the reaction specified in "complex" is charge balanced
   */
  public static boolean isChargeBalanced(Complex complex) {
    if(complex.name.startsWith("@")) {
        //prefix "@" means remove such a component/complex if already included
        return true;
    }
    double totCharge = Util.chargeOf(complex.name);
    boolean hPresent = false;
    for(int i=0; i < NDIM; i++) {
      if(complex.component[i] == null || complex.component[i].length() <=0 || 
            Math.abs(complex.numcomp[i]) < 0.001) {continue;}
      totCharge = totCharge -
              complex.numcomp[i] * Util.chargeOf(complex.component[i]);
      if(Util.isProton(complex.component[i])) {hPresent = true;}
    } //for i
    if(!hPresent) {
        totCharge = totCharge - complex.proton;
    }
    return Math.abs(totCharge) < 0.001;
  } //isChargeBalanced(complex)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="check(complex)">
/** Check that<ul>
 * <li>if a component is given, it has non-zero stoichiometric coefficient, and
 * <i>vice-versa</i>, that if a reaction coefficient is given, the component name is
 * not empty
 * <li>the reaction is charge balanced
 * <li>that if "H+" is given as one of the component names, its reaction
 * coeffitient and "proton" agree</ul>
 * 
 * @param rr a Complex object
 * @return either an error message or "null"
 */
  public static String checkComplex(Complex rr) {
    StringBuilder sb = new StringBuilder();
    int hPresent;
    hPresent = -1;
    for(int i=0; i < NDIM; i++) {
        if((rr.component[i] == null || rr.component[i].length() <=0)
                && Math.abs(rr.numcomp[i]) >= 0.001) {
            sb.append("Complex: \"");
            sb.append(rr.name);
            sb.append("\""); sb.append(nl);
            sb.append("Reaction coefficient given with no component.");
        } else if(rr.component[i] != null && rr.component[i].length() >0) {
            if(Util.isProton(rr.component[i])) {hPresent = i;}
            if(Math.abs(rr.numcomp[i]) < 0.001) {
            sb.append("Complex: \"");
            sb.append(rr.name);
            sb.append("\""); sb.append(nl);
            sb.append("No reaction coefficient for component \"");
            sb.append(rr.component[i]); sb.append("\"");
            }
        }
        if(hPresent >-1 && (Math.abs(rr.proton) >=0.001) && Math.abs(rr.numcomp[hPresent]-rr.proton) >=0.001) {
            if(sb.length() >0) {sb.append(nl);}
            sb.append("Complex: \"");
            sb.append(rr.name);
            sb.append("\""); sb.append(nl);
            sb.append("conflict between \"H+\" coefficient and nbr of protons.");
        }
    }//for i
    //--- check for charge balance
    if(!Complex.isChargeBalanced(rr)) {
        if(sb.length() >0) {sb.append(nl);}
        sb.append("Complex: \"");
        sb.append(rr.name);
        sb.append("\""); sb.append(nl);
        sb.append("Reaction is not charge balanced.");
    }
    if(sb.length() > 0) {return sb.toString();} else {return null;}
  }
  //</editor-fold>

}
