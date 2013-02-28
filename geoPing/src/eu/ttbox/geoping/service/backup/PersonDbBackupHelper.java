package eu.ttbox.geoping.service.backup;

import java.util.Arrays;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import eu.ttbox.geoping.domain.model.Person;
import eu.ttbox.geoping.domain.person.PersonDatabase;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;
import eu.ttbox.geoping.domain.person.PersonHelper;

public class PersonDbBackupHelper  extends AbstractDbBackupHelper {

    @SuppressWarnings("unused")
    private static final String TAG = "PersonBackupHelper";

    public static final String BACKUP_KEY_PERSON_DB = "GEOPING_03_PERSON_DB";

    private PersonDatabase personDatabase;
    private PersonHelper helper;
    
    // ===========================================================
    // Constructor
    // ===========================================================

    
    public PersonDbBackupHelper(Context ctx) {
        super(ctx, BACKUP_KEY_PERSON_DB ); 
        personDatabase = new PersonDatabase(ctx);
    }
  
    
    // ===========================================================
    // Backup
    // ===========================================================
 
    public   Cursor getBackupCursor() {
        String[] columns = PersonColumns.ALL_COLS;
        Cursor cursor = personDatabase.queryEntities(columns, null, null, null);
        if (helper==null) {
            helper = new PersonHelper();
        }
        helper.initWrapper(cursor);
        return cursor;
    }
    
    public  ContentValues getCursorAsContentValues(Cursor cursor ) { 
        Person entity = helper.getEntity(cursor);
        ContentValues values =   PersonHelper.getContentValues(entity);
        return values;
    }



    // ===========================================================
    // Restore
    // ===========================================================

    @Override
    public long insertEntity(ContentValues values) { 
        return   personDatabase.insertEntity(values);
    }


    @Override
    public List<String> getValidColumns() {
        final List<String> validColumns = Arrays.asList(PersonColumns.ALL_COLS); 
        return validColumns;
    }

 

}
