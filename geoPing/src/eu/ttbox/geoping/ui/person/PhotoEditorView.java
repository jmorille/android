/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.ttbox.geoping.ui.person;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import eu.ttbox.geoping.R;

/**
 * Simple editor for {@link Photo}.
 */
public class PhotoEditorView extends RelativeLayout // implements Editor
{

    private static final String TAG = "PhotoEditorView";

    // Constant
    public static  final int GEOPING_TYPE_TRIANGLE = 1;
    public static  final int GEOPING_TYPE_GEOFENCE = 1;

    // Instance
    private ImageView mPhotoImageView;
    private View mFrameView;

    // private ValuesDelta mEntry;
    private EditorListener mListener;
    private ImageView mTriangleAffordance;

    private boolean mHasSetPhoto = false;
    private boolean mReadOnly;

    // ===========================================================
    // Constructor
    // ===========================================================

    public PhotoEditorView(Context context) {
        this(context, null);
    }

    public PhotoEditorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0); 
    }

    public PhotoEditorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // Read atrs
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PhotoEditorView);
        int geopingType = a.getInteger(R.styleable.PhotoEditorView_geopingType, 0);
        a.recycle();
        Log.w(TAG, "geopingType : " + geopingType); 
    }

    private void initView(Context context){
        View.inflate(context, R.layout.item_photo_editor, this);  //correct way to inflate..
    }
    
    /** {@inheritDoc} */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mTriangleAffordance = (ImageView) findViewById(R.id.photo_triangle_affordance);
        mPhotoImageView = (ImageView) findViewById(R.id.photo);
        mFrameView = findViewById(R.id.frame);
        mFrameView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    Animation animationOut = AnimationUtils.loadAnimation(getContext(), R.anim.shrink_to_middle);
                    clearAnimation();
                    startAnimation(animationOut);
                    mListener.onRequest(v, EditorListener.REQUEST_PICK_PHOTO);
                } 
            }
        });
    }

    // ===========================================================
    // Accessors
    // ===========================================================

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mFrameView.setEnabled(enabled);
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        setValues( imageBitmap, true);
    }

    public void setValues(final Bitmap photo, boolean readOnly) {
        if (photo != null) {
            mPhotoImageView.setImageBitmap(photo);
            // mPhotoImageView.post(new Runnable() {
            // @Override
            // public void run() {
            // mPhotoImageView.setImageBitmap(photo);
            // }
            // });
            mFrameView.setEnabled(isEnabled());
            mHasSetPhoto = true;
            // mEntry.setFromTemplate(false);
        } else {
            resetDefault();
        }
    }

    /**
     * Return true if a valid {@link Photo} has been set.
     */
    public boolean hasSetPhoto() {
        return mHasSetPhoto;
    }

    protected void resetDefault() {
        // Invalid photo, show default "add photo" place-holder
        mPhotoImageView.setImageResource(R.drawable.ic_contact_picture_holo_light);
        mFrameView.setEnabled(!mReadOnly && isEnabled());
        mHasSetPhoto = false;
        // mEntry.setFromTemplate(true);
    }
    // ===========================================================
    // Photo Type
    // ===========================================================

    public void setGeopingType(int geopingType) {
        switch (geopingType ) {
            case GEOPING_TYPE_GEOFENCE:
                mTriangleAffordance.setImageResource(R.drawable.ic_action_geofence);
                break;
            default:
                mTriangleAffordance.setImageResource(R.drawable.account_spinner_icon);
                break;
        }
    }


    // ===========================================================
    // Listeners
    // ===========================================================

    /** {@inheritDoc} */
    // @Override
    public void setEditorListener(EditorListener listener) {
        mListener = listener;

        final boolean isPushable = listener != null;
        mTriangleAffordance.setVisibility(isPushable ? View.VISIBLE : View.INVISIBLE);
        mFrameView.setVisibility(isPushable ? View.VISIBLE : View.INVISIBLE);
    }



    public interface EditorListener {
        public void onRequest(View v, int request);

        public static final int REQUEST_PICK_PHOTO = 1;
    }
}
