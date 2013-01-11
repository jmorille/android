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
import android.graphics.Bitmap;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.service.core.ContactHelper;

/**
 * Simple editor for {@link Photo}.
 */
public class PhotoEditorView extends RelativeLayout // implements Editor
{

	private ImageView mPhotoImageView;
	private View mFrameView;

	// private ValuesDelta mEntry;
	private EditorListener mListener;
	private View mTriangleAffordance;

	private boolean mHasSetPhoto = false;
	private boolean mReadOnly;

	public PhotoEditorView(Context context) {
		super(context);
	}

	public PhotoEditorView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		mFrameView.setEnabled(enabled);
	}
 

	/** {@inheritDoc} */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mTriangleAffordance = findViewById(R.id.photo_triangle_affordance);
		mPhotoImageView = (ImageView) findViewById(R.id.photo);
		mFrameView = findViewById(R.id.frame);
		mFrameView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onRequest(EditorListener.REQUEST_PICK_PHOTO);
				}
			}
		});
	}
 

	public void setValues(String contactId, boolean readOnly) { 
		mReadOnly = readOnly;
		Bitmap photo = null;
		if (!TextUtils.isEmpty(contactId)) {
			photo = ContactHelper.loadPhotoContact(getContext(), contactId);
		}
		setValues(photo, readOnly);
	}

	public void setValues(Bitmap photo, boolean readOnly) {
		if (photo != null) {
			mPhotoImageView.setImageBitmap(photo);
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

	/** {@inheritDoc} */
	// @Override
	public void setEditorListener(EditorListener listener) {
		mListener = listener;

		final boolean isPushable = listener != null;
		mTriangleAffordance.setVisibility(isPushable ? View.VISIBLE : View.INVISIBLE);
		mFrameView.setVisibility(isPushable ? View.VISIBLE : View.INVISIBLE);
	}
 

	public interface EditorListener {
		public void onRequest(int request);

		public static final int REQUEST_PICK_PHOTO = 1;
	}
}
