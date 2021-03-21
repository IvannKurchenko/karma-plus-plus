import {Params} from "@angular/router";
import {FeedItemRequest, FeedRequest} from "../feed/feed-api.model";
import {SuggestItemApiModel} from "../suggest/suggest-api.model";

export class QueryParametersModel {

  static token = 'token';

  static hasToken(params: Params): boolean {
    return params[QueryParametersModel.token] != null && params[QueryParametersModel.token] != undefined;
  }

  static parseFeedItem(item: String): FeedItemRequest {
    let feedParts: string[] = item.split(';');
    return {
      name: feedParts[0],
      source: feedParts[1],
      subSource: feedParts[2],
    };
  }

  static parseFeedRequest(params: Params): FeedRequest {
    let token: string = params['token'];
    let forward: boolean = params['forward'] == "true";
    let feedParameter = params['feed'];
    let feed = (typeof(feedParameter) === 'string') ? [feedParameter] : feedParameter;
    let items = feed.map(QueryParametersModel.parseFeedItem);

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
