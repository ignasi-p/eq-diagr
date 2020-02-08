package spana;

import lib.huvud.ProgramConf;
import lib.kemi.chem.Chem;
import lib.kemi.readWriteDataFiles.ReadChemSyst;

/** Confirm dialog.
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
public class ModifyConfirm extends javax.swing.JFrame {
    private java.awt.Dimension windowSize;
    private boolean finished = false;
    private boolean cancel = true;
    private boolean dbg = true;
    private Chem ch = null;
    private Chem.ChemSystem cs = null;
    private Chem.ChemSystem.NamesEtc namn = null;
    private javax.swing.DefaultListModel<String> listSolubleCmplxModel = new javax.swing.DefaultListModel<>();
    // private javax.swing.DefaultListModel listSolubleCmplxModel = new javax.swing.DefaultListModel(); // java 1.6
    private javax.swing.DefaultListModel<String> listSolidCmplxModel = new javax.swing.DefaultListModel<>();
    // private javax.swing.DefaultListModel listSolidCmplxModel = new javax.swing.DefaultListModel(); // java 1.6
    private int compToDelOrExch;
    private boolean delete;
    private final static String nl = System.getProperty("line.separator");

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /** Creates new form ModifyConfirm, used when a component is to be either deleted
   * or exchanged with a reaction product
   * @param parentPosition
   * @param chem an instance of a Chem storage class
   * @param compToDelOrExch the component to delete or to exchange
   * @param complxToExchange if the user wishes to exchange the component:
   * the reaction product initially selected by the user; if there is not initial
   * selection then complxToExchange = -1. If the user wishes to delete the
   * reaction then complxToExchange &lt; -1.
   * @param pc program configuration data */
  public ModifyConfirm(java.awt.Point parentPosition,
            Chem chem, int compToDelOrExch, int complxToExchange,
            final ProgramConf pc) {
    //--- set the reference pointing to instance of storage class
    if(chem == null) {
        System.err.println("Programming error in ModifyConfirm:  \"ch\" is null.");
        return;}
    ch = chem;
    cs = ch.chemSystem;
    namn = cs.namn;
    if(cs == null || namn == null) {
        System.err.println("Programming error in ModifyConfirm: either \"cs\" or \"namn\" are null.");
        return;}
    if(compToDelOrExch < 0 || compToDelOrExch >= cs.Na) {
        System.err.println("Programming error in ModifyConfirm: \"compToDelOrExch\" = "+compToDelOrExch);
        return;}
    this.compToDelOrExch = compToDelOrExch;
    initComponents();
    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

    //set Window on Screen
    this.setLocation(parentPosition);
    //--- close window on ESC key
    javax.swing.KeyStroke escKeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(escKeyStroke,"ESCAPE");
    javax.swing.Action escAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            quitFrame();
        }};
    getRootPane().getActionMap().put("ESCAPE", escAction);
    //--- F1 for help
    javax.swing.KeyStroke f1KeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(f1KeyStroke,"F1");
    javax.swing.Action f1Action = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            ModifyConfirm.this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            Thread hlp = new Thread() {@Override public void run(){
                String[] a = {"S_Modify_Chem_System_htm"};
                lib.huvud.RunProgr.runProgramInProcess(ModifyConfirm.this,ProgramConf.HELP_JAR,a,false,pc.dbg,pc.pathAPP);
                try{Thread.sleep(2000);}   //show the "wait" cursor for 2 sec
                catch (InterruptedException e) {}
                ModifyConfirm.this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }};//new Thread
            hlp.start();
        }};
    getRootPane().getActionMap().put("F1", f1Action);
    //--- Define Alt-keys
    //      Alt-O and Alt-Q are button mnemonics
    // Define Alt-X
    //--- Alt-X quit
    javax.swing.KeyStroke altXKeyStroke = javax.swing.KeyStroke.getKeyStroke(
    				java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altXKeyStroke,"ALT_X");
    javax.swing.Action altXAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jButtonOK.doClick();
        }};
    getRootPane().getActionMap().put("ALT_X", altXAction);
    //--- Alt-H help
    javax.swing.KeyStroke altHKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altHKeyStroke,"ALT_H");
    getRootPane().getActionMap().put("ALT_H", f1Action);

    //--- Icon
    String iconName = "images/Modify_32x32.gif";
    java.net.URL imgURL = this.getClass().getResource(iconName);
    if (imgURL != null) {this.setIconImage(new javax.swing.ImageIcon(imgURL).getImage());}
    else {System.out.println("Error: Could not load image = \""+iconName+"\"");}

    //---
    if(complxToExchange >= -1) { // Exchange a component for a reaction
        jLabelSolubleCmplx.setText("Soluble complexes:");
        jLabelSolidCmplx.setText("Solid products:");
    }
    //--- add complexes to the list
    int complxs = 0 ;
    for(int i=0; i < cs.nx; i++) {
      if(Math.abs(cs.a[i][compToDelOrExch])>1e-7) {
        listSolubleCmplxModel.addElement(namn.ident[i+cs.Na]);
        complxs++;
      }
    } //for i
    if(complxs <= 0) {
      jLabelSolubleCmplx.setText("No soluble complexes");
      jListSolubCmplx.setBackground(java.awt.Color.lightGray);
    }
    //--- add solids to the list
    int complexesS = 0;
    int i;
    for(int is=0; is < cs.mSol-cs.solidC; is++) {
      i = is +cs.nx;
      if(Math.abs(cs.a[i][compToDelOrExch])>1e-7) {
        listSolidCmplxModel.addElement(namn.ident[i+cs.Na]);
        complexesS++;
      }
    } //for i
    if(complexesS <= 0) {
      jLabelSolidCmplx.setText("No solid products");
      jListSolidCmplx.setBackground(java.awt.Color.lightGray);
    }
    //delete a component or exchange with a reaction?
    String comp;
    comp = namn.identC[compToDelOrExch];
    jLabelName.setText(comp);
    if(complxToExchange < -1) { // delete a component
        delete = true;
        //Title
        this.setTitle("Delete \""+comp+"\"");
        if(dbg) {System.out.println(" ---- Confirm delete component: \""+comp+"\"");}
        if(complxs <= 0) {
          jLabelSolubleCmplx.setText("No soluble complexes removed");
        }
        if(complexesS <= 0) {
          jLabelSolidCmplx.setText("No solid products removed");
        }

        if(complxs <=0 && complexesS <=0) {
          jLabelWarning.setText(" ");
          jButtonOK.setText("OK");
        }
        jListSolubCmplx.setFocusable(false);
        jListSolidCmplx.setFocusable(false);
    } else { //exchange the component with a complex
        delete = false;
        //Title
        this.setTitle("Exchange \""+comp+"\" with a reaction");
        if(dbg) {System.out.println(" ---- Confirm exchange component \""+comp+"\" with a reaction");}
        jLabel0.setText("Exchange chemical component");
        jLabelWarning.setText("<html>Select the reaction product to become the<br>component:</html>");
        // select the complex in the list if user has already selected it
        if(complxToExchange >=0) {
          String cmplx = namn.ident[complxToExchange + cs.Na];
          boolean found = false;
          for(int ix=0; ix < listSolubleCmplxModel.getSize(); ix++) {
            if(cmplx.equals(listSolubleCmplxModel.elementAt(ix).toString())) {
              found = true;
              jListSolubCmplx.setSelectedIndex(ix);
              jListSolubCmplx.ensureIndexIsVisible(ix);
              break;
            }
          } //for ix
          if(!found) {
            for(int ix=0; ix < listSolidCmplxModel.getSize(); ix++) {
              if(cmplx.equals(listSolidCmplxModel.elementAt(ix).toString())) {
                jListSolidCmplx.setSelectedIndex(ix);
                jListSolidCmplx.ensureIndexIsVisible(ix);
                break;
              }
            } //for ix
          } //if !found
        } //if complxToExchange >=0
        // select the complex in the list if there is only one
        if((listSolubleCmplxModel.getSize()+listSolidCmplxModel.getSize())==1) {
          if(listSolubleCmplxModel.getSize()==1) {
            jListSolubCmplx.setSelectedIndex(0);
            jListSolubCmplx.ensureIndexIsVisible(0);
          }
          else {
            jListSolidCmplx.setSelectedIndex(0);
            jListSolidCmplx.ensureIndexIsVisible(0);
          }
        }
        jButtonOK.setText("OK  do it!");
        iconName = "images/Exchange_32x32.gif";
        imgURL = this.getClass().getResource(iconName);
        if(imgURL != null) {jButtonOK.setIcon(new javax.swing.ImageIcon(imgURL));}
        else {System.out.println("Error: Could not load image = \""+iconName+"\"");}
    } // complxToExchange < -1?
    setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    this.setVisible(true);

  } // constructor
  //</editor-fold>

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel0 = new javax.swing.JLabel();
        jLabelName = new javax.swing.JLabel();
        jLabelWarning = new javax.swing.JLabel();
        jPanelLists = new javax.swing.JPanel();
        jLabelSolubleCmplx = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListSolubCmplx = new javax.swing.JList();
        jLabelSolidCmplx = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListSolidCmplx = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jPanelButtons = new javax.swing.JPanel();
        jButtonQuit = new javax.swing.JButton();
        jButtonOK = new javax.swing.JButton();

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

        jLabel0.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel0.setForeground(new java.awt.Color(0, 0, 204));
        jLabel0.setText("Delete chemical component:"); // NOI18N

        jLabelName.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabelName.setText("SO4-2"); // NOI18N

        jLabelWarning.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelWarning.setForeground(new java.awt.Color(0, 0, 204));
        jLabelWarning.setText("<html>Will delete the following species<br>as well!</html>"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel0)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabelName)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jLabelWarning))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel0)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelWarning, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabelSolubleCmplx.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelSolubleCmplx.setForeground(new java.awt.Color(0, 0, 204));
        jLabelSolubleCmplx.setText("Soluble complexes removed:"); // NOI18N

        jListSolubCmplx.setModel(listSolubleCmplxModel);
        jListSolubCmplx.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListSolubCmplx.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListSolubCmplxValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jListSolubCmplx);

        jLabelSolidCmplx.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelSolidCmplx.setForeground(new java.awt.Color(0, 0, 204));
        jLabelSolidCmplx.setText("Solid products removed:"); // NOI18N

        jListSolidCmplx.setModel(listSolidCmplxModel);
        jListSolidCmplx.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListSolidCmplxValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jListSolidCmplx);

        javax.swing.GroupLayout jPanelListsLayout = new javax.swing.GroupLayout(jPanelLists);
        jPanelLists.setLayout(jPanelListsLayout);
        jPanelListsLayout.setHorizontalGroup(
            jPanelListsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelListsLayout.createSequentialGroup()
                .addGroup(jPanelListsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelSolubleCmplx)
                    .addComponent(jLabelSolidCmplx, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanelListsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelListsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanelListsLayout.setVerticalGroup(
            jPanelListsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelListsLayout.createSequentialGroup()
                .addComponent(jLabelSolubleCmplx)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelSolidCmplx)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 137, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 82, Short.MAX_VALUE)
        );

        jButtonQuit.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButtonQuit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/spana/images/Quit_32x32.gif"))); // NOI18N
        jButtonQuit.setMnemonic('Q');
        jButtonQuit.setText("<html><u>Q</u>uit</html>"); // NOI18N
        jButtonQuit.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        jButtonQuit.setIconTextGap(8);
        jButtonQuit.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonQuitActionPerformed(evt);
            }
        });

        jButtonOK.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButtonOK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/spana/images/Delete_32x32.gif"))); // NOI18N
        jButtonOK.setMnemonic('O');
        jButtonOK.setText("<html><u>O</u>K <br>get rid of them!</html>"); // NOI18N
        jButtonOK.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        jButtonOK.setIconTextGap(8);
        jButtonOK.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelButtonsLayout = new javax.swing.GroupLayout(jPanelButtons);
        jPanelButtons.setLayout(jPanelButtonsLayout);
        jPanelButtonsLayout.setHorizontalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addComponent(jButtonQuit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(110, Short.MAX_VALUE))
            .addComponent(jButtonOK, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addComponent(jButtonQuit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonOK, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelLists, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelLists, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

  //<editor-fold defaultstate="collapsed" desc="Events">
    private void jListSolubCmplxValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListSolubCmplxValueChanged
        jListSolidCmplx.clearSelection();
        if(delete) {jListSolubCmplx.clearSelection();}
}//GEN-LAST:event_jListSolubCmplxValueChanged

    private void jListSolidCmplxValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListSolidCmplxValueChanged
        jListSolubCmplx.clearSelection();
        if(delete) {jListSolidCmplx.clearSelection();}
}//GEN-LAST:event_jListSolidCmplxValueChanged

    private void jButtonQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonQuitActionPerformed
        quitFrame();
}//GEN-LAST:event_jButtonQuitActionPerformed

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
        cancel = false;
        if(delete) {deleteComponent(cs, compToDelOrExch);}
        else {exchangeComponent(cs, compToDelOrExch);}
        if(!cancel) {quitFrame();}
}//GEN-LAST:event_jButtonOKActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancel = true;
        quitFrame();
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
  private void quitFrame() {
    finished = true;
    this.notify_All();
    this.dispose();
  } // quitForm_Gen_Options
  private synchronized void notify_All() {notifyAll();}
  /** this method will wait for this dialog frame to be closed
   * 
   * @return "cancel" = true if no modification is made  */
  public synchronized boolean waitForModifyConfirm() {
    while(!finished) {
        try {wait();} catch (InterruptedException ex) {}
    } // while
    return cancel;
  } // waitForModifyConfirm()

  //<editor-fold defaultstate="collapsed" desc="deleteComponent">
  private void deleteComponent(Chem.ChemSystem cs, int compToDel) {
    cs.printChemSystem(null);
    // --- count first the number of species remaining
    // count soluble complexes formed by this component
    int nxOut = cs.nx;
    for(int i=0; i<cs.nx; i++) {
      if(Math.abs(cs.a[i][compToDel]) > 1e-7) {nxOut--;}
    } //for i
    // count solid products formed by this component
    int mSolOut = cs.mSol;
    for(int i=cs.nx; i<(cs.Ms-cs.Na-cs.solidC); i++) {
      if(Math.abs(cs.a[i][compToDel]) > 1e-7) {mSolOut--;}
    } //for i
    // components
    int naOut = cs.Na - 1;
    // solid components
    int solidCout = cs.solidC;
    if(compToDel >= (cs.Na-cs.solidC)) {solidCout--; mSolOut--;}
    // --- remove the species: move other species in arrays
    // remove the name of the component
    for(int i=compToDel; i<naOut; i++) {
      cs.namn.identC[i] = cs.namn.identC[i+1];
      cs.namn.ident[i] = cs.namn.ident[i+1];
    } //for i
    for(int i=naOut; i < cs.Ms-1; i++) {
      cs.namn.ident[i] = cs.namn.ident[i+1];
    } //for i
    // remove the species
    int ix1;
    int count =0;
    for(int ix =0; ix < (cs.Ms-cs.Na-cs.solidC); ix++) {
      if(Math.abs(cs.a[ix][compToDel]) > 1e-7) {
        count++;
      } else {
        ix1 = ix - count;
        cs.namn.ident[ix1+naOut] = cs.namn.ident[ix+naOut];
        cs.lBeta[ix1] = cs.lBeta[ix];
        for(int ic =0; ic < naOut; ic++) {
          cs.a[ix1][ic] = cs.a[ix][ic];
          if(ic >= compToDel) {cs.a[ix1][ic] = cs.a[ix][ic+1];}
        } //for ic
      }
    } //for ix
    // --- change the numbers of species
    cs.Ms = cs.Ms -(count + (cs.solidC - solidCout)) -1;
    cs.mSol = mSolOut;
    cs.Na = naOut;
    cs.nx = nxOut;
    cs.solidC = solidCout;
    if(dbg) {System.out.println(" ---- Component deleted.");}
  } //deleteComponent
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="exchangeComponent">
  private void exchangeComponent(Chem.ChemSystem cs, int compToExch) {
    if(dbg) {System.out.println("---- exchangeComponent(cs,"+compToExch+")");}
    int solidC0 = cs.solidC;
    // --- check
    if(jListSolubCmplx.getSelectedIndex() < 0 &&
       jListSolidCmplx.getSelectedIndex() < 0) {
        javax.swing.JOptionPane.showMessageDialog(this,
                "Please, select a species"+nl+"from the list of reaction products",
                this.getTitle(), javax.swing.JOptionPane.WARNING_MESSAGE);
        cancel = true;
        if(dbg) {System.out.println("---- exchangeComponent: cancelled");}
        return;
    }

    // --- the "fictive" solids corresponding to any solid components
    //     are removed here. If at the end there are any solid components,
    //     the corresponding solids are added.
    //done here on local variables: the instance of ChemSystem is not changed
    int Ms = cs.Ms;
    int mSol = cs.mSol;
    if(cs.solidC > 0) {
      Ms = cs.Ms - cs.solidC;
      mSol = cs.mSol - cs.solidC;
    }

    // --- make the exchange
    int ix;
    int nxOut = cs.nx;
    int mSol_Out = mSol;
    int solidC_out = cs.solidC;
    // is it a solid component?
    if(compToExch >= (cs.Na - cs.solidC)) {
        solidC_out--;
        mSol_Out++;
    } else {
        nxOut++;
    }

    String complexExchName;
    if(jListSolubCmplx.getSelectedIndex() > -1) {
        complexExchName = jListSolubCmplx.getSelectedValue().toString();
    } else {
        complexExchName = jListSolidCmplx.getSelectedValue().toString();
    }

    int complexToExch = -1;
    for(ix = 0; ix < (Ms - cs.Na); ix++) {
      if(complexExchName.equals(cs.namn.ident[ix + cs.Na])) {
        complexToExch = ix; break;
      }
    } //for ix
    if(complexToExch < 0) { //this should not occur
        System.err.println("Error exchanging \""+complexExchName+"\"; complexToExch = "+complexToExch);
        if(dbg) {System.out.println("---- exchangeComponent: error exit");}
        cancel = false; // something is really wrong: quit the frame
        return;
    }
    if(dbg) {System.out.println("  exchanging component with reaction: \""+complexExchName+"\"");}

    // exchange with a solid complex?
    if(complexToExch >= cs.nx) {
        solidC_out++;
        mSol_Out--;
    } else {
      nxOut--;
    }

    String newCompName = complexExchName;
    String newCmplxName = cs.namn.identC[compToExch];
    double oldLogK = cs.lBeta[complexToExch];
    double a1 = cs.a[complexToExch][compToExch];
    double[] old_a = new double[cs.Na];
    //for(int ic=0; ic < cs.Na; ic++) {old_a[ic] = cs.a[complexToExch][ic];}
    System.arraycopy(cs.a[complexToExch], 0, old_a, 0, cs.Na);
    for(int ic=compToExch; ic < cs.Na-1; ic++) {
          cs.namn.identC[ic] = cs.namn.identC[ic+1];
          cs.namn.ident[ic] = cs.namn.ident[ic+1];
    }
    cs.namn.identC[cs.Na-1] = "** no name **";
    cs.namn.ident[cs.Na-1] = "** no name **";
    for(ix = complexToExch; ix < (cs.nx + mSol -1); ix++) {
        cs.namn.ident[ix + cs.Na] = cs.namn.ident[ix+1 + cs.Na];
        cs.lBeta[ix] = cs.lBeta[ix+1];
        //for(int ic=0; ic < cs.Na; ic++){cs.a[ix][ic] = cs.a[ix+1][ic];}
        System.arraycopy(cs.a[ix+1], 0, cs.a[ix], 0, cs.Na);
    } //for ix
    ix = cs.nx + mSol -1;
    cs.namn.ident[ix + cs.Na] = "** no name **";
    cs.lBeta[ix] = 9999999;
    for(int ic=0; ic < cs.Na; ic++) {cs.a[ix][ic] = 0;}

    // a soluble complex to become a soluble component?
    int i; int i_cplx_new; int i_comp_new;
    if(complexToExch < cs.nx) {
      
      for(int ic=0; ic < cs.Na; ic++) {
        i = cs.Na -1 -ic;
        if(i > (cs.Na - solidC_out -1)) {
          cs.namn.identC[i] = cs.namn.identC[i-1];
          cs.namn.ident[i] = cs.namn.ident[i-1];
        }
      } //for ic
      i_comp_new = cs.Na - solidC_out -1;
      cs.namn.identC[i_comp_new] = newCompName;
      cs.namn.ident[i_comp_new] = newCompName;
    } else { //a solid complex to become a solid component:
      i_comp_new = cs.Na -1;
      cs.namn.identC[i_comp_new] = newCompName;
      cs.namn.ident[i_comp_new] = newCompName;
    } //soluble/solid complex

    // soluble component to become a soluble complex?
    if(compToExch < (cs.Na - cs.solidC)) {
      for(ix =0; ix < (cs.nx + mSol); ix++) {
        i = (cs.nx + mSol) -1 - ix;
        if(i > (nxOut-1)) {
          cs.namn.ident[i+cs.Na] = cs.namn.ident[i+cs.Na-1];
          cs.lBeta[i] = cs.lBeta[i-1];
          //for(int ic=0; ic < cs.Na; ic++) {cs.a[i][ic] = cs.a[i-1][ic];}
          System.arraycopy(cs.a[i-1], 0, cs.a[i], 0, cs.Na);
        }
      } //for ix
      i_cplx_new = nxOut -1;
    } else { //solid component to become a solid complex:
      i_cplx_new = nxOut + mSol_Out -1;
    } //solid/soluble component

    cs.namn.ident[i_cplx_new + cs.Na] = newCmplxName;
    cs.lBeta[i_cplx_new] = -oldLogK / a1;
    //for(int ic=0; ic < cs.Na; ic++) {cs.a[i_cplx_new][ic] = old_a[ic];}
    System.arraycopy(old_a, 0, cs.a[i_cplx_new], 0, cs.Na);

    double old_a_x;
    for(ix = 0; ix < (cs.nx + mSol); ix++) {
      old_a_x = cs.a[ix][compToExch];
      for(int ic=0; ic < cs.Na-1; ic++) {
        if(ic >= compToExch) {cs.a[ix][ic] = cs.a[ix][ic+1];}
      } //for ic
      if(complexToExch < cs.nx) {
        for(int ic=0; ic < cs.Na-1; ic++) {
          i = cs.Na -1 -ic;
          if(i > (cs.Na - solidC_out -1)) {cs.a[ix][i] = cs.a[ix][i-1];}
        } //for ic
        cs.a[ix][cs.Na - solidC_out -1] = old_a_x;
      } else {
        cs.a[ix][cs.Na -1] = old_a_x;
      }
    }
    for(int ic=0; ic < cs.Na; ic++) {
      old_a[ic] = cs.a[i_cplx_new][ic];
      cs.a[i_cplx_new][ic] = round4(-old_a[ic] /a1);
    }
    cs.a[i_cplx_new][i_comp_new] = round4(1/a1);
    double a1p;
    for(ix=0; ix < (cs.nx + mSol); ix++) {
      if(ix != i_cplx_new) {
        a1p = round4(-cs.a[ix][i_comp_new]/a1);
        cs.lBeta[ix] = cs.lBeta[ix] + a1p * oldLogK;
        for(int ic=0; ic < cs.Na; ic++) {
          if(ic != i_comp_new) {
            cs.a[ix][ic] = round4(cs.a[ix][ic] + a1p * old_a[ic]);
          } else {
            cs.a[ix][i_comp_new] = round4(-a1p);
          } //if ic != i_cplx_new
        } //for ic
      } // if ix != i_cplx_new
    } //for ix

    cs.nx = nxOut;
    mSol = mSol_Out;
    cs.solidC = solidC_out;

    //--- If a new solid component has been introduced, then one must create new
    //    instances of "ChemSystem" and "NamesEtc" large enough to accomodate
    //    the new arrays (because one must add a fictive solid reaction product for
    //    each solid component)
    if(solidC0 < cs.solidC) {
      if(dbg) {System.out.println("     A new solid component has been introduced.");}
      Chem.ChemSystem csNew;
      try{csNew = ch.new ChemSystem(cs.Na, (Ms + cs.solidC), (mSol + cs.solidC), cs.solidC);}
      catch (Chem.ChemicalParameterException ex) {System.err.println(ex.getMessage()); return;}
      Chem.ChemSystem.NamesEtc namnNew;
      try{namnNew = csNew.new NamesEtc(cs.Na, (Ms + cs.solidC), (mSol + cs.solidC));}
      catch (Chem.ChemicalParameterException ex) {System.err.println(ex.getMessage()); return;}
      csNew.jWater = cs.jWater;  //not really needed
      csNew.chemConcs = cs.chemConcs;
      for(ix=0; ix < cs.Ms-cs.Na; ix++) {
        csNew.noll[ix] = cs.noll[ix];
        csNew.lBeta[ix] = cs.lBeta[ix];
        //for(int ic=0; ic < cs.Na; ic++) {csNew.a[ix][ic] = cs.a[ix][ic];}
        System.arraycopy(cs.a[ix], 0, csNew.a[ix], 0, cs.Na);
      } //for ix
      csNew.nx = cs.nx;
      csNew.solidC = cs.solidC;
      //for(int ia=0; ia < cs.Na; ia++) {namnNew.identC[ia] = cs.namn.identC[ia];}
      System.arraycopy(cs.namn.identC, 0, namnNew.identC, 0, cs.Na);
      for(ix=0; ix < cs.Ms; ix++) {
        namnNew.ident[ix] = cs.namn.ident[ix];
        namnNew.nameLength[ix] = cs.namn.nameLength[ix]; //not really needed
      } //for ix
      // these two arrays are not really needed in this context
      //for(ix=0; ix < cs.namn.iel.length; ix++) {namnNew.iel[ix] = cs.namn.iel[ix];}
      //for(ix=0; ix < Math.min(cs.namn.z.length,namnNew.z.length); ix++) {namnNew.z[ix] = cs.namn.z[ix];}
      //-- change the pointers from the old to the new instances
      ch.chemSystem = csNew;
      cs.namn = namnNew;
      this.cs = csNew;
      this.namn = namnNew;
      cs = csNew;
      cs.namn = namnNew;
    } //if solidC0 < cs.namn.solidC
    else { // solidC0 >= cs.namn.solidC
        //--- No new solid components have appeared.
        //    Existing instances of "ChemSystem" and "NamesEtc" are large enough
        cs.mSol = mSol + cs.solidC;
        cs.Ms = Ms + cs.solidC;
    }

    // --- if there are any solid components, add corresponding
    //     fictive solid reaction products corresponding to the components
    ReadChemSyst.addFictiveSolids(cs);

    if(dbg) {System.out.println("---- exchangeComponent: Exchange successful.");}

  } //exchangeComponent

  /** Rounds to the 4th decimal place, but only if after the 4th decimal place
   * the "value" is less than ±0.0002. For example:<br>
   * if x&lt;±0.00019 it returns x=+0 (negative zero is avoided)<br>
   * if x=±3.000199 it returns x=±3<br>
   * if x=±0.2502  no change.<br>
   * if x=±9.999801 it returns x=±10<br>
   * if x=±0.9998  no change.<br>
   * if x=±0.199999 it returns x=±0.2<br>
   * Values larger (or smaller) than ±327.6 are <b>NOT</b> rounded.
   * @param x a double
   * @return rounded value of x   */
  private double round4(double x) {
    if(Double.isNaN(x) || Math.abs(x) > 327.6) {return x;}
    if(Math.abs(x)<0.00019999999) {return +0;}
    double x100 = x * 100; //if x=0.123456, x100=12.3456
    double diff = Math.abs(x100 - Math.rint(x100));
    double result = x;
    if(diff < 0.0199999999) {result = Math.rint(x100) / 100;}
    if(Math.abs(x100 / 100 - result) > 0.00021) {
      System.err.println("Error in \"round4(x)\": x="+x+" x100="+x100+" diff="+diff+", result="+result+"(!?)");
    }
    return result;
  } //round4(x)

  //</editor-fold>

  //</editor-fold>


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonOK;
    private javax.swing.JButton jButtonQuit;
    private javax.swing.JLabel jLabel0;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JLabel jLabelSolidCmplx;
    private javax.swing.JLabel jLabelSolubleCmplx;
    private javax.swing.JLabel jLabelWarning;
    private javax.swing.JList jListSolidCmplx;
    private javax.swing.JList jListSolubCmplx;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JPanel jPanelLists;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables

}
