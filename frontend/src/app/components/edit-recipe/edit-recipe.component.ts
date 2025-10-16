import {
  Component,
  ElementRef,
  HostListener,
  OnInit,
  OnDestroy,
  ViewChild,
} from '@angular/core';
import { Recipe } from '../../Interface/recipe';
import { Ingredients } from '../../Interface/ingredients';
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { RecipeService } from '../../services/recipe.service';
import { ToastNotificationComponent } from '../toast-notification/toast-notification.component';
import { NotificationService } from '../../services/notification.service';
import { AuthService } from '../../services/auth.service';
import { TranslateModule } from '@ngx-translate/core';
import { Subject, takeUntil, debounceTime } from 'rxjs';

@Component({
  selector: 'app-edit-recipe',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    TranslateModule,
    ToastNotificationComponent,
  ],
  templateUrl: './edit-recipe.component.html',
  styleUrl: './edit-recipe.component.css',
})
export class EditRecipeComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private updateAttempts$ = new Subject<void>();

  isUpdating = false;
  private lastUpdateAttempt = 0;
  private readonly minTimeBetweenUpdates = 3000; // 3 seconds
  isLoggedIn = false;
  recipeId!: number;
  isDragging = false;
  imagePreview: string | null = null;
  currentImageUrl: string | null = null;
  hasExistingImage = false;
  recipeDetails!: Recipe;
  ingredients: Ingredients[] = [];
  steps: { content: string }[] = [];

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
    private route: ActivatedRoute,
    private router: Router,
    private notificationService: NotificationService,
    private authService: AuthService
  ) {
    // Setup debounced update attempts
    this.updateAttempts$
      .pipe(debounceTime(1000), takeUntil(this.destroy$))
      .subscribe(() => {
        this.executeUpdate();
      });
  }

  ngOnInit(): void {
    this.getToken();
    if (!this.isLoggedIn) {
      this.router.navigate(['/not-found']);
    }

    this.route.params.subscribe((params) => {
      this.recipeId = +params['id'];
      if (this.recipeId) {
        this.loadRecipeDetails();
      }
    });
  }

  getToken(): void {
    this.isLoggedIn = this.authService.isAuthenticated();
  }

  recipeForm = new FormGroup({
    title: new FormControl('', [Validators.required]),
    category: new FormControl('', [Validators.required]),
    servings: new FormControl(1, [Validators.required, Validators.min(1)]),
    prepareTime: new FormControl(1, [Validators.required, Validators.min(1)]),
    difficulty: new FormControl(1, [Validators.required]),
    images: new FormControl<File | null>(null, [Validators.required]),
    isPublic: new FormControl(false, [Validators.required]),
    language: new FormControl('english', [Validators.required]),
    ingredients: new FormControl(''),
    quantity: new FormControl(0),
    unit: new FormControl(''),
    isOptional: new FormControl(false),
    steps: new FormControl(''),
  });

  private loadRecipeDetails(): void {
    this.recipeService.loadRecipeDetails(this.recipeId).subscribe({
      next: (response) => {
        this.recipeDetails = response;

        // Mapowanie starych polskich nazw kategorii na nowe klucze
        const categoryMap: { [key: string]: string } = {
          śniadanie: 'BREAKFAST',
          obiad: 'LUNCH',
          kolacja: 'DINNER',
          deser: 'DESSERT',
          przekąska: 'SNACK',
          napój: 'DRINK',
          sałatka: 'SALAD',
          zupa: 'SOUP',
        };

        const mappedCategory =
          categoryMap[response.category] || response.category;

        this.recipeForm.patchValue({
          title: response.recipeName,
          category: mappedCategory,
          servings: response.servings,
          prepareTime: response.prepareTime,
          difficulty: response.difficulty,
          isPublic: response.isPublic,
          language: response.language || 'english',
        });

        this.ingredients = this.recipeDetails.ingredients.map((ingredient) => ({
          name: ingredient.ingredientName,
          quantity: ingredient.quantity,
          unit: ingredient.unit,
          isOptional: ingredient.isOptional || false,
        }));

        this.steps = this.recipeDetails.steps.map((step) => ({
          content: Array.isArray(step.content)
            ? step.content.join(' ')
            : step.content,
        }));

        if (response.images && response.images.length > 0) {
          this.currentImageUrl = response.images[0].imageUrl;
          this.imagePreview = this.currentImageUrl;
          this.hasExistingImage = true;
          this.recipeForm.patchValue({
            images: null,
          });
        } else {
          // No images in response - clear image display
          this.currentImageUrl = null;
          this.imagePreview = null;
          this.hasExistingImage = false;
          this.recipeForm.patchValue({
            images: null,
          });
        }
      },
      error: (err) => {
        // Check if it's a 404 error (recipe not found)
        if (err.status === 404) {
          this.router.navigate(['/not-found']);
        } else {
          this.notificationService.error('Failed to load recipe details', 5000);
        }
      },
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  addIngredient() {
    const ingredientName = this.recipeForm.get('ingredients')?.value || '';
    const quantity = this.recipeForm.get('quantity')?.value || 0;
    const unit = this.recipeForm.get('unit')?.value || '';
    const isOptional = this.recipeForm.get('isOptional')?.value || false;

    const ingredientExists = this.ingredients.some(
      (ingredient) =>
        ingredient.name.toLowerCase() === ingredientName.toLowerCase()
    );

    if (ingredientExists) {
      this.notificationService.warning(
        'This ingredient already exists in the recipe.',
        5000
      );
      return;
    }

    if (ingredientName && quantity > 0 && unit && unit !== 'Choose a unit') {
      this.ingredients.push({
        name: ingredientName,
        quantity: quantity,
        unit: unit,
        isOptional: isOptional,
      });

      this.recipeForm.patchValue({
        ingredients: '',
        quantity: 0,
        unit: '',
        isOptional: false,
      });
    } else {
      this.notificationService.warning(
        'Please fill in all fields for the ingredient.',
        5000
      );
    }
  }

  addStep() {
    const stepContent = this.recipeForm.get('steps')?.value || '';

    if (stepContent.trim()) {
      this.steps.push({ content: stepContent.trim() });
      this.recipeForm.patchValue({ steps: '' });
    } else {
      this.notificationService.warning(
        'Please fill in the step description.',
        5000
      );
    }
  }

  removeIngredient(index: number) {
    this.ingredients.splice(index, 1);
  }

  removeStep(index: number) {
    this.steps.splice(index, 1);
  }

  toggleOptional(): void {
    const currentValue = this.recipeForm.get('isOptional')?.value || false;
    this.recipeForm.patchValue({ isOptional: !currentValue });
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
    this.currentImageUrl = null;
    this.hasExistingImage = false;
  }

  updateRecipe() {
    if (this.isUpdating) {
      this.notificationService.warning('Please wait, updating recipe...');
      return;
    }

    const now = Date.now();
    if (now - this.lastUpdateAttempt < this.minTimeBetweenUpdates) {
      this.notificationService.warning('Please wait before updating again');
      return;
    }

    this.updateAttempts$.next();
  }

  private executeUpdate() {
    if (this.isUpdating) {
      return;
    }

    if (this.ingredients.length === 0) {
      this.notificationService.error(
        'Please add at least one ingredient.',
        5000
      );
      return;
    }

    if (this.steps.length === 0) {
      this.notificationService.error('Please add at least one step.', 5000);
      return;
    }

    this.isUpdating = true;
    this.lastUpdateAttempt = Date.now();

    const formData = new FormData();

    const recipeData = {
      recipeName: this.recipeForm.value.title!,
      category: this.recipeForm.value.category!,
      difficulty: Number(this.recipeForm.value.difficulty),
      servings: Number(this.recipeForm.value.servings),
      prepareTime: Number(this.recipeForm.value.prepareTime),
      ingredients: this.ingredients.map((ingredient) => ({
        ingredientName: ingredient.name,
        quantity: ingredient.quantity,
        unit: ingredient.unit,
        isOptional: ingredient.isOptional || false,
      })),
      steps: this.steps,
      isPublic: this.recipeForm.value.isPublic,
      language: this.recipeForm.value.language,
    };

    formData.append('recipe', JSON.stringify(recipeData));

    const imageInput = document.querySelector('.image') as HTMLInputElement;
    if (imageInput.files && imageInput.files.length > 0) {
      const file = imageInput.files[0];
      formData.append('images', file);
    } else if (this.hasExistingImage && this.currentImageUrl) {
      formData.append('keepExistingImage', 'true');
    }

    this.recipeService.updateRecipe(formData, this.recipeId).subscribe({
      next: () => {
        this.notificationService.success('Recipe updated successfully!', 5000);
        this.isUpdating = false;
        // Reload recipe data to reflect changes (especially image deletion)
        this.loadRecipeDetails();
      },
      error: () => {
        this.notificationService.error('Failed to update recipe', 5000);
        this.isUpdating = false;
      },
    });
  }
}
