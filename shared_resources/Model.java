package shared_resources;
import java.util.ArrayList;

/**
 * <p> This class represents the model of an SPA-ST instance. </p>
 *
 * @author Frances
 */

public class Model {   
	/** <p> The number of students. </p> */
	public int numStudents;
	/** <p> The number of projects. </p> */
	public int numProjects;
	/** <p> The number of lecturers. </p> */
	public int numLecturers;

	/** <p> The project lower quotas. </p> */
	public int[] projectLowerQuotas;
	/** <p> The project capacities. </p> */
	public int[] projUpperQuotas;
	/** <p> The project lecturers. </p> */
	public int[] projLecturers;

	/** <p> The lecturer lower quotas. </p> */
	public int[] lecturerLowerQuotas;
	/** <p> The lecturer upper quotas. </p> */
	public int[] lecturerUpperQuotas;

	/** <p> The 2D array of student pref lists. studentPrefArray[i][j] = p if student i hasproject p at position j in their preference list. </p> */
	public int[][] studentPrefArray;
	/** <p> The 2D array of student pref ranks. studentPrefRankArray[i][j] = r if student i hasproject p as rank r. </p> */
	public int[][] studentPrefRankArray;

	/** <p> The 2D array of lecturer pref lists. studentPrefArray[i][j] = p if student i hasproject p at position j in their preference list. </p> */
	public int[][] lecturerPrefArray;
	/** <p> The 2D array of lecturer pref ranks. studentPrefRankArray[i][j] = r if student i hasproject p as rank r. </p> */
	public int[][] lecturerPrefRankArray;
	/** <p> Targets for each lecturer. </p> */
	public int[] lecturerTargets;
	/** <p> Lists of lecturers projects. </p> */
	public ArrayList<ArrayList<Integer>> lecturersProjs;
	/** <p> Lists of project projects. </p> */
	public ArrayList<ArrayList<Integer>> projectAssignments;

	/** <p> The student assignments. studentAssignments[i] = a if student i is assigned to project j. </p> */
	public int[] studentAssignments;
	/** <p> Number of project assignments. </p> */
	public int[] numProjectAssignments;
	/** <p> Number of lecturer assignments. </p> */
	public int[] numLecturerAssignments;

	/** <p> Whether there is a feasible solution. </p> */
	public boolean feasible;
	/** <p> String hold information to be printed from the SPA_IP inc which optimisations were run. </p> */
	public String infoString;
	/** <p> Maximum size of student preference list. </p> */
	public int maxPrefLength;

	/** <p> Stores when an instance run took place. </p> */
	public String timeAndDate;

	/**
	 * <p> Constructor for the Model class - sets the instance variables. </p>
	 * @param studentPrefArray		
	 * @param studentPrefRankArray 
	 * @param lecturerPrefArray 
	 * @param lecturerPrefRankArray 
	 * @param projectLowerQuotas	
	 * @param projUpperQuotas	
	 * @param projLecturers 
	 * @param lecturerLowerQuotas 
	 * @param lecturerUpperQuotas  
	 * @param lecturerTargets
	 */
	public Model(int[][] studentPrefArray, int[][] studentPrefRankArray, int[][] lecturerPrefArray, int[][] lecturerPrefRankArray,
			int[] projectLowerQuotas, int[] projUpperQuotas, int[] projLecturers, int[] lecturerLowerQuotas, int[] lecturerUpperQuotas, int[] lecturerTargets) {
		feasible = true;  // assume this model is feasible until proven otherwise

		// set the instance variables
		this.studentPrefArray = studentPrefArray;
		this.studentPrefRankArray = studentPrefRankArray;
		this.lecturerPrefArray = lecturerPrefArray;
		this.lecturerPrefRankArray = lecturerPrefRankArray;

		this.projectLowerQuotas = projectLowerQuotas;
		this.projUpperQuotas = projUpperQuotas;
		this.projLecturers = projLecturers;

		this.lecturerLowerQuotas = lecturerLowerQuotas;
		this.lecturerUpperQuotas = lecturerUpperQuotas;
		this.lecturerTargets = lecturerTargets;

		numStudents = studentPrefArray.length;
		numProjects = projectLowerQuotas.length;
		numLecturers = lecturerLowerQuotas.length;

		// get lists of projects for each lecturer
		lecturersProjs = new ArrayList<ArrayList<Integer>>();
		for (int z = 0; z < numLecturers; z++) {
			ArrayList<Integer> lecturerProj = new ArrayList<Integer>();
			for (int plIndex = 0; plIndex < projLecturers.length; plIndex++) {
				if (projLecturers[plIndex] == z) {
					lecturerProj.add(plIndex);
				}
			}
			lecturersProjs.add(lecturerProj);
		}

		// all student assignments are initially set to -1
		studentAssignments = new int[numStudents];
		for (int i = 0; i < numStudents; i++) {
			studentAssignments[i] = -1;
		}

		// set the maximum preference list length int variable
		maxPrefLength = 0;
		for (int i = 0; i < studentPrefArray.length; i++) {
			if (studentPrefArray[i].length > maxPrefLength) {
				maxPrefLength = studentPrefArray[i].length;
			}
		}
		infoString = "";
		timeAndDate = "";
	}


