import { Recipe } from './recipe';
import { RecipeImage } from './recipe-image';

export interface RecipesResponse {
  content: Recipe[];
  images: RecipeImage[];
  totalPages: number;
}
