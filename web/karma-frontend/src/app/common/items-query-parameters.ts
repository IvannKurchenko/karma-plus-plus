import {Params} from "@angular/router";
import {FeedRequest} from "../feed/feed-api.model";
import {SuggestItemApiModel} from "../suggest/suggest-api.model";

export class QueryParametersModel {
  static parseFeedRequest(params: Params): FeedRequest {
    let page: number = params['page'];
    let feed: string[] = params['feed'];
    let items =  feed.map(function (item) {
      let feedParts: string[] = item.split(';');
      return {
        name: feedParts[0],
        source: feedParts[1],
        subSource: feedParts[2],
      };
    });

    return {
      items: items,
      page: page
    };
  }

  static format(suggestions: SuggestItemApiModel[]): Params  {
    let items: string[] = suggestions.map(function (item) {
      return item.name + ';' + item.source + ';' + item.subSource;
    })

    return {
      feed: items,
    };
  }
}
