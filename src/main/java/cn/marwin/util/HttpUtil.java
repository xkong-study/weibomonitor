package cn.marwin.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HttpUtil {

    public static String request(String url) throws IOException, HttpException {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(1000, TimeUnit.SECONDS)
                .readTimeout(1500, TimeUnit.SECONDS)
                //.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("", 8888)))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("cookie", "4744457510982225")
                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.2 Safari/605.1.15")
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new HttpException("HTTP REQUEST ERROR " + response.code());
        }

        return response.body().string();
    }
}
