package me.key.appmarket.tool;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.key.appmarket.MainActivity;
import me.key.appmarket.MarketApplication;
import me.key.appmarket.ImageNet.AsyncImageLoader;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.LocalAppInfo;
import me.key.appmarket.utils.LocalUtils;
import me.key.appmarket.utils.LogUtils;
import me.key.appmarket.widgets.MyTableHost;
import net.tsz.afinal.FinalDb;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.HttpHandler;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.market.d9game.R;

public class DownloadService extends Service {

	private static NotificationManager nm;
	// private static Notification notification;
	private static boolean cancelUpdate = false;
	// private static MyHandler myHandler;
	private static ExecutorService executorService = Executors
			.newFixedThreadPool(5);
	public static Map<Integer, Long> download = new HashMap<Integer, Long>();
	public static Context context;
	private static int sendCount = 0;
	private static WindowManager wm;
	private static Set<String> downs = new HashSet<String>();
	static Drawable image;
	private static FinalDb db;
	private static AsyncImageLoader asyncImageLoader = new AsyncImageLoader();

	// 存储image的集合;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void stop() {
		stopSelf();

	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// myHandler = new MyHandler(Looper.myLooper(), DownloadService.this);
		wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		context = this;
		db = FinalDb.create(context);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	// inal String url, final int notificationId,final String name

	public static void downNewFile(AppInfo appInfo, long startOff, long endOff) {
		int notificationId = Integer.parseInt(appInfo.getIdx());
		String url = appInfo.getAppUrl();
		String name = appInfo.getAppName();
		appInfo.setDown(true);
		if (download.containsKey(notificationId))
			return;
		/*
		 * notification = new Notification(); notification.icon =
		 * android.R.drawable.stat_sys_download; notification.when =
		 * System.currentTimeMillis(); notification.defaults =
		 * Notification.DEFAULT_LIGHTS; notification.flags =
		 * Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT; Intent
		 * intent = new Intent(context, MainActivity.class);
		 * intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); PendingIntent
		 * contentIntent = PendingIntent.getActivity(context, notificationId,
		 * intent, 0); notification.setLatestEventInfo(context, name, "0%",
		 * contentIntent); download.put(notificationId, 0l);
		 * nm.notify(notificationId, notification);
		 */
		// notification = new Notification(R.drawable.icon, "联系人数量",
		// System.currentTimeMillis());
		// notification.when = System.currentTimeMillis();
		// notification.defaults = Notification.DEFAULT_LIGHTS;
		// notification.flags = Notification.FLAG_NO_CLEAR
		// | Notification.FLAG_ONGOING_EVENT;
		// Intent intent = new Intent(context, MainActivity.class);
		// intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		// PendingIntent contentIntent = PendingIntent.getActivity(context,
		// notificationId, intent, 0);
		// RemoteViews contentView = new RemoteViews(context.getPackageName(),
		// R.layout.custom_notification);
		//
		// contentView.setImageViewBitmap(R.id.image,
		// getResIcon(context.getResources(), R.drawable.arrow_down));
		//
		// contentView.setTextViewText(R.id.text, name);
		// contentView.setTextViewText(R.id.prog, "0%");
		// notification.contentView = contentView;
		// notification.contentIntent = contentIntent;
		// // notification.setLatestEventInfo(context, "", "", contentIntent);
		// // 使用RemoteView自定义通知视图
		//
		// nm.notify(notificationId, notification);
		downFile(appInfo, startOff, endOff);
	}

	private static void downFile(AppInfo appInfo, long startOff, long endOff) {
		executorService.execute(new DownFiles(appInfo, startOff, endOff));
		/*
		 * ArrayList<AppInfo> downApplist =
		 * MarketApplication.getInstance().getDownApplist();
		 * downApplist.add(appInfo);
		 */
		String id = appInfo.getId();
		List<AppInfo> findAll = db.findAll(AppInfo.class);
		Intent intent = new Intent();
		intent.setAction("startanim");
		context.sendBroadcast(intent);
		if (findAll.size() == 0) {
			db.save(appInfo);
		} else {
			int j = 0;
			for (int i = 0; i < findAll.size(); i++) {
				if (!id.equals(findAll.get(i).getId())) {
					j++;
				} else {
					break;
				}
			}
			if (j == findAll.size()) {
				db.save(appInfo);
			}
		}
	}

