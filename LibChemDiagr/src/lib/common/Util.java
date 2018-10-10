package lib.common;

/**  A collection of static methods used by the libraries and
 * by the software "Chemical Equilibrium Diagrams".
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
public class Util {
  private static final String nl = System.getProperty("line.separator");
  private static javax.swing.JFileChooser fc;
  private static final java.text.NumberFormat nf;
  private static final java.text.NumberFormat nfe;
  private static final java.text.NumberFormat nfi;
  private static final java.text.DecimalFormat myFormatter;
  private static final java.text.DecimalFormat myFormatterExp;
  private static final java.text.DecimalFormat myFormatterInt;
  private static final String SLASH = java.io.File.separator;
  static { // static initializer
    nf = java.text.NumberFormat.getNumberInstance(java.util.Locale.ENGLISH);
    nfe = java.text.NumberFormat.getNumberInstance(java.util.Locale.ENGLISH);
    nfi = java.text.NumberFormat.getNumberInstance(java.util.Locale.ENGLISH);
    myFormatter = (java.text.DecimalFormat)nf;
      myFormatter.setGroupingUsed(false);
      myFormatter.setDecimalSeparatorAlwaysShown(false);
      myFormatter.applyPattern("###0.###");
    myFormatterExp = (java.text.DecimalFormat)nfe;
      myFormatterExp.setGroupingUsed(false);
      myFormatterExp.setDecimalSeparatorAlwaysShown(false);
      myFormatterExp.applyPattern("0.0##E0");
    myFormatterInt = (java.text.DecimalFormat)nfi;
      myFormatterInt.setGroupingUsed(false);
      myFormatterInt.setDecimalSeparatorAlwaysShown(false);
      //                           -2147483648
      myFormatterInt.applyPattern("##########0");
  } // static initializer

  //--------------------------
  // ---  Chemical Names  ---
  //--------------------------

  //<editor-fold defaultstate="collapsed" desc="chargeOf(species)">
 /** Returns the charge of a species. For "H+" and "Na +" returns +1;
  * for "CO3-2" and "SO4 2-" it returns -2; for "X+10" and "X 10+" returns +10.
  * For "A+B " it returns zero. For "Al+++" and "CO3--" it +3 and -2.
  * For "B+-", "B+-+", "B++--" and "B-+++" it returns -1,+1,-2, and +3, respectively
  * @param speciesName
  * @return the electric charge of the species
  */
  public static int chargeOf(String speciesName) {
    int len = speciesName.length();
    if(len <= 1) {return 0;}
    IntPointer signPos = new IntPointer(-1);
    int lastCh = getLastCharExcludingChargeAndSignPosition(speciesName, signPos);
    //System.out.println("name \""+speciesName+"\", len="+len+", signPos.i = "+signPos.i+", lastCh="+lastCh);
    if(signPos.i <= 0) {return 0;}
    int iz = 1;
    // get the charge
    char sign = speciesName.charAt(signPos.i);
    int charge;
    if(sign =='+') {charge = 1;} else {charge = -1;}
    char ip1 =' ';
    if(signPos.i < (len-1)) {ip1 = speciesName.charAt(signPos.i+1);}
    //System.out.println("name \""+speciesName+"\", len="+len+", lastCh="+lastCh+", signPos.i="+signPos.i);
    if(Character.isDigit(ip1)) { // The char.following the sign is a number
      try{charge = charge * Integer.parseInt(speciesName.substring(signPos.i+1).trim());}
      catch(NumberFormatException ex) {charge = 0;}
    } else if(ip1 == ' ') { // The char.following the sign is not a number,
      //then the (+/-) sign is the last character
      if(lastCh < (signPos.i-1)) {
          try{iz = Integer.parseInt(speciesName.substring(lastCh+1, signPos.i).trim());}
          catch(NumberFormatException ex) {iz = 1;}
      }
      charge = charge * iz;
    } else if(ip1 == sign) { // For cases like "Al+++" or "PO4---"
      for(int i = signPos.i+1; i < len; i++) {
          if(speciesName.charAt(i) == sign) {iz++;}
          else {iz = 0; break;} // for cases such as "Al++-"
      }
      charge = charge * iz;
    }
    else {charge = 0;}
    return charge;
  } // chargeOf
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="nameCompare(species1, species2)">
 /** Compare two chemical names and decide if they are equal.
  * This takes into account electrical charges. Therefore, "Fe 3+" is equal
  * to "Fe+3". Note that "I2-" has charge 1-, and "I 2-" has charge 2-.
  * Also Ca(OH)2(aq) is equal to Ca(OH)2 which is different to Ca(OH)2(s).
  * In addition CO2 and CO2(g) are different.
  * @param spe1 one species
  * @param spe2 another species
  * @return true if they are equivalent */
  public static boolean nameCompare(String spe1, String spe2) {
    // make sure that strings are not null
    if(spe1 == null) {return (spe2 == null);}
    else {if(spe2 == null) {return false;}}
    //
    int z1 = chargeOf(spe1); int z2 = chargeOf(spe2);
    if(z1 != z2) {return false;}
    return nameOf(spe1).equalsIgnoreCase(nameOf(spe2));
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="nameOf(species)">
 /** Get the name without the electric charge for a soluble species.
  * Remove also "(aq)" at the end of a name (if it is there).
  * @param speciesName
  * @return the "bare" species name
  * @see #bareNameOf  bareNameOf
  * @see #chargeOf chargeOf
  */
  public static String nameOf(String speciesName) {
    int len = speciesName.length();
    if(len <= 1) {return speciesName;}
    if(len > 4) {
      if(speciesName.toUpperCase().endsWith("(AQ)")) {
                    return speciesName.substring(0, (len-4));
      }
    }
    //-- if the last symbol is a letter: then there is no charge
    if(Character.isLetter(speciesName.charAt(len-1))) {return speciesName;}
    IntPointer signPos = new IntPointer();
    int lastCh;
    lastCh = getLastCharExcludingChargeAndSignPosition(speciesName, signPos);
    //do not include white space at the end, but keep at least one char
    while(lastCh>0 && Character.isWhitespace(speciesName.charAt(lastCh))) {lastCh--;}
    return speciesName.substring(0, (lastCh+1));
  } // nameOf(species)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="bareNameOf(species)">
 /** Get the name without the electric charge for a soluble species.
  * Remove also "(aq)" at the end of a name. For a solid, liquid or gas,
  * remove phase specification such as "(s)", "(g)" etc
  * @param speciesName
  * @return the "bare" species name
  * @see #nameOf nameOf
  * @see #chargeOf chargeOf
  */
  public static String bareNameOf(String speciesName) {
    String sName = speciesName.trim();
    int len = sName.trim().length();
    if(len <= 1) {return sName;}
    boolean solid = false;
    if(len > 3 && sName.toUpperCase().endsWith("(S)")) {
        solid = true;
        sName = sName.substring(0, (len-3));
        len = sName.trim().length();
    }
    String sNameU = sName.toUpperCase();
    if(len > 3) {
      if(sNameU.endsWith("(C)") || sNameU.endsWith("(A)")
          || (!solid && (sNameU.endsWith("(L)") || sNameU.endsWith("(G)")))) {
            return sName.substring(0, (len-3));
      }
    }
    if(len > 4) {
      if(sNameU.endsWith("(CR)") || sNameU.endsWith("(AM)")
          || (!solid && sNameU.endsWith("(AQ)"))) {
            return sName.substring(0, (len-4));
      }
    }
    if(len > 5) {
      if(sNameU.endsWith("(VIT)") || sNameU.endsWith("(PPT)")) {
            return sName.substring(0, (len-5));
      }
    } //if len >5
    if(solid) {return sName;}
    //-- if the last symbol is a letter: then there is no charge
    if(Character.isLetter(sName.charAt(len-1))) {return sName;}
    IntPointer signPos = new IntPointer();
    int lastCh;
    lastCh = getLastCharExcludingChargeAndSignPosition(sName, signPos);
    //do not include white space at the end, but keep at least one char
    while(lastCh>0 && Character.isWhitespace(sName.charAt(lastCh))) {lastCh--;}
    return sName.substring(0, (lastCh+1));
  } // nameOf
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="isProton/isElectron/isSolid / etc">

 /** Check if a species name is equivalent to the "proton"
  * @param t0 a species name
  * @return true if the species is "H+" or "H +"  */
  public static boolean isProton(String t0) {
    if(t0 == null || t0.length() <=1) {return false;}
    return (t0.equals("H+") || t0.equals("H +"));
  }

 /** Check if a species name is equivalent to the "electron". Standard minus (hyphen)
  * or Unicode dash or minus sign are ok.
  * @param t0 a species name
  * @return true if the species is "e-", "e -", etc */
  public static boolean isElectron(String t0) {
    if(t0 == null || t0.length() <=1) {return false;}
    String tu = t0.toUpperCase();
    return (tu.equals("E-") || tu.equals("E -") ||
            // unicode en dash or unicode minus
            tu.equals("E\u2013") || tu.equals("E\u2212") ||
            tu.equals("E \u2013") || tu.equals("E \u2212"));
  }

 /** Check if a species name corresponds to water
  * @param t0 a species name
  * @return true if the species is "H2O" or "H2O(l)" etc  */
  public static boolean isWater(String t0) {
    if(t0 == null || t0.length() <=2) {return false;}
    return (t0.equals("H2O") || t0.toUpperCase().equals("H2O(L)"));
  }

 /** Check if a species name corresponds to a gas
  * @param t0 a species name
  * @return true if the species ends with "(g)"  */
  public static boolean isGas(String t0) {
    if(t0 == null || t0.length() <=3) {return false;}
    return t0.toUpperCase().endsWith("(G)");
  }

 /** Check if a species name corresponds to a cation
  * @param t0 a species name
  * @return true if the electric charge of the species is positive  */
  public static boolean isCation(String t0) {
    if(t0 == null || t0.length() <=1) {return false;}
    return chargeOf(t0)>0;
  }

 /** Check if a species name corresponds to an anion
  * @param t0 a species name
  * @return true if the electric charge of the species is negative  */
  public static boolean isAnion(String t0) {
    if(t0 == null || t0.length() <=1) {return false;}
    return chargeOf(t0)<0;
  }

 /** Check if a species name corresponds to a solid.
  * NOTE: it is dangerous to rely on this method. The user might rename a
  * solid such as Ca(CO)3(s) to "calcite", thus, this method may return false
  * for names that correspond to perfectly valid solids.
  * @param t0 a species name
  * @return true if the species ends with either:
  * "(s)", "(c)", "(l)", "(cr)", "(am)", , "(a)", "(vit)" or "(ppt)" */
  public static boolean isSolid(String t0) {
    if(t0 == null || t0.length() <=3) {return false;}
    String tU = t0.toUpperCase();
    if(tU.endsWith("(S)")) {return true;}  //solid
    else if(tU.endsWith("(A)")) {return true;} //amorphous
    else if(tU.endsWith("(C)")) {return true;}  //crystalline
    else if(tU.endsWith("(L)")) {return true;}  //liquid
    if(t0.length() > 4) {
      if(tU.endsWith("(CR)")) {return true;} //crystal
      else if(tU.endsWith("(AM)")) {return true;} //amorphous
    }
    if(t0.length() > 5) {
      if(tU.endsWith("(VIT)")) {return true;}//vitreous
      if(tU.endsWith("(PPT)")) {return true;}//precipitated
    }
    return false;
  }

 /** Check if a species name corresponds to a liquid
  * @param t0 a species name
  * @return true if the species ends with "(l)" */
  public static boolean isLiquid(String t0) {
    if(t0 == null || t0.length() <=3) {return false;}
    return t0.toUpperCase().endsWith("(L)");
  }

 /** Find out if a species is a neutral aqueous species:
  * <ul><li>the species is water,
  * <li>the name ends with "(aq)",
  * <li>it is neutral and it is not a solid nor a gas.</ul>
  * @param t0 name of a species
  * @return true if the species is a neutral aqueous species */
  public static boolean isNeutralAqu(String t0) {
    if(t0 == null || t0.length() <=0) {return false;}
    if(t0.length() > 4) {
      if(t0.toUpperCase().endsWith("(AQ)")) {return true;} //aqueous
    }
    if(isWater(t0)) {return true;}
    if(t0.length() > 1) {
      if(chargeOf(t0)!=0) {return false;}
    }
    return !isGas(t0) && !isSolid(t0);
  }

 /** Check if a species name corresponds to a "(cr)" or "(c)" solid.
  * @param t0 a species name
  * @return true if the species ends with either "(c)" or "(cr)" */
  public static boolean is_cr_or_c_solid(String t0) {
    if(t0 == null || t0.length() <=3) {return false;}
    String tU = t0.toUpperCase();
    if(tU.endsWith("(C)")) {return true;}  //crystalline
    if(t0.length() > 4) {
    if(tU.endsWith("(CR)")) {return true;} //crystal
    }
    return false;
  }

 /** Check if a species name corresponds to a "(cr)" solid.
  * @param t0 a species name
  * @return true if the species ends with "(cr)" */
  public static boolean is_cr_solid(String t0) {
    if(t0 == null || t0.length() <=4) {return false;}
    String tU = t0.toUpperCase();
    return tU.endsWith("(CR)");
  }

 /** Check if a species name corresponds to a "(c)" solid.
  * @param t0 a species name
  * @return true if the species ends with "(c)" */
  public static boolean is_c_solid(String t0) {
    if(t0 == null || t0.length() <=3) {return false;}
    String tU = t0.toUpperCase();
    return tU.endsWith("(C)");
  }

//</editor-fold>

  //-------------------
  // ---  Compare  ---
  //-------------------

  //<editor-fold defaultstate="collapsed" desc="areEqualDoubles(w1, w2)">
  /** Compares two doubles using "Double.compare". If they are not equal
   * then checks if they are equal within 1e-7*(min(w1,w2)).
   * 
   * @param w1
   * @param w2
   * @return true if both doubles are equal
   */
  public static boolean areEqualDoubles(double w1, double w2) {
    if(Double.compare(w1, w2) == 0) {return true;}
    // check if one is NaN or infinite and the other not
    final double CLOSE_TO_ZERO = Double.MIN_VALUE * 1e6;
    if((Double.isNaN(w1) && !Double.isNaN(w2)) ||
       (!Double.isNaN(w1) && Double.isNaN(w2))) {return false;}
    if((Double.isInfinite(w1) && !Double.isInfinite(w2)) ||
       (!Double.isInfinite(w1) && Double.isInfinite(w2))) {return false;}
    // check if one is zero and the other not:
    if((Math.abs(w1) < CLOSE_TO_ZERO && Math.abs(w2) > CLOSE_TO_ZERO) ||
          (Math.abs(w2) < CLOSE_TO_ZERO && Math.abs(w1) > CLOSE_TO_ZERO)) {return false;}
    return Math.abs(w2-w1) < Math.min(Math.abs(w1), Math.abs(w2))*1e-7;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="stringsEqual(species1, species2)">
 /** Decide if two stings are equal when "trimmed", even if they are both null.
  * @param t1 one string
  * @param t2 another string
  * @return true if they are equal */
  public static boolean stringsEqual(String t1, String t2) {
    return ((t1 !=null && t2 !=null && t1.trim().equals(t2.trim())) ||
          (t1 ==null && t2 ==null));
  }
  //</editor-fold>

  //---------------------------
  // ---  Error reporting  ---
  //---------------------------

  //<editor-fold defaultstate="collapsed" desc="stack2string(Exception e)">
 /** returns a <code>printStackTrace</code> in a String, surrounded by two dash-lines.
  * @param e Exception
  * @return printStackTrace  */
  public static String stack2string(Exception e) {
    try{
      java.io.StringWriter sw = new java.io.StringWriter();
      e.printStackTrace(new java.io.PrintWriter(sw));
      String t = sw.toString();
      if(t != null && t.length() >0) {
          int i = t.indexOf("Unknown Source");          
          int j = t.indexOf("\n");
          if(i>0 && i > j) {
              t = t.substring(0,i);
              j = t.lastIndexOf("\n");
              if(j>0) {t = t.substring(0,j)+nl;}
          }
      }
      return "- - - - - -"+nl+
             t +
             "- - - - - -";
    }
    catch(Exception e2) {
      return "Internal error in \"stack2string(Exception e)\"";
    }
  } //stack2string(ex)
  //</editor-fold>

  //-----------------------------
  // ---  Number formatting  ---
  //-----------------------------

  //<editor-fold defaultstate="collapsed" desc="formatNum(double)">
/** Returns a String containing a representation of a double floating
 * point value, using a point as a decimal separator. Inserts a space
 * at the beginning if the string does not start with "-". If the argument is
 * NaN (not a number) the value of zero is used.
 * @param num the variable (double) to format
 * @return the text String representing "num".
 * At least one digit is used to represent the fractional part,
 * and beyond that as many, but only as many, more digits are used as are needed
 * to uniquely distinguish the argument value from adjacent values  */
  public static String formatNum(double num) {
    if(Double.isNaN(num)) {return "NaN";}
    if(Double.isInfinite(num)) {return "infinite";}
    float f = (float)Math.max((double)(-Float.MAX_VALUE), Math.min((double)Float.MAX_VALUE,num));
    if(f == -0.) {f=0;}
    java.io.StringWriter sw = new java.io.StringWriter();
    java.io.PrintWriter pw = new java.io.PrintWriter(sw);
    pw.print(f);
    StringBuffer sb = sw.getBuffer();
    if(sb.charAt(0) != '-') {sb.insert(0, " ");}
    return sb.toString();
  } // formatNum(num)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="formatNumAsInt(double)">
/** Returns a String containing a representation of a double floating
 * point value, using a point as a decimal separator.
 * The number is written as an integer if possible. For example,
 * 1.0 will be "1" and -22.0 will be "-22", but 1.1 will be returned as "1.1".
 * If the argument is NaN (not a number) the value of zero is used.
 * @param num the variable (double) to format
 * @return the text String representing "num".
 * The number is written as an integer if possible. For example,
 * 1.0 will be "1" and -22.0 will be "-22", but 1.1 will be returned as "1.1".
 * If the fractional part is needed, at least one digit is used
 * and beyond that as many, but only as many, more digits are used as are needed
 * to uniquely distinguish the argument value from adjacent values
 * @see #formatDbl formatDbl */
  public static String formatNumAsInt(double num) {
    String textNum = formatNum(num);
    int p = textNum.indexOf(".");
    if(p > -1 && textNum.endsWith("0")) { //ends with "0" and it contains a "."
        int i = textNum.length()-1;
        while (true) {
                if(textNum.charAt(i) != '0') {break;}
                i--;
        } //while
        textNum = textNum.substring(0, (i+1));
    }
    if(textNum.endsWith(".")) {return textNum.substring(0, textNum.length()-1);}
    return textNum;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="formatDbl, formatInt">

 /** Returns a String containing a representation of a double floating
  * point value, using a point as a decimal separator. Inserts a space
  * at the beginning if the string does not start with "-"
  * If the argument is NaN (not a number) or infinite the value of zero is used.
  * @param num the variable (double) to format
  * @return the text String representing "num".
  * The patterns "###0.###" or "0.0##E0", will be used resulting
  * in a maximum of four (3) digits in the fractional part.  */
  public static String formatDbl3(double num) {
    if(Double.isNaN(num)) {return "NaN";}
    if(Double.isInfinite(num)) {return "infinite";}
    String textNum;
    if(num == -0) {num = 0;}
    if((Math.abs(num) < 0.01 && num != 0) || Math.abs(num) > 9999) {
        myFormatterExp.applyPattern("0.0###E0");
        textNum = myFormatterExp.format(num);
    } else {
        myFormatter.applyPattern("####0.###");
        textNum = myFormatter.format(num);
    }
    if(textNum.startsWith("-")) {return textNum.trim();}
    else{return " "+textNum.trim();}
  } // formatDbl(num)

  /** Returns a String containing a representation of a double floating
  * point value, using a point as a decimal separator. Inserts a space
  * at the beginning if the string does not start with "-"
  * If the argument is NaN (not a number) or infinite the value of zero is used.
  * @param num the variable (double) to format
  * @return the text String representing "num".
  * The patterns "###0.####" or "0.0###E0", will be used resulting
  * in a maximum of four (4) digits in the fractional part.  */
  public static String formatDbl4(double num) {
    if(Double.isNaN(num)) {return "NaN";}
    if(Double.isInfinite(num)) {return "infinite";}
    String textNum;
    if(num == -0) {num = 0;}
    if((Math.abs(num) < 0.001 && num != 0) || Math.abs(num) > 1000) {
        myFormatterExp.applyPattern("0.0###E0");
        textNum = myFormatterExp.format(num);
    } else {
        myFormatter.applyPattern("###0.####");
        textNum = myFormatter.format(num);
    }
    if(textNum.startsWith("-")) {return textNum.trim();}
    else{return " "+textNum.trim();}
  } // formatDbl(num)

  /** Returns a String containing a representation of a double floating
  * point value, using a point as a decimal separator. Inserts a space
  * at the beginning if the string does not start with "-"
  * If the argument is NaN (not a number) or infinite the value of zero is used.
  * @param num the variable (double) to format
  * @return the text String representing "num".
  * The patterns "###0.######" or "0.0#####E0", will be used resulting
  * in a maximum of six (6) digits in the fractional part.  */
  public static String formatDbl6(double num) {
    if(Double.isNaN(num)) {return "NaN";}
    if(Double.isInfinite(num)) {return "infinite";}
    String textNum;
    if(num == -0) {num = 0;}
    if((Math.abs(num) < 0.001 && num != 0) || Math.abs(num) > 1000) {
        myFormatterExp.applyPattern("0.0#####E0");
        textNum = myFormatterExp.format(num);
    } else {
        myFormatter.applyPattern("###0.######");
        textNum = myFormatter.format(num);
    }
    if(textNum.startsWith("-")) {return textNum.trim();}
    else{return " "+textNum.trim();}
  } // formatDbl(num)

 /** Returns a String containing a representation of an integer.
  * Inserts a space at the beginning if the string does not start with "-"
  * @param num the variable (int) to format
  * @return the text String representing "num"  */
  public static String formatInt(int num) {
    if(num == -0) {num = 0;}
    String textNum = myFormatterInt.format(num);
    if(textNum.startsWith("-")) {return textNum.trim();}
    else{ return " "+textNum.trim();}
  } // formatInt(num)

  //</editor-fold>

  //-------------------
  // ---  Diverse  ---
  //-------------------

 /**  Throw a NullPointerException: <code>thowErr(null);</code>
  * @param n must be "<code>null</code>" to throw a NullPointerException */
  public static void throwErr(Integer n) {int a = n*2;}

  //<editor-fold defaultstate="collapsed" desc="configureOptionPane()">

 /** <b>Left/Right arrow keys support for JOptionpane:</b><p>
  * Call this in the "main" method and <i>everytime after selecting a look-and-feel</i>
  * to navigate JOptionPanes with the left and right arrow keys (in addtion to TAB and Shift-TAB).
  * <p>
  * Also use "UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE)" to make
  * the ENTER key follow any JButton with the focus. */
  public static void configureOptionPane() {
    if(javax.swing.UIManager.getLookAndFeelDefaults().get("OptionPane.actionMap") == null) {
        javax.swing.UIManager.put("OptionPane.windowBindings", new 
        Object[] {
                    "ESCAPE", "close",
                    "LEFT", "left",
                    "KP_LEFT", "left",
                    "RIGHT", "right",
                    "KP_RIGHT", "right"
                });
        javax.swing.ActionMap map = new javax.swing.plaf.ActionMapUIResource();
        map.put("close", new OptionPaneCloseAction());
        map.put("left", new OptionPaneArrowAction(false));
        map.put("right", new OptionPaneArrowAction(true));
        javax.swing.UIManager.getLookAndFeelDefaults().put("OptionPane.actionMap", map);
    }
  }
  //<editor-fold defaultstate="collapsed" desc="private">
    private static class OptionPaneCloseAction extends javax.swing.AbstractAction {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            javax.swing.JOptionPane optionPane = (javax.swing.JOptionPane) e.getSource();
            optionPane.setValue(javax.swing.JOptionPane.CLOSED_OPTION);
        }
    }

    private static class OptionPaneArrowAction extends javax.swing.AbstractAction {
        private boolean myMoveRight;
        OptionPaneArrowAction(boolean moveRight) {myMoveRight = moveRight;}
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            javax.swing.JOptionPane optionPane = (javax.swing.JOptionPane) e.getSource();
            java.awt.EventQueue eq = java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue();

            eq.postEvent(new java.awt.event.KeyEvent(
                    optionPane,
                    java.awt.event.KeyEvent.KEY_PRESSED,
                    e.getWhen(),
                    (myMoveRight) ? 0 : java.awt.event.InputEvent.SHIFT_DOWN_MASK,
                    java.awt.event.KeyEvent.VK_TAB,
                    java.awt.event.KeyEvent.CHAR_UNDEFINED,
                    java.awt.event.KeyEvent.KEY_LOCATION_UNKNOWN
            ));
        }
    }
  // </editor-fold>

  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="getFileName()">

  //<editor-fold defaultstate="collapsed" desc="getSaveFileName">
 /** Get a file name from the user using an Save File dialog.
  * @param parent used for error reporting  (may be null)
  * @param progr name of calling program; used for error reporting (may be null or "")
  * @param title caption for the Open File dialog (may be null or "")
  * @param type in the range 0 to 11:<pre>
  *  0 all files
  *  1 (*.exe, *.jar)
  *  2 Data bases (*.db, *.txt, *.skv, *.csv)
  *  3 Text databases (*.txt, *.skv, *.csv)
  *  4 Binary databases (*.db)
  *  5 Data files (*.dat)
  *  6 Plot files (*.plt)
  *  7 Text files (*.txt)
  *  8 Acrobat files (*.pdf)
  *  9 PostScript (*.ps)
  * 10 encapsulated PostScript (*.eps)
  * 11 ini-files (*.ini)</pre>
  * @param defName a default file name for the Open File dialog. With or without a path. May be null.
  * @param path a default path for the Open File dialog (may be null)).
  * A path in <code>defName</code> takes preference.
  * @return "null" if the user cancels the operation; " " if a programming error is found;
  * a file name otherwise */
  public static String getSaveFileName(java.awt.Component parent,
          String progr,
          String title,
          int type,
          String defName,
          String path) {
    // Ask the user for a file name using a Open File dialog
    boolean mustExist = false;
    boolean openF = false;
    boolean filesOnly = true;
    return getFileName(parent,progr,openF,mustExist,filesOnly,title,type,defName,path);
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="getOpenFileName">
 /** Get a file name from the user using an Open File dialog.
  * @param parent used for error reporting  (may be null)
  * @param progr name of calling program; used for error reporting (may be null or "")
  * @param mustExist <code>true</code> if file must exist
  * @param title caption for the Open File dialog (may be null or "")
  * @param type in the range 0 to 11:<pre>
  *  0 all files
  *  1 (*.exe, *.jar)
  *  2 Data bases (*.db, *.txt, *.skv, *.csv)
  *  3 Text databases (*.txt, *.skv, *.csv)
  *  4 Binary databases (*.db)
  *  5 Data files (*.dat)
  *  6 Plot files (*.plt)
  *  7 Text files (*.txt)
  *  8 Acrobat files (*.pdf)
  *  9 PostScript (*.ps)
  * 10 encapsulated PostScript (*.eps)
  * 11 ini-files (*.ini)</pre>
  * @param defName a default file name for the Open File dialog. With or without a path. May be null.
  * @param path a default path for the Open File dialog (may be null)).  A path in
  * <code>defName</code> takes preference.
  * @return "null" if the user cancels the operation; " " if a programming error is found;
  * a file name otherwise */
  public static String getOpenFileName(java.awt.Component parent,
          String progr,
          boolean mustExist,
          String title,
          int type,
          String defName,
          String path) {
    // Ask the user for a file name using a Open File dialog
    boolean openF = true;
    boolean filesOnly = true;
    return getFileName(parent,progr,openF,mustExist,filesOnly,title,type,defName,path);
  }
  // </editor-fold>

 /** A general purpose procedure to get a file name from the user using
  * an Open/Save File Dialog.
  * @param parent the parent component of the dialog
  * @param progr name of calling program; used for error reporting (may be null or "")
  * @param openF show an open (true) or a save (false) dialog?
  * @param mustExist <code>true</code> if file must exist
  * @param filesOnly <code>true</code> if only files may be selected.
  *     <code>false</code> if both files and directories may be selected.
  * @param title caption for the Open File dialog (may be null or "")
  * @param type in the range 0 to 11:<pre>
  *  0 all files
  *  1 (*.exe, *.jar)
  *  2 Data bases (*.db, *.txt, *.skv, *.csv)
  *  3 Text databases (*.txt, *.skv, *.csv)
  *  4 Binary databases (*.db)
  *  5 Data files (*.dat)
  *  6 Plot files (*.plt)
  *  7 Text files (*.txt)
  *  8 Acrobat files (*.pdf)
  *  9 PostScript (*.ps)
  * 10 encapsulated PostScript (*.eps)
  * 11 ini-files (*.ini)</pre>
  * @param defName a default file name for the Open File dialog. With or without a path. May be null.
  * @param path a default path for the Open File dialog (may be null)).
  * A path in <code>defName</code> takes preference.
  * @return "null" if the user cancels the operation; " " if a programming error is found;
  * a file name otherwise
  * @see #getOpenFileName getOpenFileName
  * @see #getSaveFileName getSaveFileName */
  public static String getFileName(java.awt.Component parent,
          String progr,
          boolean openF,
          boolean mustExist,
          boolean filesOnly,
          String title,
          int type,
          String defName,
          String path) {
    int returnVal;
    // Ask the user for a file name using a Open File dialog
    parent.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
    if(path != null && path.length() >0) {fc = new javax.swing.JFileChooser(path);}
    else {fc = new javax.swing.JFileChooser(".");}
    fc.setMultiSelectionEnabled(false);
    java.io.File currDir = null;
    if(path != null && path.trim().length()>0) {currDir = new java.io.File(path);}
    if(currDir == null || !currDir.exists()) {currDir = new java.io.File(".");}
    fc.setCurrentDirectory(currDir);
    if(title != null && title.trim().length()>0) {fc.setDialogTitle(title);}
    else {fc.setDialogTitle("Enter a file name");}
    if(filesOnly) {
        fc.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
    } else {
        fc.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);
    }
    fc.setAcceptAllFileFilterUsed(true);

    java.io.File defFile;
    if(defName != null && defName.trim().length() >0) {
        defFile = new java.io.File(defName);
         //this may change the current directory of the dialog
        fc.setSelectedFile(defFile);
    }

    javax.swing.filechooser.FileNameExtensionFilter filter1 = null;
    javax.swing.filechooser.FileNameExtensionFilter filter2 = null;
    javax.swing.filechooser.FileNameExtensionFilter filter3 = null;
    //javax.swing.filechooser.FileFilter filter = null;
    //javax.swing.filechooser.FileFilter filter2 = null;
    //javax.swing.filechooser.FileFilter filter3 = null;
    String osName = System.getProperty("os.name");
    String[] ext = null;
    if(type ==1) {
        if(osName.startsWith("Mac OS")) {
            ext = new String[] {"APP", "JAR"};
            filter1 = new javax.swing.filechooser.FileNameExtensionFilter("Programs (*.app, *.jar)", ext);
            //filter = new ExtensionFileFilter("Programs (*.app, *.jar)", ext);
        } else if(osName.startsWith("Windows")) {
            ext = new String[] {"EXE", "JAR"};
            filter1 = new javax.swing.filechooser.FileNameExtensionFilter("Programs (*.exe, *.jar)", ext);
        } else {
            ext = new String[] {"JAR"};
            filter1 = new javax.swing.filechooser.FileNameExtensionFilter("Programs (*.jar)", ext);
        }
    } else if(type ==2) {
        ext = new String[] {"DB", "TXT", "SKV", "CSV"};
        filter1 = new javax.swing.filechooser.FileNameExtensionFilter("Data bases (*.db, *.txt, *.skv, *.csv)", ext);
        filter2 = new javax.swing.filechooser.FileNameExtensionFilter("Text files (*.txt, *.skv, *.csv)", new String[] {"TXT", "SKV", "CSV"});
        filter3 = new javax.swing.filechooser.FileNameExtensionFilter("Binary files (*.db)", new String[] {"DB"});
    } else if(type ==3) {
        ext = new String[] {"TXT", "SKV", "CSV"};
        filter1 = new javax.swing.filechooser.FileNameExtensionFilter("Text databases (*.txt, *.skv, *.csv)", ext);
    } else if(type ==4) {
        ext = new String[] {"DB"};
        filter1 = new javax.swing.filechooser.FileNameExtensionFilter("Binary databases (*.db)", ext);
    } else if(type ==5) {
        ext = new String[] {"DAT"};
        filter1 = new javax.swing.filechooser.FileNameExtensionFilter("*.dat", ext);
    } else if(type ==6) {
        ext = new String[] {"PLT"};
        filter1 = new javax.swing.filechooser.FileNameExtensionFilter("*.plt", ext);
    } else if(type ==7) {
        ext = new String[] {"TXT"};
        filter1 = new javax.swing.filechooser.FileNameExtensionFilter("*.txt", ext);
    } else if(type ==8) {
        ext = new String[] {"PDF"};
        filter1 = new javax.swing.filechooser.FileNameExtensionFilter("*.pdf", ext);
    } else if(type ==9) {
        ext = new String[] {"PS"};
        filter1 = new javax.swing.filechooser.FileNameExtensionFilter("*.ps", ext);
    } else if(type ==10) {
        ext = new String[] {"EPS"};
        filter1 = new javax.swing.filechooser.FileNameExtensionFilter("*.eps", ext);
    } else if(type ==11) {
        ext = new String[] {"INI"};
        filter1 = new javax.swing.filechooser.FileNameExtensionFilter("*.ini", ext);
    }

    // show the Open File dialog using the System look and feel
    javax.swing.LookAndFeel oldLaF = javax.swing.UIManager.getLookAndFeel();
    boolean resetLaF = false;
    if(!oldLaF.getClass().getName().equals(javax.swing.UIManager.getSystemLookAndFeelClassName())) {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            System.out.println("--- setLookAndFeel(System);");
            fc.updateUI();
            resetLaF = true;
        }
        catch (Exception ex) {System.out.println("--- setLookAndFeel: "+ex.getMessage());}
    }
    parent.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

    if(filter3 != null) {fc.addChoosableFileFilter(filter3);}
    if(filter2 != null) {fc.addChoosableFileFilter(filter2);}
    if(filter1 != null) {fc.setFileFilter(filter1);}
    if(openF) {
        returnVal = fc.showOpenDialog(parent);
    } else {
        returnVal = fc.showSaveDialog(parent);
    }
    // reset the look and feel
    if(resetLaF) {
        try {
            javax.swing.UIManager.setLookAndFeel(oldLaF);
            System.out.println("--- setLookAndFeel(oldLookAndFeel);");
        }
        catch (javax.swing.UnsupportedLookAndFeelException ex) {System.out.println("--- setLookAndFeel: UnsupportedLookAndFeelException!");}
        //javax.swing.UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
        System.out.println("--- configureOptionPane();");
        configureOptionPane();
    }
    if(returnVal != javax.swing.JFileChooser.APPROVE_OPTION) {return null;}
    java.io.File userFile = fc.getSelectedFile();
    if (userFile == null) {
        System.err.println("--- Error: selected file is \"null\"?");
        return null;
    }

    if(osName.startsWith("Mac OS") && userFile.getName().toLowerCase().endsWith(".app")) {
        String n = userFile.getName();
        n = n.substring(0, n.length()-4)+".jar";
        java.io.File f = new java.io.File(userFile.getAbsolutePath()+SLASH+"Contents"+SLASH+"Resources"+SLASH+"Java"+SLASH+n);
        if(f.exists()) {userFile = f;}
    }

    // -- make sure we got a correct extension
    boolean issueWarning = false;
    if(ext != null && ext.length >0) { // did the user give an extension?
        String userFNU = userFile.getName().toUpperCase();
        boolean extOk = false;
        String xt = ext[0];
        if(xt !=null && xt.length()>0 && userFNU.endsWith("."+xt)) {extOk=true;}
        if(!extOk && ext.length > 1) {
            for(int i=1; i < ext.length; i++) {
                xt = ext[i];
                if(xt !=null && xt.length()>0 && userFNU.endsWith("."+xt)) {extOk=true; break;}
            } //for
        }
        boolean dot = userFNU.contains(".");
        if(type ==1 && !osName.startsWith("Windows")) { // for Mac OS or Unix/Linux
                if(!dot) {extOk=true;}
        }
        if(!extOk) { // the user gave none of the allowed extentions
            if(userFile.getName().contains(".")) {issueWarning = true;}
            // find out if a file with any of the allowed extensions exist
            java.io.File f;
            for(String x : ext) {
                f = new java.io.File(userFile.getAbsolutePath()+"."+x.toLowerCase());
                if(f.exists()) {userFile = f;  extOk = true;  issueWarning = false;  break;}
            } //for
            if(!extOk) { // assume first extension
                userFile = new java.io.File(userFile.getAbsolutePath()+"."+ext[0].toLowerCase());
                if(openF && mustExist && userFile.exists()) {issueWarning = false;}
            } //if !extOk
        } //if !extOk
        // at this point: if "save file" an extension if given
    } //if ext[]
    if(progr == null || progr.trim().length()<=0) {progr = "(Enter a file name)";}
    if(issueWarning) {
        Object[] opt = {"OK", "Cancel"};
        int answer = javax.swing.JOptionPane.showOptionDialog(parent,
                        "Note: file name must end with \""+ext[0]+"\""+nl+nl+
                        "The file name is changed to:"+nl+"\""+userFile.getName()+"\"",
                        progr, javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE,null, opt, opt[1]);
        if(answer != javax.swing.JOptionPane.YES_OPTION) {return null;}
    }
    if(!openF) { //-- save file
        if(userFile.exists()) { //save: overwrite?
            if(!userFile.canWrite() || !userFile.setWritable(true)) {
                javax.swing.JOptionPane.showMessageDialog(parent,
                        "Can not overwrite file \""+userFile.getName()+"\"."+nl+
                        "The file or the directory is perhpas write-protected."+nl+
                        "Try again with another name.",
                        progr, javax.swing.JOptionPane.ERROR_MESSAGE);
                return null;
            }
            Object[] opt = {"Yes", "No"};
            int answer = javax.swing.JOptionPane.showOptionDialog(parent,
                        "File \""+userFile.getName()+"\" already exists,"+nl+
                        "Overwrite?",
                        progr, javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE,null, opt, opt[1]);
            if(answer != javax.swing.JOptionPane.YES_OPTION) {return null;}
        } //exists?
    } else { //-- open file
        if(mustExist && !userFile.exists()) {
            javax.swing.JOptionPane.showMessageDialog(parent,
                        "File \""+userFile.getName()+"\""+nl+
                        "does Not exist.",
                        progr, javax.swing.JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if(userFile.exists() && !userFile.canRead()) {
            javax.swing.JOptionPane.showMessageDialog(parent,
                        "Can not read file \""+userFile.getName()+"\"."+nl+
                        "The file or the directory is perhpas read-protected.",
                        progr, javax.swing.JOptionPane.ERROR_MESSAGE);
            return null;
        }
    } //save/open?
    // make sure the extension is lower case
    String s = userFile.getAbsolutePath();
    s = s.substring(0, s.length()-3)+s.substring(s.length()-3).toLowerCase();
    return s;
  } // getDataFileName()
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="isKeyPressedOK(evt)">
 /** For a JTextArea (or a JTextField) that is used to display information,
  * some KeyPressed key events,
  * such as "delete" or "enter" must be consumed. Other events, such as "page up"
  * are acceptable. This procedure returns <code>false</code> if the event is
  * not acceptable in KeyPressed events, and it should be consumed.
  * @param evt
  * @return true if the key event is acceptable, false if it should be consumed */
  public static boolean isKeyPressedOK(java.awt.event.KeyEvent evt) {
    //int ctrl = java.awt.event.InputEvent.CTRL_DOWN_MASK;
    if(evt.isControlDown() && 
            //(evt.getModifiersEx() & ctrl) == ctrl) &&
            (evt.getKeyCode() == java.awt.event.KeyEvent.VK_V
            || evt.getKeyCode() == java.awt.event.KeyEvent.VK_X
            || evt.getKeyCode() == java.awt.event.KeyEvent.VK_H)) {return false;}
    else if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {return false;}
    else if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_TAB) {return false;}
    else if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_DELETE) {return false;}
    else if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_BACK_SPACE) {return false;}
    return true;
  } //isKeyPressedOK(KeyEvent)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="rTrim">
 /** Remove trailing white space. If the argument is null, the return value is null as well.
  * @param text input String.
  * @return text without trailing white space. */
  public static String rTrim(String text) {
    if(text == null) {return text;}
    //another possibility: ("a" + text).trim().substring(1)
    int idx = text.length()-1;
    if (idx >= 0) {
        //while (idx>=0 && text.charAt(idx) == ' ') {idx--;}
        while (idx>=0 && Character.isWhitespace(text.charAt(idx))) {idx--;}
        if (idx < 0) {return "";}
        else {return text.substring(0,idx+1);}
    }
    else {  //if length =0
        return text;
    }
  } // rTrim
  //</editor-fold>

