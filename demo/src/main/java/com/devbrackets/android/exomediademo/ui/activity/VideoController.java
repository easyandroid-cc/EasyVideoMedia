package com.devbrackets.android.exomediademo.ui.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.AttrRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.devbrackets.android.exomedia.util.TimeFormatUtil;
import com.devbrackets.android.exomediademo.R;

/**
 * 音量和屏幕亮度的控制器
 */
public class VideoController extends FrameLayout {
    private FrameLayout centerContentWrapper;
    private TextView centerInfo;
    // Brightness
    private boolean mIsFirstBrightnessGesture = true;

    EasyVideoControlsMobile videoControls;

    EasyVideoControlsMobile.VideoControllerChanged videoControllerChanged;

    public void setOnSeekBarChangeListener(EasyVideoControlsMobile.VideoControllerChanged videoControllerChanged) {
        this.videoControllerChanged = videoControllerChanged;
    }

    public void setVideoControls(EasyVideoControlsMobile videoControls) {
        this.videoControls = videoControls;
    }

    public VideoController(@NonNull Context context) {
        super(context);
        setup(context);
    }

    public VideoController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    public VideoController(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoController(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup(context);
    }

    GestureDetector gestureDetector;

    protected void setup(Context context) {
        gestureDetector = new GestureDetector(context, new PlayerGestureListener());
        View.inflate(context, R.layout.video_control_view, this);
        centerContentWrapper = (FrameLayout) findViewById(R.id.centerContentWrapper);
        centerContentWrapper.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoControls != null) {
                    if (videoControls.isVisible()) {
                        videoControls.hide();
                    } else {
                        videoControls.show();
                    }
                }
            }
        });
        centerInfo = (TextView) findViewById(R.id.centerInfo);
