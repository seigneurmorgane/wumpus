package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ReceiveEdgeBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8649618615291756116L;
	private boolean finished = false;
	private int trans = 7;
	private List<Couple<String,String>> otherEdge;

	public ReceiveEdgeBehaviour(final AbstractDedaleAgent myagent, List<Couple<String,String>> otherEdge) {
		super(myagent);
		this.otherEdge = otherEdge;
	}

	@Override
	public void action() {
		// 1) receive the message
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

		final ACLMessage msg = this.myAgent.receive(msgTemplate);
		if (msg != null) {
			System.out.println(this.myAgent.getLocalName() + " <----Result received from "
					+ msg.getSender().getLocalName() + " Edge");
			this.finished = true;
			this.trans = 1;
			try {
				this.otherEdge = (List<Couple<String,String>>) msg.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
		}else {
			this.trans = 7;
		}
	}

	@Override
	public boolean done() {
		return this.finished;
	}

	public int onEnd() {
		return this.trans;
	}

}
