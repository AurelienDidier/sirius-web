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

import { NodeDragHandler } from 'reactflow';
import { GQLMessage } from '../Tool.types';

export interface UseDropNodeValue {
  onNodeDragStart: NodeDragHandler;
  onNodeDrag: NodeDragHandler;
  onNodeDragStop: NodeDragHandler;
}

export interface GQLDropNodePayload {
  __typename: string;
}

export interface GQLDropNodeData {
  dropNode: GQLDropNodePayload;
}

export interface GQLDropNodeVariables {
  input: GQLDropNodeInput;
}

export interface GQLDropNodeInput {
  id: string;
  editingContextId: string;
  representationId: string;
  droppedElementId: string;
  targetElementId: string | null;
  x: number;
  y: number;
}

export interface GQLErrorPayload extends GQLDropNodePayload {
  messages: GQLMessage[];
}

export interface GQLSuccessPayload extends GQLDropNodePayload {
  messages: GQLMessage[];
}
