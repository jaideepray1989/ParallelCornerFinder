package cornerfinders.core.shapes;

import cornerfinders.core.shapes.helpers.TPointInstance;

/**
 * Created by jaideepray on 12/4/14.
 */
public class TShape {

    private TPointInstance center;
    private Integer length;
    private Integer width;
    private RRectangle boundingBox;

    public TPointInstance getCenter() {
        return center;
    }

    public void setCenter(TPointInstance center) {
        this.center = center;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }
}
