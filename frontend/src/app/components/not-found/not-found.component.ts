import { Component } from '@angular/core';
import { Location } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateService, TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterModule, TranslateModule],
  templateUrl: './not-found.component.html',
  styleUrl: './not-found.component.css',
})
export class NotFoundComponent {
  constructor(
    private translate: TranslateService,
    private location: Location
  ) {}

  changeLanguage(language: string): void {
    this.translate.use(language);
  }

  goBack(): void {
    this.location.back();
  }
}
