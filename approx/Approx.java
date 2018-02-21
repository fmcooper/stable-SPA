package approx;
import java.util.Random;
import java.util.ArrayList;
import shared_resources.*;

/**
 * <p> Implementation of the 3/2 approximation algorithm for maximum stable matching in SPA-ST" </p>
 * 
 * @author Frances
 *
 */
public class Approx {
	/** <p>The model instance.</p> */
	private Model model;
	/** <p>List of all students.</p> */
	Student[] students;
	/** <p>List of all projects.</p> */
	Project[] projects;
	/** <p>List of all lecturers.</p> */
	Lecturer[] lecturers;

	/**
	 * <p> Constructor for the algorithm - runs the algorithm. </p>
	 * 
	 * @param instanceModel
	 */
	public Approx(Model model) {
		this.model = model;

		run();
	}


	/**
	 * <p> The 3/2 approximation algorithm. <\p>
	 * 
	 */
	public void run() {
		// create projects
		projects = new Project[model.numProjects];
		for (int i = 0; i < projects.length; i++) {
			projects[i] = new Project(i, model.projUpperQuotas[i]);
		}

		// create students
		students = new Student[model.numStudents];
		for (int i = 0; i < students.length; i++) {

			// create list of projects
			Project[] prefListP = new Project[model.studentPrefArray[i].length];
			for (int j = 0; j < prefListP.length; j++) {
				prefListP[j] = projects[model.studentPrefArray[i][j]];
			}

			students[i] = new Student(i, prefListP, model.studentPrefRankArray[i]);
		}

		// create lecturers
		lecturers = new Lecturer[model.numLecturers];
		for (int i = 0; i < lecturers.length; i++) {

			// create list of students
			Student[] prefListL = new Student[model.lecturerPrefArray[i].length];
			for (int j = 0; j < prefListL.length; j++) {
				prefListL[j] = students[model.lecturerPrefArray[i][j]];
			}
			lecturers[i] = new Lecturer(i, prefListL, model.lecturerPrefRankArray[i], model.lecturerUpperQuotas[i]);
		}

		// attach each project to their lecturer and vice versa
		for (int i = 0; i < model.projLecturers.length; i++) {
			Project proj = projects[i];
			Lecturer lec = lecturers[model.projLecturers[i]];
			proj.attachLec(lec);
			lec.attachProj(proj);
		}		

		ArrayList<Student> phase1or2Avaliable = new ArrayList<Student>();
		for (int i = 0; i < students.length; i++) {
			phase1or2Avaliable.add(students[i]);
		}
		int numAvaliable = model.numStudents;

		/////////////////////////////////
		// Algorithm
		/////////////////////////////////

		// while there are still avaliable students in phases 1 or 2
		while (phase1or2Avaliable.size() != 0) {

			Student s = phase1or2Avaliable.get(0);
			// find one of the students favourite project
			int pInd = s.getFavourite(projects);

			//System.out.println("testing s: " + s.sInd + " p: " + pInd + " rank: " + s.getRank(projects[pInd]));
			Project p = projects[pInd];
			int lInd = model.projLecturers[pInd];
			Lecturer l = lecturers[lInd];

			// if p is fully avaliable
			if (p.isFullyAvaliable()) {
				p.addStudent(s);
				phase1or2Avaliable.remove(phase1or2Avaliable.indexOf(s));
			}

			// else if p is undersubscribed, q is full and (q is precarious or q meta-prefers s to a worst assignee)
			else if ((p.numAssigned < p.upperQ) && (l.numAssigned == l.upperQ) && (l.precarious() || (l.metaPrefers(s) != null))) {
				
				//System.out.println(p.numAssigned);
				Student stReplaceMe = null;

				// if l is precarious
				if (l.precarious()) {
					stReplaceMe = l.getPrecarious();
				}
				// else if l is not precarious
				else {
					stReplaceMe = l.metaPrefers(s);
					stReplaceMe.removePref(stReplaceMe.proj);
				}

				// remove worst student from whoever they are attached to
				Project replaceP = stReplaceMe.proj;
				replaceP.removeStudent(stReplaceMe);

				// add new student to p
				p.addStudent(s);

				phase1or2Avaliable.remove(phase1or2Avaliable.indexOf(s));
				if (stReplaceMe.phase != 3) {
					phase1or2Avaliable.add(stReplaceMe);
				}
			}

			// else if p is full and (p is precarious or q meta-prefers s to a worst assignee assigned to p)
			else if ((p.numAssigned == p.upperQ) && (p.precarious() || l.metaPrefers(s, p) != null)) {

				Student stReplaceMe = null;

				// if p is precarious
				if (p.precarious()) {
					stReplaceMe = p.getPrecarious();
				}
				// else if p is not precarious
				else {
					stReplaceMe = l.metaPrefers(s, p);
					stReplaceMe.removePref(stReplaceMe.proj);
				}

				// remove worst student from whoever they are attached to
				Project replaceP = stReplaceMe.proj;
				replaceP.removeStudent(stReplaceMe);

				// add new student to p
				p.addStudent(s);

				phase1or2Avaliable.remove(phase1or2Avaliable.indexOf(s));
				if (stReplaceMe.phase != 3) {
					phase1or2Avaliable.add(stReplaceMe);
				}
			}

			// else remove the preference list value
			else {
				s.removePref(p);
				if (s.phase == 3) {
					phase1or2Avaliable.remove(phase1or2Avaliable.indexOf(s));
				}	
			}
		}

		// now all blocking pairs are accounted for except type 3bi
		// keep iterating over all the students until no more promotions can be made
		boolean finishedPromoting = false;
		while (!finishedPromoting) {

			boolean promoteInIteration = false;
			for (Student s : students) {
				// if the student is assigned a project then check whether they would prefer to be assigned 
				// another project by this lecturer and that the lecturer would be ok with this
				if (s.proj != null) {
					Project currentProj = s.proj;
					ArrayList<Project> ps = s.getPreferredProjs();
					int index = 0;
					boolean found = false;
					while(!found && index < ps.size()) {
						// if the project is not full and the lecturer is the same then swap over
						Project testerProj = ps.get(index);
						if (testerProj.numAssigned < testerProj.upperQ && testerProj.lec == currentProj.lec) {
							s.proj.removeStudent(s);
							testerProj.addStudent(s);
							found = true;
							promoteInIteration = true;
						}
						index++;
					}
				}
			}
			if (!promoteInIteration) {
				finishedPromoting = true;
			}
		}

		// set the student assignments in the model
		for (int i = 0; i < students.length; i++) {
			int finalProj = -1;
			if (students[i].proj != null) {
				finalProj = students[i].proj.pInd;
			}
			model.studentAssignments[i] = finalProj;
		}
	}
}
