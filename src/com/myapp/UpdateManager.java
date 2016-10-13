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
	// ���صİ�װ��url
	private Dialog noticeDialog;
	private Dialog downloadDialog;
	/* ���ذ���װ·�� */
	private static final String savePath = Environment.getExternalStorageDirectory().getPath() + "/";
	private String saveFileName = savePath + "myApp_";
	/* ��������֪ͨuiˢ�µ�handler��msg���� */
	private ProgressBar mProgress;
	private Version newVersion = new Version();// �������汾��Ϣ
	private static final int DOWN_UPDATE = 1;// ������Ϣ
	private static final int DOWN_OVER = 2;// ������Ϣ
	private static final int IS_UPDATE = 3;// �����ļ��������
	private int progress;// ������ʾ
	private Thread downLoadThread;// �����߳�
	private boolean interceptFlag = false;// ��־
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWN_UPDATE:
				mProgress.setProgress(progress);// ��������
				break;
			case DOWN_OVER:
				//�˴�Ӧȡ����װ�Ի���
				downloadDialog.hide();
				installApk();// ���ؽ�����װ
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

	// �ⲿ�ӿ�����Activity���ã��½��̼߳����� ���汾����
	public void checkUpdate() {
		new Thread() {
			public void run() {
				checkVersionTask();
			}
		}.start();
	}

	// ��ʾ�и�����ʾ�Ի���
	private void showNoticeDialog() {
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle("����汾����");
		builder.setMessage(newVersion.getDescription());
		builder.setPositiveButton("����", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				showDownloadDialog();
			}
		});
		builder.setNegativeButton("�Ժ���˵", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		noticeDialog = builder.create();
		noticeDialog.show();
	}

	// ��ʾ���ؽ��ȶԻ���
	private void showDownloadDialog() {
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle("����汾����");

		final LayoutInflater inflater = LayoutInflater.from(mContext);
		View v = inflater.inflate(R.layout.progress, null);
		mProgress = (ProgressBar) v.findViewById(R.id.progress);
		builder.setView(v);
		builder.setNegativeButton("ȡ��", new OnClickListener() {
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
			// ����Դ�ļ���ȡ������ ��ַ
			URL url = new URL("http://121.199.51.104/app/version.xml");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(10000);
			conn.connect();
			InputStream is = conn.getInputStream();
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(is, "utf-8");// ���ý���������Դ
			int type = parser.getEventType();
			while (type != XmlPullParser.END_DOCUMENT) {
				switch (type) {
				case XmlPullParser.START_TAG:
					if ("version".equals(parser.getName())) {
						newVersion.setVersion(parser.nextText()); // ��ȡ�汾��
					} else if ("url".equals(parser.getName())) {
						newVersion.setUrl(parser.nextText()); // ��ȡҪ������APK�ļ�
					} else if ("code".equals(parser.getName())) {
						newVersion.setCode(Integer.parseInt(parser.nextText())); // ��ȡ���ְ汾��
					} else if ("description".equals(parser.getName())) {
						newVersion.setDescription(parser.nextText()); // ��ȡ���ļ�����Ϣ
					}
					break;
				}
				type = parser.next();
			}
			// ��ȡ���İ汾�Ŵ��ڵ�ǰ�汾����ʾ����
			// Log.e("����,��ȡ���ĵ�ǰ�汾��",String.valueOf(Version.getVerCode(mContext))+"�������汾�ţ�"+newVersion.getCode());
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
					// ���½���
					mHandler.sendEmptyMessage(DOWN_UPDATE);
					if (numread <= 0) {
						// �������֪ͨ��װ
						mHandler.sendEmptyMessage(DOWN_OVER);
						break;
					}
					fos.write(buf, 0, numread);
				} while (!interceptFlag);// ���ȡ����ֹͣ����.
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
	 * ����apk
	 * 
	 * @param url
	 */
	private void downloadApk() {
		downLoadThread = new Thread(mdownApkRunnable);
		downLoadThread.start();
	}
	/**
	 * ��װapk
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
