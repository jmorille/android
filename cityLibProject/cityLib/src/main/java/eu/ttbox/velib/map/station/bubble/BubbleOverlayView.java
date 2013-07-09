package eu.ttbox.velib.map.station.bubble;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import eu.ttbox.osm.core.ExternalIntents;
import eu.ttbox.velib.R;
import eu.ttbox.velib.core.DateUtils;
import eu.ttbox.velib.model.FavoriteIconEnum;
import eu.ttbox.velib.model.Station;
import eu.ttbox.velib.service.VelibService;
import eu.ttbox.velib.ui.map.dialog.AliasInputDialog;
import eu.ttbox.velib.ui.map.dialog.SelectFavoriteIconDialog;

/**
 * @see https://github.com/jgilfelt/android-mapviewballoons/blob/master/android- mapviewballoons
 *      /src/com/readystatesoftware/mapviewballoons/BalloonOverlayView.java
 * @see http ://stackoverflow.com/questions/4800959/how-to-create-an-information- bubble -on-a-map-not-google-api
 * @see http://developer.android.com/guide/topics/graphics/2d-graphics.html
 * 
 */
public class BubbleOverlayView<ITEM> extends FrameLayout {

	private final String TAG = getClass().getSimpleName();

	private LinearLayout layout;
	private TextView title;
	private TextView snippet;
	private TextView cycleText, parckingText, ticketText;
	private TextView veloUpdatedtext;
	private TextView stationAddressText;

	private ImageView cycleImg, parkingImg;
	private ImageView imageVplus;
	private ImageView streetviewImg, navigationImg;

	private LinearLayout ticketBlock;
	private CheckBox veloDispoBulleFavorite;
	private Station station;
	private long stationVersion;

	private VelibService velibService;

