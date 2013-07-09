package eu.ttbox.velib.service.countdown;

import android.os.CountDownTimer;
import android.widget.TextView;

/**
 * @see http://dewful.com/?p=3
 * @author deostem
 * 
 */
public class MyCountDownTimer extends CountDownTimer {

	TextView tv = null; // textview to display the countdown

	public MyCountDownTimer(long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
	}

	@Override
	public void onFinish() {
		tv.setText("done!");

	}

	@Override
	public void onTick(long millisUntilFinished) {
		tv.setText("Left: " + millisUntilFinished / 1000);
	}

}
