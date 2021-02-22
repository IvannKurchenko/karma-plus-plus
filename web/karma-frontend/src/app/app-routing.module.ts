import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {SuggestComponent} from "./suggest/suggest.component";
import {FeedComponent} from "./feed/feed.component";

const routes: Routes = [
  {path: '', redirectTo: '/suggestion', pathMatch: 'full'},
  {path: 'suggestion', component: SuggestComponent},
  {path: 'feed', component: FeedComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
