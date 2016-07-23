package com.luoteng.voicerecord;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.luoteng.voicerecord.utils.DownloadStatus;
import com.luoteng.voicerecord.utils.FileUtils;
import com.luoteng.voicerecord.utils.MediaEntity;
import com.luoteng.voicerecord.utils.MediaUtils;
import com.luoteng.voicerecord.utils.MemorySpaceUtils;
import com.luoteng.voicerecord.utils.OnClickUtil;
import com.luoteng.voicerecord.utils.RecordStatus;
import com.luoteng.voicerecord.utils.VoicePlayClickListener;
import com.luoteng.voicerecord.utils.VoiceRecorder;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.ViewById;
import org.litepal.tablemanager.Connector;

import java.util.UUID;

/**
 * 录音和播放语音实现
 * 存储使用litepal数据库
 * 下载文件采用单文件多线程下载方式
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @ViewById
    TextView tv_time;

    @ViewById
    TextView tv_status;

    @ViewById
    TextView tv_replay;

    private PowerManager.WakeLock wakeLock;

    //录音关键类
    private VoiceRecorder voiceRecorder;

    //根据录音声音大小显示的图片
    private Drawable[] micImages;

    @ViewById(R.id.mic_image_bg)
    ImageView mic_image_bg;

    @ViewById(R.id.mic_image)
    ImageView micImage;

    private MediaEntity mediaEntity;

    //
    VoicePlayClickListener voicePlay;

    /**
     * 录音 启动线程开关，开始录音，录音时候点击返回和回退则recycle录音相关，点击确认发送提示录音中
     * 停止录音 关闭线程开关，停止录音
     * 重新录音，删除之前录音的文件之后，重新开始录音
     */
    private RecordStatus recordStatus = RecordStatus.BEFORE;

    private RecordingThread recordingThread;

    private AnimationDrawable voiceAnimation = null;

    public String playMsgId;

    private PlayingThread playingThread;


    @ViewById
    ImageView imv_listen;


    private final int FAST_CLICK_TIME = 200;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recordStatus == RecordStatus.RECORDING) {
            //正在录音状态则停止所有释放资源
            recycleRecord();
        }
        if (recordStatus == RecordStatus.PRE_PLAYING) {
            //正在播放状态则stop
            refreshRecordStatus(RecordStatus.STOP_PREPLAYING);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (recordStatus == RecordStatus.RECORDING) {
            //正在录音状态则stop录音
            refreshRecordStatus(RecordStatus.RECORDED);
        }
        if (recordStatus == RecordStatus.PRE_PLAYING) {
            //正在播放状态则stop
            refreshRecordStatus(RecordStatus.STOP_PREPLAYING);
        }
    }

    @AfterViews
    void afterView() {
        /**
         * 创建数据库
         */
        SQLiteDatabase db = Connector.getDatabase();

        voiceRecorder = new VoiceRecorder(this, micImageHandler);
        // 动画资源文件,用于录制语音时
        micImages = new Drawable[]{getResources().getDrawable(R.mipmap.record_animate_01_out),
                getResources().getDrawable(R.mipmap.record_animate_02_out),
                getResources().getDrawable(R.mipmap.record_animate_03_out),
                getResources().getDrawable(R.mipmap.record_animate_04_out),
                getResources().getDrawable(R.mipmap.record_animate_05_out),
                getResources().getDrawable(R.mipmap.record_animate_06_out),
                getResources().getDrawable(R.mipmap.record_animate_07_out),
                getResources().getDrawable(R.mipmap.record_animate_08_out),
                getResources().getDrawable(R.mipmap.record_animate_09_out),
                getResources().getDrawable(R.mipmap.record_animate_10_out),
                getResources().getDrawable(R.mipmap.record_animate_11_out),
                getResources().getDrawable(R.mipmap.record_animate_12_out),
                getResources().getDrawable(R.mipmap.record_animate_13_out)
        };
        wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK, "demo");


        tv_replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recordStatus == RecordStatus.PRE_PLAYING) {
                    refreshRecordStatus(RecordStatus.STOP_PREPLAYING);
                }
                if (voiceRecorder.getFile() != null && !TextUtils.isEmpty(voiceRecorder.getFile().getPath())) {
                    if (FileUtils.deleteFile(voiceRecorder.getFile().getPath())) {
                        Log.e("文件输出", "删除文件" + voiceRecorder.getFile().getPath());
                    }

                }
                refreshRecordStatus(RecordStatus.RECORDING);
            }
        });

        mic_image_bg.setOnClickListener(this);

        micImage.setOnClickListener(this);


        MediaEntity mediaEntity = MediaUtils.getMediaEntity(this, "1", "msj", "http://7xiqby.com1.z0.glb.clouddn.com/flybed.mp3", 259);

        imv_listen.setOnClickListener(new VoicePlayClickListener(mediaEntity, imv_listen, null, null, MainActivity.this, R.drawable.yida_mediaplay_icon, R.mipmap.ic_found_ting_before, null));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mic_image:
            case R.id.mic_image_bg:
                switch (recordStatus) {
                    case BEFORE:
                        if (!OnClickUtil.isFastDoubleClick(FAST_CLICK_TIME)) {
                            refreshRecordStatus(RecordStatus.RECORDING);
                        }
                        break;
                    case RECORDING:
                        refreshRecordStatus(RecordStatus.RECORDED);
                        break;
                    case RECORDED:
                        if (!OnClickUtil.isFastDoubleClick(FAST_CLICK_TIME)) {
                            refreshRecordStatus(RecordStatus.PRE_PLAYING);
                        }
                        break;
                    case PRE_PLAYING:
                        //防止快点
                        if (!OnClickUtil.isFastDoubleClick(FAST_CLICK_TIME)) {
                            refreshRecordStatus(RecordStatus.STOP_PREPLAYING);
                        }
                        break;
                    case STOP_PREPLAYING:
                        refreshRecordStatus(RecordStatus.PRE_PLAYING);
                        break;
                }
                break;
        }
    }

    /**
     * 刷新录音状态
     *
     * @param recordStatus
     */
    private void refreshRecordStatus(final RecordStatus recordStatus) {
        this.recordStatus = recordStatus;
        tv_status.setText(recordStatus.getKey());

        if (voicePlay == null) {
            voicePlay = new VoicePlayClickListener(this, new VoicePlayClickListener.ActionListener() {
                @Override
                public void stopVoice() {
                    tv_time.setVisibility(View.VISIBLE);
                    if (mediaEntity != null) {
                        tv_time.setText("" + mediaEntity.getLength() + "\'\'");
                    }
                    tv_replay.setVisibility(View.VISIBLE);
                    mic_image_bg.setImageResource(R.mipmap.ic_recording_after);
                    MainActivity.this.recordStatus = RecordStatus.STOP_PREPLAYING;
                    tv_status.setText(RecordStatus.STOP_PREPLAYING.getKey());
                }

                @Override
                public void startVoice() {

                }
            });
        }

        switch (recordStatus) {
            case BEFORE:
                if (recordingThread != null) {
                    recordingThread.isFinish = true;
                } else {
                    recordingThread = new RecordingThread();
                }
                mediaEntity = null;
                tv_time.setVisibility(View.INVISIBLE);
                tv_time.setText("");
                tv_replay.setVisibility(View.INVISIBLE);
                stopAnimation();
                mic_image_bg.setImageResource(R.mipmap.ic_recording_before);
                break;
            case RECORDING:
                if (!startRecord()) {
                    refreshRecordStatus(RecordStatus.BEFORE);
                    break;
                }
                tv_time.setVisibility(View.VISIBLE);
                tv_time.setText("60\'\'");
                tv_replay.setVisibility(View.INVISIBLE);
                mic_image_bg.setImageResource(R.drawable.record_voice_icon);
                showAnimation();
                if (recordingThread != null) {
                    recordingThread.isFinish = true;
                }
                recordingThread = new RecordingThread();
                recordingThread.start();
                break;
            case RECORDED:
                if (!stopRecord()) {
                    if (voiceRecorder.getFile() != null && !TextUtils.isEmpty(voiceRecorder.getFile().getPath())) {
                        if (FileUtils.deleteFile(voiceRecorder.getFile().getPath())) {
                            Log.e("文件输出", "删除文件" + voiceRecorder.getFile().getPath());
                        }
                    }
                    refreshRecordStatus(RecordStatus.BEFORE);
                    break;
                }
                tv_time.setVisibility(View.VISIBLE);
                tv_replay.setVisibility(View.VISIBLE);
                stopAnimation();
                mic_image_bg.setImageResource(R.mipmap.ic_recording_after);
                if (recordingThread != null) {
                    recordingThread.isFinish = true;
                }
                if (mediaEntity == null) {
                    mediaEntity = new MediaEntity();
                }
                mediaEntity.setTransDownloadStatus(DownloadStatus.LOADED);
                mediaEntity.setPath(voiceRecorder.getFile().getPath());
                mediaEntity.setLength(Integer.parseInt(tv_time.getText().toString().replaceAll("\'\'", "")));
                mediaEntity.setStatus(RecordStatus.RECORDED);
                tv_time.setText("" + mediaEntity.getLength() + "\'\'");
                break;
            case PRE_PLAYING:
                //播放文件
                if (!TextUtils.isEmpty(voiceRecorder.getFile().getPath())) {
                    voicePlay.playVoice(voiceRecorder.getFile().getPath());
                } else {
                    refreshRecordStatus(RecordStatus.RECORDED);
                    break;
                }
                tv_time.setVisibility(View.VISIBLE);
                tv_time.setText("" + mediaEntity.getLength() + "\'\'");
                playingThread = new PlayingThread(mediaEntity.getLength());
                playingThread.start();
                tv_replay.setVisibility(View.VISIBLE);
                mic_image_bg.setImageResource(R.mipmap.ic_listen_in);
                micImage.setVisibility(View.GONE);
                break;
            case STOP_PREPLAYING:
                tv_time.setVisibility(View.VISIBLE);
                tv_replay.setVisibility(View.VISIBLE);
                mic_image_bg.setImageResource(R.mipmap.ic_recording_after);
                micImage.setVisibility(View.GONE);
                tv_time.setVisibility(View.VISIBLE);
                if (playingThread != null) {
                    playingThread.isFinish = true;
                }
                if (mediaEntity != null) {
                    tv_time.setText("" + mediaEntity.getLength() + "\'\'");
                }
                //停止播放文件
                voicePlay.stopPlayVoice();
                break;
        }
    }

    /**
     * 录音计时线程
     */
    class RecordingThread extends Thread {

        public boolean isFinish = false;

        @Override
        public void run() {
            super.run();
            for (int i = 0; i <= 60; i++) {
                if (!isFinish) {
                    final int count = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_time.setText(String.format("%1$d\'\'", count));
                            if (count == 60) {
                                refreshRecordStatus(RecordStatus.RECORDED);
                            }
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    i = 0;
                }
            }

        }

    }

    /**
     * 播放倒计时线程
     */
    class PlayingThread extends Thread {

        public int duration;

        public boolean isFinish = false;

        public PlayingThread(int duration) {
            this.duration = duration;
        }

        @Override
        public void run() {
            super.run();
            for (int i = duration; i >= 0; i--) {
                if (!isFinish) {
                    final int count = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_time.setText(String.format("%1$d\'\'", count));
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    i = duration;
                }
            }

        }

    }

    /**
     * 播放录音时候背景动画
     */
    private void showAnimation() {
        // record voice, and start animation
        mic_image_bg.setImageResource(R.drawable.record_voice_icon);
        micImage.setVisibility(View.VISIBLE);
        voiceAnimation = (AnimationDrawable) mic_image_bg.getDrawable();
        voiceAnimation.start();

    }

    /**
     * 暂停录音时候背景动画
     */
    private void stopAnimation() {
        micImage.setVisibility(View.GONE);
        if (voiceAnimation != null) {
            voiceAnimation.stop();
        }
    }

    /**
     * 录音中时候，关闭，回收录音
     */
    private void recycleRecord() {
        stopRecord();
        if (voiceRecorder.getFile() != null && !TextUtils.isEmpty(voiceRecorder.getFile().getPath())) {
            if (FileUtils.deleteFile(voiceRecorder.getFile().getPath())) {
                Log.e("文件操作", "删除文件" + voiceRecorder.getFile().getPath());
            }
        }
    }


    private android.os.Handler micImageHandler = new android.os.Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            // 切换msg切换图片
            micImage.setImageDrawable(micImages[msg.what]);
        }
    };

    /**
     * 开始录音
     *
     * @return 正常可以开始
     */
    private boolean startRecord() {
        if (!FileUtils.isExitsSdcard()) {
            String st4 = getResources().getString(R.string.Send_voice_need_sdcard_support);
            showSystemShortToast(st4);
            return false;
        }

        if (!MemorySpaceUtils.hasEnoughMemory(FileUtils.getDiskCachePath(this), 1 * 1024 * 1024)) {
            String st5 = getResources().getString(R.string.Send_voice_need_avaliablespace);
            showSystemShortToast(st5);
            return false;
        }

        try {
            wakeLock.acquire();
            if (VoicePlayClickListener.isPlaying)
                VoicePlayClickListener.currentPlayListener.stopPlayVoice();
            voiceRecorder.startRecording(UUID.randomUUID().toString());
        } catch (Exception e) {
            e.printStackTrace();
            if (wakeLock.isHeld())
                wakeLock.release();
            if (voiceRecorder != null)
                voiceRecorder.discardRecording();
            showSystemShortToast(this.getString(R.string.recoding_fail));
            return false;
        }
        return true;
    }

    /**
     * 停止录音
     *
     * @return 录音结束成功
     */
    private boolean stopRecord() {
        if (wakeLock.isHeld())
            wakeLock.release();
        String st1 = getResources().getString(R.string.Recording_without_permission);
        String st2 = getResources().getString(R.string.The_recording_time_is_too_short);
        String st3 = getResources().getString(R.string.send_failure_please);
        try {
            int length = voiceRecorder.stopRecoding();
            if (length > 0) {
                return true;
            } else if (length == -1011) {
                Toast.makeText(getApplicationContext(), st1, Toast.LENGTH_SHORT).show();
                return false;
            } else {
                Toast.makeText(getApplicationContext(), st2, Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showSystemShortToast(st3);
            return false;
        }
    }

    public void showSystemShortToast(String msg) {
        Toast.makeText(this.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 七牛上传录音
     *
     * @param filePath
     * @param name
     */
//    private void ascyUpload(final String filePath, String name) {
//        showLoadingDialog();
//        final String token = app.getAudioUploadToken();
//        try {
//            app.getUploadManager().put(filePath, name, token,
//                    new UpCompletionHandler() {
//                        @Override
//                        public void complete(String key, ResponseInfo info,
//                                             JSONObject res) {
//                            try {
//                                mediaEntity.setDownloadStatus(DownloadStatus.LOADED.name());
//                                String url = ApiSettings.URL_VIDEO_BASE + res.getString("key");
//                                if (mediaEntity != null) {
//                                    mediaEntity.setUrl(url);
//                                }
//                               imv_listen.setOnClickListener(new VoicePlayClickListener(entity,imv_listen,null,null,TestActivity.this,R.drawable.yida_mediaplay_icon,R.mipmap.ic_found_ting_before,null));
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                                cancelLoadingDialog();
//                                showSystemShortToast("发生了错误，请重试");
//                                getAudioToken();
//                            }
//                        }
//                    }, null);
//        } catch (Exception e) {
//            e.printStackTrace();
//            cancelLoadingDialog();
//            showSystemShortToast("发生了错误，请重试");
//        }
//
//    }
//
//    /**
//     * 获取上传录音token
//     */
//    private void getAudioToken() {
//        AudioTokenRequest request = new AudioTokenRequest();
//        request.setListener(new Response.Listener<com.core.api.event.response.Response>() {
//            @Override
//            public void onResponse(com.core.api.event.response.Response response) {
//                if (success(response)) {
//                    app.setAudioUploadToken((String) response.getParam());
//                }
//            }
//        });
//        request.setErrorlistener(getErrorListener());
//        volleyHttpClient.doNetTask(VolleyHttpClient.GET, request);
//    }
//
//
}
