import { animate, style, transition, trigger } from '@angular/animations';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import {
  FormControl,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { NotificationService } from '../../services/notification.service';
import { ToastNotificationComponent } from '../toast-notification/toast-notification.component';
import { Subject, takeUntil, debounceTime } from 'rxjs';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TranslateModule,
    FormsModule,
    ToastNotificationComponent,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
  animations: [
    trigger('showSection', [
      transition(':enter', [
        style({ opacity: 0 }),
        animate('0.3s ease-in-out', style({ opacity: 1 })),
      ]),
    ]),
  ],
})
export class LoginComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private loginAttempts$ = new Subject<void>();
  private registerAttempts$ = new Subject<void>();

  isLoginLoading = false;
  isRegisterLoading = false;
  private lastLoginAttempt = 0;
  private lastRegisterAttempt = 0;
  private readonly minTimeBetweenAttempts = 2000; // 2 seconds
  formUsername = '';
  formRetypePassword = '';
  formPassword = '';
  formEmail = '';
  rememberMe = false;
  isEqual = true;
  login = true;

  constructor(
    private authService: AuthService,
    private router: Router,
    private translate: TranslateService,
    private notificationService: NotificationService
  ) {
    this.translate.setDefaultLang('en');

    // Setup debounced login attempts
    this.loginAttempts$
      .pipe(debounceTime(500), takeUntil(this.destroy$))
      .subscribe(() => {
        this.executeLogin();
      });

    // Setup debounced register attempts
    this.registerAttempts$
      .pipe(debounceTime(500), takeUntil(this.destroy$))
      .subscribe(() => {
        this.executeRegister();
      });
  }

  onLogin(): void {
    if (this.isLoginLoading) {
      return;
    }

    const now = Date.now();
    if (now - this.lastLoginAttempt < this.minTimeBetweenAttempts) {
      this.notificationService.warning('Please wait before trying again');
      return;
    }

    this.loginAttempts$.next();
  }

  private executeLogin(): void {
    if (this.isLoginLoading) {
      return;
    }

    this.isLoginLoading = true;
    this.lastLoginAttempt = Date.now();

    this.authService
      .login(this.formEmail, this.formPassword, this.rememberMe)
      .subscribe({
        next: (response) => {
          this.router.navigate(['/profile']);
          this.isLoginLoading = false;
        },
        error: (err) => {
          this.notificationService.error('Email or password is incorrect');
          this.isLoginLoading = false;
        },
      });
  }

  onRegister(): void {
    if (this.isRegisterLoading) {
      return;
    }

    if (this.formPassword !== this.formRetypePassword) {
      this.notificationService.error('Passwords do not match');
      return;
    }

    const now = Date.now();
    if (now - this.lastRegisterAttempt < this.minTimeBetweenAttempts) {
      this.notificationService.warning('Please wait before trying again');
      return;
    }

    this.registerAttempts$.next();
  }

  private executeRegister(): void {
    if (this.isRegisterLoading) {
      return;
    }

    this.isRegisterLoading = true;
    this.lastRegisterAttempt = Date.now();

    this.authService
      .register(this.formUsername, this.formEmail, this.formPassword)
      .subscribe({
        next: (response) => {
          this.router.navigate(['/profile']);
          this.isRegisterLoading = false;
        },
        error: (err) => {
          this.notificationService.error(
            'Registration failed. Please try again.'
          );
          this.isRegisterLoading = false;
        },
      });
  }

  comparePasswords(): void {
    this.isEqual = this.formPassword === this.formRetypePassword;
  }

  ngOnInit() {
    window.scrollTo({ top: 0 });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  changeForm(): void {
    this.login = !this.login;
  }

  username = new FormControl('', [Validators.required]);
  email = new FormControl('', [Validators.required, Validators.email]);
  password = new FormControl('', [Validators.required]);
  retypePassword = new FormControl('', [Validators.required]);

  changeLanguage(language: string): void {
    this.translate.use(language);
  }
}
