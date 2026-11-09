import { BrandComponent } from '../../components/brand/brand.component';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { routePaths } from '../../util/route-paths';

@Component({
  selector: 'app-footer',
  imports: [BrandComponent, RouterLink],
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.scss',
})
export class FooterComponent {
  protected readonly year = new Date().getFullYear();
  protected readonly routePaths = routePaths;
}
