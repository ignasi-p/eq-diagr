package lib.database;

/** Get a list of texts from a CSV line (= <i>comma</i>-separated values)<br>
 * - <code>Separator</code> = either comma (,) or semicolon (;)<br>
 * - <code>Quote</code> = either single (') or double (")<br>
 * - To include <code>separators</code> in a token, enclose the token in <code>quotes</code>: "a,b"<br>
 * - To include either trailing or preceeding white space, enclose the token in <code>quotes</code><br>
 * - It is OK to include <code>quotes</code> in a text, if it has no <code>separator</code> in it,
 * and if the <code>quote</code> is not the first character, for example: someone's<br>
 * - To include <code>quotes</code> in a text that needs to be quoted
 * (either because the <code>quote</code> is the first character, or because the text
 * also contains a <code>separator</code>):<br>
 * -- either have two quotes after each other, for example "he said, ""hello"","<br>
 * -- or use single quotes to enclose double quotes, as in 'a,b";', or use double
 * quotes to enclose a single quote: "this is 'ok',".
 * <br> 
 * Copyright (C) 2014 I.Puigdomenech.
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
public class CSVparser {
  //adapted from
  // http://publib.boulder.ibm.com/infocenter/pim/v6r0m0/topic/com.ibm.wpc.dev.doc/code/java/wpc_con_CSVParserjava.html
  private static final String nl = System.getProperty("line.separator");

/** Get a list of texts from a CSV line (= <i>comma</i>-separated values)<br>
 * - <code>Separator</code> = either comma (,) or semicolon (;)<br>
 * - <code>Quote</code> = either single (') or double (")<br>
 * - To include <code>separators</code> in a token, enclose the token in <code>quotes</code>: "a,b"<br>
 * - To include either trailing or preceeding white space, enclose the token in <code>quotes</code><br>
 * - It is OK to include <code>quotes</code> in a text, if it has no <code>separator</code> in it,
 * and if the <code>quote</code> is not the first character, for example: someone's<br>
 * - To include <code>quotes</code> in a text that needs to be quoted
 * (either because the <code>quote</code> is the first character, or because the text
 * also contains a <code>separator</code>):<br>
 * .-. either have two quotes after each other, for example "said, ""hello"","<br>
 * .-. or use single quotes to enclose double quotes, as in 'a,b";', or use double
 * quotes to enclose a single quote: "this is 'ok',"
 * @param line to be parsed into tokens (Strings)
 * @return a list of Strings
 * @see CSVparser#splitLine_N splitLine_N
 * @throws CSVdataException */
    public static java.util.ArrayList<String> splitLine(String line) throws CSVdataException {
        if(line == null) {return null;}

        java.util.ArrayList<String> al = new java.util.ArrayList<String>();
        OneRes or = new OneRes();
        int pos = 0;

        while (pos < line.length()) {
            pos = findNextComma(pos, line, or);
            al.add(or.oneRes);
//System.out.println("token "+al.size()+" = "+al.get(al.size()-1)+" pos = "+pos+" ("+line.length()+")");
            pos++;
        } //while

        if(line.length() > 0 && 
                (line.charAt(line.length() - 1) == ',' || line.charAt(line.length() - 1) == ';'))
        {al.add("");}

        return al;
    } //splitLine(line)

/** Get a list of texts from a CSV line (= <i>comma</i>-separated values)<br>
 * @param line to be parsed into tokens (Strings)
 * @param n the size of the returned array list. If not enough data are found in <code>line</code>
 * then empty Strings are added. If too many data are found, then the the remainder of the line
 * is returned in the last item of the array list.
 * @return array list of Strings
 * @see CSVparser#splitLine splitLine 
 * @see CSVparser#splitLine_1 splitLine_1
 * @throws CSVdataException */
    public static java.util.ArrayList<String> splitLine_N(String line, int n) throws CSVdataException {
        if(line == null || n <=0) {return null;}
        java.util.ArrayList<String> al = new java.util.ArrayList<String>(n);
        if(n == 1) {al.add(splitLine_1(line)); return al;}

        OneRes or = new OneRes();
        int pos = 0;
        int size = 0;

        while (pos < line.length()) {
            pos = findNextComma(pos, line, or);
            al.add(or.oneRes); size++;
            pos++;
            if(size >= n) {break;}
        } //while

        if(size <= (n-1)) {for(int i =size; i < n; i++) {al.add("");}}

        return al;
    } //splitLine_N(line, n)

