package spana;

import lib.common.MsgExceptn;
import lib.common.Util;
import lib.huvud.ProgramConf;
import lib.kemi.chem.Chem;
import lib.kemi.readDataLib.ReadDataLib;
import lib.kemi.readWriteDataFiles.DefaultPlotAndConcs;
import lib.kemi.readWriteDataFiles.ReadChemSyst;
import lib.kemi.readWriteDataFiles.WriteChemSyst;

/** This "frame" is used to allow the user to modyfy a chemical system: delete
 * components or reactions, excahnge a component for a reaction product, etc.
 * The data file is read and only the definition of the chemical system is changed:
 * the plot information and concentrations for each component are not changed,
 * but when finishing, if the user decides to save the new chemical system, the
 * plot information and concentrations for each component are adjusted depending
 * on the new components.
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
public class ModifyChemSyst extends javax.swing.JFrame {
    private final ProgramDataSpana pd;
    private ProgramConf pc;
    private boolean dbg = true;
    private spana.ModifyChemSyst thisFrame = null;
    private final java.awt.Dimension windowSize;
    private boolean finished = false;
    private java.io.File dataFile;
    /** the main Chem instance */
    private Chem ch = null;
    /** a Chem instance used to read a chemical systems from a data file */
    private Chem chRead = null;
    /** a Chem instance used when merging two chemical systems */
    private Chem ch2 = null;
    /** the main ChemSystem instance */
    private Chem.ChemSystem cs = null;
    /** the main ChemSystem.NamesEtc instance */
    private Chem.ChemSystem.NamesEtc namn = null;
    /** the main Diagr instance */
    private Chem.Diagr diagr = null;
    /** the main DiagrConcs instance */
    private Chem.DiagrConcs dgrC = null;
    private final java.util.ArrayList<String> identC0 = new java.util.ArrayList<String>();
    private String compXname0 = null;
    private String compYname0 = null;
    private String compMainName0 = null;
    private int Na0;
    /** java 1.6
    private final javax.swing.DefaultListModel listSolubleCompModel = new javax.swing.DefaultListModel();
    private final javax.swing.DefaultListModel listSolidCompModel = new javax.swing.DefaultListModel();
    private final javax.swing.DefaultListModel listSolubleCmplxModel = new javax.swing.DefaultListModel();
    private final javax.swing.DefaultListModel listSolidCmplxModel = new javax.swing.DefaultListModel();
    */
    private final javax.swing.DefaultListModel<String> listSolubleCompModel = new javax.swing.DefaultListModel<>();
    private final javax.swing.DefaultListModel<String> listSolidCompModel = new javax.swing.DefaultListModel<>();
    private final javax.swing.DefaultListModel<String> listSolubleCmplxModel = new javax.swing.DefaultListModel<>();
    private final javax.swing.DefaultListModel<String> listSolidCmplxModel = new javax.swing.DefaultListModel<>();

    private final javax.swing.border.Border defBorder;
    private final javax.swing.border.Border highlightedBorder = javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED, java.awt.Color.gray, java.awt.Color.black);
    /** has the initial chemical system been modified? */
    private boolean modified = false;
    /** has the initial chemical system been merged with another one? */
    private boolean merged = false;
    private int compToDelete;
    private int complxToExchange;
    private static final String noReactionMessage = "(double-click or type space on a name to change its logK)";
    private final static String nl = System.getProperty("line.separator");

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /** Creates new form ModifyChemSyst
   * @param pc0
   * @param pd0 */
  public ModifyChemSyst(
          ProgramConf pc0,
          spana.ProgramDataSpana pd0
          ) {
    initComponents();
    this.pd = pd0;
    this.pc = pc0;
    dbg = pc.dbg;

    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    //--- close window on ESC key
    javax.swing.KeyStroke escKeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(escKeyStroke,"ESCAPE");
    javax.swing.Action escAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jButton_Quit.doClick();
        }};
    getRootPane().getActionMap().put("ESCAPE", escAction);
    //--- F1 for help
    javax.swing.KeyStroke f1KeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(f1KeyStroke,"F1");
    javax.swing.Action f1Action = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            ModifyChemSyst.this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            Thread hlp = new Thread() {@Override public void run(){
                String[] a = {"S_Modify_Chem_System_htm"};
                lib.huvud.RunProgr.runProgramInProcess(ModifyChemSyst.this,ProgramConf.HELP_JAR,a,false,pc.dbg,pc.pathAPP);
                try{Thread.sleep(2000);}   //show the "wait" cursor for 2 sec
                catch (InterruptedException e) {}
                ModifyChemSyst.this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }};//new Thread
            hlp.start();
        }};
    getRootPane().getActionMap().put("F1", f1Action);
    // ---- Define Alt-keys
    //      Alt-Q is a button mnemonics
    //--- Alt-H help
    javax.swing.KeyStroke altHKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altHKeyStroke,"ALT_H");
    getRootPane().getActionMap().put("ALT_H", f1Action);
    //--- Alt-X quit
    javax.swing.KeyStroke altXKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altXKeyStroke,"ALT_X");
    javax.swing.Action altXAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jButton_Save.doClick();
        }};
    getRootPane().getActionMap().put("ALT_X", altXAction);

    //---- Icon
    String iconName = "images/Modify_32x32.gif";
    java.net.URL imgURL = this.getClass().getResource(iconName);
    if (imgURL != null) {this.setIconImage(new javax.swing.ImageIcon(imgURL).getImage());}
    else {System.out.println("Error: Could not load image = \""+iconName+"\"");}

    defBorder = jScrollPane1.getBorder();
    java.awt.CardLayout cl =(java.awt.CardLayout)jPanel1Top.getLayout();
    cl.show(jPanel1Top, "cardDataFile");

    jButton_Merge.setEnabled(pd.advancedVersion);
    jButton_Merge.setVisible(pd.advancedVersion);
    java.awt.Dimension d = jPanelUpDown.getPreferredSize();
    if(!pd.advancedVersion) {
      jButtonUp.setVisible(false);
      jButtonDn.setVisible(false);
    } else {
      jButtonUp.setEnabled(false);
      jButtonDn.setEnabled(false);
    }
    jLabelReaction.setText(" ");
    jLabelReactionSetSize();
    this.pack();
    //center Window on Screen
    windowSize = this.getSize();
    int left; int top;
    left = Math.max(0, (MainFrame.screenSize.width  - windowSize.width ) / 2);
    top = Math.max(0, (MainFrame.screenSize.height - windowSize.height) / 2);
    this.setLocation(Math.min(MainFrame.screenSize.width-100, left),
                     Math.min(MainFrame.screenSize.height-100, top));

    MainFrame.getInstance().setCursorDef();
    this.setVisible(true);
    javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
        thisFrame = ModifyChemSyst.this;
    }}); //invokeLater(Runnable)

  } // constructor
  //</editor-fold>


  //<editor-fold defaultstate="collapsed" desc="startDataFile(dataFile)">
 /** Reads a data file and arranges the window frame.
  * @param datFile */
  public void startDataFile(java.io.File datFile) {
    dataFile = datFile;
    if(dbg) {System.out.println(" ---- Modify Chemical System: "+datFile.getName());}
    if(!readDataFile(dataFile, pc.dbg)) {
        System.err.println(" ---- Error reading file \""+dataFile.getName()+"\"");
        MainFrame.getInstance().setCursorDef();
        quitFrame();
        return;}
    ch = chRead;
    chRead = null;
    cs = ch.chemSystem;
    namn = cs.namn;
    diagr = ch.diag;
    dgrC = ch.diagrConcs;
    if(cs.Na <=0) {return;} //this should not happen
    //--- store component names in axes, concs., etc, needed by checkPlotInfo()
    storeInitialSystem(ch);
    setupFrame();
    pack();

  } // startDataFile(dataFile)
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
        jPanel1Top = new javax.swing.JPanel();
        jPanelDataFile = new javax.swing.JPanel();
        jLabel0 = new javax.swing.JLabel();
        jTextFieldDataFile = new javax.swing.JTextField();
        jPanelNew = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jPanelUpDown = new javax.swing.JPanel();
        jButtonUp = new javax.swing.JButton();
        jButtonDn = new javax.swing.JButton();
        jPanel2up = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListSolubComps = new javax.swing.JList();
        jPanel6 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListSolubCmplx = new javax.swing.JList();
        jPanel2down = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jListSolidComps = new javax.swing.JList();
        jPanel8 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jListSolidCmplx = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jLabelReaction = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jButton_Delete = new javax.swing.JButton();
        jButton_Exchange = new javax.swing.JButton();
        jButton_Merge = new javax.swing.JButton();
        jPanel11 = new javax.swing.JPanel();
        jButton_Quit = new javax.swing.JButton();
        jButton_Save = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Modify Data File"); // NOI18N
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

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jPanel1MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jPanel1MouseExited(evt);
            }
        });

        jPanel1Top.setLayout(new java.awt.CardLayout());

        jLabel0.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel0.setForeground(new java.awt.Color(0, 0, 153));
        jLabel0.setLabelFor(jTextFieldDataFile);
        jLabel0.setText("Original Data File:"); // NOI18N
        jLabel0.setToolTipText("Click to change Data File"); // NOI18N

        jTextFieldDataFile.setText("jTextField1"); // NOI18N
        jTextFieldDataFile.setToolTipText("Click to change Data File"); // NOI18N
        jTextFieldDataFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldDataFileActionPerformed(evt);
            }
        });
        jTextFieldDataFile.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldDataFileFocusGained(evt);
            }
        });
        jTextFieldDataFile.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextFieldDataFileKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldDataFileKeyTyped(evt);
            }
        });
        jTextFieldDataFile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextFieldDataFileMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanelDataFileLayout = new javax.swing.GroupLayout(jPanelDataFile);
        jPanelDataFile.setLayout(jPanelDataFileLayout);
        jPanelDataFileLayout.setHorizontalGroup(
            jPanelDataFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDataFileLayout.createSequentialGroup()
                .addComponent(jLabel0)
                .addContainerGap(287, Short.MAX_VALUE))
            .addComponent(jTextFieldDataFile, javax.swing.GroupLayout.DEFAULT_SIZE, 385, Short.MAX_VALUE)
        );
        jPanelDataFileLayout.setVerticalGroup(
            jPanelDataFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDataFileLayout.createSequentialGroup()
                .addComponent(jLabel0)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldDataFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1Top.add(jPanelDataFile, "cardDataFile");

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 0, 153));
        jLabel5.setText("New Chemical System:"); // NOI18N

        javax.swing.GroupLayout jPanelNewLayout = new javax.swing.GroupLayout(jPanelNew);
        jPanelNew.setLayout(jPanelNewLayout);
        jPanelNewLayout.setHorizontalGroup(
            jPanelNewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelNewLayout.createSequentialGroup()
                .addGap(105, 105, 105)
                .addComponent(jLabel5)
                .addContainerGap(124, Short.MAX_VALUE))
        );
        jPanelNewLayout.setVerticalGroup(
            jPanelNewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelNewLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jPanel1Top.add(jPanelNew, "cardNewSystem");

        jButtonUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/spana/images/Arrow_up.gif"))); // NOI18N
        jButtonUp.setToolTipText("move component up"); // NOI18N
        jButtonUp.setMargin(new java.awt.Insets(1, 0, 1, 0));
        jButtonUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUpActionPerformed(evt);
            }
        });

        jButtonDn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/spana/images/Arrow_down.gif"))); // NOI18N
        jButtonDn.setToolTipText("move component down"); // NOI18N
        jButtonDn.setMargin(new java.awt.Insets(1, 0, 1, 0));
        jButtonDn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelUpDownLayout = new javax.swing.GroupLayout(jPanelUpDown);
        jPanelUpDown.setLayout(jPanelUpDownLayout);
        jPanelUpDownLayout.setHorizontalGroup(
            jPanelUpDownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelUpDownLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanelUpDownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonDn)
                    .addComponent(jButtonUp))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelUpDownLayout.setVerticalGroup(
            jPanelUpDownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelUpDownLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jButtonUp)
                .addGap(18, 18, 18)
                .addComponent(jButtonDn)
                .addContainerGap(179, Short.MAX_VALUE))
        );

        jPanel2up.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jPanel2upMouseEntered(evt);
            }
        });
        jPanel2up.setLayout(new javax.swing.BoxLayout(jPanel2up, javax.swing.BoxLayout.LINE_AXIS));

        jPanel5.setPreferredSize(new java.awt.Dimension(190, 141));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 153));
        jLabel1.setLabelFor(jScrollPane1);
        jLabel1.setText("Soluble Components:"); // NOI18N

        jListSolubComps.setModel(listSolubleCompModel);
        jListSolubComps.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListSolubComps.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jListSolubCompsMouseReleased(evt);
            }
        });
        jListSolubComps.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jListSolubCompsMouseMoved(evt);
            }
        });
        jListSolubComps.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jListSolubCompsFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jListSolubCompsFocusLost(evt);
            }
        });
        jListSolubComps.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jListSolubCompsKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jListSolubCompsKeyTyped(evt);
            }
        });
        jScrollPane1.setViewportView(jListSolubComps);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel2up.add(jPanel5);

        jPanel6.setPreferredSize(new java.awt.Dimension(190, 141));
        jPanel6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jPanel6MouseEntered(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 0, 153));
        jLabel2.setLabelFor(jScrollPane2);
        jLabel2.setText("Soluble Complexes:"); // NOI18N

        jScrollPane2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jScrollPane2MouseEntered(evt);
            }
        });

        jListSolubCmplx.setModel(listSolubleCmplxModel);
        jListSolubCmplx.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListSolubCmplxMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jListSolubCmplxMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jListSolubCmplxMouseExited(evt);
            }
        });
        jListSolubCmplx.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jListSolubCmplxMouseMoved(evt);
            }
        });
        jListSolubCmplx.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jListSolubCmplxFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jListSolubCmplxFocusLost(evt);
            }
        });
        jListSolubCmplx.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jListSolubCmplxKeyTyped(evt);
            }
        });
        jScrollPane2.setViewportView(jListSolubCmplx);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addContainerGap(65, Short.MAX_VALUE))))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel2up.add(jPanel6);

        jPanel2down.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jPanel2downMouseEntered(evt);
            }
        });
        jPanel2down.setLayout(new javax.swing.BoxLayout(jPanel2down, javax.swing.BoxLayout.LINE_AXIS));

        jPanel7.setPreferredSize(new java.awt.Dimension(190, 123));

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 0, 153));
        jLabel3.setLabelFor(jScrollPane3);
        jLabel3.setText("Solid Components"); // NOI18N

        jListSolidComps.setModel(listSolidCompModel);
        jListSolidComps.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListSolidComps.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jListSolidCompsMouseReleased(evt);
            }
        });
        jListSolidComps.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jListSolidCompsMouseMoved(evt);
            }
        });
        jListSolidComps.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jListSolidCompsFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jListSolidCompsFocusLost(evt);
            }
        });
        jListSolidComps.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jListSolidCompsKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jListSolidCompsKeyTyped(evt);
            }
        });
        jScrollPane3.setViewportView(jListSolidComps);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addGap(0, 83, Short.MAX_VALUE))
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE))
        );

        jPanel2down.add(jPanel7);

        jPanel8.setPreferredSize(new java.awt.Dimension(190, 123));
        jPanel8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jPanel8MouseEntered(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(0, 0, 153));
        jLabel4.setLabelFor(jScrollPane4);
        jLabel4.setText("Solid Products"); // NOI18N

        jScrollPane4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jScrollPane4MouseEntered(evt);
            }
        });

        jListSolidCmplx.setModel(listSolidCmplxModel);
        jListSolidCmplx.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListSolidCmplxMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jListSolidCmplxMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jListSolidCmplxMouseExited(evt);
            }
        });
        jListSolidCmplx.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jListSolidCmplxMouseMoved(evt);
            }
        });
        jListSolidCmplx.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jListSolidCmplxFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jListSolidCmplxFocusLost(evt);
            }
        });
        jListSolidCmplx.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jListSolidCmplxKeyTyped(evt);
            }
        });
        jScrollPane4.setViewportView(jListSolidCmplx);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(0, 93, Short.MAX_VALUE))))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE))
        );

        jPanel2down.add(jPanel8);

        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelReaction.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelReaction.setText("jLabelReaction");
        jPanel2.add(jLabelReaction, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 360, -1));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jPanel1Top, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanelUpDown, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2up, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(jPanel2down, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
                .addGap(10, 10, 10))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jPanel1Top, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanelUpDown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(86, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2up, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(0, 0, 0)
                        .addComponent(jPanel2down, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(0, 0, 0)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5))))
        );

        jButton_Delete.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton_Delete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/spana/images/Delete_32x32.gif"))); // NOI18N
        jButton_Delete.setMnemonic('D');
        jButton_Delete.setText("<html><u>D</u>elete</html>"); // NOI18N
        jButton_Delete.setToolTipText("Alt-D"); // NOI18N
        jButton_Delete.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        jButton_Delete.setIconTextGap(8);
        jButton_Delete.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton_Delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_DeleteActionPerformed(evt);
            }
        });

        jButton_Exchange.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton_Exchange.setIcon(new javax.swing.ImageIcon(getClass().getResource("/spana/images/Exchange_32x32.gif"))); // NOI18N
        jButton_Exchange.setMnemonic('E');
        jButton_Exchange.setText("<html><u>E</u>xchange a component<br>with a reaction</html>"); // NOI18N
        jButton_Exchange.setToolTipText("Alt-E"); // NOI18N
        jButton_Exchange.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        jButton_Exchange.setIconTextGap(8);
        jButton_Exchange.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton_Exchange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ExchangeActionPerformed(evt);
            }
        });

        jButton_Merge.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton_Merge.setIcon(new javax.swing.ImageIcon(getClass().getResource("/spana/images/Merge_32x32.gif"))); // NOI18N
        jButton_Merge.setMnemonic('M');
        jButton_Merge.setText("<html><u>M</u>erge with another<br> data file</html>"); // NOI18N
        jButton_Merge.setToolTipText("Alt-M"); // NOI18N
        jButton_Merge.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        jButton_Merge.setIconTextGap(8);
        jButton_Merge.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton_Merge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_MergeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jButton_Exchange, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jButton_Delete, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jButton_Merge, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jButton_Delete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton_Exchange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(jButton_Merge, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jButton_Quit.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton_Quit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/spana/images/Quit_32x32.gif"))); // NOI18N
        jButton_Quit.setMnemonic('Q');
        jButton_Quit.setText("<html><u>Q</u>uit</html>"); // NOI18N
        jButton_Quit.setToolTipText("Esc or Alt-Q"); // NOI18N
        jButton_Quit.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        jButton_Quit.setIconTextGap(8);
        jButton_Quit.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton_Quit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_QuitActionPerformed(evt);
            }
        });

        jButton_Save.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButton_Save.setIcon(new javax.swing.ImageIcon(getClass().getResource("/spana/images/Save_32x32.gif"))); // NOI18N
        jButton_Save.setMnemonic('S');
        jButton_Save.setText("Save & exit"); // NOI18N
        jButton_Save.setToolTipText("Alt-S or Alt-X"); // NOI18N
        jButton_Save.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        jButton_Save.setIconTextGap(8);
        jButton_Save.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton_Save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_SaveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton_Save, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton_Quit, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(jButton_Quit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton_Save))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        quitFrame();
    }//GEN-LAST:event_formWindowClosing

    private void jTextFieldDataFileKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldDataFileKeyTyped
        char c = Character.toUpperCase(evt.getKeyChar());
        if(evt.getKeyChar() != java.awt.event.KeyEvent.VK_ESCAPE &&
           !(evt.isAltDown() && ((c == 'X') ||
                   (c == 'D') || (c == 'E') ||
                   (c == 'M') || (c == 'Q') ||
                   (evt.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER))
                                 ) //isAltDown
                 ) { // if not ESC or Alt-something
                evt.consume(); // remove the typed key
                dataFile_Click();
        } // if char ok
    }//GEN-LAST:event_jTextFieldDataFileKeyTyped

    private void jTextFieldDataFileKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldDataFileKeyPressed
        if(!Util.isKeyPressedOK(evt)) {evt.consume();}
    }//GEN-LAST:event_jTextFieldDataFileKeyPressed

    private void jTextFieldDataFileMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextFieldDataFileMouseClicked
        dataFile_Click();
    }//GEN-LAST:event_jTextFieldDataFileMouseClicked

    private void jTextFieldDataFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldDataFileActionPerformed
        dataFile_Click();
    }//GEN-LAST:event_jTextFieldDataFileActionPerformed

    private void jTextFieldDataFileFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldDataFileFocusGained
        jTextFieldDataFile.selectAll();
    }//GEN-LAST:event_jTextFieldDataFileFocusGained

    private void jButton_DeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_DeleteActionPerformed
        delete_Click();
    }//GEN-LAST:event_jButton_DeleteActionPerformed

    private void jButton_ExchangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_ExchangeActionPerformed
        exchangeComponentWithComplex();
    }//GEN-LAST:event_jButton_ExchangeActionPerformed

    private void jButton_QuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_QuitActionPerformed
        quitFrame();
    }//GEN-LAST:event_jButton_QuitActionPerformed

    private void jButton_SaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_SaveActionPerformed
      if(!modified) {
        javax.swing.JOptionPane.showMessageDialog(this,
                "No changes made,\ndata-file does not need to be saved.",
                "Modify Chemical System", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        quitFrame();
        return;
      } //if not modified

      // get a file name
      String defName = null;
      if(!merged) {defName = dataFile.getPath();}
      String fileNameToSave = Util.getSaveFileName(this, pc.progName,
            "Enter a file name or select a file:", 5, defName, pc.pathDef.toString());
      if(fileNameToSave == null) {return;}
      // save the file
      setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
      java.io.File dataFileToSave = new java.io.File(fileNameToSave);

      //--- ready to write the new data file: check if the plot information
      //    needs to be changed
      if(dbg) {System.out.println(" ---- Saving modified system in data file: "+dataFileToSave.getName());}
      checkPlotInfo();

      try{WriteChemSyst.writeChemSyst(ch, dataFileToSave);}
      catch (WriteChemSyst.DataLimitsException ex) {
        System.err.println(ex.getMessage());
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        return;
      } catch (WriteChemSyst.WriteChemSystArgsException ex) {
        System.err.println(ex.getMessage());
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        return;
      }
      if(dbg) {System.out.println(" ---- data file written: \""+dataFileToSave.getName()+"\"");}
      // add file to list in main frame; do not mind about errors
      MainFrame.getInstance().addDatFile(fileNameToSave);
      modified = false;
      merged = false;
      setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
      quitFrame();
    }//GEN-LAST:event_jButton_SaveActionPerformed

    private void jListSolubCompsKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jListSolubCompsKeyTyped
        jListSolidComps.clearSelection();
        component_Click();
    }//GEN-LAST:event_jListSolubCompsKeyTyped

    private void jListSolubCompsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListSolubCompsFocusGained
        jScrollPane1.setBorder(highlightedBorder);
    }//GEN-LAST:event_jListSolubCompsFocusGained

    private void jListSolubCompsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListSolubCompsFocusLost
        jScrollPane1.setBorder(defBorder);
    }//GEN-LAST:event_jListSolubCompsFocusLost

    private void jListSolubCmplxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListSolubCmplxMouseClicked
        complex_Click();
        if(evt.getClickCount() >1) { // double-click
            checkLogKchange(jListSolubCmplx, jListSolubCmplx.getSelectedIndex());
        }
    }//GEN-LAST:event_jListSolubCmplxMouseClicked

    private void jListSolubCmplxKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jListSolubCmplxKeyTyped
        complex_Click();
        if(!(evt.isControlDown() || evt.isAltGraphDown() || evt.isAltDown() || evt.isMetaDown())
                && evt.getKeyChar() == ' ') {
          checkLogKchange(jListSolubCmplx, jListSolubCmplx.getSelectedIndex());
        }
        evt.consume(); // remove the typed key
    }//GEN-LAST:event_jListSolubCmplxKeyTyped

    private void jListSolubCmplxMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListSolubCmplxMouseMoved
        float cellBounds;
        try{cellBounds = (float)jListSolubCmplx.getCellBounds(0, 0).getHeight();}
        catch (Exception ex) {cellBounds =
                                    (float)jListSolubCmplx.getHeight() /
                                    (float)listSolubleCmplxModel.getSize();}
        if(listSolubleCmplxModel.getSize()>0) {
          int ix = (int)( (float)evt.getY()/ cellBounds );
          if(ix >= listSolubleCmplxModel.getSize()) {ix = -1;}
          jLabelReaction.setText(reactionText(cs, ix, true));
          jLabelReactionSetSize();
        }
    }//GEN-LAST:event_jListSolubCmplxMouseMoved

    private void jListSolubCmplxMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListSolubCmplxMouseExited
        jLabelReaction.setText(noReactionMessage);
        jLabelReactionSetSize();
    }//GEN-LAST:event_jListSolubCmplxMouseExited

    private void jListSolubCmplxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListSolubCmplxFocusGained
        jScrollPane2.setBorder(highlightedBorder);
    }//GEN-LAST:event_jListSolubCmplxFocusGained

    private void jListSolubCmplxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListSolubCmplxFocusLost
        jScrollPane2.setBorder(defBorder);
        if(jListSolubCmplx.getSelectedIndex() <0 &&
                jListSolidCmplx.getSelectedIndex() <0 &&
                cs.Na <=1) {
            jButton_Delete.setEnabled(false); //can not delete the last component
        }
    }//GEN-LAST:event_jListSolubCmplxFocusLost

    private void jListSolidCmplxKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jListSolidCmplxKeyTyped
        complex_Click();
        if(!(evt.isControlDown() || evt.isAltGraphDown() || evt.isAltDown() || evt.isMetaDown())
                && evt.getKeyChar() == ' ') {
          checkLogKchange(jListSolidCmplx,
                  jListSolidCmplx.getSelectedIndex() + listSolubleCmplxModel.getSize());
        }
        evt.consume(); // remove the typed key
    }//GEN-LAST:event_jListSolidCmplxKeyTyped

    private void jListSolidCmplxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListSolidCmplxMouseClicked
        complex_Click();
        if(evt.getClickCount() >1) { // double-click
            checkLogKchange(jListSolidCmplx,
                    jListSolidCmplx.getSelectedIndex() + listSolubleCmplxModel.getSize());
        }
    }//GEN-LAST:event_jListSolidCmplxMouseClicked

    private void jListSolidCmplxMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListSolidCmplxMouseMoved
        float cellBounds;
        try{cellBounds = (float)jListSolidCmplx.getCellBounds(0, 0).getHeight();}
        catch (Exception ex) {cellBounds =
                                    (float)jListSolidCmplx.getHeight() /
                                    (float)listSolidCmplxModel.getSize();}
        if(listSolidCmplxModel.getSize()>0) {
          int ix = (int)( (float)evt.getY()/ cellBounds );
          if(ix >= listSolidCmplxModel.getSize()) {ix = -1;}
          else {ix = (cs.nx+ix);}
          jLabelReaction.setText(reactionText(cs, ix, true));
          jLabelReactionSetSize();
        }
    }//GEN-LAST:event_jListSolidCmplxMouseMoved

    private void jListSolidCmplxMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListSolidCmplxMouseExited
        jLabelReaction.setText(noReactionMessage);
        jLabelReactionSetSize();
    }//GEN-LAST:event_jListSolidCmplxMouseExited

    private void jListSolidCmplxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListSolidCmplxFocusGained
        jScrollPane4.setBorder(highlightedBorder);
    }//GEN-LAST:event_jListSolidCmplxFocusGained

    private void jListSolidCmplxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListSolidCmplxFocusLost
        jScrollPane4.setBorder(defBorder);
        if(jListSolubCmplx.getSelectedIndex() <0 &&
                jListSolidCmplx.getSelectedIndex() <0 &&
                cs.Na <=1) {
            jButton_Delete.setEnabled(false); //can not delete the last component
        }
    }//GEN-LAST:event_jListSolidCmplxFocusLost

    private void jListSolidCompsKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jListSolidCompsKeyTyped
        jListSolubComps.clearSelection();
        component_Click();
    }//GEN-LAST:event_jListSolidCompsKeyTyped

    private void jListSolidCompsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListSolidCompsFocusLost
        jScrollPane3.setBorder(defBorder);
    }//GEN-LAST:event_jListSolidCompsFocusLost

    private void jListSolidCompsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListSolidCompsFocusGained
        jScrollPane3.setBorder(highlightedBorder);
    }//GEN-LAST:event_jListSolidCompsFocusGained

    private void jButton_MergeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_MergeActionPerformed
        mergeTwoSystems();
    }//GEN-LAST:event_jButton_MergeActionPerformed

    private void jListSolubCompsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jListSolubCompsKeyReleased
        jListSolidComps.clearSelection();
        component_Click();
    }//GEN-LAST:event_jListSolubCompsKeyReleased

    private void jListSolidCompsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jListSolidCompsKeyReleased
        jListSolubComps.clearSelection();
        component_Click();
    }//GEN-LAST:event_jListSolidCompsKeyReleased

    private void jButtonUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUpActionPerformed
        upDownComponent(true);
    }//GEN-LAST:event_jButtonUpActionPerformed

    private void jButtonDnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDnActionPerformed
        upDownComponent(false);
    }//GEN-LAST:event_jButtonDnActionPerformed

    private void jListSolubCompsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListSolubCompsMouseReleased
        jListSolidComps.clearSelection();
        component_Click();
    }//GEN-LAST:event_jListSolubCompsMouseReleased

    private void jListSolidCompsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListSolidCompsMouseReleased
        jListSolubComps.clearSelection();
        component_Click();
    }//GEN-LAST:event_jListSolidCompsMouseReleased

    private void jPanel1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseEntered
        jLabelReactionSetSize();
    }//GEN-LAST:event_jPanel1MouseEntered

    private void jPanel2upMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel2upMouseEntered
        jLabelReactionSetSize();
    }//GEN-LAST:event_jPanel2upMouseEntered

    private void jPanel2downMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel2downMouseEntered
        jLabelReactionSetSize();
    }//GEN-LAST:event_jPanel2downMouseEntered

    private void jScrollPane4MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jScrollPane4MouseEntered
        jLabelReactionSetSize();
    }//GEN-LAST:event_jScrollPane4MouseEntered

    private void jScrollPane2MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jScrollPane2MouseEntered
        jLabelReactionSetSize();
    }//GEN-LAST:event_jScrollPane2MouseEntered

    private void jPanel8MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel8MouseEntered
        jLabelReactionSetSize();
    }//GEN-LAST:event_jPanel8MouseEntered

    private void jPanel6MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel6MouseEntered
        jLabelReactionSetSize();
    }//GEN-LAST:event_jPanel6MouseEntered

    private void jListSolubCmplxMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListSolubCmplxMouseEntered
        jLabelReactionSetSize();
    }//GEN-LAST:event_jListSolubCmplxMouseEntered

    private void jListSolidCmplxMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListSolidCmplxMouseEntered
        jLabelReactionSetSize();
    }//GEN-LAST:event_jListSolidCmplxMouseEntered

    private void jListSolubCompsMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListSolubCompsMouseMoved
        jLabelReaction.setText(" ");
        jLabelReactionSetSize();
    }//GEN-LAST:event_jListSolubCompsMouseMoved

    private void jListSolidCompsMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListSolidCompsMouseMoved
        jLabelReaction.setText(" ");
        jLabelReactionSetSize();
    }//GEN-LAST:event_jListSolidCompsMouseMoved

    private void jPanel1MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseExited
        jLabelReaction.setText(" ");
        jLabelReactionSetSize();
    }//GEN-LAST:event_jPanel1MouseExited

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

  public final void quitFrame() {
    if(thisFrame != null && modified) {
        this.bringToFront();
        int n;
        //note that the first button is "cancel"
        Object[] options = {"Cancel", "Yes"};
            n = javax.swing.JOptionPane.showOptionDialog(this,"Discard your changes?",
                    "Modify Chemical System", javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.WARNING_MESSAGE, null, options, null);
        if(n != javax.swing.JOptionPane.NO_OPTION) {return;} //the second button is "yes"
    }
    modified = false;
    merged = false;
    finished = true;
    this.notify_All();
    this.setVisible(false);
    thisFrame = null;
    this.dispose();
  } // quitForm_Gen_Options
  private synchronized void notify_All() {notifyAll();}

  /** this method will wait for this dialog frame to be closed */
  public synchronized void waitForModifyChemSyst() {
    while(!finished) {
        try {wait();} catch (InterruptedException ex) {}
    } // while
  } // waitForModifyChemSyst()

  /** Are there changes that should be saved?
   * @return true if changes in the Chemical System have been made (and the
   * file is not yet saved); false if no changes to the Chemical System have
   * been made */
  public boolean isModified() {return modified;}

  public void bringToFront() {
    if(thisFrame != null) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                thisFrame.setVisible(true);
                if((thisFrame.getExtendedState() & javax.swing.JFrame.ICONIFIED) // minimised?
                            == javax.swing.JFrame.ICONIFIED) {
                    thisFrame.setExtendedState(javax.swing.JFrame.NORMAL);
                } // if minimized
                thisFrame.setAlwaysOnTop(true);
                thisFrame.toFront();
                thisFrame.requestFocus();
                thisFrame.setAlwaysOnTop(false);
            }
        });
    }
  } // bringToFront()

  private void jLabelReactionSetSize() {
    jLabelReaction.setSize(jPanel2.getWidth(), jPanel2.getHeight());
    int i = Math.max(0,(jPanel2.getWidth()-jLabelReaction.getWidth()));
    jLabelReaction.setLocation(i,0);
  }

