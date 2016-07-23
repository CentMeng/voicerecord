/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.luoteng.voicerecord.utils;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.luoteng.voicerecord.MainActivity;
import com.luoteng.voicerecord.R;

import java.io.File;

/**
 * 录音播放文件
 */
public class VoicePlayClickListener implements View.OnClickListener {
    private static final String TAG = "VoicePlayClickListener";
    MediaEntity voiceBody;
    ImageView voiceIconView;

    private AnimationDrawable voiceAnimation = null;
    MediaPlayer mediaPlayer = null;
    TextView tv_read_time;
    MainActivity activity;
    private BaseAdapter adapter;

    public static boolean isPlaying = false;
    public static VoicePlayClickListener currentPlayListener = null;

    //播放语音时候的动态动画，没有传0
    private int playDrawableAnimID;

    private int preDrawableID;

    private ActionListener actionListener;

//    private PlayingThread playingThread;

    /**
     * @param voiceBody
     * @param v                  动态图展示位
     * @param tv_read_time       时长展示
     * @param adapter            如果adapter需要操作就传，不需要传null
     * @param activity
     * @param playDrawableAnimID 播放语音时候的动态动画，没有传0
     * @param preDrawableID      播放语音前的图片，没有传0
     * @param actionListener     语音播放开始和结束后需要的操作
     */
    public VoicePlayClickListener(MediaEntity voiceBody, ImageView v, TextView tv_read_time, BaseAdapter adapter, MainActivity activity, int playDrawableAnimID, int preDrawableID, ActionListener actionListener) {
        this.voiceBody = voiceBody;
        this.tv_read_time = tv_read_time;
        this.adapter = adapter;
        voiceIconView = v;
        this.activity = activity;
        this.playDrawableAnimID = playDrawableAnimID;
        this.preDrawableID = preDrawableID;
        this.actionListener = actionListener;
    }

    /**
     * 用于刚录制完的音频播放
     * @param activity
     * @param actionListener
     */
    public VoicePlayClickListener(MainActivity activity, ActionListener actionListener) {
        this.activity = activity;
        this.actionListener = actionListener;
        this.playDrawableAnimID = 0;
        this.preDrawableID = 0;
    }


    public void stopPlayVoice() {
        if (playDrawableAnimID != 0) {
            voiceAnimation.stop();
        }
        if (preDrawableID != 0) {
            voiceIconView.setImageResource(preDrawableID);
        }
        // stop play voice
        if (mediaPlayer != null) {
//            在调用start()后马上调用stop(),时由于没有生成有效的音频或是视频数据。
//            解决方法：让线程睡眠一定的时间，在测试后发现1秒几乎是最短时间。
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException e) {
                // 如果当前java状态和jni里面的状态不一致，
                //e.printStackTrace();
                mediaPlayer = null;
                mediaPlayer = new MediaPlayer();
                mediaPlayer.stop();
            }
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        isPlaying = false;
        if (activity != null) {
            ((MainActivity) activity).playMsgId = null;
        }
        if (actionListener != null) {
            actionListener.stopVoice();
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
//        if (tv_read_time != null && voiceBody != null && voiceBody.getLength() > 0 && playingThread != null) {
//            playingThread.isFinish = true;
//            playingThread = null;
//            System.gc();
//            tv_read_time.setText("" + voiceBody.getLength() + "\'\'");
//        }

    }

    public void playVoice(String filePath) {
        if (!(new File(filePath).exists())) {
            return;
        }
        if (activity != null && voiceBody != null && !TextUtils.isEmpty(voiceBody.getEntityId())) {
            ((MainActivity) activity).playMsgId = voiceBody.getEntityId();
        }

        AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);

        mediaPlayer = new MediaPlayer();
//        if (getSettingMsgSpeaker()) {
        //扬声器播放
//            audioManager.setMode(AudioManager.MODE_NORMAL);
//            audioManager.setSpeakerphoneOn(true);
//            mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
//        } else {
        //听筒播放
            audioManager.setSpeakerphoneOn(false);// 关闭扬声器
            // 把声音设定成Earpiece（听筒）出来，设定为正在通话中
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
//        }
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                    mediaPlayer.release();
                    mediaPlayer = null;
                    stopPlayVoice(); // stop animation
                }

            });
            isPlaying = true;
            currentPlayListener = this;
            mediaPlayer.start();
            showAnimation();
            if (actionListener != null) {
                actionListener.startVoice();
            }
//            if (tv_read_time != null && voiceBody != null && voiceBody.getLength() > 0) {
//                playingThread = new PlayingThread(voiceBody.getLength());
//                playingThread.start();
//            }

        } catch (Exception e) {
        }
    }

    // 开始播放动画
    private void showAnimation() {
        // play voice, and start animation
        if (playDrawableAnimID != 0) {
            voiceIconView.setImageResource(playDrawableAnimID);
            voiceAnimation = (AnimationDrawable) voiceIconView.getDrawable();
            voiceAnimation.start();
        }

    }

    @Override
    public void onClick(View v) {

        String st = activity.getResources().getString(R.string.Is_download_voice_click_later);
        if (isPlaying) {
            if (((MainActivity) activity).playMsgId != null && ((MainActivity) activity).playMsgId.equals(voiceBody.getEntityId())) {
                currentPlayListener.stopPlayVoice();
                return;
            }
            currentPlayListener.stopPlayVoice();
        } else {
            if (activity != null && activity instanceof MainActivity) {
                ((MainActivity) activity).playMsgId = voiceBody.getEntityId();
            }
        }

        if (!TextUtils.isEmpty(voiceBody.getPath()) && voiceBody.getTransDownloadStatus() == DownloadStatus.LOADED) {
            // for sent msg, we will try to play the voice file directly
            playVoice(voiceBody.getPath());
        } else {
            if (!TextUtils.isEmpty(voiceBody.getUrl())) {
                //getPath在初始化MediaEntity就要加入进去，做了文件是否存在的判断，所以不为空也不影响判定
                if (voiceBody.getTransDownloadStatus() == DownloadStatus.PREPARE || voiceBody.getTransDownloadStatus() == DownloadStatus.FAILED) {
                    final DownloadTask downloadTask = new DownloadTask(voiceBody.getUrl(), 3, voiceBody.getAbsolutePath(), new DownloadTask.DownloadListener() {
                        @Override
                        public void downloadingFile(int process) {
                            if (process != 0) {
                                voiceBody.setTransDownloadStatus(DownloadStatus.LOADING);
                                //数据库更改状态
                                DbUtils.updateMediaEntity(voiceBody);
                            }
                        }

                        @Override
                        public void downloadedFile(String url, final String path) {
                            voiceBody.setTransDownloadStatus(DownloadStatus.LOADED);
                            if (!isPlaying) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        playVoice(path);
                                    }
                                });
                            }
                            //数据库信息更改状态
                            DbUtils.updateMediaEntity(voiceBody);
                        }

                        @Override
                        public void downloadedFail(String url, String path) {
                            voiceBody.setTransDownloadStatus(DownloadStatus.FAILED);
                            //数据库信息更改状态
                            DbUtils.updateMediaEntity(voiceBody);
                        }
                    }, activity);
                    downloadTask.start();
                } else {
                    Toast.makeText(activity, st, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(activity, activity.getString(R.string.geterror), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 播放倒计时线程，暂时先不用，多个文件会出问题
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
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_read_time.setText(String.format("%1$d\'\'", count));
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


    public interface ActionListener {

        public void stopVoice();

        public void startVoice();
    }
}