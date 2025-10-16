import { Rating } from './rating';

export interface ProfileRecipe {
  id: number;
  imageUrl: { imageUrl: string; publicId: string }[];
  recipeName: string;
  servings: number;
  difficulty: number;
  prepareTime: number;
  category: string;
  avgRating: number;
  language: string;
  ratings: Rating[];
  ingredients: {
    ingredientName: string;
    quantity: number;
    unit: string;
    isOptional?: boolean;
  }[];
}