package eu.ttbox.osm.ui.map.mylocation.drawable;

import java.util.concurrent.Callable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import eu.ttbox.osm.BuildConfig;

/**
 * 
 * @see https
 *      ://github.com/cyrilmottier/DrawablePresentation/blob/master/RemoteDrawable
 *      /src/com/cyrilmottier/android/remotedrawable/RemoteDrawable.java
 * 
 */
public class BlinkingDrawable extends Drawable implements Drawable.Callback {

    private static final String LOG_TAG = "BlinkingDrawable";

    private static final boolean DEBUG_LOGS_FILE_ENABLED = true;
    private static final boolean DEBUG_LOGS_ENABLED = DEBUG_LOGS_FILE_ENABLED && BuildConfig.DEBUG;

    private RemoteState mRemoteState;
    private Drawable mCurrentDrawable;
    private boolean mMutated;

    private Drawable mOnDrawable;
    private Drawable mOffDrawable;

    public BlinkingDrawable(Drawable onDrawable, Drawable offDrawable) {
        mOnDrawable = onDrawable;
        mOffDrawable = offDrawable;
        mCurrentDrawable = offDrawable;
        mCurrentDrawable.setCallback(this);
        mRemoteState = new RemoteState();
    }

    @Override
    public void draw(Canvas canvas) {
        if (mCurrentDrawable != null) {
            mCurrentDrawable.draw(canvas);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        if (alpha != mRemoteState.mAlpha) {
            mRemoteState.mAlpha = alpha;
            if (mCurrentDrawable != null) {
                mCurrentDrawable.setAlpha(alpha);
            }
        }
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (cf != mRemoteState.mColorFilter) {
            mRemoteState.mColorFilter = cf;
            if (mCurrentDrawable != null) {
                mCurrentDrawable.setColorFilter(cf);
            }
        }
    }

    @Override
    public void setDither(boolean dither) {
        if (mRemoteState.mDither != dither) {
            mRemoteState.mDither = dither;
            if (mCurrentDrawable != null) {
                mCurrentDrawable.setDither(dither);
            }
        }
    }

    @Override
    public void setFilterBitmap(boolean filter) {
        if (mRemoteState.mFilter != filter) {
            mRemoteState.mFilter = filter;
            if (mCurrentDrawable != null) {
                mCurrentDrawable.setFilterBitmap(filter);
            }
        }
    }

    @Override
    public Drawable getCurrent() {
        return mCurrentDrawable;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        if (mCurrentDrawable != null) {
            mCurrentDrawable.setBounds(bounds);
        }
    }

    @Override
    public boolean isStateful() {
        if (mCurrentDrawable != null) {
            return mCurrentDrawable.isStateful();
        }
        return false;
    }

    @Override
    protected boolean onStateChange(int[] state) {
        if (mCurrentDrawable != null) {
            return mCurrentDrawable.setState(state);
        }
        return false;
    }

    @Override
    protected boolean onLevelChange(int level) {
        if (mCurrentDrawable != null) {
            return mCurrentDrawable.setLevel(level);
        }
        return false;
    }

    @Override
    public int getIntrinsicWidth() {
        return mCurrentDrawable != null ? mCurrentDrawable.getIntrinsicWidth() : -1;
    }

    @Override
    public int getIntrinsicHeight() {
        return mCurrentDrawable != null ? mCurrentDrawable.getIntrinsicHeight() : -1;
    }

    @Override
    public int getMinimumWidth() {
        return mCurrentDrawable != null ? mCurrentDrawable.getMinimumWidth() : 0;
    }

    @Override
    public int getMinimumHeight() {
        return mCurrentDrawable != null ? mCurrentDrawable.getMinimumHeight() : 0;
    }

    @Override
    public int getOpacity() {
        /*
         * Here PixelFormat.TRANSPARENT is returned because the getOpacity()
         * method has to be as conservative as possible. Indeed, we don't know
         * the opacity of the remote Drawable ...
         */
        return PixelFormat.TRANSPARENT;
    }

    @Override
    public boolean getPadding(Rect padding) {
        return mCurrentDrawable == null ? false : mCurrentDrawable.getPadding(padding);
    }

    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mRemoteState = new RemoteState(mRemoteState);
            mMutated = true;
        }
        return this;
    }

    @Override
    public final ConstantState getConstantState() {
        mRemoteState.mChangingConfigurations = super.getChangingConfigurations();
        return mRemoteState;
    }

    final static class RemoteState extends Drawable.ConstantState {

        int mAlpha;
        ColorFilter mColorFilter;
        boolean mDither;
        boolean mFilter;

        int mChangingConfigurations;

        public RemoteState() {
            mAlpha = 0xFF;
            mColorFilter = null;
            mDither = true;
            mFilter = true;
        }

        public RemoteState(RemoteState state) {
            mAlpha = state.mAlpha;
            mColorFilter = state.mColorFilter;
            mDither = state.mDither;
            mFilter = state.mFilter;
            mChangingConfigurations = state.mChangingConfigurations;
        }

        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations;
        }

        @Override
        public Drawable newDrawable() {
            // TODO cyril: Should return a Drawable that is a copy of the
            // current Drawable (same placeholder as well as URL)
            return null;
        }

    }

    // ===========================================================
    // Drawable Callback
    // ===========================================================

    @Override
    public void invalidateDrawable(Drawable who) {
        if (who == mCurrentDrawable) {
            invalidateSelf();
        }
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        scheduleSelf(what, when);
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        unscheduleSelf(what);
    }

    // ===========================================================
    // Switch Drawable
    // ===========================================================
    
    public void setChangeBlinkDrawable  (Drawable onDrawable, Drawable offDrawable) {
        mOnDrawable = onDrawable;
        mOffDrawable = offDrawable;
    }
    
    public void doBlinkDrawable() {
        scheduleDrawable(mOnDrawable, blinkCallableOn, 1000l);
    }
    
    public void doUnBlinkDrawable() {
        unscheduleSelf(blinkCallableOn);
        unscheduleSelf(blinkCallableOff);
        onSwitchDrawableLoaded(mOffDrawable);
    }

    private final Runnable blinkCallableOn = new Runnable() {

        @Override
        public void run() {
            onSwitchDrawableLoaded(mOnDrawable);
            scheduleDrawable(mOffDrawable, blinkCallableOff, 1000l);
        }

    };

    private final Runnable blinkCallableOff = new Runnable() {

        @Override
        public void run() {
            onSwitchDrawableLoaded(mOffDrawable);
            scheduleDrawable(mOnDrawable, blinkCallableOn, 1000l);

        }

    };

    public void onSwitchDrawableLoaded(boolean isPair) {
        if (isPair) {
            onSwitchDrawableLoaded(mOnDrawable);
        } else {
            onSwitchDrawableLoaded(mOffDrawable);
        }
    }

    public void onSwitchDrawableLoaded(final Drawable drawable) {
        Rect bounds = mCurrentDrawable.getBounds();
        if (DEBUG_LOGS_ENABLED) {
            Log.d(LOG_TAG, "Bounds are : " + bounds);
        }
        drawable.setBounds(mCurrentDrawable.getBounds());
        mCurrentDrawable = drawable;
        invalidateSelf();
    }
}
