package lib.kemi.graph_lib;

import lib.common.Util;

/** Class used as a plotter driver. The plot is to be displayed in a
 * Java Swing jPannel component when the "paint" method is invoked
 * by Java. The methods in this class also save the graphic information
 * in a plot file so that it can be displayed in another occasion.
 *
 * To display the plot using the "paint" method, the methods in this
 * class store the graphic information in a "PltData" instance.
 * When finished, after calling "end()", you call the
 * "DiagrPaintUtility.paintDiagram" method from within the "paint" method
 * of the jPannel usung the PltData instance as argument.
 * The "paintDiagram" method will read the "PltData" instance and
 * re-paint the jPannel when needed.
 *
 * The output plot file is a text file that may later be displayed by the
 * user, or for example sent by e-mail, etc.
 *
 * The graphic information is a vector format ("go to", "draw to",
 * "change colour", "display a text"). The units are to be thought as "cm",
 * but during the painting events they are scaled to the size of the
 * painted component.
 *
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
public class GraphLib {
// display text outline? (for debug purposes, if sketch = false)
private static final boolean OUTLINE_DRAW = false;
// this should be = true; that is, save texts in plot-files both as
// line sketches and font information
private static final boolean CHARACTER_SKETCH = true;
/** true if a text is being sketched, but texts are painted with a font
 * (then the line sketches should not be displayed) */
private boolean sketching;
/** true if texts are to be displayed on the JPanel using a font, that is,
 * not by using the line-sketches stored in the plot file.<br>
 * If true, text sketch information is not stored in the PlotStep ArrayList
 * of the PltData instance */
private boolean textWithFonts;
// ----
private PltData pd;
/** the current value for the colour for drawing on the screen */
private int screenColour;
/** the current value for the plotter pen */
private int plotPen;
private boolean isFormula;
private String label;
private boolean save;
/** The output file */
private java.io.Writer w;
private double xL =1; private double yL =1;
private double xI =0; private double yI =0;
private double sizeSym =0;
private java.awt.geom.Rectangle2D.Double clippingArea;
private int i2Last = Integer.MIN_VALUE;
private int i3Last = Integer.MIN_VALUE;
private static java.util.Locale engl = java.util.Locale.ENGLISH;
/** New-line character(s) to substitute "\n" */
private static final String nl = System.getProperty("line.separator");
// ---- may be used to set the steps in the axes
public double stpXI =0;
public double stpYI =0;
// --------------------------

/**  Create an instance */
public void GraphLib() {
    save = false; xL =1; yL =1; xI =0; yI =0; sizeSym =0;
    screenColour =1; plotPen =1; isFormula = false;
    textWithFonts = true;
    sketching = false;
} //constructor

//<editor-fold defaultstate="collapsed" desc="class PltData">
    /** Class to store data from a "plt" file, and other data needed,
     * for the Paint method */
    public static class PltData {
    /** the plot file name from which the data in this instance was read. */
    public String pltFile_Name;
    /** last modification date for the plot file from which the data in this instance was read. */
    public java.util.Date fileLastModified;
    /** the plot steps (move to / draw to) */
    public java.util.ArrayList<PlotStep> pltFileAList = new java.util.ArrayList<PlotStep>();;
    /** the texts in the plot */
    public java.util.ArrayList<PlotText> pltTextAList = new java.util.ArrayList<PlotText>();
    /** the maximum values for x and y in user coordinates (0.1 mm) */
    public java.awt.Point userSpaceMax = new java.awt.Point(Integer.MIN_VALUE,Integer.MIN_VALUE);
    /** the minimum values for x and y in user coordinates (0.1 mm) */
    public java.awt.Point userSpaceMin = new java.awt.Point(Integer.MAX_VALUE,Integer.MAX_VALUE);
    public boolean axisInfo = false;
    public float xAxisL, yAxisL;
    public float xAxis0, yAxis0;
    public float xAxisMin, yAxisMin;
    public float xAxisMax, yAxisMax;
    //these are used when displaying the xy-label
    // when the user clicks the mouse button on a diagram
    public float xAxisMin_true;
    public float xAxisMax_true;
    public float yAxisMin_true;
    public float yAxisMax_true;
    public float xAxisScale;
    public float yAxisScale;

  //<editor-fold defaultstate="collapsed" desc="class PlotStep">
  /** Class to store data to perform either a "move to" or a "draw to" */
  public static class PlotStep {
      public int i0 = 0;
      public int i1 = 0;
      public int i2 = 0;
      /**  Data to perform either a "move to" or a "draw to"
       * @param i0 int =0 for "move to" and =1 for "draw to"
       * @param i1 int the new x-position
       * @param i2 int the new y-position */
      public PlotStep(int i0, int i1, int i2) {this.i0 = i0; this.i1 = i1; this.i2 = i2;}
      }// class PlotStep
    //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="class PlotText">
  /** Class to store data to draw a text string */
  public static class PlotText {
      public int i1 = 0;
      public int i2 = 0;
      public boolean isFormula = false;
      /** -1=Left  0=center  +1=right */
      public int alignment = 0;
      public float txtSize = 0.4f;
      public float txtAngle = 0f;
      public String txtLine;
      public int pen = 5;
      public int color = 1;
      /** Data to draw a text string
       * @param i1 int the new x-position
       * @param i2 int the new y-position
       * @param isFormula boolean
       * @param align int alignment: -1=Left,  0=center,  +1=right.
       * @param txtSize float
       * @param txtAngle float
       * @param txtLine String
       * @param pen int: the pen number
       * @param clr int: the colour number */
      public PlotText(int i1, int i2, boolean isFormula, int align,
                  float txtSize, float txtAngle,
                  String txtLine, int pen, int clr)
          {this.i1=i1; this.i2=i2; this.isFormula=isFormula;
           alignment = Math.max(-1, Math.min(align, 1));
           this.txtSize=txtSize; this.txtAngle=txtAngle; this.txtLine=txtLine;
           this.pen=pen; color=clr;}
      }// class PlotText
    //</editor-fold>

} // class PltData
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="start(PltData, java.io.File)">
/** Open the output plot file; set the PltData for storing the graphic
 * @param pD where the graphic information will be stored
 * for painting events. If null, the diagram will neither be saved
 * nor displayed
 * @param plotFile for storing graphic information, it may be "null".
 * If an error occurs while opening the file for writing, an exception is
 * thrown, the file will not be saved, but the diagram will be displayed.
 * @param txtWithFonts true if texts are to be displayed on the JPanel using
 * a font, that is, not by using the line-sketches stored in the plot file.<br>
 * If true, text sketch information is not stored in the PlotStep ArrayList
 * of the PltData instance.
 * @throws lib.kemi.graph_lib.GraphLib.OpenPlotFileException  */
public void start(PltData pD, java.io.File plotFile, boolean txtWithFonts)
        throws WritePlotFileException {
    if(pD == null) {return;}
    this.pd = pD;
    this.textWithFonts = txtWithFonts; 
    String msg = null;
    if(plotFile != null && plotFile.getName().length()>0) {
        w = null;
        try{
            w = new java.io.BufferedWriter(
                    new java.io.OutputStreamWriter(new java.io.FileOutputStream(plotFile),"UTF8"));
            pd.pltFile_Name = plotFile.getPath();
            save = true;
        } catch (java.io.IOException ex) {
            msg = "Error: \""+ex.toString()+"\","+nl+
                           "   in Graphics Library,"+nl+
                           "   while opening output file" +nl+
                           "   \""+plotFile.getPath()+"\"";
            pd.pltFile_Name = null;
            save = false;}
    } else {pd.pltFile_Name = null; save = false;}
    if (msg != null) {throw new WritePlotFileException(msg);}
    //return;
} //start(pD, plotFile)
public static class WritePlotFileException extends Exception {
    public WritePlotFileException() {}
    public WritePlotFileException(String txt) {super(txt);}
    } //openPlotFileException

//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="end()">
/** Close the output plot file;
 * Checks that the "user graphic space" is not zero. */
public void end() throws WritePlotFileException {
    // check the "UserSpace" dimensions
    if(pd.userSpaceMax.x == Integer.MIN_VALUE) {pd.userSpaceMax.x = 2100;}
    if(pd.userSpaceMin.x == Integer.MAX_VALUE) {pd.userSpaceMin.x = 0;}
    if(pd.userSpaceMax.y == Integer.MIN_VALUE) {pd.userSpaceMax.y = 1500;}
    if(pd.userSpaceMin.y == Integer.MAX_VALUE) {pd.userSpaceMin.y = 0;}
    float xtra = 0.02f; //add 1% extra space all around
    float userSpace_w = Math.abs(pd.userSpaceMax.x - pd.userSpaceMin.x);
    int xShift = Math.round(Math.max( (100f-userSpace_w)/2f, userSpace_w*xtra ) );
    pd.userSpaceMax.x = pd.userSpaceMax.x + xShift;
    pd.userSpaceMin.x = pd.userSpaceMin.x - xShift;
    userSpace_w = Math.abs(pd.userSpaceMax.y - pd.userSpaceMin.y);
    int yShift = Math.round(Math.max( (100f-userSpace_w)/2f, userSpace_w*xtra ) );
    pd.userSpaceMax.y = pd.userSpaceMax.y + yShift;
    pd.userSpaceMin.y = pd.userSpaceMin.y - yShift;
    try{if(label != null && save) {w.write("0   0   0 "+label+nl);}}
    catch(Exception ex) {
        save = false;
        pd.pltFile_Name = null;
        throw new WritePlotFileException("Error: \""+ex.getMessage()+"\","+nl+
                           "   in Graphics Library,"+nl+
                           "   while closing output file" +nl+
                           "   \""+pd.pltFile_Name+"\"");
    }
    // close output file
    if(w != null) {
        save = false;
        try{w.flush(); w.close();}
        catch(Exception ex) {
            pd.pltFile_Name = null;
            throw new WritePlotFileException("Error: \""+ex.getMessage()+"\","+nl+
                           "   in Graphics Library,"+nl+
                           "   while closing output file" +nl+
                           "   \""+pd.pltFile_Name+"\"");
        }
    }
    //return;
} // end()
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="setLabel">
/**  A comment to be written to the output file
 * @param txt String */
