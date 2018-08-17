package dataMaintenance;

import java.io.IOException;
import lib.common.MsgExceptn;
import lib.common.Util;
import lib.database.*;
import lib.huvud.Div;
import lib.huvud.ProgramConf;
import lib.huvud.RedirectedFrame;
import lib.huvud.SortedProperties;

/** The main frame.
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
public class DataMaintenance extends javax.swing.JFrame {
  private static final String VERS = "2018-July-25";
  /** all instances will use the same redirected frame */
  private static RedirectedFrame msgFrame = null;
  private final ProgramDataDB pd = new ProgramDataDB();
  private final ProgramConf pc;
  private java.awt.Dimension windowSize = new java.awt.Dimension(390,230);
  // private final javax.swing.DefaultListModel modelFiles = new javax.swing.DefaultListModel(); // java 1.6
  private final javax.swing.DefaultListModel<String> modelFiles = new javax.swing.DefaultListModel<>();
  private static final String DEF_DataBase = "Reactions.db";
  private static java.io.File fileIni;
  private static final String FileINI_NAME = ".DataMaintenance.ini";
  private boolean working = false;
  /** a counter indicating how many reactions have been read so far */
  private long cmplxNbr = 0;
  private final double F_TXT_CMPLX = 54.15; //=186863/3451
  private final double F_BIN_ELEM = 38.64;  //=3323/86  
  private final double F_BIN_CMPLX =123.39; //=425835/3451  
  /** the length in bytes of the file being read */
  private double fLength;
  /** the complexes and solid products found in the database search */
  private final java.util.Set<Complex> dataList = new java.util.TreeSet<Complex>();

  private final java.awt.Color frgrnd;
  private final java.awt.Color bckgrnd;
  private boolean finished = false;
  private FrameAddData addData = null;

  private final javax.swing.border.Border defBorder;
  private final javax.swing.border.Border highlightedBorder = javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED, java.awt.Color.gray, java.awt.Color.black);
  // variables used when dealing with command-line args.
  private boolean doNotExit = false;
  private boolean dispatchingArgs = false;
  private String fileCmplxSaveName = null;

  /** New-line character(s) to substitute "\n" */
  private static final String nl = System.getProperty("line.separator");
  private static final String SLASH = java.io.File.separator;
  static final String LINE = "- - - - - - - - - - - - - - - - - - - - - - - - - - -";

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /** Creates new form DataMaintenance
   * @param pc0  */
  public DataMaintenance(ProgramConf pc0) {
    initComponents();
    this.pc = pc0;
    this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
    frgrnd = jButtonExit.getForeground();
    bckgrnd = jButtonExit.getBackground().darker();
    
    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    //--- Close window on ESC key
    javax.swing.KeyStroke escKeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(escKeyStroke,"ESCAPE");
    javax.swing.Action escAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            closeWindow();
        }};
    getRootPane().getActionMap().put("ESCAPE", escAction);
    //--- Alt-Q Quit
    javax.swing.KeyStroke altQKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altQKeyStroke,"ALT_Q");
    getRootPane().getActionMap().put("ALT_Q", escAction);
    //--- Alt-X eXit
    javax.swing.KeyStroke altXKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altXKeyStroke,"ALT_X");
    getRootPane().getActionMap().put("ALT_X", escAction);
    //--- Alt-S show debug frame
    javax.swing.KeyStroke altSKeyStroke = javax.swing.KeyStroke.getKeyStroke(
          java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altSKeyStroke,"ALT_S");
    javax.swing.Action altSAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jCheckBoxDebugFrame.doClick();
            }};
    getRootPane().getActionMap().put("ALT_S", altSAction);
    //--- F1 for help
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
    setFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,newKeys);

    keys = getFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
    newKeys = new java.util.HashSet<java.awt.AWTKeyStroke>(keys);
    newKeys.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0));
    setFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,newKeys);

    defBorder = jScrollPaneDBlist.getBorder();

    //---- Position the window on the screen
    java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    java.awt.Point frameLocation = new java.awt.Point(-1000,-1000);
    frameLocation.x = Math.max(0, (screenSize.width  - this.getWidth() ) / 2);
    frameLocation.y = Math.max(0, (screenSize.height - this.getHeight() ) / 2);
    this.setLocation(frameLocation);
    //---- Icon
    String iconName = "images/Data.gif";
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
    //---- Title, menus, etc
    this.setTitle("Data Maintenance");

    jButtonCancel.setEnabled(false);

    jLabelSession.setText(" ");
    jLabelVersion.setText("vers. "+VERS);

  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="start(args)">
   /** Performs start-up actions that require an "object" of this class to be
   * present, for example actions that may display a message dialog box
   * (because a dialog requires a parent frame).
   * @param args the command-line arguments
   */
  private void start(final String[] args) {
    this.setVisible(true);
    if(msgFrame != null) {
        pd.msgFrame = msgFrame;
        pd.msgFrame.setParentFrame(this);
        if(pd.msgFrame.isVisible()) {jCheckBoxDebugFrame.setSelected(true);}
    } else {
        jCheckBoxDebugFrame.setEnabled(false);
    }
    // ----- read the list of database files
    readIni();
    for(int i=0; i < pd.dataBasesList.size(); i++) {
        modelFiles.addElement(pd.dataBasesList.get(i));
    }
    LibDB.getElements(this, pc.dbg, pd.dataBasesList, pd.elemComp);
    setFrameEnabled(true);
    windowSize = this.getSize();

    pd.references = new References(false);
    String r;
    String dir = pc.pathAPP;
    if(dir != null) {
        if(dir.endsWith(SLASH)) {dir = dir.substring(0, dir.length()-1);}
        r = dir + SLASH + "References.txt";
    } else {r = "References.txt";}
    if(!pd.references.readRefsFile(r, pc.dbg)) {pd.references = null;}

    if(pc.pathAPP != null && pc.pathAPP.trim().length()>0) {
        pd.pathDatabaseFiles.replace(0,pd.pathDatabaseFiles.length(),pc.pathAPP);
    } else {pd.pathDatabaseFiles.replace(0,pd.pathDatabaseFiles.length(),".");}

    System.out.println(LINE);
    System.out.println("User's home directory: "+System.getProperty("user.home"));
    System.out.println("User's current working directory: "+System.getProperty("user.dir"));
    System.out.print("Application path: ");
    if(pc.pathAPP == null) {
        System.out.print("\"null\"");
    } else {
        if(pc.pathAPP.trim().length()<=0) {System.out.print("\"\"");}
        else {System.out.print(pc.pathAPP);}
    } System.out.println();
    System.out.print("Add-data path: ");
    if(pd.pathAddData == null) {
        System.out.print("\"null\"");
    } else {
        if(pd.pathAddData.toString().trim().length()<=0) {System.out.print("\"\"");}
        else {System.out.print(pd.pathAddData);}
    }
    System.out.println("Default path: "+pc.pathDef.toString());
    System.out.println(nl+LINE);

    //---- deal with command-line arguments
    if(args != null && args.length >0){
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        Thread dArg = new Thread() {@Override public void run(){
            for(String arg : args) {
                dispatchingArgs = true;
                dispatchArg(arg);
                dispatchingArgs = false;
            }
            // do not end the program if an error occurred
            if(msgFrame == null || !msgFrame.isVisible() && !doNotExit) {closeWindow();}
        }}; //new Thread
        dArg.start();
    } // args != null
    this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
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

        jButtonOpen = new javax.swing.JButton();
        jButtonSave = new javax.swing.JButton();
        jButtonExit = new javax.swing.JButton();
        jButtonHelp = new javax.swing.JButton();
        jLabelSession = new javax.swing.JLabel();
        jLabelDataBases = new javax.swing.JLabel();
        jScrollPaneDBlist = new javax.swing.JScrollPane();
        jListDBlist = new javax.swing.JList();
        jPanelButtons = new javax.swing.JPanel();
        jButtonStatistics = new javax.swing.JButton();
        jButtonAddData = new javax.swing.JButton();
        jButton2text = new javax.swing.JButton();
        jButton2binary = new javax.swing.JButton();
        jButtonSingle = new javax.swing.JButton();
        jPanelShow = new javax.swing.JPanel();
        jCheckBoxDebugFrame = new javax.swing.JCheckBox();
        jPanelOutput = new javax.swing.JPanel();
        jProgressBar = new javax.swing.JProgressBar();
        jButtonCancel = new javax.swing.JButton();
        jLabelNbr = new javax.swing.JLabel();
        jLabelVersion = new javax.swing.JLabel();

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

        jButtonOpen.setMnemonic('o');
        jButtonOpen.setText("<html><center>Open<br>session</center></html>");
        jButtonOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenActionPerformed(evt);
            }
        });

        jButtonSave.setMnemonic('v');
        jButtonSave.setText("<html><center>Save<br>session</center></html>");
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });

        jButtonExit.setMnemonic('e');
        jButtonExit.setText("Exit");
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        jButtonHelp.setMnemonic('h');
        jButtonHelp.setText("Help");
        jButtonHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHelpActionPerformed(evt);
            }
        });

        jLabelSession.setText("<html><b>Session:</b> DataBaseMaintenance</html>");

        jLabelDataBases.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelDataBases.setLabelFor(jScrollPaneDBlist);
        jLabelDataBases.setText("Databases:");

        jListDBlist.setModel(modelFiles);
        jListDBlist.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListDBlistMouseClicked(evt);
            }
        });
        jListDBlist.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jListDBlistFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jListDBlistFocusLost(evt);
            }
        });
        jListDBlist.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jListDBlistKeyTyped(evt);
            }
        });
        jScrollPaneDBlist.setViewportView(jListDBlist);

        jButtonStatistics.setMnemonic('c');
        jButtonStatistics.setText("<html><center>statistics<br>and<br>Checks</center></html>");
        jButtonStatistics.setMargin(new java.awt.Insets(2, 7, 2, 7));
        jButtonStatistics.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStatisticsActionPerformed(evt);
            }
        });

        jButtonAddData.setMnemonic('a');
        jButtonAddData.setText("<html><center>Add data<br>or edit<br>a text-file</center></html>");
        jButtonAddData.setMargin(new java.awt.Insets(2, 7, 2, 7));
        jButtonAddData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddDataActionPerformed(evt);
            }
        });

        jButton2text.setMnemonic('t');
        jButton2text.setText("<html><center>convert binary<br>databases<br>to Text</center></html>");
        jButton2text.setMargin(new java.awt.Insets(2, 7, 2, 7));
        jButton2text.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2textActionPerformed(evt);
            }
        });

        jButton2binary.setMnemonic('b');
        jButton2binary.setText("<html><center>merge all<br>databases to<br>a single Binary</center></html>");
        jButton2binary.setMargin(new java.awt.Insets(2, 7, 2, 7));
        jButton2binary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2binaryActionPerformed(evt);
            }
        });

        jButtonSingle.setText("<html><center>show Data<br>for a single<br>component</center></html>");
        jButtonSingle.setMargin(new java.awt.Insets(2, 7, 2, 7));
        jButtonSingle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSingleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelButtonsLayout = new javax.swing.GroupLayout(jPanelButtons);
        jPanelButtons.setLayout(jPanelButtonsLayout);
        jPanelButtonsLayout.setHorizontalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addComponent(jButtonStatistics, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jButtonAddData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton2text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton2binary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonSingle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelButtonsLayout.setVerticalGroup(
            jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelButtonsLayout.createSequentialGroup()
                .addGroup(jPanelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2binary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonAddData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonStatistics, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonSingle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jCheckBoxDebugFrame.setMnemonic('s');
        jCheckBoxDebugFrame.setText("show messages window");
        jCheckBoxDebugFrame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxDebugFrameActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelShowLayout = new javax.swing.GroupLayout(jPanelShow);
        jPanelShow.setLayout(jPanelShowLayout);
        jPanelShowLayout.setHorizontalGroup(
            jPanelShowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelShowLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBoxDebugFrame)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelShowLayout.setVerticalGroup(
            jPanelShowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jCheckBoxDebugFrame)
        );

        jProgressBar.setForeground(java.awt.Color.blue);

        jButtonCancel.setMnemonic('c');
        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        jLabelNbr.setText("0000");

        javax.swing.GroupLayout jPanelOutputLayout = new javax.swing.GroupLayout(jPanelOutput);
        jPanelOutput.setLayout(jPanelOutputLayout);
        jPanelOutputLayout.setHorizontalGroup(
            jPanelOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelOutputLayout.createSequentialGroup()
                .addComponent(jButtonCancel)
                .addGap(31, 31, 31)
                .addComponent(jProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabelNbr)
                .addContainerGap(186, Short.MAX_VALUE))
        );
        jPanelOutputLayout.setVerticalGroup(
            jPanelOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabelNbr, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButtonCancel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanelOutputLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addGap(6, 6, 6))
        );

        jLabelVersion.setText("vers. 2014-Aug-31");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelShow, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelOutput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPaneDBlist)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelDataBases)
                            .addComponent(jLabelSession, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButtonOpen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(38, 38, 38)
                                .addComponent(jButtonExit)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButtonHelp)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabelVersion)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonOpen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonExit)
                    .addComponent(jButtonHelp))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelSession, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(jLabelDataBases)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneDBlist, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelShow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jLabelVersion))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

