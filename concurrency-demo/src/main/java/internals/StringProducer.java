package internals;

import java.time.ZonedDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * String {@link Widget} Producer<br>
 */
public final class StringProducer implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(StringProducer.class);

	// this should be passed in
	private static final int SECOND_DELAY = 1;

	private final BlockingQueue<Widget<String>> queue;
	private final Connector connector;
	private final WidgetStatus widgetStatus;

	/**
	 * Create String {@link Widget} producer<br>
	 * 
	 * @param queue
	 *            BlockingQueue where Widgets will go
	 * @param connector
	 *            {@link Connector} used to create keys
	 * @param widgetStatus
	 *            {@link WidgetStatus} where results are recorded
	 */
	public StringProducer(BlockingQueue<Widget<String>> queue, Connector connector, WidgetStatus widgetStatus) {
		this.queue = queue;
		this.connector = connector;
		this.widgetStatus = widgetStatus;
	}

	/**
	 * Producer of String {@link Widget} types<br>
	 */
	@Override
	public void run() {
		logger.info("Started producer on thread {}, connector name {}, connector info {}",
				Thread.currentThread().getName(), connector.name(), connector.toString());
		int id = Math.toIntExact(Thread.currentThread().getId());
		RunnableKey runnableKey = new RunnableKey(connector.getConnectionType(),
				connector.getRunnableClass().getSimpleName(), id);
		WidgetStatusKey widgetStatusKey = new WidgetStatusKey(connector.getConnectionType(),
				String.class.getSimpleName(), id);
		logger.info("threadName: {}, runnableKey: {}", Thread.currentThread().getName(), runnableKey);

		try {
			// used as counter and incorporated into the produced Widget
			AtomicInteger i = new AtomicInteger(0);
			while (!Thread.currentThread().isInterrupted()) {
				Widget<String> widget = new Widget<String>("String" + i.getAndIncrement() + "-" + runnableKey);
				queue.put(widget);
				widgetStatus.putWidgetCount(widgetStatusKey, i.get());
				logger.info("put {}", widget.get());
				TimeUnit.SECONDS.sleep(SECOND_DELAY);
			}
		} catch (InterruptedException e) {
			logger.info("InterruptedException for thread {}, runnable {}", Thread.currentThread().getName(),
					runnableKey);
			widgetStatus.putStopTime(runnableKey, ZonedDateTime.now());
			Thread.currentThread().interrupt();
		}
	}

}
