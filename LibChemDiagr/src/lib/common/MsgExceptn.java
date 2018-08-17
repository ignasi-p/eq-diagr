package lib.common;

/**  A collection of static methods to display messages and exceptions.
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
public class MsgExceptn {
  private static final String nl = System.getProperty("line.separator");

  //<editor-fold defaultstate="collapsed" desc="msg">
 /** outputs <code>txt</code> on <b><code>System.out</code></b> preceeded by a line
  * with the calling class and method, and surrounded by lines.
  * @param txt the message
  * @see lib.common.Util#stack2string(java.lang.Exception) stack2string
  * @see #exception(java.lang.String) exceptn
  * @see #showErrMsg(java.awt.Component, java.lang.String, int) showErrMsg
  */
  public static void msg(String txt) {
    exceptionPrivate(txt, ClassLocator.getCallerClassName(), getCallingMethod(), false);
  } //error(msg)
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="exception">
 /** outputs <code>txt</code> on <b><code>System.err</code></b> preceeded by a line
  * with the calling class and method, and surrounded by lines.
  * @param txt the message
  * @see lib.common.Util#stack2string(java.lang.Exception) stack2string
  * @see #msg(java.lang.String) msg
  * @see #showErrMsg(java.awt.Component, java.lang.String, int) showErrMsg
  */
  public static void exception(String txt) {
    exceptionPrivate(txt, ClassLocator.getCallerClassName(), getCallingMethod(), true);
  } //error(msg)
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="showErrMsg">
   /** Prints "msg" on System.out and displays it on a message box.
    * @param parent The owner of the modal dialog that shows the message.
    * If null or not enabled: a special frame (window) will be created
    * to show the message.
    * @param msg message to be shown. If null or empty nothing is done
    * @param type =1 exception error; =2 warning; =3 information */
    public static void showErrMsg(java.awt.Component parent, String msg, int type) {
        if(msg == null || msg.trim().length() <=0) {return;}
        if(type == 1) {
            System.err.println("----"+nl+msg+nl+"----");
        } else {
            System.out.println("----"+nl+msg+nl+"----");
        }
        if(parent == null || !parent.isEnabled()) {
            System.out.println("--- showErrMsg: parent is \"null\" or not enabled");
            ErrMsgBox mb = new ErrMsgBox(msg, null);
        } else {
            String title;
            if(parent instanceof java.awt.Frame) {
                title = ((java.awt.Frame)parent).getTitle();
            } else if(parent instanceof java.awt.Dialog) {
                title = ((java.awt.Dialog)parent).getTitle();
            } else {title = " Error:";}
            int j;
            if(type==2) {j=javax.swing.JOptionPane.INFORMATION_MESSAGE;}
            else if(type==3) {j=javax.swing.JOptionPane.WARNING_MESSAGE;}
            else {j=javax.swing.JOptionPane.ERROR_MESSAGE;}
            msg = wrapString(msg,50);
            javax.swing.JOptionPane.showMessageDialog(parent, msg, title,j);
            parent.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }
    }
  // </editor-fold>

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

  //<editor-fold defaultstate="collapsed" desc="private exceptionPrivate + getCallingMethod">
  private static void exceptionPrivate(String msg, String callingClass, String callingMethod, boolean error) {
    final String ERR_START = "============================";
    final java.io.PrintStream ps;
    if(error) {ps = System.err;} else {ps = System.out;}
    ps.println(ERR_START);
    boolean p = false;
    if(callingClass != null && callingClass.length() >0) {
        ps.print(callingClass);
        p = true;
    }
    if(callingMethod != null && callingMethod.length() >0) {
        if(p) {ps.print("$");}
        ps.print(callingMethod);
        p = true;
    }
    if(p) {ps.println();}
    if(msg == null || msg.length() <=0) {msg = "Unknown error.";}
    ps.println(msg+nl+ERR_START);
    ps.flush();
  } //error(msg)

  private static String getCallingMethod() {
    boolean doNext = false;
    int n = 2;
    for (StackTraceElement s : Thread.currentThread().getStackTrace()) {
        if (doNext) {
          if(n == 0) {
              return s.getMethodName();
          } else {n--;}
        }
        else {doNext = s.getMethodName().equals("getStackTrace");}
    }
    return "unknown";
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="private class ErrMsgBox">
  /** Displays a "message box" modal dialog with an "OK" button and a title. */
  private static class ErrMsgBox {

  /** Displays a "message box" modal dialog with an "OK" button and a title.
   * The message is displayed in a text area (non-editable),
   * which can be copied and pasted elsewhere.
   * @param msg will be displayed in a text area, and line breaks may be
   * included, for example: <code>new MsgBox("Very\nbad!",""); </code>
   * If null or empty nothing is done.
   * @param title for the dialog. If null or empty, "Error:" is used
   * @see MsgExceptn#showErrMsg(java.awt.Component, java.lang.String, int)
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
  }
  // </editor-fold>
}
