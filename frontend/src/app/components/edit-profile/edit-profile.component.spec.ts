import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { EditProfileComponent } from './edit-profile.component';
import { of, throwError } from 'rxjs';
import { ProfileService } from '../../services/profile.service';
import { NotificationService } from '../../services/notification.service';

describe('EditProfileComponent', () => {
  let component: EditProfileComponent;
  let fixture: ComponentFixture<EditProfileComponent>;
  const notificationServiceMock = jasmine.createSpyObj(
    'NotificationService',
    ['info', 'error', 'warning', 'success'],
    {
      notifications$: of([]),
    }
  );

  const profileServiceMock = jasmine.createSpyObj(
    'ProfileService',
    ['updateUserProfile', 'getUserProfile', 'setEditMode'],
    { editMode$: of(false), settingsMode$: of(false), deleteMode$: of(false) }
  );

  const mockUser = {
    id: 1,
    username: 'Andrzej',
    description: 'I like food',
  };

  beforeEach(async () => {
    profileServiceMock.getUserProfile.and.returnValue(of(mockUser));

    await TestBed.configureTestingModule({
      imports: [EditProfileComponent, TranslateModule.forRoot()],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ProfileService, useValue: profileServiceMock },
        { provide: NotificationService, useValue: notificationServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EditProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    const testFixture = TestBed.createComponent(EditProfileComponent);
    const testComponent = testFixture.componentInstance;
    // Create separate component instance to test true default values
    // Main component instance runs ngOnInit() which triggers async HTTP calls,
    // causing flaky test (inconsistent) behavior due to timing issues
    expect(testComponent.isUpdatingUsername).toBe(false);
    expect(testComponent.isUpdatingDescription).toBe(false);
    expect(testComponent.userId).toBe(0);
    expect(testComponent.saveUsernameIcon).toBe(false);
    expect(testComponent.saveDescriptionIcon).toBe(false);
    expect(testComponent.saveEmailIcon).toBe(false);
    expect(testComponent.savePasswordIcon).toBe(false);
    expect(testComponent.newUsername).toBe('');
    expect(testComponent.newDescription).toBe('');
    expect(testComponent.username).toBe('');
    expect(testComponent.description).toBe('');
    expect(testComponent.originalUsername).toBe('');
    expect(testComponent.originalDescription).toBe('');
  });

  it('should load user data on init', () => {
    component.ngOnInit();
    expect(component.username).toBe('Andrzej');
    expect(component.description).toBe('I like food');
  });

  it('should handle user data loading errors', () => {
    profileServiceMock.getUserProfile.calls.reset();
    profileServiceMock.getUserProfile.and.returnValue(throwError('API Error'));
    component.ngOnInit();

    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Failed to load user data',
      5000
    );
  });

  it('should set original values for comparison', () => {
    component.loadUserData();

    expect(component.originalUsername).toBe('Andrzej');
    expect(component.originalDescription).toBe('I like food');
    expect(component.username).toBe('Andrzej');
    expect(component.description).toBe('I like food');
    expect(component.newUsername).toBe('Andrzej');
    expect(component.newDescription).toBe('I like food');
  });

  it('should update username successfully', fakeAsync(() => {
    spyOn(document, 'getElementById').and.returnValue({
      value: 'Bogdan',
    } as HTMLInputElement);
    profileServiceMock.updateUserProfile.and.returnValue(of({}));
    spyOn(component, 'loadUserData');

    (component as any).executeUsernameUpdate();

    expect(component.username).toBe('Bogdan');
    expect(notificationServiceMock.success).toHaveBeenCalledWith(
      'Username updated successfully',
      5000
    );
    expect(component.isUpdatingUsername).toBe(false);
    expect(component.loadUserData).toHaveBeenCalled();

    tick(5001); // setTimeout cleanup
  }));

  it('should prevent username update when already updating', () => {
    component.isUpdatingUsername = true;

    component.saveUsername();

    expect(notificationServiceMock.warning).toHaveBeenCalledWith(
      'Please wait, updating username...',
      5000
    );
  });

  it('should prevent empty username update', () => {
    spyOn(document, 'getElementById').and.returnValue({
      value: '',
    } as HTMLInputElement);
    profileServiceMock.updateUserProfile.and.returnValue(of({}));
    spyOn(component, 'loadUserData');

    (component as any).executeUsernameUpdate();

    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Username cannot be empty',
      5000
    );
  });

  it('should prevent username update if unchanged', () => {
    spyOn(document, 'getElementById').and.returnValue({
      value: 'Andrzej',
    } as HTMLInputElement);
    profileServiceMock.updateUserProfile.and.returnValue(of({}));
    spyOn(component, 'loadUserData');

    (component as any).executeUsernameUpdate();

    expect(notificationServiceMock.info).toHaveBeenCalledWith(
      'Username is already up to date',
      5000
    );
  });

  it('should handle username update API errors', () => {
    spyOn(document, 'getElementById').and.returnValue({
      value: 'Bogdan',
    } as HTMLInputElement);
    profileServiceMock.updateUserProfile.and.returnValue(
      throwError('API Error')
    );
    spyOn(component, 'loadUserData');

    (component as any).executeUsernameUpdate();

    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Failed to update username',
      5000
    );
  });

  it('should update description successfully', fakeAsync(() => {
    spyOn(document, 'getElementById').and.returnValue({
      value: 'I like bigos',
    } as HTMLInputElement);
    profileServiceMock.updateUserProfile.and.returnValue(of({}));
    spyOn(component, 'loadUserData');

    (component as any).executeDescriptionUpdate();

    expect(component.description).toBe('I like bigos');
    expect(notificationServiceMock.success).toHaveBeenCalledWith(
      'Description updated successfully',
      5000
    );
    expect(component.isUpdatingDescription).toBe(false);
    expect(component.loadUserData).toHaveBeenCalled();
    tick(5000);
  }));

  it('should prevent description update when already updating', () => {
    component.isUpdatingDescription = true;
    component.saveDescription();

    expect(notificationServiceMock.warning).toHaveBeenCalledWith(
      'Please wait, updating description...',
      5000
    );
  });

  it('should allow empty description update', fakeAsync(() => {
    spyOn(document, 'getElementById').and.returnValue({
      value: '',
    } as HTMLTextAreaElement);
    profileServiceMock.updateUserProfile.and.returnValue(of({}));
    spyOn(component, 'loadUserData');

    (component as any).executeDescriptionUpdate();

    expect(notificationServiceMock.success).toHaveBeenCalledWith(
      'Description updated successfully',
      5000
    );

    tick(5001); // setTimeout cleanup
  }));

  it('should prevent description update if unchanged', () => {
    spyOn(document, 'getElementById').and.returnValue({
      value: 'I like food',
    } as HTMLTextAreaElement);
    profileServiceMock.updateUserProfile.and.returnValue(of({}));
    spyOn(component, 'loadUserData');

    (component as any).executeDescriptionUpdate();

    expect(notificationServiceMock.info).toHaveBeenCalledWith(
      'Description is already up to date',
      5000
    );
  });

  it('should handle description update API errors', () => {
    notificationServiceMock.error.calls.reset();
    spyOn(document, 'getElementById').and.returnValue({
      value: 'I like bigos',
    } as HTMLTextAreaElement);
    profileServiceMock.updateUserProfile.and.returnValue(
      throwError('API Error')
    );
    spyOn(component, 'loadUserData');

    (component as any).executeDescriptionUpdate();

    expect(notificationServiceMock.error).toHaveBeenCalledWith(
      'Failed to update description',
      5000
    );
  });

  it('should save both username and description', () => {
    spyOn(component, 'saveUsername');
    spyOn(component, 'saveDescription');
    spyOn(document, 'getElementById').and.returnValue({
      value: 'test',
    } as any);
    profileServiceMock.updateUserProfile.and.returnValue(of({}));
    spyOn(component, 'loadUserData');

    component.save();

    expect(component.saveUsername).toHaveBeenCalled();
    expect(component.saveDescription).toHaveBeenCalled();
  });

  it('should prevent save when updates in progress', () => {
    component.isUpdatingDescription = true;
    component.isUpdatingUsername = true;

    component.save();

    expect(notificationServiceMock.warning).toHaveBeenCalledWith(
      'Please wait, update in progress...',
      5000
    );
  });
});
