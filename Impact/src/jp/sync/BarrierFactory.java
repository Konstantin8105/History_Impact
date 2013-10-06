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
public class BarrierFactory {

	/**
	 * @param i
	 * @return
	 */
	public static Barrier createBarrier(int i) {
		return new Barrier(i);
	}

}
