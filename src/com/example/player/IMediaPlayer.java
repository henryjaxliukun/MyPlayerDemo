package com.example.player;

import android.content.Context;
import android.net.Uri;
import android.view.Surface;

public interface IMediaPlayer {

	// player control

	public boolean initPlayer(Surface s, Uri uri, Context context);

	public void releasePlayer();

	public void resetStatus();

	// video

	public void startPlayer();

	public void pausePlayer();

	public void stopPlayer();

	// playPosition

	public int getPlayerPositionMs();

	public void seekToPlayerMs(int positionMs);

	public int getPlayerDurationMs();

	// status

	public boolean isPlayerPlaying();

	public boolean isPlayerReady();

	// MediaPlayer callback

	public void setProgressChangedListener(OnProgressChangeListener progressChangedListener);

	public void setErrorListener(OnErrorListener listener);

	public void setCompletedListener(OnCompletedListener listener);

	public void setPlayerPreparedListener(OnPlayerPreparedListener listener);

	// 播放完成的回调

	interface OnProgressChangeListener {

		void onProgressChanged(int progressMs);

		void onBuffering(boolean isBuffering);
	}

	interface OnPlayerPreparedListener {
		void onPrepared();
	}

	interface OnErrorListener {
		void onError(int what, int extra);
	}

	interface OnCompletedListener {
		void onCompleted();
	}
}
