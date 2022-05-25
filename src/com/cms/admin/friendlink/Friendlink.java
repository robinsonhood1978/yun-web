package com.cms.admin.friendlink;


import java.util.List;
import com.jfinal.plugin.activerecord.Model;

@SuppressWarnings("serial")
public class Friendlink extends Model<Friendlink> {
	public static final Friendlink dao = new Friendlink();
	public List<Friendlink> getListByFriendlinkForTag(Integer orderBy,Integer count) {
		String sql = "select a.url,a.name,a.id ,a.friendlinkctg_id,b.id from friendlink a,friendlinkctg b where a.friendlinkctg_id = b.id and a.is_enabled = 1";
        sql = appendOrder(sql,orderBy);
		if(count != null){
			sql = sql+" LIMIT "+count;
		}

		List<Friendlink> list = dao.find(sql);
		return list;
	}
	private String appendOrder(String sql, int orderBy) {
		switch (orderBy) {
		case 1:
			// ID升序
			sql = sql + " ORDER BY a.id asc";
			break;
		default:
			// 默认： ID降序
			sql = sql + " ORDER BY a.id desc";
		}
		return sql;
	}
}