package me.key.appmarket;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import me.key.appmarket.adapter.CommentAdapter;
import me.key.appmarket.adapter.GalleryAdapter;
import me.key.appmarket.adapter.GridViewAdapter;
import me.key.appmarket.network.AppDetailRequest;
import me.key.appmarket.network.AppDetailResponse;
import me.key.appmarket.network.HttpRequest.OnResponseListener;
import me.key.appmarket.network.HttpResponse;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.CommentInfo;
import me.key.appmarket.utils.Global;
import me.key.appmarket.utils.LogUtils;
import me.key.appmarket.utils.MyAsynTask;
import me.key.appmarket.widgets.CustomScrollView;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.market.d9game.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

/**
 * 应用详情页
 */
public class AppDetailActivity extends Activity implements OnClickListener {

	private static final String TAG = "AppDetailActivity";

	private GalleryAdapter galleryAdapter;
	private GridViewAdapter gridviewAdapter;

	private ImageView backIcon;
	private ImageView appIcon;
	private TextView appName;
	// private TextView appDeveloper;
	private TextView appDownloadCounts;

	private TextView appVersion;
	private TextView appSize1, appSize2;
	private TextView appUpdateTime;

	// private EllipsizingTextView appDes;
	private TextView appDes;
	// private ImageButton appDesExpand;
	private TextView tvOperate;
	private ImageView ivOperate;

	private CustomScrollView scrollView;

	private String idx;
	private String name;
	private boolean isInstalled = false;
	private boolean isDowned = false;
	private boolean isDowning = false;
	private ProgressDialog myDialog;
	private TextView appDesc, appComment;
	private AppInfo appInfo;
	private String appid;
	private ArrayList<CommentInfo> commentList = new ArrayList<CommentInfo>();
	private ListView commentListView;
	private CommentAdapter mCommentAdapter;
	// 是否是下载状态
	private boolean isDownLoading;
	private long count;

	private int currentIndex = 0;

	private int gridviewSpac = 10;
	private int gridviewXpadding = 10;
	private int gridviewYpadding = 10;
	File cache = new File(Environment.getExternalStorageDirectory(),
			"detail_cache");
	private SharedPreferences sp;
	private File tempFile;

	private Gallery picGallery;

