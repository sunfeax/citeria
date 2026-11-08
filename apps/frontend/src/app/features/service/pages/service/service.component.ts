import { Component, computed, inject, linkedSignal, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatOption } from '@angular/material/core';
import {
  MatFormField,
  MatLabel,
  MatPrefix,
  MatSuffix,
} from '@angular/material/form-field';
import { MatIcon } from '@angular/material/icon';
import { MatInput } from '@angular/material/input';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatProgressBar } from '@angular/material/progress-bar';
import { MatSelect } from '@angular/material/select';
import { MatSlider, MatSliderRangeThumb } from '@angular/material/slider';
import { PageableContent } from '../../../../shared/models/pageable';
import { debouncedSignal } from '../../../../shared/util/rxjs-helpers';
import { ServiceList, ServiceFilters } from '../../models/service-list';
import { ServiceService } from '../../services/service.service';

type ServicePage = PageableContent<ServiceList>;

@Component({
  selector: 'app-service',
  imports: [
    MatButton,
    MatIconButton,
    MatFormField,
    MatLabel,
    MatPrefix,
    MatSuffix,
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
  templateUrl: './service.component.html',
  styleUrl: './service.component.scss',
})
export class ServiceComponent {
  private readonly serviceService = inject(ServiceService);

  readonly search = signal('');
  readonly priceSlider = { min: 0, max: 500, step: 10 } as const;
  readonly minPrice = signal<number>(this.priceSlider.min);
  readonly maxPrice = signal<number>(this.priceSlider.max);
  readonly active = signal<boolean | null>(null);

  private readonly debouncedSearch = debouncedSignal(
    computed(() => this.search().trim()),
    300,
  );
  private readonly debouncedMinPrice = debouncedSignal(this.minPrice, 300);
  private readonly debouncedMaxPrice = debouncedSignal(this.maxPrice, 300);

  private readonly filters = computed<ServiceFilters>(() => {
    const min = this.debouncedMinPrice();
    const max = this.debouncedMaxPrice();
    return {
      search: this.debouncedSearch(),
      minPrice: min === this.priceSlider.min ? null : min,
      maxPrice: max === this.priceSlider.max ? null : max,
      active: this.active(),
    };
  });

  readonly hasFilters = computed(() => {
    const { search, minPrice, maxPrice, active } = this.filters();
    return search !== '' || minPrice !== null || maxPrice !== null || active !== null;
  });

  readonly page = linkedSignal({ source: this.filters, computation: () => 0 });
  readonly size = signal(20);
  readonly sizeOptions = [5, 10, 20, 50];

  private readonly servicesRes = rxResource({
    params: () => ({ page: this.page(), size: this.size(), filters: this.filters() }),
    stream: ({ params }) =>
      this.serviceService.getList(params.page, params.size, params.filters),
  });

  private readonly lastPage = linkedSignal<
    ServicePage | undefined,
    ServicePage | undefined
  >({
    source: () => (this.servicesRes.hasValue() ? this.servicesRes.value() : undefined),
    computation: (value, previous) => value ?? previous?.value,
  });

  readonly isLoading = computed(() => this.servicesRes.isLoading());
  readonly loadFailed = computed(() => this.servicesRes.status() === 'error');
  readonly hasLoaded = computed(() => this.lastPage() !== undefined);
  readonly services = computed(() => this.lastPage()?.content ?? []);
  readonly totalElements = computed(() => this.lastPage()?.totalElements ?? 0);
  readonly filtersExpanded = signal(false);

  readonly formatPrice = (value: number): string =>
    value === this.priceSlider.max ? `${value}+` : `${value}`;

  toggleFilters(): void {
    this.filtersExpanded.update(expanded => !expanded);
  }

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
    this.active.set(null);
  }
}
