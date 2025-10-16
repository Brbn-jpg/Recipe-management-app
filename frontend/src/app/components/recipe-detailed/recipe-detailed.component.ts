import { RecipeService } from '../../services/recipe.service';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, RouterLink, Router } from '@angular/router';
import { Recipe } from '../../Interface/recipe';
import { Ingredients } from '../../Interface/ingredients';
import { AuthService } from '../../services/auth.service';
import { ToastNotificationComponent } from '../toast-notification/toast-notification.component';
import { NotificationService } from '../../services/notification.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Subject, takeUntil, debounceTime } from 'rxjs';

@Component({
  selector: 'app-recipe-detailed',
  standalone: true,
  imports: [RouterLink, ToastNotificationComponent, TranslateModule],
  templateUrl: './recipe-detailed.component.html',
  styleUrl: './recipe-detailed.component.css',
})
export class RecipeDetailedComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private favouriteToggleAttempts$ = new Subject<void>();
  private ratingAttempts$ = new Subject<number>();

  private lastFavouriteToggle = 0;
  private lastRatingSubmit = 0;
  private readonly minTimeBetweenActions = 1000; // 1 second
  recipeId!: number;
  recipeDetails!: Recipe;
  ingredients: Ingredients[] = [];
  isFavourite: boolean = false;
  isProcessing: boolean = false;
  isLoggedIn: boolean = false;
  isOwner: boolean = false;
  showConfirmation: boolean = false;
  currentRating: number = 0;
  hoveredRating: number = 0;
  isRatingProcessing: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private recipeService: RecipeService,
    private notificationService: NotificationService,
    private translateService: TranslateService
  ) {
    // Setup debounced favourite toggle attempts
    this.favouriteToggleAttempts$
      .pipe(debounceTime(300), takeUntil(this.destroy$))
      .subscribe(() => {
        this.executeFavouriteToggle();
      });

    // Setup debounced rating attempts
    this.ratingAttempts$
      .pipe(debounceTime(500), takeUntil(this.destroy$))
      .subscribe((rating) => {
        this.executeRating(rating);
      });
  }

  ngOnInit() {
    this.isLoggedIn = this.authService.isAuthenticated();
    this.scrollToTop();
    this.route.params.subscribe((params) => {
      this.recipeId = +params['id'];

      if (this.recipeId) {
        this.loadRecipeDetails();
        if (this.isLoggedIn) {
          this.checkIfFavourite();
          this.loadUserRating();
        }
      } else {
        this.notificationService.error('Recipe not found', 5000);
      }
    });
    this.setupIntersectionObserver();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private checkOwnership(): void {
    if (!this.isLoggedIn || !this.recipeDetails) {
      this.isOwner = false;
      return;
    }

    this.recipeService.isOwner(this.recipeDetails.id).subscribe({
      next: (isOwner) => {
        this.isOwner = isOwner;
      },
      error: (err) => {
        this.isOwner = false;
      },
    });
  }

  private checkIfFavourite(): void {
    if (!this.recipeId) {
      return;
    }

    this.recipeService.isFavourite(this.recipeId).subscribe({
      next: (response) => {
        this.isFavourite = response;
        this.updateFavouriteButton();
      },
      error: (err) => {
        this.isFavourite = false;
        this.updateFavouriteButton();
      },
    });
  }

  private loadRecipeDetails(): void {
    this.recipeService.loadRecipeDetails(this.recipeId).subscribe({
      next: (response) => {
        this.recipeDetails = response;
        this.ingredients = this.recipeDetails.ingredients.map((ingredient) => ({
          name: ingredient.ingredientName,
          quantity: Number(ingredient.quantity),
          unit: ingredient.unit,
        }));
        this.checkOwnership();
      },
      error: (err) => {
        // Check if it's a 404 error (recipe not found)
        if (err.status === 404) {
          this.router.navigate(['/not-found']);
        } else {
          this.notificationService.error('Error loading recipe details', 5000);
        }
      },
    });
  }

  private loadUserRating(): void {
    this.recipeService.getUserRating(this.recipeId).subscribe({
      next: (rating) => {
        this.currentRating = rating;
        this.updateStarDisplay(rating);
      },
      error: (err) => {
        this.currentRating = 0;
        this.updateStarDisplay(0);
      },
    });
  }

  private updateFavouriteButton(): void {
    const favButton = document.querySelector('path') as SVGPathElement;
    if (favButton) {
      if (this.isFavourite) {
        favButton.classList.add('active');
      } else {
        favButton.classList.remove('active');
      }
    }
  }

  private setupIntersectionObserver(): void {
    const hiddenElements = document.querySelectorAll(
      '.hidden'
    ) as NodeListOf<HTMLElement>;

    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('show');
        } else {
          entry.target.classList.remove('show');
        }
      });
    });

    hiddenElements.forEach((element: HTMLElement) => {
      observer.observe(element);
    });
  }

  check() {
    const inputs = document.querySelectorAll(
      '.ingredients input[type="checkbox"]'
    ) as NodeListOf<HTMLInputElement>;
    const labels = document.querySelectorAll(
      '.ingredients label'
    ) as NodeListOf<HTMLLabelElement>;

    inputs.forEach((input, index) => {
      const label = labels[index];
      const optionalBadge = label.querySelector('.optional-badge');

      if (input.checked) {
        label.classList.add('checked');
        if (optionalBadge) {
          optionalBadge.classList.add('checked-optional');
        }
      } else {
        label.classList.remove('checked');
        if (optionalBadge) {
          optionalBadge.classList.remove('checked-optional');
        }
      }
    });
  }

  toggleFavourite() {
    if (this.isProcessing) {
      return;
    }

    if (!this.isLoggedIn) {
      this.notificationService.warning('Please log in to add favourites');
      return;
    }

    const now = Date.now();
    if (now - this.lastFavouriteToggle < this.minTimeBetweenActions) {
      return; // Silently ignore rapid clicks
    }

    this.favouriteToggleAttempts$.next();
  }

  private executeFavouriteToggle() {
    if (this.isProcessing) {
      return;
    }

    this.isProcessing = true;
    this.lastFavouriteToggle = Date.now();

    if (!this.isFavourite) {
      this.recipeService.addToFavourites(this.recipeId).subscribe({
        next: (response) => {
          this.isFavourite = true;
          this.updateFavouriteButton();
          this.isProcessing = false;
        },
        error: (err) => {
          this.notificationService.error('Failed to add to favourites', 5000);
          this.isProcessing = false;
        },
      });
    } else {
      this.recipeService.removeFromFavourites(this.recipeId).subscribe({
        next: (response) => {
          this.isFavourite = false;
          this.updateFavouriteButton();
          this.isProcessing = false;
        },
        error: (err) => {
          this.notificationService.error(
            'Failed to remove from favourites',
            5000
          );
          this.isProcessing = false;
        },
      });
    }
  }

  delete() {
    this.showConfirmation = true;
  }

  cancelDelete() {
    this.showConfirmation = false;
  }

  confirmDelete() {
    this.recipeService.deleteRecipe(this.recipeId).subscribe({
      next: (response) => {
        this.notificationService.success('Recipe has been deleted', 5000);
        this.notificationService.info('Refresh the site to see changes', 5000);
      },
      error: (err) => {
        this.notificationService.error('Error during deletion', 5000);
      },
    });
    this.cancelDelete();
  }

  // RATING STARS

  onStarHover(rating: number): void {
    this.hoveredRating = rating;
    this.updateStarDisplay(rating);
  }

  onStarsLeave(rating: number): void {
    this.hoveredRating = 0;
    this.updateStarDisplay(rating);
  }

  onStarClick(rating: number): void {
    if (this.isRatingProcessing) {
      return;
    }

    if (!this.isLoggedIn) {
      this.notificationService.warning('Please log in to rate recipes');
      return;
    }

    // Check if user is trying to give the same rating they already gave
    if (this.currentRating === rating) {
      this.notificationService.info(
        `Recipe is already rated with ${rating} stars`
      );
      return;
    }

    const now = Date.now();
    if (now - this.lastRatingSubmit < this.minTimeBetweenActions) {
      return; // Silently ignore rapid clicks
    }

    this.ratingAttempts$.next(rating);
  }

  private executeRating(rating: number): void {
    if (this.isRatingProcessing) {
      return;
    }

    this.isRatingProcessing = true;
    this.lastRatingSubmit = Date.now();

    this.recipeService.rateRecipe(this.recipeId, rating).subscribe({
      next: (response) => {
        this.currentRating = rating;
        this.updateStarDisplay(rating);
        this.notificationService.success(
          `Recipe rated with ${rating} stars!`,
          5000
        );
        this.isRatingProcessing = false;
      },
      error: (err) => {
        this.notificationService.error('Error submitting rating', 5000);
        this.isRatingProcessing = false;
      },
    });
  }

  private updateStarDisplay(rating: number): void {
    for (let i = 1; i <= 5; i++) {
      const star = document.getElementById(`star-${i}`)?.querySelector('path');
      if (star) {
        if (i <= rating) {
          star.setAttribute('fill', '#FFD700');
          star.setAttribute('stroke', '#FFD700');
        } else {
          star.setAttribute('fill', 'none');
          star.setAttribute('stroke', 'white');
        }
      }
    }
  }

  private scrollToTop(): void {
    window.scrollTo({ top: 0 });
  }

  translateUnit(unit: string): string {
    const unitMap: { [key: string]: string } = {
      tsp: 'ADD_RECIPE.UNITS.TSP',
      tbsp: 'ADD_RECIPE.UNITS.TBSP',
      pcs: 'ADD_RECIPE.UNITS.PCS',
      g: 'ADD_RECIPE.UNITS.G',
      kg: 'ADD_RECIPE.UNITS.KG',
      ml: 'ADD_RECIPE.UNITS.ML',
      L: 'ADD_RECIPE.UNITS.L',
    };

    const translationKey = unitMap[unit];
    if (translationKey) {
      return this.translateService.instant(translationKey);
    }
    return unit; // fallback to original unit if not found
  }
}