	/**
	 * <p>Returns a string if there is no matching found.</p>
	 * @return no matching found string
	 */
	public String getNoMatchingResult() {
		String returnString = "";

		// add time and date information
		returnString += timeAndDate;

		// add a String to indicate that no matching was found
		returnString += "No matching found.";

		return returnString;
	}


	/**
	 * <p>Returns a vertical string of the assigned values.</p>
	 * @return a vertical string of assigned values
	 */
	public String getRawResults() {
		String returnString = "";

		// student assignments
		for (int i = 0; i < numStudents; i++) {
			int project = studentAssignments[i] + 1;
			returnString += project + " ";
		}
		return returnString;
	}


	/**
	 * <p>Returns a horizontal string of the assigned values.</p>
	 * @return a horizontal string of assigned values
	 */
	public String getStringAssignments() {
		String s = "";
		for (int i = 0; i < studentAssignments.length; i++) {
			s += studentAssignments[i] + " ";
		}
		return s;
	}


	/**
	 * <p>Set the assignment information for stats methods to use.</p>
	 */
	public void setAssignmentInfo() {
		// saves project assignments
		projectAssignments = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < numProjects; i++) {
			projectAssignments.add(new ArrayList<Integer>());
		}

		numProjectAssignments = new int[numProjects];
		numLecturerAssignments = new int[numLecturers];

		// student assignments & save the students that a project is assigned
		for (int i = 0; i < numStudents; i++) {
			int studentNum = i+1;
			int project = studentAssignments[i];
			if (project != -1) {
				projectAssignments.get(project).add(i);
			}
		}

		// project assignments
		for (int i = 0; i < numProjects; i++) {
			numProjectAssignments[i] = projectAssignments.get(i).size();
		}

