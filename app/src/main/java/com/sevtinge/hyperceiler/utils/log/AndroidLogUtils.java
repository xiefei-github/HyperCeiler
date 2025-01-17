package com.sevtinge.hyperceiler.utils.log;

import static com.sevtinge.hyperceiler.utils.BuildUtils.getBuildType;

import android.util.Log;

import com.sevtinge.hyperceiler.BuildConfig;
import com.sevtinge.hyperceiler.module.base.BaseHook;

/* 不太建议在非 Xposed 代码使用处调用，虽然已经做了 try 处理，但是 detailLog 将始终为 false
 * 可能因为 <BaseHook.mPrefsMap.getBoolean("settings_disable_detailed_log");>
 * 会导致 <java.lang.NoClassDefFoundError: Failed resolution of: Lcom/sevtinge/hyperceiler/XposedInit;> 等
 * */
public class AndroidLogUtils {
    private static final String Tag = "[HyperCeiler]: ";
    private static final boolean isDebugVersion = (getBuildType().equals("debug"));
    private static final boolean isNotReleaseVersion = !(getBuildType().equals("release"));
    private static boolean detailLog = false;
    private static boolean run = false;

    private static void getDisableDetailedLog() {
        if (!run) {
            try {
                detailLog = BaseHook.mPrefsMap.getBoolean("settings_disable_detailed_log");
            } catch (Throwable e) {
                LogE("getDisableDetailedLog", "It is not recommended to call this class in non Xposed code," +
                    "detailLog will be false: ", e);
            }
            run = true;
        }
    }

    public static void LogI(String tag, String msg) {
        getDisableDetailedLog();
        if (!isDebugVersion) return;
        if (detailLog) return;
        Log.i(tag, "[I]" + Tag + msg);
    }

    public static void deLogI(String tag, String msg) {
        Log.i(tag, "[I/" + Tag + msg);
    }

    public static void LogD(String tag, Throwable tr) {
        if (!isDebugVersion) return;
        Log.d(tag, "[D]" + Tag, tr);
    }

    public static void LogD(String tag, String msg, Throwable tr) {
        if (!isDebugVersion) return;
        Log.d(tag, "[D]" + Tag + msg, tr);
    }

    public static void LogE(String tag, Throwable tr) {
        if (!isDebugVersion) return;
        Log.e(tag, "[E]" + Tag, tr);
    }

    public static void LogE(String tag, String msg, Throwable tr) {
        if (!isDebugVersion) return;
        Log.e(tag, "[E]" + Tag + msg, tr);
    }
}
