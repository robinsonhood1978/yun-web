package com.cms.admin.channel;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.cms.util.StrUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

@Before(ChannelInterceptor.class)
public class AdminChannelController extends Controller {
	
	public void index() {
		render("/admin/channel/entry.html");
	}
	public void all() {
		render("/admin/channel/all.html");
	}
	public void left() {
		render("/admin/channel/left.html");
	}
	
	public void add() {
		String root = (getPara("root")==null || StrUtil.isBlank(getPara("root")))?"source":getPara("root");
		String parentName = "顶级栏目";
		String parentId = "0";
		if(!"source".equals(root)){
			parentName = Db.queryStr("select name from channel where id=?",root);
			parentId = root;
		}
		setAttr("parentName",parentName);
		setAttr("root",root);
		setAttr("channel",new Channel().set("parent_id", parentId));
		setAttr("channelList", Channel.dao.find("SELECT node.* FROM channel node, " +
				"channel parent WHERE node.lft >= parent.lft AND node.rgt <= parent.rgt " +
				"and parent.parent_id is null ORDER BY node.lft"));
		//栏目模板列表
		List<Record> channelModelList = Db.find("select name,path from template where type=0");
		setAttr("channelModelList", channelModelList);
		//内容模板列表
		List<Record> contentModelList = Db.find("select name,path from template where type=1");
		setAttr("contentModelList", contentModelList);
	}
	@Before(ChannelValidator.class)
	public void save() {
		String root = (getPara("root")==null || StrUtil.isBlank(getPara("root")))?"source":getPara("root");
		String name = getPara("channel.name"); 
		List<Channel> list = Channel.dao.find("select * from channel where name=?",name);
		if(list.size()>0){
			setAttr("nameMsg", "重名！请更正。");
			setAttr("root",root);
			forwardAction("/admin/channel/add/");
		}
		else{
			boolean succeed = Db.tx(
				new IAtom(){
					public boolean run() throws SQLException {
						boolean count = false;
						boolean boo = false;
						String parentId = getPara("channel.parent_id");
						if(parentId.equals("0")){
							int lft = 1;
							if(Db.queryLong("select count(id) from channel")>0)
								lft = Db.queryInt("select max(rgt) from channel")+1;
							int rgt = lft+1;
							count = getModel(Channel.class).set("parent_id", null).set("lft", lft).set("rgt", rgt).set("depth", 0)
							.set("priority", ("".equals(getPara("channel.priority")) || getPara("channel.priority")==null)?1:getPara("channel.priority"))
							.save();
							boo = true;
						}
						else{
							//思路：操作相当于把左侧的NodeId=B之后的所有节点后移2个位置，然后插入新节点Q.
							Channel parentCtg = getModel(Channel.class).findById(parentId);
							int lft = parentCtg.getInt("lft"); 
							int depth = parentCtg.getInt("depth"); 
							int iRgt = Db.update("UPDATE channel SET rgt = rgt + 2 WHERE rgt >?",lft);
							int iLft = Db.update("UPDATE channel SET lft = lft + 2 WHERE lft >?",lft);
							count = getModel(Channel.class).set("lft", lft+1)
							.set("rgt", lft+2).set("depth", depth+1)
							.set("priority", ("".equals(getPara("channel.priority")) || getPara("channel.priority")==null)?1:getPara("channel.priority"))
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
				forwardAction("/admin/channel/list/");
			}
			else{
				setAttr("resultMsg", "数据保存失败，请重新录入。");
				forwardAction("/admin/channel/add/");
			}
		}
	}
	public void edit() {
		Channel cat = Channel.dao.findById(getParaToInt(0));
		setAttr("channelList", Channel.dao.find("SELECT node.* FROM channel node, channel parent WHERE node.lft >= parent.lft AND node.rgt <= parent.rgt and parent.parent_id is null and node.lft not between ? and ? ORDER BY node.lft",cat.getInt("lft"),cat.getInt("rgt")));
		List<Record> channelModelList = Db.find("select name,path from template where type=0");
		List<Record> contentModelList = Db.find("select name,path from template where type=1");
		//栏目模板列表
		setAttr("channelModelList", channelModelList);
		//内容模板列表
		setAttr("contentModelList", contentModelList);
		//转义字符
		if(cat.get("tpl_channel") != null){
			cat.set("tpl_channel",cat.get("tpl_channel").toString().replace("\\", "\\\\"));
		}
		if(cat.get("tpl_content") != null){
			cat.set("tpl_content",cat.get("tpl_content").toString().replace("\\", "\\\\"));
		}
		setAttr("channel", cat);
		String root = (getPara(1)==null || StrUtil.isBlank(getPara(1)))?"source":getPara(1);
		setAttr("root", root);
	}
	@Before(ChannelValidator.class)
	public void update() {
		String root = (getPara("root")==null || StrUtil.isBlank(getPara("root")))?"source":getPara("root");
		final String id = getPara("channel.id");
		String name = getPara("channel.name");
		List<Channel> list = Channel.dao.find("select * from channel where name=? and id!=?",name,id);
		if(list.size()>0){
			setAttr("nameMsg", "重名！请更正。");
			forwardAction("/admin/channel/edit/"+id+"-"+root);
		}
		else{
			boolean succeed = Db.tx(
				new IAtom(){
					public boolean run() throws SQLException {
						String parentId = getPara("channel.parent_id");
						
						/*以移动D节点到H节点下I节点后为例。思路：操作相当于先为子树腾出空间，
						即移动右侧H跨度=（D.rgt-D.lft+1=6）个位置；然后调整D节点，
						移动偏移量=(H原来的右位置-D.lft=18-4)，然后，删除D移动后产生的空白位置。*/
						// 获得节点跨度
						Channel cat = Channel.dao.findById(id);
						
						//对已经修改的图片进行删除
						delImg(cat);
						
						int oriDepth = cat.getInt("depth");
						int span = cat.getInt("rgt") - cat.getInt("lft") + 1;
						//System.out.println("span======="+span+"---------");
						// 获得当前父节点右位置
						Channel curParentCat = getModel(Channel.class).findById(parentId);
						int curParentRgt = 1;
						int depth = 0;
						if(!"0".equals(parentId)){
							curParentRgt = curParentCat.getInt("rgt");
							depth = curParentCat.getInt("depth")+1;
						}
						//System.out.println("curParentRgt======="+curParentRgt+"---------");
						
						// 先空出位置
						
						int i = Db.update("update channel set rgt=rgt+? where rgt>=?",span,curParentRgt);
						int j = Db.update("update channel set lft=lft+? where lft>=?",span,curParentRgt);
						
						// 再调整自己
						cat = Channel.dao.findById(id);
						int offset = curParentRgt - cat.getInt("lft");
						int depthSpan = depth - oriDepth;
						int k = Db.update("update channel set lft=lft+?, rgt=rgt+?,depth=depth+? WHERE lft between ? and ?",offset,offset,depthSpan,cat.getInt("lft"),cat.getInt("rgt"));
						
						// 最后删除（清空位置）
						int p = Db.update("update channel set rgt=rgt-? where rgt>?",span,cat.getInt("rgt"));
						int q = Db.update("update channel set lft=lft-? where lft>?",span,cat.getInt("rgt"));
						
						boolean boo = false;
						if(!"0".equals(parentId)){
							boo = getModel(Channel.class).set("depth", depth).update();
						}
						else
							boo = getModel(Channel.class).set("parent_id", null).set("depth", depth).update();
						return boo ;
						
					}
				}
			);
			setAttr("root",root);
			if(succeed){
				setAttr("resultMsg", "数据保存成功。");
				forwardAction("/admin/channel/list");
			}
			else{
				setAttr("resultMsg", "数据保存失败，请重新录入。");
				forwardAction("/admin/channel/edit");
			}
		}
	}
	public void list() {
		
		Page<Channel> page = null;
		if(getPara("root")==null|| StrUtil.isBlank(getPara("root")) || "source".equals(getPara("root"))){
			page = Channel.dao.paginate(getParaToInt("page")== null ? 1 :getParaToInt("page"), 10, "select *", 
			"from channel a where a.parent_id is null order by id asc");
		}
		else{
			page = Channel.dao.paginate(getParaToInt("page")== null ? 1 :getParaToInt("page"), 10, "select *", 
			"from channel a where a.parent_id =? order by id asc",Integer.parseInt(getPara("root")));
		}
		setAttr("channelPage", page);
		String root = (getPara("root")==null || StrUtil.isBlank(getPara("root")))?"source":getPara("root");
		setAttr("root", root);
		render("/admin/channel/list.html");
	}
	public void tree() {
		String root = getPara("root");
		boolean isRoot;
		// jquery treeview的根请求为root=source
		if (root==null || StrUtil.isBlank(root) || "source".equals(root)) {
			isRoot = true;
		} else {
			isRoot = false;
		}
		setAttr("isRoot", isRoot);
		List<Channel> list;
		//导入数据：insert into channel (id,parent_id,name,path,lft,rgt,priority,is_display) select a.channel_id,a.parent_id,b.channel_name,a.channel_path,a.lft,a.rgt,a.priority,a.is_display from jc_channel a,jc_channel_ext b where a.channel_id=b.channel_id; 
		if (isRoot) {
			list = Channel.dao.find("select e.*,c.id as child from (select d.* from channel d " +
					"where d.parent_id is null) as e left join (select distinct a.id from channel a " +
					"left join channel b on a.id=b.parent_id where a.parent_id is null and " +
					"b.id is not null) as c on e.id=c.id " +
					"order by e.priority asc,e.id asc");
		} else {
			Integer rootId = Integer.valueOf(root);
			list = Channel.dao.find("select e.*,c.id as child from (select d.* from channel d " +
					"where d.parent_id =?) as e left join (select distinct a.id from channel a " +
					"left join channel b on a.id=b.parent_id where a.parent_id =? and " +
					"b.id is not null) as c on e.id=c.id " +
					"order by e.priority asc,e.id asc",rootId,rootId);
		}
		setAttr("list", list);
		getResponse().setHeader("Cache-Control", "no-cache");
		getResponse().setContentType("text/json;charset=UTF-8");
		render("/admin/channel/tree.html");
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
		forwardAction("/admin/channel/list");
	}
	private boolean delOne(int catId){
		//思路：操作先删D的所有子节点和自己；然后更新左右位置。
		Channel cat = Channel.dao.findById(catId);
		
		//对已经修改的图片进行删除
		delImg(cat);
		
		List<Integer> idList = Db.query("select id from channel where lft between ? and ?",cat.getInt("lft"),cat.getInt("rgt"));
		String strId = "";
		for(Integer id:idList){
			strId += id+",";
		}
		strId = strId.substring(0, strId.length()-1);
		Long num = Db.queryLong("select count(*) from content where channel_id in ("+strId+")"); 
		
		if(num>0){
			setAttr("resultMsg", "此栏目或其子栏目下有内容存在，栏目删除失败，请删除内容后重试。");
			setAttr("status","1");
			return false;
		}
		else{
			Long num2 = Db.queryLong("select count(*) from channel where parent_id = ?",cat.getInt("id"));
			if(num2>0){
				setAttr("resultMsg", "此栏目包含子栏目，栏目删除失败，请删除子栏目后重试。");
				setAttr("status","2");
				return false;
			}
		}
		
		int width = cat.getInt("rgt")-cat.getInt("lft")+1;
		int i = Db.update("delete from channel where lft between ? and ?",cat.getInt("lft"),cat.getInt("rgt"));
		int j = Db.update("update channel set rgt=rgt-? where rgt>?",width,cat.getInt("rgt"));
		int k = Db.update("update channel set lft=lft-? where lft>?",width,cat.getInt("rgt"));
		return i>0 ;
	}
	
	private List<String> getTplChannel(String solution) {
		File f = new File(solution);
		File[] files = f.listFiles();
		if (files != null) {
			List<String> list = new ArrayList<String>();
			for (File file : files) {
				list.add(file.toString().substring(solution.lastIndexOf("\\channel")).replace("\\", "/"));
			}
			return list;
		} else {
			return new ArrayList<String>(0);
		}
	}

	private List<String> getTplContent(String solution) {
		File f = new File(solution);
		File[] files = f.listFiles();
		if (files != null) {
			List<String> list = new ArrayList<String>();
			for (File file : files) {
				list.add(file.toString().substring(solution.lastIndexOf("\\content")).replace("\\", "/"));
			}
			return list;
		} else {
			return new ArrayList<String>(0);
		}

	}
	//更新和删除时 需要对文件进行删除
	//更新时 判断上传的图片是否和数据库一致   一致的话直接更新 不一致的话 要删除对应路径的图片 然后更新
	private void delImg(Channel cat){
		String title_img = getPara("channel.title_img");
		String content_img = getPara("channel.content_img");
		if((title_img == null && cat.get("title_img") != null)
				|| (title_img != null && !(title_img.equals(cat.get("title_img"))))){
			delFile((String) cat.get("title_img"));
		}
		if((content_img == null && cat.get("content_img") != null)
				|| (content_img != null && !(content_img.equals(cat.get("content_img"))))){
			delFile((String) cat.get("content_img"));
		}
	}
	
	//删除对应内容下的图片
	private void delFile(String fileName){
		String realPath = getRequest().getRealPath("/");
		File file=new File(realPath+fileName);
		if(file.exists() && file.isFile()){
			file.delete();
		}
	}
}
