import {
  Component,
  ElementRef,
  HostListener,
  inject,
  OnInit,
  OnDestroy,
  ViewChild,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavigationEnd, Router } from '@angular/router';
import { TranslateService, TranslateModule } from '@ngx-translate/core';
import { filter, Subject, takeUntil } from 'rxjs';
import { LanguageService } from '../../services/language.service';
import { ScrollLockService } from '../../services/scroll-lock.service';
import { ProfileService } from '../../services/profile.service';
import { EditProfileComponent } from '../edit-profile/edit-profile.component';
import { SettingsProfileComponent } from '../settings-profile/settings-profile.component';
import { ToastNotificationComponent } from '../toast-notification/toast-notification.component';
import { RecipeFiltersComponent } from '../recipe-filters/recipe-filters.component';

import { Language } from '../../Interface/language';
import { FilterState } from '../../Interface/filter-state';
import { ProfileRecipe } from '../../Interface/profile-recipe';
import { RecipeCardComponent } from '../recipe-card/recipe-card.component';
import { NotificationService } from '../../services/notification.service';
import { DeleteProfileComponent } from '../delete-profile/delete-profile.component';

// Local interfaces
export interface UserProfileResponse {
  id: number;
  username: string;
  photoUrl: string;
  backgroundUrl: string;
  description: string;
}

export interface UserFavouritesResponse {
  favourites: ProfileRecipe[];
}

export interface UserRecipesResponse {
  userRecipes: ProfileRecipe[];
}

type ActiveTab = 'favourites' | 'userRecipes';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    FormsModule,
    TranslateModule,
    EditProfileComponent,
    SettingsProfileComponent,
    DeleteProfileComponent,
    ToastNotificationComponent,
    RecipeFiltersComponent,
    RecipeCardComponent,
  ],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css',
})
export class ProfileComponent implements OnInit, OnDestroy {
  @ViewChild(RecipeFiltersComponent) filtersComponent!: RecipeFiltersComponent;

  private readonly destroy$ = new Subject<void>();
  private readonly el: ElementRef = inject(ElementRef);

  // User data
  userId!: number;
  username?: string;
  userPhotoUrl: string | null = null;
  backgroundImageUrl: string | null = null;
  description?: string;

  // Recipe data
  favouriteRecipes: ProfileRecipe[] = [];
  filteredFavouriteRecipes: ProfileRecipe[] = [];
  userRecipes: ProfileRecipe[] = [];
  filteredUserRecipes: ProfileRecipe[] = [];

  // Filter data
  categoriesArray: string[] = [];
  languagesArray: Language[] = [];
  ingredientsArray: string[] = [];

  // UI state
  activeTab: ActiveTab = 'favourites';
  editMode = false;
  settings = false;
  delete = false;
  isMenuOpen = false;
  isFiltering = false;

  // Pagination
  currentPage = 1;
  readonly pageSize = 12;
  totalPages = 1;

  // Filters
  filters: Partial<FilterState> = { recipeLanguage: '' };
  language = 'en';

  constructor(
    private translate: TranslateService,
    private languageService: LanguageService,
    private scrollLockService: ScrollLockService,
    private profileService: ProfileService,
    private notificationService: NotificationService,
    private router: Router
  ) {
    this.initializeLanguageService();
    this.initializeNavigationHandling();
  }

