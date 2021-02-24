import {Component, OnInit} from "@angular/core";
import {ActivatedRoute, Router} from "@angular/router";
import {FeedApiService} from "./feed-api.service";
import {FeedRequestQueryParameters} from "../common/items-query-parameters";
import {Feed} from "./feed-api.model";
import {RenderedFeedItemModel} from "./rendered-feed-item.model";
import {PageEvent} from "@angular/material/paginator";

@Component({
  selector: 'feed',
  templateUrl: './feed.component.html',
  styleUrls: ['./feed.component.less']
})
export class FeedComponent implements OnInit {
  private static DefaultPageSize = 10;
  private static DefaultPage = 1;

  inProgress: Boolean = false;
  feed: RenderedFeedItemModel[] = [];

  pageSize = FeedComponent.DefaultPageSize;
  pageIndex = FeedComponent.DefaultPage;

  pageSizeOptions: number[] = [5, 10, 25, 100];

  private currentFeed: string[] = [];

  constructor(private feedApiService: FeedApiService,
              private route: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit(): void {
    this.loadFeed();
  }

  private loadFeed(): void {
    this.route.queryParams.subscribe(params => {
      this.currentFeed = params['feed'];

      let pageParam = params['page'];
      this.pageIndex = pageParam ? pageParam : FeedComponent.DefaultPage;

      let pageSizeParam = params['pageSize'];
      this.pageSize = pageSizeParam ? pageSizeParam : FeedComponent.DefaultPageSize;

      let request = FeedRequestQueryParameters.parse(params);
      this.inProgress = true;
      this.feed = [];
      this.feedApiService.getFeed(request).subscribe(
        feed => this.onFeedRetrieved(feed)
      );
    });
  }

  private onFeedRetrieved(feed: Feed) {
    this.inProgress = false;
    this.feed = feed.items.map(item => new RenderedFeedItemModel(item));
  }

  openItem(item: RenderedFeedItemModel) {
    window.open(item.link, '_blank');
  }

  onPageEvent(pageEvent: PageEvent): void {
    let params = {
      feed: this.currentFeed,
      page: pageEvent.pageIndex,
      pageSize: pageEvent.pageSize
    };
    this.router.navigate(['/feed'], {queryParams: params});
    this.loadFeed();
  }
}
