package edu.uncc.parsets.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.StatelessSession;

import genosetsdb.GenoSetsClassMap;
import genosetsdb.GenoSetsSessionManager;
import genosetsdb.TableDimension;
import genosetsdb.entity.Feature;

/** 
 * Dataset that is backed by GenoSets Database
 * @author aacain
 *
 */
public class GenoSetsDataSet extends DataSet{
	
	protected ArrayList<DimensionHandle> dimHandles;
	protected Class factClass;
	protected TableDimension rootTableDimension;
	protected Map<TableDimension, List<TableDimension>> classMap;
	protected int numRecords;
	protected String section = "Genosets";
	
	
	public GenoSetsDataSet(String dbname, Class factClass){
		this.name = dbname;
		this.factClass = factClass;
		this.classMap = GenoSetsClassMap.getTableMap(factClass);
		this.rootTableDimension = GenoSetsClassMap.getRootTableDimension();
		loadDimensions();
	}
	
	protected void loadDimensions(){
		dimHandles = new ArrayList<DimensionHandle>();
		//Iterate classMap and add all dimensions
		TableDimension tableDimension = new TableDimension(Feature.class, null, "f");
		GenoSetsDimensionHandle dim = new GenoSetsDimensionHandle("Type", "featureType1", "featureType", tableDimension, DataType.categorical, 0);
		dimHandles.add(dim);
	}

	public CategoryTree getTree(List<DimensionHandle> dimensions){
		//Put all dimensions in a list with their parent table this avoids multiple joins
		Map<TableDimension, List<GenoSetsDimensionHandle>> handleParentMap = new HashMap<TableDimension, List<GenoSetsDimensionHandle>>();
		for (Iterator it = dimensions.iterator(); it.hasNext();) {
			GenoSetsDimensionHandle dimensionHandle = (GenoSetsDimensionHandle) it.next();
			List<GenoSetsDimensionHandle> list = handleParentMap
							.get(dimensionHandle.getParentTable());
			if(list == null){
				list = new ArrayList(10);
				list.add(dimensionHandle);
				handleParentMap.put(dimensionHandle.getParentTable(), list);
			}else
				list.add(dimensionHandle);			
		}
		
		//Iterate classMap, lookup tabledimension, and add dimensionhandles to query
		StatelessSession session = GenoSetsSessionManager.getStatelessSession();
		Criteria crit = session.createCriteria(rootTableDimension.getClass(), rootTableDimension.getAlias());
		recursiveCriteria(rootTableDimension, crit, handleParentMap);		
		
		return null;
	}
	
	private void recursiveCriteria(TableDimension tableDim, Criteria criteria, Map<TableDimension, List<GenoSetsDimensionHandle>> handleParentMap){
		List<TableDimension> childList = classMap.get(tableDim);
		if(childList != null){
			for (Iterator it = childList.iterator(); it.hasNext();) {
				TableDimension childDim = (TableDimension) it.next();		
				List<GenoSetsDimensionHandle> selectedDims = handleParentMap.get(childDim);
				if(selectedDims != null){ //then add to criteria
					System.out.println("Adding tableDim");
					criteria.createCriteria(tableDim.getAlias() + "." + childDim.getPropertyName(), childDim.getAlias());
				}
				recursiveCriteria(childDim, criteria, handleParentMap);
			}
		}
	}
	
	@Override
	public int getNumCategoricalDimensions() {
		if (dimHandles == null)
			loadDimensions();
		int num = 0;
		for (DimensionHandle d : dimHandles)
			if (d.getDataType() == DataType.categorical) 
				num++;
		return num;
	}


	@Override
	public int getNumDimensions() {
		if (dimHandles == null)
			loadDimensions();

		return dimHandles.size();
	}


	@Override
	public int getNumNumericDimensions() {
		return getNumDimensions()-getNumCategoricalDimensions();
	}


	@Override
	public int getNumRecords() {
		return numRecords;
	}


	@Override
	public DimensionHandle[] getNumericDimensions() {
		DimensionHandle handles[] = new DimensionHandle[getNumNumericDimensions()];
		int i = 0;
		for (DimensionHandle d : dimHandles)
			if (d.getDataType() != DataType.categorical)
				handles[i++] = d;
		
		return handles;	
	}


	@Override
	public String getSection() {
		return section;
	}

	@Override
	public String getURL() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Iterator<DimensionHandle> iterator() {
		if (dimHandles == null)
			loadDimensions();
		return dimHandles.iterator();
	}
	
	public static void main(String[] args) {
		System.out.println("okay");
		GenoSetsDataSet dset = new GenoSetsDataSet("dbName", Feature.class);
		dset.getTree(dset.dimHandles);
	}
	
}
