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
package org.eclipse.sirius.components.collaborative.trees.dto;

import java.util.UUID;

import org.eclipse.sirius.components.collaborative.trees.api.ITreeInput;

/**
 * The input for the invoke of a single click action of a tree item context menu.
 *
 * @author Jerome Gout
 */
public record InvokeSingleClickTreeItemContextMenuEntryInput(UUID id, String editingContextId, String representationId, String treeItemId, String menuEntryId) implements ITreeInput {
}
