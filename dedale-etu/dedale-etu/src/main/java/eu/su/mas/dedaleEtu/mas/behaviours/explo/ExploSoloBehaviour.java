package eu.su.mas.dedaleEtu.mas.behaviours.explo;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
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

	private int trans = 1;
	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;

	private List<Couple<String,List<Couple<Observation,Integer>>>> closedNodes;
	private List<Couple<String,List<Couple<Observation,Integer>>>> otherClosedNodes;
	private List<String> openNodes;
	private List<Couple<String,String>> Edges;
	private List<Couple<String,String>> otherEdges;
	private List<String> path;

	public ExploSoloBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap,List<Couple<String,List<Couple<Observation,Integer>>>> closedNodes,List<Couple<String,List<Couple<Observation,Integer>>>> otherClosedNodes, List<Couple<String,String>> edge,List<Couple<String,String>> otherEdge, List<String> openNodes,List<String> path) {
		super(myagent);
		this.myMap=myMap;
		this.openNodes=openNodes;
		this.closedNodes=closedNodes;
		this.Edges = edge;
		this.path = path;
		this.otherClosedNodes = otherClosedNodes;
		this.otherEdges = otherEdge;
	}

	@Override
	public void action() {
		System.out.println("EXPLOSOLO de "+this.myAgent.getLocalName());
		//création de la map
		if(this.myMap==null)
			this.myMap= new MapRepresentation();

		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		// ajoute les noeuds fermés récupérés à partir d'une communication
		if(this.otherClosedNodes.size() > 0) {
			for(Couple<String,List<Couple<Observation,Integer>>> node : this.otherClosedNodes) {
				if(!this.closedNodesContains(node.getLeft())) {
					this.closedNodes.add(node);
					if(this.openNodes.contains(node.getLeft())) {
						this.openNodes.remove(node.getLeft());
					}
					this.myMap.addNode(node.getLeft(),MapAttribute.closed);
				}
				this.otherClosedNodes.remove(node);
			}
		}


		// ajoute les arêtes des noeuds récupérés à partir d'une communication
		if(this.otherEdges.size() > 0) {
			for(Couple<String,String> edge : this.otherEdges) {
				if(!this.Edges.contains(edge)) {
					this.Edges.add(edge);
					this.myMap.addEdge(edge.getRight(), edge.getLeft());
					this.myMap.addEdge(edge.getLeft(), edge.getRight());
				}
				this.otherEdges.remove(edge);
			}
		}


		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			String nextNode=null;

			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// maj du path si nécessaire
			if(this.path.size()> 0 && this.path.get(0).equals(myPosition))
				this.path.remove(myPosition);
			if(this.path.size()> 0) {
				System.out.println("choix path cas 1 (déjà def) : "+this.myAgent.getLocalName());
				nextNode = this.path.get(0);
			}


			//1) remove the current node from openlist and add it to closedNodes.
			if(!this.closedNodesContains(myPosition))
				this.closedNodes.add(new Couple<>(myPosition,lobs.get(0).getRight()));
			this.openNodes.remove(myPosition);

			this.myMap.addNode(myPosition,MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.

			Iterator<Couple<String,List<Couple<Observation,Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				Couple<String, List<Couple<Observation, Integer>>> nodeId=iter.next();
				if (!this.closedNodesContains(nodeId.getLeft())){
					if (!this.openNodes.contains(nodeId.getLeft())){
						this.openNodes.add(nodeId.getLeft());
						this.myMap.addNode(nodeId.getLeft(), MapAttribute.open);
						this.myMap.addEdge(myPosition, nodeId.getLeft());	
					}else{
						//the node exist, but not necessarily the edge
						this.myMap.addEdge(myPosition, nodeId.getLeft());
						this.Edges.add(new Couple<>(myPosition, nodeId.getLeft()));
					}
					if (nextNode==null) {
						nextNode=nodeId.getLeft();
						this.path.clear();
						this.path.add(nodeId.getLeft());
						System.out.println("choix path cas 2 (parmi obs) : "+this.myAgent.getLocalName());
					}
				}
			}

			System.out.println(this.myAgent.getLocalName() + " : ");
			System.out.println("Open Node");
			for (String node : this.openNodes) {
				System.out.print(node + " ");
			}
			System.out.println();
			System.out.println("Close Node");
			for (Couple<String,List<Couple<Observation,Integer>>> node : this.closedNodes) {
				System.out.print(node + " ");
			}
			System.out.println();

			System.out.println("Path");
			for (String p : this.path)
				System.out.println(p+" ");
			System.out.println();



			//3) si plus de noeuds ouverts, l'agent se déplace aléatoirement
			if (this.openNodes.isEmpty()){
				System.out.println("Exploration de "+this.myAgent.getLocalName()+" terminée");
				if(nextNode == null) {
					Random r= new Random();
					int moveId=1+r.nextInt(lobs.size()-1);
					nextNode = lobs.get(moveId).getLeft();
					this.path.clear();
					this.path.add(nextNode);
				}
			}else{
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (nextNode==null){
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
					this.path = myMap.getShortestPath(((AbstractDedaleAgent)this.myAgent).getCurrentPosition(), this.openNodes.get(0));
					System.out.println(this.myAgent.getLocalName()+" doesn't know where to go ...");
					nextNode=this.path.get(0);
					System.out.println("choix path cas 3 (avec getshortestpath) : "+this.myAgent.getLocalName());
					//System.out.println("--> -->"+ nextNode);
				}
			}

			System.out.println(this.myAgent.getLocalName()+" : from "+myPosition+" to "+this.path);

			try {
				if( ! ((AbstractDedaleAgent)this.myAgent).moveTo(nextNode)) {
					finished=true;
					this.trans=2;
					System.out.println("Exploration STOP");
				}
			} catch(RuntimeException e) {
				System.out.println("FAIL MODE : " +this.myAgent.getLocalName());
				finished = false;
				this.path.clear();
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

public boolean closedNodesContains(String nodeId) {
	Iterator<Couple<String,List<Couple<Observation,Integer>>>> iter=this.closedNodes.iterator();
	while(iter.hasNext()){
		Couple<String, List<Couple<Observation, Integer>>> node=iter.next();
		if(nodeId.equals(node.getLeft())) {
			return true;
		}
	}
	return false;

}

public static Couple<List<Couple<String,String>>,List<Couple<String,String>>> cleanEdges(List<Couple<String,String>> Edges, List<Couple<String,String>> otherEdges) {
	Iterator<Couple<String,String>> it = otherEdges.iterator();
	while(it.hasNext()) {
		Couple<String,String> edge = it.next();
		if(!Edges.contains(edge)) {
			Edges.add(edge);
		}
		otherEdges.remove(edge);
	}
	return new Couple<>(Edges,otherEdges);
}

public List<Couple<String,List<Couple<Observation,Integer>>>> cleanClosedNodes(List<Couple<String,List<Couple<Observation,Integer>>>> closedNodes, List<Couple<String,List<Couple<Observation,Integer>>>> otherClosedNodes) {
	Iterator<Couple<String,List<Couple<Observation,Integer>>>> iter = this.closedNodes.iterator();
	while(iter.hasNext()) {
		Couple<String,List<Couple<Observation,Integer>>> node = iter.next();
		if(!closedNodesContains(node.getLeft())) {
			closedNodes.add(node);
			if(this.openNodes.contains(node.getLeft())) {
				this.openNodes.remove(node.getLeft());	
			}
			this.myMap.addNode(node.getLeft(),MapAttribute.closed);
		}
	}
	return closedNodes;
}

}
