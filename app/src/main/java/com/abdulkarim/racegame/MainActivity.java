package com.abdulkarim.racegame;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

    ProgressBar track1, track2;
    Button btnPlayer1, btnPlayer2, btnStart;
    TextView resultText;
    boolean gameRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        track1 = findViewById(R.id.track1);
        track2 = findViewById(R.id.track2);
        btnPlayer1 = findViewById(R.id.btnPlayer1);
        btnPlayer2 = findViewById(R.id.btnPlayer2);
        btnStart = findViewById(R.id.btnStart);
        resultText = findViewById(R.id.resultText);
    }

    public void onStartGame(View v) {
        track1.setProgress(0);
        track2.setProgress(0);
        resultText.setText("");
        gameRunning = true;
        btnPlayer1.setEnabled(true);
        btnPlayer2.setEnabled(true);
        btnStart.setEnabled(false);
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
