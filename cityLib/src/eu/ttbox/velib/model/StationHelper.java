package eu.ttbox.velib.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.widget.TextView;
import eu.ttbox.velib.core.AppConstants;
import eu.ttbox.velib.service.database.Velo.VeloColumns;

public class StationHelper {

    boolean isNotInit = true;

    // Velo Data
    public int idIdx = -1;
    public int providerIdx = -1;
    public int numberIdx = -1;
    public int nameIdx = -1;
    public int nameAliasIdx = -1;
    public int addressIdx = -1;
    public int fullAddressIdx = -1;
    public int latitudeE6Idx = -1;
    public int longitudeE6Idx = -1;
    public int openIdx = -1;
    public int bonusIdx = -1;

    // Config
    public int favoryIdx = -1;
    public int favoriteTypeIdx = -1;

    // Dispos
    public int veloTotalIdx = -1;
    public int stationCycleIdx = -1;
    public int stationParkingIdx = -1;
    public int veloTicketIdx = -1;
    public int veloUpdatedIdx = -1;

    public StationHelper initWrapper(Cursor cursor) {
        idIdx = cursor.getColumnIndex(VeloColumns.COL_ID);
        providerIdx = cursor.getColumnIndex(VeloColumns.COL_PROVIDER);
        numberIdx = cursor.getColumnIndex(VeloColumns.COL_NUMBER);
        nameIdx = cursor.getColumnIndex(VeloColumns.COL_NAME);
        nameAliasIdx = cursor.getColumnIndex(VeloColumns.COL_ALIAS_NAME);
        addressIdx = cursor.getColumnIndex(VeloColumns.COL_ADDRESS);
        fullAddressIdx = cursor.getColumnIndex(VeloColumns.COL_FULLADDRESS);
        latitudeE6Idx = cursor.getColumnIndex(VeloColumns.COL_LATITUDE_E6);
        longitudeE6Idx = cursor.getColumnIndex(VeloColumns.COL_LONGITUDE_E6);
        openIdx = cursor.getColumnIndex(VeloColumns.COL_OPEN);
        bonusIdx = cursor.getColumnIndex(VeloColumns.COL_BONUS);

        // Config
        favoryIdx = cursor.getColumnIndex(VeloColumns.COL_FAVORY);
        favoriteTypeIdx = cursor.getColumnIndex(VeloColumns.COL_FAVORY_TYPE);

        // Dispos
        veloTotalIdx = cursor.getColumnIndex(VeloColumns.COL_STATION_TOTAL);
        stationCycleIdx = cursor.getColumnIndex(VeloColumns.COL_STATION_CYCLE);
        stationParkingIdx = cursor.getColumnIndex(VeloColumns.COL_STATION_PARKING);
        veloTicketIdx = cursor.getColumnIndex(VeloColumns.COL_STATION_TICKET);
        veloUpdatedIdx = cursor.getColumnIndex(VeloColumns.COL_STATION_UPDATE_TIME);

        // Return
        isNotInit = false;
        return this;
    }

    public Station getEntity(Cursor cursor) {
        if (isNotInit) {
            initWrapper(cursor);
        }
        Station point = new Station();
        point.setId(idIdx > -1 ? cursor.getInt(idIdx) : AppConstants.UNSET_ID);
        point.setProvider(providerIdx > -1 ? cursor.getInt(providerIdx) : AppConstants.UNSET_ID);
        point.setNumber(numberIdx > -1 ? cursor.getString(numberIdx) : null);
        point.setName(nameIdx > -1 ? cursor.getString(nameIdx) : null);
        point.setNameAlias(nameAliasIdx > -1 ? cursor.getString(nameAliasIdx) : null);
        point.setAddress(addressIdx > -1 ? cursor.getString(addressIdx) : null);
        point.setFullAddress(fullAddressIdx > -1 ? cursor.getString(fullAddressIdx) : null);

        point.setLatitudeE6(latitudeE6Idx > -1 ? cursor.getInt(latitudeE6Idx) : Integer.MIN_VALUE);
        point.setLongitudeE6(longitudeE6Idx > -1 ? cursor.getInt(longitudeE6Idx) : Integer.MIN_VALUE);
        
        point.setOpen(openIdx>-1 ?  (cursor.getInt(openIdx) > 0 ? true : false) : false );
        point.setBonus(  bonusIdx>-1 ?  (cursor.getInt(bonusIdx) > 0 ? true : false) : false );
        point.setFavory(favoryIdx>-1 ?  (cursor.getInt(favoryIdx) > 0 ? true : false) : false );
        point.setFavoriteType(getFavoriteIconEnum(cursor));

        point.setVeloTotal(      veloTotalIdx > -1 ? cursor.getInt(veloTotalIdx) : AppConstants.UNSET_ID  );
        point.setStationCycle(   stationCycleIdx > -1 ? cursor.getInt(stationCycleIdx) : AppConstants.UNSET_ID  );
        point.setStationParking( stationParkingIdx > -1 ? cursor.getInt(stationParkingIdx) : AppConstants.UNSET_ID);
        point.setVeloTicket(     veloTicketIdx > -1 ? cursor.getInt(veloTicketIdx) : AppConstants.UNSET_ID);
        point.setVeloUpdated(    veloUpdatedIdx > -1 ? cursor.getLong(veloUpdatedIdx) : AppConstants.UNSET_ID);

        return point;
    }

