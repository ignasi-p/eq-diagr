package lib.database;

import lib.common.MsgExceptn;
import lib.common.Util;
import lib.huvud.Div;
import lib.huvud.ProgramConf;

/** Add new data.
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
public class FrameAddData extends javax.swing.JFrame {
  // Note: for java 1.6 jComBox must be without type
  //       for java 1.7 jComBox must be <String>
  private ProgramConf pc;
  private ProgramDataDB pd;
  private final javax.swing.JFrame parent;
  private boolean finished = false;
  private boolean dbg = false;
  /** the name, with complete path, of the file with new data to add */
  private String addFile;
  /** the name, with complete path, of a temprary file to store the database when making modifications */
  private String addFileTmp;
  /** the name, with complete path, of the chemical element - reactant file corresponding to the database "addFile" */
  private String addFileEle;

  /** Array list of all known components (e.g. "CN-" or "Fe+2").
   * This is used to fill the drop-down combo boxes, and to check for
   * unknown reactants in reactions.
   * <p>It contains all the components in the databases selected in the program,
   * plus those in the add-data file.
   * @see FrameAddData#elemCompAdd elemCompAdd */
  private java.util.ArrayList<String> componentsAdd = new java.util.ArrayList<String>();
  /** Array list of String[3] objects with the components used in file "addFile"<br>
   *  [0] contains the element name (e.g. "C"),<br>
   *  [1] the component formula ("CN-" or "Fe+2"),<br>
   *  [2] the component name ("cyanide" or null), which is not really needed,
   * but used to help the user.
   * <br>
   * This array list must contain all components needed for the "add" file.
   * That is, all components initially in file "addFileEle" (if any),
   * <b>and</b> any component in the reactions in file "addFile"
   * that were missing in "addFileEle" (if any), but which are found in the
   * list of components from the main database(s), that is, found in "elemComp".
   * @see ProgramDataDB#elemComp elemComp
   * @see FrameAddData#componentsAdd componentsAdd
   * @see AddDataElem#elemCompAdd_Update(boolean, java.awt.Component, java.lang.String, java.lang.String, java.util.ArrayList, java.util.ArrayList) elemCompAdd_Update() */
  public java.util.ArrayList<String[]> elemCompAdd = new java.util.ArrayList<String[]>();

  private java.awt.Dimension windowSize = new java.awt.Dimension(460,383);
  /** a model used for jListReact. It contains Object[2] arrays with the first object
   * being a text and the second being a Color indicating if the reaction
   * is charge balanced or not */
   private javax.swing.DefaultListModel<ModelComplexesItem> modelComplexes = new javax.swing.DefaultListModel<>();
   // private javax.swing.DefaultListModel modelComplexes = new javax.swing.DefaultListModel(); // java 1.6
 /** The objects in <code>modelComplexes</code>
  * with two variables (fields): a text string and a colour (the foreground).  */
  private static class ModelComplexesItem {
      String txt;
      java.awt.Color clr;
     /** create an object for <code>modelComplexes</code>.
      * It has two variables (fields): a text string and a colour (the foreground) */
      public ModelComplexesItem(String t, java.awt.Color c) {txt =t; clr =c;}
      @Override public String toString() {return txt;}
     /** @return the foreground colour to be used for the text string <code>txt</code>
      * of this object */
      public java.awt.Color getForeground() {return clr;}
  }
  /** a model used for jListComps */
  private javax.swing.DefaultListModel<String> modelComponents = new javax.swing.DefaultListModel<>();
  // private javax.swing.DefaultListModel modelComponents = new javax.swing.DefaultListModel(); // java 1.6

  private java.awt.CardLayout cl;
  // there are six JComboBox and six JFieldText
  private java.util.ArrayList <javax.swing.JComboBox<String>> boxes = new java.util.ArrayList<javax.swing.JComboBox<String>>(6);
  // private java.util.ArrayList <javax.swing.JComboBox> boxes = new java.util.ArrayList<javax.swing.JComboBox>(6); // java 1.6
  
  private ComboBoxActionListener boxAListener = new ComboBoxActionListener();
  private ComboBoxFocusListener boxFListener = new ComboBoxFocusListener();
  private ComboBoxKeyListener boxKListener = new ComboBoxKeyListener();
  private javax.swing.JTextField[] texts = new javax.swing.JTextField[6];
  private TextFieldFocusListener textFListener = new TextFieldFocusListener();
  private TextFieldActionListener textAListener = new TextFieldActionListener();
  private TextFieldKeyListener textKListener = new TextFieldKeyListener();

  private Complex newC;
  private Complex oldNewC;
  private String oldComponentName;
  private String oldComponentDescr;
  private String oldComponentLinked;

  private boolean editingReaction = false;
  private boolean rearranging = false;
  private boolean loading = true;
  private boolean keyUpDown = false;
  /** the selected index in jListReact when the focus was lost */
  private int old_i_react = -1;
  /** the selected index in jListComps when the focus was lost */
  private int old_i_comps = -1;
  /** indicates if a mouse click on the reactions list should show the popup menu */
  private boolean isPopup = false;

  private static final java.awt.Color vermilion = new java.awt.Color(213,94,0); 
  private static final String SLASH = java.io.File.separator;
  /** New-line character(s) to substitute "\n" */
  private static final String nl = System.getProperty("line.separator");

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /** Creates new form FrameAddData
   * @param pc0
   * @param pd0
   * @param parent */
  public FrameAddData(ProgramConf pc0, ProgramDataDB pd0, javax.swing.JFrame parent) {
    //there are six JComboBox and six JTextField
    initComponents();
    loading = true;
    pc = pc0;
    pd = pd0;
    this.parent = parent;
    dbg = pc.dbg;
    System.out.println(nl+"---- FrameAddData");
    boxes.trimToSize();
    // ----
    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    //--- Alt-S show/hide messages window
    javax.swing.KeyStroke altSKeyStroke = javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altSKeyStroke,"ALT_S");
    javax.swing.Action altSAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jCheckBoxMenuMsg.doClick();
        }};
    getRootPane().getActionMap().put("ALT_S", altSAction);
    //--- Alt-Q quit
    javax.swing.KeyStroke altQKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.ALT_MASK, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(altQKeyStroke,"ALT_Q");
    javax.swing.Action altQAction = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            closeWindow();
        }};
    getRootPane().getActionMap().put("ALT_Q", altQAction);
    //--- F1 for help
    javax.swing.KeyStroke f1KeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(f1KeyStroke,"F1");
    javax.swing.Action f1Action = new javax.swing.AbstractAction() {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) {
            jMenuHelpHlp.doClick();
        }};
    getRootPane().getActionMap().put("F1", f1Action);

    //---- Icon
    String iconName = "images/Save_32x32.gif";
    java.net.URL imgURL = this.getClass().getResource(iconName);
    if (imgURL != null) {this.setIconImage(new javax.swing.ImageIcon(imgURL).getImage());}
    else {System.out.println("Error: Could not load image = \""+iconName+"\"");}
    //---- Title, etc
    this.setTitle(pc.progName+" - Add data");
    jMenuBar.add(javax.swing.Box.createHorizontalGlue(),2); //move "Help" menu to the right
    //---- waiting...
    setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
    frameEnable(false);

    //--- Set the location of the window in the screen
    int left = Math.max(0,(LibDB.screenSize.width-this.getWidth())/2);
    int top = Math.max(0,(LibDB.screenSize.height-this.getHeight())/2);
    if(pd.addDataLocation.x < 0 || pd.addDataLocation.y < 0) {
        pd.addDataLocation.x = left;
        pd.addDataLocation.y = top;
    }
    pd.addDataLocation.x = Math.max(60,Math.min(pd.addDataLocation.x,(LibDB.screenSize.width-this.getWidth())));
    pd.addDataLocation.y = Math.max(10,Math.min(pd.addDataLocation.y,(LibDB.screenSize.height-this.getHeight())));
    this.setLocation(pd.addDataLocation);
    //---- Fill in combo boxes
    boxes.add(jComboBox0); boxes.add(jComboBox1);
    boxes.add(jComboBox2); boxes.add(jComboBox3);
    boxes.add(jComboBox4); boxes.add(jComboBox5);
    texts[0] = jTextField0; texts[1] = jTextField1;
    texts[2] = jTextField2; texts[3] = jTextField3;
    texts[4] = jTextField4; texts[5] = jTextField5;
    // add the mouse and action listeners
    for(int i=0; i < boxes.size(); i++) {
        javax.swing.JComboBox<String> jcb; // javax.swing.JComboBox jcb; // java 1.6
        jcb = boxes.get(i);
        jcb.setActionCommand(String.valueOf(i));
        jcb.addActionListener(boxAListener);
        jcb.addFocusListener(boxFListener);
        jcb.addKeyListener(boxKListener);
        boxes.set(i, jcb);
        texts[i].setName("jTextField"+String.valueOf(i).trim());
        texts[i].addFocusListener(textFListener);
        texts[i].addActionListener(textAListener);
        texts[i].addKeyListener(textKListener);
    }
    //
    java.awt.Font fN = jLabelReactionText.getFont();
    fN = new java.awt.Font(fN.getName(), java.awt.Font.PLAIN, fN.getSize());
    jLabelReactionText.setFont(fN);
    jLabelCharge.setText(" ");
    jLabelLinked.setText("");

    // fill the elements combo box
    jComboBoxElems.removeAllItems();
    java.util.AbstractList<String> items = new java.util.ArrayList<String>(LibDB.ELEMENTS);
    loop:
    for(int i =1; i < LibDB.ELEMENTS; i++) {
        for(int j=0; j<items.size(); j++) { //do not add duplicates
            if(LibDB.elementSymb[i].equals(items.get(j))) {continue loop;}
        }
        items.add(LibDB.elementSymb[i]);
    }
    jComboBoxElems.addItem("");
    java.util.Collections.sort(items,String.CASE_INSENSITIVE_ORDER);
    java.util.Iterator<String> iter = items.iterator();
    while(iter.hasNext()) {jComboBoxElems.addItem(iter.next());}
    jMenuFileSave.setEnabled(false);
    jButtonSaveReac.setEnabled(false);
    jButtonSaveComp.setEnabled(false);
    if(pd.references != null) {jMenuItemDetails.setEnabled(true);} else {jMenuItemDetails.setEnabled(false);}

  } //constructor
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="start">
  public void start() {
    this.setVisible(true);
    if(pd.msgFrame != null) {
        pd.msgFrame.setParentFrame(FrameAddData.this);
        jCheckBoxMenuMsg.setSelected(pd.msgFrame.isVisible());
    }
    // get the names of files "addFile" and "addFileEle"
    if(!getAddFileName()) {closeWindow(); return;}
    frameEnable(true);
    windowSize = FrameAddData.this.getSize();
    java.io.File aF = new java.io.File(addFile);
    if(aF.exists()) {
        jMenuAddShow.setEnabled(true);
        jMenuAddShow.doClick();
        addAddFileToList();
    } else {
        jMenuAddReact.doClick();
    }
    parent.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    loading = false;

  } //start
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="ComboBox & TextField Array Listeners">
  private void ComboBox_Click(int i) {
    if(loading || rearranging || editingReaction || keyUpDown) {return;}
    if(!jPanelReaction.isShowing()) {return;}
    if(i < 0 || i >= boxes.size()) {return;}
    javax.swing.JComboBox<String> jcb_i = boxes.get(i); // javax.swing.JComboBox jcb_i = boxes.get(i); // java 1.6
    if(jcb_i.getSelectedItem() == null) {return;}
    String ti = jcb_i.getSelectedItem().toString();
    if(ti.length()>0) {
        if(texts[i].getText().length() <=0) {texts[i].setText("1");}
        for(int j=0; j < boxes.size(); j++) {
            javax.swing.JComboBox<String> jcb_j = boxes.get(j); // javax.swing.JComboBox jcb_j = boxes.get(j); // java 1.6
            if(j != i && jcb_j.getSelectedItem().toString().length()>0) {
                if(jcb_j.getSelectedItem().toString().equals(ti)) {
                    jcb_i.setSelectedIndex(0);
                    texts[i].setText("");
                    showErr("You already have \""+ti+"\""+nl+"in the reaction.",2);
                    break;
                }
            }
        } //for j
    } else {texts[i].setText("");}
    rearrangeReaction();
    update_newC();
    //boxes[i].requestFocusInWindow();
  } //ComboBox_Click()
  private class ComboBoxActionListener implements java.awt.event.ActionListener {
    @Override public void actionPerformed(java.awt.event.ActionEvent e) {
        if(loading || rearranging || editingReaction || keyUpDown) {return;}
        int i;
        if(e.getSource() instanceof javax.swing.JComboBox) {
            i = Integer.parseInt(e.getActionCommand());
            ComboBox_Click(i);
        } //if jComboBox
    } //actionPerformed
  } //class ComboBoxActionListener

  private class ComboBoxFocusListener implements java.awt.event.FocusListener {
      @Override public void focusGained(java.awt.event.FocusEvent evt) {
        //int i = getI(evt);
        //if(i < 0) {return;}
        //buttons[i].setBorder(buttonBorderSelected);
        //buttons[i].setPreferredSize(buttonSize);
      }
      @Override public void focusLost(java.awt.event.FocusEvent evt) {
        if(loading) {return;}
        int i = getI(evt);
        if(i < 0) {return;}
        //buttons[i].setBorder(buttonBorder);
        ComboBox_Click(i);
        rearrangeReaction();
        update_newC();
      }
    private int getI(java.awt.event.FocusEvent evt) {
        javax.swing.JComboBox b = (javax.swing.JComboBox)evt.getSource();
        if(b == null) {return -1;}
        int i = Integer.parseInt(b.getActionCommand());
        if(i < 0 || i >= boxes.size()) {return -1;}
        return i;
    }//getI(evt)
  } //class ComboBoxFocusListener

  private class ComboBoxKeyListener extends java.awt.event.KeyAdapter {
    @Override public void keyPressed(java.awt.event.KeyEvent evt) {
        int i = getI(evt);
        if(i < 0) {return;}
        int k = evt.getKeyCode();
        if(k == java.awt.event.KeyEvent.VK_UP ||
           k == java.awt.event.KeyEvent.VK_DOWN) {
            keyUpDown = true;
        } else {
            keyUpDown = false;
            if(k == java.awt.event.KeyEvent.VK_LEFT ||
               k == java.awt.event.KeyEvent.VK_RIGHT) {
                if(k == java.awt.event.KeyEvent.VK_LEFT) {
                    texts[i].requestFocusInWindow();
                } else if(k == java.awt.event.KeyEvent.VK_RIGHT) {
                    if(i < (boxes.size()-1)) {texts[i+1].requestFocusInWindow();}
                    else {jTextFieldComplex.requestFocusInWindow();}
                }
            } else
            if(k == java.awt.event.KeyEvent.VK_ENTER ||
               k == java.awt.event.KeyEvent.VK_SPACE) {
                ComboBox_Click(i);
            }
        }
    } //keyTyped
    private int getI(java.awt.event.KeyEvent evt) {
        javax.swing.JComboBox b = (javax.swing.JComboBox)evt.getSource();
        if(b == null) {return -1;}
        int i = Integer.parseInt(b.getActionCommand());
        if(i < 0 || i >= boxes.size()) {return -1;}
        return i;
    }//getI(evt)
  } //class ComboBoxKeyListener

  private class TextFieldActionListener implements java.awt.event.ActionListener {
    @Override public void actionPerformed(java.awt.event.ActionEvent e) {
        if(loading || rearranging) {return;}
        javax.swing.JTextField tf = (javax.swing.JTextField)e.getSource();
        int i = -1;
        try{
            String t = tf.getName();
            if(t != null && t.length()>0) {
                i = Integer.parseInt(t.substring(t.length()-1, t.length()));
            }
        }
        catch (NumberFormatException ex) {i=-1;}
        if(i < 0 || i >= texts.length) {return;}
        validateReactionCoeff(i);
        rearrangeReaction();
        update_newC();
        texts[i].requestFocusInWindow();
    } //actionPerformed
  } //class TextFieldActionListener

  private class TextFieldFocusListener implements java.awt.event.FocusListener {
    @Override public void focusGained(java.awt.event.FocusEvent evt) {
        int i = getI(evt);
        if(i < 0 || i >= texts.length) {return;}
        texts[i].selectAll();
    } //focusGained
    @Override public void focusLost(java.awt.event.FocusEvent evt) {
        int i = getI(evt);
        if(i < 0 || i >= texts.length) {return;}
        validateReactionCoeff(i);
        update_newC();
    } //focusLost
    private int getI(java.awt.event.FocusEvent evt) {
        javax.swing.JTextField b = (javax.swing.JTextField)evt.getSource();
        if(b == null) {return -1;}
        int i = -1;
        try{
            String t = b.getName();
            if(t != null && t.length()>0) {//name is "jTextFieldN" with N=0 to 5
                i = Integer.parseInt(t.substring(t.length()-1, t.length()));
            }
        }
        catch (NumberFormatException ex) {i=-1;}
        return i;
    }//getI(evt)
  } //class TextFieldFocusListener

  private class TextFieldKeyListener extends java.awt.event.KeyAdapter {
    @Override public void keyTyped(java.awt.event.KeyEvent evt) {
        char key = evt.getKeyChar();
        if(!isCharOKforNumberInput(key)) {evt.consume();}
        //return;
    } //keyTyped
    @Override public void keyReleased(java.awt.event.KeyEvent evt) {
        //int i = getI(evt);
        //if(i < 0 || i >= texts.length) {return;}
        update_newC();
    } //keyReleased
    @Override public void keyPressed(java.awt.event.KeyEvent evt) {
        int i = getI(evt);
        if(i < 0 || i >= texts.length) {return;}
        if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_UP ||
           evt.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
            if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_UP) {
                if(i==0) {jTextFieldRef.requestFocusInWindow();}
                else {
                    javax.swing.JComboBox<String> jcb = boxes.get(i-1); // javax.swing.JComboBox jcb = boxes.get(i-1); // java 1.6
                    jcb.requestFocusInWindow();
                }
            }
            else if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
                javax.swing.JComboBox<String> jcb = boxes.get(i); // javax.swing.JComboBox jcb = boxes.get(i); // java 1.6
                jcb.requestFocusInWindow();
            }
            //return;
        }
    } //keyPressed
    private int getI(java.awt.event.KeyEvent evt) {
        javax.swing.JTextField tf = (javax.swing.JTextField)evt.getSource();
        if(tf == null) {return -1;}
        int i = -1;
        try{
            String t = tf.getName();
            if(t != null && t.length()>0) {
                i = Integer.parseInt(t.substring(t.length()-1, t.length()));
            }
        }
        catch (NumberFormatException ex) {i=-1;}
        return i;
    }//getI(evt)
  } //class TextFieldKeyListener

  //</editor-fold>

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new javax.swing.ButtonGroup();
        jPopupMenu = new javax.swing.JPopupMenu();
        jMenuItemEdit = new javax.swing.JMenuItem();
        jMenuItemDel = new javax.swing.JMenuItem();
        jMenuItemDetails = new javax.swing.JMenuItem();
        jSeparator = new javax.swing.JPopupMenu.Separator();
        jMenuItemCancel = new javax.swing.JMenuItem();
        jPanelReaction = new javax.swing.JPanel();
        jLabelReaction = new javax.swing.JLabel();
        jPanelReaction1 = new javax.swing.JPanel();
        jTextField0 = new javax.swing.JTextField();
        jComboBox0 = new javax.swing.JComboBox<String>();
        jLabelPlus1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox<String>();
        jTextField2 = new javax.swing.JTextField();
        jComboBox2 = new javax.swing.JComboBox<String>();
        jLabelPlus3 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jComboBox3 = new javax.swing.JComboBox<String>();
        jTextField4 = new javax.swing.JTextField();
        jComboBox4 = new javax.swing.JComboBox<String>();
        jLabelPlus5 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jComboBox5 = new javax.swing.JComboBox<String>();
        jLabelPlus6 = new javax.swing.JLabel();
        jLabelPlus7 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabelRLh = new javax.swing.JLabel();
        jTextFieldComplex = new javax.swing.JTextField();
        jLabelCharge = new javax.swing.JLabel();
        jPanelReaction2 = new javax.swing.JPanel();
        jLabelLogK = new javax.swing.JLabel();
        jTextFieldLogK = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldDeltH = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldDeltCp = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldComment = new javax.swing.JTextField();
        jLabelRef = new javax.swing.JLabel();
        jTextFieldRef = new javax.swing.JTextField();
        jButtonSaveReac = new javax.swing.JButton();
        jPanelComponent = new javax.swing.JPanel();
        jLabelComp = new javax.swing.JLabel();
        jLabelCompName = new javax.swing.JLabel();
        jTextFieldCompName = new javax.swing.JTextField();
        jLabelLinked = new javax.swing.JLabel();
        jLabelCompDescr = new javax.swing.JLabel();
        jTextFieldCompDescr = new javax.swing.JTextField();
        jLabelElems = new javax.swing.JLabel();
        jComboBoxElems = new javax.swing.JComboBox<String>();
        jButtonLink = new javax.swing.JButton();
        jButtonSaveComp = new javax.swing.JButton();
        jPanelFiles = new javax.swing.JPanel();
        jLabelFiles = new javax.swing.JLabel();
        jScrollPaneFiles = new javax.swing.JScrollPane();
        jTextAreaFiles = new javax.swing.JTextArea();
        jPanelReactions = new javax.swing.JPanel();
        jLabelReact = new javax.swing.JLabel();
        jScrollPaneReact = new javax.swing.JScrollPane();
        jListReact = new javax.swing.JList();
        jLabelReactionText = new javax.swing.JLabel();
        jLabelHelp = new javax.swing.JLabel();
        jPanelComps = new javax.swing.JPanel();
        jLabelComps = new javax.swing.JLabel();
        jScrollPaneComps = new javax.swing.JScrollPane();
        jListComps = new javax.swing.JList();
        jMenuBar = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuFileShow = new javax.swing.JMenuItem();
        jMenuFileSave = new javax.swing.JMenuItem();
        jMenuFileExit = new javax.swing.JMenuItem();
        jMenuAdd = new javax.swing.JMenu();
        jMenuAddReact = new javax.swing.JMenuItem();
        jMenuAddComp = new javax.swing.JMenuItem();
        jMenuAddShow = new javax.swing.JMenuItem();
        jSeparatorAdd = new javax.swing.JPopupMenu.Separator();
        jCheckBoxMenuMsg = new javax.swing.JCheckBoxMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        jMenuHelpHlp = new javax.swing.JMenuItem();

        jMenuItemEdit.setMnemonic('e');
        jMenuItemEdit.setText("Edit");
        jMenuItemEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemEditActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemEdit);

        jMenuItemDel.setMnemonic('d');
        jMenuItemDel.setText("Delete");
        jMenuItemDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDelActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemDel);

        jMenuItemDetails.setMnemonic('d');
        jMenuItemDetails.setText("show Details");
        jMenuItemDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDetailsActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemDetails);
        jPopupMenu.add(jSeparator);

        jMenuItemCancel.setMnemonic('c');
        jMenuItemCancel.setText("Cancel");
        jMenuItemCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCancelActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemCancel);

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
        getContentPane().setLayout(new java.awt.CardLayout());

        jLabelReaction.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelReaction.setText("New reaction:");

        jTextField0.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextField0.setText("1");

        jLabelPlus1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelPlus1.setText("+");

        jTextField1.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextField1.setText("1");

        jTextField2.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextField2.setText("1");

        jLabelPlus3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelPlus3.setText("+");

        jTextField3.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextField3.setText("1");

        jTextField4.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextField4.setText("1");

        jLabelPlus5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelPlus5.setText("+");

        jTextField5.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextField5.setText("1");

        jLabelPlus6.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelPlus6.setText("+");

        jLabelPlus7.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelPlus7.setText("+");

        jLabelRLh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lib/database/images/rlh.gif"))); // NOI18N
        jLabelRLh.setLabelFor(jTextFieldComplex);

        jTextFieldComplex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldComplexActionPerformed(evt);
            }
        });
        jTextFieldComplex.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldComplexFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldComplexFocusLost(evt);
            }
        });
        jTextFieldComplex.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextFieldComplexKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldComplexKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldComplexKeyTyped(evt);
            }
        });

        jLabelCharge.setText("<html>Reaction is chage balanced.</html>");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabelRLh)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldComplex, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabelCharge, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabelRLh)
                    .addComponent(jTextFieldComplex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelCharge, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanelReaction1Layout = new javax.swing.GroupLayout(jPanelReaction1);
        jPanelReaction1.setLayout(jPanelReaction1Layout);
        jPanelReaction1Layout.setHorizontalGroup(
            jPanelReaction1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelReaction1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelReaction1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextField0, javax.swing.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                    .addComponent(jTextField2)
                    .addComponent(jTextField4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelReaction1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelReaction1Layout.createSequentialGroup()
                        .addGroup(jPanelReaction1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jComboBox4, 0, 109, Short.MAX_VALUE)
                            .addComponent(jComboBox2, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBox0, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelReaction1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelReaction1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabelPlus3)
                                .addComponent(jLabelPlus5, javax.swing.GroupLayout.Alignment.TRAILING))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelReaction1Layout.createSequentialGroup()
                                .addComponent(jLabelPlus1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanelReaction1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jTextField1)
                                    .addComponent(jTextField3)
                                    .addComponent(jTextField5, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanelReaction1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jComboBox3, javax.swing.GroupLayout.Alignment.LEADING, 0, 119, Short.MAX_VALUE)
                                    .addComponent(jComboBox1, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jComboBox5, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanelReaction1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabelPlus6)
                                    .addComponent(jLabelPlus7))))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelReaction1Layout.setVerticalGroup(
            jPanelReaction1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelReaction1Layout.createSequentialGroup()
                .addGroup(jPanelReaction1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPlus1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPlus6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelReaction1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPlus3)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPlus7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelReaction1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPlus5)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabelLogK.setLabelFor(jTextFieldLogK);
        jLabelLogK.setText("log K° (25°C) =");

        jTextFieldLogK.setText("-99999");
        jTextFieldLogK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldLogKActionPerformed(evt);
            }
        });
        jTextFieldLogK.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldLogKFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldLogKFocusLost(evt);
            }
        });
        jTextFieldLogK.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextFieldLogKKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldLogKKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldLogKKeyTyped(evt);
            }
        });

        jLabel2.setText("<html>&#916;<i>H</i>&deg; =</html>");

        jTextFieldDeltH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldDeltHActionPerformed(evt);
            }
        });
        jTextFieldDeltH.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldDeltHFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldDeltHFocusLost(evt);
            }
        });
        jTextFieldDeltH.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextFieldDeltHKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldDeltHKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldDeltHKeyTyped(evt);
            }
        });

        jLabel3.setText("kJ/mol");

        jLabel4.setText("<html>&#916;<i>C<sub>p</sub></i>&deg;=</html>");

        jTextFieldDeltCp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldDeltCpActionPerformed(evt);
            }
        });
        jTextFieldDeltCp.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldDeltCpFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldDeltCpFocusLost(evt);
            }
        });
        jTextFieldDeltCp.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextFieldDeltCpKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldDeltCpKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldDeltCpKeyTyped(evt);
            }
        });

        jLabel5.setText("J/(K mol)");

        jLabel1.setLabelFor(jTextFieldComment);
        jLabel1.setText("Comment:");

        jTextFieldComment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldCommentActionPerformed(evt);
            }
        });
        jTextFieldComment.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldCommentFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldCommentFocusLost(evt);
            }
        });
        jTextFieldComment.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextFieldCommentKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldCommentKeyReleased(evt);
            }
        });

        jLabelRef.setLabelFor(jTextFieldRef);
        jLabelRef.setText("Reference(s):");

        jTextFieldRef.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldRefActionPerformed(evt);
            }
        });
        jTextFieldRef.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldRefFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldRefFocusLost(evt);
            }
        });
        jTextFieldRef.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextFieldRefKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldRefKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanelReaction2Layout = new javax.swing.GroupLayout(jPanelReaction2);
        jPanelReaction2.setLayout(jPanelReaction2Layout);
        jPanelReaction2Layout.setHorizontalGroup(
            jPanelReaction2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelReaction2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelReaction2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelReaction2Layout.createSequentialGroup()
                        .addGroup(jPanelReaction2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabelRef)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelReaction2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldComment)
                            .addComponent(jTextFieldRef)))
                    .addGroup(jPanelReaction2Layout.createSequentialGroup()
                        .addGroup(jPanelReaction2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelReaction2Layout.createSequentialGroup()
                                .addComponent(jLabelLogK)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldLogK, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanelReaction2Layout.createSequentialGroup()
                                .addGap(44, 44, 44)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldDeltH, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3))
                            .addGroup(jPanelReaction2Layout.createSequentialGroup()
                                .addGap(42, 42, 42)
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldDeltCp, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel5)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanelReaction2Layout.setVerticalGroup(
            jPanelReaction2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelReaction2Layout.createSequentialGroup()
                .addGroup(jPanelReaction2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldLogK, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelLogK))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelReaction2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jTextFieldDeltH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelReaction2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldDeltCp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelReaction2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextFieldComment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelReaction2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldRef, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelRef))
                .addGap(1, 1, 1))
        );

        jButtonSaveReac.setMnemonic('v');
        jButtonSaveReac.setText("Save");
        jButtonSaveReac.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveReacActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelReactionLayout = new javax.swing.GroupLayout(jPanelReaction);
        jPanelReaction.setLayout(jPanelReactionLayout);
        jPanelReactionLayout.setHorizontalGroup(
            jPanelReactionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelReactionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelReactionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelReactionLayout.createSequentialGroup()
                        .addGroup(jPanelReactionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelReaction)
                            .addComponent(jPanelReaction1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(49, Short.MAX_VALUE))
                    .addGroup(jPanelReactionLayout.createSequentialGroup()
                        .addComponent(jPanelReaction2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonSaveReac)
                        .addGap(18, 18, 18))))
        );
        jPanelReactionLayout.setVerticalGroup(
            jPanelReactionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelReactionLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabelReaction)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanelReaction1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelReactionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButtonSaveReac)
                    .addComponent(jPanelReaction2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanelReaction, "cardReaction");

        jLabelComp.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelComp.setText("New component:");

        jLabelCompName.setLabelFor(jTextFieldCompName);
        jLabelCompName.setText("name:");

        jTextFieldCompName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextFieldCompNameKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldCompNameKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldCompNameKeyTyped(evt);
            }
        });

        jLabelLinked.setText("Linked to: Cu");

        jLabelCompDescr.setLabelFor(jTextFieldCompDescr);
        jLabelCompDescr.setText("description:");

        jTextFieldCompDescr.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextFieldCompDescrKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldCompDescrKeyReleased(evt);
            }
        });

        jLabelElems.setLabelFor(jComboBoxElems);
        jLabelElems.setText("elements:");

        jComboBoxElems.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxElems.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxElemsActionPerformed(evt);
            }
        });
        jComboBoxElems.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jComboBoxElemsKeyPressed(evt);
            }
        });

        jButtonLink.setMnemonic('l');
        jButtonLink.setText("Link component to element");
        jButtonLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLinkActionPerformed(evt);
            }
        });
        jButtonLink.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jButtonLinkKeyPressed(evt);
            }
        });

        jButtonSaveComp.setMnemonic('v');
        jButtonSaveComp.setText("Save");
        jButtonSaveComp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveCompActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelComponentLayout = new javax.swing.GroupLayout(jPanelComponent);
        jPanelComponent.setLayout(jPanelComponentLayout);
        jPanelComponentLayout.setHorizontalGroup(
            jPanelComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelComponentLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButtonSaveComp)
                    .addGroup(jPanelComponentLayout.createSequentialGroup()
                        .addGroup(jPanelComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabelElems)
                            .addComponent(jLabelCompDescr)
                            .addComponent(jLabelCompName)
                            .addComponent(jLabelComp))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanelComponentLayout.createSequentialGroup()
                                .addComponent(jComboBoxElems, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButtonLink))
                            .addGroup(jPanelComponentLayout.createSequentialGroup()
                                .addComponent(jTextFieldCompName, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabelLinked))
                            .addComponent(jTextFieldCompDescr))))
                .addContainerGap(58, Short.MAX_VALUE))
        );
        jPanelComponentLayout.setVerticalGroup(
            jPanelComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelComponentLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelComp)
                .addGap(18, 18, 18)
                .addGroup(jPanelComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelCompName)
                    .addComponent(jTextFieldCompName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelLinked))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldCompDescr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelCompDescr))
                .addGap(23, 23, 23)
                .addGroup(jPanelComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxElems, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonLink)
                    .addComponent(jLabelElems))
                .addGap(34, 34, 34)
                .addComponent(jButtonSaveComp)
                .addContainerGap(160, Short.MAX_VALUE))
        );

        getContentPane().add(jPanelComponent, "cardComponent");

        jLabelFiles.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelFiles.setLabelFor(jTextAreaFiles);
        jLabelFiles.setText("Files:");

        jScrollPaneFiles.setFont(new java.awt.Font("Monospaced", 0, 13)); // NOI18N

        jTextAreaFiles.setColumns(20);
        jTextAreaFiles.setRows(3);
        jTextAreaFiles.setText("D:\\_USB\\Eq-Calc_Java\\Prog\\data\\DataMaintenance\\Th.elt D:\\_USB\\Eq-Calc_Java\\Prog\\data\\DataMaintenance\\Th.skv");
        jTextAreaFiles.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextAreaFilesMouseClicked(evt);
            }
        });
        jTextAreaFiles.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextAreaFilesFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextAreaFilesFocusLost(evt);
            }
        });
        jTextAreaFiles.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextAreaFilesKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextAreaFilesKeyTyped(evt);
            }
        });
        jScrollPaneFiles.setViewportView(jTextAreaFiles);

        jLabelReact.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelReact.setLabelFor(jListReact);
        jLabelReact.setText("Reactions:");

        jListReact.setModel(modelComplexes);
        jListReact.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListReact.setCellRenderer(new ComplexListCellRenderer());
        jListReact.setName("jListReact"); // NOI18N
        jListReact.setVisibleRowCount(6);
        jListReact.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListReactMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jListReactMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jListReactMouseReleased(evt);
            }
        });
        jListReact.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListReactValueChanged(evt);
            }
        });
        jListReact.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jListReactFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jListReactFocusLost(evt);
            }
        });
        jListReact.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jListReactKeyPressed(evt);
            }
        });
        jScrollPaneReact.setViewportView(jListReact);

        jLabelReactionText.setLabelFor(jListReact);
        jLabelReactionText.setText("jLabelReactionText jLabelReactionText  jLabelReactionText");

        javax.swing.GroupLayout jPanelReactionsLayout = new javax.swing.GroupLayout(jPanelReactions);
        jPanelReactions.setLayout(jPanelReactionsLayout);
        jPanelReactionsLayout.setHorizontalGroup(
            jPanelReactionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelReactionsLayout.createSequentialGroup()
                .addComponent(jLabelReact)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jScrollPaneReact)
            .addComponent(jLabelReactionText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanelReactionsLayout.setVerticalGroup(
            jPanelReactionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelReactionsLayout.createSequentialGroup()
                .addComponent(jLabelReact)
                .addGap(4, 4, 4)
                .addComponent(jScrollPaneReact, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelReactionText))
        );

        jLabelHelp.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabelHelp.setText("Press [Del] to delete; double-click or Alt-E to edit");

        jLabelComps.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelComps.setLabelFor(jListComps);
        jLabelComps.setText("Components:");

        jListComps.setModel(modelComponents);
        jListComps.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListComps.setName("jListComps"); // NOI18N
        jListComps.setVisibleRowCount(5);
        jListComps.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListCompsMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jListCompsMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jListCompsMouseReleased(evt);
            }
        });
        jListComps.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jListCompsFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jListCompsFocusLost(evt);
            }
        });
        jListComps.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jListCompsKeyPressed(evt);
            }
        });
        jScrollPaneComps.setViewportView(jListComps);

        javax.swing.GroupLayout jPanelCompsLayout = new javax.swing.GroupLayout(jPanelComps);
        jPanelComps.setLayout(jPanelCompsLayout);
        jPanelCompsLayout.setHorizontalGroup(
            jPanelCompsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCompsLayout.createSequentialGroup()
                .addComponent(jLabelComps)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jScrollPaneComps)
        );
        jPanelCompsLayout.setVerticalGroup(
            jPanelCompsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCompsLayout.createSequentialGroup()
                .addComponent(jLabelComps)
                .addGap(4, 4, 4)
                .addComponent(jScrollPaneComps))
        );

        javax.swing.GroupLayout jPanelFilesLayout = new javax.swing.GroupLayout(jPanelFiles);
        jPanelFiles.setLayout(jPanelFilesLayout);
        jPanelFilesLayout.setHorizontalGroup(
            jPanelFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelFilesLayout.createSequentialGroup()
                .addGroup(jPanelFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelFilesLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelFilesLayout.createSequentialGroup()
                                .addComponent(jLabelFiles)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPaneFiles))
                            .addComponent(jPanelReactions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanelComps, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanelFilesLayout.createSequentialGroup()
                        .addGap(160, 160, 160)
                        .addComponent(jLabelHelp)
                        .addGap(0, 49, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanelFilesLayout.setVerticalGroup(
            jPanelFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelFilesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelFiles)
                    .addComponent(jScrollPaneFiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelReactions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(jLabelHelp, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanelComps, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        getContentPane().add(jPanelFiles, "cardFiles");

        jMenuFile.setMnemonic('f');
        jMenuFile.setText("File");

        jMenuFileShow.setMnemonic('o');
        jMenuFileShow.setText("Show File contents");
        jMenuFileShow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuFileShowActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuFileShow);

        jMenuFileSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuFileSave.setMnemonic('v');
        jMenuFileSave.setText("Save file");
        jMenuFileSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuFileSaveActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuFileSave);

        jMenuFileExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK));
        jMenuFileExit.setMnemonic('x');
        jMenuFileExit.setText("Exit");
        jMenuFileExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuFileExitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuFileExit);

        jMenuBar.add(jMenuFile);

        jMenuAdd.setMnemonic('a');
        jMenuAdd.setText("Add data");

        jMenuAddReact.setMnemonic('r');
        jMenuAddReact.setText("add Reaction");
        jMenuAddReact.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuAddReactActionPerformed(evt);
            }
        });
        jMenuAdd.add(jMenuAddReact);

        jMenuAddComp.setMnemonic('c');
        jMenuAddComp.setText("add Component");
        jMenuAddComp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuAddCompActionPerformed(evt);
            }
        });
        jMenuAdd.add(jMenuAddComp);

        jMenuAddShow.setMnemonic('o');
        jMenuAddShow.setText("Show File contents");
        jMenuAddShow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuAddShowActionPerformed(evt);
            }
        });
        jMenuAdd.add(jMenuAddShow);
        jMenuAdd.add(jSeparatorAdd);

        jCheckBoxMenuMsg.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK));
        jCheckBoxMenuMsg.setMnemonic('s');
        jCheckBoxMenuMsg.setText("Show messages");
        jCheckBoxMenuMsg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuMsgActionPerformed(evt);
            }
        });
        jMenuAdd.add(jCheckBoxMenuMsg);

        jMenuBar.add(jMenuAdd);

        jMenuHelp.setMnemonic('h');
        jMenuHelp.setText("Help");

        jMenuHelpHlp.setMnemonic('h');
        jMenuHelpHlp.setText("Help");
        jMenuHelpHlp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuHelpHlpActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuHelpHlp);

        jMenuBar.add(jMenuHelp);

        setJMenuBar(jMenuBar);

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
      int nw = Math.max(w, Math.round((float)windowSize.getWidth()));
      int nh = Math.max(h, Math.round((float)windowSize.getHeight()));
      int nx=x, ny=y;
      if(x+nw > LibDB.screenSize.width) {nx = LibDB.screenSize.width - nw;}
      if(y+nh > LibDB.screenSize.height) {ny = LibDB.screenSize.height -nh;}
      if(x!=nx || y!=ny) {this.setLocation(nx, ny);}
      if(w!=nw || h!=nh) {this.setSize(nw, nh);}
    }//GEN-LAST:event_formComponentResized

    private void jTextFieldLogKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldLogKActionPerformed
        validateLogK();
    }//GEN-LAST:event_jTextFieldLogKActionPerformed

    private void jTextFieldLogKFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldLogKFocusGained
        jTextFieldLogK.selectAll();
    }//GEN-LAST:event_jTextFieldLogKFocusGained

    private void jTextFieldRefFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldRefFocusGained
        jTextFieldRef.selectAll();
    }//GEN-LAST:event_jTextFieldRefFocusGained

    private void jTextFieldComplexFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldComplexFocusGained
        rearrangeReaction();
        jTextFieldComplex.requestFocusInWindow();
        //jTextFieldComplex.setSelectionStart(jTextFieldComplex.getText().length());
        //jTextFieldComplex.selectAll();
        rearranging = false;
    }//GEN-LAST:event_jTextFieldComplexFocusGained

    private void jTextFieldLogKFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldLogKFocusLost
        validateLogK();
        update_newC();
    }//GEN-LAST:event_jTextFieldLogKFocusLost

    private void jTextFieldLogKKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldLogKKeyPressed
        if(evt.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER) {
            validateLogK();
        }
        else if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_UP) {
            jTextFieldComplex.requestFocusInWindow();
        }
        else if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
            jTextFieldDeltH.requestFocusInWindow();
        }
    }//GEN-LAST:event_jTextFieldLogKKeyPressed

    private void jTextFieldLogKKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldLogKKeyTyped
        char key = evt.getKeyChar();
        if(!isCharOKforNumberInput(key)) {evt.consume();}
    }//GEN-LAST:event_jTextFieldLogKKeyTyped

    private void jTextAreaFilesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextAreaFilesMouseClicked
        String addFileOld = addFile;
        if(!getAddFileName()) {return;}
        if(!addFileOld.equalsIgnoreCase(addFile)) {
            jMenuAddShow.setEnabled(true);
            jMenuAddShow.doClick();
        }
    }//GEN-LAST:event_jTextAreaFilesMouseClicked

    private void jListCompsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListCompsFocusGained
        int i = 0;
        if(old_i_comps >=0 && old_i_comps < modelComponents.getSize()) {i = old_i_comps;}
        jLabelHelp.setText("Press [Del] to delete; double-click or Alt-E to edit");
        jListComps.setSelectedIndex(i);
    }//GEN-LAST:event_jListCompsFocusGained

    private void jListCompsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListCompsFocusLost
        old_i_comps = jListComps.getSelectedIndex();
        if(!jPopupMenu.isVisible()) {jListComps.clearSelection();}
        jLabelHelp.setText(" ");
    }//GEN-LAST:event_jListCompsFocusLost

    private void jListReactFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListReactFocusGained
        int i = 0;
        if(old_i_react >=0 && old_i_react < modelComplexes.getSize()) {i = old_i_react;}
        jListReact.setSelectedIndex(i);
        Object value = jListReact.getSelectedValue();
        if(value == null) {jListReact.setSelectedIndex(0);}
        if(value != null) {
            String t;
            java.awt.Color fC;
            if (value instanceof Object[]) {
                Object values[] = (Object[]) value;
                try{
                    t = values[0].toString();
                    fC = (java.awt.Color) values[1];
                }
                catch (Exception ex) {
                    fC = jListReact.getForeground();
                    t = value.toString();
                }
            } else {
                fC = jListReact.getForeground();
                t = value.toString();
            }
            Complex c = null;
            try{c = Complex.fromString(t);}
            catch (Complex.ReadComplexException ex) {MsgExceptn.exception(ex.toString());}
            if(c != null) {
                jLabelReactionText.setText(Complex.reactionTextWithLogK(c,25));
                jLabelReactionText.setForeground(fC);
                jLabelHelp.setText("Press [Del] to delete; double-click or Alt-E to edit");
            }
        }
    }//GEN-LAST:event_jListReactFocusGained

    private void jListReactFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jListReactFocusLost
        if(!jPopupMenu.isVisible()) {
            old_i_react = jListReact.getSelectedIndex();
            jListReact.clearSelection();
            jLabelHelp.setText(" ");
            jLabelReactionText.setText(" ");
        }
    }//GEN-LAST:event_jListReactFocusLost

    private void jTextAreaFilesFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextAreaFilesFocusGained
        jTextAreaFiles.selectAll();
    }//GEN-LAST:event_jTextAreaFilesFocusGained

    private void jTextAreaFilesFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextAreaFilesFocusLost
        jTextAreaFiles.setCaretPosition(0);
    }//GEN-LAST:event_jTextAreaFilesFocusLost

    private void jTextAreaFilesKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextAreaFilesKeyTyped
      char c = Character.toUpperCase(evt.getKeyChar());
      if(evt.getKeyChar() != java.awt.event.KeyEvent.VK_ESCAPE
              && evt.getKeyChar() != java.awt.event.KeyEvent.VK_TAB
              && evt.getKeyChar() != java.awt.event.KeyEvent.VK_ENTER
              && !(evt.isAltDown()
                    && ((c == 'X') || (c == 'H') || (c == 'Q') || (c == 'S')
                        || (c == 'F') || (c == 'A')
                        || (evt.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER))
                 ) //isAltDown
              && !(evt.isControlDown())
              ) { // if not ESC or Alt-something
                        String addFileOld = addFile;
                        if(!getAddFileName()) {return;}
                        if(!addFileOld.equalsIgnoreCase(addFile)) {
                            jMenuAddShow.setEnabled(true);
                            jMenuAddShow.doClick();
                        }
        } // if char ok
      evt.consume();
    }//GEN-LAST:event_jTextAreaFilesKeyTyped

    private void jTextAreaFilesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextAreaFilesKeyPressed
        int k = evt.getKeyCode();
        if(k == java.awt.event.KeyEvent.VK_LEFT || k == java.awt.event.KeyEvent.VK_UP) {
            evt.consume();
            jListComps.requestFocusInWindow();
            return;
        } else if(k == java.awt.event.KeyEvent.VK_RIGHT || k == java.awt.event.KeyEvent.VK_DOWN) {
            evt.consume();
            jListReact.requestFocusInWindow();
            return;
        }
        int shft = java.awt.event.InputEvent.SHIFT_DOWN_MASK;
        if(k == java.awt.event.KeyEvent.VK_TAB) {
            evt.consume();
            if((evt.getModifiersEx() & shft) == shft) {
                jListComps.requestFocusInWindow();
            } else {
                jListReact.requestFocusInWindow();
            }
            return;
        }
        if(!Util.isKeyPressedOK(evt)) {evt.consume();}
    }//GEN-LAST:event_jTextAreaFilesKeyPressed

    private void jListReactValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListReactValueChanged
        int i = jListReact.getSelectedIndex();
        if(i>=0) {
            if(modelComplexes.get(i) != null) {
                String t;
                java.awt.Color fC;
                if (modelComplexes.get(i) instanceof ModelComplexesItem) {
                    ModelComplexesItem value = (ModelComplexesItem) modelComplexes.get(i);
                    try{
                        t = value.toString();
                        fC = value.getForeground();
                    }
                    catch (Exception ex) {
                        MsgExceptn.msg(ex.toString());
                        t = modelComplexes.get(i).toString();
                        fC = java.awt.Color.BLACK;
                    }
                } else {
                    t = modelComplexes.get(i).toString();
                    fC = java.awt.Color.BLACK;
                    if(dbg) {MsgExceptn.msg("modelComplexes.get("+i+") not instanceof ModelComplexesItem at \"jListReactValueChanged\""+nl+"    t = \""+t+"\"");}
                }
                Complex c = null;
                try{c = Complex.fromString(t);}
                catch (Complex.ReadComplexException ex) {MsgExceptn.exception(ex.toString());}
                if(c != null) {
                    jLabelReactionText.setText(Complex.reactionTextWithLogK(c,25));
                    jLabelReactionText.setForeground(fC);
                    //if(fC != null && fC == vermilion) {jLabelReactionText.setText("<html>"+jLabelReactionText.getText()+"  (<b>NOT</b> charge balanced)</html>");}
                    jLabelHelp.setText("Press [Del] to delete; double-click or Alt-E to edit");
                }
            } //value != null
        } else { //i<0
            jLabelReactionText.setText(" ");
        } //i?
    }//GEN-LAST:event_jListReactValueChanged

    private void jTextFieldCompNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldCompNameKeyPressed
        int k = evt.getKeyCode();
        if(k == java.awt.event.KeyEvent.VK_UP) {
            jButtonLink.requestFocusInWindow();
        } else if(k == java.awt.event.KeyEvent.VK_DOWN) {
            jTextFieldCompDescr.requestFocusInWindow();
        }
    }//GEN-LAST:event_jTextFieldCompNameKeyPressed

    private void jTextFieldCompDescrKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldCompDescrKeyPressed
        int k = evt.getKeyCode();
        if(k == java.awt.event.KeyEvent.VK_UP) {
            jTextFieldCompName.requestFocusInWindow();
        } else if(k == java.awt.event.KeyEvent.VK_DOWN) {
            jComboBoxElems.requestFocusInWindow();
        }
    }//GEN-LAST:event_jTextFieldCompDescrKeyPressed

    private void jButtonLinkKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jButtonLinkKeyPressed
        int k = evt.getKeyCode();
        if(k == java.awt.event.KeyEvent.VK_UP) {
            jTextFieldCompDescr.requestFocusInWindow();
        } else if(k == java.awt.event.KeyEvent.VK_DOWN) {
            jTextFieldCompName.requestFocusInWindow();
        } else if(k == java.awt.event.KeyEvent.VK_LEFT) {
            jComboBoxElems.requestFocusInWindow();
        } else if(k == java.awt.event.KeyEvent.VK_RIGHT) {
            jTextFieldCompName.requestFocusInWindow();
        }
    }//GEN-LAST:event_jButtonLinkKeyPressed

    private void jTextFieldComplexKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldComplexKeyPressed
        int k = evt.getKeyCode();
        if(k == java.awt.event.KeyEvent.VK_UP) {
            javax.swing.JComboBox<String> jcb = boxes.get(boxes.size()-1); // javax.swing.JComboBox jcb = boxes.get(boxes.size()-1); // java 1.6
            jcb.requestFocusInWindow();
        } else if(k == java.awt.event.KeyEvent.VK_DOWN) {
            jTextFieldLogK.requestFocusInWindow();
        }
    }//GEN-LAST:event_jTextFieldComplexKeyPressed

    private void jTextFieldRefKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldRefKeyPressed
        int k = evt.getKeyCode();
        if(k == java.awt.event.KeyEvent.VK_UP) {
            jTextFieldComment.requestFocusInWindow();
        } else if(k == java.awt.event.KeyEvent.VK_DOWN) {
            jTextField0.requestFocusInWindow();
        }
    }//GEN-LAST:event_jTextFieldRefKeyPressed

    private void jTextFieldRefActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldRefActionPerformed
        update_newC();
    }//GEN-LAST:event_jTextFieldRefActionPerformed

    private void jTextFieldRefFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldRefFocusLost
        update_newC();
    }//GEN-LAST:event_jTextFieldRefFocusLost

    private void jTextFieldComplexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldComplexActionPerformed
        update_newC();
    }//GEN-LAST:event_jTextFieldComplexActionPerformed

    private void jTextFieldComplexFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldComplexFocusLost
        update_newC();
    }//GEN-LAST:event_jTextFieldComplexFocusLost

    private void jTextFieldComplexKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldComplexKeyTyped
        if(evt.getKeyChar() == ',') {
            showErr("Sorry, No commas allowed"+nl+"in names for chemical species.",2);
            evt.consume();
        }
    }//GEN-LAST:event_jTextFieldComplexKeyTyped

    private void jComboBoxElemsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jComboBoxElemsKeyPressed
        int k = evt.getKeyCode();
        if(k == java.awt.event.KeyEvent.VK_LEFT) {
            jTextFieldCompDescr.requestFocusInWindow();
        } else if(k == java.awt.event.KeyEvent.VK_RIGHT) {
            jButtonLink.requestFocusInWindow();
        }
    }//GEN-LAST:event_jComboBoxElemsKeyPressed

    private void jListCompsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jListCompsKeyPressed
        int k = evt.getKeyCode();
        int alt = java.awt.event.InputEvent.ALT_DOWN_MASK;
        if(k == java.awt.event.KeyEvent.VK_LEFT) {
            jListReact.requestFocusInWindow();
        } else if(k == java.awt.event.KeyEvent.VK_RIGHT) {
            jTextAreaFiles.requestFocusInWindow();
        } else if(k == java.awt.event.KeyEvent.VK_ENTER ||
                    k == java.awt.event.KeyEvent.VK_SPACE) {
            jListComps_click();
        } else if(((evt.getModifiersEx() & alt) == alt) && k == java.awt.event.KeyEvent.VK_E) {
            jListComps_click();
        } else if (k == java.awt.event.KeyEvent.VK_DELETE ||
                    k == java.awt.event.KeyEvent.VK_BACK_SPACE) {
            jListComps_Del();
        } //delete
    }//GEN-LAST:event_jListCompsKeyPressed

    private void jListReactKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jListReactKeyPressed
        int k = evt.getKeyCode();
        int alt = java.awt.event.InputEvent.ALT_DOWN_MASK;
        if(k == java.awt.event.KeyEvent.VK_LEFT) {
            jTextAreaFiles.requestFocusInWindow();
        } else if(k == java.awt.event.KeyEvent.VK_RIGHT) {
            jListComps.requestFocusInWindow();
        } else if(k == java.awt.event.KeyEvent.VK_ENTER ||
                    k == java.awt.event.KeyEvent.VK_SPACE) {
            jListReact_click();
        } else if(((evt.getModifiersEx() & alt) == alt) && k == java.awt.event.KeyEvent.VK_E) {
            jListReact_click();
        } else if (k == java.awt.event.KeyEvent.VK_DELETE ||
                    k == java.awt.event.KeyEvent.VK_BACK_SPACE) {
            jListReact_Del();
        }
    }//GEN-LAST:event_jListReactKeyPressed

    private void jTextFieldCompNameKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldCompNameKeyTyped
        if(loading) {return;}
        if(evt.getKeyChar() == java.awt.event.KeyEvent.VK_COMMA) {evt.consume();}
    }//GEN-LAST:event_jTextFieldCompNameKeyTyped

    private void jListReactMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListReactMouseClicked
      if(evt.getClickCount() >=2) {jListReact_click();}
      else {
        java.awt.Point p = evt.getPoint();
        int i = jListReact.locationToIndex(p);
        if(i>=0) {
            java.awt.Rectangle r = jListReact.getCellBounds(i, i);
            if(p.y < r.y || p.y > r.y+r.height) {i=-1;}
            if(i>=0 && i < modelComplexes.getSize()) {
                old_i_react = i;
                jListReact.requestFocusInWindow();
                if(!isPopup) {return;}
                jMenuItemEdit.setEnabled(true);
                jMenuItemDel.setEnabled(true);
                jMenuItemDetails.setVisible(true);
                jPopupMenu.show(jListReact, evt.getX(), evt.getY());
                jListReact.setSelectedIndex(i);
            }
        }//if i>=0
        isPopup = false;
      } //double-click?
    }//GEN-LAST:event_jListReactMouseClicked

    private void jListCompsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListCompsMouseClicked
      if(evt.getClickCount() >=2) {jListComps_click();}
      else {
        java.awt.Point p = evt.getPoint();
        int i = jListComps.locationToIndex(p);
        if(i>=0) {
            java.awt.Rectangle r = jListComps.getCellBounds(i, i);
            if(r == null || p.y < r.y || p.y > r.y+r.height) {i=-1;}
            if(i>=0 && i < modelComponents.getSize()) {
                old_i_comps = i;
                jListComps.requestFocusInWindow();
                jListComps.setSelectedIndex(i);
                if(!isPopup) {return;}
                boolean enabled = true;
                String comp = modelComponents.get(i).toString();
                try {
                  comp = CSVparser.splitLine_1(comp);
                  if(comp.equals("e-") || comp.equals("H+") || comp.equals("H2O")) {enabled = false;}
                }
                catch (CSVparser.CSVdataException ex) {}
                jMenuItemEdit.setEnabled(enabled);
                jMenuItemDel.setEnabled(enabled);
                jMenuItemDetails.setVisible(false);
                jPopupMenu.show(jListComps, evt.getX(), evt.getY());
            }
        }//if i>=0
        isPopup = false;
      } //double-click?
    }//GEN-LAST:event_jListCompsMouseClicked

    private void jComboBoxElemsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxElemsActionPerformed
        if(loading) {return;}
        String element = jComboBoxElems.getSelectedItem().toString();
        if(element == null || element.length() <=0) {
            jButtonLink.setText("Link component to element");
            jButtonLink.setEnabled(false);
            return;
        } else {jButtonLink.setEnabled(true);}
        if(jLabelLinked.getText().length()>10) {
            String t = jLabelLinked.getText().substring(11);
            if(t.indexOf(element+",") >=0 ||
               t.endsWith(element)) {
                    jButtonLink.setText("un-Link component to element");
            }else {
                jButtonLink.setText("Link component to element");
            }
        } else {
            jButtonLink.setText("Link component to element");
        }
    }//GEN-LAST:event_jComboBoxElemsActionPerformed

    private void jButtonLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLinkActionPerformed
      String element = null;
      if(jComboBoxElems.getSelectedIndex()>=0) {element = jComboBoxElems.getSelectedItem().toString();}
      if(element == null || element.length() <=0) {return;}
      java.util.ArrayList<String> aL;
      if(jLabelLinked.getText() == null || jLabelLinked.getText().length() <=10 ||
        !jLabelLinked.getText().startsWith("Linked to:")) {
          jLabelLinked.setText("");
          aL = new java.util.ArrayList<String>(1);
          aL.add(element);
          jButtonLink.setText("un-Link component to element");
      } else { // there is jLabelLinked
        try{
            aL = CSVparser.splitLine(jLabelLinked.getText().substring(10));
        }
        catch (CSVparser.CSVdataException ex) {
            MsgExceptn.exception(Util.stack2string(ex));
            return;
        }
        if(jButtonLink.getText().startsWith("un-L")) { //un-link
          for(int i=0; i<aL.size(); i++) {
              if(aL.get(i).equals(element)) {aL.set(i,""); break;}
          }
          jButtonLink.setText("Link component to element");
        } else { //link
          aL.add(element);
          jButtonLink.setText("un-Link component to element");
        } // un-link?
      } // was there a jLabelLinked?
      boolean empty = true;
      jLabelLinked.setText("Linked to: ");
      java.util.Collections.sort(aL,String.CASE_INSENSITIVE_ORDER);
      for(int i=0; i<aL.size(); i++) {
          if(aL.get(i).length() <=0) {continue;}
          empty = false;
          //append ", "?
          if(!jLabelLinked.getText().endsWith(" ")) {jLabelLinked.setText(jLabelLinked.getText()+", ");}
          jLabelLinked.setText(jLabelLinked.getText() + aL.get(i));
      }
      if(empty) {jLabelLinked.setText("");}
      if(!jLabelLinked.getText().equals(oldComponentLinked) ||
         !jTextFieldCompName.getText().equals(oldComponentName) ||
         !jTextFieldCompDescr.getText().equals(oldComponentDescr)) {
            jMenuFileSave.setEnabled(true);
            jButtonSaveComp.setEnabled(true);
      } else {
          jMenuFileSave.setEnabled(false);
          jButtonSaveComp.setEnabled(false);
      }
    }//GEN-LAST:event_jButtonLinkActionPerformed

    private void jTextFieldCompNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldCompNameKeyReleased
        if(!jLabelLinked.getText().equals(oldComponentLinked) ||
           !jTextFieldCompName.getText().equals(oldComponentName) ||
           !jTextFieldCompDescr.getText().equals(oldComponentDescr)) {
                jMenuFileSave.setEnabled(true);
                jButtonSaveComp.setEnabled(true);
        } else {
            jMenuFileSave.setEnabled(false);
            jButtonSaveComp.setEnabled(false);
        }
    }//GEN-LAST:event_jTextFieldCompNameKeyReleased

    private void jTextFieldCompDescrKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldCompDescrKeyReleased
        if(!jLabelLinked.getText().equals(oldComponentLinked) ||
           !jTextFieldCompName.getText().equals(oldComponentName) ||
           !jTextFieldCompDescr.getText().equals(oldComponentDescr)) {
                jMenuFileSave.setEnabled(true);
                jButtonSaveComp.setEnabled(true);
        } else {
            jMenuFileSave.setEnabled(false);
            jButtonSaveComp.setEnabled(false);
        }
    }//GEN-LAST:event_jTextFieldCompDescrKeyReleased

    private void jTextFieldComplexKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldComplexKeyReleased
        update_newC();
    }//GEN-LAST:event_jTextFieldComplexKeyReleased

    private void jTextFieldRefKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldRefKeyReleased
        update_newC();
    }//GEN-LAST:event_jTextFieldRefKeyReleased

    private void jTextFieldLogKKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldLogKKeyReleased
        update_newC();
    }//GEN-LAST:event_jTextFieldLogKKeyReleased

    private void jMenuItemCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCancelActionPerformed
        jPopupMenu.setVisible(false);
    }//GEN-LAST:event_jMenuItemCancelActionPerformed

    private void jListReactMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListReactMousePressed
        if(evt.isPopupTrigger()) {isPopup = true;}
    }//GEN-LAST:event_jListReactMousePressed

    private void jListReactMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListReactMouseReleased
        if(evt.isPopupTrigger()) {isPopup = true;}
    }//GEN-LAST:event_jListReactMouseReleased

    private void jMenuItemEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemEditActionPerformed
        if(jPopupMenu.getInvoker().getName().equals(jListReact.getName())) {
            jListReact_click();
        } else if(jPopupMenu.getInvoker().getName().equals(jListComps.getName())) {
            jListComps_click();
        }
    }//GEN-LAST:event_jMenuItemEditActionPerformed

    private void jMenuItemDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDelActionPerformed
        if(jPopupMenu.getInvoker().getName().equals(jListReact.getName())) {
            jListReact_Del();
        } else if(jPopupMenu.getInvoker().getName().equals(jListComps.getName())) {
            jListComps_Del();
        }
    }//GEN-LAST:event_jMenuItemDelActionPerformed

    private void jListCompsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListCompsMousePressed
        if(evt.isPopupTrigger()) {isPopup = true;}
    }//GEN-LAST:event_jListCompsMousePressed

    private void jListCompsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListCompsMouseReleased
        if(evt.isPopupTrigger()) {isPopup = true;}
    }//GEN-LAST:event_jListCompsMouseReleased

    private void jMenuItemDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDetailsActionPerformed
      int index = jListReact.getSelectedIndex();
      if(index <0 || index >= modelComplexes.size()) {return;}
      Complex cmplx;
      try{cmplx = Complex.fromString(modelComplexes.get(index).toString());}
      catch (Complex.ReadComplexException ex) {cmplx = null;}
      if(cmplx == null || cmplx.name == null || cmplx.name.length() <=0) {return;}
      String refKeys = cmplx.reference.trim();
      if(pc.dbg) {System.out.println("Show reference(s) for: \""+cmplx.name+"\""+nl+
              "   ref: \""+cmplx.reference.trim()+"\"");}
      ShowDetailsDialog sd = new ShowDetailsDialog(this, true, cmplx, pd.references);
      jListReact.requestFocusInWindow();
      jListReact.setSelectedIndex(index);
    }//GEN-LAST:event_jMenuItemDetailsActionPerformed

    private void jMenuAddReactActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuAddReactActionPerformed
      if(!loading && jPanelComponent.isShowing()) {
          if(jMenuFileSave.isEnabled()) {
              if(!discardChanges()) {
                  jTextAreaFiles.requestFocusInWindow();
                  return;
                }
          }
      }

      cl = (java.awt.CardLayout)getContentPane().getLayout();
      cl.show(getContentPane(),"cardReaction");

      newC = new Complex();
      try {oldNewC = (Complex)newC.clone();}
      catch (CloneNotSupportedException ex) {
          MsgExceptn.exception("CloneNotSupportedException in jMenuAddReact.");
          oldNewC = null;
      }
      updateJPanelReaction(newC);
      jLabelReaction.setText("New reaction:");
      jMenuAddReact.setEnabled(false);
      jMenuAddComp.setEnabled(true);
      jMenuAddShow.setEnabled(true);
      jMenuFileSave.setEnabled(false);
      jButtonSaveReac.setEnabled(false);
      jButtonSaveComp.setEnabled(false);
      jMenuFileShow.setEnabled(true);
      texts[0].requestFocusInWindow();
    }//GEN-LAST:event_jMenuAddReactActionPerformed

    private void jMenuAddCompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuAddCompActionPerformed
      if(!loading && jPanelReaction.isShowing()) {
          update_newC();
          if(jMenuFileSave.isEnabled()) {
            if(!Complex.isChargeBalanced(newC)) {
                if(!noChargeBalanceQuestion(newC.name)) {
                    jTextFieldComplex.requestFocusInWindow();
                    return;
                } else {
                    jMenuFileSave.setEnabled(false);
                    jButtonSaveComp.setEnabled(false);
                }
            } else
            if(!discardChanges()) {
                jTextFieldComplex.requestFocusInWindow();
                return;
            }
          }
      }
      cl = (java.awt.CardLayout)getContentPane().getLayout();
      cl.show(getContentPane(),"cardComponent");
      for(int i=0; i < jComboBoxElems.getItemCount(); i++) {
        if(jComboBoxElems.getItemAt(i).equals("C")) {
            jComboBoxElems.setSelectedIndex(i);
            break;
        }
      }
      oldComponentName = "";
      oldComponentDescr = "";
      oldComponentLinked = "";
      jLabelLinked.setText("");
      jTextFieldCompName.setText("");
      jTextFieldCompDescr.setText("");
      jLabelComp.setText("New component:");
      jMenuAddReact.setEnabled(true);
      jMenuAddComp.setEnabled(false);
      jMenuAddShow.setEnabled(true);
      jMenuFileShow.setEnabled(true);
      jTextFieldCompName.requestFocusInWindow();
    }//GEN-LAST:event_jMenuAddCompActionPerformed

    private void jMenuAddShowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuAddShowActionPerformed
      if(dbg) {System.out.println("-- jMenuAddShow ActionPerformed (evt)");}
      if(!loading && jPanelReaction.isShowing()) {
        update_newC();
        if(jMenuFileSave.isEnabled()) {
            if(!Complex.isChargeBalanced(newC)) {
                if(!noChargeBalanceQuestion(newC.name)) {
                    jTextFieldComplex.requestFocusInWindow();
                    return;
                } else {
                    jMenuFileSave.setEnabled(false);
                    jButtonSaveReac.setEnabled(false);
                    jButtonSaveComp.setEnabled(false);
                }
            } else
            if(!discardChanges()) {
                jTextFieldComplex.requestFocusInWindow();
                return;
            }
        }
      }
      if(!loading && jPanelComponent.isShowing()) {
        if(jMenuFileSave.isEnabled()) {
            if(!discardChanges()) {
                jTextFieldCompName.requestFocusInWindow();
                return;
            }
        }
      }
      if(dbg) {System.out.println("-- jMenuAddShow");}
      cl = (java.awt.CardLayout)getContentPane().getLayout();
      cl.show(getContentPane(),"cardFiles");
      jLabelReactionText.setText(" ");
      jLabelHelp.setText(" ");
      jMenuFileSave.setEnabled(false);
      jButtonSaveReac.setEnabled(false);
      jButtonSaveComp.setEnabled(false);
      modelComplexes.clear();
      modelComponents.clear();
      jMenuAddReact.setEnabled(true);
      jMenuAddComp.setEnabled(true);
      jMenuAddShow.setEnabled(false);
      jMenuFileShow.setEnabled(false);
      final java.io.File f = new java.io.File(addFile);
      final java.io.File fe = new java.io.File(addFileEle);
      if(f.exists() || fe.exists()) {
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        Thread work = new Thread() {@Override public void run() {
          update_componentsAdd(); //needed if later trying to edit a reaction
          if(f.exists()) {addFile_Read();}
          try{
              AddDataElem.elemCompAdd_Update(dbg, FrameAddData.this,
                  addFile, addFileEle, pd.elemComp, elemCompAdd);
          }
          catch (AddDataElem.AddDataException ex) {showErr(ex.toString(),0);}
          modelComps_update(dbg, elemCompAdd, modelComponents);
          javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
              FrameAddData.this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
          }}); //invokeLater(Runnable)
        }};//new Thread
        work.start();  //any statements placed below are executed inmediately
      }
    }//GEN-LAST:event_jMenuAddShowActionPerformed

    private void jMenuFileShowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuFileShowActionPerformed
        jMenuAddShow.doClick();
    }//GEN-LAST:event_jMenuFileShowActionPerformed

    private void jMenuFileSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuFileSaveActionPerformed
        if(jPanelReaction.isShowing()) {jButtonSaveReac.doClick();}
        else
        if(jPanelComponent.isShowing()) {jButtonSaveComp.doClick();}
    }//GEN-LAST:event_jMenuFileSaveActionPerformed

    private void jMenuFileExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuFileExitActionPerformed
        closeWindow();
    }//GEN-LAST:event_jMenuFileExitActionPerformed

    private void jMenuHelpHlpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuHelpHlpActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        Thread hlp = new Thread() {@Override public void run(){
            String[] a = {"DB_Add_data_htm"};
            lib.huvud.RunProgr.runProgramInProcess(null,ProgramConf.HELP_JAR,a,false,pc.dbg,pc.pathAPP);
            try{Thread.sleep(1500);}   //show the "wait" cursor for 1.5 sec
            catch (InterruptedException e) {}
            setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }};//new Thread
        hlp.start();
    }//GEN-LAST:event_jMenuHelpHlpActionPerformed

    private void jCheckBoxMenuMsgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuMsgActionPerformed
        if(pd.msgFrame != null) {pd.msgFrame.setVisible(jCheckBoxMenuMsg.isSelected());}
    }//GEN-LAST:event_jCheckBoxMenuMsgActionPerformed

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        if(pd.msgFrame != null) {jCheckBoxMenuMsg.setSelected(pd.msgFrame.isVisible());}
        else {jCheckBoxMenuMsg.setEnabled(false);}
    }//GEN-LAST:event_formWindowGainedFocus

    private void jButtonSaveReacActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveReacActionPerformed
        addFile_SaveReaction();
    }//GEN-LAST:event_jButtonSaveReacActionPerformed

    private void jButtonSaveCompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveCompActionPerformed
        componentSave();
    }//GEN-LAST:event_jButtonSaveCompActionPerformed

    private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved
        if(!loading) {pd.addDataLocation = this.getLocation();}
    }//GEN-LAST:event_formComponentMoved

    private void jTextFieldCommentFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldCommentFocusGained
        jTextFieldComment.selectAll();
    }//GEN-LAST:event_jTextFieldCommentFocusGained

    private void jTextFieldDeltHKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldDeltHKeyPressed
        if(evt.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER) {
            validateDeltH();
        }
        else if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_UP) {
            jTextFieldLogK.requestFocusInWindow();
        }
        else if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
            jTextFieldDeltCp.requestFocusInWindow();
        }
    }//GEN-LAST:event_jTextFieldDeltHKeyPressed

    private void jTextFieldDeltCpKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldDeltCpKeyPressed
        if(evt.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER) {
            validateDeltCp();
        }
        else if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_UP) {
            jTextFieldDeltH.requestFocusInWindow();
        }
        else if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
            jTextFieldComment.requestFocusInWindow();
        }
    }//GEN-LAST:event_jTextFieldDeltCpKeyPressed

    private void jTextFieldCommentKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldCommentKeyPressed
        int k = evt.getKeyCode();
        if(k == java.awt.event.KeyEvent.VK_UP) {
            jTextFieldDeltCp.requestFocusInWindow();
        } else if(k == java.awt.event.KeyEvent.VK_DOWN) {
            jTextFieldRef.requestFocusInWindow();
        }
    }//GEN-LAST:event_jTextFieldCommentKeyPressed

    private void jTextFieldCommentKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldCommentKeyReleased
        update_newC();
    }//GEN-LAST:event_jTextFieldCommentKeyReleased

    private void jTextFieldDeltHKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldDeltHKeyReleased
        update_newC();
    }//GEN-LAST:event_jTextFieldDeltHKeyReleased

    private void jTextFieldDeltCpKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldDeltCpKeyReleased
        update_newC();
    }//GEN-LAST:event_jTextFieldDeltCpKeyReleased

    private void jTextFieldCommentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldCommentActionPerformed
        update_newC();
    }//GEN-LAST:event_jTextFieldCommentActionPerformed

    private void jTextFieldCommentFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldCommentFocusLost
        update_newC();
    }//GEN-LAST:event_jTextFieldCommentFocusLost

    private void jTextFieldDeltHKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldDeltHKeyTyped
        char key = evt.getKeyChar();
        if(!isCharOKforNumberInput(key)) {evt.consume();}
    }//GEN-LAST:event_jTextFieldDeltHKeyTyped

    private void jTextFieldDeltCpKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldDeltCpKeyTyped
        char key = evt.getKeyChar();
        if(!isCharOKforNumberInput(key)) {evt.consume();}
    }//GEN-LAST:event_jTextFieldDeltCpKeyTyped

    private void jTextFieldDeltHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldDeltHActionPerformed
        validateDeltH();
    }//GEN-LAST:event_jTextFieldDeltHActionPerformed

    private void jTextFieldDeltCpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldDeltCpActionPerformed
        validateDeltCp();
    }//GEN-LAST:event_jTextFieldDeltCpActionPerformed

    private void jTextFieldDeltHFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldDeltHFocusGained
        jTextFieldDeltH.selectAll();
    }//GEN-LAST:event_jTextFieldDeltHFocusGained

    private void jTextFieldDeltCpFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldDeltCpFocusGained
        jTextFieldDeltCp.selectAll();
    }//GEN-LAST:event_jTextFieldDeltCpFocusGained

    private void jTextFieldDeltHFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldDeltHFocusLost
        validateDeltH();
        update_newC();
    }//GEN-LAST:event_jTextFieldDeltHFocusLost

    private void jTextFieldDeltCpFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldDeltCpFocusLost
        validateDeltCp();
        update_newC();
    }//GEN-LAST:event_jTextFieldDeltCpFocusLost

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="methods">

  //<editor-fold defaultstate="collapsed" desc="closeWindow()">
  private void closeWindow() {
    System.out.println("---- FrameAddData.closeWindow()");
    if(!loading) {
        if(jPanelReaction.isShowing()) {
            update_newC();
            if(jMenuFileSave.isEnabled()) {
                if(!Complex.isChargeBalanced(newC)) {if(!noChargeBalanceQuestion(newC.name)) {return;}}
                if(!discardChanges()) {return;}
            }
        } else { //jPanelReaction is not showing
            if(jMenuFileSave.isEnabled()) {if(!discardChanges()) {return;}}
        } //jPanelReaction showing?
        // --- check the database for errors
        java.util.ArrayList<String> arrayList = new java.util.ArrayList<String>();
        arrayList.add(addFile);
        CheckDatabases.CheckDataBasesLists lists = new CheckDatabases.CheckDataBasesLists();
        CheckDatabases.checkDatabases(pc.dbg, this, arrayList, null, lists);
        java.io.File f = new java.io.File(addFile);
        boolean ok = CheckDatabases.displayDatabaseErrors(pc.dbg, this, pc.progName, f.getName(), lists);
        if(!ok) {
            if(dbg) {System.out.println("--- displayDatabaseErrors: NOT OK for file: "+addFile);}
            return;
        }

        // remove un-needed components?
        removeUnusedComps();
        pd.addDataLocation.x = this.getX();
        pd.addDataLocation.y = this.getY();
    }//if !loading
    finished = true;    //return from "waitFor()"
    this.notify_All();
    this.dispose();
  } // closeWindow()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="bringToFront()">
  public void bringToFront() {
    if(this != null) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                setVisible(true);
                if((getExtendedState() & javax.swing.JFrame.ICONIFIED) // minimised?
                            == javax.swing.JFrame.ICONIFIED) {
                    setExtendedState(javax.swing.JFrame.NORMAL);
                } // if minimized
                setAlwaysOnTop(true);
                toFront();
                requestFocus();
                setAlwaysOnTop(false);
            }
        });
    }
  } // bringToFront()
  //</editor-fold>

  /** @return true if it is ok to close this window */
  public boolean queryClose() {
    if(dbg) {System.out.println("---- FrameAddData.queryClose()");}
    return !jMenuFileSave.isEnabled() || discardChanges();
  }

  /** this method will wait for this window to be closed */
  public synchronized void waitFor() {
    while(!finished) {
        try {wait();} catch (InterruptedException ex) {}
    } // while
  } // waitFor()
  private synchronized void notify_All() { //needed by "waitFor()"
      notifyAll();
  }

  //<editor-fold defaultstate="collapsed" desc="frameEnable">
  /** If <code>false</code> sets the card layout to show files,
   * and sets background colours to grey; menus are disabled and
   * a text label shows "please wait". If <code>true</code> then
   * the window is restored to working state; swithching to some
   * other card in the layout is done elsewhere.
   * @param enable  */
  private void frameEnable(boolean enable) {
      java.awt.Color clr;
      if(enable) {
        clr = new java.awt.Color(255,255,255);
      } else {
        clr = new java.awt.Color(215,215,215);
      }
      jLabelFiles.setEnabled(enable);
      jLabelReact.setEnabled(enable);
      jLabelComps.setEnabled(enable);
      jTextAreaFiles.setEnabled(enable);
      jListReact.setEnabled(enable);
      jListComps.setEnabled(enable);
      jTextAreaFiles.setBackground(clr);
      jListReact.setBackground(clr);
      jListComps.setBackground(clr);
      jMenuFileShow.setEnabled(enable);
      jMenuFileSave.setEnabled(enable);
      jMenuAddReact.setEnabled(enable);
      jMenuAddComp.setEnabled(enable);
      jMenuAddShow.setEnabled(enable);
      jLabelReactionText.setText(" ");

      if(!enable) {
        cl = (java.awt.CardLayout)getContentPane().getLayout();
        cl.show(getContentPane(),"cardFiles");
        jLabelHelp.setText("( Please wait ... )");
      } else {
        jLabelHelp.setText(" ");
      }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="noChargeBalance Warning/Question">
  private void noChargeBalanceWarning(String name) {
    showErr("The reaction for \""+name+"\""+nl+
            "is NOT charge balanced."+nl+nl+
            "Please correct either the reaction or"+nl+
            "the electric charge of \""+name+"\"",-1);
  } //noChargeBalanceWarning(name)

  /** call this method if Complex with "name" is not charge balanced 
   * @param name of the Complex
   * @return true if the user wants to go ahead ("exit anyway"), even with
   * a non-charge balanced complex; false if the user cancels the operation
   * (to adjust the charge balance before proceeding) */
  private boolean noChargeBalanceQuestion(String name) {
    Object[] opt = {"Exit anyway", "Cancel"};
    int m = javax.swing.JOptionPane.showOptionDialog(this,
        "The reaction for \""+name+"\""+nl+
        "is NOT charge balanced."+nl+nl+
        "Choose \"[Cancel]\" to make corrections.",
        pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
        javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
    return m == javax.swing.JOptionPane.YES_OPTION;
  }
  //</editor-fold>

  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="addFile_Initialise">
/** If <code>reactionFile</code> does not exist, create a new file with a single
 * title line.
 * @param dbg set to true in order to output messages to System.out
 * @param reactionFile name with path
 * @return true if the file did not exist and it has been created;
 * false if the file already exists
 * @throws FrameAddData.AddDataFrameInternalException 
 */
  private static boolean addFile_Initialise(boolean dbg, String reactionFile)
        throws AddDataFrameInternalException {
    if(reactionFile == null || reactionFile.length() <=0) {
        throw new AddDataFrameInternalException("Error: empty file name");
    }
    java.io.File rf = new java.io.File(reactionFile);
    if(dbg) {System.out.println("-- addFile_Initialise("+rf.getName()+")");}
    if(rf.exists()) {
        if(dbg) {System.out.println("  file already exists");}
        return false;
    }
    java.io.PrintWriter pw = null;
    try {
        pw =  new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter(rf)));
        pw.println(Complex.FILE_FIRST_LINE);
    }
    catch (java.io.IOException ex) {
        if(pw != null) {pw.close();}
        throw new AddDataFrameInternalException("Error: "+ex.getMessage()+nl+
                "   for file \""+reactionFile+"\"");
    }
    finally {if(pw != null) {pw.close();}}
    return true;
  } //addFile_Initialise(file)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="addFile_ComplexAppend">
