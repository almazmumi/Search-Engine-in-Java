import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

public class SearchEngine {

	public static void main(String[] args) throws IOException {

		String y;
		do {

			System.out.print("Enter search query> ");
			Scanner kb = new Scanner(System.in);
			String search = kb.nextLine();

			String[] forAnd = search.split(" AND ");
			String[] forOr = search.split(" OR ");
			String[] forNot = search.split(" ");

			if ((search.indexOf("NOT") != -1) && (search.indexOf("AND") != -1)) {
				searchForAkeyNOTAND(forNot[1].toLowerCase(), forAnd[1].toLowerCase());
			} else if (search.indexOf("OR") != -1) {
				searchForAkeyOR(forOr[0].toLowerCase(), forOr[1].toLowerCase());
			} else if (search.indexOf("NOT") != -1) {
				searchForAkeyNOT(forNot[1].toLowerCase());
			} else if (search.indexOf("AND") != -1) {
				searchForAkeyAND(forAnd[0].toLowerCase(), forAnd[1].toLowerCase());
			} else {
				search = search.toLowerCase();
				searchForAkey(search);
			}
			System.out.print("Do you want to search again? (y/n) ");
			y = kb.nextLine();

		} while (y.matches("y"));

	}

	// prepare filesList array to go through them..
	public static File[] getFilesList() {
		final File path = new File("src/files/");
		File[] filesArray = path.listFiles();
		return filesArray;
	}

