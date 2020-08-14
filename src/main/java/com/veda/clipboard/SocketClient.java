package com.veda.clipboard;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * 原旧系统的 ClientSocket
 * 使得原先的 C 命名改为 Java 格式的命名 添加支持 Closeable 接口
 * @author derick.jin 2019-11-07 19:48:00
 * @version 1.0
 **/
public class SocketClient implements Closeable {
    // 链接失败后重试次数
    private static final int RETRY_COUNT = 2;

    // 集成的socket对象，表示实际的socket连接
    private Socket socket;

    private String localIpAddress;
    private int localPort;
    private String remoteIpAddress;
    private int remotePort;
    private int timeOut;

    public SocketClient() throws IOException {
        // 创建一未连接的客户端Socket
        this.socket = new Socket();
    }

    /**
     * 构造函数，一般用于服务端接受客户端连接后用新接受的连接创建ClientSocket对象来进行收、发操作
     * @param socket 已建立连接的java.net.Socket对象
     */
    public SocketClient(Socket socket) {
        this.socket = socket;
    }

    /**
     * 尝试重新创建链接
     * 如果创建失败将 尝试 RETRY_COUNT 次
     */
    public boolean reConnect(){
        return reConnect(RETRY_COUNT);
    }

    public boolean reConnect(int retryCount){
        if (retryCount < 0) {
            return false;
        }
        try {
            if (!isClosed()) {
                close();
            }
            socket = new Socket();
            connect(localIpAddress, localPort, remoteIpAddress, remotePort, timeOut);
//            log.info("SocketClient reConnect:{}:{} success", remoteIpAddress, remotePort);
            return true;
        } catch (Exception e) {
//            log.error(String.format("SocketClient reConnect:%s:%d failed count:%d", remoteIpAddress, remotePort, RETRY_COUNT - retryCount + 1), e);
            return reConnect(retryCount - 1);
        }
    }

    /**
     * 向 (StrIPAddr, nPort) 发起TCP/IP连接
     * @param localIpAddress !=null时为要绑定的本地IP地址
     * @param localPort > 0 时为要绑定的本地端口
     * @param remoteIpAddress 服务方IP地址
     * @param remotePort 服务方端口
     * @param timeOut 建立连接的超时时间（以毫秒为单位）
     * @throws IOException
     */
    public void connect(String localIpAddress, int localPort, String remoteIpAddress, int remotePort, int timeOut) throws IOException {
        this.localIpAddress = localIpAddress;
        this.localPort = localPort;
        this.remoteIpAddress = remoteIpAddress;
        this.remotePort = remotePort;
        this.timeOut = timeOut;

        this.socket.setKeepAlive(true);
        SocketAddress localAddress = null;
        SocketAddress remoteAddress = null;
        try {
            // 检查是否需要绑定本地IP地址和本地端口
            if (localIpAddress != null && !"".equals(localIpAddress.trim())) {
                if (0 < localPort) {
                    // 绑定本地IP地址和本地端口
                    localAddress = new InetSocketAddress(localIpAddress, localPort);
                } else {
                    // 仅绑定本地IP地址，不绑定本地端口
                    localAddress = new InetSocketAddress(localIpAddress, 0);
                }
            }
            // 不对本地IP地址和端口作限制
            remoteAddress = new InetSocketAddress(remoteIpAddress, remotePort);
        }
        catch (IllegalArgumentException IAe) {
            throw new IOException("INetSocketAddress parameter(s) is invalid");
        }

        if (null != localAddress) {
            // 进行本地绑定
            this.socket.bind(localAddress);
        }

        this.socket.connect(remoteAddress, timeOut);
    }

    /**
     * 关闭与对方的Socket连接
     *
     */
    public void close() {
        if (null != this.socket) {
            try {
                this.socket.close();
            } catch (IOException IOe) {
                // Nothing to do
            }
        }
    }

    /**
     * 判断Socket连接是否为已连接状态
     *
     * @return true 已连接 false 未连接
     */
    public boolean isConnected() {
        if (null != this.socket) {
            return this.socket.isConnected();
        }
        return false;
    }

    /**
     * 判断Socket连接是否为已关闭状态
     *
     * @return true 已关闭 false 未关闭
     */
    public boolean isClosed() {
        if (null != this.socket) {
            return this.socket.isClosed();
        }
        return true;
    }

    /**
     * 接收指定长度为nLenToRead的字节数据
     *
     * @param byteLength 要接收的数据的长度
     * @param timeout 接收数据的超时时间（以毫秒为单位）
     * @return 接收到的 byteLength 字节数据数组
     */
    public byte[] receive(int byteLength, int timeout) throws IOException
    {
        // 检查连接的有效性
        if (null == this.socket || this.socket.isClosed()) {
            throw new IOException("Socket is invalid or not connected");
        }
        // 存储要接收的数据的缓冲空间
        byte[] baData = new byte[byteLength];
        int nRecv = 0;
        int nOffset = 0;
        // 在要接收的数据比较长的情况下，必须循环多次才能保证数据接收完整
        this.socket.setSoTimeout(timeout);
        InputStream inStream = this.socket.getInputStream();
        while (0 < byteLength) {
            nRecv = inStream.read(baData, nOffset, byteLength);
            if (0 >= nRecv) {
                break;
            }
            nOffset += nRecv;
            byteLength -= nRecv;
        }
        return baData;
    }

    public byte[] receive(int timeout) throws IOException {
        byte[] receiveLength = receive(4, timeout);
        return receive(byteArrayToInt(receiveLength), timeout);
    }


    /**
     * 向对方发送字节数组 body 中的全部字节数据
     *
     * @param header 要发送的消息头
     * @param body 要发送的消息体
     * @param timeout 发送数据的超时时间（以毫秒为单位）
     */
    public void send(byte[] header, byte[] body, int timeout) throws IOException {
        // 检查连接的有效性
        if (null == this.socket || this.socket.isClosed()) {
            throw new IOException("Socket is invalid or not connected");
        }
        this.socket.setSoTimeout(timeout);
        OutputStream outStream = this.socket.getOutputStream();
        // 发送报文长度
        outStream.write(intToByteArray(header.length + body.length));
        // 写入报文头
        outStream.write(header, 0, header.length);
        // 写入报文体
        outStream.write(body, 0, body.length);
        outStream.flush();
    }


    /**
     * 获取对方的IP地址
     *
     * @return IP地址或者长度为 0 的字符串
     */
    public String getPeerHost() {
        if (null != this.socket && this.socket.isConnected()) {
            InetAddress addrPeer = this.socket.getInetAddress();
            if (null != addrPeer) {
                return addrPeer.getHostAddress();
            }
        }
        return "";
    }

    /**
     * int到byte[] 由高位到低位
     * @param i 需要转换为byte数组的整行值。
     * @return byte数组
     */
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);
        return result;
    }

    /**
     * byte[]转int
     * @param bytes 需要转换成int的数组
     * @return int值
     */
    public static int byteArrayToInt(byte[] bytes) {
        int value=0;
        for(int i = 0; i < 4; i++) {
            int shift= (3-i) * 8;
            value +=(bytes[i] & 0xFF) << shift;
        }
        return value;
    }

    public static byte[] longToByteArray(long num) {
        byte[] byteNum = new byte[8];
        for (int ix = 0; ix < 8; ++ix) {
            int offset = 64 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }
        return byteNum;
    }

    public static long byteArrayToLong(byte[] byteNum) {
        long num = 0;
        for (int ix = 0; ix < 8; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }
        return num;
    }
}
