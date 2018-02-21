package ip;

import gurobi.*;
import java.io.*;
import java.util.*;
import shared_resources.*;

/**
 * <p> This class contains the SPA_IP_HR_Alt IP for solving SPA instances with student and lecturer preferences and finds a
 * stable matching. </p>
 *
 * @author Frances Cooper
 */
public class SPA_IP_HR {
	/** <p>The model representing the SPA instance.</p> */
	Model m;
	/** <p>The number of students in the instance.</p> */                                       
	int numStudents;
	/** <p>The number of projects in the instance.</p> */
	int numProjects;
	/** <p>The number of lecturers in the instance.</p> */
	int numLecturers;

	/** <p>The gurobi environment.</p> */
	GRBEnv env;
	/** <p>The gurobi model.</p> */
	GRBModel grbmodel;

	/** <p>This array takes the form of the students preference list but has several attributes for
	  each student project pair.</p> */
	IP_model_unit[][] unitArray;
	/** <p>projectLists stores list of vars that correspond to a given project.</p> */
	ArrayList<ArrayList<GRBVar>> projectLists;
	/** <p>projectLists stores list of vars that correspond to a given lecturer.</p> */
	ArrayList<ArrayList<IP_model_unit>> lecturerLists;
	/** <p>rankList stores list of vars that correspond to a given rank.</p> */
	ArrayList<ArrayList<GRBVar>> rankLists;

	/** <p>The number of assignments constrained to be greater or equal to maxSize.</p> */
	double maxSize;
	/** <p>The number of assignments constrained to be less or equal to minSize.</p> */
	double minSize;

	String infoString = "# Information:\n";

	final int numberPotentialOpts = 6;

	boolean max;

	/**
	 * <p>The SPA_IP_HR constructor - allows optimisations to occur in different orders.</p>
	 * @param m      the model SPA instance
	 */
	public SPA_IP_HR(Model m, boolean max) {
		// instance variables are saved
		this.m = m;
		numStudents = m.numStudents;
		numProjects = m.numProjects;
		numLecturers = m.numLecturers;
		this.max = max;


		try {
			// the GRB model is set up and variables are added to model
			setUpGRBmodel();

			// gurobi flags
			grbmodel.getEnv().set(GRB.IntParam.OutputFlag, 0);
			grbmodel.getEnv().set(GRB.IntParam.Threads, 2);

			// add upper and lower quota constraints for projects and lecturers, and student upper quota (of 1)
			upperLowerConstraints();

			// add stability constraints
			infoString += "# This run finds a stable matching\n";
			stabilityConstraints();
		}
		catch (GRBException e) {
			System.out.println("Error in setting up the IP model");
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
			m.feasible = false;
		}  
	}


	/**
	 * <p>Runs the IP.</p>
	 */
	public void run() {	
		try {	
				// decide whether to optimise on maximum size
				if (max) {
					infoString += "# - Optimisation: finds a maximum sized matching\n";
					addMaxSizeConstraint();
				}

				// decide whether to optimise on minimum size
				if (!max) {
					infoString += "# - Optimisation: finds a minimum sized matching\n";
					addMinSizeConstraint();
				}

			setStudentAssignments();


			// write model then dispose of model and environment
			m.setInfoString(infoString);
			//grbmodel.write("SPA_IP_HR.lp");
			grbmodel.dispose();
			env.dispose();
		}

		catch (GRBException e) {
			System.out.println("Error in running the IP");
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
			m.feasible = false;
		}  
	}


