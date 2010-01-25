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
import genosets.data.entity.AnnoView;
import genosets.data.entity.FeatureDesc;
import genosets.data.entity.GenBankType;
import genosets.data.entity.Homologs;
import genosets.data.entity.Organism;
import genosets.interaction.GenoSetsClassMap;
import genosets.interaction.GenoSetsSessionManager;
import genosets.interaction.TableDimension;

/** 
 * Dataset that is backed by GenoSets Database
 * @author aacain
 *
 */
public class GenoSetsDataSet extends DataSet{
	
	protected ArrayList<DimensionHandle> dimHandles;
	private Class factClass;
	protected TableDimension rootTableDimension;
	protected Map<TableDimension, List<TableDimension>> classMap;
	protected int numRecords;
	protected String section = "Genosets";
	private List<DimensionHandle> currentDims;
	private Criteria currentCriteria;
	
	
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
		//TableDimension tableDimension = new TableDimension(Feature.class, null, "f0", "featureId");
		//dimHandles.add(new GenoSetsDimensionHandle("Type", "featureType1", "featureType", String.class, tableDimension, DataType.categorical, 0, this));
		
		TableDimension tableDimension = new TableDimension(FeatureDesc.class, "featureDescSet", "fd1", "featureDescId");
		//dimHandles.add(new GenoSetsDimensionHandle("type", "featureType1", "featureType", String.class, tableDimension, DataType.categorical, 0, this));
		tableDimension = new TableDimension(Organism.class, "organism", "o1", "organismId");
		dimHandles.add(new GenoSetsDimensionHandle("species", "species1", "species", String.class, tableDimension, DataType.categorical, 1, this));
		dimHandles.add(new GenoSetsDimensionHandle("genus", "genus1", "genus", String.class, tableDimension, DataType.categorical, 2, this));
		dimHandles.add(new GenoSetsDimensionHandle("strain", "strain1", "strain", String.class, tableDimension, DataType.categorical, 5, this));
		dimHandles.add(new GenoSetsDimensionHandle("biovar", "biovar1", "biovar", String.class, tableDimension, DataType.categorical, 6, this));
		dimHandles.add(new GenoSetsDimensionHandle("repUnit", "repUnit1", "repUnit", String.class, tableDimension, DataType.categorical, 7, this));
		tableDimension = new TableDimension(AnnoView.class, "annoView", "av", "featureId");
		dimHandles.add(new GenoSetsDimensionHandle("GenBank/EMBL", "gb", "genbank", String.class, tableDimension, DataType.categorical, 3, this));
		dimHandles.add(new GenoSetsDimensionHandle("Patric", "p", "patric", String.class, tableDimension, DataType.categorical, 4, this));
		
		tableDimension = new TableDimension(Homologs.class, "homologs", "h", "featureId");
		dimHandles.add(new GenoSetsDimensionHandle("homolog species Melitensis", "speciesMelitensis", "speciesMelitensis", String.class, tableDimension, DataType.categorical, 8, this));
//		dimHandles.add(new GenoSetsDimensionHandle("homolog species Suis", "speciesSuis", "speciesSuis", String.class, tableDimension, DataType.categorical, 9, this));
//		dimHandles.add(new GenoSetsDimensionHandle("homolog species Abortus", "speciesAbortus", "speciesAbortus", String.class, tableDimension, DataType.categorical, 10, this));
//		dimHandles.add(new GenoSetsDimensionHandle("homolog species Ovis", "speciesOvis", "speciesOvis", String.class, tableDimension, DataType.categorical, 11, this));
//		dimHandles.add(new GenoSetsDimensionHandle("homolog species Canis", "speciesCanis", "speciesCanis", String.class, tableDimension, DataType.categorical, 12, this));
//		dimHandles.add(new GenoSetsDimensionHandle("homolog species Microti", "speciesMicroti", "speciesMicroti", String.class, tableDimension, DataType.categorical, 13, this));
//		dimHandles.add(new GenoSetsDimensionHandle("homolog strain Abortus9941", "strainAbortus9941", "strainAbortus9941", String.class, tableDimension, DataType.categorical, 14, this));
//		dimHandles.add(new GenoSetsDimensionHandle("homolog strain MelitensisBiovarAbortus", "strainMelitensisBiovarAbortus", "strainMelitensisBiovarAbortus", String.class, tableDimension, DataType.categorical, 15, this));
//		dimHandles.add(new GenoSetsDimensionHandle("homolog strain AbortusS19", "strainAbortusS19", "strainAbortusS19", String.class, tableDimension, DataType.categorical, 16, this));
//		dimHandles.add(new GenoSetsDimensionHandle("homolog strain CanisAtcc", "strainCanisAtcc", "strainCanisAtcc", String.class, tableDimension, DataType.categorical, 17, this));
//		dimHandles.add(new GenoSetsDimensionHandle("homolog strain Melitensis16m", "strainMelitensis16m", "strainMelitensis16m", String.class, tableDimension, DataType.categorical, 18, this));
//		dimHandles.add(new GenoSetsDimensionHandle("homolog strain MelitensisAtcc", "strainMelitensisAtcc", "strainMelitensisAtcc", String.class, tableDimension, DataType.categorical, 19, this));
//		dimHandles.add(new GenoSetsDimensionHandle("homolog strain Microti", "strainMicroti", "strainMicroti", String.class, tableDimension, DataType.categorical, 20, this));
//		dimHandles.add(new GenoSetsDimensionHandle("homolog strain OvisAtcc", "strainOvisAtcc", "strainOvisAtcc", String.class, tableDimension, DataType.categorical, 21, this));
//		dimHandles.add(new GenoSetsDimensionHandle("homolog strain Suis1330", "strainSuis1330", "strainSuis1330", String.class, tableDimension, DataType.categorical, 22, this));
//		dimHandles.add(new GenoSetsDimensionHandle("homolog strain SuisAtcc", "strainSuisAtcc", "strainSuisAtcc", String.class, tableDimension, DataType.categorical, 23, this));
	
