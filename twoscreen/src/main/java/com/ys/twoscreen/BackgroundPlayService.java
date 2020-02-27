package com.ys.twoscreen;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class BackgroundPlayService extends Service {
    // 外接HDMI显示的视频路径。
    public final static String HDMI_PATH = Environment.getExternalStorageDirectory().getPath() + "/hdmi";
            //"/mnt/external_sd/hdmi";  Environment.getExternalStorageDirectory().getPath() + "/Download/hdmi" ;
    private static final String TAG = "---MainActivity---";
    private List<String> hdmiVideoPath = new ArrayList<>();
    private MediaPlayer mBackgroundPlayer;

    private SurfaceView presentSurface;
    private MyPresentation myPresentation;
    private Handler handler;

    private DisplayManager mDisplayManager;
    private SharedPreferences sharedPreferences;

    private PopupWindow videoControlPop;

    private boolean isPause = false;

    private boolean isVideoControlShowing = false;
    private SeekBar videoProgress;
    private Timer timer;

    LinearLayout presentationLayout;

    private int nowHdmiPosition;
    private final static String SERVICE_ACTION = "com.ryx.twovideos.actions";

    private ImageView airPlay, playPre, playNext, playOrPause, controlSwitch;

    private final static int HIDE_POP = 0x111;
    private final static int HIDE_POP_TIME = 10 * 1000;
    MsgReceiver receiver;

    WindowManager mWindowManager;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        sharedPreferences = getSharedPreferences("play_time", 0);
        updateContents();
        registMyReceiver();
        initViewSurface();
        mDisplayManager.registerDisplayListener(mDisplayListener, null);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initViewSurface() {
        mBackgroundPlayer = new MediaPlayer();
        mBackgroundPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
        // AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        // 设置成静音。
        // audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
//        mBackgroundPlayer.setVolume(0.5f, 0.5f);
        if (!new File(HDMI_PATH).exists()) {
            Toast.makeText(this,"请在内置存储根目录放置hdmi文件夹和视频", Toast.LENGTH_LONG).show();
            return;
        }
        hdmiVideoPath = FileUtils.getPaths(HDMI_PATH);
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        // 获取LayoutParams对象
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        // wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        wmParams.gravity = Gravity.RIGHT | Gravity.TOP;
        wmParams.x = 0;
        wmParams.y = 0;
        // 宽度和高度为0, 可以避免主屏退出后出现的主屏触摸和点击问题。
        wmParams.width = 200;
        wmParams.height = 200;
        LayoutInflater inflater = LayoutInflater.from(this);
        presentationLayout = (LinearLayout) inflater.inflate(R.layout.presentation_layout, null);
        presentationLayout.setFocusable(false);
        presentationLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("4444444", "x=====" + event.getX() + " y============" + event.getY());
                // showBottomPop();
                return false;
            }
        });
        mWindowManager.addView(presentationLayout, wmParams);
        handler.postDelayed(playVedio,500);

    }

    private Runnable playVedio = new Runnable() {
        @Override
        public void run() {
            playVideo(mBackgroundPlayer, nowHdmiPosition, true);
        }
    };

    private void registMyReceiver() {
        receiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SERVICE_ACTION);
        registerReceiver(receiver, intentFilter);
        Log.d(TAG, "registMyReceiver");
    }

    //  接收activity 发送过来的广播，来作相应的播放处理。目前就做了一个切换功能。可以加上下一曲和暂停等一些功能。
    class MsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int i = intent.getIntExtra("receiver_key", -1);
            Log.d(TAG, "com.ryx.twovideos.actions:" + i);

            if (i == 0) {
                if (myPresentation != null) {
                    myPresentation.dismiss();
                }
                unregisterReceiver(receiver);
                stopSelf();
                mBackgroundPlayer.setVolume(0,0);
            }
            // 上一曲
            if (i == 1) {
                nowHdmiPosition--;
                if (nowHdmiPosition < 0) {
                    nowHdmiPosition = hdmiVideoPath.size() - 1;
                }
                playVideo(mBackgroundPlayer, nowHdmiPosition, false);
            }

            // 下一曲
            if (i == 2) {
                nowHdmiPosition++;
                if (nowHdmiPosition >= hdmiVideoPath.size()) {
                    nowHdmiPosition = 0;
                }
                playVideo(mBackgroundPlayer, nowHdmiPosition, false);
            }

            // 播放暂停
            if (i == 3) {
                if (!isPause) {
                    isPause = true;
                    mBackgroundPlayer.pause();
                } else {
                    isPause = false;
                    mBackgroundPlayer.start();
                }
            }

        }
    }

    // 获取显示设备。
    public void updateContents() {
        mDisplayManager = (DisplayManager) getSystemService(
                Context.DISPLAY_SERVICE);
        Display[] displays = mDisplayManager.getDisplays();
        Log.d(TAG, "length:::" + displays.length);
        if (displays.length == 2) {
            showPresentation(displays[1]);
        }
    }


    // 将内容显示到display上面。
    private void showPresentation(Display display) {
        if (myPresentation == null) {
            myPresentation = new MyPresentation(this, display);
            Log.d(TAG,"myPresentation == null");
        }

        myPresentation.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // 监听消失，保存当前播放位置。
                sharedPreferences.edit().putInt("index", nowHdmiPosition).commit();
                sharedPreferences.edit().putInt("position", mBackgroundPlayer.getCurrentPosition()).commit();
            }
        });
        myPresentation.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
        myPresentation.show();
        presentSurface = myPresentation.getSurface();
        presentSurface.setFocusable(false);
        presentSurface.setMinimumWidth(500);
        presentSurface.setMinimumHeight(500);

        presentSurface.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "x=====" + event.getX() + " y============" + event.getY());
