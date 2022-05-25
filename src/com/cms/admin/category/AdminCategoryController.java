package com.cms.admin.category;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.cms.util.StrUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;

@Before(CategoryInterceptor.class)
public class AdminCategoryController extends Controller {
	public void index() {
		Page<Category> page = null;
		if(getPara(0)==null|| StrUtil.isBlank(getPara(0)) || "source".equals(getPara(0))){
			page = Category.dao.paginate(getParaToInt(1, 1), 10, "select *", 
			"from category a where a.parent_id is null order by id asc");
		}
		else{
			page = Category.dao.paginate(getParaToInt(1, 1), 10, "select *", 
			"from category a where a.parent_id =? order by id asc",Integer.parseInt(getPara(0)));
		}
		setAttr("categoryPage", page);
		String root = (getPara(0)==null || StrUtil.isBlank(getPara(0)))?"source":getPara(0);
		setAttr("root", root);
		render("/admin/category/list.html");
	}
	public void entry() {
		render("/admin/category/entry.html");
	}
	public void all() {
		render("/admin/category/all.html");
	}
	public void left() {
		System.out.println("root===");
		render("/admin/category/left.html");
	}
	public void add() {
		String root = (getPara("root")==null || StrUtil.isBlank(getPara("root")))?"source":getPara("root");
		String parentName = "顶级栏目";
		String parentId = "0";
		if(!"source".equals(root)){
			parentName = Db.queryStr("select name from category where id=?",root);
			parentId = root;
		}
		setAttr("parentName",parentName);
		setAttr("root",root);
		setAttr("category",new Category().set("parent_id", parentId));
		setAttr("categoryList", Category.dao.find("SELECT node.* FROM category node, " +
				"category parent WHERE node.lft >= parent.lft AND node.rgt <= parent.rgt " +
				"and parent.parent_id is null ORDER BY node.lft"));
	}
	@Before(CategoryValidator.class)
	public void save() {
		String root = (getPara("root")==null || StrUtil.isBlank(getPara("root")))?"source":getPara("root");
		String name = getPara("category.name");
		List<Category> list = Category.dao.find("select * from category where name=?",name);
		if(list.size()>0){
			setAttr("nameMsg", "栏目重名！请更正。");
			setAttr("root",root);
			forwardAction("/admin/category/add/");
		}
		else{
			boolean succeed = Db.tx(
				new IAtom(){
					public boolean run() throws SQLException {
						boolean count = false;
						boolean boo = false;
						String parentId = getPara("category.parent_id");
						if(parentId.equals("0")){
							int lft = 1;
							if(Db.queryLong("select count(id) from category")>0)
								lft = Db.queryInt("select max(rgt) from category")+1;
							int rgt = lft+1;
							count = getModel(Category.class).set("parent_id", null).set("lft", lft).set("rgt", rgt).set("depth", 0)
							.set("priority", ("".equals(getPara("category.priority")) || getPara("category.priority")==null)?1:getPara("category.priority"))
							.save();
							boo = true;
						}
						else{
							//思路：操作相当于把左侧的NodeId=B之后的所有节点后移2个位置，然后插入新节点Q.
							Category parentCtg = getModel(Category.class).findById(parentId);
							int lft = parentCtg.getInt("lft"); 
							int depth = parentCtg.getInt("depth"); 
							int iRgt = Db.update("UPDATE category SET rgt = rgt + 2 WHERE rgt >?",lft);
							int iLft = Db.update("UPDATE category SET lft = lft + 2 WHERE lft >?",lft);
							count = getModel(Category.class).set("lft", lft+1)
							.set("rgt", lft+2).set("depth", depth+1)
							.set("priority", ("".equals(getPara("category.priority")) || getPara("category.priority")==null)?1:getPara("category.priority"))
							.save();
							if(iRgt>0 || iLft>0)
								boo = true;
						}
						return count && boo;
					}
				}
			);
			setAttr("root",root);
			if(succeed){
				setAttr("resultMsg", "数据保存成功。");
				forwardAction("/admin/category/"+root);
			}
			else{
				setAttr("resultMsg", "数据保存失败，请重新录入。");
				forwardAction("/admin/category/add?root="+root);
			}
		}
	}
	public void edit() {
		Category cat = Category.dao.findById(getParaToInt(0));
		setAttr("categoryList", Category.dao.find("SELECT node.* FROM category node, category parent WHERE node.lft >= parent.lft AND node.rgt <= parent.rgt and parent.parent_id is null and node.lft not between ? and ? ORDER BY node.lft",cat.getInt("lft"),cat.getInt("rgt")));
		setAttr("category", cat);
		String root = (getPara(1)==null || StrUtil.isBlank(getPara(1)))?"source":getPara(1);
		setAttr("root", root);
	}

