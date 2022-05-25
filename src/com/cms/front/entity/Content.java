package com.cms.front.entity;

import java.util.List;

import com.cms.admin.channel.Channel;
import com.cms.admin.content.ContentKeywords;
import com.cms.util.StrUtils;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;

@SuppressWarnings("serial")
public class Content extends Model<Content> {
	public static final Content dao = new Content();
	
	public List<Content> getListByChannelIdsForTag(Integer[] channelIds,
			Integer[] typeIds, Boolean titleImg, Boolean recommend,
			String title, int orderBy, int option, Integer first, Integer count,Integer excludeId,int contentId) {
		
		String sql = null;

        //只包含栏目本身的内容
        if(option==0){
		    sql = "select a.id,a.title,a.creator,a.description,a.txt,a.release_date,a.custom_time,a.title_img,a.content_img,a.type_img,b.path,u.avatar from content a, channel b,content_count d,user u where a.creator=u.id and a.channel_id=b.id and a.id=d.content_id and a.type_id in ("+StrUtils.arr2String(typeIds)+") and a.channel_id in ("+StrUtils.arr2String(channelIds)+")";
        }
        //包含栏目所有子栏目的内容
        else if(option == 1){
    		String add = "select id from channel where 1=2";
            for(Integer channelId:channelIds){
            	Channel c = Channel.dao.findById(channelId);
            	add = add + " or (lft>="+c.getInt("lft")+" and rgt<="+c.getInt("rgt")+")";
            }
            sql = "select a.id,a.title,a.creator,a.txt,a.description,a.release_date,a.custom_time,a.title_img,a.content_img,a.type_img,b.path,u.avatar from content a, channel b,content_count d,user u where a.creator=u.id and a.channel_id=b.id and a.id=d.content_id and a.type_id in ("+StrUtils.arr2String(typeIds)+") and a.channel_id in ("+ add +")";
        } 
        //包含副栏目的内容
        else if(option == 2){
            sql = "select a.id,a.title,a.creator,a.txt,a.description,a.release_date,a.custom_time,a.number,a.guest,a.introduction,a.title_img,a.content_img,a.type_img,b.content_id,c.path,u.avatar from content a, content_channel b, channel c,content_count d,user u where a.creator=u.id and a.id=b.content_id and b.channel_id = c.id and a.id=d.content_id and a.type_id in ("+StrUtils.arr2String(typeIds)+") and b.channel_id in ("+StrUtils.arr2String(channelIds)+")";
        }else if(option == 3){//查询一个用户的图片集
            sql = "select c.*,ca.url,ca.priority from content c,content_att ca where c.id=ca.content_id and c.id="+contentId+"";
            List<Content> list = dao.find(sql);
    		return list;
        }else if(option == 4){//查询专家视点
            sql = "select a.*,b.id channel_id,b.expert expert,b.position position,b.introduction introduction,b.title_img title_img " +
            	"from content a,channel b where a.channel_id = b.id " +
            	"and a.type_id in ("+StrUtils.arr2String(typeIds)+") " +
            	"and b.parent_id ="+StrUtils.arr2String(channelIds);
        }
        else{
        	throw new RuntimeException("option value must be 0 or 1 or 2.");
        }
		if(recommend != null){
			sql = sql + " and a.is_recommend =" + recommend;
		}
		if (title!= null) {
			sql = sql + " and a.title like '%" + title + "%'";
		}
		if(contentId != 0){
			sql = sql +" and a.id !="+contentId+" ";
		}
		
		//查询相关文章列表
		if (excludeId != null) {
			List<ContentKeywords> keys = ContentKeywords.dao.find("select content_id from content_keywords c where c.keywords_id in " +
				    "(select keywords_id from content_keywords c where c.content_id = "+excludeId+") and c.content_id != "+excludeId+"");
            String ids = " and a.id in (";
            if(keys.size()>0){
				for(ContentKeywords key : keys){
					System.out.println(key.getInt("content_id")+"=================");
                    ids = ids + key.getInt("content_id") + ",";
				}
				ids=ids.substring(0,ids.length()-1);
            }
            else{
            	ids = ids + "0";
            }
			ids= ids + ")";
			sql = sql + ids;
		}
		
		sql = sql + " and a.status = 2";
		sql = appendOrder(sql,orderBy);
		
		if(count != null){
			sql = sql+" LIMIT "+count;
		}

		List<Content> list = dao.find(sql);
		
		return list;
	}
	
