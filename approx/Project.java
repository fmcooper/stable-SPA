package approx;
import java.util.ArrayList;
import shared_resources.*;

/*
* <p>Project class. Represents a project agent.</p>
*
* @author Frances
*/

public class Project {
	/* <p>The index of this project.</p> */ 
	int pInd;
	/* <p>Lecturer offering this project.</p> */ 
	Lecturer lec;
	/* <p>A list of students assigned to this project.</p> */ 
	ArrayList<Student> students;
	/* <p>The upper quota for this project.</p> */ 
	int upperQ;
	/* <p>The current number of stududents assigned to this project.</p> */ 
	int numAssigned;


	/*
	* <p>Constructor. Creates a project object.</p>
	*/
	public Project(int i, int upperQ) {
		int numAssigned = 0;
		this.upperQ = upperQ;
		pInd = i;
		students = new ArrayList<Student>();
	}


	/*
	* <p>Returns whether a project is precarious.</p>
	*/
	public boolean precarious() {
		for (int i = 0; i < students.size(); i++) {
			if (students.get(i).isPrecarious()) {
				return true;
			}
		}
		return false;
	}


	/*
	* <p>Retrieves a precarious student assigned to this project.</p>
	*/
	public Student getPrecarious() {
		for (int j = 0; j < students.size(); j++) {
			//test whether this student is precarious
			if (students.get(j).isPrecarious()) {
				return students.get(j);
			}
		}

		return null;
	}


	/*
	* <p>Adds a student to this project.</p>
	*/
	public void addStudent(Student s) {
		int pnum = pInd + 1;
		int snum = s.sInd + 1;
		//System.err.println("student " + snum + " adds project " + pnum);
		students.add(s);
		s.assigned(this);
		lec.numAssigned++;
		numAssigned++;
	}


	/*
	* <p>Removes a student from this project.</p>
	*/
	public void removeStudent(Student s) {
		int pnum = pInd + 1;
		int snum = s.sInd + 1;
		//System.err.println("student " + snum + " removes project " + pnum);
		students.remove(s);
		s.unassigned();
		lec.numAssigned--;
		numAssigned--;
	}


	/*
	* <p>Attaches the lecturer offering this project.</p>
	*/
	public void attachLec(Lecturer l) {	
		lec = l;
	}


	/*
	* <p>Returns if this project is fully avaliable.</p>
	*/
	public boolean isFullyAvaliable() {
		if (numAssigned < upperQ && lec.numAssigned < lec.upperQ) {
			return true;
		}
		else {
			return false;
		}
	}


	/*
	* <p>Print this project.</p>
	*/
	public void print() {
		System.out.println("proj: " + pInd + " fullyAvaliable: " + isFullyAvaliable() + " precarious: " + precarious() + " lec: " + lec.lInd);
	}
}