//<editor-fold defaultstate="collapsed" desc="Events">

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
      if(windowSize != null) {
        int w = Math.round((float)windowSize.getWidth());
        int h = Math.round((float)windowSize.getHeight());
        if(this.getHeight()<h){this.setSize(this.getWidth(), h);}
        if(this.getWidth()<w){this.setSize(w,this.getHeight());}
      }
    }//GEN-LAST:event_formComponentResized

    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
        closeWindow();
    }//GEN-LAST:event_jButtonExitActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeWindow();
    }//GEN-LAST:event_formWindowClosing

    private void jCheckBoxDebugFrameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDebugFrameActionPerformed
        if(pd.msgFrame != null) {
            pd.msgFrame.setVisible(jCheckBoxDebugFrame.isSelected());
            pd.msgFrame.setParentFrame(this);
        }
    }//GEN-LAST:event_jCheckBoxDebugFrameActionPerformed

    private void jButton2binaryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2binaryActionPerformed
        toBinary(pd.elemComp);
    }//GEN-LAST:event_jButton2binaryActionPerformed

    private void jButton2textActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2textActionPerformed
        toText();
    }//GEN-LAST:event_jButton2textActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        if(!working) {
            jButtonCancel.setEnabled(false);
        } else {
            Object[] opt = {"Yes, cancel", "Continue"};
            int m = javax.swing.JOptionPane.showOptionDialog(this,
                "Cancel job?"+nl+"Are you sure?"+nl+" ",
                pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
            if(m != javax.swing.JOptionPane.YES_OPTION) {return;}
            working = false;
            System.err.println("--- Cancelled by the user"+nl+nl+
                    "--- Close this window to terminate the program ---");
        }
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jListDBlistMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListDBlistMouseClicked
      //if(evt == null || evt.getClickCount() >=2) {
        setCursorWait();
        DBnamesDialog dbND = new DBnamesDialog(this, true,
                pc, pd.dataBasesList, pd.pathDatabaseFiles, pd.elemComp);
        setFrameEnabled(false);
        dbND.setVisible(true); //this will wait for the modal dialog to close
        if(!dbND.cancel) {
            LibDB.checkDataBasesList(this, pd.dataBasesList, true);
            //---- read the elements/components for the databases
            LibDB.getElements(this, pc.dbg, pd.dataBasesList, pd.elemComp);
            //---- 
            modelFiles.clear();
            for(int i=0; i < pd.dataBasesList.size(); i++) {modelFiles.addElement(pd.dataBasesList.get(i));}
        }
        setFrameEnabled(true);
        dbND.dispose();
        System.out.println("----  pathAddData = "+pd.pathAddData.toString());
        //this.setVisible(true);
        setCursorDef();
        this.bringToFront();
      //} //if(evt.getClickCount() >=2)
    }//GEN-LAST:event_jListDBlistMouseClicked

    private void jListDBlistKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jListDBlistKeyTyped
        if(evt.getKeyChar() == java.awt.event.KeyEvent.VK_DELETE ||
                evt.getKeyChar() == java.awt.event.KeyEvent.VK_BACK_SPACE) {
            evt.consume();
            jListDBlistMouseClicked(null);
            return;
        }
        char c = Character.toUpperCase(evt.getKeyChar());
        if(evt.getKeyChar() != java.awt.event.KeyEvent.VK_ESCAPE &&
           evt.getKeyChar() != java.awt.event.KeyEvent.VK_ENTER &&
           !(evt.isAltDown() && ((c == 'X') || (c == 'S'))
                 ) //isAltDown
                 ) { // if not ESC or Alt-something
                evt.consume(); // remove the typed key
                jListDBlistMouseClicked(null);
        } // if char ok
    }//GEN-LAST:event_jListDBlistKeyTyped

    private void jButtonStatisticsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStatisticsActionPerformed
      setCursorWait();
      this.setVisible(false);
      new javax.swing.SwingWorker<Void,Void>() {
          @Override protected Void doInBackground() throws Exception {
            Statistics s = new Statistics(pc, pd, DataMaintenance.this);
            s.start();
            s.waitFor();
            return null;
          }
          @Override protected void done(){
            bringToFront();
            pd.msgFrame.setParentFrame(DataMaintenance.this);
            setCursorDef();
          }
      }.execute();
    }//GEN-LAST:event_jButtonStatisticsActionPerformed

    private void jListDBlistFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListDBlistFocusGained
        if(jListDBlist.isFocusOwner()) {jScrollPaneDBlist.setBorder(highlightedBorder);}
        if(modelFiles.getSize()>0) {
          int i = jListDBlist.getSelectedIndex();
          if(i>=0) {
              jListDBlist.ensureIndexIsVisible(i);
          } else {
              jListDBlist.setSelectedIndex(0);
          }
        }
    }//GEN-LAST:event_jListDBlistFocusGained

    private void jListDBlistFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListDBlistFocusLost
        jListDBlist.clearSelection();
        if(!jListDBlist.isFocusOwner()) {jScrollPaneDBlist.setBorder(defBorder);}        
    }//GEN-LAST:event_jListDBlistFocusLost

    private void jButtonOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenActionPerformed
        String fn = Util.getOpenFileName(this, pc.progName, true, "Open INI file",
                11, null, pc.pathDef.toString());
        if(fn == null || fn.length() <=0) {return;}
        java.io.File f = new java.io.File(fn);
        try {fn = f.getCanonicalPath();} catch (java.io.IOException ex) {fn = null;}
        if(fn == null) {
              try {fn = f.getAbsolutePath();} catch (Exception ex) {fn = f.getPath();}}
        f = new java.io.File(fn);
        pc.setPathDef(f);
        modelFiles.clear();
        readIni2(f);
        for(int i=0; i < pd.dataBasesList.size(); i++) {modelFiles.addElement(pd.dataBasesList.get(i));}
        setFrameEnabled(true);
        jLabelSession.setText("<html><b>Session:</b>&nbsp; \""+f.getName()+"\"</html>");
        setCursorDef();
    }//GEN-LAST:event_jButtonOpenActionPerformed

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
        String fn = Util.getSaveFileName(this, pc.progName, "Open INI file",
                11, null, pc.pathDef.toString());
        if(fn == null || fn.length() <=0) {return;}
        java.io.File f = new java.io.File(fn);
        try {fn = f.getCanonicalPath();} catch (java.io.IOException ex) {fn = null;}
        if(fn == null) {
              try {fn = f.getAbsolutePath();} catch (Exception ex) {fn = f.getPath();}}
        f = new java.io.File(fn);
        pc.setPathDef(f);
        saveIni(f);
        jLabelSession.setText("<html><b>Session:</b>&nbsp; \""+f.getName()+"\"</html>");
    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jButtonAddDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddDataActionPerformed
      if(addData != null) {MsgExceptn.exception("Programming error: addData != null"); return;}
      setCursorWait();
      jButtonAddData.setEnabled(false);
      // ---- Going to wait for another frame: Start a thread
      new javax.swing.SwingWorker<Void,Void>() {
          @Override protected Void doInBackground() throws Exception {
            addData = new FrameAddData(pc, pd, DataMaintenance.this);
            DataMaintenance.this.setVisible(false);
            addData.start();
            addData.waitFor();
            return null;
          } //doInBackground()
          @Override protected void done(){
            addData = null;
            setFrameEnabled(true);
            bringToFront();
            setCursorDef();
          } //done()
      }.execute();
    }//GEN-LAST:event_jButtonAddDataActionPerformed

    private void jButtonSingleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSingleActionPerformed
        setCursorWait();
        jButtonSingle.setEnabled(false);
      // ---- Going to wait for another frame: Start a thread
      new javax.swing.SwingWorker<Void,Void>() {
          @Override protected Void doInBackground() throws Exception {
            LibDB.getElements(DataMaintenance.this, pc.dbg, pd.dataBasesList, pd.elemComp);
            FrameSingleComponent sc = new FrameSingleComponent(DataMaintenance.this, pc, pd);
            setCursorDef();
            DataMaintenance.this.setVisible(false);
            sc.start();
            sc.waitFor();
            return null;
          }
          @Override protected void done(){
            setFrameEnabled(true);
            bringToFront();
          }
      }.execute();
    }//GEN-LAST:event_jButtonSingleActionPerformed

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        if(pd.msgFrame != null) {
            jCheckBoxDebugFrame.setSelected(pd.msgFrame.isVisible());
        } else {
            jCheckBoxDebugFrame.setEnabled(false);
        }
    }//GEN-LAST:event_formWindowGainedFocus

    private void jButtonHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHelpActionPerformed
        setCursorWait();
        Thread hlp = new Thread() {@Override public void run(){
            String[] a = {"DB_Databases_htm"};
            lib.huvud.RunProgr.runProgramInProcess(null,ProgramConf.HELP_JAR,a,false,pc.dbg,pc.pathAPP);
            try{Thread.sleep(1500);}   //show the "wait" cursor for 1.5 sec
            catch (InterruptedException e) {}
            setCursorDef();
        }};//new Thread
        hlp.start();
    }//GEN-LAST:event_jButtonHelpActionPerformed

