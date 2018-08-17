package spana;

import java.util.Arrays;
import lib.common.MsgExceptn;
import lib.common.Util;
import lib.huvud.Div;
import lib.huvud.ProgramConf;
import lib.huvud.RedirectedFrame;
import lib.huvud.SortedProperties;
import lib.kemi.chem.Chem;
import lib.kemi.graph_lib.DiagrPaintUtility;

/** The main frame.
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
public class MainFrame extends javax.swing.JFrame {
  // Note: for java 1.6 jComboBox must not have type,
  //       for java 1.7 jComboBox must be <String>
  static final String VERS = "2018-Aug-14";
  /** all program instances will use the same redirected frame */
  private static RedirectedFrame msgFrame = null;

  /** Because the program checks for other instances and exits if there is
   * another instance, "spf" is a reference to the only instance of
   * this class */
  private static MainFrame spf = null;

  //<editor-fold defaultstate="collapsed" desc="Fields">
  private final ProgramDataSpana pd = new ProgramDataSpana();
  private final ProgramConf pc;
  private DiagrPaintUtility diagrPaintUtil = null;
  private HelpAbout helpAboutFrame = null;

  static boolean windows = false;
  private static String windir = null;
  private static java.awt.Dimension msgFrameSize = new java.awt.Dimension(500,400);
  private static java.awt.Point locationMsgFrame = new java.awt.Point(80,30);
  private static java.awt.Point locationFrame = new java.awt.Point(-1000,-1000);
  protected static java.awt.Point locationSDFrame = new java.awt.Point(-1000,-1000);
  static final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();;
  private java.awt.Dimension windowSize;
  private final java.awt.Dimension ZERO = new java.awt.Dimension(0,0);
  private final java.awt.Dimension PANELsize = new java.awt.Dimension(345,170);
  static java.awt.Dimension dispSize = new java.awt.Dimension(400,350);
  static java.awt.Point dispLocation = new java.awt.Point(60,30);
  static String txtEditor;
  static String pathSedPredom;
  static String createDataFileProg;
  /** used to avoid duplicate entries in the list of data files */
  private final java.util.ArrayList<String> dataFileArrList =
         new java.util.ArrayList<String>();
  /** used to show instances of Disp corresponding to the
   *   entries in the list of plot files */
  private final java.util.ArrayList<spana.Disp> diagrArrList =
          new java.util.ArrayList<spana.Disp>();
  /** do not fire an event when adding an item to the combo box
   * or when setting the selected item whithin the program  */
  private boolean jComboBox_Plt_doNothing = false;

  static java.io.File fileIni;
  private static final String FileINI_NAME = ".Spana.ini";
  /** If <code>laf</code> = 2 then the CrossPlatform look-and-feel is used,
   * else if <code>laf</code> = 1 the System look-and-feel is used.
   * Else (<code>laf</code> = 0) the System look-and-feel is
   * used on Windows and the CrossPlatform is used on Linux and Mac OS.
   * Default at program start = 0 **/
  private int laf = 0;

  private ModifyChemSyst modifyDiagramWindow = null;
  // variables used when dealing with command-line args.
  private boolean doNotExit = false;
  private boolean waitingForPrinter = false;
  public static final double Faraday = 96485.309;
  public static final double Rgas = 8.31451;
  /** The maximum number of calculation steps along an axis */
  final static int MXSTP = 1000;
  /** The minimum number of calculation steps along the X-axis */
  final static int MNSTP = 4;
  /** The default number of calculation steps along an axis */
  final static int NSTEPS_DEF = 50;
  /** New-line character(s) to substitute "\n".<br>
   * It is needed when a String is created first, including new-lines,
   * and the String is then printed. For example
   * <pre>String t = "1st line\n2nd line";
   *System.out.println(t);</pre>will add a carriage return character
   * between the two lines, which on Windows system might be
   * unsatisfactory. Use instead:
   * <pre>String t = "1st line" + nl + "2nd line";
   *System.out.println(t);</pre> */
  private static final String nl = System.getProperty("line.separator");
  private static final String SLASH = java.io.File.separator;
  public static final String[] FORMAT_NAMES;
  public static final String LINE = "- - - - - - - - - - - - - - - - - - - - - - - - - - -";
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="static initializer">
  static { // static initializer
        // Get list of unique supported write formats, e.g. png jpeg jpg
        String[] names = javax.imageio.ImageIO.getWriterFormatNames();
        if(names.length >0) {
            java.util.Set<String> set = new java.util.TreeSet<String>();
            for (String name : names) {set.add(name.toLowerCase());}
            FORMAT_NAMES = (String[])set.toArray(new String[0]);
        } else {FORMAT_NAMES = new String[]{"error"};}
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Creates new form MainFrame
   * @param pc0
   * @param msgFrame0 */
  public MainFrame(ProgramConf pc0, RedirectedFrame msgFrame0) {
    initComponents();
    pc = pc0;
    msgFrame = msgFrame0;
    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    //--- ESC key: with a menu bar, the behaviour of ESC is too complex
    //--- Alt-Q quit
    javax.swing.KeyStroke altQKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altQKeyStroke,"ALT_Q");
    javax.swing.Action altQAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            end_program();
        }};
    getRootPane().getActionMap().put("ALT_Q", altQAction);
    //--- F1 for help
    javax.swing.KeyStroke f1KeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_F1,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(f1KeyStroke,"F1");
    javax.swing.Action f1Action = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jMenu_Help_Contents.doClick();
        }};
    getRootPane().getActionMap().put("F1", f1Action);

    //---- Icon
    String iconName = "images/Spana_icon_32x32.gif";
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
                java.net.URL iconURL = this.getClass().getResource("images/Spana_icon_48x48.gif");
                if (iconURL != null) {icon = new javax.swing.ImageIcon(iconURL).getImage();}
                Object paramsObj[] = new Object[]{icon};
                m.invoke(i, paramsObj);
            } catch (Exception e) {System.out.println("Error: "+e.getMessage());}
        }
    } else {
        System.out.println("Error: Could not load image = \""+iconName+"\"");
    }
    //---- Set up Drag-and-Drop
    jPanel1.setTransferHandler(tHandler);
    jMenuBar.setTransferHandler(tHandler);
    jLabelBackgrd.setTransferHandler(tHandler);
    //---- Title, menus, etc
    this.setTitle(pc.progName+" diagram");
    getContentPane().setBackground(java.awt.Color.white);
    jPanel1.setBackground(java.awt.Color.white);
    jMenuBar.add(javax.swing.Box.createHorizontalGlue(),3); //move "Help" menu to the right
    jMenu_Data_SaveAs.setEnabled(false);
    jMenu_Plot_SaveAs.setEnabled(false);
    //---- Fix the panel and combo boxes
    jLabel_Dat.setVisible(false);
    jComboBox_Dat.setVisible(false);
    jLabel_Plt.setVisible(false);
    jComboBox_Plt.setVisible(false);
    jLabelBackgrd.setVisible(true);
    //---- initial size
    jLabelBackgrd.setSize(PANELsize);
    jLabelBackgrd.setVisible(true);
    jPanel1.setSize(ZERO);
    jPanel1.setVisible(false);
    pack();
    //---- initial location
    locationFrame.x = Math.max(0, (screenSize.width  - this.getWidth() ) / 2);
    locationFrame.y = Math.max(0, (screenSize.height - this.getHeight() ) / 2);
    setLocation(locationFrame);

  } //MainFrame constructor
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="start(args)">
  /** Performs start-up actions that require an "object" of this class to be
   * present, for example actions that may display a message dialog box
   * (because a dialog requires a parent frame).
   * @param args the command-line arguments
   */
  private void start(final String[] args) {
    // the methods and variables in DiagrPaintUtility are used to paint the diagrams
    diagrPaintUtil = new DiagrPaintUtility();
    //---- read the INI-file
    //     (this may display message dialogs)
    readIni();
    //---- Position the window on the screen
    locationFrame.x = Math.min( screenSize.width-this.getWidth()-5,
                                    Math.max(5, locationFrame.x));
    locationFrame.y = Math.min( screenSize.height-this.getHeight()-35,
                                    Math.max(5, locationFrame.y));
    setLocation(locationFrame);

    spf = this;

    if(pc.dbg) {
        System.out.flush();
        StringBuffer msg = new StringBuffer();
        msg.append(LINE); msg.append(nl);
        msg.append("After reading cfg- and INI-files");msg.append(nl);
        msg.append("   and after checking for another instance:");msg.append(nl);
        msg.append("App_Path = ");
        if(pc.pathAPP == null) {
            msg.append("\"null\"");
        } else {
            if(pc.pathAPP.trim().length()<=0) {msg.append("\"\"");}
            else {msg.append(pc.pathAPP);}
        }
        msg.append(nl);
        msg.append("Def_path = ");msg.append(pc.pathDef.toString());msg.append(nl);
        try {
            msg.append("User.dir = ");msg.append(System.getProperty("user.dir"));msg.append(nl);
            msg.append("User.home = ");msg.append(System.getProperty("user.home"));msg.append(nl);
        }
        catch (Exception e) {}
        msg.append("CLASSPATH = ");msg.append(System.getProperty("java.class.path"));msg.append(nl);
        msg.append(LINE);
        System.out.println(msg);
        System.out.flush();
    } // if(pd.dbg)

    //---- set Look-And-Feel
    try{
        if(laf == 2) {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getCrossPlatformLookAndFeelClassName());
            System.out.println("--- setLookAndFeel(CrossPlatform);");
        } else if(laf == 1) {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            System.out.println("--- setLookAndFeel(System);");
        }
    }
    catch (ClassNotFoundException ex) {System.out.println("Error: "+ex.getMessage());}
    catch (IllegalAccessException ex) {System.out.println("Error: "+ex.getMessage());}
    catch (InstantiationException ex) {System.out.println("Error: "+ex.getMessage());}
    catch (javax.swing.UnsupportedLookAndFeelException ex) {System.out.println("Error: "+ex.getMessage());}
    javax.swing.SwingUtilities.updateComponentTreeUI(spf);
    spf.invalidate();spf.validate();spf.repaint();
    javax.swing.SwingUtilities.updateComponentTreeUI(msgFrame);
    msgFrame.invalidate();msgFrame.validate();msgFrame.repaint();
    if(pc.dbg) {System.out.println("--- configureOptionPane();");}
    Util.configureOptionPane();

    setVisible(true);
    windowSize = MainFrame.this.getSize();

    //---- at this point the INI-file (if it exists) has been read.
    if(txtEditor == null || txtEditor.trim().length() <=0) {jMenu_Data_Edit.setVisible(false);}
    //
    if(msgFrame != null) {
        msgFrame.setLocation(locationMsgFrame);
        msgFrame.setSize(msgFrameSize);
        msgFrame.setParentFrame(spf);
        jCheckBoxMenuDebug.setSelected(msgFrame.isVisible());
    } else {jCheckBoxMenuDebug.setVisible(false);}
    dispLocation.x = dispLocation.x - 20;
    dispLocation.y = dispLocation.y - 20;
    //
    if(!pd.advancedVersion) {
        jMenu_Run_FileExpl.setVisible(false);
        jMenu_Run_Cmd.setVisible(false);
        jSeparatorCmd.setVisible(false);
    } else {
        jMenu_Run_FileExpl.setVisible(windows);
    }
    if(!pd.advancedVersion) {jMenu_Prefs_Calcs.setVisible(false);}
    else {jMenu_Prefs_Calcs.setVisible(true);}

    java.io.File f;
    if(createDataFileProg != null && createDataFileProg.trim().length()>0) {
        f = new java.io.File(createDataFileProg);
        if(!f.exists()) {jMenu_Run_Database.setEnabled(false);}
    } else {jMenu_Run_Database.setEnabled(false);}
    if(pathSedPredom != null) {
        f = new java.io.File(pathSedPredom);
        if(!Div.progSEDexists(f) && !Div.progPredomExists(f)) {
            jMenu_Run_MakeDiagr.setEnabled(false);
            jMenu_Data_Open.setEnabled(false);
        }
    } else {
        jMenu_Run_MakeDiagr.setEnabled(false);
        jMenu_Data_Open.setEnabled(false);
    }
    f = new java.io.File(pc.pathAPP+SLASH+ProgramConf.HELP_JAR);
    if(!f.exists()) {jMenu_Help_Contents.setEnabled(false);}

    //---- deal with command-line arguments
    if(args != null && args.length >0){
        Thread dArg = new Thread() {@Override public void run(){
            for(String arg : args) {
                dispatchArg(arg);
            }
            // do not end the program if an error occurred
            if(msgFrame == null || !msgFrame.isVisible()) {
                if(!doNotExit) {end_program();}
            }
        }}; //new Thread
        dArg.start();
    } // args != null
  } // start(args)
  // </editor-fold>

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelBackgrd = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel_Dat = new javax.swing.JLabel();
        jComboBox_Dat = new javax.swing.JComboBox<String>();
        jPanel3 = new javax.swing.JPanel();
        jLabel_Plt = new javax.swing.JLabel();
        jComboBox_Plt = new javax.swing.JComboBox<String>();
        jMenuBar = new javax.swing.JMenuBar();
        jMenu_File = new javax.swing.JMenu();
        jMenu_File_Data = new javax.swing.JMenu();
        jMenu_Data_Open = new javax.swing.JMenuItem();
        jMenu_Data_New = new javax.swing.JMenuItem();
        jMenu_Data_Modif = new javax.swing.JMenuItem();
        jMenu_Data_Edit = new javax.swing.JMenuItem();
        jMenu_Data_AddToList = new javax.swing.JMenuItem();
        jMenu_Data_SaveAs = new javax.swing.JMenuItem();
        jMenu_File_Plot = new javax.swing.JMenu();
        jMenu_Plot_Open = new javax.swing.JMenuItem();
        jMenu_Plot_SaveAs = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenu_File_Exit = new javax.swing.JMenuItem();
        jMenu_Run = new javax.swing.JMenu();
        jMenu_Run_FileExpl = new javax.swing.JMenuItem();
        jMenu_Run_Cmd = new javax.swing.JMenuItem();
        jSeparatorCmd = new javax.swing.JPopupMenu.Separator();
        jMenu_Run_Modif = new javax.swing.JMenuItem();
        jMenu_Run_MakeDiagr = new javax.swing.JMenuItem();
        jSeparatorMake = new javax.swing.JPopupMenu.Separator();
        jMenu_Run_Database = new javax.swing.JMenuItem();
        jMenu_Prefs = new javax.swing.JMenu();
        jMenu_Prefs_General = new javax.swing.JMenuItem();
        jMenu_Prefs_Diagr = new javax.swing.JMenuItem();
        jMenu_Prefs_Calcs = new javax.swing.JMenuItem();
        jCheckBoxMenuDebug = new javax.swing.JCheckBoxMenuItem();
        jMenu_Help = new javax.swing.JMenu();
        jMenu_Help_Contents = new javax.swing.JMenuItem();
        jMenu_Help_About = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setMinimumSize(new java.awt.Dimension(345, 170));
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        jLabelBackgrd.setBackground(new java.awt.Color(255, 255, 255));
        jLabelBackgrd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/spana/images/Spana_diagram.gif"))); // NOI18N
        jLabelBackgrd.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabelBackgrd.setAlignmentY(0.0F);
        jLabelBackgrd.setMinimumSize(new java.awt.Dimension(345, 160));
        jLabelBackgrd.setOpaque(true);
        jLabelBackgrd.setPreferredSize(new java.awt.Dimension(345, 160));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setMinimumSize(new java.awt.Dimension(345, 160));
        jPanel1.setPreferredSize(new java.awt.Dimension(345, 160));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setPreferredSize(new java.awt.Dimension(0, 66));

        jLabel_Dat.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel_Dat.setText("Data files:");

        jComboBox_Dat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_DatActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel_Dat)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jComboBox_Dat, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel_Dat)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox_Dat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setPreferredSize(new java.awt.Dimension(0, 66));

        jLabel_Plt.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel_Plt.setText("Plot files:");

        jComboBox_Plt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_PltActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel_Plt)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jComboBox_Plt, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel_Plt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addComponent(jComboBox_Plt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        jMenu_File.setMnemonic('F');
        jMenu_File.setText("File");

        jMenu_File_Data.setMnemonic('D');
        jMenu_File_Data.setText("Data file");

        jMenu_Data_Open.setMnemonic('O');
        jMenu_Data_Open.setText("Open (make a diagram)");
        jMenu_Data_Open.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_Data_OpenActionPerformed(evt);
            }
        });
        jMenu_File_Data.add(jMenu_Data_Open);

        jMenu_Data_New.setMnemonic('N');
        jMenu_Data_New.setText("New (create)");
        jMenu_Data_New.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_Data_NewActionPerformed(evt);
            }
        });
        jMenu_File_Data.add(jMenu_Data_New);

        jMenu_Data_Modif.setMnemonic('M');
        jMenu_Data_Modif.setText("Modify");
        jMenu_Data_Modif.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_Data_ModifActionPerformed(evt);
            }
        });
        jMenu_File_Data.add(jMenu_Data_Modif);

        jMenu_Data_Edit.setMnemonic('E');
        jMenu_Data_Edit.setText("Edit");
        jMenu_Data_Edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_Data_EditActionPerformed(evt);
            }
        });
        jMenu_File_Data.add(jMenu_Data_Edit);

        jMenu_Data_AddToList.setMnemonic('A');
        jMenu_Data_AddToList.setText("Add name to list");
        jMenu_Data_AddToList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_Data_AddToListActionPerformed(evt);
            }
        });
        jMenu_File_Data.add(jMenu_Data_AddToList);

        jMenu_Data_SaveAs.setMnemonic('S');
        jMenu_Data_SaveAs.setText("Save as");
        jMenu_Data_SaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_Data_SaveAsActionPerformed(evt);
            }
        });
        jMenu_File_Data.add(jMenu_Data_SaveAs);

        jMenu_File.add(jMenu_File_Data);

        jMenu_File_Plot.setMnemonic('P');
        jMenu_File_Plot.setText("Plot file");

        jMenu_Plot_Open.setMnemonic('O');
        jMenu_Plot_Open.setText("Open (display)");
        jMenu_Plot_Open.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_Plot_OpenActionPerformed(evt);
            }
        });
        jMenu_File_Plot.add(jMenu_Plot_Open);

        jMenu_Plot_SaveAs.setMnemonic('S');
        jMenu_Plot_SaveAs.setText("Save as");
        jMenu_Plot_SaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_Plot_SaveAsActionPerformed(evt);
            }
        });
        jMenu_File_Plot.add(jMenu_Plot_SaveAs);

        jMenu_File.add(jMenu_File_Plot);
        jMenu_File.add(jSeparator1);

        jMenu_File_Exit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK));
        jMenu_File_Exit.setMnemonic('X');
        jMenu_File_Exit.setText("Exit");
        jMenu_File_Exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_File_ExitActionPerformed(evt);
            }
        });
        jMenu_File.add(jMenu_File_Exit);

        jMenuBar.add(jMenu_File);

        jMenu_Run.setMnemonic('R');
        jMenu_Run.setText("Run");

        jMenu_Run_FileExpl.setMnemonic('E');
        jMenu_Run_FileExpl.setText("file Explorer");
        jMenu_Run_FileExpl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_Run_FileExplActionPerformed(evt);
            }
        });
        jMenu_Run.add(jMenu_Run_FileExpl);

        jMenu_Run_Cmd.setMnemonic('P');
        jMenu_Run_Cmd.setText("command Prompt");
        jMenu_Run_Cmd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_Run_CmdActionPerformed(evt);
            }
        });
        jMenu_Run.add(jMenu_Run_Cmd);
        jMenu_Run.add(jSeparatorCmd);

        jMenu_Run_Modif.setMnemonic('M');
        jMenu_Run_Modif.setText("Modify chemical system");
        jMenu_Run_Modif.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_Run_ModifActionPerformed(evt);
            }
        });
        jMenu_Run.add(jMenu_Run_Modif);

        jMenu_Run_MakeDiagr.setMnemonic('D');
        jMenu_Run_MakeDiagr.setText("make a Diagram");
        jMenu_Run_MakeDiagr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_Run_MakeDiagrActionPerformed(evt);
            }
        });
        jMenu_Run.add(jMenu_Run_MakeDiagr);
        jMenu_Run.add(jSeparatorMake);

        jMenu_Run_Database.setMnemonic('L');
        jMenu_Run_Database.setText("LogK Database");
        jMenu_Run_Database.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_Run_DatabaseActionPerformed(evt);
            }
        });
        jMenu_Run.add(jMenu_Run_Database);

        jMenuBar.add(jMenu_Run);

        jMenu_Prefs.setMnemonic('P');
        jMenu_Prefs.setText("Preferences");

        jMenu_Prefs_General.setMnemonic('G');
        jMenu_Prefs_General.setText("General options");
        jMenu_Prefs_General.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_Prefs_GeneralActionPerformed(evt);
            }
        });
        jMenu_Prefs.add(jMenu_Prefs_General);

        jMenu_Prefs_Diagr.setMnemonic('D');
        jMenu_Prefs_Diagr.setText("Diagram windows");
        jMenu_Prefs_Diagr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_Prefs_DiagrActionPerformed(evt);
            }
        });
        jMenu_Prefs.add(jMenu_Prefs_Diagr);

        jMenu_Prefs_Calcs.setMnemonic('C');
        jMenu_Prefs_Calcs.setText("Calculation options");
        jMenu_Prefs_Calcs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_Prefs_CalcsActionPerformed(evt);
            }
        });
        jMenu_Prefs.add(jMenu_Prefs_Calcs);

        jCheckBoxMenuDebug.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK));
        jCheckBoxMenuDebug.setMnemonic('S');
        jCheckBoxMenuDebug.setText("Show messages and errors");
        jCheckBoxMenuDebug.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuDebugActionPerformed(evt);
            }
        });
        jMenu_Prefs.add(jCheckBoxMenuDebug);

        jMenuBar.add(jMenu_Prefs);

        jMenu_Help.setMnemonic('H');
        jMenu_Help.setText("Help");

        jMenu_Help_Contents.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        jMenu_Help_Contents.setMnemonic('C');
        jMenu_Help_Contents.setText("help Contents");
        jMenu_Help_Contents.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_Help_ContentsActionPerformed(evt);
            }
        });
        jMenu_Help.add(jMenu_Help_Contents);

        jMenu_Help_About.setMnemonic('A');
        jMenu_Help_About.setText("About");
        jMenu_Help_About.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu_Help_AboutActionPerformed(evt);
            }
        });
        jMenu_Help.add(jMenu_Help_About);

        jMenuBar.add(jMenu_Help);

        setJMenuBar(jMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelBackgrd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabelBackgrd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        if(msgFrame != null && msgFrame.isVisible()) {
            jCheckBoxMenuDebug.setSelected(true);
        }
    }//GEN-LAST:event_formWindowActivated

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        end_program();
    }//GEN-LAST:event_formWindowClosing

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        if(helpAboutFrame != null) {helpAboutFrame.bringToFront();}
        if(msgFrame != null) {jCheckBoxMenuDebug.setSelected(msgFrame.isVisible());}
        else {jCheckBoxMenuDebug.setVisible(false);}
    }//GEN-LAST:event_formWindowGainedFocus

    private void jComboBox_DatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_DatActionPerformed
        if(jComboBox_Dat.getSelectedIndex() < 0) {return;}
        String name = jComboBox_Dat.getSelectedItem().toString();
        java.io.File datF = new java.io.File(name);
        if(!datF.exists()) {removeDatFile(name);} else {pc.setPathDef(datF);}
    }//GEN-LAST:event_jComboBox_DatActionPerformed

    private void jComboBox_PltActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_PltActionPerformed
        if(jComboBox_Plt_doNothing) {return;}
        int k = jComboBox_Plt.getSelectedIndex();
        if(k < 0) {return;}
        String name = jComboBox_Plt.getItemAt(k).toString();
        java.io.File pltF = new java.io.File(name);
        if(!pltF.exists()) {removePltFile(name); return;}
        displayPlotFile(name, null);
    }//GEN-LAST:event_jComboBox_PltActionPerformed

    private void jMenu_Data_OpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_Data_OpenActionPerformed
        setCursorWait();
        String fileName = Util.getOpenFileName(this, pc.progName, true,
                "Enter Data file name", 5, null, pc.pathDef.toString());
        if(fileName != null) {
            setCursorWait();
            java.io.File dataFile = new java.io.File(fileName);
            if(dataFile.exists() && (!dataFile.canWrite() || !dataFile.setWritable(true))) {
                String msg = "Warning - the file:"+nl+
                  "   \""+dataFile.getPath()+"\""+nl+
                  "is write-protected!"+nl+nl+
                  "It might be best to copy the file"+nl+
                  "to a writable location"+nl+
                  "before going ahead...";
                System.out.println(LINE+nl+msg+nl+LINE);
                Object[] opt = {"Go ahead anyway", "Cancel"};
                int m = javax.swing.JOptionPane.showOptionDialog(this, msg,
                    pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
                if(m != javax.swing.JOptionPane.YES_OPTION) {setCursorDef(); return;}
            }
            addDatFile(fileName);
            if(jMenu_Run_MakeDiagr.isEnabled()) {jMenu_Run_MakeDiagr.doClick();}
        }
    }//GEN-LAST:event_jMenu_Data_OpenActionPerformed

    private void jMenu_Data_NewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_Data_NewActionPerformed
        setCursorWait();
        run_Database();
    }//GEN-LAST:event_jMenu_Data_NewActionPerformed

    private void jMenu_Data_ModifActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_Data_ModifActionPerformed
        setCursorWait();
        modifyDataFile();
    }//GEN-LAST:event_jMenu_Data_ModifActionPerformed

    private void jMenu_Data_EditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_Data_EditActionPerformed
      if(txtEditor == null) {return;}
      setCursorWait();
      String fileName;
      //---- is the data-files combo box empty? if so, get a file name
      if(dataFileArrList.size() <=0) {
        // -- get a file name through an Open File Dialog
        fileName = Util.getOpenFileName(this, pc.progName, true,
                "Enter Data file name", 5, null, pc.pathDef.toString());
        if(fileName == null) {setCursorDef(); return;}
      } // if dataFileArrList.size() <=0
      else {
        // -- get the file name selected in the combo box
        String defFile = jComboBox_Dat.getSelectedItem().toString();
        // -- confirm the file name through an Open File Dialog
        fileName = Util.getOpenFileName(this, pc.progName, true,
                "Enter Data file name", 5, defFile, pc.pathDef.toString());
        if(fileName == null) {setCursorDef(); return;}
      }
      setCursorWait();
      //---- get the full name, with path
      java.io.File datFile = new java.io.File(fileName);
      try{fileName = datFile.getCanonicalPath();}
      catch (java.io.IOException ex) {
          try{fileName = datFile.getAbsolutePath();}
          catch (Exception e) {fileName = datFile.getPath();}
      }
      //---- add the file name to the combo-box
      addDatFile(fileName);
      //---- start the editor on a separate process
      final String editor;
      final String[] args;
      if(System.getProperty("os.name").startsWith("Mac OS")
              && txtEditor.equalsIgnoreCase("/usr/bin/open -t")) {
          editor = "/usr/bin/open";
          args = new String[]{"-t",fileName};
      } else { // not MacOS
          editor = txtEditor;
          args = new String[]{fileName};
      }
      setCursorWait();
      Thread edt = new Thread() {@Override public void run(){
          boolean waitForCompletion = false;
          lib.huvud.RunProgr.runProgramInProcess(null, editor, args,
              waitForCompletion, pc.dbg, pc.pathDef.toString());
            try{Thread.sleep(1000);}    //show the "wait" cursor for 1 sec
            catch (InterruptedException e) {}
            setCursorDef();
        }};//new Thread
      edt.start();
    }//GEN-LAST:event_jMenu_Data_EditActionPerformed

    private void jMenu_Data_AddToListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_Data_AddToListActionPerformed
        setCursorWait();
        String fileName = Util.getOpenFileName(this, pc.progName, true,
                "Enter Data file name", 5, null, pc.pathDef.toString());
        if(fileName != null && fileName.trim().length() >0) {
            setCursorWait();
            java.io.File dataFile = new java.io.File(fileName);
            if(dataFile.exists() && (!dataFile.canWrite() || !dataFile.setWritable(true))) {
                String msg = "Warning - the file:"+nl+
                  "   \""+dataFile.getPath()+"\""+nl+
                  "is write-protected!"+nl+nl+
                  "It might be best to copy the file"+nl+
                  "to a writable location"+nl+
                  "before going ahead...";
                System.out.println(LINE+nl+msg+nl+LINE);
                Object[] opt = {"Go ahead anyway", "Cancel"};
                int m = javax.swing.JOptionPane.showOptionDialog(this, msg,
                    pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
                if(m != javax.swing.JOptionPane.YES_OPTION) {setCursorDef(); return;}
            }
            addDatFile(fileName);
        }
        setCursorDef();
    }//GEN-LAST:event_jMenu_Data_AddToListActionPerformed

    private void jMenu_File_ExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_File_ExitActionPerformed
        end_program();
    }//GEN-LAST:event_jMenu_File_ExitActionPerformed

    private void jMenu_Run_FileExplActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_Run_FileExplActionPerformed
        if(!windows) {return;}
        setCursorWait();
        Runtime r = Runtime.getRuntime();
        try{ //%windir%\explorer.exe /n, /e, m:\
            String t = windir;
            if(t != null && t.endsWith(SLASH)) {t = t.substring(0, t.length()-1);}
            if(t != null) {t = t + SLASH + "explorer.exe";} else {t = "explorer.exe";}
            String[] a = {t,"/n,","/e,",pc.pathDef.toString()};
            java.io.File d = new java.io.File(pc.pathDef.toString());
            if(pc.dbg) {System.out.println("---- Exec "+Arrays.toString(a)+nl+
                               "     working dir: "+d);}
            r.exec( a, null,  d);
        } catch (java.io.IOException ex) {
            MsgExceptn.exception("Error: "+ex.toString());
        } //catch
        setCursorDef();
    }//GEN-LAST:event_jMenu_Run_FileExplActionPerformed

    private void jMenu_Run_CmdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_Run_CmdActionPerformed
        setCursorWait();
        String OS = System.getProperty("os.name").toLowerCase();
        // System.out.println(OS);
        Runtime r = Runtime.getRuntime();
        try{
          if(OS.indexOf("windows 9") > -1) {
            if(pc.dbg) {System.out.println("---- Exec \"command.com  /e:1024\"");}
            r.exec( "command.com  /e:1024" );
            } //Windows 9x
          else if (OS.startsWith("windows")) {
                String[] a = {"cmd.exe", "/k start cmd.exe"};
                java.io.File d = new java.io.File(pc.pathDef.toString());
                if(pc.dbg) {System.out.println("---- Exec "+Arrays.toString(a)+nl+
                               "     working dir: "+d);}
                r.exec( a, null,  d);
            } //Windows
          else if(OS.startsWith("mac os")) {
                final String [] args;
                java.io.File term = new java.io.File("/Applications/Utilities/Terminal.app");
                if(!term.exists()) {
                    MsgExceptn.exception("---- Error - file:"+term.getAbsolutePath()+nl+
                                       "     does NOT exist");
                } else {
                    if(pc.dbg) {System.out.println(
                            "---- Exec \"/usr/bin/open -n "+term.getAbsolutePath()+"\"");}
                    args = new String[]{"-n",term.getAbsolutePath()};
                    setCursorWait();
                    Thread cmd = new Thread() {@Override public void run(){
                        boolean waitForCompletion = false;
                        lib.huvud.RunProgr.runProgramInProcess(null, "/usr/bin/open", args,
                            waitForCompletion, pc.dbg, pc.pathDef.toString());
                        try{Thread.sleep(1000);}   //show the "wait" cursor for 1 sec
                        catch (InterruptedException e) {}
                        setCursorDef();
                    }};//new Thread
                    cmd.start();
                }
          } else {
            // our last hope, we assume Unix
            if(pc.dbg) {System.out.println("---- Exec \"/usr/bin/xterm\"");}
            r.exec( "/usr/bin/xterm" );
          } //unix
        } catch (java.io.IOException ex) {
                MsgExceptn.exception("Error: "+ex.toString());
        } //catch
        setCursorDef();
    }//GEN-LAST:event_jMenu_Run_CmdActionPerformed

    private void jMenu_Run_ModifActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_Run_ModifActionPerformed
        setCursorWait();
        modifyDataFile();
    }//GEN-LAST:event_jMenu_Run_ModifActionPerformed

    private void jMenu_Run_MakeDiagrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_Run_MakeDiagrActionPerformed
      setCursorWait();
      //---- is the data-files combo box empty? if so, get a file name
      if(dataFileArrList.size() <=0) {
        String fileName = Util.getOpenFileName(this, pc.progName, true,
                "Enter data file name", 5, null, pc.pathDef.toString());
        if(fileName == null) {setCursorDef(); return;}
        java.io.File dataFile = new java.io.File(fileName);
        if(dataFile.exists() && (!dataFile.canWrite() || !dataFile.setWritable(true))) {
            String msg = "Warning - the file:"+nl+
              "   \""+dataFile.getPath()+"\""+nl+
              "is write-protected!"+nl+nl+
              "It might be best to copy the file"+nl+
              "to a writable location"+nl+
              "before going ahead...";
            System.out.println(LINE+nl+msg+nl+LINE);
            Object[] opt = {"Go ahead anyway", "Cancel"};
            int m = javax.swing.JOptionPane.showOptionDialog(this, msg,
                pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
            if(m != javax.swing.JOptionPane.YES_OPTION) {setCursorDef(); return;}
        }
        if(!addDatFile(fileName)) {setCursorDef(); return;}
      } // if dataFileArrList.size() <=0
      else {
          String name = jComboBox_Dat.getSelectedItem().toString();
          java.io.File datF = new java.io.File(name);
          if(!datF.exists()) {
              removeDatFile(name);
              String fileName = Util.getOpenFileName(this, pc.progName, true,
                    "Enter data file name", 5, null, pc.pathDef.toString());
              if(fileName == null) {setCursorDef(); return;}
              if(!addDatFile(fileName)) {setCursorDef(); return;}
          }
      }
      setCursorWait();
      // ---- get the file name selected in the combo box
      final java.io.File datFile = new java.io.File(jComboBox_Dat.getSelectedItem().toString());
      jMenu_Run_MakeDiagr.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
      final boolean jMenu_Prefs_General_setEnabled = jMenu_Prefs_General.isEnabled();
      final boolean jMenu_Run_MakeDiagr_setEnabled = jMenu_Run_MakeDiagr.isEnabled();
      final boolean jMenu_Data_Open_setEnabled = jMenu_Data_Open.isEnabled();
      final boolean jMenu_Run_Modif_setEnabled = jMenu_Run_Modif.isEnabled();
      final boolean jMenu_Data_Modif_setEnabled = jMenu_Data_Modif.isEnabled();
      final boolean jMenu_Data_Edit_setEnabled = jMenu_Data_Edit.isEnabled();
      jMenu_Prefs_General.setEnabled(false);
      jMenu_Run_MakeDiagr.setEnabled(false);
      jMenu_Data_Open.setEnabled(false);
      jMenu_Run_Modif.setEnabled(false);
      jMenu_Data_Modif.setEnabled(false);
      jMenu_Data_Edit.setEnabled(false);
      // ---- Going to wait for another frame: Start a thread
      new javax.swing.SwingWorker<Void,Void>() {
          @Override protected Void doInBackground() throws Exception {
            Select_Diagram selectDiagramWindow = new Select_Diagram(datFile, pc, pd);
            selectDiagramWindow.start();
            spf.setCursorDef();
            jMenu_Run_MakeDiagr.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            selectDiagramWindow.waitForSelectDiagram();
            return null;
          }
          @Override protected void done(){
            jMenu_Prefs_General.setEnabled(jMenu_Prefs_General_setEnabled);
            jMenu_Run_MakeDiagr.setEnabled(jMenu_Run_MakeDiagr_setEnabled);
            jMenu_Data_Open.setEnabled(jMenu_Data_Open_setEnabled);
            jMenu_Run_Modif.setEnabled(jMenu_Run_Modif_setEnabled);
            jMenu_Data_Modif.setEnabled(jMenu_Data_Modif_setEnabled);
            jMenu_Data_Edit.setEnabled(jMenu_Data_Edit_setEnabled);
          }
      }.execute();
    }//GEN-LAST:event_jMenu_Run_MakeDiagrActionPerformed

    private void jMenu_Run_DatabaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_Run_DatabaseActionPerformed
        setCursorWait();
        run_Database();
    }//GEN-LAST:event_jMenu_Run_DatabaseActionPerformed

    private void jMenu_Prefs_GeneralActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_Prefs_GeneralActionPerformed
        setCursorWait();
        final boolean jMenu_Prefs_General_setEnabled = jMenu_Prefs_General.isEnabled();
        final boolean jMenu_Prefs_Calcs_setEnabled = jMenu_Prefs_Calcs.isEnabled();
        final boolean jMenu_Run_MakeDiagr_setEnabled = jMenu_Run_MakeDiagr.isEnabled();
        final boolean jMenu_Run_Modif_setEnabled = jMenu_Run_Modif.isEnabled();
        final boolean jMenu_Data_Modif_setEnabled = jMenu_Data_Modif.isEnabled();
        final boolean jMenu_Data_Open_setEnabled = jMenu_Data_Open.isEnabled();
        final boolean jMenu_Run_Database_setEnabled = jMenu_Run_Database.isEnabled();
        jMenu_Prefs_General.setEnabled(false);
        jMenu_Prefs_Calcs.setEnabled(false);
        jMenu_Run_MakeDiagr.setEnabled(false);
        jMenu_Run_Modif.setEnabled(false);
        jMenu_Data_Modif.setEnabled(false);
        jMenu_Data_Open.setEnabled(false);
        Thread genO = new Thread() {@Override public void run(){
          OptionsGeneral genOptionsFrame = new OptionsGeneral(pc,pd,msgFrame);
          spf.setCursorDef();
          genOptionsFrame.waitFor();
          javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
              jMenu_Prefs_General.setEnabled(jMenu_Prefs_General_setEnabled);
              jMenu_Prefs_Calcs.setEnabled(jMenu_Prefs_Calcs_setEnabled);
              jMenu_Run_MakeDiagr.setEnabled(jMenu_Run_MakeDiagr_setEnabled);
              jMenu_Run_Modif.setEnabled(jMenu_Run_Modif_setEnabled);
              jMenu_Data_Modif.setEnabled(jMenu_Data_Modif_setEnabled);
              jMenu_Data_Open.setEnabled(jMenu_Data_Open_setEnabled);
              if(!pd.advancedVersion) {
                  jMenu_Run_Cmd.setVisible(false);
                  jMenu_Run_FileExpl.setVisible(false);
                  jSeparatorCmd.setVisible(false);
                  jMenu_Prefs_Calcs.setVisible(false);
              } else {
                  jMenu_Run_Cmd.setVisible(true);
                  jMenu_Run_FileExpl.setVisible(windows);
                  jSeparatorCmd.setVisible(true);
                  jMenu_Prefs_Calcs.setVisible(true);
              }
              java.io.File file;
              if(createDataFileProg != null && createDataFileProg.trim().length() >0) {
                  file = new java.io.File(createDataFileProg);
                  if(!file.exists()) {
                      System.out.println("Note: file does NOT exist: "+createDataFileProg);
                      jMenu_Run_Database.setEnabled(false);
                  } else {jMenu_Run_Database.setEnabled(jMenu_Run_Database_setEnabled);}
              } else {jMenu_Run_Database.setEnabled(false);}
              if(pathSedPredom != null) {
                  file = new java.io.File(pathSedPredom);
                  if(!Div.progSEDexists(file) && !Div.progPredomExists(file)) {
                      jMenu_Run_MakeDiagr.setEnabled(false);
                      jMenu_Data_Open.setEnabled(false);
                  }
              } else {
                  jMenu_Run_MakeDiagr.setEnabled(false);
                  jMenu_Data_Open.setEnabled(false);
              }
              java.awt.Frame[] f = Disp.getFrames();
              if(diagrArrList.size() >0 && f.length >0) {
                for(int k=0; k<diagrArrList.size(); k++) {
                  Disp gotDisp = diagrArrList.get(k);
                    for(java.awt.Frame f1 : f) {
                        if (f1.equals(gotDisp)) {
                            if(gotDisp.getExtendedState()!=javax.swing.JFrame.ICONIFIED) { // minimised?
                                gotDisp.setAdvancedFeatures(pd.advancedVersion);
                                gotDisp.repaint();
                            } }
                    } // for f1 : f
                } // for k
              } //if diagrArrList.size() >0 & f.length >0
            }}); //invokeLater(Runnable)
        }};//new Thread
        genO.start();
    }//GEN-LAST:event_jMenu_Prefs_GeneralActionPerformed

    private void jMenu_Prefs_DiagrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_Prefs_DiagrActionPerformed
        setCursorWait();
        jMenu_Prefs_Diagr.setEnabled(false);
        final boolean oldTextWithFonts = diagrPaintUtil.textWithFonts;
        Thread diagPrefs = new Thread() {@Override public void run(){
            OptionsDiagram optionsWindow = new OptionsDiagram(diagrPaintUtil, pc);
            optionsWindow.setVisible(true);
            spf.setCursorDef();
            optionsWindow.waitFor();
            javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
              //take these actions on the Event Dispatch Thread
              jMenu_Prefs_Diagr.setEnabled(true);
              java.awt.Frame[] f = Disp.getFrames();
              if(diagrArrList.size() >0 && f.length >0) {
                for(int k=0; k<diagrArrList.size(); k++) {
                  Disp gotDisp = diagrArrList.get(k);
                    for(java.awt.Frame f1 : f) {
                        if (f1.equals(gotDisp)) {
                            if(oldTextWithFonts != diagrPaintUtil.textWithFonts)
                            {gotDisp.reloadPlotFile();}
                            if(gotDisp.getExtendedState()!=javax.swing.JFrame.ICONIFIED) // minimised?
                            {gotDisp.repaint();}
                        }
                    } // for f1 : f
                } // for k
              } //if diagrArrList.size() >0 & f.length >0
            }}); //invokeLater(Runnable)
        }};//new Thread
        diagPrefs.start();
    }//GEN-LAST:event_jMenu_Prefs_DiagrActionPerformed

    private void jMenu_Prefs_CalcsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_Prefs_CalcsActionPerformed
        setCursorWait();
        jMenu_Prefs_Calcs.setEnabled(false);
        final boolean jMenu_Prefs_General_setEnabled = jMenu_Prefs_General.isEnabled();
        final boolean jMenu_Run_MakeDiagr_setEnabled = jMenu_Run_MakeDiagr.isEnabled();
        final boolean jMenu_Data_Open_setEnabled = jMenu_Data_Open.isEnabled();
        jMenu_Prefs_General.setEnabled(false);
        jMenu_Run_MakeDiagr.setEnabled(false);
        jMenu_Data_Open.setEnabled(false);
        Thread calcO = new Thread() {@Override public void run(){
            OptionsCalcs calcsOptionsFrame = new OptionsCalcs(pc,pd);
            calcsOptionsFrame.setVisible(true);
            spf.setCursorDef();
            calcsOptionsFrame.waitFor();
            javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
                jMenu_Prefs_Calcs.setEnabled(true);
                jMenu_Prefs_General.setEnabled(jMenu_Prefs_General_setEnabled);
                jMenu_Run_MakeDiagr.setEnabled(jMenu_Run_MakeDiagr_setEnabled);
                jMenu_Data_Open.setEnabled(jMenu_Data_Open_setEnabled);
            }}); //invokeLater(Runnable)
        }};//new Thread
        calcO.start();
    }//GEN-LAST:event_jMenu_Prefs_CalcsActionPerformed

    private void jCheckBoxMenuDebugActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuDebugActionPerformed
        if(msgFrame != null) {msgFrame.setVisible(jCheckBoxMenuDebug.isSelected());}
    }//GEN-LAST:event_jCheckBoxMenuDebugActionPerformed

    private void jMenu_Help_ContentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_Help_ContentsActionPerformed
        setCursorWait();
        Thread hlp = new Thread() {@Override public void run(){
            String[] a = {"S_0_Main_htm"};
            lib.huvud.RunProgr.runProgramInProcess(null,ProgramConf.HELP_JAR,a,false,pc.dbg,pc.pathAPP);
            try{Thread.sleep(1500);}   //show the "wait" cursor for 1.5 sec
            catch (InterruptedException e) {}
            setCursorDef();
        }};//new Thread
        hlp.start();
    }//GEN-LAST:event_jMenu_Help_ContentsActionPerformed

    private void jMenu_Help_AboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_Help_AboutActionPerformed
        setCursorWait();
        jMenu_Help_About.setEnabled(false);
        // -- although HelpAbout is a frame, it behaves almost as a modal dialog
        //    because it is brought to focus when "this" gains focus
        Thread hlp = new Thread() {@Override public void run(){
            spf.helpAboutFrame = new HelpAbout(pc.pathAPP);
            spf.helpAboutFrame.start();
            spf.setCursorDef();
            spf.helpAboutFrame.waitFor();
            javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
                spf.helpAboutFrame = null;
                jMenu_Help_About.setEnabled(true);
                bringToFront();
            }}); //invokeLater(Runnable)
        }};//new Thread
        hlp.start();
    }//GEN-LAST:event_jMenu_Help_AboutActionPerformed

    private void jMenu_Plot_OpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_Plot_OpenActionPerformed
        setCursorWait();
        String pltFileName = Util.getOpenFileName(this, pc.progName, true,
                "Enter Plot file name", 6, null, pc.pathDef.toString());
        if(pltFileName == null) {setCursorDef(); return;}
        setCursorWait();
        java.io.File pltFile = new java.io.File(pltFileName);
        try{pltFileName = pltFile.getCanonicalPath();}
        catch (java.io.IOException ex) {
            try{pltFileName = pltFile.getAbsolutePath();}
            catch (Exception e) {pltFileName = pltFile.getPath();}
        }
        displayPlotFile(pltFileName, null);
        setCursorDef();
    }//GEN-LAST:event_jMenu_Plot_OpenActionPerformed

    private void jMenu_Plot_SaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_Plot_SaveAsActionPerformed
        if(pc.dbg) {System.out.println("--- Plot file \"save as\"");}
        String fileName;
        java.io.File f, newF;
        if(jComboBox_Plt.getSelectedIndex() < 0) {
            if(pc.dbg) {System.out.println("    jComboBox_Plt.getSelectedIndex() < 0"+nl+
                                           "    canceling \"save as\".");}
            return;
        } else {
            fileName = jComboBox_Plt.getSelectedItem().toString();
            if(pc.dbg) {System.out.println("    jComboBox_Plt.getSelectedItem() = "+fileName);}
            f = new java.io.File(fileName);
            if(!f.exists()) {
                if(pc.dbg) {System.out.println("    file does not exist. Canceling \"save as\".");}
                removePltFile(fileName);
                return;
            }
        }
        if(fileName == null || fileName.trim().length() <=0) {
            if(pc.dbg) {System.out.println("    Empty source file name; canceling \"save as\".");}
            return;
        }
        setCursorWait();
        f = new java.io.File(fileName);
        String source = f.getName();
        String newFileName = source;
        if(source.length()>4) {newFileName = source.substring(0, source.length()-4)+"(2).plt";}
        newFileName = Util.getSaveFileName(this, pc.progName,
                "Save file as ...", 6, newFileName, pc.pathDef.toString());
        setCursorDef();
        if(newFileName == null || newFileName.trim().length() <=0) {
            if(pc.dbg) {System.out.println("    Empty target file name; canceling \"save as\".");}
            return;
        }
        if(newFileName.equalsIgnoreCase(fileName)) {
            if(pc.dbg) {System.out.println("    Target file name = source file name; canceling \"save as\".");}
            return;
        }
        setCursorWait();
        newF = new java.io.File(newFileName);
        boolean ok = copyTextFile(f, newF, pc.dbg);
        if (ok) {
            String msg = "File \""+newF.getName()+"\" has been created.";
            System.out.println("    "+msg);
            javax.swing.JOptionPane.showMessageDialog(this,msg,
                pc.progName,javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
        setCursorDef();
    }//GEN-LAST:event_jMenu_Plot_SaveAsActionPerformed

    private void jMenu_Data_SaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu_Data_SaveAsActionPerformed
        if(pc.dbg) {System.out.println("--- Data file \"save as\"");}
        String fileName;
        java.io.File f, newF;
        if(jComboBox_Dat.getSelectedIndex() < 0) {
            if(pc.dbg) {System.out.println("    jComboBox_Dat.getSelectedIndex() < 0"+nl+
                                           "    canceling \"save as\".");}
            return;
        } else {
            fileName = jComboBox_Dat.getSelectedItem().toString();
            if(pc.dbg) {System.out.println("    jComboBox_Dat.getSelectedItem() = "+fileName);}
            f = new java.io.File(fileName);
            if(!f.exists()) {
                if(pc.dbg) {System.out.println("    file does not exist. Canceling \"save as\".");}
                removeDatFile(fileName);
                return;
            }
        }
        if(fileName == null || fileName.trim().length() <=0) {
            if(pc.dbg) {System.out.println("    Empty source file name; canceling \"save as\".");}
            return;
        }
        setCursorWait();
        f = new java.io.File(fileName);
        String source = f.getName();
        String newFileName = source;
        if(source.length()>4) {newFileName = source.substring(0, source.length()-4)+"(2).dat";}
        newFileName = Util.getSaveFileName(this, pc.progName,
                "Save file as ...", 5, newFileName, pc.pathDef.toString());
        setCursorDef();
        if(newFileName == null || newFileName.trim().length() <=0) {
            if(pc.dbg) {System.out.println("    Empty target file name; canceling \"save as\".");}
            return;
        }
        if(newFileName.equalsIgnoreCase(fileName)) {
            if(pc.dbg) {System.out.println("    Target file name = source file name; canceling \"save as\".");}
            return;
        }
        setCursorWait();
        newF = new java.io.File(newFileName);
        boolean ok = copyTextFile(f, newF, pc.dbg);
        if (ok) {
            String msg = "File \""+newF.getName()+"\" has been created.";
            System.out.println("    "+msg);
            javax.swing.JOptionPane.showMessageDialog(this,msg,
                pc.progName,javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
        setCursorDef();
    }//GEN-LAST:event_jMenu_Data_SaveAsActionPerformed

// </editor-fold>

//<editor-fold defaultstate="collapsed" desc="Methods">

  /** wake up any thread waiting: "this.wait()" */
  private synchronized void notify_All() {notifyAll();}

  private synchronized void synchWaitPrinted() {
    if(pc.dbg) {System.out.println("---- synchWaitPrinted()");}
    while(!waitingForPrinter) {
        try {this.wait();} catch(InterruptedException ex) {}
    }
  } // synchWaitPrinted()

  private void end_program() {
      if(pc.dbg){System.out.println(pc.progName+"---- end_program()");}
      if(modifyDiagramWindow != null) {
        if(modifyDiagramWindow.isModified()) {
            modifyDiagramWindow.quitFrame();
            if(modifyDiagramWindow != null && modifyDiagramWindow.isModified()) {return;}
        }
      }
      if(helpAboutFrame != null) {helpAboutFrame.closeWindow();}
      if(fileIni != null) {saveIni(fileIni);}
      this.dispose();
      spf = null;
      OneInstance.endCheckOtherInstances();
      if(pc.dbg) {System.out.println("System.exit(0);");}
      System.exit(0);
  } // end_program()

  public void bringToFront() {
    if(pc.dbg) {System.out.println("---- bringToFront()");}
    if(spf == null) {return;}
    java.awt.EventQueue.invokeLater(new Runnable() {@Override public void run() {
        if(!spf.isVisible()) {spf.setVisible(true);}
        if(spf.getExtendedState()==javax.swing.JFrame.ICONIFIED // minimised?
           || spf.getExtendedState()==javax.swing.JFrame.MAXIMIZED_BOTH)
                {spf.setExtendedState(javax.swing.JFrame.NORMAL);}
        spf.setEnabled(true);
        spf.setAlwaysOnTop(true);
        spf.toFront();
        spf.requestFocus();
        spf.setAlwaysOnTop(false);
    }});
  } // bringToFront()

  private void minimize() {
    if(pc.dbg) {System.out.println("---- minimize()");}
    if(spf != null && spf.isVisible()) {this.setExtendedState(javax.swing.JFrame.ICONIFIED);}
  } // minimize()

  void setCursorWait() {
    if(spf != null) {setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));}
    if(msgFrame != null && msgFrame.isShowing()) {
        msgFrame.setCursorWait();
    }
    if(modifyDiagramWindow != null && modifyDiagramWindow.isShowing()) {
        modifyDiagramWindow.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
    }
  }
  void setCursorDef() {
    if(spf != null) {setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));}
    if(msgFrame != null && msgFrame.isShowing()) {
        msgFrame.setCursorDef();
    }
    if(modifyDiagramWindow != null && modifyDiagramWindow.isShowing()) {
        modifyDiagramWindow.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }
  }

  //<editor-fold defaultstate="collapsed" desc="addDatFile(String)">
 /**  Add a data file as an item in the combo box if the file is not
  * already there; bring the main window to front.
  * @param datFileN String containing the name of the data file
  * @return true if ok, false otherwise. */
  protected boolean addDatFile(String datFileN) {
    if(datFileN == null) {return false;}
    if(pc.dbg) {System.out.println("---- addDatFile("+datFileN+")");}
    //--- check the name
    if(!datFileN.toLowerCase().endsWith(".dat")) {
        MsgExceptn.exception("Data file name = \""+datFileN.toLowerCase()+"\""+nl+
                "Error: data file name must end with \".dat\"");
        return false;}
    java.io.File datFile = new java.io.File(datFileN);
    if(datFile.getName().length() <= 4) {
        MsgExceptn.exception("Error: file name must have at least one character");
        return false;}
    if(!datFile.exists()) {
        String msg;
        if(datFileN.startsWith("-") || datFileN.startsWith("/")) {
            msg = "Error: \""+datFileN+"\""+nl+
                   "   is neither a data file"+nl+
                   "   nor a command-line switch."+nl+nl+
                   "Enter: \"java -jar Spana.jar -?\""+nl+"for a list of command-line options.";
        } else {msg = "Error: \""+datFileN+"\""+nl+
                       "    is not an existing data file.";
        }
        MsgExceptn.showErrMsg(this, msg, 1);
        return false;
    }
    if(!datFile.canRead()) {
        String msg = "Error - can not read from file:"+nl+"   \""+datFileN+"\"";
        System.out.println(msg);
        javax.swing.JOptionPane.showMessageDialog(this,msg,
                pc.progName,javax.swing.JOptionPane.ERROR_MESSAGE);
        return false;
    }
    this.bringToFront();
    String datFileNameFull;
    try {datFileNameFull = datFile.getCanonicalPath();}
    catch (java.io.IOException ex) {
        try{datFileNameFull = datFile.getAbsolutePath();}
        catch (Exception e) {datFileNameFull = datFile.getPath();}
    }
    datFile = new java.io.File(datFileNameFull);
    pc.setPathDef(datFile);

    // make sure the extension is lower case
    datFileNameFull = datFileNameFull.substring(0, datFileNameFull.length()-3)+"dat";
    // allow "save as" menu item
    jMenu_Data_SaveAs.setEnabled(true);

    // if we already have this file name in the list, just highlight it
    if(dataFileArrList.size() >0) {
        int datFile_found = -1;
        for (int i = 0; i < dataFileArrList.size(); i++) {
            if (dataFileArrList.get(i).equalsIgnoreCase(datFileNameFull)) {datFile_found = i; break;}
        } // for i
        if(datFile_found >-1) {
            final int i = datFile_found;
            javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
                    jComboBox_Dat.setSelectedIndex(i);
            }}); //invokeLater(Runnable)
            return true;
        }
    } // if dataFileArrList.size() >0

    // the file name was not in the list: add it
    final String fileName = datFileNameFull;
    dataFileArrList.add(fileName);
    jComboBox_Dat.addItem(fileName);
    jLabelBackgrd.setIcon(null);
    jLabelBackgrd.setSize(ZERO);
    jLabelBackgrd.setVisible(false);
    jPanel1.setSize(PANELsize);
    jPanel1.setVisible(true);
    //jLabelBackgrd.setVisible(false);
    //jPanel1.setVisible(true);
    jLabel_Dat.setVisible(true);
    jComboBox_Dat.setVisible(true);
    jComboBox_Dat.setSelectedIndex(jComboBox_Dat.getItemCount()-1);
    return true;
  } // addDatFile(String)
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="removeDatFile(String)">
 /**  Removes a data file as an item in the combo box if the file is there.
  * @param datFileN String containing the name of the data file
  * @return true if ok (file name removed from the combo box), false otherwise. */
  protected boolean removeDatFile(String datFileN) {
    if(datFileN == null || datFileN.trim().length() <=0) {return false;}
    if(pc.dbg) {System.out.println("---- removeDatFile("+datFileN+")");}
    // if we already have this file name in the list, remove it
    int datFile_found;
    if(dataFileArrList.size() >0) {
      synchronized (this) {
        datFile_found = -1;
        for (int i = 0; i < dataFileArrList.size(); i++) {
            if (dataFileArrList.get(i).equalsIgnoreCase(datFileN)) {datFile_found = i; break;}
        } // for i
        if(datFile_found == -1) {return false;}
        else {
                dataFileArrList.remove(datFile_found);
                jComboBox_Dat.removeItemAt(datFile_found);
                if(jComboBox_Dat.getItemCount()<=0) {
                    jComboBox_Dat.setVisible(false);
                    jLabel_Dat.setVisible(false);
                }
            System.out.println("Warning: file \""+datFileN+"\""+nl+
                            "   does not exist any more. Name removed from list.");
        }
      } // synchronized
    } // if dataFileArrList.size() >0
    javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
        int i = jComboBox_Dat.getItemCount();
        if(i >0) {jComboBox_Dat.setSelectedIndex(i-1);}
        else {jComboBox_Dat.setSelectedIndex(-1); jMenu_Data_SaveAs.setEnabled(false);}
    }}); //invokeLater(Runnable)
    return true;
  } // removeDatFile(String)
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="dispatchArg(String)">
 /** Execute the command-line arguments (one by one)
  * @param arg String containing a command-line argument */
  public void dispatchArg(String arg) {
    if(arg == null) {return;}
    if(arg.length() <=0) {doNotExit = true; return;}
    System.out.println("Command-line argument = "+arg);
    //these are handled in "main"
    if(arg.equals("-dbg") || arg.equals("/dbg")) {doNotExit = true; return;}
    if(arg.equals("-?") || arg.equals("/?") || arg.equals("?")
            || arg.equals("-help") || arg.equals("--help")) {
        printInstructions();
        doNotExit = true;
        return;}
    String pltFileName;
    if(arg.length() >3) {
        String arg0 = arg.substring(0, 2).toLowerCase();
        // ---- starts with "-p"
        if(arg0.startsWith("-p") || arg0.startsWith("/p")) {
            if(arg.charAt(2) == '=' || arg.charAt(2) == ':') {
                pltFileName = arg.substring(3);
                if(pltFileName.length() > 2 && pltFileName.startsWith("\"") && pltFileName.endsWith("\"")) {
                    pltFileName = pltFileName.substring(1, arg.length()-1);
                }
                if(pltFileName.length()>4 && pltFileName.toLowerCase().endsWith(".plt")) {
                    if(pc.dbg){System.out.println("Print: "+pltFileName);}
                    displayPlotFile(pltFileName, "print");
                    return;
                }
            }
        } // if starts with "-p"
    } // if length >3
    // ---- starts with "-ps"
    if(arg.length() >4) {
        String arg0 = arg.substring(0, 3).toLowerCase();
        if(arg0.startsWith("-ps") || arg0.startsWith("/ps")) {
                if(arg.charAt(3) == '=' || arg.charAt(3) == ':') {
                    pltFileName = arg.substring(4);
                    if(pltFileName.length() > 2 && pltFileName.startsWith("\"") && pltFileName.endsWith("\"")) {
                        pltFileName = pltFileName.substring(1, arg.length()-1);
                    }
                    if(pltFileName.length()>4 && pltFileName.toLowerCase().endsWith(".plt")) {
                        if(pc.dbg){System.out.println("Convert to PS: "+pltFileName);}
                        DiagrConvert.doIt(null, 2, pltFileName, pd, pc);
                        return;
                    }
                }
        } // if starts with "-ps"
    } // if length > 4
    // ---- starts with "-eps", "-pdf", etc
    if(arg.length() >5) {
        String arg0 = arg.substring(0, 4).toLowerCase();
        pltFileName = arg.substring(5);
        if(arg.charAt(0) == '-' || arg.charAt(0) == '/') {
            String argType = arg0.substring(1,4);
            boolean ok = false;
            for(String ext : FORMAT_NAMES) {if(argType.equalsIgnoreCase(ext)) {ok = true; break;}}
            if(argType.equals("eps") || argType.equals("pdf") || ok ) {
                if(arg.charAt(4) == '=' || arg.charAt(4) == ':') {
                    if(pltFileName.length() > 2 && pltFileName.startsWith("\"") && pltFileName.endsWith("\"")) {
                        pltFileName = pltFileName.substring(1, arg.length()-1);
                    }
                    if(pltFileName.length()>4 && pltFileName.toLowerCase().endsWith(".plt")) {
                        if(pc.dbg){System.out.println("Convert to "
                                        +arg0.substring(1,4).toUpperCase()+": "+pltFileName);}
                        if(argType.equals("pdf") || argType.equals("eps")) {
                            int typ = -1;
                            if(argType.equals("pdf")) {typ = 1;} else if(argType.equals("eps")) {typ = 3;}
                            DiagrConvert.doIt(null, typ, pltFileName, pd, pc);
                        } else {
                            displayPlotFile(pltFileName, argType);
                        }
                        return;
                    }
                }
            } // if eps/pdf/jpg/gif/png/emf
        } // if it starts with "-" or "/"
    } // if length > 5
    if(arg.length() > 2 && arg.startsWith("\"") && arg.endsWith("\"")) {
        arg = arg.substring(1, arg.length()-1);
    }
    if(arg.toLowerCase().endsWith(".plt")) {
        this.minimize();
        if(!displayPlotFile(arg, null)) {bringToFront();}
        doNotExit = true;
        return;
    } else if(arg.toLowerCase().endsWith(".dat")) {
        addDatFile(arg);
        doNotExit = true;
        return;
    }
    String msg = "Error: bad format for"+nl+
                 "   command-line argument: \""+arg+"\"";
    System.out.println(msg);
    printInstructions();
    javax.swing.JOptionPane.showMessageDialog(this,msg,
                pc.progName,javax.swing.JOptionPane.ERROR_MESSAGE);
    setCursorWait();
    Thread hlp = new Thread() {@Override public void run(){
        String[] a = {"SP_Batch_Mode_htm"};
        lib.huvud.RunProgr.runProgramInProcess(null,ProgramConf.HELP_JAR,a,false,pc.dbg,pc.pathAPP);
        try{Thread.sleep(1500);}  //show the "wait" cursor for 1.5 sec
        catch (InterruptedException e) {}
        setCursorDef();
    }};//new Thread
    hlp.start();
    doNotExit = true;
  } // dispatchArg(arg)
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="displayPlotFile(pltFileN, action)">
    /** Displays a plot file either by<ul>
     * <li>finding out if there is already an instance
     * of <code>Disp</code> for this plot file,
     * and if so, bringing the corresponding frame to front;</li>
     * <li>if no previous instance of <code>Disp</code> corresponds
     * to this file name: create a new instance of <code>Disp</code></li>
     * @param pltFileN name of the plot file to display.
     * @param action <ul>
     * <li>"null" the file is only displayed.
     * <li>"print" then the plot file is printed on the <u>default</u> printer.
     * <li>"ps", "eps", "pdf", "gif", "jpg", "png", or "bmp",<br>
     * then the plot file is exported to a file with the
     * corresponding graphic format.</ul> */
    boolean displayPlotFile(final String pltFileN, final String action) {
        if(pltFileN == null || pltFileN.trim().length() <=0) {
            this.setExtendedState(javax.swing.JFrame.NORMAL);
            MsgExceptn.exception("Error: pltFileN empty in method \"displayPlotFile\"");
            return false;}
        if(!pltFileN.toLowerCase().endsWith(".plt")) {
            this.setExtendedState(javax.swing.JFrame.NORMAL);
            MsgExceptn.exception("Error: plot file name \""+pltFileN+"\""+nl+
                         "   does not end with \".plt\"!");
            return false;
        }
        if(pc.dbg) {System.out.println("diaplayPlotFile("+pltFileN+", "+action+")");}
        java.io.File pltFile = new java.io.File(pltFileN);
        if(!pltFile.exists()) {
            this.bringToFront();
            String msg;
            if(pltFileN.startsWith("-") || pltFileN.startsWith("/")) {
                  msg = "Error: \""+pltFileN+"\""+nl+
                   "   is neither a plot file"+nl+
                   "   nor a command-line switch."+nl+nl+
                   "Enter: \"java -jar Spana.jar -?\""+nl+"for a list of command-line options.";
            } else {msg = "Error: \""+pltFileN+"\""+nl+
                       "   is not an existing plot file.";}
            MsgExceptn.showErrMsg(this, msg, 1);
            return false;}
        if(!pltFile.canRead()) {
            this.setExtendedState(javax.swing.JFrame.NORMAL);
            String msg = "Error - can not read file:"+nl+
                         "  \""+pltFile.getPath()+"\"";
            MsgExceptn.showErrMsg(this, msg, 1);
            return false;}
        // is there an action?
        boolean printFile = false;
        boolean export = false;
        final String actionLC;
        if(action != null && action.trim().length() >0) {actionLC = action.toLowerCase();}
        else {actionLC = null;}
        if(actionLC != null) {
            if(actionLC.equals("print")) {printFile = true;}
            else {export = true;}
        } // if actionLC !=null
        setCursorWait();
        // get file name
        String pltFileNameFull;
        try {pltFileNameFull = pltFile.getCanonicalPath();}
        catch (java.io.IOException ex) {
            try{pltFileNameFull = pltFile.getAbsolutePath();}
            catch (Exception e) {pltFileNameFull = pltFile.getPath();}
        }
        // make sure the extension is lower case
        pltFileNameFull = pltFileNameFull.substring(0, pltFileNameFull.length()-3)+"plt";

        pltFile = new java.io.File(pltFileNameFull);
        pc.setPathDef(pltFile);

        // allow "save as" menu item
        jMenu_Plot_SaveAs.setEnabled(true);

        // if we already have this file name in the list,
        // just show the diagram and highlight it in the list
        Disp disp = null;
        if(diagrArrList.size() > 0) {
            if(pc.dbg) {System.out.println("    diagrArrList not empty.");}
            int plt_found = -1;
            for (int i = 0; i < diagrArrList.size(); i++) {
                if(diagrArrList.get(i).diagrFullName.equalsIgnoreCase(pltFileNameFull)) {plt_found = i; break;}
            } // for i
            if(plt_found >-1) {
                if(pc.dbg) {System.out.println("    plot file found in the list.");}
                disp = showDiagr(plt_found, printFile);
                if(disp != null) {
                    final int i = plt_found;
                    javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
                        jComboBox_Plt_doNothing = true;
                        jComboBox_Plt.setSelectedIndex(i);
                        jComboBox_Plt_doNothing = false;
                    }}); //invokeLater(Runnable)
                } // if disp != null
            } // if plt_found >-1

        } // if diagrArrList.size() >0
        // else:
        if(disp == null) {
            if(pc.dbg) {System.out.println("    plot file not found in the list. Adding.");}
            // The file name was not in the list: add it
            // Create a new Diagram frame (window)
            dispLocation.x = dispLocation.x + 20;
            dispLocation.y = dispLocation.y + 20;
            if(dispLocation.x > (screenSize.width-dispSize.width)) {dispLocation.x = 60;}
            if(dispLocation.y > (screenSize.height-dispSize.height-20)) {dispLocation.y = 10;}
            disp = new Disp(diagrPaintUtil, pc, pd);
            disp.startPlotFile(pltFile);
            if(disp.diagrName == null || disp.diagrName.trim().length() <=0) {setCursorDef(); return false;}
            jComboBox_Plt_doNothing = true;
            jComboBox_Plt.addItem(pltFileNameFull);
            jComboBox_Plt_doNothing = false;
            diagrArrList.add(disp);
            // execute changes to frames in Swing's "Event Dispatching Thread"
            javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
                jLabelBackgrd.setIcon(null);
                jLabelBackgrd.setSize(ZERO);
                jLabelBackgrd.setVisible(false);
                jPanel1.setSize(PANELsize);
                jPanel1.setVisible(true);
                jLabel_Plt.setVisible(true);
                jComboBox_Plt.setVisible(true);
                int i = jComboBox_Plt.getItemCount()-1;
                jComboBox_Plt_doNothing = true;
                jComboBox_Plt.setSelectedIndex(i);
                jComboBox_Plt_doNothing = false;
                }}); //invokeLater(Runnable)
        } // if disp = null

        if(printFile) {
            final Disp fDisp = disp;
            setCursorWait();
            disp.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            Thread printF = new Thread() {@Override public void run(){
                if(pc.dbg){System.out.println("Printing file: "+pltFileN);}
                fDisp.printDiagram(true);
                waitingForPrinter = true;
                // wake up any thread waiting for the printing to finish
                notify_All();
                }}; //new Thread
            printF.start();
            // make sure the file has been printed before going ahead
            this.synchWaitPrinted();
            waitingForPrinter = false;
        } // íf printFile

        else if(export) {
            setCursorWait();
            disp.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            disp.export(actionLC, false);
        } // íf export

        setCursorDef();
        disp.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        return true;
    } // displayPlotFile(String, String)
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="removePlotFile(String)">
 /**  Removes a plot file as an item in the combo box if the file is there.
  * @param pltFileN String containing the name of the plot file
  * @return true if ok (file name removed from the combo box), false otherwise. */
  protected boolean removePltFile(String pltFileN) {
    if(pltFileN == null || pltFileN.trim().length() <=0) {return false;}
    if(pc.dbg) {System.out.println("---- removePltFile("+pltFileN+")");}
    // if we already have this file name in the list, remove it
    int plt_found;
    if(diagrArrList.size() >0) {
        plt_found = -1;
        for (int i = 0; i < diagrArrList.size(); i++) {
            if(diagrArrList.get(i).diagrFullName.equalsIgnoreCase(pltFileN)) {plt_found = i; break;}
        }
        if(plt_found == -1) {return false;}
        else {
            // close the diagram window if visible
            Disp gotDisp = diagrArrList.get(plt_found);
            java.awt.Frame[] f = Disp.getFrames();
            int foundFrame=-1;
            for(int i = 0; i<f.length ;i++){if(f[i].equals(gotDisp)) {foundFrame=i; break;}}
            if(foundFrame>0) {
                spf.bringToFront();
                jPanel1.requestFocusInWindow();
                if(gotDisp.isVisible()) {
                    int n= javax.swing.JOptionPane.showConfirmDialog(spf,
                        "Plot file \""+gotDisp.diagrFullName+"\""+nl+
                        "does NOT EXIST anymore!"+nl+nl+
                        "The diagram window will be closed"+nl+
                        "and the name removed from the list.",
                        pc.progName, javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.ERROR_MESSAGE);
                    if(n != javax.swing.JOptionPane.OK_OPTION) {return false;}
                    if((gotDisp.getExtendedState() & javax.swing.JFrame.ICONIFIED)
                            == javax.swing.JFrame.ICONIFIED) // minimised?
                        {gotDisp.setExtendedState(javax.swing.JFrame.NORMAL);}
                    gotDisp.setVisible(false);
                }
                gotDisp.dispose();
            } // if foundFrame
            synchronized (this) {
                diagrArrList.remove(plt_found);
                jComboBox_Plt_doNothing = true;
                jComboBox_Plt.removeItemAt(plt_found);
                if(jComboBox_Plt.getItemCount()<=0) {
                    jComboBox_Plt.setVisible(false);
                    jLabel_Plt.setVisible(false);
                }
                jComboBox_Plt_doNothing = false;
            } //synchronized
/*            final int i = plt_found;
            javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
                jComboBox_Plt_doNothing = true;
                jComboBox_Plt.removeItemAt(i);
                jComboBox_Plt_doNothing = false;
            }}); //invokeLater(Runnable) */
            System.out.println("Warning: file \""+pltFileN+"\""+nl+
                            "   does not exist any more. Name removed from list.");
        }
    } // if dataFileArrList.size() >0
    plt_found = jComboBox_Plt.getItemCount();
    if(plt_found >0) {jComboBox_Plt.setSelectedIndex(plt_found-1);}
    else {jComboBox_Plt.setSelectedIndex(-1); jMenu_Plot_SaveAs.setEnabled(false);}
    return true;
  } // removeDatFile(String)
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="modifyDataFile">
  private void modifyDataFile() {
    setCursorWait();
    if(pc.dbg) {System.out.println("---- modifyDataFile()");}
    //---- is the data-files combo box empty? if so, get a file name
    if(dataFileArrList.size() <=0) {
        String fileName = Util.getOpenFileName(this, pc.progName, true,
                "Open data file", 5, null, pc.pathDef.toString());
        if(fileName == null) {setCursorDef(); return;}
        java.io.File f = new java.io.File(fileName);
        if(f.exists() && (!f.canWrite() || !f.setWritable(true))) {
            String msg = "Warning - the file:"+nl+
              "   \""+f.getPath()+"\""+nl+
              "is write-protected!"+nl+nl+
              "You will be able to save changes to another file,"+nl+
              "but it might be best to make a non read-only copy"+nl+
              "of this file before continuing...";
            System.out.println(LINE+nl+msg+nl+LINE);
            Object[] opt = {"Continue anyway", "Cancel"};
            int m = javax.swing.JOptionPane.showOptionDialog(this, msg,
                pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
            if(m != javax.swing.JOptionPane.YES_OPTION) {setCursorDef(); return;}
        }

        if(!addDatFile(fileName)) {setCursorDef(); return;}
    } // if dataFileArrList.size() <=0
    setCursorWait();
    // ---- get the file name selected in the combo box
    final java.io.File datFile = new java.io.File(jComboBox_Dat.getSelectedItem().toString());
    final boolean jMenu_Prefs_General_setEnabled = jMenu_Prefs_General.isEnabled();
    final boolean jMenu_Run_Modif_setEnabled = jMenu_Run_Modif.isEnabled();
    jMenu_Prefs_General.setEnabled(false);
    jMenu_Run_Modif.setEnabled(false);
    // ---- Start a thread
    new javax.swing.SwingWorker<Void,Void>() {
    @Override protected Void doInBackground() throws Exception {
        modifyDiagramWindow = new ModifyChemSyst(pc, pd);
        modifyDiagramWindow.startDataFile(datFile);
        setCursorDef();
        modifyDiagramWindow.waitForModifyChemSyst();
        return null;
    }
    @Override protected void done(){
            jMenu_Run_Modif.setEnabled(jMenu_Run_Modif_setEnabled);
            jMenu_Prefs_General.setEnabled(jMenu_Prefs_General_setEnabled);
            modifyDiagramWindow = null;
    } // done()
    }.execute(); // this returns inmediately,
    //    but the SwingWorker continues running...

  } //modifyDataFile
// </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="run_Database">
  private void run_Database() {
    if(pc.dbg) {System.out.println("---- run_Database()");}
    java.io.File f;
    String msg = null;
    if(createDataFileProg != null && createDataFileProg.trim().length()>0) {
        f = new java.io.File(createDataFileProg);
        if(!f.exists()) {
          msg = "Error: the program to create input data files:"+nl+
              "  \""+createDataFileProg+"\""+nl+
              "  does not exist.";
        }
    } else {
          msg = "Error:"+nl+
                "no name given for the program"+nl+
                "to create input data files.";
    }
    if(msg != null) {
        MsgExceptn.exception(msg);
        javax.swing.JOptionPane.showMessageDialog(this, msg, pc.progName,
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        setCursorDef();
        return;
    }
    final boolean jMenu_Run_Database_setEnabled = jMenu_Run_Database.isEnabled();
    final boolean jMenu_Data_New_setEnabled = jMenu_Data_New.isEnabled();
    jMenu_Run_Database.setEnabled(false);
    jMenu_Data_New.setEnabled(false);
    setCursorWait();
    Thread runDatabase = new Thread() {@Override public void run(){
      // for some reason, at least in Windows XP,
      // a command argument such as "a\" is transfored into: a"
      String d = pc.pathDef.toString();
      if(d.endsWith("\\")) {d = d + " ";} // avoid the combination: \"
      String[] argsDatabase = new String[]{d};
      boolean waitForCompletion = false;
      if(pc.dbg) {System.out.println("---- runProgramInProcess:"+nl+
                                     "     "+createDataFileProg+nl+
                                     "     "+java.util.Arrays.toString(argsDatabase)+nl+
                                     "     wait = "+waitForCompletion+", dbg = "+pc.dbg+nl+
                                     "     path = "+pc.pathAPP);}
      lib.huvud.RunProgr.runProgramInProcess(MainFrame.this, createDataFileProg, argsDatabase,
              waitForCompletion, pc.dbg, pc.pathAPP);
      javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
        jMenu_Run_Database.setEnabled(jMenu_Run_Database_setEnabled);
        jMenu_Data_New.setEnabled(jMenu_Data_New_setEnabled);
      }}); //invokeLater(Runnable)
      try{Thread.sleep(1500);} //show the "wait" cursor for 1.5 sec
      catch (InterruptedException e) {}
      setCursorDef();
    }};
    runDatabase.start();
  } //run_Database()
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="showDiagr">
  /**  Shows an instance of <code>Disp</code> that may have been hidden by the user.
   * @param k int: the Disp instance in the array list <code>diagrArrList</code>
   * @param printFile = true if the plot file is to be printed on the
   * default printer.
   * @return <code>true</code> if successful. */
  private Disp showDiagr(int k, boolean printFile) {
    // check for erroneous situations
    if (k <0 || diagrArrList.size() <=0 || jComboBox_Plt.getItemCount()<=0) {return null;}
    if(pc.dbg) {System.out.println("---- showDiagr("+k+")");}
    Disp gotDisp = diagrArrList.get(k);
    java.awt.Frame[] f = Disp.getFrames();
    int foundFrame=-1;
    for(int i = 0; i<f.length ;i++){
      if(f[i].equals(gotDisp)) {foundFrame=i; break;}
    } // for i
    if(foundFrame>0) {
      if((gotDisp.getExtendedState() & javax.swing.JFrame.ICONIFIED)
                            == javax.swing.JFrame.ICONIFIED) // minimised?
            {gotDisp.setExtendedState(javax.swing.JFrame.NORMAL);}
      gotDisp.setVisible(true);
      gotDisp.toFront();
      gotDisp.requestFocus();
      gotDisp.reloadPlotFile();
      gotDisp.repaint();
      if(printFile) {gotDisp.printDiagram(true);}
      return gotDisp;
    } // if foundFrame
    // if foundFrame <=0
    synchronized (this) {
        diagrArrList.remove(k);
        jComboBox_Plt_doNothing = true;
        jComboBox_Plt.removeItemAt(k);
        if(jComboBox_Plt.getItemCount()<=0) {
            jComboBox_Plt.setVisible(false);
            jLabel_Plt.setVisible(false);
        }
        jComboBox_Plt_doNothing = false;
    } //synchronized
    return null;
  } // showDiagr(k)
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="read-write INI file">

  /** Reads program settings saved when the program was previously closed.
   * Exceptions are reported both to the console (if there is one) and to a dialog.<br>
   * Reads the ini-file in:<ul>
   *   <li> the Application Path if found there.</ul>
   * If not found in the application path, or if the file is write-protected:<ul>
   *   <li> in %HomeDrive%%HomePath% if found there; if write-protected also
   *   <li> in %Home% if found there; if write-protected also
   *   <li> in the user's home directory (system dependent) if it is found there
   * otherwise: give a warning and create a new file.  Note: except for the
   * installation directory, the ini-file will be writen in a sub-folder
   * named "<code>.config\eq-diag</code>".
   * <p>
   * This method also saves the ini-file after reading it and after
   * checking its contents.  The file is written in the application path if
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
                            java.io.File tmp = java.io.File.createTempFile("spana",".tmp", p);
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
                    java.io.File tmp = java.io.File.createTempFile("spana",".tmp", p);
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
        MsgExceptn.showErrMsg(spf, msg, 1);
    }
    if(fileINInotRO != null && fileINInotRO != fileRead) {
        ok = saveIni(fileINInotRO);
        if(ok) {fileIni = fileINInotRO;} else {fileIni = null;}
    }
  } // readIni()

  private boolean readIni2(java.io.File f) {
    String msg;
    System.out.flush();
    System.out.println("Reading ini-file: \""+f.getPath()+"\"");
    java.util.Properties propertiesIni= new java.util.Properties();
    java.io.FileInputStream properties_iniFile = null;
    boolean ok = true;
    try {
      properties_iniFile = new java.io.FileInputStream(f);
      propertiesIni.load(properties_iniFile);
    } catch (java.io.FileNotFoundException e) {
      String t = "Warning: file Not found: \""+f.getPath()+"\""+nl+
                         "using default parameter values.";
      System.out.println(t);
      if(!this.isVisible()) {this.setVisible(true);}
      javax.swing.JOptionPane.showMessageDialog(spf, t, pc.progName, javax.swing.JOptionPane.INFORMATION_MESSAGE);
      ok = false;
    }
    catch (java.io.IOException e) {
      msg = "Error: \""+e.toString()+"\""+nl+
                   "   while loading INI-file:"+nl+
                   "   \""+f.getPath()+"\"";
      System.out.println(msg);
      if(!this.isVisible()) {this.setVisible(true);}
      javax.swing.JOptionPane.showMessageDialog(spf, msg, pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
      ok = false;
    } // catch loading-exception
    try {
        if(properties_iniFile != null) {properties_iniFile.close();}
    } catch (java.io.IOException e) {
        msg ="Error: \""+e.toString()+"\""+nl+
                    "   while closing INI-file:"+nl+
                    "   \""+f.getPath()+"\"";
        System.out.println(msg);
        if(!this.isVisible()) {this.setVisible(true);}
        javax.swing.JOptionPane.showMessageDialog(spf, msg, pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
        ok = false;
    }
    finally {
        try {if(properties_iniFile != null) {properties_iniFile.close();}}
        catch (java.io.IOException e) {
            msg = "Error: \""+e.toString()+"\""+nl+
                          "   while closing INI-file:"+nl+
                          "   \""+f.getPath()+"\"";

            System.out.println(msg);
            if(!this.isVisible()) {this.setVisible(true);}
            javax.swing.JOptionPane.showMessageDialog(spf, msg, pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    if(!ok) {return ok;}
    try {
        locationFrame.x = Integer.parseInt(propertiesIni.getProperty("Location_left"));
        locationFrame.y = Integer.parseInt(propertiesIni.getProperty("Location_top"));
        msgFrameSize.width = Integer.parseInt(propertiesIni.getProperty("LogFrame_Width"));
        msgFrameSize.height = Integer.parseInt(propertiesIni.getProperty("LogFrame_Height"));
        locationMsgFrame.x = Integer.parseInt(propertiesIni.getProperty("LogFrame_left"));
        locationMsgFrame.y = Integer.parseInt(propertiesIni.getProperty("LogFrame_top"));
        dispSize.width = Integer.parseInt(propertiesIni.getProperty("Disp_Width"));
        dispSize.height = Integer.parseInt(propertiesIni.getProperty("Disp_Height"));
        dispLocation.x = Integer.parseInt(propertiesIni.getProperty("Disp_left"));
        dispLocation.y = Integer.parseInt(propertiesIni.getProperty("Disp_top"));
        diagrPaintUtil.keepAspectRatio = Boolean.parseBoolean(propertiesIni.getProperty("Disp_KeepAspectRatio"));
        diagrPaintUtil.fixedSize = Boolean.parseBoolean(propertiesIni.getProperty("Disp_FixedSize"));
        diagrPaintUtil.fixedSizeWidth = Float.parseFloat(propertiesIni.getProperty("Disp_FixedSizeWidth"));
        diagrPaintUtil.fixedSizeHeight = Float.parseFloat(propertiesIni.getProperty("Disp_FixedSizeHeight"));
        diagrPaintUtil.fontFamily = Integer.parseInt(propertiesIni.getProperty("Disp_FontFamily"));
        diagrPaintUtil.fontStyle = Integer.parseInt(propertiesIni.getProperty("Disp_FontStyle"));
        diagrPaintUtil.fontSize = Integer.parseInt(propertiesIni.getProperty("Disp_FontSize"));
        String anti = propertiesIni.getProperty("Disp_AntialiasingText");
        if (anti.toLowerCase().equals("on")) {diagrPaintUtil.antiAliasingText = 1;}
        else if (anti.toLowerCase().equals("default")) {diagrPaintUtil.antiAliasingText = 2;}
        else {diagrPaintUtil.antiAliasingText = 0;}
        anti = propertiesIni.getProperty("Disp_Antialiasing");
        if (anti.toLowerCase().equals("on")) {diagrPaintUtil.antiAliasing = 1;}
        else if (anti.toLowerCase().equals("default")) {diagrPaintUtil.antiAliasing = 2;}
        else {diagrPaintUtil.antiAliasing = 0;}
        diagrPaintUtil.penThickness = Float.parseFloat(propertiesIni.getProperty("Disp_PenThickness"));
        if(pc.pathDef.length() >0) {pc.pathDef.delete(0, pc.pathDef.length());}
        pc.pathDef.append(propertiesIni.getProperty("defaultPath"));
        txtEditor = propertiesIni.getProperty("txtEditor");
        pathSedPredom = propertiesIni.getProperty("pathSedPredom");
        createDataFileProg = propertiesIni.getProperty("createDataFileProg");
        pd.advancedVersion = Boolean.parseBoolean(propertiesIni.getProperty("advancedVersion"));
        int red, green, blue;
        for(int ii=0; ii < DiagrPaintUtility.MAX_COLOURS; ii++) {
            String[] colrs = propertiesIni.getProperty("Disp_Colour["+ii+"]").split(",");
            if (colrs.length > 0) {red =Integer.parseInt(colrs[0]);} else {red=0;}
            if (colrs.length > 1) {green =Integer.parseInt(colrs[1]);} else {green=0;}
            if (colrs.length > 2) {blue =Integer.parseInt(colrs[2]);} else {blue=0;}
            red=Math.max(0, Math.min(255, red));
            green=Math.max(0, Math.min(255, green));
            blue=Math.max(0, Math.min(255, blue));
            diagrPaintUtil.colours[ii] = new java.awt.Color(red,green,blue);
        } // for ii
        diagrPaintUtil.colourType = Integer.parseInt(propertiesIni.getProperty("Disp_ColourType"));
        diagrPaintUtil.printColour = Boolean.parseBoolean(propertiesIni.getProperty("Disp_PrintColour"));
        diagrPaintUtil.printHeader = Boolean.parseBoolean(propertiesIni.getProperty("Disp_PrintHeader"));
        diagrPaintUtil.textWithFonts = Boolean.parseBoolean(propertiesIni.getProperty("Disp_TextWithFonts"));
        diagrPaintUtil.printPenThickness = Float.parseFloat(propertiesIni.getProperty("Disp_PrintPenThickness"));
        diagrPaintUtil.useBackgrndColour = Boolean.parseBoolean(propertiesIni.getProperty("Disp_UseBackgrndColour"));
        String[] colrs = propertiesIni.getProperty("Disp_BackgroundColour").split(",");
        if (colrs.length > 0) {red =Integer.parseInt(colrs[0]);} else {red=0;}
        if (colrs.length > 1) {green =Integer.parseInt(colrs[1]);} else {green=0;}
        if (colrs.length > 2) {blue =Integer.parseInt(colrs[2]);} else {blue=0;}
        red=Math.max(0, Math.min(255, red));
        green=Math.max(0, Math.min(255, green));
        blue=Math.max(0, Math.min(255, blue));
        diagrPaintUtil.backgrnd = new java.awt.Color(red,green,blue);
        pd.fractionThreshold = Float.parseFloat(propertiesIni.getProperty("Disp_FractionThreshold"));
        pd.keepFrame = Boolean.parseBoolean(propertiesIni.getProperty("Calc_keepFrame"));
        pd.SED_nbrSteps = Integer.parseInt(propertiesIni.getProperty("Calc_SED_nbrSteps"));
        pd.SED_tableOutput = Boolean.parseBoolean(propertiesIni.getProperty("Calc_SED_tableOutput"));
        pd.Predom_nbrSteps = Integer.parseInt(propertiesIni.getProperty("Calc_Predom_nbrSteps"));
        pd.ionicStrength = Double.parseDouble(propertiesIni.getProperty("Calc_ionicStrength"));
        pd.actCoeffsMethod = Integer.parseInt(propertiesIni.getProperty("Calc_activityCoefficientsMethod"));
        pd.pathSIT = propertiesIni.getProperty("Calc_pathSIT");
        pd.aquSpeciesOnly = Boolean.parseBoolean(propertiesIni.getProperty("Calc_Predom_aquSpeciesOnly"));
        pd.reversedConcs = Boolean.parseBoolean(propertiesIni.getProperty("Calc_allowReversedConcRanges"));
        pd.useEh = Boolean.parseBoolean(propertiesIni.getProperty("Calc_useEh"));
        pd.calcDbg = Boolean.parseBoolean(propertiesIni.getProperty("Calc_dbg"));
        pd.calcDbgHalta = Integer.parseInt(propertiesIni.getProperty("Calc_dbgHalta"));
        pd.tolHalta = Double.parseDouble(propertiesIni.getProperty("Calc_tolerance"));
        pd.tblExtension = propertiesIni.getProperty("Calc_tableFileExt");
        String t = propertiesIni.getProperty("Calc_tableFieldSeparatorChar");
        if(t !=null && t.length()>=2 && t.substring(0,1).equalsIgnoreCase("\\t")) {
          pd.tblFieldSeparator = '\u0009';
        } else {
          if(t != null) {pd.tblFieldSeparator = t.charAt(0);} else {pd.tblFieldSeparator = ';';}
        }
        t = propertiesIni.getProperty("Calc_tableCommentLineStart");
        if(t !=null) {pd.tblCommentLineStart = t;}
        else {pd.tblCommentLineStart = "\"";}
        t = propertiesIni.getProperty("Calc_tableCommentLineEnd");
        if(t !=null) {pd.tblCommentLineEnd = t;}
        else {pd.tblCommentLineEnd = "\"";}
        pd.diagrConvertSizeX = Integer.parseInt(propertiesIni.getProperty("Convert_SizeX"));
        pd.diagrConvertSizeY = Integer.parseInt(propertiesIni.getProperty("Convert_SizeY"));
        pd.diagrConvertMarginB = Float.parseFloat(propertiesIni.getProperty("Convert_MarginBottom"));
        pd.diagrConvertMarginL = Float.parseFloat(propertiesIni.getProperty("Convert_MarginLeft"));
        pd.diagrConvertPortrait = Boolean.parseBoolean(propertiesIni.getProperty("Convert_Portrait"));
        pd.diagrConvertHeader = Boolean.parseBoolean(propertiesIni.getProperty("Convert_Header"));
        pd.diagrConvertColors = Boolean.parseBoolean(propertiesIni.getProperty("Convert_Colour"));
        pd.diagrConvertFont = Integer.parseInt(propertiesIni.getProperty("Convert_Font"));
        pd.diagrConvertEPS = Boolean.parseBoolean(propertiesIni.getProperty("Convert_EPS"));
        pd.diagrExportType = propertiesIni.getProperty("Export_To");
        pd.diagrExportSize = Integer.parseInt(propertiesIni.getProperty("Export_Size"));
    } catch (NumberFormatException e) {
        msg = "Error: \""+e.toString()+"\""+nl+
                         "   while reading INI-file:"+nl+
                         "   \""+f.getPath()+"\""+nl+nl+
                         "Setting default program parameters.";
        System.out.println(msg);
        if(!this.isVisible()) {this.setVisible(true);}
        javax.swing.JOptionPane.showMessageDialog(spf, msg, pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
        ok = false;
    }
    try {
        pd.temperature = Double.parseDouble(propertiesIni.getProperty("Calc_temperature"));
    } catch (NumberFormatException e) {
        msg = "Error: \""+e.toString()+"\""+nl+
                         "   while reading INI-file:"+nl+
                         "   \""+f.getPath()+"\""+nl+nl+
                         "Setting default program parameters.";
        System.out.println(msg);
        if(!this.isVisible()) {this.setVisible(true);}
        javax.swing.JOptionPane.showMessageDialog(spf, msg, pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    try{
        pd.drawNeutralPHinPourbaix = Boolean.parseBoolean(propertiesIni.getProperty("Calc_draw_pH_line"));
        String s = propertiesIni.getProperty("lookAndFeel").toLowerCase();
        if(s.startsWith("system")) {laf = 1;}
        else if(s.startsWith("cross")) {laf = 2;}
        else {laf = 0;}
    }
    catch (NullPointerException e) {laf = 0; pd.drawNeutralPHinPourbaix = false;}
    if(pc.dbg) {System.out.println("Finished reading ini-file");}
    System.out.flush();
    checkIniValues();
    return ok;
  } // readIni2()

  private void checkIniValues() {
    System.out.flush();
    System.out.println(LINE+nl+"Checking ini-values.");
    if(locationFrame.x < -1 || locationFrame.y < -1) {
        locationFrame.x = Math.max(0, (screenSize.width  - this.getWidth() ) / 2);
        locationFrame.y = Math.max(0, (screenSize.height - this.getHeight() ) / 2);
    }
    // check Default Path
    java.io.File currentDir = new java.io.File(pc.pathDef.toString());
    if(!currentDir.exists()) {
        pc.setPathDef(); // set Default Path = Start Directory
    } // if !currentDir.exists()
    java.io.File f;
    // check the editor
    if(txtEditor == null || txtEditor.trim().length() <=0) {
        String t;
        if(windows) {
            t = windir;
            if(t != null && t.endsWith(SLASH)) {t = t.substring(0, t.length()-1);}
            if(t != null) {t = t + SLASH + "Notepad.exe";} else {t = "Notepad.exe";}
            txtEditor = t;
        } else if(System.getProperty("os.name").startsWith("Mac OS")) {
            //txtEditor = "/Applications/TextEdit.app/Contents/MacOS/TextEdit";
            txtEditor = "/usr/bin/open -t";
        } else {  //assume Unix or Linux
            txtEditor = "/usr/bin/gedit";
        }
        System.out.println("Note: setting editor = \""+txtEditor+"\"");
    }
    f = new java.io.File(txtEditor);
    if(!System.getProperty("os.name").startsWith("Mac OS") && !f.exists()) {
        System.out.println("Warning: the text editor \""+txtEditor+"\""+nl+
                    "   in the INI-file does not exist.");
    }

    // check path to SED/PREDOM
    java.io.File d = null;
    String dir = pc.pathAPP;
    if(dir != null && dir.endsWith(SLASH)) {dir = dir.substring(0, dir.length()-1);}
    if(pathSedPredom != null && pathSedPredom.trim().length() >0) {
        d = new java.io.File(pathSedPredom);
        if(!d.exists() || !d.isDirectory()) {
            System.out.println("Warning: path \""+pathSedPredom+"\""+nl+
                    "   in the INI-file does not exist (or is not a directory).");
            d = null;
        }
    }
    if(d == null || !d.exists() || !d.isDirectory()) {
        if(pc.pathAPP != null && pc.pathAPP.trim().length()>0) {
            pathSedPredom = pc.pathAPP;
        } else {
            pathSedPredom = System.getProperty("user.dir");
        }
        d = new java.io.File(pathSedPredom);
    }
    String msg = null;
    if(!Div.progSEDexists(d)) {msg = " SED";}
    if(!Div.progPredomExists(d)) {if(msg == null) {msg = " Predom";} else {msg = "s SED and Predom";}}
    if(msg != null) {
        if(pathSedPredom != null) {
            System.out.println("Warning: directory \""+pathSedPredom+"\""+nl+
                "   in the INI-file does not contain program"+msg);
            if(pc.pathAPP != null && pc.pathAPP.trim().length()>0) {
                pathSedPredom = pc.pathAPP;
            } else {
                pathSedPredom = System.getProperty("user.dir");
            }
        }
    }
    if(createDataFileProg != null && createDataFileProg.trim().length() >0) {
        f = new java.io.File(createDataFileProg);
    } else {f = null;}
    if(f==null || !f.exists()) {
        String s = createDataFileProg;
        if(s==null) {s="null";}
        System.out.println("Warning: file \""+s+"\""+nl+
                    "  (the program to create input data files)"+nl+
                    "  given in the INI-file does not exist.");
        getDatabaseProgr(true);
    }

    d = null;
    if(pd.pathSIT != null && pd.pathSIT.trim().length() >0) {
        d = new java.io.File(pd.pathSIT);
        if(!d.exists() || !d.isDirectory()) {
            System.out.println("Warning: path \""+pd.pathSIT+"\""+nl+
                    "   where file \"SIT-coefficients.dta\" should be found"+nl+
                    "   in the INI-file does not exist (or is not a directory).");
            d = null;
        }
    }
    if(d == null || !d.exists() || !d.isDirectory()) {
        if(pc.pathAPP != null && pc.pathAPP.trim().length()>0) {
            pd.pathSIT = pc.pathAPP;
        } else {
            pd.pathSIT = System.getProperty("user.dir");
        }
    }
    String sit = pd.pathSIT;
    if(sit != null && sit.trim().length()>0) {
        if(sit.endsWith(SLASH)) {sit = sit.substring(0, sit.length()-1);}
        sit = sit +SLASH+ "SIT-coefficients.dta";
        f = new java.io.File(sit);
        if(!f.exists()) {
                System.out.println("Warning: directory \""+pd.pathSIT+"\""+nl+
                    "   in the INI-file does not contain file: \"SIT-coefficients.dta\".");
        }
    }
    // progLocation, dispLocation and dispSize are checked
    //  each time these windows are loaded
    diagrPaintUtil.fixedSizeHeight = Math.max(1f,Math.min(99f,diagrPaintUtil.fixedSizeHeight));
    diagrPaintUtil.fixedSizeWidth = Math.max(1f,Math.min(99f,diagrPaintUtil.fixedSizeWidth));
    diagrPaintUtil.colourType = Math.max(0,Math.min(2,diagrPaintUtil.colourType));
    diagrPaintUtil.antiAliasing = Math.max(0,Math.min(2,diagrPaintUtil.antiAliasing));
    diagrPaintUtil.antiAliasingText = Math.max(0,Math.min(2,diagrPaintUtil.antiAliasingText));
    diagrPaintUtil.penThickness = Math.max(0.2f, Math.min(diagrPaintUtil.penThickness,10f));
    diagrPaintUtil.printPenThickness = Math.max(1f, Math.min(diagrPaintUtil.printPenThickness,5f));
    diagrPaintUtil.fontFamily = Math.max(0,Math.min(diagrPaintUtil.fontFamily,4));
    diagrPaintUtil.fontStyle = Math.max(0,Math.min(diagrPaintUtil.fontStyle,2));
    diagrPaintUtil.fontSize = Math.max(1,Math.min(diagrPaintUtil.fontSize,72));
    pd.fractionThreshold = Math.max(0.001f, Math.min(pd.fractionThreshold,0.1f));
    boolean backgrndDark = false;
    if (Math.sqrt(Math.pow(diagrPaintUtil.backgrnd.getRed(),2) +
                  Math.pow(diagrPaintUtil.backgrnd.getGreen(),2) +
                  Math.pow(diagrPaintUtil.backgrnd.getBlue(),2)) < 221)
        {backgrndDark = true;}
    for(int i=0; i < DiagrPaintUtility.MAX_COLOURS; i++)
        {
        if(twoColoursEqual(diagrPaintUtil.backgrnd,diagrPaintUtil.colours[i]))
            {if(backgrndDark) {diagrPaintUtil.colours[i] = new java.awt.Color(200,200,200);}
             else {diagrPaintUtil.colours[i] = new java.awt.Color(100,100,100);}
            } // if twoColoursEqual
        } // for i
    pd.SED_nbrSteps = Math.max(MNSTP, Math.min(MXSTP,pd.SED_nbrSteps));
    pd.Predom_nbrSteps = Math.max(MNSTP, Math.min(MXSTP,pd.Predom_nbrSteps));
    pd.ionicStrength = Math.max(-100, Math.min(1000,pd.ionicStrength));
    if(!Double.isNaN(pd.temperature)) {pd.temperature = Math.max(-50, Math.min(400,pd.temperature));}
    pd.actCoeffsMethod = Math.max(0, Math.min(2,pd.actCoeffsMethod));
    pd.calcDbgHalta = Math.max(0, Math.min(6,pd.calcDbgHalta));
    pd.tolHalta = Math.max(1e-9, Math.min(0.01, pd.tolHalta));
    boolean found = false;
    if(pd.tblExtension != null && pd.tblExtension.length() == 3) {
        for(String tblExtension_type : pd.tblExtension_types) {
            if(pd.tblExtension.equalsIgnoreCase(tblExtension_type)) {found = true; break;}
        }
    }
    if(!found) {pd.tblExtension = pd.tblExtension_types[0];}
    for(int i=0; i < pd.tblFieldSeparator_types.length; i++) {
        if(pd.tblFieldSeparator ==
                pd.tblFieldSeparator_types[i]) {found = true; break;}
    }
    if(!found) {pd.tblFieldSeparator = pd.tblFieldSeparator_types[0];}
    pd.diagrConvertSizeX = Math.max(20,Math.min(300,pd.diagrConvertSizeX));
    pd.diagrConvertSizeY = Math.max(20,Math.min(300,pd.diagrConvertSizeY));
    pd.diagrConvertMarginB = Math.max(-5,Math.min(21,pd.diagrConvertMarginB));
    pd.diagrConvertMarginL = Math.max(-5,Math.min(21,pd.diagrConvertMarginL));
    pd.diagrConvertFont = Math.max(0, Math.min(3,pd.diagrConvertFont));
    boolean fnd = false;
    for(String t : FORMAT_NAMES) {if(t.equalsIgnoreCase(pd.diagrExportType)) {fnd = true; break;}}
    if(!fnd) {pd.diagrExportType = FORMAT_NAMES[0];}
    pd.diagrExportSize = Math.max(2, Math.min(5000,pd.diagrExportSize));
    laf = Math.min(2,Math.max(0,laf));
    System.out.println(LINE);
    System.out.flush();
  } // checkIniValues()

  public static boolean twoColoursEqual (java.awt.Color A, java.awt.Color B) {
      if(Math.abs(A.getRed()-B.getRed()) >25) {return false;}
      else if(Math.abs(A.getGreen()-B.getGreen()) >25) {return false;}
      else if(Math.abs(A.getBlue()-B.getBlue()) >25) {return false;}
      return true;
  } // twoColoursEqual

  public void iniDefaults() {
      // Set default values for program variables
      if (pc.dbg) {
          System.out.flush();
          System.out.println("Setting default parameter values (\"ini\"-values).");
      }
      locationFrame.x = Math.max(0, (screenSize.width  - this.getWidth() ) / 2);
      locationFrame.y = Math.max(0, (screenSize.height - this.getHeight() ) / 2);
      msgFrameSize.width = 500; msgFrameSize.height = 400;
      locationMsgFrame.x = 80; locationMsgFrame.y = 30;
      dispSize.width = 400; dispSize.height = 350;
      dispLocation.x = 60; dispLocation.y = 30;
      // set the default path to the "current directory" (from where the program is started)
      pc.setPathDef(); // set Default Path = Start Directory
      pd.advancedVersion = false;
      txtEditor = null;
      java.io.File f;
      if(windows) {
        String dir = windir;
        if(dir != null) {
            if(dir.endsWith(SLASH)) {dir = dir.substring(0, dir.length()-1);}
            txtEditor = dir + SLASH + "Notepad.exe";
            f = new java.io.File(txtEditor);
            if(!f.exists()) {txtEditor = "Notepad.exe";}
        } else {txtEditor = "Notepad.exe";}
      } else if(System.getProperty("os.name").startsWith("Mac OS")) {
          //txtEditor = "/Applications/TextEdit.app";
          txtEditor = "/usr/bin/open -t";
      } else {  //assume Unix or Linux
          txtEditor = "/usr/bin/gedit";
          f = new java.io.File(txtEditor);
          if(!f.exists()) {txtEditor = null;}
      }

      String dir = pc.pathAPP;
      if(dir != null && dir.trim().length()>0) {
          pathSedPredom = dir;
      } else {
          pathSedPredom = System.getProperty("user.dir");
      }
      getDatabaseProgr(false);

      pd.actCoeffsMethod = 2;
      pd.advancedVersion = false;
      pd.aquSpeciesOnly = false;
      pd.calcDbg = false;
      pd.calcDbgHalta = Chem.DBGHALTA_DEF;
      pd.tolHalta = Chem.TOL_HALTA_DEF;
      pd.ionicStrength = 0;
      pd.keepFrame = false;
      if(pc.pathAPP != null && pc.pathAPP.trim().length()>0) {pd.pathSIT = pc.pathAPP;} else {pd.pathSIT = ".";}
      pd.reversedConcs = false;
      pd.drawNeutralPHinPourbaix = false;
      pd.temperature = Double.NaN;
      pd.useEh = true;
      pd.SED_nbrSteps = NSTEPS_DEF;
      pd.SED_tableOutput = false;
      pd.Predom_nbrSteps = NSTEPS_DEF*2;
      pd.tblExtension = "csv";
      pd.tblFieldSeparator = ';';
      pd.tblCommentLineStart = "\"";
      pd.tblCommentLineEnd = "\"";

      diagrPaintUtil.keepAspectRatio = false;
      diagrPaintUtil.fixedSize = false;
      diagrPaintUtil.fixedSizeHeight = 15f;
      diagrPaintUtil.fixedSizeWidth = 21f;
      diagrPaintUtil.antiAliasing = 1; diagrPaintUtil.antiAliasingText = 1;
      diagrPaintUtil.colourType = 0;
      diagrPaintUtil.printColour = true;
      diagrPaintUtil.penThickness = 1f;
      diagrPaintUtil.printHeader = true;
      diagrPaintUtil.printPenThickness = 1f;
      diagrPaintUtil.fontFamily = 1; // 0 = SansSerif
      diagrPaintUtil.fontStyle = 0; // 0 = plain
      diagrPaintUtil.fontSize = 9;

      diagrPaintUtil.useBackgrndColour = false;
      diagrPaintUtil.backgrnd = java.awt.Color.white;
      diagrPaintUtil.textWithFonts = true;
      pd.fractionThreshold = 0.03f;

      pd.diagrConvertSizeX = 90;
      pd.diagrConvertSizeY = 90;
      pd.diagrConvertMarginB = 1f;
      pd.diagrConvertMarginL = 1f;
      pd.diagrConvertPortrait = true;
      pd.diagrConvertHeader = true;
      pd.diagrConvertColors = true;
      pd.diagrConvertFont = 2;
      pd.diagrConvertEPS = false;
      pd.diagrExportType = FORMAT_NAMES[0];
      pd.diagrExportSize = 1000;

    } // iniDefaults()

  private void getDatabaseProgr(final boolean print) {
    String dir = pc.pathAPP;
    if(dir != null && dir.endsWith(SLASH)) {dir = dir.substring(0, dir.length()-1);}
    java.io.File f;
    if(dir == null || dir.trim().length()<=0) {
        createDataFileProg = "DataBase.jar";
    } else {
        createDataFileProg = dir + SLASH + "DataBase.jar";
        f = new java.io.File(createDataFileProg);
        if(!f.exists() && System.getProperty("os.name").startsWith("Mac OS")) {
          createDataFileProg = dir +SLASH+".."+SLASH+".."+SLASH+".."+SLASH+".."+SLASH+"DataBase.app"
                  +SLASH+"Contents"+SLASH+"Resources"+SLASH+"Java"+SLASH+"DataBase.jar";
          f = new java.io.File(createDataFileProg);
          try{createDataFileProg = f.getCanonicalPath();}
          catch (java.io.IOException ex) {createDataFileProg = f.getAbsolutePath();}
        }
    }
    f = new java.io.File(createDataFileProg);
    if(!f.exists()) {
        if(print) {System.out.println("Warning: could NOT find the database program: "+nl+"    "+createDataFileProg);}
        createDataFileProg = null;
    } else if(print) {System.out.println("Setting database program = "+nl+"    "+createDataFileProg);}
  }

  /** Save program settings.
   * Exceptions are reported both to the console (if there is one) and to a dialog */
  private boolean saveIni(java.io.File f) {
    if(f == null) {return false;}
    if(pc.dbg) {System.out.println("--- saveIni("+f.getAbsolutePath()+")");}
    boolean ok = true;
    String msg = null;
    if(f.exists() && (!f.canWrite() || !f.setWritable(true))) {
        msg = "Error - can not write ini-file:"+nl+
              "   \""+f.getAbsolutePath()+"\""+nl+
              "The file is read-only.";
    }
    if(!f.exists() && !f.getParentFile().exists()) {
        ok = f.getParentFile().mkdirs();
        if(!ok) {
            msg = "Error - can not create directory:"+nl+
                  "   \""+f.getParent()+"\""+nl+
                  "Can not write ini-file.";
        }
    }
    if(msg != null) {
        MsgExceptn.showErrMsg(spf, msg, 2);
        return false;
    }
    java.util.Properties propertiesIni = new SortedProperties();
    if (this != null && this.isVisible()
            && locationFrame.x > -1 && locationFrame.y > -1) {
        if(this.getExtendedState()==javax.swing.JFrame.ICONIFIED // minimised?
           || this.getExtendedState()==javax.swing.JFrame.MAXIMIZED_BOTH)
                {this.setExtendedState(javax.swing.JFrame.NORMAL);}
        locationFrame.x = this.getLocation().x;
        locationFrame.y = this.getLocation().y;
    }
    propertiesIni.setProperty("<program_version>", VERS);
    propertiesIni.setProperty("defaultPath", pc.pathDef.toString());
    if(txtEditor != null) {
        propertiesIni.setProperty("txtEditor",txtEditor);
    } else {propertiesIni.setProperty("txtEditor","");}
    if(pathSedPredom != null) {
        propertiesIni.setProperty("pathSedPredom",pathSedPredom);
    } else {propertiesIni.setProperty("pathSedPredom","");}
    if(createDataFileProg != null) {
        propertiesIni.setProperty("createDataFileProg",createDataFileProg);
    } else {propertiesIni.setProperty("createDataFileProg","");}
    if(laf==2) {propertiesIni.setProperty("lookAndFeel", "CrossPlatform");}
    else if(laf==1) {propertiesIni.setProperty("lookAndFeel", "System");}
    else {propertiesIni.setProperty("lookAndFeel", "Default");}
    propertiesIni.setProperty("advancedVersion",String.valueOf(pd.advancedVersion));
    propertiesIni.setProperty("Calc_keepFrame",String.valueOf(pd.keepFrame));
    propertiesIni.setProperty("Calc_SED_nbrSteps",String.valueOf(pd.SED_nbrSteps));
    propertiesIni.setProperty("Calc_SED_tableOutput",String.valueOf(pd.SED_tableOutput));
    propertiesIni.setProperty("Calc_Predom_nbrSteps",String.valueOf(pd.Predom_nbrSteps));
    propertiesIni.setProperty("Calc_Predom_aquSpeciesOnly",String.valueOf(pd.aquSpeciesOnly));
    propertiesIni.setProperty("Calc_allowReversedConcRanges",String.valueOf(pd.reversedConcs));
    propertiesIni.setProperty("Calc_ionicStrength",String.valueOf(pd.ionicStrength));
    propertiesIni.setProperty("Calc_temperature",String.valueOf(pd.temperature));
    propertiesIni.setProperty("Calc_activityCoefficientsMethod", String.valueOf(pd.actCoeffsMethod));
    propertiesIni.setProperty("Calc_useEh",String.valueOf(pd.useEh));
    propertiesIni.setProperty("Calc_draw_pH_line",String.valueOf(pd.drawNeutralPHinPourbaix));
    propertiesIni.setProperty("Calc_dbg",String.valueOf(pd.calcDbg));
    propertiesIni.setProperty("Calc_dbgHalta",String.valueOf(pd.calcDbgHalta));
    propertiesIni.setProperty("Calc_tolerance",String.valueOf(pd.tolHalta));
    propertiesIni.setProperty("Calc_pathSIT",pd.pathSIT);
    propertiesIni.setProperty("Calc_tableFileExt",pd.tblExtension);
    propertiesIni.setProperty("Calc_tableFieldSeparatorChar",Character.toString(pd.tblFieldSeparator));
    if(pd.tblCommentLineStart != null && pd.tblCommentLineStart.length() >0) {
        propertiesIni.setProperty("Calc_tableCommentLineStart",pd.tblCommentLineStart);
    } else {propertiesIni.setProperty("Calc_tableCommentLineStart","");}
    if(pd.tblCommentLineEnd != null && pd.tblCommentLineEnd.length() >0) {
        propertiesIni.setProperty("Calc_tableCommentLineEnd",pd.tblCommentLineEnd);
    } else {propertiesIni.setProperty("Calc_tableCommentLineEnd","");}

    propertiesIni.setProperty("Disp_Width", String.valueOf(dispSize.width));
    propertiesIni.setProperty("Disp_Height", String.valueOf(dispSize.height));
    propertiesIni.setProperty("Disp_left", String.valueOf(dispLocation.x));
    propertiesIni.setProperty("Disp_top", String.valueOf(dispLocation.y));
    if(msgFrame != null) {msgFrameSize = msgFrame.getSize(); locationMsgFrame = msgFrame.getLocation();}
    propertiesIni.setProperty("LogFrame_Width", String.valueOf(msgFrameSize.width));
    propertiesIni.setProperty("LogFrame_Height", String.valueOf(msgFrameSize.height));
    propertiesIni.setProperty("LogFrame_left", String.valueOf(locationMsgFrame.x));
    propertiesIni.setProperty("LogFrame_top", String.valueOf(locationMsgFrame.y));
    propertiesIni.setProperty("Location_left", String.valueOf(locationFrame.x));
    propertiesIni.setProperty("Location_top", String.valueOf(locationFrame.y));
    propertiesIni.setProperty("Disp_KeepAspectRatio", String.valueOf(diagrPaintUtil.keepAspectRatio));
    propertiesIni.setProperty("Disp_FixedSize", String.valueOf(diagrPaintUtil.fixedSize));
    propertiesIni.setProperty("Disp_FixedSizeWidth", String.valueOf(diagrPaintUtil.fixedSizeWidth));
    propertiesIni.setProperty("Disp_FixedSizeHeight", String.valueOf(diagrPaintUtil.fixedSizeHeight));
    propertiesIni.setProperty("Disp_FontFamily", String.valueOf(diagrPaintUtil.fontFamily));
    propertiesIni.setProperty("Disp_FontStyle", String.valueOf(diagrPaintUtil.fontStyle));
    propertiesIni.setProperty("Disp_FontSize", String.valueOf(diagrPaintUtil.fontSize));
    propertiesIni.setProperty("Disp_TextWithFonts", String.valueOf(diagrPaintUtil.textWithFonts));
    propertiesIni.setProperty("Disp_FractionThreshold", String.valueOf(pd.fractionThreshold));
    String anti;
    if (diagrPaintUtil.antiAliasingText == 1) {anti="on";}
    else if (diagrPaintUtil.antiAliasingText == 2) {anti="default";}
    else {anti="off";}
    propertiesIni.setProperty("Disp_AntialiasingText", anti);
    if (diagrPaintUtil.antiAliasing == 1) {anti="on";}
    else if (diagrPaintUtil.antiAliasing == 2) {anti="default";}
    else {anti="off";}
    propertiesIni.setProperty("Disp_Antialiasing", anti);
    propertiesIni.setProperty("Disp_ColourType", String.valueOf(diagrPaintUtil.colourType));
    propertiesIni.setProperty("Disp_PrintColour", String.valueOf(diagrPaintUtil.printColour));
    propertiesIni.setProperty("Disp_PrintHeader", String.valueOf(diagrPaintUtil.printHeader));
    propertiesIni.setProperty("Disp_PrintPenThickness", String.valueOf(diagrPaintUtil.printPenThickness));
    propertiesIni.setProperty("Disp_PenThickness", String.valueOf(diagrPaintUtil.penThickness));
    propertiesIni.setProperty("Disp_UseBackgrndColour",String.valueOf(diagrPaintUtil.useBackgrndColour));
    propertiesIni.setProperty("Disp_BackgroundColour",diagrPaintUtil.backgrnd.getRed()+","+
                                                    diagrPaintUtil.backgrnd.getGreen()+","+
                                                    diagrPaintUtil.backgrnd.getBlue());
    for(int ii=0; ii < DiagrPaintUtility.MAX_COLOURS; ii++) {
        propertiesIni.setProperty("Disp_Colour["+ii+"]",diagrPaintUtil.colours[ii].getRed()+","+
                                                     diagrPaintUtil.colours[ii].getGreen()+","+
                                                     diagrPaintUtil.colours[ii].getBlue());
    }
    propertiesIni.setProperty("Convert_SizeX", String.valueOf(pd.diagrConvertSizeX));
    propertiesIni.setProperty("Convert_SizeY", String.valueOf(pd.diagrConvertSizeY));
    propertiesIni.setProperty("Convert_MarginBottom", String.valueOf(pd.diagrConvertMarginB));
    propertiesIni.setProperty("Convert_MarginLeft", String.valueOf(pd.diagrConvertMarginL));
    propertiesIni.setProperty("Convert_Portrait", String.valueOf(pd.diagrConvertPortrait));
    propertiesIni.setProperty("Convert_Font", String.valueOf(pd.diagrConvertFont));
    propertiesIni.setProperty("Convert_Colour", String.valueOf(pd.diagrConvertColors));
    propertiesIni.setProperty("Convert_Header", String.valueOf(pd.diagrConvertHeader));
    propertiesIni.setProperty("Convert_EPS", String.valueOf(pd.diagrConvertEPS));
    propertiesIni.setProperty("Export_To", pd.diagrExportType);
    propertiesIni.setProperty("Export_Size", String.valueOf(pd.diagrExportSize));

    System.out.println("Saving ini-file: \""+f.getPath()+"\"");
    java.io.FileOutputStream propertiesIniFile = null;
    try{
        propertiesIniFile = new java.io.FileOutputStream(f);
        // INI-section needed by PortableApps java launcher
        int i = nl.length();
        byte[] b = new byte[7+i];
        b[0]='['; b[1]='S'; b[2]='p'; b[3]='a'; b[4]='n'; b[5]='a'; b[6]=']';
        for(int j =0; j < i; j++) {b[7+j] = (byte)nl.codePointAt(j);}
        propertiesIniFile.write(b);
        //
        propertiesIni.store(propertiesIniFile,null);
        if (pc.dbg) {System.out.println("Written: \""+f.getPath()+"\"");}
    } catch (java.io.IOException e) {
          msg = "Error: \""+e.toString()+"\""+nl+
                "   while writing INI-file:"+nl+
                "   \""+f.getPath()+"\"";
          System.out.println(msg);
          if(!this.isVisible()) {this.setVisible(true);}
          javax.swing.JOptionPane.showMessageDialog(spf, msg, pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
          ok = false;
    }
    finally {
        try {if (propertiesIniFile != null) {propertiesIniFile.close();}}
        catch (java.io.IOException e) {ok = false;}
    } //finally
    return ok;
  } // saveIni()

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="printInstructions">
  public static void printInstructions() {
    System.out.flush();
    String msg = "Possible commands are:"+nl+
    "  -dbg            (output debugging information to the messages window)"+nl+
    "  data-file-name  (add data file to list, name must end with \".dat\")"+nl+
    "  plot-file-name  (display plot file, name must end with \".plt\")"+nl+
    "  -p=plot-file-name  (print plot file)"+nl+
    "  -pdf=plot-file-name  (convert plot file to \"pdf\" format)"+nl+
    "  -ps=plot-file-name  (convert plot file to \"PostScript\")"+nl+
    "  -eps=plot-file-name  (convert plot file to encapsulated \"PostScript\")"+nl+
    "  -ext=plot-file-name  (export plot file to \"ext\" format"+nl+
    "                        where \"ext\" is one of: bmp, jpg or png, and perhaps gif."+nl+
    "                        Plot-file-name must end with \".plt\")."+nl+
    "Enclose file names with double quotes (\"\") it they contain blank space."+nl+
    "Example:   java -jar Spana.jar \"plt\\Fe 25.plt\" -p:\"plt\\Fe I=3M\"";
    System.out.println(msg);
    System.out.flush();
    System.err.println(LINE); // show the message window
    System.err.flush();
  } //printInstructions()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="copyTextFile">
  static boolean copyTextFile(java.io.File source, java.io.File target, boolean dbg){
    if (dbg) {System.out.println("--- copyTextFile ---");}
    if(source == null) {
        MsgExceptn.exception("\"source\" file is \"null\"");
        return false;
    }
    if(target == null) {
        MsgExceptn.exception("\"target\" file is \"null\"");
        return false;
    }
    if (dbg) {System.out.println("    source: \""+source.getAbsolutePath()+"\""+nl+
                                 "    target: \""+target.getAbsolutePath()+"\"");}
    java.io.BufferedReader in;
    try{
        in = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(source)));
    }
    catch (java.io.FileNotFoundException ex) {
        MsgExceptn.exception("\"source\" file not found:"+nl+
                             "   \""+source.getAbsolutePath()+"\".");
        return false;        
    }

    java.io.BufferedWriter out;
    try {
        out = new java.io.BufferedWriter(new java.io.FileWriter(target));
    }
    catch (java.io.IOException ex) {
        MsgExceptn.exception(ex.toString()+" while writing \"target\" file:"+nl+
                             "   \""+target.getAbsolutePath()+"\".");
        return false;        
    }

    try {
        String aLine;
        while ((aLine = in.readLine()) != null) {
            //Process each line and add output to destination file
            out.write(aLine);
            out.newLine();
        }
    } catch (java.io.IOException ex) {
        MsgExceptn.exception(ex.toString()+" while reading \"source\" file:"+nl+
                "   \""+source.getAbsolutePath()+"\","+nl+
                "and writing \"target\" file:"+nl+
                "   \""+target.getAbsolutePath()+"\".");
        return false;        
    }
    finally {
        // do not forget to close the buffer reader
        try{in.close();} catch (java.io.IOException ex) {}
        // close buffer writer
        try{out.flush(); out.close();} catch (java.io.IOException ex) {}
    }
    if (dbg) {System.out.println("--- copyTextFile: OK");}
    return true;
  }
  //</editor-fold>