//<editor-fold defaultstate="collapsed" desc="checkPlotInfo()">
/** If some components have been deleted: Find out if the plot definition can
 * be the same, if not, fix it. Also remove concentration
 * information for deleted components */
private void checkPlotInfo() {

  boolean plotMustBeChanged = false;
  boolean found;
  int compXnew = -1; int compYnew = -1; int compMainNew = -1;

  if(dbg) {
      System.out.println(" ---- Checking if plot information is still valid");
      diagr.printPlotType(null);
      if(dgrC != null) {System.out.println(dgrC.toString());}
      else {System.out.println("  undefined diagram concentrations: \"null\" pointer for dgrC (DiagrConcs).");}
  }
  //--- Is there any plot information?
  if(diagr.plotType <0 || diagr.compX <0 ||
     (diagr.plotType ==0 && (diagr.compY <0 || diagr.compMain <0)) ||
     ((diagr.plotType ==1 || diagr.plotType ==4) && diagr.compY <0)) {
  plotMustBeChanged = true;
  if(dbg) {System.out.println("Plot Must Be Changed: plotType="+diagr.plotType+" compX="+diagr.compX+" compY="+diagr.compY+" compMain="+diagr.compMain);}
  }
  //--- check if a component is given in an axis,
  //    but it is not found among the new components.
  //
  //    In case the components have been rearranged:
  //    find out the new component numbers is the axes.
  if(compXname0 != null && !plotMustBeChanged) {
    found = false;
    for(int inew =0; inew < cs.Na; inew++) {
      if(namn.identC[inew].equals(compXname0)) {
          compXnew = inew;
          found = true; break;
      }
    } //for inew
    if(!found) {
        plotMustBeChanged = true;
        if(dbg) {System.out.println("Plot Must Be Changed: component \""+compXname0+"\" in X-axis has been removed");}
    }
  } //if compXname0 != null

  if((diagr.plotType ==0 || diagr.plotType ==1 || diagr.plotType ==4) &&
     compYname0 != null && !plotMustBeChanged) {
    found = false;
    for(int inew =0; inew < cs.Na; inew++) {
      if(namn.identC[inew].equals(compYname0)) {
          compYnew = inew;
          found = true; break;
      }
    } //for inew
    if(!found) {
        plotMustBeChanged = true;
        if(dbg) {System.out.println("Plot Must Be Changed: component \""+compYname0+"\" in Y-axis has been removed");}
    }
  } //if compYname0 != null

  if(diagr.plotType ==0 && compMainName0 != null && !plotMustBeChanged) {
    found = false;
    for(int inew =0; inew < cs.Na; inew++) {
      if(namn.identC[inew].equals(compMainName0)) {
          compMainNew = inew;
          found = true; break;
      }
    } //for inew
    if(!found) {
        plotMustBeChanged = true;
        if(dbg) {System.out.println("Plot Must Be Changed: main-component \""+compMainName0+"\" has been removed");}
    }
  } //if compYname0 != null

  //--- if an old-component with concentration varied is not found in
  //    the new component-list: the plot definition must change.
  if(!plotMustBeChanged) {
    if(dgrC == null) {plotMustBeChanged = true;}
    else {
    for(int iold = 0; iold < Na0; iold++) {
      // is the concentration varied?
      if(dgrC.hur[iold] ==2 || dgrC.hur[iold] ==3 || dgrC.hur[iold] ==5) {
        found = false;
        for(int inew =0; inew < cs.Na; inew++) {
          if(namn.identC[inew].equals(identC0.get(iold))) {found = true; break;}
        } //for inew
        if(!found) {
            plotMustBeChanged = true;
            if(dbg) {System.out.println("Plot Must Be Changed: component \""+identC0.get(iold)+"\" (with varied concentration) has been removed");}
            break;
        }
      } //concentration varied?
    } //for iold
    } // if dgrC != null
  } //if !plotMustBeChanged

  //--- are there H+ and e- in the new chemical system?
  int H_present = -1; int e_present = -1; int e_cmplx = -1;
  for(int inew =0; inew < cs.Na; inew++) {
    if(Util.isProton(namn.identC[inew])) {H_present = inew;}
    if(Util.isElectron(namn.identC[inew])) {e_present = inew;}
  } //for inew
  if(e_present < 0) {
    for(int ix =0; ix < cs.nx; ix++) {
      if(Util.isElectron(namn.ident[ix+cs.Na])) {e_cmplx = ix; break;}
    } //for ix
  } //if !e_present

  //--- check if pH is given for the Y-axis or it is an H-affinity diagram
  //    and there is no H+
  if((diagr.plotType == 6 || diagr.plotType == 8) && !plotMustBeChanged) {
    if(H_present < 0) {
        plotMustBeChanged = true;
        if(dbg) {System.out.println("Plot Must Be Changed: calculated pH or H-affinity requested but \"H+\" has been removed");}
    }
  } //calculated "pH" in y-axis or H-affinity
  //--- check if pe or Eh is given for the Y-axis and there are no electrons
  if(diagr.plotType == 5 && !plotMustBeChanged) {
    if(e_present < 0 && e_cmplx < 0) {
        plotMustBeChanged = true;
        if(dbg) {System.out.println("Plot Must Be Changed: calculated pe/Eh requested but \"e-\" has been removed");}
    }
  } //calculated "pe" in y-axis

  //--- get a new plot type if needed
  if(plotMustBeChanged) {
    diagr.Eh = pd.useEh;
    //set a default plot
    if(dbg) {System.out.println("Setting New Plot Type");}
    DefaultPlotAndConcs.setDefaultPlot(cs, diagr, dgrC);
  } //if plotMustBeChanged
  else {
    //In case the components have been rearranged:
    // put in the new component numbers is the axes.
    diagr.compX = compXnew;
    diagr.compY = compYnew;
    diagr.compMain = compMainNew;
  }
  if(dbg) {diagr.printPlotType(null);}

  //--- create a new instance of DiagrConcs
  Chem.DiagrConcs dgrCnew;
  try{dgrCnew = ch.new DiagrConcs(cs.Na);}
  catch (Chem.ChemicalParameterException ex) {
        MsgExceptn.showErrMsg(this, ex.getMessage(), 1);
        System.err.println(Util.stack2string(ex));
        storeInitialSystem(ch);
        return;
  }

  //--- Get concentrations (default if needed) for all new components
  if(dbg) {System.out.println("Checking concentrations:");}
  for(int inew =0; inew < cs.Na; inew++) {
    found = false;
    if(dgrC != null) {
      for(int iold = 0; iold < Na0; iold++) {
        if(namn.identC[inew].equals(identC0.get(iold)) && dgrC.hur[iold] >0) {
          dgrCnew.hur[inew] = dgrC.hur[iold];
          dgrCnew.cLow[inew] = dgrC.cLow[iold];
          dgrCnew.cHigh[inew] = dgrC.cHigh[iold];
          found = true;
          break;
        }
      } //for iold
    }
    if(!found) { //a component is new (due to an exchange)
      if(dbg) {
            System.out.println("concentration for component \""+namn.identC[inew]+"\" not found; setting default conc.");
      }
      DefaultPlotAndConcs.setDefaultConc(inew, namn.identC[inew], dgrCnew, pd.kth);
    } //if !found
  } //for inew

  //--- change the pointer to the new instance (the old instance becomes garbage)
  ch.diagrConcs = dgrCnew;
  dgrC = ch.diagrConcs;

  //--- check that concentrations in axes (and "main") are varied (or fixed)
  if(dbg) {System.out.println("Checking concentrations for components in axes...");}
  DefaultPlotAndConcs.checkConcsInAxesAndMain(namn, diagr, dgrC, dbg, pd.kth);
  if(dbg) {
    System.out.println("Component's concentrations:");
    for(int ic =0; ic < cs.Na; ic++) {
      System.out.println("  \""+namn.identC[ic]+"\" hur="+dgrC.hur[ic]+" cLow="+dgrC.cLow[ic]+" cHigh="+dgrC.cHigh[ic]);
    } //for ic
    System.out.println(" ---- Finished checking concentrations and plot-information.");
  }

  storeInitialSystem(ch);
} //checkPlotInfo()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="complex_Click()">
private void complex_Click() {
  int nbr = 0;
  if(listSolubleCmplxModel.getSize()>0) {
    if(jListSolubCmplx.getSelectedIndex() >=0) {
      if(!jButton_Delete.isEnabled()) {jButton_Delete.setEnabled(true);}
      int[] indx = jListSolubCmplx.getSelectedIndices();
      if(indx.length == 1) {nbr = 1;}
      else if(indx.length > 1) {nbr = 2;}
    }
  }
  if(listSolidCmplxModel.getSize()>0) {
    if(jListSolidCmplx.getSelectedIndex() >=0) {
      if(!jButton_Delete.isEnabled()) {jButton_Delete.setEnabled(true);}
      int[] indx = jListSolidCmplx.getSelectedIndices();
      if(indx.length == 1) {nbr++;}
      else if(indx.length > 1) {nbr = nbr + 2;}
    }
  }
  if(nbr <= 1) {
        jButton_Delete.setText("<html><u>D</u>elete a reaction</html>");
  } else {
        jButton_Delete.setText("<html><u>D</u>elete reactions</html>");
  }
} //complex_Click()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="component_Click()">
private void component_Click() {
  int nbr = 0;
  jButtonUp.setEnabled(false);
  jButtonDn.setEnabled(false);
  if(listSolubleCompModel.getSize()>0 &&
     jListSolubComps.getSelectedIndex()>=0) {
        nbr++;
        if(listSolubleCompModel.getSize() >=2) {
           if(jListSolubComps.getSelectedIndex()>=1) {jButtonUp.setEnabled(true);}
           if(jListSolubComps.getSelectedIndex()< listSolubleCompModel.getSize()-1) {
                jButtonDn.setEnabled(true);
           }
        }
  }
  if(listSolidCompModel.getSize()>0 &&
          jListSolidComps.getSelectedIndex()>=0) {
      nbr++;
        if(listSolidCompModel.getSize() >=2) {
           if(jListSolidComps.getSelectedIndex()>=1) {jButtonUp.setEnabled(true);}
           if(jListSolidComps.getSelectedIndex()< listSolidCompModel.getSize()-1) {
                jButtonDn.setEnabled(true);
           }
        }
  }
  if(nbr < 1) {
        jButton_Delete.setText("Delete");
  } else {
        jButton_Delete.setText("Delete a component");
  }
} //component_Click()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="dataFile_Click()">
  private void dataFile_Click() {
    // get a file name
    String fileName = Util.getOpenFileName(this, pc.progName, true,
            "Enter a Data file name:", 5, dataFile.getPath(), pc.pathDef.toString());
    if(fileName == null) {jTextFieldDataFile.requestFocusInWindow(); return;}
    if(MainFrame.getInstance().addDatFile(fileName)) {
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        dataFile = new java.io.File(fileName);
        if(!readDataFile(dataFile, pc.dbg)) {
            System.err.println(" ---- Error reading file \""+dataFile.getName()+"\"");
            setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            return;}
        if(dbg) {System.out.println(" ---- new data file = \""+dataFile.getName()+"\"");}
        ch = chRead;
        chRead = null;
        cs = ch.chemSystem;
        namn = cs.namn;
        diagr = ch.diag;
        dgrC = ch.diagrConcs;
    }
    storeInitialSystem(ch);
    setupFrame();
    this.toFront();
    this.requestFocus();
    jTextFieldDataFile.requestFocusInWindow();
    setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
  } // dataFile_Click()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="disableButtons()">
  private void disableButtons() {
    jTextFieldDataFile.setEnabled(false);
    jButton_Delete.setEnabled(false);
    jButton_Delete.setForeground(java.awt.Color.gray);
    jButton_Exchange.setEnabled(false);
    jButton_Exchange.setForeground(java.awt.Color.gray);
    jButton_Merge.setEnabled(false);
    jButton_Merge.setForeground(java.awt.Color.gray);
    jButton_Save.setText("Save & exit");
    jButton_Save.setEnabled(false);
    jButton_Save.setForeground(java.awt.Color.gray);
    jButtonUp.setEnabled(false);
    jButtonDn.setEnabled(false);
  } //disableButtons()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="delete_Click()">
  private void delete_Click() {
    disableButtons();
    // ---- component or complex?
    int del_Comp_or_Cmplx = -1;
    if(jButton_Delete.getText().contains("component")) {
      if(listSolubleCompModel.getSize()>0 &&
          jListSolubComps.getSelectedIndex()>=0) {
        del_Comp_or_Cmplx = 1;
      }
      if(listSolidCompModel.getSize()>0 &&
          jListSolidComps.getSelectedIndex()>=0) {
        del_Comp_or_Cmplx = 1;
      }
    }
    else if(jButton_Delete.getText().contains("reaction")){
      if(listSolubleCmplxModel.getSize()>0) {
        if(jListSolubCmplx.getSelectedIndex() >=0) {
            del_Comp_or_Cmplx = 2;
        }
      }
      if(listSolidCmplxModel.getSize()>0) {
        if(jListSolidCmplx.getSelectedIndex() >=0) {
            del_Comp_or_Cmplx = 2;
        }
      }
    }

    // ----
    switch (del_Comp_or_Cmplx) {
    case 1:
      // ---- delete a component
      // ---- is it water (H2O)?
      if(listSolubleCompModel.getSize()>0 &&
         jListSolubComps.getSelectedIndex()>=0 &&
         Util.isWater(jListSolubComps.getSelectedValue().toString())) {
            compToDelete = jListSolubComps.getSelectedIndex();
            final String water = jListSolubComps.getSelectedValue().toString();
            int n;
            // note that the buttons are  [Cancel] [Yes] [No]
            final int YES_OPTION = javax.swing.JOptionPane.NO_OPTION; //second button
            final int NO_OPTION = javax.swing.JOptionPane.CANCEL_OPTION; //third button
            // are there any complexes?
            if(listSolubleCmplxModel.getSize() > 0 ||
               listSolidCmplxModel.getSize() > 0) {
                // is there OH- among the complexes?
                String oh = "";
                for(int i=0; i < cs.Ms-cs.mSol; i++) {
                  if(namn.ident[i].equals("OH-") || namn.ident[i].equals("OH -")) {
                      oh = "\"OH-\" and ";
                  }
                } //for i
                // note that the buttons are  [Cancel] [Yes] [No]
                Object[] options = {"Cancel", "Yes", "No"};
                n = javax.swing.JOptionPane.showOptionDialog(this,
                        "Remove water without removing its reactions?\n\n"+
                        "Press \"YES\" to remove "+water+" but keep its reactions\n"+
                        "(for example to keep "+oh+"any hydrolysis species).\n\n"+
                        "Press \"NO\" to delete "+water+" and all its reactions.\n\n",
                        "Modify Chemical System", javax.swing.JOptionPane.YES_NO_CANCEL_OPTION,
                        javax.swing.JOptionPane.QUESTION_MESSAGE, null, options, null);
            } else {n = NO_OPTION;}
            if(n == YES_OPTION) {
              //do not remove reactions of H2O
              if(dbg) {System.out.println(" ---- Remove water but leave its reactions");}
              for(int ic = compToDelete; ic < cs.Na-1; ic++) {
                namn.identC[ic] = namn.identC[ic+1];
                namn.ident[ic] = namn.ident[ic+1];
              }//for ic
              for(int i=cs.Na-1; i < cs.Ms-1; i++) {
                namn.ident[i] = namn.ident[i+1];
              } //for i
              for(int ix =0; ix < cs.Ms-cs.Na; ix++) {
                for(int ic = compToDelete; ic < cs.Na-1; ic++) {
                    cs.a[ix][ic] = cs.a[ix][ic+1];
                }//for ic
              }//for ix
              cs.Na = cs.Na -1;
              cs.Ms = cs.Ms -1;
              cs.jWater = -1;
              //--- fix plot information data (if posssible)
              checkPlotInfo();
              modified = true;
              setupFrame();
            } else if(n == NO_OPTION) {
              //remove also all reactions with H2O
              if(dbg) {System.out.println(" ---- Remove water and all its reactions");}
              this.setVisible(false);
              final ModifyChemSyst thisModify = this;
              Thread mod = new Thread() {@Override public void run(){
                  spana.ModifyConfirm modifyC =
                          new spana.ModifyConfirm(thisModify.getLocation(),
                                                    ch, compToDelete, -99, pc);
                  boolean cancel = modifyC.waitForModifyConfirm();
                  thisModify.cs = ch.chemSystem;
                  thisModify.namn = cs.namn;
                  if(!cancel) {
                      //--- fix plot information data (if posssible)
                      checkPlotInfo();
                      modified = true;
                      cs.jWater = -1;
                  }
                  javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
                      setupFrame();
                      thisModify.setVisible(true);
                  }}); //invokeLater(Runnable)
              }};//new Thread
              mod.start();
            } else {setupFrame(); break;} //if cancel_option
      } else {
            // ---- not water (H2O):
            if(jListSolubComps.getSelectedIndex()>=0) {
                compToDelete = jListSolubComps.getSelectedIndex();
            } else {
                compToDelete = jListSolidComps.getSelectedIndex() + (cs.Na - cs.solidC);
            }
            if(dbg) {System.out.println(" ---- Remove component \""+namn.identC[compToDelete]+"\"");}
            this.setVisible(false);
            final ModifyChemSyst thisModify = this;
            Thread mod = new Thread() {@Override public void run(){
                spana.ModifyConfirm modifyC =
                        new spana.ModifyConfirm(thisModify.getLocation(),
                                                    ch, compToDelete, -99, pc);
                boolean cancel = modifyC.waitForModifyConfirm();
                thisModify.cs = ch.chemSystem;
                thisModify.namn = cs.namn;
                if(!cancel) {
                    //--- fix plot information data (if posssible)
                    checkPlotInfo();
                    modified = true;
                }
                javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
                    setupFrame();
                    thisModify.setVisible(true);
                }}); //invokeLater(Runnable)
            }};//new Thread
            mod.start();
      } //water?
      break;

    case 2:
      // ---- delete one or more reactions:
      //show a warning:
      String onlyOne = "";
      if(jListSolubCmplx.getSelectedIndex() >=0 &&
         jListSolubCmplx.getMaxSelectionIndex() == jListSolubCmplx.getMinSelectionIndex()) {
          onlyOne = jListSolubCmplx.getSelectedValue().toString();
      }
      if(jListSolidCmplx.getSelectedIndex() >=0) {
        if(jListSolidCmplx.getMaxSelectionIndex() == jListSolidCmplx.getMinSelectionIndex()) {
          if(onlyOne.length() ==0) {onlyOne = jListSolidCmplx.getSelectedValue().toString();}
           else {onlyOne = "";}
        } else {onlyOne = "";}
      }
      if(onlyOne.length() ==0) {onlyOne = "selected reactions";}
      else {onlyOne = "\""+onlyOne+"\"";}
      //note that the first button is "cancel"
      Object[] options = {"Cancel", "Yes"};
      int n = javax.swing.JOptionPane.showOptionDialog(this,
            "Remove "+onlyOne+" ?",
            "Modify Chemical System", javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.WARNING_MESSAGE, null, options, null);
      if (n != javax.swing.JOptionPane.NO_OPTION) {setupFrame(); break;} //second button is "yes"
      n = javax.swing.JOptionPane.showOptionDialog(this,
            "Warning!\nRemoving reactions will probably result in wrong diagrams !\n\n"+
            "Remove "+onlyOne+" anyway?",
            "Modify Chemical System", javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.WARNING_MESSAGE, null, options, null);
      if (n != javax.swing.JOptionPane.NO_OPTION) {setupFrame(); break;} //second button is "yes"
      // ---- go ahead and do it!
      int i1; int ix;
      int ixCount = 0;
      for(int i=0; i < listSolubleCmplxModel.getSize(); i++) {
          ix = i;
          if(jListSolubCmplx.isSelectedIndex(i)) {ixCount++;}
          else{ //not selected: keep
            if(ixCount >0) {
              i1 = ix -ixCount;
              namn.ident[cs.Na+i1] = namn.ident[cs.Na+ix];
              cs.lBeta[i1] = cs.lBeta[ix];
              //for(int ic=0; ic<cs.Na; ic++) {cs.a[i1][ic] = cs.a[ix][ic];}
              System.arraycopy(cs.a[ix], 0, cs.a[i1], 0, cs.Na);
            }//if ixCount >0
          }//if not isSelectedIndex(i)
      }//for i
      cs.Ms = cs.Ms - ixCount;
      cs.nx = cs.nx - ixCount;

      int ifCount = 0;
      int iCount;
      for(int i=0; i < listSolidCmplxModel.getSize(); i++) {
          ix = cs.nx + ixCount + i;
          if(jListSolidCmplx.isSelectedIndex(i)) {ifCount++;}
          else{ //not selected: keep
            iCount = ixCount + ifCount;
            if(iCount >0) {
              i1 = ix -iCount;
              namn.ident[cs.Na+i1] = namn.ident[cs.Na+ix];
              cs.lBeta[i1] = cs.lBeta[ix];
              //for(int ic=0; ic<cs.Na; ic++) {cs.a[i1][ic] = cs.a[ix][ic];}
              System.arraycopy(cs.a[ix], 0, cs.a[i1], 0, cs.Na);
            }//if iCount >0
          }//if not isSelectedIndex(i)
      }//for i
      cs.Ms = cs.Ms - ifCount;
      cs.mSol = cs.mSol - ifCount;

      modified = true;
      setupFrame();
      break;

    default:
      javax.swing.JOptionPane.showMessageDialog(this,
            "Please, select either:\n    - a component, or\n    - one or more reaction products\nfrom the lists.",
            "Modify Chemical System",javax.swing.JOptionPane.WARNING_MESSAGE);
      setupFrame();
      break;
    } //switch

  } //delete_Click()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="exchangeComponentWithComplex()">
  private void exchangeComponentWithComplex() {
    if(listSolubleCmplxModel.getSize() <=0 && listSolidCmplxModel.getSize() <=0) {return;}
    disableButtons();
    // which component is selected?
    compToDelete = -1;
    compToDelete = jListSolubComps.getSelectedIndex();
    if(compToDelete < 0) {
        compToDelete = jListSolidComps.getSelectedIndex();
        if(compToDelete>=0) {compToDelete = compToDelete +(cs.Na - cs.solidC);}
    }
    // check that a component is indeed selected
    if(compToDelete < 0) {
        String msg = "list";
        if(listSolubleCompModel.getSize() > 0 && listSolidCompModel.getSize() > 0) {msg = "lists";}
        javax.swing.JOptionPane.showMessageDialog(this, "Please, select one component from the "+msg+".", "Modify Chemical System", javax.swing.JOptionPane.WARNING_MESSAGE);
        setupFrame();
        return;
    }
    // does this component have any soluble complex?
    int complxs = 0 ;
    for(int i=0; i < cs.nx; i++) {
      if(Math.abs(cs.a[i][compToDelete])>1e-7) {complxs++; break;}
    } //for i
    if(complxs <= 0) {
      int i;
      for(int is=0; is < cs.mSol-cs.solidC; is++) {
          i = is +cs.nx;
          if(Math.abs(cs.a[i][compToDelete])>1e-7) {complxs++; break;}
      } //for i
    }
    if(complxs <=0) {
        String comp = namn.identC[compToDelete];
        javax.swing.JOptionPane.showMessageDialog(this,
                  "\""+comp+"\" does not form any reaction products.\n\n"+
                  "Can not exchange this component.",
                  "Modify Chemical System", javax.swing.JOptionPane.WARNING_MESSAGE);
        setupFrame();
        return;
    }

    // check if only one reaction is selected:
    complxToExchange = -1;
    int[] cmplxSel = jListSolubCmplx.getSelectedIndices();
    if(cmplxSel.length == 1) {complxToExchange = jListSolubCmplx.getSelectedIndex();}
    if(complxToExchange < 0) {
      int[] solidSel = jListSolidCmplx.getSelectedIndices();
      if(solidSel.length == 1) {
          complxToExchange = jListSolidCmplx.getSelectedIndex()
                  + listSolubleCmplxModel.getSize();
      }
    }
    if(complxToExchange < 0) {complxToExchange = -1;} //this should not be needed
    if(dbg) {System.out.println(" ---- Exchange \""+namn.identC[compToDelete]+"\" with reaction="+complxToExchange);}

    this.setVisible(false);
    final ModifyChemSyst thisModify = this;
    Thread exch = new Thread() {@Override public void run(){
        spana.ModifyConfirm modifyC =
                new spana.ModifyConfirm(thisModify.getLocation(),
                                            ch, compToDelete, complxToExchange, pc);
        boolean cancel = modifyC.waitForModifyConfirm();
        thisModify.cs = ch.chemSystem;
        thisModify.namn = cs.namn;
        if(!cancel) {
            //--- fix plot information data (if posssible)
            checkPlotInfo();
            modified = true;
        }
        javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
            setupFrame();
            thisModify.setVisible(true);
        }}); //invokeLater(Runnable)
    }};//new Thread
    exch.start();

  } //exchangeComponentWithComplex()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="checkLogKchange()">
  private void checkLogKchange(final javax.swing.JList list, final int cmplx) {
    if(cmplx<0) {return;}
    disableButtons();
    this.setVisible(false);
    final ModifyChemSyst thisModify = this;
    final int i = list.getSelectedIndex();
    Thread lgK = new Thread() {@Override public void run(){
        spana.LogKchange logKc = new spana.LogKchange(cs, cmplx, pc);
        boolean cancel = logKc.waitForLogKchange();
        if(!cancel) {modified = true;}
        javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
          if(modified) {
            java.awt.CardLayout cl =
                              (java.awt.CardLayout)jPanel1Top.getLayout();
            cl.show(jPanel1Top, "cardNewSystem");
            jTextFieldDataFile.setEnabled(false);
          }
          setupFrame();
          thisModify.setVisible(true);
          list.setSelectedIndex(i);
          list.requestFocusInWindow();
        }}); //invokeLater(Runnable)
    }};//new Thread
    lgK.start();
  } //checkLogKchange()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="mergeTwoSystems()">
  private void mergeTwoSystems() {
    if(modified) {
        javax.swing.JOptionPane.showMessageDialog(this,
                "Please save first this modified chemical system.\n"+
                "Only existing (un-modified) data files can be merged.",
                "Modify Chemical System", javax.swing.JOptionPane.WARNING_MESSAGE);
        return;}

    //--- warning  (note that the first button is "cancel")
    Object[] options = {"Cancel", "Yes"};
    int answ = javax.swing.JOptionPane.showOptionDialog(this,
      "<html><b>Note: a merged chemical system<br>"+
      "is probably incomplete.</b><br>"+
      "For example, after merging the two systems:<br>"+
      "&nbsp;&nbsp;&nbsp;H+, Ca+2, Cl&#x2212;,<br>"+
      "&nbsp;&nbsp;&nbsp;H+, Fe+3, CO3&#x2212;2,<br>"+ //&#8722; unicode minus
      "you will <b>NOT</b> have chloride complexes of Fe(III)<br>"+
      "and you will <b>NOT</b> have calcium carbonate!<br><br>"+
      "Are you <b>sure</b> that you still want to do this?</html>",
      "Modify Chemical System",javax.swing.JOptionPane.YES_NO_OPTION,
      javax.swing.JOptionPane.WARNING_MESSAGE, null, options, null);
    if(answ != javax.swing.JOptionPane.NO_OPTION) {return;} // second button is "yes"
    //go ahead and do it
    disableButtons();

    //--- get the second file name
    String fileName2 = Util.getOpenFileName(this, pc.progName, true,
            "Select data file to merge with this chemical system:", 5, null, pc.pathDef.toString());
    if(fileName2 == null) {setupFrame(); return;}
    java.io.File dataFile2 = new java.io.File(fileName2);
    if(dataFile2.getPath().equalsIgnoreCase(dataFile.getPath())) {
        javax.swing.JOptionPane.showMessageDialog(this,
                "File \""+dataFile2.getPath()+"\"\n"+
                "is already open!",
                "Modify Chemical System", javax.swing.JOptionPane.ERROR_MESSAGE);
        setupFrame();
        return;}

    //--- read the second data file
    setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
    if(dbg) {
        System.out.println(
            " ---- Merging data file \""+dataFile.getName()+"\"  with  \""+dataFile2.getName()+"\"");
    }
    boolean ok = readDataFile(dataFile2, pc.dbg);  //read the file
    if(!ok) {
        System.err.println(" ---- Error reading file \""+dataFile2.getName()+"\"");
        setupFrame();
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        this.bringToFront();
        return;
    }
    if(dbg) {System.out.println("---- data file read: \""+dataFile2.getName()+"\"");}
    ch2 = chRead;
    chRead = null;
    Chem.ChemSystem cs2 = ch2.chemSystem;
    Chem.ChemSystem.NamesEtc namn2 = cs2.namn;
    Chem.Diagr diag2 = ch2.diag;
    Chem.DiagrConcs dgrC2 = ch2.diagrConcs;
    if(dbg) {
        System.out.print("---- First file has "+cs.Na+" components");
        if(cs.solidC >0) {String t; if(cs.solidC ==1) {t=" is";} else {t=" are";}
            System.out.print(" (the last "+cs.solidC+t+" solid)");
        }
        System.out.print(":"+nl+"    ");
        System.out.print(namn.identC[0]);
        if(cs.Na >1) {for(int i=1;i<cs.Na;i++) {System.out.print(", "+namn.identC[i]);}}
        System.out.println();
        System.out.print("---- Second file has "+cs2.Na+" components");
        if(cs2.solidC >0) {String t; if(cs2.solidC ==1) {t=" is";} else {t=" are";}
            System.out.print(" (the last "+cs2.solidC+t+" solid)");
        }
        System.out.print(":"+nl+"    ");
        System.out.print(namn2.identC[0]);
        if(cs2.Na >1) {for(int i=1;i<cs2.Na;i++) {System.out.print(", "+namn2.identC[i]);}}
        System.out.println(nl+"----");
    }

    int mSol = cs.mSol;
    int mSol2 = cs2.mSol;
    int solidC_Out = cs.solidC;

    //--- what components are new? ---
    int naOut = cs.Na;
    // iel[] will contain what components will be in the merged system
    // and in what order
    int found;
    for(int ic2 = 0; ic2 < cs2.Na; ic2++) {
      found = -1;
      for(int ic1 = 0; ic1 < cs.Na; ic1++) {
        if(Util.nameCompare(namn.identC[ic1], namn2.identC[ic2])) {
          found = ic1; break;
        }
      } //for ic1
      namn2.iel[ic2] = found; //it will be -1 if not found
      if(found < 0) { //not found: a new component
        naOut++;
        if(dbg) {System.out.print("Component to add: \""+namn2.identC[ic2]+"\"");}
        if(ic2 > (cs2.Na - cs2.solidC -1)) {
            solidC_Out++;
            if(dbg) {System.out.print(" (this is a solid component)");}
        }
        if(dbg) {System.out.println();}
        //no complex in File1 may have the same name as this component in File2
        for(int ix1=cs.Na; ix1 < (cs.Ms-cs.solidC); ix1++) {
          if(Util.nameCompare(namn.ident[ix1],namn2.identC[ic2])) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Big trouble!\n\n"+
                "component: \""+namn2.identC[ic2]+"\"\n"+
                "in file \""+dataFile2.getName()+"\"\n"+
                "has the same name as reaction product: \""+namn.ident[ix1]+"\"\n"+
                "in file \""+dataFile.getName()+"\"\n\n"+
                "Please switch a component/reaction.\n"+
                "Terminating . . .",
                "Modify Chemical System", javax.swing.JOptionPane.ERROR_MESSAGE);
            if(dbg) {System.out.println("Component in file \""+dataFile2.getName()+"\""+nl+
                    "   has the same name as reaction in file \""+dataFile.getName()+"\"."+nl+
                    "   Can not continue merge.");}
            setupFrame();
            setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            return;
          }
        } //for ix1
      } //if found <0
    } //for ic2
    //--- number of new components: soluble (solblCompsNew) and new solid (solidCompsNew)
    int solidCompsNew = solidC_Out - cs.solidC;
    int solblCompsNew = (naOut - cs.Na) - solidCompsNew;
    if(dbg) {
        System.out.println("New components= "+(naOut-cs.Na)+",  total in the merged system= "+naOut+nl+
                "   new components: soluble= "+solblCompsNew+"  solid= "+solidCompsNew);
    }
    //--- The merged system will be larger than any of the two individual files,
    //    so the storage capacity of the arrays in the chem.Chem classes
    //    is not enough. The new species are first saved in ArrayLists and
    //    when the size of the merged system is known in detail at the end,
    //    then a new chem.Chem instance is created
    java.util.ArrayList<String> identCM = new java.util.ArrayList<String>();
    for(int i=0; i < naOut; i++) {identCM.add(i, null);}
    java.util.ArrayList<String> identM = new java.util.ArrayList<String>();
    java.util.ArrayList<Double> lBetaM = new java.util.ArrayList<Double>();
    java.util.ArrayList<Double[]> aM = new java.util.ArrayList<Double[]>();
    double[] a1 = new double[naOut];
    double[] a2 = new double[naOut];

    //--- Copy/move names of solid components in File1:
    //    Note that solid componensts must be the last in the list
    for(int ic1=0; ic1 < cs.Na; ic1++) {
      if(ic1 > (cs.Na - cs.solidC - 1)) { //solid component
        namn.iel[ic1] = ic1 + solblCompsNew;
        identCM.set(ic1+solblCompsNew, namn.identC[ic1]);
      } else {
        namn.iel[ic1] = ic1;
        identCM.set(ic1, namn.identC[ic1]);
      }
    }
    //--- Insert new component names
    //    Note that solid componensts must be the last in the list
    int iaCount=0; int iaNew;
    for(int ic2=0; ic2 < cs2.Na; ic2++) {
      if(namn2.iel[ic2] < 0) {//component to add (not found among the existing)
        iaCount++;
        if(iaCount <= solblCompsNew) {
          iaNew = iaCount + (cs.Na - cs.solidC - 1);
        } else {
          iaNew = iaCount + cs.Na -1;
        }
        identCM.set(iaNew, namn2.identC[ic2]);
        namn2.iel[ic2] = iaNew;
      }
    }
    if(dbg) { // list the names of all components in the merged system
      int n0, nM, iPl, nP;
      System.out.println("---- Components in the merged system: "+naOut); System.out.print("    ");
          n0 = 0;        //start index to print
          nM = naOut-1;  //end index to print
          iPl = 5; nP= nM-n0; if(nP >=0) { //items_Per_Line and items to print
        print_1: for(int ijj=0; ijj<=nP/iPl; ijj++) { for(int jjj=0; jjj<iPl; jjj++) { int kjj = n0+(ijj*iPl+jjj);
          System.out.format(" %15s",identCM.get(kjj));
          if(kjj >(nM-1)) {System.out.println(); break print_1;}} //for j
          System.out.println(); System.out.print("    ");} //for ijj
      }
    } //if dbg

    //--- find new Soluble complexes in 2nd file ---
    int nxOut = cs.nx;
    for(int ix2=0; ix2 < cs2.nx; ix2++) {
      found = -1;
      for(int ix1=0; ix1 < cs.nx; ix1++) {
        if(Util.nameCompare(namn.ident[ix1+cs.Na],namn2.ident[ix2+cs2.Na])) {
          found = ix1; break;
        }
      } //for ix1

      if(found < 0) { //new complex
        if(dbg) {System.out.println("New soluble complex: "+namn2.ident[ix2+cs2.Na]);}
        //check that no component in File1 has the same name as this complex
        for(int ic1=0; ic1 < cs.Na; ic1++) {
          if(Util.nameCompare(namn.identC[ic1],namn2.ident[ix2+cs2.Na])) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Big trouble!\n\n"+
                "complex: \""+namn2.ident[ix2+cs2.Na]+"\"\n"+
                "in file \""+dataFile2.getName()+"\"\n"+
                "has the same name as component: \""+namn.identC[ic1]+"\"\n"+
                "in file \""+dataFile.getName()+"\"\n\n"+
                "Please switch a component/complex.\n"+
                "Terminating . . .",
                "Modify Chemical System", javax.swing.JOptionPane.ERROR_MESSAGE);
            if(dbg) {System.out.println("Complex in file \""+dataFile2.getName()+"\""+nl+
                    "   has the same name as component in file \""+dataFile.getName()+"\"."+nl+
                    "   Can not continue merging.");}
            setupFrame();
            setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            return;
          }
        } //for ic1
        nxOut++;
        identM.add(namn2.ident[ix2+cs2.Na]);
        lBetaM.add(cs2.lBeta[ix2]);
        aM.add(new Double[cs2.Na]);
        for(int i=0; i < cs2.Na; i++) {aM.get(aM.size()-1)[i] = cs2.a[ix2][i];}
      } //if found <0

      else { //found >=0: there are two complexes with the same name
        //check that not only the names match, but also the stoichiometries match
        for(int ic=0; ic < naOut;  ic++) {a1[ic] = 0; a2[ic] = 0;}
        for(int ic=0; ic < cs.Na;  ic++) {a1[namn.iel[ic]] = cs.a[found][ic];}
        for(int ic=0; ic < cs2.Na; ic++) {a2[namn2.iel[ic]]= cs2.a[ix2][ic];}
        for(int ic=0; ic < naOut; ic++) {
          if(Math.abs(a1[ic]-a2[ic]) > 0.0001) {
              javax.swing.JOptionPane.showMessageDialog(this,
                "Big trouble!\n\n"+
                "the complex \""+namn.ident[found+cs.Na]+"\" exists both\n"+
                "in file \""+dataFile2.getName()+"\" and\n"+
                "in file \""+dataFile.getName()+"\",\n"+
                "but the stoichiometries are different!\n\n"+
                "Please correct the input files.\nTerminating . . .",
                "Modify Chemical System", javax.swing.JOptionPane.ERROR_MESSAGE);
              if(dbg) {System.out.println("Complex has the same name in both files but different stoichiometries."+nl+
                      "   Can not continue merge.");}
              setupFrame();
              setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
              return;
          }
        } //for ic
        //check also for equal equilibrium constants
        if(Math.abs(cs.lBeta[found]-cs2.lBeta[ix2]) > 0.02) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Warning:\n\n"+
                "logK = "+cs.lBeta[found]+
                "for complex \""+namn.ident[found+cs.Na]+"\"\n"+
                "in file \""+dataFile.getName()+"\"\n"+
                "but logK = "+cs2.lBeta[ix2]+
                "in file \""+dataFile2.getName()+"\",\n\n"+
                "The first value will be used.",
                "Modify Chemical System", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
      } //if found >=0
    } //for ix2
    int solblComplxsNew = nxOut - cs.nx;
    if(dbg) {System.out.println("New soluble complexes= "+solblComplxsNew+"  total="+nxOut);}

    //--- find new Solid reactions in 2nd file ---
    // total number of solids (components + reactions) so far
    // "mSol" is the total number of solids (reaction products + solid components)
    int mSolOut = (mSol-cs.solidC) + solidC_Out;
    for(int ix2=cs2.nx; ix2 < (cs2.nx + (mSol2-cs2.solidC)); ix2++) {
      found = -1;
      for(int ix1=cs.nx; ix1 < (cs.nx + (mSol-cs.solidC)); ix1++) {
        if(Util.nameCompare(namn.ident[ix1+cs.Na],namn2.ident[ix2+cs2.Na])) {
          found = ix1; break;
        }
      } //for ix1

      if(found < 0) { //new solid reaction product
        if(dbg) {System.out.println("New solid reaction product: "+namn2.ident[ix2+cs2.Na]);}
        //check that no component in File1 has the same name as this solid product
        for(int ic1=0; ic1 < cs.Na; ic1++) {
          if(Util.nameCompare(namn.identC[ic1],namn2.ident[ix2+cs2.Na])) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Big trouble!\n\n"+
                "solid product: \""+namn2.ident[ix2+cs2.Na]+"\"\n"+
                "in file \""+dataFile2.getName()+"\"\n"+
                "has the same name as component: \""+namn.identC[ic1]+"\"\n"+
                "in file \""+dataFile.getName()+"\"\n\n"+
                "Please switch a component/reaction.\n"+
                "Terminating . . .",
                "Modify Chemical System", javax.swing.JOptionPane.ERROR_MESSAGE);
            if(dbg) {System.out.println("Solid product in file \""+dataFile2.getName()+"\""+nl+
                    "   has the same name as component in file \""+dataFile.getName()+"\"."+nl+
                    "   Can not continue merge.");}
            setupFrame();
            setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            return;
          }
        } //for ic1
        mSolOut++;
        identM.add(namn2.ident[ix2+cs2.Na]);
        lBetaM.add(cs2.lBeta[ix2]);
        aM.add(new Double[cs2.Na]);
        for(int i=0; i < cs2.Na; i++) {aM.get(aM.size()-1)[i] = cs2.a[ix2][i];}
      } //if found <0

      else { //found >=0: there are two solid products with the same name
        //check that not only the names match, but also the stoichiometries match
        for(int ic=0; ic < naOut;  ic++) {a1[ic] = 0; a2[ic] = 0;}
        for(int ic=0; ic < cs.Na;  ic++) {a1[namn.iel[ic]] = cs.a[found][ic];}
        for(int ic=0; ic < cs2.Na; ic++) {a2[namn2.iel[ic]]= cs2.a[ix2][ic];}
        for(int ic=0; ic < naOut; ic++) {
          if(Math.abs(a1[ic]-a2[ic]) > 0.0001) {
              javax.swing.JOptionPane.showMessageDialog(this,
                "Big trouble!\n\n"+
                "the solid product \""+namn.ident[found+cs.Na]+"\" exists both\n"+
                "in file \""+dataFile2.getName()+"\" and\n"+
                "in file \""+dataFile.getName()+"\",\n"+
                "but the stoichiometries are different!\n\n"+
                "Please correct the input files.\nTerminating . . .",
                "Modify Chemical System", javax.swing.JOptionPane.ERROR_MESSAGE);
              if(dbg) {System.out.println("Solid product has the same name in both files but different stoichiometries."+nl+
                      "   Can not continue merge.");}
              setupFrame();
              setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
              return;
          }
        } //for ic
        //check also for equal equilibrium constants
        if(Math.abs(cs.lBeta[found]-cs2.lBeta[ix2]) > 0.02) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Warning:\n\n"+
                "logK = "+cs.lBeta[found]+
                "for reaction \""+namn.ident[found+cs.Na]+"\"\n"+
                "in file \""+dataFile.getName()+"\"\n"+
                "but logK = "+cs2.lBeta[ix2]+
                "in file \""+dataFile2.getName()+"\",\n\n"+
                "The first value will be used.",
                "Modify Chemical System", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
      } //if found >=0
    } //for ix2
    int solidComplxsNew = (mSolOut-solidC_Out) - (mSol-cs.solidC);
    if(dbg) {System.out.println("New solid products= "+solidComplxsNew+"  total= "+(mSolOut-solidC_Out));}

    //--- check temperature/pressure and title
    if(Double.isNaN(diagr.temperature) && !Double.isNaN(diag2.temperature)) {
      if(dbg) {System.out.println("Setting temperature to: "+diag2.temperature);}
      diagr.temperature = diag2.temperature;
    }
    if(!Double.isNaN(diagr.temperature) && !Double.isNaN(diag2.temperature) &&
       Math.abs(diagr.temperature-diag2.temperature)>0.0001) {
      if(dbg) {System.out.println("Warning: different temperatures."+nl+
              "  in file \""+dataFile.getName()+"\" t="+diagr.temperature+nl+
              "  but in file \""+dataFile2.getName()+"\" t="+diag2.temperature+","+nl+
              "  keeping t = "+diagr.temperature);}
    }
    if(Double.isNaN(diagr.pressure) && !Double.isNaN(diag2.pressure)) {
      if(dbg) {System.out.println("Setting pressure to: "+diag2.pressure);}
      diagr.pressure = diag2.pressure;
    }
    if(!Double.isNaN(diagr.pressure) && !Double.isNaN(diag2.pressure) &&
       Math.abs(diagr.pressure-diag2.pressure)>0.0001) {
      if(dbg) {System.out.println("Warning: different pressures."+nl+
              "  in file \""+dataFile.getName()+"\" P="+diagr.pressure+nl+
              "  but in file \""+dataFile2.getName()+"\" P="+diag2.pressure+","+nl+
              "  keeping P = "+diagr.pressure);}
    }
    if(diag2.title != null && diagr.title == null) {
      if(dbg) {System.out.println("Setting title to: \""+diag2.title+"\"");}
      diagr.title = diag2.title;
    }
    if(diagr.title != null && diag2.title != null) {
      if(dbg) {System.out.println("Warning: title in merge file discarded.");}
    }

    //--- create new instances of ChemSystem and other Chem-classes
    //    and store the merged chemical system into it
    Chem.ChemSystem csNew;
    try{csNew = ch.new ChemSystem(naOut, (naOut+nxOut+mSolOut), mSolOut, solidC_Out);}
    catch (Chem.ChemicalParameterException ex) {
        MsgExceptn.showErrMsg(this, ex.getMessage(), 1);
        System.err.println(Util.stack2string(ex));
        csNew = null;
    }
    if(csNew == null) {
        storeInitialSystem(ch);
        setupFrame();
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        return;
    }
    Chem.ChemSystem.NamesEtc namnNew = csNew.namn;

    if(dbg) {System.out.print("---- New chemical system: components total= "+naOut+"  of which solid= "+solidC_Out+nl+
            "  reactions: soluble= "+nxOut+"  solid= "+(mSolOut-solidC_Out)+nl+
            "  total number of species = "+(naOut+nxOut+mSolOut));
            System.out.print(" (including components");
            if(solidC_Out >0) {System.out.print(" and solids corresponding to any solid components");}
            System.out.println(")");
    }

    boolean plotInfoGiven = (diagr.plotType >=0 && diagr.compX >=0 && dgrC.hur[0] >0);
    Chem.DiagrConcs dgrCNew = null;
    if(plotInfoGiven) {
        try{dgrCNew = ch.new DiagrConcs(naOut);}
        catch (Chem.ChemicalParameterException ex) {
            plotInfoGiven = false;
            dgrCNew = null;
            MsgExceptn.showErrMsg(this, ex.getMessage(), 1);
            System.err.println(Util.stack2string(ex));
        }
    } //if plotInfoGiven

    csNew.chemConcs = cs.chemConcs;
    csNew.solidC = solidC_Out;
    csNew.nx = nxOut;

    //copy component names
    for(int ic1=0; ic1 < cs.Na; ic1++) {
      namnNew.identC[namn.iel[ic1]] = namn.identC[ic1];
      namnNew.ident[namn.iel[ic1]] = namn.identC[ic1];
    }
    for(int ic2=0; ic2 < cs2.Na; ic2++) {
      namnNew.identC[namn2.iel[ic2]] = namn2.identC[ic2];
      namnNew.ident[namn2.iel[ic2]] = namn2.identC[ic2];
    }
    //copy soluble complexes
    for(int ix1=0; ix1 < cs.nx; ix1++) {
      namnNew.ident[naOut+ix1] = namn.ident[cs.Na+ix1];
      csNew.lBeta[ix1] = cs.lBeta[ix1];
      for(int ic=0; ic < naOut; ic++) {csNew.a[ix1][ic] = 0;}
      for(int ic1=0; ic1 < cs.Na; ic1++) {csNew.a[ix1][namn.iel[ic1]] = cs.a[ix1][ic1];}
    }
    for(int ixM = 0; ixM < solblComplxsNew; ixM++) {
      namnNew.ident[naOut+cs.nx + ixM] = identM.get(ixM);
      csNew.lBeta[cs.nx + ixM] = lBetaM.get(ixM);
      for(int ic=0; ic < naOut; ic++) {csNew.a[cs.nx + ixM][ic] = 0;}
      for(int ic2=0; ic2 < cs2.Na; ic2++) {
            csNew.a[cs.nx + ixM][namn2.iel[ic2]] = aM.get(ixM)[ic2];
      }
    }
    //copy solid products
    for(int if1=0; if1 < (mSol-cs.solidC); if1++) {
      namnNew.ident[naOut+nxOut + if1] = namn.ident[cs.Na+cs.nx + if1];
      csNew.lBeta[nxOut + if1] = cs.lBeta[cs.nx + if1];
      for(int ic=0; ic < naOut; ic++) {csNew.a[nxOut + if1][ic] = 0;}
      for(int ic1=0; ic1 < cs.Na; ic1++) {
        csNew.a[nxOut + if1][namn.iel[ic1]] = cs.a[cs.nx + if1][ic1];
      }
    }
    int soFar = nxOut + (mSol-cs.solidC);
    for(int ifM = solblComplxsNew; ifM < solblComplxsNew+solidComplxsNew; ifM++) {
      int ifS = ifM - solblComplxsNew;
      namnNew.ident[naOut+soFar + ifS] = identM.get(ifM);
      csNew.lBeta[soFar + ifS] = lBetaM.get(ifM);
      for(int ic=0; ic < naOut; ic++) {csNew.a[soFar + ifS][ic] = 0;}
      for(int ic2=0; ic2 < cs2.Na; ic2++) {
            csNew.a[soFar + ifS][namn2.iel[ic2]] = aM.get(ifM)[ic2];
      }
    }
    //--- add the "fictive" solids corresponding to any solid components
    if(solidC_Out >0) {
        int j,k;
        for(int ic=0; ic < solidC_Out; ic++) {
            j = (csNew.Ms-csNew.Na-solidC_Out) + ic;
            k = (csNew.Na-solidC_Out)+ic;
            csNew.lBeta[j] = 0;
            for(int ic2=0; ic2 < csNew.Na; ic2++) {
                if(ic2 == k) {csNew.a[j][ic2] = 1;} else {csNew.a[j][ic2] = 0;}
            }
            namnNew.ident[j+csNew.Na] = namnNew.identC[k];
            csNew.noll[k] = true;
        }
    }

    if(dbg) {
        System.out.print("---- The merged ");
        csNew.printChemSystem(System.out);
    }

    //--- if there is a plot deffinition and concentration(s) for File1,
    //    then the information is kept.
    if(diagr.compMain >=0) {diagr.compMain = namn.iel[diagr.compMain];}
    if(diagr.compX >=0) {diagr.compX = namn.iel[diagr.compX];}
    if(diagr.compY >=0) {diagr.compY = namn.iel[diagr.compY];}
    if(plotInfoGiven && dgrCNew != null) {
      for(int ic1=0; ic1 < cs.Na; ic1++) {
        if(dgrC.hur[ic1] >0) {
          dgrCNew.hur[namn.iel[ic1]] = dgrC.hur[ic1];
          dgrCNew.cLow[namn.iel[ic1]] = dgrC.cLow[ic1];
          dgrCNew.cHigh[namn.iel[ic1]] = dgrC.cHigh[ic1];
        }
      }//for ic1
      for(int ic2=0; ic2 < cs2.Na; ic2++) {
        if(dgrC2.hur[ic2] >0) {
          dgrCNew.hur[namn2.iel[ic2]] = dgrC2.hur[ic2];
          dgrCNew.cLow[namn2.iel[ic2]] = dgrC2.cLow[ic2];
          dgrCNew.cHigh[namn2.iel[ic2]] = dgrC2.cHigh[ic2];
        }
      }//for ic2
    }//if there is plot info

    //--- change the pointers from the old to the new instances
    ch.chemSystem = csNew;
    ch.chemSystem.namn = namnNew;
    try{ch.diagrConcs = ch.new DiagrConcs(naOut);}
    catch (Chem.ChemicalParameterException ex) {
        MsgExceptn.showErrMsg(this, ex.getMessage(), 1);
        System.err.println(Util.stack2string(ex));
        return;}
    this.cs = csNew;
    this.namn = namnNew;
    this.diagr = ch.diag;
    this.dgrC = dgrCNew;

    //--- is there a plot deffinition? check concentrations
    if(plotInfoGiven && dgrC != null) {
      for(int ic=0; ic < cs.Na; ic++) {
        if(dgrC.hur[ic] <=0) {
          DefaultPlotAndConcs.setDefaultConc(ic, namn.identC[ic], dgrC, pd.kth);
        }//if no concentration range
      }//for ic
    }//if there is plot info

    // --- if there are any solid components, add corresponding
    //     fictive solid products corresponding to the components
    ReadChemSyst.addFictiveSolids(cs);

    storeInitialSystem(ch);

    modified = true;
    merged = true;
    if(dbg) {System.out.println(" ---- Merge successful.");}
    setupFrame();
    setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
  } //mergeTwoSystems()

