package eu.ttbox.velib.countdown;

import java.io.IOException;
import java.util.ArrayList;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import eu.ttbox.velib.CountDownTimerActivity;
import eu.ttbox.velib.R;

public class CountdownTimerService extends Service {

	private final String TAG = getClass().getSimpleName();

	public static final String ACTION_TIMER_ALARM_STOPPED = "TIMER_ALARM_STOPPED";
	public static final String ACTION_ALARM_SOUNDING = "ALARM_SOUNDING";

	public static final String ACTION_TIMER_TICK = "TIMER_TICK";
	public static final String ACTION_TIMER_ADDED = "TIMER_ADDED";
	public static final String ACTION_TIMER_REMOVED = "TIMER_REMOVED";
	public static final String ACTION_TIMER_STOPPED = "TIMER_STOPPED";

	public static final String EXTRA_TIME_LEFT = "TIME_LEFT";
	public static final String EXTRA_TIMER_ID = "TIMER_ID";

	// Start immediately
	// Vibrate for 200 milliseconds
	// Sleep for 500 milliseconds
	private static final long[] mVibratorPattern = new long[] { 0l, 200l, 500l };

	// Service
	private Vibrator mVibrator;
	private final IBinder binder = new ServiceBinder();

	// Instance
	private ArrayList<Timer> mTimers;

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public class ServiceBinder extends Binder {
		public CountdownTimerService getService() {
			return CountdownTimerService.this;
		}
	}

	/* End of bind stuff */

	@Override
	public void onCreate() {

		/* Sets up resources */
		mTimers = new ArrayList<Timer>();
		mTimers.add(new Timer(mTimers.size()));
		mTimers.add(new Timer(mTimers.size()));
		mTimers.add(new Timer(mTimers.size()));

		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		return Service.START_STICKY;
	}

	public int announceServiceState() {
		for (int i = 0; i < mTimers.size(); i++) {
			if (mTimers.get(i).isSounding) {
				Intent intentA = new Intent();
				intentA.setAction(ACTION_ALARM_SOUNDING);
				intentA.putExtra(EXTRA_TIMER_ID, i);
				sendBroadcast(intentA);
			}
		}
		return mTimers.size();
	}

	public void addTimer() {
		mTimers.add(new Timer(mTimers.size()));
		Intent added = new Intent();
		added.setAction(ACTION_TIMER_ADDED);
		sendBroadcast(added);
	}

	public void removeTimer() {
		mTimers.get(mTimers.size() - 1).stop();
		mTimers.remove(mTimers.size() - 1);
		Intent removed = new Intent();
		removed.setAction(ACTION_TIMER_REMOVED);
		removed.putExtra(EXTRA_TIMER_ID, mTimers.size());
		sendBroadcast(removed);
	}

	public void stopTimer(int timerId) {
		if (Log.isLoggable(TAG, Log.INFO)) {
			Log.i(TAG, "stopTimer Id : " + timerId);
		}
		mTimers.get(timerId).stop();
		Intent stopped = new Intent();
		stopped.setAction(ACTION_TIMER_STOPPED);
		stopped.putExtra(EXTRA_TIMER_ID, timerId);
		sendBroadcast(stopped);

	}

	public void stopAlarm(int timerId) {
		if (Log.isLoggable(TAG, Log.INFO)) {
			Log.i(TAG, "stopAlarm Id : " + timerId);
		}
		mTimers.get(timerId).stopAlarm();
		mVibrator.cancel();
		Intent stopped = new Intent();
		stopped.setAction(ACTION_TIMER_ALARM_STOPPED);
		stopped.putExtra(EXTRA_TIMER_ID, timerId);
		sendBroadcast(stopped);
	}

	public void startTimer(int timerId, long millisInFuture) {
		if (Log.isLoggable(TAG, Log.INFO)) {
			Log.i(TAG, "startTimer Id : " + timerId);
		}
		mTimers.get(timerId).startTimer(millisInFuture);

		// Notification Bar
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)//
				.setSmallIcon(R.drawable.icon_notification)//
				.setTicker(getString(R.string.countdown_notif_msg_title))//
				.setContentTitle(getString(R.string.countdown_notif_msg_title))//
				.setContentText(getString(R.string.countdown_notif_msg_body))//
				.setContentIntent(getDialogPendingIntent(getString(R.string.countdown_notif_msg_title)));
		// mNotification.flags = mNotification.flags | Notification.FLAG_ONGOING_EVENT;

