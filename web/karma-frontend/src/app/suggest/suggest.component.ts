import {Component} from '@angular/core';
import {FeedRequestQueryParameters} from "../common/items-query-parameters";
import {SuggestApiService} from "./suggest-api.service";
import {SuggestionsApiModel, SuggestItemApiModel} from "./suggest-api.model";
import {BooleanInput} from "@angular/cdk/coercion";
import {COMMA, ENTER} from "@angular/cdk/keycodes";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'suggest',
  templateUrl: './suggest.component.html',
  styleUrls: ['./suggest.component.less']
})
export class SuggestComponent {
  suggestion: String = '';
  inProgress: Boolean = false;

  allSuggestions: SuggestItemApiModel[] = [];
  selectedSuggestions: SuggestItemApiModel[] = [];

  selectable: BooleanInput = true;
  removable: BooleanInput = true;

  separatorKeysCodes: number[] = [ENTER, COMMA];

  constructor(private suggestApiService: SuggestApiService,
              private route: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit() {
    /*this.route.queryParams.subscribe(params => {
      this.selectedSuggestions = params['name'];
    });*/
  }

  onSuggestInputChange(): void {
    if (this.suggestion != '') {
      this.inProgress = true;
      this.suggestApiService.getSuggestions(this.suggestion).subscribe(
        suggestions => this.onSuggestionsRetrieved(suggestions)
      );
    } else {
      this.allSuggestions = [];
    }
  }

  private onSuggestionsRetrieved(suggestions: SuggestionsApiModel): void {
    this.inProgress = false;
    this.allSuggestions = suggestions.items;
  }

  clear(): void {
    this.suggestion = '';
    this.allSuggestions = [];
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

  navigateToFeed(): void {
    let params = FeedRequestQueryParameters.format(this.selectedSuggestions);
    this.router.navigate(['/feed'], {queryParams: params});
  }
}
