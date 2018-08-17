package spana;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/** Adapted from http://www.rgagnon.com/javadetails/java-0630.html<br>
 * <br>
 * The JDK contains the required code (java.util.prefs.WindowsPreferences) to access
 * the Windows registry. The trick is to use reflection to access private
 * methods defined in the WindowsPreference class.
 * Changes by Ignasi Puigdomenech: added error messages to exceptions thrown;
 * and removed access to HKEY_LOCAL_MACHINE and to HKEY_CLASSES_ROOT.
 * @author Real Gagnon
 */
public class WinRegistry {
  public static final int HKEY_CURRENT_USER = 0x80000001;
  public static final int REG_SUCCESS = 0;
  //public static final int REG_NOTFOUND = 2;
  //public static final int REG_ACCESSDENIED = 5;

  private static final int KEY_ALL_ACCESS = 0xf003f;
  private static final int KEY_READ = 0x20019;
  private static final java.util.prefs.Preferences userRoot = java.util.prefs.Preferences.userRoot();
  private static final Class<? extends java.util.prefs.Preferences> userClass = userRoot.getClass();
  private static Method regOpenKey = null;
  private static Method regCloseKey = null;
  private static Method regQueryValueEx = null;
  private static Method regEnumValue = null;
  private static Method regQueryInfoKey = null;
  private static Method regEnumKeyEx = null;
  private static Method regCreateKeyEx = null;
  private static Method regSetValueEx = null;
  private static Method regDeleteKey = null;
  private static Method regDeleteValue = null;

  static {
    try {
      regOpenKey = userClass.getDeclaredMethod("WindowsRegOpenKey",
          new Class[] { int.class, byte[].class, int.class });
      regOpenKey.setAccessible(true);
      regCloseKey = userClass.getDeclaredMethod("WindowsRegCloseKey",
          new Class[] { int.class });
      regCloseKey.setAccessible(true);
      regQueryValueEx = userClass.getDeclaredMethod("WindowsRegQueryValueEx",
          new Class[] { int.class, byte[].class });
      regQueryValueEx.setAccessible(true);
      regEnumValue = userClass.getDeclaredMethod("WindowsRegEnumValue",
          new Class[] { int.class, int.class, int.class });
      regEnumValue.setAccessible(true);
      regQueryInfoKey = userClass.getDeclaredMethod("WindowsRegQueryInfoKey1",
          new Class[] { int.class });
      regQueryInfoKey.setAccessible(true);
      regEnumKeyEx = userClass.getDeclaredMethod(
          "WindowsRegEnumKeyEx", new Class[] { int.class, int.class, int.class });
      regEnumKeyEx.setAccessible(true);
      regCreateKeyEx = userClass.getDeclaredMethod(
          "WindowsRegCreateKeyEx", new Class[] { int.class, byte[].class });
      regCreateKeyEx.setAccessible(true);
      regSetValueEx = userClass.getDeclaredMethod(
          "WindowsRegSetValueEx", new Class[] { int.class, byte[].class, byte[].class });
      regSetValueEx.setAccessible(true);
      regDeleteValue = userClass.getDeclaredMethod(
          "WindowsRegDeleteValue", new Class[] { int.class, byte[].class });
      regDeleteValue.setAccessible(true);
      regDeleteKey = userClass.getDeclaredMethod(
          "WindowsRegDeleteKey", new Class[] { int.class, byte[].class });
      regDeleteKey.setAccessible(true);
    } catch (NoSuchMethodException e) {
        System.out.println(e.getMessage()+" in class WinRegistry");
    } catch (SecurityException e) {
        System.out.println(e.getMessage()+" in class WinRegistry");
    }
  }

