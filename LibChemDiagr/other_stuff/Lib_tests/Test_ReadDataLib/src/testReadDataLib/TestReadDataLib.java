package testRreadDataLib;

import lib.kemi.readDataLib.ReadDataLib;

public class TestReadDataLib {
    private static ReadDataLib rd;
    private static final String nl = System.getProperty("line.separator");
    public static void main(String[] args) {
        String testFileName = "test.dat";
        System.out.println("Test of \"ReadDataLib\":");
        java.io.File inputFile = new java.io.File(testFileName);
        if(!inputFile.exists()) {
            // --- write a text file to read later
            System.out.println("Writing file: \""+inputFile.getAbsolutePath()+"\"");
            java.io.PrintWriter out = null;
            try{
                out = new java.io.PrintWriter(
                    new java.io.BufferedWriter(
                    new java.io.FileWriter(testFileName)));
            } //try
            catch (Exception e) {
                System.err.print("Error: \""+e.toString()+"\""+nl+
                              "trying to write file: \""+testFileName+"\"");
                try {if(out != null) {out.flush(); out.close();}}
                catch (Exception e1) {}
                return;
            } //catch
            out.println(
                "/ a test file:"+nl+
                "/"+nl+
                "\"text 1\",, text 3 ,"+nl+
                " text 4 "+nl+nl+
                " 1234567890  ,"+nl+nl+
                " 3.1  ,4e-3"+nl+
                "5.1E-5 6.0000000000001"+nl+
                "Line with comment must have \"/\" after a comma or space / Comment 1"+nl+
                "Line without a comment:/ not a comment"+nl+
                ",/ note that this line contains either an empty text string or a zero"+nl+
                "  /a line with only a comment: blank space and end-of-lines do not count"+nl+nl+
                "/another line with only a comment"+nl+
                "7.2,/ a comment after a comma"+nl+
                "-1.e-3,4 5,3600,"+nl+
                "  a line with text  /note: comments not considered for text lines"+nl+
                "/this comment does not 'belong' to any data"+nl+
                "-7 /almost the end"+nl+nl+
                "one comment line"+nl+
                "  two comment /lines"+nl+
                "three comment lines");
            out.flush(); out.close();
        }

        // --- read the test file created above
        //testFileName = "test.dat";
        System.out.println("Reading file: \""+(new java.io.File(testFileName).getAbsolutePath())+"\"");
        try{rd = new ReadDataLib(new java.io.File(testFileName));}
        catch (Exception ex) {System.err.println("Error: "+ex.toString()); return;}

        try{System.out.println("Starting comment=\""+rd.readLine()+"\""+nl);}
        catch (Exception ex) {System.err.println("Error: "+ex.toString()); return;}
        System.out.flush();

        try{System.out.println("Four texts: \""+rd.readA()+"\", \""+rd.readA()+"\", \""+rd.readA()+"\", \""+rd.readA()+"\"");}
        catch (Exception ex) {System.err.println("Error: "+ex.toString()); return;}
        System.out.println("    comment=\""+rd.dataLineComment.toString()+"\""+nl);
        System.out.flush();

        try{System.out.println("Next 5 data: "+rd.readI()+", "+
                                               rd.readR()+", "+rd.readR()+", "+
                                               rd.readR()+", "+rd.readR());}
        catch (Exception ex) {System.err.println("Error: "+ex.toString()); return;}
        System.out.flush();

        try{System.out.println("Text before comment: \""+rd.readA()+"\"");}
        catch (Exception ex) {System.err.println("Error: "+ex.toString()); return;}
        System.out.println("    comment=\""+rd.dataLineComment.toString()+"\""+nl);
        System.out.flush();

        try{System.out.println("No comment: \""+rd.readA()+"\"");}
        catch (Exception ex) {System.err.println("Error: "+ex.toString()); return;}
        System.out.println("    comment=\""+rd.dataLineComment.toString()+"\""+nl);
        System.out.flush();

        try{System.out.println("Empty data: "+rd.readR());}
        catch (Exception ex) {System.err.println("Error: "+ex.toString()); return;}
        System.out.println("    comment=\""+rd.dataLineComment.toString()+"\""+nl);
        System.out.flush();

        try{System.out.println("Next value: "+rd.readR());}
        catch (Exception ex) {System.err.println("Error: "+ex.toString()); return;}
        System.out.println("    comment=\""+rd.dataLineComment.toString()+"\""+nl);
        System.out.flush();

        try{System.out.println("A double: "+rd.readR()+" (skipping the other values in this line)");}
        catch (Exception ex) {System.err.println("Error: "+ex.toString()); return;}
        System.out.println("    comment=\""+rd.dataLineComment.toString()+"\""+nl);
        System.out.flush();

        try{System.out.println("Read a line: \""+rd.readLine()+"\"");}
        catch (Exception ex) {System.err.println("Error: "+ex.toString()); return;}
        System.out.println("    comment=\""+rd.dataLineComment.toString()+"\""+nl);
        System.out.flush();

        try{System.out.println("Next value: "+rd.readR());}
        catch (Exception ex) {System.err.println("Error: "+ex.toString()); return;}
        System.out.println("    comment=\""+rd.dataLineComment.toString()+"\""+nl);
        System.out.flush();

        StringBuilder t = new StringBuilder();
        while(true) {
            try{t.append(rd.readLine());t.append(nl);}
            catch (ReadDataLib.DataEofException ex) {break;}
            catch (Exception ex) {ex.printStackTrace(); break;}
        }
        System.out.println("Last lines=\""+t.toString()+"\"");
        System.out.println("    comment=\""+rd.dataLineComment.toString()+"\""+nl);
        System.out.flush();

        System.out.println("Finished.");
        try{rd.close();}
        catch (Exception ex) {ex.printStackTrace();}
    } // main(args)
} 