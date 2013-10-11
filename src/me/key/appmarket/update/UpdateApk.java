package me.key.appmarket.update;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import me.key.appmarket.network.CheckVersionRequest;
import me.key.appmarket.network.CheckVersionResponse;
import me.key.appmarket.network.HttpRequest.OnResponseListener;
import me.key.appmarket.network.HttpResponse;
import me.key.appmarket.utils.LogUtils;
import me.key.appmarket.utils.ToastUtils;
import me.key.appmarket.widgets.AppDialog;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import com.market.d9game.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.RemoteViews;

public class UpdateApk {

	private static final int UPDATE_ID = 1000;
	private static final int BUFFER_SIZE = 10240;
	private static final String APK_NAME = "temp.apk";

	private static UpdateApk sUpdateApk = null;
	private static boolean sChecking = false;
	private static boolean sHasUpdate = false;

	private NotificationManager mManager;
	private Notification mNotification;
	private RemoteViews mRemoteViews;
	private PendingIntent mContentIntent;
	private DownloadThread mDownloadThread = null;
	private Context mContext;
	private long mContentLength = 0;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			ToastUtils.show(R.string.update_failed);
		}
	};

	public synchronized static UpdateApk getNotification(Context cx) {
		if (sUpdateApk == null) {
			sUpdateApk = new UpdateApk(cx);
		}
		return sUpdateApk;
	}

	@SuppressWarnings("deprecation")
	private UpdateApk(Context cx) {
		mContext = cx.getApplicationContext();
		mManager = (NotificationManager) cx
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotification = new Notification(R.drawable.icon,
				cx.getString(R.string.update_starting),
				System.currentTimeMillis());
		mNotification.flags = Notification.FLAG_NO_CLEAR;
		mRemoteViews = new RemoteViews(cx.getPackageName(),
				R.layout.update_remote_view);

		Intent intent = new Intent();
		mContentIntent = PendingIntent.getActivity(mContext, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
	}

	public synchronized void start(String url) {
		if (mDownloadThread == null) {
			mNotification.contentIntent = mContentIntent;
			mNotification.contentView = mRemoteViews;
			mManager.cancel(UPDATE_ID);
			mManager.notify(UPDATE_ID, mNotification);
			mDownloadThread = new DownloadThread(url);
			mDownloadThread.start();
		}
	}

	public static boolean hasUpdate() {
		return sHasUpdate;
	}

	private void update(int progress) {
		mRemoteViews.setProgressBar(R.id.pbar, 100, progress, false);
		mManager.notify(UPDATE_ID, mNotification);
	}

	private synchronized void finish() {
		mNotification = null;
		if (mManager != null) {
			mManager.cancel(UPDATE_ID);
		}
		mRemoteViews = null;
		sUpdateApk = null;
		mDownloadThread = null;
		mContext = null;
	}

	public boolean isUpdating() {
		if (mDownloadThread != null) {
			return true;
		}
		return false;
	}

	public static boolean checkUpdate(final Context cx,
			final boolean showToast, final boolean showDialog) {
		if (sChecking) {
			return false;
		}
		if (UpdateApk.getNotification(cx).isUpdating()) {
			return false;
		}
		sChecking = true;
		PackageInfo pkgInfo;
		int versionCode;
		try {
			pkgInfo = cx.getPackageManager().getPackageInfo(
					cx.getPackageName(), 0);
			versionCode = pkgInfo.versionCode;
		} catch (Exception e) {
			e.printStackTrace();
			versionCode = 1;
		}
		new CheckVersionRequest(versionCode + "")
				.execute(new OnResponseListener() {
					@Override
					public void onGetResponse(HttpResponse resp) {
						sChecking = false;
						final CheckVersionResponse r = (CheckVersionResponse) resp;
LogUtils.d("version", r+"");
						if (r.version != null && !r.version.equals("null")) {
							
							if (showDialog) {
								showUpdateDialog(cx, r);
							}
						} else if (showToast) {
							ToastUtils.show(R.string.update_no);
						}
					}
				});
		return true;
	}

	private static void showUpdateDialog(final Context cx,
			final CheckVersionResponse r) {
		new AppDialog(cx, false).setMessage(R.string.has_update)
				.setPositiveButton(R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						UpdateApk update = UpdateApk.getNotification(cx);
						update.start(r.url);
					}
				}).setNegtiveButton(R.string.next_time, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).setAlignLeft().show();
	}

	private boolean downloadApk(String url) {
		int alreadyDownloadSize;
		int alreadyDownloadPercentage;
		int oldPercent;
		InputStream is = null;
		FileOutputStream fos = null;

		String ROOT = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/App";
		File f = new File(ROOT);
		f.mkdirs();
		f = new File(ROOT + "/" + APK_NAME);
		int readNum;
		byte[] buf = new byte[BUFFER_SIZE];
		try {
			fos = new FileOutputStream(f);
			is = getAppContentStream(url);

			if (is == null) {
				return false;
			}
			alreadyDownloadSize = 0;
			oldPercent = 0;
			while ((readNum = is.read(buf, 0, BUFFER_SIZE)) != -1) {
				fos.write(buf, 0, readNum);
				alreadyDownloadSize += readNum;
				alreadyDownloadPercentage = (int) ((float) (alreadyDownloadSize)
						/ (float) (mContentLength) * 100.0);
				if (alreadyDownloadPercentage > 100) {
					alreadyDownloadPercentage = 100;
				}
				if (oldPercent != alreadyDownloadPercentage) {
					update(alreadyDownloadPercentage);
					oldPercent = alreadyDownloadPercentage;
				}
			}

		} catch (SocketException e) {
			e.printStackTrace();
			return false;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return true;
	}

	public InputStream getAppContentStream(String url) throws SocketException {

		if (TextUtils.isEmpty(url)) {
			return null;
		}
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 10 * 1000);
		httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				5 * 60 * 1000);

		HttpGet httpget = new HttpGet(url);
		InputStream is = null;
		try {
			org.apache.http.HttpResponse response;
			response = httpclient.execute(httpget);
			is = response.getEntity().getContent();
			mContentLength = response.getEntity().getContentLength();
			return is;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			if (e instanceof SocketException
					|| e instanceof SocketTimeoutException) {
				throw new SocketException(e.getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}

		return null;
	}

	private static boolean installApk(Context context, String path) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		try {
			intent.setDataAndType(Uri.fromFile(new File(path)),
					"application/vnd.android.package-archive");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private class DownloadThread extends Thread {

		private String url;

		public DownloadThread(String url) {
			this.url = url;
		}

		@Override
		public void run() {
			if (downloadApk(url)) {
				String ROOT = Environment.getExternalStorageDirectory()
						.getAbsolutePath() + "/App";
				installApk(mContext, ROOT + "/" + APK_NAME);
			} else {
				mHandler.obtainMessage().sendToTarget();
			}
			finish();
		}
	}
}
