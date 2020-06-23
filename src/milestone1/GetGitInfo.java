/* 
 - prendo tutti i commit del progetto, e a ogni ticket associo il commit in cui è presente
 
 - di ogni commit relativo al ticket, mi prendo i file cambiati in quel commit 
 
 */

package milestone1;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.json.JSONException;

public class GetGitInfo {

	private static ArrayList<String> commit_proj = new ArrayList<>();

	private static final String REPO = "D:/Università/magistrale/isw2/Codici/bookkeeper/.git";
	private static Path repoPath = Paths.get("D:/Università/magistrale/isw2/Codici/bookkeeper");
	private static HashMap<RevCommit, Integer> releaseCommitMap = new HashMap<>(); // hashmap con commit e release a cui
																					// appartiene
	private static Repository repository;
	private static ArrayList<FileJava> listFile;
	private static ArrayList<FileAlias> listAlias;
	public static final String FILE_EXTENSION = ".java";

	public static void buildDataset(List<Ticket> listaTicket, List<Release> releases, int halfRelease)
			throws IOException, GitAPIException {

		FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
		repository = repositoryBuilder.setGitDir(new File(REPO)).readEnvironment() // scan environment GIT_* variables
				.findGitDir() // scan up the file system tree
				.setMustExist(true).build();

		// ora devo prendere i commit che mi interessano
		// per ogni commit prendo file .java
		// verifico buggyness

		Log.infoLog("\n\n\n--------------------------------getAllCommit-------");

		// ritorna hashmap che contiene key = commit, value = release
		getAllCommit(releases);
		Log.infoLog("\n\n releaseCommitMap size  = " + releaseCommitMap.size() + "\n"); // 3344 commit totali

		// ora devo associare a ogni release una lista di commit che appartengono a tale
		// release, per poi andarmi a prendere la data
		// del commit più recente
		assignCommitRelease(releases); // aggiungo a classe release la lista dei commit

		// prende la data più recente dei commit appartenenti a quella release
		// e mi prende tutti i file java relativi a quel commit, e poi li mette nelal
		// listFile della classe release
		// modifico classe release mettendo nella lista la classe e buggyness "no"
		getRecentCommitFiles(releases);

		// ora devo gestire i rename!!!!!!!!!!!!!!!!!
		checkRename(releases);

		Log.infoLog("lista Alias size = " + listAlias.size());

		for (int i = 0; i < releases.size(); i++) {
			// Log.infoLog("il numero di file java che mi servono è :" +
			// releases.get(i).getListFile().size());
		}

		checkCommRel(listaTicket, releases);

		// prendo altre metriche (ho già preso buggyness)
		Metrics.prova2(releases, repository);

		// scrivo csv

		// creoFileCSV
		String filePath = "D:\\" + "Università\\magistrale\\isw2\\Codici\\deliverable2ISW2\\dataset.csv";
		Log.infoLog("starting write user.csv file: " + filePath);
		try (FileWriter fileWriter = new FileWriter(filePath)) {
			fileWriter.append("Version; FileName ; Size;nr;nAuth; LOC_touched;LOC_Added; Max_locAdded;"
					+ "Avg_locAdded;Churn;MAX_Churn; AVG_Churn;ChgSetSize;Max_chgSetSize;Avg_chgSetSize;Buggy\n");

			for (int k = 0; k < halfRelease; k++) {
				for (int j = 0; j < releases.get(k).getListFile().size(); j++) {
					fileWriter.append(releases.get(k).getIndex().toString());
					fileWriter.append(";");
					fileWriter.append(releases.get(k).getListFile().get(j).getName());
					fileWriter.append(";");
					fileWriter.append(releases.get(k).getListFile().get(j).getSize().toString());
					fileWriter.append(";");
					fileWriter.append(releases.get(k).getListFile().get(j).getNr().toString());
					fileWriter.append(";");
					fileWriter.append(releases.get(k).getListFile().get(j).getnAuth().toString());
					fileWriter.append(";");
					fileWriter.append(releases.get(k).getListFile().get(j).getLOCtouched().toString());
					fileWriter.append(";");
					fileWriter.append(releases.get(k).getListFile().get(j).getLOCadded().toString());
					fileWriter.append(";");
					fileWriter.append(releases.get(k).getListFile().get(j).getMaxlocAdded().toString());
					fileWriter.append(";");
					fileWriter.append(releases.get(k).getListFile().get(j).getAvglocAdded().toString());
					fileWriter.append(";");
					fileWriter.append(releases.get(k).getListFile().get(j).getChurn().toString());
					fileWriter.append(";");
					fileWriter.append(releases.get(k).getListFile().get(j).getMAXChurn().toString());
					fileWriter.append(";");
					fileWriter.append(releases.get(k).getListFile().get(j).getAVGChurn().toString());
					fileWriter.append(";");
					fileWriter.append(releases.get(k).getListFile().get(j).getChgSetSize().toString());
					fileWriter.append(";");
					fileWriter.append(releases.get(k).getListFile().get(j).getMaxChgSetSize().toString());
					fileWriter.append(";");
					fileWriter.append(releases.get(k).getListFile().get(j).getAvgChgSetSize().toString());
					fileWriter.append(";");
					// fileWriter.append(releases.get(k).getListFile().get(j).getChgSetSize().toString());
					// fileWriter.append(";");
					fileWriter.append(releases.get(k).getListFile().get(j).getBugg());
					fileWriter.append("\n");
				}

			}

		} catch (Exception ex) {
			// string writer scrive stringa
			// print writer per pritnare su stringa
			// passo string su argomento del print, e print come argomento del stacktrace
			Log.errorLog("Errore nella scrittura del csv");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			Log.errorLog(sw.toString());
		}
	}

