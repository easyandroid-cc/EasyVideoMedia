/*
 * Copyright (C) 2016 Brian Wernick
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devbrackets.android.exomediademo.ui.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.devbrackets.android.exomedia.ui.widget.VideoControlsMobile;
import com.devbrackets.android.exomediademo.R;

import static android.content.Context.AUDIO_SERVICE;

public class EasyVideoControlsMobile extends VideoControlsMobile {
    private FrameLayout centerContentWrapper;
    private TextView centerInfo;
    // Brightness
    private boolean mIsFirstBrightnessGesture = true;

    public EasyVideoControlsMobile(Context context) {
        super(context);
    }

    public EasyVideoControlsMobile(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EasyVideoControlsMobile(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EasyVideoControlsMobile(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

    }

    @Override
    protected void setup(Context context) {
        super.setup(context);
        centerContentWrapper = (FrameLayout) findViewById(R.id.centerContentWrapper);
        centerContentWrapper.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        centerInfo = (TextView) findViewById(R.id.centerInfo);
        centerContentWrapper.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return dispatchCenterWrapperTouchEvent(event);
            }
        });
        initVol();
    }

    //Touch Events
    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_VOLUME = 1;
    private static final int TOUCH_BRIGHTNESS = 2;
    private static final int TOUCH_SEEK = 3;

    private int mTouchAction = TOUCH_NONE;
    private int mSurfaceYDisplayRange;
    private float mInitTouchY;
    private float touchX = -1f;
    private float touchY = -1f;

    private void initVol() {
            /* Services and miscellaneous */
        mAudioManager = (AudioManager) getContext().getApplicationContext().getSystemService(AUDIO_SERVICE);
        mAudioMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

    }

    //Volume
    private AudioManager mAudioManager;
    private int mAudioMax;
    private float mVol;

    private boolean dispatchCenterWrapperTouchEvent(MotionEvent event) {
        System.out.println("event ="+event.getAction());
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics screen = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(screen);
        if (mSurfaceYDisplayRange == 0) {
            mSurfaceYDisplayRange = Math.min(screen.widthPixels, screen.heightPixels);
        }
        float x_changed, y_changed;
        if (touchX != -1f && touchY != -1f) {
            y_changed = event.getRawY() - touchY;
            x_changed = event.getRawX() - touchX;
        } else {
            x_changed = 0f;
            y_changed = 0f;
        }
        float coef = Math.abs(y_changed / x_changed);
        float xgesturesize = (((event.getRawX() - touchX) / screen.xdpi) * 2.54f);//2.54f
        float delta_y = Math.max(1f, (Math.abs(mInitTouchY - event.getRawY()) / screen.xdpi + 0.5f) * 2f);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchAction = TOUCH_NONE;
                touchX = event.getRawX();
                mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                touchY = mInitTouchY = event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:

                if (mTouchAction != TOUCH_SEEK && coef > 2) {
                    if (Math.abs(y_changed / mSurfaceYDisplayRange) < 0.05) {
                        return false;
                    }

                    touchX = event.getRawX();
                    touchY = event.getRawY();


                    if ((int) touchX > (4 * screen.widthPixels / 7)) {
                        doVolumeTouch(y_changed);
//                        hideCenterInfo();
//                            hideOverlay(true);
                    }
                    // Brightness (Up or Down - Left side)
                    if ((int) touchX < (3 * screen.widthPixels / 7)) {
                        doBrightnessTouch(y_changed);
                    }

                } else {
                    doSeekTouch(Math.round(delta_y), xgesturesize, false);
                }

                break;

            case MotionEvent.ACTION_UP:
                if (mTouchAction == TOUCH_SEEK) {
                    doSeekTouch(Math.round(delta_y), xgesturesize, true);
                }

                if (mTouchAction != TOUCH_NONE) {
                    hideCenterInfo();
                }

                touchX = -1f;
                touchY = -1f;
                break;
            default:
                break;
        }


        return mTouchAction != TOUCH_NONE;
    }

    private void hideCenterInfo() {
        centerInfo.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
        centerInfo.setVisibility(GONE);

    }

    private void doBrightnessTouch(float y_changed) {
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_BRIGHTNESS) {
            return;
        }

        mTouchAction = TOUCH_BRIGHTNESS;
        if (mIsFirstBrightnessGesture) {
            initBrightnessTouch();
        }

        mTouchAction = TOUCH_BRIGHTNESS;
