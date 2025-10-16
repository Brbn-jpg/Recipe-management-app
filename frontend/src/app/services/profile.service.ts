import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, map, Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

export interface UpdateEmailDto {
  newEmail: string;
  password: string;
}

export interface UpdatePasswordDto {
  currentPassword: string;
  newPassword: string;
}

@Injectable({
  providedIn: 'root',
})
export class ProfileService {
  constructor(private http: HttpClient, private authService: AuthService) {}

  // EDIT

  private editModeSubject = new BehaviorSubject<boolean>(false);
  editMode$ = this.editModeSubject.asObservable();

  setEditMode(value: boolean) {
    this.editModeSubject.next(value);
  }

  getEditMode(): boolean {
    return this.editModeSubject.getValue();
  }

  // SETTINGS

  private settingsModeSubject = new BehaviorSubject<boolean>(false);
  settingsMode$ = this.settingsModeSubject.asObservable();

  setSettingsMode(value: boolean) {
    this.settingsModeSubject.next(value);
  }

  getSettingsMode(): boolean {
    return this.settingsModeSubject.getValue();
  }

  // DELETE

  private deleteModeSubject = new BehaviorSubject<boolean>(false);
  deleteMode$ = this.deleteModeSubject.asObservable();

  private showDeleteModalSubject = new BehaviorSubject<boolean>(false);
  showDeleteModal$ = this.showDeleteModalSubject.asObservable();

  setDeleteMode(value: boolean) {
    this.deleteModeSubject.next(value);
  }

  getDeleteMode(): boolean {
    return this.deleteModeSubject.getValue();
  }

  setShowDeleteModal(value: boolean) {
    this.showDeleteModalSubject.next(value);
  }

  getShowDeleteModal(): boolean {
    return this.showDeleteModalSubject.getValue();
  }

  private baseUrl = environment.apiUrl;

  private getAuthHeaders(): HttpHeaders | undefined {
    const token = this.authService.getToken();
    return token
      ? new HttpHeaders().set('Authorization', `Bearer ${token}`)
      : undefined;
  }

  getUserProfile(): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.get<any>(`${environment.apiUrl}/users/aboutme`, {
      headers,
    });
  }

  getUserRecipes(): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.get<any>(`${this.baseUrl}/users/recipes`, { headers });
  }

  getUserFavourites(): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.get<any>(`${this.baseUrl}/users/favourites`, { headers });
  }

  // Username and description only
  updateUserProfile(userId: number, userData: any): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.put<any>(
      `${environment.apiUrl}/users/${userId}/profile`,
      userData,
      {
        headers,
      }
    );
  }

  updateUserEmail(userId: number, userData: UpdateEmailDto): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.put<any>(
      `${this.baseUrl}/users/${userId}/email`,
      userData,
      { headers }
    );
  }

  updateUserPassword(
    userId: number,
    userData: UpdatePasswordDto
  ): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.put<any>(
      `${this.baseUrl}/users/${userId}/password`,
      userData,
      { headers }
    );
  }

  deleteUser(userId: number): Observable<void> {
    const headers = this.getAuthHeaders();
    return this.http.delete<any>(`${this.baseUrl}/users/${userId}`, {
      headers,
    });
  }

  private setImageSafely(selector: string, imageUrl: string): void {
    const imageElement = document.querySelector(selector) as HTMLImageElement;
    if (imageElement) {
      imageElement.crossOrigin = 'anonymous';
      imageElement.src = imageUrl;
    }
  }

  editProfilePicture(userId: number, event: Event): void {
    const fileInput = document.createElement('input');
    fileInput.type = 'file';
    fileInput.accept = 'image/*';
    fileInput.onchange = (e: Event) => {
      const input = e.target as HTMLInputElement;
      if (input.files && input.files.length > 0) {
        const file = input.files[0];
        if (file.size > 5 * 1024 * 1024) {
          alert('File size exceeds 5 MB limit.');
          return;
        }
        this.uploadProfilePicture(userId, file).subscribe({
          next: (imageUrl) => {
            this.setImageSafely('.profile-picture', imageUrl);
          },
          error: (error) => {
            alert('Failed to upload profile picture.');
          },
        });
      }
    };
    fileInput.click();
  }

  editBackgroundPicture(userId: number, event: Event): void {
    const fileInput = document.createElement('input');
    fileInput.type = 'file';
    fileInput.accept = 'image/*';
    fileInput.onchange = (e: Event) => {
      const input = e.target as HTMLInputElement;
      if (input.files && input.files.length > 0) {
        const file = input.files[0];
        if (file.size > 5 * 1024 * 1024) {
          alert('File size exceeds 5 MB limit.');
          return;
        }
        this.uploadBackgroundPicture(userId, file).subscribe({
          next: (imageUrl) => {
            this.setImageSafely('.background-image', imageUrl);
          },
          error: (error) => {
            alert('Failed to upload background picture.');
          },
        });
      }
    };
    fileInput.click();
  }

  private uploadProfilePicture(userId: number, file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    const headers = this.getAuthHeaders();
    return this.http.put(
      `${environment.apiUrl}/users/${userId}/profile-picture`,
      formData,
      {
        headers,
        responseType: 'text',
      }
    );
  }

  private uploadBackgroundPicture(
    userId: number,
    file: File
  ): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    const headers = this.getAuthHeaders();
    return this.http.put(
      `${environment.apiUrl}/users/${userId}/background-picture`,
      formData,
      {
        headers,
        responseType: 'text',
      }
    );
  }
}
