package spana;

import lib.common.MsgExceptn;
import lib.kemi.graph_lib.DiagrPaintUtility;
import lib.kemi.graph_lib.GraphLib;

/** Prints a java.awt.Component (in this case a JPanel).
 * <br>
 * Adapted from
 * http://aplcenmp.apl.jhu.edu/~hall/java/Swing-Tutorial/Swing-Tutorial-Printing.html
 * <br>
 * without restrictions as specified on their web site. To quote the link above:
 * <i>All source code freely available for unrestricted use.</i>
 * <p>
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
public class DiagrPrintUtility implements java.awt.print.Printable {
  private final java.awt.Component componentToBePrinted;
  private final GraphLib.PltData dd; // contains the info in the plt-file;
  // the methods in DiagrPaintUtility are used to paint the diagram
  private final DiagrPaintUtility diagrPaintUtil;
  private boolean defaultPrinter = false;
  private final String prgName;
  /** New-line character(s) to substitute "\n" */
  private static final String nl = System.getProperty("line.separator");

  private DiagrPrintUtility(java.awt.Component componentToBePrinted,
                                GraphLib.PltData dD,
                                boolean defaultPrinter,
                                DiagrPaintUtility dPaintUtil,
                                String progName) {
    this.componentToBePrinted = componentToBePrinted;
    this.dd = dD;
    this.defaultPrinter = defaultPrinter;
    this.diagrPaintUtil = dPaintUtil;
    this.prgName = progName;
  }

 /** Constructs an instance of DiagrPrintUtility and starts printing
  * 
  * @param c the component to be printed
  * @param dD data needed for the paint method
  * @param defaultPrinter if false, a printer dialog is used to allow the user to select a printer
  * @param dPaintUtil the object that does the painting
  * @param progName  title for dialogs
  */
  public static void printComponent(java.awt.Component c,
                                GraphLib.PltData dD,
                                boolean defaultPrinter,
                                DiagrPaintUtility dPaintUtil,
                                String progName) {
    new DiagrPrintUtility(c, dD, defaultPrinter, dPaintUtil, progName).print();
  }

  private void print() {
    componentToBePrinted.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
    java.awt.print.PrinterJob printJob = java.awt.print.PrinterJob.getPrinterJob();
    if(printJob.getPrintService() == null) {
        javax.swing.JOptionPane.showMessageDialog(componentToBePrinted,
                "Error: No printers found."+nl, prgName, javax.swing.JOptionPane.ERROR_MESSAGE);
        return;
    }
    //get printer names and loop through them
    //javax.print.PrintService[] printServices =
    //        javax.print.PrintServiceLookup.lookupPrintServices(null, null);
    //for (javax.print.PrintService  service : printServices)
    //    {System.out.println("printer="+service.getName());}

    printJob.setPrintable(this);
    javax.print.attribute.PrintRequestAttributeSet attrSet =
            new javax.print.attribute.HashPrintRequestAttributeSet();
    //Letter: 216x279;  A4 210x297
    javax.print.attribute.standard.MediaSizeName mediaSizeName =
            javax.print.attribute.standard.MediaSize.findMedia(210f, 279f, javax.print.attribute.standard.MediaSize.MM);
    if(mediaSizeName != null) {attrSet.add(mediaSizeName);}
    javax.print.attribute.standard.MediaPrintableArea mediaPrintableArea =
            new javax.print.attribute.standard.MediaPrintableArea(15f, 5f, 190f, 269f, javax.print.attribute.standard.MediaPrintableArea.MM);
    attrSet.add(mediaPrintableArea);
    boolean ok;
    if(defaultPrinter) {ok = true;}
    else {ok = printJob.printDialog();}
    if (ok)
      try {
        printJob.print(attrSet);
      } catch(java.awt.print.PrinterException ex) {
        String t = "Error: "+ex.toString()+nl+"While printing plot file:"+nl+dd.pltFile_Name;
        MsgExceptn.exception(t);
        javax.swing.JOptionPane.showMessageDialog(componentToBePrinted, t, prgName,
                    javax.swing.JOptionPane.ERROR_MESSAGE);
      }
    componentToBePrinted.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
  }

  @Override
  public int print(java.awt.Graphics g, java.awt.print.PageFormat pf, int pageIndex) {
    if (pageIndex > 0) {
      return(NO_SUCH_PAGE);
    } else {
      java.awt.Graphics2D g2d = (java.awt.Graphics2D)g;
      int x0 = Math.round((float)pf.getImageableX());
      int y0 = Math.round((float)pf.getImageableY());
      g2d.translate(x0, y0);
      disableDoubleBuffering(componentToBePrinted);

      int w = Math.round((float)pf.getImageableWidth());
      int h = Math.round((float)pf. getImageableHeight());
      java.awt.Dimension pageDim = new java.awt.Dimension(w,h);
      boolean printing = true;
      diagrPaintUtil.paintDiagram(g2d, pageDim, dd, printing);

      //componentToBePrinted.paint(g2d);

      enableDoubleBuffering(componentToBePrinted);
      return(PAGE_EXISTS);
    }
  }

  private static void disableDoubleBuffering(java.awt.Component c) {
    javax.swing.RepaintManager currentManager = javax.swing.RepaintManager.currentManager(c);
    currentManager.setDoubleBufferingEnabled(false);
  }

  private static void enableDoubleBuffering(java.awt.Component c) {
    javax.swing.RepaintManager currentManager = javax.swing.RepaintManager.currentManager(c);
    currentManager.setDoubleBufferingEnabled(true);
  }
}