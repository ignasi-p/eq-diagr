package lib.huvud;

import lib.common.Util;

/** A Java Swing class that captures error output to the console
 * (eg, <code>System.err.println</code>).
 * 
 * From http://www.rgagnon.com/javadetails/java-0435.html
 *
 * http://tanksoftware.com/juk/developer/src/com/
 *     tanksoftware/util/RedirectedFrame.java
 * A Java Swing class that captures output to the command line 
 ** (eg, System.out.println)
 * RedirectedFrame
 * <p>
 * This class was downloaded from:
 * Java CodeGuru (http://codeguru.earthweb.com/java/articles/382.shtml) <br>
 * The origional author was Real Gagnon (real.gagnon@tactika.com);
 * William Denniss has edited the code, improving its customizability
 *
 * In breif, this class captures all output to the system and prints it in
 * a frame. You can choose weither or not you want to catch errors, log 
 * them to a file and more.
 * For more details, read the constructor method description
 *  
 * Modified by Ignasi Puigdomenech (2014-2016) to use a rotating Logger
 *
 * @author Real Gagnon */
public class RedirectedFrame extends javax.swing.JFrame {
    //http://tanksoftware.com/juk/developer/src/com/
    //     tanksoftware/util/RedirectedFrame.java
    private final ProgramConf pc;
    private javax.swing.JFrame parentFrame;
    private RedirectedFrame f;
    private boolean loading;
    private boolean popupOnErr = true;
    private final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    /** New-line character(s) to substitute "\n" */
    private static final String nl = System.getProperty("line.separator");

    java.io.PrintStream errPrintStream  =
       new java.io.PrintStream(
         new errFilteredStream(
           new java.io.ByteArrayOutputStream()));
    java.io.PrintStream outPrintStream  =
       new java.io.PrintStream(
         new outFilteredStream(
           new java.io.ByteArrayOutputStream()));

