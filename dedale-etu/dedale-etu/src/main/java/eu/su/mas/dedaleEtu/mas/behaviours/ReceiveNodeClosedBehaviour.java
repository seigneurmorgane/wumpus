package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashSet;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import java.util.Set;

public class ReceiveNodeClosedBehaviour extends SimpleBehaviour{

	private static final long serialVersionUID = 8567659731496787061L;
	private boolean finished = false;
	private int trans = 5;
	private Set<String> otherCloseNode;
	
	public ReceiveNodeClosedBehaviour(final AbstractDedaleAgent myagent,Set<String> otherCloseNode) {
		super(myagent);
		this.otherCloseNode = otherCloseNode;
	}
	
	@Override
	public void action() {
		//1) receive the message
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);			

		final ACLMessage msg = this.myAgent.receive(msgTemplate);
		if (msg != null) {
			this.finished = true;
			try {
				this.otherCloseNode.addAll((HashSet<String>)msg.getContentObject());
				System.out.println(this.myAgent.getLocalName()+" <----Result received from "+msg.getSender().getLocalName());
				System.out.print("Close Node : ");
				for (String node : this.otherCloseNode) {
					System.out.print(node + " ");
				}
				System.out.println();
				this.finished = true;
				trans = 6;
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
		}else{
			this.trans = 5;
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
