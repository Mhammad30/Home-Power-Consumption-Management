package com.example.mohammad.powermanagement;

import android.app.AlertDialog;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;


public class PlugControl extends ActionBarActivity {

    private final static String TAG = "PlugControl";
    WifiManager wifiManager;

    public void connectToAP(String  networkSSID, String networkPass) {
        Log.i(TAG, "* connectToAP");
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        networkSSID = "Plug001";
        networkPass = "12345678";
        Log.d(TAG, "# password " + networkPass);
        List<ScanResult> ScanResultList = wifiManager.getScanResults();

        for (ScanResult result : ScanResultList) {
            if (result.SSID.equals(networkSSID)) {

                String securityMode = getScanResultSecurity(result);

                if (securityMode.equalsIgnoreCase("OPEN")) {

                    wifiConfiguration.SSID = "\"" + networkSSID + "\"";
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    int res = wifiManager.addNetwork(wifiConfiguration);
                    Log.d(TAG, "# add Network returned " + res);

                    boolean b = wifiManager.enableNetwork(res, true);
                    Log.d(TAG, "# enableNetwork returned " + b);

                    wifiManager.setWifiEnabled(true);
                    Toast.makeText(getApplicationContext(), "Connecting OPEN _AP...", Toast.LENGTH_LONG).show();
//==========================================================================================================================\\
                } else if (securityMode.equalsIgnoreCase("WEP")) {

                    wifiConfiguration.SSID = "\"" + networkSSID + "\"";
                    wifiConfiguration.wepKeys[0] = "\"" + networkPass + "\"";
                    wifiConfiguration.wepTxKeyIndex = 0;
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                    int res = wifiManager.addNetwork(wifiConfiguration);
                    Log.d(TAG, "### 1 ### add Network returned " + res);

                    boolean b = wifiManager.enableNetwork(res, true);
                    Log.d(TAG, "# enableNetwork returned " + b);

                    wifiManager.setWifiEnabled(true);
                    Toast.makeText(getApplicationContext(), "Connecting WEP _AP...", Toast.LENGTH_LONG).show();
                }
//========================================================================================================================\\
                wifiConfiguration.SSID = "\"" + networkSSID + "\"";
                wifiConfiguration.preSharedKey = "\"" + networkPass + "\"";
                wifiConfiguration.hiddenSSID = true;
                wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

                int res = wifiManager.addNetwork(wifiConfiguration);
                Log.d(TAG, "### 2 ### add Network returned " + res);

                wifiManager.enableNetwork(res, true);
                if(res != -1 ){ Toast.makeText(getApplicationContext(), "Connecting WPA _AP...", Toast.LENGTH_LONG).show();}
//============================================================================================================================\\
                boolean changeHappen = wifiManager.saveConfiguration();

                if(res != -1 && changeHappen){
                    Log.d(TAG, "### Change happen");
                    Toast.makeText(getApplicationContext(), "ACCESS Done...", Toast.LENGTH_LONG).show();
                    //AppStaticVar.connectedSsidName = networkSSID;

                }else{
                    Log.d(TAG, "*** Change NOT happen");
                }

                wifiManager.setWifiEnabled(true);
            }
        }
    }

    public String getScanResultSecurity(ScanResult scanResult) {
        Log.i(TAG, "* getScanResultSecurity");

        final String cap = scanResult.capabilities;
        final String[] securityModes = { "WEP", "PSK", "EAP" };

        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }

        return "OPEN";
    }

    // declare buttons and text inputs
    private Button Turn_OFF,Turn_ON,Show,access;
    EditText EMoment;
    String Moment="11";
    String PlugIP= "192.168.4.1";
    String PlugPort ="80";
    // get the Command
    String parameterValue;
    // get the ip address
    String ipAddress = PlugIP;
    // get the port number
    String portNumber = PlugPort ;

    public void Access(View v){
        Turn_OFF=(Button)findViewById(R.id.TurnOff);
        Turn_ON=(Button)findViewById(R.id.TurnOn);
        Show=(Button)findViewById(R.id.getit);

        EMoment = (EditText)findViewById(R.id.moment);


        Turn_OFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  parameterValue = "LOW";

                // execute HTTP request
                if(ipAddress.length()>0 && portNumber.length()>0) {
                    new HttpRequestAsyncTask(
                            v.getContext(), parameterValue, ipAddress, portNumber).execute(); } } }  );


        Turn_ON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { parameterValue = "HIGH";
                // execute HTTP request
                if(ipAddress.length()>0 && portNumber.length()>0) {
                    new HttpRequestAsyncTask(
                            v.getContext(), parameterValue, ipAddress, portNumber).execute(); }}} );

        Show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Moment = EMoment.getText().toString();
                String hash1 ="#";
                parameterValue=hash1.concat(Moment);
                if(ipAddress.length()>0 && portNumber.length()>0) {
                    new HttpRequestAsyncTask(
                            v.getContext(), parameterValue, ipAddress, portNumber).execute();}


            }});




    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plug_control);
        String networkSSID = "Plug001";
        String networkPass = "12345678";
        connectToAP(networkSSID,networkPass);
        //=======================================\\
        // assign buttons
        Turn_OFF = (Button)findViewById(R.id.TurnOff);
        Turn_ON = (Button)findViewById(R.id.TurnOn);
        Show = (Button)findViewById(R.id.getit);
        Access(access);
    }
    /**
     * Description: Send an HTTP Get request to a specified ip address and port.
     * Also send a parameter "parameterName" with the value of "parameterValue".
     * @param parameterValue the pin number to toggle
     * @param ipAddress the ip address to send the request to
     * @param portNumber the port number of the ip address
     * @return The ip address' reply text, or an ERROR message is it fails to receive one
     */
    public String sendRequest(String parameterValue, String ipAddress, String portNumber) {
        String serverResponse = "ERROR";

        try {

            HttpClient httpclient = new DefaultHttpClient(); // create an HTTP client
            // define the URL e.g. http://myIpaddress:myport/?pin=13 (to toggle pin 13 for example)
            URI website = new URI("http://"+ipAddress+":"+portNumber+"/?"+parameterValue);
            HttpGet getRequest = new HttpGet(); // create an HTTP GET object
            getRequest.setURI(website); // set the URL of the GET request
            HttpResponse response = httpclient.execute(getRequest); // execute the request
            // get the ip address server's reply
            InputStream content = null;
            content = response.getEntity().getContent();
            BufferedReader in = new BufferedReader(new InputStreamReader( content));
            serverResponse = in.readLine();

            // Close the connection
            content.close();
        } catch (ClientProtocolException e) {
            // HTTP error
            serverResponse = e.getMessage();
            e.printStackTrace();
        } catch (IOException e) {
            // IO error
            serverResponse = e.getMessage();
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // URL syntax error
            serverResponse = e.getMessage();
            e.printStackTrace();
        }

        // return the server's reply/response text
        return serverResponse;

    }
    /**
     * An AsyncTask is needed to execute HTTP requests in the background so that they do not
     * block the user interface.
     */
    private class HttpRequestAsyncTask extends AsyncTask<Void, Void, Void> {

        // declare variables needed
        private String requestReply,ipAddress, portNumber;
        private Context context;
        private AlertDialog alertDialog;
        //   private String parameter;
        private String parameterValue;

        /**
         * Description: The asyncTask class constructor. Assigns the values used in its other methods.
         * @param context the application context, needed to create the dialog
         * @param parameterValue the pin number to toggle
         * @param ipAddress the ip address to send the request to
         * @param portNumber the port number of the ip address
         */
        public HttpRequestAsyncTask(Context context, String parameterValue, String ipAddress, String portNumber)
        {
            this.context = context;

            alertDialog = new AlertDialog.Builder(this.context)
                    .setTitle("HTTP Response From Plug:")
                    .setCancelable(true)
                    .create();

            this.ipAddress = ipAddress;
            this.parameterValue = parameterValue;
            this.portNumber = portNumber;

        }

        /**
         * Name: doInBackground
         * Description: Sends the request to the ip address
         * @param voids
         * @return
         */
        @Override
        protected Void doInBackground(Void... voids) {
            alertDialog.setMessage("Data sent, waiting for plug reply ...");
            if(!alertDialog.isShowing())
            {
                alertDialog.show();
            }
            requestReply = sendRequest(parameterValue,ipAddress,portNumber);
            return null;
        }

        /**
         * Name: onPostExecute
         * Description: This function is executed after the HTTP request returns from the ip address.
         * The function sets the dialog's message with the reply text from the server and display the dialog
         * if it's not displayed already (in case it was closed by accident);
         * @param aVoid void parameter
         */
        @Override
        protected void onPostExecute(Void aVoid) {

            alertDialog.setMessage(requestReply);
            if(!alertDialog.isShowing())
            {
                alertDialog.show(); // show dialog
            }
        }

        /**
         * Name: onPreExecute
         * Description: This function is executed before the HTTP request is sent to ip address.
         * The function will set the dialog's message and display the dialog.
         */
        @Override
        protected void onPreExecute() {
            alertDialog.setMessage("Sending data to plug, please wait...");
            if(!alertDialog.isShowing())
            {
                alertDialog.show();
            }
        }

    }

}