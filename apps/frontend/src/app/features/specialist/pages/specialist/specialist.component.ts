import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  linkedSignal,
} from '@angular/core';
import { rxResource, toSignal } from '@angular/core/rxjs-interop';
import { MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatProgressBar } from '@angular/material/progress-bar';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { map } from 'rxjs';
import { avatarUrl } from '../../../../shared/util/avatar-url';
import { routePaths } from '../../../../shared/util/route-paths';
import { BookingPanelComponent } from '../../components/booking-panel/booking-panel.component';
import { DayOfWeek } from '../../models/specialist-detail';
import { SpecialistService } from '../../services/specialist.service';

const WEEK: { day: DayOfWeek; label: string }[] = [
  { day: 'MONDAY', label: 'Monday' },
  { day: 'TUESDAY', label: 'Tuesday' },
  { day: 'WEDNESDAY', label: 'Wednesday' },
  { day: 'THURSDAY', label: 'Thursday' },
  { day: 'FRIDAY', label: 'Friday' },
  { day: 'SATURDAY', label: 'Saturday' },
  { day: 'SUNDAY', label: 'Sunday' },
];

@Component({
  selector: 'app-specialist',
  imports: [RouterLink, MatButton, MatIcon, MatProgressBar, BookingPanelComponent],
  templateUrl: './specialist.component.html',
  styleUrl: './specialist.component.scss',
})
export class SpecialistComponent {
  /** INJECTORS */
  private readonly route = inject(ActivatedRoute);
  private readonly specialistService = inject(SpecialistService);
  private readonly title = inject(Title);

  /** TEMPLATE HELPERS */
  protected readonly routePaths = routePaths;

  /** DATA */
  readonly id = toSignal(
    this.route.paramMap.pipe(map(params => params.get('id') ?? '')),
    {
      initialValue: this.route.snapshot.paramMap.get('id') ?? '',
    },
  );
  private readonly query = this.route.snapshot.queryParamMap;
  readonly initialStart = this.query.get('start');

  readonly detail = rxResource({
    params: () => this.id(),
    stream: ({ params }) => this.specialistService.getDetail(params),
  });
  readonly specialist = computed(() =>
    this.detail.hasValue() ? this.detail.value() : null,
  );
  readonly isNotFound = computed(() => {
    const error = this.detail.error();
    return error instanceof HttpErrorResponse
      ? error.status === 404 || error.status === 400
      : false;
  });
  readonly fullName = computed(() => {
    const specialist = this.specialist();
    return specialist ? `${specialist.firstName} ${specialist.lastName}` : '';
  });
  readonly avatarSrc = computed(() => {
    const specialist = this.specialist();
    return specialist ? avatarUrl(specialist.id, specialist.avatarVersion) : null;
  });
  readonly schedule = computed(() => {
    const hours = this.specialist()?.workingHours ?? [];
    return WEEK.map(({ day, label }) => ({
      label,
      windows: hours
        .filter(item => item.dayOfWeek === day)
        .sort((a, b) => a.startTime.localeCompare(b.startTime))
        .map(item => `${item.startTime.slice(0, 5)} – ${item.endTime.slice(0, 5)}`),
    }));
  });

  /** STATE */
  readonly selectedOfferingId = linkedSignal<string | null>(() => {
    const offerings = this.specialist()?.offerings ?? [];
    const requested = this.query.get('offering');
    return (offerings.find(item => item.id === requested) ?? offerings[0])?.id ?? null;
  });

  constructor() {
    const defaultTitle = this.title.getTitle();
    inject(DestroyRef).onDestroy(() => this.title.setTitle(defaultTitle));
    effect(() => {
      const name = this.fullName();
      this.title.setTitle(name ? `${name} — Citeria` : 'Specialist — Citeria');
    });
  }

  /** ACTIONS */
  chooseOffering(id: string, panel: HTMLElement): void {
    this.selectedOfferingId.set(id);
    if (panel.getBoundingClientRect().top > window.innerHeight) {
      panel.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }
}
