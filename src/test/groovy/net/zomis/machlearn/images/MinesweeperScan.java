package net.zomis.machlearn.images;

import net.zomis.machlearn.neural.Backpropagation;
import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

public class MinesweeperScan {

    private static String LEARN_IMAGE = "challenge-flags-16x16.png";
    private static BufferedImage img = ImageUtil.resource(LEARN_IMAGE);

    public static void scan() {
        ImageAnalysis analysis = new ImageAnalysis(1, 100, true);
        ImageNetwork network = analysis.neuralNetwork(40)
                .classifyNone(analysis.imagePart(img, 0, 540))
                .classifyNone(analysis.imagePart(img, 100, 540))
                .classifyNone(analysis.imagePart(img, 200, 540))
                .classifyNone(analysis.imagePart(img, 300, 540))
                .classifyNone(analysis.imagePart(img, 400, 540))
                .classifyNone(analysis.imagePart(img, 500, 540))
                .classifyNone(analysis.imagePart(img, 600, 540))
                .classifyNone(analysis.imagePart(img, 610, 540))
                .classify(true, analysis.imagePart(img, 625, 540))
                .classify(true, analysis.imagePart(img, 630, 540))
                .classify(true, analysis.imagePart(img, 670, 540))
                .learn(new Backpropagation(0.1, 10000), new Random(42));

        BufferedImage runImage = ImageUtil.resource("challenge-press-26x14.png");
        ZRect rect = findEdges(network, analysis, runImage);
        System.out.println("Edges: " + rect);
        // also try find separations by scanning lines and finding the line with the lowest delta diff

        ZRect[][] gridLocations = findGrid(runImage, rect);
        char[][] gridValues = scanGrid(runImage, gridLocations);
        for (int y = 0; y < gridValues.length; y++) {
            for (int x = 0; x < gridValues[y].length; x++) {
                System.out.print(gridValues[y][x]);
            }
            System.out.println();
        }
    }

    private static char[][] scanGrid(BufferedImage runImage, ZRect[][] gridLocations) {
        String fileName = "challenge-flags-16x16.png";
//        String fileName = "different-colors.png";
        BufferedImage image = ImageUtil.resource(fileName);
        ImageAnalysis analyze = new ImageAnalysis(36, 36, true);
        ImageNetwork network = analyze.neuralNetwork(40)
                .classify('_', analyze.imagePart(image, 622, 200))
                .classify('1', analyze.imagePart(image, 793, 287))
                .classify('2', analyze.imagePart(image, 665, 200))
                .classify('3', analyze.imagePart(image, 793, 244))
                .classify('4', analyze.imagePart(image, 750, 416))
                .classify('5', analyze.imagePart(image, 664, 502))
                .classify('6', analyze.imagePart(image, 707, 502))
                .classify('a', analyze.imagePart(image, 793, 200))
                .classifyNone(analyze.imagePart(image, 0, 0))
                .classifyNone(analyze.imagePart(image, 878, 456))
                .classifyNone(analyze.imagePart(image, 903, 456))
                .classifyNone(analyze.imagePart(image, 948, 456))
                .classifyNone(analyze.imagePart(image, 1004, 558))
                .classifyNone(analyze.imagePart(image, 921, 496))
                .classifyNone(analyze.imagePart(image, 921, 536))
                .classifyNone(analyze.imagePart(image, 963, 536))
                .learn(new Backpropagation(0.1, 4000), new Random(42));

        char[][] result = new char[gridLocations.length][gridLocations[0].length];
        for (int y = 0; y < gridLocations.length; y++) {
            for (int x = 0; x < gridLocations[y].length; x++) {
                ZRect rect = gridLocations[y][x];
                Map<Object, Double> output = scanSquare(analyze, network, runImage, rect);
                char ch = charForOutput(output);
                result[y][x] = ch;
            }
        }
        return result;
    }

    private static char charForOutput(Map<Object, Double> output) {
        if (output == null) {
            return '%';
        }
        Map.Entry<Object, Double> max = output.entrySet().stream()
                .max(Comparator.comparingDouble(e -> e.getValue())).get();
        if (max.getValue() < 0.5) {
            return '#';
        }
        return (Character) max.getKey();
    }

    private static Map<Object, Double> scanSquare(ImageAnalysis analyze, ImageNetwork network, BufferedImage runImage, ZRect rect) {
        if (rect == null) {
            return null;
        }
        int min = Math.min(analyze.getWidth(), analyze.getHeight());
        int minRect = Math.min(rect.width(), rect.height());
        BufferedImage image = Scalr.crop(runImage, rect.left, rect.top, minRect, minRect);
        BufferedImage run = Scalr.resize(image, min, min);
        System.out.printf("Running on %s with target size %d, %d run image is %d, %d%n", rect,
                analyze.getWidth(), analyze.getHeight(), run.getWidth(), run.getHeight());
        return network.run(analyze.imagePart(run, 0, 0));
    }

