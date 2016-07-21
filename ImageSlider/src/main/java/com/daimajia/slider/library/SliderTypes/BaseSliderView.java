package com.daimajia.slider.library.SliderTypes;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.daimajia.slider.library.R;

import java.io.File;

/**
 * When you want to make your own slider view, you must extends from this class.
 * BaseSliderView provides some useful methods.
 * I provide two example: {@link com.daimajia.slider.library.SliderTypes.DefaultSliderView} and
 * {@link com.daimajia.slider.library.SliderTypes.TextSliderView}
 * if you want to show progressbar, you just need to set a progressbar id as @+id/loading_bar.
 */
public abstract class BaseSliderView {

    protected Context mContext;

    private Bundle mBundle;

    /**
     * Error place holder image.
     */
    private int mErrorPlaceHolderRes;

    /**
     * Empty imageView placeholder.
     */
    private int mEmptyPlaceHolderRes;

    private String mUrl;
    private File mFile;
    private int mRes;

    protected OnSliderClickListener mOnSliderClickListener;

    private boolean mErrorDisappear;

    private ImageLoadListener mLoadListener;

    private ImageLoadResultListener mLoadResultListener;
    private String mDescription;

    private RequestManager mGlideRequestManager;

    /**
     * Scale type of the image.
     */
    private ScaleType mScaleType = ScaleType.Fit;

    public enum ScaleType {
        CenterCrop, Fit
    }

    protected BaseSliderView(Context context) {
        mContext = context;
    }

    /**
     * the placeholder image when loading image from url or file.
     *
     * @param resId Image resource id
     * @return
     */
    public BaseSliderView empty(int resId) {
        mEmptyPlaceHolderRes = resId;
        return this;
    }

    /**
     * determine whether remove the image which failed to download or load from file
     *
     * @param disappear
     * @return
     */
    public BaseSliderView errorDisappear(boolean disappear) {
        mErrorDisappear = disappear;
        return this;
    }

    /**
     * if you set errorDisappear false, this will set a error placeholder image.
     *
     * @param resId image resource id
     * @return
     */
    public BaseSliderView error(int resId) {
        mErrorPlaceHolderRes = resId;
        return this;
    }

    /**
     * the description of a slider image.
     *
     * @param description
     * @return
     */
    public BaseSliderView description(String description) {
        mDescription = description;
        return this;
    }

    /**
     * set a url as a image that preparing to load
     *
     * @param url
     * @return
     */
    public BaseSliderView image(String url) {
        if (mFile != null || mRes != 0) {
            throw new IllegalStateException("Call multi image function," +
                    "you only have permission to call it once");
        }
        mUrl = url;
        return this;
    }

    /**
     * set a file as a image that will to load
     *
     * @param file
     * @return
     */
    public BaseSliderView image(File file) {
        if (mUrl != null || mRes != 0) {
            throw new IllegalStateException("Call multi image function," +
                    "you only have permission to call it once");
        }
        mFile = file;
        return this;
    }

    public BaseSliderView image(int res) {
        if (mUrl != null || mFile != null) {
            throw new IllegalStateException("Call multi image function," +
                    "you only have permission to call it once");
        }
        mRes = res;
        return this;
    }

    /**
     * lets users add a bundle of additional information
     *
     * @param bundle
     * @return
     */
    public BaseSliderView bundle(Bundle bundle) {
        mBundle = bundle;
        return this;
    }

    public String getUrl() {
        return mUrl;
    }

    public boolean isErrorDisappear() {
        return mErrorDisappear;
    }

    public int getEmpty() {
        return mEmptyPlaceHolderRes;
    }

    public int getError() {
        return mErrorPlaceHolderRes;
    }

