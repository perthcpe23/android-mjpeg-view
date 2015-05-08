# android-mjpeg-view
Android custom View for displaying MJPEG.<br/>
This view only required a specific http url.<br/>
Image scaling method can be specificed--fit width, fit height, original size, stretch and best fit.<br/>
Only boundary is used to separate each jpeg image (frame) from stream. Content-length is ignored.<br/>

You can download .arr at https://github.com/perthcpe23/android-mjpeg-view/tree/master/arr

Basic usage<br/>
XML layout source code:
````xml
<com.longdo.mjpegviewer.MjpegView
    android:id="@+id/mjpegview"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
````

Java source code:
````java
MjpegView viewer = (MjpegView) findViewById(R.id.mjpegview);
viewer.setMode(MjpegView.MODE_FIT_WIDTH);
viewer.setAdjustHeight(true);
viewer.setUrl("http://www.example.com/mjpeg.php?id=1234");
viewer.startStream();

//when user leaves application
viewer.stopStream();
````
		 
This project is developed using Android Studio. Feel free to clone and import to yours.
