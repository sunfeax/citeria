import { Component, computed, inject, linkedSignal, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatButton } from '@angular/material/button';
import { MatOption } from '@angular/material/core';
import { MatFormField, MatPrefix } from '@angular/material/form-field';
import { MatIcon } from '@angular/material/icon';
import { MatInput } from '@angular/material/input';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatProgressBar } from '@angular/material/progress-bar';
import { MatSelect } from '@angular/material/select';
import { MatSlider, MatSliderRangeThumb } from '@angular/material/slider';
import { RouterLink } from '@angular/router';
import { PageableContent } from '../../../../shared/models/pageable';
import { avatarUrl } from '../../../../shared/util/avatar-url';
import { debouncedSignal } from '../../../../shared/util/rxjs-helpers';
import { OfferingList, OfferingFilters } from '../../models/offering-list';
import { UserRole } from '../../../auth/models/user-role';
import { SessionStore } from '../../../auth/session.store';
import { OfferingService } from '../../services/offering.service';
import { routePaths } from '../../../../shared/util/route-paths';

type OfferingPage = PageableContent<OfferingList>;

@Component({
  selector: 'app-offering',
  imports: [
    RouterLink,
    MatButton,
    MatFormField,
    MatPrefix,
    MatInput,
    MatIcon,
    MatSelect,
    MatOption,
    MatSlider,
    MatSliderRangeThumb,
    MatPaginator,
    MatProgressBar,
    FormsModule,
  ],
  templateUrl: './offering.component.html',
  styleUrl: './offering.component.scss',
})
export class OfferingComponent {
  private readonly offeringService = inject(OfferingService);
  private readonly sessionStore = inject(SessionStore);

  protected readonly avatarUrl = avatarUrl;
  protected readonly routePaths = routePaths;

  readonly search = signal('');
  readonly priceSlider = { min: 0, max: 500, step: 10 } as const;
  readonly minPrice = signal<number>(this.priceSlider.min);
  readonly maxPrice = signal<number>(this.priceSlider.max);
  readonly active = signal<boolean>(true);
  readonly isAdmin = computed(() => this.sessionStore.user()?.role === UserRole.ADMIN);
  readonly sortOptions = [
    { value: 'name,asc', label: 'Name A–Z' },
    { value: 'priceAmount,asc', label: 'Lowest price' },
    { value: 'priceAmount,desc', label: 'Highest price' },
    { value: 'createdAt,desc', label: 'Newest' },
  ];
  readonly sort = signal(this.sortOptions[0].value);

  private readonly debouncedSearch = debouncedSignal(
    computed(() => this.search().trim()),
    300,
  );
  private readonly debouncedMinPrice = debouncedSignal(this.minPrice, 300);
  private readonly debouncedMaxPrice = debouncedSignal(this.maxPrice, 300);

  private readonly filters = computed<OfferingFilters>(() => {
    const min = this.debouncedMinPrice();
    const max = this.debouncedMaxPrice();
    return {
      search: this.debouncedSearch(),
      minPrice: min === this.priceSlider.min ? null : min,
      maxPrice: max === this.priceSlider.max ? null : max,
      active: this.active(),
      sort: this.sort(),
    };
  });

  readonly hasFilters = computed(() => {
    const { search, minPrice, maxPrice, active } = this.filters();
    return search !== '' || minPrice !== null || maxPrice !== null || !active;
  });

  readonly page = linkedSignal({ source: this.filters, computation: () => 0 });
  readonly size = signal(20);
  readonly sizeOptions = [5, 10, 20, 50];

  private readonly servicesRes = rxResource({
    params: () => ({ page: this.page(), size: this.size(), filters: this.filters() }),
    stream: ({ params }) =>
      this.offeringService.getList(params.page, params.size, params.filters),
  });

  private readonly lastPage = linkedSignal<
    OfferingPage | undefined,
    OfferingPage | undefined
  >({
    source: () => (this.servicesRes.hasValue() ? this.servicesRes.value() : undefined),
    computation: (value, previous) => value ?? previous?.value,
  });

  readonly isLoading = computed(() => this.servicesRes.isLoading());
  readonly loadFailed = computed(() => this.servicesRes.status() === 'error');
  readonly hasLoaded = computed(() => this.lastPage() !== undefined);
  readonly offerings = computed(() => this.lastPage()?.content ?? []);
  readonly totalElements = computed(() => this.lastPage()?.totalElements ?? 0);

  readonly formatPrice = (value: number): string =>
    value === this.priceSlider.max ? `€${value}+` : `€${value}`;

  onPage(event: PageEvent): void {
    this.page.set(event.pageIndex);
    this.size.set(event.pageSize);
  }

  retry(): void {
    this.servicesRes.reload();
  }

  clearFilters(): void {
    this.search.set('');
    this.minPrice.set(this.priceSlider.min);
    this.maxPrice.set(this.priceSlider.max);
    this.active.set(true);
  }
}
