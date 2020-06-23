package milestone1;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetJIRAInfo {

	private static Map<LocalDateTime, String> releaseNames;
	private static Map<LocalDateTime, String> releaseID;
	private static List<LocalDateTime> releases;
	private static Integer numVersions;

	public static List<Release> getListRelease() throws IOException, JSONException {

		String projName = "BOOKKEEPER";

		ArrayList<Release> releaseList = new ArrayList<>();

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
		Collections.sort(releases, (o1, o2) -> o1.compareTo(o2));

		if (releases.size() < 6)
			return releaseList;
		String pathname = "D:\\" + "Universita\\magistrale\\isw2\\Codici\\deliverable2ISW2\\deliverable_2.csv";
		try (FileWriter fileWriter = new FileWriter(pathname)) {

			fileWriter.append("Index;Version ID;Version Name;Date");
			fileWriter.append("\n");
			numVersions = releases.size();
			for (i = 0; i < releases.size(); i++) {
				Integer index = i + 1;
				fileWriter.append(index.toString());
				fileWriter.append(";");
				fileWriter.append(releaseID.get(releases.get(i)));
				fileWriter.append(";");
				fileWriter.append(releaseNames.get(releases.get(i)));
				fileWriter.append(";");
				fileWriter.append(releases.get(i).toString());
				fileWriter.append("\n");
			}

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			Log.errorLog(sw.toString());
		}

		// ordina la mappa con date crescenti
		Map<LocalDateTime, String> map = new TreeMap<>(releaseNames);

		Integer index = 1;

		for (Map.Entry<LocalDateTime, String> entry : map.entrySet()) {
			// la chiave e la data della release
			LocalDateTime key = LocalDateTime.parse(entry.getKey().toString());

			// il valore e il numero della release
			String value = entry.getValue();
			Release release = new Release(index, key, value);
			releaseList.add(release);
			index++;
		}
		return releaseList;
	}

	public static void addRelease(String strDate, String name, String id) {
		LocalDate date = LocalDate.parse(strDate);
		LocalDateTime dateTime = date.atStartOfDay();

		if (!releases.contains(dateTime))
			releases.add(dateTime);
		releaseNames.put(dateTime, name);

		releaseID.put(dateTime, id);
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try (

				// BufferedReader rd = new BufferedReader(new InputStreamReader(is,
				// Charset.forName("UTF-8")))) {
				BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8.name()))) {

			String jsonText = readAll(rd);
			return (new JSONObject(jsonText));
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

	public static void printMap(Map<LocalDateTime, String> map) {

		for (Map.Entry<LocalDateTime, String> entry : map.entrySet()) {
			String key = entry.getKey().toString();
			String value = entry.getValue();
			Log.infoLog(key + " " + value);
		}
	}

	// assegno alle date di rilascio e creazione dei ticket le release (OV e FV)
	public static List<Ticket> compareDateVersion(List<Release> releases2, List<LocalDateTime> dateList,
			List<Ticket> listaTicket) {

		// releases e una lista di release che hanno data e versione (dal codice
		// Falessi)
		// dateList contiene tutte le date relative ai ticket in ordine cronologico

		// scorro tutte le date dei ticket e gli associo una versione
		for (int i = 0; i < dateList.size(); i++) {

			LocalDateTime dataTicket = dateList.get(i);

			for (int j = 0; j < releases2.size(); j++) {

				// o e inferiore
				if (dataTicket.isBefore(releases2.get(j).getDate())) {

					assignVersion(dataTicket, listaTicket, releases2.get(j).getIndex());

					break;

				}

			}
		}

		return listaTicket;
	}

	public static void printArrayList(List<Ticket> listaTicket) {
		for (int i = 0; i < listaTicket.size(); i++) {

			Log.infoLog(listaTicket.get(i).getIndex() + ")\t\tticket = " + listaTicket.get(i).getID() + "\t\tcreated: "
					+ listaTicket.get(i).getCreationDate() + "\t\tresolution: " + listaTicket.get(i).getResolutionDate()
					+ "\t\tIV = " + +listaTicket.get(i).getIV() + "\t\t OV: " + listaTicket.get(i).getOV() + "\t\tFV: "
					+ listaTicket.get(i).getFV() + "\t\tAV = " + listaTicket.get(i).getAV());
		}
	}

	// scorro la lista dei ticket, e ai ticket con release date o creation date
	// assegno le versioni trovate
	public static void assignVersion(LocalDateTime data, List<Ticket> listaTicket, Integer index) {

		for (int i = 0; i < listaTicket.size(); i++) {

			if (listaTicket.get(i).getResolutionDate().isEqual(data)) {
				listaTicket.get(i).setFV(index);
			}

			if (listaTicket.get(i).getCreationDate().isEqual(data)) {
				listaTicket.get(i).setOV(index);

			}
		}

	}

	public static List<Ticket> retrieveTickets(String projName, List<Release> releases)
			throws IOException, JSONException {

		/// RITORNA UNA LISTA DI TICKET
		ArrayList<Ticket> listaTicket = new ArrayList<>();

		Integer j = 0;
		Integer i = 0;
		Integer total = 1;

		// Get JSON API for closed bugs w/ AV in the project
		do {
			// Only gets a max of 1000 at a time, so must do this multiple times if bugs
			// >1000
			j = i + 1000; // da 1 a 1000 perche ogni query non puo avere piu di 1000 risultati
			String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22" + projName
					+ "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
					+ "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,affectedVersion,versions,created&startAt="
					+ i.toString() + "&maxResults=" + j.toString();

			JSONObject json = readJsonFromUrl(url);
			JSONArray issues = json.getJSONArray("issues");
			total = json.getInt("total");
			for (; i < total && i < j; i++) { // for sulla issue
				// Iterate through each bug

				String key = issues.getJSONObject(i % 1000).get("key").toString(); // PRINT KEY DELLA ISSUE
				// String versions = issues.getJSONObject(i%1000).get("versions").toString();
				// //PRINT KEY DELLA ISSUE

				JSONObject boh = issues.getJSONObject(i % 1000);

				JSONObject fields = boh.getJSONObject("fields");
				JSONArray versions = fields.getJSONArray("versions");
				// CharSequence resolutiondate1 =
				// fields.getString("resolutiondate").subSequence(0,19); //FV

				LocalDateTime resolutiondate = LocalDateTime
						.parse(fields.getString("resolutiondate").subSequence(0, 19));
				LocalDateTime creationdate = LocalDateTime.parse(fields.getString("created").subSequence(0, 19));

				List<Integer> listaAV = getAVList(versions, releases);

				Ticket ticket = new Ticket(key, listaAV, resolutiondate, creationdate);

				if (listaAV.get(0) != null) {
					ticket.setIV(listaAV.get(0));
				} else {
					ticket.setIV(0);
				}

				listaTicket.add(ticket);

			}

			Log.infoLog("numero tickets ID = " + listaTicket.size());

		} while (i < total);

		// printArrayList(listaTicket);

		return listaTicket;
	}

	public static List<LocalDateTime> addAndSortDateTickets(List<Ticket> listaTicket) {

		// ho lasciayo localdatetime, cosi ho anche l'orario di FV e OV, anche se avrei
		// potuto mettere localdate ma
		// avrei dovuto cambiare il codice di falessi

		// cosi facendo non ci sono duplicati (e difficile trovare orario uguale di FV e
		// Ov dello stesso giorno)

		List<LocalDateTime> dateList = new ArrayList<>();

		for (int i = 0; i < listaTicket.size(); i++) {
			dateList.add(listaTicket.get(i).getResolutionDate());
			dateList.add(listaTicket.get(i).getCreationDate());

		}

		Collections.sort(dateList);

		return dateList;

	}

	public static List<Integer> getAVList(JSONArray versions, List<Release> releases) throws JSONException {

		ArrayList<Integer> listaAV = new ArrayList<>();

		if (versions.length() == 0) {
			listaAV.add(null);

		} else {
			for (int k = 0; k < versions.length(); k++) {
				String affectedVersion = versions.getJSONObject(k).getString("name");
				for (int g = 0; g < releases.size(); g++) {
					if (affectedVersion.equals(releases.get(g).getRelease())) {
						listaAV.add(releases.get(g).getIndex());
					}
				}

			}
		}

		return listaAV;

	}

	public static void main(String[] args) throws IOException, JSONException {
		// Do nothing because is a main method

	}

}