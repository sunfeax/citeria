import { Component, computed, inject, signal } from '@angular/core';
import { MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { exhaustMap } from 'rxjs';
import { SessionStore } from '../../../features/auth/session.store';
import { DialogService } from '../../services/dialog.service';
import { ThemeService } from '../../services/theme.service';
import { SIDEBAR_CONFIG } from '../../util/sidebar.config';
import { AuthService } from './../../../features/auth/services/auth.service';

@Component({
  selector: 'app-sidebar',
  imports: [MatIcon, MatIconButton, RouterLink],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
})
export class SidebarComponent {
  /** INJECTORS */
  private readonly authService = inject(AuthService);
  private readonly sessionStore = inject(SessionStore);
  private readonly dialogService = inject(DialogService);
  readonly themeService = inject(ThemeService);

  /** DATA */
  sidebarData = SIDEBAR_CONFIG;
  isExpandedSidebar = signal<boolean>(true);
  protected readonly displayName = computed(() => {
    const user = this.sessionStore.user();
    if (!user) return 'Guest';
    return user.firstName?.trim() || user.email;
  });

  toggleSidebar(): void {
    this.isExpandedSidebar.update(currentState => !currentState);
  }

  toggleTheme(): void {
    this.themeService.toggle();
  }

  logout(): void {
    this.dialogService
      .confirm({
        title: 'Logout',
        message: 'Do you want to sign out?',
        confirmText: 'Logout',
        variant: 'danger',
      })
      .pipe(exhaustMap(() => this.authService.logout()))
      .subscribe();
  }
}