  ngOnInit(): void {
    this.scrollToTop();
    this.checkScreenSize();
    this.initializeData();
    this.subscribeToProfileServices();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  @HostListener('window:resize', ['$event'])
  onResize(): void {
    this.checkScreenSize();
    if (window.innerWidth > 1350 && this.isMenuOpen) {
      this.closeMenu();
    }
    this.isFiltering = window.innerWidth <= 1350;
  }

  // Public methods
  setActiveTab(tab: ActiveTab): void {
    this.activeTab = tab;
    this.currentPage = 1;
    this.loadFilterOptions();
    this.applyFilters();
  }

  onFiltersChanged(): void {
    if (this.filtersComponent) {
      const oldLanguage = this.filters.recipeLanguage;
      this.filters = this.filtersComponent.getCurrentFilters();

      // If language changed, reload ingredients
      if (oldLanguage !== this.filters.recipeLanguage) {
        this.loadIngredients();
      }

      this.currentPage = 1;
      this.applyFilters();
    }
  }

  onTabChanged(tab: ActiveTab): void {
    this.setActiveTab(tab);
  }

  onPageChange(page: number): void {
    this.currentPage = page;
  }

  openMenu(): void {
    this.toggleMenu(true);
  }

  closeMenu(): void {
    this.toggleMenu(false);
  }

  editProfile(event: Event): void {
    event.preventDefault();
    const newState = !this.profileService.getEditMode();
    this.profileService.setEditMode(newState);
  }

  editSettings(event: Event): void {
    event.preventDefault();
    const newState = !this.profileService.getSettingsMode();
    this.profileService.setSettingsMode(newState);
  }

  deleteProfile(event: Event): void {
    event.preventDefault();
    const newState = !this.profileService.getDeleteMode();
    this.profileService.setShowDeleteModal(true);
    this.profileService.setDeleteMode(newState);
    console.log(newState);
  }

  onEditProfilePicture(event: Event): void {
    this.profileService.editProfilePicture(this.userId, event);
  }

  onEditBackgroundPicture(event: Event): void {
    this.profileService.editBackgroundPicture(this.userId, event);
  }

  // Utility methods

  // Getters for pagination
  get paginatedFavouriteRecipes(): ProfileRecipe[] {
    return this.getPaginatedItems(this.filteredFavouriteRecipes);
  }

  get paginatedUserRecipes(): ProfileRecipe[] {
    return this.getPaginatedItems(this.filteredUserRecipes);
  }

  // Private methods
  private initializeLanguageService(): void {
    this.languageService.language$
      .pipe(takeUntil(this.destroy$))
      .subscribe((language) => {
        this.language = language;
        this.translate.setDefaultLang(language);
      });
  }

  private initializeNavigationHandling(): void {
    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.scrollLockService.unlockScroll();
      });
  }

  private initializeData(): void {
    this.loadUserData();
    this.loadUserRecipes();
    this.loadUserFavourites();
  }

  private subscribeToProfileServices(): void {
    this.profileService.editMode$
      .pipe(takeUntil(this.destroy$))
      .subscribe((value) => {
        this.editMode = value;
      });

    this.profileService.settingsMode$
      .pipe(takeUntil(this.destroy$))
      .subscribe((value) => {
        this.settings = value;
      });

    this.profileService.deleteMode$
      .pipe(takeUntil(this.destroy$))
      .subscribe((value) => {
        this.delete = value;
      });
  }

  private loadUserData(): void {
    this.profileService
      .getUserProfile()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: UserProfileResponse) => {
          if (response) {
            this.userId = response.id;
            this.username = response.username;
            this.userPhotoUrl = response.photoUrl;
            this.backgroundImageUrl = response.backgroundUrl;
            this.description = response.description;
          }
        },
      });
  }

  private loadUserRecipes(): void {
    this.profileService
      .getUserRecipes()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: UserRecipesResponse) => {
          if (response && response.userRecipes) {
            this.userRecipes = response.userRecipes;
            this.filteredUserRecipes = [...this.userRecipes];
            this.loadFilterOptions();
          }
        },
        error: () => {
          this.notificationService.error('Failed to load user recipes', 5000);
          this.userRecipes = [];
          this.filteredUserRecipes = [];
        },
      });
  }

  private loadUserFavourites(): void {
    this.profileService
      .getUserFavourites()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: UserFavouritesResponse) => {
          if (response) {
            this.favouriteRecipes = response.favourites || [];
            this.filteredFavouriteRecipes = [...this.favouriteRecipes];
            this.loadFilterOptions();
          }
        },
        error: () => {
          this.notificationService.error(
            'Failed to load user favourite recipes',
            5000
          );
          this.favouriteRecipes = [];
          this.filteredFavouriteRecipes = [];
        },
      });
  }

  private loadFilterOptions(): void {
    this.loadCategories();
    this.loadLanguages();
    this.loadIngredients();
  }

  private loadCategories(): void {
    const source = this.getActiveRecipeSource();

    if (source.length > 0) {
      const uniqueCategories = Array.from(
        new Set(source.map((recipe) => recipe.category))
      ).sort();

      this.categoriesArray = uniqueCategories;
    } else {
      this.categoriesArray = [];
    }
  }

  private loadLanguages(): void {
    const source = this.getActiveRecipeSource();

    if (source.length > 0) {
      const uniqueLanguages = Array.from(
        new Set(source.map((recipe) => recipe.language))
      ).sort();

      this.languagesArray = uniqueLanguages.map((languageName) => ({
        languageName,
      }));
    } else {
      this.languagesArray = [];
    }
  }

  private loadIngredients(): void {
    if (this.activeTab === 'userRecipes' && this.userRecipes.length > 0) {
      let recipesToProcess = this.userRecipes;

      // Filter recipes by language if selected
      if (this.filters.recipeLanguage) {
        const filterLang = this.filters.recipeLanguage.trim().toLowerCase();
        recipesToProcess = this.userRecipes.filter(
          (recipe) => recipe.language?.trim().toLowerCase() === filterLang
        );
      }

      const allIngredients = recipesToProcess
        .flatMap(
          (recipe) => recipe.ingredients?.map((ing) => ing.ingredientName) || []
        )
        .filter((ing) => ing && ing.trim() !== '');

      this.ingredientsArray = Array.from(new Set(allIngredients)).sort();
    } else if (
      this.activeTab === 'favourites' &&
      this.favouriteRecipes.length > 0
    ) {
      let recipesToProcess = this.favouriteRecipes;

      // Filter recipes by language if selected
      if (this.filters.recipeLanguage) {
        const filterLang = this.filters.recipeLanguage.trim().toLowerCase();
        recipesToProcess = this.favouriteRecipes.filter(
          (recipe) => recipe.language?.trim().toLowerCase() === filterLang
        );
      }

      const allIngredients = recipesToProcess
        .flatMap(
          (recipe) => recipe.ingredients?.map((ing) => ing.ingredientName) || []
        )
        .filter((ing) => ing && ing.trim() !== '');

      this.ingredientsArray = Array.from(new Set(allIngredients)).sort();
    } else {
      this.ingredientsArray = [];
    }
  }

  private getActiveRecipeSource(): ProfileRecipe[] {
    return this.activeTab === 'favourites'
      ? this.favouriteRecipes
      : this.userRecipes;
  }

  applyFilters(): void {
    const source = [...this.getActiveRecipeSource()];

    let filtered: ProfileRecipe[] = [...source];
    filtered = this.applyAllFilters(filtered);

    if (this.activeTab === 'favourites') {
      this.filteredFavouriteRecipes = filtered;
      this.totalPages = Math.ceil(
        this.filteredFavouriteRecipes.length / this.pageSize
      );
    } else {
      this.filteredUserRecipes = filtered;
      this.totalPages = Math.ceil(
        this.filteredUserRecipes.length / this.pageSize
      );
    }
  }

  private applyAllFilters(recipes: ProfileRecipe[]): ProfileRecipe[] {
    let filtered = [...recipes];

    if (this.filters.difficulty) {
      filtered = filtered.filter(
        (recipe) => recipe.difficulty === this.filters.difficulty
      );
    }

    if (
      this.filters.prepTimeFrom !== undefined ||
      this.filters.prepTimeTo !== undefined
    ) {
      const prepTimeFrom = this.filters.prepTimeFrom ?? 0;
      const prepTimeTo = this.filters.prepTimeTo ?? 99999;
      filtered = filtered.filter(
        (recipe) =>
          recipe.prepareTime >= prepTimeFrom && recipe.prepareTime <= prepTimeTo
      );
    }

    if (
      this.filters.servingsFrom !== undefined ||
      this.filters.servingsTo !== undefined
    ) {
      const servingsFrom = this.filters.servingsFrom ?? 0;
      const servingsTo = this.filters.servingsTo ?? 99999;
      filtered = filtered.filter(
        (recipe) =>
          recipe.servings >= servingsFrom && recipe.servings <= servingsTo
      );
    }

    if (this.filters.category) {
      filtered = filtered.filter(
        (recipe) => recipe.category === this.filters.category
      );
    }

    if (this.filters.recipeLanguage) {
      const filterLang = this.filters.recipeLanguage.trim().toLowerCase();
      filtered = filtered.filter(
        (recipe) => recipe.language?.trim().toLowerCase() === filterLang
      );
    }

    if (this.filters.query) {
      const queryLower = this.filters.query.toLowerCase();
      filtered = filtered.filter((recipe) =>
        recipe.recipeName.toLowerCase().includes(queryLower)
      );
    }

    if (this.filters.ingredients && this.filters.ingredients.length > 0) {
      filtered = filtered.filter((recipe) =>
        this.filters.ingredients!.every((filterIngredient) =>
          recipe.ingredients?.some((recipeIngredient) =>
            recipeIngredient.ingredientName
              .toLowerCase()
              .includes(filterIngredient.toLowerCase())
          )
        )
      );
    }

    return filtered;
  }

  private toggleMenu(open: boolean): void {
    const menu = this.el.nativeElement.querySelector('.menu');
    const menuOverlay = this.el.nativeElement.querySelector('.menu-overlay');

    if (open) {
      menuOverlay?.classList.add('active');
      menu?.classList.add('active');
      this.scrollLockService.lockScroll();
      this.isMenuOpen = true;
    } else {
      menuOverlay?.classList.remove('active');
      menu?.classList.remove('active');
      this.scrollLockService.unlockScroll();
      this.isMenuOpen = false;
    }
  }

  private checkScreenSize(): void {
    this.isFiltering = window.innerWidth <= 1350;
  }

  private scrollToTop(): void {
    window.scrollTo({ top: 0 });
  }

  private getPaginatedItems<T>(items: T[]): T[] {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    return items.slice(startIndex, endIndex);
  }
}
