package org.apache.wicket.markup.html.form;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.WicketAjaxReference;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WicketEventReference;

public class CheckboxChoiceSelector extends LabeledWebMarkupContainer implements IHeaderContributor
{
	private final static HeaderContributor JS_CONTRIBUTOR = JavascriptPackageResource.getHeaderContribution(new ResourceReference(
		CheckboxChoiceSelector.class, "CheckboxChoiceSelector.js"));
	private final static HeaderContributor WICKET_AJAX = JavascriptPackageResource.getHeaderContribution(WicketAjaxReference.INSTANCE);
	private final static HeaderContributor WICKET_EVENT = JavascriptPackageResource.getHeaderContribution(WicketEventReference.INSTANCE);
	private final CheckBoxMultipleChoice<?> choiceComponent;
	private final boolean autoUpdate;

	public CheckboxChoiceSelector(String id, CheckBoxMultipleChoice<?> choiceComponent)
	{
		this(id, choiceComponent, true);
	}

	public CheckboxChoiceSelector(String id, CheckBoxMultipleChoice<?> choiceComponent,
		boolean autoUpdate)
	{
		super(id);
		this.choiceComponent = choiceComponent;
		this.autoUpdate = autoUpdate;
		choiceComponent.setOutputMarkupId(true);
		add(WICKET_EVENT);
		add(WICKET_AJAX);
		add(JS_CONTRIBUTOR);
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		tag.put("onclick",
			"CheckboxChoiceSelector.updateAllCheckboxes('" + choiceComponent.getMarkupId() + "', " +
				this.getMarkupId() + ".checked)");
	}

	public void renderHead(IHeaderResponse response)
	{
		if (autoUpdate)
		{
			response.renderOnLoadJavascript("CheckboxChoiceSelector.attachUpdateHandlers('" +
				choiceComponent.getMarkupId() + "', '" + this.getMarkupId() + "');");
		}
	}
}
