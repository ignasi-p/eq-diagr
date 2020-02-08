package database;

import lib.common.MsgExceptn;
import lib.common.Util;
import lib.database.*;
import lib.huvud.ProgramConf;
import lib.huvud.RedirectedFrame;
import lib.huvud.SortedProperties;
import lib.huvud.Splash;

/** Main window frame of the Database program.
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
 * @author Ignasi Puigdomenech */
public class FrameDBmain extends javax.swing.JFrame {
  static final String VERS = "2020-Feb-03";
  /** all instances will use the same redirected frame */
  static RedirectedFrame msgFrame = null;

  /** Because the program checks for other instances and exits if there is
   * another instance, "dbf" is a reference to the only instance of
   * this class */
  private static FrameDBmain dbf = null;
  private final ProgramDataDB pd = new ProgramDataDB();
  private final ProgramConf pc;
  private HelpAboutDB helpAboutFrame = null;
  /** <code>true</code> if the operating system is "Windows",
   * <code>false</code> otherwise */
  static boolean windows = false;
  private static java.awt.Dimension msgFrameSize = new java.awt.Dimension(500,400);
  private static java.awt.Point locationMsgFrame = new java.awt.Point(80,28);
  private static final java.awt.Point locationFrame = new java.awt.Point(-1000,-1000);
  private java.awt.Dimension windowSize;

  /** true if the user has done "some work": a dialog will ask to confirm quit without saving */
  private boolean doneSomeWork = false;
  /** do we need to ask the user to confirm quitting? */
  private boolean queryToQuit;
  /** is the search going on? */
  private boolean searchingComplexes = false;

  static java.io.File fileIni;
  static java.io.File fileIniUser;
  private static final String FileINI_NAME = ".DataBase.ini";
  private boolean disclaimerSkip = false;
  private Disclaimer disclaimerFrame;
  /** This parameter is read from the ini-file: if <code>true</code> then the
   * advanced menu with "AddShowReferences" and "DatabaseMaintenace" will be
   * shown (for any operating system); in WIndows the menu will be
   * invisible if <code>false</code>; in other operating systems the menu
   * might become visible anyway.  */
  private boolean advancedMenu = false;
  /** If <code>laf</code> = 2 then the CrossPlatform look-and-feel is used,
   * else if <code>laf</code> = 1 the System look-and-feel is used.
   * Else (<code>laf</code> = 0) the System look-and-feel is
   * used on Windows and the CrossPlatform is used on Linux and Mac OS.
   * Default at program start = 0 **/
  private int laf = 0;

  /** used to set any selected solid components at the end of jListSelectedComps */
  int solidSelectedComps;

  private static final String DEF_DataBase = "Reactions.db";
  private boolean noDataBasesFound = false;

  private final javax.swing.JButton[] buttons = new javax.swing.JButton[LibDB.ELEMENTS];
  private final ButtonActionListener buttonAListener = new ButtonActionListener();
  private final ButtonMouseListener buttonMListener = new ButtonMouseListener();
  private final ButtonFocusListener buttonFListener = new ButtonFocusListener();
  private final ButtonKeyListener buttonKListener = new ButtonKeyListener();
  private int lastButtonInFocus = -1;
  private int lastReactionInFocus = -1;

