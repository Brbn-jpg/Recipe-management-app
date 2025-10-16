import { Component, OnInit } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-cta-section',
  standalone: true,
  imports: [TranslateModule],
  templateUrl: './cta-section.component.html',
  styleUrl: './cta-section.component.css',
})
export class CtaSectionComponent implements OnInit {
  ngOnInit(): void {
    const hiddenElements = document.querySelectorAll(
      '.hidden'
    ) as NodeListOf<HTMLElement>;
    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('show');
        } else {
          entry.target.classList.remove('show');
        }
      });
    });
    hiddenElements.forEach((element: HTMLElement) => {
      observer.observe(element);
    });
  }
  constructor(private translate: TranslateService) {
    this.translate.setDefaultLang('en');
  }

  changeLanguage(language: string): void {
    this.translate.use(language);
  }
}
