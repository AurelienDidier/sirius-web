/*******************************************************************************
 * Copyright (c) 2023, 2024 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.sirius.components.collaborative.forms.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.eclipse.sirius.components.collaborative.api.ChangeDescription;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.collaborative.forms.api.IFormQueryService;
import org.eclipse.sirius.components.collaborative.forms.dto.HelpTextRequestInput;
import org.eclipse.sirius.components.collaborative.forms.dto.HelpTextSuccessPayload;
import org.eclipse.sirius.components.collaborative.forms.messages.ICollaborativeFormMessageService;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.forms.AbstractWidget;
import org.eclipse.sirius.components.forms.Button;
import org.eclipse.sirius.components.forms.Form;
import org.eclipse.sirius.components.forms.Group;
import org.eclipse.sirius.components.forms.Page;
import org.eclipse.sirius.components.forms.Textfield;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.Success;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;
import reactor.core.publisher.Sinks.One;

/**
 * Unit tests of the help text event handler.
 *
 * @author pcdavid
 */
public class HelpTextRequestEventHandlerTests {
    private static final String FORM_ID = UUID.randomUUID().toString();

    @Test
    public void testHelpText() {
        Map<String, AbstractWidget> widgets = new HashMap<>();

        Function<String, IStatus> newValueHandler = newValue -> {
            return new Success();
        };

        AtomicBoolean textFieldHelpProviderCalled = new AtomicBoolean();
        Textfield textfield = Textfield.newTextfield("textfield.id")
                .label("label")
                .value("Previous value")
                .newValueHandler(newValueHandler)
                .helpTextProvider(() -> {
                    textFieldHelpProviderCalled.set(true);
                    return "text field help";
                })
                .diagnostics(List.of())
                .build();
        widgets.put(textfield.getId(), textfield);

        AtomicBoolean buttonHelpProviderCalled = new AtomicBoolean();
        Button button = Button.newButton("button.id")
                .label("button")
                .buttonLabel("button label")
                .pushButtonHandler(() -> new Success())
                .diagnostics(List.of())
                .helpTextProvider(() ->  {
                    buttonHelpProviderCalled.set(true);
                    return "button field help";
                })
                .build();
        widgets.put(button.getId(), button);

        Group group = Group.newGroup("groupId")
                .label("group label")
                .widgets(List.of(textfield, button))
                .build();

        Page page = Page.newPage("pageId")
                .label("page label")
                .groups(List.of(group))
                .build();

        Form form = Form.newForm(FORM_ID)
                .targetObjectId("targetObjectId")
                .descriptionId(UUID.randomUUID().toString())
                .pages(List.of(page))
                .build();

        IFormQueryService formQueryService = new IFormQueryService.NoOp() {
            @Override
            public Optional<AbstractWidget> findWidget(Form form, String widgetId) {
                return Optional.ofNullable(widgets.get(widgetId));
            }
        };
        HelpTextRequestEventHandler handler = new HelpTextRequestEventHandler(formQueryService, new ICollaborativeFormMessageService.NoOp(), new SimpleMeterRegistry());

        this.checkWidgetHelpText(handler, form, textfield.getId(), "text field help");
        assertThat(textFieldHelpProviderCalled.get()).isTrue();
        this.checkWidgetHelpText(handler, form, button.getId(), "button field help");
        assertThat(buttonHelpProviderCalled.get()).isTrue();
    }

    private void checkWidgetHelpText(HelpTextRequestEventHandler handler, Form form, String widgetId, String expectedHelpText) {
        var input = new HelpTextRequestInput(UUID.randomUUID(), UUID.randomUUID().toString(), FORM_ID, widgetId);

        assertThat(handler.canHandle(input)).isTrue();

        Many<ChangeDescription> changeDescriptionSink = Sinks.many().unicast().onBackpressureBuffer();
        One<IPayload> payloadSink = Sinks.one();

        handler.handle(payloadSink, changeDescriptionSink, new IEditingContext.NoOp(), form, input);

        ChangeDescription changeDescription = changeDescriptionSink.asFlux().blockFirst();
        assertThat(changeDescription.getKind()).isEqualTo(ChangeKind.NOTHING);

        IPayload payload = payloadSink.asMono().block();
        assertThat(payload).isInstanceOf(HelpTextSuccessPayload.class);
        var result = (HelpTextSuccessPayload) payload;
        assertThat(result.text()).isEqualTo(expectedHelpText);
    }

}
