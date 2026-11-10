import { Component } from '@angular/core';
import {
  MatAccordion,
  MatExpansionPanel,
  MatExpansionPanelHeader,
  MatExpansionPanelTitle,
} from '@angular/material/expansion';
import { MatIcon } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { routePaths } from '../../../../shared/util/route-paths';
import { POPULAR_FAQ } from '../../../help/data/faq';

@Component({
  selector: 'app-home-faq',
  imports: [
    RouterLink,
    MatAccordion,
    MatExpansionPanel,
    MatExpansionPanelHeader,
    MatExpansionPanelTitle,
    MatIcon,
  ],
  templateUrl: './home-faq.component.html',
  styleUrl: './home-faq.component.scss',
})
export class HomeFaqComponent {
  protected readonly routePaths = routePaths;
  protected readonly faq = POPULAR_FAQ;
}
