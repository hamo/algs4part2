import java.awt.Color;

public class SeamCarver {
    private final static double borderEnergy = 195075;

    private int width_;
    private int height_;

    private boolean transpose_;

    // Color[width_][height_]
    private Color[][] colorMatrix_;
    // double[width_][height_]
    private double[][] energyMatrix_;

    private double[][] accEnergyMatrix;
    private int[][] paths;

    private Color get(Color[][] m, int x, int y) {
        if (transpose_)
            return m[y][x];
        else
            return m[x][y];
    }

    private void set(Color[][] m, int x, int y, Color c) {
        if (transpose_)
            m[y][x] = c;
        else
            m[x][y] = c;
    }

    private double get(double[][] m, int x, int y) {
        if (transpose_)
            return m[y][x];
        else
            return m[x][y];
    }

    private void set(double[][] m, int x, int y, double c) {
        if (transpose_)
            m[y][x] = c;
        else
            m[x][y] = c;
    }

    private int get(int[][] m, int x, int y) {
        if (transpose_)
            return m[y][x];
        else
            return m[x][y];
    }

    private void set(int[][] m, int x, int y, int c) {
        if (transpose_)
            m[y][x] = c;
        else
            m[x][y] = c;
    }

    private void calEnergyMatrix() {
        for (int x = 0; x < width(); x++)
            for (int y = 0; y < height(); y++)
                set(energyMatrix_, x, y, energy(x,y));
    }

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null)
            throw new java.lang.NullPointerException();

        width_ = picture.width();
        height_ = picture.height();

        transpose_ = false;

        // FIXME:
        colorMatrix_ = new Color[width()][height()];
        energyMatrix_ = new double[width()][height()];

        accEnergyMatrix = new double[width()][height()];
        paths = new int[width()][height()];

        for (int x = 0; x < width(); x++)
            for (int y = 0; y < height(); y++)
                set(colorMatrix_, x, y, picture.get(x, y));

        calEnergyMatrix();
    }

    // current picture
    public Picture picture() {
        Picture re = new Picture(width(), height());

        for (int x = 0; x < width(); x++)
            for (int y = 0; y < height(); y++)
                re.set(x, y, get(colorMatrix_, x, y));

        return re;
    }

    // width of current picture
    public int width() {
        if (transpose_)
            return height_;
        else
            return width_;
    }

    // height of current picture
    public int height() {
        if (transpose_)
            return width_;
        else
            return height_;
    }

    private double energyD(Color cc, Color bc) {
        double dr = cc.getRed() - bc.getRed();
        double dg = cc.getGreen() - bc.getGreen();
        double db = cc.getBlue() - bc.getBlue();

        return dr*dr + dg*dg + db*db;
    }

    private double energyDx(int x, int y) {
        Color cc = get(colorMatrix_, x+1, y);
        Color bc = get(colorMatrix_, x-1, y);

        return energyD(cc, bc);
    }

    private double energyDy(int x, int y) {
        Color cc = get(colorMatrix_, x, y+1);
        Color bc = get(colorMatrix_, x, y-1);

        return energyD(cc, bc);
    }

    // energy of pixel at column x and row y
    public  double energy(int x, int y) {
        if (x < 0 || x >= width())
            throw new java.lang.IndexOutOfBoundsException();
        if (y < 0 || y >= height())
            throw new java.lang.IndexOutOfBoundsException();

        if (x == 0 || y == 0 || x == width() - 1 || y == height() -1)
            return borderEnergy;

        return energyDx(x,y) + energyDy(x,y);
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                if (x == 0) {
                    set(accEnergyMatrix, x, y, get(energyMatrix_, x, y));
                    set(paths, x, y, 0);
                    continue;
                }

                int from;
                double min;

                if (y == 0) {
                    min = (get(accEnergyMatrix, x-1, y) < get(accEnergyMatrix, x-1, y+1)) ?
                        get(accEnergyMatrix, x-1, y) : get(accEnergyMatrix, x-1, y+1);
                    from = (get(accEnergyMatrix, x-1, y) < get(accEnergyMatrix, x-1, y+1)) ?
                        y : y+1;
                } else if (y == height()-1) {
                    min = (get(accEnergyMatrix, x-1, y-1) < get(accEnergyMatrix, x-1, y)) ?
                        get(accEnergyMatrix, x-1, y-1) : get(accEnergyMatrix, x-1, y);
                    from = (get(accEnergyMatrix, x-1, y-1) < get(accEnergyMatrix, x-1, y)) ?
                        y-1 : y;
                } else {
                    if (get(accEnergyMatrix, x-1, y-1) < get(accEnergyMatrix, x-1, y)) {
                        min = get(accEnergyMatrix, x-1, y-1);
                        from = y-1;
                    } else {
                        min = get(accEnergyMatrix, x-1, y);
                        from = y;
                    }

                    if (get(accEnergyMatrix, x-1, y+1) < min) {
                        min = get(accEnergyMatrix, x-1, y+1);
                        from = y+1;
                    }
                }

                set(accEnergyMatrix, x, y, get(energyMatrix_, x, y) + min);
                set(paths, x, y, from);
            }
        }

        double minEng = Double.MAX_VALUE;
        int m=0;
        for (int y = 0; y < height(); y++) {
            if (get(accEnergyMatrix, width()-1, y) < minEng) {
                minEng = get(accEnergyMatrix, width()-1, y);
                m = y;
            }
        }

        int re[] = new int[width()];

        re[width()-1] = m;

        for (int x = width()-1; x > 0; x--) {
            re[x-1] = get(paths, x, re[x]);
        }
        return re;
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        // Not thread safe
        transpose_ = true;
        int[] path = findHorizontalSeam();
        transpose_ = false;
        return path;
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (seam == null)
            throw new java.lang.NullPointerException();

        if (height() <= 1)
            throw new java.lang.IllegalArgumentException();

        if (seam.length != width())
            throw new java.lang.IllegalArgumentException();

        if (seam[0] < 0 || seam[0] >= height())
            throw new java.lang.IllegalArgumentException();

        for (int i = 1; i < width(); i++) {
            if (seam[i] < 0 || seam[i] >= height())
                throw new java.lang.IllegalArgumentException();

            int diff = seam[i-1] - seam[i];
            if (diff < -1 || diff > 1)
                throw new java.lang.IllegalArgumentException();
        }

        for (int i = 0; i < width(); i++)
            for (int j = seam[i]+1; j < height(); j++)
                colorMatrix_[i][j-1] = colorMatrix_[i][j];

        height_ = height_ - 1;

        // FIXME: reuse unchanged energy
        calEnergyMatrix();
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (seam == null)
            throw new java.lang.NullPointerException();

        if (width() <= 1)
            throw new java.lang.IllegalArgumentException();

        if (seam.length != height())
            throw new java.lang.IllegalArgumentException();

        if (seam[0] < 0 || seam[0] >= width())
            throw new java.lang.IllegalArgumentException();

        for (int i = 1; i < height(); i++) {
            if (seam[i] < 0 || seam[i] >= width())
                throw new java.lang.IllegalArgumentException();

            int diff = seam[i-1] - seam[i];
            if (diff < -1 || diff > 1)
                throw new java.lang.IllegalArgumentException();
        }

        for (int i = 0; i < height(); i++)
            for (int j = seam[i]+1; j < width(); j++)
                colorMatrix_[j-1][i] = colorMatrix_[j][i];

        width_ = width_ - 1;

        // FIXME: reuse unchanged energy
        calEnergyMatrix();
    }
}
