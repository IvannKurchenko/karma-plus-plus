export interface SuggestItemApiModel {
  name: String,
  description: String,
  source: String,
  subSource: String
}

export interface SuggestionsApiModel {
  items: SuggestItemApiModel[]
}
