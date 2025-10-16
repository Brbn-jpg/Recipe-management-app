import { Rating } from './rating';
import { RecipeImage } from './recipe-image';
import { Step } from './step';

export interface Recipe {
  id: number;
  category: string;
  difficulty: number;
  prepareTime: number;
  recipeId: number;
  recipeName: string;
  servings: number;
  isPublic: boolean;
  ingredients: {
    ingredientName: string;
    quantity: number;
    unit: string;
    isOptional?: boolean;
  }[];
  steps: Step[];
  images: RecipeImage[];
  language: string;
  ratings: Rating[];
}
