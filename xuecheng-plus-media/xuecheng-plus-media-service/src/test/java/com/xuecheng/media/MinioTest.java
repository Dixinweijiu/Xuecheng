package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jxchen
 * @version 1.0
 * @description TODO
 * @date 2023/9/16 21:32
 */
public class MinioTest {

    static MinioClient minioClient = MinioClient.builder()
            .endpoint("http://192.168.101.65:9000")
            .credentials("minioadmin", "minioadmin")
            .build();

    //上传文件
    @Test
    public void upload() {
        try {
            ContentInfo mimeTypeMatch = ContentInfoUtil.findMimeTypeMatch(".docx");

            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .object("001/摘要.docx")
                    .filename("E:\\temp\\时序数据论文\\近3年部分学界论文\\摘要.docx")
//                    .contentType(mimeTypeMatch.getMimeType())
                    .contentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                    .build();
            minioClient.uploadObject(testbucket);
            System.out.println("上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("上传失败");
        }
    }

    //删除文件
    @Test
    public void delete() {
        try {
            RemoveObjectArgs testbucket = RemoveObjectArgs.builder()
                    .bucket("testbucket")
                    .object("001/摘要.docx")
                    .build();
            minioClient.removeObject(testbucket);
            System.out.println("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("删除失败");
        }
    }

    //查询文件 从minio下载
    @Test
    public void getFile() {
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket("testbucket")
                    .object("001/摘要.docx")
                    .build();
            GetObjectResponse object = minioClient.getObject(getObjectArgs);
            FileOutputStream fileOutputStream = new FileOutputStream(new File("D:\\Learning Projects\\Java微服务-学成\\day05 媒资管理 Nacos Gateway MinIO\\下载\\摘要.docx"));
            IOUtils.copy(object, fileOutputStream);
            //md5校验
            String s1 = DigestUtils.md5Hex(Files.newInputStream(new File("D:\\Learning Projects\\Java微服务-学成\\day05 媒资管理 Nacos Gateway MinIO\\下载\\摘要.docx").toPath()));
            String s2 = DigestUtils.md5Hex(Files.newInputStream(new File("E:\\temp\\时序数据论文\\近3年部分学界论文\\摘要.docx").toPath()));
            if (s1.equals(s2)) {
                System.out.println("查询成功");
            } else {
                // inputStream因网络问题不稳定，导致md5不同
                System.out.println("校验失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("查询失败");
        }
    }

    @Test
    public void uploadChunk() {
        //路径
        String chunkFolderPath = "D:\\Develop\\temp\\chunks\\";
        File chunkFolder = new File(chunkFolderPath);
        //获取分块文件
        File[] files = chunkFolder.listFiles();
        //循环上传
        for (int i = 0; i < files.length; i++) {
            try {
                UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                        .bucket("testbucket")
                        .object("chunk/" + i)
                        .filename(files[i].getAbsolutePath())
                        .build();
                minioClient.uploadObject(uploadObjectArgs);
                System.out.println("上传分块成功" + i);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("上传分块失败" + i);
            }
        }
    }

    @Test
    public void testMerge() throws Exception{
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(2)
                .map(i -> ComposeSource.builder()
                        .bucket("testbucket")
                        .object("chunk/".concat(Integer.toString(i)))
                        .build())
                .collect(Collectors.toList());
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket("testbucket")
                .object("merge01.mmp4")
                .sources(sources)
                .build();
        minioClient.composeObject(composeObjectArgs);
    }


}