//
        // Set delta : 2f is arbitrary for now, it possibly will change in the future
        float delta = -y_changed / mSurfaceYDisplayRange;
        changeBrightness(delta);
    }

    private void doVolumeTouch(float y_changed) {
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_VOLUME) {
            return;
        }

        int oldVol = (int) mVol;
        mTouchAction = TOUCH_VOLUME;
        float delta = -((y_changed / mSurfaceYDisplayRange) * mAudioMax);
        mVol += delta;
        int vol = (int) Math.min(Math.max(mVol, 0), mAudioMax);
        if (delta != 0f) {
            setAudioVolume(vol, vol > oldVol);
        }
    }

    private void doSeekTouch(int coef, float gesturesize, boolean seek) {

        if (coef == 0) {
            coef = 1;
        }
        // No seek action if coef > 0.5 and gesturesize < 1cm

        if (Math.abs(gesturesize) < 1 || !canSeek()) {
            return;
        }


        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_SEEK) {
            return;
        }


        mTouchAction = TOUCH_SEEK;

        long length = videoView.getDuration();
        long time = videoView.getCurrentPosition();

        // Size of the jump, 10 minutes max (600000), with a bi-cubic progression, for a 8cm gesture
        int jump = (int) ((Math.signum(gesturesize) * ((600000 * Math.pow((gesturesize / 8), 4)) + 3000)) / coef);

        // Adjust the jump
        if ((jump > 0) && ((time + jump) > length)) {
            jump = (int) (length - time);
        }


        if ((jump < 0) && ((time + jump) < 0)) {
            jump = (int) -time;
        }
        //Jump !
        if (seek && length > 0) {
            seek(time + jump);
        }
        if (length > 0) {
            //Show the jump's size
            setFastForwardOrRewind(time + jump, jump > 0 ? R.mipmap.ic_fast_forward_white_36dp : R.mipmap.ic_fast_rewind_white_36dp);
        }
    }

    private void setFastForwardOrRewind(long changingTime, @DrawableRes int drawableId) {
        centerInfo.setVisibility(VISIBLE);
        centerInfo.setText(generateFastForwardOrRewindTxt(changingTime));
        centerInfo.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), drawableId), null, null);
    }

    private CharSequence generateFastForwardOrRewindTxt(long changingTime) {

        long duration = videoView == null ? 0 : videoView.getDuration();
        String result = stringForTime(changingTime) + " / " + stringForTime(duration);

        int index = result.indexOf("/");

        SpannableString spannableString = new SpannableString(result);


        TypedValue typedValue = new TypedValue();
        TypedArray a = getContext().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorAccent});
        int color = a.getColor(0, 0);
        a.recycle();
        spannableString.setSpan(new ForegroundColorSpan(color), 0, index, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        return spannableString;
    }

    private String stringForTime(long timeMs) {


        return "time";
    }

    private void seek(long position) {
        if (videoView != null) {
            videoView.seekTo(position);
        }

    }

    private void initBrightnessTouch() {

        if (!(getContext() instanceof Activity)) {
            return;
        }
        Activity activity = (Activity) getContext();

        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        float brightnesstemp = lp.screenBrightness != -1f ? lp.screenBrightness : 0.6f;
        // Initialize the layoutParams screen brightness
        try {
            if (Settings.System.getInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                if (!Permissions.canWriteSettings(activity)) {
                    return;
                }
                Settings.System.putInt(activity.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
//                restoreAutoBrightness = android.provider.Settings.System.getInt(activity.getContentResolver(),
//                        android.provider.Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
            } else if (brightnesstemp == 0.6f) {
                brightnesstemp = android.provider.Settings.System.getInt(activity.getContentResolver(),
                        android.provider.Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        lp.screenBrightness = brightnesstemp;
        activity.getWindow().setAttributes(lp);
        mIsFirstBrightnessGesture = false;
    }

    private boolean canSeek() {
//        seekBar.can


//        Timeline currentTimeline = videoView != null ? videoView.getCurrentTimeline() : null;
//        boolean haveNonEmptyTimeline = currentTimeline != null && !currentTimeline.isEmpty();
//        boolean isSeekable = false;
//        if (haveNonEmptyTimeline) {
//            int currentWindowIndex = player.getCurrentWindowIndex();
//            currentTimeline.getWindow(currentWindowIndex, currentWindow);
//            isSeekable = currentWindow.isSeekable;
////            enablePrevious = currentWindowIndex > 0 || isSeekable || !currentWindow.isDynamic;
//        }

        return videoView != null && videoView.getDuration() > 0;
    }

    private void changeBrightness(float delta) {
        // Estimate and adjust Brightness
        if (!(getContext() instanceof Activity)) {
            return;
        }
        Activity activity = (Activity) getContext();


        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        float brightness = Math.min(Math.max(lp.screenBrightness + delta, 0.01f), 1f);
        setWindowBrightness(brightness);
        brightness = Math.round(brightness * 100);

        int brightnessInt = (int) brightness;

        setVolumeOrBrightnessInfo(getContext().getString(R.string.brightness_changing, brightnessInt), whichBrightnessImageToUse(brightnessInt));
    }

    @DrawableRes
    private int whichBrightnessImageToUse(int brightnessInt) {
        if (brightnessInt <= 15) {
            return R.mipmap.ic_brightness_1_white_36dp;
        } else if (brightnessInt <= 30 && brightnessInt > 15) {
            return R.mipmap.ic_brightness_2_white_36dp;
        } else if (brightnessInt <= 45 && brightnessInt > 30) {
            return R.mipmap.ic_brightness_3_white_36dp;
        } else if (brightnessInt <= 60 && brightnessInt > 45) {
            return R.mipmap.ic_brightness_4_white_36dp;
        } else if (brightnessInt <= 75 && brightnessInt > 60) {
            return R.mipmap.ic_brightness_5_white_36dp;
        } else if (brightnessInt <= 90 && brightnessInt > 75) {
            return R.mipmap.ic_brightness_6_white_36dp;
        } else {
            return R.mipmap.ic_brightness_7_white_36dp;
        }
    }

    private void setVolumeOrBrightnessInfo(String txt, @DrawableRes int drawableId) {
        centerInfo.setVisibility(VISIBLE);
        centerInfo.setText(txt);
        centerInfo.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
        centerInfo.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), drawableId), null, null);
    }

    private void setAudioVolume(int vol, boolean up) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);

        /* Since android 4.3, the safe volume warning dialog is displayed only with the FLAG_SHOW_UI flag.
         * We don't want to always show the default UI volume, so show it only when volume is not set. */
        int newVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (vol != newVol) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, AudioManager.FLAG_SHOW_UI);
        }

        mTouchAction = TOUCH_VOLUME;
        vol = vol * 100 / mAudioMax;
        int drawableId;
        if (newVol == 0) {
            drawableId = R.mipmap.ic_volume_mute_white_36dp;
        } else if (up) {
            drawableId = R.mipmap.ic_volume_up_white_36dp;
        } else {
            drawableId = R.mipmap.ic_volume_down_white_36dp;
        }
        setVolumeOrBrightnessInfo(getContext().getString(R.string.volume_changing, vol), drawableId);
//        showInfoWithVerticalBar(getString(R.string.volume) + "\n" + Integer.toString(vol) + '%', 1000, vol);
    }

    private void setWindowBrightness(float brightness) {
        if (!(getContext() instanceof Activity)) {
            return;
        }
        Activity activity = (Activity) getContext();


        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = brightness;
        // Set Brightness
        activity.getWindow().setAttributes(lp);
    }

    @Override
    protected int getLayoutResource() {
        return com.devbrackets.android.exomediademo.R.layout.exo_playback_control_view;
    }
}