package testGraphLib;
import lib.kemi.graph_lib.DiagrPaintUtility;
import lib.kemi.graph_lib.GraphLib;

public class TestGraphLib extends javax.swing.JFrame {
    private javax.swing.JPanel jPanel1;
    private static GraphLib.PltData dd;
    private static DiagrPaintUtility glib = new DiagrPaintUtility();
    private static final boolean TEXT_WITH_FONTS = true;

    public static void main(String[] args) {

        //---- write a test plot file "test_symbol.plt"
        // - create a PltData instance
        dd = new GraphLib.PltData();
        // - create a GraphLib instance
        GraphLib g = new GraphLib();
        //---- make a diagram, store it in "dd" asnd save to a disk file
        try {g.start(dd, new java.io.File("test_symbol.plt"), true);}
        catch (Exception ex) {System.err.println(ex.getMessage()); g.end(); return;}
        float size = 0.3f;
        float x1=0.2f, y=0.2f, x2=14.2f;
        g.setIsFormula(false); g.sym(x1, y, size, "H+  Na +  OH-  F -  HCO3-  H2PO4 -", 0, -1, false);
        g.setIsFormula(true);  g.sym(x2, y, size, "H+  Na +  OH-  F -  HCO3-  H2PO4 -", 0, -1, false);
        y = y + 0.8f;
        g.setIsFormula(false); g.sym(x1, y, size, "SO4-2  CO3 2-  Fe 2+  (UO2)2(OH)2+2", 0, -1, false);
        g.setIsFormula(true);  g.sym(x2, y, size, "SO4-2  CO3 2-  Fe 2+  (UO2)2(OH)2+2", 0, -1, false);
        y = y + 0.8f;
        g.setIsFormula(false); g.sym(x1, y, size, "(PO4-3)  [Fe(CO3)2-2]`TOT'  {SO4-2}", 0, -1, false);
        g.setIsFormula(true);  g.sym(x2, y, size, "(PO4-3)  [Fe(CO3)2-2]`TOT'  {SO4-2}", 0, -1, false);
        y = y + 0.8f;
        g.setIsFormula(false); g.sym(x1, y, size, "(PO4 3-)  [Fe(CO3)2 2-]`TOT'  {SO4 2-}", 0, -1, false);
        g.setIsFormula(true);  g.sym(x2, y, size, "(PO4 3-)  [Fe(CO3)2 2-]`TOT'  {SO4 2-}", 0, -1, false);
        y = y + 0.8f;
        g.setIsFormula(false); g.sym(x1, y, size, "Fe0.943O(c)  NaCl·2H2O  UO2.6667(c)", 0, -1, false);
        g.setIsFormula(true);  g.sym(x2, y, size, "Fe0.943O(c)  NaCl·2H2O  UO2.6667(c)", 0, -1, false);
        y = y + 0.8f;
        g.setIsFormula(false); g.sym(x1, y, size, "CaCO3:2.5H2O  log P`CO2'  E`SHE' 10'-3`", 0, -1, false);
        g.setIsFormula(true);  g.sym(x2, y, size, "CaCO3:2.5H2O  log P`CO2'  E`SHE' 10'-3`", 0, -1, false);
        y = y + 0.8f;
        g.setIsFormula(false); g.sym(x1, y, size, "((CH3)2Hg)-2  (S6) 2-  (Al(OH)2)2 2+", 0, -1, false);
        g.setIsFormula(true);  g.sym(x2, y, size, "((CH3)2Hg)-2  (S6) 2-  (Al(OH)2)2 2+", 0, -1, false);
        y = y + 0.8f;
        g.setIsFormula(false); g.sym(x1, y, size, "[(CH3)2Hg]-2  [S6] 2-  [Al(OH)2]2 2+", 0, -1, false);
        g.setIsFormula(true);  g.sym(x2, y, size, "[(CH3)2Hg]-2  [S6] 2-  [Al(OH)2]2 2+", 0, -1, false);
        y = y + 0.8f;
        g.setIsFormula(false); g.sym(x1, y, size, "{(CH3)2Hg}-2  {S6} 2-  {Al(OH)2}2 2+", 0, -1, false);
        g.setIsFormula(true);  g.sym(x2, y, size, "{(CH3)2Hg}-2  {S6} 2-  {Al(OH)2}2 2+", 0, -1, false);
        y = y + 0.8f;
        g.setIsFormula(false); g.sym(x1, y, size, "Pu38O56Cl54-14  W14O41 10+  B22H66-23  Fe8 23+", 0, -1, false);
        g.setIsFormula(true);  g.sym(x2, y, size, "Pu38O56Cl54-14  W14O41 10+  B22H66-23  Fe8 23+", 0, -1, false);
        y = y + 0.8f;
        g.setIsFormula(false); g.sym(x1, y, size, "(W14O41 10-) [W14O41 10+] {B22H66-23} [Fe8]+23", 0, -1, false);
        g.setIsFormula(true);  g.sym(x2, y, size, "(W14O41 10-) [W14O41 10+] {B22H66-23} [Fe8]+23", 0, -1, false);
        y = y + 0.8f;
        g.setIsFormula(false); g.sym(x1, y, size, "\"W14O41\"-11 \"W14O41\" 10+ \"N2O3\"- \"Cu2\"+", 0, -1, false);
        g.setIsFormula(true);  g.sym(x2, y, size, "\"W14O41\"-11 \"W14O41\" 10+ \"N2O3\"- \"Cu2\"+", 0, -1, false);
        y = y + 0.8f;
        g.setIsFormula(false); g.sym(x1, y, size, "Fe+2 -2H+=Fe(OH)2   Ca+2 +CO3-2 = CaCO3(s)", 0, -1, false);
        g.setIsFormula(true);  g.sym(x2, y, size, "Fe+2 -2H+=Fe(OH)2   Ca+2 +CO3-2 = CaCO3(s)", 0, -1, false);
        y = y + 0.8f;
        g.setIsFormula(false); g.sym(x1, y, size, "x'2` + v`i' - log`10'f = 2·10'-3`bar", 0, -1, false);
        g.setIsFormula(true);  g.sym(x2, y, size, "x'2` + v`i' - log`10'f = 2·10'-3`bar", 0, -1, false);
        y = y + 0.8f;
        g.setIsFormula(false); g.sym(x1, y, size, "^H`f''o` =5;  f`Fe3O4'=3$M  t=25~C", 0, -1, false);
        g.setIsFormula(true);  g.sym(x2, y, size, "^H`f''o` =5;  f`Fe3O4'=3$M  t=25~C", 0, -1, false);
        y = y + 0.8f;
        g.setIsFormula(false); g.sym(x1, y, size, "^G`f'~=3.9 kJ'.`mol'-1`  P`CO2'", 0, -1, false);
        g.setIsFormula(true);  g.sym(x2, y, size, "^G`f'~=3.9 kJ'.`mol'-1`  P`CO2'", 0, -1, false);
        y = y + 0.8f;
        g.setIsFormula(false); g.sym(x1, y, size, "·ÅÄÖåäöñ &%#~^$@→°º±•×µ‰%", 0, -1, false);
        g.setIsFormula(true);  g.sym(x2, y, size, "·ÅÄÖåäöñ &%#~^$@→°º±•×µ‰%", 0, -1, false);
        y = y + 0.8f;
        g.setIsFormula(false); g.sym(x1, y, size, "Non-symbol style:", 0, -1, false);
        g.setIsFormula(true);  g.sym(x2, y, size, "Symbol style:", 0, -1, false);
        // finished
        g.end();
        System.out.println("Written: \"test_symbol.plt\"");

        //---- write another test plot file "test_rotated_text.plt"
        // - create a PltData instance
        dd = new GraphLib.PltData();
        // - create a GraphLib instance
        g = new GraphLib();
        //---- make a diagram, store it in "dd" and save to a disk file
        try {g.start(dd, new java.io.File("test_rotated_text.plt"), true);}
        catch (Exception ex) {System.err.println(ex.getMessage()); g.end(); return;}
        g.setIsFormula(true);
        float x0=5f, y0=5f, r=2f, angle =0f;
        float f = 3.14159265f/180f;
        size = 0.35f;
        String t = "(PO4-3)  [Fe(CO3)2-2]`TOT'  {SO4-2}  (UO2)2(OH)2+2";
        x1 = x0 + r*(float)Math.cos(angle*f); y = y0 + r*(float)Math.sin(angle*f);
        g.sym(x1, y, size, t, angle, -1, false);
        angle = 22f;
        x1 = x0 + r*(float)Math.cos(angle*f); y = y0 + r*(float)Math.sin(angle*f);
        g.sym(x1, y, size, t, angle, -1, false);
        angle = 45f;
        x1 = x0 + r*(float)Math.cos(angle*f); y = y0 + r*(float)Math.sin(angle*f);
        g.sym(x1, y, size, t, angle, -1, false);
        angle = 67f;
        x1 = x0 + r*(float)Math.cos(angle*f); y = y0 + r*(float)Math.sin(angle*f);
        g.sym(x1, y, size, t, angle, -1, false);
        angle = 90f;
        x1 = x0 + r*(float)Math.cos(angle*f); y = y0 + r*(float)Math.sin(angle*f);
        g.sym(x1, y, size, t, angle, -1, false);
        angle = 180f;
        x1 = x0 + r*(float)Math.cos(angle*f); y = y0 + r*(float)Math.sin(angle*f);
        g.sym(x1, y, size, t, angle, -1, false);
        angle = 180f + 30f;
        x1 = x0 + r*(float)Math.cos(angle*f); y = y0 + r*(float)Math.sin(angle*f);
        g.sym(x1, y, size, t, angle, -1, false);
        angle = 180f + 60f;
        x1 = x0 + r*(float)Math.cos(angle*f); y = y0 + r*(float)Math.sin(angle*f);
        g.sym(x1, y, size, t, angle, -1, false);
        angle = -30f;
        x1 = x0 + r*(float)Math.cos(angle*f); y = y0 + r*(float)Math.sin(angle*f);
        g.sym(x1, y, size, t, angle, -1, false);
        angle = -60f;
        x1 = x0 + r*(float)Math.cos(angle*f); y = y0 + r*(float)Math.sin(angle*f);
        g.sym(x1, y, size, t, angle, -1, false);
        angle = -90f;
        x1 = x0 + r*(float)Math.cos(angle*f); y = y0 + r*(float)Math.sin(angle*f);
        g.sym(x1, y, size, t, angle, -1, false);
        angle = -180f - 45f;
        x1 = x0 + r*(float)Math.cos(angle*f); y = y0 + r*(float)Math.sin(angle*f);
        g.sym(x1, y, size, t, angle, -1, false);
        // finished
        g.end();
        System.out.println("Written: \"test_rotated_text.plt\"");

        //---- write another test plot file "test_all.plt"
        // - create a PltData instance
        dd = new GraphLib.PltData();
        // - create a GraphLib instance
        g = new GraphLib();
        //---- make a diagram, store it in "dd" and save to a disk file
        try {g.start(dd, new java.io.File("test_all.plt"), true);}
        catch (Exception ex) {System.err.println(ex.getMessage()); g.end(); return;}
        size = 0.25f;
        String t1 = "^[CO3 2-]`TOT'", t2 = "123.456";
        int n1 = t1.length(),  n2 = t2.length();
        float a0 =0, a45 = 45, a90 = 95; // angles
        int align;
        x0 = 0f;
        y0 = 25f;

        g.setPen(1);  g.setPen(-1);
        g.sym(x0, y0, size, "Not \"formula\"", 0f, -1, false);
        g.sym(x0+((Math.max(n1,n2)+1)*size)*3, y0, size, "\"Formula\"", 0f, -1, false);
        g.sym(x0+2*size, y0-1.7*size, size, "Align: Left", 0f, -1, false);
        g.sym(x0+(2+(Math.max(n1,n2)+1))*size, y0-1.7*size, size, "Centre", 0f, -1, false);
        g.sym(x0+(2+2*(Math.max(n1,n2)+1))*size, y0-1.7*size, size, "Right", 0f, -1, false);
        g.sym(x0+(2+3*(Math.max(n1,n2)+1))*size, y0-1.7*size, size, "Align: Left", 0f, -1, false);
        g.sym(x0+(2+4*(Math.max(n1,n2)+1))*size, y0-1.7*size, size, "Centre", 0f, -1, false);
        g.sym(x0+(2+5*(Math.max(n1,n2)+1))*size, y0-1.7*size, size, "Right", 0f, -1, false);

        // ---- pen thickness ----
        int nPen =1;
        while(nPen <=2) {
        g.setPen(nPen);  g.setPen(-nPen);
        // ---- not formulas ----
        g.setIsFormula(false);
        // ---- align left
        align = -1; //left
        // first text
        g.sym  (x0, y0-(n1+3)*size, size, t1, a0, align, false);
        textBox(x0, y0-(n1+3)*size, size, size*n1, a0, g);
        g.sym  (x0, y0-(n1+2)*size, size, t1, a45, align, false);
        textBox(x0, y0-(n1+2)*size, size, size*n1, a45, g);
        g.sym  (x0, y0-(n1+1)*size, size, t1, a90, align, false);
        textBox(x0, y0-(n1+1)*size, size, size*n1, a90, g);

        // second text
        g.sym  (x0, y0-((n1+3)+(n2+3))*size, size, t2, a0, align, false);
        textBox(x0, y0-((n1+3)+(n2+3))*size, size, size*n2, a0, g);
        g.sym  (x0, y0-((n1+3)+(n2+2))*size, size, t2, a45, align, false);
        textBox(x0, y0-((n1+3)+(n2+2))*size, size, size*n2, a45, g);
        g.sym  (x0, y0-((n1+3)+(n2+1))*size, size, t2, a90, align, false);
        textBox(x0, y0-((n1+3)+(n2+1))*size, size, size*n2, a90, g);

        // ---- align centre
        align = 0; //centre
        x0 = x0 + (Math.max(n1,n2)+1)*size;
        // first text
        g.sym  (x0, y0-(n1+3)*size, size, t1, a0, align, false);
        textBox(x0, y0-(n1+3)*size, size, size*n1, a0, g);
        g.sym  (x0, y0-(n1+2)*size, size, t1, a45, align, false);
        textBox(x0, y0-(n1+2)*size, size, size*n1, a45, g);
        g.sym  (x0, y0-(n1+1)*size, size, t1, a90, align, false);
        textBox(x0, y0-(n1+1)*size, size, size*n1, a90, g);

        // second text
        g.sym  (x0, y0-((n1+3)+(n2+3))*size, size, t2, a0, align, false);
        textBox(x0, y0-((n1+3)+(n2+3))*size, size, size*n2, a0, g);
        g.sym  (x0, y0-((n1+3)+(n2+2))*size, size, t2, a45, align, false);
        textBox(x0, y0-((n1+3)+(n2+2))*size, size, size*n2, a45, g);
        g.sym  (x0, y0-((n1+3)+(n2+1))*size, size, t2, a90, align, false);
        textBox(x0, y0-((n1+3)+(n2+1))*size, size, size*n2, a90, g);

        // ---- align right
        align = 1; //right
        x0 = x0 + (Math.max(n1,n2)+1)*size;
        // first text
        g.sym  (x0, y0-(n1+3)*size, size, t1, a0, align, false);
        textBox(x0, y0-(n1+3)*size, size, size*n1, a0, g);
        g.sym  (x0, y0-(n1+2)*size, size, t1, a45, align, false);
        textBox(x0, y0-(n1+2)*size, size, size*n1, a45, g);
        g.sym  (x0, y0-(n1+1)*size, size, t1, a90, align, false);
        textBox(x0, y0-(n1+1)*size, size, size*n1, a90, g);

        // second text
        g.sym  (x0, y0-((n1+3)+(n2+3))*size, size, t2, a0, align, false);
        textBox(x0, y0-((n1+3)+(n2+3))*size, size, size*n2, a0, g);
        g.sym  (x0, y0-((n1+3)+(n2+2))*size, size, t2, a45, align, false);
        textBox(x0, y0-((n1+3)+(n2+2))*size, size, size*n2, a45, g);
        g.sym  (x0, y0-((n1+3)+(n2+1))*size, size, t2, a90, align, false);
        textBox(x0, y0-((n1+3)+(n2+1))*size, size, size*n2, a90, g);


        // ---- formulas ----
        g.setIsFormula(true);
        x0 = x0 + (Math.max(n1,n2)+1)*size;
        // ---- align left
        align = -1; //left
        // first text
        g.sym  (x0, y0-(n1+3)*size, size, t1, a0, align, false);
        textBox(x0, y0-(n1+3)*size, size, size*n1, a0, g);
        g.sym  (x0, y0-(n1+2)*size, size, t1, a45, align, false);
        textBox(x0, y0-(n1+2)*size, size, size*n1, a45, g);
        g.sym  (x0, y0-(n1+1)*size, size, t1, a90, align, false);
        textBox(x0, y0-(n1+1)*size, size, size*n1, a90, g);

        // second text
        float size0 = size*1.2f;
        g.sym  (x0, y0-((n1+3)+(n2+3))*size, size0, t2, a0, align, false);
        textBox(x0, y0-((n1+3)+(n2+3))*size, size0, size*n2, a0, g);
        g.sym  (x0, y0-((n1+3)+(n2+2))*size, size0, t2, a45, align, false);
        textBox(x0, y0-((n1+3)+(n2+2))*size, size0, size*n2, a45, g);
        g.sym  (x0, y0-((n1+3)+(n2+1))*size, size0, t2, a90, align, false);
        textBox(x0, y0-((n1+3)+(n2+1))*size, size0, size*n2, a90, g);

        // ---- align centre
        align = 0; //centre
        x0 = x0 + (Math.max(n1,n2)+1)*size;
        // first text
        g.sym  (x0, y0-(n1+3)*size, size, t1, a0, align, false);
        textBox(x0, y0-(n1+3)*size, size, size*n1, a0, g);
        g.sym  (x0, y0-(n1+2)*size, size, t1, a45, align, false);
        textBox(x0, y0-(n1+2)*size, size, size*n1, a45, g);
        g.sym  (x0, y0-(n1+1)*size, size, t1, a90, align, false);
        textBox(x0, y0-(n1+1)*size, size, size*n1, a90, g);

        // second text
        g.sym  (x0, y0-((n1+3)+(n2+3))*size, size, t2, a0, align, false);
        textBox(x0, y0-((n1+3)+(n2+3))*size, size, size*n2, a0, g);
        g.sym  (x0, y0-((n1+3)+(n2+2))*size, size, t2, a45, align, false);
        textBox(x0, y0-((n1+3)+(n2+2))*size, size, size*n2, a45, g);
        g.sym  (x0, y0-((n1+3)+(n2+1))*size, size, t2, a90, align, false);
        textBox(x0, y0-((n1+3)+(n2+1))*size, size, size*n2, a90, g);

        // ---- align right
        align = 1; //right
        x0 = x0 + (Math.max(n1,n2)+1)*size;
        // first text
        g.sym  (x0, y0-(n1+3)*size, size, t1, a0, align, false);
        textBox(x0, y0-(n1+3)*size, size, size*n1, a0, g);
        g.sym  (x0, y0-(n1+2)*size, size, t1, a45, align, false);
        textBox(x0, y0-(n1+2)*size, size, size*n1, a45, g);
        g.sym  (x0, y0-(n1+1)*size, size, t1, a90, align, false);
        textBox(x0, y0-(n1+1)*size, size, size*n1, a90, g);

        // second text
        g.sym  (x0, y0-((n1+3)+(n2+3))*size, size, t2, a0, align, false);
        textBox(x0, y0-((n1+3)+(n2+3))*size, size, size*n2, a0, g);
        g.sym  (x0, y0-((n1+3)+(n2+2))*size, size, t2, a45, align, false);
        textBox(x0, y0-((n1+3)+(n2+2))*size, size, size*n2, a45, g);
        g.sym  (x0, y0-((n1+3)+(n2+1))*size, size, t2, a90, align, false);
        textBox(x0, y0-((n1+3)+(n2+1))*size, size, size*n2, a90, g);

        // ---- thick pen ----
        nPen++;
        x0 = 0f;
        y0 = y0 -((n1+3)+(n2+1))*size;
        } // while pen <=2
        // finished
        g.end();
        System.out.println("Written: \"test_all.plt\"");

        //---- display a frame
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {new TestGraphLib().setVisible(true);}
        });
    } // main(args)

