package com.cms.admin.friendlinkctg;


import java.util.List;
import com.jfinal.plugin.activerecord.Model;

@SuppressWarnings("serial")
public class Friendlinkctg extends Model<Friendlinkctg> {
	public static final Friendlinkctg dao = new Friendlinkctg();
	public List<Friendlinkctg> getListByFriendlinkForTag(Integer orderBy,Integer count) {
		String sql = "select a.friendlinkctg_id,b.id,b.name from friendlink a,friendlinkctg b where a.friendlinkctg_id = b.id and b.id=?";
        sql = appendOrder(sql,orderBy);
		if(count != null){
			sql = sql+" LIMIT "+count;
		}

		List<Friendlinkctg> list = dao.find(sql);

		return list;
	}
	private String appendOrder(String sql, int orderBy) {
		switch (orderBy) {
		case 1:
			// ID升序
			sql = sql + " ORDER BY friendlinkctg.id asc";
			break;
		default:
			// 默认： ID降序
			sql = sql + " ORDER BY friendlinkctg.id desc";
		}
		return sql;
	}
}