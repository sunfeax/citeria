import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, linkedSignal, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatButton } from '@angular/material/button';
import { MatOption } from '@angular/material/core';
import {
  MatDatepickerToggle,
  MatDateRangeInput,
  MatDateRangePicker,
  MatEndDate,
  MatStartDate,
} from '@angular/material/datepicker';
import { MatFormField, MatSuffix } from '@angular/material/form-field';
import { MatIcon } from '@angular/material/icon';
import { MatInput } from '@angular/material/input';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatProgressBar } from '@angular/material/progress-bar';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatSelect } from '@angular/material/select';
import { RouterLink } from '@angular/router';
import { Observable, finalize } from 'rxjs';
import { ApiError } from '../../../../shared/models/api-error';
import { PageableContent } from '../../../../shared/models/pageable';
import { DialogService } from '../../../../shared/services/dialog.service';
import { SnackbarService } from '../../../../shared/services/snackbar.service';
import { provideAppDateAdapter } from '../../../../shared/util/app-date-adapter';
import { routePaths } from '../../../../shared/util/route-paths';
import { SessionStore } from '../../../auth/session.store';
import {
  Appointment,
  AppointmentFilters,
  AppointmentStatus,
} from '../../models/appointment';
import { AppointmentService } from '../../services/appointment.service';
import { BookingBadgeStore } from '../../services/booking-badge.store';

type AppointmentPage = PageableContent<Appointment>;
type ActionKey = 'accept' | 'reject' | 'pay' | 'cancel' | 'complete';
type StatusTone = 'warning' | 'info' | 'success' | 'neutral' | 'muted';

interface ActionView {
  key: ActionKey;
  label: string;
  appearance: 'filled' | 'outlined' | 'text';
}

const STATUS_VIEW: Record<AppointmentStatus, { label: string; tone: StatusTone }> = {
  [AppointmentStatus.PENDING]: { label: 'Pending', tone: 'warning' },
  [AppointmentStatus.AWAITING_PAYMENT]: { label: 'Awaiting payment', tone: 'info' },
  [AppointmentStatus.CONFIRMED]: { label: 'Confirmed', tone: 'success' },
  [AppointmentStatus.COMPLETED]: { label: 'Completed', tone: 'neutral' },
  [AppointmentStatus.CANCELLED]: { label: 'Cancelled', tone: 'muted' },
  [AppointmentStatus.REJECTED]: { label: 'Rejected', tone: 'muted' },
  [AppointmentStatus.EXPIRED]: { label: 'Expired', tone: 'muted' },
};

@Component({
  selector: 'app-bookings',
  imports: [
    CurrencyPipe,
    DatePipe,
    FormsModule,
    MatButton,
    MatFormField,
    MatDateRangeInput,
    MatDateRangePicker,
    MatDatepickerToggle,
    MatEndDate,
    MatStartDate,
    MatSuffix,
    MatIcon,
    MatInput,
    MatOption,
    MatPaginator,
    MatProgressBar,
    MatProgressSpinner,
    MatSelect,
    RouterLink,
  ],
  providers: [provideAppDateAdapter()],
  templateUrl: './bookings.component.html',
  styleUrl: './bookings.component.scss',
})
export class BookingsComponent {
  /** INJECTORS */
  private readonly appointmentService = inject(AppointmentService);
  private readonly bookingBadgeStore = inject(BookingBadgeStore);
  private readonly sessionStore = inject(SessionStore);
  private readonly dialogService = inject(DialogService);
  private readonly snackbarService = inject(SnackbarService);

  /** DATA */
  protected readonly routePaths = routePaths;
  protected readonly statusOptions = [
    { value: '', label: 'All statuses' },
    ...Object.values(AppointmentStatus).map(value => ({
      value,
      label: STATUS_VIEW[value].label,
    })),
  ];
  protected readonly sortOptions = [
    { value: 'startTime,desc', label: 'Latest first' },
    { value: 'startTime,asc', label: 'Earliest first' },
    { value: 'createdAt,desc', label: 'Recently booked' },
  ];

  readonly status = signal<AppointmentStatus | ''>('');
  readonly rangeStart = signal<Date | null>(null);
  readonly rangeEnd = signal<Date | null>(null);
  readonly sort = signal(this.sortOptions[0].value);
  readonly busyId = signal<string | null>(null);

  private readonly filters = computed<AppointmentFilters>(() => ({
    status: this.status(),
    from: this.rangeStart() ? this.startOfDay(this.rangeStart()!) : '',
    to: this.rangeStart() ? this.endOfDay(this.rangeEnd() ?? this.rangeStart()!) : '',
    sort: this.sort(),
  }));

  readonly hasFilters = computed(() => {
    const { status, from, to } = this.filters();
    return status !== '' || from !== '' || to !== '';
  });

  readonly page = linkedSignal({ source: this.filters, computation: () => 0 });
  readonly size = signal(10);
  readonly sizeOptions = [5, 10, 20, 50];

  private readonly appointmentsRes = rxResource({
    params: () => ({ page: this.page(), size: this.size(), filters: this.filters() }),
    stream: ({ params }) =>
      this.appointmentService.getList(params.page, params.size, params.filters),
  });

  private readonly lastPage = linkedSignal<
    AppointmentPage | undefined,
    AppointmentPage | undefined
  >({
    source: () =>
      this.appointmentsRes.hasValue() ? this.appointmentsRes.value() : undefined,
    computation: (value, previous) => value ?? previous?.value,
  });

