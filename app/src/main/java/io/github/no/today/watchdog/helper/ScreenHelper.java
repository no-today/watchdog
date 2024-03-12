package io.github.no.today.watchdog.helper;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.IDisplayManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.os.ServiceManager;
import android.view.DisplayInfo;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import android.view.SurfaceControl;

import java.io.ByteArrayOutputStream;

/**
 * https://github.com/android-notes/androidScreenShare/blob/master/shareandcontrollib/src/main/java/com/wanjian/puppet/ScreenUtils.java
 */
public class ScreenHelper {
    private static final float SCALE = 1f;
    private static final Point sDisplaySize = new Point();

    static {
        if (VERSION.SDK_INT >= 18) {
            IWindowManager wm = Stub.asInterface((IBinder) ServiceManager.getService("window"));
            wm.getInitialDisplaySize(0, sDisplaySize);
        } else if (VERSION.SDK_INT == 17) {
            DisplayInfo di = IDisplayManager.Stub.asInterface((IBinder) ServiceManager.getService("display")).getDisplayInfo(0);
            sDisplaySize.x = di.logicalWidth;
            sDisplaySize.y = di.logicalHeight;
        } else {
            IWindowManager wm = Stub.asInterface((IBinder) ServiceManager.getService("window"));
            wm.getRealDisplaySize(sDisplaySize);
        }
    }

    private static Point getDisplaySize() {
        return new Point(sDisplaySize.x, sDisplaySize.y);
    }

    private static Bitmap screenshot_() throws Exception {
        Point size = getDisplaySize();
        Bitmap b;
        if (Build.VERSION.SDK_INT <= 17) {
            String surfaceClassName = "android.view.Surface";
            b = (Bitmap) Class.forName(surfaceClassName).getDeclaredMethod("screenshot", new Class[]{int.class, int.class}).invoke(null, new Object[]{size.x, size.y});

        } else if (Build.VERSION.SDK_INT < 28) {
            b = SurfaceControl.screenshot(size.x, size.y);
        } else {
            b = SurfaceControl.screenshot(new Rect(0, 0, size.x, size.y), size.x, size.y, 0);
        }
        return b;
    }

    public static byte[] screenshot() {
        try {
            Bitmap bitmap = screenshot_();
            Point displaySize = getDisplaySize();
            bitmap = Bitmap.createScaledBitmap(bitmap, (int) (displaySize.x * SCALE), (int) (displaySize.y * SCALE), true);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
}