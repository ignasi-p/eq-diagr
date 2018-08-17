package lib.kemi.graph_lib;

import lib.common.Util;

/** Paints a graphic context (for example a JPanel or a Printer)
 * using the data stored in a PltData object.
 *
 * Copyright (C) 2014-2015 I.Puigdomenech.
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
public class DiagrPaintUtility {
  public static final int MAX_COLOURS = 11;
  public java.awt.Color[] colours =
        {new java.awt.Color(0,0,0),     //black
         new java.awt.Color(255,35,35), //red
         new java.awt.Color(149,67,0),  //vermilion
         new java.awt.Color(47,47,255), //light blue
         new java.awt.Color(0,151,0),   //green
         new java.awt.Color(200,140,0), //orange
         new java.awt.Color(255,0,255), //magenta
         new java.awt.Color(0,0,128),  //blue
         new java.awt.Color(128,128,128),//gray
         new java.awt.Color(0,166,255), //sky blue
         new java.awt.Color(128,0,255)};//violet
  /** 0-2: Colour; BW (black/white); WB (white/black) */
  public int colourType = 0;
  public boolean useBackgrndColour = false;
  public java.awt.Color backgrnd = java.awt.Color.WHITE;
  public boolean printColour = true;
  public boolean printHeader = true;
  /** 0 = OFF; 1 = ON; 2 = DEFAULT */
  public int antiAliasing = 1;
  /** 0 = OFF; 1 = ON; 2 = DEFAULT */
  public int antiAliasingText = 0;
  public boolean fixedSize = false;
  public float fixedSizeWidth = 21f;
  public float fixedSizeHeight = 15f;
  public boolean keepAspectRatio = false;
  public float penThickness = 1;
  public float printPenThickness = 1;
  public int fontSize = 8;
  /** 0-4: Serif, SansSerif, Monospaced, Dialog, DialogInput */
  public int fontFamily = 1;
  /** 0-2: PAIN, BOLD, ITALIC */
  public int fontStyle = 0;
  /** true if texts are to be displayed on the JPanel using a font, that is,
   * not by using the line-sketches stored in the plot file. */
  public boolean textWithFonts = true;

  //<editor-fold defaultstate="collapsed" desc="class ChemFormula">
  /** Class used for input and output from method "chemF" */
  static class ChemFormula {
        /** t is a StringBuffer with the initial chemical formula given
         * to method "chemF", and it will be modified on return
         * from chemF, for example "Fe+2" may be changed to "Fe2+". */
        StringBuffer t;
        /** d float[] returned by method "chemF" with
         * super- subscript data for each character position
         * in the StringBuffer t returned  by chemF. */
        float[] d;
        /** @param txt String
        * @param d_org float[] */
        ChemFormula(String txt, float[] d_org) { // constructor
            t = new StringBuffer();
            //if (t.length()>0) {t.delete(0, t.length());}
            t.append(txt);
            int nChar = txt.length();
            d = new float[nChar];
            System.arraycopy(d_org, 0, d, 0, d_org.length); }
    } // class ChemFormula
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="class FontInfo">
  /** Class used to store information: input and output for method "changeFont".
   * Having this class avoids creating new instances of font and fontmetrics
   * every time the size is changed. */
  private class FontInfo {
      java.awt.Font f;
      java.awt.FontMetrics fm;
      float fontScale;
      float txtSize;
      float oldTxtSize = Float.MIN_VALUE;
      /** a rectangle to store temporary information */
      java.awt.geom.Rectangle2D rect;
      /** width of an "H" */
      float width_H;
      /** Class used to store information: input and output from
       * method "changeFont".
       * @param fnt
       * @param fmtr
       * @param fontScale
       * @param txtSize; the height and width of text in cm
       * @param rect a rectangle to store temporary information
       * @param widthH float the width of a capital letter "H" */
      FontInfo(java.awt.Font fnt, java.awt.FontMetrics fmtr,
              float fontScale, float txtSize,
              java.awt.geom.Rectangle2D rect, float widthH) { // constructor
          this.f = fnt;
          this.fm = fmtr;
          this.fontScale = fontScale;
          this.txtSize = txtSize;
          this.oldTxtSize = Float.MIN_VALUE;
          this.rect = rect;
          this.width_H = widthH;}
      FontInfo() { // constructor
          f = new java.awt.Font("Monospaced", java.awt.Font.PLAIN, fontSize);
          fm = null;
          fontScale = Float.NaN;
          txtSize = 0.35f;
          oldTxtSize = Float.MIN_VALUE;
          rect = null;
          width_H = 12f;}
  } // class FontInfo
  //</editor-fold>

