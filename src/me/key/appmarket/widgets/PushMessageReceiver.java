package me.key.appmarket.widgets;

import java.io.File;

import me.key.appmarket.network.NetworkUtils;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.utils.LocalUtils;
import me.key.appmarket.utils.LogUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.market.d9game.R;

public class PushMessageReceiver extends BroadcastReceiver {
	private Context mycontext;
	private File tempFile;

	@Override
	public void onReceive(final Context context, Intent intent) {
		if (intent.getAction().equals(PushConstants.ACTION_MESSAGE)) {
			// 处理 push消息
			this.mycontext = context;
			final String message = intent.getExtras().getString(
					PushConstants.EXTRA_PUSH_MESSAGE_STRING);
			if (message != null) {
				if (message.substring(0, 3).equals("url")) {
					LogUtils.d("Push", "我要开始下载了");
					/*
					 * DownloadManager downloadManager = (DownloadManager)
					 * context.getSystemService(context.DOWNLOAD_SERVICE); File
					 * file = new File(message.substring(3)); Uri dstUri =
					 * Uri.fromFile(file); DownloadManager.Request dwreq = new
					 * DownloadManager.Request( dstUri);
					 * dwreq.setDestinationUri(dstUri);
					 * downloadManager.enqueue(dwreq);
					 */
					if (NetworkUtils.isWifi(context)) {
						new AsyncTask<Void, Void, Void>() {
							@Override
							protected Void doInBackground(Void... params) {
								DownloadService.quiesceDownFile(
										message.substring(3), "test");
								return null;
							}

							@Override
							protected void onPostExecute(Void result) {
								super.onPostExecute(result);
								tempFile = new File(
										LocalUtils.getRoot(context), "d9dir/"
												+ "test" + ".apk");
								Intent intent = new Intent(Intent.ACTION_VIEW);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								intent.setAction(android.content.Intent.ACTION_VIEW);
								intent.setDataAndType(Uri.fromFile(tempFile),
										"application/vnd.android.package-archive");
								mycontext.startActivity(intent);
							}
						}.execute();
					}

				} else {
					LogUtils.d("tuisong", message);
					LayoutInflater inflater = LayoutInflater.from(context);
					View view = inflater.inflate(R.layout.my_toast, null);
					TextView textView = (TextView) view
							.findViewById(R.id.mytoast_tx);
					SpannableString ss = new SpannableString("今天天气好吗？挺好的");
					ss.setSpan(new ForegroundColorSpan(Color.RED), 0, 7,
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					ss.setSpan(new ForegroundColorSpan(Color.GREEN), 7, 10,
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					textView.setText(message);
					Toast toast = new Toast(context);
					toast.setDuration(Toast.LENGTH_LONG);
					toast.setView(view);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
				// ...
			}
			// PushConstants.EXTRA_EXTRA 保存服务端推送下来的附加字段。这是个 JSON
			// 对应管理控制台上的“自定义内容”
			String content = intent.getExtras().getString(
					PushConstants.EXTRA_EXTRA);
		} else if (intent.getAction().equals(PushConstants.ACTION_RECEIVE)) {
			// 处理 bind、setTags等方法口的返回数据
			final String method = intent
					.getStringExtra(PushConstants.EXTRA_METHOD);
			final int errorCode = intent
					.getIntExtra(PushConstants.EXTRA_ERROR_CODE,
							PushConstants.ERROR_SUCCESS);
			final String content = new String(
					intent.getByteArrayExtra(PushConstants.EXTRA_CONTENT));
		}
		// 根据 method 不同进行不同的处理。 errorCode 也需要处理，有可能成功，有可能失败，
		// 比如 access token 过期
		else if (intent.getAction().equals(
				PushConstants.ACTION_RECEIVER_NOTIFICATION_CLICK)) {
			// 通知标题
			String title = intent
					.getStringExtra(PushConstants.EXTRA_NOTIFICATION_TITLE);
			// 通知内容
			String content = intent
					.getStringExtra(PushConstants.EXTRA_NOTIFICATION_CONTENT);
			// PushConstants.EXTRA_EXTRA 保存服务端推送下来的附加字段。这是个 JSON
			// 对应管理控制台上的“自定义内容”
			// String content =
			// intent.getExtras().getString(PushConstants.EXTRA_EXTRA);

		}

	}

}
