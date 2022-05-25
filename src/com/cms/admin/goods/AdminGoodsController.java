package com.cms.admin.goods;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.krysalis.barcode4j.impl.upcean.EAN13LogicImpl;

import com.cms.admin.category.Category;
import com.cms.admin.company.Company;
import com.cms.admin.goods.GoodsAtt;
import com.cms.admin.user.User;
import com.cms.util.DateFmt;
import com.cms.util.Excel;
import com.cms.util.StrUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;

import edu.npu.fastexcel.ExcelException;
import edu.npu.fastexcel.FastExcel;
import edu.npu.fastexcel.Sheet;
import edu.npu.fastexcel.Workbook;

/*import edu.npu.fastexcel.ExcelException;
import edu.npu.fastexcel.FastExcel;
import edu.npu.fastexcel.Sheet;
import edu.npu.fastexcel.Workbook;
import edu.npu.fastexcel.biff.parser.ParserException;
import edu.npu.fastexcel.compound.io.ReadException;*/

@Before(GoodsInterceptor.class)
public class AdminGoodsController extends Controller {
	private static SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmsss");
	private static SimpleDateFormat ym = new SimpleDateFormat("yyyyMM");
	private static String uploadroot = "/upload/";
	private static String root;
	public void att() {
		String savefilename="";
		String filedataFileName ="";
		StringBuffer files=new StringBuffer();
		UploadFile upfile = this.getFile();
		int num = getParaToInt("attachmentNum");
		System.out.println(num+"#");
		boolean suc=false;
		if(upfile!=null){
			String realPath = this.getRequest().getRealPath("/");
			int i=1;
			System.out.println(realPath+"#");
			
			String path=ym.format(new Date());
			File dir = new File(realPath+uploadroot+path);
			if(!dir.exists()){
				dir.mkdir();
			}
			File file = upfile.getFile();
			filedataFileName = upfile.getOriginalFileName();
			savefilename = uploadroot+path+"/"+ sf.format(new Date())+ filedataFileName.substring(filedataFileName.lastIndexOf("."));
			if(file != null){
				upfile.getFile().renameTo(new File(realPath + savefilename));
				suc=true;
				files.append(realPath+savefilename);
			}
				
		}else{
			setAttr("error", "请选择需要上传的文档");
		}
		setAttr("attachmentNum", num);
		setAttr("attachmentPath", savefilename);
		setAttr("attachmentName", filedataFileName);
		render("att.html");
	}
	public void tnatt() {
		String savefilename="";
		String filedataFileName ="";
		StringBuffer files=new StringBuffer();
		UploadFile upfile = this.getFile();
		int num = getParaToInt("attachmentThumbnailNum");
		System.out.println(num+"#");
		boolean suc=false;
		if(upfile!=null){
			String realPath = this.getRequest().getRealPath("/");
			int i=1;
			System.out.println(realPath+"#");
			
			String path=ym.format(new Date());
			File dir = new File(realPath+uploadroot+path);
			if(!dir.exists()){
				dir.mkdir();
			}
			File file = upfile.getFile();
			filedataFileName = upfile.getOriginalFileName();
			savefilename = uploadroot+path+"/"+ sf.format(new Date())+ filedataFileName.substring(filedataFileName.lastIndexOf("."));
			if(file != null){
				upfile.getFile().renameTo(new File(realPath + savefilename));
				suc=true;
				files.append(realPath+savefilename);
			}
			
		}else{
			setAttr("error", "请选择需要上传的文档");
		}
		setAttr("attachmentThumbnailNum", num);
		setAttr("attachmentThumbnailPath", savefilename);
		setAttr("attachmentThumbnailName", filedataFileName);
		render("tnatt.html");
	}
	public void index() {
		String sql = "from goods g,category c,company p where g.cat_id=c.id and g.company_id = p.id";
		String pname = getPara("pname");
		String barcode = getPara("barcode");
		String spec = getPara("spec");
		String exp = getPara("export");
		int cat_id = 0;
		int company_id = 0 ;
		if(getPara("cat_id")!=null){
			cat_id = getParaToInt("cat_id");
		}
		if(getPara("company_id")!=null){
			company_id = getParaToInt("company_id");
		}
		
		if(pname!=null && !"".equals(pname.trim())){
			sql += " and g.name like '%"+pname.trim()+"%'";
			setAttr("pname",pname);
		}
		if(spec!=null && !"".equals(spec.trim())){
			sql += " and g.spec like '%"+spec.trim()+"%'";
			setAttr("spec",spec);
		}
		if(cat_id>0){
			sql += " and g.cat_id ="+cat_id;
		}
		if(company_id>0){
			sql += " and g.company_id ="+company_id;
		}
		
		sql += " order by id desc";
		String realPath = this.getRequest().getRealPath("/");
		if(exp!=null && "1".equals(exp)){
			List<Record> goodsList = Db.find("select g.*,c.name as category,p.name as company "+sql);
			String file = DateFmt.getCurrentTimeName(0)+".xls";
			String path = "/download/"+file;
			setAttr("path",path);
			setAttr("file",file);
			try {
				Excel.exportGoods(new File(realPath+path),goodsList);
			} catch (ExcelException e) {
				e.printStackTrace();
			}
			render("exp.html");
		}
		else if(exp!=null && "2".equals(exp)){
			List<Record> goodsList = Db.find("select g.*,c.name as category,p.name as company "+sql);
			String file = DateFmt.getCurrentTimeName(0)+".xls";
			String path = "/download/"+file;
			setAttr("path",path);
			setAttr("file",file);
			try {
				Excel.exportBarcodeGoods(new File(realPath+path),goodsList);
			} catch (ExcelException e) {
				e.printStackTrace();
			}
			render("exp.html");
		}
		else{
			setAttr("cat_id",cat_id);
			setAttr("company_id",company_id);
			setAttr("catList", Category.dao.find("SELECT node.* FROM category node, category parent WHERE node.lft >= parent.lft AND node.rgt <= parent.rgt and parent.parent_id is null ORDER BY node.lft"));
			setAttr("companyList", Company.dao.find("select * from company"));
			setAttr("goodsPage", Goods.dao.paginate(getParaToInt(0, 1), 10, "select g.*,c.name as category,p.name as company", sql));
			render("goods.html");
		}
	}
	
