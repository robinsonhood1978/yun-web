package com.cms.admin.channel;

import java.util.List;

import com.cms.admin.content.ContentKeywords;
import com.cms.front.entity.Content;
import com.cms.util.StrUtils;
import com.jfinal.plugin.activerecord.Model;

@SuppressWarnings("serial")
public class Channel extends Model<Channel> {
	public static final Channel dao = new Channel();
	public List<Channel> getList(Integer channelId,Integer count) {
		
		String sql = null;
        sql = "select id,name,path,title_img,description from channel where parent_id ="+channelId;
		if(count != null){
			sql = sql+" LIMIT "+count;
		}

		List<Channel> list = dao.find(sql);
		
		return list;
	}
}