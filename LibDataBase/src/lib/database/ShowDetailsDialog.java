package lib.database;

import lib.common.Util;

/** Show the data in the databases for a single reaction.
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
public class ShowDetailsDialog extends javax.swing.JDialog {
  private java.awt.Dimension windowSize = new java.awt.Dimension(280,170);
  private boolean loading = true;
  private final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
  private final javax.swing.border.Border defBorder;
  private final javax.swing.border.Border highlightedBorder = javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED, java.awt.Color.gray, java.awt.Color.black);


  /** Creates new form NewJDialog
   * @param parent
   * @param modal
   * @param species
   * @param tC temperature in degrees Celsius
   * @param pBar pressure in bar
   * @param references
   */
  public ShowDetailsDialog(java.awt.Frame parent, boolean modal,
          Complex species, double tC, double pBar,
          References references) {
      super(parent, modal);
      initComponents();
      if(references == null) {
          jLabelRefText.setText("(file \"References.txt\" not found)   ");
          jLabelRefText.setEnabled(false);
          jScrollPaneRefs.setVisible(false);
          pack();
      }
    boolean dbg = true;
    if(species == null) {
      if(dbg) {System.out.println("Warning in \"DataDisplayDialog\": null species.");}
    }
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
    getRootPane().getActionMap().put("ALT_X", escAction);

    //---- Title, etc
    if(species != null && species.name.trim().length() > 0) {
        this.setTitle("  Data for \""+species.name.trim()+"\"");
    } else {this.setTitle("  Display Data");}
    defBorder = jScrollPaneRefs.getBorder();

    //---- Centre window on parent/screen
    int left,top;
    if(parent != null) {
        left = Math.max(0,(parent.getX() + (parent.getWidth()/2) - this.getWidth()/2));
        top = Math.max(0,(parent.getY()+(parent.getHeight()/2)-this.getHeight()/2));
    } else {
        left = Math.max(0,(screenSize.width-this.getWidth())/2);
        top = Math.max(0,(screenSize.height-this.getHeight())/2);
    }
    this.setLocation(Math.min(screenSize.width-this.getWidth()-20,left),
                     Math.min(screenSize.height-this.getHeight()-20, top));
    //----
    jTextAreaRefs.setLineWrap(true);
    jTextAreaRefs.setWrapStyleWord(true);
    jTextAreaRefs.setColumns(40);
    jTextAreaRefs.setText("");
    //----

    if(species == null) {
        jLabelReaction.setText("\"null\"");
        jLabellogK.setText(" (empty)");
        jLabelDeltaH.setText(" (empty)");
        jLabelDeltaCp.setText(" (empty)");
    } else {
        double w;
        jLabelReaction.setText(species.reactionText());
        if(Double.isNaN(species.constant) || species.constant == Complex.EMPTY) {
            jLabellogK.setText(" (empty)");
        } else {jLabellogK.setText(Util.formatDbl3(species.constant));}
        if(!Double.isNaN(tC) && !Double.isNaN(pBar)
                && (tC<24.9 || tC > 25.1 || pBar > 1.1)) {
            w = species.logKatTandP(tC, pBar);
            if(!Double.isNaN(w) && w != Complex.EMPTY) {
                if(pBar < lib.kemi.H2O.IAPWSF95.CRITICAL_pBar) {
                    jLabellogKTP.setText("<html>log <i>K</i>°("+Util.formatNumAsInt(tC).trim()+","+
                        String.format(java.util.Locale.ENGLISH,"%.2f bar",pBar)+") = "+Util.formatDbl3(w)+"</html>");
                } else {
                    jLabellogKTP.setText("<html>log <i>K</i>°("+Util.formatNumAsInt(tC).trim()+","+
                        String.format("%.0f bar",pBar)+") = "+Util.formatDbl3(w)+"</html>");
                }
            } else {jLabellogKTP.setText("<html>log <i>K</i>°("+Util.formatNumAsInt(tC).trim()+","+
                        Util.formatNumAsInt((float)pBar).trim()+") = ??</html>");}
        } else {jLabellogKTP.setVisible(false);}
        if(species.analytic) {
            // analytic equation
            jLabel4.setVisible(false);
            jLabelDeltaCp.setVisible(false);
            jLabelDeltaH.setVisible(false);
            jLabel3.setText("<html>A 6-parameter function is used<br>to evaluate logK(t,p).</html>");
        } else if(species.lookUp) {
            // look-up table of logK values
            jLabel4.setVisible(false);
            jLabelDeltaCp.setVisible(false);
            jLabelDeltaH.setVisible(false);
            jLabel3.setText("<html>A look-up table is used<br>to interpolate logK(t,p).</html>");
        } else {
            // delta-H and delta-Cp
            w = species.getDeltaH();
            if(w == Complex.EMPTY) {
                jLabelDeltaH.setText(" (empty)");
            } else {jLabelDeltaH.setText(Util.formatDbl3(w));}
            w = species.getDeltaCp();
            if(w == Complex.EMPTY) {
                jLabelDeltaCp.setText(" (empty)");
            } else {jLabelDeltaCp.setText(Util.formatDbl3(w));}
        }
        w = species.tMax;
        if(Double.isNaN(w) || w == Complex.EMPTY || w <25) {w = 25.;}
        jLabelTmax.setText("<html>Max temperature for extrapolation of logK = "+
                    String.format("%.0f",w)+"&deg;C</html>");
    }

    if(species != null && species.reference != null && species.reference.trim().length() > 0) {
        String refs = species.reference.trim();
        jLabelRefCit.setText(refs);
        if(references != null) {
            if(refs.trim().length() >0) {
                java.util.ArrayList<String> refKeys = references.splitRefs(refs);
                jTextAreaRefs.append(references.refsAsString(refKeys));
            }
        }
    } else {jLabelRefCit.setText(" (none)");}
    if(species != null && species.comment != null && species.comment.trim().length() > 0) {
        jLabelComments.setText(species.comment);
    } else {jLabelComments.setText(" (none)");}

    jTextAreaRefs.setCaretPosition(0);

    windowSize = this.getSize();
    loading = false;
    pack();
    jButtonOK.requestFocusInWindow();

  } // constructor

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jButtonOK = new javax.swing.JButton();
        jLabel0 = new javax.swing.JLabel();
        jLabelReaction = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabellogK25 = new javax.swing.JLabel();
        jLabellogK = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabelDeltaH = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabelDeltaCp = new javax.swing.JLabel();
        jLabelTmax = new javax.swing.JLabel();
        jLabellogKTP = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabelComments = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabelRefCit = new javax.swing.JLabel();
        jLabelRefText = new javax.swing.JLabel();
        jScrollPaneRefs = new javax.swing.JScrollPane();
        jTextAreaRefs = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
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

        jButtonOK.setMnemonic('c');
        jButtonOK.setText("Close");
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });

        jLabel0.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel0.setText("Reaction:");

        jLabelReaction.setText("A + B = C");

        jLabellogK25.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabellogK25.setLabelFor(jLabellogK);
        jLabellogK25.setText("<html>log <i>K</i>&deg; (25&deg;C) =</html>");

        jLabellogK.setText("-10.000");

        jLabel3.setLabelFor(jLabelDeltaH);
        jLabel3.setText("<html>&#916;<i>H</i>&deg; =</html>");

        jLabelDeltaH.setText("-10.000");

        jLabel4.setLabelFor(jLabelDeltaCp);
        jLabel4.setText("<html>&#916;<i>C<sub>p</sub></i>&deg;=</html>");

        jLabelDeltaCp.setText("-10.000");

        jLabelTmax.setText("<html>Max temperature for extrapolation of logK = 25&deg;C</html>");

        jLabellogKTP.setText("logK(32,8.64) = -10.000");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelDeltaCp))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabellogK25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabellogK)
                        .addGap(49, 49, 49)
                        .addComponent(jLabellogKTP))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelDeltaH)))
                .addContainerGap(54, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabelTmax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabellogK25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabellogK)
                    .addComponent(jLabellogKTP))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelDeltaH))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelDeltaCp))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelTmax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel6.setText("Comments:");

        jLabelComments.setText(" (none)");

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel5.setText("Reference citations:");

        jLabelRefCit.setText("##");

        jLabelRefText.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelRefText.setText("References:");

        jTextAreaRefs.setEditable(false);
        jTextAreaRefs.setColumns(40);
        jTextAreaRefs.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextAreaRefsFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextAreaRefsFocusLost(evt);
            }
        });
        jTextAreaRefs.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextAreaRefsKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextAreaRefsKeyTyped(evt);
            }
        });
        jScrollPaneRefs.setViewportView(jTextAreaRefs);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabelRefText)
                        .addGap(242, 242, 242)
                        .addComponent(jButtonOK)
                        .addGap(24, 24, 24))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPaneRefs)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel0)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addComponent(jLabelReaction))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel6)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabelComments))
                                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabelRefCit)))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel0)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelReaction)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabelComments))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabelRefCit))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonOK, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabelRefText))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneRefs, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

  //<editor-fold defaultstate="collapsed" desc="events">

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
      closeWindow();
    }//GEN-LAST:event_formWindowClosing

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
      if(loading || windowSize == null) {return;}
      int x = this.getX(); int y = this.getY();
      int w = this.getWidth(); int h = this.getHeight();
      int nw = Math.max(w, windowSize.width);
      int nh = Math.max(h, windowSize.height);
      int nx=x, ny=y;
      if(x+nw > screenSize.width) {nx = screenSize.width - nw;}
      if(y+nh > screenSize.height) {ny = screenSize.height -nh;}
      if(x!=nx || y!=ny) {this.setLocation(nx, ny);}
      if(w!=nw || h!=nh) {this.setSize(nw, nh);}
    }//GEN-LAST:event_formComponentResized

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
      closeWindow();
    }//GEN-LAST:event_jButtonOKActionPerformed

    private void jTextAreaRefsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextAreaRefsKeyPressed
      int ctrl = java.awt.event.InputEvent.CTRL_DOWN_MASK;
      if(((evt.getModifiersEx() & ctrl) == ctrl)
              && (evt.getKeyCode() == java.awt.event.KeyEvent.VK_V
                || evt.getKeyCode() == java.awt.event.KeyEvent.VK_X)) {evt.consume(); return;}
      if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {evt.consume(); closeWindow(); return;}
      if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_TAB) {evt.consume(); return;}
      if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_DELETE) {evt.consume(); return;}
      if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_BACK_SPACE) {evt.consume();}
    }//GEN-LAST:event_jTextAreaRefsKeyPressed

    private void jTextAreaRefsKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextAreaRefsKeyTyped
      evt.consume();
    }//GEN-LAST:event_jTextAreaRefsKeyTyped

    private void jTextAreaRefsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextAreaRefsFocusGained
        if(jTextAreaRefs.isFocusOwner()) {jScrollPaneRefs.setBorder(highlightedBorder);}
    }//GEN-LAST:event_jTextAreaRefsFocusGained

    private void jTextAreaRefsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextAreaRefsFocusLost
        if(!jTextAreaRefs.isFocusOwner()) {jScrollPaneRefs.setBorder(defBorder);}
    }//GEN-LAST:event_jTextAreaRefsFocusLost

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="closeWindow()">
  private void closeWindow() {
    this.dispose();
  } // closeWindow()
  // </editor-fold>


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JLabel jLabel0;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabelComments;
    private javax.swing.JLabel jLabelDeltaCp;
    private javax.swing.JLabel jLabelDeltaH;
    private javax.swing.JLabel jLabelReaction;
    private javax.swing.JLabel jLabelRefCit;
    private javax.swing.JLabel jLabelRefText;
    private javax.swing.JLabel jLabelTmax;
    private javax.swing.JLabel jLabellogK;
    private javax.swing.JLabel jLabellogK25;
    private javax.swing.JLabel jLabellogKTP;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPaneRefs;
    private javax.swing.JTextArea jTextAreaRefs;
    // End of variables declaration//GEN-END:variables
}
