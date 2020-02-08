package database;

import lib.database.ProgramDataDB;
import lib.huvud.ProgramConf;

/** Temperature dialog.
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
public class SetTempPressDialog extends javax.swing.JDialog {
  // Note: for java 1.6 jComboBox must not have type,
  //       for java 1.7 jComboBox must be <String>
  
  private boolean loading = true;
  private ProgramConf pc;
  private final ProgramDataDB pd;
  private final FrameDBmain dbF;
  private final java.awt.Dimension windowSize; // = new java.awt.Dimension(185,185);
  private double temperature_C;
  private double pressure_bar;
  private final String[] pSat = new String[] {"Psat","500","1000","2000","3000","4000","5000"};
  private final String[] noPsat =  new String[] {"500","1000","2000","3000","4000","5000"};
  private final String[] tAll = new String[] {"0","10","20","25","30","40","50","75","100","125","150","175","200","225","250","275","300","325","350","400","450","500","550","600"};
  //private final String[] t450 = new String[] {"0","10","20","25","30","40","50","75","100","125","150","175","200","225","250","275","300","325","350","400","450"};
  private final String[] t100 = new String[] {"0","5","10","15","20","25","30","35","40","50","60","70","75","80","90","100"};
  /** New-line character(s) to substitute "\n" */
  private static final String nl = System.getProperty("line.separator");

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /** Creates new form SetTempDialog
   * @param parent
   * @param modal
   * @param pc0
   * @param pd0  */
  public SetTempPressDialog(java.awt.Frame parent, boolean modal,
            ProgramConf pc0,
            ProgramDataDB pd0
          ) {
    super(parent, modal);
    initComponents();
    pc = pc0;
    pd = pd0;
    dbF = (FrameDBmain)parent;
    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    //--- Close window on ESC key
    javax.swing.KeyStroke escKeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(escKeyStroke,"ESCAPE");
    javax.swing.Action escAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            closeWindow();
        }};
    getRootPane().getActionMap().put("ESCAPE", escAction);
    //--- Alt-Q quit
    javax.swing.KeyStroke altQKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altQKeyStroke,"ALT_Q");
    javax.swing.Action altQAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            closeWindow();
        }};
    getRootPane().getActionMap().put("ALT_Q", altQAction);
    //--- Alt-X eXit
    javax.swing.KeyStroke altXKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altXKeyStroke,"ALT_X");
    javax.swing.Action altXAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jButtonOK.doClick();
        }};
    getRootPane().getActionMap().put("ALT_X", altXAction);
    //--- F1 for help
    javax.swing.KeyStroke f1KeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_F1,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(f1KeyStroke,"F1");
    javax.swing.Action f1Action = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            SetTempPressDialog.this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            Thread hlp = new Thread() {@Override public void run(){
                String[] a = {"DB_0_Main_htm"};
                lib.huvud.RunProgr.runProgramInProcess(null,ProgramConf.HELP_JAR,a,false,pc.dbg,pc.pathAPP);
                try{Thread.sleep(2000);}   //show the "wait" cursor for 2 sec
                catch (InterruptedException e) {}
                SetTempPressDialog.this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }   };//new Thread
            hlp.start();
        }};
    getRootPane().getActionMap().put("F1", f1Action);
    //--- Alt-H ghelp
    javax.swing.KeyStroke altHKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altHKeyStroke,"ALT_H");
    getRootPane().getActionMap().put("ALT_H", f1Action);
    //---- forward/backwards arrow keys
    java.util.Set<java.awt.AWTKeyStroke> keys = getFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
    java.util.Set<java.awt.AWTKeyStroke> newKeys = new java.util.HashSet<java.awt.AWTKeyStroke>(keys);
    newKeys.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0));
    //newKeys.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, 0));
    setFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,newKeys);

    keys = getFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
    newKeys = new java.util.HashSet<java.awt.AWTKeyStroke>(keys);
    newKeys.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0));
    //newKeys.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, 0));
    setFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,newKeys);
    //---- Title, etc
    this.setTitle("  Temperature:");

    //---- Centre window on parent/screen
    int left,top;
    java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    if(parent != null) {
        left = Math.max(0,(parent.getX() + (parent.getWidth()/2) - this.getWidth()/2));
        top = Math.max(0,(parent.getY()+(parent.getHeight()/2)-this.getHeight()/2));
    } else {
        left = Math.max(0,(screenSize.width-this.getWidth())/2);
        top = Math.max(0,(screenSize.height-this.getHeight())/2);
    }
    this.setLocation(Math.min(screenSize.width-this.getWidth()-20,left),
                     Math.min(screenSize.height-this.getHeight()-20, top));
    windowSize = new java.awt.Dimension(this.getWidth(),this.getHeight());
    temperature_C = pd.temperature_C;
    pressure_bar = pd.pressure_bar;
    if(pd.temperatureAllowHigher) { // up to 600 C
        //jComboBoxT.setModel(new javax.swing.DefaultComboBoxModel( // java 1.6
//        if(pressure_bar == 500) {
//            jComboBoxT.setModel(new javax.swing.DefaultComboBoxModel<String>(t450));
//        } else {
            jComboBoxT.setModel(new javax.swing.DefaultComboBoxModel<String>(tAll));
//        }
        jComboBoxP.setEnabled(true);
    } else { // only 0 to 100 C
        // jComboBoxT.setModel(new javax.swing.DefaultComboBoxModel( // java 1.6
        jComboBoxT.setModel(new javax.swing.DefaultComboBoxModel<String>(t100));
        jComboBoxP.setEnabled(false);
    }
    if(temperature_C > 350) {
        jComboBoxP.setModel(new javax.swing.DefaultComboBoxModel<String>(noPsat));
    } else {
        jComboBoxP.setModel(new javax.swing.DefaultComboBoxModel<String>(pSat));
    }
    set_temp_inComboBox();
    set_press_inComboBox();
    if(pc.dbg) {
        System.out.println("---- SetTempPressDialog"+nl+
                           "     temperature = "+temperature_C+nl+
                           "     pressure = "+pressure_bar);
    }
    pack();
    loading = false;
    setVisible(true);
  }
  //</editor-fold>

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButtonOK = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jComboBoxT = new javax.swing.JComboBox<String>();
        jLabelDegrees = new javax.swing.JLabel();
        jLabelT = new javax.swing.JLabel();
        jComboBoxP = new javax.swing.JComboBox<String>();
        jLabelP = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        jButtonOK.setMnemonic('O');
        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });

        jButtonCancel.setMnemonic('C');
        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        jComboBoxT.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "5", "10", "15", "20", "25", "30", "35", "40", "50", "60", "70", "75", "80", "90", "100" }));
        jComboBoxT.setSelectedIndex(5);
        jComboBoxT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxTActionPerformed(evt);
            }
        });

        jLabelDegrees.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelDegrees.setLabelFor(jComboBoxT);
        jLabelDegrees.setText("°C");
        jLabelDegrees.setToolTipText("double-click to set T=25'C");
        jLabelDegrees.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelDegreesMouseClicked(evt);
            }
        });

        jLabelT.setIcon(new javax.swing.ImageIcon(getClass().getResource("/database/images/Termometer.gif"))); // NOI18N

        jComboBoxP.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Psat", "500", "1000", "2000", "3000", "4000", "5000" }));
        jComboBoxP.setToolTipText("Psat = vapor-liquid equilibrium (saturation) pressure");
        jComboBoxP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxPActionPerformed(evt);
            }
        });

        jLabelP.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelP.setText("bar");
        jLabelP.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelPMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jLabelT)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jComboBoxT, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jComboBoxP, 0, 65, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelDegrees)
                    .addComponent(jLabelP))
                .addContainerGap(40, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelDegrees))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelP))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabelT)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(jButtonOK)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonCancel)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonOK)
                    .addComponent(jButtonCancel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

  //<editor-fold defaultstate="collapsed" desc="Events">
    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        closeWindow();
    }//GEN-LAST:event_jButtonCancelActionPerformed

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

    private void jComboBoxTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTActionPerformed
      if(loading) {return;}
      double old;
      temperature_C =  Double.parseDouble(jComboBoxT.getSelectedItem().toString());
      if(temperature_C > lib.kemi.H2O.IAPWSF95.CRITICAL_TC) {
        jComboBoxP.setModel(new javax.swing.DefaultComboBoxModel<String>(noPsat));
        if(temperature_C <= 450 && pressure_bar < 500) {pressure_bar = 500;}
        if(temperature_C > 450 && pressure_bar < 1000) {pressure_bar = 1000;}
      } else { // temperature <= 373
        jComboBoxP.setModel(new javax.swing.DefaultComboBoxModel<String>(pSat));
        if(pressure_bar < 500) {
            pressure_bar = Math.max(1.,lib.kemi.H2O.IAPWSF95.pSat(temperature_C));
        }
      }
      set_press_inComboBox();
    }//GEN-LAST:event_jComboBoxTActionPerformed

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
      int answer;
      if(pressure_bar == 500 && temperature_C > 450) {
          System.out.println("ButtonOK: Pbar = "+pressure_bar+", tC = "+temperature_C);
          answer = javax.swing.JOptionPane.showConfirmDialog(this,
                  "<html><b>Note:</b> at 500 bar the temperature<br>"
                    +"range is limited to 0 - 450C°C.<br><br>"
                    +"Change the pressure to 1000 bar?</html>",
                  pc.progName, javax.swing.JOptionPane.OK_CANCEL_OPTION,
                                    javax.swing.JOptionPane.WARNING_MESSAGE);
          if(answer != javax.swing.JOptionPane.OK_OPTION) {
              System.out.println("Answer: Cancel");
              return;
          }
          pressure_bar = 1000;
      }
      pd.temperature_C = temperature_C;
      pd.pressure_bar = pressure_bar;
      closeWindow();
    }//GEN-LAST:event_jButtonOKActionPerformed

    private void jLabelDegreesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelDegreesMouseClicked
        if(evt.getClickCount() >1) { // double-click
            temperature_C = 25;
            for(int i = 0; i < jComboBoxT.getItemCount();i++) {
                if(jComboBoxT.getItemAt(i).equals("25")) {jComboBoxT.setSelectedIndex(i); break;}
            }
        }
    }//GEN-LAST:event_jLabelDegreesMouseClicked

    private void jComboBoxPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxPActionPerformed
        if(loading) {return;}
        double pOld = pressure_bar;
        String s = jComboBoxP.getSelectedItem().toString();
        if(s.equalsIgnoreCase("psat")) {
            temperature_C = Math.min(350, temperature_C);
            pressure_bar = Math.max(1.,lib.kemi.H2O.IAPWSF95.pSat(temperature_C));
            set_temp_inComboBox();
        }
        else {pressure_bar =  Double.parseDouble(s);}
        if(pressure_bar != 500 && pOld == 500) {
            jComboBoxT.setModel(new javax.swing.DefaultComboBoxModel<String>(tAll));
            set_temp_inComboBox();
        }
