package spana;

import lib.common.MsgExceptn;
import lib.huvud.Div;
import lib.huvud.ProgramConf;

/** Asks the user for parameters to export a "plt"-file to pixel format.
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
public class DiagrExport extends javax.swing.JFrame {
  // Note: for java 1.6 jComboBox must not have type,
  //       for java 1.7 jComboBox must be <String>
  private ProgramConf pc;
  private ProgramDataSpana pd;
  private boolean finished = false;
  private java.awt.Dimension windowSize;

  private String pltFileFullName;
  private String convertedFileFullN;
  private java.io.File convertedFile;
  private double height2width = Double.NaN;
  private boolean loading = true;
  private boolean cancel = true;

  private String exportType = "png";
  private int exportSize = 1000;

  private static final String nl = System.getProperty("line.separator");

  //<editor-fold defaultstate="collapsed" desc="Constructor">
 /** Displays a frame to allow the user to change the settings and perform the
  * conversion of a "plt" file into "pdf", "ps" or "eps".  * 
  * @param pc0 program configuration parameters
  * @param pd0 program data
  * @param heightToWidth the height to width ratio of the original diagram in the plt file
  */
  public DiagrExport(
          ProgramConf pc0,
          ProgramDataSpana pd0,
          double heightToWidth) {
    initComponents();
    pc = pc0; pd = pd0;
    height2width = heightToWidth;
    finished = false; cancel = true;
    loading = true;

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
            DiagrExport.this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            Thread hlp = new Thread() {@Override public void run(){
                String[] a = {"S_Printing_htm_BMP"};
                lib.huvud.RunProgr.runProgramInProcess(DiagrExport.this,ProgramConf.HELP_JAR,a,false,pc.dbg,pc.pathAPP);
                try{Thread.sleep(2000);}   //show the "wait" cursor for 2 sec
                catch (InterruptedException e) {}
                DiagrExport.this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
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

    this.setTitle("Export a diagram:");
    //--- Icon
    String iconName = "images/Icon-Export_24x24.gif";
    java.net.URL imgURL = this.getClass().getResource(iconName);
    if(imgURL != null) {
        this.setIconImage(new javax.swing.ImageIcon(imgURL).getImage());
    }
    else {System.out.println("--- Error in DiagrExport constructor: Could not load image = \""+iconName+"\"");}

    //javax.swing.DefaultComboBoxModel dcbm = new javax.swing.DefaultComboBoxModel(); // java 1.6
    javax.swing.DefaultComboBoxModel<String> dcbm = new javax.swing.DefaultComboBoxModel<>();
    for(String ex : MainFrame.FORMAT_NAMES) {
        if(ex.length() >0) {dcbm.addElement(ex);}
    }
    jComboBoxType.setModel(dcbm);

    exportType = pd.diagrExportType;
    exportSize = pd.diagrExportSize;

    for(int i = 0; i < dcbm.getSize(); i++) {
        if(dcbm.getElementAt(i).toString().equalsIgnoreCase(exportType)) {
            jComboBoxType.setSelectedIndex(i);
            break;
        }
    }
    jButtonDoIt.setText("export to "+exportType.toUpperCase());

    jLabelPltFileName.setText(" ");
    jLabelDirName.setText(pc.pathDef.toString());

    exportSize = Math.max(5,
                    Math.min(jScrollBarWidth.getMaximum(),exportSize));
    jScrollBarWidth.setValue(exportSize);

    if(height2width < 1) {
        jScrollBarWidth.setMinimum((int)(11d/height2width));
        jLabel_W.setText(String.valueOf((int)((double)exportSize)));
        jLabel_H.setText(String.valueOf((int)((double)exportSize*height2width)));
    } else {
        jScrollBarWidth.setMinimum((int)(11d*height2width));
        jLabel_W.setText(String.valueOf((int)((double)exportSize/height2width)));
        jLabel_H.setText(String.valueOf((int)((double)exportSize)));
    }

  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="start(type, plotFile)">
 /** Displays this window frame (after making some checks)
  * to allow the user to change the settings and perform the
  * conversion of a "plt" file into "pdf", "ps" or "eps".
  * @param pltFileN name (with full path) of the plt file to be converted
  * @return false if an error occurs */
  public boolean start(String pltFileN) {
    if(pc.dbg) {System.out.println(" - - - - - - DiagrExport");}
    this.setVisible(true);

    if(pltFileN == null || pltFileN.trim().length() <=0) {
        String msg = "Programming Error: empty or null file name in DiagrExport.";
        MsgExceptn.exception(msg);
        javax.swing.JOptionPane.showMessageDialog(this, msg, "Programming error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        closeWindow();
        return false;
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
        return false;
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
    convertedFileFullN = Div.getFileNameWithoutExtension(pltFileFullName)+"."+exportType.toLowerCase();
    convertedFile = new java.io.File(convertedFileFullN);
    jLabelOutputName.setText(convertedFile.getName());

    int k = this.getWidth() - jPanelBottom.getWidth();
    DiagrExport.this.validate();
    int j = Math.max(jPanelBottom.getWidth(), jPanelTop.getWidth());
    java.awt.Dimension d = new java.awt.Dimension(k+j, DiagrExport.this.getHeight());
    DiagrExport.this.setSize(d);

    loading = false;
    return true;

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
        jPanelType = new javax.swing.JPanel();
        jLabelFont = new javax.swing.JLabel();
        jComboBoxType = new javax.swing.JComboBox<>();
        jPanelSize = new javax.swing.JPanel();
        jLabelSize = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel_h = new javax.swing.JLabel();
        jLabel_w = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel_H = new javax.swing.JLabel();
        jLabel_W = new javax.swing.JLabel();
        jScrollBarWidth = new javax.swing.JScrollBar();
        jPanelBottom = new javax.swing.JPanel();
        jLabelOut = new javax.swing.JLabel();
        jLabelOutputName = new javax.swing.JLabel();
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

        jLabelFont.setText("Export image as:");

        jComboBoxType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxTypeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelTypeLayout = new javax.swing.GroupLayout(jPanelType);
        jPanelType.setLayout(jPanelTypeLayout);
        jPanelTypeLayout.setHorizontalGroup(
            jPanelTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTypeLayout.createSequentialGroup()
                .addComponent(jLabelFont)
                .addGap(0, 20, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelTypeLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jComboBoxType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelTypeLayout.setVerticalGroup(
            jPanelTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTypeLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabelFont)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabelSize.setText("Image size (pixels):");

        jLabel_h.setText("height:");

        jLabel_w.setText("widtht:");

        jLabel_H.setText("2000");

        jLabel_W.setText("3000");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_W, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .addComponent(jLabel_H, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel_H)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addComponent(jLabel_W))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_h)
                    .addComponent(jLabel_w))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel_h)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel_w))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jScrollBarWidth.setMaximum(2010);
        jScrollBarWidth.setMinimum(20);
        jScrollBarWidth.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        jScrollBarWidth.setValue(1000);
        jScrollBarWidth.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                jScrollBarWidthAdjustmentValueChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanelSizeLayout = new javax.swing.GroupLayout(jPanelSize);
        jPanelSize.setLayout(jPanelSizeLayout);
        jPanelSizeLayout.setHorizontalGroup(
            jPanelSizeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabelSize)
            .addGroup(jPanelSizeLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(jPanelSizeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollBarWidth, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        jPanelSizeLayout.setVerticalGroup(
            jPanelSizeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSizeLayout.createSequentialGroup()
                .addComponent(jLabelSize)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollBarWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        jButtonDoIt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/spana/images/Icon-Export_24x24.gif"))); // NOI18N
        jButtonDoIt.setMnemonic('e');
        jButtonDoIt.setText("export to ...");
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
                    .addComponent(jPanelBottom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelTop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonDoIt)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanelType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jPanelSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelTop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelSize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelBottom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonDoIt)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

  //<editor-fold defaultstate="collapsed" desc="Events">

    private void jButtonDoItActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDoItActionPerformed
        cancel = false;
        pd.diagrExportType = exportType;
        pd.diagrExportSize = exportSize;
        closeWindow();
    }//GEN-LAST:event_jButtonDoItActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeWindow();
    }//GEN-LAST:event_formWindowClosing

    private void jScrollBarWidthAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_jScrollBarWidthAdjustmentValueChanged
        exportSize = jScrollBarWidth.getValue();
        if(height2width < 1) {
            jLabel_W.setText(String.valueOf((int)((double)exportSize)));
            jLabel_H.setText(String.valueOf((int)((double)exportSize*height2width)));
        } else {
            jLabel_W.setText(String.valueOf((int)((double)exportSize/height2width)));
            jLabel_H.setText(String.valueOf((int)((double)exportSize)));
        }
    }//GEN-LAST:event_jScrollBarWidthAdjustmentValueChanged

    private void jComboBoxTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTypeActionPerformed
        if(!loading) {
            exportType = jComboBoxType.getSelectedItem().toString();
            jButtonDoIt.setText("export to "+exportType.toUpperCase());
            //---- get the file name after conversion
            convertedFileFullN = Div.getFileNameWithoutExtension(pltFileFullName)+"."+exportType.toLowerCase();
            convertedFile = new java.io.File(convertedFileFullN);
            jLabelOutputName.setText(convertedFile.getName());
        }
    }//GEN-LAST:event_jComboBoxTypeActionPerformed

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

  public final void closeWindow() {
    finished = true;
    this.setVisible(false);
    this.dispose();
    this.notify_All();
  } // closeWindow()

  private synchronized void notify_All() {notifyAll();}

  /** this method will wait for this dialog frame to be closed
   * @return "cancel" */
  public synchronized boolean waitFor() {
    while(!finished) {try {wait();} catch (InterruptedException ex) {}}
    return cancel;
  } // waitFor()

  //</editor-fold>

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupO;
    private javax.swing.JButton jButtonDoIt;
    private javax.swing.JComboBox<String> jComboBoxType;
    private javax.swing.JLabel jLabelDir;
    private javax.swing.JLabel jLabelDirName;
    private javax.swing.JLabel jLabelFont;
    private javax.swing.JLabel jLabelOut;
    private javax.swing.JLabel jLabelOutputName;
    private javax.swing.JLabel jLabelPltFile;
    private javax.swing.JLabel jLabelPltFileName;
    private javax.swing.JLabel jLabelSize;
    private javax.swing.JLabel jLabel_H;
    private javax.swing.JLabel jLabel_W;
    private javax.swing.JLabel jLabel_h;
    private javax.swing.JLabel jLabel_w;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelBottom;
    private javax.swing.JPanel jPanelSize;
    private javax.swing.JPanel jPanelTop;
    private javax.swing.JPanel jPanelType;
    private javax.swing.JScrollBar jScrollBarWidth;
    // End of variables declaration//GEN-END:variables
}
