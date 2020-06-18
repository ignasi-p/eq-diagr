package simpleEquilibriumDiagrams;

import lib.common.Util;
import lib.kemi.chem.Chem;
import lib.kemi.graph_lib.DiagrPaintUtility;
import lib.kemi.graph_lib.GraphLib;
import lib.kemi.haltaFall.Factor;
import lib.kemi.haltaFall.HaltaFall;
import lib.kemi.readDataLib.ReadDataLib;
import lib.kemi.readWriteDataFiles.ReadChemSyst;

/** Creates a chemical equilibrium diagram.<br>
 * This program will read a data file, make some calculations, and
 * create a diagram. The diagram is displayed and stored in a plot file.<br>
 * The data file contains the description of a chemical system (usually
 * an aqueous system) and a description on how the diagram should be drawn:
 * what should be in the axes, concentrations for each chemical
 * component, etc.<br>
 * If the command-line option "-nostop" is given, then no option dialogs will
 * be shown.<br>
 * Output messages and errors are written to <code>System.out</code> and
 * <code>.err</code> (the console) and output is directed to a JTextArea as well.
 * <br>
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
public class SED extends javax.swing.JFrame {
    static final String VERS = "2020-June-18";
    static final String progName = "SED";
/** variable needed in "main" method */
    private static SED sedFrame;
/** print debug information? */
    private static final boolean DBG_DEFAULT = false;
/** print debug information? */
    boolean dbg = false;
/** if true the program does not exit after the diagram is drawn (and saved
 * as a plot file), that is, the diagram remains visible for the user.
 * If false, and if the data file is given as a command-line argument,
 * then the program saves the diagram and exits without displaying it */
    private boolean doNotExit = false;
/** if true the program does display dialogs with warnings or errors */
    private boolean doNotStop = false;
/** if true the concentration range in the x-axis may have a reversed "order",
 * that is, minimum value to the right and maximum value to the left, if so
 * it is given in the input data file. */
    private boolean reversedConcs = false;
/** the directory where the program files are located */
    private static String pathApp;
/** the directory where the last input data file is located */
    StringBuffer pathDef = new StringBuffer();
    private java.io.File inputDataFile = null;
    private java.io.File outputPltFile = null;
/** true if an input data file is given in the command line.
 * If so, then the calculations are performed without waiting for user actions,
 * a diagram is generated and saved, and the program exits unless doNotExit is true. */
    private boolean inputDataFileInCommandLine;
/** true if the calculations have finished, or if the user wishes to finish
 * them (and to end the program) */
    private boolean finishedCalculations = true;
/** true if the graphic user interface (GUI) has been displayed and then closed by the user */
    private boolean programEnded = false;
/** An instance of SwingWorker to perform the HaltaFall calculations */
    private HaltaTask tsk = null;
/** used to calculate execution time */
    private long calculationStart = 0;
/** the execution time */
    private long calculationTime = 0;
/** size of the user's computer screen */
    static java.awt.Dimension screenSize;
/** original size of the program window */
    private final java.awt.Dimension originalSize;

/** a class used to store and retrieve data, used by HaltaFall */
    private Chem ch = null;
/** a class to store data about a chemical system */
    private Chem.ChemSystem cs = null;
/** a class to store data about a the concentrations to calculate an
 * equilibrium composition, used by HaltaFall */
    private Chem.ChemSystem.ChemConcs csC = null;
/** a class to store diverse information on a chemical system: names of species, etc */
    private Chem.ChemSystem.NamesEtc namn = null;
/** a class to store array data about a diagram */
    private Chem.DiagrConcs dgrC = null;
/** a class to store non-array data about a diagram */
    private Chem.Diagr diag = null;
/** a class to calculate activity coefficients */
    Factor factor;
/** the calculations class */
    private HaltaFall h;

/** a class to read text files where data is separated by commas */
    private ReadDataLib rd;
    private Plot plot = null;
/** data from a plot-file needed by the paint methods */
    GraphLib.PltData dd; // instantiated in "Plot.drawPlot"
                                   // it containts the info in the plot file

    private HelpAboutF helpAboutFrame = null;
    private final javax.swing.JFileChooser fc;
    private final javax.swing.filechooser.FileNameExtensionFilter filterDat;
/** true if the message text area should be erased before a new datafile is
 * read (after the user has selected a new data file) */
    private boolean eraseTextArea = false;
/** Where errors will be printed. It may be <code>System.err</code>.
 * If null, <code>System.err</code> is used. */
    private java.io.PrintStream err;
/** Where messages will be printed. It may be <code>System.out</code>.
 * If null, <code>System.out</code> is used. */
    private java.io.PrintStream out;

    /** The maximum number of calculation steps along the X-axis */
    private final static int NSTP_MAX = 300;
    /** The minimum number of calculation steps along the X-axis */
    private final static int NSTP_MIN = 4;
    private final static int NSTP_DEF = 50;
    /** The number of calculation steps along the X-axis.
     * The number of points calculated is: <code>nSteps+1</code>.
     * Note that to the outside world, the number of calculation points are reported. */
    int nSteps = NSTP_DEF;
    /** The calculation step being (along the x-axis) performed out of nSteps
     * The first point corresponds to no step, <code>nStepX = 0</code> */
    private int nStepX;
    /** The given input concentrations recalculated for each calculation step.
     * Dimensions: bt[Na][nSteps+1] */
    double[][] bt;
    private final double ln10 = Math.log(10d);
    /** true if activity coeficients have to be calculated */
    boolean calcActCoeffs = false;
    private final int actCoeffsModelDefault =2;
    /** the ionic strength, or -1 if it has to be calculated at each calculation step */
    private double ionicStrength = Double.NaN;
    /** the temperature value given given in the command line */
    double temperature_InCommandLine = Double.NaN;
    /** the pressure value given given in the command line */
    double pressure_InCommandLine = Double.NaN;
    private int actCoeffsModel_InCommandLine = -1;
    private double tolHalta = Chem.TOL_HALTA_DEF;
    double peEh = Double.NaN;
    double tHeight;
/** used to direct SED and Predom to draw concentration units
 * as either:<ul><li>0="molal"</li><li>1="mol/kg_w"</li><li>2="M"</li><li>-1=""</li></ul> */
    int conc_units = 0;
    String[] cUnits = new String[]{"","molal","mol/kg`w'","M"};
/** used to direct SED and Predom to draw concentrations
 * as either scientific notation or engineering notation:<ul><li>0 = no choice
 * (default, means scientific for "molal" and engineering for "M")</li>
 * <li>1 = scientific notation</li><li>2 = engineering notation</li></ul> */
    int conc_nottn = 0;

    float threshold = 0.03f;
    private boolean tableOutput = false;
    private Table table = null;
    String tblExtension = "csv";
    String tblFieldSeparator = ";";
    String tblCommentStart = "\"";
    String tblCommentEnd = "\"";

    /** output debug reporting in HaltaFall. Default = Chem.DBGHALTA_DEF = 1 (report errors only)
     * @see Chem.ChemSystem.ChemConcs#dbg Chem.ChemSystem.ChemConcs.dbg */
    int dbgHalta = Chem.DBGHALTA_DEF;
    /** true if the component has either <code>noll</code> = false or it has positive
     * values for the stoichiometric coefficients (a[ix][ia]-values)
     * @see chem.Chem.ChemSystem#a a
     * @see chem.Chem.ChemSystem#noll noll */
    private boolean[] pos;
    /** true if the component has some negative
     * values for the stoichiometric coefficients (a[ix][ia]-values)
     * @see chem.Chem.ChemSystem#a a */
    private boolean[] neg;
    //
    private final DiagrPaintUtility diagrPaintUtil;
    //
    private final java.io.PrintStream errPrintStream  =
       new java.io.PrintStream(
         new errFilteredStreamSED(
           new java.io.ByteArrayOutputStream()),true);
    private final java.io.PrintStream outPrintStream  =
       new java.io.PrintStream(
         new outFilteredStreamSED(
           new java.io.ByteArrayOutputStream()),true);
    /** true if a minumum of information is sent to the console (System.out)
     * when inputDataFileInCommandLine and not doNotExit.
     * False if all output is sent only to the JTextArea panel. */
    boolean consoleOutput = true;
    /** New-line character(s) to substitute "\n".<br>
     * It is needed when a String is created first, including new-lines,
     * and the String is then printed. For example
     * <pre>String t = "1st line\n2nd line";
     *System.out.println(t);</pre>will add a carriage return character
     * between the two lines, which on Windows system might be
     * unsatisfactory. Use instead:
     * <pre>String t = "1st line" + nl + "2nd line";
     *System.out.println(t);</pre> */
    private static final String nl = System.getProperty("line.separator");
    private static final java.util.Locale engl = java.util.Locale.ENGLISH;
    static final String LINE = "-------------------------------------";
    private static final String SLASH = java.io.File.separator;
    //
    //todo: no names can start with "*" after reading the chemical system

  //<editor-fold defaultstate="collapsed" desc="Constructor">
    /** Creates new form SED
     * @param doNotExit0
     * @param doNotStop0
     * @param dbg0 */
    public SED(
            final boolean doNotExit0,
            final boolean doNotStop0,
            final boolean dbg0) {
        initComponents();
        dbg = dbg0;
        doNotStop = doNotStop0;
        doNotExit = doNotExit0;
        // ---- redirect all output to the tabbed pane
        out = outPrintStream;
        err = errPrintStream;
        // ---- get the current working directory
        setPathDef();
        if(DBG_DEFAULT) {out.println("default path: \""+pathDef.toString()+"\"");}
        // ---- Define open/save file filters
        fc = new javax.swing.JFileChooser("."); //the "user" path
        filterDat =new javax.swing.filechooser.FileNameExtensionFilter("*.dat", new String[] { "DAT"});
        // ----
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        //--- F1 for help
        javax.swing.KeyStroke f1KeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1,0, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(f1KeyStroke,"F1");
        javax.swing.Action f1Action = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if(jMenuHelpAbout.isEnabled()) {jMenuHelpAbout.doClick();}
            }};
        getRootPane().getActionMap().put("F1", f1Action);
        //--- ctrl-C: stop calculations
        javax.swing.KeyStroke ctrlCKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_DOWN_MASK, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlCKeyStroke,"CTRL_C");
        javax.swing.Action ctrlCAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if(jTabbedPane.getSelectedIndex() == 1) {
                      if(h != null) {h.haltaCancel();}
                      if(tsk != null) {tsk.cancel(true);}
                      finishedCalculations = true;
                      SED.this.notify_All();
                }
            }};
        getRootPane().getActionMap().put("CTRL_C", ctrlCAction);
        //--- define Alt-key actions
        //--- alt-X: eXit
        javax.swing.KeyStroke altXKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altXKeyStroke,"ALT_X");
        javax.swing.Action altXAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if(jMenuFileXit.isEnabled()) {jMenuFileXit.doClick();}
            }};
        getRootPane().getActionMap().put("ALT_X", altXAction);
        //--- alt-Q: quit
        javax.swing.KeyStroke altQKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.ALT_MASK, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altQKeyStroke,"ALT_Q");
        getRootPane().getActionMap().put("ALT_Q", altXAction);
        //--- alt-Enter: make diagram
        javax.swing.KeyStroke altEnterKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_ENTER, java.awt.event.InputEvent.ALT_MASK, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altEnterKeyStroke,"ALT_Enter");
        javax.swing.Action altEnterAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if(jMenuFileMakeD.isEnabled()) {jMenuFileMakeD.doClick();}
            }};
        getRootPane().getActionMap().put("ALT_Enter", altEnterAction);
        //--- alt-C: method for activity coefficients
        javax.swing.KeyStroke altCKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.ALT_MASK, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altCKeyStroke,"ALT_C");
        javax.swing.Action altCAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if(jTabbedPane.getSelectedIndex() == 0 &&
                        jComboBoxModel.isEnabled()) {jComboBoxModel.requestFocusInWindow();}
            }};
        getRootPane().getActionMap().put("ALT_C", altCAction);
        //--- alt-D: diagram
        javax.swing.KeyStroke altDKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.ALT_MASK, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altDKeyStroke,"ALT_D");
        javax.swing.Action altDAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if(jTabbedPane.getSelectedIndex() == 0 &&
                        jMenuFileMakeD.isEnabled() &&
                        jButtonDoIt.isEnabled()) {jButtonDoIt.doClick();
                } else if(jTabbedPane.getSelectedIndex() != 2 &&
                        jTabbedPane.isEnabledAt(2)) {jTabbedPane.setSelectedIndex(2);
                }
            }};
        getRootPane().getActionMap().put("ALT_D", altDAction);
        //--- alt-E: hEight
        javax.swing.KeyStroke altEKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.ALT_MASK, false);
        javax.swing.Action altEAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if(jTabbedPane.getSelectedIndex() == 0){jScrollBarHeight.requestFocusInWindow();}
            }};
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altEKeyStroke,"ALT_E");
        getRootPane().getActionMap().put("ALT_E", altEAction);
        //--- alt-L: pLot file name
        javax.swing.KeyStroke altLKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.ALT_MASK, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altLKeyStroke,"ALT_L");
        javax.swing.Action altLAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if(jTabbedPane.getSelectedIndex() == 0){jTextFieldPltFile.requestFocusInWindow();}
            }};
        getRootPane().getActionMap().put("ALT_L", altLAction);
        //--- alt-M: messages pane
        javax.swing.KeyStroke altMKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.ALT_MASK, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altMKeyStroke,"ALT_M");
        javax.swing.Action altMAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if(jTabbedPane.getSelectedIndex() != 1 &&
                        jTabbedPane.isEnabledAt(1)) {jTabbedPane.setSelectedComponent(jScrollPaneMessg);}
            }};
        getRootPane().getActionMap().put("ALT_M", altMAction);
        //--- alt-N:  nbr of points
        javax.swing.KeyStroke altNKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.ALT_MASK, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altNKeyStroke,"ALT_N");
        javax.swing.Action altNAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if(jTabbedPane.getSelectedIndex() == 0){jScrollBarNbrPoints.requestFocusInWindow();}
            }};
        getRootPane().getActionMap().put("ALT_N", altNAction);
        //--- alt-P: parameters
        javax.swing.KeyStroke altPKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.ALT_MASK, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altPKeyStroke,"ALT_P");
        javax.swing.Action altPAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if(jTabbedPane.getSelectedIndex() != 0) {jTabbedPane.setSelectedIndex(0);}
            }};
        getRootPane().getActionMap().put("ALT_P", altPAction);
        //--- alt-S:  ionic strength / stop calculations
        javax.swing.KeyStroke altSKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altSKeyStroke,"ALT_S");
        javax.swing.Action altSAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if(jTabbedPane.getSelectedIndex() == 0 &&
                        jTextFieldIonicStgr.isEnabled()) {
                    jTextFieldIonicStgr.requestFocusInWindow();
                } else if(jTabbedPane.getSelectedIndex() == 1) {
                      if(h != null) {h.haltaCancel();}
                      if(tsk != null) {tsk.cancel(true);}
                      finishedCalculations = true;
                      SED.this.notify_All();
                }
            }};
        getRootPane().getActionMap().put("ALT_S", altSAction);
        //--- alt-T:  tolerance in Haltafall
        javax.swing.KeyStroke altTKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.ALT_MASK, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altTKeyStroke,"ALT_T");
        javax.swing.Action altTAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if(jTabbedPane.getSelectedIndex() == 0 &&
                        jComboBoxTol.isEnabled()) {jComboBoxTol.requestFocusInWindow();}
            }};
        getRootPane().getActionMap().put("ALT_T", altTAction);
        // -------

        //--- Title
        this.setTitle("Simple Equilibrium Diagrams");
        jMenuBar.add(javax.swing.Box.createHorizontalGlue(),2); //move "Help" menu to the right

        //--- center Window on Screen
        originalSize = this.getSize();
        screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int left; int top;
        left = Math.max(55, (screenSize.width  - originalSize.width ) / 2);
        top = Math.max(10, (screenSize.height - originalSize.height) / 2);
        this.setLocation(Math.min(screenSize.width-100, left),
                         Math.min(screenSize.height-100, top));
        //---- Icon
        String iconName = "images/PlotLog256_32x32_blackBckgr.gif";
        java.net.URL imgURL = this.getClass().getResource(iconName);
        java.awt.Image icon;
        if (imgURL != null) {
            icon = new javax.swing.ImageIcon(imgURL).getImage();
            this.setIconImage(icon);
            //com.apple.eawt.Application.getApplication().setDockIconImage(new javax.swing.ImageIcon("Football.png").getImage());
            if(System.getProperty("os.name").startsWith("Mac OS")) {
                try {
                    Class<?> c = Class.forName("com.apple.eawt.Application");
                    //Class params[] = new Class[] {java.awt.Image.class};
                    java.lang.reflect.Method m =
                        c.getDeclaredMethod("setDockIconImage",new Class[] { java.awt.Image.class });
                    Object i = c.newInstance();
                    Object paramsObj[] = new Object[]{icon};
                    m.invoke(i, paramsObj);
                } catch (Exception e) {System.out.println("Error: "+e.getMessage());}
            }
        } else {
            System.out.println("Error: Could not load image = \""+iconName+"\"");
        }

        //--- set up the Form
        jMenuFileMakeD.setEnabled(false);
        jButtonDoIt.setText("make the Diagram");
        jButtonDoIt.setEnabled(false);
        java.awt.Font f = new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12);
        jTextAreaA.setFont(f);
        jTextAreaA.setText(null);
        jTabbedPane.setEnabledAt(1, false);
        jTabbedPane.setEnabledAt(2, false);

        jScrollBarNbrPoints.setFocusable(true);
        jScrollBarNbrPoints.setValue(nSteps);
        jScrollBarHeight.setFocusable(true);
        tHeight = 1;
        jScrollBarHeight.setValue(Math.round((float)(10*tHeight)));

        jLabelPltFile.setText("plot file name");
        jLabelPltFile.setEnabled(false);
        jTextFieldPltFile.setText(null);
        jTextFieldPltFile.setEnabled(false);
        jLabelStatus.setText("waiting...");
        jLabelProgress.setText(" ");
        jLabelTemperature.setText("NaN"); // no temperature given
        jLabelPressure.setText("NaN"); // no pressure given
        jComboBoxModel.setSelectedIndex(actCoeffsModelDefault);

        //--- the methods in DiagrPaintUtility are used to paint the diagram
        diagrPaintUtil = new DiagrPaintUtility();

    } // SED constructor
    //</editor-fold>

