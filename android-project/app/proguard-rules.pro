# Keep the app's own classes
-keep class com.redx.idcard.** { *; }

# Keep Bitmap and Canvas APIs used for image generation
-keep class android.graphics.** { *; }

# Keep FileProvider
-keep class androidx.core.content.FileProvider { *; }

# Suppress warnings for unused classes stripped by R8
-dontwarn kotlin.**
-dontwarn kotlinx.**