	/**
	 * <p>Sets up the current GRB model.</p>
	 */
	private void setUpGRBmodel() throws GRBException {
		// create the GRBmodel
		env = new GRBEnv();
		grbmodel = new GRBModel(env);

		// lecturerLists stores list of vars that correspond to a given lecturer 
		lecturerLists = new ArrayList<ArrayList<IP_model_unit>>(); 
		for (int z = 0; z < numLecturers; z++) {
			lecturerLists.add(new ArrayList<IP_model_unit>());
		}

		// projectLists stores list of vars that correspond to a given project 
		projectLists = new ArrayList<ArrayList<GRBVar>>(); 
		for (int x = 0; x < numProjects; x++) {
			projectLists.add(new ArrayList<GRBVar>());
		}

		// rankList stores list of vars that correspond to a given rank 
		rankLists = new ArrayList<ArrayList<GRBVar>>(); 
		for (int x = 0; x < numProjects+1; x++) {
			rankLists.add(new ArrayList<GRBVar>());
		}

		// set up the unitArray and add to project and lecturer lists as go along
		unitArray = new IP_model_unit[numStudents][];

		// for each student
		for (int x = 0; x < numStudents; x++) {
			int[] prefList = m.studentPrefArray[x];
			IP_model_unit[] unitArrayRow = new IP_model_unit[prefList.length];

			// for each student project pair
			for (int i = 0; i < prefList.length; i++) {

				// create a new unit for each student-project pair
				unitArrayRow[i] = new IP_model_unit(grbmodel, m.studentPrefArray[x][i], m.studentPrefRankArray[x][i], "[" + x + "][" + i + "]");

				// add this GRB var to the arraylist which holds variables of this rank
				int rank = m.studentPrefRankArray[x][i]; ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				ArrayList<GRBVar> rankList = rankLists.get(rank - 1);
				rankList.add(unitArrayRow[i].studentPrefVar);

				// add the variable to the appropriate project list 
				int projNo = prefList[i];
				ArrayList<GRBVar> projList = projectLists.get(projNo);
				projList.add(unitArrayRow[i].studentPrefVar);

				// add the variable to the appropriate lecturer list 
				int currentLec = m.projLecturers[projNo];
				ArrayList<IP_model_unit> lecList = lecturerLists.get(currentLec);
				lecList.add(unitArrayRow[i]);
			}
			unitArray[x] = unitArrayRow;
		}

		// Integrate variables into model
		grbmodel.update();
	}


	/**
	 * <p>Adds upper and lower quota constraints to projects and lecturers, and student upper quota.</p>
	 */
	private void upperLowerConstraints() throws GRBException {
		// ----------------------------------------------------------------------------------------
		// each student is matched to 1 or less projects
		for (int x = 0; x < numStudents; x++) {
			// get linear expression for the sum of all variables for a student
			GRBLinExpr sumVarsForStudent = new GRBLinExpr();
			for (int y = 0; y < unitArray[x].length; y++) {
				sumVarsForStudent.addTerm(1, unitArray[x][y].studentPrefVar);
			}
			// each student is matched to 1 or less projects
			grbmodel.addConstr(sumVarsForStudent, GRB.LESS_EQUAL, 1.0, "ConstraintStudent" + x);
		}

		// ----------------------------------------------------------------------------------------
		// for each project
		for (int y = 0; y < numProjects; y++) {
			// get linear expressions for the sum of variables for this project
			ArrayList<GRBVar> projList = projectLists.get(y);
			GRBLinExpr numStudentsForProj = new GRBLinExpr();
			for (int p = 0; p < projList.size(); p++) {
				numStudentsForProj.addTerm(1, projList.get(p));
			}

			// The number of students a project has must be greater than or equal to the lower quota
			grbmodel.addConstr(numStudentsForProj, GRB.GREATER_EQUAL, (double) m.projectLowerQuotas[y], "ConstraintProjectLQ" + y);

			// The number of students a project has must be less than or equal to the max capacity
			grbmodel.addConstr(numStudentsForProj, GRB.LESS_EQUAL, (double) m.projUpperQuotas[y], "ConstraintProjectUQ" + y);
		}

		// ----------------------------------------------------------------------------------------
		// for each lecturer 
		for (int z = 0; z < numLecturers; z++) {
			// get a linear expression for the sum of variables for this lecturer
			ArrayList<IP_model_unit> lecList = lecturerLists.get(z);
			GRBLinExpr numStudentsForLect = new GRBLinExpr();
			for (int var = 0; var < lecList.size(); var++) {  
				numStudentsForLect.addTerm(1, lecList.get(var).studentPrefVar);
			}

			// The number of students a lecturer has must be greater than or equal to the lower quota
			grbmodel.addConstr(numStudentsForLect, GRB.GREATER_EQUAL, (double) m.lecturerLowerQuotas[z], "ConstraintLecturerLQ" + z);

			// The number of students a lecturer has must be less than or equal to the max capacity
			grbmodel.addConstr(numStudentsForLect, GRB.LESS_EQUAL, (double) m.lecturerUpperQuotas[z], "ConstraintLecturerUQ" + z);
		}
	}


