import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, linkedSignal, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButton } from '@angular/material/button';
import { MatButtonToggle, MatButtonToggleGroup } from '@angular/material/button-toggle';
import { MatIcon } from '@angular/material/icon';
import { MatProgressBar } from '@angular/material/progress-bar';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { ApiError } from '../../../../shared/models/api-error';
import { SnackbarService } from '../../../../shared/services/snackbar.service';
import { routePaths } from '../../../../shared/util/route-paths';
import {
  addDays,
  dateKeyInZone,
  formatDateKey,
  timeInZone,
} from '../../../../shared/util/zoned-date';
import { Appointment } from '../../../appointment/models/appointment';
import { AppointmentService } from '../../../appointment/services/appointment.service';
import { UserType } from '../../../auth/models/user-type';
import { SessionStore } from '../../../auth/session.store';
import { OfferingList } from '../../../offering/models/offering-list';
import { Slot } from '../../models/slot';
import { SpecialistService } from '../../services/specialist.service';

export const BOOKING_HORIZON_DAYS = 28;

interface BookingDay {
  key: string;
  weekday: string;
  day: string;
  label: string;
}

@Component({
  selector: 'app-booking-panel',
  imports: [
    MatButton,
    MatButtonToggle,
    MatButtonToggleGroup,
    MatIcon,
    MatProgressBar,
    MatProgressSpinner,
  ],
  templateUrl: './booking-panel.component.html',
  styleUrl: './booking-panel.component.scss',
})
export class BookingPanelComponent {
  /** INJECTORS */
  private readonly specialistService = inject(SpecialistService);
  private readonly appointmentService = inject(AppointmentService);
  private readonly sessionStore = inject(SessionStore);
  private readonly snackbarService = inject(SnackbarService);
  private readonly router = inject(Router);

  /** INPUTS */
  readonly specialistId = input.required<string>();
  readonly offerings = input.required<OfferingList[]>();
  readonly timeZone = input.required<string>();
  readonly initialStart = input<string | null>(null);
  readonly selectedOfferingId = input<string | null>(null);

  /** DATA */
  readonly viewer = computed<'guest' | 'client' | 'specialist'>(() => {
    const user = this.sessionStore.user();
    if (!user) {
      return 'guest';
    }
    return user.type === UserType.SPECIALIST ? 'specialist' : 'client';
  });
  readonly selectedOffering = computed(
    () => this.offerings().find(item => item.id === this.selectedOfferingId()) ?? null,
  );
  readonly days = computed<BookingDay[]>(() => {
    const firstDay = addDays(dateKeyInZone(new Date(), this.timeZone()), 1);
    return Array.from({ length: BOOKING_HORIZON_DAYS }, (_, index) => {
      const key = addDays(firstDay, index);
      return {
        key,
        weekday: formatDateKey(key, { weekday: 'short' }),
        day: formatDateKey(key, { day: 'numeric' }),
        label: formatDateKey(key, { weekday: 'long', day: 'numeric', month: 'long' }),
      };
    });
  });

  readonly daysRange = computed(() => {
    const days = this.days();
    const format = (key: string) =>
      formatDateKey(key, { month: 'short', day: 'numeric' });
    return `${format(days[0].key)} – ${format(days[days.length - 1].key)}`;
  });

  readonly slots = rxResource({
    params: () => {
      const offeringId = this.selectedOfferingId();
      const days = this.days();
      return offeringId && this.viewer() !== 'specialist'
        ? { offeringId, from: days[0].key, to: days[days.length - 1].key }
        : undefined;
    },
    stream: ({ params }) =>
      this.specialistService.getSlots(params.offeringId, params.from, params.to),
  });

  readonly slotsByDay = computed(() => {
    const byDay = new Map<string, Slot[]>();
    if (!this.slots.hasValue()) {
      return byDay;
    }
    for (const slot of this.slots.value()) {
      const key = dateKeyInZone(slot.startTime, this.timeZone());
      byDay.set(key, [...(byDay.get(key) ?? []), slot]);
    }
    return byDay;
  });

  /** STATE */
  private readonly pickedDay = signal<string | null>(null);
  private readonly pickedStart = linkedSignal<string | null, string | null>({
    source: () => this.selectedOfferingId(),
    computation: (_, previous) => (previous ? null : this.initialStart()),
  });
  readonly isSubmitting = signal(false);
  readonly createdAppointment = signal<Appointment | null>(null);

  readonly selectedDay = computed(() => {
    const byDay = this.slotsByDay();
    const initial = this.initialStart();
    const candidates = [
      this.pickedDay(),
      initial ? dateKeyInZone(initial, this.timeZone()) : null,
    ];
    return (
      candidates.find(key => key && byDay.has(key)) ?? byDay.keys().next().value ?? null
    );
  });
  readonly daySlots = computed(() => {
    const day = this.selectedDay();
    return day ? (this.slotsByDay().get(day) ?? []) : [];
  });
  readonly selectedSlot = computed(() => {
    const picked = this.pickedStart();
    if (!picked) {
      return null;
    }
    const pickedTime = new Date(picked).getTime();
    return (
      this.daySlots().find(slot => new Date(slot.startTime).getTime() === pickedTime) ??
      null
    );
  });
  readonly selectedDayLabel = computed(
    () => this.days().find(day => day.key === this.selectedDay())?.label ?? '',
  );

  /** TEMPLATE HELPERS */
  readonly formatTime = (instant: string) => timeInZone(instant, this.timeZone());

  /** ACTIONS */
  selectDay(key: string): void {
    this.pickedDay.set(key);
    this.pickedStart.set(null);
  }

  selectStart(start: string): void {
    this.pickedStart.set(start);
  }

  book(): void {
    const offering = this.selectedOffering();
    const slot = this.selectedSlot();
    if (!offering || !slot) {
      return;
    }

    if (this.viewer() === 'guest') {
      const returnUrl = this.router
        .createUrlTree([routePaths.specialists, this.specialistId()], {
          queryParams: { offering: offering.id, start: slot.startTime },
        })
        .toString();
      void this.router.navigate([routePaths.login], { queryParams: { returnUrl } });
      return;
    }

    this.isSubmitting.set(true);
    this.appointmentService
      .create({ offeringId: offering.id, startTime: slot.startTime })
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: appointment => this.createdAppointment.set(appointment),
        error: (err: HttpErrorResponse) => {
          const apiError = err.error as ApiError | null;
          this.snackbarService.error(
            Object.values(apiError?.errors ?? {})[0] ??
              apiError?.detail ??
              'Unable to send the booking request.',
          );
          this.pickedStart.set(null);
          this.slots.reload();
        },
      });
  }

  bookAnother(): void {
    this.createdAppointment.set(null);
    this.pickedStart.set(null);
    this.slots.reload();
  }

  formatAppointmentDate(appointment: Appointment): string {
    const key = dateKeyInZone(appointment.startTime, this.timeZone());
    return formatDateKey(key, { weekday: 'long', day: 'numeric', month: 'long' });
  }
}
