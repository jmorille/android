package eu.ttbox.geoping.ui.map.timeline;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.ui.widget.comp.RangeSeekBar;
import eu.ttbox.geoping.ui.widget.comp.RangeSeekBar.OnRangeSeekBarChangeListener;

public class RangeTimelineView extends RelativeLayout {

	private static final String TAG = "RangeTimelineView";

	private TextView rangeBeginText;
	private TextView rangeEndText;
	private RangeSeekBar<Integer> rangeSeekBar;

	public RangeTimelineView(Context context) {
		super(context);
	}

	public RangeTimelineView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RangeTimelineView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		rangeBeginText = (TextView) findViewById(R.id.rangeTimeline_BeginTextView);
		rangeEndText = (TextView) findViewById(R.id.rangeTimeline_EndTextView);
		// Range Seek Bar
		// ---------------
		ViewGroup rangeViewContainer = (ViewGroup) findViewById(R.id.rangeTimeline_SeekBarViewContainer);
		RangeSeekBar<Integer> rangeSeekBar = new RangeSeekBar<Integer>(0, 86400, getContext());
		rangeViewContainer.addView(rangeSeekBar, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		rangeSeekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
			@Override
			public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
				// handle changed range values
				rangeBeginText.setText("Begin : " +minValue.toString());
				rangeEndText.setText("End : " + maxValue.toString());
				Log.i(TAG, "User selected new date range: MIN=" + minValue + ", MAX=" + maxValue);
			}
		});
	}

}