	public void add() {
		setAttr("catList", Category.dao.find("SELECT node.* FROM category node, category parent WHERE node.lft >= parent.lft AND node.rgt <= parent.rgt and parent.parent_id is null ORDER BY node.lft"));
		setAttr("companyList", Company.dao.find("select * from company"));
	}
	public void batch() {
		setAttr("catList", Category.dao.find("SELECT node.* FROM category node, category parent WHERE node.lft >= parent.lft AND node.rgt <= parent.rgt and parent.parent_id is null ORDER BY node.lft"));
		setAttr("companyList", Company.dao.find("select * from company"));
	}
	
	@Before(GoodsValidator.class)
	public void save() {
		final String name = getPara("goods.name");
		final String[] attachmentPaths = getParaValues("attachmentPaths");
		final String[] attachmentNames = getParaValues("attachmentNames");
		final String[] attachmentThumbnailPaths = getParaValues("attachmentThumbnailPaths");
		final String[] attachmentThumbnailNames = getParaValues("attachmentThumbnailNames");
		List<Goods> list = Goods.dao.find("select * from goods where name=?",name);
		if(list.size()>0){
			setAttr("nameMsg", "设备名称重名！请更正。");
			forwardAction("/admin/goods/add");
		}
		else{
			boolean succeed = Db.tx(
				new IAtom(){
					public boolean run() throws SQLException {
						boolean count = getModel(Goods.class).set("name", name.trim())
						.set("create_actor", (getSessionAttr("user")==null)?1:((User)getSessionAttr("user")).getInt("id"))
						.save();
						
						long nextId = Db.queryLong("select max(id) from goods");
						// 保存附件
						if (attachmentPaths != null && attachmentPaths.length > 0) {
							for (int i = 0, len = attachmentPaths.length; i < len; i++) {
								if (!StrUtil.isBlank(attachmentPaths[i]) && !StrUtil.isBlank(attachmentNames[i])) {
									Db.update("insert into goods_att (good_id,name,url,thumbnailname,thumbnailurl,priority) values ("
											+nextId+",'"+attachmentNames[i]+"','"+attachmentPaths[i]+"','"
											+attachmentThumbnailNames[i]+"','"+attachmentThumbnailPaths[i]+"',"+i+")");
								}
							}
						}
						return count ;
					}
				}
			);
			if(succeed){
				setAttr("resultMsg", "数据保存成功。");
				forwardAction("/admin/goods");
			}
			else{
				setAttr("resultMsg", "数据保存失败，请重新录入。");
				forwardAction("/admin/goods/add");
			}
		}
	}
	public void bsave() {
		
		String savefilename="";
		StringBuffer files=new StringBuffer();
		UploadFile upfile = this.getFile("upExcel");
		int catId = getParaToInt("goods.cat_id");
		int companyId = getParaToInt("goods.company_id");
		System.out.println(catId+"#"+companyId);
		boolean suc=false;
		if(upfile!=null){
			if(root==null)
				root=this.getRequest().getContextPath();
			String realPath = this.getRequest().getRealPath("/");
			int i=1;
			System.out.println(root+"#"+realPath+"#");
			
			File file = upfile.getFile();
			String filedataFileName = upfile.getOriginalFileName();
			savefilename = uploadroot+ sf.format(new Date())+ filedataFileName.substring(filedataFileName.lastIndexOf("."));
			if(file != null){
				upfile.getFile().renameTo(new File(realPath + savefilename));
				suc=true;
				files.append(realPath+savefilename);
			}
				
		}else{
			setAttr("resultMsg", "请选择需要上传的Excel文档");
			forwardAction("/admin/goods/batch");
		}
		if(suc){
			
			boolean count=false;
			try {
				count = readGoodsExcel(files.toString(),catId,companyId);
			} catch (ExcelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
						
			if(count){
				String[] log = ((String)getAttr("log")).split("#");
				setAttr("resultMsg", "本次共提交<font color='black'> "+log[0]+" </font>条数据，" +
						"其中<font color='red'> "+log[1]+"</font> 条由于设备库中已存在相同设备" +
								"名称而无法导入，<font color='black'> "+log[2]+"</font> 条导入成功。");
				forwardAction("/admin/goods");
			}
			else{
				setAttr("resultMsg", "数据保存失败，请重新录入。");
				forwardAction("/admin/goods/batch");
			}
		}
		//this.renderText("{'err':'" + errmsg + "','msg':'" +files.toString()+ "','suc':"+suc+"}");
		
	}
	/*
	 * CREATE TABLE `goods` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `cat_id` int(11) NOT NULL COMMENT '目录ID',
  `name` varchar(100) NOT NULL COMMENT '名称',
  `code` varchar(30) DEFAULT NULL COMMENT '编号',
  `barcode` varchar(30) DEFAULT NULL COMMENT '条码号',
  `spec` varchar(100) DEFAULT NULL COMMENT '规格型号',
  `unit` varchar(10) DEFAULT NULL COMMENT '单位',
  `color` varchar(10) DEFAULT NULL COMMENT '颜色',
  `in_price` float(10,8) DEFAULT NULL COMMENT '进货价',
  `out_price` float(10,8) DEFAULT NULL COMMENT '出货价',
  `warn` int(11) NOT NULL DEFAULT '0' COMMENT '库存下限',
  `intro` varchar(200) DEFAULT NULL COMMENT '备注',
  `origin` varchar(30) DEFAULT NULL COMMENT '产地',
  `company_id` int(11) NOT NULL DEFAULT '0' COMMENT '生产厂商',
  `create_actor` int(11) NOT NULL COMMENT '录入人员',
  `update_actor` int(11) DEFAULT NULL COMMENT '修改人员',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
  `update_time` datetime DEFAULT '0000-00-00 00:00:00' COMMENT '修改时间',
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COMMENT='设备表'
	 * */
	public boolean readGoodsExcel(String filename, final int catId, final int companyId) throws ExcelException  {
		final Workbook workBook;
		workBook = FastExcel.createReadableWorkbook(new File(filename));
		workBook.open();
		boolean succeed = Db.tx(
			new IAtom(){
				public boolean run() throws SQLException {
					
					Sheet s = null;
					try {
						s = workBook.getSheet(0);
					} catch (ExcelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("SHEET:"+s);
					int j=0;
					int actorId = (getSessionAttr("user")==null)?1:((User)getSessionAttr("user")).getInt("id");
					String sql="";
					long barcode12=0L;
					Long num = 0L;
					int repeat = 0;
					long nextId = (Db.queryLong("select max(id) from goods")==null)?1:Db.queryLong("select max(id) from goods");
					nextId++;
					for (int i = s.getFirstRow()+1; i < s.getLastRow(); i++) {
						try {
							//System.out.print(s.getCell(i, 0)+","+s.getCell(i, 1)+","+s.getCell(i, 2)+","+s.getCell(i, 3)+","+s.getCell(i, 4)+","+s.getCell(i, 5)+","+s.getCell(i, 6)+","+s.getCell(i, 7)+","+s.getCell(i, 8)+","+s.getCell(i, 9)+","+s.getCell(i, 10));
							num = Db.queryLong("select count(*) from goods where name = '"+s.getCell(i, 0).trim()+"'"); 
							if(num>0){
								repeat++;
								continue;
							}
							sql = "insert into goods (id,cat_id,name,code,spec," +
									"unit,color," +
									"origin,intro,company_id,create_actor) values ("
								+nextId+","+catId+",'"+StrUtil.null2Blank(s.getCell(i, 0).trim())+"','"+StrUtil.null2Blank(s.getCell(i, 1))+"','"
								+StrUtil.null2Blank(s.getCell(i, 2))+"','"
								+StrUtil.null2Blank(s.getCell(i, 3))+"','"+StrUtil.null2Blank(s.getCell(i, 4))+"','"
								+StrUtil.null2Blank(s.getCell(i, 5))+"','"+StrUtil.null2Blank(s.getCell(i, 6))+"',"+companyId+","+actorId+")";
							//System.out.println(sql);
							j= Db.update(sql);
							if(j>0){
								nextId++;
							}
						} catch (ExcelException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//System.out.println();
					}
					setAttr("log", (s.getLastRow()-1)+"#"+repeat+"#"+(s.getLastRow()-1-repeat));
					return true ;
				}
			}
		);
		workBook.close();
		return succeed;
	}
	
	public void edit() {
		setAttr("catList", Category.dao.find("SELECT node.* FROM category node, category parent WHERE node.lft >= parent.lft AND node.rgt <= parent.rgt and parent.parent_id is null ORDER BY node.lft"));
		setAttr("companyList", Company.dao.find("select * from company"));
		setAttr("goods", Goods.dao.findById(getParaToInt()));
		setAttr("good_att", GoodsAtt.dao.find("select * from goods_att where good_id=?",getParaToInt()));
	}
	
	@Before(GoodsValidator.class)
	public void update() {
		final String id = getPara("goods.id");
		String name = getPara("goods.name");
		final String[] attachmentPaths = getParaValues("attachmentPaths");
		final String[] attachmentNames = getParaValues("attachmentNames");
		final String[] attachmentThumbnailPaths = getParaValues("attachmentThumbnailPaths");
		final String[] attachmentThumbnailNames = getParaValues("attachmentThumbnailNames");
		List<Goods> list = Goods.dao.find("select * from goods where name=? and id!=?",name,id);
		if(list.size()>0){
			setAttr("nameMsg", "设备名称重名！请更正。");
			forwardAction("/admin/goods/edit");
		}
		else{
			boolean succeed = Db.tx(
				new IAtom(){
					public boolean run() throws SQLException {
						boolean count = getModel(Goods.class)
						.set("update_actor", (getSessionAttr("user")==null)?1:((User)getSessionAttr("user")).getInt("id"))
						.set("update_time", DateFmt.addLongDays(0))
						.update();
						
						// 保存附件
						if (attachmentPaths != null && attachmentPaths.length > 0) {
							DelPic(Integer.parseInt(id));
							Db.update("delete from goods_att where good_id="+id);
							for (int i = 0, len = attachmentPaths.length; i < len; i++) {
								if (!StrUtil.isBlank(attachmentPaths[i]) && !StrUtil.isBlank(attachmentNames[i])) {
									Db.update("insert into goods_att (good_id,name,url,thumbnailname,thumbnailurl,priority) values ("
											+id+",'"+attachmentNames[i]+"','"+attachmentPaths[i]+"','"
											+attachmentThumbnailNames[i]+"','"+attachmentThumbnailPaths[i]+"',"+i+")");
								}
								else{
									setAttr("resultMsg", "附件名称和路径不能为空，工作任务添加失败。");
									setAttr("status", "0");
									return false;
								}
							}
						}
						return count ;
					}
				}
			);
			if(succeed){
				setAttr("resultMsg", "数据保存成功。");
				forwardAction("/admin/goods");
			}
			else{
				setAttr("resultMsg", "数据保存失败，请重新录入。");
				forwardAction("/admin/goods/edit");
			}
		}
	}
	
	public void delete() {
		final Integer[] ids = getParaValuesToInt("ids");
		if(ids!=null && ids.length>0){
			boolean succeed = Db.tx(
				new IAtom(){
					public boolean run() throws SQLException {
						String strId = "";
						for(Integer id:ids){
							strId += id+",";
							DelPic(id);
						}
						strId = strId.substring(0, strId.length()-1);
						Db.update("delete from goods_att where good_id in ("+strId+")");
						int j = Db.update("delete from goods where id in ("+strId+")");
						return j>0 ;
					}
				}
			);
			if(succeed){
				setAttr("resultMsg", "数据删除成功。");
			}
			else{
				if(!"0".equals(getAttr("result")))
					setAttr("resultMsg", "数据删除失败，请重试或联系管理员。");
			}
		}
		else{
			if(getParaToInt()>0){
				boolean succeed = Db.tx(
					new IAtom(){
						public boolean run() throws SQLException {
							DelPic(getParaToInt());
							Db.update("delete from goods_att where good_id in ("+getParaToInt()+")");
							boolean boogoods = Goods.dao.deleteById(getParaToInt());
							return boogoods ;
						}
					}
				);
				if(succeed){
					setAttr("resultMsg", "数据删除成功。");
				}
				else{
					if(!"0".equals(getAttr("result")))
						setAttr("resultMsg", "数据删除失败，请重试或联系管理员。");
				}
			}
		}
		forwardAction("/admin/goods");
	}
	public void DelPic(int good_id){
		String realPath = this.getRequest().getRealPath("/");
		GoodsAtt ga= GoodsAtt.dao.findFirst("select url,thumbnailurl from goods_att where good_id=?", good_id);
		String url = ga.getStr("url");
		String tnurl = ga.getStr("thumbnailurl");
		System.out.println(realPath+"#"+url);
		System.out.println(realPath+"#"+tnurl);
		if(!"".equals(url)){
			File f = new File(realPath+url);
			if(f.exists()){
				f.delete();
			}
		}
		if(!"".equals(tnurl)){
			File f2 = new File(realPath+tnurl);
			if(f2.exists()){
				f2.delete();
			}
		}
	}
}


