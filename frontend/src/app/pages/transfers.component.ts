import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService, asArray } from '../core/api.service';
import { Account, Payment } from '../core/models';
import { PageHeaderComponent } from '../shared/page-header.component';
import { finalize } from 'rxjs';

@Component({
 selector:'app-transfers',standalone:true,imports:[FormsModule,PageHeaderComponent],
 template:`<app-page-header title="Make a transfer" subtitle="Send funds to any active FinCore account."/>
 <div class="transfer-grid"><section class="panel"><div class="panel-head"><div><h2>Transfer details</h2><p>Confirm the recipient account number and amount.</p></div></div>
 @if(error()){<div class="alert error">{{error()}}</div>} @if(success()){<div class="alert success">{{success()}}</div>}
 <form (ngSubmit)="submit()"><label>From account</label><select name="source" [(ngModel)]="source" required><option value="">Select source account</option>
 @for(a of accounts();track a.id??a.accountId){<option [value]="a.id??a.accountId">{{a.accountType??'Account'}} · {{a.accountNumber??a.id}}</option>}</select>
 <label>Recipient account number</label><input name="recipient" [(ngModel)]="recipientAccountNumber" inputmode="numeric" maxlength="20" placeholder="Enter FinCore account number" required>
 <label>Amount (INR)</label><input name="amount" type="number" min="0.01" step="0.01" [(ngModel)]="amount" placeholder="0.00" required>
 <label>Description</label><input name="description" [(ngModel)]="description" placeholder="Transfer description">
 <p class="hint">Recipient account is checked before submission. Transfers are processed atomically with ledger entries.</p>
 <button class="button primary full" type="submit" [disabled]="loading()||!accounts().length">{{loading()?'Submitting…':'Review and transfer →'}}</button></form></section>
 <aside class="panel"><h2>Transfer security</h2><p class="muted">01 · Choose your source account</p><p class="muted">02 · Enter the recipient's account number</p><p class="muted">03 · Verify the amount before sending</p><div class="hint">The recipient receives an in-app notification when the transfer succeeds. Never share your password or OTP.</div></aside></div>`
})
export class TransfersComponent implements OnInit {
 readonly accounts=signal<Account[]>([]);readonly loading=signal(false);readonly error=signal('');readonly success=signal('');
 source='';recipientAccountNumber='';amount:number|null=null;description='';private pendingIdempotencyKey:string|null=null;
 constructor(private api:ApiService){}
 ngOnInit(){this.api.accounts().subscribe({next:v=>this.accounts.set(asArray(v)),error:()=>this.error.set('Could not load accounts.')});}
 submit(){
  this.error.set('');this.success.set('');
  if(!this.source||!this.recipientAccountNumber.trim()||!this.amount||!Number.isFinite(this.amount)||this.amount<=0){this.error.set('Complete all fields with a valid amount.');return;}
  if(!this.pendingIdempotencyKey)this.pendingIdempotencyKey=crypto.randomUUID();
  this.loading.set(true);
  this.api.lookupAccount(this.recipientAccountNumber.trim()).subscribe({
   next:recipient=>{
    if(recipient.accountId===this.source){this.loading.set(false);this.error.set('Source and destination accounts must differ.');return;}
    this.api.createPayment({sourceAccountId:this.source,destinationAccountId:recipient.accountId,amount:this.amount!,currency:'INR',description:this.description.trim()||'FinCore transfer'},this.pendingIdempotencyKey!)
     .pipe(finalize(()=>this.loading.set(false))).subscribe({
      next:(payment:Payment)=>{this.success.set(`Transfer submitted · Reference: ${payment.paymentId??payment.id??'created'} · Status: ${payment.status??'completed'}`);this.pendingIdempotencyKey=null;this.recipientAccountNumber='';this.amount=null;this.description='';},
      error:e=>this.error.set(e.error?.detail??e.error?.message??`Transfer failed (${e.status||'network error'}).`)
     });
   },
   error:e=>{this.loading.set(false);this.error.set(e.error?.detail??e.error?.message??'Recipient account could not be verified.');}
  });
 }
}
