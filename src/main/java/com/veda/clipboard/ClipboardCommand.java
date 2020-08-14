package com.veda.clipboard;

/**
 * @author derick.jin 2020-08-13 21:44:00
 * @version 1.0
 **/
public class ClipboardCommand {
    public static void main(String[] args) {
        System.out.println("Veda Clipboard Synchronize Tool v1.0 - Derick.Jin 20200814");
        System.out.println("GitHub: https://github.com/HelloSetsuna/clipboard-sync");
        System.out.println("==========================================================");
        System.out.println("* --server \n\tStart as ClipboardServer, other ClipboardClient could synchronize clipboard From server.");
        System.out.println("* --client \n\tStart as ClipboardClient, connect to ClipboardServer and synchronize clipboard to this computer");
        System.out.println("  --listen [port:5000]\n\tClipboardServer listen which port");
        System.out.println("* --connect-host [host]\n\tClipboardClient connect to which host");
        System.out.println("  --connect-port [port:5000]\n\tClipboardClient connect to which port");
        System.out.println("==========================================================");
        System.out.println("剪贴板单向同步, 即从 A 电脑 同步到 B 电脑, A 电脑启动服务端, B 电脑启动客户端");
        System.out.println("启动剪贴板服务端：\n\tjava -jar clipboard-sync.jar --server --listen 5000");
        System.out.println("启动剪贴板客户端：\n\tjava -jar clipboard-sync.jar --client --connect-host 192.168.1.A --connect-port 5000");
        System.out.println("==========================================================");
        System.out.println("剪贴板双向同步, 即 A 电脑 和 B 电脑 互相同步, A 和 B 电脑均启动客户端和服务端");
        System.out.println("启动客户端服务端：\n\tjava -jar clipboard-sync.jar --server --listen 5000 --client --connect-host 192.168.1.B --connect-port 5000");
        System.out.println("启动服务端客户端：\n\tjava -jar clipboard-sync.jar --client --connect-host 192.168.1.A --connect-port 5000 --server --listen 5000");

        boolean isServerBootstrap = false;
        boolean isClientBootstrap = false;
        int listenPort = 5000;
        String connectHost = null;
        int connectPort = 5000;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--server".equals(arg)) {
                isServerBootstrap = true;
            }
            if ("--client".equals(arg)) {
                isClientBootstrap = true;
            }
            if ("--listen".equals(arg)) {
                listenPort = Integer.parseInt(args[++i]);
            }
            if ("--connect-host".equals(arg)) {
                connectHost = args[++i];
            }
            if ("--connect-port".equals(arg)) {
                connectPort = Integer.parseInt(args[++i]);
            }
        }

        if (isClientBootstrap) {
            if (connectHost == null || "".equals(connectHost.trim())) {
                throw new IllegalArgumentException("--connect-host is required");
            }
            // 服务端和客户端均启动时 单开线程启动客户端
            if (isServerBootstrap) {
                String host = connectHost;
                int port = connectPort;
                new Thread(() -> new ClipboardClient(host, port)).start();
            } else {
                new ClipboardClient(connectHost, connectPort);
            }
        }

        if (isServerBootstrap) {
            new ClipboardServer(listenPort);
        }
    }
}
