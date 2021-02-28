import { Component } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.less']
})
export class AppComponent {
  title = 'karma++';

  constructor(private route: ActivatedRoute,
              private router: Router) {
  }

  navigateToEditFeed(): void {
    let params = this.route.snapshot.queryParams;
    this.router.navigate(['/suggestion'], {queryParams: params});
  }
}