  //<editor-fold defaultstate="collapsed" desc="Constructor">
/** <p>Creates a new <code>RedirectedFrame</code>. From the moment
 * it is created, all System.out messages and error messages are diverted
 * to this frame and appended to the log file.
 * For example:</p>
 *  <pre><code>RedirectedFrame outputFrame =
 *       new RedirectedFrame(700, 600);</code></pre>
 * this will create a new <code>RedirectedFrame</code> with the
 * dimentions 700x600. Can be toggled to visible or hidden by a controlling
 * class by <code>outputFrame.setVisible(true|false)</code>.
 * @param width the width of the frame
 * @param height the height of the frame
 * @param pc0  */
    public RedirectedFrame(int width, int height, ProgramConf pc0) {
        initComponents();
        this.pc = pc0;
        parentFrame = null;
        loading = true;

        setTitle(pc.progName+" - Error and Debug output:");
        setSize(width,height);

        this.setLocation(80, 28);

        setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
        // ---- if main frame visible: change focus on ESC key
        javax.swing.KeyStroke escKeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(escKeyStroke,"ESCAPE");
        javax.swing.Action escAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if(parentFrame != null && parentFrame.isVisible()) {
                    if((parentFrame.getExtendedState() & javax.swing.JFrame.ICONIFIED)
                            == javax.swing.JFrame.ICONIFIED) {
                        parentFrame.setExtendedState(javax.swing.JFrame.NORMAL);
                    } // if minimized
                    parentFrame.toFront();
                    parentFrame.requestFocus();
                }
            }};
        getRootPane().getActionMap().put("ESCAPE", escAction);
        // ---- Alt-S close window
        javax.swing.KeyStroke altSKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK, false);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altSKeyStroke,"ALT_S");
        javax.swing.Action altSAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                jButtonClose.doClick();
            }};
        getRootPane().getActionMap().put("ALT_S", altSAction);

        //--- Icon
        String iconName;
        if(pc.progName.equalsIgnoreCase("spana")) {
            iconName = "images/Warn_RedTriangl_Blue.gif";
        } else if(pc.progName.equalsIgnoreCase("database")) {
            iconName = "images/Warn_RedTriangl_Yellow.gif";
        } else {iconName = "images/Warn_RedTriangl.gif";}
        java.net.URL imgURL = this.getClass().getResource(iconName);
        if (imgURL != null) {
            this.setIconImage(new javax.swing.ImageIcon(imgURL).getImage());
        }
        else {System.out.println("Could not load image: \""+iconName+"\"");}

        // --- set text and background colours
        if(pc.progName.equalsIgnoreCase("spana")) {
            aTextArea.setBackground(new java.awt.Color(220,220,255));
            aTextArea.setForeground(new java.awt.Color(0,0,102));
        } else if(pc.progName.equalsIgnoreCase("database")) {
            aTextArea.setBackground(new java.awt.Color(255,255,204));
            aTextArea.setForeground(java.awt.Color.black);
        } else {
            aTextArea.setBackground(java.awt.Color.black);
            aTextArea.setForeground(new java.awt.Color(102,255,0));
        }
        aTextArea.selectAll(); aTextArea.replaceRange("", 0, aTextArea.getSelectionEnd());
        jLabel1.setVisible(parentFrame != null);

        javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
            f = RedirectedFrame.this;
            loading = false;
        }}); //invokeLater(Runnable)

        System.setOut(outPrintStream); // catches System.out messages
        System.setErr(errPrintStream); // catches error messages

    } // constructor
  // </editor-fold>

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jButtonClose = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        aTextArea = new javax.swing.JTextArea();

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
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                formComponentMoved(evt);
            }
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        jLabel1.setText("(press [ESC] to focus the main window)");

        jButtonClose.setMnemonic('S');
        jButtonClose.setText("do not Show");
        jButtonClose.setToolTipText("hide frame (alt-S)");
        jButtonClose.setIconTextGap(2);
        jButtonClose.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCloseActionPerformed(evt);
            }
        });

        jScrollPane1.setFont(new java.awt.Font("Monospaced", 0, 13)); // NOI18N

        aTextArea.setBackground(new java.awt.Color(227, 227, 254));
        aTextArea.setColumns(20);
        aTextArea.setForeground(new java.awt.Color(0, 0, 102));
        aTextArea.setRows(5);
        aTextArea.setText("This is a test");
        aTextArea.setToolTipText(""); // NOI18N
        aTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                aTextAreaKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                aTextAreaKeyTyped(evt);
            }
        });
        jScrollPane1.setViewportView(aTextArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonClose))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonClose)
                    .addComponent(jLabel1))
                .addGap(0, 0, 0)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

  //<editor-fold defaultstate="collapsed" desc="Events">
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        Object[] opt0 = {"Hide window", "Terminate!", "Cancel"};
        int n = javax.swing.JOptionPane.showOptionDialog(this,
                "Hide this window?"+nl+nl+
                "\"Terminate\" will stop the program without"+nl+
                "closing connections or closing files, etc."+nl+
                "Terminate only if the program is not responding!"+nl,
                pc.progName, javax.swing.JOptionPane.YES_NO_CANCEL_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE, null, opt0, null);
        if(n == javax.swing.JOptionPane.YES_OPTION) { //the first button is "hide"
            RedirectedFrame.this.setVisible(false);
        }
        else if(n == javax.swing.JOptionPane.NO_OPTION) { //second button is "terminate"
            Object[] opt1 = {"Cancel", "Yes"};
            //note that the buttons are reversed: "cancel" is the first button
            //  and "yes" is the second
            n = javax.swing.JOptionPane.showOptionDialog(this,
                "Are you really sure?",
                pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.ERROR_MESSAGE, null, opt1, null);
            if(n == javax.swing.JOptionPane.NO_OPTION) { //the second button is "yes"
                System.out.println("Program Terminated by the User.");
                System.exit(1);
            }
        }
    }//GEN-LAST:event_formWindowClosing

    private void aTextAreaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_aTextAreaKeyTyped
        evt.consume();
    }//GEN-LAST:event_aTextAreaKeyTyped

    private void aTextAreaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_aTextAreaKeyPressed
        if(!Util.isKeyPressedOK(evt)) {evt.consume();}
    }//GEN-LAST:event_aTextAreaKeyPressed

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        if(loading || this.getExtendedState()==javax.swing.JFrame.ICONIFIED) {return;}
        if(this.getExtendedState()!=javax.swing.JFrame.MAXIMIZED_BOTH) {
            if(this.getHeight()<100){this.setSize(this.getWidth(), 100);}
            if(this.getWidth()<280){this.setSize(280,this.getHeight());}
        }
    }//GEN-LAST:event_formComponentResized

    private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved
        if(loading || this.getExtendedState()==javax.swing.JFrame.ICONIFIED) {return;}
        if(this.getExtendedState()!=javax.swing.JFrame.MAXIMIZED_BOTH) {
            if(this.getX()<0) {this.setLocation(2,this.getY());}
            if(this.getX() > screenSize.width-100){this.setLocation(screenSize.width-100,this.getY());}
            if(this.getY()<0) {this.setLocation(this.getX(),2);}
            if(this.getY() > screenSize.height-30){this.setLocation(this.getX(),screenSize.height-30);}
        }
    }//GEN-LAST:event_formComponentMoved

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        aTextArea.requestFocusInWindow();
    }//GEN-LAST:event_formWindowGainedFocus

    private void jButtonCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCloseActionPerformed
        if(parentFrame != null && parentFrame.isVisible()) {
            RedirectedFrame.this.setVisible(false);
            if((parentFrame.getExtendedState() & javax.swing.JFrame.ICONIFIED)
                            == javax.swing.JFrame.ICONIFIED) {
                parentFrame.setExtendedState(javax.swing.JFrame.NORMAL);
            }
            parentFrame.toFront();
            parentFrame.requestFocus();
        }
    }//GEN-LAST:event_jButtonCloseActionPerformed
  // </editor-fold>

 /** set the parent frame so that Alt-S and Escape will switch to it
  * @param parent the parent frame  */
  public void setParentFrame(javax.swing.JFrame parent) {
    this.parentFrame = parent;
    jLabel1.setVisible(true);
  }
  public void setCursorWait() {
      this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
      aTextArea.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
  }
  public void setCursorDef() {
      this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
      aTextArea.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
  }
  /** Enquire the behaviour of the redirected frame when a message is printed
   * on "<code>System.err</code>". Returns <code>true</code> if the redirected
   * frame is made visible when an error message is printed, and <code>false</code> otherwise.
   * @return <code>true</code> if the frame "pops up" when a \"System.err\" message
   * is printed; <code>false</code> otherwise
   * @see lib.huvud.RedirectedFrame#setPopupOnErr(boolean) setPopupOnErr */
  public boolean isPopupOnErr() {return popupOnErr;}

  /** Sets the behaviour of the redirected frame when a message is printed
   * on "<code>System.err</code>".
   * @param popOnErr <code>serVisible(popOnErr)</code> will be effectuated
   * when a message is printed on "<code>System.err</code>", that is,<ul>
   * <li>if <code>true</code> the frame will beccome visible if the frame
   * was not visible; if the frame was visible it will remain so.</li>
   * <li>if <code>false</code> the redirected frame will <i>not</i> become
   * visible; if the frame was visible before the error message is printed,
   * it will remain visible</li></ul>
   * @see lib.huvud.RedirectedFrame#isPopupOnErr() isPopupOnErr */
  public void setPopupOnErr(boolean popOnErr) {popupOnErr = popOnErr;}

  //<editor-fold defaultstate="collapsed" desc="errFilteredStream">
    class errFilteredStream extends java.io.FilterOutputStream {
        public errFilteredStream(java.io.OutputStream aStream) {
            super(aStream);
          } // constructor
        @Override
        public synchronized void write(byte b[]) throws java.io.IOException {
            String aString = new String(b);
            if(popupOnErr) {f.setVisible(true);}
            aTextArea.append(aString);
            aTextArea.setSelectionStart(Integer.MAX_VALUE);
        }
        @Override
        public synchronized void write(byte b[], int off, int len) throws java.io.IOException {
            String aString = new String(b , off , len);
            if(popupOnErr) {f.setVisible(true);}
            aTextArea.append(aString);
            aTextArea.setSelectionStart(Integer.MAX_VALUE);
        } // write
    } // class errFilteredStream
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="outFilteredStream">
    class outFilteredStream extends java.io.FilterOutputStream {
        public outFilteredStream(java.io.OutputStream aStream) {
            super(aStream);
          } // constructor
        @Override
        public synchronized void write(byte b[]) throws java.io.IOException {
            String aString = new String(b);
            aTextArea.append(aString);
            aTextArea.setSelectionStart(Integer.MAX_VALUE);
        }
        @Override
        public synchronized void write(byte b[], int off, int len) throws java.io.IOException {
            String aString = new String(b , off , len);
            aTextArea.append(aString);
            aTextArea.setSelectionStart(Integer.MAX_VALUE);
        } // write
    } // class outFilteredStream
  // </editor-fold>

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea aTextArea;
    private javax.swing.JButton jButtonClose;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

}