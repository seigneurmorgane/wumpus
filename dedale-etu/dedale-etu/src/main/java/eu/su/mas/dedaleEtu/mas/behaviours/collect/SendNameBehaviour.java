package eu.su.mas.dedaleEtu.mas.behaviours.collect;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class SendNameBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567659731496787661L;

	public SendNameBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}

	@Override
	public void action() {
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();

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
						msg.setContent(((AbstractDedaleAgent) this.myAgent).getLocalName());
						((AbstractDedaleAgent) this.myAgent).sendMessage(msg);

						System.out
								.println(this.myAgent.getLocalName() + " sent to " + dfd_res.getName().getLocalName());
					}
				}
			}
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int onEnd() {
		return 3;
	}

}