	/**
	 * <p>Adds stability constraints to model. We disallow blocking pairs by adding constraints.</p>
	 */
	private void stabilityConstraints() throws GRBException {
		// for each student project pair
		for (int x = 0; x < numStudents; x++) {
			int stPrefLength = unitArray[x].length;
			for (int prefInd = 0; prefInd < stPrefLength; prefInd++) {

				// handy links to the project, lecturer and student project unit are saved
				int y = m.studentPrefArray[x][prefInd];
				int z = m.projLecturers[m.studentPrefArray[x][prefInd]];
				IP_model_unit unit = unitArray[x][prefInd];


				// --------------------------------------------------------------------------------------
				// studentWantsToMoveExpr = 1 if either s_i is unassigned in M, or s_i prefers p_j to M(s_i)
				// , that is if this student wants to move to this project 

				// for each student project pair, add up all the variables at an equal or higher rank in 
				// the preference list and set studentWantsToMoveExpr = 1 - sum
				unit.studentWantsToMoveExpr = new GRBLinExpr();
				unit.studentWantsToMoveExpr.addConstant(1);

				int index = 0;
				int currentRank = unitArray[x][index].rank;
				int aimRank = unit.rank;

				// while the current rank is less than the aim rank and we have not reached the end of the pref list
				while(currentRank <= aimRank && index < stPrefLength) {
					// add the variable 
					unit.studentWantsToMoveExpr.addTerm(-1, unitArray[x][index].studentPrefVar);
					index++;

					// update the current rank
					if (index != stPrefLength) {
						currentRank = unitArray[x][index].rank;
					}
				}


				// --------------------------------------------------------------------------------------
				// sumBetterEqualPairs = d_k IF l_k prefers (or is indifferent to) the worst student in M(l_k) to s_i 
				// AND s_i not in M(l_k)
				// sumBetterPairsPj = c_j IF l_k prefers (or is indifferent to) the worst student of p_j to s_i
				GRBLinExpr sumBetterEqualPairs = new GRBLinExpr();
				GRBLinExpr sumBetterPairsPj = new GRBLinExpr();

				// for each student in the lecturers preference list
				int aimRank2 = 0; 
				boolean foundAim = false;
				int lecPrefLength = m.lecturerPrefArray[z].length;
				for (int lecIndex = 0; lecIndex < lecPrefLength; lecIndex++) {
					int lecStudent = m.lecturerPrefArray[z][lecIndex];

					// if the student at index lecIndex is s_i student, then set the rank beyond which
					// will not be considered
					if (lecStudent == x) {
						foundAim = true;
						aimRank2 = m.lecturerPrefRankArray[z][lecIndex];
					}

					// if this student is a candidate, then iterate over its preference list
					if (foundAim == false || (foundAim == true && aimRank2 == m.lecturerPrefRankArray[z][lecIndex])) {
						for (int pIndex = 0; pIndex < unitArray[lecStudent].length; pIndex++) {
							// retrieve the unit to test
							IP_model_unit testUnit = unitArray[lecStudent][pIndex];

							// part of 3b)
							if (m.lecturersProjs.get(z).contains(testUnit.proj) && lecStudent != x) {
								sumBetterEqualPairs.addTerm(1, testUnit.studentPrefVar); 
							}

							// part of 3c)
							if (testUnit.proj == m.studentPrefArray[x][prefInd]) {
								sumBetterPairsPj.addTerm(1, testUnit.studentPrefVar);
							}
						}
					}
				}


				// alpha = 1 IMPLIES THAT l_k is full and prefers their worst assignee to s_i or is indifferent
				// between them AND s_i is not in M(l_k)
				GRBLinExpr alphaLinExpr = new GRBLinExpr();
				int negLecUQ = m.lecturerUpperQuotas[z] - 2 * m.lecturerUpperQuotas[z];
				alphaLinExpr.addTerm(negLecUQ, unit.alpha);
				alphaLinExpr.add(sumBetterEqualPairs);
				grbmodel.addConstr(alphaLinExpr, GRB.GREATER_EQUAL, 0, "alphaLinExpr[" + x + "][" + prefInd + "]");

				// beta = 1 IMPLIES THAT p_j is full and l_k prefers their worst assignee of p_j to s_i or is 
				// indifferent between them
				GRBLinExpr betaLinExpr = new GRBLinExpr();
				int negProjUQ = m.projUpperQuotas[y] - 2 * m.projUpperQuotas[y];
				betaLinExpr.addTerm(negProjUQ, unit.beta);
				betaLinExpr.add(sumBetterPairsPj);
				grbmodel.addConstr(betaLinExpr, GRB.GREATER_EQUAL, 0, "betaLinExpr[" + x + "][" + prefInd + "]");

				// If s_i is unmatched or prefers p_j to M(s_i) then alpha or beta must equal 1
				GRBLinExpr gammaLinExpr = new GRBLinExpr();
				gammaLinExpr.add(unit.studentWantsToMoveExpr);
				gammaLinExpr.addTerm(-1, unit.alpha);
				gammaLinExpr.addTerm(-1, unit.beta);
				grbmodel.addConstr(gammaLinExpr, GRB.LESS_EQUAL, 0, "gammaLinExpr[" + x + "][" + prefInd + "]");                
			}
		}
	}


