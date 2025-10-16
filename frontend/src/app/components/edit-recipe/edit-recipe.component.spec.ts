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
import { EditRecipeComponent } from './edit-recipe.component';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { RecipeService } from '../../services/recipe.service';

describe('EditRecipeComponent', () => {
  let component: EditRecipeComponent;
  let fixture: ComponentFixture<EditRecipeComponent>;
  const notificationServiceMock = jasmine.createSpyObj(
    'NotificationService',
    ['error', 'warning', 'success'],
    {
      notifications$: of([]),
    }
  );

  const routerMock = jasmine.createSpyObj('Router', ['navigate']);
  const authServiceMock = jasmine.createSpyObj('AuthService', [
    'isAuthenticated',
  ]);
  const recipeServiceMock = jasmine.createSpyObj('RecipeService', [
    'loadRecipeDetails',
    'updateRecipe',
  ]);

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

  const mockIngredients = [
    {
      ingredientName: 'Ingredient 1',
      quantity: 10,
      unit: 'pcs',
      isOptional: false,
    },
    {
      ingredientName: 'Ingredient 2',
      quantity: 2,
      unit: 'g',
      isOptional: true,
    },
  ];

  const mockSteps = [{ content: 'Step 1' }, { content: 'Step 2' }];
  const mockPolishCategory = 'śniadanie';

  const mockImage = [
    { imageUrl: 'https://images.com/test-image.jpg', publicId: 1 },
  ];
  beforeEach(async () => {
    recipeServiceMock.loadRecipeDetails.and.returnValue(of(mockRecipe));
    await TestBed.configureTestingModule({
      imports: [
        EditRecipeComponent,
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
        { provide: Router, useValue: routerMock },
        { provide: AuthService, useValue: authServiceMock },
        { provide: NotificationService, useValue: notificationServiceMock },
        { provide: RecipeService, useValue: recipeServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EditRecipeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    const testFixture = TestBed.createComponent(EditRecipeComponent);
    const testComponent = testFixture.componentInstance;
    // Create separate component instance to test true default values
    // Main component instance runs ngOnInit() which triggers async HTTP calls,
    // causing flaky test (inconsistent) behavior due to timing issues
    expect(testComponent.isUpdating).toBe(false);
    expect(testComponent.isLoggedIn).toBe(false);
    expect(testComponent.isDragging).toBe(false);
    expect(testComponent.imagePreview).toBe(null);
    expect(testComponent.currentImageUrl).toBe(null);
    expect(testComponent.hasExistingImage).toBe(false);
    expect(testComponent.ingredients).toEqual([]);
    expect(testComponent.steps).toEqual([]);
    expect(testComponent.recipeForm.value).toEqual({
      title: '',
      category: '',
      servings: 1,
      prepareTime: 1,
      difficulty: 1,
      images: null,
      isPublic: false,
      language: 'english',
      ingredients: '',
      quantity: 0,
      unit: '',
      isOptional: false,
      steps: '',
    });
  });

  it('should authenticate user on init', () => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    component.ngOnInit();
    expect(component.isLoggedIn).toBe(true);
  });

  it('should redirect to not-found when unauthenticated', () => {
    authServiceMock.isAuthenticated.and.returnValue(false);
    component.ngOnInit();
    expect(routerMock.navigate).toHaveBeenCalledWith(['/not-found']);
    expect(component.isLoggedIn).toBe(false);
  });

  it('should parse recipeId from route params', () => {
    component.ngOnInit();
    expect(component.recipeId).toBe(1);
  });

  it('should handle missing recipeId and show error', () => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    TestBed.resetTestingModule();
    const testActivatedRoute = { params: of({ id: '' }) };

    TestBed.configureTestingModule({
      imports: [
        EditRecipeComponent,
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

    const testFixture = TestBed.createComponent(EditRecipeComponent);
    const testComponent = testFixture.componentInstance;
    testComponent.ngOnInit();

    expect(testComponent.recipeId).toBe(0);
    expect(routerMock.navigate).toHaveBeenCalledWith(['/not-found']);
  });

  it('should handle different route params', () => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    TestBed.resetTestingModule();
    const testActivatedRoute = { params: of({ id: '99' }) };

    TestBed.configureTestingModule({
      imports: [
        EditRecipeComponent,
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

    const testFixture = TestBed.createComponent(EditRecipeComponent);
    const testComponent = testFixture.componentInstance;
    testComponent.ngOnInit();

    expect(testComponent.recipeId).toBe(99);
  });

  it('should load recipe details successfully', fakeAsync(() => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    recipeServiceMock.loadRecipeDetails.and.returnValue(of(mockRecipe));

    component.ngOnInit();
    tick(1);

    expect(component.recipeForm.get('title')?.value).toBe('Test Recipe 1');
    expect(component.recipeForm.get('category')?.value).toBe('BREAKFAST');
    expect(component.recipeForm.get('servings')?.value).toBe(4);
    expect(component.recipeForm.get('prepareTime')?.value).toBe(30);
    expect(component.recipeForm.get('difficulty')?.value).toBe(2);
    expect(component.recipeForm.get('isPublic')?.value).toBe(true);
    expect(component.recipeForm.get('language')?.value).toBe('en');
  }));

  it('should show error when API Error', () => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    recipeServiceMock.loadRecipeDetails.and.returnValue(
      throwError('API Error')
    );

    component.ngOnInit();
    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Failed to load recipe details',
      5000
    );
  });

  it('should map ingredients correctly', () => {
    const recipeWithIngredients = {
      ...mockRecipe,
      ingredients: mockIngredients,
    };
    authServiceMock.isAuthenticated.and.returnValue(true);
    recipeServiceMock.loadRecipeDetails.and.returnValue(
      of(recipeWithIngredients)
    );

    component.ngOnInit();
    expect(component.ingredients).toEqual([
      {
        name: 'Ingredient 1',
        quantity: 10,
        unit: 'pcs',
        isOptional: false,
      },
      {
        name: 'Ingredient 2',
        quantity: 2,
        unit: 'g',
        isOptional: true,
      },
    ]);
    expect(component.ingredients.length).toBe(2);
  });

  it('should map steps correctly', () => {
    const recipeWithSteps = {
      ...mockRecipe,
      steps: mockSteps,
    };

    authServiceMock.isAuthenticated.and.returnValue(true);
    recipeServiceMock.loadRecipeDetails.and.returnValue(of(recipeWithSteps));

    component.ngOnInit();

    expect(component.steps).toEqual([
      { content: 'Step 1' },
      { content: 'Step 2' },
    ]);
    expect(component.steps.length).toBe(2);
  });

  it('should map category from polish to english', () => {
    const recipeWithPolishCategory = {
      ...mockRecipe,
      category: mockPolishCategory,
    };
    authServiceMock.isAuthenticated.and.returnValue(true);
    recipeServiceMock.loadRecipeDetails.and.returnValue(
      of(recipeWithPolishCategory)
    );

    component.ngOnInit();

    expect(component.recipeForm.get('category')?.value).toBe('BREAKFAST');
  });

  it('should handle exisitng image', () => {
    const recipeWithImage = {
      ...mockRecipe,
      images: mockImage,
    };
    authServiceMock.isAuthenticated.and.returnValue(true);
    recipeServiceMock.loadRecipeDetails.and.returnValue(of(recipeWithImage));

    component.ngOnInit();

    expect(component.currentImageUrl).toBe('https://images.com/test-image.jpg');
    expect(component.imagePreview).toBe('https://images.com/test-image.jpg');
    expect(component.hasExistingImage).toBe(true);
  });

  it('should handle recipes with images', () => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    recipeServiceMock.loadRecipeDetails.and.returnValue(of(mockRecipe));

    component.ngOnInit();
    expect(component.imagePreview).toBe(null);
    expect(component.currentImageUrl).toBe(null);
    expect(component.hasExistingImage).toBe(false);
  });

  it('should add ingredient successfully', () => {
    component.recipeForm.patchValue({
      ingredients: 'Test Ingredient 1',
      quantity: 1,
      unit: 'pcs',
      isOptional: false,
    });

    component.addIngredient();

    expect(component.ingredients.length).toBe(2); // tomato and test ingredient 1
    expect(component.ingredients).toContain({
      name: 'Test Ingredient 1',
      quantity: 1,
      unit: 'pcs',
      isOptional: false,
    });
    expect(component.recipeForm.get('ingredients')?.value).toBe('');
    expect(component.recipeForm.get('quantity')?.value).toBe(0);
  });

  it('should not add ingredient with missing data', () => {
    component.recipeForm.patchValue({
      ingredients: '',
      quantity: 0,
      unit: 'pcs',
      isOptional: true,
    });

    component.addIngredient();
    expect(notificationServiceMock.warning).toHaveBeenCalledWith(
      'Please fill in all fields for the ingredient.',
      5000
    );
  });

  it('should prevent duplicate ingredients', () => {
    component.recipeForm.patchValue({
      ingredients: 'Tomato',
      quantity: 5,
      unit: 'pcs',
      isOptional: true,
    });

    component.addIngredient();

    expect(notificationServiceMock.warning).toHaveBeenCalledWith(
      'This ingredient already exists in the recipe.',
      5000
    );
  });

  it('should remove ingredients by index', () => {
    component.recipeForm.patchValue({
      ingredients: 'Potato',
      quantity: 5,
      unit: 'pcs',
      isOptional: true,
    });
    component.addIngredient();
    component.removeIngredient(1);

    expect(component.ingredients.length).toBe(1); // Tomato
  });

  it('should toggle optional flag', () => {
    component.recipeForm.patchValue({
      isOptional: false,
    });

    component.toggleOptional();
    expect(component.recipeForm.get('isOptional')?.value).toBe(true);
    component.toggleOptional();
    expect(component.recipeForm.get('isOptional')?.value).toBe(false);
  });

  it('should add step successfully', () => {
    component.recipeForm.patchValue({
      steps: 'Step 1',
    });

    component.addStep();
    expect(component.steps.length).toBe(2);
    expect(component.steps).toContain({ content: 'Step 1' });
    expect(component.recipeForm.get('steps')?.value).toBe('');
  });

  it('should prevent emtpy steps', () => {
    component.recipeForm.patchValue({
      steps: '',
    });
    component.addStep();
    expect(notificationServiceMock.warning).toHaveBeenCalledWith(
      'Please fill in the step description.',
      5000
    );
  });

  it('should remove step by index', () => {
    component.removeStep(0);
    expect(component.steps.length).toBe(0); // removed tomato
  });

  it('should update recipe successfully', fakeAsync(() => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    recipeServiceMock.updateRecipe.and.returnValue(
      of({ ...mockRecipe, ingredients: mockIngredients, steps: mockSteps })
    );

    component.updateRecipe();
    tick(1000);

    expect(notificationServiceMock.success).toHaveBeenCalledWith(
      'Recipe updated successfully!',
      5000
    );
    expect(component.isUpdating).toBe(false);
  }));

  it('should prevent update with empty ingredients', fakeAsync(() => {
    notificationServiceMock.error.calls.reset();
    component.ingredients = [];
    component.steps = [{ content: 'Step 1' }];

    component.updateRecipe();
    tick(1000);

    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Please add at least one ingredient.',
      5000
    );
    expect(component.isUpdating).toBe(false);
  }));

  it('should prevent update with empty steps', fakeAsync(() => {
    notificationServiceMock.error.calls.reset();
    component.steps = [];
    component.ingredients = [
      {
        name: 'Potato',
        quantity: 10,
        unit: 'pcs',
        isOptional: false,
      },
    ];

    component.updateRecipe();
    tick(1000);

    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Please add at least one step.',
      5000
    );
    expect(component.isUpdating).toBe(false);
  }));

  it('should handle api errors', fakeAsync(() => {
    recipeServiceMock.updateRecipe.and.returnValue(throwError('API Error'));

    component.updateRecipe();
    tick(1000);
    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Failed to update recipe',
      5000
    );
    expect(component.isUpdating).toBe(false);
  }));

  it('should call loadRecipeDetails after update successfully', fakeAsync(() => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    recipeServiceMock.updateRecipe.and.returnValue(
      of({ ...mockRecipe, ingredients: mockIngredients, steps: mockSteps })
    );

    component.updateRecipe();
    tick(1000);

    expect(notificationServiceMock.success).toHaveBeenCalledWith(
      'Recipe updated successfully!',
      5000
    );
    expect(component.isUpdating).toBe(false);
    expect(recipeServiceMock.loadRecipeDetails).toHaveBeenCalled();
  }));
});
