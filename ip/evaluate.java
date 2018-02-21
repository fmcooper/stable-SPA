package ip;
import java.io.*;
import java.util.*;
import java.math.*;
import shared_resources.*;

/**
 *	<p> This class takes command line arguments of an input file, instance
 * parameters and optimisation choices and outputs results to terminal. </p>
 */
public class evaluate {
	/**<p>The model of the instance.</p>*/
	private static Model model;

	static BigInteger startTimeTot;

	/**
	 * <p> Main method. Processes a single file. </p>
	 */
	public static void main(String[] args) {
		startTimeTot = new BigInteger("" + System.currentTimeMillis());

		// input checks
		String input = "";
		boolean max = false;
		File f = null;
		try {
			input = args[0];
			if (!input.equals("-h")) {
				f = new File(input);
			}

			String maxOrMin = args[1];
			if ((maxOrMin.equals("-max") || maxOrMin.equals("-min")) && !maxOrMin.equals("-h")) {
				if (maxOrMin.equals("-max")) {
					max = true;
				}
			}
		}
		catch (Exception e) {
			System.out.println("Input error");
		}



		// set the time and date instance variable in the model
		BigInteger startTimeMod = new BigInteger("" + System.currentTimeMillis()); 
		model = Util_FileIO.readFile(f);
		Util_FileIO.createCal();
		String easyResults = Util_FileIO.getCal(false) + "\n";
		model.timeAndDate = easyResults;
		SPA_IP_HR MIP = new SPA_IP_HR(model, max); // IP model
		BigInteger endTimeMod = new BigInteger("" + System.currentTimeMillis());
		BigInteger timeTakenMod = endTimeMod.subtract(startTimeMod);

		BigInteger startTimeAlg = new BigInteger("" + System.currentTimeMillis());
		MIP.run();
		BigInteger endTimeAlg = new BigInteger("" + System.currentTimeMillis());
		BigInteger timeTakenAlg = endTimeAlg.subtract(startTimeAlg);

		if (model.feasible) {
			// read results and append to file
			BigInteger startTimeRes = new BigInteger("" + System.currentTimeMillis());
			model.setAssignmentInfo();
			BigInteger endTimeRes = new BigInteger("" + System.currentTimeMillis());
			BigInteger timeTakenRes = endTimeRes.subtract(startTimeRes);
			//System.out.println(model.getMatchingSize());

			System.out.println(model.getBriefStatsFeasible());
			System.out.println("Duration_ModCreation_milliseconds: " + timeTakenMod);
			System.out.println("Duration_GetSolution_milliseconds: " + timeTakenAlg);
			System.out.println("Duration_CollectRes_milliseconds: " + timeTakenRes + "\n");
			System.out.println("RawResults: \n" + model.getRawResults());		
		}


		else {
			System.out.println(model.getBriefStatsInfeasible());
			System.out.println("Duration_ModCreation_milliseconds: " + timeTakenMod);
			System.out.println("Duration_GetSolution_milliseconds: " + timeTakenAlg + "\n");
		}

		BigInteger endTimeTot = new BigInteger("" + System.currentTimeMillis());
		BigInteger timeTakenTot = endTimeTot.subtract(startTimeTot);
		System.out.println("Duration_Total_milliseconds: " + timeTakenTot + "\n");
	}
}

