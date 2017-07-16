package internals;

/**
 * Vessel to house a given object of type T
 *
 * @param <T>
 *            The type of object which is expected to be immutable and/or
 *            threadsafe.
 */
// Both Long and String are immutable
public final class Widget<T> {

	private final T t;

	/**
	 * Instantiate the Widget
	 * 
	 * @param t
	 *            The object to be encapsulated
	 */
	public Widget(T t) {
		this.t = t;
	}

	/**
	 * Return the value
	 * 
	 * @return value of type T
	 */
	public T get() {
		return t;
	}

}
