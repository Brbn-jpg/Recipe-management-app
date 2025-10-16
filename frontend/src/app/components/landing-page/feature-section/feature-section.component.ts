import { Component } from '@angular/core';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-feature-section',
  standalone: true,
  imports: [TranslateModule],
  templateUrl: './feature-section.component.html',
  styleUrl: './feature-section.component.css',
})
export class FeatureSectionComponent {
  constructor(private translate: TranslateService) {
    this.translate.setDefaultLang('en');
  }

  changeLanguage(language: string): void {
    this.translate.use(language);
  }
}
