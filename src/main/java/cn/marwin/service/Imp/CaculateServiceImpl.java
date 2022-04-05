package cn.marwin.service.Imp;

import cn.marwin.entity.Weibo;
import cn.marwin.service.CaculateService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.marwin.service.CaculateService;
import cn.marwin.util.PoiUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class CaculateServiceImpl implements CaculateService {
    @Override
    public File createWeiboExcelFile(PageRequest pageRequest) {
        return null;
    }

    public static File createWeiboExcelFile(List<?> records) {
        if (records == null) {
            records = new ArrayList<>();
        }
        Workbook workbook = new XSSFWorkbook();
        //创建一个sheet，括号里可以输入sheet名称，默认为sheet0
        Sheet sheet = workbook.createSheet();
        Row row0 = sheet.createRow(0);
        int columnIndex = 0;
        row0.createCell(columnIndex).setCellValue("id");
        row0.createCell(++columnIndex).setCellValue("statuses_count");
        row0.createCell(++columnIndex).setCellValue("follow_count");
        row0.createCell(++columnIndex).setCellValue("followers_count");
        row0.createCell(++columnIndex).setCellValue("reposts_count");
        row0.createCell(++columnIndex).setCellValue("comments_count");
        row0.createCell(++columnIndex).setCellValue("attitudes_count");
        for (int i = 0; i < records.size(); i++) {
            Weibo weibo = (Weibo) records.get(i);
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < columnIndex + 1; j++) {
                row.createCell(j);
            }
            columnIndex = 0;
            row.getCell(columnIndex).setCellValue(i + 1);
            row.getCell(++columnIndex).setCellValue(weibo.getId());
            row.getCell(++columnIndex).setCellValue(weibo.getStatuses_count());
            row.getCell(++columnIndex).setCellValue(weibo.getFollow_count());
            row.getCell(++columnIndex).setCellValue(weibo.getFollowers_count());
            row.getCell(++columnIndex).setCellValue(weibo.getReposts_count());
            row.getCell(++columnIndex).setCellValue(weibo.getComments_count());
            row.getCell(++columnIndex).setCellValue(weibo.getAttitudes_count());
        }
        //调用PoiUtils工具包
        return PoiUtils.createExcelFile(workbook, "download_user");
    }

}