public void setLabel(String txt) throws WritePlotFileException {
    if(label != null && save) {
        try{w.write("0   0   0 "+label+nl);}
        catch(Exception ex) {
            save = false;
            pd.pltFile_Name = null;
            throw new WritePlotFileException("Error: \""+ex.getMessage()+"\","+nl+
                           "   in Graphics Library \"setLabel\","+nl+
                           "   while writing output file" +nl+
                           "   \""+pd.pltFile_Name+"\"");
        }
    }
    label = txt;
    //return;
} //setLabel(txt)
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="setPen">
/**  Set either the "plotter" pen number or the screen colour for
 * further painting.
 * @param pen int larger than zero to set the plotter pen number;
 * zero or negative to set the screen colour */
public void setPen(int pen) throws WritePlotFileException {
    int n; int i;
    if(pen >=0) {n=8;  plotPen = Math.min(pen,9999); i=plotPen;}
    else {n=5;  screenColour = Math.min(-pen,9999); i=screenColour;}
    pd.pltFileAList.add(new PltData.PlotStep(n,i,0));
    if(save) {
        try{
        if(label != null) {
                w.write(String.format("%1d%4d     %s%n", n,i,label));
                label = null;
        } else {w.write(String.format("%1d%4d%n", n,i));}
        w.flush();
        } catch (Exception ex) {
            save = false;
            pd.pltFile_Name = null;
            throw new WritePlotFileException("Error: \""+ex.getMessage()+"\","+nl+
                           "   in Graphics Library \"setPen\","+nl+
                           "   while writing output file" +nl+
                           "   \""+pd.pltFile_Name+"\"");
        }
        } //if save
} // setPen(pen)
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="sym">
/**  @param b booleat true if all following calls to "sym" should handle the
 * text strings to be plotted (painted) as chemical formulas.
 * False to handle all subsequent strings as plain texts. */
public void setIsFormula(boolean b) {isFormula = b;}

/** Display (and save to the output plot file) a text string.
 * @param xP float: x-position
 * @param yP float: y-position
 * @param tHeight float: size of text
 * @param t String: text to be displayed
 * @param angle float, in degrees
 * @param alignment int: -1=left 0=center +1=right
 * @param axesCoord boolean: if true the (x,y)-values are taken
 * to be in the same scale as previously drawm axes */
public void sym(float xP, float yP, float tHeight, String t, float angle,
        int alignment, boolean axesCoord) throws WritePlotFileException {
    sym((double)xP, (double)yP, (double)tHeight, t, (double)angle,
            alignment, axesCoord);
} //sym(float)
/** Display (and save to the output plot file) a text string.
 * @param xP double: x-position
 * @param yP double: y-position
 * @param tHeight double: size of text (between 0.05 and 99 cm)
 * @param t String: text to be displayed
 * @param angle double, in degrees,
 * internally it will be converted to a value between +180 to -180
 * @param alignment int: -1=left 0=center +1=right
 * @param axesCoord boolean: if true the (x,y)-values are taken
 * to be in the same scale as previously drawm axes */
public void sym(double xP, double yP, double tHeight, String t, double angle,
        int alignment, boolean axesCoord) throws WritePlotFileException {
    if(t == null) {return;}
    if(t.trim().length() < 1) {return;}
    if(Math.abs(tHeight) < 0.05) {return;} // less than 0.5 mm size?
    // --------------------------
    // alignment: -1=Left  0=center  +1=right
    int align = Math.max(-1, Math.min(alignment, 1));
    String txt;
    //
    sizeSym = Math.min(99,Math.abs(tHeight));
    double angleDegr = angle;
    //get angle between +180 and -180
    while (angleDegr>360) {angleDegr=angleDegr-360;}
    while (angleDegr<-360) {angleDegr=angleDegr+360;}
    if(angleDegr>180) {angleDegr=angleDegr-360;}
    if(angleDegr<-180) {angleDegr=angleDegr+360;}
    double angleR = Math.toRadians(angleDegr);
    double sCos = sizeSym*Math.cos(angleR);
    double sSin = sizeSym*Math.sin(angleR);
    double x0 = xP; double y0 = yP;
    if(axesCoord) {
        if(xL ==0) {xL = 1;}
        if(yL ==0) {yL = 1;}
        x0 = x0 * xL + xI;  y0 = y0 * yL + yI;
    } //axesCoord

    //---- Remove white space at the start of the String
    //     by moving the text to the right
    int i;
    int len = t.length();
    for(i=0; i<len; i++) {
        if(!Character.isWhitespace(t.charAt(i))) {break;}
    }
    i = Math.min(i, len-1);
    if(i>0) {
        t = t.substring(i);
        len = t.length();
        //move the starting position (all angles are between +180 and -180)
        x0 = x0 + i*sCos;
        y0 = y0 + i*sSin;}
    int isFormulaLen = len;
    if(isFormula) {isFormulaLen = isFormulaLength(t);}

    //---- If aligned center or right, remove space in formulas
    //     by moving the text to the right
    float diff = (float)(len - isFormulaLen)+0.00001f;
    if(align != -1 && isFormula && diff > 0.1) {
      if(align == 0) {diff = diff/2f;}
      x0 = x0 + diff*sCos;
      y0 = y0 + diff*sSin;
    } // if align != -1

    //---- Write information about the text on the output file
    if(save) {
        String c; String al;
        if(isFormula) {c="C";} else {c=" ";}
        if(align == -1) {al="L";} else if(align == +1) {al="R";} else {al="C";}
        txt = String.format(engl,
            "TextBegin"+c+" size=%7.2f cm, angle=%8.2f, alignment: "+al,sizeSym,angleDegr);
        setLabel(txt);}
    moveToDrawTo(x0, y0, 0);
    if(save) {setLabel(t);} //write the text
    int l;
    if(isFormula) {l = isFormulaLen;} else {l = len;}
    if(CHARACTER_SKETCH) {
        sketch(x0,y0,t, angleDegr);
    }
    if(!CHARACTER_SKETCH || OUTLINE_DRAW) {
        int m;
        if(OUTLINE_DRAW) {m = 1;} else {m = 0;}     //"draw" outline?
        moveToDrawTo(x0, y0, 0);
        moveToDrawTo(x0+l*sCos, y0+l*sSin, m);
        moveToDrawTo(x0+l*sCos-sSin, y0+l*sSin+sCos, m);
        moveToDrawTo(x0-sSin, y0+sCos, m);
        moveToDrawTo(x0, y0, m);
    }

    if(save) {
        setLabel("TextEnd");
        moveToDrawTo(0, 0, 0);
    }

    //---- save the text in the PltData arrayList
    int i2 = Math.round((float)(x0*100));
    int i3 = Math.round((float)(y0*100));
    i2 = Math.max(-999, Math.min(i2, 9999));
    i3 = Math.max(-999, Math.min(i3, 9999));
    pd.pltTextAList.add(new PltData.PlotText(i2, i3, isFormula, align,
                            (float)sizeSym, (float)angleDegr, t, plotPen, screenColour));
    //return;
} // sym(x,y,h,txt,a)
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="sketch">
//<editor-fold defaultstate="collapsed" desc="sketch data">
// the integers in SK (QnnNN) incorporate three values: the last two digits (NN),
// the previous 2 digits (nn) and an optional flag (Q)
// The last two (NN) are the x-coordinate of the character stroke;
// the previous two digits (nn) are the y-coordinate, and the flag (Q) is
// either "0"= draw to (x,y), "1"= move to (x,y), and
// "2" = draw to (x,y) and end of character
private static final int[] SK =
{10010,9040,70,13060,23020, //A [0-4
 10010,9010,9060,8070,6070,5060,5010,15060,4070,1070,60,20010,  //B [5-16
 18070,9060,9020,8010,1010,20,60,21070, //C [17-24
 10010,9010,9050,7070,2070,50,20010, //D [25-31
 10070,10,9010,9070,15010,25050,  //E [32-37
 10010,9010,9070,15010,25050, //F [38-42
 18070,9060,9020,8010,1010,20,60,1070,4070,24050, //G [43-52
 19010,10,19070,70,15010,25070,  //H [53-58
 19030,9050,19040,40,10030,20050,  //I [59-64
 19060,1060,50,20,1010,22010,   19010,10,19060,5010,20070, //J [65-70  K [71-75
 19010,10,20070,   10010,9010,5040,9070,20070,  //L [76-78  M [79-83
 10010,9010,70,29070, //N [84-87
 10020,1010,8010,9020,9060,8070,1070,60,20020, //O [88-96
 10010,9010,9060,8070,6070,5060,25010, //P [97-103
 10020,1010,8010,9020,9060,8070,1070,60,20,12050,20070, //Q [104-114
 10010,9010,9060,8070,6070,5060,5010,15050,20070, //R [115-123
 11010,20,60,1070,4070,5060,5020,6010,8010,9020,9060,28070, //S [124-135
 19010,9070,19040,20040,  19010,1010,20,60,1070,29070, //T [136-139  U [140-145
 19010,40,29070,   19010,10,4040,70,29070, //W [146-148  V [149-153
 19010,70,10010,29070,   19010,5040,9070,15040,20040, //X [154-157  Y [158-162
 19010,9070,10,20070, // Z [163-166
 12070,40,20,1010,5010,6035,6070,20070, //a [167-174
 16010,6060,5070,1070,60,10,29910,  //b [175-181
 15070,6060,6020,5010,1010,20,20070,  //c [182-188
 16070,6020,5010,1010,20,70,29970,  //d [189-195
 13010,3070,5070,6060,6020,5010,1010,20,20070,  //e [196-204
 10020,7520,9035,9055,7570,14010,24055,   //f [205-211
-12010,-3060,-2070,5070,6060,6020,5010,2010,1020,1060,22070,  //g [212-222
 10010,9910,16010,6060,5070,20070,  //h [223-228
 16020,6035,35,10020,50, 18033,8037,8437,8433,28033,  //i [229-238
-12010,-3020,-3040,-2050,6050,18550,9050,9055,8555,28550,  //j [239-248
 10010,9010,13010,6050,14530,20060,  //k [249-254
 19020,9035,35,10020,20050,  //l [255-259
 10010,6010,15010,6040,40,15040,6070,20070,  //m [260-267
 10010,6010,15010,6055,5070,20070,  //n [268-273
 15070,1070,60,20,1010,5010,6020,6060,25070,  //o [274-282
-13010,6010,6060,5070,2070,1060,21010,  //p [283-289
-13070,6070,6020,5010,2010,1020,21070,   //q [290-296
 10010,6010,14510,6040,6060,25070,  //r [297-302
 11010,20,60,1070,2070,3060,3020,4010,5010,6020,6060,25070,  //s [303-314
 19030,1030,45,55,1070,16020,26060,  //t [315-321
 16020,1020,30,60,1070,26070,  16010,40,26070,  //u [322-327  v [328-330
 16010,20,3040,60,26070,  //w [331-335
 10010,6070,16010,20070,  -13000,-3010,6060,16010,21030,  //x [336-339  y [340-344
 16010,6070,10,20070,  //z [345-348
 10020,1010,8010,9020,9060,8070,1070,60,20020,  //0 [349-357
 17030,9040,40,10030,20050,  //1 [358-362
 18010,9020,9060,8070,5570,1510,10,20070,  //2 [363-370
 18010,9020,9060,8070,5570,4560,4520,14560,3570,1070,60,20,21010, //3 [371-383
 10060,9060,3010,23070,  //4 [384-387
 11010,20,60,1070,4070,5060,5010,9010,29070,  //5 [388-396
 14010,5020,5060,4070,1070,60,20,1010,8010,9020,29060,  //6 [397-407
 10020,9070,9010,28010,  //7 [408-411
 15020,4010,1010,20,60,1070,4070,5060,5020,6010,8010,9020,9060,8070,6070,25060, //8 [412-427
 10020,10020,60,1070,8070,9060,9020,8010,5010,4020,4060,25070,  //9 [428-439
 15010,25070,  12040,8040,15010,25070,  //- [440-441  + [442-445
 15020,5060,13050,7030,17050,23030,  10010,29070,  //* [446-451  / [452-453
-11050,40,2530,7030,9540,30550, //( [454-459
-11030,40,2550,7050,9540,30530, //) [460-465
 19950,9930,-1030,-21050,  -11030,-1050,9950,29930,  //[ [466-469  ] [470-473
 11520,1565,15520,25565,  //= [474-477
-11050,40,4040,5030,6040,9940,31050,  //{ [478-484
-11030,40,4040,5050,6040,9940,31030,  //} [485-491
 10030,40,1040,1030,20030,  //. [492-496
-11525,1040,1030,30,-21525, //, [497-501
-11010,2020,6020,12020,1030,1060,2070,6070,12070,21080,  //µ(micro) [502-511
 19960,7240,9950,29960,  19920,7240,9930,29920,          //' [512-515  ` [516-519
 19970,7260,9960,9970,19940,7030,9930,29940,             //" [520-527
 19033,3033,10030,35,535,530,20030,                      //! [528-534
 18010,9020,9060,8070,6070,4040,3040,10040,45,545,540,20040,//? [535-546
-12000,-22099,  //_ [547-548
 10030,40,1040,1030,30,14030,4040,5040,5030,24030,     //: [549-558
-11525,1040,1030,30,-1525,14030,4040,5040,5030,24030,  //; [559-568
 17070,4010,21070,  11010,4070,27010,                  //< [569-571  > [572-574
 13020,3060,15060,5020,17030,1030,11050,27050,         //# [575-582
 16010,8010,8030,6030,6010,10010,9070,13070,1070,1050,3050,23070, //% [583-594
 14074,550,033,1818,4030,6057,7060,8054,8040,7032,6232,20080,  //& [595-606
 17020,8530,7060,28570,                                //~ [607-610
 10080,10,9060,20080,                                  //Δ [611-614
 19010,20070,  19033,20033,                            //\ [615-616  | [617-618
 17030,9045,27060,                                     //^ [619-621
 16030,6040,7050,8050,9040,9030,8020,7020,26030,       //° [622-630
 10020,1010,7010,8020,8060,7070,1070,60,20, 19230,9220,
        9920,9930,9230, 19260,9250,9950,9960,29260,    //Ö [631-649
 11010,5010,6020,6060,5070,1070,60,20,1010, 17525,
        7520,8020,8025,7525, 17560,7555,8055,8060,27560,  //ö [650-668
 10010,9040,70, 13060,3020, 19247,9233,9933,9947,29247, //Å [669-678
 12070,40,20,1010,5010,6035,6070,70, 17552,9052,9038,7538,27552,  //å [679-691
 10010,9040,70, 13060,3020, 19230,9220,9920,9930,9230,
        19260,9250,9950,9960,29260, //Ä [692-706
 12070,40,20,1010,5010,6035,6070,70,
     17525,7520,8020,8025,7525,  17560,7555,8055,8060,27560,   //ä [707-724
 13040,7040,15020,5060,11020,21060,  //± [725-730
 14040,4050,5050,5040,24040,  //· [731-735
 11010,20,60,1070,4070,5060,5020,6010,8010,9020,9060,8070,
    -11040,29940,  //$ [736-749
 12070,40,30,1020,5020,6035,6070, 10080,1070,6570,7560,7520,
       6005,1005,-1020,-21040,    //@ [750-765
 11010,7070,17010,21070};   //× [766-769]
