package eu.ttbox.geoping.domain.person;

import android.content.ContentValues;
import android.database.Cursor;
import android.widget.TextView;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.domain.model.Person;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;

public class PersonHelper {

    boolean isNotInit = true;
    public int idIdx = -1;
    public int displayNameIdx = -1;
    public int phoneIdx = -1;
    public int colorIdx = -1;
    public int contactIdIdx = -1;
    public int pairingTimeIdx = -1;
    // Encryption
    public int encryptionPubKeyIdx= -1;
    public int encryptionPrivKeyIdx= -1;
    public int encryptionRemotePubKeyIdx= -1;
    public int encryptionRemoteTimeIdx= -1;
	public int encryptionRemoteWayIdx= -1;

    public PersonHelper initWrapper(Cursor cursor) {
        idIdx = cursor.getColumnIndex(PersonColumns.COL_ID);
        displayNameIdx = cursor.getColumnIndex(PersonColumns.COL_NAME);
        phoneIdx = cursor.getColumnIndex(PersonColumns.COL_PHONE);
        colorIdx = cursor.getColumnIndex(PersonColumns.COL_COLOR);
        contactIdIdx = cursor.getColumnIndex(PersonColumns.COL_CONTACT_ID);
        pairingTimeIdx = cursor.getColumnIndex(PersonColumns.COL_PAIRING_TIME);
        // Encryption
        encryptionPubKeyIdx = cursor.getColumnIndex(PersonColumns.COL_ENCRYPTION_PUBKEY);
        encryptionPrivKeyIdx = cursor.getColumnIndex(PersonColumns.COL_ENCRYPTION_PRIVKEY);
        encryptionRemotePubKeyIdx = cursor.getColumnIndex(PersonColumns.COL_ENCRYPTION_REMOTE_PUBKEY);
        encryptionRemoteTimeIdx = cursor.getColumnIndex(PairingColumns.COL_ENCRYPTION_REMOTE_TIME);
        encryptionRemoteWayIdx = cursor.getColumnIndex(PairingColumns.COL_ENCRYPTION_REMOTE_WAY); 
     
        isNotInit = false;
        return this;
    }

    public Person getEntity(Cursor cursor) {
        if (isNotInit) {
            initWrapper(cursor);
        }
        Person user = new Person();
        user.setId(idIdx > -1 ? cursor.getLong(idIdx) :  AppConstants.UNSET_ID);
        user.setDisplayName(displayNameIdx > -1 ? cursor.getString(displayNameIdx) : null);
        user.setPhone(phoneIdx > -1 ? cursor.getString(phoneIdx) : null);
        user.setColor(colorIdx > -1 ? cursor.getInt(colorIdx) : 0);
        user.setContactId(contactIdIdx > -1 ? cursor.getString(contactIdIdx) : null);
        user.setPairingTime(pairingTimeIdx > -1 ? cursor.getLong(pairingTimeIdx)  : AppConstants.UNSET_TIME );
        // Encryption
        user.encryptionPrivKey = encryptionPrivKeyIdx>-1?cursor.getString(encryptionPrivKeyIdx) : null;
        user.encryptionPubKey = encryptionPubKeyIdx>-1?cursor.getString(encryptionPubKeyIdx) : null;
        user.encryptionRemotePubKey = encryptionRemotePubKeyIdx>-1?cursor.getString(encryptionRemotePubKeyIdx) : null;
        user.setEncryptionRemoteTime(encryptionRemoteTimeIdx > -1 ? cursor.getLong(encryptionRemoteTimeIdx)  : AppConstants.UNSET_TIME );
        user.encryptionRemoteWay = encryptionRemoteWayIdx>-1?cursor.getString(encryptionRemoteWayIdx) : null;
        return user;
    }

    private PersonHelper setTextWithIdx(TextView view, Cursor cursor, int idx) {
        view.setText(cursor.getString(idx));
        return this;
    }

    public PersonHelper setTextPersonId(TextView view, Cursor cursor) {
        return setTextWithIdx(view, cursor, idIdx);
    }

    public String getPersonIdAsString(Cursor cursor) {
        return cursor.getString(idIdx);
    }

    public long getPersonId(Cursor cursor) {
        return cursor.getLong(idIdx);
    }

    public int getPersonColor(Cursor cursor) {
        return cursor.getInt(colorIdx);
    }

    public String getContactId(Cursor cursor) {
        return cursor.getString(contactIdIdx);
    }
 
    
    public PersonHelper setTextPersonName(TextView view, Cursor cursor) {
        return setTextWithIdx(view, cursor, displayNameIdx);
    }

    public PersonHelper setTextPersonPhone(TextView view, Cursor cursor) {
        return setTextWithIdx(view, cursor, phoneIdx);
    }

    public String getPersonPhone(Cursor cursor) {
        return cursor.getString(phoneIdx);
    }

 
    public String getPersonDisplayName(Cursor cursor) {
        return cursor.getString(displayNameIdx);
    }

    public static ContentValues getContentValues(Person entity) {
        ContentValues initialValues = new ContentValues(PersonColumns.ALL_COLS.length);
        if (entity.id > -1) {
            initialValues.put(PersonColumns.COL_ID, Long.valueOf(entity.id));
        }
        initialValues.put(PersonColumns.COL_NAME, entity.displayName);
        initialValues.put(PersonColumns.COL_PHONE, entity.phone);
        initialValues.put(PersonColumns.COL_COLOR, entity.color);
        initialValues.put(PersonColumns.COL_CONTACT_ID, entity.contactId);
        initialValues.put(PersonColumns.COL_PAIRING_TIME, entity.pairingTime);
        // Encryption
        initialValues.put(PersonColumns.COL_ENCRYPTION_PRIVKEY, entity.encryptionPrivKey);
        initialValues.put(PersonColumns.COL_ENCRYPTION_PUBKEY, entity.encryptionPubKey);
        initialValues.put(PersonColumns.COL_ENCRYPTION_REMOTE_PUBKEY, entity.encryptionRemotePubKey);
        initialValues.put(PersonColumns.COL_ENCRYPTION_REMOTE_TIME, entity.encryptionRemoteTime);
        initialValues.put(PersonColumns.COL_ENCRYPTION_REMOTE_WAY, entity.encryptionRemoteWay); 
      
        return initialValues;
    }

}