//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Methods">

  private void closeWindow() {
    if(fileIni != null) {saveIni(fileIni);}
    finished = true;    //return from "waitFor()"
    this.notify_All();
    this.dispose();
    System.exit(0);
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
  // </editor-fold>

 //<editor-fold defaultstate="collapsed" desc="setFrameEnabled(enable)">
 /** Sets buttons enabled - disabled
  * @param enable 
  */
  private void setFrameEnabled(boolean enable) {
    jButtonExit.setEnabled(enable);
    jButtonOpen.setEnabled(enable);
    jButtonAddData.setEnabled(enable);
    jButtonCancel.setEnabled(!enable);
    boolean b = enable;
    if(pd.dataBasesList.size() <=0) {b = false;}
    jButtonSave.setEnabled(b);
    jButtonStatistics.setEnabled(b);
    jButton2binary.setEnabled(b);
    jButton2text.setEnabled(b);
    jButtonSingle.setEnabled(b);
    if(b) {
        jButtonSave.setForeground(frgrnd);
        jButtonStatistics.setForeground(frgrnd);
        jButton2binary.setForeground(frgrnd);
        jButton2text.setForeground(frgrnd);
        jButtonSingle.setForeground(frgrnd);
    } else {
        jButtonSave.setForeground(bckgrnd);
        jButtonStatistics.setForeground(bckgrnd);
        jButton2binary.setForeground(bckgrnd);
        jButton2text.setForeground(bckgrnd);
        jButtonSingle.setForeground(bckgrnd);
    }
    if(enable) {
        jButtonOpen.setForeground(frgrnd);
        jButtonAddData.setForeground(frgrnd);
        jLabelNbr.setVisible(false);
        jProgressBar.setVisible(false);
        jButtonOpen.requestFocusInWindow();
        enable2Text();
        setCursorDef();
    } else {
        jButtonOpen.setForeground(bckgrnd);
        jButtonAddData.setForeground(bckgrnd);
        jButtonCancel.requestFocusInWindow();
        setCursorWait();
    }
  } //setFrameEnabled(enable)
  // </editor-fold>

 //<editor-fold defaultstate="collapsed" desc="enable2Text">
  /** enable jButton2text to convert binary files to text format */
  private void enable2Text() {
    boolean found = false;
    for(int i = 0; i < pd.dataBasesList.size(); i++) {
      if(Div.getFileNameExtension(pd.dataBasesList.get(i)).equalsIgnoreCase("db")) {
          found = true; break;
      }
    }//for i
    if(!found) {
        jButton2text.setEnabled(false);
        jButton2text.setForeground(bckgrnd);
    } else {
        jButton2text.setEnabled(true);
        jButton2text.setForeground(frgrnd);
    }
  } //enable2Text()
  // </editor-fold>

 //<editor-fold defaultstate="collapsed" desc="setCursorWait and setCursorDef">
  public void setCursorWait() {
    this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
    if(pd.msgFrame != null && pd.msgFrame.isShowing()) {pd.msgFrame.setCursorWait();}
  }
  public void setCursorDef() {
    this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    if(pd.msgFrame != null) {pd.msgFrame.setCursorDef();}
  }
// </editor-fold>

 //<editor-fold defaultstate="collapsed" desc="readIni - writeINI">

  /** Reads program settings saved when the program was previously closed.
   * Exceptions are reported both to the console (if there is one) and to a dialog.<br>
   * Reads the ini-file in:<ul>
   *   <li> the Application Path if found there.</ul>
   * If not found in the application path, or if the file is write-protected, then:<ul>
   *   <li> in %HomeDrive%%HomePath% if found there; if write-protected also
   *   <li> in %Home% if found there; if write-protected also
   *   <li> in the user's home directory (system dependent) if it is found there
   * otherwise: give a warning and create a new file.  Note: except for the
   * installation directory, the ini-file will be writen in a sub-folder
   * named "<code>.config\eq-diag</code>".
   * <p>
   * This method also saves the ini-file in the application path if
   * "saveIniFileToApplicationPathOnly" is <code>true</code>.  Otherwise,
   * if an ini-file was read and if it was not write-protected, then program
   * options are saved in that file on exit.  If no ini-file was found,
   * an ini file is created on the first non-write protected directory of
   * those listed above.  */

  private void readIni() {
    // start by getting the defaults (this is needed because the arrays must be initialised)
    iniDefaults();  // needed to initialise arrays etc.
    if(pc.dbg) {System.out.println("--- readIni() ---  reading ini-file(s)");}
    fileIni = null;
    java.io.File p = null, fileRead = null, fileINInotRO = null;
    boolean ok, readOk = false;
    //--- check the application path ---//
    if(pc.pathAPP == null || pc.pathAPP.trim().length() <=0) {
        if(pc.saveIniFileToApplicationPathOnly) {
            String name = "\"null\"" + SLASH + FileINI_NAME;
            MsgExceptn.exception("Error: can not read ini file"+nl+
                        "    "+name+nl+
                        "    (application path is \"null\")");
            return;
        }
    } else { //pathApp is defined
        String dir = pc.pathAPP;
        if(dir.endsWith(SLASH)) {dir = dir.substring(0, dir.length()-1);}
        fileIni = new java.io.File(dir + SLASH + FileINI_NAME);
        p = new java.io.File(dir);
        if(!p.exists()) {
            p = null; fileIni = null;
            if(pc.saveIniFileToApplicationPathOnly) {
                MsgExceptn.exception("Error: can not read ini file:"+nl+
                            "    "+fileIni.getPath()+nl+
                            "    (application path does not exist)");
                return;
            }
        }
    }
    success: {
        // --- first read the ini-file from the application path, if possible
        if(pc.saveIniFileToApplicationPathOnly && fileIni != null) {
            // If the ini-file must be written to the application path,
            // then try to read this file, even if the file is write-protected
            fileINInotRO = fileIni;
            if(fileIni.exists()) {
                readOk = readIni2(fileIni);
                if(readOk) {fileRead = fileIni;}
            }
            break success;
        } else { // not saveIniFileToApplicationPathOnly or fileINI does not exist
            if(fileIni != null && fileIni.exists()) {
                readOk = readIni2(fileIni);
                if(readOk) {fileRead = fileIni;}
                if(fileIni.canWrite() && fileIni.setWritable(true)) {
                    fileINInotRO = fileIni;
                    if(readOk) {break success;}
                }
            } else { //ini-file null or does not exist
                if(fileIni != null && p != null) {
                    try{ // can we can write to this directory?
                            java.io.File tmp = java.io.File.createTempFile("datbm",".tmp", p);
                            ok = tmp.exists();
                            if(ok) {tmp.delete();}
                    } catch (java.io.IOException ex) {ok = false;}
                    if(pc.dbg) {
                        String s; if(ok) {s="";} else {s="NOT ";}
                        System.out.println("   can "+s+"write files to path: "+p.getAbsolutePath());
                    }
                    // file does not exist, but the path is not write-protected
                    if(ok && fileINInotRO == null) {fileINInotRO = fileIni;}
                }
            }
        }
        // --- an ini-file has not been read in the application path
        //     and saveIniFileToApplicationPathOnly = false.  Read the ini-file from
        //     the user's path, if possible
        java.util.ArrayList<String> dirs = new java.util.ArrayList<String>(5);
        String homeDrv = System.getenv("HOMEDRIVE");
        String homePath = System.getenv("HOMEPATH");
        if(homePath != null && homePath.trim().length() >0 && !homePath.startsWith(SLASH)) {
            homePath = SLASH + homePath;
        }
        if(homeDrv != null && homeDrv.trim().length() >0 && homeDrv.endsWith(SLASH)) {
            homeDrv = homeDrv.substring(0, homeDrv.length()-1);
        }
        if((homeDrv != null && homeDrv.trim().length() >0)
                && (homePath != null && homePath.trim().length() >0)) {
            p = new java.io.File(homeDrv+homePath);
            if(p.exists()) {dirs.add(p.getAbsolutePath());}
        }
        String home = System.getenv("HOME");
        if(home != null && home.trim().length() >0) {
            p = new java.io.File(home);
            if(p.exists()) {dirs.add(p.getAbsolutePath());}
        }
        home = System.getProperty("user.home");
        if(home != null && home.trim().length() >0) {
            p = new java.io.File(home);
            if(p.exists()) {dirs.add(p.getAbsolutePath());}
        }        
        for(String t : dirs) {
            if(t.endsWith(SLASH)) {t = t.substring(0, t.length()-1);}
            fileIni = new java.io.File(t+SLASH+".config"+SLASH+"eq-diagr"+SLASH+FileINI_NAME);
            if(fileIni.exists()) {
                readOk = readIni2(fileIni);
                if(readOk) {fileRead = fileIni;}
                if(fileIni.canWrite() && fileIni.setWritable(true)) {
                    if(fileINInotRO == null) {fileINInotRO = fileIni;}
                    if(readOk) {break success;}
                }
            } else { //ini-file does not exist
                try{ // can we can write to this directory?
                    p =  new java.io.File(t);
                    java.io.File tmp = java.io.File.createTempFile("datbm",".tmp", p);
                    ok = tmp.exists();
                    if(ok) {tmp.delete();}
                } catch (java.io.IOException ex) {ok = false;}
                if(pc.dbg) {
                    String s; if(ok) {s="";} else {s="NOT ";}
                    System.out.println("   can "+s+"write files to path: "+t);
                }
                // file does not exist, but the path is not write-protected
                if(ok && fileINInotRO == null) {fileINInotRO = fileIni;}
            }
        } // for(dirs)
    } //--- success?

    if(pc.dbg) {
        String s;
        if(fileINInotRO != null) {s=fileINInotRO.getAbsolutePath();} else {s="\"null\"";}
        System.out.println("   fileINInotRO = "+s);
        if(fileRead != null) {s=fileRead.getAbsolutePath();} else {s="\"null\"";}        
        System.out.println("   fileRead = "+s);
    }
    if(!readOk) {
        String msg = "Failed to read any INI-file."+nl+
            "Default program settings will be used.";
        System.out.println("----"+nl+msg+nl+"----");
    }
    if(fileINInotRO != null && fileINInotRO != fileRead) {
        ok = saveIni(fileINInotRO);
        if(ok) {fileIni = fileINInotRO;} else {fileIni = null;}
    }
  } // readIni()

  private boolean readIni2(java.io.File f) {
    System.out.flush();
    System.out.println("Reading ini-file: \""+f.getPath()+"\"");
    java.util.Properties propertiesIni = new java.util.Properties();
    java.io.FileInputStream properties_iniFile = null;
    boolean ok = true;
    try {
      properties_iniFile = new java.io.FileInputStream(f);
      propertiesIni.load(properties_iniFile);
      //throw new Exception("Test error");
    } //try
    catch (java.io.FileNotFoundException e) {
      System.out.println("Warning: file not found: \""+f.getPath()+"\""+nl+
                         "using default parameter values.");
      checkIniValues();
      ok = false;
    } //catch FileNotFoundException
    catch (java.io.IOException e) {
      MsgExceptn.exception(Util.stack2string(e));
      String msg = "Error: \""+e.toString()+"\""+nl+
                   "   while loading INI-file:"+nl+
                   "   \""+f.getPath()+"\"";
      MsgExceptn.showErrMsg(this, msg, 1);
      ok = false;
    } // catch loading-exception
    finally {
        try {if(properties_iniFile != null) {properties_iniFile.close();}}
        catch (java.io.IOException e) {
            String msg = "Error: \""+e.toString()+"\""+nl+
                          "   while closing INI-file:"+nl+
                          "   \""+f.getPath()+"\"";
            MsgExceptn.showErrMsg(this, msg, 1);
        }
    }
    if(!ok) {return ok;}
    try {
        pd.pathAddData.replace(0, pd.pathAddData.length(), propertiesIni.getProperty("pathAddData"));
        int nbr = pd.dataBasesList.size();
        if(nbr > 0) {pd.dataBasesList.clear();}
        nbr = Integer.parseInt(propertiesIni.getProperty("DataBases_Nbr"));
        String dbName;
        for(int i=0; i < nbr; i++) {
            dbName = propertiesIni.getProperty("DataBase["+String.valueOf(i+1).trim()+"]");
            if(dbName != null && dbName.length() >0) {pd.dataBasesList.add(dbName);}
        }
    }  catch (NumberFormatException e) {
        MsgExceptn.exception(Util.stack2string(e));
        String msg = "Error: \""+e.toString()+"\""+nl+
                         "   while reading INI-file:"+nl+
                         "   \""+f.getPath()+"\""+nl+nl+
                         "Setting default program parameters.";
        MsgExceptn.showErrMsg(this, msg, 1);
        ok = false;
    }
    try {
        pc.pathDef.replace(0, pc.pathDef.length(), propertiesIni.getProperty("defaultPath"));
    } catch (NullPointerException ex) {pc.setPathDef();}
    if(pc.dbg) {System.out.println("Finished reading ini-file");}
    System.out.flush();
    checkIniValues();
    return ok;
  } // readIni2()

  private void checkIniValues() {
    System.out.flush();
    System.out.println(LINE+nl+"Checking ini-values.");
    //-- check Default Path
    java.io.File f = new java.io.File(pc.pathDef.toString());
    if(!f.exists()) {
        pc.setPathDef(); // = "user.home"
    } // if !currentDir.exists()
    //-- check pathAddData
    if(pd.pathAddData.length() >0) {
    f = new java.io.File(pd.pathAddData.toString());
    if(!f.exists()) {
        if(pd.pathAddData.length() >0) {pd.pathAddData.delete(0, pd.pathAddData.length());}
        pd.pathAddData.append(pc.pathDef.toString());
    }
    } else  {
        pd.pathAddData.append(pc.pathDef.toString());
    }

    String dir = pc.pathAPP;
    if(dir != null && dir.endsWith(SLASH)) {dir = dir.substring(0, dir.length()-1);}
    String msg;
    String dbName;
    boolean warn = false;
    int nbr = pd.dataBasesList.size();
    if(nbr <=0) {
        msg = "There are no databases selected.";
        warn = true;
        if(dir != null && dir.trim().length()>0) {dbName = dir + SLASH + DEF_DataBase;} else {dbName = DEF_DataBase;}
        java.io.File db = new java.io.File(dbName);
        if(db.exists() && db.canRead()) {
            msg = null;
            pd.dataBasesList.add(dbName);
        }
    } else {
      LibDB.checkDataBasesList(this, pd.dataBasesList, true);
      nbr = pd.dataBasesList.size();
      if(nbr <=0) { // none of the databases exist.
        msg = "Error: none of the databases in the \"INI\"-file exist.";
        if(dir != null && dir.trim().length()>0) {dbName = dir + SLASH + DEF_DataBase;} else {dbName = DEF_DataBase;}
        if(LibDB.isDBnameOK(this, dbName, false)) {pd.dataBasesList.add(dbName);}
      } else {msg = null;}
    } // no databases in INI-file
    if(msg != null) {
        System.out.println(msg);
        msg = msg + nl+nl+"Please select databases"+nl+
                "by double-clicking on the empty panel!"+nl+" ";
        int type = javax.swing.JOptionPane.ERROR_MESSAGE;
        if(warn) {type = javax.swing.JOptionPane.WARNING_MESSAGE;}
        javax.swing.JOptionPane.showMessageDialog(this, msg, pc.progName, type);
    }
  } // checkIniValues()

  private void iniDefaults() {
      // Set default values for program variables
      if (pc.dbg) {
          System.out.flush();
          System.out.println("Setting default parameter values (\"ini\"-values).");
      }
      // set the default path to the "user.home"
      pc.setPathDef();
      pd.pathAddData.replace(0, pd.pathAddData.length(), pc.pathDef.toString()); //System.getProperty("user.home")
      pd.dataBasesList.clear();
      String dir = pc.pathAPP;
      if(dir != null && dir.endsWith(SLASH)) {dir = dir.substring(0, dir.length()-1);}
      String dbName;
      if(dir != null && dir.trim().length()>0) {dbName = dir + SLASH + DEF_DataBase;} else {dbName = DEF_DataBase;}
      java.io.File db = new java.io.File(dbName);
      if(db.exists() && db.canRead()) {pd.dataBasesList.add(dbName);}
  } // iniDefaults()

  /** Save program settings.
   * Exceptions are reported both to the console (if there is one) and to a dialog */
  private boolean saveIni(java.io.File f) {
    if(f == null) {return false;}
    if(pc.dbg) {System.out.println("--- saveIni("+f.getAbsolutePath()+")");}
    boolean ok = true;
    String msg = null;
    if(f.exists() && (!f.canWrite() || !f.setWritable(true))) {
        msg = "Error - can not write ini-file:"+nl+
                  "\""+f.getAbsolutePath()+"\""+nl+
                  "The file is read-only.";
    }
    if(!f.exists() && !f.getParentFile().exists()) {
        ok = f.getParentFile().mkdirs();
        if(!ok) {
            msg = "Error - can not create directory:"+nl+
                  "\""+f.getParent()+"\""+nl+
                  "Can not write ini-file.";
        }
    }
    if(msg != null) {
        MsgExceptn.showErrMsg(this, msg, 2);
        return false;
    }
    java.util.Properties propertiesIni= new SortedProperties();
    propertiesIni.setProperty("<program_version>", VERS);
    propertiesIni.setProperty("pathAddData", pd.pathAddData.toString());
    propertiesIni.setProperty("defaultPath", pc.pathDef.toString());
    int nbr = pd.dataBasesList.size();
    propertiesIni.setProperty("DataBases_Nbr", String.valueOf(nbr));
    for (int i = 0; i < nbr; i++) {
        propertiesIni.setProperty("DataBase["+String.valueOf(i+1).trim()+"]", pd.dataBasesList.get(i));
    }

    System.out.println("Saving ini-file: \""+f.getPath()+"\"");
    java.io.FileOutputStream Properties_iniFile = null;
    try{
        Properties_iniFile = new java.io.FileOutputStream(f);
        propertiesIni.store(Properties_iniFile,null);
        if (pc.dbg) {System.out.println("Written: \""+f.getPath()+"\"");}
    } // try
    catch (java.io.IOException e) {
        msg = "Error: \""+e.toString()+"\""+nl+
                     "   while writing INI-file:"+nl+
                     "   \""+f.getPath()+"\"";
        MsgExceptn.showErrMsg(this, msg, 1);
        ok = false;
    } // catch store-exception
    finally {
      try {if(Properties_iniFile != null) {Properties_iniFile.close();}}
      catch (java.io.IOException e) {}
    } //finally
    return ok;
  } // saveIni()
//</editor-fold>

 //<editor-fold defaultstate="collapsed" desc="toBinary">
  /** Combines the information in all databases and stores it in a single binary file
   * @param eComp array list of String[3] objects<br>
   * [0] contains the element name (e.g. "C"),<br>
   * [1] the component formula ("CN-" or "Fe+2"),<br>
   * [2] the component name ("cyanide" or null), which is not really needed,
   * but used to help the user
   * @see lib.database.ProgramDataDB#elemComp
   */
  private void toBinary(final java.util.ArrayList<String[]> eComp) {
    if(pd == null || pd.dataBasesList == null || pd.dataBasesList.size() <=0) {
        String msg = "There are NO databases to convert to binary!"+nl+" ";
        if(dispatchingArgs) {msg = msg +nl+ "Please run the program again"+nl+
                "WITHOUT the \"-bin\" command line option,"+nl+
                "select some databases, and THEN"+nl+
                "run again with the \"-bin\" option."+nl+" ";}
        MsgExceptn.showErrMsg(this, msg, 1);
        return;
    }
    if(fileCmplxSaveName == null || fileCmplxSaveName.trim().length()<=0) {
        fileCmplxSaveName = DEF_DataBase;
    }
    fileCmplxSaveName = Util.getSaveFileName(this,pc.progName,
           "Enter an output binary database name", 4, fileCmplxSaveName, pc.pathDef.toString());
    if(fileCmplxSaveName == null || fileCmplxSaveName.trim().length() <=0) {return;}
    pc.setPathDef(fileCmplxSaveName);
    //Check that the output file is not equal to an input file
    boolean found = false;
    for(int i=0; i < pd.dataBasesList.size(); i++) {
        if(pd.dataBasesList.get(i).equalsIgnoreCase(fileCmplxSaveName)) {found = true; break;}
    }
    if(found) {
        String msg = "? Can not create output file:"+nl+
              "    \""+fileCmplxSaveName + "\""+nl+
              "Because it is given in the input database-list.";
        MsgExceptn.showErrMsg(this, msg, 1);
        return;
    }
    if(pd.dataBasesList.size() >1) {
        System.out.println(LINE+nl+"Merging all files into a single binary database . . .");
    } else {
        System.out.println(LINE+nl+"Converting the text file into a binary database . . .");
    }
    //if(pc.dbg) {
    //    System.out.println("- - -  eComp.get()[1]:");
    //    for(int j=0; j < eComp.size(); j++) {System.out.println(eComp.get(j)[1]);}
    //    System.out.println("- - -");
    //}
    setFrameEnabled(false);
    working = true;
    // --- display the messages frame
    if(pd.msgFrame != null) {
        pd.msgFrame.setVisible(true);
        jCheckBoxDebugFrame.setSelected(true);
    }
    new javax.swing.SwingWorker<Void,Integer>() {
        @Override protected Void doInBackground() throws Exception {
          // --------------------------------------
          // ----  Elements-Components file    ----
          // --------------------------------------
          //--- read the elements and components from all databases into "eComp"
          if(!LibDB.getElements(DataMaintenance.this, pc.dbg, pd.dataBasesList, eComp)) {working = false;}
          if(!working) {return null;}
          //--- write the elements and components into a single database
          String fileElemSaveName = Div.getFileNameWithoutExtension(fileCmplxSaveName)+".elb";
          java.io.File fileElemSave = new java.io.File(fileElemSaveName);
          if(fileElemSave.exists()) {
              Object[] opt = {"Yes", "Cancel"};
              int m = javax.swing.JOptionPane.showOptionDialog(DataMaintenance.this,
                    "Note: the elements-components file"+nl+"\""+fileElemSaveName+"\""+nl+" already exists.  Overwrite?",
                    pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
              if(m != javax.swing.JOptionPane.YES_OPTION) {
                  System.out.println("---- Cancelled by the user.");
                  return null;
              }//if !yes
          }//if output file exists

          // ---------------------------
          // ----  Reactions file   ----
          // ---------------------------
          // all datafiles are read and a single binary file generated,
          // including the reactions in both binary and text files
          java.io.File fileCmplxSave = new java.io.File(fileCmplxSaveName);
          System.out.println("--- Reading reactions to write binary file:"+nl+
                             "    \""+fileCmplxSaveName+"\"");
          // There is not need to ask permission to overwrite an existing file,
          // it has already been done in "Util.getSaveFileName"
          Complex complex, oldCmplx;
          dataList.clear();
          java.io.File f;
          java.io.BufferedReader br;
          java.io.DataInputStream dis = null;
          boolean binary, added, removed, found;
          String msg;
          int db = 0;
          publish(0);
          jLabelNbr.setText("0");   jLabelNbr.setVisible(true);
          java.util.ArrayList<Complex> toRemove = new java.util.ArrayList<Complex>();

          // -------------------------------------
          // --- loop  through all databases  ----
          // -------------------------------------
          int converted = 0;
          while (db < pd.dataBasesList.size()) {
            if(!working) {return null;}
            f = new java.io.File(pd.dataBasesList.get(db));
            if(!f.exists() || !f.canRead()) {
                msg = "Error - file \""+pd.dataBasesList.get(db)+"\""+nl;
                if(f.exists()) {msg = msg.concat("    can not be read.");}
                else {msg = msg.concat("    does not exist.");}
                System.err.println(msg);
                continue;
            }
            binary = false;
            if(Div.getFileNameExtension(pd.dataBasesList.get(db)).equalsIgnoreCase("db")) {binary = true;}
            publish(0);
            System.out.println("Reading file \""+f.getName()+"\"");

            // -- read a txt file
            if(!binary) {
                fLength = (double)f.length();
                try{br = new java.io.BufferedReader(new java.io.FileReader(f));}
                catch (java.io.FileNotFoundException ex) {
                        msg = "Error: "+ex.toString()+nl+"while trying to open file: \""+f.getName()+"\"";
                        System.err.println(msg+nl+"in procedure \"toBinary\".");
                        javax.swing.JOptionPane.showMessageDialog(null, msg, pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
                        return null;
                }

                try{
                cmplxNbr = 0;
                loopComplex:
                while (true) {
                  if(!working) {return null;}
                  try{complex = LibDB.getTxtComplex(br);}
                  catch (LibDB.EndOfFileException ex) {complex = null;}
                  catch (LibDB.ReadTxtCmplxException ex) {
                      msg = "Error: in \"toBinary\", cmplxNbr = "+cmplxNbr+nl+ex.toString();
                      MsgExceptn.exception(msg);
                      javax.swing.JOptionPane.showMessageDialog(DataMaintenance.this, msg, pc.progName,
                              javax.swing.JOptionPane.ERROR_MESSAGE);
                      return null;
                  }
//try{Thread.sleep(1);} catch (InterruptedException ex) {}
                  publish((int)(100*(double)cmplxNbr*F_TXT_CMPLX/fLength));
                  jLabelNbr.setText(String.valueOf(cmplxNbr));
                  if(complex == null) {break;} // loopComplex // end-of-file, open next database

                  //if name starts with "@": remove any existing complex with this name
                  if(complex.name.startsWith("@")) {
                      complex.name = complex.name.substring(1);
                      System.out.println("   removing any previous occurrences of species: \""+complex.name+"\"");
                      // this is time consuming, but hopefully it is not done many times
                      java.util.Iterator<Complex> datIt = dataList.iterator();
                      toRemove.clear();
                      while(datIt.hasNext()) {
                          if(!working) {return null;} //this will go to finally
                          oldCmplx = datIt.next();
                          if(Util.nameCompare(oldCmplx.name, complex.name)) {toRemove.add(oldCmplx);}
                      }
                      if(!toRemove.isEmpty()) {
                          for(Complex c : toRemove) {
                            System.out.println("   - removing complex \""+c.toString()+"\"");
                            removed = dataList.remove(c);
                            if(!removed) {
                                msg = "Error: in \"toBinary\", can not remove complex:"+nl+"  \""+c.toString()+"\"";
                                MsgExceptn.exception(msg);
                                javax.swing.JOptionPane.showMessageDialog(DataMaintenance.this, msg, pc.progName,
                                        javax.swing.JOptionPane.ERROR_MESSAGE);
                            }
                          }
                          System.out.println("   - finished searching for: \""+complex.name+"\"");
                      } else {
                          System.out.println("   - no previous occurrences of \""+complex.name+"\" were found.");
                      }
                  } else {
                      //name does not start with "@": add complex...
                      //check if the components are in the list of possible components
                      msg = Complex.checkComplex(complex);
                      for(int i=0; i < Complex.NDIM; i++) {
                        if(complex.component[i] != null && complex.component[i].length() >0) {
                            found = false;
                            for(int j=0; j < eComp.size(); j++) {
                                if(complex.component[i].equals(eComp.get(j)[1])) {found = true; break;}
                            } //for j
                            if(!found) {
                                String t = "Component \""+complex.component[i]+"\" in complex \""+complex.name+"\""+nl+"not found in the element-files.";
                                if(msg == null || msg.length() <= 0) {msg = t;} else {msg = msg +nl+ t;}
                            }//not found
                        }
                      } // for i
                      if(msg != null) {
                        System.out.println("---- Error: "+msg);
                        Object[] opt = {"OK", "Cancel"};
                        int answer = javax.swing.JOptionPane.showOptionDialog(DataMaintenance.this,
                                "Error in file \""+f.getName()+"\""+nl+msg,
                                pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
                                javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
                        if(answer != javax.swing.JOptionPane.YES_OPTION) {
                            return null; //this will go to finally
                        }
                      } //if msg !=null
                      //if the complex already is in the list, replace it.
                      // (this is perhaps time consuming)
                      found = false;
                      toRemove.clear();
                      java.util.Iterator<Complex> datIt = dataList.iterator();
                      while(datIt.hasNext()) {
                          if(!working) {return null;} //this will go to finally
                          oldCmplx = datIt.next();
                          if(Complex.sameNameAndStoichiometry(oldCmplx, complex) ||
                                (Util.nameCompare(oldCmplx.name, complex.name)
                                && (!Complex.isRedox(oldCmplx) || !Complex.isRedox(complex)))
                                ) { toRemove.add(oldCmplx); }
                      }
                      if(!toRemove.isEmpty()) {
                          found = true;
                          for(Complex c : toRemove) {
                            System.out.println("   - removing complex \""+c.toString()+"\"");
                            removed = dataList.remove(c);
                            if(!removed) {
                                msg = "Error: in \"toBinary\", can not remove complex:"+nl+"  \""+c.toString()+"\"";
                                MsgExceptn.exception(msg);
                                javax.swing.JOptionPane.showMessageDialog(DataMaintenance.this, msg, pc.progName,
                                        javax.swing.JOptionPane.ERROR_MESSAGE);
                            }
                          }
                      }
                      if(found){
                            System.out.println("   - adding complex \""+complex.toString()+"\"");
                      }
                      added = dataList.add(complex);
                      if(!added) {
                            msg = "Error: in \"toBinary\", can not add complex:"+nl+"  \""+complex.toString()+"\"";
                            MsgExceptn.exception(msg);
                            javax.swing.JOptionPane.showMessageDialog(DataMaintenance.this, msg, pc.progName,
                                    javax.swing.JOptionPane.ERROR_MESSAGE);
                      }
                  } //starts with "@"?
                  cmplxNbr++;
                } //while - loopComplex:
                } catch (Exception ex) {System.out.println("An exception occurred."); return null;}
                converted++;
                try{br.close();} catch (java.io.IOException ex) {System.err.println("Error "+ex.toString()); return null;}
            } else {
              // -- read a binary file
              fLength = (double)f.length();
              try{
                dis = new java.io.DataInputStream(new java.io.FileInputStream(f));
                cmplxNbr = 0;
                while (true){
                    if(!working) {return null;} //this will go to finally
                    publish((int)(100*(double)cmplxNbr*F_BIN_CMPLX/fLength));
                    javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
                      jLabelNbr.setText(String.valueOf(cmplxNbr));
                    }});
                    complex = LibDB.getBinComplex(dis);
                    if(complex == null) {break;} //end of file

                    // this is perhaps time consuming
                    for (java.util.Iterator<Complex> datIt = dataList.iterator(); datIt.hasNext(); ) {
                        if(!working) {return null;} //this will go to finally
                        oldCmplx = datIt.next();
                        if(Util.nameCompare(oldCmplx.name, complex.name)) {
                            System.out.println("   replacing \""+oldCmplx.name+"\" with: \""+complex.name+"\", logK="+Util.formatDbl3(complex.constant));
                            removed = dataList.remove(complex);
                            if(!removed) {
                                msg = "Error: in \"toBinary\", can not remove complex:"+nl+"  \""+complex.toString()+"\"";
                                MsgExceptn.exception(msg);
                                javax.swing.JOptionPane.showMessageDialog(DataMaintenance.this, msg, pc.progName,
                                    javax.swing.JOptionPane.ERROR_MESSAGE);
                            }
                        } // names are equivalent
                        added = dataList.add(complex);
                        if(!added) {
                            msg = "Error: in \"toBinary\", can not add complex:"+nl+"  \""+complex.toString()+"\"";
                            MsgExceptn.exception(msg);
                            javax.swing.JOptionPane.showMessageDialog(DataMaintenance.this, msg, pc.progName,
                                    javax.swing.JOptionPane.ERROR_MESSAGE);
                        }
                    } //for datIt-Iterator
                    cmplxNbr++;
                }// while(true) loop through the whole file
                converted++;
              } catch (Exception ex) {
                System.err.println("Error: "+ex.getMessage()+nl+"with \""+f.getAbsolutePath()+"\"");
                return null;
              }
              finally {
                if(dis != null) {
                  try{dis.close();}
                  catch (java.io.IOException ex) {System.err.println("Input-Output error: "+ex.toString()); return null;}
                }
                publish(0);
              }
              if(!working) {return null;}
            } // binary?
            // -- next database
            db++;
          } //while - db
          // --- end database loop

          // --- save all reactions that have been read from all files (in datIt)
          //     into a single binary file
          publish(0);
          System.out.println("Writing file \""+fileCmplxSave.getName()+"\"");

          java.io.DataOutputStream ds = null;
          boolean fnd;
          int eCompSize = eComp.size();
          // only components (metals or ligands) that are used
          // will be saved in the elements file
          boolean[] eCompUsed = new boolean[eCompSize];
          for(int i=0; i<eCompSize; i++) {eCompUsed[i] = false;}
          try {
            // Wrap the FileOutputStream with a DataOutputStream to obtain its writeInt(), etc methods
            ds = new java.io.DataOutputStream(new java.io.FileOutputStream(fileCmplxSave));
            cmplxNbr = 0;
            System.out.println("total nbr of reactions: "+dataList.size());
            double nTot = (double)dataList.size();
            for (java.util.Iterator<Complex> datIt = dataList.iterator(); datIt.hasNext(); ) {
              if(!working) {return null;} //this will go to finally
              publish((int)(100*(double)cmplxNbr/nTot));
              jLabelNbr.setText(String.valueOf(cmplxNbr));
              complex = datIt.next();
              LibDB.writeBinCmplx(ds,complex);
              ds.flush();
//try{Thread.sleep(1);} catch (InterruptedException ex) {}
              //mark the components that are used as "needed"
              loopNDIM:
              for(int j=0; j<Complex.NDIM; j++) {
                  if(Math.abs(complex.numcomp[j]) < 0.0001 ||
                     complex.component[j] == null || complex.component[j].length() <=0) {continue;}
                  fnd = false;
                  for(int i = 0; i < eCompSize; i++) {
                    if(eComp.get(i)[1].equals(complex.component[j])) {
                        // only components (metals or ligands) that are used
                        // will be saved in the elements file
                        eCompUsed[i] = true;
                        fnd = true;
                        //no "break": a component (ligand) might be in under several elements
                    }
                  }//for i              
                  if(!fnd) {MsgExceptn.exception("--- Component: "+complex.component[j]+" in complex "+complex.name+nl+" not found in the element files.");}
              }//for j
              cmplxNbr++;
            } //for datIt-Iterator
          }
          catch (Exception ex) {
               System.out.println(ex.toString()+nl+
                       "while writing binary reactions database file:"+nl+"   \""+fileCmplxSaveName+"\"");
               return null;
          }
          finally {
            if(ds != null) {
                try{ds.flush(); ds.close();}
                catch (java.io.IOException ex) {System.err.println(ex.toString()); return null;}
            }
          }
          if(!working) {return null;}

          // --------------------------------------
          // ----  Elements-Components file    ----
          // --------------------------------------
          //--- write the elements and components into a single database
          publish(0);
          System.out.println("Writing binary \"elements\"-file:"+nl+"   \""+fileElemSaveName+"\"");
          int n;
          StringBuilder elemSymbol = new StringBuilder();
          java.io.DataOutputStream dos = null;
          final int ELEMENTS = LibDB.ELEMENTS;
          try {
            dos = new java.io.DataOutputStream(new java.io.FileOutputStream(fileElemSave));
            for(int i=0; i < ELEMENTS; i++) {
              if(!working) {return null;} //a return here will enter "finally"
              publish((int)(100*(double)i/(double)ELEMENTS));

              elemSymbol.replace(0, elemSymbol.length(), LibDB.elementSymb[i]);
              n=0;
              for(int j=0; j < eCompSize; j++) {
                if(!working) {return null;} //this would go to "finally"
                if(eComp.get(j)[0].equals(elemSymbol.toString()) && eCompUsed[j]) {n++;}
              }//for j

              if(n > 0) {
                dos.writeUTF(elemSymbol.toString());
                dos.writeInt(n);
                for(int j=0; j < eCompSize; j++) {
                  if(!working) {return null;}
                  if(eComp.get(j)[0].equals(elemSymbol.toString()) && eCompUsed[j]) {
                    dos.writeUTF(eComp.get(j)[1]);
                    dos.writeUTF(eComp.get(j)[2]);
                  }
                }//for j
              }//if n>0
            } //for i

            //--- For components in the database not belonging to any element:
            //    set all of them into "XX"
            publish(100);
            n = 0;
            for(int j=0; j < eCompSize; j++) {
              if(!working) {return null;}
              //if(eComp.get(j)[1].equals("H2O")) {eCompUsed[j] = true;}
              if(!eCompUsed[j]) {
                  System.out.println("   Component: "+eComp.get(j)[1]+" not used.");
                  continue;
              }
              elemSymbol.replace(0, elemSymbol.length(), eComp.get(j)[0]);
              found = false;
              for(int i=0; i < ELEMENTS; i++) {
                if(!working) {return null;}
                if(LibDB.elementSymb[i].equals(elemSymbol.toString())) {
                    found = true;
                    break;
                }
              } //for i
              if(!found) {
                  System.out.println("   Component: "+eComp.get(j)[1]+" not connected to an element.");
                  n++;
              }
            }//for j
            if(n > 0) {
              if(!working) {return null;}
              dos.writeUTF("XX");
              dos.writeInt(n);
              for(int j=0; j < eCompSize; j++) {
                if(!working) {return null;}
                if(!eCompUsed[j]) {continue;} //has it been used?
                elemSymbol.replace(0, elemSymbol.length(), eComp.get(j)[0]);
                found = false;
                for(int i=0; i < ELEMENTS; i++) {
                    if(!working) {return null;}
                    if(LibDB.elementSymb[i].equals(elemSymbol.toString())) {
                        found = true;
                        break;
                    }
                }//for i
                if(!found) {
                  dos.writeUTF(eComp.get(j)[1]);
                  dos.writeUTF(eComp.get(j)[2]);
                }//if !found
              }//for j
            }//if n>0
          } catch (Exception ioe) {
              System.err.println("Error: "+ioe.toString()+nl+"with \""+fileElemSaveName+"\"");
              return null;
          }
          finally {
            if(dos != null) {
              try{dos.flush(); dos.close();}
              catch (java.io.IOException ex) {System.err.println("Error: "+ex.toString()); return null;}
            }
          }
          
          if(converted > 1) {msg = "Finished merging "+converted+" text databases";}
          else {msg = "Finished converting the text database";}
          msg = msg+"."+nl+
                "The following binary files were created:"+nl+
                "     \""+fileElemSaveName+"\""+nl+
                "     \""+fileCmplxSaveName+"\"";
          System.out.println("----"+nl+msg+nl+"----");              
          javax.swing.JOptionPane.showMessageDialog(DataMaintenance.this,msg,
                    pc.progName,javax.swing.JOptionPane.INFORMATION_MESSAGE);
          return null;
        } //doInBackground()
        @Override protected void done(){
          jProgressBar.setVisible(false);
          setFrameEnabled(true);
          working = false;
          bringToFront();
        } //done()
        @Override protected void process(java.util.List<Integer> chunks) {
            // Here we receive the values that we publish(). They may come grouped in chunks.
            final int i = chunks.get(chunks.size()-1);
            if(!jProgressBar.isVisible()) {jProgressBar.setVisible(true);}
            jProgressBar.setValue(i);
        }  //process(java.util.List<Integer> chunks)
    }.execute();
    //any statements placed below are executed inmediately
  } //toBinary()
//</editor-fold>

 //<editor-fold defaultstate="collapsed" desc="toText">
  private void toText() {
    boolean found = false;
    for(int i = 0; i < pd.dataBasesList.size(); i++) {
      final String db = pd.dataBasesList.get(i);
      if(Div.getFileNameExtension(db).equalsIgnoreCase("db")) {found = true; break;}
    }//for i
    if(!found) {
         String msg = "No binary (*.db) files selected!";
         System.err.println(msg);
         javax.swing.JOptionPane.showMessageDialog(DataMaintenance.this, msg, pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
         return;
    }
    setFrameEnabled(false);
    working = true;
    if(pd.msgFrame != null) {
        pd.msgFrame.setVisible(true);
        jCheckBoxDebugFrame.setSelected(true);
    }
    System.out.println("--- Converting binary database files to text format.");
    new javax.swing.SwingWorker<Void,Integer>() {
        @Override protected Void doInBackground() throws Exception {
        StringBuilder elemSymbol = new StringBuilder();
        int n;

        StringBuilder elemFileNameIn = new StringBuilder();
        StringBuilder elemFileNameOut = new StringBuilder();
        StringBuilder dbOut = new StringBuilder();
        java.io.File fileRead, fileSave;
        String msg;
        publish(0);
        int converted = 0;
        try {
            for(int i = 0; i < pd.dataBasesList.size(); i++) {
                if(!working) {return null;}  //a return here will enter "finally"
                final String db = pd.dataBasesList.get(i);
                if(Div.getFileNameExtension(db).equalsIgnoreCase("db")) {
                  fileRead = new java.io.File(db);
                  if(!fileRead.exists()) {
                      msg = "File not found:"+nl+"\""+db+"\""+nl;
                      System.err.println(msg);
                      javax.swing.JOptionPane.showMessageDialog(DataMaintenance.this, msg, pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
                      continue;
                  }
                  fLength = (double)fileRead.length();
                  //-------------------------------------------
                  //--- process the element/component file ----
                  //-------------------------------------------
                  elemFileNameIn.replace(0, elemFileNameIn.length(),
                          Div.getFileNameWithoutExtension(db));
                  elemFileNameOut.replace(0, elemFileNameOut.length(), elemFileNameIn.toString());
                  elemFileNameIn.append(".elb");
                  fileRead = new java.io.File(elemFileNameIn.toString());
                  if(!fileRead.exists()) {
                      msg = "File not found:"+nl+"\""+elemFileNameIn.toString()+"\""+nl;
                      System.err.println(msg);
                      javax.swing.JOptionPane.showMessageDialog(DataMaintenance.this, msg, pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
                      continue;
                  }
                  elemFileNameOut.append(".elt");
                  fileSave = new java.io.File(elemFileNameOut.toString());
                  if(fileSave.exists()) {
                    Object[] opt = {"Yes", "Cancel"};
                    int m = javax.swing.JOptionPane.showOptionDialog(DataMaintenance.this,
                        "Note: the elements-components file"+nl+"\""+elemFileNameOut.toString()+"\""+nl+"already exists.  Overwrite?",
                        pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
                    if(m != javax.swing.JOptionPane.YES_OPTION) {continue;} //next database
                  } //if output file exists
                  System.err.println("Converting \""+fileRead.getName()+"\" into \""+fileSave.getName()+"\"");
                  fLength = (double)fileRead.length();
                  java.io.DataInputStream dis = null;
                  java.io.PrintWriter pw = null;
                  try{
                    dis = new java.io.DataInputStream(new java.io.FileInputStream(fileRead));
                    pw =  new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter(fileSave)));
                    cmplxNbr = 0;
                    while (true){
                        if(!working) {return null;} //this will go to finally
                        publish((int)(100*(double)cmplxNbr*F_BIN_ELEM/fLength));
                        elemSymbol.replace(0, elemSymbol.length(), dis.readUTF());
                        //System.out.println(" element="+elemSymbol.toString());
                        n = dis.readInt();
                        pw.format("%-2s,%2d ,",elemSymbol.toString(),n);
                        for(int j = 0; j < n; j++) {
                          pw.print(dis.readUTF()+","+dis.readUTF()+",");
                        }
                        pw.println();
                        pw.flush();
                        cmplxNbr++;
                    }
                  } //try
                  catch (java.io.EOFException ex) {
                      converted++;
                      System.out.println("Finished; file \""+fileSave.getName()+"\" written.");
                  }
                  catch (java.io.IOException ex) {
                      System.err.println("Error: "+ex.toString()+nl+"with \""+elemFileNameIn.toString()+"\"");
                  }
                  finally {
                    if(dis != null) {
                        try{dis.close();}
                        catch (java.io.IOException ex) {System.err.println("Input-Output error: "+ex.toString());}
                    }
                    if(pw != null) {pw.flush(); pw.close();}
                    publish(0);
                  }
                  if(!working) {return null;}
                  //-----------------------------------
                  //--- process reactions database ----
                  //-----------------------------------
                  fileRead = new java.io.File(db);
                  dbOut.replace(0, dbOut.length(), Div.getFileNameWithoutExtension(db));
                  dbOut.append(".txt");
                  fileSave = new java.io.File(dbOut.toString());
                  if(fileSave.exists()) {
                    Object[] opt = {"Yes", "Cancel"};
                    int m = javax.swing.JOptionPane.showOptionDialog(DataMaintenance.this,
                        "Note: the reactions database file"+nl+"\""+dbOut.toString()+"\""+nl+"already exists.  Overwrite?",
                        pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
                    if(m != javax.swing.JOptionPane.YES_OPTION) {continue;} //next database
                  } //if output file exists
                  System.err.println("Converting \""+fileRead.getName()+"\" into \""+fileSave.getName()+"\"");
                  fLength = (double)fileRead.length();
                  dis = null;
                  pw = null;
                  try{
                    dis = new java.io.DataInputStream(new java.io.FileInputStream(fileRead));
                    pw =  new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter(fileSave)));
                    pw.println(Complex.FILE_FIRST_LINE);
                    Complex cmplx;
                    cmplxNbr = 0;
                    while (true){
                        if(!working) {return null;} //this will go to finally
                        publish((int)(100*(double)cmplxNbr*F_BIN_CMPLX/fLength));
                        cmplx = LibDB.getBinComplex(dis);
                        if(cmplx == null) {break;} //end of file
                        LibDB.writeTxtComplex(pw, cmplx);
                        cmplxNbr++;
                    }
                    converted++;
                    System.out.println("Finished; file \""+fileSave.getName()+"\" written.");
                  } //try
                  catch (java.io.IOException ex) {
                    System.err.println("Error: "+ex.toString()+nl+"with \""+elemFileNameIn.toString()+"\"");
                  }
                  finally {
                    if(dis != null) {
                        try {dis.close();}
                        catch (java.io.IOException ex) {System.err.println("Input-Output error: "+ex.toString());}
                    }
                    if(pw != null) {pw.flush(); pw.close();}
                    publish(0);
                  }
                  if(!working) {return null;}
              }//if binary datafile
            }//for i
            } //try
            catch (java.awt.HeadlessException ex) {
                System.err.println("Error: "+ex.toString()); ex.printStackTrace();
            }
            catch (LibDB.ReadBinCmplxException ex) {
                System.err.println("Error: "+ex.toString()); ex.printStackTrace();
            }
            catch (LibDB.WriteTxtCmplxException ex) {
                System.err.println("Error: "+ex.toString()); ex.printStackTrace();
            }
            finally {
                msg = "---- Converted "+converted+" file";
                if(converted > 1) {msg = msg+"s";}
                System.out.println(msg);
            }
            return null;
        } //doInBackground()
        @Override protected void done(){
          setCursorDef();
          jProgressBar.setVisible(false);
          setFrameEnabled(true);
          working = false;
        } //done()
        @Override protected void process(java.util.List<Integer> chunks) {
            // Here we receive the values that we publish(). They may come grouped in chunks.
            final int i = chunks.get(chunks.size()-1);
            if(!jProgressBar.isVisible()) {jProgressBar.setVisible(true);}
            jProgressBar.setValue(i);
        }  //process(java.util.List<Integer> chunks)
    }.execute();
    //any statements placed below are executed inmediately

  } //toText()
//</editor-fold>

 //<editor-fold defaultstate="collapsed" desc="dispatchArg(String)">
 /** Execute the command-line arguments (one by one)
  * @param arg String containing a command-line argument */
  public void dispatchArg(String arg) {
    if(arg == null || arg.length() <=0) {return;}
    System.out.println("Command-line argument: \""+arg+"\", length = "+arg.length());
    //these are handled in "main"
    if(arg.equals("-dbg") || arg.equals("/dbg")) {doNotExit = true; return;}
    if(arg.equals("-?") || arg.equals("/?") || arg.equals("?")) {
        printInstructions();
        msgFrame.setVisible(true);
        doNotExit = true;
        return;}
    // ---- starts with "-bin"
    if(arg.length() >3) {
        String arg0 = arg.substring(0, 4).toLowerCase();
        if((arg.charAt(0) == '-' || arg.charAt(0) == '/') &&
            arg0.substring(1,4).equals("bin")) {
                if(arg.charAt(4) == '=' || arg.charAt(4) == ':' && arg.length() >5) {
                    setFrameEnabled(false);
                    fileCmplxSaveName = arg.substring(5);
                    if(fileCmplxSaveName.length() > 2 && fileCmplxSaveName.startsWith("\"")
                        && fileCmplxSaveName.endsWith("\"")) {
                        fileCmplxSaveName = fileCmplxSaveName.substring(1, arg.length()-1);
                    }
                    if(fileCmplxSaveName.length()>3 && fileCmplxSaveName.toLowerCase().endsWith(".db")) {
                        java.io.File f = new java.io.File(fileCmplxSaveName);
                        try{fileCmplxSaveName = f.getCanonicalPath();}
                        catch (IOException ex) {
                            try{fileCmplxSaveName = f.getAbsolutePath();}
                            catch (Exception e) {fileCmplxSaveName = f.getPath();}
                        }
                        if(f.exists()) {
                            Object[] opt = {"Yes", "Cancel"};
                            int m = javax.swing.JOptionPane.showOptionDialog(DataMaintenance.this,
                                "Note: the database"+nl+"\""+fileCmplxSaveName+"\""+nl+" already exists.  Overwrite?",
                                pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
                                javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
                            if(m != javax.swing.JOptionPane.YES_OPTION) {fileCmplxSaveName = null;}
                        }
                    } else { // if it does not end with ".db"
                        fileCmplxSaveName = null;
                    }
                } // if a file name is given
                toBinary(pd.elemComp);
                return;
        } // if it starts with "-bin" or "/bin" 
    } // if length > 5
    String msg = "Error: bad format for"+nl+
                 "   command-line argument: \""+arg+"\"";
    System.out.println(msg);
    printInstructions();
    msgFrame.setVisible(true);
    javax.swing.JOptionPane.showMessageDialog(this,msg,
                pc.progName,javax.swing.JOptionPane.ERROR_MESSAGE);
    setCursorWait();
    Thread hlp = new Thread() {@Override public void run(){
        String[] a = {"S_Batch_htm"};
        //String[] a = {"SP_Batch_Mode_htm"};
        lib.huvud.RunProgr.runProgramInProcess(null,ProgramConf.HELP_JAR,a,false,pc.dbg,pc.pathAPP);
        try{Thread.sleep(1500);}   //show the "wait" cursor for 1.5 sec
        catch (InterruptedException e) {}
        setCursorDef();
    }};//new Thread
    hlp.start();
    doNotExit = true;
  } // dispatchArg(arg)
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="printInstructions">
  public static void printInstructions() {
    System.out.flush();
    String msg = "Possible commands are:"+nl+
    "  -dbg  (output debugging information to the messages window)"+nl+
    "  -bin  (merge all databases to a single binary file;"+nl+
    "         the input databases are those from last run of the program,"+nl+
    "         a list of names is stored in file 'DataMaintenance.ini')"+nl+
    "  -bin:out-file-name  (as for \"-bin\", but the name of the output"+nl+
    "                       database (possibly including a path) is given."+nl+
    "                       Note: out-file-name must end with \".db\")"+nl+
    "Enclose file names with double quotes (\"\") it they contain blank space."+nl+
    "Example:   java -jar DataMaintenance.jar /dbg -bin=\"..\\plt\\db 2.db\"";
    System.out.println(msg);
    System.out.flush();
    System.err.println(LINE); // show the message window
    System.err.flush();
  } //printInstructions()
  //</editor-fold>

//</editor-fold>

 //<editor-fold defaultstate="collapsed" desc="main(args)">
  /**
   * @param args the command line arguments
   */
  public static void main(final String[] args) {
    //---- create a local instance of ProgramConf.
    //     Contains information read from the configuration file.
    //     This variable can not be static because the program might be started
    //     from different locations having different configuration files.
    final ProgramConf pc = new ProgramConf("DataMaintenance");
    pc.saveIniFileToApplicationPathOnly = false;
    //---- deal with some command-line arguments
    boolean dbg = false;
    if(args.length > 0) {
        for (String arg : args) {
            if(arg.equalsIgnoreCase("-dbg") || arg.equalsIgnoreCase("/dbg")) {
                System.out.println("Command-line argument = \"" + arg + "\"");
                dbg = true;
            }
            if(arg.equals("-?") || arg.equalsIgnoreCase("/?")) {
                printInstructions();
            }
        }
    }

    //---- all output to System.err will show the error in a frame.
    if(msgFrame == null) {msgFrame = new RedirectedFrame(500, 400, pc);}

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

    //---- get the Application Path
    pc.pathAPP = Main.getPathApp();

    //---- read the CFG-file of "DataBase"
    java.io.File fileNameCfg;
    String dir = pc.pathAPP;
    if(dir != null && dir.trim().length()>0) {
        if(dir.endsWith(SLASH)) {dir = dir.substring(0, dir.length()-1);}
        fileNameCfg = new java.io.File(dir + SLASH + "DataMaintenance.cfg");
    } else {fileNameCfg = new java.io.File("DataMaintenance.cfg");}
    ProgramConf.read_cfgFile(fileNameCfg, pc);
    if(!pc.dbg) {pc.dbg=dbg;}
    msgFrame.setVisible(dbg);

    //---- set Default Path = Start Directory
    pc.setPathDef(System.getProperty("user.home"));

    //----
    java.text.DateFormat dateFormatter =
            java.text.DateFormat.getDateTimeInstance
                (java.text.DateFormat.DEFAULT, java.text.DateFormat.DEFAULT,
                    java.util.Locale.getDefault());
    java.util.Date today = new java.util.Date();
    String dateOut = dateFormatter.format(today);
    System.out.println("DataMaintenance started: \""+dateOut+"\"");

    //---- show the main window
    java.awt.EventQueue.invokeLater(new Runnable() {@Override public void run() {
          DataMaintenance m = new DataMaintenance(pc); //send configuration data
          m.start(args);
        } // run
    }); // invokeLater

  } //main
//</editor-fold>

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton2binary;
    private javax.swing.JButton jButton2text;
    private javax.swing.JButton jButtonAddData;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JButton jButtonHelp;
    private javax.swing.JButton jButtonOpen;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JButton jButtonSingle;
    private javax.swing.JButton jButtonStatistics;
    private javax.swing.JCheckBox jCheckBoxDebugFrame;
    private javax.swing.JLabel jLabelDataBases;
    private javax.swing.JLabel jLabelNbr;
    private javax.swing.JLabel jLabelSession;
    private javax.swing.JLabel jLabelVersion;
    private javax.swing.JList jListDBlist;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JPanel jPanelOutput;
    private javax.swing.JPanel jPanelShow;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JScrollPane jScrollPaneDBlist;
    // End of variables declaration//GEN-END:variables
}
