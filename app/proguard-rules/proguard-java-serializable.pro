# Serializable support for the classes of the app (e.g. exceptions passed in an Intent)
-keepnames class com.arcao.geocaching4locus.** implements java.io.Serializable

-keepclassmembers class com.arcao.geocaching4locus.** implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
