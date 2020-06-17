package spana;

import lib.huvud.ProgramConf;
import lib.kemi.graph_lib.DiagrPaintUtility;

/** Options dialog for diagram windows.
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
public class OptionsDiagram extends javax.swing.JFrame {
    private DiagrPaintUtility diagrPaintUtil;
    private ProgramConf pc;
    private boolean finished = false;
    private java.awt.Dimension windowSize;
    // set values in the local variables, which will be discarded
    // if the user presses "Cancel"
    private java.awt.Color[] L_colours =
                            new java.awt.Color[DiagrPaintUtility.MAX_COLOURS];
    private javax.swing.border.Border scrollBorder;
    private javax.swing.border.Border buttonBorder;
    private final javax.swing.border.Border buttonBorderSelected = 
            javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED,
                new java.awt.Color(102,102,102),
                new java.awt.Color(255,255,255),
                new java.awt.Color(102,102,102),
                new java.awt.Color(0,0,0));
    private java.awt.Color L_backgrnd;
    /** New-line character(s) to substitute "\n". */
    private static final String nl = System.getProperty("line.separator");
    private final static java.text.NumberFormat nf =
            java.text.NumberFormat.getNumberInstance(java.util.Locale.ENGLISH);
    private static final java.text.DecimalFormat myFormatter = (java.text.DecimalFormat)nf;

    /** use "df" to write numbers using decimal point and without thousand separators:
     *    df.setGroupingUsed(false); // no commas to separate thousands
     *    outputFile.write(df.format(X)); */
    // static java.text.DecimalFormat df = (java.text.DecimalFormat)nf;
    //   alternatively use the "dfp" method
    //      outputFile.write(dfp("##0.0##",X));
    //      outputFile.write(dfp("##0.0##E#00",X));


  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /** Creates new form OptionsDiagram
   * @param diagPU
   * @param pc0  */
  public OptionsDiagram(DiagrPaintUtility diagPU,
                ProgramConf pc0) {
    initComponents();
    this.pc = pc0;
    this.diagrPaintUtil = diagPU;
    finished = false;
    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    //--- close window on ESC key
    javax.swing.KeyStroke escKeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(escKeyStroke,"ESCAPE");
    javax.swing.Action escAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jButton_Cancel.requestFocus();
            closeWindow();
        }};
    getRootPane().getActionMap().put("ESCAPE", escAction);
    //--- F1 for help
    javax.swing.KeyStroke f1KeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(f1KeyStroke,"F1");
    javax.swing.Action f1Action = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            OptionsDiagram.this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            Thread hlp = new Thread() {@Override public void run(){
                String[] a = {"S_Menu_Prefs_htm_Diagr"};
                lib.huvud.RunProgr.runProgramInProcess(OptionsDiagram.this,ProgramConf.HELP_JAR,a,false,pc.dbg,pc.pathAPP);
                try{Thread.sleep(2000);}   //show the "wait" cursor for 2 sec
                catch (InterruptedException e) {}
                OptionsDiagram.this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
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
    //--- Alt-B - set Background colour
    javax.swing.KeyStroke altBKeyStroke = javax.swing.KeyStroke.getKeyStroke(
						java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altBKeyStroke,"ALT_B");
    javax.swing.Action altBAction = new javax.swing.AbstractAction() {
         @Override public void actionPerformed(java.awt.event.ActionEvent e) {
             if(!jCheckBackgr.isSelected()) {return;}
             jPanelBackGrnd.requestFocus();
             changeBackgroundColour();
         }};
    getRootPane().getActionMap().put("ALT_B", altBAction);

    //
    getRootPane().setDefaultButton(jButton_OK);
    buttonBorder = jButton1.getBorder(); // get the default button border
    scrollBorder = jScrollBar_Pen.getBorder(); // get the default scroll bar border
    //--- Title
        this.setTitle("Graphic Options:");
    //--- Icon
    String iconName = "images/Wrench_32x32.gif";
    java.net.URL imgURL = this.getClass().getResource(iconName);
    if (imgURL != null) {this.setIconImage(new javax.swing.ImageIcon(imgURL).getImage());}
    else {System.out.println("Error: Could not load image = \""+iconName+"\"");}
    //
    jPanel_Fonts.setPreferredSize(new java.awt.Dimension(jPanel_Fonts.getSize()));
    // center Window on Screen
    windowSize = this.getSize();
    int left; int top;
    left = Math.max(0, (MainFrame.screenSize.width  - windowSize.width ) / 2);
    top = Math.max(0, (MainFrame.screenSize.height - windowSize.height) / 2);
    this.setLocation(Math.min(MainFrame.screenSize.width-100, left),
                     Math.min(MainFrame.screenSize.height-100, top));
    //
    System.arraycopy(diagrPaintUtil.colours, 0, L_colours, 0, DiagrPaintUtility.MAX_COLOURS);
    L_backgrnd = diagrPaintUtil.backgrnd;

    if(diagrPaintUtil.textWithFonts) {
        jRadioButton_FontPrinter.setSelected(true);
        jCombo_FontFamily.setVisible(true);
        jCombo_FontStyle.setVisible(true);
        jText_FontSize.setVisible(true);
        jLabelAntiAT.setVisible(true);
        jComboAntiAliasT.setVisible(true);
    } else {
        jRadioButton_FontNone.setSelected(true);
        jCombo_FontFamily.setVisible(false);
        jCombo_FontStyle.setVisible(false);
        jText_FontSize.setVisible(false);
        jLabelAntiAT.setVisible(false);
        jComboAntiAliasT.setVisible(false);
    }
    jCombo_FontFamily.setSelectedIndex(diagrPaintUtil.fontFamily);
    jCombo_FontStyle.setSelectedIndex(diagrPaintUtil.fontStyle);
    jText_FontSize.setText(String.valueOf(diagrPaintUtil.fontSize));
    jScrollBar_Pen.setValue(Math.round(diagrPaintUtil.penThickness*10f));
    jScrollBar_PenAdjustmentValueChanged(null);
    myFormatter.applyPattern("##0.#");
    jCheckBox_FixedSize.setText("Constant Size ("+
            myFormatter.format(diagrPaintUtil.fixedSizeWidth)+"x"+
            myFormatter.format(diagrPaintUtil.fixedSizeHeight)+")");
    jCheckBox_FixedSize.setSelected(diagrPaintUtil.fixedSize);
    jCheckBox_AspectRatio.setSelected(diagrPaintUtil.keepAspectRatio);
    jCheckBox_FixedSize.setVisible(!jCheckBox_AspectRatio.isSelected());
    jComboAntiAlias.setSelectedIndex(diagrPaintUtil.antiAliasing);
    jComboAntiAliasT.setSelectedIndex(diagrPaintUtil.antiAliasingText);
    jButton1.setBackground(diagrPaintUtil.colours[0]);
    jButton2.setBackground(diagrPaintUtil.colours[1]);
    jButton3.setBackground(diagrPaintUtil.colours[2]);
    jButton4.setBackground(diagrPaintUtil.colours[3]);
    jButton5.setBackground(diagrPaintUtil.colours[4]);
    jButton6.setBackground(diagrPaintUtil.colours[5]);
    jButton7.setBackground(diagrPaintUtil.colours[6]);
    jButton8.setBackground(diagrPaintUtil.colours[7]);
    jButton9.setBackground(diagrPaintUtil.colours[8]);
    jButton10.setBackground(diagrPaintUtil.colours[9]);
    jButton11.setBackground(diagrPaintUtil.colours[10]);
    jCheckBackgr.setSelected(diagrPaintUtil.useBackgrndColour);
    jCheckBoxPrintColour.setSelected(diagrPaintUtil.printColour);
    jCheckPrintHeader.setSelected(diagrPaintUtil.printHeader);
    jScrollBar_PenPrint.setValue(Math.round(diagrPaintUtil.printPenThickness*10f));
    java.awt.Dimension d = jPanelColours.getSize();
    jPanelColours.setPreferredSize(d);
    if (jCheckBackgr.isSelected()) {
        jPanelBackGrnd.setFocusable(true);
        jPanelBackGrnd.setBorder(
                     javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanelBackGrnd.setBackground(L_backgrnd);
        jPanelBackGrnd.setToolTipText("Alt-B");
    } else {
        jPanelBackGrnd.setBorder(null);
        jPanelBackGrnd.setBackground(java.awt.Color.white);
        jPanelBackGrnd.setToolTipText(null);
        jPanelBackGrnd.setFocusable(false);
    }
    switch (diagrPaintUtil.colourType) {
        case 1: // BW
            jRadioButtonBW.setSelected(true);
            jCheckBackgr.setVisible(false);
            jButton_ResetColours.setVisible(false);
            jPanelBackGrnd.setVisible(false);
            break;
        case 2: // WB
            jRadioButtonWB.setSelected(true);
            jCheckBackgr.setVisible(false);
            jButton_ResetColours.setVisible(false);
            jPanelBackGrnd.setVisible(false);
            break;
        default:
            jRadioButtonColour.setSelected(true);
            jCheckBackgr.setVisible(true);
            jButton_ResetColours.setVisible(true);
            jPanelBackGrnd.setVisible(true);
    } //switch
    jPanelScreenDisp.setPreferredSize(jPanelScreenDisp.getSize());
    jScrollBar_Pen.setFocusable(true);
    jScrollBar_PenPrint.setFocusable(true);
  } //constructor

  //</editor-fold>

/*    public static String dfp (String pattern, double value) {
        //java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance(Locale.ENGLISH);
        java.text.DecimalFormat myFormatter = (java.text.DecimalFormat)nf;
        myFormatter.setGroupingUsed(false);
        try {myFormatter.applyPattern(pattern);}
        catch (Exception ex) {System.err.println("Error: "+ex.getMessage()+nl+
                "  value="+value+" pattern = \""+pattern+"\"");}
        return myFormatter.format(value);
    }
*/


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup_FontPrinter = new javax.swing.ButtonGroup();
        buttonGroup_Colr = new javax.swing.ButtonGroup();
        jPanel4 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jButton_OK = new javax.swing.JButton();
        jButton_Cancel = new javax.swing.JButton();
        jPanelScreenDisp = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jCheckBox_AspectRatio = new javax.swing.JCheckBox();
        jCheckBox_FixedSize = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel_PenThickness = new javax.swing.JLabel();
        jScrollBar_Pen = new javax.swing.JScrollBar();
        jLabelAntiA1 = new javax.swing.JLabel();
        jComboAntiAlias = new javax.swing.JComboBox();
        jPanel_Fonts = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jRadioButton_FontNone = new javax.swing.JRadioButton();
        jRadioButton_FontPrinter = new javax.swing.JRadioButton();
        jCombo_FontFamily = new javax.swing.JComboBox();
        jCombo_FontStyle = new javax.swing.JComboBox();
        jText_FontSize = new javax.swing.JTextField();
        jLabelAntiAT = new javax.swing.JLabel();
        jComboAntiAliasT = new javax.swing.JComboBox();
        jPanelColours = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jRadioButtonColour = new javax.swing.JRadioButton();
        jRadioButtonBW = new javax.swing.JRadioButton();
        jRadioButtonWB = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        jCheckBackgr = new javax.swing.JCheckBox();
        jButton_ResetColours = new javax.swing.JButton();
        jPanelBackGrnd = new javax.swing.JPanel();
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
        jPanelPrint = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jCheckBoxPrintColour = new javax.swing.JCheckBox();
        jCheckPrintHeader = new javax.swing.JCheckBox();
        jPanel8 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel_PenPrint = new javax.swing.JLabel();
        jScrollBar_PenPrint = new javax.swing.JScrollBar();

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

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
        jButton_OK.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
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
        jButton_Cancel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton_Cancel.setDefaultCapable(false);
        jButton_Cancel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton_Cancel.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton_Cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_CancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton_Cancel, javax.swing.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE)
                    .addComponent(jButton_OK, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addComponent(jButton_OK)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_Cancel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 255));
        jLabel1.setText("Screen display:"); // NOI18N

        jCheckBox_AspectRatio.setMnemonic(java.awt.event.KeyEvent.VK_K);
        jCheckBox_AspectRatio.setText("Keep Diagram's Aspect Ratio"); // NOI18N
        jCheckBox_AspectRatio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_AspectRatioActionPerformed(evt);
            }
        });

        jCheckBox_FixedSize.setMnemonic(java.awt.event.KeyEvent.VK_S);
        jCheckBox_FixedSize.setText("Constant Size (21x15)"); // NOI18N

        javax.swing.GroupLayout jPanelScreenDispLayout = new javax.swing.GroupLayout(jPanelScreenDisp);
        jPanelScreenDisp.setLayout(jPanelScreenDispLayout);
        jPanelScreenDispLayout.setHorizontalGroup(
            jPanelScreenDispLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelScreenDispLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelScreenDispLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox_FixedSize)
                    .addComponent(jCheckBox_AspectRatio)
                    .addComponent(jLabel1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelScreenDispLayout.setVerticalGroup(
            jPanelScreenDispLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelScreenDispLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox_AspectRatio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox_FixedSize))
        );

        jLabel2.setText("Line Thickness"); // NOI18N

        jLabel_PenThickness.setText("1"); // NOI18N

        jScrollBar_Pen.setMinimum(2);
        jScrollBar_Pen.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        jScrollBar_Pen.setUnitIncrement(2);
        jScrollBar_Pen.setValue(10);
        jScrollBar_Pen.setVisibleAmount(0);
        jScrollBar_Pen.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollBar_Pen.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jScrollBar_PenFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jScrollBar_PenFocusLost(evt);
            }
        });
        jScrollBar_Pen.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                jScrollBar_PenAdjustmentValueChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel2))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollBar_Pen, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(jLabel_PenThickness)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_PenThickness)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollBar_Pen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabelAntiA1.setText("Antialiasing:"); // NOI18N

        jComboAntiAlias.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Off", "On", "Default" }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabelAntiA1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboAntiAlias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanelScreenDisp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanelScreenDisp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboAntiAlias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelAntiA1))))
                .addContainerGap())
        );

        jPanel_Fonts.setBorder(javax.swing.BorderFactory.createTitledBorder(null, " Fonts: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 0, 255))); // NOI18N

        buttonGroup_FontPrinter.add(jRadioButton_FontNone);
        jRadioButton_FontNone.setMnemonic(java.awt.event.KeyEvent.VK_N);
        jRadioButton_FontNone.setSelected(true);
        jRadioButton_FontNone.setText("None"); // NOI18N
        jRadioButton_FontNone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_FontNoneActionPerformed(evt);
            }
        });

        buttonGroup_FontPrinter.add(jRadioButton_FontPrinter);
        jRadioButton_FontPrinter.setMnemonic(java.awt.event.KeyEvent.VK_P);
        jRadioButton_FontPrinter.setText("Printer Font"); // NOI18N
        jRadioButton_FontPrinter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_FontPrinterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButton_FontPrinter)
                    .addComponent(jRadioButton_FontNone))
                .addContainerGap(11, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addComponent(jRadioButton_FontNone)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jRadioButton_FontPrinter)
                .addContainerGap())
        );

        jCombo_FontFamily.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Serif", "SansSerif", "Monospaced", "Dialog", "DialogInput" }));

        jCombo_FontStyle.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Plain", "Bold", "Italic" }));

        jText_FontSize.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jText_FontSize.setText("12"); // NOI18N
        jText_FontSize.setMinimumSize(new java.awt.Dimension(60, 20));
        jText_FontSize.setPreferredSize(new java.awt.Dimension(30, 20));
        jText_FontSize.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jText_FontSizeKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jText_FontSizeKeyTyped(evt);
            }
        });

        jLabelAntiAT.setLabelFor(jComboAntiAliasT);
        jLabelAntiAT.setText("Antialiasing (text):"); // NOI18N

        jComboAntiAliasT.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Off", "On", "Default" }));

        javax.swing.GroupLayout jPanel_FontsLayout = new javax.swing.GroupLayout(jPanel_Fonts);
        jPanel_Fonts.setLayout(jPanel_FontsLayout);
        jPanel_FontsLayout.setHorizontalGroup(
            jPanel_FontsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_FontsLayout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel_FontsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel_FontsLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jCombo_FontFamily, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jCombo_FontStyle, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jText_FontSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel_FontsLayout.createSequentialGroup()
                        .addGap(85, 85, 85)
                        .addComponent(jLabelAntiAT)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jComboAntiAliasT, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel_FontsLayout.setVerticalGroup(
            jPanel_FontsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_FontsLayout.createSequentialGroup()
                .addGroup(jPanel_FontsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel_FontsLayout.createSequentialGroup()
                        .addGroup(jPanel_FontsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jCombo_FontFamily, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCombo_FontStyle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jText_FontSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(7, 7, 7)
                        .addGroup(jPanel_FontsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboAntiAliasT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelAntiAT)))
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanelColours.setBorder(javax.swing.BorderFactory.createTitledBorder(null, " Colours: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 0, 255))); // NOI18N

        buttonGroup_Colr.add(jRadioButtonColour);
        jRadioButtonColour.setMnemonic(java.awt.event.KeyEvent.VK_C);
        jRadioButtonColour.setText("Colour"); // NOI18N
        jRadioButtonColour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonColourActionPerformed(evt);
            }
        });

        buttonGroup_Colr.add(jRadioButtonBW);
        jRadioButtonBW.setMnemonic(java.awt.event.KeyEvent.VK_L);
        jRadioButtonBW.setText("bLack on white"); // NOI18N
        jRadioButtonBW.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonBWActionPerformed(evt);
            }
        });

        buttonGroup_Colr.add(jRadioButtonWB);
        jRadioButtonWB.setMnemonic(java.awt.event.KeyEvent.VK_W);
        jRadioButtonWB.setText("White on black"); // NOI18N
        jRadioButtonWB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonWBActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButtonColour)
                    .addComponent(jRadioButtonBW)
                    .addComponent(jRadioButtonWB))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jRadioButtonColour)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButtonBW)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButtonWB)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jCheckBackgr.setMnemonic(java.awt.event.KeyEvent.VK_G);
        jCheckBackgr.setText("Background"); // NOI18N
        jCheckBackgr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBackgrActionPerformed(evt);
            }
        });

        jButton_ResetColours.setMnemonic(java.awt.event.KeyEvent.VK_R);
        jButton_ResetColours.setText("Reset"); // NOI18N
        jButton_ResetColours.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton_ResetColours.setDefaultCapable(false);
        jButton_ResetColours.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ResetColoursActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jCheckBackgr))
                    .addComponent(jButton_ResetColours, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jCheckBackgr)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton_ResetColours)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelBackGrnd.setBackground(new java.awt.Color(255, 255, 255));
        jPanelBackGrnd.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanelBackGrnd.setToolTipText("Alt-B"); // NOI18N
        jPanelBackGrnd.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jPanelBackGrndFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jPanelBackGrndFocusLost(evt);
            }
        });
        jPanelBackGrnd.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jPanelBackGrndMouseReleased(evt);
            }
        });
        jPanelBackGrnd.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jPanelBackGrndKeyReleased(evt);
            }
        });

        jButton1.setBackground(new java.awt.Color(102, 102, 255));
        jButton1.setMnemonic(java.awt.event.KeyEvent.VK_1);
        jButton1.setToolTipText("Alt-1"); // NOI18N
        jButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButton1.setContentAreaFilled(false);
        jButton1.setDefaultCapable(false);
        jButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton1.setOpaque(true);
        jButton1.setPreferredSize(new java.awt.Dimension(14, 14));
        jButton1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jButton1FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jButton1FocusLost(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(255, 153, 153));
        jButton2.setMnemonic(java.awt.event.KeyEvent.VK_2);
        jButton2.setToolTipText("Alt-2"); // NOI18N
        jButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButton2.setContentAreaFilled(false);
        jButton2.setDefaultCapable(false);
        jButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton2.setOpaque(true);
        jButton2.setPreferredSize(new java.awt.Dimension(14, 14));
        jButton2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jButton2FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jButton2FocusLost(evt);
            }
        });
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setBackground(new java.awt.Color(51, 255, 51));
        jButton3.setMnemonic(java.awt.event.KeyEvent.VK_3);
        jButton3.setToolTipText("Alt-3"); // NOI18N
        jButton3.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButton3.setContentAreaFilled(false);
        jButton3.setDefaultCapable(false);
        jButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton3.setOpaque(true);
        jButton3.setPreferredSize(new java.awt.Dimension(14, 14));
        jButton3.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jButton3FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jButton3FocusLost(evt);
            }
        });
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setMnemonic(java.awt.event.KeyEvent.VK_4);
        jButton4.setToolTipText("Alt-4"); // NOI18N
        jButton4.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButton4.setContentAreaFilled(false);
        jButton4.setDefaultCapable(false);
        jButton4.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton4.setOpaque(true);
        jButton4.setPreferredSize(new java.awt.Dimension(14, 14));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jButton4.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jButton4FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jButton4FocusLost(evt);
            }
        });

        jButton5.setMnemonic(java.awt.event.KeyEvent.VK_5);
        jButton5.setToolTipText("Alt-5"); // NOI18N
        jButton5.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButton5.setContentAreaFilled(false);
        jButton5.setDefaultCapable(false);
        jButton5.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton5.setOpaque(true);
        jButton5.setPreferredSize(new java.awt.Dimension(14, 14));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jButton5.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jButton5FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jButton5FocusLost(evt);
            }
        });

        jButton6.setMnemonic(java.awt.event.KeyEvent.VK_6);
        jButton6.setToolTipText("Alt-6"); // NOI18N
        jButton6.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButton6.setContentAreaFilled(false);
        jButton6.setDefaultCapable(false);
        jButton6.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton6.setOpaque(true);
        jButton6.setPreferredSize(new java.awt.Dimension(14, 14));
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jButton6.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jButton6FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jButton6FocusLost(evt);
            }
        });

        jButton7.setMnemonic(java.awt.event.KeyEvent.VK_7);
        jButton7.setToolTipText("Alt-7"); // NOI18N
        jButton7.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButton7.setContentAreaFilled(false);
        jButton7.setDefaultCapable(false);
        jButton7.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton7.setOpaque(true);
        jButton7.setPreferredSize(new java.awt.Dimension(14, 14));
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        jButton7.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jButton7FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jButton7FocusLost(evt);
            }
        });

        jButton8.setMnemonic(java.awt.event.KeyEvent.VK_8);
        jButton8.setToolTipText("Alt-8"); // NOI18N
        jButton8.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButton8.setContentAreaFilled(false);
        jButton8.setDefaultCapable(false);
        jButton8.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton8.setOpaque(true);
        jButton8.setPreferredSize(new java.awt.Dimension(14, 14));
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        jButton8.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jButton8FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jButton8FocusLost(evt);
            }
        });

        jButton9.setMnemonic(java.awt.event.KeyEvent.VK_9);
        jButton9.setToolTipText("Alt-9"); // NOI18N
        jButton9.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButton9.setContentAreaFilled(false);
        jButton9.setDefaultCapable(false);
        jButton9.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton9.setOpaque(true);
        jButton9.setPreferredSize(new java.awt.Dimension(14, 14));
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });
        jButton9.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jButton9FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jButton9FocusLost(evt);
            }
        });

        jButton10.setMnemonic(java.awt.event.KeyEvent.VK_0);
        jButton10.setToolTipText("Alt-0"); // NOI18N
        jButton10.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButton10.setContentAreaFilled(false);
        jButton10.setDefaultCapable(false);
        jButton10.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton10.setOpaque(true);
        jButton10.setPreferredSize(new java.awt.Dimension(14, 14));
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });
        jButton10.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jButton10FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jButton10FocusLost(evt);
            }
        });

        jButton11.setMnemonic(java.awt.event.KeyEvent.VK_Z);
        jButton11.setToolTipText("Alt-Z"); // NOI18N
        jButton11.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButton11.setContentAreaFilled(false);
        jButton11.setDefaultCapable(false);
        jButton11.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton11.setOpaque(true);
        jButton11.setPreferredSize(new java.awt.Dimension(14, 14));
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });
        jButton11.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jButton11FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jButton11FocusLost(evt);
            }
        });

        javax.swing.GroupLayout jPanelBackGrndLayout = new javax.swing.GroupLayout(jPanelBackGrnd);
        jPanelBackGrnd.setLayout(jPanelBackGrndLayout);
        jPanelBackGrndLayout.setHorizontalGroup(
            jPanelBackGrndLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBackGrndLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelBackGrndLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelBackGrndLayout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelBackGrndLayout.createSequentialGroup()
                        .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelBackGrndLayout.setVerticalGroup(
            jPanelBackGrndLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBackGrndLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelBackGrndLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(jPanelBackGrndLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanelColoursLayout = new javax.swing.GroupLayout(jPanelColours);
        jPanelColours.setLayout(jPanelColoursLayout);
        jPanelColoursLayout.setHorizontalGroup(
            jPanelColoursLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelColoursLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelBackGrnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelColoursLayout.setVerticalGroup(
            jPanelColoursLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jPanelBackGrnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanelPrint.setBorder(javax.swing.BorderFactory.createTitledBorder(null, " Diagram printing: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 0, 255))); // NOI18N

        jCheckBoxPrintColour.setMnemonic(java.awt.event.KeyEvent.VK_T);
        jCheckBoxPrintColour.setText("Print colours"); // NOI18N

        jCheckPrintHeader.setMnemonic(java.awt.event.KeyEvent.VK_D);
        jCheckPrintHeader.setText("header with file name & date"); // NOI18N

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxPrintColour)
                    .addComponent(jCheckPrintHeader))
                .addContainerGap(48, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jCheckBoxPrintColour)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckPrintHeader)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel4.setText("Line Thickness"); // NOI18N

        jLabel_PenPrint.setText("1"); // NOI18N

        jScrollBar_PenPrint.setMaximum(50);
        jScrollBar_PenPrint.setMinimum(10);
        jScrollBar_PenPrint.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        jScrollBar_PenPrint.setUnitIncrement(5);
        jScrollBar_PenPrint.setVisibleAmount(0);
        jScrollBar_PenPrint.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollBar_PenPrint.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jScrollBar_PenPrintFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jScrollBar_PenPrintFocusLost(evt);
            }
        });
        jScrollBar_PenPrint.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                jScrollBar_PenPrintAdjustmentValueChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jScrollBar_PenPrint, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(6, 6, 6)
                        .addComponent(jLabel_PenPrint, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)))
                .addGap(31, 31, 31))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel_PenPrint))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollBar_PenPrint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanelPrintLayout = new javax.swing.GroupLayout(jPanelPrint);
        jPanelPrint.setLayout(jPanelPrintLayout);
        jPanelPrintLayout.setHorizontalGroup(
            jPanelPrintLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPrintLayout.createSequentialGroup()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelPrintLayout.setVerticalGroup(
            jPanelPrintLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel_Fonts, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelColours, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelPrint, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel_Fonts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelColours, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelPrint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //<editor-fold defaultstate="collapsed" desc="Events">

    private void jButton_OKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_OKActionPerformed
        diagrPaintUtil.textWithFonts = jRadioButton_FontPrinter.isSelected();

        diagrPaintUtil.fontFamily = jCombo_FontFamily.getSelectedIndex();
        diagrPaintUtil.fontStyle = jCombo_FontStyle.getSelectedIndex();
        int i;
        try{i = Integer.parseInt(jText_FontSize.getText());} catch (NumberFormatException ex) {System.err.println("Error reading integer from text \""+jText_FontSize.getText()+"\""); i=10;}
        diagrPaintUtil.fontSize = Math.max(1, Math.min(i,72));
        diagrPaintUtil.penThickness = (float)jScrollBar_Pen.getValue()/10f;
        diagrPaintUtil.antiAliasing = jComboAntiAlias.getSelectedIndex();
        diagrPaintUtil.antiAliasingText = jComboAntiAliasT.getSelectedIndex();
        diagrPaintUtil.fixedSize = jCheckBox_FixedSize.isSelected();
        diagrPaintUtil.keepAspectRatio = jCheckBox_AspectRatio.isSelected();
        diagrPaintUtil.useBackgrndColour = jCheckBackgr.isSelected();
        diagrPaintUtil.backgrnd = L_backgrnd;
        diagrPaintUtil.colours[0]=jButton1.getBackground();
        diagrPaintUtil.colours[1]=jButton2.getBackground();
        diagrPaintUtil.colours[2]=jButton3.getBackground();
        diagrPaintUtil.colours[3]=jButton4.getBackground();
        diagrPaintUtil.colours[4]=jButton5.getBackground();
        diagrPaintUtil.colours[5]=jButton6.getBackground();
        diagrPaintUtil.colours[6]=jButton7.getBackground();
        diagrPaintUtil.colours[7]=jButton8.getBackground();
        diagrPaintUtil.colours[8]=jButton9.getBackground();
        diagrPaintUtil.colours[9]=jButton10.getBackground();
        diagrPaintUtil.colours[10]=jButton11.getBackground();
        diagrPaintUtil.colourType = 0;
        if (jRadioButtonBW.isSelected()) {
            diagrPaintUtil.colourType = 1;} else if (jRadioButtonWB.isSelected()) {
                diagrPaintUtil.colourType = 2;}
        diagrPaintUtil.printColour = jCheckBoxPrintColour.isSelected();
        diagrPaintUtil.printHeader = jCheckPrintHeader.isSelected();
        diagrPaintUtil.printPenThickness = (float)jScrollBar_PenPrint.getValue()/10f;

        closeWindow();
}//GEN-LAST:event_jButton_OKActionPerformed

    private void jButton_CancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_CancelActionPerformed
        closeWindow();
}//GEN-LAST:event_jButton_CancelActionPerformed

    private void jCheckBox_AspectRatioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_AspectRatioActionPerformed
        if (jCheckBox_AspectRatio.isSelected()) {
            jCheckBox_FixedSize.setVisible(false);} else {jCheckBox_FixedSize.setVisible(true);}
}//GEN-LAST:event_jCheckBox_AspectRatioActionPerformed

    private void jScrollBar_PenFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScrollBar_PenFocusGained
        jScrollBar_Pen.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0,0,0)));
}//GEN-LAST:event_jScrollBar_PenFocusGained

    private void jScrollBar_PenFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScrollBar_PenFocusLost
        jScrollBar_Pen.setBorder(scrollBorder);
}//GEN-LAST:event_jScrollBar_PenFocusLost

    private void jScrollBar_PenAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_jScrollBar_PenAdjustmentValueChanged
        jLabel_PenThickness.setText(String.valueOf((float)jScrollBar_Pen.getValue()/10f));
}//GEN-LAST:event_jScrollBar_PenAdjustmentValueChanged

    private void jRadioButton_FontNoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_FontNoneActionPerformed
        if (jRadioButton_FontNone.isSelected()) {
            jCombo_FontFamily.setVisible(false);
            jCombo_FontStyle.setVisible(false);
            jText_FontSize.setVisible(false);
            jLabelAntiAT.setVisible(false);
            jComboAntiAliasT.setVisible(false);} else {
            jCombo_FontFamily.setVisible(true);
            jCombo_FontStyle.setVisible(true);
            jText_FontSize.setVisible(true);
            jLabelAntiAT.setVisible(true);
            jComboAntiAliasT.setVisible(true);}
}//GEN-LAST:event_jRadioButton_FontNoneActionPerformed

    private void jRadioButton_FontPrinterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_FontPrinterActionPerformed
        if (jRadioButton_FontPrinter.isSelected()) {
            jCombo_FontFamily.setVisible(true);
            jCombo_FontStyle.setVisible(true);
            jText_FontSize.setVisible(true);
            jLabelAntiAT.setVisible(true);
            jComboAntiAliasT.setVisible(true);} else {
            jCombo_FontFamily.setVisible(false);
            jCombo_FontStyle.setVisible(false);
            jText_FontSize.setVisible(false);
            jLabelAntiAT.setVisible(false);
            jComboAntiAliasT.setVisible(false);}
}//GEN-LAST:event_jRadioButton_FontPrinterActionPerformed

    private void jText_FontSizeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jText_FontSizeKeyReleased
        int i;
        try{i = Integer.parseInt(jText_FontSize.getText());} catch (NumberFormatException ex) {i=0;}
        i = Math.max(0, Math.min(i,72));
        jText_FontSize.setText(String.valueOf(i));
}//GEN-LAST:event_jText_FontSizeKeyReleased

    private void jText_FontSizeKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jText_FontSizeKeyTyped
        if (!Character.isDigit(evt.getKeyChar())) {evt.consume();}
}//GEN-LAST:event_jText_FontSizeKeyTyped

    private void jRadioButtonColourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonColourActionPerformed
        jCheckBackgr.setVisible(true);
        jButton_ResetColours.setVisible(true);
        jPanelBackGrnd.setVisible(true);
}//GEN-LAST:event_jRadioButtonColourActionPerformed

    private void jRadioButtonBWActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonBWActionPerformed
        jCheckBackgr.setVisible(false);
        jButton_ResetColours.setVisible(false);
        jPanelBackGrnd.setVisible(false);
}//GEN-LAST:event_jRadioButtonBWActionPerformed

    private void jRadioButtonWBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonWBActionPerformed
        jCheckBackgr.setVisible(false);
        jButton_ResetColours.setVisible(false);
        jPanelBackGrnd.setVisible(false);
}//GEN-LAST:event_jRadioButtonWBActionPerformed

    private void jButton_ResetColoursActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_ResetColoursActionPerformed

        L_backgrnd = new java.awt.Color(255,255,255);
        jPanelBackGrnd.setBackground(L_backgrnd);
        if (jCheckBackgr.isSelected()) {
            jPanelBackGrnd.setFocusable(true);
        } else {jPanelBackGrnd.setFocusable(false);}

        L_colours = resetColours(L_colours);

        jButton1.setBackground(L_colours[0]);
        jButton2.setBackground(L_colours[1]);
        jButton3.setBackground(L_colours[2]);
        jButton4.setBackground(L_colours[3]);
        jButton5.setBackground(L_colours[4]);
        jButton6.setBackground(L_colours[5]);
        jButton7.setBackground(L_colours[6]);
        jButton8.setBackground(L_colours[7]);
        jButton9.setBackground(L_colours[8]);
        jButton10.setBackground(L_colours[9]);
        jButton11.setBackground(L_colours[10]);
}//GEN-LAST:event_jButton_ResetColoursActionPerformed

    private void jCheckBackgrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBackgrActionPerformed
        if (jCheckBackgr.isSelected()) {
            jPanelBackGrnd.setFocusable(true);
            jPanelBackGrnd.setBorder(
                    javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
            jPanelBackGrnd.setBackground(L_backgrnd);
            jPanelBackGrnd.setToolTipText("Alt-B");
        } else {
            jPanelBackGrnd.setBorder(null);
            jPanelBackGrnd.setBackground(java.awt.Color.white);
            jPanelBackGrnd.setToolTipText(null);
            jPanelBackGrnd.setFocusable(false);
        }
}//GEN-LAST:event_jCheckBackgrActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        changeColour(jButton1);
        L_colours[0] = jButton1.getBackground();
}//GEN-LAST:event_jButton1ActionPerformed

    private void jButton1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButton1FocusGained
        jButton1.setBorder(buttonBorderSelected);
}//GEN-LAST:event_jButton1FocusGained

    private void jButton1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButton1FocusLost
        jButton1.setBorder(buttonBorder);
}//GEN-LAST:event_jButton1FocusLost

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        changeColour(jButton2);
        L_colours[1] = jButton2.getBackground();
}//GEN-LAST:event_jButton2ActionPerformed

    private void jButton2FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButton2FocusGained
        jButton2.setBorder(buttonBorderSelected);
}//GEN-LAST:event_jButton2FocusGained

    private void jButton2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButton2FocusLost
        jButton2.setBorder(buttonBorder);
}//GEN-LAST:event_jButton2FocusLost

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        changeColour(jButton3);
        L_colours[2] = jButton3.getBackground();
}//GEN-LAST:event_jButton3ActionPerformed

    private void jButton3FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButton3FocusGained
        jButton3.setBorder(buttonBorderSelected);
}//GEN-LAST:event_jButton3FocusGained

    private void jButton3FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButton3FocusLost
        jButton3.setBorder(buttonBorder);
}//GEN-LAST:event_jButton3FocusLost

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        changeColour(jButton4);
        L_colours[3] = jButton4.getBackground();
}//GEN-LAST:event_jButton4ActionPerformed

    private void jButton4FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButton4FocusGained
        jButton4.setBorder(buttonBorderSelected);
}//GEN-LAST:event_jButton4FocusGained

    private void jButton4FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButton4FocusLost
        jButton4.setBorder(buttonBorder);
}//GEN-LAST:event_jButton4FocusLost

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        changeColour(jButton5);
        L_colours[4] = jButton5.getBackground();
}//GEN-LAST:event_jButton5ActionPerformed

    private void jButton5FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButton5FocusGained
        jButton5.setBorder(buttonBorderSelected);
}//GEN-LAST:event_jButton5FocusGained

    private void jButton5FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButton5FocusLost
        jButton5.setBorder(buttonBorder);
}//GEN-LAST:event_jButton5FocusLost

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        changeColour(jButton6);
        L_colours[5] = jButton6.getBackground();
}//GEN-LAST:event_jButton6ActionPerformed

    private void jButton6FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButton6FocusGained
        jButton6.setBorder(buttonBorderSelected);
}//GEN-LAST:event_jButton6FocusGained

    private void jButton6FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButton6FocusLost
        jButton6.setBorder(buttonBorder);
}//GEN-LAST:event_jButton6FocusLost

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        changeColour(jButton7);
        L_colours[6] = jButton7.getBackground();
}//GEN-LAST:event_jButton7ActionPerformed

    private void jButton7FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButton7FocusGained
        jButton7.setBorder(buttonBorderSelected);
}//GEN-LAST:event_jButton7FocusGained

    private void jButton7FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButton7FocusLost
        jButton7.setBorder(buttonBorder);
}//GEN-LAST:event_jButton7FocusLost

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        changeColour(jButton8);
        L_colours[7] = jButton8.getBackground();
}//GEN-LAST:event_jButton8ActionPerformed

    private void jButton8FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButton8FocusGained
        jButton8.setBorder(buttonBorderSelected);
}//GEN-LAST:event_jButton8FocusGained

    private void jButton8FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButton8FocusLost
        jButton8.setBorder(buttonBorder);
}//GEN-LAST:event_jButton8FocusLost

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        changeColour(jButton9);
        L_colours[8] = jButton9.getBackground();
}//GEN-LAST:event_jButton9ActionPerformed

    private void jButton9FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButton9FocusGained
        jButton9.setBorder(buttonBorderSelected);
}//GEN-LAST:event_jButton9FocusGained

    private void jButton9FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButton9FocusLost
        jButton9.setBorder(buttonBorder);
}//GEN-LAST:event_jButton9FocusLost

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        changeColour(jButton10);
        L_colours[9] = jButton10.getBackground();
}//GEN-LAST:event_jButton10ActionPerformed

    private void jButton10FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButton10FocusGained
        jButton10.setBorder(buttonBorderSelected);
}//GEN-LAST:event_jButton10FocusGained

    private void jButton10FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButton10FocusLost
        jButton10.setBorder(buttonBorder);
}//GEN-LAST:event_jButton10FocusLost

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        changeColour(jButton11);
        L_colours[10] = jButton11.getBackground();
}//GEN-LAST:event_jButton11ActionPerformed

    private void jButton11FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButton11FocusGained
        jButton11.setBorder(buttonBorderSelected);
}//GEN-LAST:event_jButton11FocusGained

    private void jButton11FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButton11FocusLost
        jButton11.setBorder(buttonBorder);
}//GEN-LAST:event_jButton11FocusLost

    private void jPanelBackGrndFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jPanelBackGrndFocusGained
        if (!jCheckBackgr.isSelected()) {return;}
        jPanelBackGrnd.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED,
                new java.awt.Color(102,102,102),
                new java.awt.Color(255,255,255),
                new java.awt.Color(102,102,102),
                new java.awt.Color(0,0,0)));
}//GEN-LAST:event_jPanelBackGrndFocusGained

    private void jPanelBackGrndFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jPanelBackGrndFocusLost
        if (!jCheckBackgr.isSelected()) {return;}
        jPanelBackGrnd.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
}//GEN-LAST:event_jPanelBackGrndFocusLost

    private void jPanelBackGrndKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jPanelBackGrndKeyReleased
        if (!jCheckBackgr.isSelected() ||
                evt.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE ||
                evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER ||
                evt.isActionKey()) {
            evt.consume(); return;}
        changeBackgroundColour();
}//GEN-LAST:event_jPanelBackGrndKeyReleased

    private void jPanelBackGrndMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanelBackGrndMouseReleased
        if (jCheckBackgr.isSelected()) {
            changeBackgroundColour();
        }
        evt.consume();
}//GEN-LAST:event_jPanelBackGrndMouseReleased

    private void jScrollBar_PenPrintFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScrollBar_PenPrintFocusGained
        jScrollBar_PenPrint.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0,0,0)));
}//GEN-LAST:event_jScrollBar_PenPrintFocusGained

    private void jScrollBar_PenPrintFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScrollBar_PenPrintFocusLost
        jScrollBar_PenPrint.setBorder(scrollBorder);
}//GEN-LAST:event_jScrollBar_PenPrintFocusLost

    private void jScrollBar_PenPrintAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_jScrollBar_PenPrintAdjustmentValueChanged
        jLabel_PenPrint.setText(String.valueOf((float)jScrollBar_PenPrint.getValue()/10f));
}//GEN-LAST:event_jScrollBar_PenPrintAdjustmentValueChanged

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeWindow();
    }//GEN-LAST:event_formWindowClosing

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
  } // closeWindow
  private synchronized void notify_All() {notifyAll();}
  /** this method will wait for this dialog frame to be closed */
  public synchronized void waitFor() {
    while(!finished) {
        try {wait();} catch (InterruptedException ex) {}
    } // while
  } // waitFor()

    public java.awt.Color[] resetColours (java.awt.Color c[])
    {c[0] = new java.awt.Color(0,0,0);      //black
     c[1] = new java.awt.Color(255,35,35);  //red
     c[2] = new java.awt.Color(149,67,0);   //vermilion
     c[3] = new java.awt.Color(47,47,255);  //light blue
     c[4] = new java.awt.Color(0,151,0);    //green
     c[5] = new java.awt.Color(200,140,0);  //orange
     c[6] = new java.awt.Color(255,0,255);  //magenta
     c[7] = new java.awt.Color(0,0,128);    //blue
     c[8] = new java.awt.Color(128,128,128);//gray
     c[9] = new java.awt.Color(0,166,255);  //sky blue
     c[10] = new java.awt.Color(128,0,255); //violet
     return c;}

    private void changeColour(javax.swing.JButton jB) {
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        Thread w = new Thread() {@Override public void run(){
            try{Thread.sleep(10000);}   //show the "wait" cursor for 10 sec
            catch (InterruptedException e) {}
            OptionsDiagram.this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }};//new Thread
        w.start();
        java.awt.Color newColour = javax.swing.JColorChooser.showDialog(
                jB.getRootPane(),
                "Select a colour",
                jB.getBackground());
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        if(newColour != null) {
            if(pc.dbg) {System.out.println(
                    "new color = "+newColour.getRed()+"-"+newColour.getGreen()+"-"+newColour.getBlue()+
                    ", background = "+L_backgrnd.getRed()+"-"+L_backgrnd.getGreen()+"-"+L_backgrnd.getBlue());}
            if(jCheckBackgr.isSelected()) {
                if(MainFrame.twoColoursEqual(newColour, L_backgrnd)) {
                    javax.swing.JOptionPane.showMessageDialog(this,
                        "The selected colour is too close"+nl+
                        "to the background colour."+nl+nl+
                        "Your colour selection is discarded.",
                        pc.progName, javax.swing.JOptionPane.WARNING_MESSAGE);
                 return;
                } // different from background?
            } // if jCheckBackgr.isSelected()
            jB.setBackground(newColour);
        } // if newColour != null
    } // changeColour

    private void changeBackgroundColour() {
        java.awt.Color newColour = javax.swing.JColorChooser.showDialog(
                    jPanelBackGrnd.getRootPane(),
                    "Select the background colour",
                    jPanelBackGrnd.getBackground());
        if(newColour != null) {
            for(int i=0; i < DiagrPaintUtility.MAX_COLOURS; i++) {
                if(MainFrame.twoColoursEqual(L_colours[i], newColour)) {
                    javax.swing.JOptionPane.showMessageDialog(this,
                        "The selected background colour is too close"+nl+
                        "to one of the other colours."+nl+nl+
                        "Your background colour selection is discarded.",
                        pc.progName, javax.swing.JOptionPane.WARNING_MESSAGE);
                 return;
                }
                } // for i
            L_backgrnd = newColour;
            jPanelBackGrnd.setBackground(L_backgrnd);
        } // if newColour !=null
    } // changeBackgroundColour

  //</editor-fold>

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup_Colr;
    private javax.swing.ButtonGroup buttonGroup_FontPrinter;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jButton_Cancel;
    private javax.swing.JButton jButton_OK;
    private javax.swing.JButton jButton_ResetColours;
    private javax.swing.JCheckBox jCheckBackgr;
    private javax.swing.JCheckBox jCheckBoxPrintColour;
    private javax.swing.JCheckBox jCheckBox_AspectRatio;
    private javax.swing.JCheckBox jCheckBox_FixedSize;
    private javax.swing.JCheckBox jCheckPrintHeader;
    private javax.swing.JComboBox jComboAntiAlias;
    private javax.swing.JComboBox jComboAntiAliasT;
    private javax.swing.JComboBox jCombo_FontFamily;
    private javax.swing.JComboBox jCombo_FontStyle;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabelAntiA1;
    private javax.swing.JLabel jLabelAntiAT;
    private javax.swing.JLabel jLabel_PenPrint;
    private javax.swing.JLabel jLabel_PenThickness;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelBackGrnd;
    private javax.swing.JPanel jPanelColours;
    private javax.swing.JPanel jPanelPrint;
    private javax.swing.JPanel jPanelScreenDisp;
    private javax.swing.JPanel jPanel_Fonts;
    private javax.swing.JRadioButton jRadioButtonBW;
    private javax.swing.JRadioButton jRadioButtonColour;
    private javax.swing.JRadioButton jRadioButtonWB;
    private javax.swing.JRadioButton jRadioButton_FontNone;
    private javax.swing.JRadioButton jRadioButton_FontPrinter;
    private javax.swing.JScrollBar jScrollBar_Pen;
    private javax.swing.JScrollBar jScrollBar_PenPrint;
    private javax.swing.JTextField jText_FontSize;
    // End of variables declaration//GEN-END:variables

}
