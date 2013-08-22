package org.main.app;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import junit.framework.TestCase;

import org.app.config.AppConfig;
import org.app.main.Article;
import org.app.main.Comment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.CollectionCallback;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

@ContextConfiguration(classes={AppConfig.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class ExampleServiceTests extends TestCase {
	
	private static final String INPUT_COLLECTION = "articles";
	@Autowired
	MongoTemplate mongoTemplate;
	
	private static final Logger logger = LoggerFactory.getLogger(ExampleServiceTests.class);
	
	private boolean init=false;
	
	@Test
	public void testMongoTemplate() throws Exception {
		assertNotNull(mongoTemplate);
	}
	
	@Before
	public void setUp() {

		cleanDb();
		initSampleDataIfNecessary();
	}

	private void initSampleDataIfNecessary() {
		if(!init){
			mongoTemplate.dropCollection(INPUT_COLLECTION);
				mongoTemplate.execute(Article.class, new CollectionCallback<Void>() {
					public Void doInCollection(DBCollection collection) throws MongoException, DataAccessException {
						
						Scanner scanner = null;
						try {
							scanner = new Scanner(new BufferedInputStream(new ClassPathResource("testdata.json").getInputStream()));
							while (scanner.hasNextLine()) {
								String article = scanner.nextLine();
								collection.save((DBObject) JSON.parse(article));
							}
						} catch (Exception e) {
							if (scanner != null) {
								scanner.close();
							}
							throw new RuntimeException("Could not load mongodb sample dataset!", e);
						}
						scanner.close();
						long count = mongoTemplate.count(new Query(), Article.class);
						assertTrue(count==4);
						return null;
					}
				});
				init = true;
		}
	}

	@After
	public void cleanUp() {
		//cleanDb();
	}

	private void cleanDb() {
		mongoTemplate.dropCollection(INPUT_COLLECTION);
	}
	/**I need to extract all comments  ordered by createdAt desc. I achieve it with the query ->
	 * ->  db.articles.aggregate( {$project:{author:1, comments:1}} , {$unwind:"$comments"} , {$sort:{"comments.createdAt":-1}} , {$group: {_id:null,comments:{$push:"$comments"}}} );
	 * but the produced query from the testMongoAggregate() function is:
	 * produced  -						{ "$project" : { "comments" : "$comments"}} , { "$unwind" : "$comments"} , { "$group" : { "_id" : "$comments" , "comments" : { "$push" : "$comments"}}}
	 * 																																	    /\ - here should be null
	 *
	 * executing my query the output from mongodb is:
	 *The output from mongodb is :
	 	{
        "result" : [
                {
                        "_id" : null,
                        "comments" : [
                                {
                                        "author" : "jenny",
                                        "text" : "this is bad",
                                        "createdAt" : 333
                                },
                                {
                                        "author" : "sam",
                                        "text" : "this is bad",
                                        "createdAt" : 222
                                },
                                {
                                        "author" : "alex",
                                        "text" : "this is cool",
                                        "createdAt" : 111
                                },
                                {
                                        "author" : "jenny",
                                        "text" : "this is bad",
                                        "createdAt" : 32
                                },
                                {
                                        "author" : "jenny",
                                        "text" : "this is bad",
                                        "createdAt" : 15
                                },
                                {
                                        "author" : "sam",
                                        "text" : "this is bad",
                                        "createdAt" : 13
                                },
                                {
                                        "author" : "alex",
                                        "text" : "this is cool",
                                        "createdAt" : 12
                                },
                                {
                                        "author" : "jenny",
                                        "text" : "this is bad",
                                        "createdAt" : 7
                                },
                                {
                                        "author" : "sam",
                                        "text" : "this is bad",
                                        "createdAt" : 4
                                },
                                {
                                        "author" : "sam",
                                        "text" : "this is bad",
                                        "createdAt" : 2
                                },
                                {
                                        "author" : "alex",
                                        "text" : "this is cool",
                                        "createdAt" : 1
                                },
                                {
                                        "author" : "alex",
                                        "text" : "this is cool",
                                        "createdAt" : 1
                                }
                        ]
                }
        ],
        "ok" : 1
		}
	 */
	@Test
	public void testMongoAggregate(){

			Aggregation agg = newAggregation( //
					project("comments"), //this should make the project part : {$project:{author:1, comments:1}} - this is okay 
					unwind("comments"),//second part :  {$unwind:"$comments"} - this is ok
					group("comments").push("comments").as("comments") //problem part.. i need - {$group: {_id:null,comments:{$push:"$comments"}} , but it produces : {$group: {_id:"$comments",comments:{$push:"$comments"}}
					//sort(Direction.ASC,"comments.createdAt")
			);
			logger.info("aggregation: " + agg);
			AggregationResults<Comment> result = mongoTemplate.aggregate(agg, "articles", Comment.class);
			List<Comment> orderedComments = result.getMappedResults();
			printComments(orderedComments);
			assertTrue(orderedComments.size()==12);
			
			assertTrue(orderedComments.get(0).getCreatedAt().equals(333));
			assertTrue(orderedComments.get(orderedComments.size()-1).getCreatedAt().equals(1));
			
	}
	
	private void printComments(List<Comment> comments){
		logger.info("----------------------------------------");
		for (Comment comment : comments) {
			logger.info(comment.toString());
		}
		logger.info("----------------------------------------");
	}
}
