package lib.kemi.readWriteDataFiles;

import lib.common.Util;
import lib.kemi.chem.Chem;
import lib.kemi.readDataLib.ReadDataLib;

/** Read an input data file accessed through an instance of class ReadDataLib.
 * The data is read with a minimum of checks: if the format is correct no error
 * will occur. The data is stored in an instance of class Chem.
 * <p>This is the minimum amount of work needed to be able to write the data
 * again to another data file. Checks of data suitability are made within the
 * programs SED or Predom.
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
public class ReadChemSyst {
private static java.util.Locale engl = java.util.Locale.ENGLISH;
/** New-line character(s) to substitute "\n". */
private static String nl = System.getProperty("line.separator");
private static final String LINE = "-------------------------------------";

//<editor-fold defaultstate="collapsed" desc="readChemSystAndPlotInfo">
/** Read an input data file accessed through an instance of class ReadDataLib.
 * The data is read with a minimum of checks: if the format is correct no error
 * will occur.
 * <p>This is the minimum amount of work needed to be able to write the data
 * again to another data file. Checks of data suitability are made within the
 * programs SED or predom.
 * @param rd an instance of class ReadDataLib to read the input file
 * @param dbg boolean true if debug information is to be printed
 * @param warn = true if errors while reading the plot or
 * the concentrations parts of the input file should be
 * reported as a warning instead of as an error. Useful when reading
 * files from within Spana/Medusa as that program can recover from such errors.
 * For example, files created by DataBase/Hydra do not contain plot or
 * concentration data. But they can be used in Spana/Medusa.
 * @param out where messages will be printed.
 *    If null, <code>System.out</code> will be used.
 * @return a reference to an instance of inner class Chem.ChemSystem
 * with the data read. The reference is null if an error occurs
 * while reading the file.
 * @throws lib.kemi.readWriteDataFiles.ReadChemSyst.DataLimitsException
 * @throws lib.kemi.readWriteDataFiles.ReadChemSyst.ReadDataFileException
 * @throws lib.kemi.readWriteDataFiles.ReadChemSyst.PlotDataException
 * @throws lib.kemi.readWriteDataFiles.ReadChemSyst.ConcDataException
 */
