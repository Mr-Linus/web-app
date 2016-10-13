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
    	this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//�̶���Ļ����
    	//������������
    	super.setIntegerProperty("splashscreen",R.drawable.splash);
    	//������Դ��APP�У���������ҳ�е���
    	//copytolocal();
    	//�������Ӽ��
    	ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
    	isInternetPresent = cd.isConnectingToInternet();
    	if (isInternetPresent) {    		
            super.loadUrl(Config.getStartUrl(),10000);
         // ����������
            //UpdateManager manager = new UpdateManager(myApp.this);
			//manager.checkUpdate();
    	} else {
           //��������ʧ��
    		showAlertDialog(myApp.this, "���粻����","�����޷�����");         
    	}            
    }
    //��ʾ�Ի�����ʾ
    public void showAlertDialog(Context context, String title, String message) {
    	new AlertDialog.Builder(context)
    	 .setTitle(title) 
    	 .setMessage(message)
    	 .setPositiveButton("�˳�",new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
            	 myApp.super.finish();
             }
         })
    	 .show();
    }
    private static final int IO_BUFFER_SIZE = 8 * 1024;
    //�����ļ�
    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }
 
 public void copytolocal()
 {
	//��һЩ�ļ��ŵ�APPĿ¼
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
         //���ͼ���ļ��в����ڣ��򴴽�һ��ͼ���ļ���
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
         Log.e("Main", "�����ļ�����", e);
     }
 }
}