/** Append a line to <code>reactionFile</code> with a Complex.
 * @param dbg set to true in order to output messages to System.out
 * @param reactionFile name with path
 * @return true if the file did not exist and it has been created;
 * false if the file already exists
 * @throws FrameAddData.AddDataFrameInternalException 
 */
  private static void addFile_ComplexAppend(boolean dbg, String reactionFile, Complex c)
        throws AddDataFrameInternalException {
    if(dbg) {System.out.println("-- addFile_ComplexAppend("+c.name+")");}
    if(reactionFile == null || reactionFile.length() <=0) {
        throw new AddDataFrameInternalException("Error: empty file name");
    }
    if(c == null || c.name.length() <=0) {
        throw new AddDataFrameInternalException("Error: no complex to append");
    }
    java.io.File rf = new java.io.File(reactionFile);
    if(rf.exists() && (!rf.canWrite() || !rf.setWritable(true))) {
        throw new AddDataFrameInternalException ("Error: can not write to file"+nl+"    \""+reactionFile+"\".");
    }
    java.io.PrintWriter pw = null;
    try {
        boolean append = true;
        pw =  new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter(rf, append)));
        pw.println(c.toString());
    }
    catch (java.io.IOException ex) {
        if(pw != null) {pw.close();}
        throw new AddDataFrameInternalException("Error: "+ex.toString()+nl+
                "   appending complex "+c.name+nl+
                "   to file \""+reactionFile+"\"");
    }
    pw.close();
  } //addFile_ComplexAppend(file, complex)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="addFile_ComplexDelete">
