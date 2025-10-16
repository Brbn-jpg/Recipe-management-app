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
import { AddRecipePanelComponent } from './add-recipe-panel.component';
import { AuthService } from '../../services/auth.service';
import { of, throwError } from 'rxjs';
import { RecipeService } from '../../services/recipe.service';
import { NotificationService } from '../../services/notification.service';
import { Router } from '@angular/router';

describe('AddRecipePanelComponent', () => {
  let component: AddRecipePanelComponent;
  let fixture: ComponentFixture<AddRecipePanelComponent>;
  const authServiceMock = jasmine.createSpyObj('AuthService', [
    'isAuthenticated',
  ]);
  const recipeServiceMock = jasmine.createSpyObj('RecipeService', [
    'postRecipe',
  ]);
  const notificationServiceMock = jasmine.createSpyObj(
    'NotificationService',
    ['error', 'success', 'warning'],
    {
      notifications$: of([]),
    }
  );
  const routerMock = jasmine.createSpyObj('Router', ['navigate']);

  const mockRecipe = {
    title: 'Test Recipe',
    category: 'breakfast',
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
    steps: [{ content: 'Cut the tomatoes into cubes' }],
    isPublic: true,
    language: 'en',
    images: null,
  };

  beforeEach(async () => {
    recipeServiceMock.postRecipe.and.returnValue(of(mockRecipe));
    await TestBed.configureTestingModule({
      imports: [
        AddRecipePanelComponent,
        TranslateModule.forRoot(),
        NoopAnimationsModule,
      ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceMock },
        { provide: RecipeService, useValue: recipeServiceMock },
        { provide: Router, useValue: routerMock },
        { provide: NotificationService, useValue: notificationServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AddRecipePanelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    const testFixture = TestBed.createComponent(AddRecipePanelComponent);
    const testComponent = testFixture.componentInstance;
    // Create separate component instance to test true default values
    // Main component instance runs ngOnInit() which triggers async HTTP calls,
    // causing flaky test (inconsistent) behavior due to timing issues
    expect(testComponent.ingredients).toEqual([]);
    expect(testComponent.recipeLanguage).toBe('english');
    expect(testComponent.isPublic).toBe(false);
    expect(testComponent.newIngredient).toEqual({
      ingredientName: '',
      quantity: 0,
      unit: '',
      isOptional: false,
    });
    expect(testComponent.newStep).toBe('');
    expect(testComponent.success).toBe(false);
    expect(testComponent.isDragging).toBe(false);
    expect(testComponent.imagePreview).toBe(null);
    expect(testComponent.isLoggedIn).toBe(false);
  });

  it('should authenticate user on init', () => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    component.ngOnInit();
    expect(component.isLoggedIn).toBe(true);
    expect(authServiceMock.isAuthenticated).toHaveBeenCalled();
  });

  it('should redirect to not-found when unauthenticated', () => {
    authServiceMock.isAuthenticated.and.returnValue(false);
    component.ngOnInit();
    expect(component.isLoggedIn).toBe(false);
    expect(authServiceMock.isAuthenticated).toHaveBeenCalled();
    expect(routerMock.navigate).toHaveBeenCalledWith(['/not-found']);
  });

  it('should add ingredient successfully', () => {
    component.newIngredient = {
      ingredientName: 'Tomato',
      quantity: 2,
      unit: 'pcs',
      isOptional: false,
    };
    component.addIngredient();
    expect(component.ingredients.length).toBe(1);
    expect(component.ingredients).toEqual([
      { ingredientName: 'Tomato', quantity: 2, unit: 'pcs', isOptional: false },
    ]);
    expect(component.newIngredient).toEqual({
      ingredientName: '',
      quantity: 0,
      unit: '',
      isOptional: false,
    });
  });

  it('should prevent duplicate ingredients', () => {
    component.ingredients = [
      {
        ingredientName: 'Tomato',
        quantity: 2,
        unit: 'g',
        isOptional: false,
      },
    ];
    component.newIngredient = {
      ingredientName: 'TOMATO',
      quantity: 2,
      unit: 'g',
      isOptional: true,
    };
    component.addIngredient();

    expect(component.ingredients.length).toBe(1);
    expect(component.ingredients).toEqual([
      { ingredientName: 'Tomato', quantity: 2, unit: 'g', isOptional: false },
    ]);
    expect(notificationServiceMock.warning).toHaveBeenCalledWith(
      'This ingredient already exists!',
      5000
    );
  });

  it('should show warning for empty ingredient name', () => {
    component.newIngredient = {
      ingredientName: '',
      quantity: 123,
      unit: 'g',
      isOptional: false,
    };

    component.addIngredient();
    expect(notificationServiceMock.warning).toHaveBeenCalledWith(
      'Fill all fields to add ingredient',
      5000
    );
  });

  it('should show warning for 0 quantity', () => {
    component.newIngredient = {
      ingredientName: 'Tomato',
      quantity: 0,
      unit: 'g',
      isOptional: false,
    };

    component.addIngredient();
    expect(notificationServiceMock.warning).toHaveBeenCalledWith(
      'Fill all fields to add ingredient',
      5000
    );
  });

  it('should remove ingredient correctly', () => {
    component.ingredients = [
      {
        ingredientName: 'Tomato',
        quantity: 5,
        unit: 'g',
        isOptional: false,
      },
    ];
    component.removeIngredient(0);
    expect(component.ingredients.length).toBe(0);
  });

  it('should toggle ingredient optional flag', () => {
    component.newIngredient = {
      ingredientName: 'Tomato',
      quantity: 2,
      unit: 'pcs',
      isOptional: false,
    };
    component.toggleOptional();
    expect(component.newIngredient.isOptional).toBe(true);
    component.toggleOptional();
    expect(component.newIngredient.isOptional).toBe(false);
  });

  it('should add step successfully', () => {
    component.newStep = 'Cut the tomato';
    component.addStep();
    expect(component.steps.length).toBe(1);
    expect(component.steps).toEqual([{ content: 'Cut the tomato' }]);
  });

  it('should show warning with empty step', () => {
    component.newStep = '';
    component.addStep();
    expect(notificationServiceMock.warning).toHaveBeenCalledWith(
      'Please enter a step',
      5000
    );
  });

  it('should remove step correctly', () => {
    component.steps = [{ content: 'Step 1' }, { content: 'Step 2' }];
    component.removeStep(1);
    expect(component.steps.length).toBe(1);
    expect(component.steps).toEqual([{ content: 'Step 1' }]);
  });

  it('should submit recipe successfully', fakeAsync(() => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    recipeServiceMock.postRecipe.and.returnValue(of(mockRecipe));

    component.recipeForm.markAsDirty();
    component.recipeForm.markAsTouched();

    component.ingredients = [
      { ingredientName: 'Test', quantity: 1, unit: 'piece', isOptional: false },
    ];
    component.steps = [{ content: 'Test step' }];

    component.postRecipe();
    tick(1200);

    expect(recipeServiceMock.postRecipe).toHaveBeenCalled();
    expect(notificationServiceMock.success).toHaveBeenCalledWith(
      'Recipe has been created',
      5000
    );
    expect(component.isSubmitting).toBe(false);
  }));

  it('should handle api error and show notification', fakeAsync(() => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    recipeServiceMock.postRecipe.and.returnValue(throwError('API Error'));

    component.recipeForm.markAsDirty();
    component.recipeForm.markAsTouched();

    component.ingredients = [
      { ingredientName: 'Test', quantity: 1, unit: 'piece', isOptional: false },
    ];
    component.steps = [{ content: 'Test step' }];

    component.postRecipe();
    tick(1200);

    expect(recipeServiceMock.postRecipe).toHaveBeenCalled();
    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Failed to add the recipe!',
      5000
    );
    expect(component.isSubmitting).toBe(false);
  }));

  it('should prevent posting recipe when unauthenticated', fakeAsync(() => {
    authServiceMock.isAuthenticated.and.returnValue(false);
    recipeServiceMock.postRecipe.and.returnValue(of(mockRecipe));

    component.recipeForm.markAsDirty();
    component.recipeForm.markAsTouched();

    component.ingredients = [
      { ingredientName: 'Test', quantity: 1, unit: 'piece', isOptional: false },
    ];
    component.steps = [{ content: 'Test step' }];

    component.postRecipe();
    tick(1200);

    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'User is not logged in!',
      5000
    );
  }));

  it('should prevent recipe submittion when ingredients are empty', fakeAsync(() => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    recipeServiceMock.postRecipe.and.returnValue(of(mockRecipe));

    component.recipeForm.markAsDirty();
    component.recipeForm.markAsTouched();

    component.ingredients = [];
    component.steps = [{ content: 'Test step' }];
    component.postRecipe();
    tick(1200);

    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Please add at least one ingredient!',
      5000
    );
  }));

  it('should prevent recipe submittion when steps are empty', fakeAsync(() => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    recipeServiceMock.postRecipe.and.returnValue(of(mockRecipe));

    component.recipeForm.markAsDirty();
    component.recipeForm.markAsTouched();

    component.ingredients = [
      { ingredientName: 'Test', quantity: 1, unit: 'piece', isOptional: false },
    ];
    component.steps = [];
    component.postRecipe();
    tick(1200);

    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Please add at least one step!',
      5000
    );
  }));

  it('should prevent recipe submittion with empty fields', fakeAsync(() => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    recipeServiceMock.postRecipe.and.returnValue(of(mockRecipe));

    component.recipeForm.markAsPristine();
    component.recipeForm.markAsUntouched();

    component.ingredients = [
      { ingredientName: 'Test', quantity: 1, unit: 'piece', isOptional: false },
    ];
    component.steps = [{ content: 'Test step' }];

    component.postRecipe();
    tick(1200);

    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Please fill in the form!',
      5000
    );
  }));
});
