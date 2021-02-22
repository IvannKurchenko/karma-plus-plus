import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Feed, FeedRequest} from "./feed-api.model";

@Injectable()
export class FeedApiService {
  constructor(private http: HttpClient) {
  }

  getFeed(request: FeedRequest) {
    return this.http.post<Feed>('api/feed', request);
  }
}