	static class DownFiles implements Runnable {
		private String url;
		private int notificationId;
		private String name;
		private long startOff;
		private long endOff;
		private AppInfo appInfo;
		private boolean isPause = false;
		private long precent;
		private Notification notification;
		private MyHandler handler;
		private Drawable loadImageFromUrl;
		private FinalHttp fh;
		// 控制下载用
		private HttpHandler<File> handler1;

		public DownFiles(AppInfo appInfo, long startOff, long endOff) {
			super();
			this.url = appInfo.getAppUrl();
			this.appInfo = appInfo;
			this.notificationId = Integer.parseInt(appInfo.getIdx());
			this.name = appInfo.getAppName();
			this.startOff = startOff;
			this.endOff = endOff;
			handler = new MyHandler(Looper.myLooper(), context);
			final File tempFile = CreatFileName(name);
			SharedPreferences sp = context.getSharedPreferences("down",
					context.MODE_PRIVATE);
			final Editor edit = sp.edit();
			startDown(tempFile, edit);
		}

		public void startDown(final File tempFile, final Editor edit) {
			fh = new FinalHttp();
			// 调用download方法开始下载
			handler1 = fh.download(url, tempFile.getAbsolutePath(), true,
					new AjaxCallBack<File>() {
						@Override
						public void onLoading(long count, long current) {
							// textView.setText("下载进度："+current+"/"+count);
							// 将文件和文件大小存储

							edit.putLong(tempFile.getAbsolutePath(), count);
							edit.putLong(
									tempFile.getAbsolutePath() + "precent",
									precent);
							precent = (int) (((double) current / count) * 100);
							edit.commit();
							LogUtils.d("DownLoad", "我已经下了" + current + "/"
									+ count);
							if (precent - download.get(notificationId) >= 1) {
								download.put(notificationId, precent);
								Message message = handler.obtainMessage(3,
										precent);
								Bundle bundle = new Bundle();
								bundle.putString("name", name);
								message.setData(bundle);
								message.arg1 = notificationId;
								handler.sendMessage(message);

							}
						}

						@Override
						public void onSuccess(File t) {
							LogUtils.d("DownLoad", "我已经下完了");
							edit.putLong(
									tempFile.getAbsolutePath() + "precent", 100);
							edit.commit();
							// textView.setText(t==null?"null":t.getAbsoluteFile().toString());
							Message message = handler
									.obtainMessage(2, tempFile);
							message.arg1 = notificationId;
							Bundle bundle = new Bundle();
							bundle.putString("name", name);
							message.setData(bundle);
							handler.sendMessage(message);
							appInfo.setDown(false);
						}

						@Override
						public void onFailure(Throwable t, String strMsg) {
							/*
							 * Message message = handler.obtainMessage(4, name +
							 * "下载失败:网络异常"); message.arg1 = notificationId;
							 * handler.sendMessage(message);
							 */
							super.onFailure(t, strMsg);
						}

					});
		}

		// 接受暂停消息的广播
		class PauseBroadcast extends BroadcastReceiver {

			@Override
			public void onReceive(Context context, Intent intent) {
				LogUtils.d("test", "我接收消息");
				isPause = !isPause;
				File tempFile = CreatFileName(name);
				SharedPreferences sp = context.getSharedPreferences("down",
						MODE_PRIVATE);
				Editor edit = sp.edit();
				edit.putLong(tempFile.getAbsolutePath() + "precent", precent);
				edit.commit();
				if (isPause) {
					handler1.stop();
					appInfo.setIspause(isPause);
					db.update(appInfo);
				} else {
					startDown(tempFile, edit);
					appInfo.setIspause(isPause);
					db.update(appInfo);
				}

			}

		}

		// 用来关闭通知栏的广播接受者
		class CancalNotifiBroadcast extends BroadcastReceiver {

