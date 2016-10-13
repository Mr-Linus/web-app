package com.myapp;

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.*;

import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;

import android.content.pm.ActivityInfo;
import com.myapp.R;

public class myApp extends DroidGap
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	Boolean isInternetPresent=false;
    	super.onCreate(savedInstanceState);
    	this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//固定屏幕方向
    	//加载启动画面
    	super.setIntegerProperty("splashscreen",R.drawable.splash);
    	//复制资源到APP中，便于在网页中调用
    	//copytolocal();
    	//网络连接检测
    	ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
    	isInternetPresent = cd.isConnectingToInternet();
    	if (isInternetPresent) {    		
            super.loadUrl(Config.getStartUrl(),10000);
         // 检查软件更新
            //UpdateManager manager = new UpdateManager(myApp.this);
			//manager.checkUpdate();
    	} else {
           //网络连接失败
    		showAlertDialog(myApp.this, "网络不正常","网络无法连接");         
    	}            
    }
    //显示对话框提示
    public void showAlertDialog(Context context, String title, String message) {
    	new AlertDialog.Builder(context)
    	 .setTitle(title) 
    	 .setMessage(message)
    	 .setPositiveButton("退出",new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
            	 myApp.super.finish();
             }
         })
    	 .show();
    }
    private static final int IO_BUFFER_SIZE = 8 * 1024;
    //复制文件
    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }
 
 public void copytolocal()
 {
	//把一些文件放到APP目录
     try {
         for (String fileName : getAssets().list("www")) {
             if(fileName.lastIndexOf(".")>-1 && fileName.lastIndexOf("html")==-1){
             	Log.w(TAG, getFilesDir().getPath() + "/"+fileName+" length:"+fileName.lastIndexOf("."));
             	File outputFile = new File(getFilesDir().getPath() + "/" + fileName);
                 FileOutputStream out = new FileOutputStream(outputFile);
                 InputStream in = getAssets().open( "www/" +fileName);
                 // Transfer bytes from the input file to the output file
                 copy(in,out);
                 out.close();
                 in.close(); 
             }
         }
         //如果图像文件夹不存在，则创建一个图像文件夹
         File imgfolder = new File(getFilesDir().getPath() + "/images/");
         if(!imgfolder.exists())imgfolder.mkdir();
         for (String fileName : getAssets().list("images")) {              	
         	File outputFile = new File(getFilesDir().getPath() + "/images/" + fileName);                 
             FileOutputStream out = new FileOutputStream(outputFile);
             InputStream in = getAssets().open( "images/" +fileName);
             // Transfer bytes from the input file to the output file
             copy(in,out);
             out.close();
             in.close();                   	                    
         }      
     } catch (Exception e) {
         Log.e("Main", "复制文件错误", e);
     }
 }
}
