package pt.free.vpcleaner;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import pt.free.vpcleaner.FXMLController.FileData;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class ConvertWorker extends Task<Void>{
	private final ObservableList<FileData> fileList;
	private final PNGTranscoder pngTranscoder= new PNGTranscoder();
	private String directory;
	
	public ConvertWorker(ObservableList<FileData> fileList,String directory) {
		this.fileList = fileList;
		this.directory = directory;
		
	}
	
	@Override
	protected Void call() throws Exception {
		fileList(directory);
		
		return null;
	}
	
	public void fileList(String directory) {
    	final PathMatcher matcherFixed = FileSystems.getDefault().getPathMatcher("glob:*.fixed.svg");
    	final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.svg");
    	
    	fileList.clear();
    	updateProgress(-1, 1);
    	
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
            for (Path path : directoryStream) {
            	if( matcher.matches(path.getFileName()) && !matcherFixed.matches(path.getFileName())){
            		final FileData fileData = new FileData(path);
            		updateMessage("Fixing SVG");
					fixSVG(fileData);
					updateMessage("Converting to PNG");
            		convert(fileData);
            		updateMessage("Done!");
            		fileList.add(fileData);
            	}
            }
            updateMessage("");
        } catch (IOException ex) {
        	updateMessage("Failure : "+ex.getMessage());
//        	System.err.println("Error");
        } catch (TranscoderException e) {
        	updateMessage("Failure : "+e.getMessage());
//			e.printStackTrace();
        } catch (Throwable e) {
//			e.printStackTrace();
        	updateMessage("Failure : "+e.getMessage());
		}
        
        
        updateProgress(0, 0);
        
    }
	public void fixSVG(FileData fileData) throws TranscoderException, IOException{
    	final Path svgPath = Paths.get(fileData.path.toString(),fileData.fileName.toString());
    	final Path svgFixedPath = Paths.get(fileData.path.toString(),fileData.fixedSVGFileName.toString());
    	
    	 final TransformerFactory factory = TransformerFactory.newInstance();
    	 factory.setURIResolver(new URIResolver() {
			
			@Override
			public Source resolve(String arg0, String arg1) throws TransformerException {
				System.out.println("arg0="+arg0);
				System.out.println("arg1="+arg1);
				return null;
			}
		});
    	 try {
         final Source xslt = new StreamSource(FXMLController.class.getResourceAsStream("/fix.xsl"));
         final SAXParserFactory spf = SAXParserFactory.newInstance();
         spf.setNamespaceAware(true);
         spf.setValidating(false);
         final XMLReader rdr = spf.newSAXParser().getXMLReader();
         rdr.setFeature("http://xml.org/sax/features/validation", false);
         rdr.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
         
         Source text = new SAXSource(rdr, new InputSource(svgPath.toUri().toString())); 
         
//         final Source text = new StreamSource(svgPath.toFile());         
         if( !Files.exists(svgFixedPath)){
        	 Files.createFile(svgFixedPath); 
         }         
         FileOutputStream fos = new FileOutputStream(svgFixedPath.toFile());
		final Result output =new StreamResult(fos);
         
        
			final Transformer transformer = factory.newTransformer(xslt);
			 transformer.transform(text, output);
			 fos.close();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
    	
    }
    public void convert(FileData fileData) throws TranscoderException, IOException{
    	
    	Path svgPath = Paths.get(fileData.path.toString(),fileData.fixedSVGFileName.toString());
    	if( Files.exists(svgPath)) {
			final TranscoderInput input = new TranscoderInput(svgPath.toUri().toString());
	
	        
	        // Create the transcoder output.
	        final OutputStream ostream = new FileOutputStream(Paths.get(fileData.path.toString(),fileData.pngFileName.toString()).toFile());
	        final TranscoderOutput output = new TranscoderOutput(ostream);
	
	        // Save the image.
	        pngTranscoder.transcode(input, output);
	
	        // Flush and close the stream.
	        ostream.flush();
	        ostream.close();
    	}
    }
}
