public class Stereogram {
    private int[][] stereogram;
    private static final int MAX = 255;

    public Stereogram(int width, int height) {
        stereogram = new int[width][height];
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                stereogram[x][y] = choice();
            }
        }
    }

    public int[][] getStereogram() {
        return this.stereogram;
    }

    private int choice() {
        return (int) Math.round(Math.random()) * MAX;
    }

    private void printSubArr(int x1, int y1, int x2, int y2) {
        for (int x = x1, i = 0; x<x2; x++, i++) {
            for (int y = y1, j = 0; y < y2; y++, j++) {
                System.out.print(stereogram[x][y] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
    public void slice(int x1, int y1, int x2, int y2, int[][] newImg) {
        for (int x = x1, i = 0; x<x2; x++, i++) {
            for (int y = y1, j = 0; y < y2; y++, j++) {
                this.stereogram[x][y] = newImg[i][j];
            }
        }
    }
}
