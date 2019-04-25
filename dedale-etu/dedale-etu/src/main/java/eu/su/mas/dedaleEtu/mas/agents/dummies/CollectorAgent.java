package eu.su.mas.dedaleEtu.mas.agents.dummies;

import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.InitDFBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.collect.*;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

import java.util.ArrayList;
import java.util.List;

import dataStructures.tuple.Couple;

import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.Behaviour;

/**
 * This dummy collector moves randomly, tries all its methods at each time step,
 * store the treasure that match is treasureType in its backpack and intends to
 * empty its backPack in the Tanker agent. @see
 * {@link RandomWalkExchangeBehaviour}
 * 
 * @author hc
 *
 */
public class CollectorAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = -1784844593772918359L;

	/**
	 * when block, interlocutor name
	 */
	//private List<String> interlocutor = new ArrayList<>();

	private MapRepresentation myMap;
	private List<Couple<String, List<Couple<Observation, Integer>>>> closedNodes = new ArrayList<Couple<String, List<Couple<Observation, Integer>>>>();
	private List<String> openNodes = new ArrayList<String>();
	private List<Couple<String, String>> Edges = new ArrayList<Couple<String, String>>();

	private List<Couple<String, String>> otherEdges = new ArrayList<Couple<String, String>>();
	private List<Couple<String, List<Couple<Observation, Integer>>>> otherClosedNodes = new ArrayList<Couple<String, List<Couple<Observation, Integer>>>>();

	private List<String> path = new ArrayList<String>();
	private List<String> otherOpenNodes = new ArrayList<String>();

	private List<Couple<Integer,Couple<String,List<String>>>> otherPaths = new ArrayList<Couple<Integer,Couple<String,List<String>>>>();

	//private Couple<String, List<Couple<Observation, Integer>>> nextNode;

	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 1) set the agent
	 * attributes 2) add the behaviours
	 * 
	 */
	protected void setup() {

		super.setup();

		List<Behaviour> lb = new ArrayList<Behaviour>();

		FSMBehaviour fsm = new FSMBehaviour(this) {
			private static final long serialVersionUID = 4338232456572336591L;

			public int onEnd() {
				System.out.println("FSM behaviour terminé");
				myAgent.doDelete();
				return super.onEnd();
			}
		};

		/************************************************
		 * 
		 * Define State
		 * 
		 ************************************************/
		fsm.registerFirstState(new InitDFBehaviour(this, "Collect"), "DF");
		/*fsm.registerState(new WalkBehaviour(this, this.myMap, this.closedNodes, this.openNodes, this.edges,
				this.otherClosedNodes, this.otherEdges, this.path), "Walk");
		fsm.registerState(new SendNameBehaviour(this), "SendName");
		fsm.registerState(new ReceiveNameBehaviour(this, this.interlocutor), "RName");
		fsm.registerState(new SendInfosBehaviour(this, this.interlocutor, this.closedNodes, this.edges, this.path),
				"SInfos");
		fsm.registerState(new ReceiveInfosBehaviour(this, this.otherClosedNodes, this.otherEdges, this.path), "RInfos");
		fsm.registerState(new WalkBackBehaviour(this, this.nextNode), "WalkBack");
		fsm.registerState(new SortBehaviour(this, this.myMap, this.openNodes, this.closedNodes, this.otherClosedNodes,
				this.edges, this.otherEdges), "Filtre");
		fsm.registerState(new RandomWalkEndBehaviour(this), "RWalk");
		fsm.registerState(new HelpRequiredBehaviour(this),"HReq");*/
		fsm.registerState(new WalkBehaviour(this, this.myMap, this.closedNodes, this.otherClosedNodes,this.openNodes, this.Edges,this.otherEdges, this.path, this.otherPaths,this.otherOpenNodes), "Walk");;;
		fsm.registerState(new SendDatasBehaviour(this,this.closedNodes, this.Edges, this.path,this.openNodes),"Send");
		fsm.registerState(new ReceiveDatasBehaviour(this,this.path,this.otherPaths,this.otherClosedNodes,this.otherEdges,this.otherOpenNodes,this.myMap),"Receive");
		fsm.registerState(new HelpRequiredBehaviour(this),"Help");
		

		/************************************************
		 * 
		 * Define Transition
		 * 
		 ************************************************/
		fsm.registerDefaultTransition("DF", "Walk");
		/*fsm.registerTransition("Walk", "Walk", 1);
		fsm.registerTransition("Walk", "RWalk", 8);
		fsm.registerTransition("Walk", "SendName", 2);
		fsm.registerTransition("SendName", "RName", 3);
		fsm.registerTransition("RName", "SInfos", 4);
		fsm.registerTransition("RName", "Walk", 1);
		fsm.registerTransition("SInfos", "RInfos", 5);
		fsm.registerTransition("SInfos", "Walk", 1);
		fsm.registerTransition("RInfos", "WalkBack", 6);
		fsm.registerTransition("RInfos", "Filtre", 7);
		fsm.registerTransition("WalkBack", "Filtre", 7);
		fsm.registerTransition("Filtre", "Walk", 1);*/
		/*fsm.registerTransition("HReq","HReq",9);
		fsm.registerTransition("HReq","Walk",1);
		fsm.registerTransition("Walk","HReq",9);*/
		fsm.registerTransition("Walk", "Send",2);
		fsm.registerTransition("Walk", "Help", 4);
		fsm.registerTransition("Send", "Receive",3);
		fsm.registerTransition("Receive", "Walk",1);
		fsm.registerTransition("Help","Send",2);

		/************************************************
		 * 
		 * ADD the behaviours
		 * 
		 ************************************************/
		lb.add(fsm);

		addBehaviour(new startMyBehaviours(this, lb));

		System.out.println("the  agent " + this.getLocalName() + " is started");

	}

	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown() {

	}

}