//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="reactionText">
public static String reactionText(Chem.ChemSystem cs,
        int complex, boolean logK) {
  if(cs == null || cs.namn == null) {
      return "** Error **";
  }
if(complex < 0 || complex >= (cs.Ms-cs.Na)) {
      return noReactionMessage;
  }
  
  boolean first = true;
  String text = "";
  String stoich;
  double w;
  for(int ic = 0; ic < cs.Na; ic++) {
    w = cs.a[complex][ic];
    if(w > 0) {
      if(first) {stoich = ""; first = false;} else {stoich = " +";}
      if (Math.abs(w-1)>1e-6) {stoich = stoich + Util.formatNumAsInt(w);}
      text = text + stoich + " " + cs.namn.identC[ic];
    }
  } //for ic
  text = text + " = "; first = true;
  for(int ic = 0; ic < cs.Na; ic++) {
    w = -cs.a[complex][ic];
    if(w > 0) {
      if(first) {stoich = ""; first = false;} else {stoich = " +";}
      if (Math.abs(w-1)>1e-6) {stoich = stoich + Util.formatNumAsInt(w);}
      text = text + stoich + " " + cs.namn.identC[ic];
    }
  } //for ic
  if(first) {stoich = " ";} else {stoich = " + ";}
  text = Util.rTrim(text) + stoich + cs.namn.ident[complex+cs.Na];
  if(logK) {text = text + ";  logK="+Util.formatNum(cs.lBeta[complex]);}
  return text;
} //reactionText

