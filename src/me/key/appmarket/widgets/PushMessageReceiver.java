package me.key.appmarket.widgets;

import me.key.appmarket.utils.LogUtils;

import com.baidu.android.pushservice.PushConstants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PushMessageReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(PushConstants.ACTION_MESSAGE)) {
			// 处理 push消息
			String message = intent.getExtras().getString(
					PushConstants.EXTRA_PUSH_MESSAGE_STRING);
			if (message != null) {
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

			LogUtils.d("tuisong", content);
		}

	}

}
