import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { provideRouter, Router } from '@angular/router';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MobileNavComponent } from './mobile-nav.component';
import { AuthService } from '../../services/auth.service';
import { of } from 'rxjs';
import { ScrollLockService } from '../../services/scroll-lock.service';
import { LanguageService } from '../../services/language.service';

describe('MobileNavComponent', () => {
  let component: MobileNavComponent;
  let fixture: ComponentFixture<MobileNavComponent>;

  const authServiceMock = jasmine.createSpyObj('AuthService', [
    'isAdmin',
    'isAuthenticated',
    'logout',
  ]);

  const scrollLockServiceMock = jasmine.createSpyObj('scrollLockService', [
    'lockScroll',
    'unlockScroll',
  ]);

  const languageServiceMock = jasmine.createSpyObj(
    'languageService',
    ['setLanguage'],
    {
      language$: of(),
    }
  );

  beforeEach(async () => {
    authServiceMock.isLoggedIn$ = of(false);
    authServiceMock.isAuthenticated.and.returnValue(false);
    authServiceMock.isAdmin.and.returnValue(false);
    await TestBed.configureTestingModule({
      imports: [
        MobileNavComponent,
        TranslateModule.forRoot(),
        NoopAnimationsModule,
      ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: AuthService, useValue: authServiceMock },
        { provide: ScrollLockService, useValue: scrollLockServiceMock },
        { provide: LanguageService, useValue: languageServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MobileNavComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initalize with default vaulues', () => {
    const mainUrl = 'http://localhost:4200/';
    const testFixture = TestBed.createComponent(MobileNavComponent);
    const testComponent = testFixture.componentInstance;
    // Create separate component instance to test true default values
    // Main component instance runs ngOnInit() which triggers async HTTP calls,
    // causing flaky test (inconsistent) behavior due to timing issues

    expect(testComponent.params).toEqual({
      home: mainUrl + '/',
      about: mainUrl + '/about-us',
      contact: mainUrl + '/contact',
      login: mainUrl + '/login',
    });
    expect(testComponent.isLoggedIn).toBe(false);
    expect(testComponent.isAdmin).toBe(false);
    expect(testComponent.Open).toBe(false);
    expect(testComponent.language).toBe('en');
  });

  it('should subscribe to isLoggedIn and checkAuthStatus', fakeAsync(() => {
    authServiceMock.isLoggedIn$ = of(true);
    authServiceMock.isAdmin.and.returnValue(true);

    component.ngOnInit();
    tick();

    expect(component.isLoggedIn).toBe(true);
    expect(component.isAdmin).toBe(true);
  }));

  it('should handle auth status false', () => {
    authServiceMock.isLoggedIn$ = of(false);
    authServiceMock.isAdmin.and.returnValue(false);

    component.ngOnInit();

    expect(component.isLoggedIn).toBe(false);
    expect(component.isAdmin).toBe(false);
  });

  it('should unsubscribe to all services to prevent memory leaks', () => {
    component.ngOnInit();
    const unsubscribeSpy = spyOn(component['authSubscription'], 'unsubscribe');

    component.ngOnDestroy();

    expect(unsubscribeSpy).toHaveBeenCalled();
  });

  it('should change isLoggedIn to true if authenticated', () => {
    component.isLoggedIn = false;
    authServiceMock.isAuthenticated.and.returnValue(true);
    component.checkAuthStatus();

    expect(component.isLoggedIn).toBe(true);
  });

  it('should logout user', () => {
    component.isLoggedIn = true;
    const closeMenuSpy = spyOn(component, 'closeMenu');

    component.logOut();

    expect(authServiceMock.logout).toHaveBeenCalled();
    expect(component.isLoggedIn).toBe(false);
    expect(closeMenuSpy).toHaveBeenCalled();
  });

  it('should open menu properly', () => {
    component.openMenu();
    expect(component.Open).toBe(true);
    expect(scrollLockServiceMock.lockScroll).toHaveBeenCalled();
  });

  it('should close menu properly', () => {
    component.closeMenu();
    expect(component.Open).toBe(false);
    expect(scrollLockServiceMock.unlockScroll).toHaveBeenCalled();
  });

  it('should close menu when window size > 800px and menu is open', () => {
    component.Open = true;
    const closeMenuSpy = spyOn(component, 'closeMenu');
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 801,
    });

    component.onResize();

    expect(closeMenuSpy).toHaveBeenCalled();
  });

  it('should close menu when window size > 800px and menu is closed', () => {
    component.Open = false;
    const closeMenuSpy = spyOn(component, 'closeMenu');
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 801,
    });

    component.onResize();

    expect(closeMenuSpy).not.toHaveBeenCalled();
  });

  it('should not close menu when window size <= 800px', () => {
    component.Open = true;
    const closeMenuSpy = spyOn(component, 'closeMenu');
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 600,
    });

    component.onResize();

    expect(closeMenuSpy).not.toHaveBeenCalled();
  });

  it('should toggle languages', () => {
    component.language = 'en';

    component.changeLanguage();

    expect(languageServiceMock.setLanguage).toHaveBeenCalledWith('pl');
  });

  it('should get flag image for pl language', () => {
    component.language = 'pl';

    const result = component.getFlagImage();

    expect(result).toBe('images/flags/us.svg');
  });

  it('should get flag image for en language', () => {
    component.language = 'en';

    const result = component.getFlagImage();

    expect(result).toBe('images/flags/pl.svg');
  });

  it('should get proper alt text for pl language', () => {
    component.language = 'pl';

    const result = component.getAltText();

    expect(result).toBe('Change language to English');
  });

  it('should get proper alt text for en language', () => {
    component.language = 'en';

    const result = component.getAltText();

    expect(result).toBe('Zmien język na polski');
  });
});
