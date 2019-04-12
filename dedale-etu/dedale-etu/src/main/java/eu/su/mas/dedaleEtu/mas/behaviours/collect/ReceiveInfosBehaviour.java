package eu.su.mas.dedaleEtu.mas.behaviours.collect;

import java.util.ArrayList;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ReceiveInfosBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567659731496787061L;
	private boolean finished = false;
	private int repeated = 0;
	private boolean step_back = false;
	private int trans = 7;

	private List<Couple<String, List<Couple<Observation, Integer>>>> otherClosedNodes = new ArrayList<Couple<String, List<Couple<Observation, Integer>>>>();
	private List<Couple<String, String>> otherEdges = new ArrayList<Couple<String, String>>();

	private List<String> path = new ArrayList<String>();

	public ReceiveInfosBehaviour(final AbstractDedaleAgent myagent,
			List<Couple<String, List<Couple<Observation, Integer>>>> otherClosedNodes,
			List<Couple<String, String>> otherEdges, List<String> path) {
		super(myagent);
		this.otherClosedNodes = otherClosedNodes;
		this.otherEdges = otherEdges;
		this.path = path;
	}

	@Override
	public void action() {
		// 1) receive the message
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

		final ACLMessage msg = this.myAgent.receive(msgTemplate);
		try {
			if (msg != null && msg.getContentObject().getClass().getSimpleName().equals("Couple")) {
				Couple<List<Couple<String, List<Couple<Observation, Integer>>>>, Couple<List<Couple<String, String>>, List<Integer>>> infos;
				infos = (Couple<List<Couple<String, List<Couple<Observation, Integer>>>>, Couple<List<Couple<String, String>>, List<Integer>>>) msg
						.getContentObject();

				this.otherClosedNodes = infos.getLeft();
				this.otherEdges = infos.getRight().getLeft();

				System.out.println(this.myAgent.getLocalName() + "---OtherCloseNode-----: " + this.otherClosedNodes);
				System.out.println(this.myAgent.getLocalName() + "---OtherEdge------: " + this.otherEdges);

				int interlocutor_backPackFreeSpace = infos.getRight().getRight().get(0);
				int interlocutor_path_length = infos.getRight().getRight().get(1);

				if (interlocutor_backPackFreeSpace > ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace()
						|| interlocutor_path_length > this.path.size()) {
					System.out.println("Je laisse passer.");
					this.trans = 6;
					this.step_back = true;
				}
				this.finished = true;
			} else {
				if (this.repeated > 5) {
					System.out.println("Je suis prioritaire.");
					this.repeated = 0;
					this.trans = 7;
					this.finished = true;
				} else {
					this.repeated++;
					this.myAgent.doWait(500);
				}
			}
		} catch (UnreadableException e) {
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