private int[] ADDRES =
 //' ' A B  C  D  E  F  G  H  I  J  K  L  M  N  O  P   Q   R
   {-1,0,5,17,25,32,38,43,53,59,65,71,76,79,84,88,97,104,115,     //0-18
 //  S   T   U   V   W   X   Y   Z   a   b   c   d   e   f   g
   124,136,140,146,149,154,158,163,167,175,182,189,196,205,212,  //19-33
 //   h   i   j   k   l   m   n   o   p   q   r   s   t   u   v
    223,229,239,249,255,260,268,274,283,290,297,303,315,322,328, //34-48
 //   w   x   y   z   0   1   2   3   4   5   6   7   8   9   -
    331,336,340,345,349,358,363,371,384,388,397,408,412,428,440, //49-63
 //   +   *   /   (   )   [   ]   =   {   }   .   ,   '   "   !
    442,446,452,454,460,466,470,474,478,485,492,497,512,520,528, //64-78
 //   ?   _   :   ;   <   >   #   %   &   `   ~   ^   µ   $
    535,547,549,559,569,572,575,583,595,516,607,619,502,736,     //79-92
//bcksp @   \   |    Ö   ö    Å   å    Ä   ä   º   °   ±   ·
   -1,750,615,617, 631,650, 669,679, 692,707,622,622,725,731,    //93-106
 //   •   –   −   µ   Δ   ∆   ´   ×
    731,440,440,502,611,611,512,766};                            //107-114
