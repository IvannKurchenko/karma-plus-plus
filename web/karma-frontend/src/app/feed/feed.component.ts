import {Component, OnInit} from "@angular/core";
import {ActivatedRoute} from "@angular/router";
import {FeedApiService} from "./feed-api.service";
import {FeedRequestQueryParameters} from "../common/items-query-parameters";
import {Feed, FeedItem} from "./feed-api.model";
import {RenderedFeedItemModel} from "./rendered-feed-item.model";

@Component({
  selector: 'feed',
  templateUrl: './feed.component.html',
  styleUrls: ['./feed.component.less']
})
export class FeedComponent implements OnInit {

  inProgress: Boolean = false;
  feed: RenderedFeedItemModel[] = [];

  constructor(private feedApiService: FeedApiService,
              private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      let request = FeedRequestQueryParameters.parse(params);

      this.inProgress = true;
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
}