			@Override
			public void onReceive(Context context, Intent intent) {
				LogUtils.d("Main", "我接收到了关闭通知广播");
				nm.cancelAll();
				for (Activity activity : MarketApplication.getInstance()
						.getAppLication()) {
					activity.finish();
				}
				android.os.Process.killProcess(android.os.Process.myPid());
				System.exit(0);
			}

		}

		class MyHandler extends Handler {
			private Context context;

			public MyHandler(Looper looper, Context c) {
				super(looper);
				this.context = c;
			}

			@Override
			public void handleMessage(Message msg) {
				PendingIntent contentIntent = null;
				super.handleMessage(msg);
				if (msg != null) {
					switch (msg.what) {
					case 0:
						Toast.makeText(context, msg.obj.toString(),
								Toast.LENGTH_SHORT).show();
						download.remove(msg.arg1);
						break;
					case 1:
						break;
					case 2:
						contentIntent = PendingIntent.getActivity(context,
								msg.arg1,
								new Intent(context, MyTableHost.class), 0);
						notification.setLatestEventInfo(context, msg.getData()
								.getString("name") + "下载完成", "100%",
								contentIntent);
						notification.contentView.setTextViewText(R.id.text, msg
								.getData().getString("name") + "下载完成");
						notification.contentView.setTextViewText(R.id.prog,
								"100%");

						nm.notify(msg.arg1, notification);
						download.remove(msg.arg1);
						nm.cancel(msg.arg1);
						Instanll((File) msg.obj, context);
						SharedPreferences sp = context.getSharedPreferences(
								"down", MODE_PRIVATE);
						Editor edit = sp.edit();
						File tempFile = CreatFileName(msg.getData().getString(
								"name"));
						edit.remove(tempFile.getAbsolutePath());

						edit.commit();
						break;
					case 3:
						if (msg.arg1 < 95) {
							if (sendCount > 2) {
								// 发送刷新界面的广播
								Intent intent = new Intent();
								intent.setAction(MarketApplication.PRECENT);
								context.sendBroadcast(intent);
								sendCount = 0;
							} else {
								sendCount++;
							}
						} else {
							// 发送刷新界面的广播
							Intent intent = new Intent();
							intent.setAction(MarketApplication.PRECENT);
							context.sendBroadcast(intent);
							sendCount = 0;
						}

						contentIntent = PendingIntent.getActivity(context,
								msg.arg1,
								new Intent(context, MyTableHost.class), 0);
						notification.setLatestEventInfo(context, msg.getData()
								.getString("name") + "正在下载",
								download.get(msg.arg1) + "%", contentIntent);
						/*
						 * SoftReference<Drawable> sr =
						 * imageCache.get(msg.arg1+""); LogUtils.d("sr",
						 * imageCache.size()+""); if(sr != null){ Drawable
						 * drawable = sr.get(); LogUtils.d("sr", "test");
						 * notification
						 * .contentView.setImageViewBitmap(R.id.image,
						 * drawable2Bitmap(drawable)); }
						 */

						notification.contentView.setTextViewText(R.id.text, msg
								.getData().getString("name"));
						notification.contentView.setTextViewText(R.id.prog,
								download.get(msg.arg1) + "%");

						nm.notify(msg.arg1, notification);
						break;
					case 4:
						Toast.makeText(context, msg.obj.toString(),
								Toast.LENGTH_SHORT).show();
						download.remove(msg.arg1);
						nm.cancel(msg.arg1);
						Intent intent = new Intent();
						intent.setAction(MarketApplication.PRECENT);
						context.sendBroadcast(intent);
						break;
					}
				}
			}

		}

