package eu.su.mas.dedaleEtu.mas.behaviours.collect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.SimpleBehaviour;

/**
 * This behaviour is triggerable every 600ms. It tries all the API methods at
 * each time step, store the treasure that match the entity treasureType in its
 * backpack and intends to empty its backPack in the Tanker agent (if he is in
 * reach) <br/>
 *
 * Rmq : This behaviour is in the same class as the DummyCollectorAgent for
 * clarity reasons. You should prefer to save your behaviours in the behaviours
 * package, and all the behaviours referring to a given protocol in the same
 * class
 * 
 * @author hc
 */
public class WalkBehaviour extends SimpleBehaviour {
	/**
	 * When an agent choose to move
	 * 
	 */
	private static final long serialVersionUID = 9088209402507795289L;
	private int trans = 1;
	private boolean finished = false;

	private MapRepresentation myMap;
	private List<Couple<String, List<Couple<Observation, Integer>>>> closedNodes = new ArrayList<Couple<String, List<Couple<Observation, Integer>>>>();
	private List<String> openNodes = new ArrayList<String>();
	private List<Couple<String, String>> edges = new ArrayList<Couple<String, String>>();

	private List<Couple<String, List<Couple<Observation, Integer>>>> otherClosedNodes = new ArrayList<Couple<String, List<Couple<Observation, Integer>>>>();
	private List<Couple<String, String>> otherEdges = new ArrayList<Couple<String, String>>();

	private List<String> path = new ArrayList<String>();

	public WalkBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap,
			List<Couple<String, List<Couple<Observation, Integer>>>> closedNodes, List<String> openNodes,
			List<Couple<String, String>> edges,
			List<Couple<String, List<Couple<Observation, Integer>>>> otherClosedNodes,
			List<Couple<String, String>> otherEdges, List<String> path) {
		super(myagent);
		this.myMap = myMap;
		this.closedNodes = closedNodes;
		this.openNodes = openNodes;
		this.edges = edges;
		this.otherClosedNodes = otherClosedNodes;
		this.otherEdges = otherEdges;
		this.path = path;
	}

	@Override
	public void action() {

		// création de la map
		if (this.myMap == null) {
			this.myMap = new MapRepresentation();
		}
		// Example to retrieve the current position
		String myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
		String nextNode = null;

		if (myPosition != "") {
			List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent)
					.observe();// myPosition
			// System.out.println(this.myAgent.getLocalName() + " -- list of observables: "
			// + lobs);

			// Little pause to allow you to follow what is going on
			try {
				this.myAgent.doWait(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// list of observations associated to the currentPosition
			List<Couple<Observation, Integer>> lObservations = lobs.get(0).getRight();


			// on ferme le noeud actuel
			if (!this.closedNodesContains(myPosition)) {
				this.closedNodes.add(new Couple<>(myPosition, lObservations));
				this.openNodes.remove(myPosition);

				this.myMap.addNode(myPosition, MapAttribute.closed);
			}

			// maj du path si nécessaire
			if(this.path.size()> 0 && this.path.get(0).equals(myPosition))
				this.path.remove(myPosition);
			if(this.path.size()> 0)
				nextNode = this.path.get(0);

			// 2) get the surrounding nodes and, if not in closedNodes, add them to open
			// nodes.

			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter = lobs.iterator();
			while (iter.hasNext()) {
				Couple<String, List<Couple<Observation, Integer>>> nodeId = iter.next();
				if (!this.closedNodesContains(nodeId.getLeft())) {
					if (!this.openNodes.contains(nodeId.getLeft())) {
						this.openNodes.add(nodeId.getLeft());
						this.myMap.addNode(nodeId.getLeft(), MapAttribute.open);
						this.myMap.addEdge(myPosition, nodeId.getLeft());
						this.edges.add(new Couple<>(myPosition, nodeId.getLeft()));
					} else {
						// the node exist, but not necessarily the edge
						if (!this.edges.contains(new Couple<>(myPosition, nodeId.getLeft()))) {
							this.myMap.addEdge(myPosition, nodeId.getLeft());
							this.edges.add(new Couple<>(myPosition, nodeId.getLeft()));
						}
					}
					if (nextNode == null) {
						nextNode = nodeId.getLeft();
						this.path.clear();
						this.path.add(nodeId.getLeft());
					}
				}
			}

			// example related to the use of the backpack for the treasure hunt
			int tresor = -1;
			Boolean b = false;
			for (Couple<Observation, Integer> o : lObservations) {
				if( ((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace()>0) {
					switch (o.getLeft()) {
					case DIAMOND:
					case GOLD:
						tresor = ((AbstractDedaleAgent) this.myAgent).pick();
						break;
					default:
						break;
					}
				}
			}


			// Trying to store everything in the tanker
			//			System.out.println(this.myAgent.getLocalName() + " - My current backpack capacity is:"
			//					+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
			//			System.out.println(this.myAgent.getLocalName()
			//					+ " - The agent tries to transfer is load into the Silo (if reachable); succes ? : "
			//					+ ((AbstractDedaleAgent) this.myAgent).emptyMyBackPack("Silo"));
			//			System.out.println(this.myAgent.getLocalName() + " - My current backpack capacity is:"
			//					+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());

			if (this.openNodes.isEmpty()) {
				this.trans = 8;
				this.finished = true;

			} else {

				if (nextNode == null) {
					// no directly accessible openNode
					// chose one, compute the path and take the first step.
					if(openNodes.size()>0) {
						this.path = myMap.getShortestPath(((AbstractDedaleAgent) this.myAgent).getCurrentPosition(),
								this.openNodes.get(0));
						nextNode = this.path.get(0);
					}
					else {

						Random r= new Random();
						int moveId=1+r.nextInt(lobs.size()-1);
						nextNode = lobs.get(moveId).getLeft();
						this.path.add(nextNode);

					}
				}
				
				// on ne se déplace que si le trésor (s'il existe) a pu être récupéré
				if(tresor != 0) {
					if (!((AbstractDedaleAgent) this.myAgent).moveTo(nextNode)) {
						this.trans = 2;
						this.finished = true;
					}
				} else {
					this.trans = 9;
					this.finished = true;
					
				}

			}
		}

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

	@Override
	public boolean done() {
		return this.finished;
	}

	@Override
	public int onEnd() {
		return this.trans;
	}

}