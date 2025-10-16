import { Component, OnInit } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { NotificationService } from '../../services/notification.service';
import { ProfileService } from '../../services/profile.service';
import { AuthService } from '../../services/auth.service';
import { UserProfile } from '../edit-profile/edit-profile.component';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-delete-profile',
  standalone: true,
  imports: [TranslateModule],
  templateUrl: './delete-profile.component.html',
  styleUrl: './delete-profile.component.css',
})
export class DeleteProfileComponent implements OnInit {
  private destroy$ = new Subject<void>();
  showDeleteUserConfirmation = false;
  userId: number = 0;
  delete = true;

  constructor(
    private notificationService: NotificationService,
    private profileService: ProfileService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadUserData();

    this.profileService.showDeleteModal$
      .pipe(takeUntil(this.destroy$))
      .subscribe((show) => {
        this.showDeleteUserConfirmation = show;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  deleteUser(): void {
    this.profileService.setShowDeleteModal(true);
  }

  cancelDeleteUser(): void {
    this.profileService.setShowDeleteModal(false);
    this.profileService.setDeleteMode(false);
  }

  confirmDeleteUser(): void {
    if (!this.userId) return;

    this.profileService
      .deleteUser(this.userId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notificationService.success(
            'Profile deleted successfully',
            3000
          );
          this.notificationService.info('Refresh to update changes', 3000);
          this.authService.logout();
          this.cancelDeleteUser();
        },
        error: () => {
          this.notificationService.error('Failed to delete user', 3000);
          this.cancelDeleteUser();
        },
      });
  }

  loadUserData() {
    this.profileService.getUserProfile().subscribe({
      next: (profile: UserProfile) => {
        this.userId = profile.id;
      },
      error: () => {
        this.notificationService.error('Failed to load user data', 5000);
      },
    });
  }
}
