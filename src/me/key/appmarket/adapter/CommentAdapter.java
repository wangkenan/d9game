package me.key.appmarket.adapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.market.d9game.R;

import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.CommentInfo;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CommentAdapter extends BaseAdapter {

	private ArrayList<CommentInfo> commentInfos;
	private LayoutInflater lay;
	private File cache;
	private Context mContext;

	public CommentAdapter(ArrayList<CommentInfo> commentInfos, Context context,
			File cache) {
		super();
		this.commentInfos = commentInfos;
		this.cache = cache;
		mContext = context;
		lay = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return commentInfos.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return commentInfos.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(final int position, View convertvView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder;
		if (convertvView == null) {
			viewHolder = new ViewHolder();
			convertvView = lay.inflate(R.layout.app_comment_item, null);
			viewHolder.name = (TextView) convertvView
					.findViewById(R.id.tv_name);
			viewHolder.time = (TextView) convertvView
					.findViewById(R.id.tv_time);
			viewHolder.comment = (TextView) convertvView
					.findViewById(R.id.tv_comment);
			convertvView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertvView.getTag();
		}
		// asyncloadImage(viewHolder.icon,appInfos.get(position).getIconUrl());

		CommentInfo mCommentInfo = commentInfos.get(position);
		viewHolder.name.setText(mCommentInfo.getUser_name());

		SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		Date date = new Date(System.currentTimeMillis()
				- Long.parseLong(mCommentInfo.getSend_time()));
		String time = sfd.format(date);

		viewHolder.time.setText(time);
		if (mCommentInfo.getContent() != null) {
			viewHolder.comment.setText(mCommentInfo.getContent().trim());
		}

		return convertvView;
	}

	private class ViewHolder {
		private TextView name;
		private TextView time;
		private TextView comment;
	}

	private void asyncloadImage(ImageView iv_header, String path) {
		AsyncImageTask task = new AsyncImageTask(iv_header);
		task.execute(path);
	}

	private final class AsyncImageTask extends AsyncTask<String, Integer, Uri> {

		private ImageView iv_header;

		public AsyncImageTask(ImageView iv_header) {
			this.iv_header = iv_header;
		}

		@Override
		protected Uri doInBackground(String... params) {
			try {
				return ToolHelper.getImageURI(params[0], cache);
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(Uri result) {
			super.onPostExecute(result);
			if (iv_header != null && result != null) {
				iv_header.setImageURI(result);
			}
		}
	}

	/**
	 * @param newsitem
	 */
	public void addNewsItem(CommentInfo newsitem) {
		commentInfos.add(newsitem);
	}
}
