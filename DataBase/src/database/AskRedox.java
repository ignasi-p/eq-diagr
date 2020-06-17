package database;

import lib.database.ProgramDataDB;
import lib.huvud.ProgramConf;

/** Ask the user about what redox equilibria to be included
 * in the database searchs.
 * <br>
 * Copyright (C) 2014-2020 I.Puigdomenech
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
public class AskRedox extends javax.swing.JDialog {
  boolean askSomething = false;
  private ProgramConf pc;
  private ProgramDataDB pd;
  private FrameDBmain dbF;
  private boolean ok = false;
  private boolean askingBeforeSearch;
  private java.awt.Dimension windowSize = new java.awt.Dimension(227,122);

 /** Creates new form AskRedox
  * @param parent
  * @param modal
  * @param pc0 program configuration
  * @param pd0 program data
  * @param aBS true if "askingBeforeSearch";
  *     false if asking the user for program options
  * @param model the selected components, or null */
  public AskRedox(java.awt.Frame parent, boolean modal,
          ProgramConf pc0,
          ProgramDataDB pd0,
          boolean aBS,
          javax.swing.DefaultListModel model) {
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
    this.setTitle(pc.progName+" - Redox options");

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

    if(pd.redoxN) {jCheckBoxN.setSelected(true);}
    if(pd.redoxP) {jCheckBoxP.setSelected(true);}
    if(pd.redoxS) {jCheckBoxS.setSelected(true);}
    if(pd.redoxAsk) {
        jRadioButtonAsk.setSelected(true);
        jCheckBoxN.setEnabled(false);
        jCheckBoxP.setEnabled(false);
        jCheckBoxS.setEnabled(false);
    } else {
        jRadioButtonDoNotAsk.setSelected(true);
        jCheckBoxN.setEnabled(true);
        jCheckBoxP.setEnabled(true);
        jCheckBoxS.setEnabled(true);
    }
    jLabelMessage.setVisible(false);
    // if asking before a database search: askSomething =false
    askSomething = !askingBeforeSearch;
    if(askingBeforeSearch) {
      // find out which elements "N", "P", or "S" the user selected
      String[] elemComp; String selCompName; String el;
      for(int i =0; i< model.size(); i++) {
        selCompName = model.get(i).toString();
        if(selCompName.equals("SCN-")) {continue;}
        for (String[] elemComp1 : pd.elemComp) {
            //loop through all components in the database (CO3-2,SO4-2,HS-,etc)
            //Note: array elemComp[0] contains: the name-of-the-element (e.g. "C"),
            //  the formula-of-the-component ("CN-"), and
            //  the name-of-the-component ("cyanide")
            elemComp = elemComp1;
            if(!elemComp[1].equals(selCompName)) {continue;}
            //Got the component selected by the user
            el = elemComp[0]; //get the element corresponding to the component: e.g. "S" for SO4-2
            if(el.equals("N")) {askSomething = true; jCheckBoxN.setEnabled(true);}
            if(el.equals("S")) {askSomething = true; jCheckBoxS.setEnabled(true);}
            if(el.equals("P")) {askSomething = true; jCheckBoxP.setEnabled(true);}
        } //for  list of all available components
      } //for i;   all selected components
      if(!askSomething) {closeWindow(); return;}
      jLabelTop.setText("<html>\"e-\" has been selected:<br>"+
            "include redox equilibria among ligands?<br>&nbsp;</html>");
      jLabelMessage.setVisible(true);
      jRadioButtonAsk.setVisible(false);
      jRadioButtonDoNotAsk.setVisible(false);
    } //if askingBeforeSearch

  } //constructor

  public void start() {
    this.setVisible(true);
    windowSize = AskRedox.this.getSize();
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
        jLabelTop = new javax.swing.JLabel();
        jButtonHelp = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jButtonOK = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jRadioButtonAsk = new javax.swing.JRadioButton();
        jRadioButtonDoNotAsk = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        jCheckBoxN = new javax.swing.JCheckBox();
        jCheckBoxS = new javax.swing.JCheckBox();
        jCheckBoxP = new javax.swing.JCheckBox();
        jLabelMessage = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
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

        jLabelTop.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelTop.setText("<html>If \"e-\" is selected:<br>include redox equilibria among ligands?</html>");

        jButtonHelp.setMnemonic('h');
        jButtonHelp.setText(" Help ");
        jButtonHelp.setToolTipText("Alt-H or F1");
        jButtonHelp.setAlignmentX(0.5F);
        jButtonHelp.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButtonHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHelpActionPerformed(evt);
            }
        });

        jButtonCancel.setMnemonic('c');
        jButtonCancel.setText(" Cancel ");
        jButtonCancel.setToolTipText("Alt-C or Esc");
        jButtonCancel.setAlignmentX(0.5F);
        jButtonCancel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        jButtonOK.setMnemonic('o');
        jButtonOK.setText("OK");
        jButtonOK.setToolTipText("Alt-O");
        jButtonOK.setAlignmentX(0.5F);
        jButtonOK.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButtonAsk);
        jRadioButtonAsk.setMnemonic('A');
        jRadioButtonAsk.setText("Ask every time \"e-\" is selected");
        jRadioButtonAsk.setToolTipText("Alt-A");
        jRadioButtonAsk.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        jRadioButtonAsk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonAskActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButtonDoNotAsk);
        jRadioButtonDoNotAsk.setMnemonic('d');
        jRadioButtonDoNotAsk.setText("Do not ask. Include the following equilibria:");
        jRadioButtonDoNotAsk.setToolTipText("Alt-D");
        jRadioButtonDoNotAsk.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        jRadioButtonDoNotAsk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonDoNotAskActionPerformed(evt);
            }
        });

        jCheckBoxN.setMnemonic('n');
        jCheckBoxN.setText("N (NO3-, NO2-, NH3, etc)");
        jCheckBoxN.setToolTipText("Alt-N");

        jCheckBoxS.setMnemonic('s');
        jCheckBoxS.setText("S (SO4-2, SO3-2, HS-, etc)");
        jCheckBoxS.setToolTipText("Alt-S");

        jCheckBoxP.setMnemonic('p');
        jCheckBoxP.setText("P (PO4-3, HPO3-2, H2PO2-, etc)");
        jCheckBoxP.setToolTipText("Alt-P");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxN)
                    .addComponent(jCheckBoxS)
                    .addComponent(jCheckBoxP))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jCheckBoxN)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxP)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxS))
        );

        jLabelMessage.setText("<html>Note: some of the selected components might not participate in redox reactions</html>");
        jLabelMessage.setVerticalAlignment(javax.swing.SwingConstants.TOP);

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
                .addContainerGap(10, Short.MAX_VALUE))
            .addComponent(jLabelMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jRadioButtonAsk)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButtonDoNotAsk)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButtonCancel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonHelp)
                            .addComponent(jButtonOK, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
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
                        .addGap(59, 59, 59)
                        .addComponent(jButtonHelp)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonOK)
                        .addContainerGap(44, Short.MAX_VALUE))
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
        pd.redoxAsk = jRadioButtonAsk.isSelected();
        pd.redoxN = jCheckBoxN.isSelected();
        pd.redoxP = jCheckBoxP.isSelected();
        pd.redoxS = jCheckBoxS.isSelected();
        ok = true;
        closeWindow();
    }//GEN-LAST:event_jButtonOKActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        closeWindow();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHelpActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        Thread hlp = new Thread() {@Override public void run(){
            String[] a = {"DB_0_Main_htm"};
            lib.huvud.RunProgr.runProgramInProcess(null,ProgramConf.HELP_JAR,a,false,pc.dbg,pc.pathAPP);
            try{Thread.sleep(2000);}   //show the "wait" cursor for 2 sec
            catch (InterruptedException e) {}
            setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }};//new Thread
        hlp.start();
    }//GEN-LAST:event_jButtonHelpActionPerformed

    private void jRadioButtonAskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonAskActionPerformed
        jCheckBoxN.setEnabled(false);
        jCheckBoxP.setEnabled(false);
        jCheckBoxS.setEnabled(false);
    }//GEN-LAST:event_jRadioButtonAskActionPerformed

    private void jRadioButtonDoNotAskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonDoNotAskActionPerformed
        jCheckBoxN.setEnabled(true);
        jCheckBoxP.setEnabled(true);
        jCheckBoxS.setEnabled(true);
    }//GEN-LAST:event_jRadioButtonDoNotAskActionPerformed

  private void closeWindow() {
    dbF.exitCancel = !ok;
    this.dispose();
    dbF.bringToFront();
  } // closeWindow()

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonHelp;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JCheckBox jCheckBoxN;
    private javax.swing.JCheckBox jCheckBoxP;
    private javax.swing.JCheckBox jCheckBoxS;
    private javax.swing.JLabel jLabelMessage;
    private javax.swing.JLabel jLabelTop;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JRadioButton jRadioButtonAsk;
    private javax.swing.JRadioButton jRadioButtonDoNotAsk;
    // End of variables declaration//GEN-END:variables
}
