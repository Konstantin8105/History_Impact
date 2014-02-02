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

/**
 * Topology optimization of structure.
 *
 * @author: Yuriy Mikhaylovskiy
 */

import java.io.*;
import java.util.*;

import j3d.*;
import gui.*;

public class ImpactOpt implements Runnable, ExceptionListener {
	public static final String ver = "Impact Topology optimization of structure (2006.03.11)";
	public byte steps = 20;
	public float ignore_stress_i = 35, ignore_strain_i = 35;
	public Hashtable stress_i = new Hashtable(), strain_i = new Hashtable();
	public String sourcefile;
	private _In infile;
	private float maxei = 0, maxsi = 0;
	private String[] args;
	private Set exception_listeners;

	public ImpactOpt() {
	}

	public ImpactOpt(String[] args) {
		this.args = args;
		exception_listeners = Collections.synchronizedSet(new HashSet());
	}

	public void run() {
		System.out.println("\n" + ver);
		System.out.println("Start topology optimization of structure.");
		if (args.length > 0) {
			try {
				parseArgs(args);
				show_parametrs();
				solve();
			} catch (Exception e) {
				if (e instanceof InterruptedException)
					System.out
							.println("*** Solution interrupted by user, ending ***");
				else {
					System.out
							.println("*** Error in optimization, terminating");
					e.printStackTrace();
					sendException(e); // Notify GUI
				}
			}
		} else
			show_syntax();
		System.out.println("\nStop topology optimization of structure.");
	}

