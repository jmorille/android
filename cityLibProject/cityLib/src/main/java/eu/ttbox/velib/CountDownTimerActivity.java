package eu.ttbox.velib;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import eu.ttbox.velib.countdown.CountdownLayout;
import eu.ttbox.velib.countdown.CountdownTimerService;
import eu.ttbox.velib.countdown.NumberPicker;

/**
 * @see http://blog.sptechnolab.com/2011/02/10/android/android-countdown-timer/
 * 
 */
public class CountDownTimerActivity extends Activity {

	private final String TAG = getClass().getSimpleName();

	private LinearLayout mLlContentLayout;
	private LinearLayout mLlTimePicker;
	// private ViewGroup mViewGroup;
	 

	private static NumberPicker npHours;
	private static NumberPicker npMinutes;
	private static NumberPicker npSeconds;
	private CountdownTimerService mCDService;

	private ArrayList<CountdownLayout> mTimerViews;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.countdown_timer);

		 
//		setHasOptionsMenu(true);
		/*
		 * Binds the activity to our service in order to make method calls directly on the service, as well as properties.
		 */
		Intent bindIntent = new Intent(this, CountdownTimerService.class);
		bindService(bindIntent, mConnection, Context.BIND_IMPORTANT);

		/* Sets up the layout */
 		mLlContentLayout = (LinearLayout) findViewById(R.id.llContentLayout);
		mLlTimePicker = (LinearLayout) mLlContentLayout.findViewById(R.id.llTimePicker);
		setupTimePickers();
		mTimerViews = new ArrayList<CountdownLayout>();

		/* Sets up the add- and remove timer buttons */
		Button bAddTimer = (Button) mLlContentLayout.findViewById(R.id.bAddTimer);
		Button bRemoveTimer = (Button) mLlContentLayout.findViewById(R.id.bRemoveTimer);
		bAddTimer.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				addTimer();
			}

		});
		bRemoveTimer.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				removeTimer();
			}

		});

		
	}
	

	@Override
	public void onResume() {
		super.onResume();
		registerBroadcastReceiver();

		Intent bindIntent = new Intent(this, CountdownTimerService.class);
		this.bindService(bindIntent, mConnection, Context.BIND_IMPORTANT);

		Intent startService = new Intent(this, CountdownTimerService.class);
		this.startService(startService);
	}

	@Override
	public void onPause() {
		super.onPause();
		this.unregisterReceiver(broadcastReceiver);
		this.unbindService(mConnection);
		serviceShutdownMagic();

	}
 

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuMap: {
			// Go To
			Intent intentMap = new Intent(this, VelibMapActivity.class);
			// intentMap.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intentMap);
			// overridePendingTransition(R.animator.push_right_in, R.animator.push_right_out);
			return true;
		}
		case R.id.stopAll: {
			for (int i = 0; i < mCDService.announceServiceState(); i++) {
				mCDService.stopTimer(i);
			}
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	 

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("cd", "workaround");
		super.onSaveInstanceState(outState);
	}

	protected void registerBroadcastReceiver() {
		IntentFilter ifilter = new IntentFilter();
		ifilter.addAction(CountdownTimerService.ACTION_TIMER_ADDED);
		ifilter.addAction(CountdownTimerService.ACTION_TIMER_REMOVED);
		ifilter.addAction(CountdownTimerService.ACTION_TIMER_STOPPED);
		ifilter.addAction(CountdownTimerService.ACTION_TIMER_ALARM_STOPPED);
		ifilter.addAction("TIMER_STARTED");
		ifilter.addAction(CountdownTimerService.ACTION_TIMER_TICK);
		ifilter.addAction(CountdownTimerService.ACTION_ALARM_SOUNDING);
		this.registerReceiver(broadcastReceiver, new IntentFilter(ifilter));
	}

	protected void addTimer() {
		mCDService.addTimer();
	}

	protected void addTimerView() {
		mTimerViews.add(new CountdownLayout(this, mTimerViews.size(), mCDService));
		mLlContentLayout.addView(mTimerViews.get(mTimerViews.size() - 1).getLayout());
	}

	protected void removeTimer() {
		if (mCDService.announceServiceState() > 0) {
			mCDService.removeTimer();
		}
	}

	protected void removeTimerView() {
		if (mTimerViews.size() > 0) {
			mLlContentLayout.removeView(mTimerViews.get(mTimerViews.size() - 1).getLayout());
			mTimerViews.remove(mTimerViews.size() - 1);
		}
	}

	/* Sets up the TimePicker widgets */
	protected void setupTimePickers() {
		npHours = new NumberPicker(this);
		npMinutes = new NumberPicker(this);
		npSeconds = new NumberPicker(this);

		npHours.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
		npMinutes.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
		npSeconds.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);

		npHours.setCurrent(0);
		npMinutes.setCurrent(1);
		npSeconds.setCurrent(0);

		npHours.setRange(0, 24);
		npMinutes.setRange(0, 59);
		npSeconds.setRange(0, 59);

		mLlTimePicker.addView(npHours);
		mLlTimePicker.addView(npMinutes);
		mLlTimePicker.addView(npSeconds);
	}

	public void serviceShutdownMagic() {
		if (mCDService != null && mConnection != null) {
			if (mCDService.allAreFinished()) {
				mCDService.stopSelf();
			}
		}
	}

	protected ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mCDService = ((CountdownTimerService.ServiceBinder) service).getService();
			mCDService.announceServiceState();

			if (mCDService.announceServiceState() != mTimerViews.size()) {
				for (int i = 0; i < mTimerViews.size(); i++) {
					mTimerViews.get(i).remove();
				}
				for (int i = 0; i < mCDService.announceServiceState(); i++) {
					addTimerView();
				}
			}

			/* What we need has now been loaded, and we can start using the app */
			// mLlContentLayout.removeView(mViewGroup.findViewById(R.id.tvLoading));
		}

		public void onServiceDisconnected(ComponentName className) {
			mCDService = null;
		}
	};

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null) {
				return;
			}
			if (intent.getAction().equals(CountdownTimerService.ACTION_TIMER_TICK)) {
				mTimerViews.get(intent.getIntExtra(CountdownTimerService.EXTRA_TIMER_ID, -1)).updateTick(
						intent.getLongExtra(CountdownTimerService.EXTRA_TIME_LEFT, 0l));
			} else if (intent.getAction().equals(CountdownTimerService.ACTION_TIMER_REMOVED)) {
				removeTimerView();
			} else if (intent.getAction().equals(CountdownTimerService.ACTION_TIMER_STOPPED)) {
				mTimerViews.get(intent.getIntExtra(CountdownTimerService.EXTRA_TIMER_ID, -1)).resetUI();
			} else if (intent.getAction().equals(CountdownTimerService.ACTION_ALARM_SOUNDING)) {
				mTimerViews.get(intent.getIntExtra(CountdownTimerService.EXTRA_TIMER_ID, -1)).setSounding();
			} else if (intent.getAction().equals(CountdownTimerService.ACTION_TIMER_ALARM_STOPPED)) {
				mTimerViews.get(intent.getIntExtra(CountdownTimerService.EXTRA_TIMER_ID, -1)).resetUI();
			} else if (intent.getAction().equals(CountdownTimerService.ACTION_TIMER_ADDED)) {
				addTimerView();
			}

		}

	};

	public static int getHours() {
		return npHours.getCurrent();
	}

	public static int getMinutes() {
		return npMinutes.getCurrent();
	}

	public static int getSeconds() {
		return npSeconds.getCurrent();
	}

}
