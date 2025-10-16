import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { Recipe } from '../../Interface/recipe';
import { ProfileRecipe } from '../../Interface/profile-recipe';
import { Rating } from '../../Interface/rating';

type RecipeCardData = Recipe | ProfileRecipe;

@Component({
  selector: 'app-recipe-card',
  standalone: true,
  imports: [RouterLink, TranslateModule],
  templateUrl: './recipe-card.component.html',
  styleUrl: './recipe-card.component.css',
})
export class RecipeCardComponent {
  @Input() recipe!: RecipeCardData;

  getRecipeImage(): string {
    if ('images' in this.recipe) {
      return this.recipe.images?.[0]?.imageUrl || 'images/Cibaria.png';
    } else {
      return this.recipe.imageUrl?.[0]?.imageUrl || 'images/Cibaria.png';
    }
  }

  getAverageRating(ratings: Rating[]): string {
    if (!ratings || ratings.length === 0) {
      return '0.0';
    }

    const totalRating = ratings.reduce((sum, rating) => sum + rating.value, 0);
    const avgRating = totalRating / ratings.length;
    return avgRating.toFixed(1);
  }

  getDifficulty(difficulty: number): string {
    switch (difficulty) {
      case 1:
        return 'Easy';
      case 2:
        return 'Medium';
      case 3:
        return 'Hard';
      default:
        return 'Unknown';
    }
  }
}
