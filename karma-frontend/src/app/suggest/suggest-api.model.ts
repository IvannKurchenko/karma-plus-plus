export interface SuggestItemApiModel {
  name: String,
  description: String,
  source: String,
  site: String,
  subSource: String
}

export interface SuggestionsApiModel {
  items: SuggestItemApiModel[]
}