		// start
		// startForeground(1, builder.getNotification());
	}

	public boolean allAreFinished() {
		for (int i = 0; i < mTimers.size(); i++) {
			if (mTimers.get(i).isCounting || mTimers.get(i).isSounding) {
				return false;
			}
		}
		return true;
	}

	private PendingIntent getDialogPendingIntent(String dialogText) {
		Intent notifIntent = new Intent(this, CountDownTimerActivity.class) //
				.putExtra(Intent.EXTRA_TEXT, dialogText)//
				.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		// Create Pending
		return PendingIntent.getActivity(this, dialogText.hashCode(), // Otherwise previous PendingIntents with the same
																		// requestCode may be overwritten.
				notifIntent, 0);
	}

	private void showAlarmFinishedNotification() {
		/* Creates a notification manager to show our notification */
		NotificationManager nm = (NotificationManager) getSystemService(android.app.Activity.NOTIFICATION_SERVICE);

		/* Creates the notification itself */
		// Notification notification = new Notification(R.drawable.icon_notification, getString(R.string.countdown_notif_timeup_body),
		// System.currentTimeMillis());
		// Intent resumeActivity = new Intent(this, CountdownFragment.class);
		/*
		 * This line lets the user touch the notification to be taken to the KlerudKitchenTimer Activity. The flag FLAG_ACTIVITY_SINGLE_TOP makes sure that we
		 * don't start up a new instance of the activity, but rather go back to the one we started in the first place.
		 */
		// resumeActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		// notification.setLatestEventInfo(this, getString(R.string.countdown_notif_timeup_title), getString(R.string.countdown_notif_timeup_body),
		// PendingIntent.getActivity(this, 0, resumeActivity, 0));
		/* Sets some of the notification properties */
		// notification.defaults |= Notification.DEFAULT_LIGHTS;
		// notification.defaults |= Notification.FLAG_INSISTENT;
		// notification.defaults |= Notification.FLAG_AUTO_CANCEL;

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)//
				.setSmallIcon(R.drawable.icon_notification)//
				.setTicker(getString(R.string.countdown_notif_timeup_title))//
				.setContentTitle(getString(R.string.countdown_notif_timeup_title))//
				.setContentText(getString(R.string.countdown_notif_timeup_body))//
				.setContentIntent(getDialogPendingIntent(getString(R.string.countdown_notif_msg_title)));

		/* Shows the notification */
		nm.notify(1, builder.getNotification());
	}

	private class Timer {
		private CountdownTimer mTimer;
		private Object mPendingAlarmIntent;
		private AlarmManager mAlarmManager;
		private final int mTimerId;

		boolean isSounding;
		boolean isCounting;

		MediaPlayer mMediaPlayer;
		AudioManager mAudioManager;

		PowerManager mPowerManager;
		PowerManager.WakeLock mWakeLock;

		public Timer(int timerId) {
			mTimerId = timerId;
		}

		public synchronized void startTimer(long millisInFuture) {
			if (isCounting) {
				return;
			}
			isCounting = true;

			/*
			 * Uses the systems Alarm Manager to set an alarm at millisInFuture
			 * 
			 * Note that an Alarm in Android doesn't necessarily play a sound. They are used by apps to do stuff at certain times, like for instance start a
			 * download of RSS elements, or play an alarm sound like we do in CountdownTimer.onFinish().
			 */
			Intent mAlarmIntent = new Intent(getApplicationContext(), CountDownTimerActivity.class);
			mPendingAlarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 1234, mAlarmIntent, 0);
			mAlarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
			mAlarmManager.set(AlarmManager.RTC_WAKEUP, millisInFuture, (PendingIntent) mPendingAlarmIntent);

			/* Starts our countdown timer */
			mTimer = new CountdownTimer(millisInFuture, 1000);
			mTimer.start();
		}

		public void stop() {
			isCounting = false;

			/* Stops the timer */
			if (mTimer != null) {
				mTimer.cancel();
				mTimer = null;
			}

			/* Stops the alarm as we no longer need it */
			if (mAlarmManager != null && mPendingAlarmIntent != null) {
				mAlarmManager.cancel((PendingIntent) mPendingAlarmIntent);
			}

			/* Releases the wake lock (if held) as we no longer need it */
			if (mWakeLock != null && mWakeLock.isHeld()) {
				mWakeLock.release();
			}
		}

		public void stopAlarm() {
			isSounding = false;

			/* Stops the playing alarm sound */
			if (mMediaPlayer != null) {
				mMediaPlayer.stop();
			}

			/* Releases the wake lock (if held) as we no longer need it */
			if (mWakeLock != null && mWakeLock.isHeld()) {
				mWakeLock.release();
			}

		}

		/* Our implementation of the CountDownTimer */
		private class CountdownTimer extends CountDownTimer

		{
			private Intent tick;

			public CountdownTimer(long millisInFuture, long countDownInterval) {
				super(millisInFuture, countDownInterval);

			}

			@Override
			public void onFinish() {

				isCounting = false;
				isSounding = true;

				/*
				 * The PowerManager and its flags make sure the phone screen lights up if it has been locked down. This is called AQUIRE_CAUSES_WAKEUP, which
				 * basically means that when an app acquires a Wake Lock like below (wl.acquire()) the phone wakes up from its slumber. The wakeup requires that
				 * the flag FULL_WAKE_LOCK is also set. The wakelock is released when the user stops the alarm. It is important that the lock is released, or
				 * else the screen won't turn off automatically!
				 */
				mPowerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
				mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,
						"Time is up");

				if (!mWakeLock.isHeld()) {
					mWakeLock.acquire();
				}

				showAlarmFinishedNotification();

				/*
				 * Sets up the sound the app plays for an alarm, using the default alarm for the particular phone, and some backups just in case.
				 * 
				 * TODO: Consider allowing the user to select his own alarm sound, or bundle our own?
				 */
				Uri alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
				if (alertSound == null) {
					alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
					if (alertSound == null) {
						alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
					}
				}

				/* Sets up the MediaPlayer and points it to the sound. */
				mMediaPlayer = new MediaPlayer();
				try {
					mMediaPlayer.setDataSource(getApplicationContext(), alertSound);
				} catch (IllegalArgumentException e1) {
					/*
					 * The media player was given a bad parameter.
					 * 
					 * Bad parameter, bad! >:(
					 */
					// Log.e("CountdownTimer",
					// "Illegal argument exception:" + e1.toString());
				} catch (SecurityException e1) {
					/*
					 * We were somehow not allowed access to a required resource. This exception is mainly a sign that the app lacks a permission in
					 * AndroidManifest.xml.
					 */
					// Log.e("CountdownTimer",
					// "Security exception: " + e1.toString());
				} catch (IllegalStateException e1) {
					/*
					 * The media player was in a state that didn't allow for this particular operation right now.
					 */
					// Log.e("CountdownService",
					// "Illegal state exception: " + e1.toString());
				} catch (IOException e1) {
					/*
					 * The app could not read the sound file. We have nothing to play! This isn't that critical, as we vibrate as well.
					 */
					// Log.e("CountdownTimer", "IOException: " + e1.toString());
				}

				/*
				 * Finds the Audio Manager and checks the media volume. If it isn't zero (silent) we play the alarm.
				 */
				mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
				if (mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
					mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);

					/* Don't stop until the user has responded! */
					mMediaPlayer.setLooping(true);
					try {
						mMediaPlayer.prepare();
					} catch (IllegalStateException e) {
						// Log.e("CountdownService", "Illegal state exception: "
						// + e.toString());
					} catch (IOException e) {
						// Log.e("CountdownTimer", "IOException: " +
						// e.toString());
					}
					mMediaPlayer.start();
				}

				/* Starts vibrating, repeating until cancel()-ed */
				try {
					mVibrator.vibrate(mVibratorPattern, 0);
				} catch (Exception e) {
					/* Already vibrating, no biggie */
					// Log.e("CountdownTimer",
					// "Vibrator exception: " + e.toString());
				}

				/*
				 * Broadcasts that the alarm finished, so we can update our UI and let the user turn the damned thing off!
				 */
				Intent alarm = new Intent();
				alarm.setAction(ACTION_ALARM_SOUNDING);
				alarm.putExtra(EXTRA_TIMER_ID, mTimerId);
				sendBroadcast(alarm);

			}

			@Override
			public void onTick(long millisUntillFinished) {
				/* Broadcasts the tick, so we can update our UI */
				tick = new Intent();
				tick.setAction(ACTION_TIMER_TICK);
				tick.putExtra(EXTRA_TIMER_ID, mTimerId);
				tick.putExtra(EXTRA_TIME_LEFT, millisUntillFinished);
				sendBroadcast(tick);
			}

		}
	}
}