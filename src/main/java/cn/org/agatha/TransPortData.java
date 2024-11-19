package cn.org.agatha;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static cn.org.agatha.Main.criteria;

public class TransPortData extends Thread {

    private transient static Log log = LogFactory.getLog(Main.class);

    Socket getDataSocket;
    Socket putDataSocket;

    String type;

    public TransPortData(Socket getDataSocket , Socket putDataSocket ,String type){
        this.getDataSocket = getDataSocket;
        this.putDataSocket = putDataSocket;
        this.type = type;
    }

    public void run(){
        try {
            while(true){

                InputStream in = getDataSocket.getInputStream() ;
                OutputStream out = putDataSocket.getOutputStream() ;
                //读入数据
                byte[] data = new byte[2048];
                int readlen = in.read(data);

                //如果没有数据，则暂停
                if(readlen<=0){
                    Thread.sleep(100);
                    continue;
                }
                System.out.println(data[7]);
                //118, 101, 114, 115, 105, 111, 110 version
                if(data[7]==118 && data[8]==101 && data[9]==114 && data[10]==115 && data[11]==105 && data[12]==111 && data[13]==110){
                    for(int i=0;i<data.length-2;i++){
                        if (data[i]==110 && data[i+1]==118 && data[i+2]==118){
                            int offset=i-2;
                            String ip = putDataSocket.getInetAddress().toString();
                            String editStr=("❌不支持自动登录\",\"color\":\"gray\"},{\"text\":\"");
                            for (HashMap.Entry<String, String> entry : criteria.entrySet()) {
                                System.out.println(entry.getValue());
                                if(("/"+entry.getKey()).equalsIgnoreCase(ip))editStr=("✔ "+entry.getValue()+"\",\"color\":\"green\",\"bold\":true},{\"text\":\"");
                            }
                            byte[] editBytes=editStr.getBytes();
                            for(int j=offset;j<offset+editBytes.length;j++)data[j]=editBytes[j-offset];//注入
                        }
                    }
                    System.out.println(new String(data));
                    //offset-2
                    out.write(data ,0,readlen);
                }
                else out.write(data ,0,readlen);



                out.flush();
            }
        } catch (Exception e) {
            log.error("type:"+type,e);
        }
        finally{
            //关闭socket
            try {
                if(putDataSocket != null){
                    putDataSocket.close();
                }
            } catch (Exception exx) {
            }

            try {
                if(getDataSocket != null){
                    getDataSocket.close();
                }
            } catch (Exception exx) {
            }
        }
    }

}