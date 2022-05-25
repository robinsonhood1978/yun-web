package com.cms.admin.content;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.htmlparser.Parser;
import org.htmlparser.beans.StringBean;
import org.htmlparser.util.ParserException;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.cms.admin.channel.Channel;
import com.cms.admin.config.Config;
import com.cms.admin.user.User;
import com.cms.util.AverageImageScale;
import com.cms.util.DateFmt;
import com.cms.util.StrUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;

@Before(ContentInterceptor.class)
public class AdminContentController extends Controller {
	private static SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmsss"); 
	private static SimpleDateFormat ym = new SimpleDateFormat("yyyyMM");
	private static String uploadroot = "/upload/";
	private static IKAnalyzer analyzer = new IKAnalyzer(true);
	// lucene分词后的分页展示条数
	private static int pageSize = 5;

	public void file() {
		String savefilename = "";
		String filedataFileName = "";
		StringBuffer files = new StringBuffer();
		UploadFile upfile = this.getFile();
		String textId = getPara("mediaFilename");
		boolean suc = false;
		if (upfile != null) {
			String realPath = this.getRequest().getRealPath("/");
			System.out.println(realPath + "#");

			String path = ym.format(new Date());
			File dir = new File(realPath + uploadroot + path);
			if (!dir.exists()) {
				dir.mkdir();
			}
			File file = upfile.getFile();
			filedataFileName = upfile.getOriginalFileName();
			savefilename = uploadroot
					+ path
					+ "/"
					+ sf.format(new Date())
					+ filedataFileName.substring(filedataFileName
							.lastIndexOf("."));
			if (file != null) {
				upfile.getFile().renameTo(new File(realPath + savefilename));
				suc = true;
				files.append(realPath + savefilename);
			}

		} else {
			setAttr("error", "请选择需要上传的文档");
		}
		System.out.println("textId=====" + textId + ",,savefilename===="
				+ savefilename + ",,filedataFileName=====" + filedataFileName);
		setAttr("textId", textId);
		setAttr("contentPath", savefilename);
		setAttr("contentName", filedataFileName);
		render("/admin/common/file.html");
	}
	
	//图片裁剪
	public void imageSelect(){
		setAttr("imgSrcPath",getPara("imgSrcPath"));
		setAttr("zoomWidth",getPara("zoomWidth"));
		setAttr("zoomHeight",getPara("zoomHeight"));
		setAttr("uploadNum",getPara("uploadNum"));
		render("/admin/common/image_select.html");
	}
	
	public void imageCut() throws IOException{
		String imgSrcPath = getPara("imgSrcPath");
		Integer imgTop = getParaToInt("imgTop");
		Integer imgLeft = getParaToInt("imgLeft");
		Integer imgWidth = getParaToInt("imgWidth");
		Integer imgHeight = getParaToInt("imgHeight");
		Integer reMinWidth = getParaToInt("reMinWidth");
		Integer reMinHeight = getParaToInt("reMinHeight");
		String uploadNum = getPara("uploadNum");
		String imgScaleStr = getPara("imgScale");
		float imgScale = Float.valueOf(imgScaleStr);
		
		String realPath = this.getRequest().getRealPath("/");
		File file = new File(realPath + imgSrcPath);
		AverageImageScale.resizeFix(file, file, reMinWidth, reMinHeight, 
				getLen(imgTop, imgScale), getLen(imgLeft, imgScale), 
				getLen(imgWidth, imgScale), getLen(imgHeight, imgScale));
		
		setAttr("uploadNum",uploadNum);
		render("/admin/common/image_cut.html");
		
	}

	public void index() throws java.text.ParseException, ParseException {
		Page<Content> page = null;
		// 当前页
		StringBuffer sql =new StringBuffer(" from content where 1=1 ");
		//sql文章标题（模糊查询）
		if(getPara("title")!=null && !"".equals(getPara("title").trim())){
			if(getPara("is_href")!=null && getParaToInt("is_href")==1){
				sql.append(" and title like '%"+unicodeToGB(getPara("title"))+"%'");
				setAttr("title", unicodeToGB(getPara("title")));
			}else{
				sql.append(" and title like '%"+getPara("title")+"%'");
				setAttr("title", getPara("title"));
			}
		}
		//sql发布人（模糊查询）
		if(getPara("creator")!=null && !"".equals(getPara("creator").trim())){
			if(getPara("is_href")!=null && getParaToInt("is_href")==1){
				sql.append(" and creator in ( select id from user where name like '%"+unicodeToGB(getPara("creator"))+"%')");
				setAttr("creator", unicodeToGB(getPara("creator")));
			}else{
				sql.append(" and creator in ( select id from user where name like '%"+getPara("creator")+"%')");
				setAttr("creator", getPara("creator"));
			}
		}
		//sql是否推荐
		if(getPara("is_recommend")!=null && !"".equals(getPara("is_recommend").trim()) && getParaToInt("is_recommend")!=2){
			sql.append(" and is_recommend = "+getParaToInt("is_recommend"));
		}
		//sql文章类型
		if(getPara("type_id")!=null && !"".equals(getPara("type_id").trim()) && getParaToInt("type_id")!=0){
			sql.append(" and type_id = "+getParaToInt("type_id"));
		}
		//sql审核状态
		if(getPara("status")!=null && !"".equals(getPara("status").trim()) && getParaToInt("status")!=4){
			sql.append(" and status = "+getParaToInt("status"));
		}
		//sql发布时间
		String startDateStr = getPara("startTime");
		String endDateStr = getPara("endTime");
		if(startDateStr!=null && !"".equals(startDateStr.trim())){
			sql.append(" and UNIX_TIMESTAMP(release_date) >= UNIX_TIMESTAMP('"+startDateStr+"')");
		}
		if(endDateStr!=null && !"".equals(endDateStr.trim())){
			sql.append(" and UNIX_TIMESTAMP(release_date) < UNIX_TIMESTAMP('"+endDateStr+"')");
		}
		//sql关键字
		if(getPara("keyword")!=null && !"".equals(getPara("keyword").trim())){
			if(getPara("is_href")!=null && getParaToInt("is_href")==1){
				sql.append(" and id in (select content_id from content_keywords where keywords_id in ( select id from keywords where word ='"+unicodeToGB(getPara("keyword"))+"' )) ");
				setAttr("keyword", unicodeToGB(getPara("keyword")));
			}else{
				sql.append(" and id in (select content_id from content_keywords where keywords_id in ( select id from keywords where word ='"+getPara("keyword")+"' )) ");
				setAttr("keyword", getPara("keyword"));
			}
		}
		//树节点(栏目)
		if (getPara("root") != null && getParaToInt("root") != 0 && !StrUtil.isBlank(getPara("root")) && !"source".equals(getPara("root"))) {
			sql.append(" and id in ( select content_id from content_channel where channel_id = "+Integer.parseInt(getPara("root"))+")");
		} 
		//排序
		if(getPara("orderBy")!=null && !"".equals(getPara("orderBy").trim())){
			sql.append(" order by "+getPara("orderBy"));
		}else{
			sql.append(" order by id desc ");
		}
		//页数
		int pageNum = 1;
		if (getParaToInt("pageNum") != null) {
			pageNum = getParaToInt("pageNum");
		}
		page = Content.dao.paginate(pageNum, 14, "select *",sql.toString());
		//拿到级联数据
		if(page!=null && page.getList()!=null && page.getList().size()>0){
			for (int i = 0; i < page.getList().size(); i++) {
				page.getList().get(i).set("channel_id", Channel.dao.findById(page.getList().get(i).get("channel_id")));
				page.getList().get(i).set("creator", User.dao.findById(page.getList().get(i).get("creator")));
			}
		}
		setAttr("contentPage", page);
		String root = (getPara("root") == null || StrUtil.isBlank(getPara("root"))) ? "0" : getPara("root");
		setAttr("root", root);
		//setAttr("title", unicodeToGB(getPara("title")));
		//setAttr("creator", unicodeToGB(getPara("creator")));
		setAttr("is_recommend", getPara("is_recommend"));
		setAttr("type_id", getPara("type_id"));
		setAttr("status", getPara("status"));
		setAttr("startDateStr", getPara("startTime"));
		setAttr("endDateStr", getPara("endTime"));
		setAttr("orderBy", getPara("orderBy"));
		//setAttr("keyword", unicodeToGB(getPara("keyword")));
		setAttr("resultMsg",unicodeToGB(getPara("resultMsg")));
		render("/admin/content/list.html");
	}

