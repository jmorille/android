package eu.ttbox.geoping.domain.core;

import android.database.DatabaseUtils;
import android.text.TextUtils;

public class PhoneDatabaseUtils {
    
    private void appendPhoneLookupSelection(StringBuilder sb, String number, String numberE164) {
        boolean hasNumberE164 = !TextUtils.isEmpty(numberE164);
        boolean hasNumber = !TextUtils.isEmpty(number);
        if (hasNumberE164 || hasNumber) {
            if (hasNumberE164) {
                sb.append(" lookup.normalized_number = ");
                DatabaseUtils.appendEscapedSQLString(sb, numberE164);
            }
            if (hasNumberE164 && hasNumber) {
                sb.append(" OR ");
            }
            if (hasNumber) {
                int numberLen = number.length();
                sb.append(" lookup.len <= ");
                sb.append(numberLen);
                sb.append(" AND substr(");
                DatabaseUtils.appendEscapedSQLString(sb, number);
                sb.append(',');
                sb.append(numberLen);
                sb.append(" - lookup.len + 1) = lookup.normalized_number");

                // Some countries (e.g. Brazil) can have incoming calls which contain only the local
                // number (no country calling code and no area code). This case is handled below.
                // Details see b/5197612.
                // This also handles a Gingerbread -> ICS upgrade issue; see b/5638376.
                sb.append(" OR (");
                sb.append(" lookup.len > ");
                sb.append(numberLen);
                sb.append(" AND substr(lookup.normalized_number,");
                sb.append("lookup.len + 1 - ");
                sb.append(numberLen);
                sb.append(") = ");
                DatabaseUtils.appendEscapedSQLString(sb, number);
                sb.append(")");
            }
        }
    }

}
