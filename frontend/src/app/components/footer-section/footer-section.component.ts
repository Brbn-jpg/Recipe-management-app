import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-footer-section',
  standalone: true,
  imports: [RouterLink, TranslateModule],
  templateUrl: './footer-section.component.html',
  styleUrl: './footer-section.component.css',
})
export class FooterSectionComponent {
  constructor(private translate: TranslateService) {
    this.translate.setDefaultLang('en');
  }

  changeLanguage(language: string): void {
    this.translate.use(language);
  }
}
