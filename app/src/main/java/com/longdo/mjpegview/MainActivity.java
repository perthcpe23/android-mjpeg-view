package com.longdo.mjpegview;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.longdo.mjpegviewer.MjpegView;


public class MainActivity extends ActionBarActivity {

    private MjpegView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view = (MjpegView) findViewById(R.id.mjpegview);
        view.setAdjustHeight(true);
        //view.setAdjustWidth(true);
        view.setMode(MjpegView.MODE_FIT_WIDTH);
        view.setUrl("http://bma-itic1.iticfoundation.org/mjpeg2.php?camid=10.107.129.126");
        //view.seturl("http://trackfield.webcam.oregonstate.edu/axis-cgi/mjpg/video.cgi?resolution=800x600&amp%3bdummy=1333689998337");
    }

    @Override
    protected void onResume() {
        view.startStream();
        super.onResume();
    }

    @Override
    protected void onPause() {
        view.stopStream();
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
        view.stopStream();
        super.onStop();
    }
}
