package botto.xmpp.service.component;

public class NodeFilters {

    public static final NodeFilter singleNode(final String node) {
        return new NodeFilter() {

            @Override
            public boolean accept(String testNode) {
                return node.equals(testNode);
            }

            @Override
            public String toString() {
                return "SingleNodeFilter: " + node;
            }

            @Override
            public int hashCode() {
                return node.hashCode();
            }
        };
    }

}
