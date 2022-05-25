package com.cms.admin.role;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Record;

@Before(RoleInterceptor.class)
public class AdminRoleController extends Controller {
	public void index() {
		setAttr("rolePage", Role.dao.paginate(getParaToInt(0, 1), 10, "select *", "from role order by id asc"));
		render("role.html");
	}
	
	public void add() {
		//setAttr("role", null);
	}
	
	@Before(RoleValidator.class)
	public void save() {
		String name = getPara("role.name");
		List<Role> list = Role.dao.find("select * from role where name=?",name);
		if(list.size()>0){
			setAttr("nameMsg", "角色重名！请更正。");
			forwardAction("/admin/role/add");
		}
		else{
			boolean succeed = Db.tx(
				new IAtom(){
					public boolean run() throws SQLException {
						boolean count = getModel(Role.class).save();
						int role_id = Db.queryInt("select max(id) from role");
						boolean count2 = true;
						int[] count3;
						if(count){
							String[] uris = getParaValues("uri");
							if(uris!=null && uris.length>0){
								String[][] objs=new String[uris.length][1];
								int i=0;
								for(String m:uris){
									objs[i++][0]=m;
								}
								count3=Db.batch("insert into role_uri (role_id,uri) values ("+role_id+",?)",objs,20);
								for(int j:count3){
									if(j==0){
										count2 = false;
										break;
									}
								}
							}
						}
						return count  && count2 ;
					}
				}
			);
			if(succeed){
				setAttr("resultMsg", "数据保存成功。");
				forwardAction("/admin/role");
			}
			else{
				setAttr("resultMsg", "数据保存失败，请重新录入。");
				forwardAction("/admin/role/add");
			}
		}
	}
	
	public void edit() {
		setAttr("role", Role.dao.findById(getParaToInt()));
		List<Record> uris = Db.find("select uri from role_uri where role_id="+getParaToInt());
		List<String> sUris = new LinkedList<String>();
		for(Record uri:uris){
			sUris.add(uri.getStr("uri"));
		}
		setAttr("uris",sUris);
	}
	
	@Before(RoleValidator.class)
	public void update() {
		final String id = getPara("role.id");
		String name = getPara("role.name");
		List<Role> list = Role.dao.find("select * from role where name=? and id!=?",name,id);
		if(list.size()>0){
			setAttr("nameMsg", "角色重名！请更正。");
			forwardAction("/admin/role/edit");
		}
		else{
			boolean succeed = Db.tx(
				new IAtom(){
					public boolean run() throws SQLException {
						boolean count = getModel(Role.class).update();
						int del = Db.update("delete from role_uri where role_id = ?",id);
						boolean count2 = true;
						int[] count3;
						if(count){
							String[] uris = getParaValues("uri");
							if(uris!=null && uris.length>0){
								String[][] objs=new String[uris.length][1];
								int i=0;
								for(String m:uris){
									objs[i++][0]=m;
								}
								count3=Db.batch("insert into role_uri (role_id,uri) values ("+id+",?)",objs,20);
								for(int j:count3){
									if(j==0){
										count2 = false;
										break;
									}
								}
							}
						}
						return count && del>0 && count2 ;
					}
				}
			);
			if(succeed){
				setAttr("resultMsg", "数据保存成功。");
				forwardAction("/admin/role");
			}
			else{
				setAttr("resultMsg", "数据保存失败，请重新录入。");
				forwardAction("/admin/role/edit");
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
						}
						strId = strId.substring(0, strId.length()-1);
						int i = Db.update("delete from role_uri where role_id in ("+strId+")");
						int j = Db.update("delete from role where id in ("+strId+")");
						return i>0 && j>0 ;
					}
				}
			);
			if(succeed){
				setAttr("resultMsg", "数据删除成功。");
			}
			else{
				setAttr("resultMsg", "数据删除失败，请重试。");
			}
		}
		else{
			if(getParaToInt()>0){
				boolean succeed = Db.tx(
					new IAtom(){
						public boolean run() throws SQLException {
							int i = Db.update("delete from role_uri where role_id = ?",getParaToInt());
							boolean booRole = Role.dao.deleteById(getParaToInt());
							return i>0  && booRole ;
						}
					}
				);
				if(succeed){
					setAttr("resultMsg", "数据删除成功。");
				}
				else{
					setAttr("resultMsg", "数据删除失败，请重试。");
				}
			}
		}
		forwardAction("/admin/role");
	}
}


