import { Component,signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../core/auth.service';
@Component({selector:'app-login',standalone:true,imports:[FormsModule,RouterLink],template:`
<main class="login-page"><section class="login-card"><a class="brand login-brand" href="/login"><b class="brand-mark">F</b>Fin<span>Core</span></a><small class="eyebrow">YOUR FINANCIAL WORKSPACE</small><h1>Welcome<br>back.</h1><p>Sign in to manage your accounts, payments and financial activity.</p>
<form (ngSubmit)="submit()"><label>Email address</label><input type="email" name="email" [(ngModel)]="email" autocomplete="username" placeholder="you@example.com" required><label>Password</label><input type="password" name="password" [(ngModel)]="password" autocomplete="current-password" placeholder="Enter your password" required>
@if(error()){<div class="alert error">{{error()}}</div>}<button class="button primary full" [disabled]="loading()">{{loading()?'Signing in…':'Sign in securely →'}}</button></form><small class="login-foot">New to FinCore? <a routerLink="/register">Create an account</a><br><br>● Protected connection through FinCore Gateway</small></section><aside class="login-art"><div><small>FINANCIAL OPERATIONS</small><h2>Clarity for every<br>financial move.</h2><p>One place to see your money, move with confidence, and stay in control.</p></div></aside></main>`})
export class LoginComponent{
 email='';password='';readonly loading=signal(false);readonly error=signal('');
 constructor(private auth:AuthService,private router:Router){}
 submit(){this.error.set('');if(!this.email.trim()||!this.password){this.error.set('Enter your email and password.');return;}this.loading.set(true);this.auth.login(this.email.trim(),this.password).subscribe({next:()=>this.router.navigateByUrl(this.auth.role()==='ADMIN'?'/admin':'/dashboard'),error:(e:HttpErrorResponse)=>{this.loading.set(false);this.error.set(e.status===401?'Email or password is incorrect.':e.status===0?'Cannot reach Gateway at localhost:8086.':e.error?.message??`Sign in failed (${e.status}).`);},complete:()=>this.loading.set(false)});}
}
