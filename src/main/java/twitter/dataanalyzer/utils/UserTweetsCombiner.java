/**
 * 
 */
package twitter.dataanalyzer.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;

import com.mysql.jdbc.Extension;

import dbutils.HibernateUtil;

import twitter.dto.StatusDto;
import twitter.dto.UserDto;

/**
 * @author pulkit and sapan
 *
 */
public class UserTweetsCombiner {

	private static final String FILE_EXTENSION = ".txt";
	private String documentDir;
	private File documentDirectory;
	private List<UserDto> users;
	
	public UserTweetsCombiner(String documentDir) {
		setDocumentDir(documentDir);
	}

	public List<File> generateTweetFiles() throws IOException {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		Transaction transaction = session.beginTransaction();
		
		List<File> userTweetFiles = new ArrayList<File>();
		
		for (int i = 0; i < users.size(); ++i) {
			UserDto u = users.get(i);
			Criteria c = session.createCriteria(StatusDto.class);
			c.add(Restrictions.eq("user", u));
			List<StatusDto> statuses = c.list();
			if (statuses.isEmpty()) {
				users.remove(i);
				--i;
				continue;
			}
			File f = new File(documentDir+File.separator+u.getScreenName()+FILE_EXTENSION);
			userTweetFiles.add(f);
			FileWriter fw = new FileWriter(f);
			BufferedWriter out = new BufferedWriter(fw);
			for (StatusDto s : statuses) {
				out.write(s.getText() + "\n");
			}
			out.close();

		}
		session.close();
		return userTweetFiles;
	}
	
	public void setDocumentDir(String documentDir) {
		this.documentDir = documentDir;
		documentDirectory = new File(documentDir);
		
		if (!documentDirectory.exists()) {
			documentDirectory.mkdirs();
		}
	}
	
	public List<UserDto> getUsers() {
		return users;
	}

	public void setUsers(List<UserDto> users) {
		this.users = users;
	}
	
	
}
