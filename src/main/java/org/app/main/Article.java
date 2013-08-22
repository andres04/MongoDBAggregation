package org.app.main;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="articles")
public class Article extends BaseMongoObject{
	private String title;
	private String author;
	private String[] tags;
	private Integer pageViews;
	private List<Comment> comments;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String[] getTags() {
		return tags;
	}
	public void setTags(String[] tags) {
		this.tags = tags;
	}
	public Integer getPageViews() {
		return pageViews;
	}
	public void setPageViews(Integer pageViews) {
		this.pageViews = pageViews;
	}
	public List<Comment> getComments() {
		return comments;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	
	
	
}
