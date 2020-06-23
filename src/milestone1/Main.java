package milestone1;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;

public class Main {

	public static List<Ticket> listaTicket;
	public static String name_project = "Bookkeeper";
	// public static Map<LocalDateTime, String> releases;
	public static List<Release> releases;
	public static List<LocalDateTime> dateList;

	public static int numTicket;

	public static int numRelease;
	public static int halfRelease;

	public static void main(String[] args) throws IOException, JSONException, IllegalStateException, GitAPIException {

		Log.setupLogger(); // apre due stream su console e testo
		// prendo tutta lista release progetto
		Log.infoLog("La lista delle release e: ");
		releases = GetJIRAInfo.getListRelease();
		numRelease = releases.size();

		// ora mi calcolo il valore di halfRelease
		float half = (float) numRelease / 2;
		String.format("%.3f", half);
		halfRelease = (int) half; // arrotondo in difetto

		// prendo tutti i ticket di tipo bug ecc e i relativi campi che mi interessano
		// DA JIRA e li metto in listaTicket (ci ho tolto i ticket con IV>7)
		// System.out.println("La lista dei ticket che mi interessano e : ");
		listaTicket = GetJIRAInfo.retrieveTickets(name_project, releases);
		System.out.println("\n\n---------------------------------------------\n\n");

		// salvo la dimensione di listaTicket, ossia il numero di ticket, che mi servira
		// per proportion
		numTicket = listaTicket.size();

		// System.out.println("\n\nLa lista dei ticket di tipo bug,
		// closed,resolved,fixed e:");
		// RetrieveTicketsJIRA.printArrayList(listaTicket);

		// System.out.println("\n\nSTAMPO LISTA DATE\n");
		// dateList contiene in ordine cronologico tutte le resolution date e creation
		// date di ogni ticket
		dateList = GetJIRAInfo.addAndSortDateTickets(listaTicket);

		// adesso devo confrontare le date dei ticket e le date del progetto, per
		// definire OV e FV,
		// e mi va a modificare la classe Ticket aggiungendo le release

		System.out.println("getJIRAInfo.compareDateVersion : ");

		listaTicket = GetJIRAInfo.compareDateVersion(releases, dateList, listaTicket);
		// GetJIRAInfo.printArrayList(listaTicket);

		// ritorna lista con ticket che mi servono per proportion e chiama il metodo
		// proportion, che
		// modifica la lista dei ticket con i valori di IV calcolati con l'algoritmo
		System.out.println("checkTicket : ");

		checkTicket2();

		System.out.println("LISTA TICKET  : " + listaTicket.size());

		// RetrieveTicketsJIRA.printArrayList(listaTicket);

		// ordino hashmap con date in ordine crescente

		// System.out.println("\n\n------------------PROPORTION----------------------\n");
		// calcola Proportion
		// proportion(good);

		// ora devo modificare AV di ogni ticket, la setto manualmente perche i dati
		// presi da JIRA riguardo le AV sono quasi tutti sbagliati
		modifyListAV();

		// adesso devo prendere la meta delle release, questo lavoro lo faccio qui cosii
		// lavoro solamente sulle AV che ho calcolato, e dico che
		// se nella lista di AV c'e una release che ha un valore maggiore della meta del
		// # di release, levo tale valore dalla lista

		// inoltre, se ho un IV maggiore della meta del # di release, levo il ticket
		// dalla lista

		// BOOKKEEPER-688 IV: 4 OV: 6 FV: 8 AV: [4, 5, 6, 7] --> mi serve sapere AV,
		// perche anche se FV e 8, AV contiene le corrette release
		// BOOKKEEPER-752 IV: 6 OV: 8 FV: 8 AV: [6, 7]
		// BOOKKEEPER-859 IV: 1 OV: 10 FV: 12 AV: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]
		// non posso cancellare tali ticket, percio controllo AV, tanto FV e OV non mi
		// servono

		// removeRelease();

		System.out.println("\n\nhalfRelease = " + halfRelease);

		System.out.println("\n\n\n\n------------------------ COMMIT ----------------------------\n\n");

		// lavoro con i commit e mi ritorna una lista di javafile con i dati che i
		// servono
		GetGitInfo.buildDataset(listaTicket, releases, halfRelease);

		// getGitInfo.getCommit(listaTicket,releases,halfRelease);

		// RetrieveTicketsJIRA.printArrayList(listaTicket);
	}

