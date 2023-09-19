package com.xuecheng.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author jxchen
 * @version 1.0
 * @description 测试分块文件处理
 * @date 2023/9/18 10:05
 */
public class BigFileTest {

    //分块上传
    @Test
    public void testChunk() throws IOException {
        //源文件
        File sourceFile = new File("D:\\Develop\\temp\\sea.mp4");
        //分块文件存储路径
        String chunkFilePath = "D:\\Develop\\temp\\chunks\\";
        //分块文件大小：1MB
        int chunkSize = 1024 * 1024 * 5;
        //分块文件个数
        int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        //使用流从源文件读取数据，向分块文件中写入数据
        RandomAccessFile raf_r = new RandomAccessFile(sourceFile, "r");
        //缓存区
        byte[] bytes = new byte[1024];
        for (int i = 0; i < chunkNum; i++) {
            File chunkFile = new File(chunkFilePath + i);
            //分块文件写入流
            RandomAccessFile raf_rw = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            while ((len = raf_r.read(bytes)) != -1) {
                raf_rw.write(bytes, 0, len);
                if (chunkFile.length() >= chunkSize) {
                    break;
                }
            }
            raf_rw.close();
            System.out.println("分块完成");
        }
        raf_r.close();
    }

    //合并文件块
    @Test
    public void testMerge() throws IOException{
        //块文件目录
        File chunkFolder = new File("D:\\Develop\\temp\\chunks\\");
        //原始文件
        File sourceFile = new File("D:\\Develop\\temp\\sea.mp4");
        //合并后的文件
        File mergeFile = new File("D:\\Develop\\temp\\sea_merge.mp4");
        //取出所有分块文件
        File[] files = chunkFolder.listFiles();
        //将数组转成list
        List<File> fileList = Arrays.asList(files);
        //排序
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });
        //写流
        RandomAccessFile raf_rw = new RandomAccessFile(mergeFile, "rw");
        //缓冲区
        byte[] bytes = new byte[1024];
        //遍历分块文件，向合并后的文件中写入
        for (File file: fileList) {
            //读分块的流
            RandomAccessFile raf_r = new RandomAccessFile(file, "r");
            int len = -1;
            while ((len = raf_r.read(bytes)) != -1) {
                raf_rw.write(bytes, 0 ,len);
            }
            raf_r.close();
        }
        raf_rw.close();
        System.out.println("合并完成");
        //合并完成后进行md5校验
        FileInputStream fileInputStream_merge = new FileInputStream(mergeFile);
        FileInputStream fileInputStream_source = new FileInputStream(sourceFile);
        String mergeMd5 = DigestUtils.md5Hex(fileInputStream_merge);
        String sourceMd5 = DigestUtils.md5Hex(fileInputStream_source);
        if (mergeMd5.equals(sourceMd5)) {
            System.out.println("校验正确");
        } else {
            System.out.println("校验失败");
        }
    }
}
