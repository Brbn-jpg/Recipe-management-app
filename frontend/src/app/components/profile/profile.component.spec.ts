import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideHttpClient } from '@angular/common/http';
import { ProfileComponent } from './profile.component';
import { of, throwError } from 'rxjs';
import { ProfileService } from '../../services/profile.service';
import { NotificationService } from '../../services/notification.service';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  const profileServiceMock = jasmine.createSpyObj(
    'ProfileService',
    [
      'getEditMode',
      'getSettingsMode',
      'editProfilePicture',
      'editBackgroundPicture',
      'getUserProfile',
      'getUserRecipes',
      'getUserFavourites',
      'setShowDeleteModal',
      'getShowDeleteModal',
      'setDeleteMode',
      'getDeleteMode',
      'deleteUser',
    ],
    {
      editMode$: of(false),
      settingsMode$: of(false),
      deleteMode$: of(false),
      showDeleteModal$: of(false),
    }
  );

  const notificationServiceMock = jasmine.createSpyObj(
    'NotificationService',
    ['error'],
    {
      notifications$: of([]),
    }
  );

  const mockUser = {
    userId: 1,
    username: 'Marian',
    userPhotoUrl: [],
    backgroundImageUrl: [],
    description: '',
  };

  const mockUserRecipes = [
    {
      title: 'Test Recipe',
      category: 'BREAKFAST',
      difficulty: 1,
      servings: 2,
      prepareTime: 25,
      ingredients: [
        {
          ingredientName: 'Tomato',
          quantity: 100,
          unit: 'g',
        },
      ],
      steps: [{ content: 'Fill the pot with water' }],
      isPublic: true,
      language: 'en',
      images: null,
    },
  ];

  const mockUserFavRecipes = [
    {
      title: 'Shakshuka',
      category: 'BREAKFAST',
      difficulty: 1,
      servings: 2,
      prepareTime: 25,
      ingredients: [
        {
          ingredientName: 'Tomato',
          quantity: 2,
          unit: 'pcs',
        },
      ],
      steps: [{ content: 'Cut the tomatoes into cubes' }],
      isPublic: true,
      language: 'en',
      images: null,
    },
  ];

  const mockRecipesForFiltering = [
    {
      id: 1,
      recipeName: 'Pancakes',
      category: 'BREAKFAST',
      difficulty: 1,
      prepareTime: 15,
      servings: 2,
      language: 'english',
      avgRating: 4.5,
      ingredients: [{ ingredientName: 'Flour', quantity: 200, unit: 'g' }],
      imageUrl: [
        { imageUrl: 'https://images.com/test-image1.jpg', publicId: '1' },
      ],
      ratings: [{ ratingId: 1, value: 5 }],
    },
    {
      id: 2,
      recipeName: 'Pasta',
      category: 'LUNCH',
      difficulty: 3,
      prepareTime: 30,
      servings: 4,
      language: 'polish',
      avgRating: 4.3,
      ingredients: [{ ingredientName: 'Pasta', quantity: 300, unit: 'g' }],
      imageUrl: [
        { imageUrl: 'https://images.com/test-image2.jpg', publicId: '2' },
      ],
      ratings: [{ ratingId: 2, value: 4 }],
    },
  ];

  beforeEach(async () => {
    profileServiceMock.getUserProfile.and.returnValue(of(mockUser));
    profileServiceMock.getUserRecipes.and.returnValue(
      of({ userRecipes: mockUserRecipes })
    );
    profileServiceMock.getUserFavourites.and.returnValue(
      of({ favourites: mockUserFavRecipes })
    );

    await TestBed.configureTestingModule({
      imports: [
        ProfileComponent,
        TranslateModule.forRoot(),
        NoopAnimationsModule,
      ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: ProfileService, useValue: profileServiceMock },
        { provide: NotificationService, useValue: notificationServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should intiialize with default values', () => {
    const testFixture = TestBed.createComponent(ProfileComponent);
    const testComponent = testFixture.componentInstance;
    // Create separate component instance to test true default values
    // Main component instance runs ngOnInit() which triggers async HTTP calls,
    // causing flaky test (inconsistent) behavior due to timing issues
    expect(testComponent.userPhotoUrl).toBe(null);
    expect(testComponent.backgroundImageUrl).toBe(null);
    expect(testComponent.favouriteRecipes).toEqual([]);
    expect(testComponent.filteredFavouriteRecipes).toEqual([]);
    expect(testComponent.userRecipes).toEqual([]);
    expect(testComponent.filteredUserRecipes).toEqual([]);
    expect(testComponent.categoriesArray).toEqual([]);
    expect(testComponent.languagesArray).toEqual([]);
    expect(testComponent.ingredientsArray).toEqual([]);
    expect(testComponent.activeTab).toBe('favourites');
    expect(testComponent.editMode).toBe(false);
    expect(testComponent.settings).toBe(false);
    expect(testComponent.isMenuOpen).toBe(false);
    expect(testComponent.isFiltering).toBe(false);
    expect(testComponent.currentPage).toBe(1);
    expect(testComponent.totalPages).toBe(1);
    expect(testComponent.language).toBe('en');
  });

  it('should load user data, recipes, favourites on init', () => {
    component.ngOnInit();

    expect(profileServiceMock.getUserProfile).toHaveBeenCalled();
    expect(profileServiceMock.getUserRecipes).toHaveBeenCalled();
    expect(profileServiceMock.getUserFavourites).toHaveBeenCalled();
  });

  it('should handle user recipes loading errors', () => {
    profileServiceMock.getUserRecipes.calls.reset();
    profileServiceMock.getUserRecipes.and.returnValue(throwError('API Error'));

    component.ngOnInit();

    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Failed to load user recipes',
      5000
    );
    expect(component.userRecipes).toEqual([]);
    expect(component.filteredUserRecipes).toEqual([]);
  });

  it('should handle user favourite recipes loading errors', () => {
    profileServiceMock.getUserFavourites.calls.reset();
    profileServiceMock.getUserFavourites.and.returnValue(
      throwError('API Error')
    );
    component.ngOnInit();

    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Failed to load user favourite recipes',
      5000
    );
    expect(component.favouriteRecipes).toEqual([]);
    expect(component.filteredFavouriteRecipes).toEqual([]);
  });

  it('should apply difficulty filter', () => {
    component.userRecipes = mockRecipesForFiltering;
    component.activeTab = 'userRecipes';

    component.filters = { difficulty: 1 };
    component.applyFilters();
    expect(component.filteredUserRecipes.length).toBe(1);
    expect(component.filteredUserRecipes[0].recipeName).toBe('Pancakes');
  });

  it('should apply category filter', () => {
    component.userRecipes = mockRecipesForFiltering;
    component.activeTab = 'userRecipes';

    component.filters = { category: 'BREAKFAST' };
    component.applyFilters();
    expect(component.filteredUserRecipes.length).toBe(1);
    expect(component.filteredUserRecipes[0].recipeName).toBe('Pancakes');
  });

  it('should apply language filter', () => {
    component.userRecipes = mockRecipesForFiltering;
    component.activeTab = 'userRecipes';

    component.filters = { recipeLanguage: 'polish' };
    component.applyFilters();
    expect(component.filteredUserRecipes.length).toBe(1);
    expect(component.filteredUserRecipes[0].recipeName).toBe('Pasta');
  });

  it('should apply prep time range filter', () => {
    component.userRecipes = mockRecipesForFiltering;
    component.activeTab = 'userRecipes';

    component.filters = { prepTimeFrom: 1, prepTimeTo: 15 };
    component.applyFilters();
    expect(component.filteredUserRecipes.length).toBe(1);
    expect(component.filteredUserRecipes[0].recipeName).toBe('Pancakes');
  });

  it('should apply ingredients filter', () => {
    component.userRecipes = mockRecipesForFiltering;
    component.activeTab = 'userRecipes';

    component.filters = { ingredients: ['Flour'] };
    component.applyFilters();
    expect(component.filteredUserRecipes.length).toBe(1);
    expect(component.filteredUserRecipes[0].recipeName).toBe('Pancakes');
  });

  it('should apply multiple filters', () => {
    component.userRecipes = mockRecipesForFiltering;
    component.activeTab = 'userRecipes';

    component.filters = {
      difficulty: 1,
      category: 'BREAKFAST',
      prepTimeFrom: 1,
      prepTimeTo: 15,
      ingredients: ['Flour'],
    };
    component.applyFilters();

    expect(component.filteredUserRecipes.length).toBe(1);
    expect(component.filteredUserRecipes[0].recipeName).toBe('Pancakes');
  });

  it('should reload ingredients when language filter changes', () => {
    component.ingredientsArray = [];
    component.userRecipes = mockRecipesForFiltering;
    component.activeTab = 'userRecipes';

    component.filters = { recipeLanguage: 'polish' };

    (component as any).loadIngredients();
    // seems to work better than component.onFiltersChange();

    expect(component.ingredientsArray).toEqual(['Pasta']);
  });

  it('should switch to userRecipes tab', () => {
    component.setActiveTab('userRecipes');

    expect(component.activeTab).toBe('userRecipes');
  });

  it('should switch to favourites tab', () => {
    component.setActiveTab('favourites');

    expect(component.activeTab).toBe('favourites');
  });

  it('should reset current page when switching tabs', () => {
    component.currentPage = 4;
    component.setActiveTab('favourites');

    expect(component.activeTab).toBe('favourites');
    expect(component.currentPage).toBe(1);
  });

  it('should paginate favourite recipes correctly', () => {
    component.activeTab = 'favourites';
    component.currentPage = 1;
    component.onPageChange(2);

    expect(component.currentPage).toBe(2);
  });
  it('should paginate user recipes correctly', () => {
    component.activeTab = 'userRecipes';
    component.currentPage = 1;
    component.onPageChange(2);

    expect(component.currentPage).toBe(2);
  });

  it('should calculate total pages correctly', () => {
    const manyRecipes = Array.from({ length: 25 }, (_, i) => ({
      ...mockRecipesForFiltering[0],
      id: i + 1,
      recipeName: `Recipe ${i + 1}`,
    }));
    component.activeTab = 'userRecipes';
    component.userRecipes = manyRecipes;
    component.applyFilters();

    expect(component.totalPages).toBe(3); // 25 / 12 = 2.08 => 3 pages
  });
});
