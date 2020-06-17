package spana;

import lib.common.MsgExceptn;
import lib.common.Util;
import lib.huvud.Div;
import lib.huvud.ProgramConf;
import lib.kemi.chem.Chem;
import lib.kemi.readDataLib.ReadDataLib;
import lib.kemi.readWriteDataFiles.DefaultPlotAndConcs;
import lib.kemi.readWriteDataFiles.ReadChemSyst;
import lib.kemi.readWriteDataFiles.WriteChemSyst;
import static spana.MainFrame.LINE;

/** Perhaps the most important part of the program: it allows the user to
 * make changes to the input file without a text editor. A lot of
 * "intelligence" is used to make life easy for the user.
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
public class Select_Diagram extends javax.swing.JFrame {
    // classes used to store data
    private final ProgramConf pc;
    private final spana.ProgramDataSpana pd;
    private Chem ch = null;
    private Chem.ChemSystem cs = null;
    private Chem.ChemSystem.NamesEtc namn = null;
    private Chem.Diagr diag = null;
    /** the diagram data originally found in the input data file.
     * Used to check if the file needs to be saved because changes have been made. */
    private Chem.Diagr diag0 = null;
    private Chem.DiagrConcs dgrC = null;
    /** the diagram concentrations originally found in the input data file.
     * Used to check if the file needs to be saved because changes have been made. */
    private Chem.DiagrConcs dgrC0 = null;
    //
    private final java.awt.Dimension windowSize;
    private boolean finished = false;
    private java.io.File dataFile;
    private java.io.File pltFile;
    private boolean cancel = false;
    private int nbrGases;
    /** the species equal to "e-" if it exists, otherwise =-1.
     * It may be a component or a reaction product. */
    private int ePresent;
    /** the species equal to "H+" if it exists, otherwise =-1.
     * It may be a component or a reaction product. */
    private int hPresent;

    //---- these local fields are discarded if the user does not save them as default
    /** 0 = loading frame (starting); 1 = Predom diagram; 2 = SED diagram*/
    private int runPredomSED;
    /** number of calculation steps for SED diagram.
     * The number of calculation points is the number of steps plus one. */
    private int runNbrStepsSED;
    /** number of calculation steps for Predom diagram.
     * The number of calculation points is the number of steps plus one. */
    private int runNbrStepsPred;
    /** output a table with results (for SED obly)? */
    private boolean runTbl;
    /** aqueous species only? */
    private boolean runAqu;
    /** allow reverse concentration ranges in the axes? */
    private boolean runRevs;
    /** use "Eh" (if true) or "pe" (if false) as variable in diagram */
    private boolean runUseEh;
    /** model to calculate activity coefficients<ul>
     * <li> &lt;0 for ideal solutions (all activity coefficients = 1)
     * <li> =0 Davies eqn.
     * <li> =1 SIT (Specific Ion interaction "Theory")
     * <li> =2 Simplified HKF (Helson, Kirkham and Flowers)
     * </ul> */
    private int runActCoeffsMethod;
    /** draw pH line in Pourbaix diagrams? */
    private boolean runPHline;
    /** concentration units when drawing diagrams
     * (may be 0="molal", 1="mol/kg_w", 2="M" or -1="") */
    private int runConcUnits;
    /** concentration notation when drawing diagrams
     * (may be 0="no choice", 1="scientific", 2="engineering") */
    private int runConcNottn;
    

    /** true if SED/Predom are from the Medusa-32 package, that is, they are Fortran programs */
    private boolean oldProg = false;
    /** New-line character(s) to substitute "\n" */
    private static final String nl = System.getProperty("line.separator");
    private final double ln10 = Math.log(10);
    /** <pre>concTypes[]:
     *  [0]= "Total conc."      [1]= "Total conc. varied"
     *  [2]= "log (Total conc.) varied"
     *  [3]= "log (activity)"   [4]= "log (activity) varied"
     *  [5]= "pH"               [6]= "pH varied"
     *  [7]= "pe"               [8]= "pe varied"
     *  [9]= "Eh"              [10]= "Eh varied"
     * [11]= "log P"           [12]= "log P varied"</pre>
    * @see Select_Diagram#componentConcType componentConcType */
    private final String[] concTypes = {"Total conc.","Total conc. varied",
    "log (Total conc.) varied","log (activity)","log (activity) varied",
    "pH","pH varied","pe","pe varied","Eh","Eh varied",
    "log P","log P varied"};
   /** for each component it indicates which of concTypes is used:<pre>
    *  0 = Total conc.      1 = Total conc. varied
    *  2 = log (Total conc.) varied
    *  3 = log (activity)   4 = log (activity) varied
    *  5 = pH               6 = pH varied
    *  7 = pe               8 = pe varied
    *  9 = Eh              10 = Eh varied
    * 11 = log P           12 = log P varied</pre>
    * Used in <code>updateAxes()</code> to determine <code>mustChange</code>.
    * @see Select_Diagram#concTypes concTypes */
    private int[] componentConcType;
    private boolean loading, changingComp, updatingAxes;
    private boolean getConcLoading;
    private String xAxisType0, yAxisType0;
    /** the species-number for H+ or e- if its activity will be plotted in the
     * Y-axis. If neither calculated pH nor pe/Eh is plotted in the Y-axis,
     * then Ycalc = -1. Note that Ycalc may be a component or a reaction product.
     * @see chem.Chem.Diagr#compY compY
     * @see chem.Chem.Diagr#compX compX */
    private int Ycalc;
    private final javax.swing.DefaultListModel<String> listCompConcModel = new javax.swing.DefaultListModel<>();
    //private final javax.swing.DefaultListModel listCompConcModel = new javax.swing.DefaultListModel(); // java 1.6
    private final java.awt.Color backg;
    /** when a jTextField containing a number is edited,
     * the old text is saved in this variable. If the user makes a mistake, for example
     * enters "0.7e", then the old text is restored from this variable. */
    private String oldTextCLow = "0";
    /** when a jTextField containing a number is edited,
     * the old text is saved in this variable. If the user makes a mistake, for example
     * enters "0.7e", then the old text is restored from this variable. */
    private String oldTextCHigh = "0";
    /** when a jTextField containing a number is edited,
     * the old text is saved in this variable. If the user makes a mistake, for example
     * enters "0.7e", then the old text is restored from this variable. */
    private String oldTextI = "0";
    /** when a jTextField containing a number is edited,
     * the old text is saved in this variable. If the user makes a mistake, for example
     * enters "0.7e", then the old text is restored from this variable. */
    private String oldTextT = "25";
    /** when a jTextField containing a number is edited,
     * the old text is saved in this variable. If the user makes a mistake, for example
     * enters "0.7e", then the old text is restored from this variable. */
    private String oldTextXmin = "0";
    /** when a jTextField containing a number is edited,
     * the old text is saved in this variable. If the user makes a mistake, for example
     * enters "0.7e", then the old text is restored from this variable. */
    private String oldTextXmax = "0";
    /** when a jTextField containing a number is edited,
     * the old text is saved in this variable. If the user makes a mistake, for example
     * enters "0.7e", then the old text is restored from this variable. */
    private String oldTextYmin = "0";
    /** when a jTextField containing a number is edited,
     * the old text is saved in this variable. If the user makes a mistake, for example
     * enters "0.7e", then the old text is restored from this variable. */
    private String oldTextYmax = "0";
    private static String lastDataFileName = null;
    private static String lastPlotFileName = null;
    private boolean plotAndConcsNotGiven;
    /** true if the temperature is written as a comment in the first line of the input file */
    private boolean temperatureGivenInInputFile;
    private double ionicStrOld;
    /** the concentration type in <code>jComboBoxConcType</code> when the user clicks
     * on <code>jListCompConc</code> */
    private String comboBoxConcType0;
    /** do not fire the action event when adding an item to the combo box
     * or when setting the selected item within the program  */
    private boolean diagramType_doNothing = false;
    /** the selected component index in the concentrations list */
    private int getConc_idx;
    /** command line arguments passed to the program (SED or Predom) that calculates the diagram */
    private String[] args;
    /** a class that loads a jar file and executes its "main" method. Used to
     * run the program (SED or Predom) that calculates the diagram */
    private lib.huvud.RunJar rj;
    private static final java.text.DecimalFormat myFormatter =
            (java.text.DecimalFormat)java.text.NumberFormat.getNumberInstance(java.util.Locale.ENGLISH);
    private static final String SLASH = java.io.File.separator;
    private final javax.swing.border.Border scrollBorder;
    private final javax.swing.border.Border defBorder;
    private final javax.swing.border.Border highlightedBorder = javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED, java.awt.Color.gray, java.awt.Color.black);

    //<editor-fold defaultstate="collapsed" desc="Constructor">
    public Select_Diagram(java.io.File datFile,
            ProgramConf pc0,
            spana.ProgramDataSpana pd0) {
        this.pc = pc0;
        this.pd = pd0;
        dataFile = datFile;
        // ---- compose the jFrame
        initComponents();
        //move jTextFieldYmax and jTextFieldYmin to the right of their jPanels
        jPanelYmax.add(javax.swing.Box.createHorizontalGlue(),0);
        jPanelYmax.validate();
        jPanelYmin.add(javax.swing.Box.createHorizontalGlue(),0);
        jPanelYmin.validate();
        //move jTextFieldXmax and jTextFieldXmin to the right and left of their jPanel
        jPanelXaxis.add(javax.swing.Box.createHorizontalGlue(),1);
        jPanelXaxis.validate();
        //
        myFormatter.setGroupingUsed(false);
        // ---- center Window on Screen
        windowSize = this.getSize();
        int left; int top;
        left = Math.max(0, (MainFrame.screenSize.width  - windowSize.width ) / 2);
        top = Math.max(0, (MainFrame.screenSize.height - windowSize.height) / 2);
        left = Math.min(MainFrame.screenSize.width-100, left);
        top = Math.min(MainFrame.screenSize.height-100, top);
        if(MainFrame.locationSDFrame.x >= 0) {
            this.setLocation(MainFrame.locationSDFrame);
        } else {this.setLocation(left,top);}
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        //--- close window on ESC key / exit jPanelGetConc
        javax.swing.KeyStroke escKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                    java.awt.event.KeyEvent.VK_ESCAPE,0, false);
        javax.swing.Action escAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if(jPanelGetConc.isShowing()) {getConc_Unload();}
                else {cancel =true; quitFrame();}
            }};
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(escKeyStroke,"ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escAction);
        //--- F1 for help
        javax.swing.KeyStroke f1KeyStroke = javax.swing.KeyStroke.getKeyStroke(
                    java.awt.event.KeyEvent.VK_F1,0, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(f1KeyStroke,"F1");
        javax.swing.Action f1Action = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                jButton_Help.doClick();
            }};
        getRootPane().getActionMap().put("F1", f1Action);
        //--- F2 to edit a concentration
        javax.swing.KeyStroke f2KeyStroke = javax.swing.KeyStroke.getKeyStroke(
                    java.awt.event.KeyEvent.VK_F2,0, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(f2KeyStroke,"F2");
        javax.swing.Action f2Action = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if(jListCompConc.isVisible()) {
                    compConcList_Click(jListCompConc.getSelectedIndex());
                }
            }};
        getRootPane().getActionMap().put("F2", f2Action);
        // ---- Define Alt-keys
        //      Alt-M, Alt-Q, Alt-H and Alt-S are mnemonics to the 4 buttons
        //      Alt-E, R, T, W are shortcuts for check-boxes
        // Define Alt-Enter, -X, -O, -C, -F, -N, -D, -I
        //--- Alt-X = Make diagram
        javax.swing.KeyStroke altXKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altXKeyStroke,"ALT_X");
        javax.swing.Action altXAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                jButton_OK.doClick();
            }};
        getRootPane().getActionMap().put("ALT_X", altXAction);
        //--- Alt-Enter = Make diagram
        javax.swing.KeyStroke altEnterKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                    java.awt.event.KeyEvent.VK_ENTER, java.awt.event.InputEvent.ALT_MASK, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altEnterKeyStroke,"ALT_ENTER");
        getRootPane().getActionMap().put("ALT_ENTER", altXAction);
        //--- Alt-O  concentration-ok/make diagram
        javax.swing.KeyStroke altOKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.ALT_MASK, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altOKeyStroke,"ALT_O");
        javax.swing.Action altOAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if(jPanelGetConc.isShowing()) {jButtonGetConcOK.doClick();}
                else {jButton_OK.doClick();}
            }};
        getRootPane().getActionMap().put("ALT_O", altOAction);
        //--- Alt-C  focus to concentration/cancel
        javax.swing.KeyStroke altCKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.ALT_MASK, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altCKeyStroke,"ALT_C");
        javax.swing.Action altCAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if(jPanelGetConc.isShowing()) {getConc_Unload();}
                else {
                    jListCompConc.requestFocus();
                    jListCompConc.clearSelection();
                }
            }};
        getRootPane().getActionMap().put("ALT_C", altCAction);
        //--- Alt-F = File name text box
        javax.swing.KeyStroke altFKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.ALT_MASK, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altFKeyStroke,"ALT_F");
        javax.swing.Action altFAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                jTextFieldDataFile.requestFocusInWindow();
            }};
        getRootPane().getActionMap().put("ALT_F", altFAction);
        //--- Alt-N = Diagram name text box
        javax.swing.KeyStroke altNKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.ALT_MASK, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altNKeyStroke,"ALT_N");
        javax.swing.Action altNAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                jTextFieldDiagName.requestFocusInWindow();
            }};
        getRootPane().getActionMap().put("ALT_N", altNAction);
        //--- Alt-A = activity coefficient method
        javax.swing.KeyStroke altAKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.ALT_MASK, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altAKeyStroke,"ALT_A");
        javax.swing.Action altAAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if(jPanelModel.isVisible()) {jComboBoxActCoeff.requestFocusInWindow();}
            }};
        getRootPane().getActionMap().put("ALT_A", altAAction);
        //--- Alt-D diagram
        javax.swing.KeyStroke altDKeyStroke = javax.swing.KeyStroke.getKeyStroke(
              java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.ALT_MASK, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altDKeyStroke,"ALT_D");
        javax.swing.Action altDAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                jComboBoxDiagType.requestFocus();
            }};
        getRootPane().getActionMap().put("ALT_D", altDAction);
        //--- Alt-I ionic strength
        javax.swing.KeyStroke altIKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.ALT_MASK, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altIKeyStroke,"ALT_I");
        javax.swing.Action altIAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                jTextFieldIonicStr.requestFocus();
            }};
        getRootPane().getActionMap().put("ALT_I", altIAction);

        // ---- Title
        this.setTitle(" Select Diagram Type: "+datFile.getName());
        // ---- Icon
        String iconName = "images/SelDiagr.gif";
        java.net.URL imgURL = this.getClass().getResource(iconName);
        if (imgURL != null) {this.setIconImage(new javax.swing.ImageIcon(imgURL).getImage());}
        else {System.out.println("--- Error: Could not load image = \""+iconName+"\"");}
        // ---- Set main things
        defBorder = jScrollPaneCompConcList.getBorder();
        scrollBorder = jScrollBarSEDNbrP.getBorder();
        backg = jPanelDiagrType.getBackground();
        loading = true;
        updatingAxes = false;
        changingComp = false;

        if(!pd.advancedVersion) {
            jLabelTitle.setText("");
            jTextFieldTitle.setVisible(false);
        }

    } // constructor

  public void start() {
    if(pc.dbg) {System.out.println("---- starting \"Select_Diagram\" ----");}
    if(!readDataFile(pc.dbg)) {
        cancel =true;
        quitFrame();
        return;
    }
    // --- set default values
    jComboBoxActCoeff.setSelectedIndex(pd.actCoeffsMethod);
    jLabelModel.setVisible(pd.advancedVersion);
    jComboBoxActCoeff.setVisible(pd.advancedVersion);
    runNbrStepsPred = pd.Predom_nbrSteps;
    jLabelPredNbrP.setText(String.valueOf(runNbrStepsPred));
    jScrollBarPredNbrP.setValue(runNbrStepsPred);
    jScrollBarPredNbrP.setFocusable(true);
    runNbrStepsSED = pd.SED_nbrSteps;
    jLabelSEDNbrP.setText(String.valueOf(runNbrStepsSED));
    jScrollBarSEDNbrP.setValue(runNbrStepsSED);
    jScrollBarSEDNbrP.setFocusable(true);
    runAqu = pd.aquSpeciesOnly; jCheckBoxAqu.setSelected(runAqu);
    runPHline = pd.drawNeutralPHinPourbaix; jCheckBoxDrawPHline.setSelected(runPHline);
    runConcUnits = Math.max(-1,Math.min(pd.concentrationUnits, 2));
    runConcNottn = Math.max(0,Math.min(pd.concentrationNotation, 2));
    runTbl = pd.SED_tableOutput; jCheckBoxTableOut.setSelected(runTbl);
    runRevs = pd.reversedConcs; jCheckBoxRev.setSelected(runRevs);
    runUseEh = pd.useEh; jCheckBoxUseEh.setSelected(runUseEh);
    runPredomSED = 0;

    diag.ionicStrength = pd.ionicStrength;
    ionicStrOld = diag.ionicStrength;
    jTextFieldIonicStr.setText(Util.formatNum(diag.ionicStrength));
    if(diag.ionicStrength >= 0) {jRadioButtonFixed.doClick();} else {jRadioButtonCalc.doClick();}

    this.pack();

    setUpFrame();

    loading = false;

    updateAxes();
    updateConcList();
    updateUseEhCheckBox();
    updateDrawPHlineBox();

    //focusLostYmin(); focusLostYmax();
    //focusLostXmin(); focusLostXmax();
    resizeXMinMax(); resizeYMax(); resizeYMin();
    this.setVisible(true);
    windowSize.width = getWidth();
    windowSize.height = getHeight();
    if(plotAndConcsNotGiven) {missingPlotAndConcsMessage();}
    if(pc.dbg) {System.out.println("---- \"Select_Diagram\" started! ----");}
  } // start()

  private void missingPlotAndConcsMessage() {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override
    public void run() {
        String msg= "Warning: plot/concentration data missing or not correct"+nl+
                    "  in input data file:"+nl+
                    "        \""+dataFile+"\""+nl+
                    "  Default values will be used.";
        System.out.println(msg);
        if(diag.databaseSpanaFile != 1) {
            javax.swing.JOptionPane.showMessageDialog(Select_Diagram.this, msg,
                        pc.progName+" - Missing data", javax.swing.JOptionPane.WARNING_MESSAGE);
        }
    }}); //invokeLater(Runnable)
  }
  //</editor-fold>

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupActCoef = new javax.swing.ButtonGroup();
        jPanelButtons = new javax.swing.JPanel();
        jButton_OK = new javax.swing.JButton();
        jButton_Cancel = new javax.swing.JButton();
        jButton_Help = new javax.swing.JButton();
        jPanelFileNames = new javax.swing.JPanel();
        jLabelDataFile = new javax.swing.JLabel();
        jLabelDFileName = new javax.swing.JLabel();
        jTextFieldDataFile = new javax.swing.JTextField();
        jLabelOFile = new javax.swing.JLabel();
        jTextFieldDiagName = new javax.swing.JTextField();
        jPanelDiagram = new javax.swing.JPanel();
        jPanelTitle = new javax.swing.JPanel();
        jLabelTitle = new javax.swing.JLabel();
        jTextFieldTitle = new javax.swing.JTextField();
        jPanelDiagrType = new javax.swing.JPanel();
        jPanelAxes = new javax.swing.JPanel()   
        {   
            @Override
            public void paint(java.awt.Graphics g)   
            {   
                super.paint(g);   
                paintDiagPanel(g);   
            }   
        };
        jLabelDiagrType = new javax.swing.JLabel();
        jComboBoxDiagType = new javax.swing.JComboBox<>();
        jPanelMainComp = new javax.swing.JPanel();
        jComboBoxMainComp = new javax.swing.JComboBox<>();
        jPanelXaxis = new javax.swing.JPanel();
        jTextFieldXmin = new javax.swing.JTextField();
        jTextFieldXmax = new javax.swing.JTextField();
        jPanelYaxis = new javax.swing.JPanel();
        jLabelYaxis = new javax.swing.JLabel();
        jPanelYmax = new javax.swing.JPanel();
        jTextFieldYmax = new javax.swing.JTextField();
        jPanelYaxInner = new javax.swing.JPanel();
        jPanelYcombo = new javax.swing.JPanel();
        jComboBoxYaxType = new javax.swing.JComboBox<>();
        jPanelYlogC = new javax.swing.JPanel();
        jLabelYlogC = new javax.swing.JLabel();
        jPanelYlogA = new javax.swing.JPanel();
        jLabelYlogA = new javax.swing.JLabel();
        jPanelYlogS = new javax.swing.JPanel();
        jLabelYlogS = new javax.swing.JLabel();
        jPanelYRef = new javax.swing.JPanel();
        jLabelYRef = new javax.swing.JLabel();
        jPanelYfraction = new javax.swing.JPanel();
        jLabelYFract = new javax.swing.JLabel();
        jPanelYcalc = new javax.swing.JPanel();
        jLabelYcalc = new javax.swing.JLabel();
        jPanelYHaff = new javax.swing.JPanel();
        jLabelHaff = new javax.swing.JLabel();
        jPanelYaxComp = new javax.swing.JPanel();
        jComboBoxYaxComp = new javax.swing.JComboBox<>();
        jPanelEmptyYax = new javax.swing.JPanel();
        jPanelYmin = new javax.swing.JPanel();
        jTextFieldYmin = new javax.swing.JTextField();
        jPanelXComponent = new javax.swing.JPanel();
        jLabelXaxis = new javax.swing.JLabel();
        jComboBoxXaxType = new javax.swing.JComboBox<>();
        jComboBoxXaxComp = new javax.swing.JComboBox<>();
        jPanelEmptyDiagram = new javax.swing.JPanel();
        jPanelParams = new javax.swing.JPanel();
        jPanelActCoef = new javax.swing.JPanel();
        jLabelIS = new javax.swing.JLabel();
        jRadioButtonFixed = new javax.swing.JRadioButton();
        jRadioButtonCalc = new javax.swing.JRadioButton();
        jLabelIonicS = new javax.swing.JLabel();
        jTextFieldIonicStr = new javax.swing.JTextField();
        jLabelM = new javax.swing.JLabel();
        jLabelT = new javax.swing.JLabel();
        jTextFieldT = new javax.swing.JTextField();
        jLabelTC = new javax.swing.JLabel();
        jPanelModel = new javax.swing.JPanel();
        jLabelModel = new javax.swing.JLabel();
        jComboBoxActCoeff = new javax.swing.JComboBox<>();
        jPanelSedPredom = new javax.swing.JPanel();
        jPanelPredom = new javax.swing.JPanel();
        jLabelPredNbr = new javax.swing.JLabel();
        jLabelPredNbrP = new javax.swing.JLabel();
        jScrollBarPredNbrP = new javax.swing.JScrollBar();
        jPanelTotCalcs = new javax.swing.JPanel();
        jLabelTotNbr = new javax.swing.JLabel();
        jLabelNbrCalcs = new javax.swing.JLabel();
        jCheckBoxAqu = new javax.swing.JCheckBox();
        jCheckBoxDrawPHline = new javax.swing.JCheckBox();
        jLabelSpaceP = new javax.swing.JLabel();
        jPanelSED = new javax.swing.JPanel();
        jCheckBoxTableOut = new javax.swing.JCheckBox();
        jLabelSEDNbr = new javax.swing.JLabel();
        jLabelSEDNbrP = new javax.swing.JLabel();
        jScrollBarSEDNbrP = new javax.swing.JScrollBar();
        jPanelEmptyParameters = new javax.swing.JPanel();
        jPanelLow = new javax.swing.JPanel();
        jCheckBoxRev = new javax.swing.JCheckBox();
        jCheckBoxUseEh = new javax.swing.JCheckBox();
        jLabelSpace = new javax.swing.JLabel();
        jButtonSaveDef = new javax.swing.JButton();
        jPanelConcs = new javax.swing.JPanel();
        jScrollPaneCompConcList = new javax.swing.JScrollPane();
        jListCompConc = new javax.swing.JList();
        jPanelGetConc = new javax.swing.JPanel();
        jLabelEnterConc = new javax.swing.JLabel();
        jPanelConcInner = new javax.swing.JPanel();
        jLabel_GetConcCompName = new javax.swing.JLabel();
        jComboBoxConcType = new javax.swing.JComboBox<>();
        jLabelEqual = new javax.swing.JLabel();
        jLabelFrom = new javax.swing.JLabel();
        jTextFieldCLow = new javax.swing.JTextField();
        jLabelTo = new javax.swing.JLabel();
        jTextFieldCHigh = new javax.swing.JTextField();
        jButtonGetConcOK = new javax.swing.JButton();
        jButtonGetConcCancel = new javax.swing.JButton();
        jPanelLabl = new javax.swing.JPanel();
        jLabl1 = new javax.swing.JLabel();
        jLabl2 = new javax.swing.JLabel();
        jLabl3 = new javax.swing.JLabel();
        jLabl4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanelButtons.setMaximumSize(new java.awt.Dimension(169, 61));
        jPanelButtons.setMinimumSize(new java.awt.Dimension(100, 61));

        jButton_OK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/spana/images/Spana_icon_32x32.gif"))); // NOI18N
        jButton_OK.setMnemonic('m');
        jButton_OK.setToolTipText("<html><u>M</u>ake Diagram (Alt-X or Alt-M)</html>"); // NOI18N
        jButton_OK.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton_OK.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton_OK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_OKActionPerformed(evt);
            }
        });

        jButton_Cancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/spana/images/Trash.gif"))); // NOI18N
        jButton_Cancel.setMnemonic('q');
        jButton_Cancel.setToolTipText("Cancel (Alt-Q, Esc)"); // NOI18N
        jButton_Cancel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton_Cancel.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton_Cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_CancelActionPerformed(evt);
            }
        });

        jButton_Help.setIcon(new javax.swing.ImageIcon(getClass().getResource("/spana/images/Help_32x32.gif"))); // NOI18N
        jButton_Help.setMnemonic('h');
        jButton_Help.setToolTipText("<html><u>H</u>elp</html>"); // NOI18N
        jButton_Help.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton_Help.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton_Help.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_HelpActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelButtonsLayout = new javax.swing.GroupLayout(jPanelButtons);
        jPanelButtons.setLayout(jPanelButtonsLayout);
        jPanelButtonsLayout.setHorizontalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton_OK)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton_Cancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton_Help)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_OK)
                    .addComponent(jButton_Cancel)
                    .addComponent(jButton_Help))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabelDataFile.setText("<html>Input Data <u>F</u>ile</html>"); // NOI18N

        jLabelDFileName.setLabelFor(jTextFieldDataFile);
        jLabelDFileName.setText("<html>name:</html>"); // NOI18N

        jTextFieldDataFile.setText("jTextFieldDataFile"); // NOI18N
        jTextFieldDataFile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextFieldDataFileMouseClicked(evt);
            }
        });
        jTextFieldDataFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldDataFileActionPerformed(evt);
            }
        });
        jTextFieldDataFile.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldDataFileFocusGained(evt);
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

        jLabelOFile.setLabelFor(jTextFieldDiagName);
        jLabelOFile.setText("<html>Diagram <u>n</u>ame:</html>"); // NOI18N

        jTextFieldDiagName.setText("jTextFieldDiagName"); // NOI18N
        jTextFieldDiagName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldDiagNameFocusGained(evt);
            }
        });

        javax.swing.GroupLayout jPanelFileNamesLayout = new javax.swing.GroupLayout(jPanelFileNames);
        jPanelFileNames.setLayout(jPanelFileNamesLayout);
        jPanelFileNamesLayout.setHorizontalGroup(
            jPanelFileNamesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelFileNamesLayout.createSequentialGroup()
                .addGroup(jPanelFileNamesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelFileNamesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelFileNamesLayout.createSequentialGroup()
                            .addGap(4, 4, 4)
                            .addComponent(jLabelOFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jLabelDataFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabelDFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelFileNamesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldDataFile)
                    .addComponent(jTextFieldDiagName))
                .addContainerGap())
        );
        jPanelFileNamesLayout.setVerticalGroup(
            jPanelFileNamesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelFileNamesLayout.createSequentialGroup()
                .addGroup(jPanelFileNamesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelDataFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelFileNamesLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelFileNamesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextFieldDataFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelDFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelFileNamesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelOFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldDiagName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanelDiagram.setBorder(javax.swing.BorderFactory.createTitledBorder(null, " Diagram: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12), new java.awt.Color(153, 0, 153))); // NOI18N

        jLabelTitle.setLabelFor(jTextFieldTitle);
        jLabelTitle.setText("Title:"); // NOI18N

        jTextFieldTitle.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldTitleFocusGained(evt);
            }
        });

        javax.swing.GroupLayout jPanelTitleLayout = new javax.swing.GroupLayout(jPanelTitle);
        jPanelTitle.setLayout(jPanelTitleLayout);
        jPanelTitleLayout.setHorizontalGroup(
            jPanelTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTitleLayout.createSequentialGroup()
                .addComponent(jLabelTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldTitle)
                .addGap(0, 0, 0))
        );
        jPanelTitleLayout.setVerticalGroup(
            jPanelTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jLabelTitle)
                .addComponent(jTextFieldTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanelDiagrType.setMaximumSize(new java.awt.Dimension(179, 131));

        jPanelAxes.setBackground(new java.awt.Color(236, 236, 236));
        jPanelAxes.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanelAxes.setMaximumSize(new java.awt.Dimension(179, 105));

        jLabelDiagrType.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelDiagrType.setForeground(new java.awt.Color(0, 0, 255));
        jLabelDiagrType.setLabelFor(jComboBoxDiagType);
        jLabelDiagrType.setText("<html><u>D</u>iagram type:</html>"); // NOI18N

        jComboBoxDiagType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Predominance Area", "Logarithmic", "log Activities", "Fraction", "log Solubilities", "Relative activities", "Calculated Eh", "Calculated pH", "H+ affinity spectrum" }));
        jComboBoxDiagType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxDiagTypeActionPerformed(evt);
            }
        });

        jPanelMainComp.setOpaque(false);

        javax.swing.GroupLayout jPanelMainCompLayout = new javax.swing.GroupLayout(jPanelMainComp);
        jPanelMainComp.setLayout(jPanelMainCompLayout);
        jPanelMainCompLayout.setHorizontalGroup(
            jPanelMainCompLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        jPanelMainCompLayout.setVerticalGroup(
            jPanelMainCompLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jComboBoxMainComp.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxMainComp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxMainCompActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelAxesLayout = new javax.swing.GroupLayout(jPanelAxes);
        jPanelAxes.setLayout(jPanelAxesLayout);
        jPanelAxesLayout.setHorizontalGroup(
            jPanelAxesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAxesLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanelAxesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelDiagrType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxDiagType, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelAxesLayout.createSequentialGroup()
                        .addComponent(jComboBoxMainComp, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelMainComp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanelAxesLayout.setVerticalGroup(
            jPanelAxesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAxesLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(jLabelDiagrType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxDiagType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelAxesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBoxMainComp, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanelMainComp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        jPanelXaxis.setLayout(new javax.swing.BoxLayout(jPanelXaxis, javax.swing.BoxLayout.LINE_AXIS));

        jTextFieldXmin.setText("-5"); // NOI18N
        jTextFieldXmin.setName("Xmin"); // NOI18N
        jTextFieldXmin.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldXminFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldXminFocusLost(evt);
            }
        });
        jTextFieldXmin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldXminActionPerformed(evt);
            }
        });
        jTextFieldXmin.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldXminKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldXminKeyTyped(evt);
            }
        });
        jPanelXaxis.add(jTextFieldXmin);

        jTextFieldXmax.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldXmax.setText("-5"); // NOI18N
        jTextFieldXmax.setName("Xmax"); // NOI18N
        jTextFieldXmax.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldXmaxFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldXmaxFocusLost(evt);
            }
        });
        jTextFieldXmax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldXmaxActionPerformed(evt);
            }
        });
        jTextFieldXmax.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldXmaxKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldXmaxKeyTyped(evt);
            }
        });
        jPanelXaxis.add(jTextFieldXmax);

        javax.swing.GroupLayout jPanelDiagrTypeLayout = new javax.swing.GroupLayout(jPanelDiagrType);
        jPanelDiagrType.setLayout(jPanelDiagrTypeLayout);
        jPanelDiagrTypeLayout.setHorizontalGroup(
            jPanelDiagrTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelAxes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanelXaxis, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
        );
        jPanelDiagrTypeLayout.setVerticalGroup(
            jPanelDiagrTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelDiagrTypeLayout.createSequentialGroup()
                .addComponent(jPanelAxes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelXaxis, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabelYaxis.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelYaxis.setLabelFor(jPanelYaxInner);
        jLabelYaxis.setText("Y-axis:"); // NOI18N

        jPanelYmax.setLayout(new javax.swing.BoxLayout(jPanelYmax, javax.swing.BoxLayout.LINE_AXIS));

        jTextFieldYmax.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldYmax.setText("-999"); // NOI18N
        jTextFieldYmax.setName("Ymax"); // NOI18N
        jTextFieldYmax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldYmaxActionPerformed(evt);
            }
        });
        jTextFieldYmax.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldYmaxFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldYmaxFocusLost(evt);
            }
        });
        jTextFieldYmax.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldYmaxKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldYmaxKeyTyped(evt);
            }
        });
        jPanelYmax.add(jTextFieldYmax);

        jPanelYaxInner.setLayout(new java.awt.CardLayout());

        jComboBoxYaxType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "jComboBoxYaxType", "Item 2", "Item 3", "Item 4" }));
        jComboBoxYaxType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxYaxTypeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelYcomboLayout = new javax.swing.GroupLayout(jPanelYcombo);
        jPanelYcombo.setLayout(jPanelYcomboLayout);
        jPanelYcomboLayout.setHorizontalGroup(
            jPanelYcomboLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jComboBoxYaxType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanelYcomboLayout.setVerticalGroup(
            jPanelYcomboLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelYcomboLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jComboBoxYaxType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanelYaxInner.add(jPanelYcombo, "cardYcombo");

        jLabelYlogC.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabelYlogC.setText("log Concs."); // NOI18N

        javax.swing.GroupLayout jPanelYlogCLayout = new javax.swing.GroupLayout(jPanelYlogC);
        jPanelYlogC.setLayout(jPanelYlogCLayout);
        jPanelYlogCLayout.setHorizontalGroup(
            jPanelYlogCLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelYlogCLayout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(jLabelYlogC)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelYlogCLayout.setVerticalGroup(
            jPanelYlogCLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelYlogCLayout.createSequentialGroup()
                .addComponent(jLabelYlogC)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelYaxInner.add(jPanelYlogC, "cardYlogC");

        jLabelYlogA.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabelYlogA.setText("log Activities"); // NOI18N

        javax.swing.GroupLayout jPanelYlogALayout = new javax.swing.GroupLayout(jPanelYlogA);
        jPanelYlogA.setLayout(jPanelYlogALayout);
        jPanelYlogALayout.setHorizontalGroup(
            jPanelYlogALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelYlogALayout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(jLabelYlogA)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelYlogALayout.setVerticalGroup(
            jPanelYlogALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelYlogALayout.createSequentialGroup()
                .addComponent(jLabelYlogA)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelYaxInner.add(jPanelYlogA, "cardYlogA");

        jLabelYlogS.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabelYlogS.setText("log Solubilities"); // NOI18N

        javax.swing.GroupLayout jPanelYlogSLayout = new javax.swing.GroupLayout(jPanelYlogS);
        jPanelYlogS.setLayout(jPanelYlogSLayout);
        jPanelYlogSLayout.setHorizontalGroup(
            jPanelYlogSLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelYlogSLayout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(jLabelYlogS)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelYlogSLayout.setVerticalGroup(
            jPanelYlogSLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelYlogSLayout.createSequentialGroup()
                .addComponent(jLabelYlogS)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelYaxInner.add(jPanelYlogS, "cardYlogS");

        jLabelYRef.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabelYRef.setText("log {i}/{ref},  ref="); // NOI18N

        javax.swing.GroupLayout jPanelYRefLayout = new javax.swing.GroupLayout(jPanelYRef);
        jPanelYRef.setLayout(jPanelYRefLayout);
        jPanelYRefLayout.setHorizontalGroup(
            jPanelYRefLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelYRefLayout.createSequentialGroup()
                .addComponent(jLabelYRef)
                .addGap(0, 59, Short.MAX_VALUE))
        );
        jPanelYRefLayout.setVerticalGroup(
            jPanelYRefLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabelYRef, javax.swing.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
        );

        jPanelYaxInner.add(jPanelYRef, "cardYRef");

        jLabelYFract.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabelYFract.setText("Fraction for:"); // NOI18N

        javax.swing.GroupLayout jPanelYfractionLayout = new javax.swing.GroupLayout(jPanelYfraction);
        jPanelYfraction.setLayout(jPanelYfractionLayout);
        jPanelYfractionLayout.setHorizontalGroup(
            jPanelYfractionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelYfractionLayout.createSequentialGroup()
                .addComponent(jLabelYFract)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelYfractionLayout.setVerticalGroup(
            jPanelYfractionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabelYFract, javax.swing.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
        );

        jPanelYaxInner.add(jPanelYfraction, "cardYFract");

        jLabelYcalc.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabelYcalc.setText("calculated Eh"); // NOI18N

        javax.swing.GroupLayout jPanelYcalcLayout = new javax.swing.GroupLayout(jPanelYcalc);
        jPanelYcalc.setLayout(jPanelYcalcLayout);
        jPanelYcalcLayout.setHorizontalGroup(
            jPanelYcalcLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelYcalcLayout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(jLabelYcalc)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelYcalcLayout.setVerticalGroup(
            jPanelYcalcLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelYcalcLayout.createSequentialGroup()
                .addComponent(jLabelYcalc)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelYaxInner.add(jPanelYcalc, "cardYcalc");

        jLabelHaff.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabelHaff.setText("d(H-h) / d(-pH)"); // NOI18N

        javax.swing.GroupLayout jPanelYHaffLayout = new javax.swing.GroupLayout(jPanelYHaff);
        jPanelYHaff.setLayout(jPanelYHaffLayout);
        jPanelYHaffLayout.setHorizontalGroup(
            jPanelYHaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelYHaffLayout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(jLabelHaff)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelYHaffLayout.setVerticalGroup(
            jPanelYHaffLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelYHaffLayout.createSequentialGroup()
                .addComponent(jLabelHaff)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelYaxInner.add(jPanelYHaff, "cardYHaff");

        jPanelYaxComp.setPreferredSize(new java.awt.Dimension(132, 33));
        jPanelYaxComp.setLayout(new java.awt.CardLayout());

        jComboBoxYaxComp.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "jComboBxYaxC", "Item 2", "Item 3", "Item 4" }));
        jComboBoxYaxComp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxYaxCompActionPerformed(evt);
            }
        });
        jPanelYaxComp.add(jComboBoxYaxComp, "cardYaxComp1");

        javax.swing.GroupLayout jPanelEmptyYaxLayout = new javax.swing.GroupLayout(jPanelEmptyYax);
        jPanelEmptyYax.setLayout(jPanelEmptyYaxLayout);
        jPanelEmptyYaxLayout.setHorizontalGroup(
            jPanelEmptyYaxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 161, Short.MAX_VALUE)
        );
        jPanelEmptyYaxLayout.setVerticalGroup(
            jPanelEmptyYaxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );

        jPanelYaxComp.add(jPanelEmptyYax, "cardYaxComp0");

        jPanelYmin.setLayout(new javax.swing.BoxLayout(jPanelYmin, javax.swing.BoxLayout.LINE_AXIS));

        jTextFieldYmin.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextFieldYmin.setText("-999"); // NOI18N
        jTextFieldYmin.setName("Ymin"); // NOI18N
        jTextFieldYmin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldYminActionPerformed(evt);
            }
        });
        jTextFieldYmin.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldYminFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldYminFocusLost(evt);
            }
        });
        jTextFieldYmin.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldYminKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldYminKeyTyped(evt);
            }
        });
        jPanelYmin.add(jTextFieldYmin);

        javax.swing.GroupLayout jPanelYaxisLayout = new javax.swing.GroupLayout(jPanelYaxis);
        jPanelYaxis.setLayout(jPanelYaxisLayout);
        jPanelYaxisLayout.setHorizontalGroup(
            jPanelYaxisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelYaxisLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelYaxisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelYaxisLayout.createSequentialGroup()
                        .addComponent(jLabelYaxis)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelYmax, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanelYmin, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanelYaxisLayout.createSequentialGroup()
                        .addGroup(jPanelYaxisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanelYaxComp, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanelYaxInner, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        jPanelYaxisLayout.setVerticalGroup(
            jPanelYaxisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelYaxisLayout.createSequentialGroup()
                .addGroup(jPanelYaxisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelYaxis)
                    .addComponent(jPanelYmax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanelYaxInner, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jPanelYaxComp, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanelYmin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        jLabelXaxis.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelXaxis.setLabelFor(jComboBoxXaxType);
        jLabelXaxis.setText("X-axis:"); // NOI18N

        jComboBoxXaxType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "jComboBoxXaxType", "Item 2", "Item 3", "Item 4" }));
        jComboBoxXaxType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxXaxTypeActionPerformed(evt);
            }
        });

        jComboBoxXaxComp.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2" }));
        jComboBoxXaxComp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxXaxCompActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelXComponentLayout = new javax.swing.GroupLayout(jPanelXComponent);
        jPanelXComponent.setLayout(jPanelXComponentLayout);
        jPanelXComponentLayout.setHorizontalGroup(
            jPanelXComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelXComponentLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelXaxis)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelXComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jComboBoxXaxComp, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jComboBoxXaxType, 0, 156, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelXComponentLayout.setVerticalGroup(
            jPanelXComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelXComponentLayout.createSequentialGroup()
                .addGroup(jPanelXComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelXaxis)
                    .addComponent(jComboBoxXaxType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxXaxComp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3))
        );

        jPanelEmptyDiagram.setMinimumSize(new java.awt.Dimension(94, 50));
        jPanelEmptyDiagram.setPreferredSize(new java.awt.Dimension(94, 50));

        javax.swing.GroupLayout jPanelEmptyDiagramLayout = new javax.swing.GroupLayout(jPanelEmptyDiagram);
        jPanelEmptyDiagram.setLayout(jPanelEmptyDiagramLayout);
        jPanelEmptyDiagramLayout.setHorizontalGroup(
            jPanelEmptyDiagramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 94, Short.MAX_VALUE)
        );
        jPanelEmptyDiagramLayout.setVerticalGroup(
            jPanelEmptyDiagramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 50, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanelDiagramLayout = new javax.swing.GroupLayout(jPanelDiagram);
        jPanelDiagram.setLayout(jPanelDiagramLayout);
        jPanelDiagramLayout.setHorizontalGroup(
            jPanelDiagramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDiagramLayout.createSequentialGroup()
                .addGroup(jPanelDiagramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDiagramLayout.createSequentialGroup()
                        .addGap(129, 129, 129)
                        .addComponent(jPanelTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanelDiagramLayout.createSequentialGroup()
                        .addComponent(jPanelEmptyDiagram, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40)
                        .addComponent(jPanelXComponent, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanelDiagramLayout.createSequentialGroup()
                        .addComponent(jPanelYaxis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelDiagrType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanelDiagramLayout.setVerticalGroup(
            jPanelDiagramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDiagramLayout.createSequentialGroup()
                .addComponent(jPanelTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelDiagramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelDiagrType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanelYaxis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanelDiagramLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelEmptyDiagram, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanelXComponent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanelParams.setBorder(javax.swing.BorderFactory.createTitledBorder(null, " Parameters: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12), new java.awt.Color(153, 0, 153))); // NOI18N

        jLabelIS.setText("Ionic strength: ");

        buttonGroupActCoef.add(jRadioButtonFixed);
        jRadioButtonFixed.setSelected(true);
        jRadioButtonFixed.setText("fixed");
        jRadioButtonFixed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonFixedActionPerformed(evt);
            }
        });

        buttonGroupActCoef.add(jRadioButtonCalc);
        jRadioButtonCalc.setText("calculated");
        jRadioButtonCalc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonCalcActionPerformed(evt);
            }
        });

        jLabelIonicS.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelIonicS.setLabelFor(jTextFieldIonicStr);
        jLabelIonicS.setText("<html><u>I</u> = </html>"); // NOI18N
        jLabelIonicS.setToolTipText("Ionic strength =");

        jTextFieldIonicStr.setText("0.0"); // NOI18N
        jTextFieldIonicStr.setToolTipText("ionic strength value");
        jTextFieldIonicStr.setName("IonicStr"); // NOI18N
        jTextFieldIonicStr.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldIonicStrFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldIonicStrFocusLost(evt);
            }
        });
        jTextFieldIonicStr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldIonicStrActionPerformed(evt);
            }
        });
        jTextFieldIonicStr.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextFieldIonicStrKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldIonicStrKeyTyped(evt);
            }
        });

        jLabelM.setText("<html>mol/(kg H<sub>2</sub>O)</html>"); // NOI18N

        jLabelT.setLabelFor(jTextFieldT);
        jLabelT.setText("t ="); // NOI18N
        jLabelT.setToolTipText("Temperature =");
        jLabelT.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelTMouseClicked(evt);
            }
        });

        jTextFieldT.setText("25"); // NOI18N
        jTextFieldT.setToolTipText("temperature");
        jTextFieldT.setName("T"); // NOI18N
        jTextFieldT.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldTFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldTFocusLost(evt);
            }
        });
        jTextFieldT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTActionPerformed(evt);
            }
        });
        jTextFieldT.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextFieldTKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldTKeyTyped(evt);
            }
        });

        jLabelTC.setText("°C"); // NOI18N

        jPanelModel.setMinimumSize(new java.awt.Dimension(220, 136));
        jPanelModel.setPreferredSize(new java.awt.Dimension(220, 136));

        jLabelModel.setLabelFor(jComboBoxActCoeff);
        jLabelModel.setText("<html><u>A</u>ctivity coefficient model:</html>"); // NOI18N

        jComboBoxActCoeff.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Davies eqn.", "SIT (Specific Ion-interaction)", "simpl. Helgeson-Krikham-Flowers" }));
        jComboBoxActCoeff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxActCoeffActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelModelLayout = new javax.swing.GroupLayout(jPanelModel);
        jPanelModel.setLayout(jPanelModelLayout);
        jPanelModelLayout.setHorizontalGroup(
            jPanelModelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelModelLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanelModelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBoxActCoeff, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanelModelLayout.createSequentialGroup()
                        .addComponent(jLabelModel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        jPanelModelLayout.setVerticalGroup(
            jPanelModelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelModelLayout.createSequentialGroup()
                .addComponent(jLabelModel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxActCoeff, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanelActCoefLayout = new javax.swing.GroupLayout(jPanelActCoef);
        jPanelActCoef.setLayout(jPanelActCoefLayout);
        jPanelActCoefLayout.setHorizontalGroup(
            jPanelActCoefLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelActCoefLayout.createSequentialGroup()
                .addGroup(jPanelActCoefLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelActCoefLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addGroup(jPanelActCoefLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelActCoefLayout.createSequentialGroup()
                                .addComponent(jLabelIS)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jRadioButtonFixed)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jRadioButtonCalc))
                            .addGroup(jPanelActCoefLayout.createSequentialGroup()
                                .addGroup(jPanelActCoefLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabelIonicS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabelT, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanelActCoefLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanelActCoefLayout.createSequentialGroup()
                                        .addComponent(jTextFieldT, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabelTC))
                                    .addGroup(jPanelActCoefLayout.createSequentialGroup()
                                        .addComponent(jTextFieldIonicStr, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabelM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanelModel, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE))
                .addGap(4, 4, 4))
        );
        jPanelActCoefLayout.setVerticalGroup(
            jPanelActCoefLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelActCoefLayout.createSequentialGroup()
                .addGroup(jPanelActCoefLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelIS)
                    .addComponent(jRadioButtonFixed)
                    .addComponent(jRadioButtonCalc))
                .addGap(6, 6, 6)
                .addGroup(jPanelActCoefLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldIonicStr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelIonicS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelActCoefLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelTC)
                    .addComponent(jLabelT))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanelModel, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanelSedPredom.setPreferredSize(new java.awt.Dimension(230, 104));
        jPanelSedPredom.setLayout(new java.awt.CardLayout());

        jLabelPredNbr.setText("<html>Nbr. of calc. steps in each axis:</html>"); // NOI18N

        jLabelPredNbrP.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelPredNbrP.setLabelFor(jScrollBarPredNbrP);
        jLabelPredNbrP.setText("50"); // NOI18N
        jLabelPredNbrP.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelPredNbrPMouseClicked(evt);
            }
        });

        jScrollBarPredNbrP.setMaximum(spana.MainFrame.MXSTP+1);
        jScrollBarPredNbrP.setMinimum(spana.MainFrame.MNSTP);
        jScrollBarPredNbrP.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        jScrollBarPredNbrP.setVisibleAmount(1);
        jScrollBarPredNbrP.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollBarPredNbrP.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jScrollBarPredNbrPFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jScrollBarPredNbrPFocusLost(evt);
            }
        });
        jScrollBarPredNbrP.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                jScrollBarPredNbrPAdjustmentValueChanged(evt);
            }
        });

        jPanelTotCalcs.setBackground(new java.awt.Color(226, 225, 225));
        jPanelTotCalcs.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabelTotNbr.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelTotNbr.setText("<html><center>Total number<br>of calculations<br>in Predom:</center></html>"); // NOI18N

        jLabelNbrCalcs.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelNbrCalcs.setText("2500"); // NOI18N

        javax.swing.GroupLayout jPanelTotCalcsLayout = new javax.swing.GroupLayout(jPanelTotCalcs);
        jPanelTotCalcs.setLayout(jPanelTotCalcsLayout);
        jPanelTotCalcsLayout.setHorizontalGroup(
            jPanelTotCalcsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabelTotNbr, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanelTotCalcsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelNbrCalcs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelTotCalcsLayout.setVerticalGroup(
            jPanelTotCalcsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTotCalcsLayout.createSequentialGroup()
                .addComponent(jLabelTotNbr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jLabelNbrCalcs)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jCheckBoxAqu.setMnemonic('w');
        jCheckBoxAqu.setText("<html>sho<u>w</u> only aqueous species</html>"); // NOI18N
        jCheckBoxAqu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxAquActionPerformed(evt);
            }
        });

        jCheckBoxDrawPHline.setMnemonic('L');
        jCheckBoxDrawPHline.setText("draw neutral pH line");
        jCheckBoxDrawPHline.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxDrawPHlineActionPerformed(evt);
            }
        });

        jLabelSpaceP.setText("      ");

        javax.swing.GroupLayout jPanelPredomLayout = new javax.swing.GroupLayout(jPanelPredom);
        jPanelPredom.setLayout(jPanelPredomLayout);
        jPanelPredomLayout.setHorizontalGroup(
            jPanelPredomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelPredomLayout.createSequentialGroup()
                .addGroup(jPanelPredomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jCheckBoxAqu)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelPredomLayout.createSequentialGroup()
                        .addComponent(jCheckBoxDrawPHline, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabelSpaceP)
                        .addGap(0, 24, Short.MAX_VALUE))
                    .addGroup(jPanelPredomLayout.createSequentialGroup()
                        .addGroup(jPanelPredomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelPredomLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollBarPredNbrP, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanelPredomLayout.createSequentialGroup()
                                .addGap(49, 49, 49)
                                .addComponent(jLabelPredNbrP, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanelPredomLayout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(jLabelPredNbr, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanelTotCalcs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(4, 4, 4))
        );
        jPanelPredomLayout.setVerticalGroup(
            jPanelPredomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPredomLayout.createSequentialGroup()
                .addGroup(jPanelPredomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanelPredomLayout.createSequentialGroup()
                        .addComponent(jLabelPredNbr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(jLabelPredNbrP)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollBarPredNbrP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanelTotCalcs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxAqu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelPredomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxDrawPHline)
                    .addComponent(jLabelSpaceP)))
        );

        jPanelSedPredom.add(jPanelPredom, "Predom");

        jCheckBoxTableOut.setMnemonic('T');
        jCheckBoxTableOut.setText("<html>write a file with <u>t</u>able of results</html>"); // NOI18N
        jCheckBoxTableOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxTableOutActionPerformed(evt);
            }
        });

        jLabelSEDNbr.setText("Number of calculation steps:"); // NOI18N

        jLabelSEDNbrP.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelSEDNbrP.setLabelFor(jScrollBarSEDNbrP);
        jLabelSEDNbrP.setText("50"); // NOI18N
        jLabelSEDNbrP.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelSEDNbrPMouseClicked(evt);
            }
        });

        jScrollBarSEDNbrP.setMaximum(spana.MainFrame.MXSTP+1);
        jScrollBarSEDNbrP.setMinimum(spana.MainFrame.MNSTP);
        jScrollBarSEDNbrP.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        jScrollBarSEDNbrP.setVisibleAmount(1);
        jScrollBarSEDNbrP.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollBarSEDNbrP.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jScrollBarSEDNbrPFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jScrollBarSEDNbrPFocusLost(evt);
            }
        });
        jScrollBarSEDNbrP.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                jScrollBarSEDNbrPAdjustmentValueChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanelSEDLayout = new javax.swing.GroupLayout(jPanelSED);
        jPanelSED.setLayout(jPanelSEDLayout);
        jPanelSEDLayout.setHorizontalGroup(
            jPanelSEDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSEDLayout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addComponent(jLabelSEDNbrP, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE)
                .addGap(123, 123, 123))
            .addGroup(jPanelSEDLayout.createSequentialGroup()
                .addGroup(jPanelSEDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxTableOut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelSEDLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabelSEDNbr))
                    .addGroup(jPanelSEDLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jScrollBarSEDNbrP, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelSEDLayout.setVerticalGroup(
            jPanelSEDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSEDLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBoxTableOut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelSEDNbr)
                .addGap(2, 2, 2)
                .addComponent(jLabelSEDNbrP)
                .addGap(2, 2, 2)
                .addComponent(jScrollBarSEDNbrP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(33, Short.MAX_VALUE))
        );

        jPanelSedPredom.add(jPanelSED, "SED");

        javax.swing.GroupLayout jPanelEmptyParametersLayout = new javax.swing.GroupLayout(jPanelEmptyParameters);
        jPanelEmptyParameters.setLayout(jPanelEmptyParametersLayout);
        jPanelEmptyParametersLayout.setHorizontalGroup(
            jPanelEmptyParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 233, Short.MAX_VALUE)
        );
        jPanelEmptyParametersLayout.setVerticalGroup(
            jPanelEmptyParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 118, Short.MAX_VALUE)
        );

        jPanelSedPredom.add(jPanelEmptyParameters, "Empty");

        jCheckBoxRev.setMnemonic('R');
        jCheckBoxRev.setText("<html>allow <u>r</u>eversed conc. ranges</html>"); // NOI18N
        jCheckBoxRev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxRevActionPerformed(evt);
            }
        });

        jCheckBoxUseEh.setMnemonic('E');
        jCheckBoxUseEh.setText("<html>use <u>E</u>h for e-</html>"); // NOI18N
        jCheckBoxUseEh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxUseEhActionPerformed(evt);
            }
        });

        jLabelSpace.setText("      ");

        javax.swing.GroupLayout jPanelLowLayout = new javax.swing.GroupLayout(jPanelLow);
        jPanelLow.setLayout(jPanelLowLayout);
        jPanelLowLayout.setHorizontalGroup(
            jPanelLowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLowLayout.createSequentialGroup()
                .addGroup(jPanelLowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelLowLayout.createSequentialGroup()
                        .addComponent(jCheckBoxUseEh, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelSpace))
                    .addComponent(jCheckBoxRev, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelLowLayout.setVerticalGroup(
            jPanelLowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelLowLayout.createSequentialGroup()
                .addComponent(jCheckBoxRev, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelLowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxUseEh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelSpace)))
        );

        jButtonSaveDef.setMnemonic('S');
        jButtonSaveDef.setText("<html><u>S</u>ave as defaults</html>"); // NOI18N
        jButtonSaveDef.setMargin(new java.awt.Insets(0, 2, 0, 2));
        jButtonSaveDef.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveDefActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelParamsLayout = new javax.swing.GroupLayout(jPanelParams);
        jPanelParams.setLayout(jPanelParamsLayout);
        jPanelParamsLayout.setHorizontalGroup(
            jPanelParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelParamsLayout.createSequentialGroup()
                .addGroup(jPanelParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelParamsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButtonSaveDef, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelParamsLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addGroup(jPanelParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanelSedPredom, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanelActCoef, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanelLow, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(4, 4, 4))
        );
        jPanelParamsLayout.setVerticalGroup(
            jPanelParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelParamsLayout.createSequentialGroup()
                .addComponent(jPanelActCoef, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelSedPredom, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanelLow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSaveDef, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelConcs.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Concentrations:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12), new java.awt.Color(153, 0, 153))); // NOI18N
        jPanelConcs.setMinimumSize(new java.awt.Dimension(396, 156));
        jPanelConcs.setLayout(new java.awt.CardLayout());

        jScrollPaneCompConcList.setMinimumSize(new java.awt.Dimension(260, 132));

        jListCompConc.setModel(listCompConcModel);
        jListCompConc.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListCompConc.setToolTipText("Click on a line or press F2 to edit concentration"); // NOI18N
        jListCompConc.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListCompConcMouseClicked(evt);
            }
        });
        jListCompConc.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jListCompConcFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jListCompConcFocusLost(evt);
            }
        });
        jListCompConc.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jListCompConcKeyTyped(evt);
            }
        });
        jScrollPaneCompConcList.setViewportView(jListCompConc);

        jPanelConcs.add(jScrollPaneCompConcList, "panelCompConcList");

        jPanelGetConc.setMinimumSize(new java.awt.Dimension(260, 132));
        jPanelGetConc.setPreferredSize(new java.awt.Dimension(260, 132));

        jLabelEnterConc.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelEnterConc.setText("Enter concentration for component:"); // NOI18N

        jLabel_GetConcCompName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel_GetConcCompName.setText("SO4-2"); // NOI18N

        jComboBoxConcType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "jComboBoxConcType", "Item 2", "Item 3", "Item 4" }));
        jComboBoxConcType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxConcTypeActionPerformed(evt);
            }
        });

        jLabelEqual.setText("="); // NOI18N

        jLabelFrom.setText("from:"); // NOI18N

        jTextFieldCLow.setText("jTextFieldCLow"); // NOI18N
        jTextFieldCLow.setName("CLow"); // NOI18N
        jTextFieldCLow.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldCLowFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldCLowFocusLost(evt);
            }
        });
        jTextFieldCLow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldCLowActionPerformed(evt);
            }
        });
        jTextFieldCLow.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextFieldCLowKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldCLowKeyTyped(evt);
            }
        });

        jLabelTo.setText("to:"); // NOI18N

        jTextFieldCHigh.setText("jTextFieldCHigh"); // NOI18N
        jTextFieldCHigh.setName("CHigh"); // NOI18N
        jTextFieldCHigh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldCHighActionPerformed(evt);
            }
        });
        jTextFieldCHigh.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldCHighFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldCHighFocusLost(evt);
            }
        });
        jTextFieldCHigh.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextFieldCHighKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldCHighKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout jPanelConcInnerLayout = new javax.swing.GroupLayout(jPanelConcInner);
        jPanelConcInner.setLayout(jPanelConcInnerLayout);
        jPanelConcInnerLayout.setHorizontalGroup(
            jPanelConcInnerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelConcInnerLayout.createSequentialGroup()
                .addGroup(jPanelConcInnerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelConcInnerLayout.createSequentialGroup()
                        .addComponent(jComboBoxConcType, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelEqual))
                    .addGroup(jPanelConcInnerLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel_GetConcCompName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelConcInnerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldCLow, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelFrom))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelConcInnerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelTo)
                    .addComponent(jTextFieldCHigh, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelConcInnerLayout.setVerticalGroup(
            jPanelConcInnerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelConcInnerLayout.createSequentialGroup()
                .addGroup(jPanelConcInnerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel_GetConcCompName)
                    .addComponent(jLabelFrom)
                    .addComponent(jLabelTo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelConcInnerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxConcType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelEqual)
                    .addComponent(jTextFieldCLow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldCHigh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jButtonGetConcOK.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButtonGetConcOK.setMnemonic('O');
        jButtonGetConcOK.setText("<html><u>O</u>K</html>"); // NOI18N
        jButtonGetConcOK.setToolTipText("Alt-O"); // NOI18N
        jButtonGetConcOK.setMargin(new java.awt.Insets(2, 10, 2, 10));
        jButtonGetConcOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGetConcOKActionPerformed(evt);
            }
        });

        jButtonGetConcCancel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButtonGetConcCancel.setMnemonic('C');
        jButtonGetConcCancel.setText("<html><u>C</u>ancel</html>"); // NOI18N
        jButtonGetConcCancel.setToolTipText("Alt-C"); // NOI18N
        jButtonGetConcCancel.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonGetConcCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGetConcCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelGetConcLayout = new javax.swing.GroupLayout(jPanelGetConc);
        jPanelGetConc.setLayout(jPanelGetConcLayout);
        jPanelGetConcLayout.setHorizontalGroup(
            jPanelGetConcLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelGetConcLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelGetConcLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanelConcInner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanelGetConcLayout.createSequentialGroup()
                        .addComponent(jLabelEnterConc)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonGetConcOK, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonGetConcCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanelGetConcLayout.setVerticalGroup(
            jPanelGetConcLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelGetConcLayout.createSequentialGroup()
                .addGroup(jPanelGetConcLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonGetConcOK, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonGetConcCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelEnterConc))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelConcInner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanelConcs.add(jPanelGetConc, "panelGetConc");

        jLabl1.setText("jLabl1"); // NOI18N

        jLabl2.setText("jLabl2"); // NOI18N

        jLabl3.setText("jLabl3"); // NOI18N

        jLabl4.setText("jLabl4"); // NOI18N

        javax.swing.GroupLayout jPanelLablLayout = new javax.swing.GroupLayout(jPanelLabl);
        jPanelLabl.setLayout(jPanelLablLayout);
        jPanelLablLayout.setHorizontalGroup(
            jPanelLablLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLablLayout.createSequentialGroup()
                .addGroup(jPanelLablLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabl2)
                    .addComponent(jLabl3)
                    .addComponent(jLabl4)
                    .addComponent(jLabl1))
                .addGap(356, 356, 356))
        );
        jPanelLablLayout.setVerticalGroup(
            jPanelLablLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLablLayout.createSequentialGroup()
                .addComponent(jLabl1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabl2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabl3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabl4)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelConcs.add(jPanelLabl, "panelJLabl");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanelFileNames, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelConcs, 0, 0, Short.MAX_VALUE)
                    .addComponent(jPanelDiagram, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelParams, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelFileNames, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelDiagram, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelConcs, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addComponent(jPanelParams, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

//<editor-fold defaultstate="collapsed" desc="Events">
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        quitFrame();
    }//GEN-LAST:event_formWindowClosing

    private void jButton_OKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_OKActionPerformed
      // --- this will call "runSedPredom()"
      cancel = true;
      double w = readTextField(jTextFieldXmax);
      String t = Util.formatNum(w);
      if(Double.parseDouble(t) != w) {jTextFieldXmax.setText(t);}
      w = readTextField(jTextFieldXmin);
      t = Util.formatNum(w);
      if(Double.parseDouble(t) != w) {jTextFieldXmin.setText(t);}
      w = readTextField(jTextFieldYmax);
      t = Util.formatNum(w);
      if(Double.parseDouble(t) != w) {jTextFieldYmax.setText(t);}
      w = readTextField(jTextFieldYmin);
      t = Util.formatNum(w);
      if(Double.parseDouble(t) != w) {jTextFieldYmin.setText(t);}
      updateConcs();
      if(dataFile == null || dataFile.getPath().length() <=0) {
        MsgExceptn.exception("Error in \"Select_Diagram\": the data file name is empty.");
      } else {
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        MainFrame.getInstance().setCursorWait();
        if(checkChanges()) {
            // make the calculations and draw the diagram
            boolean ok = runSedPredom();
            Thread wt = new Thread() {@Override public void run(){
              try{Thread.sleep(2000);}   //show the "wait" cursor for 2 sec
              catch (InterruptedException e) {}
            }};//new Thread
            wt.start();
            if(ok) {cancel = false; quitFrame();}
        } // if there was an error while saving the file(s): return
      }
      setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_jButton_OKActionPerformed

    private void jButton_CancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_CancelActionPerformed
        cancel = true;
        quitFrame();
    }//GEN-LAST:event_jButton_CancelActionPerformed

    private void jTextFieldDataFileFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldDataFileFocusGained
        jTextFieldDataFile.selectAll();
    }//GEN-LAST:event_jTextFieldDataFileFocusGained

    private void jTextFieldDataFileKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldDataFileKeyPressed
        if(!Util.isKeyPressedOK(evt)) {evt.consume();}
    }//GEN-LAST:event_jTextFieldDataFileKeyPressed

    private void jTextFieldDataFileKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldDataFileKeyTyped
        char c = Character.toUpperCase(evt.getKeyChar());
        if(evt.getKeyChar() != java.awt.event.KeyEvent.VK_ESCAPE &&
           !(evt.isAltDown() && ((c == 'X') || (c == 'A') ||
                                 (c == 'H') ||
                 (evt.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER))
                 ) //isAltDown
                 ) { // if not ESC or Alt-something
                evt.consume(); // remove the typed key
                dataFile_Click();
        } // if char ok
    }//GEN-LAST:event_jTextFieldDataFileKeyTyped

    private void jTextFieldDataFileMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextFieldDataFileMouseClicked
        dataFile_Click();
    }//GEN-LAST:event_jTextFieldDataFileMouseClicked

    private void jTextFieldDataFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldDataFileActionPerformed
        dataFile_Click();
    }//GEN-LAST:event_jTextFieldDataFileActionPerformed

   private void jScrollBarPredNbrPFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScrollBarPredNbrPFocusGained
        jScrollBarPredNbrP.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0,0,0)));
    }//GEN-LAST:event_jScrollBarPredNbrPFocusGained

    private void jScrollBarPredNbrPFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScrollBarPredNbrPFocusLost
        jScrollBarPredNbrP.setBorder(scrollBorder);
    }//GEN-LAST:event_jScrollBarPredNbrPFocusLost

    private void jScrollBarSEDNbrPFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScrollBarSEDNbrPFocusGained
        jScrollBarSEDNbrP.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0,0,0)));
    }//GEN-LAST:event_jScrollBarSEDNbrPFocusGained

    private void jScrollBarSEDNbrPFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScrollBarSEDNbrPFocusLost
        jScrollBarSEDNbrP.setBorder(scrollBorder);
    }//GEN-LAST:event_jScrollBarSEDNbrPFocusLost

    private void jScrollBarPredNbrPAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_jScrollBarPredNbrPAdjustmentValueChanged
        int nStep = (int)((float)jScrollBarPredNbrP.getValue()/1f);
        jLabelPredNbrP.setText(String.valueOf(nStep));
        jLabelNbrCalcs.setText(String.valueOf((nStep+1)*(nStep+1)));
    }//GEN-LAST:event_jScrollBarPredNbrPAdjustmentValueChanged

    private void jScrollBarSEDNbrPAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_jScrollBarSEDNbrPAdjustmentValueChanged
        int nStep = (int)((float)jScrollBarSEDNbrP.getValue()/1f);
        jLabelSEDNbrP.setText(String.valueOf(nStep));
    }//GEN-LAST:event_jScrollBarSEDNbrPAdjustmentValueChanged

    private void jCheckBoxAquActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxAquActionPerformed
        runAqu = jCheckBoxAqu.isSelected();
    }//GEN-LAST:event_jCheckBoxAquActionPerformed

    private void jCheckBoxRevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxRevActionPerformed
        runRevs = jCheckBoxRev.isSelected();
    }//GEN-LAST:event_jCheckBoxRevActionPerformed

    private void jTextFieldIonicStrFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldIonicStrFocusLost
        validateIonicStrength();
    }//GEN-LAST:event_jTextFieldIonicStrFocusLost

    private void jTextFieldIonicStrKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldIonicStrKeyPressed
        if(evt.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER) {
            validateIonicStrength();}
    }//GEN-LAST:event_jTextFieldIonicStrKeyPressed

    private void jTextFieldIonicStrKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldIonicStrKeyTyped
        char key = evt.getKeyChar();
        if(!isCharOKforNumberInput(key)) {evt.consume();}
    }//GEN-LAST:event_jTextFieldIonicStrKeyTyped

    private void jTextFieldTKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldTKeyTyped
        char key = evt.getKeyChar();
        if(!isCharOKforNumberInput(key)) {evt.consume();}
    }//GEN-LAST:event_jTextFieldTKeyTyped

    private void jComboBoxDiagTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxDiagTypeActionPerformed
        if(jComboBoxDiagType.getSelectedIndex() >=0) {
            diagramType_Click();
        }
    }//GEN-LAST:event_jComboBoxDiagTypeActionPerformed

    private void jTextFieldXminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldXminActionPerformed
        if(loading || updatingAxes) {return;}
        focusLostXmin();
    }//GEN-LAST:event_jTextFieldXminActionPerformed

    private void jTextFieldXmaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldXmaxActionPerformed
        if(loading || updatingAxes) {return;}
        focusLostXmax();
    }//GEN-LAST:event_jTextFieldXmaxActionPerformed

    private void jTextFieldYmaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldYmaxActionPerformed
        if(loading || updatingAxes) {return;}
        focusLostYmax();
    }//GEN-LAST:event_jTextFieldYmaxActionPerformed

    private void jTextFieldXminKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldXminKeyTyped
        char key = evt.getKeyChar();
        if(!isCharOKforNumberInput(key)) {evt.consume();}
    }//GEN-LAST:event_jTextFieldXminKeyTyped

    private void jTextFieldXminFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldXminFocusLost
        if(loading) {return;}
        focusLostXmin();
    }//GEN-LAST:event_jTextFieldXminFocusLost

    private void jTextFieldXmaxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldXmaxFocusLost
        if(loading) {return;}
        focusLostXmax();
    }//GEN-LAST:event_jTextFieldXmaxFocusLost

    private void jTextFieldYmaxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldYmaxFocusLost
        if(loading) {return;}
        focusLostYmax();
    }//GEN-LAST:event_jTextFieldYmaxFocusLost

    private void jTextFieldXminFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldXminFocusGained
        oldTextXmin = jTextFieldXmin.getText();
        jTextFieldXmin.selectAll();
    }//GEN-LAST:event_jTextFieldXminFocusGained

    private void jTextFieldXmaxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldXmaxFocusGained
        oldTextXmax = jTextFieldXmax.getText();
        jTextFieldXmax.selectAll();
    }//GEN-LAST:event_jTextFieldXmaxFocusGained

    private void jTextFieldYmaxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldYmaxFocusGained
        oldTextYmax = jTextFieldYmax.getText();
        jTextFieldYmax.selectAll();
    }//GEN-LAST:event_jTextFieldYmaxFocusGained

    private void jComboBoxConcTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxConcTypeActionPerformed
        if(getConcLoading) {return;}
        getConc_ComboConcType_Click();
    }//GEN-LAST:event_jComboBoxConcTypeActionPerformed

    private void jTextFieldCLowKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldCLowKeyTyped
        char key = evt.getKeyChar();
        if(!isCharOKforNumberInput(key)) {evt.consume();}
    }//GEN-LAST:event_jTextFieldCLowKeyTyped

    private void jTextFieldCHighKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldCHighKeyTyped
        char key = evt.getKeyChar();
        if(evt.getKeyCode()==java.awt.event.KeyEvent.VK_ENTER) {jButtonGetConcOK.doClick(); evt.consume();}
        else if(!isCharOKforNumberInput(key)) {evt.consume();}
    }//GEN-LAST:event_jTextFieldCHighKeyTyped

    private void jTextFieldDiagNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldDiagNameFocusGained
        jTextFieldDiagName.selectAll();
    }//GEN-LAST:event_jTextFieldDiagNameFocusGained

    private void jListCompConcKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jListCompConcKeyTyped
        char c = Character.toUpperCase(evt.getKeyChar());
        if(evt.getKeyChar() != java.awt.event.KeyEvent.VK_ESCAPE &&
           !(evt.isAltDown() && ((c == 'X') || (c == 'A') ||
                                 (c == 'H') || (c == 'M') ||
                 (evt.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER))
                 ) //isAltDown
                 ) { // if not ESC or Alt-something
                evt.consume(); // remove the typed key
                compConcList_Click(jListCompConc.getSelectedIndex());
        } // if char ok
    }//GEN-LAST:event_jListCompConcKeyTyped

    private void jListCompConcMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListCompConcMouseClicked
        java.awt.Point p = evt.getPoint();
        int i = jListCompConc.locationToIndex(p);
        if(i>=0) {
            java.awt.Rectangle r = jListCompConc.getCellBounds(i, i);
            if(p.y < r.y || p.y > r.y+r.height) {return;}
            jListCompConc.setSelectedIndex(i);
            compConcList_Click(i);
        }
    }//GEN-LAST:event_jListCompConcMouseClicked

    private void jListCompConcFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListCompConcFocusGained
        if(jListCompConc.isFocusOwner()) {jScrollPaneCompConcList.setBorder(highlightedBorder);}
    }//GEN-LAST:event_jListCompConcFocusGained

    private void jListCompConcFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListCompConcFocusLost
        if(!jListCompConc.isFocusOwner()) {jScrollPaneCompConcList.setBorder(defBorder);}
        jListCompConc.clearSelection();
    }//GEN-LAST:event_jListCompConcFocusLost

    private void jButtonGetConcCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGetConcCancelActionPerformed
        getConc_Unload();
    }//GEN-LAST:event_jButtonGetConcCancelActionPerformed

    private void jButtonGetConcOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGetConcOKActionPerformed
      if(jComboBoxConcType.getSelectedItem().toString().startsWith("?")) {
        MsgExceptn.exception("Programming error in \"jButtonGetConcOK\" - jComboBoxConcType = \"?...\"");
        getConc_Unload(); return;
      }
      if(getConc_idx < 0) {getConc_Unload(); return;}
      int use_pHpe = 0;
      String concType = jComboBoxConcType.getSelectedItem().toString();
      int jc = -1;
      for(int j=0; j < concTypes.length; j++) {
        if(concTypes[j].equals(concType)) {
          jc = j+1;
          if(j>=3) {
            if(getConc_idx == hPresent) {use_pHpe =1;}
            else if(getConc_idx == ePresent) {use_pHpe =2; if(runUseEh) {use_pHpe =3;}}
            jc = j+1 - use_pHpe * 2;
            if(j == 11) {jc = 4;} // "log P"
            if(j == 12) {jc = 5;} // "log P varied"
          } //if j>4
          dgrC.hur[getConc_idx] = jc;
          break;
        }
      } //for j
      if(jc < 0) {MsgExceptn.exception("Programming error in \"jButtonGetConcOK\" - jc<0"); getConc_Unload();}
      double fEh = Double.NaN;
      if(use_pHpe == 3) {
            fEh = (MainFrame.Rgas * ln10 * (diag.temperature +273.15)) / MainFrame.Faraday;
      }
      double w;
      w = readTextField(jTextFieldCLow);
      if(use_pHpe ==1 || use_pHpe ==2) {w = -w;}
      if(use_pHpe == 3) {w = -w / fEh;}
      dgrC.cLow[getConc_idx] = w;
      if(concType.toLowerCase().contains("varied")) {
            w = readTextField(jTextFieldCHigh);
            if(use_pHpe ==1 || use_pHpe ==2) {w = -w;}
            if(use_pHpe == 3) {w = -w / fEh;}
            dgrC.cHigh[getConc_idx] = w;
      } //if varied
      getConc_Unload();
    }//GEN-LAST:event_jButtonGetConcOKActionPerformed

    private void jTextFieldIonicStrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldIonicStrActionPerformed
        validateIonicStrength();
    }//GEN-LAST:event_jTextFieldIonicStrActionPerformed

    private void jTextFieldTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTActionPerformed
        validateTemperature();
    }//GEN-LAST:event_jTextFieldTActionPerformed

    private void jTextFieldIonicStrFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldIonicStrFocusGained
        oldTextI = jTextFieldIonicStr.getText();
        jTextFieldIonicStr.selectAll();
    }//GEN-LAST:event_jTextFieldIonicStrFocusGained

    private void jTextFieldTFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldTFocusGained
        oldTextT = jTextFieldT.getText();
        jTextFieldT.selectAll();
    }//GEN-LAST:event_jTextFieldTFocusGained

    private void jTextFieldTKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldTKeyPressed
        if(evt.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER) {
            validateTemperature();
        }
    }//GEN-LAST:event_jTextFieldTKeyPressed

    private void jTextFieldTFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldTFocusLost
        validateTemperature();
    }//GEN-LAST:event_jTextFieldTFocusLost

    private void jLabelSEDNbrPMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelSEDNbrPMouseClicked
        jScrollBarSEDNbrP.setValue(MainFrame.NSTEPS_DEF);
    }//GEN-LAST:event_jLabelSEDNbrPMouseClicked

    private void jLabelPredNbrPMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelPredNbrPMouseClicked
        jScrollBarPredNbrP.setValue(MainFrame.NSTEPS_DEF);
    }//GEN-LAST:event_jLabelPredNbrPMouseClicked

    private void jCheckBoxTableOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxTableOutActionPerformed
        runTbl = jCheckBoxTableOut.isSelected();
    }//GEN-LAST:event_jCheckBoxTableOutActionPerformed

    private void jLabelTMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelTMouseClicked
        if(jTextFieldT.isEnabled()) {jTextFieldT.setText("25");}
    }//GEN-LAST:event_jLabelTMouseClicked

    private void jCheckBoxUseEhActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxUseEhActionPerformed
        if(pc.dbg) {System.out.println("jCheckBoxUseEh_Action_Performed");}
        runUseEh = jCheckBoxUseEh.isSelected();
        int oldDiagType = jComboBoxDiagType.getSelectedIndex();
        String dt = jComboBoxDiagType.getSelectedItem().toString();
        boolean old = diagramType_doNothing;
        diagramType_doNothing = true;
        if(runUseEh) {
          // change  "calculated pe"  to  "calculated Eh"
          for(int j=0; j < jComboBoxDiagType.getItemCount(); j++) {
            if(jComboBoxDiagType.getItemAt(j).toString().equalsIgnoreCase("calculated pe")) {
              jComboBoxDiagType.removeItemAt(j);
              jComboBoxDiagType.insertItemAt("Calculated Eh", j);
              break;
            }
          } //for j
          if(jLabelYcalc.getText().equalsIgnoreCase("calculated pe")) {
            jLabelYcalc.setText("calculated Eh");
          }
        } else {
          //change  "calculated Eh"  to  "calculated pe"
          for(int j=0; j < jComboBoxDiagType.getItemCount(); j++) {
            if(jComboBoxDiagType.getItemAt(j).toString().equalsIgnoreCase("calculated Eh")) {
              jComboBoxDiagType.removeItemAt(j);
              jComboBoxDiagType.insertItemAt("Calculated pe", j);
              break;
            }
          } //for j
          if(jLabelYcalc.getText().equalsIgnoreCase("calculated Eh")) {
            jLabelYcalc.setText("calculated pe");
          }
        } //if not selected
        diagramType_doNothing = old;
        if(oldDiagType >=0) {jComboBoxDiagType.setSelectedIndex(oldDiagType);}
        if(dt.equalsIgnoreCase("calculated Eh")) {
            double fEh = (MainFrame.Rgas * ln10 * (diag.temperature +273.15)) / MainFrame.Faraday;
            double w1 = readTextField(jTextFieldYmin);
            double w2 = readTextField(jTextFieldYmax);
            if(runUseEh) { // change  "calculated pe"  to  "calculated Eh"
                w1 = -w1 *fEh;  w2 = -w2 *fEh;
            } else { // change  "calculated Eh"  to  "calculated pe"
                w1 = -w1 /fEh;  w2 = -w2 /fEh;
            }
            jTextFieldYmin.setText(Util.formatDbl3(Math.round(w1)));
            jTextFieldYmax.setText(Util.formatDbl3(Math.round(w2)));
            resizeYMin(); resizeYMax();
        }
        enableTemperature();
        updateAxes();
        resizeXMinMax(); resizeYMin(); resizeYMin();
        updateConcList();
    }//GEN-LAST:event_jCheckBoxUseEhActionPerformed

    private void jButtonSaveDefActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveDefActionPerformed
      pd.SED_nbrSteps = (int)((float)jScrollBarSEDNbrP.getValue()/1f);
      pd.Predom_nbrSteps = (int)((float)jScrollBarPredNbrP.getValue()/1f);
      pd.SED_tableOutput = jCheckBoxTableOut.isSelected();
      pd.ionicStrength = diag.ionicStrength;
      if(jTextFieldT.isEnabled()) {pd.temperature = diag.temperature;}
      pd.pressure = diag.pressure;
      pd.aquSpeciesOnly = jCheckBoxAqu.isSelected();
      pd.reversedConcs = jCheckBoxRev.isSelected();
      pd.drawNeutralPHinPourbaix = jCheckBoxDrawPHline.isSelected();
      pd.useEh = jCheckBoxUseEh.isSelected();
      pd.actCoeffsMethod = jComboBoxActCoeff.getSelectedIndex();
    }//GEN-LAST:event_jButtonSaveDefActionPerformed

    private void jTextFieldTitleFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldTitleFocusGained
        jTextFieldTitle.selectAll();
    }//GEN-LAST:event_jTextFieldTitleFocusGained

    private void jButton_HelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_HelpActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        Thread hlp = new Thread() {@Override public void run(){
            String[] a = {"S_Making_Diagrams_htm"};
            lib.huvud.RunProgr.runProgramInProcess(Select_Diagram.this,ProgramConf.HELP_JAR,a,false,pc.dbg,pc.pathAPP);
            try{Thread.sleep(2000);}   //show the "wait" cursor for 2 sec
            catch (InterruptedException e) {}
            setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }};//new Thread
        hlp.start();
    }//GEN-LAST:event_jButton_HelpActionPerformed

    private void jTextFieldXmaxKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldXmaxKeyTyped
        char key = evt.getKeyChar();
        if(!isCharOKforNumberInput(key)) {evt.consume();}
    }//GEN-LAST:event_jTextFieldXmaxKeyTyped

    private void jTextFieldYmaxKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldYmaxKeyTyped
        char key = evt.getKeyChar();
        if(!isCharOKforNumberInput(key)) {evt.consume();}
    }//GEN-LAST:event_jTextFieldYmaxKeyTyped

    private void jComboBoxActCoeffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxActCoeffActionPerformed
        runActCoeffsMethod = jComboBoxActCoeff.getSelectedIndex();
    }//GEN-LAST:event_jComboBoxActCoeffActionPerformed

    private void jTextFieldYminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldYminActionPerformed
        if(loading || updatingAxes) {return;}
        focusLostYmin();
    }//GEN-LAST:event_jTextFieldYminActionPerformed

    private void jTextFieldYminFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldYminFocusGained
        oldTextYmin = jTextFieldYmin.getText();
        jTextFieldYmin.selectAll();
    }//GEN-LAST:event_jTextFieldYminFocusGained

    private void jTextFieldYminFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldYminFocusLost
        if(loading) {return;}
        focusLostYmin();
    }//GEN-LAST:event_jTextFieldYminFocusLost

    private void jTextFieldYminKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldYminKeyTyped
        char key = evt.getKeyChar();
        if(!isCharOKforNumberInput(key)) {evt.consume();}
    }//GEN-LAST:event_jTextFieldYminKeyTyped

    private void jTextFieldYminKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldYminKeyReleased
        resizeYMin();
    }//GEN-LAST:event_jTextFieldYminKeyReleased

    private void jTextFieldYmaxKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldYmaxKeyReleased
        resizeYMax();
    }//GEN-LAST:event_jTextFieldYmaxKeyReleased

    private void jTextFieldXminKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldXminKeyReleased
        resizeXMinMax();
    }//GEN-LAST:event_jTextFieldXminKeyReleased

    private void jTextFieldXmaxKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldXmaxKeyReleased
        resizeXMinMax();
    }//GEN-LAST:event_jTextFieldXmaxKeyReleased

    private void jComboBoxMainCompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxMainCompActionPerformed
      if(loading || updatingAxes) {return;}
      if(pc.dbg) {System.out.println("jComboBoxMainComp_Action_Performed");}
      // --- Get the new compMain
      for(int ic = 0; ic < cs.Na; ic++) {
          if(jComboBoxMainComp.getSelectedItem().toString().
                equalsIgnoreCase(namn.identC[ic])) {diag.compMain = ic; break;}
      } // for ic

      // ---- if Main-component does not belong to an axis,
      //      set the concentration to not varied
      if(diag.compMain != diag.compX && diag.compMain != diag.compY) {
        if(dgrC.hur[diag.compMain] ==2 || dgrC.hur[diag.compMain] ==3 ||
           dgrC.hur[diag.compMain] ==5) { //if "*V" (conc varied)
            // set a concentration-type not varied
            if(Util.isGas(namn.identC[diag.compMain])) {
                dgrC.hur[diag.compMain] = 4; //"LA" = "log P"
            } else { //not Gas
                if(dgrC.hur[diag.compMain] ==2 || dgrC.hur[diag.compMain] ==3) { //"TV" or "LTV"
                  if(dgrC.hur[diag.compMain] ==3) { //"LTV"
                    dgrC.cLow[diag.compMain] = logToNoLog(dgrC.cLow[diag.compMain]);
                    dgrC.cHigh[diag.compMain] = logToNoLog(dgrC.cHigh[diag.compMain]);
                  } // "LTV"
                  dgrC.hur[diag.compMain] = 1; //"T"
                } //if "TV" or "LTV"
                else { //"LAV"
                  dgrC.hur[diag.compMain] = 4; //"LA"
                } //"T"?
            } //not Gas
        } //if conc varied
      } //if Main comp. not in axis (if Main != compX & Main != compY)

      changingComp = true;
      updateAxes();
      changingComp = false;
      updateUseEhCheckBox();
      updateDrawPHlineBox();
      enableTemperature();
    }//GEN-LAST:event_jComboBoxMainCompActionPerformed

    private void jComboBoxXaxTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxXaxTypeActionPerformed
        if(loading || changingComp || updatingAxes) {return;}
        if(pc.dbg) {System.out.println("jComboBoxXaxType_Action_performed");}
        if(jPanelGetConc.isShowing()) {jButtonGetConcCancel.doClick();}
        String xAxisType = jComboBoxXaxType.getSelectedItem().toString();
            comboBoxAxisType(xAxisType, xAxisType0, jTextFieldXmin, jTextFieldXmax);
            xAxisType0 = xAxisType;
        resizeXMinMax();
        updateConcs();
        updateConcList();
        updateUseEhCheckBox();
        updateDrawPHlineBox();
        enableTemperature();
    }//GEN-LAST:event_jComboBoxXaxTypeActionPerformed

    private void jComboBoxXaxCompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxXaxCompActionPerformed
      if(loading) {return;}
      if(jPanelGetConc.isShowing()) {jButtonGetConcCancel.doClick();}
      int iX;
      boolean predomDgr = jComboBoxDiagType.getSelectedItem().toString().equals("Predominance Area");
      if(diag.compX >=0) {
        // Change the concentration of the component that was in the X-axis:
        iX = diag.compX;
        if(!jComboBoxXaxComp.getSelectedItem().toString().equals(namn.identC[iX]) &&
                (!predomDgr || iX != diag.compY)) {
            if(iX == ePresent &&
               (dgrC.hur[iX] !=1 && dgrC.hur[iX] !=2 && dgrC.hur[iX] !=3)) { //not "T"
                dgrC.cLow[iX] = -8.5;
                dgrC.hur[iX] = 4; // "LA" = "pe"
            } else if(iX == hPresent &&
               (dgrC.hur[iX] !=1 && dgrC.hur[iX] !=2 && dgrC.hur[iX] !=3)) { //not "T"
                dgrC.cLow[iX] = -7;
                dgrC.hur[iX] = 4; // "LA" = "pH"
            } else if(Util.isWater(namn.identC[iX])) {
                dgrC.cLow[iX] = 0;
                dgrC.hur[iX] = 4; // "LA"
            } else if(dgrC.hur[iX] ==1 || dgrC.hur[iX] ==2 || dgrC.hur[iX] ==3) { //"T"
                if(dgrC.hur[iX] ==3) { // "LTV"
                    dgrC.cLow[iX] = logToNoLog(dgrC.cLow[iX]);
                    dgrC.cHigh[iX] = logToNoLog(dgrC.cHigh[iX]);
                } // "LTV"
                dgrC.hur[iX] = 1; // "T"
                if(dgrC.cLow[iX] == 0 && dgrC.cHigh[iX] !=0 && !Double.isNaN(dgrC.cHigh[iX])) {dgrC.cLow[iX] = dgrC.cHigh[iX];}
            } else {
                dgrC.hur[iX] = 4; // "LA"
                if(Util.isGas(namn.identC[iX])) {dgrC.cLow[iX] = -3.5;}
            }
        } // if there was a component change
      } // compX >=0

      // --- Get the new compX
      for(int ic = 0; ic < cs.Na; ic++) {
          if(jComboBoxXaxComp.getSelectedItem().toString().
                equalsIgnoreCase(namn.identC[ic])) {diag.compX = ic; break;}
      } // for ic
      // -- for a Predom diagram: the concentrations in the axis
      //    must be log-scale: change concentration if needed
      iX = diag.compX;
      if(predomDgr && (dgrC.hur[iX] ==1 || dgrC.hur[iX] ==2)) { //"T" or "TV"
          // "hur[iX]" will be changed in updateAxes()
      }

      changingComp = true;
      updateAxes();
      changingComp = false;
      resizeXMinMax();
      updateConcList();
      updateUseEhCheckBox();
      updateDrawPHlineBox();
      enableTemperature();
    }//GEN-LAST:event_jComboBoxXaxCompActionPerformed

    private void jComboBoxYaxTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxYaxTypeActionPerformed
        if(loading || changingComp || updatingAxes) {return;}
        if(pc.dbg) {System.out.println("jComboBoxYaxType_Action_performed");}
        if(jPanelGetConc.isShowing()) {jButtonGetConcCancel.doClick();}
        String yAxisType = jComboBoxYaxType.getSelectedItem().toString();
            comboBoxAxisType(yAxisType, yAxisType0, jTextFieldYmin, jTextFieldYmax);
            yAxisType0 = yAxisType;
        resizeYMin(); resizeYMax();
        updateConcs();
        updateUseEhCheckBox();
        updateDrawPHlineBox();
        enableTemperature();
    }//GEN-LAST:event_jComboBoxYaxTypeActionPerformed

    private void jComboBoxYaxCompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxYaxCompActionPerformed
      if(loading || updatingAxes) {return;}
      if(pc.dbg) {System.out.println("jComboBoxYaxComp_Action_Performed");}
      if(jPanelGetConc.isShowing()) {jButtonGetConcCancel.doClick();}
      if(jComboBoxDiagType.getSelectedItem().toString().equals("Predominance Area")) {
        //Change the concentration of the component that was in the Y-axis:
        if(diag.compY >=0) {
          int iY = diag.compY;
          if(!jComboBoxYaxComp.getSelectedItem().toString().equals(namn.identC[iY])
                && (iY != diag.compX)) {
            if(iY == ePresent &&
                    dgrC.hur[iY] !=1 && dgrC.hur[iY] !=2 && dgrC.hur[iY] !=3) { //not "T"
                dgrC.cLow[iY] = -8.5;
                dgrC.hur[iY] = 4; // "LA" = "pe"
            } else if(iY == hPresent &&
                    dgrC.hur[iY] !=1 && dgrC.hur[iY] !=2 && dgrC.hur[iY] !=3) { //not "T"
                dgrC.cLow[iY] = -7;
                dgrC.hur[iY] = 4; // "LA" = "pH"
            } else if(Util.isWater(namn.identC[iY])) {
                dgrC.cLow[iY] = 0;
                dgrC.hur[iY] = 4; // "LA"
            } else if(dgrC.hur[iY] ==1 || dgrC.hur[iY] ==2 || dgrC.hur[iY] ==3) { //"T", "TV", "LTV"
                if(dgrC.hur[iY] ==3) { // "LTV"
                    dgrC.cLow[iY] = logToNoLog(dgrC.cLow[iY]);
                    dgrC.cHigh[iY] = logToNoLog(dgrC.cHigh[iY]);
                } // "LT"
                dgrC.hur[iY] = 1; // "T"
            } else {
                dgrC.hur[iY] = 4; // "LA"
                if(Util.isGas(namn.identC[iY])) {dgrC.cLow[iY] = -3.5;}
            }
          } // if there was a component change
        } // if compY >=0
      } // if diagram type = "Predominance Area"

      // --- Get the new compY
      for(int ic = 0; ic < cs.Na; ic++) {
          if(jComboBoxYaxComp.getSelectedItem().toString().
                equalsIgnoreCase(namn.identC[ic])) {diag.compY = ic; break;}
      } // for ic

      changingComp = true;
      updateAxes();
      changingComp = false;
      resizeYMin(); resizeYMax();
      updateUseEhCheckBox();
      updateDrawPHlineBox();
      enableTemperature();
    }//GEN-LAST:event_jComboBoxYaxCompActionPerformed

    private void jRadioButtonFixedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonFixedActionPerformed
      if(loading) {return;}
      diag.ionicStrength = ionicStrOld;
      jTextFieldIonicStr.setText(Util.formatNum(diag.ionicStrength));
      jTextFieldIonicStr.setEnabled(true);
      jLabelIonicS.setText("<html><u>I</u> =</html>");
      jLabelIonicS.setEnabled(true);
      jLabelM.setEnabled(true);
      if(diag.ionicStrength == 0) {
        jComboBoxActCoeff.setVisible(false);
        jLabelModel.setVisible(false);
      } else {
        if(pd.advancedVersion) {
          jLabelModel.setVisible(true);
          jComboBoxActCoeff.setVisible(true);
        }
      }
      enableTemperature();
    }//GEN-LAST:event_jRadioButtonFixedActionPerformed

    private void jRadioButtonCalcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonCalcActionPerformed
      ionicStrOld = Math.max(0,readTextField(jTextFieldIonicStr));
      jTextFieldIonicStr.setEnabled(false);
      jLabelIonicS.setText("I =");
      jLabelIonicS.setEnabled(false);
      jLabelM.setEnabled(false);
      diag.ionicStrength = -1;
      if(pd.advancedVersion) {
          jLabelModel.setVisible(true);
          jComboBoxActCoeff.setVisible(true);
      }
      enableTemperature();
    }//GEN-LAST:event_jRadioButtonCalcActionPerformed

    private void jTextFieldCLowKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldCLowKeyPressed
        if(evt.getKeyCode()==java.awt.event.KeyEvent.VK_ENTER) {jButtonGetConcOK.doClick(); evt.consume();}
    }//GEN-LAST:event_jTextFieldCLowKeyPressed

    private void jTextFieldCHighKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldCHighKeyPressed
        if(evt.getKeyCode()==java.awt.event.KeyEvent.VK_ENTER) {jButtonGetConcOK.doClick(); evt.consume();}
    }//GEN-LAST:event_jTextFieldCHighKeyPressed

    private void jTextFieldCLowFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldCLowFocusGained
        oldTextCLow = jTextFieldCLow.getText();
        jTextFieldCLow.selectAll();
    }//GEN-LAST:event_jTextFieldCLowFocusGained

    private void jTextFieldCHighFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldCHighFocusGained
        oldTextCHigh = jTextFieldCHigh.getText();
        jTextFieldCHigh.selectAll();
    }//GEN-LAST:event_jTextFieldCHighFocusGained

    private void jTextFieldCLowFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldCLowFocusLost
        focusLostCLow();
    }//GEN-LAST:event_jTextFieldCLowFocusLost

    private void jTextFieldCHighFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldCHighFocusLost
        focusLostCHigh();
    }//GEN-LAST:event_jTextFieldCHighFocusLost

    private void jTextFieldCLowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldCLowActionPerformed
        focusLostCLow();
    }//GEN-LAST:event_jTextFieldCLowActionPerformed

    private void jTextFieldCHighActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldCHighActionPerformed
        focusLostCHigh();
    }//GEN-LAST:event_jTextFieldCHighActionPerformed

    private void jCheckBoxDrawPHlineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDrawPHlineActionPerformed
        runPHline = jCheckBoxDrawPHline.isSelected();
    }//GEN-LAST:event_jCheckBoxDrawPHlineActionPerformed

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
      if(windowSize != null) {
        int w = windowSize.width;
        int h = windowSize.height;
        if(this.getHeight()<h){this.setSize(this.getWidth(), h);}
        if(this.getWidth()<w){this.setSize(w,this.getHeight());}
      }
    }//GEN-LAST:event_formComponentResized
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Methods">

  private void quitFrame() {
    setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    MainFrame.locationSDFrame = this.getLocation();
    this.setVisible(false);
    finished = true;
    this.notify_All();
    this.dispose();
  } // quitFrame()

  /** this method will wait for this frame to be closed
     * @return  "cancel", true if no calculations performed,
     * false = "ok", the calculations performed */
  public synchronized boolean waitForSelectDiagram() {
    while(!finished) {
        try {wait();} catch (InterruptedException ex) {}
    } // while
    return cancel;
  } // waitForSelectDiagram()

  private synchronized void notify_All() {notifyAll();}

  /** @param key a character
   * @return true if the character is ok, that is, it is either a number,
   * or a dot, or a minus sign, or an "E" (such as in "2.5e-6") */
  private boolean isCharOKforNumberInput(char key) {
        return Character.isDigit(key)
                || key == '-' || key == '+' || key == '.' || key == 'E' || key == 'e';
  } // isCharOKforNumberInput(char)

