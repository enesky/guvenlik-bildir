# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepattributes Signature
-keepattributes *Annotation*

-dontwarn okhttp3.internal.platform.ConscryptPlatform

-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# The remainder of this file is identical to the non-optimized version
# of the Proguard configuration file (except that the other file has
# flags to turn off optimization).
-dontusemixedcaseclassnames
-verbose

# Disable logging
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class ** {
    native <methods>;
}

# Keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends androidx.appcompat.app.AppCompatActivity {
   public void *(android.view.View);
}

-keepclassmembers class * extends androidx.fragment.app.Fragment {
   public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum ** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep public enum com.enesky.guvenlikbildir.** {
    **[] $VALUES;
    public *;
}
-keepclassmembers enum com.enesky.guvenlikbildir.** { *; }
-keepclassmembers enum ** { *; }

# Preventing null reference crashes
-keep class com.enesky.guvenlikbildir.model.** { *; }

# Keep Data Class
-keepclasseswithmembers class com.enesky.guvenlikbildir.** {
    public ** component1();
    <fields>;
}

# Necessary packages
-keep class com.google.firebase.**
-keep class com.squareup.timessquare.**
-keep class com.prolificinteractive.materialcalendarview.**
-keep class ru.rambler.libs.swipe_layout.**
-keep class retrofit2.**

#--------------------------------------- GMS CONFIGURATION ----------------------------------------
-keep class com.google.android.gms.**

# Dont warn unknown classes
-dontnote android.os.SystemProperties
-dontnote com.google.android.gms.gcm.GcmListenerService
-dontnote com.google.android.gms.dynamite.DynamiteModule

# Fix maps 3.0.0-beta crash:
-keep,allowoptimization class com.google.android.libraries.maps.** { *; }

# Fix maps 3.0.0-beta marker taps ignored:
-keep,allowoptimization class com.google.android.apps.gmm.renderer.** { *; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
#--------------------------------------------------------------------------------------------------

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**

-dontwarn javax.annotation.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2

# Android and Apache HTTP classes overlaps, ignore
-dontnote android.net.http.**
-dontnote org.apache.http.**

# Unknown dynamic referenced classes, ignore
-dontnote okhttp3.internal.**
-dontnote retrofit2.**
-dontnote com.scottyab.rootbeer.util.Utils

-keep class org.ocpsoft.prettytime.i18n.**

