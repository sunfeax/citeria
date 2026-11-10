import { Component } from '@angular/core';
import { MatButton } from '@angular/material/button';
import {
  MatAccordion,
  MatExpansionPanel,
  MatExpansionPanelHeader,
  MatExpansionPanelTitle,
} from '@angular/material/expansion';
import { MatIcon } from '@angular/material/icon';
import { FAQ_SECTIONS, SUPPORT_EMAIL } from '../../data/faq';

@Component({
  selector: 'app-help',
  imports: [
    MatAccordion,
    MatExpansionPanel,
    MatExpansionPanelHeader,
    MatExpansionPanelTitle,
    MatButton,
    MatIcon,
  ],
  templateUrl: './help.component.html',
  styleUrl: './help.component.scss',
})
export class HelpComponent {
  readonly supportEmail = SUPPORT_EMAIL;
  readonly sections = FAQ_SECTIONS;
}
