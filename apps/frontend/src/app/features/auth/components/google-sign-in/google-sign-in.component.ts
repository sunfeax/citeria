import { HttpErrorResponse } from '@angular/common/http';
import {
  afterRenderEffect,
  booleanAttribute,
  Component,
  ElementRef,
  inject,
  input,
  signal,
  untracked,
  viewChild,
} from '@angular/core';
import { MatButton } from '@angular/material/button';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { ApiError } from '../../../../shared/models/api-error';
import { SnackbarService } from '../../../../shared/services/snackbar.service';
import { routePaths } from '../../../../shared/util/route-paths';
import { UserType } from '../../models/user-type';
import { AuthService } from '../../services/auth.service';
import { GoogleIdentityService } from '../../services/google-identity.service';

const MAX_BUTTON_WIDTH = 400;

@Component({
  selector: 'app-google-sign-in',
  imports: [MatButton, MatProgressSpinner],
  templateUrl: './google-sign-in.component.html',
  styleUrl: './google-sign-in.component.scss',
})
export class GoogleSignInComponent {
  /** INJECTORS */
  private readonly googleIdentityService = inject(GoogleIdentityService);
  private readonly authService = inject(AuthService);
  private readonly snackbarService = inject(SnackbarService);
  private readonly router = inject(Router);

  /** INPUTS */
  readonly type = input<UserType | null>(null);
  readonly large = input(false, { transform: booleanAttribute });

  /** STATE */
  readonly isLoading = signal(false);
  readonly loadFailed = signal(false);
  private readonly container = viewChild<ElementRef<HTMLElement>>('container');

  constructor() {
    afterRenderEffect(onCleanup => {
      const element = this.container()?.nativeElement;
      if (!element) {
        return;
      }

      const subscription = this.googleIdentityService.load().subscribe({
        next: accountsId => {
          accountsId.initialize({
            client_id: this.googleIdentityService.clientId,
            callback: response => this.signIn(response.credential),
          });
          element.replaceChildren();
          accountsId.renderButton(element, {
            size: 'large',
            text: 'continue_with',
            width: Math.min(MAX_BUTTON_WIDTH, element.clientWidth || MAX_BUTTON_WIDTH),
          });
        },
        error: () => untracked(() => this.loadFailed.set(true)),
      });

      onCleanup(() => subscription.unsubscribe());
    });
  }

  /** ACTIONS */
  private signIn(credential: string): void {
    this.isLoading.set(true);

    this.authService
      .loginWithGoogle({ credential, type: this.type() ?? undefined })
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: () => this.router.navigateByUrl(routePaths.profile),
        error: (err: HttpErrorResponse) => {
          const apiError = err.error as ApiError | null;
          const message =
            err.status === 401 && apiError?.detail
              ? apiError.detail
              : 'Unable to sign in with Google right now. Please try later.';
          this.snackbarService.error(message);
        },
      });
  }
}
