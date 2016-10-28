package com.example.player;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Jax on 2016/10/24.
 */
public class VideoPlayerTouchAdapter implements View.OnTouchListener {

	private static final String TAG = "VideoPlayerTouchAdapter";
	// private boolean isTouching;
	private boolean isClick;
	private float xBegin;
	private float yBegin;
	// 有效移动距离
	final float VALID_MOVE_DISTANCE = 50;
	private int moveDirection = 0;
	final int MOVE_DIRECTION_HORIZONTAL = 1;
	final int MOVE_DIRECTION_VERTICAL_LEFT = 2;
	final int MOVE_DIRECTION_VERTICAL_RIGHT = 3;

	// 事件回调
	private VideoPlayerTouchListener listener;

	public VideoPlayerTouchAdapter(VideoPlayerTouchListener listener) {
		assert listener != null;
		this.listener = listener;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Log.i(TAG, "ACTION_DOWN");
			isClick = true;
			moveDirection = 0;
			xBegin = event.getX();
			yBegin = event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			float horizontalMoveDistance = event.getX() - xBegin;
			float verticalMoveDistance = event.getY() - yBegin;
			Log.i(TAG, "ACTION_MOVE " + horizontalMoveDistance + "," + verticalMoveDistance);
			if (moveDirection == 0 && (Math.pow(horizontalMoveDistance, 2) + Math.pow(verticalMoveDistance, 2) < Math
					.pow(VALID_MOVE_DISTANCE, 2))) {
				// 移动距离小于有效距离，判断为点击事件
				break;
			}
			// 判断不是点击事件
			isClick = false;
			// 判断位移方向，优先执行左右位移
			int nextMoveDirection;
			if (Math.abs(horizontalMoveDistance) > VALID_MOVE_DISTANCE) {
				// 左右
				nextMoveDirection = MOVE_DIRECTION_HORIZONTAL;
			} else {
				if (isLeftSide(xBegin, v.getWidth())) {
					// 左侧上下滑动，调节明暗
					nextMoveDirection = MOVE_DIRECTION_VERTICAL_LEFT;
				} else {
					// 右侧上下滑动，调节声音
					nextMoveDirection = MOVE_DIRECTION_VERTICAL_RIGHT;
				}
			}
			move(nextMoveDirection, horizontalMoveDistance, verticalMoveDistance);
			// 如果发生位移，则更新开始计算的位置
			xBegin = event.getX();
			yBegin = event.getY();
			break;
		case MotionEvent.ACTION_UP:
			Log.i(TAG, "ACTION_UP");
			if (isClick) {
				listener.onClick();
				v.performClick();
			} else if (moveDirection == MOVE_DIRECTION_HORIZONTAL) {
				listener.onHorizontalMoveEnd();
			}
			break;
		}
		return true;
	}

	private boolean isLeftSide(float x, float playerWidth) {
		return x < playerWidth / 2;
	}

	private void move(int nextMoveDirection, float horizontalMoveDistance, float verticalMoveDistance) {
		// 如果未确定方向，则按照nextMoveDirection确定方向，如果已经确定方向，则按照之前确定的方向
		if (this.moveDirection == 0) {
			this.moveDirection = nextMoveDirection;
		}
		switch (this.moveDirection) {
		case MOVE_DIRECTION_HORIZONTAL:
			listener.onHorizontalMove(horizontalMoveDistance);
			break;
		case MOVE_DIRECTION_VERTICAL_LEFT:
			listener.onLeftVerticalMove(verticalMoveDistance);
			break;
		case MOVE_DIRECTION_VERTICAL_RIGHT:
			listener.onRightVerticalMove(verticalMoveDistance);
			break;
		}
	}

	public interface VideoPlayerTouchListener {
		// 大于0则向右滑动
		void onHorizontalMove(float horizontalMoveDistance);

		// 大于0则向下滑动
		void onLeftVerticalMove(float verticalMoveDistance);

		void onRightVerticalMove(float verticalMoveDistance);

		// 判断点击
		void onClick();

		// 判断横向位移已经结束
		void onHorizontalMoveEnd();
	}
}