//        if(pressure_bar == 500 && pOld != 500) {
//            temperature_C = Math.min(450, temperature_C);
//            jComboBoxT.setModel(new javax.swing.DefaultComboBoxModel<String>(t450));
//            set_temp_inComboBox();
//        }
    }//GEN-LAST:event_jComboBoxPActionPerformed

    private void jLabelPMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelPMouseClicked
        if(temperature_C > 350) {return;}
        if(evt.getClickCount() >1) { // double-click
            jComboBoxP.setSelectedIndex(0);
            pressure_bar = Math.max(1.,lib.kemi.H2O.IAPWSF95.pSat(temperature_C));
        }
    }//GEN-LAST:event_jLabelPMouseClicked
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Methods">

  private void closeWindow() {
    this.dispose();
    dbF.bringToFront();
  } // closeWindow()

//<editor-fold defaultstate="collapsed" desc="set_temp_inComboBox">
/** find the closest item in the temperature combo box and select it */
  private void set_temp_inComboBox() {
    double w0, w1;
    int listItem =0;
    int listCount = jComboBoxT.getItemCount();
    for(int i =1; i < listCount; i++) {
      w0 = Double.parseDouble(jComboBoxT.getItemAt(i-1).toString());
      w1 = Double.parseDouble(jComboBoxT.getItemAt(i).toString());
      if(temperature_C <= w0 && i==1) {listItem = 0; break;}
      if(temperature_C >= w1 && i==(listCount-1)) {listItem = (listCount-1); break;}
      if(temperature_C > w0 && temperature_C <=w1) {
        if(Math.abs(temperature_C-w0) < Math.abs(temperature_C-w1)) {
            listItem = i-1;
        } else {
            listItem = i;
        }
        break;
      }
    } //for i
    jComboBoxT.setSelectedIndex(listItem);
  } //set_tol_inComboBox()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="set_press_inComboBox">
