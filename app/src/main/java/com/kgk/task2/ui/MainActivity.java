package com.kgk.task2.ui;

import android.graphics.Color;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.downloader.Status;
import com.kgk.task2.R;
import com.kgk.task2.base.BaseActivity;

import java.util.Locale;

public class MainActivity extends BaseActivity {
    private String dirPath;
    private Integer downloadId=0;

    private Button buttonOne, buttonCancel;
    private ProgressBar progressBar;
    private TextView textViewProgress;

    @Override
    public void initView() {
        setContentView(R.layout.activity_main);
        dirPath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

        buttonOne = findViewById(R.id.buttonOne);
        buttonCancel = findViewById(R.id.buttonCancelOne);
        progressBar = findViewById(R.id.progressBarOne);
        textViewProgress = findViewById(R.id.textViewProgressOne);
    }

    @Override
    public void initData() {
        buttonOne.setOnClickListener(view -> {
            if (Status.RUNNING == PRDownloader.getStatus(downloadId)) {
                PRDownloader.pause(downloadId);
                return;
            }

            buttonOne.setEnabled(false);
            progressBar.setIndeterminate(true);
            progressBar.getIndeterminateDrawable().setColorFilter(Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN);

            if (Status.PAUSED == PRDownloader.getStatus(downloadId)) {
                PRDownloader.resume(downloadId);
                return;
            }

            String URL = "https://speed.hetzner.de/100MB.bin";

            downloadId = PRDownloader.download(URL, dirPath, "Task1.bin")
                    .build()
                    .setOnStartOrResumeListener(() -> {
                        progressBar.setIndeterminate(false);
                        buttonOne.setEnabled(true);
                        buttonOne.setText("Pause");
                        buttonCancel.setEnabled(true);
                    })
                    .setOnPauseListener(() -> buttonOne.setText("Resume"))
                    .setOnCancelListener(() -> {
                        buttonOne.setText("Start");
                        buttonCancel.setEnabled(false);
                        progressBar.setProgress(0);
                        textViewProgress.setText("");
                        downloadId = 0;
                        progressBar.setIndeterminate(false);
                    })
                    .setOnProgressListener(progress -> {
                        long progressPercent = progress.currentBytes * 100 / progress.totalBytes;
                        progressBar.setProgress((int) progressPercent);
                        textViewProgress.setText(getProgressDisplayLine(progress.currentBytes, progress.totalBytes));
                        progressBar.setIndeterminate(false);
                    })
                    .start(new OnDownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            buttonOne.setEnabled(false);
                            buttonCancel.setEnabled(false);
                            buttonOne.setText("Complete");
                        }

                        @Override
                        public void onError(Error error) {
                            buttonOne.setText("Start");
                            Toast.makeText(getApplicationContext(), "Error Occurred " + "1", Toast.LENGTH_SHORT).show();
                            textViewProgress.setText("");
                            progressBar.setProgress(0);
                            downloadId = 0;
                            buttonCancel.setEnabled(false);
                            progressBar.setIndeterminate(false);
                            buttonOne.setEnabled(true);
                        }
                    });
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PRDownloader.cancel(downloadId);
            }
        });
    }


    public static String getProgressDisplayLine(long currentBytes, long totalBytes) {
        return getBytesToMBString(currentBytes) + "/" + getBytesToMBString(totalBytes);
    }

    private static String getBytesToMBString(long bytes) {
        return String.format(Locale.ENGLISH, "%.2fMb", bytes / (1024.00 * 1024.00));
    }
}
