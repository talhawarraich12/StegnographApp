package com.seecs.mushtaq.async;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.seecs.mushtaq.utils.BitmapUtils;
import com.seecs.mushtaq.utils.SteganographyUtils;

public class DecodeTask extends SteganographyTask {
    public DecodeTask(AsyncResponse<SteganographyParams> delegate) {
        super(delegate);
    }


    @Override
    protected SteganographyParams execute(SteganographyParams steganographyParams) {
        Bitmap bitmap = BitmapFactory.decodeFile(steganographyParams.getFilePath());

        String message = SteganographyUtils.decode(BitmapUtils.getPixels(bitmap));

        steganographyParams.setMessage(message);
        steganographyParams.setType(AsyncResponse.Type.DECODE_SUCCESS);
        return steganographyParams;
    }
}