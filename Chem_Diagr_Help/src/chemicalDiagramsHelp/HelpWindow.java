package chemicalDiagramsHelp;

/** This java program will display a JavaHelp helpset.
 * I may be run as stand alone to provide help information, or it can
 * act as a "slave" of another application. If another "master" application
 * (e.g. Spana or DataBase) wants to display help using this HelpWindow, it can:
 * <ul><li> either start another independent Java Virtual Machine
 *      (java -jar jarFileName.jar "helpID"); or<br>
 * <li> use JarClassLoader to execute the main method with the "helpID" as argument; or<br>
 * <li> if this jar-file is made accessible through CLASSPATH at run-time
 *      (and as a library at compile time), then the "master" application can
 *      have direct access to the public methods in order to bring the help
 *      window forward and to change the "help ID".  For example:</ul>
 *<pre>      chemicalDiagramsHelp.HelpWindow hw = null;
 *      hw = chemicalDiagramsHelp.HelpWindow.getInstance();
 *      if(hw == null || hw.hFrame == null)  {
 *          String[] argsHW = {"M_Working_with_htm"};
 *          chemicalDiagramsHelp.HelpWindow.main(argsHW);}
 *      else {
 *          hw.hFrame.setVisible(true);
 *          hw.setHelpID("M_Working_with_htm");}</pre>
 * The helpset to display must be found either:<ul>
 *   <li> first the file "javahelp/helpset.hs" (external to the jar-file) is searched.
 *   <li> if the external helpset is not found, then the jar-file of this program
 *     is searched: first in a location specified by the entry "Helpset" in the
 *     jar-file's manifest ("META-INF/MANIFEST.MF").
 *   <li> if there is no Manifest-entry in the jar file of this program,
 *     then the helpset is searched inside the jar-file at: "javahelp/helpset.hs".</ul>
 * Many ideas taken from the QuickHelp class available at www.halogenware.com
 * <br>
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
public class HelpWindow {
    private static final String VERS = "2020-June-10";
    private final ProgramConf pc;
    private static boolean started = false;
    /** Because the program checks for other instances and exits if there is
     * another instance, "helpWindow" is a reference to the only instance of
     * this class. Used for example in "main" to decide if an object instance
     * needs to be created */
    private static HelpWindow helpWindow;
    private static String jarName = null;
    /** a reference to the helpWindow frame */
    public javax.swing.JFrame hFrame = null;
    private javax.help.HelpSet hs = null;
    javax.help.HelpBroker hb;
    private javax.swing.JPanel jPanel_font_size;
    private javax.swing.JComboBox jComboBox_font;
    private javax.swing.JComboBox jComboBox_size;
    private javax.swing.JLabel jLabel_status;
    private boolean isSlave = false;
    // variables used for user messages:
    private String helpSetFileNameWithPath;
    private java.io.File fileINI = null;
    private static final String FileINI_NAME = ".ChemDiagrHelp.ini";
    private int left;
    private int top;
    private int width;
    private int height;
    private String hbFont;
    private int hbSize;
    private int windowState;
    /** New-line character(s) to substitute "\n" */
    private static final String nl = System.getProperty("line.separator");
    private static final String line = "-----";
    private static final String SLASH = java.io.File.separator;
  
  //<editor-fold defaultstate="collapsed" desc="main">
  /** Displays a JavaHelp system
   * @param args the command line arguments: at most two arguments are read.
   * If one of the first two arguments is "-dbg" or "/dbg" then debug information
   * is printed. The other argument may be a "helpID" from the helpSet. */
  public static void main(String args[]) {
    boolean dbg = false;
    // --- debug?
    if(args.length >0) {
        if(args[0].equalsIgnoreCase("-dbg") || args[0].equalsIgnoreCase("/dbg")) {
            dbg = true;
            args[0] = "";
            //if 2 arguments given: move the second argument to be the first
            if(args.length >1) {args[0] = args[1]; args[1] = "";}
        }
        if(args.length >1) {
            if(args[1].equalsIgnoreCase("-dbg") || args[1].equalsIgnoreCase("/dbg")) {
                dbg = true; args[1] = "";
            }
        }
    }
    String progName = HelpWindow.class.getPackage().getName();
    if(dbg) {System.out.println(line+" "+progName+" - (version "+VERS+") - Starting...");}
    try { Class c = Class.forName("javax.help.HelpSet"); }
    catch (ClassNotFoundException ex) {
        String t = "Error - file \"jh.jar\" not found. Can Not show help.";
        OutMsg(t);
        ErrMsgBx mb = new ErrMsgBx(t,progName);
        return;
    }

    //---- is there another instance already running?
    if(new OneInstance().findOtherInstance(args, 56100, progName, dbg)) {
        OutMsg("Already running.");
        return;
    }
    // ---- get jar file name
    java.util.jar.JarFile jarFile = getRunningJarFile();
    if(jarFile != null) {
        jarName = getRunningJarFile().getName();
        java.io.File jf = new java.io.File(jarName);
        jarName = jf.getName();
    }
    if(jarName != null) {
        if(jarName.toLowerCase().endsWith(".jar"))
                {progName = jarName.substring(0,jarName.lastIndexOf("."));}
        else {progName = jarName;}
    }
    //---- create a local instance of ProgramConf.
    //     Contains information read from the configuration file.
    final ProgramConf pc = new ProgramConf(progName);
    pc.dbg = dbg;


    // --- set SystemLookAndFeel
    try{
        OutMsg("setting look-and-feel: \"System\"");
        javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
    }
    catch (Exception e) {OutMsg(e.getMessage());}

    //---- read the CFG-file
    java.io.File fileNameCfg;
    String dir = getPathApp();
    if(dir != null && dir.trim().length()>0) {
        if(dir.endsWith(SLASH)) {dir = dir.substring(0, dir.length()-1);}
        fileNameCfg = new java.io.File(dir + SLASH + pc.progName+".cfg");
    } else {fileNameCfg = new java.io.File(pc.progName+".cfg");}
    ProgramConf.read_cfgFile(fileNameCfg, pc);
    if(!pc.dbg) {pc.dbg=dbg;}

    // --- helpID?
    final String helpID;
    if(args.length >0) {
        if(args[0].trim().length()>1) {helpID = args[0];} else {helpID = null;}
    } else {helpID = null;}
    // --- If the Java Help is already "alive" just bring it to front
    if(helpWindow != null && helpWindow.hb != null) {
        if(pc.dbg){
            String msg = "helpWindow not null";
            if(helpID != null) {msg = msg + ";  helpID=\""+helpID+"\"";}
            OutMsg(msg);
        }
        if(helpID != null && helpID.length() >0) {
            java.awt.EventQueue.invokeLater(new Runnable() {@Override public void run() {
                if(pc.dbg){OutMsg("helpWindow.setHelpID("+helpID+")");}
                helpWindow.setHelpID(helpID);
            }}); // invokeLater (new Runnable)
        }
    } // if helpWindow !=null

    if(pc.dbg){
        String msg = "Starting HelpWindow - (version "+VERS+")";
        if(helpID != null) {msg = msg + ";  helpID=\""+helpID+"\"";}
        OutMsg(msg);
    }

    // ----- Construct a new HelpWindow
    // --- Is this executed stand-alone (starting a Java Virtual Machine),
    //     or is it called from another Java application?
    String callingClass = ClassLocator.getCallerClassName();
    if(pc.dbg){OutMsg("Has been called from:  "+callingClass);}
    boolean standAlone = true;
    if(!callingClass.contains("HelpWindow")) { // called from another application
      if(pc.dbg){OutMsg("Stand alone = false");}
      standAlone = false;
    } else {if(pc.dbg){OutMsg("Stand alone = true");}}

    final boolean isSlave0 = !standAlone;
    java.awt.EventQueue.invokeLater(new Runnable() {@Override public void run() {
        HelpWindow hw = HelpWindow.getInstance();
        if(hw == null) {
            if(pc.dbg){OutMsg("No previous instance found. Constructing.");}
            HelpWindow hw1 = new HelpWindow(isSlave0, pc);
        } else {
            if(pc.dbg){OutMsg("Previous instance found. bringToFront();");}
            hw.bringToFront();
        }
        if(helpID != null && helpID.length() >0) {
            // put the "setHelpID(helpID)" in the Event queue
            java.awt.EventQueue.invokeLater(new Runnable() {@Override public void run() {
                HelpWindow hw = HelpWindow.getInstance();
                if(hw != null && hw.hb != null) {
                    if(pc.dbg){OutMsg("HelpWindow.main: setHelpID("+helpID+")");}
                    hw.setHelpID(helpID);
                } else {
                    if(pc.dbg){OutMsg("HelpWindow.main: getInstance() is null");}
                }
            }});
        } // if helpID !=null
    }}); // invokeLater (new Runnable)
    //return;
  } // main(args[])

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  private HelpWindow(final boolean isSlave0, ProgramConf pc0){ // show a HelpSet
    isSlave = isSlave0;
    pc = pc0;
    javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
        helpWindow = HelpWindow.this;
    }});

    // ---- next line must be *BEFORE* starting JavaHelp
    javax.help.SwingHelpUtilities.setContentViewerUI("chemicalDiagramsHelp.ExternalLinkContentViewerUI");

    // ---- read the INI-file
    readIni();

    // ---- get the Helpset in variable "hs"
    ClassLoader cl = HelpWindow.class.getClassLoader();
    final String def_HelpSetFileName = "helpset.hs";
    // ----  get the name of the HelpSet from the Jar-Manifest (if any)
    String helpSetFileName = null;
    if(jarName == null) {
        helpSetFileName = "javahelp/"+def_HelpSetFileName;
    }else {
        try {
            for(java.util.Enumeration e = cl.getResources(java.util.jar.JarFile.MANIFEST_NAME);
                        e.hasMoreElements();) {
                java.net.URL url = (java.net.URL)e.nextElement();
                helpSetFileName = new java.util.jar.Manifest(url.openStream()).getMainAttributes().getValue("Helpset");
                if(helpSetFileName != null) {break;}
            } //for Enumeration e
        } // try
        catch (java.io.IOException ex) {
            String msg = "Error \""+ex.toString()+"\"";
            ErrMsgBx mb = new ErrMsgBx(msg, pc.progName);
        } //catch
    } //if jarName !=null

    if(helpSetFileName == null) {
        if(pc.dbg && jarName != null) {
            OutMsg("Warning: value \"Helpset\" not found"+nl+
                    "in jar-file manifest: \""+
                        jarName+SLASH+java.util.jar.JarFile.MANIFEST_NAME+"\"");
        }
        // no manifest value; assume it is there in any case
        helpSetFileName = "javahelp/"+def_HelpSetFileName;
    }

    // ---- try to get a helpset in the jar-file
    if(!getHelpSet(cl, helpSetFileName)) {end_program(); return;}
    if(hs != null) {
    }

    if(hs != null) {
        if(jarName != null) {
            helpSetFileNameWithPath = jarName+"$"+helpSetFileName;
        } else {
            helpSetFileNameWithPath = hs.getHelpSetURL().getFile();
        }
        if(pc.dbg) {
            OutMsg("Found helpset URL=\""+helpSetFileNameWithPath+"\"");
        }
    } else {
        String msg = "Error: HelpSet \""+def_HelpSetFileName+"\" not found!";
        if(jarName != null) {msg = msg +nl+ "in jar-file: "+jarName;}
        ErrMsgBx mb = new ErrMsgBx(msg, pc.progName);
        end_program();
        return;
    } // if hs = null

    // ---- create HelpBroker
    String msgEx = null;
    try {
        if(hb == null) {
            if(pc.dbg) {OutMsg("creating helpBroker");}
            hb = hs.createHelpBroker();
        }
    } catch (Exception e) {
        hb = null;
        msgEx = e.toString();
    } //catch
    if(hb == null){
        String msg;
        if(msgEx == null) {msg = "Error: Could not create HelpBroker for HelpSet:"+nl+
                "\""+helpSetFileNameWithPath+"\".";
        } else {msg = "Error: "+msgEx+nl+
                "Can not create HelpBroker for HelpSet:"+nl+
                "\""+helpSetFileNameWithPath+"\".";}
        ErrMsgBx mb = new ErrMsgBx(msg, pc.progName);
        end_program();
        return;
    }

    // ---- get the WindowPresentation and the JFrame
    javax.help.DefaultHelpBroker dhb = (javax.help.DefaultHelpBroker)hb;
    try{
        if(hb.getCurrentView() == null) {
            if(pc.dbg) {OutMsg("initPresentation();");}
            hb.initPresentation();
        }
    } catch (javax.help.UnsupportedOperationException ex) {
        ErrMsgBx mb = new ErrMsgBx(ex.toString(), pc.progName);
    }

    javax.help.WindowPresentation wp = ((javax.help.DefaultHelpBroker)hb).getWindowPresentation();
    if(wp == null) {
        if(pc.dbg) {OutMsg("initPresentation()");}
        hb.initPresentation();
        wp = ((javax.help.DefaultHelpBroker)hb).getWindowPresentation();
    }
    hFrame = (javax.swing.JFrame)wp.getHelpWindow();
    hFrame.setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
    hFrame.addWindowListener(
        new java.awt.event.WindowAdapter() {
        @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {end_program();}
        }
    );

    // ---- change the Icon
    String iconName = "images/help_16x16.gif";
    java.net.URL imgURL = this.getClass().getResource(iconName);
    java.awt.Image icon;
    if (imgURL != null) {
        icon = new javax.swing.ImageIcon(imgURL).getImage();
        hFrame.setIconImage(icon);
        //com.apple.eawt.Application.getApplication().setDockIconImage(new javax.swing.ImageIcon("Football.png").getImage());
        if(System.getProperty("os.name").startsWith("Mac OS")) {
            try {
                Class<?> c = Class.forName("com.apple.eawt.Application");
                //Class params[] = new Class[] {java.awt.Image.class};
                java.lang.reflect.Method m =
                    c.getDeclaredMethod("setDockIconImage",new Class[] { java.awt.Image.class });
                Object i = c.newInstance();
                java.net.URL iconURL = this.getClass().getResource("images/help_48x48.gif");
                if (iconURL != null) {icon = new javax.swing.ImageIcon(iconURL).getImage();}
                Object paramsObj[] = new Object[]{icon};
                m.invoke(i, paramsObj);
            } catch (Exception e) {OutMsg("Error: "+e.getMessage());}
        }
    } else {
        OutMsg("Error: Could not load image = \""+iconName+"\"");
    }

    // ---- create a Panel with 2 Combo Boxes
    //      for the Font and the Font-Size
    jPanel_font_size = new javax.swing.JPanel();
    //--- Create a Combo Box and add the font list
    jComboBox_font = new javax.swing.JComboBox<String>( // jComboBox_font = new javax.swing.JComboBox( // java 1.6
                java.awt.GraphicsEnvironment.
                    getLocalGraphicsEnvironment().
                        getAvailableFontFamilyNames());
    //--- Create a Combo Box and add the size list
    jComboBox_size = new javax.swing.JComboBox<>( // jComboBox_size = new javax.swing.JComboBox( // java 1.6
            new String[] {"8","9","10","11","12","14","16","18","20",
                                "22","24","26","28","36","48","72"});
    //--- Arrange the Combo Boxes in the Panel
    java.awt.FlowLayout jPanel_Layout = new java.awt.FlowLayout(java.awt.FlowLayout.LEADING);
    jPanel_font_size.setLayout(jPanel_Layout);
    jPanel_font_size.add(jComboBox_font);
    jPanel_font_size.add(jComboBox_size);
    jPanel_font_size.add(javax.swing.Box.createHorizontalGlue());

    // ---- add the Panel at the top of the help window
    hFrame.getContentPane().add(jPanel_font_size, java.awt.BorderLayout.PAGE_START);

    // Events/Action/ActionPerformed
    jComboBox_font.addActionListener(
      new java.awt.event.ActionListener() {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            hbFont = jComboBox_font.getSelectedItem().toString();
            java.awt.Font f = hb.getFont();
            hb.setFont(new java.awt.Font(hbFont, java.awt.Font.PLAIN, f.getSize()));
        } // actionPerformed
      }  // new ActionListener
    ); // addActionListener

    // Events/Action/ActionPerformed
    jComboBox_size.addActionListener(
      new java.awt.event.ActionListener() {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            hbSize = Integer.parseInt(jComboBox_size.getSelectedItem().toString());
            float fSize = (float) hbSize;
            // force a font change even if the the size is the same as before:
            hb.setFont(hb.getFont().deriveFont(fSize - 0.5f));
            hb.setFont(hb.getFont().deriveFont(fSize));
        } // actionPerformed
      }  // new ActionListener
    ); // addActionListener

    // ---- add a status bar at the bottom of the window
    jLabel_status = new javax.swing.JLabel(" ");
    jLabel_status.setBorder(javax.swing.BorderFactory.createCompoundBorder(
        javax.swing.BorderFactory.createLoweredBevelBorder(),
        javax.swing.BorderFactory.createEmptyBorder(0,5,0,5))); // leave space to the left and right
    hFrame.getContentPane().add(jLabel_status, java.awt.BorderLayout.PAGE_END);
    jLabel_status.setText("Helpset: "+helpSetFileNameWithPath);

    // ---- display the HelpBroker
    hb.setDisplayed(true);

    // ---- set the window location
    java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    hFrame.setLocation(Math.min(screenSize.width-100, Math.max(0,left)), Math.min(screenSize.height-100, Math.max(0,top)));
    hFrame.setSize(Math.max(325,width), Math.max(215,height));
    hFrame.validate();
    hFrame.setExtendedState(windowState);

    // ---- change the font and font-size
    int found = -1;
    for(int i = 0; i < jComboBox_font.getItemCount(); i++)
        {if(jComboBox_font.getItemAt(i).toString().toLowerCase().startsWith(hbFont.toLowerCase()))
            {jComboBox_font.setSelectedIndex(i); found = i; break;}} //for
    if(found < 0) {hbFont = "Tahoma";
            for(int i = 0; i < jComboBox_font.getItemCount(); i++) {
                if(jComboBox_font.getItemAt(i).toString().toLowerCase().startsWith(hbFont.toLowerCase()))
                        {jComboBox_font.setSelectedIndex(i); break;}} //for
    } // if(found < 0)
    found = -1;
    for(int i = 0; i < jComboBox_size.getItemCount(); i++) {
        if(Float.parseFloat(jComboBox_size.getItemAt(i).toString()) == hbSize) {
            jComboBox_size.setSelectedIndex(i);
            found = i; break;
        }
    } // for
    if(found < 0) {
        hbSize = 14;
        for(int i = 0; i < jComboBox_size.getItemCount(); i++) {
            if(Float.parseFloat(jComboBox_size.getItemAt(i).toString()) == hbSize) {
                jComboBox_size.setSelectedIndex(i); break;
            }
        } // for
    } //if(found < 0)

    // ---- change the status bar when the ID changes
    wp.setTitleFromDocument(true);
    java.beans.PropertyChangeListener pcl = new java.beans.PropertyChangeListener() {
          @Override
          public void propertyChange(java.beans.PropertyChangeEvent event) {
            String property = event.getPropertyName();
            if ("title".equals(property)) {
                try{
                    if(hb != null && hb.getCurrentID() != null)
                            {jLabel_status.setText(hb.getCurrentID().getURL().getPath());}
                } catch (java.net.MalformedURLException ex) {
                    jLabel_status.setText(hb.getCurrentID().getIDString());
                }
            }
          }
    };
    hFrame.addPropertyChangeListener(pcl);

  } //  Constructor for: HelpWindow(isSlave)

 //</editor-fold>

  /** Return a reference to this instance
   * @return this instance  */
    public static HelpWindow getInstance() {return helpWindow;}

  //<editor-fold defaultstate="collapsed" desc="setHelpID">
    public void setHelpID(String startHelpID) {
        if(startHelpID == null || startHelpID.length() <=0) {return;}
        
        bringToFront();
        if(helpWindow != null && helpWindow.hb != null) {
            if(pc.dbg){OutMsg("setHelpID using \""+startHelpID+"\"");}
            try{helpWindow.hb.setCurrentID(startHelpID);}
            catch (javax.help.BadIDException ex) {
                String msg = "Error: can not set help ID to: \""+startHelpID+"\"";
                OutMsg(msg);
                if(this.hFrame != null) {
                    javax.swing.JOptionPane.showMessageDialog(this.hFrame,msg,pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
                } else {
                    ErrMsgBx mb = new ErrMsgBx(msg, pc.progName);
                }
            } // catch
        } else {
            String msg = "Warning: in setHelpID ";
            if(helpWindow == null) {msg = msg + "helpWindow = \"null\".";}
            else if(helpWindow.hb == null) {msg = msg + "HelpBroker = \"null\".";}
            OutMsg(msg);
        }
        //return;
    } // setHelpID
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="bringToFront">
  public void bringToFront(){
    if(helpWindow != null && helpWindow.hFrame != null) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                helpWindow.hFrame.setVisible(true);
                if((helpWindow.hFrame.getExtendedState() & javax.swing.JFrame.ICONIFIED) // minimised?
                            == javax.swing.JFrame.ICONIFIED) {
                    helpWindow.hFrame.setExtendedState(javax.swing.JFrame.NORMAL);
                } // if minimized
                helpWindow.hFrame.setAlwaysOnTop(true);
                helpWindow.hFrame.toFront();
                helpWindow.hFrame.requestFocus();
                helpWindow.hFrame.setAlwaysOnTop(false);
            }
        });

    }
  } // bringToFront()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="end_program">
  private void end_program() {
    if(isSlave) {
        if(hFrame == null) {return;}
        if(pc.dbg) {OutMsg("- - - - setVisible(false) - - - -");}
        hFrame.setVisible(false);
        return;
    }
    if(fileINI != null) {saveIni(fileINI);}
    if(pc.dbg) {OutMsg("- - - - end_program - - - -");}
    if(hFrame != null) {hFrame.dispose();}
    hFrame = null;
    helpWindow = null;
    OneInstance.endCheckOtherInstances();
    } // end_program()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="getHelpSet">
  private boolean getHelpSet(ClassLoader cl, String HelpSetFileName) {
    java.net.URL hs_Url;
    try{hs_Url = javax.help.HelpSet.findHelpSet(cl, HelpSetFileName);}
    catch (java.lang.NoClassDefFoundError ex) {
        String msg = "Serious Error: \"" + ex.toString()+"\""+nl+
            "Library \"lib"+java.io.File.separator+"jh.jar\" not found!";
        ErrMsgBx mb = new ErrMsgBx(msg, pc.progName);
        return false;
    }
    if(hs_Url != null) {
        try{hs = new javax.help.HelpSet(cl, hs_Url);}
        catch (javax.help.HelpSetException ex) {
            String msg = "HelpSet error: \"" + ex.toString()+"\""+nl+
                "for HelpSet URL = \""+hs_Url.getPath()+"\"";
            OutMsg(msg);
            ErrMsgBx mb = new ErrMsgBx(msg, pc.progName);
            hs = null;
            return true;
        }
    } // if(hs_Url != null)
    if(hs == null) {
        if(pc.dbg){OutMsg("Note: HelpSet \""+HelpSetFileName+
                "\" not found in class loader.");}
    }
    return true;
  } // getHelpSet_in_jarFile

    //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Read_Write_INI">

  /** Read program variables saved when the program was previously closed */
  private void readIni() {
    iniDefaults();  // needed to initialise arrays etc.
    if(pc.dbg) {OutMsg("--- readIni() ---  reading ini-file(s)");}
    fileINI = null;
    java.io.File p = null, fileRead = null, fileINInotRO = null;
    boolean ok, readOk = false;
    //--- check the application path ---//
    String pathAPP = getPathApp();
    if(pathAPP == null || pathAPP.trim().length() <=0) {
        if(pc.saveIniFileToApplicationPathOnly) {
            String name = "\"null\"" + SLASH + FileINI_NAME;
            String msg = "Error: can not read ini file"+nl+
                         "    "+name+nl+
                         "    (application path is \"null\")";
            OutMsg(msg);
            ErrMsgBx mb = new ErrMsgBx(msg, pc.progName);
            return;
        }
    } else { //pathApp is defined
        String dir = pathAPP;
        if(dir.endsWith(SLASH)) {dir = dir.substring(0, dir.length()-1);}
        fileINI = new java.io.File(dir + SLASH + FileINI_NAME);
        p = new java.io.File(dir);
        if(!p.exists()) {
            p = null;  fileINI = null;
            if(pc.saveIniFileToApplicationPathOnly) {
                String msg = "Error: can not read ini file:"+nl+
                             "    "+fileINI.getPath()+nl+
                             "    (application path does not exist)";
                OutMsg(msg);
                ErrMsgBx mb = new ErrMsgBx(msg, pc.progName);
                return;
            }
        }
    }
    success: {
        // --- first read the ini-file from the application path, if possible
        if(pc.saveIniFileToApplicationPathOnly && fileINI != null) {
            // If the ini-file must be written to the application path,
            // then try to read this file, even if the file is write-protected
            fileINInotRO = fileINI;
            if(fileINI.exists()) {
                readOk = readIni2(fileINI);
                if(readOk) {fileRead = fileINI;}
            }
            break success;
        } else { // not saveIniFileToApplicationPathOnly or fileINI does not exist
            if(fileINI != null && fileINI.exists()) {
                readOk = readIni2(fileINI);
                if(readOk) {fileRead = fileINI;}
                if(fileINI.canWrite() && fileINI.setWritable(true)) {
                    fileINInotRO = fileINI;
                    if(readOk) {break success;}
                }
            } else { //ini-file null or does not exist
                if(fileINI != null && p != null) {
                    try{ // can we can write to this directory?
                            java.io.File tmp = java.io.File.createTempFile("eqHelp",".tmp", p);
                            ok = tmp.exists();
                            if(ok) {tmp.delete();}
                    } catch (java.io.IOException ex) {ok = false;}
                    // file does not exist, but the path is not write-protected
                    if(ok && fileINInotRO == null) {fileINInotRO = fileINI;}
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
            fileINI = new java.io.File(t+SLASH+".config"+SLASH+"eq-diagr"+SLASH+FileINI_NAME);
            if(fileINI.exists()) {
                readOk = readIni2(fileINI);
                if(readOk) {fileRead = fileINI;}
                if(fileINI.canWrite() && fileINI.setWritable(true)) {
                    if(fileINInotRO == null) {fileINInotRO = fileINI;}
                    if(readOk) {break success;}
                }
            } else { //ini-file does not exist
                try{ // can we can write to this directory?
                    p =  new java.io.File(t);
                    java.io.File tmp = java.io.File.createTempFile("eqHelp",".tmp", p);
                    ok = tmp.exists();
                    if(ok) {tmp.delete();}
                } catch (java.io.IOException ex) {ok = false;}
                // file does not exist, but the path is not write-protected
                if(ok && fileINInotRO == null) {fileINInotRO = fileINI;}
            }
        } // for(dirs)
    } //--- success?
    if(!readOk) {OutMsg("Could not read any INI-file.");}
    if(fileINInotRO != null && fileINInotRO != fileRead) {
        ok = saveIni(fileINInotRO);
        if(ok) {fileINI = fileINInotRO;} else {fileINI = null;}
    }
  } // Read_ini()

  private boolean readIni2(java.io.File f) {
    System.out.flush();
    OutMsg("Reading ini-file: \""+f.getPath()+"\"");
    java.util.Properties ini= new java.util.Properties();
    java.io.FileInputStream fis = null;
    java.io.BufferedReader r = null;
    boolean ok = true;
    try {
      fis = new java.io.FileInputStream(f);
      r = new java.io.BufferedReader(new java.io.InputStreamReader(fis, "UTF8"));
      ini.load(r);
    } catch (java.io.FileNotFoundException e) {
      OutMsg("Warning: file Not found: \""+f.getPath()+"\""+nl+
             "    using default parameter values.");
      ok = false;
    }
    catch (java.io.IOException e) {
      String msg = "Error: \""+e.toString()+"\""+nl+
                   "   while loading INI-file:"+nl+
                   "   \""+f.getPath()+"\"";
      OutMsg(msg);
      ErrMsgBx mb = new ErrMsgBx(msg, pc.progName);
      ok = false;
    } // catch loading-exception
    try {if(r != null) {r.close();} if(fis != null) {fis.close();}}
    catch (java.io.IOException e) {
        String msg ="Error: \""+e.toString()+"\""+nl+
                    "   while closing INI-file:"+nl+
                    "   \""+f.getPath()+"\"";
        ErrMsgBx mb = new ErrMsgBx(msg, pc.progName);
        ok = false;
    }
    finally {
        try {if(r != null) {r.close();} if(fis != null) {fis.close();}}
        catch (java.io.IOException e) {
            String msg = "Error: \""+e.toString()+"\""+nl+
                          "   while closing INI-file:"+nl+
                          "   \""+f.getPath()+"\"";
            ErrMsgBx mb = new ErrMsgBx(msg, pc.progName);
        }
    }
    if(!ok) {return ok;}
    try {
        left = Integer.parseInt(ini.getProperty("left"));
        top = Integer.parseInt(ini.getProperty("top"));
        width = Integer.parseInt(ini.getProperty("width"));
        height = Integer.parseInt(ini.getProperty("height"));
        hbFont = ini.getProperty("font");
        hbSize = Integer.parseInt(ini.getProperty("font_size"));
        windowState = Integer.parseInt(ini.getProperty("window_state"));
    } catch (java.lang.NumberFormatException e) {
        String msg = "Error: \""+e.toString()+"\""+nl+
                         "   while reading INI-file:"+nl+
                         "   \""+f.getPath()+"\""+nl+nl+
                         "Setting default program parameters.";
        ErrMsgBx mb = new ErrMsgBx(msg, pc.progName);
        ok = false;
    }
    if(pc.dbg) {OutMsg("Finished reading ini-file");}
    System.out.flush();
    checkIniValues();
    return ok;
  }

  private void checkIniValues() {
    left = Math.max(0,left);        top = Math.max(0,top);
    width = Math.max(325,width);    height = Math.max(215,height);
    hbSize = Math.max(8,Math.min(hbSize,72));
    // do not start with windowState = JFrame.ICONIFIED
    if(windowState != javax.swing.JFrame.NORMAL &&
        windowState != javax.swing.JFrame.MAXIMIZED_BOTH) {
                        windowState = javax.swing.JFrame.NORMAL;}
    //return;
  } // checkIniValues()

  /** Set default values for program variables */
  private void iniDefaults() {
    left = 60;  top = 0;  width = 600;  height = 550;
    hbFont = "Tahoma";  hbSize = 14;
    windowState = javax.swing.JFrame.NORMAL;
    //return;
  } // iniDefaults()

  /** Save program variables (when the program ends, etc) */
  private boolean saveIni(java.io.File f) {
    if(f == null) {return false;} // this will not happen
    if(pc.dbg) {OutMsg("Writing ini-file "+f.getAbsolutePath());}
    boolean ok = true;
    String msg = null;
    if(f.exists() && (!f.canWrite() || !f.setWritable(true))) {
        msg = "Error - can not write ini-file:"+nl+
              "  \""+f.getAbsolutePath()+"\""+nl+
              "  The file is read-only.";
    }
    if(!f.exists() && !f.getParentFile().exists()) {
        ok = f.getParentFile().mkdirs();
        if(!ok) {
            msg = "Error - can not create directory:"+nl+
                  "  \""+f.getParent()+"\""+nl+
                  "  Can not write ini-file.";
        }
    }
    if(msg != null) {
        OutMsg(msg);
        if(hFrame != null && hFrame.isVisible()) {
            javax.swing.JOptionPane.showMessageDialog(hFrame, msg, pc.progName,
                javax.swing.JOptionPane.WARNING_MESSAGE);
        } else {
            ErrMsgBx mb = new ErrMsgBx(msg, pc.progName);
        }
        return false;
    }
    if(helpWindow != null && helpWindow.hFrame != null) {
        if((helpWindow.hFrame.getExtendedState() &
                javax.swing.JFrame.MAXIMIZED_BOTH)
                        != javax.swing.JFrame.MAXIMIZED_BOTH) {
            left = helpWindow.hFrame.getX();
            top = helpWindow.hFrame.getY();
            width = helpWindow.hFrame.getWidth();
            height = helpWindow.hFrame.getHeight();
            } // if getExtendedState
        windowState = helpWindow.hFrame.getExtendedState();
    // hbFont and hbSize are set in the ActionListeners
    }
    java.util.Properties ini = new java.util.Properties();
    ini.setProperty("width", String.valueOf(width));
    ini.setProperty("height", String.valueOf(height));
    ini.setProperty("left", String.valueOf(left));
    ini.setProperty("top", String.valueOf(top));
    ini.setProperty("font", hbFont);
    ini.setProperty("font_size", String.valueOf(hbSize));
    ini.setProperty("window_state", String.valueOf(windowState));

    java.io.FileOutputStream fos = null;
    java.io.Writer w = null;
    try{
        fos = new java.io.FileOutputStream(f);
        w = new java.io.BufferedWriter(new java.io.OutputStreamWriter(fos, "UTF8"));
        ini.store(w, null);
    }
    catch (java.io.IOException ex) {
        msg = "Error \""+ex.toString()+"\""+nl+
            "while writing INI-file: \""+f.toString()+"\"";
        OutMsg(msg);
        ErrMsgBx mb = new ErrMsgBx(msg, pc.progName);
        ok = false;
    } // catch store-exception
    finally {
        try{if(w != null) {w.close();} if(fos != null) {fos.close();}}
        catch (java.io.IOException e) {ok = false;}
    }
    return ok;
  } // Save_ini

  //</editor-fold>

//<editor-fold defaultstate="collapsed" desc="getPathApp">
/** Get the path where an application is located.
 * @return the directory where the application is located,
 * or "user.dir" if an error occurs
 * @version 2014-Jan-17 */
  private static class C {private static void C(){}}
  public static String getPathApp() {
    C c = new C();
    String path;
    java.net.URI dir;
    try{
        dir = c.getClass().
                getProtectionDomain().
                    getCodeSource().
                        getLocation().
                            toURI();
        if(dir != null) {
            String d = dir.toString();
            if(d.startsWith("jar:") && d.endsWith("!/")) {
                d = d.substring(4, d.length()-2);
                dir = new java.net.URI(d);
            }
            path = (new java.io.File(dir.getPath())).getParent();
        } else {path = System.getProperty("user.dir");}
    }
    catch (java.net.URISyntaxException e) {
      if(!started) {
        ErrMsgBx emb = new ErrMsgBx("Error: "+e.toString()+nl+
                    "   trying to get the application's directory.", "Help Window");
      }
      path = System.getProperty("user.dir");
    } // catch
    started = true;
    return path;
  } //getPathApp()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="getRunningJarFile()">
/** Find out the jar file that contains this class
 * @return a File object of the jar file containing the enclosing class "Main",
 * or null if it is not containing inside a jar file.  */
public static java.util.jar.JarFile getRunningJarFile() {
  //from http://www.rgagnon.com/javadetails/
  //and the JarClassLoader class
  C c = new C();
  String className = c.getClass().getName().replace('.', '/');
  // class = "progPackage.Main";   className = "progPackage/Main"
  java.net.URL url = c.getClass().getResource("/" + className + ".class");
  // url = "jar:file:/C:/Eq-Calc_Java/dist/Prog.jar!/progPackage/Main.class"
  if(url.toString().startsWith("jar:")) {
    java.net.JarURLConnection jUrlC;
    try{
        jUrlC = (java.net.JarURLConnection)url.openConnection();
        return jUrlC.getJarFile();
    } catch(java.io.IOException ex) {
        ErrMsgBx mb = new ErrMsgBx("Error "+ex.toString(), "Help Window");
        return null;
    }
  }
  return null;
} //getRunningJarFile()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="OutMsg and ErrMsg">
/** Prints a message to System.out surrounded by a lines:<br>
 * ---------- Chemical Diagrams Help:<br>
 * The error message here<br>
 * ----------<br>
 * If the msessage is short (less than 50 chars) then the format is:<br>
 * ---------- Chemical Diagrams Help: The error message here<br>
 * This is useful if the Help is called from within DataBase or Spana,
 * so that messages from the Help may be differenciated from messages
 * by the calling program.
 * @param txt the message to print  */
static void OutMsg(String txt) {
    String msg;
    if(txt.length()>=50) {msg = line+" Chemical Diagrams Help:"+nl+txt+nl+line;}
    else {msg = line+" Chemical Diagrams Help: "+txt;}
    System.out.println(msg);
    System.out.flush();
}
/** Prints a message to System.err surrounded by a lines:<br>
 * ---------- Chemical Diagrams Help:<br>
 * The error message here<br>
 * ----------<br>
 * If the msessage is short (less than 50 chars) then the format is:<br>
 * ---------- Chemical Diagrams Help: The error message here<br>
 * This is useful if the Help is called from within DataBase or Spana,
 * so that messages from the Help may be differenciated from messages
 * by the calling program.
 * @param txt the message to print  */
static void ErrMsg(String txt) {
    String msg;
    if(txt.length()>=50) {msg = line+" Chemical Diagrams Help:"+nl+txt+nl+line;}
    else {msg = line+" Chemical Diagrams Help: "+txt;}
    System.err.println(msg);
    System.err.flush();
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="ErrMsgBx">
  /** Displays a "message box" modal dialog with an "OK" button.<br>
   * Why is this needed? For any java console application: if started using
   * javaw.exe (on Windows) or through a ProcessBuilder, no console will appear.
   * Error messages are then "lost" unless a log-file is generated and the user
   * reads it. This class allows the program to stop running and wait for the user
   * to confirm that the error message has been read.
   * <br>
   * A small frame (window) is first created and made visible. This frame is
   * the parent to the modal "message box" dialog, and it has an icon on the
   * task bar (Windows).  Then the modal dialog is displayed on top of the
   * small parent frame.
   * <br>
   * Copyright (C) 2015-2018  I.Puigdomenech.
   * @author Ignasi Puigdomenech
   * @version 2015-July-14 */
  static class ErrMsgBx {

  /** Displays a "message box" modal dialog with an "OK" button and a title.
   * The message is displayed in a text area (non-editable),
   * which can be copied and pasted elsewhere.
   * @param msg will be displayed in a text area, and line breaks may be
   * included, for example: <code>new MsgBox("Very\nbad!",""); </code>
   * If null or empty nothing is done.
   * @param title for the dialog. If null or empty, "Error:" is used
   * @version 2018-May-13 */
  public ErrMsgBx(String msg, String title) {
    if(msg == null || msg.trim().length() <=0) {
        System.err.println("--- ErrMsgBx: null or empty \"message\".");
        return;
    }
    //--- Title
    if(title == null || title.length() <=0) {title = " Error:";}
    java.awt.Frame frame = new java.awt.Frame(title);
    //--- Icon
    String iconName = "images/ErrMsgBx.gif";
    java.net.URL imgURL = this.getClass().getResource(iconName);
    if(imgURL != null) {frame.setIconImage(new javax.swing.ImageIcon(imgURL).getImage());}
    else {System.err.println("--- Error in ErrMsgBx constructor: Could not load image = \""+iconName+"\"");}
    frame.pack();
    //--- centre Window frame on Screen
    java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    int left; int top;
    left = Math.max(55, (screenSize.width  - frame.getWidth() ) / 2);
    top = Math.max(10, (screenSize.height - frame.getHeight()) / 2);
    frame.setLocation(Math.min(screenSize.width-100, left), Math.min(screenSize.height-100, top));
    //---
    final String msgText = wrapString(msg.trim(),80);
    //System.out.println("--- MsgBox:"+nl+msgText+nl+"---");
    frame.setVisible(true);
    //javax.swing.JOptionPane.showMessageDialog(frame, msg, title, javax.swing.JOptionPane.ERROR_MESSAGE);
    MsgBoxDialog msgBox = new MsgBoxDialog(frame, msgText, title, true);
    msgBox.setVisible(true); // becase the dialog is modal, statements below will wait
    msgBox.dispose();
    frame.setVisible(false);
    frame.dispose();
  }

/** Returns an input string, with lines that are longer than <code>maxLength</code>
 * word-wrapped and indented. * 
 * @param s input string
 * @param maxLength if an input line is longer than this length,
 * the line will be word-wrapped at the first white space after <code>maxLength</code>
 * and indented with 4 spaces
 * @return string with long-lines word-wrapped
 */
    public static String wrapString(String s, int maxLength) {
        String deliminator = "\n";
        StringBuilder result = new StringBuilder();
        StringBuffer wrapLine;
        int lastdelimPos;
        for (String line : s.split(deliminator, -1)) {
            if(line.length()/(maxLength+1) < 1) {
                result.append(line).append(deliminator);
            }
            else { //line too long, try to split it
                wrapLine = new StringBuffer();
                lastdelimPos = 0;
                for (String token : line.trim().split("\\s+", -1)) {
                    if (wrapLine.length() - lastdelimPos + token.length() > maxLength) {
                        if(wrapLine.length()>0) {wrapLine.append(deliminator);}
                        wrapLine.append("    ").append(token);
                        lastdelimPos = wrapLine.length() + 1;
                    } else {
                        if(wrapLine.length() <=0) {wrapLine.append(token);}
                        else {wrapLine.append(" ").append(token);}
                    }
                }
                result.append(wrapLine).append(deliminator);
            }
        }
        return result.toString();
    }

  //<editor-fold defaultstate="collapsed" desc="MsgBoxDialog">
  private static class MsgBoxDialog extends java.awt.Dialog {
    private java.awt.Button ok;
    private java.awt.Panel p;
    private final java.awt.TextArea text;

    /**  Creates new form NewDialog */
    public MsgBoxDialog(java.awt.Frame parent, String msg, String title, boolean modal) {
        super(parent, (" "+title), modal);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent evt) {
                MsgBoxDialog.this.setVisible(false);
            }
        });
        setLayout(new java.awt.BorderLayout());
        p = new java.awt.Panel();
        p.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
        ok = new java.awt.Button();

        // find out the size of the message (width and height)
        final int wMax = 85; final int hMax=20;
        final int wMin = 5; final int hMin = 1;
        int w = wMin;
        int h=hMin; int i=0; int j=wMin;
        final String eol = "\n";  char c;
        final String nl = System.getProperty("line.separator");
        while (true) {
            c = msg.charAt(i);
            String s = String.valueOf(c);
            if(s.equals(eol) || s.equals(nl)) {
                h++; j=wMin;
            } else {
                j++; w = Math.max(j,w);
            }
            i++;
            if(i >= msg.length()-1) {break;}
        }

        // create a text area
        int scroll = java.awt.TextArea.SCROLLBARS_NONE;
        if(w > wMax && h <= hMax) {scroll = scroll & java.awt.TextArea.SCROLLBARS_HORIZONTAL_ONLY;}
        if(h > hMax && w <= wMax) {scroll = scroll & java.awt.TextArea.SCROLLBARS_VERTICAL_ONLY;}
        if(w > wMax && h > hMax) {scroll = java.awt.TextArea.SCROLLBARS_BOTH;}
        w = Math.min(Math.max(w,10),wMax);
        h = Math.min(h,hMax);
        text = new java.awt.TextArea(msg, h, w, scroll);
        text.setEditable(false);
        //text.setBackground(java.awt.Color.white);
        text.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(java.awt.event.KeyEvent evt) {
                if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER
                    || evt.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {closeDialog();}
                if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_TAB) {ok.requestFocusInWindow();}
            }
        });
        text.setBackground(java.awt.Color.WHITE);
        text.setFont(new java.awt.Font("monospaced", java.awt.Font.PLAIN, 12));
        add(text, java.awt.BorderLayout.CENTER);

        ok.setLabel("OK");
        ok.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeDialog();
            }
        });
        ok.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER
                        || evt.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {closeDialog();}
            }
        });
        p.add(ok);

        add(p, java.awt.BorderLayout.SOUTH);

        pack();
        ok.requestFocusInWindow();

        //--- centre Window frame on Screen
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int left; int top;
        left = Math.max(55, (screenSize.width  - getWidth() ) / 2);
        top = Math.max(10, (screenSize.height - getHeight()) / 2);
        setLocation(Math.min(screenSize.width-100, left), Math.min(screenSize.height-100, top));

    }

    private void closeDialog() {this.setVisible(false);}

  } // private static class MsgBoxDialog
  //</editor-fold>
}  // static class ErrMsgBx
  //</editor-fold>

}
