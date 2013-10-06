/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOUSE. See the GNU
 * General Public License for more details.
 *
 * You should have recieved a copy of the GNU General Public License
 * along with this program; if not write to the Free Software
 * Foundation, inc., 59 Temple Place, Suite 330, Boston MA 02111-1307
 * USA
 */

package run;

import uka.karmi.rmi.RemoteException;
import jp.lang.*;
import jp.sync.Barrier;
import jp.sync.BarrierFactory;

/**
 * This is the main program. The only thing that happens here is that an
 * instance of the program is started. This happens in the main() method Notes
 * on how to make this work under Linux..... (after much frustration) 1. Export
 * the classes as a directory, selecting all referenced classes and resources in
 * the dialog panel. 2. Go to the directory (example, you exported to
 * /home/myuser/test), then cd /home/myuser/test 3. If you have exported class
 * files, then write java run.Impact (note the dot) and the program should run.
 * 4. If you have exported java files, then make sure there are no class files
 * in the directory by writing rm run/.class 5. Compile the program by writing
 * javac run/ 6. go to 3.
 * 
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 */
public class Impact {
	/**
	 * Main process. This is the essence of the program. Here, an instance of
	 * the core is created and told to go through the different steps required
	 * to complete an analysis. Creation date: (2001-08-20 21:48:22)
	 * 
	 * @param args
	 *            java.lang.String[]
	 */
	public static void main(java.lang.String[] args) {

		System.out.println("\n=========== Welcome to Impact ==========");
		// System.out.println("= Impact is Alpha code. This means the =");
		// System.out.println("= results are not validated and should =");
		// System.out.println("= not be trusted for use.              =");
		// System.out.println("=                                      =");
		System.out.println("= Impact is free software and licensed =");
		System.out.println("= under GPL. Please note that it comes =");
		System.out.println("= with no warranties whatsoever.       =");
		System.out.println("=                                      =");
		System.out.println("= Report any bugs at our webpage:      =");
		System.out.println("= http://impact.sourceforge.net        =");
		System.out.println("=============== ENJOY!!!! ==============\n\n");

		int nr_CPU = DistributedRuntime.getMachineCnt();

		// Check if something is written after the command
		if (args.length == 1) {

			if (nr_CPU > 0)
				try {
					Impact.run_cluster(args[0], nr_CPU);
				} catch (Error e) {
					e.printStackTrace();
					return;
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			else {
				// A standard smp version is run -> make nr_CPU positive
				// nr_CPU = 1; (old)
				nr_CPU = Runtime.getRuntime().availableProcessors();
				try {
					Impact.run_smp(args[0], nr_CPU);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
		} else {
			System.out.println("Impact - A simple explicit dynamic program\n");
			System.out
					.println("Syntax: java run.Impact sourcefile.in  to start solver");
		}
		System.exit(0);
	}

	public static void run_smp(String fname, int nr_CPU)
			throws RemoteException, Exception {

		System.out.println("*******************************");
		System.out.println("*** Solver has been invoked ***");
		System.out.println("*******************************\n\n");
		System.out.println("Running on " + nr_CPU + " clients\n\n");

		// Create an instance of the solver
		ModelSmp node = new ModelSmp(nr_CPU, fname);

		// Since this is only a single instance, just run and wait for
		// completion
		node.run();

		// Solution completed successfully
		System.out.println("*** Solution completed ***");

	}

	public static void run_cluster(final String fname, int nr_CPU)
			throws Exception {
		final ModelCluster[] nodes;
		final Barrier barrier;

		System.out.println("********************************");
		System.out.println("*** Cluster has been invoked ***");
		System.out.println("********************************\n\n");
		System.out.println("Running on " + nr_CPU + " clients\n\n");

		nodes = new ModelCluster[nr_CPU];
		barrier = BarrierFactory.createBarrier(nr_CPU);

		System.out.println("*** Creating cluster processes ***");

		for (int n = 0; n < nr_CPU; n++) {
			/**
			 * @at n
			 */
			nodes[n] = new ModelCluster(nr_CPU, n, fname, barrier);
		}

		for (int n = 0; n < nr_CPU; n++)
			nodes[n].setCluster_nodes(nodes);

		// Create threads
		Thread[] tsolve = new Thread[nr_CPU];

		for (int n = 0; n < tsolve.length; n++) {
			tsolve[n] = new Thread(nodes[n]);
			tsolve[n].setPriority(Thread.MIN_PRIORITY);
		}

		// Start threads
		System.out.println("*** Starting the solution ***");

		for (int n = 0; n < tsolve.length; n++) {
			tsolve[n].start();
		}

		long stime = System.currentTimeMillis();

		// wait for worker threads to terminate
		for (int n = 0; n < tsolve.length; n++) {
			try {
				tsolve[n].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}

		// Solution completed successfully
		System.out.println("Solving took "
				+ (System.currentTimeMillis() - stime) + " ms");
	}

}
