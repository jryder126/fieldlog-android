# Keep the JS bridge methods callable from WebView
-keepclassmembers class com.fieldlog.app.MainActivity$Bridge {
    @android.webkit.JavascriptInterface <methods>;
}