	// controllare la consistenza delle date, e eliminare dalla lista dei ticket i
	// ticket che hanno dati inconsistenti
	public static void checkTicket() {

		Integer count = 0;
		Integer count1 = 0;

		ArrayList<Ticket> goodTicket = new ArrayList<>();
		ArrayList<Ticket> noIVTicket = new ArrayList<>();

		// tolgo da listaTicket i ticket che hanno date inconsistenti
		System.out.println("\nI ticket che hanno date inconsistenti e che vengono rimossi dalla listTicket sono : ");
		for (int i = 0; i < listaTicket.size(); i++) {

			// se IV>FV o IV> OV (dati da JIRA) rimuovo i ticket perche sono sbagliati
			if (listaTicket.get(i).getIV() > listaTicket.get(i).getFV()) {
				System.out.println(listaTicket.get(i).getID());
				listaTicket.remove(i);
				i--;
				count++;
			}
		}

		Integer count3 = 0;
		Integer count4 = 0;
		Integer count5 = 0;
		Integer count6 = 0;

		System.out.println("-----------\n\n");

		// inverto ordine listaTicket per semplicita
		Collections.reverse(listaTicket);

		// ora ho la lista perfetta di ticket, non dovro piu toglierli, quindi associo
		// un indice a ogni ticket cosi da rendere piu semplice
		// il confronto tra le liste
		for (int i = 0; i < listaTicket.size(); i++) {
			listaTicket.get(i).setIndex(i);
		}

		System.out.println("La lista ordinata di ticket e:\n ");
		// GetJIRAInfo.printArrayList(listaTicket);
		System.out.println("-----------\n\n\n");

		// prendo i ticket che rispettano le condizioni per implementare proportion
		// i ticket ora sono 422, poiche ne ho tolti 13 prima
		for (int i = 0; i < listaTicket.size(); i++) {

			// System.out.println(listaTicket.get(i).getID());

			LocalDateTime creationDate = listaTicket.get(i).getCreationDate();
			LocalDateTime resolutionDate = listaTicket.get(i).getResolutionDate();

			Integer FV = listaTicket.get(i).getFV();
			Integer OV = listaTicket.get(i).getOV();
			Integer IV = listaTicket.get(i).getIV();
			// System.out.println("IV = " + IV);
			// System.out.println("FV = " + FV);

			// IV!=0
			if (IV != 0) {
				// FV!=IV e FV!=OV senno P=0
				count6++;
				if (FV != IV && FV != OV && IV <= OV) { // ticket buoni per calcolare proportion
					// System.out.println(listaTicket.get(i).getID());
					/*
					 * System.out.println("ticket = " + listaTicket.get(i).getID() + "\t\tcreated: "
					 * + listaTicket.get(i).getCreationDate() + "\t\tresolution: " +
					 * listaTicket.get(i).getResolutionDate() +"\t\t IV: " +
					 * listaTicket.get(i).getIV() + "\t\t OV: "+ listaTicket.get(i).getOV()
					 * +"\t\tFV: " + listaTicket.get(i).getFV() );
					 */
					goodTicket.add(listaTicket.get(i));
					count3++;
				}

			} else { // IV = 0

				if (OV == 1) {
					listaTicket.get(i).setIV(1);
					// listaTicket.get(i).getAV().add(1);
					count4++;
				}

				else { // if(IV == 0) {
					// aggiungi alla lista per cui calcolare IV con proportion
					count5++;
					noIVTicket.add(listaTicket.get(i));
				}
			}

			/*
			 * if(listaTicket.get(i).getIV() != 0 && listaTicket.get(i).getIV() <
			 * listaTicket.get(i).getFV() ) { count1 ++;
			 * 
			 * }
			 */

			if (creationDate.isAfter(resolutionDate)) {
				System.out.println(listaTicket.get(i).getID() + "ERRORE: creationDate is after resolutionDate");
			}

			// SE HO OV = 1 E NON HO IV, SICURAMENTE, POICHe IV<= OV --> IV = 1 PER
			// FORZA!!!!!!!!!!!!!!

		}

		System.out.println("La lista di ticket che servono per il calcolo di proportion e: \n");
		// GetJIRAInfo.printArrayList(goodTicket);
		System.out.println("-----------\n\n\n");

		System.out.println("count = " + count);
		System.out.println("count1 = " + count1);
		System.out.println("count3 = " + count3);
		System.out.println("count5 = " + count5);
		System.out.println("count6 = " + count6);

		System.out.println("goodTicket.size() = " + goodTicket.size());
		Integer conta = count4 + count5 + count6;
		System.out.println("conta = " + conta);
		System.out.println("\n\nnoIVTicket.size  = " + noIVTicket.size());

		System.out.println("\n--------------------------\n stampo ticket senza IV");
		// GetJIRAInfo.printArrayList(noIVTicket);

		// chiamo il metodo proportion
		System.out.println("\n\n------------------PROPORTION----------------------\n");

		proportion(goodTicket, noIVTicket);
		// return goodTicket;

	}

