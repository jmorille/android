package eu.ttbox.velib.countdown;

import eu.ttbox.velib.core.MorseCodeConverter;
/**
 * @see https://github.com/jayway/maven-android-plugin-samples/blob/master/morseflash/morse-lib/src/main/java/com/simpligility/android/morse/MorseCodeConverter.java
 *
 */
public class VibratorMorsePattern {

	 public static long[] pattern(String str) {
		 long[] morsePattern = MorseCodeConverter.pattern(str);
		 return morsePattern;
	 }
	 
//	private static long[] convertToVibratorPattern(long[] morsePattern) {
//		int morseCodeSize = morsePattern.length;
//		long[] vibratorPattern = new long[morseCodeSize + (int) (morseCodeSize / 2)];
//		int i = 0;
//		for (long morseLetter : morsePattern) {
//			if (i > 0) {
//				morsePattern[i++] = short_gap;
//			}
//			morsePattern[i++] = morseLetter;
//		}
//		return vibratorPattern;
//	}

}
