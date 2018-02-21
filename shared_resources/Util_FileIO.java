package shared_resources;

import java.io.*;
import java.util.*;
import java.util.ArrayList;

/**
* <p> This class contains functions to read an SPA HR instance from a file and crete a Model instance, along
* with other IO functions (can deal with ties). </p>
*
* @author Frances
*/
public abstract class Util_FileIO {
	private static Calendar calendar;
	private static int maxPrefLength;

	// cannot be instantiated - this is a utility class
	private Util_FileIO() {
	}


	/**
	* <p> Input the instance from the file and create a Model. </p>
	* @param file 		the file to input data from
	* @return the created Model instance
	*/
	public static Model readFile(File file) {
		boolean incLecLQ = true;
		boolean incLecTargets = true;
		boolean incLecUQ = true;
		boolean incProjLQ = true;
		boolean incProjUQ = true;

		int numStudents = 0;
		int numProjects = 0;
		int numLecturers = 0;

		int[][] studentPrefs = new int[1][1];
		int[][] studentPrefRanks = new int[1][1];
		int[][] lecturerPrefs = new int[1][1];
		int[][] lecturerPrefRanks = new int[1][1];

		int[] projectLowerQuotas = new int[1];
		int[] projectCapacities = new int[1];
		int[] projectLecturers = new int[1];
		int[] lecturerUpperQuotas = new int[1];
		int[] lecturerLowerQuotas = new int[1];
		int[] lecturerTargets = new int[1];
		maxPrefLength = -1;

		FileReader fr = null;
		try {
			try {				
				// opens file and create a scanner on that file
				fr = new FileReader (file);
				Scanner scan = new Scanner(fr);
				// input the number of students and projects
				numStudents = scan.nextInt();
				numProjects = scan.nextInt();
				numLecturers = scan.nextInt();
				scan.nextLine();

				studentPrefs = new int[numStudents][];
				studentPrefRanks = new int[numStudents][];
				lecturerPrefs = new int[numLecturers][];
				lecturerPrefRanks = new int[numLecturers][];

				projectLowerQuotas = new int [numProjects];
				projectCapacities = new int [numProjects];
				projectLecturers = new int [numProjects];
				lecturerUpperQuotas = new int [numLecturers];
				lecturerLowerQuotas = new int [numLecturers];
				lecturerTargets = new int [numLecturers];
				
				// input student preferences
				for (int i = 0; i < numStudents; i++) {
					String prefList = scan.nextLine();
					int studentPrefsAndRanks[][] = getPrefsAndRanks(prefList, 1);
					studentPrefs[i] = studentPrefsAndRanks[0];
					studentPrefRanks[i] = studentPrefsAndRanks[1];
				}

				// input project lower and upper quotas, and project lecturer
				for (int i = 0; i < numProjects; i++) {
					String s = scan.nextLine();
					String[] ssplit = s.split("[ :]+");

					int argNum = 1;
					if (incProjLQ) {
						projectLowerQuotas[i] = Integer.parseInt(ssplit[argNum]);
						argNum++;
					}
					else {
						projectLowerQuotas[i] = 0;
					}

					if (incProjUQ) {
						projectCapacities[i] = Integer.parseInt(ssplit[argNum]);
						argNum++;
					}
					else {
						projectCapacities[i] = numStudents;
					}

					projectLecturers[i] = Integer.parseInt(ssplit[argNum]) - 1;

				}	

				// input lecturer lower and upper quotas, targets and pref lists
				for (int i = 0; i < numLecturers; i++) {
					String s = scan.nextLine();
					String[] ssplit = s.split("[ :]+");

					int[][] lecturerPrefsAndRanks;
					

					// add lecturer lower quotas, targets and upper quotas
					int argNum = 1;
					if (incLecLQ) {
						lecturerLowerQuotas[i] = Integer.parseInt(ssplit[argNum]);
						argNum++;
					}
					else {
						lecturerLowerQuotas[i] = 0;
					}

					if (incLecTargets) {
						lecturerTargets[i] = Integer.parseInt(ssplit[argNum]);
						argNum++;
					}

					if (incLecUQ) {
						lecturerUpperQuotas[i] = Integer.parseInt(ssplit[argNum]);
						argNum++;
					}
					else {
						lecturerUpperQuotas[i] = numStudents;
					}

					if (!incLecTargets && incLecUQ) {
						lecturerTargets[i] = lecturerUpperQuotas[i];
					}

					if (lecturerTargets[i] > lecturerUpperQuotas[i]) {
						lecturerTargets[i] = lecturerUpperQuotas[i];
					}

					lecturerPrefsAndRanks = getPrefsAndRanks(s, argNum);
					lecturerPrefs[i] = lecturerPrefsAndRanks[0];
					lecturerPrefRanks[i] = lecturerPrefsAndRanks[1];
				}	

				Model model = new Model(studentPrefs, studentPrefRanks, lecturerPrefs, lecturerPrefRanks, 
					projectLowerQuotas, projectCapacities, projectLecturers, lecturerLowerQuotas, lecturerUpperQuotas, lecturerTargets);
				return model;
			}
			// closes file if it was successfully opened
			finally {
				if (fr != null) {fr.close();}
			}
		}
		// catches IO exception
		catch (Exception e) {
			// error message if problem with input file
			System.out.println("Error in reading from file: " + file.toString());
		}
		return null;
	}


