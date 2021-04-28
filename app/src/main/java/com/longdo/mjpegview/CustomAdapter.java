package com.longdo.mjpegview;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.longdo.mjpegviewer.MjpegView;

import java.util.List;

public class CustomAdapter extends ArrayAdapter {

    public CustomAdapter(@NonNull Activity context, int resource, @NonNull List objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_layout, parent, false);
        }

        MjpegView mjpegview = (MjpegView) convertView.findViewById(R.id.mjpegview);
        mjpegview.setMode(MjpegView.MODE_FIT_WIDTH);
        mjpegview.setAdjustHeight(true);
        mjpegview.setSupportPinchZoomAndPan(true);
        mjpegview.setUrl("https://bma-itic1.iticfoundation.org/mjpeg2.php?camid=test");
        mjpegview.startStream();

        return convertView;
    }
}
