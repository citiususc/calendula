-dontwarn com.mikepenz.materialize.holder.**
-dontwarn com.mikepenz.aboutlibraries.**

# ----------------------------------------------------------------------------------------
# Keep our own classes
# ----------------------------------------------------------------------------------------
-keep class es.usc.citius.servando.calendula.** { *; }
-dontwarn es.usc.citius.servando.calendula.**

# ----------------------------------------------------------------------------------------
# Joda Time
# https://github.com/krschultz/android-proguard-snippets/blob/master/libraries/proguard-joda-time.pro
# ----------------------------------------------------------------------------------------
-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }

# ----------------------------------------------------------------------------------------
# Square Picasso
# https://github.com/square/picasso
# ----------------------------------------------------------------------------------------
-dontwarn com.squareup.okhttp.**

# ----------------------------------------------------------------------------------------
# OrmLite
# ----------------------------------------------------------------------------------------
-keepattributes *DatabaseField*
-keepattributes *DatabaseTable*
-keepattributes *SerializedName*
-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep enum com.j256.**
-keepclassmembers enum com.j256.** { *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }
-dontwarn com.j256.ormlite.android.**
-dontwarn com.j256.ormlite.logger.**
-dontwarn com.j256.ormlite.misc.**


# ----------------------------------------------------------------------------------------
# Rules applied to test code
# ----------------------------------------------------------------------------------------
-ignorewarnings
-keepattributes *Annotation*
-dontnote junit.framework.**
-dontnote junit.runner.**
-dontwarn android.test.**
-dontwarn android.support.test.**
-dontwarn org.junit.**
-dontwarn org.hamcrest.**
-dontwarn com.squareup.javawriter.JavaWriter