package me.key.appmarket.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;

import com.market.d9game.R;

public class GestureViewFliper extends ViewFlipper implements OnGestureListener {  
	  
    GestureDetector gestureDetector = null;  
    private Context mContext = null;  
    FlipperFacousChangedListener flipperFacousChangedListener=null;  
  
    public GestureViewFliper(Context mContext) {  
        super(mContext);  
  
    }  
  
    public GestureViewFliper(Context mContext, AttributeSet attrs) {  
        super(mContext, attrs);  
        this.mContext = mContext;  
        gestureDetector = new GestureDetector(mContext, this);  
        setLongClickable(true);  
        setOnTouchListener(new OnTouchListener() {  
  
            @Override  
            public boolean onTouch(View v, MotionEvent event) {  
                // TODO Auto-generated method stub  
                return gestureDetector.onTouchEvent(event);  
            }  
        });  
    }  
  
    @Override  
    public void startFlipping() {  
        // TODO Auto-generated method stub  
        super.startFlipping();  
        setInAnimation(AnimationUtils.loadAnimation(mContext,  
                R.anim.gallery_right_in_anim));     
        setOutAnimation(AnimationUtils.loadAnimation(mContext,  
                R.anim.gallery_right_out_anim));  
        
    }  
  
    @Override  
    public boolean onTouchEvent(MotionEvent event) {  
        // TODO Auto-generated method stub  
        stopFlipping();       //�û������Ļʱ��ֹͣ����  
        setAutoStart(false);   //ȡ���Զ�����  
        return this.gestureDetector.onTouchEvent(event);   //��touch�¼�����gesture����  
    }  
  
    @Override  
    public boolean onDown(MotionEvent e) {  
        // TODO Auto-generated method stub  
       // <span style="color:#ff0000;">
    	return true;//</span> // ȱʡֵ��false,��onTouchEvent�󴥷������Ϊfalse��onFling���ò���down���¼���������  
    }  
  
    /* 
     * ��д��onFling��Ϊ���ж����ƣ������ƻ��� 
     *  
     * @see android.view.GestureDetector.OnGestureListener#onFling(android.view. 
     * MotionEvent, android.view.MotionEvent, float, float) 
     */  
  
    @Override  
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,  
            float velocityY) {  
        // TODO Auto-generated method stub  
        if (e2.getX() - e1.getX() > 120) {    // ����໬��  
            setInAnimation(AnimationUtils.loadAnimation(mContext,  
                    R.anim.gallery_left_in_anim));     //���ý������  
            setOutAnimation(AnimationUtils.loadAnimation(mContext,  
                    R.anim.gallery_left_out_anim));  
            //�û����ƻ��������ٴο�ʼ�Զ�����  
            showPrevious();  
            
            setAutoStart(true);  
            startFlipping();  
  
            return true;  
        } else if (e2.getX() - e1.getX() < -120) {   //���Ҳ໭��  
            setOutAnimation(AnimationUtils.loadAnimation(mContext,  
                    R.anim.gallery_right_out_anim));         //���ý������  
            setInAnimation(AnimationUtils.loadAnimation(mContext,  
                    R.anim.gallery_right_in_anim));            
            //���������ٴ��Զ�����  
            showNext(); 
            setAutoStart(true);  
            startFlipping();  
  
            return true;  
        }  
        return false;  
    }  
      
      
  
    @Override  
    public void showNext() {  
        // TODO Auto-generated method stub  
        super.showNext();  
        //�����������·�ҳ  
        flipperFacousChangedListener.onFliperChanged(getDisplayedChild());
    }  
  
    @Override  
    public void showPrevious() {  
        // TODO Auto-generated method stub  
        super.showPrevious();  
       flipperFacousChangedListener.onFliperChanged(getDisplayedChild()); 
    }  
  
    @Override  
    public void onLongPress(MotionEvent e) {  
        // TODO Auto-generated method stub  
  
    }  
  
    @Override  
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,  
            float distanceY) {  
        // TODO Auto-generated method stub  
        return true;  
    }  
  
    @Override  
    public void onShowPress(MotionEvent e) {  
        // TODO Auto-generated method stub  
  
    }  
  
    @Override  
    public boolean onSingleTapUp(MotionEvent e) {  
        // TODO Auto-generated method stub  
        return false;  
    }  
    /* 
     * �ص��ӿڣ����ڼ���viewflipper�л��¼� 
     */  
      
    public interface FlipperFacousChangedListener{  
        public void onFliperChanged(int index);  
    }  
      
    public void setOnFacousChangedListener(FlipperFacousChangedListener flipperFacousChangedListener){  
        this.flipperFacousChangedListener=flipperFacousChangedListener;  
    }  
  
}  