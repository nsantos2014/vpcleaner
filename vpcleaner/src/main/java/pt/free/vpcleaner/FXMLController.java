package pt.free.vpcleaner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;

public class FXMLController implements Initializable {
    
    @FXML
    private ComboBox<String> directoryPath;
    @FXML
    private Button browseDirectoryPath;
    @FXML
    private TableView<FileData> tblFiles;
    @FXML
    private TableColumn<FileData,String> colFilename;
    @FXML
    private TableColumn<FileData,Date> colModified;
    @FXML
    private TableColumn<FileData,Long> colSize;
    
    
    private final ObservableList<FileData> fileList=FXCollections.observableList(new ArrayList<FileData>());
    
    @FXML
    private void handleBrowseAction(ActionEvent event) {
    	
    }
    
    @Override

    public void initialize(URL url, ResourceBundle rb) {
    	//System.getProperty("user.home",null);
    	
    	Path listenPath=Paths.get(System.getProperty("user.home",""),"Documents","VPProjects");
    	
    	directoryPath.setValue(listenPath.toString());
    	
    	colFilename.setCellValueFactory(cellData -> cellData.getValue().filename);
    	colModified.setCellValueFactory(cellData -> cellData.getValue().modified);
    	colSize.setCellValueFactory(cellData -> cellData.getValue().size);
    	fileList(listenPath.toString());
    	
    	tblFiles.setItems(fileList);
    }
    
    public void fileList(String directory) {
    	final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/*.svg");
    	
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
            for (Path path : directoryStream) {
            	if( matcher.matches(path)){
            		fileList.add(new FileData(path,0,0));
            		convert(path);
            	}
            }
        } catch (IOException ex) {
        	System.err.println("Error");
        } catch (TranscoderException e) {
			e.printStackTrace();
        } catch (Throwable e) {
			e.printStackTrace();
		}
        
    }
    
    public void convert(Path path) throws TranscoderException, IOException{
    	// Create a JPEG transcoder
        JPEGTranscoder t = new JPEGTranscoder();

        // Set the transcoding hints.
        t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY,new Float(.9));

        // Create the transcoder input.
        //String svgURI = new File(args[0]).toURL().toString();
        TranscoderInput input = new TranscoderInput(path.toUri().toString());

        Path jpgFilename = Paths.get(path.getFileName().toString().replaceAll("[.]svg$", ".jpg"));
        
        // Create the transcoder output.
        OutputStream ostream = new FileOutputStream(Paths.get(path.getParent().toString(),jpgFilename.toString()).toFile());
        TranscoderOutput output = new TranscoderOutput(ostream);

        // Save the image.
        t.transcode(input, output);

        // Flush and close the stream.
        ostream.flush();
        ostream.close();
//        System.exit(0);
    }
    
    class FileData{
    	SimpleStringProperty filename=new SimpleStringProperty();
    	SimpleObjectProperty<Date> modified=new SimpleObjectProperty<Date>();
    	SimpleObjectProperty<Long> size=new SimpleObjectProperty<Long>();
		private Path path;
    	
    	public FileData(Path path,long modified,long size) {
			this.path = path;
			this.filename.set(path.getFileName().toString());
			this.modified.set(new Date(modified));
			this.size.set(size);		
		}
    }
}
