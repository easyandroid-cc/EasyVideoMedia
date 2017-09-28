package com.devbrackets.android.exomediademo.ui.activity.view;

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
import com.devbrackets.android.exomediademo.ui.activity.EasyVideoControlsMobile;

/**
 * 音量和屏幕亮度的控制器
 */
public class VideoSlideController extends FrameLayout implements View.OnTouchListener {
    private FrameLayout centerContentWrapper;
    private TextView centerInfo;

    EasyVideoControlsMobile videoControls;

    EasyVideoControlsMobile.VideoControllerChangedListener videoControllerChangedListener;

    GestureDetector gestureDetector;

    private int mSurfaceYDisplayRange;

    //Volume
    private AudioManager mAudioManager;
    private int mAudioMax;
    private int mVol = -1;
    /**
     * 滑动改变亮度
     *
     * @param percent 值大小
     */
    private float brightness = -1;//亮度

    private boolean toSeek;//是否是条进度

    public void setOnSeekBarChangeListener(EasyVideoControlsMobile.VideoControllerChangedListener videoControllerChangedListener) {
        this.videoControllerChangedListener = videoControllerChangedListener;
    }

    public void setVideoControls(EasyVideoControlsMobile videoControls) {
        this.videoControls = videoControls;
    }

    public VideoSlideController(@NonNull Context context) {
        super(context);
        setup(context);
    }

    public VideoSlideController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    public VideoSlideController(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoSlideController(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup(context);
    }

    protected void setup(Context context) {
        View.inflate(context, R.layout.video_control_view, this);
        centerInfo = (TextView) findViewById(R.id.centerInfo);
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

        centerContentWrapper.setOnTouchListener(this);

        mAudioManager = (AudioManager) getContext().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        gestureDetector = new GestureDetector(context, new PlayerGestureListener(this));
    }


    private void hideCenterInfo() {
        centerInfo.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
        centerInfo.setVisibility(GONE);
    }


    private void setFastForwardOrRewind(long changingTime, @DrawableRes int drawableId) {
        centerInfo.setVisibility(VISIBLE);
        centerInfo.setText(generateFastForwardOrRewindTxt(changingTime));
        centerInfo.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), drawableId), null, null);
    }

    private CharSequence generateFastForwardOrRewindTxt(long changingTime) {
        long duration = videoControls == null ? 0 : videoControls.getDuration();
        String result = TimeFormatUtil.formatMs(changingTime) + " / " + TimeFormatUtil.formatMs(duration);
        int index = result.indexOf("/");
        SpannableString spannableString = new SpannableString(result);
        TypedValue typedValue = new TypedValue();
        TypedArray a = getContext().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorAccent});
        int color = a.getColor(0, 0);
        a.recycle();
        spannableString.setSpan(new ForegroundColorSpan(color), 0, index, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        return spannableString;
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

    private void setImageInfo(String txt, @DrawableRes int drawableId) {
        centerInfo.setVisibility(VISIBLE);
        centerInfo.setText(txt);
        centerInfo.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
        centerInfo.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), drawableId), null, null);
    }


    private boolean isToSeek() {
        return toSeek;
    }

    public void setToSeek(boolean toSeek) {
        this.toSeek = toSeek;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }
        if (MotionEvent.ACTION_UP == event.getAction()) {
            if (isToSeek()) {
                videoControllerChangedListener.onStopTrackingTouch();
                setToSeek(false);
            }
            if (centerInfo.getVisibility() == View.VISIBLE) {
                hideCenterInfo();
            }
            mVol = -1;
            brightness = -1f;
        }
        return false;
    }

    /****
     * 手势监听类
     *****/
    private class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {
        private boolean firstTouch;
        private boolean volumeControl;
        private VideoSlideController videoSlideController;

        public PlayerGestureListener(VideoSlideController videoSlideController) {
            this.videoSlideController = videoSlideController;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            firstTouch = true;
            return super.onDown(e);
        }

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
                videoSlideController.setToSeek(Math.abs(distanceX) >= Math.abs(distanceY));//是否是快进
                volumeControl = mOldX > mSurfaceYDisplayRange * 0.5f;
                firstTouch = false;
                if (videoSlideController.isToSeek()) {
                    videoControllerChangedListener.onStartTrackingTouch();
                }
            }
            if (videoSlideController.isToSeek()) {
                deltaX = -deltaX;
                long position = videoControls.getCurrentPosition();
                long duration = videoControls.getDuration();
                long newPosition = Math.max(0, Math.min((int) (position + deltaX * duration / (3 * mSurfaceYDisplayRange)), duration));
                if (newPosition >= 0) {
                    if (videoControls != null) {
                        videoControllerChangedListener.onProgressChanged((int) newPosition);
                    }
                    setFastForwardOrRewind(newPosition, deltaX > 0 ? R.mipmap.ic_fast_forward_white_36dp : R.mipmap.ic_fast_rewind_white_36dp);
                    videoControls.setPosition(newPosition);
                }
            } else {
                float percent = deltaY / mSurfaceYDisplayRange;
                if (volumeControl) {
                    showVolume(percent);
                } else {
                    showBrightness(percent);
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    private void showVolume(float percent) {
        if (mVol == -1) {
            mVol = Math.max(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC), 0);
        }
        int index = (int) (percent * mAudioMax) + mVol;
        index = Math.max(0, Math.min(index, mAudioMax));
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        int vol = index * 100 / mAudioMax;
        int drawableId = vol == 0 ? R.mipmap.ic_volume_mute_white_36dp : R.mipmap.ic_volume_up_white_36dp;
        setImageInfo(getContext().getString(R.string.volume_changing, vol), drawableId);
    }


    private synchronized void showBrightness(float percent) {
        Activity activity = (Activity) getContext();
        if (brightness < 0) {
            float screenBrightness = activity.getWindow().getAttributes().screenBrightness;
            if (screenBrightness < 0) {
                try {
                    screenBrightness = Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS) / 255;
                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                }
            }
            brightness = Math.max(screenBrightness, 0.01f);
        }

        WindowManager.LayoutParams lpa = activity.getWindow().getAttributes();
        lpa.screenBrightness = Math.max(Math.min(brightness + percent, 1.0f), 0.01f);
        activity.getWindow().setAttributes(lpa);
        int vol = (int) (lpa.screenBrightness * 100);
        int drawableId = whichBrightnessImageToUse(vol);
        setImageInfo(getContext().getString(R.string.brightness_changing, vol), drawableId);
    }
}
