package com.ygsoft.lwh.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.db.dialect.impl.MysqlDialect;
import com.google.gson.Gson;
import com.ygsoft.lwh.entity.MyResultVO;
import com.ygsoft.lwh.entity.MySqlVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sun.security.jgss.GSSCaller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class MyService {

    private RestTemplate rest = new RestTemplate();
    //事项数据保存地址
    private final static String URL_TEST = "http://19.54.117.12:8085/jwface/qh/event/saveObj";
    //本地数据文件
    private final static String FILE_PATH = "C:\\Users\\lwh\\Desktop\\event.xlsx";
    private final static Gson gson = new Gson();

    /**
     * 将excel数据上传到金湾系统内
     * @return
     */
    public String putData(){
        try {
            //获取excel数据
            //1级节点数据
            List<MySqlVO> list1 = getExcelData(FILE_PATH,"1级菜单",1);
            //2级节点数据
            List<MySqlVO> list2 = getExcelData(FILE_PATH,"2级菜单",1);
            //3级节点数据
            List<MySqlVO> list3 = getExcelData(FILE_PATH,"3级菜单",1);
            //4级节点数据
            List<MySqlVO> list4 = getExcelData(FILE_PATH,"4级菜单",1);
            //1级菜单的pid全部赋值0
            for (MySqlVO vo : list1){
                vo.setPid("0");
                //vo.setTreeName(vo.getEventName());
                //vo.setTreeCode("001");
            }
            //填充树数据
            //1级节点加入2级节点数据
            fillTree(list1,list2,1);
            //2级节点加入3级节点数据
            fillTree(list2,list3,2);
            //3级节点加入4级节点数据
            fillTree(list3,list4,3);
            //tree转list
            List<MySqlVO> list = new ArrayList<MySqlVO>();
            //将list1这是最顶层的整颗大树转成list列表
            tree2List(list1,list);
            //putFile(list);//生成event.txt
            //将数据上报到金湾系统内部
            for (MySqlVO vo : list){
                postData(vo);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return "ok";
    }

    /**
     * 生成event.txt
     * @param list
     * @throws Exception
     */
    private void putFile(List<MySqlVO> list ) throws Exception{
        String names = obj2Name(list.get(0));
        FileOutputStream fos = new FileOutputStream(new File("C:\\Users\\lwh\\Desktop\\event.txt"));
        //写入中文字符时解决中文乱码问题
        OutputStreamWriter osw=new OutputStreamWriter(fos, "UTF-8");
        BufferedWriter bw=new BufferedWriter(osw);
        bw.write(names + "\r\n");
        for (MySqlVO vo : list){
            String str = toFile(vo);
            bw.write(str + "\r\n");
            bw.append(str + "\r\n");
        }
        bw.flush();
        bw.close();
    }

    /**
     * 将vo转成一行string字符串，添加逗号分隔符
     * @param vo
     * @return
     * @throws Exception
     */
    private String toFile(MySqlVO vo) throws Exception{
        List<Object> list = obj2List(vo);
        String str = "";
        for (Object obj : list){
            if (ObjectUtil.isNotNull(obj)){
                str += obj.toString() + ",";
            }else{
                str += ",";
            }
        }
        return str.substring(0,str.length() - 1);
    }

    /**
     * 将对象的所有成员变量的数据存储到list中
     * @param obj
     * @return
     * @throws IllegalAccessException
     */
    private List<Object> obj2List(Object obj) throws IllegalAccessException{
        List<Object> list = new ArrayList<Object>();
        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(obj);
            list.add(value);
        }
        return list;
    }

    /**
     * 将对象的所有变量名称放到string字符串一行中
     * @param obj
     * @return
     * @throws IllegalAccessException
     */
    private String obj2Name(Object obj) throws IllegalAccessException{
        String list = "";
        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            list += fieldName + ",";
        }
        return list.substring(0,list.length() - 1);
    }

    /**
     * 树结构转list列表
     * @param listRoot
     * @param list
     */
    private void tree2List(List<MySqlVO> listRoot,List<MySqlVO> list){
        if (!CollectionUtil.isEmpty(listRoot)){
            for(MySqlVO vo : listRoot){
                list.add(toVO(vo));
                tree2List(vo.getChilds(),list);
            }
        }
    }

    /**
     * 将vo对象重新复制，去除child节点的树结构引用
     * @param vo
     * @return
     */
    private MySqlVO toVO(MySqlVO vo){
        MySqlVO vo1 = new MySqlVO();
        vo1.setGid(vo.getGid());
        vo1.setIsLeaf(vo.getIsLeaf());
        vo1.setIsTb(vo.getIsTb());
        vo1.setHeat(vo.getHeat());
        vo1.setOffice(vo.getOffice());
        vo1.setIsFollowHoliday(vo.getIsFollowHoliday());
        vo1.setCanSundays(vo.getCanSundays());
        vo1.setCanSaturdays(vo.getCanSaturdays());
        vo1.setPmEnd(vo.getPmEnd());
        vo1.setPmBegin(vo.getPmBegin());
        vo1.setAmEnd(vo.getAmEnd());
        vo1.setAmBegin(vo.getAmBegin());
        vo1.setIsNetEvent(vo.getIsNetEvent());
        vo1.setNumPrefix(vo.getNumPrefix());
        vo1.setIsGetNo(vo.getIsGetNo());
        vo1.setPlatform(vo.getPlatform());
        vo1.setEventNo(vo.getEventNo());
        vo1.setEventName(vo.getEventName());
        vo1.setDepartName(vo.getDepartName());
        vo1.setPid(vo.getPid());
        vo1.setAllChildrenLeaf(vo.isAllChildrenLeaf());
        vo1.setCityReservation(vo.getCityReservation());
        vo1.setCreateTime(vo.getCreateTime());
        vo1.setDelLimitIds(vo.getDelLimitIds());
        vo1.setDelPlatformIds(vo.getDelPlatformIds());
        vo1.setDepartId(vo.getDepartId());
        vo1.setEventType(vo.getEventType());
        vo1.setIsCanTakeNum(vo.getIsCanTakeNum());
        vo1.setIsImmediately(vo.getIsImmediately());
        vo1.setJwTbEventTbTypePOList(vo.getJwTbEventTbTypePOList());
        vo1.setLimitPOList(vo.getLimitPOList());
        vo1.setOldEventName(vo.getOldEventName());
        vo1.setPlatformIds(vo.getPlatformIds());
        vo1.setPName(vo.getPName());
        vo1.setSortKey(vo.getSortKey());
        vo1.setSpecialMark(vo.getSpecialMark());
        vo1.setStatus(vo.getStatus());
        vo1.setTreeCode(vo.getTreeCode());
        vo1.setTreeName(vo.getTreeName());
        vo1.setUpdateTime(vo.getUpdateTime());
        vo1.setUpRemark(vo.getUpRemark());
        return vo1;
    }

    /**
     * 父节点挂子节点数据
     * @param listRoot 父节点
     * @param listChilds 子节点数据
     * @param cellType 1代表1级节点挂2级节点，2代表2级节点挂3级节点，3代表3级节点挂4级节点
     * @return
     */
    private boolean fillTree(List<MySqlVO> listRoot,List<MySqlVO> listChilds,int cellType){
        for (MySqlVO vo : listRoot){
            String pibName = vo.getEventName();

            if (StringUtils.isEmpty(pibName)){
                return false;
            }

            for(MySqlVO vo1 : listChilds){
                String pibName1 = "";
                if (3 == cellType){
                    pibName1 = vo1.getDepartName();
                }else if (2 == cellType){
                    pibName1 = vo1.getPlatform();
                }else if (1 == cellType){
                    pibName1 = vo1.getOffice();
                }
                if (StringUtils.isEmpty(pibName1)){
                    return false;
                }
                if (pibName.equals(pibName1)){
                    if (StringUtils.isEmpty(vo1.getGid())){
                        vo1.setGid(UUID.randomUUID().toString());
                    }
                    if (StringUtils.isEmpty(vo.getGid())){
                        vo.setGid(UUID.randomUUID().toString());
                    }
                    if (CollectionUtil.isEmpty(vo.getChilds())){
                        vo.setChilds(new ArrayList<MySqlVO>());
                    }
                    vo1.setPid(vo.getGid());
                    vo1.setPName(vo.getEventName());
                    //vo1.setTreeName(vo.getTreeName() + vo1.getEventName());
                    //vo1.setTreeCode(vo.getTreeCode() + String.format("%03d",vo.getChilds().size() + 1));
                    vo.getChilds().add(vo1);
                }
            }
        }
        return true;
    }


    /**
     * 根据excel中的sheet页名称获取当中的数据，且起跳标题
     * @param filePath 文件路径
     * @param sheetName sheet页名称
     * @param firstRowData 跳过的行数
     * @return
     * @throws Exception
     */
    private List<MySqlVO> getExcelData(String filePath,String sheetName,int firstRowData) throws Exception{
//1.创建Excel对象
        XSSFWorkbook wb = new XSSFWorkbook(filePath);
//2.创建Sheet对象
        Sheet sheet = wb.getSheet(sheetName);
        List<MySqlVO> list = new ArrayList<MySqlVO>();
        int count = 0;

        for (Iterator iRows = sheet.rowIterator(); iRows.hasNext(); ) {//行
            XSSFRow row = (XSSFRow)iRows.next();
            if (count >= firstRowData){
                MySqlVO vo = new MySqlVO();
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
                            value = (long)cell.getNumericCellValue() + "";
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
                                vo.setDepartName(value);
                                break;
                            case "B":
                                vo.setEventName(value);
                                break;
                            case "C":
                                vo.setEventNo(value);
                                break;
                            case "D":
                                vo.setPlatform(value);
                                break;
                            case "E":
                                vo.setIsGetNo(Integer.parseInt(value));
                                break;
                            case "F":
                                vo.setNumPrefix(value);
                                break;
                            case "G":
                                vo.setIsNetEvent(Integer.parseInt(value));
                                break;
                            case "H":
                                vo.setAmBegin(value);
                                break;
                            case "I":
                                vo.setAmEnd(value);
                                break;
                            case "J":
                                vo.setPmBegin(value);
                                break;
                            case "K":
                                vo.setPmEnd(value);
                                break;
                            case "L":
                                if ("周六".equals(value)){
                                    vo.setCanSaturdays(1);
                                    vo.setCanSundays(0);
                                    vo.setIsFollowHoliday(1);
                                }else if("否".equals(value)){
                                    vo.setCanSaturdays(0);
                                    vo.setCanSundays(0);
                                    vo.setIsFollowHoliday(1);
                                }
                                break;
                            case "M":
                                vo.setOffice(value);
                                break;
                            case "N":
                                vo.setGid(value);
                                break;
                            case "O":
                                vo.setHeat(Integer.parseInt(value));
                                break;
                            case "P":
                                vo.setIsTb(Integer.parseInt(value));
                                break;
                            case "Q":
                                vo.setIsLeaf(Integer.parseInt(value));
                                break;
                            default:
                                break;
                        }
                    }
                }
                list.add(vo);
            }
            count++;
        }
        System.out.println("count = " + count);
        wb.close();

        return list;
    }

    /**
     * 调用pst接口上报数据到金湾系统内部
     * @param vo
     */
    private void postData(MySqlVO vo){

        String inStr = gson.toJson(vo);
        System.out.println("输入 === " + inStr);

//创建头部
        HttpHeaders headers = new HttpHeaders();

//设置提交格式
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);

//将“current_tenant”塞入请求头，把值给tenantid
//headers.add("current_tenant",tenantId);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());

//把入参类型为“List”的“locationHistory”，放入HttpEntity并放入请求头“headers”，准备请求
        //HttpEntity<MySqlVO> formEntity = new HttpEntity<MySqlVO>(vo, headers);
        HttpEntity<String> formEntity = new HttpEntity<String>(inStr, headers);

//通过postForObject开始请求，塞入：请求路径，请求参数，返回类型；
        ResponseEntity<MyResultVO> resultVO = rest.postForEntity(URL_TEST, formEntity,MyResultVO.class);
        //MyResultVO resultVO = gson.fromJson(json, MyResultVO.class);
        if (0 != resultVO.getBody().getCode()){
            System.out.println("错误输出 === " + gson.toJson(resultVO.getBody()));
        }
    }

}
