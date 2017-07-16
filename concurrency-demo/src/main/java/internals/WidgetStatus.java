package internals;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Track status of each produced and consumed {@link Widget}
 */
public final class WidgetStatus {

	private static final Logger logger = LoggerFactory.getLogger(WidgetStatus.class);

	private final ConcurrentMap<WidgetStatusKey, Integer> widgetCount = new ConcurrentHashMap<>();
	private final ConcurrentMap<RunnableKey, ZonedDateTime> stopTimes = new ConcurrentHashMap<>();

	/**
	 * Create or update the Widget count for the WidgetStatusKey<br>
	 * 
	 * @param widgetStatusKey
	 *            {@link WidgetStatusKey}
	 * @param i
	 *            The new Widget total
	 * @return The previous Widget total
	 */
	public Integer putWidgetCount(WidgetStatusKey widgetStatusKey, Integer i) {
		Integer j = widgetCount.put(widgetStatusKey, i);
		logger.debug("WidgetStatus Widget Count: {}, {}", widgetStatusKey, j);
		return j;
	}

	/**
	 * Create or update the stop time for the RunnableKey<br>
	 * 
	 * @param runnableKey
	 *            {@link RunnableKey}
	 * @param date
	 *            The new stop time
	 * @return The previous stop time
	 */
	public ZonedDateTime putStopTime(RunnableKey runnableKey, ZonedDateTime date) {
		ZonedDateTime previousDate = stopTimes.put(runnableKey, date);
		logger.debug("WidgetStatus Stop Time: {}, {}", runnableKey, previousDate);
		return previousDate;
	}

	/**
	 * Get the current Widget count for the WidgetStatusKey<br>
	 * 
	 * @param widgetStatusKey
	 *            {@link WidgetStatusKey}
	 * @return The current Widget total
	 */
	public Integer getWidgetCount(String widgetStatusKey) {
		return widgetCount.get(widgetStatusKey);
	}

	/**
	 * Get the current stop time for the RunnableKey<br>
	 * 
	 * @param runnableKey
	 *            {@link RunnableKey}
	 * @return The current stop time
	 */
	public ZonedDateTime getStopTime(RunnableKey runnableKey) {
		return stopTimes.get(runnableKey);
	}

	/**
	 * Get the current total Widgets produced or consumed<br>
	 * 
	 * @param connectionType
	 *            {@link ConnectionType} which identifies Producer or Consumer
	 * @param widgetType
	 *            String which identifies simple class name of the
	 *            {@link Widget} data type
	 * @return The current Widget total for all producers or consumers
	 */
	// synchronized because of the iteration, potential performance impacts
	public synchronized Integer getWidgetCountSummary(ConnectionType connectionType, String widgetType) {
		return widgetCount.entrySet().stream().filter(entry -> entry.getKey().getWidgetType().equals(widgetType))
				.filter(entry -> entry.getKey().getConnectionType().equals(connectionType))
				.map(entry -> entry.getValue()).reduce(0, Integer::sum);
	}

	@Override
	public String toString() {
		Map<WidgetStatusKey, Integer> mapCount;
		Map<RunnableKey, ZonedDateTime> mapStop;
		// synchronized because of the iteration, potential performance impacts
		synchronized (this) {
			mapCount = new HashMap<>(widgetCount).entrySet().stream()
					.sorted((e1, e2) -> e1.getKey().toString().compareTo(e2.getKey().toString())).collect(Collectors
							.toMap(e -> e.getKey(), e -> e.getValue(), (oldVal, newVal) -> oldVal, LinkedHashMap::new));

			mapStop = new HashMap<>(stopTimes).entrySet().stream()
					.sorted((e1, e2) -> e1.getValue().compareTo(e2.getValue())).collect(Collectors
							.toMap(e -> e.getKey(), e -> e.getValue(), (oldVal, newVal) -> oldVal, LinkedHashMap::new));
		}

		StringBuilder sb = new StringBuilder("\nWidgets Processed:\n");
		for (WidgetStatusKey key : mapCount.keySet()) {
			sb.append(key).append("\t").append(mapCount.get(key)).append("\n");
		}
		sb.append("\nStop Times:\n");
		for (RunnableKey key : mapStop.keySet()) {
			sb.append(key).append("\t").append(mapStop.get(key).format(DateTimeFormatter.ISO_DATE_TIME)).append("\n");
		}
		return sb.toString();
	}

}
