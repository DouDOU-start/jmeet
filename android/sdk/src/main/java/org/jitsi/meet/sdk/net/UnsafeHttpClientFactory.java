package org.jitsi.meet.sdk.net;

import com.facebook.react.modules.network.OkHttpClientFactory;
import com.facebook.react.modules.network.ReactCookieJarContainer;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class UnsafeHttpClientFactory implements OkHttpClientFactory {
    @Override
    public OkHttpClient createNewNetworkModuleClient() {
//        OkHttpUtil.SSLParams sslParams = OkHttpUtil.getSslSocketFactory(null,null, null);
//        OkHttpClient.Builder client = new OkHttpClient.Builder()
//            //这两个是信任所有https 证书，包括自签证书
//            .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
//            .hostnameVerifier(new HostnameVerifier() {
//                @Override
//                public boolean verify(String hostname, SSLSession session) {
//                    return true;
//                }
//            })
//            //这三个不是必须的
//            .connectTimeout(0, TimeUnit.MILLISECONDS)
//            .readTimeout(0, TimeUnit.MILLISECONDS)
//            .writeTimeout(0, TimeUnit.MILLISECONDS)
//            //这个是必须的
//            .cookieJar(new ReactCookieJarContainer());
//
//        return OkHttpClientProvider.enableTls12OnPreLollipop(client).build();

//        return OkHttpUtil.getHttpsClient();

        return new OkHttpClient.Builder()
            .sslSocketFactory(getSSLSocketFactory(), getX509TrustManager()) // //通过sslSocketFactory方法设置https证书
            .hostnameVerifier(getHostnameVerifier())
            // .connectTimeout(0, TimeUnit.SECONDS)
	        // .writeTimeout(0, TimeUnit.SECONDS)
	        // .readTimeout(0, TimeUnit.SECONDS)
            .cookieJar(new ReactCookieJarContainer())
            .build();
    }

    public static SSLSocketFactory getSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, getTrustManager(), new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //获取TrustManager
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

    //获取HostnameVerifier，验证主机名
    public static HostnameVerifier getHostnameVerifier() {
        HostnameVerifier hostnameVerifier = (s, sslSession) -> true;
        return hostnameVerifier;
    }
   //X509TrustManager：证书信任器管理类
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
}
