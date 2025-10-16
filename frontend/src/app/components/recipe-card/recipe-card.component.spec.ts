import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { RecipeCardComponent } from './recipe-card.component';

describe('RecipeCardComponent', () => {
  let component: RecipeCardComponent;
  let fixture: ComponentFixture<RecipeCardComponent>;
  const mockRecipe = {
    id: 1,
    recipeId: 1,
    recipeName: 'Test Recipe',
    difficulty: 1,
    servings: 4,
    prepareTime: 30,
    category: 'breakfast',
    images: [{ imageUrl: 'https://images.com/test-image.jpg', publicId: '1' }],
    ratings: [
      { ratingId: 1, value: 4 },
      { ratingId: 2, value: 5 },
    ],
    isPublic: true,
    steps: [{ content: 'Step1' }],
    ingredients: [
      {
        ingredientName: 'Flour',
        quantity: 500,
        unit: 'g',
        isOptional: false,
      },
    ],
    language: 'pl',
  };

  const mockProfileRecipe = {
    id: 2,
    recipeId: 2,
    recipeName: 'Test Recipe',
    difficulty: 1,
    servings: 4,
    prepareTime: 30,
    category: 'breakfast',
    imageUrl: [
      { imageUrl: 'https://images.com/profile-image.jpg', publicId: '2' },
    ],
    isPublic: true,
    steps: [{ content: 'Step1' }],
    ingredients: [
      {
        ingredientName: 'Flour',
        quantity: 500,
        unit: 'g',
        isOptional: false,
      },
    ],
    language: 'pl',
    avgRating: 4.5,
    ratings: [],
  };

  const mockRecipeWithoutImage = {
    id: 1,
    recipeId: 1,
    recipeName: 'Test Recipe',
    difficulty: 1,
    servings: 4,
    prepareTime: 30,
    category: 'breakfast',
    images: [],
    ratings: [{ ratingId: 1, value: 4.89 }],
    isPublic: true,
    steps: [{ content: 'Step1' }],
    ingredients: [
      {
        ingredientName: 'Flour',
        quantity: 500,
        unit: 'g',
        isOptional: false,
      },
    ],
    language: 'pl',
  };

  const mockProfileRecipeWithoutImage = {
    id: 2,
    recipeId: 2,
    recipeName: 'Test Recipe',
    difficulty: 1,
    servings: 4,
    prepareTime: 30,
    category: 'breakfast',
    imageUrl: [],
    isPublic: true,
    steps: [{ content: 'Step1' }],
    ingredients: [
      {
        ingredientName: 'Flour',
        quantity: 500,
        unit: 'g',
        isOptional: false,
      },
    ],
    language: 'pl',
    avgRating: 4.5,
    ratings: [],
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RecipeCardComponent, TranslateModule.forRoot()],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();
    fixture = TestBed.createComponent(RecipeCardComponent);
    component = fixture.componentInstance;
    component.recipe = mockRecipe;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should return Recipe image when recipe has images', () => {
    component.recipe = mockRecipe;
    const imageUrl = component.getRecipeImage();

    expect(imageUrl).toBe('https://images.com/test-image.jpg');
  });

  it('should return ProfileRecipe imageUrl when recipe has imageUrl', () => {
    component.recipe = mockProfileRecipe;
    const imageUrl = component.getRecipeImage();

    expect(imageUrl).toBe('https://images.com/profile-image.jpg');
  });

  it('should return default image when Recipe has no images', () => {
    component.recipe = mockRecipeWithoutImage;
    const imageUrl = component.getRecipeImage();

    expect(imageUrl).toBe('images/Cibaria.png');
  });

  it('should return default image when ProfileRecipe has no imageUrl', () => {
    component.recipe = mockProfileRecipeWithoutImage;
    const imageUrl = component.getRecipeImage();

    expect(imageUrl).toBe('images/Cibaria.png');
  });

  it('should calculate correct average rating', () => {
    const avgRating = component.getAverageRating(mockRecipe.ratings);
    expect(avgRating).toBe('4.5');
  });

  it('should return "0.0" for empty ratings array', () => {
    const avgRating = component.getAverageRating(mockProfileRecipe.ratings);
    expect(avgRating).toBe('0.0');
  });

  it('should return "0.0" for null/undefined ratings', () => {
    expect(component.getAverageRating(null as any)).toBe('0.0');
    expect(component.getAverageRating(undefined as any)).toBe('0.0');
  });

  it('should format rating to 1 decimal place', () => {
    const avgRating = component.getAverageRating(
      mockRecipeWithoutImage.ratings
    );
    expect(avgRating).toBe('4.9');
  });

  it('should return "Easy" for difficulty 1', () => {
    expect(component.getDifficulty(1)).toBe('Easy');
  });

  it('should return "Medium" for difficulty 2', () => {
    expect(component.getDifficulty(2)).toBe('Medium');
  });

  it('should return "Hard" for difficulty 3', () => {
    expect(component.getDifficulty(3)).toBe('Hard');
  });

  it('should return "Unknown" for invalid difficulty values', () => {
    expect(component.getDifficulty(0)).toBe('Unknown');
  });
});
