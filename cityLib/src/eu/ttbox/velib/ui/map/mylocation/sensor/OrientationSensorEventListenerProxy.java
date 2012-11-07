package eu.ttbox.velib.ui.map.mylocation.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class OrientationSensorEventListenerProxy implements SensorEventListener {

	private final SensorManager mSensorManager;
	private SensorEventListener mListener = null;

	// Instance Data
	private int azimuth = 0;
	
	public OrientationSensorEventListenerProxy(final SensorManager pSensorManager) {
		mSensorManager = pSensorManager;
	}

	public boolean startListening(final SensorEventListener pListener) {
		return startListening(pListener, SensorManager.SENSOR_DELAY_UI);
	}

	public boolean startListening(final SensorEventListener pListener, final int pRate) {
		final Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		if (sensor == null)
			return false;
		mListener = pListener;
		return mSensorManager.registerListener(this, sensor, pRate);
	}

	public void stopListening() {
		mListener = null;
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(final Sensor pSensor, final int pAccuracy) {
		if (mListener != null) {
			mListener.onAccuracyChanged(pSensor, pAccuracy);
		}
	}

	@Override
	public void onSensorChanged(final SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			if (event.values != null) {
				azimuth = roudAzimuth(event.values[0]); 
			}
		}
		if (mListener != null) {
			mListener.onSensorChanged(event);
		}
	}
	
	private int roudAzimuth(float eventVal) {
		return Math.round(eventVal);
	}

	/**
	 * @see http://developer.android.com/reference/android/hardware/SensorEvent.html#values
	 * @return
	 */
	public int getAzimuth() {
		return azimuth;
	}
}
