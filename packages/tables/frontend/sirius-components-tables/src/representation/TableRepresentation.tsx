/*******************************************************************************
 * Copyright (c) 2024, 2025 CEA List.
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
import { RepresentationComponentProps } from '@eclipse-sirius/sirius-components-core';
import Typography from '@mui/material/Typography';
import { useState } from 'react';
import { makeStyles } from 'tss-react/mui';
import { TableContent } from '../table/TableContent';
import { ColumnFilter } from '../table/TableContent.types';
import { tableIdProvider } from './tableIdProvider';
import { TableRepresentationState } from './TableRepresentation.types';
import { useTableSubscription } from './useTableSubscription';

const useTableRepresentationStyles = makeStyles()((theme) => ({
  complete: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    paddingTop: theme.spacing(8),
  },
  representation: {
    overflowX: 'auto',
  },
}));

export const TableRepresentation = ({ editingContextId, representationId, readOnly }: RepresentationComponentProps) => {
  const { classes } = useTableRepresentationStyles();
  const [state, setState] = useState<TableRepresentationState>({
    cursor: null,
    direction: 'NEXT',
    size: 10,
    globalFilter: null,
    columnFilters: null,
  });

  const tableId = tableIdProvider(
    representationId,
    state.cursor,
    state.direction,
    state.size,
    state.globalFilter,
    state.columnFilters
  );
  const { complete, table } = useTableSubscription(editingContextId, tableId);

  const onPaginationChange = (cursor: string | null, direction: 'PREV' | 'NEXT', size: number) => {
    setState((prevState) => ({ ...prevState, cursor, direction, size }));
  };

  const onGlobalFilterChange = (globalFilter: string) => {
    setState((prevState) => ({ ...prevState, globalFilter }));
  };

  const onColumnFiltersChange = (columnFilters: ColumnFilter[]) => {
    setState((prevState) => ({ ...prevState, columnFilters }));
  };

  let completeMessage: JSX.Element | null = null;
  if (complete) {
    completeMessage = (
      <div className={classes.complete}>
        <Typography variant="h6" align="center">
          The table does not exist anymore
        </Typography>
      </div>
    );
  }

  return (
    <div data-testid={'table-representation'} className={classes.representation}>
      {table !== null && !complete ? (
        <TableContent
          editingContextId={editingContextId}
          representationId={tableId}
          table={table}
          readOnly={readOnly}
          onPaginationChange={onPaginationChange}
          onGlobalFilterChange={onGlobalFilterChange}
          onColumnFiltersChange={onColumnFiltersChange}
          enableColumnVisibility
          enableColumnResizing
          enableColumnFilters
          enableRowSizing
          enableGlobalFilter
          enablePagination
          enableColumnOrdering
        />
      ) : null}
      {completeMessage}
    </div>
  );
};
