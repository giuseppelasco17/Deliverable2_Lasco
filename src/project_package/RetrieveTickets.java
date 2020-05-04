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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class RetrieveTickets {




   private static String readAll(Reader rd) throws IOException {
	      StringBuilder sb = new StringBuilder();
	      int cp;
	      while ((cp = rd.read()) != -1) {
	         sb.append((char) cp);
	      }
	      return sb.toString();
	   }


   private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
      InputStream is = new URL(url).openStream();
      try (
         BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
         ){
    	 String jsonText = readAll(rd);
         return new JSONObject(jsonText);
       } finally {
         is.close();
       }
   }

   
   private void filterTickets(int i, JSONArray issues, String projName, List<Ticket> ticketList, int midVersion) throws InstantiationException{
	   Ticket tkt = new Ticket();
       String key = issues.getJSONObject(i%1000).get("key").toString() + ":";
       tkt.setKey(key);
       //add FV
       boolean discardFlag = false;
       JSONObject field = (JSONObject) issues.getJSONObject(i%1000).get("fields");
       String resDate = field.get("resolutiondate").toString().substring(0, 10);
       JSONArray fixVersions = field.getJSONArray("fixVersions");
       int fV = 0;
       int oV = 0;
       int iV = 0;
       if (!fixVersions.isEmpty()) {//check fixVersion field
           String fixVersion = fixVersions.getJSONObject(0).get("name").toString();
           fV = Utilities.getIdVersionFromVersion(projName, fixVersion);
           if (fV == -1) {
           		discardFlag = true;
           }
           tkt.setFixedVersion(fV);
       }else {
       		String fixDate = GitQuery.GetInstance().logFilter(key);
       		if(!(fixDate == null)) {//check if there is a commit
       			fV = Utilities.getIdVersionFromDate(projName, fixDate);
       			if (fV == -1) {
       				discardFlag = true;
       			}
       			tkt.setFixedVersion(fV);
       		}else {
       			if(!resDate.isBlank()) {//check if resDate exist
       				fV = Utilities.getIdVersionFromDate(projName, resDate);
       				if (fV == -1) {
       					discardFlag = true;
       				}
       				tkt.setFixedVersion(fV);
       			}else {//else discard the ticket
       				discardFlag = true;
       			}
       		}
       }
       //add OV
       String openDate = field.get("created").toString().substring(0, 10);
       if (!openDate.isBlank()) {
       		oV = Utilities.getIdVersionFromDate(projName, openDate);
       		if (oV == -1) {
       			discardFlag = true;
       		}
       		tkt.setOpenVersion(oV);
       }else {
       		discardFlag = true;//TODO: check this
       }//add IV
       JSONArray injVersions = field.getJSONArray("versions");
       if (!injVersions.isEmpty()) {//check fixVersion field
           String injVersion = injVersions.getJSONObject(0).get("name").toString();
           //System.out.println(key);
           iV = Utilities.getIdVersionFromVersion(projName, injVersion);
           if (iV == -1) {
        	   discardFlag = true;
           }
           tkt.setInjVersion(iV);
           if(!(iV >= fV || oV >= fV || discardFlag == true || oV < iV || iV > midVersion)) {//not valid values 
        	   Proportion.getIstance().calcProportion(fV, oV, iV);//if IV exist, calculate proportion
           }else if (discardFlag == false && iV > oV && iV < fV){		            	
           }else {
        	   discardFlag = true;
           } 
       }else {//apply predIv only if ticket is not discarded
       		if (discardFlag == false && oV <= fV) {//calculate predIV only for correct ticket
       			iV = Proportion.getIstance().getPredIv(oV,fV);
       			tkt.setInjVersion(iV);
       		}else {
       			discardFlag = true;
       		}	            	
       }
       if (iV > midVersion) {
    	   discardFlag = true;// refer only to the first half of the project
       }
       
       if (discardFlag == false) {
       		//System.out.println("key = "+key+" FV = "+fV+", OV = "+oV+", IV = "+iV+" discFlag = "+discardFlag); 
    	   tkt.setLastAv(tkt.getFixedVersion() - 1);
    	   ticketList.add(tkt);
       }
   }

  
	public List<Ticket> retrieveTicketsIDs(String projName, int midVersion) throws Exception {
		//ordered by creation date OV
		Integer j = 0;
		Integer i = 0;
		Integer total = 1;
		ArrayList<Ticket> ticketList = new ArrayList<>();
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
	        	 filterTickets(i, issues, projName, ticketList, midVersion);	      
	         }  
	      } while (i < total);		
	    return ticketList;
	}
}