//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="TransferHandler for Drag-and-Drop">
//==========================================================================
// Drag and Drop:
    /** Use <code>tHandler</code> to allow Drag-and-Drop of files;
     * for example:
     * <pre>   jPanel.setTransferHandler(tHandler);</pre>
     * Where <code>jPanel</code> is a component.
     * Then change in method <code>importData</code> the code using the
     * list of file names obtained through the Drop action.
     */
public static javax.swing.TransferHandler tHandler =
            new javax.swing.TransferHandler(null) {@Override
    public boolean importData(javax.swing.JComponent component,
                                java.awt.datatransfer.Transferable t) {
        // Import file names from the Drop action
        // First check for "javaFileListFlavor"
        if (!canImport(component, t.getTransferDataFlavors()))
                {return false;}
        // Get the list of file names
        try {
            @SuppressWarnings("unchecked")
            java.util.List<java.io.File> list =
                (java.util.List<java.io.File>)t.
                    getTransferData(java.awt.datatransfer.DataFlavor.javaFileListFlavor);
            for(java.io.File f : list) {
                String fileName;
                try {fileName = f.getCanonicalPath();}
                catch (java.io.IOException ex) {
                    try {fileName = f.getAbsolutePath();}
                    catch (Exception e) {fileName = f.getPath();}
                }
                final String fileN = fileName;
                if(fileName.toLowerCase().endsWith(".plt")) {
                    spf.displayPlotFile(fileN, null);
                } // if ".plt"
                if(fileName.toLowerCase().endsWith(".dat")) {
                    spf.addDatFile(fileN);
                } // if ".dat"
            } //for f : lists
        } //try
        catch (java.awt.datatransfer.UnsupportedFlavorException e) {return false;}
        catch (java.io.IOException ex) {return false;}
        return true;
        } // importData(JComponent, Transferable)
    @Override
    public boolean canImport(javax.swing.JComponent component,
                java.awt.datatransfer.DataFlavor[] flavors) {
        // check for "javaFileListFlavor"
        boolean hasFileFlavor = false;
        for(java.awt.datatransfer.DataFlavor flavor : flavors) {
            if(java.awt.datatransfer.DataFlavor.javaFileListFlavor.equals(flavor)) {
                hasFileFlavor = true;
                return true;
            }
        }
        return false;
        } // canImport(JComponent, DataFlavor[])
    }; // TransferHandler tHandler