	private void parseArgs(String[] args) {
		for (int i = 0; i < args.length; i++) {
			try {
				System.out.println("Parse command line switche [" + i + "]\t"
						+ args[i]);
				if (args[i].toUpperCase().endsWith(".IN"))
					sourcefile = args[i];
				else if (args[i].toUpperCase().startsWith("STEPS")) {
					StringTokenizer stt = new StringTokenizer(args[i], "=");
					stt.nextToken();
					steps = Byte.parseByte(stt.nextToken());
				} else if (args[i].toUpperCase().startsWith("IGNORE.STRESS_I")) {
					StringTokenizer stt = new StringTokenizer(args[i], "=");
					stt.nextToken();
					ignore_stress_i = Float.parseFloat(stt.nextToken());
				} else if (args[i].toUpperCase().startsWith("IGNORE.STRAIN_I")) {
					StringTokenizer stt = new StringTokenizer(args[i], "=");
					stt.nextToken();
					ignore_strain_i = Float.parseFloat(stt.nextToken());
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	private void show_syntax() {
		System.out.println("\nThe command line syntax to run " + ver + " is:");
		System.out
				.println("java run.ImpactOpt [steps=... ignore.stress_i=... ignore.strain_i=... ...] sourcefile.in");
		System.out.println("\nCommand Line Switches:");
		System.out.println("steps\t-\tSteps of optimization. Default " + steps
				+ ".");
		System.out.println("ignore.stress_i\t-\tDefault " + ignore_stress_i
				+ "% from the maximum value stress(i).");
		System.out.println("ignore.strain_i\t-\tDefault " + ignore_strain_i
				+ "% from the maximum value strain(i).");
	}

	private void show_parametrs() {
		System.out
				.println("\nInitial parameters for topology optimization of structure:");
		System.out.println("steps\t=\t" + steps);
		System.out.println("ignore.stress_i\t=\t" + ignore_stress_i);
		System.out.println("ignore.strain_i\t=\t" + ignore_strain_i);
	}

	public void solve() throws Exception {

		for (byte i = 0; i <= steps; i++) {
			System.gc();
			solve(i);

			if (Thread.currentThread().isInterrupted())
				break;

		}

	}

	/**
	 * This is where we will end up if the solution is interrupted or fails.
	 * 
	 */
	public void exceptionOccurred(Exception e, Object o) {

		// Pass this error on to the GUI
		sendException(e);

		// Terminate everything
		Thread.currentThread().interrupt();
	}

	/**
	 * Add exception listener to the object
	 * 
	 * @param l
	 *            The listener to add
	 */
	public void addExceptionListener(ExceptionListener l) {
		exception_listeners.add(l);
	}

	public void solve(byte step) {
		infile = null;
		ModelSmp I;
		int nr_CPU = Runtime.getRuntime().availableProcessors();

		if (step == 0) {
			System.out.println("\nTopology optimization of structure. Step = "
					+ step);
			System.out.println("Start solving problems .... ");
			show_parametrs();
			I = new ModelSmp(nr_CPU, sourcefile);
		} else {
			I = new ModelSmp(nr_CPU, opt(step));
		}

		I.addExceptionListener(this);
		I.run();
	}

	public static void main(String[] args) {

		ImpactOpt opt = new ImpactOpt(args) {
			private void sendException(Exception e) {
			}
		};
		opt.run();
		System.exit(0);
	}

	private String opt(byte step) {
		String[] args = { sourcefile.substring(0, sourcefile.length() - 3)
				+ "." + step + ".in" };
		String resfile, infile;
		maxei = 0;
		maxsi = 0;
		int id_node = 0;
		if (step == 1) {
			resfile = sourcefile + ".flavia.res";
			infile = sourcefile;
		} else {
			resfile = sourcefile.substring(0, sourcefile.length() - 3) + "."
					+ (step - 1) + ".in.flavia.res";
			infile = sourcefile.substring(0, sourcefile.length() - 3) + "."
					+ (step - 1) + ".in";
		}
		stress_i.clear();
		strain_i.clear();
		System.out.println("\nReading result file: " + resfile);
		try {
			RandomAccessFile in = new RandomAccessFile(resfile, "r");
			String dat;
			while ((dat = in.readLine()) != null) {
				if (dat.indexOf("STRESSES") != -1) {
					while ((dat = in.readLine()) != null
							&& dat.indexOf("LOCAL") == -1
							&& dat.indexOf("GLOBAL") == -1
							&& dat.indexOf("DISPLACEMENTS") == -1) {
						StringTokenizer st_t = new StringTokenizer(dat);
						float maxsii = Float.MIN_VALUE;
						if (st_t.countTokens() == 7) {
							long pos = in.getFilePointer();
							String dat1 = in.readLine();
							StringTokenizer st_t1 = null;
							if (dat1 != null)
								st_t1 = new StringTokenizer(dat1);
							if (dat1 == null || st_t1.countTokens() == 7
									|| dat1.indexOf("LOCAL") != -1
									|| dat1.indexOf("GLOBAL") != -1
									|| dat1.indexOf("DISPLACEMENTS") != -1) {
								in.seek(pos);
							} else {
								dat += " " + dat1 + " " + in.readLine() + " "
										+ in.readLine() + " " + in.readLine()
										+ " " + in.readLine() + " "
										+ in.readLine() + " " + in.readLine();
							}
						}
						String key = st_t.nextToken();
						float[] arr = toArrayFloat(dat);
						if (arr.length == 6) {
							maxsii = (float) Math.max(
									maxsii,
									Math.sqrt(2)
											/ 2
											* Math.sqrt(Math.pow(arr[0]
													- arr[1], 2)
													+ Math.pow(arr[1] - arr[2],
															2)
													+ Math.pow(arr[2] - arr[0],
															2)
													+ 6
													* (arr[3] * arr[3] + arr[4]
															* arr[4] + arr[5]
															* arr[5])));
						} else {
							maxsii = (float) Math.max(
									maxsii,
									Math.sqrt(2)
											/ 2
											* Math.sqrt(Math.pow(arr[0]
													- arr[1], 2)
													+ Math.pow(arr[1] - arr[2],
															2)
													+ Math.pow(arr[2] - arr[0],
															2)
													+ 6
													* (arr[3] * arr[3] + arr[4]
															* arr[4] + arr[5]
															* arr[5])));
							maxsii = (float) Math
									.max(maxsii,
											Math.sqrt(2)
													/ 2
													* Math.sqrt(Math.pow(arr[6]
															- arr[7], 2)
															+ Math.pow(arr[7]
																	- arr[8], 2)
															+ Math.pow(arr[8]
																	- arr[6], 2)
															+ 6
															* (arr[9] * arr[9]
																	+ arr[10]
																	* arr[10] + arr[11]
																	* arr[11])));
							maxsii = (float) Math
									.max(maxsii,
											Math.sqrt(2)
													/ 2
													* Math.sqrt(Math.pow(
															arr[12] - arr[13],
															2)
															+ Math.pow(arr[13]
																	- arr[14],
																	2)
															+ Math.pow(arr[14]
																	- arr[12],
																	2)
															+ 6
															* (arr[15]
																	* arr[15]
																	+ arr[16]
																	* arr[16] + arr[17]
																	* arr[17])));
							maxsii = (float) Math
									.max(maxsii,
											Math.sqrt(2)
													/ 2
													* Math.sqrt(Math.pow(
															arr[18] - arr[19],
															2)
															+ Math.pow(arr[19]
																	- arr[20],
																	2)
															+ Math.pow(arr[20]
																	- arr[18],
																	2)
															+ 6
															* (arr[21]
																	* arr[21]
																	+ arr[22]
																	* arr[22] + arr[23]
																	* arr[23])));
							maxsii = (float) Math
									.max(maxsii,
											Math.sqrt(2)
													/ 2
													* Math.sqrt(Math.pow(
															arr[24] - arr[25],
															2)
															+ Math.pow(arr[25]
																	- arr[26],
																	2)
															+ Math.pow(arr[26]
																	- arr[24],
																	2)
															+ 6
															* (arr[27]
																	* arr[27]
																	+ arr[28]
																	* arr[28] + arr[29]
																	* arr[29])));
							maxsii = (float) Math
									.max(maxsii,
											Math.sqrt(2)
													/ 2
													* Math.sqrt(Math.pow(
															arr[30] - arr[31],
															2)
															+ Math.pow(arr[31]
																	- arr[32],
																	2)
															+ Math.pow(arr[32]
																	- arr[30],
																	2)
															+ 6
															* (arr[33]
																	* arr[33]
																	+ arr[34]
																	* arr[34] + arr[35]
																	* arr[35])));
							maxsii = (float) Math
									.max(maxsii,
											Math.sqrt(2)
													/ 2
													* Math.sqrt(Math.pow(
															arr[36] - arr[37],
															2)
															+ Math.pow(arr[36]
																	- arr[38],
																	2)
															+ Math.pow(arr[38]
																	- arr[36],
																	2)
															+ 6
															* (arr[39]
																	* arr[39]
																	+ arr[40]
																	* arr[40] + arr[41]
																	* arr[41])));
							maxsii = (float) Math
									.max(maxsii,
											Math.sqrt(2)
													/ 2
													* Math.sqrt(Math.pow(
															arr[42] - arr[43],
															2)
															+ Math.pow(arr[43]
																	- arr[44],
																	2)
															+ Math.pow(arr[44]
																	- arr[42],
																	2)
															+ 6
															* (arr[45]
																	* arr[45]
																	+ arr[46]
																	* arr[46] + arr[47]
																	* arr[47])));
						}
						maxsi = (float) Math.max(maxsi, maxsii);
						Object m = stress_i.get(key);
						if (m != null) {
							maxsii = Math.max(maxsii, ((Float) m).floatValue());
						}
						stress_i.put(key, new Float(maxsii));
					}
				}
				if (dat.indexOf("STRAINS") != -1) {
					while ((dat = in.readLine()) != null
							&& dat.indexOf("LOCAL") == -1
							&& dat.indexOf("GLOBAL") == -1
							&& dat.indexOf("DISPLACEMENTS") == -1) {
						StringTokenizer st_t = new StringTokenizer(dat);
						float maxeii = Float.MIN_VALUE;
						if (st_t.countTokens() == 7) {
							long pos = in.getFilePointer();
							String dat1 = in.readLine();
							StringTokenizer st_t1 = null;
							if (dat1 != null)
								st_t1 = new StringTokenizer(dat1);
							if (dat1 == null || st_t1.countTokens() == 7
									|| dat1.indexOf("LOCAL") != -1
									|| dat1.indexOf("GLOBAL") != -1
									|| dat1.indexOf("DISPLACEMENTS") != -1) {
								in.seek(pos);
							} else {
								dat += " " + dat1 + " " + in.readLine() + " "
										+ in.readLine() + " " + in.readLine()
										+ " " + in.readLine() + " "
										+ in.readLine() + " " + in.readLine();
							}
						}
						String key = st_t.nextToken();
						float[] arr = toArrayFloat(dat);
						if (arr.length == 6) {
							maxeii = (float) Math.max(
									maxeii,
									Math.sqrt(2)
											/ 3
											* Math.sqrt(Math.pow(arr[0]
													- arr[1], 2)
													+ Math.pow(arr[1] - arr[2],
															2)
													+ Math.pow(arr[2] - arr[0],
															2)
													+ 1.5
													* (arr[3] * arr[3] + arr[4]
															* arr[4] + arr[5]
															* arr[5])));
						} else {
							maxeii = (float) Math.max(
									maxeii,
									Math.sqrt(2)
											/ 3
											* Math.sqrt(Math.pow(arr[0]
													- arr[1], 2)
													+ Math.pow(arr[1] - arr[2],
															2)
													+ Math.pow(arr[2] - arr[0],
															2)
													+ 1.5
													* (arr[3] * arr[3] + arr[4]
															* arr[4] + arr[5]
															* arr[5])));
							maxeii = (float) Math
									.max(maxeii,
											Math.sqrt(2)
													/ 3
													* Math.sqrt(Math.pow(arr[6]
															- arr[7], 2)
															+ Math.pow(arr[7]
																	- arr[8], 2)
															+ Math.pow(arr[8]
																	- arr[6], 2)
															+ 1.5
															* (arr[9] * arr[9]
																	+ arr[10]
																	* arr[10] + arr[11]
																	* arr[11])));
							maxeii = (float) Math
									.max(maxeii,
											Math.sqrt(2)
													/ 3
													* Math.sqrt(Math.pow(
															arr[12] - arr[13],
															2)
															+ Math.pow(arr[13]
																	- arr[14],
																	2)
															+ Math.pow(arr[14]
																	- arr[12],
																	2)
															+ 1.5
															* (arr[15]
																	* arr[15]
																	+ arr[16]
																	* arr[16] + arr[17]
																	* arr[17])));
							maxeii = (float) Math
									.max(maxeii,
											Math.sqrt(2)
													/ 3
													* Math.sqrt(Math.pow(
															arr[18] - arr[19],
															2)
															+ Math.pow(arr[19]
																	- arr[20],
																	2)
															+ Math.pow(arr[20]
																	- arr[18],
																	2)
															+ 1.5
															* (arr[21]
																	* arr[21]
																	+ arr[22]
																	* arr[22] + arr[23]
																	* arr[23])));
							maxeii = (float) Math
									.max(maxeii,
											Math.sqrt(2)
													/ 3
													* Math.sqrt(Math.pow(
															arr[24] - arr[25],
															2)
															+ Math.pow(arr[25]
																	- arr[26],
																	2)
															+ Math.pow(arr[26]
																	- arr[24],
																	2)
															+ 1.5
															* (arr[27]
																	* arr[27]
																	+ arr[28]
																	* arr[28] + arr[29]
																	* arr[29])));
							maxeii = (float) Math
									.max(maxeii,
											Math.sqrt(2)
													/ 3
													* Math.sqrt(Math.pow(
															arr[30] - arr[31],
															2)
															+ Math.pow(arr[31]
																	- arr[32],
																	2)
															+ Math.pow(arr[32]
																	- arr[30],
																	2)
															+ 1.5
															* (arr[33]
																	* arr[33]
																	+ arr[34]
																	* arr[34] + arr[35]
																	* arr[35])));
							maxeii = (float) Math
									.max(maxeii,
											Math.sqrt(2)
													/ 3
													* Math.sqrt(Math.pow(
															arr[36] - arr[37],
															2)
															+ Math.pow(arr[36]
																	- arr[38],
																	2)
															+ Math.pow(arr[38]
																	- arr[36],
																	2)
															+ 1.5
															* (arr[39]
																	* arr[39]
																	+ arr[40]
																	* arr[40] + arr[41]
																	* arr[41])));
							maxeii = (float) Math
									.max(maxeii,
											Math.sqrt(2)
													/ 3
													* Math.sqrt(Math.pow(
															arr[42] - arr[43],
															2)
															+ Math.pow(arr[43]
																	- arr[44],
																	2)
															+ Math.pow(arr[44]
																	- arr[42],
																	2)
															+ 1.5
															* (arr[45]
																	* arr[45]
																	+ arr[46]
																	* arr[46] + arr[47]
																	* arr[47])));
						}
						maxei = (float) Math.max(maxei, maxeii);
						Object m = strain_i.get(key);
						if (m != null) {
							maxeii = Math.max(maxeii, ((Float) m).floatValue());
						}
						strain_i.put(key, new Float(maxeii));
					}
				}
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("\nEdit model: " + infile);
		in_load(infile);
		System.out.println("\nRemeshing model");
		in_remesh(step);
		System.out.println("\nSave new model: " + args[0]);
		in_save(args[0]);
		System.out.println("\nTopology optimization of structure. Step = "
				+ step);
		System.out.println("Start solving problems .... ");

		return args[0];
	}

	public float[] toArrayFloat(String st) throws Exception {
		StringTokenizer st_t = new StringTokenizer(st);
		float[] arr = new float[st_t.countTokens() - 1];
		st_t.nextToken();
		for (int i = 0; i < arr.length; i++) {
			arr[i] = Float.parseFloat(st_t.nextToken());
		}
		return arr;
	}

	public void in_load(String fin) {
		infile = new _In(fin);
		infile.set_Id("");
		infile.reset(true);
	}

	public void in_remesh(byte step) {

		System.out.println("\nRemoving elements ignore.stress(i) =  "
				+ (ignore_stress_i / steps * step));
		for (Enumeration en = stress_i.keys(); en.hasMoreElements();) {
			String key = en.nextElement() + "";
			float volue = ((Float) stress_i.get(key)).floatValue();
			if (maxsi / 100 * ignore_stress_i / steps * step >= volue) {
				for (int i = 0; i < infile.elementsDB.size(); i++) {
					_Object obj = (_Object) infile.elementsDB.elementAt(i);
					String id = obj.get_Id();
					if (id.equalsIgnoreCase(key)) {
						System.out.println(obj.toString() + "\tStress(i) = "
								+ volue);
						infile.elementsDB.removeElementAt(i);
						i--;
					}
				}
			}
		}

		System.out.println("\nRemoving elements ignore.strain(i) =  "
				+ (ignore_strain_i / steps * step));
		for (Enumeration en = strain_i.keys(); en.hasMoreElements();) {
			String key = en.nextElement() + "";
			float volue = ((Float) strain_i.get(key)).floatValue();
			if (maxei / 100 * ignore_strain_i / steps * step >= volue) {
				for (int i = 0; i < infile.elementsDB.size(); i++) {
					_Object obj = (_Object) infile.elementsDB.elementAt(i);
					String id = obj.get_Id();
					if (id.equalsIgnoreCase(key)) {
						System.out.println(obj.toString() + "\tStrain(i) = "
								+ volue);
						infile.elementsDB.removeElementAt(i);
						i--;
					}
				}
			}
		}

		boolean isNext = true;
		while (isNext) {
			isNext = false;
			System.out.println("\nRemoving free elements");
			for (int i = 0; i < infile.elementsDB.size(); i++) {
				boolean isFree = true;
				_Object obj = (_Object) infile.elementsDB.elementAt(i);
				_Node[] n = obj.get_Nodes();
				int count_elements = 0;
				for (int j = 0; j < infile.elementsDB.size(); j++) {
					if (j == i)
						continue;
					byte join_nodes = 0;
					_Object obj_to = (_Object) infile.elementsDB.elementAt(j);
					_Node[] n_to = obj_to.get_Nodes();
					for (int k = 0; k < n.length; k++) {
						for (int m = 0; m < n_to.length; m++) {
							if (n[k].equals(n_to[m]))
								join_nodes++;
						}
					}
					if (isFree)
						isFree = join_nodes < 2;
					if (join_nodes > 1)
						count_elements++;
				}
				if (isFree || count_elements < 2) {
					System.out.println(obj.toString());
					infile.elementsDB.removeElementAt(i);
					isNext = true;
					i--;
				}
			}
		}

		System.out.println("\nRemoving free nodes");
		for (int i = 0; i < infile.nodesDB.size(); i++) {
			boolean isFree = true;
			_Node n = (_Node) infile.nodesDB.elementAt(i);
			for (int j = 0; (j < infile.elementsDB.size() && isFree); j++) {
				_Object obj = (_Object) infile.elementsDB.elementAt(j);
				_Node[] n_obj = obj.get_Nodes();
				for (int m = 0; (m < n_obj.length && isFree); m++) {
					if (n.equals(n_obj[m]))
						isFree = false;
				}
			}
			if (isFree) {
				System.out.println(n.toString());
				infile.nodesDB.removeElementAt(i);
				i--;
			}
		}
	}

	public void in_save(String fout) {
		File file = new File(fout);
		file.delete();
		if (infile == null)
			return;
		try {
			RandomAccessFile out = new RandomAccessFile(fout, "rw");
			out.seek(0);
			out.writeBytes("# " + ver + "\n");
			out.writeBytes("# Model: \t" + sourcefile + "\n");
			out.writeBytes("# " + new Date() + "\n\n");
			out.writeBytes(infile.ContpolsDB + "\n");
			out.writeBytes(infile.TrackersDB + "\n");
			if (infile.LoadDB.keys().hasMoreElements()) {
				out.writeBytes("\nLOADS\n");
				for (Enumeration en = infile.LoadDB.keys(); en.hasMoreElements();) {
					String key = en.nextElement() + "";
					Loads ld = (Loads) infile.LoadDB.get(key);
					out.writeBytes(ld.toString() + "\n");
				}
			}
			out.writeBytes("\n\n");
			for (Enumeration en = infile.ConstDB.keys(); en.hasMoreElements();) {
				String key = en.nextElement() + "";
				Constraints con = (Constraints) infile.ConstDB.get(key);
				out.writeBytes("\nCONSTRAINTS OF TYPE " + con.type + "\n");
				out.writeBytes(con.toString() + "\n\n");
			}
			out.writeBytes("\n");
			for (Enumeration en = infile.MatDB.keys(); en.hasMoreElements();) {
				String key = en.nextElement() + "";
				gui.Material mat = (gui.Material) infile.MatDB.get(key);
				out.writeBytes("\nMATERIALS OF TYPE " + mat.type + "\n");
				out.writeBytes(mat.name + mat.description + "\n\n");
			}
			out.writeBytes("\n");
			out.writeBytes("NODES\n");
			for (int i = 0; i < infile.nodesDB.size(); i++) {
				_Node n = (_Node) infile.nodesDB.elementAt(i);
				String st = n.get_Id() + " \tX = " + n.x + " \tY = " + n.y
						+ " \tZ = " + n.z;
				if (n.constraint != null)
					st += " \tConstraint = " + n.constraint.name;
				if (n.load != null)
					st += " \tLoad = " + n.load.name;
				if (n.M != null)
					st += " \tM = " + n.M;
				if (n.Ixx != null)
					st += " \tIxx = " + n.Ixx;
				if (n.Iyy != null)
					st += " \tIyy = " + n.Iyy;
				if (n.Izz != null)
					st += " \tIzz = " + n.Izz;
				if (n.Ixy != null)
					st += " \tIxy = " + n.Ixy;
				if (n.Iyz != null)
					st += " \tIyz = " + n.Iyz;
				if (n.Ixz != null)
					st += " \tIxz = " + n.Ixz;
				out.writeBytes(st + "\n");
			}
			out.writeBytes("\n");
			String st_elements = "";
			StringBuffer st_elements_Rod_2 = new StringBuffer(
					"ELEMENTS OF TYPE Rod_2\n");
			StringBuffer st_elements_Beam_2 = new StringBuffer(
					"ELEMENTS OF TYPE Beam_2\n");
			StringBuffer st_elements_Beam_Spring_2 = new StringBuffer(
					"ELEMENTS OF TYPE Beam_Spring_2\n");
			StringBuffer st_elements_Contact_Line = new StringBuffer(
					"ELEMENTS OF TYPE Contact_Line\n");
			StringBuffer st_elements_Contact_Triangle = new StringBuffer(
					"ELEMENTS OF TYPE Contact_Triangle\n");
			StringBuffer st_elements_Shell_C0_3 = new StringBuffer(
					"ELEMENTS OF TYPE Shell_C0_3\n");
			StringBuffer st_elements_Shell_BT_4 = new StringBuffer(
					"ELEMENTS OF TYPE Shell_BT_4\n");
			StringBuffer st_elements_Solid_Iso_4 = new StringBuffer(
					"ELEMENTS OF TYPE Solid_Iso_4\n");
			StringBuffer st_elements_Solid_Iso_6 = new StringBuffer(
					"ELEMENTS OF TYPE Solid_Iso_6\n");
			for (int i = 0; i < infile.elementsDB.size(); i++) {
				_Object obj = (_Object) infile.elementsDB.elementAt(i);
				String st = obj.get_Id();
				if (obj instanceof _Element2
						&& ((_Element2) obj).msh_type == Canvas3D.MESH_Beam_2) {
					_Element2 el = (_Element2) obj;
					st += " \tnodes = [" + el.node1.get_Id() + ","
							+ el.node2.get_Id() + "] \tD = " + el.diameter
							+ "\tMaterial = " + el.material.name + "\n";
					st_elements_Beam_2.append(st);
				} else if (obj instanceof _Element2
						&& ((_Element2) obj).msh_type == Canvas3D.MESH_Contact_Line) {
					_Element2 el = (_Element2) obj;
					st += " \tnodes = [" + el.node1.get_Id() + ","
							+ el.node2.get_Id() + "] \tD = " + el.diameter;
					if (el.factor != null)
						st += " \tFactor = " + el.factor;
					if (el.friction != null)
						st += " \tFriction = " + el.friction;
					if (el.contact.compareToIgnoreCase("OFF") == 0)
						st += " \tContact = OFF";
					st_elements_Contact_Line.append(st + "\n");
				} else if (obj instanceof _Element2
						&& ((_Element2) obj).msh_type == Canvas3D.MESH_Rod_2) {
					_Element2 el = (_Element2) obj;
					st += " \tnodes = [" + el.node1.get_Id() + ","
							+ el.node2.get_Id() + "] \tD = " + el.diameter
							+ "\tMaterial = " + el.material.name;
					if (el.factor != null)
						st += " \tFactor = " + el.factor;
					if (el.friction != null)
						st += " \tFriction = " + el.friction;
					if (el.contact != null
							&& el.contact.compareToIgnoreCase("OFF") == 0)
						st += " \tContact = OFF";
					st_elements_Rod_2.append(st + "\n");
				} else if (obj instanceof _Element3
						&& ((_Element3) obj).msh_type == Canvas3D.MESH_Contact_Triangle) {
					_Element3 el = (_Element3) obj;
					st += " \tnodes = [" + el.node1.get_Id() + ","
							+ el.node2.get_Id() + "," + el.node3.get_Id()
							+ "] \tT = " + el.thickness;
					if (el.factor != null)
						st += " \tFactor = " + el.factor;
					if (el.friction != null)
						st += " \tFriction = " + el.friction;
					st_elements_Contact_Triangle.append(st + "\n");
				} else if (obj instanceof _Element3
						&& ((_Element3) obj).msh_type == Canvas3D.MESH_Shell_C0_3) {
					_Element3 el = (_Element3) obj;
					st += " \tnodes = [" + el.node1.get_Id() + ","
							+ el.node2.get_Id() + "," + el.node3.get_Id()
							+ "] \tT = " + el.thickness + " \tMaterial = "
							+ el.material.name;
					if (el.NIP != null)
						st += " \tNIP = " + el.NIP;
					if (el.PIP != null)
						st += " \tPIP = " + el.PIP;
					if (el.load != null)
						st += " \tLoad = " + el.load.name;
					if (el.factor != null)
						st += " \tFactor = " + el.factor;
					if (el.contact != null && el.contact.length() != 0)
						st += " \tContact = " + el.contact;
					if (el.friction != null)
						st += " \tFriction = " + el.friction;
					if (el.thinning != null
							&& el.thinning.compareToIgnoreCase("OFF") == 0)
						st += " \tThinning = OFF";
					st_elements_Shell_C0_3.append(st + "\n");
				} else if (obj instanceof _Element4
						&& ((_Element4) obj).msh_type == Canvas3D.MESH_Shell_BT_4) {
					_Element4 el = (_Element4) obj;
					st += " \tnodes = [" + el.node1.get_Id() + ","
							+ el.node2.get_Id() + "," + el.node3.get_Id() + ","
							+ el.node4.get_Id() + "] \tT = " + el.thickness
							+ " \tMaterial = " + el.material.name;
					if (el.NIP != null)
						st += " \tNIP = " + el.NIP;
					if (el.PIP != null)
						st += " \tPIP = " + el.PIP;
					if (el.load != null)
						st += " \tLoad = " + el.load.name;
					if (el.factor != null)
						st += " \tFactor = " + el.factor;
					if (el.contact != null && el.contact.length() != 0)
						st += " \tContact = " + el.contact;
					if (el.friction != null)
						st += " \tFriction = " + el.friction;
					if (el.thinning != null
							&& el.thinning.compareToIgnoreCase("OFF") == 0)
						st += " \tThinning = OFF";
					if (el.shear_factor != null)
						st += " \tSHEAR_FACTOR = " + el.shear_factor;
					if (el.hourglass != null
							&& el.hourglass.compareToIgnoreCase("OFF") == 0)
						st += " \tHOURGLASS = OFF";
					if (el.MHC != null)
						st += " \tMHC = " + el.MHC;
					if (el.OOPHC != null)
						st += " \tOOPHC = " + el.OOPHC;
					if (el.RHC != null)
						st += " \tRHC = " + el.RHC;
					st_elements_Shell_BT_4.append(st + "\n");
				} else if (obj instanceof _Element4
						&& ((_Element4) obj).msh_type == Canvas3D.MESH_Solid_Iso_4) {
					_Element4 el = (_Element4) obj;
					st += " \tnodes = [" + el.node1.get_Id() + ","
							+ el.node2.get_Id() + "," + el.node3.get_Id() + ","
							+ el.node4.get_Id() + "] \tMaterial = "
							+ el.material.name;
					if (el.NIP != null)
						st += " \tNIP = " + el.NIP;
					st_elements_Solid_Iso_4.append(st + "\n");
				} else if (obj instanceof _Element8) {
					_Element8 el = (_Element8) obj;
					st += " \tnodes = [" + el.node1.get_Id() + ","
							+ el.node2.get_Id() + "," + el.node3.get_Id() + ","
							+ el.node4.get_Id() + "," + el.node5.get_Id() + ","
							+ el.node6.get_Id() + "," + el.node7.get_Id() + ","
							+ el.node8.get_Id() + "] \tNIP = 8 \tMaterial = "
							+ el.material.name + "\n";
					st_elements_Solid_Iso_6.append(st);
				}
			}
			st_elements = (st_elements_Rod_2.length() > 35 ? st_elements_Rod_2
					.toString() + "\n\n" : "")
					+ (st_elements_Beam_2.length() > 35 ? st_elements_Beam_2
							.toString() + "\n\n" : "")
					+ (st_elements_Beam_Spring_2.length() > 35 ? st_elements_Beam_Spring_2
							.toString() + "\n\n"
							: "")
					+ (st_elements_Contact_Line.length() > 35 ? st_elements_Contact_Line
							.toString() + "\n\n"
							: "")
					+ (st_elements_Contact_Triangle.length() > 35 ? st_elements_Contact_Triangle
							.toString() + "\n\n"
							: "")
					+ (st_elements_Shell_C0_3.length() > 35 ? st_elements_Shell_C0_3
							.toString() + "\n\n"
							: "")
					+ (st_elements_Shell_BT_4.length() > 35 ? st_elements_Shell_BT_4
							.toString() + "\n\n"
							: "")
					+ (st_elements_Solid_Iso_4.length() > 35 ? st_elements_Solid_Iso_4
							.toString() + "\n\n"
							: "")
					+ (st_elements_Solid_Iso_6.length() > 35 ? st_elements_Solid_Iso_6
							.toString() : "");
			out.writeBytes(st_elements);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method sends an exception to registred listeners. This is needed
	 * since exception migrates normally only to the run() method and not
	 * outside.
	 * 
	 * @param e
	 *            The exception to send
	 */
	private void sendException(Exception e) {
		if (exception_listeners.size() == 0) {
			e.printStackTrace();
			return;
		}

		Iterator iter = exception_listeners.iterator();

		while (iter.hasNext()) {
			ExceptionListener l = (ExceptionListener) iter.next();
			l.exceptionOccurred(e, this);
		}
	}

}
