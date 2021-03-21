import {FeedItem} from "./feed-api.model";
import * as moment from "moment";

export class RenderedFeedItemModel {
  private static maxLength = 500;

  public name: string;
  public description: string;
  public source: string;
  public link: string;
  public site: string;
  public parentLink?: string;
  public created: string;
  public seen: boolean;

  public showShortDescription: boolean;
  public longDescription: boolean;

  constructor(item: FeedItem, lastOpenTimestamp: number) {
    this.name = item.name;
    this.description = item.description ? item.description : "";
    this.source = item.source;
    this.link = item.link;
    this.site = item.site;
    this.parentLink = item.parentLink;
    this.created = moment.unix(item.created).format("DD-MM-YYYY HH:mm:ss");
    this.seen = (item.created * 1000) > lastOpenTimestamp;

    this.showShortDescription = true;
    this.longDescription = this.description.length > RenderedFeedItemModel.maxLength;
  }

  showMore(): boolean {
    return this.longDescription && this.showShortDescription;
  }

  showLess(): boolean {
    return this.longDescription && !this.showShortDescription;
  }

  triggerShortDescription(): void {
    this.showShortDescription = !this.showShortDescription;
  }

  getDescription(): string {
    if(!this.longDescription) {
      return this.description;
    } else {
      return this.showShortDescription ? this.description.substring(0, RenderedFeedItemModel.maxLength) + "..." : this.description;
    }
  }
}
