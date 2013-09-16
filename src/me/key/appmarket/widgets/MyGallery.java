package me.key.appmarket.widgets;

import java.io.File;
import java.util.ArrayList;

import com.market.d9game.R;

import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.BannerInfo;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MyGallery extends HorizontalScrollView implements
		OnGestureListener {

	public MyGallery(Context context) {
		super(context);
		this.mContext = context;
	}

	public MyGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
	}

	public MyGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
	}

	private Context mContext; // 调用本view的activity
	private int mWidth; // 宽度onlayout里赋值
	private int selectedItemIndex = -1;// 选中的项下标
	private int displayNum = 2; // 要显示项的数量
	private int defaultIndex = 0; // 默认选中的下标
	private int itemWidth; // 每一项的宽度
	private OnClickListener itemClickListener;// 点击每项的事件，初始化传入，可以通过onclick中的view内的tag获得postion区分
	private Handler handler; // 用来处理选中滚动事件
	private LayoutInflater mInflater;
	private int adjust = -5; // 调整移动位置

	private ArrayList<BannerInfo> bannerList = new ArrayList<BannerInfo>();

	public void setBannerList(ArrayList<BannerInfo> bannerList) {
		if (bannerList != null) {
			this.bannerList = bannerList;
		}
	}

	/**
	 * 完成布局时根据宽度与显示的数量初始化item布局
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (this.mWidth != this.getWidth()) {
			this.mWidth = this.getWidth();
			this.itemWidth = this.mWidth / this.displayNum;
			initImages(itemClickListener);
		}
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 * @param images
	 * @param itemClickListener
	 */
	public void init(Context context, OnClickListener itemClickListener,
			int defaultIndex) {
		this.mContext = context;
		this.mInflater = LayoutInflater.from(context);
		this.defaultIndex = defaultIndex;
		this.setVerticalScrollBarEnabled(false); // 禁用垂直滚动
		this.setHorizontalScrollBarEnabled(false); // 禁用水平滚动
		this.itemClickListener = itemClickListener;
		this.handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.arg1 > msg.arg2) {
					int each = (msg.arg1 - msg.arg2) / 10;
					if (msg.arg2 + each < msg.arg1 && each > 0) {
						scrollTo(msg.arg2 + each + adjust, 0);
					} else {
						scrollTo(msg.arg1 + adjust, 0);
					}
					Message message = new Message();
					message.arg1 = msg.arg1;
					if (each != 0) {
						message.arg2 = msg.arg2 + each;
						sendMessageDelayed(message, 1);
					}
				} else {
					int each = (msg.arg2 - msg.arg1) / 10;
					if (msg.arg2 - each > msg.arg1 && each > 0) {
						scrollTo(msg.arg2 - each + adjust, 0);
					} else {
						scrollTo(msg.arg1 + adjust, 0);
					}
					Message message = new Message();
					message.arg1 = msg.arg1;
					if (each != 0) {
						message.arg2 = msg.arg2 - each;
						sendMessageDelayed(message, 1);
					}
				}
			};
		};
	}

	/**
	 * 绑定图片项
	 * 
	 * @param clickListener
	 */
	private void initImages(OnClickListener clickListener) {
		this.removeAllViews();
		LinearLayout ll = new LinearLayout(mContext);
		ll.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));

		for (int i = 0; i < bannerList.size(); i++) {
			FrameLayout llitem = (FrameLayout) mInflater.inflate(
					R.layout.banner_item, null);
			llitem.setLayoutParams(new RelativeLayout.LayoutParams(
					this.itemWidth, LayoutParams.WRAP_CONTENT));
			ImageView image = (ImageView) llitem.findViewById(R.id.icon);
			asyncloadImage(image, bannerList.get(i).getPicurl());
			image.setTag(i);
			image.setOnClickListener(clickListener);
			ll.addView(llitem);

			// imageViews.add(image);
			// textViews.add(textView);
		}
		this.addView(ll);
		selectItem(defaultIndex);
	}

	/**
	 * 选中某个item
	 * 
	 * @param position
	 */
	public void selectItem(Integer position) {
		selectedItemIndex = position;
		Message msg = new Message();
		msg.arg1 = position * itemWidth;
		msg.arg2 = getScrollX();
		this.handler.sendMessage(msg);
	}

	// /**
	// * 选择下一个
	// * @return 选中的index
	// */
	// public int selectNext(){
	// if(selectedItemIndex+1 < imageViews.size()){
	// selectItem(selectedItemIndex+1);
	// }
	// return selectedItemIndex;
	// }

	// /**
	// * 选择上一个
	// * @return 选中的index
	// */
	// public int selectPrev(){
	// if(selectedItemIndex>0){
	// selectItem(selectedItemIndex-1);
	// }
	// return selectedItemIndex;
	// }

	private ViewPager mPager;

	public ViewPager getmPager() {
		return mPager;
	}

	public void setmPager(ViewPager mPager) {
		this.mPager = mPager;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		mPager.requestDisallowInterceptTouchEvent(true);
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		mPager.requestDisallowInterceptTouchEvent(true);
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		mPager.requestDisallowInterceptTouchEvent(true);
		if (event.getAction() == MotionEvent.ACTION_DOWN) {

		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			int scrollX = this.getScrollX();

			int temp = scrollX / this.itemWidth;
			int scrollW = scrollX % this.itemWidth;
			if (scrollW > this.itemWidth / 2) {
				selectItem(temp + 1);
			} else {
				selectItem(temp);
			}
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {

		}
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	private void asyncloadImage(ImageView iv_header, String path) {
		File cache = new File(Environment.getExternalStorageDirectory(),
				"cache");
		if (!cache.exists()) {
			cache.mkdirs();
		}
		AsyncImageTask task = new AsyncImageTask(iv_header, cache);
		task.execute(path);
	}

	private final class AsyncImageTask extends AsyncTask<String, Integer, Uri> {
		private File cache;
		private ImageView iv_header;

		public AsyncImageTask(ImageView iv_header, File cache) {
			this.iv_header = iv_header;
			this.cache = cache;
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
}