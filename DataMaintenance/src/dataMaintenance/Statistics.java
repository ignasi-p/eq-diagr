package dataMaintenance;

import lib.common.MsgExceptn;
import lib.common.Util;
import lib.database.CheckDatabases;
import lib.database.ProgramDataDB;
import lib.huvud.Div;
import lib.huvud.ProgramConf;
import lib.huvud.SortedListModel;

/** Find out possible errors in the databases in the list.
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
public class Statistics extends javax.swing.JFrame {
  private boolean finished = false;
  private final ProgramDataDB pd;
  private final ProgramConf pc;
  private final javax.swing.JFrame parent;
  /** the output file where statistics and errors are written */
  private java.io.PrintWriter outputPW;

  private final SortedListModel sortedModelCompsFnd = new SortedListModel();
  private final SortedListModel sortedModelCompsUnknown = new SortedListModel();
  private final SortedListModel sortedModelNameWoutNumber = new SortedListModel();
  private final SortedListModel sortedModelNumberWoutName = new SortedListModel();
  private final SortedListModel sortedModelChargeError = new SortedListModel();

  private final javax.swing.border.Border defBorder;
  private final javax.swing.border.Border highlightedBorder =
             javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED,
                new java.awt.Color(102,102,102),
                new java.awt.Color(255,255,255),
                new java.awt.Color(102,102,102),
                new java.awt.Color(0,0,0));

  private java.awt.Dimension windowSize = new java.awt.Dimension(400,280);

  /** New-line character(s) to substitute "\n" */
  private static final String nl = System.getProperty("line.separator");
  private static final String SLASH = java.io.File.separator;