  private javax.swing.border.Border buttonBorder;
  private final javax.swing.border.Border bevelBorder = javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED);
  private final javax.swing.border.Border lineBorder = javax.swing.BorderFactory.createLineBorder(java.awt.Color.black,1);
  private final javax.swing.border.Border buttonBorderSelected = javax.swing.BorderFactory.createCompoundBorder(lineBorder, bevelBorder);
  //private javax.swing.border.Border buttonBorderSelected = javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED,java.awt.Color.gray,java.awt.Color.white);
  //private javax.swing.border.Border buttonBorderSelected = javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED);
  //private javax.swing.border.Border buttonBorderSelected = javax.swing.BorderFactory.createLineBorder(java.awt.Color.black,2);
  //private javax.swing.border.Border buttonBorderSelected = javax.swing.BorderFactory.createLineBorder(java.awt.Color.black,1);
  /** background for elements with data in the database(s) */
  private final java.awt.Color buttonBackgroundB = new java.awt.Color(204, 204, 204);
  /** background for elements without data in the database(s) */
  private final java.awt.Color buttonBackgroundN = new java.awt.Color(231, 231, 231);

  private final java.awt.Font buttonFont = new java.awt.Font("Tahoma",java.awt.Font.BOLD,11);

  private final StringBuilder originalJLabelLeft = new StringBuilder(" ");
  /** the names of the components (if any) corresponding to the formulas
   * listed in jListAvailableComps, for example "cyanide" for component "CN-" */
  private final java.util.ArrayList<String> availableComponentsNames = new java.util.ArrayList<String>();
  /** the names of the components listed in jListComponents */
  private final java.util.ArrayList<String> componentsNames = new java.util.ArrayList<String>();

  /** java 1.6
  private final javax.swing.DefaultListModel modelAvailableComps = new javax.swing.DefaultListModel();
  javax.swing.DefaultListModel modelSelectedComps = new javax.swing.DefaultListModel();
  javax.swing.DefaultListModel modelComplexes = new javax.swing.DefaultListModel();
  */
  private final javax.swing.DefaultListModel<String> modelAvailableComps = new javax.swing.DefaultListModel<>();
  javax.swing.DefaultListModel<String> modelSelectedComps = new javax.swing.DefaultListModel<>();
  javax.swing.DefaultListModel<String> modelComplexes = new javax.swing.DefaultListModel<>();

  /** An object that performs searches reactions in the databases */
  private DBSearch srch;

  /** when ending the program (through window "ExitDialog"):
   * does the user want to send the data file to "Diagram"? */
  boolean send2Diagram;
  /** Changed by dialogs to indicate if the user closed the dialog without
   * clicking the "ok" button. For exameple, when ending the program (through
   * dialog "ExitDialog"): does the user want to cancel and continue instead? */
  boolean exitCancel;
  /** name of output data file; either selected by the user on exit
   * (through window "ExitDialog") or given as command-line argument */
  String outputDataFile = null;

  private final int panelLowerLabelsW0;
  private final int labelRightX0;

  /** indicates if a mouse click on the reaction list should show the popup menu */
  private boolean isPopup = false;

  /** if it is not null, it means addData is showing and "this" is not showing */
  private FrameAddData addData = null;
  /** if it is not null, it means singleComp is showing and "this" is not showing */
  private FrameSingleComponent singleComp = null;
  /** if it is not null, it means dbND is showing */
  private DBnamesDialog dbND = null;

  private static String[] commandArgs;

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
  private static final String SLASH = java.io.File.separator;
  static final String LINE = "- - - - - - - - - - - - - - - - - - - - - - - - - - -";

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /** Creates new form FrameDBmain
     * @param pc0
     * @param msgFrame0  */
  public FrameDBmain(ProgramConf pc0, RedirectedFrame msgFrame0) {
    initComponents();
    this.pc = pc0;
    pd.msgFrame = msgFrame0;
    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

    labelRightX0 = jLabelRight.getX();
    panelLowerLabelsW0 = jPanelLowerLabels.getWidth();

    solidSelectedComps = 0;
    //---- initial location: centered on screen
    locationFrame.x = Math.max(0, (LibDB.screenSize.width  - this.getWidth() ) / 2);
    locationFrame.y = Math.max(0, (LibDB.screenSize.height - this.getHeight() ) / 2);
    setLocation(locationFrame);

  } // constructor
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="setTitle()">
  private void setTitle() {
    String title = "Hydrochemical logK Database";
    String t;
    if(pd.temperature_C <=22.5 || pd.temperature_C >=27.5) {
        t = "Temperature = " + String.format("%.0f 'C",pd.temperature_C);
        //title = title + "   -   " + t;
        jLabelTemperature.setText(t);
    } else {jLabelTemperature.setText(" ");}
    if(pd.pressure_bar >1.1) {
        if(pd.pressure_bar < lib.kemi.H2O.IAPWSF95.CRITICAL_pBar) {
            t = "Pressure = " + String.format(java.util.Locale.ENGLISH,"%.2f bar",pd.pressure_bar);
        } else {
            t = "Pressure = " + String.format("%.0f bar",pd.pressure_bar);
        }
        jLabelPressure.setText(t);
    } else {jLabelPressure.setText(" ");}
    this.setTitle(title);    
  }

  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="prepareButtons()">
  private void prepareButtons() {
    if(pc.dbg) {System.out.println(pc.progName+" - prepareButtons()");}
    setCursorWait();
    buttonBorder = jButton1.getBorder(); // get the default button border

    //---- an array of JButton is made with the chemical element buttons in the frame;
    //     to each element button it is assigned an action name ("0", "1", "2", ...),
    //     corresponding to the button's index in the array.
    //     When the user clicks on an element, the button's action name
    //     is used as the array index
    makeButtonsArray();
    // add the mouse and action listeners
    for(int i=0; i < buttons.length; i++) {
        buttons[i].setActionCommand(String.valueOf(i));
        buttons[i].addMouseListener(buttonMListener);
        buttons[i].addActionListener(buttonAListener);
        buttons[i].addFocusListener(buttonFListener);
        buttons[i].addKeyListener(buttonKListener);
        buttons[i].setMargin(new java.awt.Insets(2, 0, 2, 0));
        buttons[i].setPreferredSize(new java.awt.Dimension(27, 27));
        buttons[i].setBackground(buttonBackgroundB);
    }
    setCursorDef();
  } // prepareButtons()

  //<editor-fold defaultstate="collapsed" desc="makeButtonsArray()">
  private void makeButtonsArray(){
      buttons[ 0]= jButton0; buttons[ 1]= jButton1; buttons[ 2]= jButton2; buttons[ 3]= jButton3;
      buttons[ 4]= jButton4; buttons[ 5]= jButton5; buttons[ 6]= jButton6; buttons[ 7]= jButton7;
      buttons[ 8]= jButton8; buttons[ 9]= jButton9; buttons[10]=jButton10; buttons[11]=jButton11;
      buttons[12]=jButton12; buttons[13]=jButton13; buttons[14]=jButton14; buttons[15]=jButton15;
      buttons[16]=jButton16; buttons[17]=jButton17; buttons[18]=jButton18; buttons[19]=jButton19;
      buttons[20]=jButton20; buttons[21]=jButton21; buttons[22]=jButton22; buttons[23]=jButton23;
      buttons[24]=jButton24; buttons[25]=jButton25; buttons[26]=jButton26; buttons[27]=jButton27;
      buttons[28]=jButton28; buttons[29]=jButton29; buttons[30]=jButton30; buttons[31]=jButton31;
      buttons[32]=jButton32; buttons[33]=jButton33; buttons[34]=jButton34; buttons[35]=jButton35;
      buttons[36]=jButton36; buttons[37]=jButton37; buttons[38]=jButton38; buttons[39]=jButton39;
      buttons[40]=jButton40; buttons[41]=jButton41; buttons[42]=jButton42; buttons[43]=jButton43;
      buttons[44]=jButton44; buttons[45]=jButton45; buttons[46]=jButton46; buttons[47]=jButton47;
      buttons[48]=jButton48; buttons[49]=jButton49; buttons[50]=jButton50; buttons[51]=jButton51;
      buttons[52]=jButton52; buttons[53]=jButton53; buttons[54]=jButton54; buttons[55]=jButton55;
      buttons[56]=jButton56; buttons[57]=jButton57; buttons[58]=jButton58; buttons[59]=jButton59;
      buttons[60]=jButton60; buttons[61]=jButton61; buttons[62]=jButton62; buttons[63]=jButton63;
      buttons[64]=jButton64; buttons[65]=jButton65; buttons[66]=jButton66; buttons[67]=jButton67;
      buttons[68]=jButton68; buttons[69]=jButton69; buttons[70]=jButton70; buttons[71]=jButton71;
      buttons[72]=jButton72; buttons[73]=jButton73; buttons[74]=jButton74; buttons[75]=jButton75;
      buttons[76]=jButton76; buttons[77]=jButton77; buttons[78]=jButton78; buttons[79]=jButton79;
      buttons[80]=jButton80; buttons[81]=jButton81; buttons[82]=jButton82; buttons[83]=jButton83;
      buttons[84]=jButton84; buttons[85]=jButton85; buttons[86]=jButton86; buttons[87]=jButton87;
      buttons[88]=jButton88; buttons[89]=jButton89; buttons[90]=jButton90; buttons[91]=jButton91;
      buttons[92]=jButton92; buttons[93]=jButton93; buttons[94]=jButton94; buttons[95]=jButton95;
      buttons[96]=jButton96; buttons[97]=jButton97; buttons[98]=jButton98; buttons[99]=jButton99;
      buttons[100]=jButton100; buttons[101]=jButton101; buttons[102]=jButton102; buttons[103]=jButton103;
  } //makeButtonsArray
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="buttonsArray Listeners">
  private class ButtonActionListener implements java.awt.event.ActionListener {
    @Override public void actionPerformed(java.awt.event.ActionEvent e) {
        if(e.getSource() instanceof javax.swing.JButton) {
            int i = Integer.parseInt(e.getActionCommand());
            lastButtonInFocus = i;
            getAvailableComponents(i);
            if(LibDB.elementName[i] != null) {
                jLabelLeft.setText("Element: "+LibDB.elementName[i]);
            }
            originalJLabelLeft.replace(0, originalJLabelLeft.length(), jLabelLeft.getText());
            buttons[i].requestFocusInWindow();
            if(i==0) {availableComponentClick(0);}
        }
    } //actionPerformed
  } //class ButtonActionListener

  private class ButtonFocusListener implements java.awt.event.FocusListener {
    @Override public void focusGained(java.awt.event.FocusEvent evt) {
        javax.swing.JButton b = (javax.swing.JButton)evt.getSource();
        int i = Integer.parseInt(b.getActionCommand());
        lastButtonInFocus = i;
        if(LibDB.elementName[i] != null) {
            jLabelLeft.setText("Element: "+LibDB.elementName[i]);
            originalJLabelLeft.replace(0, originalJLabelLeft.length(), jLabelLeft.getText());
        }
        buttons[i].setPreferredSize(new java.awt.Dimension(27, 27));
        buttons[i].setMargin(new java.awt.Insets(2, 0, 2, 0));
        buttons[i].setBorder(buttonBorderSelected);
        getAvailableComponents(i);
    }
    @Override public void focusLost(java.awt.event.FocusEvent evt) {
        javax.swing.JButton b = (javax.swing.JButton)evt.getSource();
        int i = Integer.parseInt(b.getActionCommand());
        buttons[i].setPreferredSize(new java.awt.Dimension(27, 27));
        buttons[i].setMargin(new java.awt.Insets(2, 0, 2, 0));
        buttons[i].setBackground(buttonBackgroundB);
        buttons[i].setBorder(buttonBorder);
        jLabelLeft.setText(originalJLabelLeft.toString());
    }
  } //class ButtonFocusListener

  private class ButtonKeyListener extends java.awt.event.KeyAdapter {
    @Override public void keyPressed(java.awt.event.KeyEvent evt) {
      javax.swing.JButton b = (javax.swing.JButton)evt.getSource();
      int i = Integer.parseInt(b.getActionCommand());
      int j;
      if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_UP) {
          if(i == 0) {
            for(j=(buttons.length-1); j > i; j--) {
              if(buttons[j].isEnabled()) {buttons[j].requestFocusInWindow(); return;}
            }
          }
          if(i == 1 || i == 2) {j=0;}
          else if(i == 3) {j=1;}
          else if(i ==10 || (i >=11 && i <=20)) {j=i-8;}
          else if(i >=31 && i <= 57) {j=i-18;}
          else if(i >=72 && i <= 103) {j=i-32;} else   {j=-1;}
          if(j > -1) {buttons[j].requestFocusInWindow();}
      } else if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
          if(i == 0) {j=1;}
          else if(i == 1) {j=3;}
          else if(i >= 2 && i <=12) {j=i+8;}
          else if(i >= 13 && i <=39) {j=i+18;}
          else if(i >= 40 && i <=71) {j=i+32;}
          else if(i >=72 && i <= 103) {j=0;} else   {j=-1;}
          if(j > -1) {buttons[j].requestFocusInWindow();}
      } else if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_RIGHT) {
          if(i < (buttons.length-1)) {
            for(j=i+1; j<buttons.length; j++) {
                if(buttons[j].isEnabled()) {buttons[j].requestFocusInWindow(); return;}
            }
          }
          buttons[0].requestFocusInWindow(); // buttons[0] is always enabled
      } else if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_LEFT) {
          if(i > 0) {
            for(j=i-1; j >= 0; j--) {
                if(buttons[j].isEnabled()) {buttons[j].requestFocusInWindow(); return;}
            }
          }
          for(j=(buttons.length-1); j > i; j--) {
              if(buttons[j].isEnabled()) {buttons[j].requestFocusInWindow(); return;}
          }
          buttons[i].requestFocusInWindow();
      }
    }
  } //class ButtonKeyListener

  private class ButtonMouseListener extends java.awt.event.MouseAdapter {
    @Override
    public void mouseEntered(java.awt.event.MouseEvent evt) {
        javax.swing.JButton b = (javax.swing.JButton)evt.getSource();
        int i = Integer.parseInt(b.getActionCommand());
        if(LibDB.elementName[i] != null) {
            jLabelLeft.setText("Element: "+LibDB.elementName[i]);
        }
    }
    @Override
    public void mouseExited(java.awt.event.MouseEvent evt) {
        jLabelLeft.setText(originalJLabelLeft.toString());
    }
  } //class ButtonMouseListener

  // </editor-fold>

  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="start()">
  /** Performs start-up actions that require an "object" of this class to be present */
  private void start() {
    if(pc.dbg) {System.out.println(pc.progName+" - start()");}
    //---- read the INI-file
    //     because this might result in message boxes, that nee a parent,
    //     this must be done after the constructor is finished.
    //     Note, however, that the parent (this) frame is not visible yet.
    readIni();
    //---- Position the window on the screen
    locationFrame.x = Math.min(LibDB.screenSize.width - this.getWidth() - 4,
                            Math.max(4, locationFrame.x));
    locationFrame.y = Math.min(LibDB.screenSize.height - this.getHeight()- 25,
                            Math.max(4, locationFrame.y));
    setLocation(locationFrame);
    //----

    jLabel_cr_solids.setText(" ");
    jLabelLeft.setText(" ");
    jLabelMid.setText(" ");
    jLabelRight.setText(" ");
    jMenuSearch.setEnabled(false);
    jMenuExit.setEnabled(false);
    //---- Icon
    String iconName = "images/DataBase.gif";
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

    //getContentPane().setBackground(java.awt.Color.white);
    jMenuBar.add(javax.swing.Box.createHorizontalGlue(),2); //move "Help" menu to the right

    //----- key actions -----
    //--- ESC key: with a menu bar, the behaviour of ESC is too complex
    //--- Alt-Q quit
    javax.swing.KeyStroke altQKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altQKeyStroke,"ALT_Q");
    javax.swing.Action altQAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            queryToQuit = true; end_program();
        }};
    getRootPane().getActionMap().put("ALT_Q", altQAction);
    //--- Alt-X eXit
    javax.swing.KeyStroke altXKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altXKeyStroke,"ALT_X");
    javax.swing.Action altXAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jMenuExit.doClick();
        }};
    getRootPane().getActionMap().put("ALT_X", altXAction);
    //--- Alt-S show debug frame
    javax.swing.KeyStroke altSKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altSKeyStroke,"ALT_S");
    javax.swing.Action altSAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jCheckBoxMenuDebug.doClick();
        }};
    getRootPane().getActionMap().put("ALT_S", altSAction);
    //--- Alt-A available components or "Accept"
    javax.swing.KeyStroke altAKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altAKeyStroke,"ALT_A");
    javax.swing.Action altAAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jListAvailableComps.requestFocusInWindow();
            int i = jListAvailableComps.getSelectedIndex();
            if(i>=0) {
                jLabelLeft.setText(availableComponentsNames.get(i));
            } else {
                jListAvailableComps.setSelectedIndex(0);
                jLabelLeft.setText(availableComponentsNames.get(0));
            }
        }};
    getRootPane().getActionMap().put("ALT_A", altAAction);
    //--- F1 for help
    javax.swing.KeyStroke f1KeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_F1,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(f1KeyStroke,"F1");
    javax.swing.Action f1Action = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jMenuHelp.doClick();
        }};
    getRootPane().getActionMap().put("F1", f1Action);
    //--- Alt-C components
    javax.swing.KeyStroke altCKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altCKeyStroke,"ALT_C");
    javax.swing.Action altCAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jListSelectedComps.requestFocusInWindow();
            int i = jListSelectedComps.getSelectedIndex();
            if(i>=0) {
                jLabelMid.setText(componentsNames.get(i));
            } else {
                jListSelectedComps.setSelectedIndex(0);
                jLabelMid.setText(componentsNames.get(0));
            }
        }};
    getRootPane().getActionMap().put("ALT_C", altCAction);
    //--- Alt-D complexes
    javax.swing.KeyStroke altDKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altDKeyStroke,"ALT_D");
    javax.swing.Action altDAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            if(jListComplexes.isShowing()) {jListComplexes.requestFocusInWindow();}
            int i = jListComplexes.getSelectedIndex();
            if(i<0) {
                jListComplexes.setSelectedIndex(0);
                lastReactionInFocus = 0;
            } else {
                lastReactionInFocus = i;
            }
        }};
    getRootPane().getActionMap().put("ALT_D", altDAction);
    //----- key actions end -----

    if (pc.dbg) {
        System.out.println(LINE);
        StringBuffer msg = new StringBuffer();
        msg.append("After reading cfg- and INI-files");msg.append(nl);
        msg.append("   and after checking for another instance:");msg.append(nl);
        msg.append("App_Path = ");
        if(pc.pathAPP == null) {
            msg.append("\"null\"");
        } else {
            if(pc.pathAPP.trim().length()<=0) {msg.append("\"\"");}
            else {msg.append(pc.pathAPP);}
        }
        msg.append(nl);
        msg.append("Def_path = ");msg.append(pc.pathDef.toString());msg.append(nl);
        try {
            msg.append("User.dir = ");msg.append(System.getProperty("user.dir"));msg.append(nl);
            msg.append("User.home = ");msg.append(System.getProperty("user.home"));msg.append(nl);
        }
        catch (Exception e) {}
        msg.append("CLASSPATH = ");msg.append(System.getProperty("java.class.path"));msg.append(nl);
        msg.append("Program to make diagrams = ");msg.append(pd.diagramProgr);msg.append(nl);
        int ndb = pd.dataBasesList.size();
        msg.append(ndb+" database"); if(ndb != 1) {msg.append("s");}
        if(ndb > 0) {
            msg.append(":"+nl);
            for(int i = 0; i < ndb; i++) {
                msg.append("    "+pd.dataBasesList.get(i));
                msg.append(nl);
            }
        }
        System.out.println(msg);
        System.out.println(LINE);
    } // if dbg

    // ----
    if(pd.msgFrame != null) {
        pd.msgFrame.setLocation(locationMsgFrame);
        pd.msgFrame.setSize(msgFrameSize);
        jCheckBoxMenuDebug.setSelected(pd.msgFrame.isVisible());
    } else {jCheckBoxMenuDebug.setVisible(false);}

    if(pc.pathAPP != null && pc.pathAPP.trim().length()>0) {
        pd.pathDatabaseFiles.replace(0,pd.pathDatabaseFiles.length(),pc.pathAPP);
    } else {pd.pathDatabaseFiles.replace(0,pd.pathDatabaseFiles.length(),".");}
    java.io.File f = new java.io.File(pc.pathAPP+SLASH+ProgramConf.HELP_JAR);
    if(!f.exists()) {jMenuHelp.setEnabled(false);}

    //---- set Look-And-Feel
    try{
        if(laf == 2) {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getCrossPlatformLookAndFeelClassName());
            System.out.println("--- setLookAndFeel(CrossPlatform);");
        } else if(laf == 1) {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            System.out.println("--- setLookAndFeel(System);");
        }
    }
    catch (ClassNotFoundException ex) {System.out.println("Error: "+ex.getMessage());}
    catch (IllegalAccessException ex) {System.out.println("Error: "+ex.getMessage());}
    catch (InstantiationException ex) {System.out.println("Error: "+ex.getMessage());}
    catch (javax.swing.UnsupportedLookAndFeelException ex) {System.out.println("Error: "+ex.getMessage());}

    javax.swing.SwingUtilities.updateComponentTreeUI(dbf);
    dbf.invalidate();dbf.validate();dbf.repaint();

    javax.swing.SwingUtilities.updateComponentTreeUI(msgFrame);
    msgFrame.invalidate();msgFrame.validate();msgFrame.repaint();

    // System.out.println("--- configureOptionPane();");
    Util.configureOptionPane();

    if(pd.temperatureAllowHigher) {
        jMenuItemTemperature.setText("set Temperature & pressure");
    } else {
        jMenuItemTemperature.setText("set Temperature");
    }

    prepareButtons();
    setTitle();
    pack();

    if(disclaimerFrame != null) {
        if(disclaimerSkip) {
            //--- remove the "disclaimer" window
            // this will call "disclaimerAccepted()" and "bringToFront"
            disclaimerFrame.closeWindow(true);            
            disclaimerFrame = null;
            // set this window visible, set the minimum size and the location
            bringToFront();
            disclaimerAccepted();
        } else {
            System.out.println("... waiting for disclaimer acceptance ...");
            disclaimerFrame.requestFocus();
            // ---- Start a thread to wait for the disclaimer
            new javax.swing.SwingWorker<Void,Void>() {
            @Override protected Void doInBackground() throws Exception {
                disclaimerFrame.waitForDisclaimer();
                return null;
            }
            @Override protected void done(){
                disclaimerFrame = null;
                // set this window visible, set the minimum size and the location
                bringToFront();
                System.out.println("    disclaimer accepted.");
                disclaimerAccepted();
            } // done()
            }.execute(); // this returns inmediately,
            //    but the SwingWorker continues running...
        }
    } else {  // if disclaimerFrame == null
        // there was no disclaimer,
        // set this window visible, set the minimum size and the location
        bringToFront();
        disclaimerAccepted();
    }

    if(pc.dbg) {System.out.println(pc.progName+" - start() exit.");}
  } //start
  // </editor-fold>

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu = new javax.swing.JPopupMenu();
        jMenuItemDel = new javax.swing.JMenuItem();
        jMenuItemData = new javax.swing.JMenuItem();
        jSeparator = new javax.swing.JPopupMenu.Separator();
        jMenuItemCancel = new javax.swing.JMenuItem();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanelTable = new javax.swing.JPanel();
        jPanelPeriodicT = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jButton19 = new javax.swing.JButton();
        jButton20 = new javax.swing.JButton();
        jButton21 = new javax.swing.JButton();
        jButton22 = new javax.swing.JButton();
        jButton23 = new javax.swing.JButton();
        jButton24 = new javax.swing.JButton();
        jButton25 = new javax.swing.JButton();
        jButton26 = new javax.swing.JButton();
        jButton27 = new javax.swing.JButton();
        jButton28 = new javax.swing.JButton();
        jButton29 = new javax.swing.JButton();
        jButton30 = new javax.swing.JButton();
        jButton31 = new javax.swing.JButton();
        jButton32 = new javax.swing.JButton();
        jButton33 = new javax.swing.JButton();
        jButton34 = new javax.swing.JButton();
        jButton35 = new javax.swing.JButton();
        jButton36 = new javax.swing.JButton();
        jButton37 = new javax.swing.JButton();
        jButton38 = new javax.swing.JButton();
        jButton39 = new javax.swing.JButton();
        jButton40 = new javax.swing.JButton();
        jButton41 = new javax.swing.JButton();
        jButton42 = new javax.swing.JButton();
        jButton43 = new javax.swing.JButton();
        jButton44 = new javax.swing.JButton();
        jButton45 = new javax.swing.JButton();
        jButton46 = new javax.swing.JButton();
        jButton47 = new javax.swing.JButton();
        jButton48 = new javax.swing.JButton();
        jButton49 = new javax.swing.JButton();
        jButton50 = new javax.swing.JButton();
        jButton51 = new javax.swing.JButton();
        jButton52 = new javax.swing.JButton();
        jButton53 = new javax.swing.JButton();
        jButton54 = new javax.swing.JButton();
        jButton55 = new javax.swing.JButton();
        jButton56 = new javax.swing.JButton();
        jButton57 = new javax.swing.JButton();
        jButton72 = new javax.swing.JButton();
        jButton73 = new javax.swing.JButton();
        jButton74 = new javax.swing.JButton();
        jButton75 = new javax.swing.JButton();
        jButton76 = new javax.swing.JButton();
        jButton77 = new javax.swing.JButton();
        jButton78 = new javax.swing.JButton();
        jButton79 = new javax.swing.JButton();
        jButton80 = new javax.swing.JButton();
        jButton81 = new javax.swing.JButton();
        jButton82 = new javax.swing.JButton();
        jButton83 = new javax.swing.JButton();
        jButton84 = new javax.swing.JButton();
        jButton85 = new javax.swing.JButton();
        jButton86 = new javax.swing.JButton();
        jButton87 = new javax.swing.JButton();
        jButton88 = new javax.swing.JButton();
        jButton89 = new javax.swing.JButton();
        jButton0 = new javax.swing.JButton();
        jPanelREEactinides = new javax.swing.JPanel();
        jButton58 = new javax.swing.JButton();
        jButton59 = new javax.swing.JButton();
        jButton60 = new javax.swing.JButton();
        jButton61 = new javax.swing.JButton();
        jButton62 = new javax.swing.JButton();
        jButton63 = new javax.swing.JButton();
        jButton64 = new javax.swing.JButton();
        jButton65 = new javax.swing.JButton();
        jButton66 = new javax.swing.JButton();
        jButton67 = new javax.swing.JButton();
        jButton68 = new javax.swing.JButton();
        jButton69 = new javax.swing.JButton();
        jButton70 = new javax.swing.JButton();
        jButton71 = new javax.swing.JButton();
        jButton90 = new javax.swing.JButton();
        jButton91 = new javax.swing.JButton();
        jButton92 = new javax.swing.JButton();
        jButton93 = new javax.swing.JButton();
        jButton94 = new javax.swing.JButton();
        jButton95 = new javax.swing.JButton();
        jButton96 = new javax.swing.JButton();
        jButton97 = new javax.swing.JButton();
        jButton98 = new javax.swing.JButton();
        jButton99 = new javax.swing.JButton();
        jButton100 = new javax.swing.JButton();
        jButton101 = new javax.swing.JButton();
        jButton102 = new javax.swing.JButton();
        jButton103 = new javax.swing.JButton();
        jLabelTemperature = new javax.swing.JLabel();
        jLabelPressure = new javax.swing.JLabel();
        jPanelBottom = new javax.swing.JPanel();
        jPanelLower = new javax.swing.JPanel();
        jLabelAvailableComp = new javax.swing.JLabel();
        jScrollPaneAvailableComps = new javax.swing.JScrollPane();
        jListAvailableComps = new javax.swing.JList();
        jLabelComps = new javax.swing.JLabel();
        jScrollPaneSelectedComps = new javax.swing.JScrollPane();
        jListSelectedComps = new javax.swing.JList();
        jLabelComplexes = new javax.swing.JLabel();
        jScrollPaneComplexes = new javax.swing.JScrollPane();
        jListComplexes = new javax.swing.JList();
        jLabel_cr_solids = new javax.swing.JLabel();
        jPanelLowerLabels = new javax.swing.JPanel();
        jLabelLeft = new javax.swing.JLabel();
        jLabelMid = new javax.swing.JLabel();
        jLabelRight = new javax.swing.JLabel();
        jPanelProgressBar = new javax.swing.JPanel();
        jLabelNow = new javax.swing.JLabel();
        jLabelNowLoop = new javax.swing.JLabel();
        jProgressBar = new javax.swing.JProgressBar();
        jMenuBar = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuSearch = new javax.swing.JMenuItem();
        jMenuExit = new javax.swing.JMenuItem();
        jMenuQuit = new javax.swing.JMenuItem();
        jMenuOptions = new javax.swing.JMenu();
        jMenuItemTemperature = new javax.swing.JMenuItem();
        jMenuPref = new javax.swing.JMenu();
        jMenuAdvH2O = new javax.swing.JMenuItem();
        jMenuAdvRedox = new javax.swing.JMenuItem();
        jMenuAdvSolids = new javax.swing.JMenuItem();
        jMenuLocate = new javax.swing.JMenuItem();
        jCheckBoxMenuVerbose = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuDebug = new javax.swing.JCheckBoxMenuItem();
        jMenuData = new javax.swing.JMenu();
        jMenuDBFiles = new javax.swing.JMenuItem();
        jMenuAddData = new javax.swing.JMenuItem();
        jMenuSingleC = new javax.swing.JMenuItem();
        jMenuAdvanced = new javax.swing.JMenu();
        jMenuRefs = new javax.swing.JMenuItem();
        jMenuMaintenance = new javax.swing.JMenuItem();
        jMenuH = new javax.swing.JMenu();
        jMenuHelp = new javax.swing.JMenuItem();
        jMenuHelpAbout = new javax.swing.JMenuItem();

        jMenuItemDel.setMnemonic('r');
        jMenuItemDel.setText("Remove");
        jMenuItemDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDelActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemDel);

        jMenuItemData.setMnemonic('d');
        jMenuItemData.setText("show Details");
        jMenuItemData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDataActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemData);
        jPopupMenu.add(jSeparator);

        jMenuItemCancel.setMnemonic('c');
        jMenuItemCancel.setText("cancel");
        jMenuItemCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCancelActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemCancel);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        jPanelTable.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanelPeriodicT.setBackground(new java.awt.Color(255, 255, 153));
        jPanelPeriodicT.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanelPeriodicT.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setText("H");
        jButton1.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton1.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 13, -1, -1));

        jButton2.setText("He");
        jButton2.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton2.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(471, 13, -1, -1));

        jButton3.setText("Li");
        jButton3.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton3.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 40, -1, -1));

        jButton4.setText("Be");
        jButton4.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton4.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(39, 40, -1, -1));

        jButton5.setText("B");
        jButton5.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton5.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(336, 40, -1, -1));

        jButton6.setText("C");
        jButton6.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton6.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(363, 40, -1, -1));

        jButton7.setText("N");
        jButton7.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton7.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 40, -1, -1));

        jButton8.setText("O");
        jButton8.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton8.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(417, 40, -1, -1));

        jButton9.setText("F");
        jButton9.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton9.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton9, new org.netbeans.lib.awtextra.AbsoluteConstraints(444, 40, -1, -1));

        jButton10.setText("Ne");
        jButton10.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton10.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton10, new org.netbeans.lib.awtextra.AbsoluteConstraints(471, 40, -1, -1));

        jButton11.setText("Na");
        jButton11.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton11.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton11, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 67, -1, -1));

        jButton12.setText("Mg");
        jButton12.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton12.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton12, new org.netbeans.lib.awtextra.AbsoluteConstraints(39, 67, -1, -1));

        jButton13.setText("Al");
        jButton13.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton13.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton13, new org.netbeans.lib.awtextra.AbsoluteConstraints(336, 67, -1, -1));

        jButton14.setText("Si");
        jButton14.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton14.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton14, new org.netbeans.lib.awtextra.AbsoluteConstraints(363, 67, -1, -1));

        jButton15.setText("P");
        jButton15.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton15.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton15, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 67, -1, -1));

        jButton16.setText("S");
        jButton16.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton16.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton16, new org.netbeans.lib.awtextra.AbsoluteConstraints(417, 67, -1, -1));

        jButton17.setText("Cl");
        jButton17.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton17.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton17, new org.netbeans.lib.awtextra.AbsoluteConstraints(444, 67, -1, -1));

        jButton18.setText("Ar");
        jButton18.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton18.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton18, new org.netbeans.lib.awtextra.AbsoluteConstraints(471, 67, -1, -1));

        jButton19.setText("K");
        jButton19.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton19.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton19, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 94, -1, -1));

        jButton20.setText("Ca");
        jButton20.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton20.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton20, new org.netbeans.lib.awtextra.AbsoluteConstraints(39, 94, -1, -1));

        jButton21.setText("Sc");
        jButton21.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton21.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton21, new org.netbeans.lib.awtextra.AbsoluteConstraints(66, 94, -1, -1));

        jButton22.setText("Ti");
        jButton22.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton22.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton22, new org.netbeans.lib.awtextra.AbsoluteConstraints(93, 94, -1, -1));

        jButton23.setText("V");
        jButton23.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton23.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton23, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 94, -1, -1));

        jButton24.setText("Cr");
        jButton24.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton24.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton24, new org.netbeans.lib.awtextra.AbsoluteConstraints(147, 94, -1, -1));

        jButton25.setText("Mn");
        jButton25.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton25.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton25, new org.netbeans.lib.awtextra.AbsoluteConstraints(174, 94, -1, -1));

        jButton26.setText("Fe");
        jButton26.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton26.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton26, new org.netbeans.lib.awtextra.AbsoluteConstraints(201, 94, -1, -1));

        jButton27.setText("Co");
        jButton27.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton27.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton27, new org.netbeans.lib.awtextra.AbsoluteConstraints(228, 94, -1, -1));

        jButton28.setText("Ni");
        jButton28.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton28.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton28, new org.netbeans.lib.awtextra.AbsoluteConstraints(255, 94, -1, -1));

        jButton29.setText("Cu");
        jButton29.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton29.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton29, new org.netbeans.lib.awtextra.AbsoluteConstraints(282, 94, -1, -1));

        jButton30.setText("Zn");
        jButton30.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton30.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton30, new org.netbeans.lib.awtextra.AbsoluteConstraints(309, 94, -1, -1));

        jButton31.setText("Ga");
        jButton31.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton31.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton31, new org.netbeans.lib.awtextra.AbsoluteConstraints(336, 94, -1, -1));

        jButton32.setText("Ge");
        jButton32.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton32.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton32, new org.netbeans.lib.awtextra.AbsoluteConstraints(363, 94, -1, -1));

        jButton33.setText("As");
        jButton33.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton33.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton33, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 94, -1, -1));

        jButton34.setText("Se");
        jButton34.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton34.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton34, new org.netbeans.lib.awtextra.AbsoluteConstraints(417, 94, -1, -1));

        jButton35.setText("Br");
        jButton35.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton35.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton35, new org.netbeans.lib.awtextra.AbsoluteConstraints(444, 94, -1, -1));

        jButton36.setText("Kr");
        jButton36.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton36.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton36, new org.netbeans.lib.awtextra.AbsoluteConstraints(471, 94, -1, -1));

        jButton37.setText("Rb");
        jButton37.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton37.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton37, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 121, -1, -1));

        jButton38.setText("Sr");
        jButton38.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton38.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton38, new org.netbeans.lib.awtextra.AbsoluteConstraints(39, 121, -1, -1));

        jButton39.setText("Y");
        jButton39.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton39.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton39, new org.netbeans.lib.awtextra.AbsoluteConstraints(66, 121, -1, -1));

        jButton40.setText("Zr");
        jButton40.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton40.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton40, new org.netbeans.lib.awtextra.AbsoluteConstraints(93, 121, -1, -1));

        jButton41.setText("Nb");
        jButton41.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton41.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton41, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 121, -1, -1));

        jButton42.setText("Mo");
        jButton42.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton42.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton42, new org.netbeans.lib.awtextra.AbsoluteConstraints(147, 121, -1, -1));

        jButton43.setText("Tc");
        jButton43.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton43.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton43, new org.netbeans.lib.awtextra.AbsoluteConstraints(174, 121, -1, -1));

        jButton44.setText("Ru");
        jButton44.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton44.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton44, new org.netbeans.lib.awtextra.AbsoluteConstraints(201, 121, -1, -1));

        jButton45.setText("Rh");
        jButton45.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton45.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton45, new org.netbeans.lib.awtextra.AbsoluteConstraints(228, 121, -1, -1));

        jButton46.setText("Pd");
        jButton46.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton46.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton46, new org.netbeans.lib.awtextra.AbsoluteConstraints(255, 121, -1, -1));

        jButton47.setText("Ag");
        jButton47.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton47.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton47, new org.netbeans.lib.awtextra.AbsoluteConstraints(282, 121, -1, -1));

        jButton48.setText("Cd");
        jButton48.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton48.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton48, new org.netbeans.lib.awtextra.AbsoluteConstraints(309, 121, -1, -1));

        jButton49.setText("In");
        jButton49.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton49.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton49, new org.netbeans.lib.awtextra.AbsoluteConstraints(336, 121, -1, -1));

        jButton50.setText("Sn");
        jButton50.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton50.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton50, new org.netbeans.lib.awtextra.AbsoluteConstraints(363, 121, -1, -1));

        jButton51.setText("Sb");
        jButton51.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton51.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton51, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 121, -1, -1));

        jButton52.setText("Te");
        jButton52.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton52.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton52, new org.netbeans.lib.awtextra.AbsoluteConstraints(417, 121, -1, -1));

        jButton53.setText("I");
        jButton53.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton53.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton53, new org.netbeans.lib.awtextra.AbsoluteConstraints(444, 121, -1, -1));

        jButton54.setText("Xe");
        jButton54.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton54.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton54, new org.netbeans.lib.awtextra.AbsoluteConstraints(471, 121, -1, -1));

        jButton55.setText("Cs");
        jButton55.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton55.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton55, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 148, -1, -1));

        jButton56.setText("Ba");
        jButton56.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton56.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton56, new org.netbeans.lib.awtextra.AbsoluteConstraints(39, 148, -1, -1));

        jButton57.setText("La");
        jButton57.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton57.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton57, new org.netbeans.lib.awtextra.AbsoluteConstraints(66, 148, -1, -1));

        jButton72.setText("Hf");
        jButton72.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton72.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton72, new org.netbeans.lib.awtextra.AbsoluteConstraints(93, 148, -1, -1));

        jButton73.setText("Ta");
        jButton73.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton73.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton73, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 148, -1, -1));

        jButton74.setText("W");
        jButton74.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton74.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton74, new org.netbeans.lib.awtextra.AbsoluteConstraints(147, 148, -1, -1));

        jButton75.setText("Re");
        jButton75.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton75.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton75, new org.netbeans.lib.awtextra.AbsoluteConstraints(174, 148, -1, -1));

        jButton76.setText("Os");
        jButton76.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton76.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton76, new org.netbeans.lib.awtextra.AbsoluteConstraints(201, 148, -1, -1));

        jButton77.setText("Ir");
        jButton77.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton77.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton77, new org.netbeans.lib.awtextra.AbsoluteConstraints(228, 148, -1, -1));

        jButton78.setText("Pt");
        jButton78.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton78.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton78, new org.netbeans.lib.awtextra.AbsoluteConstraints(255, 148, -1, -1));

        jButton79.setText("Au");
        jButton79.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton79.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton79, new org.netbeans.lib.awtextra.AbsoluteConstraints(282, 148, -1, -1));

        jButton80.setText("Hg");
        jButton80.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton80.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton80, new org.netbeans.lib.awtextra.AbsoluteConstraints(309, 148, -1, -1));

        jButton81.setText("Tl");
        jButton81.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton81.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton81, new org.netbeans.lib.awtextra.AbsoluteConstraints(336, 148, -1, -1));

        jButton82.setText("Pb");
        jButton82.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton82.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton82, new org.netbeans.lib.awtextra.AbsoluteConstraints(363, 148, -1, -1));

        jButton83.setText("Bi");
        jButton83.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton83.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton83, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 148, -1, -1));

        jButton84.setText("Po");
        jButton84.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton84.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton84, new org.netbeans.lib.awtextra.AbsoluteConstraints(417, 148, -1, -1));

        jButton85.setText("At");
        jButton85.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton85.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton85, new org.netbeans.lib.awtextra.AbsoluteConstraints(444, 148, -1, -1));

        jButton86.setText("Rn");
        jButton86.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton86.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton86, new org.netbeans.lib.awtextra.AbsoluteConstraints(471, 148, -1, -1));

        jButton87.setText("Fr");
        jButton87.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton87.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton87, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 175, -1, -1));

        jButton88.setText("Ra");
        jButton88.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton88.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton88, new org.netbeans.lib.awtextra.AbsoluteConstraints(39, 175, -1, -1));

        jButton89.setText("Ac");
        jButton89.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton89.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton89, new org.netbeans.lib.awtextra.AbsoluteConstraints(66, 175, -1, -1));

        jButton0.setText("e-");
        jButton0.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton0.setPreferredSize(new java.awt.Dimension(27, 27));
        jPanelPeriodicT.add(jButton0, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 10, -1, -1));

        jPanelREEactinides.setOpaque(false);

        jButton58.setText("Ce");
        jButton58.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton58.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton59.setText("Pr");
        jButton59.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton59.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton60.setText("Nd");
        jButton60.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton60.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton61.setText("Pm");
        jButton61.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton61.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton62.setText("Sm");
        jButton62.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton62.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton63.setText("Eu");
        jButton63.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton63.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton64.setText("Gd");
        jButton64.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton64.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton65.setText("Tb");
        jButton65.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton65.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton66.setText("Dy");
        jButton66.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton66.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton67.setText("Ho");
        jButton67.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton67.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton68.setText("Er");
        jButton68.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton68.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton69.setText("Tm");
        jButton69.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton69.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton70.setText("Yb");
        jButton70.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton70.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton71.setText("Lu");
        jButton71.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton71.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton90.setText("Th");
        jButton90.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton90.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton91.setText("Pa");
        jButton91.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton91.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton92.setText("U");
        jButton92.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton92.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton93.setText("Np");
        jButton93.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton93.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton94.setText("Pu");
        jButton94.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton94.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton95.setText("Am");
        jButton95.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton95.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton96.setText("Cm");
        jButton96.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton96.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton97.setText("Bk");
        jButton97.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton97.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton98.setText("Cf");
        jButton98.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton98.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton99.setText("Es");
        jButton99.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton99.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton100.setText("Fm");
        jButton100.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton100.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton101.setText("Md");
        jButton101.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton101.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton102.setText("No");
        jButton102.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton102.setPreferredSize(new java.awt.Dimension(27, 27));

        jButton103.setText("Lr");
        jButton103.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jButton103.setPreferredSize(new java.awt.Dimension(27, 27));

        javax.swing.GroupLayout jPanelREEactinidesLayout = new javax.swing.GroupLayout(jPanelREEactinides);
        jPanelREEactinides.setLayout(jPanelREEactinidesLayout);
        jPanelREEactinidesLayout.setHorizontalGroup(
            jPanelREEactinidesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelREEactinidesLayout.createSequentialGroup()
                .addComponent(jButton90, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton91, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton92, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton93, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton94, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton95, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton96, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton97, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton98, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton99, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton100, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton101, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton102, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton103, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanelREEactinidesLayout.createSequentialGroup()
                .addComponent(jButton58, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton59, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton60, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton61, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton62, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton63, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton64, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton65, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton66, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton67, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton68, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton69, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton70, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton71, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanelREEactinidesLayout.setVerticalGroup(
            jPanelREEactinidesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelREEactinidesLayout.createSequentialGroup()
                .addGroup(jPanelREEactinidesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton58, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton59, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton60, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton61, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton62, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton63, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton64, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton65, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton66, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton67, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton68, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton69, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton70, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton71, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(jPanelREEactinidesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton90, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton91, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton92, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton93, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton94, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton95, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton96, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton97, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton98, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton99, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton100, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton101, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton102, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton103, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelPeriodicT.add(jPanelREEactinides, new org.netbeans.lib.awtextra.AbsoluteConstraints(101, 184, -1, 60));

        jLabelTemperature.setText("Temperature = 80°C");
        jLabelTemperature.setToolTipText("double-click to set T=25´C");
        jLabelTemperature.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelTemperatureMouseClicked(evt);
            }
        });
        jPanelPeriodicT.add(jLabelTemperature, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 10, -1, -1));

        jLabelPressure.setText("Pressure = 1.014 bar");
        jLabelPressure.setToolTipText("");
        jLabelPressure.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelPressureMouseClicked(evt);
            }
        });
        jPanelPeriodicT.add(jLabelPressure, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 30, -1, -1));

        jPanelTable.add(jPanelPeriodicT, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 510, -1));

        jPanelBottom.setLayout(new java.awt.CardLayout());

        jPanelLower.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanelLower.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelAvailableComp.setText("<html><u>A</u>vailable:</html>");
        jPanelLower.add(jLabelAvailableComp, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 7, -1, -1));

        jListAvailableComps.setModel(modelAvailableComps);
        jListAvailableComps.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListAvailableComps.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListAvailableCompsMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jListAvailableCompsMouseExited(evt);
            }
        });
        jListAvailableComps.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jListAvailableCompsMouseMoved(evt);
            }
        });
        jListAvailableComps.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListAvailableCompsValueChanged(evt);
            }
        });
        jListAvailableComps.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jListAvailableCompsFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jListAvailableCompsFocusLost(evt);
            }
        });
        jListAvailableComps.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jListAvailableCompsKeyTyped(evt);
            }
        });
        jScrollPaneAvailableComps.setViewportView(jListAvailableComps);

        jPanelLower.add(jScrollPaneAvailableComps, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 27, 150, 100));

        jLabelComps.setText("<html><u>C</u>omponents selected:</html>");
        jPanelLower.add(jLabelComps, new org.netbeans.lib.awtextra.AbsoluteConstraints(181, 7, -1, -1));

        jListSelectedComps.setModel(modelSelectedComps);
        jListSelectedComps.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListSelectedComps.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListSelectedCompsMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jListSelectedCompsMouseExited(evt);
            }
        });
        jListSelectedComps.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jListSelectedCompsMouseMoved(evt);
            }
        });
        jListSelectedComps.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListSelectedCompsValueChanged(evt);
            }
        });
        jListSelectedComps.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jListSelectedCompsFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jListSelectedCompsFocusLost(evt);
            }
        });
        jListSelectedComps.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jListSelectedCompsKeyTyped(evt);
            }
        });
        jScrollPaneSelectedComps.setViewportView(jListSelectedComps);

        jPanelLower.add(jScrollPaneSelectedComps, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 27, 150, 100));

        jLabelComplexes.setText("<html>Reactions foun<u>d</u>:</html>");
        jPanelLower.add(jLabelComplexes, new org.netbeans.lib.awtextra.AbsoluteConstraints(346, 7, -1, -1));

        jListComplexes.setModel(modelComplexes);
        jListComplexes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListComplexes.setToolTipText("Right-click for a pop-up menu");
        jListComplexes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListComplexesMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jListComplexesMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jListComplexesMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jListComplexesMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jListComplexesMouseReleased(evt);
            }
        });
        jListComplexes.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jListComplexesMouseMoved(evt);
            }
        });
        jListComplexes.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jListComplexesFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jListComplexesFocusLost(evt);
            }
        });
        jListComplexes.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jListComplexesKeyTyped(evt);
            }
        });
        jScrollPaneComplexes.setViewportView(jListComplexes);

        jPanelLower.add(jScrollPaneComplexes, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 27, 150, 100));

        jLabel_cr_solids.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        jLabel_cr_solids.setText("(cr) solids excluded!");
        jPanelLower.add(jLabel_cr_solids, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 127, -1, -1));

        jPanelLowerLabels.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jPanelLowerLabels.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelLeft.setText("left");
        jPanelLowerLabels.add(jLabelLeft, new org.netbeans.lib.awtextra.AbsoluteConstraints(1, 1, -1, -1));

        jLabelMid.setText("mid");
        jPanelLowerLabels.add(jLabelMid, new org.netbeans.lib.awtextra.AbsoluteConstraints(167, 1, -1, -1));

        jLabelRight.setText("right");
        jPanelLowerLabels.add(jLabelRight, new org.netbeans.lib.awtextra.AbsoluteConstraints(328, 1, -1, -1));

        jPanelLower.add(jPanelLowerLabels, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 138, 480, -1));

        jPanelBottom.add(jPanelLower, "cardLower");

        jLabelNow.setText("jLabelNow");

        jLabelNowLoop.setText("jLabelNowLoop");

        jProgressBar.setForeground(java.awt.Color.blue);

        javax.swing.GroupLayout jPanelProgressBarLayout = new javax.swing.GroupLayout(jPanelProgressBar);
        jPanelProgressBar.setLayout(jPanelProgressBarLayout);
        jPanelProgressBarLayout.setHorizontalGroup(
            jPanelProgressBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelProgressBarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelProgressBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
                    .addComponent(jLabelNowLoop)
                    .addComponent(jLabelNow))
                .addContainerGap())
        );
        jPanelProgressBarLayout.setVerticalGroup(
            jPanelProgressBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelProgressBarLayout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(jLabelNow)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelNowLoop)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(57, Short.MAX_VALUE))
        );

        jPanelBottom.add(jPanelProgressBar, "cardProgress");

        jPanelTable.add(jPanelBottom, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 250, -1, 160));

        jMenuFile.setMnemonic('f');
        jMenuFile.setText("File");

        jMenuSearch.setMnemonic('s');
        jMenuSearch.setText("Search reactions in database");
        jMenuSearch.setEnabled(false);
        jMenuSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuSearchActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuSearch);

        jMenuExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK));
        jMenuExit.setMnemonic('x');
        jMenuExit.setText("Search and Exit");
        jMenuExit.setEnabled(false);
        jMenuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuExitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuExit);

        jMenuQuit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.ALT_MASK));
        jMenuQuit.setMnemonic('q');
        jMenuQuit.setText("Quit");
        jMenuQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuQuitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuQuit);

        jMenuBar.add(jMenuFile);

        jMenuOptions.setMnemonic('o');
        jMenuOptions.setText("Options");
        jMenuOptions.setEnabled(false);

        jMenuItemTemperature.setMnemonic('T');
        jMenuItemTemperature.setText("set Temperature & pressure");
        jMenuItemTemperature.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemTemperatureActionPerformed(evt);
            }
        });
        jMenuOptions.add(jMenuItemTemperature);

        jMenuPref.setMnemonic('P');
        jMenuPref.setText("Preferences");

        jMenuAdvH2O.setMnemonic('w');
        jMenuAdvH2O.setText("Water (H2O)");
        jMenuAdvH2O.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuAdvH2OActionPerformed(evt);
            }
        });
        jMenuPref.add(jMenuAdvH2O);

        jMenuAdvRedox.setMnemonic('R');
        jMenuAdvRedox.setText("Redox reactions");
        jMenuAdvRedox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuAdvRedoxActionPerformed(evt);
            }
        });
        jMenuPref.add(jMenuAdvRedox);

        jMenuAdvSolids.setMnemonic('S');
        jMenuAdvSolids.setText("Solids");
        jMenuAdvSolids.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuAdvSolidsActionPerformed(evt);
            }
        });
        jMenuPref.add(jMenuAdvSolids);

        jMenuLocate.setMnemonic('L');
        jMenuLocate.setText("Locate diagram-making program");
        jMenuLocate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuLocateActionPerformed(evt);
            }
        });
        jMenuPref.add(jMenuLocate);

        jCheckBoxMenuVerbose.setMnemonic('V');
        jCheckBoxMenuVerbose.setText("debug (Verbose output of messages)");
        jCheckBoxMenuVerbose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuVerboseActionPerformed(evt);
            }
        });
        jMenuPref.add(jCheckBoxMenuVerbose);

        jCheckBoxMenuDebug.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK));
        jCheckBoxMenuDebug.setMnemonic('S');
        jCheckBoxMenuDebug.setText("Show errors and messages");
        jCheckBoxMenuDebug.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuDebugActionPerformed(evt);
            }
        });
        jMenuPref.add(jCheckBoxMenuDebug);

        jMenuOptions.add(jMenuPref);

        jMenuData.setMnemonic('d');
        jMenuData.setText("Data");

        jMenuDBFiles.setMnemonic('D');
        jMenuDBFiles.setText("Database files");
        jMenuDBFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuDBFilesActionPerformed(evt);
            }
        });
        jMenuData.add(jMenuDBFiles);

        jMenuAddData.setMnemonic('A');
        jMenuAddData.setText("Add data !");
        jMenuAddData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuAddDataActionPerformed(evt);
            }
        });
        jMenuData.add(jMenuAddData);

        jMenuSingleC.setMnemonic('C');
        jMenuSingleC.setText("display single Component");
        jMenuSingleC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuSingleCActionPerformed(evt);
            }
        });
        jMenuData.add(jMenuSingleC);

        jMenuAdvanced.setMnemonic('v');
        jMenuAdvanced.setText("advanced");

        jMenuRefs.setMnemonic('R');
        jMenuRefs.setText("add-show References");
        jMenuRefs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuRefsActionPerformed(evt);
            }
        });
        jMenuAdvanced.add(jMenuRefs);

        jMenuMaintenance.setMnemonic('M');
        jMenuMaintenance.setText("database Maintenance");
        jMenuMaintenance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuMaintenanceActionPerformed(evt);
            }
        });
        jMenuAdvanced.add(jMenuMaintenance);

        jMenuData.add(jMenuAdvanced);

        jMenuOptions.add(jMenuData);

        jMenuBar.add(jMenuOptions);

        jMenuH.setMnemonic('h');
        jMenuH.setText("Help");
        jMenuH.setEnabled(false);

        jMenuHelp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        jMenuHelp.setMnemonic('c');
        jMenuHelp.setText("help Contents");
        jMenuHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuHelpActionPerformed(evt);
            }
        });
        jMenuH.add(jMenuHelp);

        jMenuHelpAbout.setMnemonic('a');
        jMenuHelpAbout.setText("About");
        jMenuHelpAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuHelpAboutActionPerformed(evt);
            }
        });
        jMenuH.add(jMenuHelpAbout);

        jMenuBar.add(jMenuH);

        setJMenuBar(jMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

  //<editor-fold defaultstate="collapsed" desc="Events">

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        queryToQuit = true;
        end_program();
    }//GEN-LAST:event_formWindowClosing

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        if(pd.msgFrame != null) {
            pd.msgFrame.setParentFrame(this);
            jCheckBoxMenuDebug.setSelected(pd.msgFrame.isVisible());
        }
    }//GEN-LAST:event_formWindowActivated

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        if(helpAboutFrame != null) {helpAboutFrame.bringToFront();}
        if(pd.msgFrame != null) {jCheckBoxMenuDebug.setSelected(pd.msgFrame.isVisible());}
        else {jCheckBoxMenuDebug.setEnabled(false);}
        if(lastReactionInFocus >-1) {
            jListComplexes.requestFocusInWindow();
            jListComplexes.ensureIndexIsVisible(lastReactionInFocus);
            jListComplexes.setSelectedIndex(lastReactionInFocus);
        } else if(lastButtonInFocus >-1) {
            buttons[lastButtonInFocus].requestFocusInWindow();
        } else {
            jListSelectedComps.requestFocusInWindow();
        }
    }//GEN-LAST:event_formWindowGainedFocus

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
      if(windowSize != null) {
        int w = windowSize.width;
        int h = windowSize.height;
        if(this.getHeight()<h){this.setSize(this.getWidth(), h);}
        if(this.getWidth()<w){this.setSize(w,this.getHeight());}
      }
    }//GEN-LAST:event_formComponentResized

    private void jMenuSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuSearchActionPerformed
        searchReactions(false); // exit = false
    }//GEN-LAST:event_jMenuSearchActionPerformed

    private void jMenuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuExitActionPerformed
        searchReactions(true); // exit = true
    }//GEN-LAST:event_jMenuExitActionPerformed

    private void jMenuQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuQuitActionPerformed
        queryToQuit = false;
        end_program();
    }//GEN-LAST:event_jMenuQuitActionPerformed

    private void jCheckBoxMenuDebugActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuDebugActionPerformed
        if(pd.msgFrame != null) {
            pd.msgFrame.setVisible(jCheckBoxMenuDebug.isSelected());
        }
    }//GEN-LAST:event_jCheckBoxMenuDebugActionPerformed

    private void jMenuDBFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuDBFilesActionPerformed
        setCursorWait();
        if(pc.dbg) {System.out.println("--- jMenuDBFiles(event)");}
        dbND = new DBnamesDialog(this, true, pc, pd.dataBasesList, pd.pathDatabaseFiles, pd.elemComp);
        dbND.setVisible(true); //this will wait for the modal dialog to close
        if(!dbND.cancel) {
            //---- read the elements/components for the databases
            LibDB.getElements(this, pc.dbg, pd.dataBasesList, pd.elemComp);
            pd.foundH2O = false;
            for (String[] elemComp : pd.elemComp) {
                if (Util.isWater(elemComp[1])) {pd.foundH2O = true; break;}
            }
            //---- show which elements have data
            setupFrame();
            int i = modelSelectedComps.size()-1;
            while(i>0) {
                componentClick(i);
                i = modelSelectedComps.size()-1;
            }
        }
        dbND.dispose();
        dbND = null;
        //this.setVisible(true);
        setCursorDef();
        bringToFront();
    }//GEN-LAST:event_jMenuDBFilesActionPerformed

    private void jMenuLocateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuLocateActionPerformed
      setCursorWait();
      String fn = Util.getOpenFileName(this, pc.progName, true, "Select a program:",
              1, pd.diagramProgr, null);
      if(fn == null || fn.length() <=0) {setCursorDef(); return;}
      java.io.File f = new java.io.File(fn);
      fn = f.getName();
      if(!fn.toLowerCase().startsWith("spana") && !fn.toLowerCase().startsWith("medusa")) {
            String msg = "Warning: \""+fn+"\""+nl+"is NOT the \"Spana\" program."+nl+
                      "(the program to make diagrams)";
            javax.swing.JOptionPane.showMessageDialog(this, msg, pc.progName, javax.swing.JOptionPane.WARNING_MESSAGE);
            MsgExceptn.msg(msg);
      } //is the file name "spana"?
      try {fn = f.getCanonicalPath();} catch (java.io.IOException ex) {fn = null;}
      if(fn == null) {
            try {fn = f.getAbsolutePath();} catch (Exception ex) {fn = f.getPath();}
      }
      pd.diagramProgr = fn;
      if(pc.dbg) {System.out.println("--- Path to diagram-making program: "+pd.diagramProgr);}
      setCursorDef();
    }//GEN-LAST:event_jMenuLocateActionPerformed

    private void jMenuAddDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuAddDataActionPerformed
        setCursorWait();
        if(pc.dbg) {System.out.println("--- jMenuAddData(event)");}
        if(addData != null) {
            String msg = "Programming error: addData != null in Add Data Menu";
            MsgExceptn.showErrMsg(this,msg,1);
            setCursorDef();
            return;
        }
        jMenuAddData.setEnabled(false);
        Thread adShow = new Thread() {@Override public void run(){
            addData = new FrameAddData(pc, pd, dbf);
            dbf.setVisible(false);
            addData.start();
            addData.waitFor();
            javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
                jMenuAddData.setEnabled(true);
                addData = null;
                //---- read the elements/components for the databases
                LibDB.getElements(dbf, pc.dbg, pd.dataBasesList, pd.elemComp);
                pd.foundH2O = false;
                for(int i=0; i < pd.elemComp.size(); i++) {
                  if(Util.isWater(pd.elemComp.get(i)[1])) {pd.foundH2O = true; break;}
                }
                //---- show which elements have data
                setupFrame();
                bringToFront();
                setCursorDef();
            }}); //invokeLater(Runnable)
        }};//new Thread
        adShow.start();
    }//GEN-LAST:event_jMenuAddDataActionPerformed

    private void jMenuSingleCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuSingleCActionPerformed
        if(singleComp != null) {MsgExceptn.exception("Programming error: singleComp != null in Single Component Menu"); return;}
        setCursorWait();
        jMenuSingleC.setEnabled(false);
        Thread scShow = new Thread() {@Override public void run(){
            singleComp = new FrameSingleComponent(dbf, pc, pd);
            singleComp.start();
            javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
                setCursorDef();
                dbf.setVisible(false);
            }});
            singleComp.waitFor();
            javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
                jMenuSingleC.setEnabled(true);
                singleComp = null;
                bringToFront();
            }}); //invokeLater(Runnable)
        }};//new Thread
        scShow.start();
    }//GEN-LAST:event_jMenuSingleCActionPerformed

    private void jMenuHelpAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuHelpAboutActionPerformed
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        jMenuHelpAbout.setEnabled(false);
        // -- although HelpAboutDB is a frame, it behaves almost as a modal dialog
        //    because it is brought to focus when "this" gains focus
        Thread hlp = new Thread() {@Override public void run(){
            dbf.helpAboutFrame = new HelpAboutDB(pc.pathAPP, pd, pc.saveIniFileToApplicationPathOnly);
            dbf.helpAboutFrame.start();
            dbf.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            dbf.helpAboutFrame.waitFor();
            javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
                dbf.helpAboutFrame = null;
                jMenuHelpAbout.setEnabled(true);
                bringToFront();
            }}); //invokeLater(Runnable)
        }};//new Thread
        hlp.start();
    }//GEN-LAST:event_jMenuHelpAboutActionPerformed

    private void jMenuHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuHelpActionPerformed
        setCursorWait();
        Thread hlp = new Thread() {@Override public void run(){
            String[] a = {"DB_0_Main_htm"};
            lib.huvud.RunProgr.runProgramInProcess(null,ProgramConf.HELP_JAR,a,false,pc.dbg,pc.pathAPP);
            try{Thread.sleep(2000);}   //show the "wait" cursor for 2 sec
            catch (InterruptedException e) {}
            setCursorDef();
        }};//new Thread
        hlp.start();
    }//GEN-LAST:event_jMenuHelpActionPerformed

    private void jListAvailableCompsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListAvailableCompsFocusGained
        if(modelAvailableComps.getSize()>0) {
          int i = jListAvailableComps.getSelectedIndex();
          if(i<0) {jListAvailableComps.setSelectedIndex(0);}
        }
        jListSelectedComps.clearSelection();
        if(jListComplexes.isVisible()) {jListComplexes.clearSelection();} else {lastReactionInFocus = -1;}
    }//GEN-LAST:event_jListAvailableCompsFocusGained

    private void jListAvailableCompsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListAvailableCompsFocusLost
        jListAvailableComps.clearSelection();
    }//GEN-LAST:event_jListAvailableCompsFocusLost

    private void jListAvailableCompsKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jListAvailableCompsKeyTyped
        if(evt.getKeyChar() == java.awt.event.KeyEvent.VK_SPACE) {
            evt.consume();
            availableComponentClick();
        }
    }//GEN-LAST:event_jListAvailableCompsKeyTyped

    private void jListAvailableCompsMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListAvailableCompsMouseMoved
        java.awt.Point p = evt.getPoint();
        int i = jListAvailableComps.locationToIndex(p);
        if(i>=0) {
            java.awt.Rectangle r = jListAvailableComps.getCellBounds(i, i);
            if(p.y < r.y || p.y > r.y+r.height) {i=-1;}
            if(i>=0) {
                jLabelLeft.setText(availableComponentsNames.get(i));
            } else {jLabelLeft.setText(" ");}
        }
    }//GEN-LAST:event_jListAvailableCompsMouseMoved

    private void jListAvailableCompsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListAvailableCompsMouseClicked
        java.awt.Point p = evt.getPoint();
        int i = jListAvailableComps.locationToIndex(p);
        if(i>=0) {
            java.awt.Rectangle r = jListAvailableComps.getCellBounds(i, i);
            if(p.y < r.y || p.y > r.y+r.height) {return;}
            availableComponentClick(i);
        }
    }//GEN-LAST:event_jListAvailableCompsMouseClicked

    private void jListAvailableCompsMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListAvailableCompsMouseExited
        jLabelLeft.setText(originalJLabelLeft.toString());
    }//GEN-LAST:event_jListAvailableCompsMouseExited

    private void jListAvailableCompsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListAvailableCompsValueChanged
        int i = jListAvailableComps.getSelectedIndex();
        if(i>=0) {
            jLabelLeft.setText(availableComponentsNames.get(i));
        } else {
            jLabelLeft.setText(originalJLabelLeft.toString());
        }
    }//GEN-LAST:event_jListAvailableCompsValueChanged

    private void jListSelectedCompsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListSelectedCompsFocusGained
        jListAvailableComps.clearSelection();
        if(jListComplexes.isVisible()) {jListComplexes.clearSelection();}
        if(modelSelectedComps.getSize()>0) {
          int i = jListSelectedComps.getSelectedIndex();
          if(i>=0) {
              //jListSelectedComps.ensureIndexIsVisible(i);
              lastReactionInFocus = i;
          } else {
              jListSelectedComps.setSelectedIndex(0);
              lastReactionInFocus = 0;
          }
        }
    }//GEN-LAST:event_jListSelectedCompsFocusGained

    private void jListSelectedCompsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListSelectedCompsFocusLost
        jListSelectedComps.clearSelection();
        if(modelSelectedComps.size() <=0) {
            jMenuExit.setText("Search and Exit");
            jMenuExit.setEnabled(false);
            jMenuSearch.setEnabled(false);
        }
        jLabelMid.setText(" ");
    }//GEN-LAST:event_jListSelectedCompsFocusLost

    private void jListSelectedCompsKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jListSelectedCompsKeyTyped
        if(evt.getKeyChar() == java.awt.event.KeyEvent.VK_BACK_SPACE ||
                    evt.getKeyChar() == java.awt.event.KeyEvent.VK_DELETE) {
            evt.consume();
            componentClick();
        }
    }//GEN-LAST:event_jListSelectedCompsKeyTyped

    private void jListSelectedCompsMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListSelectedCompsMouseMoved
        java.awt.Point p = evt.getPoint();
        int i = jListSelectedComps.locationToIndex(p);
        if(i>=0) {
            java.awt.Rectangle r = jListSelectedComps.getCellBounds(i, i);
            if(p.y < r.y || p.y > r.y+r.height) {i=-1;}
            if(i>=0) {jLabelMid.setText(componentsNames.get(i));} else {jLabelMid.setText(" ");}
        }
    }//GEN-LAST:event_jListSelectedCompsMouseMoved

    private void jListSelectedCompsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListSelectedCompsMouseClicked
        java.awt.Point p = evt.getPoint();
        int i = jListSelectedComps.locationToIndex(p);
        if(i>=0) {
            java.awt.Rectangle r = jListSelectedComps.getCellBounds(i, i);
            if(p.y < r.y || p.y > r.y+r.height) {i=-1;}
            componentClick(i);
        }
    }//GEN-LAST:event_jListSelectedCompsMouseClicked

    private void jListSelectedCompsMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListSelectedCompsMouseExited
        jLabelMid.setText(" ");
    }//GEN-LAST:event_jListSelectedCompsMouseExited

    private void jListSelectedCompsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListSelectedCompsValueChanged
        int i = jListSelectedComps.getSelectedIndex();
        if(i>=0) {jLabelMid.setText(componentsNames.get(i));} else {jLabelMid.setText(" ");}
    }//GEN-LAST:event_jListSelectedCompsValueChanged

    private void jListComplexesFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListComplexesFocusGained
        jListAvailableComps.clearSelection();
        jListSelectedComps.clearSelection();
        if(!jPopupMenu.isVisible() && modelComplexes.getSize()>0) {
          int i = lastReactionInFocus;
          if(i < 0) {i = jListComplexes.getSelectedIndex();}
          if(i>=0) {
              jListComplexes.ensureIndexIsVisible(i);
              //jListComplexes.setSelectedIndex(i);
              lastReactionInFocus = i;
          } else {
              jListComplexes.setSelectedIndex(0);
              lastReactionInFocus = 0;
          }
        }
    }//GEN-LAST:event_jListComplexesFocusGained

    private void jListComplexesFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListComplexesFocusLost
        if(!jPopupMenu.isVisible()) {jListComplexes.clearSelection();}
    }//GEN-LAST:event_jListComplexesFocusLost

    private void jListComplexesKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jListComplexesKeyTyped
        if(evt.getKeyChar() == java.awt.event.KeyEvent.VK_SPACE) {
            int i = jListComplexes.getSelectedIndex();
            if(i>=0) {
                lastReactionInFocus = i;
                jListComplexes.ensureIndexIsVisible(i);
                jListComplexes.setSelectedIndex(i);
                setTextLabelRight(" ");
                java.awt.Rectangle r = jListComplexes.getCellBounds(i, i);
                jPopupMenu.show(jListComplexes, (r.x+r.width/2), (r.y+r.height/2));
            }//if i>=0
            isPopup = false;
        } // if "space"
        else if(evt.getKeyChar() == java.awt.event.KeyEvent.VK_BACK_SPACE ||
                    evt.getKeyChar() == java.awt.event.KeyEvent.VK_DELETE) {
            evt.consume();
            complexClick();
        }
    }//GEN-LAST:event_jListComplexesKeyTyped

    private void jListComplexesMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListComplexesMouseEntered
        if(jLabelLeft.getText().trim().length()>0) {
            originalJLabelLeft.replace(0, originalJLabelLeft.length(), jLabelLeft.getText());
            jLabelLeft.setText(" ");
        }
        jLabelMid.setText(" ");
    }//GEN-LAST:event_jListComplexesMouseEntered

    private void jListComplexesMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListComplexesMouseMoved
        if(jPopupMenu.isVisible()) {return;}
        java.awt.Point p = evt.getPoint();
        int i = jListComplexes.locationToIndex(p);
        if(i>=0) {
            java.awt.Rectangle r = jListComplexes.getCellBounds(i, i);
            if(p.y < r.y || p.y > r.y+r.height) {i=-1;}
            String rt;
            if(i>=0) {
                rt = srch.dat.get(i).reactionTextWithLogK(pd.temperature_C, pd.pressure_bar);
            } else {
                rt = " ";
            }
            setTextLabelRight(rt);
        }
    }//GEN-LAST:event_jListComplexesMouseMoved

    private void jListComplexesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListComplexesMouseClicked
        int i = -1;
        if(isPopup || evt.getClickCount() >1) {
            java.awt.Point p = evt.getPoint();
            i = jListComplexes.locationToIndex(p);
            if(i>=0) {
                java.awt.Rectangle r = jListComplexes.getCellBounds(i, i);
                if(p.y < r.y || p.y > r.y+r.height) {i=-1;}
            }
        }
        //if(pc.dbg) {System.out.println("jListComplexesMouseClicked("+i+")");}
        if(i>=0 && i < modelComplexes.size()) {
            lastReactionInFocus = i;
            if(isPopup) {
                jListComplexes.ensureIndexIsVisible(i);
                jListComplexes.setSelectedIndex(i);
                setTextLabelRight(" ");
                jPopupMenu.show(jListComplexes, evt.getX(), evt.getY());
            }
            else if(evt.getClickCount() >1) {
                ShowDetailsDialog dd = new ShowDetailsDialog(this, true, srch.dat.get(i),
                        pd.temperature_C, pd.pressure_bar, pd.references);
                dd.setVisible(true);
            }
        }//if i>=0
        isPopup = false;
    }//GEN-LAST:event_jListComplexesMouseClicked

    private void jListComplexesMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListComplexesMouseExited
        setTextLabelRight(" ");
        jLabelLeft.setText(originalJLabelLeft.toString());
    }//GEN-LAST:event_jListComplexesMouseExited

    private void jMenuItemDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDelActionPerformed
        complexClick();
    }//GEN-LAST:event_jMenuItemDelActionPerformed

    private void jMenuItemCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCancelActionPerformed
        jPopupMenu.setVisible(false);
    }//GEN-LAST:event_jMenuItemCancelActionPerformed

    private void jListComplexesMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListComplexesMousePressed
        if(evt.isPopupTrigger()) {isPopup = true;}
    }//GEN-LAST:event_jListComplexesMousePressed

    private void jListComplexesMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListComplexesMouseReleased
        if(evt.isPopupTrigger()) {isPopup = true;}
    }//GEN-LAST:event_jListComplexesMouseReleased

    private void jMenuAdvH2OActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuAdvH2OActionPerformed
        setCursorWait();
        pd.includeH2O = askH2O(this, pc.progName, pd.includeH2O);
        bringToFront();
        setCursorDef();
    }//GEN-LAST:event_jMenuAdvH2OActionPerformed

    private void jMenuItemTemperatureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemTemperatureActionPerformed
      SetTempPressDialog tDialog = new SetTempPressDialog(dbf, true, pc, pd);
      setTitle();
      if(pc.dbg) {
          System.out.println("--- temperature changed to = "+pd.temperature_C+nl+
                             "      pressure to = "+(float)pd.pressure_bar);
      }
      if(srch != null) {
          srch.temperature_C = pd.temperature_C;
          srch.pressure_bar = pd.pressure_bar;
          if(pc.dbg) {
              System.out.println("setting temperature of search results = "+srch.temperature_C+
                      ", pressure = "+srch.pressure_bar);
          }
      }
    }//GEN-LAST:event_jMenuItemTemperatureActionPerformed

    private void jMenuItemDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDataActionPerformed
      int index = jListComplexes.getSelectedIndex();
      lastReactionInFocus = index;
      if(pc.dbg) {System.out.println("jMenuItemData("+index+")");}
      if(index <0 || index >= modelComplexes.size()) {return;}
      if(pc.dbg) {System.out.println("Show data(s) for: \""+srch.dat.get(index).name.trim()+"\"");}
      ShowDetailsDialog dd = new ShowDetailsDialog(this, true, srch.dat.get(index),
              pd.temperature_C, pd.pressure_bar, pd.references);
      dd.setVisible(true);
    }//GEN-LAST:event_jMenuItemDataActionPerformed

    private void jLabelTemperatureMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelTemperatureMouseClicked
      if(evt.getClickCount() >1  // double-click
              && (pd.temperature_C <24.99 || pd.temperature_C >25.01
              || pd.pressure_bar >1.02)) {
        pd.temperature_C = 25;
        pd.pressure_bar = 1.;
        setTitle();
        if(pc.dbg) {
            System.out.println("--- temperature changed to = "+pd.temperature_C+", p = "+(float)pd.pressure_bar+" bar");
        }
        if(srch != null) {
            srch.temperature_C = pd.temperature_C;
            srch.pressure_bar = pd.pressure_bar;
            if(pc.dbg) {
              System.out.println("    setting temperature of search results = "+srch.temperature_C+", p = "+srch.pressure_bar+" bar");
            }
        }
      }
    }//GEN-LAST:event_jLabelTemperatureMouseClicked

    private void jMenuRefsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuRefsActionPerformed
        setCursorWait();
        Thread asr = new Thread() {@Override public void run(){
            //---- start the program on a separate process
            lib.huvud.RunProgr.runProgramInProcess(null,"AddShowReferences.jar",null,false,pc.dbg,pc.pathAPP);
            try{Thread.sleep(2000);}   //show the "wait" cursor for 2 sec
            catch (InterruptedException e) {}
            setCursorDef();
        }};//new Thread
        asr.start();
    }//GEN-LAST:event_jMenuRefsActionPerformed

    private void jMenuMaintenanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuMaintenanceActionPerformed
        setCursorWait();
        Thread dm = new Thread() {@Override public void run(){
            //---- start the program on a separate process
            lib.huvud.RunProgr.runProgramInProcess(null,"DataMaintenance.jar",null,false,pc.dbg,pc.pathAPP);
            try{Thread.sleep(2000);}   //show the "wait" cursor for 2 sec
            catch (InterruptedException e) {}
            setCursorDef();
        }};//new Thread
        dm.start();
    }//GEN-LAST:event_jMenuMaintenanceActionPerformed

    private void jCheckBoxMenuVerboseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuVerboseActionPerformed
        pc.dbg = jCheckBoxMenuVerbose.isSelected();
    }//GEN-LAST:event_jCheckBoxMenuVerboseActionPerformed

    private void jMenuAdvRedoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuAdvRedoxActionPerformed
        boolean askingBeforeSearch = false;
        AskRedox ask = new AskRedox(this, true, pc, pd, askingBeforeSearch, modelSelectedComps);
        ask.start();
    }//GEN-LAST:event_jMenuAdvRedoxActionPerformed

    private void jMenuAdvSolidsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuAdvSolidsActionPerformed
        boolean askingBeforeSearch = false;
        AskSolids askSolids = new AskSolids(this, true, pc, pd, askingBeforeSearch);
        askSolids.start();
    }//GEN-LAST:event_jMenuAdvSolidsActionPerformed

    private void jLabelPressureMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelPressureMouseClicked
      if(evt.getClickCount() >1  // double-click
              && (pd.temperature_C <24.99 || pd.temperature_C >25.01
              || pd.pressure_bar >1.02)) {
        pd.temperature_C = 25;
        pd.pressure_bar = 1.;
        setTitle();
        if(pc.dbg) {
            System.out.println("--- temperature changed to = "+pd.temperature_C+", p = "+(float)pd.pressure_bar+" bar");
        }
        if(srch != null) {
            srch.temperature_C = pd.temperature_C;
            srch.pressure_bar = pd.pressure_bar;
            if(pc.dbg) {
              System.out.println("    setting temperature of search results = "+srch.temperature_C+", p = "+srch.pressure_bar+" bar");
            }
        }
      }
    }//GEN-LAST:event_jLabelPressureMouseClicked

    //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Methods">

  //<editor-fold defaultstate="collapsed" desc="end_program()">
  void end_program() {
    if(pc.dbg){System.out.println(pc.progName+"-- end_program()");}
    if(addData != null) {
        addData.bringToFront();
        if(!addData.queryClose()) {return;}
    }
    if((doneSomeWork && queryToQuit) || searchingComplexes) {
        bringToFront();
        Object[] opt = {"Yes", "Cancel"};
        int m = javax.swing.JOptionPane.showOptionDialog(this,
                "Quit?",
                pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
        if(m != javax.swing.JOptionPane.YES_OPTION) {return;}
      } //if query
      if(helpAboutFrame != null) {helpAboutFrame.closeWindow();}
      if(fileIniUser != null) {saveIni(fileIniUser);} else if(fileIni != null) {saveIni(fileIni);}
      this.dispose();
      dbf = null;
      OneInstance.endCheckOtherInstances();
      if(pc.dbg) {MsgExceptn.showErrMsg(this, "DataBase - Debug mode"+nl+"System.exit(0);", 3);}
      System.exit(0);
  } // end_program()
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="bringToFront()">
  public void bringToFront() {
    if(this != null) {
        if(addData != null) {
            addData.bringToFront();
            return;
        } else if(singleComp != null) {
            singleComp.bringToFront();
            return;
        } else if(dbND != null) {
            dbND.bringToFront();
            return;
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                setVisible(true);
                if((getExtendedState() & javax.swing.JFrame.ICONIFIED) // minimised?
                            == javax.swing.JFrame.ICONIFIED) {
                    setExtendedState(javax.swing.JFrame.NORMAL);
                } // if minimized
                setAlwaysOnTop(true);
                toFront();
                requestFocus();
                setAlwaysOnTop(false);
                pd.msgFrame.setParentFrame(FrameDBmain.this);
                if(windowSize == null) {
                    // get the size after the window is made visible
                    windowSize = getSize();
                }
            }
        });
    } // if this != null
  } // bringToFront()
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="askH2O">
/** Show a JOptionPane asking the user if H2O should be included
 * <p>The JOptionPane responds to both the TAB key and to
 * the arrow keys in the keyboard
 * @return true if the user clicks the OK button and the "include" radio button is selected;
 * false if either a) the user chooses "Cancel" or b) the dialog is closed, or
 * c) if OK is clicked and the "do not include" radio button is selected */