	public void entry() {
		render("/admin/content/entry.html");
	}
	public void comment() {
		setAttr("cid",getPara("id"));
		setAttr("commentPage", Db.paginate(getParaToInt(0, 1), 30, "select c.*,u.nick", "from comment c,user u where c.uid=u.id and content_id="+getPara("id")+" order by c.id desc"));
		render("/admin/content/comment.html");
	}
	public void delete_comment() {
		Db.update("delete from comment where id=?",getParaToInt("id"));
		setAttr("resultMsg", "评论删除成功！");
		comment();
	}
	

	public void all() {
		render("/admin/content/all.html");
	}

	public void left() {
		render("/admin/content/left.html");
	}

	public void add() {
		String parentId = getPara("root");
		System.out.println("parentId================" + parentId);
		if (!"source".equals(parentId)) {
			setAttr("content", new Content().set("channel_id", parentId).set("is_recommend", 0));
			setAttr(
					"channelList",
					Channel.dao
							.find("SELECT node.* FROM channel node, "
									+ "channel parent WHERE node.lft >= parent.lft AND node.rgt <= parent.rgt "
									+ "and parent.parent_id is null ORDER BY node.lft"));
			setAttr("contentTypeList", ContentType.dao
					.find(" select type_id,type_name from content_type "));
		}
		String root = (getPara("root") == null || StrUtil.isBlank(getPara("root"))) ? "0" : getPara("root");
		setAttr("root", root);
		setAttr("title", getPara("title"));
		setAttr("creator", getPara("creator"));
		setAttr("is_recommend", getPara("is_recommend"));
		setAttr("type_id", getPara("type_id"));
		setAttr("status", getPara("status"));
		setAttr("startDateStr", getPara("startTime"));
		setAttr("endDateStr", getPara("endTime"));
		setAttr("orderBy", getPara("orderBy"));
		setAttr("keyword", getPara("keyword"));
		setAttr("pageNum", getPara("pageNum"));
		render("/admin/content/add.html");
	}