	public BubbleOverlayView(Context context, VelibService velibService, int balloonBottomOffset) {
		super(context);
		this.velibService = velibService;
		setPadding(0, 0, 0, balloonBottomOffset);

		layout = new LinearLayout(context);
		layout.setVisibility(VISIBLE);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.station_balloon_overlay, layout);
		title = (TextView) v.findViewById(R.id.balloon_item_title);
		title.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				getAliasInputDialog().show();
				return true;
			}
		});
		// Address
		snippet = (TextView) v.findViewById(R.id.balloon_item_snippet);
		stationAddressText = (TextView) v.findViewById(R.id.balloon_item_address);

		// Block Dispo
		cycleText = (TextView) v.findViewById(R.id.balloon_block_panneau_velo_textdispo);
		cycleImg = (ImageView) v.findViewById(R.id.balloon_block_panneau_velo_image);
		// Block Parking
		parckingText = (TextView) v.findViewById(R.id.balloon_block_panneau_parking_textdispo);
		parkingImg = (ImageView) v.findViewById(R.id.balloon_block_panneau_parking_image);
		// Block Ticket
		ticketBlock = (LinearLayout) v.findViewById(R.id.balloon_block_panneau_ticket);
		ticketText = (TextView) v.findViewById(R.id.balloon_block_panneau_ticket_textdispo);
		// Update
		veloUpdatedtext = (TextView) v.findViewById(R.id.balloon_item_updated);
		// Favorite
		veloDispoBulleFavorite = (CheckBox) v.findViewById(R.id.veloDispoBulleFavorite);
		veloDispoBulleFavorite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				veloDispoBulleFavorite.setSelected(true);
				station.setFavory(!station.isFavory());
				if (BubbleOverlayView.this.velibService != null) {
					BubbleOverlayView.this.velibService.updateStationnFavorite(station);
				}
			}
		});
		veloDispoBulleFavorite.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				getFavoriteIconInputDialog().show();
				return true;
			}
		});
		// Image Bonus
		imageVplus = (ImageView) v.findViewById(R.id.balloon_bonus_image);
		streetviewImg = (ImageView) v.findViewById(R.id.balloon_streetview_image);
		streetviewImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startStreetView();
			}
		});
		navigationImg = (ImageView) v.findViewById(R.id.balloon_navigation_image);
		navigationImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startNavigationTo();
			}
		});
		//
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;
		params.width = 300;
		// params.height = 300;

		addView(layout, params);
	}

	private Dialog getFavoriteIconInputDialog() {
		final Dialog dialog = new SelectFavoriteIconDialog(getContext(), new SelectFavoriteIconDialog.OnSelectFavoriteIconListener() {

			@Override
			public void onSelectFavoriteIcon(FavoriteIconEnum favicon) {
				station.setFavoriteType(favicon);
				if (!station.isFavory()) {
					station.setFavory(true);
				}
				if (velibService != null) {
					velibService.updateStationnFavorite(station);
				}
				// Update View
				copyDataToViewBubble();
			}
		});
		return dialog;
	}

	private Dialog getAliasInputDialog() {
		AliasInputDialog dialog = new AliasInputDialog(getContext(), new AliasInputDialog.OnAliasInputListener() {

			@Override
			public void onAliasInput(String alias) {
				station.setNameAlias(alias);
				if (velibService != null) {
					velibService.updateStationnFavorite(station);
				}
				// Update View
				copyDataToViewBubble();
			}
		});
		dialog.setAliasInput(station.getNameAlias());
		return dialog;
	}

	/**
	 * @see http://developer.android.com/guide/appendix/g-app-intents.html
	 * @param lat
	 * @param lng
	 */
	private void startNavigationTo() {
		if (station != null) { 
			ExternalIntents.startActivityNavigationTo(getContext(), station.getLatitude(), station.getLongitude());
 		}
	}

	// TODO https://developers.google.com/maps/documentation/directions/?hl=fr-FR
	// http://maps.googleapis.com/maps/api/directions/json?origin=Adelaide,SA&destination=Adelaide,SA&waypoints=optimize:true|Barossa+Valley,SA|Clare,SA|Connawarra,SA|McLaren+Vale,SA&sensor=false
	/**
	 * @see http://developer.android.com/guide/appendix/g-app-intents.html
	 * 
	 */
	private void startStreetView() {
		if (station != null) {
			ExternalIntents.startActivityStreetView(getContext(), station.getLatitude(), station.getLongitude());
 		}
	}

	public Station getStation() {
		return station;
	}

	public boolean isBubleStation(Station testedStation) {
		boolean isBubleStation = false;
		if (station != null && testedStation != null) {
			isBubleStation = station.equals(testedStation);
		}
		return isBubleStation;
	}

	public void setData(Station item, long nowInMs) {
		boolean needUpdate = true;
		if (station != null && station.getId() == item.getId()) {
			needUpdate = stationVersion != item.getVeloUpdated(); 
		} else {
			needUpdate = true;
		}
		if (Log.isLoggable(TAG, Log.DEBUG))  
			Log.d(TAG, "Update Bubble with Station needed " + needUpdate);
		if (needUpdate) {
			this.station = item;
			this.stationVersion = station.getVeloUpdated();
			copyDataToViewBubble();

		}
		// Update time
		updateTime(nowInMs, needUpdate);
		// layout.setVisibility(VISIBLE);
	}

	private void copyDataToViewBubble() {
		// Favorite
		veloDispoBulleFavorite.setChecked(station.isFavory());
		// Name
		if (station.getNameAlias() != null && station.getNameAlias().length() > 0) {
			// Title
			title.setText(station.getNameAlias());
			// Snipet
			snippet.setText(station.getName());
			snippet.setVisibility(VISIBLE);
		} else {
			// Title
			title.setText(station.getName());
			// Snipet
			snippet.setText(null);
			snippet.setVisibility(GONE);
		}
		// Address
		if (station.getAddress() != null) {
			stationAddressText.setVisibility(VISIBLE);
			stationAddressText.setText(station.getAddress());
		} else {
			stationAddressText.setVisibility(GONE);
		}
		// Total
		// int veloTotal = station.getVeloTotal();
		// String veloTotalString = getResources().getQuantityString(R.plurals.numberOfVeloTotal, veloTotal, veloTotal);
		// stationAddressText.setText(veloTotalString);

		// Dispo
		int veloDispo = station.getStationCycle();
		String dispoString = String.valueOf(veloDispo);
		// if (station.getVeloAvailableDelta() != 0) {
		// StringBuilder sb = new StringBuilder().append(station.getVeloAvailable()).append('(');
		// if (station.getVeloAvailableDelta() > 0) {
		// sb.append('+');
		// }
		// sb.append(station.getVeloAvailableDelta()).append(')');
		// dispoString= sb.toString();
		// } else {
		// dispoString = String.valueOf(station.getVeloAvailable());
		// }
		cycleText.setText(dispoString);
		if (veloDispo < 1) {
			cycleImg.setImageDrawable(getResources().getDrawable(R.drawable.panneau_obligation_cycles_fin));
		} else {
			cycleImg.setImageDrawable(getResources().getDrawable(R.drawable.panneau_obligation_cycles));
		}
		// Parcking
		int veloParking = station.getStationParking();
		String parckingString = String.valueOf(veloParking);
		// if (station.getVeloFreeDelta() != 0) {
		// StringBuilder sb = new StringBuilder().append(station.getVeloFree()).append('(');
		// if (station.getVeloFreeDelta() > 0) {
		// sb.append('+');
		// }
		// sb.append(station.getVeloFreeDelta()).append(')');
		// parckingString = sb.toString();
		// } else {
		// parckingString = String.valueOf(station.getVeloFree());
		// }
		parckingText.setText(parckingString);
		if (veloParking < 1) {
			parkingImg.setImageDrawable(getResources().getDrawable(R.drawable.panneau_parking_fin));
		} else {
			parkingImg.setImageDrawable(getResources().getDrawable(R.drawable.panneau_parking));
		}

		// Ticket
		if (station.getVeloTicket() > 0) {
			String ticketString = String.valueOf(station.getVeloTicket());
			ticketText.setText(ticketString);
			ticketBlock.setVisibility(View.VISIBLE);
		} else {
			ticketBlock.setVisibility(View.GONE);
		}
		// Velib Plus
		if (station.getBonus()) {
			imageVplus.setVisibility(View.VISIBLE);
		} else {
			imageVplus.setVisibility(View.GONE);
		}
	}

	public boolean updateTime(long nowInMs) {
		return updateTime(nowInMs, false);
	}

	private boolean updateTime(long nowInMs, boolean needUpdate) {
		boolean isUpdated = false;
		if (station.getVeloUpdated() > 0) {
			long lastUpdateDeltaInMs = nowInMs - station.getVeloUpdated();
			if (needUpdate || lastUpdateDeltaInMs > DateUtils.MILLIS_PER_SECOND) {
				String humaunduration = DateUtils.formatDuration(lastUpdateDeltaInMs);
				//
				// String infoBubbleTextLine6 = new StringBuffer(356).append("Updated : ").append(humaunduration).toString();
				veloUpdatedtext.setText(humaunduration);
				isUpdated = true;
			}
		} else {
			veloUpdatedtext.setText("Never updated");
			isUpdated = true;
		}
		return isUpdated;
	}
}
