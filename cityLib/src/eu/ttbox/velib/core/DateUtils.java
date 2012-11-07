package eu.ttbox.velib.core;

public class DateUtils {

	// public static void main( String[] args) {
	// System.out.println("15s ==> " +DateUtils.formatDuration(15*MILLIS_PER_SECOND));
	// System.out.println("15min ==> " +DateUtils.formatDuration(15*MILLIS_PER_MINUTE));
	// System.out.println("15min20s ==> " +DateUtils.formatDuration(15*MILLIS_PER_MINUTE+20*MILLIS_PER_SECOND));
	// System.out.println("15H ==> " +DateUtils.formatDuration(15*MILLIS_PER_HOUR));
	// }
	//
	/**
	 * Number of milliseconds in a standard second.
	 */
	public static final long MILLIS_PER_SECOND = 1000;
	/**
	 * Number of milliseconds in a standard minute.
	 */
	public static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
	/**
	 * Number of milliseconds in a standard hour.
	 */
	public static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
	/**
	 * Number of milliseconds in a standard day.
	 */
	public static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;

	public static String formatDuration(long durationMillis) {
		String humanDuration = null;
		// if (durationMillis >= DateUtils.MILLIS_PER_DAY) {
		// long days = (durationMillis / DateUtils.MILLIS_PER_DAY);
		// long hours = (durationMillis - (days * DateUtils.MILLIS_PER_DAY))/DateUtils.MILLIS_PER_HOUR;
		// humanDuration = String.format("%sJ %sh", days, hours);
		// } else
		if (durationMillis >= DateUtils.MILLIS_PER_HOUR) {
			long hours = (durationMillis / DateUtils.MILLIS_PER_HOUR);
			long minutes = (durationMillis - (hours * DateUtils.MILLIS_PER_HOUR)) / DateUtils.MILLIS_PER_MINUTE;
			humanDuration = String.format("%sh %smin", hours, minutes);
		} else if (durationMillis >= DateUtils.MILLIS_PER_MINUTE) {
			long minutes = (durationMillis / DateUtils.MILLIS_PER_MINUTE);
			long seconds = (durationMillis - (minutes * DateUtils.MILLIS_PER_MINUTE)) / DateUtils.MILLIS_PER_SECOND;
			humanDuration = String.format("%smin %ss", minutes, seconds);
		} else {
			int seconds = (int) (durationMillis / DateUtils.MILLIS_PER_SECOND);
			humanDuration = String.format("%ss", seconds);
		}
		return humanDuration;
	}

}