//<editor-fold defaultstate="collapsed" desc="textBox">
  /** draw a box around an assumed text
   * @param x0 the x-position of the text-box (lower left corner)
   * @param y0 the y-position of the text-box (lower left corner)
   * @param height the height of the box
   * @param width the width of the box
   * @param angle the angle at which the text is supposed to be printed
   * @param g
   * @return an instance of BoundingBox with the four corners of the box
   */
  private static void textBox (float x0, float y0,
            float height, float width, float angle,
            GraphLib g) {
        final double DEG_2_RAD = 0.017453292519943;
        final double a0 = angle *  DEG_2_RAD;
        final double a1 = (angle + 90) *  DEG_2_RAD;
        Point p1 = new Point(),  p2 = new Point(), p3 = new Point();
        p1.x = x0 + width * (float)Math.cos(a0);
        p1.y = y0 + width * (float)Math.sin(a0);
        p2.x = p1.x + height * (float)Math.cos(a1);
        p2.y = p1.y + height * (float)Math.sin(a1);
        p3.x = x0 + height * (float)Math.cos(a1);
        p3.y = y0 + height * (float)Math.sin(a1);
        g.setLabel("TextBox w="+width+" h="+height);
        g.moveToDrawTo(x0, y0, 0);
        g.moveToDrawTo(p1.x, p1.y, 1);
        g.moveToDrawTo(p2.x, p2.y, 1);
        g.moveToDrawTo(p3.x, p3.y, 1);
        g.setLabel("TextBox end");
        g.moveToDrawTo(x0, y0, 1);
  }
