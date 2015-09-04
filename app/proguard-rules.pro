-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# It's being sought but doesn't need to be.
-dontwarn org.joda.convert.**

# It's included as a jar file so don't worry, right?
-dontwarn com.example.exceptionhandler.**

# Necessary since adding compile files('../../../libs/opencsv/opencsv-3.3.jar')
-dontwarn org.apache.commons.collections.BeanMap
-dontwarn java.beans.**