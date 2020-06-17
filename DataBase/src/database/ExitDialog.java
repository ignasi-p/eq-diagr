package database;

import lib.common.MsgExceptn;
import lib.common.Util;
import lib.database.Complex;
import lib.database.LibDB;
import lib.database.ProgramDataDB;
import lib.huvud.ProgramConf;

/** Exit dialog.
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
public class ExitDialog extends javax.swing.JDialog {
  private ProgramConf pc;
  private final ProgramDataDB pd;
  private final FrameDBmain dbF;
  private final DBSearch hs;
  private final java.awt.Dimension windowSize = new java.awt.Dimension(185,185);
  /** New-line character(s) to substitute "\n". */
  private static final String nl = System.getProperty("line.separator");
  private static final java.util.Locale engl = java.util.Locale.ENGLISH;

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /** Creates new form ExitDialog
     * @param parent
     * @param modal
     * @param pc0
     * @param pd0
     * @param hs0  */
  public ExitDialog(java.awt.Frame parent, boolean modal,
            ProgramConf pc0,
            ProgramDataDB pd0,
            DBSearch hs0) {
    super(parent, modal);
    initComponents();
    pc = pc0;
    pd = pd0;
    dbF = (FrameDBmain)parent;
    hs = hs0;
    // ----
    dbF.setCursorDef();
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
    //--- Alt-X eXit
    javax.swing.KeyStroke altXKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altXKeyStroke,"ALT_X");
    getRootPane().getActionMap().put("ALT_X", escAction);
    //--- F1 for help
    javax.swing.KeyStroke f1KeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_F1,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(f1KeyStroke,"F1");
    javax.swing.Action f1Action = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            ExitDialog.this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            Thread hlp = new Thread() {@Override public void run(){
                String[] a = {"DB_0_Main_htm"};
                lib.huvud.RunProgr.runProgramInProcess(null,ProgramConf.HELP_JAR,a,false,pc.dbg,pc.pathAPP);
                try{Thread.sleep(2000);}   //show the "wait" cursor for 2 sec
                catch (InterruptedException e) {}
                ExitDialog.this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
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
    newKeys.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, 0));
    setFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,newKeys);

    keys = getFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
    newKeys = new java.util.HashSet<java.awt.AWTKeyStroke>(keys);
    newKeys.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0));
    newKeys.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, 0));
    setFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,newKeys);
    //---- Title, etc
    getContentPane().setBackground(java.awt.Color.BLUE);
    this.setTitle(pc.progName+" - Exit and...");

    //---- Centre window on parent/screen
    int left,top;
    java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    if(parent != null) {
        left = Math.max(0,(parent.getX() + 20));
        top = Math.max(0,(parent.getY()+20));
    } else {
        left = Math.max(0,(screenSize.width-this.getWidth())/2);
        top = Math.max(0,(screenSize.height-this.getHeight())/2);
    }
    this.setLocation(Math.min(screenSize.width-this.getWidth()-20,left),
                     Math.min(screenSize.height-this.getHeight()-20, top));
    //---- is "Diagram" there?
    String diagramProg = LibDB.getDiagramProgr(pd.diagramProgr);
    if(diagramProg == null) {
        jButtonDiagram.setEnabled(false);
        jLabelDiagram.setForeground(java.awt.Color.GRAY);
        jLabelDiagram.setText("<html>save file and<br>make a Diagram</html>");
        jLabelSave.setText("<html><u>S</u>ave<br>to disk file</html>");
    }
    jButtonDiagram.setMnemonic(java.awt.event.KeyEvent.VK_D);
    dbF.exitCancel = true;
    dbF.send2Diagram = false;
    this.setVisible(true);
    //windowSize = ExitDialog.this.getSize();
  } //constructor
  //</editor-fold>

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButtonDiagram = new javax.swing.JButton();
        jLabelDiagram = new javax.swing.JLabel();
        jButtonSave = new javax.swing.JButton();
        jLabelSave = new javax.swing.JLabel();
        jButtonCancel = new javax.swing.JButton();

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

        jButtonDiagram.setIcon(new javax.swing.ImageIcon(getClass().getResource("/database/images/Diagram_button.gif"))); // NOI18N
        jButtonDiagram.setMnemonic('d');
        jButtonDiagram.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButtonDiagram.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonDiagram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDiagramActionPerformed(evt);
            }
        });

        jLabelDiagram.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelDiagram.setForeground(java.awt.Color.white);
        jLabelDiagram.setLabelFor(jButtonDiagram);
        jLabelDiagram.setText("<html>save file and<br>make a <u>D</u>iagram</html>");

        jButtonSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/database/images/Save_32x32.gif"))); // NOI18N
        jButtonSave.setMnemonic('s');
        jButtonSave.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButtonSave.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });

        jLabelSave.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelSave.setForeground(java.awt.Color.white);
        jLabelSave.setLabelFor(jButtonSave);
        jLabelSave.setText("<html>only <u>S</u>ave<br>file</html>");

        jButtonCancel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButtonCancel.setMnemonic('c');
        jButtonCancel.setText("Cancel");
        jButtonCancel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButtonCancel.setMargin(new java.awt.Insets(2, 5, 2, 5));
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jButtonSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonDiagram, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelDiagram, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonDiagram)
                    .addComponent(jLabelDiagram, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonSave)
                    .addComponent(jLabelSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButtonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
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

    private void jButtonDiagramActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDiagramActionPerformed
        if(saveDataFile(hs)) {
            dbF.send2Diagram = true;
            dbF.exitCancel = false;
        } else {dbF.exitCancel = true;}
        closeWindow();
    }//GEN-LAST:event_jButtonDiagramActionPerformed

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
        dbF.exitCancel = !saveDataFile(hs);
        closeWindow();
    }//GEN-LAST:event_jButtonSaveActionPerformed

    //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Methods">

  private void closeWindow() {
    this.dispose();
    dbF.bringToFront();
  } // closeWindow()

  private boolean saveDataFile(DBSearch srch) {
      if(srch == null) {
          MsgExceptn.exception("Error in \"saveDataFile\": null argument");
          return false;
      }
      // Is there enthalpy data?
      if(!DBSearch.checkTemperature(srch, this, false)) {return false;}

      String defaultName;
      if(dbF.outputDataFile != null) {defaultName = dbF.outputDataFile;} else {defaultName = "";}
      String fn = Util.getSaveFileName(this, pc.progName,
              "Save file: Enter data-file name", 5, defaultName, pc.pathDef.toString());
      if(fn == null) {return false;}
      pc.setPathDef(fn);
      // Make a temporary file to write the information. If no error occurs,
      // then the data file is overwritten with the temporary file
      String tmpFileName;
      tmpFileName = fn.substring(0,fn.length()-4).concat(".tmp");
      java.io.File tmpFile = new java.io.File(tmpFileName);
      java.io.FileOutputStream fos;
      java.io.Writer w;
      try{
          fos = new java.io.FileOutputStream(tmpFile);
          w = new java.io.BufferedWriter(new java.io.OutputStreamWriter(fos,"UTF8"));
      }
      catch (java.io.IOException ex) {
          String msg = "Error in \"saveDataFile\","+nl+
                    "   \""+ex.toString()+"\","+nl+
                    "   while preparing file:"+nl+"   \""+tmpFileName+"\"";
          MsgExceptn.exception(msg);
          javax.swing.JOptionPane.showMessageDialog(this, msg, pc.progName,javax.swing.JOptionPane.ERROR_MESSAGE);
          return false;
      }
      // make some simple checks
      if(srch.na<1 || srch.na>1000) {
            MsgExceptn.exception("Error: Number of components is: "+srch.na+nl+
            "Must be >0 and <1000.");
      }
      if(srch.nx < 0 || srch.nx > 1000000) {
            MsgExceptn.exception("Error: Number of soluble complexes is: "+srch.nx+nl+
            "Must be >=0 and < 1 000 000.");}
      int nrSol = srch.nf;
      if(srch.nf < 0 || srch.nf > 100000) {
            MsgExceptn.exception("Error: Number of solid reaction products is: "+srch.nf+nl+
            "Must be >=0 and < 100 000.");}
      if(srch.solidC < 0 || srch.solidC > srch.na) {
            MsgExceptn.exception("Error: Number of solid components is: "+srch.solidC+nl+
            "Must be >=0 and <= "+srch.na+" (the nbr of components).");}

      if(pc.dbg) {System.out.println("---- Saving file \""+fn+"\".");}
      String m = ",   /DATABASE (HYDRA)";
      if(!Double.isNaN(srch.temperature_C)) {m = m + ", t="+Util.formatDbl3(srch.temperature_C);}
      if(!Double.isNaN(srch.pressure_bar)) {m = m + ", p="+Util.formatDbl3(srch.pressure_bar);}
      //if requested: add water
      if(pd.includeH2O) {
          dbF.modelSelectedComps.add((srch.na - srch.solidC), "H2O");
          srch.na++;
      }

      try{
      w.write(" "+String.valueOf(srch.na)+", "+
          String.valueOf(srch.nx)+", "+String.valueOf(nrSol)+", "+
          String.valueOf(srch.solidC) + m.trim()+nl);
      for(int i=0; i < srch.na; i++) {
        w.write(String.format("%s",dbF.modelSelectedComps.get(i))+nl);
      } //for i

      w.flush();
      int j, jc, nTot;
      Complex cmplx;
      StringBuilder logB = new StringBuilder();
      for(int ix=0; ix < srch.nx+srch.nf; ix++) {
        cmplx = srch.dat.get(ix);
        if(cmplx.name.length()<=19) {
            w.write(String.format(engl, "%-19s,  ",cmplx.name));
        } else {w.write(String.format(engl, "%s,  ",cmplx.name));}
        if(logB.length()>0) {logB.delete(0, logB.length());}
        double lgK = cmplx.logKatTandP(srch.temperature_C, srch.pressure_bar);
        if(Double.isNaN(lgK)) {
          String msg = "Error in \"saveDataFile\","+nl+
                    "   species \""+cmplx.name+"\"  has logK = Not-a-Number."+nl+
                    "   while writing data file:"+nl+
                    "   \""+fn+"\"";
          MsgExceptn.exception(msg);
          javax.swing.JOptionPane.showMessageDialog(this, msg, pc.progName,javax.swing.JOptionPane.ERROR_MESSAGE);
          return false;
        }
        logB.append(Util.formatDbl3(lgK));
        //make logB occupy at least 9 chars: padding with space
        j = 9 - logB.length();
        if(j>0) {for(int k=0;k<j;k++) {logB.append(' ');}}
        else {logB.append(' ');} //add at least one space
        w.write(logB.toString());
        boolean fnd;
        for(jc = 0; jc < srch.na; jc++) {
            fnd = false;
            nTot = Math.min(cmplx.reactionComp.size(), cmplx.reactionCoef.size());
            for(int jdat=0; jdat < nTot; jdat++) {
                if(cmplx.reactionComp.get(jdat).equals(dbF.modelSelectedComps.get(jc))) {
                    w.write(Util.formatDbl4(cmplx.reactionCoef.get(jdat)));
                    if(jc < srch.na-1) {w.write(" ");}
                    fnd = true; break;
                }
            } //for jdat
            if(!fnd) {
                w.write(" 0");
                if(jc < srch.na-1) {w.write(" ");}
            }
        }//for jc
        if(cmplx.comment != null && cmplx.comment.length() >0) {
            w.write(" /"+cmplx.comment);
        }
        w.write(nl);
      }//for ix
      w.flush(); w.close(); fos.close();
      }
      catch (Exception ex) {
          String msg = "Error in \"saveDataFile\","+nl+
                    "   \""+ex.getMessage()+"\","+nl+
                    "   while writing to file:"+nl+
                    "   \""+tmpFileName+"\"";
          MsgExceptn.exception(msg);
          javax.swing.JOptionPane.showMessageDialog(this, msg, pc.progName,javax.swing.JOptionPane.ERROR_MESSAGE);
          return false;
      }

      //the temporary file has been created without a problem
      //  delete the data file and rename the temporary file
      java.io.File dataFile = new java.io.File(fn);
      dataFile.delete();
      if(!tmpFile.renameTo(dataFile)) {
          MsgExceptn.exception("Error in \"saveDataFile\":"+nl+
                  "   can not rename file \""+tmpFile.getAbsolutePath()+"\""+nl+
                  "   to \""+dataFile.getAbsolutePath()+"\"");
          return false;
      }
      dbF.outputDataFile = dataFile.getAbsolutePath();
      return true;
  } //saveDataFile

  //</editor-fold>


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonDiagram;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JLabel jLabelDiagram;
    private javax.swing.JLabel jLabelSave;
    // End of variables declaration//GEN-END:variables
}
