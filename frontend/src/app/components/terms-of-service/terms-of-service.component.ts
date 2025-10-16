import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-terms-of-service',
  standalone: true,
  imports: [RouterModule, TranslateModule],
  templateUrl: './terms-of-service.component.html',
  styleUrl: './terms-of-service.component.css',
})
export class TermsOfServiceComponent implements OnInit {
  ngOnInit() {
    window.scrollTo({ top: 0 });
  }
  constructor(private translate: TranslateService) {}

  changeLanguage(language: string): void {
    this.translate.use(language);
  }
}
