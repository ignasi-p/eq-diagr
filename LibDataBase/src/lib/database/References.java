package lib.database;

import lib.huvud.SortedProperties;

/** The objects of this class store the citations and corresponding references
 * contained in a references file (text file in "ini" properties format).
 * The keys and references are stored in a SortedProperties object.
 * Methods for reading and saving the file are provided. 
 * References may be displayed through a dialog window.
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
 * @see lib.huvud.SortedProperties SortedProperties
 * @author Ignasi Puigdomenech */
public class References {
  private final boolean dbg;
  private String referenceFileName;
  /** the list with references found in "refFile" */
  private SortedProperties propertiesRefs = null;
  private final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

  private final String line = "- - - - -";
  /** New-line character(s) to substitute "\n" */
  private static final String nl = System.getProperty("line.separator");

  public References() {dbg = false;}
  public References(boolean dbg) {this.dbg = dbg;}

  //<editor-fold defaultstate="collapsed" desc="readRefsFile()">
 /** Read a text file containing references in properties "ini" format, and
  * store the citation-references in this class instance.
  * @param refFileName  a file name, including path and extension
  * @param dbg  if true some debug output has to be printed 
  * @return <code>false</code> if the text file can not be read for any reason
  * @see References#saveRefsFile saveRefsFile
  */
  public boolean readRefsFile(String refFileName, boolean dbg) {
    if(refFileName == null || refFileName.trim().length() <=0) {
        referenceFileName = null;
        propertiesRefs = null;
        return false;
    }
    if(propertiesRefs == null) {
        propertiesRefs = new SortedProperties();
    } else {
        propertiesRefs.clear();
    }
    referenceFileName = refFileName.trim();
    java.io.File rf = new java.io.File(referenceFileName);
    if(!rf.exists()) {
        System.out.println(
                line+nl+
                "Warning - file not found:"+nl+
                "\""+rf.getAbsolutePath()+"\""+nl+
                line);
        rf = null;
    } else {
        if(!rf.canRead()) {
        System.out.println(
                line+nl+
                "Warning - can not read from file:"+nl+
                "\""+rf.getAbsolutePath()+"\""+nl+
                line);
        rf = null;
        }
    }
    if(rf == null) {return false;}
    if(dbg) {System.out.println("Reading file \""+rf.getAbsolutePath()+"\"");}
    java.io.FileInputStream fis = null;
    java.io.BufferedReader r = null;
    try {
        fis = new java.io.FileInputStream(rf);
        r = new java.io.BufferedReader(new java.io.InputStreamReader(fis,"UTF8"));
        propertiesRefs.load(r);
    } //try
    catch (java.io.FileNotFoundException e) {
        System.out.println("Warning: file Not found: \""+rf.getAbsolutePath()+"\"");
        propertiesRefs = null;
    } //catch FileNotFoundException
    catch (java.io.IOException e) {
        String msg = line+nl+"Error: \""+e.toString()+"\""+nl+
                     "   while loading REF-file:"+nl+
                     "   \""+rf.getAbsolutePath()+"\""+nl+line;
        System.err.println(msg);
        propertiesRefs = null;
        referenceFileName = null;
    } // catch loading-exception
    finally {
        try {if(r != null) {r.close();} if(fis != null) {fis.close();}}
        catch (java.io.IOException e) {
            String msg = line+nl+"Error: \""+e.toString()+"\""+nl+
                          "   while closing REF-file:"+nl+
                          "   \""+rf.getAbsolutePath()+"\""+nl+line;
            System.err.println(msg);
        } // catch close-exception
    }
    return propertiesRefs != null;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="saveRefsFile()">
  /** Save the references and their citation codes (stored in this object)
   * into the file previously read.  If there are no references to save,
   * an empty file is created.
   * @param parent used to display warnings and errors.
   * @param warning if <code>true</code> and if the file exists (normally it should)
   * then a warning is issued asking the user if it is OK to replace the file.
   * @see References#readRefsFile readRefsFile
   * @see References#setRef setRef
   */
  public void saveRefsFile(java.awt.Frame parent, boolean warning) {
    if(propertiesRefs == null) {
        propertiesRefs = new SortedProperties();
        System.err.println(line+nl+"Warning in \"saveRefsFile\" - propertiesRefs is empty."+nl+line);
    }
    if(referenceFileName == null || referenceFileName.trim().length() <=0) {
        System.err.println(line+nl+"Error in \"saveRefsFile\" - empty file name."+nl+line);
        return;
    }
    java.io.File rf = new java.io.File(referenceFileName);
    if(rf.exists()) {
        if(warning) {
            Object[] opt = {"OK", "Cancel"};
            int m = javax.swing.JOptionPane.showOptionDialog(parent,
                "Warning - file:"+nl+"    "+rf.getName()+nl+
                "will be overwritten."+nl+" ",
                "Save reerences", javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE, null, opt, opt[1]);
            if(m != javax.swing.JOptionPane.YES_OPTION) {return;}
        }
        if(!rf.canWrite() || !rf.setWritable(true)) {
            javax.swing.JOptionPane.showMessageDialog(parent,
                    "Error - Can not write to file:"+nl+
                    rf.getAbsolutePath()+nl+" ",
                    "Save reerences", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
    }
    java.io.FileOutputStream fos = null;
    java.io.Writer w = null;
    System.out.println("Saving file \""+rf.getAbsolutePath()+"\"");
    try{
        fos = new java.io.FileOutputStream(rf);
        w = new java.io.BufferedWriter(new java.io.OutputStreamWriter(fos,"UTF8"));
        propertiesRefs.store(w,null);
        System.out.println("Written: \""+rf.getAbsolutePath()+"\"");
    } catch (java.io.IOException e) {
        String msg = "Error: \""+e.toString()+"\""+nl+
                       "   while writing the References file:"+nl+
                       "   \""+rf.getAbsolutePath()+"\"";
        System.err.println(msg);
        javax.swing.JOptionPane.showMessageDialog(parent, msg,
                  "Save references",javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    finally {
        try {if(w != null) {w.close();} if(fos != null) {fos.close();}}
        catch (java.io.IOException e) {
            String msg = "Error: \""+e.toString()+"\""+nl+
                          "   while closing the References file:"+nl+
                          "   \""+rf.getAbsolutePath()+"\"";
            System.err.println(msg);
            javax.swing.JOptionPane.showMessageDialog(parent,msg,
                    "Save references",javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    } //finally
  }
  // </editor-fold>

  /** Returns the name of the references-file that has been read and
   * stored into this instance.
   * @return  the name of the file. It will be null if no file has been read yet. */
  public String referencesFileName() {
      return referenceFileName;
  }

  //<editor-fold defaultstate="collapsed" desc="splitRefs(text)">
  /** Split a <code>text</code>, containing reference citations, into a
   * list of citations.  Citations are expected to be separated either by
   * a comma (,) or by a semicolon (;) or by a "plus" (+).  For example, either of: "Yu 95,Li" or
   * "Yu 95, Li" or "Yu 95+Li" will return the array {"Yu 95", "Li"}.
   * To include a "+" or "," into a reference citation, include the citation
   * in parentheses (either "()", "[]" or "{}"), for example the text
   * "1996H (estimate based in La+3)" will return an array with two citations:
   * {"1996H", "(estimate based in La+3)"}.
   * @param txt containing reference citations separated by "+", semicolons or commas.
   * @return array list of reference citations. Returns null if <code>text</code> is null,
   * Returns the array {""} if <code>text</code> is empty  */
  public static java.util.ArrayList<String> splitRefs(String txt) {
    if(txt == null) {return null;}
    final boolean debug = false;
    // Regex delimiter:
    // Delimiter is either:
    // "zero or more whitespace followed by "+" followed by zero or more whitespace"  or
    // "zero or more whitespace followed by "," followed by zero or more whitespace"  or
    // "zero or more whitespace followed by ";" followed by zero or more whitespace"  or
    String delimiter = "\\s*\\+\\s*|\\s*,\\s*|\\s*;\\s*";
    java.util.Scanner scanner;
    String text = txt.trim();
    java.util.ArrayList<String> rfs = new java.util.ArrayList<String>();
    if(text.length() <=0) {rfs.add(""); return rfs;}
    String token, left;
    //  open parenthesis, square brackets, curly braces
    int open, openP,openS,openC,close;
    char openChar, closeChar;
    // ---- get a list of references ----
    if(debug) {System.out.println("text = \""+text+"\"");}
    //
    // ---- 1st find and remove texts within "([{}])"
    openP = text.indexOf("(");
    openS = text.indexOf("[");
    openC = text.indexOf("{");
    open = openP;
    if(openP >=0) {
        if(openS >=0) {open = Math.min(openP, openS);}
        if(openC >=0) {open = Math.min(open,  openC);}
    } else if(openS >=0) {
        open = openS;
        if(openC >=0) {open = Math.min(openS, openC);}
    } else {open = openC;}
    if(debug) {System.out.println("open = "+open+", (="+openP+", [="+openS+", {="+openC);}
    while (open >=0) {
        openChar = '('; closeChar = ')';
        if(open == openS) {openChar = '['; closeChar = ']';}
        if(open == openC) {openChar = '{'; closeChar = '}';}
        close = findClosingParen(text.toCharArray(), openChar, closeChar, open);
        if(close <= open) {break;}
        left = text.substring(0, open).trim();
        if(debug) {System.out.println("left of () = \""+left+"\"");}
        if(left.length() >0) { // add any tokens before "("
            scanner = new java.util.Scanner(left);
            scanner.useDelimiter(delimiter);
            scanner.useLocale(java.util.Locale.ENGLISH);
            while (scanner.hasNext()) {
                token = scanner.next();
                if(token.trim().length() >0) {
                    if(debug) {System.out.println("token = \""+token+"\"");}
                    rfs.add(token);
                }
            } //while
            scanner.close();
        } // tokens before "("
        token = text.substring(open,close+1);
        rfs.add(token);
        text = text.substring(close+1);
        if(debug) {System.out.println("(token) = \""+token+"\", new text = \""+text+"\"");}
        openP = text.indexOf("(");
        openS = text.indexOf("[");
        openC = text.indexOf("{");
        open = openP;
        if(openP >=0) {
            if(openS >=0) {open = Math.min(openP, openS);}
            if(openC >=0) {open = Math.min(open,  openC);}
        } else if(openS >=0) {
            open = openS;
            if(openC >=0) {open = Math.min(openS, openC);}
        } else {open = openC;}
        if(debug) {System.out.println("open = "+open+", (="+openP+", [="+openS+", {="+openC);}
    } // while (open >=0)

    scanner = new java.util.Scanner(text.trim());
    scanner.useDelimiter(delimiter);
    scanner.useLocale(java.util.Locale.ENGLISH);
    while (scanner.hasNext()) {
        token = scanner.next();
        if(token.trim().length() >0) {
            if(debug) {System.out.println("token = \""+token+"\"");}
            rfs.add(token);
        }
    } //while
    scanner.close();
    if(debug) {System.out.println(java.util.Arrays.toString(rfs.toArray()));}
    return rfs;
  }
    private static int findClosingParen(char[] text, char openParenthesis, char closeParenthesis, int openPos) {
        if(openPos < 0 || text == null || openPos >= text.length) {return -1;}
        int closePos = openPos;
        int counter = 1, pos;
        while (counter > 0) {
            pos = ++closePos;
            if(pos >= text.length) {return -1;}
            char c = text[pos];
            if (c == openParenthesis) {counter++;} else if(c == closeParenthesis) {counter--;}
        }
        return closePos;
    }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="referenceKeys()">
  /** Returns the reference keys
   * @return  the keys. It will be null if no file has been read yet. */
  public String[] referenceKeys() {
    if(propertiesRefs == null || propertiesRefs.size() <=0) {
        System.out.println(line+nl+"Warning in \"referenceKeys()\": there are no reference keys"+nl+line);
        return null;
    }
    //return propertiesRefs.keySet().toArray();
    String[] s = propertiesRefs.stringPropertyNames().toArray(new String[0]);
    return s;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="isRefThere(ref)">
  /** Returns a reference text if the key (citation) is found (ignoring case)
   * @param key the reference citation
   * @return a reference text if <code>key</code> is found;
   * returns null if <code>key</code> is null or empty or if the reference <code>key</code>
   * is not found (even when ignoring case).
   * @see References#setRef setRef
   * @see References#readRefsFile readRefsFile
   * @see References#saveRefsFile saveRefsFile
   */
  public String isRefThere(String key) {
    if(key == null || key.trim().length() <=0) {return null;}
    if(propertiesRefs == null || propertiesRefs.size() <=0) {
        System.out.println(line+nl+"Warning in \"isRefThere\": the reference file has not yet been read."+nl+line);
        return null;
    }
    String refText = propertiesRefs.getProperty(key.trim());
    if(refText != null && refText.length() >0) {return refText.trim();}
    // Not found, try case insensitive
    java.util.Set<java.util.Map.Entry<Object, Object>> s = propertiesRefs.entrySet();
    java.util.Iterator<java.util.Map.Entry<Object, Object>> it = s.iterator();
    while (it.hasNext()) {
        java.util.Map.Entry<Object, Object> entry = it.next();
        if (key.equalsIgnoreCase((String) entry.getKey())) {
            return ((String) entry.getValue()).trim();
        }
    }
    // not found anyway...
    return null;   
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="setRef(key, txt)">
  /** Set a reference text corresponding to a reference citation (key). If <code>key</code>
   * was not previously there, it is added; otherwise <code>txt</code> replaces
   * the reference corresponding to <code>key</code>. Note that the change is
   * performed in memory. The user must save the file afterwards if needed.
   * @param key a reference citation
   * @param txt the text corresponding to the reference citation. If null or empty
   * the citation <code>key</code> will be deleted.
   * @return <code>true</code> if successful;  <code>false</code> if a problem occurs
   * @see References#readRefsFile readRefsFile
   * @see References#saveRefsFile saveRefsFile
   */
  public synchronized boolean setRef(String key, String txt) {
    if(propertiesRefs == null || propertiesRefs.size() <=0) {
        System.out.println(line+nl+"Warning in \"setRef\": there are no references yet."+nl+line);
        if(propertiesRefs == null) {propertiesRefs = new SortedProperties();}
    }
    if(key == null || key.trim().length() <=0) {return false;}
    if(txt == null || txt.trim().length() <=0) {
        propertiesRefs.remove(key.trim());
    } else {
        propertiesRefs.setProperty(key.trim(), txt.trim());
    }
    return true;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="refsAsString(ArrayList)">
 /** Returns a text string containing the references corresponding to a list
  * of reference citations (ignoring case)
  * 
  * @param refKeys the citation keys for the references to display
  * @return a text string containing the references
  */
  public String refsAsString(java.util.ArrayList<String> refKeys) {
    if(dbg) {
        System.out.println("\"refsAsString\" - References read from file:");
        propertiesRefs.list(System.out);
    }
    if(refKeys == null || refKeys.isEmpty()) {return "(no references)";}
    StringBuilder allReferences = new StringBuilder();
    String refText;
    StringBuilder refKey = new StringBuilder();
    for(int i=0; i < refKeys.size(); i++) {
      if(refKeys.get(i) == null || refKeys.get(i).trim().length() <=0) {continue;}
      refKey.delete(0, refKey.length());
      refKey.append(refKeys.get(i).trim());
      if(allReferences.length()>0) {allReferences.append("- - - - - - - - - - - - - - -"); allReferences.append(nl);}
      allReferences.append(refKey.toString()); allReferences.append(":"); allReferences.append(nl); allReferences.append(nl);
      if(dbg) {System.out.println(" looking for \""+refKey.toString()+"\"");}
      if(refKey.toString().length() <=0) {continue;}
      refText = isRefThere(refKey.toString());
      if(refText == null || refText.length() <=0) {refText = "reference not found in file"+nl+"  \""+referenceFileName+"\"";}      
      allReferences.append(refText);
      if(allReferences.charAt(allReferences.length()-1) != '\n'
              && allReferences.charAt(allReferences.length()-1) != '\r') {allReferences.append(nl);}
    }
    return allReferences.toString();
  } // refsAsString
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="refsAsString(String)">
 /** Returns the references (a text string) corresponding to
  * citations contained in an imput string.
  * Citations are expected to be separated by either a comma, or a "+".
  * For example, any of: "Yu 95,Li" or "Yu 95, Li" or "Yu 95+Li" , etc.
  * 
  * @param refs the citation keys for the references to display
  * @return a text string containing the references
  */
  public String refsAsString(String refs) {
    return refsAsString(splitRefs(refs));
  } // refsAsString
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="displayRefs()">
  /** Display a dialog with references
   * @param parent
   * @param modal
   * @param species a name for the reaction product (chemical species) whose
   * reference will be displayed.  Used in the title of the dialog. May be null or empty.
   * @param refKeys the citation keys for the references to be displayed
   */
  public void displayRefs(java.awt.Frame parent, boolean modal,
          String species, java.util.ArrayList<String> refKeys) {
    if(propertiesRefs == null || propertiesRefs.size() <=0) {
        System.out.println(line+nl+"Error in \"displayRefs\":"+nl+"the reference file has not yet been read."+nl+line);
        return;
    }
    if(refKeys == null || refKeys.isEmpty()) {
        String msg = "There are no references ";
        if(species == null || species.trim().length() <=0) {msg = msg + "to show";}
        else {msg = msg +"for "+species+".";}
        javax.swing.JOptionPane.showMessageDialog(parent, msg, "No references", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        return;
    }
    ReferenceDisplayDialog dr = new ReferenceDisplayDialog(parent, true,
            species, refKeys);
    dr.setVisible(true);
  }
  // </editor-fold>

//<editor-fold defaultstate="collapsed" desc="private ReferenceDisplayDialog">

/** Display the references associated with a reaction product (chemical species)
 * @author Ignasi Puigdomenech
 */
private class ReferenceDisplayDialog extends javax.swing.JDialog {
  private javax.swing.JButton jButtonClose;
  private javax.swing.JLabel jLabelRef;
  private javax.swing.JPanel jPanel;
  private javax.swing.JScrollPane jScrollPane;
  private javax.swing.JTextArea jTextArea;
  private java.awt.Dimension windowSize = new java.awt.Dimension(280,170);
  private boolean loading = true;

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /** Creates new form ReferenceDisplayDialog */

 /** Display a dialog with some references
  * @param parent
  * @param modal
  * @param species the name of the species for which the reference will be displayed
  * @param refKeys the citation keys for the references to display  */
  private ReferenceDisplayDialog(java.awt.Frame parent, boolean modal,
          String species,
          java.util.ArrayList<String> refKeys
          ) {
    super(parent, modal);
    initComponents();
    if(species == null || species.trim().length() <=0) {
        if(dbg) {System.out.println("Warning in \"ReferenceDisplayDialog\": empty species name.");}
        species = "";
    }

    //--- Close window on ESC key
    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    javax.swing.KeyStroke escKeyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0, false);
    getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(escKeyStroke,"ESCAPE");
    javax.swing.Action escAction = new javax.swing.AbstractAction() {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
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
    if(species.trim().length() > 0) {
        this.setTitle("Reference(s) for \""+species+"\"");
    } else {this.setTitle("Reference(s)");}

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
    if(refKeys == null || refKeys.isEmpty()) {
        jScrollPane.setVisible(false);
        jLabelRef.setText("(No reference(s))");
        return;
    }
    if(dbg) {
        System.out.println("References read from file:");
        propertiesRefs.list(System.out);
    }
    jTextArea.setText("");
    jTextArea.append(refsAsString(refKeys));
    jTextArea.setCaretPosition(0);
    jButtonClose.requestFocusInWindow();
    windowSize = this.getSize();
    loading = false;
  } //constructor
  //</editor-fold>

    /** This method is called from within the constructor to
     * initialize the form. */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        jPanel = new javax.swing.JPanel();
        jLabelRef = new javax.swing.JLabel();
        jButtonClose = new javax.swing.JButton();
        jScrollPane = new javax.swing.JScrollPane();
        jTextArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        jPanel.setLayout(new java.awt.BorderLayout());

        jLabelRef.setText("Reference(s):");
        jPanel.add(jLabelRef, java.awt.BorderLayout.WEST);

        jButtonClose.setMnemonic('c');
        jButtonClose.setText("Close");
        jButtonClose.setMargin(new java.awt.Insets(1, 2, 1, 2));
        jButtonClose.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCloseActionPerformed(evt);
            }
        });
        jPanel.add(jButtonClose, java.awt.BorderLayout.EAST);

        getContentPane().add(jPanel, java.awt.BorderLayout.PAGE_START);

        jTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextAreaKeyPressed(evt);
            }
            @Override
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextAreaKeyTyped(evt);
            }
        });
        jTextArea.setLineWrap(true);
        jTextArea.setWrapStyleWord(true);
        jTextArea.setColumns(40);
        jTextArea.setRows(9);
        jScrollPane.setViewportView(jTextArea);
        //jScrollPane.setPreferredSize(new java.awt.Dimension(450, 200));

        getContentPane().add(jScrollPane, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="events">

    private void jTextAreaKeyPressed(java.awt.event.KeyEvent evt) {                                      
        int ctrl = java.awt.event.InputEvent.CTRL_DOWN_MASK;
        if(((evt.getModifiersEx() & ctrl) == ctrl) &&
            (evt.getKeyCode() == java.awt.event.KeyEvent.VK_V
                || evt.getKeyCode() == java.awt.event.KeyEvent.VK_X)) {evt.consume(); return;}
        if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {closeWindow(); evt.consume(); return;}
        if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_TAB) {evt.consume(); return;}
        if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_DELETE) {evt.consume(); return;}
        if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_BACK_SPACE) {evt.consume();}
    }                                     

    private void jTextAreaKeyTyped(java.awt.event.KeyEvent evt) {                                    
        evt.consume();
    }                                   

    private void formWindowClosing(java.awt.event.WindowEvent evt) {                                   
        closeWindow();
    }     

    private void formComponentResized(java.awt.event.ComponentEvent evt) {
      if(loading || windowSize == null) {return;}
      int x = this.getX(); int y = this.getY();
      int w = this.getWidth(); int h = this.getHeight();
      int nw = Math.max(w, Math.round((float)windowSize.getWidth()));
      int nh = Math.max(h, Math.round((float)windowSize.getHeight()));
      int nx=x, ny=y;
      if(x+nw > screenSize.width) {nx = screenSize.width - nw;}
      if(y+nh > screenSize.height) {ny = screenSize.height -nh;}
      if(x!=nx || y!=ny) {this.setLocation(nx, ny);}
      if(w!=nw || h!=nh) {this.setSize(nw, nh);}
    }

    private void jButtonCloseActionPerformed(java.awt.event.ActionEvent evt) {                                             
        closeWindow();
     }                                            

  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="methods">

  //<editor-fold defaultstate="collapsed" desc="closeWindow()">
  private void closeWindow() {
    this.dispose();
  } // closeWindow()
  // </editor-fold>

  // </editor-fold>

} //class ReferenceDisplayDialog
// </editor-fold>

}
