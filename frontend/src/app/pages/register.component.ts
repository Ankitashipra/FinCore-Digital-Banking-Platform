import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiService } from '../core/api.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
  <main class="login-page"><section class="login-card">
    <a class="brand login-brand" routerLink="/login"><b class="brand-mark">F</b>Fin<span>Core</span></a>
    <small class="eyebrow">CUSTOMER ONBOARDING</small><h1>Create<br>account.</h1>
    <p>Create your customer application. Verify your email, then wait for an administrator to review it before signing in.</p>
    <form (ngSubmit)="submit()">
      <label>Full name</label><input name="fullName" [(ngModel)]="fullName" autocomplete="name" maxlength="100" required>
      <label>Email address</label><input type="email" name="email" [(ngModel)]="email" autocomplete="email" maxlength="150" required>
      <label>Password</label><input type="password" name="password" [(ngModel)]="password" autocomplete="new-password" minlength="8" maxlength="72" required>
      <label>Confirm password</label><input type="password" name="confirm" [(ngModel)]="confirmPassword" autocomplete="new-password" required>
      @if(error()){<div class="alert error">{{error()}}</div>}
      <button class="button primary full" [disabled]="loading()">{{loading()?'Registering…':'Create account →'}}</button>
    </form>
    <small class="login-foot">Already registered? <a routerLink="/login">Sign in</a></small>
  </section><aside class="login-art"><div><small>FINANCIAL OPERATIONS</small><h2>Your finances.<br>Your control.</h2><p>Secure access to your FinCore financial workspace.</p></div></aside></main>`
})
export class RegisterComponent {
  fullName=''; email=''; password=''; confirmPassword='';
  readonly loading=signal(false); readonly error=signal('');
  constructor(private api:ApiService, private router:Router) {}
  submit() {
    this.error.set('');
    if(this.password !== this.confirmPassword){this.error.set('Passwords do not match.');return;}
    if(this.password.length < 8 || this.password.length > 72){this.error.set('Password must contain 8 to 72 characters.');return;}
    this.loading.set(true);
    this.api.register({fullName:this.fullName.trim(),email:this.email.trim(),password:this.password})
      .subscribe({next:()=>this.router.navigate(['/verify-email'],{queryParams:{email:this.email.trim()}}),
      error:(e:HttpErrorResponse)=>{this.loading.set(false);this.error.set(e.error?.detail??e.error?.message??(e.status===409?'Email is already registered.':`Registration failed (${e.status||'network error'}).`));},
      complete:()=>this.loading.set(false)});
  }
}
