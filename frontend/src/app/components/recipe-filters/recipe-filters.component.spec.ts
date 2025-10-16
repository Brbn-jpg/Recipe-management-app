import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { RecipeFiltersComponent } from './recipe-filters.component';
import { of, throwError } from 'rxjs';
import { NotificationService } from '../../services/notification.service';
import { FilterService } from '../../services/filter.service';

describe('RecipeFiltersComponent', () => {
  const filterServiceMock = jasmine.createSpyObj(
    'FilterService',
    ['loadLanguages', 'loadIngredients', 'updateFilters'],
    { currentFilters: of() }
  );

  const notificationServiceMock = jasmine.createSpyObj(
    'NotificationService',
    ['error'],
    {
      notifications$: of([]),
    }
  );
  let component: RecipeFiltersComponent;
  let fixture: ComponentFixture<RecipeFiltersComponent>;

  beforeEach(async () => {
    filterServiceMock.loadLanguages.and.returnValue(of([]));
    filterServiceMock.loadIngredients.and.returnValue(of([]));
    await TestBed.configureTestingModule({
      imports: [RecipeFiltersComponent, TranslateModule.forRoot()],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: NotificationService, useValue: notificationServiceMock },
        { provide: FilterService, useValue: filterServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RecipeFiltersComponent);
    component = fixture.componentInstance;

    component.selectedIngredients = [];
    component.searchQuery = '';
    component.prepTimeFrom = undefined;
    component.prepTimeTo = undefined;
    component.servingsFrom = undefined;
    component.servingsTo = undefined;
    component.selectedCategory = '';
    component.selectedDifficulty = '';
    component.selectedLanguage = '';

    fixture.detectChanges();
  });

  afterEach(() => {
    // complete reset to get rid of flakey tests
    component.selectedIngredients = [];
    Object.defineProperty(filterServiceMock, 'currentFilters', {
      value: {},
      writable: true,
      configurable: true,
    });
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    const testFixture = TestBed.createComponent(RecipeFiltersComponent);
    const testComponent = testFixture.componentInstance;
    // Create separate component instance to test true default values
    // Main component instance runs ngOnInit() which triggers async HTTP calls,
    // causing flaky test (inconsistent) behavior due to timing issues
    expect(testComponent.searchQuery).toBe('');
    expect(testComponent.prepTimeFrom).toBe(undefined);
    expect(testComponent.prepTimeTo).toBe(undefined);
    expect(testComponent.servingsFrom).toBe(undefined);
    expect(testComponent.servingsTo).toBe(undefined);
    expect(testComponent.selectedCategory).toBe('');
    expect(testComponent.selectedDifficulty).toBe('');
    expect(testComponent.selectedLanguage).toBe('');
    expect(testComponent.selectedIngredients).toEqual([]);
    expect(testComponent.languagesArray).toEqual([]);
    expect(testComponent.ingredientsArray).toEqual([]);
  });

  it('should load languagaes and ingredients when useCustomData is false', () => {
    component.useCustomData = false;
    const loadLanguagesSpy = spyOn(component as any, 'loadLanguages');
    const loadIngredientsSpy = spyOn(component as any, 'loadIngredients');
    const loadCurrentFiltersSpy = spyOn(component as any, 'loadCurrentFilters');

    component.ngOnInit();

    expect(loadLanguagesSpy).toHaveBeenCalled();
    expect(loadIngredientsSpy).toHaveBeenCalled();
    expect(loadCurrentFiltersSpy).toHaveBeenCalled();
  });

  it('should use custom data when useCustomData is true', () => {
    component.useCustomData = true;
    component.customCategories = ['breakfast', 'dinner', 'lunch'];
    component.customLanguages = [
      { languageName: 'polish' },
      { languageName: 'english' },
    ];
    component.customIngredients = ['tomato', 'potato'];
    const loadCurrentFiltersFromCustomSpy = spyOn(
      component as any,
      'loadCurrentFiltersFromCustom'
    );

    component.ngOnInit();

    expect(loadCurrentFiltersFromCustomSpy).toHaveBeenCalled();
  });

  it('should complete destroy$ to prevent memory leaks', () => {
    const destroySubject = (component as any).destroy$;
    const nextSpy = spyOn(destroySubject, 'next');
    const completeSpy = spyOn(destroySubject, 'complete');

    component.ngOnDestroy();

    expect(nextSpy).toHaveBeenCalled();
    expect(completeSpy).toHaveBeenCalled();
  });

  it('onFilterChange should call filterChange$.next', () => {
    const filterSubject = (component as any).filterChange$;
    const nextSpy = spyOn(filterSubject, 'next');

    component.onFilterChange();

    expect(nextSpy).toHaveBeenCalled();
  });

  it('onFilterChange should not modify component properties', () => {
    const beforeState = {
      searchQuery: component.searchQuery,
      prepTimeFrom: component.prepTimeFrom,
      prepTimeTo: component.prepTimeTo,
      servingsFrom: component.servingsFrom,
      servingsTo: component.servingsTo,
      selectedCategory: component.selectedCategory,
      selectedDifficulty: component.selectedDifficulty,
      selectedIngredients: [...component.selectedIngredients],
    };

    component.onFilterChange();

    expect(component.searchQuery).toBe(beforeState.searchQuery);
    expect(component.prepTimeFrom).toBe(beforeState.prepTimeFrom);
    expect(component.prepTimeTo).toBe(beforeState.prepTimeTo);
    expect(component.servingsFrom).toBe(beforeState.servingsFrom);
    expect(component.servingsTo).toBe(beforeState.servingsTo);
    expect(component.selectedCategory).toBe(beforeState.selectedCategory);
    expect(component.selectedDifficulty).toBe(beforeState.selectedDifficulty);
    expect(component.selectedIngredients).toEqual(
      beforeState.selectedIngredients
    );
  });

  it('onSearchChange should call filterChange$.next', () => {
    const filterSubject = (component as any).filterChange$;
    const nextSpy = spyOn(filterSubject, 'next');

    component.onSearchChange();

    expect(nextSpy).toHaveBeenCalled();
  });

  it('onSearchChange should not modify component properties', () => {
    const beforeState = {
      searchQuery: component.searchQuery,
      prepTimeFrom: component.prepTimeFrom,
      prepTimeTo: component.prepTimeTo,
      servingsFrom: component.servingsFrom,
      servingsTo: component.servingsTo,
      selectedCategory: component.selectedCategory,
      selectedDifficulty: component.selectedDifficulty,
      selectedIngredients: [...component.selectedIngredients],
    };

    component.onSearchChange();

    expect(component.searchQuery).toBe(beforeState.searchQuery);
    expect(component.prepTimeFrom).toBe(beforeState.prepTimeFrom);
    expect(component.prepTimeTo).toBe(beforeState.prepTimeTo);
    expect(component.servingsFrom).toBe(beforeState.servingsFrom);
    expect(component.servingsTo).toBe(beforeState.servingsTo);
    expect(component.selectedCategory).toBe(beforeState.selectedCategory);
    expect(component.selectedDifficulty).toBe(beforeState.selectedDifficulty);
    expect(component.selectedIngredients).toEqual(
      beforeState.selectedIngredients
    );
  });

  it('should load all ingredients when useCustomData is false', () => {
    component.useCustomData = false;
    const loadIngredientsSpy = spyOn(component as any, 'loadIngredients');
    component.onLanguageChange();

    expect(loadIngredientsSpy).toHaveBeenCalled();
  });

  it('should load language based ingredients when useCustom data is true', () => {
    component.useCustomData = true;
    const filterSubject = (component as any).filterChange$;
    const nextSpy = spyOn(filterSubject, 'next');

    component.onLanguageChange();

    expect(nextSpy).toHaveBeenCalled();
    expect(component.selectedIngredients).toEqual([]);
  });

  it('should add ingredient to selectedIngredients when isChecked is true', () => {
    const filterSubject = (component as any).filterChange$;
    const nextSpy = spyOn(filterSubject, 'next');
    component.onIngredientChange('potato', true);

    expect(component.selectedIngredients).toEqual(['potato']);
    expect(nextSpy).toHaveBeenCalled();
  });

  it('should remove ingredient from selectedIngredients when isChecked is false', () => {
    const filterSubject = (component as any).filterChange$;
    const nextSpy = spyOn(filterSubject, 'next');
    component.onIngredientChange('potato', false);

    expect(component.selectedIngredients).toEqual([]);
    expect(nextSpy).toHaveBeenCalled();
  });

  it('should handle non-existent ingredients without problems', () => {
    component.selectedIngredients = ['potato', 'tomato'];
    component.onIngredientChange('onion', false);

    expect(component.selectedIngredients).toEqual(['potato', 'tomato']);
  });

  it('should return true when ingredient is selected', () => {
    component.selectedIngredients = ['potato', 'tomato'];

    expect(component.isIngredientSelected('potato')).toBe(true);
  });

  it('should return false when ingredient is not selected', () => {
    component.selectedIngredients = ['potato', 'tomato'];

    expect(component.isIngredientSelected('onion')).toBe(false);
  });

  it('should return false when selectedIngredients is empty', () => {
    component.selectedIngredients = [];

    expect(component.isIngredientSelected('onion')).toBe(false);
  });

  it('should handle empty string ingredient', () => {
    component.selectedIngredients = ['tomato', ''];

    expect(component.isIngredientSelected('')).toBe(true);
  });

  it('should load languages successfully', () => {
    filterServiceMock.loadLanguages.and.returnValue(
      of([{ languageName: 'polish' }, { languageName: 'english' }])
    );

    component.ngOnInit();

    expect(component.languagesArray).toEqual([
      { languageName: 'polish' },
      { languageName: 'english' },
    ]);
  });

  it('should handle loadLanguage errors', () => {
    filterServiceMock.loadLanguages.and.returnValue(throwError('API Error'));

    component.ngOnInit();

    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Failed to load languages',
      5000
    );
  });

  it('should load ingredients successfully', () => {
    filterServiceMock.loadIngredients.and.returnValue(
      of(['onion', 'potato', 'tomato'])
    );

    component.ngOnInit();

    expect(component.ingredientsArray).toEqual(['onion', 'potato', 'tomato']);
  });

  it('should handle loadIngredients errors', () => {
    filterServiceMock.loadIngredients.and.returnValue(throwError('API Error'));

    component.ngOnInit();

    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Failed to load ingredients',
      5000
    );
  });

  it('should loadCurrentFilters successfully', () => {
    component.useCustomData = false;
    Object.defineProperty(filterServiceMock, 'currentFilters', {
      value: {
        query: 'banana',
        prepTimeFrom: 5,
        prepTimeTo: 25,
        servingsFrom: 1,
        servingsTo: 4,
        category: 'DESERT',
        difficulty: 1,
        recipeLanguage: 'polish',
        ingredients: ['banana', 'sugar'],
      },
    });

    (component as any).loadCurrentFilters();

    expect(component.searchQuery).toBe('banana');
    expect(component.prepTimeFrom).toBe(5);
    expect(component.prepTimeTo).toBe(25);
    expect(component.servingsFrom).toBe(1);
    expect(component.servingsTo).toBe(4);
    expect(component.selectedCategory).toBe('DESERT');
    expect(component.selectedDifficulty).toBe('1');
    expect(component.selectedLanguage).toBe('polish');
    expect(component.selectedIngredients).toEqual(['banana', 'sugar']);
  });

  it('should updateFiltersFromParent with recieved data', () => {
    const recievedData = {
      difficulty: 1,
      prepTimeFrom: 10,
      prepTimeTo: 15,
      servingsFrom: 2,
      servingsTo: 4,
      category: 'DINNER',
      recipeLanguage: 'polish',
      query: 'banana',
      ingredients: ['banana', 'sugar'],
      currentPage: 1,
      pageSize: 12,
    };
    const filterSubject = (component as any).filterChange$;
    const nextSpy = spyOn(filterSubject, 'next');

    component.updateFiltersFromParent(recievedData);

    expect(nextSpy).toHaveBeenCalled();
    expect(component.searchQuery).toBe('banana');
    expect(component.prepTimeFrom).toBe(10);
    expect(component.prepTimeTo).toBe(15);
    expect(component.servingsFrom).toBe(2);
    expect(component.servingsTo).toBe(4);
    expect(component.selectedCategory).toBe('DINNER');
    expect(component.selectedDifficulty).toBe('1');
    expect(component.selectedLanguage).toBe('polish');
    expect(component.selectedIngredients).toEqual(['banana', 'sugar']);
  });

  it('should getCorrectFilters successfully', () => {
    component.selectedDifficulty = '1';
    component.prepTimeFrom = 10;
    component.prepTimeTo = 15;
    component.servingsFrom = 2;
    component.servingsTo = 4;
    component.selectedCategory = 'DINNER';
    component.selectedLanguage = 'polish';
    component.searchQuery = 'banana';
    component.selectedIngredients = ['banana', 'sugar'];

    const result = component.getCurrentFilters();

    expect(result).toEqual({
      query: 'banana',
      prepTimeFrom: 10,
      prepTimeTo: 15,
      servingsFrom: 2,
      servingsTo: 4,
      category: 'DINNER',
      difficulty: 1,
      recipeLanguage: 'polish',
      ingredients: ['banana', 'sugar'],
      currentPage: 1,
    });
  });

  it('should update categoriesArray when customCategories changes', () => {
    component.customCategories = ['breakfast', 'lunch', 'dinner'];

    component.ngOnChanges({
      customCategories: {
        currentValue: ['breakfast', 'lunch', 'dinner'],
        previousValue: [],
        firstChange: false,
        isFirstChange: () => false,
      },
    });

    expect(component.categoriesArray).toEqual([
      { key: 'BREAKFAST', value: 'breakfast' },
      { key: 'LUNCH', value: 'lunch' },
      { key: 'DINNER', value: 'dinner' },
    ]);
  });

  it('should update languagesArray when customLanguages changes', () => {
    const customLangs = [
      { languageName: 'english' },
      { languageName: 'polish' },
    ];

    component.customLanguages = customLangs;

    component.ngOnChanges({
      customLanguages: {
        currentValue: customLangs,
        previousValue: [],
        firstChange: false,
        isFirstChange: () => false,
      },
    });

    expect(component.languagesArray).toEqual(customLangs);
  });

  it('should update ingredientsArray when customIngredients changes', () => {
    const customIngr = ['potato', 'tomato'];
    component.customIngredients = customIngr;

    component.ngOnChanges({
      customIngredients: {
        currentValue: customIngr,
        previousValue: [],
        firstChange: false,
        isFirstChange: () => false,
      },
    });

    expect(component.ingredientsArray).toEqual(customIngr);
  });
});
