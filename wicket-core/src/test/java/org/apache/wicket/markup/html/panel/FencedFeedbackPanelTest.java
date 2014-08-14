/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.markup.html.panel;

import static org.junit.Assert.*;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.feedback.ErrorLevelFeedbackMessageFilter;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTesterScope;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests {@link FencedFeedbackPanel}
 *
 * @author igor
 */
public class FencedFeedbackPanelTest
{
	@Rule
	public WicketTesterScope scope = new WicketTesterScope();

	@Test
	public void fencing()
	{
		TestPage page = scope.getTester().startPage(TestPage.class);
		page.container1Input.error("error");

		// container messages should be visible to container feedbacks but not outside

		assertTrue(page.container1Feedback.anyMessage());
		assertTrue(page.container1Feedback2.anyMessage());
		assertFalse(page.formFeedback.anyMessage());
		assertFalse(page.externalFeedback.anyMessage());

		page = scope.getTester().startPage(TestPage.class);
		page.formInput.error("error");

		// form messages should be visible only to the form feedbacks

		assertFalse(page.container1Feedback.anyMessage());
		assertFalse(page.container1Feedback2.anyMessage());
		assertTrue(page.formFeedback.anyMessage());
		assertFalse(page.externalFeedback.anyMessage());

		page = scope.getTester().startPage(TestPage.class);
		page.externalLabel.error("error");

		// external messages should be picked up only by catch-all feedbacks

		assertFalse(page.container1Feedback.anyMessage());
		assertFalse(page.container1Feedback2.anyMessage());
		assertFalse(page.formFeedback.anyMessage());
		assertTrue(page.externalFeedback.anyMessage());

		page = scope.getTester().startPage(TestPage.class);
		page.getSession().error("error");

		// session scoped errors should only be picked up by catch-all feedbacks

		assertFalse(page.container1Feedback.anyMessage());
		assertFalse(page.container1Feedback2.anyMessage());
		assertFalse(page.formFeedback.anyMessage());
		assertTrue(page.externalFeedback.anyMessage());
	}

	@Test
	public void filtering()
	{
		TestPage page = scope.getTester().startPage(TestPage.class);

		// set a filter that will only allow errors or higher

		page.container1Feedback
				.setFilter(new ErrorLevelFeedbackMessageFilter(FeedbackMessage.ERROR));

		// report an info message - should be filtered out

		page.container1Input.info("info");

		// check info message was filtered out

		assertFalse(page.container1Feedback.anyMessage());
		assertTrue(page.container1Feedback2.anyMessage());

		// ensure filtered out messages dont leak

		assertFalse(page.formFeedback.anyMessage());
		assertFalse(page.externalFeedback.anyMessage());

		// same setup

		page = scope.getTester().startPage(TestPage.class);

		page.container1Feedback
				.setFilter(new ErrorLevelFeedbackMessageFilter(FeedbackMessage.ERROR));

		// but now with an error message that should not be filtered out

		page.container1Input.error("info");

		// check message was not filtered out

		assertTrue(page.container1Feedback.anyMessage());
		assertTrue(page.container1Feedback2.anyMessage());

		// and that it should not leak

		assertFalse(page.formFeedback.anyMessage());
		assertFalse(page.externalFeedback.anyMessage());

	}

	@Test
	public void moving()
	{
		TestPage page = scope.getTester().startPage(TestPage.class);
		page.container1Input.error("error");

		assertTrue(page.container1Feedback.anyMessage());
		assertTrue(page.container1Feedback2.anyMessage());

		// does not propagate out of container
		assertFalse(page.formFeedback.anyMessage());

		// remove one of two fencing feedback panels

		page = scope.getTester().startPage(TestPage.class);
		page.container1Feedback.remove();

		page.container1Input.error("error");

		assertTrue(page.container1Feedback2.anyMessage());

		// still does not propagate out of container because there is still a fencing panel
		assertFalse(page.formFeedback.anyMessage());

		// remove the last fencing feedback panel

		page = scope.getTester().startPage(TestPage.class);
		page.container1Feedback.remove();
		page.container1Feedback2.remove();

		page.container1Input.error("error");

		// now propagates out of container
		assertTrue(page.formFeedback.anyMessage());

	}

	/**
	 * 
	 */
	@Test
	public void replacingBackAndForthShouldNotBreakFencing()
	{
		TestPage page = scope.getTester().startPage(TestPage.class);
		page.container1Input.error("error");
		assertTrue(page.container1Feedback.anyMessage());
		assertFalse(page.formFeedback.anyMessage());
		scope.getTester().clickLink("forward");
		assertFalse(page.container2Feedback.anyMessage());
		scope.getTester().clickLink("backward");
		assertTrue(page.container1Feedback.anyMessage());
		assertFalse(page.formFeedback.anyMessage());
	}

	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		FencedFeedbackPanel externalFeedback, formFeedback, container1Feedback,
				container1Feedback2, container2Feedback, container2Feedback2;
		Component externalLabel, formInput, container1Input, container2Input;
		WebMarkupContainer container1, container2;

		public TestPage()
		{
			externalFeedback = new FencedFeedbackPanel("feedback");
			externalLabel = new Label("externalLabel");
			add(externalFeedback, externalLabel);

			Form<?> form = new Form<Void>("form");
			formFeedback = new FencedFeedbackPanel("formFeedback", form);
			form.add(formFeedback);
			formInput = new TextField<String>("formInput");
			form.add(formInput);
			container1 = new WebMarkupContainer("container");
			container1Input = new TextField<String>("containerInput");
			container1.add(container1Input);
			container1Feedback = new FencedFeedbackPanel("container1Feedback", container1);
			container1Feedback2 = new FencedFeedbackPanel("container1Feedback2", container1);
			container1.add(container1Feedback, container1Feedback2);
			container2 = new WebMarkupContainer("container");
			container2Input = new TextField<String>("containerInput");
			container2.add(container2Input);
			container2Feedback = new FencedFeedbackPanel("container1Feedback", container2);
			container2Feedback2 = new FencedFeedbackPanel("container1Feedback2", container2);
			container2.add(container2Feedback, container2Feedback2);
			form.add(container1);
			add(form);
			add(new Link<Void>("forward")
			{
				@Override public void onClick()
				{
					container1.replaceWith(container2);
				}
			});
			add(new Link<Void>("backward")
			{
				@Override public void onClick()
				{
					container2.replaceWith(container1);
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
				Class<?> containerClass)
		{
			return new StringResourceStream(//
							"<body>" + //
							"   <div wicket:id='feedback'/>" + //
							"   <div wicket:id='externalLabel'/>" + //
							"   <form wicket:id='form'>" + //
							"       <div wicket:id='formFeedback'/>" + //
							"       <input wicket:id='formInput' type='text'/>" + //
							"       <div wicket:id='container'>" + //
							"           <div wicket:id='container1Feedback'/>" + //
							"           <input wicket:id='containerInput' type='text'/>" + //
							"           <div wicket:id='container1Feedback2'/>" + //
							"       </div>" + //
							"    </form>" + //
							"    <a wicket:id='forward'></a>" + //
							"    <a wicket:id='backward'></a>" + //
							"</body>");
		}
	}
}
