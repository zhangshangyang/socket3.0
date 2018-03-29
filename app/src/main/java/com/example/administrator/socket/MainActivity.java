package com.example.administrator.socket;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URI;
import java.net.URISyntaxException;

@SuppressLint("ValidFragment")
class ViewDialogFragment extends DialogFragment {

    public interface Callback {
        void onClick(String userName, String password);
    }

    private Callback callback;

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, "ViewDialogFragment");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams") final View view = inflater.inflate(R.layout.login, null);
        builder.setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null) {
                            EditText et_userName = view.findViewById(R.id.username);
                            EditText et_password = view.findViewById(R.id.password);
                            callback.onClick(et_userName.getText().toString(), et_password.getText().toString());
                        }
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Callback) {
            callback = (Callback) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement Callback");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        callback = null;
    }
}

public class MainActivity extends AppCompatActivity  implements ViewDialogFragment.Callback{
    private WebSocketClient webSocketClient;
    private TextView textView;
    private int state;
    private String string="ws://192.168.11.103:6341/"; //IP and port

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //textView.setText("message:" + "\n" + msg.obj);
            parseJson((String) msg.obj);
        }
    };
    @SuppressLint("SetTextI18n")
    private void parseJson(String strResult){
        try{
            JSONTokener jsonTokener = new JSONTokener(strResult);
            JSONObject person = (JSONObject) jsonTokener.nextValue();
            //person.getString("Master ID");
            textView.setText("Master ID:" + person.getString("Master ID"));
            JSONArray jsonArray = new JSONObject(strResult).getJSONArray("Doubles");
            //*Double X = (Double) jsonArray.get(0);
           /* Double Y = (Double) jsonArray.get(1);
            Double Z = (Double) jsonArray.get(2);
            Double R1 = (Double) jsonArray.get(3);
            Double R3 = (Double) jsonArray.get(4);*/
            textView.setText(textView.getText()+"\n"+"X="+jsonArray.get(0));
            textView.setText(textView.getText()+"\n"+"Y="+jsonArray.get(1));
            textView.setText(textView.getText()+"\n"+"Z="+jsonArray.get(2));
            textView.setText(textView.getText()+"\n"+"R1="+jsonArray.get(3));
            textView.setText(textView.getText()+"\n"+"R3="+jsonArray.get(4));
            JSONArray jsonArray2 = new JSONObject(strResult).getJSONArray("Booleans");
            textView.setText(textView.getText()+"\n"+"X:  MOVE =" +jsonArray2.get(0)+ "  Limit- ="+jsonArray2.get(1)+"   Limit+ ="+jsonArray2.get(2));
            textView.setText(textView.getText()+"\n"+"Y:  MOVE =" +jsonArray2.get(3)+ "  Limit- ="+jsonArray2.get(4)+"   Limit+ ="+jsonArray2.get(5));
            textView.setText(textView.getText()+"\n"+"Z:  MOVE =" +jsonArray2.get(6)+ "  Limit- ="+jsonArray2.get(7)+"   Limit+ ="+jsonArray2.get(8));
            textView.setText(textView.getText()+"\n"+"R1:  MOVE =" +jsonArray2.get(9)+ "  Limit- ="+jsonArray2.get(10)+"  Limit+ ="+jsonArray2.get(11));
            textView.setText(textView.getText()+"\n"+"R3:  MOVE =" +jsonArray2.get(12)+ "  Limit- ="+jsonArray2.get(13)+"  Limit+ ="+jsonArray2.get(14));
        }catch (JSONException e) {
            textView.setText("Json pares error");
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView=findViewById(R.id.editText);
        Button button = findViewById(R.id.button);
        Button button2 = findViewById(R.id.button2);
        Button button3= findViewById(R.id.button3);
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
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showViewDialogFragment(v);
            }
        });
    }

    public void showViewDialogFragment(View view) {
        ViewDialogFragment viewDialogFragment = new ViewDialogFragment();
        viewDialogFragment.show(getFragmentManager());
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
    public void onClick(String userName, String password) {
        Toast.makeText(MainActivity.this, "用户名: " + userName + " 密码: " + password, Toast.LENGTH_SHORT).show();
    }
}
