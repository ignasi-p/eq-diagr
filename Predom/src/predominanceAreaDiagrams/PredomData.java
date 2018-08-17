package predominanceAreaDiagrams;

/** A class that contains diverse information associated with a Predom diagram.
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
public class PredomData {
/** <code>pair[i][0]</code> and <code>pair[i][1]</code> = number of the
 * two chemical species that in the predominance area diagram are separated
 * by the line going through the point "i". <code>pair[i][2]</code> = number
 * of a third species involved in the point "i". */
public int[][] pair;
/** X-coordinate of a point to plot; a point that is a frontier between
 * two predominating species */
public double[] xPl;
/** Y-coordinate of a point to plot; a point that is a frontier between
 * two predominating species */
public double[] yPl;
/** number of points to plot, that is, the number of points that are
 * the borderlines between predominance areas */
public int nPoint;
/** The size of the step in the X-axis */
public double stepX;
/** The size of the step in the Y-axis */
public double stepY;
/** The leftmost value in the x-axis */
public double xLeft;
/** The rightmost value in the x-axis */
public double xRight;
/** The bottom value in the y-axis */
public double yBottom;
/** The top value in the y-axis */
public double yTop;
/** X-value for the center of the predominance area for each species */
public double[] xCentre;
/** Y-value for the center of the predominance area for each species */
public double[] yCentre;
/** Max number of points to be plotted. In a predominance area diagram
 * these are the points delimiting the areas. */
public final int mxPNT;

/** Constructs an instance
 * @param ms number of species
 * @param maxPNT Max. number of points to be plotted. In a predominance area diagram
 * these are the points delimiting the areas. */
public PredomData(int ms, int maxPNT){
  mxPNT = maxPNT;
  nPoint = 0;
  pair = new int[mxPNT][3];
  for(int[] pair1 : pair) {pair1[0] = -1;  pair1[1] = -1;  pair1[2] = -1;}
  xPl = new double[mxPNT];
  yPl = new double[mxPNT];
  xCentre = new double[2*ms];
  yCentre = new double[2*ms];
  for(int i=0; i < xCentre.length; i++) {xCentre[i]=-100000; yCentre[i]=-100000;}
} // constructor

} // class PredomData
