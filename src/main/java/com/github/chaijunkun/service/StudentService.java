package com.github.chaijunkun.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.github.chaijunkun.entity.Student;

/**
 * 学生持久化服务
 * @author chaijunkun
 * @since 2015年4月3日
 */
@Service
public class StudentService {
	
	private static Map<Integer, Student> vDB = new HashMap<Integer, Student>();
	
	private static int counter = 1;
	
	public Integer add(Student student){
		student.setId(counter);
		vDB.put(counter, student);
		counter++;
		return student.getId();
	}
	
	public boolean del(Integer id){
		Student student = vDB.remove(id);
		return student != null ? true : false;
	}
	
	public Student get(Integer id){
		return vDB.get(id);
	}
	
	public boolean update(Student student){
		Student dbObj = vDB.get(student.getId());
		if (dbObj==null){
			return false;
		}else{
			vDB.put(student.getId(), student);
			return true;
		}
	}

}
