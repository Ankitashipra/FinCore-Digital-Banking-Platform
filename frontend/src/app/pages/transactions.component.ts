import { Component,OnInit,signal } from '@angular/core';
import { CurrencyPipe,DatePipe } from '@angular/common';
import { ApiService,asArray } from '../core/api.service';
import { Transaction } from '../core/models';
import { PageHeaderComponent } from '../shared/page-header.component';
@Component({selector:'app-transactions',standalone:true,imports:[CurrencyPipe,DatePipe,PageHeaderComponent],template:`
<app-page-header title="Transactions" subtitle="Review your recent financial activity."/><section class="panel"><div class="panel-head"><div><h2>Transaction history</h2><p>{{items().length}} transaction(s)</p></div><button class="button secondary" (click)="load()">Refresh ↻</button></div>
<div class="table-wrap"><table><thead><tr><th>DESCRIPTION</th><th>TYPE</th><th>DATE</th><th>STATUS</th><th class="right">AMOUNT</th></tr></thead><tbody>@for(t of items();track t.id??t.transactionId??$index){<tr><td><b>{{t.description??'Transaction'}}</b><small>{{t.id??t.transactionId??''}}</small></td><td>{{t.type??'—'}}</td><td>{{(t.createdAt??t.transactionDate)?((t.createdAt??t.transactionDate)|date:'medium'):'—'}}</td><td>{{t.status??'—'}}</td><td class="right"><b>{{(t.amount??0)|currency:(t.currency??'INR'):'symbol':'1.2-2'}}</b></td></tr>}</tbody></table></div>
@if(error()){<div class="alert error">{{error()}}</div>}@if(!items().length){<div class="empty">No transactions returned. Confirm the transaction endpoint response.</div>}</section>`})
export class TransactionsComponent implements OnInit{readonly items=signal<Transaction[]>([]);readonly error=signal('');constructor(private api:ApiService){}ngOnInit(){this.load();}load(){this.error.set('');this.api.transactions().subscribe({next:v=>this.items.set(asArray(v)),error:e=>this.error.set(e.error?.message??`Unable to load transactions (${e.status||'network error'}).`)});}}
