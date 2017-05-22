-keepclasseswithmembers public class android.support.v7.widget.RecyclerView { *; }

# ----------------------------------------------------------------------------------------
# Keep our own classes
# ----------------------------------------------------------------------------------------
-keep class es.usc.citius.servando.calendula.** { *; }
-dontwarn es.usc.citius.servando.calendula.**


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