/** Read the file <code>addFile</code> and write a temporary file line by line,
 * excluding lines with the complex to remove;
 * delete the original file, rename the temporary file to the original name.
 * If <code>cDel.name</code> starts with "@" then any line starting with the name,
 * with or without "@", will be removed.
 * 
 * @param cDel complex to delete
 * @throws FrameAddData.AddDataFrameInternalException 
 */
  private void addFile_ComplexDelete(Complex cDel)
        throws AddDataFrameInternalException {
    if(dbg) {System.out.println("-- addFile_ComplexDelete("+cDel.name+")");}
    // ---- make some checks
    if(cDel == null || cDel.name == null || cDel.name.length() <=0) {
        throw new AddDataFrameInternalException("Error: empty species name");
    }
    if(addFile == null || addFile.length() <=0) {
        throw new AddDataFrameInternalException("Error: empty file name");
    }
    boolean startsWithAt = false;
    String cName = cDel.name;
    if(cName.startsWith("@")) {
        if(cName.length() <=1) {throw new AddDataFrameInternalException("Error: species name is \"@\" (must contain additional characters)");}
        startsWithAt = true;
        cName = cName.substring(1);
    }
    if(cName.contains(",")) {
        throw new AddDataFrameInternalException("Error: species "+cName+" contains a comma (,)");
    }
    java.io.File rf = new java.io.File(addFile);
    if(!rf.exists() || !rf.canRead()) {
        String msg = "Error: can not open file"+nl+"    \""+addFile+"\"";
        if(!rf.exists()) {msg = msg +nl+ "(the file does not exist)";}
        throw new AddDataFrameInternalException(msg);
    }
    if(!rf.canWrite() || !rf.setWritable(true)) {
        throw new AddDataFrameInternalException ("Error: can not write to file"+nl+"    \""+addFile+"\"");
    }

    // --- before doing the work check that the species name is really there
    int n;
    if(startsWithAt) {n = addFile_ComplexFindName(cName,false);}
    else {n = addFile_ComplexFindName(cName,true);}
    if(n == 0) {
        if(dbg) {System.out.println("   "+cName+" not found. No lines to delete");}
        return;
    }

    // --- open input and output files
    java.io.BufferedReader br;
    try{br = new java.io.BufferedReader(new java.io.FileReader(rf));}
    catch (java.io.FileNotFoundException ex) {throw new AddDataFrameInternalException(ex.toString());}

    java.io.File tmpF = new java.io.File(addFileTmp);
    if(dbg) {System.out.println("   copying lines from \""+rf.getName()+"\"  to  \""+tmpF.getName()+"\"");}
    boolean ok;
    if(tmpF.exists()) {
        if(dbg) {System.out.println("   deleting \""+tmpF.getName()+"\"");}
        try{ok = tmpF.delete();}
        catch (Exception ex) {throw new AddDataFrameInternalException(ex.toString());}
        if(!ok) {throw new AddDataFrameInternalException("Could not delete file:"+nl+"\""+addFileTmp+"\"");}
    }
    addFile_Initialise(dbg, addFileTmp);
    java.io.PrintWriter pw;
    boolean append = true;
    try {pw =  new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter(tmpF, append)));}
    catch (java.io.IOException ex) {throw new AddDataFrameInternalException(ex.toString());}

    String line = null, errMsg = null;
    Complex cRead;
    try{
        while ((line = br.readLine()) != null){
            if(line.trim().length() <=0) {continue;}
            if(line.toUpperCase().startsWith("COMPLEX")) {continue;}
            cRead = Complex.fromString(line);
            if(cRead.name.startsWith("@")) {cRead.name = cRead.name.substring(1);}
            if(Util.nameCompare(cName, cRead.name)) {continue;}
            pw.println(line); pw.flush();
        } //while
    } //try
    catch (java.io.IOException ex) {
        MsgExceptn.exception(Util.stack2string(ex));
        errMsg = ex.toString()+nl+"with file:\""+addFile+"\"";
    } catch (Complex.ReadComplexException ex) {
        MsgExceptn.exception(Util.stack2string(ex));
        errMsg = ex.toString()+nl+"in line: \""+line+"\""+nl+"in file:\""+addFile+"\"";
    }
    finally {
      if(errMsg != null) {showErr(errMsg,0);}
      if(dbg) {System.out.println("  closing files");}
      try {br.close();}
      catch (java.io.IOException ex) {showErr(ex.toString()+nl+"with file:\""+addFile+"\"",0);}
      pw.close();
    }
    line = null;
    if(dbg) {System.out.println("   deleting \""+rf.getName()+"\"");}
    try{ok = rf.delete();}
    catch (Exception ex) {
        line = ex.toString()+nl;
        ok = false;
    }
    if(!ok || line != null) {
        if(line == null) {line = "";} 
        showErr(line+"Could not delete file:"+nl+"\""+addFile+"\"",0);
        return;
    }
    if(dbg) {System.out.println("   renaming \""+tmpF.getName()+"\"  to  \""+rf.getName()+"\"");}
    line = null;
    try{ok = tmpF.renameTo(rf);}
    catch (Exception ex) {
        line = ex.toString()+nl;
        ok = false;
    }
    if(!ok || line != null) {
        if(line == null) {line = "";} 
        showErr(line+"Could not rename file:"+nl+"\""+addFileTmp+"\""+nl+"into: \""+addFile+"\"",0);
    }
  } //addFile_ComplexDelete(name)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="addFile_ComplexFind">
