package eu.su.mas.dedaleEtu.mas.behaviours.explo;

import java.util.ArrayList;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ReceiveInfosBehaviour extends SimpleBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8861361278818411518L;
	private List<Couple<String, List<Couple<Observation, Integer>>>> otherClosedNodes;
	private List<Couple<String, String>> otherEdges;
	private List<String> openNodes;
	private List<String> path;
	private boolean finished = false;
	private int trans = 5;
	private List<String> nom_corres;

	public ReceiveInfosBehaviour(final AbstractDedaleAgent myagent,
			List<Couple<String, List<Couple<Observation, Integer>>>> otherClosedNodes,
			List<Couple<String, String>> otherEdges, List<String> openNodes, List<String> path, List<String> nom_corres) {
		super(myagent);
		this.otherClosedNodes = otherClosedNodes;
		this.otherEdges = otherEdges;
		this.openNodes = openNodes;
		this.path = path;
		this.nom_corres = nom_corres;
	}

	@Override
	public void action() {

		this.myAgent.doWait(1000);
		System.out.print("RECEIVEINFO DE "+this.myAgent.getLocalName()+" ("+this.nom_corres.get(0)+")");
		//System.out.println(this.myAgent.getLocalName()+" attend les infos de "+this.nom_corres.get(0));
		// 1) receive the message
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

		final ACLMessage msg = this.myAgent.receive(msgTemplate);
		if (msg != null && ((String)(msg.getContent())).charAt(0) != 'a') {
			System.out.println(
					this.myAgent.getLocalName() + " <----Result received from " + msg.getSender().getLocalName());
			List<Integer> comp = new ArrayList<Integer>();

			try {
				//System.out.println("j'essaie de vérifier le type");
				if (msg.getContentObject().getClass().getSimpleName().equals("Couple")) {
					this.finished=true;
					Couple<List<Couple<String, List<Couple<Observation, Integer>>>>, Couple<List<Couple<String, String>>, List<Integer>>> infos = (Couple<List<Couple<String, List<Couple<Observation, Integer>>>>, Couple<List<Couple<String, String>>, List<Integer>>>) (msg.getContentObject());
					this.otherClosedNodes = infos.getLeft();
					this.otherEdges = infos.getRight().getLeft();
					comp = infos.getRight().getRight();

					this.trans = 1;

					//System.out.println("taille des infos sur les capacités -> " + comp.size());
					//System.out.println("le chemin de l'explo '"+this.nom_corres+"' = " + comp.get(1));
					List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent)
							.observe();
					String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
					// pour savoir qui a la priorité
					/*if (comp.get(0) > 0 || (comp.get(0) == 0 && Math.abs(comp.get(1)) < this.path.size()) 
							|| (comp.get(0) == 0 && Math.abs(comp.get(1)) == this.path.size() && this.nom_corres.compareTo(this.myAgent.getLocalName())<0)) {
						System.out.println(this.myAgent.getLocalName()+" doit laisser passer");
						String dest = this.path.get(0);

						if (lobs.size() > 0) {
							String nextNode = (lobs.get(0)).getLeft();
							int i = 1;
							while ( i < lobs.size() && (nextNode.equals(((AbstractDedaleAgent)this.myAgent).getCurrentPosition()) || nextNode.equals(dest))) {
								nextNode = (lobs.get(i)).getLeft();
								i++;
							}
							System.out.println(this.myAgent.getLocalName()+" veut aller en "+nextNode);
							this.myAgent.doWait(500);
							if (!((AbstractDedaleAgent) this.myAgent).moveTo(nextNode)) {
								System.out.println(this.myAgent.getLocalName() + " couldn't move ! -> go to ping");
								this.trans = 2;
							}
						} else {
							this.trans = 8;
							System.out.println("Exploration successufully done, behaviour removed.");
						}
					} else {
						System.out.println(this.myAgent.getLocalName()+" a la priorité");
						System.out.println(this.myAgent.getLocalName()+" veut aller en "+this.path.get(0));
						//this.myAgent.doWait(1000);
						/*if(path.size()>0) 
							((AbstractDedaleAgent) this.myAgent).moveTo(this.path.get(0));
						else
							((AbstractDedaleAgent) this.myAgent).moveTo(lobs.get(0).getLeft());*/
					/*this.myAgent.doWait(500);
						this.trans = 1;
						if(this.path.get(0).equals(((AbstractDedaleAgent)this.myAgent).getCurrentPosition()))
							System.out.println("je veux aller la ou je suis déjà ...");
						((AbstractDedaleAgent) this.myAgent).moveTo(this.path.get(0));
						//this.trans=1;
					}*/

					String nextNode = null;
					//deuxième essai : pour donner la priorité à un des deux agents bloqués
					if(comp.get(0)> 0 || (comp.get(0)==0 && comp.get(1)> this.path.size()) || 
							(comp.get(0) == 0 && comp.get(1) == this.path.size() && this.nom_corres.get(0).compareTo(this.myAgent.getLocalName())<0)) {
						//dans ce cas là, l'agent courant n'a donc pas la priorité et doit changer de chemin
						this.myAgent.doWait(500);

						//on veut être sur que l'agent ne fait pas du "sur place"
						if(path.size()>0 && path.get(0).equals(myPosition)) {
							path.remove(path.get(0));
						}

						// cas normal (path n'est pas vide) :
						if (path.size() > 0) {
							System.out.println("AVANT : "+path.get(0));
							// on suppose que l'agent essayait de se rendre en path.get(0)
							int i = 1;
							nextNode = lobs.get(0).getLeft();
							while(i < lobs.size() && (nextNode.equals(myPosition) || nextNode.equals(path.get(0)))) {
								nextNode = lobs.get(i).getLeft();
								i++;
							}
							this.path.clear();
							this.path.add(nextNode);
						} else {
							System.out.println("AVANT (vide)");
							System.out.println("bizarrement, "+this.myAgent.getLocalName()+" n'a pas de chemin prédéfini ...");
							nextNode = lobs.get(0).getLeft();
							int i = 0;
							while(i < lobs.size() && (nextNode.equals(myPosition) )) {
								nextNode = lobs.get(i).getLeft();
								i++;
							}
							this.path.clear();
							this.path.add(nextNode);
						}
						System.out.println("APRES "+nextNode);
						System.out.println(this.myAgent.getLocalName()+"("+myPosition+") n'a pas la priorité et veut aller en "+nextNode);
					}
					else {
						this.myAgent.doWait(750);
						// dans ce cas là, l'agent courant a la priorité
						//cas normal (path nn vide)
						if(path.size()>0) {
							nextNode = this.path.get(0);
						} else {
							System.out.println("bizarrement, "+this.myAgent.getLocalName()+" n'a pas de chemin prédéfini ...");
							nextNode=lobs.get(1).getLeft();
							this.path.clear();
							this.path.add(nextNode);
						}
						System.out.println(this.myAgent.getLocalName()+"("+myPosition+") a la priorité et veut aller en "+nextNode);
					}
					this.trans = 1;

					System.out.println(this.myAgent.getLocalName()+" : from "+myPosition+" to "+this.path);
					try {
						if(!((AbstractDedaleAgent)this.myAgent).moveTo(nextNode)) {
							System.out.println(this.myAgent.getLocalName()+" n'arrive pas à bouger !");
							this.trans = 2;
						}
					}catch(RuntimeException e) {
						this.trans = 2;
						this.path.clear();
					}

				}
			} catch (UnreadableException e) {
				e.printStackTrace();
			}

		}

	}

	@Override
	public boolean done() {
		return finished;
	}

	public int onEnd() {
		return this.trans;
	}

}