//        centerContentWrapper.setOnTouchListener(new OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return dispatchCenterWrapperTouchEvent(event);
//            }
//        });
        centerContentWrapper.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                if (getPlayerView().isListPlayer()&&VideoPlayUtils.getOrientation(activity) == Configuration.ORIENTATION_PORTRAIT) {//竖屏
//                    return false;//列表竖屏不执行手势
//                }
//                System.out.println("cgp  e1.getAction()=" +  event.getAction());
                if (gestureDetector.onTouchEvent(event))
                    return true;
                // 处理手势结束
//                switch (event.getAction() & MotionEvent.ACTION_MASK) {
//                    case MotionEvent.ACTION_UP:
//                        endGesture();
//                        break;
//                }
                if (MotionEvent.ACTION_UP == event.getAction()) {
                    hideCenterInfo();
                    mVol = -1;
                    brightness = -1f;
                    videoControllerChanged.onStopTrackingTouch();
                }
                return false;
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
        mAudioManager = (AudioManager) getContext().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

    }

    //Volume
    private AudioManager mAudioManager;
    private int mAudioMax;
    private int mVol = -1;

    private boolean dispatchCenterWrapperTouchEvent(MotionEvent event) {
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
//                System.out.println("cgp ACTION_MOVE");
                if (mTouchAction != TOUCH_SEEK && coef > 2) {
                    if (Math.abs(y_changed / mSurfaceYDisplayRange) < 0.05) {
                        return false;
                    }
                    touchX = event.getRawX();
                    touchY = event.getRawY();
                    if ((int) touchX > (4 * screen.widthPixels / 7)) {
                        doVolumeTouch(y_changed);
                    }
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
//        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_BRIGHTNESS) {
//            return;
//        }

//        mTouchAction = TOUCH_BRIGHTNESS;
        if (mIsFirstBrightnessGesture) {
            initBrightnessTouch();
        }

//        mTouchAction = TOUCH_BRIGHTNESS;
//
        // Set delta : 2f is arbitrary for now, it possibly will change in the future
        float delta =  y_changed/10  ;
        changeBrightness(delta);
    }

    private void doVolumeTouch(float y_changed) {
//        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_VOLUME) {
//            return;
//        }

        int oldVol = (int) mVol;
//        mTouchAction = TOUCH_VOLUME;
        float delta = -((y_changed) * mAudioMax);
        mVol += delta;
        int vol = (int) Math.min(Math.max(mVol, 0), mAudioMax);
        if (delta != 0f) {
            setAudioVolume(vol, vol > oldVol);
        }
    }

    private void doSeekTouch(int coef, float gesturesize, boolean seek) {
        System.out.println("cgp doSeekTouch coef=" + coef);
        System.out.println("cgp doSeekTouch gesturesize=" + gesturesize);
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

        if (mTouchAction != TOUCH_SEEK) {
            videoControllerChanged.onStartTrackingTouch();
        }
        mTouchAction = TOUCH_SEEK;
        long length = videoControls.getDuration();
        long time = videoControls.getCurrentPosition();
        // Size of the jump, 10 minutes max (600000), with a bi-cubic progression, for a 8cm gesture
        int jump = (int) ((Math.signum(gesturesize) * ((600000 * Math.pow((gesturesize / 8), 4)) + 3000)) / coef);
        // Adjust the jump
        if ((jump > 0) && ((time + jump) > length)) {
            jump = (int) (length - time);
        }
        if ((jump < 0) && ((time + jump) <= 0)) {
            jump = (int) -time;
        }
        //Jump !
        if (seek && length > 0) {//抬起
            seek(time + jump);
        }
        if (length > 0) {
            //Show the jump's size
            setFastForwardOrRewind(time + jump, jump > 0 ? R.mipmap.ic_fast_forward_white_36dp : R.mipmap.ic_fast_rewind_white_36dp);
            videoControls.setPosition(time + jump);
        }
    }

    private void setFastForwardOrRewind(long changingTime, @DrawableRes int drawableId) {
        centerInfo.setVisibility(VISIBLE);
        centerInfo.setText(generateFastForwardOrRewindTxt(changingTime));
        centerInfo.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), drawableId), null, null);
    }

    private CharSequence generateFastForwardOrRewindTxt(long changingTime) {
        long duration = videoControls == null ? 0 : videoControls.getDuration();
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


        return TimeFormatUtil.formatMs(timeMs);
    }

    private void seek(long position) {
        if (videoControls != null) {

            videoControllerChanged.onProgressChanged((int) position);

//            videoControls.setPosition(position);
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

        return videoControls != null;
    }

//    VideoView videoView;
//
//    public void setVideoView(@Nullable VideoView VideoView) {
//        this.videoView = VideoView;
//    }

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

    /****
     * 手势监听类
     *****/
    private class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {
        private boolean firstTouch;
        private boolean volumeControl;
        private boolean toSeek;


        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            hideCenterInfo();
            return super.onSingleTapUp(e);
        }

        /**
         * 双击
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            firstTouch = true;
            return super.onDown(e);
        }

        /**
         * 滑动
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            if (mSurfaceYDisplayRange == 0) {
                WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                DisplayMetrics screen = new DisplayMetrics();
                wm.getDefaultDisplay().getMetrics(screen);
                mSurfaceYDisplayRange = Math.min(screen.heightPixels, screen.widthPixels);
            }
            float mOldX = e1.getX(), mOldY = e1.getY();
            float deltaY = mOldY - e2.getY();
            float deltaX = mOldX - e2.getX();
            if (firstTouch) {
                toSeek = Math.abs(distanceX) >= Math.abs(distanceY);
                volumeControl = mOldX > centerContentWrapper.getWidth() * 0.5f;
                firstTouch = false;
                videoControllerChanged.onStartTrackingTouch();
            }
            if (toSeek) {

                deltaX = -deltaX;
                long position = videoControls.getCurrentPosition();
                long duration = videoControls.getDuration();
                long newPosition = (int) (position + deltaX * duration / centerContentWrapper.getWidth());
                if (newPosition > duration) {
                    newPosition = duration;
                } else if (newPosition <= 0) {
                    newPosition = 0;
                }
//                showProgressDialog(deltaX, stringForTime(newPosition), newPosition, stringForTime(duration), duration);
                seek(newPosition);
                if (newPosition > 0) {
                    //Show the jump's size
                    setFastForwardOrRewind(newPosition, newPosition > 0 ? R.mipmap.ic_fast_forward_white_36dp : R.mipmap.ic_fast_rewind_white_36dp);
                    videoControls.setPosition(newPosition);
                }
            } else {
                float percent = deltaY / mSurfaceYDisplayRange;

                if (volumeControl) {
//                    showVolumeDialog(percent);
                    showVolumeDialog(percent);
//                    setAudioVolume((int) (percent * mAudioMax), false);
//                    System.out.println("cgp doVolumeTouch percent=" + percent);
                } else {
                    showBrightnessDialog(percent);
//                    System.out.println("cgp doBrightnessTouch percent=" + percent);
//                    showBrightnessDialog(percent);
                }
            }
//            System.out.println("cgp  e1.getAction()=" + e1.getAction());
//            if (MotionEvent.ACTION_UP == e1.getAction()) {
//                hideCenterInfo();
//            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

    }

    private void showVolumeDialog(float percent) {
        if (mVol == -1) {
            mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mVol < 0)
                mVol = 0;
        }
        System.out.println("cgp percent = " + percent);
        System.out.println("cgp mAudioMax = " + mAudioMax);

        int index =  (int) (percent * mAudioMax) + mVol;
        if (index > mAudioMax) {
            index = mAudioMax;
        } else if (index < 0) {
            index = 0;
        }
        System.out.println("cgp index = " + index);
        // 变更进度条
        // int i = (int) (index * 1.5 / mMaxVolume * 100);
        //  String s = i + "%";
        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        int vol = index * 100 / mAudioMax;
        int drawableId;
        if (vol == 0) {
            drawableId = R.mipmap.ic_volume_mute_white_36dp;
        } else {
            drawableId = R.mipmap.ic_volume_up_white_36dp;
        }

        setVolumeOrBrightnessInfo(getContext().getString(R.string.volume_changing, vol), drawableId);


//        exo_video_audio_brightness_layout.setVisibility(View.VISIBLE);
//        exo_video_audio_brightness_pro.setMax(mMaxVolume);
//        exo_video_audio_brightness_pro.setProgress(index);
//        exo_video_audio_brightness_img.setImageResource(index == 0 ? R.drawable.ic_volume_off_white_48px : R.drawable.ic_volume_up_white_48px);
    }
    /**
     * 滑动改变亮度
     *
     * @param percent 值大小
     */
    private float brightness = -1;//亮度
    private synchronized void showBrightnessDialog(float percent) {
        Activity activity = (Activity) getContext();
        if (brightness < 0) {
            brightness = activity.getWindow().getAttributes().screenBrightness;
            if (brightness <= 0.00f) {
                brightness = 0.50f;
            } else if (brightness < 0.01f) {
                brightness = 0.01f;
            }
        }
        WindowManager.LayoutParams lpa = activity.getWindow().getAttributes();
        lpa.screenBrightness = brightness + percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }

        activity.getWindow().setAttributes(lpa);
//        if (!exo_video_audio_brightness_layout.isShown()) {
//            exo_video_audio_brightness_layout.setVisibility(View.VISIBLE);
//            exo_video_audio_brightness_pro.setMax(100);
//            exo_video_audio_brightness_img.setImageResource(R.drawable.ic_brightness_6_white_48px);
//        }
//        exo_video_audio_brightness_pro.setProgress((int) (lpa.screenBrightness * 100));


        int vol = (int) (lpa.screenBrightness* 100);
        int drawableId;
        if (vol == 0) {
            drawableId = R.mipmap.ic_volume_mute_white_36dp;
        } else {
            drawableId = R.mipmap.ic_volume_up_white_36dp;
        }

        setVolumeOrBrightnessInfo(getContext().getString(R.string.volume_changing, vol), drawableId);
    }
}