/** Get the first text from a CSV line (= <i>comma</i>-separated values)<br>
 * @param line to be parsed
 * @return the first String token from the line
 * @see CSVparser#splitLine splitLine 
 * @see CSVparser#splitLine_N splitLine_N
 * @throws CSVdataException */
    public static String splitLine_1(String line) throws CSVdataException {
        if(line == null) {return null;}

        OneRes or = new OneRes();
        findNextComma(0, line, or);

        return or.oneRes;
    } //splitLine_N(line, n)

  //<editor-fold defaultstate="collapsed" desc="private">
    private static int findNextComma(int p, String line, OneRes or) throws CSVdataException {
        char c;
        int i,j,k;
        or.oneRes = "";
        c = line.charAt(p);
        // remove white space at the beginning
        while (Character.isWhitespace(c)) {
            p++;
            if(p>=line.length()) {or.oneRes = "";  return p;}
            c = line.charAt(p);
        }
//System.out.println("findNextComma 1st char = ("+c+")  p="+p);
        if(c == ',' || c == ';') { // --- empty field
            or.oneRes = "";
            return p;
        }

        if(c != '"' && c != '\'') { // --- not a quote char
            // find next separator
            i = line.length();
            j = line.indexOf(',', p);
            k = line.indexOf(';', p);
            if(j > -1 && j < i) {i = j;}
            if(k > -1 && k < i) {i = k;}
            or.oneRes = line.substring(p, i).trim();
            return i;
        }

        k = p;
        char quote = c;     // --- starts with quote (either " or ')
        p++;
        StringBuilder sb = new StringBuilder(200);
        while (true) {
            if(p >= line.length()) {
                throw new CSVdataException("Error: missing closing quote in line"+nl+"   "+line+nl+
                        "in text: "+quote+sb.toString()+"  (starting at position "+(k+1)+")");
            }
            c = line.charAt(p);
            p++;
//System.out.println("     p="+p+"  char = ("+c+")");

            // if this is not a quote
            if(c != quote) {sb.append(c);  continue;}

            // this is a quote and last char -> ok
            if(p == line.length()) {or.oneRes = sb.toString();  return p;}

            c = line.charAt(p);
            p++;
//System.out.println("        p="+p+"  char = ("+c+")");

            // "" -> just get one
            if(c == quote) {
                sb.append(quote);
                continue;
            }

            // remove white space after a closing quote
            while (Character.isWhitespace(c)) {
                if(p>=line.length()) {or.oneRes = sb.toString();  return p;}
                c = line.charAt(p);
                p++;
            }
//System.out.println("        p="+p+"  char = ("+c+")");

            // closing quote followed by separator -> return
            if(c == ',' || c == ';') {
                or.oneRes = sb.toString();
                return p - 1;
            }
            throw new CSVdataException("Unexpected token found in line:"+nl+"   "+line+nl+
                    "token: "+quote+sb.toString()+quote+"  (starting at position "+(k+1)+")");

        } //while
    } //findNextComma(p)

/** A class of objects that contain <code>String oneRes</code>.
 * May be used as pointer when calling procedures. */
    private static class OneRes {
        /** The only field of class "OneRes"; the objects of this class may be
         * used as pointers to Strings when calling procedures. */
        String oneRes = "";
/** creates an object that contains <code>String oneRes</code>.
 * A reference to this instance can be used when calling procedures */
        public OneRes() {}
    }
//</editor-fold>

public static class CSVdataException extends Exception {
    public CSVdataException() {super();}
    public CSVdataException(String txt) {super(txt);}
} //AddDataInternalException

}
