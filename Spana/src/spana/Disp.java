package spana;

import lib.common.MsgExceptn;
import lib.common.Util;
import lib.huvud.Div;
import lib.huvud.ProgramConf;
import lib.kemi.graph_lib.DiagrPaintUtility;
import lib.kemi.graph_lib.GraphLib;

/** Displays a "plt"-file. The user coordinate units are in 0.01 cm.
 * The diagram is displayed in a JPanel using the method
 * <code>lib.kemi.graph_lib.DiagrPaintUtility.paintDiagram</code>
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
public class Disp extends javax.swing.JFrame {
    private ProgramConf pc;
    private final ProgramDataSpana pd;
    private java.io.File plotFile;
    private int icon_type = 1;
    /** containts the info in the plot. Instantiated in "readThePlotFile" */
    private GraphLib.PltData dd;
    /** the methods in DiagrPaintUtility are used to paint the diagram */
    private final DiagrPaintUtility diagrPaintUtil;
    /** The name of the plot file. This is just the last name in the pathname's name sequence. */
    public String diagrName;
    /** The absolute path name for the plot file. */
    public String diagrFullName;
    /** The size of this object (JFrame) in the form of a Dimension object. */
    public java.awt.Dimension diagrSize;
    private boolean loading = true;
    private DiagrExport dExp = null;
    private DiagrConvert dConv = null;
    private final static java.text.NumberFormat nf =
            java.text.NumberFormat.getNumberInstance(java.util.Locale.ENGLISH);
    private static final java.text.DecimalFormat myFormatter = (java.text.DecimalFormat)nf;
    /** New-line character(s) to substitute "\n" */
    private static final String nl = System.getProperty("line.separator");
    private final javax.swing.JPanel jPanelDispPlot;

  //<editor-fold defaultstate="collapsed" desc="Disp Constructor">
  /** Creates new form Disp
   * @param dPaintUtil
   * @param pc0
   * @param pd0 */
  public Disp(
        DiagrPaintUtility dPaintUtil,
        ProgramConf pc0,
        ProgramDataSpana pd0) {
    initComponents();
    // ------ the methods in DiagrPaintUtility are used to paint the diagram
    diagrPaintUtil = dPaintUtil;
    this.pc = pc0;
    this.pd = pd0;
    loading = true;

    // ------ the JPanel where the diagram is painted
    jPanelDispPlot = new javax.swing.JPanel(){
        @Override public void paint(java.awt.Graphics g){
            super.paint(g);
            // org.freehep.graphics2d.VectorGraphics g2D = org.freehep.graphics2d.VectorGraphics.create(g);
            java.awt.Graphics2D g2D = (java.awt.Graphics2D)g;
            diagrPaintUtil.paintDiagram(g2D, jPanelDispPlot.getSize(), dd, false);
        }
    };
    jPanelDispPlot.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override public void mousePressed(java.awt.event.MouseEvent evt) {
            if(dd.axisInfo) {maybeShowPopup(evt);}
        }
        @Override public void mouseReleased(java.awt.event.MouseEvent evt) {
            if(dd.axisInfo) {maybeHidePopup(evt);}
        }
    });
    jPanelDispPlot.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
        @Override public void mouseDragged(java.awt.event.MouseEvent evt) {
            if(dd.axisInfo) {maybeMovePopup(evt);}
        }
    });
    jPanelDispPlot.setBackground(new java.awt.Color(255, 255, 255));
    this.add(jPanelDispPlot);
    // ------

    // ------ Set up Drag-and-Drop
    jPanelDispPlot.setTransferHandler(MainFrame.tHandler);
    jMenuBar.setTransferHandler(MainFrame.tHandler);

    jPanelDispPlot.setLocation(0, 0);
    jPanelDispPlot.setSize(getContentPane().getSize());
    // Note: the size of the panel is also set in "formComponentResized"

    this.pack();

    // ------ Set the size and location of the window in the screen
    MainFrame.dispSize.width = Math.max(120,Math.min(MainFrame.screenSize.width,MainFrame.dispSize.width));
    MainFrame.dispSize.height = Math.max(100,Math.min(MainFrame.screenSize.height,MainFrame.dispSize.height));
    MainFrame.dispLocation.x = Math.max(60,Math.min(MainFrame.dispLocation.x,(MainFrame.screenSize.width-MainFrame.dispSize.width)));
    MainFrame.dispLocation.y = Math.max(10,Math.min(MainFrame.dispLocation.y,(MainFrame.screenSize.height-MainFrame.dispSize.height)));
    this.setLocation(MainFrame.dispLocation);
    this.setSize(MainFrame.dispSize);
    diagrSize = this.getSize();

    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    //--- Alt-Q quit
    javax.swing.KeyStroke altQKeyStroke = javax.swing.KeyStroke.getKeyStroke(
    				java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altQKeyStroke,"ALT_Q");
    javax.swing.Action escAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                closeWindow();
            }};
    getRootPane().getActionMap().put("ALT_Q", escAction);
    //--- F1 for help
    javax.swing.KeyStroke f1KeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(f1KeyStroke,"F1");
    javax.swing.Action f1Action = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                jMenuHelp.doClick();
            }};
    getRootPane().getActionMap().put("F1", f1Action);

    //--- Title, etc
    //getContentPane().setBackground(new java.awt.Color(255, 255, 153));
    jMenuBar.add(javax.swing.Box.createHorizontalGlue(),1); //move "Help" menu to the right
    this.setTitle("Plot file display");
    jPanel_XY.setVisible(false);
    setAdvancedFeatures(pd.advancedVersion);

    // ------
    this.setVisible(true);
    this.toFront();
    this.requestFocus();
    javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
        java.io.File f;
        if(pc.pathAPP != null && pc.pathAPP.trim().length() >0) {
            f = new java.io.File(pc.pathAPP+java.io.File.separator+"PlotPS.jar");
            if(f.exists()) {jMenuItemPS.setEnabled(true);}
            f = new java.io.File(pc.pathAPP+java.io.File.separator+"PlotPDF.jar");
            if(f.exists()) {jMenuItemPDF.setEnabled(true);}
        }
        if(!jMenuItemPS.isEnabled() && !jMenuItemPDF.isEnabled()) {jMenuConvert.setEnabled(false);}
    }}); //invokeLater(Runnable)
  } // Disp
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="startPlotFile(plotFile)">
 /** Reads a plot file and displays it in this object.
  * If there is a problem while reading the file, the variable "this.diagrName"
  * will be null.
  * @param plotFile0 */
  public void startPlotFile(java.io.File plotFile0) {
    this.plotFile = plotFile0;
    diagrName = null; diagrFullName = null;
    if(plotFile != null) {
        if(pc.dbg) {System.out.println("Disp("+plotFile.getAbsolutePath()+")");}
        if(!readThePlotFile(plotFile)) {MsgExceptn.exception("Error reading plot file");}
        else {
            diagrName = plotFile.getName();
            diagrFullName = plotFile.getAbsolutePath();
        }
    } else {MsgExceptn.exception("Error in \"Disp\": plot file is null.");}
    if(diagrName == null || diagrName.trim().length() <=0) {
        this.setVisible(false);
        this.dispose();
        return;
    }
    this.setTitle(dd.pltFile_Name);

    loading = false;
    jPanelDispPlot.repaint();

  } // start(args)
  // </editor-fold>

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel_XY = new javax.swing.JPanel();
        jLabel_XY = new javax.swing.JLabel();
        jMenuBar = new javax.swing.JMenuBar();
        jMenu = new javax.swing.JMenu();
        jMenuCopyAs = new javax.swing.JMenu();
        jMenu_Copy_EMF = new javax.swing.JMenuItem();
        jMenu_Copy_WMF_RTF = new javax.swing.JMenuItem();
        jMenu_Copy_EMF_RTF = new javax.swing.JMenuItem();
        jMenu_Copy_MacPict = new javax.swing.JMenuItem();
        jMenu_Copy_Image = new javax.swing.JMenuItem();
        jMenuExport = new javax.swing.JMenuItem();
        jMenuConvert = new javax.swing.JMenu();
        jMenuItemPDF = new javax.swing.JMenuItem();
        jMenuItemPS = new javax.swing.JMenuItem();
        jMenuPrint = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuRefresh = new javax.swing.JMenuItem();
        jMenuWSize = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuMainW = new javax.swing.JMenuItem();
        jMenu_Exit = new javax.swing.JMenuItem();
        jMenuHlp = new javax.swing.JMenu();
        jMenuHelp = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                formComponentMoved(evt);
            }
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        getContentPane().setLayout(null);

        jPanel_XY.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel_XY.setText("<html>&nbsp;x:0.00000<br>&nbsp;y:-8.00E+00&nbsp;</html>");
        jLabel_XY.setFocusable(false);
        jLabel_XY.setPreferredSize(new java.awt.Dimension(70, 30));

        javax.swing.GroupLayout jPanel_XYLayout = new javax.swing.GroupLayout(jPanel_XY);
        jPanel_XY.setLayout(jPanel_XYLayout);
        jPanel_XYLayout.setHorizontalGroup(
            jPanel_XYLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_XYLayout.createSequentialGroup()
                .addComponent(jLabel_XY, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(23, Short.MAX_VALUE))
        );
        jPanel_XYLayout.setVerticalGroup(
            jPanel_XYLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_XYLayout.createSequentialGroup()
                .addComponent(jLabel_XY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel_XY);
        jPanel_XY.setBounds(10, 10, 90, 43);

        jMenu.setMnemonic('m');
        jMenu.setText("Menu");

        jMenuCopyAs.setMnemonic('c');
        jMenuCopyAs.setText("Copy as ...");

        jMenu_Copy_EMF.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        jMenu_Copy_EMF.setText("EMF (Enhanced MetaFile)");
        jMenu_Copy_EMF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_Copy_EMFActionPerformed(evt);
            }
        });
        jMenuCopyAs.add(jMenu_Copy_EMF);

        jMenu_Copy_WMF_RTF.setText("WMF (Windows MetaFile) RTF-embedded");
        jMenu_Copy_WMF_RTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_Copy_WMF_RTFActionPerformed(evt);
            }
        });
        jMenuCopyAs.add(jMenu_Copy_WMF_RTF);

        jMenu_Copy_EMF_RTF.setText("EMF embedded in RTF");
        jMenu_Copy_EMF_RTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_Copy_EMF_RTFActionPerformed(evt);
            }
        });
        jMenuCopyAs.add(jMenu_Copy_EMF_RTF);

        jMenu_Copy_MacPict.setText("MacPict embedded in RTF");
        jMenu_Copy_MacPict.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_Copy_MacPictActionPerformed(evt);
            }
        });
        jMenuCopyAs.add(jMenu_Copy_MacPict);

        jMenu_Copy_Image.setText("Image (bitmap)");
        jMenu_Copy_Image.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_Copy_ImageActionPerformed(evt);
            }
        });
        jMenuCopyAs.add(jMenu_Copy_Image);

        jMenu.add(jMenuCopyAs);

        jMenuExport.setMnemonic('e');
        jMenuExport.setText("Export to bitmap file");
        jMenuExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuExportActionPerformed(evt);
            }
        });
        jMenu.add(jMenuExport);

        jMenuConvert.setMnemonic('n');
        jMenuConvert.setText("convert to ...");

        jMenuItemPDF.setMnemonic('d');
        jMenuItemPDF.setText("PDF");
        jMenuItemPDF.setEnabled(false);
        jMenuItemPDF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPDFActionPerformed(evt);
            }
        });
        jMenuConvert.add(jMenuItemPDF);

        jMenuItemPS.setMnemonic('s');
        jMenuItemPS.setText("PS (or EPS)");
        jMenuItemPS.setEnabled(false);
        jMenuItemPS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPSActionPerformed(evt);
            }
        });
        jMenuConvert.add(jMenuItemPS);

        jMenu.add(jMenuConvert);

        jMenuPrint.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.ALT_MASK));
        jMenuPrint.setMnemonic('p');
        jMenuPrint.setText("Print");
        jMenuPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuPrintActionPerformed(evt);
            }
        });
        jMenu.add(jMenuPrint);
        jMenu.add(jSeparator1);

        jMenuRefresh.setMnemonic('r');
        jMenuRefresh.setText("Refresh");
        jMenuRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuRefreshActionPerformed(evt);
            }
        });
        jMenu.add(jMenuRefresh);

        jMenuWSize.setMnemonic('s');
        jMenuWSize.setText("window Size");
        jMenuWSize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuWSizeActionPerformed(evt);
            }
        });
        jMenu.add(jMenuWSize);
        jMenu.add(jSeparator2);

        jMenuMainW.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.ALT_MASK));
        jMenuMainW.setMnemonic('w');
        jMenuMainW.setText("main Window");
        jMenuMainW.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuMainWActionPerformed(evt);
            }
        });
        jMenu.add(jMenuMainW);

        jMenu_Exit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK));
        jMenu_Exit.setMnemonic('x');
        jMenu_Exit.setText("eXit");
        jMenu_Exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_ExitActionPerformed(evt);
            }
        });
        jMenu.add(jMenu_Exit);

        jMenuBar.add(jMenu);

        jMenuHlp.setMnemonic('H');
        jMenuHlp.setText("Help");
        jMenuHlp.setToolTipText("Help");

        jMenuHelp.setMnemonic('D');
        jMenuHelp.setText("Diagram Windows");
        jMenuHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuHelpActionPerformed(evt);
            }
        });
        jMenuHlp.add(jMenuHelp);

        jMenuBar.add(jMenuHlp);

        setJMenuBar(jMenuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //<editor-fold defaultstate="collapsed" desc="Events">
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeWindow();
    }//GEN-LAST:event_formWindowClosing

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        if(this.getExtendedState()==javax.swing.JFrame.ICONIFIED) {return;} // minimised?
        if(this.getExtendedState()!=javax.swing.JFrame.MAXIMIZED_BOTH) {
            if(this.getHeight()<100){this.setSize(this.getWidth(), 100);}
            if(this.getWidth()<120){this.setSize(120,this.getHeight());}
            diagrSize = this.getSize();
            if(!loading) {MainFrame.dispSize = diagrSize;}
        }
        if(this.isVisible()) {jPanelDispPlot.setSize(getContentPane().getSize());}
    }//GEN-LAST:event_formComponentResized

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        setAdvancedFeatures(pd.advancedVersion);
        // Icon
        String iconName;
        if (icon_type == 1) {iconName = "images/PlotLog256_32x32_whiteBckgr.gif";}
        else {iconName = "images/PlotPred256_32x32_whiteBckgr.gif";}
        java.net.URL imgURL = this.getClass().getResource(iconName);
        if (imgURL != null) {
            this.setIconImage(new javax.swing.ImageIcon(imgURL).getImage());}
        else {System.out.println("Could not load image = \""+iconName+"\"");}
        jPanel_XY.setVisible(false);
        pc.setPathDef(plotFile);
    }//GEN-LAST:event_formWindowActivated

    private void jMenuPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuPrintActionPerformed
        printDiagram(false);
    }//GEN-LAST:event_jMenuPrintActionPerformed

    private void jMenu_Copy_WMF_RTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_Copy_WMF_RTFActionPerformed
        copyWith_jVectClipboard(2);
    }//GEN-LAST:event_jMenu_Copy_WMF_RTFActionPerformed

    private void jMenu_Copy_EMF_RTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_Copy_EMF_RTFActionPerformed
        copyWith_jVectClipboard(1);
    }//GEN-LAST:event_jMenu_Copy_EMF_RTFActionPerformed

    private void jMenu_Copy_MacPictActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_Copy_MacPictActionPerformed
        copyWith_jVectClipboard(4);
    }//GEN-LAST:event_jMenu_Copy_MacPictActionPerformed

    private void jMenu_Copy_ImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_Copy_ImageActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        double h0 = (double)Math.max(10,Math.abs(dd.userSpaceMax.y-dd.userSpaceMin.y));
        double w0 = (double)Math.max(10,Math.abs(dd.userSpaceMax.x-dd.userSpaceMin.x));
        double h2w = h0/w0;
        ClipboardCopy_Image.setClipboard_Image(h2w, dd, diagrPaintUtil);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_jMenu_Copy_ImageActionPerformed

    private void jMenu_ExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_ExitActionPerformed
        closeWindow();
    }//GEN-LAST:event_jMenu_ExitActionPerformed

    private void jMenuMainWActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuMainWActionPerformed
        if(MainFrame.getInstance().getExtendedState()==javax.swing.JFrame.ICONIFIED // minimised?
           || MainFrame.getInstance().getExtendedState()==javax.swing.JFrame.MAXIMIZED_BOTH)
                {MainFrame.getInstance().setExtendedState(javax.swing.JFrame.NORMAL);}
        MainFrame.getInstance().setEnabled(true);
        MainFrame.getInstance().requestFocus();
    }//GEN-LAST:event_jMenuMainWActionPerformed

    private void jMenuExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuExportActionPerformed
        exportImage();
    }//GEN-LAST:event_jMenuExportActionPerformed

    private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved
        if(!loading) {MainFrame.dispLocation = this.getLocation();}
    }//GEN-LAST:event_formComponentMoved

    private void jMenuHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuHelpActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        Thread hlp = new Thread() {@Override public void run(){
            String[] a = {"S_Diagram_Window_htm"};
            lib.huvud.RunProgr.runProgramInProcess(Disp.this,ProgramConf.HELP_JAR,a,false,pc.dbg,pc.pathAPP);
            try{Thread.sleep(2000);}   //show the "wait" cursor for 2 sec
            catch (InterruptedException e) {}
            setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }};//new Thread
        hlp.start();
    }//GEN-LAST:event_jMenuHelpActionPerformed

    private void jMenuRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuRefreshActionPerformed
        reloadPlotFile();
        this.repaint();
    }//GEN-LAST:event_jMenuRefreshActionPerformed

    private void jMenuWSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuWSizeActionPerformed
        DiagrWSize d = new DiagrWSize(this, true, this);
        d.setVisible(true);
        this.setSize(diagrSize);
    }//GEN-LAST:event_jMenuWSizeActionPerformed

    private void jMenuItemPDFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPDFActionPerformed
        pltConvertFile(plotFile.getAbsolutePath(), "pdf");
    }//GEN-LAST:event_jMenuItemPDFActionPerformed

    private void jMenuItemPSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPSActionPerformed
        String type;
        if(pd.diagrConvertEPS) {type = "eps";} else {type = "ps";}
        pltConvertFile(plotFile.getAbsolutePath(), type);
    }//GEN-LAST:event_jMenuItemPSActionPerformed

    private void jMenu_Copy_EMFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_Copy_EMFActionPerformed
        copyWith_jVectClipboard(3);
    }//GEN-LAST:event_jMenu_Copy_EMFActionPerformed

    //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Methods">

  // --- Public methods:

  //<editor-fold defaultstate="collapsed" desc="reloadPlotFile()">
    /** does what the name hints: reloads the plot file from the file
     * into this object (useful if the file has been modified). */
    public void reloadPlotFile() {
        if(dd.pltFile_Name == null) {return;}
        java.io.File f = new java.io.File(dd.pltFile_Name);
        if(!readThePlotFile(f)) {
            String t = "Error reading plot file";
            MsgExceptn.msg(t);
            javax.swing.JOptionPane.showMessageDialog(this, t, pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
            this.dispose(); return;}
        String iconName;
        if(icon_type == 1) {iconName = "images/PlotLog256_32x32_whiteBckgr.gif";}
        else {iconName = "images/PlotPred256_32x32_whiteBckgr.gif";}
        java.net.URL imgURL = this.getClass().getResource(iconName);
        if (imgURL != null) {
            //System.out.println("Icon image = "+(new java.io.File(imgURL.getPath())).toString());
            this.setIconImage(new javax.swing.ImageIcon(imgURL).getImage());}
        else {System.out.println("Could not load image = \""+iconName+"\"");}
    } // reloadPlotFile
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="printDiagram()">
    /**
     * @param defaultPrinter if false, a printer dialog is used to allow the user to select a printer
     */
    public void printDiagram(boolean defaultPrinter) {
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        jPanel_XY.setVisible(false);
        jPanelDispPlot.requestFocus();
        jMenuPrint.setEnabled(false);
        jMenuCopyAs.setEnabled(false);
        jMenuExport.setEnabled(false);
        DiagrPrintUtility.printComponent(jPanelDispPlot, dd, defaultPrinter,
                                            diagrPaintUtil, pc.progName);
        jMenuPrint.setEnabled(true);
        jMenuCopyAs.setEnabled(true);
        jMenuExport.setEnabled(true);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    } // printDiagram()
  //</editor-fold>

  // --- Private methods:

  //<editor-fold defaultstate="collapsed" desc="copyWith_jVectClipboard">
    private void copyWith_jVectClipboard (int i) {
        if (i<1 || i>4) {return;}
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        org.qenherkhopeshef.graphics.vectorClipboard.SimpleClipGraphics
                clipGraphics =
                new org.qenherkhopeshef.graphics.vectorClipboard.SimpleClipGraphics(
                                            jPanelDispPlot.getWidth(),
                                            jPanelDispPlot.getHeight());
        if(i==1) {
            clipGraphics.setPictureFormat(
                org.qenherkhopeshef.graphics.vectorClipboard.PictureFormat.EMF);
        } else if(i==2) {
            clipGraphics.setPictureFormat(
                org.qenherkhopeshef.graphics.vectorClipboard.PictureFormat.WMF);
        } else if(i==3) {
            clipGraphics.setPictureFormat(
                org.qenherkhopeshef.graphics.vectorClipboard.PictureFormat.DIRECT_EMF);
        } else if(i==4) {
            clipGraphics.setPictureFormat(
                org.qenherkhopeshef.graphics.vectorClipboard.PictureFormat.MACPICT);
        }
        java.awt.Graphics2D g = clipGraphics.getGraphics();
        boolean printing = false;
        diagrPaintUtil.paintDiagram(g, jPanelDispPlot.getSize(), dd, printing);
        g.dispose();
        clipGraphics.copyToClipboard();
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        System.out.println("Copied to clipboard");
    } //copyWith_jVectClipboard
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="closeWindow">
    private void closeWindow() {
        if(dExp != null) {dExp.closeWindow();}
        if(dConv != null) {dConv.closeWindow();}
        if(this.getExtendedState()==javax.swing.JFrame.ICONIFIED // minimised?
           || this.getExtendedState()==javax.swing.JFrame.MAXIMIZED_BOTH)
                {this.setExtendedState(javax.swing.JFrame.NORMAL);}
        MainFrame.dispSize.width = this.getWidth();
        MainFrame.dispSize.height = this.getHeight();
        MainFrame.dispLocation.x = this.getX();
        MainFrame.dispLocation.y = this.getY();
        MainFrame.getInstance().setEnabled(true);
        MainFrame.getInstance().requestFocus();
        this.dispose();
    } // closeWindow()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Popup Label_XY">
    private void maybeShowPopup(java.awt.event.MouseEvent e) {
      if (e.getID() == java.awt.event.MouseEvent.MOUSE_CLICKED ||
              e.getID() == java.awt.event.MouseEvent.MOUSE_PRESSED) {
         //it is important that the container (jPanel) has "Null Layout"
         maybeMovePopup(e);
      }
   } // maybeShowPopup
    private void maybeMovePopup(java.awt.event.MouseEvent e) {
         set_jLabel_XY(e.getX(), e.getY());
         jPanel_XY.setLocation(e.getX()+5, e.getY()-jLabel_XY.getHeight()-5);
   } // maybeMovePopup
    private void maybeHidePopup(java.awt.event.MouseEvent e) {
      if (e.getID() == java.awt.event.MouseEvent.MOUSE_RELEASED) {
         jPanel_XY.setVisible(false);
         e.consume();
      }
   } // maybeHidePopup
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="set_jLabel_XY">
    private void set_jLabel_XY(int x, int y) {
        float xCoord, yCoord;
        java.awt.Dimension windowSize = jPanelDispPlot.getSize();
        xCoord = dd.xAxisMin +
                ((0.01f*(dd.userSpaceMin.x + x *
                    (dd.userSpaceMax.x - dd.userSpaceMin.x) / windowSize.width)) - dd.xAxis0) * dd.xAxisScale;
        yCoord = dd.yAxisMin +
                ((0.01f*(dd.userSpaceMax.y - y * 
                    (dd.userSpaceMax.y - dd.userSpaceMin.y) /windowSize.height)) - dd.yAxis0) * dd.yAxisScale;
        if (xCoord > dd.xAxisMax_true || xCoord < dd.xAxisMin_true)
            {jPanel_XY.setVisible(false);return;}
        if (yCoord > dd.yAxisMax_true || yCoord < dd.yAxisMin_true)
            {jPanel_XY.setVisible(false);return;}
        jPanel_XY.setVisible(true);
        String tXformat; String tYformat;
        float abs = Math.abs(xCoord);
        if(abs >1 && abs <=9999.9) {tXformat="####0.00";}
        else if(abs >0.1 && abs <=1) {tXformat="#0.000";}
        else if(abs >0.01 && abs <=0.1) {tXformat="#0.0000";}
        else if(abs >0.001 && abs <=0.01) {tXformat="#0.00000";}
        else {tXformat="#1.00E00";} // if(abs >9999.9 || abs <=0.0001)
        abs = Math.abs(yCoord);
        if(abs >1 && abs <=9999.9) {tYformat="####0.00";}
        else if(abs >0.1 && abs <=1) {tYformat="#0.000";}
        else if(abs >0.01 && abs <=0.1) {tYformat="#0.0000";}
        else if(abs >0.001 && abs <=0.01) {tYformat="#0.00000";}
        else {tYformat="#1.00E00";} // if(abs >9999.9 || abs <=0.0001)
        myFormatter.applyPattern(tXformat);
        String txt = "<html>&nbsp;x:" + myFormatter.format(xCoord);
        myFormatter.applyPattern(tYformat);
        txt = txt + "<br>&nbsp;y:" + myFormatter.format(yCoord) + "</html>";
        jLabel_XY.setText(txt);
        //jLabel_XY.setText("<html>&nbsp;x:"+dfp(tXformat,x_coord)+
        //        "<br>&nbsp;y:"+dfp(tYformat,y_coord)+"</html>");
        jPanel_XY.setSize((int)jLabel_XY.getSize().width, 3+(int)jLabel_XY.getSize().height);
    } //set_jLabel_XY

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="readThePlotFile">
    private boolean readThePlotFile(java.io.File f) {
        // read the plot file and store the information in an instance of
        // DiagrData, which will be used to repaint the diagram from memory
        this.dd = new GraphLib.PltData();
        dd.pltFile_Name = f.getPath();
        dd.fileLastModified = new java.util.Date(f.lastModified());
        java.io.BufferedReader bufReader;
        try{bufReader = new java.io.BufferedReader(new java.io.FileReader(f));}
        catch (java.io.FileNotFoundException e) {
            String msg = "Error: \""+e.toString()+"\""+nl+nl+
                "For plot file:"+f.getPath();
            MsgExceptn.exception(msg);
            javax.swing.JOptionPane.showMessageDialog(this, msg,
                pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
        return false;}

        dd.axisInfo = false;
        dd.xAxisL=0f; dd.yAxisL=0f;
        dd.xAxis0=0f; dd.yAxis0=0f;
        boolean readingText = false;
        int align = 0;
        icon_type = 1;
        boolean axisInfo1 = false; boolean axisInfo2 = false;
        int i0, i1, i2;
        StringBuilder line = new StringBuilder();
        String comment;
        int penColour = 5; int currentColour = 1;
        // ----- read all lines
        String l;
        do {
            try {l = bufReader.readLine();}
            catch (java.io.IOException e) {
                String msg = "Error: \""+e.toString()+"\""+nl+nl+
                    "For plot file:"+f.getPath();
                javax.swing.JOptionPane.showMessageDialog(this,msg,
                        pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
                return false;} //catch
            if (line.length()>0) {line.delete(0, line.length());}
            if (l != null) {
                line.append(l);
                String s0, s1, s2;
                if (line.length() > 0) {s0= line.substring(0,1).trim();} else {s0 = "";}
                if (line.length() > 4) {s1= line.substring(1,5).trim();} else {s1 = "";}
                if (line.length() > 8) {s2= line.substring(5,9).trim();} else {s2 = "";}
                // get comments
                if (line.length() > 9) {
                    comment= line.substring(9).trim();
                    if (comment.length()>12 && (comment.substring(0, 12).equals("-- PREDOM DI") ||
                            comment.substring(0, 12).equals("-- PREDOM2 D")))
                        {icon_type = 2;}
                } else {comment = "";}

                if (s0.length() > 0) {i0 = readInt(s0);} else {i0 = -1;}
                if (s1.length() > 0) {i1 = readInt(s1);} else {i1 = 0;}
                if (s2.length() > 0) {i2 = readInt(s2);} else {i2 = 0;}

                if (i0==0 || i0==1) {
                    if(i1<dd.userSpaceMin.x) {dd.userSpaceMin.x = i1;}
                    if(i1>dd.userSpaceMax.x) {dd.userSpaceMax.x = i1;}
                    if(i2<dd.userSpaceMin.y) {dd.userSpaceMin.y = i2;}
                    if(i2>dd.userSpaceMax.y) {dd.userSpaceMax.y = i2;}
                    if (!diagrPaintUtil.textWithFonts || !readingText)
                        {dd.pltFileAList.add(new GraphLib.PltData.PlotStep(i0, i1, i2));
                        // for non-static class use:
                        //dd.plt_fileAList.add(dd.new PlotStep(I0, I1, I2));
                        }
                } // if draw/move line
                if (i0==5 || i0==8) {
                    penColour = i0; currentColour = i1;
                    dd.pltFileAList.add(new GraphLib.PltData.PlotStep(i0,i1,i2));
                } // if colour/pen change

                //---- read axis information if available
                if (comment.equals("-- AXIS --")) {axisInfo1 = true;}
                if (axisInfo1 && i0 == 8 &&
                        comment.length()>64 &&
                        comment.substring(0,9).equals("Size, X/Y"))
                {axisInfo2 = true;
                 dd.xAxisL = readFloat(comment.substring(34,42));
                 dd.yAxisL = readFloat(comment.substring(42,50));
                 dd.xAxis0 = readFloat(comment.substring(50,58));
                 dd.yAxis0 = readFloat(comment.substring(58,comment.length()));
                }
                if (axisInfo1 && axisInfo2 && i0 == 0 &&
                        comment.length()>60 &&
                        comment.substring(0,9).equals("X/Y low a"))
                {dd.axisInfo = true;
                //these are used when displaying the xy-label
                // when the user click the mouse button on a diagram
                dd.xAxisMin = readFloat(comment.substring(18,29));
                dd.xAxisMax = readFloat(comment.substring(29,40));
                dd.yAxisMin = readFloat(comment.substring(40,51));
                dd.yAxisMax = readFloat(comment.substring(51,comment.length()));
                float tmp;
                dd.xAxisMin_true = dd.xAxisMin;
                dd.xAxisMax_true = dd.xAxisMax;
                if (dd.xAxisMin_true > dd.xAxisMax_true)
                    {tmp = dd.xAxisMax_true;
                        dd.xAxisMax_true = dd.xAxisMin_true;
                        dd.xAxisMin_true = tmp;}
                dd.yAxisMin_true = dd.yAxisMin;
                dd.yAxisMax_true = dd.yAxisMax;
                if (dd.yAxisMin_true > dd.yAxisMax_true)
                    {tmp = dd.yAxisMax_true;
                        dd.yAxisMax_true = dd.yAxisMin_true;
                        dd.yAxisMin_true = tmp;}
                float x = Math.abs(dd.xAxisMax - dd.xAxisMin) * 0.01f;
                dd.xAxisMin_true = dd.xAxisMin_true - x;
                dd.xAxisMax_true = dd.xAxisMax_true + x;
                float y = Math.abs(dd.yAxisMax - dd.yAxisMin) * 0.01f;
                dd.yAxisMin_true = dd.yAxisMin_true - y;
                dd.yAxisMax_true = dd.yAxisMax_true + y;
                dd.xAxisScale = (dd.xAxisMax - dd.xAxisMin) / dd.xAxisL;
                dd.yAxisScale = (dd.yAxisMax - dd.yAxisMin) / dd.yAxisL;
                } // axisInfo
                //---- end of reading axis information

                if (diagrPaintUtil.textWithFonts) {
                // read a TextBegin-TextEnd
                boolean isFormula; float txtSize; float txtAngle;
                if (comment.equals("-- HEADING --")) {align = -1;}
                if (i0 == 0 && comment.length()>41 && comment.startsWith("TextBegin")) {
                    isFormula = comment.substring(9,10).equals("C");
                    txtSize = readFloat(comment.substring(17,24));
                    txtAngle = readFloat(comment.substring(35,42));
                    //get angles between +180 and -180
                    while (txtAngle>360) {txtAngle=txtAngle-360f;}
                    while (txtAngle<-360) {txtAngle=txtAngle+360f;}
                    if(txtAngle>180) {txtAngle=txtAngle-360f;}
                    if(txtAngle<-180) {txtAngle=txtAngle+360f;}
                    // get alignment
                    if(comment.length() > 42) {
                        String t = comment.substring(55,56);
                        if(t.equalsIgnoreCase("L")) {align = -1;}
                        else if(t.equalsIgnoreCase("R")) {align = 1;}
                        else if(t.equalsIgnoreCase("C")) {align = 0;}                     
                    }
                    // the text is in next line
                    if(line.length()>0) {line.delete(0, line.length());}
                    try {l = bufReader.readLine();}
                    catch (java.io.IOException e) {
                        String msg = "Error: \""+e.toString()+"\""+nl+nl+
                                    "For plot file:"+f.getPath();
                                    javax.swing.JOptionPane.showMessageDialog(this, msg,
                                    pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
                                    return false;
                    } //catch
                    if(l != null) {line.append(l);} else {line.append("null");}
                    if(!line.toString().equals("null")) {
                        if (line.length()>9) {
                            comment= Util.rTrim(line.substring(9));
                            if(comment.startsWith(" ")) {comment = comment.substring(1);}
                        } else {comment = "";}
                        // addjust userSpaceMax and userSpaceMin
                        textBoxMinMax(i1, i2, (txtSize * 100f),
                            (txtSize*100f*comment.length()), txtAngle, dd);
                        // exchange "-" for minus sign
                        if(isFormula) {comment = replaceMinusSign(comment);}
                        // save the text to print
                        dd.pltTextAList.add(new GraphLib.PltData.PlotText(i1, i2,
                            isFormula, align, txtSize,txtAngle, comment,
                            penColour, currentColour));
                    } // if line != null
                    // ------ read all lines until "TextEnd"
                    readingText = true;
                } // end reading a TextBegin - TextEnd
                if (comment.equals("TextEnd")) {readingText = false;}
                } // if textWithFonts
            } // if line != null
        } while (l != null); // do-while
    try {bufReader.close();}
    catch (java.io.IOException ex) {
        String msg = "Error: \""+ex.toString()+"\""+nl+nl+"For plot file: "+f.getPath();
        javax.swing.JOptionPane.showMessageDialog(this, msg,
                        pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    // set the "UserSpace" dimensions
    if (dd.userSpaceMax.x == Integer.MIN_VALUE) {dd.userSpaceMax.x = 2100;}
    if (dd.userSpaceMin.x == Integer.MAX_VALUE) {dd.userSpaceMin.x = 0;}
    if (dd.userSpaceMax.y == Integer.MIN_VALUE) {dd.userSpaceMax.y = 1500;}
    if (dd.userSpaceMin.y == Integer.MAX_VALUE) {dd.userSpaceMin.y = 0;}
    float xtra = 0.02f; //add 1% extra space all around
    float userSpace_w = Math.abs(dd.userSpaceMax.x - dd.userSpaceMin.x);
    int xShift = Math.round(Math.max( (100f-userSpace_w)/2f, userSpace_w*xtra ) );
    dd.userSpaceMax.x = dd.userSpaceMax.x + xShift;
    dd.userSpaceMin.x = dd.userSpaceMin.x - xShift;
    userSpace_w = Math.abs(dd.userSpaceMax.y - dd.userSpaceMin.y);
    int yShift = Math.round(Math.max( (100f-userSpace_w)/2f, userSpace_w*xtra ) );
    dd.userSpaceMax.y = dd.userSpaceMax.y + yShift;
    dd.userSpaceMin.y = dd.userSpaceMin.y - yShift;
    //these are used when displaying the xy-label
    // when the user click the mouse button on a diagram
    return true;
    } // readThePlotFile (File)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="textBoxMinMax">
 /** Finds out the bounding box around a text, (adding some margins on top and bottom
  * to allow for super- and sub-scripts). Adjusts dd.userSpaceMin and dd.userSpaceMax
  * using this bounding box.
  * @param i1 the x-value for the bottom-left corner
  * @param i2 the y-value for the bottom-left corner
  * @param height the height of the letters of the text in the same coordinates
  * as "i1" and "i2"
  * @param width the width of the text in the same coordinates
  * as "i1" and "i2"
  * @param angle the angle in degrees
  * @param dd  */
  private static void textBoxMinMax (int i1, int i2,
            float height, float width, float angle,
            GraphLib.PltData dd) {
        final double DEG_2_RAD = 0.017453292519943;
        final double a0 = angle *  DEG_2_RAD;
        final double a1 = (angle + 90) *  DEG_2_RAD;
        final float h = height * 2f;
        final float w = width;
        java.awt.Point p0 = new java.awt.Point();
        java.awt.Point p1 = new java.awt.Point();
        p0.x = i1 - (int)((height) * (float)Math.cos(a1));
        p0.y = i2 - (int)((height) * (float)Math.sin(a1));
        if(p0.x<dd.userSpaceMin.x) {dd.userSpaceMin.x = p0.x;}
        if(p0.x>dd.userSpaceMax.x) {dd.userSpaceMax.x = p0.x;}
        if(p0.y<dd.userSpaceMin.y) {dd.userSpaceMin.y = p0.y;}
        if(p0.y>dd.userSpaceMax.y) {dd.userSpaceMax.y = p0.y;}

        p0.x = i1 + (int)(w * (float)Math.cos(a0));
        p0.y = i2 + (int)(w * (float)Math.sin(a0));
        if(p0.x<dd.userSpaceMin.x) {dd.userSpaceMin.x = p0.x;}
        if(p0.x>dd.userSpaceMax.x) {dd.userSpaceMax.x = p0.x;}
        if(p0.y<dd.userSpaceMin.y) {dd.userSpaceMin.y = p0.y;}
        if(p0.y>dd.userSpaceMax.y) {dd.userSpaceMax.y = p0.y;}

        p1.x = p0.x + (int)(h * (float)Math.cos(a1));
        p1.y = p0.y + (int)(h * (float)Math.sin(a1));
        if(p1.x<dd.userSpaceMin.x) {dd.userSpaceMin.x = p1.x;}
        if(p1.x>dd.userSpaceMax.x) {dd.userSpaceMax.x = p1.x;}
        if(p1.y<dd.userSpaceMin.y) {dd.userSpaceMin.y = p1.y;}
        if(p1.y>dd.userSpaceMax.y) {dd.userSpaceMax.y = p1.y;}

        p0.x = i1 + (int)(h * (float)Math.cos(a1));
        p0.y = i2 + (int)(h * (float)Math.sin(a1));
        if(p0.x<dd.userSpaceMin.x) {dd.userSpaceMin.x = p0.x;}
        if(p0.x>dd.userSpaceMax.x) {dd.userSpaceMax.x = p0.x;}
        if(p0.y<dd.userSpaceMin.y) {dd.userSpaceMin.y = p0.y;}
        if(p0.y>dd.userSpaceMax.y) {dd.userSpaceMax.y = p0.y;}
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="readInt(String)">
    private static int readInt(String t) {
      int i;
      try{i = Integer.parseInt(t);}
      catch (NumberFormatException ex) {
          MsgExceptn.exception("Error: "+ex.toString()+nl+
                  "   while reading an integer from String: \""+t+"\"");
          i = 0;}
      return i;
    } //readFloat(T)
  //</editor-fold>
  //<editor-fold defaultstate="collapsed" desc="readFloat(String)">
    private static float readFloat(String t) {
      float f;
      try{f = Float.parseFloat(t);}
      catch (NumberFormatException ex) {
          MsgExceptn.exception("Error: "+ex.toString()+nl+
                  "   while reading a \"float\" from String: \""+t+"\"");
          f = 0f;}
      return f;
    } //readFloat(T)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="replaceMinusSign (String)">
   /** replace hyphen "-" by minus, except betwen two letters */
    private static String replaceMinusSign (String t) {
        // change "-" for Unicode minus sign
        final char M='\u2013'; // Unicode En Dash = \u2013; Minus Sign = \u2212
        int tLength = t.length();
        if (tLength<=1 || t.indexOf('-')<0) {return t;}
        StringBuilder sb = new StringBuilder();
        sb.append(t);
        int l, now, n; //last, now and next characters
        // check for a minus at the beginning
        now = sb.charAt(0);
        if(now =='-') {sb.setCharAt(0, M);}
        // loop
        for (int i=1; i<tLength; i++) {
            now = sb.charAt(i);
            if(now !='-') {continue;}
            l = sb.charAt(i-1);
            if(i<=(tLength-2)) {n = sb.charAt(i+1);} else {n = 32;} // next character
            if( ((l>=65 && l<=90) // upper case letter
                        || (l>=97 && l<=122)) // lower case letter
                    && 
                    ((n>=65 && n<=90) // upper case letter
                        || (n>=97 && n<=122)) ) // lower case letter)
                    {continue;}
            sb.setCharAt(i, M);
        } // for i
        return sb.toString();
    } // replaceMinusSign
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="pltConvertFile">
 /**  Convert a plot file to another vector graphics format.
  * @param pltFile
  * @param type a text specifying the new graphic format.
  * Must be one of: ps, eps, pdf.
  */
  private void pltConvertFile(String pltFile, String type) {
    if(dConv != null) {
        MsgExceptn.exception("Error: \"dConv\" is not null.");
        return;
    }
    if(pltFile == null || pltFile.trim().length() <=0) {
         MsgExceptn.exception("Programming Error in \"pltConvertFile\": empty file name.");
         return;
    }
    String t = null;
    if(type != null) {t = type.toLowerCase();}
    if(t == null || t.length()<2 || t.length() >3 ||
        (!t.equals("pdf") && !t.equals("ps") && !t.equals("eps"))) {
        MsgExceptn.exception("Programming Error in \"pltConvertFile\""+nl+
                  "   type = \""+type+"\""+nl+
                  "   must be one of: pdf, ps or eps.");
        return;
    }
    jMenuConvert.setEnabled(false);
    jMenuItemPDF.setEnabled(false);
    jMenuItemPS.setEnabled(false);
    pc.setPathDef(pltFile);
    final String pltName = pltFile;
    final int typ;
    if(t.equals("pdf")) {typ = 1;}
    else if(t.equals("ps")) {typ = 2;}
    else if(t.equals("eps")) {typ = 3;}
    else {typ = -1;}
    //---- do the conversion
    Thread c = new Thread() {@Override public void run(){
        dConv = new DiagrConvert(pc,pd, dd);
        dConv.start(typ, pltName);
        dConv.waitFor();
        //dConv.dispose();
        dConv = null;
        javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
            jMenuConvert.setEnabled(true);
            jMenuItemPDF.setEnabled(true);
            jMenuItemPS.setEnabled(true);
        }}); //invokeLater(Runnable)
    }};//new Thread
    c.start();
  } //pltConvertFile
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="exportImage(type)">
  private void exportImage() {
    if(dExp != null) {
        MsgExceptn.exception("Error: dExp is not null.");
        return;
    }
    jMenuExport.setEnabled(false);
    Thread c = new Thread() {@Override public void run(){
        double h0 = (double)Math.max(10,Math.abs(dd.userSpaceMax.y-dd.userSpaceMin.y));
        double w0 = (double)Math.max(10,Math.abs(dd.userSpaceMax.x-dd.userSpaceMin.x));
        double h2w = h0/w0;
        dExp = new DiagrExport(pc, pd, h2w);
        boolean cancel;
        if(dExp.start(plotFile.getAbsolutePath())) {
            cancel = dExp.waitFor();
        } else {
            cancel = true;
        }
        dExp = null;
        if(!cancel) {export(pd.diagrExportType, true);}
        javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
            jMenuExport.setEnabled(true);
        }}); //invokeLater(Runnable)
        }};//new Thread
        c.start();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="export(type)">
 /** Export the plot file to some pixel format.
  * The size of the image is the size selected by the user in the last export,
  * or the default value if the user has not yet made any manual export.
  * @param type one of the supported formats (bmp, jpg, png, etc)
  * @param showMsgBox show a message box at the end?
  */
  public void export(String type, boolean showMsgBox) {
    if(type == null || type.length() <= 0) {
        MsgExceptn.exception("Error in \"export(type)\":  empty type.");
        return;
    }
    boolean fnd = false;
    for(String t : MainFrame.FORMAT_NAMES) {if(type.equalsIgnoreCase(t)) {fnd = true; break;}}
    if(!fnd) {
         String msg = "Error in \"export(\""+type+"\")\":"+nl+
                  "Type not supported."+nl+"Should be one of:  ";
         for(String t : MainFrame.FORMAT_NAMES) {msg = "  "+msg + t + ", ";}
         msg = msg.substring(0,msg.length()-2);
         msg = msg + ".";
         MsgExceptn.exception(msg);
         return;
    }
    //---- get the file name after conversion
    String pltFileFullName;
    try{pltFileFullName = plotFile.getCanonicalPath();}
    catch (java.io.IOException ex) {
        try{pltFileFullName = plotFile.getAbsolutePath();}
        catch (Exception e) {pltFileFullName = plotFile.getPath();}
    }
    String convertedFileFullN = Div.getFileNameWithoutExtension(pltFileFullName)+"."+type.toLowerCase();
    java.io.File convertedFile = new java.io.File(convertedFileFullN);
    if(pc.dbg) {System.out.println("Exporting file \""+plotFile.getName()+"\" to "+type.toUpperCase()+"-format");}

    int w, h;
    double h0 = (double)Math.max(10,Math.abs(dd.userSpaceMax.y-dd.userSpaceMin.y));
    double w0 = (double)Math.max(10,Math.abs(dd.userSpaceMax.x-dd.userSpaceMin.x));
    if((h0/w0) < 1) {
        w = pd.diagrExportSize;
        h = (int)((double)pd.diagrExportSize * (h0/w0)); 
    } else {
        h = pd.diagrExportSize;
        w = (int)((double)pd.diagrExportSize / (h0/w0)); 
    }

    int i = java.awt.image.BufferedImage.TYPE_INT_RGB;
    if(type.equalsIgnoreCase("wbmp")) {i = java.awt.image.BufferedImage.TYPE_BYTE_BINARY;}
    java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(w, h, i);

    java.awt.Graphics2D ig2 = bi.createGraphics();
      
    if(diagrPaintUtil.useBackgrndColour) {
        ig2.setColor(diagrPaintUtil.backgrnd);
    } else {
        ig2.setColor(java.awt.Color.WHITE);
    }
    ig2.fillRect(0, 0, w, h);

    diagrPaintUtil.paintDiagram(ig2, new java.awt.Dimension(w,h), dd, false);

    long fileDate0 = convertedFile.lastModified();

    try{
        javax.imageio.ImageIO.write(bi, type.toLowerCase(), convertedFile);
    } catch (java.io.IOException ioe) {
        MsgExceptn.exception(ioe.toString()+nl+Util.stack2string(ioe));
    }
    ig2.dispose();
    // bi = null; // garbage collect
      
    long fileDate = convertedFile.lastModified();
    if(fileDate > fileDate0) {
        System.out.println("Created "+type+"-file: \""+convertedFile.getName()+"\"");
        if(showMsgBox) {
            String msg = "<html>Created "+type+"-file:<br>"+
                    "<b>\""+convertedFile.getName()+"\"</b></html>";
            javax.swing.JOptionPane.showMessageDialog(Disp.this, msg,
                pc.progName,javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
        System.out.flush();
    } else {
        if(showMsgBox) {
            javax.swing.JOptionPane.showMessageDialog(Disp.this,
                "<html>Failed to create file:<br>"+
                "<b>\""+convertedFile.getName()+"\"</b></html>",
                pc.progName,javax.swing.JOptionPane.ERROR_MESSAGE);
        } else {
            System.err.println("---- Failed to create "+type+"-file: \""+convertedFile.getName()+"\" !");
            System.err.flush();
        }
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="setAdvancedFeatures(boolean)">
    final void setAdvancedFeatures(boolean advanced) {
      jMenuWSize.setVisible(advanced);
    } //readFloat(T)
  //</editor-fold>

//</editor-fold>

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel_XY;
    private javax.swing.JMenu jMenu;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenu jMenuConvert;
    private javax.swing.JMenu jMenuCopyAs;
    private javax.swing.JMenuItem jMenuExport;
    private javax.swing.JMenuItem jMenuHelp;
    private javax.swing.JMenu jMenuHlp;
    private javax.swing.JMenuItem jMenuItemPDF;
    private javax.swing.JMenuItem jMenuItemPS;
    private javax.swing.JMenuItem jMenuMainW;
    private javax.swing.JMenuItem jMenuPrint;
    private javax.swing.JMenuItem jMenuRefresh;
    private javax.swing.JMenuItem jMenuWSize;
    private javax.swing.JMenuItem jMenu_Copy_EMF;
    private javax.swing.JMenuItem jMenu_Copy_EMF_RTF;
    private javax.swing.JMenuItem jMenu_Copy_Image;
    private javax.swing.JMenuItem jMenu_Copy_MacPict;
    private javax.swing.JMenuItem jMenu_Copy_WMF_RTF;
    private javax.swing.JMenuItem jMenu_Exit;
    private javax.swing.JPanel jPanel_XY;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    // End of variables declaration//GEN-END:variables

} // public class Disp
