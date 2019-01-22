//*************************************************************************************
//*********************************************************************************** *
//author Aritra Dhar 																* *
//PhD Researcher																  	* *
//ETH Zurich													   				    * *
//Zurich, Switzerland															    * *
//--------------------------------------------------------------------------------- * * 
///////////////////////////////////////////////// 									* *
//This program is meant to do world domination... 									* *
///////////////////////////////////////////////// 									* *
//*********************************************************************************** *
//*************************************************************************************
package test;

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * @author Aritra
 *
 */
public class RandTest {

	public static void main(String[] args) {

		long start = System.nanoTime();
		int test = 1000;
		boolean notMatched = false;
		for(int i = 0; i < test; i++)
		{
			SecureRandom rand = new SecureRandom();
			byte[] seed = new byte[32];
			rand.nextBytes(seed);

			SecureRandom s1 = new SecureRandom(seed);
			SecureRandom s2 = new SecureRandom(seed);

			byte[] r1 = new byte[512];
			byte[] r2 = new byte[32];
			s1.nextBytes(r1);
			s2.nextBytes(r2);

			
			if(!Arrays.equals(r1, r2))
			{
				notMatched = true;
				break;
			}
			/*else
				System.out.println("Not");*/
			//System.out.println(Base64.getUrlEncoder().encodeToString(r1));
			//System.out.println(Base64.getUrlEncoder().encodeToString(r2));
		}
		long end = System.nanoTime();
		System.out.println(notMatched);
		System.out.println("Total time : " + (end - start) + " ns");
		System.out.println("Avg time : " + (double)((end - start) / test) + " ns");
	}
}
