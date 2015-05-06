# android-mjpeg-view
Android custom View to display MJPEG by giving a specific http url.
Image scaling method can be specific--fit width, fit height, original size, stratch and best fit.
This custom view use only boundary to separate jpeg images (frame) from stream. Content-length is ignored.

You can download .arr at https://github.com/perthcpe23/android-mjpeg-view/tree/master/arr

Basic usage
XML layout source code:
<com.longdo.mjpegviewer.MjpegView
    android:id="@+id/mjpegview"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />

Java source code:
MjpegView viewer = (MjpegView) findViewById(R.id.mjpegview);
viewer.setMode(MjpegView.MODE_FIT_WIDTH);
viewer.setAdjustHeight(true);
viewer.setUrl("http://www.example.com/mjpeg.php?id=1234");
viewer.startStream();

//when user leaves application
viewer.stopStream();
		 
This project is developed using Android Studio. Feel free to clone and import to yours.