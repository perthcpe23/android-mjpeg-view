package com.longdo.mjpegviewer;

import android.graphics.Bitmap;
import android.graphics.Rect;

public interface MjpegViewStateChangeListener {
    void onStreamDownloadStart();

    void onStreamDownloadStop();

    void onServerConnected();

    void onMeasurementChanged(Rect rect);

    void onNewFrame(Bitmap image);

    void onError(MjpegViewError error);
}
