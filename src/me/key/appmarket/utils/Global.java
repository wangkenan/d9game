package me.key.appmarket.utils;

import android.graphics.Bitmap;

import com.market.d9game.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class Global {
	
	public static final String GAME_MAIN_URL = "http://appmarket.dqchic.com/d9game";
	public static final String FILTERGAME = GAME_MAIN_URL +"/getLocalGameList.php"; 
	public static final String BANNER = GAME_MAIN_URL +"/mobileBanner.php";
	public static final String MAIN_URL = "http://appmarket.dqchic.com/appstore";
	public static final String HOME_PAGE = "/reco.php";
	public static final String RANK_PAGE = "/rank.php";
	public static final String INDEX_PAGE = "/indexdetail.php";
	public static final String APPUPGRADE = "/appUpgrade.php";
	public static final String SEARCH = "/appSearch.php";
	public static final String RECOMMEDNBANNER = "/recommednBanner.php";
	// 应用详情页
	public static final String APP_DETAIL = "/appShare.php";
	public static final String GAME_PAGE = "/appData.php";

	// 分类列表
	public static final String APP_CATEGORY = "/getAppCategory.php";

	public static final String UPGRADEVERSION = "/upgradeVersion.php";

	public static final String HOTSEARCH = "/getHotSearch.php";

	public static final String APPCOMMENT = "/getAppComment.php";

	public static final String TAGLIST = "/getTagList.php";// ?tagid=2
															// 获取精选标签应用List
	// http://appmarket.dqchic.com/appstore/getAppComment.php?appid=1123

	public static final String RECOTAGS = "/getRecoTags.php";// ?tagid=2 1是应用
																// 2是游戏

	public static final int DOWN_DATA_HOME_SUCCESSFULL = 201;
	public static final int DOWN_DATA_HOME_FAILLY = 202;
	public static final int DOWN_DATA_HOME_EMPTY = 203;
	public static final int DOWN_DATA_RANK_SUCCESSFUL = 204;
	public static final int DOWN_DATA_RANK_FAILLY = 205;
	public static final int DOWN_DATA_RANK_EMPTY = 206;
	public static final int DOWN_DATA_SEARCH_EMPTY = 207;

	public static final int DOWN_DATA_SUCCESSFULL = 208;
	public static final int DOWN_DATA_FAILLY = 209;
	public static final int DOWN_DATA_EMPTY = 210;
	
	public final static DisplayImageOptions options = new DisplayImageOptions.Builder()
	.showImageForEmptyUri(R.drawable.a20131008173440)
	.showStubImage(R.drawable.a20131008173440).resetViewBeforeLoading(false)
	.delayBeforeLoading(50).cacheInMemory(true).cacheOnDisc(true)
	.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
	.bitmapConfig(Bitmap.Config.RGB_565).build();

}