//</editor-fold>
private void sketch(double x0, double y0, String txt, double angleDegr)
                    throws WritePlotFileException {
  //Characters recognized:
  char[] CHR =
   {' ','A', 'B','C','D','E','F','G','H','I','J','K','L','M','N', // 0-14
    'O','P', 'Q','R','S','T','U','V','W','X','Y','Z','a','b','c', //15-29
    'd','e', 'f','g','h','i','j','k','l','m','n','o','p','q','r', //30-44
    's','t', 'u','v','w','x','y','z','0','1','2','3','4','5','6', //45-59
    '7','8', '9','-','+','*','/','(',')','[',']','=','{','}','.', //60-74
    ',','\'','"','!','?','_',':',';','<','>','#','%','&','`','~', //75-89
    '^','μ','$',' ','@','\\','|','Ö','ö','Å','å','Ä','ä',         //90-102
    'º','°','±','·','•','–','−','µ','Δ','∆','´','×'};            //103-114
  int nRecn = CHR.length;
  /* to convert a character to ASCII code and back:
   *   char x = '−';
   *   int cast = (int)x;
   *   System.out.println("String.valueOf("+x+") = \""+String.valueOf(x)+"\"");
   *   int codePoint = String.valueOf(x).codePointAt(0);
   *   char again = (char)codePoint;
   *   System.out.printf("x = %s  cast = %d  codePoint = %d  again = %s%n",
   *                    x, cast, codePoint, again);
   */
  // Backspace is Ctrl-B (=CHR(93))                                   bcksp
  int bcksp = 2;   CHR[93]=(char)bcksp; //Ctrl-B
  ADDRES[89]=607; //~
  ADDRES[90]=619; //^
  ADDRES[92]=736; //$
  if(isFormula) {
    ADDRES[89]=622; //~ = degree sign
    ADDRES[90]=611; //^ = Delta
    ADDRES[92]=502; //$ = µ(mu)
  } // if isFormula
  sketching = textWithFonts;
  //---- get super- and sub-scripts for chemical formulas
  boolean isFormulaOld = isFormula;
  DiagrPaintUtility.ChemFormula cf;
  float[] d;
  int n = txt.length();
    if (isFormula) {
        cf = new DiagrPaintUtility.ChemFormula(txt, new float[1]);
        DiagrPaintUtility.chemF(cf);
        txt = cf.t.toString();
        n = txt.length();
        d = new float[n];
        System.arraycopy(cf.d, 0, d, 0, n);
        isFormula = false;
        for (int i=0; i<n; i++)
            {if(Math.abs(d[i])>0.01f) {isFormula = true;} }
    } // isFormula
    else {d = new float[n];
          for(int i=0; i<n; i++) {d[i]=0f;}}
  // ---- Loop through the characters ----
  int iNow = -1;
  int iCHR;
  boolean stopIt;
  int action;
  double x = 0; double y = 0; double height = sizeSym;
  double xPrev; double yPrev;
  double xPlt, yPlt, radius, angle;
  int iP;
  do {
    iNow++;
    iCHR = 0;
    for(int j=0; j<nRecn; j++) {
        if(txt.charAt(iNow) == CHR[j]) {iCHR=j; break;}
    }
    if(isFormula && Math.abs(d[iNow]) >0.01f) {height = 0.8*sizeSym;}
    xPrev = x; yPrev = y; stopIt = false;
    if(iCHR == 93) { // bcksp
      x = -1; y = 0;
    }
    else if (iCHR == 0 || ADDRES[iCHR] <= -1) { // blank
      x = 1; y = 0;
    }
    else // !(bcksp|blank)
    {
      iP = ADDRES[iCHR] - 1;
      for_j:
      for(int j=0; j<3001; j++) { // do all strokes of the sketch
        iP++;
        x = (double)SK[iP]/100d;
        //action = 1 draw to x,y
        //       = 2 move to x,y
        //       = 3 draw to x,y  and end of character
        action = 1;
        if(Math.abs(x) > 99.999) {action = 2;}
        if(Math.abs(x) > 199.999) {action = 3;}
        if(action == 2) { //move to x,y
          if(x > 0) {x=x-100;}
          if(x < 0) {x=x+100;}
        } else if(action == 3) { //draw to x,y  and end of character
          if(x > 0) {x=x-200;}
          if(x < 0) {x=x+200;}
        }
        y = (int)x;
        x = Math.abs(x-y);
        y = y/100;
        x = x*height + xPrev;
        y = y*height + yPrev + d[iNow]*sizeSym;
        while(true) {
          xPlt =x; yPlt = y;
          if(Math.abs(angleDegr) >0.001) {
            radius = x*x + y*y;
            if(radius > 0) {
              radius = Math.sqrt(radius);
              angle = Math.asin(y/radius) + Math.toRadians(angleDegr);
              xPlt = radius*Math.cos(angle);
              yPlt = radius*Math.sin(angle);
            } //if radius >0
          } // if angleDegr !=0
          xPlt = xPlt + x0;
          yPlt = yPlt + y0;
          if(stopIt) {break for_j;}
          int i =1; if(action ==2) {i=0;}
          //
              moveToDrawTo(xPlt, yPlt, i);
          //
          if(action != 3) {continue for_j;}
          // end of character
          x = 1; y=0;
          x = x*height + xPrev; y = y*height + yPrev;
          stopIt = true;
          // action = 2; //move to x,y
        } //while(true)
      } //for j
    } // if !(bcksp|blank)
    if(iCHR == 93 || iCHR == 0 || ADDRES[iCHR] <= -1) { // bcksp | blank
      x = x * sizeSym + xPrev; y = y * sizeSym + yPrev;
      //action = 2; //move to x,y
      //xPlt = x;  yPlt = y;
      if(Math.abs(angleDegr) > 0.001) {
        //radius = Math.sqrt(x*x + y*y);
        //angle = Math.asin(y/radius) + Math.toRadians(angleDegr);
        //xPlt = radius*Math.cos(angle);
        //yPlt = radius*Math.sin(angle);
      } // if angleDegr !=0
      //xPlt = xPlt + x0;
      //yPlt = yPlt + y0;
    } // if  (bcksp|blank)
  } while (iNow < (n-1));
  isFormula = isFormulaOld;
  sketching = false;
  //return;
} //sketch
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="moveToDrawTo">
/**  "Move to" or "Draw to"
 * @param x float, X-position, should be between -9.99 and 99.99
 * @param y float, Y-position, should be between -9.99 and 99.99
 * @param flag int, must be zero (0) for "move to" or one (1) for "draw to".
 * If not =1 then zero is assumed. */
public void moveToDrawTo(float x, float y, int flag) throws WritePlotFileException {
    moveToDrawTo((double)x, (double)y, flag);
    //return;
} //moveToDrawTo(float)

/**  "Move to" or "Draw to"
 * @param x double, X-position, should be between -9.99 and 99.99
 * @param y double, Y-position, should be between -9.99 and 99.99
 * @param flag int, must be zero (0) for "move to" or one (1) for "draw to".
 * If not =1 then zero is assumed. */
public void moveToDrawTo(double x, double y, int flag) throws WritePlotFileException {
    int i0;
    if(flag != 1) {i0 = 0;} else {i0 = 1;}
    x = Math.min(99.999, Math.max(-9.999, x));
    y = Math.min(99.999, Math.max(-9.999, y));
    int i1 = Math.round((float)(x*100));
    int i2 = Math.round((float)(y*100));
    i1 = Math.max(-999, Math.min(i1, 9999));
    i2 = Math.max(-999, Math.min(i2, 9999));
    if(i1 == i2Last && i2 == i3Last && label == null) {return;}
    i2Last = i1;  i3Last = i2;
    if(save) {
        try{
            if(label != null) {
                w.write(String.format("%1d%4d%4d %s%n", i0,i1,i2,label));
                label = null; }
            else {w.write(String.format("%1d%4d%4d%n", i0,i1,i2));}
            w.flush();
        } catch (Exception ex) {
            save = false;
            pd.pltFile_Name = null;
            throw new WritePlotFileException("Error: \""+ex.getMessage()+"\","+nl+
                           "   in Graphics Library \"moveToDrawTo\","+nl+
                           "   while writing output file" +nl+
                           "   \""+pd.pltFile_Name+"\"");
        }
    } //if(save)
    if(!sketching) {pd.pltFileAList.add(new PltData.PlotStep(i0, i1, i2));}
    if(i1<pd.userSpaceMin.x) {pd.userSpaceMin.x = i1;}
    if(i1>pd.userSpaceMax.x) {pd.userSpaceMax.x = i1;}
    if(i2<pd.userSpaceMin.y) {pd.userSpaceMin.y = i2;}
    if(i2>pd.userSpaceMax.y) {pd.userSpaceMax.y = i2;}
    //return;
} // moveToDrawTo(x,y,i)
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="axes">
/**  Display (X,Y)-axes (and save to the output plot file).
 * @param xMinI float, minimum X-value. For a log-axis xMinI = log10(min. X-value)
 * @param xMaxI float, maximum X-value. For a log-axis xMaxI = log10(max. X-value)
 * @param yMinI float, minimum Y-value. For a log-axis yMinI = log10(min. Y-value)
 * @param yMaxI float, maximum X-value. For a log-axis yMaxI = log10(max. Y-value)
 * @param xOr float, X-position of the axes origin
 * @param yOr float, Y-position of the axes origin
 * @param xAxL float, length of X-axis
 * @param yAxL float, length of Y-axis
 * @param size float, size of axes tick labels
 * @param logX boolean, true if X-axis should be log10 scale
 * @param logY boolean, true if Y-axis should be log10 scale
 * @param frame boolean, true for a framed graph, with bottom and top X-axes
 * and left and right Y-axes.
 * @throws lib.kemi.graph_lib.GraphLib.AxesDataException */
public void axes(float xMinI, float xMaxI, float yMinI, float yMaxI,
        float xOr, float yOr, float xAxL, float yAxL, float size,
        boolean logX, boolean logY, boolean frame)
        throws AxesDataException, WritePlotFileException {

    axes((double)xMinI, (double)xMaxI, (double)yMinI, (double)yMaxI,
            (double)xOr, (double)yOr, (double)xAxL, (double)yAxL,
            (double)size, logX, logY, frame);

} //axes(float)
/**  Display (X,Y)-axes (and save to the output plot file).
 * @param xMinI double, minimum X-value. For a log-axis xMinI = log10(min. X-value)
 * @param xMaxI double, maximum X-value. For a log-axis xMaxI = log10(max. X-value)
 * @param yMinI double, minimum Y-value. For a log-axis yMinI = log10(min. Y-value)
 * @param yMaxI double, maximum X-value. For a log-axis yMaxI = log10(max. Y-value)
 * @param xOr double, X-position of the axes origin
 * @param yOr double, Y-position of the axes origin
 * @param xAxL double, length of X-axis
 * @param yAxL double, length of Y-axis
 * @param size double, size of axes tick labels
 * @param logX boolean, true if X-axis should be log10 scale
 * @param logY boolean, true if Y-axis should be log10 scale
 * @param frame boolean, true for a framed graph, with bottom and top X-axes
 * and left and right Y-axes.
 * @throws lib.kemi.graph_lib.GraphLib.AxesDataException */
