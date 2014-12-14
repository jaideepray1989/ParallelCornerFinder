package cornerfinders.impl.rankfragmenter.features.stroke;

import cornerfinders.core.shapes.TStroke;
import cornerfinders.impl.rankfragmenter.rfutils.RFUtils;

/**
 * Created by jaideepray on 12/12/14.
 */
public class DiagonalFeature {
    public static double getDiagonalLength(TStroke stroke) {
        return RFUtils.boundingBoxSize(stroke);
    }
}