//<editor-fold defaultstate="collapsed" desc="start">
 /** Sets this window frame visible, and deals with the command-line arguments
  * @param reversedConcs0 needed when reading the input data file.
  * The command-line argument may be given <i>after</i> the data file name...
  * @param help0 if help is displayed, request focus for the text pane,
  * where the help is shown
  * @param args the command-line arguments */
  private void start(
          final boolean reversedConcs0,
          final boolean help0,
          final String[] args) {
    this.setVisible(true);

    //--- deal with any command line arguments
    inputDataFileInCommandLine = false;
    reversedConcs = reversedConcs0; //this is needed when reading the data file
    boolean argErr = false;
    if(args != null && args.length >0){
        for (String arg : args) {
            if (!dispatchArg(arg)) {argErr = true;}
        } // for arg
    } // if argsList != null
    if(argErr && !doNotExit) {end_program(); return;}
    out.println("Finished reading command-line arguments.");
    if(dbg) {
      out.println("--------"+nl+
                  "Application path: \""+pathApp+"\""+nl+
                  "Default path: \""+pathDef.toString()+"\""+nl+
                  "--------");
    }
    consoleOutput = inputDataFileInCommandLine & !doNotExit;
    // is the temperture missing even if a ionic strength is given?
    if((!Double.isNaN(ionicStrength) && Math.abs(ionicStrength) >1e-10)
            && Double.isNaN(temperature_InCommandLine)) {
        String msg = "Warning: ionic strength given as command line argument, I="
                +(float)ionicStrength+nl+
                "    but no temperature is given on the command line.";
        out.println(msg);
    }
    // if the command-line "-i" is not given, set the ionic strength to zero
    if(!calcActCoeffs) {ionicStrength = 0;}

    //set_dbgHalta_inRadioMenus();
    jCheckReverse.setSelected(reversedConcs);
    jCheckTable.setSelected(tableOutput);
    jCheckActCoeff.setSelected(calcActCoeffs);
    jTextFieldIonicStgr.setText(String.valueOf(ionicStrength));
    showActivityCoefficientControls(calcActCoeffs);
    set_tol_inComboBox();

    //--- if help is requested on the command line and the
    //    program's window stays on screen: show the message pane
    if(help0) {
        jTextAreaA.setCaretPosition(0);
        jTabbedPane.setSelectedComponent(jScrollPaneMessg);
    }

    // Make a diagram if an input file is given in the command-line
    if(inputDataFileInCommandLine) {
        if(outputPltFile == null) {
            String txt = inputDataFile.getAbsolutePath();
            String plotFileN = txt.substring(0,txt.length()-3).concat("plt");
            outputPltFile = new java.io.File(plotFileN);
        }
        // note: as the calculations are done on a worker thread, this returns pretty quickly
        try {doCalculations();}
        catch (Exception ex) {showErrMsgBx(ex);}
    }
    programEnded = false;
  } //start
//</editor-fold>

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupDebug = new javax.swing.ButtonGroup();
        jTabbedPane = new javax.swing.JTabbedPane();
        jPanelParameters = new javax.swing.JPanel();
        jPanelFiles = new javax.swing.JPanel();
        jLabelData = new javax.swing.JLabel();
        jTextFieldDataFile = new javax.swing.JTextField();
        jLabelPltFile = new javax.swing.JLabel();
        jTextFieldPltFile = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jCheckReverse = new javax.swing.JCheckBox();
        jCheckTable = new javax.swing.JCheckBox();
        jPanelActC = new javax.swing.JPanel();
        jCheckActCoeff = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jLabelIonicStr = new javax.swing.JLabel();
        jTextFieldIonicStgr = new javax.swing.JTextField();
        jLabelIonicStrM = new javax.swing.JLabel();
        jPanelT = new javax.swing.JPanel();
        jLabelT = new javax.swing.JLabel();
        jLabelTC = new javax.swing.JLabel();
        jLabelP = new javax.swing.JLabel();
        jLabelPressure = new javax.swing.JLabel();
        jLabelBar = new javax.swing.JLabel();
        jLabelTemperature = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabelModel = new javax.swing.JLabel();
        jComboBoxModel = new javax.swing.JComboBox();
        jPanel4 = new javax.swing.JPanel();
        jLabelTol = new javax.swing.JLabel();
        jComboBoxTol = new javax.swing.JComboBox();
        jPanel6 = new javax.swing.JPanel();
        jButtonDoIt = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabelNbrPText = new javax.swing.JLabel();
        jLabelPointsNbr = new javax.swing.JLabel();
        jScrollBarNbrPoints = new javax.swing.JScrollBar();
        jLabelHeight = new javax.swing.JLabel();
        jLabelHD = new javax.swing.JLabel();
        jScrollBarHeight = new javax.swing.JScrollBar();
        jScrollPaneMessg = new javax.swing.JScrollPane();
        jTextAreaA = new javax.swing.JTextArea();
        jPanelDiagram = new javax.swing.JPanel()   
        {   
            @Override
            public void paint(java.awt.Graphics g)   
            {   
                super.paint(g);   
                paintDiagrPanel(g);   
            }   
        };
        jPanelStatusBar = new javax.swing.JPanel();
        jLabelStatus = new javax.swing.JLabel();
        jLabelProgress = new javax.swing.JLabel();
        jMenuBar = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuFileOpen = new javax.swing.JMenuItem();
        jMenuFileMakeD = new javax.swing.JMenuItem();
        jMenuFileXit = new javax.swing.JMenuItem();
        jMenuDebug = new javax.swing.JMenu();
        jCheckBoxMenuSEDdebug = new javax.swing.JCheckBoxMenuItem();
        jMenuSave = new javax.swing.JMenuItem();
        jMenuHF_dbg = new javax.swing.JMenuItem();
        jMenuCancel = new javax.swing.JMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        jMenuHelpAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jTabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        jTabbedPane.setAlignmentX(0.0F);

        jLabelData.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelData.setLabelFor(jTextFieldDataFile);
        jLabelData.setText("input data file:");

        jTextFieldDataFile.setBackground(new java.awt.Color(204, 204, 204));
        jTextFieldDataFile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextFieldDataFileMouseClicked(evt);
            }
        });
        jTextFieldDataFile.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextFieldDataFileKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldDataFileKeyTyped(evt);
            }
        });

        jLabelPltFile.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelPltFile.setLabelFor(jTextFieldPltFile);
        jLabelPltFile.setText("<html>p<u>l</u>ot file name:</html>");

        javax.swing.GroupLayout jPanelFilesLayout = new javax.swing.GroupLayout(jPanelFiles);
        jPanelFiles.setLayout(jPanelFilesLayout);
        jPanelFilesLayout.setHorizontalGroup(
            jPanelFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelFilesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelData, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPltFile, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldDataFile)
                    .addComponent(jTextFieldPltFile))
                .addContainerGap())
        );
        jPanelFilesLayout.setVerticalGroup(
            jPanelFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelFilesLayout.createSequentialGroup()
                .addGroup(jPanelFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelData)
                    .addComponent(jTextFieldDataFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldPltFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPltFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(120, 120, 120))
        );

        jCheckReverse.setMnemonic(java.awt.event.KeyEvent.VK_R);
        jCheckReverse.setText("<html>allow <u>R</u>eversed min. and max. axes limits</html>");
        jCheckReverse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckReverseActionPerformed(evt);
            }
        });

        jCheckTable.setMnemonic(java.awt.event.KeyEvent.VK_O);
        jCheckTable.setText("<html>table <u>O</u>utput</html>");
        jCheckTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckTableActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jCheckReverse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(53, 53, 53)
                .addComponent(jCheckTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(189, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckTable, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckReverse, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jCheckActCoeff.setMnemonic(java.awt.event.KeyEvent.VK_A);
        jCheckActCoeff.setText("<html><u>A</u>ctivity coefficient calculations</html>");
        jCheckActCoeff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckActCoeffActionPerformed(evt);
            }
        });

        jLabelIonicStr.setLabelFor(jTextFieldIonicStgr);
        jLabelIonicStr.setText("<html>ionic <u>S</u>trength</html>");
        jLabelIonicStr.setEnabled(false);

        jTextFieldIonicStgr.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldIonicStgr.setEnabled(false);
        jTextFieldIonicStgr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldIonicStgrActionPerformed(evt);
            }
        });
        jTextFieldIonicStgr.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldIonicStgrFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldIonicStgrFocusLost(evt);
            }
        });
        jTextFieldIonicStgr.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextFieldIonicStgrKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldIonicStgrKeyTyped(evt);
            }
        });

        jLabelIonicStrM.setText("<html>mol/(kg H<sub>2</sub>O)</html>");
        jLabelIonicStrM.setEnabled(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabelIonicStr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldIonicStgr, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelIonicStrM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelIonicStr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldIonicStgr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabelIonicStrM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabelT.setText("<html>temperature =</html>");
        jLabelT.setEnabled(false);

        jLabelTC.setText("<html>°C</html>");
        jLabelTC.setEnabled(false);

        jLabelP.setText("<html>pressure =</html>");
        jLabelP.setEnabled(false);

        jLabelPressure.setText("88.36");
        jLabelPressure.setEnabled(false);

        jLabelBar.setText("<html>bar</html>");
        jLabelBar.setEnabled(false);

        jLabelTemperature.setText("300");
        jLabelTemperature.setEnabled(false);

        javax.swing.GroupLayout jPanelTLayout = new javax.swing.GroupLayout(jPanelT);
        jPanelT.setLayout(jPanelTLayout);
        jPanelTLayout.setHorizontalGroup(
            jPanelTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabelT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelTemperature)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelTC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(71, 71, 71)
                .addComponent(jLabelP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelPressure)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(146, Short.MAX_VALUE))
        );
        jPanelTLayout.setVerticalGroup(
            jPanelTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabelTC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabelP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabelPressure)
                .addComponent(jLabelBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabelTemperature))
        );

        jLabelModel.setLabelFor(jComboBoxModel);
        jLabelModel.setText("<html>activity <u>C</u>officient model:<html>");

        jComboBoxModel.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Davies eqn.", "SIT (Specific Ion interaction 'Theory')", "simplified HKF (Helgeson, Kirkham & Flowers)" }));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBoxModel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelModel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(28, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jLabelModel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxModel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanelActCLayout = new javax.swing.GroupLayout(jPanelActC);
        jPanelActC.setLayout(jPanelActCLayout);
        jPanelActCLayout.setHorizontalGroup(
            jPanelActCLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelActCLayout.createSequentialGroup()
                .addGroup(jPanelActCLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelActCLayout.createSequentialGroup()
                        .addGroup(jPanelActCLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelActCLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jCheckActCoeff, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanelT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanelActCLayout.setVerticalGroup(
            jPanelActCLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelActCLayout.createSequentialGroup()
                .addGroup(jPanelActCLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelActCLayout.createSequentialGroup()
                        .addComponent(jCheckActCoeff, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addComponent(jPanelT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabelTol.setLabelFor(jComboBoxTol);
        jLabelTol.setText("<html><u>T</u>olerance (for calculations in HaltaFall):</html>");

        jComboBoxTol.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1E-2", "1E-3", "1E-4", "1E-5", "1E-6", "1E-7", "1E-8", "1E-9", " " }));
        jComboBoxTol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxTolActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabelTol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxTol, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(265, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelTol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jComboBoxTol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jButtonDoIt.setMnemonic('D');
        jButtonDoIt.setText("make the Diagram");
        jButtonDoIt.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButtonDoIt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDoItActionPerformed(evt);
            }
        });

        jLabelNbrPText.setLabelFor(jLabelPointsNbr);
        jLabelNbrPText.setText("<html><u>N</u>br of calc. steps:</html>");

        jLabelPointsNbr.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelPointsNbr.setText("50");
        jLabelPointsNbr.setToolTipText("double-click to reset to default");
        jLabelPointsNbr.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelPointsNbrMouseClicked(evt);
            }
        });

        jScrollBarNbrPoints.setMaximum(NSTP_MAX+1);
        jScrollBarNbrPoints.setMinimum(NSTP_MIN);
        jScrollBarNbrPoints.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        jScrollBarNbrPoints.setVisibleAmount(1);
        jScrollBarNbrPoints.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                jScrollBarNbrPointsAdjustmentValueChanged(evt);
            }
        });
        jScrollBarNbrPoints.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jScrollBarNbrPointsFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jScrollBarNbrPointsFocusLost(evt);
            }
        });

        jLabelHeight.setText("<html>h<u>e</u>ight of text in diagram:</html>");

        jLabelHD.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelHD.setText("1.0");
        jLabelHD.setToolTipText("double-click to reset to default");
        jLabelHD.setMaximumSize(new java.awt.Dimension(15, 14));
        jLabelHD.setMinimumSize(new java.awt.Dimension(15, 14));
        jLabelHD.setPreferredSize(new java.awt.Dimension(15, 14));
        jLabelHD.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelHDMouseClicked(evt);
            }
        });

        jScrollBarHeight.setMaximum(101);
        jScrollBarHeight.setMinimum(3);
        jScrollBarHeight.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        jScrollBarHeight.setValue(10);
        jScrollBarHeight.setVisibleAmount(1);
        jScrollBarHeight.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                jScrollBarHeightAdjustmentValueChanged(evt);
            }
        });
        jScrollBarHeight.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jScrollBarHeightFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jScrollBarHeightFocusLost(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabelNbrPText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelPointsNbr, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelHD, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollBarHeight, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                    .addComponent(jScrollBarNbrPoints, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabelNbrPText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelPointsNbr))
                    .addComponent(jScrollBarNbrPoints, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabelHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelHD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollBarHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonDoIt, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(121, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonDoIt)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanelParametersLayout = new javax.swing.GroupLayout(jPanelParameters);
        jPanelParameters.setLayout(jPanelParametersLayout);
        jPanelParametersLayout.setHorizontalGroup(
            jPanelParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelParametersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanelFiles, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelParametersLayout.createSequentialGroup()
                        .addGroup(jPanelParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelActC, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 21, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanelParametersLayout.setVerticalGroup(
            jPanelParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelParametersLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelFiles, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanelActC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(49, Short.MAX_VALUE))
        );

        jTabbedPane.addTab("<html><u>P</u>arameters</html>", jPanelParameters);

        jScrollPaneMessg.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPaneMessg.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPaneMessg.setFont(new java.awt.Font("Monospaced", 0, 13)); // NOI18N

        jTextAreaA.setBackground(new java.awt.Color(255, 255, 204));
        jTextAreaA.setText("Use the PrintStreams \"err\" and \"out\" to\nsend messages to this pane, for example;\n   out.println(\"message\");\n   err.println(\"Error\");\netc.\nSystem.out and System.err will send\noutput to the console, which might\nnot be available to the user.");
        jTextAreaA.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextAreaAKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextAreaAKeyTyped(evt);
            }
        });
        jScrollPaneMessg.setViewportView(jTextAreaA);

        jTabbedPane.addTab("Messages", jScrollPaneMessg);

        jPanelDiagram.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanelDiagramLayout = new javax.swing.GroupLayout(jPanelDiagram);
        jPanelDiagram.setLayout(jPanelDiagramLayout);
        jPanelDiagramLayout.setHorizontalGroup(
            jPanelDiagramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 594, Short.MAX_VALUE)
        );
        jPanelDiagramLayout.setVerticalGroup(
            jPanelDiagramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 370, Short.MAX_VALUE)
        );

        jTabbedPane.addTab("Diagram", jPanelDiagram);

        jPanelStatusBar.setBackground(new java.awt.Color(204, 255, 255));
        jPanelStatusBar.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabelStatus.setText("# # # #");

        jLabelProgress.setText("text");

        javax.swing.GroupLayout jPanelStatusBarLayout = new javax.swing.GroupLayout(jPanelStatusBar);
        jPanelStatusBar.setLayout(jPanelStatusBarLayout);
        jPanelStatusBarLayout.setHorizontalGroup(
            jPanelStatusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStatusBarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelStatus)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelProgress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelStatusBarLayout.setVerticalGroup(
            jPanelStatusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStatusBarLayout.createSequentialGroup()
                .addGroup(jPanelStatusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelProgress)
                    .addComponent(jLabelStatus))
                .addContainerGap())
        );

        jMenuFile.setMnemonic('F');
        jMenuFile.setText("File");

        jMenuFileOpen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.ALT_MASK));
        jMenuFileOpen.setMnemonic('O');
        jMenuFileOpen.setText("Open input file");
        jMenuFileOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuFileOpenActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuFileOpen);

        jMenuFileMakeD.setMnemonic('D');
        jMenuFileMakeD.setText("make the Diagram");
        jMenuFileMakeD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuFileMakeDActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuFileMakeD);

        jMenuFileXit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK));
        jMenuFileXit.setMnemonic('x');
        jMenuFileXit.setText("Exit");
        jMenuFileXit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuFileXitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuFileXit);

        jMenuBar.add(jMenuFile);

        jMenuDebug.setMnemonic('b');
        jMenuDebug.setText("debug");

        jCheckBoxMenuSEDdebug.setMnemonic('V');
        jCheckBoxMenuSEDdebug.setText("Verbose");
        jCheckBoxMenuSEDdebug.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuSEDdebugActionPerformed(evt);
            }
        });
        jMenuDebug.add(jCheckBoxMenuSEDdebug);

        jMenuSave.setMnemonic('v');
        jMenuSave.setText("save messages to file");
        jMenuSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuSaveActionPerformed(evt);
            }
        });
        jMenuDebug.add(jMenuSave);

        jMenuHF_dbg.setMnemonic('H');
        jMenuHF_dbg.setText("debugging in HaltaFall...");
        jMenuHF_dbg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuHF_dbgActionPerformed(evt);
            }
        });
        jMenuDebug.add(jMenuHF_dbg);

        jMenuCancel.setMnemonic('S');
        jMenuCancel.setText("STOP the Calculations (Alt-S)");
        jMenuCancel.setEnabled(false);
        jMenuCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuCancelActionPerformed(evt);
            }
        });
        jMenuDebug.add(jMenuCancel);

        jMenuBar.add(jMenuDebug);

        jMenuHelp.setMnemonic('H');
        jMenuHelp.setText("Help");

        jMenuHelpAbout.setMnemonic('a');
        jMenuHelpAbout.setText("About");
        jMenuHelpAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuHelpAboutActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuHelpAbout);

        jMenuBar.add(jMenuHelp);

        setJMenuBar(jMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelStatusBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jTabbedPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jTabbedPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelStatusBar, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

//<editor-fold defaultstate="collapsed" desc="Events">
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        end_program();
    }//GEN-LAST:event_formWindowClosing

    private void jMenuHelpAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuHelpAboutActionPerformed
        jMenuHelpAbout.setEnabled(false);
        Thread hlp = new Thread() {@Override public void run(){
            helpAboutFrame = new HelpAboutF(VERS, pathApp, out);
            helpAboutFrame.setVisible(true);
            helpAboutFrame.waitFor();
            javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
                helpAboutFrame = null;
                jMenuHelpAbout.setEnabled(true);
            }}); //invokeLater(Runnable)
        }};//new Thread
        hlp.start();
    }//GEN-LAST:event_jMenuHelpAboutActionPerformed

    private void jMenuFileXitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuFileXitActionPerformed
        end_program();
    }//GEN-LAST:event_jMenuFileXitActionPerformed

    private void jMenuFileOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuFileOpenActionPerformed
        eraseTextArea = true;
        getTheInputFileName();
        jTabbedPane.requestFocusInWindow();
    }//GEN-LAST:event_jMenuFileOpenActionPerformed

    private void jMenuFileMakeDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuFileMakeDActionPerformed
        jButtonDoIt.doClick();
    }//GEN-LAST:event_jMenuFileMakeDActionPerformed

    private void jCheckActCoeffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckActCoeffActionPerformed
        if(jCheckActCoeff.isSelected()) {
            calcActCoeffs = true;
            showActivityCoefficientControls(true);
            ionicStrength = readIonStrength();
        }
        else {
            calcActCoeffs = false;
            showActivityCoefficientControls(false);
        }
    }//GEN-LAST:event_jCheckActCoeffActionPerformed

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        int width = originalSize.width;
        int height = originalSize.height;
        if(this.getHeight()<height){this.setSize(this.getWidth(), height);}
        if(this.getWidth()<width){this.setSize(width,this.getHeight());}
        if(jTabbedPane.getWidth()>this.getWidth()) {
            jTabbedPane.setSize(this.getWidth(), jTabbedPane.getWidth());
        }
    }//GEN-LAST:event_formComponentResized

    private void jTextAreaAKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextAreaAKeyTyped
        evt.consume();
    }//GEN-LAST:event_jTextAreaAKeyTyped

    private void jTextAreaAKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextAreaAKeyPressed
        if(!Util.isKeyPressedOK(evt)) {evt.consume();}
    }//GEN-LAST:event_jTextAreaAKeyPressed

    private void jScrollBarNbrPointsAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_jScrollBarNbrPointsAdjustmentValueChanged
        jLabelPointsNbr.setText(String.valueOf(jScrollBarNbrPoints.getValue()).trim());
    }//GEN-LAST:event_jScrollBarNbrPointsAdjustmentValueChanged

    private void jScrollBarNbrPointsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScrollBarNbrPointsFocusGained
        jScrollBarNbrPoints.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0,0,0)));
    }//GEN-LAST:event_jScrollBarNbrPointsFocusGained

    private void jScrollBarNbrPointsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScrollBarNbrPointsFocusLost
        jScrollBarNbrPoints.setBorder(null);
    }//GEN-LAST:event_jScrollBarNbrPointsFocusLost

    private void jScrollBarHeightAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_jScrollBarHeightAdjustmentValueChanged
        jLabelHD.setText(String.valueOf((float)jScrollBarHeight.getValue()/10f).trim());
    }//GEN-LAST:event_jScrollBarHeightAdjustmentValueChanged

    private void jScrollBarHeightFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScrollBarHeightFocusGained
        jScrollBarHeight.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0,0,0)));
    }//GEN-LAST:event_jScrollBarHeightFocusGained

    private void jScrollBarHeightFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScrollBarHeightFocusLost
        jScrollBarHeight.setBorder(null);
    }//GEN-LAST:event_jScrollBarHeightFocusLost

    private void jTextFieldIonicStgrKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldIonicStgrKeyTyped
        char key = evt.getKeyChar();
        if(!isCharOKforNumberInput(key)) {evt.consume();}
    }//GEN-LAST:event_jTextFieldIonicStgrKeyTyped

    private void jTextFieldIonicStgrFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldIonicStgrFocusLost
        validateIonicStrength();
    }//GEN-LAST:event_jTextFieldIonicStgrFocusLost

    private void jTextFieldIonicStgrKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldIonicStgrKeyPressed
        if(evt.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER) {
            validateIonicStrength();
        }
    }//GEN-LAST:event_jTextFieldIonicStgrKeyPressed

    private void jTextFieldDataFileKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldDataFileKeyTyped
        char c = Character.toUpperCase(evt.getKeyChar());
        if(evt.getKeyChar() != java.awt.event.KeyEvent.VK_ESCAPE &&
           !(evt.isAltDown() &&
                ((c == 'F') ||
                 (c == 'I') || (c == 'X') ||
                 (c == 'H') || (c == 'A') ||
                 (c == 'N') || (c == 'D') ||
                 (c == 'R') || (c == 'O') ||
                 (c == 'T') ||
                 (c == 'A') || (c == 'S'))
                 ) //isAltDown
                 )
            {
                evt.consume(); // remove the typed key
                getTheInputFileName();
            }
    }//GEN-LAST:event_jTextFieldDataFileKeyTyped

    private void jTextFieldDataFileKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldDataFileKeyPressed
        if(!Util.isKeyPressedOK(evt)) {evt.consume();}
    }//GEN-LAST:event_jTextFieldDataFileKeyPressed

    private void jCheckReverseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckReverseActionPerformed
        reversedConcs = jCheckReverse.isSelected();
    }//GEN-LAST:event_jCheckReverseActionPerformed

    private void jCheckTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckTableActionPerformed
        tableOutput = jCheckTable.isSelected();
    }//GEN-LAST:event_jCheckTableActionPerformed

    private void jButtonDoItActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDoItActionPerformed
        // If no plot-file name is given in the command line but
        //      a data-file name is given: set a default plot-file name
        if(outputPltFile == null) {
            String dir = pathDef.toString();
            if(dir.endsWith(SLASH)) {dir = dir.substring(0,dir.length()-1);}
            outputPltFile = new java.io.File(dir + SLASH + jTextFieldPltFile.getText());
        }
        // note: as the calculations are done on a worker thread, this returns pretty quickly
        doCalculations();
        // statements here are performed almost inmediately
    }//GEN-LAST:event_jButtonDoItActionPerformed

    private void jTextFieldDataFileMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextFieldDataFileMouseClicked
        eraseTextArea = true;
        getTheInputFileName();
    }//GEN-LAST:event_jTextFieldDataFileMouseClicked

    private void jTextFieldIonicStgrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldIonicStgrActionPerformed
        validateIonicStrength();
    }//GEN-LAST:event_jTextFieldIonicStgrActionPerformed

    private void jTextFieldIonicStgrFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldIonicStgrFocusGained
        jTextFieldIonicStgr.selectAll();
    }//GEN-LAST:event_jTextFieldIonicStgrFocusGained

    private void jCheckBoxMenuSEDdebugActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuSEDdebugActionPerformed
        dbg = jCheckBoxMenuSEDdebug.isSelected();
    }//GEN-LAST:event_jCheckBoxMenuSEDdebugActionPerformed

    private void jComboBoxTolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTolActionPerformed
        int i = jComboBoxTol.getSelectedIndex();
        tolHalta = 0.01/Math.pow(10,i);
    }//GEN-LAST:event_jComboBoxTolActionPerformed

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        if(helpAboutFrame != null) {helpAboutFrame.bringToFront();}

    }//GEN-LAST:event_formWindowGainedFocus

    private void jLabelPointsNbrMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelPointsNbrMouseClicked
        if(evt.getClickCount() >1) {  // double-click
            jScrollBarNbrPoints.setValue(NSTP_DEF);
        }
    }//GEN-LAST:event_jLabelPointsNbrMouseClicked