//                showBottomPop();
                return false;
            }
        });

        presentSurface.getHolder().addCallback(new MySurfaceCallback());
    }

    // 显示Pop
    private void showBottomPop() {

        LinearLayout controlLay = (LinearLayout) presentationLayout.findViewById(R.id.media_control_layout);
        controlLay.setVisibility(View.VISIBLE);
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        // 获取LayoutParams对象
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        mHandler.sendEmptyMessageDelayed(HIDE_POP, HIDE_POP_TIME);
        isVideoControlShowing = true;
        //mainSurface.setZOrderOnTop(false);
        LayoutInflater inflater = LayoutInflater.from(this);
        View bottomPopView = inflater.inflate(R.layout.video_control_layout, null);
        videoProgress = (SeekBar) bottomPopView.findViewById(R.id.video_progress);
        videoProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mBackgroundPlayer.seekTo(seekBar.getProgress() * mBackgroundPlayer.getDuration() / 100);
            }
        });

        airPlay = (ImageView) bottomPopView.findViewById(R.id.air_play);
        airPlay.setOnClickListener(new MyViewClickListener());

        playPre = (ImageView) bottomPopView.findViewById(R.id.play_pre);
        // playPre.setOnClickListener(new MyViewOnClickListener());

        playNext = (ImageView) bottomPopView.findViewById(R.id.play_next);
        //  playNext.setOnClickListener(new MyViewOnClickListener());

        playOrPause = (ImageView) bottomPopView.findViewById(R.id.play_or_pause);
        //  playOrPause.setOnClickListener(new MyViewOnClickListener());

        controlSwitch = (ImageView) bottomPopView.findViewById(R.id.control_switch);
        // if (isControlFirstScreen) {
        //     controlSwitch.setImageDrawable(getResources().getDrawable(R.drawable.control_1));
        // } else {
        //      controlSwitch.setImageDrawable(getResources().getDrawable(R.drawable.control_2));
        //  }
        // controlSwitch.setOnClickListener(new MyViewOnClickListener());

        //  videoControlPop = new PopupWindow(bottomPopView, WindowManager.LayoutParams.MATCH_PARENT,
        //       android.view.WindowManager.LayoutParams.WRAP_CONTENT);
        //  popupWindow.setAnimationStyle(R.style.popwin_anim_style);
        // videoControlPop.showAtLocation(bottomPopView, Gravity.BOTTOM, 0, 0);
        //   videoControlPop.setOnDismissListener(new PopupWindow.OnDismissListener() {
        //     @Override
        //     public void onDismiss() {
        //         isVideoControlShowing = false;
        //    }
        // });

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // videoProgress.setProgress(screen1Player.getCurrentPosition() * 100 / screen1Player.getDuration());
                //  currentVideoTime = screen1Player.getCurrentPosition();
            }
        }, 0, 1000);
        // mWindowManager.addView(presentationLayout, wmParams);
        wmParams.gravity = Gravity.BOTTOM;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        mWindowManager.addView(bottomPopView, wmParams);
    }


    class MyViewClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Toast.makeText(BackgroundPlayService.this, "34235353", Toast.LENGTH_LONG).show();
        }
    }


    // 统一的播放界面。
    private void playVideo(MediaPlayer mediaPlayer, int pathIndex, boolean isFirstPlay) {

        // 首次播放从上次记录位置开始。
        if (isFirstPlay) {
            mediaPlayer.setOnCompletionListener(new MyVideoFinishListener());
            try {
                nowHdmiPosition = sharedPreferences.getInt("index", 0);
                mediaPlayer.setDataSource(hdmiVideoPath.get(sharedPreferences.getInt("index", 0)));
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                //  拔hdmi会出现异常。
            }
            mediaPlayer.start();
            mediaPlayer.seekTo(sharedPreferences.getInt("position", 0));

        } else {
            mediaPlayer.reset();
            mediaPlayer.setOnCompletionListener(new MyVideoFinishListener());
            try {
                mediaPlayer.setDataSource(hdmiVideoPath.get(pathIndex));
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                //  拔hdmi会出现异常。
            }
            mediaPlayer.start();
        }
    }

    // 播放结束监听。
    class MyVideoFinishListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            nowHdmiPosition++;
            if (nowHdmiPosition >= hdmiVideoPath.size()) {
                nowHdmiPosition = 0;
            }
            playVideo(mp, nowHdmiPosition, false);
        }
    }

    // surface回调。
    class MySurfaceCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // SurfaceHolder是SurfaceView的控制接口
            // 对mediaplayer设定显示的surfaceView.
            mBackgroundPlayer.setDisplay(presentSurface.getHolder());
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBackgroundPlayer.release();
    }

    private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        public void onDisplayAdded(int displayId) {
//             mHasTwoDispalys = true;
//            updateContents();
            Log.d(TAG, "onDisplayAdded");

        }

        public void onDisplayChanged(int displayId) {
//            updateContents();
            Log.d(TAG, "onDisplayChanged");
        }

        public void onDisplayRemoved(int displayId) {
            // mHasTwoDispalys = true;
            Log.d(TAG, "onDisplayRemoved");
            mDisplayManager = null;
            // updateContents();
        }
    };
}
