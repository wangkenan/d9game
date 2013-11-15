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
		tableView = (UITableView) findViewById(R.id.tableView);
		 createList();        
	        tableView.commit();
	}
	   private void createList() {
	        tableView.addBasicItem("昵称", "艾丝凡");
	        tableView.addBasicItem("我的积分", "213");
	        tableView.addBasicItem("我的等级", "lv2212");
	        tableView.addBasicItem("我的d9币", "21");
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
