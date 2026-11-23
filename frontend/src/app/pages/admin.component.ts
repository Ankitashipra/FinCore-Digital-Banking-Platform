import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { AuthService } from '../core/auth.service';

interface AccountRequest { id:string; accountNumber:string; accountType:string; currency:string; balance:number; status:string; createdAt:string; }

interface CustomerApplication {
  id: string;
  fullName: string;
  email: string;
  applicationStatus: string;
  emailVerified: boolean;
  createdAt: string;
  reviewedBy?: string;
  reviewReason?: string;
}

@Component({
  selector: 'app-admin', standalone: true, imports: [CommonModule],
  template: `
  <div class="shell">
    <aside class="sidebar">
      <a class="brand"><b class="brand-mark">F</b>Fin<span>Core</span></a>
      <div class="nav-label">ADMINISTRATION</div>
      <nav><a class="active">Overview</a><a href="#applications">Customer applications</a></nav>
      <div class="side-bottom"><span>Administrator</span><button (click)="logout()">Sign out ↗</button></div>
    </aside>
    <main class="main">
      <header class="topbar"><span>FinCore <b>/</b> Admin console</span><span><small>● ADMIN SESSION</small></span></header>
      <section class="page">
        <div class="page-heading"><div><small>CONTROL CENTRE</small><h1>Administrator overview</h1><p>Review customer applications and manage access to the FinCore demo.</p></div></div>
        @if (error()) { <div class="alert error">{{error()}}</div> }
        @if (message()) { <div class="alert success">{{message()}}</div> }
        <div class="metrics">
          <article class="metric"><small>Total customer applications</small><strong>{{customers().length}}</strong><span>Registered customers</span></article>
          <article class="metric"><small>Awaiting review</small><strong>{{pendingCount()}}</strong><span>Verified email applications</span></article>
          <article class="metric"><small>Approved</small><strong>{{approvedCount()}}</strong><span>Customer access enabled</span></article>
          <article class="metric"><small>Rejected</small><strong>{{rejectedCount()}}</strong><span>Applications declined</span></article>
        </div>
        <section class="panel" id="applications">
          <div class="panel-head"><div><h2>Customer applications</h2><p>Review verified registrations before enabling customer access.</p></div><button class="button secondary" (click)="load()">Refresh</button></div>
          @if (loading()) { <div class="empty">Loading customer applications…</div> }
          @else if (!customers().length) { <div class="empty">No customer applications found.</div> }
          @else {
            <div class="table-wrap"><table><thead><tr><th>CUSTOMER</th><th>EMAIL</th><th>STATUS</th><th>EMAIL VERIFIED</th><th>REGISTERED</th><th class="right">ACTIONS</th></tr></thead>
              <tbody>@for (customer of customers(); track customer.id) {<tr>
                <td><b>{{customer.fullName}}</b></td><td>{{customer.email}}</td><td><span class="pill">{{customer.applicationStatus}}</span></td><td>{{customer.emailVerified ? 'Yes' : 'No'}}</td><td>{{customer.createdAt | date:'mediumDate'}}</td>
                <td class="right">@if(customer.applicationStatus === 'PENDING_REVIEW' && customer.emailVerified) {<button class="button primary" (click)="decide(customer, 'APPROVE')">Approve</button> <button class="button secondary" (click)="decide(customer, 'REJECT')">Reject</button>} @else {<small>Reviewed</small>}</td>
              </tr>}</tbody></table></div>
          }
        </section>
        <section class="panel" id="account-requests" style="margin-top:18px">
          <div class="panel-head"><div><h2>Account opening requests</h2><p>Approve or reject customer account requests. Approved accounts begin at ₹0.00.</p></div><button class="button secondary" (click)="loadAccounts()">Refresh</button></div>
          @if(accountError()){<div class="alert error">{{accountError()}}</div>}
          @if(!accountRequests().length){<div class="empty">No pending account requests.</div>}
          @else {<div class="table-wrap"><table><thead><tr><th>ACCOUNT NUMBER</th><th>TYPE</th><th>REQUESTED</th><th>STATUS</th><th class="right">ACTIONS</th></tr></thead><tbody>
          @for(a of accountRequests();track a.id){<tr><td><b>{{a.accountNumber}}</b></td><td>{{a.accountType}}</td><td>{{a.createdAt | date:'mediumDate'}}</td><td><span class="pill">{{a.status}}</span></td><td class="right"><button class="button primary" (click)="decideAccount(a,'APPROVE')">Approve</button> <button class="button secondary" (click)="decideAccount(a,'REJECT')">Reject</button></td></tr>}
          </tbody></table></div>}
        </section>
        <p class="muted">Demo environment · FinCore simulates account and customer approvals. No real banking or KYC decisions are performed.</p>
      </section>
      <footer>FinCore · Administrative workspace</footer>
    </main>
  </div>`
})
export class AdminComponent implements OnInit {
  readonly customers = signal<CustomerApplication[]>([]);
  readonly loading = signal(false);
  readonly accountRequests = signal<AccountRequest[]>([]);
  readonly accountError = signal('');
  readonly error = signal('');
  readonly message = signal('');
  readonly pendingCount = () => this.customers().filter(c => c.applicationStatus === 'PENDING_REVIEW').length;
  readonly approvedCount = () => this.customers().filter(c => c.applicationStatus === 'APPROVED').length;
  readonly rejectedCount = () => this.customers().filter(c => c.applicationStatus === 'REJECTED').length;

