package com.luoteng.voicerecord.utils;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * @author CentMeng csdn@vip.163.com on 16/7/5.
 * 录音相关，关键文件
 */
public class VoiceRecorder {

    MediaRecorder recorder;
    static final String EXTENSION = ".amr";
    private boolean isRecording = false;
    private long startTime;
    private String voiceFileName = null;
    private File file;
    private Handler handler;
    private Context context;


    public VoiceRecorder(Context context, Handler var1) {
        this.context = context;
        this.handler = var1;
    }

    public String startRecording(String name) {
        this.file = null;

        try {
            if (this.recorder != null) {
                this.recorder.release();
                this.recorder = null;
            }

            this.recorder = new MediaRecorder();
            this.recorder.setAudioSource(1);
            this.recorder.setOutputFormat(3);
            this.recorder.setAudioEncoder(1);
            this.recorder.setAudioChannels(1);
            this.recorder.setAudioSamplingRate(8000);
            this.recorder.setAudioEncodingBitRate(64);
            this.voiceFileName = this.getVoiceFileName(name);
            this.file = new File(FileUtils.getDiskFileDir(context, "", Environment.DIRECTORY_ALARMS).getPath()+File.separator+voiceFileName);
            this.recorder.setOutputFile(this.file.getAbsolutePath());
            this.recorder.prepare();
            this.isRecording = true;
            this.recorder.start();
        } catch (IOException var5) {
            Log.e("voice", "prepare() failed");
        }

        (new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        if (VoiceRecorder.this.isRecording) {
                            Message var1 = new Message();
                            var1.what = VoiceRecorder.this.recorder.getMaxAmplitude() * 12 / 32767;
                            VoiceRecorder.this.handler.sendMessage(var1);
                            SystemClock.sleep(100L);
                            continue;
                        }
                    } catch (Exception var2) {
                        Log.e("voice", var2.toString());
                    }

                    return;
                }
            }
        })).start();
        this.startTime = (new Date()).getTime();
        Log.d("voice", "start voice recording to file:" + this.file.getAbsolutePath());
        return this.file == null ? null : this.file.getAbsolutePath();
    }

    public void discardRecording() {
        if (this.recorder != null) {
            try {
                this.recorder.stop();
                this.recorder.release();
                this.recorder = null;
                if (this.file != null && this.file.exists() && !this.file.isDirectory()) {
                    this.file.delete();
                }
            } catch (IllegalStateException var2) {
                ;
            } catch (RuntimeException var3) {
                ;
            }

            this.isRecording = false;
        }

    }

    public int stopRecoding() {
        if (this.recorder != null) {
            this.isRecording = false;
            this.recorder.stop();
            this.recorder.release();
            this.recorder = null;
            if (this.file != null && this.file.exists() && this.file.isFile()) {
                if (this.file.length() == 0L) {
                    this.file.delete();
                    return -1011;
                } else {
                    int var1 = (int) ((new Date()).getTime() - this.startTime) / 1000;
                    Log.d("voice", "voice recording finished. seconds:" + var1 + " file length:" + this.file.length());
                    return var1;
                }
            } else {
                return -1011;
            }
        } else {
            return 0;
        }
    }

    protected void finalize() throws Throwable {
        super.finalize();
        if (this.recorder != null) {
            this.recorder.release();
        }

    }

    public String getVoiceFileName(String var1) {
//        Time var2 = new Time();
//        var2.setToNow();
//        return var1 + var2.toString().substring(0, 15) + EXTENSION;
          return var1 + EXTENSION;
    }

    public boolean isRecording() {
        return this.isRecording;
    }

    public File getFile() {
        return file;
    }

}
