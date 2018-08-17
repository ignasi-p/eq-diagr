package spana;

import lib.Version;
import lib.common.MsgExceptn;
import lib.huvud.LicenseFrame;

/** Shows a window frame with "help-about" information, versions, etc.
 * <br>
 * Copyright (C) 2014-2018 I.Puigdomenech
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
public class HelpAbout extends javax.swing.JFrame {
  private boolean finished = false;
  private HelpAbout frame = null;
  private LicenseFrame lf = null;
  private java.awt.Dimension windowSize = new java.awt.Dimension(300,200);
  /** New-line character(s) to substitute "\n" */
  private static final String nl = System.getProperty("line.separator");

  /** Creates new form HelpAboutF
   * @param pathAPP */
  public HelpAbout(final String pathAPP) {
    initComponents();
    finished = false;
    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    //--- Close window on ESC key
    javax.swing.KeyStroke escKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_ESCAPE,0, false);
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
    //--- alt-Q exit
    javax.swing.KeyStroke altQKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altQKeyStroke,"ALT_Q");
    getRootPane().getActionMap().put("ALT_Q", escAction);
    //--- Enter exit
    javax.swing.KeyStroke enterKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_ENTER, 0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(enterKeyStroke,"_Enter");
    getRootPane().getActionMap().put("_Enter", escAction);
    //--- Title, etc
    //getContentPane().setBackground(new java.awt.Color(255, 255, 153));
    getContentPane().setBackground(java.awt.Color.white);
    this.setTitle("Spana: About");
    //---- Icon
    String iconName = "images/Question_16x16.gif";
    java.net.URL imgURL = this.getClass().getResource(iconName);
    if(imgURL != null) {this.setIconImage(new javax.swing.ImageIcon(imgURL).getImage());}
    else {System.out.println("Error: Could not load image = \""+iconName+"\"");}
    //
    jLabelVers.setText("Program version: "+MainFrame.VERS);
    //
    jLabelLib.setText("LibChemDiagr: "+Version.version());
    //
    if(pathAPP != null) {
        if(pathAPP.trim().length()>0) {jLabelPathApp.setText(pathAPP);}
        else {jLabelPathApp.setText(" \"\"");}
    }
    jLabelPathUser.setText(System.getProperty("user.home"));
    this.validate();
    int w = jLabelPathApp.getWidth()+40;
    if(w > this.getWidth()) {this.setSize(w, this.getHeight());}
    //
    if(MainFrame.fileIni != null) {jLabelIniFile.setText(MainFrame.fileIni.getPath());}
    jLabelJavaVers.setText("Java Runtime Environment "+System.getProperty("java.version"));
    jLabelOS.setText("<html>Operating system: \""+
            System.getProperty("os.name")+"&nbsp;"+System.getProperty("os.version")+"\"<br>"+
            "architecture: \""+System.getProperty("os.arch")+"\"</html>");

    this.validate();
    w = jLabelIniFile.getWidth()+40;
    if(w > this.getWidth()) {this.setSize(w, this.getHeight());}
    // Center the window on the screen
    int left = Math.max(0,(MainFrame.screenSize.width-this.getWidth())/2);
    int top = Math.max(0,(MainFrame.screenSize.height-this.getHeight())/2);
    this.setLocation(Math.min(MainFrame.screenSize.width-100, left),
                     Math.min(MainFrame.screenSize.height-100, top));

  } //constructor

  public void start() {
    this.setVisible(true);
    windowSize = this.getSize();
    frame = this;
  }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelName = new javax.swing.JLabel();
        jLabelVers = new javax.swing.JLabel();
        jLabelLibs = new javax.swing.JLabel();
        jLabelLib = new javax.swing.JLabel();
        jLabelJVectClipb = new javax.swing.JLabel();
        jLabelJVectClipb_www = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jButtonLicense = new javax.swing.JButton();
        jLabel_wwwKTH = new javax.swing.JLabel();
        jLabel_www = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabelJava = new javax.swing.JLabel();
        jLabelJavaIcon = new javax.swing.JLabel();
        jPanelNetBeans = new javax.swing.JPanel();
        jLabelNetbeansIcon = new javax.swing.JLabel();
        jLabelNetBeans = new javax.swing.JLabel();
        jLabelNetBeans_www = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabelJavaVers = new javax.swing.JLabel();
        jLabelOS = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jLabelPathA = new javax.swing.JLabel();
        jLabelPathApp = new javax.swing.JLabel();
        jLabelPathU = new javax.swing.JLabel();
        jLabelPathUser = new javax.swing.JLabel();
        jLabelIniF = new javax.swing.JLabel();
        jLabelIniFile = new javax.swing.JLabel();

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

        jLabelName.setText("<html><font size=+1><b>Spana</b></font> &nbsp;&nbsp; © 2012-2018 &nbsp; I.Puigdomenech<br>\nThis program comes with<br>\nABSOLUTELY NO WARRANTY.<br>\nThis is free software, and you may<br>\nredistribute it under the GNU GPL license.</html>"); // NOI18N
        jLabelName.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jLabelVers.setText(" Program version: 2010-March-30");

        jLabelLibs.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        jLabelLibs.setText("Libraries:");

        jLabelLib.setText("LibChemDiagr: 2011-March-30");

        jLabelJVectClipb.setText("jvect-clipboard 1.3 "); // NOI18N

        jLabelJVectClipb_www.setForeground(new java.awt.Color(0, 0, 221));
        jLabelJVectClipb_www.setText("<html><u>sourceforge.net/projects/jvect-clipboard/</u></html>"); // NOI18N
        jLabelJVectClipb_www.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabelJVectClipb_www.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelJVectClipb_wwwMouseClicked(evt);
            }
        });

        jPanel2.setOpaque(false);

        jButtonLicense.setMnemonic('l');
        jButtonLicense.setText("License Details");
        jButtonLicense.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLicenseActionPerformed(evt);
            }
        });

        jLabel_wwwKTH.setForeground(new java.awt.Color(0, 0, 221));
        jLabel_wwwKTH.setText("<html><u>www.kth.se/che/medusa</u></html>"); // NOI18N
        jLabel_wwwKTH.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel_wwwKTH.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel_wwwKTHMouseClicked(evt);
            }
        });

        jLabel_www.setForeground(new java.awt.Color(0, 0, 221));
        jLabel_www.setText("<html><u>sites.google.com/site/chemdiagr/</u></html>"); // NOI18N
        jLabel_www.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel_www.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel_wwwMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_www, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_wwwKTH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(32, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jButtonLicense)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jButtonLicense)
                .addGap(18, 18, 18)
                .addComponent(jLabel_wwwKTH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(jLabel_www, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(42, Short.MAX_VALUE))
        );

        jLabelJava.setText("<html>Java Standard Edition<br> Development Kit 7 (JDK 1.7)</html>"); // NOI18N

        jLabelJavaIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/spana/images/Java_24x24.gif"))); // NOI18N
        jLabelJavaIcon.setIconTextGap(0);

        jPanelNetBeans.setOpaque(false);

        jLabelNetbeansIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/spana/images/Netbeans.gif"))); // NOI18N

        jLabelNetBeans.setText("NetBeans IDE 8.0"); // NOI18N

        jLabelNetBeans_www.setForeground(new java.awt.Color(0, 0, 221));
        jLabelNetBeans_www.setText("<html><u>netbeans.org</u></html>"); // NOI18N
        jLabelNetBeans_www.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabelNetBeans_www.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelNetBeans_wwwMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanelNetBeansLayout = new javax.swing.GroupLayout(jPanelNetBeans);
        jPanelNetBeans.setLayout(jPanelNetBeansLayout);
        jPanelNetBeansLayout.setHorizontalGroup(
            jPanelNetBeansLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelNetBeansLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabelNetbeansIcon)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelNetBeans)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelNetBeans_www, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelNetBeansLayout.setVerticalGroup(
            jPanelNetBeansLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelNetBeansLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanelNetBeansLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelNetbeansIcon)
                    .addGroup(jPanelNetBeansLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabelNetBeans_www, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelNetBeans, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );

        jPanel1.setOpaque(false);

        jLabelJavaVers.setText("jLabelJavaVers");

        jLabelOS.setText("<html>Operating system:<br>\"os.name &nbsp;os.version\" \narchitecture: \"os.arch\"</html>");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabelOS)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabelJavaVers)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(jLabelJavaVers)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelOS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabelPathA.setLabelFor(jLabelPathA);
        jLabelPathA.setText("Application path:"); // NOI18N

        jLabelPathApp.setText("\"null\""); // NOI18N

        jLabelPathU.setText("User \"home\" directory:");

        jLabelPathUser.setText("\"null\"");

        jLabelIniF.setLabelFor(jLabelIniFile);
        jLabelIniF.setText("Ini-file:"); // NOI18N

        jLabelIniFile.setText("\"null\""); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabelJavaIcon)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabelJava, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanelNetBeans, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jSeparator3)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabelPathA)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabelPathApp))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabelPathU)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabelPathUser))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabelIniF)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabelIniFile)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabelLib)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabelJVectClipb)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabelJVectClipb_www, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(jLabelLibs)
                            .addComponent(jLabelVers)
                            .addComponent(jLabelName, javax.swing.GroupLayout.PREFERRED_SIZE, 331, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabelName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelVers)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelLibs)
                        .addGap(2, 2, 2)
                        .addComponent(jLabelLib)
                        .addGap(2, 2, 2)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelJVectClipb)
                            .addComponent(jLabelJVectClipb_www, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelJavaIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelJava, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelNetBeans, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelPathA)
                    .addComponent(jLabelPathApp))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelPathU)
                    .addComponent(jLabelPathUser))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelIniF)
                    .addComponent(jLabelIniFile))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel_wwwKTHMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_wwwKTHMouseClicked
      this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
      jLabel_wwwKTH.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("https://www.kth.se/che/medusa/",this);
      this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
      jLabel_wwwKTH.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
}//GEN-LAST:event_jLabel_wwwKTHMouseClicked

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeWindow();
    }//GEN-LAST:event_formWindowClosing

    private void jLabelJVectClipb_wwwMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelJVectClipb_wwwMouseClicked
      this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
      jLabelJVectClipb_www.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("https://sourceforge.net/projects/jvect-clipboard/",this);
      this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
      jLabelJVectClipb_www.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jLabelJVectClipb_wwwMouseClicked

    private void jLabelNetBeans_wwwMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelNetBeans_wwwMouseClicked
      this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
      jLabelNetBeans_www.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("https://netbeans.org/",this);        
      this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
      jLabelNetBeans_www.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jLabelNetBeans_wwwMouseClicked

    private void jLabel_wwwMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_wwwMouseClicked
      this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
      jLabel_www.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("https://sites.google.com/site/chemdiagr/",this);
      this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
      jLabel_www.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jLabel_wwwMouseClicked

