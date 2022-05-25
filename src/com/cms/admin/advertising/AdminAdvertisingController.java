package com.cms.admin.advertising;

import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.cms.util.StrUtil;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;

public class AdminAdvertisingController extends Controller{
	public void index() {
		render("/admin/advertising/entry.html");
	}
	public void all() {
		render("/admin/advertising/all.html");
	}
	public void left() {
		render("/admin/advertising/left.html");
	}
	public void advertising(){
		setAttr("advertisingPage", Advertising.dao.paginate(getParaToInt(0, 1), 10, "select *","from advertising where type=0 "));
		render("advertising.html");
	}
	//合作伙伴数据
	public void partners(){
		setAttr("advertisingPage", Advertising.dao.paginate(getParaToInt(0, 1), 10, "select *","from advertising where type=1 "));
		render("partners.html");
	}
	//查询单个合作伙伴数据
	public void partnersedit(){
		//获取id
		int id=getParaToInt("id");
		Advertising advertising=Advertising.dao.findById(id);
		setAttr("advertising",advertising);
	}
	//合作伙伴信息更新
	public void partnersUpdate(){
		allUpdate();
		redirect("/admin/advertising/partners");
	}
	//合作伙伴的添加方法
	public void partnersSave(){
		allSave();
		redirect("/admin/advertising/partners");
	}
	//删除一条广告版位
	public void partnersDelete(){
		allDelete(getParaToInt(0));
		redirect("/admin/advertising/partners");
	}
	//合作伙伴的add
	public void partnersadd(){
		setAttr("advertising","");
	}
	//广告的add
	public void add(){
		setAttr("advertising","");
	}
	//查询单个广告数据
	public void edit(){
		//获取id
		int id=getParaToInt("id");
		System.out.println(id);
		Advertising advertising=Advertising.dao.findById(id);
		setAttr("advertising",advertising);
	}
	//广告的添加方法
	public  void save(){
		allSave();
		redirect("/admin/advertising/advertising");
	}
	//删除一条广告版位
	public void delete(){
		
		final Integer[] ids = getParaValuesToInt("ids");
		if (ids != null && ids.length > 0) {
			boolean succeed = Db.tx(new IAtom() {
				public boolean run() throws SQLException {
					Arrays.sort(ids);
					for (int i = ids.length - 1; i >= 0; i--) {
						if (!allDelete(ids[i])) {
							return false;
						}
					}
					return true;
				}
			});
			if (succeed) {
				setAttr("resultMsg", "删除成功。");
			} else {
				setAttr("resultMsg", "<font color=red >删除失败，请重新尝试。</font>");
			}
		} else {
			if (getParaToInt(0) > 0) {
				boolean succeed = Db.tx(new IAtom() {
					public boolean run() throws SQLException {
						boolean flag = false;
						flag = allDelete(getParaToInt(0));
						return flag;
					}
				});
				if (succeed) {
					setAttr("resultMsg", "删除成功。");
				} else {
					setAttr("resultMsg", "<font color=red >删除失败，请重新尝试。</font>");
				}
			}
		}
		redirect("/admin/advertising/advertising");
	}
	//修给广告版位信息
	public void update(){
		allUpdate();
		redirect("/admin/advertising/advertising");
	}
	//公共Save方法
	public void allSave(){
		//获取广告名称
		String advertise_name=getPara("advertising.advertise_name");
		//获取版位名称
		String version_name=getPara("advertising.version_name");
		//获取图片路径
		String image_url=getPara("advertising.image_url");
		//获取图片类型
		String type=getPara("advertising.type");
		//获取链接地址
		String link_url=getPara("advertising.link_url");
		//获取链接提示
		String link_prompt=getPara("advertising.link_prompt");
		int affected=Db.update("insert into advertising(advertise_name,version_name,image_url,link_url,link_prompt,type) values('"+advertise_name+"','"+version_name+"','"+image_url+"','"+link_url+"','"+link_prompt+"','"+type+"')");
	} 
	//公共的删除方法
	public boolean allDelete(int id){
		//获取该版位ID
		//String id=getPara(0);
		//根据给ID查询该版位的图片路径
		String image_url=Db.queryStr("select image_url from advertising where id=?",id);
		//调用删除图片方法
		delFile(image_url);
		//删除该条数据
		boolean flag = Advertising.dao.deleteById(id);
		return flag;
	}
	//公共的修改方法
	//修改时 判断上传的图片是否和数据库一致   一致的话直接修改，不一致的话 要删除对应路径的图片 然后修改
	public void allUpdate(){
		//获取id
		int id=getParaToInt("advertising.id");
		System.out.println(id);
		//获取广告名称
		String advertise_name=getPara("advertising.advertise_name");
		//获取版位名称
		String version_name=getPara("advertising.version_name");
		//获取链接地址
		String link_url=getPara("advertising.link_url");
		//获取图片类型
		String type=getPara("advertising.type");
		//获取链接提示
		String link_prompt=getPara("advertising.link_prompt");
		//图片路径的判断
		String image_url = getPara("advertising.image_url");
		Advertising adv=Advertising.dao.findById(id);
		if((image_url == null && adv.get("image_url") != null)
				|| (image_url != null && !(image_url.equals(adv.get("image_url"))))){
			delFile((String) adv.get("image_url"));
		}
		int affected=Db.update("update advertising set advertise_name=?,version_name=?,image_url=?,link_url=?,link_prompt=?,type=? where id=?",advertise_name,version_name,image_url,link_url,link_prompt,type,id);
	}
	
	//删除对应内容下的图片
	private void delFile(String fileName){
		String realPath = getRequest().getRealPath("/");
		File file=new File(realPath+fileName);
		if(file.exists() && file.isFile()){
			file.delete();
		}
	}
	//广告的分页
    public void list() {
    	Page<Advertising> page = null;
		if(getPara(0)==null|| getParaToInt(0)==0 || StrUtil.isBlank(getPara(0)) || "source".equals(getPara(0))){
			page = Advertising.dao.paginate(getParaToInt(1, getParaToInt("page")), 10, "select *", 
			"from advertising a where type=0 ");
		}
		else{
			page = Advertising.dao.paginate(getParaToInt(1, getParaToInt("page")), 10, "select a.*", 
			"from advertising a where a.id =? ",Integer.parseInt(getPara(0)));
		}
		setAttr("advertisingPage", page);
		render("/admin/advertising/advertising.html");
	}
    //合作伙伴的分页
    public void lists() {
    	Page<Advertising> page = null;
		if(getPara(0)==null|| getParaToInt(0)==0 || StrUtil.isBlank(getPara(0)) || "source".equals(getPara(0))){
			page = Advertising.dao.paginate(getParaToInt(1, getParaToInt("page")), 10, "select *", 
			"from advertising a where type=1 ");
		}
		else{
			page = Advertising.dao.paginate(getParaToInt(1, getParaToInt("page")), 10, "select a.*", 
			"from advertising a where a.id =? ",Integer.parseInt(getPara(0)));
		}
		setAttr("advertisingPage", page);
		render("/admin/advertising/partners.html");
	}
}
