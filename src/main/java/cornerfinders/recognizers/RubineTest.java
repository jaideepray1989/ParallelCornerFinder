package cornerfinders.recognizers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import edu.tamu.bpaulson.linclassifier.ClassificationResult;
import edu.tamu.bpaulson.linclassifier.DataSet;
import edu.tamu.bpaulson.linclassifier.ExampleSet;
import edu.tamu.bpaulson.linclassifier.FeatureVector;
import edu.tamu.bpaulson.linclassifier.LinearClassifier;
import edu.tamu.hammond.sketch.shapes.TRubineStroke;
import edu.tamu.hammond.sketch.shapes.TStroke;

public class RubineTest {

	public static File train;
	public static File test;
	public static DataSet trainSet;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		trainSet = new DataSet();
		JFileChooser c = new JFileChooser();
		c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int r = c.showOpenDialog(null);
		if (r == JFileChooser.APPROVE_OPTION) {
			train = c.getSelectedFile();
		}
		else return;
		r = c.showOpenDialog(null);
		if (r == JFileChooser.APPROVE_OPTION) {
			test = c.getSelectedFile();
		}
		else return;

		File[] files = train.listFiles();
		for (int f = 0; f < files.length; f++) {
			if (!files[f].isDirectory())
				continue;
			File[] shapes = files[f].listFiles();
			ExampleSet set = new ExampleSet();
			for (int s = 0; s < shapes.length; s++) {
				if (shapes[s].isDirectory() || !shapes[s].exists())
					continue;
				List<TStroke> strokeList = TStroke.getTStrokesFromXML(shapes[s]);
				if (strokeList.size() <= 0)
					continue;
				System.out.println("Adding: " + shapes[s]);
				FeatureVector v = new FeatureVector(new TRubineStroke(strokeList.get(0)));
				set.add(v);
			}
			if (!set.isEmpty())
				trainSet.add(set);
		}

		LinearClassifier lc = new LinearClassifier(trainSet);
		int correct = 0;
		int tested = 0;
		files = test.listFiles();
		int actual = 0;
		for (int f = 0; f < files.length; f++) {
			if (!files[f].isDirectory())
				continue;
			File[] shapes = files[f].listFiles();
			boolean didShape = false;
			for (int s = 0; s < shapes.length; s++) {
				if (shapes[s].isDirectory() || !shapes[s].exists())
					continue;
				List<TStroke> strokeList = TStroke.getTStrokesFromXML(shapes[s]);
				if (strokeList.size() <= 0)
					continue;
				TRubineStroke rs = new TRubineStroke(strokeList.get(0));
				FeatureVector fv = new FeatureVector(rs);
				ArrayList<ClassificationResult> result = lc.classify(fv, 1);
				System.out.println("Actual: " + actual);
				System.out.println("Result: " + result.get(0).getClassChosen() + "\n");
				if (actual == result.get(0).getClassChosen())
					correct++;
				tested++;
				didShape = true;
			}
			if (didShape)
				actual++;
		}

		System.out.println("Tested: " + tested);
		System.out.println("Correct: " + correct);
		System.out.println("Accuracy: " + (double)correct/(double)tested);
	}

}
