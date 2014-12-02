package pt.free.vpcleaner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FXMLController implements Initializable {
    private static final Logger LOGGER=LoggerFactory.getLogger(FXMLController.class);
    
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
    @FXML
    private ImageView originalImageView;
    @FXML
    private ImageView fixedImageView;
    
    @FXML
    private ProgressIndicator statusProgress;
    @FXML
    private Label statusLabel;
    
    private final BufferedImageTranscoder originalImageTranscoder = new BufferedImageTranscoder();
    private final BufferedImageTranscoder fixedImageTranscoder = new BufferedImageTranscoder();
    
    
    private final ObservableList<FileData> fileList=FXCollections.observableList(new ArrayList<FileData>());
    
    @FXML
    private void handleBrowseAction(ActionEvent event) {
    	
    }
    
    @Override

    public void initialize(URL url, ResourceBundle rb) {
    	//System.getProperty("user.home",null);
    	
    	Path listenPath=Paths.get(System.getProperty("user.home",""),"Documents","VPProjects");
    	
    	directoryPath.setValue(listenPath.toString());
    	
    	colFilename.setCellValueFactory(cellData -> cellData.getValue().filenameProperty);
    	colModified.setCellValueFactory(cellData -> cellData.getValue().modifiedProperty);
    	colSize.setCellValueFactory(cellData -> cellData.getValue().sizeProperty);
    	//fileList(listenPath.toString());
    	
    	tblFiles.setItems(fileList);
    	tblFiles.getSelectionModel().selectedItemProperty().addListener( new ChangeListener<FileData>() {

			@Override
			public void changed(ObservableValue<? extends FileData> observable,FileData oldValue, FileData newValue) {
				if(newValue!=null) {					
					final TranscoderInput originalTransIn = new TranscoderInput(Paths.get(newValue.path.toString(),newValue.fileName.toString()).toUri().toString());
					final TranscoderInput fixedTransIn = new TranscoderInput(Paths.get(newValue.path.toString(),newValue.fixedSVGFileName.toString()).toUri().toString());
					
					try {
						originalImageTranscoder.transcode(originalTransIn, null);
						final Image originalImg = SwingFXUtils.toFXImage(originalImageTranscoder.getBufferedImage(), null);
						originalImageView.setImage(originalImg);
						
						
						fixedImageTranscoder.transcode(fixedTransIn, null);
						final Image fixedImg = SwingFXUtils.toFXImage(fixedImageTranscoder.getBufferedImage(), null);
						fixedImageView.setImage(fixedImg);
						
					} catch (TranscoderException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					
					
				}
			}
		});
    	
    	refresh();
    }      
    
    @FXML
    public void refresh(){
    	final ConvertWorker t = new ConvertWorker(fileList,directoryPath.getValue());
    	statusProgress.progressProperty().bind(t.progressProperty());
    	statusLabel.textProperty().bind(t.messageProperty());
    	
    	Thread thread=new Thread(t);
    	thread.start();
    }
    
    public static class FileData{
    	SimpleStringProperty filenameProperty=new SimpleStringProperty();
    	SimpleObjectProperty<Date> modifiedProperty=new SimpleObjectProperty<Date>();
    	SimpleObjectProperty<Long> sizeProperty=new SimpleObjectProperty<Long>();
		Path path;
		Path fileName;
		Path fixedSVGFileName;
		Path pngFileName;
    	
    	public FileData(Path path) throws IOException {
			this.path = path.getParent();
			this.fileName = path.getFileName();			
			this.pngFileName=Paths.get(extractName(path)+".png");
			this.fixedSVGFileName=Paths.get(extractName(path)+".fixed.svg");
			
			
			this.filenameProperty.set(fileName.toString());			
			this.modifiedProperty.set(new Date(Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toMillis()));
			this.sizeProperty.set(Files.size(path));		
		}

		private String extractName(Path path) {
			final String fileNameStr = path.getFileName().toString();
			return fileNameStr.substring(0, fileNameStr.length()-4);
		}
    }
}
