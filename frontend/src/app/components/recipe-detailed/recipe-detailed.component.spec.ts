import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RecipeDetailedComponent } from './recipe-detailed.component';
import { of, throwError } from 'rxjs';
import { RecipeService } from '../../services/recipe.service';
import { NotificationService } from '../../services/notification.service';
import { AuthService } from '../../services/auth.service';

describe('RecipeDetailedComponent', () => {
  let component: RecipeDetailedComponent;
  let fixture: ComponentFixture<RecipeDetailedComponent>;

  const authServiceMock = jasmine.createSpyObj('AuthService', [
    'isAuthenticated',
  ]);
  const recipeServiceMock = jasmine.createSpyObj('RecipeService', [
    'isOwner',
    'isFavourite',
    'loadRecipeDetails',
    'getUserRating',
    'addToFavourites',
    'removeFromFavourites',
    'deleteRecipe',
    'rateRecipe',
  ]);

  const notificationServiceMock = jasmine.createSpyObj(
    'NotificationService',
    ['error', 'info', 'success', 'warning'],
    {
      notifications$: of([]),
    }
  );

  const routerMock = jasmine.createSpyObj('Router', ['navigate']);

  const mockRecipe = {
    id: 1,
    recipeId: 1,
    recipeName: 'Test Recipe 1',
    category: 'BREAKFAST',
    difficulty: 2,
    prepareTime: 30,
    servings: 4,
    isPublic: true,
    language: 'en',
    ingredients: [{ ingredientName: 'Tomato', unit: 'g', quantity: 100 }],
    steps: [{ content: 'Chop tomatoes' }],
    images: [],
    ratings: [{ ratingId: 1, value: 5 }],
  };

  beforeEach(async () => {
    recipeServiceMock.loadRecipeDetails.and.returnValue(of(mockRecipe));
    recipeServiceMock.isFavourite.and.returnValue(of(false));
    recipeServiceMock.isOwner.and.returnValue(of(false));
    recipeServiceMock.getUserRating.and.returnValue(of(0));
    recipeServiceMock.addToFavourites.and.returnValue(of({}));
    recipeServiceMock.removeFromFavourites.and.returnValue(of({}));
    recipeServiceMock.deleteRecipe.and.returnValue(of({}));
    recipeServiceMock.rateRecipe.and.returnValue(of({}));

    routerMock.navigate.calls.reset();
    notificationServiceMock.error.calls.reset();
    notificationServiceMock.success.calls.reset();
    recipeServiceMock.loadRecipeDetails.calls.reset();
    recipeServiceMock.addToFavourites.calls.reset();
    recipeServiceMock.rateRecipe.calls.reset();

    await TestBed.configureTestingModule({
      imports: [
        RecipeDetailedComponent,
        TranslateModule.forRoot(),
        NoopAnimationsModule,
      ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { params: of({ id: '1' }) },
        },
        { provide: RecipeService, useValue: recipeServiceMock },
        { provide: NotificationService, useValue: notificationServiceMock },
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RecipeDetailedComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    const testFixture = TestBed.createComponent(RecipeDetailedComponent);
    const testComponent = testFixture.componentInstance;
    // Create separate component instance to test true default values
    // Main component instance runs ngOnInit() which triggers async HTTP calls,
    // causing flaky test (inconsistent) behavior due to timing issues
    expect(testComponent.ingredients).toEqual([]);
    expect(testComponent.isFavourite).toBe(false);
    expect(testComponent.isProcessing).toBe(false);
    expect(testComponent.isLoggedIn).toBe(false);
    expect(testComponent.isOwner).toBe(false);
    expect(testComponent.showConfirmation).toBe(false);
    expect(testComponent.isRatingProcessing).toBe(false);
    expect(testComponent.currentRating).toBe(0);
    expect(testComponent.hoveredRating).toBe(0);
  });

  it('should throw error on non 404 error', () => {
    const error = { status: 500 };
    recipeServiceMock.loadRecipeDetails.and.returnValue(throwError(error));

    component.ngOnInit();
    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Error loading recipe details',
      5000
    );
    expect(routerMock.navigate).not.toHaveBeenCalled();
  });

  it('should navigate to not-found when 404 error', () => {
    const error = { status: 404 };
    recipeServiceMock.loadRecipeDetails.and.returnValue(throwError(error));

    component.ngOnInit();
    expect(routerMock.navigate).toHaveBeenCalledWith(['/not-found']);
  });

  it('should check if isAuthenticated is called', () => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    fixture.detectChanges();
    expect(component.isLoggedIn).toBe(true);
    expect(component.recipeId).toBe(1);
  });

  it('should retain isLoggedIn false when unauthenticated', () => {
    authServiceMock.isAuthenticated.and.returnValue(false);
    component.ngOnInit();
    expect(component.isLoggedIn).toBe(false);
  });

  it('should map ingredients properly when loading recipe details', () => {
    component.ngOnInit();
    expect(component.ingredients).toEqual([
      { name: 'Tomato', unit: 'g', quantity: 100 },
    ]);
  });

  it('should correctly load user rating and set current rating', () => {
    recipeServiceMock.getUserRating.and.returnValue(of(5));
    authServiceMock.isAuthenticated.and.returnValue(true);
    component.ngOnInit();
    expect(component.isLoggedIn).toBe(true);
    expect(component.currentRating).toBe(5);
  });

  it('should set currentRating to 0 when error', () => {
    recipeServiceMock.getUserRating.and.returnValue(throwError('API Error'));
    authServiceMock.isAuthenticated.and.returnValue(true);
    component.ngOnInit();
    expect(component.isLoggedIn).toBe(true);
    expect(component.currentRating).toBe(0);
  });

  it('should show delete confirmation', () => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    component.delete();
    expect(component.showConfirmation).toBe(true);
  });

  it('should close confirmation when cancelDelete is called', () => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    component.cancelDelete();
    expect(component.showConfirmation).toBe(false);
  });

  it('should delete recipe', () => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    recipeServiceMock.deleteRecipe.and.returnValue(of({ recipeId: 1 }));
    component.confirmDelete();
    expect(notificationServiceMock.success).toHaveBeenCalledWith(
      'Recipe has been deleted',
      5000
    );
    expect(notificationServiceMock.info).toHaveBeenCalledWith(
      'Refresh the site to see changes',
      5000
    );
  });

  it('should throw error when deleting recipe goes wrong', () => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    recipeServiceMock.deleteRecipe.and.returnValue(throwError('API Error'));
    component.confirmDelete();
    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Error during deletion',
      5000
    );
  });

  it('should add recipe to favourites', fakeAsync(() => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    recipeServiceMock.addToFavourites.and.returnValue(of({ recipeId: 1 }));

    component.isLoggedIn = true;
    component.isFavourite = false;
    component.toggleFavourite();

    tick(400);

    expect(recipeServiceMock.addToFavourites).toHaveBeenCalledWith(
      component.recipeId
    );
    expect(component.isFavourite).toBe(true);
  }));

  it('shold throw error when adding to favourites goes wrong', fakeAsync(() => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    recipeServiceMock.addToFavourites.and.returnValue(throwError('API Error'));

    component.isLoggedIn = true;
    component.isFavourite = false;
    component.toggleFavourite();
    tick(400);

    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Failed to add to favourites',
      5000
    );
    expect(component.isProcessing).toBe(false);
  }));

  it('should remove recipe from favourites', fakeAsync(() => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    recipeServiceMock.removeFromFavourites.and.returnValue(of({ recipeId: 1 }));

    component.isLoggedIn = true;
    component.isFavourite = true;

    component.toggleFavourite();

    tick(400);

    expect(component.isFavourite).toBe(false);
    expect(recipeServiceMock.removeFromFavourites).toHaveBeenCalledWith(
      component.recipeId
    );
  }));

  it('shold throw error when removing from favourites goes wrong', fakeAsync(() => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    recipeServiceMock.removeFromFavourites.and.returnValue(
      throwError('API Error')
    );

    component.isLoggedIn = true;
    component.isFavourite = true;
    component.toggleFavourite();
    tick(400);

    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Failed to remove from favourites',
      5000
    );
    expect(component.isProcessing).toBe(false);
  }));

  it('should add rating to recipe', fakeAsync(() => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    recipeServiceMock.rateRecipe.and.returnValue(
      of({ recipeId: 1, rating: 5 })
    );
    component.isLoggedIn = true;
    component.currentRating = 0;
    component.onStarClick(5);
    tick(600);

    expect(recipeServiceMock.rateRecipe).toHaveBeenCalledTimes(1);
    expect(component.currentRating).toBe(5);
    expect(notificationServiceMock.success).toHaveBeenCalledWith(
      'Recipe rated with 5 stars!',
      5000
    );
    expect(component.isRatingProcessing).toBe(false);
  }));

  it('should handle errors when rating recipe', fakeAsync(() => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    recipeServiceMock.rateRecipe.and.returnValue(throwError('API Error'));
    component.isLoggedIn = true;
    component.currentRating = 0;
    component.onStarClick(5);
    tick(600);

    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Error submitting rating',
      5000
    );
    expect(component.isRatingProcessing).toBe(false);
  }));

  it('should parse recipeId from route params correctly', () => {
    component.ngOnInit();
    expect(component.recipeId).toBe(1);
  });

  it('should handle missing recipeId and show error ', () => {
    TestBed.resetTestingModule();
    const testActivatedRoute = { params: of({ id: '' }) };

    TestBed.configureTestingModule({
      imports: [
        RecipeDetailedComponent,
        TranslateModule.forRoot(),
        NoopAnimationsModule,
      ],
      providers: [
        { provide: ActivatedRoute, useValue: testActivatedRoute },
        { provide: AuthService, useValue: authServiceMock },
        { provide: RecipeService, useValue: recipeServiceMock },
        { provide: NotificationService, useValue: notificationServiceMock },
        { provide: Router, useValue: routerMock },
      ],
    });
    const testFixture = TestBed.createComponent(RecipeDetailedComponent);
    const testComponent = testFixture.componentInstance;
    testComponent.ngOnInit();

    expect(testComponent.recipeId).toBe(0);
    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Recipe not found',
      5000
    );
  });

  it('should handle different route params', () => {
    TestBed.resetTestingModule();
    const testActivatedRoute = { params: of({ id: '99' }) };

    TestBed.configureTestingModule({
      imports: [
        RecipeDetailedComponent,
        TranslateModule.forRoot(),
        NoopAnimationsModule,
      ],
      providers: [
        { provide: ActivatedRoute, useValue: testActivatedRoute },
        { provide: AuthService, useValue: authServiceMock },
        { provide: RecipeService, useValue: recipeServiceMock },
        { provide: NotificationService, useValue: notificationServiceMock },
        { provide: Router, useValue: routerMock },
      ],
    });
    const testFixture = TestBed.createComponent(RecipeDetailedComponent);
    const testComponent = testFixture.componentInstance;
    testComponent.ngOnInit();

    expect(testComponent.recipeId).toBe(99);
  });
});
