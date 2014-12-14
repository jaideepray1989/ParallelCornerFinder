package cornerfinders.impl.combination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.combination.objectivefuncs.IObjectiveFunction;

public class SBFSCombinationSegmenter {

	private static final double S_THRESHOLD = 2.020747995;

	public List<Integer> sbfs(List<Integer> corners, TStroke stroke,
			IObjectiveFunction objFunction) {

		if (corners.size() <= 2)
			return corners;

		double currError = Double.MAX_VALUE;
		List<Integer> cornerSubset = new ArrayList<Integer>(corners);
		List<List<Integer>> cornerSubsetList = new ArrayList<List<Integer>>(
				cornerSubset.size());
		List<Double> errorList = new ArrayList<Double>(cornerSubset.size());

		List<List<Integer>> forwardOnSubset = new ArrayList<List<Integer>>();

		int n = -1;

		while (cornerSubset.size() > 2) {

			// Go backward
			List<Object> backResults = prevBestSubset(cornerSubset, stroke,
					objFunction);
			List<Integer> backSubset = (List<Integer>) backResults.get(0);
			double backError = (Double) backResults.get(1);

			// Go forward (if possible)
			int forwardCorner = -1;
			double forwardError = Double.MAX_VALUE;
			if (cornerSubset.size() < corners.size() - 1) {
				List<Object> forwardResults = nextBestCorner(cornerSubset,
						corners, stroke, objFunction);
				forwardCorner = (Integer) forwardResults.get(0);
				forwardError = (Double) forwardResults.get(1);
			}

			// Go forward if the error is better, otherwise continue backward
			if (forwardCorner != -1 && forwardError < errorList.get(n - 1)
					&& !alreadySeenSubset(cornerSubset, forwardOnSubset)) {

				forwardOnSubset.add(new ArrayList<Integer>(cornerSubset));
				cornerSubset.add(forwardCorner);
				Collections.sort(cornerSubset);

				currError = forwardError;
				n--;
			} else {
				cornerSubset = backSubset;
				currError = backError;
				n++;
			}

			// Update the list of best subsets for n corners
			if (cornerSubsetList.size() <= n) {
				cornerSubsetList.add(new ArrayList<Integer>(cornerSubset));
				errorList.add(currError);
			} else {
				cornerSubsetList.set(n, new ArrayList<Integer>(cornerSubset));
				errorList.set(n, currError);
			}
		}

		List<Integer> bestSubset = null;

		double d1Errors[] = new double[errorList.size()];
		for (int i = 1; i < errorList.size(); i++) {
			double deltaError = errorList.get(i) / errorList.get(i - 1);
			d1Errors[i] = deltaError;
		}

		// double d2Errors[] = new double[errorList.size()];
		// for (int i = 2; i < errorList.size(); i++) {
		// double deltaDeltaError = d1Errors[i] / d1Errors[i - 1];
		// d2Errors[i] = deltaDeltaError;
		// }

		// for (int i = 3; i < d2Errors.length; i++) {
		// if (d2Errors[i] > S_THRESHOLD) {
		// bestSubset = cornerSubsetList.get(i - 1);
		// break;
		// }
		// }

		for (int i = 2; i < d1Errors.length; i++) {
			if (d1Errors[i] > S_THRESHOLD) {
				bestSubset = cornerSubsetList.get(i - 1);
				break;
			}
		}

		if (bestSubset == null)
			bestSubset = cornerSubsetList.get(0);

		Collections.sort(bestSubset);

		// printArrayToMatlab(errorList);
		// System.out.println(errorList.size());

		// printArrayToMatlab(d1Errors);
		// System.out.println(d1Errors.length);

		double[] cornerArray = new double[corners.size() - 2];
		for (int i = cornerArray.length; i > 2; i--) {
			cornerArray[cornerArray.length - i] = i;
		}
		// printArrayToMatlab(cornerArray);
		// System.out.println(cornerArray.length);

		return bestSubset;
	}

	private List<Object> prevBestSubset(List<Integer> cornerSubset,
			TStroke stroke, IObjectiveFunction objFunction) {

		List<Double> errorValues = new ArrayList<Double>();
		Collections.sort(cornerSubset);

		for (int i = 0; i < cornerSubset.size(); i++) {
			Integer currCorner = cornerSubset.get(i);

			if (currCorner != 0 && currCorner != stroke.numPoints() - 1) {

				List<Integer> tempSubset = new ArrayList<Integer>(cornerSubset);
				tempSubset.remove(currCorner);

				double value = objFunction.solve(tempSubset, stroke);
				errorValues.add(value);
			} else {
				errorValues.add(Double.MAX_VALUE);
			}
		}

		double minError = Collections.min(errorValues);
		int index = errorValues.indexOf(minError);
		int worstCorner = cornerSubset.get(index);

		List<Integer> bestSubset = new ArrayList<Integer>(cornerSubset);
		bestSubset.remove(new Integer(worstCorner));

		List<Object> returnList = new ArrayList<Object>();
		returnList.add(bestSubset);
		returnList.add(minError);

		return returnList;
	}

	private List<Object> nextBestCorner(List<Integer> cornerSubset,
			List<Integer> corners, TStroke stroke,
			IObjectiveFunction objFunction) {

		List<Double> errorValues = new ArrayList<Double>();
		Collections.sort(cornerSubset);

		for (int i = 0; i < corners.size(); i++) {
			Integer currCorner = corners.get(i);

			if (!cornerSubset.contains(currCorner)) {

				List<Integer> tempSubset = new ArrayList<Integer>(cornerSubset);
				tempSubset.add(currCorner);

				double value = objFunction.solve(tempSubset, stroke);
				errorValues.add(value);
			} else {
				errorValues.add(Double.MAX_VALUE);
			}
		}

		Double minError = Collections.min(errorValues);
		int index = errorValues.indexOf(minError);
		int bestCorner = corners.get(index);

		List<Object> returnList = new ArrayList<Object>();
		returnList.add(bestCorner);
		returnList.add(minError);

		return returnList;
	}

	private boolean alreadySeenSubset(List<Integer> subset,
			List<List<Integer>> prevSeenSubsets) {

		Collections.sort(subset);

		for (List<Integer> seenSubset : prevSeenSubsets) {
			Collections.sort(seenSubset);

			if (subset.size() != seenSubset.size()) {
				continue;
			} else {

				boolean allValsEqual = true;
				for (int i = 0; i < subset.size(); i++) {
					if (subset.get(i).intValue() != seenSubset.get(i)
							.intValue()) {
						allValsEqual = false;
						break;
					}
				}

				if (allValsEqual) {
					return true;
				}
			}
		}

		return false;
	}
}
