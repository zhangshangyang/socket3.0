package com.example.administrator.socket;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {
    private WebSocketClient webSocketClient;
    private TextView textView;
    private int state;
    private String string="ws://192.168.11.103:6341/";

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            textView.setText("");
            textView.setText(textView.getText() + "\n" + msg.obj);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView=findViewById(R.id.editText);
        Button button = findViewById(R.id.button);
        Button button2 = findViewById(R.id.button2);
        state=1;
       // init();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webSocketClient != null) {
                    //webSocketClient.send(textView.getText().toString().trim());
                    webSocketClient.close();
                    state=1;
                }
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state == 1){
                init();
                state=0;
            }
        }
        });
    }
    //网络读取数据
    private void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    webSocketClient = new WebSocketClient(new URI(string), new Draft_10()) {
                        @Override
                        public void onOpen(ServerHandshake handshakedata) {
                            Log.d("picher_log", "打开通道" + handshakedata.getHttpStatus());
                           // handler.obtainMessage(0,  message).sendToTarget();
                        }

                        @Override
                        public void onMessage(String message) {
                            Log.d("picher_log", "接收消息" + message);
                            handler.obtainMessage(0, message).sendToTarget();
                        }

                        @Override
                        public void onClose(int code, String reason, boolean remote) {
                            Log.d("picher_log", "通道关闭");
                          //  handler.obtainMessage(0, message).sendToTarget();
                        }

                        @Override
                        public void onError(Exception ex) {
                            Log.d("picher_log", "链接错误");
                        }
                    };
                    webSocketClient.connect();

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }
}
