package de.rwth.i2.attestor.graph.heap.internal;

import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import org.jboss.util.NotImplementedException;

/**
 * A class to convert between the ids used by HeapConfigurations and Graphs.
 */
public class HeapConfigurationIdConverter {

    public static int getGraphId(HeapConfiguration hc, int heapConfigurationId) {
        if (hc instanceof InternalHeapConfiguration) {
            InternalHeapConfiguration internalHeapConfiguration = (InternalHeapConfiguration) hc;
            return internalHeapConfiguration.getPrivateId(heapConfigurationId);
        } else {
            throw new NotImplementedException("HeapConfigurationIdConverter is not implemented for " + hc.getClass().getName());
        }
    }

    public static int getHeapConfigurationId(HeapConfiguration hc, int graphId) {
        if (hc instanceof InternalHeapConfiguration) {
            InternalHeapConfiguration internalHeapConfiguration = (InternalHeapConfiguration) hc;
            return internalHeapConfiguration.getPublicId(graphId);
        } else {
            throw new NotImplementedException("HeapConfigurationIdConverter is not implemented for " + hc.getClass().getName());
        }
    }

}
