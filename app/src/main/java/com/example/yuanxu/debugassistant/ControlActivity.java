package com.example.yuanxu.debugassistant;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ControlActivity extends Activity {

    private final int RADIUS = 99;
    private int width = 0;
    private int height = 0;
    //widget
    private List<Button> fun_button = new ArrayList<>();

    private Button joystick;
    private TextView x_text;
    private TextView y_text;
    private TextView receive_text;

    //service
    private BluetoothService service = null;

    //joystick
    private Point joystick_center = new Point();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        initWidget();
        setListening();

        binBluetoothService();
    }

    private void initWidget()
    {
        joystick = findViewById(R.id.joystick);
        joystick_center.set(joystick.getLeft(), joystick.getTop());
        width = joystick.getWidth();
        height = joystick.getHeight();

        x_text = findViewById(R.id.x_edit);
        y_text = findViewById(R.id.y_edit);
        receive_text = findViewById(R.id.con_receive_text);

        fun_button.add((Button) findViewById(R.id.fun_button1));
        fun_button.add((Button) findViewById(R.id.fun_button2));
        fun_button.add((Button) findViewById(R.id.fun_button3));
        fun_button.add((Button) findViewById(R.id.fun_button4));
        fun_button.add((Button) findViewById(R.id.fun_button5));
        fun_button.add((Button) findViewById(R.id.fun_button6));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setListening()
    {
        for (Button button: fun_button) {
            button.setOnClickListener(new ClickListening());
        }

        joystick.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getActionMasked() == MotionEvent.ACTION_UP)
                {
                    //回到原点
                    moveJoystick(joystick_center.x, joystick_center.y);
                    setSpeedText(new Point(0,0));
                    sendData("X0Y0");
                }
                else if(event.getActionMasked() == MotionEvent.ACTION_MOVE)
                {
                    float dx = event.getX() - joystick_center.x;
                    float dy = event.getY() - joystick_center.y;
                    Point speed = calSpeed(dx, dy);

                    setSpeedText(speed);
                    sendData("X"+ speed.x + "Y" + speed.y);
                }
                //else if(event.getActionMasked() == MotionEvent.ACTION_DOWN) { }
                return true;
            }
        });
    }

    private void binBluetoothService() {
        //bluetooth service
        BluetoothServiceConnect serviceConnect = new BluetoothServiceConnect();
        Intent intent = new Intent(ControlActivity.this, BluetoothService.class);
        bindService(intent, serviceConnect, BIND_AUTO_CREATE);
    }

    //发送数据
    private void sendData(String str)
    {
        if(service != null)
        {
            service.setSendStream(str);
        }
        else
        {
            Toast.makeText(ControlActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
        }
    }

    private Point calSpeed(float dx, float dy)
    {
        Point speed = new Point();
        dy = -dy;

        if(dx > RADIUS)
        {
            dx = RADIUS;
        }
        else if(dx < -RADIUS)
        {
            dx = -RADIUS;
        }

        if (dy > RADIUS)
        {
            dy = RADIUS;
        }
        else if(dy < -RADIUS)
        {
            dy = -RADIUS;
        }

        moveJoystick(joystick_center.x + (int)dx, joystick_center.y + (int)dy );

        speed.set((int) dx, (int) dy);

        if(Math.sqrt(Math.pow(speed.x ,2) + Math.pow(speed.y, 2)) > RADIUS)
        {
            if (speed.y >= 0)
            {
                double theta = Math.atan2(speed.y , speed.x);
                speed.set((int)(RADIUS * Math.cos(theta)), (int)(RADIUS * Math.sin(theta)));
            }
            else
            {
                double theta = Math.atan2(speed.y, speed.x);
                speed.set((int)(RADIUS * Math.cos(theta)),(int) (RADIUS * Math.sin(theta)));
            }
        }
        return speed;
    }

    private void setSpeedText(Point point)
    {
        if(point != null)
        {
            x_text.setText(String.valueOf(point.x));
            y_text.setText(String.valueOf(point.y));
        }
    }

    private void moveJoystick(int x, int y)
    {
        //joystick.setLayoutParams(new RelativeLayout.LayoutParams(x,y));
        joystick.layout(x, y, x+width, y+height);
    }

    public class BluetoothServiceConnect implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ((BluetoothService.LocalBinder) binder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service = null;
        }
    }

    //接收广播
    public class ContentReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            String str = intent.getStringExtra("info");
            receive_text.append(str);
        }
    }

    private class ClickListening implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.fun_button1:
                    sendData("B1");
                    break;
                case R.id.fun_button2:
                    sendData("B2");
                    break;
                case R.id.fun_button3:
                    sendData("B3");
                    break;
                case R.id.fun_button4:
                    sendData("B4");
                    break;
                case R.id.fun_button5:
                    sendData("B5");
                    break;
                case R.id.fun_button6:
                    sendData("B6");
                    break;
            }
        }
    }
}

