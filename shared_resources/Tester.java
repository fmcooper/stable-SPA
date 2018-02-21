package shared_resources;

import java.io.*;
import java.util.*;
import java.math.*;


/**
 *	<p>Tests the stability of an SPA-ST instance result. </p>
 *
 * @author Frances
 */

public class Tester {
	/**<p>The model of the instance.</p>*/
	private static Model model;

	/**
	* <p> Help message. </p>
	*/
	public static void helpAndExit() {
		System.out.println();
		System.out.println("This class tests that the results file is a stable matching that adheres to upper and lower quotas");
		System.out.println();
		System.out.println("Using this program:");
		System.out.println("$ java Tester [-h] <instance file name> <results file name> <String info>");
		System.out.println();
		System.exit(0);
	}


	/**
	* <p> Main method. Processes all files. </p>
	*/
	public static void main(String args[]) {
		// input testing
		String instinput = "";
		String resinput = "";
		String info = "";
		File instf = null;
		File resf = null;
		try {
			instinput = args[0];
			if (!instinput.equals("-h")) {
				instf = new File(instinput);
			}

			resinput = args[1];
			if (!resinput.equals("-h")) {
				resf = new File(resinput);
			}

			info = args[2];
		}
		catch (Exception e) {
			System.out.println("Input file error");
			helpAndExit();
		}

		if (instinput.equals("-h") || resinput.equals("-h") || info.equals("-h")) {
			helpAndExit();
		}

		// create the model
		model = Util_FileIO.readFile(instf);

		// set the assignments in the model from the results file
		int [] assignments = Util_FileIO.inputRawResult(resf);
		if (assignments == null) {
			System.out.println("\n" + info + "_Correctness tests not conducted due to results input error (e.g. possibly a timeout)");
		}
		else {
			model.studentAssignments = assignments;

			// set the time and date instance variable in the model
			Util_FileIO.createCal();
			String easyResults = Util_FileIO.getCal(false) + "\n";
			model.timeAndDate = easyResults;

			if (model == null) {
				System.out.println("** error: the file " + instf.getName() + " is incompatable");
				helpAndExit();
			}

			// prep for matching statistics and check correctness
			model.setAssignmentInfo();
			boolean stable = checkStable();
			boolean upperlower = checkUpperLower();
			System.out.println("\n" + info + "_Correctness_tests: " + easyResults);
			System.out.println(info + "_Correctness_stable: " + stable);
			System.out.println(info + "_Correctness_upperLower: " + upperlower);

			if (!stable) {
				System.err.println("not stable: " + instf.getName());
			}
			if (!upperlower) {
				System.err.println("not upperlower: " + instf.getName());
			}
		}
	}


	/**
	 * <p>Checks that the assignment adheres to upper and lower quotas.</p>
	 * @return if the assignment adheres to upper and lower quotas.
	 */
	public static boolean checkUpperLower() {
		boolean adheres = true;
		model.setAssignmentInfo();
		for (int j = 0; j < model.numProjects; j++) {
			int numPassigned = model.numProjectAssignments[j];
			if ((numPassigned < model.projectLowerQuotas[j]) || (numPassigned > model.projUpperQuotas[j])) {
				adheres = false;
			}
		}

		for (int k = 0; k < model.numLecturers; k++) {
			int numLassigned = model.numLecturerAssignments[k];
			if ((numLassigned < model.lecturerLowerQuotas[k]) || (numLassigned > model.lecturerUpperQuotas[k])) {
				adheres = false;
			}
		}

		return adheres;
	}


	/**
	 * <p>Checks that the assignment is stable.</p>
	 * @return if the assignment is stable
	 */
	public static boolean checkStable() {
		// print (matching, "");
		// assume stable until proven otherwise
		boolean isStable = true;
		int[] matching = model.studentAssignments;

		// for each student
		for (int x = 0; x < model.numStudents; x++) {
			// find the project assignment and lecturer assignment
			int projAssignment = matching[x];
			int lecturer = -1;
			if (projAssignment != -1) {
				lecturer = model.projLecturers[projAssignment];
			}


			// rank of the assigned project for this student
			int[] stPrefList = model.studentPrefArray[x];
			int targetRank = stPrefList.length;
			for (int projloc = 0; projloc < stPrefList.length; projloc++) {
				if (stPrefList[projloc] == projAssignment) {
					targetRank = model.studentPrefRankArray[x][projloc];
				}
			}

			// for all student project pairs in the students preference list decide whether it is a blocking pair
			for (int projloc = 0; projloc < stPrefList.length; projloc++) {
				int currentRank = model.studentPrefRankArray[x][projloc];
				// test if this pair is a blocking pair
				boolean blocking = testBlockingPair(x, projAssignment, targetRank, lecturer, projloc, currentRank, matching);
				if (blocking) {
					isStable = false;
					System.out.println("** blocking");
					System.err.println("** blocking");
				}
			}
		}

		return isStable;
	}


