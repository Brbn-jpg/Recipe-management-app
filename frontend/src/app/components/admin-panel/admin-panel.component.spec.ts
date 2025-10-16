import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { provideRouter, Router } from '@angular/router';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { AdminPanelComponent } from './admin-panel.component';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { AdminService } from '../../services/admin.service';
import { of, throwError } from 'rxjs';

describe('AdminPanelComponent', () => {
  let component: AdminPanelComponent;
  let fixture: ComponentFixture<AdminPanelComponent>;
  const authServiceMock = jasmine.createSpyObj('AuthService', ['isAdmin']);
  const routerMock = jasmine.createSpyObj('Router', ['navigate']);
  const adminServiceMock = jasmine.createSpyObj('AdminService', [
    'getStats',
    'getAllUsers',
    'updateUser',
    'deleteUser',
    'getAllRecipes',
    'deleteRecipe',
  ]);
  const notificationServiceMock = jasmine.createSpyObj(
    'notificationService',
    ['success', 'error'],
    {
      notifications$: of([]),
    }
  );

  const mockStats = {
    totalUsers: 25,
    adminUsers: 1,
    regularUsers: 24,
    totalRecipes: 27,
    publicRecipes: 19,
    privateRecipes: 8,
  };

  const mockUsers = [
    { id: 1, username: 'testuser', email: 'test@example.com', role: 'USER' },
    {
      id: 2,
      username: 'admin',
      email: 'admin@example.com',
      role: 'ADMIN',
    },
  ];
  const mockRecipes = [
    {
      id: 1,
      recipeName: 'Test Recipe',
      difficulty: 2,
      servings: 4,
      public: true,
    },
  ];

  beforeEach(async () => {
    routerMock.navigate.and.returnValue(Promise.resolve(true)); // making sure router will always navigate
    await TestBed.configureTestingModule({
      imports: [
        AdminPanelComponent,
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
        { provide: AdminService, useValue: adminServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminPanelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    expect(component.loading).toBe(false);
    expect(component.showEditUserModal).toBe(false);
    expect(component.showDeleteUserConfirmation).toBe(false);
    expect(component.showDeleteRecipeConfirmation).toBe(false);
    expect(component.showRecipeModal).toBe(false);
  });

  it('should update activeTab properly', () => {
    adminServiceMock.getStats.and.returnValue(of(mockStats));
    adminServiceMock.getAllRecipes.and.returnValue(of(mockRecipes));
    adminServiceMock.getAllUsers.and.returnValue(of(mockUsers));

    component.setActiveTab('dashboard');
    expect(component.activeTab).toEqual('dashboard');

    component.setActiveTab('users');
    expect(component.activeTab).toEqual('users');

    component.setActiveTab('recipes');
    expect(component.activeTab).toEqual('recipes');
  });

  it('should throw error when user has no admin role', () => {
    authServiceMock.isAdmin.and.returnValue(false);
    component.ngOnInit();
    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Access denied - Admin role required',
      3000
    );
  });

  it('should load dashboard data when user is admin', () => {
    adminServiceMock.getStats.and.returnValue(of(mockStats));

    component.loadDashboardData();

    expect(component.loading).toBe(false);
    expect(component.stats).toEqual(mockStats);
    expect(adminServiceMock.getStats).toHaveBeenCalled();
  });

  it('should handle dashboard loading error', () => {
    adminServiceMock.getStats.and.returnValue(throwError('API Error'));

    component.loadDashboardData();
    expect(component.loading).toBe(false);
    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Failed to load statistics',
      3000
    );
  });

  it('should load all users', () => {
    adminServiceMock.getAllUsers.and.returnValue(of(mockUsers));

    component.loadUsers();
    expect(component.users).toEqual(mockUsers);
    expect(component.loading).toBe(false);
  });

  it('should handle loading users error', () => {
    adminServiceMock.getAllUsers.and.returnValue(throwError('API Error'));

    component.loadUsers();
    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Failed to load users',
      3000
    );
    expect(component.loading).toBe(false);
  });

  it('should load all recipes', () => {
    adminServiceMock.getAllRecipes.and.returnValue(of(mockRecipes));

    component.loadRecipes();
    expect(component.recipes).toEqual(mockRecipes);
    expect(component.loading).toBe(false);
  });

  it('should handle loading recipes error', () => {
    adminServiceMock.getAllRecipes.and.returnValue(throwError('API Error'));

    component.loadRecipes();
    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Failed to load recipes',
      3000
    );
    expect(component.loading).toBe(false);
  });

  it('should show deleteRecipeConfirmation', () => {
    component.deleteRecipe(1);
    expect(component.recipeToDelete).toBe(1);
    expect(component.showDeleteRecipeConfirmation).toBe(true);
  });

  it('should confirm delete recipe successfully', () => {
    adminServiceMock.deleteRecipe.and.returnValue(of(undefined));
    component.recipeToDelete = 1;
    component.recipes = mockRecipes;

    component.confirmDeleteRecipe();
    expect(component.recipes.length).toBe(0);
    expect(notificationServiceMock.success).toHaveBeenCalledWith(
      'Recipe deleted successfully',
      3000
    );
  });

  it('should cancel delete recipe', () => {
    component.cancelDeleteRecipe();
    expect(component.showDeleteRecipeConfirmation).toBe(false);
    expect(component.recipeToDelete).toBe(null);
  });

  it('should show deleteUserConfirmation', () => {
    component.deleteUser(1);
    expect(component.userToDelete).toBe(1);
    expect(component.showDeleteUserConfirmation).toBe(true);
  });

  it('should confirm delete user successfully', () => {
    adminServiceMock.deleteUser.and.returnValue(of(undefined));
    component.userToDelete = 1;
    component.users = mockUsers;

    component.confirmDeleteUser();
    expect(component.users.length).toBe(1); // still have 1 more user (admin)
    expect(notificationServiceMock.success).toHaveBeenCalledWith(
      'User deleted successfully',
      3000
    );
  });

  it('should cancel delete user', () => {
    component.cancelDeleteUser();
    expect(component.showDeleteUserConfirmation).toBe(false);
    expect(component.userToDelete).toBe(null);
  });

  it('should update user', () => {
    const updatedUser = {
      id: 1,
      username: 'abc1234',
      email: 'abc123@gmail.com',
      role: 'ADMIN',
    };
    adminServiceMock.updateUser.and.returnValue(of(updatedUser));

    component.selectedUser = {
      id: 1,
      username: 'testuser',
      email: 'test@example.com',
      role: 'USER',
    };
    component.newRole = 'ADMIN';
    component.newEmail = 'newemail@gmail.com';
    component.newUsername = 'newusername';

    component.saveUser();
    expect(notificationServiceMock.success).toHaveBeenCalledWith(
      'User updated successfully',
      3000
    );
    expect(component.showEditUserModal).toBe(false);
  });

  it('should navigate to update recipe', () => {
    component.editRecipe(1);
    expect(routerMock.navigate).toHaveBeenCalledWith(['/update-recipe', 1]);
  });
});
