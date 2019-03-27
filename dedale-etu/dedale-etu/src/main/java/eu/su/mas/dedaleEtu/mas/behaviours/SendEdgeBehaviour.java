package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class SendEdgeBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = -6569481969026380668L;

	private int trans = 7;
	
	private List<Couple<String,String>> edge;
	
	public SendEdgeBehaviour(final AbstractDedaleAgent myagent,List<Couple<String,String>> edge) {
		super(myagent);
		this.edge = edge;
	}

	@Override
	public void action() {
		DFAgentDescription dfd = new DFAgentDescription();

		ServiceDescription sd = new ServiceDescription();

		sd.setType("Explo");
		dfd.addServices(sd);
		DFAgentDescription[] result;
		try {
			result = DFService.search(this.myAgent, dfd);
			if (result == null || result.length <= 0) {
				System.out.println("Search returns null");
			} else {
				for (DFAgentDescription dfd_res : result) {
					if (!dfd_res.getName().getLocalName().equals(this.myAgent.getLocalName())) {
						// 1°Create the message
						final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						msg.setSender(this.myAgent.getAID());
						msg.addReceiver(new AID(dfd_res.getName().getLocalName(), AID.ISLOCALNAME));

						// 2° compute the random value
						msg.setContentObject((ArrayList<Couple<String,String>>)this.edge);
						((AbstractDedaleAgent) this.myAgent).sendMessage(msg);

						System.out.println(this.myAgent.getLocalName() + " sent to " + dfd_res.getName().getLocalName() + " Edge");
					}
				}
			}
		} catch (FIPAException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.trans = 7;
	}

	public int onEnd() {
		return this.trans;
	}

}
