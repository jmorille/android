package eu.ttbox.geoping.ui.map.timeline;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.ui.widget.comp.RangeSeekBar;
import eu.ttbox.geoping.ui.widget.comp.RangeSeekBar.OnRangeSeekBarChangeListener;

public class RangeTimelineView extends RelativeLayout {

	private static final String TAG = "RangeTimelineView";

	private TextView rangeBeginText;
	private TextView rangeEndText;
	private RangeSeekBar rangeSeekBar;

	private OnRangeTimelineChangeListener onRangeTimelineChangeListener;

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
		rangeBeginText = (TextView) findViewById(R.id.rangeTimeline_beginTextView);
		rangeEndText = (TextView) findViewById(R.id.rangeTimeline_endTextView);
		// Range Seek Bar
		// ---------------
		ViewGroup rangeViewContainer = (ViewGroup) findViewById(R.id.rangeTimeline_seekBarViewContainer);
		rangeSeekBar = new RangeSeekBar(0, AppConstants.ONE_DAY_IN_MS, getContext());
		rangeViewContainer.addView(rangeSeekBar, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		// rangeSeekBar = (RangeSeekBar)
		// findViewById(R.id.rangeTimeline_seekBarView);
		rangeSeekBar.setNotifyWhileDragging(true);
		rangeSeekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener() {
			@Override
			public void onRangeSeekBarValuesChanged(RangeSeekBar bar, int minValue, int maxValue) {
				String minValueString = getTimeFromMs(minValue);
				String maxValueString = getTimeFromMs(maxValue);
				rangeBeginText.setText(minValueString);
				rangeEndText.setText(maxValueString);
				if (onRangeTimelineChangeListener != null) {
					boolean isRangeDefine = minValue > rangeSeekBar.getAbsoluteMinValue() || maxValue < rangeSeekBar.getAbsoluteMaxValue();
					onRangeTimelineChangeListener.onRangeTimelineValuesChanged(minValue, maxValue, isRangeDefine);
				}
				Log.d(TAG, "User selected new date range: MIN=" + minValue + ", MAX=" + maxValue);
			}
		});
	}

	private String getTimeFromMs(int ms) {
		// final String timePattern = "%1$tH:%1$tM:%1$tS";
		// String.format(timePattern, (long) minValue
		int x = ms / 1000;
		int seconde = x % 60;
		x /= 60;
		int minutes = x % 60;
		x /= 60;
		int hours = x;// % 24;  
//		x /= 24;
//		hours = x*24 + hours;
		return String.format("%02d:%02d:%02d", hours, minutes, seconde);
	}

	/**
	 * Returns the absolute minimum value of the range that has been set at
	 * construction time.
	 * 
	 * @return The absolute minimum value of the range.
	 */
	public int getAbsoluteMinValue() {
		return rangeSeekBar.getAbsoluteMinValue();
	}

	/**
	 * Returns the absolute maximum value of the range that has been set at
	 * construction time.
	 * 
	 * @return The absolute maximum value of the range.
	 */
	public int getAbsoluteMaxValue() {
		return rangeSeekBar.getAbsoluteMaxValue();
	}

	public void setOnRangeTimelineChangeListener(OnRangeTimelineChangeListener listener) {
		this.onRangeTimelineChangeListener = listener;
	}

	public interface OnRangeTimelineChangeListener {
		public void onRangeTimelineValuesChanged(int minValue, int maxValue, boolean isRangeDefine);
	}

}
