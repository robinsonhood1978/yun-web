/**
 * 
 */
package com.cms.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jxls.exception.ParsePropertyException;
import net.sf.jxls.report.ReportManager;
import net.sf.jxls.report.ReportManagerImpl;
import net.sf.jxls.transformer.XLSTransformer;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import com.cms.admin.goods.Goods;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.Record;

import edu.npu.fastexcel.ExcelException;
import edu.npu.fastexcel.FastExcel;
import edu.npu.fastexcel.Sheet;
import edu.npu.fastexcel.Workbook;

public class Excel {

	public static void basicWrite(File file) throws ExcelException{
		Workbook wb=FastExcel.createWriteableWorkbook(file);
		wb.open();
		Sheet sheet=wb.addSheet("SheetA");
		sheet.setCell(1, 2, "aaa");
		wb.close();
	}
	public static void streamWrite(File file) throws ExcelException{
		Workbook wb=FastExcel.createWriteableWorkbook(file);
		wb.open();
		Sheet sheet=wb.addStreamSheet("SheetA");
		for(int i=0;i<101;i++){
			sheet.addRow(new String[]{"aaa","bbb","ccc"});
		}
		wb.close();
	}
	
	public void basicWriteBig(File file) throws ExcelException{
		String basic="aaaaa中文aaa";
		Workbook wb=FastExcel.createWriteableWorkbook(file);
		wb.open();
		Sheet sheet=wb.addSheet("SheetA");
		for(int i=0;i<60000;i++){
			sheet.setCell(i, 0, basic+0+i);
			sheet.setCell(i, 1, basic+1+i);
			sheet.setCell(i, 2, basic+2+i);
			sheet.setCell(i, 3, basic+3+i);
			sheet.setCell(i, 4, basic+0+i);
			sheet.setCell(i, 5, basic+5+i);
			sheet.setCell(i, 6, basic+6+i);
			sheet.setCell(i, 7, basic+7+i);
		}
		wb.close();
	}
	public static void streamWriteBig(File file) throws ExcelException{
		Workbook wb=FastExcel.createWriteableWorkbook(file);
		wb.open();
		String basic="aaaaa中文aaa";
		Sheet sheet=wb.addStreamSheet("SheetA");
		String ss[]=new String[8];
		for(int i=0;i<30000;i++){
			ss[0]=basic+0+i;
			ss[1]=basic+1+i;
			ss[2]=basic+2+i;
			ss[3]=basic+3+i;
			ss[4]=basic+0+i;
			ss[5]=basic+5+i;
			ss[6]=basic+6+i;
			ss[7]=basic+7+i;
			sheet.addRow(ss);
		}
		wb.close();
	}
	public static void exportGoods(File file,List<Record> list) throws ExcelException{
		Workbook wb=FastExcel.createWriteableWorkbook(file);
		wb.open();
		Sheet sheet=wb.addStreamSheet("设备信息导出");
		sheet.addRow(new String[]{"设备名称","条码号","编号","规格型号","单位","库存下限","颜色","进货价","出货价","产地","设备简介"});
		String ss[]=new String[11];
		for(Record r:list){
			ss[0]=r.getStr("name");
			ss[1]=r.getStr("barcode");
			ss[2]=r.getStr("code");
			ss[3]=r.getStr("spec");
			ss[4]=r.getStr("unit");
			ss[5]=String.valueOf(r.getInt("warn"));
			ss[6]=r.getStr("color");
			ss[7]=String.valueOf(r.getFloat("in_price"));
			ss[8]=String.valueOf(r.getFloat("out_price"));
			ss[9]=r.getStr("origin");
			ss[10]=r.getStr("intro");
			sheet.addRow(ss);
		}
		wb.close();
	}
	public static void exportBarcodeGoods(File file,List<Record> list) throws ExcelException{
		Workbook wb=FastExcel.createWriteableWorkbook(file);
		wb.open();
		Sheet sheet=wb.addStreamSheet("设备信息导出");
		sheet.addRow(new String[]{"设备名称","条码号"});
		String ss[]=new String[2];
		for(Record r:list){
			ss[0]=r.getStr("name");
			ss[1]=r.getStr("barcode");
			sheet.addRow(ss);
		}
		wb.close();
	}
	public static void exportCheckinfo(File file,List<Record> list) throws ExcelException{
		Workbook wb=FastExcel.createWriteableWorkbook(file);
		wb.open();
		Sheet sheet=wb.addStreamSheet("盘点信息导出");
		sheet.addRow(new String[]{"盘点项目","仓库名称","设备名称","条码号","设备状态","权属状态","库存数量","盘点数量","盘点差异数量"});
		String ss[]=new String[9];
		String[] status={"正常","待修","报废"};
		String[] own_status={"在库","出库","借出","借入","调出","调入"};
		for(Record r:list){
			ss[0]=r.getStr("cname");
			ss[1]=r.getStr("sname");
			ss[2]=r.getStr("pname");
			ss[3]=r.getStr("barcode");
			ss[4]=status[r.getInt("status")];
			ss[5]=own_status[r.getInt("own_status")];
			ss[6]=String.valueOf(r.getInt("quantity"));
			ss[7]=String.valueOf(r.getInt("check_quantity"));
			ss[8]=StrUtil.null2Blank(String.valueOf(r.getLong("diff_quantity")));
			sheet.addRow(ss);
		}
		wb.close();
	}
	public static void exportStream(File file,List<Record> list) throws ExcelException{
		Workbook wb=FastExcel.createWriteableWorkbook(file);
		wb.open();
		Sheet sheet=wb.addStreamSheet("库存流水明细");
		sheet.addRow(new String[]{"仓库名称","设备名称","条码号","规格型号","出厂日期","操作前状态","操作后状态","操作类型","发生数量","操作人","操作时间"});
		String ss[]=new String[11];
		String[] status={"正常","待修","报废","借出","借入","出库"};
		String[] actype={"借出","还回","借入","还出","入库","出库","调出","调入"};
		for(Record r:list){
			ss[0]=r.getStr("sname");
			ss[1]=r.getStr("pname");
			ss[2]=r.getStr("barcode");
			ss[3]=r.getStr("spec");
			if(r.getStr("manufacture_date")==null)
				ss[4] = "";
			else 
				ss[4]=r.getStr("manufacture_date");
			ss[5]=status[r.getInt("before_status")];
			ss[6]=status[r.getInt("after_status")];
			ss[7]=actype[r.getInt("act_type")];
			ss[8]=String.valueOf(r.getInt("act_quantity"));
			ss[9]=r.getStr("uname");
			ss[10]=DateFmt.getStampTime(r.getTimestamp("create_time"));
			sheet.addRow(ss);
		}
		wb.close();
	}
	/*public static void jxlsInventory(String fileName,List<Record> list,List<Inventory> barcodeList, String realPath) throws ExcelException, ParsePropertyException, InvalidFormatException, SQLException, IOException{
		String tplPath = realPath+"/tpl/";
		String wholePath = realPath+fileName;		
		
		Map<String, Object> beans = new HashMap<String, Object>();
		ReportManager rm = new ReportManagerImpl( DbKit.getConnection(), beans );
		beans.put("rm", rm);
		beans.put("checkList", list);
		beans.put("barcodeList", barcodeList);
		XLSTransformer transformer = new XLSTransformer();
		String templateFileName = tplPath + "ware.xls";
		InputStream is = new FileInputStream(templateFileName);//模板文件流
        HSSFWorkbook workBook = (HSSFWorkbook) transformer.transformXLS(is, beans);         
		OutputStream os = new FileOutputStream(wholePath); //导出文件流
        workBook.write(os);  //写导出文件
        is.close(); 
        os.flush(); 
        os.close(); 
	}*/
	/*public static void jxlsNeed(String fileName,List<Record> list,List<Inventory> barcodeList, String realPath) throws ExcelException, ParsePropertyException, InvalidFormatException, SQLException, IOException{
		String tplPath = realPath+"/tpl/";
		String wholePath = realPath+fileName;		
		
		Map<String, Object> beans = new HashMap<String, Object>();
		ReportManager rm = new ReportManagerImpl( DbKit.getConnection(), beans );
		beans.put("rm", rm);
		beans.put("checkList", list);
		beans.put("barcodeList", barcodeList);
		XLSTransformer transformer = new XLSTransformer();
		String templateFileName = tplPath + "need.xls";
		InputStream is = new FileInputStream(templateFileName);//模板文件流
		HSSFWorkbook workBook = (HSSFWorkbook) transformer.transformXLS(is, beans);         
		OutputStream os = new FileOutputStream(wholePath); //导出文件流
		workBook.write(os);  //写导出文件
		is.close(); 
		os.flush(); 
		os.close(); 
	}*/
	public static void exportInventory(File file,List<Record> list) throws ExcelException{
		Workbook wb=FastExcel.createWriteableWorkbook(file);
		wb.open();
		Sheet sheet=wb.addStreamSheet("库存信息导出");
		sheet.addRow(new String[]{"仓库名称","设备名称","条码号","规格型号","设备状态","生产厂商","库存数量","库存下限","补货数量"});
		String ss[]=new String[9];
		String[] status={"正常","待修","报废","借出","借入","出库"};
		for(Record r:list){
			ss[0]=r.getStr("sname");
			ss[1]=r.getStr("pname");
			ss[2]=r.getStr("barcode");
			ss[3]=r.getStr("spec");
			ss[4]=status[r.getInt("status")];
			ss[5]=r.getStr("cname");
			ss[6]=String.valueOf(r.getInt("quantity"));
			ss[7]=String.valueOf(r.getInt("warn"));
			ss[8]=String.valueOf((r.getInt("warn")-r.getInt("quantity")<0)?0:(r.getInt("warn")-r.getInt("quantity")));
			sheet.addRow(ss);
		}
		wb.close();
	}
	public static void exportWarePrint(File file,List<Record> list) throws ExcelException{
		Workbook wb=FastExcel.createWriteableWorkbook(file);
		wb.open();
		Sheet sheet=wb.addStreamSheet("入库单条码信息");
		sheet.addRow(new String[]{"条码号","设备名称","规格型号","生产厂商","仓库名称","补货数量"});
		String ss[]=new String[6];
		for(Record r:list){
			ss[0]=r.getStr("barcode");
			ss[1]=r.getStr("pname");
			ss[2]=r.getStr("spec");
			ss[3]=r.getStr("cname");
			ss[4]=r.getStr("sname");
			ss[5]=String.valueOf(r.getInt("quantity"));
			sheet.addRow(ss);
		}
		wb.close();
	}
	public static void exportBarcode(File file,List<Record> list) throws ExcelException{
		Workbook wb=FastExcel.createWriteableWorkbook(file);
		wb.open();
		Sheet sheet=wb.addStreamSheet("sheet1");
		sheet.addRow(new String[]{"条码号","设备名称","规格型号","生产厂商","仓库名称","补货数量"});
		String ss[]=new String[6];
		for(Record r:list){
			ss[0]=r.getStr("barcode");
			ss[1]=r.getStr("pname");
			ss[2]=r.getStr("spec");
			ss[3]=r.getStr("cname");
			ss[4]=r.getStr("sname");
			ss[5]=String.valueOf((r.getInt("warn")-r.getInt("quantity")<0)?0:(r.getInt("warn")-r.getInt("quantity")));
			sheet.addRow(ss);
		}
		wb.close();
	}
	public static void printBarcode(File file,List<Record> list) throws ExcelException{
		Workbook wb=FastExcel.createWriteableWorkbook(file);
		wb.open();
		Sheet sheet=wb.addStreamSheet("sheet1");
		sheet.addRow(new String[]{"条码号","设备名称","规格型号","生产厂商","仓库名称","打印数量"});
		String ss[]=new String[6];
		for(Record r:list){
			ss[0]=r.getStr("barcode");
			ss[1]=r.getStr("pname");
			ss[2]=r.getStr("spec");
			ss[3]=r.getStr("cname");
			ss[4]=r.getStr("sname");
			ss[5]=String.valueOf((r.getInt("quantity")));
			sheet.addRow(ss);
		}
		wb.close();
	}
	public void testWriteRead() throws ExcelException{
		File f=new File("/home/yama/a.xls");
		write(f);
		read(f);
	}
	public void testWriteRead2() throws ExcelException{
		File f=new File("/home/yama/a.xls");
		writeStream(f);
		read(f);
	}
	public void testWriteEmpty() throws ExcelException{
		File f=new File("/home/yama/a2.xls");
		writeEmpty(f);
		read(f);
	}
	public void writeEmpty(File file) throws ExcelException{
		Workbook wb=FastExcel.createWriteableWorkbook(file);
		wb.open();
		Sheet sheet=wb.addSheet("SheetA");
		wb.close();
	}
	public void write(File file) throws ExcelException{
		Workbook wb=FastExcel.createWriteableWorkbook(file);
		wb.open();
		Sheet sheet=wb.addSheet("SheetA中文");
		for(int i=0;i<100;i++){
			sheet.setCell(i,(int)0, "aaa中文");
			sheet.setCell(i,(int)1, "bbb");
			sheet.setCell(i,(int)2, "ccc");
		}
		wb.close();
	}
	public void writeStream(File file) throws ExcelException{
		Workbook wb=FastExcel.createWriteableWorkbook(file);
		wb.open();
		Sheet sheet=wb.addStreamSheet("SheetA中文");
		for(int i=0;i<100;i++){
			sheet.addRow(new String[]{"aaa","vvv中文","ccc"});
		}
		wb.close();
	}
	public void read(File file) throws ExcelException{
		Workbook workBook;
		workBook = FastExcel.createReadableWorkbook(file);
		workBook.open();
		Sheet s;
		s = workBook.getSheet(0);
		System.out.println("SHEET:" + s);
		for (int i = s.getFirstRow(); i <s.getLastRow(); i++) {
			System.out.print(i + "#");
			for (int j = s.getFirstColumn(); j <s.getLastColumn(); j++) {
				System.out.print("," + s.getCell(i, j));
				s.getCell(i, j);
			}
			System.out.println();
		}
		workBook.close();
	}
}