	public Page<Content> getPageByChannelIdsForTag(Integer[] channelIds,
			Integer[] typeIds, Boolean titleImg, Boolean recommend,
			String title, int orderBy, int option, Integer first,int pageNo,Integer excludeId,int count,Integer guestId, int contentId) {
		
		String sql = null;
		String sqlE = null;

        //只包含栏目本身的内容
        if(option==0){
        	sql = "select a.id,a.title,a.description,a.txt,a.release_date,a.custom_time,a.title_img,a.content_img,a.type_img,b.path";
		    sqlE = "from content a,channel b,content_count d where a.channel_id=b.id and a.id=d.content_id and a.type_id in ("+StrUtils.arr2String(typeIds)+") and a.channel_id in ("+StrUtils.arr2String(channelIds)+")";
        }
        //包含栏目所有子栏目的内容
        else if(option == 1){
    		String add = "select id from channel where 1=2";
            for(Integer channelId:channelIds){
            	Channel c = Channel.dao.findById(channelId);
            	add = add + " or (lft>="+c.getInt("lft")+" and rgt<="+c.getInt("rgt")+")";
            }
            sql = "select a.id,a.title,a.txt,a.description,a.release_date,a.custom_time,a.title_img,a.content_img,a.type_img,b.path";
            sqlE = "from content a,channel b,content_count d where a.channel_id=b.id and a.id=d.content_id and a.type_id in ("+StrUtils.arr2String(typeIds)+") and a.channel_id in ("+ add +")";
        } 
        //包含副栏目的内容
        else if(option == 2){
        	sql = "select a.id,a.title,a.number,a.txt,a.release_date,a.custom_time,a.title_img,a.description,a.content_img,a.guest,a.introduction,a.type_img,b.content_id,c.path ";
            sqlE = "from content a, content_channel b,channel c,content_count d where a.id=b.content_id and b.channel_id=c.id and a.id=d.content_id and a.type_id in ("+StrUtils.arr2String(typeIds)+") and b.channel_id in ("+StrUtils.arr2String(channelIds)+") ";
        }else if(option == 3){//查询专家视点
            sql = "select a.*,b.id channel_id,b.expert expert,b.position position,b.introduction introduction,b.title_img title_img " ;
            sqlE = "from content a,channel b where a.channel_id = b.id " +
        	"and a.type_id in ("+StrUtils.arr2String(typeIds)+") " +
        	"and b.parent_id ="+StrUtils.arr2String(channelIds);
        }
        else{
        	throw new RuntimeException("option value must be 0 or 1 or 2.");
        }
		if(recommend != null){
			sqlE = sqlE + " and a.is_recommend =" + recommend;
		}
		if (title!= null) {
			sqlE = sqlE + " and a.title like '%" + title + "%'";
		}
		if (guestId != null) {
			sqlE = sqlE + " and a.channel_id ="+guestId;
		}
		sqlE = sqlE + " and a.status = 2";
		sqlE = appendOrder(sqlE,orderBy);
		
		Page<Content> page = dao.paginate(pageNo, count, sql, sqlE);
		
		return page;
	}
	
	private String appendOrder(String sql, int orderBy) {
		switch (orderBy) {
		case 1:
			// ID升序
			sql = sql + " ORDER BY a.id asc";
			break;
		case 2:
			// 发布时间降序
			sql = sql + " ORDER BY a.release_date desc";
			break;
		case 3:
			// 发布时间升序
			sql = sql + " ORDER BY a.release_date asc";
			break;
		case 4:
			// 点击量降序
			sql = sql + " ORDER BY d.totalClick desc";
			break;
		case 5:
			// 点击量升序
			sql = sql + " ORDER BY d.totalClick asc";
			break;
		case 6:
			//会客厅类表倒序排序
			sql=sql+" order by convert(a.number,SIGNED) desc";
			break;
		case 7:
			// 更新时间降序
			sql = sql + " ORDER BY a.update_time desc";
			break;
		case 8:
			// 发布时间降序
			sql = sql + " ORDER BY a.custom_time desc";
			break;
		case 9:
			// 发布时间升序
			sql = sql + " ORDER BY a.custom_time asc";
			break;
		default:
			// 默认： ID降序
			sql = sql + " ORDER BY a.id desc";
		}
		return sql;
	}
}