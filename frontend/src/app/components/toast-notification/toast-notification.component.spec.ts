import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ToastNotificationComponent } from './toast-notification.component';
import {
  NotificationService,
  Notification,
} from '../../services/notification.service';
import { of } from 'rxjs';

describe('ToastNotificationComponent', () => {
  let component: ToastNotificationComponent;
  let fixture: ComponentFixture<ToastNotificationComponent>;

  const notificationServiceMock = jasmine.createSpyObj('NotificationService', [
    'remove',
  ]);

  beforeEach(async () => {
    notificationServiceMock.notifications$ = of([]);
    await TestBed.configureTestingModule({
      imports: [
        ToastNotificationComponent,
        TranslateModule.forRoot(),
        NoopAnimationsModule,
      ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: NotificationService, useValue: notificationServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ToastNotificationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with empty notifications', () => {
    expect(component.notifications).toEqual([]);
  });

  it('should subscribe to notifications$ on init', () => {
    const mockNotifications: Notification[] = [
      { id: '1', message: 'Test notification', type: 'success' },
      { id: '2', message: 'Error notification', type: 'error' },
    ];

    notificationServiceMock.notifications$ = of(mockNotifications);

    component.ngOnInit();

    expect(component.notifications).toEqual(mockNotifications);
  });

  it('should update notifications when service emits new data', () => {
    const initialNotifications: Notification[] = [
      { id: '1', message: 'First', type: 'info' },
    ];
    const updatedNotifications: Notification[] = [
      { id: '1', message: 'First', type: 'info' },
      { id: '2', message: 'Second', type: 'success' },
    ];

    notificationServiceMock.notifications$ = of(initialNotifications);
    component.ngOnInit();
    expect(component.notifications).toEqual(initialNotifications);

    notificationServiceMock.notifications$ = of(updatedNotifications);
    component.ngOnInit();
    expect(component.notifications).toEqual(updatedNotifications);
  });

  it('should call notificationService.remove when removeNotifaction is called', () => {
    const testId = 'test-notification-id';

    component.removeNotifaction(testId);

    expect(notificationServiceMock.remove).toHaveBeenCalledWith(testId);
  });

  it('should handle empty notifications array', () => {
    notificationServiceMock.notifications$ = of([]);

    component.ngOnInit();

    expect(component.notifications).toEqual([]);
    expect(component.notifications.length).toBe(0);
  });

  it('should unsubscribe on destroy to prevent memory leaks', () => {
    component.ngOnInit();

    expect(component['subscription']).toBeDefined();

    component.ngOnDestroy();

    expect(component['subscription']?.closed).toBe(true);
  });

  it('should handle destroy when no subscription exists', () => {
    component['subscription'] = undefined;

    expect(() => component.ngOnDestroy()).not.toThrow();
  });

  it('should handle multiple notification types', () => {
    const mixedNotifications: Notification[] = [
      { id: '1', message: 'Success message', type: 'success' },
      { id: '2', message: 'Error message', type: 'error' },
      { id: '3', message: 'Warning message', type: 'warning' },
      { id: '4', message: 'Info message', type: 'info' },
    ];

    notificationServiceMock.notifications$ = of(mixedNotifications);

    component.ngOnInit();

    expect(component.notifications).toEqual(mixedNotifications);
    expect(component.notifications.length).toBe(4);
  });
});
