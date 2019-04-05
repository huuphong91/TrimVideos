package com.example.trimvideo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.dinuscxj.progressbar.CircleProgressBar;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;

public class ProgressBarActivity extends AppCompatActivity {

    CircleProgressBar circleProgressBar;
    int duration;
    String[] command;
    String path;

    ServiceConnection mConnection;
    FFmpegService fFmpegService;
    Integer res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_bar);
        circleProgressBar = findViewById(R.id.circleProgressBar);
        circleProgressBar.setMax(100);
        Intent intent = getIntent();
        if (intent != null) {
            duration = intent.getIntExtra("duration", 0);
            command = intent.getStringArrayExtra("command");
            path = intent.getStringExtra("destination");

            final Intent intent1 = new Intent(ProgressBarActivity.this, FFmpegService.class);
            intent1.putExtra("duration", String.valueOf(duration));
            intent1.putExtra("command", command);
            intent1.putExtra("destination", path);
            startService(intent1);

            mConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    FFmpegService.LocalBinder binder = (FFmpegService.LocalBinder) service;
                    fFmpegService = binder.getServiceInstance();
                    fFmpegService.registerClient(getParent());

                    final Observer<Integer> resultObserver = new Observer<Integer>() {
                        @Override
                        public void onChanged(Integer integer) {
                            res = integer;
                            if (res < 100) {
                                circleProgressBar.setProgress(res);
                            }
                            if (res == 100) {
                                circleProgressBar.setProgress(res);
                                stopService(intent1);
                                Toast.makeText(getApplicationContext(), "Video trimmed success", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    };

                    fFmpegService.getPercentage().observe(ProgressBarActivity.this, resultObserver);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };

            bindService(intent1, mConnection, Context.BIND_AUTO_CREATE);

        }
    }
}