private static boolean askH2O(java.awt.Frame parent, final String title, final boolean includeH2O) {
  //-- crate a group of radio buttons
  javax.swing.JLabel label = new javax.swing.JLabel("Water (H2O):");
  final javax.swing.JRadioButton radio1 = new javax.swing.JRadioButton("Include H2O as a component in output files");
  //javax.swing.SwingUtilities.invokeLater(new Runnable() {public void run() { radio1.requestFocusInWindow(); }});
  javax.swing.JRadioButton radio2 = new javax.swing.JRadioButton("do Not include H2O");
  radio1.setMnemonic('i');
  radio2.setMnemonic('n');
  javax.swing.ButtonGroup group = new javax.swing.ButtonGroup();
  group.add(radio1); group.add(radio2);
  radio1.setSelected(includeH2O);
  radio2.setSelected(!includeH2O);
  //-- create the object to display in the JOptionPane
  Object[] array = {label,radio1,radio2};
  //-- the option pane
  final Object[] options = {"Ok","Cancel"};
  final javax.swing.JOptionPane pane = new javax.swing.JOptionPane(array,
          javax.swing.JOptionPane.PLAIN_MESSAGE, javax.swing.JOptionPane.OK_CANCEL_OPTION, null, options, options[0]);
  //-- bind to the arrow keys
  java.util.Set<java.awt.AWTKeyStroke> keys = new java.util.HashSet<java.awt.AWTKeyStroke>(
      pane.getFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
  keys.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0));
  keys.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, 0));
  pane.setFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, keys);

  keys = new java.util.HashSet<java.awt.AWTKeyStroke>(
      pane.getFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
  keys.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0));
  keys.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, 0));
  pane.setFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, keys);
  //-- create a dialog
  final javax.swing.JDialog dialog = pane.createDialog(parent, title);
  //--- Alt-Q
  javax.swing.KeyStroke altQKeyStroke = javax.swing.KeyStroke.getKeyStroke(
          java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.ALT_MASK, false);
  dialog.getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altQKeyStroke,"ALT_Q");
  javax.swing.Action altQAction = new javax.swing.AbstractAction() {
    @Override public void actionPerformed(java.awt.event.ActionEvent e) {
        pane.setValue(options[1]);
        dialog.setVisible(false);
        dialog.dispose();
    }};
  dialog.getRootPane().getActionMap().put("ALT_Q", altQAction);
  //--- Alt-X
  javax.swing.KeyStroke altXKeyStroke = javax.swing.KeyStroke.getKeyStroke(
          java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK, false);
  dialog.getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altXKeyStroke,"ALT_X");
  javax.swing.Action altXAction = new javax.swing.AbstractAction() {
    @Override public void actionPerformed(java.awt.event.ActionEvent e) {
        pane.setValue(options[0]);
        dialog.setVisible(false);
        dialog.dispose();
    }};
  dialog.getRootPane().getActionMap().put("ALT_X", altXAction);
  //--- F1 for help
  javax.swing.KeyStroke f1KeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1,0, false);
  dialog.getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(f1KeyStroke,"F1");
  javax.swing.Action f1Action = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            FrameDBmain.getInstance().setCursorWait();
            dialog.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            Thread hlp = new Thread() {@Override public void run(){
                String[] a = {"DB_H2O_htm"};
                lib.huvud.RunProgr.runProgramInProcess(null,ProgramConf.HELP_JAR,a,false,dbf.pc.dbg,dbf.pc.pathAPP);
                try{Thread.sleep(2000);}   //show the "wait" cursor for 2 sec
                catch (InterruptedException e) {}
                dialog.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                FrameDBmain.getInstance().setCursorDef();
            }};//new Thread
            hlp.start();
        }};
  dialog.getRootPane().getActionMap().put("F1", f1Action);
  //--- Alt-H help
  javax.swing.KeyStroke altHKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.ALT_MASK, false);
  dialog.getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altHKeyStroke,"ALT_H");
  dialog.getRootPane().getActionMap().put("ALT_H", f1Action);

  //-- show the dialog
  dialog.setVisible(true);
  //-- The return value
  int res = javax.swing.JOptionPane.CLOSED_OPTION; //default return value, signals nothing selected
  // Get the selected Value
  Object selectedValue = pane.getValue();
  // If none, then nothing selected
  if (selectedValue != null) {
        for (int i = 0, n = options.length; i < n; i++) {
          if (options[i].equals(selectedValue)) {res = i; break;}
        }
  }
  // the user pressed the OK button?
  if(res != javax.swing.JOptionPane.OK_OPTION) {return includeH2O;}
  return radio1.isSelected();
} //askH2O