private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
    if(windowSize != null) {
        int w = Math.round((float)windowSize.getWidth());
        int h = Math.round((float)windowSize.getHeight());
        if(this.getHeight()<h){this.setSize(this.getWidth(), h);}
        if(this.getWidth()<w){this.setSize(w,this.getHeight());}
    }
}//GEN-LAST:event_formComponentResized

    private void jButtonLicenseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLicenseActionPerformed
        if(lf != null) {return;} //this should not happen
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        Thread licShow = new Thread() {@Override public void run(){
            jButtonLicense.setEnabled(false);
            lf = new LicenseFrame(HelpAbout.this);
            lf.setVisible(true);
            HelpAbout.this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            lf.waitFor();
            javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
                lf = null;
                jButtonLicense.setEnabled(true);
            }}); //invokeLater(Runnable)
        }};//new Thread
        licShow.start();
    }//GEN-LAST:event_jButtonLicenseActionPerformed

  public void closeWindow() {
    if(lf != null) {lf.closeWindow(); lf = null;}
    finished = true;
    this.notify_All();
    this.setVisible(false);
    frame = null;
    this.dispose();
  } // closeWindow()

  private synchronized void notify_All() {notifyAll();}

  /** this method will wait for this dialog frame to be closed */
  public synchronized void waitFor() {
    while(!finished) {
        try {wait();} catch (InterruptedException ex) {}
    } // while
  } // waitFor()

  public void bringToFront() {
    if(frame != null) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                frame.setVisible(true);
                if((frame.getExtendedState() & javax.swing.JFrame.ICONIFIED) // minimised?
                            == javax.swing.JFrame.ICONIFIED) {
                    frame.setExtendedState(javax.swing.JFrame.NORMAL);
                } // if minimized
                frame.setAlwaysOnTop(true);
                frame.toFront();
                frame.requestFocus();
                frame.setAlwaysOnTop(false);
            }
        });
    }
  } // bringToFront()

  //<editor-fold defaultstate="collapsed" desc="class BareBonesBrowserLaunch">

