package lib.huvud;

import lib.common.MsgExceptn;

/**  A class loader for loading jar files, both local and remote.
 *
 * Copyright (C) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 * Modified by I.Puigdomenech (20014-2020)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class JarClassLoader extends java.net.URLClassLoader {
    private final java.net.URL url;

    /** Creates a new JarClassLoader for the specified url.
     * @param url the url of the jar file */
    public JarClassLoader(java.net.URL url) {
        super(new java.net.URL[] { url });
        this.url = url;
    }

    /** Returns the name of the jar file main class, or null if
     * no "Main-Class" manifest attributes is defined.
     * @return 
     * @throws java.io.IOException  */
    public String getMainClassName() throws java.io.IOException {
        String nl = System.getProperty("line.separator");
        //The syntax for the URL of a JAR file is:
        //  jar:http://www.zzz.yyy/jarfile.jar!/
        java.net.URL u = new java.net.URL("jar", "", url + "!/");
        // modified by I.Puigdomenech to catch cast problems
        java.net.JarURLConnection uc = null;
        try{uc = (java.net.JarURLConnection)u.openConnection();}
        catch (Exception | Error ex) {MsgExceptn.exception(ex.toString());}
        if(uc == null) {return null;}
        java.util.jar.Attributes attr = uc.getMainAttributes();
        return attr != null ? attr.getValue(java.util.jar.Attributes.Name.MAIN_CLASS) : null;
    }

    /** Invokes the application in this jar file given the name of the
     * main class and an array of arguments. The class must define a
     * static method "main" which takes an array of String arguments
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
        // java.lang.reflect.Method m = c.getMethod("main", new Class[] { args.getClass() });
        java.lang.reflect.Method m = c.getMethod("main", new Class<?>[] { String[].class }); // changed by I.Puigdomenech
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