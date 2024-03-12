package io.github.no.today.watchdog;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;

public class WatchdogHelper {
    public static final Gson JSON = new Gson();

    public static byte[] createBlankImage(int width, int height) {
        // 创建一个空白的 Bitmap 对象
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // 创建一个画布，并在画布上绘制白色背景
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        // 将 Bitmap 转换为字节数组
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        // 释放资源
        bitmap.recycle();
        return byteArray;
    }

}
