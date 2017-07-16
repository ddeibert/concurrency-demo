package main;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import internals.ConnectionType;
import internals.Connector;
import internals.Widget;
import internals.WidgetStatus;

/**
 * Main class - see main method
 */
public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	// these should be moved to a properties file
	private static final int QUEUE_CAPACITY = 300;
	private static final int STRING_PRODUCER_COUNT = 3;
	private static final int LONG_PRODUCER_COUNT = 2;
	private static final int CONSUMER_COUNT = 2;
	private static final int DURATION_SECONDS = 50;
	private static final String OUTPUT_FILE = "results.txt";

	/**
	 * Initiate producers and consumers, wait allotted time, consume results and
	 * produce output.
	 * 
	 * @param args
	 *            Not used
	 */
	public static void main(String args[]) {
		BlockingQueue<Widget<? extends Object>> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
		WidgetStatus widgetStatus = new WidgetStatus();

		ExecutorService stringExecutor = getExecutor(Connector.STRING_PRODUCER, STRING_PRODUCER_COUNT, queue,
				widgetStatus);
		ExecutorService longExecutor = getExecutor(Connector.LONG_PRODUCER, LONG_PRODUCER_COUNT, queue, widgetStatus);
		ExecutorService consumerExecutor = getExecutor(Connector.GENERAL_CONSUMER, CONSUMER_COUNT, queue, widgetStatus);

		waitForIt(DURATION_SECONDS);

		logger.info("Terminating producers");
		longExecutor.shutdownNow();
		stringExecutor.shutdownNow();

		consumerExecutor.shutdown();
		logger.info("Awaiting consumer termination");
		boolean consumerComplete = false;
		try {
			consumerComplete = consumerExecutor.awaitTermination(DURATION_SECONDS * 2, TimeUnit.SECONDS);
			logger.info("consumerComplete: {}", consumerComplete);
		} catch (InterruptedException e) {
			logger.error("Error while waiting for consumer to complete", e);
		}

		logger.info("String Widgets Produced: {}",
				widgetStatus.getWidgetCountSummary(ConnectionType.PRODUCER, String.class.getSimpleName()).toString());
		logger.info("String Widgets Consumed: {}",
				widgetStatus.getWidgetCountSummary(ConnectionType.CONSUMER, String.class.getSimpleName()).toString());
		logger.info("Long Widgets Produced: {}",
				widgetStatus.getWidgetCountSummary(ConnectionType.PRODUCER, Long.class.getSimpleName()).toString());
		logger.info("Long Widgets Consumed: {}",
				widgetStatus.getWidgetCountSummary(ConnectionType.CONSUMER, Long.class.getSimpleName()).toString());

		outputResults(widgetStatus);

		logger.info("Processing complete. See {} for details.", OUTPUT_FILE);

		if (!consumerComplete) {
			logger.error("Final results recorded prior to completion");
		}
	}

	/**
	 * Short delay
	 * 
	 * @param delaySeconds
	 *            Number of seconds to delay
	 */
	private static void waitForIt(int delaySeconds) {
		logger.info("Starting {} second delay", delaySeconds);
		try {
			TimeUnit.SECONDS.sleep(delaySeconds);
		} catch (InterruptedException e) {
			logger.error("InterruptedException during wait: ", e);
			Thread.currentThread().interrupt();
		}
		logger.info("Completed {} second delay", delaySeconds);
	}

	/**
	 * Establish an {@link ExecutorService} based on the input parameters.
	 * 
	 * @param connector
	 *            The {@link Connector} to bind to this {@link ExecutorService}
	 * @param threadCount
	 *            The number of runnable threads for this
	 *            {@link ExecutorService}
	 * @param queue
	 *            The {@link BlockingQueue} to bind to this
	 *            {@link ExecutorService}
	 * @param widgetStatus
	 *            The {@link WidgetStatus} to bind to each Runnable instance
	 * @return The initialized {@link ExecutorService}
	 */
	private static ExecutorService getExecutor(Connector connector, int threadCount,
			BlockingQueue<Widget<? extends Object>> queue, WidgetStatus widgetStatus) {

		logger.debug(connector.getConnectionType().toString());
		logger.debug(connector.getRunnableClass().getSimpleName());

		Runnable runnable = null;
		try {
			// instantiate the Runnable via reflection
			Class<? extends Runnable> runnableClass = connector.getRunnableClass();
			Constructor<? extends Runnable> constructor = runnableClass.getConstructor(BlockingQueue.class,
					Connector.class, WidgetStatus.class);
			runnable = constructor.newInstance(queue, connector, widgetStatus);
		} catch (Exception e) {
			// Possible Exceptions: NoSuchMethodException, SecurityException,
			// InstantiationException, IllegalAccessException,
			// IllegalArgumentException, InvocationTargetException
			logger.error(e.getMessage(), e);
			// throw e;
		}

		ExecutorService executor = Executors.newFixedThreadPool(threadCount);

		for (int i = 0; i < threadCount; i++) {
			logger.debug("Thread: {}", i);
			executor.execute(runnable);
		}

		return executor;
	}

	/**
	 * Output the results
	 * 
	 * @param widgetStatus
	 *            The {@link WidgetStatus} to output
	 */
	private static void outputResults(WidgetStatus widgetStatus) {
		try (PrintWriter out = new PrintWriter(OUTPUT_FILE)) {
			out.println("Processing Results:");
			out.println(widgetStatus.toString());
			out.print("String Widgets Produced: ");
			out.println(widgetStatus.getWidgetCountSummary(ConnectionType.PRODUCER, String.class.getSimpleName())
					.toString());
			out.print("String Widgets Consumed: ");
			out.println(widgetStatus.getWidgetCountSummary(ConnectionType.CONSUMER, String.class.getSimpleName())
					.toString());
			out.print("Long Widgets Produced: ");
			out.println(
					widgetStatus.getWidgetCountSummary(ConnectionType.PRODUCER, Long.class.getSimpleName()).toString());
			out.print("Long Widgets Consumed: ");
			out.println(
					widgetStatus.getWidgetCountSummary(ConnectionType.CONSUMER, Long.class.getSimpleName()).toString());
		} catch (FileNotFoundException e) {
			logger.error("Error writing output", e);
		}
	}

}
