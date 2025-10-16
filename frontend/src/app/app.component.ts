import { Component, OnInit, HostListener } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { TranslateModule } from '@ngx-translate/core';
import { TranslateService } from '@ngx-translate/core';
import { filter, map } from 'rxjs';
import {
  Router,
  RouterModule,
  RouterOutlet,
  NavigationEnd,
  ActivatedRoute,
} from '@angular/router';
import { NavbarComponent } from './components/navbar/navbar.component';
import { MobileNavComponent } from './components/mobile-nav/mobile-nav.component';
import { FooterSectionComponent } from './components/footer-section/footer-section.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterModule,
    TranslateModule,
    NavbarComponent,
    MobileNavComponent,
    FooterSectionComponent,
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent implements OnInit {
  isMobile = false;
  isLoggedIn = false;
  title = '';

  constructor(
    private router: Router,
    private titleService: Title,
    private translate: TranslateService
  ) {
    this.translate.addLangs(['pl', 'en']);
    this.translate.setDefaultLang('en');
    this.translate.use('en');
    this.checkMobile();
  }

  @HostListener('window:resize')
  onResize() {
    this.checkMobile();
  }

  private checkMobile() {
    this.isMobile = window.innerWidth <= 800;
  }

  ngOnInit() {
    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        map(() => {
          let route: ActivatedRoute = this.router.routerState.root;
          let routeTitle = '';
          while (route!.firstChild) {
            route = route.firstChild;
          }
          if (route.snapshot.data['title']) {
            routeTitle = route!.snapshot.data['title'];
          }
          return routeTitle;
        })
      )
      .subscribe((title: string) => {
        if (title) {
          this.titleService.setTitle(`${title}`);
        }
      });
  }
}
