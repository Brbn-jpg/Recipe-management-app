import { Component, OnInit, OnDestroy } from '@angular/core';
import { ProfileService } from '../../services/profile.service';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { NotificationService } from '../../services/notification.service';
import { Subject, takeUntil, debounceTime } from 'rxjs';

export interface UserProfile {
  id: number;
  username: string;
  description: string;
}

@Component({
  selector: 'app-edit-profile',
  standalone: true,
  imports: [FormsModule, TranslateModule],
  templateUrl: './edit-profile.component.html',
  styleUrl: './edit-profile.component.css',
})
export class EditProfileComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private usernameUpdateAttempts$ = new Subject<void>();
  private descriptionUpdateAttempts$ = new Subject<void>();

  isUpdatingUsername = false;
  isUpdatingDescription = false;
  private lastUsernameUpdate = 0;
  private lastDescriptionUpdate = 0;
  private readonly minTimeBetweenUpdates = 2000; // 2 seconds
  userId: number = 0;
  saveUsernameIcon: boolean = false;
  saveDescriptionIcon: boolean = false;
  saveEmailIcon: boolean = false;
  savePasswordIcon: boolean = false;
  newUsername: string = '';
  newDescription: string = '';
  username: string = '';
  description: string = '';

  // Store original values for comparison
  originalUsername: string = '';
  originalDescription: string = '';

  constructor(
    private profileService: ProfileService,
    private notificationService: NotificationService
  ) {
    // Setup debounced username update attempts
    this.usernameUpdateAttempts$
      .pipe(debounceTime(800), takeUntil(this.destroy$))
      .subscribe(() => {
        this.executeUsernameUpdate();
      });

    // Setup debounced description update attempts
    this.descriptionUpdateAttempts$
      .pipe(debounceTime(800), takeUntil(this.destroy$))
      .subscribe(() => {
        this.executeDescriptionUpdate();
      });
  }

  ngOnInit(): void {
    this.loadUserData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  saveUsername() {
    if (this.isUpdatingUsername) {
      this.notificationService.warning(
        'Please wait, updating username...',
        5000
      );
      return;
    }

    const now = Date.now();
    if (now - this.lastUsernameUpdate < this.minTimeBetweenUpdates) {
      this.notificationService.warning('Please wait before updating again');
      return;
    }

    this.usernameUpdateAttempts$.next();
  }

  private executeUsernameUpdate() {
    if (this.isUpdatingUsername) {
      return;
    }

    const usernameTextarea = document.getElementById(
      'username-id'
    ) as HTMLInputElement;

    if (
      !usernameTextarea ||
      !usernameTextarea.value ||
      usernameTextarea.value.trim() === ''
    ) {
      this.notificationService.error('Username cannot be empty', 5000);
      return;
    }

    const newUsername = usernameTextarea.value.trim();

    if (newUsername === this.originalUsername) {
      this.notificationService.info('Username is already up to date', 5000);
      return;
    }

    this.isUpdatingUsername = true;
    this.lastUsernameUpdate = Date.now();

    const profileData = {
      username: newUsername,
      description: this.description,
    };

    this.profileService.updateUserProfile(this.userId, profileData).subscribe({
      next: () => {
        this.saveUsernameIcon = true;
        this.username = newUsername;
        this.newUsername = newUsername;
        this.originalUsername = newUsername; // Update original value
        this.loadUserData(); // Reload user data to reflect changes
        this.notificationService.success('Username updated successfully', 5000);
        this.notificationService.info('Refresh site to update changes', 5000);
        this.isUpdatingUsername = false;
        setTimeout(() => {
          this.saveUsernameIcon = false;
        }, 5000);
      },
      error: () => {
        this.notificationService.error('Failed to update username', 5000);
        this.isUpdatingUsername = false;
      },
    });
  }

  saveDescription() {
    if (this.isUpdatingDescription) {
      this.notificationService.warning(
        'Please wait, updating description...',
        5000
      );
      return;
    }

    const now = Date.now();
    if (now - this.lastDescriptionUpdate < this.minTimeBetweenUpdates) {
      this.notificationService.warning(
        'Please wait before updating again',
        5000
      );
      return;
    }

    this.descriptionUpdateAttempts$.next();
  }

  private executeDescriptionUpdate() {
    if (this.isUpdatingDescription) {
      return;
    }

    const descriptionTextarea = document.getElementById(
      'description-id'
    ) as HTMLTextAreaElement;

    const newDescription = descriptionTextarea
      ? descriptionTextarea.value.trim()
      : '';

    if (newDescription === this.originalDescription) {
      this.notificationService.info('Description is already up to date', 5000);
      return;
    }

    this.isUpdatingDescription = true;
    this.lastDescriptionUpdate = Date.now();

    const profileData = {
      username: this.username,
      description: newDescription,
    };

    this.profileService.updateUserProfile(this.userId, profileData).subscribe({
      next: () => {
        this.saveDescriptionIcon = true;
        this.description = newDescription;
        this.newDescription = newDescription;
        this.originalDescription = newDescription; // Update original value
        this.loadUserData(); // Reload user data to reflect changes
        this.notificationService.success(
          'Description updated successfully',
          5000
        );
        this.isUpdatingDescription = false;
        setTimeout(() => {
          this.saveDescriptionIcon = false;
        }, 5000);
      },
      error: () => {
        this.notificationService.error('Failed to update description', 5000);
        this.isUpdatingDescription = false;
      },
    });
  }

  loadUserData() {
    this.profileService.getUserProfile().subscribe({
      next: (profile: UserProfile) => {
        this.userId = profile.id;
        this.username = profile.username;
        this.description = profile.description;
        this.newUsername = profile.username;
        this.newDescription = profile.description;

        // Store original values
        this.originalUsername = profile.username;
        this.originalDescription = profile.description;
      },
      error: (error) => {
        this.notificationService.error('Failed to load user data', 5000);
      },
    });
  }

  cancelEdit() {
    this.profileService.setEditMode(false);
    this.newUsername = this.username;
    this.newDescription = this.description || '';
  }

  save() {
    if (this.isUpdatingUsername || this.isUpdatingDescription) {
      this.notificationService.warning(
        'Please wait, update in progress...',
        5000
      );
      return;
    }

    const usernameTextarea = document.getElementById(
      'username-id'
    ) as HTMLInputElement;
    const descriptionTextarea = document.getElementById(
      'description-id'
    ) as HTMLTextAreaElement;

    // Just try to save both - individual methods will handle validation
    if (usernameTextarea && usernameTextarea.value.trim()) {
      this.saveUsername();
    }

    if (descriptionTextarea) {
      this.saveDescription();
    }
  }
}