		public void run() {

			final File tempFile = CreatFileName(name);
			IntentFilter filter = new IntentFilter(tempFile.getAbsolutePath());
			PauseBroadcast receiver = new PauseBroadcast();
			context.registerReceiver(receiver, filter);
			IntentFilter notifitionIf = new IntentFilter(
					"duobaohui.cancalnotifition");
			CancalNotifiBroadcast cnb = new CancalNotifiBroadcast();
			context.registerReceiver(cnb, notifitionIf);
			// try {
			LogUtils.d("Main", "我注册了取消通知广播");
			download.put(notificationId, 0l);
			notification = new Notification(R.drawable.icon, name + "开始下载",
					System.currentTimeMillis());
			notification.when = System.currentTimeMillis();
			notification.defaults = Notification.DEFAULT_LIGHTS;
			notification.flags = Notification.FLAG_NO_CLEAR
					| Notification.FLAG_ONGOING_EVENT;
			Intent intent = new Intent(context, MyTableHost.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent contentIntent = PendingIntent.getActivity(context,
					notificationId, intent, 0);

			RemoteViews contentView = new RemoteViews(context.getPackageName(),
					R.layout.custom_notification);
			notification.contentView = contentView;
			// 异步下载图标
			/*
			 * new AsyncTask<Void, Void, Void>() {
			 * 
			 * @Override protected Void doInBackground(Void... params) {
			 * loadImageFromUrl = asyncImageLoader
			 * .loadImageFromUrl(appInfo.getIconUrl()); return null; }
			 * 
			 * protected void onPostExecute(Void result) {
			 * notification.contentView.setImageViewBitmap(R.id.image,
			 * drawable2Bitmap(loadImageFromUrl)); };
			 * 
			 * }.execute();
			 */
			if (loadImageFromUrl == null) {
				notification.contentView
						.setImageViewBitmap(
								R.id.image,
								getResIcon(context.getResources(),
										R.drawable.tempicon));
			}

			contentView.setTextViewText(R.id.text, name);
			contentView.setTextViewText(R.id.prog, "0%");

			notification.contentIntent = contentIntent;
			// 使用RemoteView自定义通知视图
			notification.setLatestEventInfo(context, name, "0%", contentIntent);
			nm.notify(notificationId, notification);

			/*
			 * HttpClient client = new DefaultHttpClient(); HttpGet get = new
			 * HttpGet(url); HttpResponse response = client.execute(get);
			 * HttpEntity entity = response.getEntity(); long length =
			 * entity.getContentLength(); InputStream is = entity.getContent();
			 * if (is != null) { File rootFile = new File(Environment
			 * .getExternalStorageDirectory(), "/market"); if
			 * (!rootFile.exists() && !rootFile.isDirectory()) rootFile.mkdir();
			 * if (tempFile.exists()) tempFile.delete();
			 * tempFile.createNewFile(); BufferedInputStream bis = new
			 * BufferedInputStream(is); FileOutputStream fos = new
			 * FileOutputStream(tempFile); BufferedOutputStream bos = new
			 * BufferedOutputStream(fos); int read; long count = 0; int precent
			 * = 0; byte[] buffer = new byte[1024]; while ((read =
			 * bis.read(buffer)) != -1 && !cancelUpdate) { bos.write(buffer, 0,
			 * read); count += read; precent = (int) (((double) count / length)
			 * * 100); if (precent - download.get(notificationId) >= 1) {
			 * download.put(notificationId, precent); Message message =
			 * myHandler.obtainMessage(3, precent); Bundle bundle = new
			 * Bundle(); bundle.putString("name", name);
			 * message.setData(bundle); message.arg1 = notificationId;
			 * myHandler.sendMessage(message); } } bos.flush(); bos.close();
			 * fos.flush(); fos.close(); is.close(); bis.close(); }
			 */
			/*
			 * URL urls = new URL(url); // 获取http连接 HttpURLConnection coon =
			 * (HttpURLConnection) urls .openConnection(); // 设置请求头信息
			 * coon.setRequestMethod("GET");
			 * coon.setRequestProperty("Accept-Language", "zh-CN");
			 * coon.setRequestProperty("Referer", urls.toString());
			 * coon.setRequestProperty("Charset", "UTF-8"); // 设置从哪个位置开始下载
			 * coon.setRequestProperty("Range", "bytes=" + startOff + "-"); //
			 * 超时时间 coon.setConnectTimeout(5000); // 获取文件大小 int flieLength =
			 * coon.getContentLength(); flieLength += startOff; InputStream is =
			 * coon.getInputStream(); // 存储下载的文件及下载的大小 SharedPreferences sp =
			 * context.getSharedPreferences("down", MODE_PRIVATE); Editor edit =
			 * sp.edit(); BufferedInputStream bis = new BufferedInputStream(is);
			 * 
			 * if (is != null) { File rootFile = new File(
			 * Environment.getExternalStorageDirectory(), "/market"); if
			 * (!rootFile.exists() && !rootFile.isDirectory()) rootFile.mkdir();
			 * 
			 * if (tempFile.exists()) tempFile.delete();
			 * 
			 * // tempFile.createNewFile(); int read = 0; long count = startOff;
			 * 
			 * byte[] buffer = new byte[1024]; RandomAccessFile ranFile = new
			 * RandomAccessFile(tempFile, "rwd"); FileChannel fco =
			 * ranFile.getChannel(); MappedByteBuffer mbbo =
			 * fco.map(FileChannel.MapMode.READ_WRITE, startOff, flieLength);
			 * if(startOff == 0 ){ edit.putLong(tempFile.getAbsolutePath(), 0);
			 * edit.commit(); } if (startOff == 0) {
			 * ranFile.setLength(coon.getContentLength()); }
			 * 
			 * // 设置从文件的哪个位置开始写入 ranFile.seek(startOff);
			 */

			/*
			 * while (read != -1 && !cancelUpdate) { if (!isPause) { read =
			 * bis.read(buffer); if (read != -1) { //ranFile.write(buffer, 0,
			 * read); mbbo.put(buffer,0,read); count += read; // 将文件和文件大小存储
			 * edit.putLong(tempFile.getAbsolutePath(), count);
			 * edit.putLong(tempFile.getAbsolutePath() + "precent", precent);
			 * edit.commit(); precent = (int) (((double) count / flieLength) *
			 * 100); if (precent - download.get(notificationId) >= 1) {
			 * download.put(notificationId, precent); Message message =
			 * handler.obtainMessage( 3, precent); Bundle bundle = new Bundle();
			 * bundle.putString("name", name); message.setData(bundle);
			 * message.arg1 = notificationId; handler.sendMessage(message);
			 * 
			 * } } }
			 * 
			 * }
			 * 
			 * is.close(); ranFile.close(); bis.close(); }
			 */

			/*
			 * if (!cancelUpdate) { Message message = handler.obtainMessage(2,
			 * tempFile); message.arg1 = notificationId; Bundle bundle = new
			 * Bundle(); bundle.putString("name", name);
			 * message.setData(bundle); handler.sendMessage(message); } else {
			 * tempFile.delete(); } } catch (ClientProtocolException e) { if
			 * (tempFile.exists()) tempFile.delete(); Message message =
			 * handler.obtainMessage(4, name + "下载失败:网络异常！"); message.arg1 =
			 * notificationId; handler.sendMessage(message); } catch
			 * (IOException e) {
			 * 
			 * if (tempFile.exists()) tempFile.delete();
			 * 
			 * Message message = handler .obtainMessage(4, name + "下载失败:网络异常");
			 * message.arg1 = notificationId; handler.sendMessage(message);
			 * e.printStackTrace(); } catch (Exception e) {
			 * 
			 * if (tempFile.exists()) tempFile.delete();
			 * 
			 * Message message = handler .obtainMessage(4, name + "下载失败:网络异常");
			 * message.arg1 = notificationId; handler.sendMessage(message);
			 * e.printStackTrace(); } finally { appInfo.setDown(false); }
			 */
		}

	}

