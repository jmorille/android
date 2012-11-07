package eu.ttbox.velib.map.station.drawable;

import android.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;
import eu.ttbox.velib.model.FavoriteIconEnum;
import eu.ttbox.velib.model.Station;

public class StationDispoIcDrawable extends StationDispoDrawable {

	private Bitmap starBitmap, bonusBitmap, infoBitmap;
	private int starBitmapHeight, bonusBitmapHeight, bonusBitmapWidth, infoBitmapWidth;

	public StationDispoIcDrawable(Context context, int expectedVelo) {
		super(context, expectedVelo);
		initStarBitmap();
	}

	private void initStarBitmap() {
		// Start
		// int id = R.drawable.star_on;
		Resources resources = context.getResources();
		starBitmap = BitmapFactory.decodeResource(resources, R.drawable.star_big_on);
		starBitmapHeight = starBitmap.getHeight();
		// Info
		infoBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_dialog_info);
		infoBitmapWidth = infoBitmap.getWidth();
		// Bonus
		bonusBitmap = BitmapFactory.decodeResource(resources, eu.ttbox.velib.R.drawable.plus_16);
		// bonusBitmap=BitmapFactory.decodeResource(resources, R.drawable.ic_input_add);
		bonusBitmapHeight = bonusBitmap.getHeight();
		bonusBitmapWidth = bonusBitmap.getWidth();
		// Log
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "Icon Favorite Default Star : Bitmap Height = " + starBitmapHeight);
			Log.d(TAG, "Icon Bonus : Bitmap Height = " + bonusBitmapHeight);
			for (FavoriteIconEnum favico : FavoriteIconEnum.values()) {
				Log.d(TAG, String.format("Icon Favorite %s : Bitmap Height = %s", favico, favico.getIconBitmap(resources)));
			}
		}
	}

	@Override
	public void drawForExternalRadius(final Canvas canvas, final int radiusExternal, final Station station, final Point myScreenCoords) {
		super.drawForExternalRadius(canvas, radiusExternal, station, myScreenCoords);
		// Draw Star
		if (station.isFavory()) {
			drawFavorite(canvas, myScreenCoords, station.getFavoriteType(), false);
		}
		if (station.getBonus()) {
			drawBonus(canvas, myScreenCoords, station.getStationParking(), this.expectedVelo, false);
		}
	}

	private void drawInfo(Canvas canvas, Point myScreenCoords, int veloFree, int expectedVelo, int zoomLevel, boolean b) {
		canvas.drawBitmap(infoBitmap, myScreenCoords.x - infoBitmapWidth, myScreenCoords.y, textPaint);
	}

	private void drawFavorite(Canvas canvas, Point myScreenCoords, FavoriteIconEnum favoriteType, boolean b) {
		if (favoriteType == null) {
			canvas.drawBitmap(starBitmap, myScreenCoords.x, myScreenCoords.y - starBitmapHeight, textPaint);
		} else {
			canvas.drawBitmap(favoriteType.getIconBitmap(context.getResources()), myScreenCoords.x, myScreenCoords.y - starBitmapHeight, textPaint);
		}
	}

	private void drawBonus(Canvas canvas, Point myScreenCoords, int veloFree, int expectedVelo, boolean b) {
		canvas.drawBitmap(bonusBitmap, myScreenCoords.x - bonusBitmapWidth, myScreenCoords.y - bonusBitmapHeight, textPaint);
	}
}
