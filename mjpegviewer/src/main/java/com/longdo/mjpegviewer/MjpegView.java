package com.longdo.mjpegviewer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Metamedia Technology
 * Created by perth on 6/5/2558.
 */
public class MjpegView extends View{
    public static final int MODE_ORIGINAL = 0;
    public static final int MODE_FIT_WIDTH = 1;
    public static final int MODE_FIT_HEIGHT = 2;
    public static final int MODE_BEST_FIT = 3;
    public static final int MODE_STRETCH = 4;

    private static final int WAIT_AFTER_READ_IMAGE_ERROR_MSEC = 5000;
    private static final int CHUNK_SIZE = 4096;
    private static final String DEFAULT_BOUNDARY_REGEX = "[_a-zA-Z0-9]*boundary";

    private final String tag = getClass().getSimpleName();
    private final Context context;
    private final Object lockBitmap = new Object();

    private String url;
    private Bitmap lastBitmap;
    private MjpegDownloader downloader;
    private Paint paint;
    private Rect dst;

    private int mode = MODE_ORIGINAL;
    private int drawX,drawY, vWidth = -1, vHeight = -1;
    private int lastImgWidth, lastImgHeight;
    private boolean adjustWidth, adjustHeight;
    private int msecWaitAfterReadImageError = WAIT_AFTER_READ_IMAGE_ERROR_MSEC;
    private boolean isRecycleBitmap;
    private boolean isUserForceConfigRecycle;
    private boolean isSupportPinchZoomAndPan;

    public MjpegView(Context context){
        super(context);
        this.context = context;
        init();
    }

    public MjpegView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init(){
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dst = new Rect(0,0,0,0);
    }

    public void setUrl(String url){
        this.url = url;
    }

    public void startStream(){
        if(downloader != null && downloader.isRunning()){
            Log.w(tag,"Already started, stop by calling stopStream() first.");
            return;
        }

        downloader = new MjpegDownloader();
        downloader.start();
    }

    public void stopStream(){
        downloader.cancel();
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
        lastImgWidth = -1; // force re-calculate view size
        requestLayout();
    }

