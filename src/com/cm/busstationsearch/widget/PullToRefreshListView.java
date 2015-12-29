package com.cm.busstationsearch.widget;

import java.sql.Date;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.cm.busstationsearch.R;

/**
 * 自定义ListView,实现OnScrollListener接口。 实现下拉刷新定位功能。
 * */
public class PullToRefreshListView extends ListView implements OnScrollListener {
	public static final String TAG = "PullToRefreshListView";
	private RelativeLayout mHeader;
	private int mHeaderHeight;// 顶部布局文件的高度。
	private int firstVisibleItem; // 当前第一个可见的item的位置。
	private boolean isRemark; // 标记，当前是在listview最顶端按下的。
	private int scrollState;// listview当前滚动状态。
	private int startY; // 按下的Y值
	private int state; // 当前的状态。
	private final int NONE = 0;// 正常状态
	private final int PULL = 1;// 提示下拉刷新状态
	private final int RELESE = 2;// 提示释放状态
	private final int REFLASHING = 3;// 正在刷新
	IReflashListener iReflashListener;// 刷新数据的接口

	public PullToRefreshListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
		Log.d(TAG, "pullToRefreshListView");
	}

	public PullToRefreshListView(Context context) {
		super(context);
		initView(context);
		Log.d(TAG, "pullToRefreshListView1");
	}

	public PullToRefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
		Log.d(TAG, "pullToRefreshListView2");
	}

	/**
	 * 初始化界面，添加顶部布局文件到listView。
	 * */
	private void initView(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		mHeader = (RelativeLayout) inflater.inflate(
				R.layout.pull_to_refreash_header, null);
		measureView(mHeader);
		mHeaderHeight = mHeader.getMeasuredHeight();
		Log.d(TAG, "mHaderHeight:" + mHeaderHeight);
		topPadding(-mHeaderHeight, 0);
		this.addHeaderView(mHeader);
		this.setOnScrollListener(this);
	}

	/**
	 * 设置header布局的上边距。
	 * */
	private void topPadding(int topPadding, int bottom) {

		mHeader.setPadding(mHeader.getPaddingLeft(), topPadding,
				mHeader.getPaddingRight(), bottom);
		Log.d("aaa", "topPadding:" + topPadding);
		Log.d("aaa", "mHeader.getBottom():" + mHeader.getBottom());
		mHeader.invalidate();
	}

	/**
	 * 通知父布局占用的宽，高。
	 * */
	private void measureView(View view) {
		ViewGroup.LayoutParams params = view.getLayoutParams();
		if (params == null) {
			params = new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			int width = ViewGroup.getChildMeasureSpec(0, 0, params.width);
			int height;
			int tempHeight = params.height;
			if (tempHeight > 0) {
				height = MeasureSpec.makeMeasureSpec(tempHeight,
						MeasureSpec.EXACTLY);
			} else {
				height = MeasureSpec
						.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
			}

			Log.d("aaa", "width" + width);
			Log.d("aaa", "height" + height);
			view.measure(width, height);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visableItemCount, int totalItemCount) {
		this.firstVisibleItem = firstVisibleItem;

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		this.scrollState = scrollState;
		Log.d(TAG, "onScrollStateChanged");

	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Log.d(TAG, "onTouchEvent_ACTION_DOWN");
			if (firstVisibleItem == 0) {
				isRemark = true;
				startY = (int) ev.getY();
				reflashViewByState();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			onMove(ev);
			break;
		case MotionEvent.ACTION_UP:
			Log.d(TAG, "onTouchEvent_ACTION_UP");
			if (state == RELESE) {
				state = REFLASHING;
				Log.d(TAG, "加载最新数据");
				// 加载最新数据
				reflashViewByState();
				iReflashListener.onReflash();

			} else if (state == PULL) {
				state = NONE;
				isRemark = false;
				reflashViewByState();
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * 判断移动过程操作：
	 * 
	 * @param ev
	 * */
	private void onMove(MotionEvent ev) {
		Log.d(TAG, "onTouchEvent_ACTION_MOVE");
		// Log.d(TAG, "isRemark==" + isRemark);
		Log.d(TAG, "state==" + state);
		if (!isRemark) {
			return;
		}
		int tempY = (int) ev.getY();
		int space = tempY - startY;
		int topPadding = space - mHeaderHeight;
		Log.d(TAG, "tempY==" + tempY);
		Log.d(TAG, "startY==" + startY);
		Log.d(TAG, "space==" + space);
		switch (state) {
		case NONE:
			if (space > 0) {
				state = PULL;
				reflashViewByState();
			}
			break;
		case PULL:
			topPadding(topPadding, space);
			Log.d(TAG, "TOPPADDING==" + topPadding);
			if (space > mHeaderHeight + 50
					&& scrollState == SCROLL_STATE_TOUCH_SCROLL) {
				state = RELESE;
				reflashViewByState();
			}
			break;
		case RELESE:
			topPadding(topPadding, space);
			Log.d(TAG, "TOPPADDING==" + topPadding);
			if (space <= mHeaderHeight + 50) {
				state = PULL;
				reflashViewByState();
			} else if (space <= 0) {
				state = NONE;
				isRemark = false;
				reflashViewByState();
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 根据当前状态，改变布局显示。
	 * */

	private void reflashViewByState() {
		Log.d("aaa", "reflashViewByState");
		TextView tip = (TextView) mHeader
				.findViewById(R.id.textView_pull_to_refresh);
		ImageView arrow = (ImageView) mHeader
				.findViewById(R.id.pull_to_refresh_image);
		ProgressBar progressBar = (ProgressBar) mHeader
				.findViewById(R.id.pull_to_refresh_progress);
		RotateAnimation anim = new RotateAnimation(0, 180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		anim.setDuration(500);
		anim.setFillAfter(true);
		RotateAnimation anim1 = new RotateAnimation(180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		anim1.setDuration(500);
		anim1.setFillAfter(true);

		Log.d("aaa", "state:" + state);
		switch (state) {
		case NONE:
			Log.d("aaa", "mHeaderHeight:" + mHeaderHeight);
			topPadding(-mHeaderHeight, 0);
			arrow.clearAnimation();
			break;
		case PULL:
			tip.setText("下拉可以刷新");
			arrow.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			arrow.clearAnimation();
			arrow.setAnimation(anim1);
			break;
		case RELESE:
			tip.setText("松开可以刷新");
			arrow.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			arrow.clearAnimation();
			arrow.setAnimation(anim);
			break;
		case REFLASHING:
			topPadding(60, 150);
			arrow.setVisibility(View.GONE);
			progressBar.setVisibility(View.VISIBLE);
			arrow.clearAnimation();
			tip.setText("正在刷新...");
			Log.d(TAG, "正在刷新。。。");
			break;
		default:
			break;
		}

	}

	/**
	 * 获取完数据，刷新界面，获取上次更新时间。
	 * */
	public void reflashComplete() {
		Log.d(TAG, "reflashComplete");
		state = NONE;
		isRemark = false;
		reflashViewByState();
		TextView txVLastUpDateTime = (TextView) findViewById(R.id.textView_refreash_at);
		SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 hh:mm:ss");
		Date date = new Date(System.currentTimeMillis());
		String time = format.format(date);
		txVLastUpDateTime.setText("更新于：" + time);
	}

	public void setInterface(IReflashListener iReflashListener) {
		this.iReflashListener = iReflashListener;
	}

	/**
	 * 刷新数据接口
	 * 
	 * @author Administrator
	 * 
	 */
	public interface IReflashListener {
		public void onReflash();
	}
}
