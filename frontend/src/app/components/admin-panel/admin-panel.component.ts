import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { Router } from '@angular/router';

import {
  AdminService,
  AdminStats,
  UserEntity,
} from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { ToastNotificationComponent } from '../toast-notification/toast-notification.component';

@Component({
  selector: 'app-admin-panel',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule,
    ToastNotificationComponent,
  ],
  templateUrl: './admin-panel.component.html',
  styleUrls: ['./admin-panel.component.css'],
})
export class AdminPanelComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  // Stats
  stats: AdminStats | null = null;

  // Users
  users: UserEntity[] = [];
  selectedUser: UserEntity | null = null;
  newRole = '';
  newEmail = '';
  newUsername = '';

  // Recipes
  recipes: any[] = [];
  selectedRecipe: any = null;

  // UI state
  activeTab: 'dashboard' | 'users' | 'recipes' = 'dashboard';
  loading = false;
  showEditUserModal = false;
  showRecipeModal = false;
  showDeleteUserConfirmation = false;
  showDeleteRecipeConfirmation = false;
  userToDelete: number | null = null;
  recipeToDelete: number | null = null;

  constructor(
    private adminService: AdminService,
    private authService: AuthService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAdmin()) {
      this.notificationService.error(
        'Access denied - Admin role required',
        3000
      );
      return;
    }

    this.loadDashboardData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // Tab Management
  setActiveTab(tab: 'dashboard' | 'users' | 'recipes'): void {
    this.activeTab = tab;

    switch (tab) {
      case 'dashboard':
        this.loadDashboardData();
        break;
      case 'users':
        this.loadUsers();
        break;
      case 'recipes':
        this.loadRecipes();
        break;
    }
  }

  // Dashboard
  loadDashboardData(): void {
    this.loading = true;
    this.adminService
      .getStats()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (stats) => {
          this.stats = stats;
          this.loading = false;
        },
        error: (error) => {
          this.notificationService.error('Failed to load statistics', 3000);
          this.loading = false;
        },
      });
  }

  // Users Management
  loadUsers(): void {
    this.loading = true;
    this.adminService
      .getAllUsers()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (users) => {
          this.users = users;
          this.loading = false;
        },
        error: (error) => {
          this.notificationService.error('Failed to load users', 3000);
          this.loading = false;
        },
      });
  }

  openEditUserModal(user: UserEntity): void {
    this.selectedUser = user;
    this.newRole = user.role;
    this.newEmail = user.email;
    this.newUsername = user.username;
    this.showEditUserModal = true;
  }

  closeEditUserModal(): void {
    this.showEditUserModal = false;
    this.selectedUser = null;
    this.newRole = '';
    this.newEmail = '';
    this.newUsername = '';
  }

  saveUser(): void {
    if (!this.selectedUser) return;

    this.adminService
      .updateUser(
        this.selectedUser.id,
        this.newRole,
        this.newEmail,
        this.newUsername
      )
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedUser) => {
          const index = this.users.findIndex((u) => u.id === updatedUser.id);
          if (index !== -1) {
            this.users[index] = updatedUser;
          }
          this.notificationService.success('User updated successfully', 3000);
          this.closeEditUserModal();
        },
        error: (error) => {
          this.notificationService.error('Failed to update user', 3000);
        },
      });
  }

  deleteUser(userId: number): void {
    this.userToDelete = userId;
    this.showDeleteUserConfirmation = true;
  }

  confirmDeleteUser(): void {
    if (!this.userToDelete) return;

    this.adminService
      .deleteUser(this.userToDelete)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.users = this.users.filter((u) => u.id !== this.userToDelete);
          this.notificationService.success('User deleted successfully', 3000);
          this.cancelDeleteUser();
        },
        error: (error) => {
          this.notificationService.error('Failed to delete user', 3000);
          this.cancelDeleteUser();
        },
      });
  }

  cancelDeleteUser(): void {
    this.showDeleteUserConfirmation = false;
    this.userToDelete = null;
  }

  // Recipes Management
  loadRecipes(): void {
    this.loading = true;
    this.adminService
      .getAllRecipes()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (recipes) => {
          this.recipes = recipes;
          this.loading = false;
        },
        error: (error) => {
          this.notificationService.error('Failed to load recipes', 3000);
          this.loading = false;
        },
      });
  }

  editRecipe(recipeId: number): void {
    this.router.navigate(['/update-recipe', recipeId]);
  }

  deleteRecipe(recipeId: number): void {
    this.recipeToDelete = recipeId;
    this.showDeleteRecipeConfirmation = true;
  }

  confirmDeleteRecipe(): void {
    if (!this.recipeToDelete) return;

    this.adminService
      .deleteRecipe(this.recipeToDelete)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.recipes = this.recipes.filter(
            (r) => r.id !== this.recipeToDelete
          );
          this.notificationService.success('Recipe deleted successfully', 3000);
          this.cancelDeleteRecipe();
        },
        error: (error) => {
          this.notificationService.error('Failed to delete recipe', 3000);
          this.cancelDeleteRecipe();
        },
      });
  }

  cancelDeleteRecipe(): void {
    this.showDeleteRecipeConfirmation = false;
    this.recipeToDelete = null;
  }
}
