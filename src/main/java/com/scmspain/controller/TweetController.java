package com.scmspain.controller;

import com.scmspain.controller.command.PublishTweetCommand;
import com.scmspain.entities.Tweet;
import com.scmspain.entities.TweetDiscarded;
import com.scmspain.services.TweetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
public class TweetController {
    private TweetService tweetService;

    public TweetController(TweetService tweetService) {
        this.tweetService = tweetService;
    }

    @GetMapping("/tweet")
    public List<Tweet> listAllTweets() {
        return this.tweetService.listAllTweets();
    }

//    @PostMapping("/tweet")
    @PostMapping(value="/tweet",consumes = "application/json", produces = "application/json")
    @ResponseStatus(CREATED)
    public void publishTweet(@RequestBody PublishTweetCommand publishTweetCommand) {
        this.tweetService.publishTweet(publishTweetCommand.getPublisher(), publishTweetCommand.getTweet());
    }
    
    @PostMapping(value="/discarded",consumes = "application/json", produces = "application/json")
    @ResponseStatus(CREATED)
    public void discardTweet(@RequestBody PublishTweetCommand publishTweetCommand) {
        this.tweetService.discardTweet(publishTweetCommand.getTweet());
    }
    
    @GetMapping("/discarded")
    public List<TweetDiscarded> listAllTweetsDiscarded() {
        return this.tweetService.listAllTweetsDiscarded();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public Object invalidArgumentException(IllegalArgumentException ex) {
        return new Object() {
            public String message = ex.getMessage();
            public String exceptionClass = ex.getClass().getSimpleName();
        };
    }
}
