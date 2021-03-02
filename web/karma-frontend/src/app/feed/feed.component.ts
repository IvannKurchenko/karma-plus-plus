import {Component, OnInit} from "@angular/core";
import {ActivatedRoute, Router} from "@angular/router";
import {FeedApiService} from "./feed-api.service";
import {QueryParametersModel} from "../common/items-query-parameters";
import {Feed, FeedRequestPage} from "./feed-api.model";
import {RenderedFeedItemModel} from "./rendered-feed-item.model";
import {PageEvent} from "@angular/material/paginator";

@Component({
  selector: 'feed',
  templateUrl: './feed.component.html',
  styleUrls: ['./feed.component.less']
})
export class FeedComponent implements OnInit {

  inProgress: Boolean = false;
  feed: RenderedFeedItemModel[] = [];

  private pageToken: string = "";
  private currentFeed: string[] = [];

  constructor(private feedApiService: FeedApiService,
              private route: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.currentFeed = params['feed'];
      let request = QueryParametersModel.parseFeedRequest(params);

      this.inProgress = true;
      this.feed = [];

      this.feedApiService.getFeed(request).subscribe(
        feed => this.onFeedRetrieved(feed)
      );
    });
  }

  private onFeedRetrieved(feed: Feed) {
    this.inProgress = false;
    this.pageToken = feed.pageToken;
    this.feed = feed.items.map(item => new RenderedFeedItemModel(item));
  }

  openItem(item: RenderedFeedItemModel) {
    window.open(item.link, '_blank');
  }

  paginateRight(): void {
    let params = {
      feed: this.currentFeed,
      page: this.pageToken,
      forward: true
    };
    this.router.navigate(['/feed'], {queryParams: params});
  }

  paginateLeft(): void {
    let params = {
      feed: this.currentFeed,
      page: this.pageToken,
      forward: false
    };
    this.router.navigate(['/feed'], {queryParams: params});
  }

  /*onPageEvent(pageEvent: PageEvent): void {
    let params = {
      feed: this.currentFeed,
      page: pageEvent.pageIndex,
      pageSize: pageEvent.pageSize
    };
    this.router.navigate(['/feed'], {queryParams: params});
  }*/
}
