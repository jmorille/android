package eu.ttbox.geoping.test.domain.model;

import eu.ttbox.geoping.domain.model.PairingAuthorizeTypeEnum;
import android.test.AndroidTestCase;

public class PairingAuthorizeTypeEnumTest extends AndroidTestCase  {

    public void testGetByCode() {
        // Read Normal value
        for (PairingAuthorizeTypeEnum val : PairingAuthorizeTypeEnum.values()) {
            PairingAuthorizeTypeEnum readByCode = PairingAuthorizeTypeEnum.getByCode(val.getCode());
            assertEquals(val, readByCode);
        }
        // Read Bad value
        PairingAuthorizeTypeEnum readByNegCode = PairingAuthorizeTypeEnum.getByCode(-1);
        assertNull(readByNegCode);
        PairingAuthorizeTypeEnum readByBigCode = PairingAuthorizeTypeEnum.getByCode(1515);
        assertNull(readByBigCode);

    }
}
