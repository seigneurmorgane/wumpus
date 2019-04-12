package eu.su.mas.dedaleEtu.mas.behaviours.collect;

import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveNameBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567659731496787061L;
	private boolean finished = false;
	private int repeated = 0;
	private int trans = 4;
	private List<String> interlocutor;

	public ReceiveNameBehaviour(final AbstractDedaleAgent myagent, List<String> interlocutor) {
		super(myagent);
		this.interlocutor = interlocutor;
	}

	@Override
	public void action() {
		// 1) receive the message
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

		final ACLMessage msg = this.myAgent.receive(msgTemplate);

		if (msg != null) {
			this.interlocutor.add(msg.getContent());
			System.out.println(msg.getContent() + " want share info.");
			this.finished = true;
		} else {
			// when we receive no name we repeated one time
			if (this.repeated < 5) {
				this.myAgent.doWait(500);
				this.repeated++;
			} else {
				if (this.interlocutor.isEmpty()) {
					this.trans = 1;
				}
				this.repeated = 0;
				this.finished = true;
			}
		}
	}

	@Override
	public boolean done() {
		return this.finished;
	}

	@Override
	public int onEnd() {
		return 4;
	}
}