	public void update() {
		String root = (getPara("root")==null || StrUtil.isBlank(getPara("root")))?"source":getPara("root");
		final String id = getPara("category.id");
		String name = getPara("category.name");
		List<Category> list = Category.dao.find("select * from category where name=? and id!=?",name,id);
		if(list.size()>0){
			setAttr("nameMsg", "目录重名！请更正。");
			forwardAction("/admin/category/edit/"+id+"-"+root);
		}
		else{
			boolean succeed = Db.tx(
				new IAtom(){
					public boolean run() throws SQLException {
						
						String parentId = getPara("category.parent_id");
						
						/*以移动D节点到H节点下I节点后为例。思路：操作相当于先为子树腾出空间，
						即移动右侧H跨度=（D.rgt-D.lft+1=6）个位置；然后调整D节点，
						移动偏移量=(H原来的右位置-D.lft=18-4)，然后，删除D移动后产生的空白位置。*/
						// 获得节点跨度
						Category cat = Category.dao.findById(id);
						int oriDepth = cat.getInt("depth");
						int span = cat.getInt("rgt") - cat.getInt("lft") + 1;
						//System.out.println("span======="+span+"---------");
						// 获得当前父节点右位置
						Category curParentCat = getModel(Category.class).findById(parentId);
						int curParentRgt = 1;
						int depth = 0;
						if(!"0".equals(parentId)){
							curParentRgt = curParentCat.getInt("rgt");
							depth = curParentCat.getInt("depth")+1;
						}
						
						
						// 先空出位置
						
						int i = Db.update("update category set rgt=rgt+? where rgt>=?",span,curParentRgt);
						int j = Db.update("update category set lft=lft+? where lft>=?",span,curParentRgt);
						
						// 再调整自己
						cat = Category.dao.findById(id);
						int offset = curParentRgt - cat.getInt("lft");
						int depthSpan = depth - oriDepth;
						int k = Db.update("update category set lft=lft+?, rgt=rgt+?,depth=depth+? WHERE lft between ? and ?",offset,offset,depthSpan,cat.getInt("lft"),cat.getInt("rgt"));
						// 最后删除（清空位置）
						int p = Db.update("update category set rgt=rgt-? where rgt>?",span,cat.getInt("rgt"));
						int q = Db.update("update category set lft=lft-? where lft>?",span,cat.getInt("rgt"));
						
						
						boolean boo = false;
						if(!"0".equals(parentId)){
							boo = getModel(Category.class).set("depth", depth).update();
						}
						else
							boo = getModel(Category.class).set("parent_id", null).set("depth", depth).update();
						
						return boo ;
						
					}
				}
			);
			setAttr("root",root);
			if(succeed){
				setAttr("resultMsg", "数据保存成功。");
				forwardAction("/admin/category/"+root);
			}
			else{
				setAttr("resultMsg", "数据保存失败，请重新录入。");
				forwardAction("/admin/category/edit/"+id+"-"+root);
			}
		}
	}

	public void tree() {
		String root = getPara("root");
		System.out.println("root==="+root);
		boolean isRoot;
		// jquery treeview的根请求为root=source
		if (root==null || StrUtil.isBlank(root) || "source".equals(root)) {
			isRoot = true;
		} else {
			isRoot = false;
		}
		setAttr("isRoot", isRoot);
		List<Category> list;

		if (isRoot) {
			list = Category.dao.find("select e.*,c.id as child from (select d.* from category d " +
					"where d.parent_id is null) as e left join (select distinct a.id from category a " +
					"left join category b on a.id=b.parent_id where a.parent_id is null and " +
					"b.id is not null) as c on e.id=c.id " +
					"order by e.priority asc,e.id asc");
		} else {
			Integer rootId = Integer.valueOf(root);
			list = Category.dao.find("select e.*,c.id as child from (select d.* from category d " +
					"where d.parent_id =?) as e left join (select distinct a.id from category a " +
					"left join category b on a.id=b.parent_id where a.parent_id =? and " +
					"b.id is not null) as c on e.id=c.id " +
					"order by e.priority asc,e.id asc",rootId,rootId);
		}
		setAttr("list", list);
		getResponse().setHeader("Cache-Control", "no-cache");
		getResponse().setContentType("text/json;charset=UTF-8");
		render("/admin/category/tree.html");
	}
	
	public void delete() {
		String root = (getPara(1)==null || StrUtil.isBlank(getPara(1)))?"source":getPara(1);
		System.out.println("root===="+root);
		setAttr("root",root);
		final Integer[] ids = getParaValuesToInt("ids");
		if(ids!=null && ids.length>0){
			boolean succeed = Db.tx(
				new IAtom(){
					public boolean run() throws SQLException {
						Arrays.sort(ids);
						for(int i=ids.length-1;i>=0;i--){
							if(!delOne(ids[i])){
								return false;
							}
						}
						return true;
					}
				}
			);
			if(succeed){
				setAttr("resultMsg", "数据删除成功。");
			}
			else{
				if(!"1".equals(getAttr("status")) && !"2".equals(getAttr("status")))
					setAttr("resultMsg", "数据删除失败，请重试。");
			}
		}
		else{
			if(getParaToInt(0)>0){
				boolean succeed = Db.tx(
					new IAtom(){
						public boolean run() throws SQLException {
							return delOne(getParaToInt(0));
						}
					}
				);
				if(succeed){
					setAttr("resultMsg", "数据删除成功。");
				}
				else{
					if(!"1".equals(getAttr("status")) && !"2".equals(getAttr("status")))
						setAttr("resultMsg", "数据删除失败，请重试。");
				}
			}
		}
		forwardAction("/admin/category/"+root);
	}
	private boolean delOne(int catId){
		//思路：操作先删D的所有子节点和自己；然后更新左右位置。
		Category cat = Category.dao.findById(catId);

			Long num2 = Db.queryLong("select count(*) from category where parent_id = ?",cat.getInt("id"));
			if(num2>0){
				setAttr("resultMsg", "此目录包含子目录，目录删除失败，请删除子目录后重试。");
				setAttr("status","2");
				return false;
			}

		
		int width = cat.getInt("rgt")-cat.getInt("lft")+1;
		int i = Db.update("delete from category where lft between ? and ?",cat.getInt("lft"),cat.getInt("rgt"));
		int j = Db.update("update category set rgt=rgt-? where rgt>?",width,cat.getInt("rgt"));
		int k = Db.update("update category set lft=lft-? where lft>?",width,cat.getInt("rgt"));
		return i>0 ;
	}
}
