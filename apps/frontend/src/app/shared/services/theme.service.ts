import { DOCUMENT } from '@angular/common';
import { effect, inject, Service, signal } from '@angular/core';

export type Theme = 'dark' | 'light';

const STORAGE_KEY = 'theme';

@Service()
export class ThemeService {
  // DEPENDENCIES
  private readonly document = inject(DOCUMENT);

  // STATE
  private readonly themeState = signal<Theme>(this.readInitialTheme());
  readonly theme = this.themeState.asReadonly();

  constructor() {
    effect(() => {
      const theme = this.themeState();
      const root = this.document.documentElement;
      root.dataset['theme'] = theme;
      root.style.colorScheme = theme;
    });
  }

  // ACTIONS
  toggle(): void {
    const next: Theme = this.themeState() === 'dark' ? 'light' : 'dark';
    this.themeState.set(next);

    try {
      localStorage.setItem(STORAGE_KEY, next);
    } catch {
      // Keep the selected theme active when browser storage is unavailable.
    }
  }

  // HELPERS
  private readInitialTheme(): Theme {
    return this.document.documentElement.dataset['theme'] === 'light' ? 'light' : 'dark';
  }
}
