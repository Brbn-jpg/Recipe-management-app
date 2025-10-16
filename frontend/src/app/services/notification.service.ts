import { Injectable } from '@angular/core';
import { BehaviorSubject, generate } from 'rxjs';

export interface Notification {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
  duration?: number; // Duration in milliseconds, optional
}

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private notificationSubject = new BehaviorSubject<Notification[]>([]);
  public notifications$ = this.notificationSubject.asObservable();

  private generateId(): string {
    return Math.random().toString(36).substring(2, 9);
  }

  private AddNotification(notification: Notification) {
    const currentNotifitcations = this.notificationSubject.value;
    this.notificationSubject.next([...currentNotifitcations, notification]);

    // Automatically remove the notification after its duration
    setTimeout(() => {
      this.removeNotification(notification.id);
    }, notification.duration || 3000);
  }

  success(message: string, duration: number = 5000) {
    this.AddNotification({
      id: this.generateId(),
      type: 'success',
      message,
      duration,
    });
  }

  error(message: string, duration: number = 5000) {
    this.AddNotification({
      id: this.generateId(),
      type: 'error',
      message,
      duration,
    });
  }

  warning(message: string, duration: number = 5000) {
    this.AddNotification({
      id: this.generateId(),
      type: 'warning',
      message,
      duration,
    });
  }

  info(message: string, duration: number = 5000) {
    this.AddNotification({
      id: this.generateId(),
      type: 'info',
      message,
      duration,
    });
  }

  remove(id: string) {
    const currentNotifications = this.notificationSubject.value;
    this.notificationSubject.next(
      currentNotifications.filter((notification) => notification.id !== id)
    );
  }

  removeNotification(id: string) {
    this.remove(id);
  }

  clear() {
    this.notificationSubject.next([]);
  }
}
