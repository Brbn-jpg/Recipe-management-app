import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Recipe } from '../Interface/recipe';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root',
})
export class RecipeService {
  constructor(private http: HttpClient, private authService: AuthService) {}
  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    if (token) {
      return new HttpHeaders().set('Authorization', `Bearer ${token}`);
    }
    return new HttpHeaders();
  }

  url = `${environment.apiUrl}/recipes`;
  loadRecipeDetails(recipeId: number): Observable<Recipe> {
    return this.http.get<Recipe>(`${this.url}/${recipeId}`);
  }

  postRecipe(recipeData: FormData): Observable<any> {
    const headers = this.getAuthHeaders();

    return this.http.post<any>(this.url, recipeData, { headers });
  }

  updateRecipe(recipeData: FormData, recipeId: number): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.put<any>(`${this.url}/${recipeId}`, recipeData, {
      headers,
    });
  }

  deleteRecipe(recipeId: number): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.delete<any>(`${this.url}/${recipeId}`, { headers });
  }

  rateRecipe(recipeId: number, rating: number): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.post<any>(`${this.url}/${recipeId}`, rating, {
      headers,
    });
  }

  getUserRating(recipeId: number): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.get<number>(`${this.url}/${recipeId}/rating`, { headers });
  }

  isOwner(recipeId: number): Observable<boolean> {
    const headers = this.getAuthHeaders();
    return this.http.get<boolean>(`${this.url}/${recipeId}/isOwner`, {
      headers,
    });
  }

  isFavourite(recipeId: number): Observable<boolean> {
    const headers = this.getAuthHeaders();

    const params = new HttpParams().set('recipeId', recipeId.toString());

    return this.http.get<boolean>(`${this.url}/favourites/isFavourite`, {
      headers,
      params,
    });
  }

  addToFavourites(recipeId: number): Observable<any> {
    const headers = this.getAuthHeaders();

    return this.http.post<any>(
      this.url + '/favourites/add',
      { recipeId },
      { headers }
    );
  }

  removeFromFavourites(recipeId: number): Observable<any> {
    const headers = this.getAuthHeaders();

    return this.http.post<any>(
      this.url + '/favourites/delete',
      { recipeId },
      { headers }
    );
  }
}
