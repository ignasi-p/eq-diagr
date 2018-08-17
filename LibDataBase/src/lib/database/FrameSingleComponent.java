package lib.database;

import lib.common.MsgExceptn;
import lib.common.Util;
import lib.huvud.ProgramConf;
import lib.huvud.SortedListModel;

/** Show the data in the databases involving a single component.
 * <br>
 * Copyright (C) 2014-2018 I.Puigdomenech.
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
public class FrameSingleComponent extends javax.swing.JFrame {
  // Note: for java 1.6 jComboBox must not have type,
  //       for java 1.7 jComboBox must be <String>
  private ProgramConf pc;
  private ProgramDataDB pd;
  private boolean finished = false;
  /** the number of reactions read so far from the databases */
  private int n;
  private LibSearch hs;
  private java.awt.Dimension windowSize = new java.awt.Dimension(227,122);
  private final SortedListModel sortedModelComplexes = new SortedListModel();
  /** indicates if a mouse click on the reaction list should show the popup menu */
  private boolean isPopup = false;
  /** New-line character(s) to substitute "\n" */
  private static final String nl = System.getProperty("line.separator");
  private static final String SLASH = java.io.File.separator;

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /** Creates new form FrameSingleComponent
   * @param parent
   * @param pc0
   * @param pd0  */
  public FrameSingleComponent(final java.awt.Frame parent,
          ProgramConf pc0, ProgramDataDB pd0) {
    initComponents();
    pc = pc0;
    pd = pd0;

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
    getRootPane().getActionMap().put("ALT_Q", escAction);
    //--- F1 for help
    javax.swing.KeyStroke f1KeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(f1KeyStroke,"F1");
    javax.swing.Action f1Action = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            if(parent != null) {parent.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));}
            FrameSingleComponent.this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            Thread hlp = new Thread() {@Override public void run(){
                String[] a = {"DB_0_Main_htm"};
                lib.huvud.RunProgr.runProgramInProcess(null,ProgramConf.HELP_JAR,a,false,pc.dbg,pc.pathAPP);
                try{Thread.sleep(1500);}   //show the "wait" cursor for 1.5 sec
                catch (InterruptedException e) {}
                FrameSingleComponent.this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
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
    //--- Alt-S message frame on/off
    javax.swing.KeyStroke altSKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altSKeyStroke,"ALT_S");
    javax.swing.Action altSAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            if(pd.msgFrame != null) {pd.msgFrame.setVisible(!pd.msgFrame.isVisible());}
        }};
    getRootPane().getActionMap().put("ALT_S", altSAction);
    //--- Ctrl-S save
    javax.swing.KeyStroke ctrlSKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlSKeyStroke,"CTRL_S");
    javax.swing.Action ctrlSAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jButtonSave.doClick();
        }};
    getRootPane().getActionMap().put("CTRL_S", ctrlSAction);

    //---- forward/backwards arrow keys
    java.util.Set<java.awt.AWTKeyStroke> keys = getFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
    java.util.Set<java.awt.AWTKeyStroke> newKeys = new java.util.HashSet<java.awt.AWTKeyStroke>(keys);
    newKeys.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0));
    setFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,newKeys);

    keys = getFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
    newKeys = new java.util.HashSet<java.awt.AWTKeyStroke>(keys);
    newKeys.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0));
    setFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,newKeys);

    //---- Icon
    String iconName = "images/Kemi.gif";
    java.net.URL imgURL = this.getClass().getResource(iconName);
    if (imgURL != null) {this.setIconImage(new javax.swing.ImageIcon(imgURL).getImage());}
    else {System.out.println("Error: Could not load image = \""+iconName+"\"");}
    if(parent != null) {jButtonExit.setIcon(new javax.swing.ImageIcon(parent.getIconImage()));}
    //---- Title, etc
    this.setTitle(pc.progName+" - Display a single component");

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

    //---- fill in the Combo Box, sorting items first
    fillComboBox(jComboBox, pd.elemComp, pd.includeH2O);
    jComboBox.removeItemAt(0); //the top item is empty

    jLabelTop.setText("<html>Select one of the following<br>"+(jComboBox.getItemCount())+" components:</html>");


    if(pd.dataBasesList.size() >1) {
        jLabelFound.setText("Contents of databases:");
    } else {
        jLabelFound.setText("Contents of the database:");
    }
    jLabelN.setText(" ");
    jLabelSpecies.setText(" ");
    jButtonSave.setEnabled(false);
    if(pd.references != null) {jMenuItemRef.setEnabled(true);} else {jMenuItemRef.setEnabled(false);}

  } //constructor

  public void start() {
    this.setVisible(true);
    pd.msgFrame.setParentFrame(this);
    windowSize = this.getSize();
    jButtonExit.requestFocusInWindow();
  }

  private static void fillComboBox(
          javax.swing.JComboBox<String> jComboBox, // javax.swing.JComboBox jComboBox,  // java 1.6
          java.util.ArrayList<String[]> elemCompStr,
          final boolean includeH2O) {
    if(jComboBox == null) {MsgExceptn.exception("Error: \"JComboBox\" = null in \"fillComboBox\""); return;}
    if(elemCompStr == null || elemCompStr.size() <= 0) {MsgExceptn.exception("Error: \"elemCompStr\" empy in \"fillComboBox\""); return;}
    java.util.ArrayList<String> components = new java.util.ArrayList<String>();
    for(int i=0; i < elemCompStr.size(); i++) {components.add(elemCompStr.get(i)[1]);}
    java.util.Collections.sort(components, String.CASE_INSENSITIVE_ORDER);
    jComboBox.removeAllItems();
    jComboBox.addItem(" ");
    jComboBox.addItem("H+");
    jComboBox.addItem("e-");
    if(includeH2O) {jComboBox.addItem("H2O");}
    int i = jComboBox.getItemCount()-1;
    for(String t : components) {
        if(!t.equals("H+") && !t.equals("e-") && !t.equals("H2O")) {
            if(!jComboBox.getItemAt(i).toString().equals(t)) {jComboBox.addItem(t); i++;}
        }
    }
  } //fillComboBox

