import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { provideRouter, Router } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RecipesComponent } from './recipes.component';
import { BehaviorSubject, of, throwError } from 'rxjs';
import { FilterService } from '../../services/filter.service';
import { NotificationService } from '../../services/notification.service';
import { LanguageService } from '../../services/language.service';

describe('RecipesComponent', () => {
  let component: RecipesComponent;
  let fixture: ComponentFixture<RecipesComponent>;
  let filterServiceMock: jasmine.SpyObj<FilterService>;

  const routerMock = jasmine.createSpyObj('Router', ['navigate']);
  const notificationServiceMock = jasmine.createSpyObj(
    'notificationService',
    ['error'],
    {
      notifications$: of([]),
    }
  );

  beforeEach(async () => {
    filterServiceMock = jasmine.createSpyObj(
      'filterService',
      ['loadRecipes', 'updateFilters', 'loadLanguages', 'loadIngredients'],
      {
        currentFilters: {},
        filters$: of({ currentPage: 1 }),
      }
    );

    const languageServiceMock = jasmine.createSpyObj(
      'LanguageService',
      ['setLanguage'],
      {
        language$: of('en'),
      }
    );
    const defaultResponse = {
      content: [],
      images: [],
      totalPages: 1,
    };
    filterServiceMock.loadRecipes.and.returnValue(of(defaultResponse));
    filterServiceMock.loadLanguages.and.returnValue(of([]));
    filterServiceMock.loadIngredients.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [
        RecipesComponent,
        TranslateModule.forRoot(),
        NoopAnimationsModule,
      ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: Router, useValue: routerMock },
        { provide: FilterService, useValue: filterServiceMock },
        { provide: NotificationService, useValue: notificationServiceMock },
        { provide: LanguageService, useValue: languageServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RecipesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initalize with default valeus', () => {
    const testFixture = TestBed.createComponent(RecipesComponent);
    const testComponent = testFixture.componentInstance;
    // Create separate component instance to test true default values
    // Main component instance runs ngOnInit() which triggers async HTTP calls,
    // causing flaky test (inconsistent) behavior due to timing issues
    expect(testComponent.isFiltering).toBe(false);
    expect(testComponent.language).toBe('en');
    expect(testComponent.recipesArray).toEqual([]);
    expect(testComponent.totalPages).toBe(1);
    expect(testComponent.currentPage).toBe(1);
  });

  it('should load recipes successfuly', () => {
    const mockResponse = {
      content: [
        {
          id: 1,
          recipeId: 1,
          recipeName: 'Test Recipe 1',
          category: 'Main Course',
          difficulty: 2,
          prepareTime: 30,
          servings: 4,
          isPublic: true,
          language: 'en',
          ingredients: [
            { ingredientName: 'Tomato', quantity: 2, unit: 'pieces' },
          ],
          steps: [{ content: 'Chop tomatoes' }],
          images: [],
          ratings: [{ ratingId: 1, value: 5 }],
        },
        {
          id: 2,
          recipeId: 2,
          recipeName: 'Test Recipe 2',
          category: 'Dessert',
          difficulty: 3,
          prepareTime: 45,
          servings: 2,
          isPublic: true,
          language: 'en',
          ingredients: [
            { ingredientName: 'Sugar', quantity: 100, unit: 'grams' },
          ],
          steps: [{ content: 'Mix sugar' }],
          images: [],
          ratings: [],
        },
        {
          id: 3,
          recipeId: 3,
          recipeName: 'Private Recipe',
          category: 'Snack',
          difficulty: 1,
          prepareTime: 15,
          servings: 6,
          isPublic: false,
          language: 'en',
          ingredients: [],
          steps: [{ content: 'Toast bread' }],
          images: [],
          ratings: [],
        },
      ],
      images: [],
      totalPages: 3,
    };

    filterServiceMock.loadRecipes.and.returnValue(of(mockResponse));

    component.loadRecipes();
    expect(component.recipesArray.length).toBe(2);
    expect(component.totalPages).toBe(3);
    expect(component.recipesArray[0].isPublic).toBe(true);
  });

  it('should call loadRecipes when calling onFiltersChange', () => {
    component.onFiltersChanged();
    expect(filterServiceMock.loadRecipes).toHaveBeenCalled();
  });

  it('should throw error when failed to load recipes', () => {
    filterServiceMock.loadRecipes.and.returnValue(throwError('API Error'));

    component.loadRecipes();
    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Failed to load recipes. Please try again later.',
      5000
    );
    expect(component.recipesArray).toEqual([]);
  });

  it('should call updateFilters and loadRecipes when calling onPageChange', () => {
    const mockResponse = {
      content: [
        {
          id: 1,
          recipeId: 1,
          recipeName: 'Test Recipe 1',
          category: 'Main Course',
          difficulty: 2,
          prepareTime: 30,
          servings: 4,
          isPublic: true,
          language: 'en',
          ingredients: [
            { ingredientName: 'Tomato', quantity: 2, unit: 'pieces' },
          ],
          steps: [{ content: 'Chop tomatoes' }],
          images: [],
          ratings: [{ ratingId: 1, value: 5 }],
        },
      ],
      images: [],
      totalPages: 3,
    };
    filterServiceMock.loadRecipes.and.returnValue(of(mockResponse));
    const scrollSpy = spyOn(window, 'scrollTo');

    component.onPageChange(2);
    expect(scrollSpy).toHaveBeenCalledTimes(1);
    expect(filterServiceMock.updateFilters).toHaveBeenCalledWith({
      currentPage: 2,
    });
    expect(filterServiceMock.loadRecipes).toHaveBeenCalled();
  });

  it('should filter out private recipes and show only public ones', () => {
    const mockResponse = {
      content: [
        {
          id: 1,
          recipeId: 1,
          recipeName: 'Test Recipe 1',
          category: 'breakfast',
          difficulty: 2,
          prepareTime: 30,
          servings: 4,
          isPublic: true,
          language: 'en',
          ingredients: [{ ingredientName: 'Tomato', quantity: 2, unit: 'pcs' }],
          steps: [{ content: 'Chop tomatoes' }],
          images: [],
          ratings: [{ ratingId: 1, value: 5 }],
        },
        {
          id: 2,
          recipeId: 2,
          recipeName: 'Test Recipe 2',
          category: 'dinner',
          difficulty: 2,
          prepareTime: 30,
          servings: 4,
          isPublic: false,
          language: 'pl',
          ingredients: [{ ingredientName: 'Tomato', quantity: 2, unit: 'pcs' }],
          steps: [{ content: 'Chop tomatoes' }],
          images: [],
          ratings: [{ ratingId: 1, value: 5 }],
        },
      ],
      images: [],
      totalPages: 3,
    };

    filterServiceMock.loadRecipes.and.returnValue(of(mockResponse));

    component.loadRecipes();
    expect(component.recipesArray.length).toBe(1);
    expect(component.recipesArray[0].isPublic).toBe(true);
    expect(
      component.recipesArray.some(
        (recipe) => recipe.recipeName === 'Test Recipe 2'
      )
    ).toBe(false);
  });

  it('should set isFiltering to true when screen width <=1350px', () => {
    Object.defineProperty(window, 'innerWidth', {
      value: 1200,
    });
    component.onResize();
    expect(component.isFiltering).toBe(true);
  });

  it('should set isFiltering to false when screen width <=1350px', () => {
    Object.defineProperty(window, 'innerWidth', {
      value: 1400,
    });
    component.onResize();
    expect(component.isFiltering).toBe(false);
  });

  it('should detect language change', () => {
    component.language = 'pl';
    expect(component.language).toBe('pl');
  });
});