	private GridView picGridview;
	private ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();;
	private MyInstalledReceiver installedReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!cache.exists()) {
			cache.mkdirs();
		}

		setContentView(R.layout.app_detail_main);
		MarketApplication.getInstance().getAppLication().add(this);

		setupView();
		addListener();

		installedReceiver = new MyInstalledReceiver();
		IntentFilter filter = new IntentFilter();

		filter.addAction("android.intent.action.PACKAGE_ADDED");
		filter.addDataScheme("package");
		this.registerReceiver(installedReceiver, filter);
		appid = getIntent().getStringExtra("appid");
		LogUtils.d("appid", appid);
		appInfo = (AppInfo) getIntent().getSerializableExtra("appinfo");
		if (appInfo == null) {
			appInfo = new AppInfo();
		}
		if (appid != null) {
			myDialog = ProgressDialog.show(this, "正在连接服务器..", "连接中,请稍后..",
					true, true);
		}
		autoLoad(appid);

		sp = getSharedPreferences("down", MODE_PRIVATE);
		tempFile = new File(Environment.getExternalStorageDirectory(),
				"/market/" + appInfo.getAppName() + ".apk");

		appDesc.setSelected(true);

		mCommentAdapter = new CommentAdapter(commentList,
				AppDetailActivity.this, cache);
		commentListView.setAdapter(mCommentAdapter);

		// scrollView.setVisibility(View.GONE);
		// commentListView.setVisibility(View.VISIBLE);
		new Thread(runCommentData).start();
	}

	private void addListener() {
		// appDesExpand.setOnClickListener(this);
		ivOperate.setOnClickListener(this);
		backIcon.setOnClickListener(this);
		ivOperate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (appInfo.isCanUpdate()) {
					File tempFile = new File(Environment
							.getExternalStorageDirectory(), "/market/"
							+ appInfo.getAppName() + ".apk");
					List<AppInfo> down_temp = new ArrayList<AppInfo>();
					if (tempFile.exists()) {
						tempFile.delete();
					}
					DownloadService.downNewFile(appInfo, 0, 0, null);
					Intent intent = new Intent();
					Intent downState = new Intent();

					downState.setAction(tempFile.getAbsolutePath() + "down");
					downState.putExtra("isPause", appInfo.isIspause());
					AppDetailActivity.this.sendBroadcast(downState);
					intent.setAction(MarketApplication.PRECENT);
					AppDetailActivity.this.sendBroadcast(intent);
					Toast.makeText(AppDetailActivity.this,
							appInfo.getAppName() + " 开始升级...",
							Toast.LENGTH_SHORT).show();

				}
				if (appInfo.isInstalled()) {
					AppUtils.launchApp(AppDetailActivity.this,
							appInfo.getPackageName());
				} else if (DownloadService.isDownLoading(Integer
						.parseInt(appInfo.getIdx()))) {
					LogUtils.d("test", "暂停");
					File tempFile = DownloadService.CreatFileName(appInfo
							.getAppName());
					Intent intent = new Intent();
					intent.setAction(tempFile.getAbsolutePath());
					sendBroadcast(intent);
					Intent downState = new Intent();
					downState.setAction(tempFile.getAbsolutePath() + "down");
					downState.putExtra("isPause", !appInfo.isIspause());
					sendBroadcast(downState);
					LogUtils.d("pro", "我发出了暂停中下载广播");
					if (!appInfo.isIspause()) {
						tvOperate.setText("暂停");
						// tvOperate.setText("暂停");
						appInfo.setDown(false);
					} else {
						tvOperate.setText(count + "%");
						// tvOperate.setText("下载");
						ivOperate.setImageResource(R.drawable.install_btn);
						appInfo.setDown(true);
						/*
						 * v1.progress_view.setVisibility(View.VISIBLE);
						 * v1.tvdown.setVisibility(View.INVISIBLE);
						 */
					}
					appInfo.setIspause(!appInfo.isIspause());
					LogUtils.d("APPDETAIL", "我现在是" + appInfo.isIspause() + "状态");
				} else if (DownloadService.isDownLoaded(appInfo.getApkName())) {
					// 已经下载
					DownloadService.Instanll(appInfo.getAppName(),
							AppDetailActivity.this);
				} else if (!appInfo.isInstalled()) {
					/*
					 * Log.e("tag", "appname = " +
					 * appInfos.get(position).getAppName());
					 */

					Intent downState = new Intent();
					downState.setAction(tempFile.getAbsolutePath() + "down");
					downState.putExtra("isPause", appInfo.isIspause());
					sendBroadcast(downState);
					LogUtils.d("pro", "我发出了暂停中下载广播safdasfasf");
					long length = sp.getLong(tempFile.getAbsolutePath(), 0);
					/*
					 * DownloadService.downNewFile(appInfos.get(position)
					 * .getAppUrl(), Integer.parseInt(appInfos.get(
					 * position).getIdx()), appInfos.get(position)
					 * .getAppName(),length,0);
					 */
					LogUtils.d("Local",
							appInfo.getId() + "id" + appInfo.getAppName());
					DownloadService.downNewFile(appInfo, length, 0, null);
					appInfo.setDown(true);
					Intent intent = new Intent();
					intent.setAction(MarketApplication.PRECENT);
					sendBroadcast(intent);
					LogUtils.d("pro", "我发出了暂停中下载广播but");
					Toast.makeText(AppDetailActivity.this,
							appInfo.getAppName() + " 开始下载...",
							Toast.LENGTH_SHORT).show();
					tvOperate.setText(count + "%");
					// tvOperate.setText("下载");
					ivOperate.setImageResource(R.drawable.install_btn);
					/*
					 * v1.progress_view.setVisibility(View.VISIBLE);
					 * v1.tvdown.setVisibility(View.INVISIBLE);
					 */
				}

			}
		});
		appDesc.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				appDesc.setSelected(true);
				appComment.setSelected(false);
				appDesc.setBackgroundResource(R.drawable.bk_navigate_tv_appdetail);
				appComment.setBackgroundColor(00000000);

				commentListView.setVisibility(View.GONE);
				scrollView.setVisibility(View.VISIBLE);
			}
		});
		appComment.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				appComment.setSelected(true);
				appDesc.setSelected(false);
				appComment
						.setBackgroundResource(R.drawable.bk_navigate_tv_appdetail);
				appDesc.setBackgroundColor(00000000);

				scrollView.setVisibility(View.GONE);
				commentListView.setVisibility(View.VISIBLE);
			}
		});

	}

	private void setupView() {
		backIcon = (ImageView) findViewById(R.id.iv_back_app_detail);
		appIcon = (ImageView) findViewById(R.id.app_icon_app_detail);
		appName = (TextView) findViewById(R.id.app_name_app_detail);
		appDownloadCounts = (TextView) findViewById(R.id.app_download_counts_app_detail);

		picGallery = (Gallery) findViewById(R.id.gallery_app_detail);
		picGridview = (GridView) findViewById(R.id.gridview_app_detail);

		appVersion = (TextView) findViewById(R.id.app_version_app_detail);
		appSize1 = (TextView) findViewById(R.id.app_size1_app_detail);
		appSize2 = (TextView) findViewById(R.id.app_size2_app_detail);
		appUpdateTime = (TextView) findViewById(R.id.app_update_time_app_detail);

		appDes = (TextView) findViewById(R.id.app_description_app_detail);
		// appDes = (EllipsizingTextView)
		// findViewById(R.id.app_description_app_detail);
		// appDesExpand = (ImageButton)
		// findViewById(R.id.app_des_expand_app_detail);
		tvOperate = (TextView) findViewById(R.id.tv_operate_app_detail);
		ivOperate = (ImageView) findViewById(R.id.iv_operate_app_detail);

		scrollView = (CustomScrollView) findViewById(R.id.scroll_app_app_detail);

		// appDownloadCounts.setText(getString(
		// R.string.app_detail_download_counts, ""));
		// appVersion.setText(getString(R.string.app_detail_version, ""));
		// appUpdateTime.setText(getString(R.string.app_detail_update_time,
		// ""));
		// appSize1.setText(getString(R.string.app_detail_size, ""));
		// appSize2.setText(getString(R.string.app_detail_size, ""));

		appDesc = (TextView) findViewById(R.id.tv_appdesc_app_detail);
		appComment = (TextView) findViewById(R.id.tv_appcomment_app_detail);

		commentListView = (ListView) findViewById(R.id.list_comment_app_detail);
	}

	private void autoLoad(String appid) {
		new AppDetailRequest(appid).execute(new OnResponseListener() {

			@Override
			public void onGetResponse(HttpResponse resp) {
				final AppDetailResponse response = (AppDetailResponse) resp;
				if (myDialog != null) {
					myDialog.dismiss();
				}
				if (response != null) {
					idx = response.getIdx();
					name = response.getAppName();

					isInstalled = AppUtils.isInstalled(response
							.getAppPackageName());
					// 是否已经下载完
					isDowned = DownloadService.isDownLoaded(appInfo.getApkName());
					// 是否正在下载
					isDowning = DownloadService.isDownLoading(Integer
							.parseInt(idx));

					// appDesExpand.setSelected(true);

					if (response.getAppName() != null) {
						appName.setText(response.getAppName());
						appInfo.setAppName(response.getAppName());

					}
					if (response.getAppDownloadCounts() != null) {
						appDownloadCounts.setText(getString(
								R.string.app_detail_download_counts,
								response.getAppDownloadCounts()));
						appInfo.setAppDownCount(response.getAppDownloadCounts());
					}
					if (response.getAppVersion() != null) {
						appVersion.setText(getString(
								R.string.app_detail_version,
								response.getAppVersion()));
						appInfo.setVersion(response.getAppVersion());
					}
					if (response.getAppUpdateTime() != null) {
						appUpdateTime.setText(getString(
								R.string.app_detail_update_time,
								response.getAppUpdateTime()));
					}
					if (response.getAppSize() != null) {
						appSize1.setText(getString(R.string.app_detail_size,
								formetFileSize(Long.parseLong(response
										.getAppSize()))));
						appSize2.setText(getString(R.string.app_detail_size,
								formetFileSize(Long.parseLong(response
										.getAppSize()))));
						appInfo.setAppSize(response.getAppSize());
					}
					if (response.getAppUrl() != null) {
						tvOperate.setTag(response.getAppUrl());
						appInfo.setAppUrl(response.getAppUrl());

					}
					if (response.getAppDes() != null) {
						String appdes = ToDBC(response.getAppDes());
						appDes.setText(appdes);
						appInfo.setAppDescri(response.getAppDes());
					}
					if (response.getAppIconUrl() != null) {
						ImageLoader.getInstance().displayImage(
								Global.MAIN_URL + response.getAppIconUrl(),
								appIcon, Global.options);
						if (appInfo.getIconUrl() == null) {
							LogUtils.d("AppDetail", response.getAppIconUrl());
							appInfo.setIconUrl(response.getAppIconUrl());
						}
					}
					if (response.getIdx() != null) {
						appInfo.setIdx(response.getIdx());
					}
					if (response.getAppPackageName() != null) {
						if (appInfo.getId() == null) {
							appInfo.setId(response.getAppPackageName());
						}
					}
					appInfo.setLastTime(Long.MAX_VALUE);
					File tempFile = new File(Environment
							.getExternalStorageDirectory(), "/market/"
							+ appInfo.getAppName() + ".apk");
					SharedPreferences sp = getSharedPreferences("down",
							MODE_PRIVATE);
					boolean isDownLoaded = DownloadService.isDownLoaded(appInfo.getApkName()
							);
					int idx = Integer.parseInt(appInfo.getIdx());
					isDownLoading = DownloadService.isDownLoading(idx);
					/*
					 * if (isInstalled) { appDownload.setText("打开"); } else if
					 * (isDowning) { appDownload.setText("下载中"); } else if
					 * (isDowned) { appDownload.setText("安装"); } else {
					 * appDownload.setText("下载"); }
					 */
					setDownState(0);
					// appOperate.setCompoundDrawablesWithIntrinsicBounds(
					// getResources().getDrawable(
					// R.drawable.btn_icon_download_disable),
					// null, null, null);
					/*
					 * appDownload.setText("下载("+
					 * formetFileSize(Long.parseLong(response
					 * .getAppSize()))+")");
					 */
					/*
					 * if (response.getAppImgUrl() != null) { for (int i = 0; i
					 * < response.getAppImgUrl().length; i++) { ImageView iv =
					 * new ImageView(AppDetailActivity.this);
					 * iv.setRotation(-90.0f);
					 * iv.setScaleType(ScaleType.FIT_XY); //asyncloadImage(iv,
					 * response.getAppImgUrl()[i]);
					 * ImageLoader.getInstance().displayImage
					 * (response.getAppImgUrl()[i], iv, options); //
					 * LayoutParams params = new LayoutParams(200, //
					 * LayoutParams.MATCH_PARENT); MarginLayoutParams params =
					 * new MarginLayoutParams( LayoutParams.WRAP_CONTENT, 200);
					 * // params.setMargins(0, 0, 20, 0); iv.setPadding(0, 0,
					 * 20, 0); iv.setLayoutParams(params);
					 * appImgGallery.addView(iv); } }
					 */
					final String[] appImgUrl = response.getAppImgUrl();
					new MyAsynTask(AppDetailActivity.this, null) {
						InputStream is;

						@Override
						protected Void doInBackground(Void... params) {
							try {
								for (int i = 0; i < appImgUrl.length; i++) {
									URL url = new URL(appImgUrl[i]);
									HttpURLConnection connection = (HttpURLConnection) url
											.openConnection();
									is = connection.getInputStream();
									BitmapFactory.Options options=new BitmapFactory.Options();
									options.inJustDecodeBounds = false;
									options.inSampleSize = 2;   //压缩比例
									Bitmap bitmap =BitmapFactory.decodeStream(is,null,options);
									/*Bitmap bitmap = BitmapFactory
											.decodeStream(is);*/
									int width = bitmap.getWidth();
									int height = bitmap.getHeight();
									
									Matrix matrix = new Matrix();
									if (height > width) {
										matrix.setRotate(-90);
									}
									// matrix.setScale(scale, scale);
									Bitmap newBitmap = Bitmap.createBitmap(
											bitmap, 0, 0, width, height,
											matrix, false);
									bitmaps.add(newBitmap);
								}

							} catch (Exception e) {
								e.printStackTrace();
							}
							return super.doInBackground(params);
						}

						@Override
						protected void onPostExecute(Void result) {
							super.onPostExecute(result);
							galleryAdapter = new GalleryAdapter(
									AppDetailActivity.this, bitmaps);
							picGallery.setAdapter(galleryAdapter);
						}

					}.exe();

					// Gallery 赋值
					gridviewAdapter = new GridViewAdapter(
							AppDetailActivity.this, picGallery, appImgUrl);
					picGridview.setAdapter(gridviewAdapter);
					gridviewAdapter.autoPlay();
					Bitmap bmp = new BitmapFactory().decodeResource(
							getResources(), R.drawable.ball_unselected);
					int width = bmp.getWidth();
					int height = bmp.getHeight();
					picGridview.setColumnWidth(width);
					picGridview.setHorizontalSpacing(gridviewSpac);
					picGridview.setNumColumns(appImgUrl.length);
					LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) picGridview
							.getLayoutParams();
					lp.width = width * appImgUrl.length + gridviewSpac
							* (appImgUrl.length - 1) + (gridviewXpadding << 1);
					lp.height = height + (gridviewYpadding << 1);
					picGridview.setLayoutParams(lp);
					picGridview.setPadding(gridviewXpadding, gridviewYpadding,
							gridviewXpadding, gridviewYpadding);
					// Gallery OnItemSelected
					picGallery
							.setOnItemSelectedListener(new Gallery.OnItemSelectedListener() {

								@Override
								public void onItemSelected(
										AdapterView<?> parent, View view,
										int position, long id) {
									// TODO Auto-generated method stub
									// 设置当前选中的Index
									gridviewAdapter.currentIndex = position;
									// 改变GridView显示
									gridviewAdapter.notifyDataSetInvalidated();
								}

								@Override
								public void onNothingSelected(
										AdapterView<?> arg0) {
									// TODO Auto-generated method stub

								}

							});
					// Gallery OnItemClick
					// picGridview
					// .setOnItemClickListener(new OnItemClickListener() {
					//
					// @Override
					// public void onItemClick(AdapterView<?> parent,
					// View view, int position, long id) {
					// // TODO Auto-generated method stub
					// // 设置当前选中的Index
					// currentIndex = position;
					// // 改变GridView显示
					// gridviewAdapter.notifyDataSetInvalidated();
					// // 改变Gallery显示
					// picGallery.setSelection(currentIndex);
					// }
					//
					//		});
					// 释放图片资源
					if (bmp != null && !bmp.isRecycled()) {
						bmp.recycle();
						bmp = null;
					}

				} else {
					Toast.makeText(AppDetailActivity.this, "获取失败",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	// /**
	// * 下载图片，并显示
	// *
	// * @param iv_header
	// * @param path
	// */
	// private void asyncloadImage(ImageView iv_header, String path) {
	// Log.d(TAG, "download pic url: " + path);
	// AsyncImageTask task = new AsyncImageTask(iv_header);
	// task.execute(path);
	// }
	//
	// private final class AsyncImageTask extends AsyncTask<String, Integer,
	// Uri> {
	//
	// private ImageView iv_header;
	//
	// public AsyncImageTask(ImageView iv_header) {
	// this.iv_header = iv_header;
	// }
	//
	// @Override
	// protected Uri doInBackground(String... params) {
	// try {
	// return ToolHelper.getImageURI(params[0], cache);
	// } catch (Exception e) {
	// return null;
	// }
	// }
	//
	// @Override
	// protected void onPostExecute(Uri result) {
	// super.onPostExecute(result);
	// if (iv_header != null && result != null) {
	// // iv_header.setImageURI(result);
	// ImageLoader.getInstance().displayImage(result.toString(),
	// iv_header, options);
	// }
	// }
	// }

	public static String ToDBC(String input) {
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 12288) {
				c[i] = (char) 32;
				continue;
			}
			if (c[i] > 65280 && c[i] < 65375)
				c[i] = (char) (c[i] - 65248);
		}
		return new String(c);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// case R.id.app_des_expand_app_detail:
		// if (appDesExpand.isSelected()) {
		// appDesExpand.setSelected(false);
		// appDes.setMaxLines(100);
		// } else {
		// appDesExpand.setSelected(true);
		// appDes.setMaxLines(5);
		// }
		// break;
		/*
		 * case R.id.app_download: if (isInstalled) {
		 * AppUtils.launchApp(AppDetailActivity.this, name); } else if
		 * (isDowning) {
		 * 
		 * } else if (isDowned) { DownloadService.Instanll(name,
		 * AppDetailActivity.this); } else { if (appDownload.getTag() != null) {
		 * DownloadService.downNewFile((String) appDownload.getTag(), idx !=
		 * null ? Integer.parseInt(idx) : 0, name,0,0);
		 * Toast.makeText(AppDetailActivity.this, name + " 开始下载...",
		 * Toast.LENGTH_SHORT).show(); Log.d(TAG, "download apk name=" + name +
		 * "  idx=" + idx + " url=" + appDownload.getTag());
		 * DownloadService.downNewFile(appInfo, 0, 0, null); } } break;
		 */
		case R.id.iv_back_app_detail:
			AppDetailActivity.this.finish();
			break;
		default:
			break;
		}
	}

	private String formetFileSize(long fileS) {
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "K";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "M";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "G";
		}
		return fileSizeString;
	}

	Runnable runCommentData = new Runnable() {
		@Override
		public void run() {
			String str = ToolHelper.donwLoadToString(Global.MAIN_URL
					+ Global.APPCOMMENT + "?appid=" + appid);
			if (str.equals("null")) {
				mHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL);
			} else if (str.equals("-1")) {
				mHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_FAILLY);
			} else {
				ParseCommentJson(str);
			}
		}
	};

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Global.DOWN_DATA_HOME_FAILLY: {
			}
				break;
			case Global.DOWN_DATA_HOME_SUCCESSFULL: {
				mCommentAdapter.notifyDataSetChanged();
			}
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	private void ParseCommentJson(String str) {
		try {
			JSONArray jsonArray = new JSONArray(str);
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String content = jsonObject.getString("content");
				String send_time = jsonObject.getString("send_time");
				String user_name = jsonObject.getString("user_name");
				String score = jsonObject.getString("score");

				CommentInfo mCommentInfo = new CommentInfo(content, send_time,
						user_name, score);

				commentList.add(mCommentInfo);
			}
			mHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_SUCCESSFULL);
		} catch (Exception ex) {
			// homeDataHandler.sendEmptyMessage(Global.DOWN_DATA_HOME_FAILLY);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		registerPrecent();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		unregisterPrecent();
		MobclickAgent.onPause(this);
	}
	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		this.unregisterReceiver(installedReceiver);
	}

	PrecentReceiver mPrecentReceiver;


	private void registerPrecent() {
		mPrecentReceiver = new PrecentReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(MarketApplication.PRECENT);
		this.registerReceiver(mPrecentReceiver, filter);
	}

	private void unregisterPrecent() {
		if (mPrecentReceiver != null) {
			this.unregisterReceiver(mPrecentReceiver);
		}
	}

	class PrecentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			count = sp.getLong(tempFile.getAbsolutePath() + "precent", 0);
			if (intent.getAction().equals(MarketApplication.PRECENT)) {
				if (appInfo.isIspause()) {
					// tvOperate.setText("暂停" + "(" + count + "%)");
					tvOperate.setText("暂停");
					// ivOperate.setImageResource(resId);
				} else {
					tvOperate.setText(count + "%");
					// tvOperate.setText("下载");
					ivOperate.setImageResource(R.drawable.install_btn);
				}
				if (DownloadService.isDownLoaded(appInfo.getApkName())) {
					tvOperate.setText("安装");
					ivOperate.setImageResource(R.drawable.install_btn);
				}
				/*
				 * if(idx != null) isDowning = DownloadService
				 * .isDownLoading(Integer.parseInt(idx));
				 */
				/*
				 * isInstalled = AppUtils.isInstalled(name); // 是否已经下载完 isDowned
				 * = DownloadService.isDownLoaded(name); // 是否正在下载 if(idx !=
				 * null) { isDowning = DownloadService
				 * .isDownLoading(Integer.parseInt(idx));
				 * 
				 * if (isInstalled) { appDownload.setText("打开"); } else if
				 * (isDowning) { appDownload.setText("下载中"); } else if
				 * (isDowned) { appDownload.setText("安装"); } else {
				 * appDownload.setText("下载"); }
				 */
			}
		}
	}

	public void setDownState(final int position) {

		Drawable mDrawable;
		// v1.progress_view.setProgress(0);
		// v1.progress_view.setVisibility(View.VISIBLE);
		final File tempFile = new File(
				Environment.getExternalStorageDirectory(), "/market/"
						+ appInfo.getAppName() + ".apk");
		count = sp.getLong(tempFile.getAbsolutePath() + "precent", 0);

		boolean isDownLoaded = DownloadService.isDownLoaded(appInfo.getApkName()
				);
		int idx = Integer.parseInt(appInfo.getIdx());
		isDownLoading = DownloadService.isDownLoading(idx);
		if (appInfo.isIspause()) {
			LogUtils.d("ture", appInfo.isIspause() + "");
			// tvOperate.setText("暂停" + "(" + count + "%)");
			tvOperate.setText("暂停");

			// v1.progress_view.setProgress(DownloadService.getPrecent(idx));
			LogUtils.d("new", "我是下载中暂停" + appInfo.getAppName());
			if (!isDownLoaded) {
				LogUtils.d("new", "我执行了下载中暂停" + appInfo.getAppName());
				// v1.progress_view.setVisibility(View.INVISIBLE);
				// v1.tvdown.setVisibility(View.VISIBLE);
			}
		} else {
			tvOperate.setText(count + "%");
			// tvOperate.setText("下载");
			ivOperate.setImageResource(R.drawable.install_btn);
			LogUtils.d("new", "我是暂停中下载" + appInfo.getAppName());
			if (!isDownLoaded) {
				LogUtils.d("new", "我执行了暂停中下载" + appInfo.getAppName());
				// v1.progress_view.setVisibility(View.VISIBLE);
				// v1.tvdown.setVisibility(View.INVISIBLE);
				// v1.progress_view.setProgress(DownloadService.getPrecent(idx));
				LogUtils.d("ture", isDownLoading + "isDown");
				LogUtils.d("newdowndown", "我变成下载中了" + appInfo.getAppName());
			}
		}
		if (appInfo.isDown()) {
			// v1.progress_view.setProgress(DownloadService.getPrecent(idx));
			LogUtils.d("ture", isDownLoading + "isDown");
			LogUtils.d("newdowndown", "我变成下载中了" + appInfo.getAppName());
			/*
			 * //v1.tvdown.setText("下载中");
			 * v1.progress_view.setVisibility(View.VISIBLE);
			 * v1.tvdown.setVisibility(View.INVISIBLE);
			 * 
			 * Drawable mDrawableicon = mContext.getResources().getDrawable(
			 * R.drawable.downloading);
			 * v1.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
			 * mDrawableicon, null, null);
			 */

		} else if (isDownLoaded) {
			/*
			 * Drawable mDrawableicon = mContext.getResources().getDrawable(
			 * R.drawable.downloaded);
			 * v1.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
			 * mDrawableicon, null, null);
			 */
			tvOperate.setText("安装");
			ivOperate.setImageResource(R.drawable.install_btn);
			/*
			 * v1.progress_view.setProgress(100);
			 * v1.progress_view.setVisibility(View.INVISIBLE);
			 * v1.tvdown.setVisibility(View.VISIBLE);
			 */
		} else if (!isDownLoading) {
			tvOperate.setText("下载");
			ivOperate.setImageResource(R.drawable.install_btn);
			/*
			 * mDrawable = mContext.getResources().getDrawable(
			 * R.layout.mydown_buton);
			 * v1.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
			 * mDrawable, null, null);
			 */
			// 获取将要下载的文件名，如果本地存在该文件，则取出该文件

			// LogUtils.d("sa", tempFile.getAbsolutePath());
			/*
			 * v1.progress_view.setVisibility(View.INVISIBLE);
			 * v1.tvdown.setVisibility(View.VISIBLE);
			 */
			long length = sp.getLong(tempFile.getAbsolutePath(), 0);
			// LogUtils.d("sa", length+"");
			if (length != 0 && DownloadService.isExist(appInfo.getAppName())) {
				LogUtils.d("test", "已经存在");
				// tvOperate.setText("暂停" + "(" + count + "%)");
				tvOperate.setText("暂停");

				// v1.progress_view.setProgress(count);
			} else if (length != 0
					&& !DownloadService.isExist(appInfo.getAppName())) {
				Editor edit = sp.edit();
				edit.remove(tempFile.getAbsolutePath());
				edit.commit();
			}
		}
		if (appInfo.isInstalled()) {
			tvOperate.setText("打开");
			ivOperate.setImageResource(R.drawable.one_key);
			/*
			 * v1.progress_view.setVisibility(View.INVISIBLE);
			 * v1.tvdown.setVisibility(View.VISIBLE);
			 * v1.progress_view.setProgress(100);
			 */
			/*
			 * Drawable mDrawableicon = mContext.getResources().getDrawable(
			 * R.drawable.action_type_software_update);
			 * v1.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
			 * mDrawableicon, null, null);
			 */
		}
		/*
		 * v1.progress_view.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) {
		 * v1.tvdown.setVisibility(View.VISIBLE);
		 * v1.progress_view.setVisibility(View.INVISIBLE);
		 * v1.tvdown.setText("暂停"); appInfo.setDown(false); LogUtils.d("test",
		 * "暂停"); File tempFile =
		 * DownloadService.CreatFileName(appInfo.getAppName()); Intent intent =
		 * new Intent(); intent.setAction(tempFile.getAbsolutePath());
		 * sendBroadcast(intent); Intent downState = new Intent();
		 * downState.setAction(tempFile.getAbsolutePath() + "down");
		 * downState.putExtra("isPause", !appInfo .isIspause());
		 * sendBroadcast(downState); LogUtils.d("pro", "我发出了下载中暂停广播");
		 * appInfo.setIspause( !appInfo.isIspause()); } });
		 */
		if (appInfo.isCanUpdate()) {
			tvOperate.setText("升级");
			ivOperate.setImageResource(R.drawable.update_btn);
		}

	}

	class MyInstalledReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 接收安装广播
			if (intent.getAction()
					.equals("android.intent.action.PACKAGE_ADDED")) {
				String packageName = intent.getDataString().substring(8);
				LogUtils.d("Search", "安装了:" + packageName + "包名的程序");

				MarketApplication.getInstance().reflashAppList();
				String installAppName = AppUtils.getAppName(context,
						packageName);
				appInfo.setInstalled(true);
				tvOperate.setText("打开");
				ivOperate.setImageResource(R.drawable.one_key);
				ivOperate.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						AppUtils.launchApp(AppDetailActivity.this,
								appInfo.getPackageName());
					}
				});
			}
		}
	}
}