	public static void checkTicket2() {

		Integer ovIs1 = 0;
		Integer mettoIn_noIV = 0;
		Integer smistoTraIv_eGood = 0;
		Integer removed = 0;
		Integer good = 0;
		Integer noIV = 0;
		Integer IVdiv0 = 0;

		int i;

		ArrayList<Ticket> ovIs1_list = new ArrayList<>();
		ArrayList<Ticket> mettoIn_noIV_list = new ArrayList<>();
		ArrayList<Ticket> removed_list = new ArrayList<>();
		ArrayList<Ticket> goodTicket = new ArrayList<>();
		ArrayList<Ticket> noIVTicket = new ArrayList<>();

		System.out.println("I Ticket iniziali sono: ");

		for (i = 0; i < listaTicket.size(); i++) {
			System.out.println("ticket = " + listaTicket.get(i).getID() + " \t\tIV = " + listaTicket.get(i).getIV()
					+ " \t\tOV = " + listaTicket.get(i).getOV() + " \t\tFV = " + listaTicket.get(i).getFV()
					+ " \t\tAV = " + listaTicket.get(i).getAV());
		}

		// analizzo la lista dei ticket presi da jira che hanno AV, IV, OV e FV

		for (i = 0; i < listaTicket.size(); i++) {

			// se OV = 1 e IV = 0 --> sicuramente IV = 1
			if (listaTicket.get(i).getOV() == 1 && listaTicket.get(i).getIV() == 0) {
				ovIs1_list.add(listaTicket.get(i));
				listaTicket.get(i).setIV(1);
				listaTicket.get(i).getAV().remove(null);
				listaTicket.get(i).getAV().add(1);
				ovIs1++;
				// prova.remove(i);
			}

			else {

				// se IV>OV o IV>FV (x es il 638 e 633), magari sono stati inseriti dati su JIRA
				// relativi
				// alla AV sbagliati (non affidabili), quindi setto IV=0 e lo calcolo tramite
				// proportion
				if (listaTicket.get(i).getIV() > listaTicket.get(i).getOV()
						|| listaTicket.get(i).getIV() > listaTicket.get(i).getFV()) {
					mettoIn_noIV_list.add(listaTicket.get(i));
					listaTicket.get(i).setIV(0);
					listaTicket.get(i).getAV().clear();
					mettoIn_noIV++;
					// prova.remove(i);

				}

			}
		}

		for (i = 0; i < listaTicket.size(); i++) {

			// se FV = OV e non ho AV (IV == 0) , rimuovo il ticket perche nel calcolo del
			// predictedIV con proportion, la cui formula e IV = FV-(FV-OV)*P, il termine a
			// dx fa zero
			// e quindi proportion non l'ho utilizzato

			// IV = 0 se
			// il ticket non ha AV presa da JIRA
			// AV presa da JIRA e inconsistente (IV>=OV o IV>FV)
			if (listaTicket.get(i).getIV() == 0 && listaTicket.get(i).getFV() == listaTicket.get(i).getOV()) {
				removed_list.add(listaTicket.get(i));
				listaTicket.remove(i);
				i--;
				removed++;
				// prova.remove(i);

			}
		}

		// GetJIRAInfo.printArrayList(listaTicket);
		// una volta fatti i check,devo togliere dalla lista dei ticket i ticket che
		// hanno IV = 0 e OV o FV > 7
		// perche non posso calcolarne predictedIV e quindi non mi servono
		/*
		 * System.out.println("\n\ndopo aver tolto IV=0, OV o FV>7"); for(int k =
		 * 0;k<listaTicket.size();k++) { if(listaTicket.get(k).getIV() == 0 &&
		 * (listaTicket.get(k).getOV() >halfRelease || listaTicket.get(k).getFV() >
		 * halfRelease)) { System.out.println("ticket = " + listaTicket.get(k).getID() +
		 * "\tIV = " + listaTicket.get(k).getIV() + "\tOV = " +
		 * listaTicket.get(k).getOV() + "\tFV = " + listaTicket.get(k).getFV());
		 * listaTicket.remove(k); k--; } }
		 */

		// System.out.println("listaTicket size = " + listaTicket.size());

		// RetrieveTicketsJIRA.printArrayList(listaTicket);

		System.out.println("\n\n------------------------------\n");
		System.out.println("ovIs1_list--------I ticket che hanno OV = 1 sono: ");
		for (i = 0; i < ovIs1_list.size(); i++) {
			System.out.println("ticket = " + ovIs1_list.get(i).getID() + " \t\tIV = " + ovIs1_list.get(i).getIV()
					+ " \t\tOV = " + ovIs1_list.get(i).getOV() + " \t\tFV = " + ovIs1_list.get(i).getFV() + " \t\tAV = "
					+ ovIs1_list.get(i).getAV());
		}

		/*
		 * System.out.println("\n\n------------------------------\n"); System.out.
		 * println("mettoIn_noIV_list-------I ticket che hanno IV > OV o IV > FV e gli setto IV = 0 sono: "
		 * ) ; for (i=0; i<mettoIn_noIV_list.size();i++) {
		 * System.out.println("ticket = " + mettoIn_noIV_list.get(i).getID() +
		 * " \t\tIV = " + mettoIn_noIV_list.get(i).getIV() + " \t\tOV = " +
		 * mettoIn_noIV_list.get(i).getOV() + " \t\tFV = " +
		 * mettoIn_noIV_list.get(i).getFV() + " \t\tAV = " +
		 * mettoIn_noIV_list.get(i).getAV()); }
		 * 
		 */

		System.out.println("\n\n------------------------------\n");
		System.out.println("removed_list--------I ticket rimuovo perche hanno FV = OV sono: ");
		for (i = 0; i < removed_list.size(); i++) {
			System.out.println("ticket = " + removed_list.get(i).getID() + " \t\tIV = " + removed_list.get(i).getIV()
					+ " \t\tOV = " + removed_list.get(i).getOV() + " \t\tFV = " + removed_list.get(i).getFV()
					+ " \t\tAV = " + removed_list.get(i).getAV());
		}

		// inverto ordine listaTicket per semplicita
		Collections.reverse(listaTicket);

		// ora ho la lista perfetta di ticket, non dovro piu toglierli, quindi associo
		// un indice a ogni ticket cosi da rendere piu semplice
		// il confronto tra le liste
		for (i = 0; i < listaTicket.size(); i++) {
			listaTicket.get(i).setIndex(i);
		}

		System.out.println("\n***********  La lista ordinata di ticket e: ************\n ");
		// GetJIRAInfo.printArrayList(listaTicket);
		System.out.println("-----------\n\n\n");

		// avro gia tolto IV>7

		// prendo i ticket che rispettano le condizioni per implementare proportion
		for (i = 0; i < listaTicket.size(); i++) {

			// System.out.println(listaTicket.get(i).getID());

			LocalDateTime creationDate = listaTicket.get(i).getCreationDate();
			LocalDateTime resolutionDate = listaTicket.get(i).getResolutionDate();

			Integer FV = listaTicket.get(i).getFV();
			Integer OV = listaTicket.get(i).getOV();
			Integer IV = listaTicket.get(i).getIV();
			// System.out.println("IV = " + IV);
			// System.out.println("FV = " + FV);
			smistoTraIv_eGood++;

			// IV!=0
			if (IV != 0) {
				// FV!=IV e FV!=OV senno P=0
				if (FV != IV && FV != OV && IV <= OV) { // ticket buoni per calcolare proportion
					// System.out.println(listaTicket.get(i).getID());
					/*
					 * System.out.println("ticket = " + listaTicket.get(i).getID() + "\t\tcreated: "
					 * + listaTicket.get(i).getCreationDate() + "\t\tresolution: " +
					 * listaTicket.get(i).getResolutionDate() +"\t\t IV: " +
					 * listaTicket.get(i).getIV() + "\t\t OV: "+ listaTicket.get(i).getOV()
					 * +"\t\tFV: " + listaTicket.get(i).getFV() );
					 */
					// if(OV<=halfRelease && FV <=halfRelease) {
					goodTicket.add(listaTicket.get(i));
					good++;
					// }
				} else {
					IVdiv0++;
				}

			} else { // IV = 0
				// if(IV == 0) {
				// aggiungi alla lista per cui calcolare IV con proportion
				noIV++;
				noIVTicket.add(listaTicket.get(i));
			}

			/*
			 * if(listaTicket.get(i).getIV() != 0 && listaTicket.get(i).getIV() <
			 * listaTicket.get(i).getFV() ) { count1 ++;
			 * 
			 * }
			 */

			if (creationDate.isAfter(resolutionDate)) {
				System.out.println(listaTicket.get(i).getID() + "ERRORE: creationDate is after resolutionDate");
			}

			// SE HO OV = 1 E NON HO IV, SICURAMENTE, POICHe IV<= OV --> IV = 1 PER
			// FORZA!!!!!!!!!!!!!!

		}

		// GetJIRAInfo.printArrayList(goodTicket);

		System.out.println("noIV ticket: \n");
		System.out.println("\n\nnoIVTicket.size  = " + noIVTicket.size());
		// GetJIRAInfo.printArrayList(noIVTicket);

		// chiamo il metodo proportion
		System.out.println("\n\n------------------PROPORTION----------------------\n");
		proportion(goodTicket, noIVTicket);

		/*
		 * System.out.
		 * println("\nI ticket che hanno date inconsistenti e che vengono rimossi dalla listTicket sono : "
		 * ); for(int i = 0; i<listaTicket.size();i++){ //se IV>FV o IV> OV (dati da
		 * JIRA) rimuovo i ticket perche sono sbagliati if(listaTicket.get(i).getIV() >
		 * listaTicket.get(i).getFV()) { System.out.println(listaTicket.get(i).getID());
		 * listaTicket.remove(i); i--; count++; } }
		 * 
		 * 
		 * 
		 * Integer count3 = 0; Integer count4 = 0; Integer count5 = 0; Integer count6 =
		 * 0; System.out.println("-----------\n\n");
		 * 
		 * 
		 * //inverto ordine listaTicket per semplicita Collections.reverse(listaTicket);
		 * 
		 * //ora ho la lista perfetta di ticket, non dovro piu toglierli, quindi associo
		 * un indice a ogni ticket cosi da rendere piu semplice //il confronto tra le
		 * liste for(int i = 0; i<listaTicket.size();i++) {
		 * listaTicket.get(i).setIndex(i); }
		 * 
		 * System.out.println("La lista ordinata di ticket e:\n ");
		 * RetrieveTicketsJIRA.printArrayList(listaTicket);
		 * System.out.println("-----------\n\n\n"); //prendo i ticket che rispettano le
		 * condizioni per implementare proportion //i ticket ora sono 422, poiche ne ho
		 * tolti 13 prima for(int i = 0; i<listaTicket.size();i++){
		 * 
		 * //System.out.println(listaTicket.get(i).getID()); LocalDateTime creationDate
		 * = listaTicket.get(i).getCreationDate(); LocalDateTime resolutionDate =
		 * listaTicket.get(i).getResolutionDate();
		 * 
		 * Integer FV = listaTicket.get(i).getFV(); Integer OV =
		 * listaTicket.get(i).getOV(); Integer IV = listaTicket.get(i).getIV(); //
		 * System.out.println("IV = " + IV); // System.out.println("FV = " + FV);
		 * 
		 * 
		 * //IV!=0 if (IV != 0) { //FV!=IV e FV!=OV e IV<= OV , senno P=0 count6++; if(
		 * FV != IV && FV!= OV && IV<=OV) { // ticket buoni per calcolare proportion //
		 * System.out.println(listaTicket.get(i).getID()); /*
		 * System.out.println("ticket = " + listaTicket.get(i).getID() + "\t\tcreated: "
		 * + listaTicket.get(i).getCreationDate() + "\t\tresolution: " +
		 * listaTicket.get(i).getResolutionDate() +"\t\t IV: " +
		 * listaTicket.get(i).getIV() + "\t\t OV: "+ listaTicket.get(i).getOV()
		 * +"\t\tFV: " + listaTicket.get(i).getFV() );
		 */
		/*
		 * goodTicket.add(listaTicket.get(i)); count3++; }
		 * 
		 * } else { //IV = 0
		 * 
		 * if(OV == 1) { listaTicket.get(i).setIV(1);
		 * //listaTicket.get(i).getAV().add(1); count4++; }
		 * 
		 * else { //if(IV == 0) { //aggiungi alla lista per cui calcolare IV con
		 * proportion count5++; noIVTicket.add(listaTicket.get(i)); } }
		 * 
		 * 
		 * 
		 * 
		 * 
		 * /* if(listaTicket.get(i).getIV() != 0 && listaTicket.get(i).getIV() <
		 * listaTicket.get(i).getFV() ) { count1 ++;
		 * 
		 * }
		 */

		/*
		 * 
		 * if(creationDate.isAfter(resolutionDate)) {
		 * System.out.println(listaTicket.get(i).getID() +
		 * "ERRORE: creationDate is after resolutionDate"); }
		 * 
		 * 
		 * 
		 * //SE HO OV = 1 E NON HO IV, SICURAMENTE, POICHe IV<= OV --> IV = 1 PER
		 * FORZA!!!!!!!!!!!!!!
		 * 
		 * 
		 * 
		 * 
		 * 
		 * }
		 * 
		 * System.out.
		 * println("La lista di ticket che servono per il calcolo di proportion e: \n");
		 * RetrieveTicketsJIRA.printArrayList(goodTicket);
		 * System.out.println("-----------\n\n\n");
		 * 
		 * System.out.println("count = " + count); System.out.println("count1 = " +
		 * count1); System.out.println("count3 = " + count3);
		 * System.out.println("count5 = " + count5); System.out.println("count6 = " +
		 * count6); System.out.println("goodTicket.size() = " + goodTicket.size());
		 * Integer conta = count4 + count5 + count6; System.out.println("conta = " +
		 * conta); System.out.println("\n\nnoIVTicket.size  = " + noIVTicket.size());
		 * System.out.println("\n--------------------------\n stampo ticket senza IV");
		 * RetrieveTicketsJIRA.printArrayList(noIVTicket);
		 * 
		 * //chiamo il metodo proportion
		 * System.out.println("\n\n------------------PROPORTION----------------------\n"
		 * ); proportion(goodTicket, noIVTicket); //return goodTicket;
		 * 
		 */

	}

