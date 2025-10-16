import { Injectable } from '@angular/core';
import { BehaviorSubject, map, Observable } from 'rxjs';
import { FilterState } from '../Interface/filter-state';
import { HttpClient } from '@angular/common/http';
import { RecipesResponse } from '../Interface/recipe-response';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class FilterService {
  private readonly url = `${environment.apiUrl}/recipes`;

  private filterState$ = new BehaviorSubject<FilterState>({
    currentPage: 1,
    pageSize: 12,
  });

  constructor(private http: HttpClient) {}

  loadRecipes(filters?: Partial<FilterState>): Observable<RecipesResponse> {
    const params = this.buildParams(filters);

    if (filters?.query) {
      return this.http.get<any[]>(`${this.url}/search`, { params }).pipe(
        map((recipes) => ({
          content: recipes,
          images: [],
          totalPages: 1,
        }))
      );
    }

    return this.http.get<RecipesResponse>(this.url, { params });
  }

  loadCategories(): Observable<string[]> {
    return new Observable((observer) => {
      this.http.get<RecipesResponse>(this.url).subscribe({
        next: (response) => {
          if (response && Array.isArray(response.content)) {
            const categories = Array.from(
              new Set(response.content.map((recipe) => recipe.category))
            ).sort();
            observer.next(categories);
          } else {
            observer.next([]);
          }
          observer.complete();
        },
        error: (err) => observer.error(err),
      });
    });
  }

  loadLanguages(): Observable<{ languageName: string }[]> {
    return new Observable((observer) => {
      this.http.get<RecipesResponse>(this.url).subscribe({
        next: (response) => {
          if (response && Array.isArray(response.content)) {
            const uniqueLanguages = Array.from(
              new Set(response.content.map((recipe) => recipe.language))
            ).filter((lang) => lang && lang.trim() !== '');

            const languages = uniqueLanguages
              .sort()
              .map((lang) => ({ languageName: lang }));

            observer.next(languages);
          } else {
            observer.next([]);
          }
          observer.complete();
        },
        error: (err) => observer.error(err),
      });
    });
  }

  loadIngredients(language?: string): Observable<string[]> {
    return new Observable((observer) => {
      this.http.get<RecipesResponse>(this.url).subscribe({
        next: (response) => {
          if (response && Array.isArray(response.content)) {
            let filteredRecipes = response.content;
            if (language) {
              filteredRecipes = response.content.filter(
                (recipe) => recipe.language === language
              );
            }

            const allIngredients = filteredRecipes
              .flatMap(
                (recipe) =>
                  recipe.ingredients?.map((ing) => ing.ingredientName) || []
              )
              .filter((ing) => ing && ing.trim() !== '');

            const uniqueIngredients = Array.from(
              new Set(allIngredients)
            ).sort();
            observer.next(uniqueIngredients);
          } else {
            observer.next([]);
          }
          observer.complete();
        },
        error: (err) => observer.error(err),
      });
    });
  }

  get currentFilters(): FilterState {
    return this.filterState$.value;
  }

  get filters$(): Observable<FilterState> {
    return this.filterState$.asObservable();
  }

  updateFilters(filters: Partial<FilterState>): void {
    const currentState = this.filterState$.value;
    const newState = { ...currentState, ...filters };
    this.filterState$.next(newState);
  }

  private buildParams(filters?: Partial<FilterState>): any {
    const currentFilters = { ...this.currentFilters, ...filters };
    const params: any = {
      page: currentFilters.currentPage,
      size: currentFilters.pageSize,
    };

    if (currentFilters.difficulty) {
      params.difficulty = currentFilters.difficulty;
    }

    if (
      currentFilters.prepTimeFrom !== undefined ||
      currentFilters.prepTimeTo !== undefined
    ) {
      const prepTimeFrom = currentFilters.prepTimeFrom ?? 0;
      const prepTimeTo = currentFilters.prepTimeTo ?? 99999;
      params.prepareTime = `${prepTimeFrom}-${prepTimeTo}`;
    }

    if (
      currentFilters.servingsFrom !== undefined ||
      currentFilters.servingsTo !== undefined
    ) {
      const servingsFrom = currentFilters.servingsFrom ?? 0;
      const servingsTo = currentFilters.servingsTo ?? 99999;
      params.servings = `${servingsFrom}-${servingsTo}`;
    }

    if (currentFilters.category) {
      params.category = currentFilters.category;
    }

    if (currentFilters.recipeLanguage) {
      params.language = currentFilters.recipeLanguage;
    }

    if (currentFilters.query) {
      params.query = currentFilters.query;
    }

    if (currentFilters.ingredients && currentFilters.ingredients.length > 0) {
      params.ingredients = currentFilters.ingredients;
    }

    return params;
  }
}
