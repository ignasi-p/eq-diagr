package lib.huvud;

/** Show a window frame with the GNU General Public License.
 *
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
public class LicenseFrame extends javax.swing.JFrame {
    private boolean finished = false;
    /** New-line character(s) to substitute "\n" */
    private static final String nl = System.getProperty("line.separator");

    /** Creates new form LicenseFrame
     * @param parentFrame  */
    public LicenseFrame(final javax.swing.JFrame parentFrame) {
        initComponents();

        setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
        //--- ESC: close frame - if parent frame visible: request focus
        javax.swing.KeyStroke escKeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(escKeyStroke,"ESCAPE");
        javax.swing.Action escAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if(parentFrame != null && parentFrame.isVisible()) {parentFrame.requestFocus();}
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
        // ---- Title, etc
        //getContentPane().setBackground(new java.awt.Color(255, 255, 153));
        this.setTitle(" GNU General Public License");
        // ---- Icon
        String iconName = "images/GNU_32x32.gif";
        java.net.URL imgURL = this.getClass().getResource(iconName);
        if(imgURL != null) {this.setIconImage(new javax.swing.ImageIcon(imgURL).getImage());}
        else {System.out.println("Error: Could not load image = \""+iconName+"\"");}
        //---- Centre window on parent/screen
        int left,top;
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        if(parentFrame != null) {
            left = Math.max(0,(parentFrame.getX() + (parentFrame.getWidth()/2) - this.getWidth()/2));
            top = Math.max(0,(parentFrame.getY()+(parentFrame.getHeight()/2)-this.getHeight()/2));
        } else {
            left = Math.max(0,(screenSize.width-this.getWidth())/2);
            top = Math.max(0,(screenSize.height-this.getHeight())/2);
        }
        this.setLocation(Math.min(screenSize.width-this.getWidth()-20,left),
                     Math.min(screenSize.height-this.getHeight()-20, top));        
        // ----
        java.net.URL licenseURL = LicenseFrame.class.getResource("GPL.html");
        if (licenseURL != null) {
            try {
                jEditorPane.setPage(licenseURL);
            } catch (java.io.IOException e) {
                System.err.println("Attempted to read a bad URL: " + licenseURL);
            }
        } else {
            System.err.println("Couldn't find file: GPL.html");
            if(parentFrame != null && parentFrame.isVisible()) {parentFrame.requestFocus();}
            javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
                closeWindow();
            }}); //invokeLater(Runnable)
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane = new javax.swing.JScrollPane();
        jEditorPane = new javax.swing.JEditorPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jEditorPane.setEditable(false);
        jEditorPane.setContentType("text/html"); // NOI18N
        jEditorPane.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jEditorPaneKeyPressed(evt);
            }
        });
        jScrollPane.setViewportView(jEditorPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 544, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeWindow();
    }//GEN-LAST:event_formWindowClosing

    private void jEditorPaneKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jEditorPaneKeyPressed
      if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {evt.consume(); closeWindow();}
    }//GEN-LAST:event_jEditorPaneKeyPressed

  public void closeWindow() {
    this.setVisible(false);
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
  

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane jEditorPane;
    private javax.swing.JScrollPane jScrollPane;
    // End of variables declaration//GEN-END:variables
}
