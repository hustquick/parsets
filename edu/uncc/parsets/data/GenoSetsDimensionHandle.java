package edu.uncc.parsets.data;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.StatelessSession;
import org.hibernate.transform.Transformers;

import genosetsdb.GenoSetsSessionManager;



public class GenoSetsDimensionHandle extends DimensionHandle{
	private Class parentSuperDimension;
	private String alias;
	private String selectStmt;
	
	public GenoSetsDimensionHandle(String name, String handle, DataType dataType, int dimNum) {
		super(name, handle, dataType, dimNum, null);
	}
	
	
	private void loadCategories(){
		categories = new ArrayList<CategoryHandle>();
		StatelessSession session = GenoSetsSessionManager.getStatelessSession();
		
		if(selectStmt == null)
			selectStmt = this.handle;
		
		String query = "Select " + selectStmt + " as " + alias + 
			" from " + parentSuperDimension.getPropertyName() + 
			" group by " + this.handle;
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