    private static ZRect[][] findGrid(BufferedImage runImage, ZRect rect) {
        // Classify the line separator as true
        ImageAnalysis horizontalAnalysis = new ImageAnalysis(50, 2, true);
        ImageNetwork horizontal = horizontalAnalysis.neuralNetwork(20)
                .classify(true, horizontalAnalysis.imagePart(img, 600, 235))
                .classify(true, horizontalAnalysis.imagePart(img, 700, 235))
                .classifyNone(horizontalAnalysis.imagePart(img, 600, 249))
                .classifyNone(horizontalAnalysis.imagePart(img, 664, 249))
                .learn(new Backpropagation(0.1, 10000), new Random(42));

        ImageAnalysis verticalAnalysis = new ImageAnalysis(2, 50, true);
        ImageNetwork vertical = verticalAnalysis.neuralNetwork(20)
                .classify(true, verticalAnalysis.imagePart(img, 700, 300))
                .classify(true, verticalAnalysis.imagePart(img, 700, 400))
                .classifyNone(verticalAnalysis.imagePart(img, 682, 279))
                .classifyNone(verticalAnalysis.imagePart(img, 765, 279))
                .classifyNone(verticalAnalysis.imagePart(img, 630, 249))
                .classifyNone(verticalAnalysis.imagePart(img, 795, 290))
                .classifyNone(verticalAnalysis.imagePart(img, 795, 365))
                .classifyNone(verticalAnalysis.imagePart(img, 795, 465))
                .classifyNone(verticalAnalysis.imagePart(img, 722, 497))
                .classifyNone(verticalAnalysis.imagePart(img, 770, 249))
                .classifyNone(verticalAnalysis.imagePart(img, 719, 497))
                .learn(new Backpropagation(0.1, 10000), new Random(42));

        List<Integer> horizontalLines = new ArrayList<>();
        for (int y = rect.top; y + horizontalAnalysis.getHeight() < rect.bottom; y++) {
            double[] input = horizontalAnalysis.imagePart(runImage, rect.left + 10, y);
            double[] output = horizontal.getNetwork().run(input);
            double result = output[0];
            if (result > 0.7) {
                horizontalLines.add(y);
            }
        }

        List<Integer> verticalLines = new ArrayList<>();
        for (int x = rect.left; x + verticalAnalysis.getWidth() < rect.right; x++) {
            double[] input = verticalAnalysis.imagePart(runImage, x, rect.top + 10);
            double[] output = vertical.getNetwork().run(input);
            double result = output[0];
            if (result > 0.7) {
                verticalLines.add(x);
            }
        }

//        runAndSave(verticalAnalysis, vertical, runImage);

        System.out.println("Edges: " + rect);
        System.out.println("Horizontal: " + horizontalLines);
        System.out.println("Vertical  : " + verticalLines);

        horizontalLines = removeCloseValues(horizontalLines, 15);
        verticalLines = removeCloseValues(verticalLines, 15);
        System.out.println("------------");
        System.out.println("Horizontal " + horizontalLines.size() + ": " + horizontalLines);
        System.out.println("Vertical " + verticalLines.size() + ": " + verticalLines);

        // Remove outliers
        int squareWidth = verticalLines.get(1) - verticalLines.get(0);
        int squareHeight = horizontalLines.get(1) - horizontalLines.get(0);
        verticalLines = removeCloseValues(verticalLines, (int) (squareWidth * 0.75));
        horizontalLines = removeCloseValues(horizontalLines, (int) (squareHeight * 0.75));

        System.out.println("------------");
        System.out.println("Horizontal " + horizontalLines.size() + ": " + horizontalLines);
        System.out.println("Vertical " + verticalLines.size() + ": " + verticalLines);

        ZRect[][] gridLocations = grabRects(runImage, rect, horizontalLines, verticalLines, squareWidth, squareHeight);
        System.out.println("Square size = " + squareWidth + " x " + squareHeight);
        System.out.println("Squares found: " + gridLocations[0].length + " x " + gridLocations.length);
        return gridLocations;
    }