//<editor-fold defaultstate="collapsed" desc="paintDiagram">
 /**  Paints a diagram into graphics context "g2D" using the data
  * in an object of class PltData
  @param g2D Graphics2D context
  @param compDim Dimension of the Component to be painted
  @param pltD PltData containing the data needed to plot the diagram
  @param printing boolean (printing or painting on the screen?) */
  public void paintDiagram (java.awt.Graphics2D g2D,
          java.awt.Dimension compDim,
          GraphLib.PltData pltD, boolean printing) {
    if (compDim.width <=5 || compDim.height <=5) {return;}
    int i1, i2;
    GraphLib.PltData.PlotStep ps;
    GraphLib.PltData.PlotText pt;
    int x_min; int x_max;  int y_min; int y_max;
    if(pltD == null || (fixedSize && !keepAspectRatio && !printing)) {
        x_min = -10; y_min = -10; // = -1mm
        x_max = Math.round(fixedSizeWidth*100f)+10; //add 1mm
        y_max = Math.round(fixedSizeHeight*100f)+10;
    } else {
        x_min = pltD.userSpaceMin.x; x_max = pltD.userSpaceMax.x;
        y_min = pltD.userSpaceMin.y; y_max = pltD.userSpaceMax.y;
    }
    if(keepAspectRatio || printing)
        {if (Math.abs((float)compDim.width / (float)(x_max - x_min)) <
                 Math.abs((float)compDim.height / (float)(y_max - y_min)))
            {y_max = 10 + Math.round((float)x_max * (float)compDim.height/(float)compDim.width);}
         else
            {x_max = 10 + Math.round((float)y_max * (float)compDim.width/(float)compDim.height);}
        } // if keepAspectRatio
    //System.out.println("The JPanel dimmension: width = "+compDim.getWidth()+", heght = "+compDim.getHeight());
    float xScale, yScale; int xScale0, yScale0;
    float userWidth = (float)Math.abs(x_max - x_min);
    float userHeight = (float)Math.abs(y_max - y_min);
    xScale = (float)compDim.width / userWidth;
    yScale = (float)compDim.height / userHeight;
    //System.out.println("The user space is:\n"+
    //  " x-min = "+x_min+", x-max = "+x_max+",  y-min = "+y_min+", y-max = "+y_max+
    //  " x-range = "+((int)userWidth)+", y-range = "+((int)userHeight)+"\n");
    if(pltD == null) {
        xScale0 = Math.round(-10f*xScale);
        yScale0 = Math.round(-10f*yScale);
    } else {
        xScale0 = Math.round((float)pltD.userSpaceMin.x*xScale);
        yScale0 = Math.round((float)pltD.userSpaceMin.y*yScale);
    }

    //---- paint background
    if(printing) {
        if(printColour) {
            g2D.setColor(backgrnd);
            g2D.fillRect(0, 0, compDim.width, compDim.height);
            g2D.setColor(java.awt.Color.BLACK);}
        }
    else // not printing
        {// background colour
        switch (colourType)
            {case 1: // BW (black on white)
                g2D.setColor(java.awt.Color.WHITE);
                g2D.fillRect(0, 0, compDim.width, compDim.height);
                g2D.setColor(java.awt.Color.BLACK);
                break;
             case 2: // WB (white on black)
                g2D.setColor(java.awt.Color.BLACK);
                g2D.fillRect(0, 0, compDim.width, compDim.height);
                g2D.setColor(java.awt.Color.WHITE);
                break;
             default:
                if(useBackgrndColour) {
                    g2D.setColor(backgrnd);
                    g2D.fillRect(0, 0, compDim.width, compDim.height);
                }
                g2D.setColor(java.awt.Color.BLACK);
            } // switch
    } // if printing and colour printing

    //---- if Printing: Header with file name
    // get a font
    java.awt.Font f = new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 7);
    g2D.setFont(f);
    // print Header with file name
    if(printing && printHeader) {
        g2D.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                         java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        //get the size of an upper case "H"
        final float H = 1.2f*(float)g2D.getFontMetrics().getStringBounds("H", g2D).getWidth();
        g2D.setColor(java.awt.Color.BLACK);
        java.util.Date today = new java.util.Date();
        java.text.DateFormat formatter = java.text.DateFormat
                                   .getDateTimeInstance(java.text.DateFormat.DEFAULT,
                                   java.text.DateFormat.DEFAULT);
        //String printDateOut = formatter.format(today);
        //String lastModDateOut = formatter.format(pltD.fileLastModified);
        if(pltD != null) {
            g2D.drawString("File: \""+pltD.pltFile_Name+"\"", 0f, H);
            g2D.drawString(
                "File last modified: "+formatter.format(pltD.fileLastModified)+
                ";  Printed: "+formatter.format(today)+")", 0f, (2f*H+0.5f*H));
        }
    } // if printing & printHeader

    //------------------------
    //----  paint all lines

    // antiAliasing
    if(antiAliasing == 1) { g2D.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);}
    else if(antiAliasing == 2) { g2D.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_DEFAULT);}
    else { g2D.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);}

    //line thickness
    java.awt.BasicStroke stroke; float pen;
    if(!printing) {pen = penThickness;} else {pen = printPenThickness;}
    stroke = new java.awt.BasicStroke(pen,
                java.awt.BasicStroke.CAP_SQUARE, java.awt.BasicStroke.JOIN_BEVEL);
    g2D.setStroke(stroke);

    int ix_start = 0, iy_start = 0;
    if(pltD != null) {
      if(pltD.pltFileAList.size() > 0) {
          ps = pltD.pltFileAList.get(0);
          ix_start = ps.i1;
          iy_start = ps.i2;
      }
      for(int j=0; j < pltD.pltFileAList.size(); j++) {
        ps = pltD.pltFileAList.get(j);
        //if(ps == null) {break;}
        int i0 = ps.i0;
        if (i0 == 1) {// draw line
            int ix_new = ps.i1;
            int iy_new = ps.i2;
            int x0 = Math.round((float)(ix_start)*xScale) -xScale0;
            int y0 = compDim.height - (Math.round((float)(iy_start)*yScale) -yScale0);
            int x1 = Math.round((float)(ix_new)*xScale) -xScale0;
            int y1 = compDim.height - (Math.round((float)(iy_new)*yScale) -yScale0);
            //draw
            g2D.drawLine(x0, y0, x1, y1);
            ix_start = ix_new; iy_start = iy_new; } // I0=1
        else if (i0 == 0) { // move to
            ix_start = ps.i1;
            iy_start = ps.i2; } // I0=0
        else if ( (i0 ==5 || i0 ==8)
                && ((colourType ==0 && !printing) || (printing && printColour)) ) {  // set colour
            i1 = ps.i1 - 1;
            if(i1<0) {i1=0;}
            while (i1 >= MAX_COLOURS) {i1 = i1 - MAX_COLOURS;}
            if (i1<0) {i1=0;}
            g2D.setColor(colours[i1]);
        } // I0 = 5 or 8
      } // for j
    } // if(pltD != null)
    //------------------------
    //----  paint all texts

    if (pltD == null || pltD.pltTextAList.size()<=0 || !textWithFonts) {return;}
    int oldColour = -1;
    // -- get a font
    String diagrFontFamily;
    if (fontFamily == 1) {diagrFontFamily = java.awt.Font.SANS_SERIF;}
    else if (fontFamily == 2) {diagrFontFamily = java.awt.Font.MONOSPACED;}
    else if (fontFamily == 3) {diagrFontFamily = java.awt.Font.DIALOG;}
    else if (fontFamily == 4) {diagrFontFamily = java.awt.Font.DIALOG_INPUT;}
    else {diagrFontFamily = java.awt.Font.SERIF;}
    int diagrFontStyle;
    if (fontStyle == 1) {diagrFontStyle = java.awt.Font.BOLD;}
    else if (fontStyle == 2) {diagrFontStyle = java.awt.Font.ITALIC;}
    else {diagrFontStyle = java.awt.Font.PLAIN;}
    f = new java.awt.Font(diagrFontFamily, diagrFontStyle, fontSize);
    g2D.setFont(f);
    if(antiAliasingText == 1) {
        g2D.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                             java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);}
    else if(antiAliasingText == 2) {
        g2D.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                             java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);}
    else {
        g2D.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                             java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);}

    // -- get information needed by method "changeFont"
    java.awt.FontMetrics fm = g2D.getFontMetrics(f);
    // Note: using stringWidth gave StackOverflowError with earlier version of jVectClipboard
    java.awt.geom.Rectangle2D mBnds = fm.getStringBounds("H", g2D);
    // Store information needed by method "changeFont" into "fi"
    FontInfo fi = new FontInfo(f,fm,0f,0.35f,mBnds,(float)mBnds.getWidth());
    //fontScale gives a font with point size = fontSize
    // for a text size = 0.35cm with UserSpace width = 2100
    // and window width of 350 pixels
    fi.fontScale = (fontSize/35f)*(2100f/userWidth)
                           *((float)compDim.width/350f);

    // -- Scale the font (squeeze or expand vertically/horizontally):
    double wH =(double)compDim.width/(double)compDim.height;
    double wHuserSpace = (double)userWidth/(double)userHeight;
    double scaleXY = wHuserSpace / wH;
    java.awt.geom.AffineTransform original;// = new java.awt.geom.AffineTransform();
    original = g2D.getTransform();
    g2D.scale(1,scaleXY); // from now on the user space coordinates are scaled
    // -- loop through all texts
    for (int j=0; j < pltD.pltTextAList.size(); j++) {
      String txt = pltD.pltTextAList.get(j).txtLine;
      if(txt != null && txt.length() > 0) {
         if ((!printing && colourType ==0) || (printing && printColour)) {
            // set colour to a value between 0 to "MAX_COLOURS"
            int iColour = pltD.pltTextAList.get(j).color -1;
            if (iColour<0) {iColour=0;}
            while (iColour >= MAX_COLOURS) {iColour = iColour - MAX_COLOURS;}
            if (iColour<0) {iColour=0;}
            // change colour if needed
            if (iColour != oldColour)
                  {g2D.setColor(colours[iColour]); oldColour = iColour;}
            } // if (!printing & colourType =0) | (printing & printColour)
        //
        pt = pltD.pltTextAList.get(j);
        boolean isFormula = pt.isFormula;
        int align = pltD.pltTextAList.get(j).alignment;
        // Size and Angle
        // multiply by 100 = convert to user coordinate units
        float textSize = 100f * pltD.pltTextAList.get(j).txtSize;
        float txtAngleDegr = pltD.pltTextAList.get(j).txtAngle;
        float txtAngleRad = (float)Math.toRadians(txtAngleDegr);
        // position
        i1 = pt.i1;
        i2 = pt.i2;
        int ix1 = Math.round((float)i1*xScale) -xScale0;
        int iy1 = compDim.height -(Math.round((float)i2*yScale) -yScale0);
        // correct y-position for the scaling
        iy1 = (int)Math.round((double)iy1/scaleXY);
        // scale factor to align texts
        float scaleAlignX = xScale;
        float scaleAlignY = yScale/(float)scaleXY;

        PrintFormula(g2D,txt,ix1,iy1,
                             isFormula,align,textSize,txtAngleRad,
                             fi, scaleAlignX, scaleAlignY);
        } // if txt.length()>0
      } // for j
    //To scale back to original
    g2D.setTransform(original);
    } // paintDiagram
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="PrintFormula">
private void PrintFormula (java.awt.Graphics2D g2D,
                            String textLine, int ix, int iy,
                            boolean isFormula0, int align,
                            float tSize, float txtAngleRad,
                            FontInfo fi,
                            float scaleAlignX, float scaleAlignY) {
    int n0 = textLine.length();
    if (n0 <= 0 || textLine.trim().length() <= 0) {return;}
    int ix1 = ix;
    int iy1 = iy;
    // size
    float normalSize = tSize;
    float subSize = 0.9f * tSize;
    fi.txtSize = tSize;
    if (normalSize != fi.oldTxtSize) {changeFont(g2D, fi); fi.oldTxtSize = normalSize;}

    // ------------------------------------------
    // ----  initial adjustments and checks  ----

    if (isFormula0) {
        if (textLine.indexOf('~')>=0) {textLine = textLine.replace('~', '°');}
        if (textLine.indexOf('$')>=0)  {textLine = textLine.replace('$', 'µ');}
        if (textLine.indexOf('^')>=0) {textLine = textLine.replace('^', 'Δ');}
    }

    // ----  Check if the text is really a chemical formula
    //       does TxtLine contain only numbers (and/or: +-.)?
    boolean isFormula = isFormula0;
    int i;
    if (isFormula0) {
        boolean onlyDigits = true; int j;
        for (i = 0; i <n0; i++) {
          j = textLine.charAt(i);
             // > 9 or <0   and
          if((j>57 || j<48) &&
           // not .       +        -     unicode en dash or minus
              j!=46 && j!=43 && j!=45 && j!='\u2013' && j!='\u2212')
                {onlyDigits = false;}
        } //for i
        // does TxtLine NOT contain numbers or `' +-?
        // (a chemical formula must contain numbers in sub-indeces etc)
        boolean notDigits = true;
        for (i = 0; i <n0; i++) {
            j = textLine.charAt(i);
            if (j >=48 && j <=57) {notDigits = false;}
            if (j ==39 || j ==96) {notDigits = false;}
            if (j ==36 || j ==94 || j ==126) {notDigits = false;}
            if (j ==43 || j ==45 || j=='\u2013' || j=='\u2212') {notDigits = false;}
        } //for i
        if (onlyDigits || notDigits) {isFormula = false;}
    } // isFormula0

    // ----  Get super/sub-scripts
    float[] d; int n = n0;
    if (isFormula) {
        ChemFormula cf = new ChemFormula(textLine, new float[1]);
        chemF(cf);
        textLine = cf.t.toString();
        n = textLine.length();
        d = new float[n];
        System.arraycopy(cf.d, 0, d, 0, n);
        isFormula = false;
        for (i=0;i<n;i++) {if(Math.abs(d[i])>0.01f) {isFormula = true;} }
    } // isFormula
    else {d = new float[1]; d[0]=0f;} //otherwise the compiler complains that
                                      // d[] might not have been initialised

    // --------------------------------
    // ----  Draw the text String  ----

    // ---- Get starting position to plot the text: ix1 and iy1
    float shift; float q;
    // --- align horizontally: -1=Left;  0=Center;  +1=Right.
    //     note that for chemical formulas the x-position is already
    //     adjusted in procedure "sym"
    if(align != -1) { // if Not left align
        float w =1f;  if(align == 0) {w = 0.5f;} // center
        fi.rect = fi.fm.getStringBounds(textLine, g2D);
        float txtWidth = (float)fi.rect.getWidth();
        float startWidth = (float)n * tSize;
        q = startWidth * scaleAlignX;
        shift = q - txtWidth;
        if (Math.abs(shift) > 0.01f) {
                ix1 = ix1 + Math.round(w*shift);
        } //shift !=0
    } //if Not left align
    // --- align vertically
    //     is the target vertical size of the text > a displayed "H"'s width?
    q = tSize * scaleAlignY;
    shift = q - fi.width_H;
    //Note that "y" increases downwards; zero = upper left corner
    if(Math.abs(shift) > 0.01f) {
        iy1 = iy1 - Math.round(shift/2f);
    } // if shift !=0

    // ---- Rotate
    if(Math.abs(txtAngleRad)>0.001) {g2D.rotate(-txtAngleRad, ix, iy);}
    // ---- Draw String
    float newSize;
    if (!isFormula) {
        newSize = normalSize;
        if (newSize != fi.oldTxtSize) {
            fi.txtSize = newSize;
            changeFont (g2D, fi);
            fi.oldTxtSize=newSize;
        }
        g2D.drawString(textLine, ix1, iy1);
    } else { // isFormula
        float d0 = d[0];
        int i0=0;
        int ix2 = ix1;
        int iy2;
        for(i=1;i<n;i++) {
            if(d[i]!=d0) {
                if(Math.abs(d0)>0.001f) {newSize = subSize;} else {newSize = normalSize;}
                if(newSize != fi.oldTxtSize)
                    {fi.txtSize = newSize;
                    changeFont (g2D, fi);
                    fi.oldTxtSize=newSize;}
                iy2 = iy1 - Math.round(d0 * fi.width_H);
                String txt = textLine.substring(i0,i);
                g2D.drawString(txt, ix2,iy2);
                fi.fm = g2D.getFontMetrics();
                fi.rect = fi.fm.getStringBounds(txt, g2D);
                int txtW = Math.round((float)fi.rect.getWidth());
                ix2 = ix2 + txtW;
                i0 = i; d0 = d[i];
                } // if d[] is changed
        } //for i
        if (Math.abs(d0)>0.001f) {newSize = subSize;}
        else {newSize = normalSize;}
        if (newSize != fi.oldTxtSize)
            {fi.txtSize = newSize;
             changeFont (g2D, fi);
             fi.oldTxtSize=newSize;}
        iy2 = iy1 - Math.round(d0 * fi.width_H);
        String txt = textLine.substring(i0,n);
        g2D.drawString(txt, ix2,iy2);
    } // isFormula
    // ---- Rotate back
    if(Math.abs(txtAngleRad)>0.001) {g2D.rotate(txtAngleRad, ix, iy);}
}// PrintFormula
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="changeFont">
/**  Changes the size of the font of a graphics context (g2D).
 * Returns information on the new font in an instance of FontInfo.
 * @param g2D Graphics2D
 * @param fi FontInfo
 * @return FontInfo with new calculated width_H (the width of a capital letter "H") */