	/**
	 * <p>Optimises on the maximum size and adds relevant constraint.</p>
	 */
	public void addMaxSizeConstraint() throws GRBException {
		GRBLinExpr sumAllVariables = new GRBLinExpr();
		for (int x = 0; x < numStudents; x++) {
			for (int projInd = 0; projInd < unitArray[x].length; projInd++) {
				sumAllVariables.addTerm(1, unitArray[x][projInd].studentPrefVar);
			}
		}

		grbmodel.setObjective(sumAllVariables, GRB.MAXIMIZE);
		grbmodel.optimize();
		int status = grbmodel.get(GRB.IntAttr.Status); 

		// if there is a feasible solution
		if (status == GRB.Status.OPTIMAL) {
			maxSize = grbmodel.get(GRB.DoubleAttr.ObjVal);
		}
	}


	/**
	 * <p>Optimises on the minimum size and adds relevant constraint.</p>
	 */
	public void addMinSizeConstraint() throws GRBException {
		GRBLinExpr sumAllVariables = new GRBLinExpr();
		for (int x = 0; x < numStudents; x++) {
			for (int projInd = 0; projInd < unitArray[x].length; projInd++) {
				sumAllVariables.addTerm(1, unitArray[x][projInd].studentPrefVar);
			}
		}

		grbmodel.setObjective(sumAllVariables, GRB.MINIMIZE);
		grbmodel.optimize();
		int status = grbmodel.get(GRB.IntAttr.Status); 

		// if there is a feasible solution
		if (status == GRB.Status.OPTIMAL) {
			minSize = grbmodel.get(GRB.DoubleAttr.ObjVal);
		}
	}


	/**
	 * <p> Prints the variable values after an optimisation to stdout. </p>
	 */
	public void printResults() throws GRBException {
		// variables for printing
		int numberOfAttributes = 3;
		String[] attNames = {"studentPrefVar", "alpha", "beta"};

		// attributes
		GRBVar[][] studentPrefArray = new GRBVar[numStudents][];
		GRBVar[][] alphaArray = new GRBVar[numStudents][];
		GRBVar[][] betaArray = new GRBVar[numStudents][];
		GRBLinExpr[][] studentWantsToMoveArray = new GRBLinExpr[numStudents][];

		// collect attribute values 
		// for each student
		for (int x = 0; x < numStudents; x++) {
			int lengthStPref = unitArray[x].length;

			// initialise this students preference list for all attributes
			studentPrefArray[x] = new GRBVar[lengthStPref];
			alphaArray[x] = new GRBVar[lengthStPref];
			betaArray[x] = new GRBVar[lengthStPref];
			studentWantsToMoveArray[x] = new GRBLinExpr[lengthStPref];

			// set the values for all attributes in the preference list
			for (int prefInd = 0; prefInd < unitArray[x].length; prefInd++) {
				studentPrefArray[x][prefInd] = unitArray[x][prefInd].studentPrefVar;
				alphaArray[x][prefInd] = unitArray[x][prefInd].alpha;
				betaArray[x][prefInd] = unitArray[x][prefInd].beta;
				studentWantsToMoveArray[x][prefInd] = unitArray[x][prefInd].studentWantsToMoveExpr;
			}
		}

		// print the attributes
		System.out.println();
		printGRBVarArray(studentPrefArray, "assignment");
		printGRBVarArray(alphaArray, "alpha variables");
		printGRBVarArray(betaArray, "beta variables");
		printGRBLinExpArray(studentWantsToMoveArray, "studentWantsToMove linear expression");
	}