    private static ZRect[][] grabRects(BufferedImage image, ZRect rect, List<Integer> horizontalLines, List<Integer> verticalLines,
             int squareWidth, int squareHeight) {
        horizontalLines = new ArrayList<>(horizontalLines);
        verticalLines = new ArrayList<>(verticalLines);
        horizontalLines.add(rect.top);
        horizontalLines.add(rect.bottom);
        verticalLines.add(rect.left);
        verticalLines.add(rect.right);
        Collections.sort(horizontalLines);
        Collections.sort(verticalLines);

        horizontalLines = removeCloseValues(horizontalLines, (int) (squareHeight * 0.75));
        verticalLines = removeCloseValues(verticalLines, (int) (squareWidth * 0.75));

//        int beforeFirstX = verticalLines.get(0) - rect.left;
//        int afterLastX = rect.right - verticalLines.get(verticalLines.size() - 1);
//        int beforeFirstY = horizontalLines.get(0) - rect.top;
//        int afterLastY = rect.bottom - horizontalLines.get(horizontalLines.size() - 1);

        System.out.println("Horizontal " + horizontalLines.size() + ": " + horizontalLines);
        System.out.println("Vertical " + verticalLines.size() + ": " + verticalLines);

        ZRect[][] results = new ZRect[horizontalLines.size() + 1][verticalLines.size() + 1];
        int x = 0;
        for (Integer left : verticalLines) {
            int y = 0;
            for (Integer top : horizontalLines) {
                ZRect r = new ZRect();
                r.left = left;
                r.top = top;
                r.right = left + squareWidth;
                r.bottom = top + squareHeight;
                if (r.right >= image.getWidth()) {
                    continue;
                }
                if (r.bottom >= image.getHeight()) {
                    continue;
                }
                results[y][x] = r;
                y++;
            }
            x++;
        }

        return results;
    }

    private static List<Integer> removeCloseValues(List<Integer> values, int closeRange) {
        List<Integer> result = new ArrayList<>();
        Integer last = null;
        for (Integer i : values) {
            if (last == null || last + closeRange < i) {
                last = i;
                result.add(i);
            }
        }
        return result;
    }

    private static void runAndSave(ImageAnalysis analysis, ImageNetwork network, BufferedImage image) {
        BufferedImage[] networkResult = runOnImage(analysis, network, image);
        for (int i = 0; i < networkResult.length; i++) {
            ImageUtil.save(networkResult[i], new File("network-result-" + i + ".png"));
        }
    }

    private static BufferedImage[] runOnImage(ImageAnalysis analysis, ImageNetwork network, BufferedImage runImage) {
        int maxY = runImage.getHeight() - analysis.getHeight();
        int maxX = runImage.getWidth() - analysis.getWidth();
        BufferedImage[] images = new BufferedImage[network.getNetwork().getOutputLayer().size()];
        for (int i = 0; i < images.length; i++) {
            images[i] = new BufferedImage(runImage.getWidth(), runImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = images[i].createGraphics();
            graphics.setColor(Color.MAGENTA);
            graphics.fillRect(0, 0, runImage.getWidth(), runImage.getHeight());
        }

        for (int y = 0; y < maxY; y++) {
            if (y % 20 == 0) {
                System.out.println("process y " + y);
            }
            for (int x = 0; x < maxX; x++) {
                double[] input = analysis.imagePart(runImage, x, y);
                double[] output = network.getNetwork().run(input);
                for (int i = 0; i < output.length; i++) {
                    double value = output[i];
                    int grayscaleValue = (int) (value * 255);
//                    System.out.println(x + ", " + y + ": " + grayscaleValue + " -- " + value);
                    int rgb = 0xff << 24 | grayscaleValue << 16 | grayscaleValue << 8 | grayscaleValue;
                    images[i].setRGB(x, y, rgb);
                }
            }
        }
        return images;
    }

    private static ZRect findEdges(ImageNetwork network, ImageAnalysis analysis, BufferedImage runImage) {
        ZRect rect = new ZRect();
        int x = runImage.getWidth() - 1;
        int y = runImage.getHeight() / 2;

        rect.left  = findEdge(network, analysis, runImage, 0, y, 3, 0).getX();
        rect.right = findEdge(network, analysis, runImage, x, y, -3, 0).getX();

        int imgBottom = runImage.getHeight() - analysis.getHeight();
        rect.top    = findEdge(network, analysis, runImage, rect.left, 0, 0, 3).getY();
        rect.bottom = findEdge(network, analysis, runImage, rect.left, imgBottom, 0, -3).getY()
            + analysis.getHeight();

        return rect;
    }

    private static ZPoint findEdge(ImageNetwork network,
           ImageAnalysis analysis, BufferedImage runImage,
           int x, int y, int deltaX, int deltaY) {
        while (true) {
            double[] input = analysis.imagePart(runImage, x, y);
            double[] output = network.getNetwork().run(input);
            for (double v : output) {
                if (v >= 0.7) {
                    return new ZPoint(x, y);
                }
            }
            x += deltaX;
            y += deltaY;
            if (!inRange(x, y, runImage)) {
                throw new RuntimeException("Unable to find goal");
            }
        }
    }

    private static boolean inRange(int x, int y, BufferedImage img) {
        return x >= 0 &&
            y >= 0 &&
            x < img.getWidth() &&
            y < img.getHeight();
    }

}
