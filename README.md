# lessDFS-java-client
访问LessDFS的客户端API

## 如何使用

先克隆完整代码到本地机器

```shell
$ git clone git@github.com:liaochente/lessDFS-java-client.git
```

复制一份配置文件（文件在src/main/resources/less-client.conf），主要配置有服务器地址、服务器端口、通讯密码

```shell
less.client.password=123456
less.client.server.address=127.0.0.1
less.client.server.port=8888
```

通过maven工具打包

```shell
mvn clean package
```

最后，把jar包整合到现有项目，并把less-client.conf文件放在自己项目的src/main/resources下；

## 代码实例

```java
//upload file
DefaultLessDFSClient client = DefaultLessDFSClient.newInstance();
String fileName = client.upload("/test.sh", "sh");

//download file
DefaultLessDFSClient client = DefaultLessDFSClient.newInstance();
InputStream inputStream = client.download("L0/00/01/c9fa2a2c4e0b46e58c7ad19872625ede");
byte[] fileBytes = client.download("L0/00/01/c9fa2a2c4e0b46e58c7ad19872625ede", (inputStream)->{
  byte[] bs = new byte[1];
  //处理输入流，获得文件二进制数组并返回
  return bs;
});
```

## 