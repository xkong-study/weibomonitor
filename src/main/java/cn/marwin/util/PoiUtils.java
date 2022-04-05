package cn.marwin.util;

import cn.marwin.util.IOUtils;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.*;
/**
 * POI相关操作
 * @author Louis
 * @date Jan 14, 2019
 */
public class PoiUtils {

    /**
     * 生成Excel文件
     * @param workbook
     * @param fileName
     * @return
     */
    public static File createExcelFile(Workbook workbook, String fileName) {
        OutputStream stream = null;
        File file = null;
        try {            //用了createTempFile，这是创建临时文件，系统会自动给你的临时文件编号，所以后面有号码，你用createNewFile的话就完全按照你指定的名称来了
            file = File.createTempFile(fileName, ".xlsx");
            stream = new FileOutputStream(file.getAbsoluteFile());
            workbook.write(stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {            //这里调用了IO工具包控制开关
            IOUtils.closeQuietly(workbook);
            IOUtils.closeQuietly(stream);
        }
        return file;
    }
}