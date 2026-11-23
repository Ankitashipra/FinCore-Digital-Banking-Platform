import { Component,OnInit,signal } from '@angular/core';
import { CurrencyPipe,DatePipe } from '@angular/common';
import { ApiService,asArray } from '../core/api.service';
import { Account,Payment,NotificationItem } from '../core/models';
import { PageHeaderComponent } from '../shared/page-header.component';
@Component({selector:'app-dashboard',standalone:true,imports:[CurrencyPipe,DatePipe,PageHeaderComponent],template:`
<app-page-header title="Good to see you." subtitle="Here’s what’s happening across your FinCore workspace today."/>
@if(error()){<div class="alert error">{{error()}}</div>}
<div class="metrics"><article class="metric"><small>Total balance</small><strong>{{total()|currency:'INR':'symbol':'1.2-2'}}</strong><span>Across {{accounts().length}} accounts</span></article><article class="metric"><small>Accounts</small><strong>{{accounts().length}}</strong><span>Connected accounts</span></article><article class="metric"><small>Payments</small><strong>{{payments().length}}</strong><span>Payment records loaded</span></article><article class="metric"><small>Unread updates</small><strong>{{unread()}}</strong><span>Notifications</span></article></div>
<div class="columns"><section class="panel"><div class="panel-head"><div><h2>Your accounts</h2><p>A quick view of your balances</p></div><a routerLink="/accounts">All accounts →</a></div>
@for(a of accounts().slice(0,5);track a.id??a.accountId??$index){<div class="list-row"><b class="icon-box">▤</b><div class="grow"><strong>{{a.accountName??a.name??a.accountType??a.type??'Account'}}</strong><small>{{a.accountNumber??a.id??a.accountId??''}}</small></div><strong>{{(a.balance??a.availableBalance??0)|currency:(a.currency??'INR'):'symbol':'1.2-2'}}</strong></div>}
@if(!accounts().length){<div class="empty">No accounts returned by the API.</div>}</section>
<section class="panel"><div class="panel-head"><div><h2>Recent payments</h2><p>Latest payment activity</p></div></div>
@for(p of payments().slice(0,5);track p.paymentId??p.id??$index){<div class="list-row"><b class="icon-box">↗</b><div class="grow"><strong>{{p.description??'Payment'}}</strong><small>{{p.createdAt?(p.createdAt|date:'mediumDate'):'Date unavailable'}}</small></div><div class="amount"><strong>{{(p.amount??0)|currency:(p.currency??'INR'):'symbol':'1.2-2'}}</strong><small>{{p.status??'—'}}</small></div></div>}
@if(!payments().length){<div class="empty">No payment records loaded.</div>}</section></div><div class="refresh"><span>● Live data from FinCore API</span><button (click)="load()">Refresh ↻</button></div>`})
export class DashboardComponent implements OnInit{
 readonly accounts=signal<Account[]>([]);readonly payments=signal<Payment[]>([]);readonly notifications=signal<NotificationItem[]>([]);readonly error=signal('');
 total=()=>this.accounts().reduce((s,a)=>s+Number(a.balance??a.availableBalance??0),0);unread=()=>this.notifications().filter(n=>!n.read).length;
 constructor(private api:ApiService){}ngOnInit(){this.load();}
 load(){this.error.set('');this.api.accounts().subscribe({next:v=>this.accounts.set(asArray(v)),error:()=>this.error.set('Could not load accounts. Verify the accounts endpoint.')});this.api.payments().subscribe({next:v=>this.payments.set(asArray(v)),error:()=>this.payments.set([])});this.api.notifications().subscribe({next:v=>this.notifications.set(asArray(v)),error:()=>this.notifications.set([])});}
}
