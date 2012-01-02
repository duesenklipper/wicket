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
package org.apache.wicket.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

public class BehaviorExceptionHandlingTest extends WicketTestCase
{
	public static final class RedirectedPage extends WebPage
	{
		public final int id;

		public RedirectedPage(int id)
		{
			this.id = id;
		}
	}

	public static final class ExpectedException extends RuntimeException
	{

	}

	public static class StartPage extends WebPage
	{
		public StartPage(IModel<String> model, IBehavior... behaviors)
		{
			final Label label = new Label("label", model);
			add(label);
			for (IBehavior behavior : behaviors)
			{
				label.add(behavior);
			}
		}
	}

	private final IModel<String> explodingModel = new AbstractReadOnlyModel<String>()
	{
		@Override
		public String getObject()
		{
			throw new ExpectedException();
		}
	};

	public void testDefaultExceptionHandling() throws Exception
	{
		try
		{
			Page page = tester.startPage(new StartPage(explodingModel));
			fail("should have thrown exception");
		}
		catch (WicketRuntimeException e)
		{
			assertTrue(e.getCause() instanceof ExpectedException);
		}
		catch (Throwable t)
		{
			fail("should have thrown wicketruntimeex");
		}
	}

	private boolean behaviorCalled = false;

	public void testSingleBehaviorWithCleanupOnly() throws Exception
	{
		try
		{
			tester.startPage(new StartPage(explodingModel, new AbstractBehavior()
			{
				@Override
				public void onException(Component component, RuntimeException exception)
				{
					super.onException(component, exception);
					assertTrue(exception instanceof ExpectedException ||
						exception.getCause() instanceof ExpectedException);
					behaviorCalled = true;
				}
			}));
			fail("should have thrown exception");
		}
		catch (WicketRuntimeException e)
		{
			assertTrue(e.getCause() instanceof ExpectedException);
			assertTrue(behaviorCalled);
		}
		catch (Throwable t)
		{
			fail("should have thrown wicketruntimeex");
		}
	}

	public void testTwoBehaviorsWithCleanupOnly() throws Exception
	{
		try
		{
			tester.startPage(new StartPage(explodingModel, new AbstractBehavior()
			{
				@Override
				public void onException(Component component, RuntimeException exception)
				{
					super.onException(component, exception);
					assertTrue(exception instanceof ExpectedException ||
						exception.getCause() instanceof ExpectedException);
					behaviorCalled = true;
				}
			}, new AbstractBehavior()
			{
				@Override
				public void onException(Component component, RuntimeException exception)
				{
					super.onException(component, exception);
					assertTrue(exception instanceof ExpectedException ||
						exception.getCause() instanceof ExpectedException);
					behavior2called = true;
				}
			}));
			fail("should have thrown exception");
		}
		catch (WicketRuntimeException e)
		{
			assertTrue(e.getCause() instanceof ExpectedException);
			assertTrue(behaviorCalled);
			assertTrue(behavior2called);
		}
		catch (Throwable t)
		{
			fail("should have thrown wicketruntimeex");
		}
	}

	public void testSingleBehaviorWithRedirect() throws Exception
	{
		tester.startPage(new StartPage(explodingModel, new AbstractBehavior()
		{
			@Override
			public void onException(Component component, RuntimeException exception)
			{
				super.onException(component, exception);
				assertTrue(exception instanceof ExpectedException ||
					exception.getCause() instanceof ExpectedException);
				behaviorCalled = true;
				throw new RestartResponseException(new RedirectedPage(1));
			}
		}));
		tester.assertRenderedPage(RedirectedPage.class);
		assertTrue(behaviorCalled);
	}

	private boolean behavior2called;


	public void testSecondBehaviorIsCalledEvenWithRedirectInFirst() throws Exception
	{
		tester.startPage(new StartPage(explodingModel, new AbstractBehavior()
		{
			@Override
			public void onException(Component component, RuntimeException exception)
			{
				super.onException(component, exception);
				assertTrue(exception instanceof ExpectedException ||
					exception.getCause() instanceof ExpectedException);
				behaviorCalled = true;
				throw new RestartResponseException(new RedirectedPage(1));
			}
		}, new AbstractBehavior()
		{
			@Override
			public void onException(Component component, RuntimeException exception)
			{
				super.onException(component, exception);
				behavior2called = true;
			}
		}));
		assertTrue(behaviorCalled);
		assertTrue(behavior2called);
		tester.assertRenderedPage(RedirectedPage.class);
	}

	public void testSecondBehaviorRedirectWins() throws Exception
	{
		RedirectedPage page = (RedirectedPage)tester.startPage(new StartPage(explodingModel,
			new AbstractBehavior()
			{
				@Override
				public void onException(Component component, RuntimeException exception)
				{
					super.onException(component, exception);
					assertTrue(exception instanceof ExpectedException ||
						exception.getCause() instanceof ExpectedException);
					behaviorCalled = true;
					throw new RestartResponseException(new RedirectedPage(1));
				}
			}, new AbstractBehavior()
			{
				@Override
				public void onException(Component component, RuntimeException exception)
				{
					super.onException(component, exception);
					behavior2called = true;
					throw new RestartResponseException(new RedirectedPage(2));
				}
			}));
		assertTrue(behaviorCalled);
		assertTrue(behavior2called);
		assertEquals(2, page.id);
	}
}
