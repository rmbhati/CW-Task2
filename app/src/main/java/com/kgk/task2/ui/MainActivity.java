package com.kgk.task2.ui;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.core.content.ContextCompat;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.downloader.Status;
import com.kgk.task2.R;
import com.kgk.task2.base.BaseActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class MainActivity extends BaseActivity {
    private String dirPath;
    private Integer downloadId = 0;

    private Button buttonOne, buttonCancel;
    private ProgressBar progressBar;
    private TextView textViewProgress;

    @Override
    public void initView() {
        setContentView(R.layout.activity_main);

        dirPath = getRootDirPath(getApplicationContext());
        //dirPath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

        Log.e("path", dirPath.toString());
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

            downloadId = PRDownloader.download(URL, dirPath, "Task2.bin")
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
                            try {
                                if (hashFile(new File(dirPath + "/Task2.bin")).equals("20492a4d0d84f8beb1767f6616229f85d44c2827b64bdbfb260ee12fa1109e0e")) {
                                    Toast.makeText(MainActivity.this, "SHA256 Matched: " + hashFile(new File(dirPath + "/Task2.bin")), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "SHA256 Not Matched: " + hashFile(new File(dirPath + "/Task2.bin")), Toast.LENGTH_SHORT).show();
                                }
                            } catch (IOException | NoSuchAlgorithmException e) {
                                Log.e("SHA", e.toString());
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Error error) {
                            if(error.isConnectionError()){
                                Toast.makeText(MainActivity.this, "isConnectionError", Toast.LENGTH_SHORT).show();
                            }else  if(error.isServerError()){
                                Toast.makeText(MainActivity.this, "isServerError", Toast.LENGTH_SHORT).show();
                            }
                            buttonOne.setText("Resume");
                            progressBar.setIndeterminate(false);
                            buttonOne.setEnabled(true);

                            /*buttonOne.setText("Start");
                            Toast.makeText(getApplicationContext(), "Error Occurred " + "1", Toast.LENGTH_SHORT).show();
                            textViewProgress.setText("");
                            progressBar.setProgress(0);
                            downloadId = 0;
                            buttonCancel.setEnabled(false);
                            progressBar.setIndeterminate(false);
                            buttonOne.setEnabled(true);*/
                        }
                    });
        });

        buttonCancel.setOnClickListener(view -> PRDownloader.cancel(downloadId));
    }

    private String hashFile(File file) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        FileInputStream fis = new FileInputStream(file);
        byte[] dataBytes = new byte[1024];

        int nread = 0;
        while ((nread = fis.read(dataBytes)) != -1) {
            md.update(dataBytes, 0, nread);
        }

        byte[] mdbytes = md.digest();

        StringBuilder sb = new StringBuilder();
        for (byte mdbyte : mdbytes) {
            sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    public static String getRootDirPath(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File file = ContextCompat.getExternalFilesDirs(context.getApplicationContext(), null)[0];
            return file.getAbsolutePath();
        } else {
            return context.getApplicationContext().getFilesDir().getAbsolutePath();
        }
    }

    private String getProgressDisplayLine(long currentBytes, long totalBytes) {
        return getBytesToMBString(currentBytes) + "/" + getBytesToMBString(totalBytes);
    }

    private String getBytesToMBString(long bytes) {
        return String.format(Locale.ENGLISH, "%.2fMb", bytes / (1024.00 * 1024.00));
    }
}
