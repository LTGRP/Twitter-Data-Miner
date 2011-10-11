package twitter.datacollector;

import java.util.List;

import javax.sound.midi.Track;

import org.hibernate.Criteria;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import dbutils.HibernateUtil;

import twitter.dto.StatusDto;
import twitter.dto.UserDto;
import twitter4j.FilterQuery;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @author sapan & pulkit
 * 
 */
public class App {
	static int BATCH_SIZE = 1;
	static int COMMIT_SIZE = 10;
	static Session session;
	static Transaction transaction;

	static StatusListener listener = new StatusListener() {
		int countTweets = 0; // Count to implement batch processing

		public void onStatus(Status status) {
			countTweets++;
			StatusDto statusDto = new StatusDto(status);
			try {
				session.saveOrUpdate(statusDto);
			} catch (NonUniqueObjectException e) {
				session.merge(statusDto);
			}

			// Save 1 round of tweets to the database
			if (countTweets == BATCH_SIZE) {
				countTweets = 0;
				session.flush();
				session.clear();
				transaction.commit();
				session = HibernateUtil.getSessionFactory()
						.getCurrentSession();
				transaction = session.beginTransaction();
			}
		}

		public void onDeletionNotice(
				StatusDeletionNotice statusDeletionNotice) {
		}

		public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		}

		public void onException(Exception ex) {
			ex.printStackTrace();
		}

		public void onScrubGeo(long arg0, long arg1) {
			// TODO Auto-generated method stub
		}
	};
	
	public static void main(String[] args) {
		

		// sample() method internally creates a thread which manipulates
		// TwitterStream and calls these adequate listener methods continuously.
		// twitterStream.sample();
	}

	static void follow(TwitterStream twitterStream, List<UserDto> users) {
		twitterStream.addListener(listener);
		session = HibernateUtil.getSessionFactory().getCurrentSession();
		transaction = session.beginTransaction();

		long[] follow = new long[users.size()];
		int i = 0;
		for (UserDto u : users) {
			follow[i] = (long) u.getId();
			i++;
		}

		twitterStream.filter(new FilterQuery(follow));
	}
	
	static void track(TwitterStream twitterStream, String[] keywords) {
		twitterStream.addListener(listener);
		session = HibernateUtil.getSessionFactory().getCurrentSession();
		transaction = session.beginTransaction();
		twitterStream.filter(new FilterQuery().track(keywords));
	}
	
	static void location(TwitterStream twitterStream, double[][] locationCoordinates) {
		twitterStream.addListener(listener);
		session = HibernateUtil.getSessionFactory().getCurrentSession();
		transaction = session.beginTransaction();
		twitterStream.filter(new FilterQuery().locations(locationCoordinates));
	}
	
}