public void axes(double xMinI, double xMaxI, double yMinI, double yMaxI,
        double xOr, double yOr, double xAxL, double yAxL, double size,
        boolean logX, boolean logY, boolean frame)
        throws AxesDataException, WritePlotFileException {
// Draw axes.
// The spacing between numbering in the axes can be controlled by the
// variables stpXI,stpYI. Valid input values are 0, 0.5, 1.0 and 2.0
// If zero, standard spacing between axes labels is used.
// Uses logTxt (as well as setIsFormula, sym and moveToDrawTo).
double anglX = 0; double anglY =0; double o = 5.E-6d;
double w;
//Check input values
if(Math.abs(stpXI - 2)>1E-6 && Math.abs(stpXI - 1)>1E-6 &&
        Math.abs(stpXI - 0.5)>5E-7 && Math.abs(stpXI - 0.2)>2E-7) {stpXI=0;}
if(Math.abs(stpYI - 2)>1E-6 && Math.abs(stpYI - 1)>1E-6 &&
        Math.abs(stpYI - 0.5)>1E-6 && Math.abs(stpYI - 0.2)>1E-6) {stpYI=0;}
xAxL = Math.max(2,Math.min(xAxL, 50));
yAxL = Math.max(2,Math.min(yAxL, 50));
double shift = Math.max(0.1,Math.min(size,0.6));

// ---- DRAW THE AXES
setLabel("-- AXIS --");
setPen(Math.min(-1, screenColour));
double x_Mx = xOr + xAxL;  double y_Mx = yOr + yAxL;
String t = String.format(engl,"Size, X/Y length & origo: %7.3f %7.3f %7.3f %7.3f %7.3f",
            size,xAxL,yAxL,xOr,yOr);
setLabel(t);
setPen(Math.max(1, plotPen));
t = String.format(engl, "X/Y low and high:%11.4g%11.4g%11.4g%11.4g",
            xMinI,xMaxI,yMinI,yMaxI);
setLabel(t);
moveToDrawTo(xOr,y_Mx,0);
moveToDrawTo(xOr,yOr, 1);
moveToDrawTo(x_Mx,yOr,1);
if(frame) {moveToDrawTo(x_Mx,y_Mx,1);
            moveToDrawTo(xOr,y_Mx, 1);}

// check for infinity values
double Xmx_i = Math.min(Math.abs(xMaxI),1.e35)*Math.signum(xMaxI);
double Xmn_i = Math.min(Math.abs(xMinI),1.e35)*Math.signum(xMinI);
double Ymx_i = Math.min(Math.abs(yMaxI),1.e35)*Math.signum(yMaxI);
double Ymn_i = Math.min(Math.abs(yMinI),1.e35)*Math.signum(yMinI);

double xPl; double yPl; String txt; boolean b;
double wMn; double wMx; double v; double tl; double rtl; double scaleMaxX = -1;
int j, iStep, k, kAbs, kLength;
boolean xAxis;
// The steps in log-10 scale:
              // 2      4      6      10
double[] logSt ={0.3010,0.3010,0.1761,0.2219};
              // 2        3        4        5        6        7        8        9        10
double[] logS = {0.301030,0.176091,0.124939,0.096910,0.079181,0.066947,0.057992,0.051153,0.045757};

// use chemical formulas
boolean isFormulaOld = isFormula;
setIsFormula(true);

// ----------------  X-AXIS  ----------------

// check that the range is wide enough. If not, try to enlarge it:
boolean xAxisErr = false; String errTxtX = null;
w = Math.abs(Xmx_i - Xmn_i);
if(logX)
    {if(w <= 0.5) {if(Xmx_i < Xmn_i) {Xmx_i = Xmx_i - (0.5+o-w);}
                else {Xmx_i = Xmx_i + (0.5+o-w);}}}
else //not logX
    {if(w < 1.E-30) {if(Xmx_i < Xmn_i) {Xmx_i = Xmx_i - Math.max(Xmx_i*5.01E-4, 1.000001E-30);}
                else {Xmx_i = Xmx_i + Math.max(Xmx_i*5.01E-4, 1.000001E-30);}}}
// check that the rage is really OK
w = Math.abs(Xmx_i - Xmn_i);
if(logX) {
    if(w < 0.5) {xAxisErr = true;
            errTxtX = "abs(xMax-xMin) must be > 0.5";}
    } else { //!logX
    if(w < 1.E-30) {xAxisErr = true;
            errTxtX = "abs(xMax-xMin) must be > 1E-30";}
    if(!xAxisErr) {
        w = w / Math.max( Math.abs(Xmx_i), Math.abs(Xmn_i) );
        if (w <= 4.999999E-4) {xAxisErr = true;
            errTxtX = "abs(xMax-xMin)/max(xMax,xMin) must be > 5E-4";}
        }
    } //logX?

if(!xAxisErr) { //draw labels and tick marks only if the range is large enough

boolean reverseX =false;
if(xMinI > xMaxI) {reverseX =true;}

// X-Scaling Factors
xL = xAxL / (Xmx_i - Xmn_i);
xI = xL * Xmn_i - xOr;

//---- Decide Scale for X-Axis
// expX is the power of ten for the difference
//   between xMax and xMin, thus for xMin=10 and xMax=100, expX=1
//   while for xMax=1000, expX=2. For xMin=10010 and xMax=10100,
//   expX is =1 as well.
double xMa; double xMi; double potX;
long expX = 2 + Math.round( Math.log10(Math.abs(Xmx_i-Xmn_i)) );
while (true) {
    expX--;
    potX = Math.pow(10,expX);
    wMn = (Xmn_i/potX);  wMx = (Xmx_i/potX);
    if(Math.abs(wMx - wMn) >= 0.999999) {break;}
} //while
long lowX = Math.round( ((Math.abs(wMn)+o) * Math.signum(Xmn_i)) );
if(logX) {lowX = Math.round( (((long)((double)lowX*potX))/potX ));}
if(lowX < 0 && (double)lowX < (wMn-o)) {lowX++;}
double stpX = 1;
w = Math.abs(wMx-wMn);
if(w > 7) {stpX = 2;}
if(logX) {
    if(w <= 1.050001) {stpX = 0.2;}
    if(expX == 1 && w <= 2.000001) {stpX = 0.2;}
} else {
    if(w <= 3.000001) {stpX = 0.5;}
    if(w <= 1.400001) {stpX = 0.2;}
}
if(stpXI > 0) {stpX = stpXI;}
double spanX = 0;
if(logX) {spanX = potX * stpX;}
w = 3*o*potX;
if(!reverseX) {xMa = Xmx_i + w; xMi = Xmn_i - w;}
else {xMa = Xmx_i - w; xMi = Xmn_i + w;}
String formX;
if(logX) {formX="    %8.4f";} //width=12
else {
  formX = " %8.1f"; //width=9
  if((expX == -1 && stpX < 1) || (expX == -2 && stpX >= 1)) {formX="  %8.2f";} //width=10
  if((expX == -2 && stpX < 1) || (expX == -3 && stpX >= 1)) {formX="   %8.3f";}//width=11
  if(expX == -3 && stpX < 1) {formX="    %8.4f";} //width=12
}

// ---- Write Scale in X-Axis
b = (Math.abs(xMa)<1E5 & Math.abs(xMi)<1E5);
yPl = yOr -(2.2-shift)*size;
xAxis = true;
iStep=0;
j=1; if(expX == -1) {j=10;}
w = -20*stpX;
while (true) {
  if(logX && spanX < 1) {
      w = w + logSt[iStep]*j;
      iStep++;
      if(iStep > (logSt.length-1)) {iStep=0;}
  } else {w = w + stpX;}
  if(!reverseX)
    {xPl = lowX + w;}
  else { //reverseX:
    if(logX && (spanX < 1 && w>1E-4 && w<0.9999))
        {xPl = lowX -(1-w);} else {xPl = lowX - w;}
  } // reverseX?
  if(xPl >= -1E-6) {xPl = xPl + o;} else {xPl = xPl - o;}
  v = xPl;
  xPl = xPl*potX;
  if(!reverseX) {
        if(xPl > xMa) {break;}
        if(xPl < xMi) {continue;}
  } else {
        if(xPl < xMa) {break;}
        if(xPl > xMi) {continue;}
  }
  if(expX >= 1 && expX <= 4 && b) {k =(int)xPl;} else {k =(int)v;}
  double xPlTxt;
  if(!((expX >= 0 || expX < -3) && (stpX > 0.5)) &&
      !(expX >= 1 && expX <= 4 && b)) {
            if(!(expX >= 0 || expX < -3)) {v = xPl;}
            txt = String.format(engl, formX, v);
            xPlTxt = xPl*xL-xI - 7.4*size;
  } else {
    txt = String.format(" %8d", k);
    kAbs= Math.abs(k);
    if(kAbs < 10) {kLength=1;}
    else if(kAbs > 9 && kAbs < 100) {kLength=2;}
    else if(kAbs > 99 && kAbs < 1000) {kLength=3;}
    else if(kAbs > 999 && kAbs < 10000) {kLength=4;}
    else {kLength=5;} //if kAbs > 9999
    xPlTxt = xPl*xL-xI - (8.9-((double)kLength/2))*size;
  }
  rtl = (double)Util.rTrim(txt).length();
  if(logX) {
    txt = logTxt(txt,xAxis);
    rtl = (double)Util.rTrim(txt).length();
    int p = txt.indexOf(".");
    if(p > -1)
        {xPlTxt = (xPl*xL-xI) - ((double)p -0.5) * size;}
    else
        {tl = (double)txt.trim().length();
        xPlTxt = (xPl*xL-xI)  - ((rtl-tl) + (tl/2)) * size;} //p > -1 ?
    } //logX?
  sym(xPlTxt, yPl, size, txt, anglX, 0, false); //align center
  scaleMaxX = Math.max(scaleMaxX, ((xPl*xL-xI)+rtl*size));
} //while (true)

// ---- Draw X-Axis Power of 10 (if any)
b = (Math.abs(xMa) < 1.E5 & Math.abs(xMi) < 1.E5);
if(!(expX >= 1 && expX <= 4 && b) &&
    !(expX <= 0 && expX >= -3)) {
  yPl = yOr-(2.2-shift)*size;
  xPl = Math.max(x_Mx,scaleMaxX) +2*size;
  //txt = "*10";
  //sym(xPl, yPl, size, txt, anglY, 1, false);
  //yPl = yPl + size/2;
  //if(expX > 9 || expX < -9) {xPl = xPl + size;}
  //if(expX > 99 || expX < -99) {xPl = xPl + size;}
  //if(expX < 0) {xPl = xPl + size;}
  txt = String.format("%4d", expX);
  txt = "×10'"+txt.trim()+"`";
  sym(xPl, yPl, size, txt, anglX, -1, false);
}

// ----     X-axis divisions
b = false;
if(logX) {b = (spanX <= 2.000001 & expX <= 0);}
iStep=0;
if(reverseX) {iStep = logS.length-1;}
w = -40*stpX;
while (true) {
    if(!b) {
      w = w +(stpX/2);
    } else {
      w = w + logS[iStep]/potX;
      if(!reverseX) {iStep++;} else {iStep--;}
      if(iStep > (logS.length-1)) {iStep = 0;}
      if(iStep < 0) {iStep = logS.length-1;}
    }
if(!reverseX) {
  xPl = (lowX + w)*potX;
  if(xPl > xMa) {break;}
  if(xPl < xMi) {continue;}
} else {
  xPl = (lowX - w)*potX;
  if(xPl < xMa) {break;}
  if(xPl > xMi) {continue;}
}
xPl = xPl*xL-xI;
v = 0.5;
if((!reverseX && b && iStep == 0) ||
    (reverseX && b && iStep == (logS.length-1))) {v = 0.8;}
moveToDrawTo(xPl, yOr,        0);
moveToDrawTo(xPl, yOr+v*size, 1);
if(frame) {moveToDrawTo(xPl, y_Mx,        0);
           moveToDrawTo(xPl, y_Mx-v*size, 1);}
} //while (true)

} //if !xAxisErr


// ----------------  Y-AXIS  ----------------

// check that the range is wide enough. If not, try to enlarge it:
boolean yAxisErr = false; String errTxtY = null;
w = Math.abs(Ymx_i - Ymn_i);
if(logY)
{if(w <= 0.5) {if(Ymx_i < Ymn_i) {Ymx_i = Ymx_i - (0.5+o-w);}
                else {Ymx_i = Ymx_i + (0.5+o-w);}}}
else //not logY
{if(w <1.E-30) {if(Ymx_i < Ymn_i) {Ymx_i = Ymx_i - Math.max(Ymx_i*5.01E-4, 1.000001E-30);}
                else {Ymx_i = Ymx_i + Math.max(Ymx_i*5.01E-4, 1.000001E-30);}}}
// check that the rage is really OK
w = Math.abs(Ymx_i - Ymn_i);
if(logY) {
    if(w < 0.5) {yAxisErr = true;
            errTxtY = "abs(yMax-yMin) must be > 0.5";}
    } else { //!logY
    if(w < 1.E-30) {yAxisErr = true;
            errTxtY = "abs(yMax-yMin) must be > 1E-30";}
    if(!yAxisErr) {
        w = w / Math.max( Math.abs(Ymx_i), Math.abs(Ymn_i) );
        if (w <= 4.999999E-4) {xAxisErr = true;
            errTxtY = "abs(yMax-yMin)/max(yMax,yMin) must be > 5E-4";}
        }
    } //logY?

if(!yAxisErr) { //draw labels and tick marks only if the range is large enough

boolean reverseY =false;
if(yMinI > yMaxI) {reverseY =true;}

// Y-Scaling Factors
yL = yAxL / (Ymx_i - Ymn_i);
yI = yL * Ymx_i - y_Mx;

// ---- Decide Scale for Y-Axis
// expY is the power of ten for the difference
//   between yMax and yMin, thus for yMin=10 and yMax=100, expY=1
//   while for yMax=1000, expY=2. For yMin=10010 and yMax=10100,
//   expY is =1 as well.
double yMa; double yMi;
long expY = 2 + Math.round( Math.log10(Math.abs(Ymx_i-Ymn_i)) );
double potY;
while (true) {
    expY--;
    potY = Math.pow(10,expY);
    wMn = (Ymn_i/potY);  wMx = (Ymx_i/potY);
    if(Math.abs(wMx - wMn) >= 0.999999) {break;}
} //while
long lowY = Math.round( (Math.abs(wMn)+o) * Math.signum(Ymn_i) );
if(logY) {lowY = Math.round( ((long)((double)(lowY)*potY))/potY );}
if(lowY < 0 && (double)lowY < (wMn-o)) {lowY++;}
double stpY = 1;

w = Math.abs(wMx-wMn);
if(w > 7) {stpY = 2;}
if(logY) {
    if(w <= 1.050001) {stpY = 0.2;}
    if(expY == 1 && w <= 2.000001) {stpY = 0.2;}
} else {
    if(w <= 3.000001) {stpY = 0.5;}
    if(w <= 1.400001) {stpY = 0.2;}
}
if(stpYI > 0) {stpY = stpYI;}
double spanY = 0;
if(logY) {spanY = potY * stpY;}
w = 3*o*potY;
if(!reverseY) {yMa = Ymx_i + w; yMi = Ymn_i - w;}
else {yMa = Ymx_i - w; yMi = Ymn_i + w;}
String formY;
if(logY) {formY="    %8.4f";}
else{
  formY = " %8.1f";
  if((expY == -1 && stpY < 1) || (expY == -2 && stpY >= 0.999999)) {formY="  %8.2f";}
  if((expY == -2 && stpY < 1) || (expY == -3 && stpY >= 0.999999)) {formY="   %8.3f";}
  if((expY == -3 && stpY < 1) ) {formY="    %8.4f";}
}

// ---- Wrtie Scale in Y-Axis
b =(Math.abs(yMa)<1.E5 & Math.abs(yMi)<1.E5);
xAxis=false;
iStep=0;
j=1; if(expY == -1) {j=10;}
w = -20*stpY;
while (true) {
  if(logY && spanY < 1) {
    w = w + logSt[iStep]*j;
    iStep++;
    if(iStep > (logSt.length-1)) {iStep=0;}
  } else {w = w + stpY;}
  if(!reverseY)
    {yPl = lowY + w;}
  else {//reverseY:
    if((logY && spanY < 1) && (w>1E-4 && w<0.9999))
        {yPl = lowY - (1-w);} else {yPl = lowY - w;}
  } //reverseY?
  if(yPl >= -1E-6) {yPl = yPl +o;} else {yPl = yPl -o;}
  v = yPl;
  yPl = yPl*potY;
  if(!reverseY) {
        if(yPl > yMa) {break;}
        if(yPl < yMi) {continue;}
  } else {
        if(yPl < yMa) {break;}
        if(yPl > yMi) {continue;}
  }
  if(expY >= 1 && expY <= 4 && b) {k =(int)yPl;} else {k =(int)v;}
  if(!((expY >= 0 || expY < -3) && (stpY > 0.5)) &&
        !(expY >= 1 && expY <= 4 && b)) {
        if(!(expY >= 0 || expY < -3)) {v=yPl;}
        txt = String.format(engl, formY, v);
  } else {
        txt = String.format(" %8d", k);
  }
  if(logY) {txt = logTxt(txt,xAxis);}
  yPl = yPl*yL-yI - size/2;
  if(yPl < yOr) {yPl = yPl +0.6*size;}
  xPl = xOr - ((Util.rTrim(txt).length() +1) - shift) * size;

  sym(xPl, yPl, size, txt, anglY, 1, false); //align right
} //while (true)

// ---- Draw Y-Axis Power of 10 (if any)
b = (Math.abs(yMa) < 1.E5 & Math.abs(yMi) < 1.E5);
if(!(expY >= 1 && expY <= 4 && b) &&
    !(expY <= 0 && expY >= -3)) {
  xPl = xOr;
  yPl = y_Mx +0.8*size;
  //txt = "*10";
  //sym(xPl, yPl, size, txt, anglY, 1, false);
  //yPl = yPl + size/2;
  //if(expY > 9 || expY < -9) {xPl = xPl + size;}
  //if(expY > 99 || expY < -99) {xPl = xPl + size;}
  //if(expY < 0) {xPl = xPl + size;}
  txt = String.format("%4d", expY);
  txt = "×10'"+txt.trim()+"`";
  sym(xPl, yPl, size, txt, anglY, -1, false);
}

// ----     Y-axis divisions
b = false;
if(logY) {b = (spanY <= 2.000001 & expY <= 0);}
iStep=0;
if(reverseY) {iStep = logS.length-1;}
w = -40*stpY;
while (true) {
  if(!b) {
      w = w + (stpY/2);
  } else {
      w = w + logS[iStep]/potY;
      if(reverseY) {iStep--;} else {iStep++;}
      if(iStep > (logS.length-1)) {iStep=0;}
      if(iStep < 0) {iStep = logS.length-1;}
  }
if(!reverseY) {
  yPl = (lowY + w)*potY;
  if(yPl > yMa) {break;}
  if(yPl < yMi) {continue;}
} else {
  yPl = (lowY - w)*potY;
  if(yPl < yMa) {break;}
  if(yPl > yMi) {continue;}
}
yPl = yPl*yL-yI;
v = 0.5;
if((!reverseY && b && iStep == 0) ||
        (reverseY && b && iStep == (logS.length-1))) {v = 0.8;}
moveToDrawTo(xOr,        yPl, 0);
moveToDrawTo(xOr+v*size, yPl, 1);
if(frame) {moveToDrawTo(x_Mx,        yPl, 0);
           moveToDrawTo(x_Mx-v*size, yPl, 1);}
} // while (true)

} //if !yAxisErr

// errors?
if(xAxisErr) {
    throw new AxesDataException ("Error: can Not draw X-axis - range too narrow"+nl
            + errTxtX);}
if(yAxisErr) {
    throw new AxesDataException ("Error: can Not draw Y-axis - range too narrow"+nl
            + errTxtY);}

// reset character set
setIsFormula(isFormulaOld);
// set limits for clip-window when drawing lines with method "line"
double x = Math.min(Xmn_i,Xmx_i); double y = Math.min(Ymn_i,Ymx_i);
double width = Math.max(Xmx_i,Xmn_i) - x;
double height = Math.max(Ymx_i,Ymn_i) - y;
clippingArea = new java.awt.geom.Rectangle2D.Double(x,y,width,height);

//return;
} //axes
//<editor-fold defaultstate="collapsed" desc="AxesDataException">
public class AxesDataException extends Exception {
    public AxesDataException() {}
    public AxesDataException(String txt) {super(txt);}
} //DataFileException
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="logTxt">
/** Convert a tick label representing a log10-value, for example "23", to a
 * non-log10 representation, that is, "10'23`". Input values between -3 and +3
 * are converted to: 0.001, 0.01, ... , 100, 1000.<br>
 * Floating point numbers with a fractional part: If the input value
 * is n.301, n.6021 or n.7782 (or -n.6990, -n.3979, -n.2219) then
 * the output is "2.10'n`", "4.10'n`", "6.10'n`" (or "2.10'(n-1)`" etc).
 * For example, "-3.398" is converted to "4.10'-4`".<br>
 * If the input text contains any other fractional part ("n.m") then output text
 * is: "10'n.m`".
 *
 * @param cDum String: on input it contains the value to be plotted as an axis
 * tick label; for example "5" or "5.0".
 * @param xAxis boolean =true if the String cDum will be used as an X-axis tick
 * label, =false for Y-axis tick labels. Y-axis labels are right-adjusted.
 * @return String tick label
 */
