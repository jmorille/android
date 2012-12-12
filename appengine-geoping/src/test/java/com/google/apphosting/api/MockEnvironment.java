package com.google.apphosting.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock environment bean for testing persistence, as described <a
 * href="http://code.google.com/appengine/docs/java/howto/unittesting.html">here</a>
 * 
 * @author androns
 */
public class MockEnvironment implements ApiProxy.Environment {

    /**
     * @see com.google.apphosting.api.ApiProxy.Environment#getAppId()
     */
    @Override
    public String getAppId() {
        return "test";
    }

    /**
     * @see com.google.apphosting.api.ApiProxy.Environment#getAttributes()
     * @return empty map
     */
    @Override
    public Map<String, Object> getAttributes() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("com.google.appengine.server_url_key", "http://localhost:8080");
        return map;
    }

    /**
     * @see com.google.apphosting.api.ApiProxy.Environment#getAuthDomain()
     * @throws UnsupportedOperationException
     *             Not implemented.
     */
    @Override
    public String getAuthDomain() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * @see com.google.apphosting.api.ApiProxy.Environment#getEmail()
     * @throws UnsupportedOperationException
     *             Not implemented.
     */
    @Override
    public String getEmail() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * @see com.google.apphosting.api.ApiProxy.Environment#getRequestNamespace()
     */
    @Override
    public String getRequestNamespace() {
        return "";
    }

    /**
     * @see com.google.apphosting.api.ApiProxy.Environment#getVersionId()
     */
    @Override
    public String getVersionId() {
        return "1.0";
    }

    /**
     * @see com.google.apphosting.api.ApiProxy.Environment#isAdmin()
     * @throws UnsupportedOperationException
     *             Not implemented.
     */
    @Override
    public boolean isAdmin() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * @see com.google.apphosting.api.ApiProxy.Environment#isLoggedIn()
     * @throws UnsupportedOperationException
     *             Not implemented.
     */
    @Override
    public boolean isLoggedIn() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
