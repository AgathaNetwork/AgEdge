package cn.org.agatha;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class Main {
    private transient static Log log = LogFactory.getLog(Main.class);
    public static HashMap<String,String> criteria = new HashMap<String ,String >();
    public static void update(HashMap<String,String> criteria2) {
        Main.criteria = criteria2;
    }
    public static void main(String[] args) {
        try {

            //获取本地监听端口、远程IP和远程端口
            //int localPort = Integer.parseInt(args[0].trim());
            //String remoteIp = args[1].trim();
            //int remotePort = Integer.parseInt(args[2].trim());
            int localPort = <监听端口>;
            String remoteIp = <远程IP>;
            int remotePort = <远程端口>;
            Refresher ref = new Refresher();
            ref.start();
            //启动本地监听端口
            ServerSocket serverSocket = new ServerSocket(localPort);
            log.info("localPort="+localPort + ";remoteIp=" + remoteIp +
                    ";remotePort="+remotePort+";启动本地监听端口" + localPort + "成功！");

            while(true){
                Socket clientSocket = null;
                Socket remoteServerSocket = null;
                try {
                    //获取客户端连接
                    clientSocket = serverSocket.accept();
                    //建立远程连接
                    remoteServerSocket = new Socket(remoteIp ,remotePort);
                    String clientIP = clientSocket.getLocalAddress().toString();
                    clientIP = clientIP.substring(clientIP.lastIndexOf("/")+1);
                    log.info("客户端IP："+clientIP);
                    //启动数据转换接口
                    (new TransPortData(clientSocket ,remoteServerSocket ,"1")).start();
                    (new TransPortData(remoteServerSocket ,clientSocket,"2")).start();
                } catch (Exception ex) {
                    log.info("",ex);
                }
                //建立连接远程
            }
        } catch (Exception e) {
            log.error("",e);
        }
    }

}