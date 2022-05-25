package com.cms.admin.friendlinkctg;
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

@Before(FriendlinkctgInterceptor.class)
public class AdminFriendlinkctgController extends Controller {
	public void index() {
		String sql = "from friendlinkctg t where 1=1";
		String name = getPara("name");
		
		if(name!=null && !"".equals(name.trim())){
			sql += " and t.name like '%"+name.trim()+"%'";
			setAttr("name",name);
		}
		sql += " order by id desc";
		
		setAttr("friendlinkctgPage", Friendlinkctg.dao.paginate(getParaToInt(0, 1), 10, "select *", sql));
		render("friendlinkctg.html");

	}
	
	public void add() {
	
	}

	
	public void save() {
		final String name = getPara("friendlinkctg.name");
		
		List<Friendlinkctg> list = Friendlinkctg.dao.find("select * from friendlinkctg where name=?",name);
	
		if(list.size()>0){
			setAttr("nameMsg", "产品名称重名！请更正。");
			forwardAction("/admin/friendlinkctg/add");
		
		}
		else{
			boolean succeed = Db.tx(
					
				new IAtom(){
					public boolean run() throws SQLException {
						boolean count = getModel(Friendlinkctg.class).set("name", name.trim())
						.save();
						return count ;
					}
				}
			);
			if(succeed){
			
				setAttr("resultMsg", "数据保存成功。");
				forwardAction("/admin/friendlinkctg");
			}
			else{
				setAttr("resultMsg", "数据保存失败，请重新录入。");
				forwardAction("/admin/friendlinkctg/add");
			}
		}
	}
		
	public void edit() {
		setAttr("friendlinkctg", Friendlinkctg.dao.findById(getParaToInt()));
	}
	
	@Before(FriendlinkctgValidator.class)
	public void update() {
		final String id = getPara("friendlinkctg.id");
		String name = getPara("friendlinkctg.name");
		List<Friendlinkctg> list = Friendlinkctg.dao.find("select * from friendlinkctg where name=? and id!=?",name,id);
		if(list.size()>0){
			setAttr("nameMsg", "产品名称重名！请更正。");
			forwardAction("/admin/friendlinkctg/edit");
		}
		else{
			boolean succeed = Db.tx(
				new IAtom(){
					public boolean run() throws SQLException {
						boolean count = getModel(Friendlinkctg.class)
						.update();
						return count ;
					}
				}
			);
			if(succeed){
				setAttr("resultMsg", "数据保存成功。");
				forwardAction("/admin/friendlinkctg");
			}
			else{
				setAttr("resultMsg", "数据保存失败，请重新录入。");
				forwardAction("/admin/friendlinkctg/edit");
			}
		}
	}
	
	public void delete() {
		Friendlinkctg.dao.deleteById(getParaToInt());
		redirect("/admin/friendlinkctg");
	}

}
