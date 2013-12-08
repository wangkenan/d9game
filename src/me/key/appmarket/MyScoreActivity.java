package me.key.appmarket;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.annotation.view.ViewInject;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import br.com.dina.ui.widget.UITableView;

import com.market.d9game.R;
/**
 * 我的积分
 * @author Administrator
 *
 */
public class MyScoreActivity extends FinalActivity {
	@ViewInject(id=R.id.back_onkey,click="onClick")
	ImageView back_onkey;
	 UITableView tableView;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acticvity_myscore);
		MarketApplication.getInstance().getAppLication().add(this);
		tableView = (UITableView) findViewById(R.id.tableView);
		 createList();        
	        tableView.commit();
	}
	   private void createList() {
	        tableView.addBasicItem("版本号", "v1.9");
	        tableView.addBasicItem("开发日期", "2013/3/1");
	        tableView.addBasicItem("开发公司", "北京格尚科技有限公司");
	        tableView.addBasicItem("联系人", "王科楠");
	        tableView.addBasicItem("联系邮箱", "service@geshangtech.com");
	        tableView.addBasicItem("联系电话", "18701450306");
	    }
	   
	   public void onClick(View v){
		 switch (v.getId()) {
		case R.id.back_onkey:
			finish();
			break;

		default:
			break;
		}
	 }
}
