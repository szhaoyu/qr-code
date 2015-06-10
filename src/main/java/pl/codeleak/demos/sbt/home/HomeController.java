package pl.codeleak.demos.sbt.home;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import pl.codeleak.demos.sbt.model.QRCodeInfo;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;



@Controller
@Slf4j
class HomeController {
	@Autowired
	@Value("${message.url}")
	private String 	messageUrl;
	
    @RequestMapping("/")
    String index(HttpServletRequest request ) {
    	log.info("LocalAddress:"+request.getLocalAddr() );
    	log.info("LocalPort:"+request.getLocalPort());
    	log.info("LocalName:"+request.getLocalName());
    	log.info("RemoteAddress:"+request.getRemoteAddr());
    	log.info("RemoteHost:"+request.getRemoteHost());
    	log.info("RemotePort:"+request.getRemotePort());
    	log.info("RemoteUser:"+request.getRemoteUser());
    	log.info("RequestedSessionID:"+request.getRequestedSessionId());
    	Principal pripal=request.getUserPrincipal();
    	log.info("UserPrincipal:"+pripal);
    	log.info("x-forwarded-for:"+request.getHeader("x-forwarded-for"));

        return "index";
    }

    @RequestMapping("properties")
    @ResponseBody
    java.util.Properties properties() {
        return System.getProperties();
    }
    
    
    @RequestMapping("api/decode")
    public ResponseEntity<?> decodeMessage( @RequestParam(value="e", required=true ) String e ) throws UnsupportedEncodingException {

    	Decoder de=Base64.getDecoder();
    	byte[] deBytes=de.decode(e);
    	String result=null;
    	result=new String(deBytes, "utf-8");

    	return new ResponseEntity<>(result, null, HttpStatus.OK);
    }

    @RequestMapping("message")
    public String regMSG( @RequestParam(value="e", required=true ) String e, Model model ) throws UnsupportedEncodingException {
    	log.info(e);
    	/*
    	String urlTxt=java.net.URLDecoder.decode(e, java.nio.charset.StandardCharsets.UTF_8.toString());
    	log.info(urlTxt);
    	Decoder de=Base64.getDecoder();
    	byte[] deBytes=de.decode(urlTxt);
    	*/
    	byte[] deBytes = Base64.getUrlDecoder().decode(e);
    	String result=null;
    	result=new String(deBytes, "utf-8");
    	model.addAttribute("message", result);
    	
    	Base64.getUrlDecoder().decode(e);
    	
    	return "message";
    }
    
    @RequestMapping(value="generate",method=RequestMethod.POST)
    public ResponseEntity<?> generateQRcode(@RequestBody String body ) throws WriterException, IOException {
    	File directory = new File("");
    	
    	//String path1=directory.getCanonicalPath();
    	String path2=directory.getAbsolutePath();
    	
    	//
    	//Encoder en=Base64.getEncoder();
    	body = URLDecoder.decode( body, "UTF-8");
    	//body = new String( Base64.getDecoder().decode(body) );
    	//body = new String(Base64.getUrlDecoder().decode(body));
    	log.info( body );
    	/*
    	String en_str = Base64.getEncoder().encodeToString( body.getBytes("utf-8") );  
    	log.info( en_str );
    	String e_val = java.net.URLEncoder.encode(en_str, java.nio.charset.StandardCharsets.UTF_8.toString());
    	*/
    	String e_val = Base64.getUrlEncoder().encodeToString( body.getBytes("utf-8")); 
    	StringBuilder urlBuilder = new StringBuilder();
    	urlBuilder.append(messageUrl).append("message?e=").append(e_val);
    	String qr_data=urlBuilder.toString();
    	
    	//generate QRCode
    	StringBuilder filePath = new StringBuilder();
        String charset = "UTF-8"; 							//or "ISO-8859-1"
        Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        filePath.append("/m/images/").append( UUID.randomUUID()).append(".png");
        //
        StringBuilder fullPath = new StringBuilder();
        //fullPath.append(path2).append("/target/classes/static").append(filePath);
        fullPath.append(path2).append(filePath);
        //
        log.info( qr_data );
        //
        createQRCode( qr_data, fullPath.toString(), charset, hintMap, 256, 256 );
        //return 
        QRCodeInfo info = new QRCodeInfo( filePath.toString(), qr_data );

    	return new ResponseEntity<>( info, null, HttpStatus.OK);
    }

    public static void createQRCode(String qrCodeData,String filePath, 
    	String charset, Map hintMap, int qrCodeheight, int qrCodewidth) throws WriterException, IOException {
    	BitMatrix matrix = new MultiFormatWriter().encode( 
    			new String(qrCodeData.getBytes(charset), charset),
    			BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap);
    	Path path=Paths.get( filePath );
    	MatrixToImageWriter.writeToPath(matrix, filePath.substring(filePath.lastIndexOf('.') + 1), path );
    }
    
    public static String readQRCode(String filePath, String charset, Map hintMap)
    	      throws FileNotFoundException, IOException, NotFoundException {
    	BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
    			new BufferedImageLuminanceSource(ImageIO.read(new FileInputStream(filePath)))));
    	Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap, hintMap);
    	return qrCodeResult.getText();
    }
}
