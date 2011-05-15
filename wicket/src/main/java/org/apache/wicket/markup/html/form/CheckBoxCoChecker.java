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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.util.string.JavascriptUtils;
import org.apache.wicket.util.template.PackagedTextTemplate;
import org.apache.wicket.util.template.TextTemplate;

/**
 * A Javascript trigger that automatically checks other checkboxes when the checkbox it is attached
 * to is checked.
 * 
 * @author Carl-Eric Menzel <cmenzel@wicketbuch.de>
 */
public class CheckBoxCoChecker extends CheckBoxTrigger
{
	private static final long serialVersionUID = 1L;
	private final List<Component> checkBoxesToCheck;

	/**
	 * @param checkBoxesToCheck
	 *            The checkboxes to check when the main checkbox is checked.
	 */
	public CheckBoxCoChecker(Component... checkBoxesToCheck)
	{
		this.checkBoxesToCheck = Arrays.asList(checkBoxesToCheck);
	}

	@Override
	protected String getOnCheckJavaScript(String targetMarkupId)
	{
		TextTemplate template = new PackagedTextTemplate(CheckBoxSelector.class,
			"CheckBoxCoChecker.onCheck.template.js");
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("checkBoxIDs", JavascriptUtils.buildMarkupIdJSArrayLiteral(checkBoxesToCheck));
		return template.asString(variables);
	}
}