	/**
	* <p> Input results from a file. </p>
	* @param file 		the file to input results from
	* @return the array of integers representing project assignments
	*/
	public static int[] inputResult(File file, int numNums) {
		FileReader fr = null;
		int[] result = new int[numNums];
		try {
			try {				
				// opens file and create a scanner on that file
				fr = new FileReader (file);
				Scanner scan = new Scanner(fr);

				for (int i = 0; i < numNums; i++) {
					result[i] = scan.nextInt();
				}
			}

			// closes file if it was successfully opened
			finally {
				if (fr != null) {fr.close();}
			}
		}
		// catches IO exception
		catch (Exception e) {
			// error message if problem with input file
			System.out.println("Error in reading from file: " + file.toString());
		}
		return result;
	}


	/**
	* <p> Input results from a file. </p>
	* @param file 		the file to input results from
	* @return the array of integers representing student assignments
	*/
	public static int[] inputRawResult(File file) {
		FileReader fr = null;
		int[] result = new int [0];
		String resultsline = "";
		try {
			try {				
				// opens file and create a scanner on that file
				fr = new FileReader (file);
				Scanner scan = new Scanner(fr);

				boolean found = false;
				
				while (!found) {
					if (scan.hasNextLine()) {
						String line = scan.nextLine();
						String [] lineSplit = line.split(" ");
						if (lineSplit[0].equals("RawResults:")) {
							resultsline = scan.nextLine();
							found = true;
						}
					}
					else {
						break;
					}
				}
				if (!found) {
					return null;
				}
			}

			// closes file if it was successfully opened
			finally {
				if (fr != null) {fr.close();}
			}
		}
		// catches IO exception
		catch (Exception e) {

			// error message if problem with input file
			System.out.println("Error from file: " + file.toString());
		}

		String [] resultslineSplit = resultsline.split(" ");
		result = new int [resultslineSplit.length];

		for (int i = 0; i < resultslineSplit.length; i++) {
			int proj = Integer.parseInt(resultslineSplit[i]);
			proj--;
			result[i] = proj;
		}

		return result;
	}


	/*
	* <p> Creates the student ranks lists from the String preference lists. If ties need to be broken randomly that will be
	* done here. <\p>
	*/
	private static int[][] getPrefsAndRanks(String stringPrefs, int startingPosition) {
		int rank = 1;
		int tieRank = 1;
		boolean inTie = false;
		String[] studentSplit = stringPrefs.split("[ :]+");

		// define a 2D array to hold pref list and rank information...
		// for the line s_1: (p_1 p_2) p_3
		// we get the fillowing array:
		// prefAndRankList[0] = {1, 2, 3}
		// prefAndRankList[1] = {1, 1, 3}
		int[][] prefAndRankList = new int[2][studentSplit.length - startingPosition];

		// iterate over each item in the preference list
		// ignore the first token as it is just the student number
		for (int i = startingPosition; i < studentSplit.length; i++) {		
			int index = i - startingPosition;	
			// if there is a bracket
			if (studentSplit[i].contains("(")) {
				tieRank = rank;
				inTie = true;
				int proj = Integer.parseInt(studentSplit[i].substring(1,studentSplit[i].length()))-1;
				// set the project
				prefAndRankList[0][index] = proj;
				// set the rank
				prefAndRankList[1][index] = tieRank;
			}
			else if (studentSplit[i].contains(")")) {
				inTie = false;
				int proj = Integer.parseInt(studentSplit[i].substring(0,studentSplit[i].length()-1))-1;
				// set the project
				prefAndRankList[0][index] = proj;
				// set the rank
				prefAndRankList[1][index] = tieRank;
			}
			// else if there is no bracket and we are either in the middle of a tie or we are outside a tie
			else {
				int proj = Integer.parseInt(studentSplit[i])-1;
				// set the project
				prefAndRankList[0][index] = proj;

				if (inTie) {
					// set the rank
					prefAndRankList[1][index] = tieRank;
				}
				else {
					// set the rank
					prefAndRankList[1][index] = rank;
				}
			}
			rank++;
		}
		return prefAndRankList;
	}


	/**
	*<p>Create a calendar object.</p>
	*/
	public static void createCal() {
		calendar = new GregorianCalendar();
	}


	/**
	*<p>Return a short or long version of the calendar's time.</p>
	*/
	public static String getCal(boolean shortCal) {
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);

		if (shortCal) {
			return "" + year + "," + month + "," + day + "_" + hour + "," + minute + "," + second;
		}
		else {
			return "Date: " + year + "/" + month + "/" + day + "  Time: " + hour + ":" + minute + ":" + second;
		}	
	}
}