//<editor-fold defaultstate="collapsed" desc="checkChanges()">
/** Is there a need to save the input data file? If so the file is saved here.
 * @return  <code>=false</code> if the data file needed to be saved but an error
 * occurred while trying to save the changes; otherwise it returns <code>=true</code>
 * indicating that there was no need to save the file, or that the user
 * made changes that required saving the data file and no errors
 * occurred while saving the data file. */
private boolean checkChanges() {
  boolean changed = false;
  if(pc.dbg) {System.out.println("Checking changes in data file: \""+dataFile.getName()+"\"");}
  if(!dgrC.isEqualTo(dgrC0)) {
      if(pc.dbg) {System.out.println("Concentrations changed.");}
      changed = true;
  }
  if(!changed && diag.plotType != diag0.plotType) {
      if(pc.dbg) {System.out.println("Plot type changed.");}
      changed = true;
  }
  if(!changed) {
    if(diag.plotType ==0) { //Predom
       if(diag.compX != diag0.compX ||
          diag.compY != diag0.compY ||
          diag.compMain != diag0.compMain) {
           if(pc.dbg) {System.out.println("Axis and/or main components changed.");}
           changed = true;
       }
    } //if Predom
  } //if not changed
  if(!changed) {
    if(diag.plotType >0) { //SED
       if(diag.compX != diag0.compX ||
          ((diag.plotType ==1 || diag.plotType ==4) && //Fraction or Relative act.
                diag.compY != diag0.compY)) {
           if(pc.dbg) {System.out.println("Axis components changed.");}
           changed = true;
       }
    } //if SED
  } //if not changed
  if(!changed) {
    if(((diag.plotType >=2 && diag.plotType <=7) && // log scale in Y-axis?
        (diag.yLow != diag0.yLow || diag.yHigh != diag0.yHigh))) {
        if(pc.dbg) {System.out.println("Y-axis range changed.");}
        changed = true;
    }
  } //if not changed
  diag.title = Util.rTrim(jTextFieldTitle.getText());
  if(!changed) {
    // check if one is null and the other not
    if((diag.title != null & diag0.title == null) ||
       (diag.title == null & diag0.title != null)) {
        if(pc.dbg) {System.out.println("Title changed from (or to) \"null\".");}
        changed = true;
    } else {
        if(diag.title != null & diag0.title != null) {
            if(diag.title.length() != diag0.title.length()) {
                if(pc.dbg) {System.out.println("Title changed: different lengths.");}
                changed = true;
            } else {
                if(!diag.title.equals(diag0.title)) {
                    if(pc.dbg) {System.out.println("Title changed.");}
                    changed = true;
                }
            }
        }
    }
  } //if not changed
  if(!changed) {
    if(!Double.isNaN(diag.temperature) || !Double.isNaN(diag0.temperature)) {
      if((!Double.isNaN(diag.temperature) && Double.isNaN(diag0.temperature)) ||
         (Double.isNaN(diag.temperature) && !Double.isNaN(diag0.temperature)) ||
         diag.temperature != diag0.temperature) {
          if(pc.dbg) {System.out.println("Temperature changed.");}
          changed = true;
      }
    }
  } //if not change
  if(!changed) {
    if(!Double.isNaN(diag.pressure) || !Double.isNaN(diag0.pressure)) {
      if((!Double.isNaN(diag.pressure) && Double.isNaN(diag0.pressure)) ||
         (Double.isNaN(diag.pressure) && !Double.isNaN(diag0.pressure)) ||
         diag.pressure != diag0.pressure) {
          if(pc.dbg) {System.out.println("Pressure changed.");}
          changed = true;
      }
    }
  } //if not change

  // are there "e-" in the chemical system?
  if(ePresent >= 0) {
    diag.Eh = runUseEh;
    if(!changed) {
        if(diag.Eh != diag0.Eh) {
            if(pc.dbg) {System.out.println("\"Eh\" changed.");}
            changed = true;
        }
    } //if not changed
  }

  if(changed) {
    System.out.println("--- Saving changes to input file.");
    try {WriteChemSyst.writeChemSyst(ch, dataFile);}
    catch (Exception ex) {
        MsgExceptn.showErrMsg(this,ex.getMessage(),1);
        return false;
    }
    return true;
  } //if changed
  else {
      System.out.println("--- No changes need to be saved to the input file.");
      return true;
  } //not changed

} //checkChanges();
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="checkComponentsInAxes()">
/** Check that compX &gt;=0, and for a PREDOM, Fraction or Relative diagrams
 * check that compY is &gt;=0. Set default values otherwise. */
  private void checkComponentsInAxes() {
    //--- X-axis: Check that there is a valid component.
    //    Take a default otherwise
    if(diag.compX < 0) {
      // select a default X-component: e-, H+, the 1st anion, or the 1st gas
      if(hPresent >= 0 && hPresent < cs.Na) {
        diag.compX = hPresent;
        dgrC.cLow[diag.compX] = -12;
        dgrC.cHigh[diag.compX] = -1;
        dgrC.hur[diag.compX] = 5; // "LAV" = "pH varied"
      } else
      if(ePresent >= 0 && ePresent < cs.Na) {
        diag.compX = ePresent;
        dgrC.cLow[diag.compX] = -17;
        dgrC.cHigh[diag.compX] = 17;
        dgrC.hur[diag.compX] = 5; // "LAV" = "pe varied"
      }
    } // if compX < 0
    if(diag.compX < 0) {
      for(int ic =0; ic < cs.Na; ic++) {
        if(ic != ePresent && Util.isAnion(namn.identC[ic])) {
          diag.compX = ic;
          dgrC.cLow[ic] = -6;
          dgrC.cHigh[ic] = 0.5;
          if(dgrC.hur[ic] == 1 || dgrC.hur[ic] == 2 || dgrC.hur[ic] == 3) { // "T"
                dgrC.hur[ic] = 3; // "LTV" = "log (Total conc.) varied"
          } else { // not "T"
            dgrC.hur[ic] = 5; // "LAV" = "log (activity) varied"
          }
          break;
        } // if isAnion
      } // for ic
    } // if compX < 0
    if(diag.compX < 0) {
      for(int ic =0; ic < cs.Na; ic++) {
        if(Util.isGas(namn.identC[ic])) {
          diag.compX = ic;
          dgrC.cLow[ic] = -4;
          dgrC.cHigh[ic] = 2;
          if(dgrC.hur[ic] == 1 || dgrC.hur[ic] == 2 ||
             dgrC.hur[ic] == 3) { // "T"
                dgrC.hur[ic] = 3; // "LTV" = "log (Total conc.) varied"
          } // "T"
          else
          {
            dgrC.hur[ic] = 5; // "LAV" = "log P varied"
          }
          break;
        }
      } // for ic
    } // if compX < 0

    //--- Y-axis: Check that there is a valid component.
    //    Take a default otherwise
    if(diag.plotType == 0 || //PREDOM
       diag.plotType == 1 || //fraction
       diag.plotType == 4) { //relative diagram
    if(diag.compY < 0) {
      // select a default Y-component: e-, H+, the 1st anion, or the 1st gas
      if(hPresent >= 0 && hPresent < cs.Na && diag.compX != hPresent) {
        diag.compY = hPresent;
        dgrC.cLow[diag.compY] = -12;
        dgrC.cHigh[diag.compY] = -1;
        dgrC.hur[diag.compY] = 5; // "LAV" = "pH varied"
      } else
      if(ePresent >= 0 && ePresent < cs.Na && diag.compX != ePresent) {
        diag.compY = ePresent;
        dgrC.cLow[diag.compY] = -17;
        dgrC.cHigh[diag.compY] = 17;
        dgrC.hur[diag.compY] = 5; // "LAV" = "pe varied"
      }
    } // if compY < 0
    if(diag.compY < 0) { //take an anion
      for(int ic =0; ic < cs.Na; ic++) {
        if(ic != ePresent && Util.isAnion(namn.identC[ic])
           && diag.compX != ic) {
          diag.compY = ic;
          dgrC.cLow[ic] = -6;
          dgrC.cHigh[ic] = 0.5;
          if(dgrC.hur[ic] == 1 || dgrC.hur[ic] == 2 ||
             dgrC.hur[ic] == 3) { // "T"
                dgrC.hur[ic] = 3; // "LTV" = "log (Total conc.) varied"
          } // "T"
          else
          {
            dgrC.hur[ic] = 5; // "LAV" = "log (activity) varied"
          }
          break;
        } // if isAnion
      } // for ic
    } // if compY < 0
    if(diag.compY < 0) { //take a gas
      for(int ic =0; ic < cs.Na; ic++) {
        if(Util.isGas(namn.identC[ic]) && diag.compX != ic) {
          diag.compY = ic;
          dgrC.cLow[ic] = -4;
          dgrC.cHigh[ic] = 2;
          if(dgrC.hur[ic] == 1 || dgrC.hur[ic] == 2 ||
             dgrC.hur[ic] == 3) { // "T"
                dgrC.hur[ic] = 3; // "LTV" = "log (Total conc.) varied"
          } // "T"
          else
          {
            dgrC.hur[ic] = 5; // "LAV" = "log P varied"
          }
          break;
        }
      } // for ic
    } // if compY < 0
    } //if PREDOM etc
    //--- the end
  } //checkComponentsInAxes()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="checkForEqual(w1,w2)">
  private static class TwoDoubles {
      double x1, x2;
      //public twoDoubles() {x1 = Double.NaN; x2 = Double.NaN;}
      public TwoDoubles(double w1, double w2) {x1 = w1; x2 = w2;}
  }
  private void checkForEqual(TwoDoubles td) {
      boolean done = false;
      if(Double.isNaN(td.x1)) {td.x1 = 0;}
      if(Double.isNaN(td.x2)) {td.x2 = 0;}
      if(td.x1 != 0) {
          if(td.x2 != 0 && Math.abs((td.x1-td.x2)/td.x1) <= 0.00001) {
              td.x2 = td.x1 + td.x1*0.001; done = true;}
      }
      if(!done && td.x2 != 0) {
          if(td.x1 != 0 && Math.abs((td.x1-td.x2)/td.x2) <= 0.00001) {
              td.x2 = td.x1 + td.x1*0.001; done = true;}
      }
      if(!done) {
          if(Math.abs(td.x1-td.x2) <= 1e-26) {td.x2 = 1;}
      }
  } // checkForEqual(TwoDoubles)
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="checkInputConcs()">
/** Check that the components in the axes have varied concentrations, and that
 * for PREDOM, check also that all components not belonging to an axes have
 * not varied concentrations. Fix the problem with default choices if a
 * problem is found. */