	public static List<DiffEntry> getDiffs(RevCommit commit) throws IOException {
		List<DiffEntry> diffs;
		DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
		df.setRepository(repository);
		df.setDiffComparator(RawTextComparator.DEFAULT);
		df.setContext(0);
		df.setDetectRenames(true);
		if (commit.getParentCount() != 0) {
			RevCommit parent = (RevCommit) commit.getParent(0).getId();
			diffs = df.scan(parent.getTree(), commit.getTree());
		} else {
			RevWalk rw = new RevWalk(repository);
			ObjectReader reader = rw.getObjectReader();
			diffs = df.scan(new EmptyTreeIterator(), new CanonicalTreeParser(null, reader, commit.getTree()));
		}
		return diffs;

	}

	public static void checkRename(List<Release> releases) throws IOException {

		listAlias = new ArrayList<>();
		for (RevCommit commit : releaseCommitMap.keySet()) {
			List<DiffEntry> diffs = getDiffs(commit);
			if (diffs != null) {
				for (DiffEntry diff : diffs) {
					String type = diff.getChangeType().toString();
					String oldPath = diff.getOldPath();
					String newPath = diff.getNewPath();
					if (type.equals("RENAME") && oldPath.endsWith(FILE_EXTENSION)) {
						boolean oPCheck = true;
						boolean nPCheck = true;
						populateListAlias(oldPath, newPath, oPCheck, nPCheck);

					}
				}
			}
		}
		updateRenames(releases);
	}

	public static void populateListAlias(String oldPath, String newPath, boolean oPCheck, boolean nPCheck) {
		for (FileAlias fA : listAlias) {

			if (!fA.checkAlias(oldPath)) {
				oPCheck = false;
				if (fA.checkAlias(newPath)) {
					fA.getAlias().add(newPath);
					nPCheck = false;
				}
			}
			if (!fA.checkAlias(newPath)) {
				nPCheck = false;
				if (fA.checkAlias(oldPath)) {
					fA.getAlias().add(oldPath);
					oPCheck = false;
				}
			}
		}

		if (oPCheck && nPCheck) {

			FileAlias alias = new FileAlias();
			alias.getAlias().add(oldPath);
			alias.getAlias().add(newPath);
			listAlias.add(alias);

		}
	}

	public static void updateRenames(List<Release> releases) {
		for (Release release : releases) {
			updateListAlias(release);
		}

		for (Release release : releases) {
			updateListFileRelease(release);
		}

	}

	public static void updateListAlias(Release release) {
		// per ogni insieme di alias di ogni file calcolo il nome piu recente ad esso
		// assocaito
		// scorro le release perchè ho i file in ordine
		for (FileJava2 file : release.getListFile()) {
			String fileName = file.getName();
			for (FileAlias fA : listAlias) {
				for (String alias : fA.getAlias()) {
					if (alias.equals(fileName) || file.getName().contains(alias)) {
						fA.setLastFileName(alias);
					}
				}
			}
		}

	}

	public static void updateListFileRelease(Release release) {

		// per ogni DBEntry dotata di alias, imposto il nome del file come l'ultimo tra
		// gli alias con cui è conosciuto
		for (FileJava2 file : release.getListFile()) {
			String fileName = file.getName();
			for (FileAlias fA : listAlias) {
				for (String alias : fA.getAlias()) {
					if (alias.equals(fileName) || file.getName().contains(alias)) { // lo faccio solo per i file
																					// ocntenuti ella lista dei file di
																					// ogni rename --> sono quell che mi
																					// interessano
						file.setName(fA.getLastFileName());
					}
				}
			}
		}

	}