	private static void Instanll(File file, Context context) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		context.startActivity(intent);
	}

	// class MyHandler extends Handler {
	// private Context context;
	//
	// public MyHandler(Looper looper, Context c) {
	// super(looper);
	// this.context = c;
	// }
	//
	// @Override
	// public void handleMessage(Message msg) {
	// PendingIntent contentIntent = null;
	// super.handleMessage(msg);
	// if (msg != null) {
	// switch (msg.what) {
	// case 0:
	// Toast.makeText(context, msg.obj.toString(),
	// Toast.LENGTH_SHORT).show();
	// download.remove(msg.arg1);
	// break;
	// case 1:
	// break;
	// case 2:
	// contentIntent = PendingIntent.getActivity(
	// DownloadService.this, msg.arg1, new Intent(
	// DownloadService.this, MainActivity.class),
	// 0);
	// // notification.setLatestEventInfo(DownloadService.this, msg
	// // .getData().getString("name") + "下载完成", "100%",
	// // contentIntent);
	// // nm.notify(msg.arg1, notification);
	// download.remove(msg.arg1);
	// nm.cancel(msg.arg1);
	// Instanll((File) msg.obj, context);
	// SharedPreferences sp = context.getSharedPreferences("down",
	// MODE_PRIVATE);
	// Editor edit = sp.edit();
	// File tempFile = CreatFileName(msg.getData().getString(
	// "name"));
	// edit.remove(tempFile.getAbsolutePath());
	//
	// edit.commit();
	// break;
	// case 3:
	// if (msg.arg1 < 95) {
	// if (sendCount > 5) {
	// // 发送刷新界面的广播
	// Intent intent = new Intent();
	// intent.setAction(MarketApplication.PRECENT);
	// context.sendBroadcast(intent);
	// sendCount = 0;
	// } else {
	// sendCount++;
	// }
	// } else {
	// // 发送刷新界面的广播
	// Intent intent = new Intent();
	// intent.setAction(MarketApplication.PRECENT);
	// context.sendBroadcast(intent);
	// sendCount = 0;
	// }
	//
	// contentIntent = PendingIntent.getActivity(
	// DownloadService.this, msg.arg1, new Intent(
	// DownloadService.this, MainActivity.class),
	// 0);
	// // notification.setLatestEventInfo(DownloadService.this, msg
	// // .getData().getString("name") + "正在下载",
	// // download.get(msg.arg1) + "%", contentIntent);
	// /*
	// * SoftReference<Drawable> sr = imageCache.get(msg.arg1+"");
	// * LogUtils.d("sr", imageCache.size()+""); if(sr != null){
	// * Drawable drawable = sr.get(); LogUtils.d("sr", "test");
	// * notification.contentView.setImageViewBitmap(R.id.image,
	// * drawable2Bitmap(drawable)); }
	// */
	// /*
	// * notification.contentView.setTextViewText(R.id.text, msg
	// * .getData().getString("name"));
	// * notification.contentView.setTextViewText(R.id.prog,
	// * download.get(msg.arg1) + "%");
	// */
	// //nm.notify(msg.arg1, notification);
	// break;
	// case 4:
	// Toast.makeText(context, msg.obj.toString(),
	// Toast.LENGTH_SHORT).show();
	// download.remove(msg.arg1);
	// nm.cancel(msg.arg1);
	// break;
	// }
	// }
	// }
	//
	// }