private void checkInputConcs() {
  if(pc.dbg) {System.out.println("checkInputConcs()");}
  String component;
  boolean inAxis;
  for(int i = 0; i < cs.Na; i++) {
    // ------------------------------
    // For SED and PREDOM: set default concs. for all comps. not in an axis
    // (this is perhaps not needed, but included for robustness)
    component = namn.identC[i];
    inAxis = false;
    if(diag.plotType == 0) {//PREDOM
      if(i == diag.compX || i == diag.compY) {inAxis = true;}
    } else if(diag.plotType >= 1) { //SED
      if(i == diag.compX) {inAxis = true;}
    }
    if(diag.plotType == 0 && !inAxis) { //PREDOM
      //in PREDOM only components in the axes can have conc. varied
      if(dgrC.hur[i] ==2 || dgrC.hur[i] ==3 || dgrC.hur[i] ==5) {
        // conc. varied: must change to fixed
        MsgExceptn.showErrMsg(this, "Warning: the concentration for \""+component+"\" is varied,"+nl+
                "but it does not belong to an axes."+nl+"It will be changed to non-varied.", 2);
        if(i == ePresent &&  dgrC.hur[i] ==5) { // not "T"?
          dgrC.cLow[i] = -8.5;
          dgrC.hur[i] = 4; // "LA"
        } else
        if(i == hPresent && dgrC.hur[i] ==5) {
          dgrC.cLow[i] = -7;
          dgrC.hur[i] = 4; // "LA" = "pH"
        } else
        if(Util.isWater(component)) {
          dgrC.cLow[i] = 0;
          dgrC.hur[i] = 4; // "LA" = "log (activity)"
        } else
        if(dgrC.hur[i] ==2 || dgrC.hur[i] ==3) { //"TV" or "LTV"
          if(dgrC.hur[i] ==3) { //"LTV"
            if(dgrC.cLow[i] <= 20) {
                dgrC.cLow[i] = logToNoLog(dgrC.cLow[i]);
            } else {
                if(pd.kth) {dgrC.cLow[i] = 0.01;} else {dgrC.cLow[i] = 1e-5;}
            } // if < 20
          } // if "LTV"
          dgrC.hur[i] = 1; // "T" = "Total conc."
        } // if "TV" or "LTV"
        else {
          dgrC.hur[i] = 4; // "LA" = "log (activity)"
          if(Util.isGas(component)) {dgrC.cLow[i] = -3.5;}
        } // if not "T..."
      } // if conc. varied
    } //if PREDOM and !inAxis
    if((diag.plotType == 0 && inAxis) || (diag.plotType >= 1 && inAxis)) {
    // ------------------------------
    // For SED and PREDOM: set varied concs. for all comps. in the axis
    // (this is perhaps not needed, but included for robustness)
      if(dgrC.hur[i] ==1 || dgrC.hur[i] ==4) { //not varied ("T" or "LA")
        // conc. not varied: must change
        MsgExceptn.showErrMsg(this, "Warning: the concentration for \""+component+"\" is not varied,"+nl+
                "but it belongs to an axes."+nl+"It will be changed to varied.",2);
        if(i == ePresent &&  dgrC.hur[i] ==4) {// "LA"?
          dgrC.cLow[i] = -16.9034;
          dgrC.cHigh[i] = +16.9034;
          dgrC.hur[i] = 5; // "LAV"
        } else
        if(i == hPresent &&  dgrC.hur[i] ==4) {// "LA"?
          dgrC.cLow[i] = -12;
          dgrC.cHigh[i] = -1;
          dgrC.hur[i] = 5; // "LAV" = "pH varied"
        } else
        if(Util.isWater(component)) {
          dgrC.cLow[i] = -2;
          dgrC.cHigh[i] = 0;
          dgrC.hur[i] = 5; // "LAV" = "log (activity) varied"
        } else
        if(dgrC.hur[i] ==1) { //"T"?
            if(dgrC.cLow[i]>0) {
                if(dgrC.cLow[i] <=0.001) {
                    dgrC.cLow[i] = Math.log(dgrC.cLow[i]);
                    dgrC.cHigh[i] = dgrC.cLow[i]+3;
                } else {
                    dgrC.cHigh[i] = Math.log(dgrC.cLow[i]);
                    dgrC.cLow[i] = dgrC.cHigh[i]-3;
                }
            } else {
                dgrC.cLow[i] = -6;
                dgrC.cHigh[i] = 0;
            }
          dgrC.hur[i] = 3; // "LTV" = "log (Total conc.) varied"
        } // if "T..."
        else { //"LA"
          dgrC.hur[i] = 5; // "LAV" = "log (activity) varied"
          if(dgrC.cLow[i] <=-3) {
              dgrC.cHigh[i] = dgrC.cLow[i]+3;
          } else {
              dgrC.cHigh[i] = dgrC.cLow[i];
              dgrC.cLow[i] = dgrC.cHigh[i]-3;
          }
        } // if "LA"
      } //if con. not varied
    } //if SED or PREDOM and inAxis
    if(diag.plotType == 0 && inAxis) {
    // ------------------------------
    // For PREDOM: if the concentration is "TV" for a comp. in an axis
    // change to "LTV"
      if(dgrC.hur[i] ==2) {
        // Tot. conc. varied: must change to log (Tot. conc.) varied
        MsgExceptn.showErrMsg(this, "Warning: the total concentration for \""+component+"\" is varied."+nl+
                "It will be changed to log(Tot.Conc.) varied.",2);
        dgrC.hur[i] = 3; // "LTV" = "log (Total conc.) varied"
        dgrC.cLow[i] = Math.max(-50, Math.log10(Math.max(1e-51, Math.abs(dgrC.cLow[i]))));
        dgrC.cHigh[i] = Math.max(-45, Math.log10(Math.max(1e-46, Math.abs(dgrC.cHigh[i]))));
      } //if "TV"
    } //if PREDOM and inAxis
    // ------------------------------
  } // for i = 0 to ca.Na-1
} //checkInputConcs()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="checkMainComponent()">
private void checkMainComponent() {
  if(diag.plotType == 0) { //PREDOM
    if(diag.compMain < 0) {
      for(int ic =0; ic < cs.Na; ic++) {
        if(ic != hPresent && Util.isCation(namn.identC[ic])) {
          diag.compMain = ic; break;
        }
      }
    }
  }
} //checkMainComponent()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="compConcList_Click(i)">
  private void compConcList_Click(int i) {
    if(i < 0) {return;}
    getConc_idx = i;
    // --- get the component name
    String comp = namn.identC[getConc_idx];
    if(Util.isWater(comp)) { // is it H2O?
        String msg = "<html>The calculations are made for 1kg H<sub>2</sub>O<br>&nbsp;<br>";
        if(dgrC.hur[getConc_idx] < 4) { // total conc. for H2O given
            msg = msg + "The input for <b>water</b> will be changed to activity = 1.";
        } else { // log A given
            msg = msg + "The activity of <b>water</b> is calculated when<br>"+
                    "the ionic strength is <i>not</i> zero.";
        }
        javax.swing.JOptionPane.showMessageDialog(this, msg, pc.progName, javax.swing.JOptionPane.INFORMATION_MESSAGE);
        dgrC.cLow[getConc_idx] = 0;
        dgrC.hur[getConc_idx] = 4; // "LA" = "log (activity)"
        return;
    } // for H2O

    getConcLoading = true;

    // disable buttons and things
    jButton_OK.setEnabled(false);
    jButton_Cancel.setEnabled(false);
    jButtonSaveDef.setEnabled(false);
    if(getConc_idx == ePresent) {
        if(jCheckBoxUseEh.isVisible()) {
          jCheckBoxUseEh.setText("use Eh for e-");
          jCheckBoxUseEh.setEnabled(false);
        }
        jLabelT.setEnabled(false);
        jTextFieldT.setEnabled(false);
        jLabelTC.setEnabled(false);
    }

    java.awt.CardLayout cl = (java.awt.CardLayout)jPanelConcs.getLayout();
    cl.show(jPanelConcs, "panelGetConc");
    //cl.show(jPanelConcs, "panelCompConcList");

    double fEh = (MainFrame.Rgas * ln10 * (diag.temperature +273.15)) / MainFrame.Faraday;

    boolean isGas = Util.isGas(comp);
    boolean isSolid = (Util.isSolid(comp) || getConc_idx >= (cs.Na-cs.solidC));
    jLabel_GetConcCompName.setText(comp);

    // --- get the value of the concentration(s)
    jLabelFrom.setText(" "); jLabelTo.setText(" ");
    jTextFieldCHigh.setVisible(false);
    int use_pHpe = 0;
    if(dgrC.hur[getConc_idx] ==4 || dgrC.hur[getConc_idx] ==5) { // "A"
      if(getConc_idx == hPresent) {use_pHpe =1;}
      else if(getConc_idx == ePresent) {use_pHpe =2; if(runUseEh) {use_pHpe =3;}}
    }
    double w1 = dgrC.cLow[getConc_idx];
    double w2 = dgrC.cHigh[getConc_idx];
    if(use_pHpe ==1 || use_pHpe ==2) {w1 =-w1; w2 =-w2;}
    if(use_pHpe == 3) {w1 = -w1 *fEh;  w2 = -w2 *fEh;}
    // rounding for logarithmic concs.
    if(dgrC.hur[getConc_idx] ==2 || dgrC.hur[getConc_idx] ==3 ||
       dgrC.hur[getConc_idx] ==5) { // "V"
         jLabelFrom.setText("from:"); jLabelTo.setText("to:");
         jTextFieldCHigh.setVisible(true);
         if(!runRevs) {if(w2 < w1) {double w = w1; w1 = w2; w2 = w;}}
         jTextFieldCHigh.setText(Util.formatDbl6(w2));
    } // if conc. varied
    else {jTextFieldCHigh.setText("0");}
    jTextFieldCLow.setText(Util.formatDbl6(w1));

    int jc = dgrC.hur[getConc_idx]-1 + (use_pHpe * 2);
    if(isGas) {
      if(jc==3) {jc = 11;} // LA = "log P"
      if(jc==4) {jc = 12;} // LAV = "log P varied"
    }

    String conc2 = concTypes[jc];
    // --- fill in the conc. combo-box
    boolean inXaxis = false;
    boolean inYaxis = false;
    boolean inAxis;
    if(namn.identC[getConc_idx].
       equalsIgnoreCase(jComboBoxXaxComp.getSelectedItem().toString())) {inXaxis = true;}
    if(namn.identC[getConc_idx].
       equalsIgnoreCase(jComboBoxYaxComp.getSelectedItem().toString())) {inYaxis = true;}
    inAxis = (inXaxis | inYaxis);
    if(getConc_idx == hPresent) {use_pHpe =1;}
    else if(getConc_idx == ePresent) {use_pHpe =2; if(runUseEh) {use_pHpe =3;}}
    jComboBoxConcType.removeAllItems();
    int ic;
    if(!inXaxis && !(runPredomSED==1 && (inYaxis || (isSolid && cs.nx ==0 && dgrC.hur[getConc_idx] !=4)))) {
        ic = 3 + use_pHpe * 2;
        if(isGas) {ic = 11;} // "log P"
        jComboBoxConcType.addItem(concTypes[ic]); //pH/pe/Eh/log(activity)
    }
    if(!(runPredomSED==1 && (!inAxis || (isSolid && cs.nx ==0 && dgrC.hur[getConc_idx] !=5)))) {
        ic = 4 + use_pHpe * 2;
        if(isGas) {ic = 12;} // "log P varied"
        jComboBoxConcType.addItem(concTypes[ic]); //pH/pe/Eh/log(activity) varied
    }
    if(!inXaxis && !(runPredomSED==1 && inYaxis)) {
        jComboBoxConcType.addItem(concTypes[0]); // Total conc.
    }
    if(runPredomSED!=1) {
        jComboBoxConcType.addItem(concTypes[1]); // Total conc. varied
    }
    if(!(runPredomSED==1 && !inAxis)) {
        jComboBoxConcType.addItem(concTypes[2]); // log Total conc. varied
    }
    // select the conc. type in the combo box
    jComboBoxConcType.setSelectedIndex(-1);
    for(int k=0; k < jComboBoxConcType.getItemCount(); k++) {
        if(conc2.equalsIgnoreCase(jComboBoxConcType.getItemAt(k).toString())) {
            jComboBoxConcType.setSelectedIndex(k);
            break;
        }
    } //for k
    // if the original conc is of unknown type:
    if(jComboBoxConcType.getSelectedIndex() <0) {
        jComboBoxConcType.addItem("? unknown conc. type");
        jComboBoxConcType.setSelectedIndex(jComboBoxConcType.getItemCount()-1);
    }
    comboBoxConcType0 = jComboBoxConcType.getSelectedItem().toString();
    getConcLoading = false;
    jTextFieldCLow.requestFocusInWindow();
// msgBox
//javax.swing.JOptionPane.showMessageDialog(null,"hej",pc.progName, javax.swing.JOptionPane.INFORMATION_MESSAGE);
  } // compConcList_Click(i)
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="logToNoLog(w)">
/** coverts "x" to 10^(x). If x is larger than 307, 10^(307) is returned.
 * If x is less than -300, or if x is NaN, zero is returned.
 * @param x
 * @return 10^(x) */
  private double logToNoLog (double x) {
    // Convert -10 to 1E-10
    double r = 0;
    if(!Double.isNaN(x)) {
        if(x > 307) {
            r = 1E+307;
        } else
            if(x > -300) {r = Math.pow(10,x);}
    }
    return r;
  } // logToNoLog(w)
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="dataFile_Click()">
  private void dataFile_Click() {
    // get a file name
    jTextFieldDataFile.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
    setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
    String fileName = Util.getOpenFileName(this, pc.progName, true,
            "Enter Data file name", 5, null, dataFile.getPath());
    if(fileName == null) {
        jTextFieldDataFile.requestFocusInWindow();
        jTextFieldDataFile.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        return;}
    if(MainFrame.getInstance().addDatFile(fileName)) {
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        dataFile = new java.io.File(fileName);
        if(!readDataFile(pc.dbg)) {
            jTextFieldDataFile.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            return;}
        if(pc.dbg) {System.out.println(" ------------------------ change of data file to \""+dataFile.getName()+"\"");}

        boolean b = loading; loading = true;
        setUpFrame();
        loading = b;

        updateAxes();
        updateConcList();
        resizeXMinMax(); resizeYMin(); resizeYMax();
        updateUseEhCheckBox();
        updateDrawPHlineBox();
        enableTemperature();
        if(plotAndConcsNotGiven) {missingPlotAndConcsMessage();}
    }
    // bringToFront
    java.awt.EventQueue.invokeLater(new Runnable() {
        @Override public void run() {
            setAlwaysOnTop(true);
            toFront();
            requestFocus();
            setAlwaysOnTop(false);
            jTextFieldDataFile.requestFocusInWindow();
            jTextFieldDataFile.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }
    });
  } // dataFile_Click()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="diagramType_Click()">
