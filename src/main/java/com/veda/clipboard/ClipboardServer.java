package com.veda.clipboard;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author derick.jin 2020-08-13 21:44:00
 * @version 1.0
 **/
public class ClipboardServer {

    private BlockingQueue<String> contentQueue = new ArrayBlockingQueue<>(32);

    public ClipboardServer(int port){
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        Object lock = new Object();

        clipboard.addFlavorListener(e -> {
            synchronized (lock) {
                lock.notify();
            }
        });

        Set<Socket> acceptSocket = new CopyOnWriteArraySet<>();
        new Thread(() -> {
            try {
                ServerSocket server = new ServerSocket(port);
                while (true) {
                    Socket accept = server.accept();
                    System.out.println("accept client connect : " + accept.getLocalAddress());
                    acceptSocket.add(accept);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            while (true) {
                try {
                    String content = contentQueue.take();
                    System.out.println(content);
                    byte[] bytes = content.getBytes();
                    for (Socket socket : acceptSocket) {
                        System.out.println(socket.getLocalAddress());
                        if (socket.isConnected()) {
                            try {
                                socket.getOutputStream().write(SocketClient.intToByteArray(bytes.length));
                                socket.getOutputStream().write(bytes);
                                socket.getOutputStream().flush();
                            } catch (Exception e) {
                                acceptSocket.remove(socket);
                                e.printStackTrace();
                            }
                        } else {
                            acceptSocket.remove(socket);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        try {
            System.out.println("Listen IP:" + InetAddress.getLocalHost().getHostAddress() +" Port:" + port + ", ClipboardServer Running...");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        String lastContent = null;
        while (true) {
            synchronized (lock) {
                try {
                    if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                        String thisContent = (String)clipboard.getData(DataFlavor.stringFlavor);
                        if (lastContent == null || !lastContent.equals(thisContent)) {
                            lastContent = thisContent;
                            contentQueue.offer(thisContent);
                        }
                    }
                    lock.wait(25);
                } catch (Exception e) {
                    try {
                        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }
}
