import { Routes } from '@angular/router';
import { AboutUsComponent } from './components/about-us/about-us.component';
import { LandingPageComponent } from './components/landing-page/landing-page.component';
import { PrivacyPolicyComponent } from './components/privacy-policy/privacy-policy.component';
import { TermsOfServiceComponent } from './components/terms-of-service/terms-of-service.component';
import { ContactComponent } from './components/contact/contact.component';
import { LoginComponent } from './components/login/login.component';
import { RecipesComponent } from './components/recipes/recipes.component';
import { RecipeDetailedComponent } from './components/recipe-detailed/recipe-detailed.component';
import { ProfileComponent } from './components/profile/profile.component';
import { AddRecipePanelComponent } from './components/add-recipe-panel/add-recipe-panel.component';
import { EditRecipeComponent } from './components/edit-recipe/edit-recipe.component';
import { AdminPanelComponent } from './components/admin-panel/admin-panel.component';
import { NotFoundComponent } from './components/not-found/not-found.component';

export const routes: Routes = [
  {
    path: '',
    component: LandingPageComponent,
    data: { title: 'Cibaria | From Plans To Plates' },
  },
  {
    path: 'terms-of-service',
    component: TermsOfServiceComponent,
    data: { title: 'Cibaria | Terms Of Service' },
  },
  {
    path: 'about-us',
    component: AboutUsComponent,
    data: { title: 'Cibaria | About Us' },
  },
  {
    path: 'privacy-policy',
    component: PrivacyPolicyComponent,
    data: { title: 'Cibaria | Privacy Policy' },
  },
  {
    path: 'contact',
    component: ContactComponent,
    data: { title: 'Cibaria | Contact' },
  },
  {
    path: 'login',
    component: LoginComponent,
    data: { title: 'Cibaria | Login to explore the world of Cibaria' },
  },
  {
    path: 'recipes',
    component: RecipesComponent,
    data: { title: 'Cibaria | Recipes' },
  },
  { path: 'recipes/:id', component: RecipeDetailedComponent },
  {
    path: 'add-recipe',
    component: AddRecipePanelComponent,
    data: { title: 'Cibaria | Add Recipe' },
  },
  {
    path: 'update-recipe/:id',
    component: EditRecipeComponent,
    data: { title: 'Cibaria | Update Recipe' },
  },
  {
    path: 'profile',
    component: ProfileComponent,
    data: { title: 'Cibaria | Your profile' },
  },
  {
    path: 'admin',
    component: AdminPanelComponent,
    data: { title: 'Cibaria | Admin Panel' },
  },

  { 
    path: 'not-found', 
    component: NotFoundComponent,
    data: { title: 'Cibaria | Strona nie znaleziona' }
  },
  { path: '**', component: NotFoundComponent },
];
