package com.myapp;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class Version {
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	/**
	 * 本地版本代码
	 * @param context
	 * @return
	 */
	public static int getVerCode(Context context) {  
        int verCode = -1;  
        try {  
            verCode = context.getPackageManager().getPackageInfo("com.huiche",0).versionCode;  
        } catch (NameNotFoundException e) {  
            Log.e("获取本地版本失败", e.getMessage());
        }  
        return verCode;  
    }  
	public static String getVerName(Context context) {  
        String verName = "";  
        try {  
        	verName = context.getPackageManager().getPackageInfo("com.huiche",0).versionName;  
        } catch (NameNotFoundException e) {  
            Log.e("获取本地版本失败", e.getMessage());
        }  
        return verName;  
    }  
	private String version;
	private int code;
	private String url;
	private String description;
}
