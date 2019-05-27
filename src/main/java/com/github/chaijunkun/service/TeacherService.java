package com.github.chaijunkun.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.github.chaijunkun.entity.Teacher;

/**
 * 教师持久化服务
 * @author chaijunkun
 * @since 2015年4月3日
 */
@Service
public class TeacherService {
	
	private static Map<Integer, Teacher> vDB = new HashMap<Integer, Teacher>();
	
	private static int counter = 1;
	
	public Integer add(Teacher teacher){
		teacher.setId(counter);
		vDB.put(counter, teacher);
		counter++;
		return teacher.getId();
	}
	
	public boolean del(Integer id){
		Teacher teacher = vDB.remove(id);
		return teacher != null ? true : false;
	}
	
	public Teacher get(Integer id){
		return vDB.get(id);
	}
	
	public boolean update(Teacher teacher){
		Teacher dbObj = vDB.get(teacher.getId());
		if (dbObj==null){
			return false;
		}else{
			vDB.put(teacher.getId(), teacher);
			return true;
		}
	}

}