	/**
	 * <p> Print an int[][]. </p>
	 * @param intArray
	 * @param message
	 */
	private void printIntArray(int[][] intArray, String message) {
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
	 * <p>Method to print a 1D array of GRB variables.</p>
	 * @Param array      the array of GRBVars
	 * @Param message    name of the array
	 */
	public void printGRBVarArray(GRBVar[] array, String message) throws GRBException {
		double[] arrayd = grbmodel.get(GRB.DoubleAttr.X, array);
		System.out.println(message + " variables");
		for (int x = 0; x < array.length; x++) {
			//if (arrayd[x] > 0.5) {   
			System.out.print(arrayd[x] + " ");
			//}
			//else {
			//     System.out.print("0");
			//}
		}
		System.out.println();
		System.out.println();
	}


	/**
	 * <p>Method to print a 2D array of GRB integer variables which may contain non 0/1 values.</p>
	 * @Param array      the 2D array of GRBVars
	 * @Param message    name of the array
	 */
	public void printGRBVarArray(GRBVar[][] array, String message) throws GRBException {
		System.out.println(message);

		// for each student
		for (int x = 0; x < numStudents; x++) {

			// for each index in the students preference list
			for (int prefInd = 0; prefInd < array[x].length; prefInd++) {
				double resultPref = array[x][prefInd].get(GRB.DoubleAttr.X);
				boolean found = false;
				int val = -1;       // if the result is negative then val = -1
				while(!found) {
					if (resultPref < val + 0.5) {
						System.out.print(val);
						found = true;
					}
					val++;
				}
			}
			System.out.println();
		}
		System.out.println();
	}


	/**
	 * <p>Method to print a 2D array of GRB integer Linear Expressions which may contain non 0/1 values.</p>
	 * @Param array      the 2D array of GRBVars
	 * @Param message    name of the array
	 */
	public void printGRBLinExpArray(GRBLinExpr[][] array, String message) throws GRBException {
		System.out.println(message);

		// for each student
		for (int x = 0; x < numStudents; x++) {

			// for each index in the students preference list
			for (int prefInd = 0; prefInd < unitArray[x].length; prefInd++) {
				double resultPref = array[x][prefInd].getValue();
				boolean found = false;
				int val = -1;       // if the result is negative then val = -1
				while(!found) {
					if (resultPref < val + 0.5) {
						System.out.print(val);
						found = true;
					}
					val++;
				}
			}
			System.out.println();
		}
		System.out.println();
	}


	/**
	 * <p>Set the current student assignments in the model.</p>
	 */
	public void setStudentAssignments() throws GRBException {
		// ready to save the assigned students to the studentAssignments array in the model
		int[] studentAssignments = m.studentAssignments;
		for (int x = 0; x < numStudents; x++) {
			studentAssignments[x] = -1;
		}

		// set the student assignments
		for (int x = 0; x < numStudents; x++) {
			int prefLength = unitArray[x].length;
			boolean matched = false;
			for (int prefInd = 0; prefInd < prefLength; prefInd++) {
				double resultPref = unitArray[x][prefInd].studentPrefVar.get(GRB.DoubleAttr.X);
				if (resultPref > 0.5) {
					studentAssignments[x] = m.studentPrefArray[x][prefInd];   
					matched = true;   
				}
			}
			if (!matched) {
				studentAssignments[x] = -1;
			}
		}
	}
}
