# android-mjpeg-view
Android View for displaying MJPEG stream.

- This view only requires a specific http url.
- Supported image scaling methods are fit width, fit height, original size, stretch and best fit.
- Only boundary is used to separate each jpeg image (each frame) from a stream. Content-length is ignored.
- A boundary must be specified in an HTTP headr (Content-type). Otherwise a default boundary pattern will be used.

Basic usage<br/>
1. Include a library in to your project by adding this to <b>app level</b> build.gradle file.
```gradle
dependencies {
    // other dependencies
    
    implementation 'com.github.perthcpe23:mjpegviewer:1.0.7'
}
```
2. Add a view to XML layout:
````xml
<com.longdo.mjpegviewer.MjpegView
    android:id="@+id/mjpegview"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
````

3. Specify mjpeg source and start streaming
````java
MjpegView viewer = (MjpegView) findViewById(R.id.mjpegview);
viewer.setMode(MjpegView.MODE_FIT_WIDTH);
viewer.setAdjustHeight(true);
viewer.setUrl("http://bma-itic1.iticfoundation.org/mjpeg2.php?camid=test");
viewer.startStream();

//when user leaves application
viewer.stopStream();
````
* You can also download .aar at https://github.com/perthcpe23/android-mjpeg-view/tree/master/arr

# Contact
perth.s28@gmail.com
