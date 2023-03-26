# JMeet 安卓 SDK

参考文档： [The Handbook](https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-android-sdk).

## 使用源码构建SDK

如果要使用从源代码构建的SDK，则编写包含这些依赖的本地Maven存储库可能会受益。为了便于说明，我们将此本地Maven存储库的位置定义为`/tmp/repo`

在源代码形式中，Andriod SDK依赖项由JMeet项目锁定/固定的。要获取数据，请在jmeet项目目录中执行NPM：`package.json` `package-lock.json`

```shell
npm install
```

这将以二进制格式或源代码格式在/node_modules/下的某处引入依赖项

JMeet SDK for Andriod依赖的第三方React Native模块由NPM以源码或二进制形式下载。这些需要组装成Maven工件，然后发布到本地Maven存储库。这里提供了一个脚本来促进这一点。从jmeet项目存储库的根目录运行：

```shell
./android/scripts/release-sdk.sh /tmp/repo
```

这将构建SDK并将其所有依赖项发布到`/tmp/repo`本示例中指定的Maven存储库()。

现在您可以使用工件了。在您的项目中，将您在上面使用的Maven存储库`/tmp/repo`添加到您的顶级`build.gradle`文件中：

```properties
allprojects {
    repositories {
        maven { url "file:/tmp/repo" }
        google()
        mavenCentral()
        maven { url 'https://www.jitpack.io' }
    }
}
```

maven { url "https://github.com/jitsi/jitsi-maven-repository/raw/master/releases" }当您发布所有子项目时，您可以使用本地存储库替换 Jitsi 存储库 ( ) 。如果您没有这样做，则必须添加这两个存储库。确保首先列出您的本地存储库！

然后，将依赖项定义org.jitsi.react:jitsi-meet-sdk到build.gradle模块的文件中：

```properties
implementation ('org.jitsi.react:jitsi-meet-sdk:+') { transitive = true }
```

编译SDK过程中可能会出现找不到lib-jitsi-meet模块错误：`Error: Unable to resolve module lib-jitsi-meet`

```shell
# 您需要将lib-jitsi-meet模块依赖导入到node_module中

# 首先尝试通过install-local直接导入lib-jitsi-meet模块依赖
# 切换工作目录到meet目录
$ cd meet
# 直接安装lib-jmeet模块依赖
$ install-local ../lib-jmeet/

# 如果直接安装失败则可以尝试使用以下方式进行手动安装
# 切换工作目录到lib-jmeet目录
$ cd lib-jmeet
# 安装依赖
$ npm install
# 项目打包
$ npm run build
# 打包依赖，生成lib-jitsi-meet-version.tgz包
$ npm pack

# 切换工作目录到jmeet目录
$ cd jmeet
# 安装lib-jitsi-meet模块依赖
$ npm i --no-save --no-package-lock ../lib-jmeet/lib-jitsi-meet-version.tgz

# tip：如果报 npm ERR! notarget No matching version found for yallist@3.0.2. 异常,
# 建议尝试不使用私有仓库再进行install。
```

## 通过 android studio 运行

如果您想通过`android studio`使用`debug`运行app：
1. 生成debug.keystore到jmeet/android/app目录，生成配置在jmeet/android/keystores目录；
2. 切换工作目录到jmeet目录，运行`npx react-native start`命令将js代码加载到debug app中，否则app无法正常运行。

### 调试环境下wss连接关闭ssl证书校验

1. 使用react-native（这里使用的版本就是0.68.5，以此为例）源码进行编译，直接去[react-native仓库](https://github.com/facebook/react-native.git)下载源代码；
2. `npm install`安装依赖；
3. 依赖安装完成后编译一遍项目，编译完成后保证代码不报错;
    ```shell
    # linux环境
    $ ./gradlew clean assebleDebug
    # window环境
    $ gradlew.bat clean assebleDebug
    ```
4. `Ctrl+N`搜索`WebSocketModule.java`源码，找到`connect`方法，修改`OkHttpClient.Builder()`关闭`ssl`证书校验;
   ```java
    OkHttpClient client = new OkHttpClient.Builder()
        .sslSocketFactory(getSSLSocketFactory(), getX509TrustManager()) // 通过sslSocketFactory方法设置https证书
        .hostnameVerifier(getHostnameVerifier())
        .cookieJar(new ReactCookieJarContainer())
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MINUTES) // Disable timeouts for read
        .build();

    public static SSLSocketFactory getSSLSocketFactory() {
        try {
          SSLContext sslContext = SSLContext.getInstance("SSL");
          sslContext.init(null, getTrustManager(), new SecureRandom());
          return sslContext.getSocketFactory();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

      private static TrustManager[] getTrustManager() {
        TrustManager[] trustAllCerts = new TrustManager[]{
          new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
              return new X509Certificate[]{};
            }
          }
        };
        return trustAllCerts;
      }

      public static HostnameVerifier getHostnameVerifier() {
        HostnameVerifier hostnameVerifier = (s, sslSession) -> true;
        return hostnameVerifier;
      }

      public static X509TrustManager getX509TrustManager() {
        X509TrustManager x509TrustManager = new X509TrustManager() {
          //检查客户端的证书是否可信
          @Override
          public void checkClientTrusted(X509Certificate[] chain, String authType) {

          }
          //检查服务器端的证书是否可信
          @Override
          public void checkServerTrusted(X509Certificate[] chain, String authType) {

          }

          @Override
          public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
          }
        };
        return x509TrustManager;
      }
   ```
5. 打包aar，如果直接在项目中依赖RN的源码，不是一个很好的选择，更好的方式是将RN源码编译打包为一个aar文件，然后在Android项目中使用，打包aar的命令如下：
   ```shell
   # linux环境
   ./gradlew :ReactAndroid:installArchives --no-daemon
   # window环境
   $ gradlew.bat :ReactAndroid:installArchives --no-daemon
   ```
6. 将本地生成的android目录直接替换到jmeet/node_module/reactive/android目录即可。

## 生成发行APK包

只需在终端运行以下命令：

```shell
$ cd jmeet/android
$ ./gradlew assembleRelease
```
