-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable
-optimizationpasses 6
-verbose

#prevent severe obfuscation
-keep,allowshrinking,allowoptimization class * { <methods>; }

-keepclasseswithmembernames,allowshrinking,allowoptimization class * {
    native <methods>;
}

-keepclasseswithmembers,allowshrinking,allowoptimization class * {
	public <init>(android.content.Context);
    public <init>(android.content.Context,android.util.AttributeSet);
    public <init>(android.content.Context,android.util.AttributeSet,int);
}

-keepclassmembers,allowoptimization class * {
    public <init>(android.content.Context);
    public <init>(android.content.Context,android.util.AttributeSet);
    public <init>(android.content.Context,android.util.AttributeSet,int);
}

# For Crashlytics
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

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

# ButterKnife support
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# OkHTTP
-dontwarn okio.**

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