private String logTxt(String cDum, boolean xAxis) {
  String[] numb = {"0","1","2","3","4","5","6"};
  //                                    012345678901234567
  StringBuilder form = new StringBuilder("     %1d'.`10'%1d`");
  boolean integr; double w; long k; int coef; int exponent; int width;
  integr = true; coef =1;
  //-- check for real or integer
  for(int n = 0; n < cDum.length(); n++)
      { if(cDum.charAt(n) == '.') {integr = false; break;} } //for n

  if(integr) {k = Long.parseLong(cDum.trim()); w = (double)k;}
  else {w = Double.parseDouble(cDum.trim()); k = (long)w;}

  if(!integr) { // --  Floating point values:
    width = 1;
    if(w > 9.99999 || w < -9.1) {width = 2;}
    if(w > 99.99999 || w < -99.1) {width = 3;}
    if(w < 0) {width++;}
    form.replace(15, 16, numb[width]); //width might be 1,2,3 or 4
    if(!xAxis) {form.delete(0,width);} //shorten the output string
    if(w > 0.) {exponent = (int)(w+0.00001);}
        else {exponent = (int)(w-0.00001);}
    w = w - exponent;
    if(w < -3.E-5) {
        //if w was negative; for example -5.699: convert -0.699 to +0.301
        w = w +1;  exponent--;
    }
    if(w <= 3.E-5) { //it is an exact power of ten, write it as 10'exponent`
        cDum = String.format("   %8d", exponent);
    } else { //w > 3.E-5; get the coef, to write for example 2.10'-5`
        coef = -1;
        if(Math.abs(w-0.3010) <=(Math.max(Math.abs(w),Math.abs(0.3010))*1E-4)) {coef=2;}
        if(Math.abs(w-0.6020) <=(Math.max(Math.abs(w),Math.abs(0.6020))*1E-4)) {coef=4;}
        if(Math.abs(w-0.7781) <=(Math.max(Math.abs(w),Math.abs(0.7781))*1E-4)) {coef=6;}
        if(coef <=0) { //this should not happen
            //System.err.println("Error in logTxt: w,exponent= "+w+", "+exponent);
            coef = 2;
        }
        if(coef > 0 && Math.abs(exponent) > 3) {
            cDum = String.format(form.toString(), coef, exponent);
            //length=13, for example: "    2'.`10'3`"; width=5
            return cDum;}
      } //if w <= 3.E-5
  } //if not integr
  else { //if !integr
    exponent = (int)k;
  } // integr?

  //-- Integers & exact floats below -3 or above 3 (below 0.001 or above 1000)
  if(Math.abs(exponent) > 3 | coef < 0) {
    // get first non-blank
    int n;
    for(n =0; n < cDum.length(); n++) {
        if(cDum.charAt(n) != ' ') {break;} }//for n
    //n = Math.max(3, Math.min(n, cDum.length()));
    //   and insert 10' at the begining
    if(n <3) {cDum = "10'" + cDum.substring(n);}
    else {cDum = cDum.substring(0, n-3) + "10'" + cDum.substring(n);}
    // insert "`" at the UserSpaceMin
    // get last non-blank
    int len;
    for(len = cDum.length()-1; len >= 0; len--)
        { if(cDum.charAt(len) != ' ') {break;} }//for len
    if(len <0) {len=0;}
    cDum = cDum.substring(0,len+1)+"`";
    return cDum;
  } //if abs(exponent)>3 or coef < 0

  //-- Integers & floats between -3 and 3 (between 0.001 and 1000)
  String cDum1 = " ";
  if(exponent == -3)      {cDum1="0.001";}
  else if(exponent == -2) {cDum1=" 0.01";}
  else if(exponent == -1) {cDum1="  0.1";}
  else if(exponent == 0)  {cDum1="    1";}
  else if(exponent == 1)  {cDum1="   10";}
  else if(exponent == 2)  {cDum1="  100";}
  else if(exponent == 3)  {cDum1=" 1000";}
  // write for example 0.02, 0.04 etc
  if(xAxis) {
        if(integr) {
          if(exponent == -3 || exponent == -2)  cDum = "      "+cDum1;
          if(exponent == 0  || exponent == 1)   cDum = "    "  +cDum1;
          if(exponent == -1 || exponent == 2 ||
                  exponent == 3)                cDum = "     " +cDum1;
          return cDum;
        }
        if(exponent == -3 || exponent == -2)    cDum = "     " +cDum1;
        if(exponent == 0  || exponent == 1)     cDum = "   "   +cDum1;
        if(exponent == -1 || exponent == 2 ||
                exponent == 3)                  cDum = "    "  +cDum1;}
  else {//not xAxis
        cDum = "    " +cDum1;
  } //xAxis
  if(integr) return cDum;
  // Substitute the "1", in for example "0.1" or "100", for the correct number
  for(int n =0; n < cDum.length(); n++) {
    if(cDum.charAt(n) == '1') {
    cDum = cDum.substring(0,n)+ numb[coef] + cDum.substring(n+1);
    break;}
  }
  return cDum;
} // logTxt()
//</editor-fold>