	/*
	 * assegno a ogni release contenuta nella lista releases, una lista di commit
	 * appartenenti a tale release
	 */
	public static void assignCommitRelease(List<Release> releases) {

		// scorro le release
		// scorro hash map
		// se valore hash map è release, associo il commit (key) alla release

		for (int i = 0; i < releases.size(); i++) {
			for (RevCommit commit : releaseCommitMap.keySet()) {
				int rel = releaseCommitMap.get(commit);
				if (rel == releases.get(i).getIndex()) {
					// aggiungo alla release il commit
					releases.get(i).getListCommit().add(commit);
				}
			}
		}

		// i commit sono 3344, controllo
		int somma = 0;
		for (int i = 0; i < releases.size(); i++) {
			Log.infoLog("I commit della release " + releases.get(i).getIndex() + "sono : "
					+ releases.get(i).getListCommit().size() + "\n\n");
			somma = somma + releases.get(i).getListCommit().size();
		}
		Log.infoLog("la somma di tutti i commit è : " + somma);
	}

	public static void getRecentCommitFiles(List<Release> releases) throws IOException, GitAPIException {

		int i;
		TreeMap<LocalDateTime, RevCommit> dateCommit = new TreeMap<>(); // TreeMap mi permette di avere key ordinate

		// creo lista in cui metto tutti i commit con le dateMax
		ArrayList<RevCommit> listMaxCommit = new ArrayList<>();
		RevCommit lastCommit;

		for (i = 0; i < releases.size(); i++) {
			// mi prendo tutte le date dei commit che stanno nella lsitacommit associata
			// alla release, e mi prendo la data più recente

			// scorro listaCommit della release e mi prendo le date di ogni commit
			for (int k = 0; k < releases.get(i).getListCommit().size(); k++) {
				RevCommit commit = releases.get(i).getListCommit().get(k);
				LocalDateTime date = Instant.ofEpochSecond(commit.getCommitTime()).atZone(ZoneId.of("UTC"))
						.toLocalDateTime();
				dateCommit.put(date, commit);
			}

			/*
			 * //ora ho lista ordinata in base alla data dei commit, provo a metterli nella
			 * lista delel relase in modo ordinato releases.get(i).getListCommit().clear();
			 * for(LocalDateTime data : date_Commit.keySet()) {
			 * releases.get(i).getListCommit().add(date_Commit.get(data)); }
			 */

			if (dateCommit.size() != 0) {
				// mi prendo ultimo commit e me lo metto nella lista (mi serve per quelli che
				// non hanno commit)
				lastCommit = dateCommit.lastEntry().getValue();
				listMaxCommit.add(lastCommit);
				// ora di qyesto commit mi prendo ttutti i file e li metto nella lista associata
				// alla release
				getAllFileJava(lastCommit, releases.get(i));
			} else {
				// prendo commit precedente

				lastCommit = listMaxCommit.get(i - 1);
				listMaxCommit.add(lastCommit);

				getAllFileJava(lastCommit, releases.get(i));

			}

			dateCommit.clear();

		}
	}

	// per ogni ticket mi scorro la hashmap in cui ci sono i commit, mi prendo i
	// commit in cui c'è il ticket, prendo la lista di diff nel commit, e controllo
	// la buggyness di ogni file nel commit
	// e aggiorno la buggyness
	public static void checkCommRel(List<Ticket> listaTicket, List<Release> releases) throws IOException {

		List<DiffEntry> diffList = new ArrayList<>();

		for (int i = 0; i < listaTicket.size(); i++) {

			String idTicket = listaTicket.get(i).getID();
			List<Integer> aV = listaTicket.get(i).getAV();
			// mi scorro i commit e vedo i ticket
			// i commit stanno in releaseCommitMap, key = commit, value = release
			for (RevCommit commit : releaseCommitMap.keySet()) {
				String message = commit.getFullMessage();
				if (message.contains(idTicket + ":")) {

					// prendo tutti i file .java che sono stati DELETE o MODIFY nel commit e li
					// metto in una lista
					// mi serve per poter trovare i file modificati
					getDiff(commit, diffList); // diffList contiene diff element relativi al commit

					// controllo buggyness confrontando release commit e modifico la lista di
					// oggetti di tipo FileJava inserendo i nuovi file
					checkBugg(diffList, aV, releases);

				}

				diffList.clear();

			}
		}

	}

