package com.suyf.mediadev.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.TextureView;

import com.suyf.mediadev.utils.RenderThread;

public class BlockGLTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    private RenderThread mRenderThread;

    public BlockGLTextureView(Context context) {
        super(context);
        setSurfaceTextureListener(this);
    }

    public BlockGLTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSurfaceTextureListener(this);
    }

    public void setRenderer(GLSurfaceView.Renderer renderer) {
        if (mRenderThread != null) {
            throw new IllegalArgumentException("Renderer already set");
        }
        mRenderThread = new RenderThread(renderer);
    }

    public void render() {
        mRenderThread.render();
    }

    public void destroy() {
        if (mRenderThread != null) {
            mRenderThread.finish();
            mRenderThread = null;
        }
    }

    public void updateTexImage() {
        if (mRenderThread != null) {
            mRenderThread.updateTexImage();
        }
    }

    public void queueEvent(Runnable r) {
        if (mRenderThread != null) {
            mRenderThread.queueEvent(r);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mRenderThread.setSurface(surface);
        mRenderThread.setSize(width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        mRenderThread.setSize(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mRenderThread != null) {
            mRenderThread.setSurface(null);
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            destroy();
        } catch (Throwable t) {
            // Ignore
        }
        super.finalize();
    }
}
