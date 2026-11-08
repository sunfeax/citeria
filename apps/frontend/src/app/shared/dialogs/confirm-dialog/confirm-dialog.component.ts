import { Component, inject } from '@angular/core';
import { MatButton } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';
import { Dialog } from '../../models/dialog';

@Component({
  selector: 'app-confirm-dialog',
  imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButton],
  templateUrl: './confirm-dialog.component.html',
  styleUrl: './confirm-dialog.component.scss',
})
export class ConfirmDialogComponent {
  /** INJECTORS */
  private readonly dialogRefService =
    inject<MatDialogRef<ConfirmDialogComponent, boolean>>(MatDialogRef);

  /** DATA */
  readonly data = inject<Dialog>(MAT_DIALOG_DATA);

  get confirmClass(): string {
    switch (this.data.variant) {
      case 'danger':
        return 'confirm__accept--danger';
      case 'warning':
        return 'confirm__accept--warning';
      default:
        return '';
    }
  }

  /** ACTIONS */
  onConfirm(): void {
    this.dialogRefService.close(true);
  }
  onCancel(): void {
    this.dialogRefService.close(false);
  }
}
