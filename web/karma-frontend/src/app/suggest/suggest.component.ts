import { Component } from '@angular/core';
import {SuggestApiService} from "./suggest-api.service";
import {SuggestionsApiModel, SuggestItemApiModel} from "./suggest-api.model";

@Component({
  selector: 'suggest',
  templateUrl: './suggest.component.html',
  styleUrls: ['./suggest.component.less']
})
export class SuggestComponent {
  value: String = '';
  inProgress: Boolean = false;
  suggestions: SuggestItemApiModel[] = [];

  constructor(private suggestApiService: SuggestApiService) {
  }

  onSuggestInputChange(): void {
    if(this.value != '') {
      this.inProgress = true;
      this.suggestApiService.
        getSuggestions(this.value).
        subscribe(
          suggestions => this.onSuggestionsRetrieved(suggestions)
        );
    }
  }

  private onSuggestionsRetrieved(suggestions: SuggestionsApiModel): void {
    this.inProgress = false;
    this.suggestions = suggestions.items;
  }
}
