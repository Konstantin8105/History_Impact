package run;

import java.util.Comparator;

public class NodeComparator implements Comparator {

	public int compare(Object arg0, Object arg1) {
		Node n1 = (Node) arg0;
		Node n2 = (Node) arg1;
		double diff = n1.getX_pos() - n2.getX_pos();

		if (diff > 0)
			return 1;
		if (diff < 0)
			return -1;
		return 0;
	}

}
