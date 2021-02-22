export interface FeedItemRequest {
  source: string,
  subSource: string,
  name: string
}

export interface FeedRequest {
  items: FeedItemRequest[],
  page?: number
}

export interface FeedItem {
  name: string,
  description?: string,
  source: string,
  link: string,
  parentLink?: string,
  created: number
}


export interface Feed {
  items: FeedItem[]
}
