package spana;

import lib.common.MsgExceptn;
import lib.kemi.graph_lib.DiagrPaintUtility;
import lib.kemi.graph_lib.GraphLib;

/** Copies to the clipboard (as an RGB image) a graphics stored in a GraphLib.PltData object.
 * <br>
 * With contributions from the code posted by "perpetuum" on 2008-mar-29 at:
 * "http://forums.sun.com/thread.jspa?threadID=5129602"
 * <br>
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
public class ClipboardCopy_Image {

  /** Inner class used to hold an image while on the clipboard. */
  private static class ImageSelection implements java.awt.datatransfer.Transferable {
    /** The size is 4/5 of the user's screen size. */
    private final int width = (4*java.awt.Toolkit.getDefaultToolkit().getScreenSize().width)/5;
    private final int height;
    private final GraphLib.PltData dd; // contains the info in the plt-file;
    private final DiagrPaintUtility diagrPaintUtil;
    private static final java.awt.datatransfer.DataFlavor[] supportedFlavors
            = {java.awt.datatransfer.DataFlavor.imageFlavor};

    //Constructor
    public ImageSelection(double height2width,
            GraphLib.PltData dD,
            DiagrPaintUtility diagrPU) {
	height = (int)((double)width * height2width);
        dd = dD;
        diagrPaintUtil = diagrPU;
    }

    @Override
    public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors() {
        return supportedFlavors;
    }

    @Override
    public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor flavor) {
        for(java.awt.datatransfer.DataFlavor f : supportedFlavors) {
            if (f.equals(flavor)) return true; 
        }
        return false;
    } // isDataFlavorSupported

    /** Returns Image object housed by Transferable object */
    @Override
    public Object getTransferData(java.awt.datatransfer.DataFlavor flavor)
            throws java.awt.datatransfer.UnsupportedFlavorException, java.io.IOException {
        if (java.awt.datatransfer.DataFlavor.imageFlavor.equals(flavor)) {
            //System.out.println("Mime type imageFlavor recognized");
            try{
                //Create an empty BufferedImage to paint
                java.awt.image.BufferedImage buff_image =
                            new java.awt.image.BufferedImage(width, height,
                                      java.awt.image.BufferedImage.TYPE_INT_RGB);
                //get a graphics context
                java.awt.Graphics2D g2D = buff_image.createGraphics();
                if(diagrPaintUtil.useBackgrndColour) {
                    g2D.setColor(diagrPaintUtil.backgrnd);
                } else {
                    g2D.setColor(java.awt.Color.WHITE);
                }
                g2D.fillRect(0, 0, width, height);
                diagrPaintUtil.paintDiagram(g2D,
                        new java.awt.Dimension(width, height), dd, false);
                return buff_image;
            } catch(Exception ex) {
                MsgExceptn.exception("getTransferData - Error: "+ex.getMessage());
                return null;
            }
        } else {
            //throw new UnsupportedFlavorException(flavor);
            MsgExceptn.exception("UnsupportedFlavorException; flavor = "+flavor.toString());
            return null;
        }
    } //getTransferData

  } // class ImageSelection


/** Set the Clipboard contents to the plotting data in dD.
 * The clipboard contents will be in RGB image format.
 * The size is 4/5 of the user's screen size.
 * @param heightToWidth height to width ratio of the image to be stored on the clipboard.
 * @param dD information from the plt-file
 * @param diagrPU contains parameters an methods to do the painting  */
public static void setClipboard_Image(double heightToWidth,
                                        GraphLib.PltData dD,
                                        DiagrPaintUtility diagrPU) {
    ImageSelection imageSelection = new ImageSelection(heightToWidth, dD, diagrPU);
    java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imageSelection, null);
  } // setClipboard

} // class ClipboardCopy_Image
