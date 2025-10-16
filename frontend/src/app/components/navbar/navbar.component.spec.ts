import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { TranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';

import { NavbarComponent } from './navbar.component';
import { LanguageService } from '../../services/language.service';
import { AuthService } from '../../services/auth.service';

describe('NavbarComponent', () => {
  let component: NavbarComponent;
  let fixture: ComponentFixture<NavbarComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let languageService: jasmine.SpyObj<LanguageService>;
  let translateService: TranslateService;

  beforeEach(async () => {
    const authSpy = jasmine.createSpyObj('AuthService', ['logout', 'isAdmin'], {
      isLoggedIn$: of(false),
    });
    const languageSpy = jasmine.createSpyObj(
      'LanguageService',
      ['setLanguage'],
      {
        language$: of('en'),
      }
    );

    await TestBed.configureTestingModule({
      imports: [NavbarComponent, TranslateModule.forRoot()],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: AuthService, useValue: authSpy },
        { provide: LanguageService, useValue: languageSpy },
      ],
    }).compileComponents();
  });

  function createComponent() {
    fixture = TestBed.createComponent(NavbarComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    languageService = TestBed.inject(
      LanguageService
    ) as jasmine.SpyObj<LanguageService>;
    translateService = TestBed.inject(
      TranslateService
    ) as jasmine.SpyObj<TranslateService>;

    if (!(translateService.setDefaultLang as any).and) {
      spyOn(translateService, 'setDefaultLang');
      spyOn(translateService, 'use');
    }
  }

  it('should create', () => {
    createComponent();
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    createComponent();

    expect(component.isLoggedIn).toBe(false);
    expect(component.isAdmin).toBe(false);
    expect(component.language).toBe('en');
  });

  it('should set default language on initialization', () => {
    const translateSpy = TestBed.inject(TranslateService);
    spyOn(translateSpy, 'setDefaultLang');
    createComponent();
    expect(translateSpy.setDefaultLang).toHaveBeenCalledWith('en');
  });

  it('should subscribe to auth service on ngOnInit', () => {
    const newAuthSpy = jasmine.createSpyObj(
      'AuthService',
      ['logout', 'isAdmin'],
      {
        isLoggedIn$: of(true),
      }
    );
    newAuthSpy.isAdmin.and.returnValue(true);
    TestBed.overrideProvider(AuthService, { useValue: newAuthSpy });

    createComponent();
    fixture.detectChanges();

    expect(component.isLoggedIn).toBe(true);
    expect(component.isAdmin).toBe(true);
  });

  it('should change language from English to Polish', () => {
    createComponent();
    component.language = 'en';

    component.changeLanguage();

    expect(languageService.setLanguage).toHaveBeenCalledWith('pl');
    expect(translateService.use).toHaveBeenCalledWith('pl');
  });

  it('should change language from Polish to English', () => {
    createComponent();
    component.language = 'pl';

    component.changeLanguage();

    expect(languageService.setLanguage).toHaveBeenCalledWith('en');
    expect(translateService.use).toHaveBeenCalledWith('en');
  });

  it('should logout user', () => {
    createComponent();
    component.isLoggedIn = true;

    component.logOut();

    expect(authService.logout).toHaveBeenCalled();
    expect(component.isLoggedIn).toBe(false);
  });

  it('should return correct flag image for English language', () => {
    createComponent();
    component.language = 'en';

    const flagImage = component.getFlagImage();

    expect(flagImage).toBe('images/flags/pl.svg');
  });

  it('should return correct flag image for Polish language', () => {
    createComponent();
    component.language = 'pl';

    const flagImage = component.getFlagImage();

    expect(flagImage).toBe('images/flags/us.svg');
  });

  it('should return correct alt text for English language', () => {
    createComponent();
    component.language = 'en';

    const altText = component.getAltText();

    expect(altText).toBe('Zmien język na polski');
  });

  it('should return correct alt text for Polish language', () => {
    createComponent();
    component.language = 'pl';

    const altText = component.getAltText();

    expect(altText).toBe('Change language to English');
  });

  it('should update language when language service emits new value', () => {
    createComponent();
    component.language = 'pl';

    fixture.detectChanges();

    expect(component.language).toBe('pl');
  });
});
