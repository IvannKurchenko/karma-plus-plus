import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {SuggestionsApiModel} from "./suggest-api.model";
import {FeedRequest} from "../feed/feed-api.model";

@Injectable()
export class SuggestApiService {
  constructor(private http: HttpClient) {
  }

  getSuggestions(term: String) {
    return this.http.get<SuggestionsApiModel>('api/suggest/' + term);
  }

  searchSuggestions(request: FeedRequest) {
    return this.http.post<SuggestionsApiModel>('api/suggest', request);
  }
}
