package eu.su.mas.dedaleEtu.mas.behaviours;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.SimpleBehaviour;


/**
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.</br>
 * 
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs.</br> 
 * This (non optimal) behaviour is done until all nodes are explored. </br> 
 * 
 * Warning, this behaviour does not save the content of visited nodes, only the topology.</br> 
 * Warning, this behaviour is a solo exploration and does not take into account the presence of other agents (or well) and indefinitely tries to reach its target node
 * @author hc
 *
 */
public class ExploSoloBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;

	private int trans = 0;
	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;

	/**
	 * Nodes known but not yet visited
	 */
	private List<String> openNodes;
	/**
	 * Visited nodes
	 */
	private Set<String> closedNodes;
	/**
	 * edge inter node
	 */
	private List<Couple<String,String>> edge;

	private Set<String> otherClosedNodes;

	private List<Couple<String,String>> otherEdge;
	
	public ExploSoloBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap,Set<String> closedNodes,Set<String> otherClosedNodes, List<Couple<String,String>> edge,List<Couple<String,String>> otherEdge) {
		super(myagent);
		this.myMap=myMap;
		this.openNodes=new ArrayList<String>();
		this.closedNodes=closedNodes;
		this.edge = edge;
		
		this.otherClosedNodes = otherClosedNodes;
		this.otherEdge = otherEdge;
	}

	@Override
	public void action() {
		
		if(this.myMap==null)
			this.myMap= new MapRepresentation();
		
		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
	
		if(!this.otherClosedNodes.isEmpty()) {
			Iterator<String> it = this.otherClosedNodes.iterator();
			while(it.hasNext()) {
				String node = it.next();
				if(!this.closedNodes.contains(node)) {
					this.closedNodes.add(node);
					if(this.openNodes.contains(node)) {
						this.openNodes.remove(node);	
					}
					this.myMap.addNode(node,MapAttribute.closed);
				}
				it.remove();
			}
		}
		
		if(!this.otherEdge.isEmpty()) {
			Iterator<Couple<String,String>> it = this.otherEdge.iterator();
			while(it.hasNext()) {
				Couple<String,String> ed = it.next();
				if((!this.edge.contains(ed)) && this.closedNodes.contains(ed.getLeft()) && this.closedNodes.contains(ed.getRight())) {
					this.edge.add(ed);
					this.myMap.addEdge(ed.getLeft(),ed.getRight());
				}
				it.remove();
			}
		}
		
		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition

			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(500);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//1) remove the current node from openlist and add it to closedNodes.
			this.closedNodes.add(myPosition);
			this.openNodes.remove(myPosition);

			this.myMap.addNode(myPosition,MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNode=null;
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				String nodeId=iter.next().getLeft();
				if (!this.closedNodes.contains(nodeId)){
					if (!this.openNodes.contains(nodeId)){
						this.openNodes.add(nodeId);
						this.myMap.addNode(nodeId, MapAttribute.open);
						this.myMap.addEdge(myPosition, nodeId);	
					}else{
						//the node exist, but not necessarily the edge
						this.myMap.addEdge(myPosition, nodeId);
						this.edge.add(new Couple<>(myPosition, nodeId));
					}
					if (nextNode==null) nextNode=nodeId;
				}
			}
			
			System.out.println(this.myAgent.getLocalName() + " : ");
			System.out.println("Open Node");
			for (String node : this.openNodes) {
				System.out.print(node + " ");
			}
			System.out.println();
			System.out.println("Close Node");
			for (String node : this.closedNodes) {
				System.out.print(node + " ");
			}
			System.out.println();
			
			
			//3) while openNodes is not empty, continues.
			if (this.openNodes.isEmpty()){
				//Explo finished
				this.trans = 8;
				finished=true;
				System.out.println("Exploration successufully done, behaviour removed.");
			}else{
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (nextNode==null){
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
					nextNode=this.myMap.getShortestPath(myPosition, this.openNodes.get(0)).get(0);
				}
				if( ! ((AbstractDedaleAgent)this.myAgent).moveTo(nextNode)) {
					finished=true;
					trans=2;
					System.out.println("Exploration STOP");
				}
			}

		}
	}

	@Override
	public boolean done() {
		return finished;
	}
	
	@Override
	public int onEnd() {
		return this.trans;
	}
}
