package com.example.mohammad.powermanagement;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;



public class Log_In extends ActionBarActivity {

    private EditText username,password;
    private Button but;

    public void login(View v){
        but=(Button)findViewById(R.id.but1);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(username.getText().toString().equals("un") && password.getText().toString().equals("pw")) {
                    Toast.makeText(getApplicationContext(), "Redirecting...", Toast.LENGTH_SHORT).show();
                    Intent to_open= new Intent(Log_In.this,Scan_WiFi.class);
                    startActivity(to_open);

                }
                else{ Toast.makeText(getApplicationContext(), "Wrong Credentials",Toast.LENGTH_SHORT).show(); }
            } }  );}



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log__in);
        username= (EditText) findViewById(R.id.UN);
        password= (EditText)findViewById(R.id.PW);
        login(but);
    }


}
