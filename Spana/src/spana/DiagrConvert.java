package spana;

import lib.common.MsgExceptn;
import lib.huvud.Div;
import lib.huvud.ProgramConf;
import lib.kemi.graph_lib.GraphLib;

/** Asks the user for parameters to convert a "plt"-file to either pdf or PostScript.
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
public class DiagrConvert extends javax.swing.JFrame {
  private ProgramConf pc;
  private final ProgramDataSpana pd;
  private final GraphLib.PltData dd;
  private boolean finished = false;
  private final java.awt.Dimension windowSize;
  /** conversion: 1 = to "pdf"; 2 = to "ps" (PostScript); 3 = to "eps" (encapsulated PostScript) */
  private int type = -1;

  private String pltFileFullName;
  private String convertedFileFullN;
  private java.io.File convertedFile;
  private boolean cancel = true;

  private boolean convertHeader = true;
  private boolean convertColours = true;
  /** Font type: 0=draw; 1=Times; 2=Helvetica; 3=Courier. */
  private int convertFont = 2;
  private boolean convertPortrait = true;
  private boolean convertEPS = false;
  private int convertSizeX = 100;
  private int convertSizeY = 100;
  /** margin in cm */
  private float convertMarginB = 1;
  /** margin in cm */
  private float convertMarginL = 1;

  /** the "bounds" for jPanelPaper */
  private java.awt.Rectangle paper = new java.awt.Rectangle(0, 0, 100, 100);
  /** the "bounds" for jScrollBarMarginB */
  private java.awt.Rectangle marginB = new java.awt.Rectangle(0, 0, 16, 100);
  /** the "bounds" for jScrollBarMarginL */
  private java.awt.Rectangle marginL = new java.awt.Rectangle(0, 0, 100, 16);
  /** the diagram box shown inside the jPanelPaper */
  private final java.awt.Rectangle diagram = new java.awt.Rectangle(0, 0, 100, 100);
  private final java.awt.Dimension A4 = new java.awt.Dimension(21*4, 29*4);
  private final double D_FACTOR = 1.00*4;
  /** {"pdf", "ps", "eps"} */
  private static final String[] EXTS = {"pdf", "ps", "eps"};
  private javax.swing.border.Border scrollBorder;
  private static final String nl = System.getProperty("line.separator");
  private javax.swing.ImageIcon icon = null;

  //<editor-fold defaultstate="collapsed" desc="Constructor">
 /** Displays a frame to allow the user to change the settings and perform the
  * conversion of a "plt" file into "pdf", "ps" or "eps".
  * @param pc0 program configuration parameters
  * @param pd0 program data
  * @param dd0 data from the "plt" file and other information needed to paint the diagram
  */
  public DiagrConvert(
          ProgramConf pc0,
          ProgramDataSpana pd0,
          GraphLib.PltData dd0) {
    initComponents();
    pc = pc0; pd = pd0; dd = dd0;
    finished = false;
    cancel = true;
    //--- close window on ESC key
    javax.swing.KeyStroke escKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_ESCAPE,0, false);
    javax.swing.Action escAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            closeWindow();
        }};
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(escKeyStroke,"ESCAPE");
    getRootPane().getActionMap().put("ESCAPE", escAction);
    //--- alt-Q quit
    javax.swing.KeyStroke altQKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altQKeyStroke,"ALT_Q");
    getRootPane().getActionMap().put("ALT_Q", escAction);
    //--- Alt-X eXit
    javax.swing.KeyStroke altXKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altXKeyStroke,"ALT_X");
    javax.swing.Action altXAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jButtonDoIt.doClick();
        }};
    getRootPane().getActionMap().put("ALT_X", altXAction);
    //--- F1 for help
    javax.swing.KeyStroke f1KeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(f1KeyStroke,"F1");
    javax.swing.Action f1Action = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            DiagrConvert.this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            Thread hlp = new Thread() {@Override public void run(){
                String[] a = {"S_Printing_htm_Convert"};
                lib.huvud.RunProgr.runProgramInProcess(DiagrConvert.this,ProgramConf.HELP_JAR,a,false,pc.dbg,pc.pathAPP);
                try{Thread.sleep(2000);}   //show the "wait" cursor for 2 sec
                catch (InterruptedException e) {}
                DiagrConvert.this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }};//new Thread
            hlp.start();
        }};
    getRootPane().getActionMap().put("F1", f1Action);
    //--- Alt-H help
    javax.swing.KeyStroke altHKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altHKeyStroke,"ALT_H");
    getRootPane().getActionMap().put("ALT_H", f1Action);

    //--- center Window on Screen
    windowSize = this.getSize();
    int left; int top;
    left = Math.max(55, (MainFrame.screenSize.width  - windowSize.width ) / 2);
    top = Math.max(10, (MainFrame.screenSize.height - windowSize.height) / 2);
    this.setLocation(Math.min(MainFrame.screenSize.width-100, left),
                         Math.min(MainFrame.screenSize.height-100, top));

    scrollBorder = jScrollBarX.getBorder(); // get the default scroll bar border
    this.setTitle("Convert a diagram:");
    //--- Icon
    String iconName;
    if(type == 1) {iconName = "images/Icon-PDF.gif";} else {iconName = "images/Icon-PS.gif";}
    java.net.URL imgURL = this.getClass().getResource(iconName);
    if(imgURL != null) {
        icon = new javax.swing.ImageIcon(imgURL);
        this.setIconImage(icon.getImage());
    }
    else {System.out.println("--- Error in DiagrConvert constructor: Could not load image = \""+iconName+"\""); icon =null;}

    convertHeader = pd.diagrConvertHeader;
    convertColours = pd.diagrConvertColors;
    convertFont = pd.diagrConvertFont;
    convertPortrait = pd.diagrConvertPortrait;
    convertSizeX = pd.diagrConvertSizeX;
    convertSizeY = pd.diagrConvertSizeY;
    convertMarginB = pd.diagrConvertMarginB;

    convertMarginL = pd.diagrConvertMarginL;
    convertEPS = pd.diagrConvertEPS;

    jCheckHeader.setSelected(convertHeader);
    jCheckColours.setSelected(convertColours);
    jComboBoxFonts.setSelectedIndex(Math.max(0, Math.min(jComboBoxFonts.getItemCount()-1,convertFont)));
    jRadioButtonP.setSelected(convertPortrait);
    jRadioButtonL.setSelected(!convertPortrait);
    jScrollBarX.setValue(Math.max(20,Math.min(300,convertSizeX)));
    jScrollBarY.setValue(Math.max(20,Math.min(300,convertSizeY)));
    jScrollBarMarginB.setValue(-Math.max(-50,Math.min(200,(int)(10f*convertMarginB))));
    jScrollBarMarginL.setValue(Math.max(-50,Math.min(210,(int)(10f*convertMarginL))));
    jLabelMarginBcm.setText(String.valueOf(convertMarginB));
    jLabelMarginLcm.setText(String.valueOf(convertMarginL));
    jButtonDoIt.setIcon(icon);

    jLabelPltFileName.setText(" ");
    jLabelDirName.setText(pc.pathDef.toString());
    jLabelOutputName.setText(" ");

    jScrollBarX.setFocusable(true);
    jScrollBarY.setFocusable(true);
    jScrollBarMarginL.setFocusable(true);
    jScrollBarMarginB.setFocusable(true);

  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="start(type, plotFile)">
 /** Displays this window frame (after making some checks)
  * to allow the user to change the settings and perform the
  * conversion of a "plt" file into "pdf", "ps" or "eps".
  * @param typ conversion: 1 = to "pdf"; 2 = to "ps" (PostScript);
  * 3 = to "eps" (encapsulated PostScript)
  * @param pltFileN name (with full path) of the plt file to be converted
  */
  public void start(
          int typ,
          String pltFileN
          ) {
    type = typ;
    if(type < 1 || type > 3) {
        String msg = "Programming Error: type = "+type+" in DiagrConvert. Should be 1, 2 or 3.";
        MsgExceptn.exception(msg);
        closeWindow();
        return;
    }
    jButtonDoIt.setText("convert to "+EXTS[type-1].toUpperCase());
    if(type == 1) {jCheckBoxEPS.setVisible(false);}
    else {
        jCheckBoxEPS.setVisible(true);
        jCheckBoxEPS.setSelected(convertEPS);
    }
    if(type ==2 || type ==3) {setEPS(convertEPS);}
    redrawDisposition();

    if(pc.dbg) {System.out.println(" - - - - - - DiagrConvert, typ="+typ);}
    this.setVisible(true);

    if(pltFileN == null || pltFileN.trim().length() <=0) {
        String msg = "Programming Error: empty or null file name in DiagrConvert.";
        MsgExceptn.exception(msg);
        javax.swing.JOptionPane.showMessageDialog(this, msg, "Programming error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        closeWindow();
        return;
    }
    //---- get the full name, with path
    java.io.File pltFile = new java.io.File(pltFileN);
    String msg = null;
    if(!pltFile.exists()) {msg = "the file does not exist.";}
    if(!pltFile.canRead()) {msg = "can not open file for reading.";}
    if(msg != null) {
        String t = "Error: \""+pltFileN+"\""+nl+msg;
        MsgExceptn.exception(t);
        javax.swing.JOptionPane.showMessageDialog(this, t, pc.progName,
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        closeWindow();
        return;
    }
    try{pltFileFullName = pltFile.getCanonicalPath();}
    catch (java.io.IOException ex) {
          try{pltFileFullName = pltFile.getAbsolutePath();}
          catch (Exception e) {pltFileFullName = pltFile.getPath();}
    }
    this.setTitle(pltFile.getName());
    jLabelPltFileName.setText(pltFile.getName());
    jLabelDirName.setText(pltFile.getParent());
    //---- get the file name after conversion
    convertedFileFullN = Div.getFileNameWithoutExtension(pltFileFullName)+"."+EXTS[type-1];
    convertedFile = new java.io.File(convertedFileFullN);
    jLabelOutputName.setText(convertedFile.getName());

    int k = this.getWidth() - jPanelBottom.getWidth();
    DiagrConvert.this.validate();
    int j = Math.max(jPanelBottom.getWidth(), jPanelTop.getWidth());
    java.awt.Dimension d = new java.awt.Dimension(k+j, DiagrConvert.this.getHeight());
    DiagrConvert.this.setSize(d);

  } // start
  //</editor-fold>

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupO = new javax.swing.ButtonGroup();
        jPanelTop = new javax.swing.JPanel();
        jLabelPltFile = new javax.swing.JLabel();
        jLabelPltFileName = new javax.swing.JLabel();
        jLabelDir = new javax.swing.JLabel();
        jLabelDirName = new javax.swing.JLabel();
        jPanelMain = new javax.swing.JPanel();
        jPanelSize = new javax.swing.JPanel();
        jScrollBarY = new javax.swing.JScrollBar();
        jScrollBarX = new javax.swing.JScrollBar();
        jLabelSize = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabelY = new javax.swing.JLabel();
        jLabelX = new javax.swing.JLabel();
        jLabelX100 = new javax.swing.JLabel();
        jLabelY100 = new javax.swing.JLabel();
        jPanelDisposition = new javax.swing.JPanel();
        jPanelMarginB = new javax.swing.JPanel();
        jPanelBmargin = new javax.swing.JPanel();
        jLabelMarginB = new javax.swing.JLabel();
        jLabelMarginBcm = new javax.swing.JLabel();
        jPanelEmpty = new javax.swing.JPanel();
        jPanelPaperMargins = new javax.swing.JPanel();
        jPanelPaper = new javax.swing.JPanel();
        jPanelDiagram = new javax.swing.JPanel();
        jLabelDiagram = new javax.swing.JLabel();
        jScrollBarMarginL = new javax.swing.JScrollBar();
        jScrollBarMarginB = new javax.swing.JScrollBar();
        jPanelLmargin = new javax.swing.JPanel();
        jLabelMarginL = new javax.swing.JLabel();
        jLabelMarginLcm = new javax.swing.JLabel();
        jPanelBottom = new javax.swing.JPanel();
        jLabelOut = new javax.swing.JLabel();
        jLabelOutputName = new javax.swing.JLabel();
        jPanelRight = new javax.swing.JPanel();
        jLabelFont = new javax.swing.JLabel();
        jComboBoxFonts = new javax.swing.JComboBox();
        jCheckColours = new javax.swing.JCheckBox();
        jCheckHeader = new javax.swing.JCheckBox();
        jPanelOrientation = new javax.swing.JPanel();
        jRadioButtonP = new javax.swing.JRadioButton();
        jRadioButtonL = new javax.swing.JRadioButton();
        jCheckBoxEPS = new javax.swing.JCheckBox();
        jButtonDoIt = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
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

        jLabelPltFile.setText("Plot file:");

        jLabelPltFileName.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelPltFileName.setText("Hello.plt");

        jLabelDir.setText("Directory: ");

        jLabelDirName.setText("\"D:\\myfiles\\subdir");

        javax.swing.GroupLayout jPanelTopLayout = new javax.swing.GroupLayout(jPanelTop);
        jPanelTop.setLayout(jPanelTopLayout);
        jPanelTopLayout.setHorizontalGroup(
            jPanelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTopLayout.createSequentialGroup()
                .addGroup(jPanelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelTopLayout.createSequentialGroup()
                        .addComponent(jLabelPltFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelPltFileName))
                    .addGroup(jPanelTopLayout.createSequentialGroup()
                        .addComponent(jLabelDir)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelDirName)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanelTopLayout.setVerticalGroup(
            jPanelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTopLayout.createSequentialGroup()
                .addGroup(jPanelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelPltFile)
                    .addComponent(jLabelPltFileName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelDir)
                    .addComponent(jLabelDirName)))
        );

        jScrollBarY.setBlockIncrement(5);
        jScrollBarY.setMaximum(210);
        jScrollBarY.setMinimum(20);
        jScrollBarY.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        jScrollBarY.setValue(100);
        jScrollBarY.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollBarY.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jScrollBarYFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jScrollBarYFocusLost(evt);
            }
        });
        jScrollBarY.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                jScrollBarYAdjustmentValueChanged(evt);
            }
        });

        jScrollBarX.setBlockIncrement(5);
        jScrollBarX.setMaximum(210);
        jScrollBarX.setMinimum(20);
        jScrollBarX.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        jScrollBarX.setValue(100);
        jScrollBarX.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollBarX.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jScrollBarXFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jScrollBarXFocusLost(evt);
            }
        });
        jScrollBarX.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                jScrollBarXAdjustmentValueChanged(evt);
            }
        });

        jLabelSize.setText("Size");

        jLabelY.setText("Y");
        jLabelY.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelYMouseClicked(evt);
            }
        });

        jLabelX.setText("X");
        jLabelX.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelXMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelY, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabelX, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jLabelX)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelY)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jLabelX100.setText("100 %");
        jLabelX100.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelX100MouseClicked(evt);
            }
        });

        jLabelY100.setText("100 %");
        jLabelY100.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelY100MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanelSizeLayout = new javax.swing.GroupLayout(jPanelSize);
        jPanelSize.setLayout(jPanelSizeLayout);
        jPanelSizeLayout.setHorizontalGroup(
            jPanelSizeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSizeLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelSize)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelSizeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelSizeLayout.createSequentialGroup()
                        .addComponent(jScrollBarX, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelX100, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE))
                    .addGroup(jPanelSizeLayout.createSequentialGroup()
                        .addComponent(jScrollBarY, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelY100, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(0, 10, 10))
        );
        jPanelSizeLayout.setVerticalGroup(
            jPanelSizeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSizeLayout.createSequentialGroup()
                .addComponent(jLabelSize)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanelSizeLayout.createSequentialGroup()
                .addGroup(jPanelSizeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabelX100)
                    .addComponent(jScrollBarX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelSizeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollBarY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelY100)))
        );

        jPanelMarginB.setLayout(new java.awt.CardLayout());

        jLabelMarginB.setText("<html>Bottom<br>margin:</html>");
        jLabelMarginB.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelMarginBMouseClicked(evt);
            }
        });

        jLabelMarginBcm.setText("1.0");
        jLabelMarginBcm.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelMarginBcmMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanelBmarginLayout = new javax.swing.GroupLayout(jPanelBmargin);
        jPanelBmargin.setLayout(jPanelBmarginLayout);
        jPanelBmarginLayout.setHorizontalGroup(
            jPanelBmarginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBmarginLayout.createSequentialGroup()
                .addGroup(jPanelBmarginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelBmarginLayout.createSequentialGroup()
                        .addComponent(jLabelMarginB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanelBmarginLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabelMarginBcm)))
                .addContainerGap())
        );
        jPanelBmarginLayout.setVerticalGroup(
            jPanelBmarginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBmarginLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelMarginB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelMarginBcm)
                .addGap(0, 14, Short.MAX_VALUE))
        );

        jPanelMarginB.add(jPanelBmargin, "labels");

        javax.swing.GroupLayout jPanelEmptyLayout = new javax.swing.GroupLayout(jPanelEmpty);
        jPanelEmpty.setLayout(jPanelEmptyLayout);
        jPanelEmptyLayout.setHorizontalGroup(
            jPanelEmptyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 46, Short.MAX_VALUE)
        );
        jPanelEmptyLayout.setVerticalGroup(
            jPanelEmptyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 73, Short.MAX_VALUE)
        );

        jPanelMarginB.add(jPanelEmpty, "empty");

        jPanelPaperMargins.setLayout(null);

        jPanelPaper.setBackground(new java.awt.Color(255, 255, 255));
        jPanelPaper.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanelPaper.setPreferredSize(new java.awt.Dimension(42, 60));
        jPanelPaper.setLayout(null);

        jPanelDiagram.setBackground(new java.awt.Color(153, 153, 153));
        jPanelDiagram.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanelDiagram.setLayout(null);

        jLabelDiagram.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelDiagram.setText("Diagram");
        jPanelDiagram.add(jLabelDiagram);
        jLabelDiagram.setBounds(11, 12, 48, 14);

        jPanelPaper.add(jPanelDiagram);
        jPanelDiagram.setBounds(6, 60, 0, 0);

        jPanelPaperMargins.add(jPanelPaper);
        jPanelPaper.setBounds(17, 0, 84, 116);

        jScrollBarMarginL.setMaximum(210);
        jScrollBarMarginL.setMinimum(-50);
        jScrollBarMarginL.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        jScrollBarMarginL.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollBarMarginL.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jScrollBarMarginLFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jScrollBarMarginLFocusLost(evt);
            }
        });
        jScrollBarMarginL.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                jScrollBarMarginLAdjustmentValueChanged(evt);
            }
        });
        jPanelPaperMargins.add(jScrollBarMarginL);
        jScrollBarMarginL.setBounds(17, 117, 84, 16);

        jScrollBarMarginB.setMaximum(60);
        jScrollBarMarginB.setMinimum(-200);
        jScrollBarMarginB.setValue(-10);
        jScrollBarMarginB.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollBarMarginB.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jScrollBarMarginBFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jScrollBarMarginBFocusLost(evt);
            }
        });
        jScrollBarMarginB.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                jScrollBarMarginBAdjustmentValueChanged(evt);
            }
        });
        jPanelPaperMargins.add(jScrollBarMarginB);
        jScrollBarMarginB.setBounds(0, 0, 16, 116);

        jLabelMarginL.setText("Left margin:");
        jLabelMarginL.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelMarginLMouseClicked(evt);
            }
        });

        jLabelMarginLcm.setText("1.0");
        jLabelMarginLcm.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelMarginLcmMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanelLmarginLayout = new javax.swing.GroupLayout(jPanelLmargin);
        jPanelLmargin.setLayout(jPanelLmarginLayout);
        jPanelLmarginLayout.setHorizontalGroup(
            jPanelLmarginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLmarginLayout.createSequentialGroup()
                .addComponent(jLabelMarginL)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelMarginLcm)
                .addGap(0, 55, Short.MAX_VALUE))
        );
        jPanelLmarginLayout.setVerticalGroup(
            jPanelLmarginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLmarginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelMarginL)
                .addComponent(jLabelMarginLcm))
        );

        javax.swing.GroupLayout jPanelDispositionLayout = new javax.swing.GroupLayout(jPanelDisposition);
        jPanelDisposition.setLayout(jPanelDispositionLayout);
        jPanelDispositionLayout.setHorizontalGroup(
            jPanelDispositionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDispositionLayout.createSequentialGroup()
                .addComponent(jPanelMarginB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelPaperMargins, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelDispositionLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jPanelLmargin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelDispositionLayout.setVerticalGroup(
            jPanelDispositionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDispositionLayout.createSequentialGroup()
                .addGroup(jPanelDispositionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelPaperMargins, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelDispositionLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jPanelMarginB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelLmargin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanelMainLayout = new javax.swing.GroupLayout(jPanelMain);
        jPanelMain.setLayout(jPanelMainLayout);
        jPanelMainLayout.setHorizontalGroup(
            jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jPanelDisposition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanelMainLayout.setVerticalGroup(
            jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelMainLayout.createSequentialGroup()
                .addComponent(jPanelSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanelDisposition, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabelOut.setText("Output file:");

        jLabelOutputName.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelOutputName.setText("Hello.pdf");

        javax.swing.GroupLayout jPanelBottomLayout = new javax.swing.GroupLayout(jPanelBottom);
        jPanelBottom.setLayout(jPanelBottomLayout);
        jPanelBottomLayout.setHorizontalGroup(
            jPanelBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBottomLayout.createSequentialGroup()
                .addComponent(jLabelOut)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelOutputName)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelBottomLayout.setVerticalGroup(
            jPanelBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBottomLayout.createSequentialGroup()
                .addGroup(jPanelBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelOutputName)
                    .addComponent(jLabelOut))
                .addGap(8, 8, 8))
        );

        jLabelFont.setText("Font");

        jComboBoxFonts.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Vector graphics", "Times-Roman", "Helvetica", "Courier" }));
        jComboBoxFonts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxFontsActionPerformed(evt);
            }
        });

        jCheckColours.setMnemonic('c');
        jCheckColours.setText("Colours");
        jCheckColours.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckColoursActionPerformed(evt);
            }
        });

        jCheckHeader.setMnemonic('b');
        jCheckHeader.setText("Banner");
        jCheckHeader.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckHeaderActionPerformed(evt);
            }
        });

        jPanelOrientation.setBorder(javax.swing.BorderFactory.createTitledBorder("Orientation"));

        buttonGroupO.add(jRadioButtonP);
        jRadioButtonP.setMnemonic('p');
        jRadioButtonP.setSelected(true);
        jRadioButtonP.setText("Portrait");
        jRadioButtonP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonPActionPerformed(evt);
            }
        });

        buttonGroupO.add(jRadioButtonL);
        jRadioButtonL.setMnemonic('l');
        jRadioButtonL.setText("Landscape");
        jRadioButtonL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonLActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelOrientationLayout = new javax.swing.GroupLayout(jPanelOrientation);
        jPanelOrientation.setLayout(jPanelOrientationLayout);
        jPanelOrientationLayout.setHorizontalGroup(
            jPanelOrientationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelOrientationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelOrientationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButtonP)
                    .addComponent(jRadioButtonL)))
        );
        jPanelOrientationLayout.setVerticalGroup(
            jPanelOrientationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelOrientationLayout.createSequentialGroup()
                .addComponent(jRadioButtonP)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButtonL)
                .addContainerGap())
        );

        jCheckBoxEPS.setMnemonic('e');
        jCheckBoxEPS.setText("encapsulated PS");
        jCheckBoxEPS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxEPSActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelRightLayout = new javax.swing.GroupLayout(jPanelRight);
        jPanelRight.setLayout(jPanelRightLayout);
        jPanelRightLayout.setHorizontalGroup(
            jPanelRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelRightLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBoxFonts, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanelRightLayout.createSequentialGroup()
                        .addGroup(jPanelRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelFont)
                            .addComponent(jPanelOrientation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckColours)
                            .addComponent(jCheckBoxEPS)
                            .addComponent(jCheckHeader))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanelRightLayout.setVerticalGroup(
            jPanelRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelRightLayout.createSequentialGroup()
                .addComponent(jLabelFont)
                .addGap(3, 3, 3)
                .addComponent(jComboBoxFonts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckColours)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckHeader)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelOrientation, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jCheckBoxEPS)
                .addContainerGap())
        );

        jButtonDoIt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/spana/images/Icon-PDF.gif"))); // NOI18N
        jButtonDoIt.setMnemonic('c');
        jButtonDoIt.setText("convert to ...");
        jButtonDoIt.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButtonDoIt.setIconTextGap(8);
        jButtonDoIt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDoItActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelBottom, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanelTop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonDoIt)
                        .addGap(0, 264, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelRight, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelTop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelRight, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelBottom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonDoIt)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

  //<editor-fold defaultstate="collapsed" desc="Events">

    private void jRadioButtonPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonPActionPerformed
        convertPortrait = jRadioButtonP.isSelected();
        redrawDisposition();
    }//GEN-LAST:event_jRadioButtonPActionPerformed

    private void jRadioButtonLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonLActionPerformed
        convertPortrait = jRadioButtonP.isSelected();
        redrawDisposition();
    }//GEN-LAST:event_jRadioButtonLActionPerformed

    private void jCheckHeaderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckHeaderActionPerformed
        convertHeader = jCheckHeader.isSelected();
    }//GEN-LAST:event_jCheckHeaderActionPerformed

    private void jCheckColoursActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckColoursActionPerformed
        convertColours = jCheckColours.isSelected();
    }//GEN-LAST:event_jCheckColoursActionPerformed

    private void jButtonDoItActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDoItActionPerformed
        saveSettings();
        cancel = false;
        DiagrConvert.this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        //---- do the conversion
        doIt(DiagrConvert.this, type, pltFileFullName, pd, pc);
        closeWindow();
    }//GEN-LAST:event_jButtonDoItActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeWindow();
    }//GEN-LAST:event_formWindowClosing

    private void jScrollBarXAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_jScrollBarXAdjustmentValueChanged
        convertSizeX = jScrollBarX.getValue();
        jLabelX100.setText(String.valueOf(jScrollBarX.getValue())+" %");
        redrawDisposition();
    }//GEN-LAST:event_jScrollBarXAdjustmentValueChanged

    private void jScrollBarYAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_jScrollBarYAdjustmentValueChanged
        convertSizeY = jScrollBarY.getValue();
        jLabelY100.setText(String.valueOf(jScrollBarY.getValue())+" %");
        redrawDisposition();
    }//GEN-LAST:event_jScrollBarYAdjustmentValueChanged

    private void jLabelXMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelXMouseClicked
        jScrollBarX.setValue(100);
    }//GEN-LAST:event_jLabelXMouseClicked

    private void jLabelX100MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelX100MouseClicked
        jScrollBarX.setValue(100);
    }//GEN-LAST:event_jLabelX100MouseClicked

    private void jLabelYMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelYMouseClicked
        jScrollBarY.setValue(100);
    }//GEN-LAST:event_jLabelYMouseClicked

    private void jLabelY100MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelY100MouseClicked
        jScrollBarY.setValue(100);
    }//GEN-LAST:event_jLabelY100MouseClicked

    private void jScrollBarMarginBAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_jScrollBarMarginBAdjustmentValueChanged
        convertMarginB = -(float)jScrollBarMarginB.getValue()/10f;
        if(Math.abs(convertMarginB) < 0.001) {convertMarginB = 0f;}
        jLabelMarginBcm.setText(String.valueOf(convertMarginB));
        redrawDisposition();
    }//GEN-LAST:event_jScrollBarMarginBAdjustmentValueChanged

    private void jScrollBarMarginLAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_jScrollBarMarginLAdjustmentValueChanged
        convertMarginL = (float)jScrollBarMarginL.getValue()/10f;
        if(Math.abs(convertMarginL) < 0.001) {convertMarginL = 0f;}
        jLabelMarginLcm.setText(String.valueOf(convertMarginL));
        redrawDisposition();
    }//GEN-LAST:event_jScrollBarMarginLAdjustmentValueChanged

    private void jLabelMarginBMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelMarginBMouseClicked
        jScrollBarMarginB.setValue(-10);
    }//GEN-LAST:event_jLabelMarginBMouseClicked

    private void jLabelMarginBcmMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelMarginBcmMouseClicked
        jScrollBarMarginB.setValue(-10);
    }//GEN-LAST:event_jLabelMarginBcmMouseClicked

    private void jLabelMarginLMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelMarginLMouseClicked
        jScrollBarMarginL.setValue(10);
    }//GEN-LAST:event_jLabelMarginLMouseClicked

    private void jLabelMarginLcmMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelMarginLcmMouseClicked
        jScrollBarMarginL.setValue(10);
    }//GEN-LAST:event_jLabelMarginLcmMouseClicked

    private void jComboBoxFontsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxFontsActionPerformed
        convertFont = jComboBoxFonts.getSelectedIndex();
    }//GEN-LAST:event_jComboBoxFontsActionPerformed

    private void jCheckBoxEPSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxEPSActionPerformed
        setEPS(jCheckBoxEPS.isSelected());
        redrawDisposition();
    }//GEN-LAST:event_jCheckBoxEPSActionPerformed

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
      if(windowSize != null) {
        int w = windowSize.width;
        int h = windowSize.height;
        if(this.getHeight()<h){this.setSize(this.getWidth(), h);}
        if(this.getWidth()<w){this.setSize(w,this.getHeight());}
      }
    }//GEN-LAST:event_formComponentResized

    private void jScrollBarXFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScrollBarXFocusGained
        jScrollBarX.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0,0,0)));
    }//GEN-LAST:event_jScrollBarXFocusGained

    private void jScrollBarXFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScrollBarXFocusLost
        jScrollBarX.setBorder(scrollBorder);
    }//GEN-LAST:event_jScrollBarXFocusLost

    private void jScrollBarYFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScrollBarYFocusGained
        jScrollBarY.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0,0,0)));
    }//GEN-LAST:event_jScrollBarYFocusGained

    private void jScrollBarYFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScrollBarYFocusLost
        jScrollBarY.setBorder(scrollBorder);
    }//GEN-LAST:event_jScrollBarYFocusLost

    private void jScrollBarMarginBFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScrollBarMarginBFocusGained
        jScrollBarMarginB.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0,0,0)));
    }//GEN-LAST:event_jScrollBarMarginBFocusGained

    private void jScrollBarMarginBFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScrollBarMarginBFocusLost
        jScrollBarMarginB.setBorder(scrollBorder);
    }//GEN-LAST:event_jScrollBarMarginBFocusLost

    private void jScrollBarMarginLFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScrollBarMarginLFocusGained
        jScrollBarMarginL.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0,0,0)));
    }//GEN-LAST:event_jScrollBarMarginLFocusGained

    private void jScrollBarMarginLFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jScrollBarMarginLFocusLost
        jScrollBarMarginL.setBorder(scrollBorder);
    }//GEN-LAST:event_jScrollBarMarginLFocusLost

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Methods">

  public final void closeWindow() {
    if(cancel && changes()) {
        Object[] opt = {"Yes","NO","Cancel"};
        int answer = javax.swing.JOptionPane.showOptionDialog(this,
                "Save options?", pc.progName,
                javax.swing.JOptionPane.YES_NO_CANCEL_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
        if(answer == javax.swing.JOptionPane.YES_OPTION) {saveSettings();}
        else if(answer == javax.swing.JOptionPane.CANCEL_OPTION) {return;}
    }
    finished = true;
    this.setVisible(false);
    this.dispose();
    this.notify_All();
  } // closeWindow()
  private synchronized void notify_All() {notifyAll();}
  /** this method will wait for this dialog frame to be closed
     * @return "cancel"  */
  public synchronized boolean waitFor() {
    while(!finished) {try {wait();} catch (InterruptedException ex) {}}
    return cancel;
  } // waitFor()

  //<editor-fold defaultstate="collapsed" desc="changes">
  private boolean changes() {
    boolean changed = false;
    if(pd.diagrConvertHeader != convertHeader) {changed = true;}
    else if(pd.diagrConvertColors != convertColours) {changed = true;}
    else if(pd.diagrConvertFont != convertFont) {changed = true;}
    else if(pd.diagrConvertPortrait != convertPortrait) {changed = true;}
    else if(pd.diagrConvertSizeX != convertSizeX) {changed = true;}
    else if(pd.diagrConvertSizeY != convertSizeY) {changed = true;}
    else if(pd.diagrConvertMarginB != convertMarginB) {changed = true;}
    else if(pd.diagrConvertMarginL != convertMarginL) {changed = true;}
    else if(pd.diagrConvertEPS != convertEPS) {changed = true;}
    return changed;
  }
  //</editor-fold>
  //<editor-fold defaultstate="collapsed" desc="saveSettings">
  private void saveSettings() {
    pd.diagrConvertHeader = convertHeader;
    pd.diagrConvertColors = convertColours;
    pd.diagrConvertFont = convertFont;
    pd.diagrConvertPortrait = convertPortrait;
    pd.diagrConvertSizeX = convertSizeX;
    pd.diagrConvertSizeY = convertSizeY;
    pd.diagrConvertMarginB = convertMarginB;
    pd.diagrConvertMarginL = convertMarginL;
    pd.diagrConvertEPS = convertEPS;
  }
  //</editor-fold>


  //<editor-fold defaultstate="collapsed" desc="setEPS">
  private void setEPS(boolean eps) {
    convertEPS = eps;
    java.awt.CardLayout cl = (java.awt.CardLayout)jPanelMarginB.getLayout();
    if(eps) {
        type = 3;
        cl.show(jPanelMarginB, "empty");
        jLabelMarginL.setText(" ");
        jLabelMarginLcm.setText(" ");
    } else {
        type = 2;
        cl.show(jPanelMarginB, "labels");
        jLabelMarginL.setText("Left margin:");
        jLabelMarginLcm.setText(String.valueOf(convertMarginL));
    }
    jScrollBarMarginB.setEnabled(!eps);
    jScrollBarMarginL.setEnabled(!eps);
    jPanelOrientation.setVisible(!eps);
    jCheckHeader.setVisible(!eps);
    jButtonDoIt.setText("convert to "+EXTS[type-1].toUpperCase());
    //---- get the file name after conversion
    convertedFileFullN = Div.getFileNameWithoutExtension(pltFileFullName)+"."+EXTS[type-1];
    convertedFile = new java.io.File(convertedFileFullN);
    jLabelOutputName.setText(convertedFile.getName());

  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="redrawDisposition">
  private void redrawDisposition() {
    // these are Rectangles:
    paper = jPanelPaper.getBounds();
    marginB = jScrollBarMarginB.getBounds();
    marginL = jScrollBarMarginL.getBounds();

    boolean portrait;
    if(type !=1 && convertEPS) {portrait = true;} else {portrait = convertPortrait;}

    if(portrait) {
        paper.height = A4.height;
        paper.width = A4.width;
        paper.y = 0;
    } else { //landscape
        paper.width = A4.height;
        paper.height = A4.width;
        paper.y = A4.height - paper.height;
    }
    paper.x = marginB.width + 1;
    marginB.height = paper.height;
    marginB.y = paper.y;
    marginL.width = paper.width;
    jPanelPaper.setBounds(paper);
    jScrollBarMarginB.setBounds(marginB);
    jScrollBarMarginL.setBounds(marginL);

    diagram.height = (int)( ((double)Math.abs(dd.userSpaceMax.y - dd.userSpaceMin.y)/100d)
            * ((double)convertSizeY/100d) * D_FACTOR );
    diagram.width = (int)( ((double)Math.abs(dd.userSpaceMax.x - dd.userSpaceMin.x)/100d)
            * ((double)convertSizeX/100d) * D_FACTOR);
    if(type !=1 && convertEPS) {
        diagram.x = (paper.width - diagram.width)/2;
        diagram.y = (paper.height - diagram.height)/2;
    } else {
        diagram.x = (int)(convertMarginL * 4f);
        diagram.y = (int)((float)(paper.height - diagram.height) - (convertMarginB * 4f));
    }
    int x,y;
    x = (diagram.width - jLabelDiagram.getWidth())/2;
    y = (diagram.height - jLabelDiagram.getHeight())/2;
    jLabelDiagram.setLocation(x, y);

    jPanelDiagram.setBounds(diagram);
    jPanelDisposition.validate();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="doIt">
 /** Convert a plot file to either pdf, PostScript (PS) or encapsulated PS. 
  * @param parent the parent window used for message boxes.
  * If null, no messages are displayed
  * @param typ 1 = pdf conversion; 2 = PS; 3 = EPS
  * @param fileName full path plot file name
  * @param pd0
  * @param pc0 */
  public static void doIt(
          final java.awt.Frame parent,
          int typ,
          String fileName,
          ProgramDataSpana pd0,
          ProgramConf pc0) {
      if(typ <1 || typ >3) {MsgExceptn.exception("Error: typ = "+typ+" (must be 1,2 or 3)"); return;}
      if(fileName == null || fileName.trim().length() <=0) {MsgExceptn.exception("Error: empty file name"); return;}
      if(pd0 == null) {MsgExceptn.exception("Error: prgram data = null"); return;}
      if(pc0 == null) {MsgExceptn.exception("Error: program configuration = null"); return;}
      final ProgramDataSpana pd = pd0;
      final ProgramConf pc = pc0;
      final int type = typ;
      final String pltFileFullName = fileName;
      final String program;
      if(type <= 2) {program = "Plot"+EXTS[type-1].toUpperCase()+".jar";}
      else {program = "Plot"+EXTS[type-1].substring(1).toUpperCase() +".jar";}      
      //---- do the conversion
      java.util.ArrayList<String> options = new java.util.ArrayList<String>();
      options.add(pltFileFullName);
      //---- get the file name after conversion
      String convertedFileFullN = Div.getFileNameWithoutExtension(pltFileFullName)+"."+EXTS[type-1];
      if(type == 1) {options.add("-pdf="+convertedFileFullN);}
      else if(type == 2 || type == 3) {options.add("-ps="+convertedFileFullN);}
      synchronized (pd) {
        options.add("-b="+pd.diagrConvertMarginB);
        options.add("-l="+pd.diagrConvertMarginL);
        options.add("-sX="+(int)(pd.diagrConvertSizeX));
        options.add("-sY="+(int)(pd.diagrConvertSizeY));
        if(!pd.diagrConvertHeader) {options.add("-noH");}
        if(pd.diagrConvertColors) {options.add("-clr");} else {options.add("-bw");}
        if(pd.diagrConvertPortrait) {options.add("-o=P");} else {options.add("-o=L");}
        options.add("-f="+(1+Math.max(0,Math.min(3,pd.diagrConvertFont))));
      } //synchronized
      String[] args = new String[options.size()];
      args = options.toArray(args);
      if(pd.keepFrame || pc.dbg) {
          System.out.println("Running:");
          System.out.print(program+" ");
          for(String t : args) {System.out.print(" "+t);}
          System.out.println();
      }
      final java.io.File convertedFile = new java.io.File(convertedFileFullN);
      final long fileDate0 = convertedFile.lastModified();

      boolean waitForCompletion = true;
      lib.huvud.RunProgr.runProgramInProcess(parent,program,args,waitForCompletion,pc.dbg,pc.pathAPP);
      if(parent != null) {parent.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));}
      long fileDate = convertedFile.lastModified();
      if(fileDate > fileDate0) {
          if(pc.dbg) {System.out.println("--- Created file \""+convertedFile.getName()+"\"");}
          if(parent != null) {
              String msg = "<html>Created ";
              if(type == 1) {msg = msg + "pdf-";}
              else if(type == 3) {msg = msg + "encapsulated ";}
              if(type == 2 || type == 3) {msg = msg + "PostScript-";}
              msg = msg + "file:<br>"+
                  "<b>\""+convertedFile.getName()+"\"</b></html>";
              //--- Icon
              //String iconName = "images/Icon-"+EXTS[type-1]+".gif";
              String iconName = "images/Successful.gif";
              java.net.URL imgURL = DiagrConvert.class.getResource(iconName);
              javax.swing.ImageIcon icon;
              if(imgURL != null) {icon = new javax.swing.ImageIcon(imgURL);}
              else {System.out.println("--- Error in DiagrConvert constructor: Could not load image = \""+iconName+"\""); icon =null;}
              if(icon != null) {
                  javax.swing.JOptionPane.showMessageDialog(parent,
                      msg, pc.progName,javax.swing.JOptionPane.INFORMATION_MESSAGE, icon);
              } else {
                  javax.swing.JOptionPane.showMessageDialog(parent,
                      msg, pc.progName,javax.swing.JOptionPane.INFORMATION_MESSAGE);
              }
          }
      } else {
          if(pc.dbg) {System.out.println("--- Failed to create file \""+convertedFile.getName()+"\"");}
          if(parent != null) {
              javax.swing.JOptionPane.showMessageDialog(parent,
                    "<html>Failed to create file:<br>"+
                    "<b>\""+convertedFile.getName()+"\"</b></html>",
                    pc.progName,javax.swing.JOptionPane.ERROR_MESSAGE);
          }
      }
  }
  //</editor-fold>

  //</editor-fold>

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupO;
    private javax.swing.JButton jButtonDoIt;
    private javax.swing.JCheckBox jCheckBoxEPS;
    private javax.swing.JCheckBox jCheckColours;
    private javax.swing.JCheckBox jCheckHeader;
    private javax.swing.JComboBox jComboBoxFonts;
    private javax.swing.JLabel jLabelDiagram;
    private javax.swing.JLabel jLabelDir;
    private javax.swing.JLabel jLabelDirName;
    private javax.swing.JLabel jLabelFont;
    private javax.swing.JLabel jLabelMarginB;
    private javax.swing.JLabel jLabelMarginBcm;
    private javax.swing.JLabel jLabelMarginL;
    private javax.swing.JLabel jLabelMarginLcm;
    private javax.swing.JLabel jLabelOut;
    private javax.swing.JLabel jLabelOutputName;
    private javax.swing.JLabel jLabelPltFile;
    private javax.swing.JLabel jLabelPltFileName;
    private javax.swing.JLabel jLabelSize;
    private javax.swing.JLabel jLabelX;
    private javax.swing.JLabel jLabelX100;
    private javax.swing.JLabel jLabelY;
    private javax.swing.JLabel jLabelY100;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanelBmargin;
    private javax.swing.JPanel jPanelBottom;
    private javax.swing.JPanel jPanelDiagram;
    private javax.swing.JPanel jPanelDisposition;
    private javax.swing.JPanel jPanelEmpty;
    private javax.swing.JPanel jPanelLmargin;
    private javax.swing.JPanel jPanelMain;
    private javax.swing.JPanel jPanelMarginB;
    private javax.swing.JPanel jPanelOrientation;
    private javax.swing.JPanel jPanelPaper;
    private javax.swing.JPanel jPanelPaperMargins;
    private javax.swing.JPanel jPanelRight;
    private javax.swing.JPanel jPanelSize;
    private javax.swing.JPanel jPanelTop;
    private javax.swing.JRadioButton jRadioButtonL;
    private javax.swing.JRadioButton jRadioButtonP;
    private javax.swing.JScrollBar jScrollBarMarginB;
    private javax.swing.JScrollBar jScrollBarMarginL;
    private javax.swing.JScrollBar jScrollBarX;
    private javax.swing.JScrollBar jScrollBarY;
    // End of variables declaration//GEN-END:variables
}
