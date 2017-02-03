package com.example.mohammad.powermanagement;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.net.wifi.*;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Intent;


import java.util.List;
public class Scan_WiFi extends ActionBarActivity {

    private final static String TAG = "MainActivity";
    TextView txtWifiInfo;
    WifiManager wifi;
    WifiScanReceiver wifiReceiver;
    Switch aswitch;
    TextView textView;
    Button Go,EntrPlug;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan__wi_fi);

        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        textView = (TextView) findViewById(R.id.textView);
        aswitch = (Switch) findViewById(R.id.sw);
        aswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && !wifi.isWifiEnabled()) {
                    wifi.setWifiEnabled(true);
                } else if (!isChecked && wifi.isWifiEnabled()) {
                    wifi.setWifiEnabled(false);
                }
            }
        });
        wifiReceiver = new WifiScanReceiver();
        txtWifiInfo = (TextView) findViewById(R.id.txtWifiInfo);
        Button btnScan = (Button) findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Start scan...");
                wifi.startScan();
            }
        });
        Go = (Button) findViewById(R.id.goweb);
        Go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent to_access = new Intent(Scan_WiFi.this, ConsumptionOnline.class);
                startActivity(to_access);
            }
        });


        EntrPlug = (Button) findViewById(R.id.EnterPlug001);
        EntrPlug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent to_access = new Intent(Scan_WiFi.this, PlugControl.class);
                startActivity(to_access);
            }
        });

    }
    protected void onPause () {
        unregisterReceiver(wifiReceiver);
        super.onPause();
    }

    protected void onResume () {
        registerReceiver(
                wifiReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        );
        super.onResume();
    }
    String desiredMacAddress = "5e:cf:7f:dc:41:bf";  //!! letters must be in LowerCase <-- f5 app
    // String desiredMacAddress = "62:01:94:17:8c:6d";
    String PlugNo = "Plug001";
    String bssid;
    String ssid;
    private class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> wifiScanList = wifi.getScanResults();
            txtWifiInfo.setText("");
            for (int i = 0; i < wifiScanList.size(); i++) {
                bssid = wifiScanList.get(i).BSSID;
                if (desiredMacAddress.equals(bssid)) {
                    ssid = wifiScanList.get(i).SSID;
                    if (PlugNo.equals(ssid)) {
                        txtWifiInfo.append(ssid);
                    }
                }
            }
        }

    }
}
