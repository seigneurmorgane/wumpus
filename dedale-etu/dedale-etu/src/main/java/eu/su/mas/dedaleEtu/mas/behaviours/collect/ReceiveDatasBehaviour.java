package eu.su.mas.dedaleEtu.mas.behaviours.collect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ReceiveDatasBehaviour extends SimpleBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5507692493226819321L;
	private List<String> path;
	private List<Couple<String, List<Couple<Observation, Integer>>>> otherClosedNodes;
	private List<Couple<String, String>> otherEdges;
	private List<Couple<Integer,Couple<String,List<String>>>> otherPaths;
	private List<String> otherOpenNodes;
	private MapRepresentation myMap;
	private List<String> locationTanker;
	private boolean finished = false;
	private List<Couple<String,String>> help;


	public ReceiveDatasBehaviour(final AbstractDedaleAgent myagent, List<String> path, List<Couple<Integer,Couple<String,List<String>>>> otherPaths,
			List<Couple<String, List<Couple<Observation, Integer>>>> otherClosedNodes, List<Couple<String, String>> otherEdges, List<String> otherOpenNodes,
			MapRepresentation myMap, List<String> locationTanker, List<Couple<String,String>> help) {
		super(myagent);
		this.path = path;
		this.otherPaths = otherPaths;
		this.otherClosedNodes = otherClosedNodes;
		this.otherEdges = otherEdges;
		this.otherOpenNodes = otherOpenNodes;
		this.myMap = myMap;
		this.locationTanker = locationTanker;
		this.help = help;

	}


	@Override
	public void action() {
		this.myAgent.doWait(100);
		MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		ACLMessage msg = this.myAgent.receive(msgTemplate);
		while(msg != null) {
			try {
				if(msg.getContentObject().getClass().getSimpleName().equals("ArrayList")) {
					List<String> tmp = (List<String>)msg.getContentObject();
					List<String> tank = new ArrayList<String>();
					Iterator<String> iter = tmp.iterator();
					String S = iter.next();
					this.locationTanker.clear();
					this.locationTanker.add(S);
					while(iter.hasNext()) {
						S = iter.next();
						tank.add(S);
						this.locationTanker.add(S);
					}
					Couple<String,List<String>> infos1 = new Couple<String,List<String>>(this.locationTanker.get(0),tank);
					Couple<Integer,Couple<String,List<String>>> infos2 = new Couple<Integer,Couple<String,List<String>>>(0,infos1);
					this.otherPaths.add(infos2);
					
				} else {



					Couple<Object,Object> c_infos = (Couple<Object,Object>)msg.getContentObject();
					if (c_infos.getLeft().getClass().getSimpleName().equals("String")) {
						Couple<String,List<Integer>> help = (Couple<String,List<Integer>>) msg.getContentObject();
						int force = 0;
						int serrure = 0;
						for(Couple<Observation,Integer> obs : ((AbstractDedaleAgent)this.myAgent).getMyExpertise()) {
							switch(obs.getLeft()) {
							case LOCKPICKING:
								serrure = obs.getRight();
								break;
							case STRENGH:
								force = obs.getRight();
								break;
							default:
								break;
							}
						}

						if(serrure >= help.getRight().get(0) || force >= help.getRight().get(1)) {
							this.help.add(new Couple<>(((AbstractDedaleAgent)this.myAgent).getCurrentPosition(), help.getLeft()));
							
						}



					} else {
						Couple<List<Couple<String,List<Couple<Observation,Integer>>>>,Couple<List<String>,Couple<List<Couple<String,String>>,Couple<Integer,Couple<String,List<String>>>>>> infos = 
								(Couple<List<Couple<String,List<Couple<Observation,Integer>>>>,Couple<List<String>,Couple<List<Couple<String,String>>,Couple<Integer,Couple<String,List<String>>>>>>) msg.getContentObject();
						this.otherClosedNodes.addAll(infos.getLeft());
						this.otherOpenNodes.addAll(infos.getRight().getLeft());
						this.otherEdges.addAll(infos.getRight().getRight().getLeft());
						this.otherPaths.add(infos.getRight().getRight().getRight());

					}
				}

				msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				msg = this.myAgent.receive(msgTemplate);
			} catch (UnreadableException e) {
				e.printStackTrace();
			}

		}
		this.finished = true;


	}

	@Override
	public boolean done() {
		return this.finished;
	}

	public int onEnd() {
		return 1;
	}

}


