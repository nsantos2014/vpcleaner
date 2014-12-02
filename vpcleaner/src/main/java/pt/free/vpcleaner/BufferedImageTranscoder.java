package pt.free.vpcleaner;

import java.awt.image.BufferedImage;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

public class BufferedImageTranscoder extends ImageTranscoder {
 
    private BufferedImage img = null;
 
    @Override
    public BufferedImage createImage(int width, int height) {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        return bi;
    }
 
    @Override
    public void writeImage(BufferedImage img, TranscoderOutput to) throws TranscoderException {
        this.img = img;
    }
 
    public BufferedImage getBufferedImage() {
        return img;
    }
}
