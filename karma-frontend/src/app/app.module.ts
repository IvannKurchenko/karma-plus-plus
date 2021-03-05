import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {BrowserModule} from '@angular/platform-browser';
import {HttpClientModule} from "@angular/common/http";

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';

import {SuggestApiService} from "./suggest/suggest-api.service";
import {SuggestComponent} from "./suggest/suggest.component";

import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

import {MatToolbarModule} from '@angular/material/toolbar';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatInputModule} from '@angular/material/input';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatIconModule} from '@angular/material/icon';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatListModule} from '@angular/material/list';
import {MatChipsModule} from '@angular/material/chips';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {FeedApiService} from "./feed/feed-api.service";
import {FeedComponent} from "./feed/feed.component";

@NgModule({
  declarations: [
    AppComponent,
    SuggestComponent,
    FeedComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    BrowserAnimationsModule,

    MatToolbarModule,
    MatProgressSpinnerModule,
    MatPaginatorModule,
    MatInputModule,
    MatTooltipModule,
    MatIconModule,
    MatProgressBarModule,
    MatListModule,
    MatChipsModule,
    MatCardModule,
    MatButtonModule
  ],

  providers: [
    SuggestApiService,
    FeedApiService
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
