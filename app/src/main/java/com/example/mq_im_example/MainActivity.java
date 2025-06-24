package com.example.mq_im_example;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rabbitmq.client.DeliverCallback;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {
    private TextView tvMsg;
    private EditText etInput;
    private Button btnSend;
    private Button btnChannel;
    private RecyclerView rvMsg;
    private MsgAdapter adapter;
    private List<MsgBean> msgList = new ArrayList<>();
    private final String QUEUE_NAME = "mq_im_example";
    private String userName = "";
    private String userPassword = "";
    private String serverHost = "20.120.25.102";
    private int serverPort = 5672;
    private String channelName = "mq_im_example";
    private static final String[] PRESET_USERS = {"user1", "user2", "user3", "user4"};
    private static final String[] PRESET_PASSWORDS = {"00000001", "00000002", "00000003", "00000004"};
    private boolean isImMode = true; // true=IM群聊模式，false=简单队列

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        rvMsg = findViewById(R.id.rvMsg);
        adapter = new MsgAdapter(msgList);
        rvMsg.setLayoutManager(new LinearLayoutManager(this));
        rvMsg.setAdapter(adapter);
        etInput = findViewById(R.id.etInput);
        btnSend = findViewById(R.id.btnSend);
        btnChannel = findViewById(R.id.btnChannel);
        btnSend.setOnClickListener(v -> sendMsg());
        btnChannel.setOnClickListener(v -> switchChannel());
        showServerDialog();
    }

    private void switchChannel() {
        // 清空消息列表，重新弹出频道/服务器/用户选择
        msgList.clear();
        adapter.notifyDataSetChanged();
        showServerDialog();
    }

    private void showServerDialog() {
        runOnUiThread(() -> {
            final EditText etHost = new EditText(this);
            etHost.setHint("服务器地址(留空为默认)");
            final EditText etPort = new EditText(this);
            etPort.setHint("端口(默认5672)");
            etPort.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            final EditText etChannel = new EditText(this);
            etChannel.setHint("频道名称(默认mq_im_example)");
            final String[] modes = {"IM群聊模式", "简单队列模式"};
            final int[] checkedItem = {0};
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(etHost);
            layout.addView(etPort);
            layout.addView(etChannel);
            // 用AlertDialog单选模式
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("服务器设置")
                .setView(layout)
                .setSingleChoiceItems(modes, 0, (dialog, which) -> checkedItem[0] = which)
                .setCancelable(false)
                .setPositiveButton("确定", (d, w) -> {
                    String host = etHost.getText().toString().trim();
                    String portStr = etPort.getText().toString().trim();
                    String ch = etChannel.getText().toString().trim();
                    if (!host.isEmpty()) serverHost = host;
                    if (!portStr.isEmpty()) serverPort = Integer.parseInt(portStr);
                    if (!ch.isEmpty()) channelName = ch;
                    isImMode = checkedItem[0] == 0;
                    if (host.isEmpty()) {
                        showUserSelectDialog();
                    } else {
                        showCustomUserDialog();
                    }
                })
                .show();
        });
    }

    private void showUserSelectDialog() {
        runOnUiThread(() -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("选择用户名")
                .setItems(PRESET_USERS, (dialog, which) -> {
                    userName = PRESET_USERS[which];
                    userPassword = PRESET_PASSWORDS[which];
                    RabbitMqManager.setConfig(serverHost, serverPort, userName, userPassword, channelName, isImMode);
                    receiveMsg();
                })
                .setCancelable(false)
                .show();
        });
    }
    private void showCustomUserDialog() {
        runOnUiThread(() -> {
            final EditText etUser = new EditText(this);
            etUser.setHint("用户名");
            final EditText etPwd = new EditText(this);
            etPwd.setHint("密码");
            etPwd.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(etUser);
            layout.addView(etPwd);
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("输入用户名和密码")
                .setView(layout)
                .setCancelable(false)
                .setPositiveButton("确定", (d, w) -> {
                    userName = etUser.getText().toString().trim();
                    userPassword = etPwd.getText().toString().trim();
                    RabbitMqManager.setConfig(serverHost, serverPort, userName, userPassword, channelName, isImMode);
                    receiveMsg();
                })
                .show();
        });
    }

    private void sendMsg() {
        String msg = etInput.getText().toString();
        if (msg.isEmpty()) return;
        JSONObject obj = new JSONObject();
        try {
            obj.put("user", userName.isEmpty() ? "匿名" : userName);
            obj.put("msg", msg);
            obj.put("timestamp", System.currentTimeMillis()); // 加入时间戳
        } catch (Exception e) {}
        runOnUiThread(() -> {
            etInput.setText("");
            btnSend.setEnabled(false);
            btnSend.setText("发送中...");
        });
        new Thread(() -> {
            try {
                RabbitMqManager.sendMessage(channelName, obj.toString(), isImMode);
                
                runOnUiThread(() -> {
                    btnSend.setEnabled(true);
                    btnSend.setText("发送");
                    rvMsg.scrollToPosition(adapter.getItemCount() - 1);
                });
            } catch (IOException | TimeoutException e) {
                runOnUiThread(() -> {
                    btnSend.setEnabled(true);
                    btnSend.setText("发送");
                    showErrorDialog(e);
                });
            }
        }).start();
    }

    private void receiveMsg() {
        new Thread(() -> {
            try {
                RabbitMqManager.receiveMessage(channelName, (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody());
                    processMessage(message);
                }, isImMode);
            } catch (IOException | TimeoutException e) {
                runOnUiThread(() -> showErrorDialog(e));
            }
        }).start();
    }
    
    private void processMessage(String message) {
        try {
            JSONObject obj = new JSONObject(message);
            String user = obj.optString("user", "匿名");
            String msg = obj.optString("msg", message);
            
            runOnUiThread(() -> {
                adapter.addMsg(new MsgBean(user, msg));
                rvMsg.scrollToPosition(adapter.getItemCount() - 1);
            });
        } catch (Exception ex) {
            // 忽略格式错误
        }
    }

    private void showErrorDialog(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.toString()).append("\n\n");
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("错误")
                .setMessage(sb.toString())
                .setPositiveButton("确定", null)
                .show();
    }
}