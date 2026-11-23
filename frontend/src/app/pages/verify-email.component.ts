import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiService } from '../core/api.service';

@Component({
 selector:'app-verify-email',standalone:true,imports:[FormsModule,RouterLink],
 template:`<main class="login-page"><section class="login-card">
 <a class="brand login-brand" routerLink="/login"><b class="brand-mark">F</b>Fin<span>Core</span></a>
 <small class="eyebrow">EMAIL VERIFICATION</small><h1>Check your<br>inbox.</h1>
 <p>Enter the six-digit code sent to <b>{{email}}</b>. The code expires in five minutes.</p>
 <form (ngSubmit)="verify()"><label>Verification code</label>
 <input name="otp" [(ngModel)]="otp" inputmode="numeric" maxlength="6" pattern="[0-9]{6}" placeholder="000000" required>
 @if(error()){<div class="alert error">{{error()}}</div>} @if(success()){<div class="alert success">{{success()}}</div>}
 <button class="button primary full" [disabled]="loading()">{{loading()?'Verifying…':'Verify email →'}}</button>
 </form>
 <button class="button secondary full" (click)="resend()" [disabled]="resending()"> {{resending()?'Sending…':'Resend code'}} </button>
 <small class="login-foot">Already verified? <a routerLink="/login">Sign in</a></small>
 </section><aside class="login-art"><div><small>FINCORE SECURITY</small><h2>One more step<br>to get started.</h2><p>Email verification confirms email ownership. FinCore reviews customer applications separately; this is not Aadhaar or identity verification.</p></div></aside></main>`
})
export class VerifyEmailComponent {
 email='';otp='';readonly loading=signal(false);readonly resending=signal(false);readonly error=signal('');readonly success=signal('');
 constructor(private api:ApiService,route:ActivatedRoute,private router:Router){
  this.email=route.snapshot.queryParamMap.get('email')??'';
 }
 verify(){this.error.set('');if(!/^\d{6}$/.test(this.otp)){this.error.set('Enter the six-digit code.');return;}
 this.loading.set(true);this.api.verifyEmail(this.email,this.otp).subscribe({next:()=>{this.success.set('Email verified. Your customer application is now pending administrator review. You can sign in after approval.');setTimeout(()=>this.router.navigate(['/login']),2400);},
 error:(e:HttpErrorResponse)=>{this.loading.set(false);this.error.set(e.error?.detail??e.error?.message??`Verification failed (${e.status}).`);},complete:()=>this.loading.set(false)});}
 resend(){this.error.set('');this.resending.set(true);this.api.resendVerification(this.email).subscribe({next:()=>this.success.set('A new code has been sent if your registration is pending.'),error:(e:HttpErrorResponse)=>this.error.set(e.error?.detail??e.error?.message??`Could not resend (${e.status}).`),complete:()=>this.resending.set(false)});}
}
