# R8 usage for jtx Board:
#    shrinking        yes (only in release builds)
#    optimization     yes (on by R8 defaults)
#    obfuscation      no (open-source)

-dontobfuscate
-printusage build/reports/r8-usage.txt

# jtx Board
-keep class at.techbee.jtx.** { *; }       # all jtx Board code is required


# ical4j: keep all iCalendar properties/parameters (used via reflection)
-keep class net.fortuna.ical4j.** { *; }

# we use enum classes (https://www.guardsquare.com/en/products/proguard/manual/examples#enumerations)
-keepclassmembers,allowoptimization enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#Amazon
-dontwarn com.amazon.**
-keep class com.amazon.** {*;}
-keepattributes *Annotation*

#Huawei
-dontwarn com.huawei.**
#-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class com.huawei.hianalytics.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}


# Additional rules which are now required since missing classes can't be ignored in R8 anymore.
# [https://developer.android.com/build/releases/past-releases/agp-7-0-0-release-notes#r8-missing-class-warning]
-dontwarn groovy.**
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.codehaus.groovy.**
-dontwarn org.joda.**
-dontwarn org.json.*
-dontwarn org.jsoup.**
-dontwarn org.xmlpull.**


# Great resource with explanations:
# https://medium.com/androiddevelopers/troubleshooting-proguard-issues-on-android-bce9de4f8a74

# Kept for future reference

# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}



# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
