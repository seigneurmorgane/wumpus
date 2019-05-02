package eu.su.mas.dedaleEtu.mas.agents.dummies;

import java.util.ArrayList;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.explo.*;
import eu.su.mas.dedaleEtu.mas.behaviours.*;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;

public class ExploreAgent extends AbstractDedaleAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8514666733339763688L;
	private MapRepresentation myMap = null;
	private List<Couple<String,List<Couple<Observation,Integer>>>> closedNodes = new ArrayList<Couple<String,List<Couple<Observation,Integer>>>>();
	private List<Couple<String,List<Couple<Observation,Integer>>>> otherClosedNodes = new ArrayList<Couple<String,List<Couple<Observation,Integer>>>>();
	private List<String> openNodes = new ArrayList<String>();
	private List<Couple<String,String>> Edges = new ArrayList<Couple<String,String>>();
	private List<Couple<String,String>> otherEdges = new ArrayList<Couple<String,String>>();
	private List<String> path = new ArrayList<String>();
	private List<Couple<Integer,Couple<String,List<String>>>> otherPaths = new ArrayList<Couple<Integer,Couple<String,List<String>>>>();
	private List<String> otherOpenNodes = new ArrayList<String>();

	private List<Couple<String,String>> help = new ArrayList<>();

	protected void setup() {
		super.setup();

		List<Behaviour> lb = new ArrayList<Behaviour>();
		FSMBehaviour fsm = new FSMBehaviour(this) {

			/**
			 * 
			 */
			private static final long serialVersionUID = -768256229603748830L;

			public int onEnd() {
				System.out.println("FSM behaviour terminé");
				myAgent.doDelete();
				return super.onEnd();
			}

		};



		// definiton des etats
		fsm.registerFirstState(new InitDFBehaviour(this, "Explo"), "DF");
		fsm.registerState(new WalkBehaviour(this, this.myMap, this.closedNodes, this.otherClosedNodes,this.openNodes, this.Edges,this.otherEdges, this.path, this.otherPaths,this.otherOpenNodes,this.help), "Walk");
		fsm.registerState(new SendDatasBehaviour(this,this.closedNodes, this.Edges, this.path,this.openNodes),"Send");
		fsm.registerState(new ReceiveDatasBehaviour(this,this.path,this.otherPaths,this.otherClosedNodes,this.otherEdges,this.otherOpenNodes,this.myMap,this.help),"Receive");
		
		// definition des transaction
		fsm.registerDefaultTransition("DF", "Walk");
		fsm.registerTransition("Walk", "Send", 2);
		fsm.registerTransition("Send","Receive", 3);
		fsm.registerTransition("Receive", "Walk", 1);
		
		lb.add(fsm);
		addBehaviour(new startMyBehaviours(this, lb));
		System.out.println("the  agent " + this.getLocalName() + " is started");


	}




}