/** Is the reaction "<code>c</code>" found in file <code>addFile</code>?
 * The names of the products are compared taking into account different ways to write
 * charges so that "Fe+3" is equal to "Fe 3+". Also "CO2" is equivalent to "CO2(aq)".
 * Note however that names of reactants must match exactly: "Fe+3" is NOT equal
 * to "Fe 3+" and "H+" is not equal to "H +".  Two reactions are equivalent if
 * they have the same product name and if the reaction is the same,
 * even if the order of the reactants differs.
 * For example, <code>A + B = C</code> is equal to <code>B + A = C</code>
 * <p>
 * If "<code>c.name</code>" starts with "@" then only the reaction name
 * is searched, equivalent to: <code>addFile_ComplexFindName(c.name, true)</code>,
 * that is, it looks for an equivalent name also stating with "@". For example,
 * "@Fe+2" will match "@Fe 2+" but not "Fe+2"
 * @param c
 * @return how many times the given reaction is found; zero if not found
 * @see FrameAddData#addFile_ComplexFindName addFile_ComplexFindName
 */
  private int addFile_ComplexFind(Complex c) {
    if(c == null) {
        MsgExceptn.exception("Error: complex = null in \"addFile_ComplexFind\"");
        return 0;
    }
    Complex cFind;
    try {cFind = (Complex)c.clone();}
    catch (CloneNotSupportedException ex) {
        MsgExceptn.exception("Error: CloneNotSupportedException in \"addFile_ComplexFind\"");
        return 0;
    }
    if(dbg) {System.out.println("-- addFile_ComplexFind("+cFind.name+")");}
    if(cFind.name == null || cFind.name.length() <= 0) {
        MsgExceptn.exception("Error: empty complex name in \"addFile_ComplexFind\"");
        return 0;
    }
    if(cFind.name.contains(",")) {
        MsgExceptn.exception("Error: complex name contains \",\" in \"addFile_ComplexFind\"");
        return 0;
    }
    if(cFind.name.startsWith("@")) {
        return addFile_ComplexFindName(cFind.name, true);
    }
    if(addFile == null || addFile.length() <=0) {
            MsgExceptn.exception("Error: empty file name in \"addFile_ComplexFind\"");
            return 0;
    }
    java.io.File afF = new java.io.File(addFile);
    if(!afF.exists()) {if(dbg) {System.out.println("   file \""+afF.getName()+"\" not found");} return 0;}
    if(!afF.canRead()) {
        showErr("Error: can not open file"+nl+"    \""+addFile+"\".",-1);
        return 0;
    }

    int lineNbr = 0;
    int fnd = 0;
    java.io.BufferedReader br = null;
    Complex cRead;
    String line = null;
    try{
        br = new java.io.BufferedReader(new java.io.FileReader(afF));
        while ((line = br.readLine()) != null){
            lineNbr++;
            if(line.length()<=0 || line.toUpperCase().startsWith("COMPLEX")) {continue;}
            cRead = Complex.fromString(line);
            if(Complex.sameNameAndStoichiometry(cFind, cRead)) {fnd++;}
        } //while
    } //try
    catch (java.io.IOException ex) {
        MsgExceptn.exception(Util.stack2string(ex));
        showErr(ex.toString()+nl+"reading line "+lineNbr+" in file:\""+addFile+"\"",0);
    }
    catch (Complex.ReadComplexException ex) {
        MsgExceptn.exception(Util.stack2string(ex));
        showErr(ex.toString()+nl+"reading line: \""+line+"\""+nl+" in file:\""+addFile+"\"",0);
    }
    finally {
        if(br != null) {
            try {br.close();}
            catch (java.io.IOException ex) {showErr(ex.toString()+nl+"with file:\""+addFile+"\"",0);}
        }
    }
    return fnd;
  } //addFile_ComplexFind
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="addFile_ComplexFindName">
/** Is the species <code>cName</code> found in file <code>addFile</code>?
 * This takes into account electrical charges. Therefore, "Fe 3+" is equal
 * to "Fe+3". Note that "I2-" has charge 1-, and "I 2-" has charge 2-.
 * Also Ca(OH)2(aq) is equal to Ca(OH)2 which is different to Ca(OH)2(s).
 * In addition CO2 and CO2(g) are different.
 * @param cName the name of a species
 * @param exactMatch if <code>false</code> names starting both with or without "@"
 * will be ok, that is, "@Na+" and "Na+" are considered equal;
 * if <code>true</code> then the first character counts, that is, "@Cl-" and "Cl-"
 * are not considered to be the same
 * @return how many times the given complex name is found; zero if not found
 * @see FrameAddData#addFile_ComplexFind addFile_ComplexFind
 */
  private int addFile_ComplexFindName(String cName, boolean exactMatch) {
    if(dbg) {System.out.println("-- addFile_ComplexFindName("+cName+", "+exactMatch+")");}
    if(cName == null) {
        MsgExceptn.exception("Error: complex name = null in \"addFile_ComplexFindName\"");
        return 0;
    }
    if(cName.length() <= 0) {
        MsgExceptn.exception("Error: empty complex name in \"addFile_ComplexFindName\"");
        return 0;
    }
    if(cName.contains(",")) {
        MsgExceptn.exception("Error: complex name contains \",\" in \"addFile_ComplexFindName\"");
        return 0;
    }
    if(!exactMatch && cName.startsWith("@")) {cName = cName.substring(1);}
    if(addFile == null || addFile.length() <=0) {
            MsgExceptn.exception("Error: empty file name in \"addFile_ComplexFindName\"");
            return 0;
    }
    java.io.File afF = new java.io.File(addFile);
    if(!afF.exists()) {if(dbg) {System.out.println("   file \""+afF.getName()+"\" not found");} return 0;}
    if(!afF.canRead()) {
        showErr("Error: can not open file"+nl+"    \""+addFile+"\".",-1);
        return 0;
    }

    int lineNbr = 0;
    int fnd = 0;
    java.io.BufferedReader br = null;
    String line = null;
    try{
        br = new java.io.BufferedReader(new java.io.FileReader(afF));
        String t;
        while ((line = br.readLine()) != null){
            lineNbr++;
            if(line.length()<=0 || line.toUpperCase().startsWith("COMPLEX")) {continue;}
            t = CSVparser.splitLine_1(line); //get the first token from the line
            if(!exactMatch && t.startsWith("@")) {t = t.substring(1);}
            if(Util.nameCompare(cName,t)) {fnd++;}
        } //while
    } //try
    catch (java.io.IOException ex) {
        MsgExceptn.exception(Util.stack2string(ex));
        showErr(ex.toString()+nl+"reading line "+lineNbr+" in file:\""+addFile+"\"",0);
    }
    catch (CSVparser.CSVdataException ex) {
        MsgExceptn.exception(Util.stack2string(ex));
        showErr(ex.toString()+nl+"reading line: \""+line+"\""+nl+" in file:\""+addFile+"\"",0);
    }
    finally {
        if(br != null) {
            try{br.close();}
            catch(java.io.IOException ex) {showErr(ex.toString()+nl+"with file:\""+addFile+"\"",0);}
        }
    }
    return fnd;
  } //addFile_ComplexFindName
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="addFile_Read(name)">
/** Read a text reaction-file and add sorted entries in "modelComplexes".
 * Erroneous lines are skipped. Reactants with zero coefficients are removed.
 * Reactions with charge imbalance are indicated with a different text colour  */
  private void addFile_Read() {
    if(dbg) {System.out.println("-- addFile_Read(..)");}

    if(addFile == null || addFile.length() <=0) {
            MsgExceptn.exception("Error: empty file name in \"addFile_Read\"");
            return;
    }
    java.io.File afF = new java.io.File(addFile);
    if(dbg) {System.out.println("Reading file \""+afF.getName()+"\"");}
    if(!afF.exists() || !afF.canRead()) {
        String msg = "Error: can not open file"+nl+"    \""+addFile+"\".";
        if(!afF.exists()) {msg = msg +nl+ "(the file does not exist).";}
        showErr(msg,0);
        return;
    }

    int cmplxNbr = 0;
    java.io.BufferedReader br = null;
    try{
        br = new java.io.BufferedReader(new java.io.FileReader(afF));
        String line;
        Complex c;
        java.util.ArrayList<String> items = new java.util.ArrayList<String>();
        while ((line = br.readLine()) != null){
            cmplxNbr++;
            if(line.trim().length()<=0 || line.toUpperCase().startsWith("COMPLEX")) {continue;}
            try{c = Complex.fromString(line);}
            catch (Complex.ReadComplexException ex) {
                showErr(ex.getMessage()+nl+"Line discarded!",0);
                continue;
            }
            if(c == null) {
                showErr("Error: empty reaction product in line"+nl+"   "+line+nl+"Line discarded!",0);
                continue;
            }
            if(!c.name.startsWith("@")) {
                for(int j=0; j < Complex.NDIM; j++) {
                  if(((c.component[j] != null && c.component[j].length()>0) && Math.abs(c.numcomp[j]) < 0.001)
                      || (Math.abs(c.numcomp[j]) >= 0.001) && (c.component[j] == null || c.component[j].length() <=0)) {
                        c.component[j] ="";
                        c.numcomp[j] = 0;
                  }
                } //for j
            } //if not starts with "@"
            items.add(c.toString());
        } //while
        //java.util.Collections.sort(items, String.CASE_INSENSITIVE_ORDER);
        java.util.Iterator<String> iter = items.iterator();
        while(iter.hasNext()) {
            String cLine = iter.next();
            try{c = Complex.fromString(cLine);}
            catch (Complex.ReadComplexException ex) {c = null; MsgExceptn.exception(ex.getMessage());}
            if(c == null) {continue;}
            //Complex.sortReactants(c);
            final ModelComplexesItem o;
            if(Complex.isChargeBalanced(c)) {
                o = new ModelComplexesItem(cLine, java.awt.Color.BLACK);
            } else {
                o = new ModelComplexesItem(cLine, vermilion);
            }
            javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
                modelComplexes.addElement(o);
            }}); //invokeLater(Runnable)
        }
    } catch (java.io.IOException ex) {showErr(ex.toString()+nl+"reading line "+cmplxNbr,0);}
    finally {
        try{if(br != null) {br.close();}}
        catch(java.io.IOException ex) {showErr(ex.toString(),0);}
    }
    //return;
  } //addFile_Read(fileName)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="addFile_SaveReaction()">
 /** Save the reaction displayed in "jPanelReaction" to file "addFile" */
  private void addFile_SaveReaction() {
    if(dbg) {System.out.println("-- addFile_SaveReaction()");}
    // ---- make some checks
    if(!jPanelReaction.isShowing()) {
        MsgExceptn.exception("Programming error: jPanelReaction is not showing in \"AddFile_SaveReaction()\".");
    }
    if(newC == null || newC.name == null) {
        MsgExceptn.exception("Programming error: empty \"newC\" in \"AddFile_SaveReaction()\".");
    }
    // -- make some checks
    if(jTextFieldComplex.getText().length() <=0 || newC.name.length() <=0) {
        showErr("A name is needed for the complex!",-1);
        return;
    }
    String msg = "";
    if(!newC.name.startsWith("@")) {
        boolean ok = false;
        for(int i=0; i < Complex.NDIM; i++) {
            if(newC.component[i] != null && newC.component[i].length() >0 &&
                    Math.abs(newC.numcomp[i]) >= 0.001) {ok = true; break;}
        } //for i
        if(!ok) {
            showErr("At least one reactant is needed in the reaction"+nl+
                "for the formation of the complex \""+newC.name+"\"",-1);
            return;
        }
        for(int i=0; i < Complex.NDIM; i++) {
            if(newC.component[i] != null && newC.component[i].length() >0 &&
                    Math.abs(newC.numcomp[i]) < 0.001) {
                showErr("The stoichiometric coefficient"+nl+
                        "for reactant \""+newC.component[i]+"\" may not be zero!"+nl+nl+
                        "Either enter a number or select"+nl+
                        "the \"empty\" reactant instead of \""+newC.component[i]+"\".",-1);
                return;
            }
        } //for i
        if(jTextFieldLogK.getText().length() <=0) {
            showErr("Please: enter a value for the equilibrium constant"+nl+
                    "for the complex \""+newC.name+"\"",-1);
            return;
        }
        if(!Complex.isChargeBalanced(newC)) {
            noChargeBalanceWarning(newC.name);
            return;
        }
    } //does not start with "@"
    else
    { //name starts with "@"
        msg = "The species name begins with \"@\"."+nl+nl+"This will exclude the ";
        if(Util.isSolid(newC.name)) {msg = msg + "solid";}
        else if(Util.isGas(newC.name)) {msg = msg + "gas";}
        else {msg = msg + "aqueous species";}
        msg = msg + " \""+newC.name.substring(1) +"\""+nl+"when searching the databases."+nl+nl;
    } //name starts with "@"

    // ---- ask for confirmation
    boolean replaceComplex;
    if(addFile_ComplexFindName(newC.name,false) > 0) {
        msg = msg + "Replace existing reaction for \""+
              newC.name+"\""+nl+
              "with"+nl+
              Complex.reactionTextWithLogK(newC,25);
        replaceComplex = true;
    } else {
        msg = msg + "Add reaction?"+nl+
              Complex.reactionTextWithLogK(newC,25)+nl+" ";
        replaceComplex = false;
    }
    Object[] opt = {"Yes", "Cancel"};
    int m = javax.swing.JOptionPane.showOptionDialog(this, msg,
                pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
    if(m != javax.swing.JOptionPane.YES_OPTION) {return;}

    // ---- user confirmed and no errors: go ahead
    update_newC();

    if(replaceComplex) { // the file already exists
        try {addFile_Initialise(dbg, addFile);}
        catch (AddDataFrameInternalException ex) {
            showErr(ex.toString(),0);
            return;
        }
        // To replace a reaction:  read the file, write a temp file excluding
        // the replaced complex, delete the original file, rename the temp file
        // to the original name. Then append the changed reaction at the end of the new file.
        try {addFile_ComplexDelete(newC);}
        catch (AddDataFrameInternalException ex) {
            showErr(ex.toString(),0);
            return;
        }
    }
    try {addFile_ComplexAppend(dbg, addFile, newC);}
    catch (AddDataFrameInternalException ex) {
        showErr(ex.toString(),0);
        return;
    }

    newC = new Complex();
    try {oldNewC = (Complex)newC.clone();}
    catch (CloneNotSupportedException ex) {MsgExceptn.exception("Error "+ex.toString()+nl+"  in Complex.clone().");}
    updateJPanelReaction(newC);
    jLabelReaction.setText("New reaction:");
    jMenuFileSave.setEnabled(false);
    jButtonSaveReac.setEnabled(false);
    jButtonSaveComp.setEnabled(false);

    // create a list of components if it does not exist
    try{
        AddDataElem.elemCompAdd_Update(dbg, this,
                addFile, addFileEle, pd.elemComp, elemCompAdd);
    } catch (AddDataElem.AddDataException ex) {
        showErr(ex.toString(),0);
        return;
    }
    // update the element file
    // save changes made in "elemCompAdd" in file "addFileEle"
    try {AddDataElem.addFileEle_Write(dbg, addFileEle, elemCompAdd);}
    catch (AddDataElem.AddDataException ex) {
        showErr(ex.toString(),0);
        return;
    }

    // -- add this database file to the list?
    addAddFileToList();
    //return;
  } //addFile_SaveReaction()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="addAddFileToList()">
  private void addAddFileToList() {
    //--- check that the name is not already in the list
    boolean found = false;
        int n = pd.dataBasesList.size();
        for(int j=0; j<n; j++) {
            if(pd.dataBasesList.get(j).equalsIgnoreCase(addFile)) {
                found = true; break;
            }
        }
        //--- add new name to list
        if(!found) {
            java.io.File f = new java.io.File(addFile);
            Object[] opt = {"OK", "No, thanks"};
            int m = javax.swing.JOptionPane.showOptionDialog(this,
                    "The file \""+f.getName()+"\""+nl+
                    "will be added to the list"+nl+
                    "of available databases",
                    pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.QUESTION_MESSAGE, null, opt, opt[0]);
            if(m != javax.swing.JOptionPane.YES_OPTION) {return;}
            if(pc.dbg) {System.out.println("---- Adding database: \""+addFile+"\"");}
            pd.dataBasesList.add(addFile);
        }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="componentSave()">
/** Add a component to file "addFileEle" (re-writes the file) */
  private void componentSave() {
    if(dbg) {System.out.println("-- componentSave()");}
    // ---- make some checks
    if(!jPanelComponent.isShowing()) {
        MsgExceptn.exception("Programming error: jPanelComponent is not showing in \"componentSave()\".");
        return;
    }
    if(jTextFieldCompName.getText() == null || jTextFieldCompName.getText().trim().length() <=0) {return;}
    String newComp = jTextFieldCompName.getText().trim();
    if(jLabelLinked.getText().length() <=10 || !jLabelLinked.getText().startsWith("Linked to:")) {
        showErr("Please: link component \""+newComp+"\""+nl+"to some element(s)",-1);
        return;
    }

    String linkedTo = jLabelLinked.getText().substring(10).trim();
    String descr = jTextFieldCompDescr.getText().trim();
    boolean ok;
    try{
        ok = AddDataElem.addFileEle_ComponentSave(dbg, this,
                newComp, linkedTo, descr,
                addFile, addFileEle, pd.elemComp, elemCompAdd);
    } catch (AddDataElem.AddDataException ex) {
        showErr(ex.toString(),0);
        ok = false;
    }
    if(!ok) {return;}

    // - - no problems: the end
    jMenuFileSave.setEnabled(false);
    jButtonSaveReac.setEnabled(false);
    jButtonSaveComp.setEnabled(false);
    //  focus on "C"
    for(int i=0; i < jComboBoxElems.getItemCount(); i++) {
        if(jComboBoxElems.getItemAt(i).equals("C")) {
            jComboBoxElems.setSelectedIndex(i);
            break;
        }
    }
    oldComponentName = "";
    oldComponentDescr = "";
    oldComponentLinked = "";
    jLabelLinked.setText("");
    jTextFieldCompName.setText("");
    jTextFieldCompDescr.setText("");
    jLabelComp.setText("New component:");
    jButtonLink.setText("Link component to element");
    // -- add this database file to the list?
    addAddFileToList();
    // return;
  } //componentSave()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="discardChanges()">
/** Ask the user if to discard changes or to continue
 * @return true if the user wants to discard changes; false otherwise */
  private boolean discardChanges() {
    bringToFront();
    String msg = "Save changes?"+nl+nl+
                 "Press [Cancel] and then [Save] to keep changes;"+nl+
                 "or choose [Discard] to reject changes.";
    Object[] opt = {"Discard", "Cancel"};
    int m = javax.swing.JOptionPane.showOptionDialog(this,msg,
                        "Add Data to "+pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
    if(m != javax.swing.JOptionPane.YES_OPTION) {
        return false; //Cancel = do Not discard changes
    }
    jMenuFileSave.setEnabled(false);
    jButtonSaveReac.setEnabled(false);
    jButtonSaveComp.setEnabled(false);
    return true; // OK/Yes = discard changes
  } //discardChanges()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="getAddFileName">
 /** Get a database file name from the uer. If it is an existing file, check
  * that it is ok.
  * @return false if there is an error or if the user cancels the process */
  private boolean getAddFileName() {
    if(dbg) {System.out.println("-- getAddFileName()");}
    // -- ask for the file name
    boolean fileMustExist = false;
    System.out.println("querying for a file name...");
    String newFile = Util.getOpenFileName(this, pc.progName, fileMustExist,
              "Open or Create a file:  Enter a file name", 3, "New-Data.txt", pd.pathAddData.toString());
    if(newFile == null || newFile.length() <= 0) {
        if(dbg) {System.out.println("No \"add\" file name given.");}
        return false;
    }
    // -- get full path and get a temporary file name
    // save variables in case the user cancels or there is an error
    String f01 = addFile;
    String f02 = addFileTmp;
    addFile = newFile;
    java.io.File f = new java.io.File(addFile);
    String fn;
    try {fn = f.getCanonicalPath();} catch (java.io.IOException ex) {fn = null;}
    if (fn == null) {try {fn = f.getAbsolutePath();} catch (Exception ex) {fn = f.getPath();}}
    f = new java.io.File(fn);
    pd.pathAddData.replace(0, pd.pathAddData.length(), f.getParent());
    String dir = pd.pathAddData.toString();
    if(dir.endsWith(SLASH)) {dir = dir.substring(0, dir.length()-1);}
    addFile = dir + SLASH + f.getName();
    String ext = Div.getFileNameExtension(addFile);
    if(ext == null || ext.length() <=0) {ext = "txt";}
    addFileTmp = Div.getFileNameWithoutExtension(addFile)+"-"+ext+".tmp";

    // -- element file name
    String f03 = addFileEle;
    // if reactants (components) are not found in the element file,
    // they will be added if found in "pd.elemComp"
    addFileEle = AddDataElem.elemFileCheck(dbg, this, addFile, pd.elemComp);
    if(addFileEle == null) {
        if(dbg) {System.out.println("AddDataElem.elemFileCheck("+f.getName()+") returns \"null\".");}
        addFile = f01; addFileTmp = f02; addFileEle = f03;
        return false;
    }
    // -- check the add file
    if(f.exists()) {
        // --- check the database for errors
        java.util.ArrayList<String> arrayList = new java.util.ArrayList<String>();
        arrayList.add(addFile);
        CheckDatabases.CheckDataBasesLists lists = new CheckDatabases.CheckDataBasesLists();
        CheckDatabases.checkDatabases(pc.dbg, this, arrayList, null, lists);
        boolean ok = CheckDatabases.displayDatabaseErrors(pc.dbg, this, pc.progName, f.getName(), lists);
        if(!ok) {
            if(dbg) {System.out.println("--- displayDatabaseErrors: NOT OK for file: "+addFile);}
            addFile = f01; addFileTmp = f02; addFileEle = f03;
            return false;
        }
    } //if f.exists()

    System.out.println("add-file = "+addFile);
    jTextAreaFiles.setText(addFile+nl+addFileEle);
    return true;
  } //getAddFileName()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="isCharOKforNumberInput">
  /** @param key a character
   * @return true if the character is ok, that is, it is either a number,
   * or a dot, or a minus sign, or an "E" (such as in "2.5e-6") */
  private boolean isCharOKforNumberInput(char key) {
        return Character.isDigit(key)
                || key == '-' || key == '+' || key == '.' || key == 'E' || key == 'e';
  } // isCharOKforNumberInput(char)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="isComponentNeeded(reactant)">
 /** is a component (reactant) found among the reactions listed in jListReact
  * (that is, in file addFile)?
  * @param reactant the component's name
  * @return true if <code>reactant</code> is not null and not empty and it is found
  * in the reactions listed in <code>modelComplexes</code>; false otherwise */
  private boolean isComponentNeeded(String reactant) {
    if(dbg) {System.out.println("-- isComponentNeeded("+reactant+")");}
    if(reactant == null || reactant.length() <=0) {return false;}
    String line = null;
    for(int i=0; i < modelComplexes.size(); i++) {
      Object o = modelComplexes.get(i);
      if(o != null) {line = o.toString();}
      if(line != null && line.length() >0) {
          Complex c = null;
          try{c =  Complex.fromString(line);}
          catch (Complex.ReadComplexException ex) {MsgExceptn.exception(ex.getMessage());}
          if(c != null && c.name != null && c.name.length() >0) {
            for(int j=0; j < Complex.NDIM; j++) {
              if(c.component[j] == null || c.component[j].length() <=0
                        || Math.abs(c.numcomp[j]) < 0.001) {continue;}
              if(reactant.equals(c.component[j])) {return true;}
            }//for j
          }
      } //if line != null
    } //for i in modelComplexes
    return false;
  } //isComponentNeeded(reactant)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="jListComps_click()">
  private void jListComps_click() {
    if(dbg) {System.out.println("-- jListComps_click()");}
    int i = jListComps.getSelectedIndex();
    if(i >= 0 && i < modelComponents.getSize()) {
        String dataLine = modelComponents.get(i).toString();
        if(dataLine != null && dataLine.length() >0) {
            jMenuAddComp.doClick();
            try{ //CSVparser.splitLine will remove enclosing quotes
                java.util.ArrayList<String> aL = CSVparser.splitLine_N(dataLine, 3);
                jTextFieldCompName.setText(aL.get(0));
                jTextFieldCompDescr.setText(aL.get(1));
                if(aL.get(2).length() >9 && aL.get(2).toLowerCase().startsWith("linked to")) {
                    aL.set(2, "Linked to"+aL.get(2).substring(9));
                    jLabelLinked.setText(aL.get(2));
                } else {
                    jLabelLinked.setText("");
                    jMenuFileSave.setEnabled(true);
                    jButtonSaveReac.setEnabled(true);
                    jButtonSaveComp.setEnabled(true);
                }
                oldComponentName = jTextFieldCompName.getText();
                oldComponentDescr = jTextFieldCompDescr.getText();
                oldComponentLinked = jLabelLinked.getText();
                jLabelComp.setText("Edit component:");
            } catch (CSVparser.CSVdataException ex) {
                showErr(ex.toString(),0);
            }
        }
    }
  } //jListComps_click()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="jListReact_click()">
  private void jListReact_click() {
    if(dbg) {System.out.println("-- jListReact_click()");}
    int i = jListReact.getSelectedIndex();
    if(i >= 0 && i < modelComplexes.getSize()) {
        String dataLine = null;
        Object value = modelComplexes.get(i);
        if(value != null) {
            dataLine = value.toString();
        } //value != null
        if(dataLine != null && dataLine.length() >0) {
            Complex c = null;
            try{c = Complex.fromString(dataLine);}
            catch (Complex.ReadComplexException ex) {MsgExceptn.exception(ex.getMessage());}
            if(c !=  null) {
            //check that all reactants for this reaction are in the list of available components
            boolean fnd;
            for(int j=0; j < Complex.NDIM; j++) {
                if(c.component[j] == null || c.component[j].length() <=0
                        || Math.abs(c.numcomp[j]) < 0.001) {continue;}
                fnd = false;
                for(String t : componentsAdd) {
                    if(t.equals(c.component[j])) {fnd = true; break;}
                } //for
                if(!fnd) {
                    java.io.File eF = new java.io.File(addFileEle);
                    showErr("Error:  component \""+c.component[j]+"\""+nl+
                            "for species \""+c.name+"\""+nl+
                            "can not be found."+nl+nl+
                            "You must save the component first.",-1);
                    jMenuAddComp.doClick();
                    try{
                        jTextFieldCompName.setText(c.component[j]);
                        jTextFieldCompDescr.setText("");
                        jLabelLinked.setText("");
                        oldComponentName = "";
                        oldComponentDescr = "";
                        oldComponentLinked = "";
                        jLabelComp.setText("Edit component:");
                        jMenuFileSave.setEnabled(true);
                        jButtonSaveReac.setEnabled(true);
                        jButtonSaveReac.setEnabled(true);
                    } catch (Exception ex) {MsgExceptn.exception(ex.toString());}
                    return;
                }//if !fnd
            }//for j
            jMenuAddReact.doClick();
            newC = c;
            try{oldNewC = (Complex)newC.clone();}
            catch (CloneNotSupportedException ex) {MsgExceptn.exception("Error "+ex.toString()+nl+"  in Complex.clone().");}
            jLabelReaction.setText("Edit reaction:");
            updateJPanelReaction(newC);
            } // if c != null
        } //if dataLine != null
    } else {jMenuAddReact.doClick();}
  } //jListReact_click()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="jListReact_Del()">
  private void jListReact_Del() {
    if(dbg) {System.out.println("-- jListReact_Del()");}
    int i = jListReact.getSelectedIndex();
    newC = null;
    if(i >= 0 && i < modelComplexes.getSize()) {
        String dataLine = null;
        Object value = modelComplexes.get(i);
        if(value != null) {dataLine = value.toString();}
        if(dataLine != null && dataLine.length() >0) {
            try{newC = Complex.fromString(dataLine);}
            catch (Complex.ReadComplexException ex) {MsgExceptn.exception(ex.getMessage());}
        }
        if(newC == null || newC.name.length() <=0) {
            showErr("Error: empty reaction",0);
            return;
        }
        String cName;
        if(newC.name.startsWith("@")) {cName = newC.name.substring(1);} else {cName = newC.name;}
        String msg = "Delete  "+cName+" ?";
        int n = addFile_ComplexFindName(cName, false);
        Object[] opt = {"Yes", "Cancel"};
        int m = javax.swing.JOptionPane.showOptionDialog(this, msg,
                    pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
        if(m != javax.swing.JOptionPane.YES_OPTION) {return;}
        try{addFile_ComplexDelete(newC);}
        catch (AddDataFrameInternalException ex) {showErr(ex.toString(),0);}
        // -- remove components not needed eanymore
        removeUnusedComps();
    }
    // -- update the window contents
    jMenuAddShow.setEnabled(true);
    jMenuAddShow.doClick();
  } //jListReact_Del()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="jListComps_Del()">
  private void jListComps_Del() {
    if(dbg) {System.out.println("-- jListComps_Del()");}
    int i = jListComps.getSelectedIndex();
    if(i < 0 || i >= modelComponents.getSize()) {return;}
    //String dataLine = modelComponents.get(i).toString();
    String dataLine = modelComponents.get(i).toString();
    if(dataLine == null || dataLine.length() <=0) {return;}
    java.util.ArrayList<String> aL;
    try{
        aL = CSVparser.splitLine_N(dataLine, 3);
        if(aL.get(2).length() >10 && aL.get(2).toLowerCase().startsWith("linked to:")) {
            aL.set(2, "Linked to:"+aL.get(2).substring(10));
        }
    } catch (CSVparser.CSVdataException ex) {
        showErr(ex.toString(),0);
        return;
    }
    boolean needed = false;
    try{
        needed = isComponentNeeded(aL.get(0));
    } catch (Exception ex) {}
    if(needed) {
        showErr("It is not possible to delete"+nl+"   "+aL.get(0)+nl+
                "because it is used in"+nl+"one or more reactions.",2);
        return;
    }
    Object[] opt = {"Yes", "Cancel"};
    int m = javax.swing.JOptionPane.showOptionDialog(this, "Delete  "+aL.get(0)+" ?",
                    pc.progName, javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
    if(m != javax.swing.JOptionPane.YES_OPTION) {return;}

    if(aL.get(2).length() <=10 || !aL.get(2).startsWith("Linked to:")) {
        System.out.println("Warning - deleting component \""+aL.get(0)+"\":"+nl+
              "    but it is not linked to any chemical element");
    }

    try{AddDataElem.addFileEle_ComponentDelete(dbg,aL.get(0),addFileEle,elemCompAdd);}
    catch (AddDataElem.AddDataException ex) {
        showErr(ex.toString(),0);
    }
    // the list elemCompAdd has been updated, but componentsAdd not
    for(i=0;i<componentsAdd.size();i++) {
        if(componentsAdd.get(i).equals(aL.get(0))) {
            componentsAdd.remove(i);
            break;  // assume there are no duplicate entries
        }
    } //for i
    // -- update window frame
    jMenuAddShow.setEnabled(true);
    jMenuAddShow.doClick();
  } //jListComps_Del()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="modelComps_update()">
/** Sort the data in "eleCmpAdd" and add the sorted lines to "modelComponentsLocal"
 * @param dbg if true output some information to System.out
 * @param eleCmpAdd (see <code>elemCompAdd</code> below)
 * @param modelComponentsLocal
 * @see FrameAddData#elemCompAdd elemCompAdd
 * @see LibDB#readElemFileText(java.io.File, java.util.ArrayList) readElemFileText
 * @see AddDataElem#elemCompAdd_Update(boolean, java.awt.Component, java.lang.String, java.lang.String, java.util.ArrayList, java.util.ArrayList) elemCompAdd_Update */
  private static void modelComps_update(boolean dbg,
          java.util.ArrayList<String[]> eleCmpAdd,
          final javax.swing.DefaultListModel<String> modelComponentsLocal) { // final javax.swing.DefaultListModel modelComponentsLocal) { // java 1.6
    if(dbg) {System.out.println("-- modelComps_update(..)");}
    if(modelComponentsLocal == null) {
            MsgExceptn.exception("Programming error: model = null in \"modelComps_update\"");
            return;
    }
    if(eleCmpAdd == null) {
            MsgExceptn.exception("Programming error: eleCmpAdd = null in \"modelComps_update\"");
            return;
    }
    if(eleCmpAdd.size() <= 0) {
            if(dbg) {System.out.println("Empty eleCmpAdd in \"modelComps_update\"");}  return;
    }
    javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {modelComponentsLocal.clear();}});
    java.util.ArrayList<String> items = new java.util.ArrayList<String>(eleCmpAdd.size());
    String compName;
    boolean fnd;
    for(int i = 0; i < eleCmpAdd.size(); i++) {
        compName = Complex.encloseInQuotes(eleCmpAdd.get(i)[1]);
        fnd = false;
        if(!items.isEmpty()) {
            for(int k =0; k < items.size(); k++) {
                if(items.get(k).startsWith(compName)) {
                    fnd = true;
                    String n = items.get(k)+", "+Complex.encloseInQuotes(eleCmpAdd.get(i)[0]);
                    items.set(k, n);
                    break;
                }
            }//for k
        }//if items !empty
        if(!fnd) {
            if(eleCmpAdd.get(i)[2].length() >0) {
                compName = compName + "; " + Complex.encloseInQuotes(eleCmpAdd.get(i)[2].trim());
            } else { //no description
                compName = compName + ";";
            }
            if(!eleCmpAdd.get(i)[0].equals("XX")) {
                compName = compName + "; linked to: " + Complex.encloseInQuotes(eleCmpAdd.get(i)[0]);
            } else {
                compName = compName + ";";
            }
            items.add(compName);
        } //if !fnd
    }//for i
    // sort the list alphabetically and update the "model"
    java.util.Collections.sort(items,String.CASE_INSENSITIVE_ORDER);
    java.util.Iterator<String> iter = items.iterator();
    while(iter.hasNext()) {
        final String o = iter.next();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
            modelComponentsLocal.addElement(o);
        }}); //invokeLater(Runnable)
    } //while
    // return;
  } //modelComps_update
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="rearrangeReaction">
/** Move reactants towards the upper left corner, so that there is no empty
 * space between reactants */
  private void rearrangeReaction() {
    if(dbg) {System.out.println("-- rearrangeReaction()");}
    // this method does nothing unless
    //  -the user creates a "hole" by emptying a combo box in the middle of the reaction, or  
    //  -a new reactant is added in a combo box towards the end of the list, leaving empty
    //   empty space between
    // if so, the reactants are moved so tha empty space is at the end
    //
    // -- keep track of focus owner (fo)
    javax.swing.JComboBox<String> jcb_i, jcb_j; // javax.swing.JComboBox jcb_i, jcb_j; // java 1.6
    int fo = -1;
    for(int i=0; i < boxes.size(); i++) {
        jcb_i = boxes.get(i);
        if(jcb_i.isFocusOwner()) {fo = i; break;}
    }
    boolean good = true;
    do {
      loop_i:
      for(int i=0; i < boxes.size(); i++) {
        jcb_i = boxes.get(i);
        // is this an empty reactant?
        if(jcb_i.getSelectedIndex() <=0 || texts[i].getText().length() <=0) {
          good = true;
          for(int j=i+1; j<boxes.size(); j++) {
             jcb_j = boxes.get(j);
             // is this reactant not empty?
             if(jcb_j.getSelectedIndex() >0 && texts[j].getText().length() >0) {
               rearranging = true;
               jcb_i.setSelectedIndex(jcb_j.getSelectedIndex());
               texts[i].setText(texts[j].getText());
               if(fo >=0) {fo = Math.min(fo, i);} //change the focus owner?
               jcb_j.setSelectedIndex(0);
               texts[j].setText("");
               rearranging = false;
               good = false;  break loop_i; //start all over
             }//if j-box not empty
             boxes.set(j, jcb_j);
          }//for j
        }//if i-box empty
        boxes.set(i, jcb_i);
      }//for i
    } while (!good);
    if(fo < 0) {return;}
    //set focus to the new focus owner
    if(fo >=0) {
        jcb_i = boxes.get(fo);
        jcb_i.requestFocusInWindow();
        boxes.set(fo, jcb_i);
    }
  } //rearrangeReaction()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="removeUnusedComps()">
 /** Check that all reactants in "elemCompAdd" are used in "addFile";
  * and if not ask the user if they should be removed. */
  private synchronized void removeUnusedComps() {
    // ---- remove from "elemCompAdd" any components that are not
    //      present in any of the reactions in file "addFile"
    if(dbg) {System.out.println("-- removeUnusedComps()");}
    if(addFile == null || addFile.length() <=0) {
            MsgExceptn.exception("Error: empty file name in \"addFile_Read\"");
            return;
    }
    java.io.File afF = new java.io.File(addFile);
    if(dbg) {System.out.println("   reading file \""+afF.getName()+"\"");}
    if(!afF.exists()) {
        if(dbg) {System.out.println("  file:  \""+afF.getName()+"\"  does not exist");}
        return;
    }
    if(!afF.canRead()) {
        showErr("Error: can not open file"+nl+"    \""+addFile+"\".",-1);
        return;
    }

    int lineNbr = 0;
    java.io.BufferedReader br = null;
    // ---- Array "found" keeps track of which reactants in "elemCompAdd" are found in "addFile"
    //      The two lists, "found" and "elemCompAdd", need to be synchronized
    boolean[] found = new boolean[elemCompAdd.size()];
    for(int i=0; i < elemCompAdd.size(); i++) {
        found[i] = (elemCompAdd.get(i)[1].equals("H+") || elemCompAdd.get(i)[1].equals("e-")
                || elemCompAdd.get(i)[1].equals("H2O"));
    }
    try{
        br = new java.io.BufferedReader(new java.io.FileReader(afF));
        String line;
        Complex c;
        while ((line = br.readLine()) != null){
            lineNbr++;
            if(line.trim().length()<=0 || line.toUpperCase().startsWith("COMPLEX")) {continue;}
            try{c = Complex.fromString(line);}
            catch (Complex.ReadComplexException ex) {MsgExceptn.msg(ex.getMessage()); continue;}
            if(c == null) {
                MsgExceptn.msg("Error reading reaction in line:"+nl+"   "+line);
                continue;
            }
            if(c.name.startsWith("@")) {continue;}
            for(int j=0; j < Complex.NDIM; j++) {
                if(c.component[j] != null && c.component[j].length()<=0) {c.numcomp[j] = 0; continue;}
                if(Math.abs(c.numcomp[j]) < 0.001) {c.component[j] = ""; continue;}
                // is this reactant in the list?
                for(int i = 0; i < elemCompAdd.size(); i++) {
                    if(c.component[j].equals(elemCompAdd.get(i)[1])) {
                        found[i] = true;
                        //can not break: one must go through all "elemCompAdd" because
                        // some reactants will be listed with two elements
                        // (CN- will occur twice under "N" and "C")
                        //break; 
                    }
                } //for i
            } //for j
        } //while
    } //try
    catch (java.io.IOException ex) {
        MsgExceptn.msg(ex.getMessage()+nl+"reading line "+lineNbr);
    }
    finally {
        try{if(br != null) {br.close();}} catch(java.io.IOException ex) {showErr(ex.toString(),0);}
    }
    //----------------------------
    //-- remove unused components?
    javax.swing.DefaultListModel<String> aModel = new javax.swing.DefaultListModel<>();
    // javax.swing.DefaultListModel aModel = new javax.swing.DefaultListModel(); // java 1.6
    boolean there;
    for(int i =0; i < elemCompAdd.size(); i++) {
      if(!found[i]) {
          there = false;
          for(int j =0; j < aModel.size(); j++) {
              if(aModel.get(j).equals(elemCompAdd.get(i)[1])) {there = true; break;}
          } //for j
          if(!there) {aModel.addElement(elemCompAdd.get(i)[1]);}
      } //if !found
    }//for i
    if(aModel.size() <= 0) {return;}
    java.io.File afE = new java.io.File(addFileEle);
    String msg = "<html>The following reactant";
    if(aModel.size() > 1) {msg = msg+"s";}
    msg = msg+"<br>in file \""+afE.getName()+"\"<br>";
    if(aModel.size() > 1) {msg = msg+"are";} else {msg = msg+"is";}
    msg = msg+" not used. &nbsp; Remove ";
    if(aModel.size() > 1) {msg = msg+"them";} else {msg = msg+"it";}
    msg = msg+"?<br>&nbsp;</html>";
    if(!CheckDatabases.showListDialog(this, pc.progName, msg, "Remove", "Keep", aModel)) {return;}
    if(dbg) {System.out.println("  removing unused components");}
    int i = elemCompAdd.size() -1;
    while(true) {
      for(int j=0; j < aModel.size(); j++) {
          if(aModel.get(j).equals(elemCompAdd.get(i)[1])) {elemCompAdd.remove(i); break;}
      }
      i--;
      if(i <= 0) {break;}
    } //while
    //-- save changes
    try {AddDataElem.addFileEle_Write(dbg, addFileEle, elemCompAdd);}
    catch (AddDataElem.AddDataException ex) {showErr(ex.toString(),0);}
    //return;
  } //removeUnusedComps()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="update_componentsAdd()">
 /** All known reactants are added into the array list "componentsAdd". */
  private void update_componentsAdd() {
    if(dbg) {System.out.println("-- update_componentsAdd()");}
    // make a local copy of the database-list
    java.util.ArrayList<String> dataBasesListLocal = new java.util.ArrayList<String>(pd.dataBasesList);
    // include "addFile" at the end of the list
    // (check that the name is not already in the list)
    int n = dataBasesListLocal.size();
    boolean found = false;
    for(int j=0; j<n; j++) {
        if(dataBasesListLocal.get(j).equalsIgnoreCase(addFile)) {found = true;  break;}
    }
    if(!found) {dataBasesListLocal.add(addFile);}
    // read components-elements
    java.util.ArrayList<String[]> arrL = new java.util.ArrayList<String[]>();
    LibDB.getElements(this, pc.dbg, dataBasesListLocal, arrL);
    componentsAdd.clear();
    for(int i = 0; i < arrL.size(); i++) {
        componentsAdd.add(arrL.get(i)[1]);
    }
    //return;
  } //update_componentsAdd()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="updateJPanelReaction(Complex)">
