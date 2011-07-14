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

import org.apache.wicket.Component;
import org.apache.wicket.Component.IVisitor;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.AutoLabelForInputTagResolver.AutoLabel;
import org.apache.wicket.markup.html.internal.ResponseBufferZone;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.markup.parser.filter.WicketTagIdentifier;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Resolver that provides the <code>{@literal <wicket:label>}</code> tag, which will output a
 * FormComponent's {@link FormComponent#getLabel() label} without requiring a manual extra component
 * such as {@link Label} or {@link FormComponentLabel}.
 * 
 * <code>{@literal <wicket:label>}</code> can be used
 * <ul>
 * <li>together with <code>{@literal <label wicket:for="...">}</code>:
 * 
 * <pre>
 * {@literal
 * <label wicket:for="myFormComponent">some other markup, optionally<wicket:label/></label>
 * }
 * </pre>
 * 
 * </li>
 * <li>
 * standalone, with a <code>for</code> attribute:
 * 
 * <pre>
 * {@literal
 * <wicket:label for="myFormComponent"/>
 * }
 * </pre>
 * 
 * </li>
 * </ul>
 * <p>
 * It also supports both input and output:
 * <ul>
 * <li>If the FormComponent has a label model, the <code>{@literal <wicket:label>}</code> tag will
 * be replaced by the contents of that label.</li>
 * <li>If the FormComponent's label model is null, it can be picked up from
 * <code>{@literal <wicket:label>}</code>:
 * <ul>
 * <li><code>{@literal <wicket:label>}</code> can contain some raw markup, like this:
 * 
 * <pre>
 * {@literal
 * <wicket:label>I will become the component's label!</wicket:label>
 * }
 * </pre>
 * 
 * </li>
 * <li>Or it can be a message pulled from resources, similar to
 * <code>{@literal <wicket:message/>}</code>:
 * 
 * <pre>
 * {@literal
 * <wicket:label key="messagekey"/>
 * }
 * </pre>
 * 
 * </li>
 * </ul>
 * </li>
 * </ul>
 * 
 * 
 * @author Carl-Eric Menzel <cmenzel@wicketbuch.de>
 * @author igor
 */
public class AutoFormComponentLabelResolver implements IComponentResolver
{
	static
	{
		WicketTagIdentifier.registerWellKnownTagName("label");
	}

	/**
	 * This is inserted by the resolver to render the label.
	 */
	private static class FormLabel extends WebMarkupContainer
	{

		private final FormComponent<?> formComponent;

		public FormLabel(String id, FormComponent<?> formComponent)

		{
			super(id);
			this.formComponent = formComponent;
			setRenderBodyOnly(true);
		}

		@Override
		protected void onComponentTag(final ComponentTag tag)
		{
			if (tag.isOpenClose())
			{
				tag.setType(XmlTag.OPEN);
			}
			super.onComponentTag(tag);
		}

		@Override
		protected void onComponentTagBody(final MarkupStream markupStream,
			final ComponentTag openTag)
		{
			// try and find some form of label content...
			String labelData = null;
			// ...either in the form component...
			if (formComponent.getLabel() != null && formComponent.getLabel().getObject() != null)
			{
				labelData = formComponent.getLabel().getObject();
			}
			// ...or as a message key...
			else if (openTag.getAttribute("key") != null)
			{
				labelData = getString(openTag.getAttribute("key"));
			}
			else
			{
				// ...or as last resort the tag body...
				labelData = new ResponseBufferZone(RequestCycle.get(), markupStream)
				{
					@Override
					protected void executeInsideBufferedZone()
					{
						FormLabel.super.onComponentTagBody(markupStream, openTag);
					}
				}.execute().toString();
			}

			// ...no matter where it came from, we just want to print this label content...
			replaceComponentTagBody(markupStream, openTag, labelData);
			// ...and make sure it ends up in the FormComponent's label model
			IModel<String> labelModel;
			if (formComponent.getLabel() == null)
			{
				labelModel = new Model<String>();
				formComponent.setLabel(labelModel);
			}
			else
			{
				labelModel = formComponent.getLabel();
			}
			labelModel.setObject(labelData);
		}
	}

	public boolean resolve(MarkupContainer container, MarkupStream markupStream, ComponentTag tag)
	{
		if (tag instanceof WicketTag && "label".equals(((WicketTag)tag).getName()))
		{
			// We need to find a FormComponent...
			FormComponent<?> formComponent = null;
			// ...which could be explicitly specified...
			if (tag.getAttribute("for") != null)
			{
				Component component = findRelatedComponent(container, tag.getAttribute("for"));
				if (component instanceof FormComponent)
				{
					formComponent = (FormComponent<?>)component;
				}
			}
			if (formComponent == null)
			{
				// ...or available through an AutoLabel, either directly above us...
				if (container instanceof AutoLabel)
				{
					formComponent = ((AutoLabel)container).getFormComponent();
				}
				if (formComponent == null)
				{
					// ...or perhaps further up...
					AutoLabel autoLabel = container.findParent(AutoLabel.class);
					if (autoLabel != null)
					{
						formComponent = autoLabel.getFormComponent();
					}
				}
			}
			if (formComponent == null)
			{
				// ...or it might just not be available.
				throw new IllegalStateException("no form component found for <wicket:label>");
			}
			else
			{
				// ...found the form component, so we can add our label.
				container.autoAdd(new FormLabel("label" + container.getPage().getAutoIndex2(),
					formComponent), markupStream);
				return true;
			}
		}
		return false;
	}

	private Component findRelatedComponent(MarkupContainer container, final String id)
	{
		// try the quick and easy route first

		Component component = container.get(id);
		if (component != null && (component instanceof ILabelProvider))
		{
			return component;
		}

		// try the long way, search the hierarchy from the closest container up to the page

		final Component[] searched = new Component[] { null };
		while (container != null)
		{
			component = (Component)container.visitChildren(Component.class,
				new IVisitor<Component>()
				{
					public Object component(Component child)
					{
						if (child == searched[0])
						{
							// this container was already searched
							return CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
						}
						if (id.equals(child.getId()))
						{
							return child;
						}
						return CONTINUE_TRAVERSAL;
					}
				});

			if (component != null)
			{
				return component;
			}

			// remember the container so we dont search it again, and search the parent
			searched[0] = container;
			container = container.getParent();
		}

		return null;
	}
}
