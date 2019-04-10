package eu.su.mas.dedaleEtu.mas.behaviours.explo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceivePingBehaviour extends SimpleBehaviour{

	private static final long serialVersionUID = 8567659731496787061L;
	private boolean finished = false;
	private int trans = 3;
	private MapRepresentation myMap;
	private List<String> openNodes = new ArrayList<String>();
	private List<String> path;
	
	public ReceivePingBehaviour(final AbstractDedaleAgent myagent,MapRepresentation myMap, List<String> openNodes,List<String> path) {
		super(myagent);
		this.myMap = myMap;
		this.openNodes = openNodes;
		this.path = path;
	}
	
	@Override
	public void action() {
		this.myAgent.doWait(750);
		System.out.println(this.myAgent.getLocalName()+" is waiting for another ping ...\n current open nodes : "+this.openNodes);
		
		//1) receive the message
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);			

		final ACLMessage msg = this.myAgent.receive(msgTemplate);
		if (msg != null) {
			this.finished = true;
			this.trans = 4;
			System.out.println(this.myAgent.getLocalName()+" <----Ping received from "+msg.getSender().getLocalName()+" ,content= "+msg.getContent());
		}else{
			/*this.trans = 1;
			this.finished=true;
			System.out.println(this.openNodes.size());
			String nextNode = null;
			if (path.size()>0) {
				nextNode = path.get(0);
				System.out.println("go to"+nextNode);
			}
			else {
				System.out.println("no path !");
				nextNode = ((AbstractDedaleAgent)this.myAgent).observe().get(0).getLeft();

				System.out.println("go to"+nextNode);
			}
			if( ! ((AbstractDedaleAgent)this.myAgent).moveTo(nextNode)) {
				this.trans = 3;
			}*/
			this.trans=3;
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
