import {
  Component,
  HostListener,
  OnInit,
  OnDestroy,
  ViewChild,
  ElementRef,
} from '@angular/core';
import {
  FormsModule,
  FormGroup,
  Validators,
  ReactiveFormsModule,
  FormControl,
} from '@angular/forms';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { RecipeService } from '../../services/recipe.service';
import { ToastNotificationComponent } from '../toast-notification/toast-notification.component';
import { NotificationService } from '../../services/notification.service';
import { AuthService } from '../../services/auth.service';
import { Subject, takeUntil, debounceTime } from 'rxjs';
import { Router } from '@angular/router';

@Component({
  selector: 'app-add-recipe-panel',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TranslateModule,
    ToastNotificationComponent,
  ],
  templateUrl: './add-recipe-panel.component.html',
  styleUrls: ['./add-recipe-panel.component.css'],
})
export class AddRecipePanelComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private submitAttempts$ = new Subject<void>();

  isSubmitting = false;
  private lastSubmitAttempt = 0;
  private readonly minTimeBetweenSubmits = 3000; // 3 seconds
  ingredients: {
    ingredientName: string;
    quantity: number;
    unit: string;
    isOptional: boolean;
  }[] = [];
  steps: { content: string }[] = [];
  recipeLanguage = 'english';
  isPublic = false;
  newIngredient = {
    ingredientName: '',
    quantity: 0,
    unit: '',
    isOptional: false,
  };
  newStep = '';
  success = false;
  isDragging = false;
  isLoggedIn = false;
  imagePreview: string | null = null;

  // Categories with translation keys
  categoriesArray: { key: string; value: string }[] = [
    { key: 'BREAKFAST', value: 'śniadanie' },
    { key: 'LUNCH', value: 'obiad' },
    { key: 'DINNER', value: 'kolacja' },
    { key: 'DESSERT', value: 'deser' },
    { key: 'SNACK', value: 'przekąska' },
    { key: 'DRINK', value: 'napój' },
    { key: 'SALAD', value: 'sałatka' },
    { key: 'SOUP', value: 'zupa' },
  ];

  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  constructor(
    private recipeService: RecipeService,
    private router: Router,
    private notificationService: NotificationService,
    private authService: AuthService
  ) {
    // Setup debounced submit attempts
    this.submitAttempts$
      .pipe(debounceTime(1000), takeUntil(this.destroy$))
      .subscribe(() => {
        this.executeSubmit();
      });
  }

  recipeForm = new FormGroup({
    title: new FormControl('', [Validators.required]),
    description: new FormControl('', [Validators.required]),
    category: new FormControl('', [Validators.required]),
    servings: new FormControl(null, [Validators.required, Validators.min(1)]),
    prepareTime: new FormControl(null, [
      Validators.required,
      Validators.min(1),
    ]),
    quantity: new FormControl(this.ingredients, [Validators.required]),
    unit: new FormControl(this.ingredients, [Validators.required]),
    difficulty: new FormControl(null, [Validators.required]),
    images: new FormControl<File | null>(null, [Validators.required]),
    steps: new FormControl(this.steps, [Validators.required]),
    ingredients: new FormControl(this.ingredients, [Validators.required]),
    isPublic: new FormControl(this.isPublic, [Validators.required]),
    language: new FormControl(this.recipeLanguage, [Validators.required]),
  });

  ngOnInit(): void {
    this.getToken();
    if (!this.isLoggedIn) {
      this.router.navigate(['/not-found']);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  getToken(): void {
    this.isLoggedIn = this.authService.isAuthenticated();
  }

  addIngredient() {
    const ingredientExists = this.ingredients.some(
      (ingredient) =>
        ingredient.ingredientName.toLowerCase() ===
        this.newIngredient.ingredientName.toLowerCase()
    );
    if (ingredientExists) {
      this.notificationService.warning('This ingredient already exists!', 5000);
      return;
    }
    if (
      this.newIngredient.ingredientName &&
      this.newIngredient.quantity > 0 &&
      this.newIngredient.unit &&
      this.newIngredient.unit !== 'Choose a unit'
    ) {
      this.ingredients.push({ ...this.newIngredient });
      this.newIngredient = {
        ingredientName: '',
        quantity: 0,
        unit: '',
        isOptional: false,
      };
    } else {
      this.notificationService.warning(
        'Fill all fields to add ingredient',
        5000
      );
    }
  }

  addStep() {
    if (this.newStep.trim()) {
      this.steps.push({ content: this.newStep.trim() });
      this.newStep = '';
    } else {
      this.notificationService.warning('Please enter a step', 5000);
    }
  }

  removeIngredient(index: number) {
    this.ingredients.splice(index, 1);
  }

  removeStep(index: number) {
    this.steps.splice(index, 1);
  }

  toggleOptional(): void {
    this.newIngredient.isOptional = !this.newIngredient.isOptional;
  }

  triggerFileInput() {
    this.fileInput.nativeElement.click();
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.processFile(input.files[0]);
    }
  }

  processFile(file: File) {
    if (file.size > 5242880) {
      this.notificationService.warning('The file size exceeds 5MB!', 5000);
      this.resetFile();
      return;
    }

    if (!file.type.startsWith('image/')) {
      this.notificationService.warning('Please upload an image file!', 5000);
      this.resetFile();
      return;
    }

    const reader = new FileReader();

    reader.onload = (e) => {
      const result = e.target?.result as string;
      this.imagePreview = result;

      this.recipeForm.patchValue({
        // update the form with the selected file
        images: file,
      });
    };

    reader.onerror = () => {
      this.notificationService.warning('Error reading file', 5000);
    };

    try {
      reader.readAsDataURL(file);
    } catch (error) {
      this.notificationService.warning('Error processing file', 5000);
      return;
    }

    this.notificationService.success(
      `Image "${file.name}" has been selected`,
      5000
    );
  }

  @HostListener('dragenter', ['$event'])
  @HostListener('dragover', ['$event'])
  onDragOver(event: DragEvent) {
    const target = event.target as HTMLElement;
    const dropzone = target.closest('.dropzone');

    if (dropzone) {
      event.preventDefault();
      event.stopPropagation(); // stops the event from bubbling up
      this.isDragging = true;
    }
  }

  @HostListener('dragleave', ['$event'])
  onDragLeave(event: DragEvent) {
    const target = event.target as HTMLElement;
    const dropzone = target.closest('.dropzone');

    if (dropzone && !dropzone.contains(event.relatedTarget as Node)) {
      event.preventDefault();
      event.stopPropagation();
      this.isDragging = false;
    }
  }

  @HostListener('drop', ['$event'])
  onDrop(event: DragEvent) {
    const target = event.target as HTMLElement;
    const dropzone = target.closest('.dropzone');

    if (dropzone) {
      event.preventDefault();
      event.stopPropagation();
      this.isDragging = false;

      const files = event.dataTransfer?.files;
      if (files && files.length > 0) {
        const file = files[0];

        const dataTransfer = new DataTransfer();
        dataTransfer.items.add(file);
        this.fileInput.nativeElement.files = dataTransfer.files;

        this.processFile(file);
      }
    }
  }

  resetFile(event?: Event) {
    if (event) {
      event.stopPropagation();
    }

    this.fileInput.nativeElement.value = '';
    this.recipeForm.patchValue({
      images: null,
    });
    this.imagePreview = null;
  }

  postRecipe() {
    if (this.isSubmitting) {
      this.notificationService.warning('Please wait, submitting recipe...');
      return;
    }

    const now = Date.now();
    if (now - this.lastSubmitAttempt < this.minTimeBetweenSubmits) {
      this.notificationService.warning('Please wait before submitting again');
      return;
    }

    this.submitAttempts$.next();
  }

  private executeSubmit() {
    if (this.isSubmitting) {
      return;
    }

    if (!this.authService.isAuthenticated()) {
      this.notificationService.error('User is not logged in!', 5000);
      return;
    }

    if (this.recipeForm.pristine || this.recipeForm.untouched) {
      this.notificationService.error('Please fill in the form!', 5000);
      return;
    }

    if (this.ingredients.length === 0) {
      this.notificationService.error(
        'Please add at least one ingredient!',
        5000
      );
      return;
    }

    if (this.steps.length === 0) {
      this.notificationService.error('Please add at least one step!', 5000);
      return;
    }

    this.isSubmitting = true;
    this.lastSubmitAttempt = Date.now();

    const formData = new FormData();
    formData.append(
      'recipe',
      JSON.stringify({
        recipeName: this.recipeForm.value.title!,
        category: this.recipeForm.value.category!,
        difficulty: this.recipeForm.value.difficulty,
        servings: this.recipeForm.value.servings,
        prepareTime: this.recipeForm.value.prepareTime,
        ingredients: this.ingredients,
        steps: this.steps,
        isPublic: this.recipeForm.value.isPublic,
        language: this.recipeForm.value.language,
      })
    );

    if (
      this.fileInput.nativeElement.files &&
      this.fileInput.nativeElement.files.length > 0
    ) {
      const file = this.fileInput.nativeElement.files[0];
      formData.append('images', file);
    }
    this.recipeService.postRecipe(formData).subscribe({
      next: () => {
        this.notificationService.success('Recipe has been created', 5000);
        this.resetForm();
        this.isSubmitting = false;
      },
      error: () => {
        this.notificationService.error('Failed to add the recipe!', 5000);
        this.isSubmitting = false;
      },
    });
  }

  private resetForm() {
    this.recipeForm.reset();
    this.ingredients = [];
    this.steps = [];
    this.resetFile();
  }
}
