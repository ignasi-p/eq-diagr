package lib.huvud;
/** Sort Properties when saving.
 * The SortedProperties class extends the regular Properties class.
 * It overrides the keys() method to return the sorted keys instead. 
 * From Real Gagnon (http://www.rgagnon.com/javadetails/java-0614.html) */
public class SortedProperties extends java.util.Properties {
    @Override
    @SuppressWarnings("unchecked")
    public synchronized java.util.Enumeration<java.lang.Object> keys() {
        java.util.Enumeration keysEnum = super.keys();
        //java.util.Vector keyList = new java.util.Vector();
        java.util.ArrayList keyList2 = new java.util.ArrayList();
        while(keysEnum.hasMoreElements()){
            keyList2.add(keysEnum.nextElement());
            }
        java.util.Collections.sort(keyList2);
        //return keyList.elements();
        return java.util.Collections.enumeration(keyList2);
    } // keys()    
}