//<editor-fold defaultstate="collapsed" desc="Constructor">
  /** Creates new form Statistics
   * @param pc0
   * @param pd0
   * @param parent  */
  public Statistics(
          ProgramConf pc0,
          ProgramDataDB pd0,
          javax.swing.JFrame parent) {
    initComponents();
    this.parent = parent;
    setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
    pc = pc0;
    pd = pd0;
    System.out.println(DataMaintenance.LINE+nl+"Starting \"Statistics\"");
    defBorder = jScrollPaneComps.getBorder();  // get the default list border
    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    //--- Close window on ESC key
    javax.swing.KeyStroke escKeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(escKeyStroke,"ESCAPE");
    javax.swing.Action escAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            closeWindow();
        }};
    getRootPane().getActionMap().put("ESCAPE", escAction);
    //--- Alt-X eXit
    javax.swing.KeyStroke altXKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altXKeyStroke,"ALT_X");
    getRootPane().getActionMap().put("ALT_X", escAction);
    //--- Alt-Q quit
    javax.swing.KeyStroke altQKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altQKeyStroke,"ALT_Q");
    getRootPane().getActionMap().put("ALT_Q", escAction);
    //--- F1 for help
    javax.swing.KeyStroke f1KeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_F1,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(f1KeyStroke,"F1");
    javax.swing.Action f1Action = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            Statistics.this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            Thread hlp = new Thread() {@Override public void run(){
                String[] a = {"DB_Databases_htm"};
                lib.huvud.RunProgr.runProgramInProcess(null,ProgramConf.HELP_JAR,a,false,pc.dbg,pc.pathAPP);
                try{Thread.sleep(1500);}   //show the "wait" cursor for 1.5 sec
                catch (InterruptedException e) {}
                Statistics.this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
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
    //---- Title, menus, etc
    this.setTitle("Database - Statistics");
    setPanelsEnabled(false);
    //---- Icon
    String iconName = "images/Sigma.gif";
    java.net.URL imgURL = this.getClass().getResource(iconName);
    if (imgURL != null) {this.setIconImage(new javax.swing.ImageIcon(imgURL).getImage());}
    else {System.out.println("Error: Could not load image = \""+iconName+"\"");}

  } //constructor
//</editor-fold>

  public void start() {
    this.jButtonClose.setEnabled(false);
    this.setVisible(true);
    windowSize = this.getSize();
    if(pd.msgFrame != null) {
        pd.msgFrame.setParentFrame(Statistics.this);
        jCheckBoxDebugFrame.setSelected(pd.msgFrame.isVisible());
    }

    sortedModelCompsFnd.clear();
    sortedModelCompsUnknown.clear();
    sortedModelNameWoutNumber.clear();
    sortedModelNumberWoutName.clear();
    sortedModelChargeError.clear();
    parent.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    if(!startStatistics()) {closeWindow();}
  }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelCheckBox = new javax.swing.JPanel();
        jCheckBoxDebugFrame = new javax.swing.JCheckBox();
        jButtonClose = new javax.swing.JButton();
        jLabelWait = new javax.swing.JLabel();
        jLabelComps = new javax.swing.JLabel();
        jScrollPaneComps = new javax.swing.JScrollPane();
        jListComps = new javax.swing.JList();
        jPanelTot = new javax.swing.JPanel();
        jLabelTot = new javax.swing.JLabel();
        jLabelTotCmplx = new javax.swing.JLabel();
        jLabelN = new javax.swing.JLabel();
        jLabelTotComps = new javax.swing.JLabel();
        jLabelTotCompsFiles = new javax.swing.JLabel();
        jLabelNa = new javax.swing.JLabel();
        jLabelNaFiles = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanelErrs = new javax.swing.JPanel();
        jLabelErr = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabelNameWoutNumb = new javax.swing.JLabel();
        jScrollNameWoutNumb = new javax.swing.JScrollPane();
        jListNameWoutNumb = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jLabelNumbWoutName = new javax.swing.JLabel();
        jScrollNumbWoutName = new javax.swing.JScrollPane();
        jListNumbWoutName = new javax.swing.JList();
        jPanel4 = new javax.swing.JPanel();
        jLabelChargeError = new javax.swing.JLabel();
        jScrollChargeError = new javax.swing.JScrollPane();
        jListChargeError = new javax.swing.JList();
        jPanel5 = new javax.swing.JPanel();
        jLabelCompsUnknown = new javax.swing.JLabel();
        jScrollCompsUnk = new javax.swing.JScrollPane();
        jListCompsUnk = new javax.swing.JList();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });
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

        jCheckBoxDebugFrame.setMnemonic('s');
        jCheckBoxDebugFrame.setText("show messages");
        jCheckBoxDebugFrame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxDebugFrameActionPerformed(evt);
            }
        });

        jButtonClose.setMnemonic('c');
        jButtonClose.setText("Close");
        jButtonClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCloseActionPerformed(evt);
            }
        });

        jLabelWait.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelWait.setText("( Please wait ...)");

        javax.swing.GroupLayout jPanelCheckBoxLayout = new javax.swing.GroupLayout(jPanelCheckBox);
        jPanelCheckBox.setLayout(jPanelCheckBoxLayout);
        jPanelCheckBoxLayout.setHorizontalGroup(
            jPanelCheckBoxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCheckBoxLayout.createSequentialGroup()
                .addComponent(jButtonClose)
                .addGap(52, 52, 52)
                .addComponent(jCheckBoxDebugFrame)
                .addGap(73, 73, 73)
                .addComponent(jLabelWait)
                .addContainerGap(137, Short.MAX_VALUE))
        );
        jPanelCheckBoxLayout.setVerticalGroup(
            jPanelCheckBoxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCheckBoxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jCheckBoxDebugFrame)
                .addComponent(jButtonClose)
                .addComponent(jLabelWait))
        );

        jLabelComps.setLabelFor(jListComps);
        jLabelComps.setText("Components found:");

        jListComps.setModel(sortedModelCompsFnd);
        jListComps.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jListCompsFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jListCompsFocusLost(evt);
            }
        });
        jScrollPaneComps.setViewportView(jListComps);

        jLabelTot.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelTot.setText("Totals");

        jLabelTotCmplx.setText("Number of reactions =");

        jLabelN.setText("0");

        jLabelTotComps.setText("Number of components =");

        jLabelTotCompsFiles.setText("Nbr comps. in \"element\" files: ");

        jLabelNa.setText("0");

        jLabelNaFiles.setText("0");

        javax.swing.GroupLayout jPanelTotLayout = new javax.swing.GroupLayout(jPanelTot);
        jPanelTot.setLayout(jPanelTotLayout);
        jPanelTotLayout.setHorizontalGroup(
            jPanelTotLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTotLayout.createSequentialGroup()
                .addGroup(jPanelTotLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelTot)
                    .addGroup(jPanelTotLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelTotLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelTotLayout.createSequentialGroup()
                                .addComponent(jLabelTotCmplx)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabelN))
                            .addGroup(jPanelTotLayout.createSequentialGroup()
                                .addComponent(jLabelTotComps)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabelNa))
                            .addGroup(jPanelTotLayout.createSequentialGroup()
                                .addComponent(jLabelTotCompsFiles)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabelNaFiles)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelTotLayout.setVerticalGroup(
            jPanelTotLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTotLayout.createSequentialGroup()
                .addComponent(jLabelTot)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelTotLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelTotCmplx)
                    .addComponent(jLabelN))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelTotLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelTotComps)
                    .addComponent(jLabelNa))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelTotLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelTotCompsFiles)
                    .addComponent(jLabelNaFiles))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        jPanelErrs.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N

        jLabelErr.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelErr.setText("Errors:");

        javax.swing.GroupLayout jPanelErrsLayout = new javax.swing.GroupLayout(jPanelErrs);
        jPanelErrs.setLayout(jPanelErrsLayout);
        jPanelErrsLayout.setHorizontalGroup(
            jPanelErrsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelErrsLayout.createSequentialGroup()
                .addComponent(jLabelErr)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelErrsLayout.setVerticalGroup(
            jPanelErrsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelErrsLayout.createSequentialGroup()
                .addComponent(jLabelErr)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabelNameWoutNumb.setLabelFor(jListNameWoutNumb);
        jLabelNameWoutNumb.setText("<html>Reactions with a<br>component and<br>no stoich.coef.</html>");

        jListNameWoutNumb.setBackground(new java.awt.Color(215, 215, 215));
        jListNameWoutNumb.setModel(sortedModelNameWoutNumber);
        jListNameWoutNumb.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jListNameWoutNumbFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jListNameWoutNumbFocusLost(evt);
            }
        });
        jScrollNameWoutNumb.setViewportView(jListNameWoutNumb);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollNameWoutNumb, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabelNameWoutNumb, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabelNameWoutNumb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollNameWoutNumb, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
        );

        jLabelNumbWoutName.setLabelFor(jListNumbWoutName);
        jLabelNumbWoutName.setText("<html>Reactions with a<br>stoich.coef. and<br>no component</html>");

        jListNumbWoutName.setModel(sortedModelNumberWoutName);
        jListNumbWoutName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jListNumbWoutNameFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jListNumbWoutNameFocusLost(evt);
            }
        });
        jScrollNumbWoutName.setViewportView(jListNumbWoutName);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollNumbWoutName, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelNumbWoutName, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabelNumbWoutName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollNumbWoutName))
        );

        jLabelChargeError.setLabelFor(jListChargeError);
        jLabelChargeError.setText("<html>Reactions with<br>charge<br>imbalance</html>");

        jListChargeError.setModel(sortedModelChargeError);
        jListChargeError.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jListChargeErrorFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jListChargeErrorFocusLost(evt);
            }
        });
        jScrollChargeError.setViewportView(jListChargeError);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelChargeError, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollChargeError, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabelChargeError, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollChargeError, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
        );

        jLabelCompsUnknown.setText("<html>&nbsp;<br>Unknown<br>components:</html>");
        jLabelCompsUnknown.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jListCompsUnk.setModel(sortedModelCompsUnknown);
        jListCompsUnk.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jListCompsUnkFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jListCompsUnkFocusLost(evt);
            }
        });
        jScrollCompsUnk.setViewportView(jListCompsUnk);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabelCompsUnknown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jScrollCompsUnk, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabelCompsUnknown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollCompsUnk, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jSeparator1)
                        .addContainerGap())
                    .addComponent(jPanelErrs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabelComps)
                                    .addComponent(jScrollPaneComps, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(jPanelTot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanelCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelTot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabelComps)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPaneComps, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelErrs, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

//<editor-fold defaultstate="collapsed" desc="Events">

    private void jButtonCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCloseActionPerformed
        closeWindow();
    }//GEN-LAST:event_jButtonCloseActionPerformed

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

    private void jCheckBoxDebugFrameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDebugFrameActionPerformed
        if(pd.msgFrame != null) {pd.msgFrame.setVisible(jCheckBoxDebugFrame.isSelected());}
    }//GEN-LAST:event_jCheckBoxDebugFrameActionPerformed

    private void jListCompsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListCompsFocusGained
        jScrollPaneComps.setBorder(highlightedBorder);
        if(sortedModelCompsFnd.getSize()>0) {
            int i = Math.max(0,jListComps.getSelectedIndex());
            jListComps.setSelectedIndex(i);
            jListComps.ensureIndexIsVisible(i);
        }
    }//GEN-LAST:event_jListCompsFocusGained

    private void jListCompsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListCompsFocusLost
        jScrollPaneComps.setBorder(defBorder);
    }//GEN-LAST:event_jListCompsFocusLost

    private void jListNameWoutNumbFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListNameWoutNumbFocusGained
        jScrollNameWoutNumb.setBorder(highlightedBorder);
        if(sortedModelNameWoutNumber.getSize()>0) {
            int i = Math.max(0,jListNameWoutNumb.getSelectedIndex());
            jListNameWoutNumb.setSelectedIndex(i);
            jListNameWoutNumb.ensureIndexIsVisible(i);
        }
    }//GEN-LAST:event_jListNameWoutNumbFocusGained

    private void jListNameWoutNumbFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListNameWoutNumbFocusLost
        jScrollNameWoutNumb.setBorder(defBorder);
    }//GEN-LAST:event_jListNameWoutNumbFocusLost

    private void jListNumbWoutNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListNumbWoutNameFocusGained
        jScrollNumbWoutName.setBorder(highlightedBorder);
        if(sortedModelNumberWoutName.getSize()>0) {
            int i = Math.max(0,jListNumbWoutName.getSelectedIndex());
            jListNumbWoutName.setSelectedIndex(i);
            jListNumbWoutName.ensureIndexIsVisible(i);
        }
    }//GEN-LAST:event_jListNumbWoutNameFocusGained

    private void jListNumbWoutNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListNumbWoutNameFocusLost
        jScrollNumbWoutName.setBorder(defBorder);
    }//GEN-LAST:event_jListNumbWoutNameFocusLost

    private void jListChargeErrorFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListChargeErrorFocusGained
        jScrollChargeError.setBorder(highlightedBorder);
        if(sortedModelChargeError.getSize()>0) {
            int i = Math.max(0,jListChargeError.getSelectedIndex());
            jListChargeError.setSelectedIndex(i);
            jListChargeError.ensureIndexIsVisible(i);
        }
    }//GEN-LAST:event_jListChargeErrorFocusGained

    private void jListChargeErrorFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListChargeErrorFocusLost
        jScrollChargeError.setBorder(defBorder);
    }//GEN-LAST:event_jListChargeErrorFocusLost

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        if(pd.msgFrame != null) {jCheckBoxDebugFrame.setSelected(pd.msgFrame.isVisible());}
        else {jCheckBoxDebugFrame.setEnabled(false);}
    }//GEN-LAST:event_formWindowGainedFocus

    private void jListCompsUnkFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListCompsUnkFocusGained
        jScrollCompsUnk.setBorder(highlightedBorder);
        if(sortedModelCompsUnknown.getSize()>0) {
            int i = Math.max(0,jListCompsUnk.getSelectedIndex());
            jListCompsUnk.setSelectedIndex(i);
            jListCompsUnk.ensureIndexIsVisible(i);
        }
    }//GEN-LAST:event_jListCompsUnkFocusGained

    private void jListCompsUnkFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListCompsUnkFocusLost
        jScrollCompsUnk.setBorder(defBorder);
    }//GEN-LAST:event_jListCompsUnkFocusLost
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Methods">
  private void closeWindow() {
    finished = true;    //return from "waitFor()"
    this.notify_All();
    this.dispose();
  } // closeWindow()
  /** this method will wait for this window to be closed */
  public synchronized void waitFor() {
    while(!finished) {
        try {wait();} catch (InterruptedException ex) {}
    } // while
  } // waitFor()
  private synchronized void notify_All() { //needed by "waitFor()"
      notifyAll();
  }

  //<editor-fold defaultstate="collapsed" desc="setPanelsEnabled(enable)">
  private void setPanelsEnabled(boolean enable) {
    jButtonClose.setEnabled(enable);
    jLabelComps.setEnabled(enable);
    showJPanelTot(enable);
    jLabelErr.setEnabled(enable);
    java.awt.Color clr;
    if(enable) {
        clr = new java.awt.Color(0,0,0);
    } else {
        clr = new java.awt.Color(153,153,153);
    }
    jLabelNameWoutNumb.setForeground(clr);
    jLabelNumbWoutName.setForeground(clr);
    jLabelChargeError.setForeground(clr);
    jLabelCompsUnknown.setForeground(clr);
    if(enable) {
        clr = new java.awt.Color(255,255,255);
    } else {
        clr = new java.awt.Color(215,215,215);
    }
    jListComps.setEnabled(enable);
    jListComps.setBackground(clr);
    jListNameWoutNumb.setEnabled(enable);
    jListNameWoutNumb.setBackground(clr);
    jListNumbWoutName.setEnabled(enable);
    jListNumbWoutName.setBackground(clr);
    jListChargeError.setEnabled(enable);
    jListChargeError.setBackground(clr);
    jListCompsUnk.setEnabled(enable);
    jListCompsUnk.setBackground(clr);
    if(enable) {jLabelWait.setText(" ");} else {jLabelWait.setText("( Please wait... )");}
  }
  private void showJPanelTot(boolean show) {
    if(show) {
        jLabelTot.setText("Totals:");
        jLabelTotCmplx.setText("Number of reactions =");
        jLabelN.setText("0");
        jLabelTotComps.setText("Number of components =");
        jLabelNa.setText("0");
        jLabelTotCompsFiles.setText("Nbr comps. in files: ");
        jLabelNaFiles.setText("0");
    } else {
        jLabelTot.setText(" ");
        jLabelTotCmplx.setText(" ");
        jLabelN.setText(" ");
        jLabelTotComps.setText(" ");
        jLabelNa.setText(" ");
        jLabelTotCompsFiles.setText(" ");
        jLabelNaFiles.setText(" ");
    }
    jListComps.setEnabled(show);
    jListNameWoutNumb.setEnabled(show);
    jListNumbWoutName.setEnabled(show);
    jListChargeError.setEnabled(show);
    jListCompsUnk.setEnabled(show);
  } //showJPanelTot(show)
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="startStatistics">
 /** Performs the "real" work: checks and statistics
  * 
  * @return false if the procedure ends "unexpectedly": the user selects to quit
  */
  private boolean startStatistics() {
      System.out.println("---- startStatistics()"+nl+"default path: "+pc.pathDef);
      // ----- get an output file name
      String dir = pc.pathDef.toString();
      if(dir.endsWith(SLASH)) {dir = dir.substring(0, dir.length()-1);}
      System.out.println("querying for an output file name...");
      final String outFileName = Util.getSaveFileName(this, pc.progName,
                "Select an output file:", 7,
                dir + SLASH + "Statistics.txt", null);
      if(outFileName == null || outFileName.trim().length() <=0) {
        System.out.println("---- cancelled by the user");
        return false;
      }
      final java.io.File outFile = new java.io.File(outFileName);
      pc.setPathDef(outFile);
      outputPW = null;
      try {
          outputPW = new java.io.PrintWriter(
                new java.io.BufferedWriter(
                new java.io.FileWriter(outFile)));
      }
      catch (java.io.IOException ex) {
          String msg = "Error \""+ex.getMessage()+"\","+nl+
                    "   while making a PrintWriter for file:"+nl+
                    "   \""+outFileName+"\"";
          MsgExceptn.exception(msg);
          javax.swing.JOptionPane.showMessageDialog(this, msg, pc.progName,
                                    javax.swing.JOptionPane.ERROR_MESSAGE);
          if(outputPW != null) {outputPW.close();}
          return false;
      }
      System.out.println("Output file: "+outFileName);
      this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
      this.jButtonClose.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
      System.out.println("---- start scanning of databases, checking errors, calculating statistics...");
      // ----- scan the files
      java.text.DateFormat dateFormatter =
            java.text.DateFormat.getDateTimeInstance
                (java.text.DateFormat.DEFAULT, java.text.DateFormat.DEFAULT, java.util.Locale.getDefault());
      java.util.Date today = new java.util.Date();
      String dateOut = dateFormatter.format(today);
      outputPW.println("DataMaintenance (java) - "+dateOut+nl+
              "Statistics for LogK databases:"+nl+nl+"Database(s) for reactions:");
      java.io.File f;
      String name;
      java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.DEFAULT,
                                   java.text.DateFormat.SHORT);
      for(int i=0; i < pd.dataBasesList.size(); i++) {
        name = pd.dataBasesList.get(i).trim();
        if(name.length() >0) {
            f = new java.io.File(name);
            outputPW.println("  "+f.getAbsolutePath()+"   "+df.format(f.lastModified()));
        }
      }//for i
      outputPW.println(nl+"Element-reactant file(s):");
      for(int i=0; i < pd.dataBasesList.size(); i++) {
        name = pd.dataBasesList.get(i).trim();
        if(name.length() >0) {
            f = new java.io.File(Div.getFileNameWithoutExtension(name)+".elt");
            if(!f.exists()) {f = new java.io.File(Div.getFileNameWithoutExtension(name)+".elb");}
            if(f.exists()) {outputPW.println("  "+f.getAbsolutePath()+"   "+df.format(f.lastModified()));}
        }
      }//for i

      final CheckDatabases.CheckDataBasesLists lists = new CheckDatabases.CheckDataBasesLists();

      new javax.swing.SwingWorker<Void,Void>() {
      @Override protected Void doInBackground() throws Exception {
        if(pc.dbg) {System.out.println("---- doInBackground(),  dbg = "+pc.dbg);}
        CheckDatabases.checkDatabases(pc.dbg, Statistics.this,  pd.dataBasesList, pd.references, lists);
        if(finished) { // if the user closed the window
            System.out.println("---- startStatistics() - doInBackground() - cancelled by the user!");
            outputPW.flush(); outputPW.close(); return null;
        }

        java.util.ArrayList<String> arrayList;
        java.util.TreeSet<String> treeSet;
        outputPW.println(nl+"Total nbr reactions = "+lists.productsReactionsSet.size());
        outputPW.println("Total nbr reactants found in reaction databases = "+lists.reactantsSet.size()+nl+
                "      (nbr reactants in element-reactant files = "+lists.nbrCompsInElementFiles+")"+nl);
        if(lists.reactantsSet.size()>0) {
            outputPW.println(
                "Reactants found in reaction database(s):"+nl+
                "  Name     &    Nbr of reactions they participate in");
            treeSet = new java.util.TreeSet<String>(lists.reactantsSet.keySet());
            int j;
            for(String t : treeSet) {
                j = lists.reactantsSet.get(t);
                if(t.length() <=20) {
                    outputPW.format("  %-20s   %d", t, j);
                    outputPW.println();
                } else {
                    outputPW.format("  %s   %d",  t, j);
                    outputPW.println();
                }
            }
        }
        // --
        if(lists.reactantsUnknown.size() > 0) {
            outputPW.println(nl+"Error: reactants (components) in the reactions database(s) NOT found"+nl+
                            "   in the element-reactant file(s)."+nl+
                            "   Note that any reaction involving these components"+nl+
                            "   will NOT be found in a database search!");
            treeSet = new java.util.TreeSet<String>(lists.reactantsUnknown);
            for(String t : treeSet) {outputPW.println(" "+t);}
        }
        // --
        if(lists.reactantsNotUsed.size()>0) {
            outputPW.println(nl+"Warning: components in the element-reactant file(s)"+nl+
                                "   not used in the reactions database(s):");
            treeSet = new java.util.TreeSet<String>(lists.reactantsNotUsed);
            for(String t : treeSet) {outputPW.println(" "+t);}
        }
        // --
        if(lists.reactantsCompare.size()>0) {
            outputPW.println(nl+"Error: names of reactants in the database file(s)"+nl+
                    "   that are equivalent but will be treated as different:");
            java.util.Collections.sort(lists.reactantsCompare,String.CASE_INSENSITIVE_ORDER);
            for(String t : lists.reactantsCompare) {outputPW.println(" "+t);}
        }
        if(lists.elementReactantsCompare.size()>0) {
            outputPW.println(nl+"Error: names of reactants in the element-reactant file(s)"+nl+
                    "   that are equivalent but will be treated as different:");
            java.util.Collections.sort(lists.elementReactantsCompare,String.CASE_INSENSITIVE_ORDER);
            for(String t : lists.elementReactantsCompare) {outputPW.println(" "+t);}
        }
        // --
        if(lists.reactantWithoutCoef.size() >0) {
            outputPW.println(nl+"Error: reactions having a reactant with name but without its stoich.coeff:");
            treeSet = new java.util.TreeSet<String>(lists.reactantWithoutCoef);
            for(String t : treeSet) {outputPW.println(" "+t);}
        }
        if(lists.coefWithoutReactant.size() >0) {
            outputPW.println(nl+"Error: reactions having a stoich.coeff with no reactant:");
            treeSet = new java.util.TreeSet<String>(lists.coefWithoutReactant);
            for(String t : treeSet) {outputPW.println(" "+t);}
        }
        if(lists.chargeImbalance.size() >0) {
            outputPW.println(nl+"Error: reactions with charge imbalance:");
            treeSet = new java.util.TreeSet<String>(lists.chargeImbalance);
            for(String t : treeSet) {outputPW.println(" "+t);}
        }
        if(lists.duplReactionsSameProdctSet.size() > 0) {
            outputPW.println(nl+"Warning: reactions found more than once"+nl+
                                  "    (with the same product):");
            for(String t : lists.duplReactionsSameProdctSet) {outputPW.println(" "+t);}
        }
        if(lists.duplReactionsDifProductSet.size() > 0) {
            outputPW.println(nl+"Warning: reactions found more than once"+nl+
                                  "    (with a different product):");
            for(String t : lists.duplReactionsDifProductSet) {outputPW.println(" "+t);}
        }
        if(lists.duplProductsSet.size() > 0) {
            outputPW.println(nl+"Warning: reaction products found more than once"+nl+
                                  "    (with a different reaction):");
            for(String t : lists.duplProductsSet) {outputPW.println(" "+t);}
        }
        if(lists.duplSolidsSet.size() > 0) {
            outputPW.println(nl+"Note: solids found more than once"+nl+
                                  "    (with different phase designator)");
            for(String t : lists.duplSolidsSet) {outputPW.println(" "+t);}
        }        
        if(lists.itemsNames.size() >0) {
            java.util.Collections.sort(lists.itemsNames,String.CASE_INSENSITIVE_ORDER);
            outputPW.println(nl+"Error? reaction products where the name"+nl+
                                  "    does not contain one or more reactant(s):");
            for(String t : lists.itemsNames) {outputPW.println(" "+t);}
        }
        if(finished) { // if the user closed the window
            System.out.println("---- startStatistics() - doInBackground() - cancelled by the user!");
            outputPW.flush(); outputPW.close(); return null;
        }

        // -- References
        if(pd.references == null) {
            outputPW.println(nl+"No reference file found.");
        } else {
            if(lists.refsNotFnd != null && !lists.refsNotFnd.isEmpty()) {
                outputPW.println(nl+"Error: citations with no references:");
                java.util.Collections.sort(lists.refsNotFnd,String.CASE_INSENSITIVE_ORDER);
                for(String t : lists.refsNotFnd) {outputPW.println(" "+t);}
            }
            if(lists.refsFnd != null && !lists.refsFnd.isEmpty()) {
                boolean ok;
                arrayList = new java.util.ArrayList<String>();
                for (Object k : pd.references.referenceKeys()) {
                    if(k != null) {
                        ok = false;
                        String t = k.toString().trim();
                        for(String t2 : lists.refsFnd) {
                            if(t2 != null && t2.equalsIgnoreCase(t)) {ok = true; break;}
                        }
                        if(!ok) {arrayList.add(t);}
                    }
                } // for Object k
                if(arrayList.size() >0) {
                    java.util.Collections.sort(arrayList,String.CASE_INSENSITIVE_ORDER);
                    outputPW.println(nl+"Warning: references not used in the database(s):");
                    for(String t : arrayList) {outputPW.println(" "+t);}
                }
            }
        }// references

        // -- close
        outputPW.flush();
        outputPW.close();
        if(finished) { // if the user closed the window
            System.out.println("---- startStatistics() - doInBackground() - cancelled by the user!");
            return null;
        }

        if(pc.dbg) {System.out.println("\"doInBackground()\" finished.");}
        return null;
      } // doInBackground()
      @Override protected void done(){
        // ---- The end
        if(finished) { // if the user closed the window
            System.out.println("---- startStatistics() - doInBackground() - cancelled by the user!");
            return;
        }
        Statistics.this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setPanelsEnabled(true);

        jLabelNaFiles.setText(String.valueOf(lists.nbrCompsInElementFiles));
        jLabelN.setText(String.valueOf(lists.productsReactionsSet.size()));

        for (String r : lists.reactantsSet.keySet()) {
            sortedModelCompsFnd.add(r);
        }
        jLabelNa.setText(String.valueOf(lists.reactantsSet.size()));
        for (String r : lists.reactantWithoutCoef) {
            sortedModelNameWoutNumber.add(r);
        }
        for (String r : lists.coefWithoutReactant) {
            sortedModelNumberWoutName.add(r);
        }
        for (String r : lists.chargeImbalance) {
            sortedModelChargeError.add(r);
        }
        for (String r : lists.reactantsUnknown) {
            sortedModelCompsUnknown.add(r);
        }

        jButtonClose.setEnabled(true);
        jButtonClose.requestFocusInWindow();
        String msg = "Written file:"+nl+"   \""+outFileName+"\"";
        System.out.println(msg+nl+"\"Statistics\" finished.");
        javax.swing.JOptionPane.showMessageDialog(Statistics.this,msg,
                    pc.progName,javax.swing.JOptionPane.INFORMATION_MESSAGE);
      } // done()
      }.execute(); // SwingWorker
      return true; // this returns inmediately
  } //startStatistics
