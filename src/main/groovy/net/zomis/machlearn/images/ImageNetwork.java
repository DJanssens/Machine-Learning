package net.zomis.machlearn.images;

import net.zomis.machlearn.neural.NeuralNetwork;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ImageNetwork {

    private final NeuralNetwork network;
    private final Object[] outputs;

    public ImageNetwork(NeuralNetwork network, Object[] outputs) {
        if (network.getOutputLayer().size() != outputs.length) {
            throw new IllegalArgumentException(
                    String.format("Network output layer (%d) does not match object array length (%d)",
                            network.getOutputLayer().size(), outputs.length));
        }
        this.network = network;
        this.outputs = Arrays.copyOf(outputs, outputs.length);
    }

    public Object[] getOutputs() {
        return Arrays.copyOf(outputs, outputs.length);
    }

    public Map<Object, Double> run(double[] imageData) {
        double[] outputs = network.run(imageData);
        Map<Object, Double> result = new HashMap<>();
        for (int i = 0; i < outputs.length; i++) {
            result.put(this.outputs[i], outputs[i]);
        }
        return result;
    }

    public NeuralNetwork getNetwork() {
        return this.network;
    }

}
