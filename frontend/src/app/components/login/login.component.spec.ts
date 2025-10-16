import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideRouter, Router } from '@angular/router';
import { LoginComponent } from './login.component';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { of, throwError } from 'rxjs';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;

  const authServiceMock = jasmine.createSpyObj('AuthService', [
    'login',
    'register',
  ]);
  const routerMock = jasmine.createSpyObj('Router', ['navigate']);
  const notificationServiceMock = jasmine.createSpyObj(
    'NotificationService',
    ['error', 'warning'],
    { notifications$: of([]) }
  );
  beforeEach(async () => {
    routerMock.navigate.and.returnValue(Promise.resolve(true)); // making sure router will always navigate
    await TestBed.configureTestingModule({
      imports: [
        LoginComponent,
        TranslateModule.forRoot(),
        NoopAnimationsModule,
      ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock },
        { provide: NotificationService, useValue: notificationServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    expect(component.isLoginLoading).toBe(false);
    expect(component.isRegisterLoading).toBe(false);
    expect(component.rememberMe).toBe(false);
    expect(component.isEqual).toBe(true);
    expect(component.login).toBe(true);
  });

  it('should toggle between login and register forms', () => {
    expect(component.login).toBe(true);
    component.changeForm();
    expect(component.login).toBe(false);
    component.changeForm();
    expect(component.login).toBe(true);
  });

  it('should compare password correctly', () => {
    component.formPassword = 'abc123';
    component.formRetypePassword = 'abc12';
    component.comparePasswords();
    expect(component.isEqual).toBe(false);

    component.formPassword = 'abc123';
    component.formRetypePassword = 'abc123';
    component.comparePasswords();
    expect(component.isEqual).toBe(true);
  });

  it('should show error when passwords do not match during registration', () => {
    component.formPassword = 'abc123';
    component.formRetypePassword = 'abc12';
    component.onRegister();
    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Passwords do not match'
    );
    expect(component.isRegisterLoading).toBe(false);
  });

  it('should prevent rapid login attempts with rate limiting', () => {
    spyOn(Date, 'now').and.returnValues(0, 1500); // needed because rapid firing onLogin is ~2ms

    component.formPassword = 'abc123';
    component.formEmail = 'abc123@gmail.com';
    component.onLogin();
    component.onLogin();

    expect(notificationServiceMock.warning).toHaveBeenCalledWith(
      'Please wait before trying again'
    );
    expect(notificationServiceMock.warning).toHaveBeenCalledTimes(2); // 2 beacuse executeLogin & onLogin
  });

  it('should handle successful login and navigate to profile', fakeAsync(() => {
    authServiceMock.login.and.returnValue(of({ success: true }));
    component.formPassword = 'abc123';
    component.formEmail = 'abc123@gmail.com';
    component.onLogin();
    tick(600);
    expect(routerMock.navigate).toHaveBeenCalled();
    expect(component.isLoginLoading).toBe(false);
  }));

  it('should handle login failure and show error message', fakeAsync(() => {
    routerMock.navigate.calls.reset();
    authServiceMock.login.and.returnValue(throwError('Login failed'));
    component.formPassword = 'abc123';
    component.formEmail = 'abc123@gmail.co';

    component.onLogin();
    tick(600);

    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Email or password is incorrect'
    );
    expect(routerMock.navigate).not.toHaveBeenCalled();
    expect(component.isLoginLoading).toBe(false);
  }));

  it('should validate email format with FormControl', () => {
    component.email.setValue('abc123@gmail.com');
    expect(component.email.invalid).toBe(false);

    component.email.setValue('abc123.com');
    expect(component.email.invalid).toBe(true);
  });
});
