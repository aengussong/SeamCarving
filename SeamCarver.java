import edu.princeton.cs.algs4.Picture;
import java.util.Arrays;

public class SeamCarver {

	private static final int VERTICAl = 0;
	private static final int HORIZONTAL = 1;

	private double[][] energy;

	private Picture picture;

	public SeamCarver(Picture picture) {
		if(picture == null) throw new IllegalArgumentException();
		initPicture(picture);
	}

	public Picture picture() {
		return new Picture(picture);
	}

	public int width() {
		return picture.width();
	}

	public int height() {
		return picture.height();
	}

	public double energy(int x, int y) {
		checkPixel(x, y);
		return energy[x][y];
	}

	public int[] findHorizontalSeam() {
		int[][] path = new int[width()][height()];
		double[][] values = new double[width()][height()];
		for (int i = 0; i < width(); i++) {
			Arrays.fill(values[i], Double.MAX_VALUE);
		}

		for (int i = 0; i < height(); i++) {
			values[0][i] = energy[0][i];
		}

		for (int x = 0; x < width()-1; x++) {
			for (int y = 1; y < height()-1; y++) {
				relax(path, values, x, y, x+1, y-1, HORIZONTAL);
				relax(path, values, x, y, x+1, y, HORIZONTAL);
				relax(path, values, x, y, x+1, y+1, HORIZONTAL);
			}
		}

		double shorthest = Double.MAX_VALUE;
		int shorthestIndex = 0;
		for (int y = 0; y < height(); y++) {
			if(values[width()-1][y] < shorthest) {
				shorthest = values[width()-1][y];
				shorthestIndex = y;
			}
		}

		int nextIndex = shorthestIndex;
		int[] horizontalSeam = new int[width()];
		for (int x = width()-1; x >= 0; x--) {
			horizontalSeam[x] = nextIndex;
			nextIndex = path[x][nextIndex];
		}

		return horizontalSeam;
	}

	public int[] findVerticalSeam() {
		int[][] path = new int[width()][height()];
		double[][] values = new double[width()][height()];
		for (int i = 0; i < width(); i++) {
			Arrays.fill(values[i], Double.MAX_VALUE);
		}

		for (int i = 0; i < width(); i++) {
			values[i][0] = energy[i][0];
		}

		for (int y = 0; y < height()-1; y++) {
			for (int x = 1; x < width()-1; x++) {
				relax(path, values, x, y, x-1, y+1, VERTICAl);
				relax(path, values, x, y, x, y+1, VERTICAl);
				relax(path, values, x, y, x+1, y+1, VERTICAl);
			}
		}

		double shorthest = Double.MAX_VALUE;
		int shorthestIndex = 0;
		for (int x = 0; x < width(); x++) {
			if(values[x][height()-1] < shorthest) {
				shorthest = values[x][height()-1];
				shorthestIndex = x;
			}
		}

		int nextIndex = shorthestIndex;
		int[] verticalSeam = new int[height()];
		for (int y = height()-1; y >= 0; y--) {
			verticalSeam[y] = nextIndex;
			nextIndex = path[nextIndex][y];
		}

		return verticalSeam;
	}

	private void relax(int[][] path, double[][] values, int x, int y, int tx, int ty, int orientation) {
		if (values[tx][ty] < values[x][y] + energy[tx][ty]) return;

		values[tx][ty] = values[x][y] + energy[tx][ty];
		if (orientation == VERTICAl) {
			path[tx][ty] = x;
		} else {
			path[tx][ty] = y;
		}
	}

	public void removeHorizontalSeam(int[] seam) {
		checkSeam(seam, HORIZONTAL);

		Picture newPicture = new Picture(width(), height()-1);

		for (int x = 0; x < width(); x++) {
			for (int y = 0; y < height(); y++) {
				if (y == seam[x]) continue;
				if (y > seam[x]) {
					newPicture.setRGB(x, y-1, picture.getRGB(x, y));
				} else {
					newPicture.setRGB(x, y, picture.getRGB(x, y));
				}
			}
		}

		initPicture(newPicture);
	}

	public void removeVerticalSeam(int[] seam) {
		checkSeam(seam, VERTICAl);

		Picture newPicture = new Picture(width()-1, height());

		for (int y = 0; y< height(); y++) {
			for (int x = 0; x<width(); x++) {
				if (x == seam[y]) continue;
				if (x > seam[y]) {
					newPicture.setRGB(x-1, y, picture.getRGB(x, y));
				} else {
					newPicture.setRGB(x, y, picture.getRGB(x, y));
				}
			}
		}

		initPicture(newPicture);
	}

	private void initPicture(Picture picture) {
		this.picture = picture;
		computeEnergy();
	}

	private void checkPixel(int x, int y) {
		if(x < 0 || x > width()-1 || y < 0 || y > height()-1) {
			throw new IllegalArgumentException();
		}
	}

	private void checkSeam(int[] seam, int orientation) {
		if (seam == null) throw new IllegalArgumentException();
		int size;
		int maxSeamIndex;
		if (orientation == VERTICAl) {
			size = height();
			maxSeamIndex = width()-1;
		} else {
			size = width();
			maxSeamIndex = height()-1;
		} 
		if(maxSeamIndex <= 0) throw new IllegalArgumentException();
		if(seam.length != size) throw new IllegalArgumentException();

		int prevP = -1;
		for(int p: seam) {
			if(p < 0 || p > maxSeamIndex) throw new IllegalArgumentException();
			if(prevP == -1) {
				prevP = p;
				continue;
			}
			if(Math.abs(prevP - p) > 1) throw new IllegalArgumentException();
			prevP = p;
		}
	}

	private void computeEnergy() {
		energy = new double[width()][height()];

		for(int x = 0; x < width(); x++) {
			for( int y = 0; y < height(); y++) {
				energy[x][y] = pixelEnergy(x, y);
			}
		}
	}

	private double pixelEnergy(int x, int y) {
		if(x == 0 || y == 0 || x == width()-1 || y == height() -1 ) return 1000.0;

		int colorRight = picture.getRGB(x+1, y);
		int colorLeft = picture.getRGB(x-1, y);
		int colorBottom = picture.getRGB(x, y+1);
		int colorTop = picture.getRGB(x, y-1);

		double deltaX = calculateDelta(colorRight, colorLeft);
		double deltaY = calculateDelta(colorBottom, colorTop);

		return Math.sqrt(deltaX + deltaY);
	}

	private double calculateDelta(int firstColor, int secondColor) {
		int rFirst = getRed(firstColor);
  		int gFirst = getGreen(firstColor);
  		int bFirst = getBlue(firstColor);

  		int rSecond = getRed(secondColor);
  		int gSecond = getGreen(secondColor);
  		int bSecond = getBlue(secondColor);

  		return Math.pow(rFirst - rSecond, 2) + Math.pow(gFirst - gSecond, 2) + Math.pow(bFirst - bSecond, 2);
	}

	private int getRed(int color) {
		return (color >> 16) & 0xFF;
	}

	private int getGreen(int color) {
		return (color >>  8) & 0xFF;
	}

	private int getBlue(int color) {
		return (color >>  0) & 0xFF;
	}
}