import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

export interface AdminStats {
  totalUsers: number;
  adminUsers: number;
  regularUsers: number;
  totalRecipes: number;
  publicRecipes: number;
  privateRecipes: number;
}

export interface UserEntity {
  id: number;
  username: string;
  email: string;
  role: string;
  description?: string;
}

export interface UpdateRoleDto {
  role: string;
}

export interface UpdateUserDto {
  role: string;
  email: string;
  username: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = environment.apiUrl;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  // Statistics
  getStats(): Observable<AdminStats> {
    const headers = this.getAuthHeaders();
    return this.http.get<AdminStats>(`${this.apiUrl}/admin/stats`, { headers });
  }

  // User management
  getAllUsers(): Observable<UserEntity[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<UserEntity[]>(`${this.apiUrl}/admin/users`, { headers });
  }

  deleteUser(userId: number): Observable<void> {
    const headers = this.getAuthHeaders();
    return this.http.delete<void>(`${this.apiUrl}/admin/users/${userId}`, { headers });
  }

  updateUserRole(userId: number, role: string): Observable<UserEntity> {
    const headers = this.getAuthHeaders();
    const body: UpdateRoleDto = { role };
    return this.http.put<UserEntity>(`${this.apiUrl}/admin/users/${userId}/role`, body, { headers });
  }

  updateUser(userId: number, role: string, email: string, username: string): Observable<UserEntity> {
    const headers = this.getAuthHeaders();
    const body: UpdateUserDto = { role, email, username };
    return this.http.put<UserEntity>(`${this.apiUrl}/admin/users/${userId}/role`, body, { headers });
  }

  // Recipe management
  getAllRecipes(): Observable<any[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<any[]>(`${this.apiUrl}/admin/recipes`, { headers });
  }

  deleteRecipe(recipeId: number): Observable<void> {
    const headers = this.getAuthHeaders();
    return this.http.delete<void>(`${this.apiUrl}/admin/recipes/${recipeId}`, { headers });
  }

  updateRecipe(recipeId: number, recipe: any): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.put<any>(`${this.apiUrl}/admin/recipes/${recipeId}`, recipe, { headers });
  }
}