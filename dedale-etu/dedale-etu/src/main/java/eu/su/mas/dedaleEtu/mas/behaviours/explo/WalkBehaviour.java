
package eu.su.mas.dedaleEtu.mas.behaviours.explo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.OneShotBehaviour;

public class WalkBehaviour extends OneShotBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6499471880574537333L;
	private MapRepresentation myMap;
	private List<Couple<String,List<Couple<Observation,Integer>>>> closedNodes;
	private List<Couple<String,List<Couple<Observation,Integer>>>> otherClosedNodes;
	private List<String> openNodes;
	private List<Couple<String,String>> Edges;
	private List<Couple<String,String>> otherEdges;
	private List<String> path;
	private List<Couple<Integer,Couple<String,List<String>>>> otherPaths;
	private List<String> otherOpenNodes;
	private List<Couple<String,String>> help;
	
	public WalkBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<Couple<String,List<Couple<Observation,Integer>>>> closedNodes,
			List<Couple<String,List<Couple<Observation,Integer>>>> otherClosedNodes, List<String> openNodes, List<Couple<String,String>> Edges, 
			List<Couple<String,String>> otherEdges, List<String> path, List<Couple<Integer,Couple<String,List<String>>>> otherPaths, List<String> otherOpenNodes, List<Couple<String,String>> help) {
		super(myagent);
		this.myMap = myMap;
		this.closedNodes = closedNodes;
		this.otherClosedNodes = otherClosedNodes;
		this.openNodes = openNodes;
		this.Edges = Edges;
		this.otherEdges = otherEdges;
		this.path = path;
		this.otherPaths = otherPaths;
		this.otherOpenNodes = otherOpenNodes;
		this.help = help;
	}

	@Override
	public void action() {
		// création de la map
		if(this.myMap == null) 
			this.myMap = new MapRepresentation();

		// position actuelle
		String myPosition = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		// ajoute les noeuds fermés récupérés à partir d'une communication
		Iterator<Couple<String,List<Couple<Observation,Integer>>>> iter = this.otherClosedNodes.iterator();
		while(iter.hasNext()) {
			Couple<String,List<Couple<Observation,Integer>>> node = iter.next();
			if(!this.closedNodesContains(node.getLeft())) {
				this.closedNodes.add(node);
				if(this.openNodes.contains(node.getLeft())) {
					this.openNodes.remove(node.getLeft());	
				}
				this.myMap.addNode(node.getLeft(),MapAttribute.closed);
			}
			int i = this.closedNodes.indexOf(node);
			if( i > 0 && ((node.getRight().isEmpty() && !this.closedNodes.get(i).getRight().isEmpty())
					|| (node.getRight().size() < this.closedNodes.get(i).getRight().size()))) {
				remplacerNoeud(node.getLeft(), node.getRight());
			}
		}
		this.otherClosedNodes.clear();

		//ajoute les noeuds ouverts récupérés à partir d'une communication
		Iterator<String> ite = this.otherOpenNodes.iterator();
		while(ite.hasNext()) {
			String openNode = ite.next();
			if(!this.closedNodesContains(openNode) && !this.openNodes.contains(openNode)) {
				this.openNodes.add(openNode);
				this.myMap.addNode(openNode, MapAttribute.open);
			}
		}
		this.otherOpenNodes.clear();

		// ajoute les arêtes des noeuds récupérés à partir d'une communication
		Iterator<Couple<String,String>> it = this.otherEdges.iterator();
		while(it.hasNext()) {
			Couple<String,String> edge = it.next();
			if(!this.Edges.contains(edge)) {
				this.Edges.add(edge);
				this.myMap.addEdge(edge.getRight(), edge.getLeft());
			}
		}
		this.otherEdges.clear();


		if (myPosition != null ) {
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			String nextNode=null;
			
			if(!this.help.isEmpty() && (this.path.size() == 0 || (this.path.size() == 1 && this.path.get(0).equals(myPosition)))) {
				Couple<String,String> way = this.help.remove(0);
				try {
					this.path = this.myMap.getShortestPath(way.getLeft(),way.getRight());
					this.path.add(0, way.getLeft());
				} catch(Exception e) {}
			}

			// maj du path si nécessaire
			if(this.path != null && this.path.size()>0 && this.path.get(0).equals(myPosition))
				this.path.remove(myPosition);
			if(this.path != null && this.path.size()> 0) {
				nextNode = this.path.get(0);
			}

			// remove the current node from openlist and add it to closedNodes.
			if(!this.closedNodesContains(myPosition))
				this.closedNodes.add(new Couple<>(myPosition,lobs.get(0).getRight()));
			this.openNodes.remove(myPosition);

			this.myMap.addNode(myPosition,MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.

			iter=lobs.iterator();
			while(iter.hasNext()){
				Couple<String, List<Couple<Observation, Integer>>> nodeId=iter.next();
				if (!this.closedNodesContains(nodeId.getLeft())){
					if (!this.openNodes.contains(nodeId.getLeft())){
						this.openNodes.add(nodeId.getLeft());
						this.myMap.addNode(nodeId.getLeft(), MapAttribute.open);
					}
					if (nextNode==null) {
						nextNode=nodeId.getLeft();
						if(this.path != null)
							this.path.clear();
						else
							this.path = new ArrayList<String>();
						this.path.add(nodeId.getLeft());
					}
				}

				if(!nodeId.getLeft().equals(myPosition)) {
					if(!this.Edges.contains(new Couple<>(myPosition,nodeId.getLeft())) && !this.Edges.contains(new Couple<>(nodeId.getLeft(),myPosition))) {
						this.Edges.add(new Couple<>(myPosition, nodeId.getLeft()));
					}
					this.myMap.addEdge(myPosition, nodeId.getLeft());
				}
				this.remplacerNoeud(nodeId.getLeft(), nodeId.getRight());
			}

			// si toujours pas de chemin à prendre 
			if (nextNode == null) {
				if (this.openNodes.size() == 0) {
					Random r= new Random();
					int moveId=1+r.nextInt(lobs.size()-1);
					nextNode = lobs.get(moveId).getLeft();
					if(this.path != null)
						this.path.clear();
					else
						this.path = new ArrayList<String>();
					this.path.add(nextNode);
				} else {
					try {
						this.path = myMap.getShortestPath(((AbstractDedaleAgent)this.myAgent).getCurrentPosition(), this.openNodes.get(0));
						nextNode=this.path.get(0);
					} catch(Exception e) {
						//e.printStackTrace();
						Random r= new Random();
						int moveId=1+r.nextInt(lobs.size()-1);
						nextNode = lobs.get(moveId).getLeft();
						this.path.clear();
						this.path.add(nextNode);

					}
				}
			}

			// d'autres chemins sont enregistrés, i.e. l'agent n'est pas seul dans les environs
			if (this.otherPaths.size()>0) {
				this.path = whichWay(lobs,nextNode);
				this.otherPaths.clear();
				if(this.path == null || this.path.size() == 0 )
					this.myAgent.doWait(500);
				else
					nextNode = this.path.get(0);
			}


			if(nextNode != null) {
				try {
					if(!((AbstractDedaleAgent)this.myAgent).moveTo(nextNode)) {
						if(this.openNodes.size() <= 1) {
							this.myAgent.doWait(500);
							List<String> chem = cheminNonBloque(lobs,nextNode);
							if(chem.size()>1) {
								Random r= new Random();
								int moveId=1+r.nextInt(chem.size()-1);
								nextNode = chem.get(moveId);
								this.path.clear();
								this.path.add(nextNode);
								((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
							} else if (chem.size()==1) {
								this.path.clear();
								nextNode = chem.get(0);
								this.path.add(nextNode);
								((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
							} else {
								this.myAgent.doWait(500);
							}
							
						}else{
							this.myAgent.doWait(100);
							this.openNodes.add(this.openNodes.remove(0));
						}
					} else
						this.path.remove(nextNode);


				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	public int onEnd() {
		return 2;
	}


	/**
	 * 
	 * @param nodeId
	 * @return Vrai si nodeId est déjà connu par l'agent en temps que noeud fermé
	 */
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

	/**
	 *  
	 * @return une liste de tous les chemins interdits
	 */
	public List<String> cheminsInterdits() {
		List<String> chemins= new ArrayList<String>();
		for (Couple<Integer,Couple<String,List<String>>> elem : this.otherPaths) {
			int taillechemin = elem.getRight().getRight().size();
			if (elem.getLeft()>0) {
				chemins.addAll(elem.getRight().getRight());
			} else if(this.path.size()< taillechemin) {
				chemins.addAll(elem.getRight().getRight());
			}
			else if ( this.path.size() == taillechemin && ((AbstractDedaleAgent)this.myAgent).getLocalName().compareTo(elem.getRight().getLeft()) > 0)  {
				chemins.addAll(elem.getRight().getRight());
			}
			else
				chemins.add(elem.getRight().getRight().get(0));
		}
		return chemins;
	}

	/**
	 * 
	 * @param lobs observation
	 * @return la liste des noeuds de l'observation lobs
	 */
	public static List<String> obsString(List<Couple<String,List<Couple<Observation,Integer>>>> lobs) {
		List<String> newlobs = new ArrayList<String>();
		for(Couple<String,List<Couple<Observation,Integer>>> o : lobs) {
			newlobs.add(o.getLeft());
		}
		newlobs.remove(0);
		return newlobs;
	}

	/**
	 * détermine quel chemin prendre pour ne bloquer aucun agent en fonction des priorités
	 * 
	 * @param lobs observations autour de soi
	 * @param p
	 * @return les chemins non bloqué
	 */
	public List<String> whichWay(List<Couple<String,List<Couple<Observation,Integer>>>> lobs, String p) {
		List<String> way = obsString(lobs);
		List<String> chems = this.cheminsInterdits();
		List<String> res = new ArrayList<String>();
		for (String c : chems) {
			if (way.contains(c)) {
				way.remove(c);
			}
		}
		if (way.size()>0) {

			if(p!= null && way.contains(p)) {
				res.add(p);
			} else {
				res.add(way.get(0));
			}
			return res;
		}
		else
			return res;
	}
	
	/**
	 * Remplace un noeud dans la liste des noeud fermé
	 * 
	 * @param node noeud remplacé
	 * @param obs observation du noeud remplacé
	 */
	public void remplacerNoeud(String node, List<Couple<Observation,Integer>> obs) {
		boolean b = true;
		Iterator<Couple<String,List<Couple<Observation,Integer>>>> iter = this.closedNodes.iterator();
		int i = 0;
		while(iter.hasNext() && b) {
			Couple<String,List<Couple<Observation,Integer>>> comp = iter.next();
			if(comp.getLeft().equals(node)) {
				i = this.closedNodes.indexOf(comp);
				this.closedNodes.set(i, new Couple<String,List<Couple<Observation,Integer>>>(node,obs));
				b = false;
			}
		}
	}

	public List<String> cheminsInterdits2() {
		List<String> chemins= new ArrayList<String>();
		for (Couple<Integer,Couple<String,List<String>>> elem : this.otherPaths) {
			chemins.add(elem.getRight().getRight().get(0));
		}
		return chemins;
	}


	public List<String> cheminNonBloque(List<Couple<String,List<Couple<Observation,Integer>>>> lobs, String p) {
		List<String> noeudsBloque = this.cheminsInterdits2();
		noeudsBloque.add(p);
		List<String> way = obsString(lobs);
		List<String> res = new ArrayList<String>();
		for (String c : noeudsBloque) {
			if (way.contains(c)) {
				way.remove(c);
			}
		}
		if (way.size()>0) {

			if(p!= null && way.contains(p)) {
				res.add(p);
			} else {
				res.addAll(way);
			}
			return res;
		}
		else
			return res;
	}
}
