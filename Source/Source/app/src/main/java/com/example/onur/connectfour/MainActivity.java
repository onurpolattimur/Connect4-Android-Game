package com.example.onur.connectfour;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Uygulama ilk açıldığında çalışan activitydir.
 * Kullanıcıdan oynanacak oyun hakkında bilgileri alır.
 */
public class MainActivity extends AppCompatActivity {
    private RadioGroup radioGroup;
    private RadioButton pvp;
    private RadioButton pvc;
    private SeekBar timeBar;
    private TextView timeSecond;
    private Button startButton;

    private SeekBar sizeBar;
    private TextView sizeText;

    private int gameMode;
    private int time;
    private int size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Tam ekran yapmak için gerekli işlemler.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        gameMode = 2;
        pvp = findViewById(R.id.pvp);
        startButton = findViewById(R.id.startButton);
        pvc = findViewById(R.id.pvc);
        radioGroup = findViewById(R.id.rgroup);
        timeBar = findViewById(R.id.timeBar);
        timeSecond = findViewById(R.id.timeSecond);
        time=0;


        sizeBar = findViewById(R.id.sizeBar);
        sizeText=findViewById(R.id.sizeText);
        size = sizeBar.getProgress();
        //Size için gerekli olan seekbar
        sizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i<5){
                    seekBar.setProgress(5);
                    i=5;
                }
                size=i;
                sizeText.setText("Board Size: "+i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Zaman için gerekli olan seek bar
        timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i<2){
                    timeBar.setProgress(2);
                    i=2;
                }
                time=i;
                timeSecond.setText(i + " sec");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Switch onOffSwitch = (Switch) findViewById(R.id.timeSwitch);
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    timeBar.setVisibility(View.VISIBLE);
                    timeSecond.setVisibility(View.VISIBLE);
                    time = timeBar.getProgress();
                }else{
                    time=0;
                    timeSecond.setVisibility(View.GONE);
                    timeBar.setVisibility(View.GONE);
                }
            }

        });
    }

    public void radioClick(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.pvp:
                if (checked) {
                    radioGroup.clearCheck();
                    pvc.setChecked(false);
                    gameMode = 2;
                    break;
                }
            case R.id.pvc:
                if (checked) {
                    radioGroup.clearCheck();
                    pvp.setChecked(false);
                    gameMode = 1;
                    break;
                }
        }
    }

    public void startClick(View view) {
        if(view.getId()== startButton.getId()){
            Intent intent = new Intent(this,GameActivity.class);
            intent.putExtra("GameMode",gameMode);
            intent.putExtra("Size",size);
            intent.putExtra("Time",time);
            startActivity(intent);

        }
    }
}
