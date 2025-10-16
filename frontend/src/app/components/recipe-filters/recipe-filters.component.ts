import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  inject,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { Subject, takeUntil, debounceTime } from 'rxjs';
import { Language } from '../../Interface/language';
import { NotificationService } from '../../services/notification.service';
import { ScrollLockService } from '../../services/scroll-lock.service';
import { FilterService } from '../../services/filter.service';
import { FilterState } from '../../Interface/filter-state';
import { FormsModule } from '@angular/forms';
import { NgTemplateOutlet } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-recipe-filters',
  standalone: true,
  imports: [FormsModule, NgTemplateOutlet, TranslateModule],
  templateUrl: './recipe-filters.component.html',
  styleUrl: './recipe-filters.component.css',
})
export class RecipeFiltersComponent implements OnInit, OnDestroy, OnChanges {
  @Input() isMobileFiltering = false;
  @Input() showTabs = false; // input for showing tabs
  @Input() activeTab: 'favourites' | 'userRecipes' | null = null; // input for active tab
  @Input() customCategories: string[] = []; //  input for custom categories
  @Input() customLanguages: Language[] = []; //  input for custom languages
  @Input() customIngredients: string[] = []; //  input for custom ingredients
  @Input() useCustomData = false; //  input to determine data source
  @Input() showIngredients = true; //  input to show/hide ingredients section
  @Output() filtersChanged = new EventEmitter<void>();
  @Output() tabChanged = new EventEmitter<'favourites' | 'userRecipes'>(); // output for tab changes

  private destroy$ = new Subject<void>();
  private el: ElementRef = inject(ElementRef);
  private filterChange$ = new Subject<void>();

  searchQuery = '';
  prepTimeFrom?: number;
  prepTimeTo?: number;
  servingsFrom?: number;
  servingsTo?: number;
  selectedCategory = '';
  selectedDifficulty: string = '';
  selectedLanguage = '';
  selectedIngredients: string[] = [];

  // Categories with translation keys
  categoriesArray: { key: string; value: string }[] = [
    { key: 'BREAKFAST', value: 'BREAKFAST' },
    { key: 'LUNCH', value: 'LUNCH' },
    { key: 'DINNER', value: 'DINNER' },
    { key: 'DESSERT', value: 'DESSERT' },
    { key: 'SNACK', value: 'SNACK' },
    { key: 'DRINK', value: 'DRINK' },
    { key: 'SALAD', value: 'SALAD' },
    { key: 'SOUP', value: 'SOUP' },
  ];
  languagesArray: Language[] = [];
  ingredientsArray: string[] = [];

  isMenuOpen = false;

  constructor(
    private filterService: FilterService,
    private notificationService: NotificationService,
    private scrollLockService: ScrollLockService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    // Setup debounced filter updates
    this.filterChange$
      .pipe(debounceTime(300), takeUntil(this.destroy$))
      .subscribe(() => {
        this.executeFilterUpdate();
      });

    if (this.useCustomData) {
      // For custom usage, load custom categories and languages
      this.categoriesArray = this.customCategories.map((cat) => ({
        key: cat.toUpperCase(),
        value: cat,
      }));
      this.languagesArray = this.customLanguages;
      this.ingredientsArray = this.customIngredients;
      this.loadCurrentFiltersFromCustom();
    } else {
      this.loadLanguages();
      this.loadIngredients();
      this.loadCurrentFilters();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['customLanguages'] && this.customLanguages) {
      this.languagesArray = this.customLanguages;
      this.cdr.detectChanges();
    }

    if (changes['customCategories'] && this.customCategories) {
      // Convert custom categories to new format
      this.categoriesArray = this.customCategories.map((cat) => ({
        key: cat.toUpperCase(),
        value: cat,
      }));
      this.cdr.detectChanges();
    }

    if (changes['customIngredients'] && this.customIngredients) {
      this.ingredientsArray = this.customIngredients;
      this.cdr.detectChanges();
    }
  }

