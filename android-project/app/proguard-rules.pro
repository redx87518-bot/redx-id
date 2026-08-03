# Keep all app classes
-keep class com.redx.idcard.** { *; }

# Keep Android graphics APIs used for image generation
-keep class android.graphics.** { *; }

# Keep FileProvider
-keep class androidx.core.content.FileProvider { *; }

# Suppress R8 warnings
-dontwarn kotlin.**
-dontwarn kotlinx.**
