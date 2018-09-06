package com.scmspain.services;

import com.scmspain.entities.Tweet;
import com.scmspain.entities.TweetDiscarded;

import org.springframework.boot.actuate.metrics.writer.Delta;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class TweetService {
    private EntityManager entityManager;
    private MetricWriter metricWriter;

    public TweetService(EntityManager entityManager, MetricWriter metricWriter) {

        this.entityManager = entityManager;
        this.metricWriter = metricWriter;
    }

    /**
      Push tweet to repository
      Parameter - publisher - creator of the Tweet
      Parameter - text - Content of the Tweet
      Result - recovered Tweet
    */
    public void publishTweet(String publisher, String text) {
    	
    	String link = "";
    	int indexLinkHttp,indexLinkHttps;
    	indexLinkHttp = text.indexOf("http://");
		indexLinkHttps = text.indexOf("https://");
		indexLinkHttp = indexLinkHttp > 0? indexLinkHttp:indexLinkHttps;
		
		boolean spaceempty = text.endsWith(" ");
		
		if ( indexLinkHttp > 0 && spaceempty ) {
			link = text.substring(indexLinkHttp);
			text = text.substring(0, indexLinkHttp);
		}
    	
        if (publisher != null && publisher.length() > 0 && text != null && text.length() > 0 && text.length() < 140) {
            Tweet tweet = new Tweet();
                      
           	tweet.setTweet(text.concat(link));
            tweet.setPublisher(publisher);

            this.metricWriter.increment(new Delta<Number>("published-tweets", 1));
            this.entityManager.persist(tweet);
        } else {
            throw new IllegalArgumentException("Tweet must not be greater than 140 characters");
        }
    }

    /**
      Recover tweet from repository
      Parameter - id - id of the Tweet to retrieve
      Result - retrieved Tweet
    */
    public Tweet getTweet(Long id) {
      return this.entityManager.find(Tweet.class, id);
    }
    
    public TweetDiscarded getTweetDiscarded(Long id) {
      return this.entityManager.find(TweetDiscarded.class, id);
    }
    
    public void discardTweet(String idTweet) {
  	
    	Tweet tweet= getTweet(Long.valueOf(idTweet).longValue());
    	
    	this.entityManager.remove(tweet);
    	
    	TweetDiscarded tweetDiscarded = new TweetDiscarded();
    	tweetDiscarded.setId(tweet.getId());
    	tweetDiscarded.setPublisher(tweet.getPublisher());
    	tweetDiscarded.setTweet(tweet.getTweet());
    	tweetDiscarded.setDate(new Date());
    	
    	this.metricWriter.increment(new Delta<Number>("discarded-tweets", 1));
        this.entityManager.persist(tweetDiscarded);
    }
    

    /**
      Recover tweet from repository
      Parameter - id - id of the Tweet to retrieve
      Result - retrieved Tweet
    */
    public List<Tweet> listAllTweets() {
        List<Tweet> result = new ArrayList<Tweet>();
        this.metricWriter.increment(new Delta<Number>("times-queried-tweets", 1));
        TypedQuery<Long> query = this.entityManager.createQuery("SELECT id FROM Tweet AS tweetId WHERE pre2015MigrationStatus<>99 ORDER BY id DESC", Long.class);
        List<Long> ids = query.getResultList();
        for (Long id : ids) {
            result.add(getTweet(id));
        }
        return result;
    }

    public List<TweetDiscarded> listAllTweetsDiscarded() {
        List<TweetDiscarded> result = new ArrayList<TweetDiscarded>();
        this.metricWriter.increment(new Delta<Number>("times-queried-tweets", 1));
        TypedQuery<Long> query = this.entityManager.createQuery("SELECT id FROM TweetDiscarded AS tweetId ORDER BY date DES", Long.class);
        List<Long> ids = query.getResultList();
        for (Long id : ids) {
            result.add(getTweetDiscarded(id));
        }
        return result;
    }
}
