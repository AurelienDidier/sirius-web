/*******************************************************************************
 * Copyright (c) 2024 Obeo.
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
package org.eclipse.sirius.web.application.controllers.diagrams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.jayway.jsonpath.JsonPath;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.eclipse.sirius.components.collaborative.diagrams.dto.DiagramRefreshedEventPayload;
import org.eclipse.sirius.components.collaborative.diagrams.dto.EditLabelInput;
import org.eclipse.sirius.components.collaborative.diagrams.dto.EditLabelSuccessPayload;
import org.eclipse.sirius.components.collaborative.dto.CreateRepresentationInput;
import org.eclipse.sirius.components.diagrams.tests.graphql.EditLabelMutationRunner;
import org.eclipse.sirius.components.diagrams.tests.graphql.InitialDirectEditElementLabelQueryRunner;
import org.eclipse.sirius.components.diagrams.tests.navigation.DiagramNavigator;
import org.eclipse.sirius.web.AbstractIntegrationTests;
import org.eclipse.sirius.web.data.PapayaIdentifiers;
import org.eclipse.sirius.web.services.diagrams.EditableLabelDiagramDescriptionProvider;
import org.eclipse.sirius.web.tests.services.api.IGivenCreatedDiagramSubscription;
import org.eclipse.sirius.web.tests.services.api.IGivenInitialServerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * Integration tests for the edition of diagram elements label.
 *
 * @author pcdavid
 */
@Transactional
@SuppressWarnings("checkstyle:MultipleStringLiterals")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = { "sirius.web.test.enabled=studio" })
public class EditLabelDiagramControllerTests extends AbstractIntegrationTests {
    @Autowired
    private IGivenInitialServerState givenInitialServerState;

    @Autowired
    private IGivenCreatedDiagramSubscription givenCreatedDiagramSubscription;

    @Autowired
    private EditLabelMutationRunner editLabelMutationRunner;

    @Autowired
    private InitialDirectEditElementLabelQueryRunner initialDirectEditElementLabelQueryRunner;

    @Autowired
    private EditableLabelDiagramDescriptionProvider editableLabelDiagramDescriptionProvider;

    @BeforeEach
    public void beforeEach() {
        this.givenInitialServerState.initialize();
    }

    private Flux<DiagramRefreshedEventPayload> givenSubscriptionToLabelEditableDiagramDiagram() {
        var input = new CreateRepresentationInput(
                UUID.randomUUID(),
                PapayaIdentifiers.PAPAYA_PROJECT.toString(),
                this.editableLabelDiagramDescriptionProvider.getRepresentationDescriptionId(),
                PapayaIdentifiers.PROJECT_OBJECT.toString(),
                "EditableLabelDiagram"
        );
        return this.givenCreatedDiagramSubscription.createAndSubscribe(input);
    }

    @Test
    @DisplayName("Given a diagram with a node using a computed label, when its initial label is requested, then the raw label is returned")
    @Sql(scripts = {"/scripts/papaya.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"/scripts/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void givenDiagramWithNodeUsingComputedLabelWhenItsInitialLabelIsRequestedThenTheRawLabelIsReturned() {
        var flux = this.givenSubscriptionToLabelEditableDiagramDiagram();

        var diagramId = new AtomicReference<String>();
        var labelId = new AtomicReference<String>();

        Consumer<DiagramRefreshedEventPayload> initialDiagramContentConsumer = payload -> Optional.of(payload)
                .map(DiagramRefreshedEventPayload::diagram)
                .ifPresentOrElse(diagram -> {
                    diagramId.set(diagram.getId());

                    var node = new DiagramNavigator(diagram).nodeWithLabel("sirius-web-domain-suffix").getNode();
                    labelId.set(node.getInsideLabel().getId());
                }, () -> fail("Missing diagram"));

        Runnable requestInitialLabel = () -> {
            Map<String, Object> variables = Map.of(
                    "editingContextId", PapayaIdentifiers.PAPAYA_PROJECT.toString(),
                    "diagramId", diagramId.get(),
                    "labelId", labelId.get()
            );
            var initialDirectEditElementLabelResult = this.initialDirectEditElementLabelQueryRunner.run(variables);

            String initialDirectEditElementLabel = JsonPath.read(initialDirectEditElementLabelResult, "$.data.viewer.editingContext.representation.description.initialDirectEditElementLabel");
            assertThat(initialDirectEditElementLabel).isEqualTo("sirius-web-domain");
        };

        StepVerifier.create(flux)
                .consumeNextWith(initialDiagramContentConsumer)
                .then(requestInitialLabel)
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    @DisplayName("Given a diagram with a node, when its label is edited, then the label is updated")
    @Sql(scripts = {"/scripts/papaya.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"/scripts/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void givenDiagramWithNodeWhenItsLabelIsEditedThenTheLabelIsUpdated() {
        var flux = this.givenSubscriptionToLabelEditableDiagramDiagram();

        var diagramId = new AtomicReference<String>();
        var nodeId = new AtomicReference<String>();
        var labelId = new AtomicReference<String>();

        Consumer<DiagramRefreshedEventPayload> initialDiagramContentConsumer = payload -> Optional.of(payload)
                .map(DiagramRefreshedEventPayload::diagram)
                .ifPresentOrElse(diagram -> {
                    diagramId.set(diagram.getId());
                    var node = new DiagramNavigator(diagram).nodeWithLabel("sirius-web-domain-suffix").getNode();
                    nodeId.set(node.getId());
                    labelId.set(node.getInsideLabel().getId());
                }, () -> fail("Missing diagram"));

        Runnable editLabel = () -> {
            var input = new EditLabelInput(UUID.randomUUID(), PapayaIdentifiers.PAPAYA_PROJECT.toString(), diagramId.get(), labelId.get(), "new label");
            var invokeSingleClickOnDiagramElementToolResult = this.editLabelMutationRunner.run(input);

            String invokeSingleClickOnDiagramElementToolResultTypename = JsonPath.read(invokeSingleClickOnDiagramElementToolResult, "$.data.editLabel.__typename");
            assertThat(invokeSingleClickOnDiagramElementToolResultTypename).isEqualTo(EditLabelSuccessPayload.class.getSimpleName());
        };

        Predicate<DiagramRefreshedEventPayload> updatedDiagramContentMatcher = payload -> Optional.of(payload)
                .map(DiagramRefreshedEventPayload::diagram)
                .filter(diagram -> {
                    return diagram.getNodes().stream()
                        .filter(node -> node.getId().equals(nodeId.get()))
                        .map(node -> node.getInsideLabel().getText().equals("new label-suffix"))
                        .findFirst()
                        .orElse(false);
                })
                .isPresent();

        StepVerifier.create(flux)
                .consumeNextWith(initialDiagramContentConsumer)
                .then(editLabel)
                .expectNextMatches(updatedDiagramContentMatcher)
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }


}
