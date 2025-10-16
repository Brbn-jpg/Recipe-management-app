import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class LanguageService {
  private languageSubject = new BehaviorSubject<string>(
    this.getSavedLanguage()
  );
  language$ = this.languageSubject.asObservable();

  constructor() {}

  private getSavedLanguage(): string {
    return localStorage.getItem('language') || 'en';
  }

  setLanguage(language: string): void {
    this.languageSubject.next(language);
    localStorage.setItem('language', language);
  }

  getLanguage(): string {
    return this.languageSubject.value;
  }
}
