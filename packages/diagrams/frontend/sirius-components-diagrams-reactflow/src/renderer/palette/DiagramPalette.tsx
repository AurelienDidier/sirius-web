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

import { memo } from 'react';
import { DiagramPaletteProps } from './DiagramPalette.types';
import { Palette } from './Palette';
import { PalettePortal } from './PalettePortal';
import { useDiagramPalette } from './useDiagramPalette';

export const DiagramPalette = memo(({ diagramElementId }: DiagramPaletteProps) => {
  const { isOpened, x, y } = useDiagramPalette();
  return isOpened && x && y ? (
    <PalettePortal>
      <Palette x={x} y={y} diagramElementId={diagramElementId} onDirectEditClick={() => {}} />
    </PalettePortal>
  ) : null;
});