private void jLabelHDMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelHDMouseClicked
    if(evt.getClickCount() >1) {  // double-click
        jScrollBarHeight.setValue(10);
    }
}//GEN-LAST:event_jLabelHDMouseClicked

    private void jMenuHF_dbgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuHF_dbgActionPerformed
        Debugging debugging = new Debugging(this,true, this, err);
        debugging.setVisible(true);
    }//GEN-LAST:event_jMenuHF_dbgActionPerformed

    private void jMenuSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuSaveActionPerformed
        jTextFieldDataFile.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        setCursorWait();
        if(pathDef == null) {setPathDef();}
        String txtfn = Util.getSaveFileName(this, progName, "Choose an output file name:", 7, "messages.txt", pathDef.toString());
        jTextFieldDataFile.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setCursorDef();
        if(txtfn == null || txtfn.trim().equals("")) {return;}
        java.io.File outputTxtFile = new java.io.File(txtfn);
        java.io.Writer w = null;
        try {
            w = new java.io.BufferedWriter(
                    new java.io.OutputStreamWriter(
                            new java.io.FileOutputStream(outputTxtFile),"UTF8"));
            w.write(jTextAreaA.getText());
            w.flush();
            w.close();
            javax.swing.JOptionPane.showMessageDialog(this, "File:"+nl+"    "+outputTxtFile.toString()+nl+"has been written.",
                    progName,javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
        catch (Exception ex) {showErrMsgBx(ex.toString(),1);}
        finally {
            try{if(w != null) {w.flush(); w.close();}}
            catch (Exception ex) {showErrMsgBx(ex.toString(),1);}
        }
        jTabbedPane.setSelectedComponent(jScrollPaneMessg);
        jTabbedPane.requestFocusInWindow();
    }//GEN-LAST:event_jMenuSaveActionPerformed

    private void jMenuCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuCancelActionPerformed
        quitConfirm(this);
    }//GEN-LAST:event_jMenuCancelActionPerformed
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="checkInput">
/** Make checks and make changes to the data stored in the Chem classes.
 * @return true if checks are OK  */
  private boolean checkInput() {
    if(cs == null) {err.println("? Programming error in \"SED.checkInput\": cs=null."); return false;}
    if(dbg) {out.println("--- checkInput()");}
    // -------------------
    //   CHEMICAL SYSTEM
    // -------------------
    // mg = total number of soluble species in the aqueous solution:
    //      all components + soluble complexes
    // Note that in HaltaFall the solid components are fictitious soluble
    // components with "zero" concentration (with noll = true)
    int mg = cs.Ms - cs.mSol; // = na + nx;
    int i;

    // ---- Remove asterisk "*" from the name of components
    for(i=0; i<cs.Na; i++) {
        if(namn.identC[i].startsWith("*")) {
            namn.identC[i] = namn.identC[i].substring(1);
            namn.ident[i]  = namn.identC[i];
            cs.noll[i] = true;
        }
        //if(chem_syst.Chem.isWater(csNamn.identC[i]))
        //            {water = i;}
    } // for i=0...Na-1

    // ---- Remove reaction products (soluble or solid) with
    //      name starting with "*".
    // Note that the solids corresponding to the components will
    // not have a name starting with "*". This is already removed when
    // reading the input file.
    double w; int j; int js;
    i = cs.Na;
    while (i < cs.Ms) {
        if(namn.ident[i].startsWith("*")) {
            if(i < mg) {mg--; cs.nx = cs.nx-1;} else {cs.mSol = cs.mSol -1;}
            cs.Ms = cs.Ms -1;
            if(i >= cs.Ms) {break;}
            for(j=i; j<cs.Ms; j++) {
                js = j - cs.Na;
                cs.lBeta[js] = cs.lBeta[js+1];
                //for(int ia=0; ia<cs.Na; ia++) {cs.a[js][ia] = cs.a[js+1][ia];}
                System.arraycopy(cs.a[js+1], 0, cs.a[js], 0, cs.Na);
                namn.ident[j] = namn.ident[j+1];
                cs.noll[j] = cs.noll[j+1];
            }
        } else {i++;}
    } //while (true)

    // ---- get electric charge, length of name, etc
    diag.aquSystem = false;
    for(i=0; i<cs.Ms; i++) {
        namn.nameLength[i] = getNameLength(namn.ident[i]);
        // Species for which the concentration is not to be
        // included in the Mass-Balance (for ex. the concentration
        // of "free" electrons is excluded)
        if(Util.isElectron(namn.ident[i]) || Util.isWater(namn.ident[i])) {
                cs.noll[i] = true;
                diag.aquSystem = true;
        }
        if(i < mg) { //aqueous species
            namn.z[i]=0;
            csC.logf[i] = 0;
            if(namn.ident[i].length() >4 &&
                namn.ident[i].toUpperCase().endsWith("(AQ)")) {
                    diag.aquSystem = true;}
                else { //does not end with "(aq)"
                    namn.z[i] = Util.chargeOf(namn.ident[i]);
                    if(namn.z[i] != 0) {diag.aquSystem = true;}
                } // ends with "(aq)"?
        }
    } // for i=0...Ms-1
    // The electric charge of two fictive species (Na+ and Cl-)
    // that are used to ensure electrically neutral aqueous solutions
    // when calculating the ionic strength and activity coefficients
    namn.z[mg] = 1;     //electroneutrality "Na+"
    namn.z[mg+1] =-1;   //electroneutrality "Na+"

    // ---- set Gaseous species to have zero conc
    //      if it is an aqueous system
    if(diag.aquSystem) {
        for(i =0; i < mg; i++) {
            if(Util.isGas(namn.ident[i])
                    || Util.isLiquid(namn.ident[i])
                    || Util.isWater(namn.ident[i])) {cs.noll[i] = true;}
        } //for i
    }

    // ---- Remove any reaction product (complex) named "H2O", if found
    if(diag.aquSystem) {
      for(i=cs.Na; i<cs.Ms; i++) {
        if(Util.isWater(namn.ident[i])) {
            if(i < mg) {mg--; cs.nx = cs.nx-1;} else {cs.mSol = cs.mSol -1;}
            cs.Ms = cs.Ms -1;
            if(i >= cs.Ms) {break;}
            for(j=i; j<cs.Ms; j++) {
                js = j - cs.Na;
                cs.lBeta[js] = cs.lBeta[js+1];
                //for(int ia=0; ia<cs.Na; ia++) {cs.a[js][ia] = cs.a[js+1][ia];}
                System.arraycopy(cs.a[js+1], 0, cs.a[js], 0, cs.Na);
                namn.ident[j] = namn.ident[j+1];
                cs.noll[j] = cs.noll[j+1];
            }
        } //ident[i]="H2O"
      } //for i
    } //if aquSystem

    if(dbg) {cs.printChemSystem(out);}

    // ---- Check that all reactions are charge balanced
    if(calcActCoeffs) {
        double zSum;
        boolean ok = true;
        for(i=cs.Na; i < mg; i++) {
            int ix = i - cs.Na;
            zSum = (double)(-namn.z[i]);
            for(j=0; j < cs.Na; j++) {
                zSum = zSum + cs.a[ix][j]*(double)namn.z[j];
            } //for j
            if(Math.abs(zSum) > 0.0005) {
                ok = false;
                err.format(engl,"--- Warning: %s, z=%3d, charge imbalance:%9.4f",
                            namn.ident[i],namn.z[i],zSum);
            }
        } //for i
        if(!ok) {
            if(!showErrMsgBxCancel("There are charge imbalanced reactions in the input file.",1)) {
                return false;
            }
        }
    } // if calcActCoeffs

    // ---- Check that at least there is one fuid species active
    boolean foundOne = false;
    for(i =0; i < mg; i++) {
        if(!cs.noll[i]) {foundOne = true; break;}
    } //for i
    if(!foundOne) {
        String t = "Error: There are no fluid species active ";
        if(cs.mSol > 0) {t = t.concat("(Only solids)");}
        t = t+nl+"This program can not handle such chemical systems.";
        showErrMsgBx(t, 1);
        return false;
    }

    // --------------------
    //   PLOT INFORMATION
    // --------------------
    diag.pInX =0; diag.pInY = 0;
    //  pInX=0 "normal" X-axis
    //  pInX=1 pH in X-axis
    //  pInX=2 pe in X-axis
    //  pInX=3 Eh in X-axis
    if(Util.isElectron(namn.identC[diag.compX])) {
        if(diag.Eh) {diag.pInX = 3;} else {diag.pInX = 2;}
    } else if(Util.isProton(namn.identC[diag.compX])) {diag.pInX = 1;}

    // ----------------------------------------------
    //   CHECK THE CONCENTRATION FOR EACH COMPONENT
    // ----------------------------------------------

    for(int ia =0; ia < cs.Na; ia++) {
        if(dgrC.hur[ia]==1 && // T
            ia==diag.compX) {
                showErrMsgBx("Error: the concentration for component \""+namn.identC[ia]+"\" "+
                    "must vary, as it belongs to the X-axis!",1);
                return false;
                }
        if(dgrC.hur[ia] >0 && dgrC.hur[ia]<=3) { //T, TV or LTV
            if(Util.isWater(namn.identC[ia])) {
                showErrMsgBx("Error: The calculations are made for 1kg H2O"+nl+
                           "Give log(H2O-activity) instead of a total conc. for water.",1);
                return false;
           } // if water
        } //if T, TV or LTV
        if(dgrC.hur[ia] ==2 || dgrC.hur[ia] ==3 || dgrC.hur[ia] ==5) { //TV, LTV or LAV
            if(ia != diag.compX) {
                String t;
                if(dgrC.hur[ia] ==5) {t="log(activity)";} else {t="total conc.";}
                String msg = "Warning: The "+t+" is varied for \""+namn.identC[ia]+"\""+nl+
                           "   but the component in the X-axis is \""+namn.identC[diag.compX]+"\"";
                if(!showErrMsgBxCancel(msg,2)) {return false;}
            }
        } //if TV, LTV or LAV
        if((dgrC.hur[ia] ==1 || dgrC.hur[ia] ==4) && //T or LA
           ia == diag.compX) {
                String t;
                if(dgrC.hur[ia] ==4) {t="log(activity)";} else {t="total conc.";}
                showErrMsgBx("Error: The "+t+" of \""+namn.identC[ia]+"\""+nl+
                        "can NOT be a fixed value because this component belongs to the X-axis !",1);
                return false;
            }
        if(dgrC.hur[ia] ==1 && // T
           Math.abs(dgrC.cLow[ia]) >100) {
                String t = String.format(engl,"Error:  For component: "+namn.identC[ia]+nl+
                        "   Tot.Conc.=%12.4g mol/kg.  This is not a reasonable value!",dgrC.cLow[ia]);
                showErrMsgBx(t,1);
                return false;
            }
        if(dgrC.hur[ia] ==4 &&  // LA
           Util.isProton(namn.identC[ia]) &&
           (dgrC.cLow[ia] <-14 || dgrC.cLow[ia] >2)) {
                String msg = String.format(engl,"Warning: In the input, you give  pH =%8.2f%n"+
                        "This value could be due to an input error.",(-dgrC.cLow[ia]));
                if(!showErrMsgBxCancel(msg,2)) {return false;}
        }
        if(dgrC.hur[ia] !=1 && dgrC.hur[ia] !=4) {//if TV, LTV or LAV
            if(dgrC.cLow[ia] == dgrC.cHigh[ia] ||
                    Math.max(Math.abs(dgrC.cLow[ia]), Math.abs(dgrC.cHigh[ia])) < 1e-15) {
                showErrMsgBx("Error:  Min-value = Max-value for component \""+namn.identC[ia]+"\"",1);
                return false;
            }
            if(dgrC.cLow[ia] > dgrC.cHigh[ia] && !reversedConcs) {
                w = dgrC.cLow[ia];
                dgrC.cLow[ia] = dgrC.cHigh[ia];
                dgrC.cHigh[ia] = w;
            }
            if(!reversedConcs && dgrC.hur[ia] ==5 && // pH/pe/EH varied - LAV
                   (Util.isProton(namn.identC[ia]) ||
                    Util.isElectron(namn.identC[ia]))) {
                w = dgrC.cLow[ia];
                dgrC.cLow[ia] = dgrC.cHigh[ia];
                dgrC.cHigh[ia] = w;
            }
            if(dgrC.hur[ia] ==5 && // LAV
                    (Util.isProton(namn.identC[ia])) &&
                     (dgrC.cLow[ia] <-14.00001 || dgrC.cLow[ia] >2.00001 ||
                      dgrC.cHigh[ia] <-14.00001 || dgrC.cHigh[ia] >2.00001)) {
                String msg = String.format(engl,"Warning: In the input, you give  pH =%8.2f to %7.2f%n"+
                        "These values could be due to an input error.",(-dgrC.cLow[ia]),(-dgrC.cHigh[ia]));
                if(!showErrMsgBxCancel(msg,2)) {return false;}
            }
            if(dgrC.hur[ia] ==2 && // TV
                  (Math.max(Math.abs(dgrC.cHigh[ia]),Math.abs(dgrC.cLow[ia]))>100)) {
                showErrMsgBx("Error:  You give  ABS(TOT.CONC.) > 100  for component: "+namn.identC[ia]+nl+
                    "This value is too high and perhaps an input error."+nl+
                    "Set the maximum ABS(TOT.CONC.) value to 100.",1);
                return false;
            }
            if(dgrC.hur[ia] ==3) { // LTV
                if((Math.min(dgrC.cLow[ia], dgrC.cHigh[ia]) < -7.0001) &&
                      (Util.isProton(namn.identC[ia]))) {
                    String msg = "Warning: You give a  LOG (TOT.CONC.) < -7  for component: "+namn.identC[ia]+nl+
                        "This value is rather low and could be due to an input error."+nl+
                        "Maybe you meant to set  LOG (ACTIVITY) < -7 ??";
                    if(!showErrMsgBxCancel(msg,2)) {return false;}
                }
                if(Math.max(dgrC.cLow[ia], dgrC.cHigh[ia]) > 2.0001) {
                    showErrMsgBx("Error: You give a  LOG (TOT.CONC.) > 2  for component: "+namn.identC[ia]+nl+
                        "This value is too high and it could be due to an input error."+nl+
                        "Please set the LOG (TOT.CONC.) value to <=2.",1);
                    return false;
                }
            } //if LTV
        } //if TV, LTV or LAV

    } // for ia = 0... Na-1

    // ----------------
    //   OTHER CHECKS
    // ----------------

    // ---- See which components have positive or negative (or both)
    //      values for the stoichiometric coefficients (a[ix][ia]-values)
    pos = new boolean[cs.Na];
    neg = new boolean[cs.Na];
    for(i =0; i < cs.Na; i++) {
        pos[i] = false; neg[i] = false;
        if(!cs.noll[i]) { // if not "e-" and not solid component
                    pos[i] = true;}
        for(j = cs.Na; j < cs.Ms; j++) {
            if(!cs.noll[j]) {
                if(cs.a[j-cs.Na][i] >0) {pos[i] = true;}
                if(cs.a[j-cs.Na][i] <0) {neg[i] = true;}
            } // !noll
        } //for j
    } //for i
    // check POS and NEG with the Tot.Conc. given in the input
    for(i =0; i < cs.Na; i++) {
        if(csC.kh[i] == 2) {continue;} //only it Tot.conc. is given
        if((!pos[i] && !neg[i]) ) { // || cs.nx ==0
            String msg = "Error: for component \""+namn.identC[i]+"\" give Log(Activity)";
            if(dgrC.hur[i] !=1) { // not "T", that is: "TV" or "LTV"
                msg = msg+" to vary";}
            showErrMsgBx(msg,1);
            return false;
        } //if Nx =0 or (!pos[] & !neg[])
        if((pos[i] && neg[i]) ||
           (pos[i] && (dgrC.hur[i] ==3 ||  //LTV
                (dgrC.cLow[i]>0 && (Double.isNaN(dgrC.cHigh[i]) || dgrC.cHigh[i]>0)))) ||
           (neg[i] && (dgrC.hur[i] !=3 && //not LTV
                (dgrC.cLow[i]<0 && (Double.isNaN(dgrC.cHigh[i]) || dgrC.cHigh[i]<0))))) {
            continue;
        }
        if(pos[i] || neg[i]) {
            String msg = "Error: Component \"%s\" may not have %s Tot.Conc. values.%s"+
                    "Give either  Tot.Conc. %s=0.0  or  Log(Activity)%s";
            if(neg[i] && (dgrC.hur[i] ==3 || // LTV
                    dgrC.cLow[i]>0 || (!Double.isNaN(dgrC.cHigh[i]) && dgrC.cHigh[i]>0))) {
                showErrMsgBx(String.format(msg, namn.identC[i], "positive", nl,"<",nl),1);
                return false;}
            if(pos[i] && (dgrC.hur[i] !=3 &&  //not LTV
                    (dgrC.cLow[i]<0 || (!Double.isNaN(dgrC.cHigh[i]) && dgrC.cHigh[i]<0)))) {
                showErrMsgBx(String.format(msg, namn.identC[i], "negative", nl,">",nl),1);
                return false;
            }
        } //if pos or neg
    } //for i

    // OK so far. Update "nx" (=nbr of soluble complexes)
    cs.nx = mg - cs.Na;
    return true;

  } // checkInput()

