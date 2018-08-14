package com.seecs.mushtaq.async;

import android.graphics.Bitmap;
import android.net.Uri;

import com.seecs.mushtaq.utils.BitmapUtils;
import com.seecs.mushtaq.utils.FileUtils;
import com.seecs.mushtaq.utils.SteganographyUtils;

public class EncodeTask extends SteganographyTask {
    public EncodeTask(AsyncResponse<SteganographyParams> delegate) {
        super(delegate);
    }


    @Override
    protected SteganographyParams execute(SteganographyParams steganographyParams) {

        Bitmap bitmap = BitmapUtils.decodeFile(steganographyParams.getFilePath());
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int numberOfPixels = w * h;

        byte[] data = steganographyParams.getMessage().getBytes();

        int requiredLength = data.length * 8 + 32;

        if (requiredLength > numberOfPixels) {
            throw new IllegalArgumentException("Message is too long to fit into pixels.");
        }

        int[] encodedPixels = SteganographyUtils.encode(
                BitmapUtils.getPixels(bitmap, requiredLength),
                steganographyParams.getMessage()
        );

        BitmapUtils.setPixels(bitmap, encodedPixels);

        Uri resultUri = FileUtils.saveBitmap(bitmap);

        steganographyParams.setResultUri(resultUri);

        steganographyParams.setType(AsyncResponse.Type.ENCODE_SUCCESS);
        return steganographyParams;
    }
}