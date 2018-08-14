package com.seecs.mushtaq.async;

import android.net.Uri;

public class SteganographyParams {

    private String mFilePath;
    private Uri mResultUri;
    private String mMessage;
    private String mKey;
    private AsyncResponse.Type mType;
    private byte[] messageBytes;

    public SteganographyParams(String filePath, String message, String Key) {
        mFilePath = filePath;
        mMessage = message;
        mKey = Key;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public String getMessage() {
        return mMessage;
    }

    public byte[] getmessageBytes() {
        return messageBytes;
    }

    public void setmessageBytes(byte [] data) {
        messageBytes = data;
    }

    public String getKey() {
        return mKey;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public AsyncResponse.Type getType() {
        return mType;
    }

    public void setType(AsyncResponse.Type type) {
        mType = type;
    }

    public Uri getResultUri() {
        return mResultUri;
    }

    public void setResultUri(Uri resultUri) {
        mResultUri = resultUri;
    }
}