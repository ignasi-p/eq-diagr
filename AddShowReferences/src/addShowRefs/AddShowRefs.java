package addShowRefs;

import java.util.Arrays;
import lib.huvud.ProgramConf;
import lib.common.Util;
import lib.database.References;

/**
 * Copyright (C) 2015-2020 I.Puigdomenech.
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
public class AddShowRefs extends javax.swing.JFrame {
  // Note: for java 1.6 jComboBox must not have type,
  //       for java 1.7 jComboBox must be <String>
  private final String pathApp;
  private static final String progName = "AddRefs";
  private References r = null;
  /** true if it is possible to write to the references-file;
   * false if the file is read-only */
  private boolean canWriteRefs = true;

  private java.awt.Dimension windowSize = new java.awt.Dimension(400,280);

  /** New-line character(s) to substitute "\n" */
  private static final String nl = System.getProperty("line.separator");
  private static final String SLASH = java.io.File.separator;


  /** Creates new form AddShowRefs  */
  public AddShowRefs() {
    initComponents();
    pathApp = Main.getPathApp();
    this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
    //--- F1 for help
    javax.swing.KeyStroke f1KeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_F1,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(f1KeyStroke,"F1");
    javax.swing.Action f1Action = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            AddShowRefs.this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            Thread hlp = new Thread() {@Override public void run(){
                String[] a = {"DB_Supplied_htm"};
                lib.huvud.RunProgr.runProgramInProcess(null,ProgramConf.HELP_JAR,a,false,false,Main.getPathApp());
                try{Thread.sleep(2000);}   //show the "wait" cursor for 2 sec
                catch (InterruptedException e) {}
                AddShowRefs.this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }};//new Thread
            hlp.start();
        }};
    getRootPane().getActionMap().put("F1", f1Action);
    //---- Position the window on the screen
    java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    java.awt.Point frameLocation = new java.awt.Point(-1000,-1000);
    frameLocation.x = Math.max(0, (screenSize.width  - this.getWidth() ) / 2);
    frameLocation.y = Math.max(0, (screenSize.height - this.getHeight() ) / 2);
    this.setLocation(frameLocation);
    //---- Icon
    String iconName = "images/Refs.gif";
    java.net.URL imgURL = this.getClass().getResource(iconName);
    java.awt.Image icon;
    if (imgURL != null) {
        icon = new javax.swing.ImageIcon(imgURL).getImage();
        this.setIconImage(icon);
        //com.apple.eawt.Application.getApplication().setDockIconImage(new javax.swing.ImageIcon("Football.png").getImage());
        if(System.getProperty("os.name").startsWith("Mac OS")) {
            try {
                Class<?> c = Class.forName("com.apple.eawt.Application");
                //Class params[] = new Class[] {java.awt.Image.class};
                java.lang.reflect.Method m =
                    c.getDeclaredMethod("setDockIconImage",new Class[] { java.awt.Image.class });
                Object i = c.newInstance();
                Object paramsObj[] = new Object[]{icon};
                m.invoke(i, paramsObj);
            } catch (Exception e) {System.out.println("Error: "+e.getMessage());}
        }
    } else {
        System.out.println("Error: Could not load image = \""+iconName+"\"");
    }
    jComboBoxKeys.removeAllItems();
    jComboBoxKeys.setEnabled(false);
    jButtonShow.setEnabled(false);
    jButtonEdit.setEnabled(false);
    jButtonAdd.setEnabled(false);
    jLabelFileName.setText("(not selected yet...)");
    this.setTitle("Show references");
  }
  private void start() {
    AddShowRefs.this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
    jButtonShow.setEnabled(false);
    jButtonEdit.setEnabled(false);
    jButtonAdd.setEnabled(false);
    String t = getFileName(this);
    if(t == null) {
        this.dispose();
        return;
    } else {
        AddShowRefs.this.setVisible(true);
        jLabelFileName.setText(t);
    }
    AddShowRefs.this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
    r = new References();
    if(!r.readRefsFile(t, false)) {
        String msg = "File:"+nl+"   "+t+nl+"does not exist."+nl+nl+
                         "Create?"+nl+" ";
        Object[] opt = {"Yes", "Exit"};
        int m = javax.swing.JOptionPane.showOptionDialog(this, msg,
                "Save/Show References", javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
        if(m != javax.swing.JOptionPane.YES_OPTION) {
            this.dispose();
            return;
        }
        r.saveRefsFile(this, true);
    }
    fillComboBox();
    jButtonShow.setEnabled(true);
    windowSize = AddShowRefs.this.getSize();
    jButtonEdit.setEnabled(canWriteRefs);
    jButtonAdd.setEnabled(canWriteRefs);
    AddShowRefs.this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
  }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel0 = new javax.swing.JLabel();
        jLabelFileName = new javax.swing.JLabel();
        jButtonAdd = new javax.swing.JButton();
        jButtonQuit = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBoxKeys = new javax.swing.JComboBox<>();
        jButtonShow = new javax.swing.JButton();
        jButtonEdit = new javax.swing.JButton();
        jLabelUnicode = new javax.swing.JLabel();

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

        jLabel0.setText("Reference file:");

        jLabelFileName.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelFileName.setText("jLabelFileName");

        jButtonAdd.setMnemonic('a');
        jButtonAdd.setText(" Add new reference ");
        jButtonAdd.setAlignmentX(0.5F);
        jButtonAdd.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddActionPerformed(evt);
            }
        });

        jButtonQuit.setMnemonic('x');
        jButtonQuit.setText(" Exit ");
        jButtonQuit.setAlignmentX(0.5F);
        jButtonQuit.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButtonQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonQuitActionPerformed(evt);
            }
        });

        jLabel1.setText("Keys:");

        jComboBoxKeys.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jButtonShow.setMnemonic('s');
        jButtonShow.setText(" Show ");
        jButtonShow.setAlignmentX(0.5F);
        jButtonShow.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButtonShow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonShowActionPerformed(evt);
            }
        });

        jButtonEdit.setMnemonic('e');
        jButtonEdit.setText(" Edit ");
        jButtonEdit.setAlignmentX(0.5F);
        jButtonEdit.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButtonEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButtonShow)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonEdit))
                    .addComponent(jComboBoxKeys, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 10, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxKeys, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonShow)
                    .addComponent(jButtonEdit)))
        );

        jLabelUnicode.setText("(Unicode UTF-8)");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel0, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                        .addGap(33, 33, 33))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonQuit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabelUnicode))
                    .addComponent(jLabelFileName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButtonAdd))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel0)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelFileName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonQuit)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabelUnicode))))
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
        this.dispose();
    }//GEN-LAST:event_formWindowClosing

    private void jButtonQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonQuitActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButtonQuitActionPerformed

    private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddActionPerformed
      if(!canWriteRefs) {return;}
      this.setVisible(false);
      Thread t = new Thread() {@Override public void run(){
        EditAddRefs sr = new EditAddRefs(r);
        sr.setVisible(true);
        sr.start();
        final String k = sr.waitFor();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override
        public void run() {
            AddShowRefs.this.setVisible(true);
            AddShowRefs.this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            fillComboBox();
            setComboBoxKey(k);
            AddShowRefs.this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }}); //invokeLater(Runnable)
      }};//new Thread
      t.start();
    }//GEN-LAST:event_jButtonAddActionPerformed

    private void jButtonShowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonShowActionPerformed
        if(jComboBoxKeys.getSelectedIndex()<0) {
            javax.swing.JOptionPane.showMessageDialog(this, "No key is selected?", "Show refs", javax.swing.JOptionPane.QUESTION_MESSAGE);
            return;
        }
        java.util.ArrayList<String> a = new java.util.ArrayList<String>();
        a.add(jComboBoxKeys.getSelectedItem().toString());
        r.displayRefs(this, true, null, a);
    }//GEN-LAST:event_jButtonShowActionPerformed

    private void jButtonEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditActionPerformed
      if(!canWriteRefs) {return;}
      if(jComboBoxKeys.getSelectedIndex()<0) {
            javax.swing.JOptionPane.showMessageDialog(this, "No key is selected?", "Show refs", javax.swing.JOptionPane.QUESTION_MESSAGE);
            return;
      }
      this.setVisible(false);
      Thread t = new Thread() {@Override public void run(){
        EditAddRefs sr = new EditAddRefs(r);
        sr.setVisible(true);
        String key = jComboBoxKeys.getSelectedItem().toString();
        String reftxt = r.isRefThere(key);
        sr.start(key,reftxt);
        final String k = sr.waitFor();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override
        public void run() {
            AddShowRefs.this.setVisible(true);
            fillComboBox();
            setComboBoxKey(k);
        }}); //invokeLater(Runnable)
      }};//new Thread
      t.start();
    }//GEN-LAST:event_jButtonEditActionPerformed

  private void fillComboBox() {
    String[] k = r.referenceKeys();
    if(k == null || k.length <=0) {
        jComboBoxKeys.removeAllItems();
        jComboBoxKeys.setEnabled(false);
        jButtonShow.setEnabled(false);
        jButtonEdit.setEnabled(false);
        jButtonAdd.setEnabled(false);
        return;
    }
    jComboBoxKeys.setEnabled(true);
    jButtonShow.setEnabled(true);
    jButtonEdit.setEnabled(true);
    jButtonAdd.setEnabled(true);
    javax.swing.DefaultComboBoxModel<String> dcbm = new javax.swing.DefaultComboBoxModel<>();
    //javax.swing.DefaultComboBoxModel dcbm = new javax.swing.DefaultComboBoxModel(); // java 1.6
    java.util.ArrayList<String> keyList2 = new java.util.ArrayList<String>();
    keyList2.addAll(Arrays.asList(k));
    java.util.Collections.sort(keyList2);
    k = keyList2.toArray(new String[0]);
    for(String ex : k) {
        if(ex.length() >0) {dcbm.addElement(ex);}
    }
    jComboBoxKeys.setModel(dcbm);
  }
  private void setComboBoxKey(String key) {
      // -- makes the "key" the selected item in the combo box
      if(key == null || key.trim().length() <=0 || jComboBoxKeys.getItemCount() <=0) {return;}
      int fnd = -1;
      for(int j=0; j < jComboBoxKeys.getItemCount(); j++) {
          if(jComboBoxKeys.getItemAt(j).toString().equalsIgnoreCase(key)) {fnd = j; break;}
      }
      if(fnd >-1) {jComboBoxKeys.setSelectedIndex(fnd);}
  }

  //<editor-fold defaultstate="collapsed" desc="getFileName()">
  /**  Get a file name from the user using an Open File dialog.
   * If the file exists and it is not empty, read it and save it to sort the entries.
   * @param frame the parent component of the dialog
   * @return "null" if the user cancels the opertion; a file name otherwise. */
  private String getFileName(java.awt.Component frame) {
      frame.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
      // Ask the user for a file name using a Open File dialog
      boolean mustExist = false;
      final String refFileName = Util.getOpenFileName(this, progName,mustExist,
                "Select a text file with references:", 7,
                pathApp + SLASH + "References.txt", null);
      if(refFileName == null || refFileName.trim().length() <=0) {return null;}
      int answer;
      java.io.File refFile = new java.io.File(refFileName);
          if(!refFile.getName().toLowerCase().endsWith(".txt")) {
              refFile = new java.io.File(refFile.getAbsolutePath()+".txt");
              if(refFile.getName().contains(".")) {
                  Object[] opt = {"OK", "Cancel"};
                  answer = javax.swing.JOptionPane.showOptionDialog(frame,
                        "Note: file name must end with \".txt\""+nl+nl+
                        "The file name will be:"+nl+"\""+refFile.getName()+".txt\"",
                        progName, javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
                        if(answer != javax.swing.JOptionPane.YES_OPTION) {return null;}
              }
          }
          /* if(refFile.exists()) {
              Object[] opt = {"Ok","Cancel"};
              answer = javax.swing.JOptionPane.showOptionDialog(frame,
                        "File \""+refFile.getName()+"\" already exists!"+nl+nl+
                        "If you add or delete references"+nl+"the file will be overwritten",
                        progName, javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.QUESTION_MESSAGE,null,opt,opt[0]);
              if(answer != javax.swing.JOptionPane.YES_OPTION) {return null;}
          } */
          if(refFile.exists() && (!refFile.canWrite() || !refFile.setWritable(true))) {
              javax.swing.JOptionPane.showMessageDialog(frame,
                        "Can not overwrite file \""+refFile.getName()+"\"."+nl+
                        "The file or the directory is perhpas write-protected."+nl+nl+
                        "You will only be able to display existing references.",
                        progName, javax.swing.JOptionPane.ERROR_MESSAGE);
              canWriteRefs = false;
          }
          return refFile.getAbsolutePath();
  } // getFileName()
  // </editor-fold>

    /** @param args the command line arguments  */
    public static void main(String args[]) {
        boolean windows = System.getProperty("os.name").startsWith("Windows");
        //---- set Look-And-Feel
        try{
            if(windows) {
                javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
                System.out.println("--- setLookAndFeel(System);");
            } else {
                javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getCrossPlatformLookAndFeelClassName());
                System.out.println("--- setLookAndFeel(CrossPlatform);");
            }
        }
        catch (Exception ex) {System.out.println("Error: "+ex.getMessage());}
        //---- for JOptionPanes set the default button to the one with the focus
        //     so that pressing "enter" behaves as expected:
        javax.swing.UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
        //     and make the arrow keys work:
        Util.configureOptionPane();

        /*  Create and display the form  */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                AddShowRefs sr = new AddShowRefs();
                // sr.setVisible(true);
                sr.start();
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JButton jButtonEdit;
    private javax.swing.JButton jButtonQuit;
    private javax.swing.JButton jButtonShow;
    private javax.swing.JComboBox<String> jComboBoxKeys;
    private javax.swing.JLabel jLabel0;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelFileName;
    private javax.swing.JLabel jLabelUnicode;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
