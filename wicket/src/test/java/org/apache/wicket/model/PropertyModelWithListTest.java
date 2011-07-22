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
package org.apache.wicket.model;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class PropertyModelWithListTest extends TestCase
{
	public static class BeansContainer
	{
		private List<Bean> beans = new ArrayList<Bean>();

		public List<Bean> getBeans()
		{
			return beans;
		}

		public void setBeans(List<Bean> beans)
		{
			this.beans = beans;
		}
	}

	public static class Bean
	{
		private String text;

		public String getText()
		{
			return text;
		}

		public void setText(String text)
		{
			this.text = text;
		}
	}

	public void testListPropertyModel() throws Exception
	{
		List<Bean> beans = new ArrayList<PropertyModelWithListTest.Bean>();
		Bean bean = new Bean();
		bean.setText("Wrinkly and green I am.");
		beans.add(bean);
		PropertyModel<String> model = new PropertyModel<String>(beans, "[0].text");
		assertEquals("Wrinkly and green I am.", model.getObject());
	}

	public void testContainerPropertyModel() throws Exception
	{
		BeansContainer container = new BeansContainer();
		Bean bean = new Bean();
		bean.setText("Wrinkly and green I am.");
		container.getBeans().add(bean);
		PropertyModel<String> model = new PropertyModel<String>(container, "beans[0].text");
		assertEquals("Wrinkly and green I am.", model.getObject());
	}
}
