package com.example.qkx.speechtotext.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by qkx on 16/7/25.
 */
public class ImageUtil {
    /**
     * 根据指定大小进行比例缩放加载指定图片
     *
     * @param context
     * @param maxWidth
     * @param maxHeight
     * @param uri
     * @return
     * @throws FileNotFoundException
     */
    public static Bitmap decodeBitmapByRatioSize(Context context, int maxWidth,
                                                 int maxHeight, Uri uri) throws FileNotFoundException {
        if (null == context || null == uri) {
            throw new FileNotFoundException("context is null or file is null!!!");
        }
        InputStream is1 = context.getContentResolver().openInputStream(uri);
        InputStream is2 = context.getContentResolver().openInputStream(uri);
        return decodeBitmapByRatioSize(maxWidth, maxHeight, is1, is2);
    }


    /**
     * 按指定大小进行比例缩放加载指定图片
     *
     * @param maxWidth
     * @param maxHeight
     * @param is1
     * @param is2
     * @return
     */
    private static Bitmap decodeBitmapByRatioSize(int maxWidth, int maxHeight,
                                                  InputStream is1, InputStream is2) {

        // 载入图片尺寸大小没载入图片本身 true
        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is1, null, bmpFactoryOptions);

        // outHeight图像高 outWidth图像宽
        int mOutHeight = bmpFactoryOptions.outHeight;
        int mOutWidth = bmpFactoryOptions.outWidth;
        int mRatioHeight = (int) Math.ceil(mOutHeight / (float) maxHeight);
        int mRatioWidth = (int) Math.ceil(mOutWidth / (float) maxWidth);

        bmpFactoryOptions.inDither = false;
        bmpFactoryOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bmpFactoryOptions.inSampleSize = 1;

        // inSampleSize表示图片占原图比例 =1表示原图
        if (mRatioHeight > 1 && mRatioWidth > 1) {
            if (mRatioHeight > mRatioWidth) {
                bmpFactoryOptions.inSampleSize = mRatioHeight;
            } else {
                bmpFactoryOptions.inSampleSize = mRatioWidth;
            }
        }

        // 图像真正解码 false
        bmpFactoryOptions.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeStream(is2, null, bmpFactoryOptions);
        try {
            is1.close();
            is2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