//<editor-fold defaultstate="collapsed" desc="getNameLength(species)">
    private static int getNameLength(String species) {
        int nameL = Math.max(1, Util.rTrim(species).length());
        if(nameL < 3) {return nameL;}
        // Correct name length if there is a space between name and charge
        // "H +",  "S 2-",  "Q 23+"
        int sign; int ik;
        sign =-1;
        for(ik =nameL-1; ik >= 2; ik--) {
            char c = species.charAt(ik);
            if(c == '+' || c == '-' ||
               // unicode en dash or unicode minus
               c =='\u2013' || c =='\u2212') {sign = ik; break;}
            } //for ik
        if(sign <2) {return nameL;}
        if(sign < nameL-1 &&
                (Character.isLetterOrDigit(species.charAt(sign+1)))) {return nameL;}
        if(species.charAt(sign-1) == ' ')
                        {nameL = nameL-1; return nameL;}
        if(nameL >=4) {
                if(species.charAt(sign-1) >= '2' && species.charAt(sign-1) <= '9' &&
                   species.charAt(sign-2) == ' ')
                        {nameL = nameL-1; return nameL;}
        } //if nameL >=4
        if(nameL >=5) {
                if((species.charAt(sign-1) >= '0' && species.charAt(sign-1) <= '9') &&
                   (species.charAt(sign-2) >= '1' && species.charAt(sign-2) <= '9') &&
                   species.charAt(sign-3) == ' ')
                        {nameL = nameL-1;}
            } //if nameL >=5
        return nameL;
    } // getNameLength(species)
//</editor-fold>

//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="dispatchArg">
/**
 * @param arg String containing a command-line argument
 * @return false if there was an error associated with the command argument
 */
