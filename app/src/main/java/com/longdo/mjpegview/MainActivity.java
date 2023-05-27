package com.longdo.mjpegview;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.longdo.mjpegviewer.MjpegView;
import com.longdo.mjpegviewer.MjpegViewError;
import com.longdo.mjpegviewer.MjpegViewStateChangeListener;


public class MainActivity extends AppCompatActivity {

    private MjpegView view1;
    private MjpegView view2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view1 = findViewById(R.id.mjpegview1);
        view1.setAdjustHeight(true);
        //view.setAdjustWidth(true);
        view1.setMode(MjpegView.MODE_FIT_WIDTH);
        //view.setMsecWaitAfterReadImageError(1000);
        view1.setUrl("https://bma-itic1.iticfoundation.org/mjpeg2.php?camid=test");
        view1.setRecycleBitmap(true);
        view1.setStateChangeListener(new MjpegViewStateChangeListener() {

            @Override
            public void onStreamDownloadStart() {
                Log.d("StateChangeListener", "onStreamDownloadStart");
            }

            @Override
            public void onStreamDownloadStop() {
                Log.d("StateChangeListener", "onStreamDownloadStop");
            }

            @Override
            public void onServerConnected() {
                Log.d("StateChangeListener", "onServerConnected");
            }

            @Override
            public void onMeasurementChanged(Rect rect) {
                Log.d("StateChangeListener", "onMeasurementChanged");
            }

            @Override
            public void onNewFrame(Bitmap image) {
            }

            @Override
            public void onError(MjpegViewError error) {
                Log.d("StateChangeListener", String.valueOf(error.errorCode));
            }
        });

        view2 = findViewById(R.id.mjpegview2);
        view2.setAdjustHeight(true);
        view2.setSupportPinchZoomAndPan(true);
        //view.setAdjustWidth(true);
        view2.setMode(MjpegView.MODE_FIT_WIDTH);
        //view.setMsecWaitAfterReadImageError(1000);
        view2.setUrl("https://bma-itic1.iticfoundation.org/mjpeg2.php?camid=test");
    }

    @Override
    protected void onResume() {
        view1.startStream();
        view2.startStream();
        super.onResume();
    }

    @Override
    protected void onPause() {
        view1.stopStream();
        view2.stopStream();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        view1.stopStream();
        view2.stopStream();
        super.onStop();
    }
}
