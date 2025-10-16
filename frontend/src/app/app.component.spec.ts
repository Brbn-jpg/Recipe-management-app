import { TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Title } from '@angular/platform-browser';

describe('AppComponent', () => {
  let innerWidthSpy: jasmine.Spy;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AppComponent, // Standalone component
        RouterTestingModule,
        TranslateModule.forRoot(),
      ],
      providers: [Title, TranslateService],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it(`should have empty title initially`, () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.title).toEqual('');
  });

  it('should initialize with correct default language', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    const translateService = TestBed.inject(TranslateService);

    expect(translateService.getDefaultLang()).toEqual('en');
    expect(translateService.currentLang).toEqual('en');
  });
});
