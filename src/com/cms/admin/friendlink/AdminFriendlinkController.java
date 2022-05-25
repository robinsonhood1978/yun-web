package com.cms.admin.friendlink;
import java.sql.SQLException;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;

/*import edu.npu.fastexcel.ExcelException;
import edu.npu.fastexcel.FastExcel;
import edu.npu.fastexcel.Sheet;
import edu.npu.fastexcel.Workbook;
import edu.npu.fastexcel.biff.parser.ParserException;
import edu.npu.fastexcel.compound.io.ReadException;*/

@Before(FriendlinkInterceptor.class)
public class AdminFriendlinkController extends Controller {
	public void index() {
		String sql = "from friendlink t where 1=1";
		String name = getPara("name");
		
		if(name!=null && !"".equals(name.trim())){
			sql += " and t.name like '%"+name.trim()+"%'";
			setAttr("name",name);
		}
		sql += " order by id desc";
		
		setAttr("friendlinkPage", Friendlink.dao.paginate(getParaToInt(0, 1), 10, "select *", sql));
		render("friendlink.html");

	}
	
	public void add() {
		setAttr("friendlinkctgList", Friendlink.dao.find(" select id,name from friendlinkctg "));
		setAttr("friendlink", "");
	
	}

	@Before(FriendlinkValidator.class)
	public void save() {
		final String name = getPara("friendlink.name");
		List<Friendlink> list = Friendlink.dao.find("select * from friendlink where name=?",name);
		if(list.size()>0){
			setAttr("nameMsg", "产品名称重名！请更正。");
			forwardAction("/admin/friendlink/add");
		}
		else{
			boolean succeed = Db.tx(
				new IAtom(){
					public boolean run() throws SQLException {
						boolean count = getModel(Friendlink.class).set("name", name.trim())
					
						.save();
					
						return count ;
					}
				}
			);
			if(succeed){
				setAttr("resultMsg", "数据保存成功。");
				forwardAction("/admin/friendlink");
			}
			else{
				setAttr("resultMsg", "数据保存失败，请重新录入。");
				forwardAction("/admin/friendlink/add");
			}
		}
	}
		
	public void edit() {
		setAttr("friendlink", Friendlink.dao.findById(getParaToInt()));
		setAttr("friendlinkctgList", Friendlink.dao.find(" select id,name from friendlinkctg "));
	}
	
	@Before(FriendlinkValidator.class)
	public void update() {
		final String id = getPara("friendlink.id");
		String name = getPara("friendlink.name");
		List<Friendlink> list = Friendlink.dao.find("select * from friendlink where name=? and id!=?",name,id);
		if(list.size()>0){
			setAttr("nameMsg", "产品名称重名！请更正。");
			forwardAction("/admin/friendlink/edit");
		}
		else{
			boolean succeed = Db.tx(
				new IAtom(){
					public boolean run() throws SQLException {
						boolean count = getModel(Friendlink.class)
						
						.update();
						return count ;
					}
				}
			);
			if(succeed){
				setAttr("resultMsg", "数据保存成功。");
				forwardAction("/admin/friendlink");
			}
			else{
				setAttr("resultMsg", "数据保存失败，请重新录入。");
				forwardAction("/admin/friendlink/edit");
			}
		}
	}
	
	public void delete() {
		Friendlink.dao.deleteById(getParaToInt());
		redirect("/admin/friendlink");
	}

}
