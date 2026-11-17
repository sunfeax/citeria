import { computed, inject, Service } from '@angular/core';
import { rxResource, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';
import { UserType } from '../../auth/models/user-type';
import { SessionStore } from '../../auth/session.store';
import { AppointmentStatus } from '../models/appointment';
import { AppointmentService } from './appointment.service';

@Service()
export class BookingBadgeStore {
  /** INJECTORS */
  private readonly appointmentService = inject(AppointmentService);
  private readonly sessionStore = inject(SessionStore);
  private readonly router = inject(Router);

  /** DATA */
  private readonly attentionStatus = computed(() => {
    const user = this.sessionStore.user();
    if (!user) {
      return undefined;
    }
    return user.type === UserType.SPECIALIST
      ? AppointmentStatus.PENDING
      : AppointmentStatus.AWAITING_PAYMENT;
  });

  private readonly countRes = rxResource({
    params: this.attentionStatus,
    stream: ({ params }) =>
      this.appointmentService.getList(0, 1, {
        status: params,
        from: '',
        to: '',
        sort: 'startTime,asc',
      }),
  });

  readonly attentionCount = computed(() =>
    this.attentionStatus() && this.countRes.hasValue()
      ? this.countRes.value().totalElements
      : 0,
  );

  constructor() {
    this.reloadOnNavigation();
  }

  /** ACTIONS */
  refresh(): void {
    this.countRes.reload();
  }

  /** EFFECTS */
  private reloadOnNavigation(): void {
    this.router.events
      .pipe(
        filter(event => event instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe(() => {
        if (this.attentionStatus()) {
          this.refresh();
        }
      });
  }
}
