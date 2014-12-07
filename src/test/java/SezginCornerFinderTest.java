import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.SezginCornerFinder;

/**
 * Created by jaideepray on 12/6/14.
 */
public class SezginCornerFinderTest extends AbstractCornerFinderTest implements ICornerFinderTest {

    public void main() {
        testCorners();
    }


    @Override
    public void testCorners() {
        SezginCornerFinder cornerFinder = new SezginCornerFinder();
        TStroke testStroke = TestFigure.getStroke();
        super.printCorners(cornerFinder.findCorners(testStroke), testStroke);
    }
}

