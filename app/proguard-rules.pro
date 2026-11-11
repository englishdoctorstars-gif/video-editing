# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class com.englishdoctor.vocabsolution.** {
    *** Companion;
}
-keepclasseswithmembers class com.englishdoctor.vocabsolution.** {
    kotlinx.serialization.KSerializer serializer(...);
}