private static class Point {
   public float x, y;
   public Point(float x, float y) {this.x = x;  this.y = y;}
   public Point() {x = Float.NaN;y = Float.NaN;}
   @Override
   public String toString() {return "["+x+","+y+"]";}
}
//</editor-fold>


    public TestGraphLib() { // constructor
        jPanel1 = new javax.swing.JPanel() {
            @Override public void paint(java.awt.Graphics g) {
              super.paint(g);
              java.awt.Graphics2D g2D = (java.awt.Graphics2D)g;
              glib.paintDiagram(g2D, jPanel1.getSize(), dd, false);
            } // paint
            }; // JPanel
        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setPreferredSize(new java.awt.Dimension(640,470));
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new java.awt.BorderLayout());
        getContentPane().add(jPanel1);
        this.setLocation(60, 30);
        pack();
        // - create a PltData instance
        dd = new GraphLib.PltData();
        // - create a GraphLib instance
        GraphLib g = new GraphLib();
        // -  make a diagram, store it in "dd" but do not save to a disk file
        try {g.start(dd, null, TEXT_WITH_FONTS);}
        catch (Exception ex) {System.err.println(ex.getMessage()); g.end(); return;}
        glib.textWithFonts = TEXT_WITH_FONTS;

        dd.axisInfo = false;
        float xAxl = 15, yAxl = 10, xOr = 4, yOr = 5;
        float heightAx = (0.03f*Math.max(xAxl, yAxl));

        // draw axes
        try {g.axes(0f, 1f, 10f, 11f,  xOr,yOr, xAxl,yAxl, heightAx,
                false, true, true);}
        catch (Exception ex) {System.err.println("Error: "+ex.getMessage()); g.end(); return;}
        // draw a line
        g.lineType(1);
        double[] xd = {0.2,1.75}, yd = {10.5,10.8};
        g.line(xd, yd);
        // draw a text
        g.setIsFormula(true);
        g.sym(xOr+0.5*xAxl, yOr+1, heightAx, "P`CO`2'' = 10 bar", 45, 0, false);

        // finished
        g.end();
        //---- now the "paint" method of jPanel1 will call method
        //     "graphLib.DiagrPaintUtility.paintDiagram"
        //     using the PltData instance "dd".
        return;
    } // Test() constructor

}