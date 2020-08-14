# clipboard-sync
A tool to synchronize the clipboards between computers in LAN / 局域网内电脑之间剪贴板同步工具 

## 使用说明
首先电脑上需要具有 JRE / JDK 才可使用该工具, 对此不再赘述, 使用时直接使用项目根目录下的 `clipboard-sync.jar` 即可.

##### 剪贴板单向同步, 即从 A 电脑 同步到 B 电脑, A 电脑启动服务端, B 电脑启动客户端
1. 启动剪贴板服务端：`java -jar clipboard-sync.jar --server --listen 5000`
2. 启动剪贴板客户端：`java -jar clipboard-sync.jar --client --connect-host 192.168.1.A --connect-port 5000`

当需要将 A 电脑的剪切板同步到多台电脑上时, 只需在 A 电脑上启动服务端, 其它电脑上启动客户端即可

##### 剪贴板双向同步, 即 A 电脑 和 B 电脑 互相同步, A 和 B 电脑均启动客户端和服务端
1. 启动客户端服务端：`java -jar clipboard-sync.jar --server --listen 5000 --client --connect-host 192.168.1.B --connect-port 5000`
2. 启动服务端客户端：`java -jar clipboard-sync.jar --client --connect-host 192.168.1.A --connect-port 5000 --server --listen 5000`


## 参数说明
执行 `java -jar clipboard-sync.jar` 后追加以下参数:
```shell script
* --server
    Start as ClipboardServer, other ClipboardClient could synchronize clipboard From server.
* --client
    Start as ClipboardClient, connect to ClipboardServer and synchronize clipboard to this computer
  --listen [port:5000]
    ClipboardServer listen which port
* --connect-host [host]
    ClipboardClient connect to which host
  --connect-port [port:5000]
    ClipboardClient connect to which port
```

## 脚本启动
在 CMD 中使用 java -jar 命令启动该工具会使得命令行一直占有窗口, 因此提供了脚本可在后台运行该工具, 可查看项目根目录下的 `start-clipboard-client.bat` 和 `start-clipboard-server.bat` 脚本的内容, 修改 脚本里的 host 后将其和 jar 包放在一起去运行.

只需根据需要修改该脚本末尾的 java -jar clipboard-sync.jar 命令 后的参数即可, 注意不用重复启动客户端脚本, 当需要设置开机自动重启时需将脚本的快捷方式放到 系统用户目录下的 `\AppData\Roaming\Microsoft\Windows\Start Menu\Programs\Startup` 目录下即可.