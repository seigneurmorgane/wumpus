package eu.su.mas.dedaleEtu.mas.behaviours.collect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class SendInfosBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = -3429744247766338802L;
	private List<String> interlocutor;

	private List<Couple<String, List<Couple<Observation, Integer>>>> closedNodes;
	private List<Couple<String, String>> edges;

	private List<String> path = new ArrayList<String>();

	private int trans = 5;

	public SendInfosBehaviour(final AbstractDedaleAgent myagent, List<String> interlocutor,
			List<Couple<String, List<Couple<Observation, Integer>>>> closedNodes, List<Couple<String, String>> edges,
			List<String> path) {
		super(myagent);
		this.interlocutor = interlocutor;
		this.closedNodes = closedNodes;
		this.edges = edges;
		this.path = path;
	}

	@Override
	public void action() {
		if (this.interlocutor.isEmpty()) {
			this.trans = 1;
		} else {
			// 1°Create the message
			final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setSender(this.myAgent.getAID());
			msg.addReceiver(new AID(this.interlocutor.remove(0), AID.ISLOCALNAME));

			// 2° compute the random value
			List<Integer> comp = new ArrayList<Integer>();

			comp.add(((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
			comp.add(this.path.size());
			Couple<List<Couple<String, String>>, List<Integer>> rightInfos = new Couple<>(this.edges, comp);

			Couple<List<Couple<String, List<Couple<Observation, Integer>>>>, Couple<List<Couple<String, String>>, List<Integer>>> infos = new Couple<>(
					this.closedNodes, rightInfos);
			try {
				msg.setContentObject(
						(Couple<List<Couple<String, List<Couple<Observation, Integer>>>>, Couple<List<Couple<String, String>>, List<Integer>>>) infos);
			} catch (IOException e) {
				e.printStackTrace();
			}

			((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
		}
	}

	public int onEnd() {
		return 5;
	}

}
