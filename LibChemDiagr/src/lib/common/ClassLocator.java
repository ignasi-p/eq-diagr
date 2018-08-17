package lib.common;

/** Adapted from http://jroller.com/eu/entry/looking_who_is_calling<br>
 *
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
public class ClassLocator extends SecurityManager {
/** Adapted by Ignasi Puigdomenech<br>
 * from http://jroller.com/eu/entry/looking_who_is_calling<br>
 * (by Eugene Kuleshov). To use:<pre>
 * public class Demo {
 *   public static void main(String[] args) {
 *       A.aa();
 *   }
 * }
 * class A {
 *   static void aa() {
 *     System.err.println(ClassLocator.getCallerClass().getName());
 *   }
 * }</pre>
 * This method (getCallerClass()) is about six times faster than to do:<pre>
 *   Throwable stack = new Throwable();
 *   stack.fillInStackTrace();
 *   StackTraceElement[] stackTE = stack.getStackTrace();
 *   String msg = stackTE[1].getClassName();</pre>
 * @return the class that called this method
 */
  public static Class getCallerClass() {
    Class[] classes = new ClassLocator().getClassContext();
    if(classes.length >0) {return classes[classes.length-1];}
    return null;
  }
  /** Obtains the name of the class that called the method where "getCallerClassName()" is used
   * @return the name of the class  */
  public static String getCallerClassName() {
    Class[] classes = new ClassLocator().getClassContext();
    if(classes.length >0) {
        int i = Math.min(classes.length-1, 2);
        return classes[i].getName();
    }
    return null;
  }
} 