//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="isFormulaLength">
/**
 * @param cDum String
 * @return the length of the text string without leading or
 * trailing white space and without counting the characters "'", "´" or "`".
 * For example for "10'4`" returns 3.
 * In addition for a "simple" chemical name with charge, removes from the length any
 * space associated with the charge. For example for "H +" returns 2, and
 * for "CO3 2-" returns 5. Note however that for "(CO3 2-)" it returns 8.
 */
public static int isFormulaLength(String cDum) {
  cDum = cDum.trim();
  int len = cDum.length();
  if(len<=0) {return 0;}
  int start, nbr, i;
  nbr = 0;
  i=Math.max(cDum.indexOf("+"),Math.max(cDum.indexOf("-"),
          // Unicode En Dash and Minus Sign
          Math.max(cDum.indexOf('\u2013'),cDum.indexOf('\u2212'))));
  if(i>=(len-3) && i>1) { //found + or - at the right place
    if(i==(len-1) && len >2) {
        if(Character.isWhitespace(cDum.charAt(i-1))) {nbr++;} //"H +"
        else if(len >3 && cDum.charAt(i-1)>='2'&& cDum.charAt(i-1)<='9'
           && Character.isWhitespace(cDum.charAt(i-2))) {nbr++;} //"S 2-"
        if(len >4 && cDum.charAt(i-1)>='0'&& cDum.charAt(i-1)<='9'
           && cDum.charAt(i-2)>='1'&& cDum.charAt(i-2)<='9'
           && Character.isWhitespace(cDum.charAt(i-3))) {nbr++;} //"S 12-"
    }
    else
    if(len >3 && i==(len-2) && Character.isWhitespace(cDum.charAt(i-1))
            && (cDum.charAt(i+1)>='2'&& cDum.charAt(i+1)<='9'))  //"S -2"
                {nbr++;}
    else
    if(len>4 && i==(len-3) && Character.isWhitespace(cDum.charAt(i-1))
            && (cDum.charAt(i+1)>='1'&& cDum.charAt(i+1)<='9')
            && (cDum.charAt(i+2)>='0'&& cDum.charAt(i+2)<='9')) //"S -12"
                {nbr++;}
  } // found + or -
  start = 0;
  while(true) {
    i=cDum.indexOf("'", start);
    if(i<0) {break;}
    nbr++; start = i+1;
    if(start > len) {break;}
  }
  start = 0;
  while(true) {
    i=cDum.indexOf("´", start);
    if(i<0) {break;}
    nbr++; start = i+1;
    if(start > len) {break;}
  }
  start = 0;
  while(true) {
    i=cDum.indexOf("`", start);
    if(i<0) {break;}
    nbr++; start = i+1;
    if(start > len) {break;}
  }
  len = len - nbr;
  return len;
} // isFormulaLength(String)
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="line">
/**  Set the style for the lines drawn using the method "line"
 * @param lineStyle int 0=continous, 1-5=dashed. */
