package internals;

import java.util.Objects;

/**
 * Key for segmenting data types. Used in {@link WidgetStatus}. Necessary
 * because consumers process multiple data types.<br>
 */
public final class WidgetStatusKey {

	private final ConnectionType connectionType;
	private final String widgetType;
	private final int id;

	/**
	 * Create the Widget Status Key<br>
	 * 
	 * @param connectionType
	 *            {@link ConnectionType} which identifies Producer or Consumer
	 * @param widgetType
	 *            A String intended to represent the simple class name of the
	 *            {@link Widget} type
	 * @param id
	 *            int to distinguish between multiple instances
	 */
	public WidgetStatusKey(ConnectionType connectionType, String widgetType, int id) {
		this.connectionType = connectionType;
		this.widgetType = widgetType;
		this.id = id;
	}

	public ConnectionType getConnectionType() {
		return connectionType;
	}

	public int getId() {
		return id;
	}

	public String getWidgetType() {
		return widgetType;
	}

	@Override
	public String toString() {
		return connectionType.name() + "-" + widgetType + "-" + id;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof WidgetStatusKey)) {
			return false;
		}
		WidgetStatusKey widgetStatusKey = (WidgetStatusKey) obj;
		if (widgetStatusKey.getId() != this.getId()
				|| !widgetStatusKey.getConnectionType().equals(this.getConnectionType())
				|| !widgetStatusKey.getWidgetType().equals(this.getWidgetType())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(connectionType, widgetType, id);
	}

}
