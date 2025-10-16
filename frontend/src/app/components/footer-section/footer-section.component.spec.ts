import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { FooterSectionComponent } from './footer-section.component';

describe('FooterSectionComponent', () => {
  let component: FooterSectionComponent;
  let fixture: ComponentFixture<FooterSectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FooterSectionComponent, TranslateModule.forRoot()],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(FooterSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