	public static File CreatFileName(String name) {
		File marketDir = new File(LocalUtils.getRoot(context)+"market");
		try {
		if(!marketDir.exists()){
				marketDir.mkdir();}
			} catch (Exception e) {
				e.printStackTrace();
			}
		File tempFile = new File(marketDir.getAbsoluteFile(),
				"/" + name + ".apk");
		LogUtils.i("download---------", tempFile.getAbsolutePath());
		return tempFile;
	}

	public static long getPrecent(int idx) {
		if (isDownLoading(idx)) {
			return download.get(idx);
		}
		return 0;
	}

	public static boolean isDownLoading(int idx) {
		boolean result = false;
		result = download.containsKey(idx);
		return result;
	}

	public static boolean isDownLoaded(String name) {
		boolean result = false;
		SharedPreferences sp = context.getSharedPreferences("down",
				MODE_PRIVATE);
		File tempFile = new File(Environment.getExternalStorageDirectory(),
				"/market/" + name + ".apk");

		if (tempFile.exists()) {
			result = true;
		}
		long temp = sp.getLong(tempFile.getAbsolutePath(), -1);
		if (temp != -1) {
			LogUtils.d("DownLoadService", "temp" + temp);
			result = false;
		}
		return result;
	}

	public static boolean isExist(String name) {
		boolean result = false;
		File tempFile = new File(Environment.getExternalStorageDirectory(),
				"/market/" + name + ".apk");
		if (tempFile.exists()) {
			result = true;
		}
		return result;
	}

