package com.example.yuanxu.debugassistant;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SerialActivity extends Activity {

    //widget
    private TextView receive_text;
    private EditText send_text;
    private Button clear_receive;
    private Button clear_send;
    private Button send;

    //service
    private BluetoothService service;
    ContentReceiver contentReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial);

        initWidget();
        setListener();
        bindBluetoothService();
    }

    //继续时接收广播
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

    //暂停时解除广播
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(contentReceiver);
    }

    private void initWidget()
    {
        receive_text = findViewById(R.id.receive_text);
        receive_text.setMovementMethod(ScrollingMovementMethod.getInstance());
        send_text = findViewById(R.id.send_text);
        clear_receive = findViewById(R.id.cls_receive_but);
        clear_send = findViewById(R.id.cls_send_but);
        send = findViewById(R.id.send_button);
    }

    private void setListener()
    {
        clear_receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receive_text.setText("");   //清空
            }
        });

        clear_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_text.setText("");
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = String.valueOf(send_text.getText());
                if(!str.isEmpty())
                {
                    //发送
                    if(service != null)
                    {
                        service.setSendStream(str);
                    }
                    else
                    {
                        Toast.makeText(SerialActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        send_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = String.valueOf(send_text.getText());
                if(str.isEmpty())
                {
                    send.setEnabled(false);
                }
                else
                {
                    send.setEnabled(true);
                }
            }
        });
    }

    private void bindBluetoothService()
    {
        //bluetooth service
        if(BluetoothService.isRunning()) {

            BluetoothServiceConnect serviceConnect = new BluetoothServiceConnect();
            Intent intent = new Intent(this, BluetoothService.class);
            bindService(intent, serviceConnect, BIND_AUTO_CREATE);
        }
    }

    public class BluetoothServiceConnect implements ServiceConnection {

        //创建是绑定
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ((BluetoothService.LocalBinder) binder).getService();
        }

        //销毁时解除绑定
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
}
