package eu.ttbox.geoping.ui.map.timeline;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
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

    private OnRangeTimelineValuesChangeListener onRangeTimelineChangeListener;

    public int rangeTimelineMin = 0;
    public int rangeTimelineMax = AppConstants.ONE_DAY_IN_S;
    
    // ===========================================================
    // Constructor
    // ===========================================================

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
        rangeSeekBar = new RangeSeekBar(0, AppConstants.ONE_DAY_IN_S, getContext());
        rangeViewContainer.addView(rangeSeekBar, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        // rangeSeekBar = (RangeSeekBar)
        // findViewById(R.id.rangeTimeline_seekBarView);
        rangeSeekBar.setNotifyWhileDragging(true);
        rangeSeekBar.setOnRangeSeekBarChangeListener(onRangeSeekBarChangeListener);
        // Define range Text
        setRangeBeginText(rangeSeekBar.getSelectedMinValue());
        setRangeEndText(rangeSeekBar.getSelectedMaxValue());
        this.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				setAbsoluteValues(rangeSeekBar.getSelectedMinValue(), rangeSeekBar.getSelectedMaxValue());
				return true;
			}
		});
    }

    // ===========================================================
    // Range Listener
    // ===========================================================

    private OnRangeSeekBarChangeListener onRangeSeekBarChangeListener = new OnRangeSeekBarChangeListener() {
        @Override
        public void onRangeSeekBarValuesChanged(RangeSeekBar bar, int minValue, int maxValue) {
            setRangeBeginText(minValue);
            setRangeEndText(maxValue);  
            notifyOnRangeTimelineChangeListener(minValue,  maxValue) ;
            // Log.d(TAG, "User selected new date range: MIN=" + minValue +  ", MAX=" + maxValue);
        }

       
    };
    
    public void notifyOnRangeTimelineChangeListener(int minValue, int maxValue) {
        if (onRangeTimelineChangeListener != null) { 
            boolean isRangeDefine = minValue > rangeSeekBar.getAbsoluteMinValue() || maxValue < rangeSeekBar.getAbsoluteMaxValue();
            onRangeTimelineChangeListener.onRangeTimelineValuesChanged(minValue, maxValue, isRangeDefine);
        }

    }

    public void setOnRangeTimelineChangeListener(OnRangeTimelineValuesChangeListener listener) {
        this.onRangeTimelineChangeListener = listener;
    }

    public interface OnRangeTimelineValuesChangeListener {
        public void onRangeTimelineValuesChanged(int minValue, int maxValue, boolean isRangeDefine);
    }

    // ===========================================================
    // Text Range
    // ===========================================================

    private void setRangeBeginText(int minValue) {
        String minValueString = getTimeFromMs(minValue);
        rangeBeginText.setText(minValueString);
    }
    
    private void setRangeEndText(int maxValue) {
        String maxValueString = getTimeFromMs(maxValue); 
        rangeEndText.setText(maxValueString);
    }
    
    private String getTimeFromMs(int valueInS) { 
        int x = valueInS;// / 1000;
        int seconde = x % 60;
        x /= 60;
        int minutes = x % 60;
        x /= 60;
        int hours = x;// % 24;
        // x /= 24;
        // hours = x*24 + hours;
        return String.format("%02d:%02d:%02d", hours, minutes, seconde);
    }
    
    // ===========================================================
    // Accessors
    // ===========================================================

    public void setAbsoluteValues(int absoluteMinValue, int absoluteMaxValue) {
//       if (  absoluteMinValue > absoluteMinValue) {
//           // Unset Value
//           Log.w(TAG, "Ignore setAbsoluteValues for min > max : "+  absoluteMinValue + " > " + absoluteMaxValue);
//           return;
//       }
    	Log.d(TAG, "setAbsoluteValues " + getTimeFromMs(absoluteMinValue) + " to " +    getTimeFromMs(absoluteMaxValue) );
        Log.d(TAG, "setAbsoluteValues " +  absoluteMinValue  + " to " +     absoluteMaxValue  );
        int currentSelectMinVal = rangeSeekBar.getSelectedMinValue();
        int currentSelectMaxVal = rangeSeekBar.getSelectedMaxValue();
        boolean isSelectMin = currentSelectMinVal == rangeSeekBar.getAbsoluteMinValue();
        boolean isSelectMax = currentSelectMaxVal == rangeSeekBar.getAbsoluteMaxValue();
        this.rangeSeekBar.setAbsoluteMinValue(absoluteMinValue);
        this.rangeSeekBar.setAbsoluteMaxValue(absoluteMaxValue);
        if (isSelectMin) {
            setSelectedMinValue(absoluteMinValue);
        } else {
            setSelectedMinValue(currentSelectMinVal);
        }
        if (isSelectMax) {
           setSelectedMaxValue(absoluteMaxValue);
        } else {
            setSelectedMaxValue(currentSelectMaxVal);
        }
        this.rangeSeekBar.postInvalidate();
    }
    
    public boolean isSelectedValues() {
    	return rangeSeekBar.isSelectedValues();
    }
    
    public void resetSelectedValues() {
    	setAbsoluteValues(rangeTimelineMin, rangeTimelineMax);
    	setSelectedMinValue(getAbsoluteMinValue());
    	setSelectedMaxValue(getAbsoluteMaxValue());
    	notifyOnRangeTimelineChangeListener(getAbsoluteMinValue(), getAbsoluteMaxValue());
    }
    
    private void setSelectedMinValue(int value) {
        this.rangeSeekBar.setSelectedMinValue(value);
        setRangeBeginText(value); 
    }
    
    private void setSelectedMaxValue(int value) {
        this.rangeSeekBar.setSelectedMaxValue(value);
        setRangeEndText(value);
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

	public int getRangeTimelineMin() {
		return rangeTimelineMin;
	}
 
	public int getRangeTimelineMax() {
		return rangeTimelineMax;
	}
 

}
