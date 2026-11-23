import { Component,Input } from '@angular/core';
@Component({selector:'app-page-header',standalone:true,template:`<div class="page-heading"><div><small>{{eyebrow}}</small><h1>{{title}}</h1><p>{{subtitle}}</p></div><ng-content/></div>`})
export class PageHeaderComponent{@Input()title='';@Input()subtitle='';@Input()eyebrow='FINCORE WORKSPACE';}
