import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../services/language.service';
import { AuthService } from '../../services/auth.service';

const mainUrl = 'http://localhost:4200/';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, TranslateModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css'],
})
export class NavbarComponent implements OnInit {
  params = {
    home: mainUrl + '/',
    about: mainUrl + '/about-us',
    contact: mainUrl + '/contact',
    login: mainUrl + '/login',
  };

  isLoggedIn = false;
  isAdmin = false;
  language: string = 'en';

  constructor(
    private translate: TranslateService,
    private languageService: LanguageService,
    private authService: AuthService
  ) {
    this.languageService.language$.subscribe((language) => {
      this.language = language;
    });
    this.translate.setDefaultLang(this.language);
  }

  ngOnInit(): void {
    this.authService.isLoggedIn$.subscribe((isLoggedIn) => {
      this.isLoggedIn = isLoggedIn;
      this.isAdmin = this.authService.isAdmin();
    });
  }

  changeLanguage(): void {
    const newLanguage = this.language === 'en' ? 'pl' : 'en';
    this.languageService.setLanguage(newLanguage);
    this.translate.use(newLanguage);
  }

  logOut(): void {
    this.authService.logout();
    this.isLoggedIn = false;
  }

  getFlagImage(): string {
    return this.language === 'en'
      ? 'images/flags/pl.svg'
      : 'images/flags/us.svg';
  }

  getAltText(): string {
    return this.language === 'en'
      ? 'Zmien język na polski'
      : 'Change language to English';
  }
}
