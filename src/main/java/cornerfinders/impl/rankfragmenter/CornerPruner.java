package cornerfinders.impl.rankfragmenter;

import cornerfinders.core.shapes.TPoint;
import cornerfinders.impl.rankfragmenter.rfutils.RFCost;

import java.util.Map;

/**
 * Created by jaideepray on 12/12/14.
 */
public class CornerPruner {

    public Map<Integer, RFNode> pruneList(Map<Integer, RFNode> cornerList, int np) {
        while (cornerList.size() > np) {
            removeAndUpdate(cornerList);
        }
        return cornerList;
    }

    public RFNode removeAndUpdate(Map<Integer, RFNode> cornerList) {
        double minCost = 100000;
        int index = -1;
        for (Map.Entry<Integer, RFNode> entry : cornerList.entrySet()) {
            Integer i = entry.getKey();
            RFNode node = entry.getValue();

            if (minCost > node.cost) {
                minCost = node.cost;
                index = i;
            }
        }
        RFNode minCostNode = cornerList.get(index);
        RFNode prev = null, succ = null;
        if (minCostNode.previous != null) {
            minCostNode.previous.next = minCostNode.next;
            prev = minCostNode.previous;
        }
        if (minCostNode.next != null) {
            minCostNode.next.previous = minCostNode.previous;
            succ = minCostNode.next;
        }
        cornerList.remove(index);
        updateCost(succ);
        updateCost(prev);
        return minCostNode;
    }

    public void updateCost(RFNode node) {
        if (node == null)
            return;
        TPoint next = null, prev = null;
        if (node.next != null)
            next = node.next.corner;
        if (node.previous != null)
            prev = node.previous.corner;

        node.cost = RFCost.cost(prev, next, node.corner);

    }
}