    public void setBitmap(Bitmap bm){
        synchronized (lockBitmap) {
            if (lastBitmap != null && isUserForceConfigRecycle && isRecycleBitmap) {
                lastBitmap.recycle();
            }

            lastBitmap = bm;
        }

        if(context instanceof  Activity) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    invalidate();
                    requestLayout();
                }
            });
        }
        else{
            Log.e(tag,"Can not request Canvas's redraw. Context is not an instance of Activity");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        boolean shouldRecalculateSize;
        synchronized (lockBitmap) {
            shouldRecalculateSize = lastBitmap != null && (lastImgWidth != lastBitmap.getWidth() || lastImgHeight != lastBitmap.getHeight());
            if(shouldRecalculateSize) {
                lastImgWidth = lastBitmap.getWidth();
                lastImgHeight = lastBitmap.getHeight();
            }
        }

        if (shouldRecalculateSize) {
            vWidth = MeasureSpec.getSize(widthMeasureSpec);
            vHeight = MeasureSpec.getSize(heightMeasureSpec);

            if(mode == MODE_ORIGINAL){
                drawX = (vWidth - lastImgWidth)/2;
                drawY = (vHeight - lastImgHeight)/2;

                if(adjustWidth){
                    vWidth = lastImgWidth;
                    drawX = 0;
                }

                if(adjustHeight){
                    vHeight = lastImgHeight;
                    drawY = 0;
                }
            }
            else if(mode == MODE_FIT_WIDTH){
                int newHeight = (int)(((float)lastImgHeight/(float)lastImgWidth)*vWidth);

                drawX = 0;

                if(adjustHeight){
                    vHeight = newHeight;
                    drawY = 0;
                }
                else{
                    drawY = (vHeight - newHeight)/2;
                }

                //no need to check adjustWidth because in this mode image's width is always equals view's width.

                dst.set(drawX,drawY,vWidth,drawY+newHeight);
            }
            else if(mode == MODE_FIT_HEIGHT){
                int newWidth = (int)(((float)lastImgWidth/(float)lastImgHeight)*vHeight);

                drawY = 0;

                if(adjustWidth){
                    vWidth = newWidth;
                    drawX = 0;
                }
                else{
                    drawX = (vWidth - newWidth)/2;
                }

                //no need to check adjustHeight because in this mode image's height is always equals view's height.

                dst.set(drawX,drawY,drawX+newWidth,vHeight);
            }
            else if(mode == MODE_BEST_FIT){
                if((float)lastImgWidth/(float)vWidth > (float)lastImgHeight/(float)vHeight){
                    //duplicated code
                    //fit width
                    int newHeight = (int)(((float)lastImgHeight/(float)lastImgWidth)*vWidth);

                    drawX = 0;

                    if(adjustHeight){
                        vHeight = newHeight;
                        drawY = 0;
                    }
                    else{
                        drawY = (vHeight - newHeight)/2;
                    }

                    //no need to check adjustWidth because in this mode image's width is always equals view's width.

                    dst.set(drawX,drawY,vWidth,drawY+newHeight);
                }
                else{
                    //duplicated code
                    //fit height
                    int newWidth = (int)(((float)lastImgWidth/(float)lastImgHeight)*vHeight);

                    drawY = 0;

                    if(adjustWidth){
                        vWidth = newWidth;
                        drawX = 0;
                    }
                    else{
                        drawX = (vWidth - newWidth)/2;
                    }

                    //no need to check adjustHeight because in this mode image's height is always equals view's height.

                    dst.set(drawX,drawY,drawX+newWidth,vHeight);
                }
            }
            else if(mode == MODE_STRETCH){
                dst.set(0,0,vWidth,vHeight);
                //no need to check neither adjustHeight nor adjustHeight because in this mode image's size is always equals view's size.
            }
        }
        else {
            if(vWidth == -1 || vHeight == -1){
                vWidth = MeasureSpec.getSize(widthMeasureSpec);
                vHeight = MeasureSpec.getSize(heightMeasureSpec);
            }
        }

        setMeasuredDimension(vWidth, vHeight);
    }

    @Override
    protected void onDraw(Canvas c) {
        synchronized (lockBitmap) {
            if (c != null && lastBitmap != null && !lastBitmap.isRecycled()) {
                if (mode != MODE_ORIGINAL) {
                    c.drawBitmap(lastBitmap, null, dst, paint);
                } else {
                    c.drawBitmap(lastBitmap, drawX, drawY, paint);
                }
            } else {
                Log.d(tag, "Skip drawing, canvas is null or bitmap is not ready yet");
            }
        }
    }

    public boolean isAdjustWidth() {
        return adjustWidth;
    }

    public void setAdjustWidth(boolean adjustWidth) {
        this.adjustWidth = adjustWidth;
    }

    public boolean isAdjustHeight() {
        return adjustHeight;
    }

    public void setAdjustHeight(boolean adjustHeight) {
        this.adjustHeight = adjustHeight;
    }

    public int getMsecWaitAfterReadImageError() {
        return msecWaitAfterReadImageError;
    }

    public void setMsecWaitAfterReadImageError(int msecWaitAfterReadImageError) {
        this.msecWaitAfterReadImageError = msecWaitAfterReadImageError;
    }

    public boolean isRecycleBitmap() {
        return isRecycleBitmap;
    }

    public void setRecycleBitmap(boolean recycleBitmap) {
        isUserForceConfigRecycle = true;
        isRecycleBitmap = recycleBitmap;
    }

    public boolean getSupportPinchZoomAndPan() {
        return isSupportPinchZoomAndPan;
    }

    public void setSupportPinchZoomAndPan(boolean supportPinchZoomAndPan) {
        isSupportPinchZoomAndPan = supportPinchZoomAndPan;
    }

    class MjpegDownloader extends Thread{
        private boolean run = true;
        private long lastFrameTimestamp = 0;

        byte[] currentImageBody = new byte[(int) 1e6];
        int currentImageBodyLength = 0;

        public void cancel(){
            run = false;
        }

        public boolean isRunning(){
            return run;
        }

        @Override
        public void run() {
            while(run) {
                HttpURLConnection connection = null;
                BufferedInputStream bis = null;
                URL serverUrl;

                try {
                    serverUrl = new URL(url);
                    connection = (HttpURLConnection) serverUrl.openConnection();
                    connection.setDoInput(true);
                    connection.connect();

                    String headerBoundary = DEFAULT_BOUNDARY_REGEX;

                    try{
                        // Try to extract a boundary from HTTP header first.
                        // If the information is not presented, throw an exception and use default value instead.
                        String contentType = connection.getHeaderField("Content-Type");
                        if (contentType == null) {
                            throw new Exception("Unable to get content type");
                        }

                        String[] types = contentType.split(";");
                        if (types.length == 0) {
                            throw new Exception("Content type was empty");
                        }

                        String extractedBoundary = null;
                        for (String ct : types) {
                            String trimmedCt = ct.trim();
                            if (trimmedCt.startsWith("boundary=")) {
                                extractedBoundary = trimmedCt.substring(9); // Content after 'boundary='
                            }
                        }

                        if (extractedBoundary == null) {
                            throw new Exception("Unable to find mjpeg boundary");
                        }

                        headerBoundary = extractedBoundary;
                    }
                    catch(Exception e){
                        Log.w(tag,"Cannot extract a boundary string from HTTP header with message: " + e.getMessage() + ". Use a default value instead.");
                    }

                    //determine boundary pattern
                    //use the whole header as separator in case boundary locate in difference chunks
                    Pattern pattern = Pattern.compile("--" + headerBoundary + "\\s+(.*)\\r\\n\\r\\n",Pattern.DOTALL);
                    Matcher matcher;

                    bis = new BufferedInputStream(connection.getInputStream());
                    byte[] read = new byte[CHUNK_SIZE], tmpCheckBoundary;
                    int readByte, boundaryIndex;
                    String checkHeaderStr, boundary;

                    long totalFindPatternMicroSec = 0;
                    long totalAddByteMicroSec = 0;
                    long totalReadMicroSec = 0;

                    //always keep reading images from server
                    while (run) {
                        try {
                            long start = System.nanoTime();
                            readByte = bis.read(read);
                            totalReadMicroSec += (System.nanoTime() - start)/1000;

                            //no more data
                            if (readByte == -1) {
                                break;
                            }

                            addByte(read, 0, readByte, false);
                            start = System.nanoTime();
                            checkHeaderStr = new String(currentImageBody, 0, currentImageBodyLength, "ASCII");
                            totalAddByteMicroSec += (System.nanoTime() - start)/1000;

                            start = System.nanoTime();
                            matcher = pattern.matcher(checkHeaderStr);
                            boolean isFound = matcher.find();
                            totalFindPatternMicroSec += (System.nanoTime() - start)/1000;

                            if (isFound) {
                                // delete and re-add because if body contains boundary, it means body is over one image already
                                // we want exact one image content
                                delByte(readByte);

                                Log.d("performance", String.format("matcher until new frame %dms", totalFindPatternMicroSec));
                                Log.d("performance", String.format("new String until new frame %dms", totalAddByteMicroSec ));
                                Log.d("performance", String.format("read until new frame %dms", totalReadMicroSec ));

                                //boundary is found
                                boundary = matcher.group(0);

                                boundaryIndex = checkHeaderStr.indexOf(boundary);
                                boundaryIndex -= currentImageBodyLength;

                                if (boundaryIndex > 0) {
                                    addByte(read, 0, boundaryIndex, false);
                                } else {
                                    delByte(boundaryIndex);
                                }

                                start = System.nanoTime();
                                Bitmap outputImg = BitmapFactory.decodeByteArray(currentImageBody, 0, currentImageBodyLength);
                                long decodeByteArrayMicroSec = (System.nanoTime() - start)/1000;
                                Log.d("performance", String.format("decodeByteArray %dms", decodeByteArrayMicroSec));
                                Log.d("performance", String.format("total until new frame %dms", decodeByteArrayMicroSec + totalFindPatternMicroSec + totalAddByteMicroSec + totalReadMicroSec ));
                                totalFindPatternMicroSec = 0;
                                totalAddByteMicroSec = 0;
                                totalReadMicroSec = 0;

                                if (outputImg != null) {
                                    if(run) {
                                        newFrame(outputImg);
                                    }
                                } else {
                                    Log.e(tag, "Read image error");
                                }

                                int headerIndex = boundaryIndex + boundary.length();

                                addByte(read, headerIndex, readByte - headerIndex, true);
                            } else {
//                                addByte(read, 0, readByte, false);
                            }
                        } catch (Exception e) {
                            if(e.getMessage() != null) {
                                Log.e(tag, e.getMessage());
                            }
                            break;
                        }
                    }

                } catch (Exception e) {
                    if(e.getMessage() != null) {
                        Log.e(tag, e.getMessage());
                    }
                }

                try {
                    bis.close();
                    connection.disconnect();
                    Log.i(tag,"disconnected with " + url);
                } catch (Exception e) {
                    if(e.getMessage() != null) {
                        Log.e(tag, e.getMessage());
                    }
                }

                if(msecWaitAfterReadImageError > 0) {
                    try {
                        Thread.sleep(msecWaitAfterReadImageError);
                    } catch (InterruptedException e) {
                        if(e.getMessage() != null) {
                            Log.e(tag, e.getMessage());
                        }
                    }
                }
            }
        }

        private void addByte(byte[] src, int srcPos, int length, boolean reset) {
            if (reset) {
                System.arraycopy(src, srcPos, currentImageBody, 0, length);
                currentImageBodyLength = 0;
            } else {
                System.arraycopy(src, srcPos, currentImageBody, currentImageBodyLength, length);
            }
            currentImageBodyLength += length;
        }

        private void delByte(int del) {
            currentImageBodyLength -= del;
        }

        private void newFrame(Bitmap bitmap){
            if (lastFrameTimestamp > 0) {
                Log.d("performance", "------------ " + (System.currentTimeMillis() - lastFrameTimestamp) + "ms ------------");
            }

            lastFrameTimestamp = System.currentTimeMillis();
            setBitmap(bitmap);
        }
    }

    private final ScaleGestureDetector.OnScaleGestureListener scaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = detector.getScaleFactor();

            int oldW = dst.right - dst.left;
            int oldH = dst.bottom - dst.top;
            int newW = (int)(oldW * scale);
            int newH = (int)(oldH * scale);

            // TODO: also use appropriate centroid
            int screenCX = getWidth()/2;
            int screenCY = getHeight()/2;

            float CYRatio = (screenCY - dst.top)/(float)oldH;
            float CXRatio = (screenCX - dst.left)/(float)oldW;

            int newTop = (int) (dst.top - (newH - oldH) * CYRatio);
            int newLeft = (int) (dst.left - (newW - oldW) * CXRatio);
            int newBottom =  newTop + newH;
            int newRight = newLeft + newW;

            if (newH >= getHeight()) {
                // never leave a blank space
                if (newTop > 0) {
                    newTop = 0;
                    newBottom = newH;
                } else if (newBottom < getHeight()) {
                    newBottom = getHeight();
                    newTop = newBottom - newH;
                }

                dst.top = newTop;
                dst.bottom = newBottom;
            }

            if (newW >= getWidth()) {
                // never leave a blank space
                if (newLeft > 0) {
                    newLeft = 0;
                    newRight = newW;
                } else if (newRight < getWidth()) {
                    newRight = getWidth();
                    newLeft = newRight - newW;
                }

                dst.left = newLeft;
                dst.right = newRight;
            }

            // force re-render
            invalidate();

            // prevent onTouch to operate when zooming
            isTouchDown = false;

            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    };

    private boolean isTouchDown;
    private final PointF touchStart = new PointF();
    private final Rect stateStart = new Rect();
    private final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(getContext(),scaleGestureListener);

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isSupportPinchZoomAndPan) {
            return false;
        } else if(event.getPointerCount() == 1) {
            int id = event.getAction();
            if(id == MotionEvent.ACTION_DOWN){
                touchStart.set(event.getX(),event.getY());
                stateStart.set(dst);
                isTouchDown = true;
            }
            else if(id == MotionEvent.ACTION_UP || id == MotionEvent.ACTION_CANCEL){
                isTouchDown = false;
            }
            else if(id == MotionEvent.ACTION_MOVE){
                if(isTouchDown){
                    int offsetLeft = (int) (stateStart.left + event.getX() - touchStart.x);
                    int offsetTop =(int) (stateStart.top + event.getY() - touchStart.y);
                    int w = dst.right - dst.left;
                    int h = dst.bottom - dst.top;

                    // keep image in the frame -- no blank space on every side
                    offsetLeft = Math.min(0,offsetLeft);
                    offsetTop = Math.min(0,offsetTop);
                    offsetLeft = Math.max(getWidth() - w,offsetLeft);
                    offsetTop = Math.max(getHeight() - h,offsetTop);

                    dst.left = offsetLeft;
                    dst.top = offsetTop;
                    dst.right = dst.left + w;
                    dst.bottom = dst.top + h;

                    invalidate();
                }
            }
        } else {
            scaleGestureDetector.onTouchEvent(event);
        }

        return true;
    }
}
