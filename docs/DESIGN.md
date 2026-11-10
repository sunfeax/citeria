---
name: citeria
version: 0.1
tokens-source: apps/frontend/src/styles.scss
breakpoints-source: apps/frontend/src/styles/_breakpoints.scss
ui-library: "@angular/material ^22.2 (Material 3)"
icons: Material Symbols Outlined via <mat-icon>

themes:
  default: saved choice, else prefers-color-scheme, else dark (inline script in index.html)
  switch: data-theme on <html> via ThemeService
  dark: graphite
  light: green-gray (not white)

colors:
  primary: --color-primary              # filled buttons, active nav/tab, links on hover
  primary-hover: --color-primary-hover
  on-primary: --color-on-primary
  secondary: --color-secondary          # Material slider and button-toggle only
  background: --color-background
  surface: --color-surface              # cards, panels, header, sidebar
  surface-muted: --color-surface-muted  # hover on nav items, avatar placeholder
  text: --color-text
  text-soft: --color-text-soft          # ledes, secondary copy
  text-muted: --color-text-muted        # captions, hints, inactive nav
  border: --color-border                # control outlines
  border-soft: --color-border-soft      # dividers
  border-strong: --color-border-strong  # outline on hover
  focus-ring: --color-focus-ring
  status: --color-{success|danger|warning|info} + -soft
  danger-solid: --color-danger-solid / --color-on-solid  # destructive confirm button

booking-status:                         # provisional
  PENDING: warning
  AWAITING_PAYMENT: info
  CONFIRMED: success
  COMPLETED: neutral                    # surface-muted bg, text-soft
  CANCELLED / REJECTED / EXPIRED: neutral, text-muted

typography:
  font-family: --font-family-sans       # Inter 400/500/600/700
  scale:                                # role: size / weight / line-height / letter-spacing
    display: --font-size-display / 600 / --line-height-display / --letter-spacing-display  # home hero only
    headline: --font-size-headline / 600 / --line-height-display / --letter-spacing-display  # .page-title, marketing section titles
    work-page-title: --font-size-30 / 600 / --line-height-heading / --letter-spacing-heading  # provisional
    section-title: --font-size-20 / 600 / --line-height-heading / --letter-spacing-heading
    list-item-title: --font-size-16 / 600 / --line-height-normal   # provisional
    lede: --font-size-18 / 400 / --line-height-relaxed
    body: --font-size-16 / 400 / --line-height-normal
    ui: --font-size-14 / 500              # buttons, nav, labels, fields, selects
    caption: --font-size-12 / 400         # hints, footnotes, badges (500)
  weights: 400 text, 500 controls and nav, 600 headings, 700 brand name only

spacing: --space-{4,6,8,12,16,20,24,32,40,48,64,80,96}   # 6 only for icon-to-label gaps
radius: --radius-{8,12,24,full}
border-width: --border-width-{1,2,4}    # 1 outlines/dividers, 2 focus and active underline, 4 snackbar accent
shadow: --shadow-bar (header, sidebar), --shadow-overlay (snackbar, menu, dialog, floating demo card)
icon-size: --icon-size-{20,24,48,72}
breakpoints: { 600px, 960px, 1280px }   # bp.up() only, mobile-first

components:
  button-primary: { use: 'matButton="filled"', radius: full, height: 40px, large: .btn-lg 48px (auth forms) }
  button-secondary: { use: 'matButton="outlined"' }
  button-tertiary: { use: 'matButton="text"' }
  icon-button: { use: matIconButton, requires: aria-label }
  button-danger: { use: DialogService confirm, accept: --color-danger-solid }
  states: hover, focus-visible, disabled, busy (mat-progress-spinner inside the button, button disabled)
  input: { use: 'mat-form-field appearance outline', height: 48px, text: 14px, radius: 12 }
  filter-field: { height: 40px, radius: 24 }             # catalog filter bar
  card: { bg: surface, radius: 24, padding: --space-24, form-padding: --space-32, border: none, shadow: none }
  inset-panel: { radius: 12, padding: --space-24 }       # e.g. danger zone inside a card
  badge: { bg: status -soft, text: status color, radius: full, font: 12px / 500 }
  dialog: { use: DialogService + MatDialog, radius: 24 }
  snackbar: { use: SnackbarService, variants: success, error, warning, info }
  tabs: { active: --color-primary text + 2px inset underline }
  nav-header: guests and marketing pages
  nav-sidebar: { signed-in pages, width: 280px, collapsed: 80px, item: 48px high, radius full }
  loading: { list: mat-progress-bar 3px above content, stale content opacity 0.55 }
  empty-error-state: { card, padding: --space-48, centered icon + text + one action }
  table: { use: mat-table from 960px, card list below }  # provisional
