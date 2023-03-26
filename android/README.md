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
$ npm install --no-package-lock ../lib-jmeet/lib-jitsi-meet-version.tgz
```
