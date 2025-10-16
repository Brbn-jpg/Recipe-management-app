import { Component, OnInit } from '@angular/core';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [TranslateModule],
  templateUrl: './contact.component.html',
  styleUrl: './contact.component.css',
})
export class ContactComponent implements OnInit {
  ngOnInit() {
    window.scrollTo({ top: 0 });
  }
  constructor(private translate: TranslateService) {}

  changeLanguage(language: string): void {
    this.translate.use(language);
  }
}
