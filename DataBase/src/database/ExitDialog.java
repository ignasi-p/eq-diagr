package database;

import lib.common.MsgExceptn;
import lib.common.Util;
import lib.database.Complex;
import lib.database.LibDB;
import lib.database.ProgramDataDB;
import lib.huvud.ProgramConf;

/** Exit dialog.
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
System.out.println("hs.temperature="+(float)hs.temperature);
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
                try{Thread.sleep(1500);}   //show the "wait" cursor for 1.5 sec
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

        jButtonDiagram.setIcon(new javax.swing.ImageIcon(getClass().getResource("/database/images/Diagram_button.gif"))); // NOI18N
        jButtonDiagram.setMnemonic('d');
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
        int w = Math.round((float)windowSize.getWidth());
        int h = Math.round((float)windowSize.getHeight());
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
      boolean fnd = false;
      // Is there enthalpy data?
      if(srch.temperature < 24.99 || srch.temperature > 25.01) {
        java.util.ArrayList<String> items = new java.util.ArrayList<String>();
        long cnt = 0;
        for(int ix=0; ix < srch.nx+srch.nf; ix++) {
            if(srch.dat.get(ix).deltH == Complex.EMPTY) {
                if(!fnd) {System.out.println("--------- Temperature extrapolations"); fnd = true;}
                System.out.println("species \""+srch.dat.get(ix).name+"\": missing enthalpy.");
                items.add(srch.dat.get(ix).name);
                cnt++;
            }
        }
        if(cnt >0) {
            System.out.println("---------");
            javax.swing.DefaultListModel<String> aModel = new javax.swing.DefaultListModel<>();
            //javax.swing.DefaultListModel aModel = new javax.swing.DefaultListModel(); // java 1.6
            java.util.Iterator<String> iter = items.iterator();
            while(iter.hasNext()) {aModel.addElement(iter.next());}
            String msg = "<html><b>Error:</b><br>"
                    + "Temperature extrapolations from 25 to "+String.format("%.0f",srch.temperature)+"°C are required,<br>"
                    +"but enthalpy values are missing for the following species:</html>";
            javax.swing.JLabel aLabel = new javax.swing.JLabel(msg);
            // javax.swing.JList aList = new javax.swing.JList(aModel); // java 1.6
            javax.swing.JList<String> aList = new javax.swing.JList<>(aModel);
            aList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            aList.setVisibleRowCount(5);
            javax.swing.JScrollPane aScrollPane = new javax.swing.JScrollPane();
            aScrollPane.setViewportView(aList);
            aList.setFocusable(false);
            javax.swing.JLabel endLabel = new javax.swing.JLabel("Please change the temperature to 25°C in the menu \"Options\".");
            Object[] o = {aLabel, aScrollPane, endLabel};
            javax.swing.JOptionPane.showMessageDialog(this, o,
                    "Temperature extrapolations",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return false;
        }
      } // temperature?
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
      java.io.PrintWriter outputFile;
      try{
          outputFile = new java.io.PrintWriter(
                new java.io.BufferedWriter(
                new java.io.FileWriter(tmpFile)));
      }
      catch (java.io.IOException ex) {
          String msg = "Error in \"saveDataFile\","+nl+
                    "   \""+ex.toString()+"\","+nl+
                    "   while making a PrintWriter for data file:"+nl+
                    "   \""+tmpFileName+"\"";
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
      if(!Double.isNaN(srch.temperature)) {m = m + ", t="+Util.formatDbl3(srch.temperature);}
      if(!Double.isNaN(srch.pressure)) {m = m + ", p="+Util.formatDbl3(srch.pressure);}
      //if requested: add water
      if(pd.includeH2O) {
          dbF.modelSelectedComps.add((srch.na - srch.solidC), "H2O");
          srch.na++;
      }
      outputFile.println(" "+String.valueOf(srch.na)+", "+
          String.valueOf(srch.nx)+", "+String.valueOf(nrSol)+", "+
          String.valueOf(srch.solidC) + m.trim());
      for(int i=0; i < srch.na; i++) {
        outputFile.format("%s",dbF.modelSelectedComps.get(i));
        outputFile.println();
      } //for i

      outputFile.flush();
      int j, jc;
      StringBuilder logB = new StringBuilder();
      for(int ix=0; ix < srch.nx+srch.nf; ix++) {
        if(srch.dat.get(ix).name.length()<=19) {
            outputFile.format(engl, "%-19s,  ",srch.dat.get(ix).name);
        } else {outputFile.format(engl, "%s,  ",srch.dat.get(ix).name);}
        if(logB.length()>0) {logB.delete(0, logB.length());}
        double lgK = Complex.constCp(srch.dat.get(ix).constant, srch.dat.get(ix).deltH, srch.dat.get(ix).deltCp, srch.temperature);
        logB.append(Util.formatDbl3(lgK));
        //make logB occupy at least 9 chars: padding with space
        j = 9 - logB.length();
        if(j>0) {for(int k=0;k<j;k++) {logB.append(' ');}}
        else {logB.append(' ');} //add at least one space
        outputFile.print(logB.toString());
        for(jc = 0; jc < srch.na; jc++) {
            fnd = false;
            for(int jdat=0; jdat < Complex.NDIM; jdat++) {
                if(srch.dat.get(ix).component[jdat].equals(dbF.modelSelectedComps.get(jc))) {
                    outputFile.print(Util.formatDbl4(srch.dat.get(ix).numcomp[jdat]));
                    if(jc < srch.na-1) {outputFile.print(" ");}
                    fnd = true; break;
                }
            } //for jdat
            if(!fnd) {
                if(Util.isProton(dbF.modelSelectedComps.get(jc).toString()) && 
                        Math.abs(srch.dat.get(ix).proton) > 0.001) {
                    fnd = true;
                    outputFile.print(Util.formatDbl4(srch.dat.get(ix).proton));
                    if(jc < srch.na-1) {outputFile.print(" ");}
                }
            } //!fnd
            if(!fnd) {
                outputFile.print(" 0");
                if(jc < srch.na-1) {outputFile.print(" ");}
            }
        }//for jc
        if(srch.dat.get(ix).comment != null && srch.dat.get(ix).comment.length() >0) {
            outputFile.print(" /"+srch.dat.get(ix).comment);
        }
        outputFile.println();
      }//for ix
      outputFile.flush(); outputFile.close();

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
