package com.example.trimvideo;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

public class FFmpegService extends Service {

    private static final String TAG = "FFMPEG" ;
    FFmpeg fFmpeg;
    int duration;

    String[] command;
    Callbacks activity;

    public MutableLiveData<Integer> percentage;
    IBinder myBinder = new LocalBinder();

    public FFmpegService() {
        super();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            duration = Integer.parseInt(intent.getStringExtra("duration"));
            command = intent.getStringArrayExtra("command");
            try {
                loadFFmpegBinary();
                execFFMpegCommand();
            } catch (FFmpegNotSupportedException e) {
                e.printStackTrace();
            } catch (FFmpegCommandAlreadyRunningException e) {
                e.printStackTrace();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void execFFMpegCommand() throws FFmpegCommandAlreadyRunningException {
        fFmpeg.execute(command,new ExecuteBinaryResponseHandler(){
            @Override
            public void onFailure(String message) {
                super.onFailure(message);
            }

            @Override
            public void onSuccess(String message) {
                super.onSuccess(message);
            }

            @Override
            public void onProgress(String message) {
                Log.d(TAG, message);
                String arr[];
                if (message.contains("time=")) {
                    arr = message.split("time=");
                    String yalo = arr[1];
                    String abikamha[] = yalo.split(":");
                    String yaenda[] = abikamha[2].split(" ");
                    String seconds = yaenda[0];
                    int hours = Integer.parseInt(abikamha[0]);
                    hours = hours * 3600;
                    int min = Integer.parseInt(abikamha[1]);
                    min = min * 60;
                    float sec = Float.valueOf(seconds);

                    float timeInSec = hours + min + sec;

                    percentage.setValue((int) ((timeInSec/duration)*100));


                }
            }

            @Override
            public void onFinish() {
               percentage.setValue(100);
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            loadFFmpegBinary();
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
        percentage = new MutableLiveData<>();
    }

    private void loadFFmpegBinary() throws FFmpegNotSupportedException {
        if (fFmpeg == null) {
            fFmpeg = FFmpeg.getInstance(this);
        }

            fFmpeg.loadBinary(new LoadBinaryResponseHandler(){
                @Override
                public void onFailure() {
                    super.onFailure();
                }

                @Override
                public void onSuccess() {
                    super.onSuccess();
                }
            });
    }

    public class LocalBinder extends Binder {
        public FFmpegService getServiceInstance() {
            return FFmpegService.this;
        }
    }

    public void registerClient(Activity activity) {
        this.activity = (Callbacks)activity;
    }

    public MutableLiveData<Integer> getPercentage() {
        return percentage;
    }

    public interface Callbacks {
        void updateClient(float data);
    }
}
