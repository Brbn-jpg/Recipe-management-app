import { Component, OnInit, OnDestroy } from '@angular/core';
import { ProfileService } from '../../services/profile.service';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { NotificationService } from '../../services/notification.service';
import { Subject, takeUntil, debounceTime } from 'rxjs';

export interface UserProfileResponse {
  id: number;
}

@Component({
  selector: 'app-settings-profile',
  standalone: true,
  imports: [FormsModule, TranslateModule],
  templateUrl: './settings-profile.component.html',
  styleUrl: './settings-profile.component.css',
})
export class SettingsProfileComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private emailUpdateAttempts$ = new Subject<void>();
  private passwordUpdateAttempts$ = new Subject<void>();

  isUpdatingEmail = false;
  isUpdatingPassword = false;
  private lastEmailUpdate = 0;
  private lastPasswordUpdate = 0;
  private readonly minTimeBetweenUpdates = 3000; // 3 seconds
  userId!: number;
  email: string = '';
  newPasswordInput: string = '';
  saveEmailIcon: boolean = false;
  savePasswordIcon: boolean = false;
  settings: boolean = true;

  constructor(
    private profileService: ProfileService,
    private notificationService: NotificationService
  ) {
    // Setup debounced email update attempts
    this.emailUpdateAttempts$
      .pipe(debounceTime(1000), takeUntil(this.destroy$))
      .subscribe(() => {
        this.executeEmailUpdate();
      });

    // Setup debounced password update attempts
    this.passwordUpdateAttempts$
      .pipe(debounceTime(1000), takeUntil(this.destroy$))
      .subscribe(() => {
        this.executePasswordUpdate();
      });
  }

  ngOnInit() {
    this.loadUserData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  saveEmail() {
    if (this.isUpdatingEmail) {
      this.notificationService.warning('Please wait, updating email...');
      return;
    }

    const now = Date.now();
    if (now - this.lastEmailUpdate < this.minTimeBetweenUpdates) {
      this.notificationService.warning('Please wait before updating again');
      return;
    }

    this.emailUpdateAttempts$.next();
  }

  private executeEmailUpdate() {
    if (this.isUpdatingEmail) {
      return;
    }

    const passwordInput = document.getElementById(
      'password-id'
    ) as HTMLInputElement;

    // Validate new email using Angular binding
    if (!this.email || this.email.trim() === '') {
      this.notificationService.warning('Email cannot be empty');
      return;
    }

    // Validate password from DOM element
    if (
      !passwordInput ||
      !passwordInput.value ||
      passwordInput.value.trim() === ''
    ) {
      this.notificationService.warning('Password is required to change email');
      return;
    }

    const newEmail = this.email.trim().toLowerCase();
    const password = passwordInput.value.trim();

    // Validate email format
    const emailRegex = /^[A-Za-z0-9+_.-]+@(.+)$/;
    if (!emailRegex.test(newEmail)) {
      this.notificationService.error('Invalid email format');
      return;
    }

    this.isUpdatingEmail = true;
    this.lastEmailUpdate = Date.now();

    const updateEmailDto = {
      newEmail: newEmail,
      password: password,
    };

    this.profileService.updateUserEmail(this.userId, updateEmailDto).subscribe({
      next: () => {
        this.saveEmailIcon = true;
        // Clear fields
        this.email = '';
        passwordInput.value = '';
        this.loadUserData();

        // Show success notification
        this.notificationService.success('Email updated successfully!');
        this.notificationService.info(
          'Please log in again with your new email'
        );
        this.isUpdatingEmail = false;

        setTimeout(() => {
          this.saveEmailIcon = false;
        }, 5000);
      },
      error: (error) => {
        this.handleApiError(error, 'Failed to update Email. Please try again.');
        this.isUpdatingEmail = false;
      },
    });
  }
  savePassword() {
    if (this.isUpdatingPassword) {
      this.notificationService.warning('Please wait, updating password...');
      return;
    }

    const now = Date.now();
    if (now - this.lastPasswordUpdate < this.minTimeBetweenUpdates) {
      this.notificationService.warning('Please wait before updating again');
      return;
    }

    this.passwordUpdateAttempts$.next();
  }

  private executePasswordUpdate() {
    if (this.isUpdatingPassword) {
      return;
    }

    const currentPasswordInput = document.getElementById(
      'current-password-id'
    ) as HTMLInputElement;

    // Validate current password from DOM element
    if (
      !currentPasswordInput ||
      !currentPasswordInput.value ||
      currentPasswordInput.value.trim() === ''
    ) {
      this.notificationService.error('Current password cannot be empty');
      return;
    }

    // Validate new password using Angular binding
    if (!this.newPasswordInput || this.newPasswordInput.trim() === '') {
      this.notificationService.error('New password cannot be empty');
      return;
    }

    const currentPassword = currentPasswordInput.value.trim();
    const newPassword = this.newPasswordInput.trim();

    // Validate password length (zgodnie z backendem)
    if (newPassword.length < 8) {
      this.notificationService.error(
        'Password must be at least 8 characters long'
      );
      return;
    }

    if (newPassword.length > 64) {
      this.notificationService.error(
        'Password cannot be longer than 64 characters'
      );
      return;
    }

    // Validate password complexity (digit and uppercase letter)
    if (!/.*\d.*/.test(newPassword)) {
      this.notificationService.error(
        'Password must contain at least one digit'
      );
      return;
    }

    if (!/.*[A-Z].*/.test(newPassword)) {
      this.notificationService.error(
        'Password must contain at least one uppercase letter'
      );
      return;
    }

    // Check if new password is different from current
    if (currentPassword === newPassword) {
      this.notificationService.error(
        'New password must be different from the current password'
      );
      return;
    }

    this.isUpdatingPassword = true;
    this.lastPasswordUpdate = Date.now();

    const updatePasswordDto = {
      currentPassword: currentPassword,
      newPassword: newPassword,
    };

    this.profileService
      .updateUserPassword(this.userId, updatePasswordDto)
      .subscribe({
        next: () => {
          this.savePasswordIcon = true;
          // Clear all password fields
          currentPasswordInput.value = '';
          this.newPasswordInput = '';
          this.loadUserData(); // Reload user data to reflect changes

          // Show success notification
          this.notificationService.success('Password updated successfully!');
          this.notificationService.info(
            'Please log in again with your new password'
          );
          this.isUpdatingPassword = false;

          setTimeout(() => {
            this.savePasswordIcon = false;
          }, 5000);
        },
        error: (error) => {
          this.handleApiError(
            error,
            'Failed to update password. Please try again.'
          );
          this.isUpdatingPassword = false;
        },
      });
  }

  loadUserData() {
    this.profileService.getUserProfile().subscribe({
      next: (response: UserProfileResponse) => {
        if (response) {
          this.userId = response.id;
        }
      },
    });
  }

  save() {
    if (this.isUpdatingEmail || this.isUpdatingPassword) {
      this.notificationService.warning('Please wait, update in progress...');
      return;
    }

    if (this.email != '') {
      this.saveEmail();
    }
    if (this.newPasswordInput != '') {
      this.savePassword();
    }
  }
  cancelSettings() {
    this.profileService.setSettingsMode(false);
  }

  private handleApiError(
    error: any,
    defaultMessage: string = 'An error occurred'
  ) {
    let errorMessage = defaultMessage;

    if (error.error?.message) {
      errorMessage = error.error.message;
    } else if (error.status === 400) {
      errorMessage = 'Bad request - please check your input';
    } else if (error.status === 401) {
      errorMessage = 'Unauthorized - one of the passwords is not matching';
    } else if (error.status === 403) {
      errorMessage = 'Access forbidden';
    } else if (error.status === 404) {
      errorMessage = 'Resource not found';
    } else if (error.status === 500) {
      errorMessage = 'Server error - please try again later';
    }

    this.notificationService.error(errorMessage);
  }
}
