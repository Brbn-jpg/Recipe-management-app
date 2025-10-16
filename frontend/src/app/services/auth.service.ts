import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = environment.apiUrl;
  private isLoggedInSubject = new BehaviorSubject<boolean>(false);
  public isLoggedIn$ = this.isLoggedInSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {
    this.checkInitialAuthState();
  }

  private checkInitialAuthState(): void {
    const token = this.getToken();
    if (token) {
      this.isLoggedInSubject.next(true);
    } else {
      this.isLoggedInSubject.next(false);
    }
  }

  login(
    email: string,
    password: string,
    rememberMe: boolean = false
  ): Observable<any> {
    const body = { email: email, password: password };
    return this.http.post(this.apiUrl + '/authenticate', body).pipe(
      tap((response: any) => {
        if (response && response.token) {
          if (rememberMe) {
            localStorage.setItem('token', response.token);
          } else {
            sessionStorage.setItem('token', response.token);
          }
          this.isLoggedInSubject.next(true);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    sessionStorage.removeItem('token');
    this.isLoggedInSubject.next(false);
    this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) {
      this.isLoggedInSubject.next(false);
      return false;
    }
    return true;
  }

  getToken(): string | null {
    return localStorage.getItem('token') || sessionStorage.getItem('token');
  }

  register(username: string, email: string, password: string): Observable<any> {
    const body = { username, email, password };
    return this.http.post(this.apiUrl + '/register', body).pipe(
      tap((response: any) => {
        if (response && response.token) {
          sessionStorage.setItem('token', response.token);
          this.isLoggedInSubject.next(true);
        }
      })
    );
  }

  isAdmin(): boolean {
    const token = this.getToken();
    if (!token) return false;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const roles = payload.roles || [];
      return roles.includes('ADMIN');
    } catch (error) {
      return false;
    }
  }

  hasRole(role: string): boolean {
    const token = this.getToken();
    if (!token) return false;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const roles = payload.roles || [];
      return roles.includes(role);
    } catch (error) {
      return false;
    }
  }

  getUserRoles(): string[] {
    const token = this.getToken();
    if (!token) return [];

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.roles || [];
    } catch (error) {
      return [];
    }
  }
}