public static Chem readChemSystAndPlotInfo (ReadDataLib rd,
                            boolean dbg, boolean warn,
                            java.io.PrintStream out)
            throws DataLimitsException, ReadDataFileException,
                    PlotDataException, ConcDataException {
    if(out == null) {out = System.out;}
    if(dbg) {out.println("Reading the chemical system...");}
    // ------------------------
    //   READ CHEMICAL SYSTEM
    // ------------------------
    int na;   // number of chemical components given in input file
    int nx;   // soluble complexes given in input file
    int nrSol; // number of solid products given in input file
    int solidC; // solid components given in input file
    int ms;   // total number of species (components (soluble + solid)
              //    + soluble complexes + solid reaction products)
    int mSol; // number of solids (components + reaction products)
    int mg;   // total number of "soluble" species
    rd.nowReading = "Nbr of components (Na)";
    try{na = rd.readI();}
    catch(ReadDataLib.DataReadException ex) {
        throw new ReadDataFileException(ex.getMessage());
    } catch (ReadDataLib.DataEofException ex) {
        throw new ReadDataFileException(ex.getMessage());
    }
    if(na<1 || na>1000) {
            throw new DataLimitsException(
                    "Error: Number of components is: "+na+nl+
                    "  must be >0 and <1000."+nl+
                    "  Reading data file:"+nl+
                    "  \""+rd.dataFileName+"\"");
    }
    rd.nowReading = "Nbr of complexes (Nx)";
    try{nx = rd.readI();}
    catch(ReadDataLib.DataReadException ex) {
        throw new ReadDataFileException(ex.getMessage());
    } catch (ReadDataLib.DataEofException ex) {
        throw new ReadDataFileException(ex.getMessage());
    }
    if(nx < 0 || nx > 1000000) {
            throw new DataLimitsException(
                    "Error: Number of soluble complexes is: "+nx+nl+
                    "  must be >=0 and < 1 000 000."+nl+
                    "  Reading data file:"+nl+
                    "  \""+rd.dataFileName+"\"");
    }
    rd.nowReading = "Nbr of solid reaction products (nrSol)";
    try{nrSol = rd.readI();}
    catch(ReadDataLib.DataReadException ex) {
        throw new ReadDataFileException(ex.getMessage());
    }
    catch (ReadDataLib.DataEofException ex) {
            throw new ReadDataFileException(ex.getMessage());
    }
    if(nrSol < 0 || nrSol > 100000) {
            throw new DataLimitsException(
                    "Error: Number of solid reaction products is: "+nrSol+nl+
                    "  must be >=0 and < 100 000."+nl+
                    "  Reading data file:"+nl+
                    "  \""+rd.dataFileName+"\"");
    }
    rd.nowReading = "Nbr of solid components (solidC)";
    try{solidC = rd.readI();}
    catch(ReadDataLib.DataReadException ex) {
        throw new ReadDataFileException(ex.getMessage());
    } catch (ReadDataLib.DataEofException ex) {
        throw new ReadDataFileException(ex.getMessage());
    }
    if(solidC<0 || solidC>na) {
            throw new DataLimitsException(
                    "Error: Number of solid components is: "+solidC+nl+
                    "  must be >=0 and <= "+na+" (the nbr of components)."+nl+
                    "  Reading data file:"+nl+
                    "  \""+rd.dataFileName+"\"");
    }
    // ---- number of solids (components + reaction products)
    //      because in Halta the solid components are assumed to be
    //      fictive soluble components with "zero" concentration
    //      (setting noll[] = true) and the corresponding solid complex
    //      is added to the list of solids.
    mSol = solidC + nrSol;
    // ----
    ms = na + nx + mSol;   // total number of chemical species
    // nx = ms - na - mSol;
    // mg = total number of soluble species in the aqueous solution:
    //      all components + soluble complexes
    mg = na + nx; // = ms - mSol;

    // --- create instances of the classes that store the data
    Chem ch = null;
    try{ch = new Chem(na, ms, mSol, solidC);}
    catch (Chem.ChemicalParameterException ex) {
        throw new DataLimitsException(ex.getMessage());
    }
    Chem.ChemSystem cs = ch.chemSystem;
    Chem.ChemSystem.NamesEtc namn = cs.namn;
    Chem.DiagrConcs dgrC = ch.diagrConcs;
    Chem.Diagr diag = ch.diag;

    // ------------------------
    // READ NAMES OF COMPONENTS
    for(int ia=0; ia <cs.Na; ia++) {
        rd.nowReading = "Name  of component nbr "+String.valueOf(ia+1);
        try{namn.identC[ia] = rd.readA();}
        catch(ReadDataLib.DataReadException ex) {
            throw new ReadDataFileException(ex.getMessage());
        } catch (ReadDataLib.DataEofException ex) {
            throw new ReadDataFileException(ex.getMessage());
        }
        namn.comment[ia] = rd.dataLineComment.toString();
    } // for ia

    // ------------------------
    // is this a file created by DataBase/Spana or Hydra/Medusa?
    diag.databaseSpanaFile = rd.fileIsDatabaseOrSpana;

    // ---------------------------
    // READ DATA FOR THE COMPLEXES
    double[] temp = new double[cs.Na];
    String identTemp; double w;
    String comment;
    int i = -1;
    int ki;
    int shift = cs.Na;
    doMs:
    do {
        i++;
        // for components
        if(i < cs.Na) {namn.ident[i] = namn.identC[i];}
        else if(i >= (cs.Ms - solidC)) {
            int ic = cs.Na - (cs.Ms - i);
            namn.ident[i] = namn.identC[ic];}
        else { // for reactions
            String species = "solid";
            if(i < mg) {species = "soluble";}
            rd.nowReading = "Name of "+species+" reaction product "+String.valueOf(i+1);
            try{identTemp = rd.readA();}
            catch(ReadDataLib.DataReadException ex) {
                throw new ReadDataFileException(ex.getMessage());
            } catch (ReadDataLib.DataEofException ex) {
                throw new ReadDataFileException(ex.getMessage());
            }
            comment = rd.dataLineComment.toString();

            rd.nowReading = "Formation Equilibrium Constant for "+species+" complex \""+identTemp+"\"";
            try{w = rd.readD();}
            catch(ReadDataLib.DataReadException ex) {
                throw new ReadDataFileException(ex.getMessage());
            } catch (ReadDataLib.DataEofException ex) {
                throw new ReadDataFileException(ex.getMessage());
            }
            for(int ia =0; ia < cs.Na; ia++) {
                rd.nowReading = "Stoichiometric coeff. "+String.valueOf(ia+1)+" for "+species+" complex \""+identTemp+"\"";
                try{temp[ia] = rd.readD();}
                catch(ReadDataLib.DataReadException ex) {
                    throw new ReadDataFileException(ex.getMessage());
                } catch (ReadDataLib.DataEofException ex) {
                    throw new ReadDataFileException(ex.getMessage());
                }
            } //for ia
            namn.ident[i] = identTemp;
            if(!Util.stringsEqual(comment,rd.dataLineComment.toString())) {
                comment = comment + rd.dataLineComment.toString();
            } else if(comment == null || comment.length()<=0) {comment = rd.dataLineComment.toString();}
            namn.comment[i] = comment;
            ki = i - shift;
            cs.lBeta[ki] = w;
            //for(int ia =0; ia < cs.Na; ia++) {cs.a[ki][ia] = temp[ia];}
            System.arraycopy(temp, 0, cs.a[ki], 0, cs.Na);
        } // for reactions
    } while (i < (cs.Ms-1)); // doMs:

    // for HALTA - solid components:
    //      the data for the extra fictive solid reaction products
    //      equilibrium contstant of formation = 1.
    //      and set stoichiometric coefficients for components
    // Note: this must be undone if you save a "data file" from the
    // information stored in the Chem classes
    addFictiveSolids(cs);

    // --- is there H2O?
    for(i = 0; i < cs.Na; i++) {
        if(Util.isWater(namn.identC[i])) {cs.jWater = i;}
    }

    // -------------------------
    //   READ PLOT INFORMATION
    // -------------------------

    boolean canBePredomFile = false;
    int found;
    if(dbg) {out.println("Reading plot definition...");}
    diag.inputYMinMax = false;
    diag.Eh = false; diag.plotType = -1;
    // --------
    //  Y-axis
    // --------
    String tmp0, tmpUC, ide, ideC, errMsg;
    diag.yLow = Double.MAX_VALUE; diag.yHigh = Double.MIN_VALUE;
    diag.oneArea = -1;
    while(true) {
        rd.nowReading = "Plot information for the Y-axis";
        try {tmp0 = rd.readA();}
        catch(ReadDataLib.DataReadException ex) {
            if(!warn) {throw new PlotDataException(ex.getMessage());}
            else {diag.plotType = -1; return ch;}
        } catch (ReadDataLib.DataEofException ex) {
            if(!warn) {throw new PlotDataException(ex.getMessage());}
            else {diag.plotType = -1; return ch;}
        }
        if(!tmp0.equalsIgnoreCase("EH") && !tmp0.equalsIgnoreCase("ONE AREA")) {break;}
        if(tmp0.equalsIgnoreCase("EH")) {diag.Eh = true;}
        else { //tmp0.equalsIgnoreCase("ONE AREA")
            rd.nowReading = "Species name to be plotted as \"One Area\"";
            try{tmp0 = rd.readA();}
            catch(ReadDataLib.DataReadException ex) {
                if(!warn) {throw new PlotDataException(ex.getMessage());}
                else {diag.plotType = -1; return ch;}
            } catch (ReadDataLib.DataEofException ex) {
                if(!warn) {throw new PlotDataException(ex.getMessage());}
                else {diag.plotType = -1; return ch;}
            }
            tmpUC = tmp0.toUpperCase();
            if(tmpUC.startsWith("*")) {tmpUC = tmpUC.substring(1);}
            found = -1;
            for(i = 0; i < cs.Ms; i++) {
                ideC = namn.ident[i];
                if(ideC.startsWith("*")) {ideC = ideC.substring(1);}
                if(tmpUC.equalsIgnoreCase(ideC)) {found = i; break;}
            } //for i
            if(found < 0) {
                errMsg = "Error in data file:"+nl+
                        "  \""+rd.dataFileName+"\""+nl+
                        "\"One Area\" given, but \""+tmp0+"\" is NOT a species.";
                out.println(LINE+nl+errMsg);
                printDescriptionYaxis(namn.identC, out);
                if(!warn) {throw new PlotDataException(errMsg);}
                else {diag.plotType = -1; return ch;}
            } // not found
            else {diag.oneArea = found;}
        } //if tmp0.equalsIgnoreCase("ONE AREA")
    } // while (true);
    // Values for the Y-axis
    //  plotType=0 Predom diagram        compMain = main component
    //  plotType=1 fraction diagram      compY= main component
    //  plotType=2 log solubility diagram
    //  plotType=3 log (conc) diagram
    //  plotType=4 log (ai/ar) diagram   compY= reference species
    //  plotType=5 pe in Y-axis
    //  plotType=6 pH in Y-axis
    //  plotType=7 log (activity) diagram
    //  plotType=8 H-affinity diagram
    diag.plotType = -1;
    diag.compX = -1; diag.compY = -1; diag.compMain = -1;
    found = -1; ide = tmp0;
    
    if(ide.startsWith("*")) {ide = ide.substring(1);}
    for(i = 0; i < cs.Na; i++) {
        ideC = namn.identC[i];
        if(ideC.startsWith("*")) {ideC = ideC.substring(1);}
        if(ide.equalsIgnoreCase(ideC)) {found = i; break;}
    } // for i
    if(found >= 0) {
        diag.plotType=1;
        diag.compY = found;
    } //fraction diagram
    else {
        tmpUC = tmp0.toUpperCase();
        if(tmpUC.equals("S")) { //log(solubility)
            diag.plotType=2;}
        else if(tmpUC.equals("LS")) { //log(solubility) with Ymin,Ymax
            diag.plotType=2;
            diag.inputYMinMax = true;}
        else if(tmpUC.equals("L")) { //log(conc)
            diag.plotType=3;}
        else if(tmpUC.equals("LC")) { //log(conc) with Ymin,Ymax
            diag.plotType=3;
            diag.inputYMinMax = true;}
        else if(tmpUC.equals("LAC")) { //log(activity) with Ymin,Ymax
            diag.plotType=7;
            diag.inputYMinMax = true;}
        else if(tmpUC.equals("PE")) { // Eh-calc.
            diag.plotType=5;
            diag.inputYMinMax = true;}
        else if(tmpUC.equals("PH")) { // pH-calc.
            diag.plotType=6;
            diag.inputYMinMax = true;}
        else if(tmpUC.equals("R")) { //log (ai/ar)
            diag.plotType=4;
            diag.inputYMinMax = true;
            rd.nowReading = "Reference species name for a relative activity diagram";
            try{tmp0 = rd.readA();}
            catch(ReadDataLib.DataReadException ex) {
                if(!warn) {throw new PlotDataException(ex.getMessage());}
                else {diag.plotType = -1; return ch;}
            } catch (ReadDataLib.DataEofException ex) {
                if(!warn) {throw new PlotDataException(ex.getMessage());}
                else {diag.plotType = -1; return ch;}
            }
            tmpUC = tmp0.toUpperCase();
            if(tmpUC.startsWith("*")) {tmpUC = tmpUC.substring(1);}
            found = -1;
            for(i = 0; i < cs.Ms; i++) {
                ideC = namn.ident[i];
                if(ideC.startsWith("*")) {ideC = ideC.substring(1);}
                if(tmpUC.equalsIgnoreCase(ideC)) {found = i; break;}
            } //for i
            if (found < 0) {
                errMsg = "Error in data file:"+nl+
                        "  \""+rd.dataFileName+"\""+nl+
                        "Relative diagram, but \""+tmp0+"\" is NOT a species.";
                out.println(LINE+nl+errMsg);
                printDescriptionYaxis(namn.identC, out);
                if(!warn) {throw new PlotDataException(errMsg);}
                else {diag.plotType = -1; return ch;}
            } // not found
            else {diag.compY = found;}
         } //if log(ai/ar)
         else if(tmpUC.equals("PS")) { // H-affinity
            diag.plotType=8;
            diag.Hplus = -1;
            for(i = 0; i < cs.Na; i++) {
                ideC = namn.ident[i];
                if(ideC.startsWith("*")) {ideC = ideC.substring(1);}
                if(Util.isProton(ideC)) {diag.Hplus = i; break;}
            } //for i
            if(diag.Hplus <0) {
                errMsg = "Error in data file:"+nl+
                        "  \""+rd.dataFileName+"\""+nl+
                        "H-affinity diagram but no \"H+\" component found.";
                out.println(LINE+nl+errMsg);
                printDescriptionYaxis(namn.identC, out);
                if(!warn) {throw new PlotDataException(errMsg);}
                else {diag.plotType = -1; return ch;}
            } // not found
            diag.OHmin = -1;
            for(i = 0; i < cs.Ms; i++) {
                if(namn.ident[i].equalsIgnoreCase("OH-") ||
                   namn.ident[i].equalsIgnoreCase("OH -")) {
                    diag.OHmin = i; break;
                }
            } //for i
         } // if H-affinity
    } // not a fraction diagram

    if(diag.plotType <= 0) {
        errMsg = "Error in data file:"+nl+
                "  \""+rd.dataFileName+"\""+nl+
                "\""+tmp0+"\" is NOT a valid description for the variable in Y-axis.";
        out.println(LINE+nl+errMsg);
        printDescriptionYaxis(namn.identC, out);
        if(!warn) {throw new PlotDataException(errMsg);}
        else {diag.plotType = -1; return ch;}
    }
    else if(diag.plotType == 1) {canBePredomFile = true;}
    else if(diag.plotType > 1) {canBePredomFile = false;}

    //--- read y-min and y-max
    double y;
    if(diag.inputYMinMax) {
        rd.nowReading = "Minimum value for the Y-axis";
        try{diag.yLow = rd.readD();}
        catch(ReadDataLib.DataReadException ex) {
          if(!warn) {throw new PlotDataException(ex.getMessage());}
          else {
              out.println(LINE+nl+ex.getMessage());
              diag.plotType = -1; return ch;}
        } catch (ReadDataLib.DataEofException ex) {
            if(!warn) {throw new PlotDataException(ex.getMessage());}
            else {
                out.println(LINE+nl+ex.getMessage());
                diag.plotType = -1; return ch;}
        }
        rd.nowReading = "Maximum value for the Y-axis";
        try{diag.yHigh = rd.readD();}
        catch(ReadDataLib.DataReadException ex) {
          if(!warn) {throw new PlotDataException(ex.getMessage());}
          else {
              out.println(LINE+nl+ex.getMessage());
              diag.plotType = -1; return ch;}
        } catch (ReadDataLib.DataEofException ex) {
            if(!warn) {throw new PlotDataException(ex.getMessage());}
            else {
                out.println(LINE+nl+ex.getMessage());
                diag.plotType = -1; return ch;}
        }
        w = Math.abs(diag.yLow - diag.yHigh);
        y = Math.max(Math.abs(diag.yHigh), Math.abs(diag.yLow));
        if((y != 0 && (w/y) < 1e-6) || (w < 1e-6)) { // yLow = yMax
            String msg = "Error in data file:"+nl+
                    "  \""+rd.dataFileName+"\""+nl+
                    "Min-value = Max-value  in Y-axis !";
            if(!warn) {throw new PlotDataException(msg);}
            else {out.println(LINE+nl+msg); diag.plotType = -1; return ch;}
        }
        if(diag.yLow > diag.yHigh) {
            w = diag.yLow;
            diag.yLow = diag.yHigh;
            diag.yHigh = w;}
    } //if inputYMinMax

    // ---------
    //  X-axis
    // ---------
    while(true) {
        rd.nowReading = "Plot information for the X-axis (a component name)";
        try{tmp0 = rd.readA();}
        catch(ReadDataLib.DataReadException ex) {
          if(!warn) {throw new PlotDataException(ex.getMessage());}
          else {
              out.println(LINE+nl+ex.getMessage());
              diag.plotType = -1; return ch;}
        } catch (ReadDataLib.DataEofException ex) {
            if(!warn) {throw new PlotDataException(ex.getMessage());}
            else {
                out.println(LINE+nl+ex.getMessage());
                diag.plotType = -1; return ch;}
        }
        if(!tmp0.equalsIgnoreCase("EH")) {break;}
        diag.Eh = true;
    } //while (true);
    found = -1; ide = tmp0;
    if(ide.startsWith("*")) {ide = ide.substring(1);}
    for(i = 0; i < cs.Na; i++) {
        ideC = namn.identC[i];
        if(ideC.startsWith("*")) {ideC = ideC.substring(1);}
        if(ide.equalsIgnoreCase(ideC)) {found = i; break;}
    }
    if(found >= 0) {diag.compX = found;}
    else {
        errMsg = "Error in data file:"+nl+
                "  \""+rd.dataFileName+"\""+nl+
                "\""+tmp0+"\" is neither a component for the X-axis, nor \"EH\".";
        out.println(LINE+nl+errMsg);
        printDescriptionXaxis(namn.identC, out);
        if(!warn) {throw new PlotDataException(errMsg);}
        else {diag.plotType = -1; return ch;}
    }

    // --------------------------------
    //  is this a Predom input file?
    //  try to read the MAIN COMPONENT
    // --------------------------------
    if(canBePredomFile) {
    while(true) {
        rd.nowReading =
                "Either the main component name for a Predom diagram, or"+nl+
                "conc. data for component nbr 1 ("+namn.identC[0]+")";
        try {tmp0 = rd.readA();}
        catch(ReadDataLib.DataReadException ex) {
          if(!warn) {throw new PlotDataException(ex.getMessage());}
          else {
            out.println(LINE+nl+ex.getMessage());
            diag.plotType = -1; return ch;}
        } catch (ReadDataLib.DataEofException ex) {
            if(!warn) {throw new PlotDataException(ex.getMessage());}
            else {
                out.println(LINE+nl+ex.getMessage());
                diag.plotType = -1; return ch;}
        }
        if(!tmp0.equalsIgnoreCase("EH")) {break;}
        diag.Eh = true;
    } //while (true);
    found = -1; ide = tmp0;
    if(ide.startsWith("*")) {ide = ide.substring(1);}
    for(i = 0; i < cs.Na; i++) {
        ideC = namn.identC[i];
        if(ideC.startsWith("*")) {ideC = ideC.substring(1);}
        if(ide.equalsIgnoreCase(ideC)) {found = i; break;}
    }
    if(found >= 0) { // it is a component name: a Predom input file
        diag.plotType=0; // Predominance area diagram
        diag.compMain = found;
        tmp0 = null;
    }
    // If the file was for a SED-fraction diagram then component is not found,
    // the text read from the input file is stored in tmp0.
    } // if canBePredomFile
    else {tmp0 = null;} // if it is a SED file

    // -----------------------------------------
    //   READ CONCENTRATION FOR EACH COMPONENT
    // -----------------------------------------

    if(dbg) {out.println("Reading the concentrations for each component...");}
    dgrC.hur = new int[cs.Na];
    // Concentration types for each component:
    //    hur =1 for "T" (fixed Total conc.)
    //    hur =2 for "TV" (Tot. conc. Varied)
    //    hur =3 for "LTV" (Log(Tot.conc.) Varied)
    //    hur =4 for "LA" (fixed Log(Activity) value)
    //    hur =5 for "LAV" (Log(Activity) Varied)</pre>
    for(int ia =0; ia < cs.Na; ia++) {
        dgrC.hur[ia] = -1;
        if(tmp0 == null) { // if it is a fraction diagram:
        // --- read the type of concentration
        rd.nowReading = "Concentration type for component nbr "+String.valueOf(ia+1)+" ("+namn.identC[ia]+")";
        try {tmp0 = rd.readA();}
        catch(ReadDataLib.DataReadException ex) {
            if(!warn) {throw new ConcDataException(ex.getMessage());}
            else {
                out.println(LINE+nl+ex.getMessage());
                diag.plotType = -1; return ch;}
        }   catch (ReadDataLib.DataEofException ex) {
            if(!warn) {throw new ConcDataException(ex.getMessage());}
            else {
                out.println(LINE+nl+ex.getMessage());
                diag.plotType = -1; return ch;}
            }
        } // if tmp0 = null
        if(tmp0.equalsIgnoreCase("T")) {dgrC.hur[ia] =1;}
        if(tmp0.equalsIgnoreCase("TV")) {dgrC.hur[ia] =2;}
        if(tmp0.equalsIgnoreCase("LTV")) {dgrC.hur[ia] =3;}
        if(tmp0.equalsIgnoreCase("LA")) {dgrC.hur[ia] =4;}
        if(tmp0.equalsIgnoreCase("LAV")) {dgrC.hur[ia] =5;}
        if(dgrC.hur[ia] <= 0) {
            String msg = "Error in data file:"+nl+
                    "  \""+rd.dataFileName+"\""+nl+
                    "\""+tmp0+"\" is NOT any of: T,TV,LTV,LA or LAV."+nl+
                    "  Reading concentration data for component nbr "+String.valueOf(ia+1)+" ("+namn.identC[ia]+")";
            if(!warn) {throw new ConcDataException(msg);}
            else {
                out.println(LINE+nl+msg);
                diag.plotType = -1; return ch;}
        }

        // --- Read the concentration (min and max if varied)
        String t;
        if(dgrC.hur[ia] ==1 || dgrC.hur[ia] ==4) { //T or LA
            if(dgrC.hur[ia] ==1) {t="total conc.";} else {t="log(activity)";}
            rd.nowReading =
                    "The value for the "+t+" for component nbr "+String.valueOf(ia+1)+" ("+namn.identC[ia]+")";
            try {dgrC.cLow[ia] = rd.readD();}
            catch(ReadDataLib.DataReadException ex) {
                if(!warn) {throw new ConcDataException(ex.getMessage());}
                else {
                    out.println(LINE+nl+ex.getMessage());
                    diag.plotType = -1; return ch;}
            } catch (ReadDataLib.DataEofException ex) {
                if(!warn) {throw new ConcDataException(ex.getMessage());}
                else {
                    out.println(LINE+nl+ex.getMessage());
                    diag.plotType = -1; return ch;}
            }
            dgrC.cHigh[ia] = Double.NaN;
        } //if T or LA
        else { //if TV, LTV or LAV
            if(dgrC.hur[ia] ==2) {t="total conc.";}
            else if (dgrC.hur[ia] ==3) {t="log(tot.conc.)";}
            else {t="log(activity)";}
            rd.nowReading =
                    "The lowest value for the "+t+" for component nbr "+String.valueOf(ia+1)+" ("+namn.identC[ia]+")";
            try {dgrC.cLow[ia] = rd.readD();}
            catch(ReadDataLib.DataReadException ex) {
                if(!warn) {throw new ConcDataException(ex.getMessage());}
                else {
                    out.println(LINE+nl+ex.getMessage());
                    diag.plotType = -1; return ch;}
            } catch (ReadDataLib.DataEofException ex) {
                if(!warn) {throw new ConcDataException(ex.getMessage());}
                else {
                    out.println(LINE+nl+ex.getMessage());
                    diag.plotType = -1; return ch;}
            }
            rd.nowReading =
                    "The highest value for the "+t+" for component nbr "+String.valueOf(ia+1)+" ("+namn.identC[ia]+")";
            try {dgrC.cHigh[ia] = rd.readD();}
            catch(ReadDataLib.DataReadException ex) {
                if(!warn) {throw new ConcDataException(ex.getMessage());}
                else {
                    out.println(LINE+nl+ex.getMessage());
                    diag.plotType = -1; return ch;}
            } catch (ReadDataLib.DataEofException ex) {
                if(!warn) {throw new ConcDataException(ex.getMessage());}
                else {
                    out.println(LINE+nl+ex.getMessage());
                    diag.plotType = -1; return ch;}
            }
        } //if TV, LTV or LAV
        tmp0 = null;
    } //for ia

    //--- is there a title?
    rd.nowReading = "Diagram title";
    try {diag.title = rd.readLine();}
    catch (ReadDataLib.DataEofException ex) {
        diag.title = null;
    }
    catch (ReadDataLib.DataReadException ex) {
        diag.title = null;
        throw new ReadDataFileException(ex.getMessage());
    }
    //--- any other lines...
    StringBuilder t = new StringBuilder();
    rd.nowReading = "Any comment lines after the diagram title";
    while(true) {
        try {t.append(rd.readLine()); t.append(nl);}
        catch (ReadDataLib.DataEofException ex) {break;}
        catch (ReadDataLib.DataReadException ex) {throw new ReadDataFileException(ex.getMessage());}
    }
    if(t.length() >0) {diag.endLines = t.toString();} else {diag.endLines = null;}
    // ---------------------------
    return ch;
} //readChemSystAndPlotInfo(inputFile)
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="printDescription">
private static void printDescriptionYaxis(String[] identC, java.io.PrintStream out) {
    if(out == null) {out = System.out;}
    String description =
    "General format for Y-axis plot-data is:      to obtain:"+nl+
    "    component-name,                        either fraction diagram, or"+nl+
    "                                           predominance area diagram"+nl+
    "    L,                                     log(conc) diagram"+nl+
    "    LC, max-Y-value, min-Y-value,             -\"-"+nl+
    "    LAC, max-Y-value, min-Y-value,         log(activity) diagram"+nl+
    "    S,                                     log(solubility) diagram"+nl+
    "    LS, max-Y-value, min-Y-value,             -\"-"+nl+
    "    pe, max-Y-value, min-Y-value,          pe-calc. in Y-axis"+nl+
    "    pH, max-Y-value, min-Y-value,          pH-calc. in Y-axis"+nl+
    "    R, species-name, Y-max, Y-min,         log(ai/ar) diagram"+nl+
    "    PS,                                    H+ affinity diagram"+nl+
    "You may preceede this with \"Eh,\" in order to use the redox potential"+nl+
    "instead of \"pe\" in the diagram.  For a predominance area diagram, you"+nl+
    "may also preceede the Y-axis plot-data with \"ONE AREA, species-name\","+nl+
    "to only show the predominance area of the given species.";
    out.println();
    out.println(description);
    listComponents(identC, out);
    out.println("Error in input plot-data for:  Y-axis");
} // printDescriptionYaxis()
private static void printDescriptionXaxis(String[] identC, java.io.PrintStream out){
    if(out == null) {out = System.out;}
    String description =
    "General format for X-axis plot-data is:"+nl+
    "A component name, for which the concentration is varied.  You may"+nl+
    "preceede this with \"Eh,\" in order to use the redox potential"+nl+
    "instead of \"pe\" in the diagram.";
    out.println();
    out.println(description);
    listComponents(identC, out);
    out.println("Error in input plot-data for:  X-axis");
} // printDescriptionXaxis()
private static void listComponents(String[] identC, java.io.PrintStream out){
    if(out == null) {out = System.out;}
    out.println("The names of the components are:");
    int n0, nM, iPl, nP;
    out.print("    ");
    n0 = 0;     //start index to print
    nM = identC.length - 1;  //end index to print
    iPl = 7; nP= nM-n0; //items_Per_Line and number of items to print
  print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
        out.format(engl,"  \"%s\",",identC[kjj]);
        if(kjj >(nM-1)) {out.println(); break print_1;}} //for ia
        out.println(); out.print("    ");} //for ijj
} //listComponents()
//</editor-fold>

