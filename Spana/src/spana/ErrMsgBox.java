package spana;

/**  Displays a "message box" modal dialog with an "OK" button and a title.
 * The message is displayed in a text area (non-editable),
 * which can be copied and pasted elsewhere.
 *
 * Copyright (C) 2014-2016 I.Puigdomenech.
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
public class ErrMsgBox {
  private static final String nl = System.getProperty("line.separator");

  //<editor-fold defaultstate="collapsed" desc="ErrMsgBox(msg, title)">
  /** Displays a "message box" modal dialog with an "OK" button and a title.
   * The message is displayed in a text area (non-editable),
   * which can be copied and pasted elsewhere.
   * @param msg will be displayed in a text area, and line breaks may be
   * included, for example: <code>new MsgBox("Very\nbad!",""); </code>
   * If null or empty nothing is done.
   * @param title for the dialog. If null or empty, "Error:" is used
   * @see #showErrMsg(java.awt.Component, java.lang.String, int) showErrMsg
   * @version 2015-July-14 */
  public ErrMsgBox(String msg, String title) {
    if(msg == null || msg.trim().length() <=0) {
        System.out.println("--- MsgBox: null or empty \"message\".");
        return;
    }
    //--- Title
    if(title == null || title.length() <=0) {title = " Error:";}
    java.awt.Frame frame = new java.awt.Frame(title);
    //--- Icon
    String iconName = "images/ErrMsgBx.gif";
    java.net.URL imgURL = this.getClass().getResource(iconName);
    if(imgURL != null) {frame.setIconImage(new javax.swing.ImageIcon(imgURL).getImage());}
    else {System.out.println("--- Error in MsgBox constructor: Could not load image = \""+iconName+"\"");}
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
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="private class MsgBoxDialog">
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

  //<editor-fold defaultstate="collapsed" desc="private wrapString">
/** Returns an input string, with lines that are longer than <code>maxLength</code>
 * word-wrapped and indented. * 
 * @param s input string
 * @param maxLength if an input line is longer than this length,
 * the line will be word-wrapped at the first white space after <code>maxLength</code>
 * and indented with 4 spaces
 * @return string with long-lines word-wrapped
 */
    private static String wrapString(String s, int maxLength) {
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
  // </editor-fold>

}
