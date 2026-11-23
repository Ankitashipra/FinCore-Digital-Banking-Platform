import { Routes } from '@angular/router';
import { adminGuard, customerGuard } from './core/role.guard';
import { AdminComponent } from './pages/admin.component';
import { LoginComponent } from './pages/login.component';
import { RegisterComponent } from './pages/register.component';
import { VerifyEmailComponent } from './pages/verify-email.component';
import { DashboardComponent } from './pages/dashboard.component';
import { AccountsComponent } from './pages/accounts.component';
import { TransfersComponent } from './pages/transfers.component';
import { TransactionsComponent } from './pages/transactions.component';
import { NotificationsComponent } from './pages/notifications.component';
export const routes:Routes=[
 {path:'login',component:LoginComponent},
 {path:'register',component:RegisterComponent},
 {path:'verify-email',component:VerifyEmailComponent},
 {path:'admin',component:AdminComponent,canActivate:[adminGuard]},
 {path:'dashboard',component:DashboardComponent,canActivate:[customerGuard]},
 {path:'accounts',component:AccountsComponent,canActivate:[customerGuard]},
 {path:'transfers',component:TransfersComponent,canActivate:[customerGuard]},
 {path:'transactions',component:TransactionsComponent,canActivate:[customerGuard]},
 {path:'notifications',component:NotificationsComponent,canActivate:[customerGuard]},
 {path:'',pathMatch:'full',redirectTo:'dashboard'},{path:'**',redirectTo:'dashboard'}
];
