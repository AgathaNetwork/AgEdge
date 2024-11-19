[原文]([https://github.com/bangbang93/minecraft-proxy](https://hyp.ink/index.php/archives/773/)) 
本文介绍一种部署在边缘节点的修改Minecraft服务器列表MOTD信息的方法。
图中MikeWu597，即软件实现的基于IP地址实时判定游戏ID的方法。
感谢bangbang93于2021年提供的思路： [GitHub Repo minecraft-proxy](https://github.com/bangbang93/minecraft-proxy) 

------------

程序启动时创建监听器流程：
```java
            int localPort = <本地监听端口>;
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
```

------------

TransPortData库对数据进行处理，流程如下：
```java
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
                            for(int j=offset;j<offset+editBytes.length;j++)data[j]=editBytes[j-offset];
                        }
                    }
                    System.out.println(new String(data));
                    //{"text":"DMS-Success","color":"green"},"
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
```
其中通过一个if语句快速执行对MOTD数据包的判定（即头文字"version"），检测到MOTD信息发送时立即劫持修改数据。修改时，通过服务端预设的关键字（此处为"nvv"）确定修改起始位置，预留足够多的空格避免超长。如需彻底解决此问题，需要像上述bangbang93仓库中代码一样完全重新构建一个MOTD数据包。这里为了降低复杂度、便于劫持，仅做了简单的修改功能。

检测到MOTD后，用putSocket取客户端IP，与预存的键值表HashMap比对，确认是否有预先注册过的用户IP，如有，则注入相应绿色标识，否则注入灰色标识。


------------

程序通过一个单独线程定期从授权服务器拉取完整HashMap，更新授权库。
