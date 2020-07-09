package com.suyf.mediadev.utils;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

public class RenderThread extends Thread {
    private static final int INVALID = -1;
    private static final int RENDER = 1;
    private static final int CHANGE_SURFACE = 2;
    private static final int RESIZE_SURFACE = 3;
    private static final int EVENT = 4;
    private static final int FINISH = 5;

    private EglHelper mEglHelper = new EglHelper();

    private Object mLock = new Object();
    private int mExecMsgId = INVALID;
    private SurfaceTexture mSurface;
    private GLSurfaceView.Renderer mRenderer;
    private int mWidth, mHeight;

    private boolean mFinished = false;
    private GL10 mGL;
    private ArrayList<Runnable> eventQueue = new ArrayList<>();

    public RenderThread(GLSurfaceView.Renderer renderer) {
        super("RenderThread");
        mRenderer = renderer;
        start();
    }

    private void checkRenderer() {
        if (mRenderer == null) {
            throw new IllegalArgumentException("Renderer is null!");
        }
    }

    private void checkSurface() {
        if (mSurface == null) {
            throw new IllegalArgumentException("surface is null!");
        }
    }

    public void setSurface(SurfaceTexture surface) {
        // If the surface is null we're being torn down, don't need a
        // renderer then
        if (surface != null) {
            checkRenderer();
        }
        mSurface = surface;
        exec(CHANGE_SURFACE);
    }

    public void setSize(int width, int height) {
        checkRenderer();
        checkSurface();
        mWidth = width;
        mHeight = height;
        exec(RESIZE_SURFACE);
    }

    public void render() {
        checkRenderer();
        if (mSurface != null) {
            exec(RENDER);
//            mSurface.updateTexImage();
        }
    }

    public void updateTexImage() {
        checkRenderer();
        if (mSurface != null) {
            //bind its texture to the GL_TEXTURE_EXTERNAL_OES texture
            mSurface.updateTexImage();
        }
    }

    public void finish() {
        mSurface = null;
        exec(FINISH);
        try {
            join();
        } catch (InterruptedException e) {
            // Ignore
        }
    }

    public void queueEvent(Runnable r) {
        eventQueue.add(r);
        exec(EVENT);
    }

    private void exec(int msgid) {
        synchronized (mLock) {
            if (mExecMsgId != INVALID) {
                throw new IllegalArgumentException(
                        "Message already set - multithreaded access?");
            }
            mExecMsgId = msgid;
            mLock.notify();
            try {
                mLock.wait();
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }

    private void handleMessageLocked(int what) {
        switch (what) {
            case CHANGE_SURFACE:
                if (mEglHelper.createSurface(mSurface)) {
                    mGL = mEglHelper.createGL();
                    mRenderer.onSurfaceCreated(mGL, mEglHelper.getEglConfig());
                }
                break;
            case RESIZE_SURFACE:
                mRenderer.onSurfaceChanged(mGL, mWidth, mHeight);
                break;
            case RENDER:
                mRenderer.onDrawFrame(mGL);
                mEglHelper.swap();
                break;
            case EVENT:
                if (!eventQueue.isEmpty()) {
                    eventQueue.remove(0).run();
                }
                break;
            case FINISH:
                mEglHelper.destroySurface();
                mEglHelper.finish();
                mFinished = true;
                break;
        }
    }

    @Override
    public void run() {
        synchronized (mLock) {
            mEglHelper.start();
            while (!mFinished) {
                while (mExecMsgId == INVALID) {
                    try {
                        mLock.wait();
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
                handleMessageLocked(mExecMsgId);
                mExecMsgId = INVALID;
                mLock.notify();
            }
            mExecMsgId = FINISH;
        }
    }

}