/** Show a Complex data in the jPanelReaction */
  private void updateJPanelReaction(Complex c) {
    if(dbg) {System.out.println("-- updateJPanelReaction("+c.name+")");}
    editingReaction = true;
    // --- update the combo boxes
    update_componentsAdd();
    for(int i=0; i<boxes.size();i++){
        java.util.Collections.sort(componentsAdd, String.CASE_INSENSITIVE_ORDER);
        javax.swing.JComboBox<String> jcb = boxes.get(i); // javax.swing.JComboBox jcb = boxes.get(i); // java 1.6
        jcb.removeAllItems();
        jcb.addItem("");
        jcb.addItem("H+");
        jcb.addItem("e-");
        jcb.addItem("H2O");
        for(String t : componentsAdd) {
            if(!t.equals("H+") && !t.equals("e-") && !t.equals("H2O")) {
                jcb.addItem(t);
            }
        } //for
        boxes.set(i, jcb);
    }
    for(javax.swing.JTextField tf : texts) {tf.setText("");}
    // --- display the complex in the pannel
    jTextFieldComplex.setText(c.name);
    if(c.constant !=  Complex.EMPTY) {
        jTextFieldLogK.setText(Util.formatNumAsInt(c.constant));
    } else {jTextFieldLogK.setText("");}
    if(c.deltH !=  Complex.EMPTY) {
        jTextFieldDeltH.setText(Util.formatNumAsInt(c.deltH));
    } else {jTextFieldDeltH.setText("");}
    if(c.deltCp !=  Complex.EMPTY) {
        jTextFieldDeltCp.setText(Util.formatNumAsInt(c.deltCp));
    } else {jTextFieldDeltCp.setText("");}    
    jTextFieldComment.setText(c.comment);
    jTextFieldRef.setText(c.reference);
    int fnd;
    for(int i = 0; i < Complex.NDIM; i++) {
        javax.swing.JComboBox<String> jcb = boxes.get(i); // javax.swing.JComboBox jcb = boxes.get(i); // java 1.6
        if(c.component[i] != null && c.component[i].length() >0) {
            fnd = -1;
            for(int j=0; j < jcb.getItemCount(); j++) {
                if(c.component[i].equals(jcb.getItemAt(j))) {fnd = j; break;}
            } //for j
            if(fnd > -1) {
                jcb.setSelectedIndex(fnd);
                if(Math.abs(c.numcomp[i]) >= 0.001) {
                    texts[i].setText(Util.formatNumAsInt(c.numcomp[i]));
                } else {texts[i].setText("1");}
            } else {
                MsgExceptn.exception("Programming error? Component "+c.component[i]+nl+"not found in combo box");
                jcb.setSelectedIndex(0);
                texts[i].setText("");
            }//if fnd
        } else {
            jcb.setSelectedIndex(0);
            texts[i].setText("");
        }
        boxes.set(i, jcb);
    } //for i
    if(!Complex.isChargeBalanced(c)) {
        jLabelCharge.setText("<html>Reaction is <b>NOT</b> charge balanced</html>");
        jLabelCharge.setForeground(vermilion);
        jMenuFileSave.setEnabled(true);
        jButtonSaveReac.setEnabled(true);
        jButtonSaveComp.setEnabled(true);
    } else {
        jLabelCharge.setText("<html>Reaction is charge balanced</html>");
        jLabelCharge.setForeground(java.awt.Color.BLACK);
    }
    editingReaction = false;

    rearrangeReaction();

  } //updateJPanelReaction(c)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="update_newC()">
