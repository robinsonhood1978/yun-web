package com.cms.admin.content;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

@SuppressWarnings("serial")
public class ContentAtt extends Model<ContentAtt> {
	public static final ContentAtt dao = new ContentAtt();
	
	public List<ContentAtt> getContentAttList(Integer id, Integer count){
		
		String sql = "select a.* from content_att a where a.content_id=" + id;
		if(count != null){
			sql = sql+" LIMIT "+count;
		}
		List<ContentAtt> list = dao.find(sql);
		return list;
	}
}

