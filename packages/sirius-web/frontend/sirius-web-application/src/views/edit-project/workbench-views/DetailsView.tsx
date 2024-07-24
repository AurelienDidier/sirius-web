/*******************************************************************************
 * Copyright (c) 2022, 2024 Obeo.
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
import { useSelection, WorkbenchViewComponentProps } from '@eclipse-sirius/sirius-components-core';
import { FormBasedView, FormContext } from '@eclipse-sirius/sirius-components-forms';
import Typography from '@material-ui/core/Typography';
import { makeStyles } from '@material-ui/core/styles';
import { useEffect, useState } from 'react';
import { DetailsViewState } from './DetailsView.types';
import { useDetailsViewSubscription } from './useDetailsViewSubscription';

const useDetailsViewStyles = makeStyles((theme) => ({
  idle: {
    padding: theme.spacing(1),
  },
}));

export const DetailsView = ({ editingContextId, readOnly }: WorkbenchViewComponentProps) => {
  const [state, setState] = useState<DetailsViewState>({
    currentSelection: { entries: [] },
  });

  const { selection } = useSelection();

  /**
   * Displays another form if the selection indicates that we should display another properties view.
   */
  const currentSelectionKey: string = state.currentSelection.entries
    .map((entry) => entry.id)
    .sort()
    .join(':');
  const newSelectionKey: string = selection.entries
    .map((entry) => entry.id)
    .sort()
    .join(':');
  useEffect(() => {
    if (selection.entries.length > 0 && currentSelectionKey !== newSelectionKey) {
      setState((prevState) => ({ ...prevState, currentSelection: selection }));
    } else if (selection.entries.length === 0) {
      setState((prevState) => ({ ...prevState, currentSelection: { entries: [] } }));
    }
  }, [currentSelectionKey, newSelectionKey]);

  const objectIds: string[] = state.currentSelection.entries.map((entry) => entry.id);
  const skip = objectIds.length === 0;
  const { form, payload } = useDetailsViewSubscription(editingContextId, objectIds, skip);

  const classes = useDetailsViewStyles();

  if (!form) {
    return (
      <div className={classes.idle}>
        <Typography variant="subtitle2">No object selected</Typography>
      </div>
    );
  }
  return (
    <div data-representation-kind="form-details">
      <FormContext.Provider
        value={{
          payload: payload,
        }}>
        <FormBasedView editingContextId={editingContextId} form={form} readOnly={readOnly} />
      </FormContext.Provider>
    </div>
  );
};
