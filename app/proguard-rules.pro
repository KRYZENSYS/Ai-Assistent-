# Keep attributes for reflection and stack traces
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod, SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile

# Retrofit Rules
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# Moshi Rules
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}

# Keep our remote models so Moshi JSON parsing won't be broken by R8 code obfuscation
-keep class com.example.data.remote.** { *; }

# OkHttp Rules
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Room Database Rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

