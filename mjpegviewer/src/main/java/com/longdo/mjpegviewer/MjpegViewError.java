package com.longdo.mjpegviewer;

public class MjpegViewError extends Throwable {

    /**
     * Cannot detect Mjpeg stream boundary, use default boundary instead
     */
    public static int ERROR_CODE_EXTRACT_BOUNDARY_FAILED = 1;

    /**
     * Cannot parse stream into bitmap image
     */
    public static int ERROR_CODE_READ_IMAGE_STREAM_FAILED = 2;

    /**
     * Cannot open a connection to server
     */
    public static int ERROR_CODE_OPEN_CONNECTION_FAILED = 3;

    /**
     * Context is not an instance of Activity
     */
    public static int ERROR_CODE_INVALID_CONTEXT = 4;

    public int errorCode;
    public String errorMessage = null;

    public MjpegViewError(int errorCode) {
        this.errorCode = errorCode;
    }

    public MjpegViewError(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
