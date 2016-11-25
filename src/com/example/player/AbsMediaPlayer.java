package com.example.player;

public abstract class AbsMediaPlayer implements IMediaPlayer {

    protected OnProgressChangeListener onProgressChangeListener;
    protected OnErrorListener onErrorListener;
    protected OnCompletedListener onCompletedListener;
    protected OnPlayerPreparedListener onPlayerPreparedListener;
    protected int lastPositionMs;

    @Override
    public void setPlayerPreparedListener(OnPlayerPreparedListener listener) {
        this.onPlayerPreparedListener = listener;
    }

    @Override
    public void setProgressChangedListener(OnProgressChangeListener progressChangedListener) {
        this.onProgressChangeListener = progressChangedListener;
    }

    @Override
    public void setErrorListener(OnErrorListener listener) {
        this.onErrorListener = listener;
    }

    @Override
    public void setCompletedListener(OnCompletedListener listener) {
        this.onCompletedListener = listener;
    }

    protected void initProgressChangeListener() {
        new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                while (isPlayerReady()) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int positionMs = getPlayerPositionMs();
                    if (isPlayerPlaying() && onProgressChangeListener != null) {
                    	onProgressChangeListener.onProgressChanged(positionMs);
                        checkBufferring(positionMs, onProgressChangeListener);
                    }
                    lastPositionMs = positionMs;
                }
            }
        }).start();
    }

    int bufferCounter;

    protected void checkBufferring(int positionMs,OnProgressChangeListener mProgressChangedListener) {
        if (positionMs != lastPositionMs) {
            bufferCounter = 0;
        } else {
            bufferCounter++;
        }
        if (mProgressChangedListener != null) {
            mProgressChangedListener.onBuffering(bufferCounter > 3);
        }
    }

}
