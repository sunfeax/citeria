import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { HeaderComponent } from '../header/header.component';
import { FooterComponent } from '../footer/footer.component';
import { SessionStore } from '../../../features/auth/session.store';

@Component({
  selector: 'app-sidebar-layout',
  imports: [RouterOutlet, SidebarComponent, HeaderComponent, FooterComponent],
  templateUrl: './sidebar-layout.component.html',
  styleUrl: './sidebar-layout.component.scss',
})
export class SidebarLayoutComponent {
  protected readonly sessionStore = inject(SessionStore);
}