	public static void Instanll(String name, Context context) {
		File tempFile = new File(Environment.getExternalStorageDirectory(),
				"/market/" + name + ".apk");

		if (tempFile.exists()) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(tempFile),
					"application/vnd.android.package-archive");
			context.startActivity(intent);

		}
	}

	// 获取图片
	private static Bitmap getResIcon(Resources res, int resId) {
		Drawable icon = res.getDrawable(resId);
		if (icon instanceof BitmapDrawable) {
			BitmapDrawable bd = (BitmapDrawable) icon;
			return bd.getBitmap();
		} else {
			return null;
		}
	}

	public static void downNewFile(final AppInfo appInfo, long startOff,
			long endOff, Drawable drawable) {
		final int notificationId = Integer.parseInt(appInfo.getIdx());
		String url = appInfo.getAppUrl();
		String name = appInfo.getAppName();
		if (download.containsKey(notificationId))
			return;

		// notification = new Notification();
		// notification.icon = android.R.drawable.stat_sys_download;
		// notification.when = System.currentTimeMillis();
		// notification.defaults = Notification.DEFAULT_LIGHTS;
		// notification.flags = Notification.FLAG_NO_CLEAR
		// | Notification.FLAG_ONGOING_EVENT;
		// Intent intent = new Intent(context, MainActivity.class);
		// intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		// PendingIntent contentIntent = PendingIntent.getActivity(context,
		// notificationId, intent, 0);
		// notification.setLatestEventInfo(context, name, "0%", contentIntent);
		// download.put(notificationId, 0l);
		// nm.notify(notificationId, notification);

		/*
		 * download.put(notificationId, 0l); notification = new
		 * Notification(R.drawable.icon, name+"开始", System.currentTimeMillis());
		 * notification.when = System.currentTimeMillis(); notification.defaults
		 * = Notification.DEFAULT_LIGHTS; notification.flags =
		 * Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT; Intent
		 * intent = new Intent(context, MainActivity.class);
		 * intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); PendingIntent
		 * contentIntent = PendingIntent.getActivity(context, notificationId,
		 * intent, 0); final RemoteViews contentView = new
		 * RemoteViews(context.getPackageName(), R.layout.custom_notification);
		 * 
		 * //Bitmap bm = drawable2Bitmap(drawable);
		 * //ImageLoader.getInstance().displayImage
		 * (appInfo.getIconUrl(),imageview , options);
		 * //contentView.setImageViewResource(R.id.image, R.drawable.icon);
		 * notification.contentView.setImageViewBitmap(R.id.image, bm);
		 * //启动一个异步任务更新通知栏图片 new AsyncTask<Void, Void, Void> (){
		 * 
		 * @Override protected Void doInBackground(Void... params) {
		 * 
		 * String imageUrl = appInfo.getIconUrl(); HashMap<String,
		 * SoftReference<Drawable>> imageCache = asyncImageLoader.imageCache; if
		 * (imageCache.containsKey(imageUrl)) { SoftReference<Drawable>
		 * softReference = imageCache .get(imageUrl); Drawable icon =
		 * softReference.get(); image = icon; } else { image =
		 * AsyncImageLoader.loadImageFromUrl(appInfo.getIconUrl());; } return
		 * null; }
		 * 
		 * @Override protected void onPostExecute(Void result) {
		 * super.onPostExecute(result);
		 * contentView.setImageViewBitmap(R.id.image, drawable2Bitmap(image));
		 * imageCache.put(notificationId+"",new SoftReference<Drawable>(image)
		 * ); } }.execute();
		 * 
		 * 
		 * contentView.setTextViewText(R.id.text, name);
		 * contentView.setTextViewText(R.id.prog, "0%");
		 * notification.contentIntent = contentIntent;
		 * notification.setLatestEventInfo(context, "", "", contentIntent); //
		 * 使用RemoteView自定义通知视图 notification.contentView = contentView;
		 * nm.notify(notificationId, notification);
		 */
		downFile(appInfo, startOff, endOff);
	}

	public static Bitmap drawable2Bitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		} else if (drawable instanceof NinePatchDrawable) {
			Bitmap bitmap = Bitmap
					.createBitmap(
							drawable.getIntrinsicWidth(),
							drawable.getIntrinsicHeight(),
							drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
									: Bitmap.Config.RGB_565);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
					drawable.getIntrinsicHeight());
			drawable.draw(canvas);
			// 获得屏幕高度和宽度
			int height = wm.getDefaultDisplay().getHeight();
			int width = wm.getDefaultDisplay().getWidth();
			// 通过Options获得图片的高宽
			Options opts = new Options();
			// 设置 不去真正的解析位图 不把他加载到内存 只是获取这个图片的宽高信息
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()
					+ "/a.jpg", opts);
			int bitmapWidth = opts.outWidth;
			int bitmapHeight = opts.outHeight;
			// 计算缩放比例
			int scalex = bitmapWidth / width;
			int scaley = bitmapHeight / height;
			// 计算缩放的方式
			if (scalex > scaley) {
				opts.inSampleSize = scalex;
			} else {
				opts.inSampleSize = scaley;
			}
			// 设置真正的解析图片
			opts.inJustDecodeBounds = false;
			Bitmap bp = BitmapFactory.decodeFile(
					Environment.getExternalStorageDirectory() + "/a.jpg", opts);
			return bp;
		} else {
			return null;
		}
	}

	public static void quiesceDownFile(String url, String name) {
		File tempFile = new File(Environment.getExternalStorageDirectory(),
				"/market/" + name + ".apk");
		URL urls;
		try {
			urls = new URL(url);
			HttpURLConnection coon = (HttpURLConnection) urls.openConnection();
			// 设置请求头信息
			coon.setRequestMethod("GET");
			coon.setRequestProperty("Accept-Language", "zh-CN");
			coon.setRequestProperty("Referer", urls.toString());
			coon.setRequestProperty("Charset", "UTF-8");
			// 设置从哪个位置开始下载
			// 超时时间
			coon.setConnectTimeout(5000);
			// 获取文件大小
			int flieLength = coon.getContentLength();
			InputStream is = coon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);

			if (is != null) {
				File rootFile = new File(
						Environment.getExternalStorageDirectory(), "/market");
				if (!rootFile.exists() && !rootFile.isDirectory())
					rootFile.mkdir();
				/*
				 * if (tempFile.exists()) tempFile.delete();
				 */
				// tempFile.createNewFile();
				int read = 0;
				long count = 0;

				byte[] buffer = new byte[1024];
				RandomAccessFile ranFile = new RandomAccessFile(tempFile, "rwd");
				ranFile.setLength(flieLength);

				while (read != -1) {
					read = bis.read(buffer);
					if (read != -1) {
						ranFile.write(buffer, 0, read);
						count += read;
						LogUtils.d("testdown", count + "");
					}

				}
				is.close();
				ranFile.close();
				bis.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 获取http连接

		// 存储下载的文件及下载的大小

	}

	// 获取当前打开应用
	public static void watchDog(List<AppInfo> list, ActivityManager am) {
		LogUtils.d("Down", context + "");
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		String packageName = cn.getPackageName();
		LogUtils.d("down", packageName);
		if (list.size() > 0) {
			for (AppInfo ai : list) {
				// LogUtils.d("down", ai.getAppName()+"");
				if (ai.getPackageName().equals(packageName)) {
					long currentTimeMillis = System.currentTimeMillis();
					ai.setLastTime(currentTimeMillis);
					// LogUtils.d("timelast", ai.getAppName()+ai.getLastTime());
					AppInfo findById = db.findById(ai.getId(), AppInfo.class);
					if (findById != null) {
						findById.setLastTime(ai.getLastTime());
						db.update(findById);
					} else {
						LocalAppInfo findById2 = db.findById(ai.getId(),
								LocalAppInfo.class);
						if (findById2 == null) {
							LocalAppInfo localApp = new LocalAppInfo();
							localApp.setIconUrl(ai.getIconUrl());
							localApp.setAppName(ai.getAppName());
							localApp.setId(ai.getPackageName());
							Long lastTime = ai.getLastTime();
							localApp.setLastTime(lastTime);
							db.save(localApp);
						} else {
							findById2.setLastTime(ai.getLastTime());
							db.update(findById2);
						}
					}
				}
			}
		}
	}
}
