package cn.marwin.service.Imp;

import cn.marwin.entity.User;
import cn.marwin.service.TestService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
public class TestServiceImpl implements TestService {
    @Override
    public File createUserExcelFile(PageRequest pageRequest) {
        return null;
    }

    public static File createUserExcelFile(List<?> records) {
        if (records == null) {
            records = new ArrayList<>();
        }
        Workbook workbook = new XSSFWorkbook();
        //创建一个sheet，括号里可以输入sheet名称，默认为sheet0
        Sheet sheet = workbook.createSheet();
        Row row0 = sheet.createRow(0);
        int columnIndex = 0;
        row0.createCell(columnIndex).setCellValue("No");
        row0.createCell(++columnIndex).setCellValue("ID");
        row0.createCell(++columnIndex).setCellValue("提交人姓名");
        row0.createCell(++columnIndex).setCellValue("情绪描述");
        row0.createCell(++columnIndex).setCellValue("提交人所在部门");
        row0.createCell(++columnIndex).setCellValue("提交人工号");
        row0.createCell(++columnIndex).setCellValue("热搜名称");
        row0.createCell(++columnIndex).setCellValue("热搜排名");
        for (int i = 0; i < records.size(); i++) {
             User user = (User) records.get(i);
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < columnIndex + 1; j++) {
                row.createCell(j);
            }
            columnIndex = 0;
            row.getCell(columnIndex).setCellValue(i + 1);
            row.getCell(++columnIndex).setCellValue(user.getId());
            row.getCell(++columnIndex).setCellValue(user.getWeiNo());
            row.getCell(++columnIndex).setCellValue(user.getWeiName());
            row.getCell(++columnIndex).setCellValue(user.getAge());
            row.getCell(++columnIndex).setCellValue(user.getUserName());
            row.getCell(++columnIndex).setCellValue(user.getUserId());
            row.getCell(++columnIndex).setCellValue(user.getUserAge());
        }
        //调用PoiUtils工具包
        return PoiUtils.createExcelFile(workbook, "download_user");
    }

}
