import { Component,OnInit,signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ApiService,asArray } from '../core/api.service';
import { NotificationItem } from '../core/models';
import { PageHeaderComponent } from '../shared/page-header.component';
@Component({selector:'app-notifications',standalone:true,imports:[DatePipe,PageHeaderComponent],template:`
<app-page-header title="Notifications" subtitle="Stay up to date with account and payment events."/><section class="panel"><div class="panel-head"><div><h2>All notifications</h2><p>{{items().length}} update(s)</p></div><button class="button secondary" (click)="load()">Refresh ↻</button></div>
@for(n of items();track n.id??n.notificationId??$index){<article class="notification"><div class="icon-box">♧</div><div class="grow"><b>{{n.title??n.type??'Notification'}}</b><p>{{n.message??'You have a new update.'}}</p><small>{{n.createdAt?(n.createdAt|date:'medium'):'Date unavailable'}}</small></div>@if(!n.read){<span class="unread">NEW</span>}</article>}
@if(error()){<div class="alert error">{{error()}}</div>}@if(!items().length){<div class="empty">No notifications found.</div>}</section>`})
export class NotificationsComponent implements OnInit{readonly items=signal<NotificationItem[]>([]);readonly error=signal('');constructor(private api:ApiService){}ngOnInit(){this.load();}load(){this.error.set('');this.api.notifications().subscribe({next:v=>this.items.set(asArray(v)),error:e=>this.error.set(e.error?.message??`Unable to load notifications (${e.status||'network error'}).`)});}}