	/**
	 * <p>Test a specific student project pair to see if it is a blocking pair.</p>
	 * @return if the pair is stable
	 */
	public static boolean testBlockingPair(int stInd, int projAssignment, int projAssRank, int lecAssignment, int projloc, int currentRank, int[] matching) {
		int project = model.studentPrefArray[stInd][projloc];
		int lecturer = model.projLecturers[project];

		// (1) we know p_j is acceptable as we are only iterating over the preference lists

		// (2) either s_i is unassigned in M, or s_i prefers p_j to M(s_i)
		boolean two = false;
		if (projAssignment == -1) {
			two = true;
		}
		if (currentRank < projAssRank) {
			two = true;
		}

		// (3) either 
		// (a) p_j is undersubscribed and l_k is undersubscribed, or
		int numProjAssignments = numAssignedToProj(project, matching);
		int projCap = model.projUpperQuotas[project];
		boolean pUnder = false;
		pUnder = numProjAssignments < projCap;

		int numLecAssignments = numAssignedToLec(lecturer, matching);
		int lecCap = model.lecturerUpperQuotas[lecturer];
		boolean lUnder = false;
		lUnder = numLecAssignments < lecCap;

		boolean threeA = pUnder && lUnder;


		// (b) p_j is undersubscribed, l_k is full and either s_i is in M(l_k) or l_k prefers s_i to the worst student in M(l_k)
		boolean stMatchedLec = lecAssignment == lecturer;

		int worstRankForLec = getWorstRankForLec(lecturer, matching);
		int rankInLecPref = getRankInLecPref(stInd, lecturer, matching);

		boolean prefToWorst = rankInLecPref < worstRankForLec;
		boolean threeB = pUnder && !lUnder && (stMatchedLec || prefToWorst);

		// (c) p_j is full and l_k prefers s_i to the worst student in M(p_j)
		int worstRankInP = getWorstRankInPForLec(lecturer, project, matching);
		boolean prefToWorstInP = rankInLecPref < worstRankInP;
		boolean threeC = !pUnder && prefToWorstInP;

		// calculate whether this is a blocking pair and return
		boolean blockingPair = two && (threeA || threeB || threeC);
		if (blockingPair) {
			System.out.println("s: " + stInd + " p: " + project);
			System.out.println("two: " + two);
			System.out.println("threeA: " + threeA);
			System.out.println("threeB: " + threeB);
			System.out.println("threeC: " + threeC);
		}

		return blockingPair;
	}





	/**************************** these functions are called multiple times by the tester class ****************************/
	/**************************** they can be be sped up by creating master lists ****************************/

	/**
	 * <p> Retrieves the number of students assigned to a project.</p>
	 * @param p          the project 
	 * @return the number of assignments for the given project
	 */
	public static int numAssignedToProj(int p, int[] matching) {
		int numAssignments = 0;
		for (int i = 0; i < matching.length; i++) {
			if (matching[i] == p) {
				numAssignments++;
			}
		}
		return numAssignments;
	}


	/**
	 * <p> Retrieves the number of students assigned to a lecturer.</p>
	 * @param l          the lecturer 
	 * @return the number of assignments for the given lecturer
	 */
	public static int numAssignedToLec(int l, int[] matching) {
		int numAssignedToLec = 0;
		ArrayList<Integer> lecProjs = model.lecturersProjs.get(l);

		for (int i : lecProjs) {
			numAssignedToLec += numAssignedToProj(i, matching);
		}

		return numAssignedToLec;
	}


	/**
	 * <p> Retrieves the worst rank that a lecturer currently accepts.</p>
	 * @param lec          the lecturer 
	 * @return the worst rank this lecturer accepts
	 */
	public static int getWorstRankForLec(int lec, int[] matching) {
		int worstRank = 0;
		ArrayList<Integer> lecProjInds = model.lecturersProjs.get(lec);

		ArrayList<Integer> lecProjs = new ArrayList<Integer>();

		for (int i = 0; i < lecProjInds.size(); i++) {
			int p = lecProjInds.get(i);
			lecProjs.add(p);
		}

		// for each student in the lecturers preference list, if the student is matched to this lecturer then save rank
		int [] lecPrefs = model.lecturerPrefArray[lec];
		for (int prefInd = 0; prefInd < lecPrefs.length; prefInd++) {
			int studentInd = lecPrefs[prefInd];

			if (lecProjs.contains(matching[studentInd])) {
				worstRank =  model.lecturerPrefRankArray[lec][prefInd];
			}
		}   

		return worstRank;
	}


	/**
	 * <p> Retrieves the worst rank that a lecturer currently accepts for a given project.</p>
	 * @param lec          the lecturer 
	 * @param p            the project
	 * @return the worst rank this lecturer accepts for this project
	 */
	public static int getWorstRankInPForLec(int lec, int p, int[] matching) {
		int worstRank = 0;

		// for each student in the lecturers preference list, if the student is matched to this lecturer then save rank

		int [] lecPrefs = model.lecturerPrefArray[lec];
		for (int prefInd = 0; prefInd < lecPrefs.length; prefInd++) {
			int studentInd = lecPrefs[prefInd];
			if (matching[studentInd] == p) {
				worstRank =  model.lecturerPrefRankArray[lec][prefInd];
			}
		}   
		return worstRank;
	}


	/**
	 * <p> Returns the rank of the given student in the given lecturers preference list.</p>
	 * @param stInd          the student index 
	 * @param lec            the lecturer
	 * @return the rank of this student in this lecturers preference list
	 */
	public static int getRankInLecPref(int stInd, int lec, int[] matching) {
		int rank = -1;

		int [] lecPrefs = model.lecturerPrefArray[lec];
		for (int prefInd = 0; prefInd < lecPrefs.length; prefInd++) {
			if (stInd == lecPrefs[prefInd]) {
				rank = model.lecturerPrefRankArray[lec][prefInd];
			}
		}
		return rank;   
	}
}

