    public String getDescription() {
        return mDescription;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * set a slider image click listener
     *
     * @param l
     * @return
     */
    public BaseSliderView setOnSliderClickListener(OnSliderClickListener l) {
        mOnSliderClickListener = l;
        return this;
    }

    /**
     * When you want to implement your own slider view, please call this method in the end in `getView()` method
     *
     * @param v               the whole view
     * @param targetImageView where to place image
     */
    protected void bindEventAndShow(final View v, ImageView targetImageView) {
        final BaseSliderView me = this;

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSliderClickListener != null) {
                    mOnSliderClickListener.onSliderClick(me);
                }
            }
        });

        if (targetImageView == null)
            return;

        if (mLoadListener != null) {
            mLoadListener.onStart(me);
        }
        RequestManager glideRequestManager = (mGlideRequestManager != null) ? mGlideRequestManager : Glide.with(mContext);
        DrawableTypeRequest rq;

        if (mUrl != null) {
            rq = glideRequestManager.load(mUrl);
        } else if (mFile != null) {
            rq = glideRequestManager.load(mFile);
        } else if (mRes != 0) {
            rq = glideRequestManager.load(mRes);
        } else {
            return;
        }

        if (rq == null) {
            return;
        }

        if (getEmpty() != 0) {
            rq.placeholder(getEmpty());
        }

        if (getError() != 0) {
            rq.error(getError());
        }

        switch (mScaleType) {
            case Fit:
                rq.fitCenter();
                break;
            case CenterCrop:
                rq.centerCrop();
                break;
        }

        rq.into(new GlideDrawableImageViewTarget(targetImageView) {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                super.onResourceReady(resource, animation);
                View loadingBar = v.findViewById(R.id.loading_bar);
                if (loadingBar == null) {
                    return;
                }
                loadingBar.setVisibility(View.INVISIBLE);
                if (mLoadResultListener != null) {
                    mLoadResultListener.onSuccess(BaseSliderView.this);
                }
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);
                if (mLoadListener != null) {
                    mLoadListener.onEnd(false, me);
                }
                View loadingBar = v.findViewById(R.id.loading_bar);
                if (loadingBar == null) {
                    return;
                }
                loadingBar.setVisibility(View.INVISIBLE);
                if (mLoadResultListener != null) {
                    mLoadResultListener.onFailure();
                }
            }
        });
    }


    public BaseSliderView setScaleType(ScaleType type) {
        mScaleType = type;
        return this;
    }

    public ScaleType getScaleType() {
        return mScaleType;
    }

    /**
     * the extended class have to implement getView(), which is called by the adapter,
     * every extended class response to render their own view.
     *
     * @return
     */
    public abstract View getView();

    /**
     * set a listener to get a message , if load error.
     *
     * @param l
     */
    public void setOnImageLoadListener(ImageLoadListener l) {
        mLoadListener = l;
    }

    /**
     * incase you want to check it to do something else.
     * @param l
     */
    public void setOnImageLoadResultListener(ImageLoadResultListener l) {
        this.mLoadResultListener = l;
    }

    public interface OnSliderClickListener {
        void onSliderClick(BaseSliderView slider);
    }

    /**
     * when you have some extra information, please put it in this bundle.
     *
     * @return
     */
    public Bundle getBundle() {
        return mBundle;
    }

    public interface ImageLoadListener {
        void onStart(BaseSliderView target);

        void onEnd(boolean result, BaseSliderView target);
    }
    public interface ImageLoadResultListener {
        void onSuccess(BaseSliderView target);

        void onFailure();
    }

    /**
     * Get the last instance set via setGlideRequestManager, or null if no user provided instance was set
     *
     * @return The current user-provided GlideRequestManager instance, or null if none
     */

    public RequestManager getGlideRequestManager() {
        return mGlideRequestManager;
    }

    /**
     * Provide a Glide instance to use when loading pictures, this is useful if you have a
     * particular HTTP cache you would like to share.
     *
     * @param requestManager The Glide instance to use, may be null to let the system use the default
     *                instance
     */
    public void setGlideRequestManager(RequestManager requestManager) {
        mGlideRequestManager = requestManager;
    }
}
