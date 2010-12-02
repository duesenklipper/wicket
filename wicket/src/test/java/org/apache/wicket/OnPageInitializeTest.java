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

import static org.junit.Assert.*;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;

public class OnPageInitializeTest
{
	public static abstract class SuperPage extends WebPage
	{
		private boolean subclassCallWorked = false;
		private int pageInitializeCount = 0;

		public SuperPage()
		{
			add(new Label("label", "label"));
		}

		@Override
		protected void onPageInitialize()
		{
			super.onPageInitialize();
			add(new Link<Void>("link")
			{
				@Override
				public void onClick()
				{
					; // just re-render page
				}
			});
			subclassCallWorked = getSomeInformationFromSubclasses();
			pageInitializeCount++;
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

	public static class BadInitializePage extends SubPage
	{
		@Override
		protected void onPageInitialize()
		{
			// not calling super.() to trigger check
		}
	}

	@Test
	public void testPageInitializeWorks() throws Exception
	{
		final WicketTester wicketTester = new WicketTester();
		SuperPage superPage = (SuperPage)wicketTester.startPage(SubPage.class);
		assertTrue(superPage.subclassCallWorked);
		assertEquals(1, superPage.pageInitializeCount);
	}

	@Test
	public void testPageInitializeIsCalledOnlyBeforeFirstRender() throws Exception
	{
		final WicketTester wicketTester = new WicketTester();
		SuperPage superPage = (SuperPage)wicketTester.startPage(SubPage.class);
		assertTrue(superPage.subclassCallWorked);
		assertEquals(1, superPage.pageInitializeCount);
		wicketTester.clickLink("link");
		superPage = (SuperPage)wicketTester.getLastRenderedPage();
		assertEquals(1, superPage.pageInitializeCount);
	}

	@Test(expected = IllegalStateException.class)
	public void testMissingSuperCallIsCaught() throws Exception
	{
		final WicketTester wicketTester = new WicketTester();
		SuperPage superPage = (SuperPage)wicketTester.startPage(BadInitializePage.class);
	}

}