    public StationHelper setTextWithIdx(TextView view, Cursor cursor, int idx) {
        view.setText(cursor.getString(idx));
        return this;
    }
    
    

    public StationHelper setTextStationId(TextView view, Cursor cursor) {
        return setTextWithIdx(view, cursor, idIdx);
    }

    public String getStationIdAsString(Cursor cursor) {
        return cursor.getString(idIdx);
    }

    public long getStationId(Cursor cursor) {
        return cursor.getLong(idIdx);
    }
    
    public int getStationTotal(Cursor cursor) {
        return cursor.getInt(veloTotalIdx);
    }
    public long getStationUpdated(Cursor cursor) {
        return cursor.getLong(veloUpdatedIdx);
    }
    
    public int getStationTicket(Cursor cursor) {
        return cursor.getInt(veloTicketIdx);
    }
    public int getStationCycle(Cursor cursor) {
        return cursor.getInt(stationCycleIdx);
    }
    public int getStationParking(Cursor cursor) {
        return cursor.getInt(stationParkingIdx);
    }
 
    public int getLatitudeE6(Cursor cursor) {
        return cursor.getInt(latitudeE6Idx);
    }

    public int getLongitudeE6(Cursor cursor) {
        return cursor.getInt(longitudeE6Idx);
    }
 
    public  FavoriteIconEnum getFavoriteIconEnum(Cursor cursor) {
        return favoriteTypeIdx>-1 ? FavoriteIconEnum.getFromName(  cursor.getString(favoriteTypeIdx)) : null;
    }

    public static ContentValues getContentValues(Station point) {
        ContentValues initialValues = new ContentValues();
        if (point.id > AppConstants.UNSET_ID ) {
            initialValues.put(VeloColumns.COL_ID, Integer.valueOf(point.id));
        } 
        initialValues.put(VeloColumns.COL_PROVIDER, point.getProvider());
        initialValues.put(VeloColumns.COL_NUMBER, point.getNumber());
        initialValues.put(VeloColumns.COL_NAME, point.getName());
        initialValues.put(VeloColumns.COL_ADDRESS, point.getAddress());
        initialValues.put(VeloColumns.COL_FULLADDRESS, point.getFullAddress());
        initialValues.put(VeloColumns.COL_LATITUDE_E6, point.getLatitudeE6());
        initialValues.put(VeloColumns.COL_LONGITUDE_E6, point.getLongitudeE6());
        initialValues.put(VeloColumns.COL_OPEN, point.getOpen());
        initialValues.put(VeloColumns.COL_BONUS, point.getBonus());
        initialValues.put(VeloColumns.COL_FAVORY, point.isFavory());
        initialValues.put(VeloColumns.COL_FAVORY_TYPE, point.getFavoriteTypeId());
        initialValues.put(VeloColumns.COL_ALIAS_NAME, point.getNameAlias());

        initialValues.put(VeloColumns.COL_STATION_TOTAL, point.getVeloTotal());
        initialValues.put(VeloColumns.COL_STATION_CYCLE, point.getStationCycle());
        initialValues.put(VeloColumns.COL_STATION_PARKING, point.getStationParking());
        initialValues.put(VeloColumns.COL_STATION_TICKET, point.getVeloTicket());
        initialValues.put(VeloColumns.COL_STATION_UPDATE_TIME, point.getVeloUpdated());

        
        // values.put(VeloColumns.COL_PROVIDER, point.getProvider());
        // values.put(VeloColumns.COL_NUMBER, point.getNumber());
        // values.put(VeloColumns.COL_NAME, point.getName());
        // values.put(VeloColumns.COL_ADDRESS, point.getAddress());
        // values.put(VeloColumns.COL_FULLADDRESS, point.getFullAddress());
        // values.put(VeloColumns.COL_LATITUDE_E6, point.getLatitudeE6());
        // values.put(VeloColumns.COL_LONGITUDE_E6, point.getLongitudeE6());
        // values.put(VeloColumns.COL_OPEN, point.getOpen());
        // values.put(VeloColumns.COL_BONUS, point.getBonus());
        // values.put(VeloColumns.COL_FAVORY, point.isFavory());
        // values.put(VeloColumns.COL_FAVORY_TYPE, point.getFavoriteTypeId());
        // values.put(VeloColumns.COL_ALIAS_NAME, point.getNameAlias());
        
        return initialValues;
    }


}
