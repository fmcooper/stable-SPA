package approx;
import java.util.ArrayList;
import shared_resources.*;

/*
* <p>Student class. Represents a student agent.</p>
*
* @author Frances
*/
public class Student {
	/* <p>The current phase this student is in.</p> */
	int phase;
	/* <p>The index of this student.</p> */ 
	int sInd;
	/* <p>The number of this student.</p> */ 
	int sNum;
	/* <p>Preference list array - remains unchanged.</p> */
	private Project[] prefList;
	/* <p>Rank of preference list - remains unchanged.</p> */
	private int[] ranks;
	/* <p>Changing preference list.</p> */
	private ArrayList<Project> tempPrefList;
	/* <p>Changing ranks list.</p> */
	private ArrayList<Integer> tempRanks;
	/* <p>The index of the current favourite project in the changing pref list.</p> */
	int currentFAindex = -1;
	/* <p>The current project assigned to this student.</p> */
	Project proj;
	/* <p>The phase this student was assigned in.</p> */
	int assignedInPhase;


	/*
	* <p>Constructor. Creates a student object.</p>
	*/
	public Student(int num, Project[] prefList, int[] ranks) {
		assignedInPhase = 0;
		proj = null;
		phase = 1;
		sInd = num;
		sNum = sInd + 1;

		this.prefList = prefList;
		this.ranks = ranks;
		tempPrefList = new ArrayList<Project>();
		tempRanks = new ArrayList<Integer>();

		for (int i = 0; i < prefList.length; i++) {
			tempPrefList.add(prefList[i]);
			tempRanks.add(ranks[i]);
		}
	}


	/*
	* <p>Returns whether this student is part of a precarious pair.</p>
	*/
	public boolean isPrecarious() {
		// get the rank of the attached project on this students preference list.
		int rank = getRank(proj);

		// look at all other projects of the same rank
		ArrayList<Project> sameRankPs = new ArrayList<Project>();
		for (int i = 0; i < ranks.length; i++) {
			if (ranks[i] == rank) {
				sameRankPs.add(prefList[i]);
			}
		}

		// check whether there are otehr fully available projects at the same rank
		for (Project p : sameRankPs) {
			if (p != proj) {
				if (p.isFullyAvaliable()) {
					return true;
				}
			}
		}

		return false;
	}


	/*
	* <p>Returns the rank of the assigned project on this students preference list.</p>
	*/
	public int getRank(Project p) {
		for (int i = 0; i < prefList.length; i++) {
			if (prefList[i] == p) {
				return ranks[i];
			}
		}
		return -1;
	}


	/*
	* <p>Returns the phase of this student.</p>
	*/
	public int status() {
		if (proj == null) {
			return 1;			// student is avaliable
		}
		else if (assignedInPhase == 1) {
			return 2;
		}
		else {
			return 3;
		}
	}


	/*
	* <p>Returns whether this student is avaliable.</p>
	*/
	public boolean isAvaliable() {
		if (status() == 1) {
			return true;
		}
		return false;
	}


	/*
	* <p>Returns whether this student is provisionally assigned.</p>
	*/
	public boolean isProvisional() {
		if (status() == 2) {
			return true;
		}
		return false;
	}


	/*
	* <p>Returns whether this student is confirmed.</p>
	*/
	public boolean isConfirmed() {
		if (status() == 3) {
			return true;
		}
		return false;
	}


	/*
	* <p>Assigns this student to a given project.</p>
	*/
	public void assigned(Project p) {
		proj = p;
		assignedInPhase = phase;
	}


	/*
	* <p>Makes this student unassigned.</p>
	*/	
	public void unassigned() {
		proj = null;
	}


	/*
	* <p>Returns a list of this students preferred projects.</p>
	*/	
	public ArrayList<Project> getPreferredProjs() {
		if (proj == null) {
			return null;
		}
		else {
			int rank = getRank(proj);
			ArrayList<Project> betterProjs = new ArrayList<Project>();
			for (int i = 0; i < prefList.length; i++) {
				if (ranks[i] < rank) {
					betterProjs.add(prefList[i]);
				}
			}
			return betterProjs;
		}
	}


	/*
	* <p>Remove the given project from this students preference list.</p>
	*/
	public void removePref(Project p) {
		int index = tempPrefList.indexOf(p);
		tempPrefList.remove(index);
		tempRanks.remove(index);
		if (tempPrefList.size() == 0) {
			// if we have completed traversal through the preference list for the first time, 
			// then move to phase 2 and reinstate preference list and rank list
			if (phase == 1) {
				phase = 2;   // for testing whether matching is stable only with 1 round: phase = 3;
				for (int i = 0; i < prefList.length; i++) {
					tempPrefList.add(prefList[i]);
					tempRanks.add(ranks[i]);

				}
			}
			// otherwise we have traversed the preference list twice
			else {
				phase = 3;
			}
		}
	}


	/*
	* <p>Returns a favourite project of a student. The favourite project is defined as one of  
	* the highest ranking meta-preferred projects on the students list.</p>
	*/
	public int getFavourite(Project[] projects) {
		ArrayList<Integer> favouriteProjs = new ArrayList<Integer>();
		int currentRank = tempRanks.get(0);

		// iterate over the projects of highest rank
		int r = currentRank;
		int index = 0;

		while (r == currentRank && index < tempPrefList.size()) {

			int projInd = tempPrefList.get(index).pInd;

			// if a fullyAvaliable project is found, then return its index
			if (projects[projInd].isFullyAvaliable()) {
				currentFAindex = index;
				return projInd;
			}
			index++;

			if (index < tempPrefList.size()) {
				r = tempRanks.get(index);
			}
		}
		// if no fully Avaliable project was found then return the first
		currentFAindex = 0;
		return tempPrefList.get(0).pInd;
	}


	/*
	* <p>Print the student.</p>
	*/
	public void print() {
		String pinfo = "";
		if (proj == null) {
			pinfo = "null";
		}
		else {
			pinfo = "" + proj.pInd;
		}
		String s = "student " + sInd + " phase: " + phase + " status: " + status() + " proj: " + pinfo;
		s += " tempPL: < ";
		for (Project p : tempPrefList) {
			s += p.pInd + " ";
		}
		s += "> tempRanks: < ";
		for (Integer num : tempRanks) {
			s += num + " ";
		}		
		s += ">";
		System.out.println(s);
	}
}
