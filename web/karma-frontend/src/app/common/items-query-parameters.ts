import {Params} from "@angular/router";
import {FeedRequest} from "../feed/feed-api.model";
import {SuggestItemApiModel} from "../suggest/suggest-api.model";

export class QueryParametersModel {
  static parseFeedRequest(params: Params): FeedRequest {
    let token: string = params['page'];
    let forward: boolean = params['forward'] == "true";
    let feed: string[] = params['feed'];
    let items = feed.map(function (item) {
      let feedParts: string[] = item.split(';');
      return {
        name: feedParts[0],
        source: feedParts[1],
        subSource: feedParts[2],
      };
    });

    if (token != null && token != "") {
      return {
        items: items,
        pageToken: {
          token: token,
          forward: forward
        }
      };
    } else {
      return {
        items: items
      };
    }
  }

  static format(suggestions: SuggestItemApiModel[]): Params {
    let items: string[] = suggestions.map(function (item) {
      return item.name + ';' + item.source + ';' + item.subSource;
    })

    return {
      feed: items,
    };
  }
}
