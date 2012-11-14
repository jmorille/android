package eu.ttbox.osm.ui.map.mylocation;

import eu.ttbox.osm.R;
import android.content.Context;
import android.content.res.Resources;

public enum CompassEnum {

	// ( "N", 360 )
	N("N", 0), //
	NNE("NNE", 22.5f), //
	NE("NE", 45f), //
	ENE("ENE", 67.5f), //
	E("E", 90f), //
	ESE("ESE", 112.5f), //
	SE("SE", 135f), //
	SSE("SSE", 157.5f), //
	S("S", 180f), //
	SSW("SSW", 202.5f), //
	SW("SW", 225f), //
	WSW("WSW", 247.5f), //
	W("W", 270f), //
	WNW("WNW", 292.5f), //
	NW("NW", 315f), //
	NNW("NNW", 337.5f);//

	final float angle;
	final char[] cardinalPoints;

	CompassEnum(String cardinalPoint, float angle) {
		this.cardinalPoints = cardinalPoint.toCharArray();
		this.angle = angle;
	}

	/**
	 * 
	 * @param context
	 * @return like Nord-Nord-Est
	 */
	public String getI18nLabel(Context context) {
		// I18N String
		Resources resource = context.getResources();
		StringBuilder sb = new StringBuilder();
		boolean isNotFirst = false;
		for (char c : cardinalPoints) {
			switch (c) {
			case 'N':
				sb.append(resource.getString(R.string.cardinal_point_nord));
				break;
			case 'S':
				sb.append(resource.getString(R.string.cardinal_point_sud));
				break;
			case 'E':
				sb.append(resource.getString(R.string.cardinal_point_est));
				break;
			case 'W':
				sb.append(resource.getString(R.string.cardinal_point_west));
				break;
			default:
				;
				sb.append(c);
				break;
			}
			if (isNotFirst) {
				sb.append('-');
			} else {
				isNotFirst = true;
			}
		}

		return sb.toString();
	}

	/**
	 * 
	 * @param context
	 * @return like NNE
	 */
	public String getI18nLabelShort(Context context) {
		// I18N String
		Resources resource = context.getResources();
		StringBuilder sb = new StringBuilder();
		for (char c : cardinalPoints) {
			switch (c) {
			case 'N':
				sb.append(resource.getString(R.string.cardinal_point_short_nord));
				break;
			case 'S':
				sb.append(resource.getString(R.string.cardinal_point_short_sud));
				break;
			case 'E':
				sb.append(resource.getString(R.string.cardinal_point_short_est));
				break;
			case 'W':
				sb.append(resource.getString(R.string.cardinal_point_short_west));
				break;
			default:
				sb.append(c);
				break;
			}
		}

		return sb.toString();
	}

	/**
	 * {@link http://www.toujourspret.com/techniques/orientation/topographie/
	 * rose_des_vents.php}
	 * 
	 * @param angleInDegres
	 *            Angle In degres
	 * @return
	 */
	public static CompassEnum getCardinalPoint(float angleInDegres) {
		float angle = convertToDegresRange(angleInDegres);
		for (CompassEnum compass : CompassEnum.values()) {
			float delta = Math.abs(compass.angle - angle);
			if (delta <= 11.25f) {
				return compass;
			}
		}
		// Last Born
		float delta = Math.abs(360 - angle); 
		if (delta <= 11.25f) {
			return CompassEnum.N;
		}
		return null;
	}

	public static float convertToDegresRange(float degres) {
		float result = degres;
		if (result <= -360) {
			result = (result % 360f) + 360f;
		} else if (result < 0) {
			result = result + 360f;
		} else if (result >= 360f) {
			result = degres % 360f;
		}
		return result;
	}
}
