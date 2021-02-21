import {Component} from '@angular/core';
import {SuggestApiService} from "./suggest-api.service";
import {SuggestionsApiModel, SuggestItemApiModel} from "./suggest-api.model";
import {BooleanInput} from "@angular/cdk/coercion";
import {COMMA, ENTER} from "@angular/cdk/keycodes";

@Component({
  selector: 'suggest',
  templateUrl: './suggest.component.html',
  styleUrls: ['./suggest.component.less']
})
export class SuggestComponent {
  value: String = '';
  inProgress: Boolean = false;
  suggestions: SuggestItemApiModel[] = [];
  selectedSuggestions: SuggestItemApiModel[] = [];

  selectable: BooleanInput = true;
  removable: BooleanInput = true;

  separatorKeysCodes: number[] = [ENTER, COMMA];

  constructor(private suggestApiService: SuggestApiService) {
  }

  onSuggestInputChange(): void {
    if (this.value != '') {
      this.inProgress = true;
      this.suggestApiService.getSuggestions(this.value).subscribe(
        suggestions => this.onSuggestionsRetrieved(suggestions)
      );
    }
  }

  private onSuggestionsRetrieved(suggestions: SuggestionsApiModel): void {
    this.inProgress = false;
    this.suggestions = suggestions.items;
  }

  add(suggestion: SuggestItemApiModel): void {
      this.selectedSuggestions.push(suggestion);
  }

  remove(suggestion: SuggestItemApiModel): void {
    const index = this.selectedSuggestions.indexOf(suggestion);

    if (index >= 0) {
      this.selectedSuggestions.splice(index, 1);
    }
  }
}
