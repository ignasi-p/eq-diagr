package spana;

/** A dialog to change the size of the parent "Disp" window.
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
public class DiagrWSize extends javax.swing.JDialog {
  private boolean finished = false;
  private DiagrWSize frame = null;
  private spana.Disp d = null;
  private java.awt.Dimension windowSize;
  private final int h0; //original height
  private final int w0; //original width
  /** New-line character(s) to substitute "\n" */
  private static final String nl = System.getProperty("line.separator");

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  public DiagrWSize(java.awt.Frame parent, boolean modal, spana.Disp diagr0) {
    super(parent, modal);
    initComponents();
    d = diagr0;
    finished = false;
    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    //--- Close window on ESC key
    javax.swing.KeyStroke escKeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(escKeyStroke,"ESCAPE");
    javax.swing.Action escAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            closeWindow();
        }};
    getRootPane().getActionMap().put("ESCAPE", escAction);
    //--- Alt-Q quit
    javax.swing.KeyStroke altQKeyStroke = javax.swing.KeyStroke.getKeyStroke(
    				java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altQKeyStroke,"ALT_Q");
    getRootPane().getActionMap().put("ALT_Q", escAction);
    //--- Alt-X eXit
    javax.swing.KeyStroke altXKeyStroke = javax.swing.KeyStroke.getKeyStroke(
    				java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altXKeyStroke,"ALT_X");
    javax.swing.Action altXAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jButtonOK.doClick();
        }};
    getRootPane().getActionMap().put("ALT_X", altXAction);

    //--- Title, etc
    //getContentPane().setBackground(new java.awt.Color(255, 255, 153));
    this.setTitle(" Diagram size");

    //---- data
    jLabelName.setText("Diagram: \""+d.diagrName+"\"");
    h0 = Math.round((float)d.diagrSize.getHeight());
    jTextFieldH.setText(String.valueOf(h0).trim());
    w0 = Math.round((float)d.diagrSize.getWidth());
    jTextFieldW.setText(String.valueOf(w0).trim());

    javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
        frame = DiagrWSize.this;
        windowSize = frame.getSize();
        // Center window on the parent
        int left = Math.max(0,(d.getX() + (d.getWidth()/2) - frame.getWidth()/2));
        int top = Math.max(0,(d.getY()+(d.getHeight()/2)-frame.getHeight()/2));
        frame.setLocation(left, top);
    }   }); //invokeLater(Runnable)
 
  } //constructor
  //</editor-fold>

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButtonOK = new javax.swing.JButton();
        jLabelName = new javax.swing.JLabel();
        jLabelW = new javax.swing.JLabel();
        jTextFieldW = new javax.swing.JTextField();
        jLabelH = new javax.swing.JLabel();
        jTextFieldH = new javax.swing.JTextField();
        jButtonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
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

        jButtonOK.setMnemonic('O');
        jButtonOK.setText("<html><u>O</u>K</html>");
        jButtonOK.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButtonOK.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonOK.setMinimumSize(new java.awt.Dimension(47, 23));
        jButtonOK.setPreferredSize(new java.awt.Dimension(47, 23));
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });

        jLabelName.setText("Diagram: \"hello\"");

        jLabelW.setLabelFor(jTextFieldW);
        jLabelW.setText("width:");

        jTextFieldW.setText("1000");
        jTextFieldW.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldWFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldWFocusLost(evt);
            }
        });
        jTextFieldW.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldWKeyTyped(evt);
            }
        });

        jLabelH.setLabelFor(jTextFieldH);
        jLabelH.setText("height:");

        jTextFieldH.setText("1000");
        jTextFieldH.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldHFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldHFocusLost(evt);
            }
        });
        jTextFieldH.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldHKeyTyped(evt);
            }
        });

        jButtonCancel.setMnemonic('C');
        jButtonCancel.setText("<html><u>C</u>ancel</html>");
        jButtonCancel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButtonCancel.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelName)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabelH)
                            .addComponent(jLabelW))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldH, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldW, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonOK, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(48, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabelName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelW))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelH))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonOK, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //<editor-fold defaultstate="collapsed" desc="Events">
    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
        int h;
        try {h = Integer.parseInt(jTextFieldH.getText());}
        catch (NumberFormatException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error: "+ex.toString(),
                    "Diagram Size - Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            h = h0;}
        int w;
        try {w = Integer.parseInt(jTextFieldW.getText());}
        catch (NumberFormatException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error: "+ex.toString(),
                    "Diagram Size - Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            w = w0;}
        d.diagrSize.setSize(w, h);
        closeWindow();
    }//GEN-LAST:event_jButtonOKActionPerformed

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

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        closeWindow();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jTextFieldWKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldWKeyTyped
        if(!Character.isDigit(evt.getKeyChar())) {evt.consume();}
    }//GEN-LAST:event_jTextFieldWKeyTyped

    private void jTextFieldHKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldHKeyTyped
        if(!Character.isDigit(evt.getKeyChar())) {evt.consume();}
    }//GEN-LAST:event_jTextFieldHKeyTyped

    private void jTextFieldWFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldWFocusLost
        int w;
        try {
            w = Integer.parseInt(jTextFieldW.getText());
            jTextFieldW.setText(String.valueOf(w).trim());
        } //try
        catch (NumberFormatException nfe) {
        javax.swing.JOptionPane.showMessageDialog
                (this,"Wrong numeric format"+nl+nl+
                "Please enter an integer.",
                "Numeric Format Error", javax.swing.JOptionPane.WARNING_MESSAGE);
        w = w0;
        jTextFieldW.setText(String.valueOf(w));
        jTextFieldW.requestFocus();
        } //catch
    }//GEN-LAST:event_jTextFieldWFocusLost

    private void jTextFieldHFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldHFocusLost
        int h;
        try {
            h = Integer.parseInt(jTextFieldH.getText());
            jTextFieldH.setText(String.valueOf(h).trim());
        } //try
        catch (NumberFormatException nfe) {
        javax.swing.JOptionPane.showMessageDialog
                (this,"Wrong numeric format"+nl+nl+
                "Please enter an integer.",
                "Numeric Format Error", javax.swing.JOptionPane.WARNING_MESSAGE);
        h = h0;
        jTextFieldH.setText(String.valueOf(h));
        jTextFieldH.requestFocus();
        } //catch
    }//GEN-LAST:event_jTextFieldHFocusLost

    private void jTextFieldWFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldWFocusGained
        jTextFieldW.selectAll();
    }//GEN-LAST:event_jTextFieldWFocusGained

    private void jTextFieldHFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldHFocusGained
        jTextFieldH.selectAll();
    }//GEN-LAST:event_jTextFieldHFocusGained
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
    //</editor-fold>

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JLabel jLabelH;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JLabel jLabelW;
    private javax.swing.JTextField jTextFieldH;
    private javax.swing.JTextField jTextFieldW;
    // End of variables declaration//GEN-END:variables
}
