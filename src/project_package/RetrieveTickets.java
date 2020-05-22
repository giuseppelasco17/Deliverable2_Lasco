package project_package;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.json.JSONArray;

public class RetrieveTickets {
	
	private String projName;

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	private static JSONObject readJsonFromUrl(String url) throws IOException {
		InputStream is = new URL(url).openStream();
		try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));) {
			String jsonText = readAll(rd);
			return new JSONObject(jsonText);
		} finally {
			is.close();
		}
	}

	private void filterTickets(int i, JSONArray issues, List<Ticket> ticketList, int midVersion)
			throws InstantiationException {
		Ticket tkt = new Ticket();
		String key = issues.getJSONObject(i % 1000).get("key").toString() + ":";
		tkt.setKey(key);
		// add FV
		boolean discardFlag = false;
		JSONObject field = (JSONObject) issues.getJSONObject(i % 1000).get("fields");
		int fV = 0;
		int oV = 0;
		int iV = 0;
		int[] buggyWindowVersions = {fV, oV, iV};
		discardFlag = addFixedVersionToTicket(field, discardFlag, key, tkt, buggyWindowVersions);
		// add OV
		oV = buggyWindowVersions[1];
		String openDate = field.get("created").toString().substring(0, 10);
		if (!openDate.isBlank()) {
			oV = Utilities.getIdVersionFromDate(projName, openDate);
			if (oV == -1) {
				discardFlag = true;
			}
			tkt.setOpenVersion(oV);
		} else {
			discardFlag = true;
		} // add IV
		buggyWindowVersions[1] = oV;
		discardFlag = addInjVersionToTicket(field, buggyWindowVersions, midVersion, discardFlag, tkt);
		if (!discardFlag) {
			tkt.setLastAv(tkt.getFixedVersion() - 1);
			ticketList.add(tkt);
		}
	}
	
	private boolean addInjVersionToTicket(JSONObject field, int[] buggyWindowVersions, int midVersion, boolean discardFlag, Ticket tkt) {
		JSONArray injVersions = field.getJSONArray("versions");
		int fV = buggyWindowVersions[0];
		int oV = buggyWindowVersions[1];
		int iV = buggyWindowVersions[2];
		if (!injVersions.isEmpty()) {// check fixVersion field
			String injVersion = injVersions.getJSONObject(0).get("name").toString();
			iV = Utilities.getIdVersionFromVersion(projName, injVersion);
			if (iV == -1) {
				discardFlag = true;
			}
			tkt.setInjVersion(iV);
			if (!(iV >= fV || oV >= fV || discardFlag || oV < iV || iV > midVersion)) {// not valid values
				Proportion.getIstance().calcProportion(fV, oV, iV);// if IV exist, calculate proportion
			} else if (!discardFlag && iV > oV && iV < fV) {
				//in this case is like continue
			} else {
				discardFlag = true;
			}
		} else {// apply predIv only if ticket is not discarded
			if (!discardFlag && oV <= fV) {// calculate predIV only for correct ticket
				iV = Proportion.getIstance().getPredIv(oV, fV);
				tkt.setInjVersion(iV);
			} else {
				discardFlag = true;
			}
		}
		if (iV > midVersion) {
			discardFlag = true;// refer only to the first half of the project
		}
		buggyWindowVersions[0] = fV;
		buggyWindowVersions[1] = oV;
		buggyWindowVersions[2] = iV;
		return discardFlag;
	}

	private boolean addFixedVersionToTicket(JSONObject field, boolean discardFlag, String key, Ticket tkt, int[] buggyWindowVersions) throws InstantiationException {
		String resDate = field.get("resolutiondate").toString().substring(0, 10);
		JSONArray fixVersions = field.getJSONArray("fixVersions");
		int fV = buggyWindowVersions[0];
		if (!fixVersions.isEmpty()) {// check fixVersion field
			String fixVersion = fixVersions.getJSONObject(0).get("name").toString();
			fV = Utilities.getIdVersionFromVersion(projName, fixVersion);
			if (fV == -1) {
				discardFlag = true;
			}
			tkt.setFixedVersion(fV);
		} else {
			discardFlag = othersCheckForFixedVersion(key, buggyWindowVersions, discardFlag, resDate, tkt);
			buggyWindowVersions[0] = fV;
		}
		buggyWindowVersions[0] = fV;
		return discardFlag;
	}
  
	private boolean othersCheckForFixedVersion(String key, int[] buggyWindowVersions, boolean discardFlag, String resDate, Ticket tkt) throws InstantiationException {
		int fV = buggyWindowVersions[0];
		String fixDate = GitQuery.getInstance().logFilter(key);
		if (fixDate != null) {// check if there is a commit
			fV = Utilities.getIdVersionFromDate(projName, fixDate);
			if (fV == -1) {
				discardFlag = true;
			}
			tkt.setFixedVersion(fV);
		} else {
			if (!resDate.isBlank()) {// check if resDate exist
				fV = Utilities.getIdVersionFromDate(projName, resDate);
				if (fV == -1) {
					discardFlag = true;
				}
				tkt.setFixedVersion(fV);
			} else {// else discard the ticket
				discardFlag = true;
			}
		}
		buggyWindowVersions[0] = fV;
		return discardFlag;
	}
	
	public List<Ticket> retrieveTicketsIDs(String projName, int midVersion) throws IOException, InstantiationException  {
		//ordered by creation date OV
		Integer j = 0;
		Integer i = 0;
		Integer total = 1;
		ArrayList<Ticket> ticketList = new ArrayList<>();
		this.projName = projName;
	      //Get JSON API for closed bugs w/ AV in the project
		do {
	         //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
	         j = i + 1000;
	         String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
	                 + projName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"//%22issueType%22=%22Bug%22AND//ORDER BY created ASC&orderBy=-created
	                 + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22ORDER%20BY%20created%20ASC&orderBy=-created&fields=key,resolutiondate,versions,created,fixVersions&startAt="
	                 + i.toString() + "&maxResults=" + j.toString();
	         JSONObject json = readJsonFromUrl(url);
	         JSONArray issues = json.getJSONArray("issues");
	         total = json.getInt("total");
	         for (; i < total && i < j; i++) {
	            //Iterate through each bug
	        	 filterTickets(i, issues, ticketList, midVersion);	      
	         }  
	      } while (i < total);		
	    return ticketList;
	}
}