	public static void proportion(ArrayList<Ticket> listGood, ArrayList<Ticket> listNoIV) {

		// uso tree map perche ha i valori di keys in ordine
		TreeMap<Integer, Integer> proportionValue = new TreeMap<>(); // contiene indice del ticket e il valore di P

		// System.out.println("\n\nticketList.size() = " + listaTicket.size());
		// float percentage = (float) (listaTicket.size() * 0.01); //calcolo la
		// percentuale dalla listaticket in cui ne ho tolti circa la meta, a causa dei
		// check
		// float percentage2 = (float) (488 * 0.01);
		System.out.println("numTicket = " + numTicket);
		float percentage = (float) (numTicket * 0.01);
		// String.format("%.3f", percentage);
		// String.format("%.3f", percentage2);

		int perc = Math.round(percentage); // Math.round() converts a floating-point number to the nearest integer by
											// first adding 0.5 and then truncating value after decimal point
		System.out.println("percentage = " + (int) percentage);
		System.out.println("perc = " + (int) perc);

		for (int i = 0; i < listGood.size(); i++) {
			Integer IV = listGood.get(i).getIV();
			Integer OV = listGood.get(i).getOV();
			Integer FV = listGood.get(i).getFV();
			String id = listGood.get(i).getID();
			Integer index = listGood.get(i).getIndex();

			float proportion = (float) (FV - IV) / (FV - OV);
			// String.format("%.3f", proportion);

			// Integer P_NOround = (int) proportion; //ARROTONDO PER DIFETTO
			Integer P_round = Math.round(proportion); // ARROTONDO PER EFFETTO
			// System.out.println(index + ")\tTicket ID = " + id + "\t\tP_NOround = " +
			// P_NOround);
			System.out.println(index + ")\tTicket ID = " + id + "\t\tP_round = " + P_round);

			System.out.println("proportion = " + proportion);

			System.out.println("P = " + P_round);
			// System.out.println("-----------------\n");

			proportionValue.put(index, P_round);

		}

		System.out.println("-----------------\n\n");
		// System.out.println("il primo ticket e: " + listNoIV.get(0).getID());

		// FACCIO PROVA CON IL PRIMO TICKET E GLI APPLICO PROPORTION

		for (int j = 0; j < listNoIV.size(); j++) {

			Ticket ticket = listNoIV.get(j);
			String ticketID = ticket.getID();
			Integer index = ticket.getIndex();
			Integer OV = ticket.getOV();
			Integer FV = ticket.getFV();

			// System.out.println("proportionValue.keySet() = " + proportionValue.keySet());

			System.out.println(index + ")\tticketID = " + ticketID);
			ArrayList<Integer> listIndex = new ArrayList<>();
			// scorro HashMap
			for (Integer i : proportionValue.keySet()) {

				// Integer key = i;
				// Integer value = proportionValue.get(key);
				// System.out.println("key = " + key + "\t\tvalue = " + value);

				if (index > i) {

					if (listIndex.size() < perc) { // non sono ancora arrivata a 4, quindi posso aggiungere

						listIndex.add(i);
					}

					else {
						listIndex.remove(0);
						listIndex.add(i);

					}

				}
			}

			System.out.println(listIndex + "\n\n");

			// somma degli eleementi dell'array(le P)
			Integer sum = 0;

			for (int i = 0; i < listIndex.size(); i++) {
				System.out.println("P = " + proportionValue.get(listIndex.get(i)));
				sum = sum + proportionValue.get(listIndex.get(i));
			}

			float average = (float) sum / perc;
			String.format("%.3f", average);
			int P_average_round = Math.round(average); // media delle P dei 4 difetti precedenti
			int P_average_NOround = (int) average;
			System.out.println("P_average = " + P_average_round);
			System.out.println("P_average_NOround = " + P_average_NOround);

			// Predicted IV = FV -(FV - OV) * P
			Integer predicted_IV = FV - (FV - OV) * P_average_round;
			System.out.println("predicted_IV = " + predicted_IV);

			// devo settare IV dei ticket presenti nella listNoIV, ma poi dovro cambiare
			// anche quelli in listaTicket, che e la lista principale
			// da cui prendo informazioni e su cui lavoro sempre
			ticket.setIV(predicted_IV);

			/*
			 * ticket.getAV().clear(); //elimino gli elementi di AV, poiche potrei avere
			 * dati inconsistenti presi da JIRA, e setto manualmente gli altri.
			 * 
			 * for(int k = predicted_IV; k<FV;k++) { ticket.getAV().add(k); }
			 */

			// devo calcolare la media delle P!

			System.out.println("-----------------\n\n");

		}

		// modificando i ticket dentro listNoIV, automaticamente modifico i ticket
		// presenti in listaTicket, quindi ora avro la lista ticket
		// modificata con tutti i valori di IV e i valori di FV e OV coerenti
		// RetrieveTicketsJIRA.printArrayList(listaTicket);

		System.out.println("\n\nTicket con predictedIV = ");
		for (int i = 0; i < listNoIV.size(); i++) {
			/*
			 * System.out.println("L'ID del ticket e = " + listaTicket.get(i).getID());
			 * System.out.println("L'AV del ticket e = " + listaTicket.get(i).getAV());
			 * System.out.println("La data di creazione del ticket e = " +
			 * listaTicket.get(i).getCreationDate());
			 * System.out.println("La data di risoluzione del ticket e = " +
			 * listaTicket.get(i).getResolutionDate()); System.out.println("OV e = " +
			 * listaTicket.get(i).getOV()); System.out.println("FV e = " +
			 * listaTicket.get(i).getFV());
			 */

			System.out.println(listNoIV.get(i).getID() + "\t\t IV: " + listNoIV.get(i).getIV() + "\t\t OV: "
					+ listNoIV.get(i).getOV() + "\t\tFV: " + listNoIV.get(i).getFV() + "\t\t AV: "
					+ listNoIV.get(i).getAV());

			// System.out.println("-----------------\n");

		}

		/*
		 * //assegno correttamente AV ad ogni ticket for(int i = 0; i <
		 * listaTicket.size(); i++) { Ticket ticket = listaTicket.get(i);
		 * ticket.getAV().remove(null); Integer IV = ticket.getIV(); Integer FV =
		 * ticket.getFV();
		 * 
		 * }
		 */

		System.out.println("listaTicket size = " + listaTicket.size());

	}

