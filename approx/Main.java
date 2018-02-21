package approx;
import java.io.*;
import java.util.*;
import java.math.*;
import shared_resources.*;

/**
*	This class runs the SPA-ST algorithm over a given file and outputs the result to stdout. </p>
*
* @author Frances
*/

public class Main {
	/* <p>Start time in milliseconds.</p> */
	static BigInteger startTimeTot;


	/**
	* <p> Help message. </p>
	*/
	public static void helpAndExit() {
		System.out.println();
		System.out.println("This class runs the 3/2 approximation algorithm to the maximum stable matching for instances of SPA-ST");
		System.out.println();
		System.out.println("Using this program:");
		System.out.println("$ java Main [-h] <file name>");
		System.out.println();
		System.exit(0);
	}


	/**
	* <p> Main method. Runs a single instance. </p>
	*/
	public static void main(String args[]) {
		startTimeTot = new BigInteger("" + System.currentTimeMillis());

		// input checks
		String input = "";
		File f = null;
		try {
			input = args[0];
			if (!input.equals("-h")) {
				f = new File(input);
			}
		}
		catch (Exception e) {
			System.out.println("Input name error");
			helpAndExit();
		}

		if (input.equals("-h")) {
			helpAndExit();
		}

		// create the model 
		BigInteger startTimeMod = new BigInteger("" + System.currentTimeMillis()); 
		Model model = Util_FileIO.readFile(f);
		// set the time and date instance variable in the model
		Util_FileIO.createCal();
		String easyResults = Util_FileIO.getCal(false) + "\n";
		model.timeAndDate = easyResults;

		if (model == null) {
			System.out.println("** error: the file " + f.getName() + " is incompatable");
			helpAndExit();
		}
		BigInteger endTimeMod = new BigInteger("" + System.currentTimeMillis());
		BigInteger timeTakenMod = endTimeMod.subtract(startTimeMod);

		// run the approximation algorithm recording start and end times
		BigInteger startTimeAlg = new BigInteger("" + System.currentTimeMillis());
		Approx alg = new Approx(model);
		BigInteger endTimeAlg = new BigInteger("" + System.currentTimeMillis());
		BigInteger timeTakenAlg = endTimeAlg.subtract(startTimeAlg);		

		// retrieve results
		BigInteger startTimeRes = new BigInteger("" + System.currentTimeMillis());
		model.setAssignmentInfo();
		BigInteger endTimeRes = new BigInteger("" + System.currentTimeMillis());
		BigInteger timeTakenRes = endTimeRes.subtract(startTimeRes);
		
		// output results
		System.out.println(model.getBriefStatsFeasible());
		System.out.println("Duration_ModCreation_milliseconds: " + timeTakenMod);
		System.out.println("Duration_GetSolution_milliseconds: " + timeTakenAlg);
		System.out.println("Duration_CollectRes_milliseconds: " + timeTakenRes + "\n");
		System.out.println("RawResults: \n" + model.getRawResults() + "\n");

		BigInteger endTimeTot = new BigInteger("" + System.currentTimeMillis());
		BigInteger timeTakenTot = endTimeTot.subtract(startTimeTot);
		System.out.println("Duration_Total_milliseconds: " + timeTakenTot + "\n");
	}	
}