private boolean dispatchArg(String arg) {
  if(arg == null) {return true;}
  out.println("Command-line argument = \""+arg+"\"");
  if(arg.equals("-?") || arg.equals("/?") || arg.equals("?")) {
      out.println("Usage:   SED  [data-file-name]  [-command=value]");
      printInstructions(out);
      if(this.isVisible()) {jTabbedPane.setSelectedComponent(jScrollPaneMessg);}
      return true;} //if args[] = "?"

  String msg = null;
  while(true) {
    if(arg.length() >3) {
        String arg0 = arg.substring(0, 2).toLowerCase();
        if(arg0.startsWith("-d") || arg0.startsWith("/d")) {
            if(arg.charAt(2) == '=' || arg.charAt(2) == ':') {
                String f = arg.substring(3);
                if(f.startsWith("\"") && f.endsWith("\"")) { //remove enclosing quotes
                    f = f.substring(1, f.length()-1);
                }
                if(!f.toLowerCase().endsWith(".dat")) {f = f.concat(".dat");}
                inputDataFile = new java.io.File(f);
                setPathDef(inputDataFile);
                //get the complete file name
                String fil;
                try{fil = inputDataFile.getCanonicalPath();}
                catch (java.io.IOException e) {
                    try{fil = inputDataFile.getAbsolutePath();}
                    catch (Exception e1) {fil = inputDataFile.getPath();}
                }
                inputDataFile = new java.io.File(fil);
                if(dbg){out.println("Data file: "+inputDataFile.getAbsolutePath());}
                if(!doNotExit) {consoleOutput = true;}
                if(!readDataFile(inputDataFile)) {
                    //-- error reading data file
                    if(this.isVisible()) {jTabbedPane.setSelectedComponent(jScrollPaneMessg);}
                    inputDataFileInCommandLine = false;
                    return false;
                }
                else {showTheInputFileName(inputDataFile);}
                inputDataFileInCommandLine = true;
                return true; // no error
            }// = or :
        } else if(arg0.startsWith("-p") || arg0.startsWith("/p")) {
            if(arg.charAt(2) == '=' || arg.charAt(2) == ':') {
                String f = arg.substring(3);
                if(f.startsWith("\"") && f.endsWith("\"")) { //remove enclosing quotes
                    f = f.substring(1, f.length()-1);
                }
                if(!f.toLowerCase().endsWith(".plt")) {f = f.concat(".plt");}
                outputPltFile = new java.io.File(f);
                if(dbg){out.println("Plot file: "+outputPltFile.getAbsolutePath());}
                jTextFieldPltFile.setText(outputPltFile.getName());
                return true;
            }// = or :
        } else if(arg0.startsWith("-i") || arg0.startsWith("/i")) {
            if(arg.charAt(2) == '=' || arg.charAt(2) == ':') {
                String t = arg.substring(3);
            try {ionicStrength = Double.parseDouble(t);
                ionicStrength = Math.max(-1,Math.min(1000,ionicStrength));
                if(ionicStrength < 0) {ionicStrength = -1;}
                jTextFieldIonicStgr.setText(String.valueOf(ionicStrength));
                if(Math.abs(ionicStrength) > 1e-10) {calcActCoeffs = true;}
                if(dbg) {out.println("Ionic strength = "+ionicStrength);}
                return true;
            } //try
            catch (NumberFormatException nfe) {
              msg = "Wrong numeric format for ionic strength in text \""+t+"\"";
              ionicStrength = 0;
              jTextFieldIonicStgr.setText(String.valueOf(ionicStrength));
              break;
            } //catch
            }// = or :
        } else if(arg0.startsWith("-t") || arg0.startsWith("/t")) {
            if(arg.charAt(2) == '=' || arg.charAt(2) == ':') {
                String t = arg.substring(3);
                try {temperature_InCommandLine = Double.parseDouble(t);
                    temperature_InCommandLine = Math.min(1000,Math.max(temperature_InCommandLine,-10));
                    jLabelTemperature.setText(String.valueOf(temperature_InCommandLine));
                    if(dbg) {out.println("Temperature = "+temperature_InCommandLine);}
                    return true;
                    } //try
                catch (NumberFormatException nfe) {
                  msg = "Error: Wrong numeric format for temperature in text \""+t+"\"";
                  temperature_InCommandLine = Double.NaN;
                  break;
                } //catch
            }// = or :
        } else if(arg0.startsWith("-h") || arg0.startsWith("/h")) {
            if(arg.charAt(2) == '=' || arg.charAt(2) == ':') {
                String t = arg.substring(3);
                try {tHeight = Double.parseDouble(t);
                    tHeight = Math.min(10,Math.max(tHeight, 0.3));
                    jScrollBarHeight.setValue(Math.round((float)(10*tHeight)));
                    if(dbg) {out.println("Height factor for texts in diagrams = "+tHeight);}
                    return true;
                    } //try
                catch (NumberFormatException nfe) {
                msg = "Wrong numeric format for text height in \""+t+"\"";
                tHeight =1;
                jScrollBarHeight.setValue(Math.round((float)(10*tHeight)));
                break;
                } //catch
            }// = or :
        } // if starts with "-h"
        if(arg0.startsWith("-m") || arg0.startsWith("/m")) {
            if(arg.charAt(2) == '=' || arg.charAt(2) == ':') {
                String t = arg.substring(3);
                try {actCoeffsModel_InCommandLine = Integer.parseInt(t);
                    actCoeffsModel_InCommandLine = Math.min(jComboBoxModel.getItemCount()-1,
                            Math.max(0,actCoeffsModel_InCommandLine));
                    jComboBoxModel.setSelectedIndex(actCoeffsModel_InCommandLine);
                    if(dbg) {out.println("Activity coeffs. method = "+actCoeffsModel_InCommandLine);}
                    return true;
                    } //try
                catch (NumberFormatException nfe) {
                msg = "Wrong numeric format for activity coeff. model in \""+t+"\"";
                actCoeffsModel_InCommandLine = actCoeffsModelDefault;
                jComboBoxModel.setSelectedIndex(actCoeffsModelDefault);
                break;
                } //catch
            }// = or :
        } else if(arg0.startsWith("-n") || arg0.startsWith("/n")) {
            if(arg.charAt(2) == '=' || arg.charAt(2) == ':') {
                String t = arg.substring(3);
                try{nSteps = Integer.parseInt(t);
                    nSteps = Math.min(NSTP_MAX, nSteps);
                    if(!dbg) {nSteps = Math.max(nSteps, NSTP_MIN);}
                    jScrollBarNbrPoints.setValue(nSteps);
                    if(dbg) {out.println("Nbr calc. steps in diagram = "+(nSteps)+" (nbr. points = "+(nSteps+1)+")");}
                    return true;
                } catch (NumberFormatException nfe) {
                    msg = "Wrong numeric format for number of calculation steps in \""+t+"\"";
                    nSteps = NSTP_DEF;
                    jScrollBarNbrPoints.setValue(nSteps);
                    break;
                }
            }// = or :
        } // if starts with "-n"
    } // if length >3

    if(arg.length() >4) {
        String arg0 = arg.substring(0, 4).toLowerCase();
        if(arg0.startsWith("-pr") || arg0.startsWith("/pr")) {
            if(arg.charAt(4) == '=' || arg.charAt(4) == ':') {
                String t = arg.substring(4);
                try {pressure_InCommandLine = Double.parseDouble(t);
                    pressure_InCommandLine = Math.min(10000,Math.max(pressure_InCommandLine,1));
                    jLabelPressure.setText(String.valueOf(pressure_InCommandLine));
                    if(dbg) {out.println("Pressure = "+pressure_InCommandLine);}
                    return true;
                    } //try
                catch (NumberFormatException nfe) {
                  msg = "Error: Wrong numeric format for pressure in text \""+t+"\"";
                  pressure_InCommandLine = Double.NaN;
                  break;
                } //catch
            }// = or :
        } // if starts with "-tol"
    } // if length >4

    if(arg.length() >5) {
        String arg0 = arg.substring(0, 5).toLowerCase();
        if(arg0.startsWith("-tol") || arg0.startsWith("/tol")) {
            if(arg.charAt(4) == '=' || arg.charAt(4) == ':') {
                String t = arg.substring(5);
                double w;
                try {w = Double.parseDouble(t);
                    tolHalta = Math.min(1e-2,Math.max(w, 1e-9));
                    set_tol_inComboBox();
                    if(dbg) {out.println("Max tolerance in HaltaFall = "+tolHalta);}
                    return true;
                    } //try
                catch (NumberFormatException nfe) {
                msg = "Wrong numeric format for tolerance in \""+t+"\"";
                tolHalta = Chem.TOL_HALTA_DEF;
                set_tol_inComboBox();
                break;
                } //catch
            }// = or :
        } // if starts with "-tol"
        if(arg0.startsWith("-thr") || arg0.startsWith("/thr")) {
            if(arg.charAt(4) == '=' || arg.charAt(4) == ':') {
                String t = arg.substring(5);
                try {threshold = Float.parseFloat(t);
                    threshold = Math.min(0.1f,Math.max(threshold, 0.001f));
                    if(dbg) {out.println("Threshold for fraction diagrams = "+threshold);}
                    return true;
                    } //try
                catch (NumberFormatException nfe) {
                msg = "Wrong numeric format for fraction threshold in \""+t+"\"";
                threshold =0.03f;
                break;
                } //catch
            }// = or :
        } // if starts with "-thr"
    }

    if(arg.length() >=6) {
        String arg0 = arg.substring(0, 5).toLowerCase();
        if(arg0.startsWith("-tble") || arg0.startsWith("/tble")) {
            if(arg.charAt(5) == '=' || arg.charAt(5) == ':') {
                String t = arg.substring(6);
                if(t.length() <=0) {t = "";}
                tblExtension = t;
                tableOutput = true;
                if(dbg) {out.println("Table output: file name extension = \""+t+"\"");}
                return true;
            }// = or :
        } else if(arg0.startsWith("-tbls") || arg0.startsWith("/tbls")) {
            if(arg.charAt(5) == '=' || arg.charAt(5) == ':') {
                String t = arg.substring(6);
                if(t.length() <=0) {t = " ";}
                tblFieldSeparator = t;
                if(tblFieldSeparator.equalsIgnoreCase("\\t"))
                    {tblFieldSeparator = "\u0009";}
                tableOutput = true;
                if(dbg) {out.println("Table output: field separator = \""+t+"\"");}
                return true;
            }// = or :
        } // if starts with "-tbls"
    }
    if(arg.length() >=7) {
        String arg0 = arg.substring(0, 6).toLowerCase();
        if(arg0.startsWith("-tblcs") || arg0.startsWith("/tblcs")) {
            if(arg.charAt(6) == '=' || arg.charAt(6) == ':') {
                String t = arg.substring(7);
                if(t.length() <=0) {t = "";}
                tblCommentStart = t;
                tableOutput = true;
                if(dbg) {out.println("Table output: comment-line start = \""+t+"\"");}
                return true;
            }// = or :
        } else if(arg0.startsWith("-tblce") || arg0.startsWith("/tblce")) {
            if(arg.charAt(6) == '=' || arg.charAt(6) == ':') {
                String t = arg.substring(7);
                if(t.length() <=0) {t = "";}
                tblCommentEnd = t;
                tableOutput = true;
                if(dbg) {out.println("Table output: comment-line end = \""+t+"\"");}
                return true;
            }// = or :
        } // if starts with "-tbls"
    }
    if(arg.length() >6) {
        String arg0 = arg.substring(0, 5).toLowerCase();
        if(arg0.startsWith("-dbgh") || arg0.startsWith("/dbgh")) {
            if(arg.charAt(5) == '=' || arg.charAt(5) == ':') {
                String t = arg.substring(6);
                try {dbgHalta = Integer.parseInt(t);
                    dbgHalta = Math.min(6, Math.max(dbgHalta, 0));
                    //set_dbgHalta_inRadioMenus();
                    if(dbg) {out.println("Debug printout level in HaltaFall = "+dbgHalta);}
                    return true;
                    } //try
                catch (NumberFormatException nfe) {
                msg = "Wrong numeric format for HaltaFall debug level \""+t+"\" (setting default:"+Chem.DBGHALTA_DEF+")";
                dbgHalta = Chem.DBGHALTA_DEF;
                //set_dbgHalta_inRadioMenus();
                break;
                } //catch
            }// = or :
        } // if starts with "-dbgh"
    } //if length >6
    if(arg.length() >7) {
        String arg0 = arg.substring(0, 6).toLowerCase();
        if(arg0.startsWith("-units") || arg0.startsWith("/units")) {
            if(arg.charAt(6) == '=' || arg.charAt(6) == ':') {
                String t = arg.substring(7);
                try {conc_units = Integer.parseInt(t);
                    conc_units = Math.min(2, Math.max(conc_units, -1));
                    if(dbg) {out.println("Concentration units = "+conc_units+"(\""+cUnits[(conc_units+1)]+"\")");}
                    return true;
                    } //try
                catch (NumberFormatException nfe) {
                msg = "Wrong numeric format for concentration units \""+t+"\" (setting default: 0=\"molal\")";
                conc_units = 0;
                break;
                } //catch
            }// = or :
        } // if starts with "-dbgh"
    } //if length >7

    if(arg.equalsIgnoreCase("-tbl") || arg.equalsIgnoreCase("/tbl")) {
        tableOutput = true;
        if(dbg) {out.println("Table output = true");}
        return true;
    } else if(arg.equalsIgnoreCase("-dbg") || arg.equalsIgnoreCase("/dbg")) {
        dbg = true;
        jCheckBoxMenuSEDdebug.setSelected(dbg);
        out.println("Debug printout = true");
        return true;
    } else if(arg.equalsIgnoreCase("-rev") || arg.equalsIgnoreCase("/rev")) {
        reversedConcs = true;
        if(dbg) {out.println("Allow reversed ranges in axes");}
        return true;
    } else if(arg.equalsIgnoreCase("-keep") || arg.equalsIgnoreCase("/keep")) {
        out = outPrintStream; // direct messages to tabbed pane
        err = errPrintStream; // direct error messages to tabbed pane
        doNotExit = true;
        if(dbg) {out.println("Do not close window after calculations");}
        return true;
    } else if(arg.equalsIgnoreCase("-nostop") || arg.equalsIgnoreCase("/nostop")) {
        doNotStop = true;
        if(dbg) {out.println("Do not show message boxes");}
        return true;
    } else if(arg.equalsIgnoreCase("-sci") || arg.equalsIgnoreCase("/sci")) {
            conc_nottn = 1;
            if(dbg) {out.println("Display concentrations in scientific notation.");}
            return true;
    } else if(arg.equalsIgnoreCase("-eng") || arg.equalsIgnoreCase("/eng")) {
            conc_nottn = 2;
            if(dbg) {out.println("Display concentrations in engineerng notation.");}
            return true;
    }
    break;
  } //while

  if(msg == null) {msg = "Error: can not understand command-line argument:"+nl+
            "  \""+arg+"\""+nl+"For a list of possible commands type:  SED  -?";}
  else {msg = "Command-line argument \""+arg+"\":"+nl+msg;}
  out.flush();
  err.println(msg);
  err.flush();
  printInstructions(out);
  if(!doNotStop) {
      if(!this.isVisible()) {this.setVisible(true);}
      javax.swing.JOptionPane.showMessageDialog(this,msg,progName,
            javax.swing.JOptionPane.ERROR_MESSAGE);
  }
  if(this.isVisible()) {jTabbedPane.setSelectedComponent(jScrollPaneMessg);}
  return false;
} // dispatchArg(arg)
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="diverse Methods">

  //<editor-fold defaultstate="collapsed" desc="showActivityCoefficientControls">
/** show/hide the activity coefficient controls in the window */
  private void showActivityCoefficientControls(boolean show) {
    if(show) {
        jTextFieldIonicStgr.setEnabled(true);
        jLabelIonicStr.setEnabled(true);
        jLabelIonicStr.setText("<html>ionic <u>S</u>trength</html>");
        jLabelIonicStrM.setEnabled(true);
        jLabelModel.setEnabled(true);
        jLabelModel.setText("<html>activity <u>C</u>officient model:<html>");
        jComboBoxModel.setEnabled(true);
    } else {
        jLabelIonicStr.setEnabled(false);
        jLabelIonicStr.setText("ionic Strength");
        jLabelIonicStrM.setEnabled(false);
        jTextFieldIonicStgr.setEnabled(false);
        jLabelModel.setText("activity cofficient model:");
        jLabelModel.setEnabled(false);
        jComboBoxModel.setEnabled(false);
    }
  } //showActivityCoefficientControls(show)
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="disable/restore Menus">
  /** disable menus and buttons during calculations */
  private void disableMenus() {
    //if(this.isVisible()) {
      jMenuFileOpen.setEnabled(false);
      jMenuFileMakeD.setEnabled(false);
      jMenuDebug.setEnabled(true);
      jCheckBoxMenuSEDdebug.setEnabled(false);
      jMenuSave.setEnabled(false);
      jMenuHF_dbg.setEnabled(false);
      jMenuCancel.setEnabled(true);
      jTabbedPane.setSelectedComponent(jScrollPaneMessg);
      jLabelData.setText("input data file:");
      jLabelData.setEnabled(false);
      jTextFieldDataFile.setEnabled(false);
      jLabelPltFile.setText("plot file name:");
      jLabelPltFile.setEnabled(false);
      jTextFieldPltFile.setEnabled(false);

      jLabelNbrPText.setText("Nbr of calc. steps:");
      jLabelNbrPText.setEnabled(false);
      jLabelPointsNbr.setEnabled(false);
      jScrollBarNbrPoints.setEnabled(false);

      jLabelHeight.setText("height of text in diagram:");
      jLabelHeight.setEnabled(false);
      jLabelHD.setEnabled(false);
      jScrollBarHeight.setEnabled(false);

      jCheckTable.setText("table Output");
      jCheckTable.setEnabled(false);

      jCheckActCoeff.setText("Activity coefficient calculations");
      jCheckActCoeff.setEnabled(false);
      showActivityCoefficientControls(false);
      jLabelTol.setEnabled(false);
      jComboBoxTol.setEnabled(false);

      jCheckReverse.setText("allow Reversed min. and max. axes limits");
      jCheckReverse.setEnabled(false);
      jButtonDoIt.setText("make the Diagram");
      jButtonDoIt.setEnabled(false);
    //} //if visible
  } //disableMenus()
/** enable menus and buttons after the calculations are finished
 * and the diagram is displayed
 * @param allowMakeDiagram if true then the button and menu to make diagrams are enabled,
 * they are disabled otherwise */
  private void restoreMenus(boolean allowMakeDiagram) {
    //if(this.isVisible()) {
      jMenuFileOpen.setEnabled(true);
      if(allowMakeDiagram) {
        jButtonDoIt.setText("<html>make the <u>D</u>iagram</html>");
      } else {
        jButtonDoIt.setText("make the Diagram");
      }
      jButtonDoIt.setEnabled(allowMakeDiagram);
      jMenuFileMakeD.setEnabled(allowMakeDiagram);
      jMenuDebug.setEnabled(true);
      jCheckBoxMenuSEDdebug.setEnabled(true);
      jMenuSave.setEnabled(true);
      jMenuHF_dbg.setEnabled(true);
      jMenuCancel.setEnabled(false);
      jLabelData.setEnabled(true);
      jTextFieldDataFile.setEnabled(true);
      jLabelPltFile.setText("<html>p<u>l</u>ot file name:</html>");
      jLabelPltFile.setEnabled(true);
      jTextFieldPltFile.setEnabled(true);

      jLabelNbrPText.setText("<html><u>N</u>br of calc. steps:</html>");
      jLabelNbrPText.setEnabled(true);
      jLabelPointsNbr.setEnabled(true);
      jScrollBarNbrPoints.setEnabled(true);

      jLabelHeight.setText("<html>h<u>e</u>ight of text in diagram:</html>");
      jLabelHeight.setEnabled(true);
      jLabelHD.setEnabled(true);
      jScrollBarHeight.setEnabled(true);

      jCheckTable.setText("<html>table <u>O</u>utput</html>");
      jCheckTable.setEnabled(true);

      jCheckActCoeff.setText("<html><u>A</u>ctivity coefficient calculations</html>");
      jCheckActCoeff.setEnabled(true);
      showActivityCoefficientControls(jCheckActCoeff.isSelected());

      jLabelPltFile.setText("plot file name");
      jLabelPltFile.setEnabled(false);
      jTextFieldPltFile.setText(null);
      jTextFieldPltFile.setEnabled(false);
      //jTextFieldDataFile.setText("");

      jLabelTol.setEnabled(true);
      jComboBoxTol.setEnabled(true);
      jCheckReverse.setText("<html>allow <u>R</u>eversed min. and max. axes limits</html>");
      jCheckReverse.setEnabled(true);
      jLabelStatus.setText("waiting...");
      jLabelProgress.setText(" ");
    //} //if visible
  } //restoreMenus()
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="readDataFile_hadError">
/** enable controls and disable diagram if an error is found when
 * reading the input file */
  private void readDataFile_hadError() {
    out.println("--- Error(s) reading the input file ---");
    if(this.isVisible()) {
        jTabbedPane.setTitleAt(2, "Diagram");
        jTabbedPane.setEnabledAt(2, false); //disable the diagram
        restoreMenus(false);
        jTabbedPane.setSelectedComponent(jScrollPaneMessg);
    }
  } // readDataFile_hadError()
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="set_tol_inComboBox">
/** find the closest item in the tolerances combo box and select it */
  private void set_tol_inComboBox() {
    double w0, w1;
    int listItem =0;
    int listCount = jComboBoxTol.getItemCount();
    for(int i =1; i < listCount; i++) {
      w0 = Double.parseDouble(jComboBoxTol.getItemAt(i-1).toString());
      w1 = Double.parseDouble(jComboBoxTol.getItemAt(i).toString());
      if(tolHalta >= w0 && i==1) {listItem = 0; break;}
      if(tolHalta <= w1 && i==(listCount-1)) {listItem = (listCount-1); break;}
      if(tolHalta < w0 && tolHalta >=w1) {
        if(Math.abs(tolHalta-w0) < Math.abs(tolHalta-w1)) {
            listItem = i-1;
        } else {
            listItem = i;
        }
        break;
      }
    } //for i
    jComboBoxTol.setSelectedIndex(listItem);
  } //set_tol_inComboBox()
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="end_program">
  private void end_program() {
      if(dbg) {out.println("--- end_program()");}
      if(!finishedCalculations && !quitConfirm(this)) {return;}
      programEnded = true;
      this.notify_All();
      this.dispose();
      if(helpAboutFrame != null) {
          helpAboutFrame.closeWindow();
          helpAboutFrame = null;
      }
  } // end_program()
//</editor-fold>

  private synchronized void notify_All() {this.notifyAll();}

  private synchronized void synchWaitCalcs() {
      while(!finishedCalculations) {
          try {wait();} catch(InterruptedException ex) {}
      }
  }
  private synchronized void synchWaitProgramEnded() {
      while(!programEnded) {
          try {wait();} catch(InterruptedException ex) {}
      }
  }

