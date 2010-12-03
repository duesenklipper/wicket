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
package org.apache.wicket;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.application.IComponentInitializationListener;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.tester.WicketTester;

/**
 * Tests {@link Component#onInitialize()} contract
 * 
 * @author igor
 */
public class ComponentInitializationTest extends WicketTestCase
{

	public static abstract class SuperPage extends WebPage
	{
		private boolean subclassCallWorked = false;
		private int pageInitializeCount = 0;
		final TestComponent outerContainer;
		TestComponent innerComponent;
		private TestComponent onInitializeContainer;
		private TestComponent onInitializeComponent;
		private TestComponent onBeforeRenderContainer;
		private TestComponent onBeforeRenderComponent;
		private final Link<Void> link;

		public SuperPage()
		{
			outerContainer = new TestComponent("outerContainer");
			add(outerContainer);
			innerComponent = new TestComponent("innerComponent");
			outerContainer.add(innerComponent);
			link = new Link<Void>("refreshLink")
			{
				@Override
				public void onClick()
				{
					; // just re-render page
				}
			};
			add(link);
		}

		@Override
		protected void onInitialize()
		{
			super.onInitialize();
			subclassCallWorked = getSomeInformationFromSubclasses();
			onInitializeContainer = new TestComponent("onInitializeContainer");
			onInitializeComponent = new TestComponent("onInitializeComponent");
			onInitializeContainer.add(onInitializeComponent);
			outerContainer.add(onInitializeContainer);
			pageInitializeCount++;
		}

		private boolean beforeRenderComponentsAdded = false;

		@Override
		protected void onBeforeRender()
		{
			super.onBeforeRender();
			assertTrue(getFlag(FLAG_INITIALIZED));
			if (!beforeRenderComponentsAdded)
			{
				onBeforeRenderContainer = new TestComponent("onBeforeRenderContainer");
				onBeforeRenderComponent = new TestComponent("onBeforeRenderComponent");
				onBeforeRenderContainer.add(onBeforeRenderComponent);
				onInitializeContainer.add(onBeforeRenderContainer);
				beforeRenderComponentsAdded = true;
			}
		}

		protected abstract boolean getSomeInformationFromSubclasses();
	}

	public static class SubPage extends SuperPage
	{
		private final boolean constructorHasBeenRun;

		public SubPage()
		{
			constructorHasBeenRun = true;
		}

		@Override
		protected boolean getSomeInformationFromSubclasses()
		{
			assertTrue(constructorHasBeenRun);
			return true;
		}

	}

	public static class BadPage extends SubPage
	{
		@Override
		protected void onInitialize()
		{
			super.onInitialize();
			innerComponent.replaceWith(new InvalidComponent("innerComponent"));
		}
	}

	public void testPageInitialization()
	{
		WicketTester tester = new WicketTester();
		tester.startPage(SubPage.class);
		SuperPage page = (SuperPage)tester.getLastRenderedPage();

		assertEquals(1, page.pageInitializeCount);
	}


	public void testInitListeners()
	{
		TestInitListener listener1 = new TestInitListener();
		TestInitListener listener2 = new TestInitListener();
		tester.getApplication().addComponentInitializationListener(listener1);
		tester.getApplication().addComponentInitializationListener(listener2);

		tester.startPage(SubPage.class);
		SuperPage page = (SuperPage)tester.getLastRenderedPage();

		assertTrue(listener1.getComponents().contains(page));
		assertTrue(listener1.getComponents().contains(page.outerContainer));
		assertTrue(listener1.getComponents().contains(page.onBeforeRenderComponent));
		assertTrue(listener1.getComponents().contains(page.onBeforeRenderContainer));
		assertTrue(listener1.getComponents().contains(page.innerComponent));
		assertTrue(listener1.getComponents().contains(page.onInitializeComponent));
		assertTrue(listener1.getComponents().contains(page.onInitializeContainer));
		assertTrue(listener2.getComponents().contains(page));
		assertTrue(listener2.getComponents().contains(page.outerContainer));
		assertTrue(listener2.getComponents().contains(page.onBeforeRenderComponent));
		assertTrue(listener2.getComponents().contains(page.onBeforeRenderContainer));
		assertTrue(listener2.getComponents().contains(page.innerComponent));
		assertTrue(listener2.getComponents().contains(page.onInitializeComponent));
		assertTrue(listener2.getComponents().contains(page.onInitializeContainer));
	}

