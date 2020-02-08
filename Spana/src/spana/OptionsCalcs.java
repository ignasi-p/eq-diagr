package spana;

import lib.huvud.ProgramConf;
import lib.kemi.chem.Chem;

/** Options dialog for chemical equilibrium calculations with Haltafall.
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
public class OptionsCalcs extends javax.swing.JFrame {
  // Note: for java 1.6 jComboBox must not have type,
  //       for java 1.7 jComboBox must be <String>
  private boolean finished = false;
  private java.awt.Dimension windowSize;
  private double tolHalta = Chem.TOL_HALTA_DEF;
  private ProgramDataSpana pd;
  private ProgramConf pc;
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
  private int calcDbgHalta = Chem.DBGHALTA_DEF;
  /** New-line character(s) to substitute "\n" */
  private static final String nl = System.getProperty("line.separator");

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /** Creates new form OptionsCalcs
   * @param pc0
   * @param pd0 */
  public OptionsCalcs(ProgramConf pc0, ProgramDataSpana pd0) {
    initComponents();
    this.pc = pc0;
    this.pd = pd0;
    finished = false;
    //center Window on Screen
    windowSize = this.getSize();
    int left; int top;
    left = Math.max(55, (MainFrame.screenSize.width  - windowSize.width ) / 2);
    top = Math.max(10, (MainFrame.screenSize.height - windowSize.height) / 2);
    this.setLocation(Math.min(MainFrame.screenSize.width-100, left),
                         Math.min(MainFrame.screenSize.height-100, top));
    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    //--- close window on ESC key
    javax.swing.KeyStroke escKeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(escKeyStroke,"ESCAPE");
    javax.swing.Action escAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            closeWindow();
        }};
    getRootPane().getActionMap().put("ESCAPE", escAction);
    //--- F1 for help
    javax.swing.KeyStroke f1KeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(f1KeyStroke,"F1");
    javax.swing.Action f1Action = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            OptionsCalcs.this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            Thread hlp = new Thread() {@Override public void run(){
                String[] a = {"S_Menu_Prefs_htm_Calcs"};
                lib.huvud.RunProgr.runProgramInProcess(OptionsCalcs.this,ProgramConf.HELP_JAR,a,false,pc.dbg,pc.pathAPP);
                try{Thread.sleep(2000);}   //show the "wait" cursor for 2 sec
                catch (InterruptedException e) {}
                OptionsCalcs.this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }};//new Thread
            hlp.start();
        }};
    getRootPane().getActionMap().put("F1", f1Action);
    //--- Alt-H help
    javax.swing.KeyStroke altHKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altHKeyStroke,"ALT_H");
    getRootPane().getActionMap().put("ALT_H", f1Action);
    //--- alt-X
    javax.swing.KeyStroke altXKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altXKeyStroke,"ALT_X");
    javax.swing.Action altXAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jButton_OK.doClick();
        }};
    getRootPane().getActionMap().put("ALT_X", altXAction);
    //--- alt-D
    javax.swing.KeyStroke altDKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altDKeyStroke,"ALT_D");
    javax.swing.Action altDAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jComboBoxDbgH.requestFocusInWindow();
        }};
    getRootPane().getActionMap().put("ALT_D", altDAction);
    //--- alt-T
    javax.swing.KeyStroke altTKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altTKeyStroke,"ALT_T");
    javax.swing.Action altTAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jComboBoxTol.requestFocusInWindow();
        }};
    getRootPane().getActionMap().put("ALT_T", altTAction);
    //--- alt-E
    javax.swing.KeyStroke altEKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altEKeyStroke,"ALT_E");
    javax.swing.Action altEAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jComboBoxExt.requestFocusInWindow();
        }};
    getRootPane().getActionMap().put("ALT_E", altEAction);
    //--- alt-C
    javax.swing.KeyStroke altCKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altCKeyStroke,"ALT_C");
    javax.swing.Action altCAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jComboBoxChar.requestFocusInWindow();
        }};
    getRootPane().getActionMap().put("ALT_C", altCAction);

    //
    //--- Title
    this.setTitle("Calculation Options:");
    //---- Icon
    String iconName = "images/Wrench_32x32.gif";
    java.net.URL imgURL = this.getClass().getResource(iconName);
    if (imgURL != null) {this.setIconImage(new javax.swing.ImageIcon(imgURL).getImage());}
    else {System.out.println("Error: Could not load image = \""+iconName+"\"");}

    // --- set up the frame
    if(pd.jarClassLd) {jRadioButtonLoadJars.setSelected(true);} else {jRadioButtonSeparate.setSelected(true);}
    jCheckBoxCalcsDbg.setSelected(pd.calcDbg);
    // store variables locally so the user can quit without saving
    // debugging in HaltaFall
    calcDbgHalta = Math.max(0, Math.min(jComboBoxDbgH.getItemCount()-1,pd.calcDbgHalta));
    jComboBoxDbgH.setSelectedIndex(calcDbgHalta);
    // tolerance in HaltaFall
    tolHalta = pd.tolHalta;
    set_tol_inComboBox();
    for(String tblExtension_type : pd.tblExtension_types) {
        jComboBoxExt.addItem(tblExtension_type);
    }
    boolean found = false;
    for(int i=0; i < jComboBoxExt.getItemCount(); i++) {
      if(jComboBoxExt.getItemAt(i).toString().equalsIgnoreCase(pd.tblExtension)) {
        jComboBoxExt.setSelectedIndex(i);
        found = true;
        break;
      }
    }
    if(!found) {jComboBoxExt.setSelectedIndex(0);}
    // character separating column output
    for(int i=0; i< pd.tblFieldSeparator_types.length; i++) {
      String t;
      if(pd.tblFieldSeparator_types[i] == ';') {t = "; (semicolon)";}
      else if(pd.tblFieldSeparator_types[i] == ',') {t = ", (comma)";}
      else if(pd.tblFieldSeparator_types[i] == ' ') {t = "  (space)";}
      else if(pd.tblFieldSeparator_types[i] == '\u0009') {t = "\\t (tab)";}
      else {t = "(error)";}
      jComboBoxChar.addItem(t);
    }
    found = false;
    String c;
    if(pd.tblFieldSeparator == '\u0009') {c = "\\t";}
    else {c = Character.toString(pd.tblFieldSeparator)+" ";}
    for(int i=0; i < jComboBoxChar.getItemCount(); i++) {
      if(jComboBoxChar.getItemAt(i).toString().substring(0,2).equalsIgnoreCase(c)) {
          jComboBoxChar.setSelectedIndex(i);
          found = true;
          break;
      }
    }
    if(!found) {jComboBoxChar.setSelectedIndex(0);}

    jComboBoxTol.setToolTipText("default: "+Chem.TOL_HALTA_DEF);
    jLabelTol.setToolTipText("default: "+Chem.TOL_HALTA_DEF);
    jLabel4.setToolTipText("default: 3%");
    jScrollBarMin.setToolTipText("default: 3%");
    
    jScrollBarMin.setValue(Math.round(pd.fractionThreshold*1000f));
    jScrollBarMinAdjustmentValueChanged(null);
    jScrollBarMin.setFocusable(true);

    jCheckBoxKeep.setSelected(pd.keepFrame);
    if(pd.tblCommentLineStart !=null && pd.tblCommentLineStart.length() >0) {
        jTextF_Start.setText(pd.tblCommentLineStart);
    } else {jTextF_Start.setText("");}
    if(pd.tblCommentLineEnd !=null && pd.tblCommentLineEnd.length() >0) {
        jTextF_End.setText(pd.tblCommentLineEnd);
    } else {jTextF_End.setText("");}
    checkBoxKeep();
  } //constructor
  //</editor-fold>

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jButton_OK = new javax.swing.JButton();
        jButton_Cancel = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jLabelTol = new javax.swing.JLabel();
        jComboBoxTol = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        jScrollBarMin = new javax.swing.JScrollBar();
        jLabelMinVal = new javax.swing.JLabel();
        jPanelTable = new javax.swing.JPanel();
        jLabelExt = new javax.swing.JLabel();
        jComboBoxExt = new javax.swing.JComboBox<>();
        jLabelChar = new javax.swing.JLabel();
        jComboBoxChar = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextF_End = new javax.swing.JTextField();
        jTextF_Start = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jRadioButtonLoadJars = new javax.swing.JRadioButton();
        jRadioButtonSeparate = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        jCheckBoxKeep = new javax.swing.JCheckBox();
        jCheckBoxCalcsDbg = new javax.swing.JCheckBox();
        jLabelDbgH = new javax.swing.JLabel();
        jComboBoxDbgH = new javax.swing.JComboBox<>();

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

        jButton_OK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/spana/images/OK_32x32.gif"))); // NOI18N
        jButton_OK.setMnemonic('O');
        jButton_OK.setText("OK");
        jButton_OK.setToolTipText("OK (Alt-O orAlt-X)"); // NOI18N
        jButton_OK.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton_OK.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton_OK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_OKActionPerformed(evt);
            }
        });

        jButton_Cancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/spana/images/Trash.gif"))); // NOI18N
        jButton_Cancel.setMnemonic('Q');
        jButton_Cancel.setText("Quit");
        jButton_Cancel.setToolTipText("Cancel (Esc or Alt-Q)"); // NOI18N
        jButton_Cancel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton_Cancel.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton_Cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_CancelActionPerformed(evt);
            }
        });

        jLabelTol.setLabelFor(jComboBoxTol);
        jLabelTol.setText("<html>Max. <u>t</u>olerance for mass-balance eqns. in HaltaFall:</html>"); // NOI18N
        jLabelTol.setToolTipText("default: 1E-5");

        jComboBoxTol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1E-2", "1E-3", "1E-4", "1E-5", "1E-6", "1E-7", "1E-8", "1E-9" }));
        jComboBoxTol.setToolTipText("default: 1E-4");

        jLabel4.setText("Fraction diagrams: threshold for curves:");
        jLabel4.setToolTipText("default: 3%");

        jScrollBarMin.setMinimum(1);
        jScrollBarMin.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        jScrollBarMin.setToolTipText("default: 3%");
        jScrollBarMin.setValue(0);
        jScrollBarMin.setVisibleAmount(0);
        jScrollBarMin.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jScrollBarMinFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jScrollBarMinFocusLost(evt);
            }
        });
        jScrollBarMin.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                jScrollBarMinAdjustmentValueChanged(evt);
            }
        });

        jLabelMinVal.setText("3 %");
        jLabelMinVal.setToolTipText("double-click to set default value");
        jLabelMinVal.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelMinValMouseClicked(evt);
            }
        });

        jPanelTable.setBorder(javax.swing.BorderFactory.createTitledBorder(null, " Table output in SED: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(51, 0, 255))); // NOI18N

        jLabelExt.setLabelFor(jComboBoxExt);
        jLabelExt.setText("<html>Name <u>e</u>xtension for output file:</html>"); // NOI18N

        jLabelChar.setLabelFor(jComboBoxChar);
        jLabelChar.setText("<html><u>C</u>olumn-separation character:</html>"); // NOI18N

        jLabel1.setText("Comment-lines:"); // NOI18N

        jLabel3.setLabelFor(jTextF_Start);
        jLabel3.setText("start characters:"); // NOI18N

        jLabel2.setLabelFor(jTextF_End);
        jLabel2.setText("end characters:"); // NOI18N

        jTextF_End.setText("\""); // NOI18N
        jTextF_End.setToolTipText("Text that will be appended at the end of any comment line. Default is double quote (\")"); // NOI18N
        jTextF_End.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextF_EndFocusGained(evt);
            }
        });

        jTextF_Start.setText("\""); // NOI18N
        jTextF_Start.setToolTipText("Text that will be inserted before any comment line. Default is double quote (\")"); // NOI18N
        jTextF_Start.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextF_StartFocusGained(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextF_End)
                    .addComponent(jTextF_Start))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextF_Start, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextF_End, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanelTableLayout = new javax.swing.GroupLayout(jPanelTable);
        jPanelTable.setLayout(jPanelTableLayout);
        jPanelTableLayout.setHorizontalGroup(
            jPanelTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTableLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabelExt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelChar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBoxChar, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxExt, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanelTableLayout.setVerticalGroup(
            jPanelTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTableLayout.createSequentialGroup()
                .addGroup(jPanelTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelExt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxExt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxChar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelChar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollBarMin, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelMinVal, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel10Layout.createSequentialGroup()
                                .addComponent(jLabelTol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBoxTol, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanelTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelTol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxTol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jScrollBarMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelMinVal))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, " Calculations: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(51, 0, 255))); // NOI18N

        buttonGroup1.add(jRadioButtonLoadJars);
        jRadioButtonLoadJars.setMnemonic('L');
        jRadioButtonLoadJars.setText("Load jar-files in Java Virtual machine");

        buttonGroup1.add(jRadioButtonSeparate);
        jRadioButtonSeparate.setMnemonic('R');
        jRadioButtonSeparate.setText("Run jar-files in separate system process");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButtonLoadJars)
                    .addComponent(jRadioButtonSeparate))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jRadioButtonLoadJars)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jRadioButtonSeparate))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, " Debugging: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(51, 0, 255))); // NOI18N

        jCheckBoxKeep.setMnemonic('K');
        jCheckBoxKeep.setText("Keep windows open after calculations"); // NOI18N
        jCheckBoxKeep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxKeepActionPerformed(evt);
            }
        });

        jCheckBoxCalcsDbg.setMnemonic('W');
        jCheckBoxCalcsDbg.setText("Write messages in SED & Predom"); // NOI18N

        jLabelDbgH.setLabelFor(jComboBoxDbgH);
        jLabelDbgH.setText("<html><u>D</u>ebugging in HaltaFall:</html>"); // NOI18N
        jLabelDbgH.setToolTipText("Default is 1 (show errors)");

        jComboBoxDbgH.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0 - none", "1 - errors only", "2 - errors + results", "3 - errors + results + input", "4 = 3 + output from Fasta", "5 = 4 + output from activity coeffs.", "6 = 5 + lots of output" }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBoxKeep)
                            .addComponent(jCheckBoxCalcsDbg)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBoxDbgH, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelDbgH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jCheckBoxKeep)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxCalcsDbg)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelDbgH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxDbgH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton_OK, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton_Cancel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(jButton_OK)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton_Cancel))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

  //<editor-fold defaultstate="collapsed" desc="Events">

    private void jButton_OKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_OKActionPerformed

        pd.jarClassLd = jRadioButtonLoadJars.isSelected();
        pd.keepFrame = jCheckBoxKeep.isSelected();
        pd.calcDbg = jCheckBoxCalcsDbg.isSelected();
        pd.calcDbgHalta = Integer.parseInt(jComboBoxDbgH.getSelectedItem().toString().substring(0,2).trim());
        String t = jComboBoxTol.getSelectedItem().toString();
        try{pd.tolHalta = Double.parseDouble(t);}
        catch (NumberFormatException ex) {
            System.err.println("Error: trying to read a number in \""+t+"\""+nl+"Setting max tolerance to "+(float)Chem.TOL_HALTA_DEF+" ...");
            pd.tolHalta = Chem.TOL_HALTA_DEF;
        }
        //file name extention for table output
        pd.tblExtension = jComboBoxExt.getSelectedItem().toString();
        //character separating column output
        t = jComboBoxChar.getSelectedItem().toString().substring(0,2);
        if(t.equalsIgnoreCase("\\t")) {pd.tblFieldSeparator = '\u0009';}
        else {pd.tblFieldSeparator = t.charAt(0);}
        t = jTextF_Start.getText();
        if(t !=null && t.length() >0) {pd.tblCommentLineStart = t;} else {pd.tblCommentLineStart = "";}
        t = jTextF_End.getText();
        if(t !=null && t.length() >0) {pd.tblCommentLineEnd = t;} else {pd.tblCommentLineEnd = "";}
        pd.fractionThreshold = (float)jScrollBarMin.getValue()/1000f;

        closeWindow();
}//GEN-LAST:event_jButton_OKActionPerformed

    private void jButton_CancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_CancelActionPerformed
        closeWindow();
}//GEN-LAST:event_jButton_CancelActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeWindow();
    }//GEN-LAST:event_formWindowClosing

    private void jCheckBoxKeepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxKeepActionPerformed
        checkBoxKeep();
    }//GEN-LAST:event_jCheckBoxKeepActionPerformed

    private void jTextF_StartFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextF_StartFocusGained
        jTextF_Start.selectAll();
    }//GEN-LAST:event_jTextF_StartFocusGained

    private void jTextF_EndFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextF_EndFocusGained
        jTextF_End.selectAll();
    }//GEN-LAST:event_jTextF_EndFocusGained

    private void jScrollBarMinAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_jScrollBarMinAdjustmentValueChanged
        jLabelMinVal.setText(String.valueOf((float) jScrollBarMin.getValue() / 10f) + "%");
    }//GEN-LAST:event_jScrollBarMinAdjustmentValueChanged

    private void jScrollBarMinFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScrollBarMinFocusGained
        jScrollBarMin.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    }//GEN-LAST:event_jScrollBarMinFocusGained

    private void jScrollBarMinFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScrollBarMinFocusLost
        jScrollBarMin.setBorder(null);
    }//GEN-LAST:event_jScrollBarMinFocusLost

    private void jLabelMinValMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelMinValMouseClicked
      if(evt.getClickCount() >1) {  // double-click
          jScrollBarMin.setValue(Math.round(0.03f*1000f));
      }
    }//GEN-LAST:event_jLabelMinValMouseClicked

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
  private void closeWindow() {
    finished = true;
    this.notify_All();
    this.dispose();
  } // closeWindow()
  private synchronized void notify_All() {notifyAll();}
  /** this method will wait for this dialog frame to be closed */
  public synchronized void waitFor() {
    while(!finished) {
        try {wait();} catch (InterruptedException ex) {}
    } // while
  } // waitFor()

  private void checkBoxKeep() {
      if(jCheckBoxKeep.isSelected()) {
        jCheckBoxCalcsDbg.setEnabled(true);
        jLabelDbgH.setEnabled(true);
        jLabelDbgH.setText("<html><u>D</u>ebugging in HaltaFall:</html>");
        jComboBoxDbgH.setEnabled(true);
      } else {
        jCheckBoxCalcsDbg.setEnabled(false);
        jLabelDbgH.setText("Debugging in HaltaFall:");
        jLabelDbgH.setEnabled(false);
        jComboBoxDbgH.setEnabled(false);
      }
  } //checkBoxKeep()