//<editor-fold defaultstate="collapsed" desc="isCharOKforNumberInput">
  /** @param key a character
   * @return true if the character is ok, that is, it is either a number,
   * or a dot, or a minus sign, or an "E" (such as in "2.5e-6") */
  private boolean isCharOKforNumberInput(char key) {
        return Character.isDigit(key)
                || key == '-' || key == '+' || key == '.' || key == 'E' || key == 'e';
  } // isCharOKforNumberInput(char)
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="quitConfirm">
  private boolean quitConfirm(javax.swing.JFrame c) {
    boolean q = true;
    if(!doNotStop) {
      Object[] options = {"Cancel", "STOP"};
      int n = javax.swing.JOptionPane.showOptionDialog (c,
                "Do you really want to stop the calculations?",
                progName, javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.ERROR_MESSAGE, null, options, null);
      q = n == javax.swing.JOptionPane.NO_OPTION;
    } //not "do not stop":
    if(q) {
      if(h != null) {h.haltaCancel();}
      if(tsk != null) {tsk.cancel(true);}
      finishedCalculations = true;
      this.notify_All();
    }
    return q;
  } // quitConfirm(JFrame)
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="showTheInputFileName">
  /** Show the input data file name in the JFrame (window) */
  private void showTheInputFileName(java.io.File dataFile) {
    jTextFieldDataFile.setText(dataFile.getAbsolutePath());
    jLabelPltFile.setEnabled(true);
    jLabelPltFile.setText("<html>p<u>l</u>ot file name:</html>");
    jTextFieldPltFile.setEnabled(true);
    jMenuFileMakeD.setEnabled(true);
    jButtonDoIt.setEnabled(true);
    jButtonDoIt.setText("<html>make the <u>D</u>iagram</html>");
    jButtonDoIt.requestFocusInWindow();
  } // showTheInputFileName()
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="setPathDef">

  /** Sets the variable "pathDef" to the path of a file.
   * Note that "pathDef" may end with the file separator character, e.g. "D:\"
   * @param f File */
  private void setPathDef(java.io.File f) {
    if(pathDef == null) {pathDef = new StringBuffer();}
    java.net.URI uri;
    if(f != null) {
        if(!f.getAbsolutePath().contains(SLASH)) {
            // it is a bare file name, without a path
            if(pathDef.length()>0) {return;}
        }
        try{uri = f.toURI();}
        catch (Exception ex) {uri = null;}
    } else {uri = null;}
    if(pathDef.length()>0) {pathDef.delete(0, pathDef.length());}
    if(uri != null) {
        if(f != null && f.isDirectory()) {
          pathDef.append((new java.io.File(uri.getPath())).toString());
        } else {
          pathDef.append((new java.io.File(uri.getPath())).getParent().toString());
        } //directory?
    } else { //uri = null:  set Default Path = Start Directory
        java.io.File currDir = new java.io.File("");
        try {pathDef.append(currDir.getCanonicalPath());}
        catch (java.io.IOException e) {
          try{pathDef.append(System.getProperty("user.dir"));}
          catch (Exception e1) {pathDef.append(".");}
        }
    } //uri = null
  } // setPathDef(File)

  /** Set the variable "pathDef" to the path of a file name.
   * Note that "pathDef" may end with the file separator character, e.g. "D:\"
   * @param fName String with the file name */
  private void setPathDef(String fName) {
    java.io.File f = new java.io.File(fName);
    setPathDef(f);
  }

  /** Set the variable "pathDef" to the user directory ("user.home", system dependent) */
  private void setPathDef() {
    String t = System.getProperty("user.home");
    setPathDef(t);
  }

// </editor-fold>

/*
    static void pause() {
    // Defines the standard input stream
    java.io.BufferedReader stdin =
        new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
        System.out.print ("Press Enter");
        System.out.flush();
        try{String txt = stdin.readLine();}
        catch (java.io.IOException ex) {System.err.println(ex.toString());}
    }
*/
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="millisToShortDHMS">
/** converts time (in milliseconds) to human-readable format "&lt;dd&gt;hh:mm:ss"
 * @param duration (in milliseconds)
 * @return  */
  public static String millisToShortDHMS(long duration) {
    //adapted from http://www.rgagnon.com
    String res;
    int millis = (int)(duration % 1000);
    duration /= 1000;
    int seconds = (int) (duration % 60);
    duration /= 60;
    int minutes = (int) (duration % 60);
    duration /= 60;
    int hours = (int) (duration % 24);
    int days = (int) (duration / 24);
    if (days == 0) {
      res = String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds,millis);
    } else {
      res = String.format("%dd%02d:%02d:%02d", days, hours, minutes, seconds);
    }
    return res;
  }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="setCursor">
  private void setCursorWait() {
    this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
    jTextAreaA.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
  }
  private void setCursorDef() {
    this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    jTextAreaA.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
  }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="FilteredStreams">
private class errFilteredStreamSED extends java.io.FilterOutputStream {
    public errFilteredStreamSED(java.io.OutputStream aStream) {
        super(aStream);
      } // constructor
    @Override
    public void write(byte b[]) throws java.io.IOException {
        String aString = new String(b);
        jTabbedPane.setTitleAt(1, "<html><u>M</u>essages</html>");
        jTabbedPane.setEnabledAt(1, true);
        jTabbedPane.setSelectedComponent(jScrollPaneMessg);
        jTextAreaA.append(aString);
        jTextAreaA.setSelectionStart(Integer.MAX_VALUE);
    }
    @Override
    public void write(byte b[], int off, int len) throws java.io.IOException {
        String aString = new String(b , off , len);
        jTabbedPane.setTitleAt(1, "<html><u>M</u>essages</html>");
        jTabbedPane.setEnabledAt(1, true);
        jTabbedPane.setSelectedComponent(jScrollPaneMessg);
        jTextAreaA.append(aString);
        jTextAreaA.setSelectionStart(Integer.MAX_VALUE);
    } // write
    } // class errFilteredStreamSED

/** redirect output */
private class outFilteredStreamSED extends java.io.FilterOutputStream {
    public outFilteredStreamSED(java.io.OutputStream aStream) {
      super(aStream);
    } // constructor
    @Override
    public void write(byte b[]) throws java.io.IOException {
        String aString = new String(b);
        jTabbedPane.setTitleAt(1, "<html><u>M</u>essages</html>");
        jTabbedPane.setEnabledAt(1, true);
        jTextAreaA.append(aString);
        jTextAreaA.setSelectionStart(Integer.MAX_VALUE);
   }
    @Override
    public void write(byte b[], int off, int len) throws java.io.IOException {
        String aString = new String(b , off , len);
        jTabbedPane.setTitleAt(1, "<html><u>M</u>essages</html>");
        jTabbedPane.setEnabledAt(1, true);
        jTextAreaA.append(aString);
        jTextAreaA.setSelectionStart(Integer.MAX_VALUE);
    } // write
    } // class outFilteredStreamSED

//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="getTheInputFileName">
    /** Get an input data file name from the user
     * using an Open File dialog */
    private void getTheInputFileName() {
        jTextFieldDataFile.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        setCursorWait();
        if(pathDef == null) {setPathDef();}
        fc.setMultiSelectionEnabled(false);
        fc.setCurrentDirectory(new java.io.File(pathDef.toString()));
        fc.setDialogTitle("Select a data file:");
        fc.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
        fc.setAcceptAllFileFilterUsed(true);
        javax.swing.LookAndFeel defLaF = javax.swing.UIManager.getLookAndFeel();
        try {javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());}
        catch (Exception ex) {}
        fc.updateUI();
        jTextFieldDataFile.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setCursorDef();
        fc.setFileFilter(filterDat);
        int returnVal = fc.showOpenDialog(this);
        // reset the look and feel
        try {javax.swing.UIManager.setLookAndFeel(defLaF);}
        catch (javax.swing.UnsupportedLookAndFeelException ex) {}
        Util.configureOptionPane();
        if(returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {
            if(eraseTextArea) {
                jTextAreaA.selectAll();
                jTextAreaA.replaceRange("", 0, jTextAreaA.getSelectionEnd());
            }
            inputDataFile = fc.getSelectedFile();
            setPathDef(fc.getCurrentDirectory());
            if(readDataFile(inputDataFile)) {
                showTheInputFileName(inputDataFile);
                outputPltFile = null;
                String txt = inputDataFile.getName();
                String plotFileN = txt.substring(0,txt.length()-3).concat("plt");
                jTextFieldPltFile.setText(plotFileN);
                jTabbedPane.setSelectedComponent(jPanelParameters);
                jTabbedPane.setTitleAt(2, "Diagram");
                jTabbedPane.setEnabledAt(2, false);
                jMenuFileOpen.setEnabled(true);
                jMenuFileMakeD.setEnabled(true);
                jButtonDoIt.setEnabled(true);
                jButtonDoIt.setText("<html>make the <u>D</u>iagram</html>");
                jButtonDoIt.requestFocusInWindow();
            } // if readDataFile
            else {return;}
        } // if returnVal = JFileChooser.APPROVE_OPTION
        jTabbedPane.setSelectedComponent(jPanelParameters);
        jTabbedPane.requestFocusInWindow();
        jButtonDoIt.requestFocusInWindow();
        jCheckReverse.setText("allow Reversed min. and max. axes limits");
        jCheckReverse.setEnabled(false);
    } // getTheInputFileName()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="paintDiagrPanel">
