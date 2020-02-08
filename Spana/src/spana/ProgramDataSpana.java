package spana;

import lib.kemi.chem.Chem;

/** Class to store information on the "Spana" program.
 * The class is used to retrieve data in diverse methods
 * <br>
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
public class ProgramDataSpana {
  /** true if advanced options should be shown to the user */
  public boolean advancedVersion = false;
  /** true if reversed concentration ranges are to be allowed */
  public boolean reversedConcs = false;
  /** true if the frame of SED or PREDOM is to be kept once the
   * calculations are finished */
  public boolean keepFrame = false;
  /** number of calculation steps for program SED.
   * The number of calculation points is the number of steps plus one. */
  public int SED_nbrSteps = 50;
  /** true if SED should also create a table with output data */
  public boolean SED_tableOutput = false;
  /** number of calculation steps for program PREDOM.
   * The number of calculation points is the number of steps plus one. */
  public int Predom_nbrSteps = 50;
  /** true if PRDOM should only show aqueous species in the predominance area diagram */
  public boolean aquSpeciesOnly = false;
  /** the ionic strength for the calculations */
  public double ionicStrength = 0;
  /** the temperature in degrees Celsius, used both to calculate ionic strength effects, that is,
   * to calculate activity coefficients, and to calculate values of the
   * redox potential (Eh) from  pe-values */
  public double temperature = 25;
  /** the pressure in bar, displayed in the diagram */
  public double pressure = 1;
  /** Model to calculate activity coefficents:<ul>
   * <li> &lt;0 for ideal solutions (all activity coefficients = 1)
   * <li> =0 Davies eqn.
   * <li> =1 SIT (Specific Ion interaction "Theory")
   * <li> =2 Simplified HKF (Helson, Kirkham and Flowers)
   * </ul> */
  public int actCoeffsMethod = 0;
  /** Max tolerance when solving mass-balance equations in HaltaFall. Should
   * be between 0.01 and 1e-9 */
  public double tolHalta = Chem.TOL_HALTA_DEF;
  /** the path for a file containing parameters needed for the specific
   * ion interaction model of calculation of activity coefficients (ionic
   * strength effects) */
  public String pathSIT = null;
  /** level of debug output in HaltaFall:<br>
   * 0 - none<br>
   * 1 - errors only<br>
   * 2 - errors + results<br>
   * 3 - errors + results + input<br>
   * 4 = 3 + output from Fasta<br>
   * 5 = 4 + output from activity coeffs<br>
   * 6 = 5 + lots of output.<br>
   * Default = Chem.DBGHALTA_DEF =1 (output errors only)
   * @see Chem#DBGHALTA_DEF Chem.DBGHALTA_DEF
   * @see Chem.ChemSystem.ChemConcs#dbg Chem.ChemSystem.ChemConcs.dbg */
  public int calcDbgHalta = Chem.DBGHALTA_DEF;
  /** debug output in SED/Predom */
  public boolean calcDbg = false;
  /** true if values of redox potential (Eh) should be displayed in the
   * diagrams, rather than values of pe */
  public boolean useEh = true;
  /** The file name extension for the table output from the program SED */
  public String tblExtension = "csv";
  /** Choices for <code>tblExtension</code>
   * @see ProgramDataSpana#tblExtension tblExtension */
  public String[] tblExtension_types = {"csv","dta","lst","txt","out","prn"};
  /** The character separating data for the table output from the program SED */
  public char tblFieldSeparator = ',';
  /** Choices for <code>tblFieldSeparator</code>
   * @see ProgramDataSpana#tblFieldSeparator tblFieldSeparator */
  public char[] tblFieldSeparator_types = {';',',','\u0009',' '};
  /** A string to be inserted at the beginning of comment-lines in the
   * table output from program SED */
  public String tblCommentLineStart = "\"";
  /** A string to be appended at the end of comment-lines in the
   * table output from program SED */
  public String tblCommentLineEnd = "\"";
  public boolean diagrConvertHeader = true;
  public boolean diagrConvertColors = true;
  public boolean diagrConvertPortrait = true;
  /** Font type: 0=draw; 1=Times; 2=Helvetica; 3=Courier. */
  public int diagrConvertFont = 2;
  /** the size factor (%) in the X-direction */
  public int diagrConvertSizeX = 100;
  /** the size factor (%) in the Y-direction */
  public int diagrConvertSizeY = 100;
  /** margin in cm */
  public float diagrConvertMarginB = 1;
  /** margin in cm */
  public float diagrConvertMarginL = 1;
  public boolean diagrConvertEPS = false;
  /** size in pixels */
  public int diagrExportSize = 1000;
  /** For example: bmp gif jpg png. */
  public String diagrExportType = "png";
  /** the minimum fraction value that a species must reach
   * to be displayed in a fraction diagram. A value of 0.03 means that
   * a species must have a fraction above 3% in order to be displayed
   * in a fraction diagram. */
  public float fractionThreshold = 0.03f;
  /** if <code>true</code> a neutral pH line (at pH=7 for 25°C)
   * will be drawn in Pourbaix diagrams (Eh/pH diagram) */
  public boolean drawNeutralPHinPourbaix = false;
  /**  if <code>true</code> special settings will be "on" for students at
   * the school of chemistry at the Royal Institute of Technology (KTH)
   * in Stockholm, Sweden. */
  public boolean kth = false;
  /**  if <code>true</code> then calculations are run by loading the jar-files
   * (SED and Predom) into the Java Virtual Machine;  if <code>false</code>
   * the calculations are run as a separate system process.  Used in class
   * "Select_Diagram" */
  public boolean jarClassLd = true;
  public ProgramDataSpana() {}
}