	public static void getDiff(RevCommit commit, List<DiffEntry> listFileJava) throws IOException {

		// lista che contiene i file .java relativi al commit
		List<DiffEntry> diffs;
		DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
		df.setRepository(repository);
		df.setDiffComparator(RawTextComparator.DEFAULT);
		df.setDetectRenames(true);
		if (commit.getParentCount() != 0) {
			RevCommit parent = (RevCommit) commit.getParent(0).getId();
			diffs = df.scan(parent.getTree(), commit.getTree());
		} else {
			RevWalk rw = new RevWalk(repository);
			ObjectReader reader = rw.getObjectReader();
			diffs = df.scan(new EmptyTreeIterator(), new CanonicalTreeParser(null, reader, commit.getTree()));
		}

		for (DiffEntry diff : diffs) {
			if (diff.toString().contains(FILE_EXTENSION) && (diff.getChangeType() == DiffEntry.ChangeType.MODIFY
					|| diff.getChangeType() == DiffEntry.ChangeType.DELETE)) {
				listFileJava.add(diff);

			}
		}

	}

	public static void checkBugg(List<DiffEntry> diffList, List<Integer> aV, List<Release> releases) {

		// in diffList ci stanno le diff relative al commit
		// ora per ogni diff, scorro la lista delle release , prendo la listFile, prendo
		// il name di ogni file, e lo confronto, prendo la release,
		// se la release è contenuta in AV setto yes
		// System.out.println("AV = " + AV);

		if (!aV.isEmpty()) {
			for (int i = 0; i < diffList.size(); i++) {
				String javaClass;

				if (diffList.get(i).getChangeType() == DiffEntry.ChangeType.RENAME
						|| diffList.get(i).getChangeType() == DiffEntry.ChangeType.DELETE) {
					javaClass = diffList.get(i).getOldPath(); // mi prendo il path
				} else {
					javaClass = diffList.get(i).getNewPath();
				}

				for (int k = 0; k < releases.size(); k++) { // scorro release

					// per ogni release mi prendo la lista dei file java e mi prendo il nome del
					// file
					if (!releases.get(k).getListFile().isEmpty()) {
						for (int j = 0; j < releases.get(k).getListFile().size(); j++) {
							String nameFile = releases.get(k).getListFile().get(j).getName();
							String alias = checkAlias(javaClass);
							String nameToBeUsed = null;
							if ((alias != null)) {

								nameToBeUsed = alias;

							} else {

								nameToBeUsed = javaClass;
							}

							if (nameToBeUsed.equals(nameFile)) {

								if (releases.get(k).getIndex() >= aV.get(0)
										&& releases.get(k).getIndex() <= aV.get(aV.size() - 1)) {
									// il file è buggy in quella release
									releases.get(k).getListFile().get(j).setBugg("yes");

								}
							}
						}
					}

				}

			}
		}
	}

	public static String checkAlias(String nameFile) {

		for (FileAlias fA : listAlias) {
			if (fA.getAlias().contains(nameFile)) {
				return fA.getLastFileName();
			}
		}
		return null;
	}