  readonly isLoading = computed(() => this.appointmentsRes.isLoading());
  readonly loadFailed = computed(() => this.appointmentsRes.status() === 'error');
  readonly hasLoaded = computed(() => this.lastPage() !== undefined);
  readonly appointments = computed(() => this.lastPage()?.content ?? []);
  readonly totalElements = computed(() => this.lastPage()?.totalElements ?? 0);

  /** ACTIONS */
  onPage(event: PageEvent): void {
    this.page.set(event.pageIndex);
    this.size.set(event.pageSize);
  }

  retry(): void {
    this.appointmentsRes.reload();
  }

  clearFilters(): void {
    this.status.set('');
    this.rangeStart.set(null);
    this.rangeEnd.set(null);
  }

  statusView(appointment: Appointment): { label: string; tone: StatusTone } {
    return STATUS_VIEW[appointment.status];
  }

  counterparty(appointment: Appointment): string {
    return this.isSpecialistSide(appointment)
      ? appointment.clientName
      : appointment.specialistName;
  }

  counterpartyRole(appointment: Appointment): string {
    return this.isSpecialistSide(appointment) ? 'Client' : 'Specialist';
  }

  actionsFor(appointment: Appointment): ActionView[] {
    const { status } = appointment;
    if (this.isSpecialistSide(appointment)) {
      switch (status) {
        case AppointmentStatus.PENDING:
          return [
            { key: 'accept', label: 'Accept', appearance: 'filled' },
            { key: 'reject', label: 'Decline', appearance: 'outlined' },
          ];
        case AppointmentStatus.AWAITING_PAYMENT:
          return [{ key: 'cancel', label: 'Cancel', appearance: 'outlined' }];
        case AppointmentStatus.CONFIRMED:
          return [
            { key: 'complete', label: 'Mark as done', appearance: 'filled' },
            { key: 'cancel', label: 'Cancel', appearance: 'outlined' },
          ];
        default:
          return [];
      }
    }
    if (this.isClientSide(appointment)) {
      switch (status) {
        case AppointmentStatus.PENDING:
          return [{ key: 'cancel', label: 'Cancel request', appearance: 'outlined' }];
        case AppointmentStatus.AWAITING_PAYMENT:
          return [
            { key: 'pay', label: 'Pay now', appearance: 'filled' },
            { key: 'cancel', label: 'Cancel', appearance: 'outlined' },
          ];
        case AppointmentStatus.CONFIRMED:
          return [{ key: 'cancel', label: 'Cancel', appearance: 'outlined' }];
        default:
          return [];
      }
    }
    return [];
  }

  run(appointment: Appointment, action: ActionKey): void {
    switch (action) {
      case 'accept':
        this.perform(
          appointment,
          this.appointmentService.accept(appointment.id),
          'Request accepted.',
        );
        return;
      case 'complete':
        this.perform(
          appointment,
          this.appointmentService.complete(appointment.id),
          'Visit marked as completed.',
        );
        return;
      case 'reject':
        this.confirmThenPerform(
          appointment,
          {
            title: 'Decline this request',
            message: `${appointment.clientName} will be notified that the request was declined.`,
            confirmText: 'Decline',
            variant: 'danger',
          },
          () => this.appointmentService.reject(appointment.id),
          'Request declined.',
        );
        return;
      case 'cancel':
        this.confirmThenPerform(
          appointment,
          {
            title: 'Cancel this booking',
            message: 'The time slot will be released. This cannot be undone.',
            confirmText: 'Cancel booking',
            cancelText: 'Keep it',
            variant: 'danger',
          },
          () => this.appointmentService.cancel(appointment.id),
          'Booking cancelled.',
        );
        return;
      case 'pay':
        this.confirmThenPerform(
          appointment,
          {
            title: 'Pay for this visit',
            message: `€${appointment.priceAmount.toFixed(2)} will be charged to confirm ${appointment.offeringName}. This is a demo payment, no card is charged.`,
            confirmText: 'Pay',
            variant: 'info',
          },
          () => this.appointmentService.pay(appointment.id),
          'Payment received. Your visit is confirmed.',
        );
        return;
    }
  }

  /** HELPERS */
  private isSpecialistSide(appointment: Appointment): boolean {
    return this.sessionStore.user()?.id === appointment.specialistId;
  }

  private isClientSide(appointment: Appointment): boolean {
    return this.sessionStore.user()?.id === appointment.clientId;
  }

  private startOfDay(date: Date): string {
    return new Date(date.getFullYear(), date.getMonth(), date.getDate()).toISOString();
  }

  private endOfDay(date: Date): string {
    return new Date(
      date.getFullYear(),
      date.getMonth(),
      date.getDate() + 1,
    ).toISOString();
  }

  private confirmThenPerform(
    appointment: Appointment,
    dialog: Parameters<DialogService['confirm']>[0],
    request: () => Observable<Appointment>,
    successMessage: string,
  ): void {
    this.dialogService.confirm(dialog).subscribe(() => {
      this.perform(appointment, request(), successMessage);
    });
  }

  private perform(
    appointment: Appointment,
    request: Observable<Appointment>,
    successMessage: string,
  ): void {
    this.busyId.set(appointment.id);
    request.pipe(finalize(() => this.busyId.set(null))).subscribe({
      next: () => {
        this.snackbarService.success(successMessage);
        this.appointmentsRes.reload();
        this.bookingBadgeStore.refresh();
      },
      error: (err: HttpErrorResponse) => {
        const apiError = err.error as ApiError | null;
        this.snackbarService.error(apiError?.detail ?? 'Unable to update the booking.');
        this.appointmentsRes.reload();
      },
    });
  }
}
