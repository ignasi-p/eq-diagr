package spana;

import lib.common.Util;
import lib.huvud.Div;
import lib.huvud.ProgramConf;

/** Options dialog for general program behaviour.
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
public class OptionsGeneral extends javax.swing.JFrame {
  private boolean finished = false;
  private java.awt.Dimension windowSize;
  private ProgramConf pc;
  private final ProgramDataSpana pd;
  private static lib.huvud.RedirectedFrame msgFrame = null;
  private boolean associateDAT0, associatePLT0;
  /** Windows only: the full path of the application exe-file,
   * used when associating file types. Null if the user can not make file
   * associations (e.g if not admin rights) */
  private final String pathToExecute;
  /** New-line character(s) to substitute "\n". */
  private static final String nl = System.getProperty("line.separator");
  private static final String SLASH = java.io.File.separator;

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /** Creates new form OptionsGeneral
   * @param pc0
   * @param pd0
   * @param msgF */
  public OptionsGeneral(ProgramConf pc0, ProgramDataSpana pd0, lib.huvud.RedirectedFrame msgF) {
    initComponents();
    this.pc = pc0;
    this.pd = pd0;
    msgFrame = msgF;
    finished = false;

    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    //--- close window on ESC key
    javax.swing.KeyStroke escKeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(escKeyStroke,"ESCAPE");
    javax.swing.Action escAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            closeWindow();
        }};
    getRootPane().getActionMap().put("ESCAPE", escAction);
    //--- F1 for help
    javax.swing.KeyStroke f1KeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(f1KeyStroke,"F1");
    javax.swing.Action f1Action = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            OptionsGeneral.this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            Thread hlp = new Thread() {@Override public void run(){
                String[] a = {"S_Menu_Prefs_htm"};
                lib.huvud.RunProgr.runProgramInProcess(OptionsGeneral.this,ProgramConf.HELP_JAR,a,false,pc.dbg,pc.pathAPP);
                try{Thread.sleep(2000);}   //show the "wait" cursor for 2 sec
                catch (InterruptedException e) {}
                OptionsGeneral.this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }};//new Thread
            hlp.start();
        }};
    getRootPane().getActionMap().put("F1", f1Action);
    //--- Alt-H help
    javax.swing.KeyStroke altHKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altHKeyStroke,"ALT_H");
    getRootPane().getActionMap().put("ALT_H", f1Action);
    //--- alt-X
    javax.swing.KeyStroke altXKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altXKeyStroke,"ALT_X");
    javax.swing.Action altXAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jButton_OK.doClick();
        }};
    getRootPane().getActionMap().put("ALT_X", altXAction);
    //--- alt-P
    javax.swing.KeyStroke altPKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altPKeyStroke,"ALT_P");
    javax.swing.Action altPAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jTextF_ISP.requestFocus(); jTextF_ISPMouseClicked(null);
				}};
    getRootPane().getActionMap().put("ALT_P", altPAction);
    //--- alt-C
    javax.swing.KeyStroke altCKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altCKeyStroke,"ALT_C");
    javax.swing.Action altCAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jTextFnewData.requestFocus();
            jTextFnewDataMouseClicked(null);
				}};
    getRootPane().getActionMap().put("ALT_C", altCAction);
    //--- alt-T
    javax.swing.KeyStroke altTKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altTKeyStroke,"ALT_T");
    javax.swing.Action altTAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jTextFedit.requestFocus();
            jTextFeditMouseClicked(null);
				}};
    getRootPane().getActionMap().put("ALT_T", altTAction);
    //
    //--- Title
    this.setTitle("Preferences:");
    //--- Icon
    String iconName = "images/Wrench_32x32.gif";
    java.net.URL imgURL = this.getClass().getResource(iconName);
    if (imgURL != null) {this.setIconImage(new javax.swing.ImageIcon(imgURL).getImage());}
    else {System.out.println("Error: Could not load image = \""+iconName+"\"");}

    //if(MainFrame.windows && (!isAdmin() || !canWriteToProgramFiles())) {
    //    jLabelNote.setText("You have no \"admin\" rights...");
    //}
    if(MainFrame.windows //&& isAdmin() && canWriteToProgramFiles()
            && pc.pathAPP != null && pc.pathAPP.trim().length()>0) {
        String dir = pc.pathAPP;
        if(dir.endsWith(SLASH)) {dir = dir.substring(0,dir.length()-1);}
        pathToExecute = dir +SLASH+ pc.progName+".exe";
    } else {pathToExecute = null;}

    setupFrame();

    frameResize();

    //center Window on Screen
    int left; int top;
    left = Math.max(55, (MainFrame.screenSize.width  - windowSize.width ) / 2);
    top = Math.max(10, (MainFrame.screenSize.height - windowSize.height) / 2);
    this.setLocation(Math.min(MainFrame.screenSize.width-100, left),
                         Math.min(MainFrame.screenSize.height-100, top));
    setVisible(true);

  } // constructor

  public static boolean isAdmin() {
    String groups[];
    try {
        groups = (new com.sun.security.auth.module.NTSystem()).getGroupIDs();
    } catch (Exception e) {return false;}
    if(groups == null || groups.length <= 0) {return false;}
    for (String group : groups) {
        if (group.equals("S-1-5-32-544"))
            return true;
    }
    return false;
  }
  public static boolean isAdmin2(){
    java.util.prefs.Preferences prefs = java.util.prefs.Preferences.systemRoot();
    try{
        prefs.put("foo", "bar"); // SecurityException on Windows
        prefs.remove("foo");
        prefs.flush(); // BackingStoreException on Linux
        return true;
    }catch(java.util.prefs.BackingStoreException e){return false;}
  }
