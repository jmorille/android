package eu.ttbox.velib.countdown;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import eu.ttbox.velib.CountDownTimerActivity;
import eu.ttbox.velib.R;

public class CountdownLayout {
	private final LinearLayout mLlTimerLayout;

	private int mTextSize;

	private TextView mTvHours;
	private TextView mTvMinutes;
	private TextView mTvSeconds;
	private TextView mTvHourMinuteSeparator;
	private TextView mTvMinuteSecondSeparator;

	private static Context mContext;
	private int mTimerViewId;

	boolean isCounting;
	boolean isSounding;
	private CountdownTimerService mService;

	public CountdownLayout(Context context, int timerViewId, CountdownTimerService serviceBinder) {
		mTextSize = 50;
		mContext = context;
		mService = serviceBinder;
		mTimerViewId = timerViewId;

		mLlTimerLayout = new LinearLayout(mContext);
		mLlTimerLayout.setClickable(true);
		mLlTimerLayout.setGravity(android.view.Gravity.CENTER);

		setupLayouts();

		/*
		 * The shortClickListener. Here we start the timer and set the alarm, as well as stop a sounding alarm.
		 */
		mLlTimerLayout.setOnClickListener(new OnClickListener() {

			public synchronized void onClick(View v) {
				/*
				 * We shut down the service to conserve resources when all countdowns are finished (even if there was just one). In order to be able to use the
				 * app right away, we need to start the service on click.
				 */
				Intent startService = new Intent(mContext, CountdownTimerService.class);
				mContext.startService(startService);

				if (isCounting) {
					Toast.makeText(mContext, R.string.countdown_how_to_stop, Toast.LENGTH_SHORT).show();
				} else if (isSounding) {
					/*
					 * Here we release the wake lock we acquired further down in the code, in KitchenCountDownTimer.onFinish(). We also stop the annoying alarm.
					 * Then we reset the UI.
					 */

					// Intent stopAlarm = new Intent(mContext,
					// edu.killerud.kitchentimer.CountdownService.class);
					// stopAlarm.setAction("STOP_ALARM");
					// stopAlarm.putExtra("TIMER_ID", mTimerViewId);
					// mContext.startService(stopAlarm);

					mService.stopAlarm(mTimerViewId);

					mTvHours.setTextColor(mContext.getResources().getColor(R.color.white));
					mTvMinutes.setTextColor(mContext.getResources().getColor(R.color.white));
					mTvSeconds.setTextColor(mContext.getResources().getColor(R.color.white));
					mTvHourMinuteSeparator.setTextColor(mContext.getResources().getColor(R.color.white));
					mTvMinuteSecondSeparator.setTextColor(mContext.getResources().getColor(R.color.white));
					isSounding = false;
				} else if (!isCounting && !isSounding) {

					/* Set a new alarm and start counting down! */
					Integer hours = CountDownTimerActivity.getHours();
					Integer minutes = CountDownTimerActivity.getMinutes();
					Integer seconds = CountDownTimerActivity.getSeconds();

					Long millisInFuture = (long) ((seconds * 1000) + (minutes * 60 * 1000) + (hours * 60 * 60 * 1000));

					if (millisInFuture < 1000) {
						Toast.makeText(mContext, R.string.countdown_enter_higher, Toast.LENGTH_SHORT).show();
						return;
					}

					mTvHours.setTextColor(mContext.getResources().getColor(R.color.white));
					mTvMinutes.setTextColor(mContext.getResources().getColor(R.color.white));
					mTvSeconds.setTextColor(mContext.getResources().getColor(R.color.white));
					mTvHourMinuteSeparator.setTextColor(mContext.getResources().getColor(R.color.white));
					mTvMinuteSecondSeparator.setTextColor(mContext.getResources().getColor(R.color.white));

					mService.startTimer(mTimerViewId, millisInFuture);
					isCounting = true;
				}

			}

		});

		/*
		 * The longClickListener. Here we stop a countdown and alarm while it is in progress (hasn't sounded yet).
		 */
		mLlTimerLayout.setOnLongClickListener(new android.view.View.OnLongClickListener() {
			public synchronized boolean onLongClick(View v) {
				if (isCounting) {
					/* Reset the UI */
					resetUI();
					mService.stopTimer(mTimerViewId);
					isCounting = false;
				}
				return true;
			}
		});
	}

