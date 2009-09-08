package edu.uncc.parsets.data;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.StatelessSession;
import org.hibernate.transform.Transformers;

import genosetsdb.GenoSetsSessionManager;



public class GenoSetsDimensionHandle extends GenoSetsDimension implements Iterable<CategoryHandle>{
	private GenoSetsDimension parentSuperDimension;
	private List<CategoryHandle> categories;
	private String selectStmt;
	
	public GenoSetsDimensionHandle(String propertyName, String alias) {
		super(propertyName, alias);
	}
	public GenoSetsDimensionHandle(String propertyName, String alias, String selectStmt){
		this(propertyName, alias);
		this.selectStmt = selectStmt;
	}
	private void loadCategories(){
		categories = new ArrayList<CategoryHandle>();
		StatelessSession session = GenoSetsSessionManager.getStatelessSession();
		
		if(selectStmt == null)
			selectStmt = this.propertyName;
		
		String query = "Select " + selectStmt + " as " + alias + 
			" from " + parentSuperDimension.getPropertyName() + 
			" group by " + this.getPropertyName();
    	List<Map> list = session.createQuery(query).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
    	for (Map map : list) {
			Object value = map.get(this.alias);
			//categories.add(new CategoryHandle(value, this.propertyName + " " value, ));
		}
	}
	
	@Override
	public Iterator<CategoryHandle> iterator() {
		if (categories == null)
			loadCategories();
		return categories.iterator();
	}
	
	public static void main(String[] args){
		
	}
}