	public void testInitializationOrder()
	{
		TestInitListener listener1 = new TestInitListener();
		tester.getApplication().addComponentInitializationListener(listener1);

		SuperPage page = (SuperPage)tester.startPage(SubPage.class);

		assertTrue(page == listener1.getComponents().get(0));
		assertTrue(page.outerContainer == listener1.getComponents().get(1));
		assertTrue(page.innerComponent == listener1.getComponents().get(2));
		assertTrue(page.onInitializeContainer == listener1.getComponents().get(3));
		assertTrue(page.onInitializeComponent == listener1.getComponents().get(4));
		// link should come after everything inside outerContainer
		assertTrue(page.link == listener1.getComponents().get(5));
		/*
		 * onBeforeRender components are added after the first onInitialize, so they should be
		 * initialized last, but in the correct order
		 */
		assertTrue(page.onBeforeRenderContainer == listener1.getComponents().get(6));
		assertTrue(page.onBeforeRenderComponent == listener1.getComponents().get(7));
	}

	public void testInitializeOnlyOnce()
	{
		SuperPage page = (SuperPage)tester.startPage(SubPage.class);
		assertEquals(1, page.pageInitializeCount);
		assertEquals(1, page.outerContainer.getCount());
		assertEquals(1, page.onBeforeRenderComponent.getCount());
		assertEquals(1, page.onBeforeRenderContainer.getCount());
		assertEquals(1, page.innerComponent.getCount());
		assertEquals(1, page.onInitializeComponent.getCount());
		assertEquals(1, page.onInitializeContainer.getCount());
	}

	public void testDontInitializeAgainAfterRedraw()
	{
		SuperPage page = (SuperPage)tester.startPage(SubPage.class);
		assertEquals(1, page.pageInitializeCount);
		assertEquals(1, page.outerContainer.getCount());
		assertEquals(1, page.onBeforeRenderComponent.getCount());
		assertEquals(1, page.onBeforeRenderContainer.getCount());
		assertEquals(1, page.innerComponent.getCount());
		assertEquals(1, page.onInitializeComponent.getCount());
		assertEquals(1, page.onInitializeContainer.getCount());
		tester.clickLink("refreshLink");
		page = (SuperPage)tester.getLastRenderedPage();
		assertEquals(1, page.pageInitializeCount);
		assertEquals(1, page.outerContainer.getCount());
		assertEquals(1, page.onBeforeRenderComponent.getCount());
		assertEquals(1, page.onBeforeRenderContainer.getCount());
		assertEquals(1, page.innerComponent.getCount());
		assertEquals(1, page.onInitializeComponent.getCount());
		assertEquals(1, page.onInitializeContainer.getCount());
	}

	public void testCatchBadInitializeMethod()
	{
		try
		{
			tester.startPage(BadPage.class);
			fail("should have failed");
		}
		catch (IllegalStateException e)
		{
			assertTrue(e.getMessage().contains("onInitialize"));
			assertTrue(e.getMessage().contains("InvalidComponent"));
		}
	}


	private static class TestComponent extends WebMarkupContainer
	{
		private int count = 0;

		public TestComponent(String id)
		{
			super(id);
		}

		@Override
		protected void onInitialize()
		{
			super.onInitialize();
			count++;
		}

		public int getCount()
		{
			return count;
		}


	}

	private static class InvalidComponent extends WebComponent
	{
		private final boolean initialized = false;

		public InvalidComponent(String id)
		{
			super(id);
		}

		@Override
		protected void onBeforeRender()
		{
			super.onBeforeRender();
			if (!initialized)
			{
				onInitialize();
			}
		}

		@Override
		protected void onInitialize()
		{
			// missing super call
		}
	}

	private static class TestInitListener implements IComponentInitializationListener
	{
		private final List<Component> components = new ArrayList<Component>();

		public void onInitialize(Component component)
		{
			System.out.println(component);
			components.add(component);
		}

		public List<Component> getComponents()
		{
			return components;
		}


	}
}
