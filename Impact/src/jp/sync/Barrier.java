/*
 * Created on Dec 18, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package jp.sync;

/**
 * @author pc58410
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class Barrier {

	int state, count;

	/**
	 * @param state
	 */
	public Barrier(int count) {
		super();
		this.count = count;
		this.state = count;
	}

	/**
	 * 
	 */
	public synchronized void sync() throws InterruptedException {

		state--;

		if (state == 0) {
			notifyAll();
			state = count;
		} else
			wait();
	}

}
