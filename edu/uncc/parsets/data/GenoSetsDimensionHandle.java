package edu.uncc.parsets.data;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.Projections;
import org.hibernate.transform.Transformers;

import genosetsdb.GenoSetsSessionManager;
import genosetsdb.TableDimension;
import genosetsdb.entity.Feature;



public class GenoSetsDimensionHandle extends DimensionHandle{
	private TableDimension parentTable;
	private String propertyName;
	
	public GenoSetsDimensionHandle(String name, String handle, String propertyName, TableDimension parentTable, DataType dataType, int dimNum) {
		//TODO: change DimensionHandle constructor doesn't need LocalDBDataSet
		//the null value is for the LocalDBDataSet 
		super(name, handle, dataType, dimNum, null);
		this.propertyName = propertyName;
		this.parentTable = parentTable;
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
    	for (Map map : list) {
    		//TODO: typecasting to string might break if integer is returned
    		String categoryName = (String)map.get(handle);
			Integer count = (Integer)map.get("count");
			categories.add(new CategoryHandle(categoryName, categoryName, 0, this, count));		
		}
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


	@Override
	public Iterator<CategoryHandle> iterator() {
		if (categories == null)
			loadCategories();
		return categories.iterator();
	}

}
