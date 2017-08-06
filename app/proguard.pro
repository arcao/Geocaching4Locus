-optimizations !field/*,!class/merging/*,!code/allocation/variable
-optimizationpasses 6

#prevent severe obfuscation
-keep,allowshrinking class * { <methods>; }

# For Feedback info from Locus Map
-keepnames class locus.api.android.utils.LocusInfo {
    *;
}

# serializable support
-keepnames class * implements java.io.Serializable

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keep class com.arcao.geocaching4locus.fragment.**

# OkHTTP
-dontwarn okio.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault

# Scribe
-dontwarn com.github.scribejava.core.services.DatatypeConverterEncoder
-dontwarn org.apache.commons.codec.binary.**
-dontwarn com.ning.http.client.**

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable #needed
-keepattributes Signature # Needed by google-api-client #to make XStream work with obfuscation?
-keepattributes EnclosingMethod #required?
-keepattributes InnerClasses #required?

-keepattributes Exceptions # can be removed?
-keepattributes Deprecated # can be removed?
-keepattributes Synthetic # can be removed?

-keepattributes *Annotation*

# Bugfix on some devices with java.lang.NoSuchMethodError
-keepnames class org.apache.commons.lang3.** { *; }

# Suppress duplicate warning for system classes;  Blaze is passing android.jar
# to proguard multiple times.
-dontnote android.**
-dontnote java.**
-dontnote javax.**
-dontnote junit.**
-dontnote org.**
-dontnote dalvik.**
-dontnote com.android.internal.**

# Suppress other notes
-dontnote com.android.**
-dontnote com.google.android.gms.**
-dontnote com.google.gson.internal.**
-dontnote com.afollestad.materialdialogs.internal.MDTintHelper
-dontnote com.arcao.feedback.collector.DisplayManagerCollector