	public static void modifyListAV() {

		System.out.println("\n\n-----------------------MODIFICO AV -----------------------------");
		for (int i = 0; i < listaTicket.size(); i++) {
			Ticket ticket = listaTicket.get(i);
			Integer IV = ticket.getIV();
			Integer OV = ticket.getOV();
			Integer FV = ticket.getFV();

			ticket.getAV().clear(); // elimino gli elementi di AV, poiche potrei avere dati inconsistenti presi da
									// JIRA, e setto manualmente gli altri.

			for (int k = IV; k < FV; k++) {

				ticket.getAV().add(k);
			}

			System.out.println(ticket.getID() + "\t\t IV: " + IV + "\t\t OV: " + OV + "\t\tFV: " + FV + "\t\t AV: "
					+ ticket.getAV());
		}

		System.out.println("listaTicket size = " + listaTicket.size());

		System.out.println("\n\n------------");

		/*
		 * for(int i = 0; i<listaTicket.size();i++) {
		 * 
		 * //controllo le AV, e se in AV c'e una release > 7, la cancello Ticket ticket
		 * = listaTicket.get(i); for(int k = 0 ; k <ticket.getAV().size(); k++) {
		 * if(ticket.getAV().get(k) > halfRelease) {
		 * ticket.getAV().remove(ticket.getAV().get(k)); k--; } }
		 * 
		 * System.out.println(ticket.getID() + "\t\t IV: " + ticket.getIV() +
		 * "\t\t OV: "+ ticket.getOV() +"\t\tFV: " + ticket.getFV() + "\t\t AV: " +
		 * ticket.getAV() ); }
		 */
		System.out.println("listaTicket size = " + listaTicket.size());

	}

