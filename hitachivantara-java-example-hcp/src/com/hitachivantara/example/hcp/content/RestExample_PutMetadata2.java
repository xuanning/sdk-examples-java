package com.hitachivantara.example.hcp.content;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.hitachivantara.common.ex.HSCException;
import com.hitachivantara.common.util.StreamUtils;
import com.hitachivantara.example.hcp.util.HCPClients;
import com.hitachivantara.hcp.common.ex.InvalidResponseException;
import com.hitachivantara.hcp.standard.body.HCPStandardClient;
import com.hitachivantara.hcp.standard.model.metadata.HCPMetadata;
import com.hitachivantara.hcp.standard.model.metadata.S3CompatibleMetadata;
import com.hitachivantara.hcp.standard.model.request.impl.PutMetadataRequest;
import com.hitachivantara.hcp.standard.util.MetadataUtils;

/**
 * 存取S3标准metadata示例
 * @author sohan
 *
 */
public class RestExample_PutMetadata2 {

	public static void main(String[] args) throws IOException {
		// Here is the file will be uploaded into HCP
		File file = new File("C:\\VDisk\\DriverD\\Downloads\\Temp\\WeChat Image_20180716111626.doc");
		// The location in HCP where this file will be stored.
		String key = "folder/subfolder/" + file.getName();

		{
			try {
				HCPStandardClient hcpClient = HCPClients.getInstance().getHCPClient();

				S3CompatibleMetadata metadata = new S3CompatibleMetadata();
				metadata.put("name", "Rison");
				metadata.put("company", "hitachi vantara");
				
				// Attach S3 Compatible METADATA with specific key
				//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
				hcpClient.putMetadata(key, metadata);
				//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*

				Document doc = RestExample_PutMetadata.createDocument();

				// Attach Custom METADATA with specific key
				//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
				hcpClient.putMetadata(new PutMetadataRequest(key, "metadata2", MetadataUtils.toByteArray(doc)));
				//=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
				
				// Get metadata from HCP
				S3CompatibleMetadata metadataFromHCP = hcpClient.getMetadata(key);
				HCPMetadata meta = hcpClient.getMetadata(key, "moreInfo");
				String xmlContent = StreamUtils.inputStreamToString(meta.getContent(), true);

				// Verify contents.
				assertTrue("Rison".equals(metadataFromHCP.get("name")));
				assertTrue("hitachi vantara".equals(metadataFromHCP.get("company")));

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				OutputFormat format = OutputFormat.createPrettyPrint(); // 转换成字符串
				format.setEncoding("UTF-8");
				XMLWriter writer = new XMLWriter(out, format);
				writer.write(doc);
				assertTrue(xmlContent.equalsIgnoreCase(out.toString()));

			} catch (InvalidResponseException e) {
				e.printStackTrace();
				return;
			} catch (HSCException e) {
				e.printStackTrace();
				return;
			}

			System.out.println("Well done!");
		}
	}

}