		// lecturer assignments
		for (int i = 0; i < numLecturers; i++) {
			int numLecAssignments = 0;
			for (int proj : lecturersProjs.get(i)) {
				numLecAssignments += projectAssignments.get(proj).size();
			}
			numLecturerAssignments[i] = numLecAssignments;
		}
	}


	/**************************** results summaries ****************************/

	/**
	 * <p> Returns a neat and user friendly version of results for the current student assignments. </p>
	 * @return results for the student assignments
	 */
	public String getAllResults() {
		setAssignmentInfo();

		String assignmentsString = "";

		// student assignments & save the students that a project is assigned
		assignmentsString += "# details of which project each student is assigned \n";
		assignmentsString += "Student_assignments:\n";
		for (int i = 0; i < numStudents; i++) {
			int studentNum = i+1;
			assignmentsString += "s_" + studentNum + ":";
			int project = studentAssignments[i];
			if (project == -1) {
				assignmentsString += " no assignment\n";
			}
			else {
				int pl = projLecturers[project];
				assignmentsString += " p_" + project + " (l_" + pl + ")" + "\n";
			}
		}
		assignmentsString += "\n";

		// project assignments
		assignmentsString += "# details of which students each project is assigned, and the number of students\n" ;
		assignmentsString += "# assigned compared to the projects maximum capacity \n";
		assignmentsString += "Project_assignments:\n";
		for (int i = 0; i < numProjects; i++) {
			int pl = projLecturers[i];
			int projectNum = i+1;
			assignmentsString += "p_" + projectNum + " (l_" + pl + "): ";

			if (numProjectAssignments[i] == 0) {
				assignmentsString += "no assignment";
			}

			for (int j : projectAssignments.get(i)) {
				assignmentsString += "s_" + j + " ";
			}
			assignmentsString += "   " + numProjectAssignments[i] + "/" + projUpperQuotas[i] + "\n";
		}
		assignmentsString += "\n";

		// lecturer assignments
		assignmentsString += "# details of which students each lecturer is assigned, and the number of students\n"; 
		assignmentsString += "# assigned compared to the lecturers maximum capacity (target in brackets) \n";
		assignmentsString += "Lecturer_assignments:\n";
		for (int i = 0; i < numLecturers; i++) {
			int lecturerNum = i + 1;
			assignmentsString += "l_" + lecturerNum + ": ";

			for (int proj : lecturersProjs.get(i)) {

				for (int j : projectAssignments.get(proj)) {
					int projNum = proj + 1;
					assignmentsString += "s_" + j + " (p_" + projNum + ") ";
				}
			}
			if (numLecturerAssignments[i] == 0) {
				assignmentsString += "no assignment";
			}
			assignmentsString += "   " + numLecturerAssignments[i] + "/" + lecturerUpperQuotas[i] + "   (" + lecturerTargets[i] + ")\n";
		}

		String returnString = "";

		// add information relating to which optimisations have taken place
		returnString += timeAndDate;
		returnString += infoString + "\n";

		// add further matching statistics
		int size = getMatchingSize();
		returnString += "# the number of students matched compared with the total number of students\n";
		returnString += "Matching_size: " + size + "/" + numStudents + "\n\n";

		int matchedCost = calcMatchedCost();
		returnString += "# the sum of ranks of student assignments in the matching\n";
		returnString += "Matching_cost: " + matchedCost + "\n\n";

		int maxDiff = calcMaxAbsDiffLec();
		returnString += "# the maximum absolute difference between the number of students assigned to a lecturer and their target\n";
		returnString += "Matching_max_lecturer_abs_diff: " + maxDiff + "\n\n";

		int lecDiffs = sumAbsLecDiffs();
		returnString += "# the sum the difference between the number of students assigned to a lecturer and their target\n";
		returnString += "Sum_lecturer_abs_diff: " + lecDiffs + "\n\n";

		int lecSqDiffs = sumSqAbsLecDiffs();
		returnString += "# the sum of squares of the difference between the number of students assigned to a lecturer and their target\n";
		returnString += "Sum_sq_lecturer_abs_diff: " + lecSqDiffs + "\n\n";

		int sumSqRanks = calcSumSqRanks();
		returnString += "# the sum of squares of the ranks\n";
		returnString += "Sum_sq_ranks: " + sumSqRanks + "\n\n";

		int sumSqDiffsAndRanks = calcSumSqDiffsAndRanks();
		returnString += "# the sum of squares of the lecturer diffs and ranks\n";
		returnString += "Sum_sq_diffs_and_ranks: " + sumSqDiffsAndRanks + "\n\n";

		String profile = getMatchingProfile();
		returnString += "# the number of students gaining their 1st, 2nd, 3rd choice project etc\n";
		returnString += "Matching_profile: " + profile + "\n\n";

		int matchedDegree = calcMatchedDegree();
		returnString += "# the highest rank of project assigned to a student in the matching\n";
		returnString += "Matching_degree: " + matchedDegree + "\n\n";

		// add the assignments string to the return string and return
		returnString += assignmentsString;
		return returnString;
	}


	/**
	 * <p> Returns a slightly less user friendly version of results for the current student assignments. </p>
	 */
	public String getBriefStatsFeasible() {
		String returnString = "";

		// add information relating to which optimisations have taken place
		returnString += "# " + timeAndDate;
		returnString += infoString + "\n";

		// add further matching statistics
		returnString += "Instance_feasible \n";

		int size = getMatchingSize();
		returnString += "Matching_size: " + size + "/" + numStudents + "\n";

		int matchedCost = calcMatchedCost();
		returnString += "Matching_cost: " + matchedCost + "\n";

		int maxDiff = calcMaxAbsDiffLec();
		returnString += "Matching_max_lecturer_abs_diff: " + maxDiff + "\n";

		int lecDiffs = sumAbsLecDiffs();
		returnString += "Sum_lecturer_abs_diff: " + lecDiffs + "\n";

		int lecSqDiffs = sumSqAbsLecDiffs();
		returnString += "Sum_sq_lecturer_abs_diff: " + lecSqDiffs + "\n";

		int sumSqRanks = calcSumSqRanks();
		returnString += "Sum_sq_ranks: " + sumSqRanks + "\n";

		int sumSqDiffsAndRanks = calcSumSqDiffsAndRanks();
		returnString += "Sum_sq_diffs_and_ranks: " + sumSqDiffsAndRanks + "\n";

		String profile = getMatchingProfile();
		returnString += "Matching_profile: " + profile + "\n";

		int matchedDegree = calcMatchedDegree();
		returnString += "Matching_degree: " + matchedDegree;

		return returnString;
	}


	/**
	 * <p> Returns a slightly less user friendly version of results for infeasible instances. </p>
	 */
	public String getBriefStatsInfeasible() {
		String returnString = "";
		returnString += "# " + timeAndDate;

		returnString += infoString + "\n";

		// add further matching statistics
		returnString += "Instance_infeasible \n";
		return returnString;

	}


	/**************************** matching statistics helper methods ****************************/

	/**
	 * <p>Calculates the sum of the squares of the absolute differences in lecturer targets and occupancies.</p>
	 */
	protected int sumSqAbsLecDiffs() {
		int[] diffs = getAbsDiffs();

		int sumSq = 0;

		// for each lecturer
		for (int z = 0; z < diffs.length; z++) {
			sumSq += diffs[z] * diffs[z];
		}

		return sumSq;
	}


	/**
	 * <p>Calculates the sum of the absolute differences in lecturer targets and occupancies.</p>
	 */
	protected int sumAbsLecDiffs() {
		
		int[] diffs = getAbsDiffs();

		int sum = 0;

		// for each lecturer
		for (int z = 0; z < diffs.length; z++) {
			sum += diffs[z];
		}

		return sum;
	}


	/**
	 * <p>Returns a list of the absolute differences in lecturer targets and occupancies.</p>
	 */
	protected int[] getAbsDiffs() {
		int[] diffs = new int[numLecturers];

		// for each lecturer
		for (int z = 0; z < numLecturers; z++) {

			// calculate the absolute difference
			int targetMinusNum = lecturerTargets[z] - numLecturerAssignments[z];
			int numMinusTarget = numLecturerAssignments[z] - lecturerTargets[z];

			int absDiff = 0;
			if (targetMinusNum < numMinusTarget) {
				absDiff = numMinusTarget;
			}
			else {
				absDiff = targetMinusNum;
			}

			diffs[z] = absDiff;
		}
		return diffs;
	}


	/**
	 * <p> Returns the cost of the matching for matched students only. </p>
	 * @return the cost of the matching
	 */
	public int calcMatchedCost() {
		int cost = 0;
		for (int i = 0; i < numStudents; i++) {
			// only consider matched students
			if (studentAssignments[i] != -1) {
				cost += getRank(i, studentAssignments[i]);
			}   
		}
		return cost;
	}


	/**
	 * <p> Returns the sum of squares of student ranks for matched students only. </p>
	 * @return the sum of squares of student ranks
	 */
	public int calcSumSqRanks() {
		int sumSqRanks = 0;
		for (int i = 0; i < numStudents; i++) {
			// only consider matched students
			if (studentAssignments[i] != -1) {
				sumSqRanks += getRank(i, studentAssignments[i]) * getRank(i, studentAssignments[i]);
			}   
		}
		return sumSqRanks;
	}


	/**
	 * <p> Returns the sum of squares of lec diffs and ranks. </p>
	 * @return the sum of squares of lec diffs and ranks
	 */
	public int calcSumSqDiffsAndRanks() {
		int sumSqDiffs = sumSqAbsLecDiffs();
		int sumSqRanks = calcSumSqRanks();
		return sumSqDiffs + sumSqRanks;
	}


	/**
	 * <p> Returns the maximum absolute difference between the number of students assigned to each 
	 * lecturer and their target. </p>
	 * @return the maximum absolute difference
	 */
	protected int calcMaxAbsDiffLec() {
		int maxDiff = -1;
		for (int k = 0; k < numLecturers; k++) {
			int targetMinusAssigned = lecturerTargets[k] - numLecturerAssignments[k];
			int assignedMinusTarget = numLecturerAssignments[k] - lecturerTargets[k];
			if (maxDiff < targetMinusAssigned) {
				maxDiff = targetMinusAssigned;
			}
			if (maxDiff < assignedMinusTarget) {
				maxDiff = assignedMinusTarget;
			}
		}
		return maxDiff;
	}


	/**
	 * <p> Returns the profile of the matching. </p>
	 * @return the profile of the matching
	 */
	public int[] calcProfile() {
		int[] profile = new int[maxPrefLength];
		for (int i = 0; i < numStudents; i++) {
			if (studentAssignments[i] != -1) {
				int rank = getRank(i, studentAssignments[i]);
				profile[rank - 1]++;
			}
		}
		return profile;
	}


	/**
	 * <p> Returns the rank for a given student index and project index. </p>
	 * @return the rank of the project or -1 if the project is unacceptable to the student
	 */
	public int getRank(int studentInd, int projectInd) {
		int[] studentPref = studentPrefArray[studentInd];
		for (int i = 0; i < studentPref.length; i++) {
			if (studentPref[i] == projectInd) {
				return studentPrefRankArray[studentInd][i];
			}
		}
		return -1;
	}


	/**
	 * <p> Returns the matched degree of the matching. </p>
	 * @return the matched degree of the matching
	 */
	public int calcMatchedDegree() {
		int matchedDegree = -1;
		int[] profile = calcProfile();

		for (int i = 0; i < profile.length; i++) {
			if (profile[i] != 0) {
				matchedDegree = i + 1;
			}
		}
		return matchedDegree;
	}


	/**
	 * <p> Returns the profile of the matching. </p>
	 * @return the profile of the matching
	 */
	public String getMatchingProfile() {

		int[] profile = calcProfile();
		String stringProfile = "< ";
		for (int i = 0; i < profile.length; i++) {
			stringProfile += profile[i] + " ";
		}
		stringProfile += ">";
		return stringProfile;
	}


	/**
	 * <p> Returns the size of the matching. </p>
	 * @return the size of the matching
	 */
	protected int getMatchingSize() {
		int numMatchedStudents = 0;
		for (int i = 0; i < studentAssignments.length; i++) {
			if (studentAssignments[i] != -1) {
				numMatchedStudents++;
			}
		}

		return numMatchedStudents;
	}


	/**
	 * <p> Collects the information on which optimisations were used in the IP run.</p>
	 */
	public void setInfoString(String s) {
		infoString = s;
	}



	/**************************** print statement helper methods ****************************/

	/**
	 * <p>Prints a small selection of results to the console.</p>
	 */
	public void printStdoutResults() {
		print(studentAssignments, "studentAssignments: ");
	}


	/**
	 * <p> Print all arrays of the model. </p>
	 */
	public void printModel() {
		System.out.println("--------------- model ---------------");
		print(studentPrefArray, "studentPrefArray");
		print(studentPrefRankArray, "studentPrefRankArray");
		print(lecturerPrefArray, "lecturerPrefArray");
		print(lecturerPrefRankArray, "lecturerPrefRankArray");
		print(projectLowerQuotas, "projectLowerQuotas");
		print(projUpperQuotas, "projUpperQuotas");
		print(projLecturers, "projLecturers");
		print(lecturerLowerQuotas, "lecturerLowerQuotas");
		print(lecturerUpperQuotas, "lecturerUpperQuotas");
		print(lecturerTargets, "lecturerTargets");
		print(studentAssignments, "studentAssignments");
	}


	/**
	 * <p> Print an int[][]. </p>
	 * @param intArray
	 * @param message
	 */
	private void print(int[][] intArray, String message) {
		String s = message + "\n";

		for (int[] row : intArray) {
			for (int cell : row) {
				s += cell + " ";
			}
			s += "\n";
		}
		System.out.println(s);
	}


	/**
	 * <p> Print an int[]. </p>
	 * @param intArray
	 * @param message
	 */
	public void print(int[] intArray, String message) {
		String s = message + "\n";

		for (int cell : intArray) {
			s += cell + " ";  
		}
		s += "\n";
		System.out.println(s);
	}
}
