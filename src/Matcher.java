import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Matcher {
    private final double OCCLUSION = 3.8 * 3;
    private static final int maxRGB = 128; //255 for stereogram (pair 4)
    private static final int WIDTH = 512;
    private static final int HEIGHT = 512;
    private static final int SMALL_WIDTH = 256;
    private static final int SMALL_HEIGHT = 256;

    private int[][] buildDisparityMap(int[][] left, int[][] right) {
        // Build disparity map to infer depth of an image
        int rows = left.length;
        int cols = left[0].length;
        int i = cols - 1;
        int[][] disparityMap = new int[rows][cols];
        double[][] C = emptyMatrix(cols);

        for (int row = 0; row < rows; row++) {
            int[] L = left[row];
            int[] R = right[row];
            double[][] cost_matrix = buildCostMatrix(L, R, C.clone(), cols);
            int[] row_map = backwardPass(cost_matrix, L, R, i, i);
            disparityMap[row] = normaliseMap(row_map);
        }
        return disparityMap;
    }

    private double[][] emptyMatrix(int max) {
        double[][] C = new double[max][max];
        for (int i = 1; i < max; i++) {
            C[i][0] = i * OCCLUSION;
        }
        for (int j = 1; j < max; j++) {
            C[0][j] = j * OCCLUSION;
        }
        return C;
    }

    private double[][] buildCostMatrix(int[] L, int[] R, double[][] C, int max) {
        //Return a matrix representing the cost of matching features
        for (int i = 1; i < max; i++) {
            for (int j = 1; j < max; j++) {
                int c = Math.abs(L[i - 1] - R[j - 1]);
                double x = C[i - 1][j - 1] + ((c * c) >> 4);
                double y = C[i - 1][j] + OCCLUSION;
                double z = C[i][j - 1] + OCCLUSION;
                C[i][j] = Math.min(x, Math.min(y, z));
            }
        }
        return C;
    }

    private int[] backwardPass(double[][] C, int[] L, int[] R, int i, int j) {
        int[] disparityMap = new int[i + 1];
        while (i > 0 && j > 0) {
            double cost = C[i][j];
            int c = Math.abs(L[i - 1] - R[j - 1]);
            if (cost == (C[i - 1][j - 1] + ((c * c) >> 4))) {
                disparityMap[i] = Math.abs(i - j);
                i--;
                j--;
            } else if (cost == (C[i - 1][j] + OCCLUSION)) {
                disparityMap[i] = 0;//(int) Math.round((diff * PROBABILITY_OF_OCCLUSION));
                i--;
            } else {
                disparityMap[i] = 0;//(int) Math.round((diff * PROBABILITY_OF_OCCLUSION));
                j--;
            }
        }
        return disparityMap;
    }

    private int getMax(int[] map) {
        int maxValue = map[0];
        for (int value : map) {
            if (value > maxValue) {
                maxValue = value;
            }
        }
        return maxValue;
    }

    private int[] normaliseMap(int[] map) {
        int maxValue = getMax(map);
        if (maxValue != 0) {
            for (int i = 0; i < map.length; i++) {
                if (map[i] != 0) {
                    map[i] = map[i] * maxRGB / maxValue;
                }
            }
        }
        return map;
    }

    private void saveImage(BufferedImage image, int pairNum) throws IOException {
        String filename = String.format("/home/ernest/Documents/UCL/Algorithms/Coursework 2/Output/Pair %d/disparityMap.png", pairNum);
        File file = new File(filename);
        ImageIO.write(image, "png", file);
        System.out.println(">>> Image saved to " + filename);
    }

    private void saveMap(int[][] map) throws IOException {
        String filename = "/home/ernest/Desktop/out.txt";
        StringBuilder builder = new StringBuilder();
        //for each row
        for (int[] ints : map) {
            for (int j = 0; j < map[0].length; j++)//for each column
            {
                builder.append(ints[j]);//append to the output string
                if (j < map[0].length - 1)//if this is not the last row element
                    builder.append(",");//then add comma (if you don't like commas you can use spaces)
            }
            builder.append("\n");//append new line at the end of the row
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write(builder.toString());//save the string representation of the map
        writer.close();
    }

    private int[][] readImage(int pairNum, int imageNum) throws IOException {
        String filename = String.format("/home/ernest/Documents/UCL/Algorithms/Coursework 2/Stereo Pairs BW/Pair %d/view%d.png", pairNum, imageNum);
        System.out.println(">>> Opening image at " + filename);
        BufferedImage image = ImageIO.read(new File(filename));
        int width = image.getWidth(); //columns
        int height = image.getHeight(); //rows
        int[][] pixels = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int[] pixel = image.getRaster().getPixel(x, y, new int[3]);
                pixels[x][y] = pixel[0];
            }
        }
        return pixels;
    }

    private BufferedImage getImage(int[][] map) {
        int width = map.length;
        int height = map[0].length;
        WritableRaster raster = Raster.createWritableRaster(new PixelInterleavedSampleModel(0, width, height, 1,
                1920, new int[]{0}), new Point(0, 0));
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                raster.setSample(x, y, 0, map[x][y]);
            }
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        image.setData(raster);
        return image;
    }

    private void displayImage(BufferedImage image1, BufferedImage image2, BufferedImage image3) {
        SwingUtilities.invokeLater(
                () -> new GUI(image1, image2, image3));
    }

    private int[][] rotateMap(int[][] map) {
        int width = map.length;
        int height = map[0].length;
        int[][] new_map = new int[height][width];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                new_map[y][x] = map[x][y];
            }
        }
        return new_map;
    }

    private int[][][] getStereograms() {
        Stereogram l = new Stereogram(WIDTH, HEIGHT);
        Stereogram r = new Stereogram(WIDTH, HEIGHT);
        Stereogram s = new Stereogram(256, 256);
        l.slice(124, 128, 124 + SMALL_WIDTH, 128 + SMALL_HEIGHT, s.getStereogram());
        r.slice(132, 128, 128 + SMALL_WIDTH, 128 + SMALL_HEIGHT, s.getStereogram());
        return new int[][][]{l.getStereogram(), r.getStereogram()};
    }

    public void run(boolean image) throws IOException {
        run(image, 4);
    }

    public void run(boolean image, int pairNum) throws IOException {
        int[][] leftImage, rightImage, L, R;
        if (image) {
            leftImage = readImage(pairNum, 1);
            rightImage = readImage(pairNum, 2);
        } else {
            int[][][] stereograms = getStereograms();
            leftImage = stereograms[0];
            rightImage = stereograms[1];
        }
        L = rotateMap(leftImage);
        R = rotateMap(rightImage);
        long startTime = System.nanoTime();
        int[][] disparityMap = buildDisparityMap(L, R);
        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1e9;
        duration = Math.round(duration * 100.0) / 100.0;
        System.out.println(">>> Building the disparity map took " + duration + "s\n");

        int[][] rotatedMap = rotateMap(disparityMap);
        saveImage(getImage(rotatedMap), pairNum);
        displayImage(getImage(leftImage), getImage(rightImage), getImage(rotatedMap));
    }

    public static void main(String[] args) throws IOException {
        Matcher matcher = new Matcher();
        for (int i = 1; i < 6; i++) {
            matcher.run(true, i);
        }
        //matcher.run(true, 4); //stereogram
    }
}