/**
 * <b>Bare Bones Browser Launch for Java</b><br>
 * Utility class to open a web page from a Swing application
 * in the user's default browser.<br>
 * Supports: Mac OS X, GNU/Linux, Unix, Windows XP/Vista/7<br>
 * Example Usage:<code><br> &nbsp; &nbsp;
 *    String url = "http://www.google.com/";<br> &nbsp; &nbsp;
 *    BareBonesBrowserLaunch.openURL(url);<br></code>
 * Latest Version: <a href=http://centerkey.com/java/browser>centerkey.com/java/browser</a><br>
 * Author: Dem Pilafian<br>
 * WTFPL -- Free to use as you like
 * @version 3.2, October 24, 2010
 */
private static class BareBonesBrowserLaunch {

   static final String[] browsers = { "x-www-browser", "google-chrome",
      "firefox", "opera", "epiphany", "konqueror", "conkeror", "midori",
      "kazehakase", "mozilla", "chromium" };  // modified by Ignasi (added "chromium")
   static final String errMsg = "Error attempting to launch web browser";

   /**
    * Opens the specified web page in the user's default browser
    * @param url A web address (URL) of a web page (ex: "http://www.google.com/")
    */
   public static void openURL(String url, javax.swing.JFrame parent) {  // modified by Ignasi (added "parent")
      try {  //attempt to use Desktop library from JDK 1.6+
         Class<?> d = Class.forName("java.awt.Desktop");
         d.getDeclaredMethod("browse", new Class[] {java.net.URI.class}).invoke(
            d.getDeclaredMethod("getDesktop").invoke(null),
            new Object[] {java.net.URI.create(url)});
         //above code mimicks:  java.awt.Desktop.getDesktop().browse()
         }
      catch (Exception ignore) {  //library not available or failed
         String osName = System.getProperty("os.name");
         try {
            if (osName.startsWith("Mac OS")) {
               Class.forName("com.apple.eio.FileManager").getDeclaredMethod(
                  "openURL", new Class[] {String.class}).invoke(null,new Object[] {url});
            }
            else if (osName.startsWith("Windows"))
               Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            else { //assume Unix or Linux
               String browser = null;
               for (String b : browsers)
                  if (browser == null && Runtime.getRuntime().exec(new String[]
                        {"which", b}).getInputStream().read() != -1)
                     Runtime.getRuntime().exec(new String[] {browser = b, url});
               if (browser == null)
                  throw new Exception(java.util.Arrays.toString(browsers));
            }
         }
         catch (Exception e) {
            MsgExceptn.exception(errMsg + "\n" + e.getMessage()); // added by Ignasi
            javax.swing.JOptionPane.showMessageDialog(parent, errMsg + "\n" + e.getMessage(), // modified by Ignasi (added "parent")
                    "Chemical Equilibrium Diagrams", javax.swing.JOptionPane.ERROR_MESSAGE);  // added by Ignasi
         }
      }
   }

}
  //</editor-fold>

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonLicense;
    private javax.swing.JLabel jLabelIniF;
    private javax.swing.JLabel jLabelIniFile;
    private javax.swing.JLabel jLabelJVectClipb;
    private javax.swing.JLabel jLabelJVectClipb_www;
    private javax.swing.JLabel jLabelJava;
    private javax.swing.JLabel jLabelJavaIcon;
    private javax.swing.JLabel jLabelJavaVers;
    private javax.swing.JLabel jLabelLib;
    private javax.swing.JLabel jLabelLibs;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JLabel jLabelNetBeans;
    private javax.swing.JLabel jLabelNetBeans_www;
    private javax.swing.JLabel jLabelNetbeansIcon;
    private javax.swing.JLabel jLabelOS;
    private javax.swing.JLabel jLabelPathA;
    private javax.swing.JLabel jLabelPathApp;
    private javax.swing.JLabel jLabelPathU;
    private javax.swing.JLabel jLabelPathUser;
    private javax.swing.JLabel jLabelVers;
    private javax.swing.JLabel jLabel_www;
    private javax.swing.JLabel jLabel_wwwKTH;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelNetBeans;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    // End of variables declaration//GEN-END:variables

}