/** Take the actions necessary when the diagram type in "jComboBoxDiagType"
 * is changed */
private void diagramType_Click() {
  if(diagramType_doNothing) {return;}
  if(pc.dbg) {System.out.println("--- diagramType_Click() --- (loading="+loading+")");}
  updatingAxes = true;
  java.awt.CardLayout cl;
  String dt = jComboBoxDiagType.getSelectedItem().toString();
  if(ch != null && loading) {
          if(diag.plotType == 0) {dt =  "Predominance Area";}
          else if(diag.plotType == 1) {dt = "Fraction";}
          else if(diag.plotType == 2) {dt = "log Solubilities";}
          else if(diag.plotType == 3) {dt = "Logarithmic";}
          else if(diag.plotType == 4) {dt = "Relative activities";}
          else if(diag.plotType == 5) {
                    if(runUseEh) {dt = "calculated Eh";}
                            else {dt = "calculated pe";}}
          else if(diag.plotType == 6) {dt = "calculated pH";}
          else if(diag.plotType == 7) {dt = "log Activities";}
          else if(diag.plotType == 8) {dt = "H+ affinity spectrum";}
  }
  if(pc.dbg) {System.out.println("diagram type: "+dt);}
  int oldPlotType = diag.plotType;


  // ---------------------------------------------------------------------------
  // -----  PREDOM:
  // ---------------
  if(dt.equalsIgnoreCase("Predominance Area")) {
    cl = (java.awt.CardLayout)jPanelSedPredom.getLayout();
    if(pd.advancedVersion) {
      cl.show(jPanelSedPredom, "Predom");
    } else {
      cl.show(jPanelSedPredom, "Empty"); //.setVisible(false);
    }
    cl = (java.awt.CardLayout)jPanelYaxInner.getLayout();
    cl.show(jPanelYaxInner, "cardYcombo");
    cl = (java.awt.CardLayout)jPanelYaxComp.getLayout();
    cl.show(jPanelYaxComp, "cardYaxComp1"); //.setVisible(true);

    enableYminYmax(true);
    jComboBoxMainComp.setVisible(true);

    setOKButtonIcon("images/PlotPred16_32x32_transpBckgr.gif");

    if(!loading) {

    if(runPredomSED == 2) {
        // --- The user changes from a SED to a PREDOM-diagram:
        //     (it is possible that the user selects "predominance diagram" twice in a row)
        // select a default Y-component: e-, H+, the 1st anion, or the 1st gas
        diag.compY = -1;
        diag.compMain = -1;
        boolean includeSpecies = false;
        updateYcomp(includeSpecies);
        updatingAxes = true;
    } //if it was SED

    // --- Select a component for the Y-axis if missing
    if(diag.compY < 0) {
        if(diag.compX == hPresent) {
            if(ePresent >=0 && ePresent < cs.Na) { // pH in X-axis: select Eh in Y-axis
                diag.compY = ePresent;
                dgrC.cLow[diag.compY] = -17;
                dgrC.cHigh[diag.compY] = 17;
                dgrC.hur[diag.compY] = 5; // "LAV" = "pe varied"
            }
        } else if(hPresent >=0 && hPresent < cs.Na) {
            diag.compY = hPresent;     // otherwise, pH in Y-axis
            dgrC.cLow[diag.compY] = -12;
            dgrC.cHigh[diag.compY] = -1;
            dgrC.hur[diag.compY] = 5; // "LAV" = "pH varied"
        }
    } //if compY < 0
    if(diag.compY < 0) { // take an anion
        for(int ic =0; ic < cs.Na; ic++) {
            if(ic != ePresent && Util.isAnion(namn.identC[ic])) {
                diag.compY = ic;
                if(diag.compY != diag.compX) {
                    if(dgrC.hur[ic] ==3 || dgrC.hur[ic] ==5) { // "L*V"
                        dgrC.cLow[ic] = -6; dgrC.cHigh[ic] = 0;
                    } else if(dgrC.hur[ic] ==2) { // "TV"
                        dgrC.cLow[ic] = 0; dgrC.cHigh[ic] = 1;
                    }
                } //if not = X-axis
                break;
            } //isAnion
        } //for ic
    } //if compY < 0
    if(diag.compY < 0) { // take a gas
        for(int ic =0; ic < cs.Na; ic++) {
            if(Util.isGas(namn.identC[ic])) {
                diag.compY = ic;
                if(diag.compY != diag.compX) {
                    if(dgrC.hur[ic] ==3 || dgrC.hur[ic] ==5) { // "L*V"
                        dgrC.cLow[ic] = -4; dgrC.cHigh[ic] = 2;
                    } else if(dgrC.hur[ic] ==2) { // "TV"
                        dgrC.cLow[ic] = 0; dgrC.cHigh[ic] = 1;
                    }
                } //if not = X-axis
                break;
            } //isGas
        } //for ic
    } //if compY < 0
    if(diag.compY < 0) { //nothing fits, get the 1st one on the list
        int j=0;
        if(diag.compX == 0 && jComboBoxYaxComp.getItemCount() >1) {j=1;}
        for(int ic=0; ic < cs.Na; ic++) {
            if(namn.identC[ic].equalsIgnoreCase(jComboBoxYaxComp.getItemAt(j).toString())) {
                diag.compY = ic;
                if(diag.compY != diag.compX) {
                    if(dgrC.hur[ic] ==3 || dgrC.hur[ic] ==5) { // "L*V"
                        dgrC.cLow[ic] = -6; dgrC.cHigh[ic] = 0;
                    } else if(dgrC.hur[ic] ==2) { // "TV"
                        dgrC.cLow[ic] = 0; dgrC.cHigh[ic] = 1;
                    }
                } //if not = X-axis
                break;
            }
        } //for ic
    } //if compY < 0

    // ---- Set the concentration for the Y-component varied:
    if(dgrC.hur[diag.compY] !=2 && dgrC.hur[diag.compY] !=3 && dgrC.hur[diag.compY] !=5) {
        // not "*V"
        if(Util.isGas(namn.identC[diag.compY])) {
            dgrC.hur[diag.compY] = 5; //"LAV" = "log P varied"
        } //if isGas
        else
        if(dgrC.hur[diag.compY] ==1 || dgrC.hur[diag.compY] ==2 || dgrC.hur[diag.compY] ==3) {
            //"*T*": total conc.
            dgrC.hur[diag.compY] = 3; //"LTV" = "log (Total conc.) varied"
        } else { // not "*T*": activity
            dgrC.hur[diag.compY] = 5; //"LAV" = "log (activity) varied"
        }
    } //if conc. not varied

    // ---- Set a default Main-component: the 1st cation, or 1st solid, gas, aqu.
    if(diag.compMain < 0) {
        // first get a cation which is dos not begin with either "NH" or "N(" to skip ammonium
        for(int ic =0; ic < cs.Na; ic++) {
            if(ic != hPresent && Util.isCation(namn.identC[ic])
                    && !namn.ident[ic].startsWith("NH") && !namn.ident[ic].startsWith("N(")) {
                diag.compMain = ic; break;
            } // isCation
        } //for ic
    } //if compMain < 0
    if(diag.compMain < 0 && cs.solidC >0) { // get the first solid component
            diag.compMain = (cs.Na - cs.solidC);
    } //if compMain < 0
    if(diag.compMain < 0) {  // get a gas
        for(int ic =0; ic < cs.Na; ic++) {
            if(Util.isGas(namn.identC[ic])) {
                diag.compMain = ic; break;
            } // isGas
        } //for ic
    } //if compMain < 0
    if(diag.compMain < 0) { // get an aqueous species
        for(int ic =0; ic < cs.Na; ic++) {
            if(namn.identC[ic].length() >4 &&
               namn.identC[ic].toUpperCase().endsWith("(AQ)")) {
                diag.compMain = ic; break;
            } // is aqueous
        } //for ic
    } //if compMain < 0
    if(diag.compMain < 0) { // get a cation even if it begin with either "NH" or "N("
        for(int ic =0; ic < cs.Na; ic++) {
            if(ic != hPresent && Util.isCation(namn.identC[ic])) {
                diag.compMain = ic; break;
            } // isCation
        } //for ic
    } //if compMain < 0
    if(diag.compMain < 0) { // nothing fits, get the 1st one on the list
        for(int ic=0; ic < cs.Na; ic++) {
            if(namn.identC[ic].equalsIgnoreCase(jComboBoxMainComp.getItemAt(0).toString())) {
                diag.compMain = ic;
                break;
            }
        } //for ic
    } //if compMain < 0

    }//--- if not loading

    // --- select the component in the Y-axis ComboBox
    if(diag.compY >=0) {
      for(int j=0; j < jComboBoxYaxComp.getItemCount(); j++) {
        if(namn.identC[diag.compY].equalsIgnoreCase(jComboBoxYaxComp.getItemAt(j).toString())) {
            jComboBoxYaxComp.setSelectedIndex(j);
            break;
        }
      } //for j
    }

    // --- select the component in the Main-comp ComboBox
    if(diag.compMain >=0) {
      for(int j=0; j < jComboBoxMainComp.getItemCount(); j++) {
        if(namn.identC[diag.compMain].equalsIgnoreCase(jComboBoxMainComp.getItemAt(j).toString())) {
            jComboBoxMainComp.setSelectedIndex(j);
            break;
        }
      } //for j
    }

    // ---- if Main-component does not belong to an axis,
    //      set the concentration to not varied
    if(diag.compMain != diag.compX && diag.compMain != diag.compY) {
        if(dgrC.hur[diag.compMain] ==2 || dgrC.hur[diag.compMain] ==3 ||
           dgrC.hur[diag.compMain] ==5) { //if "*V" (conc varied)
            // set a concentration-type not varied
            if(Util.isGas(namn.identC[diag.compMain])) {
                dgrC.hur[diag.compMain] = 4; //"LA" = "log P"
            } else { //not Gas
                if(dgrC.hur[diag.compMain] ==2 || dgrC.hur[diag.compMain] ==3) { //"TV" or "LTV"
                  if(dgrC.hur[diag.compMain] ==3) { //"LTV"
                    dgrC.cLow[diag.compMain] = logToNoLog(dgrC.cLow[diag.compMain]);
                    dgrC.cHigh[diag.compMain] = logToNoLog(dgrC.cLow[diag.compMain]);
                  } // "LTV"
                  dgrC.hur[diag.compMain] = 1; //"T" = "Total conc."
                } //if "TV" or "LTV"
                else { //"LAV"
                  dgrC.hur[diag.compMain] = 4; //"LA" = "log (activity)"
                } //"T"?
            } //not Gas
        } //if conc varied
    } //if Main comp. not in axis (if Main != compX & Main != compY)

    runPredomSED = 1; // flag for a Predom diagram
    diag.plotType = 0;

  } //if user selected a Predom diagram

  else {  //user selected a SED diagram
  // ---------------------------------------------------------------------------
  // -----  SED:
  // -----------

    setOKButtonIcon("images/PlotLog256_32x32_traspBckgr.gif");

    if(!loading) {

        if(runPredomSED == 1) {
            // --- The user changes from a PREDOM  to a SED-diagram:
            // --- select a default concentration
            //     for the component that was in the Y-axis
            if(diag.compY >=0) {
                if(diag.compY == ePresent
                        && (dgrC.hur[diag.compY] ==4 || dgrC.hur[diag.compY] ==5)) { // "A"
                    dgrC.cLow[diag.compY] = -8.5;
                    dgrC.hur[diag.compY] = 4; // "LA" = "pe"
                } //if "e-" & "A"
                else
                if(diag.compY == hPresent
                        && (dgrC.hur[diag.compY] ==4 || dgrC.hur[diag.compY] ==5)) { // "A"
                    dgrC.cLow[diag.compY] = -7;
                    dgrC.hur[diag.compY] = 4; // "LA" = "pH"
                } //if "H+" & "A"
                else
                if(Util.isWater(namn.identC[diag.compY])) {
                    dgrC.cLow[diag.compY] = 0;
                    dgrC.hur[diag.compY] = 4; // "LA" = "log (activity)"
                } //if is H2O
                else
                if(dgrC.hur[diag.compY] ==4 || dgrC.hur[diag.compY] ==5) { //"LA"
                    dgrC.hur[diag.compY] = 4; // "LA" = "log (activity)"
                } //if "LA*"
                else { //must be "LTV" or "TV"
                    if(dgrC.hur[diag.compY] ==3) { // "LTV"
                        dgrC.cLow[diag.compY] = logToNoLog(dgrC.cLow[diag.compY]);
                        dgrC.cHigh[diag.compY] = logToNoLog(dgrC.cHigh[diag.compY]);
                    } //"LTV"
                    dgrC.hur[diag.compY] = 1; // "T" = "Total conc."
                } //"LTV" or "TV"
            } //if compY >=0

            diag.compY = -1; diag.compMain = -1;
            // --- for logarithmic plot: set default axes range
            if(dt.equalsIgnoreCase("Logarithmic") || dt.equalsIgnoreCase("log Solubilities")
                    || dt.equalsIgnoreCase("log Activities")) {
                if(!diag.inputYMinMax) {
                    jTextFieldYmin.setText("-9");
                    jTextFieldYmax.setText("1");
                } else {
                    jTextFieldYmin.setText(String.valueOf(diag.yLow));
                    jTextFieldYmax.setText(String.valueOf(diag.yHigh));
                }
            } //if "log"
        } //if the user changes from PREDOM to SED

        if(dt.equalsIgnoreCase("Fraction")) {diag.plotType = 1;}
        if(dt.equalsIgnoreCase("log Solubilities")) {diag.plotType = 2;}
        if(dt.equalsIgnoreCase("Logarithmic")) {diag.plotType = 3;}
        if(dt.equalsIgnoreCase("Relative activities")) {diag.plotType = 4;}
        if(dt.equalsIgnoreCase("calculated pe") || dt.equalsIgnoreCase("calculated Eh")) {diag.plotType = 5;}
        if(dt.equalsIgnoreCase("calculated pH")) {diag.plotType = 6;}
        if(dt.equalsIgnoreCase("log Activities")) {diag.plotType = 7;}
        if(dt.equalsIgnoreCase("H+ affinity spectrum")) {diag.plotType = 8;}

    } //if not loading

    cl = (java.awt.CardLayout)jPanelSedPredom.getLayout();
    if(pd.advancedVersion) {
      cl.show(jPanelSedPredom, "SED");
    } else {
      cl.show(jPanelSedPredom, "Empty"); //.setVisible(false);
    }
    //cl = (java.awt.CardLayout)jPanelMainComp.getLayout();
    //cl.show(jPanelMainComp, "cardMainComp0"); //.setVisible(false);
    enableYminYmax(true);
    jComboBoxMainComp.setVisible(false);

    // Calculated-pH or calculated-pe/Eh ?
    if(Ycalc >= 0) {
      if(Ycalc == ePresent &&
            (!dt.equalsIgnoreCase("calculated pe") &&
             !dt.equalsIgnoreCase("calculated Eh"))) {Ycalc = -1;} //not calc pe/Eh
      if(Ycalc == hPresent && !dt.equalsIgnoreCase("calculated pH")) {Ycalc = -1;} //not calc pH
    }
    if(dt.equalsIgnoreCase("calculated pH") || //calc pH
       dt.equalsIgnoreCase("calculated pe") ||
       dt.equalsIgnoreCase("calculated Eh")) { //calc pe/Eh
        diag.compY = -1;
        cl = (java.awt.CardLayout)jPanelYaxComp.getLayout();
        cl.show(jPanelYaxComp, "cardYaxComp0"); // .setVisible(false);
        if(dt.equalsIgnoreCase("calculated pe") || dt.equalsIgnoreCase("calculated Eh")) {
            Ycalc = ePresent;
        } else {
            Ycalc = hPresent;
        }
        if(Ycalc >=0 && Ycalc < cs.Na) { //select default conc-type
          if(dgrC.hur[Ycalc] ==4) { //"LA"
              dgrC.hur[Ycalc] =1; //"T" = "Total conc."
              dgrC.cLow[Ycalc] = logToNoLog(dgrC.cLow[Ycalc]);
          } else if(dgrC.hur[Ycalc] ==5)  { //"LAV"
              dgrC.hur[Ycalc] =3; //"LTV" = "log (Total conc.) varied"
          }
        }
        jLabelYcalc.setText(dt);
        cl = (java.awt.CardLayout)jPanelYaxInner.getLayout();
        cl.show(jPanelYaxInner, "cardYcalc");
        cl = (java.awt.CardLayout)jPanelYaxComp.getLayout();
        cl.show(jPanelYaxComp, "cardYaxComp0"); // .setVisible(false);
        setOKButtonIcon("images/PlotPHpe256_32x32_transpBckgr.gif");

        if(dt.equalsIgnoreCase("calculated pH")) { //calc pH
            if(!jTextFieldYmin.isVisible()
                || jTextFieldYmin.getText().contains("%") || jTextFieldYmax.getText().contains("%")
                || readTextField(jTextFieldYmin)<0
                || readTextField(jTextFieldYmax)<0 || readTextField(jTextFieldYmax)>14) {
                    jTextFieldYmin.setText("0");
                    jTextFieldYmax.setText("14");
            }
        } //if "calc pH"
        if(dt.equalsIgnoreCase("calculated pe")) {
            if(!jTextFieldYmin.isVisible()
                || jTextFieldYmin.getText().contains("%") || jTextFieldYmax.getText().contains("%")
                || readTextField(jTextFieldYmin)>=0 || readTextField(jTextFieldYmax)<=0) {
                    jTextFieldYmin.setText("-17");
                    jTextFieldYmax.setText("17");
            }
        } //if "calc pe"
        if(dt.equalsIgnoreCase("calculated Eh")) {
            System.out.println("## calculated Eh");
            if(!jTextFieldYmin.isVisible()
                || jTextFieldYmin.getText().contains("%") || jTextFieldYmax.getText().contains("%")
                || readTextField(jTextFieldYmin)>0 || readTextField(jTextFieldYmin)<-2
                || readTextField(jTextFieldYmax)<0 || readTextField(jTextFieldYmax)>2) {
                    jTextFieldYmin.setText("-1");
                    jTextFieldYmax.setText("1");
            }
        } //if "calc Eh"
    } //if "calc pH/pe/Eh"

    if(dt.equalsIgnoreCase("Logarithmic") || //logarithmic
            dt.equalsIgnoreCase("log Solubilities") || //log Solubilities
            dt.equalsIgnoreCase("log Activities")) {//log Activities
        diag.compY = -1;
        cl = (java.awt.CardLayout)jPanelYaxComp.getLayout();
        cl.show(jPanelYaxComp, "cardYaxComp0"); // .setVisible(false);
        cl = (java.awt.CardLayout)jPanelYaxInner.getLayout();
        if(dt.equalsIgnoreCase("Logarithmic")) { //logarithmic
            cl.show(jPanelYaxInner, "cardYlogC");
        } else if(dt.equalsIgnoreCase("log Solubilities")) { //log solub.
            cl.show(jPanelYaxInner, "cardYlogS");
        } else if(dt.equalsIgnoreCase("log Activities")) { //log activities
            cl.show(jPanelYaxInner, "cardYlogA");
        }
        if(readTextField(jTextFieldYmin) >=-1) {
            if(!diag.inputYMinMax || (oldPlotType !=3 && oldPlotType !=7) ) {
                jTextFieldYmin.setText("-9");
            } else {jTextFieldYmin.setText(String.valueOf(diag.yLow));}
        }
        if(readTextField(jTextFieldYmax) >= 1) {
            if(!diag.inputYMinMax || (oldPlotType !=3 && oldPlotType !=7) ) {
                jTextFieldYmax.setText("1");
            } else {jTextFieldYmax.setText(String.valueOf(diag.yHigh));}
        }
    } // if "log / logSolub / logAct"

    if(dt.equalsIgnoreCase("H+ affinity spectrum")) {
        diag.compY = -1;
        cl = (java.awt.CardLayout)jPanelYaxComp.getLayout();
        cl.show(jPanelYaxComp, "cardYaxComp0"); // .setVisible(false);
        cl = (java.awt.CardLayout)jPanelYaxInner.getLayout();
        cl.show(jPanelYaxInner, "cardYHaff"); // .setVisible(false);
        setOKButtonIcon("images/PlotPHpe256_32x32_transpBckgr.gif");
        jTextFieldYmin.setVisible(false);
        jTextFieldYmax.setVisible(false);
    } // if "H+ affinity spectrum"
    else {
        jTextFieldYmin.setVisible(true);
        jTextFieldYmax.setVisible(true);
        jPanelYaxis.validate();
    }

    if(dt.equalsIgnoreCase("Relative activities")) { //Relative activities
        boolean includeSpecies = true; //both components and reaction products in Y-axis
        updateYcomp(includeSpecies);
        updatingAxes = true;
        if(readTextField(jTextFieldYmin) >= -1) {jTextFieldYmin.setText("-6");}
        if(readTextField(jTextFieldYmax) < 1.1) {jTextFieldYmax.setText("6");}
        setOKButtonIcon("images/PlotRel256_32x32_transpBckgr.gif");
        cl = (java.awt.CardLayout)jPanelYaxInner.getLayout();
        cl.show(jPanelYaxInner, "cardYRef");
        cl = (java.awt.CardLayout)jPanelYaxComp.getLayout();
        cl.show(jPanelYaxComp, "cardYaxComp1"); // .setVisible(true);
    } // if "Relative activities"
    else { // not "Relative activities"
        boolean includeSpecies = false; //only components in Y-axis
        updateYcomp(includeSpecies);
        updatingAxes = true;
    }

    if(dt.equalsIgnoreCase("Fraction")) { //Fraction
        cl = (java.awt.CardLayout)jPanelYaxInner.getLayout();
        cl.show(jPanelYaxInner, "cardYFract");
        jLabelYFract.setText("Fractions for:");
        cl = (java.awt.CardLayout)jPanelYaxComp.getLayout();
        cl.show(jPanelYaxComp, "cardYaxComp1"); // .setVisible(true);
        setOKButtonIcon("images/PlotFrctn16_32x32_transpBckgr.gif");
        // change from non-Fraction to Faction:
        //     set a default component and set its conc. value
        //     select the 1st cation, or 1st solid, gas, aqu.
        if(diag.compY < 0) {
            for(int ic = 0; ic < cs.Na; ic++) {
                if(ic != hPresent && Util.isCation(namn.identC[ic])
                   && !namn.ident[ic].startsWith("NH") && !namn.ident[ic].startsWith("N(")) {
                        diag.compY = ic; break;
                }
            } // for ic
        } // if compY < 0
        if(diag.compY < 0 && cs.solidC >0) { // get the first solid component
            diag.compY = (cs.Na - cs.solidC);
        } // if compY < 0
        if(diag.compY < 0) { // get a gas
            for(int ic = 0; ic < cs.Na; ic++) {
              if(Util.isGas(namn.identC[ic])) {diag.compY = ic; break;}
            } // for ic
        } // if compY < 0
        if(diag.compY < 0) { // get an aqueous species
            for(int ic = 0; ic < cs.Na; ic++) {
              if(Util.isNeutralAqu(namn.identC[ic])) {diag.compY = ic; break;}
            } // for ic
        } // if compY < 0
        if(diag.compY < 0) { //get a cation, even if it is ammonium
            for(int ic = 0; ic < cs.Na; ic++) {
                if(ic != hPresent && Util.isCation(namn.identC[ic])) {diag.compY = ic; break;}
            } // for ic
        } // if compY < 0
        if(diag.compY < 0) {
          int j=0;
          if(diag.compX == 0 && jComboBoxYaxComp.getItemCount() >1) {j=1;}
          for(int ic=0; ic < cs.Na; ic++) {
            if(namn.identC[ic].equalsIgnoreCase(jComboBoxYaxComp.getItemAt(j).toString())) {
              diag.compY = ic; break;
            }
        } //for ic
        } // if compY < 0

        // set the selected item
        for(int j = 0; j < jComboBoxYaxComp.getItemCount(); j++) {
            if(namn.identC[diag.compY].equalsIgnoreCase(jComboBoxYaxComp.getItemAt(j).toString())) {
                jComboBoxYaxComp.setSelectedIndex(j); break;
            }
        } // for j

    } // if "Fraction"

    runPredomSED = 2; // flag for a SED diagram

  } //if user selected a SED diagram
  // -------------------------------------------------------------

  // check for "%" in min/max Y-values
  if(!dt.equalsIgnoreCase("Fraction")) { //not Fraction?
    if(jTextFieldYmin.getText().contains("%") ||
            jTextFieldYmax.getText().contains("%")) {
        jTextFieldYmin.setText("-9");
        jTextFieldYmax.setText("1");
    }
  } else {
        jTextFieldYmax.setText("100 %");
        jTextFieldYmin.setText("0 %");
        resizeYMin();
        resizeYMax();
        enableYminYmax(false);
  } // Fraction?

  updateAxes(); // set concentration types
  updatingAxes = false;

  updateUseEhCheckBox();
  updateDrawPHlineBox();
  enableTemperature();

  updateConcs(); // Change the concentration type and Min/Max values

  resizeXMinMax();
  resizeYMin(); resizeYMax();

  if(pc.dbg) {System.out.println("--- diagramType_Click() -- ends");}

  } // diagramType_Click()

  /** Change the icon displayed by the OK-button.
   * @param iconName the name of the file containing the icon in "gif" format */
  private void setOKButtonIcon(String iconName) {
    java.net.URL imgURL = this.getClass().getResource(iconName);
    if (imgURL != null) {jButton_OK.setIcon(new javax.swing.ImageIcon(imgURL));}
        else {System.out.println("--- Error: Could not load image = \""+iconName+"\"");}
  } // setOKButtonIcon(iconName)
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="enable Ymin/Ymax">
  /** Enables or disables the Y-axes min and max boxes 
   * @param action if true, the Y-min and max boxes are enabled, if false they are disabled
   */
  private synchronized void enableYminYmax(boolean action) {
    enableYmin(action);
    enableYmax(action);
  } // enableYminYmax(b)
  private synchronized void enableYmin(boolean action) {
    if(action) {
      jTextFieldYmin.setBackground(java.awt.Color.WHITE);
    }
    else { // do not enable
      jTextFieldYmin.setBackground(backg);
    }
    jTextFieldYmin.setEditable(action);
    jTextFieldYmin.setFocusable(action);
  } // enableYmin(b)
  private synchronized void enableYmax(boolean action) {
    if(action) {
      jTextFieldYmax.setBackground(java.awt.Color.WHITE);
    }
    else { // do not enable
      jTextFieldYmax.setBackground(backg);
    }
    jTextFieldYmax.setEditable(action);
    jTextFieldYmax.setFocusable(action);
  } // enableYmax(b)
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="focusLost() CLow/CHigh">
  private void focusLostCLow() {
    double w = readTextField(jTextFieldCLow);
    if(w == -0.0) {w = 0;}
    String t = Util.formatNum(w);
    if(Double.parseDouble(t) != w) {jTextFieldCLow.setText(t);}
  }
  private void focusLostCHigh() {
    double w = readTextField(jTextFieldCHigh);
    if(w == -0.0) {w = 0;}
    String t = Util.formatNum(w);
    if(Double.parseDouble(t) != w) {jTextFieldCHigh.setText(t);}
  }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="focusLost X/Y Max/Min">
  private void focusLostXmax() {
    double w = readTextField(jTextFieldXmax);
    if(w == -0.0) {w = 0;}
    String t = Util.formatNum(w); //String.valueOf(w);
    if(Double.parseDouble(t) != w) {jTextFieldXmax.setText(t);}
    resizeXMinMax();
    updateConcs();
  } // focusLostXmax()
  private void focusLostXmin() {
    double w = readTextField(jTextFieldXmin);
    if(w == -0.0) {w = 0;}
    String t = Util.formatNum(w);
    if(Double.parseDouble(t) != w) {jTextFieldXmin.setText(t);}
    resizeXMinMax();
    updateConcs();
  } // focusLostXmin()
  private void focusLostYmax() {
    if(!jTextFieldYmax.isEditable()) {return;}
    double w = readTextField(jTextFieldYmax);
    if(w == -0.0) {w = 0;}
    String t = Util.formatNum(w);
    if(Double.parseDouble(t) != w) {jTextFieldYmax.setText(t);}
    resizeYMax();
    updateConcs();
  } // focusLostYmax()
  private void focusLostYmin() {
    if(!jTextFieldYmin.isEditable()) {return;}
    double w = readTextField(jTextFieldYmin);
    if(w == -0.0) {w = 0;}
    String t = Util.formatNum(w);
    if(Double.parseDouble(t) != w) {jTextFieldYmin.setText(t);}
    resizeYMin();
    updateConcs();
  } // focusLostYmin()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="getConc_ComboConcType_Click()">
  private void getConc_ComboConcType_Click() {
    if(jComboBoxConcType.getSelectedIndex() < 0) {return;}
    //if(pc.dbg) {System.out.println("getConc_ComboConcType_Click()");}
    int k = jComboBoxConcType.getItemCount() -1;
    if(jComboBoxConcType.getItemAt(k).toString().startsWith("?")) {
      if(jComboBoxConcType.getSelectedIndex() == k) {jComboBoxConcType.setSelectedIndex(-1);}
      jComboBoxConcType.removeItemAt(k);
    }
    String tn = jComboBoxConcType.getSelectedItem().toString();
    String t0 = comboBoxConcType0;
    boolean vary = tn.toLowerCase().contains("varied");
    double w1 = readTextField(jTextFieldCLow);
    double w2 = readTextField(jTextFieldCHigh);
    boolean nowTot = tn.toLowerCase().startsWith("tot");
    boolean nowLog = tn.toLowerCase().startsWith("log");
    boolean now_pHpe = (tn.startsWith("pH") || tn.startsWith("pe"));
    //boolean nowEh = tn.toLowerCase().startsWith("Eh");

    boolean tot0 = t0.toLowerCase().startsWith("tot");
    boolean log0 = t0.toLowerCase().startsWith("log");
    boolean pHpe0 = (t0.startsWith("pH") || t0.startsWith("pe"));
    boolean Eh0 = t0.toLowerCase().startsWith("Eh");

    if(!vary) {
      jTextFieldCHigh.setText(" ");
      jTextFieldCHigh.setVisible(false);
      jLabelTo.setText(" "); jLabelFrom.setText(" ");
    } else { //vary
      jTextFieldCHigh.setVisible(true);
      jLabelTo.setText("to:"); jLabelFrom.setText("from:");
      if(jTextFieldCHigh.getText().equals(" ")) {
        if(nowTot) {
          if(log0) {w2 = w1 +2;}
          else if(pHpe0) {w2 = w1 -2;}
          else {w2 = w1 +100;}
        } //if nowTot
        else { //now: log / pH /pe /Eh
          if(log0 || pHpe0) {w2 = w1 +2;}
          else if(Eh0) {
            if(w1 >0) {w2 = -0.5;} else {w2 = +0.5;}
          } else {
            w2 = w1 * 100;
          }
        }
      } //if text = " "
    } //vary?

    if(!t0.equalsIgnoreCase(tn)) { // conc. type has been changed
        if(nowLog && tot0) {
            if(vary && w2>0) {w2 = Math.log10(w2);} else {w2 = 0;}
            if(w1>0) {w1 = Math.log10(readTextField(jTextFieldCLow));}
            else {
                if(vary && w2>0) {w1 = Math.log10(w2) -2d;} else {w1 = -6;}
            }
        } else if((nowLog && pHpe0) || (now_pHpe && log0)) {
            w1 = -w1;
            if(vary) {w2 = -w2;}
        } else if(now_pHpe && tot0) {
            if(w1>0) {w1 = -Math.log10(w1);}
            if(vary && w2>0) {w2 = -Math.log10(w2);}
        } else if(nowTot && log0) {
            if(w1<20) {w1 = logToNoLog(w1);}
            if(vary && w2<20) {w2 = logToNoLog(w2);}
        } else if(nowTot && pHpe0) {
            if(w1>-20) {w1 = logToNoLog(-w1);}
            if(vary && w2>-20) {w2 = logToNoLog(-w2);}
        } else if(nowTot && tot0) {
            if(vary) {
                if(Math.abs(w1) > 1e-10) {
                    if(w1 >0) {w2 = w1; w1 = 0;}
                    else if(w1 < 0) {w2 = 0;}
                } else {if(w1 !=0) {w2 = Math.signum(w1)*0.01;} else {w2 = 0.01;}}
            } else {
                if(Math.abs(w1) < 1e-10) {w1 = w2;}
            }
        }
        comboBoxConcType0 = tn;
    } //if t0 != tn

    if(vary && !runRevs) {
        if(w2 < w1) {double w = w1; w1 = w2; w2 = w;}
    }

    if(!vary && nowLog
            && getConc_idx > (cs.Na - cs.solidC -1)) { // solid component
        // if the concentration type starts with "log" and it is not varied, then it must be "log Ativity"
        w1 = 0; // set log Activity for a solid = 0 (activity = 1)
    }

    jTextFieldCLow.setText(Util.formatNum(w1));
    if(vary) {jTextFieldCHigh.setText(Util.formatNum(w2));}
    jPanelConcInner.validate();

  } //getConc_ComboConcType_Click()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="getConc_Unload()">
  private void getConc_Unload() {
    if(pc.dbg) {System.out.println("getConc_Unload()");}
    jButton_OK.setEnabled(true);
    jButton_Cancel.setEnabled(true);
    jButtonSaveDef.setEnabled(true);
    updateUseEhCheckBox();
    updateDrawPHlineBox();
    enableTemperature();
    java.awt.CardLayout cl = (java.awt.CardLayout)jPanelConcs.getLayout();
    cl.show(jPanelConcs, "panelCompConcList");

    if(getConc_idx >= 0) {
        String compConcText = setConcText(getConc_idx);
        listCompConcModel.set(getConc_idx, compConcText);
        jListCompConc.setSelectedIndex(getConc_idx);
    }
    // set Xmin/max, Ymin/max  and conc. types in axes
    // from the values given in the "Conc. List Box"
    updateAxes();

    resizeXMinMax(); resizeYMin(); resizeYMax();

    jListCompConc.requestFocus();
    if(getConc_idx >= 0) {
        jListCompConc.setSelectedIndex(getConc_idx);
        jListCompConc.ensureIndexIsVisible(getConc_idx);
    }
    getConc_idx = -1;
  } // getConc_Unload()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="paintDiagrPanel">
    private void paintDiagPanel(java.awt.Graphics g) {
        // add tick marks to the diagram panel
        java.awt.Graphics2D g2D = (java.awt.Graphics2D)g;
        java.awt.Dimension ps = jPanelAxes.getSize();
	g.setColor(java.awt.Color.BLACK);
        // the length of the tick marks
        int d = Math.min((int)(ps.getWidth() / 15),(int)(ps.getHeight() / 15));
        for(int i=1; i<=5; i++) {
            int y = (int)ps.getHeight() - (int)((double)i * ps.getHeight()/6);
            g.drawLine(0,y,d,y);
        }
        int y = (int)ps.getHeight();
        for(int i=1; i<=5; i++) {
            int x = (int)((double)i * ps.getWidth()/6);
            g.drawLine(x,y,x,(y-d));
        }
    } // paintDiagPanel(Graphics g)
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="readDataFile()">
  private boolean readDataFile(boolean dbg){
    if(dbg) {System.out.println("readDataFile("+dbg+")");}
    //--- create a ReadData instance
    ReadDataLib rd;
    try {rd = new ReadDataLib(dataFile);}
    catch (ReadDataLib.DataFileException ex) {
        MsgExceptn.exception(ex.getMessage()); return false;}
    if(dbg) {
        System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - -");
        System.out.println("Reading input data file \""+dataFile+"\"");
    }
    //--- read the chemical system (names, equilibrium constants, stoichiometry)
    // report missing plot data as a warning (do not throw an exception)
    boolean warn = true;
    try {
      ch = ReadChemSyst.readChemSystAndPlotInfo(rd, dbg, warn, System.out);
    }
    catch (ReadChemSyst.DataLimitsException ex) {
        MsgExceptn.exception(ex.getMessage());
        ch = null;}
    catch (ReadChemSyst.ReadDataFileException ex) {
        MsgExceptn.exception(ex.getMessage());
        ch = null;}
    catch (ReadChemSyst.PlotDataException ex) {}
    catch (ReadChemSyst.ConcDataException ex) {
        MsgExceptn.exception(ex.getMessage()+nl+Util.stack2string(ex));
        ch = null;
    }
    if(ch == null) {
        try {rd.close();}
        catch (ReadDataLib.ReadDataLibException ex) {MsgExceptn.exception(ex.getMessage());}
        MsgExceptn.showErrMsg(spana.MainFrame.getInstance(),
                "Error while reading file"+nl+"\""+dataFile+"\"", 1);
        return false;}
    //--- set the references pointing to the instances of the storage classes
    cs = ch.chemSystem;
    namn = cs.namn;
    diag = ch.diag;
    dgrC = ch.diagrConcs;

    componentConcType = new int[cs.Na];

    //--- temperature written as a comment in the data file?
    double w;
    try {w = rd.getTemperature();}
    catch (ReadDataLib.DataReadException ex) {
        MsgExceptn.exception(nl+ex.getMessage());
        w = 25.;
    }
    temperatureGivenInInputFile = !Double.isNaN(w);
    if(Double.isNaN(w)) {w = 25;}
    w = Math.min(1000., Math.max(-50, w));
    diag.temperature = w;
    jTextFieldT.setText(Util.formatNum(diag.temperature));
    //--- pressure written as a comment in the data file?
    try {w = rd.getPressure();}
    catch (ReadDataLib.DataReadException ex) {
        MsgExceptn.exception(nl+ex.getMessage());
        w = 1.;
    }
    if(Double.isNaN(w)) {w = 1.;}
    w = Math.min(10000., Math.max(1., w));
    diag.pressure = w;

    // get number of gases
    nbrGases = 0;
    for(int i=0; i < cs.Ms-cs.mSol; i++) {if(Util.isGas(namn.ident[i])) {nbrGases++;}}

    // which components are "H+" and "e-"
    hPresent = -1; ePresent = -1;
    for(int i=0; i < cs.Na; i++) {
      if(Util.isProton(namn.identC[i])) {hPresent = i; if(ePresent >-1) {break;}}
      if(Util.isElectron(namn.identC[i])) {ePresent = i; if(hPresent >-1) {break;}}
    }
    for(int i=0; i < cs.nx; i++) {
      if(Util.isProton(namn.ident[i+cs.Na])) {hPresent = i; if(ePresent >-1) {break;}}
      if(Util.isElectron(namn.ident[i+cs.Na])) {ePresent = i; if(hPresent >-1) {break;}}
    }

    // is the plot information or concentrations missing?
    plotAndConcsNotGiven = false;
    if(diag.plotType < 0 || dgrC.hur[0] <1) {
        // make sure a message box is displayed
        plotAndConcsNotGiven = true;
        diag.Eh = runUseEh; // use default
        // set default plot type and X-axis component
        DefaultPlotAndConcs.setDefaultPlot(cs, diag, dgrC);
        // set default concentrations for each component
        DefaultPlotAndConcs.setDefaultConcs(cs, dgrC, pd.kth);
        DefaultPlotAndConcs.checkConcsInAxesAndMain(namn, diag, dgrC, dbg, pd.kth);
    } // plot information or concentrations missing?
    if(diag.title == null) {diag.title = "";} else {diag.title = Util.rTrim(diag.title);}

    try {rd.close();}
    catch (ReadDataLib.ReadDataLibException ex) {MsgExceptn.exception(ex.getMessage());}
    if(dbg) {System.out.println("Finished reading the input data file");
                System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - -");
    }

    checkComponentsInAxes();
    checkMainComponent();
    checkInputConcs();

    // make a backup copy of the original data
    try {diag0 = (Chem.Diagr)diag.clone(); }
    catch(CloneNotSupportedException ex) { MsgExceptn.exception("Error: "+ex.getMessage()); }
    try {dgrC0 = (Chem.DiagrConcs)dgrC.clone(); }
    catch(CloneNotSupportedException ex) { MsgExceptn.exception("Error: "+ex.getMessage()); }

    return true;
  } // readDataFile()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="readTextField (X/Y Min/Max)">
