package testHaltaFall;
import lib.kemi.chem.Chem;
import lib.kemi.haltaFall.HaltaFall;
import lib.kemi.haltaFall.Factor;
/**
 * Test of HaltaFall routine
 *  3 chemical components are: H+, e-, Fe(s)
 * 15 aqueous species are:     H+, e-,
 *     Fe 2+, FeOH+, Fe(OH)3 -, Fe(OH)4 2-
 *     Fe 3+, FeOH 2+, Fe(OH)2+, Fe(OH)3, Fe2(OH)2 4+, Fe3(OH)4 5+
 *     OH-, O2(g), H2(g)
 *  3 solid complexes are:   Fe(OH)2(s), Fe3O4(s), FeOOH(s)
 * Because a component is a solid, an extra solid complex is added:  Fe(s)
 * ------------------------------------------------------------------
 * @author Ignasi Puigdomenech
 */
public class TestHaltaFall {
  private static final String nl = System.getProperty("line.separator");
  /**  @param args the command line arguments */
  public static void main(String[] args) {
    int na = 3; // chemical cmponents (both soluble and solids)
    int ms = 19; // species = components (soluble+solid) + complexes (soluble+solid)
    int solidC = 1; // how many components are solid phases
    ms = ms + solidC;   // add one extra species (solid complex)
                        //  for each solid component
    int msol = 4; // solids = 3 solid complexes +1 solid component
    // create an instance of the enclosing class "Chem"
    // this also creates ChemSystem and ChemConc objects
    Chem chem = null;
    try{chem = new Chem(na, ms, msol);}
    catch (Chem.ChemicalParameterException ex) {ex.printStackTrace(); System.exit(1);}
    if(chem == null) {System.out.println("Chem instance failed."); System.exit(1);}
    // get the instance of the inner class "ChemSystem"
    Chem.ChemSystem cs = chem.chemSystem;
    cs.noll = new boolean[cs.Ms];
    for(int i = 0; i < cs.noll.length; i++)
        {cs.noll[i] = false;} // for i
    cs.noll[1] = true; // do not consider the formation of "e-"
    // Note: all soluble complexes must be given 1st, followed by all solids.
    // Note: Here the solids are arranged in the following order:
    // all solid complexes, followed by all solid components.
    // Enter data for complexes
    cs.a = new double[cs.Ms-cs.Na][cs.Na];
    cs.lBeta = new double[cs.Ms-cs.Na];
    cs.a[0][0] = 0.; cs.a[0][1] =-2.; cs.a[0][2] = 1.; cs.lBeta[0] =14.9;  // Fe+2
    cs.a[1][0] =-1.; cs.a[1][1] =-2.; cs.a[1][2] = 1.; cs.lBeta[1] = 5.1;  // FeOH+
    cs.a[2][0] =-3.; cs.a[2][1] =-2.; cs.a[2][2] = 1.; cs.lBeta[2] =-16.88;// Fe(OH)3-
    cs.a[3][0] =-4.; cs.a[3][1] =-2.; cs.a[3][2] = 1.; cs.lBeta[3] =-30.8; // Fe(OH)4-2
    cs.a[4][0] = 0.; cs.a[4][1] =-3.; cs.a[4][2] = 1.; cs.lBeta[4] = 2.43; // Fe+3
    cs.a[5][0] =-1.; cs.a[5][1] =-3.; cs.a[5][2] = 1.; cs.lBeta[5] =-0.62; // FeOH+2
    cs.a[6][0] =-2.; cs.a[6][1] =-3.; cs.a[6][2] = 1.; cs.lBeta[6] =-3.88; // Fe(OH)2+
    cs.a[7][0] =-3.; cs.a[7][1] =-3.; cs.a[7][2] = 1.; cs.lBeta[7] =-10.49;// Fe(OH)3
    cs.a[8][0] =-2.; cs.a[8][1] =-6.; cs.a[8][2] = 2.; cs.lBeta[8] = 1.9;  // Fe2(OH)2+4
    cs.a[9][0] =-4.; cs.a[9][1] =-9.; cs.a[9][2] = 3.; cs.lBeta[9] = 1.52; // Fe3(OH)4+5
    cs.a[10][0]=-1.; cs.a[10][1]= 0.; cs.a[10][2]= 0.; cs.lBeta[10]=-14.2; // OH-
    cs.a[11][0]=-4.; cs.a[11][1]=-4.; cs.a[11][2]= 0.; cs.lBeta[11]=-84.49;// O2(g)
    cs.a[12][0]= 2.; cs.a[12][1]= 2.; cs.a[12][2]= 0.; cs.lBeta[12]=-1.39; // H2(g)
    cs.a[13][0]=-2.; cs.a[13][1]=-2.; cs.a[13][2]= 1.; cs.lBeta[13]= 1.52; // Fe(OH)2(s)
    cs.a[14][0]=-8.; cs.a[14][1]=-8.; cs.a[14][2]= 3.; cs.lBeta[14]= 8.6;  // Fe3O4(s)
    cs.a[15][0]=-3.; cs.a[15][1]=-3.; cs.a[15][2]= 1.; cs.lBeta[15]=-1.12; // FeOOH(s)
    // Data for components
    // create a reference data type variable pointing to the inner class
    Chem.ChemSystem.ChemConcs c = cs.chemConcs;
    for(int i = 0; i < cs.Na; i++) {
        c.kh[i] = 1;  // only total concentrations given (kh=1)
        c.tol = 1.E-5; // tolerance when solving mass-balance equation
        } // for li
    // for solid components: the data for the extra solid complexex
    for(int i =0; i < solidC; i++) {
        int j = (cs.Ms-cs.Na-solidC) +i; // j= (Ms-Na)-solidC ...(Ms-Na)-1
        int k = (cs.Na - solidC) +i; // k = (Na-solidC)...(Na-1)
        cs.noll[k] = true; // solid components are not aqueous specie
        cs.lBeta[j] = 0.; // equilibrium contstant of formation = 1.
        for(int n = 0; n <cs.Na; n++) {
            cs.a[j][n] = 0.;
            if(n == k) {cs.a[j][n] = 1.;}
            } // for n
        } // for i
    // total concentrations for the components
    c.tot[0]= 1.E-5;
    c.tot[1]= -0.15;
    c.tot[2]= 0.1;
    int nIon = cs.Ms - cs.mSol; // nIon= number of aqueous species (comps + complxs)
    System.out.println("Test of HaltaFall"+nl+nl+
            "Nr. Components = "+cs.Na+nl+
            "Nr. Aqueous Species = "+nIon+nl+
            "Nr. Solid Phases = "+cs.mSol+nl+nl+
            "Find pH and pe of an almost neutral aqueous solution where 0.1 mol of"+nl+
            "Fe(s) have been oxidized (0.15 mol e- substracted from the system).");
    System.out.println("Components: Input Tot.Concs. = "+
            java.util.Arrays.toString(c.tot));
    // -------------------
    //c.dbg = 0; // do not print errors nor debug information
    c.dbg = 1; // print errors, but no debug information
    //c.dbg = 2; // print errors and some debug information
    //c.dbg = 3; // print errors and more debug information
    c.cont = false;
    // create an instance of Factor
    Factor f = new Factor(); //ideal solution: all activity coeffs.=1
    // create an instance of class HaltaFall.
    HaltaFall h = null;
    try{h = new HaltaFall(cs, f, System.out);}
    catch (Chem.ChemicalParameterException ex) {ex.printStackTrace(); System.exit(1);}
    if(h == null) {System.out.println("HaltaFall instance failed."); System.exit(1);}
    // do the calculations
    try{h.haltaCalc();}
    catch (Chem.ChemicalParameterException ex) {ex.printStackTrace(); System.exit(1);}
    // -------------------
    System.out.println("RESULTS:       err = "+c.errFlagsToString());
    double pH = -c.logA[0];
    double pe = -c.logA[1];
    java.util.Locale e = java.util.Locale.ENGLISH;
    System.out.format(e,"    pH=%6.3f (should be 6.565),  pe=%7.3f (should be -6.521)%n",pH,pe);
    System.out.println("Components:");
    System.out.format(e,"     Tot.Conc. = %14.6G %14.6G %14.6G%n",
            c.tot[0],c.tot[1],c.tot[2]);
    System.out.format(e,"  Solubilities = %14.6G %14.6G %14.6G%n",
            c.solub[0],c.solub[1],c.solub[2]);
    System.out.format(e,"      log Act. = %14.6f %14.6f %14.6f%n",
            c.logA[0],c.logA[1],c.logA[2]);
//for(int i=0; i<cs.Ms; i++) {c.C[i] = i;c.logA[i]=-i;}
    System.out.println("Aqu.Species  (all components + aqu.complexes):");
    int n0 = 0;     //start index to print
    int nM = nIon-1;  //end index to print
    int iPl = 4; int nP= nM-n0; //items_Per_Line and number of items to print
    if(nP>0) {
    System.out.print("       Conc. =");
    print_1:
    for(int i=0; i<=nP/iPl; i++) { for(int j=0; j<iPl; j++) { int k = n0+(i*iPl+j);
            System.out.format(e," %14.6g",c.C[k]);
            if(k >(nM-1)) {System.out.println(); break print_1;}} //for j
        System.out.println(); System.out.print("              ");
        } //for i
    }
    System.out.println("Solid Phases:");
    n0 = nIon;  //start index to print
    nM = cs.Ms-1;  //end index to print
    nP= nM-n0; if(nP>0) {
    System.out.print("       Conc. =");
    print_1:
    for(int i=0; i<=nP/iPl; i++) { for(int j=0; j<iPl; j++) { int k = n0+(i*iPl+j);
            System.out.format(e," %14.6g",c.C[k]);
            if(k >(nM-1)) {System.out.println(); break print_1;}} //for i2
        System.out.println(); System.out.print("              ");
        } //for i
    }
    n0 = nIon;  //start index to print
    nM = cs.Ms-1;  //end index to print
    nP= nM-n0; if(nP>0) {
    System.out.print("    log Act. =");
    print_1:
    for(int i=0; i<=nP/iPl; i++) { for(int j=0; j<iPl; j++) { int k = n0+(i*iPl+j);
            System.out.format(e," %14.6f",c.logA[k]);
            if(k >(nM-1)) {System.out.println(); break print_1;}} //for i2
        System.out.println(); System.out.print("              ");
        } //for i
    }

    } // main
}
