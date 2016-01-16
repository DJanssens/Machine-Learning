package net.zomis.machlearn.neural

import java.util.stream.DoubleStream

class BackPropagation {

    final double learningRate
    final int iterations
    int logRate = Integer.MAX_VALUE

    public BackPropagation(double learningRate, int iterations) {
        assert 0 < learningRate
        assert learningRate < 1
        this.learningRate = learningRate
        this.iterations = iterations
    }

    static class Deltas {
        private final Map<Neuron, Integer> neuronIndexes = new HashMap<>()
        private final double[] deltaValues

        Deltas(NeuralNetwork network) {
            int index = 0
            for (NeuronLayer layer : network.getLayers()) {
                for (Neuron neuron : layer) {
                    neuronIndexes.put(neuron, index++)
                }
            }
            this.deltaValues = new double[index]
        }

        def set(Neuron neuron, double v) {
            deltaValues[neuronIndexes.get(neuron)] = v
        }

        double get(Neuron neuron) {
            deltaValues[neuronIndexes.get(neuron)]
        }

        def log() {
            println "Deltas are $deltaValues"
        }

        @Override
        String toString() {
            Arrays.toString(deltaValues)
        }
    }

    NeuralNetwork backPropagationLearning(Collection<LearningData> examples, NeuralNetwork network) {
        backPropagationLearning(examples, network, new Random())
    }

    NeuralNetwork backPropagationLearning(Collection<LearningData> examples, NeuralNetwork network, Random random) {
        int[] layerSizes = network.layers.stream().mapToInt({it.size()}).toArray()
//        inputs: examples, a set of examples, each with input vector x and output vector y
//        network , a multilayer network with L layers, weights wi,j , activation function g

        // local variables: Δ, a vector of errors, indexed by network node
        Deltas deltas = new Deltas(network)
        int iterations = 0

        network.links().forEach({it.weight = random.nextDouble()})
        while (true) {
            iterations++
            double cost = 0
            for (LearningData data : examples) {
                /* Propagate the inputs forward to compute the outputs */
                int neuronIndexInLayer = 0
                for (Neuron neuron : network.getInputLayer()) {
                    neuron.output = data.inputs[neuronIndexInLayer++]
                }
                for (int layerIndex = 1; layerIndex < network.getLayerCount(); layerIndex++) {
                    NeuronLayer layer = network.getLayer(layerIndex)
                    for (Neuron node : layer) {
                        node.process()
                    }
                }

                /* Propagate deltas backward from output layer to input layer */
                double[] expectedOutput = data.outputs
                neuronIndexInLayer = 0
                for (Neuron neuron : network.getOutputLayer()) {
                    double neuronError = expectedOutput[neuronIndexInLayer++] - neuron.output
                    deltas.set(neuron, (double) (neuron.gPrimInput() * neuronError))
                }

                for (int layerIndex = network.getLayerCount() - 2; layerIndex >= 0; layerIndex--) {
                    for (Neuron node : network.getLayer(layerIndex)) {
                        double sum = node.outputs.stream().mapToDouble({it.weight * deltas.get(it.to)}).sum()
                        deltas.set(node, (double) (node.gPrimInput() * sum))
                    }
                }

                /* Update every weight in network using deltas */
                for (int layerIndex = 1; layerIndex < network.getLayerCount(); layerIndex++) {
                    NeuronLayer layer = network.getLayer(layerIndex)
                    for (Neuron node : layer) {
                        for (NeuronLink link : node.getInputs()) {
                            link.weight = link.weight + learningRate * link.getInputValue() * deltas.get(node)
                        }
                    }
                }
                cost += costFunction(network, data)
            }

            double regularizationTerm = 0
            cost = (-cost / examples.size()) + regularizationTerm

            if (iterations % logRate == 0) {
                DoubleSummaryStatistics data = DoubleStream.of(deltas.deltaValues).summaryStatistics()
                println "BackPropagation $layerSizes iteration $iterations : $data cost $cost"
            }
            if (iterations > this.iterations) {
                break;
            }
        }
        return network
    }

    static double costFunction(NeuralNetwork network, LearningData learningData) {
        double sum = 0
        double[] out = learningData.outputs

        NeuronLayer outputLayer = network.getOutputLayer()
        for (int i = 0; i < out.length; i++) {
            double expected = out[i]
            double actual = outputLayer.neurons.get(i).output
            sum += logisticCost(expected, actual)
        }
        sum
    }

    static double logisticCost(double expected, double actual) {
        expected * Math.log(actual) + (1 - expected) * Math.log(1 - actual)
    }
}