	public static void getAllFileJava(RevCommit lastCommit, Release release) throws IOException, GitAPIException {

		try (Git git = Git.init().setDirectory(repoPath.toFile()).call()) {

		}

		InitCommand init = Git.init();
		init.setDirectory(repoPath.toFile());
		try (Git git = init.call()) {

		}

		try (Git git = Git.open(repoPath.toFile())) {

			ObjectId treeId = lastCommit.getTree();
			List<String> filePath = new ArrayList<>();

			try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
				treeWalk.reset(treeId);
				treeWalk.setRecursive(true);

				while (treeWalk.next()) {
					if (treeWalk.getPathString().endsWith(FILE_EXTENSION)) {
						filePath.add(treeWalk.getPathString());
						String nameFile = treeWalk.getPathString();

						FileJava2 file = new FileJava2(nameFile);

						// set buggyness
						file.setBugg("no");

						// set LOC
						int size = Metrics.loC(treeWalk, git, file);
						file.setSize(size);
						release.getListFile().add(file);
					}
				}
			} catch (IOException e) {
				Log.errorLog("Errore nel prendere i file java associati al commit");
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				Log.errorLog(sw.toString());
			}
			Log.infoLog("\n\nIl numero di file .java relativi alla release è: " + filePath.size());

			// per ogni release mi devo prendere tutti i file, cerco quel file se è nel
			// commit e mi prendo le differenze

		}

	}

	public static void printListFile(List<FileJava> list) {

		for (int i = 0; i < list.size(); i++) {
			FileJava file = list.get(i);
			Log.infoLog("\nfile = " + file.getName());
			Log.infoLog("oldPath = " + file.getoldPath() + "\n");

			for (int release : file.getMap().keySet()) {
				Log.infoLog("release " + release + "\tbuggyness: " + file.getMap().get(release));
			}
		}

	}

	// ritorna una mappa mapCommRel con key = commit, value = release
	public static Map<RevCommit, Integer> getAllCommit(List<Release> releases) throws GitAPIException, IOException {

		// mapCommRel con key = commit, value = release, in cui ci vado a mettere tutti
		// i commit e la relativa release

		float half = (float) releases.size() / 2;
		int halfRelease = Math.round(half); // arrotondo in eccesso così prendo più release
		Log.infoLog("halfRelease = " + halfRelease);

		// ritorna una lista che contiene tutti i commit del progetto
		ArrayList<RevCommit> listaCommit = new ArrayList<>();

		FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
		repository = repositoryBuilder.setGitDir(new File(REPO)).readEnvironment() // scan environment GIT_* variables
				.findGitDir() // scan up the file system tree
				.setMustExist(true).build();

		try (Git git = Git.init().setDirectory(repoPath.toFile()).call()) {

		}

		InitCommand init = Git.init();
		init.setDirectory(repoPath.toFile());
		try (Git git = init.call()) {

		}

		try (Git git = Git.open(repoPath.toFile())) {

			// log del progetto
			/*
			 * returns an iterable of RevCommit instances. With this object, you have access
			 * to commit information like the id, time, message, and author.
			 */

			Iterable<RevCommit> logs = git.log().all().call();

			for (RevCommit commit : logs) {
				// System.out.print(Instant.ofEpochSecond(rev.getCommitTime())); //giorno e ora
				// commit

				// Log.infoLog("commit : " + commit.getId().getName());
				// Log.infoLog(String.valueOf(commit.getCommitTime())); //giorno e ora commit,
				// espresso in secondi che passano dal momento del commit
				// a me serve in localDateTime, per confrontarlo con data delle release

				LocalDateTime commitDate = Instant.ofEpochSecond(commit.getCommitTime()).atZone(ZoneId.of("UTC"))
						.toLocalDateTime();

				// Log.infoLog("data commit = " + commitDate);
				// System.out.print(rev.getFullMessage()); //messaggio nel commit
				// System.out.println(rev.getId().getName()); //id commit
				// System.out.print(rev.getAuthorIdent().getName()); //autore commit
				// System.out.println(rev.getAuthorIdent().getEmailAddress()); //email autore
				// commit

				// devo verificare la relase a cui appartiene il commit, poichè, se è > della
				// metà, non aggiungo il commit
				// e assegno la release al commit

				// check contiene il numero di release (indice release) a cui appartiene il
				// commit
				int check = setCommitRelease(releases, commitDate);

				/*
				 * SE DIMEZZO RELEASE if(check != 0) { //la release del commit appartiene alla
				 * prima metà, quindi aggiungo il commit alla lista dei commit
				 * lista_commit.add(commit); releaseCommitMap.put(commit, check); vero++; } else
				 * { falso++; }
				 */

				// SE NON DIMEZZO RELEASE
				releaseCommitMap.put(commit, check); // aggiungo coppia commit-release

			}

		}

		Log.infoLog("il numero dei commit che hanno release < 7 è: " + listaCommit.size() + "\n\n");

		// System.out.println("\n\n releaseCommitMap size = " + releaseCommitMap.size()
		// + "\n"); //3344 commit totali
		return releaseCommitMap;

	}

	// ritorna numRelease ---------/ SE DIMEZZO RELEASE invece -->ritorna numRelease
	// se la release appartiene alle prime 7, 0 altrimenti
	public static int setCommitRelease(List<Release> releases, LocalDateTime commitDate) {

		// verifico e setto release commit
		int numRelease = 0;

		for (int i = 0; i < releases.size(); i++) {
			if (commitDate.isBefore(releases.get(i).getDate())) {
				// Log.infoLog("La release del commit è: " + releases.get(i).getIndex());
				numRelease = releases.get(i).getIndex();
				break; // fine
			}

		}

		return numRelease;

	}

	public static void main(String[] args) throws GitAPIException, IOException, JSONException {
		// main
	}

}