	/*
	 * public static void removeRelease() {
	 * 
	 * System.out.
	 * println("\n\n-----------------------REMOVE RELEASE -----------------------------"
	 * ); System.out.println("numero release = " + numRelease); /* float half =
	 * (float) numRelease/2; String.format("%.3f", half); halfRelease = (int) half;
	 * //arrotondo in difetto //System.out.println("half= " + half);
	 * System.out.println("halfRelease = " + halfRelease);
	 * System.out.println("size listaTicket = " + listaTicket.size() + "\n\n");
	 */

	// CANCELLO da listaTicket i ticket che hanno IV>7
	/*
	 * for(int i = 0; i<listaTicket.size();i++) { if(listaTicket.get(i).getIV() >
	 * halfRelease) { //li rimuovo dalla lista dei ticket
	 * System.out.println("Ticket con IV>7 = "+ listaTicket.get(i).getID());
	 * listaTicket.remove(i); i--; } }
	 * 
	 * System.out.println("\n\nsize listaTicket = " + listaTicket.size() + "\n\n");
	 * for(int i = 0; i<listaTicket.size();i++) {
	 * 
	 * //controllo le AV, e se in AV c'e una release > 7, la cancello Ticket ticket
	 * = listaTicket.get(i); for(int k = 0 ; k <ticket.getAV().size(); k++) {
	 * if(ticket.getAV().get(k) > halfRelease) {
	 * ticket.getAV().remove(ticket.getAV().get(k)); k--; } }
	 * 
	 * 
	 * }
	 * 
	 * System.out.println("\n\n----------------------------------------------------"
	 * ); System.out.println("listaTicket size = " + listaTicket.size());
	 * RetrieveTicketsJIRA.printArrayList(listaTicket);
	 * 
	 * }
	 */

}