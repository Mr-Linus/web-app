package com.myapp;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.xmlpull.v1.XmlPullParser;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import com.myapp.R;

public class UpdateManager {
	private Context mContext;
	// 返回的安装包url
	private Dialog noticeDialog;
	private Dialog downloadDialog;
	/* 下载包安装路径 */
	private static final String savePath = Environment.getExternalStorageDirectory().getPath() + "/";
	private String saveFileName = savePath + "myApp_";
	/* 进度条与通知ui刷新的handler和msg常量 */
	private ProgressBar mProgress;
	private Version newVersion = new Version();// 服务器版本信息
	private static final int DOWN_UPDATE = 1;// 更新消息
	private static final int DOWN_OVER = 2;// 结束消息
	private static final int IS_UPDATE = 3;// 更新文件下载完毕
	private int progress;// 进度提示
	private Thread downLoadThread;// 下载线程
	private boolean interceptFlag = false;// 标志
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWN_UPDATE:
				mProgress.setProgress(progress);// 下载任务
				break;
			case DOWN_OVER:
				//此处应取消安装对话框
				downloadDialog.hide();
				installApk();// 下载结束安装
				break;
			case IS_UPDATE:
				if (newVersion.getCode() > Version.getVerCode(mContext)) {
					showNoticeDialog();
				}
				break;
			default:
				break;
			}
		};
	};

	public UpdateManager(Context context) {
		this.mContext = context;
		saveFileName=saveFileName+Version.getVerName(context)+".apk";
	}

	// 外部接口让主Activity调用，新建线程检查服务 器版本更新
	public void checkUpdate() {
		new Thread() {
			public void run() {
				checkVersionTask();
			}
		}.start();
	}

	// 显示有更新提示对话框
	private void showNoticeDialog() {
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle("软件版本更新");
		builder.setMessage(newVersion.getDescription());
		builder.setPositiveButton("下载", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				showDownloadDialog();
			}
		});
		builder.setNegativeButton("以后再说", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		noticeDialog = builder.create();
		noticeDialog.show();
	}

	// 显示下载进度对话框
	private void showDownloadDialog() {
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle("软件版本更新");

		final LayoutInflater inflater = LayoutInflater.from(mContext);
		View v = inflater.inflate(R.layout.progress, null);
		mProgress = (ProgressBar) v.findViewById(R.id.progress);
		builder.setView(v);
		builder.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				interceptFlag = true;
			}
		});
		downloadDialog = builder.create();
		downloadDialog.show();
		downloadApk();
	}

	public void checkVersionTask() {
		try {
			// 从资源文件获取服务器 地址
			URL url = new URL("http://121.199.51.104/app/version.xml");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(10000);
			conn.connect();
			InputStream is = conn.getInputStream();
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(is, "utf-8");// 设置解析的数据源
			int type = parser.getEventType();
			while (type != XmlPullParser.END_DOCUMENT) {
				switch (type) {
				case XmlPullParser.START_TAG:
					if ("version".equals(parser.getName())) {
						newVersion.setVersion(parser.nextText()); // 获取版本号
					} else if ("url".equals(parser.getName())) {
						newVersion.setUrl(parser.nextText()); // 获取要升级的APK文件
					} else if ("code".equals(parser.getName())) {
						newVersion.setCode(Integer.parseInt(parser.nextText())); // 获取数字版本号
					} else if ("description".equals(parser.getName())) {
						newVersion.setDescription(parser.nextText()); // 获取该文件的信息
					}
					break;
				}
				type = parser.next();
			}
			// 获取到的版本号大于当前版本则提示升级
			// Log.e("测试,获取到的当前版本号",String.valueOf(Version.getVerCode(mContext))+"服务器版本号："+newVersion.getCode());
			mHandler.sendEmptyMessage(IS_UPDATE);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Runnable mdownApkRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				URL url = new URL(newVersion.getUrl());
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.connect();
				int length = conn.getContentLength();
				InputStream is = conn.getInputStream();

				File file = new File(savePath);
				if (!file.exists()) {
					file.mkdir();
				}
				String apkFile = saveFileName;
				File ApkFile = new File(apkFile);
				FileOutputStream fos = new FileOutputStream(ApkFile);

				int count = 0;
				byte buf[] = new byte[1024];

				do {
					int numread = is.read(buf);
					count += numread;
					progress = (int) (((float) count / length) * 100);
					// 更新进度
					mHandler.sendEmptyMessage(DOWN_UPDATE);
					if (numread <= 0) {
						// 下载完成通知安装
						mHandler.sendEmptyMessage(DOWN_OVER);
						break;
					}
					fos.write(buf, 0, numread);
				} while (!interceptFlag);// 点击取消就停止下载.
				fos.close();
				is.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};
	/**
	 * 下载apk
	 * 
	 * @param url
	 */
	private void downloadApk() {
		downLoadThread = new Thread(mdownApkRunnable);
		downLoadThread.start();
	}
	/**
	 * 安装apk
	 * 
	 * @param url
	 */
	private void installApk() {
		File apkfile = new File(saveFileName);
		if (!apkfile.exists()) {
			return;
		}
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()),"application/vnd.android.package-archive");
		mContext.startActivity(i);
	}
}
