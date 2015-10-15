package com.lincanbin.carbonforum.tools;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

import com.lincanbin.carbonforum.config.APIAddress;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by 灿斌 on 5/18/2015.
 */
public class VerificationCode {
    Context context;
    public VerificationCode(Context context){this.context = context;}

    public void loadImage(final ImageCallBack callBack){

        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Drawable drawable = (Drawable) msg.obj;
                callBack.getDrawable(drawable);
            }
        };

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    URL localURL = new URL(APIAddress.VERIFICATION_CODE);
                    URLConnection connection = localURL.openConnection();
                    HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
                    InputStream inputStream = null;
                    inputStream = httpURLConnection.getInputStream();
                    //获取Cookie
                    String headerName=null;
                    for (int i=1; (headerName = connection.getHeaderFieldKey(i))!=null; i++) {
                        if (headerName.equals("Set-Cookie")) {
                            String cookie = connection.getHeaderField(i);
                            //cookie = cookie.substring(0, cookie.indexOf(";"));
                            //String cookieName = cookie.substring(0, cookie.indexOf("="));
                            //String cookieValue = cookie.substring(cookie.indexOf("=") + 1, cookie.length());
                            //将Cookie保存起来
                            SharedPreferences mySharedPreferences= context.getSharedPreferences("Session",
                                    Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = mySharedPreferences.edit();
                            editor.putString("Cookie", cookie);
                            editor.apply();
                        }
                    }
                    Drawable drawable = Drawable.createFromStream(inputStream, "");
                    Message message = Message.obtain();
                    message.obj = drawable;
                    handler.sendMessage(message);

                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public interface ImageCallBack{
        void getDrawable(Drawable drawable);
    }
}
