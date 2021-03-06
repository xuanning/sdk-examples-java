/*                                                                             
 * Copyright (C) 2019 Hitachi Vantara Inc.                                     
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");           
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,         
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */                                                                            
package com.hitachivantara.example.hcp.content;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.hitachivantara.common.util.DigestUtils;
import com.hitachivantara.example.hcp.util.Account;
import com.hitachivantara.example.hcp.util.HCPClients;
import com.hitachivantara.example.hcp.util.multipartupload.MulitipartUploadException;
import com.hitachivantara.example.hcp.util.multipartupload.MulitipartUploaderExecutor;
import com.hitachivantara.example.hcp.util.multipartupload.UploadEventHandler;

/**
 * S3 分片上传示例
 * @author sohan
 *
 */
public class S3Example_HCPMultipartUpload {
	public static void main(String[] args) throws MulitipartUploadException {
		// 取得客户端Instance
		final AmazonS3 s3Client = HCPClients.getInstance().getS3Client();

		// 分片大小（**万兆带宽推荐设置100MB**，此处示例设置为10MB）
		final int PART_SIZE = 10 * 1024 * 1024; // Set part size to 10 MB.

		// 测试用大文件（**分片上传文件应为大文件至少500MB以上，小文件不建议使用分片方式上传**）
//		final File tobeUploadFile = new File("C:\\VDisk\\DriverD\\Downloads\\Libs\\tika-app-1.7.jar");
//		// 上传key
//		final String objectPath = "hcp-test/" + tobeUploadFile.getName() + "6";
		
		final File tobeUploadFile = new File("D:\\Downloads\\Soft\\anyconnect-win-4.7.01076-predeploy-k9.zip");
		// 上传key
		final String objectPath = "hcp-test/" + tobeUploadFile.getName();

		// 桶名称
		final String bucketName = Account.namespace;

		if(s3Client.doesObjectExist(bucketName, objectPath)) {
			s3Client.deleteObject(bucketName, objectPath);
		}
		// ==========================================================================================================================
		MulitipartUploaderExecutor exec = new MulitipartUploaderExecutor(s3Client, bucketName, objectPath, tobeUploadFile, PART_SIZE);
		// 开始上传（这里使用10个线程上传,文件被分为10片）
		exec.multiThreadUpload(10,
				/**
				 * 分片上传事件处理
				 * 
				 * @author sohan
				 *
				 */
				new UploadEventHandler() {
					private final PrintStream log = System.out;

					@Override
					public void init(String bucketName, String objectPath, String uploadId) {
						log.println("Step 1: Initialize [" + objectPath + "] [" + uploadId + "]");
					}

					@Override
					public void beforePartUpload(String bucketName, String objectPath, String uploadId, int partNumber, long uploadPartsize, long startTime) {
						log.println("Step 2: Upload parts... [" + objectPath + "] [" + uploadId + "] " + partNumber + " " + uploadPartsize);
					}

					@Override
					public void caughtPartUploadException(String bucketName, String objectPath, String uploadId, int partNumber, long uploadPartsize, Exception e) {
						log.println("Step 2: Upload parts Error [" + objectPath + "] [" + uploadId + "] " + partNumber + " " + uploadPartsize);
						e.printStackTrace();

						// **此处可以记录失败分片以备后期重传此断点分片**
						// Do something
					}

					@Override
					public void afterPartUpload(String bucketName, String objectPath, String uploadId, int partNumber, long uploadPartsize, long startTime, long endTime) {
						log.println("Step 2: Upload parts OK ["
								+ objectPath
								+ "] ["
								+ uploadId
								+ "] "
								+ partNumber
								+ " "
								+ uploadPartsize
								+ "\t用时:"
								+ (((double) (endTime - startTime)) / 1000)
								+ " sec");
					}

					@Override
					public void complete(String bucketName, String objectPath, String uploadId, Long uploadedSize, long startTime, long endTime) {
						log.println("Step 3: Complete... [" + objectPath + "] [" + uploadId + "]");

						// 通過計算两侧文件的MD5验证上传数据是否正确
						// **此处验证为验证上传正确性代码-实际开发无需此操作**
						try {
							S3Object s3Object = s3Client.getObject(bucketName, objectPath);
							S3ObjectInputStream in = s3Object.getObjectContent();
							byte[] orginalFileMd5;
							orginalFileMd5 = DigestUtils.calcMD5(tobeUploadFile);
							byte[] objectFromHCPMd5 = DigestUtils.calcMD5(in);
							in.close();

							boolean equals = Arrays.equals(orginalFileMd5, objectFromHCPMd5);
							assertTrue(equals == true);
							System.out.println("***Upload " + objectPath + " Successfully!***");
						} catch (Exception e1) {
							e1.printStackTrace();
						}

					}
				});
		// =========================================================================================================================

		// =========================================================================================================================
		// // 以下为断点上传示例
		// // 上次未完成的分片上传id编号
		// String uploadId = "xxxxx";
		// MulitipartUploaderExecutor exec2 = new MulitipartUploaderExecutor(s3Client, bucketName, objectPath, tobeUploadFile, PART_SIZE, uploadId);
		// // 再次上传上传失败的分片编号例=第三片3
		// exec2.uploadPart(3);
		// // 合并分片
		// exec2.complete();
		// =========================================================================================================================

	}

}
