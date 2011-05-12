package org.apache.wicket.markup.html.form;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.util.string.JavascriptUtils;
import org.apache.wicket.util.template.PackagedTextTemplate;
import org.apache.wicket.util.template.TextTemplate;

public class CheckBoxCoUnchecker extends CheckBoxTrigger
{
	private final List<Component> checkBoxesToUncheck;

	public CheckBoxCoUnchecker(Component... checkBoxesToUncheck)
	{
		this.checkBoxesToUncheck = Arrays.asList(checkBoxesToUncheck);
	}

	@Override
	protected String getOnUncheckJavaScript(String targetMarkupId)
	{
		TextTemplate template = new PackagedTextTemplate(CheckBoxSelector.class,
			"CheckBoxCoUnchecker.onUncheck.template.js");
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("checkBoxIDs",
			JavascriptUtils.buildMarkupIdJSArrayLiteral(checkBoxesToUncheck));
		return template.asString(variables);
	}
}
