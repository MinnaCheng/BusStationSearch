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
 * �Զ���ListView,ʵ��OnScrollListener�ӿڡ� ʵ������ˢ�¶�λ���ܡ�
 * */
public class PullToRefreshListView extends ListView implements OnScrollListener {
	public static final String TAG = "PullToRefreshListView";
	private RelativeLayout mHeader;
	private int mHeaderHeight;// ���������ļ��ĸ߶ȡ�
	private int firstVisibleItem; // ��ǰ��һ���ɼ���item��λ�á�
	private boolean isRemark; // ��ǣ���ǰ����listview��˰��µġ�
	private int scrollState;// listview��ǰ����״̬��
	private int startY; // ���µ�Yֵ
	private int state; // ��ǰ��״̬��
	private final int NONE = 0;// ����״̬
	private final int PULL = 1;// ��ʾ����ˢ��״̬
	private final int RELESE = 2;// ��ʾ�ͷ�״̬
	private final int REFLASHING = 3;// ����ˢ��
	IReflashListener iReflashListener;// ˢ�����ݵĽӿ�

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
	 * ��ʼ�����棬��Ӷ��������ļ���listView��
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
	 * ����header���ֵ��ϱ߾ࡣ
	 * */
	private void topPadding(int topPadding, int bottom) {

		mHeader.setPadding(mHeader.getPaddingLeft(), topPadding,
				mHeader.getPaddingRight(), bottom);
		Log.d("aaa", "topPadding:" + topPadding);
		Log.d("aaa", "mHeader.getBottom():" + mHeader.getBottom());
		mHeader.invalidate();
	}

	/**
	 * ֪ͨ������ռ�õĿ��ߡ�
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
				Log.d(TAG, "������������");
				// ������������
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
	 * �ж��ƶ����̲�����
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
	 * ���ݵ�ǰ״̬���ı䲼����ʾ��
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
			tip.setText("��������ˢ��");
			arrow.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			arrow.clearAnimation();
			arrow.setAnimation(anim1);
			break;
		case RELESE:
			tip.setText("�ɿ�����ˢ��");
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
			tip.setText("����ˢ��...");
			Log.d(TAG, "����ˢ�¡�����");
			break;
		default:
			break;
		}

	}

	/**
	 * ��ȡ�����ݣ�ˢ�½��棬��ȡ�ϴθ���ʱ�䡣
	 * */
	public void reflashComplete() {
		Log.d(TAG, "reflashComplete");
		state = NONE;
		isRemark = false;
		reflashViewByState();
		TextView txVLastUpDateTime = (TextView) findViewById(R.id.textView_refreash_at);
		SimpleDateFormat format = new SimpleDateFormat("yyyy��MM��dd�� hh:mm:ss");
		Date date = new Date(System.currentTimeMillis());
		String time = format.format(date);
		txVLastUpDateTime.setText("�����ڣ�" + time);
	}

	public void setInterface(IReflashListener iReflashListener) {
		this.iReflashListener = iReflashListener;
	}

	/**
	 * ˢ�����ݽӿ�
	 * 
	 * @author Administrator
	 * 
	 */
	public interface IReflashListener {
		public void onReflash();
	}
}
