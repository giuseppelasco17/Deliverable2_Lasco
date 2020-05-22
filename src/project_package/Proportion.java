package project_package;

public class Proportion {
	private static Proportion istance = null;

	private int predIv = -1; // special value, means that the proportion is not yet calculated, so the IV =
								// OV
	private double prop;
	private int n = 0; // # of proportion calculated

	private Proportion() {
	}

	public static Proportion getIstance() {
		if (istance == null)
			istance = new Proportion();
		return istance;
	}

	public int getPredIv(int openV, int fixV) {
		if (n == 0) {
			return openV;
		} else {
			predIv = (int) ((double) fixV - (double) (fixV - openV) * prop);
			if (predIv < 1) {
				predIv = 1;
			}
		}
		return predIv;
	}

	public void setPredIv(int predIv) {
		this.predIv = predIv;
	}

	public double getProp() {
		return prop;
	}

	public void setProp(int prop) {
		this.prop = prop;
	}

	public void calcProportion(int fixV, int openV, int injV) {
		int newProp =  ((fixV - injV) / (fixV - openV));
		n++;
		if (n == 1) {
			prop = newProp;
		} else {
			prop = ((((double) n - 1.0) / (double) n) * prop + (1.0 / (double) n) * newProp);// average between new and
																								// old prop value
		}
	}
}
