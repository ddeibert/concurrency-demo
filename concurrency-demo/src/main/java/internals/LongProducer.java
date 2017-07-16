package internals;

import java.time.ZonedDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Long {@link Widget} Producer<br>
 */
public final class LongProducer implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(LongProducer.class);

	// this should be passed in
	private static final int SECOND_DELAY = 1;

	private final BlockingQueue<Widget<Long>> queue;
	private final Connector connector;
	private final WidgetStatus widgetStatus;

	/**
	 * Create Long {@link Widget} producer<br>
	 * 
	 * @param queue
	 *            BlockingQueue where Widgets will go
	 * @param connector
	 *            {@link Connector} used to create keys
	 * @param widgetStatus
	 *            {@link WidgetStatus} where results are recorded
	 */
	public LongProducer(BlockingQueue<Widget<Long>> queue, Connector connector, WidgetStatus widgetStatus) {
		this.queue = queue;
		this.connector = connector;
		this.widgetStatus = widgetStatus;
	}

	/**
	 * Producer of Long {@link Widget} types<br>
	 */
	@Override
	public void run() {
		logger.info("Started producer on thread {}, connector name {}, connector info {}",
				Thread.currentThread().getName(), connector.name(), connector.toString());
		int id = Math.toIntExact(Thread.currentThread().getId());
		RunnableKey runnableKey = new RunnableKey(connector.getConnectionType(),
				connector.getRunnableClass().getSimpleName(), id);
		WidgetStatusKey widgetStatusKey = new WidgetStatusKey(connector.getConnectionType(), Long.class.getSimpleName(),
				id);
		logger.info("threadName: {}, runnableKey: {}", Thread.currentThread().getName(), runnableKey);

		try {
			// used as counter and incorporated into the produced Widget
			AtomicInteger i = new AtomicInteger(0);
			while (!Thread.currentThread().isInterrupted()) {
				Widget<Long> widget = new Widget<Long>((long) i.getAndIncrement());
				queue.put(widget);
				widgetStatus.putWidgetCount(widgetStatusKey, i.get());
				logger.debug("put Long{}", widget.get());
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
