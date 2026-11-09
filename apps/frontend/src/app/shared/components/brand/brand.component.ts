import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { routePaths } from '../../util/route-paths';

@Component({
  selector: 'app-brand',
  imports: [RouterLink],
  templateUrl: './brand.component.html',
  styleUrl: './brand.component.scss',
})
export class BrandComponent {
  readonly compact = input(false);
  protected readonly routePaths = routePaths;
}
