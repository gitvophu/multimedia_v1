package com.example.phu.multimedia_v1;

import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private TextView textMaxTime;
    private TextView textCurrentPosition;
    private Button buttonPause,btnNext,btnPre;
    private Button buttonStart;
    private SeekBar seekBar;
    ArrayList<Integer> songsId_list = new ArrayList<>();
    private Handler threadHandler = new Handler();
    int current_song_index = 0;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.textCurrentPosition = (TextView) this.findViewById(R.id.textView_currentPosion);
        this.textMaxTime = (TextView) this.findViewById(R.id.textView_maxTime);
        this.buttonStart = (Button) this.findViewById(R.id.button_start);
        this.buttonPause = (Button) this.findViewById(R.id.button_pause);
        btnNext = (Button)findViewById(R.id.button_next);
        btnPre = (Button)findViewById(R.id.button_previous);
        this.buttonPause.setEnabled(false);

        this.seekBar = (SeekBar) this.findViewById(R.id.seekBar);
        this.seekBar.setClickable(false);
        String[] names = getAllRawResources();
        for(String s:names){
            songsId_list.add(this.getRawResIdByName(s));
        }

        // ID của file nhạc trong thư mục 'raw'.
//        int songId = this.getRawResIdByName("s1");
        final int songId = songsId_list.get(current_song_index);
        // Tạo đối tượng MediaPlayer.
        this.mediaPlayer = MediaPlayer.create(this, songId);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                current_song_index++;
                if(current_song_index>songsId_list.size()-1){
                    current_song_index = 0;
                }
                int songid = songsId_list.get(current_song_index);
                mediaPlayer.stop();
                mediaPlayer = MediaPlayer.create(v.getContext(),songid);

                // Khoảng thời gian của bài hát (Tính theo mili giây).
                int duration = mediaPlayer.getDuration();

                int currentPosition = mediaPlayer.getCurrentPosition();
                if (currentPosition == 0) {
                    seekBar.setMax(duration);
                    String maxTimeString = millisecondsToString(duration);
                    textMaxTime.setText(maxTimeString);
                } else if (currentPosition == duration) {

                    // Trở lại trạng thái ban đầu trước khi chơi.
                    mediaPlayer.reset();
                }
                mediaPlayer.start();

                // Tạo một thread để update trạng thái của SeekBar.
                UpdateSeekBarThread updateSeekBarThread = new UpdateSeekBarThread();
                threadHandler.postDelayed(updateSeekBarThread, 50);

                buttonPause.setEnabled(true);
                buttonStart.setEnabled(false);
            }
        });
        btnPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                current_song_index--;
                if(current_song_index<0){
                    current_song_index = songsId_list.size()-1;
                }
                int songid = songsId_list.get(current_song_index);
                mediaPlayer.stop();
                mediaPlayer = MediaPlayer.create(v.getContext(),songid);

                // Khoảng thời gian của bài hát (Tính theo mili giây).
                int duration = mediaPlayer.getDuration();

                int currentPosition = mediaPlayer.getCurrentPosition();
                if (currentPosition == 0) {
                    seekBar.setMax(duration);
                    String maxTimeString = millisecondsToString(duration);
                    textMaxTime.setText(maxTimeString);
                } else if (currentPosition == duration) {

                    // Trở lại trạng thái ban đầu trước khi chơi.
                    mediaPlayer.reset();
                }
                mediaPlayer.start();

                // Tạo một thread để update trạng thái của SeekBar.
                UpdateSeekBarThread updateSeekBarThread = new UpdateSeekBarThread();
                threadHandler.postDelayed(updateSeekBarThread, 50);

                buttonPause.setEnabled(true);
                buttonStart.setEnabled(false);
            }
        });

    }

    private String[] getAllRawResources() {
        Field fields[] = R.raw.class.getDeclaredFields();
        String[] names = new String[fields.length];

        try {
            for (int i = 0; i < fields.length; i++) {
                Field f = fields[i];
                names[i] = f.getName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return names;
    }

    // Tìm ID của một file nguồn trong thư mục 'raw' theo tên.
    public int getRawResIdByName(String resName) {
        String pkgName = this.getPackageName();
        // Return 0 if not found.
        // Trả về 0 nếu không tìm thấy.
        int resID = this.getResources().getIdentifier(resName, "raw", pkgName);
        return resID;
    }


    // Chuyển số lượng milli giây thành một String có ý nghĩa.
    private String millisecondsToString(int milliseconds) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes((long) milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds((long) milliseconds);
        return minutes + ":" + seconds;
    }

    // Khi người dùng click vào Button "Start".
    public void doStart(View view) {

        // Khoảng thời gian của bài hát (Tính theo mili giây).
        int duration = this.mediaPlayer.getDuration();

        int currentPosition = this.mediaPlayer.getCurrentPosition();
        if (currentPosition == 0) {
            this.seekBar.setMax(duration);
            String maxTimeString = this.millisecondsToString(duration);
            this.textMaxTime.setText(maxTimeString);
        } else if (currentPosition == duration) {

            // Trở lại trạng thái ban đầu trước khi chơi.
            this.mediaPlayer.reset();
        }
        this.mediaPlayer.start();

        // Tạo một thread để update trạng thái của SeekBar.
        UpdateSeekBarThread updateSeekBarThread = new UpdateSeekBarThread();
        threadHandler.postDelayed(updateSeekBarThread, 50);

        this.buttonPause.setEnabled(true);
        this.buttonStart.setEnabled(false);
    }


    // Thread sử dụng để Update trạng thái cho SeekBar.
    class UpdateSeekBarThread implements Runnable {

        public void run() {
            int currentPosition = mediaPlayer.getCurrentPosition();
            String currentPositionStr = millisecondsToString(currentPosition);
            textCurrentPosition.setText(currentPositionStr);

            seekBar.setProgress(currentPosition);

            // Ngừng thread 50 mili giây.
            threadHandler.postDelayed(this, 50);
        }
    }


    // Khi người dùng Click vào nút tạm dừng (Pause).
    public void doPause(View view) {
        this.mediaPlayer.pause();
        this.buttonPause.setEnabled(false);
        this.buttonStart.setEnabled(true);
    }


    // Khi người dùng Click vào nút tua lại (Rewind)
    public void doRewind(View view) {
        int currentPosition = this.mediaPlayer.getCurrentPosition();
        int duration = this.mediaPlayer.getDuration();

        // 5 giây.
        int SUBTRACT_TIME = 5000;

        if (currentPosition - SUBTRACT_TIME > 0) {
            this.mediaPlayer.seekTo(currentPosition - SUBTRACT_TIME);
        }
    }


    // Khi người dùng Click vào nút tua đi (Fast-Forward).
    public void doFastForward(View view) {
        int currentPosition = this.mediaPlayer.getCurrentPosition();
        int duration = this.mediaPlayer.getDuration();

        // 5 giây.
        int ADD_TIME = 5000;

        if (currentPosition + ADD_TIME < duration) {
            this.mediaPlayer.seekTo(currentPosition + ADD_TIME);
        }
    }

}