export interface LoginResponse { accessToken:string; tokenType?:string; user?:{id?:string;email?:string}; }
export interface Account { id?:string; accountId?:string; accountNumber?:string; accountName?:string; name?:string; accountType?:string; type?:string; balance?:number; availableBalance?:number; currency?:string; status?:string; }
export interface Payment { paymentId?:string; id?:string; sourceAccountId?:string; destinationAccountId?:string; amount?:number; currency?:string; status?:string; description?:string; createdAt?:string; }
export interface Transaction { id?:string; transactionId?:string; type?:string; amount?:number; currency?:string; description?:string; status?:string; createdAt?:string; transactionDate?:string; }
export interface NotificationItem { id?:string; notificationId?:string; type?:string; title?:string; message?:string; read?:boolean; createdAt?:string; }
