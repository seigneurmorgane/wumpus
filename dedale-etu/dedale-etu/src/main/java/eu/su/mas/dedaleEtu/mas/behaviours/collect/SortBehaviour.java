package eu.su.mas.dedaleEtu.mas.behaviours.collect;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;

/**
 * This behaviour allows an agent to explore the environment and learn the
 * associated topological map. The algorithm is a pseudo - DFS computationally
 * consuming because its not optimised at all.</br>
 * 
 * When all the nodes around him are visited, the agent randomly select an open
 * node and go there to restart its dfs.</br>
 * This (non optimal) behaviour is done until all nodes are explored. </br>
 * 
 * Warning, this behaviour does not save the content of visited nodes, only the
 * topology.</br>
 * Warning, this behaviour is a solo exploration and does not take into account
 * the presence of other agents (or well) and indefinitely tries to reach its
 * target node
 * 
 * @author hc
 *
 */
public class SortBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private MapRepresentation myMap;

	private List<String> openNodes = new ArrayList<String>();

	private List<Couple<String, List<Couple<Observation, Integer>>>> closedNodes;
	private List<Couple<String, String>> edges;

	private List<Couple<String, List<Couple<Observation, Integer>>>> otherClosedNodes;
	private List<Couple<String, String>> otherEdges;

	public SortBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> openNodes,
			List<Couple<String, List<Couple<Observation, Integer>>>> closedNodes,
			List<Couple<String, List<Couple<Observation, Integer>>>> otherClosedNodes,
			List<Couple<String, String>> edge, List<Couple<String, String>> otherEdge) {
		super(myagent);

		this.myMap = myMap;

		this.openNodes = openNodes;

		this.closedNodes = closedNodes;
		this.otherClosedNodes = otherClosedNodes;

		this.edges = edge;
		this.otherEdges = otherEdge;
	}

	@Override
	public void action() {
		Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter = this.otherClosedNodes.iterator();
		while (iter.hasNext()) {
			Couple<String, List<Couple<Observation, Integer>>> node = iter.next();
			if (!closedNodesContains(node.getLeft())) {
				this.closedNodes.add(node);
				if (this.openNodes.contains(node.getLeft())) {
					this.openNodes.remove(node.getLeft());
				}
				this.myMap.addNode(node.getLeft(), MapAttribute.closed);
			}
			iter.remove();
		}
		this.otherClosedNodes.clear();

		Iterator<Couple<String, String>> it = this.otherEdges.iterator();
		while (it.hasNext()) {
			Couple<String, String> edge = it.next();
			if (!this.edges.contains(edge)
					&& ((closedNodesContains(edge.getLeft()) && closedNodesContains(edge.getRight()))
							|| (this.openNodes.contains(edge.getLeft()) && closedNodesContains(edge.getRight()))
							|| (this.openNodes.contains(edge.getRight()) && closedNodesContains(edge.getLeft()))
							|| (this.openNodes.contains(edge.getLeft())) && this.openNodes.contains(edge.getRight()))) {
				this.edges.add(edge);
				this.myMap.addEdge(edge.getLeft(), edge.getRight());
			}
			it.remove();
		}
		this.otherEdges.clear();
	}

	@Override
	public int onEnd() {
		return 1;
	}

	public boolean closedNodesContains(String nodeId) {
		Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter = this.closedNodes.iterator();
		while (iter.hasNext()) {
			Couple<String, List<Couple<Observation, Integer>>> node = iter.next();
			if (nodeId.equals(node.getLeft())) {
				return true;
			}
		}
		return false;
	}
}
