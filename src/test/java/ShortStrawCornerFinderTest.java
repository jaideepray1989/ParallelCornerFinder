import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.ShortStrawCornerFinder;

/**
 * Created by jaideepray on 12/6/14.
 */
public class ShortStrawCornerFinderTest extends AbstractCornerFinderTest implements ICornerFinderTest{

    public void main() {
        testCorners();
    }


    @Override
    public void testCorners() {
        ShortStrawCornerFinder cornerFinder = new ShortStrawCornerFinder();
        TStroke testStroke = TestFigure.getStroke();
        super.printCorners(cornerFinder.findCorners(testStroke),testStroke);
    }
}
