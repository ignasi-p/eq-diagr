package chemicalDiagramsHelp;

/** a UI subclass that will open external links (website or ftp links)
 * in an external browser.
 * Adapted from examples in the internet.
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
public class ExternalLinkContentViewerUI extends javax.help.plaf.basic.BasicContentViewerUI {
  private final javax.swing.JComponent me;
  public ExternalLinkContentViewerUI(javax.help.JHelpContentViewer x) {
      super(x);
      me = (javax.swing.JComponent) x;
  } // constructor

  public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent x) {
      return new ExternalLinkContentViewerUI((javax.help.JHelpContentViewer)x);
  } // createUI

  @Override
  public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent he){
      if(he.getEventType()== javax.swing.event.HyperlinkEvent.EventType.ACTIVATED){
          try{
              final java.net.URL u = he.getURL();
              if(u != null) {
                  if(u.getProtocol().equalsIgnoreCase("http")
                          ||u.getProtocol().equalsIgnoreCase("ftp")) {
                      Thread t = new Thread() {@Override public void run(){
                            BareBonesBrowserLaunch.openURL(u.toString(),me);
                      }};
                      t.start(); // Note: t.start() returns inmediately.
                      return;
                  }
              }
          } catch(Throwable t){}
      } // if(eventType.ACTIVATED)
      super.hyperlinkUpdate(he);

} // hyperlinkUpdate

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
 * @author  Dem Pilafian
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
   public static void openURL(String url, javax.swing.JComponent parent) {  // modified by Ignasi (added "parent")
       try {  //attempt to use Desktop library from JDK 1.6+
         Class<?> d = Class.forName("java.awt.Desktop");
         d.getDeclaredMethod("browse", new Class<?>[] {java.net.URI.class}).invoke(
            d.getDeclaredMethod("getDesktop").invoke(null),
            new Object[] {java.net.URI.create(url)});
         //above code mimicks:  java.awt.Desktop.getDesktop().browse()
         }
      catch (Exception ignore) {  //library not available or failed
         String osName = System.getProperty("os.name");
         try {
            if (osName.startsWith("Mac OS")) {
               Class.forName("com.apple.eio.FileManager").getDeclaredMethod(
                  "openURL", new Class<?>[] {String.class}).invoke(null,new Object[] {url});
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
            HelpWindow.ErrMsg(errMsg + "\n" + e.getMessage()); // added by Ignasi
            javax.swing.JOptionPane.showMessageDialog(parent, errMsg + "\n" + e.getMessage(), // modified by Ignasi (added "parent")
                    "Chemical Equilibrium Diagrams", javax.swing.JOptionPane.ERROR_MESSAGE);  // added by Ignasi
         }
      }
   }

}
  //</editor-fold>

}