//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="readDataFile()">
/** Read a data file
 * @param dtaFile the data file to read.
 * @param dbg true if debug information is to be printed
 * @return true if there is no error, false otherwise.
 */
  private boolean readDataFile(java.io.File dtaFile, boolean dbg){
    //--- create a ReadData instance
    ReadDataLib rd;
    try {rd = new ReadDataLib(dtaFile);}
    catch (ReadDataLib.DataFileException ex) {
        MsgExceptn.showErrMsg(this,ex.getMessage(),1);
        return false;
    }
    if(dbg) {
        System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - -");
        System.out.println("Reading input data file \""+dtaFile+"\"");
    }
    //--- read the chemical system (names, equilibrium constants, stoichiometry)
    boolean warn = true; // report missing plot data as a warning
    try {
      chRead = ReadChemSyst.readChemSystAndPlotInfo(rd, dbg, warn, System.out);
    }
    catch (ReadChemSyst.DataLimitsException ex) {
        System.err.println(ex.getMessage());
    }
    catch (ReadChemSyst.ReadDataFileException ex) {
        System.err.println(ex.getMessage());
    }
    catch (ReadChemSyst.PlotDataException ex) {}
    catch (ReadChemSyst.ConcDataException ex) {}
    if(chRead == null) {
        try {rd.close();}
        catch (ReadDataLib.ReadDataLibException ex) {MsgExceptn.exception(ex.getMessage());}
        MsgExceptn.showErrMsg(spana.MainFrame.getInstance(),
                "Error while reading file"+nl+"\""+dtaFile+"\"", 1);
        return false;
    }

    //--- get other information to be saved in the modified data file
    //temperature written as a comment in the data file?
    double w;
    try{w = rd.getTemperature();}
    catch(ReadDataLib.DataReadException ex) {System.err.println(nl+ex.getMessage()); w = Double.NaN;}
    chRead.diag.temperature = w;
    try{w = rd.getPressure();}
    catch(ReadDataLib.DataReadException ex) {System.err.println(nl+ex.getMessage()); w = Double.NaN;}
    chRead.diag.pressure = w;

    // is there a title?
    if(chRead.diag.plotType >= 0 && chRead.diagrConcs.hur[0] >= 1) {
        try {chRead.diag.title = rd.readLine();}
        catch (ReadDataLib.DataEofException ex) {chRead.diag.title = null;}
        catch (ReadDataLib.DataReadException ex) {chRead.diag.title = null;}
    }

    //--- finished
    try {rd.close();}
    catch (ReadDataLib.ReadDataLibException ex) {
        System.err.println(ex.getMessage());
        return false;
    }
    if(dbg) {System.out.println("Finished reading the input data file");
                System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - -");
    }

    return true;
  } // readDataFile()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="setupFrame()">
  private void setupFrame() {
    jTextFieldDataFile.setText(dataFile.getPath());
    //--- Components
    int solubleComps = cs.Na - cs.solidC;
    if(solubleComps <=0) {jLabel1.setText("No Soluble Components");}
    else if(solubleComps ==1) {jLabel1.setText("1 Soluble Component:");}
    else if(solubleComps >1) {jLabel1.setText(solubleComps+" Soluble Components:");}
    listSolubleCompModel.clear();
    if(solubleComps>=1) {
        for(int i=0; i < solubleComps; i++) {
            listSolubleCompModel.addElement(namn.identC[i]);
        }
    }
    if(cs.solidC <=0) {jLabel3.setText("No Solid Components");}
    else if(cs.solidC ==1) {jLabel3.setText("1 Solid Component:");}
    else if(cs.solidC >1) {jLabel3.setText(cs.solidC+" Solid Components:");}
    listSolidCompModel.clear();
    if(cs.solidC >=1) {
        for(int i=solubleComps; i < cs.Na; i++) {
            listSolidCompModel.addElement(namn.identC[i]);
        }
    }
    //--- Complexes
    if(cs.nx <=0) {jLabel2.setText("No Soluble Complexes");}
    else if(cs.nx ==1) {jLabel2.setText("1 Soluble Complex:");}
    else if(cs.nx >1) {jLabel2.setText(cs.nx+" Soluble Complexes:");}
    listSolubleCmplxModel.clear();
    if(cs.nx >=1) {
        for(int i=cs.Na; i < cs.Na+cs.nx; i++) {
            listSolubleCmplxModel.addElement(namn.ident[i]);
        }
    }
    int solidComplexes = cs.mSol-cs.solidC;
    if(solidComplexes <=0) {jLabel4.setText("No Solid Products");}
    else if(solidComplexes ==1) {jLabel4.setText("1 Solid Product:");}
    else if(solidComplexes >1) {jLabel4.setText(solidComplexes+" Solid Products:");}
    listSolidCmplxModel.clear();
    if(solidComplexes >=1) {
        for(int i=(cs.Na+cs.nx); i < cs.Ms-cs.solidC; i++) {
            listSolidCmplxModel.addElement(namn.ident[i]);
        }
    }
    //---
    if(!modified) {
        jTextFieldDataFile.setEnabled(true);
    } else {
        java.awt.CardLayout cl = (java.awt.CardLayout)jPanel1Top.getLayout();
        cl.show(jPanel1Top, "cardNewSystem");
        jTextFieldDataFile.setEnabled(false);
    }
    jButton_Delete.setEnabled(true);
    jButton_Delete.setText("<html><u>D</u>elete</html>");
    jButton_Delete.setForeground(java.awt.Color.black);
    jButton_Exchange.setEnabled(true);
    jButton_Exchange.setForeground(java.awt.Color.black);
    jButton_Merge.setEnabled(true);
    jButton_Merge.setForeground(java.awt.Color.black);
    jButton_Save.setEnabled(true);
    jButton_Save.setForeground(java.awt.Color.black);
    jButton_Save.setText("<html><u>S</u>ave &amp; exit</html>");
    jButtonUp.setEnabled(false);
    jButtonDn.setEnabled(false);
    //---
    if(listSolubleCmplxModel.getSize() <=0 &&
            listSolidCmplxModel.getSize() <=0) {
        jButton_Exchange.setEnabled(false);
        jButton_Exchange.setForeground(java.awt.Color.gray);
    }
    //---
  } //setupFrame()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="storeInitialSystem(chem)">
  /** Store the names of the components in the axes, etc,
   *  needed by checkPlotInfo() */
  private void storeInitialSystem(Chem chem) {
    Na0 = chem.chemSystem.Na;
    if(chem.diag.compX >= 0) {compXname0= chem.chemSystem.namn.identC[chem.diag.compX];}
    if(chem.diag.compY >= 0) {compYname0= chem.chemSystem.namn.identC[chem.diag.compY];}
    if(chem.diag.compMain >= 0) {compMainName0= chem.chemSystem.namn.identC[chem.diag.compMain];}
    if(identC0.size()>0) { //empty the ArrayList
      for(int i = identC0.size()-1; i>=0 ; i--) {identC0.remove(i);}
    }
    for(int i=0; i < chem.chemSystem.Na; i++) {identC0.add(i, chem.chemSystem.namn.identC[i]);}
  }//storeInitialSystem
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="upDownComponent()">
/** Move the selected component "up" or "down" one position in the
 * component list.
 * @param up if true the component is moved one step closser to zero (move up),
 * if false the orther of component is increased by one (moved down).*/
  private void upDownComponent(boolean up) {
    // which component is selected?
    // is the move possible?
    boolean impossibleMove = false;
    int compToMove;
    String compToMoveName;
    compToMove = jListSolubComps.getSelectedIndex();
    if(compToMove >= 0) {
      if(compToMove == 0 && up) {impossibleMove = true;}
      if(compToMove >= (listSolubleCompModel.getSize()-1) && !up) {impossibleMove = true;}
      compToMoveName = jListSolubComps.getSelectedValue().toString();
    } else {
        compToMove = jListSolidComps.getSelectedIndex();
        if(compToMove == 0 && up) {impossibleMove = true;}
        if(compToMove >= (listSolidCompModel.getSize()-1) && !up) {impossibleMove = true;}
        compToMoveName = jListSolidComps.getSelectedValue().toString();
        if(compToMove>=0) {compToMove = compToMove +(cs.Na - cs.solidC);}
    }
    if(impossibleMove) {
      String move = "up"; if(!up) {move = "down";}
      System.err.println("Programming error: impossible move \""+move+"\""+
                         " of component "+compToMove+" \""+compToMoveName+"\"");
      setupFrame();
      return;
    }
    // check that a component is indeed selected
    if(compToMove < 0) {
        String msg = "list";
        if(listSolubleCompModel.getSize() > 0 && listSolidCompModel.getSize() > 0) {msg = "lists";}
        javax.swing.JOptionPane.showMessageDialog(this, "Please, select one component from the "+msg+".", "Modify Chemical System", javax.swing.JOptionPane.WARNING_MESSAGE);
        setupFrame();
        return;
    }
    disableButtons();
    double w;
    int otherCompMoved;
    if(up) {otherCompMoved = compToMove -1;}
    else {otherCompMoved = compToMove +1;}

    setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

    for(int ic=0; ic < cs.Na; ic++) {namn.iel[ic] = ic;}
    namn.iel[otherCompMoved] = compToMove;
    namn.iel[compToMove] = otherCompMoved;

    //move info on components
    String other = namn.identC[otherCompMoved];
    namn.identC[otherCompMoved] = compToMoveName;
    namn.identC[compToMove] = other;
    namn.ident[otherCompMoved] = namn.identC[otherCompMoved];
    namn.ident[compToMove] = namn.identC[compToMove];

    boolean plotInfoGiven = false;
    if(diagr.plotType >=0 && diagr.compX >=0 && dgrC.hur[0] >=1) {plotInfoGiven = true;}
    if(plotInfoGiven) {
      diagr.compX = namn.iel[diagr.compX];
      if(diagr.compY >=0) {diagr.compY = namn.iel[diagr.compY];}
      if(diagr.compMain >=0) {diagr.compMain = namn.iel[diagr.compMain];}
      int otherHur = dgrC.hur[otherCompMoved];
      dgrC.hur[otherCompMoved] = dgrC.hur[compToMove];
      dgrC.hur[compToMove] = otherHur;
      w = dgrC.cLow[otherCompMoved];
      dgrC.cLow[otherCompMoved] = dgrC.cLow[compToMove];
      dgrC.cLow[compToMove] = w;
      w = dgrC.cHigh[otherCompMoved];
      dgrC.cHigh[otherCompMoved] = dgrC.cHigh[compToMove];
      dgrC.cHigh[compToMove] = w;
    }
    //move stoichiometric coefficients
    for(int ix=0; ix < cs.Ms-cs.Na; ix++) {
      w = cs.a[ix][otherCompMoved];
      cs.a[ix][otherCompMoved] = cs.a[ix][compToMove];
      cs.a[ix][compToMove] = w;
    }//for ix

    modified = true;
    storeInitialSystem(ch);
    setupFrame();
    //select (highlight) the component that has been moved
    if(compToMove < cs.Na-cs.solidC) {
      jListSolubComps.setSelectedIndex(otherCompMoved);
    } else {
      jListSolidComps.setSelectedIndex(otherCompMoved - (cs.Na-cs.solidC));
    }
    component_Click();
    setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
  } //upDownComponent()
//</editor-fold>

//</editor-fold>

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonDn;
    private javax.swing.JButton jButtonUp;
    private javax.swing.JButton jButton_Delete;
    private javax.swing.JButton jButton_Exchange;
    private javax.swing.JButton jButton_Merge;
    private javax.swing.JButton jButton_Quit;
    private javax.swing.JButton jButton_Save;
    private javax.swing.JLabel jLabel0;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabelReaction;
    private javax.swing.JList jListSolidCmplx;
    private javax.swing.JList jListSolidComps;
    private javax.swing.JList jListSolubCmplx;
    private javax.swing.JList jListSolubComps;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel1Top;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel2down;
    private javax.swing.JPanel jPanel2up;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanelDataFile;
    private javax.swing.JPanel jPanelNew;
    private javax.swing.JPanel jPanelUpDown;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextField jTextFieldDataFile;
    // End of variables declaration//GEN-END:variables

}