/** find the closest item in the pressure combo box and select it */
  private void set_press_inComboBox() {
    if(pressure_bar < lib.kemi.H2O.IAPWSF95.CRITICAL_pBar) {jComboBoxP.setSelectedIndex(0); return;}
    int min = 1;
    if(jComboBoxP.getItemAt(0).toString().equalsIgnoreCase("pSat")) {
        if(pressure_bar < lib.kemi.H2O.IAPWSF95.CRITICAL_pBar) {jComboBoxP.setSelectedIndex(0); return;}
        min = 2;
    }
    double w0, w1;
    int listItem =0;
    int listCount = jComboBoxP.getItemCount();
    for(int i = min; i < listCount; i++) {
      w0 = Double.parseDouble(jComboBoxP.getItemAt(i-1).toString());
      w1 = Double.parseDouble(jComboBoxP.getItemAt(i).toString());
      if(pressure_bar <= w0 && i == min) {listItem = min-1; break;}
      if(pressure_bar >= w1 && i == (listCount-1)) {listItem = (listCount-1); break;}
      if(pressure_bar > w0 && pressure_bar <=w1) {
        if(Math.abs(pressure_bar-w0) < Math.abs(pressure_bar-w1)) {
            listItem = i-1;
        } else {
            listItem = i;
        }
        break;
      }
    } //for i
    jComboBoxP.setSelectedIndex(listItem);
  } //set_tol_inComboBox()
//</editor-fold>

  //</editor-fold>


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JComboBox<String> jComboBoxP;
    private javax.swing.JComboBox<String> jComboBoxT;
    private javax.swing.JLabel jLabelDegrees;
    private javax.swing.JLabel jLabelP;
    private javax.swing.JLabel jLabelT;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
