import { Component,signal } from '@angular/core';
import { Router,RouterLink,RouterLinkActive,RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';
@Component({selector:'app-root',standalone:true,imports:[RouterOutlet,RouterLink,RouterLinkActive],template:`
@if(auth.isAuthenticated() && auth.role()==='ADMIN'){<router-outlet/>}@else if(auth.isAuthenticated()){<div class="shell"><aside class="sidebar" [class.open]="menu()">
<a class="brand" routerLink="/dashboard"><b class="brand-mark">F</b>Fin<span>Core</span></a><div class="nav-label">WORKSPACE</div><nav>
<a routerLink="/dashboard" routerLinkActive="active">◫ &nbsp; Overview</a><a routerLink="/accounts" routerLinkActive="active">▤ &nbsp; Accounts</a><a routerLink="/transfers" routerLinkActive="active">⇄ &nbsp; Transfers</a><a routerLink="/transactions" routerLinkActive="active">↗ &nbsp; Transactions</a><a routerLink="/notifications" routerLinkActive="active">♧ &nbsp; Notifications</a></nav>
<div class="side-bottom"><small>● Secure session</small><button (click)="logout()">↪ Sign out</button></div></aside>
@if(menu()){<button class="scrim" (click)="menu.set(false)" aria-label="Close navigation"></button>}
<main class="main"><header class="topbar"><button class="menu-btn" (click)="menu.set(!menu())">☰</button><span>Workspace <b>/</b> FinCore</span><small>● Systems operational</small></header><section class="page"><router-outlet/></section><footer>FinCore · Financial operations workspace</footer></main></div>}@else{<router-outlet/>}`})
export class AppComponent {
 readonly menu=signal(false);
 constructor(public auth:AuthService,private router:Router){}
 logout(){this.auth.logout();this.router.navigateByUrl('/login');}
}
