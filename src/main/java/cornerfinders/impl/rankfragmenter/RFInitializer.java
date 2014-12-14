package cornerfinders.impl.rankfragmenter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cornerfinders.core.shapes.TPoint;
import cornerfinders.impl.rankfragmenter.rfutils.RFCost;
import cornerfinders.impl.rankfragmenter.rfutils.RFUtils;

import java.util.List;
import java.util.Map;

public class RFInitializer {

    public Map<Integer, RFNode> getInitialList(List<TPoint> pointList) {
        Map<Integer, RFNode> map = Maps.newHashMap();
        for (int i = 0; i < pointList.size(); i++) {
            RFNode node = new RFNode(i, pointList.get(i), 0);
            map.put(i, node);
        }
        map.get(0).cost = RFUtils.INF;
        List<Integer> toDelete = Lists.newArrayList();
        map.get(pointList.size() - 1).cost = RFUtils.INF;
        for (int i = 0; i < pointList.size(); i++) {
            if (i > 0)
                map.get(i).previous = map.get(i - 1);
            if (i < pointList.size() - 1)
                map.get(i).next = map.get(i + 1);
            if (i > 0 && i < pointList.size() - 1) {
                double cost = RFCost.cost(map.get(i - 1).corner, map.get(i + 1).corner, map.get(i).corner);
                if (cost < 1e-5) {
                    toDelete.add(i);
                } else {
                    map.get(i).cost = cost;
                }
            }
        }

        for(Integer i:toDelete)
        {
            map.remove(i);
        }
        return map;
    }
}