//<editor-fold defaultstate="collapsed" desc="set_tol_inComboBox">
/** find the closest item in the tolerances combo box and select it */
  private void set_tol_inComboBox() {
    double w0;
    double w1;
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

  //</editor-fold>

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton_Cancel;
    private javax.swing.JButton jButton_OK;
    private javax.swing.JCheckBox jCheckBoxCalcsDbg;
    private javax.swing.JCheckBox jCheckBoxKeep;
    private javax.swing.JComboBox<String> jComboBoxChar;
    private javax.swing.JComboBox<String> jComboBoxDbgH;
    private javax.swing.JComboBox<String> jComboBoxExt;
    private javax.swing.JComboBox<String> jComboBoxTol;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabelChar;
    private javax.swing.JLabel jLabelDbgH;
    private javax.swing.JLabel jLabelExt;
    private javax.swing.JLabel jLabelMinVal;
    private javax.swing.JLabel jLabelTol;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelTable;
    private javax.swing.JRadioButton jRadioButtonLoadJars;
    private javax.swing.JRadioButton jRadioButtonSeparate;
    private javax.swing.JScrollBar jScrollBarMin;
    private javax.swing.JTextField jTextF_End;
    private javax.swing.JTextField jTextF_Start;
    // End of variables declaration//GEN-END:variables

}
