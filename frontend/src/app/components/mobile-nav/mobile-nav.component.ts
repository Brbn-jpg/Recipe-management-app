import {
  Component,
  ElementRef,
  HostListener,
  OnInit,
  OnDestroy,
} from '@angular/core';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../services/language.service';
import { ScrollLockService } from '../../services/scroll-lock.service';
import { filter, Subscription } from 'rxjs';
import { AuthService } from '../../services/auth.service';

const mainUrl = 'http://localhost:4200/';

@Component({
  selector: 'app-mobile-nav',
  standalone: true,
  imports: [RouterLink, TranslateModule],
  templateUrl: './mobile-nav.component.html',
  styleUrl: './mobile-nav.component.css',
})
export class MobileNavComponent implements OnInit, OnDestroy {
  params = {
    home: mainUrl + '/',
    about: mainUrl + '/about-us',
    contact: mainUrl + '/contact',
    login: mainUrl + '/login',
  };

  isLoggedIn = false;
  isAdmin = false;
  Open = false;
  private authSubscription!: Subscription;
  language: string = 'en';

  ngOnInit(): void {
    this.checkAuthStatus();
    this.authSubscription = this.authService.isLoggedIn$.subscribe(
      (isLoggedIn: boolean) => {
        this.isLoggedIn = isLoggedIn;
        this.isAdmin = this.authService.isAdmin();
      }
    );
  }

  ngOnDestroy(): void {
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
  }

  checkAuthStatus(): void {
    this.isLoggedIn = this.authService.isAuthenticated();
  }

  constructor(
    private translate: TranslateService,
    private languageService: LanguageService,
    private el: ElementRef,
    private scrollLockService: ScrollLockService,
    private router: Router,
    private authService: AuthService
  ) {
    this.languageService.language$.subscribe((language) => {
      this.language = language;
      this.router.events
        .pipe(filter((event) => event instanceof NavigationEnd))
        .subscribe(() => {
          this.scrollLockService.unlockScroll();
        });
    });
    this.translate.setDefaultLang(this.language);
  }

  @HostListener('window:resize', ['$event'])
  onResize() {
    const mobileBreakpoint = 800;
    if (window.innerWidth > mobileBreakpoint && this.Open) {
      this.closeMenu();
    }
  }

  changeLanguage(): void {
    const newLanguage = this.language === 'en' ? 'pl' : 'en';
    this.languageService.setLanguage(newLanguage);
    this.translate.use(newLanguage);
  }

  logOut(): void {
    this.authService.logout();
    this.isLoggedIn = false;
    this.closeMenu();
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

  openMenu(): void {
    const menu = this.el.nativeElement.querySelector('.nav-menu');
    if (menu) {
      menu.classList.add('active');
      this.Open = true;
      this.scrollLockService.lockScroll();
    }
  }

  closeMenu(): void {
    const menu = this.el.nativeElement.querySelector('.nav-menu');
    if (menu) {
      menu.classList.remove('active');
      this.Open = false;
      this.scrollLockService.unlockScroll();
    }
  }
}
