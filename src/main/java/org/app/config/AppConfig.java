package org.app.config;

import java.net.UnknownHostException;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoFactoryBean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoURI;

@Configuration
@ComponentScan( basePackages = "org.app" )
@PropertySource({"classpath:persistence.properties"})
public class AppConfig {
	@Autowired
	Environment env;
	
	@Bean
	public FactoryBean<Mongo> factoryBean() {
		MongoFactoryBean mongo = new MongoFactoryBean();
		mongo.setPort(Integer.valueOf(env.getProperty("port.dev")));
		mongo.setHost(env.getProperty("host.dev"));
		return mongo;
	}

	@Bean
	public MongoDbFactory mongoDbFactory() throws MongoException, UnknownHostException {
		MongoDbFactory factory = new SimpleMongoDbFactory(new MongoURI(env.getProperty("mongo_uri.dev")));
		return factory;
	}

	@Bean
	public MongoOperations mongoTemplate() throws MongoException, UnknownHostException {
		MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory());
		return mongoTemplate;
	}
}