private FontInfo changeFont (java.awt.Graphics2D g2D, FontInfo fi) {
    //The point size of the font is changed depending on
    //  the Dimension of the component where the text is displayed.
    float txtSizePts = fi.txtSize * fi.fontScale;
    int pts =Math.round(Math.max(4f, Math.min(76f,txtSizePts)));
    fi.f = g2D.getFont();
    fi.f = new java.awt.Font(fi.f.getFontName(), fi.f.getStyle(), pts);
    g2D.setFont(fi.f);
    //Get the FontMetrics
    fi.fm = g2D.getFontMetrics();
    //Get the size of an upper case "H"
    fi.rect = fi.fm.getStringBounds("H", g2D);
    // Note: stringWidth made StackOverflowError with an earlier version of jVectClipboard
    //    width_H = (float)fm.stringWidth("H");
    fi.width_H = (float)fi.rect.getWidth();
    return fi;
}// changeFont
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="chemF">
/** Purpose: given a "chemical formula" in StringBuffer <b>t</b> it returns
 * an array <b>d</b> indicating which characters are super- or
 * sub-scripted and a new (possibly modified) <b>t</b>.
 * Both <b>t</b> and <b>d</b> are stored for input
 * and output in objects of class ChemFormula. Note that <b>t</b> may
 * be changed, e.g. from "Fe+2" to "Fe2+".
 * <p>
 * Stores in array <b>d</b> the vertical displacement of each
 * character (in units of character height).  In the case of HCO3-, for example,
 * d(1..3)=0, d(4)=-0.4, d(5)=+0.4
 * <p>
 * Not only chemical formulas, but this method also "understands" math
 * formulas: x'2`, v`i', log`10', 1.35'.`10'-3`
 * <p>
 * For aqueous ions, the electric charge may be written as either a space and
 * the charge (Fe 3+, CO3 2-, Al(OH)2 +), or as a sign followed by a number
 * (Fe+3, CO3-2, Al(OH)2+). For charges of +-1: with or without a space
 * sepparating the name from the charge (Na+, Cl-, HCO3- or Na +, Cl -, HCO3 -).
 * <p>
 * Within this method, <b>t</b> may be changed, so that on return:
 * <ul><li>formulas Fe+3, CO3-2 are converted to Fe3+ and CO32-
 *  <li>math formulas: x'2`, v`i', log`10', 1.35'.`10'-3`
 *      are stripped of "`" and "'"
 *  <li>@ is used to force next character into base line, and
 *      the @ is removed (mostly used for names containing digits and
 *      some math formulas)</ul>
 * <p>
 * To test:<br>
 * <pre>String txt = "Fe(CO3)2-";
 * System.out.println("txt = \""+txt+"\","+
 *   " new txt = \""+chemF(new ChemFormula(txt, new float[1])).t.toString()+"\","+
 *   " d="+Arrays.toString(chemF(new ChemFormula(txt, new float[1])).d));</pre></p>
 *
 * <p>Version: 2013-Apr-03.
 * 
 * @param cf ChemFormula
 * @return new instance of ChemFormula with d[] of super- subscript
 * positions for each character and a modified StingBuffer t. */
