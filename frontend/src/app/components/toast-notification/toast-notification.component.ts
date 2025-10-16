import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import {
  NotificationService,
  Notification,
} from '../../services/notification.service';

@Component({
  selector: 'app-toast-notification',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './toast-notification.component.html',
  styleUrl: './toast-notification.component.css',
})
export class ToastNotificationComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  private subscription?: Subscription;

  constructor(private notificationService: NotificationService) {}
  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }

  ngOnInit() {
    this.subscription = this.notificationService.notifications$.subscribe(
      (notification) => (this.notifications = notification)
    );
  }

  removeNotifaction(id: string) {
    this.notificationService.remove(id);
  }
}