---

## Rules
- Read every value from `tokens-source`; reference tokens by name, never by their value.
- Name new tokens by pixel value (`--space-28`) or by role when fluid (`--font-size-headline`).
- Build UI from Angular Material first; write a custom widget only when Material has no equivalent.
- Customize Material through `--mat-*` tokens; global overrides go in `styles.scss`, local ones on the component host class.
- One `filled` button per form or card; everything else is `outlined` or `text`.
- Destructive actions always go through `DialogService` confirmation.
- Show errors with `SnackbarService` unless the error belongs to a form field (`mat-error`).
- Status is never shown by color alone: badges carry text.
- Use `.page-title` / `.page-title--display` / `.page-lede` for page headings instead of re-declaring their styles.
- Put component dimensions (avatar, sidebar, grid tracks) in a local custom property, e.g. `--avatar-size`.
- Tints are `color-mix(in srgb, var(--color-x) N%, var(--color-surface))`, never a new hex.
- Reuse a pattern from another feature by extracting it to `shared/components/` on its second use.

## Don'ts
- No hex, rgb or named colors outside `tokens-source`.
- No px literals for spacing, font-size, radius, border, shadow or icon size in component SCSS.
- No `max-width` media queries; no breakpoint values other than those in `breakpoints`.
- No `::ng-deep`, `!important`, inline `style` for visuals, or `.mdc-*` overrides in components.
- No shadows on cards or static surfaces; gradients only as a photo scrim or a line pattern, never as a fill.
- No `--color-secondary` in custom components.
- No font weight 700 outside the brand name; no letter-spacing on text below 20px.
- No trailing period in headings.
- Do not edit this file; propose changes under "Предлагаю добавить в DESIGN.md".

## Responsive
- Base styles target phones (< 600px): single column, header nav wraps under the brand row.
- `bp.up(600px)`: two-column forms, inline header nav, page padding switches to `--page-gutter`.
- `bp.up(960px)`: split layouts (hero, auth, register photo), tables, horizontal danger zones.
- `bp.up(1280px)`: side column layouts (profile avatar column), three-column forms.
- Page container: `width: min(100%, var(--page-width))` with `--page-gutter` inline padding.
- Raw `@media` is allowed only for `hover`, `prefers-reduced-motion` and `max-height`.

## Accessibility
- Contrast: WCAG AA, 4.5:1 for text, 3:1 for controls and focus indicators, in both themes.
- Every custom interactive element gets `:focus-visible` with `outline: var(--focus-ring-width) solid var(--color-focus-ring)` and `outline-offset: var(--focus-ring-offset)`; use a negative offset inside clipped containers.
- Touch targets are at least 44×44px.
- Icon-only buttons need `aria-label`; decorative `mat-icon` gets `aria-hidden="true"`.
- Disable animations and transitions under `prefers-reduced-motion: reduce`.

## Code conventions
- Tokens: `apps/frontend/src/styles.scss` (`:root` for light, `:root[data-theme='dark']` overrides).
- Breakpoints: `@use 'breakpoints' as bp;` then `@include bp.up(960px) { ... }`.
- Global Material config and shared classes (`.btn-block`, `.btn-lg`, `.app-snack-bar--*`): `styles.scss`.
- Page-level shared styles: `apps/frontend/src/styles/_page.scss`, `auth-page.scss`.
- Shared UI: `shared/components/` (brand), `shared/dialogs/` (confirm-dialog), `shared/layout/` (header, footer, sidebar, layouts).
- Services: `DialogService`, `SnackbarService`, `ThemeService` in `shared/services/`.
- Component SCSS holds layout only: grid/flex, placement and token references.

## Open questions
- Work-page title (30px) and list-item title (16px) sizes are provisional until bookings and the specialist cabinet exist.
- Booking status colors are provisional.
- `mat-table` vs card list threshold (960px) is provisional.
- Unused color tokens: `--color-overlay`, `--color-sidebar`, `--color-text-disabled`, `--color-danger-hover`. Keep or remove.
