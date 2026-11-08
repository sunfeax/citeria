import { inject, Service } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { filter, Observable } from 'rxjs';
import { ConfirmDialogComponent } from '../dialogs/confirm-dialog/confirm-dialog.component';
import { Dialog } from '../models/dialog';

@Service()
export class DialogService {
  /** INJECTORS */
  private readonly dialogService = inject(MatDialog);

  /** ACTIONS */
  confirm(data: Dialog): Observable<boolean> {
    return this.dialogService
      .open<ConfirmDialogComponent, Dialog, boolean>(ConfirmDialogComponent, {
        data,
        width: 'min(28rem, calc(100vw - 2rem))',
        autoFocus: 'dialog',
      })
      .afterClosed()
      .pipe(filter(confirmed => confirmed === true));
  }
}
