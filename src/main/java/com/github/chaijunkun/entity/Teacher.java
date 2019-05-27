package com.github.chaijunkun.entity;

import javax.validation.constraints.NotNull;

import com.github.chaijunkun.validation.group.TeacherGroup;

import org.hibernate.validator.constraints.NotBlank;


/**
 * 教师对象
 * @author chaijunkun
 * @since 2015年4月3日
 */
public class Teacher {
	
	@NotNull(groups = {TeacherGroup.Get.class, TeacherGroup.Del.class, TeacherGroup.Update.class})
	private Integer id;
	
	@NotBlank(groups = {TeacherGroup.Add.class, TeacherGroup.Update.class})
	private String name;
	
	@NotNull(groups = {TeacherGroup.Add.class, TeacherGroup.Update.class})
	private Boolean male;
	
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

}
