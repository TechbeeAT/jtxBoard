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

# DAVx⁵ + libs
#-keep class at.bitfire.** { *; }       # all DAVx⁵ code is required

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
-dontwarn com.android.org.conscrypt.SSLParametersImpl
-dontwarn groovy.**
-dontwarn java.beans.Transient
-dontwarn junit.textui.TestRunner
-dontwarn org.apache.harmony.xnet.provider.jsse.SSLParametersImpl
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.codehaus.groovy.**
-dontwarn org.joda.**
-dontwarn org.json.*
-dontwarn org.jsoup.**
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
-dontwarn org.xmlpull.**
-dontwarn sun.net.spi.nameservice.NameService
-dontwarn sun.net.spi.nameservice.NameServiceDescriptor
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier

-dontwarn com.github.erosb.jsonsKema.IJsonValue
-dontwarn com.github.erosb.jsonsKema.JsonParser
-dontwarn com.github.erosb.jsonsKema.JsonValue
-dontwarn com.github.erosb.jsonsKema.Schema
-dontwarn com.github.erosb.jsonsKema.SchemaLoader
-dontwarn com.github.erosb.jsonsKema.ValidationFailure
-dontwarn com.github.erosb.jsonsKema.Validator
-dontwarn javax.cache.Cache
-dontwarn javax.cache.CacheManager
-dontwarn javax.cache.Caching
-dontwarn javax.cache.configuration.Configuration
-dontwarn javax.cache.configuration.MutableConfiguration
-dontwarn javax.cache.spi.CachingProvider
-dontwarn org.jparsec.OperatorTable
-dontwarn org.jparsec.Parser$Reference
-dontwarn org.jparsec.Parser
-dontwarn org.jparsec.Parsers
-dontwarn org.jparsec.Scanners
-dontwarn org.jparsec.Terminals$Builder
-dontwarn org.jparsec.Terminals$Identifier
-dontwarn org.jparsec.Terminals$IntegerLiteral
-dontwarn org.jparsec.Terminals$StringLiteral
-dontwarn org.jparsec.Terminals
-dontwarn org.jparsec.functors.Map3


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