		tableDimension = new TableDimension(GenBankType.class, "genBankType", "gt", "featureId", 2);
		dimHandles.add(new GenoSetsDimensionHandle("GenBankType", "gbType", "featureType", String.class, tableDimension, DataType.categorical, 24, this));
	
	}
	
	public Criteria createCriteria(){		
		//While iterating also create projection
		ProjectionList projList = Projections.projectionList();
		
		//Put all dimensions in a list with their parent table this avoids multiple joins
		//by creating a map for all Table dimensions with their associated list of all their 
		//genosetsDimensionHandles that are part of that selection
		Map<TableDimension, List<GenoSetsDimensionHandle>> handleParentMap = new HashMap<TableDimension, List<GenoSetsDimensionHandle>>();
		for (Iterator it = currentDims.iterator(); it.hasNext();) {
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
		
		Criteria crit = recursiveCriteria(rootTableDimension, null, handleParentMap);		
		crit.setProjection(projList);
		crit.setResultTransformer(Transformers.TO_LIST);
		return crit;
	}

	@Override
	public CategoryTree getTree(List<DimensionHandle> dimensions){
		currentDims = dimensions;
		Criteria crit = createCriteria();
		currentCriteria = crit;
		List<List> resultList = crit.list();
		
		
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
   
		return tree;
	}
	
	private Criteria recursiveCriteria(TableDimension tableDim, Criteria criteria, Map<TableDimension, List<GenoSetsDimensionHandle>> handleParentMap){
		//Create new criteria if hasn't been created, based on root table dimension
		if(criteria == null){
			StatelessSession session = GenoSetsSessionManager.getStatelessSession();
			criteria = session.createCriteria(rootTableDimension.getEntityClass(), tableDim.getAlias());
		}
		
		//get all tableDimension children of given table dimension, iterate list and see if in the selected list
		List<TableDimension> childList = classMap.get(tableDim);
		if(childList != null){
			for (Iterator it = childList.iterator(); it.hasNext();) {
				TableDimension childDim = (TableDimension) it.next();		
				List<GenoSetsDimensionHandle> selectedDims = handleParentMap.get(childDim);
				if(selectedDims != null){ //then add to criteria					
					System.out.println("Adding tableDim " + childDim.getPropertyName());
					if(childDim.getJoinType() != 0)
						criteria.createCriteria(childDim.getPropertyName(), childDim.getAlias());
					else{
						criteria.createCriteria(childDim.getPropertyName(), childDim.getAlias(), 1);
						System.out.println("Criteria ");
					}
				}
				recursiveCriteria(childDim, criteria, handleParentMap);
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

	public void setFactClass(Class factClass) {
		this.factClass = factClass;
	}

	public Class getFactClass() {
		return factClass;
	}

	public void setCurrentCriteria(Criteria currentCriteria) {
		this.currentCriteria = currentCriteria;
	}

	public Criteria getCurrentCriteria() {
		return currentCriteria;
	}
	
}
