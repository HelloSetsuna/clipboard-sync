package com.veda.clipboard;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * @author derick.jin 2020-08-13 21:45:00
 * @version 1.0
 **/
public class ClipboardClient {

    public ClipboardClient(String ip, int port) {
        // 连接失败后重试
        while (true) {
            try {
                SocketClient socketClient = new SocketClient();
                socketClient.connect(null, 0, ip, port, 5000);
                System.out.println("Connect IP:" + ip + " Port:" + port + " Success, ClipboardClient is Running...");
                while (true) {
                    try {
                        byte[] receive = socketClient.receive(5000);
                        String content = new String(receive);
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(content), null);
                    } catch (Exception e) {}
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