/**
  private boolean canWriteToProgramFiles() {
    try {
        String programFiles = System.getenv("ProgramFiles");
        if(programFiles == null) {programFiles = "C:\\Program Files";}
        java.io.File temp = new java.io.File(programFiles, "deleteMe.txt");
        if(temp.createNewFile()) {
            temp.delete();
            return true;
        } else {return false;}
    } catch (java.io.IOException e) {return false;}
  }
 */

  //</editor-fold>

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanelLevel = new javax.swing.JPanel();
        jRadioB_LevelNorm = new javax.swing.JRadioButton();
        jRadioB_LevelAdv = new javax.swing.JRadioButton();
        jPanelButtons = new javax.swing.JPanel();
        jButton_OK = new javax.swing.JButton();
        jButton_Cancel = new javax.swing.JButton();
        jButton_Reset = new javax.swing.JButton();
        jPanelAdv = new javax.swing.JPanel();
        jLabel_ISP = new javax.swing.JLabel();
        jTextF_ISP = new javax.swing.JTextField();
        jLabelFnewData = new javax.swing.JLabel();
        jTextFnewData = new javax.swing.JTextField();
        jLabelFedit = new javax.swing.JLabel();
        jTextFedit = new javax.swing.JTextField();
        jLabelDebug = new javax.swing.JLabel();
        jCheckBoxOutput = new javax.swing.JCheckBox();
        jLabelOutput = new javax.swing.JLabel();
        jPanelAssoc = new javax.swing.JPanel();
        jCheckBoxDat = new javax.swing.JCheckBox();
        jCheckBoxPlt = new javax.swing.JCheckBox();
        jLabelNote = new javax.swing.JLabel();

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

        jPanelLevel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, " Program Level: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(51, 51, 255))); // NOI18N

        buttonGroup1.add(jRadioB_LevelNorm);
        jRadioB_LevelNorm.setMnemonic('n');
        jRadioB_LevelNorm.setSelected(true);
        jRadioB_LevelNorm.setText("<html><u>n</u>ormal</html>"); // NOI18N
        jRadioB_LevelNorm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioB_LevelNormActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioB_LevelAdv);
        jRadioB_LevelAdv.setMnemonic('a');
        jRadioB_LevelAdv.setText("<html><u>a</u>dvanced</html>"); // NOI18N
        jRadioB_LevelAdv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioB_LevelAdvActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelLevelLayout = new javax.swing.GroupLayout(jPanelLevel);
        jPanelLevel.setLayout(jPanelLevelLayout);
        jPanelLevelLayout.setHorizontalGroup(
            jPanelLevelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLevelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelLevelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioB_LevelNorm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioB_LevelAdv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(28, Short.MAX_VALUE))
        );
        jPanelLevelLayout.setVerticalGroup(
            jPanelLevelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLevelLayout.createSequentialGroup()
                .addComponent(jRadioB_LevelNorm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioB_LevelAdv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jButton_OK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/spana/images/OK_32x32.gif"))); // NOI18N
        jButton_OK.setMnemonic('O');
        jButton_OK.setText("OK");
        jButton_OK.setToolTipText("OK (Alt-O orAlt-X)"); // NOI18N
        jButton_OK.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton_OK.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_OK.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton_OK.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_OK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_OKActionPerformed(evt);
            }
        });

        jButton_Cancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/spana/images/Trash.gif"))); // NOI18N
        jButton_Cancel.setMnemonic('Q');
        jButton_Cancel.setText("Quit");
        jButton_Cancel.setToolTipText("Cancel (Esc or Alt-Q)"); // NOI18N
        jButton_Cancel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton_Cancel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Cancel.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton_Cancel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_CancelActionPerformed(evt);
            }
        });

        jButton_Reset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/spana/images/Setup.gif"))); // NOI18N
        jButton_Reset.setMnemonic('R');
        jButton_Reset.setText("Reset");
        jButton_Reset.setToolTipText("Reset to default values (Alt-R)"); // NOI18N
        jButton_Reset.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton_Reset.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Reset.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton_Reset.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Reset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ResetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelButtonsLayout = new javax.swing.GroupLayout(jPanelButtons);
        jPanelButtons.setLayout(jPanelButtonsLayout);
        jPanelButtonsLayout.setHorizontalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton_OK)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton_Cancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton_Reset)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_OK)
                    .addComponent(jButton_Cancel)
                    .addComponent(jButton_Reset)))
        );

        jLabel_ISP.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel_ISP.setForeground(new java.awt.Color(51, 51, 255));
        jLabel_ISP.setText("<html><u>P</u>ath to SED and PREDOM:</html>"); // NOI18N

        jTextF_ISP.setText("jTextField1"); // NOI18N
        jTextF_ISP.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextF_ISPFocusGained(evt);
            }
        });
        jTextF_ISP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextF_ISPKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextF_ISPKeyTyped(evt);
            }
        });
        jTextF_ISP.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextF_ISPMouseClicked(evt);
            }
        });

        jLabelFnewData.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelFnewData.setForeground(new java.awt.Color(51, 51, 255));
        jLabelFnewData.setText("<html>Program used to <u>c</u>reate new data files:</html>"); // NOI18N

        jTextFnewData.setText("jTextField2"); // NOI18N
        jTextFnewData.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextFnewDataMouseClicked(evt);
            }
        });
        jTextFnewData.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFnewDataFocusGained(evt);
            }
        });
        jTextFnewData.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextFnewDataKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFnewDataKeyTyped(evt);
            }
        });

        jLabelFedit.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelFedit.setForeground(new java.awt.Color(102, 102, 102));
        jLabelFedit.setText("<html><u>T</u>ext editor:</html>"); // NOI18N

        jTextFedit.setText("jTextField3"); // NOI18N
        jTextFedit.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFeditFocusGained(evt);
            }
        });
        jTextFedit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextFeditKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFeditKeyTyped(evt);
            }
        });
        jTextFedit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextFeditMouseClicked(evt);
            }
        });

        jLabelDebug.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelDebug.setForeground(new java.awt.Color(51, 51, 255));
        jLabelDebug.setLabelFor(jCheckBoxOutput);
        jLabelDebug.setText("Debugging:"); // NOI18N

        jCheckBoxOutput.setMnemonic('v');
        jCheckBoxOutput.setText("<html><u>V</u>erbose output of program messages</html>"); // NOI18N

        jLabelOutput.setLabelFor(jCheckBoxOutput);
        jLabelOutput.setText("<html>Note: messages and errors are written to a separate frame<br>which is shown / hidden from menu &quot;Preferences&quot;</html>"); // NOI18N
        jLabelOutput.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jPanelAssoc.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Associate file extensions with this program: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(51, 51, 255))); // NOI18N

        jCheckBoxDat.setText("<html><b>.</b><u>D</u>AT</html>"); // NOI18N
        jCheckBoxDat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxDatActionPerformed(evt);
            }
        });

        jCheckBoxPlt.setText("<html><b>.</b>P<u>L</u>T</html>"); // NOI18N
        jCheckBoxPlt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxPltActionPerformed(evt);
            }
        });

        jLabelNote.setText("<html>Note: this might conflict with other<br>programs installed on your computer</html>"); // NOI18N

        javax.swing.GroupLayout jPanelAssocLayout = new javax.swing.GroupLayout(jPanelAssoc);
        jPanelAssoc.setLayout(jPanelAssocLayout);
        jPanelAssocLayout.setHorizontalGroup(
            jPanelAssocLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAssocLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelAssocLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxDat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBoxPlt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabelNote, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelAssocLayout.setVerticalGroup(
            jPanelAssocLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAssocLayout.createSequentialGroup()
                .addComponent(jCheckBoxDat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jCheckBoxPlt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jLabelNote, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout jPanelAdvLayout = new javax.swing.GroupLayout(jPanelAdv);
        jPanelAdv.setLayout(jPanelAdvLayout);
        jPanelAdvLayout.setHorizontalGroup(
            jPanelAdvLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAdvLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelAdvLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFnewData)
                    .addComponent(jTextF_ISP)
                    .addComponent(jTextFedit)
                    .addComponent(jLabelOutput, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelAdvLayout.createSequentialGroup()
                        .addGroup(jPanelAdvLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel_ISP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelFnewData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelFedit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanelAdvLayout.createSequentialGroup()
                                .addComponent(jLabelDebug)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jCheckBoxOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(jPanelAssoc, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanelAdvLayout.setVerticalGroup(
            jPanelAdvLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAdvLayout.createSequentialGroup()
                .addComponent(jLabel_ISP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextF_ISP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelFnewData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFnewData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelFedit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFedit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelAdvLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelDebug)
                    .addComponent(jCheckBoxOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addComponent(jPanelAssoc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(50, 50, 50)
                        .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 56, Short.MAX_VALUE))
                    .addComponent(jPanelAdv, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanelLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addComponent(jPanelAdv, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

  //<editor-fold defaultstate="collapsed" desc="Events">

    private void jRadioB_LevelNormActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioB_LevelNormActionPerformed
        frameResize();
}//GEN-LAST:event_jRadioB_LevelNormActionPerformed

    private void jRadioB_LevelAdvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioB_LevelAdvActionPerformed
        frameResize();
}//GEN-LAST:event_jRadioB_LevelAdvActionPerformed

    private void jButton_OKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_OKActionPerformed
      pd.advancedVersion = jRadioB_LevelAdv.isSelected();

      if(jTextF_ISP.getText() != null) {MainFrame.pathSedPredom = jTextF_ISP.getText();}
      if(jTextFnewData.getText() != null) {MainFrame.createDataFileProg = jTextFnewData.getText();}
      if(jTextFedit.getText() != null) {MainFrame.txtEditor = jTextFedit.getText();}

      pc.dbg = jCheckBoxOutput.isSelected();

      if(MainFrame.windows && pathToExecute != null) { // Windows registry
        boolean changed = false;
            try{
              if(!associateDAT0 && jCheckBoxDat.isSelected()) {
                  System.out.println("Associating \"dat\" files.");
                  FileAssociation.associateFileExtension("dat", pathToExecute);
                  changed = true;
              } else if(associateDAT0 && !jCheckBoxDat.isSelected()) {
                  System.out.println("Un-associating \"dat\" files.");
                  FileAssociation.unAssociateFileExtension("dat");
                  changed = true;
              }
            } catch (java.io.IOException ex) {System.out.println(ex.getMessage());}
            catch (IllegalAccessException ex) {System.out.println(ex.getMessage());}
            catch (IllegalArgumentException ex) {System.out.println(ex.getMessage());}
            catch (java.lang.reflect.InvocationTargetException ex) {System.out.println(ex.getMessage());}
            catch (java.net.URISyntaxException ex) {System.out.println(ex.getMessage());}              
            try{
              if(!associatePLT0 && jCheckBoxPlt.isSelected()) {
                  System.out.println("Associating \"plt\" files.");
                  FileAssociation.associateFileExtension("plt", pathToExecute);
                  changed = true;
              } else if(associatePLT0 && !jCheckBoxPlt.isSelected()) {
                  System.out.println("Un-associating \"plt\" files.");
                  FileAssociation.unAssociateFileExtension("plt");
                  changed = true;
              }
            } catch (java.io.IOException ex) {System.out.println(ex.getMessage());}
            catch (IllegalAccessException ex) {System.out.println(ex.getMessage());}
            catch (IllegalArgumentException ex) {System.out.println(ex.getMessage());}
            catch (java.lang.reflect.InvocationTargetException ex) {System.out.println(ex.getMessage());}
            catch (java.net.URISyntaxException ex) {System.out.println(ex.getMessage());}
        if(changed) {
            //--- Call "shell32.dll", SHChangeNotify to update icons in folders
            // -- alternative: call a NSIS script:
            lib.huvud.RunProgr.runProgramInProcess(OptionsGeneral.this,
                    "ShellChangeNotify.exe",null,false,
                    pc.dbg,pc.pathAPP);
            // -- alternative using NativeCall
              //try{
              //  NativeCall.init();
              //  VoidCall ic = new VoidCall("shell32.dll","SHChangeNotify");
              //  int SHCNE_ASSOCCHANGED = 0x8000000;  int SHCNF_IDLIST = 0x0000000;
              //  ic.executeCall(new Object[] { SHCNE_ASSOCCHANGED, SHCNF_IDLIST, 0, 0 });
              //  ic.destroy();
              //} catch (Exception ex) {
              //  //notify the user?
              //}
        }
      } // if Windows
      closeWindow();
}//GEN-LAST:event_jButton_OKActionPerformed

    private void jButton_CancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_CancelActionPerformed
        closeWindow();
}//GEN-LAST:event_jButton_CancelActionPerformed

    private void jButton_ResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_ResetActionPerformed
      Object[] options = {"OK", "Cancel"};
      int n = javax.swing.JOptionPane.showOptionDialog(this,
            "Do you wish to set ALL program settings"+nl+
            "(general options, graphic window preferences, etc)"+nl+
            "to default values?",
            pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.WARNING_MESSAGE, null, options, null);
      if (n == javax.swing.JOptionPane.YES_OPTION) { //the first button is "ok"
        options[0] = "Yes";
        n = javax.swing.JOptionPane.showOptionDialog(this,
            "Are you sure?",
            pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.WARNING_MESSAGE, null, options, null);
         if(n  == javax.swing.JOptionPane.YES_OPTION) { //the first button is "yes"
                MainFrame.getInstance().iniDefaults();
                setupFrame();
                frameResize();
         }
      }
}//GEN-LAST:event_jButton_ResetActionPerformed

    private void jTextF_ISPFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextF_ISPFocusGained
        jTextF_ISP.selectAll();
}//GEN-LAST:event_jTextF_ISPFocusGained

    private void jTextF_ISPKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextF_ISPKeyPressed
        if(!Util.isKeyPressedOK(evt)) {evt.consume();}
}//GEN-LAST:event_jTextF_ISPKeyPressed

    private void jTextF_ISPKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextF_ISPKeyTyped
        if(consumeKeyTyped(evt)) {
            evt.consume();
            jTextF_ISPMouseClicked(null);
        }
}//GEN-LAST:event_jTextF_ISPKeyTyped

    private void jTextF_ISPMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextF_ISPMouseClicked
      // Ask the user for a file name using a Open File dialog
      java.io.File currDir;
      if(MainFrame.pathSedPredom != null) {currDir = new java.io.File(MainFrame.pathSedPredom);}
      else if(pc.pathAPP != null) {currDir = new java.io.File(pc.pathAPP);}
      else {currDir = new java.io.File(System.getProperty("user.dir"));}
      boolean mustExist = true;
      boolean openF = true;
      boolean filesOnly = false;
      String dir = Util.getFileName(this, pc.progName, openF, mustExist, filesOnly,
              "Select either a program or a directory:", 1, "SED.jar", currDir.getPath());
      if(dir == null || dir.length() <=0) {return;}
      java.io.File f = new java.io.File(dir);
      //if(!f.isDirectory()) {f = MainFrame.fc.getCurrentDirectory();}
      if(!f.isDirectory()) {
          f = f.getParentFile();
      }
      dir = null;
      try {dir = f.getCanonicalPath();} catch (java.io.IOException ex) {}
      if (dir == null) {
        try {dir = f.getAbsolutePath();} catch (Exception ex) {dir = f.getPath();}
      }
      String msg = null;
      if(!Div.progSEDexists(f)) {msg = " SED";}
      if(!Div.progPredomExists(f)) {
              if(msg == null) {msg = " Predom";} else {msg = "s SED and Predom";}
      }
      if(msg != null) {
      javax.swing.JOptionPane.showMessageDialog(this,
                    "Program"+msg+" not found in the slected directory:" +nl+
                    "\""+dir+"\""+nl+nl+
                    "Selection discarded.",
                    pc.progName+" Preferences", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
      }
      jTextF_ISP.setText(dir);
}//GEN-LAST:event_jTextF_ISPMouseClicked

    private void jTextFnewDataFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFnewDataFocusGained
        jTextFnewData.selectAll();
}//GEN-LAST:event_jTextFnewDataFocusGained

    private void jTextFnewDataKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFnewDataKeyPressed
        if(!Util.isKeyPressedOK(evt)) {evt.consume();}
}//GEN-LAST:event_jTextFnewDataKeyPressed

    private void jTextFnewDataKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFnewDataKeyTyped
        if(consumeKeyTyped(evt)) {
            evt.consume();
            jTextFnewDataMouseClicked(null);
        }
}//GEN-LAST:event_jTextFnewDataKeyTyped

    private void jTextFnewDataMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextFnewDataMouseClicked
      boolean mustExist = true;
      String prgr = Util.getOpenFileName(this, pc.progName, mustExist,
              "Select a program:", 1, "DataBase.jar", pc.pathAPP);
      if(prgr == null || prgr.length() <=0) {return;}
      java.io.File f = new java.io.File(prgr);
      String fName = null;
      try {fName = f.getCanonicalPath();} catch (java.io.IOException ex) {}
      if(fName == null) {
        try {fName = f.getAbsolutePath();} catch (Exception ex) {fName = f.getPath();}
      }
      jTextFnewData.setText(fName);
}//GEN-LAST:event_jTextFnewDataMouseClicked

    private void jTextFeditFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFeditFocusGained
        jTextFedit.selectAll();
}//GEN-LAST:event_jTextFeditFocusGained

    private void jTextFeditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFeditKeyPressed
        if(!Util.isKeyPressedOK(evt)) {evt.consume();}
}//GEN-LAST:event_jTextFeditKeyPressed

    private void jTextFeditKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFeditKeyTyped
        if(consumeKeyTyped(evt)) {
            evt.consume();
            jTextFeditMouseClicked(null);
        }
}//GEN-LAST:event_jTextFeditKeyTyped

    private void jTextFeditMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextFeditMouseClicked
      // Ask the user for a file name using a Open File dialog
      String edt = Util.getOpenFileName(this, pc.progName, true,
              "Select an editor:", 1, null, pc.pathAPP);
      if(edt == null || edt.length() <=0) {return;}
      java.io.File f = new java.io.File(edt);
      String fName = null;
      try {fName = f.getCanonicalPath();} catch (java.io.IOException ex) {}
      if (fName == null) {
        try {fName = f.getAbsolutePath();} catch (Exception ex) {fName = f.getPath();}
      }
      jTextFedit.setText(fName);
}//GEN-LAST:event_jTextFeditMouseClicked

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

    private void jCheckBoxDatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDatActionPerformed
        if((jCheckBoxDat.isSelected())
                || (jCheckBoxPlt.isSelected())) {
            jLabelNote.setVisible(true);
        } else {
            jLabelNote.setVisible(false);
        }
    }//GEN-LAST:event_jCheckBoxDatActionPerformed

    private void jCheckBoxPltActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxPltActionPerformed
        if((jCheckBoxDat.isSelected()) || (jCheckBoxPlt.isSelected())) {
            jLabelNote.setVisible(true);
        } else {
            jLabelNote.setVisible(false);
        }
    }//GEN-LAST:event_jCheckBoxPltActionPerformed

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Methods">

  private void closeWindow() {
    finished = true;
    this.notify_All();
    this.dispose();
  } // closeWindow()
  private synchronized void notify_All() {notifyAll();}
  /** this method will wait for this dialog frame to be closed */
  public synchronized void waitFor() {
    while(!finished) {
        try {wait();} catch (InterruptedException ex) {}
    } // while
  } // waitFor()

  private void frameResize() {
    if(jRadioB_LevelNorm.isSelected()) {
        jPanelAdv.setVisible(false);
        jPanelAssoc.setVisible(false);
        pack();
        windowSize = this.getSize();
    } else {
        jPanelAdv.setVisible(true);
        if(MainFrame.windows) {jPanelAssoc.setVisible(true);}
        pack();
        windowSize = this.getSize();
    }
  }

  private boolean consumeKeyTyped(java.awt.event.KeyEvent evt) {
    char c = Character.toUpperCase(evt.getKeyChar());
    if(!Character.isISOControl(evt.getKeyChar()) &&
            evt.getKeyChar() != java.awt.event.KeyEvent.VK_ESCAPE ) {
        if (!( evt.isAltDown() &&
                (evt.getKeyChar() != java.awt.event.KeyEvent.VK_ENTER ||
                (c == 'N') || (c == 'A') || (c == 'P') || (c == 'C') ||
                (c == 'T') || (c == 'K') || (c == 'D') || (c == 'L') ||
                (c == 'O') || (c == 'X') || (c == 'Q')) )) {
            return true;
          }
    }
    return false;
  }

  private void setupFrame() {
    if(pd.advancedVersion) {jRadioB_LevelAdv.setSelected(true);}
    else {jRadioB_LevelNorm.setSelected(true);}

    if(MainFrame.pathSedPredom != null) {
        jTextF_ISP.setText(MainFrame.pathSedPredom);
    } else {jTextF_ISP.setText("");}
    if(MainFrame.createDataFileProg != null) {
        jTextFnewData.setText(MainFrame.createDataFileProg);
    } else {jTextFnewData.setText("");}

    if(MainFrame.txtEditor != null) {
        jTextFedit.setText(MainFrame.txtEditor);
    } else {jTextFedit.setText("");}
    if(System.getProperty("os.name").startsWith("Mac OS")) {
        jLabelFedit.setForeground(new java.awt.Color(102, 102, 102));
        jTextFedit.setEnabled(false);
    } else { // not MacOS
        jLabelFedit.setForeground(new java.awt.Color(51, 51, 255));
        jTextFedit.setEnabled(true);
    }
    jCheckBoxOutput.setSelected(pc.dbg);
    if(!MainFrame.windows) {
        jPanelAssoc.setVisible(false);
    } else { // Windows
        jPanelAssoc.setVisible(true);
        associateDAT0 = false;
        associatePLT0 = false;
        if(pathToExecute != null) { // look at Windows registry
            boolean msgFramePopUp = msgFrame.isPopupOnErr();
            msgFrame.setPopupOnErr(false);
            try {
                associateDAT0 = FileAssociation.isAssociated("dat", pathToExecute);
                associatePLT0 = FileAssociation.isAssociated("plt", pathToExecute);
            }
            catch (IllegalAccessException ex) {System.out.println(ex.getMessage());}
            catch (IllegalArgumentException ex) {System.out.println(ex.getMessage());}
            catch (java.lang.reflect.InvocationTargetException ex) {System.out.println(ex.getMessage());}
            msgFrame.setPopupOnErr(msgFramePopUp);
            jCheckBoxDat.setSelected(associateDAT0);
            jCheckBoxPlt.setSelected(associatePLT0);
            if(associateDAT0 || associatePLT0) {
                jLabelNote.setVisible(true);
            } else {jLabelNote.setVisible(false);}
        } else {
            jCheckBoxDat.setText(".DAT");
            jCheckBoxDat.setEnabled(false);
            jCheckBoxPlt.setText(".PLT");
            jCheckBoxPlt.setEnabled(false);
        }
    } // windows
  } //setupFrame

  //</editor-fold>

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton_Cancel;
    private javax.swing.JButton jButton_OK;
    private javax.swing.JButton jButton_Reset;
    private javax.swing.JCheckBox jCheckBoxDat;
    private javax.swing.JCheckBox jCheckBoxOutput;
    private javax.swing.JCheckBox jCheckBoxPlt;
    private javax.swing.JLabel jLabelDebug;
    private javax.swing.JLabel jLabelFedit;
    private javax.swing.JLabel jLabelFnewData;
    private javax.swing.JLabel jLabelNote;
    private javax.swing.JLabel jLabelOutput;
    private javax.swing.JLabel jLabel_ISP;
    private javax.swing.JPanel jPanelAdv;
    private javax.swing.JPanel jPanelAssoc;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JPanel jPanelLevel;
    private javax.swing.JRadioButton jRadioB_LevelAdv;
    private javax.swing.JRadioButton jRadioB_LevelNorm;
    private javax.swing.JTextField jTextF_ISP;
    private javax.swing.JTextField jTextFedit;
    private javax.swing.JTextField jTextFnewData;
    // End of variables declaration//GEN-END:variables

}