//</editor-fold>

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu = new javax.swing.JPopupMenu();
        jMenuItemRef = new javax.swing.JMenuItem();
        jSeparator = new javax.swing.JPopupMenu.Separator();
        jMenuItemCancel = new javax.swing.JMenuItem();
        jLabelTop = new javax.swing.JLabel();
        jComboBox = new javax.swing.JComboBox<String>();
        jLabelFound = new javax.swing.JLabel();
        jLabelN = new javax.swing.JLabel();
        jLabelSpecies = new javax.swing.JLabel();
        jScrollPaneList = new javax.swing.JScrollPane();
        jList = new javax.swing.JList();
        jPanel1 = new javax.swing.JPanel();
        jButtonExit = new javax.swing.JButton();
        jButtonSave = new javax.swing.JButton();
        jButtonSearch = new javax.swing.JButton();

        jMenuItemRef.setMnemonic('r');
        jMenuItemRef.setText("show References");
        jMenuItemRef.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRefActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemRef);
        jPopupMenu.add(jSeparator);

        jMenuItemCancel.setText("Cancel");
        jMenuItemCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCancelActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemCancel);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
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

        jLabelTop.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelTop.setText("<html>Select one of the following<br>321 components:</html>");

        jComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabelFound.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabelFound.setText("Contents of databases:");

        jLabelN.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelN.setText("0000");

        jLabelSpecies.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabelSpecies.setText("species");

        jScrollPaneList.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N

        jList.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        jList.setModel(sortedModelComplexes);
        jList.setFocusable(false);
        jList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jListMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jListMouseReleased(evt);
            }
        });
        jList.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jListFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jListFocusLost(evt);
            }
        });
        jScrollPaneList.setViewportView(jList);

        jButtonExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lib/database/images/Java_32x32.gif"))); // NOI18N
        jButtonExit.setMnemonic('x');
        jButtonExit.setText("Exit");
        jButtonExit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonExit.setMargin(new java.awt.Insets(5, 2, 5, 2));
        jButtonExit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        jButtonSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lib/database/images/Save_32x32.gif"))); // NOI18N
        jButtonSave.setMnemonic('a');
        jButtonSave.setText("Save");
        jButtonSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonSave.setMargin(new java.awt.Insets(5, 2, 5, 2));
        jButtonSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });

        jButtonSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lib/database/images/Search_32x32.gif"))); // NOI18N
        jButtonSearch.setMnemonic('e');
        jButtonSearch.setText("Search");
        jButtonSearch.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonSearch.setMargin(new java.awt.Insets(5, 2, 5, 2));
        jButtonSearch.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSearchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jButtonSearch)
                .addGap(31, 31, 31)
                .addComponent(jButtonSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonExit))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jButtonExit)
            .addComponent(jButtonSave)
            .addComponent(jButtonSearch)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabelFound)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelN)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelSpecies))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelTop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPaneList, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabelTop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelFound)
                            .addComponent(jLabelN)
                            .addComponent(jLabelSpecies)))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneList, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

  //<editor-fold defaultstate="collapsed" desc="events">

    private void jButtonSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSearchActionPerformed
        doSearch();
    }//GEN-LAST:event_jButtonSearchActionPerformed

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
      if(sortedModelComplexes.getSize() <= 0) {
          javax.swing.JOptionPane.showMessageDialog(this, "Nothig to save!", pc.progName, javax.swing.JOptionPane.INFORMATION_MESSAGE);
          return;
      }
      String d = pd.pathAddData.toString();
      if(!d.endsWith(SLASH)) {d = d + SLASH;}
      String fn = Util.getSaveFileName(this, pc.progName, "Enter a file name:", 7,
              (d + "single_comp.txt"), pd.pathAddData.toString());
      if(fn == null || fn.length() <=0) {return;}
      java.io.File f = new java.io.File(fn);
      try {fn = f.getCanonicalPath();} catch (java.io.IOException ex) {fn = null;}
      if(fn == null) {try {fn = f.getAbsolutePath();} catch (Exception ex) {fn = f.getPath();}}
      f = new java.io.File(fn);
      if(f.exists()) {f.delete();}
      if(pc.dbg) {System.out.println("--- Saving file: \""+fn+"\"");}
      java.io.PrintWriter pw;
      try {pw = new java.io.PrintWriter(new java.io.FileWriter(f));}
      catch (java.io.IOException ex) {
          String msg = "Error: "+ex.toString()+nl+
                  "while constructing a \"PrintWrite\" for file:"+nl+"\""+fn+"\"";
          MsgExceptn.exception(msg);
          javax.swing.JOptionPane.showMessageDialog(this, msg, pc.progName,javax.swing.JOptionPane.ERROR_MESSAGE);
          return;
      }
      pw.println(Complex.FILE_FIRST_LINE);
      for(int i=0; i<sortedModelComplexes.getSize(); i++) {
          pw.println(sortedModelComplexes.getElementAt(i).toString());
      } //for i
      pw.close();
    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        closeWindow();
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
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

    private void jListFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListFocusGained
        int i = jList.getSelectedIndex();
        if(i < 0) {i = 0;}
        jList.setSelectedIndex(i);
        jList.ensureIndexIsVisible(i);
    }//GEN-LAST:event_jListFocusGained

    private void jListFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListFocusLost
        if(!jPopupMenu.isVisible()) {jList.clearSelection();}
    }//GEN-LAST:event_jListFocusLost

    private void jMenuItemCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCancelActionPerformed
        jPopupMenu.setVisible(false);
    }//GEN-LAST:event_jMenuItemCancelActionPerformed

    private void jMenuItemRefActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRefActionPerformed
      int index = jList.getSelectedIndex();
      if(pc.dbg) {System.out.println("jMenuItemRef("+index+")");}
      if(index <0 || index >= sortedModelComplexes.getSize()) {return;}
      Complex cmplx;
      try{cmplx = Complex.fromString(sortedModelComplexes.getElementAt(index).toString());}
      catch (Complex.ReadComplexException ex) {cmplx = null;}
      if(cmplx == null || cmplx.name == null) {return;}
      if(pc.dbg) {System.out.println("Show reference(s) for: \""+cmplx.name+"\""+nl+
              "   ref: \""+cmplx.reference.trim()+"\"");}
      pd.references.displayRefs(this, true, cmplx.name, pd.references.splitRefs(cmplx.reference));
      jList.requestFocusInWindow();
      jList.setSelectedIndex(index);
    }//GEN-LAST:event_jMenuItemRefActionPerformed

    private void jListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListMouseClicked
        if(!isPopup) {return;}
        java.awt.Point p = evt.getPoint();
        int i = jList.locationToIndex(p);
        if(i>=0) {
            java.awt.Rectangle r = jList.getCellBounds(i, i);
            if(p.y < r.y || p.y > r.y+r.height) {i=-1;}
            if(i>=0) {
                jPopupMenu.show(jList, evt.getX(), evt.getY());
                jList.setSelectedIndex(i);
            }
        }//if i>=0
        isPopup = false;
    }//GEN-LAST:event_jListMouseClicked

    private void jListMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListMousePressed
        if(evt.isPopupTrigger()) {isPopup = true;}
    }//GEN-LAST:event_jListMousePressed

    private void jListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListMouseReleased
        if(evt.isPopupTrigger()) {isPopup = true;}
    }//GEN-LAST:event_jListMouseReleased

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="methods">
  private void closeWindow() {
    finished = true;    //return from "waitFor()"
    this.notify_All();
    this.dispose();
  } // closeWindow()

  private synchronized void notify_All() {
      //needed by "waitFor()"
      notifyAll();
  }

  /** this method will wait for this window to be closed */
  public synchronized void waitFor() {
    while(!finished) {
        try {wait();} catch (InterruptedException ex) {}
    } // while
  } // waitFor()

  //<editor-fold defaultstate="collapsed" desc="bringToFront()">
  public void bringToFront() {
    if(this != null) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                setVisible(true);
                if((getExtendedState() & javax.swing.JFrame.ICONIFIED) // minimised?
                            == javax.swing.JFrame.ICONIFIED) {
                    setExtendedState(javax.swing.JFrame.NORMAL);
                } // if minimized
                setAlwaysOnTop(true);
                toFront();
                requestFocus();
                setAlwaysOnTop(false);
            }
        });
    }
  } // bringToFront()
  //</editor-fold>

  private void doSearch() {
    sortedModelComplexes.clear();
    int i = jComboBox.getSelectedIndex();
    if(i < 0 || i >= jComboBox.getItemCount()) {
        javax.swing.JOptionPane.showMessageDialog(
                this, "Please select a component"+nl+"from the pull-down list.",
                pc.progName, javax.swing.JOptionPane.INFORMATION_MESSAGE);
        return;
    }
    setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
    final String choosenComp = jComboBox.getItemAt(i).toString();
    final boolean isH = Util.isProton(choosenComp);
    try {hs = new LibSearch(pd.dataBasesList);}
    catch (LibSearch.LibSearchException ex) {
        MsgExceptn.exception(ex.toString());
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        return;}
    jLabelFound.setText("Checking species:");
    jLabelN.setText("0");
    jLabelSpecies.setText(" ");

  Thread srch = new Thread() {@Override public void run() {
    boolean fistComplex = true;
    n = 0;
    int j;
    boolean fnd;
    Complex cmplx = null;
    String errMsg = null;

    while(true) {
        try {
            cmplx = hs.getComplex(fistComplex);
            //Thread.sleep(1);
        } catch (LibSearch.LibSearchException ex) {
            hs.libSearchClose();
            String msg = ex.toString()+nl+"when reading complex nbr."+(n+1);
            MsgExceptn.showErrMsg(FrameSingleComponent.this,msg,1);
            System.err.println(Util.stack2string(ex));
        }
        if(cmplx == null) {break;}
        if(cmplx.name.startsWith("@")) {
            cmplx.name = cmplx.name.substring(1);
              j = 0;
              while(j < sortedModelComplexes.getSize()) {
                if(sortedModelComplexes.getElementAt(j).toString().startsWith(cmplx.name)) {
                    final int jj = j;
                    try {
                        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {@Override public void run() {
                            sortedModelComplexes.removeElement(sortedModelComplexes.getElementAt(jj));
                        }});
                    } catch (InterruptedException ex) {
                        errMsg = ex.toString()+nl+"when reading complex nbr."+(n);
                        System.err.println(Util.stack2string(ex));
                    } catch (java.lang.reflect.InvocationTargetException ex) {
                        errMsg = ex.toString()+nl+"when reading complex nbr."+(n);
                        System.err.println(Util.stack2string(ex));
                    }
                    if(errMsg != null) {
                        MsgExceptn.exception(errMsg);
                        javax.swing.JOptionPane.showMessageDialog(FrameSingleComponent.this,
                            errMsg, pc.progName,javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                } else {j++;}
              } //while
        } else { // does not start with "@"
            fnd = false;
            for(j =0; j < Complex.NDIM; j++) {
                if(cmplx.component[j].equals(choosenComp) &&
                        Math.abs(cmplx.numcomp[j]) > 0.0001) {fnd = true; break;}
            } //for i
            if(isH && Math.abs(cmplx.proton) > 0.0001) {fnd = true;}
            if(fnd) {
                n++;
                javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
                        jLabelN.setText(String.valueOf(n));
                }});
                final String o = cmplx.toString();
                errMsg = null;
                try {
                    javax.swing.SwingUtilities.invokeAndWait(new Runnable() {@Override public void run() {
                                sortedModelComplexes.add(o);
                    }});
                } catch (InterruptedException ex) {
                    errMsg = ex.toString()+nl+"when reading complex nbr."+(n);
                    System.err.println(Util.stack2string(ex));
                } catch (java.lang.reflect.InvocationTargetException ex) {
                    errMsg = ex.toString()+nl+"when reading complex nbr."+(n);
                    System.err.println(Util.stack2string(ex));
                }
                if(errMsg != null) {
                    MsgExceptn.exception(errMsg);
                    javax.swing.JOptionPane.showMessageDialog(FrameSingleComponent.this,
                            errMsg, pc.progName,javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            } //if fnd
        } // starts with "@"?
        fistComplex = false;
    } //while
    n = sortedModelComplexes.getSize();
    javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
      if(pd.dataBasesList.size() >1) {
        jLabelFound.setText("Found in databases:");
      } else {
        jLabelFound.setText("Found in the database:");
      }
      jLabelN.setText(String.valueOf(n));
      if(n>=1) {
          jLabelSpecies.setText("species");
          jButtonSave.setEnabled(true);
          jList.setFocusable(true);
      } else {
          jButtonSave.setEnabled(false);
          jList.setFocusable(false);
      }
      jScrollPaneList.validate();
      FrameSingleComponent.this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }           });
  }     };//new Thread
  srch.start();  //any statements placed below are executed inmediately

  } // doSearch
  //</editor-fold>

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JButton jButtonSearch;
    private javax.swing.JComboBox<String> jComboBox;
    private javax.swing.JLabel jLabelFound;
    private javax.swing.JLabel jLabelN;
    private javax.swing.JLabel jLabelSpecies;
    private javax.swing.JLabel jLabelTop;
    private javax.swing.JList jList;
    private javax.swing.JMenuItem jMenuItemCancel;
    private javax.swing.JMenuItem jMenuItemRef;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPopupMenu jPopupMenu;
    private javax.swing.JScrollPane jScrollPaneList;
    private javax.swing.JPopupMenu.Separator jSeparator;
    // End of variables declaration//GEN-END:variables
}