/** Reads the contents of "jPanelReaction" and stores it in complex "newC".
 * If a change is detected the [Save] button is enabled */
  private synchronized void update_newC() {
    //if(dbg) {System.out.println("-- update_newC()");}
    if(loading || editingReaction || !jPanelReaction.isShowing()) {return;}
    if(newC == null) {newC = new Complex();}
    newC.name = jTextFieldComplex.getText();
    if(!jTextFieldLogK.getText().equals("")) {
        try {newC.constant = Double.parseDouble(jTextFieldLogK.getText());}
        catch (NumberFormatException ex) {
            //System.err.println("Error reading double from \""+jTextFieldLogK.getText()+"\"");
            newC.constant = Complex.EMPTY;
        }
    } else {newC.constant = Complex.EMPTY;}
    if(!jTextFieldDeltH.getText().equals("")) {
        try {newC.deltH = Double.parseDouble(jTextFieldDeltH.getText());}
        catch (NumberFormatException ex) {
            //System.err.println("Error reading double from \""+jTextFieldLogK.getText()+"\"");
            newC.deltH = Complex.EMPTY;
        }
    } else {newC.deltH = Complex.EMPTY;}
    if(!jTextFieldDeltCp.getText().equals("")) {
        try {newC.deltCp = Double.parseDouble(jTextFieldDeltCp.getText());}
        catch (NumberFormatException ex) {
            //System.err.println("Error reading double from \""+jTextFieldLogK.getText()+"\"");
            newC.deltCp = Complex.EMPTY;
        }
    } else {newC.deltCp = Complex.EMPTY;}
    int jProton = -1;
    for (int i=0; i < Complex.NDIM; i++) {
        javax.swing.JComboBox<String> jcb = boxes.get(i); // javax.swing.JComboBox jcb = boxes.get(i); // java 1.6
        newC.component[i] = jcb.getSelectedItem().toString();
        if(newC.component[i] == null) {newC.component[i] = "";}
        if(newC.component[i].length() >0) {
            try {newC.numcomp[i] = Double.parseDouble(texts[i].getText());}
            catch (NumberFormatException ex) {
                    //System.err.println("Error reading double from \""+texts[i].getText()+"\"");
                    newC.component[i] = ""; newC.numcomp[i] =0;
            }
            if(Util.isProton(newC.component[i])) {jProton = i;}
        } else {newC.numcomp[i] =0;}
    }//for i
    if(jProton > -1) {newC.proton = newC.numcomp[jProton];} else {newC.proton = 0;}
    newC.reference = jTextFieldRef.getText();
    newC.comment = jTextFieldComment.getText();
    //newC.comment = "";
    boolean ok = Complex.isChargeBalanced(newC);
    // if there has been a change: enable the Save button
    if(newC.isEqualTo(oldNewC) && ok) { //no changes?
        jMenuFileSave.setEnabled(false);
        jButtonSaveReac.setEnabled(false);
    } else {
        jMenuFileSave.setEnabled(true);
        jButtonSaveReac.setEnabled(true);
    }
    if(newC.name != null && newC.name.length() >0) {
        if(ok) {
            jLabelCharge.setText("<html>Reaction is charge balanced</html>");
            jLabelCharge.setForeground(java.awt.Color.BLACK);
        } else {
            jLabelCharge.setText("<html>Reaction is <b>NOT</b> charge balanced</html>");
            jLabelCharge.setForeground(vermilion);
        }
    } else {
        jLabelCharge.setText(" ");
    }
  } //update_newC()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="validateReactionCoeff">
  private synchronized void validateReactionCoeff(int i) {
    if(loading || rearranging) {return;}
    if(i < 0 || i >= texts.length) {return;}
    String t = texts[i].getText();
    if(t.length() <= 0) {return;}
    double w;
    try {w = Double.parseDouble(t);  w = Math.min(1000,Math.max(w,-1000));}
    catch (NumberFormatException nfe) {
        texts[i].setText("");
        System.out.println("Error reading reaction coefficient "+i+" from text \""+t+"\""+nl+
                "   "+nfe.toString());
        return;
    }
    if(Math.abs(w) > 999) {
        if(w>0) {w = 999;} else {w = -999;}
        texts[i].setText(Util.formatNumAsInt(w));
        javax.swing.JOptionPane.showMessageDialog(this, "The reaction coefficient is outside \"reasonable\" range", pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
        return;
    }
    if(Math.abs(w) < 0.001) {w = 0;} 
    texts[i].setText(Util.formatNumAsInt(w));
  } //validateReactionCoeff(i)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="validateLogK">
  private void validateLogK(){
    if(loading) {return;}
    String t = jTextFieldLogK.getText();
    if(t.length() <= 0) {return;}
    double w;
    try {w = Double.parseDouble(t);  w = Math.min(10000,Math.max(w,-10000));}
    catch (NumberFormatException nfe) {
        jTextFieldLogK.setText("");
        MsgExceptn.msg("Error reading reaction logK from text \""+t+"\""+nl+
                "   "+nfe.toString());
        return;
    }
    if(Math.abs(w) > 9999) {
        if(w>0) {w = 9999;} else {w = -9999;}
        jTextFieldLogK.setText(Util.formatNumAsInt(w));
        javax.swing.JOptionPane.showMessageDialog(this, "logK  is outside \"reasonable\" range", pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
        return;
    }
    jTextFieldLogK.setText(Util.formatNumAsInt(w));
  } //validateLogK()
  //</editor-fold>
  //<editor-fold defaultstate="collapsed" desc="validateDeltH">
  private void validateDeltH(){
    if(loading) {return;}
    String t = jTextFieldDeltH.getText();
    if(t.length() <= 0) {return;}
    double w;
    try {w = Double.parseDouble(t);  w = Math.min(100000,Math.max(w,-100000));}
    catch (NumberFormatException nfe) {
        jTextFieldDeltH.setText("");
        MsgExceptn.msg("Error reading reaction Delta-H from text \""+t+"\""+nl+
                "   "+nfe.toString());
        return;
    }
    if(Math.abs(w) > 99999) {
        if(w>0) {w = 99999;} else {w = -99999;}
        jTextFieldDeltH.setText(Util.formatNumAsInt(w));
        javax.swing.JOptionPane.showMessageDialog(this, "Delta-H  is outside \"reasonable\" range", pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
        return;
    }
    jTextFieldDeltH.setText(Util.formatNumAsInt(w));
  } //validateDeltH()
  //</editor-fold>
  //<editor-fold defaultstate="collapsed" desc="validateDeltCp">
  private void validateDeltCp(){
    if(loading) {return;}
    String t = jTextFieldDeltCp.getText();
    if(t.length() <= 0) {return;}
    double w;
    try {w = Double.parseDouble(t);  w = Math.min(100000,Math.max(w,-100000));}
    catch (NumberFormatException nfe) {
        jTextFieldDeltCp.setText("");
        MsgExceptn.msg("Error reading reaction Delta-Cp from text \""+t+"\""+nl+
                "   "+nfe.toString());
        return;
    }
    if(Math.abs(w) > 99999) {
        if(w>0) {w = 99999;} else {w = -99999;}
        jTextFieldDeltCp.setText(Util.formatNumAsInt(w));
        javax.swing.JOptionPane.showMessageDialog(this, "Delta-Cp  is outside \"reasonable\" range", pc.progName, javax.swing.JOptionPane.ERROR_MESSAGE);
        return;
    }
    jTextFieldDeltCp.setText(Util.formatNumAsInt(w));
  } //validateDeltCp()
  //</editor-fold>

/** Shows a message box (and optionally outputs a message)
 * @param msg
 * @param type =-1 shows an error message dialog only; 0 shows an error message dialog and
 * the message is logged; =1 warning dialog; =2 information dialog */
void showErr(String msg, int type) {
    if(msg == null || msg.trim().length() <=0) {return;}
    int j;
    if(type == 1 || type == 2) {
        if(type==2) {j=javax.swing.JOptionPane.INFORMATION_MESSAGE;}
        else {j=javax.swing.JOptionPane.WARNING_MESSAGE;}
    } else {
        if(type == 0) {MsgExceptn.msg(msg);}
        j = javax.swing.JOptionPane.ERROR_MESSAGE;
    }
    if(!this.isVisible()) {this.setVisible(true);}
    javax.swing.JOptionPane.showMessageDialog(this, msg, pc.progName,j);
} //showErr

private static class AddDataFrameInternalException extends Exception {
    public AddDataFrameInternalException() {super();}
    public AddDataFrameInternalException(String txt) {super(txt);}
} //AddDataInternalException

  //</editor-fold>

//<editor-fold defaultstate="collapsed" desc="class ComplexListCellRenderer">
/** used to display text lines in a JList using different colours. Items in the
 * DefaultListModel of the JList are Object[2] where the first Object is a String
 * and the second a Color*/
private class ComplexListCellRenderer implements javax.swing.ListCellRenderer {
  private final javax.swing.DefaultListCellRenderer defaultRenderer = new javax.swing.DefaultListCellRenderer();
  private final java.awt.Font defFont = new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12);

  @Override public java.awt.Component getListCellRendererComponent(
          javax.swing.JList list,
          Object value,
          int index,
          boolean isSelected,
          boolean cellHasFocus) {
    java.awt.Color theForeground;
    String theText;

    javax.swing.JLabel renderer =
        (javax.swing.JLabel)
            defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

    if (value instanceof ModelComplexesItem) {
        ModelComplexesItem item = (ModelComplexesItem) value;
        theText = item.toString();
        theForeground = item.getForeground();
    } else {
        theText = value.toString();
        theForeground = list.getForeground();
    }
    if (!isSelected) {
        renderer.setForeground(theForeground);
    }
    renderer.setText(theText);
    renderer.setFont(defFont);
    return renderer;
  } //getListCellRendererComponent
}//class ComplexListCellRenderer

//</editor-fold>

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JButton jButtonLink;
    private javax.swing.JButton jButtonSaveComp;
    private javax.swing.JButton jButtonSaveReac;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuMsg;
    private javax.swing.JComboBox<String> jComboBox0;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JComboBox<String> jComboBox4;
    private javax.swing.JComboBox<String> jComboBox5;
    private javax.swing.JComboBox<String> jComboBoxElems;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabelCharge;
    private javax.swing.JLabel jLabelComp;
    private javax.swing.JLabel jLabelCompDescr;
    private javax.swing.JLabel jLabelCompName;
    private javax.swing.JLabel jLabelComps;
    private javax.swing.JLabel jLabelElems;
    private javax.swing.JLabel jLabelFiles;
    private javax.swing.JLabel jLabelHelp;
    private javax.swing.JLabel jLabelLinked;
    private javax.swing.JLabel jLabelLogK;
    private javax.swing.JLabel jLabelPlus1;
    private javax.swing.JLabel jLabelPlus3;
    private javax.swing.JLabel jLabelPlus5;
    private javax.swing.JLabel jLabelPlus6;
    private javax.swing.JLabel jLabelPlus7;
    private javax.swing.JLabel jLabelRLh;
    private javax.swing.JLabel jLabelReact;
    private javax.swing.JLabel jLabelReaction;
    private javax.swing.JLabel jLabelReactionText;
    private javax.swing.JLabel jLabelRef;
    private javax.swing.JList jListComps;
    private javax.swing.JList jListReact;
    private javax.swing.JMenu jMenuAdd;
    private javax.swing.JMenuItem jMenuAddComp;
    private javax.swing.JMenuItem jMenuAddReact;
    private javax.swing.JMenuItem jMenuAddShow;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenuItem jMenuFileExit;
    private javax.swing.JMenuItem jMenuFileSave;
    private javax.swing.JMenuItem jMenuFileShow;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuHelpHlp;
    private javax.swing.JMenuItem jMenuItemCancel;
    private javax.swing.JMenuItem jMenuItemDel;
    private javax.swing.JMenuItem jMenuItemDetails;
    private javax.swing.JMenuItem jMenuItemEdit;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelComponent;
    private javax.swing.JPanel jPanelComps;
    private javax.swing.JPanel jPanelFiles;
    private javax.swing.JPanel jPanelReaction;
    private javax.swing.JPanel jPanelReaction1;
    private javax.swing.JPanel jPanelReaction2;
    private javax.swing.JPanel jPanelReactions;
    private javax.swing.JPopupMenu jPopupMenu;
    private javax.swing.JScrollPane jScrollPaneComps;
    private javax.swing.JScrollPane jScrollPaneFiles;
    private javax.swing.JScrollPane jScrollPaneReact;
    private javax.swing.JPopupMenu.Separator jSeparator;
    private javax.swing.JPopupMenu.Separator jSeparatorAdd;
    private javax.swing.JTextArea jTextAreaFiles;
    private javax.swing.JTextField jTextField0;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextFieldComment;
    private javax.swing.JTextField jTextFieldCompDescr;
    private javax.swing.JTextField jTextFieldCompName;
    private javax.swing.JTextField jTextFieldComplex;
    private javax.swing.JTextField jTextFieldDeltCp;
    private javax.swing.JTextField jTextFieldDeltH;
    private javax.swing.JTextField jTextFieldLogK;
    private javax.swing.JTextField jTextFieldRef;
    // End of variables declaration//GEN-END:variables
}
