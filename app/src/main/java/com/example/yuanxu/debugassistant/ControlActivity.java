package com.example.yuanxu.debugassistant;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ControlActivity extends Activity {

    private final int RADIUS = 200;
    private float last_x;
    private float last_y;
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
    private ContentReceiver contentReceiver;

    //joystick
    private Point joystick_origin = new Point();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        initWidget();
        setListener();
        binBluetoothService();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //广播接受者
        contentReceiver = new ContentReceiver();
        //过滤器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("ServiceBoardCast");
        //注册广播
        registerReceiver(contentReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(contentReceiver);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        joystick_origin.set(joystick.getLeft(), joystick.getTop());
        width = joystick.getWidth();
        height = joystick.getHeight();
    }

    private void initWidget()
    {
        joystick = findViewById(R.id.joystick);

        x_text = findViewById(R.id.x_edit);
        y_text = findViewById(R.id.y_edit);

        receive_text = findViewById(R.id.con_receive_text);
        receive_text.setMovementMethod(ScrollingMovementMethod.getInstance());

        fun_button.add((Button) findViewById(R.id.fun_button1));
        fun_button.add((Button) findViewById(R.id.fun_button2));
        fun_button.add((Button) findViewById(R.id.fun_button3));
        fun_button.add((Button) findViewById(R.id.fun_button4));
        fun_button.add((Button) findViewById(R.id.fun_button5));
        fun_button.add((Button) findViewById(R.id.fun_button6));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setListener()
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
                    moveJoystick(joystick_origin.x, joystick_origin.y);
                    setSpeedText(new Point(0,0));
                    sendData("X0Y0");
                }
                else if(event.getActionMasked() == MotionEvent.ACTION_MOVE)
                {

                    float dx = event.getX() - last_x;   //计算移动的距离
                    float dy = event.getY() - last_y;   //

                    if(dx > RADIUS)
                    {
                        dx = RADIUS;
                    }
                    else if (dx < -RADIUS)
                    {
                        dx = -RADIUS;
                    }

                    if (dy > RADIUS)
                    {
                        dy = RADIUS;
                    }
                    else if (dy < -RADIUS)
                    {
                        dy = -RADIUS;
                    }

                    int left = (int) (joystick_origin.x + dx);
                    int top = (int) (joystick_origin.y + dy);

                    //移动
                    moveJoystick(left, top);
                    Point speed = calSpeed(left, top);
                    setSpeedText(speed);
                    sendData("X"+ speed.x + "Y" + speed.y);
                }
                else if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {

                    last_x = event.getX();
                    last_y = event.getY();
                }
                return true;
            }
        });
    }

    private void binBluetoothService() {

        if(BluetoothService.isRunning()) {
            //bluetooth service
            BluetoothServiceConnect serviceConnect = new BluetoothServiceConnect();
            Intent intent = new Intent(ControlActivity.this, BluetoothService.class);

            bindService(intent, serviceConnect, BIND_AUTO_CREATE);
        }
    }

    //发送数据
    private void sendData(String str)
    {
        if(service != null)
        {
            service.setSendStream(str);
        }
    }

    private Point calSpeed(int left, int top)
    {
        Point speed = new Point();
        speed.set(left - joystick_origin.x, top-joystick_origin.y);
        speed.y = -speed.y;

        if(Math.sqrt(Math.pow(speed.x ,2) + Math.pow(speed.y, 2)) > RADIUS)
        {
            double theta = Math.atan2(speed.y , speed.x);
            speed.set((int)(RADIUS * Math.cos(theta)), (int)(RADIUS * Math.sin(theta)));
        }
        float rate = (float) 100/RADIUS;
        speed.x = (int) (speed.x * rate);
        speed.y = (int) (speed.y * rate);
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

            int offset = (receive_text.getLineCount()-3) * receive_text.getLineHeight();

            if(offset > receive_text.getLineHeight()) {
                receive_text.scrollTo(0, offset);
            }
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