  private WinRegistry() {}

/*
  /** Create a key. Nothing happens if the key already exists.
   * @param key
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException */
  public static void createKey(String key)
    throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    int [] ret;
    ret = createKeyPrivate(key);
    regCloseKey.invoke(userRoot, new Object[] { new Integer(ret[0]) });
    if (ret[1] != REG_SUCCESS) {
      throw new IllegalArgumentException("Error in \"createKey\", rc=" + ret[1] + ", hkey=HKCU, key=" + key );
    }
  }

  /** Delete a given key, even if it has values.
   * Throws an IllegalArgumentException if either the key has subkeys,
   * or if the key does not exist.
   * @param key
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException */
  public static void deleteKey(String key)
    throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    int rc = deleteKeyPrivate(key);
    if (rc != REG_SUCCESS) {
      throw new IllegalArgumentException("Error in \"deleteKey\", rc=" + rc + ", hkey=HKCU, key=" + key );
    }
  }

  /** Delete a value from a given key/value name.
   * Throws an IllegalArgumentException if the key/value does not exist.
   * @param key
   * @param value
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException */
  public static void deleteValue(String key, String value)
    throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    int rc = deleteValuePrivate(key, value);
    if (rc != REG_SUCCESS) {
      throw new IllegalArgumentException("Error in \"deleteValue\", rc=" + rc + ", hkey=HKCU, key=" + key + ", value=" + value);
    }
  }

  /**  Read a value from a key and value name.
   * Returns "null" if there is no such key/valueName
   * @param key
   * @param value
   * @return the value
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException */
  public static String readString(String key, String value)
    throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    int[] handles = (int[]) regOpenKey.invoke(userRoot, new Object[] {
        new Integer(HKEY_CURRENT_USER), toCstr(key), new Integer(KEY_READ) });
    if (handles[1] != REG_SUCCESS) {
      return null;
    }
    byte[] valb = (byte[]) regQueryValueEx.invoke(userRoot, new Object[] {
        new Integer(handles[0]), toCstr(value) });
    regCloseKey.invoke(userRoot, new Object[] { new Integer(handles[0]) });
    return (valb != null ? new String(valb).trim() : null);
  }

  /** Read the subkey name(s) from a given key.
   * It returns an empty List if there are no subkeys.
   * @param key
   * @return the value name(s)
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException */
  public static List<String> readStringSubKeys(String key)
    throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    List<String> results = new ArrayList<String>();
    int[] handles = (int[]) regOpenKey.invoke(userRoot, new Object[] {
        new Integer(HKEY_CURRENT_USER), toCstr(key), new Integer(KEY_READ)
        });
    if (handles[1] != REG_SUCCESS) {
      return null;
    }
    int[] info = (int[]) regQueryInfoKey.invoke(userRoot,
        new Object[] { new Integer(handles[0]) });

    // int count = info[2]; // count
    int count = info[0];    // bug fix 20130112
    int maxlen = info[3]; // value length max
    for(int index=0; index<count; index++)  {
      byte[] name = (byte[]) regEnumKeyEx.invoke(userRoot, new Object[] {
          new Integer
            (handles[0]), new Integer(index), new Integer(maxlen + 1)
          });
      if(name != null) { // Ignasi
        results.add(new String(name).trim());
      }
    }
    regCloseKey.invoke(userRoot, new Object[] { new Integer(handles[0]) });
    return results;
  }

  /** Write a value in a given key/value name.
   * If the valueName does not exist, it is created.
   * Otherwise its contents is replaced with the new value.
   * @param key
   * @param valueName
   * @param value
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException */
  public static void writeStringValue(String key, String valueName, String value)
    throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    int[] handles = (int[]) regOpenKey.invoke(userRoot, new Object[] {
        new Integer(HKEY_CURRENT_USER), toCstr(key), new Integer(KEY_ALL_ACCESS) });

    regSetValueEx.invoke(userRoot,
        new Object[] {
          new Integer(handles[0]), toCstr(valueName), toCstr(value)
          });
    regCloseKey.invoke(userRoot, new Object[] { new Integer(handles[0]) });
  }

  /** Read value(s) and value name(s) from given key.
   * It returns an empty Map if there are no values.
   * Note by Ignasi: it seems not to work properly if the key has no subKeys.
   * @param key
   * @return the value name(s) plus the value(s)
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException */
  public static java.util.Map<String,String> readStringValues(String key)
    throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    java.util.HashMap<String, String> results = new java.util.HashMap<String,String>();
    int[] handles = (int[]) regOpenKey.invoke(userRoot, new Object[] {
        new Integer(HKEY_CURRENT_USER), toCstr(key), new Integer(KEY_READ) });
    if (handles[1] != REG_SUCCESS) {
      return null;
    }
    int[] info = (int[]) regQueryInfoKey.invoke(userRoot,
        new Object[] { new Integer(handles[0]) });

    // int count = info[2]; // count
    int count = info[0];    // bug fix 20130112
    int maxlen = info[3]; // value length max
    for(int index=0; index<count; index++)  {
      byte[] name = (byte[]) regEnumValue.invoke(userRoot, new Object[] {
          new Integer
            (handles[0]), new Integer(index), new Integer(maxlen + 1)});
      if(name != null) { // Ignasi
        String value = readString(key, new String(name));
        results.put(new String(name).trim(), value);
      }
    }
    regCloseKey.invoke(userRoot, new Object[] { new Integer(handles[0]) });
    return results;
  }

  // =====================
  //<editor-fold defaultstate="collapsed" desc="private methods">

  private static int deleteValuePrivate(String key, String value)
    throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    int[] handles = (int[]) regOpenKey.invoke(userRoot, new Object[] {
        new Integer(HKEY_CURRENT_USER), toCstr(key), new Integer(KEY_ALL_ACCESS) });
    if (handles[1] != REG_SUCCESS) {
      return handles[1];  // can be REG_NOTFOUND, REG_ACCESSDENIED
    }
    int rc =((Integer) regDeleteValue.invoke(userRoot,
        new Object[] {
          new Integer(handles[0]), toCstr(value)
          })).intValue();
    regCloseKey.invoke(userRoot, new Object[] { new Integer(handles[0]) });
    return rc;
  }

  private static int deleteKeyPrivate(String key)
    throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    int rc =((Integer) regDeleteKey.invoke(userRoot,
        new Object[] { new Integer(HKEY_CURRENT_USER), toCstr(key) })).intValue();
    return rc;  // can be REG_NOTFOUND, REG_ACCESSDENIED, REG_SUCCESS
  }

  private static int [] createKeyPrivate(String key)
    throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    return  (int[]) regCreateKeyEx.invoke(userRoot,
        new Object[] { new Integer(HKEY_CURRENT_USER), toCstr(key) });
  }

  // utility
  private static byte[] toCstr(String str) {
    byte[] result = new byte[str.length() + 1];

    for (int i = 0; i < str.length(); i++) {
      result[i] = (byte) str.charAt(i);
    }
    result[str.length()] = 0;
    return result;
  }

  //</editor-fold>
  
}