/** the number of components, complexes, solids, etc, were
 * either less than zero, our unrealistically large */
public static class DataLimitsException extends Exception {
    public DataLimitsException() {}
    public DataLimitsException(String txt) {super(txt);}
} //DataLimitsException
/** problem found while reading the input data file */
public static class ReadDataFileException extends Exception {
    public ReadDataFileException() {}
    public ReadDataFileException(String txt) {super(txt);}
} //ReadDataFileException
/** problem found while reading the plot information from the input data file */
public static class PlotDataException extends Exception {
    public PlotDataException() {}
    public PlotDataException(String txt) {super(txt);}
} //PlotDataException
/** problem found while reading the concentrations from the input data file */
public static class ConcDataException extends Exception {
    public ConcDataException() {}
    public ConcDataException(String txt) {super(txt);}
} //ConcDataException

//<editor-fold defaultstate="collapsed" desc="addFictiveSolids">
public static boolean addFictiveSolids(Chem.ChemSystem cs) {
  if(cs == null) {return false;}
    // for HALTA - solid components:
    //      the data for the extra fictive solid reaction products
    //      equilibrium contstant of formation = 1.
    //      and set stoichiometric coefficients for components
    // Note: this must be undone if you save a "data file" from the
    // information stored in the Chem classes
    if(cs.solidC >0) {
        int j, k, ji;
        for(int m =0; m < cs.solidC; m++) {
            // j= (Ms-Na)-solidC ...(Ms-Na)-1
            j = (cs.Ms - cs.Na - cs.solidC) + m;
            // k = (Na-solidC)...(Na-1)
            k = (cs.Na - cs.solidC) + m;
            cs.noll[k] = true; // solid components are not aqueous specie
            cs.lBeta[j] = 0.; // equilibrium contstant of formation = 1.
            for(int n = 0; n < cs.Na; n++) {
                cs.a[j][n] = 0.;
                if(n == k) {cs.a[j][n] = 1.;}
            } // for n
            ji = j + cs.Na;
            cs.namn.ident[ji] = cs.namn.identC[k];
            if(cs.namn.ident[ji].startsWith("*")) {
                cs.namn.ident[ji] = cs.namn.ident[ji].substring(1);
                cs.noll[ji] = true;
            }
        } // for m = 0 ... (solidC-1)
    } //if solidC >0
  return true;
}
//</editor-fold>

} 