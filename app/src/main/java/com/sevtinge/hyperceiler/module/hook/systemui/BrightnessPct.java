package com.sevtinge.hyperceiler.module.hook.systemui;

import static com.sevtinge.hyperceiler.utils.devicesdk.SystemSDKKt.isMoreHyperOSVersion;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.sevtinge.hyperceiler.module.base.BaseHook;

import de.robv.android.xposed.XposedHelpers;

public class BrightnessPct extends BaseHook {
    @Override
    public void init() throws NoSuchMethodException {

        if (!isMoreHyperOSVersion(1f)) {
            findAndHookMethod("com.android.systemui.statusbar.policy.BrightnessMirrorController", "showMirror", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    ViewGroup mStatusBarWindow = (ViewGroup) XposedHelpers.getObjectField(param.thisObject, "mStatusBarWindow");
                    if (mStatusBarWindow == null) {
                        logE(TAG, lpparam.packageName, "mStatusBarWindow is null");
                        return;
                    }
                    initPct(mStatusBarWindow, 1, mStatusBarWindow.getContext());
                    getTextView().setVisibility(View.VISIBLE);
                }
            });

            findAndHookMethod("com.android.systemui.statusbar.policy.BrightnessMirrorController", "hideMirror", new MethodHook() {
                    @Override
                    protected void after(MethodHookParam param) throws Throwable {
                        removePct(getTextView());
                    }
                }
            );
        }

        hookAllMethods("com.android.systemui.controlcenter.policy.MiuiBrightnessController", "onStart", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                Object mMirror = XposedHelpers.getObjectField(param.thisObject, "mControl");
                Object controlCenterWindowViewController = XposedHelpers.getObjectField(mMirror, "controlCenterWindowViewController");
                String ClsName = controlCenterWindowViewController.getClass().getName();
                if (!ClsName.equals("ControlCenterWindowViewController")) {
                    controlCenterWindowViewController = XposedHelpers.callMethod(controlCenterWindowViewController, "get");
                }
                Object windowView = XposedHelpers.callMethod(controlCenterWindowViewController, "getView");
                if (windowView == null) {
                    logE(TAG, lpparam.packageName, "mControlPanelContentView is null");
                    return;
                }
                initPct((ViewGroup) windowView, 2, mContext);
                getTextView().setVisibility(View.VISIBLE);
            }
        });

        hookAllMethods("com.android.systemui.controlcenter.policy.MiuiBrightnessController", "onStop", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                removePct(getTextView());
            }
        });

        final Class<?> BrightnessUtils = findClassIfExists("com.android.systemui.controlcenter.policy.BrightnessUtils");
        hookAllMethods("com.android.systemui.controlcenter.policy.MiuiBrightnessController", "onChanged", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                int pctTag = 0;
                if (getTextView() != null && getTextView().getTag() != null) {
                    pctTag = (int) getTextView().getTag();
                }
                if (pctTag == 0 || getTextView() == null) return;
                int currentLevel = (int) param.args[3];
                if (BrightnessUtils != null) {
                    int maxLevel = (int) XposedHelpers.getStaticObjectField(BrightnessUtils, "GAMMA_SPACE_MAX");
                    getTextView().setText(((currentLevel * 100) / maxLevel) + "%");
                }
            }
        });
    }
}
