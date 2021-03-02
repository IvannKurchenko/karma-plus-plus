export interface FeedItemRequest {
  source: string,
  subSource: string,
  name: string
}

export interface FeedRequestPage {
  token: string,
  forward: boolean
}

export interface FeedRequest {
  items: FeedItemRequest[],
  pageToken?: FeedRequestPage
}

export interface FeedItem {
  name: string,
  description?: string,
  source: string,
  link: string,
  site: string,
  parentLink?: string,
  created: number
}


export interface Feed {
  items: FeedItem[],
  pageToken: string
}
