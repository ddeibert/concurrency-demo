package internals;

import java.util.Objects;

/**
 * Key associated with each Runnable instance. Used in {@link WidgetStatus}<br>
 */
public final class RunnableKey {

	// not needed for uniqueness, but helpful in output
	private final ConnectionType connectionType;
	private final String runnableName;
	private final int id;

	/**
	 * Create the Runnable Key<br>
	 * 
	 * @param connectionType
	 *            {@link ConnectionType} which identifies Producer or Consumer
	 * @param runnableName
	 *            A String intended to represent the simple class name of the
	 *            Runnable
	 * @param id
	 *            int to distinguish between multiple instances
	 */
	public RunnableKey(ConnectionType connectionType, String runnableName, int id) {
		this.connectionType = connectionType;
		this.runnableName = runnableName;
		this.id = id;
	}

	public ConnectionType getConnectionType() {
		return connectionType;
	}

	public int getId() {
		return id;
	}

	public String getRunnableName() {
		return runnableName;
	}

	@Override
	public String toString() {
		return id + "-" + connectionType.name() + "-" + runnableName;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RunnableKey)) {
			return false;
		}
		RunnableKey runnableKey = (RunnableKey) obj;
		if (runnableKey.getId() != this.getId() || !runnableKey.getConnectionType().equals(this.getConnectionType())
				|| !runnableKey.getRunnableName().equals(this.getRunnableName())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(connectionType, runnableName, id);
	}

}
