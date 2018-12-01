package com.example.yuanxu.debugassistant;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;

public class MainActivity extends AppCompatActivity {

    private Button bluetooth_but;
    private Button serial_but;
    private Button control_but;
    private Button about_but;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.initWidget();
        this.setListening();
    }

    private void initWidget()
    {
        //Button Listening
        bluetooth_but = findViewById(R.id.button1);
        serial_but = findViewById(R.id.button2);
        control_but = findViewById(R.id.button3);
        about_but = findViewById(R.id.button4);
    }

    private void setListening()
    {
        //bluetooth connect activity
        bluetooth_but.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivity(intent);
            }
        });

        //serial assistant activity
        serial_but.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, SerialActivity.class);
                startActivity(intent);
            }
        });

        // control activity
        control_but.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, ControlActivity.class);
                startActivity(intent);
            }
        });

        //about activity
        about_but.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
    }
}

