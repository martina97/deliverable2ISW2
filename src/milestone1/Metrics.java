package milestone1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.json.JSONException;

public class Metrics {

	public static int loC(TreeWalk treeWalk, Git git, FileJava2 file) throws IOException {
		ObjectLoader loader = git.getRepository().open(treeWalk.getObjectId(0));
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		loader.copyTo(output);

		String contentFile = output.toString();
		StringTokenizer token = new StringTokenizer(contentFile, "\n"); // frammenta stringa in token quando trova \n

		int count = 0;
		while (token.hasMoreTokens()) {
			count++;
			token.nextToken();
		}

		file.setSize(count);
		return count;
	}

	public static void prova2(List<Release> releases, Repository repository) throws IOException {

		System.out.println("\n\n\n***************************    PROVA *********************************\n\n");

		RevWalk rw = new RevWalk(repository);
		int nr, locAdded, locTouched, locDeleted, churn, chgGetSize;
		int locAddedOnce, churnOnce, chgGetSizeOnce;
		Integer max;
		int avg;

		List<RevCommit> comList;
		List<Integer> churnList = new ArrayList<>();
		List<Integer> locAddedList = new ArrayList<>();
		List<String> numFiles = new ArrayList<>();
		List<Integer> chgSetSizeList = new ArrayList<>();
		List<PersonIdent> authors = new ArrayList<>();

		// mi prendo tutti i commit nella release e mi calcolo le metriche per ogni file
		// delal release
		for (int i = 0; i < releases.size(); i++) {
			// per ogni file nella release
			for (int k = 0; k < releases.get(i).getListFile().size(); k++) {
				FileJava2 file = releases.get(i).getListFile().get(k);
				String fileName = file.getName();
				nr = 0;
				locAdded = 0;
				locTouched = 0;
				locDeleted = 0;
				churn = 0;
				chgGetSize = 0;

				for (int j = 0; j < releases.get(i).getListCommit().size(); j++) {
					RevCommit commit = releases.get(i).getListCommit().get(j);

					RevCommit parent = null;

					if (commit.getParentCount() != 0) {
						parent = commit.getParent(0);

					}
					DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
					df.setRepository(repository);
					df.setDiffComparator(RawTextComparator.DEFAULT);
					df.setContext(0);
					df.setDetectRenames(true);
					List<DiffEntry> diffs;
					if (parent != null) {
						diffs = df.scan(parent.getTree(), commit.getTree());
					} else {
						ObjectReader reader = rw.getObjectReader();
						diffs = df.scan(new EmptyTreeIterator(),
								new CanonicalTreeParser(null, reader, commit.getTree()));
					}
					for (DiffEntry diff : diffs) { // For each file changed in the commit

						String diffName;
						if (diff.toString().endsWith(".java") && (diff.getChangeType().toString().equals("RENAME")
								|| diff.getChangeType().toString().equals("DELETE"))) {
							diffName = diff.getOldPath();
						}

						else {
							diffName = diff.getNewPath();
						}

						String alias = GetGitInfo.checkAlias(diffName);
						String nameToBeUsed = null;

						if (alias != null) {
							nameToBeUsed = alias;
						} else {
							nameToBeUsed = diffName;

						}

						if (nameToBeUsed.contains(fileName) || diff.getNewPath().contains(fileName)) {

							nr++;

							// aggiusta autori + file = dbENtry.getfilename ?
							// se la lista di autori non contiene l'autore del commit corrente, lo aggiungo
							if (authors.contains(commit.getAuthorIdent()) == false) {
								authors.add(commit.getAuthorIdent());
							}

							for (Edit edit : df.toFileHeader(diff).toEditList()) {

								locAddedOnce = edit.getEndB() - edit.getBeginB();
								locAdded += edit.getEndB() - edit.getBeginB();
								locAddedList.add(locAddedOnce);

								locDeleted += edit.getEndA() - edit.getBeginA(); // endA=BeginB

								churnOnce = locAdded - locDeleted;
								churnList.add(churnOnce);

							}
							// prendo i path tutti i file toccati dal commit
							// cosi se contengono il file che sto esaminando, vedo quanti ne ho committati
							// con lui
							if (diff.getChangeType().toString().equals("DELETE")) {
								numFiles.add(diff.getOldPath());
							} else { // se ho MODIFY, ADD o RENAME, aggiungo newPath del file della diffEntry
								numFiles.add(diff.getNewPath());
							}
						}
					}
					if (numFiles.contains(fileName)) {

						chgGetSize = chgGetSize + numFiles.size() - 1; // numero dei file commitati insieme al file
																		// "dbEntry.get(i).getFilename()"

						chgGetSizeOnce = numFiles.size() - 1;
						chgSetSizeList.add(chgGetSizeOnce);
					}
				}

				file.setNr(nr);
				file.setnAuth(authors.size());

				// prendo loc touched per un file (dbEntry.get(i).getFilename) in un commit di
				// una release, e vado agli altri
				// commit della stessa release per vedere le modifiche apportate sempre a quello
				// stesso file

				// dopo che ho scorso tutti i commit di una release che contengono un certo
				// file, calcolo :

				// ============= LOC TOUCHED , LOC ADDED , MAX&AVG
				max = maxElement(locAddedList); // devono essere quelle volta per volta (non somma)
				avg = calculateAverage(locAddedList);

				locTouched = locAdded + locDeleted;

				file.setMaxLocAdded(max);
				file.setAvgLOCAdded(avg);
				file.setLOCadded(locAdded);
				file.setLOCtouched(locTouched);

				// ============= CHURN, MAX&AVG
				churn = locAdded - locDeleted;

				max = maxElement(churnList);
				avg = calculateAverage(locAddedList);

				file.setChurn(churn);
				file.setMAXChurn(max);
				file.setAVGChurn(avg);

				// ============= chgSetSize, MAX&AVG
				max = maxElement(chgSetSizeList);
				avg = calculateAverage(chgSetSizeList);

				file.setChgSetSize(chgGetSize);
				file.setMaxChgSetSize(max);
				file.setAvgChgSetSize(avg);

				// ============= CLEAR LISTS =============

				locAddedList.clear();
				churnList.clear();
				chgSetSizeList.clear();
				numFiles.clear();
				authors.clear();

			}

		}
	}

	public static int maxElement(List<Integer> list) {

		if (list.size() > 0) {
			int max = list.get(0);
			for (int i = 0; i < list.size(); i++) {

				if (list.get(i) > max) {
					max = list.get(i);
				}

			}
			return max;
		} else {
			return 0;
		}
	}

	public static int calculateAverage(List<Integer> list) {
		int i;
		int avg;
		double sum;
		double tempAvg;
		double len;
		sum = 0;
		len = (double) list.size();
		for (i = 0; i < list.size(); i++) {
			sum = sum + list.get(i);
		}
		tempAvg = sum / len;
		String.format("%3f", tempAvg);
		avg = (int) Math.round(tempAvg);
		return avg;
	}

	public static void main(String[] args) throws GitAPIException, IOException, JSONException {
		// main
	}

}