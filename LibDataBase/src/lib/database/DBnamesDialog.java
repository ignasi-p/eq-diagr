package lib.database;

import lib.common.MsgExceptn;
import lib.common.Util;
import lib.huvud.ProgramConf;

/**  Allows the user to choose which databases to use.
 * <br>
 * Copyright (C) 2015-2018 I.Puigdomenech.
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
public class DBnamesDialog extends javax.swing.JDialog {
    private ProgramConf pc;
   /** array list of String[3] objects<br>
    * [0] contains the element name (e.g. "C"),<br>
    * [1] the component formula ("CN-" or "Fe+2"),<br>
    * [2] the component name ("cyanide" or null), which is not really needed,
    * but used to help the user */
    private final java.util.ArrayList<String[]> elemComp;
    /** the database list when the window is created, updated on exit */
    private final java.util.ArrayList<String> dbList;
    /** the database list, local copy */
    private final java.util.ArrayList<String> dbListLocal;
    /** the default path to open database files */
    private StringBuffer pathDatabaseFiles;

    // private final javax.swing.DefaultListModel dBnamesModel = new javax.swing.DefaultListModel(); // java 1.6
    private final javax.swing.DefaultListModel<String> dBnamesModel = new javax.swing.DefaultListModel<>();
    private java.awt.Dimension windowSize = new java.awt.Dimension(300,200);

    private final javax.swing.border.Border defBorder;
    private final javax.swing.border.Border highlightedBorder = javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED, java.awt.Color.gray, java.awt.Color.black);

    /** New-line character(s) to substitute "\n". */
    private static final String nl = System.getProperty("line.separator");

    public boolean cancel = false;

  //<editor-fold defaultstate="collapsed" desc="Constructor">
    /** Creates new form DBNamesDialog
     * @param parent
     * @param modal
     * @param pc0
     * @param dbList0
     * @param pathDatabaseFiles0
     * @param elemComp0  */
    public DBnamesDialog(final java.awt.Frame parent, boolean modal,
            ProgramConf pc0,
            java.util.ArrayList<String> dbList0,
            StringBuffer pathDatabaseFiles0,
            java.util.ArrayList<String[]> elemComp0) {
    super(parent, modal);
    initComponents();
    pc = pc0;
    dbList = dbList0;
    pathDatabaseFiles = pathDatabaseFiles0;
    elemComp = elemComp0;
    if(pathDatabaseFiles == null) {pathDatabaseFiles = new StringBuffer();}
    if(pathDatabaseFiles.length() > 0) {
        java.io.File f = new java.io.File(pathDatabaseFiles.toString());
        if(f.exists()) {
            if(f.isFile()) {
                pathDatabaseFiles.replace(0,pathDatabaseFiles.length(),f.getParent());
            }
        } else { //path does not exist:
            if(pc.pathAPP != null && pc.pathAPP.trim().length()>0) {
                pathDatabaseFiles.replace(0,pathDatabaseFiles.length(),pc.pathAPP);
            } else {pathDatabaseFiles.replace(0,pathDatabaseFiles.length(),".");}
        }
    }
    if(pc.dbg) {System.out.println("---- Select Databases ----");}

    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    //--- Close window on ESC key
    javax.swing.KeyStroke escKeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(escKeyStroke,"ESCAPE");
    javax.swing.Action escAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            cancel = true;
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
    //--- F1 for help
    javax.swing.KeyStroke f1KeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(f1KeyStroke,"F1");
    javax.swing.Action f1Action = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            if(parent != null) {parent.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));}
            DBnamesDialog.this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            Thread hlp = new Thread() {@Override public void run(){
                String[] a = {"DB_Remove_change_data_htm"};
                lib.huvud.RunProgr.runProgramInProcess(null,ProgramConf.HELP_JAR,a,false,pc.dbg,pc.pathAPP);
                try{Thread.sleep(1500);}   //show the "wait" cursor for 1.5 sec
                catch (InterruptedException e) {}
                DBnamesDialog.this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                if(parent != null) {parent.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));}
            }};//new Thread
            hlp.start();
        }};
    getRootPane().getActionMap().put("F1", f1Action);
    //--- Alt-H help
    javax.swing.KeyStroke altHKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altHKeyStroke,"ALT_H");
    getRootPane().getActionMap().put("ALT_H", f1Action);

    //---- forward/backwards arrow keys
    java.util.Set<java.awt.AWTKeyStroke> keys = getFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
    java.util.Set<java.awt.AWTKeyStroke> newKeys = new java.util.HashSet<java.awt.AWTKeyStroke>(keys);
    newKeys.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0));
    setFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,newKeys);

    keys = getFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
    newKeys = new java.util.HashSet<java.awt.AWTKeyStroke>(keys);
    newKeys.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0));
    setFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,newKeys);

    //---- Title, etc
    //getContentPane().setBackground(new java.awt.Color(255, 255, 153));
    this.setTitle(pc.progName+" - Set Database Names");
    defBorder = jScrollPane1.getBorder();
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
    //---- OK-button icon
    if(parent != null) {jButtonOK.setIcon(new javax.swing.ImageIcon(parent.getIconImage()));}
    //---- Make a local copy
    dbListLocal = new java.util.ArrayList<String>();
    dbListLocal.addAll(dbList);
    if(pc.dbg) {
        if(dbListLocal.size()>0) {System.out.println("---- Database list:"+nl+dbListLocal.toString());}
        else {System.out.println("---- Database list is empty.");}
    }
    //----
    updateDBnames();
    windowSize = this.getSize();
    jButtonRemove.setEnabled(false);
    if(dBnamesModel.getSize() <=0) {jButtonAdd.requestFocusInWindow();}
    else {jButtonOK.requestFocusInWindow();}

    }
  //</editor-fold>

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabelNr = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListDBnames = new javax.swing.JList();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jButtonOK = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jButtonAdd = new javax.swing.JButton();
        jButtonRemove = new javax.swing.JButton();
        jButtonUp = new javax.swing.JButton();
        jButtonDn = new javax.swing.JButton();

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

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

        jLabelNr.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelNr.setText("0 Databases");

        jListDBnames.setModel(dBnamesModel);
        jListDBnames.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListDBnamesMouseClicked(evt);
            }
        });
        jListDBnames.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListDBnamesValueChanged(evt);
            }
        });
        jListDBnames.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jListDBnamesFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jListDBnamesFocusLost(evt);
            }
        });
        jListDBnames.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jListDBnamesKeyTyped(evt);
            }
        });
        jScrollPane1.setViewportView(jListDBnames);

        jButtonOK.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButtonOK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lib/database/images/Java_32x32.gif"))); // NOI18N
        jButtonOK.setMnemonic('o');
        jButtonOK.setText("OK");
        jButtonOK.setToolTipText("exit");
        jButtonOK.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonOK.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonOK.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });

        jButtonCancel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButtonCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lib/database/images/Quit_32x32.gif"))); // NOI18N
        jButtonCancel.setMnemonic('C');
        jButtonCancel.setText("Cancel");
        jButtonCancel.setToolTipText("quit");
        jButtonCancel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonCancel.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonCancel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addComponent(jButtonOK, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonCancel)
                    .addComponent(jButtonOK))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButtonAdd.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButtonAdd.setMnemonic('a');
        jButtonAdd.setText("<html><u>A</u>dd another file</html>");
        jButtonAdd.setToolTipText("Add a new file to the list");
        jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddActionPerformed(evt);
            }
        });

        jButtonRemove.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButtonRemove.setMnemonic('r');
        jButtonRemove.setText("Remove");
        jButtonRemove.setToolTipText("Remove the selected file from the list");
        jButtonRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveActionPerformed(evt);
            }
        });

        jButtonUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lib/database/images/ArrowUp.gif"))); // NOI18N
        jButtonUp.setMnemonic('u');
        jButtonUp.setToolTipText("move Up (Alt-U)");
        jButtonUp.setIconTextGap(0);
        jButtonUp.setMargin(new java.awt.Insets(0, 2, 0, 2));
        jButtonUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUpActionPerformed(evt);
            }
        });

        jButtonDn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lib/database/images/ArrowDn.gif"))); // NOI18N
        jButtonDn.setMnemonic('d');
        jButtonDn.setToolTipText("move Down (Alt-D)");
        jButtonDn.setIconTextGap(0);
        jButtonDn.setMargin(new java.awt.Insets(0, 2, 0, 2));
        jButtonDn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jButtonAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonRemove)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonUp, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonDn, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jButtonRemove, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jButtonAdd)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(jButtonUp, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButtonDn, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelNr)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelNr)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

  //<editor-fold defaultstate="collapsed" desc="Events">

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancel = true;
        closeWindow();
    }//GEN-LAST:event_formWindowClosing

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
      if(windowSize != null) {
        int w = Math.round((float)windowSize.getWidth());
        int h = Math.round((float)windowSize.getHeight());
        if(this.getHeight()<h){this.setSize(this.getWidth(), h);}
        if(this.getWidth()<w){this.setSize(w,this.getHeight());}
      }
    }//GEN-LAST:event_formComponentResized

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
        if(pc.dbg) {System.out.println("---- Exit: New database list:"+nl+dbListLocal.toString()+nl+"----");}
        dbList.clear();
        dbList.addAll(dbListLocal);
        closeWindow();
    }//GEN-LAST:event_jButtonOKActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        cancel = true;
        if(pc.dbg) {System.out.println("---- Quit: database list unchanged.");}
        closeWindow();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jListDBnamesKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jListDBnamesKeyTyped
        if(evt.getKeyChar() == java.awt.event.KeyEvent.VK_DELETE ||
                evt.getKeyChar() == java.awt.event.KeyEvent.VK_BACK_SPACE) {
            evt.consume();
            jButtonRemoveActionPerformed(null);
            return;
        }
        char c = Character.toUpperCase(evt.getKeyChar());
        if(evt.getKeyChar() != java.awt.event.KeyEvent.VK_ESCAPE &&
           evt.getKeyChar() != java.awt.event.KeyEvent.VK_ENTER &&
           !(evt.isAltDown() && ((c == 'X') || (c == 'A') || (c == 'R') ||
                                 (c == 'U') || (c == 'D') || (c == 'C') || (c == 'O') ||
                 (evt.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER))
                 ) //isAltDown
                 ) { // if not ESC or Alt-something
                evt.consume(); // remove the typed key
                dBnames_Click();
        } // if char ok
    }//GEN-LAST:event_jListDBnamesKeyTyped

    private void jListDBnamesFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListDBnamesFocusGained
        if(jListDBnames.isFocusOwner()) {
            jScrollPane1.setBorder(highlightedBorder);
        }
        if(dBnamesModel.getSize()>0) {
          int i = Math.max(0,jListDBnames.getSelectedIndex());
          jListDBnames.setSelectedIndex(i);
          jListDBnames.ensureIndexIsVisible(i);
        }
    }//GEN-LAST:event_jListDBnamesFocusGained

    private void jListDBnamesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListDBnamesValueChanged
      int i = -1;
      if(dBnamesModel.getSize()>0) {
          i = jListDBnames.getSelectedIndex();
      } else {jButtonRemove.setEnabled(false);}
      int n = dBnamesModel.size();
      if(i < 0 || i >= n) {
          jButtonRemove.setEnabled(false);
          return;
      }
      jButtonRemove.setEnabled(true);
      jButtonUp.setEnabled(i>0);
      jButtonDn.setEnabled(i < (n-1));
    }//GEN-LAST:event_jListDBnamesValueChanged

    private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddActionPerformed
        dBnames_Click();
    }//GEN-LAST:event_jButtonAddActionPerformed

    private void jButtonRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveActionPerformed
        dbNameRemove();
    }//GEN-LAST:event_jButtonRemoveActionPerformed

    private void jButtonUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUpActionPerformed
      int i = -1;
      if(dBnamesModel.getSize()>0) {i = jListDBnames.getSelectedIndex();}
      if(i < 1 || i >= dBnamesModel.size()) {return;}
      // move the name one position up
      synchronized (this) {
          String dbn = dBnamesModel.get(i).toString();
          if(pc.dbg) {System.out.println("---- Moving up: \""+dbn+"\"");}
          String dbnUp = dBnamesModel.get(i-1).toString();
          dbListLocal.set(i, dbnUp);
          dbListLocal.set(i-1, dbn);
      }
      updateDBnames();
      jListDBnames.setSelectedIndex(i-1);
      jListDBnames.ensureIndexIsVisible(i-1);
      jListDBnames.requestFocusInWindow();
    }//GEN-LAST:event_jButtonUpActionPerformed

    private void jButtonDnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDnActionPerformed
      int i = -1;
      if(dBnamesModel.getSize()>0) {i = jListDBnames.getSelectedIndex();}
      if(i < 0 || i >= (dBnamesModel.size()-1)) {return;}
      // move the name one position down
      synchronized (this) {
        String dbn = dBnamesModel.get(i).toString();
        if(pc.dbg) {System.out.println("---- Moving down: \""+dbn+"\"");}
        String dbnDn = dBnamesModel.get(i+1).toString();
        dbListLocal.set(i, dbnDn);
        dbListLocal.set(i+1, dbn);
      }
      updateDBnames();
      jListDBnames.setSelectedIndex(i+1);
      jListDBnames.ensureIndexIsVisible(i+1);
      jListDBnames.requestFocusInWindow();
    }//GEN-LAST:event_jButtonDnActionPerformed

    private void jListDBnamesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListDBnamesMouseClicked
        if(evt.getClickCount()>=2) {dBnames_Click();}
    }//GEN-LAST:event_jListDBnamesMouseClicked

    private void jListDBnamesFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListDBnamesFocusLost
        if(!jListDBnames.isFocusOwner()) {jScrollPane1.setBorder(defBorder);}
    }//GEN-LAST:event_jListDBnamesFocusLost

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Methods">

  private void closeWindow() {
    if(!cancel && dbListLocal.size() <=0) {
        javax.swing.JOptionPane.showMessageDialog(this,
                "There are no databases selected?"+nl+
                "Please note that the program is useless without databases."+nl+nl,
                pc.progName, javax.swing.JOptionPane.WARNING_MESSAGE);
    }
    this.setVisible(false);
    //this.dispose();
  } // closeWindow()

  //<editor-fold defaultstate="collapsed" desc="bringToFront()">
  public void bringToFront() {
    if(this != null) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                setVisible(true);
                setAlwaysOnTop(true);
                toFront();
                requestFocus();
                setAlwaysOnTop(false);
            }
        });
    }
  } // bringToFront()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="dBnames_Click">
  private void dBnames_Click() {
    setCursorWait();
    if(pc.dbg) {System.out.println("--- dBnames_Click()");}

    //--- get a name
    String dbn = getDBFileName();
    //
    if(dbn == null) {
        setCursorDef();
        jListDBnames.requestFocusInWindow();
        return;
    }
    //--- OK?
    if(!LibDB.isDBnameOK(this, dbn, pc.dbg)) {System.out.println("--- isDBnameOK("+dbn+") = false"); return;}
    java.io.File dbf = new java.io.File(dbn);
    try {dbn = dbf.getCanonicalPath();}
    catch(java.io.IOException ex) {
        try{dbn = dbf.getAbsolutePath();}
        catch(Exception ex2) {dbn = dbf.getPath();}
    }
    //--- element-reactants file
    String dbnEle = AddDataElem.getElemFileName(pc.dbg, this, dbn);
    if(dbnEle == null) {
        if(pc.dbg){System.out.println("--- Could not get an element file name for file: "+dbn);}
        setCursorDef();
        return;
    }
    java.io.File dbfEle = new java.io.File(dbnEle);
    boolean binaryDB;
    binaryDB = dbn.toLowerCase().endsWith(".db");
    if(binaryDB) {
        if(!dbfEle.exists()) {
            String msg = "Could not find the \"element\"-file"+nl+
                "     \""+dbfEle.getName()+"\""+nl+
                "for database: \""+dbf.getName()+"\"."+nl+nl+
                "The database can NOT be used.";
            MsgExceptn.showErrMsg(this, msg, 1);
            return;
        }
    }  else { // not binary database
        if(!dbfEle.exists()) {
            String msg = "Could not find the \"element\"-file"+nl+
                "for database: \""+dbf.getName()+"\"."+nl+nl+
                "The file \""+dbfEle.getName()+"\" will be created.";
            System.out.println("--- Could not find the \"element\"-file for database: \""+dbf.getName()+"\"");
            Object[] opt = {"OK", "Cancel"};
            int m = javax.swing.JOptionPane.showOptionDialog(this,msg,
                        pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[0]);
            if(m != javax.swing.JOptionPane.YES_OPTION) {
                if(pc.dbg) {System.out.println("Cancelled by the user");}
                setCursorDef();
                return;
            }
        }
        // list of reactants and their chemical elements
        java.util.ArrayList<String[]> elemCompNewFile = new java.util.ArrayList<String[]>();
        // are the reactants in "dbn" not found in "dbnEle"?
        //    if so, are they in "elemComp"?
        //    if new reactants are found the file "dbnEle" is automatically saved
        try{AddDataElem.elemCompAdd_Update(pc.dbg, this, dbn, dbnEle, elemComp, elemCompNewFile);}
        catch (AddDataElem.AddDataException ex) {
            if(!this.isVisible()) {this.setVisible(true);}
            MsgExceptn.showErrMsg(this, ex.getMessage(), 1);
            setCursorDef();
            return;
        }
    } // binary database?

    // --- check the database for errors
    java.util.ArrayList<String> arrayList = new java.util.ArrayList<String>();
    arrayList.add(dbn);
    CheckDatabases.CheckDataBasesLists lists = new CheckDatabases.CheckDataBasesLists();
    CheckDatabases.checkDatabases(pc.dbg, this, arrayList, null, lists);
    String title = pc.progName;
    boolean ok = CheckDatabases.displayDatabaseErrors(pc.dbg, this, title, dbf.getName(), lists);
    if(!ok) {
        System.out.println("--- displayDatabaseErrors: NOT OK for file: "+dbn);
        setCursorDef();
        return;
    }

    //--- add new name to list
    //    check that the name is not already in the list
    synchronized (this) {
        int n = dbListLocal.size();
        boolean found = false;
        for(int j=0; j<n; j++) {
            if(dbListLocal.get(j).equalsIgnoreCase(dbn)) {
                found = true; break;
            }
        }
        //--- add new name to list
        if(!found) {
            if(pc.dbg) {System.out.println("---- Adding database: \""+dbn+"\"");}
            dbListLocal.add(dbn);
        }
    } // synchronized

    //---
    updateDBnames();

    setCursorDef();
    jListDBnames.requestFocusInWindow();
    for(int i=0; i < dBnamesModel.size(); i++) {
        if(dBnamesModel.get(i).equals(dbn)) {
            jListDBnames.setSelectedIndex(i);
            jListDBnames.ensureIndexIsVisible(i);
            break;
        }
    }
    jButtonOK.requestFocusInWindow();
  } //dBnames_Click
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="getDBFileName()">
  /**  Get a database file name from the user using an Open File dialog.
   * @return "null" if the user cancels the opertion; a file name otherwise. */
  private String getDBFileName() {
    // Ask the user for a file name using a Open File dialog
    this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
    String dbFileName;
    dbFileName = Util.getOpenFileName(this, pc.progName, true,
              "Select a database file:", 2, null, pathDatabaseFiles.toString());
    if(dbFileName == null || dbFileName.trim().length() <= 0) { // cancelled or error
        return null;
    }
    java.io.File dbFile = new java.io.File(dbFileName);
    if(pathDatabaseFiles.length() >0) {pathDatabaseFiles.delete(0, pathDatabaseFiles.length());}
    pathDatabaseFiles.append(dbFile.getParent());
    return dbFileName;
  } // getDBFileName()
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="dbNameRemove">
  private synchronized void dbNameRemove() {
    int iold = -1;
    if(dBnamesModel.getSize()>0) {iold = jListDBnames.getSelectedIndex();}
    if(iold < 0) {return;}
    String dbn = dBnamesModel.get(iold).toString();
    String warning = "";
    if(dBnamesModel.getSize() ==1) {warning = "Removing the last database is not a very good idea!"+nl+nl;}
    warning = warning+"Do you want to remove the database:"+nl+"   \""+dbn+"\" ??";

    Object[] opt1 = {"OK", "Cancel"};
    int m = javax.swing.JOptionPane.showOptionDialog(this,
                warning, pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE, null, opt1, opt1[1]);
    if(m == javax.swing.JOptionPane.NO_OPTION) { //the second button is "cancel"
        jListDBnames.requestFocusInWindow();
        if(iold < dBnamesModel.getSize()) {
            jListDBnames.setSelectedIndex(iold);
            jListDBnames.ensureIndexIsVisible(iold);
        }
        return;
    }

    int n = dbListLocal.size();
    boolean found = false;
    // In principle "i" should be the index to remove. Just in case: check
    // in the array list that the name matches...
    int k = 0;
    for(int j=0; j<n; j++) {
        if(dbListLocal.get(j).equalsIgnoreCase(dbn)) {k = j; found = true; break;}
    }
    //--- remove name from list
    if(found) {
        if(pc.dbg) {System.out.println("---- Removing database: \""+dbn+"\"");}
        dbListLocal.remove(k);
    }
    //---
    updateDBnames();

    jListDBnames.requestFocusInWindow();
    if(iold >= dBnamesModel.getSize()) {iold = dBnamesModel.getSize()-1;}
    if(iold < dBnamesModel.getSize() && iold >=0) {
        jListDBnames.setSelectedIndex(iold);
        jListDBnames.ensureIndexIsVisible(iold);
    }
  } //dBnameRemove
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="updateDBnames">
  private synchronized void updateDBnames(){
    dBnamesModel.clear();
    int n = dbListLocal.size();
    for(int i = 0; i < n; i++) {
      dBnamesModel.addElement(dbListLocal.get(i));
    }
    String t;
    if(n == 1) {t = "1 Database used:";}
    else {t = String.valueOf(n)+" Databases used:";}
    jLabelNr.setText(t);
    if(dBnamesModel.getSize() <=0) {
        jButtonAdd.setText("<html><u>A</u>dd a file</html>");
    } else {
        jButtonAdd.setText("<html><u>A</u>dd another file</html>");
    }
    jButtonUp.setEnabled(false);
    jButtonDn.setEnabled(false);
  } //updateDBnames
  //</editor-fold>

  private void setCursorWait() {
    setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
    jPanel1.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
    jListDBnames.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
  }
  private void setCursorDef() {
    setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    jPanel1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    jListDBnames.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
  }

  //</editor-fold>

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonDn;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JButton jButtonRemove;
    private javax.swing.JButton jButtonUp;
    private javax.swing.JLabel jLabelNr;
    private javax.swing.JList jListDBnames;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
