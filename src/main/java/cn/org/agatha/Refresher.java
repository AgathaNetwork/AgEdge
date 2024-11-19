package cn.org.agatha;

import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

public class Refresher implements Runnable {
    private Thread t;
    Refresher() {

    }
    public JSONObject json = JSONObject.parseObject("{}");
    public void run() {
        while(true){
            System.out.println("正在刷新许可列表"+new Date(System.currentTimeMillis()).toString());
            URL url = null;
            try {
                url = new URL("<许可证授权URL>");
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                con.setRequestMethod("GET");
            } catch (ProtocolException e) {
                throw new RuntimeException(e);
            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String inputLine;
            StringBuffer content = new StringBuffer();
            while (true) {
                try {
                    if (!((inputLine = in.readLine()) != null)) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                content.append(inputLine);
            }
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            con.disconnect();
            String cnt = content.toString();
            json = (JSONObject) JSONObject.parseObject(cnt);
            HashMap<String, String> hc = new HashMap<>();
            for (HashMap.Entry<String, Object> entry : json.entrySet()) {
                hc.put(entry.getKey(), entry.getValue().toString());
            }
            Main.update(hc);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }
}
