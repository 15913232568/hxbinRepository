package com.ygsoft.lwh.controller;

import com.ygsoft.lwh.domain.User;
import com.ygsoft.lwh.service.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/excel")
public class ExcelController {
    private final static Logger LOGGER = LoggerFactory.getLogger(ExcelController.class);

    @Autowired
    private UserRepository userRepository;


    @GetMapping("/putExcel")
    public String putData() throws IOException {

        XSSFWorkbook wb = new XSSFWorkbook("C:\\Users\\huang\\Desktop\\qz.xlsx");
        Sheet sheet = wb.getSheet("Sheet1");
        List<User> list = new ArrayList<User>();
        for (Iterator iRows = sheet.rowIterator(); iRows.hasNext(); ) {//行
            XSSFRow row = (XSSFRow)iRows.next();
            User vo = new User();
            String uid = UUID.randomUUID().toString();
            vo.setId(uid);
            for (Iterator iCells = row.cellIterator(); iCells.hasNext(); ) {//列
                XSSFCell cell = (XSSFCell)iCells.next();
                CellType type = cell.getCellType();
                String value = "";
                if (type == CellType.BOOLEAN){
                    //得到Boolean对象的方法
                    value = cell.getBooleanCellValue() + "";
                }else if (type == CellType.NUMERIC){
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        //读取日期格式
                        //value =  + "";
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                        value = simpleDateFormat.format(cell.getDateCellValue());
                    } else {
                        //读取数字
                        value = (long)cell.getNumericCellValue()+"";
                    }
                }else if (type == CellType.FORMULA){
                    value = cell.getCellFormula() + "";
                }else if (type == CellType.STRING){
                    value = cell.getRichStringCellValue().toString() + "";
                }

                String eCellName = CellReference.convertNumToColString(cell.getColumnIndex());
                if (StringUtils.isNotEmpty(value)){
                    switch (eCellName){
                        case "A":
                            vo.setName(value);
                            break;
                        case "B":
                            vo.setAge(value);
                            break;
                        case "C":
                            vo.setNumber(Integer.valueOf(value));
                            break;

                        default:
                            break;
                    }
                }
                 }
                list.add(vo);

           }
        userRepository.saveAll(list);
        return "11111111111";
        }
    }
