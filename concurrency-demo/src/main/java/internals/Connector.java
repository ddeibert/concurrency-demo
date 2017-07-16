package internals;

import internals.ConnectionType;

/**
 * Types of producers and consumers that are supported<br>
 * Binds the Runnable class and {@link ConnectionType} to this enum.<br>
 * {@link #STRING_PRODUCER} and {@link #LONG_PRODUCER} are
 * {@link ConnectionType#PRODUCER}<br>
 * {@link #GENERAL_CONSUMER} is {@link ConnectionType#CONSUMER}<br>
 * 
 * @see ConnectionType
 */
public enum Connector {
	STRING_PRODUCER(ConnectionType.PRODUCER, StringProducer.class), LONG_PRODUCER(ConnectionType.PRODUCER,
			LongProducer.class), GENERAL_CONSUMER(ConnectionType.CONSUMER, Consumer.class);

	private final ConnectionType connectionType;
	private final Class<? extends Runnable> runnableClass;

	// ensure class is Runnable
	Connector(ConnectionType connectionType, Class<? extends Runnable> runnableClass) {
		this.connectionType = connectionType;
		this.runnableClass = runnableClass;
	}

	/**
	 * @return The {@link ConnectionType} for this Connector
	 */
	public ConnectionType getConnectionType() {
		return connectionType;
	}

	/**
	 * @return The Runnable class used by this Connector
	 */
	public Class<? extends Runnable> getRunnableClass() {
		return runnableClass;
	}

	/**
	 * @return A String representation different than name()
	 */
	@Override
	public String toString() {
		return connectionType + "-" + runnableClass.getSimpleName();
	}
}