//<editor-fold defaultstate="collapsed" desc="private">
//<editor-fold defaultstate="collapsed" desc="getLastCharExcludingChargeAndSignPosition(species)">
/** Finds the position of the last character excluding the electric charge
 * (the first character is at position zero) and the position of the (+/-) sign.
 * For "X+10", "Y+3" and "F- " it returns 0 and sets signPos.i=1; while for "X 10+",
 * "Y 3+" and "F - " it returns 1 and sets signPos.i to 4, 3 and 2, respectively.
 * For "A+B " it returns 2 and sets signPos.i = -1. For "Al+++" it returns 1 and
 * sets signPos.i = 2 (position of first +/- sign). For "B+-", "B+-+", "B++--" and
 * "B-+++" it returns 1,2,2,1, respectively, and sets signPos.i to 2,3,3,2, respectively
 * (position of first +/- sign equal to the last sign).
 * @param speciesName
 * @param signPos a pointer, set by this method, to the position of the (+/-) sign;
 * "-1" if there is no (+/-) sign that corresponds to an electric charge in
 * speciesName.
 * @return the position of the last character excluding the electric charge
 * (the first character is at position zero).
 */
private static int getLastCharExcludingChargeAndSignPosition(
        String speciesName, IntPointer signPos) {
  int len = rTrim(speciesName).length();
  //--- is the last character is not a +/- or a number: then finish
  char c = speciesName.charAt(len-1);
  //                         unicode en dash or unicode minus
  if((c != '+' && c != '-' && c !='\u2013' && c !='\u2212') &&
     (c < '0' | c >'9')) {
             signPos.i = -1; return len-1;
  }
  //--- find out if there is a charge;  get the last +/-
  char sign = ' ';
  signPos.i = -1;
  for(int ik = len-1; ik >= 0; ik--) {
    sign = speciesName.charAt(ik);
    //                                unicode en dash or unicode minus
    if(sign == '+' || sign == '-' || sign =='\u2013' || sign =='\u2212')
            {signPos.i = ik; break;}
  }
  //--- if there is no (+/-) in the name, return
  if(sign != '+' && sign != '-' && sign !='\u2013' && sign !='\u2212') {signPos.i = -1; return len-1;}
  //--- if the (+/-) is not located near the end of the name it is not a sign
  if(signPos.i < (len-3) ||   //for W14O41+10 the position is at (len-3)
          signPos.i <= 0) {   //if the name starts with + or -.
      signPos.i = -1; return len-1;
  }
  //--- get lastCh = position of the name's last character (excluding last sign)
  int lastCh = signPos.i - 1;
  char ip1 =' '; // char at position signPos+1
  if(signPos.i < (len-1)) {ip1 = speciesName.charAt(signPos.i+1);}
  if(ip1 == ' ') {
      //The (+/-) sign is the last character (i1 == ' ')
      //   then lastCh must be smaller...
      //   look at the character before the sign
      //   for cases like "Fe 3+" and "SO4 2-" or "Al+++":
      c = speciesName.charAt(lastCh); //lastCh =signPos-1
      if(Character.isDigit(c)) { // There is a number before the sign: as in "CO3 2-" or "Fe(OH)2+"
          //is there a space before the charge?
          char im2; //the char at signPos-2
          if(signPos.i >=3) { //for example: "U 3+",  but Not "I3-"
            im2 =speciesName.charAt(signPos.i-2);
            if(im2 == ' ') {//it is a space like "CO3 2-"
                        lastCh = signPos.i - 3;
            } else {//im2 is not a space, such as "HS2-"
                    //for cases like "X 10+"
                if(signPos.i >=4
                   && Character.isDigit(im2)
                   && speciesName.charAt(signPos.i-3) ==' ') {lastCh = signPos.i -4;}
            }
          } //if(signPos >=3)
      } //if isDigit(signPos-1)
      else if(c == sign) { // There is another sign before the sign: like Al+++ or CO3--
          for(int ik = lastCh; ik >= 0; ik--) {
              if(speciesName.charAt(ik) != sign) {signPos.i = ik+1; return ik;}
          }
      } // else: The character before the sign is not a digit, and it is not the same (+/-)
      //         do noting, lastCh = signPos.i - 1;
  } //if signPos is the last char
  //The (+/-) sign is not the last char:
       //   chech that the (+/-) sign is really an electric charge:
       //   look at the following chars
  else if(Character.isDigit(ip1)) {//The character following the (+/-) sign is a digit
    // such as "Ca+2" or for "X+10"
    if(signPos.i != (len-2) // exclude "X+1b"
       && !(signPos.i == (len-3) && Character.isDigit(speciesName.charAt(len-1)))) {lastCh = len-1;}
  } //if !isDigit(signPos+1)
  else {lastCh = len-1;} //the (+/-) sign is not the last, and the following char is not a digit:
  return lastCh;
} //getLastCharExcludingChargeAndSignPosition(speciesName,i)
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="class IntPointer">
/** a class to point to an integer */
private static class IntPointer {
  public int i;
  public IntPointer() {}
  public IntPointer(int j) {
    i = j;
  }
} // class IntPointer
//</editor-fold>
//</editor-fold>

}
