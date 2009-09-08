package edu.uncc.parsets.data;

public class GenoSetsDimension {
	protected String propertyName;
	protected String alias;
	
	public GenoSetsDimension(String propertyName, String alias){
		this.propertyName = propertyName;
		this.alias = alias;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	
}
