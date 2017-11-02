-optimizations !field/*,!class/merging/*,!code/allocation/variable
-optimizationpasses 6

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

# Keep setting fragments
-keep class com.arcao.geocaching4locus.settings.fragment.** { *; }

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
