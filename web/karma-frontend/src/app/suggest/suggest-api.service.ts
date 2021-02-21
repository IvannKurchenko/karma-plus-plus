import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {SuggestionsApiModel} from "./suggest-api.model";

@Injectable()
export class SuggestApiService {
  constructor(private http: HttpClient) {
  }

  getSuggestions(term: String) {
    return this.http.get<SuggestionsApiModel>('api/suggest/' + term);
  }
}