	public void save() {
		final String[] attachmentPaths = getParaValues("attachmentPaths");
		final String[] attachmentNames = getParaValues("attachmentNames");
		final String[] picMsgs = getParaValues("picMsgs");
		final Integer[] channel_ids = getParaValuesToInt("channel_id");
		// 获得关键字数组
		String keywords = getPara("keywords");
		final String[] keys = keywords.split("//");

		boolean succeed = Db.tx(new IAtom() {
			public boolean run() throws SQLException {
				Integer userId = (Integer)getSessionAttr("userId");
				//根据系统配置的是否需要审核功能来给status赋值
				int audit = Config.dao.findFirst("select value from config where code='contentCheck'").getInt("value");
				int status = (audit==1)?1:2;
				//自定义发布日期（没填写默认当前日期）
				Date customDate = null ;
				String customTime = getPara("content.custom_time");
				if(customTime==null || customTime.trim().equals("")){
					customDate = new Date();
				}else{
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					try {
						customDate = sdf.parse(customTime);
					} catch (java.text.ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				Content content = getModel(Content.class).set("status", status)
						.set("creator", userId)
						.set("release_date",DateFmt.addLongDays(0))
						.set("custom_time", customDate);
				boolean boo = content.save();
				//拿到刚刚添加文字的ID
				int contentId = Db.queryInt("select max(id) from content");
				
				//无需审核直接‘审核通过’的时候,要为这个文章建立所以
				if(status==2){
					try {
						Content contentForLucene = Content.dao.findById(contentId);
						if(contentForLucene!=null){
							content2Index(contentForLucene);
						}
					} catch (ParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				// 向关联表content_channel添加数据
				boolean isTrue = true;
				boolean isTrue2 = Db.save("content_channel", new Record().set(
						"content_id", contentId).set("channel_id",
						getParaToInt("content.channel_id")));
				if (channel_ids != null && channel_ids.length > 0) {
					for (int i = 0; i < channel_ids.length; i++) {
						boolean boo2 = Db.save("content_channel", new Record()
								.set("content_id", contentId).set("channel_id",
										channel_ids[i]));
						if (!boo2) {
							isTrue = false;
							break;
						}
					}
				}
				// 计算文章关键字权重，并向content_keywords表中新增关联数据
				if (keys != null && keys.length > 0) {
					for (int i = 0; i < keys.length; i++) {
						try {
							if (!"".equalsIgnoreCase(keys[i].trim())) {
								int weight = getKeyWeight(getPlainText(content.getStr("txt")),keys[i]);
								// 判断此关键字是否已经存在关键字表中 不存在先添加关键字到表中
								List<Keywords> keywordsList = Keywords.dao
										.find(
												" select * from keywords k where k.word = ? ",
												keys[i]);
								if (keywordsList == null
										|| keywordsList.size() == 0) {
									// 向关键字表新增这个关键字
									createKeyword(keys[i]);
									int keywordId = Db
											.queryInt(" select max(id) from keywords ");
									createRelateKey(content.getInt("id"),
											keywordId, weight);
								} else {
									// 也就是已经存在这个关键字的时候.直接找到关键字id然后向关联表新增
									int keywordId = Db
											.queryInt(
													" select id from keywords k where k.word = ? ",
													keys[i]);
									createRelateKey(contentId, keywordId,
											weight);
								}
							}
						} catch (ParserException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				// 保存附件
				if (attachmentPaths != null && attachmentPaths.length > 0) {
					for (int i = 0, len = attachmentPaths.length; i < len; i++) {
						if (!StrUtil.isBlank(attachmentPaths[i])
								&& !StrUtil.isBlank(attachmentNames[i])) {
							Db
									.update("insert into content_att (content_id,name,url,picMsg,priority) values ("
											+ contentId
											+ ",'"
											+ attachmentNames[i]
											+ "','"
											+ attachmentPaths[i]
											+ "','"
											+ picMsgs[i]
											+ "',"
											+ i
											+ ")");
						} else {
							setAttr("resultMsg", "<font color=red >附件名称和路径不能为空，信息发布失败。</font>");
							setAttr("status", "0");
						}
					}
				}
				//增添文章的点击量
				boolean isTrue3 = new ContentCount().set("content_id", contentId).save();
				return boo && isTrue && isTrue2 && isTrue3;
			}
		});
		String s="";
		if (succeed) {
			s="信息发布成功！";
		} else {
			if (!"0".equals(getAttr("status")))
				s="<font color=red >信息发布失败，重试或联系管理员。</font>";
		}
		s = toUnicode(s);
		String t = toUnicode(getPara("title"));
		String c = toUnicode(getPara("creator"));
		String k = toUnicode(getPara("keyword"));
		redirect("/admin/content/index?resultMsg="+s+"&root="+getPara("root")+"&title="+t+"&creator="+c+"&is_recommend="+getPara("is_recommend")+"&type_id="+getPara("type_id")+"&status="+getPara("status")+"&startTime="+getPara("startDateStr")+"&endTime="+getPara("endDateStr")+"&orderBy="+getPara("orderBy")+"&keyword="+k+"&pageNum="+getPara("pageNum")+"&is_href=1",true);
	}

	public void edit() {
		Content cat = Content.dao.findById(getParaToInt("id"));
		// 找出栏目的集合
		setAttr(
				"channelList",
				Channel.dao
						.find("SELECT node.* FROM channel node, "
								+ "channel parent WHERE node.lft >= parent.lft AND node.rgt <= parent.rgt "
								+ "and parent.parent_id is null ORDER BY node.lft"));
		// 找出对应的关键字集合，并拼成 str//str//str 类型的字符串返回
		StringBuffer strB = new StringBuffer();
		List<ContentKeywords> contentKeyList = ContentKeywords.dao
				.find(
						"select keywords_id from content_keywords where content_id = ?",
						getParaToInt("id"));
		if (contentKeyList != null && contentKeyList.size() > 0) {
			for (int i = 0; i < contentKeyList.size(); i++) {
				if (i != contentKeyList.size() - 1) {
					strB = strB.append(Keywords.dao.findById(
							contentKeyList.get(i).get("keywords_id")).getStr(
							"word")
							+ "//");
				} else {
					strB = strB.append(Keywords.dao.findById(
							contentKeyList.get(i).get("keywords_id")).getStr(
							"word"));
				}
			}
		}
		setAttr("keywords", strB);
		// 内容类型（普通，图文。。。集合）
		setAttr("contentTypeList", ContentType.dao
				.find(" select type_id,type_name from content_type "));
		// 查询出已有副栏目集合（此处要去掉关联表中跟content内容表中冗余的数据，即主栏目对应的那一条 如下sql）
		setAttr(
				"contentChannelList",
				ContentChannel.dao
						.find(
								" select channel_id from content_channel where channel_id not in ( select channel_id from content where id=? )  and content_id=? order by channel_id ",
								getParaToInt("id"), getParaToInt("id")));
		// 查询跟内容相对应的附件集合
		setAttr("content_att", Db.find(
				" select * from content_att where content_id = ? ",
				getParaToInt("id")));
		setAttr("content", cat);
		String root = (getPara("root") == null || StrUtil.isBlank(getPara("root"))) ? "0" : getPara("root");
		setAttr("root", root);
		setAttr("title", getPara("title"));
		setAttr("creator", getPara("creator"));
		setAttr("is_recommend", getPara("is_recommend"));
		setAttr("type_id", getPara("type_id"));
		setAttr("status", getPara("status"));
		setAttr("startDateStr", getPara("startTime"));
		setAttr("endDateStr", getPara("endTime"));
		setAttr("orderBy", getPara("orderBy"));
		setAttr("keyword", getPara("keyword"));
		setAttr("pageNum", getPara("pageNum"));
		render("/admin/content/edit.html");
	}

	public void update() {
		final String[] attachmentPaths = getParaValues("attachmentPaths");
		final String[] attachmentNames = getParaValues("attachmentNames");
		final String[] picMsgs = getParaValues("picMsgs");
		// 这个数组收集的是附件修改之前的路径
		final String[] attachmentPathHiddens = getParaValues("attachmentPathHiddens");
		boolean succeed = Db.tx(new IAtom() {
			public boolean run() throws SQLException {
				//首先得到修改前的status审核状态，如果是从‘审核通过’修改的证明这个文章已经存在lucene索引，所以要删除
				//如果不是从‘审核通过’修改证明文章还未存在索引无需操作删除lucene索引操作
				int oldStatus = Db.queryInt(" select status from content where id = ? ",getParaToInt("content.id"));
				
				//根据系统配置获得是否需要审核（关系到lucene索引）
				int audit = Config.dao.findFirst("select value from config where code='contentCheck'").getInt("value");
				int status = (audit==1)?1:2;
				
				//自定义发布日期（没填写默认当前日期）
				Date customDate = null ;
				String customTime = getPara("content.custom_time");
				if(customTime==null || customTime.trim().equals("")){
					customDate = new Date();
				}else{
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					try {
						customDate = sdf.parse(customTime);
					} catch (java.text.ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				final Integer[] channel_ids = getParaValuesToInt("channel_id");
				
				// 首先直接保存修改后的数据
				boolean flag1 = getModel(Content.class)
								.set("status", status)
								.set("custom_time", customDate)
								.update();
				
				// 这里开始操作content_keywords！首先删除contentID对应的关联表数据
				boolean flag2 = delKeyByContent(getParaToInt("content.id"));
				// 再向content_keywords表中添加新的数据
				String keywords = getPara("keywords");
				final String[] keys = keywords.split("//");
				// 计算文章关键字权重，并向content_keywords表中新增关联数据
				if (keys != null && keys.length > 0) {
					for (int i = 0; i < keys.length; i++) {
						try {
							if (!"".equalsIgnoreCase(keys[i].trim())) {
								int weight = getKeyWeight(getPlainText(getPara("content.txt")),keys[i]);
								// 判断此关键字是否已经存在关键字表中 不存在先添加关键字到表中
								List<Keywords> keywordsList = Keywords.dao
										.find(
												" select * from keywords k where k.word = ? ",
												keys[i]);
								if (keywordsList == null
										|| keywordsList.size() == 0) {
									// 向关键字表新增这个关键字
									createKeyword(keys[i]);
									int keywordId = Db
											.queryInt(" select max(id) from keywords ");
									createRelateKey(getParaToInt("content.id"),
											keywordId, weight);
								} else {
									// 也就是已经存在这个关键字的时候.直接找到关键字id然后向关联表新增
									int keywordId = Db
											.queryInt(
													" select id from keywords k where k.word = ? ",
													keys[i]);
									createRelateKey(getParaToInt("content.id"),
											keywordId, weight);
								}
							}
						} catch (ParserException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
				// 这里开始操作content_att！首先删除对应contentId原有的数据
				List<ContentAtt> attList = ContentAtt.dao.find(
						" select * from content_att where content_id = ? ",
						getParaToInt("content.id"));
				if (attList != null && attList.size() > 0) {
					for (int i = 0; i < attList.size(); i++) {
						boolean flag = ContentAtt.dao.deleteById(attList.get(i)
								.get("id"));
						if (!flag) {
							return flag;
						}
					}
				}
				// 再向表content_att添加新数据
				if (attachmentPaths != null && attachmentPaths.length > 0) {
					for (int i = 0, len = attachmentPaths.length; i < len; i++) {
						if (!StrUtil.isBlank(attachmentPaths[i])
								&& !StrUtil.isBlank(attachmentNames[i])) {
							Db
									.update("insert into content_att (content_id,name,url,picMsg,priority) values ("
											+ getParaToInt("content.id")
											+ ",'"
											+ attachmentNames[i]
											+ "','"
											+ attachmentPaths[i]
						                    + "','"
						                    + picMsgs[i]
											+ "',"
											+ i
											+ ")");
						} else {
							setAttr("resultMsg", "附件名称和路径不能为空，信息发布失败。");
							setAttr("status", "0");
						}
					}
				}
				
				// 这里开始操作content_channel关联表 ！！！首先删除对应contentId关联表中原有的数据
				delChannel(getParaToInt("content.id"));
				// 再向关联表content_channel添加新数据(主栏目的一个+副栏目N个)
				boolean flag3 = Db.save("content_channel", new Record().set(
						"content_id", getParaToInt("content.id")).set(
						"channel_id", getParaToInt("content.channel_id")));
				boolean flag4 = true;
				if (channel_ids != null && channel_ids.length > 0) {
					for (int i = 0; i < channel_ids.length; i++) {
						boolean boo2 = Db.save("content_channel", new Record()
								.set("content_id", getParaToInt("content.id"))
								.set("channel_id", channel_ids[i]));
						if (!boo2) {
							flag4 = false;
							break;
						}
					}
				}
				// 如果前面的修改步骤都正确 这里开始修改实体文件
				if (flag1 && flag2 && flag3 && flag4) {
					// 判断是否修改了图片 如果修改了 删除之前对应图片
					String titleBefore = getPara("titleHidden");
					String titleAfter = getPara("content.title_img");
					if (!titleBefore.equals(titleAfter)) {
						delFile(titleBefore);
					}
					String contentBefore = getPara("contentHidden");
					String contentAfter = getPara("content.content_img");
					if (!contentBefore.equals(contentAfter)) {
						delFile(contentBefore);
					}
					String typeBefore = getPara("typeHidden");
					String typeAfter = getPara("content.type_img");
					if (!typeBefore.equals(typeAfter)) {
						delFile(typeBefore);
					}
					// 最后根据修改前和修改后的附件进行比较判断是否有要删除的实体文件
					if (attachmentPathHiddens != null
							&& attachmentPathHiddens.length > 0) {
						a: for (int i = 0; i < attachmentPathHiddens.length; i++) {
							if (attachmentPaths != null
									&& attachmentPaths.length > 0) {
								for (int j = 0; j < attachmentPaths.length; j++) {
									if (attachmentPathHiddens[i]
											.equals(attachmentPaths[j])) {
										continue a;
									}
								}
							}
							delFile(attachmentPathHiddens[i]);
						}
					}
				}
				// 如果前面的修改步骤都正确 操作lucene索引
				if (flag1 && flag2 && flag3 && flag4) {
					try {
						//如果之前就是审核通过修改完如果系统配置给的还是‘审核通过’的话，先删除之前索引，在建立新的索引
						if(status==2 && oldStatus==2){
							deleteIndex(getParaToInt("content.id"));
							try {
								content2Index(Content.dao.findById(getPara("content.id")));
							} catch (ParserException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						//如果修改后是“通过”而之前是未通过或者未审核就要新增这个文章索引
						if(status==2 && (oldStatus==1 || oldStatus==3 || oldStatus==0)){
							try {
								content2Index(Content.dao.findById(getPara("content.id")));
							} catch (ParserException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						//如果修改后为‘待审核‘，而之前是’通过’，那么删除之前的索引
						if(status==1 && oldStatus==2){
							deleteIndex(getParaToInt("content.id"));
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return flag1 && flag2 && flag3 && flag4;
			}
		});
		String s="";
		if (succeed) {
			s="数据保存成功。";
		} else {
			s="<font color=red >数据保存失败，请重新录入。</font>";
		}
		s=toUnicode(s);
		String t = toUnicode(getPara("title"));
		String c = toUnicode(getPara("creator"));
		String k = toUnicode(getPara("keyword"));
		redirect("/admin/content/index?resultMsg="+s+"&root="+getPara("root")+"&title="+t+"&creator="+c+"&is_recommend="+getPara("is_recommend")+"&type_id="+getPara("type_id")+"&status="+getPara("status")+"&startTime="+getPara("startDateStr")+"&endTime="+getPara("endDateStr")+"&orderBy="+getPara("orderBy")+"&keyword="+k+"&pageNum="+getPara("pageNum")+"&is_href=1",true);
	}
	
	/**
	 * 跳转查看页面
	 */
	public void toLook(){
		int contentId = getParaToInt("id");
		Content content = Content.dao.findById(contentId);
		content.set("creator", User.dao.findById(content.get("creator")));
		content.set("channel_id", Channel.dao.findById(content.get("channel_id")));
		setAttr("content", content);
		String root = (getPara("root") == null || StrUtil.isBlank(getPara("root"))) ? "0" : getPara("root");
		setAttr("root", root);
		setAttr("title", getPara("title"));
		setAttr("creator", getPara("creator"));
		setAttr("is_recommend", getPara("is_recommend"));
		setAttr("type_id", getPara("type_id"));
		setAttr("status", getPara("status"));
		setAttr("startDateStr", getPara("startTime"));
		setAttr("endDateStr", getPara("endTime"));
		setAttr("orderBy", getPara("orderBy"));
		setAttr("keyword", getPara("keyword"));
		setAttr("pageNum", getPara("pageNum"));
		render("/admin/content/look.html");
	}
	
	/**
	 * 审核与退回
	 */
	public void examine(){
		if(getPara("status")!=null && !"".equals(getPara("status").trim())){
			int newStatus = getParaToInt("status");
			Content content = Content.dao.findById(getParaToInt("contentId"));
			int oldStatus = content.getInt("status");
			boolean succeed = content.set("status", newStatus).update();
			String s="";
			if (succeed) {
				s="内容审核成功！";
				//当之前的状态为‘待审核1’与‘未通过3’现在的状态为‘通过2’时，把文章加入lucene索引
				//如果之前的状态就为‘通过2’则不做lucene的新增
				if(newStatus==2 && (oldStatus==1 || oldStatus==3 || oldStatus==0)){
					try {
						content2Index(content);
					} catch (ParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//如果现在的状态为‘未通过3’而之前的状态为‘通过2’时。要删除之前的lucene索引
				if(newStatus==3 && oldStatus==2){
					try {
						deleteIndex(getParaToInt("contentId"));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				s="<font color=red >内容审核失败，重试或联系管理员。</font>";
			}
			s=toUnicode(s);
			String t = toUnicode(getPara("title"));
			String c = toUnicode(getPara("creator"));
			String k = toUnicode(getPara("keyword"));
			redirect("/admin/content/index?resultMsg="+s+"&root="+getPara("root")+"&title="+t+"&creator="+c+"&is_recommend="+getPara("is_recommend")+"&type_id="+getPara("type_id")+"&startTime="+getPara("startTime")+"&endTime="+getPara("endTime")+"&orderBy="+getPara("orderBy")+"&keyword="+k+"&pageNum="+getPara("pageNum")+"&is_href=1",false);
		}
	}
	
	/**
	 * 批量审核通过
	 */
	public void examineForBatch(){
		String s="";
		String root = (getPara("root") == null || StrUtil.isBlank(getPara("root"))) ? "source"
				: getPara("root");
		setAttr("root", root);
		final Integer[] ids = getParaValuesToInt("ids");
		if (ids != null && ids.length > 0) {
			boolean succeed = Db.tx(new IAtom() {
				public boolean run() throws SQLException {
					Arrays.sort(ids);
					for (int i = ids.length - 1; i >= 0; i--) {
						try {
							if (!examineForOne(ids[i])) {
								return false;
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					return true;
				}
			});
			if (succeed) {
				s="审核成功。";
			} else {
				if (!"1".equals(getAttr("status")) && !"2".equals(getAttr("status")))
					s="审核失败，请重试。";
			}
			s=toUnicode(s);
		} else {
			if (getParaToInt("id") > 0) {
				boolean succeed = Db.tx(new IAtom() {
					public boolean run() throws SQLException {
						boolean flag = false;
						try {
							flag = examineForOne(getParaToInt("id"));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return flag;
					}
				});
				if (succeed) {
					s="审核成功。";
				} else {
					if (!"1".equals(getAttr("status")) && !"2".equals(getAttr("status")))
						s="审核失败，请重试。";
				}
				s=toUnicode(s);
			}
		}
		String t = toUnicode(getPara("title"));
		String c = toUnicode(getPara("creator"));
		String k = toUnicode(getPara("keyword"));
		redirect("/admin/content/index?resultMsg="+s+"&root="+getPara("root")+"&title="+t+"&creator="+c+"&is_recommend="+getPara("is_recommend")+"&type_id="+getPara("type_id")+"&status="+getPara("status")+"&startTime="+getPara("startTime")+"&endTime="+getPara("endTime")+"&orderBy="+getPara("orderBy")+"&keyword="+k+"&pageNum="+getPara("pageNum")+"&is_href=1",false);

	}
	/**
	 * 为批量审核写的单一审核方法
	 */
	public boolean examineForOne(int contentId){
		boolean finalFlag = false;
		int newStatus = 2;
		Content content = Content.dao.findById(contentId);
		int oldStatus = content.getInt("status");
		boolean succeed = content.set("status", newStatus).update();
		if (succeed) {
			//当之前的状态为‘待审核1’与‘未通过3’现在的状态为‘通过2’时，把文章加入lucene索引
			//如果之前的状态就为‘通过2’则不做lucene的新增
			if(oldStatus==1 || oldStatus==3 || oldStatus==0){
				try {
					content2Index(content);
				} catch (ParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			finalFlag =  true;
		}
		return finalFlag;
	}

	
	/**
	 * 左侧树（栏目channel）
	 */
	public void tree() {
		String root = getPara("root");
		boolean isRoot;
		// jquery treeview的根请求为root=source
		if (root == null || StrUtil.isBlank(root) || "source".equals(root)) {
			isRoot = true;
		} else {
			isRoot = false;
		}
		setAttr("isRoot", isRoot);
		List<Channel> list;
		// 导入数据：insert into channel
		// (id,parent_id,name,path,lft,rgt,priority,is_display) select
		// a.channel_id,a.parent_id,b.channel_name,a.channel_path,a.lft,a.rgt,a.priority,a.is_display
		// from jc_channel a,jc_channel_ext b where a.channel_id=b.channel_id;
		if (isRoot) {
			list = Channel.dao
					.find("select e.*,c.id as child from (select d.* from channel d "
							+ "where d.parent_id is null) as e left join (select distinct a.id from channel a "
							+ "left join channel b on a.id=b.parent_id where a.parent_id is null and "
							+ "b.id is not null) as c on e.id=c.id "
							+ "order by e.priority asc,e.id asc");
		} else {
			Integer rootId = Integer.valueOf(root);
			list = Channel.dao
					.find(
							"select e.*,c.id as child from (select d.* from channel d "
									+ "where d.parent_id =?) as e left join (select distinct a.id from channel a "
									+ "left join channel b on a.id=b.parent_id where a.parent_id =? and "
									+ "b.id is not null) as c on e.id=c.id "
									+ "order by e.priority asc,e.id asc",
							rootId, rootId);
		}
		setAttr("list", list);
		getResponse().setHeader("Cache-Control", "no-cache");
		getResponse().setContentType("text/json;charset=UTF-8");
		render("/admin/content/tree.html");
	}

	public void delete() {
		String s="";
		String root = (getPara("root") == null || StrUtil.isBlank(getPara("root"))) ? "source"
				: getPara("root");
		System.out.println("root====" + root);
		setAttr("root", root);
		final Integer[] ids = getParaValuesToInt("ids");
		if (ids != null && ids.length > 0) {
			boolean succeed = Db.tx(new IAtom() {
				public boolean run() throws SQLException {
					Arrays.sort(ids);
					for (int i = ids.length - 1; i >= 0; i--) {
						try {
							if (!delOne(ids[i])) {
								return false;
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					return true;
				}
			});
			if (succeed) {
				s="数据删除成功。";
			} else {
				if (!"1".equals(getAttr("status")) && !"2".equals(getAttr("status")))
					s="数据删除失败，请重试。";
			}
			s=toUnicode(s);
		} else {
			if (getParaToInt("id") > 0) {
				boolean succeed = Db.tx(new IAtom() {
					public boolean run() throws SQLException {
						boolean flag = false;
						try {
							flag = delOne(getParaToInt("id"));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return flag;
					}
				});
				if (succeed) {
					s="数据删除成功。";
				} else {
					if (!"1".equals(getAttr("status")) && !"2".equals(getAttr("status")))
						s="数据删除失败，请重试。";
				}
				s=toUnicode(s);
			}
		}
		String t = toUnicode(getPara("title"));
		String c = toUnicode(getPara("creator"));
		String k = toUnicode(getPara("keyword"));
		redirect("/admin/content/index?resultMsg="+s+"&root="+getPara("root")+"&title="+t+"&creator="+c+"&is_recommend="+getPara("is_recommend")+"&type_id="+getPara("type_id")+"&status="+getPara("status")+"&startTime="+getPara("startTime")+"&endTime="+getPara("endTime")+"&orderBy="+getPara("orderBy")+"&keyword="+k+"&pageNum="+getPara("pageNum")+"&is_href=1",false);
	}

	private boolean delOne(int contentId) throws IOException {
		// 删除前获得对象的status状态  如果是‘已通过2’就要删除对应的lucene索引
		int status = Db.queryInt(" select status from content where id ="+contentId);
		// 删除关联表content_channel中数据
		boolean count1 = delChannel(contentId);
		// 删除关键字关联表对应的content记录
		boolean count4 = delKeyByContent(contentId);
		// 删除content记录中对应的图片
		delImg(contentId);
		// 删除对应附件表的数据和实体文件
		boolean count2 = delAtt(contentId);
		// 删除内容对应的点击量（centent_count表）
		ContentCount conCun = ContentCount.dao.findFirst(" select id from content_count where content_id = ? ", contentId);
		boolean count5 = conCun.delete();
		// 最后删除内容content表中的数据
		boolean count3 = Content.dao.deleteById(contentId);
		// 如果都正确的时候再删除对应的lucene索引
		if (count1 && count2 && count3 && count4) {
			if(status==2){
				deleteIndex(contentId);
			}
		}
		return count1 && count2 && count3 && count4;
	}

	/**
	 * 删除对应contentId下的栏目
	 * 
	 * @param contentId
	 * @return
	 */
	private boolean delChannel(int contentId) {
		List<ContentChannel> list = ContentChannel.dao.find(
				" select id from content_channel where content_id = ? ",
				contentId);
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				boolean flag = ContentChannel.dao.deleteById(list.get(i).get(
						"id"));
				if (!flag) {
					return flag;
				}
			}
		}
		return true;
	}

	/**
	 * 删除对应内容下的图片
	 * 
	 * @param contentId
	 */
	private void delImg(int contentId) {
		Content content = Content.dao.findById(contentId);
		if (content != null) {
			if (content.getStr("title_img") != null
					&& content.getStr("title_img").trim() != "") {
				delFile(content.getStr("title_img"));
			}
			if (content.getStr("content_img") != null
					&& content.getStr("content_img").trim() != "") {
				delFile(content.getStr("content_img"));
			}
			if (content.getStr("type_img") != null
					&& content.getStr("type_img").trim() != "") {
				delFile(content.getStr("type_img"));
			}
		}
	}

	/**
	 * 删除对应内容下的附件
	 * 
	 * @param contentId
	 * @return
	 */
	private boolean delAtt(int contentId) {
		String realPath = this.getRequest().getRealPath("/");
		List<ContentAtt> list = ContentAtt.dao.find(
				" select * from content_att where content_id = ? ", contentId);
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				// 这里是删除数据库中的记录
				boolean flag = ContentAtt.dao.deleteById(list.get(i).get("id"));
				if (!flag) {
					return flag;
				}
				// 这里开始是 删除对应的实体文件
				delFile(list.get(i).getStr("url"));
			}
		}
		return true;
	}

	/**
	 * 基于项目文件命名规则 写了传入文件名称删除对应文件的函数
	 * 
	 * @param fileName
	 */
	private void delFile(String fileName) {
		String realPath = getRequest().getRealPath("/");
		File file = new File(realPath + fileName);
		if (file.exists() && file.isFile()) {
			file.delete();
		}
	}

	/**
	 * 用户前台搜索时候调用这个函数
	 * 
	 * @throws IOException
	 * @throws InvalidTokenOffsetsException
	 * @throws ParserException
	 * @throws ParseException
	 */
	@SuppressWarnings("unchecked")
	public void userSearch() throws IOException, InvalidTokenOffsetsException,
			ParserException, ParseException {
		// 根据用户查询关键字找到所有记录
		String queryStr = getPara("queryStr");
		List list = search(queryStr);
		// 当前页
		int pageNum = 1;
		if (getParaToInt("pageNum") != null) {
			pageNum = getParaToInt("pageNum");
		}
		// 开始条数
		int startNum = (pageNum - 1) * pageSize;
		if (startNum < 0 || startNum > list.size()) {
			startNum = 0;
		}
		// 结束条数
		int endNum = pageNum * pageSize;
		if (endNum > list.size()) {
			endNum = list.size();
		}
		// 总条数
		int totalRow = list.size();
		// 总页数
		int totalPage = 0;
		if((totalRow % pageSize)==0){
			totalPage = totalRow / pageSize;
		}else{
			totalPage = (totalRow / pageSize) + 1;
		}
		// 分页后的List集合
		List contentList = list.subList(startNum, endNum);
		// 封装到Page对象中发到页面
		Page<Content> contentPage = new Page<Content>(contentList, pageNum,
				pageSize, totalPage, totalRow);
		setAttr("contentPage", contentPage);
		setAttr("queryStr", queryStr);
		render("/admin/content/test.html");
	}

	/**
	 * 把数据库现有数据加到lucene索引中。在调用此方法前要删除webRoot下的luceneIndex文件夹,否则数据会冗余
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	public void dateToIndex() throws IOException, ParserException {
		// 找到所有的content对象 把对应的属性加到lucene索引和文件中
		List<Content> contentList = Content.dao
				.find(" select id,title,txt from content where status=2");
		for (int i = 0; i < contentList.size(); i++) {
			Content content = contentList.get(i);
			Document document = this.content2Document(content);
			createIndex(document);
		}
		render("/admin/content/test.html");
	}

	/**
	 * 把传过来的content对象直接加到lucene索引中
	 * 
	 * @param content
	 * @throws ParserException
	 */
	public void content2Index(Content content) throws ParserException {
		createIndex(content2Document(content));
	}

	/**
	 * 传过来的content对象加到document中
	 * 
	 * @param content
	 * @return
	 * @throws ParserException
	 */
	public Document content2Document(Content content) throws ParserException {
		Document document = new Document();
		document.add(new Field("id", content.getInt("id").toString(),Store.YES, Index.NOT_ANALYZED));
		document.add(new Field("title", getPlainText(content.getStr("title")),Store.YES, Index.ANALYZED));
		if (content.getStr("txt") != null && !StrUtil.isBlank(content.getStr("txt"))) {
			if(getPlainText(content.getStr("txt"))!=null && !getPlainText(content.getStr("txt")).trim().equals("")){
				document.add(new Field("txt", getPlainText(content.getStr("txt")),Store.NO, Index.ANALYZED));
			}
		}
		return document;
	}

	/**
	 * 传入document对象加入索引
	 * 
	 * @param document
	 */
	public void createIndex(Document document) {
		String indexPath = this.getRequest().getRealPath("/") + "/luceneIndex";
		// 设定lucene索引存放目录，如果不存在就创建
		File indexFile = new File(indexPath);
		if (!indexFile.exists()) {
			indexFile.mkdir();
		}
		Directory directory = null;
		try {
			directory = FSDirectory.open(indexFile);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_36,
				analyzer);
		IndexWriter indexWriter = null;
		try {
			indexWriter = new IndexWriter(directory, conf);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			indexWriter.addDocument(document);
			indexWriter.close();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 根据传过来的contentID来删除对应lucene目录下的索引
	 * 
	 * @param contentId
	 * @throws IOException
	 */
	public void deleteIndex(int contentId) throws IOException {
		String indexPath = this.getRequest().getRealPath("/") + "/luceneIndex";
		// 设定lucene索引存放目录，当存在这个目录时候才能执行删除，不存在就什么都不做
		File indexFile = new File(indexPath);
		if (indexFile.exists()) {
			Directory dir = FSDirectory.open(indexFile);
			boolean exist = IndexReader.indexExists(dir);
			if (exist) {
				IndexWriterConfig con = new IndexWriterConfig(
						Version.LUCENE_36, analyzer);
				IndexWriter writer = new IndexWriter(dir, con);
				try {
					writer.deleteDocuments(new Term("id", String
							.valueOf(contentId)));
				} finally {
					writer.close();
				}
			}
		}
	}

	/**
	 * 传入查询关键字返回contentList
	 * 
	 * @param queryStr
	 * @return contentList
	 * @throws IOException
	 * @throws InvalidTokenOffsetsException
	 * @throws ParserException
	 * @throws ParseException
	 */
	public List<Content> search(String queryStr) throws IOException,
			InvalidTokenOffsetsException, ParserException, ParseException {
		List<Content> contentList = new ArrayList<Content>();
		if(queryStr!=null && queryStr.trim().length()>0){
			String indexPath = this.getRequest().getRealPath("/") + "/luceneIndex";
			// 要在那个字段查询
			String[] fields = new String[] { "title", "txt" };
			QueryParser qp = new MultiFieldQueryParser(Version.LUCENE_36, fields,new IKAnalyzer(false));
			qp.setDefaultOperator(QueryParser.AND_OPERATOR);
			Query query = qp.parse(queryStr);
			System.out.println("*************************"+query.toString()+"*******************");
			// 指定那个在哪个索引文件中查找
			File indexFile = new File(indexPath);
			Directory directory = FSDirectory.open(indexFile);
			IndexReader indexReader = IndexReader.open(directory);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			// 过滤掉不想展示的结果Filter
			TopDocs topDocs = indexSearcher.search(query, 999);
			// 高亮设置
			// 设定高亮显示的格式，也就是对高亮显示的词组加上前缀后缀
			SimpleHTMLFormatter simpleHtmlFormatter = new SimpleHTMLFormatter(
					"<b><font color='red'>", "</font></b>");
			Highlighter highlighter = new Highlighter(simpleHtmlFormatter,
					new QueryScorer(query));
			// 设置每次返回的字符数
			highlighter.setTextFragmenter(new SimpleFragmenter(80));
			System.out.println("**********总共有【" + topDocs.totalHits + "】条匹配的结果**********");
			ScoreDoc[] scoreDoc = topDocs.scoreDocs;
			for (int i = 0; i < scoreDoc.length; i++) {
				int docSn = scoreDoc[i].doc;// 文档内部编号
				Document document = indexSearcher.doc(docSn);// 根据文档编号取出相应的文档
				if (document.get("id") != null) {
					// 根据lucene中存放的id找到content对象
					Content content = Content.dao.findById(document.get("id"));
					if(content==null){
						continue;
					}
					// 吧标题和内容转换为纯文本字段
					content.set("title", getPlainText(content.getStr("title")));
					content.set("txt", getPlainText(content.getStr("txt")));
					// 这里开始处理文字的高亮
					String showTxt = "";
					if (content.getStr("txt") != null
							&& !"".equals(content.getStr("txt").trim())) {
						TokenStream tokenStreamTxt = analyzer.tokenStream("txt",
								new StringReader(content.getStr("txt")));
						showTxt = highlighter.getBestFragment(tokenStreamTxt,
								content.getStr("txt"));
					}
					TokenStream tokenStreamTitle = analyzer.tokenStream("title",
							new StringReader(content.getStr("title")));
					String showTitle = highlighter.getBestFragment(
							tokenStreamTitle, content.getStr("title"));
					// 标题里含搜索关键字的时候才把加上格式的字符串重新赋值给title属性
					if (showTitle != null && !"".equals(showTitle.trim())) {
						content.set("title", showTitle);
					}
					// 如果内容中包含搜索关键字就直接把加上格式的字符串赋值给txt属性，如果没有则从开始截取一段字符展示
					if (showTxt != null && !"".equals(showTxt.trim())) {
						content.set("txt", showTxt);
					} else {
						if (content.getStr("txt") != null
								&& !"".equals(content.getStr("txt").trim())) {
							if (content.getStr("txt").length() >= 100) {
								content.set("txt", content.getStr("txt").substring(
										0, 100)
										+ "...");
							} else {
								content.set("txt", content.getStr("txt"));
							}
						} else {
							content.set("txt", "");
						}
					}
					contentList.add(content);
				}
			}
			return contentList;
		}
		return contentList;
	}

	/**
	 * 获取html纯文本信息
	 * 
	 * @param str
	 * @return
	 * @throws ParserException
	 */
	public String getPlainText(String str) throws ParserException {
		Parser parser = new Parser();
		if (str != null && !"".equalsIgnoreCase(str.trim())) {
			parser.setInputHTML(str);
			StringBean sb = new StringBean();
			// 设置不需要得到页面所包含的链接信息
			sb.setLinks(false);
			// 设置将不间断空格由正规空格所替代
			sb.setReplaceNonBreakingSpaces(true);
			// 设置将一序列空格由一个单一空格所代替
			sb.setCollapse(true);
			parser.visitAllNodesWith(sb);
			return sb.getStrings();
		} else {
			return "";
		}
	}
	
	/**
	 * 中文转换成Unicode码
	 * @param str
	 * @return
	 */
	public String toUnicode(String str){
        char[]arChar=str.toCharArray();
        int iValue=0;
        String uStr="";
        for(int i=0;i<arChar.length;i++){
            iValue=(int)str.charAt(i);           
            if(iValue<=256){
              // uStr+="& "+Integer.toHexString(iValue)+";";
                uStr+="\\"+Integer.toHexString(iValue);
            }else{
              // uStr+="&#x"+Integer.toHexString(iValue)+";";
                uStr+="\\u"+Integer.toHexString(iValue);
            }
        }
        return uStr;
    }
	/**
	 * unicode码转换成中文
	 * @param s
	 * @return
	 */
	 public String unicodeToGB(String   s)   {
		 if(s!=null && !"".equals(s.trim())){
	         StringBuffer   sb   =   new   StringBuffer();      
	         StringTokenizer   st   =   new   StringTokenizer(s,   "\\u");      
	         while   (st.hasMoreTokens())   {      
	             sb.append(   (char)   Integer.parseInt(st.nextToken(),   16));      
	         }      
	         return   sb.toString();      
	 	}
	 	return "";
     }

	
	/**
	 * 提取关键字的异步处理函数,找到用户的关键字集合到文章中匹配，并返回权重打的关键字
	 * @throws ParserException
	 * @throws IOException
	 */
	public void drawKeywords() throws ParserException, IOException {
		Map<String, Integer> keyMap = new HashMap<String, Integer>();
		// 收集到前台传来的字符串
		HttpServletRequest request = this.getRequest();
		String str = request.getParameter("str");
		str = getPlainText(str);
		// 从数据库查询关键字
		List<Keywords> keywordsList = Keywords.dao
				.find(" select * from keywords ");
		//调用keyMap()函数来返回文章中出现的关键字和相应的次数
		keyMap=getKeyMap(str,keywordsList);
		HttpServletResponse response = this.getResponse();
		response.setContentType("text/xml;charset=UTF-8");
		PrintWriter out = response.getWriter();
		//截取权重最大的前3个关键字拼成字符返回给用户
		out.write(mapSort(keyMap, 3));
		renderNull();
	}
	
	/**
	 * 找出文章中所出现的关键字的Map集合 。返回：Map<出现的关键字, 权重>
	 * @param text
	 * @param list
	 * @return
	 */
	public Map<String, Integer> getKeyMap(String text,List<Keywords> list){
		Map<String, Integer> keyMap = new HashMap<String, Integer>();
		int count=0;
        for (int i = 0; i < list.size(); i++) {
        	count=getKeyWeight(text,list.get(i).getStr("word"));
        	if(count!=0){
        		keyMap.put(list.get(i).getStr("word"), count);
        	}
		}
        return keyMap;
    }
	
	/**
	 * 关键字在文本中的权重
	 * @param text 对应的文本字段
	 * @param key	要查询的关键字
	 * @return
	 */
	public int getKeyWeight(String text,String key){
        int count =0;
        int start =0;
        while((start=text.indexOf(key,start))>=0){
            start += key.length();
            count ++;
        }
        return count;
    }

	/**
	 * 根据关键字和权重的Map返回定义条数的关键字
	 * 
	 * @param keyMap
	 *            在文章中找到的关键字和权重的Map集合
	 * @param subNum
	 *            要取得权重较大的前几条关键字
	 * @return
	 */
	public String mapSort(Map keyMap, int subNum) {
		if(keyMap.isEmpty()){
			return "";
		}
		StringBuffer strB = new StringBuffer();
		List<Map.Entry<String, Integer>> list_Data = new ArrayList<Map.Entry<String, Integer>>(
				(Collection<? extends Entry<String, Integer>>) keyMap
						.entrySet());
		Collections.sort(list_Data,
				new Comparator<Map.Entry<String, Integer>>() {
					public int compare(Map.Entry<String, Integer> o1,
							Map.Entry<String, Integer> o2) {
						if (o2.getValue() != null && o1.getValue() != null
								&& o2.getValue().compareTo(o1.getValue()) > 0) {
							return 1;
						} else {
							return -1;
						}

					}
				});
		if (subNum <= list_Data.size()) {
			for (int i = 0; i < subNum; i++) {
				if (i != subNum - 1) {
					strB.append(list_Data.get(i).getKey() + "//");
				} else {
					strB.append(list_Data.get(i).getKey());
				}
			}
		} else {
			for (int i = 0; i < list_Data.size(); i++) {
				if (i != list_Data.size() - 1) {
					strB.append(list_Data.get(i).getKey() + "//");
				} else {
					strB.append(list_Data.get(i).getKey());
				}
			}
		}
		return strB.toString();
	}

	/**
	 * 向文章和关键字的关联表增添数据
	 * 
	 * @param contentId
	 * @param keywordId
	 * @return
	 */
	public boolean createRelateKey(int contentId, int keywordId, int weight) {
		boolean flag = false;
		flag = new ContentKeywords().set("content_id", contentId).set(
				"keywords_id", keywordId).set("weight", weight).save();
		return flag;
	}

	/**
	 * 根据文章ID删除在文章和关键字的关联表中的数据
	 * 
	 * @param contentId
	 * @return
	 */
	public boolean delKeyByContent(int contentId) {
		boolean flag = false;
		List<ContentKeywords> list = ContentKeywords.dao.find(
				"select id from content_keywords where content_id = ? ",
				contentId);
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				flag = ContentKeywords.dao.deleteById(list.get(i).get("id"));
				if (!flag) {
					return flag;
				}
			}
		} else {
			flag = true;
		}
		return flag;
	}

	// **********************************这里是操作关键字表的函数**************************
	public void tagEntry() {
		render("/admin/content/tag/entry.html");
	}

	public void tagAll() {
		render("/admin/content/tag/all.html");
	}

	public void tagLeft() {
		render("/admin/content/tag/left.html");
	}
	/**
	 * 关键字首页数据加载
	 */
	public void tagIndex(){
		Page<Keywords> page = null;
		String sql = " from keywords w where 1=1 ";
		String searchWord = getPara("searchWord");
		if(searchWord!=null && !"".equals(searchWord.trim())){
			sql += " and w.word like '%"+searchWord.trim()+"%'";
		}
		sql += " order by id desc ";
		
		// 当前页
		int pageNum = 1;
		if (getParaToInt("pageNum") != null) {
			pageNum = getParaToInt("pageNum");
		}
		page = Keywords.dao.paginate(pageNum, 16, "select *",sql);
		setAttr("keywordsPage", page);
		setAttr("searchWord", searchWord);
		render("/admin/content/tag/list.html");
	}
	
	public void tagAdd(){
		createToken("myToken", 30*60);
		render("tag/add.html");
	}
	@Before(ContentValidator.class)
	public void tagSave(){
		boolean flag=getModel(Keywords.class).save();
		if(flag){
			setAttr("resultMsg", "添加关键字成功。");
		}else{
			setAttr("resultMsg", "<font color=red >关键字添加失败，请重新操作。</font>");
		}
		forwardAction("/admin/content/tagIndex");
	}
	public void tagEdit(){
		createToken("myToken", 30*60);
		if(getPara(0)!=null){
			setAttr("keywords", Keywords.dao.findById(getParaToInt(0)));
		}
		render("tag/edit.html");
	}
	@Before(ContentValidator.class)
	public void tagUpdate(){
		boolean flag=getModel(Keywords.class).update();
		if(flag){
			setAttr("resultMsg", "修改关键字成功。");
		}else{
			setAttr("resultMsg", "<font color=red >关键字修改失败，请重新操作。</font>");
		}
		forwardAction("/admin/content/tagIndex");
	}
	/**
	 * 关键词的数据库增添函数
	 * 
	 * @param keyword
	 * @return
	 */
	public boolean createKeyword(String keyword) {
		boolean flag = false;
		flag = new Keywords().set("word", keyword).save();
		return flag;
	}

	/**
	 * 用户前台操作删除关键字都调用这个函数
	 */
	public void deleteKeywords() {
		final Integer[] ids = getParaValuesToInt("ids");
		if (ids != null && ids.length > 0) {
			boolean succeed = Db.tx(new IAtom() {
				public boolean run() throws SQLException {
					Arrays.sort(ids);
					for (int i = ids.length - 1; i >= 0; i--) {
						if (!deleteOneKeyword(ids[i])) {
							return false;
						}
					}
					return true;
				}
			});
			if (succeed) {
				setAttr("resultMsg", "关键字删除成功。");
			} else {
				setAttr("resultMsg", "<font color=red >删除失败，存在文章关联，如需要请先删除关联文章。</font>");
			}
		} else {
			if (getParaToInt(0) > 0) {
				boolean succeed = Db.tx(new IAtom() {
					public boolean run() throws SQLException {
						boolean flag = false;
						flag = deleteOneKeyword(getParaToInt(0));
						return flag;
					}
				});
				if (succeed) {
					setAttr("resultMsg", "关键字删除成功。");
				} else {
					setAttr("resultMsg", "<font color=red >删除失败，存在文章关联，如需要请先删除关联文章。</font>");
				}
			}
		}
		forwardAction("/admin/content/tagIndex");
	}
	/**
	 * 根据关键字ID删除关键字表中的一条数据
	 * 
	 * @param keywordId
	 * @return
	 */
	public boolean deleteOneKeyword(int keywordId) {
		boolean flag = false;
		List<ContentKeywords> list = ContentKeywords.dao.find(
				" select id from content_keywords where keywords_id = ? ",
				keywordId);
		if (list == null || list.size() == 0) {
			flag = Keywords.dao.deleteById(keywordId);
			if (!flag) {
				return flag;
			}
		}
		return flag;
	}
	
	/**
	 * 添加关键字  时候判断是否已经存在这个关键的异步处理函数
	 * @throws IOException
	 */
	public void isContainsKey() throws IOException{
		String result=null;
		String keyword = getPara("keyword");
		List<Keywords> list = Keywords.dao.find("select id from keywords where word = ? ", keyword);
		if(list==null || list.size()==0){
			result="<font color=green >*关键词可用*</font>";
		}else{
			result="<font color=red >*已包含此关键词*</font>";
		}
		HttpServletResponse response = this.getResponse();
		response.setContentType("text/xml;charset=UTF-8");
		PrintWriter out = response.getWriter();
		out.write(result);
		renderNull();
	}
	
	private int getLen(int len, float imgScale) {
		return Math.round(len / imgScale);
	}
}