  constructor(private http: HttpClient, private auth: AuthService, private router: Router) {}
  ngOnInit(): void { this.load(); this.loadAccounts(); }
  private headers(): HttpHeaders { return new HttpHeaders({ Authorization: `Bearer ${this.auth.token()}` }); }
  load(): void {
    this.loading.set(true); this.error.set('');
    this.http.get<CustomerApplication[]>(`${environment.apiBaseUrl}/api/v1/admin/customers`, { headers: this.headers() }).subscribe({
      next: rows => this.customers.set(rows ?? []),
      error: err => this.error.set(err.status === 403 ? 'Admin permission was denied by the backend.' : `Could not load applications (${err.status || 'network error'}). Check that the Gateway routes /api/v1/admin/** to Identity Service.`),
      complete: () => this.loading.set(false)
    });
  }
  loadAccounts(): void {
    this.accountError.set('');
    this.http.get<AccountRequest[]>(`${environment.apiBaseUrl}/api/v1/accounts/admin/pending`, { headers: this.headers() }).subscribe({
      next: rows => this.accountRequests.set(rows ?? []),
      error: err => this.accountError.set(`Could not load account requests (${err.status || 'network error'}).`)
    });
  }
  decideAccount(account: AccountRequest, decision: 'APPROVE' | 'REJECT'): void {
    this.http.put<AccountRequest>(`${environment.apiBaseUrl}/api/v1/accounts/admin/${encodeURIComponent(account.id)}/decision`, { decision }, { headers: this.headers() }).subscribe({
      next: () => { this.message.set(`Account ${account.accountNumber} ${decision === 'APPROVE' ? 'approved' : 'rejected'}.`); this.loadAccounts(); },
      error: err => this.accountError.set(err.error?.message ?? `Unable to review account (${err.status}).`)
    });
  }

  decide(customer: CustomerApplication, decision: 'APPROVE' | 'REJECT'): void {
    const reason = decision === 'REJECT' ? window.prompt('Reason for rejection (optional):') : null;
    if (decision === 'REJECT' && reason === null) return;
    this.http.put<CustomerApplication>(`${environment.apiBaseUrl}/api/v1/admin/customers/${encodeURIComponent(customer.id)}/decision`, { decision, reason }, { headers: this.headers() }).subscribe({
      next: () => { this.message.set(`${customer.fullName}'s application was ${decision === 'APPROVE' ? 'approved' : 'rejected'}.`); this.load(); },
      error: err => this.error.set(err.error?.message ?? `Unable to ${decision.toLowerCase()} application (${err.status}).`)
    });
  }
  logout(): void { this.auth.logout(); this.router.navigateByUrl('/login'); }
}