//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="availableComponentClick">
  private void availableComponentClick() {
    int index = jListAvailableComps.getSelectedIndex();
    availableComponentClick(index);
  }
  private void availableComponentClick(int index) {
    if(index < 0 || index >= modelAvailableComps.size()) {return;}
    //if(pc.dbg) {System.out.println("availableComponentClick("+index+")");}
    boolean alreadyThere = false;
    if(modelSelectedComps.size() >0) {
       for(int i = 0; i < modelSelectedComps.size(); i++) {
         if(modelAvailableComps.get(index).equals(modelSelectedComps.get(i))) {alreadyThere = true; break;}
       }
    }
    if(!alreadyThere) {
        jLabelComplexes.setVisible(false);
        jScrollPaneComplexes.setVisible(false);
        jLabel_cr_solids.setText(" ");
        setTextLabelRight(" ");
        int position;
        if(modelAvailableComps.get(index).equals("H+")) {
            position = 0;
        } else if(modelAvailableComps.get(index).equals("e-")) {
            position = 0;
            if(modelSelectedComps.size() >0 && modelSelectedComps.get(0).equals("H+")) {position = 1;}
        } else {
            if(Util.isSolid(modelAvailableComps.get(index).toString())) {
                solidSelectedComps++;
                position = -1;
            } else { //not a solid
                if(solidSelectedComps >0) {
                    position = modelSelectedComps.size() - solidSelectedComps;
                } else { //no selected solid components yet
                    position = -1;
                }
            } //solid?
        } //not H+ or e-
        if(position == -1) {position = modelSelectedComps.size();}
        modelSelectedComps.add(position, modelAvailableComps.get(index));
        componentsNames.add(position, availableComponentsNames.get(index));
        doneSomeWork = true;
    } //if not alreadyThere
    jListAvailableComps.clearSelection();
    jMenuExit.setText("Search and Exit");
    if(modelSelectedComps.size() >0) {
      jMenuExit.setEnabled(true);
      jMenuSearch.setEnabled(true);
    } else {
      jMenuExit.setEnabled(false);
      jMenuSearch.setEnabled(false);
    }
  } //availableComponentClick
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="askRemoveSpecies(species)">
  private boolean askRemoveSpecies(String species) {
    if(species == null || species.length() <= 0) {return true;}
    if(pc.dbg) {System.out.println("askRemoveSpecies \""+species+"\"");}
    Object[] opt = {"Yes", "Cancel"};
    int m = javax.swing.JOptionPane.showOptionDialog(this,
                "Remove \""+species+"\" ?",
                pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
    if(m == javax.swing.JOptionPane.YES_OPTION) {
        String msg = "Please note:"+nl;
        if(Util.isProton(species)) {
            msg = msg +
                "Hydrogen ions are always present in aqueous solutions ..."+nl+
                "Removing \"H+\" will probably result in wrong diagrams !";
        } else if(species.equals("OH-")) {
            msg = msg +
                "Hydroxide ions are always present in aqueous solutions ..."+nl+
                "Removing \"OH-\" will probably result in wrong diagrams !";
        } else {
            msg = msg + "Removing a species may result in wrong diagrams !";
        }
        msg = msg +nl+nl+ "Remove \""+species+"\" anyway?";
        m = javax.swing.JOptionPane.showOptionDialog(this, msg,
                pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.ERROR_MESSAGE, null, opt, opt[1]);
        return m == javax.swing.JOptionPane.YES_OPTION;
    } else {return false;}
  } //askRemoveSpecies(species)
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="complexClick">
  private void complexClick() {
    int index = jListComplexes.getSelectedIndex();
    complexClick(index);
  }
  private void complexClick(int index) {
    if(index <0 || index >= modelComplexes.size()) {return;}
    if(pc.dbg) {System.out.println("complexClick index="+index);}
    String species = modelComplexes.get(index).toString();
    if(!askRemoveSpecies(species)) {return;}
    if(Util.isSolid(species)) {srch.nf--;} else {srch.nx--;}
    modelComplexes.remove(index);
    srch.dat.remove(index);
    //jListComplexes.clearSelection();
    //jMenuExit.setText("Search and Exit");
    jMenuExit.setEnabled(true);
    jMenuSearch.setEnabled(true);
    if(modelComplexes.size() <=0) {
        jScrollPaneComplexes.setVisible(false);
        jLabel_cr_solids.setText(" ");
        jLabelComplexes.setVisible(false);
        setTextLabelRight(" ");
    }
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="componentClick">
  private void componentClick() {
    int index = jListSelectedComps.getSelectedIndex();
    componentClick(index);
  }
  private void componentClick(int index) {
    if(index <0 || index >= modelSelectedComps.size()) {return;}
    String species = modelSelectedComps.get(index).toString(); 
    if(pc.dbg) {System.out.println("componentClick index="+index+" species = \""+species+"\"");}
    if(Util.isProton(species)) {
        if(!askRemoveSpecies(species)) {return;}
    } // H+?
    if(jListComplexes.isShowing()) { //clear any search
        doneSomeWork = true;
        modelComplexes.clear();
        jScrollPaneComplexes.setVisible(false);
        lastReactionInFocus = -1;
        jLabel_cr_solids.setText(" ");
        jLabelComplexes.setVisible(false);
        setTextLabelRight(" ");
    }
    if(Util.isSolid(species)) {solidSelectedComps--;}
    modelSelectedComps.remove(index);
    componentsNames.remove(index);
    jListSelectedComps.clearSelection();
    jMenuExit.setText("Search and Exit");
    if(modelSelectedComps.size() >0) {
      jMenuExit.setEnabled(true);
      jMenuSearch.setEnabled(true);
    } else {
      jMenuExit.setEnabled(false);
      jMenuSearch.setEnabled(false);
    }
  } //componentClick()
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="disclaimerAccepted()">
  void disclaimerAccepted() {
    if(pc.dbg) {System.out.println("--- disclaimerAccepted()");}
    if(noDataBasesFound) {jMenuDBFiles.doClick();}
    LibDB.getElements(dbf, pc.dbg, pd.dataBasesList, pd.elemComp);
    pd.foundH2O = false;
    for(String[] elemComp : pd.elemComp) {
        if (Util.isWater(elemComp[1])) {
            pd.foundH2O = true; break;
        }
    }

    setupFrame();

    jMenuH.setEnabled(true);
    jMenuOptions.setEnabled(true);
    pd.references = new References();
    String r;
    String dir = pc.pathAPP;
    if(dir != null) {
        if(dir.endsWith(SLASH)) {dir = dir.substring(0, dir.length()-1);}
        r = dir + SLASH + "References.txt";
    } else {r = "References.txt";}
    if(!pd.references.readRefsFile(r, pc.dbg)) {pd.references = null;}

    jCheckBoxMenuVerbose.setSelected(pc.dbg);
    jMenuAdvanced.setEnabled(advancedMenu);
    jMenuAdvanced.setVisible(advancedMenu);
    if(advancedMenu) {
        java.io.File f = new java.io.File(dir+SLASH+"DataMaintenance.jar");
        jMenuMaintenance.setEnabled(f.exists());
        f = new java.io.File(dir+SLASH+"AddShowReferences.jar");
        jMenuRefs.setEnabled(f.exists());
    }
    jMenuAdvSolids.setEnabled(advancedMenu || pd.allSolidsAsk || pd.allSolids != 0);
    jMenuAdvSolids.setVisible(advancedMenu || pd.allSolidsAsk || pd.allSolids != 0);
    jMenuAdvRedox.setEnabled(advancedMenu || pd.redoxAsk || !pd.redoxN || !pd.redoxP || !pd.redoxS);
    jMenuAdvRedox.setVisible(advancedMenu || pd.redoxAsk || !pd.redoxN || !pd.redoxP || !pd.redoxS);
    
    if(pc.dbg) {System.out.println("--- disclaimerAccepted() end.");}
    //---- deal with command-line arguments
    if(commandArgs != null && commandArgs.length > 0){
        if(pc.dbg) {System.out.println("--- Dealing with command line arguments");}
        Thread dArg = new Thread() {@Override public void run(){
            for(String commandArg : commandArgs) {dispatchArg(commandArg);}
            // add path to output file name if needed
            if(outputDataFile != null && !outputDataFile.contains(SLASH)) {
                String dir = pc.pathDef.toString();
                if(dir.endsWith(SLASH)) {dir = dir.substring(0, dir.length()-1);}
                java.io.File f = new java.io.File(dir + SLASH + outputDataFile);
                outputDataFile = f.getAbsolutePath();
            }
        }}; //new Thread
        dArg.start();
    } // if args != null
    setCursorDef();
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="dispatchArg(String)">
    /** Execute the command-line arguments (one by one)
     * @param arg String containing a command-line argument */
    public void dispatchArg(String arg) {
        if(arg == null || arg.trim().length() <=0) {return;}
        //these are handled in "main"
        System.out.println("Command-line argument = "+arg);
        if(arg.equals("-dbg") || arg.equals("/dbg")) {return;}
        if(arg.equals("-?") || arg.equals("/?") || arg.equals("?")
                || arg.equals("-help") || arg.equals("--help")) {
            printInstructions();
            msgFrame.setVisible(true);
            return;
        }
        if(arg.length() > 2 && arg.startsWith("\"") && arg.endsWith("\"")) {
            arg = arg.substring(1, arg.length());
        }
        if(arg.length() > 4 &&  arg.toLowerCase().endsWith(".dat")) {
            java.io.File datFile = new java.io.File(arg);
            if(datFile.getName().length() <= 4) {
                MsgExceptn.exception("Error: file name must have at least one character");
                return;
            }
            if(!arg.contains(SLASH)) {
                outputDataFile = arg;
                System.out.println("Default output file: \""+arg+"\"");
                return;
            }
            if(datFile.getParentFile() != null && !datFile.getParentFile().exists()) {
                String msg = "Error: \""+datFile.getAbsolutePath()+"\""+nl+
                       "    file directory does not exist.";
                MsgExceptn.exception(msg);
                return;
            }
            if(datFile.exists() && (!datFile.canWrite() || !datFile.setWritable(true))) {
                String msg = "Error - file is read-only:"+nl+"   \""+datFile.getAbsolutePath()+"\"";
                MsgExceptn.exception(msg);
                if(!this.isVisible()) {
                    MsgExceptn.showErrMsg(dbf, msg, 1);
                } else {
                    javax.swing.JOptionPane.showMessageDialog(this, msg, pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
                }
                return;
            }
            try{outputDataFile = datFile.getCanonicalPath();}
            catch(java.io.IOException ex) {
                try{outputDataFile = datFile.getAbsolutePath();}
                catch(Exception e) {outputDataFile = datFile.getPath();}
            }
            System.out.println("Default output file: \""+outputDataFile+"\"");
            return;
        } else { // does not end with ".dat"
            arg = arg.trim();
            java.io.File d = new java.io.File(arg);
            if(d.exists()) {
                pc.setPathDef(d);
                System.out.println("Default path: \""+pc.pathDef.toString()+"\"");
                return;
            } else if(arg.contains(SLASH)) {
                String msg = "Error: \""+d.getAbsolutePath()+"\""+nl+
                       "    directory does not exist.";
                MsgExceptn.exception(msg);
                return;
            }
        }
        String msg = "Error: bad format for"+nl+
                     "   command-line argument: \""+arg+"\"";
        System.out.println(msg);
        printInstructions();
        msgFrame.setVisible(true);
        javax.swing.JOptionPane.showMessageDialog(this,msg,
                pc.progName,javax.swing.JOptionPane.ERROR_MESSAGE);
        setCursorWait();
        Thread hlp = new Thread() {@Override public void run(){
            String[] a = {"S_Batch_htm"};
            lib.huvud.RunProgr.runProgramInProcess(null,ProgramConf.HELP_JAR,a,false,dbf.pc.dbg,dbf.pc.pathAPP);
            try{Thread.sleep(2000);}   //show the "wait" cursor for 2 sec
            catch (InterruptedException e) {}
            setCursorDef();
        }};//new Thread
        hlp.start();
    } // dispatchArg(arg)
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="getAvailableComponents(i)">
  private void getAvailableComponents(int index) {
    if(index < 0 || index >= buttons.length) {return;}
    modelAvailableComps.clear();
    availableComponentsNames.clear();
    String element = LibDB.elementSymb[index];
    String[] elemComp;
    for(int i = 0; i < pd.elemComp.size(); i++) {
        elemComp = pd.elemComp.get(i);
        if(elemComp[0].equals(element)) {
            modelAvailableComps.addElement(elemComp[1]);
            availableComponentsNames.add(elemComp[2]);
        }
    }
    jLabelAvailableComp.setText("<html><u>A</u>vailable for "+element+":</html>");
  } //getAvailableComponents(i)
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="menuOptionsEnable">
  private void menuOptionsEnable(boolean b) {
    //jMenuSolids.setEnabled(b);
    jMenuLocate.setEnabled(b);
    jMenuData.setEnabled(b);
    if(pd.foundH2O) {jMenuAdvH2O.setEnabled(b);}
  } //menuOptionsEnable
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="printInstructions">
  public static void printInstructions() {
    System.out.flush();
    String msg = LINE+nl+"Possible commands are:"+nl+
    "  -dbg            (output debugging information to the messages window)"+nl+
    "  data-file-name  (create a data file, name must end with \".dat\","+nl+
    "                   it may contain a directory part)"+nl+
    "  directory-name  (create a data file in the given directory.  A file name"+nl+
    "                   will be requested from the user.  If a file name"+nl+
    "                   with a directory is also given, it takes precedence)"+nl+
    "Enclose file or path names with double quotes (\"\") it they contain blank space."+nl+
    "Example:   java -jar DataBase.jar \"projects\\Fe\\test 25.dat\""+nl+LINE;
    System.out.println(msg);
    System.out.flush();
  } //printInstructions()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="read-write INI file">

  //<editor-fold defaultstate="collapsed" desc="readIni()">
  /** Reads program settings saved when the program was previously closed.
   * Exceptions are reported both to the console (if there is one) and to a dialog.<br>
   * Reads the ini-file in:<ul>
   *   <li> the Application Path if found there.</ul>
   * If not found in the application path, or if the file is write-protected, then:<ul>
   *   <li> in %HomeDrive%%HomePath% if found there; if write-protected also
   *   <li> in %Home% if found there; if write-protected also
   *   <li> in the user's home directory (system dependent) if it is found there
   * otherwise: give a warning and create a new file.  Note: except for the
   * installation directory, the ini-file will be writen in a sub-folder
   * named "<code>.config\eq-diag</code>".
   * <p>
   * This method also saves the ini-file after reading it and after
   * checking its contents.  The file is written in the application path if
   * "saveIniFileToApplicationPathOnly" is <code>true</code>.  Otherwise,
   * if an ini-file was read and if it was not write-protected, then program
   * options are saved in that file on exit.  If no ini-file was found,
   * an ini file is created on the first non-write protected directory of
   * those listed above.  */
  private void readIni() {
    // start by getting the defaults (this is needed because the arrays must be initialised)
    iniDefaults();  // needed to initialise arrays etc.
    if(pc.dbg) {System.out.println("--- readIni() ---  reading ini-file(s)");}
    fileIni = null;
    java.io.File p = null, fileRead = null, fileINInotRO = null;
    boolean ok, readOk = false;
    //--- check the application path ---//
    if(pc.pathAPP == null || pc.pathAPP.trim().length() <=0) {
        if(pc.saveIniFileToApplicationPathOnly) {
            String name = "\"null\"" + SLASH + FileINI_NAME;
            MsgExceptn.exception("Error: can not read ini file"+nl+
                        "    "+name+nl+
                        "    (application path is \"null\")");
            return;
        }
    } else { //pathApp is defined
        String dir = pc.pathAPP;
        if(dir.endsWith(SLASH)) {dir = dir.substring(0, dir.length()-1);}
        fileIni = new java.io.File(dir + SLASH + FileINI_NAME);
        p = new java.io.File(dir);
        if(!p.exists()) {
            p = null; fileIni = null;
            if(pc.saveIniFileToApplicationPathOnly) {
                MsgExceptn.exception("Error: can not read ini file:"+nl+
                            "    "+fileIni.getPath()+nl+
                            "    (application path does not exist)");
                return;
            }
        }
    }
    success: {
        // --- first read the ini-file from the application path, if possible
        if(pc.saveIniFileToApplicationPathOnly && fileIni != null) {
            // If the ini-file must be written to the application path,
            // then try to read this file, even if the file is write-protected
            fileINInotRO = fileIni;
            if(fileIni.exists()) {
                readOk = readIni2(fileIni, false);
                if(readOk) {fileRead = fileIni;}
            }
            break success;
        } else { // not saveIniFileToApplicationPathOnly or fileINI does not exist
            if(fileIni != null && fileIni.exists()) {
                readOk = readIni2(fileIni, false);
                if(readOk) {fileRead = fileIni;}
                if(fileIni.canWrite() && fileIni.setWritable(true)) {
                    fileINInotRO = fileIni;
                    if(readOk) {break success;}
                }
            } else { //ini-file null or does not exist
                if(fileIni != null && p != null) {
                    try{ // can we can write to this directory?
                            java.io.File tmp = java.io.File.createTempFile("datab",".tmp", p);
                            ok = tmp.exists();
                            if(ok) {tmp.delete();}
                    } catch (java.io.IOException ex) {ok = false;}
                    if(pc.dbg) {
                        String s; if(ok) {s="";} else {s="NOT ";}
                        System.out.println("   can "+s+"write files to path: "+p.getAbsolutePath());
                    }
                    // file does not exist, but the path is not write-protected
                    if(ok && fileINInotRO == null) {fileINInotRO = fileIni;}
                }
            }
        }
        // --- an ini-file has not been read in the application path
        //     and saveIniFileToApplicationPathOnly = false.  Read the ini-file from
        //     the user's path, if possible
        java.util.ArrayList<String> dirs = new java.util.ArrayList<String>(5);
        String homeDrv = System.getenv("HOMEDRIVE");
        String homePath = System.getenv("HOMEPATH");
        if(homePath != null && homePath.trim().length() >0 && !homePath.startsWith(SLASH)) {
            homePath = SLASH + homePath;
        }
        if(homeDrv != null && homeDrv.trim().length() >0 && homeDrv.endsWith(SLASH)) {
            homeDrv = homeDrv.substring(0, homeDrv.length()-1);
        }
        if((homeDrv != null && homeDrv.trim().length() >0)
                && (homePath != null && homePath.trim().length() >0)) {
            p = new java.io.File(homeDrv+homePath);
            if(p.exists()) {dirs.add(p.getAbsolutePath());}
        }
        String home = System.getenv("HOME");
        if(home != null && home.trim().length() >0) {
            p = new java.io.File(home);
            if(p.exists()) {dirs.add(p.getAbsolutePath());}
        }
        home = System.getProperty("user.home");
        if(home != null && home.trim().length() >0) {
            p = new java.io.File(home);
            if(p.exists()) {dirs.add(p.getAbsolutePath());}
        }        
        for(String t : dirs) {
            if(t.endsWith(SLASH)) {t = t.substring(0, t.length()-1);}
            fileIniUser = new java.io.File(t+SLASH+".config"+SLASH+"eq-diagr"+SLASH+FileINI_NAME);
            if(fileIniUser.exists()) {
                readOk = readIni2(fileIniUser, true);
                if(readOk) {fileRead = fileIniUser;}
                if(fileIniUser.canWrite() && fileIniUser.setWritable(true)) {
                    if(fileINInotRO == null) {fileINInotRO = fileIniUser;}
                    if(readOk) {break success;}
                }
            } else { //ini-file does not exist
                try{ // can we can write to this directory?
                    p =  new java.io.File(t);
                    java.io.File tmp = java.io.File.createTempFile("datab",".tmp", p);
                    ok = tmp.exists();
                    if(ok) {tmp.delete();}
                } catch (java.io.IOException ex) {ok = false;}
                if(pc.dbg) {
                    String s; if(ok) {s="";} else {s="NOT ";}
                    System.out.println("   can "+s+"write files to path: "+t);
                }
                // file does not exist, but the path is not write-protected
                if(ok && fileINInotRO == null) {fileINInotRO = fileIniUser;}
            }
        } // for(dirs)
    } //--- success?

    if(pc.dbg) {
        String s;
        if(fileINInotRO != null) {s=fileINInotRO.getAbsolutePath();} else {s="\"null\"";}
        System.out.println("   fileINInotRO = "+s);
        if(fileRead != null) {s=fileRead.getAbsolutePath();} else {s="\"null\"";}        
        System.out.println("   fileRead = "+s);
    }
    if(!readOk) {
        String msg = "Failed to read any INI-file."+nl+
            "Default program settings will be used.";
        MsgExceptn.showErrMsg(dbf, msg, 1);
        int nbr = pd.dataBasesList.size();
        if(nbr <=0) {
            msg = "Error:  no databases found";
            msg = msg + nl+nl+
                  "Please select a database using"+nl+
                  "menu \"Options / Data / Database files\""+nl+" ";
            MsgExceptn.showErrMsg(dbf, msg, 1);
            noDataBasesFound = true;
        }
    }
    if(fileINInotRO != null && fileINInotRO != fileRead) {
        ok = saveIni(fileINInotRO);
        if(ok) {
            if(fileIni != fileINInotRO) {fileIniUser = fileINInotRO;} else {fileIniUser = null;}
        } else {fileIniUser = null;}
    }
  } // readIni()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="readIni2">
  /**
   * @param f the INI-file
   * @param userFile if true databases are added to the list (the list is not
   * cleared first) and some options, such as "all solids" are not read
   * @return true if ok   */
  private boolean readIni2(java.io.File f, boolean userFile) {
    System.out.flush();
    String msg = "Reading ";
    if(userFile) {msg = msg + "user ";}
    msg = msg + "ini-file: \""+f.getPath()+"\"";
    System.out.println(msg);
    java.util.Properties propertiesIni = new java.util.Properties();
    java.io.FileInputStream properties_iniFile = null;
    boolean ok = true;
    try {
      properties_iniFile = new java.io.FileInputStream(f);
      propertiesIni.load(properties_iniFile);
      //throw new Exception("Test error");
    } //try
    catch (java.io.FileNotFoundException e) {
      System.out.println("Warning: file not found: \""+f.getPath()+"\""+nl+
                         "using default parameter values.");
      checkIniValues();
      ok = false;
    } //catch FileNotFoundException
    catch (java.io.IOException e) {
      MsgExceptn.exception(Util.stack2string(e));
      msg = "Error: \""+e.toString()+"\""+nl+
                   "   while loading INI-file:"+nl+
                   "   \""+f.getPath()+"\"";
      MsgExceptn.showErrMsg(dbf, msg, 1);
      ok = false;
    } // catch loading-exception
    finally {
        try {if(properties_iniFile != null) {properties_iniFile.close();}}
        catch (java.io.IOException e) {
            msg = "Error: \""+e.toString()+"\""+nl+
                          "   while closing INI-file:"+nl+
                          "   \""+f.getPath()+"\"";
            MsgExceptn.showErrMsg(dbf, msg, 1);
            javax.swing.JOptionPane.showMessageDialog(this, msg, pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    if(!ok) {return ok;}
    try{
        disclaimerSkip = Boolean.parseBoolean(propertiesIni.getProperty("skipDisclaimer"));
    } catch (NullPointerException e) {disclaimerSkip = false;}
    //--- hide the "disclaimer" window
    if(disclaimerSkip && disclaimerFrame != null) {disclaimerFrame.setVisible(false);}
    try {
        locationFrame.x = Integer.parseInt(propertiesIni.getProperty("location_left"));
        locationFrame.y = Integer.parseInt(propertiesIni.getProperty("location_top"));
        msgFrameSize.width = Integer.parseInt(propertiesIni.getProperty("msgFrame_width"));
        msgFrameSize.height = Integer.parseInt(propertiesIni.getProperty("msgFrame_height"));
        locationMsgFrame.x = Integer.parseInt(propertiesIni.getProperty("msgFrame_left"));
        locationMsgFrame.y = Integer.parseInt(propertiesIni.getProperty("msgFrame_top"));
        pd.addDataLocation.x = Integer.parseInt(propertiesIni.getProperty("addData_left"));
        pd.addDataLocation.y = Integer.parseInt(propertiesIni.getProperty("addData_top"));
        try{pd.allSolidsAsk = Boolean.parseBoolean(propertiesIni.getProperty("All_Solids_Ask"));}
        catch (NullPointerException e) {pd.allSolidsAsk = false;}
        try{pd.redoxAsk = Boolean.parseBoolean(propertiesIni.getProperty("Redox_Ask"));}
        catch (NullPointerException e) {pd.redoxAsk = false;}
        try{pd.temperatureAllowHigher = Boolean.parseBoolean(propertiesIni.getProperty("Temperature_Allow_Higher"));}
        catch (NullPointerException e) {pd.temperatureAllowHigher = false;}
        //if(!userFile) {
        //    // These settings are NOT read from the "user" ini-file.
        //    // If running from a CD or on a network server, the user may save these
        //    // but they will have to be re-setted on every run
            pd.allSolids = Integer.parseInt(propertiesIni.getProperty("All_Solids"));
            pd.redoxN = Boolean.parseBoolean(propertiesIni.getProperty("Redox_N"));
            pd.redoxS = Boolean.parseBoolean(propertiesIni.getProperty("Redox_S"));
            pd.redoxP = Boolean.parseBoolean(propertiesIni.getProperty("Redox_P"));
            pd.includeH2O = Boolean.parseBoolean(propertiesIni.getProperty("H2O"));
            pd.diagramProgr = propertiesIni.getProperty("diagramProgram");
            try{pd.temperature_C = Double.parseDouble(propertiesIni.getProperty("Temperature"));}
            catch (NullPointerException e) {pd.temperature_C = 25;}
            catch (NumberFormatException e) {pd.temperature_C = 25;
                System.out.println("Error reading temperature in \"ini\"-file; setting temperature = 25 C.");
            }
            try{pd.pressure_bar = Double.parseDouble(propertiesIni.getProperty("Pressure"));}
            catch (NullPointerException e) {pd.pressure_bar = 1;}
            catch (NumberFormatException e) {pd.pressure_bar = 1;
                System.out.println("Error reading pressure in \"ini\"-file; setting pressure = 1 bar.");
            }
            try{advancedMenu = Boolean.parseBoolean(propertiesIni.getProperty("advancedMenu"));}
            catch (NullPointerException e) {advancedMenu = false;}
        //} // if !userFile
        if(pc.pathDef.length() >0) {pc.pathDef.delete(0, pc.pathDef.length());}
        pc.pathDef.append(propertiesIni.getProperty("pathDefault"));
        if(pd.pathAddData.length() >0) {pd.pathAddData.delete(0, pd.pathAddData.length());}
        pd.pathAddData.append(propertiesIni.getProperty("pathAddData"));
        if(!userFile) {
            // If running from a CD or on a network server, a default database
            // is supplied to all users. The user's ini-file will not clear
            // the default database.
            pd.dataBasesList.clear();
        }
        int nbr = Integer.parseInt(propertiesIni.getProperty("DataBases_Nbr"));
        if(nbr > 0) {
            String dbName;
            for(int i=0; i < nbr; i++) {
                dbName = propertiesIni.getProperty("DataBase["+String.valueOf(i+1).trim()+"]");
                if(dbName != null && dbName.trim().length() >0) {pd.dataBasesList.add(dbName);}
            }
        }
    } catch (NumberFormatException e) {
        MsgExceptn.exception(Util.stack2string(e));
        msg = "Error: \""+e.toString()+"\""+nl+
                         "   while reading INI-file:"+nl+
                         "   \""+f.getPath()+"\""+nl+nl+
                         "Setting default program parameters.";
        MsgExceptn.showErrMsg(dbf, msg, 1);
        ok = false;
    }

    if(advancedMenu) {
        String s = propertiesIni.getProperty("lookAndFeel").toLowerCase();
        if(s.startsWith("system")) {laf = 1;}
        else if(s.startsWith("cross")) {laf = 2;}
        else {laf = 0;}
    } else {laf = 0;}
    if(ok && pc.dbg) {System.out.println("Finished reading ini-file");}
    System.out.flush();
    checkIniValues();
    return ok;
  } // readIni2()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="checkIniValues">
  private void checkIniValues() {
    System.out.flush();
    System.out.println(LINE+nl+"Checking ini-values.");
    if(locationFrame.x < -1 || locationFrame.y < -1) {
      locationFrame.x = Math.max(0, (LibDB.screenSize.width  - this.getWidth() ) / 2);
      locationFrame.y = Math.max(0, (LibDB.screenSize.height - this.getHeight() ) / 2);
    }
    // check Default Path
    java.io.File f = new java.io.File(pc.pathDef.toString());
    if(!f.exists()) {
        pc.setPathDef(); // set Default Path = User Directory
    }

    if(pd.pathAddData.length() >0) {
        f = new java.io.File(pd.pathAddData.toString());
        if(!f.exists()) {
            if(pd.pathAddData.length() >0) {pd.pathAddData.delete(0, pd.pathAddData.length());}
            pd.pathAddData.append(pc.pathDef.toString());
        }
    } else  {
        pd.pathAddData.append(pc.pathDef.toString());
    }

    if(pd.diagramProgr != null && pd.diagramProgr.trim().length()>0) {
        f = new java.io.File(pd.diagramProgr);
    } else {f = null;}
    if(f==null || !f.exists()) {
        String s = pd.diagramProgr;
        if(s==null) {s="null";}
        System.out.println("Warning: the diagram-making program:"+nl+
                "    \""+s+"\""+nl+
                "    in the INI-file does not exist.");
        getDiagramProgr(true);
    }

    if(System.getProperty("os.name").startsWith("Mac OS")) {
        String dir = pc.pathAPP;
        if(dir != null && dir.endsWith(SLASH)) {dir = dir.substring(0, dir.length()-1);}
        String s = dir +SLASH+".."+SLASH+".."+SLASH+".."+SLASH+".."+SLASH+"DataBase.app";
        f = new java.io.File(s);
        if(f.exists()) {
            System.out.println("Enabling menu \"Data / advanced\".");
            advancedMenu = true;
        }
    }

    String dir = pc.pathAPP;
    if(dir != null && dir.endsWith(SLASH)) {dir = dir.substring(0, dir.length()-1);}
    String dbName;
    int nbr = pd.dataBasesList.size();
    String msg = null;
    if(nbr <=0) {
        msg = "Warning:  no databases are given in the INI-file";
        System.out.println(msg);
        if(dir != null && dir.trim().length()>0) {dbName = dir + SLASH + DEF_DataBase;} else {dbName = DEF_DataBase;}
        if(LibDB.isDBnameOK(dbf, dbName, false)) {
            pd.dataBasesList.add(dbName);
            System.out.println("    Setting databse to "+dbName);
            msg = null;
        }
    } else {
        LibDB.checkListOfDataBases(dbf, pd.dataBasesList, pc.pathAPP, true);
        nbr = pd.dataBasesList.size();
        if(nbr <=0) {
            msg = "Error:  no database in the INI-file exist";
            if(dir != null && dir.trim().length()>0) {dbName = dir + SLASH + DEF_DataBase;} else {dbName = DEF_DataBase;}
            if(LibDB.isDBnameOK(dbf, dbName, false)) {
                pd.dataBasesList.add(dbName);
                System.out.println("    Setting databse to "+dbName);
                msg = null;
            }
        } // none of the databases exist.
    } // no databases in INI-file
    if(msg != null) {
        msg = msg + nl+nl+"Please select a database using"+nl+
                "menu \"Options / Data / Database files\""+nl+" ";
        MsgExceptn.showErrMsg(dbf, msg, 1);
        noDataBasesFound = true;
    }

    // diagramLocation, dispLocation and dispSize are checked
    //  each time these windows are loaded

    pd.allSolids = Math.min(3, Math.max(0, pd.allSolids));
    double maxT, maxP;
    if(pd.temperatureAllowHigher) {maxT = 600; maxP = 5000;} else {maxT = 100; maxP = 1.0142;}
    pd.temperature_C = Math.min(maxT, Math.max(-0.00001, pd.temperature_C));
    pd.pressure_bar = Math.min(maxP, Math.max(1, pd.pressure_bar));
    laf = Math.min(2,Math.max(0,laf));
    System.out.println(LINE);
    System.out.flush();
  } // checkIniValues()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="getDiagramProgr">
  private void getDiagramProgr(final boolean print) {
    String dir = pc.pathAPP;
    if(dir != null && dir.endsWith(SLASH)) {dir = dir.substring(0, dir.length()-1);}
    java.io.File f;
    if(dir == null || dir.trim().length()<=0) {
        pd.diagramProgr = "Spana.jar";
    } else {
        pd.diagramProgr = dir + SLASH + "Spana.jar";
        f = new java.io.File(pd.diagramProgr);
        if(!f.exists() && System.getProperty("os.name").startsWith("Mac OS")) {
          pd.diagramProgr = dir +SLASH+".."+SLASH+".."+SLASH+".."+SLASH+".."+SLASH+"Spana.app"
                  +SLASH+"Contents"+SLASH+"Resources"+SLASH+"Java"+SLASH+"Spana.jar";
          f = new java.io.File(pd.diagramProgr);
          try{pd.diagramProgr = f.getCanonicalPath();}
          catch (java.io.IOException ex) {pd.diagramProgr = f.getAbsolutePath();}
        }
    }
    f = new java.io.File(pd.diagramProgr);
    if(!f.exists()) {
        if(print) {System.out.println("Warning: could NOT find the diagram-making program: "+nl+
                "    "+pd.diagramProgr);}
        pd.diagramProgr = null;
    } else if(print) {System.out.println("Setting diagram-making program = "+nl+
                             "    "+pd.diagramProgr);}
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="iniDefaults">
  private void iniDefaults() {
      // Set default values for program variables
      if (pc.dbg) {
          System.out.flush();
          System.out.println("Setting default parameter values (\"ini\"-values).");
      }
      advancedMenu = false;
      pd.addDataLocation.x = -1000;   pd.addDataLocation.y = -1000;
      locationFrame.x = Math.max(0, (LibDB.screenSize.width  - this.getWidth() ) / 2);
      locationFrame.y = Math.max(0, (LibDB.screenSize.height - this.getHeight() ) / 2);
      msgFrameSize.width = 500; msgFrameSize.height = 400;
      locationMsgFrame.x = 80; locationMsgFrame.y = 28;
      // set the default path to the "current directory" (from where the program is started)
      pc.setPathDef(); // set Default Path = User Directory
      pd.temperatureAllowHigher = false;
      pd.temperature_C = 25;
      pd.pressure_bar = 1;
      pd.allSolidsAsk = false;
      // 0=include all solids; 1=exclude (cr); 2=exclude (c); 3=exclude (cr)&(c)
      pd.allSolids = 0;
      pd.redoxAsk = false;
      pd.redoxN = true;
      pd.redoxS = true;
      pd.redoxP = true;
      pd.includeH2O = false;
      if(pd.pathAddData.length() >0) {pd.pathAddData.delete(0, pd.pathAddData.length());}
      pd.pathAddData.append(System.getProperty("user.home"));

      getDiagramProgr(false);

      String dir = pc.pathAPP;
      if(dir != null && dir.endsWith(SLASH)) {dir = dir.substring(0, dir.length()-1);}
      pd.dataBasesList.clear();
      String dbName;
      if(dir != null && dir.trim().length()>0) {dbName = dir + SLASH + DEF_DataBase;} else {dbName = DEF_DataBase;}
      java.io.File db = new java.io.File(dbName);
      String msg = null;
      if(db.exists()) {
          if(db.canRead()) {pd.dataBasesList.add(dbName);}
          else {msg ="Error -- file \""+dbName+"\":"+nl+"   can not be read.";}
      }
      if(msg != null) {MsgExceptn.showErrMsg(dbf, msg, 1);}
    } // iniDefaults()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="saveIni(file)">
  /** Save program settings.
   * Exceptions are reported both to the console (if there is one) and to a dialog */
  private boolean saveIni(java.io.File f) {
    if(f == null) {return false;}
    if(pc.dbg) {System.out.println("--- saveIni("+f.getAbsolutePath()+")");}
    boolean ok = true;
    String msg = null;
    if(f.exists() && (!f.canWrite() || !f.setWritable(true))) {
        msg = "Error - can not write ini-file:"+nl+
              "   \""+f.getAbsolutePath()+"\""+nl+
              "The file is read-only.";
    }
    if(!f.exists() && !f.getParentFile().exists()) {
        ok = f.getParentFile().mkdirs();
        if(!ok) {
            msg = "Error - can not create directory:"+nl+
                  "   \""+f.getParent()+"\""+nl+
                  "Can not write ini-file.";
        }
    }
    if(msg != null) {
        MsgExceptn.showErrMsg(dbf, msg, 2);
        return false;
    }
    java.util.Properties propertiesIni= new SortedProperties();
    if(this != null && this.isVisible()
            && locationFrame.x > -1 && locationFrame.y > -1) {
        if(getExtendedState()==javax.swing.JFrame.ICONIFIED
           || getExtendedState()==javax.swing.JFrame.MAXIMIZED_BOTH)
                {setExtendedState(javax.swing.JFrame.NORMAL);}
        locationFrame.x = getX();
        locationFrame.y = getY();
    }
    propertiesIni.setProperty("<program_version>", VERS);
    if(pd.msgFrame != null) {msgFrameSize = pd.msgFrame.getSize(); locationMsgFrame = pd.msgFrame.getLocation();}
    propertiesIni.setProperty("msgFrame_width", String.valueOf(msgFrameSize.width));
    propertiesIni.setProperty("msgFrame_height", String.valueOf(msgFrameSize.height));
    propertiesIni.setProperty("msgFrame_left", String.valueOf(locationMsgFrame.x));
    propertiesIni.setProperty("msgFrame_top", String.valueOf(locationMsgFrame.y));
    propertiesIni.setProperty("location_left", String.valueOf(locationFrame.x));
    propertiesIni.setProperty("location_top", String.valueOf(locationFrame.y));
    propertiesIni.setProperty("addData_left", String.valueOf(pd.addDataLocation.x));
    propertiesIni.setProperty("addData_top", String.valueOf(pd.addDataLocation.y));
    propertiesIni.setProperty("All_Solids_Ask", String.valueOf(pd.allSolidsAsk));
    propertiesIni.setProperty("All_Solids", String.valueOf(pd.allSolids));
    propertiesIni.setProperty("Temperature_Allow_Higher", String.valueOf(pd.temperatureAllowHigher));
    propertiesIni.setProperty("Temperature", Util.formatNumAsInt(pd.temperature_C).trim());
    propertiesIni.setProperty("Pressure", Util.formatNumAsInt(pd.pressure_bar).trim());
    propertiesIni.setProperty("Redox_Ask", String.valueOf(pd.redoxAsk));
    propertiesIni.setProperty("Redox_N", String.valueOf(pd.redoxN));
    propertiesIni.setProperty("Redox_S", String.valueOf(pd.redoxS));
    propertiesIni.setProperty("Redox_P", String.valueOf(pd.redoxP));
    propertiesIni.setProperty("H2O", String.valueOf(pd.includeH2O));
    propertiesIni.setProperty("pathDefault", pc.pathDef.toString());
    propertiesIni.setProperty("pathAddData", pd.pathAddData.toString());
    propertiesIni.setProperty("skipDisclaimer", String.valueOf(disclaimerSkip));
    propertiesIni.setProperty("advancedMenu", String.valueOf(advancedMenu));
    if(laf==2) {propertiesIni.setProperty("lookAndFeel", "CrossPlatform (may be 'CrossPlatform', 'System' or 'Default')");}
    else if(laf==1) {propertiesIni.setProperty("lookAndFeel", "System (may be 'CrossPlatform', 'System' or 'Default')");}
    else {propertiesIni.setProperty("lookAndFeel", "Default (may be 'CrossPlatform', 'System' or 'Default')");}
    if(pd.diagramProgr != null) {
        propertiesIni.setProperty("diagramProgram", pd.diagramProgr);
    } else {propertiesIni.setProperty("diagramProgram", "");}
    int nbr = pd.dataBasesList.size();
    propertiesIni.setProperty("DataBases_Nbr", String.valueOf(nbr));
    for (int i = 0; i < nbr; i++) {
        propertiesIni.setProperty("DataBase["+String.valueOf(i+1).trim()+"]", pd.dataBasesList.get(i));
    }

    System.out.println("Saving ini-file: \""+f.getPath()+"\"");
    java.io.FileOutputStream propertiesIniFile = null;
    try{
        propertiesIniFile = new java.io.FileOutputStream(f);
        // INI-section "[DataBase]" needed by PortableApps java launcher
        int i = nl.length();
        byte[] b = new byte[10+i];
        b[0]='['; b[1]='D'; b[2]='a'; b[3]='t'; b[4]='a';
        b[5]='B'; b[6]='a'; b[7]='s'; b[8]='e'; b[9]=']';
        for(int j =0; j < i; j++) {b[10+j] = (byte)nl.codePointAt(j);}
        propertiesIniFile.write(b);
        //
        propertiesIni.store(propertiesIniFile,null);
        if (pc.dbg) {System.out.println("Written: \""+f.getPath()+"\"");}
    } catch (java.io.IOException e) {
          msg = "Error: \""+e.getMessage()+"\""+nl+
                "while writing INI-file:"+nl+
                "\""+f.getPath()+"\"";
          MsgExceptn.showErrMsg(dbf, msg, 1);
          ok = false;
    } // catch store-exception
    finally {
        try {if(propertiesIniFile != null) {propertiesIniFile.close();}}
        catch (java.io.IOException e) {}
    } //finally
    return ok;
  } // saveIni()
  //</editor-fold>

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="searchReactions(exit)">
  private void searchReactions(final boolean exit) {
    if(modelSelectedComps.size() <=0) {
        System.out.println("Error in \"searchReactions\": modelSelectedComps.size() <=0");
        return;
    }
    setCursorWait();

    final boolean needToSearch = (!exit || !jScrollPaneComplexes.isVisible() || modelComplexes.isEmpty());
    if(pc.dbg) {System.out.println("--- search reactions: needToSearch = "+needToSearch);}

    if(needToSearch) {
        boolean electronsSelected = false;
        for(int i=0; i < modelSelectedComps.size(); i++) {
            if(Util.isElectron(modelSelectedComps.get(i).toString())) {electronsSelected = true; break;}
        } //for i
        if(pd.redoxAsk && electronsSelected) {
            exitCancel = true;
            boolean askingBeforeSearch = true;
            AskRedox ask = new AskRedox(this, true, pc, pd, askingBeforeSearch, modelSelectedComps);
            if(ask.askSomething) {
                ask.start();
                // because ask is a modal dialog, next statement is executed when "ask" is closed by the user
                if(exitCancel) {setCursorDef(); return;}
            }
            ask.dispose();
        }
        if(pd.allSolidsAsk) {
            exitCancel = true;
            boolean askingBeforeSearch = true;
            AskSolids ask = new AskSolids(this, true, pc, pd, askingBeforeSearch);
            ask.start();
            // because ask is a modal dialog, next statement is executed when "ask" is closed by the user
            if(exitCancel) {setCursorDef(); return;}
            ask.dispose();
        }
        searchingComplexes = true;
        menuOptionsEnable(false);
        jMenuExit.setEnabled(false);
        jMenuSearch.setEnabled(false);
        jScrollPaneComplexes.setVisible(true);
        jLabel_cr_solids.setText(" ");
        jLabelComplexes.setVisible(true);
        java.awt.CardLayout cl = (java.awt.CardLayout)this.jPanelBottom.getLayout();
        cl.show(this.jPanelBottom,"cardProgress");
        updateProgressBar(0);
        updateProgressBarLabel(" ", 0);
        modelComplexes.clear();
    }

    javax.swing.SwingWorker srchWorker; 
      srchWorker = new javax.swing.SwingWorker<Boolean, Void>() {
          private boolean searchError;
          private boolean temperatureCorrectionsPossible;
          @Override
          public Boolean doInBackground() {
              //do we need to search the databases?
              if(!needToSearch) {
                  setCursorDef();
                  return true;
              } else {
                  searchError = false;
                  if(pc.dbg) {System.out.println("---- new search");}
                  
                  try{
                      srch = new DBSearch(pc,pd);
                      setCursorDef();
                      // the results of the search are stored in "srch.dat"
                      srch.searchComplexes(dbf);
                      javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
                          jMenuExit.setText("Exit");
                          jMenuExit.setEnabled(true);
                          jMenuSearch.setEnabled(true);
                      }}); //invokeLater(Runnable)
                  }
                  catch (DBSearch.SearchException ex) {
                      MsgExceptn.showErrMsg(dbf, ex.getMessage(), 1);
                      searchError = true;
                      srch = null;
                      return false; //this will go to finally
                  }
                  finally {
                      javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
                          menuOptionsEnable(true);
                          jMenuExit.setEnabled(true);
                          java.awt.CardLayout cl = (java.awt.CardLayout)dbf.jPanelBottom.getLayout();
                          cl.show(dbf.jPanelBottom,"cardLower");
                          //jListComplexes.requestFocusInWindow();
                      }}); //invokeLater(Runnable)
                      searchingComplexes = false;
                  }
                  temperatureCorrectionsPossible = true;
                  if(!searchError) {
                      for (Complex dat : srch.dat) {modelComplexes.addElement(dat.name);}
                      // ---
                      // ---  Temperature corrections?
                      // ---
                      temperatureCorrectionsPossible = DBSearch.checkTemperature(srch, dbf, true);
                  } // searchError?
                  if(searchError || !temperatureCorrectionsPossible) {
                      final boolean se = searchError;
                      //javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
                      jMenuExit.setEnabled(true);
                      jMenuSearch.setEnabled(true);
                      if(se) {
                          jMenuExit.setText("Search and Exit");
                          jScrollPaneComplexes.setVisible(false);
                          jLabel_cr_solids.setText(" ");
                          jLabelComplexes.setVisible(false);
                      } else {
                          jMenuExit.setText("Exit");
                      }
                      //}});
                      if(searchError) {
                          srch = null;
                          if(pc.dbg) {System.out.println("---- new search end with error.");}
                      }
                      setCursorDef();
                      return false;
                  }
                  if(pc.dbg) {System.out.println("---- new search end.");}
                  doneSomeWork = true;
              } //needToSearch
              return true;
          } // doInBackground()
          @Override protected void done() {
              if(needToSearch) {
                  if(isCancelled()) {
                      if(pc.dbg) {System.out.println("--- SwingWorker cancelled.");}
                      return;
                  }
                  if(searchError) {
                      if(pc.dbg) {System.out.println("--- Search error.");}
                      return;
                  }
                  if(!temperatureCorrectionsPossible) {
                      if(pc.dbg) {System.out.println("--- Temperature Corrections NOT Possible.");}
                      return;
                  }
              }
              if(!exit) {return;}
              if(pc.dbg) {System.out.println("--- exit ...");}
              exitCancel = true;
              send2Diagram = false;
              ExitDialog exitDialog = new ExitDialog(dbf, true, pc, pd, srch);
              if(exitCancel) {
                  if(pc.dbg) {System.out.println("--- Exit cancelled");}
                  return;
              }
              if(send2Diagram) {
                  if(pd.diagramProgr == null) {
                      MsgExceptn.exception("Error \"pd.diagramProgr\" is null."); return;
                  }
                  if(outputDataFile == null || outputDataFile.length() <=0) {
                      System.out.println("   outputDataFile name is empty!?");
                      return;
                  }
                  if(pc.dbg) {System.out.println("Sending file to the diagram-making program");}
                  
                  final String diagramProg = LibDB.getDiagramProgr(pd.diagramProgr);
                  if(diagramProg == null || diagramProg.trim().length()<=0) {
                      String t = "Error: could not find the diagram-making program:"+nl;
                      if(pd.diagramProgr != null) {t = t+"    \""+pd.diagramProgr+"\"";}
                      else {t = t +"    \"null\"";}
                      MsgExceptn.exception(t);
                      javax.swing.JOptionPane.showMessageDialog(dbf, t, pc.progName,javax.swing.JOptionPane.ERROR_MESSAGE);
                      return;
                  }
                  setCursorWait();
                  Thread hlp = new Thread() {@Override public void run(){
                      String[] argsDiagram = new String[]{"\""+outputDataFile+"\""};
                      boolean waitForCompletion = false;
                      lib.huvud.RunProgr.runProgramInProcess(null,diagramProg,argsDiagram,waitForCompletion,pc.dbg,pc.pathAPP);
                      try{Thread.sleep(2000);}   //show the "wait" cursor for 2 sec
                      catch (InterruptedException e) {}
                      setCursorDef();
                  }};//new Thread
                  hlp.start();
                  
              } //if send2Diagram
              doneSomeWork = false;
              if(pc.dbg) {System.out.println("--- SwingWorker done.");}
              end_program();
          } // done()
      };
    srchWorker.execute();
  } //searchReactions(exit)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="setCursorWait and setCursorDef">
  private void setCursorWait() {
    if(this != null) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
            setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        }});
    }
    if(pd.msgFrame != null && pd.msgFrame.isShowing()) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
            pd.msgFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        }});
    }
  }
  protected void setCursorDef() {
    if(this != null) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
            setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }});
    }
    if(pd.msgFrame != null && pd.msgFrame.isShowing()) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
            pd.msgFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }});
    }
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="setTextLabelRight(msg)">
 /** Sets and resizes jLabelRight with a text string
  * @param msg
  */
  private void setTextLabelRight(String msg) {
    if(msg == null || msg.trim().length() <=0) {msg = " ";}
    jLabelRight.setText(msg);
    int w = jLabelRight.getWidth()+1;
    if((labelRightX0 + w) > panelLowerLabelsW0) {
        jLabelRight.setLocation(Math.max(0,(panelLowerLabelsW0 - w)), 1);
    } else {
        jLabelRight.setLocation(labelRightX0, 1);
    }
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="setupFrame">
  /** restore the main frame after executing "getElements" */
  private void setupFrame() {
    java.awt.Font fN = buttonFont.deriveFont(java.awt.Font.PLAIN);
    java.awt.Font fB = buttonFont.deriveFont(java.awt.Font.BOLD);
    String[] elemComp;
    int jN = pd.elemComp.size();
    boolean found;
    for(javax.swing.JButton button : this.buttons) {
        found = false;
        for(int j = 0; j<jN; j++) {
            elemComp = pd.elemComp.get(j);
            if(button.getText().equals(elemComp[0])) {
                found = true;
                break;
            }
        } //for j
        if(found) {
            button.setEnabled(true);
            button.setFocusable(true);
            button.setFont(fB);
            button.setBackground(buttonBackgroundB);
        } else {
            button.setEnabled(false);
            button.setFocusable(false);
            button.setFont(fN);
            button.setBackground(buttonBackgroundN);
        }
    } //for button
    if(pd.foundH2O) {jMenuAdvH2O.setEnabled(true);} else {jMenuAdvH2O.setEnabled(false);}
    buttons[1].doClick(); //click on "H+" to select it as a component
    availableComponentClick(0);
    jLabelComplexes.setVisible(false);
    jScrollPaneComplexes.setVisible(false);
    jLabel_cr_solids.setText(" ");
    setTextLabelRight(" ");
    originalJLabelLeft.replace(0, originalJLabelLeft.length(), " ");
    jLabelLeft.setText(" ");
    jLabelMid.setText(" ");
    doneSomeWork = false;
  } //setupFrame()
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="updateProgressBar">
  void updateProgressBar(final int newValue) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
        jProgressBar.setValue(newValue);
    }}); //invokeLater(Runnable)
  }
  // </editor-fold>
  //<editor-fold defaultstate="collapsed" desc="updateProgressBarLabel">
  void updateProgressBarLabel(final String newTxt, final int nLoop) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
      if(nLoop <= 1) {
          jLabelNowLoop.setText(" ");
          jLabelNowLoop.setVisible(false);
      } else {
          jLabelNowLoop.setText("Redox loop number "+nLoop);
          jLabelNowLoop.setVisible(true);
      }
      if(newTxt == null || newTxt.length() <=0) {jLabelNow.setText(" ");} else {jLabelNow.setText(newTxt);}
    }}); //invokeLater(Runnable)
  }
  // </editor-fold>

