package widget;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import internals.Widget;

import org.junit.Assert;

public class WidgetTest {

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(WidgetTest.class);

	@Test
	public void createAndRetrieveWidgetString() {
		String input = "Hello";
		Widget<String> widget = new Widget<>(input);
		String output = widget.get();
		Assert.assertTrue("Widget does not return the provided String", input.equals(output));
	}

	@Test
	public void createAndRetrieveWidgetLong() {
		Long input = 123L;
		Widget<Long> widget = new Widget<>(input);
		Long output = widget.get();
		Assert.assertTrue("Widget does not return the provided Long", input == output);
	}

}
