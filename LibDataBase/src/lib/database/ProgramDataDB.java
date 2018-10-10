package lib.database;

import lib.huvud.RedirectedFrame;

/** Class to store information on the "DataBase" program.
 * The class is used to retrieve data in diverse methods.
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
public class ProgramDataDB {    
  /** the temperature (Celsius), used both to extrapolate equilibrium constants,
   * to calculate ionic strength effects (that is, to calculate activity
   * coefficients), and to calculate values of the redox potential, Eh,
   * from the pe-values */
  public double temperature_C = 25;
  /** the pressure (bar), used to calculate values of the
   * equilibrium constants */
  public double pressure_bar = 25;
  /** if <code>true</code> then it will be possible to choose temperatures
   * up to 350 C in the program Database. */
  public boolean temperatureAllowHigher = false;
  public boolean redoxAsk = false;
  public boolean redoxN = true;
  public boolean redoxS = true;
  public boolean redoxP = true;
  /** if <code>true</code> then it will be possible to choose which solid phases
   * are selected in the program Database. */
  public boolean allSolidsAsk = false;
  /** 0=include all solids; 1=exclude (cr); 2=exclude (c); 3=exclude (cr)&(c) */
  public int allSolids = 0;
  /** include water (H2O) as a component in the output data file?
   * @see ProgramDataDB#foundH2O foundH2O
   * @see ProgramDataDB#elemComp elemComp   */
  public boolean includeH2O = false;
  /** the program to make diagrams ("Spana") including directory */
  public String diagramProgr;
  /** the path where new data files will be stored */
  public StringBuffer pathAddData = new StringBuffer();
  /** the default path where database files will searched */
  public StringBuffer pathDatabaseFiles = new StringBuffer();
  public java.awt.Point addDataLocation = new java.awt.Point(-1000,-1000);
  /** the databases that will be searched */
  public java.util.ArrayList<String> dataBasesList = new java.util.ArrayList<String>();
  /** array list of String[3] objects<br>
   * [0] contains the element name (e.g. "C"),<br>
   * [1] the component formula ("CN-" or "Fe+2"),<br>
   * [2] the component name ("cyanide" or null), which is not really needed,
   * but used to help the user */
  public java.util.ArrayList<String[]> elemComp = new java.util.ArrayList<String[]>();
  /** is water (H2O) found in elemComp? This is set when reading the element files.
   * @see ProgramDataDB#includeH2O includeH2O
   * @see ProgramDataDB#elemComp elemComp   */
  public boolean foundH2O = false;

  /** an instance of the class References
   * @see References References */
  public References references = null;

  /** a redirected frame for output and error messages */
  public RedirectedFrame msgFrame = null;

  public ProgramDataDB() {}
}
