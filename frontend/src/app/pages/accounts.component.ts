import { Component,OnInit,signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService,asArray } from '../core/api.service';
import { Account } from '../core/models';
import { PageHeaderComponent } from '../shared/page-header.component';
@Component({selector:'app-accounts',standalone:true,imports:[CurrencyPipe,FormsModule,PageHeaderComponent],template:`
<app-page-header title="Accounts" subtitle="View and open accounts connected to your FinCore profile."/>
<section class="panel">
<div class="panel-head"><div><h2>Request a new account</h2><p>Every account request is reviewed by a FinCore administrator. Approved accounts open with ₹0.00.</p></div></div>
<form class="account-open-form" (ngSubmit)="openAccount()">
<label>Account type</label><select name="accountType" [(ngModel)]="accountType"><option value="SAVINGS">Savings</option><option value="CURRENT">Current</option></select>
<button class="button primary" type="submit" [disabled]="creating()">{{creating()?'Submitting…':'Submit account request →'}}</button>
</form>
@if(message()){<div class="alert success">{{message()}}</div>}
@if(error()){<div class="alert error">{{error()}}</div>}
</section>
<section class="panel" style="margin-top:17px"><div class="panel-head"><div><h2>All accounts</h2><p>{{accounts().length}} account(s) found</p></div><button class="button secondary" (click)="load()">Refresh ↻</button></div>
<div class="table-wrap"><table><thead><tr><th>ACCOUNT</th><th>ACCOUNT NUMBER</th><th>TYPE</th><th>STATUS</th><th class="right">BALANCE</th></tr></thead><tbody>@for(a of accounts();track a.id??a.accountId??$index){<tr><td><b>{{a.accountName??a.name??'FinCore account'}}</b><small>{{a.id??a.accountId??''}}</small></td><td>{{a.accountNumber??'—'}}</td><td>{{a.accountType??a.type??'—'}}</td><td><span class="pill" [class.pending-pill]="a.status==='PENDING_APPROVAL'" [class.rejected-pill]="a.status==='REJECTED'">{{a.status??'UNKNOWN'}}</span></td><td class="right"><b>{{(a.balance??a.availableBalance??0)|currency:(a.currency??'INR'):'symbol':'1.2-2'}}</b></td></tr>}</tbody></table></div>
@if(!accounts().length){<div class="empty">No accounts yet. Submit your first account request above.</div>}</section>`})
export class AccountsComponent implements OnInit{
 readonly accounts=signal<Account[]>([]);readonly error=signal('');readonly message=signal('');readonly creating=signal(false);
 accountType='SAVINGS';
 constructor(private api:ApiService){}
 ngOnInit(){this.load();}
 load(){this.error.set('');this.api.accounts().subscribe({next:v=>this.accounts.set(asArray(v)),error:e=>this.error.set(e.error?.detail??e.error?.message??`Unable to load accounts (${e.status||'network error'}).`)});}
 openAccount(){this.error.set('');this.message.set('');this.creating.set(true);this.api.createAccount(this.accountType).subscribe({next:(created:Account)=>{this.message.set(`Account request submitted. Your ${created.accountType??this.accountType} account is pending administrator approval. No funds have been added.`);this.load();},error:e=>this.error.set(e.error?.detail??e.error?.message??`Account opening failed (${e.status}).`),complete:()=>this.creating.set(false)});}
}
