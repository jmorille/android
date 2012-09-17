package eu.ttbox.geoping.domain.core.wrapper;

public interface HelperWrapper<T> {

	T getWrappedValue();

	// Iterator
	int size();

	boolean isEmpty();

	void clear();

	boolean containsKey(String key);

	Object get(String key);

	void remove(String key);

	// Set<String> keySet();

	// Long
	void putLong(String key, Long value);

	public long getLong(String key);

	// long getLong(String key, long defaultValue);

	// String
	void putString(String key, String value);

	String getString(String key);

	// String getString(String key, String defaultValue);

	// Int
	void putInt(String key, int value);

	int getInt(String key);

	// int getInt(String key, int defaultValue);

}