static void chemF(ChemFormula cf) {
int act; // list of actions:
    final int REMOVE_CHAR = 0;
    final int DOWN = 1;
    final int NONE = 2;
    final int UP = 3;
    final int CONT = 4;
    final int END = 6;
int nchr; char t1; char t2; int i;
float dm; float df;
final char M1='\u2013'; // Unicode En Dash
final char M2='\u2212'; // Minus Sign = \u2212
// The characters in the StringBuffer are stored in "int" variables
// Last, Next-to-last, Next-to-Next-to-last
//                          ow (present character)
//                                  Next, Next-Next, Next-Next-Next
int L; int nL; int nnL; int now; int n; int n2; int n3;
// ASCII Table:
// -----------------------------------------------------------------------------
// 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57
//     !  "  #  $  %  &  '  (  )  *  +  ,  -  .  /  0  1  2  3  4  5  6  7  8  9
// -----------------------------------------------------------------------------
// 58 59 60 61 62 63 64 65 66 67 68 69 70 71 72 73 74 75 76 77 78 79 80 81 82 83
//  :  ;  <  =  >  ?  @  A  B  C  D  E  F  G  H  I  J  K  L  M  N  O  P  Q  R  S
// -----------------------------------------------------------------------------
// 84 85 86 87 88 89 90 91 92 93 94 95 96 97 98 99 00 01 02 03 04 05 06 07 08 09
//  T  U  V  W  X  Y  Z  [  \  ]  ^  _  `  a  b  c  d  e  f  g  h  i  j  k  n  m
// -----------------------------------------------------------------------------
//110 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26
//  n  o  p  q  r  s  t  u  v  w  x  y  z  {  |  }  ~
// -----------------------------------------------------------------------------
// If the string is too short: do nothing
StringBuffer t = new StringBuffer();
t.append(cf.t);
L = t.toString().trim().length();
if(L <= 1) {return;}
// Remove trailing space from the StringBuffer
L = t.length();  // total length
nchr = Util.rTrim(t.toString()).length(); // length without trailing space
if(L > nchr) {t.delete(nchr, L);}
// create d and initialize it
float[] d = new float[nchr];
for(i = 0; i < d.length; i++) {d[i] = 0f;}
// set the non-existing characters to "space"
//     Last, Next-to-last, Next-to-Next-to-last
L = 32; nL = 32; nnL = 32;
// get the ASCII code
now = t.charAt(0);
// get the following characters
n = 32;  if (nchr >=2) {n  = t.charAt(1);} // Next
n2 = 32; if (nchr >=3) {n2 = t.charAt(2);} // Next-Next
// dm (Displacement-Math) for '` sequences: 1.5'.`10'-12`, log p`CO2'
dm = 0f;
// df (Displacement-Formulas) in chemical formulas: CO3 2-, Fe 3+, Cl-, Na+
df = 0f;
//
// -----------------------------
//    Main Loop
//
i = 0;
main_do_loop:
do {
  n3 = 32;
  if(nchr >=(i + 4)) {n3 = t.charAt(i + 3);} //Next-Next-Next
  // Because a chemical formula only contains supersripts at the end
  // (the electric charge), a superscrip character marks the end of a formula.
  // Set df=0 after superscript for characters that are not 0:9 + or -
  if(df > 0.001f) { //     (if last was supercript, i.e. df>0)
    if (now <43 || now >57 || now ==44 || now ==46 || now ==47) //not +- 0:9
         {df = 0f;}
  } //df > 0.001

  checks: { // named block of statements to be used with "break"
    // ---------------
    //   for @
    // ---------------
    //   if the character is @, do nothing; if last char. was @, treat "now" as a
    //   "normal" char. (and therefore, if last char. was @, and "now" is a space,
    //   leave a blank space);
    if(now ==64 && L !=64) { act =REMOVE_CHAR; break checks;}
    if(L ==64) { // @
      if(now ==32) {act =CONT;} else {act =NONE;}
      break checks;
    } // Last=@
    // ---------------
    //   for Ctrl-B (backspace)
    // ---------------
    if(now ==2) {
      now = n; n = n2; n2 = n3;
      act =END; break checks;
    } // now = backspace
    // ---------------
    //   for ' or `
    // ---------------
    if(now ==39 || now ==96) {
      //     for  '`, `' sequences: change the value of variable dm.
      //     if it is a ' and we are writing a "normal"-line, and next is either
      //     blank or s (like it's or it's.): write it normal
      if(now ==39 && dm ==0
              && ( (n ==32 && L ==115)
              || (n ==115 && (n2==32||n2==46||n2==44||n2==58||n2==59||n2==63
                        ||n2==33||n2==41||n2==93)) ) )
                {act =CONT; break checks;}
      if(now ==39) {dm = dm + 0.5f;}
      if(now ==96) {dm = dm - 0.5f;}
      act =REMOVE_CHAR; break checks;
    } // now = ' or `
    // ---------------
    //   for BLANK (space)
    // ---------------
    // Decide if the blank must be printed or not.
    // In front of an electric charge: do not print (like CO3 2-, etc)
    if(now ==32) {
      // if next char. is not a digit (1:9) and not (+-), make a normal blank
      if((n <49 && (n !=43 && n !=45 &&n!=M1&&n!=M2)) || n >57) {act =NONE; break checks;}
      // Space and next is a digit or +-:
      // if next is (+-) and last (+-) make a normal blank
      if((n ==43 || n ==45 || n==M1||n==M2)
              && (L ==43 || L ==45 ||L==M1||L==M2)) {act =NONE; break checks;}
      // if the 2nd-next is letter (A-Z, a-z), make a normal blank
      if((n2 >=65 && n2 <=90) || (n2 >=97 && n2 <=122)) {act =NONE; break checks;}
      // if next is a digit (1-9)
      if(n >=49 && n <=57) { // 1:9
        // and 2nd-next is also a digit and 3rd-next is +-  " 12+"
        if(n2 >=48 && n2 <=57) { // 0:9
            if (n3 !=43 && n3 !=45 &&n3!=M1&&n3!=M2) {act =NONE; break checks;} // n3=+:-
        } // n2=0:9
        // if next is (1-9) and 2nd-next is not either + or -, make a blank
        else if (n2 !=43 && n2 !=45 &&n2!=M1&&n2!=M2) {act =NONE; break checks;}
      } // n=1:9
      // if last char. was blank,  make a blank
      if(L ==32) {act =NONE; break checks;} // 110 //
      // if last char. was neither a letter (A-Z,a-z) nor a digit (0:9),
      // and was not )]}"'>, make a blank
      if((L <48 || L >122 || (L >=58 && L <=64) || (L >=91 && L<= 96))
        && (L !=41 && L !=93 && L !=125 && L !=62 && L !=34 && L !=39))
                    {act =NONE; break checks;}
      // Blanks followed either by + or -, or by a digit (1-9) and a + or -
      // and preceeded by either )]}"'> or a letter or a digit:
      // do not make a blank space.
      act =REMOVE_CHAR; break checks;
    } //now = 32
    // ---------------
    //   for Numbers (0:9)
    // ---------------
    // In general a digit is a subscript, except for electric charges...etc
    if(now >=48 && now <=57) {
      // if last char. is either ([{+- or an impossible chem.name,
      // then write it "normal"
      if(L !=40 && L !=91 && L !=123 && L !=45 &&L!=M1&&L!=M2&& L !=43) { // Last not ([{+-
        if((L ==106 || L ==74) || (L ==113 || L ==81)) {act =NONE; break checks;} // jJ qQ
        if((L ==120 || L ==88) || (L ==122 || L ==90)) {act =NONE; break checks;} // xX zZ
        //if last char. is )]} or a letter (A-Z,a-z): write it 1/2 line down
        if((L >=65 && L <=90) || (L >=97 && L <=122)) {act =DOWN; break checks;} // A-Z a-z
        if(L ==41 || L ==93 || L ==125) {act =DOWN; break checks;} // )]}
        //if last char. is (0:9 or .) 1/2 line (down or up), keep it
        if(df >0.01f && ((L >=48 && L <=57) || L ==46)) {act =CONT; break checks;}
        if(df <-0.01f && (L ==46 || (L >=48 && L <=57))) {act =CONT; break checks;}
      } // Last not ([{+-
      // it is a digit and last char. is not either of ([{+-)]} or A:Z a:z
      // is it an electric charge?
      df = 0f; //125//
      // if last char is space, and next char. is a digit and 2nd-next is a +-
      // (like: W14O41 10+)  then write it "up"
      if(L ==32 && (n >=48 && n <=57)
            && (n2 ==43 || n2 ==45 ||n2 ==M1||n2 ==M2) && now !=48) {
        //if 3rd-next char. is not (space, )]}), write it "normal"
        if(n3 !=32 && n3 !=41 && n3 !=93 && n3 !=125) {act =CONT; break checks;}  // )]}
        act =UP; break checks;
      }
      // if last is not space or next char. is not one of +-, then write it "normal"
      if(L !=32 || (n !=43 && n !=45 &&n!=M1&&n!=M2)) {act =CONT; break checks;}              // +-
      // Next is +-:
      // if 2nd-next char. is a digit or letter, write it "normal"
      if((n2 >=48 && n2 <=57) || (n2 >=65 && n2 <=90)
            || (n2 >=97 && n2 <=122)) {act =CONT; break checks;}                    // 0:9 A:Z a:z
      // if last char. was a digit or a blank, write it 1/2 line up
      // if ((L >=48 && L <=57) || L ==32) {act =UP; break checks;}
      //     act =CONT; break checks;
      act =UP; break checks;
    } // now = (0:9)
    // ---------------
    //   for Signs (+ or -)
    // ---------------
    // Decide if it is an electric charge...
    //  and convert things like: CO3-2  to: CO3 2-
    if(now ==43 || now ==45 ||now ==M1||now ==M2) {
      // First check for charges like: Fe 2+  and  W16O24 11-
      //  If last char. was a digit (2:9) written 1/2 line up, write it
      //  also 1/2 line up (as an electrical charge in H+ or Fe 3+).
      if(L >=50 && L <=57 && df >0.01f) {act =UP; break checks;}                // 2:9
      // charges like: W16O24 11-
      if((L >=48 && L <=57) && (nL >=49 && nL <=57)
              && (df >0.01f)) {act =UP; break checks;}                            // 0:9 1:9
      //is it a charge like: Fe+3 ?   ------------------------
      if(n >=49 && n <=57) {                                       // 1:9
      // it is a +- and last is not superscript and next is a number:
      // check for impossible cases:
      // if 2nd to last was a digit or a period and next a digit: 1.0E-1, 2E-3, etc
        if(((nL >=48 && nL <=57) || nL ==46) && (L ==69 || L==101)) {act =NONE; break checks;} // 09.E
        if(L ==32) {act =NONE; break checks;}                                      // space
        if((L ==106 || L ==74) || (L ==113 || L ==81)) {act =NONE; break checks;}  // jJ qQ
        if((L ==120 || L ==88) || (L ==122 || L ==90)) {act =NONE; break checks;}  // xX zZ
        // if last was 0 and 2nd to last 1 and dm>0 (for 10'-3` 10'-3.1` or 10'-36`)
        if(dm >0.01f && (L ==39 && nL ==48 && nnL ==49)
                && (n >=49 && n <=57)
                && (n2 ==46 || n2 ==96 || (n2 >=48 && n2 <=57))) {act =NONE; break checks;}
        // allowed in L: numbers, letters and )]}"'
        // 1st Line: !#$%&(*+,-./    2nd line: {|~:;<=?@     3rd Line: [\^_`
        if((L <48 && L !=41 && L !=34 && L !=39)
                || (L >122 && L !=125) || (L >=58 && L <=64 && L !=62)
                || (L >=91 && L <=96 && L !=93)) {act =NONE; break checks;}
        if((L ==41 || L ==93 || L ==125 || L ==34 || L ==39)
                && (nL ==32 || nL <48 || nL >122 || (nL >=58 && nL <=64)
                || (nL >=91 && nL <=96))) {act =NONE; break checks;}                   // )]}"'
        // allowed in n2: numbers and space )']`}
        // 1st Line: !"#$%&(*+,-./   2nd Line: except ]`}
        if((n2 <48 && n2 !=41 && n2 !=32 && n2 !=39)
                || (n2 >57 && n2 !=93 && n2 !=96 && n2 !=125)) {act =NONE; break checks;}
        //it is a +- and last is not superscript and next is a number:
        // is it a charge like:  W14O41-10 ?
        if((n >=49 && n <=57) && (n2 >=48 && n2 <=57)) {    // 1:9 0:9
          // characters allowed after the electric charge:  )]}`'@= space and backsp
            if(n3 !=32 && n3 !=2 && n3 !=41 && n3 !=93 && n3 !=125
                    && n3 !=96 && n3 !=39 && n3 !=64 && n3 !=61) {act =NONE; break checks;}
          // it is a formula like "W12O41-10"  convert to  "W12O41 10-"  and move "up"
          t1 = t.charAt(i+1); t2 = t.charAt(i+2);
          t.setCharAt(i+2, t.charAt(i));
          t.setCharAt(i+1, t2);
          t.setCharAt(i, t1);
          int j1 = n;  int j2 = n2;
          n2 = now;
          n = j2; now = j1;
          act =UP; break checks;
        } // 1:9 0:9
        // is it a charge like Fe+3, CO3-2 ?
        if(n >=50 && n <=57) {                                   // 2:9
          //it is a formula of type Fe+3 or CO3-2
          // convert it to Fe3+ or CO32-  and move "up"
          t1 = t.charAt(i+1);
          t.setCharAt(i+1, t.charAt(i));
          t.setCharAt(i, t1);
          int j = n; n = now; now = j;
          act =UP; break checks;
        } // 2:9
      } // 1:9
      // it is a +- and last is not superscript and:  next is not a number:
      // write it 1/2 line up (H+, Cl-, Na +, HCO3 -), except:
      //    1) if last char. was either JQXZ, or not blank-letter-digit-or-)]}, or
      //    2) if next char. is not blank or )}]
      //    3) if last char. is blank or )]}"' and next-to-last is not letter or digit
      if(L ==106 || L ==74 || L ==113 || L ==81) {act =NONE; break checks;}           // jJ qQ
      if(L ==120 || L ==88 || L ==122 || L ==90) {act =NONE; break checks;}          // xX zZ
      if(L ==32 || L ==41 || L ==93 || L ==125) {                     // space )]}
        //1st Line: !"#$%&'(*+,-./{|}~      2nd Line: :;<=>?@[\^_`
        if(nL ==32 || (nL <48 && nL !=41) || (nL >122)
                || (nL >=58 && nL <=64)
                || (nL >=91 && nL <=96 && nL !=93)) {act =NONE; break checks;}
        act =UP; break checks;
      } // space )]}
      // 1st Line: !#$%&(*+,-./{|~     2nd Line: :;<=>?@[\^_`
      if((L <48 && L !=41 && L !=34 && L !=39)
              || (L >122 && L !=125) || (L >=58 && L <=64)
              || (L >=91 && L <=96 && L !=93)) {act =NONE; break checks;}
      if(n !=32 && n !=41 && n !=93 && n !=125 && n !=61
              && n !=39 && n !=96) {act =NONE; break checks;}                             // )]}=`'
      act =UP; break checks;
    } // (+ or -)
    // ---------------
    //   for Period (.)
    // ---------------
    if (now ==46) {
      // if last char was a digit (0:9) written 1/2 line (down or up)
      // and next char is also a digit, continue 1/2 line (down or up)
      if((L >=48 && L <=57) && (n >=48 && n <=57)) {act =CONT; break checks;} // 0:9
      act =NONE; break checks;
    } // (.)
    act =NONE;
  } // --------------- "checks" (end of block name)

  switch_action:
  switch (act) {
    case REMOVE_CHAR:
        // ---------------
        //   take OUT present character //150//
        if(i <nchr) {t.deleteCharAt(i);}
        nchr = nchr - 1;
        nnL = nL; nL = L; L = now; now = n; n = n2; n2 = n3;
        continue; // main_do_loop;
    case DOWN:
        df = -0.4f;       // 170 //
        break; // switch_action;
    case NONE:
        df = 0.0f;        // 180 //
        break; // switch_action;
    case UP:
        df = 0.4f;        // 190 //
        break; // switch_action;
    case CONT:
    case END:
    default:
  } // switch_action

  if(act != END) {// - - - - - - - - - - - - - - - - - - - // 200 //
    nnL = nL;
    nL = L;
    L = now;
    now = n;
    n = n2;
    n2 = n3;
  }

  d[i] = dm + df; //201//
  i = i + 1;
} while (i < nchr); // main do-loop

// finished
// store results in ChemFormula cf
if(cf.t.length()>0) {cf.t.delete(0, cf.t.length());}
cf.t.append(t);
cf.d = new float[nchr];
System.arraycopy(d, 0, cf.d, 0, nchr);
// return;
} // ChemFormula chemF
//</editor-fold>

} 