/** used when constructing the jPanelDiagram:
 * <pre>jPanelDiagram = new javax.swing.JPanel() {@Override
 *    public void paint(java.awt.Graphics g)
 *         {
 *             super.paint(g);
 *             paintDiagrPanel(g);
 *         }
 *     };</pre>
 */
    private void paintDiagrPanel(java.awt.Graphics g) {
        java.awt.Graphics2D g2D = (java.awt.Graphics2D)g;
        if(dd != null) {
            diagrPaintUtil.paintDiagram(g2D, jPanelDiagram.getSize(), dd, false);
        }
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="printInstructions">
  private static void printInstructions(java.io.PrintStream out) {
    if(out == null) {out = System.out;}
    out.flush();
    out.println("Possible commands are:"+nl+
    "  -d=data-file-name  (input file name)"+nl+
    "  -dbg     (output debug information)"+nl+
    "  -dbgH=n  (level for debug output from HaltaFall"+nl+
    "            in the first calculation point; default ="+Chem.DBGHALTA_DEF+")"+nl+
    "  -eng     (display concentrations in engineering notation)"+nl+
    "  -h=nbr   (height factor for labels in the plot)"+nl+
    "  -i=nbr   (ionic strength (the equil. constants are"+nl+
    "            assumed for I=0). Requires a temperature."+nl+
    "            Enter \"-i=-1\" to calculate I at each point)"+nl+
    "  -keep    (window open to see the diagram after the calculations)"+nl+
    "  -m=nbr   (model to calculate activity coefficients:"+nl+
    "            0 = Davies eqn; 1 = SIT; 2 = simplified HKF; default =2)"+nl+
    "  -n=nbr   (calculation steps along the X-axis; "+(NSTP_MIN)+" to "+(NSTP_MAX)+")"+nl+
    "  -nostop  (do not stop for warnings)"+nl+
    "  -p=output-plot-file-name"+nl+
    "  -pr=nbr  (pressure in bar; displayed in the diagram)"+nl+
    "  -rev     (do not reverse the input min. and max. limits in x-axis)"+nl+
    "  -sci     (display concentrations in scientific notation)"+nl+
    "  -t=nbr   (temperature in degrees C, ignored if not needed)"+nl+
    "  -tbl     (output both a diagram and a table file with comma-"+nl+
    "            separated values and extension \"csv\")"+nl+
    "  -tbls=;  (character(s) to separate fields in the output table file;"+nl+
    "            for example comma(,) semicolon(;) or tab (\\t))"+nl+
    "  -tble=csv (name extension for output table file)"+nl+
    "  -tblcs=\"  (character(s) to insert at the start of comment lines)"+nl+
    "  -tblce=\"  (character(s) to append to the end of comment lines)"+nl+
    "  -tol=nbr (tolerance when solving mass-balance equations in Haltafall,"+nl+
    "            0.01 >= nbr >= 1e-9; default ="+Chem.TOL_HALTA_DEF+")"+nl+
    "  -thr=nbr (threshold to display curves in a fraction diagram,"+nl+
    "            0.001 to 0.1 (equivalent to 0.1 ro 10%); ignored if not needed)"+nl+
    "  -units=nbr (concentration units displayed in the diagram: 0=\"molal\","+nl+
    "              1=\"mol/kg_w\", 2=\"M\", -1=\"\")"+nl+
    "Enclose file names with double quotes (\"\") it they contain blank space."+nl+
    "Example:   java -jar SED.jar \"/d=Fe 25\" -t:25 -i=-1 \"-p:plt\\Fe 25\" -n=200");
  } //printInstructions(out)
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="readDataFile">
private boolean readDataFile(java.io.File dataFile) {
    if(dbg) {out.println("--- readDataFile("+dataFile.getAbsolutePath()+")");}
    String msg;
    //--- check the name
    if(!dataFile.getName().toLowerCase().endsWith(".dat")) {
        msg = "File: \""+dataFile.getName()+"\""+nl+
                "Error: data file name must end with \".dat\"";
        showErrMsgBx(msg,1);
        return false;
    }
    if(dataFile.getName().length() <= 4) {
        msg = "File: \""+dataFile.getName()+"\""+nl+
                "Error: file name must have at least one character";
        showErrMsgBx(msg,1);
        return false;
    }
    String dataFileN = null;
    try {dataFileN = dataFile.getCanonicalPath();} catch (java.io.IOException ex) {}
    if(dataFileN == null) {
        try {dataFileN = dataFile.getAbsolutePath();}
        catch (Exception ex) {dataFileN = dataFile.getPath();}
    }
    dataFile = new java.io.File(dataFileN);
    //
    //--- create a ReadDataLib instance
    try {rd = new ReadDataLib(dataFile);}
    catch (ReadDataLib.DataFileException ex) {
        showErrMsgBx(ex.getMessage(),1);
        if(rd != null) {
            try{rd.close();}
            catch (ReadDataLib.ReadDataLibException ex2) {showErrMsgBx(ex2);}
        }
        return false;
    }
    msg = LINE+nl+"Reading input data file \""+dataFile+"\"";
    out.println(msg);
    if(consoleOutput) {System.out.println(msg);}
    //--- read the chemical system (names, equilibrium constants, stoichiometry)
    boolean warn = false; // throw an exception for missing plot data
    try {ch = null;
        ch = ReadChemSyst.readChemSystAndPlotInfo(rd, dbg, warn, out);
    } catch (ReadChemSyst.ConcDataException ex) {
        ch = null; showErrMsgBx(ex.getMessage(), 1);
    }
    catch (ReadChemSyst.DataLimitsException ex) {
        ch = null; showErrMsgBx(ex.getMessage(), 1);
    }
    catch (ReadChemSyst.PlotDataException ex) {
        ch = null; showErrMsgBx(ex.getMessage(), 1);
    }
    catch (ReadChemSyst.ReadDataFileException ex) {
        ch = null; showErrMsgBx(ex.getMessage(), 1);
    }
    if(ch == null) {
        msg = "Error while reading data file \""+dataFile.getName()+"\"";
        showMsg(msg,1);
        try {rd.close();}
        catch (ReadDataLib.ReadDataLibException ex) {showMsg(ex);}
        readDataFile_hadError();
        return false;
    }
    if(ch.diag.plotType <= 0 || ch.diag.plotType > 8) {
        msg = "Error: data file \""+dataFile.getName()+"\""+nl;
        if(ch.diag.plotType == 0) {msg = msg +
                "contains information for a Predominance Area Diagram."+nl+
                "Run program PREDOM instead.";}
        else {msg = msg + "contains erroneous plot information.";}
        showErrMsgBx(msg,1);
        try {rd.close();}
        catch (ReadDataLib.ReadDataLibException ex) {showMsg(ex);}
        readDataFile_hadError();
        return false;}
    //
    //--- get a temperature:
    double t_d, w;
    try {w = rd.getTemperature();} // temperature written as a comment in the data file?
    catch (ReadDataLib.DataReadException ex) {showErrMsgBx(ex); w = Double.NaN;}
    if(!Double.isNaN(w)) {
      t_d = w;
      if(!Double.isNaN(temperature_InCommandLine)) {
        // temperature also given in command line
        if(Math.abs(t_d - temperature_InCommandLine)>0.001) { // difference?
          msg = String.format(engl,"Warning: temperature in data file =%6.2f,%s",t_d,nl);
          msg = msg + String.format(engl,
                  "   but in the command line t=%6.2f!%s",
                  temperature_InCommandLine,nl);
          msg = msg + String.format(engl,"t=%6.2f will be used.",temperature_InCommandLine);
          showErrMsgBx(msg,2);
          t_d = temperature_InCommandLine;
        } // temperatures differ
      }  // temperature also given in command line
    } // temperature written in data file
    else {t_d = 25.;}
    jLabelTemperature.setText(String.valueOf(t_d));
    //--- get a pressure:
    double p_d;
    try {w = rd.getPressure();} // pressure written as a comment in the data file?
    catch (ReadDataLib.DataReadException ex) {showErrMsgBx(ex); w = Double.NaN;}
    if(!Double.isNaN(w)) {
      p_d = w;
      if(!Double.isNaN(pressure_InCommandLine)) {
        // pressure also given in command line
        if(Math.abs(p_d - pressure_InCommandLine)>0.001) { // difference?
          msg = String.format(engl,"Warning: pressure in data file =%.3f bar,%s",p_d,nl);
          msg = msg + String.format(engl,
                  "   but in the command line p=%.3f bar!%s",
                  pressure_InCommandLine,nl);
          msg = msg + String.format(engl,"p=%.3f will be used.",pressure_InCommandLine);
          showErrMsgBx(msg,2);
          p_d = pressure_InCommandLine;
        } // pressures differ
      }  // pressure also given in command line
    } // pressure written in data file
    else {p_d = 1.;}
    jLabelPressure.setText(String.valueOf(p_d));

    try {rd.close();}
    catch (ReadDataLib.ReadDataLibException ex) {showMsg(ex);}
    msg = "Finished reading the input data file.";
    out.println(msg);
    if(consoleOutput) {System.out.println(msg);}
    //--- set the references pointing to the instances of the storage classes
    cs = ch.chemSystem;
    csC = cs.chemConcs;
    namn = cs.namn;
    dgrC = ch.diagrConcs;
    diag = ch.diag;

    //---- Set the calculation instructions for HaltaFall
    // Concentration types for each component:
    //    hur =1 for "T" (fixed Total conc.)
    //    hur =2 for "TV" (Tot. conc. Varied)
    //    hur =3 for "LTV" (Log(Tot.conc.) Varied)
    //    hur =4 for "LA" (fixed Log(Activity) value)
    //    hur =5 for "LAV" (Log(Activity) Varied)</pre>
    for(int j =0; j < cs.Na; j++) {
        if(dgrC.hur[j] >3)
            {csC.kh[j]=2;} //kh=2 log(activity) is given
                            //The Mass Balance eqn. has to be solved
        else {csC.kh[j]=1;} //kh=1 Tot.Conc. is given
                            //The Tot.Conc. will be calculated
    }

    // --------------------------
    // ---  Check concs. etc  ---
    // --------------------------
    if(!checkInput()) {
        ch = null; cs = null; csC = null; namn = null; dgrC = null; diag = null;
        readDataFile_hadError();
        return false;}

    if(cs.jWater >=0 && dbg) {
        out.println("Water (H2O) is included. All concentrations are in \"mol/(kg H2O)\".");
    }

    return true;
} //readDataFile()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Temperature & Ionic Strength">
    private void validateIonicStrength() {
        if(jTextFieldIonicStgr.getText().length() <=0) {return;}
        try{
            ionicStrength = readIonStrength();
            ionicStrength = Math.min(1000,Math.max(ionicStrength, -1000));
            if(ionicStrength < 0) {ionicStrength = -1;}
            jTextFieldIonicStgr.setText(String.valueOf(ionicStrength));
        } //try
        catch (NumberFormatException nfe) {
            String msg = "Wrong numeric format"+nl+nl+"Please enter a floating point number.";
            showErrMsgBx(msg,1);
            jTextFieldIonicStgr.setText(String.valueOf(ionicStrength));
            jTextFieldIonicStgr.requestFocusInWindow();
        } //catch
    } // validateIonicStrength()

  /** Reads the value in <code>jTextFieldIonicStgr</code>.
   * Check that it is within -1 to 1000
   * @return the ionic strength */
  private double readIonStrength() {
    if(jTextFieldIonicStgr.getText().length() <=0) {return 0;}
    double w;
    try{w = Double.parseDouble(jTextFieldIonicStgr.getText());
        w = Math.min(1000,Math.max(w, -1));
        if(w < 0) {w = -1;}
        } //try
    catch (NumberFormatException nfe) {
        out.println("Error reading Ionic Strength:"+nl+"   "+nfe.toString());
        w = 0;
        } //catch
    return w;
  } //readIonStrength()

  /** Reads the value in <code>jLabelTemperature</code>.
   * Checks that it is within -50 to 1000 Celsius. It returns 25
   * if there is no temperature to read.
   * @return the temperature in Celsius */
  private double readTemperature() {
    if(jLabelTemperature.getText().length() <=0) {return 25.;}
    double w;
    try{w = Double.parseDouble(jLabelTemperature.getText());
        w = Math.min(1000,Math.max(w, -50));
        } //try
    catch (NumberFormatException nfe) {
        if(!jLabelTemperature.getText().equals("NaN")) {
            out.println("Error reading Temperature:"+nl+"   "+nfe.toString());
        }
        w = 25.;
    } //catch
    return w;
  } //readTemperature()

  /** Reads the value in <code>jLabelPressure</code>.
   * Checks that it is within 1 to 10000 bar. It returns 1 (one bar)
   * if there is no pressure to read.
   * @return the pressure in bar */
  private double readPressure() {
    if(jLabelPressure.getText().length() <=0) {return 1.;}
    double w;
    try{w = Double.parseDouble(jLabelPressure.getText());
        w = Math.min(10000.,Math.max(w, 1.));
        } //try
    catch (NumberFormatException nfe) {
        if(!jLabelPressure.getText().equals("NaN")) {
            out.println("Error reading Pressure:"+nl+"   "+nfe.toString());
        }
        w = 1.;
    } //catch
    return w;
  } //readPressure()
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="showErrMsgBx">

/** Outputs a message (through a call to <code>showMsg(msg,type)</code>
 * and shows it in a [OK, Cancel] message box (if doNotStop = false)
 * @param msg the message
 * @param type =1 exception error; =2 warning; =3 information
 * @return it return <code>true</code> if the user chooses "OK", returns <code>false</code> otherwise
 * @see #showMsg(java.lang.String, int) showMsg */
  boolean showErrMsgBxCancel(String msg, int type) {
    if(msg == null || msg.trim().length() <=0) {return true;}
    //if(type == 1) {type = 0;}
    showMsg(msg,type);
    if(!doNotStop && sedFrame != null) {
        int j;
        if(type<=1) {j=javax.swing.JOptionPane.ERROR_MESSAGE;}
        else if(type==2) {j=javax.swing.JOptionPane.INFORMATION_MESSAGE;}
        else {j=javax.swing.JOptionPane.WARNING_MESSAGE;}
        if(!sedFrame.isVisible()) {sedFrame.setVisible(true);}
        Object[] opt = {"OK", "Cancel"};
        int n= javax.swing.JOptionPane.showOptionDialog(sedFrame,msg,
                        progName,javax.swing.JOptionPane.OK_CANCEL_OPTION,j, null, opt, opt[0]);
        if(n != javax.swing.JOptionPane.OK_OPTION) {return false;}
    }
    return true;
}

/** Outputs a message (through a call to <code>showMsg(msg,0)</code> if type=1,
 * or to <code>showMsg(msg,type)</code> if type is =2 or 3),
 * and shows it in a message box (if doNotStop = false)
 * @param msg the message
 * @param type =1 exception error; =2 warning; =3 information
 * @see #showMsg(java.lang.String, int) showMsg
 */
void showErrMsgBx(String msg, int type) {
    if(msg == null || msg.trim().length() <=0) {return;}
    //if(type == 1) {type = 0;}
    showMsg(msg,type);
    if(!doNotStop) {
        if(sedFrame == null) {
            ErrMsgBox mb = new ErrMsgBox(msg, progName);
        } else {
            int j;
            if(type<=1) {j=javax.swing.JOptionPane.ERROR_MESSAGE;}
            else if(type==2) {j=javax.swing.JOptionPane.INFORMATION_MESSAGE;}
            else {j=javax.swing.JOptionPane.WARNING_MESSAGE;}
            if(!this.isVisible()) {this.setVisible(true);}
            javax.swing.JOptionPane.showMessageDialog(this, msg, progName,j);
        }
    }
}
/** Outputs the exception message and the stack trace,
 * through a call to <code>showMsg(ex)</code>,
 * and shows a message box (if doNotStop = false)
 * @param ex the exception
 * @see #showMsg(java.lang.Exception) showMsg */
void showErrMsgBx(Exception ex) {
    if(ex == null) {return;}
    showMsg(ex);
    String msg = ex.getMessage();
    if(!doNotStop) {
        if(sedFrame == null) {
            ErrMsgBox mb = new ErrMsgBox(msg, progName);
        } else {
            int j = javax.swing.JOptionPane.ERROR_MESSAGE;
            if(!this.isVisible()) {this.setVisible(true);}
            javax.swing.JOptionPane.showMessageDialog(this, msg, progName,j);
        }
    }
}
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="showMsg">
/** Outputs a message either to System.out (if type is 1, 2 or 3) or
 * to System.err otherwise (type=0).
 * @param msg the message
 * @param type =0 error (outputs message to System.err); =1 error; =2 warning; =3 information
 * @see #showErrMsgBx(java.lang.String, int) showErrMsgBx */
void showMsg(String msg, int type) {
    if(msg == null || msg.trim().length() <=0) {return;}
    final String flag;
    if(type == 2) {flag = "Warning";} else if(type == 3) {flag = "Message";} else {flag = "Error";}
    if(type == 1 || type == 2 || type == 3) {
        out.println("- - - - "+flag+":"+nl+msg+nl+"- - - -");
        System.out.println("- - - - "+flag+":"+nl+msg+nl+"- - - -");
        out.flush(); System.out.flush();
    } else {
        err.println("- - - - "+flag+":"+nl+msg+nl+"- - - -");
        System.err.println("- - - - "+flag+":"+nl+msg+nl+"- - - -");
        err.flush(); System.err.flush();
    }
}
/** Outputs the exception message and the stack trace to System.err and to err
 * @param ex the exception
 * @see #showErrMsgBx(java.lang.Exception) showErrMsgBx */
void showMsg(Exception ex) {
    if(ex == null) {return;}
    String msg = "- - - - Error:"+nl+ex.getMessage()+nl+nl+Util.stack2string(ex)+nl+"- - - -";
    err.println(msg);
    System.err.println(msg);
    err.flush();
    System.err.flush();
}
  //</editor-fold>

//<editor-fold defaultstate="collapsed" desc="doCalculations">
  private void doCalculations() {
    setCursorWait();
    if(dbg) {out.println("--- doCalculations()");}
    jLabelStatus.setText("Starting the calculations");
    jLabelProgress.setText(" ");
    disableMenus();
    diag.fractionThreshold = threshold;
    //---- Check if "EH" is needed -----------
    if(diag.Eh &&
            diag.pInX != 3 && diag.plotType != 5) { //plotType=5: pe in Y-axis
        boolean peGiven = false;
        for(int i =0; i < cs.Na; i++) {
            if(Util.isElectron(namn.identC[i]) &&
                csC.kh[i] ==2) { // kh=2  logA given
                    peGiven = true; break;}
        } //for i
        if(!peGiven) {diag.Eh = false;}
    }
    //--- temperature & pressure ------------------------
    diag.temperature = readTemperature();
    diag.pressure = readPressure();
    if(Double.isNaN(diag.temperature)){
        String msg = "";
        if(diag.Eh) {msg = "\"Error: Need to plot Eh values but no temperature is given.";}
        else if(calcActCoeffs)  {msg = "\"Error: activity coefficient calculations required but no temperature is given.";}
        if(!msg.isEmpty()) {
            showErrMsgBx(msg,1);
            setCursorDef();
            restoreMenus(true);
            return;            
        }
    }
    // decide if the temperature should be displayed in the diagram
    if(!diag.Eh && !calcActCoeffs) {
        if(dbg) {out.println(" (Note: temperature not needed in the diagram)");}
    } else {
        double pSat;
        try {pSat= lib.kemi.H2O.IAPWSF95.pSat(diag.temperature);}
        catch (Exception ex) {
            out.println("\"IAPWSF95.pSat\": "+ex.getMessage());
            out.println("Calculations cancelled.");
            restoreMenus(false);
            setCursorDef();
            return;
        }
        if(Double.isNaN(diag.pressure)){diag.pressure = 1.;}
        if(diag.temperature <= 99.61) {
            if(diag.pressure < 1.) {
                out.println("tC = "+diag.temperature+", pBar = "+diag.pressure+", setting pBar = 1.");
            }
            diag.pressure = Math.max(1.,diag.pressure);
        }
        // if pressure = 1 bar and temperature = 0, set temperature to 0.01 C (tripple point of water)
        if(diag.pressure >0.99999 && diag.pressure < 1.00001 && Math.abs(diag.temperature) < 0.001) {diag.temperature = 0.01;}
        if(diag.temperature <= 373.95) { // below critical point
            if(diag.pressure < (pSat*0.999)) {
                out.println("tC = "+diag.temperature+", pBar = "+diag.pressure+",  setting pBar = "+(float)pSat);
                diag.pressure = pSat;
            }
        }
    }
    if(diag.Eh) {peEh = (ln10*8.3144126d*(diag.temperature+273.15d)/96484.56d);} else {peEh = Double.NaN;}
    // nbr of calculation steps
    if(!dbg) {
        nSteps = Math.max(NSTP_MIN,nSteps);
        nSteps = jScrollBarNbrPoints.getValue();
    }
    final int nSteps1 = nSteps + 1;
    jLabelNbrPText.setText("Nbr of calc. steps:");
    jScrollBarNbrPoints.setEnabled(false);
    jLabelNbrPText.setEnabled(false);
    if(dbg) {out.println(" "+nSteps1+" caculation points"+nl+
            "ionic strength = "+ionicStrength+nl+
            "temperature = "+(float)diag.temperature+", pressure = "+(float)diag.pressure+nl+
            "max relative mass-balance tolerance = "+(float)tolHalta);}

    // ---------------------------------------
    // get an instance of Plot
    plot = new Plot(this, err, out);
    //---- set up some arrays
    plot.preparePlot(ch);
    // ---------------------------------------

    String msg;
    // ionic strength
    diag.ionicStrength = ionicStrength;
    if(!diag.aquSystem && diag.ionicStrength != 0) {
        msg = "Warning: This does not appear to be an aqueous system,"+nl+
                "and yet you give a value for the ionic strength?";
        showErrMsgBx(msg,2);
    }

    // model to calculate ion activity coefficients
    if(calcActCoeffs) {
        diag.activityCoeffsModel = jComboBoxModel.getSelectedIndex();
        diag.activityCoeffsModel = Math.min(jComboBoxModel.getItemCount()-1,
                                        Math.max(0,diag.activityCoeffsModel));
        csC.actCoefCalc = true;
    } else {
        diag.activityCoeffsModel = -1;
        csC.actCoefCalc = false;
    }
    // height scale for texts in the diagram
    tHeight = jScrollBarHeight.getValue()/10;
    tHeight = Math.min(10.d,Math.max(tHeight, 0.3));

    int j;
    // ---- Store given conc. ranges in array bt[Na][nSteps+1]
    bt = new double[cs.Na][nSteps1];
    for(j =0; j < cs.Na; j++) {
        if(dgrC.hur[j] ==1 || dgrC.hur[j] ==4) { //T or LA
            for(int n =0; n < bt[0].length; n++) {bt[j][n] = dgrC.cLow[j];}
        } //if T or LA
        else {  //if TV, LTV or LAV
            double stepX =(dgrC.cHigh[j] - dgrC.cLow[j]) / nSteps;
            bt[j][0] = dgrC.cLow[j];
            for(int n =1; n < (bt[0].length-1); n++) {bt[j][n] = bt[j][n-1] + stepX;}
            bt[j][nSteps] = dgrC.cHigh[j]; // to avoid rounding errors
            if(dgrC.hur[j] ==3) { // LTV
                for(int n =0; n < bt[0].length; n++)
                    {bt[j][n] = Math.exp(ln10*bt[j][n]);}
            } //if LTV
        }  //if TV, LTV or LAV
    } //for j

    // check POS and NEG with the Tot.Conc. given in the input
    for(j =0; j < cs.Na; j++) {
        if(csC.kh[j] == 2) {continue;} //only it Tot.conc. is given
        if(!(pos[j] && neg[j]) && (pos[j] || neg[j])) {
            if(dgrC.hur[j] !=3 &&  //not LTV
               dgrC.cLow[j]==0 && (Double.isNaN(dgrC.cHigh[j]) || dgrC.cHigh[j]==0)) {
                    //it is only POS or NEG and Tot.Conc =0
                    for(int n =0; n < bt[0].length; n++) {bt[j][n] = -9999.;}
                    csC.kh[j] =2; //kh=2 means logA given
                    if(dbg) {out.println("Can not calculate mass-balance for for component \""+namn.identC[j]+"\""+nl+
                        "   its log(activity) will be set to -9999.");}
            }
        }
    } //for j

    calculationStart = System.nanoTime();

    msg = "Starting the calculations...";
    out.println(msg);
    if(consoleOutput) {System.out.println(msg);}

    // ---- Make an instance of Factor
    String userHome = System.getProperty("user.home");
    try{factor = new Factor(ch, pathApp, userHome, pathDef.toString(), out);}
    catch (Exception ex) {showErrMsgBx(ex.getMessage(),1); calcActCoeffs = false; diag.ionicStrength = Double.NaN;}
    out.flush();
    // ---- print information on the model used for activity coefficients
    if(factor != null) {
        try {factor.factorPrint(dbg);}
        catch (Exception ex) {showErrMsgBx(ex.getMessage(),1); calcActCoeffs = false; diag.ionicStrength = Double.NaN;}
    }
    if(factor == null) {
        out.println("Calculations cancelled.");
        restoreMenus(false);
        setCursorDef();
        return;
    }
    
    // ---- table output?
    if(inputDataFile == null) {tableOutput = false;}
    if(tableOutput) {
        table = new Table(this, err, out);
        String txt = inputDataFile.getAbsolutePath();
        if(tblExtension == null) {tblExtension = "";}
        if(tblExtension.startsWith(".")) {tblExtension = tblExtension.substring(1);}
        String tableFileN = txt.substring(0,txt.length()-4).concat(".").concat(tblExtension);
        java.io.File outputTableFile = new java.io.File(tableFileN);
        if(!table.tableHeader(ch, outputTableFile)) {
            table = null;
            tableOutput = false;
        }
    }

    // ---- Initialize variables
    csC.dbg = dbgHalta;
    csC.cont = false;
    csC.tol = tolHalta;
    for(j =0; j < cs.Na; j++) {
        if(csC.kh[j] == 1) {
            csC.tot[j]=bt[j][0];
            csC.logA[j]=-10;
            if(csC.tot[j]>0) {csC.logA[j] = Math.log10(csC.tot[j]) -3;}            
        }
        else {csC.logA[j]=bt[j][0];}
    } // for j

    // ---- Run the calculations on another Thread ----
    jLabelStatus.setText("Please wait --");
    finishedCalculations = false;

    tsk = new HaltaTask();
    tsk.execute();

  } //doCalculations()
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="private class HaltaTask">
/** A SwingWorker to perform tasks in the background.
 * @see HaltaTask#doInBackground() doInBackground() */
public class HaltaTask extends javax.swing.SwingWorker<Boolean, Integer> {
    private boolean getHaltaInstanceOK = true;
    private boolean haltaError = false;
    private int nbrTooLargeConcs = 0;
    private int nbrHaltaErrors = 0;
    private int nbrHaltaUncertain = 0;
    private final StringBuilder failuresMsg = new StringBuilder();
  /** The instructions to be executed are defined here
   * @return true if no error occurs, false otherwise
   * @throws Exception */
  @Override protected Boolean doInBackground() throws Exception {
    //--- do the HaltaFall calculations
    // create an instance of class HaltaFall
    h = null;
    try {h = new HaltaFall(cs,factor, out);}
    catch (Chem.ChemicalParameterException ex) { // this should not occur, but you never know
        showErrMsgBx(ex);
        getHaltaInstanceOK = false; // skip the rest of the thread
    }
    if(!getHaltaInstanceOK) {this.cancel(true); return false;}

    int nStepX1, j;
    double tolHalta0 = csC.tol;
    final String f = "--- Calculation problem in \"HaltaFall.haltaCalc\" at point=%d, x=%7.5f"+nl+"%s";
    nStepX = -1;
    do_loopX:
    do {  // -------------------------------------- Loop for X-axis
        nStepX++;
        nStepX1 = nStepX+1;
        publish(nStepX1);
        //if needed for debugging: sleep some milliseconds (wait) at each calculation point
        //try {Thread.sleep(500);} catch(Exception ex) {}

        // --- input data for this calculation point
        for(j =0; j < cs.Na; j++) {
            if(dgrC.hur[j] >1 && dgrC.hur[j] !=4) { //TV, LTV or LAV
                if(csC.kh[j] == 1) {csC.tot[j]=bt[j][nStepX];}
                else {csC.logA[j]=bt[j][nStepX];}
            } //if TV, LTV or LAV
        } // for j

        // ------ print debug output from HaltaFall only for the first point (first point = zero) ------
        if(nStepX == 0) {
            if(dbg || csC.dbg > Chem.DBGHALTA_DEF) {
                out.println("Starting calculation point: "+nStepX1+" (of "+(nSteps+1)+"), x="+(float)bt[diag.compX][nStepX]);
            }
            /** // ########## ---------- ##########  ---------- ########## ##?##
                csC.dbg = 6;
                out.println("---- Note:  nStepX="+nStepX+"  (debug) ----");
            //     ########## ---------- ##########  ---------- ########## ##?## */
        } else {
            csC.dbg = Chem.DBGHALTA_DEF;
            /** // ########## ---------- ##########  ---------- ########## ##?##
            j = diag.compX;
            if(Math.abs(bt[j][nStepX]+0.35)<0.005) {
                csC.dbg = 6;
                out.println("---- Note: x="+(float)bt[j][nStepX]+",  (debug) ---- nStepX="+nStepX);
            }   // ########## ---------- ##########  ---------- ########## ##?## */
        }

        // --- HaltaFall: do the calculations
        //     calculate the equilibrium composition of the system
        try {
            csC.tol = tolHalta0;
            h.haltaCalc();
            if(csC.isErrFlagsSet(2)) { // too many iterations when solving the mass balance equations
                do {
                    csC.tol = csC.tol * 0.1; // decrease tolerance and try again
                    if(dbg || csC.dbg > Chem.DBGHALTA_DEF) {
                        out.println("Too many iterations when solving the mass balance equations"+nl+
                                "  decreasing tolerance to: "+(float)csC.tol+" and trying again.");
                    }
                    h.haltaCalc();
                } while (csC.isErrFlagsSet(2) && csC.tol >= 1e-9);
                csC.tol = tolHalta0;
                if(dbg || csC.dbg > Chem.DBGHALTA_DEF) {
                    out.println("Restoring tolerance to: "+(float)tolHalta0+" for next calculations.");
                }
            }
            if(csC.isErrFlagsSet(3)) { // failed to find a satisfactory combination of solids
                if(dbg || csC.dbg > Chem.DBGHALTA_DEF) {
                    out.println("Failed to find a satisfactory combination of solids. Trying again...");
                }
                csC.cont = false;      // try again
                h.haltaCalc();
            }
        }
        catch (Chem.ChemicalParameterException ex) {
            String ms = "Error in \"HaltaFall.haltaCalc\", "+ex.getMessage()+nl+
                    "   at point: "+nStepX1+"  x="+bt[diag.compX][nStepX]+nl+
                    Util.stack2string(ex);
            showErrMsgBx(ms, 1);
            haltaError = true;
            break; // do_loopX;
        }
        // ---
        if(finishedCalculations) {break;} // do_loopX  // user request exit?

        out.flush();

        if(csC.isErrFlagsSet(1)) {nbrHaltaUncertain++;}
        if(csC.isErrFlagsSet(5)) {nbrTooLargeConcs++;}
        if(csC.isErrFlagsSet(2) || csC.isErrFlagsSet(3) || csC.isErrFlagsSet(4)
                            || csC.isErrFlagsSet(6)) {
            nbrHaltaErrors++;
            if(failuresMsg.length() >0) {failuresMsg.append(nl);}
            failuresMsg.append(String.format(engl,f,nStepX1,(float)bt[diag.compX][nStepX],csC.errFlagsGetMessages()));
        }

        if(dbg) {
            h.printConcs();
            factor.printActivityCoeffs(out);
        }

        if(finishedCalculations) {break;} // do_loopX  // user request exit?

        // store the results for later plotting (and table output)
        plot.storePlotData(nStepX, ch);
        if(table != null) {table.tableBody(nStepX, ch);}
    } while (nStepX < nSteps); // -------------------------- Loop for X-axis
    return true;
  }
  /** Performs some tasks after the calculations have been finished */
  @Override protected void done() {
    if(isCancelled()) {
        if(dbg) {System.out.println("SwingWorker cancelled.");}
    } else {
        String msg;
        if(!haltaError) {
            calculationTime = (System.nanoTime() - calculationStart)
                    /1000000; //convert nano seconds to milli seconds
            msg = "--- Calculated "+(nSteps+1)+" points, time="+millisToShortDHMS(calculationTime);
            out.println(msg);
            System.out.println(msg);
            msg = "";
            if(nbrTooLargeConcs > 0) {
                int percent = nbrTooLargeConcs*100 / (nSteps+1);
                if(percent > 0) {
                    msg = percent+" % of the calculated points had some"+nl+
                          "concentrations > "+(int)Factor.MAX_CONC+" (molal); impossible in reality."+nl+nl;
                    if(calcActCoeffs) {msg = msg + "The activity coefficients are then WRONG"+nl+
                          "and the results unrealistic."+nl;}
                    else {msg = msg + "These results are unrealistic."+nl;}
                }
            }
            if(nbrHaltaErrors <= 0 && nbrHaltaUncertain >0 && dbg) {
                if(msg.length() >0) msg = msg+nl;
                msg = msg+String.format("%d",nbrHaltaUncertain).trim()+" point(s) with round-off errors (not within tolerance).";
            }
            if(nbrHaltaErrors >0) {
                if(msg.length() >0) msg = msg+nl;
                msg = msg+String.format("Calculations failed for %d",nbrHaltaErrors).trim()+" point(s).";
            }
            if(msg.length() >0) {showErrMsgBx(msg, 1);}
            if(nbrHaltaErrors >0 && failuresMsg != null && failuresMsg.length() >0) {// failuresMsg should not be empty...
                out.println(LINE);
                out.println(failuresMsg);
                out.println(LINE);
            }

            out.println("Saving plot file \""+outputPltFile.getAbsolutePath()+"\"...");
            try{plot.drawPlot(outputPltFile, ch);}
            catch (Exception ex) {
              showErrMsgBx("Error: "+ex.getMessage()+nl+
                  "while saving plot file \""+outputPltFile.getAbsolutePath()+"\"", 1);
            }
            if(outputPltFile != null && outputPltFile.getName().length()>0) {
                String msg3 = "Saved plot file: \""+outputPltFile.getAbsolutePath()+"\"";
                out.println(msg3);
                System.out.println(msg3);
            }
            if(table != null) {table.tableClose();}
        } // if !haltaError

        // execute the following actions on the event-dispatching Thread
        // after the "calculations" and the plotting are finished
        if(getHaltaInstanceOK && !haltaError) {
          javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
            jTabbedPane.setTitleAt(2, "<html><u>D</u>iagram</html>");
            jTabbedPane.setEnabledAt(2, true);
            jTabbedPane.setSelectedComponent(jPanelDiagram);
            jTabbedPane.requestFocusInWindow();
            restoreMenus(true);
          }}); // invokeLater
        }//if getHaltaInstanceOK
        else {
          javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
            jTabbedPane.setSelectedComponent(jScrollPaneMessg);
            jTabbedPane.requestFocusInWindow();
            restoreMenus(true);
          }}); // invokeLater
        }//if !getHaltaInstanceOK
        if(dbg) {System.out.println("SwingWorker done.");}
    }
    out.println(LINE);
    System.out.println(LINE);
    finishedCalculations = true;
    sedFrame.notify_All();
    setCursorDef();
  }
  @Override protected void process(java.util.List<Integer> chunks) {
    // Here we receive the values that we publish(). They may come grouped in chunks.
    final int i = chunks.get(chunks.size()-1);
    int nn = (int)Math.floor(Math.log10(nSteps+1))+1;
    final String f = "now calculating loop: %"+String.format("%3d",nn).trim()
            +"d (out of %"+String.format("%3d",nn).trim()+"d)";
    javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
        jLabelProgress.setText(String.format(f,i,(nSteps+1)));
    }}); // invokeLater
  }
    }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="main">
  /** The "main" method. Creates a new frame if needed.
   * Errors and messages are sent to System.out and System.err.
   * @param args the command line arguments */
  public static void main(final String args[]) {
    // ----
    System.out.println(LINE+nl+progName+" (Simple Equilibrium Diagrams),  version: "+VERS);
    // set LookAndFeel
    //try {javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getCrossPlatformLookAndFeelClassName());}
    //try {javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");}
    try {javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());}
    catch (Exception ex) {}
    //---- for JOptionPanes set the default button to the one with the focus
    //     so that pressing "enter" behaves as expected:
    javax.swing.UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
    //     and make the arrow keys work:
    Util.configureOptionPane();

    if(args.length <=0) {System.out.println("Usage:   SED  [-command=value]"+nl+
                "For a list of possible commands type:  SED  -?");}
    else {
        if(DBG_DEFAULT) {System.out.println("SED "+java.util.Arrays.toString(args));}
    }
    //---- get Application Path
    pathApp = Main.getPathApp();
    if(DBG_DEFAULT) {System.out.println("Application path: \""+pathApp+"\"");}
    //---- "invokeAndWait": Wait for either:
    //     - the main window is shown, or
    //     - perform the calculations and save the diagram
    boolean ok = true;
    String errMsg = "SED construction did not complete successfully"+nl;
    try {
        java.awt.EventQueue.invokeAndWait(new Runnable() {@Override public void run() {
            // deal with some special command-line arguments
            boolean doNotExit0 = false;
            boolean doNotStop0 = false;
            boolean dbg0 = DBG_DEFAULT;
            boolean rev0 = false;
            boolean h = false;
            if(args.length > 0) {
                for(String arg : args) {
                    if (arg.equalsIgnoreCase("-dbg") || arg.equalsIgnoreCase("/dbg")) {
                        dbg0 =true;
                    } else if (arg.equalsIgnoreCase("-keep") || arg.equalsIgnoreCase("/keep")) {
                        doNotExit0 =true;
                    } else if (arg.equalsIgnoreCase("-nostop") || arg.equalsIgnoreCase("/nostop")) {
                        doNotStop0 = true;
                    } else if (arg.equalsIgnoreCase("-rev") || arg.equalsIgnoreCase("/rev")) {
                        rev0 =true;
                    } else if (arg.equals("-?") || arg.equals("/?") || arg.equals("?")) {
                        h = true;
                        printInstructions(System.out);
                    } //if args[] = "?"
                } //for arg
                if(h && !doNotExit0) {return;} // exit after help if OK to exit
            } //if args.length >0
            sedFrame = new SED(doNotExit0, doNotStop0, dbg0);//.setVisible(true);
            sedFrame.start(rev0, h, args);
        }}); //invokeAndWait
    } catch (InterruptedException ex) {
        ok = false;  errMsg = errMsg + Util.stack2string(ex);
    }
    catch (java.lang.reflect.InvocationTargetException ex) {
        ok = false;  errMsg = errMsg + Util.stack2string(ex)+nl+ex.getCause().toString();
    }
    if(!ok) {
        System.err.println(errMsg);
        ErrMsgBox mb = new ErrMsgBox(errMsg, progName);
    }

    //-- wait, either for the calculations to finish, or
    //   for the window to be closed by the user
    if(sedFrame != null) {
        Thread t = new Thread() {@Override public void run(){
            if(sedFrame.inputDataFileInCommandLine) {
                sedFrame.synchWaitCalcs();
                if(!sedFrame.doNotExit) {sedFrame.end_program();}
                else{sedFrame.synchWaitProgramEnded();}
            } else {
                sedFrame.synchWaitProgramEnded();
            }
        }};// Thread t
        t.start();  // Note: t.start() returns inmediately;
                    // statements here are executed inmediately.
        try {t.join();} catch (InterruptedException ex) {} // wait for the thread to finish
        if(sedFrame.dbg) {System.out.println(progName+" - finished.");}
    }
    //javax.swing.JOptionPane.showMessageDialog(null, "ready", progName, javax.swing.JOptionPane.INFORMATION_MESSAGE);
  } // main(args[])
  //</editor-fold>

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupDebug;
    private javax.swing.JButton jButtonDoIt;
    private javax.swing.JCheckBox jCheckActCoeff;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuSEDdebug;
    private javax.swing.JCheckBox jCheckReverse;
    private javax.swing.JCheckBox jCheckTable;
    private javax.swing.JComboBox jComboBoxModel;
    private javax.swing.JComboBox jComboBoxTol;
    private javax.swing.JLabel jLabelBar;
    private javax.swing.JLabel jLabelData;
    private javax.swing.JLabel jLabelHD;
    private javax.swing.JLabel jLabelHeight;
    private javax.swing.JLabel jLabelIonicStr;
    private javax.swing.JLabel jLabelIonicStrM;
    private javax.swing.JLabel jLabelModel;
    private javax.swing.JLabel jLabelNbrPText;
    private javax.swing.JLabel jLabelP;
    private javax.swing.JLabel jLabelPltFile;
    private javax.swing.JLabel jLabelPointsNbr;
    private javax.swing.JLabel jLabelPressure;
    private javax.swing.JLabel jLabelProgress;
    private javax.swing.JLabel jLabelStatus;
    private javax.swing.JLabel jLabelT;
    private javax.swing.JLabel jLabelTC;
    private javax.swing.JLabel jLabelTemperature;
    private javax.swing.JLabel jLabelTol;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenuItem jMenuCancel;
    private javax.swing.JMenu jMenuDebug;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenuItem jMenuFileMakeD;
    private javax.swing.JMenuItem jMenuFileOpen;
    private javax.swing.JMenuItem jMenuFileXit;
    private javax.swing.JMenuItem jMenuHF_dbg;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuHelpAbout;
    private javax.swing.JMenuItem jMenuSave;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanelActC;
    private javax.swing.JPanel jPanelDiagram;
    private javax.swing.JPanel jPanelFiles;
    private javax.swing.JPanel jPanelParameters;
    private javax.swing.JPanel jPanelStatusBar;
    private javax.swing.JPanel jPanelT;
    private javax.swing.JScrollBar jScrollBarHeight;
    private javax.swing.JScrollBar jScrollBarNbrPoints;
    private javax.swing.JScrollPane jScrollPaneMessg;
    private javax.swing.JTabbedPane jTabbedPane;
    private javax.swing.JTextArea jTextAreaA;
    private javax.swing.JTextField jTextFieldDataFile;
    private javax.swing.JTextField jTextFieldIonicStgr;
    private javax.swing.JTextField jTextFieldPltFile;
    // End of variables declaration//GEN-END:variables

} // class SED
