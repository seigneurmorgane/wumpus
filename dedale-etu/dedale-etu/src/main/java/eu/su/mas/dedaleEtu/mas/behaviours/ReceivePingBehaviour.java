package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Set;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceivePingBehaviour extends SimpleBehaviour{

	private static final long serialVersionUID = 8567659731496787061L;
	private boolean finished = false;
	private int trans = 0;
	private Set<String> closeNode;
	public ReceivePingBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}
	
	@Override
	public void action() {
		//1) receive the message
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);			

		final ACLMessage msg = this.myAgent.receive(msgTemplate);
		if (msg != null) {
			this.finished = true;
			this.trans = 4;
			System.out.println(this.myAgent.getLocalName()+" <----Result received from "+msg.getSender().getLocalName()+" ,content= "+msg.getContent());
		}else{
			this.trans = 3;
		}
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
