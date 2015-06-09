package pl.codeleak.demos.sbt.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeInfo {
	String  		img_url;
	String          message;
}