public void lineType(int lineStyle) throws WritePlotFileException {
  this.lineStyle = Math.max(0,Math.min(5,lineStyle));
  if(nDash !=0) {dashIt();}
  nDash = 0;
  if(lineStyle ==1)      {dash[0]=0.35; dash[1]=0.25; dash[2]=0; dash[3]=0;}
  else if(lineStyle ==2) {dash[0]=0.20; dash[1]=0.30; dash[2]=0; dash[3]=0;}
  else if(lineStyle ==3) {dash[0]=0.30; dash[1]=0.10; dash[2]=0; dash[3]=0;}
  else if(lineStyle ==4) {dash[0]=0.30; dash[1]=0.20; dash[2]=0.07; dash[3]=0.2;}
  else if(lineStyle ==5) {dash[0]=0.07; dash[1]=0.20; dash[2]=0; dash[3]=0;}
  else                   {dash[0]=0; dash[1]=0; dash[2]=0; dash[3]=0;}
  //return;
} //lineType(int)


/** Draw a line using the type (continuous or dashed) specified by
 * previous call to lineType(i).
 * @param x double[] x-coordinates
 * @param y double[] y-coordinates  */
public void line(double[] x, double[] y) throws WritePlotFileException {
  int n = Math.min(x.length, y.length);
  if(n <=1) {return;}
  double oldX = Double.NEGATIVE_INFINITY;
  double oldY = Double.NEGATIVE_INFINITY;
  double x1; double y1;
  double x2=Double.NaN; double y2=Double.NaN;
  java.awt.geom.Line2D lineToClip;
  // draw the line
  for(int j = 1; j < n; j++) {
    x1 = x[j-1]; y1 = y[j-1];
    x2 = x[j];   y2 = y[j];
    lineToClip = new java.awt.geom.Line2D.Double(x1,y1, x2,y2);
    lineToClip = myClip(lineToClip, clippingArea);
    if(lineToClip == null) {continue;}
    x1 = lineToClip.getX1(); y1 = lineToClip.getY1();
    x2 = lineToClip.getX2(); y2 = lineToClip.getY2();
    if(oldX != x1 || oldY != y1) {
        x1 = x1*xL-xI; y1 = y1*yL-yI;
        lineMoveToDrawTo(x1,y1,0);
    }
    oldX = x2; oldY = y2;
    x2 = x2*xL-xI; y2 = y2*yL-yI;
    lineMoveToDrawTo(x2,y2,1);
  } //for j
  //if a dash line has been plotted: close and draw
  if(!Double.isNaN(x2) && !Double.isNaN(y2)) {lineMoveToDrawTo(x2,y2,0);}
  //return;
} //line(double[],double[])

//<editor-fold defaultstate="collapsed" desc="line methods">
// fields used in dash lines
   private int lineStyle = 0;
   private int nDash = 0;
   private double[] xDash = new double[100];
   private double[] yDash = new double[100];
   private double[] dash = {0,0,0,0};
//<editor-fold defaultstate="collapsed" desc="dash-line methods">
private void lineMoveToDrawTo(double x, double y, int n) throws WritePlotFileException {
  if(lineStyle == 0) {moveToDrawTo(x, y, n); return;}
  if(n == 0 && nDash >0) {dashIt();}
  nDash++;
  xDash[nDash-1] = x; yDash[nDash-1] = y;
  if(nDash >= xDash.length-1) {dashIt();}
  //return;
} //lineMoveToDrawTo(x,y,n)
private void dashIt() throws WritePlotFileException {
  if(nDash <=1) {nDash=0; return;}
  boolean down;
  double xP = xDash[0]; double yP = yDash[0];
  double xOld = xP; double yOld = yP;
  double cuLength = 0;
  double x; double y;
  int k;
  for(k = 0; k < nDash-1; k++) {
    x = xDash[k+1]-xDash[k];
    y = yDash[k+1]-yDash[k];
    cuLength = cuLength + Math.sqrt(x*x + y*y);
  } //for k
  double dLength = dash[0]+dash[1]+dash[2]+dash[3];
  int noDash = (int)((cuLength-dash[0])/dLength);
  if(noDash <=0) {
    for(k=0; k < nDash-1; k++) {
      xP = xDash[k+1];  yP=yDash[k+1];
      moveToDrawTo(xOld, yOld, 0);
      moveToDrawTo(xP, yP, 1);
      xOld = xP;  yOld = yP;
    } //for k
    nDash = 0; return;
  } //if noDash <=0
  double f = noDash * dLength /(cuLength-dash[0]);
  double[] dash2 = {0,0,0,0};
  dash2[0] = dash[0]/f;  dash2[1] = dash[1]/f;
  dash2[2] = dash[2]/f;  dash2[3] = dash[3]/f;
  double del; double left; double lenP;
  double incr; double l; double d22;
  k =0;
  down = true;
  while(down) {
    for(int i =0; i < 4; i++) {
      del = dash2[i];  left = del;
      lenP = 0;
      while((lenP < del) && (k < nDash-1)) {
          x = xDash[k+1] - xP;
          y = yDash[k+1] - yP;
          incr = Math.sqrt(x*x + y*y);
          lenP = lenP + incr;
          if(lenP < del) {
            k++;
            xP = xDash[k];  yP = yDash[k];
            if(down) {
              moveToDrawTo(xOld, yOld, 0);
              moveToDrawTo(xP, yP, 1);
            } //if down
            xOld = xP;  yOld = yP;
            left = left - incr;
          } //if(lenP < del)
      } //while (lenP < del) & (k < nDash)
      if(del < lenP) {
        l = lenP - del;
        d22 = left/(left + l);
        xP = xP + d22*(xDash[k+1]-xP);  yP = yP + d22*(yDash[k+1]-yP);
        if(down) {moveToDrawTo(xOld, yOld, 0);
                  moveToDrawTo(xP, yP, 1);}
        xOld = xP;  yOld = yP;
      }//if(del < lenP)
      down = !down;
      if(k >= (nDash-1)) {nDash = 0; return;}
    } //for i
  } //while(down)
  nDash = 0;
  //return;
} //dashIt()
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="Cohen-Sutherland Clip">
//<editor-fold defaultstate="collapsed" desc="class CodeData">
  /**  Class used to retrieve information from method "code" */
  private class CodeData {
      int c = 0;   int i1 = 0; int i2 = 0; int i3 = 0; int i4 = 0;
      CodeData(int c, int i1, int i2, int i3, int i4)
      {this.c = c;   this.i1=i1; this.i2=i2; this.i3=i3; this.i4=i4;}
      CodeData() {}
  } // class CodeData
//</editor-fold>

/** Clip with the Cohen-Sutherlands method.
 * @param lineToClip Line2D
 * @param clippingArea Rectangle2D
 * @return Line2D: the "lineToClip" clipped into the "clippingArea"
 */
private java.awt.geom.Line2D myClip(java.awt.geom.Line2D lineToClip,
        java.awt.geom.Rectangle2D clippingArea) {
  double x1, y1, x2, y2;
  x1= lineToClip.getX1(); y1= lineToClip.getY1();
  x2= lineToClip.getX2(); y2= lineToClip.getY2();
  java.awt.geom.Line2D lineClipped;
  CodeData cd1 =  new CodeData();
  CodeData cd2 =  new CodeData();
  cd1 = code(x1,y1, cd1, clippingArea);
  cd2 = code(x2,y2, cd2, clippingArea);
  while (true) {
    if(cd1.c == 0 && cd2.c == 0) {  //Draw the line
        lineToClip = new java.awt.geom.Line2D.Double(x1,y1, x2,y2);
        return lineToClip;
    }
    if(bitMultipl(cd1.i1, cd2.i1,  cd1.i2, cd2.i2,
                  cd1.i3, cd2.i3,  cd1.i4, cd2.i4) !=0) {
        return null;  //Do Not draw the line
    }
    int c = cd1.c;
    if(c ==0) {c = cd2.c;}
    double x = x1,  y = y1;
    if(c >= 1000) {
      x = x1+(x2-x1)*(clippingArea.getMaxY()-y1)/(y2-y1);
      y = clippingArea.getMaxY();
    }
    if(c < 1000 && c >= 100) {
      x = x1+(x2-x1)*(clippingArea.getMinY()-y1)/(y2-y1);
      y = clippingArea.getMinY();
    }
    if(c < 100 && c >= 10) {
      y = y1+(y2-y1)*(clippingArea.getMaxX()-x1)/(x2-x1);
      x = clippingArea.getMaxX();
    }
    if(c < 10) {
      y = y1+(y2-y1)*(clippingArea.getMinX()-x1)/(x2-x1);
      x = clippingArea.getMinX();
    }
    if(c != cd2.c) {
      x1 = x; y1 = y;
      cd1 = code(x1,y1, cd1, clippingArea);
    } else {
      x2 = x; y2 = y;
      cd2 = code(x2,y2, cd2, clippingArea);
    }
  } //while(true)
} // myClip
private CodeData code(double x, double y, CodeData cd,
        java.awt.geom.Rectangle2D clippingArea) {
    cd.c = 0;  cd.i1 =0; cd.i2 =0; cd.i3 =0; cd.i4 =0;
    if(x < clippingArea.getMinX()) {cd.c =1; cd.i1 =1;}
    if(x > clippingArea.getMaxX()) {cd.c =10; cd.i2 =1;}
    if(y < clippingArea.getMinY()) {cd.c =cd.c+100; cd.i3 =1;}
    if(y > clippingArea.getMaxY()) {cd.c =cd.c+1000; cd.i4 =1;}
    return cd;
} //code
private int bitMultipl(int i1_1, int i1_2,  int i2_1, int i2_2,
                       int i3_1, int i3_2,  int i4_1, int i4_2) {
  int bitMult =0;
  if((i1_1 !=0 && i1_2 !=0) ||
     (i2_1 !=0 && i2_2 !=0) ||
     (i3_1 !=0 && i3_2 !=0) ||
     (i4_1 !=0 && i4_2 !=0))   {bitMult =1;}
  return bitMult;
} //bitMultipl
//</editor-fold>
//</editor-fold>
//</editor-fold>

} 