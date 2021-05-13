package com.netbyte.vtunnel.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Bundle;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.netbyte.vtunnel.R;
import com.netbyte.vtunnel.config.AppConst;
import com.netbyte.vtunnel.service.VTunnelService;

public class MainActivity extends AppCompatActivity {
    private Button btnConn, btnDisConn;
    private EditText editServer, editServerPort, editLocal, editDNS, tokenEdit;
    private TextView msgView;
    private MaterialButtonToggleGroup protocolGroup;
    SharedPreferences preferences;
    SharedPreferences.Editor preEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConn = findViewById(R.id.connButton);
        btnDisConn = findViewById(R.id.disConnButton);
        btnWs = findViewById(R.id.protocolBtnWs);
        btnUdp = findViewById(R.id.protocolBtnUdp);
        editServer = findViewById(R.id.serverAddressEdit);
        editServerPort = findViewById(R.id.serverPortEdit);
        editLocal = findViewById(R.id.localAddressEdit);
        msgView = findViewById(R.id.msgTextView);
        tokenEdit = findViewById(R.id.keyText);
        editDNS = findViewById(R.id.dnsEdit);
        protocolGroup = findViewById(R.id.protocolGroup);
        preferences = getPreferences(Activity.MODE_PRIVATE);
        preEditor = preferences.edit();

        editServer.setText(preferences.getString("serverIP", AppConst.DEFAULT_SERVER_ADDRESS));
        editServerPort.setText(preferences.getString("serverPort", AppConst.DEFAULT_SERVER_PORT));
        editLocal.setText(preferences.getString("localIP", AppConst.DEFAULT_LOCAL_ADDRESS));
        editDNS.setText(preferences.getString("dns", AppConst.DEFAULT_DNS));
        tokenEdit.setText(preferences.getString("token", AppConst.DEFAULT_TOKEN));
        String preProtocol = preferences.getString("protocol", AppConst.PROTOCOL_WS);
        protocolGroup.check(preProtocol.equals(AppConst.PROTOCOL_WS) ? R.id.protocolBtnWs : R.id.protocolBtnUdp);
        btnDisConn.setEnabled(preferences.getBoolean("connected", false));
        btnDisConn.setOnClickListener(v -> {
            msgView.setText("Disconnected");
            btnDisConn.setEnabled(false);
            btnConn.setEnabled(true);
            preEditor.putBoolean("connected", false);
            preEditor.apply();

            Intent intent = new Intent();
            intent.setClass(MainActivity.this, VTunnelService.class);
            intent.setAction(AppConst.BTN_ACTION_DISCONNECT);
            startService(intent);
        });
        btnConn.setEnabled(!preferences.getBoolean("connected", false));
        btnConn.setOnClickListener(v -> {
            msgView.setText("Connected");
            btnConn.setEnabled(false);
            btnDisConn.setEnabled(true);
            Intent intent = VpnService.prepare(MainActivity.this);
            if (intent != null) {
                startActivityForResult(intent, 0);
            } else {
                onActivityResult(0, RESULT_OK, null);
            }
        });

    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (result != RESULT_OK) {
            return;
        }
        String serverIP = editServer.getText().toString();
        String serverPort = editServerPort.getText().toString();
        String localIp = editLocal.getText().toString();
        String dns = editDNS.getText().toString();
        String token = tokenEdit.getText().toString();
        int buttonId = protocolGroup.getCheckedButtonId();
        String protocol = (buttonId == R.id.protocolBtnWs) ? AppConst.PROTOCOL_WS : AppConst.PROTOCOL_UDP;
        // new intent
        Intent intent = new Intent(this, VTunnelService.class);
        intent.setAction(AppConst.BTN_ACTION_CONNECT);
        intent.putExtra("serverIP", serverIP);
        intent.putExtra("serverPort", Integer.parseInt(serverPort));
        intent.putExtra("localIP", localIp);
        intent.putExtra("dns", dns);
        intent.putExtra("token", token);
        intent.putExtra("protocol", protocol);
        // start service
        startService(intent);
        // save config
        preEditor.putString("serverIP", serverIP);
        preEditor.putString("serverPort", serverPort);
        preEditor.putString("localIP", localIp);
        preEditor.putString("dns", dns);
        preEditor.putString("token", token);
        preEditor.putString("protocol", protocol);
        preEditor.putBoolean("connected", true);
        preEditor.apply();
    }

}
