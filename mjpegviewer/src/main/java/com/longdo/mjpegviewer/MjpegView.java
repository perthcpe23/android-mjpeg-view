package com.longdo.mjpegviewer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
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

    private static final int CHUNK_SIZE = 4096;
    private final String tag = getClass().getName();

    private Context context;
    private String url;
    private Bitmap lastBitmap;
    private MjpegDownloader downloader;

    private Paint paint;
    private Rect dst;

    private int mode = MODE_ORIGINAL;
    private int drawX,drawY, vWidth = -1, vHeight = -1;
    private int lastImgWidth, lastImgHeight;

    private boolean adjustWidth, adjustHeight;

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
        if(downloader != null && downloader.getState() != Thread.State.TERMINATED){
            downloader.cancel();
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
    }

    public void setBitmap(Bitmap bm){
        Log.d(tag,"new frame");
        if(lastBitmap != null){
            lastBitmap.recycle();
        }

        lastBitmap = bm;

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
            Log.e(tag,"Context is not instance of Activity");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(lastBitmap != null && (lastImgWidth != lastBitmap.getWidth() || lastImgHeight != lastBitmap.getHeight())) {
            vWidth = MeasureSpec.getSize(widthMeasureSpec);
            vHeight = MeasureSpec.getSize(heightMeasureSpec);

            Log.d(tag,"recalculate view/image size");

            lastImgWidth = lastBitmap.getWidth();
            lastImgHeight = lastBitmap.getHeight();

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

            setMeasuredDimension(vWidth, vHeight);
        }
        else {
            if(vWidth == -1 || vHeight == -1){
                vWidth = MeasureSpec.getSize(widthMeasureSpec);
                vHeight = MeasureSpec.getSize(heightMeasureSpec);
            }

            setMeasuredDimension(vWidth, vHeight);
        }
    }

    @Override
    protected void onDraw(Canvas c) {
        if (c != null && lastBitmap != null) {
            if(isInEditMode()){

            }
            else if (mode != MODE_ORIGINAL) {
                c.drawBitmap(lastBitmap, null, dst, paint);
            }
            else{
                c.drawBitmap(lastBitmap, drawX, drawY, paint);
            }
        } else {
            Log.d(tag, "skip drawing");
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

    class MjpegDownloader extends Thread{

        private boolean run = true;

        public void cancel(){
            run = false;
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

                    //determine boundary pattern
                    //use the whole header as separator in case boundary locate in difference chunks
                    Pattern pattern = Pattern.compile("--[_a-zA-Z0-9]*boundary\\s+(.*)\\r\\n\\r\\n",Pattern.DOTALL);
                    Matcher matcher;

                    bis = new BufferedInputStream(connection.getInputStream());
                    byte[] image = new byte[0], read = new byte[CHUNK_SIZE], tmpCheckBoundry;
                    int readByte, boundaryIndex;
                    String checkHeaderStr, boundary;

                    //always keep reading images from server
                    while (run) {
                        try {
                            readByte = bis.read(read);

                            //no more data
                            if (readByte == -1) {
                                break;
                            }

                            tmpCheckBoundry = addByte(image, read, 0, readByte);
                            checkHeaderStr = new String(tmpCheckBoundry, "ASCII");

                            matcher = pattern.matcher(checkHeaderStr);
                            if (matcher.find()) {
                                //boundary is found
                                boundary = matcher.group(0);
                                boundaryIndex = checkHeaderStr.indexOf(boundary);
                                boundaryIndex -= image.length;

                                if (boundaryIndex > 0) {
                                    image = addByte(image, read, 0, boundaryIndex);
                                } else {
                                    image = delByte(image, -boundaryIndex);
                                }

                                Bitmap outputImg = BitmapFactory.decodeByteArray(image, 0, image.length);
                                if (outputImg != null) {
                                    newFrame(outputImg);
                                } else {
                                    Log.e(tag, "Read image error");
                                }

                                int headerIndex = boundaryIndex + boundary.length();

                                image = addByte(new byte[0], read, headerIndex, readByte - headerIndex);
                            } else {
                                image = addByte(image, read, 0, readByte);
                            }
                        } catch (Exception e) {
                            Log.e(tag, e.getMessage());
                            break;
                        }
                    }

                } catch (Exception e) {
                    Log.e(tag, e.getMessage());
                }

                try {
                    bis.close();
                    connection.disconnect();
                    Log.i(tag,"disconnected with " + url);
                } catch (Exception e) {
                    Log.e(tag, e.getMessage());
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Log.e(tag, e.getMessage());
                }
            }
        }

        private byte[] addByte(byte[] base, byte[] add, int addIndex, int length) {
            byte[] tmp = new byte[base.length + length];
            System.arraycopy(base, 0, tmp, 0, base.length);
            System.arraycopy(add, addIndex, tmp, base.length, length);
            return tmp;
        }

        private byte[] delByte(byte[] base, int del) {
            byte[] tmp = new byte[base.length - del];
            System.arraycopy(base, 0, tmp, 0, tmp.length);
            return tmp;
        }

        private void newFrame(Bitmap bitmap){
            setBitmap(bitmap);
        }
    }
}
