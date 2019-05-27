package com.github.chaijunkun.entity;

import javax.validation.constraints.NotNull;

import com.github.chaijunkun.validation.group.StudentGroup;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 学生对象
 * @author chaijunkun
 * @since 2015年4月3日
 */
public class Student {
	
	@NotNull(groups = {StudentGroup.Get.class, StudentGroup.Del.class, StudentGroup.Update.class})
	private Integer id;
	
	@NotBlank(groups = {StudentGroup.Add.class, StudentGroup.Update.class})
	private String name;
	
	@NotNull(groups = {StudentGroup.Add.class, StudentGroup.Update.class})
	private Boolean male;
	
	private Integer teacherId;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getMale() {
		return male;
	}

	public void setMale(Boolean male) {
		this.male = male;
	}

	public Integer getTeacherId() {
		return teacherId;
	}

	public void setTeacherId(Integer teacherId) {
		this.teacherId = teacherId;
	}
	
}
