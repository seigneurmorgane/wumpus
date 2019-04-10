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

	public ReceiveInfosBehaviour(final AbstractDedaleAgent myagent,
			List<Couple<String, List<Couple<Observation, Integer>>>> otherClosedNodes,
			List<Couple<String, String>> otherEdges, List<String> openNodes, List<String> path) {
		super(myagent);
		this.otherClosedNodes = otherClosedNodes;
		this.otherEdges = otherEdges;
		this.openNodes = openNodes;
		this.path = path;
	}

	@Override
	public void action() {
		// 1) receive the message
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

		final ACLMessage msg = this.myAgent.receive(msgTemplate);
		if (msg != null) {
			System.out.println(
					this.myAgent.getLocalName() + " <----Result received from " + msg.getSender().getLocalName());
			this.finished = true;
			List<Integer> comp = new ArrayList<Integer>();

			try {
				System.out.println("j'essaie de vérifier le type");
				if (msg.getContentObject().getClass().getSimpleName().equals("Couple")) {
					Couple<List<Couple<String, List<Couple<Observation, Integer>>>>, Couple<List<Couple<String, String>>, List<Integer>>> infos = (Couple<List<Couple<String, List<Couple<Observation, Integer>>>>, Couple<List<Couple<String, String>>, List<Integer>>>) (msg.getContentObject());
					this.otherClosedNodes = infos.getLeft();
					this.otherEdges = infos.getRight().getLeft();
					comp = infos.getRight().getRight();

					this.trans = 1;

					System.out.println("taille des infos sur les capacités -> " + comp.size());
					System.out.println("le chemin de l'autre explo = " + comp.get(1));
					List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent)
							.observe();
					// pour savoir qui a la priorité
					if (comp.get(0) > 0 || (comp.get(0) == 0 && Math.abs(comp.get(1)) < this.path.size())) {
						String dest = this.path.get(0);
						
						if (lobs.size() > 0) {
							String nextNode = (lobs.get(0)).getLeft();
							int i = 1;
							while (nextNode.equals(dest) && i < lobs.size()) {
								nextNode = (lobs.get(i)).getLeft();
								i++;
							}

							if (!((AbstractDedaleAgent) this.myAgent).moveTo(nextNode)) {
								System.out.println(this.myAgent.getLocalName() + " couldn't move ! -> go to ping");
								this.trans = 2;
							}
						} else {
							this.trans = 8;
							System.out.println("Exploration successufully done, behaviour removed.");
						}
					} else {
						if(path.size()>0) 
							((AbstractDedaleAgent) this.myAgent).moveTo(this.path.get(0));
						else
							((AbstractDedaleAgent) this.myAgent).moveTo(lobs.get(0).getLeft());
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
