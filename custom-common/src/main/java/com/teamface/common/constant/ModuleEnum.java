package com.teamface.common.constant;

public enum ModuleEnum {

	basic(0,"user"),	
	approval(1,"approval"),	
	attendance(2,"attendance"),
	blog(3,"blog"),
	common(4,"common"),
	company(5,"company"),
	complain(6,"complain"),
	crm(7,"crm"),
	daily(8,"daily"),
	forum(9,"forum"),
	notes(10,"notes"),
	notice(11,"notice"),
	permission(12,"permission"),
	project(13,"project"),
	records(14,"records"),
	report(15,"report"),
	schedule(16,"report"),
	task(17,"daily");
	private int dex;
	private String des;

	public int getDex() {
		return dex;
	}
	public void setDex(int dex) {
		this.dex = dex;
	}
	public String getDes() {
		return des;
	}
	public void setDes(String des) {
		this.des = des;
	}

	private ModuleEnum(int dex, String des) {
		this.dex = dex;
		this.des = des;
	}
	public static String getDes(Integer index){
		if(index==null)
			return  ModuleEnum.basic.getDes();
		for(ModuleEnum ae:ModuleEnum.values()){
			if(index==ae.getDex()){
				return ae.getDes();
			}
		}
		return ModuleEnum.basic.getDes();
	}
	
}
