# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# TarsosDSP specific rules
-keep class be.hogent.tarsos.dsp.** { *; }
-keepclassmembers class be.hogent.tarsos.dsp.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep AudioProcessor implementations
-keep interface be.hogent.tarsos.dsp.AudioProcessor
-keep class * implements be.hogent.tarsos.dsp.AudioProcessor {
    public <methods>;
}
