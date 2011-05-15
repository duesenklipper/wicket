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
package org.apache.wicket.markup.html.form;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.WicketAjaxReference;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WicketEventReference;
import org.apache.wicket.util.template.PackagedTextTemplate;
import org.apache.wicket.util.template.TextTemplate;

/**
 * Abstract behavior that allows JavaScript snippets to be executed whenever a {@link CheckBox},
 * {@link Check} or {@link CheckBoxMultipleChoice} is
 * <ul>
 * <li>checked</li>
 * <li>unchecked</li>
 * <li>clicked (regardless of its current state)</li>
 * </ul>
 * 
 * Subclasses can implement one or more of the getOnCheckJavaScript, getOnUncheckJavaScript and
 * getOnClickJavaScript methods to provide JavaScript snippets to execute on the given event.
 * 
 * @author Carl-Eric Menzel <cmenzel@wicketbuch.de>
 */
public abstract class CheckBoxTrigger extends AbstractBehavior
{
	private static final long serialVersionUID = 1L;
	private Component target;

	@Override
	public void bind(Component component)
	{
		if (target != null)
		{
			throw new IllegalStateException(
				"this behavior can only be bound to exactly one component");
		}
		target = component;
		target.setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.renderJavascriptReference(WicketAjaxReference.INSTANCE);
		response.renderJavascriptReference(WicketEventReference.INSTANCE);

		// attach the onClick event to our target:
		TextTemplate template = new PackagedTextTemplate(CheckBoxSelector.class,
			"CheckBoxTrigger.onClick.template.js");
		Map<String, Object> variables = new HashMap<String, Object>();
		final String targetMarkupId = target.getMarkupId();

		// the id of the component this behavior is attached to
		variables.put("targetId", targetMarkupId);

		// the event JS snippets
		variables.put("onClick", getOnClickJavaScript(targetMarkupId));
		variables.put("onCheck", getOnCheckJavaScript(targetMarkupId));
		variables.put("onUncheck", getOnUncheckJavaScript(targetMarkupId));
		response.renderOnLoadJavascript(template.asString(variables));
	}

	/**
	 * @param targetMarkupId
	 *            the ID of the component this behavior is attached to
	 * @return a JS snippet
	 */
	protected String getOnClickJavaScript(String targetMarkupId)
	{
		return "";
	}

	/**
	 * @param targetMarkupId
	 *            the ID of the component this behavior is attached to
	 * @return a JS snippet
	 */
	protected String getOnCheckJavaScript(String targetMarkupId)
	{
		return "";
	}

	/**
	 * @param targetMarkupId
	 *            the ID of the component this behavior is attached to
	 * @return a JS snippet
	 */
	protected String getOnUncheckJavaScript(String targetMarkupId)
	{
		return "";
	}
}