//</editor-fold>

//</editor-fold>


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonClose;
    private javax.swing.JCheckBox jCheckBoxDebugFrame;
    private javax.swing.JLabel jLabelChargeError;
    private javax.swing.JLabel jLabelComps;
    private javax.swing.JLabel jLabelCompsUnknown;
    private javax.swing.JLabel jLabelErr;
    private javax.swing.JLabel jLabelN;
    private javax.swing.JLabel jLabelNa;
    private javax.swing.JLabel jLabelNaFiles;
    private javax.swing.JLabel jLabelNameWoutNumb;
    private javax.swing.JLabel jLabelNumbWoutName;
    private javax.swing.JLabel jLabelTot;
    private javax.swing.JLabel jLabelTotCmplx;
    private javax.swing.JLabel jLabelTotComps;
    private javax.swing.JLabel jLabelTotCompsFiles;
    private javax.swing.JLabel jLabelWait;
    private javax.swing.JList jListChargeError;
    private javax.swing.JList jListComps;
    private javax.swing.JList jListCompsUnk;
    private javax.swing.JList jListNameWoutNumb;
    private javax.swing.JList jListNumbWoutName;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanelCheckBox;
    private javax.swing.JPanel jPanelErrs;
    private javax.swing.JPanel jPanelTot;
    private javax.swing.JScrollPane jScrollChargeError;
    private javax.swing.JScrollPane jScrollCompsUnk;
    private javax.swing.JScrollPane jScrollNameWoutNumb;
    private javax.swing.JScrollPane jScrollNumbWoutName;
    private javax.swing.JScrollPane jScrollPaneComps;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
}