//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="main">
  /** Starts DataBase. If another instance is running, send the command
   * arguments to the other instance and quit, otherwise, start a DataBase-frame.
   * @param args the command line arguments
   */
  public static void main(final String[] args) {
    System.out.println("DataBase - version "+VERS);
    final Splash spl = new Splash(0);
    //---- deal with some command-line arguments
    boolean dbg = false;
    if(args.length > 0) {
        for(String arg : args) {
            if (arg.equalsIgnoreCase("-dbg") || arg.equalsIgnoreCase("/dbg")) {
                System.out.println("Command-line argument = \"" + arg + "\"");
                dbg = true;
            }
            if (arg.equals("-?") || arg.equals("/?") || arg.equals("?")
                    || arg.equals("-help") || arg.equals("--help")) {
                System.out.println("Command-line argument = \"" + arg + "\"");
                printInstructions();
            } //if args[] = "?"              
        }
    }

    //---- is there another instance already running?
    if((new OneInstance()).findOtherInstance(args, 56050, "DataBase", dbg)) {
        System.out.println("---- Already running.");
        spl.setVisible(false);
        spl.dispose();
        return;
    }

    //---- create a local instance of ProgramConf.
    //     Contains information read from the configuration file.
    final ProgramConf pc = new ProgramConf("DataBase");
    pc.dbg = dbg;

    //---- all output to System.err and System.out will be shown in a frame.
    if(msgFrame == null) {
      msgFrame = new RedirectedFrame(550, 400, pc);
      msgFrame.setVisible(dbg);
      System.out.println("DataBase - version "+VERS);
    }

    if(System.getProperty("os.name").toLowerCase().startsWith("windows")) {windows = true;}

    //---- set Look-And-Feel
    //     laf = 0
    try{
        if(windows) {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            System.out.println("--- setLookAndFeel(System);");
        } else {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getCrossPlatformLookAndFeelClassName());
            System.out.println("--- setLookAndFeel(CrossPlatform);");
        }
    }
    catch (ClassNotFoundException ex) {System.out.println("Error: "+ex.getMessage());}
    catch (IllegalAccessException ex) {System.out.println("Error: "+ex.getMessage());}
    catch (InstantiationException ex) {System.out.println("Error: "+ex.getMessage());}
    catch (javax.swing.UnsupportedLookAndFeelException ex) {System.out.println("Error: "+ex.getMessage());}

    //---- for JOptionPanes set the default button to the one with the focus
    //     so that pressing "enter" behaves as expected:
    javax.swing.UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
    //     and make the arrow keys work:
    Util.configureOptionPane();

    //---- get the Application Path
    pc.pathAPP = Main.getPathApp();

    //---- read the CFG-file
    java.io.File fileNameCfg;
    String dir = pc.pathAPP;
    if(dir != null && dir.trim().length()>0) {
        if(dir.endsWith(SLASH)) {dir = dir.substring(0, dir.length()-1);}
        fileNameCfg = new java.io.File(dir + SLASH + pc.progName+".cfg");
    } else {fileNameCfg = new java.io.File(pc.progName+".cfg");}
    ProgramConf.read_cfgFile(fileNameCfg, pc);

    java.text.DateFormat dateFormatter =
            java.text.DateFormat.getDateTimeInstance
                (java.text.DateFormat.DEFAULT, java.text.DateFormat.DEFAULT,
                    java.util.Locale.getDefault());
    java.util.Date today = new java.util.Date();
    String dateOut = dateFormatter.format(today);
    System.out.println(pc.progName+" started: \""+dateOut+"\"");
    System.out.println(LINE);

    //---- set Default Path = Start Directory
    pc.setPathDef();

    commandArgs = args;

    //---- show the main window
    java.awt.EventQueue.invokeLater(new Runnable() {
        @Override public void run() {
          dbf = new FrameDBmain(pc, msgFrame);  //send configuration data
          if(!dbf.disclaimerSkip) {
              dbf.disclaimerFrame = new Disclaimer(dbf, pc, msgFrame);
          }
          //--- remove the "splash" window
          spl.setVisible(false);
          spl.dispose();
          dbf.start();
        } // run
    }); // invokeLater

  } //main(args)
  // </editor-fold>

  public static FrameDBmain getInstance() {return dbf;}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton0;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton100;
    private javax.swing.JButton jButton101;
    private javax.swing.JButton jButton102;
    private javax.swing.JButton jButton103;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton24;
    private javax.swing.JButton jButton25;
    private javax.swing.JButton jButton26;
    private javax.swing.JButton jButton27;
    private javax.swing.JButton jButton28;
    private javax.swing.JButton jButton29;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton30;
    private javax.swing.JButton jButton31;
    private javax.swing.JButton jButton32;
    private javax.swing.JButton jButton33;
    private javax.swing.JButton jButton34;
    private javax.swing.JButton jButton35;
    private javax.swing.JButton jButton36;
    private javax.swing.JButton jButton37;
    private javax.swing.JButton jButton38;
    private javax.swing.JButton jButton39;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton40;
    private javax.swing.JButton jButton41;
    private javax.swing.JButton jButton42;
    private javax.swing.JButton jButton43;
    private javax.swing.JButton jButton44;
    private javax.swing.JButton jButton45;
    private javax.swing.JButton jButton46;
    private javax.swing.JButton jButton47;
    private javax.swing.JButton jButton48;
    private javax.swing.JButton jButton49;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton50;
    private javax.swing.JButton jButton51;
    private javax.swing.JButton jButton52;
    private javax.swing.JButton jButton53;
    private javax.swing.JButton jButton54;
    private javax.swing.JButton jButton55;
    private javax.swing.JButton jButton56;
    private javax.swing.JButton jButton57;
    private javax.swing.JButton jButton58;
    private javax.swing.JButton jButton59;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton60;
    private javax.swing.JButton jButton61;
    private javax.swing.JButton jButton62;
    private javax.swing.JButton jButton63;
    private javax.swing.JButton jButton64;
    private javax.swing.JButton jButton65;
    private javax.swing.JButton jButton66;
    private javax.swing.JButton jButton67;
    private javax.swing.JButton jButton68;
    private javax.swing.JButton jButton69;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton70;
    private javax.swing.JButton jButton71;
    private javax.swing.JButton jButton72;
    private javax.swing.JButton jButton73;
    private javax.swing.JButton jButton74;
    private javax.swing.JButton jButton75;
    private javax.swing.JButton jButton76;
    private javax.swing.JButton jButton77;
    private javax.swing.JButton jButton78;
    private javax.swing.JButton jButton79;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton80;
    private javax.swing.JButton jButton81;
    private javax.swing.JButton jButton82;
    private javax.swing.JButton jButton83;
    private javax.swing.JButton jButton84;
    private javax.swing.JButton jButton85;
    private javax.swing.JButton jButton86;
    private javax.swing.JButton jButton87;
    private javax.swing.JButton jButton88;
    private javax.swing.JButton jButton89;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jButton90;
    private javax.swing.JButton jButton91;
    private javax.swing.JButton jButton92;
    private javax.swing.JButton jButton93;
    private javax.swing.JButton jButton94;
    private javax.swing.JButton jButton95;
    private javax.swing.JButton jButton96;
    private javax.swing.JButton jButton97;
    private javax.swing.JButton jButton98;
    private javax.swing.JButton jButton99;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuDebug;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuVerbose;
    private javax.swing.JLabel jLabelAvailableComp;
    private javax.swing.JLabel jLabelComplexes;
    private javax.swing.JLabel jLabelComps;
    private javax.swing.JLabel jLabelLeft;
    private javax.swing.JLabel jLabelMid;
    private javax.swing.JLabel jLabelNow;
    private javax.swing.JLabel jLabelNowLoop;
    private javax.swing.JLabel jLabelPressure;
    private javax.swing.JLabel jLabelRight;
    private javax.swing.JLabel jLabelTemperature;
    protected javax.swing.JLabel jLabel_cr_solids;
    private javax.swing.JList jListAvailableComps;
    private javax.swing.JList jListComplexes;
    private javax.swing.JList jListSelectedComps;
    private javax.swing.JMenuItem jMenuAddData;
    private javax.swing.JMenuItem jMenuAdvH2O;
    private javax.swing.JMenuItem jMenuAdvRedox;
    private javax.swing.JMenuItem jMenuAdvSolids;
    private javax.swing.JMenu jMenuAdvanced;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenuItem jMenuDBFiles;
    private javax.swing.JMenu jMenuData;
    private javax.swing.JMenuItem jMenuExit;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenu jMenuH;
    private javax.swing.JMenuItem jMenuHelp;
    private javax.swing.JMenuItem jMenuHelpAbout;
    private javax.swing.JMenuItem jMenuItemCancel;
    private javax.swing.JMenuItem jMenuItemData;
    private javax.swing.JMenuItem jMenuItemDel;
    private javax.swing.JMenuItem jMenuItemTemperature;
    private javax.swing.JMenuItem jMenuLocate;
    private javax.swing.JMenuItem jMenuMaintenance;
    private javax.swing.JMenu jMenuOptions;
    private javax.swing.JMenu jMenuPref;
    private javax.swing.JMenuItem jMenuQuit;
    private javax.swing.JMenuItem jMenuRefs;
    private javax.swing.JMenuItem jMenuSearch;
    private javax.swing.JMenuItem jMenuSingleC;
    private javax.swing.JPanel jPanelBottom;
    private javax.swing.JPanel jPanelLower;
    private javax.swing.JPanel jPanelLowerLabels;
    private javax.swing.JPanel jPanelPeriodicT;
    private javax.swing.JPanel jPanelProgressBar;
    private javax.swing.JPanel jPanelREEactinides;
    private javax.swing.JPanel jPanelTable;
    private javax.swing.JPopupMenu jPopupMenu;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JScrollPane jScrollPaneAvailableComps;
    private javax.swing.JScrollPane jScrollPaneComplexes;
    private javax.swing.JScrollPane jScrollPaneSelectedComps;
    private javax.swing.JPopupMenu.Separator jSeparator;
    // End of variables declaration//GEN-END:variables

}