//==========================================================================
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="main">
  /** Starts "Spana". If another instance is running, send the command
   * arguments to the other instance and quit, otherwise, start a MainFrame.
   * @param args the command line arguments
   */
  public static void main(final String[] args) {
    System.out.println("Starting Spana - version "+VERS);
    //---- deal with some command-line arguments
    boolean dbg = false;
    if(args.length > 0) {
        for(String arg : args) {
            if (arg.equalsIgnoreCase("-dbg") || arg.equalsIgnoreCase("/dbg")) {
                System.out.println("Command-line argument = \"" + arg + "\"");
                dbg = true;
            }
            if (arg.equals("-?") || arg.equals("/?") || arg.equals("?")
                    || arg.equals("-help") || arg.equals("--help")) {
                System.out.println("Command-line argument = \"" + arg + "\"");
                printInstructions();
            } //if args[] = "?"
        }
    }

    //---- is there another instance already running?
    if((new OneInstance()).findOtherInstance(args, 56055, "Spana", dbg)) {
        System.out.println("---- Already running.");
        return;
    }

    //---- create a local instance of ProgramConf.
    //     Contains information read from the configuration file.
    final ProgramConf pc = new ProgramConf("Spana");
    pc.dbg = dbg;

    //---- all output to System.err will show the error in a frame.
    if(msgFrame == null) {
      msgFrame = new RedirectedFrame(500, 400, pc);
      msgFrame.setVisible(dbg);
      System.out.println("Spana diagram - version "+VERS);
    }

    //---- is it Windows?
    if(System.getProperty("os.name").toLowerCase().startsWith("windows")) {
        windows = true;
        // get windows directory
        try{windir = System.getenv("windir");}
        catch (Exception ex) {
            System.out.println("Warning: could not get environment variable \"windir\"");
            windir = null;
        }
        if(windir != null && windir.trim().length() <= 0) {windir = null;}
        if(windir != null) {
            java.io.File f = new java.io.File(windir);
            if(!f.exists() || !f.isDirectory()) {windir = null;}
        }
    }

    //---- set Look-And-Feel
    //     laf = 0
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

    //---- read the CFG-file
    java.io.File fileNameCfg;
    String dir = pc.pathAPP;
    if(dir != null && dir.trim().length()>0) {
        if(dir.endsWith(SLASH)) {dir = dir.substring(0, dir.length()-1);}
        fileNameCfg = new java.io.File(dir + SLASH + pc.progName+".cfg");
    } else {fileNameCfg = new java.io.File(pc.progName+".cfg");}
    ProgramConf.read_cfgFile(fileNameCfg, pc);
    if(!pc.dbg) {pc.dbg=dbg;}
    msgFrame.setVisible(pc.dbg);

    java.text.DateFormat dateFormatter =
            java.text.DateFormat.getDateTimeInstance
                (java.text.DateFormat.DEFAULT, java.text.DateFormat.DEFAULT, java.util.Locale.getDefault());
    java.util.Date today = new java.util.Date();
    String dateOut = dateFormatter.format(today);
    System.out.println("\"Spana\" started: "+dateOut);
    System.out.println(LINE);

    //---- set Default Path = Start Directory
    pc.setPathDef();

    //---- show the main window
    java.awt.EventQueue.invokeLater(new Runnable() {@Override public void run() {
          // create the frame object
          spf = new MainFrame(pc, msgFrame); //send configuration data
          // process command-line arguments etc
          spf.start(args);
        } // run
    }); // invokeLater

  } //main(args)
  // </editor-fold>

  public static MainFrame getInstance() {return spf;}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuDebug;
    private javax.swing.JComboBox<String> jComboBox_Dat;
    private javax.swing.JComboBox<String> jComboBox_Plt;
    private javax.swing.JLabel jLabelBackgrd;
    private javax.swing.JLabel jLabel_Dat;
    private javax.swing.JLabel jLabel_Plt;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenuItem jMenu_Data_AddToList;
    private javax.swing.JMenuItem jMenu_Data_Edit;
    private javax.swing.JMenuItem jMenu_Data_Modif;
    private javax.swing.JMenuItem jMenu_Data_New;
    private javax.swing.JMenuItem jMenu_Data_Open;
    private javax.swing.JMenuItem jMenu_Data_SaveAs;
    private javax.swing.JMenu jMenu_File;
    private javax.swing.JMenu jMenu_File_Data;
    private javax.swing.JMenuItem jMenu_File_Exit;
    private javax.swing.JMenu jMenu_File_Plot;
    private javax.swing.JMenu jMenu_Help;
    private javax.swing.JMenuItem jMenu_Help_About;
    private javax.swing.JMenuItem jMenu_Help_Contents;
    private javax.swing.JMenuItem jMenu_Plot_Open;
    private javax.swing.JMenuItem jMenu_Plot_SaveAs;
    private javax.swing.JMenu jMenu_Prefs;
    private javax.swing.JMenuItem jMenu_Prefs_Calcs;
    private javax.swing.JMenuItem jMenu_Prefs_Diagr;
    private javax.swing.JMenuItem jMenu_Prefs_General;
    private javax.swing.JMenu jMenu_Run;
    private javax.swing.JMenuItem jMenu_Run_Cmd;
    private javax.swing.JMenuItem jMenu_Run_Database;
    private javax.swing.JMenuItem jMenu_Run_FileExpl;
    private javax.swing.JMenuItem jMenu_Run_MakeDiagr;
    private javax.swing.JMenuItem jMenu_Run_Modif;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparatorCmd;
    private javax.swing.JPopupMenu.Separator jSeparatorMake;
    // End of variables declaration//GEN-END:variables
}
