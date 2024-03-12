package io.github.no.today.watchdog.helper;

import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.core.view.InputDeviceCompat;

import java.lang.reflect.Method;

public class TouchHelper {

    static InputManager inputManager;
    static Method injectInputEvent;
    static long downTime;

    static {
        try {
            inputManager = (InputManager) InputManager.class.getDeclaredMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
            injectInputEvent = InputManager.class.getMethod("injectInputEvent", InputEvent.class, Integer.TYPE);
            MotionEvent.class.getDeclaredMethod("obtain", new Class[0]).setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void touchDown(float clientX, float clientY) {
        downTime = SystemClock.uptimeMillis();
        injectMotionEvent(InputDeviceCompat.SOURCE_TOUCHSCREEN, KeyEvent.ACTION_DOWN, downTime, downTime, clientX,
                clientY, 1.0f);
    }

    public static void touchMove(float clientX, float clientY) {
        injectMotionEvent(InputDeviceCompat.SOURCE_TOUCHSCREEN, 2, downTime,
                SystemClock.uptimeMillis(), clientX, clientY, 1.0f);
    }

    public static void touchUp(float clientX, float clientY) {
        injectMotionEvent(InputDeviceCompat.SOURCE_TOUCHSCREEN, KeyEvent.ACTION_UP, downTime,
                SystemClock.uptimeMillis(), clientX, clientY, 1.0f);
    }

    public static void entryKey(int keyCode) {
        sendKeyEvent(InputDeviceCompat.SOURCE_KEYBOARD, keyCode);
    }

    public static void menu() {
        sendKeyEvent(InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_MENU);
    }

    public static void back() {
        sendKeyEvent(InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_BACK);
    }

    public static void home() {
        sendKeyEvent(InputDeviceCompat.SOURCE_KEYBOARD, KeyEvent.KEYCODE_HOME);
    }

    public static void injectMotionEvent(int inputSource, int action,
                                         long downTime, long eventTime, float x, float y, float pressure) {
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, action, x, y, pressure, 1.0f, 0, 1.0f, 1.0f, 0, 0);
        event.setSource(inputSource);
        try {
            injectInputEvent.invoke(inputManager, event, 0);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public static void sendKeyEvent(int inputSource, int keyCode) {
        long now = SystemClock.uptimeMillis();
        injectKeyEvent(new KeyEvent(now, now, 0, keyCode, 0, 0, -1, 0, 0, inputSource));
        injectKeyEvent(new KeyEvent(now, now, 1, keyCode, 0, 0, -1, 0, 0, inputSource));
    }

    public static void injectKeyEvent(KeyEvent event) {
        try {
            injectInputEvent.invoke(inputManager, event, 0);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}