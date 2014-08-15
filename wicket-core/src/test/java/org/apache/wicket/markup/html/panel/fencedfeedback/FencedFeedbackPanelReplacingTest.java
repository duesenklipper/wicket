package org.apache.wicket.markup.html.panel.fencedfeedback;

import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.WicketTesterScope;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by calle on 15/08/14.
 */
public class FencedFeedbackPanelReplacingTest
{
	@Rule
	public WicketTesterScope scope = new WicketTesterScope();

	public static class TestPage extends WebPage
	{
		private final FencedFeedbackPanel feedbackCatchall;
		private final TestPanel panel;

		public TestPage()
		{
			Form form = new Form("form");
			add(form);
			WebMarkupContainer top = new WebMarkupContainer("top");
			form.add(top);
			feedbackCatchall = new FencedFeedbackPanel("feedbackCatchall");
			top.add(feedbackCatchall);
			final WebMarkupContainer bottom = new WebMarkupContainer("bottom");
			form.add(bottom);
			panel = new TestPanel("panel");
			bottom.add(panel);
			form.add(new Link<Void>("forward")
			{
				@Override public void onClick()
				{
					bottom.replace(new EmptyPanel("panel"));
				}
			});
			form.add(new Link<Void>("backward")
			{
				@Override public void onClick()
				{
					bottom.replace(panel);
				}
			});
		}

		public static class TestPanel extends Panel
		{
			private final FencedFeedbackPanel panelFeedback;

			public TestPanel(String id)
			{
				super(id);
				panelFeedback = new FencedFeedbackPanel("panelFeedback", this);
				add(panelFeedback);
			}

			@Override protected void onConfigure()
			{
				super.onConfigure();
				this.info("info message");
			}
		}
	}

	@Test
	public void backandforth()
	{
		TestPage page = scope.getTester().startPage(new TestPage());
		assertFalse(page.feedbackCatchall.anyMessage());
		assertTrue(page.panel.panelFeedback.anyMessage());
		scope.getTester().clickLink("form:forward");
		assertFalse(page.feedbackCatchall.anyMessage());
		scope.getTester().clickLink("form:backward");
		assertFalse(page.feedbackCatchall.anyMessage());
		assertTrue(page.panel.panelFeedback.anyMessage());
		scope.getTester().debugComponentTrees();
		fail("foo");
	}
}
