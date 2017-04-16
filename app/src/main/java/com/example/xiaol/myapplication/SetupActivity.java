package com.example.xiaol.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by xiaol on 2017/2/25.
 */

public class SetupActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 隐藏标题栏
        getSupportActionBar().hide();

        setContentView(R.layout.activity_setup);

        Intent intent = getIntent();
        String ip_addr = intent.getStringExtra("ip_addr");
        int port = intent.getIntExtra("port", -1);

        if (ip_addr != null && port != -1) {
            ((EditText) findViewById(R.id.ip_text)).setText(ip_addr);
            ((EditText) findViewById(R.id.port_text)).setText(String.valueOf(port));
        }

        ((Button)findViewById(R.id.confirm_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("SetupActivity", "confirm_button");
                Intent intent = new Intent(SetupActivity.this, MainActivity.class);

                Editable str_ip = ((EditText)findViewById(R.id.ip_text)).getText();
                Editable str_port = ((EditText)findViewById(R.id.port_text)).getText();

                if ((str_ip.length() > 0) && (str_port.length() > 0)) {
                    String ip_addr = str_ip.toString();
                    int port = Integer.valueOf(str_port.toString());

                    SharedPreferences.Editor edit = getSharedPreferences("data", MODE_PRIVATE).edit();
                    edit.putString("ip_addr", ip_addr);
                    edit.putInt("port", port);
                    edit.apply();

                    intent.putExtra("ip_addr", ip_addr);
                    intent.putExtra("port", port);

                } else {
                    Toast.makeText(SetupActivity.this, "地址和端口不能为空", Toast.LENGTH_SHORT).show();

                    return;
                }

                setResult(RESULT_OK, intent);
                finish();
            }
        });

        ((Button)findViewById(R.id.cancel_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("SetupActivity", "cancel_button");
                Intent intent = new Intent(SetupActivity.this, MainActivity.class);

                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
    }
}
