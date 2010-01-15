package edu.uncc.parsets.data;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.Projections;
import org.hibernate.transform.Transformers;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import genosetsdb.GenoSetsSessionManager;
import genosetsdb.TableDimension;

public class GenoSetsDimensionHandle extends DimensionHandle{
	private TableDimension parentTable;
	private String propertyName;
	private Class propertyClass;
	
	
	public GenoSetsDimensionHandle(String name, String handle, String propertyName, 
			Class propertyClass, TableDimension parentTable, DataType dataType, int dimNum, GenoSetsDataSet dataSet) {
		//TODO: pass GenoSetsDataSet to super constructor here! 
		super(name, handle, dataType, dimNum, null);
		this.propertyName = propertyName;
		this.parentTable = parentTable;
		this.propertyClass = propertyClass;
		this.dataSet = dataSet;
	}
	
	
	protected void loadCategories(){
		categories = new ArrayList<CategoryHandle>();
		
		StatelessSession session = GenoSetsSessionManager.getStatelessSession();
		Criteria crit = session.createCriteria(parentTable.getEntityClass(),
				parentTable.getAlias());
		crit.setProjection(Projections.projectionList()
				.add(Projections.count(propertyName), "count")
				.add(Projections.groupProperty(propertyName), handle));
	
    	crit.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
    	List<Map> list = crit.list();
    	int catNum = 1;
    	for (Map map : list) {
    		//TODO: typecasting to string might break if integer is returned
    		String categoryName;
    		if(propertyClass.getSimpleName().equals("String"))
    			categoryName = (String)map.get(handle);
    		else if(propertyClass.getSimpleName().equals("Integer"))
    			categoryName = ((Integer)map.get(handle)).toString();
    		else //TODO: implement other property class types
    			throw new NotImplementedException();
			Integer count = (Integer)map.get("count");
			categories.add(new CategoryHandle(categoryName, categoryName, catNum, this, count));		
			catNum++;
		}
	}
	
	/**
	 * Finds the category corresponding to a given name in this dimension. If not found, returns null.
	 * 
	 * @param name The name of the category, from the database
	 * @return The category handle or null
	 */
	protected CategoryHandle name2Handle(String name) {
		if (categories == null)
			loadCategories();
		for (CategoryHandle h : categories)
			if (h.getName().equals(name))
				return h;
		return null;
	}
	
	
	
	public TableDimension getParentTable() {
		return parentTable;
	}


	public void setParentTable(TableDimension parentTable) {
		this.parentTable = parentTable;
	}


	public String getPropertyName() {
		return propertyName;
	}


	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}


	public Class getPropertyClass() {
		return propertyClass;
	}


	public void setPropertyClass(Class propertyClass) {
		this.propertyClass = propertyClass;
	}


	@Override
	public Iterator<CategoryHandle> iterator() {
		if (categories == null)
			loadCategories();
		return categories.iterator();
	}
	
	@Override
	public DataSet getDataSet() {
		return dataSet;
	}

}
