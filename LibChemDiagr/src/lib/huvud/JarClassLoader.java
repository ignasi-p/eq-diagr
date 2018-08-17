package lib.huvud;

/**  A class loader for loading jar files, both local and remote.
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
public class JarClassLoader extends java.net.URLClassLoader {
    private final java.net.URL url;

    /** Creates a new JarClassLoader for the specified url.
     * @param url the url of the jar file */
    public JarClassLoader(java.net.URL url) {
        super(new java.net.URL[] { url });
        this.url = url;}

    /** Returns the name of the jar file main class, or null if
     * no "Main-Class" manifest attributes is defined.
     * @return 
     * @throws java.io.IOException  */
    public String getMainClassName() throws java.io.IOException {
        String nl = System.getProperty("line.separator");
        //The syntax for the URL of a JAR file is:
        //  jar:http://www.zzz.yyy/jarfile.jar!/
        java.net.URL u = new java.net.URL("jar", "", url + "!/");
        java.net.JarURLConnection uc = (java.net.JarURLConnection)u.openConnection();
        java.util.jar.Attributes attr = uc.getMainAttributes();
        //return attr != null ? attr.getValue(java.util.jar.Attributes.Name.MAIN_CLASS) : null;
        if(attr == null) {
          return null;
        } else {
          // add the jar-file url to the "class loader"
          java.net.URLClassLoader classLoader
                = (java.net.URLClassLoader) ClassLoader.getSystemClassLoader();
          // use reflection
          String msg = null;
          try {
              java.lang.reflect.Method method =
                        java.net.URLClassLoader.class.
                                getDeclaredMethod("addURL", new Class[] {java.net.URL.class});
              method.setAccessible(true);
              method.invoke(classLoader, new Object[] { url });
              method.setAccessible(false);
            //System.out.println("added to URLClassLoader: "+url.toString());
          }
          catch (NoSuchMethodException ex) {msg = ex.toString();}
          catch (SecurityException ex) {msg = ex.toString();}
          catch (IllegalAccessException ex) {msg = ex.toString();}
          catch (IllegalArgumentException ex) {msg = ex.toString();}
          catch (java.lang.reflect.InvocationTargetException ex) {msg = ex.toString();}
          if(msg != null) {System.out.println("Warning - Failed to add to URLClassLoader:"+
              nl+url.toString()+nl+msg);}
          return attr.getValue(java.util.jar.Attributes.Name.MAIN_CLASS);
        }
    }

    /** Invokes the application in this jar file given the name of the
     * main class and an array of arguments. The class must define a
     * static method "main" which takes an array of String arguemtns
     * and is of return type "void".
     *
     * @param name the name of the main class
     * @param args the arguments for the application
     * @exception ClassNotFoundException if the specified class could not
     *            be found
     * @exception NoSuchMethodException if the specified class does not
     *            contain a "main" method
     * @exception java.lang.reflect.InvocationTargetException if the application raised an
     *            exception
     */
    public void invokeClass(String name, String[] args)
        throws ClassNotFoundException,
               NoSuchMethodException,
               java.lang.reflect.InvocationTargetException {
        Class<?> c = findLoadedClass(name); // added by Ignasi Puigdomenech   // added "<?>" (Ignasi Puigdomenech)
        if(c == null) {c = loadClass(name);}
        if(args == null) {args = new String[]{""};} // added by Ignasi Puigdomenech
        //java.lang.reflect.Method m = c.getMethod("main", new Class[] { args.getClass() });
        // changed by I.Puigdomenech:
        java.lang.reflect.Method m = c.getMethod("main", new Class<?>[] { String[].class });
        m.setAccessible(true);
        int mods = m.getModifiers();
        if (m.getReturnType() != void.class ||
            !java.lang.reflect.Modifier.isStatic(mods) ||
            !java.lang.reflect.Modifier.isPublic(mods))
                {throw new NoSuchMethodException("main");}
        try {
            m.invoke(null, new Object[] { args });
        } catch (IllegalAccessException e) {
            // This should not happen, as we have disabled access checks
        }
    } // invokeClass(name, args[])
} 