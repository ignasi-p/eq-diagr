package database;

import lib.database.ProgramDataDB;
import lib.huvud.ProgramConf;

/** Ask the user about what redox equilibria to be included
 * in the database searchs.
 * <br>
 * Copyright (C) 2018-2020 I.Puigdomenech
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
public class AskSolids extends javax.swing.JDialog {
  private final ProgramConf pc;
  private final ProgramDataDB pd;
  private final FrameDBmain dbF;
  private boolean ok = false;
  private final boolean askingBeforeSearch;
  private java.awt.Dimension windowSize = new java.awt.Dimension(227,122);

 /** Creates new form AskRedox
  * @param parent
  * @param modal
  * @param pc0 program configuration
  * @param pd0 program data
  * @param aBS true if "askingBeforeSearch";
  *     false if asking the user for program options */
  public AskSolids(java.awt.Frame parent, boolean modal,
          ProgramConf pc0,
          ProgramDataDB pd0,
          boolean aBS) {
    super(parent, modal);
    initComponents();
    pc = pc0;
    pd = pd0;
    dbF = (FrameDBmain)parent;
    askingBeforeSearch = aBS;

    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    // ---- Close window on ESC key
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
    getRootPane().getActionMap().put("ALT_Q", escAction);
    //--- Alt-X eXit
    javax.swing.KeyStroke altXKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altXKeyStroke,"ALT_X");
    javax.swing.Action altXAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jButtonOK.doClick();
        }};
    getRootPane().getActionMap().put("ALT_X", altXAction);
    // ---- F1 for help
    javax.swing.KeyStroke f1KeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_F1,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(f1KeyStroke,"F1");
    javax.swing.Action f1Action = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jButtonHelp.doClick();
        }};
    getRootPane().getActionMap().put("F1", f1Action);
    //---- forward/backwards arrow keys
    java.util.Set<java.awt.AWTKeyStroke> keys = getFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
    java.util.Set<java.awt.AWTKeyStroke> newKeys = new java.util.HashSet<java.awt.AWTKeyStroke>(keys);
    newKeys.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0));
    newKeys.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, 0));
    setFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,newKeys);

    keys = getFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
    newKeys = new java.util.HashSet<java.awt.AWTKeyStroke>(keys);
    newKeys.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0));
    newKeys.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, 0));
    setFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,newKeys);

    //---- Title, etc
    this.setTitle(pc.progName+" - Solid options");

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

    //  0=include all solids; 1=exclude (cr); 2=exclude (c); 3=exclude (cr)&(c)

    if(pd.allSolids == 1) {jRadioButton1.setSelected(true);}
    else if(pd.allSolids == 2) {jRadioButton2.setSelected(true);}
    else if(pd.allSolids == 3) {jRadioButton3.setSelected(true);}
    else {jRadioButtonAll.setSelected(true);}
    if(pd.allSolidsAsk) {
        jRadioButtonAsk.setSelected(true);
        jRadioButtonAll.setEnabled(false);
        jRadioButton1.setEnabled(false);
        jRadioButton2.setEnabled(false);
        jRadioButton3.setEnabled(false);
    } else {
        jRadioButtonDoNotAsk.setSelected(true);
        jRadioButtonAll.setEnabled(true);
        jRadioButton1.setEnabled(true);
        jRadioButton2.setEnabled(true);
        jRadioButton3.setEnabled(true);
    }

    if(askingBeforeSearch) {
      jRadioButtonAll.setEnabled(true);
      jRadioButton1.setEnabled(true);
      jRadioButton2.setEnabled(true);
      jRadioButton3.setEnabled(true);
      jRadioButtonAsk.setVisible(false);
      jRadioButtonDoNotAsk.setVisible(false);
    } //if askingBeforeSearch

  } //constructor

  public void start() {
    this.setVisible(true);
    windowSize = AskSolids.this.getSize();
    jButtonCancel.requestFocusInWindow();
    dbF.setCursorDef();
  }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jLabelTop = new javax.swing.JLabel();
        jButtonHelp = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jButtonOK = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jRadioButtonAsk = new javax.swing.JRadioButton();
        jRadioButtonDoNotAsk = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        jRadioButtonAll = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();

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

        jLabelTop.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelTop.setText("<html>If solids are found when searching the database:<br>include all types of solids?</html>");

        jButtonHelp.setMnemonic('h');
        jButtonHelp.setText("Help");
        jButtonHelp.setToolTipText("Alt-H or F1");
        jButtonHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHelpActionPerformed(evt);
            }
        });

        jButtonCancel.setMnemonic('c');
        jButtonCancel.setText("Cancel");
        jButtonCancel.setToolTipText("Alt-C or Esc");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        jButtonOK.setMnemonic('o');
        jButtonOK.setText("OK");
        jButtonOK.setToolTipText("Alt-O");
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButtonAsk);
        jRadioButtonAsk.setMnemonic('A');
        jRadioButtonAsk.setText("Ask before every database search");
        jRadioButtonAsk.setToolTipText("Alt-A");
        jRadioButtonAsk.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        jRadioButtonAsk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonAskActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButtonDoNotAsk);
        jRadioButtonDoNotAsk.setMnemonic('d');
        jRadioButtonDoNotAsk.setText("Do not ask. Include the following type of solids:");
        jRadioButtonDoNotAsk.setToolTipText("Alt-D");
        jRadioButtonDoNotAsk.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        jRadioButtonDoNotAsk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonDoNotAskActionPerformed(evt);
            }
        });

        buttonGroup2.add(jRadioButtonAll);
        jRadioButtonAll.setMnemonic('0');
        jRadioButtonAll.setText("All solids included");
        jRadioButtonAll.setToolTipText("Alt-0");

        buttonGroup2.add(jRadioButton3);
        jRadioButton3.setMnemonic('3');
        jRadioButton3.setText("Exclude solids endng with either \"(cr)\" or \"(c)\"");
        jRadioButton3.setToolTipText("Alt-3");

        buttonGroup2.add(jRadioButton1);
        jRadioButton1.setMnemonic('1');
        jRadioButton1.setText("Exclude solids endng with \"(cr)\"");
        jRadioButton1.setToolTipText("Alt-1");

        buttonGroup2.add(jRadioButton2);
        jRadioButton2.setMnemonic('2');
        jRadioButton2.setText("Exclude solids endng with \"(c)\"");
        jRadioButton2.setToolTipText("Alt-2");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButtonAll)
                    .addComponent(jRadioButton3)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jRadioButtonAll)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButtonAsk)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jRadioButtonDoNotAsk))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jRadioButtonAsk)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButtonDoNotAsk)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelTop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonCancel)
                            .addComponent(jButtonHelp)
                            .addComponent(jButtonOK, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelTop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(58, 58, 58)
                        .addComponent(jButtonHelp)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonOK)
                        .addContainerGap(30, Short.MAX_VALUE))
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
      if(windowSize != null) {
        int w = windowSize.width;
        int h = windowSize.height;
        if(this.getHeight()<h){this.setSize(this.getWidth(), h);}
        if(this.getWidth()<w){this.setSize(w,this.getHeight());}
      }
    }//GEN-LAST:event_formComponentResized

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeWindow();
    }//GEN-LAST:event_formWindowClosing

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
        if(jRadioButton1.isSelected()) {pd.allSolids = 1;}
        else if(jRadioButton2.isSelected()) {pd.allSolids = 2;}
        else if(jRadioButton3.isSelected()) {pd.allSolids = 3;}
        else {pd.allSolids = 0;}
        pd.allSolidsAsk = jRadioButtonAsk.isSelected();
        ok = true;
        closeWindow();
    }//GEN-LAST:event_jButtonOKActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        closeWindow();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHelpActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        Thread hlp = new Thread() {@Override public void run(){
            String[] a = {"DB_Solid_solub_htm"};
            lib.huvud.RunProgr.runProgramInProcess(null,ProgramConf.HELP_JAR,a,false,pc.dbg,pc.pathAPP);
            try{Thread.sleep(2000);}   //show the "wait" cursor for 2 sec
            catch (InterruptedException e) {}
            setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }};//new Thread
        hlp.start();
    }//GEN-LAST:event_jButtonHelpActionPerformed

    private void jRadioButtonAskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonAskActionPerformed
        jRadioButtonAll.setEnabled(false);
        jRadioButton1.setEnabled(false);
        jRadioButton2.setEnabled(false);
        jRadioButton3.setEnabled(false);
    }//GEN-LAST:event_jRadioButtonAskActionPerformed

    private void jRadioButtonDoNotAskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonDoNotAskActionPerformed
        jRadioButtonAll.setEnabled(true);
        jRadioButton1.setEnabled(true);
        jRadioButton2.setEnabled(true);
        jRadioButton3.setEnabled(true);
    }//GEN-LAST:event_jRadioButtonDoNotAskActionPerformed

  private void closeWindow() {
    dbF.exitCancel = !ok;
    this.dispose();
    dbF.bringToFront();
  } // closeWindow()

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonHelp;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JLabel jLabelTop;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButtonAll;
    private javax.swing.JRadioButton jRadioButtonAsk;
    private javax.swing.JRadioButton jRadioButtonDoNotAsk;
    // End of variables declaration//GEN-END:variables
}