	/*
	 * put each files' words in a single hashMap and create array contains all
	 * hashMaps of the files ..
	 */
	public static HashMap<String, Integer>[] arrayOfHashes() throws FileNotFoundException {

		HashMap<String, Integer>[] arrayOfHashes = new HashMap[getFilesList().length]; // array
																						// of
																						// hashMap
		for (int i = 0; i < arrayOfHashes.length; i++) {
			arrayOfHashes[i] = new HashMap<String, Integer>();
			Scanner input = new Scanner(new File(getFilesList()[i] + ""));
			String nextWord = "";
			while (input.hasNext()) {
				nextWord = input.next().toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "");
				nextWord = removeStopword(nextWord);
				if (nextWord.length() > 2) {

					Integer occurence = arrayOfHashes[i].get(nextWord);

					if (occurence == null)
						occurence = 1;
					else
						occurence++;
					arrayOfHashes[i].put(nextWord, occurence);

				}
			}

			// to get the relative frequency
			for (Entry<String, Integer> hashMap : arrayOfHashes[i].entrySet()) {
				Integer occurence = hashMap.getValue();
				arrayOfHashes[i].put(hashMap.getKey(), (occurence * 1000) / arrayOfHashes[i].size());
			}

		}
		return arrayOfHashes;
	}

	public static String getTerms(int index, String key) throws IOException {
		File[] files = getFilesList();
		key = key.toLowerCase();
		BufferedReader bf = new BufferedReader(new FileReader(files[index]));
		String sentence = bf.readLine();
		int nextIndex = sentence.toLowerCase().indexOf(" " + key + " ");

		String Fsentence = sentence.substring((nextIndex - 20) < 0 ? 0 : nextIndex - 20,
				(nextIndex + 20) > sentence.length() ? sentence.length() : nextIndex + 20);

		String searchKey = ConsoleColors.GREEN_UNDERLINED + key + ConsoleColors.RESET;

		Fsentence = Fsentence.replaceAll(key, searchKey);

		return "..." + Fsentence + "...";
	}

	public static String removeStopword(String s) {
		Pattern p = Pattern.compile("\\b(the|and|or|of|for|to|be)\\b\\s?");
		Matcher m = p.matcher(s);
		String s1 = m.replaceAll("");
		return s1;
	}

	public static void searchForAkey(String searchKey) throws IOException {
		System.out.println("Single term query: " + searchKey);

		File[] files = getFilesList();
		ArrayList<QueryWord> result = new ArrayList<QueryWord>();

		long startTime = System.currentTimeMillis();
		int indexes = 0;
		HashMap<String, Integer>[] arrayOfHashes2 = arrayOfHashes();
		for (int j = 0; j < files.length; j++) {
			HashMap<String, Integer> arrayOfHashes = arrayOfHashes2[j];
			if (arrayOfHashes.containsKey(searchKey)) {
				indexes += 1;
				int occurs = arrayOfHashes.get(searchKey);
				result.add(new QueryWord(occurs, "Found in " + files[j].getName() + "  with Relative Frequncy of: "
						+ occurs + "\n" + getTerms(j, searchKey)));
			}
		}
		System.out.println("Found in " + indexes + " files");
		Collections.sort(result);
		for (QueryWord q : result) {
			System.out.println(q.toStringOne());
		}
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("\nThe Number of milli seconds to complete " + totalTime);

	}

	public static void searchForAkeyNOT(String searchKey) throws FileNotFoundException {
		System.out.println("NOT queries: " + "!" + searchKey);

		File[] files = getFilesList();

		long startTime = System.currentTimeMillis();
		int total = 0;
		HashMap<String, Integer>[] arrayOfHashes2 = arrayOfHashes();
		for (int j = 0; j < files.length; j++) {
			HashMap<String, Integer> arrayOfHashes = arrayOfHashes2[j];
			if (!arrayOfHashes.containsKey(searchKey)) {
				total += 1;
				System.out.println(total + ". NOT Found in " + files[j].getName()); // File
																					// index
																					// for
																					// the
																					// word
			}
		}
		System.out.println("NOT Found in " + total + " files");
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("\nThe Number of milli seconds to complete " + totalTime);
	}

	public static void searchForAkeyNOTAND(String searchKey1, String searchKey2) throws FileNotFoundException {
		System.out.println("NOT/AND queries: " + "!(" + searchKey1 + " && " + searchKey2 + ")");

		File[] files = getFilesList();

		long startTime = System.currentTimeMillis();
		int total = 0;
		HashMap<String, Integer>[] arrayOfHashes2 = arrayOfHashes();
		for (int j = 0; j < files.length; j++) {
			HashMap<String, Integer> arrayOfHashes = arrayOfHashes2[j];
			if (!(arrayOfHashes.containsKey(searchKey1) && arrayOfHashes.containsKey(searchKey2))) {
				total += 1;
				System.out.println(total + ". NOT Found in " + files[j].getName()); // File
																					// index
																					// for
																					// the
																					// word
			}
		}
		System.out.println("NOT Found in " + total + " files");
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("\nThe Number of milli seconds to complete " + totalTime);
	}

	public static void searchForAkeyAND(String searchKey1, String searchKey2) throws IOException {
		System.out.println("AND queries: " + searchKey1 + " && " + searchKey2);

		File[] files = getFilesList();
		ArrayList<QueryWord> result = new ArrayList<QueryWord>();

		long startTime = System.currentTimeMillis();
		int total = 0;
		HashMap<String, Integer>[] arrayOfHashes2 = arrayOfHashes();
		for (int j = 0; j < files.length; j++) {
			HashMap<String, Integer> arrayOfHashes = arrayOfHashes2[j];
			if (arrayOfHashes.containsKey(searchKey1) && arrayOfHashes.containsKey(searchKey2)) {
				total += 1;
				int occurs = arrayOfHashes.get(searchKey1) * arrayOfHashes.get(searchKey2);

				result.add(new QueryWord(occurs, "Found in " + files[j].getName() + " |  Freq:  " + occurs + "\n"
						+ getTerms(j, searchKey1) + " | ", getTerms(j, searchKey2)));

			}
		}
		System.out.println("Found in " + total + " files");
		Collections.sort(result);
		for (QueryWord q : result) {
			System.out.println(q.toStringTwo());
		}
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("\nThe Number of milli seconds to compleate " + totalTime);

	}

	public static void searchForAkeyOR(String searchKey1, String searchKey2) throws IOException {
		System.out.println("OR queries: " + searchKey1 + " || " + searchKey2);
		File[] files = getFilesList();
		ArrayList<QueryWord> resultS = new ArrayList<QueryWord>();
		ArrayList<QueryWord> resultC = new ArrayList<QueryWord>();

		int occurs = 0;
		long startTime = System.currentTimeMillis();
		int total = 0;
		HashMap<String, Integer>[] arrayOfHashes2 = arrayOfHashes();
		for (int j = 0; j < files.length; j++) {
			HashMap<String, Integer> arrayOfHashes = arrayOfHashes2[j];
			if (arrayOfHashes.containsKey(searchKey1) && arrayOfHashes.containsKey(searchKey2)) {
				occurs = arrayOfHashes.get(searchKey1) + arrayOfHashes.get(searchKey2);
				total += 1;
				resultC.add(new QueryWord(occurs, "Found in " + files[j].getName() + " |  Freq:  " + occurs + "\n"
						+ getTerms(j, searchKey1) + " | ", getTerms(j, searchKey2)));
			} else if (arrayOfHashes.containsKey(searchKey1)) {
				total += 1;
				occurs = arrayOfHashes.get(searchKey1);
				resultS.add(new QueryWord(occurs,
						"Found in " + files[j].getName() + " |  Freq:  " + occurs + "\n" + getTerms(j, searchKey1)));
			} else if (arrayOfHashes.containsKey(searchKey2)) {
				total += 1;
				occurs = arrayOfHashes.get(searchKey2);
				resultS.add(new QueryWord(occurs,
						"Found in " + files[j].getName() + " |  Freq:  " + occurs + "\n" + getTerms(j, searchKey2)));
			}
		}
		System.out.println("Found in " + total + " files");
		System.out.println("Saperate Files: ");
		Collections.sort(resultS);
		for (QueryWord q : resultS) {
			System.out.println(q.toStringOne());
		}
		System.out.println("Common Files: ");
		Collections.sort(resultC);
		for (QueryWord q : resultC) {
			System.out.println(q.toStringTwo());
		}
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("\nThe Number of milli seconds to compleate " + totalTime);
	}
}

class QueryWord implements Comparable<Object> {
	private int occurence;
	private String sentenceOne;
	private String sentenceTwo;
	private int relativeF;

	public QueryWord(int occurence, String sentenceOne) {
		this.occurence = occurence;
		this.sentenceOne = sentenceOne;
	}

	public QueryWord(int occurence, String sentenceOne, String sentenceTwo) {
		this.occurence = occurence;
		this.sentenceOne = sentenceOne;
		this.sentenceTwo = sentenceTwo;
	}

	public void setRelativeFrequncy(int count) {
		relativeF = occurence * 1000 / count;
	}

	public int getRelativeFrequncy() {
		return this.relativeF;
	}

	public String getSentence() {
		return this.sentenceOne;
	}

	@Override
	public int compareTo(Object o) {
		QueryWord p = (QueryWord) o;
		if (this.occurence > p.occurence)
			return -1;
		else if (this.occurence == p.occurence)
			return 0;
		else
			return 1;
	}

	public String toStringOne() {
		return sentenceOne;

	}

	public String toStringTwo() {
		return sentenceOne + "" + sentenceTwo;

	}
}