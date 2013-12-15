package testsets.mnist;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.List;

import javax.swing.JPanel;

public class MnistPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private List<int[]> outputs;

	public void setOutputs(List<int[]> outputs) {
		this.outputs = outputs;
	}

	public void paint(java.awt.Graphics g) {
		if (outputs == null)
			return;
		int i = 0;
		for (int[] output : outputs) {
			BufferedImage out = new BufferedImage(28, 28, BufferedImage.TYPE_INT_RGB);
			WritableRaster r = out.getRaster();
			r.setDataElements(0, 0, 28, 28, output);
			BufferedImage newImage = new BufferedImage(56, 56, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = newImage.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2.clearRect(0, 0, 56, 56);
			g2.drawImage(out, 0, 0, 56, 56, null);
			g2.dispose();
			g.drawImage(newImage, 10, 30 + (i * 60), null);
			i++;
		}
	}
}