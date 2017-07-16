package internals;

import java.time.ZonedDateTime;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * String and Long {@link Widget} Consumer<br>
 */
public final class Consumer implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

	private static final Random RANDOM = new Random();

	private final BlockingQueue<Widget<? extends Object>> queue;
	private final Connector connector;
	private final WidgetStatus widgetStatus;

	public Consumer(BlockingQueue<Widget<? extends Object>> queue, Connector connector, WidgetStatus widgetStatus) {
		this.queue = queue;
		this.connector = connector;
		this.widgetStatus = widgetStatus;
	}

	/**
	 * Consumer for String and Long {@link Widget} types<br>
	 */
	@Override
	public void run() {
		logger.info("Started consumer on thread {}, connector name {}, connector info {}",
				Thread.currentThread().getName(), connector.name(), connector.toString());
		int id = Math.toIntExact(Thread.currentThread().getId());
		RunnableKey runnableKey = new RunnableKey(connector.getConnectionType(),
				connector.getRunnableClass().getSimpleName(), id);
		logger.info("threadName: {}, runnableKey: {}", Thread.currentThread().getName(), runnableKey);
		try {
			// separate counters for String and Long Widgets
			AtomicInteger iString = new AtomicInteger(0);
			AtomicInteger iLong = new AtomicInteger(0);
			while (!Thread.currentThread().isInterrupted()) {
				Widget<?> widget = queue.poll(2, TimeUnit.SECONDS);
				if (widget == null) {
					logger.info("No more Widgets to consume.  Terminating {}", runnableKey);
					Thread.currentThread().interrupt();
				} else if (widget.get() instanceof String) {
					logger.debug("{} - String Consumed: {}", iString, widget.get());
					WidgetStatusKey widgetStatusKey = new WidgetStatusKey(connector.getConnectionType(),
							String.class.getSimpleName(), id);
					widgetStatus.putWidgetCount(widgetStatusKey, iString.incrementAndGet());
				} else if (widget.get() instanceof Long) {
					logger.debug("{} - Long Consumed: {}", iLong, widget.get());
					WidgetStatusKey widgetStatusKey = new WidgetStatusKey(connector.getConnectionType(),
							Long.class.getSimpleName(), id);
					widgetStatus.putWidgetCount(widgetStatusKey, iLong.incrementAndGet());
				} else {
					logger.error("{} - Consumed unexpected Widget of type {}", widget.get().getClass().getSimpleName());
				}
				// random sleep duration between 250 and 1250 millis
				TimeUnit.MILLISECONDS.sleep(250 + RANDOM.nextInt(1000));
			}
		} catch (InterruptedException e) {
			logger.info("InterruptedException for thread {}, runnable {}", Thread.currentThread().getName(),
					runnableKey);
			widgetStatus.putStopTime(runnableKey, ZonedDateTime.now());
			Thread.currentThread().interrupt();
		}
	}

}
