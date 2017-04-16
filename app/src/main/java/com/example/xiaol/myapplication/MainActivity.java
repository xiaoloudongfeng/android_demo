package com.example.xiaol.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;
import java.util.Locale;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    private String IP_ADDR = "";
    private int PORT = -1;

    public static final int UPDATE_TEXT = 1;
    public static final int UPDATE_FAIL = 2;

    private String recvStr;
    private long lasttime;

    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            String weather = "";
            double temperature = -1;
            double humidity = -1;
            double cpu_usage = -1;
            double mem_usage = -1;
            String temp_hum_stat = "";
            String weather_stat = "";

            TextView text = (TextView)findViewById(R.id.text1);
            MyChart chart = (MyChart) findViewById(R.id.chart);
            chart.setYLimit(30);
            chart.setYStr("℃");
            chart.setYInterval(5);

            MyChart chart1 = (MyChart)findViewById(R.id.chart1);
            chart1.setYLimit(80);
            chart1.setYStr("%");
            chart1.setYInterval(10);

            switch (msg.what) {
                case UPDATE_TEXT:
                    if (recvStr != null) {
                        Log.e("handlerMessage", recvStr);
                        try {
                            JSONObject jsonObject = new JSONObject(recvStr);

                            weather = jsonObject.getString("weather");
                            temperature = jsonObject.getDouble("temperature");
                            humidity = jsonObject.getDouble("humidity");
                            cpu_usage = jsonObject.getDouble("cpu_usage");
                            mem_usage = jsonObject.getDouble("mem_usage");
                            temp_hum_stat = jsonObject.getString("temp_hum_stat");
                            weather_stat = jsonObject.getString("weather_stat");

                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }

                    Log.e("handlerMessage", "UPDATE_TEXT");

                    text.setText(
                            "IP[" + IP_ADDR + ":" + PORT + temp_hum_stat + weather_stat +"]\n" +
                            "天气[" + weather + "]\n" +
                            "室温[" + temperature + "℃] 湿度[" + humidity + "%]\n" +
                            "CPU[" + String.format(Locale.CHINA, "%.2f", cpu_usage) + "%] " +
                            "内存[" + String.format(Locale.CHINA, "%.2f", mem_usage) + "%]\n"
                    );

                    Log.e("handlerMessage", String.valueOf(new Date().getTime() / 1000));

                    if (new Date().getTime() / 1000 - lasttime >= 60) {
                        lasttime = new Date().getTime() / 1000;
                        chart.onTempRefresh((float)temperature);
                        chart1.onTempRefresh((float)humidity);
                    }

                    break;

                case UPDATE_FAIL:
                    text.setText(recvStr);
                    break;

                default:
                    break;
            }
        }

    };

    private Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            HttpURLConnection urlConnection = null;
            for ( ;; ) {
                Log.e("Thread", "for begin");

                try {
                    String http_url = "http://" + IP_ADDR + ":" + String.valueOf(PORT);
                    Log.e("Thread", "http_url: " + http_url);

                    URL url = new URL(http_url);

                    // 打开连接
                    urlConnection = (HttpURLConnection)url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(5000);
                    urlConnection.setReadTimeout(5000);

                    // 获取输入流
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"));

                    // 读取json
                    String line = null;
                    StringBuilder sb = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }

                    Log.e("Thread", sb.toString());

                    recvStr = sb.toString();

                    Message message = new Message();
                    message.what = UPDATE_TEXT;
                    handler.sendMessage(message);

                } catch (Exception e) {
                    recvStr = e.getMessage() != null ? e.getMessage() : "未知错误";
                    Log.e("http客户端异常", recvStr);

                    Message message = new Message();
                    message.what = UPDATE_FAIL;
                    handler.sendMessage(message);

                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }

                try {
                    Thread.sleep(1000);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("onActivityResult", "*******enter onActivityResult******");
        switch (requestCode) {
        case 1:
            if (resultCode == RESULT_OK) {
                String ip_addr = data.getStringExtra("ip_addr");
                int port = data.getIntExtra("port", -1);

                Log.e("onActivityResult", ip_addr + String.valueOf(port));

                if ((ip_addr != null) && (port != -1)) {
                    IP_ADDR = ip_addr;
                    PORT = port;
                }
            }
            break;

        default:
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 隐藏标题栏
        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.e("onStart", "onStart");

        if (IP_ADDR.equals("") || PORT == -1) {
            SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
            IP_ADDR = pref.getString("ip_addr", "");
            PORT = pref.getInt("port", -1);

            Log.e("onStart", IP_ADDR + ":" + PORT);

            if (IP_ADDR.equals("") || PORT == -1) {
                Intent setup_intent = new Intent(MainActivity.this, SetupActivity.class);
                startActivityForResult(setup_intent, 1);
            }
        }

        ((Button)findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SetupActivity.class);

                intent.putExtra("ip_addr", IP_ADDR);
                intent.putExtra("port", PORT);

                startActivityForResult(intent, 1);
            }
        });

        Log.e("onStart", "onStart done");

        if (thread.getState() == Thread.State.NEW) {
            thread.start();
        }
    }
}
