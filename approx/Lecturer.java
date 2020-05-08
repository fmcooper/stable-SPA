package approx;
import java.util.ArrayList;
import shared_resources.*;

/*
* <p>Lecturer class. Represents a lecturer agent.</p>
*
* @author Frances
*/
public class Lecturer {
	/* <p>The index of this lecturer.</p> */
	int lInd;
	/* <p>The number of this lecturer.</p> */
	int lNum;
	/* <p>This lecturers preference list.</p> */
	public Student[] prefList;
	/* <p>The ranks of this lecturers preference list.</p> */
	private int[] ranks;
	/* <p>A list of this lecturers projects.</p> */
	public ArrayList<Project> projects;
	/* <p>The current number of assigned students to projects of this lecturer.</p> */
	int numAssigned;
	/* <p>The upper quota of this lecturer.</p> */
	int upperQ;


	/*
	* <p>Constructor. Creates a lecturer object.</p>
	*/
	public Lecturer(int num, Student[] prefList, int[] ranks, int upperQ) {
		numAssigned = 0;
		projects = new ArrayList<Project>();
		lInd = num;
		lNum = lInd + 1;
		this.upperQ = upperQ;

		this.prefList = prefList;
		this.ranks = ranks;
	}


	/*
	* <p>Attaches a project to this lecturer.</p>
	*/
	public void attachProj(Project p) {
		projects.add(p);
	}


	/*
	* <p>Returns whether the given student is meta-preferred to any of the lecturers students.</p>
	*/
	public Student metaPrefers(Student s) {
		return metaPrefers(s, null);
	}


	/*
	* <p> Lecturer l is wobble with respect to s if there is another student assigned to l at the same rank.</p>
	*/
	public boolean wobbly(Student s) {
		ArrayList<Student> assignedStudents = new ArrayList<Student>();
		for (int i = 0; i < projects.size(); i++) {
			assignedStudents.addAll(projects.get(i).students);
		}

		int rankOrig = getRank(s);

		for (Student testerSt : assignedStudents) {
			if (rankOrig == getRank(testerSt) && s != testerSt) {
				return true;
			}
		}
		return false;
	}


	/*
	* <p>Returns whether this lecturer is precarious.</p>
	*/
	public boolean precarious() {
		for (int i = 0; i < projects.size(); i++) {
			if (projects.get(i).precarious()) {
				return true;
			}
		}
		return false;
	}


	/*
	* <p>Returns whether the given student is meta-preferred to any of the lecturers students. If a project is provided then only students associated with this project are considered.</p>
	* <p>Lecturer l meta-prefers s1 to s2 if either rank(s1) < rank(s2) or rank(s1) = rank(s2) but s1 is in phase 2 whereas s2 is not.
	*/
	public Student metaPrefers(Student s, Project p) {
		ArrayList<Student> assignedStudents = new ArrayList<Student>();

		if (p != null) {
			assignedStudents.addAll(p.students);
		}
		else {
			for (int i = 0; i < projects.size(); i++) {
				assignedStudents.addAll(projects.get(i).students);
			}
		}

		// obtain a list of the worst ranked students 
		int rankOfWorst = -1;
		ArrayList<Student> worstStudents = new ArrayList<Student>();
		for (int i = 0; i < assignedStudents.size(); i++) {
			Student tempS = assignedStudents.get(i);
			int rank = getRank(tempS);
			if (rank > rankOfWorst) {
				rankOfWorst = rank;
				worstStudents = new ArrayList<Student>();
				worstStudents.add(tempS);
			}
			else if (rank == rankOfWorst) {
				worstStudents.add(tempS);
			}
				// else do nothing
		}

		// compare rank of the worst and given student
		int rankOfS = getRank(s);
		if (rankOfS > rankOfWorst) {
			return null;
		}
		else if (rankOfS < rankOfWorst) {
			return worstStudents.get(0);
		}
		else {
			if (worstStudents.contains(s)) {
				return s;
			}

			// if this student is in phase 2
			if (s.phase == 2) {
				boolean worstInPhase1 = false;
				for (Student st : worstStudents) {
					if (st.phase != 2) {
						return st;
					}
				}
			}

		}
		return null;
	}


	/*
	* <p>Returns the rank of the given studentIndex on this lecturers preference list.</p>
	*/
	public int getRank(Student s) {
		int sInd = s.sInd;
		int rank = -1;
		for (int i = 0; i < prefList.length; i++) {
			if (prefList[i].sInd == sInd) {
				rank = ranks[i];
			}
		}
		return rank;
	}


	/*
	* <p> Returns a precarious student assigned to a project of this lecturer. </p>
	*/
	public Student getPrecarious() {
		for (int i = 0; i < projects.size(); i++) {
			if (projects.get(i).precarious()) {
				Student precSt = projects.get(i).getPrecarious();
				if (precSt != null) {
					return precSt;
				}
			}
		}
		return null;
	}


	/*
	* <p>Prints this lecturer.</p>
	*/
	public void print() {
		String s = "lec: " + lInd + " precarious: " + precarious();
		s += " projects: ";
		for (int i = 0; i < projects.size(); i++) {
			s += projects.get(i).pInd + " ";
		}
		s += " PL: < ";
		for (Student st : prefList) {
			s += st.sInd + " ";
		}
		s += "> Ranks: < ";
		for (int num : ranks) {
			s += num + " ";
		}		
		s += ">";
		System.out.println(s);
	}
}