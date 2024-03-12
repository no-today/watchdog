package android.view;

import android.graphics.Point;
import android.os.IBinder;

public interface IWindowManager {
    void getInitialDisplaySize(int i, Point displaySize);

    void getRealDisplaySize(Point displaySize);

    abstract class Stub {
        public static IWindowManager asInterface(IBinder invoke) {
            return null;
        }
    }
}