/** Returns a value contained in a text field, catching any errors
 * @param textField the text field to read
 * @return the value found in the text field, or Not_a_Number
 * if the text field is not visible or if an error occurs.
 */
  private double readTextField(javax.swing.JTextField textField) {
    double w;
    if(!textField.isVisible()) {w = 0.;}
    else {
      String t = textField.getText().trim();
      if(t.length() <= 0) {return 0;}
      if(t.endsWith("%")) {t = t.substring(0,t.length()-1).trim();}
      try{w = Double.valueOf(t);}
      catch(NumberFormatException ex) {
        String type;
        if(textField.getName().endsWith("Xmin")) {
            type = "X-min";
            try{w = Double.valueOf(oldTextXmin); textField.setText(oldTextXmin);}
            catch (NumberFormatException ex2) {w = 0.;}
        }
        else if(textField.getName().endsWith("Xmax")) {
            type = "X-max";
            try{w = Double.valueOf(oldTextXmax); textField.setText(oldTextXmax);}
            catch (NumberFormatException ex2) {w = 0.;}
        }
        else if(textField.getName().endsWith("Ymin")) {
            type = "Y-min";
            try{w = Double.valueOf(oldTextYmin); textField.setText(oldTextYmin);}
            catch (NumberFormatException ex2) {w = 0.;}
        }
        else if(textField.getName().endsWith("Y-max")) {
            type = "Y-max";
            try{w = Double.valueOf(oldTextYmax); textField.setText(oldTextYmax);}
            catch (NumberFormatException ex2) {w = 0.;}
        }
        else if(textField.getName().endsWith("CLow")) {
            type = "C-low";
            try{w = Double.valueOf(oldTextCLow); textField.setText(oldTextCLow);}
            catch (NumberFormatException ex2) {w = 0.;}
        }
        else if(textField.getName().endsWith("CHigh")) {
            type = "C-high";
            try{w = Double.valueOf(oldTextCHigh); textField.setText(oldTextCHigh);}
            catch (NumberFormatException ex2) {w = 0.;}
        }
        else if(textField.getName().endsWith("IonicStr")) {
            type = "ionic strength";
            try{w = Double.valueOf(oldTextI); textField.setText(oldTextI);}
            catch (NumberFormatException ex2) {w = 0.;}
        }
        else if(textField.getName().endsWith("T")) {
            type = "temperature";
            try{w = Double.valueOf(oldTextT); textField.setText(oldTextT);}
            catch (NumberFormatException ex2) {w = 0.;}
        }
        else {type = ""; w = 0.;}
        String msg = "Error (NumberFormatException)";
        if(type.trim().length() >0) {msg = msg+nl+"reading "+type;}
        msg = msg +nl+"with text: \""+t+"\"";
        System.out.println(LINE+nl+msg+nl+LINE);
        javax.swing.JOptionPane.showMessageDialog(this, msg,
                pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
        
      }
    }
    return w;
  }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="resize X/Y Max/Min ()">
  private synchronized void resizeXMinMax() {
    String t;
    t = jTextFieldXmin.getText();
    if(t == null || t.length() <= 1) {t = "m";}
    jLabl1.setText(t);
    jLabl1.invalidate();
    t = jTextFieldXmax.getText();
    if(t == null || t.length() <= 1) {t = "m";}
    jLabl2.setText(t);
    jLabl2.invalidate();
    jPanelLabl.validate();
    // find out how large can each field be
    int m = jPanelXaxis.getWidth()-10; int h = m/2;
    int ixMin = Math.max(jLabl1.getWidth()+10,20);
    int ixMax = Math.max(jLabl2.getWidth()+10,20);
    if(ixMin > h && ixMax > h) {ixMin = h; ixMax = h;} // both are wider than half the space available
    else if((ixMin+ixMax) >= m) {
        // the sum of both widths is larger than the total available,
        // but one of them is smaller than half
        if(ixMin > ixMax) {ixMin = m - ixMax;} else {ixMax = m - ixMin;}
    }
    java.awt.Dimension dL1 = new java.awt.Dimension(ixMin, jTextFieldXmin.getHeight());
    java.awt.Dimension dL2 = new java.awt.Dimension(ixMax, jTextFieldXmax.getHeight());

    jTextFieldXmin.setMaximumSize(dL1);   jTextFieldXmax.setMaximumSize(dL2);
    jTextFieldXmin.setPreferredSize(dL1); jTextFieldXmax.setPreferredSize(dL2);
    jTextFieldXmin.invalidate();          jTextFieldXmin.invalidate();
    jPanelXaxis.validate();
  } // resizeXMinMax()

  private synchronized void resizeYMin() {
    if(!jTextFieldYmin.isEditable()) {return;}
    //java.awt.CardLayout cl = (java.awt.CardLayout)jPanelConcs.getLayout();
    //cl.show(jPanelConcs, "panelJLabl");
    //new java.lang.Exception().printStackTrace();
    String t = jTextFieldYmin.getText();
    if(t == null || t.length() <= 1) {t="m";}
    jLabl3.setText(t);
    jLabl3.invalidate();
    jPanelLabl.validate();
    java.awt.Dimension dL1 = new java.awt.Dimension(Math.max(jLabl3.getWidth()+10,20), jTextFieldYmin.getHeight());
    jTextFieldYmin.setMaximumSize(dL1);
    jTextFieldYmin.setPreferredSize(dL1);
    jTextFieldYmin.invalidate();
    jPanelYmin.validate();
  } // resizeYMin()

  private synchronized void resizeYMax() {
    if(!jTextFieldYmax.isEditable()) {return;}
    String t = jTextFieldYmax.getText();
    if(t == null || t.length() <= 1) {t="m";}
    jLabl4.setText(t);
    jLabl4.invalidate();
    jPanelLabl.validate();
    java.awt.Dimension dL1 = new java.awt.Dimension(Math.max(jLabl4.getWidth()+10,20), jTextFieldYmax.getHeight());
    jTextFieldYmax.setMaximumSize(dL1);
    jTextFieldYmax.setPreferredSize(dL1);
    jTextFieldYmax.invalidate();
    jPanelYmax.validate();
  } // resizeYMax()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="runSedPredom()">
private boolean runSedPredom() {
  // ===========================
  //  Make a SED/Predom diagram
  // ===========================

  if(pc.dbg) {System.out.println("runSedPredom()");}
  if(MainFrame.pathSedPredom == null) {
      MsgExceptn.exception("Programming error in runSedPredom(): \"pathSedPredom\" is null.");
      return false;
  }
  pc.setPathDef(dataFile);
  lastPlotFileName = jTextFieldDiagName.getText();
  lastDataFileName = jTextFieldDataFile.getText();
  String dir = pc.pathDef.toString();
  if(dir.endsWith(SLASH)) {dir = dir.substring(0,dir.length()-1);}
  if(dir.trim().length() >0) {
      pltFile = new java.io.File(dir + SLASH + lastPlotFileName.concat(".plt"));
  } else {pltFile = new java.io.File(lastPlotFileName.concat(".plt"));}
  if(pltFile.exists() && (!pltFile.canWrite() || !pltFile.setWritable(true))) {
    String msg = "Warning: Can NOT overwrite the plot file:"+nl+
            "   \""+pltFile.getName()+"\""+nl+
            "Is this file (or folder) write-protected?"+nl+nl+
            "Please choose another plot-file name.";
    System.out.println(LINE+nl+msg+nl+LINE);
    javax.swing.JOptionPane.showMessageDialog(this, msg,
                pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
    return false;
  }

  if(runPredomSED ==2) { // SED diagram
    if(!Div.progSEDexists(new java.io.File(MainFrame.pathSedPredom))) {
        javax.swing.JOptionPane.showMessageDialog(this, "Error: Program SED not found in path:"+nl+
                  "\""+MainFrame.pathSedPredom+"\""+nl+nl+
                  "The calculations can not be performed."+nl+nl+
                  "Please select a correct path in menu \"Preferences / General\"",
                  pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
        return false;
    }
    oldProg = isOldSED();
  }
  else if(runPredomSED ==1) { // Predom diagram
    if(!Div.progPredomExists(new java.io.File(MainFrame.pathSedPredom))) {
        javax.swing.JOptionPane.showMessageDialog(this, "Error: Predom not found in path:"+nl+
                  "\""+MainFrame.pathSedPredom+"\""+nl+nl+
                  "The calculations can not be performed."+nl+nl+
                  "Please select a correct path in menu \"Preferences / General\"",
                  pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
        return false;
    }
    if(diag.compMain < 0) { // check main chemical component
        MsgExceptn.showErrMsg(this, "Programming error in runSedPredom(): diag.compMain < 0!", 1);
        return false;
    }
    if(dgrC.hur[diag.compMain] != 4 
            && (dgrC.hur[diag.compMain] == 1 && Math.abs(dgrC.cLow[diag.compMain]) <= 0 )) {
        Object[] opt = {"Make the diagram anyway", "Cancel"};
        String msg = "The concentration for the"+nl+
                "main component ("+namn.identC[diag.compMain]+") is "+nl+
                "less or equal to zero!";
        System.out.println("----- "+msg);
        int n= javax.swing.JOptionPane.showOptionDialog(this,msg,pc.progName,
                javax.swing.JOptionPane.OK_CANCEL_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
        if(n != javax.swing.JOptionPane.OK_OPTION) {
            System.out.println("----- [Cancel]");
            return false;
        }
        System.out.println("----- [Make the diagram anyway]");
    }
    oldProg = isOldPredom();
  }

  final String prog_name;
  if(runPredomSED ==1) {
    if(oldProg && Div.progExists(new java.io.File(MainFrame.pathSedPredom), "Predom2", "exe")) {
        prog_name = "Predom2";
    } else {
        prog_name = "Predom";
    }
  } else if(runPredomSED ==2) {
    prog_name= "SED";
  } else {prog_name= "(error?)";}

  // ===========================
  //  command-line options
  // ===========================

  java.util.ArrayList<String> options = new java.util.ArrayList<String>();
  options.add("\"-d="+dataFile.getPath()+"\"");
  options.add("\"-p="+pltFile.getPath()+"\"");
  if(!Double.isNaN(diag.temperature)) {options.add("-t="+diag.temperature);}
  if(!Double.isNaN(diag.ionicStrength) && diag.ionicStrength != 0) {
    options.add("-i="+diag.ionicStrength);
    if(!oldProg) {
        if(runActCoeffsMethod >=0 && runActCoeffsMethod <=2) {options.add("-m="+runActCoeffsMethod);}
    }
  }
  if(!Double.isNaN(pd.tolHalta) && pd.tolHalta != Chem.TOL_HALTA_DEF) {
    options.add("-tol="+Math.min(1e-2,Math.max(pd.tolHalta, 1e-9)));
  }
  if(runPredomSED ==2 && runTbl) { // SED table output
    options.add("-tbl");
    if(!oldProg) {
        if(pd.tblExtension != null && pd.tblExtension.length() >0) {options.add("-tble:"+pd.tblExtension);}
        if(pd.tblFieldSeparator == '\u0009') {options.add("-tbls:\\t");}
        else if(pd.tblFieldSeparator == ' ') {options.add("-tbls:\" \"");}
        else {options.add("-tbls:"+pd.tblFieldSeparator);}
        if(pd.tblCommentLineStart != null) {
            if(pd.tblCommentLineStart.length() >0) {options.add("-tblcs:"+pd.tblCommentLineStart);}
            else {options.add("-tblcs: ");} //if not given quotes (") are used as default
        }
        if(pd.tblCommentLineEnd != null) {
            if(pd.tblCommentLineEnd.length() >0) {options.add("-tblce:"+pd.tblCommentLineEnd);}
            else {options.add("-tblce: ");} //if not given quotes (") are used as default
        }
    }
  }

  if(runPredomSED ==2) {  // SED diagram
    runNbrStepsSED = (int)((float)jScrollBarSEDNbrP.getValue()/1f);
    if(runNbrStepsSED != MainFrame.NSTEPS_DEF) {options.add("-n="+(runNbrStepsSED));}
    if(diag.plotType == 1 && !oldProg) { // "fraction"
        options.add("-thr="+(pd.fractionThreshold));
    }
  } else if(runPredomSED ==1) { // Predom diagram
    if(runAqu) {options.add("-aqu");}
    runNbrStepsPred = (int)((float)jScrollBarPredNbrP.getValue()/1f);
    if(runNbrStepsPred != MainFrame.NSTEPS_DEF) {options.add("-n="+(runNbrStepsPred));}
    if(runPHline) {options.add("-pH");}
  }

  if(runConcUnits != 0 && !oldProg) {options.add("-units="+(runConcUnits));}
  if(runConcNottn != 0 && !oldProg) {
      if(runConcNottn == 1) {options.add("-sci");}
      else if(runConcNottn == 2) {options.add("-eng");}
  }

  if(runRevs) {options.add("-rev");}

  if(pd.keepFrame) {
    options.add("-keep");
    if(!oldProg) {
        if(pd.calcDbg) {options.add("-dbg");}
        if(pd.calcDbgHalta != Chem.DBGHALTA_DEF) {options.add("-dbgH="+String.valueOf(pd.calcDbgHalta));}
    }
  } //if "keep"

  args = new String[options.size()];
  args = options.toArray(args);

  final long pltFileDate0 = pltFile.lastModified();
  MainFrame.getInstance().setCursorWait();

  new javax.swing.SwingWorker<Void,Void>() {
    @Override
    protected Void doInBackground() throws Exception {

        if(oldProg) {
            String pSP = MainFrame.pathSedPredom;
            if(pSP != null && pSP.endsWith(SLASH)) {pSP = pSP.substring(0,pSP.length()-1);}
            final String prg;
            if(pSP != null) {prg = pSP + SLASH + prog_name+".exe";} else {prg = prog_name+".exe";}
            boolean waitForCompletion = true;
            lib.huvud.RunProgr.runProgramInProcess(null,prg,args,waitForCompletion,
                  (pc.dbg || pd.calcDbg),pc.pathAPP);
            return null;
        }

        if(!pd.jarClassLd) { // run calculations in a separate system process
            boolean waitForCompletion = true;
            lib.huvud.RunProgr.runProgramInProcess(MainFrame.getInstance(), prog_name+".jar", args,
                waitForCompletion, (pc.dbg || pd.calcDbg), pc.pathAPP);
            return null;
        }

        // ----- neither "old" programs, or ProgramData.jarClassLd:
        //       invoke the main class of the jar-file

        // save the look-and-feel
        javax.swing.LookAndFeel oldLaF = javax.swing.UIManager.getLookAndFeel();
        if(pc.dbg) {System.out.println("--- oldLookAndFeel("+oldLaF.getName()+");");}
        rj = new lib.huvud.RunJar();
        // The "main" method of SED and Predom waits for the calculations to be finished..
        // RunJar uses a separate thread (SwingWorker) to run the programs.
        // Next statement will return when the jar-program ends
        rj.runJarLoadingFile(MainFrame.getInstance(), prog_name+".jar", args, (pc.dbg || pd.calcDbg),
                pc.pathAPP);
        // reset the look-and-feel if needed
        if(!oldLaF.getName().equals(javax.swing.UIManager.getLookAndFeel().getName())) {
            try {
                if(pc.dbg) {System.out.println("--- setLookAndFeel("+oldLaF.getName()+");");}
                javax.swing.UIManager.setLookAndFeel(oldLaF);
            }
            catch (javax.swing.UnsupportedLookAndFeelException ex) {
                System.out.println("--- setLookAndFeel: "+ex.getMessage());
            }
        }

        // --- restore the Mac OS Dock icon
        if(System.getProperty("os.name").startsWith("Mac OS")) {
            String iconName = "images/Spana_icon_48x48.gif";
            java.net.URL imgURL = this.getClass().getResource(iconName);
            if (imgURL != null) {
                java.awt.Image icon = new javax.swing.ImageIcon(imgURL).getImage();
                try {
                    Class<?> c = Class.forName("com.apple.eawt.Application");
                    java.lang.reflect.Method m =
                        c.getDeclaredMethod("setDockIconImage",new Class[] { java.awt.Image.class });
                    Object i = c.newInstance();
                    Object paramsObj[] = new Object[]{icon};
                    m.invoke(i, paramsObj);
                } catch (Exception e) {System.out.println("Error: "+e.getMessage());}
            } else {
                System.out.println("Error: Could not load image = \""+iconName+"\"");
            }
        } //---- Mac OS Dock Icon

        return null;

    } // doInBackground()

    @Override protected void done(){
        long pltFileDate = pltFile.lastModified();
        if(pltFileDate <= pltFileDate0) {
            String msg = "Apparently the program \""+prog_name+"\" has"+nl+
                         "failed the calculations, and the plot-file:"+nl+
                         "    \""+pltFile.getName()+"\""+nl+
                         "has NOT been generated.";
            if(pc.dbg || pd.calcDbg) {System.out.println(msg);}
            javax.swing.JOptionPane.showMessageDialog(MainFrame.getInstance(), msg,
                                pc.progName, javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } else {
            MainFrame.getInstance().displayPlotFile(pltFile.getPath(), null);
        }

        MainFrame.getInstance().setCursorDef();

    } // done()

  }.execute(); // SwingWorker returns inmediately, but it continues running...

  return true;

} // runSedPredom()

 /** returns true if the "old" Fortran program SED.exe exists
  * in the executable-path, <u>and</u> if "SED.jar" is not found. False otherwise  */
  private static boolean isOldSED() {
    if(MainFrame.pathSedPredom == null) {return false;}
    java.io.File exePath = new java.io.File(MainFrame.pathSedPredom);
    if(!exePath.exists()) {return false;}
    if(Div.progExists(exePath, "SED","jar")) {return false;}
    if(!System.getProperty("os.name").toLowerCase().startsWith("windows")) {
        if(!Div.progExists(exePath, "SED","exe")) {return false;}
        java.io.File f = new java.io.File(exePath.getPath() + java.io.File.separator + "SED.exe");
        return f.length() >= 100000;
    }
    return false;
  }
 /** returns true if the "old" Fortran program Predom.exe (or Predom2.exe) exists
  * in the executable-path, <u>and</u> if "Predom.jar" is not found. False otherwise  */
  private static boolean isOldPredom() {
    if(MainFrame.pathSedPredom == null) {return false;}
    java.io.File exePath = new java.io.File(MainFrame.pathSedPredom);
    if(!exePath.exists()) {return false;}
    if(Div.progExists(exePath, "Predom","jar")) {return false;}
    if(!System.getProperty("os.name").toLowerCase().startsWith("windows")) {
        if(!Div.progExists(exePath, "Predom","exe")) {
            return Div.progExists(exePath, "Predom2","exe");
        }
        java.io.File f = new java.io.File(exePath.getPath() + java.io.File.separator + "Predom.exe");
        return f.length() >= 100000;
    }
    return false;
  }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="setConcText(component)">
  /** Creates a text String describing the concentration
   * (or range of concentration values) used in the diagram
   * for chemical component "ic"
   * @param ic the chemical component
   * @return a descriptive text String */
  private String setConcText(int ic) {
    if(ic < 0 || ic >= cs.Na) {
        return "? program error in \"setConcText\"; ic = "+String.valueOf(ic);}
    String compConcText = "? unknown conc. type (" + namn.identC[ic] + ")";
    int use_pHpe = 0;
    if(dgrC.hur[ic] >= 4) { // "LA" or "LAV"
        if(ic == hPresent) {use_pHpe =1;}
        else if(ic == ePresent) {use_pHpe =2; if(runUseEh) {use_pHpe =3;}}
    }
    double x1, x2;
    x1 = dgrC.cLow[ic]; x2 = dgrC.cHigh[ic];
    if(use_pHpe == 1 || use_pHpe == 2) {x1 = -x1; x2 = -x2;}
    else if(use_pHpe == 3) {
        double fEh = (MainFrame.Rgas * ln10 * (diag.temperature +273.15)) / MainFrame.Faraday;
        x1 = -x1 * fEh;
        x2 = -x2 * fEh;
    }
    if(!runRevs &&  // Varied: concentration range?
       (dgrC.hur[ic] == 2 || dgrC.hur[ic] == 3 || dgrC.hur[ic] == 5)) { // "TV", "LTV" or "LAV"
            if(x2 < x1) {double w = x1; x1 = x2; x2 = w;}
    }
    String cLowText, cHighText;
    cLowText = Util.formatDbl6(x1);
    if(dgrC.hur[ic] == 2 || dgrC.hur[ic] == 3 || dgrC.hur[ic] == 5) { // varied
        cHighText = Util.formatDbl6(x2);
    } else {cHighText = " ";}
    if(use_pHpe == 1) {compConcText = "pH ";}
    else if(use_pHpe == 2) {compConcText = "pe ";}
    else if(use_pHpe == 3) {compConcText = "Eh ";}
    else if(dgrC.hur[ic] == 4 || dgrC.hur[ic] == 5) {// LA or LAV
        if(Util.isGas(namn.identC[ic])) {
          compConcText = "log P("+namn.identC[ic]+") ";
        } else {
          compConcText = "log activity ("+namn.identC[ic]+") ";
        }
    }
    else if(dgrC.hur[ic] == 1 || dgrC.hur[ic] == 2) {// T or TV
        compConcText = "Total conc. ("+namn.identC[ic]+") ";
    }
    else if(dgrC.hur[ic] == 3) {// LTV
        compConcText = "log Total conc. ("+namn.identC[ic]+") ";
    }
    if(dgrC.hur[ic] == 2 || dgrC.hur[ic] == 3 || dgrC.hur[ic] == 5) {
        // Varied: concentration range?
        compConcText = compConcText + "varied from "+ cLowText +" to "+ cHighText;
    } else {
        compConcText = compConcText + "= "+ cLowText;
    }
    return compConcText;
  } // setConcText(i)
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="setUpFrame()">
  private void setUpFrame() {
    if(pc.dbg) {System.out.println("setUpFrame()");}
    String datFileN = dataFile.getPath();
    jTextFieldDataFile.setText(datFileN);
    jTextFieldDataFile.setCaretPosition(datFileN.length());

    String plotFileN;
    if(lastDataFileName != null && lastDataFileName.equalsIgnoreCase(datFileN)
            && (lastPlotFileName != null && lastPlotFileName.length() >0)) {
        plotFileN = lastPlotFileName;
    } else {
        String txt = dataFile.getName();
        plotFileN = txt.substring(0,txt.length()-4);
    }
    jTextFieldDiagName.setText(plotFileN);    
    if(diag.title != null && diag.title.length() >0) {
        jTextFieldTitle.setText(diag.title);
    } else {jTextFieldTitle.setText("");}

    Ycalc = -1;
    // are "H+" or "e-" a complex?
    // (need to know to fill-in diagram-types in combo box)
    boolean e_Complex = false;
    boolean h_Complex = false;
    for(int i = cs.Na; i < cs.Ms; i++) {
        if(Util.isProton(namn.ident[i])) {
            h_Complex = true;
            if(e_Complex) {break;}
        }
        if(Util.isElectron(namn.ident[i])) {
            e_Complex = true;
            if(h_Complex) {break;}
        }
    } // for i

    // --- ionic strength
    if(cs.nx <=0) { //for non-aqueous systems
        jRadioButtonFixed.doClick();
        diag.ionicStrength = 0;
        jTextFieldIonicStr.setText("0");
    }

    loading = true;

    // --- temperature
    jTextFieldT.setText(Util.formatNum(diag.temperature));
    if(Double.isNaN(diag.temperature)) {
        jCheckBoxUseEh.setSelected(false);
        jCheckBoxUseEh.setEnabled(jTextFieldT.isEnabled());
    }

    // --- set up lists: Main-comp, X-comp and Y-comp
    jComboBoxMainComp.removeAllItems();
    jComboBoxXaxComp.removeAllItems();
    jComboBoxYaxComp.removeAllItems();
    for(int i=0; i < cs.Na; i++) {
        if(!Util.isWater(namn.identC[i])) {
            if(!Util.isElectron(namn.identC[i]) && !Util.isProton(namn.identC[i])) {
                    jComboBoxMainComp.addItem(namn.identC[i]);
            }
            if(i < (cs.Na - cs.solidC) || cs.nx > 0) {
                    jComboBoxXaxComp.addItem(namn.identC[i]);
                    jComboBoxYaxComp.addItem(namn.identC[i]);
            }
        }
    } // for i

    // --- set available diagram types
    jComboBoxDiagType.removeAllItems();
    diagramType_doNothing = true;
    if(jComboBoxMainComp.getItemCount()>0) {
        jComboBoxDiagType.addItem("Predominance Area");
    }
    jComboBoxDiagType.addItem("Logarithmic");
    if(pd.advancedVersion || diag.plotType == 7) {jComboBoxDiagType.addItem("log Activities");}
    jComboBoxDiagType.addItem("Fraction");
    if(cs.mSol > 0 || nbrGases > 0) {jComboBoxDiagType.addItem("log Solubilities");}
    jComboBoxDiagType.addItem("Relative activities");
    if(ePresent >=0 || e_Complex) {
        if(runUseEh) {jComboBoxDiagType.addItem("Calculated Eh");}
        else {jComboBoxDiagType.addItem("Calculated pe");}
    }
    if(hPresent >=0 || h_Complex) {jComboBoxDiagType.addItem("Calculated pH");}
    if((pd.advancedVersion && hPresent >= 0 && hPresent < cs.Na)
            || diag.plotType == 8) {jComboBoxDiagType.addItem("H+ affinity spectrum");}
    diagramType_doNothing = false;

    // is a Predom diagram impossible? (e.g. in a system with only H+ and e-)
    if(jComboBoxMainComp.getItemCount()<=0) {
      if(diag.compMain >= 0 || diag.plotType == 0) {
        diag.compMain = -1;
        diag.plotType = 3; // log Concs.
        diag.yLow = -9; diag.yHigh = 1;
      }
    }

    // --- Y-axis

    // --- set Ymin and Ymax
    if(diag.plotType == 3) { // if "log conc."
        if(!diag.inputYMinMax) {diag.yLow = -9; diag.yHigh = 1;}
    } else if(diag.plotType == 2) { // if "log solubility"
        if(cs.mSol < 1) { // no solids?
          diag.plotType = 3; diag.yLow = -9; diag.yHigh = 1;
        } else {
          if(!diag.inputYMinMax) {diag.yLow = -16; diag.yHigh = 0;}
        }
    }
    if((ePresent < 0 && !e_Complex && diag.plotType == 5) || //calc. pe
       (hPresent < 0 && !h_Complex && diag.plotType == 6)) { //calc. pH
            diag.plotType = 3;
            diag.yLow = -9; diag.yHigh = 1;
    }
    jTextFieldYmin.setText(String.valueOf(diag.yLow));
    jTextFieldYmax.setText(String.valueOf(diag.yHigh));

    //--- make sure that diag.compX (and diag.compY) are >=0
    if(diag.compX >= 0) {
      int ic = diag.compX;
      diag.compX = -1;
      for(int j =0; j < jComboBoxXaxComp.getItemCount(); j++) {
        if(namn.identC[ic].equalsIgnoreCase(jComboBoxXaxComp.getItemAt(j).toString())) {
          diag.compX = ic; break;
        }
      } // for j
    } // if compX >= 0
    if(diag.compX < 0) { // nothing fits, get the 1st one in the list
      for(int ic =0; ic < cs.Na; ic++) {
        if(namn.identC[ic].equalsIgnoreCase(jComboBoxXaxComp.getItemAt(0).toString())) {
          diag.compX = ic;
          dgrC.cLow[ic] = -6;
          dgrC.cHigh[ic] = 0.5;
          if(dgrC.hur[ic] == 1 || dgrC.hur[ic] == 2 || dgrC.hur[ic] == 3) { // "T"
                dgrC.hur[ic] = 3; // "LTV" = "log (Total conc.) varied"
          } else {  //not "T"
            dgrC.hur[ic] = 5; // "LAV" = "log (activity) varied"
          }
          break;
        }
      } // for ic
    } // if compX < 0

    if(diag.compY >= 0) {
      int ic = diag.compY;
      diag.compY = -1;
      for(int j =0; j < jComboBoxXaxComp.getItemCount(); j++) {
        if(namn.identC[ic].equalsIgnoreCase(jComboBoxXaxComp.getItemAt(j).toString())) {
          diag.compY = ic; break;
        }
      } // for j
      if(diag.compY < 0) { // nothing fits, get the 1st one in the list
        for(ic =0; ic < cs.Na; ic++) {
          if(namn.identC[ic].equalsIgnoreCase(jComboBoxXaxComp.getItemAt(0).toString())) {
            diag.compY = ic;
            dgrC.cLow[ic] = -6;
            dgrC.cHigh[ic] = 0.5;
            if(dgrC.hur[ic] == 1 || dgrC.hur[ic] == 2 ||
               dgrC.hur[ic] == 3) { // "T"
                  dgrC.hur[ic] = 3; // "LTV" = "log (Total conc.) varied"
            } else {  //not "T"
               dgrC.hur[ic] = 5; // "LAV" = "log (activity) varied"
            }
            break;
          }
        } // for ic
      } // if compY < 0
    } // if compY >= 0

    if(diag.compMain >= 0) {
      int ic = diag.compMain;
      diag.compMain = -1;
      for(int j =0; j < jComboBoxMainComp.getItemCount(); j++) {
        if(namn.identC[ic].equalsIgnoreCase(jComboBoxMainComp.getItemAt(j).toString())) {
          diag.compMain = ic; break;
        }
      } // for j
      if(diag.compMain < 0) { // something failed, get the 1st one in the list
        for(ic =0; ic < cs.Na; ic++) {
          if(namn.identC[ic].equalsIgnoreCase(jComboBoxMainComp.getItemAt(0).toString())) {
            diag.compMain = ic; break;
          }
        } // for ic
      } //if compMain <0
    } // if compMain >= 0

    //--- Set the X-axis component in the frame
    for(int j = 0; j < jComboBoxXaxComp.getItemCount(); j++) {
      if(namn.identC[diag.compX].equalsIgnoreCase(jComboBoxXaxComp.getItemAt(j).toString())) {
        jComboBoxXaxComp.setSelectedIndex(j);
        break;
      }
    } // for j

    if((diag.plotType == 0 || diag.plotType == 1 || diag.plotType == 4) && diag.compY >=0) {
        // Set the Y-axis component in the frame
        for(int j = 0; j < jComboBoxYaxComp.getItemCount(); j++) {
            if(namn.identC[diag.compY].equalsIgnoreCase(jComboBoxYaxComp.getItemAt(j).toString())) {
                jComboBoxYaxComp.setSelectedIndex(j);
                break;
            }
        } // for j
    } //PREDOM

    // set diagram type in the frame
    String[] diagramType = {"Predominance Area","Fraction","log Solubilities","Logarithmic","Relative activities","Calculated pe","Calculated pH","log Activities","H+ affinity spectrum"};
    for(int i = 0; i < jComboBoxDiagType.getItemCount(); i++) {
        if(diagramType[diag.plotType].equalsIgnoreCase(jComboBoxDiagType.getItemAt(i).toString()) ||
           (diagramType[diag.plotType].equalsIgnoreCase("Calculated pe") &&
            jComboBoxDiagType.getItemAt(i).toString().equalsIgnoreCase("Calculated Eh"))) {
                    jComboBoxDiagType.setSelectedIndex(i); // = diagramType_Click();
                    break;
        }
    } // for i

    // set the "main" component for PREDOM
    if(diag.compMain >= 0) {
      for(int i =0; i < jComboBoxMainComp.getItemCount(); i++) {
        if(namn.identC[diag.compMain].equals(jComboBoxMainComp.getItemAt(i))) {
          jComboBoxMainComp.setSelectedIndex(i);
          break;
        }
      } // for i
    }

    if(pc.dbg) {System.out.println("setUpFrame() -- ends");}

  } // setUpFrame()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="temperature & ionic strength">
 /** Reads the ionic strength, writes the value (to make sure) in the text
  * field, and enables/disables the activity coefficient and temperature fields  */
  private void validateIonicStrength() {
      diag.ionicStrength = readTextField(jTextFieldIonicStr);
      diag.ionicStrength = Math.min(1000,Math.max(diag.ionicStrength, -1));
      ionicStrOld = diag.ionicStrength;
      jTextFieldIonicStr.setText(Util.formatNum(diag.ionicStrength));
      if(diag.ionicStrength == 0) {
        jComboBoxActCoeff.setVisible(false);
        jLabelModel.setVisible(false);
      } else {
        if(pd.advancedVersion) {
          jLabelModel.setVisible(true);
          jComboBoxActCoeff.setVisible(true);
        }
      }
      enableTemperature();
    } // validateIonicStrength()
 /** Reads the temperature and stores it in <code>diag.temperature</code>.
  * The temperature is written (to make sure) in the text field. 
  * @see lib.kemi.chem.Chem.Diagr#temperature diag.temperature */
  private void validateTemperature() {
    if(jTextFieldT.getText().length() <=0) {return;}
    diag.temperature = readTextField(jTextFieldT);
    diag.temperature = Math.min(1000,Math.max(diag.temperature, -50));
    jTextFieldT.setText(Util.formatNum(diag.temperature));
  } // validateTemperature()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="updateAxes()">
/** Sets the Axes: Xmin/max, Ymin/max and conc-types, from
 * the values given in the "Concentration List Box" */
  private void updateAxes() {
    if(loading) {return;}
    if(pc.dbg) {System.out.println("updateAxes()");}
    double fEh;

    // -----------------------------------
    // 1st set permissible conc-types for X/Y axes
    int x0 = jComboBoxXaxType.getSelectedIndex();
    int ix4 = 4;
    if(diag.compX == hPresent) {ix4 = 6;}
    else if(diag.compX == ePresent) {
        ix4 = 8;
        if(runUseEh) {ix4 = 10;}
    } else if(Util.isGas(namn.identC[diag.compX])) {ix4 = 12;} // "log P varied"
    int iy4 = 4;
    if(diag.compY >= 0) {
      if(diag.compY == hPresent) {iy4 = 6;} else
      if(diag.compY == ePresent) {
        iy4 = 8;
        if(runUseEh) {iy4 = 10;}
      } else
      if(Util.isGas(namn.identC[diag.compY])) {iy4 = 12;} // "log P varied"
    } // if compY >=0

    updatingAxes = true;
    jComboBoxXaxType.removeAllItems();

    if(runPredomSED == 1) { // Predom
      int y0 = jComboBoxYaxType.getSelectedIndex();
      jComboBoxXaxType.addItem(concTypes[ix4]); //log (activity) varied (or pH,pe,Eh)
      jComboBoxXaxType.addItem(concTypes[2]); //log (Total conc.) varied
      jComboBoxYaxType.removeAllItems();
      jComboBoxYaxType.addItem(concTypes[iy4]); //log (activity) varied (or pH,pe,Eh)
      jComboBoxYaxType.addItem(concTypes[2]); //log (Total conc.) varied
      if(y0 >= 0 && y0 < jComboBoxYaxType.getItemCount()) {
          jComboBoxYaxType.setSelectedIndex(y0);
      } else {
          jComboBoxYaxType.setSelectedIndex(-1);
      }
    } else { // SED
      jComboBoxXaxType.addItem(concTypes[ix4]); //log (activity) varied (or pH,pe,Eh)
      jComboBoxXaxType.addItem(concTypes[2]); //log (Total conc.) varied
      jComboBoxXaxType.addItem(concTypes[1]); //Total conc. varied
    } // if run Predom

    if(x0 >= 0 && x0 < jComboBoxXaxType.getItemCount()) {
        jComboBoxXaxType.setSelectedIndex(x0);
    } else {
        jComboBoxXaxType.setSelectedIndex(-1);
    }
    // -----------------------------------
    // check the conc-type
    //   if the user changes a component in an axis,
    //   the conc-type might be wrong (not varied)
    // or Eh should be used instead of pe

    // ---------
    //  X-axis:
    // ---------

    // set the concentration to "varied"
    int use_pHpe = 0; int jc;
    if(dgrC.hur[diag.compX] == 4 || dgrC.hur[diag.compX] == 5) { //"LA" or "LAV"
      //  set right type (pH/pe/Eh/P)
      if(diag.compX == hPresent) {use_pHpe = 1;}
      else if(diag.compX == ePresent) {use_pHpe = 2; if(runUseEh) {use_pHpe = 3;}}
      else if(Util.isGas(namn.identC[diag.compX])) {use_pHpe = 4;}
      jc = 4 + use_pHpe*2;
      componentConcType[diag.compX] = jc;
    } else if(dgrC.hur[diag.compX] == 1
            || dgrC.hur[diag.compX] == 2 || dgrC.hur[diag.compX] == 3) { //"T", "TV" or "LTV"
        if(runPredomSED != 1 && // not Predom and
                (dgrC.hur[diag.compX] != 3 && dgrC.hur[diag.compX] != 4 &&
                    dgrC.hur[diag.compX] != 5)) { // no "L"
            componentConcType[diag.compX] = 1; // Total conc. varied
        } else {
            componentConcType[diag.compX] = 2; // log Total conc. varied
            if(dgrC.hur[diag.compX] != 3) { // not "LTV"
                dgrC.cLow[diag.compX] = Math.max(-50, Math.log10(Math.max(1e-51, Math.abs(dgrC.cLow[diag.compX]))));
                dgrC.cHigh[diag.compX] = Math.max(-45, Math.log10(Math.max(1e-46, Math.abs(dgrC.cHigh[diag.compX]))));
            }
        }
    } // if "LA" or "T.."

    boolean mustChange = true;
    for(int j = 0; j < jComboBoxXaxType.getItemCount(); j++) {
      if(concTypes[componentConcType[diag.compX]].equals(jComboBoxXaxType.getItemAt(j))) {
        jComboBoxXaxType.setSelectedIndex(j);
        mustChange = false;
        break;
      }
    } // for j

    if(pc.dbg) {System.out.println("     X-mustChange="+mustChange);}
    if(mustChange) {
      // set default values
        jComboBoxXaxType.setSelectedIndex(1); // default is: log Tot-conc varied
        if(diag.compX == ePresent) {
          if(dgrC.hur[diag.compX] != 4 && dgrC.hur[diag.compX] != 5) { // not "A"
            dgrC.cLow[diag.compX] = -17;
            dgrC.cHigh[diag.compX] = 17;
            jComboBoxXaxType.setSelectedIndex(0);
          }
        }
        else
        if(diag.compX == hPresent) {
          if(dgrC.hur[diag.compX] != 4 && dgrC.hur[diag.compX] != 5) { // not "A"
            dgrC.cLow[diag.compX] = -12;
            dgrC.cHigh[diag.compX] = -1;
            jComboBoxXaxType.setSelectedIndex(0);
          }
        }
    } // if mustChange

    for(int j=0; j < concTypes.length; j++) {
      if(concTypes[j].equalsIgnoreCase(jComboBoxXaxType.getSelectedItem().toString())) {
        int j2 = j;
        if(j2 > 4) {j2 = 4;}
        dgrC.hur[diag.compX] = j2+1;
        componentConcType[diag.compX] = j;
        break;
      } // if equal
    } // for j

    if(Double.isNaN(dgrC.cHigh[diag.compX])) {
        if(dgrC.hur[diag.compX] == 3 || dgrC.hur[diag.compX] == 5) { // log scale
            if(dgrC.cLow[diag.compX] <-1) {
                dgrC.cHigh[diag.compX] = dgrC.cLow[diag.compX]+1;
            } else {
                dgrC.cHigh[diag.compX] = dgrC.cLow[diag.compX]-1;
            }
        } else { // tot-conc.
            if(dgrC.cLow[diag.compX] ==0) {
                dgrC.cHigh[diag.compX] = 0.1;
            } else {
                dgrC.cHigh[diag.compX] = dgrC.cLow[diag.compX];
                dgrC.cLow[diag.compX] = 0;
            }
        }
    }
    TwoDoubles td = new TwoDoubles(dgrC.cLow[diag.compX], dgrC.cHigh[diag.compX]);
    checkForEqual(td);
    dgrC.cLow[diag.compX] = td.x1; dgrC.cHigh[diag.compX] = td.x2;
    double w, w1, w2;
    fEh = (MainFrame.Rgas * ln10 * (diag.temperature +273.15)) / MainFrame.Faraday;
    w1 = dgrC.cLow[diag.compX]; w2 = dgrC.cHigh[diag.compX];
    if(use_pHpe == 1 || use_pHpe == 2) {w1 = -w1; w2 = -w2;}
    else if(use_pHpe == 3) {
        w1 = -w1 * fEh;
        w2 = -w2 * fEh;
    }
    if(!runRevs) {if(w2 < w1) {w = w1; w1 = w2; w2 = w;}}
    jTextFieldXmin.setText(Util.formatNum(w1));
    jTextFieldXmax.setText(Util.formatNum(w2));

    if(runPredomSED != 1) {// not Predom
        if(jComboBoxXaxType.getSelectedIndex() > -1) {
            xAxisType0 = jComboBoxXaxType.getSelectedItem().toString();
        }
        if(jComboBoxYaxType.getSelectedIndex() > -1) {
            yAxisType0 = jComboBoxYaxType.getSelectedItem().toString();
        }
        updatingAxes = false;
        if(pc.dbg) {System.out.println("updateAxes() - ends");}
        return;
    }

    //---- for Predom diagrams:
    // ---------
    //  Y-axis:
    // ---------

    // set the concentration to "varied"
    use_pHpe = 0;
    if(dgrC.hur[diag.compY] == 4 || dgrC.hur[diag.compY] == 5) { //"LA" or "LAV"
      //  set right type (pH/pe/Eh/P)
      if(diag.compY == hPresent) {use_pHpe = 1;}
      else if(diag.compY == ePresent) {use_pHpe = 2; if(runUseEh) {use_pHpe = 3;}}
      else if(Util.isGas(namn.identC[diag.compY])) {use_pHpe = 4;}
      jc = 4 + use_pHpe*2;
      componentConcType[diag.compY] = jc;
    } // "LA."
    else if(dgrC.hur[diag.compY] == 1 || dgrC.hur[diag.compY] == 2
            || dgrC.hur[diag.compY] == 3) { //"T", "TV" or "LTV"
        componentConcType[diag.compY] = 2; // log Total conc. varied
        if(dgrC.hur[diag.compY] != 3) { // not "LTV"
            dgrC.cLow[diag.compY] = Math.max(-50, Math.log10(Math.max(1e-51, Math.abs(dgrC.cLow[diag.compY]))));
            dgrC.cHigh[diag.compY] = Math.max(-45, Math.log10(Math.max(1e-46, Math.abs(dgrC.cHigh[diag.compY]))));
        }
    }

    mustChange = true;
    for(int j = 0; j < jComboBoxYaxType.getItemCount(); j++) {
        if(concTypes[componentConcType[diag.compY]].equals(jComboBoxYaxType.getItemAt(j))) {
          jComboBoxYaxType.setSelectedIndex(j);
          mustChange = false;
          break;
        }
      } // for j
    if(pc.dbg) {System.out.println("     Y-mustChange="+mustChange);}
    if(mustChange) { // set default values
        jComboBoxYaxType.setSelectedIndex(1); // default is: log Tot-conc varied
        if(diag.compY == ePresent) {
          if(dgrC.hur[diag.compY] != 4 && dgrC.hur[diag.compY] != 5) { // not "A"
            dgrC.cLow[diag.compY] = -17;
            dgrC.cHigh[diag.compY] = 17;
            jComboBoxYaxType.setSelectedIndex(0);
          }
        }
        else if(diag.compY == hPresent) {
          if(dgrC.hur[diag.compY] != 4 && dgrC.hur[diag.compY] != 5) { // not "A"
            dgrC.cLow[diag.compY] = -12;
            dgrC.cHigh[diag.compY] = -1;
            jComboBoxYaxType.setSelectedIndex(0);
          }
        }
    } // if mustChange

    for(int j=0; j < concTypes.length; j++) {
      if(concTypes[j].equalsIgnoreCase(jComboBoxYaxType.getSelectedItem().toString())) {
        int j2 = j;
        if(j2 > 4) {j2 = 4;}
        dgrC.hur[diag.compY] = j2+1;
        componentConcType[diag.compY] = j;
        break;
      } // if equal
    } // for j

    if(Double.isNaN(dgrC.cHigh[diag.compY])) {
        if(dgrC.cLow[diag.compY] <-1) {
            dgrC.cHigh[diag.compY] = dgrC.cLow[diag.compY]+1;
        } else {
            dgrC.cHigh[diag.compY] = dgrC.cLow[diag.compY]-1;
        }
    }
    td = new TwoDoubles(dgrC.cLow[diag.compY], dgrC.cHigh[diag.compY]);
    checkForEqual(td);
    dgrC.cLow[diag.compY] = td.x1;
    dgrC.cHigh[diag.compY] = td.x2;
    w1 = dgrC.cLow[diag.compY]; w2 = dgrC.cHigh[diag.compY];
    if(use_pHpe == 1 || use_pHpe == 2) {w1 = -w1; w2 = -w2;}
    else if(use_pHpe == 3) {
        fEh = (MainFrame.Rgas * ln10 * (diag.temperature +273.15)) / MainFrame.Faraday;
        w1 = -w1 * fEh;
        w2 = -w2 * fEh;
    }
    if(!runRevs) {if(w2 < w1) {w = w1; w1 = w2; w2 = w;}}
    jTextFieldYmin.setText(Util.formatDbl6(w1));
    jTextFieldYmax.setText(Util.formatDbl6(w2));

    // -----------------------------------
    updateConcList();
    if(jComboBoxXaxType.getSelectedIndex() > -1) {
        xAxisType0 = jComboBoxXaxType.getSelectedItem().toString();
    }
    if(jComboBoxYaxType.getSelectedIndex() > -1) {
        yAxisType0 = jComboBoxYaxType.getSelectedItem().toString();
    }
    updatingAxes = false;
    if(pc.dbg) {System.out.println("updateAxes() - ends");}
} // updateAxes()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="updateConcList()">
/** Displays the concentration for each component in the list:
 * jScrollPaneCompConcList / jListCompConc */
private void updateConcList() {
  if(pc.dbg) {System.out.println("updateConcList()");}
  listCompConcModel.clear();
  String compConcText;

  for(int i = 0; i < cs.Na; i++) {
    //if(Util.isWater(namn.identC[i])) {continue;}
    compConcText = setConcText(i);
    listCompConcModel.addElement(compConcText);
  } // for i = 0 to ca.Na-1

  jListCompConc.clearSelection();

} // updateConcList()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="updateConcs()">
/** For the components in the axes: change the conc-type (<code>dgrC.hur<code>) and Min/Max values
 *  (<code>dgrC.cLow</code> and <code>dgrC.cHigh</code>) to those seen in the form.
 *  If PREDOM diagram: check that only components in the axes have concentrations varied. */
 private void updateConcs(){
  if(loading) {return;}
  if(pc.dbg) {System.out.println("updateConcs()");}
  int j2;

  // --------
  //  X-axis
  // --------
  if(jComboBoxXaxType.getSelectedIndex() >= 0 &&
     jComboBoxXaxType.getSelectedItem() != null) {
      String t = jComboBoxXaxType.getSelectedItem().toString();
      for(int j = 0; j < concTypes.length; j++) {
        if(t.equals(concTypes[j])) {
          j2=j+1;
          if(j2>5) {j2=5;}
          dgrC.hur[diag.compX] = j2;
          break;
        }
      } // for j
  }
  // make sure we got something useful
  int use_pHpe = 0;
  if(dgrC.hur[diag.compX] ==4 || dgrC.hur[diag.compX] ==5) { // LA or LAV
      if(!Util.isGas(namn.identC[diag.compX])) {
          if(diag.compX == hPresent) {use_pHpe =1;}
          else if(diag.compX == ePresent) {use_pHpe =2; if(runUseEh) {use_pHpe =3;}}
      }
  } // if LA or LAV
  double fEh, x1, x2;
  fEh = (MainFrame.Rgas * ln10 * (diag.temperature +273.15))/MainFrame.Faraday;
  x1 = readTextField(jTextFieldXmin);
  if(use_pHpe ==1 || use_pHpe == 2) {x1 = -x1;}
  else if(use_pHpe == 3) {
      x1 = -x1 / fEh;
  }
  x2 = readTextField(jTextFieldXmax);
  if(use_pHpe ==1 || use_pHpe == 2) {x2 = -x2;}
  else if(use_pHpe == 3) {
      x2 = -x2 / fEh;
  }
  dgrC.cLow[diag.compX] = x1 + Math.signum(x1) * Math.abs(x1) * 1e-14;
  dgrC.cHigh[diag.compX] = x2 + Math.signum(x2) * Math.abs(x2) * 1e-14;

  // --------
  //  Y-axis
  // --------
  if(runPredomSED != 1) { //not Predom (i.e., for SED)
      updateConcList();
      diag.yHigh = readTextField(jTextFieldYmax);
      diag.yLow = readTextField(jTextFieldYmin);
      if(pc.dbg) {System.out.println("updateConcs() -- end");}
      return;
  } // for SED
  else {
      if(jComboBoxYaxType.getSelectedIndex() >= 0
        && jComboBoxYaxType.getSelectedItem() != null) {
          String t = jComboBoxYaxType.getSelectedItem().toString();
          for(int j = 0; j < concTypes.length; j++) {
            if(t.equals(concTypes[j])) {
                j2=j+1; if(j2>5) {j2=5;}
                dgrC.hur[diag.compY] = j2;
                break;
            }
          } // for j
      }

      // make sure we got something useful
      use_pHpe = 0;
      if(dgrC.hur[diag.compY] == 4 || dgrC.hur[diag.compY] == 5) { // LA or LAV
        if(!Util.isGas(namn.identC[diag.compY])) {
          if(diag.compY == hPresent) {use_pHpe =1;}
          else if(diag.compY == ePresent) {use_pHpe =2; if(runUseEh) {use_pHpe =3;}}
        }
      } // if LA or LAV

      fEh = (MainFrame.Rgas * ln10 * (diag.temperature +273.15))/MainFrame.Faraday;
      x1 = readTextField(jTextFieldYmin);
      if(use_pHpe ==1 || use_pHpe == 2) {x1 = -x1;}
      else if(use_pHpe == 3) {x1 = -x1 / fEh;}
      x2 = readTextField(jTextFieldYmax);
      if(use_pHpe ==1 || use_pHpe == 2) {x2 = -x2;}
      else if(use_pHpe == 3) {x2 = -x2 / fEh;}
      dgrC.cLow[diag.compY] = x1 + Math.signum(x1) * Math.abs(x1) * 1e-14;
      dgrC.cHigh[diag.compY] = x2 + Math.signum(x2) * Math.abs(x2) * 1e-14;

      checkInputConcs();

      updateConcList();

  } // for Predom

  if(pc.dbg) {System.out.println("updateConcs() -- end");}

  } // updateConcs()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="updateDrawPHlineBox()">
  /** sets Eh check box visible and enabled/disabled */
  private void updateDrawPHlineBox() {
    if(loading) {return;}
    if(pc.dbg) {System.out.println("updateDrawPHlineBox()");}
    jCheckBoxDrawPHline.setVisible(false);
    if(ePresent < 0 || ePresent >= cs.Na || hPresent < 0 || hPresent >= cs.Na) {return;}
    if(ePresent < cs.Na && dgrC.hur[ePresent] == 5
        && hPresent < cs.Na && dgrC.hur[hPresent] == 5
        && (jComboBoxDiagType.getSelectedIndex() >=0 &&
                (jComboBoxDiagType.getSelectedItem().toString().equals("Predominance Area"))))
    {
        jCheckBoxDrawPHline.setVisible(true);
        jCheckBoxDrawPHline.setSelected(runPHline);
    }
  } // updateDrawPHlineBox()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="updateUseEhCheckBox()">
  /** sets Eh check box visible and enabled/disabled */
  private void updateUseEhCheckBox() {
    if(loading) {return;}
    if(pc.dbg) {System.out.println("updateUseEhCheckBox()");}
    if(ePresent < 0) {
      jCheckBoxUseEh.setVisible(false);
      return;
    } else {
      jCheckBoxUseEh.setVisible(true);
    }
    boolean enableEhCheckBox = false;
    if(!Double.isNaN(diag.temperature) &&
        ((jComboBoxDiagType.getSelectedIndex() >=0 &&
                (jComboBoxDiagType.getSelectedItem().toString().equalsIgnoreCase("calculated pe")
                 || jComboBoxDiagType.getSelectedItem().toString().equalsIgnoreCase("calculated Eh")))
            || (ePresent < cs.Na && dgrC.hur[ePresent] == 4)
             || (ePresent < cs.Na && dgrC.hur[ePresent] == 5)))
    {
        enableEhCheckBox = true;
    }
    jCheckBoxUseEh.setEnabled(enableEhCheckBox);
    if(enableEhCheckBox) {
        jCheckBoxUseEh.setText("<html>use <u>E</u>h for e-</html>");
    } else {
        jCheckBoxUseEh.setText("use Eh for e-");
    }
    if(Double.isNaN(diag.temperature)) {
        jCheckBoxUseEh.setSelected(false);
        jCheckBoxUseEh.setEnabled(jTextFieldT.isEnabled());
        } else {
        jCheckBoxUseEh.setSelected(runUseEh);
    }
  } // updateUseEhCheckBox()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="enableTemperature()">
  /** enable temperature?
   * If the temperature is given in the input file, then it is not enabled.
   * If the normal/advanced preference is "normal", then it is not enabled.
   */
  private void enableTemperature() {
    if(pc.dbg) {System.out.println("enableTemperature()");}
    boolean enableTemp = true;
    if(temperatureGivenInInputFile || !pd.advancedVersion) {
        enableTemp = false;
    } else {
      if(diag.ionicStrength == 0) {enableTemp = false;}
      if(runUseEh && ePresent >= 0
              && ((jComboBoxDiagType.getSelectedIndex() >=0
                    && (jComboBoxDiagType.getSelectedItem().toString().equalsIgnoreCase("calculated pe")
                        || jComboBoxDiagType.getSelectedItem().toString().equalsIgnoreCase("calculated Eh")))
                    || (ePresent < cs.Na && dgrC.hur[ePresent] ==4)
                    || (ePresent < cs.Na && dgrC.hur[ePresent] ==5))) {
                enableTemp = true;
                if(pc.dbg) {System.out.println("  use Eh = true, enableTemp = true.");}
      }
      if(ePresent >= 0 && ePresent < cs.Na) { // is it an pe/pH (Pourbaix) diagram?
        if(runPredomSED == 1) {
          if((dgrC.hur[ePresent] == 4 || dgrC.hur[ePresent] == 5)
                  && (hPresent >= 0 && hPresent < cs.Na)) { // pe varied and hPresent
            if(dgrC.hur[hPresent] == 4 || dgrC.hur[hPresent] == 5) { // pH varied
                boolean pHinAxis = false;
                boolean EhInAxis = false;
                if(jComboBoxXaxComp.getSelectedIndex() >=0 &&
                    jComboBoxXaxComp.getSelectedItem().toString().equalsIgnoreCase(namn.identC[hPresent])) {pHinAxis = true;}
                if(jComboBoxYaxComp.getSelectedIndex() >=0 &&
                    jComboBoxYaxComp.getSelectedItem().toString().equalsIgnoreCase(namn.identC[hPresent])) {pHinAxis = true;}
                if(jComboBoxXaxComp.getSelectedIndex() >=0 &&
                    jComboBoxXaxComp.getSelectedItem().toString().equalsIgnoreCase(namn.identC[ePresent])) {EhInAxis = true;}
                if(jComboBoxYaxComp.getSelectedIndex() >=0 &&
                    jComboBoxYaxComp.getSelectedItem().toString().equalsIgnoreCase(namn.identC[ePresent])) {EhInAxis = true;}
                if(pHinAxis && EhInAxis) { // pe/pH diagram
                    enableTemp = true;
                    if(pc.dbg) {System.out.println("  Pourbaix diagr., enableTemp = true.");}
                }
            } // pH varied
          } // pe varied and hPresent
        } // if Predom
      } // if ePresent
    } // if pd.advancedVersion
  jTextFieldT.setText(Util.formatNum(diag.temperature));
  jLabelT.setEnabled(enableTemp);
  jTextFieldT.setEnabled(enableTemp);
  jLabelTC.setEnabled(enableTemp);
  } // enableTemperature()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="updateYcomp(species)">
/** Set a list of Component/Species names on Y-axis combo box.
 * For relative activity diagrams: both components and reaction products must be
 * in the list, which is accomplished by setting species = true.
 * @param includeAllSpecies set to true if the combo box must list both component
 * names and all species */
  private void updateYcomp(boolean includeAllSpecies) {
    if(pc.dbg) {System.out.println("updateYcomp("+includeAllSpecies+")");}
    int jOld = jComboBoxYaxComp.getSelectedIndex();

    updatingAxes = true;
    jComboBoxYaxComp.removeAllItems();

    boolean axisCompAdd;
    for(int i = 0; i < cs.Na; i++) {
      axisCompAdd = true;
      if(Util.isWater(namn.identC[i])) {axisCompAdd = false;}
      // is this component solid or soluble ?
      if(i > (cs.Na - cs.solidC -1)) { // solid component
        // Solid component and no soluble complexes?
        // then do not add to Y-axis-list
        if(cs.nx <= 0) {axisCompAdd = false;}
      }
      if(axisCompAdd) {jComboBoxYaxComp.addItem(namn.identC[i]);}
    } // for i

    if(includeAllSpecies) {
      for(int i = 0; i < (cs.nx + cs.mSol); i++ ) {
        axisCompAdd = true;
        if(Util.isWater(namn.ident[i+cs.Na])) {axisCompAdd = false;}
        if(axisCompAdd) {jComboBoxYaxComp.addItem(namn.ident[i+cs.Na]);}
      } // for i
    } // if species

    if(jOld >= jComboBoxYaxComp.getItemCount()) {jOld = 0;}
    jComboBoxYaxComp.setSelectedIndex(jOld);
    updatingAxes = false;
  } // updateYcomp
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="comboBoxAxisType">
 /** Sets new values in the "textFieldMin" and "textFieldMax" of an axis,
  * from the type of concentration in the axis in the combo box.
  * The type is either "pH varied" or "log (Tot.conc.) varied" or something like that.
  * 
  * @param axisTypeNow
  * @param axisTypeBefore
  * @param textFieldMin a text field whose value will be changed
  * @param textFieldMax a text field whose value will be changed
  */
  private void comboBoxAxisType(String axisTypeNow, String axisTypeBefore,
          javax.swing.JTextField textFieldMin, javax.swing.JTextField textFieldMax) {
    if(axisTypeNow.equalsIgnoreCase(axisTypeBefore)) {return;}
    double valueMin = readTextField(textFieldMin);
    if(Double.isNaN(valueMin)) {valueMin = 0;}
    double valueMax = readTextField(textFieldMax);
    if(Double.isNaN(valueMax)) {valueMax = 0;}
    double valueMinNew, valueMaxNew;
    // --- set default values
    if(axisTypeNow.startsWith("Tot")) {valueMinNew = 1e-6; valueMaxNew = 1;}
    else if(axisTypeNow.startsWith("pH")) {valueMinNew = 1; valueMaxNew = 12;}
    else if(axisTypeNow.startsWith("pe")) {valueMinNew = -17; valueMaxNew = 17;}
    else if(axisTypeNow.startsWith("Eh")) {valueMinNew = -1; valueMaxNew = 1;}
    else {valueMinNew = -6; valueMaxNew = 0;} // should start with "log"
    // ---
    if(axisTypeNow.startsWith("log")) {
        if(axisTypeBefore.startsWith("log")) {
            if(valueMin >= -10 && valueMin <=2) {valueMinNew = valueMin;}
            if(valueMax >= -10 && valueMax <=2) {valueMaxNew = valueMax;}
        } else if(axisTypeBefore.startsWith("Tot")) {
            if(valueMin >0) {valueMinNew = Math.log10(valueMin);}
            if(valueMax >0) {valueMaxNew = Math.log10(valueMax);}
        } else if(axisTypeNow.startsWith("log (Tot") && axisTypeBefore.startsWith("pH")) {
            if(valueMax <= 14) {valueMinNew = -valueMax;}
            if(valueMin >= 0)  {valueMaxNew = -valueMin;}
        }
    } else if(axisTypeNow.startsWith("Tot")) {
        if(axisTypeBefore.startsWith("log")) {
            if(valueMin <= 1) {valueMinNew = logToNoLog(valueMin);}
            if(valueMax <= 2) {valueMaxNew = logToNoLog(valueMax);}
        } else if(axisTypeBefore.startsWith("pH")) {
            if(valueMax >= 0 && valueMax <= 14) {valueMinNew = logToNoLog(-valueMax);}
            if(valueMin >= 0 && valueMin <= 14) {valueMaxNew = logToNoLog(-valueMin);}
        }
    } else if(axisTypeNow.startsWith("pH")) {
        if(axisTypeBefore.startsWith("Tot")) {
            if(valueMax >0) {valueMinNew = -Math.log10(valueMax);}
            if(valueMin >0) {valueMaxNew = -Math.log10(valueMin);}
        } else
        if(axisTypeBefore.startsWith("log (Tot")) {
            if(valueMax <0) {valueMinNew = -valueMax;}
            if(valueMin <0) {valueMaxNew = -valueMin;}
        }
    }
    if(valueMaxNew < valueMinNew) {valueMin = valueMinNew; valueMinNew = valueMaxNew; valueMaxNew = valueMin;}
    // --- we got new values: change the text fields.
    textFieldMin.setText(Util.formatNum(valueMinNew));
    textFieldMax.setText(Util.formatNum(valueMaxNew));
    //return
  } // comboBoxAxisType(axisTypeNow, axisTypeBefore, valueMin, valueMax, textFieldMax, textFieldMin)
//</editor-fold>

//</editor-fold>

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupActCoef;
    private javax.swing.JButton jButtonGetConcCancel;
    private javax.swing.JButton jButtonGetConcOK;
    private javax.swing.JButton jButtonSaveDef;
    private javax.swing.JButton jButton_Cancel;
    private javax.swing.JButton jButton_Help;
    private javax.swing.JButton jButton_OK;
    private javax.swing.JCheckBox jCheckBoxAqu;
    private javax.swing.JCheckBox jCheckBoxDrawPHline;
    private javax.swing.JCheckBox jCheckBoxRev;
    private javax.swing.JCheckBox jCheckBoxTableOut;
    private javax.swing.JCheckBox jCheckBoxUseEh;
    private javax.swing.JComboBox<String> jComboBoxActCoeff;
    private javax.swing.JComboBox<String> jComboBoxConcType;
    private javax.swing.JComboBox<String> jComboBoxDiagType;
    private javax.swing.JComboBox<String> jComboBoxMainComp;
    private javax.swing.JComboBox<String> jComboBoxXaxComp;
    private javax.swing.JComboBox<String> jComboBoxXaxType;
    private javax.swing.JComboBox<String> jComboBoxYaxComp;
    private javax.swing.JComboBox<String> jComboBoxYaxType;
    private javax.swing.JLabel jLabelDFileName;
    private javax.swing.JLabel jLabelDataFile;
    private javax.swing.JLabel jLabelDiagrType;
    private javax.swing.JLabel jLabelEnterConc;
    private javax.swing.JLabel jLabelEqual;
    private javax.swing.JLabel jLabelFrom;
    private javax.swing.JLabel jLabelHaff;
    private javax.swing.JLabel jLabelIS;
    private javax.swing.JLabel jLabelIonicS;
    private javax.swing.JLabel jLabelM;
    private javax.swing.JLabel jLabelModel;
    private javax.swing.JLabel jLabelNbrCalcs;
    private javax.swing.JLabel jLabelOFile;
    private javax.swing.JLabel jLabelPredNbr;
    private javax.swing.JLabel jLabelPredNbrP;
    private javax.swing.JLabel jLabelSEDNbr;
    private javax.swing.JLabel jLabelSEDNbrP;
    private javax.swing.JLabel jLabelSpace;
    private javax.swing.JLabel jLabelSpaceP;
    private javax.swing.JLabel jLabelT;
    private javax.swing.JLabel jLabelTC;
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JLabel jLabelTo;
    private javax.swing.JLabel jLabelTotNbr;
    private javax.swing.JLabel jLabelXaxis;
    private javax.swing.JLabel jLabelYFract;
    private javax.swing.JLabel jLabelYRef;
    private javax.swing.JLabel jLabelYaxis;
    private javax.swing.JLabel jLabelYcalc;
    private javax.swing.JLabel jLabelYlogA;
    private javax.swing.JLabel jLabelYlogC;
    private javax.swing.JLabel jLabelYlogS;
    private javax.swing.JLabel jLabel_GetConcCompName;
    private javax.swing.JLabel jLabl1;
    private javax.swing.JLabel jLabl2;
    private javax.swing.JLabel jLabl3;
    private javax.swing.JLabel jLabl4;
    private javax.swing.JList jListCompConc;
    private javax.swing.JPanel jPanelActCoef;
    private javax.swing.JPanel jPanelAxes;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JPanel jPanelConcInner;
    private javax.swing.JPanel jPanelConcs;
    private javax.swing.JPanel jPanelDiagrType;
    private javax.swing.JPanel jPanelDiagram;
    private javax.swing.JPanel jPanelEmptyDiagram;
    private javax.swing.JPanel jPanelEmptyParameters;
    private javax.swing.JPanel jPanelEmptyYax;
    private javax.swing.JPanel jPanelFileNames;
    private javax.swing.JPanel jPanelGetConc;
    private javax.swing.JPanel jPanelLabl;
    private javax.swing.JPanel jPanelLow;
    private javax.swing.JPanel jPanelMainComp;
    private javax.swing.JPanel jPanelModel;
    private javax.swing.JPanel jPanelParams;
    private javax.swing.JPanel jPanelPredom;
    private javax.swing.JPanel jPanelSED;
    private javax.swing.JPanel jPanelSedPredom;
    private javax.swing.JPanel jPanelTitle;
    private javax.swing.JPanel jPanelTotCalcs;
    private javax.swing.JPanel jPanelXComponent;
    private javax.swing.JPanel jPanelXaxis;
    private javax.swing.JPanel jPanelYHaff;
    private javax.swing.JPanel jPanelYRef;
    private javax.swing.JPanel jPanelYaxComp;
    private javax.swing.JPanel jPanelYaxInner;
    private javax.swing.JPanel jPanelYaxis;
    private javax.swing.JPanel jPanelYcalc;
    private javax.swing.JPanel jPanelYcombo;
    private javax.swing.JPanel jPanelYfraction;
    private javax.swing.JPanel jPanelYlogA;
    private javax.swing.JPanel jPanelYlogC;
    private javax.swing.JPanel jPanelYlogS;
    private javax.swing.JPanel jPanelYmax;
    private javax.swing.JPanel jPanelYmin;
    private javax.swing.JRadioButton jRadioButtonCalc;
    private javax.swing.JRadioButton jRadioButtonFixed;
    private javax.swing.JScrollBar jScrollBarPredNbrP;
    private javax.swing.JScrollBar jScrollBarSEDNbrP;
    private javax.swing.JScrollPane jScrollPaneCompConcList;
    private javax.swing.JTextField jTextFieldCHigh;
    private javax.swing.JTextField jTextFieldCLow;
    private javax.swing.JTextField jTextFieldDataFile;
    private javax.swing.JTextField jTextFieldDiagName;
    private javax.swing.JTextField jTextFieldIonicStr;
    private javax.swing.JTextField jTextFieldT;
    private javax.swing.JTextField jTextFieldTitle;
    private javax.swing.JTextField jTextFieldXmax;
    private javax.swing.JTextField jTextFieldXmin;
    private javax.swing.JTextField jTextFieldYmax;
    private javax.swing.JTextField jTextFieldYmin;
    // End of variables declaration//GEN-END:variables

}