  private loadLanguages(): void {
    this.filterService
      .loadLanguages()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (languages) => {
          this.languagesArray = languages;
        },
        error: () => {
          this.notificationService.error('Failed to load languages', 5000);
        },
      });
  }

  private loadIngredients(): void {
    this.filterService
      .loadIngredients(this.selectedLanguage)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (ingredients) => {
          this.ingredientsArray = ingredients;
        },
        error: () => {
          this.notificationService.error('Failed to load ingredients', 5000);
        },
      });
  }

  private loadCurrentFilters(): void {
    const currentFilters = this.filterService.currentFilters;

    this.searchQuery = currentFilters.query || '';
    this.prepTimeFrom = currentFilters.prepTimeFrom;
    this.prepTimeTo = currentFilters.prepTimeTo;
    this.servingsFrom = currentFilters.servingsFrom;
    this.servingsTo = currentFilters.servingsTo;
    this.selectedCategory = currentFilters.category || '';
    this.selectedDifficulty = currentFilters.difficulty
      ? currentFilters.difficulty.toString()
      : ''; // Convert to string for select binding
    this.selectedLanguage = currentFilters.recipeLanguage || '';
    this.selectedIngredients = currentFilters.ingredients || [];
  }

  private loadCurrentFiltersFromCustom(): void {
    // Reset filters when using custom data
    this.searchQuery = '';
    this.prepTimeFrom = undefined;
    this.prepTimeTo = undefined;
    this.servingsFrom = undefined;
    this.servingsTo = undefined;
    this.selectedCategory = '';
    this.selectedDifficulty = '';
    this.selectedLanguage = '';
    this.selectedIngredients = [];
  }

  // Method to update filters from parent component (for custom usage)
  updateFiltersFromParent(filters: Partial<FilterState>): void {
    this.searchQuery = filters.query || '';
    this.prepTimeFrom = filters.prepTimeFrom;
    this.prepTimeTo = filters.prepTimeTo;
    this.servingsFrom = filters.servingsFrom;
    this.servingsTo = filters.servingsTo;
    this.selectedCategory = filters.category || '';
    this.selectedDifficulty = filters.difficulty
      ? filters.difficulty.toString()
      : '';
    this.selectedLanguage = filters.recipeLanguage || '';
    this.selectedIngredients = filters.ingredients || [];

    // Trigger debounced update
    this.updateFilters();
  }

  // Method to get current filters (for custom usage)
  getCurrentFilters(): Partial<FilterState> {
    return {
      query: this.searchQuery,
      prepTimeFrom: this.prepTimeFrom,
      prepTimeTo: this.prepTimeTo,
      servingsFrom: this.servingsFrom,
      servingsTo: this.servingsTo,
      category: this.selectedCategory || undefined,
      difficulty:
        this.selectedDifficulty === '' ? undefined : +this.selectedDifficulty,
      recipeLanguage: this.selectedLanguage || undefined,
      ingredients:
        this.selectedIngredients.length > 0
          ? this.selectedIngredients
          : undefined,
      currentPage: 1,
    };
  }

  onSearchChange(): void {
    this.filterChange$.next();
  }

  onFilterChange(): void {
    this.filterChange$.next();
  }

  onLanguageChange(): void {
    if (!this.useCustomData) {
      this.loadIngredients();
    }
    this.selectedIngredients = [];
    this.filterChange$.next();
  }

  onTabChange(tab: 'favourites' | 'userRecipes'): void {
    this.activeTab = tab;
    this.tabChanged.emit(tab);
  }

  onIngredientChange(ingredient: string, isChecked: boolean): void {
    if (isChecked) {
      this.selectedIngredients = [...this.selectedIngredients, ingredient];
    } else {
      this.selectedIngredients = this.selectedIngredients.filter(
        (i) => i !== ingredient
      );
    }
    this.onFilterChange();
  }

  isIngredientSelected(ingredient: string): boolean {
    return this.selectedIngredients.includes(ingredient);
  }

  private executeFilterUpdate(): void {
    if (this.useCustomData) {
      // For custom usage, just emit the change
      this.filtersChanged.emit();
    } else {
      // For FilterService usage
      const filters: Partial<FilterState> = {
        query: this.searchQuery,
        prepTimeFrom: this.prepTimeFrom,
        prepTimeTo: this.prepTimeTo,
        servingsFrom: this.servingsFrom,
        servingsTo: this.servingsTo,
        category: this.selectedCategory || undefined,
        difficulty:
          this.selectedDifficulty === '' ? undefined : +this.selectedDifficulty,
        recipeLanguage: this.selectedLanguage || undefined,
        ingredients:
          this.selectedIngredients.length > 0
            ? this.selectedIngredients
            : undefined,
        currentPage: 1,
      };

      this.filterService.updateFilters(filters);
      this.filtersChanged.emit();
    }
  }

  private updateFilters(): void {
    this.filterChange$.next();
  }

  openMenu(): void {
    this.scrollLockService.lockScroll();
    this.isMenuOpen = true;
  }

  closeMenu(): void {
    this.scrollLockService.unlockScroll();
    this.isMenuOpen = false;
  }
}
