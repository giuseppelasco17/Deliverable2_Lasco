package milestone_one;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Collections;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.json.JSONObject;
import org.json.JSONArray;

public class GetReleaseInfo {

	protected static Map<LocalDateTime, String> releaseNames;
	protected static Map<LocalDateTime, String> releaseID;
	protected static List<LocalDateTime> releases;
	protected static Integer numVersions;

	private static final String EXCEPTION_THROWN = "an exception was thrown";
	private static final Logger logger = Logger.getAnonymousLogger();
	
	private GetReleaseInfo() {
	    throw new IllegalStateException("GetReleaseInfo class");
	  }
	
	public static void getRelease(String projName) throws IOException {

		// Fills the arraylist with releases dates and orders them
		// Ignores releases with missing dates
		releases = new ArrayList<>();
		Integer i;
		String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName;
		JSONObject json = readJsonFromUrl(url);
		JSONArray versions = json.getJSONArray("versions");
		releaseNames = new HashMap<>();
		releaseID = new HashMap<>();
		for (i = 0; i < versions.length(); i++) {
			String name = "";
			String id = "";
			if (versions.getJSONObject(i).has("releaseDate")) {
				if (versions.getJSONObject(i).has("name"))
					name = versions.getJSONObject(i).get("name").toString();
				if (versions.getJSONObject(i).has("id"))
					id = versions.getJSONObject(i).get("id").toString();
				addRelease(versions.getJSONObject(i).get("releaseDate").toString(), name, id);
			}
		}
		// order releases by date
		Collections.sort(releases, (LocalDateTime o1, LocalDateTime o2) -> o1.compareTo(o2));
		if (releases.size() < 6)
			return;
		
		String outname = projName + "VersionInfo.csv";
		// Name of CSV for output
		try (FileWriter fileWriter = new FileWriter(outname)) {
			fileWriter.append("Index,Version ID,Version Name,Date");
			fileWriter.append("\n");
			numVersions = releases.size();
			for (i = 0; i < releases.size(); i++) {
				Integer index = i + 1;
				fileWriter.append(index.toString());
				fileWriter.append(",");
				fileWriter.append(releaseID.get(releases.get(i)));
				fileWriter.append(",");
				fileWriter.append(releaseNames.get(releases.get(i)));
				fileWriter.append(",");
				fileWriter.append(releases.get(i).toString().substring(0, 10));
				fileWriter.append("\n");
			}
			fileWriter.flush();
		} catch (Exception e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
		}
	}

	public static void addRelease(String strDate, String name, String id) {
		LocalDate date = LocalDate.parse(strDate);
		LocalDateTime dateTime = date.atStartOfDay();
		if (!releases.contains(dateTime))
			releases.add(dateTime);
		releaseNames.put(dateTime, name);
		releaseID.put(dateTime, id);
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException {
		InputStream is = new URL(url).openStream();
		try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			String jsonText = readAll(rd);
			return new JSONObject(jsonText);
		} finally {
			is.close();
		}
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}
}