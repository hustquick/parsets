package edu.uncc.parsets.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.transform.Transformers;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import genosetsdb.GenoSetsClassMap;
import genosetsdb.GenoSetsSessionManager;
import genosetsdb.TableDimension;
import genosetsdb.entity.Feature;
import genosetsdb.entity.Organism;

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
		//TODO: Iterate classMap and add all dimensions
		TableDimension tableDimension = new TableDimension(Feature.class, null, "f0", "featureId");
		dimHandles.add(new GenoSetsDimensionHandle("Type", "featureType1", "featureType", String.class, tableDimension, DataType.categorical, 0, this));
		tableDimension = new TableDimension(Organism.class, "organism", "o1", "organismId");
		dimHandles.add(new GenoSetsDimensionHandle("species", "species1", "species", String.class, tableDimension, DataType.categorical, 1, this));
		
		dimHandles.add(new GenoSetsDimensionHandle("genus", "genus1", "genus", String.class, tableDimension, DataType.categorical, 2, this));
	}

	@Override
	public CategoryTree getTree(List<DimensionHandle> dimensions){
		//Put all dimensions in a list with their parent table this avoids multiple joins
		//While iterating also create projection
		ProjectionList projList = Projections.projectionList();
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
			//Now add to select/group by statement
			projList.add(Projections.groupProperty(dimensionHandle.getParentTable().getAlias() + "." + dimensionHandle.getPropertyName()), dimensionHandle.getHandle());
		}
		projList.add(Projections.count(rootTableDimension.getAlias() + "." + rootTableDimension.getIdPropertyName()), "count");
		
		Criteria crit = recursiveCriteria(rootTableDimension, null, projList, handleParentMap);    
		crit.setResultTransformer(Transformers.TO_LIST);
		List<List> resultList = crit.list();
		for (Iterator iterator = resultList.iterator(); iterator.hasNext();) {
			List columnList = (List) iterator.next();
			for (Iterator iterator2 = columnList.iterator(); iterator2
					.hasNext();) {
				Object item = (Object) iterator2.next();
				System.out.print(item + "\t");
			}
			System.out.println();
		}
		
        CategoryTree tree = new CategoryTree(dimensions.size()+1);
        CategoryNode[] thisLine = new CategoryNode[dimensions.size()+1];
        CategoryNode[] previousLine = new CategoryNode[dimensions.size()+1];
		CategoryNode root = new CategoryNode(null, null, 0);
		tree.addtoLevel(0, root); 	
		
		for (Iterator iterator = resultList.iterator(); iterator.hasNext();) {
			List columnList = (List) iterator.next();
			int column = 0;
			CategoryNode previousNode = root;
			for (Iterator<DimensionHandle> dimIt = dimensions.iterator(); dimIt.hasNext();) {
				GenoSetsDimensionHandle dim = (GenoSetsDimensionHandle)dimIt.next();
				String name;
				if(dim.getPropertyClass().getSimpleName().equals("String"))
					name = (String)columnList.get(column);
				else if(dim.getPropertyClass().getSimpleName().equals("Integer"))
					name = ((Integer)columnList.get(column)).toString();
				else //TODO: implement for other property class types
					throw new NotImplementedException();
				CategoryHandle cat = dim.name2Handle(name);
				CategoryNode node = null;
				if (previousLine[column] != null && cat == previousLine[column].getToCategory()) {
					node = previousLine[column];
				} else {
					if (column+1 == dimensions.size())
						node = new CategoryNode(previousNode, cat, (Integer)columnList.get(column+1));
					else {
						node = new CategoryNode(previousNode, cat, 0);
						for (int i = column+1; i <= dimensions.size(); i++)
							previousLine[i] = null;
					}
					tree.addtoLevel(column+1, node);
				}
				previousNode = node;
				thisLine[column] = node;
				
				column++;
			}
			CategoryNode temp[] = thisLine;
			thisLine = previousLine;
			previousLine = temp;

		}
        
		//Print tree
		int numLevels = dimensions.size()+1;
		for (int i = 1; i < numLevels; i++) {
			List<CategoryNode> levelList = tree.getLevelList(i);
			for (Iterator it = levelList.iterator(); it.hasNext();) {
				CategoryNode categoryNode = (CategoryNode) it.next();
				System.out.print(categoryNode.getToCategory().getName() + categoryNode.getCount() + "\t");
			}
			System.out.println();
		}
   
		return tree;
	}
	
	private Criteria recursiveCriteria(TableDimension tableDim, Criteria criteria, ProjectionList projList, Map<TableDimension, List<GenoSetsDimensionHandle>> handleParentMap){
		if(criteria == null){
			StatelessSession session = GenoSetsSessionManager.getStatelessSession();
			criteria = session.createCriteria(tableDim.getEntityClass(), tableDim.getAlias());
		    criteria.setProjection(projList);
		}
		List<TableDimension> childList = classMap.get(tableDim);
		if(childList != null){
			for (Iterator it = childList.iterator(); it.hasNext();) {
				TableDimension childDim = (TableDimension) it.next();		
				List<GenoSetsDimensionHandle> selectedDims = handleParentMap.get(childDim);
				if(selectedDims != null){ //then add to criteria					
					System.out.println("Adding tableDim " + childDim.getPropertyName());
					criteria.createCriteria(childDim.getPropertyName(), childDim.getAlias());
				}
				recursiveCriteria(childDim, criteria, projList, handleParentMap);
			}
		}
		return criteria;
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