	/* Sets up the Timer UI */
	protected void setupLayouts() {

		mTvHours = new TextView(mLlTimerLayout.getContext());
		mTvMinutes = new TextView(mLlTimerLayout.getContext());
		mTvSeconds = new TextView(mLlTimerLayout.getContext());
		mTvHourMinuteSeparator = new TextView(mLlTimerLayout.getContext());
		mTvMinuteSecondSeparator = new TextView(mLlTimerLayout.getContext());

		mTvHours.setTextSize(mTextSize);
		mTvMinutes.setTextSize(mTextSize);
		mTvSeconds.setTextSize(mTextSize);
		mTvHourMinuteSeparator.setTextSize(mTextSize);
		mTvMinuteSecondSeparator.setTextSize(mTextSize);

		resetUI();

		mLlTimerLayout.addView(mTvHours);
		mLlTimerLayout.addView(mTvHourMinuteSeparator);
		mLlTimerLayout.addView(mTvMinutes);
		mLlTimerLayout.addView(mTvMinuteSecondSeparator);
		mLlTimerLayout.addView(mTvSeconds);

		mLlTimerLayout.setBackgroundResource(R.drawable.countdown_background);
	}

	public void resetUI() {
		mTvHours.setTextColor(mContext.getResources().getColor(R.color.white));
		mTvMinutes.setTextColor(mContext.getResources().getColor(R.color.white));
		mTvSeconds.setTextColor(mContext.getResources().getColor(R.color.white));
		mTvHourMinuteSeparator.setTextColor(mContext.getResources().getColor(R.color.white));
		mTvMinuteSecondSeparator.setTextColor(mContext.getResources().getColor(R.color.white));

		mTvHourMinuteSeparator.setText(":");
		mTvMinuteSecondSeparator.setText(":");
		mTvHours.setText("00");
		mTvMinutes.setText("00");
		mTvSeconds.setText("00");
	}

	public void updateTick(long millisUntillFinished) {
		int hours = (int) (millisUntillFinished / 3600000);
		int minutes = (int) (millisUntillFinished / 60000) - (hours * 60);
		int seconds = (int) (millisUntillFinished / 1000) - (hours * 60 * 60) - (minutes * 60);
		mTvHours.setText((hours < 10) ? "0" + hours : "" + hours);
		mTvMinutes.setText((minutes < 10) ? "0" + minutes : "" + minutes);
		mTvSeconds.setText((seconds < 10) ? "0" + seconds : "" + seconds);
	}

	/* Used by the UI for object reference */
	public LinearLayout getLayout() {
		return mLlTimerLayout;
	}

	/*
	 * Used by the UI to remove the timer. Stops the countdown and releases resources.
	 */
	public void remove() {
		if (mLlTimerLayout != null) {
			mLlTimerLayout.removeAllViews();
		}
	}

	@Override
	public String toString() {
		return "TimerView";
	}

	public void setSounding() {
		isSounding = true;
		isCounting = false;
		mTvHours.setTextColor(mContext.getResources().getColor(R.color.red));
		mTvMinutes.setTextColor(mContext.getResources().getColor(R.color.red));
		mTvSeconds.setTextColor(mContext.getResources().getColor(R.color.red));
		mTvHourMinuteSeparator.setTextColor(mContext.getResources().getColor(R.color.red));
		mTvMinuteSecondSeparator.setTextColor(mContext.getResources().getColor(R.color.red));

		mTvHourMinuteSeparator.setText(":");
		mTvMinuteSecondSeparator.setText(":");
		mTvHours.setText("00");
		mTvMinutes.setText("00");
		mTvSeconds.setText("00");

	}

}
