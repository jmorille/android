package net.kindleit.gae.example.server;

import org.junit.After;
import org.junit.Before;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

/**
 * Performs datastore setup, as described <a
 * href="http://code.google.com/appengine/docs/java/howto/unittesting.html">here</a>.
 * 
 * @author androns
 */
public abstract class LocalDatastoreTest {

    private final LocalServiceTestHelper helper =
        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    
        /**
         * 
         */
        @Before
        public void setUp() {
                helper.setUp();
        }

        /**
         * @see LocalServiceTest#tearDown()
         */
        @After
        public void tearDown() {
                helper.tearDown();
        }
}
