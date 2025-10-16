import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteProfileComponent } from './delete-profile.component';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { NotificationService } from '../../services/notification.service';
import { ProfileService } from '../../services/profile.service';
import { AuthService } from '../../services/auth.service';

describe('DeleteProfileComponent', () => {
  let component: DeleteProfileComponent;
  let fixture: ComponentFixture<DeleteProfileComponent>;

  const profileServiceMock = jasmine.createSpyObj(
    'ProfileService',
    ['getUserProfile', 'deleteUser'],
    {
      editMode$: of(false),
      settingsMode$: of(false),
      deleteMode$: of(false),
      showDeleteModal$: of(false),
    }
  );

  const authServiceMock = jasmine.createSpyObj('AuthService', ['logout']);

  const notificationServiceMock = jasmine.createSpyObj(
    'NotificationService',
    ['info', 'error', 'warning', 'success'],
    {
      notifications$: of([]),
    }
  );

  const mockUser = {
    userId: 1,
    username: 'Marian',
    userPhotoUrl: [],
    backgroundImageUrl: [],
    description: '',
  };

  beforeEach(async () => {
    profileServiceMock.getUserProfile.and.returnValue(of(mockUser));
    await TestBed.configureTestingModule({
      imports: [DeleteProfileComponent, TranslateModule.forRoot()],
      providers: [
        { provide: NotificationService, useValue: notificationServiceMock },
        { provide: ProfileService, useValue: profileServiceMock },
        { provide: AuthService, useValue: authServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DeleteProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
