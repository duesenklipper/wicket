package org.apache.wicket.markup.html.form;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.util.string.JavascriptUtils;
import org.apache.wicket.util.template.PackagedTextTemplate;
import org.apache.wicket.util.template.TextTemplate;

public class CheckBoxCoChecker extends CheckBoxTrigger
{
	private final List<Component> checkBoxesToCheck;

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
